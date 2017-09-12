package com.tomecode.schemashub.server.register.localfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tomecode.schemashub.server.Config;
import com.tomecode.schemashub.server.common.BasicFile;
import com.tomecode.schemashub.server.common.DownloadStream;
import com.tomecode.schemashub.server.common.FileWalker;
import com.tomecode.schemashub.server.register.SpaceException;

/**
 * Version of repository
 * 
 * @author tomecode.com
 *
 */
public class RepositoryVersion extends LocalFsDir {

	private static final Logger log = Logger.getLogger(RepositoryVersion.class);

	@JsonIgnore
	private Path fsContent;
	@JsonIgnore
	private Path fsDownload;

	/**
	 * 
	 * @param fsPathParent
	 * @param name
	 * @throws SpaceException
	 */
	public RepositoryVersion(Path fsPathParent, String name) throws SpaceException {
		super(fsPathParent, name);

		try {
			this.fsContent = FileWalker.mkDirs(fsPath.resolve(".docs"));
		} catch (IOException e) {
			log.error("Failed to create dir: .docs, reason: " + e.getMessage(), e);
			throw new SpaceException("Failed to create dir: .docs reason: " + e.getMessage(), e);
		}
		try {
			this.fsDownload = FileWalker.mkDirs(fsPath.resolve(".download"));
		} catch (IOException e) {
			log.error("Failed to create dir: .download, reason: " + e.getMessage(), e);
			throw new SpaceException("Failed to create dir: .download reason: " + e.getMessage(), e);
		}

	}

	@JsonIgnore
	public final Path getFsDownload() {
		return this.fsDownload;
	}

	@JsonIgnore
	public final Path getFsContent() {
		return this.fsContent;
	}

	/**
	 * create new package (file) for download if not exists
	 * 
	 * @param type
	 * @return
	 */
	// TODO: move it to dedicated class
	public final DownloadStream prepareDownloadPks(DownloadPackageType downloadPackageType) {
		if (DownloadPackageType.ZIP == downloadPackageType) {
			try {
				Path downloadFile = getFsDownload().resolve(Config.FILE_NAME_PACKAGED_REPO);
				// if repository content was already generated then get the stream
				InputStream is = FileWalker.getInputStream(downloadFile);
				if (is == null) {
					// if repo.zip file not exists then new stream will be create
					repoToZip(downloadFile);
					is = FileWalker.getInputStream(downloadFile);
				}
				// return the download stream

				try (FileChannel fch = FileChannel.open(downloadFile)) {
					return new DownloadStream(is, this.getName() + ".zip", "application/octet-stream", fch.size());
				}
			} catch (IOException e) {
				log.error("Failed to prepare input stream for downloading the repo");
			}
		}

		return null;
	}

	/**
	 * convert all documents from repository to zip file
	 * 
	 * @return
	 * @throws IOException
	 */
	private final void repoToZip(Path downloadFile) throws IOException {

		List<BasicFile> allFiles = FileWalker.listFiles(getFsContent());

		try (OutputStream os = Files.newOutputStream(downloadFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
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

	/**
	 * 
	 * @author tomecode.com
	 *
	 */
	public enum DownloadPackageType {
		ZIP
	}

	/**
	 * 
	 * @param uf
	 * @throws IOException
	 */
	public final void pushDocument(MultipartFile uf) throws IOException {
		log.info("append file: " + uf.getOriginalFilename() + " to version: " + this.getName());

		String fileExt = FileWalker.getExtension(uf.getOriginalFilename());
		if (".zip".equals(fileExt)) {
			extractZip(uf.getInputStream());
		} else {
			Path path = fsContent.resolve(uf.getOriginalFilename());
			// create dirs (if not exists)
			FileWalker.mkFile(path);
			// transform to file
			uf.transferTo(path.toFile());

		}

	}

	/**
	 * extract zip
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	private final void extractZip(InputStream inputStream) throws IOException {
		byte[] buff = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(inputStream)) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				// extract entity
				extractEntry(buff, zis, ze.getName());
			}
		}
	}

	/**
	 * extract entity
	 * 
	 * @param buff
	 * @param zis
	 * @param entityName
	 * @throws IOException
	 */
	private final void extractEntry(byte[] buff, ZipInputStream zis, String entityName) throws IOException {
		Path targetFile = this.fsContent.resolve(name);
		try {
			FileWalker.mkFile(targetFile);
		} catch (IOException e) {
			log.error("version: " + getName() + " failed to create new file: " + targetFile + " in: " + this.getFsContent());
			throw e;
		}

		try (OutputStream os = Files.newOutputStream(targetFile, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
			int l = 0;
			// write buffer to file
			while ((l = zis.read(buff)) > 0) {
				os.write(buff, 0, l);
			}
		} catch (IOException e) {
			log.error("version: " + getName() + " Failed to extract/write entiy: " + entityName + " to file:" + targetFile);
			throw e;
		}

	}

}
