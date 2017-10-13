package comschevo.client.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * cmd: fetch
 * 
 * @author tomecode.com
 *
 */
@Command(name = "fetch", description = "Fetch schemas from space")
public class Fetch {

	/**
	 * print help
	 */
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Prints this help message and exits")
	private boolean helpRequested;
}
