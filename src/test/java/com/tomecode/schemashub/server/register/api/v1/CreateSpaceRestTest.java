package com.tomecode.schemashub.server.register.api.v1;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tomecode.schemashub.server.ServerApp;
import com.tomecode.schemashub.server.register.localfs.SpacesFsLocal;

/**
 * 
 * several tests to create space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class CreateSpaceRestTest {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "restApi" + UUID.randomUUID().toString()).toString());
	}

	private static final String BASIC_URI = "/register/v1";

	private MockMvc mockMvc;

	@Autowired
	private RegisterController restApi;

	@Before
	public final void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(restApi).build();
		restApi = Mockito.mock(RegisterController.class);
	}

	// TODO: test get space where names contains: blank spaces/characters etc. all
	// special charaters should be replaces to '-
	// TODO: test space name should be not longer than 100 characters

	/**
	 * test create workspace
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspace() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		String name = UUID.randomUUID().toString();
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + name))//
				.andExpect(MockMvcResultMatchers.status().isOk());
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(name), LinkOption.NOFOLLOW_LINKS));
	}

	/**
	 * create workspace where name is null`or empty
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testCreateWorkspaceNull() throws Exception {
		// restApi = Mockito.mock(RegisterApiV1.class);
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/ "))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + name))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create again should be error
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + name))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());//
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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName))//
				.andExpect(MockMvcResultMatchers.status().isOk());
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(wName, rName)), LinkOption.NOFOLLOW_LINKS));
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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/ "))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());

		// create new empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isBadRequest());
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName.toLowerCase())).andExpect(MockMvcResultMatchers.status().isBadRequest());
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName.toUpperCase())).andExpect(MockMvcResultMatchers.status().isBadRequest());
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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/" + rvName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		Path fsPath = Paths.get(wName, rName, rvName);
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath), LinkOption.NOFOLLOW_LINKS));
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath.resolve(".docs")), LinkOption.NOFOLLOW_LINKS));
		Assert.assertEquals(true, Files.exists(SpacesFsLocal.get().getFsPathRoot().resolve(fsPath.resolve(".download")), LinkOption.NOFOLLOW_LINKS));

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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName))//
				.andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/ "))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
		// create empty repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/ / / "))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/ "))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());

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
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName))//
				.andExpect(MockMvcResultMatchers.status().isOk());

		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName))//
				.andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/" + rvName))//
				.andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/" + rvName))//
				.andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

}
