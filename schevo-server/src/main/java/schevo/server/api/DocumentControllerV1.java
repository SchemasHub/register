package schevo.server.api;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import schevo.UriConfigs;
import schevo.common.Pair;
import schevo.common.Utils;
import schevo.server.space.FetchStream;
import schevo.server.space.PushStatus;
import schevo.server.space.Repository;
import schevo.server.space.RepositoryVersion;
import schevo.server.space.SpaceName;
import schevo.server.space.SpacesFsLocal;
import schevo.server.space.Workspace;

/**
 * 
 * Push(Upload)/Fetch(Download) schema(s)/document(s) from particular space
 * 
 * 
 * @author Tome (tomecode.com)
 *
 */

@RestController
public class DocumentControllerV1 {

	private static final Logger log = Logger.getLogger(SpacesControllerV1.class);

	private static final SpacesFsLocal spacesFs = SpacesFsLocal.get();

	/**
	 * upload documents to repository
	 * 
	 * @param spaceRef
	 * @param pushFiles
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = UriConfigs.PUSH_URI, consumes = { "multipart/mixed", "multipart/form-data" }, method = RequestMethod.POST)
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
	@GetMapping(value = UriConfigs.FETCH_URI)
	public final ResponseEntity<?> fetchDocuments(@RequestParam("spaceRef") String spaceRef, @RequestParam(name = "type", required = false, defaultValue = "zip") String type, HttpServletResponse response) {
		// resolve space ref
		log.info("GET /fetch - @spaceRef=" + spaceRef);

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
			response.addHeader(UriConfigs.HEADER_FETCH_NAME, ds.getFileName());

			log.info("GET /fetch - return: file " + ds.getFileName() + " length:" + ds.getContentLength());

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
				log.error("GET /fetch - (OutputStream) Failed to prepare download, reason: " + e.getMessage(), e);
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			log.error("GET /fetch - (InputStream) Failed to prepare download, reason: " + e.getMessage(), e);
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
