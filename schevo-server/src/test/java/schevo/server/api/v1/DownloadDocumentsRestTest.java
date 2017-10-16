package schevo.server.api.v1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.UriConfigs;
import schevo.common.FileWalker;
import schevo.common.SpaceRef;
import schevo.server.SchevoServer;
import schevo.server.api.DocumentControllerV1;
import schevo.server.api.SpacesControllerV1;

/**
 * several tests to download documents from space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class DownloadDocumentsRestTest extends BasicTestInfra {

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
	 * create new space and push docs
	 * 
	 * @param files
	 * @return
	 * @throws Exception
	 */
	private final SpaceRef newSpaceAndPushDocs(MockMultipartFile[] files) throws Exception {
		SpaceRef spaceRef = createNewTestSpace();

		MockMultipartHttpServletRequestBuilder pushApi = MockMvcRequestBuilders.fileUpload(UriConfigs.PUSH_URI);
		for (MockMultipartFile uploadFile : files) {
			pushApi.file(uploadFile);
		}
		// upload to
		pushApi.param(UriConfigs.PARAM_SPACE_REF, spaceRef.getPath());
		// send post request
		this.mockMvc.perform(pushApi).andExpect(MockMvcResultMatchers.status().isOk());

		return spaceRef;
	}

	/**
	 * fetch particular version of repository
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testFetchRepositoryVersion() throws Exception {
		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "hello/world/other-file-name.data", "text/plain", "some other type".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};

		SpaceRef spaceRef = newSpaceAndPushDocs(files);
		ResultActions resultActions = performRequest(spaceRef, schevo.UriConfigs.FETCH_URI);

		Path fsFetchFile = FileWalker.mkFile(Paths.get(System.getProperty("user.dir"), "target", "fetch", UUID.randomUUID().toString(), spaceRef.getWorkspace() + ".zip"));
		resultActions.andExpect(MockMvcResultMatchers.status().isOk());
		MockHttpServletResponse response = resultActions.andReturn().getResponse();
		String fnName = ((String) response.getHeaderValue("Content-disposition")).split("filename=")[1];

		// compare name
		Assert.assertEquals(spaceRef.getVersion() + ".zip", fnName);
		Files.write(fsFetchFile, response.getContentAsByteArray());
		// check if a file has downloaded successfully ...
		Assert.assertTrue(fsFetchFile.toFile().length() > 1);

		try (ZipFile zf = new ZipFile(fsFetchFile.toFile())) {
			Enumeration<? extends ZipEntry> es = zf.entries();
			while (es.hasMoreElements()) {
				ZipEntry e = es.nextElement();

				String en = e.getName().replaceAll("\\\\", "/");

				boolean found = false;
				for (MockMultipartFile file : files) {
					found |= (file.getOriginalFilename().equals(en));
				}

				if (!found) {
					Assert.fail("Not found file: " + e.getName() + " in downloaded repository");
				}
			}
		}
	}

	/**
	 * fetch documents form repository which not exists
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testFetchRepositoryNotFound() throws Exception {
		performRequest(new SpaceRef("w", "r", UUID.randomUUID().toString()), UriConfigs.FETCH_URI).andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	/**
	 * download single document
	 * 
	 * @throws Exception
	 */
	@Test
	public final void testFetcSingleDocument() throws Exception {
		MockMultipartFile[] files = new MockMultipartFile[] { //
				new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes()), //
				new MockMultipartFile("file", "hello/world/other-file-name.data", "text/plain", "some other type".getBytes()), //
				new MockMultipartFile("file", "ahoj", "application/json", "{\"json\": \"someValue\"}".getBytes())//
		};
		// create temp space
		SpaceRef spaceRef = newSpaceAndPushDocs(files);

		performRequest(spaceRef, UriConfigs.FETCH_URI, "scope=single");
	}

	/**
	 * perform rest request
	 * 
	 * @param spaceRef
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	private final ResultActions performRequest(SpaceRef spaceRef, String uri, String... params) throws Exception {

		MockHttpServletRequestBuilder restApi = MockMvcRequestBuilders.get(uri);
		// upload to
		restApi.param(UriConfigs.PARAM_SPACE_REF, spaceRef.toString());

		if (params != null) {
			for (String p : params) {
				String[] pv = p.split("=");
				restApi.param(pv[0], pv[1]);
			}
		}

		// send post request
		return this.mockMvc.perform(restApi);
	}
}
