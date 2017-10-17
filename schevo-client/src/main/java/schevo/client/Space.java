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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import schevo.UriConfigs;
import schevo.client.SchevoClient.HttpGenericResponseHandler;
import schevo.client.SchevoClient.HttpJsonResponseHandler;
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
		fetchDocumentList();
		fetchContent();
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
	private final void fetchDocumentList() throws SchevoClientException {
		// target/request uri
		String targetUri = this.url + UriConfigs.REPOSITORY_VERSION_URI//
				.replace("{workspaceName}", spaceRef.getWorkspace()) //
				.replace("{repositoryName}", spaceRef.getRepository()) //
				.replace("{repositoryVersionName}", spaceRef.getVersion());

		// execute request

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpJsonResponseHandler respJson = new HttpJsonResponseHandler();
			// execute request
			httpClient.execute(RequestBuilder.get(targetUri).build(), respJson);
			// parse documents from server
			parseDocumentListFromSever(respJson.getResponse());

		} catch (IOException e1) {
			log.error("Failed to fetch document list from space: " + targetUri + " ,reason: " + e1.getMessage(), e1);
			throw new SchevoClientException("Failed to fetch document list from space: " + targetUri + " ,reason: " + e1.getMessage(), e1);
		}

	}

	private final void parseDocumentListFromSever(JSONObject resp) {
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

	}

	/**
	 * fetch content i.e. document(s)/schema(s) from schevo
	 * 
	 * @throws SchevoClientException
	 */
	private final void fetchContent() throws SchevoClientException {
		String targetUri = this.url + UriConfigs.FETCH_URI;

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			URI uri = new URIBuilder(targetUri)//
					.addParameter(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath())//
					.build();

			// generic response - download content zip file and extract it to lcoal FS
			HttpGenericResponseHandler<Object> response = new HttpGenericResponseHandler<Object>() {

				@Override
				protected final void handleResponseOK(HttpResponse httpResponse) throws ClientProtocolException, IOException {
					try {
						writeAndExtractFetchZipInLocalFs(httpResponse.getFirstHeader(UriConfigs.HEADER_FETCH_NAME), httpResponse.getEntity().getContent());
					} catch (UnsupportedOperationException | SchevoClientException e) {

					}
				}
			};

			// HttpResponseBinary responseBinary = new HttpResponseBinary();
			httpClient.execute(new HttpGet(uri), response);

			// call if there was somewhere error
			response.getResponse();

		} catch (IOException | URISyntaxException e1) {
			log.error("Failed to fetch data: " + targetUri + " ,reason: " + e1.getMessage(), e1);
			throw new SchevoClientException("Failed to fetch data: " + targetUri + " ,reason: " + e1.getMessage(), e1);
		}

		// try {
		// URI uri = new URIBuilder(targetUri)//
		// .addParameter(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath())//
		// .build();
		//
		// SchevoHttpClient.doGetBinary(uri, new RepsonseHandler() {
		//
		// @Override
		// public void responseContent(CloseableHttpResponse response) throws
		// UnsupportedOperationException, IOException {
		// writeAndExtractFetchZipInLocalFs(response.getFirstHeader(UriConfigs.HEADER_FETCH_NAME),
		// response.getEntity().getContent());
		// }
		// });
		//
		// } catch (IOException | URISyntaxException e) {
		// throw new SchevoClientException("Failed to fetch data, reason: " +
		// e.getMessage(), e);
		// }

	}

	/**
	 * 
	 * fetched zip file write to local fs and extract it
	 * 
	 * @param fnHeader
	 * @param isFile
	 * @throws IOException
	 * @throws SchevoClientException
	 */
	private final void writeAndExtractFetchZipInLocalFs(org.apache.http.Header fnHeader, InputStream isFile) throws SchevoClientException {
		String fileName = (fnHeader == null ? null : fnHeader.getValue());
		if (Utils.strOrNull(fileName) == null) {
			fileName = UUID.randomUUID().toString() + ".zip";
		}

		try {

			//
			// prepare dirs
			//
			Path contentDir = FileWalker.mkDirs(homeDir.resolve("content"));
			Path fetchFile = FileWalker.mkFile(homeDir.resolve("fetch").resolve(fileName));

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
			throw new SchevoClientException("Failed to fetch or extract the zip file from schevo, reason: " + e.getMessage(), e);
		}

	}

	/**
	 * push selected documents to schevo, and update the local repository
	 * 
	 * @param filter
	 * @throws SchevoClientException
	 */
	public final void push(PushDocumentFilter filter) throws SchevoClientException {
		Path pushFile = createPushFile(filter);

		// create multipart request
		HttpEntity data = MultipartEntityBuilder.create()//
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)//
				.addBinaryBody("file", pushFile.toFile(), ContentType.APPLICATION_OCTET_STREAM, pushFile.getFileName().toString()).build();

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			//
			// response handler
			HttpJsonResponseHandler responseJson = new HttpJsonResponseHandler();
			// execute request
			httpclient.execute(
					//
					RequestBuilder.post(this.url + UriConfigs.PUSH_URI)//
							.addParameter(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath()).setEntity(data).build()
					//
					, responseJson);

			// get json response
			JSONObject json = responseJson.getResponse();
			// TODO: and what with response?
		} catch (IOException e) {
			log.error("Failed to push: " + filter.getSource() + " ,reason: " + e.getMessage(), e);
			throw new SchevoClientException(e.getMessage(), e);
		}

	}

	/**
	 * prepare push 'file'
	 * 
	 * @param filter
	 * @return
	 * @throws SchevoClientException
	 */
	private final Path createPushFile(PushDocumentFilter filter) throws SchevoClientException {
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
			log.error("Failed to push, reason: " + e.getMessage(), e);
			throw new SchevoClientException("Failed to push, reason: " + e.getMessage(), e);
		}

		return pushFile;

	}
}