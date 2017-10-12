package schevo.server.space;

public class Document {
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