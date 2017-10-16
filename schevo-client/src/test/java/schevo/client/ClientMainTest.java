package schevo.client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class ClientMainTest {

	public static final void main(String[] arg) throws SchevoClientException {

		SchevoClient client = new SchevoClient("http://localhost:9999");

		List<String> workspaces = client.listWorkspaces();
		List<String> repositories = client.listRepositories("oracle");
		List<String> versions = client.listVersions("oracle", "union");
		versions.get(0);

		Space space = client.getSpace("oracle", "union", "v1");

		// oracle/union/v1
		// SpaceRef.get('oracle/union/v 1');

		space.fetch();

		// push new files
		space.push(new PushDocumentFilter() {

			@Override
			public final Path getSource() {
				return Paths.get("D:/Oracle/ws/union/KBP/kbp-common-mds/soa-infra/apps");
			}

			@Override
			public final boolean accept(Path path) {
				return true;
			}
		});

		space.fetch();

	}
}
