package schevo.server.api.v1;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import schevo.server.SpaceException;
import schevo.server.common.Pair;
import schevo.server.common.Utils;
import schevo.server.space.FetchStream;
import schevo.server.space.PushStatus;
import schevo.server.space.Repository;
import schevo.server.space.RepositoryVersion;
import schevo.server.space.SpaceName;
import schevo.server.space.SpacesFsLocal;
import schevo.server.space.Workspace;

/**
 * Main rest controller
 * 
 * @author tomecode.com
 *
 */
@RestController
@RequestMapping("/spaces/v1")
public class RegisterController {

	private static final Logger log = Logger.getLogger(RegisterController.class);

	private static final SpacesFsLocal spacesFs = SpacesFsLocal.get();

	@ResponseBody
	@GetMapping("")
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
	@GetMapping("/{workspaceName}")
	public final ResponseEntity<?> getWorkpace(@PathVariable("workspaceName") String workspaceName) {
		log.info("GET /spaces/{workspaceName} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /spaces/{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(workspace, HttpStatus.OK);
	}

	/**
	 * get info about repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 */
	@ResponseBody
	@GetMapping("/{workspaceName}/{repositoryName}")
	public final ResponseEntity<?> getRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("GET /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /spaces/{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		// get repository
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			log.error("GET /spaces/{workspaceName}/{repositoryName} - @repository=" + repositoryName + " not found in workspace");
			return new ResponseEntity<>(SpaceError.repositryNotFound(workspaceName, repositoryName), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(repository, HttpStatus.OK);
	}

	/**
	 * create new workspace
	 * 
	 * @param workspaceName
	 * @return
	 */
	@ResponseBody
	@PostMapping("/n/{workspaceName}")
	public final ResponseEntity<?> newWorkspace(@PathVariable("workspaceName") String workspaceName) {
		log.info("POST /spaces/{workspaceName} - @workspaceName=" + workspaceName);
		try {
			Workspace workspace = spacesFs.newWorkspace(workspaceName);
			log.info("POST /spaces/{workspaceName} - @workpsace=" + workspaceName + " created!");
			return new ResponseEntity<>(workspace, HttpStatus.OK);

		} catch (SpaceException e) {
			log.error("POST /spaces/{workspaceName} - @workpsace=" + workspaceName + " failed to create, reason: " + e.getMessage(), e);
			return new ResponseEntity<SpaceError>(SpaceError.workspaceExists(workspaceName), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * create new repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 */
	@ResponseBody
	@PostMapping("/{workspaceName}/{repositoryName}")
	public final ResponseEntity<?> newRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName);

		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("POST /spaces/{workspaceName}/{repositoryName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(SpaceError.workspaceNotFound(workspaceName), HttpStatus.NOT_FOUND);
		}

		try {
			// create new repository
			Repository repository = workspace.newRepository(repositoryName);
			log.info("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " created!");
			return new ResponseEntity<>(repository, HttpStatus.OK);
		} catch (SpaceException e) {
			log.error("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " failed to create: reason: " + e.getMessage(), e);
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
	@PostMapping("/{workspaceName}/{repositoryName}/{repositoryVersionName}")
	public final ResponseEntity<?> newRepositoryVersion(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName, @PathVariable("repositoryVersionName") String repositoryVersionName) {
		log.info("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName);

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
			log.info("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " created!");
			return new ResponseEntity<>(repositoryVersion, HttpStatus.OK);
		} catch (SpaceException e) {
			log.error("POST /spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " failed to create: reason: " + e.getMessage(), e);
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
	@GetMapping("/{workspaceName}/{repositoryName}/{repositoryVersionName}")
	public final ResponseEntity<?> getRepositoryVersion(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName, @PathVariable("repositoryVersionName") String repositoryVersionName) {
		log.info("GET /spaces/{workspaceName}/{repositoryName}/{repositoryVersionName} - @workspaceName=" + workspaceName);

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

	/**
	 * upload documents to repository
	 * 
	 * @param spaceRef
	 * @param pushFiles
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/pushDocuments", method = RequestMethod.POST)
	public final ResponseEntity<?> pushDocuments(//
			@RequestParam("spaceRef") String spaceRef, //
			@RequestPart(value = "file", required = true) MultipartFile[] pushFiles //
	// @RequestPart(value = "byPlan", required = false) String byPlan //
	// @RequestPart(value = "toDir", required = false) String toDir//
	) {
		//
		if (pushFiles == null || pushFiles.length == 0) {
			log.error("POST /pushDocuments - @file=null");
			return new ResponseEntity<>(SpaceError.pushFileListEmpty(), HttpStatus.BAD_REQUEST);
		}

		// validate file names
		for (MultipartFile uploadFile : pushFiles) {
			if (Utils.strOrNull(uploadFile.getOriginalFilename()) == null) {
				return new ResponseEntity<>(SpaceError.pushFileNameEmpty(spaceRef), HttpStatus.BAD_REQUEST);
			}
		}
		// resolve target version
		Pair<RepositoryVersion, ResponseEntity<String>> targetVersion = resolveSpaceByRef(spaceRef);
		if (targetVersion.getKey() == null) {
			return new ResponseEntity<>(SpaceError.spaceRefNotFound(spaceRef), HttpStatus.BAD_REQUEST);
		}

		try {
			log.info("POST /pushDocuments - @spaceRef=" + spaceRef + "Upload documents: @file=" + pushFiles.length);
			PushStatus status = targetVersion.getKey().pushDocuments(pushFiles);
			return new ResponseEntity<>(status, HttpStatus.OK);
		} catch (Exception e) {
			log.error("POST /pushDocuments - @spaceRef=" + spaceRef + " failed, reason: " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * fetch documents form particular space
	 * 
	 * @param spaceRef
	 * @param pkg
	 * @param response
	 * @return
	 */
	@ResponseBody
	@GetMapping("/fetchDocuments")
	public final ResponseEntity<?> fetchDocuments(@RequestParam("spaceRef") String spaceRef, @RequestParam(name = "type", required = false, defaultValue = "zip") String type, HttpServletResponse response) {
		// resolve space ref
		log.info("GET /fetchDocument - @spaceRef=" + spaceRef);

		Pair<RepositoryVersion, ResponseEntity<String>> resolve = resolveSpaceByRef(spaceRef);
		if (resolve.getKey() == null) {
			return resolve.getValue();
		}
		// DownloadPackageType dt = DownloadPackageType.ZIP;
		// TODO: type?;

		// get file for download and send it to client
		try (FetchStream ds = resolve.getKey().fetchDocuments()) {
			response.addHeader("Content-disposition", "attachment;filename=" + ds.getFileName());
			response.setContentType(ds.getContentType());
			response.setContentLengthLong(ds.getContentLength());

			log.info("GET /fetchDocument - return: file " + ds.getFileName() + " length:" + ds.getContentLength());

			// InputStreamResource inputStreamResource = new
			// InputStreamResource(ds.getIs());
			// HttpHeaders httpHeaders = new HttpHeaders();
			// httpHeaders.set("Content-Disposition", "attachment;filename=" +
			// ds.getFileName());
			// httpHeaders.set("Content-Type", ds.getContentType());
			// httpHeaders.setContentLength(ds.getContentLength());

			// return the file to client
			// return new ResponseEntity<>(inputStreamResource, httpHeaders, HttpStatus.OK);

			// get output stream
			try (OutputStream os = response.getOutputStream()) {
				// copy data to output
				IOUtils.copy(ds.getIs(), os);
				os.flush();
				response.flushBuffer();
			} catch (Exception e) {
				log.error("GET /fetchDocument - (OutputStream) Failed to prepare download, reason: " + e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("GET /fetchDocument - (InputStream) Failed to prepare download, reason: " + e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		// return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * resolve space ref
	 * 
	 * @param spaceRef
	 * @return
	 */
	private final Pair<RepositoryVersion, ResponseEntity<String>> resolveSpaceByRef(String spaceRef) {

		String[] ref = SpaceName.splitRef(spaceRef);
		if (ref.length != 3) {
			log.error("/resolveSpaceRef - @spaceRef=" + spaceRef);
			return new Pair<RepositoryVersion, ResponseEntity<String>>(null, new ResponseEntity<String>(new String("SpaceRef is wrong"), HttpStatus.BAD_REQUEST));
		}
		// get workspace by name
		Workspace workspace = spacesFs.getWorkspace(ref[0]);
		if (workspace == null) {
			log.error("/resolveSpaceRef - @spaceRef=" + spaceRef + " not found workspace!");
			return new Pair<RepositoryVersion, ResponseEntity<String>>(null, new ResponseEntity<String>(new String("Not found workspace"), HttpStatus.NOT_FOUND));
		}
		// get repository from workspace
		Repository repository = workspace.getRepository(ref[1]);
		if (repository == null) {
			log.error("/resolveSpaceRef - @spaceRef=" + spaceRef + " not found repository!");
			return new Pair<RepositoryVersion, ResponseEntity<String>>(null, new ResponseEntity<String>(new String("Not found repository"), HttpStatus.NOT_FOUND));
		}

		// get version of repository
		RepositoryVersion repositoryVersion = repository.getVersions().get(ref[2]);
		if (repositoryVersion == null) {
			log.error("/resolveSpaceRef - @spaceRef=" + spaceRef + " not found repository version!");
			return new Pair<RepositoryVersion, ResponseEntity<String>>(null, new ResponseEntity<String>(new String("Not found verions of repository"), HttpStatus.NOT_FOUND));
		}

		return new Pair<RepositoryVersion, ResponseEntity<String>>(repositoryVersion, null);
	}

}
