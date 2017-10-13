package schevo.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import schevo.common.FileWalker;
import schevo.common.Utils;

/**
 * 
 * @author Tome (tomecode.com)
 *
 */
public final class SchevoClient {

	private static final Logger log = Logger.getLogger(SchevoClient.class);

	private static final String URL_BASE = "/spaces/v1";
	private static final String URL_WORKSPACE = URL_BASE + "/{workspaceName}";
	private static final String URL_REPOSITORY = URL_WORKSPACE + "/{repositoryName}";
	private static final String URL_REPOSITORY_VERSION = URL_REPOSITORY + "/{spaceRef}";
	private static final String URL_FETCHDOCUMENTS = URL_BASE + "/fetchDocuments";

	public static final String SCHEVO_HOME_DIR = "schevo.home";

	/**
	 * url to schevo server
	 */
	private final String schevoUrl;

	/**
	 * home dir
	 */
	private Path schevoHomeDir;

	/**
	 * 
	 * @param schevoUrl
	 * @throws SchevoClientException
	 */
	public SchevoClient(String schevoUrl) throws SchevoClientException {
		this(schevoUrl, null);
	}

	public SchevoClient(String schevoUrl, String schevoHome) throws SchevoClientException {
		if (Utils.strOrNull(schevoHome) == null) {
			schevoHome = Utils.strOrNull(System.getProperty(SCHEVO_HOME_DIR));
			if (schevoHome == null) {
				schevoHome = System.getProperty("user.dir");
			}
		}

		try {
			this.schevoHomeDir = Paths.get(schevoHome, ".schevo");
			FileWalker.mkDirs(this.schevoHomeDir);
		} catch (IOException e1) {
			throw new SchevoClientException("Failed to create schevo home dir: " + schevoHome + " ,reason: " + e1.getMessage(), e1);
		}

		try {
			this.schevoUrl = new URL(schevoUrl).toString();
		} catch (MalformedURLException e) {
			throw new SchevoClientException("Failed to validate schevo server url: " + schevoUrl + " ,reason: " + e.getMessage(), e);
		}
	}

	/**
	 * get(fetch) workspaces from schevo server
	 * 
	 * @return
	 * @throws SchevoClientException
	 */
	public final List<String> listWorkspaces() throws SchevoClientException {
		try {
			return toList(Unirest.get(this.schevoUrl + URL_BASE).asJson().getBody(), "workspaces");
		} catch (UnirestException e) {
			log.error("Failed to get list workspaces!, reason:  " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get workspaces!, reason:  " + e.getMessage(), e);
		}
	}

	public final List<String> listRepositories(String workspaceName) throws SchevoClientException {
		try {
			return toList(Unirest.get(this.schevoUrl + URL_WORKSPACE)//
					.routeParam("workspaceName", workspaceName).asJson().getBody(), "repositories");
		} catch (UnirestException e) {
			log.error("Failed to get list repositories from workspace!, reason:  " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get workspaces!, reason:  " + e.getMessage(), e);
		}
	}

	public final List<String> listVersions(String workspaceName, String repositoryName) throws SchevoClientException {
		try {
			return toList(Unirest.get(this.schevoUrl + URL_REPOSITORY)//
					.routeParam("workspaceName", workspaceName)//
					.routeParam("repositoryName", repositoryName)//
					.asJson().getBody(), "versions");
		} catch (UnirestException e) {
			log.error("Failed to get list repositories from workspace!, reason:  " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get workspaces!, reason:  " + e.getMessage(), e);
		}
	}

	private final List<String> toList(JsonNode jsonNode, String listName) {
		List<String> list = new ArrayList<>();
		JSONArray respWorkspaces = (JSONArray) jsonNode.getObject().get(listName);
		for (int i = 0; i <= respWorkspaces.length() - 1; i++) {
			list.add(respWorkspaces.getString(i));
		}
		return list;
	}

	/**
	 * get data about space
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @param repositoryVersionName
	 * @return
	 * @throws SchevoClientException
	 */
	public final Space getSpace(String workspaceName, String repositoryName, String repositoryVersionName) throws SchevoClientException {
		// create new space
		return new Space(this.schevoUrl, this.schevoHomeDir, workspaceName, repositoryName, repositoryVersionName).fetch();
	}

	/**
	 * Resent the particular version of space
	 * 
	 * @author Tome (tomecode.com)
	 *
	 */
	public class Space {

		// private final StampedLock sl = new StampedLock();

		private String url;

		// private String workspaceName;
		// private String repositoryName;
		// private String repositoryVersionName;

		private String spaceRef;

		private List<Document> documents;

		private Path homeDir;

		/**
		 * 
		 * @param url
		 * @param spaceHomeDir
		 * @param workspaceName
		 * @param repositoryName
		 * @param repositoryVersionName
		 */
		Space(String url, Path spaceHomeDir, String workspaceName, String repositoryName, String repositoryVersionName) {
			this.documents = new ArrayList<>();
			this.url = url;
			this.spaceRef = workspaceName + "/" + repositoryName + "/" + repositoryVersionName;
			// home dir
			this.homeDir = spaceHomeDir.resolve(spaceRef);
		}

		public final List<Document> getDocuments() {
			// long rStamp = sl.readLock();
			// try {
			return this.documents;
			// } finally {
			// sl.unlockRead(rStamp);
			// }
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
		 * fetch data about space
		 * 
		 * @throws UnirestException
		 * @throws JSONException
		 * @throws SchevoClientException
		 */
		private final void fetchData() throws UnirestException, JSONException, SchevoClientException {
			// get info about space
			HttpResponse<JsonNode> resp = Unirest.get(this.url + URL_REPOSITORY_VERSION)//
					.routeParam("spaceRef", spaceRef).asJson();

			if (resp.getStatus() != HttpStatus.SC_OK) {
				throw new SchevoClientException(resp.getBody().getObject().getString("message"));
			}

			JSONObject body = resp.getBody().getObject();
			// parse documents
			JSONArray adocs = (JSONArray) body.get("documents");
			for (int i = 0; i <= adocs.length() - 1; i++) {
				JSONObject jo = adocs.getJSONObject(i);

				Document document = new Document();
				document.setId(jo.getString("id"));
				document.setHash(jo.getString("hash"));
				document.setPath(jo.getString("path"));
				document.setType(jo.getString("type"));

				this.documents.add(document);
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
		private final void fetchContent() throws UnirestException, JSONException, SchevoClientException, IOException {

			HttpResponse<InputStream> resp = Unirest.get(this.url + URL_FETCHDOCUMENTS)//
					.queryString("spaceRef", spaceRef).asBinary();

			// check if error response
			if (resp.getStatus() != HttpStatus.SC_OK) {

				throw new SchevoClientException(new JSONObject(Utils.inputStreamToStr(resp.getBody())).getString("message"));
			}

			String fileName = resp.getHeaders().getFirst("schevo-fetch-name");
			if (fileName == null) {
				fileName = UUID.randomUUID().toString() + ".zip";
			}

			//
			// prepare dirs
			//

			Path contentDir = FileWalker.mkDirs(homeDir.resolve("content"));
			Path fetchFile = FileWalker.mkFile(homeDir.resolve("fetch").resolve(fileName));

			// copy, save to FS
			try (ReadableByteChannel src = Channels.newChannel(resp.getBody())) {
				try (WritableByteChannel dest = Channels.newChannel(Files.newOutputStream(fetchFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
					FileWalker.channelCopy(src, dest);
				}
			}
			// uznip to content dir
			FileWalker.unizip(fetchFile, contentDir);

		}

		/**
		 * 
		 * @param filter
		 * @throws SchevoClientException 
		 */
		public final void push(PushFilter filter) throws SchevoClientException {
			Path source = filter.getSource();

			if (!Files.exists(source, LinkOption.NOFOLLOW_LINKS)) {
				throw new SchevoClientException("Source dir: " + source + " not exists!");
			}
			
			
			

		}

		/**
		
		 */
		public final class SpaceRef {

			private String workspaceName;
			private String repositoryName;
			private String repositoryVersionName;

			public SpaceRef() {
			}

			public SpaceRef(String workspaceName, String repositoryName, String repositoryVersionName) {
				this.workspaceName = workspaceName;
				this.repositoryName = repositoryName;
				this.repositoryVersionName = repositoryVersionName;
			}

			public final String getWorkspaceName() {
				return workspaceName;
			}

			public final void setWorkspaceName(String workspaceName) {
				this.workspaceName = workspaceName;
			}

			public final String getRepositoryName() {
				return repositoryName;
			}

			public final void setRepositoryName(String repositoryName) {
				this.repositoryName = repositoryName;
			}

			public final String getRepositoryVersionName() {
				return repositoryVersionName;
			}

			public final void setRepositoryVersionName(String repositoryVersionName) {
				this.repositoryVersionName = repositoryVersionName;
			}

			@Override
			public final String toString() {
				return workspaceName + "/" + repositoryName + "/" + repositoryVersionName;
			}
		}

		/**
		 * class represent document(schema) file
		 * 
		 * @author Tome (tomecode.com)
		 *
		 */
		public final class Document {

			/**
			 * short id
			 */
			private String id;

			/**
			 * unique hash by content
			 */
			private String hash;

			/**
			 * path/file name
			 */
			private String path;

			/**
			 * import time
			 */
			private long time;

			/**
			 * version
			 */
			private String version;

			/**
			 * type of document
			 */
			private String type;

			public Document() {

			}

			public final String getId() {
				return id;
			}

			public final void setId(String id) {
				this.id = id;
			}

			public final String getHash() {
				return hash;
			}

			public final void setHash(String hash) {
				this.hash = hash;
			}

			public final String getPath() {
				return path;
			}

			public final void setPath(String path) {
				this.path = path;
			}

			public final long getTime() {
				return time;
			}

			public final void setTime(long time) {
				this.time = time;
			}

			public final String getVersion() {
				return version;
			}

			public final void setVersion(String version) {
				this.version = version;
			}

			public final String getType() {
				return type;
			}

			public final void setType(String type) {
				this.type = type;
			}
		}
	}

	public static abstract class PushFilter {

		public abstract Path getSource();

		public abstract boolean accept(Path path);
	}

}