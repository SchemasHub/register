package com.tomecode.schevo.server.register.localfs;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tomecode.schevo.server.Config;
import com.tomecode.schevo.server.common.FileWalker;
import com.tomecode.schevo.server.register.SpaceException;

/**
 * 
 * @author tomecode.com
 *
 */
public abstract class LocalFsDir {

	protected String name;
	@JsonIgnore
	protected Path fsPath;
	@JsonIgnore
	protected Path fsPathMeta;

	public LocalFsDir(Path fsPathParent, String name) throws SpaceException {
		this.fsPathMeta = fsPathParent.resolve(name).resolve(Config.INTERNAL_META_DATA_DIR);
		this.fsPath = fsPathMeta.getParent();
		this.name = name;
		try {
			FileWalker.mkDirs(this.fsPathMeta);
		} catch (IOException e) {
			throw new SpaceException("Failed to mkdir: " + this.fsPathMeta + " reason: " + e.getMessage(), e);
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
