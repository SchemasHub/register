package comschevo.client.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * cmd: push
 * 
 * @author tomecode.com
 *
 */
@Command(name = "push", description = "Push schemas to space")
public class Push {

	/**
	 * print help
	 */
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Prints this help message and exits")
	private boolean helpRequested;
}
