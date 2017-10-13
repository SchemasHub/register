package schevo.server.space;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import schevo.common.BasicFile;
import schevo.common.FileWalker;
import schevo.server.Config;
import schevo.server.SpaceException;
import schevo.server.space.PushStatus.PushFile;
import schevo.server.space.PushStatus.PushPackageFile;

/**
 * Version of repository
 * 
 * @author tomecode.com
 *
 */
public class RepositoryVersion extends LocalFsDir {
	// 1. init (startup|create new)
	// 1.2 init - nacitanie cache
	// 2. upload/push document (subor alebo balicek)
	// 2.1 lock - update content.log -> buffered reader
	// 2.2 update file infos (hash,path,date,type)
	//
	// ...
	// 3. upload/push document (subor alebo balicek)
	// 3.1 document (extract balicek)
	// 3.2 document = new hash
	// 3.3 document.exists = yes ->
	//
	//
	// push - retend lock (write to FS and update cache) - StampedLock

	// https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html
	//

	// @cache|model
	// 01/change
	// ....content/01/api/client.xsd
	// ....content/01/api/contact.xsd
	// ....content/01/serice/update.wsdl
	//
	// 02/change
	// ....content/01/api/client.xsd
	// ....content/02/api/contact.xsd

	//
	//
	//
	//
	//
	//
	////
	//
	//
	//
	//
	//
	//

	private static final Logger log = Logger.getLogger(RepositoryVersion.class);

	private static final String DOCUMENT_DEFUALT_VERSION = "1.0.0";

	private static final String DIR_CONENT = "content";
	private static final String DIR_DOWNLOAD = "download";
	private static final String FILE_CONTENT = "content.log";

	private final StampedLock sl = new StampedLock();

	/**
	 * path to content dir
	 */
	@JsonIgnore
	private Path fsContentDir;
	/**
	 * path to download dir
	 */
	@JsonIgnore
	private Path fsDownloadDir;
	/**
	 * path to content log file
	 */
	@JsonIgnore
	private Path fsContentLogFile;

	@JsonIgnore
	private Path fsSchevoLogFile;

	/**
	 * documents in repository
	 */
	@JsonIgnore
	private final ConcurrentHashMap<String, Document> documents;

	/**
	 * create date
	 */
	private long createDate;

	/**
	 * update date
	 */
	private long updateDate;

	/**
	 * if is true then new download file should be generated
	 */
	@JsonIgnore
	private boolean genNewDownloadFile;

	/**
	 * 
	 * 
	 * 
	 * 
	 * @param fsPathParent
	 * @param name
	 *            of version
	 * @throws SpaceException
	 */
	public RepositoryVersion(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);
		this.documents = new ConcurrentHashMap<>();
		try {
			this.fsContentDir = fsPath.resolve(DIR_CONENT);
			log.info("init: mkdir: " + fsContentDir);
			this.fsContentDir = FileWalker.mkDirs(fsContentDir);
		} catch (IOException e) {
			log.error("Failed to mkdir: " + fsContentDir + ", reason: " + e.getMessage(), e);
			throw new SpaceException("Failed to mkdir: " + fsContentDir + " reason: " + e.getMessage(), e);
		}
		try {
			this.fsDownloadDir = fsPath.resolve(DIR_DOWNLOAD);
			log.info("init: mkdir: " + fsDownloadDir);
			this.fsDownloadDir = FileWalker.mkDirs(fsDownloadDir);
		} catch (IOException e) {
			log.error("Failed to mkdir: " + fsDownloadDir + ", reason: " + e.getMessage(), e);
			throw new SpaceException("Failed to mkdir: " + fsDownloadDir + "reason: " + e.getMessage(), e);
		}
		try {
			this.fsContentLogFile = fsPath.resolve(FILE_CONTENT);
			log.info("init: mkfile: " + fsContentLogFile);
			this.fsContentLogFile = FileWalker.mkFile(fsContentLogFile);
		} catch (IOException e) {
			log.error("Failed to mkfile: " + fsContentLogFile + ", reason: " + e.getMessage(), e);
			throw new SpaceException("Failed to mkfile: " + fsContentLogFile + ", reason: " + e.getMessage(), e);
		}

		this.fsSchevoLogFile = fsPath.resolve("schevo.log");

		createDate = System.currentTimeMillis();
		updateDate = System.currentTimeMillis();
		init();
	}

	/**
	 * initialize the content log
	 */
	private final void init() {
		// read content
		long rStamp = sl.readLock();
		try {
			log.info("init: Read content long");
			readContentLog();
			readSchevoLog();
		} finally {
			sl.unlockRead(rStamp);
		}
	}

	@JsonIgnore
	public final Path getFsDownload() {
		return this.fsDownloadDir;
	}

	@JsonIgnore
	public final Path getFsContent() {
		return this.fsContentDir;
	}

	public final long getCreateDate() {
		return createDate;
	}

	public final long getUpdateDate() {
		return updateDate;
	}

	/**
	 * 
	 * push document(s) to (space://workspace/repository/version)
	 * 
	 * 
	 * @param multipartFiles
	 * @return
	 * @throws IOException
	 */
	public final PushStatus pushDocuments(MultipartFile[] multipartFiles) throws IOException {
		PushStatus pushStatus = new PushStatus();
		// push time...
		pushStatus.setWhen(System.currentTimeMillis());

		long wStamp = sl.writeLock();
		try {
			// iterate over docs
			for (MultipartFile mf : multipartFiles) {
				//
				log.info("Push mulipartfile: " + mf.getName() + " originalFile: " + mf.getOriginalFilename());

				try (InputStream is = mf.getInputStream()) {
					pushDocumentFile(mf.getOriginalFilename(), is, pushStatus);
				} catch (Exception e) {
					log.error("push failed - mulipartfile: " + mf.getName() + " originalFile: " + mf.getOriginalFilename() + ", reason: " + e.getMessage(), e);
				}
			}

			// update content log about changes (new or updated docs)
			writeContentLog(pushStatus);

		} finally {
			// unlock
			sl.unlockWrite(wStamp);
			// update me
			updateMe();
			// track how long time ....
			pushStatus.setPushTime(System.currentTimeMillis() - pushStatus.getWhen());
		}

		return pushStatus;

	}

	/**
	 * push files to content dir , if file is zip then it will be extracted
	 * 
	 * @param newTargetFileName
	 * @param is
	 * @throws Exception
	 */
	private final void pushDocumentFile(String newTargetFileName, InputStream is, PushStatus pushStatus) throws Exception {

		// get file extension
		String fileExt = FileWalker.getExtension(newTargetFileName);
		if (".zip".equals(fileExt)) {
			// if packaged file(zip,...) extract it and push docs ...
			pushExtractFilesFromZip(is, pushStatus.newPkg(newTargetFileName));
		} else {
			// single file
			pushFileToContentDir(newTargetFileName, is, pushStatus.singleFile(newTargetFileName));
		}
	}

	/**
	 * the list of documents
	 * 
	 * @return
	 */
	@JsonGetter
	public final Collection<Document> getDocuments() {
		long rStamp = sl.readLock();
		try {
			return this.documents.values();
		} finally {
			sl.unlockRead(rStamp);
		}
	}

	/**
	 * extract files from zip file and push to content dir
	 * 
	 * @param inputStream
	 * @param pushPkgStatus
	 * @throws IOException
	 */
	private final void pushExtractFilesFromZip(InputStream inputStream, PushPackageFile pushPkgStatus) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				// extract entity
				if (!ze.isDirectory()) {
					pushFileToContentDir(ze.getName(), zis, pushPkgStatus.singleFile(ze.getName()));
				}
			}
		}
	}

	/**
	 * push file to {@link #getFsContent()}
	 * 
	 * @param newTargetFileName
	 * @param is
	 * @param pushedDocs
	 */
	private final void pushFileToContentDir(String newTargetFileName, InputStream is, PushFile pushedDocs) {
		pushedDocs.setTime(System.currentTimeMillis());
		pushedDocs.setType(FileWalker.getExtension(newTargetFileName));

		try {
			log.info("RepositoryVersion:  " + name + " push file: " + newTargetFileName);
			// resolve the real path
			Path targetFile = fsContentDir.resolve(newTargetFileName);
			// create dir|file (if not exists)
			FileWalker.mkFile(targetFile);
			// copy stream
			Files.copy(is, targetFile, StandardCopyOption.REPLACE_EXISTING);

			pushedDocs.setHash(FileWalker.fileHash(targetFile));
			// TODO-default version
			pushedDocs.setVersion(DOCUMENT_DEFUALT_VERSION);

			pushedDocs.setPushed(true);
		} catch (Exception e) {
			log.error("Failed to push file: " + newTargetFileName + " reason: " + e.getMessage(), e);
			pushedDocs.setPushed(false);
		}
	}

	/**
	 * fetch/download (all) document(s) from repository
	 * 
	 * @return
	 */
	public final FetchStream fetchDocuments() throws IOException {
		String targetDownloadFileName = name + Config.FETCH_REPOSITORY_EXTENSION;
		Path targetDownloadFile = fsDownloadDir.resolve(targetDownloadFileName);

		long rStamp = sl.readLock();
		try {
			// if this is true then new version of download file should be generated
			if (genNewDownloadFile || !Files.exists(targetDownloadFile, LinkOption.NOFOLLOW_LINKS)) {
				// generate new file
				createZipFile(targetDownloadFile);
			}
			// prepare fetch stream
			return prepareFetchStream(targetDownloadFile);
		} catch (IOException e) {
			log.error("Failed to create file: " + targetDownloadFile + " ,reason: " + e.getMessage(), e);
			throw e;
		} finally {
			sl.unlockRead(rStamp);
		}
	}

	/**
	 * prepare {@link FetchStream}
	 * 
	 * @param targetDownloadFile
	 * @return
	 * @throws IOException
	 */
	private final FetchStream prepareFetchStream(Path targetDownloadFile) throws IOException {
		// TODO: optimize get size of file + stream
		FileChannel fch = null;
		try {
			fch = FileChannel.open(targetDownloadFile, StandardOpenOption.READ);
			return new FetchStream(Channels.newInputStream(fch), targetDownloadFile.getFileName().toString(), "application/octet-stream", fch.size());
		} catch (Exception e) {
			log.error("Failed to prepare FetchStream for file:  " + targetDownloadFile + ", reason: " + e.getMessage(), e);

			if (fch != null) {
				try {
					if (!fch.isOpen()) {
						fch.close();
					}
				} catch (Exception ex) {
					log.error("Failed to close channel on file: " + targetDownloadFile + ", reason:  " + ex.getMessage(), ex);
				}
			}
			throw e;
		}

	}

	// /**
	// * create new package (file) for download if not exists
	// *
	// * @param type
	// * @return
	// */
	// // TODO: move it to dedicated class
	// public final FetchStream prepareDownloadPks(DownloadPackageType
	// downloadPackageType) {
	// if (DownloadPackageType.ZIP == downloadPackageType) {
	// try {
	// Path downloadFile = getFsDownload().resolve(Config.FILE_NAME_PACKAGED_REPO);
	// // if repository content was already generated then get the stream
	// InputStream is = FileWalker.getInputStream(downloadFile);
	// if (is == null) {
	// // if repo.zip file not exists then new stream will be create
	// createZipFile(downloadFile);
	// is = FileWalker.getInputStream(downloadFile);
	// }
	// // return the download stream
	//
	// try (FileChannel fch = FileChannel.open(downloadFile)) {
	// return new FetchStream(is, this.getName() + ".zip",
	// "application/octet-stream", fch.size());
	// }
	// } catch (IOException e) {
	// log.error("Failed to prepare input stream for downloading the repo, reason: "
	// + e.getMessage(), e);
	// }
	// }
	//
	// return null;
	// }

	/**
	 * put all documents to zip file
	 * 
	 * @param targetDownloadFile
	 * @throws IOException
	 */
	private final void createZipFile(Path targetDownloadFile) throws IOException {

		List<BasicFile> allFiles = FileWalker.listFiles(getFsContent());

		try (OutputStream os = Files.newOutputStream(targetDownloadFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			try (ZipOutputStream zos = new ZipOutputStream(os)) {

				for (BasicFile bf : allFiles) {
					ZipEntry zipEntry = new ZipEntry(bf.getPath().toString());
					zos.putNextEntry(zipEntry);

					try (InputStream zfis = Files.newInputStream(bf.getFsPath(), StandardOpenOption.READ)) {
						int BUFFER_SIZE = 8192;
						byte[] buf = new byte[BUFFER_SIZE];
						int length;
						while ((length = zfis.read(buf)) >= 0) {
							zos.write(buf, 0, length);
						}
					} finally {
						zos.closeEntry();
					}
				}
			}
		} catch (IOException e) {
			log.error("Failed to create repo.zip, reason: " + e.getMessage(), e);
			throw e;
		}
	}

	// /**
	// *
	// * @author tomecode.com
	// *
	// */
	// public enum DownloadPackageType {
	// ZIP
	// }

	/**
	 * set the time when something was changed
	 */
	private final void updateMe() {
		this.updateDate = System.currentTimeMillis();
		this.genNewDownloadFile = true;

		this.updateSchevoLog();
	}

	/**
	 * read file infos from file: content.log
	 * 
	 * @return
	 */
	private final void readContentLog() {
		log.info("Read: " + this.fsContentLogFile);

		try (BufferedReader reader = Files.newBufferedReader(fsContentLogFile, FileWalker.DEFAULT_CHARSET)) {
			String currentLine = null;
			while ((currentLine = reader.readLine()) != null) {
				String[] lineData = currentLine.split("\t");
				Document document = new Document();

				document.setId(lineData[0]);
				document.setHash(lineData[1]);
				document.setTime(Long.parseLong(lineData[2]));
				document.setType(lineData[3]);
				document.setPath(lineData[4]);

				this.documents.put(document.getPath(), document);
			}
		} catch (Exception e) {
			log.error("Failed to read " + fsContentLogFile + ", reason: " + e.getMessage(), e);
		}
	}

	private final void readSchevoLog() {
		log.info("Read: " + fsSchevoLogFile);

		if (Files.exists(fsSchevoLogFile, LinkOption.NOFOLLOW_LINKS)) {

			// TODO: optimalize
			try (BufferedReader reader = Files.newBufferedReader(fsSchevoLogFile, FileWalker.DEFAULT_CHARSET)) {
				String currentLine = null;
				while ((currentLine = reader.readLine()) != null) {
					String[] data = currentLine.split("=");
					if (data.length == 2) {
						if (data[0].equals("createDate")) {
							this.createDate = Long.parseLong(data[1]);
						} else if (data[0].equals("updateDate")) {
							this.updateDate = Long.parseLong(data[1]);
						}
					}
				}
			} catch (Exception e) {
				log.error("Failed to read " + fsSchevoLogFile + ", reason: " + e.getMessage(), e);
			}
		}

	}

	private final void updateSchevoLog() {
		log.info("Write: " + fsSchevoLogFile);
		// track time when something was changed

		// TODO: optimalize
		try (BufferedWriter writer = Files.newBufferedWriter(this.fsSchevoLogFile, FileWalker.DEFAULT_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

			StringBuilder sb = new StringBuilder();
			sb.append("createDate=").append(this.createDate).append("\n");
			sb.append("updateDate=").append(this.updateDate).append("\n");

			writer.write(sb.toString());
		} catch (Exception e) {
			log.error("Failed to write: " + fsSchevoLogFile + ", reason: " + e.getMessage(), e);
		}
	}

	/**
	 * 
	 * update push log file
	 * 
	 * @param updatedDocs
	 */
	private final void writeContentLog(PushStatus pushStatus) {
		for (Document updatedDoc : pushStatus.getSingleFiles()) {
			this.documents.put(updatedDoc.getPath(), updatedDoc);
		}

		for (PushPackageFile packageFile : pushStatus.getPackageFiles()) {
			for (Document updatedDoc : packageFile.getContent()) {
				this.documents.put(updatedDoc.getPath(), updatedDoc);
			}
		}

		// TODO: optimize
		try (BufferedWriter writer = Files.newBufferedWriter(this.fsContentLogFile, FileWalker.DEFAULT_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

			for (Document doc : this.documents.values()) {
				writer.append(new StringBuilder()//
						.append(doc.getId()).append("\t")//
						.append(doc.getHash()).append("\t")//
						.append(doc.getTime()).append("\t")//
						.append(doc.getType()).append("\t")//
						.append(doc.getPath()).append("\t")//
						.append("\n"));
			}
		} catch (Exception e) {
			log.error("Failed to update|write to " + fsContentLogFile + " reason: " + e.getMessage(), e);
		}
	}

}
