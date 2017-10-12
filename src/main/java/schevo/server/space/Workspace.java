package schevo.server.space;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import schevo.server.SpaceException;
import schevo.server.SpaceException.RepositoryExistsException;

/**
 * Workspace
 * 
 * @author tomecode.com
 *
 */
public final class Workspace extends LocalFsDir {

	private static final Logger log = Logger.getLogger(Workspace.class);

	@JsonIgnore
	private Hashtable<String, Repository> repositories;

	public Workspace(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);
		this.repositories = new Hashtable<>();
		this.initRepositories();
	}

	/**
	 * init repositories
	 */
	private final void initRepositories() {
		log.info("Init repositories from workspace: " + name);

		try {
			for (Path dir : Files.newDirectoryStream(this.fsPath)) {
				// if (!Config.INTERNAL_META_DATA_DIR.equals(dir.getFileName().toString())) {
				initRepository(dir);
				// }
			}
		} catch (IOException e) {
			log.error("Failed to init the directories from workspace, reason: " + e.getMessage(), e);
		}
	}

	/**
	 * init repository
	 * 
	 * @param dir
	 */
	private final void initRepository(Path dir) {
		log.info("Load repository: " + dir + " in workspace: " + name);

		String on = dir.getFileName().toString();
		try {
			Repository repository = new Repository(this.fsPath, on);
			this.repositories.put(on, repository);
		} catch (Exception e) {
			log.error("Failed to init repository: " + dir + " in workspace: " + name + " ,reason: " + e.getMessage(), e);
		}
	}

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
