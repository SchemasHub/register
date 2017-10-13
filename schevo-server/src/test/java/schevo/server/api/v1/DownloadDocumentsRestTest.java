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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.common.FileWalker;
import schevo.server.SchevoServer;
import schevo.server.api.RegisterControllerV1;

/**
 * several tests to download documents from space
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class DownloadDocumentsRestTest {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
	}

	private static final String BASIC_URI = "/register/v1";

	private MockMvc mockMvc;

	@Autowired
	private RegisterControllerV1 restApi;

	@Before
	public final void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(restApi).build();
		restApi = Mockito.mock(RegisterControllerV1.class);
	}

	private final String[] createAndPushDocsToSpace(MockMultipartFile[] files) throws Exception {
		String spaceNames[] = new String[] { "wf" + System.nanoTime(), "rf" + System.nanoTime(), "rv1" };

		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + spaceNames[0])).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + spaceNames[0] + "/" + spaceNames[1])).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + spaceNames[0] + "/" + spaceNames[1] + "/" + spaceNames[2])).andExpect(MockMvcResultMatchers.status().isOk());

		MockMultipartHttpServletRequestBuilder restApi = MockMvcRequestBuilders.fileUpload(BASIC_URI + "/pushDocuments");
		for (MockMultipartFile uploadFile : files) {
			restApi.file(uploadFile);
		}
		// upload to
		restApi.param("spaceRef", spaceNames[0] + "/" + spaceNames[1] + "/" + spaceNames[2]);
		// send post request
		this.mockMvc.perform(restApi).andExpect(MockMvcResultMatchers.status().isOk());

		return spaceNames;
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

		String[] spaceRef = createAndPushDocsToSpace(files);
		ResultActions resultActions = performRequest(spaceRef, "/fetchDocuments");

		Path fsFetchFile = FileWalker.mkFile(Paths.get(System.getProperty("user.dir"), "target", "fetch", UUID.randomUUID().toString(), spaceRef[0] + ".zip"));
		resultActions.andExpect(MockMvcResultMatchers.status().isOk());
		MockHttpServletResponse response = resultActions.andReturn().getResponse();
		String fnName = ((String) response.getHeaderValue("Content-disposition")).split("filename=")[1];

		// compare name
		Assert.assertEquals(spaceRef[2] + ".zip", fnName);
		Files.write(fsFetchFile, response.getContentAsByteArray());
		// check if file was downloaded
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
		performRequest(new String[] { "w", "r", "b" }, "/fetchDocuments").andExpect(MockMvcResultMatchers.status().isNotFound());
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
		String[] spaceRef = createAndPushDocsToSpace(files);

		performRequest(spaceRef, "/fetchDocuments", "scope=single");
	}

	/**
	 * perform rest request
	 * 
	 * @param spaceRef
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	private final ResultActions performRequest(String[] spaceRef, String uri, String... params) throws Exception {

		MockHttpServletRequestBuilder restApi = MockMvcRequestBuilders.get(BASIC_URI + uri);
		// upload to
		restApi.param("spaceRef", spaceRef[0] + "/" + spaceRef[1] + "/" + spaceRef[2]);

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
