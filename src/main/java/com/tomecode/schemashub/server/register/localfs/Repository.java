package com.tomecode.schemashub.server.register.localfs;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tomecode.schemashub.server.common.Utils;
import com.tomecode.schemashub.server.register.SpaceException;
import com.tomecode.schemashub.server.register.SpaceException.RepositoryVersionExistsException;

/**
 * Repository
 * 
 * @author tomecode.com
 *
 */
public final class Repository extends LocalFsDir {

	@JsonIgnore
	private Hashtable<String, RepositoryVersion> versions;

	public Repository(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);
		this.versions = new Hashtable<>();
	}

	@JsonIgnore
	public final Hashtable<String, RepositoryVersion> getVersions() {
		return versions;
	}

	@JsonProperty("version")
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
}
