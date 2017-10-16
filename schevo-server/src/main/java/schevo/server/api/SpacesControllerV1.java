package schevo.server.api;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import schevo.UriConfigs;
import schevo.common.Utils;
import schevo.server.SpaceException;
import schevo.server.space.Repository;
import schevo.server.space.RepositoryVersion;
import schevo.server.space.SpacesFsLocal;
import schevo.server.space.Workspace;

/**
 * Rest controller for managed spaces i.e. create or get workspace, repository,
 * version of repository
 * 
 * @author tomecode.com
 *
 */
@RestController
public class SpacesControllerV1 {

	private static final Logger log = Logger.getLogger(SpacesControllerV1.class);

	private static final SpacesFsLocal spacesFs = SpacesFsLocal.get();

	@ResponseBody
	@GetMapping(UriConfigs.WORKSPACES_URI)
	public final ResponseEntity<?> getWorkspaces() {
		return new ResponseEntity<>(new WorkspacesDto(spacesFs.getWorkspaceNames()), HttpStatus.OK);
	}

	/**
	 * get info about workspace
	 * 
	 * @param workspaceName
	 * @return
	 */
	@ResponseBody
	@GetMapping(UriConfigs.WORKSPACE_URI)
	public final ResponseEntity<?> getWorkpace(@PathVariable("workspaceName") String workspaceName) {
		log.info("GET /{workspaceName} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(workspace, HttpStatus.OK);
	}

	/**
	 * create new workspace
	 * 
	 * @param workspaceName
	 * @return
	 */
	@ResponseBody
	@PostMapping(UriConfigs.WORKSPACE_URI)
	public final ResponseEntity<?> newWorkspace(@PathVariable("workspaceName") String workspaceName) {
		log.info("POST /{workspaceName} - @workspaceName=" + workspaceName);
		try {
			Workspace workspace = spacesFs.newWorkspace(workspaceName);
			log.info("POST /{workspaceName} - @workpsace=" + workspaceName + " created!");
			return new ResponseEntity<>(workspace, HttpStatus.OK);

		} catch (SpaceException e) {
			log.error("POST /{workspaceName} - @workpsace=" + workspaceName + " failed to create, reason: " + e.getMessage(), e);
			return new ResponseEntity<SpaceError>(SpaceError.workspaceExists(workspaceName), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * get info about repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 */
	@ResponseBody
	@GetMapping(UriConfigs.REPOSITORY_URI)
	public final ResponseEntity<?> getRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("GET /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		// get repository
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			log.error("GET /{workspaceName}/{repositoryName} - @repository=" + repositoryName + " not found in workspace");
			return new ResponseEntity<>(SpaceError.repositryNotFound(workspaceName, repositoryName), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(repository, HttpStatus.OK);
	}

	/**
	 * create new repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 */
	@ResponseBody
	@PostMapping(UriConfigs.REPOSITORY_URI)
	public final ResponseEntity<?> newRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName);

		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("POST /{workspaceName}/{repositoryName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		try {
			// create new repository
			Repository repository = workspace.newRepository(repositoryName);
			log.info("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " created!");
			return new ResponseEntity<>(repository, HttpStatus.OK);
		} catch (SpaceException e) {
			log.error("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " failed to create: reason: " + e.getMessage(), e);
			return new ResponseEntity<>(SpaceError.repositryExists(workspaceName, repositoryName), HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * create new version of repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @param repositoryVersionName
	 * @return
	 */
	@ResponseBody
	@PostMapping(UriConfigs.REPOSITORY_VERSION_URI)
	public final ResponseEntity<?> newRepositoryVersion(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName, @PathVariable("repositoryVersionName") String repositoryVersionName) {
		log.info("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName);

		repositoryVersionName = Utils.strOrNull(repositoryVersionName);
		if (repositoryVersionName == null) {
			return new ResponseEntity<>(new String("Repository version name is empty"), HttpStatus.BAD_REQUEST);
		}

		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			return new ResponseEntity<>(SpaceError.repositryNotFound(workspaceName, repositoryName), HttpStatus.NOT_FOUND);
		}

		try {
			// create new version of repository
			RepositoryVersion repositoryVersion = repository.newVersion(repositoryVersionName);
			log.info("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " created!");
			return new ResponseEntity<>(repositoryVersion, HttpStatus.OK);
		} catch (SpaceException e) {
			log.error("POST /{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " failed to create: reason: " + e.getMessage(), e);
			return new ResponseEntity<>(SpaceError.repositryVersionExists(workspaceName, repositoryName, repositoryVersionName), HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * get details about repository version
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @param repositoryVersionName
	 * @return
	 */
	@ResponseBody
	@GetMapping(UriConfigs.REPOSITORY_VERSION_URI)
	public final ResponseEntity<?> getRepositoryVersion(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName, @PathVariable("repositoryVersionName") String repositoryVersionName) {
		log.info("GET /{workspaceName}/{repositoryName}/{repositoryVersionName} - @workspaceName=" + workspaceName);

		// find workspace
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		// find repository
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			return new ResponseEntity<>(SpaceError.repositryNotFound(workspaceName, repositoryName), HttpStatus.NOT_FOUND);
		}

		// find version of repository
		RepositoryVersion repositoryVersion = repository.getVersion(repositoryVersionName);
		if (repositoryVersion == null) {
			return new ResponseEntity<>(SpaceError.repositryVersionNotFound(workspaceName, repositoryName, repositoryVersionName), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(repositoryVersion, HttpStatus.OK);
	}

}
