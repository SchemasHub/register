package comschevo.client.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * 
 * @author tomecode.com
 *
 */
@Command(synopsisHeading = "blabla", name = "space", description = "Manage spaces")
public final class Space {

	@Option(names = { "-new" }, description = "Create a new space")
	public boolean newSpace;

	@Option(names = { "-delete" }, description = "Delete space")
	public boolean deleteSpace;

	@Parameters(arity = "1", description = "Path(s) to space(s) is in format: <workspace>/<repository>/<repository version>")
	public String[] spacePaths;

}
