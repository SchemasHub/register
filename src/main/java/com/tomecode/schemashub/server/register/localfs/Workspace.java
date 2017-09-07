package com.tomecode.schemashub.server.register.localfs;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tomecode.schemashub.server.register.SpaceException;
import com.tomecode.schemashub.server.register.SpaceException.RepositoryExistsException;

/**
 * Workspace
 * 
 * @author tomecode.com
 *
 */
public final class Workspace extends LocalFsDir {

	@JsonIgnore
	private Hashtable<String, Repository> repositories;

	public Workspace(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);
		this.repositories = new Hashtable<>();
	}

//	@JsonIgnore
//	public final Hashtable<String, Repository> getRepositories() {
//		return repositories;
//	}

	@JsonProperty("repositories")
	public final Collection<String> getReposiotieNames() {
		return this.repositories.keySet();
	}

	/**
	 * create new {@link Repository}
	 * 
	 * @param name
	 * @return
	 * @throws SpaceException
	 */
	public final Repository newRepository(String name) throws SpaceException {
		name = SpaceName.toName(name);
		if (name == null) {
			throw new SpaceException("Repository name can't be null or emtpy!");
		}
		Repository repository = this.repositories.get(name);
		if (repository == null) {
			repository = new Repository(this.fsPath, name);
			this.repositories.put(name, repository);
		} else {
			throw new RepositoryExistsException("Repository: " + name + " already exists!");
		}

		return repository;

	}

	/**
	 * get {@link Repository} by name
	 * 
	 * @param repositoryName
	 * @return
	 */
	public final Repository getRepository(String repositoryName) {
		repositoryName = SpaceName.toName(repositoryName);
		if (repositoryName != null) {
			return this.repositories.get(repositoryName);
		}
		return null;
	}

}
