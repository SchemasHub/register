package schevo.server.api.v1;

import java.nio.file.Paths;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.server.SchevoServer;
import schevo.server.api.v1.RegisterController;

/**
 * several tests to get information about spaces
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class GetSpaceInfoRestTest {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
	}

	private static final String BASIC_URI = "/register/v1";

	private MockMvc mockMvc;

	@Autowired
	private RegisterController restApi;

	private String[] spaceRef;

	@Before
	public final void setup() throws Exception {
		this.mockMvc = MockMvcBuilders.standaloneSetup(restApi).build();
		restApi = Mockito.mock(RegisterController.class);
		spaceRef = createTestSpace();
	}

	private final String[] createTestSpace() throws Exception {
		String wName = "w" + System.nanoTime();
		String rName = "r" + System.nanoTime();
		String rvName = "vr";
		// create new workspace
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		this.mockMvc.perform(MockMvcRequestBuilders.post(BASIC_URI + "/spaces/" + wName + "/" + rName + "/" + rvName)).andExpect(MockMvcResultMatchers.status().isOk());

		return new String[] { wName, rName, rvName };
	}

	@Test
	public final void testWorkspace() throws Exception {

		ResultActions ra = this.mockMvc.perform(MockMvcRequestBuilders.get(BASIC_URI + "/spaces/" + spaceRef[0])).andExpect(MockMvcResultMatchers.status().isOk());
		ra.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		String body = ra.andReturn().getResponse().getContentAsString();
		body.toCharArray();
	}

}
