package com.tomecode.schemashub.server.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * @author tomecode.com
 *
 */
public final class FileWalker {

	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FileWalker.class);

	public final static List<BasicFile> listFiles(Path fromPath) throws IOException {
		DocumentVisitor bv = new DocumentVisitor(fromPath);
		Files.walkFileTree(fromPath, bv);
		return bv.getFiles();
	}

	private static final class DocumentVisitor extends BasicFileVisitor<BasicFile> {

		public DocumentVisitor(Path startPath) {
			super(startPath);
		}

		@Override
		public final void file(Path revitalizedPath, Path sourceFile, String extension, BasicFileAttributes attr) {
			files.add(new BasicFile(sourceFile, revitalizedPath));
		}
	}

	/**
	 * get {@link InputStream} from file
	 * 
	 * @param fsPath
	 * @return
	 * @throws IOException
	 */
	public static final InputStream getInputStream(Path fsPath) throws IOException {
		if (Files.exists(fsPath, LinkOption.NOFOLLOW_LINKS)) {
			return Files.newInputStream(fsPath, StandardOpenOption.READ);
		}
		return null;
	}

	public static final Path mkFile(Path path) throws IOException {
		mkDirs(path.getParent());
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return Files.createFile(path);
		}
		return path;
	}

	public static final Path mkDirs(Path path) throws IOException {
		if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			return Files.createDirectories(path);
		}
		return path;
	}

	public static final void rmDir(Path dirPath) throws IOException {
		Files.walk(dirPath).map(Path::toFile).sorted(Comparator.comparing(File::isDirectory)).forEach(File::delete);
	}

	public static final void rmrfDir(Path dirPath) {
		try {
			if (dirPath != null && Files.exists(dirPath, LinkOption.NOFOLLOW_LINKS)) {
				Files.walk(dirPath).map(Path::toFile).sorted(Comparator.comparing(File::isDirectory)).forEach(File::delete);
			}
		} catch (Exception e) {
			log.error("Failed to delete dir: " + dirPath + " reason: " + e.getMessage(), e);
		}
	}

	/**
	 * file extension
	 * 
	 * @param file
	 * @return
	 */
	public static final String getExtension(Path file) {
		return getExtension(file.getFileName().toString());
	}

	/**
	 * get file extension
	 * 
	 * @param fileName
	 * @return
	 */
	public final static String getExtension(String fileName) {
		if (fileName != null) {
			int extIndex = fileName.lastIndexOf(".");
			if (extIndex != -1) {
				return fileName.substring(extIndex).toLowerCase();
			}
		}

		return null;
	}

	/**
	 * Basic file visitor
	 * 
	 * @author 431999
	 *
	 * @param <T>
	 */
	public static abstract class BasicFileVisitor<T extends BasicFile> extends SimpleFileVisitor<Path> {

		protected List<T> files;

		private final Path startPath;

		public BasicFileVisitor(Path startPath) {
			this.startPath = startPath;
			this.files = new ArrayList<>();
		}

		public final Path getStartPath() {
			return startPath;
		}

		public List<T> getFiles() {
			return files;
		}

		/**
		 * Invoked for a directory before entries in the directory are visited.
		 *
		 * <p>
		 * Unless overridden, this method returns {@link FileVisitResult#CONTINUE
		 * CONTINUE}.
		 */
		@Override
		public final FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public abstract void file(Path revitalizedPath, Path sourceFile, String extension, BasicFileAttributes attr);

		@Override
		public final FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attr) {
			if (attr.isSymbolicLink() || !attr.isRegularFile()) {
				return FileVisitResult.SKIP_SIBLINGS;
			}
			Path revitalizedPath = startPath.relativize(sourceFile);

			String extension = getExtension(revitalizedPath);
			file(revitalizedPath, sourceFile, extension, attr);

			return FileVisitResult.CONTINUE;
		}
	}

}
