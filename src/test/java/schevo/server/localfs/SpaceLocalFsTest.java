package schevo.server.localfs;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;
import schevo.server.Config;
import schevo.server.SpaceException.RepositoryExistsException;
import schevo.server.SpaceException.RepositoryVersionExistsException;
import schevo.server.SpaceException.WorkspaceExistsException;
import schevo.server.space.Repository;
import schevo.server.space.RepositoryVersion;
import schevo.server.space.SpacesFsLocal;
import schevo.server.space.Workspace;

/**
 * some tests for creating spaces
 * 
 * @author tomecode.com
 *
 */
public final class SpaceLocalFsTest extends TestCase {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
	}

	/**
	 * create new space
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private final SpacesFsLocal createNewSpace(String name) throws Exception {
		Path dirTarget = Paths.get(System.getProperty("user.dir"), "target");

		Path dirCustomSpace = dirTarget.resolve(Paths.get("testSpace", name == null ? UUID.randomUUID().toString() : name));
		System.setProperty("space.dir", dirCustomSpace.toString());

		Constructor<?> ccc = SpacesFsLocal.class.getDeclaredConstructors()[0];
		ccc.setAccessible(true);
		return (SpacesFsLocal) ccc.newInstance();

	}

	@Test
	public final void testInitSpace() throws Exception {
		SpacesFsLocal spaceLocalFs = createNewSpace(null);

		if (!Files.exists(spaceLocalFs.getFsPathRoot(), LinkOption.NOFOLLOW_LINKS)) {
			throw new Exception("Not found dir: " + spaceLocalFs.getFsPathRoot());
		}
	}

	/**
	 * test: create new workspace
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspace() throws Exception {
		SpacesFsLocal space = createNewSpace(null);

		Workspace workspace = null;
		try {
			workspace = space.newWorkspace("w01");
		} catch (WorkspaceExistsException e) {
			Assert.fail("workspace already exists, but not exists!");
		}
		Assert.assertNotNull(workspace);
		Assert.assertEquals("w01", workspace.getName());
		Assert.assertTrue("Worksapce not exists in FS", Files.exists(workspace.getFsPath(), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue("Worksapce meta dir not exists in FS", Files.exists(workspace.getFsPath().resolve(Config.INTERNAL_META_DATA_DIR), LinkOption.NOFOLLOW_LINKS));
		try {
			workspace = space.newWorkspace("w01");
			Assert.fail("workspace already exists, exception should be throwed!");
		} catch (WorkspaceExistsException e) {
			//
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * test: create new repository
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepository() throws Exception {
		SpacesFsLocal space = createNewSpace(null);
		Workspace workspace = space.newWorkspace(UUID.randomUUID().toString());
		String repoName = UUID.randomUUID().toString();
		Repository newRepo = null;
		try {
			newRepo = workspace.newRepository(repoName);
		} catch (RepositoryExistsException e) {
			Assert.fail("Repository already exists, but not exists!");
		}
		Assert.assertNotNull(newRepo);
		Assert.assertEquals(repoName, newRepo.getName());
		Assert.assertTrue("Repository not exists in FS", Files.exists(newRepo.getFsPath(), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue("Repository meta dir not exists in FS", Files.exists(newRepo.getFsPath().resolve(Config.INTERNAL_META_DATA_DIR), LinkOption.NOFOLLOW_LINKS));
		try {
			workspace.newRepository(repoName);
			Assert.fail("repository already exists, exception should be throwed!");
		} catch (RepositoryExistsException e) {
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * test: create new version
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryVersion() throws Exception {
		SpacesFsLocal space = createNewSpace(null);
		Workspace workspace = space.newWorkspace(UUID.randomUUID().toString());
		Repository repository = workspace.newRepository(UUID.randomUUID().toString());

		String versionName = UUID.randomUUID().toString();

		RepositoryVersion newVersion = null;
		try {
			newVersion = repository.newVersion(versionName);
		} catch (RepositoryVersionExistsException e) {
			Assert.fail("Repository version already exists, but not exists!");
		}
		Assert.assertNotNull(newVersion);
		Assert.assertEquals(versionName, newVersion.getName());
		Assert.assertTrue("Repository version not exists in FS", Files.exists(newVersion.getFsPath(), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue("Repository version meta dir not exists in FS", Files.exists(newVersion.getFsPath().resolve(Config.INTERNAL_META_DATA_DIR), LinkOption.NOFOLLOW_LINKS));
		try {
			repository.newVersion(versionName);
			Assert.fail("repository version already exists, exception should be throwed!");
		} catch (RepositoryVersionExistsException e) {
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
