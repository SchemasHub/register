package schevo.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

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

		// this.httpClient = new OkHttpClient.Builder().connectTimeout(100,
		// TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180,
		// TimeUnit.SECONDS).build();

	}

	/**
	 * get(fetch) workspaces from schevo server
	 * 
	 * @return
	 * @throws SchevoClientException
	 */
	public final List<String> listWorkspaces() throws SchevoClientException {
		try {
			return toList(SchevoHttpClient.doGetJson(new URI(this.schevoUrl + UriConfigs.WORKSPACES_URI)).getJSONArray("workspaces"));
		} catch (JSONException | URISyntaxException | IOException e) {
			log.error("Failed to get list workspces, reason: " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get list workspces, reason: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * list of repositories in/from particular workspace
	 * 
	 * @param workspaceName
	 * @return
	 * @throws SchevoClientException
	 */
	public final List<String> listRepositories(String workspaceName) throws SchevoClientException {
		try {
			return toList(SchevoHttpClient.doGetJson(new URI(this.schevoUrl + UriConfigs.WORKSPACE_URI.replace("{workspaceName}", workspaceName))).getJSONArray("repositories"));
		} catch (JSONException | URISyntaxException | IOException e) {
			log.error("Failed to get list repositories from workspace!, reason:  " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get list repositories from workspace!, reason:  " + e.getMessage(), e);
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
		try {
			return toList(SchevoHttpClient.doGetJson(new URI(this.schevoUrl + UriConfigs.REPOSITORY_URI//
					.replace("{workspaceName}", workspaceName)//
					.replace("{repositoryName}", repositoryName)))//
					.getJSONArray("versions"));
		} catch (JSONException | URISyntaxException | IOException e) {
			log.error("Failed to get list repository versions!, reason:  " + e.getMessage(), e);
			throw new SchevoClientException("Failed to get list repository versions!, reason:  " + e.getMessage(), e);
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

}