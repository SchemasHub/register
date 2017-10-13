package comschevo.client.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * command: space
 * 
 * @author tomecode.com
 *
 */
@Command( name = "space", description = "Schevo is a schema evolution " + "system with an unusually rich command set that provides both " + "high-level operations and full access to internals.", commandListHeading = "%nCommands:%n%nThe most commonly used commands are:%n")
public final class BasicCmd {

	/**
	 * print help
	 */
	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Prints this help message and exits")
	private boolean helpRequested;

	/**
	 * create new workspace
	 */
	@Option(names = { "-v", "--verbose" }, description = "Be verbose.")
	public String verbose;
	@Option(names = { "-url" }, description = "Set the server url.")
	public String url;
}
