package schevo.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import schevo.UriConfigs;
import schevo.common.FileWalker;
import schevo.common.Utils;

/**
 * 
 * @author Tome (tomecode.com)
 *
 */
public final class SchevoClient {

	private static final Logger log = Logger.getLogger(SchevoClient.class);

	// private static final String URL_WORKSPACE = URL_BASE + "/{workspaceName}";
	// private static final String URL_REPOSITORY = URL_WORKSPACE +
	// "/{repositoryName}";
	// pr//ivate static final String URL_REPOSITORY_VERSION = URL_BASE +
	// "/{spaceRef}";
	/// private static final String URL_FETCHDOCUMENTS = "/documents/v1/fetch";

	// private static final MediaType MEDIATYPE_ZIP =
	// MediaType.parse("application/zip, application/octet-stream");

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
		String targetUri = this.schevoUrl + UriConfigs.WORKSPACES_URI;
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpJsonResponseHandler responseJson = new HttpJsonResponseHandler();
			httpclient.execute(new HttpGet(targetUri), responseJson);

			return toList(responseJson.getResponse().getJSONArray("workspaces"));
		} catch (IOException e1) {
			log.error("Failed to get list of all workspaces: " + targetUri + " ,reason: " + e1.getMessage(), e1);
			throw new SchevoClientException("Failed to get list of all workspaces: " + targetUri + " ,reason: " + e1.getMessage(), e1);
		}
	}

	/**
	 * 
	 * list of all repositories in/from particular workspace
	 * 
	 * @param workspaceName
	 * @return
	 * @throws SchevoClientException
	 */
	public final List<String> listRepositories(String workspaceName) throws SchevoClientException {
		String targetUri = this.schevoUrl + UriConfigs.WORKSPACE_URI.replace("{workspaceName}", workspaceName);
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpJsonResponseHandler responseJson = new HttpJsonResponseHandler();
			httpclient.execute(new HttpGet(targetUri), responseJson);

			return toList(responseJson.getResponse().getJSONArray("repositories"));
		} catch (IOException e1) {
			log.error("Failed to get list of all respositories: " + targetUri + " ,reason: " + e1.getMessage(), e1);
			throw new SchevoClientException("Failed to get list of all respositories: " + targetUri + " ,reason: " + e1.getMessage(), e1);
		}
	}

	/**
	 * list of repository versions in/from particular workspace
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 * @throws SchevoClientException
	 */
	public final List<String> listVersions(String workspaceName, String repositoryName) throws SchevoClientException {

		String targetUri = this.schevoUrl + UriConfigs.REPOSITORY_URI//
				.replace("{workspaceName}", workspaceName)//
				.replace("{repositoryName}", repositoryName);

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpJsonResponseHandler responseJson = new HttpJsonResponseHandler();
			httpclient.execute(new HttpGet(targetUri), responseJson);

			return toList(responseJson.getResponse().getJSONArray("versions"));
		} catch (IOException e1) {
			log.error("Failed to get list of all versions: " + targetUri + " ,reason: " + e1.getMessage(), e1);
			throw new SchevoClientException("Failed to get list of all versions: " + targetUri + " ,reason: " + e1.getMessage(), e1);
		}
	}

	private final List<String> toList(JSONArray jsonArray) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i <= jsonArray.length() - 1; i++) {
			list.add(jsonArray.getString(i));
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
	 * helper class for processing the server response
	 * 
	 * @author Tome (tomecode.com)
	 *
	 * @param <T>
	 */
	public static abstract class HttpGenericResponseHandler<T> implements ResponseHandler<Object> {

		/**
		 * exception from server
		 */
		private SchevoClientException exception;
		/**
		 * response object: normal/correct object
		 */
		protected T response;

		private HttpResponse originalResponse;

		public HttpGenericResponseHandler() {

		}

		public final HttpResponse getOriginalResponse() {
			return this.originalResponse;
		}

		@Override
		public Object handleResponse(HttpResponse httpPesponse) throws ClientProtocolException, IOException {
			this.originalResponse = httpPesponse;
			int code = httpPesponse.getStatusLine().getStatusCode();

			if (HttpStatus.SC_OK != code) {
				// if is error
				processError(httpPesponse, code);
			} else {
				handleResponseOK(httpPesponse);
			}
			return null;
		}

		/**
		 * handle response in case that is OK
		 * 
		 * @param httpResponse
		 * @throws ClientProtocolException
		 * @throws IOException
		 */
		protected abstract void handleResponseOK(HttpResponse httpResponse) throws ClientProtocolException, IOException;

		/**
		 * 
		 * parse exception from
		 * 
		 * @param response
		 * @param code
		 * @throws ClientProtocolException
		 * @throws IOException
		 */
		protected final void processError(HttpResponse response, int code) throws ClientProtocolException, IOException {
			if (HttpStatus.SC_BAD_REQUEST == code) {
				exception = new SchevoClientException(toJson(response.getEntity()));
			} else {
				exception = new SchevoClientException(toJson(response.getEntity()));
			}
		}

		/**
		 * parse response to JSON object
		 * 
		 * @param httpEntity
		 * @return
		 * @throws ClientProtocolException
		 * @throws IOException
		 */
		protected final JSONObject toJson(HttpEntity httpEntity) throws ClientProtocolException, IOException {
			try (InputStream is = httpEntity.getContent()) {
				return new JSONObject(new JSONTokener(httpEntity.getContent()));
			}

		}

		/**
		 * to return the answer if there is no exception, but if there is exception then
		 * the exception is throwed
		 * 
		 * @return
		 * @throws SchevoClientException
		 */
		public final T getResponse() throws SchevoClientException {
			if (exception != null) {
				throw exception;
			}
			return response;
		}

	}

	/**
	 *
	 * Helper class for processing the server response: JSON
	 * 
	 * @author Tome (tomecode.com)
	 *
	 */
	public static final class HttpJsonResponseHandler extends HttpGenericResponseHandler<JSONObject> {

		@Override
		protected final void handleResponseOK(HttpResponse httpResponse) throws ClientProtocolException, IOException {
			response = toJson(httpResponse.getEntity());
		}
	}

}