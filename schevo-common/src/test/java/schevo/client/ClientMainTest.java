package schevo.client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import schevo.client.SchevoClient.PushFilter;

public final class ClientMainTest {

	public static final void main(String[] arg) throws SchevoClientException {

		SchevoClient client = new SchevoClient("http://localhost:9999");

		List<String> workspaces = client.listWorkspaces();
		List<String> repositories = client.listRepositories("oracle");
		List<String> versions = client.listVersions("oracle", "union");
		versions.get(0);

		schevo.client.SchevoClient.Space space = client.getSpace("oracle", "union", "v10");

		// oracle/union/v1
		// SpaceRef.get('oracle/union/v1');

		space.fetch();

		space.push(new PushFilter() {

			@Override
			public final Path getSource() {
				return Paths.get("D:/Oracle/ws/union/KBP/kbp-common-mds/soa-infra/apps");
			}

			@Override
			public final boolean accept(Path path) {
				return true;
			}
		});
	}
}
