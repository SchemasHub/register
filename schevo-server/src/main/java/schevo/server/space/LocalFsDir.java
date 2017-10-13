package schevo.server.space;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;

import schevo.common.FileWalker;
import schevo.server.SpaceException;

/**
 * Dir in local FS
 * 
 * @author tomecode.com
 *
 */
public abstract class LocalFsDir {

	/**
	 * directory name
	 */
	protected String name;
	/**
	 * real path to dir
	 */
	@JsonIgnore
	protected Path fsPath;
	// /**
	// * real path to metadata dir
	// */
	// @JsonIgnore
	// protected Path fsPathMeta;

	public LocalFsDir(Path fsPathParent, String name) throws SpaceException {
		// this.fsPathMeta =
		// fsPathParent.resolve(name).resolve(Config.INTERNAL_META_DATA_DIR);
		this.fsPath = fsPathParent.resolve(name);
		this.name = name;
		try {
			FileWalker.mkDirs(this.fsPath);
		} catch (IOException e) {
			throw new SpaceException("Failed to mkdir: " + this.fsPath + " reason: " + e.getMessage(), e);
		}
	}

	public final String getName() {
		return name;
	}

	@JsonIgnore
	public final Path getFsPath() {
		return fsPath;
	}

}
