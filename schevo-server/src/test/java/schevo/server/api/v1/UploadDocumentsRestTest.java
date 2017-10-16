package schevo.server.api.v1;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.UriConfigs;
import schevo.common.SpaceRef;
import schevo.server.SchevoServer;
import schevo.server.api.DocumentControllerV1;
import schevo.server.api.SpaceError;
import schevo.server.api.SpacesControllerV1;
import schevo.server.space.SpacesFsLocal;

/**
 * several tests to upload documents to particular space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class UploadDocumentsRestTest extends BasicTestInfra {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
	}

	@Autowired
	private SpacesControllerV1 restSpaces;

	@Autowired
	private DocumentControllerV1 restDocuments;

	@Before
	public final void setup() {
		this.restSpaces = Mockito.mock(SpacesControllerV1.class);
		this.restDocuments = Mockito.mock(DocumentControllerV1.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(restSpaces, restDocuments).build();

	}

	/**
	 * upload file (s) to workspace that not exists
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadToNotExistsWorkspace() throws Exception {
		SpaceRef spaceRef = createNewTestSpace();
		// prepare temp files

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI).file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));
		// upload to

		String spaceRefPath = UUID.randomUUID().toString() + "/" + spaceRef.getRepository() + "/" + spaceRef.getVersion();

		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRefPath);
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		SpaceError expectedError = SpaceError.spaceRefNotFound(spaceRefPath);

		Assert.assertEquals(expectedError.getName(), resp.getString("name"));
		Assert.assertEquals(expectedError.getMessage(), resp.getString("message"));

	}

	@Test
	public void testUploadToNotExistsRepository() throws Exception {
		SpaceRef spaceRef = createNewTestSpace();
		// prepare temp files

		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI).file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));

		String spaceRefPath = spaceRef.getWorkspace() + "/" + UUID.randomUUID().toString() + "/" + spaceRef.getVersion();

		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRefPath);
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		SpaceError expectedError = SpaceError.spaceRefNotFound(spaceRefPath);

		Assert.assertEquals(expectedError.getName(), resp.getString("name"));
		Assert.assertEquals(expectedError.getMessage(), resp.getString("message"));

	}

	@Test
	public void testUploadToNotExistsRepositoryVersion() throws Exception {
		SpaceRef spaceRef = createNewTestSpace();
		// prepare temp files

		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI).file(new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes()));

		String spaceRefPath = spaceRef.getWorkspace() + "/" + spaceRef.getRepository() + "/" + UUID.randomUUID().toString();

		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRefPath);
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		SpaceError expectedError = SpaceError.spaceRefNotFound(spaceRefPath);

		Assert.assertEquals(expectedError.getName(), resp.getString("name"));
		Assert.assertEquals(expectedError.getMessage(), resp.getString("message"));
	}

	/**
	 * request is wrong because one file is without name
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadFileWithoutNames() throws Exception {
		SpaceRef spaceRef = createNewTestSpace();
		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI);
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath());
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isBadRequest());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		SpaceError expectedError = SpaceError.pushFileNameEmpty(spaceRef.getPath());

		Assert.assertEquals(expectedError.getName(), resp.getString("name"));
		Assert.assertEquals(expectedError.getMessage(), resp.getString("message"));

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef.getWorkspace(), spaceRef.getRepository(), spaceRef.getVersion()));
		Path fsDocs = fsVersion.resolve("content");

		Assert.assertEquals(0, Files.list(fsDocs).count());
	}

	/**
	 * request with several files, test if files are stored in repo
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUploadMultipleFiles() throws Exception {
		SpaceRef spaceRef = createNewTestSpace();

		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "other-file-name.data", "text/plain", "some other type".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI);
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath());
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isOk());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		Assert.assertEquals(3, resp.getJSONArray("singleFiles").length());
		Assert.assertEquals(0, resp.getJSONArray("packageFiles").length());

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef.getWorkspace(), spaceRef.getRepository(), spaceRef.getVersion()));
		Assert.assertTrue("Repository version not exists", Files.exists(fsVersion, LinkOption.NOFOLLOW_LINKS));
		Path fsDocs = fsVersion.resolve("content");
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
		SpaceRef spaceRef = createNewTestSpace();

		// prepare temp files

		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "sampleZipBasic.zip", "application/zip", Files.readAllBytes(Paths.get(UploadDocumentsRestTest.class.getResource("sampleZipBasic.zip").toURI()))) //
		};

		// prepare files for rest
		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI);
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath());
		// send post request
		ResultActions resultActions = this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isOk());

		JSONObject resp = new JSONObject(resultActions.andReturn().getResponse().getContentAsString());

		Assert.assertEquals(0, resp.getJSONArray("singleFiles").length());
		Assert.assertEquals(1, resp.getJSONArray("packageFiles").length());

		Path fsVersion = SpacesFsLocal.get().getFsPathRoot().resolve(Paths.get(spaceRef.getWorkspace(), spaceRef.getRepository(), spaceRef.getVersion()));
		Assert.assertTrue("Repository version not exists", Files.exists(fsVersion, LinkOption.NOFOLLOW_LINKS));
		Path fsDocs = fsVersion.resolve("content");
		Assert.assertTrue("Repository version not exists", Files.exists(fsDocs, LinkOption.NOFOLLOW_LINKS));

		// check if all files exists
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/test.txt"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/json.schema.txt"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/testDir/com/fake/universumJsonSchema"), LinkOption.NOFOLLOW_LINKS));
		Assert.assertTrue(Files.exists(fsDocs.resolve("sampleZip/testDir/com/fake/universumJsonSchema.json"), LinkOption.NOFOLLOW_LINKS));
	}

}
