package schevo.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import schevo.UriConfigs;
import schevo.client.SchevoHttpClient.RepsonseHandler;
import schevo.common.BasicFile;
import schevo.common.FileWalker;
import schevo.common.SpaceRef;
import schevo.common.Utils;

/**
 * 
 * @author Tome (tomecode.com)
 *
 */

public final class Space {

	private static final Logger log = Logger.getLogger(Space.class);

	// private final StampedLock sl = new StampedLock();

	private String url;
	private SpaceRef spaceRef;

	private List<Document> documents;

	private Path homeDir;

	private Space() {
		this.documents = new ArrayList<>();
	}

	/**
	 * 
	 * @param url
	 * @param spaceHomeDir
	 * @param workspace
	 * @param repository
	 * @param version
	 */
	Space(String url, Path spaceHomeDir, String workspace, String repository, String version) {
		this();
		this.url = url;
		this.spaceRef = new SpaceRef(workspace, repository, version);
		this.homeDir = spaceHomeDir.resolve(spaceRef.getPath());
	}

	public final List<Document> getDocuments() {
		return this.documents;
	}

	/**
	 * fetch space(data, document(s)/schema(s),...)
	 * 
	 * @return
	 * @throws Exception
	 */
	public final Space fetch() throws SchevoClientException {
		try {
			fetchData();
			fetchContent();

		} catch (SchevoClientException e) {
			throw e;
		} catch (Exception e) {
			SchevoClientException ex = new SchevoClientException("Failed to fetch space: " + spaceRef + ", reason: " + e.getMessage(), e);
			log.error(ex.getMessage(), e);
			throw ex;
		}
		// finally {
		// sl.unlockWrite(wStamp);
		// }
		return this;

	}

	/**
	 * 
	 * fetch data about space
	 * 
	 * @throws UnirestException
	 * @throws JSONException
	 * @throws SchevoClientException
	 */
	private final void fetchData() throws SchevoClientException {
		// get info about space
		try {
			JSONObject resp = SchevoHttpClient.doGetJson(
					//
					new URIBuilder(this.url + UriConfigs.REPOSITORY_VERSION_URI//
							.replace("{workspaceName}", spaceRef.getWorkspace()) //
							.replace("{repositoryName}", spaceRef.getRepository()) //
							.replace("{repositoryVersionName}", spaceRef.getVersion())) //
									.build()
			//
			);

			JSONArray adocs = resp.getJSONArray("documents");
			for (int i = 0; i <= adocs.length() - 1; i++) {
				JSONObject jo = adocs.getJSONObject(i);

				Document document = new Document();
				if (jo.has("id")) {
					document.setId(jo.getString("id"));
				}
				if (jo.has("hash")) {
					document.setHash(jo.getString("hash"));
				}
				if (jo.has("path")) {
					document.setPath(jo.getString("path"));
				}
				if (jo.has("type")) {
					document.setType(jo.getString("type"));
				}
				this.documents.add(document);
			}

		} catch (IOException | URISyntaxException e) {
			throw new SchevoClientException("Failed to fetch data, reason: " + e.getMessage(), e);
		}

	}

	/**
	 * fetch content i.e. document(s)/schema(s) from schevo
	 * 
	 * @throws UnirestException
	 * @throws JSONException
	 * @throws SchevoClientException
	 * @throws IOException
	 */
	private final void fetchContent() throws SchevoClientException {

		// SchevoHttpClient.doGet(this.url + SchevoUri.FETCH_URI)

		try {

			URI uri = new URIBuilder(this.url + UriConfigs.FETCH_URI)//
					.addParameter(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath())//
					.build();

			SchevoHttpClient.doGetBinary(uri, new RepsonseHandler() {

				@Override
				public void responseContent(CloseableHttpResponse response) throws UnsupportedOperationException, IOException {
					writeAndExtractFetchZipInLocalFs(response.getFirstHeader(UriConfigs.HEADER_FETCH_NAME), response.getEntity().getContent());
				}
			});

		} catch (IOException | URISyntaxException e) {
			throw new SchevoClientException("Failed to fetch data, reason: " + e.getMessage(), e);
		}

	}

	/**
	 * 
	 * fetched zip file write to local fs and extract it
	 * 
	 * @param fnHeader
	 * @param isFile
	 * @throws IOException
	 */
	private final void writeAndExtractFetchZipInLocalFs(org.apache.http.Header fnHeader, InputStream isFile) throws IOException {
		String fileName = (fnHeader == null ? null : fnHeader.getValue());
		if (Utils.strOrNull(fileName) == null) {
			fileName = UUID.randomUUID().toString() + ".zip";
		}

		//
		// prepare dirs
		//
		Path contentDir = FileWalker.mkDirs(homeDir.resolve("content"));
		Path fetchFile = FileWalker.mkFile(homeDir.resolve("fetch").resolve(fileName));
		try {
			// copy, save to FS
			try (ReadableByteChannel src = Channels.newChannel(isFile)) {
				try (WritableByteChannel dest = Channels.newChannel(Files.newOutputStream(fetchFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
					FileWalker.channelCopy(src, dest);
				}
			}
			// uznip to content dir
			FileWalker.unizip(fetchFile, contentDir);

		} catch (IOException e) {
			log.error("Failed to fetch or extract the zip file from schevo, reason: " + e.getMessage(), e);
			throw e;
		}

	}

	/**
	 * push selected documents to schevo, and update the local repository
	 * 
	 * @param filter
	 * @throws SchevoClientException
	 */
	public final void push(PushDocumentFilter filter) throws SchevoClientException {
		Path source = filter.getSource();

		if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
			throw new SchevoClientException("Source dir: " + source + " not exists!");
		}

		//
		// create zip file for push
		//
		Path pushFile = null;
		try {
			List<BasicFile> filesForPush = FileWalker.listFiles(filter.getSource());
			// create temp file
			pushFile = FileWalker.mkFile(this.homeDir.resolve("push").resolve(System.currentTimeMillis() + ".zip"));

			try (OutputStream osZip = Files.newOutputStream(pushFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
				FileWalker.toZip(osZip, filesForPush);
			}
		} catch (Exception e) {
			throw new SchevoClientException("Failed to create list of ifles for push, reason: " + e.getMessage(), e);
		}

		try {
			//
			// // create multipart body
			// RequestBody body = new MultipartBody.Builder()//
			// .setType(MultipartBody.FORM)//
			// .addFormDataPart("file", pushFile.getFileName().toString(),
			// RequestBody.create(MEDIATYPE_ZIP, pushFile.toFile())).build();
			// // build url
			//
			// HttpUrl url = new HttpUrl.Builder()//
			// .scheme("http")//
			// .host("localhost").port(9999)//
			// .addPathSegment("documents").addPathSegment("v1").addPathSegment("push")//
			// .addQueryParameter("spaceRef", spaceRef)//
			// .build();
			//
			// Request request = new Request.Builder().url(url).post(body).build();
			//
			// Response response = httpClient.newCall(request).execute();
			// if (!response.isSuccessful()) {
			//
			// String result = response.body().string();
			// throw new SchevoClientException("Failed to push, reason: " + result);
			// }
		} catch (Exception e) {
			throw new SchevoClientException(e.getMessage(), e);
		}
	}
}