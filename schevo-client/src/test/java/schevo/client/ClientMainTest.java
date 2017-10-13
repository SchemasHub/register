package schevo.client;

import java.util.List;

public final class ClientMainTest {

	public static final void main(String[] arg) throws SchevoClientException {

		SchevoClient client = new SchevoClient("http://localhost:9999");

		List<String> workspaces = client.listWorkspaces();
		List<String> repositories = client.listRepositories("oracle");
		List<String> versions = client.listVersions("oracle", "union");
		versions.get(0);

		schevo.client.SchevoClient.Space space = client.getSpace("oracle", "union", "v10");

		space.fetch();

		
	}
}
