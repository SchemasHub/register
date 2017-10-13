package schevo.server.space;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import schevo.common.Utils;
import schevo.server.SpaceException;
import schevo.server.SpaceException.RepositoryVersionExistsException;

/**
 * Repository
 * 
 * @author tomecode.com
 *
 */
public final class Repository extends LocalFsDir {

	private static final Logger log = Logger.getLogger(Repository.class);

	@JsonIgnore
	private Hashtable<String, RepositoryVersion> versions;

	public Repository(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);
		this.versions = new Hashtable<>();
		this.initVersions();
	}

	private final void initVersions() {
		log.info("Init versions from repository: " + name);

		try {
			for (Path dir : Files.newDirectoryStream(this.fsPath)) {
				// if (!Config.INTERNAL_META_DATA_DIR.equals(dir.getFileName().toString())) {
				initVersion(dir);
				// }
			}
		} catch (IOException e) {
			log.error("Failed to init the directories from workspace, reason: " + e.getMessage(), e);
		}
	}

	/**
	 * init repository
	 * 
	 * @param repositoryDir
	 */
	private final void initVersion(Path dir) {
		log.info("Init repository version: " + dir + " in workspace: " + name);

		String on = dir.getFileName().toString();
		try {

			RepositoryVersion repoVer = new RepositoryVersion(this.fsPath, on);
			this.versions.put(on, repoVer);
		} catch (Exception e) {
			log.error("Failed to init repository: " + dir + " in workspace: " + name + " ,reason: " + e.getMessage(), e);
		}
	}

	@JsonIgnore
	public final Hashtable<String, RepositoryVersion> getVersions() {
		return versions;
	}

	@JsonProperty("versions")
	public final Collection<String> getVersionNames() {
		return versions.keySet();
	}

	public final RepositoryVersion newVersion(String name) throws SpaceException {
		name = Utils.strOrNull(name);
		if (name == null) {
			throw new SpaceException("Version name can't be null or emtpy!");
		}

		String fixName = name.toLowerCase();
		RepositoryVersion repoVer = this.versions.get(fixName);
		if (repoVer == null) {
			repoVer = new RepositoryVersion(this.fsPath, name);
			this.versions.put(name, repoVer);
		} else {
			throw new RepositoryVersionExistsException("Repository version: " + name + " already exists!");
		}

		return repoVer;
	}

	public final RepositoryVersion getVersion(String versionName) {
		if (versionName != null) {
			return this.versions.get(versionName);
		}
		return null;
	}
}
