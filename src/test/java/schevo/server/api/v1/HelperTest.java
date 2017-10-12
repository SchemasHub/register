package schevo.server.api.v1;

import java.nio.file.Paths;
import java.util.UUID;

public abstract class HelperTest {

	static {
		System.setProperty("spaces.dir", Paths.get(System.getProperty("user.dir"), "target", "restApi" + UUID.randomUUID().toString()).toString());
	}

}
