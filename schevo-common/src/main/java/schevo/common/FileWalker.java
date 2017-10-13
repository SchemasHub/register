package schevo.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.hash.Hashing;

/**
 * 
 * @author tomecode.com
 *
 */
public final class FileWalker {

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

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

	public static final void channelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		while (src.read(buffer) != -1) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			dest.write(buffer);
			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}
		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while (buffer.hasRemaining()) {
			dest.write(buffer);
		}
		buffer.clear();
		buffer = null;
	}

	/**
	 * unzip content of zip file
	 * 
	 * @param srcZip
	 * @param destDir
	 * @throws IOException
	 */
	public static final void unizip(Path srcZip, Path destDir) throws IOException {

		byte[] buffer = new byte[1024];

		try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(srcZip, StandardOpenOption.READ))) {
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				// create file if not exists
				Path destFile = mkFile(destDir.resolve(entry.toString()));
				// copy/extract data to file
				try (OutputStream os = Files.newOutputStream(destFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
					int len;
					while ((len = zis.read(buffer)) > 0) {
						os.write(buffer, 0, len);
					}
				} finally {
					if (zis != null) {
						zis.closeEntry();
					}

				}
			}
		}

		// try (FileSystem zipFileSystem = FileSystems.newFileSystem(srcZip, null)) {
		// final Path root = zipFileSystem.getRootDirectories().iterator().next();
		//
		// Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
		// @Override
		// public final FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		// throws IOException {
		// final Path destFile = destDir.resolve(file.toString());
		// try {
		// // create directories and file
		// mkFile(destFile);
		// // copy data to file
		// Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
		// } catch (DirectoryNotEmptyException ignore) {
		// }
		// return FileVisitResult.CONTINUE;
		// }
		//
		// // @Override
		// // public final FileVisitResult preVisitDirectory(Path dir,
		// BasicFileAttributes
		// // attrs) throws IOException {
		// // FileWalker.mkDirs(destDir.resolve(dir));
		// // return FileVisitResult.CONTINUE;
		// // }
		// });
		//
		// }

	}

	public static final String fileHash(Path file) {
		if (!Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS) && Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			try {
				return com.google.common.io.Files.asByteSource(file.toFile()).hash(Hashing.sha256()).toString();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "unknown";
	}
}
