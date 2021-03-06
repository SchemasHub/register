package schevo.server.api.v1;

import static schevo.UriConfigs.WORKSPACES_URI;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.server.Config;
import schevo.server.SchevoServer;
import schevo.server.api.SpacesControllerV1;
import schevo.server.space.SpacesFsLocal;

/**
 * 
 * several tests to create space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class CreateSpaceRestTest extends BasicTestInfra {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "restApi" + UUID.randomUUID().toString()).toString());
	}

	private MockMvc mockMvc;

	@Autowired
	private SpacesControllerV1 restSpaces;

	@Before
	public final void setup() {
		this.restSpaces = Mockito.mock(SpacesControllerV1.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(restSpaces).build();
	}

	/**
	 * test create workspace
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspace() throws Exception {
		String name = UUID.randomUUID().toString();
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + name)).andExpect(MockMvcResultMatchers.status().isOk());
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(name), LinkOption.NOFOLLOW_LINKS));
	}

	/**
	 * test create space longer as
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspaceWithLongName() throws Exception {
		String name = genStr(Config.SPACE_NAME_MAX_LENGTH + 30);
		// create space with name
		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + name));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(name.toLowerCase().substring(0, Config.SPACE_NAME_MAX_LENGTH))));
	}

	/**
	 * test try to create workspace where name contains special character like blank
	 * space, etc.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspaceWithSpacialChars() throws Exception {
		// create space with name
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/a A ba1-a ? 19-  AAhoj"))//
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("a-a-ba1-a")));
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/aa   aa BB     a   "))//
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("aa---aa-bb-----a")));
	}

	/**
	 * create workspace where name is null`or empty
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspaceNull() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI)).andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}

	/**
	 * create duplicate workspace
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspacAlreadyExists() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String name = UUID.randomUUID().toString();
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + name)).andExpect(MockMvcResultMatchers.status().isOk());

		// create again should be error
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + name)).andExpect(MockMvcResultMatchers.status().isBadRequest());//
	}

	/**
	 * create new repository
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepository() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();
		String rName = UUID.randomUUID().toString();

		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(wName, rName)), LinkOption.NOFOLLOW_LINKS));
	}

	/**
	 * the test try to create a repository with a longer name than allowed
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryWithLongName() throws Exception {
		String wname = UUID.randomUUID().toString();
		String name = genStr(Config.SPACE_NAME_MAX_LENGTH + 30);
		// create space with name
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wname)).andExpect(MockMvcResultMatchers.status().isOk());

		ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wname + "/" + name));
		result.andExpect(MockMvcResultMatchers.status().isOk());
		result.andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(name.toLowerCase().substring(0, Config.SPACE_NAME_MAX_LENGTH))));
	}

	/**
	 * create new repository
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryNull() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();

		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/ ")).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * create new repository
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryAlreadyExists() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();
		String rName = "Abcd";

		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isBadRequest());
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName.toLowerCase())).andExpect(MockMvcResultMatchers.status().isBadRequest());
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName.toUpperCase())).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * test create repository version
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryVersion() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();
		String rName = UUID.randomUUID().toString();
		String rvName = UUID.randomUUID().toString();
		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());

		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName + "/" + rvName)).andExpect(MockMvcResultMatchers.status().isOk());

		Path fsPath = Paths.get(wName, rName, rvName);
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath), LinkOption.NOFOLLOW_LINKS));
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath.resolve("content")), LinkOption.NOFOLLOW_LINKS));
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath.resolve("download")), LinkOption.NOFOLLOW_LINKS));

	}

	/**
	 * test creat repository version - null
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryVersionNull() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();
		String rName = UUID.randomUUID().toString();
		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName + "/ ")).andExpect(MockMvcResultMatchers.status().isBadRequest());
		// create empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + "/ / / ")).andExpect(MockMvcResultMatchers.status().isBadRequest());

		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName + "/ ")).andExpect(MockMvcResultMatchers.status().isBadRequest());

	}

	/**
	 * test repository version already exists
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryVersionAlreadyExists() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String wName = UUID.randomUUID().toString();
		String rName = UUID.randomUUID().toString();
		String rvName = UUID.randomUUID().toString();
		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName + "/" + rvName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wName + "/" + rName + "/" + rvName)).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	/**
	 * test try to create repository where name contains special character like
	 * blank space, etc.
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateRepositoryWithSpacialChars() throws Exception {
		String wname = UUID.randomUUID().toString();
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wname))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create space with name
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wname + "/a A ba1-a ? 19-  AAhoj"))//
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("a-a-ba1-a")));
		this.mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + wname + "/aa   aa BB     a   "))//
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.name", Matchers.is("aa---aa-bb-----a")));
	}

}
