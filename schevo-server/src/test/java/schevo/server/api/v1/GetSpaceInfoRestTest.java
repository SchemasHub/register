package schevo.server.api.v1;

import static schevo.UriConfigs.WORKSPACES_URI;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import schevo.common.SpaceRef;
import schevo.server.SchevoServer;
import schevo.server.api.SpacesControllerV1;

/**
 * several tests to get information about spaces
 * 
 * @author tomecode.com
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SchevoServer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class GetSpaceInfoRestTest extends BasicTestInfra {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "spacesApi" + UUID.randomUUID().toString()).toString());
	}

	@Autowired
	private SpacesControllerV1 restSpaces;

	private SpaceRef spaceRef;

	@Before
	public final void setup() throws Exception {
		this.restSpaces = Mockito.mock(SpacesControllerV1.class);
		this.mockMvc = MockMvcBuilders.standaloneSetup(restSpaces).build();
		this.spaceRef = createNewTestSpace();
	}

	@Test
	public final void testWorkspace() throws Exception {
		ResultActions ra = this.mockMvc.perform(MockMvcRequestBuilders.get(WORKSPACES_URI + "/" + spaceRef.getWorkspace())).andExpect(MockMvcResultMatchers.status().isOk());
		ra.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8));
		String body = ra.andReturn().getResponse().getContentAsString();
		body.toCharArray();
	}

}
