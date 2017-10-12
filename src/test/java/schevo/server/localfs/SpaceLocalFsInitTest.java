package schevo.server.localfs;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Assert;

import junit.framework.TestCase;
import schevo.server.Config;
import schevo.server.common.FileWalker;
import schevo.server.space.SpacesFsLocal;

/**
 * 
 * test for init space
 * 
 * @author Tome (tomecode.com)
 *
 */
public class SpaceLocalFsInitTest extends TestCase {

	static {
		System.setProperty(Config.CONFIG_SPACE_LOCAL_DIR, Paths.get(System.getProperty("user.dir"), "target", "initSpace").toString());
	}

	public final void testInitLocalFs() throws Exception {
		Path root = FileWalker.mkDirs(Paths.get(System.getProperty("spaces.dir"), ""));

		byte[] buff = new byte[1024];
		try (ZipInputStream zis = new ZipInputStream(SpaceLocalFsInitTest.class.getResourceAsStream("sampleWorkspace.zip"))) {
			ZipEntry ze = null;
			while ((ze = zis.getNextEntry()) != null) {
				// extract entity

				Path targetPath = Paths.get(root.toString(), ze.getName());
				if (ze.isDirectory()) {
					FileWalker.mkDirs(targetPath);
				} else {
					FileWalker.mkFile(targetPath);
					try (OutputStream os = Files.newOutputStream(targetPath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
						int l = 0;
						// write buffer to file
						while ((l = zis.read(buff)) > 0) {
							os.write(buff, 0, l);
						}
					}
				}
			}
		}

		Constructor<?> cons = SpacesFsLocal.class.getDeclaredConstructors()[0];
		cons.setAccessible(true);
		SpacesFsLocal s = (SpacesFsLocal) cons.newInstance();
		org.junit.Assert.assertNotNull(s.getWorkspace("workspace"));
		Assert.assertEquals(1, s.getWorkspace("workspace").getReposiotieNames().size());
		Assert.assertNotNull(s.getWorkspace("workspace").getRepository("repository"));
		Assert.assertEquals(1, s.getWorkspace("workspace").getReposiotieNames().size());
		Assert.assertNotNull(s.getWorkspace("workspace").getRepository("repository").getVersions().get("v1.0.0"));

	}

}
