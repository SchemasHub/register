package schevo.client;

import java.nio.file.Path;

public abstract class PushDocumentFilter {

	public abstract Path getSource();

	public abstract boolean accept(Path path);
}
