package schevo;

/**
 * URL(s)/API for SCHEVO
 * 
 * @author Tome (tomecode.com)
 *
 */
public final class UriConfigs {

	public static final String WORKSPACES_URI = "/spaces";

	public static final String API_DOCUMENTS = "/documents";

	/**
	 * Push document(s) to SCHEVO
	 * 
	 * POST /push?{spaceRef}
	 */
	public static final String PUSH_URI = API_DOCUMENTS + "/push";

	/**
	 * 
	 * Fetch(download) document(s) from SCHEVO
	 * 
	 * GET /fetch?(spaceRef}
	 */
	public static final String FETCH_URI = API_DOCUMENTS + "/fetch";

	/**
	 * (POST|GET) /{workspaceName}
	 */
	public static final String WORKSPACE_URI = WORKSPACES_URI + "/{workspaceName}";
	/**
	 * (POST|GET) /{workspaceName}/{repositoryName}
	 */
	public static final String REPOSITORY_URI = WORKSPACE_URI + "/{repositoryName}";
	/**
	 * (POST|GET) /{workspaceName}/{repositoryName}/{repositoryVersionName}
	 */
	public static final String REPOSITORY_VERSION_URI = REPOSITORY_URI + "/{repositoryVersionName}";

	/**
	 * query param {spaceRef}
	 */
	public static final String PARAM_SPACE_REF = "spaceRef";

	/**
	 * header: schevo-fetch-name
	 */
	public static final String HEADER_FETCH_NAME = "schevo-fetch-name";
}
