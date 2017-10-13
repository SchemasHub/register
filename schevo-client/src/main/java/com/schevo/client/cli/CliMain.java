package com.schevo.client.cli;

import java.util.Iterator;
import java.util.List;

import comschevo.client.cmd.BasicCmd;
import comschevo.client.cmd.Fetch;
import comschevo.client.cmd.Push;
import comschevo.client.cmd.Space;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

/**
 * 
 * @author tomecode.com
 * 
 * 
 * 
 *         ./schevo -sate
 * 
 *
 */
public final class CliMain {

	public static final void main(String[] args) {
		String[] argss = { "space", "-new", "w/r/rv" };

		// String[] argss = { "-url", "http://www.schevo.com/@workspace", "space",
		// "-new", "w/r/rv" };

		CommandLine commandLine = new CommandLine(new BasicCmd());
		commandLine.addSubcommand("space", new Space());
		commandLine.addSubcommand("push", new Push());
		commandLine.addSubcommand("fetch", new Fetch());

		List<CommandLine> commands = null;
		try {
			// commandLine.setUnmatchedArgumentsAllowed(true);
			commands = commandLine.parse(argss);
		} catch (UnmatchedArgumentException e) {
			commandLine.usage(System.out);
			System.exit(-1);
		} catch (MissingParameterException e) {
			commandLine.usage(System.out);
			System.exit(-1);
		}

		//
		// 1.get basic cmd and create execute context
		// 2.iterate over other commands
		//

		//
		// execute cmd
		//
		// iterate over commands
		Iterator<CommandLine> it = commands.iterator();
		while (it.hasNext()) {
			CommandLine nextCmd = it.next();
			if (nextCmd.getCommand() instanceof Space) {
				Space space = (Space) nextCmd.getCommand();
				if (!nextCmd.getUnmatchedArguments().isEmpty()) {
					nextCmd.usage(System.out);
					System.exit(-1);
				}
				if (space.newSpace) {
					doCreateNewSpace(space);
				}

				if (space.deleteSpace) {

				}
			}

		}

	}

	/**
	 * create new space
	 * 
	 * @param space
	 */
	private static void doCreateNewSpace(Space space) {
		System.out.println("new space");
	}

}
