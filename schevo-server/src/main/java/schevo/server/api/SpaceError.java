package schevo.server.api;

/**
 * general error
 * 
 * @author Tome (tomecode.com)
 *
 */
public class SpaceError {

	private long time;
	private String name;
	private String message;

	public SpaceError() {

	}

	public SpaceError(String name, String message) {
		this(System.currentTimeMillis(), name, message);
	}

	public SpaceError(long time, String name, String message) {
		this.time = time;
		this.name = name;
		this.message = message;
	}

	public final long getTime() {
		return time;
	}

	public final void setTime(long time) {
		this.time = time;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getMessage() {
		return message;
	}

	public final void setMessage(String message) {
		this.message = message;
	}

	//
	// ================================================================
	//

	public static final SpaceError workspaceExists(String name) {
		return new SpaceError("ERROR-1", "Workspace: " + name + " already: exists!");
	}

	public static final SpaceError workspaceNotFound(String name) {
		return new SpaceError("ERROR-2", "Workspace: " + name + " not found!");
	}

	public static final SpaceError repositryExists(String workspaceName, String repositoryName) {
		return new SpaceError("ERROR-3", "Repositry: " + repositoryName + " already exists in workspace: " + workspaceName);
	}

	public static final SpaceError repositryNotFound(String workspaceName, String repositoryName) {
		return new SpaceError("ERROR-4", "Repositry: " + repositoryName + " not found inworkspace: " + workspaceName);
	}

	public static final SpaceError repositryVersionExists(String workspaceName, String repositoryName, String repositoryVersionName) {
		return new SpaceError("ERROR-5", "Repositry: " + repositoryName + " version: " + repositoryVersionName + " already exists in workspace: " + workspaceName);
	}

	public static final SpaceError repositryVersionNotFound(String workspaceName, String repositoryName, String repositoryVersionName) {
		return new SpaceError("ERROR-6", "Repositry: " + repositoryName + " version: " + repositoryVersionName + " not found inworkspace: " + workspaceName);
	}

	public static final SpaceError spaceRefNotFound(String spaceRef) {
		return new SpaceError("ERROR-7", "SpaceRef: " + spaceRef + " not exists.");
	}

	public static final SpaceError pushFileNameEmpty(String spaceRef) {
		return new SpaceError("ERROR-8", "SpaceRef: " + spaceRef + " pushed file name can't be empty.");
	}
	
	
	public static final SpaceError pushFileListEmpty() {
		return new SpaceError("ERROR-9", "List of files can't be empty");
	}
	
}
