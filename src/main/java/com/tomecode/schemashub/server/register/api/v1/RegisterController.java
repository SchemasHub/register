package com.tomecode.schemashub.server.register.api.v1;

import java.io.OutputStream;
import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tomecode.schemashub.server.common.DownloadStream;
import com.tomecode.schemashub.server.common.FileWalker;
import com.tomecode.schemashub.server.common.Utils;
import com.tomecode.schemashub.server.register.SpaceException;
import com.tomecode.schemashub.server.register.localfs.Repository;
import com.tomecode.schemashub.server.register.localfs.RepositoryVersion;
import com.tomecode.schemashub.server.register.localfs.RepositoryVersion.DownloadPackageType;
import com.tomecode.schemashub.server.register.localfs.SpaceName;
import com.tomecode.schemashub.server.register.localfs.SpacesFsLocal;
import com.tomecode.schemashub.server.register.localfs.Workspace;

import javafx.util.Pair;

/**
 * 
 * @author tomecode.com
 *
 */
@RestController
@RequestMapping("/register/v1")
public class RegisterController {

	private static final Logger log = Logger.getLogger(RegisterController.class);

	private static final SpacesFsLocal spacesFs = SpacesFsLocal.get();

	/**
	 * get info about workspace
	 * 
	 * @param workspaceName
	 * @return
	 */
	@ResponseBody
	@GetMapping("/spaces/{workspaceName}")
	public final ResponseEntity<?> getWorkpace(@PathVariable("workspaceName") String workspaceName) {
		log.info("GET /spaces/{workspaceName} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /spaces/{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(new String("Workspace " + workspaceName + " not found"), HttpStatus.NOT_FOUND);
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
	@GetMapping("/spaces/{workspaceName}/{repository}")
	public final ResponseEntity<?> getRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("GET /spaces/{workspaceName}/{repository} - @workspaceName=" + workspaceName);
		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("GET /spaces/{workspaceName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(new String("Workspace " + workspaceName + " not found"), HttpStatus.NOT_FOUND);
		}

		// get repository
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			log.error("GET /spaces/{workspaceName}/{repository} - @repository=" + repositoryName + " not found in workspace");
			return new ResponseEntity<>(new String("Repository " + repositoryName + " not found"), HttpStatus.NOT_FOUND);
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
	@PostMapping("/spaces/{workspaceName}")
	public final ResponseEntity<?> newWorkspace(@PathVariable("workspaceName") String workspaceName) {
		log.info("POST /spaces/{workspaceName} - @workspaceName=" + workspaceName);
		try {
			spacesFs.newWorkspace(workspaceName);
			log.info("POST /spaces/{workspaceName} - @workpsace=" + workspaceName + " created!");
		} catch (SpaceException e) {
			log.error("POST /spaces/{workspaceName} - @workpsace=" + workspaceName + " failed to create, reason: " + e.getMessage(), e);
			return new ResponseEntity<>(new String(e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * create repository
	 * 
	 * @param workspaceName
	 * @param repositoryName
	 * @return
	 */
	@ResponseBody
	@PostMapping("/spaces/{workspaceName}/{repositoryName}")
	public final ResponseEntity<?> newRepository(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName) {
		log.info("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName);

		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			log.error("POST /spaces/{workspaceName}/{repositoryName} - @workpsace=" + workspaceName + " not found workspace");
			return new ResponseEntity<>(new String("Workspace " + workspaceName + " not found"), HttpStatus.NOT_FOUND);
		}

		try {
			// create new repository
			workspace.newRepository(repositoryName);
			log.info("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " created!");
		} catch (SpaceException e) {
			log.error("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " failed to create: reason: " + e.getMessage(), e);
			return new ResponseEntity<>(new String(e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
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
	@PostMapping("/spaces/{workspaceName}/{repositoryName}/{repositoryVersionName}")
	public final ResponseEntity<?> newRepositoryVersion(@PathVariable("workspaceName") String workspaceName, @PathVariable("repositoryName") String repositoryName, @PathVariable("repositoryVersionName") String repositoryVersionName) {
		log.info("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName);

		repositoryVersionName = Utils.strOrNull(repositoryVersionName);
		if (repositoryVersionName == null) {
			return new ResponseEntity<>(new String("Repository version name is empty"), HttpStatus.BAD_REQUEST);
		}

		Workspace workspace = spacesFs.getWorkspace(workspaceName);
		if (workspace == null) {
			return new ResponseEntity<>(new String("Workspace " + workspaceName + " not found"), HttpStatus.NOT_FOUND);
		}
		Repository repository = workspace.getRepository(repositoryName);
		if (repository == null) {
			return new ResponseEntity<>(new String("Respository " + repositoryName + " not found"), HttpStatus.NOT_FOUND);
		}

		try {
			// create new repository
			repository.newVersion(repositoryVersionName);
			log.info("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " created!");
		} catch (SpaceException e) {
			log.error("/spaces/{workspaceName}/{repositoryName} - @workspaceName=" + workspaceName + " @repositoryName=" + repositoryName + " @repositoryVersionName=" + repositoryVersionName + " failed to create: reason: " + e.getMessage(), e);
			return new ResponseEntity<>(new String(e.getMessage()), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * upload documennts to repository
	 * 
	 * @param spaceRef
	 * @param files
	 * @return
	 */
	@ResponseBody
	@PostMapping("/pushDocuments")
	public final ResponseEntity<?> pushDocuments(@RequestParam("spaceRef") String spaceRef, @RequestParam("file") MultipartFile[] files) {
		if (files == null || files.length == 0) {
			log.error("/pushDocuments - @file=null");
			return new ResponseEntity<>(new String("Empty files"), HttpStatus.BAD_REQUEST);
		}
		// validate file names
		for (MultipartFile uploadFile : files) {
			if (Utils.strOrNull(uploadFile.getOriginalFilename()) == null) {
				return new ResponseEntity<>(new String("Some files are without name!"), HttpStatus.BAD_REQUEST);
			}
		}
		Pair<RepositoryVersion, ResponseEntity<String>> resolve = resolveSpaceByRef(spaceRef);
		if (resolve.getKey() == null) {
			return resolve.getValue();
		}

		log.info("/pushDocuments - @spaceRef=" + spaceRef + "Upload documents: @file=" + files.length);

		//
		// copy files to workspace/repository/version/*
		//
		Path path = null;
		for (MultipartFile uf : files) {
			try {
				path = resolve.getKey().getFsContent().resolve(uf.getOriginalFilename());
				log.info("/pushDocuments - @spaceRef=" + spaceRef + " push: " + uf.getOriginalFilename() + " to: " + path);
				// create dirs (if not exists)
				FileWalker.mkFile(path);
				// transform to file
				uf.transferTo(path.toFile());
			} catch (Exception e) {
				log.error("/pushDocuments - @spaceRef=" + spaceRef + " failed to copy from: " + uf.getOriginalFilename() + " file=" + path + " reason: " + e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}

		return new ResponseEntity<>(HttpStatus.OK);
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
		log.info("/fetchDocument - @spaceRef=" + spaceRef);

		Pair<RepositoryVersion, ResponseEntity<String>> resolve = resolveSpaceByRef(spaceRef);
		if (resolve.getKey() == null) {
			return resolve.getValue();
		}
		DownloadPackageType dt = DownloadPackageType.ZIP;
		// TODO: type?;

		// get file for download and send it to client
		try (DownloadStream ds = resolve.getKey().prepareDownloadPks(dt)) {
			response.addHeader("Content-disposition", "attachment;filename=" + ds.getFileName());
			response.setContentType(ds.getContentType());
			response.setContentLengthLong(ds.getContentLength());

			log.info("/fetchDocument - return: file " + ds.getFileName() + " length:" + ds.getContentLength());

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
				log.error("/fetchDocument - (OutputStream) Failed to prepare download, reason: " + e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("/fetchDocument - (InputStream) Failed to prepare download, reason: " + e.getMessage(), e);
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
