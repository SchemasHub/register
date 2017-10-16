package schevo.server.api.v1;

import static schevo.UriConfigs.WORKSPACES_URI;

import java.security.SecureRandom;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import schevo.common.SpaceRef;

public abstract class BasicTestInfra {

	protected MockMvc mockMvc;

	public static final String genStr(int len) {
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(chars.charAt(rnd.nextInt(chars.length())));
		}
		return sb.toString();
	}

	/**
	 * just create new test space
	 * 
	 * @return
	 * @throws Exception
	 */
	public final SpaceRef createNewTestSpace() throws Exception {
		String workspace = "w" + System.nanoTime();
		String repository = "r" + System.nanoTime();
		String version = "1";
		// create new workspace
		mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + workspace)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository
		mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + workspace + "/" + repository)).andExpect(MockMvcResultMatchers.status().isOk());
		// create new repository version
		mockMvc.perform(MockMvcRequestBuilders.post(WORKSPACES_URI + "/" + workspace + "/" + repository + "/" + version)).andExpect(MockMvcResultMatchers.status().isOk());

		return new SpaceRef(workspace, repository, version);
		// return new String[] { wName, rName, rvName };
	}

}
