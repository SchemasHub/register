package com.tomecode.schemashub.server.common;

import java.nio.file.Path;

/**
 * 
 * @author tomecode.com
 *
 */
public class BasicFile {

	private Path path;
	private Path fsPath;

	public BasicFile(Path realPath, Path path) {
		this.fsPath = realPath;
		this.path = path;
	}

	public final Path getPath() {
		return path;
	}

	public final void setPath(Path revitalizedPath) {
		this.path = revitalizedPath;
	}

	public final Path getFsPath() {
		return fsPath;
	}

	public final void setRealPath(Path realPath) {
		this.fsPath = realPath;
	}

	@Override
	public final String toString() {
		return "BasicFile [fsPath: " + this.fsPath + "] [path: " + this.path + "]";
	}
}
