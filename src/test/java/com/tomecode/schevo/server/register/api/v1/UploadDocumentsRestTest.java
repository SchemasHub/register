package com.tomecode.schevo.server.register.api.v1;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tomecode.schevo.server.ServerApp;
import com.tomecode.schevo.server.register.api.v1.RegisterController;
import com.tomecode.schevo.server.register.localfs.SpacesFsLocal;

/**
 * several tests to upload documents to particular space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class UploadDocumentsRestTest {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
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

	/**
	 * just create new test space
	 * 
	 * @return
	 * @throws Exception
	 */
	private final String[] createTestSpace() throws Exception {
		String wName = "w" + System.nanoTime();
		String rName = "r" + System.nanoTime();
		String rvName = "1";
		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/" + rvName)).andExpect(MockMvcResultMatchers.status().isOk());

		return new String[] { wName, rName, rvName };
	}

	/**
	 * upload file (s) to workspace that not exists
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadToNotExistsWorkspace() throws Exception {
		String spaceRef[] = createTestSpace();
		// prepare temp files

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments").file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));
		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + UUID.randomUUID().toString() + "/" + UUID.randomUUID().toString());
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isNotFound());

		// upload to
		restApi.param("spaceRef", spaceRef[0]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isNotFound());

	}

	@Test
	public void testUploadToNotExistsRepository() throws Exception {
		String spaceRef[] = createTestSpace();
		// prepare temp files

		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments").file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));

		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + UUID.randomUUID().toString());
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isNotFound());

		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void testUploadToNotExistsRepositoryVersion() throws Exception {
		String spaceRef[] = createTestSpace();
		// prepare temp files

		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments").file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));

		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + UUID.randomUUID().toString());
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isNotFound());

		// upload to
		restApi.param("spaceRef", spaceRef[0]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isNotFound());

	}

	/**
	 * request is wrong because one file is without name
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadFileWithoutNames() throws Exception {
		String spaceRef[] = createTestSpace();
		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments");
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + spaceRef[2]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef[0], spaceRef[1], spaceRef[2]));
		Path fsDocs = fsVersion.resolve(".docs");

		Assert.assertEquals(0, Files.list(fsDocs).count());
	}

	/**
	 * request with several files, test if files are stored in repo
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadMultipleFiles() throws Exception {
		String spaceRef[] = createTestSpace();

		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "other-file-name.data", "text/plain", "some other type".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments");
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + spaceRef[2]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isOk());

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef[0], spaceRef[1], spaceRef[2]));
		Assert.assertTrue("Repository version not exists", Files.exists(fsVersion, LinkOption.NOFOLLOW_LINKS));
		Path fsDocs = fsVersion.resolve(".docs");
		Assert.assertTrue("Repository version not exists", Files.exists(fsDocs, LinkOption.NOFOLLOW_LINKS));

		Assert.assertEquals(files.length, Files.list(fsDocs).count());

		// check if all files exists
		for (MockMultipartFile uploadFiles : files) {
			Assert.assertTrue("File not found: " + uploadFiles.getOriginalFilename(), Files.exists(fsDocs.resolve(uploadFiles.getOriginalFilename()), LinkOption.NOFOLLOW_LINKS));
		}
	}

	/**
	 * upload single zip file, and check if content of zip was extracted
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadZipFile() throws Exception {
		String spaceRef[] = createTestSpace();

		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "sampleZipBasic.zip", "application/zio", Files.readAllBytes(Paths.get(UploadDocumentsRestTest.class.getResource("sampleZipBasic.zip").toURI()))) //
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments");
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + spaceRef[2]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isOk());

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef[0], spaceRef[1], spaceRef[2]));
		Assert.assertTrue("Repository version not exists", Files.exists(fsVersion, LinkOption.NOFOLLOW_LINKS));
		Path fsDocs = fsVersion.resolve(".docs");
		Assert.assertTrue("Repository version not exists", Files.exists(fsDocs, LinkOption.NOFOLLOW_LINKS));

		// check if all files exists
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/test.txt"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/json.schema.txt"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/testDir/com/fake/universumJsonSchema"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/testDir/com/fake/universumJsonSchema.json"), LinkOption.NOFOLLOW_LINKS));
	}

}
