package schevo.common;

public class SpaceRef {

	private String workspace;

	private String repository;

	private String version;

	private String path;

	public SpaceRef(String workspace, String repository, String version) {
		this.workspace = workspace;
		this.repository = repository;
		this.version = version;

		this.path = workspace + "/" + repository + "/" + version;
	}

	public final String getPath() {
		return this.path;
	}

	public final String getWorkspace() {
		return workspace;
	}

	public final void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public final String getRepository() {
		return repository;
	}

	public final void setRepository(String repository) {
		this.repository = repository;
	}

	public final String getVersion() {
		return version;
	}

	public final void setVersion(String version) {
		this.version = version;
	}

	public final String toString() {
		return this.path;
	}
}
