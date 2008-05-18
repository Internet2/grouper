/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/SignetXml.java,v 1.4 2008-05-18 23:05:22 ddonn Exp $

Copyright (c) 2007 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet.util.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.Signet;

/**
 * SignetXml - A command-line utility for using the Signet XML API.
 */
public class SignetXml
{
	public static final String STDIN_FILENAME = "stdin";
	public static final String STDOUT_FILENAME = "stdout";

	protected Log		log;
	protected Signet	signet;


	public SignetXml(Vector<Command> commands)
	{
		log = LogFactory.getLog(SignetXml.class);
		signet = new Signet();
		for (Command cmd : commands)
			processCommand(cmd);
	}

	public void processCommand(Command cmd)
	{
		if (cmd.getCommand().equals(Command.CMD_EXPORT) ||
			cmd.getCommand().equals(Command.CMD_APPEND))
		{
			if (cmd.getFilename().equals(STDOUT_FILENAME))
				cmd.setOutFile(System.out);
			else
				setupOutfile(cmd);

			exportXml(cmd);

			try
			{
				cmd.getOutFile().flush();
				if ( !cmd.getOutFile().equals(System.out))
					cmd.getOutFile().close();
			}
			catch (IOException e) { e.printStackTrace(); }
		}

		else if (cmd.getCommand().equals(Command.CMD_IMPORT))
		{
			if (cmd.getFilename().equals(STDIN_FILENAME))
				cmd.setInFile(System.in);
			else
			{
				File infile = new File(cmd.getFilename());
				try { cmd.setInFile(new FileInputStream(infile)); }
				catch (FileNotFoundException e)
				{
					log.error("Input XML file \"" + cmd.getFilename() + "\" not found.");
				}
			}

			log.info("Reading XML file \"" + cmd.getFilename() + "\"");

			new XmlImporter(signet, log).importXml(cmd);

			try
			{
				if ( !cmd.getInFile().equals(System.in))
					cmd.getInFile().close();
			}
			catch (IOException e) { e.printStackTrace(); }
		}

		else
			log.error("Unknown command: " + cmd.getCommand());
	}

	protected void setupOutfile(Command cmd)
	{
		try
		{
			File outFile = new File(cmd.getFilename());
			StringBuffer buf = new StringBuffer();
			if (outFile.exists())
			{
				if (cmd.getCommand().equals(Command.CMD_EXPORT))
					buf.append("Overwriting");
				else
					buf.append("Appending to");
			}
			else
				buf.append("Creating");
			buf.append(" XML file " + outFile.getCanonicalPath());
			log.info(buf.toString());
			cmd.setOutFile(new FileOutputStream(outFile,
							cmd.getCommand().equals(Command.CMD_APPEND)));
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }

	}

	public void exportXml(Command cmd)
	{
		if (cmd.getType().equals(Command.EX_ASSGN))
			new AssignmentXml(signet).exportAssignment(cmd);
		else if (cmd.getType().equals(Command.EX_PERM))
			new PermissionXml(signet).exportPermission(cmd);
//		else if (cmd.getType().equals(Command.EX_PROXY))
//			exportProxy(cmd);
		else if (cmd.getType().equals(Command.EX_SCOPE))
			new ScopeTreeXml(signet).exportScopeTree(cmd);
		else if (cmd.getType().equals(Command.EX_SUBJ))
			new SubjectXml(signet).exportSubject(cmd);
		else if (cmd.getType().equals(Command.EX_SUBSYS))
			new SubsystemXml(signet).exportSubsystem(cmd);
		else
			log.error("Invalid command: " + cmd.toString());
	}



	/////////////////////////////////////
	// static members
	/////////////////////////////////////

	protected static CommandOptions		options;
	protected static Vector<Command>	commands;
	protected static String				version = "$Revision: 1.4 $";
	protected static Log				mainLog;


	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args)
	{
		mainLog = LogFactory.getLog(SignetXml.class);
		if (parseArgs(args))
			new SignetXml(commands);
	}


	/////////////////////////////////////
	// static methods
	/////////////////////////////////////

	/**
	 * Top-level command-line parsing
	 * @param args The raw command-line arguments
	 * @return   True on success, false otherwise
	 */
	protected static boolean parseArgs(String[] args)
	{
		boolean retval = false; // assume failure

		if (retval = (null != (options = parseOptions(args))))
		{
			if (options.showHelp())
			{
				showUsage();
				retval = false;
			}
			else if (options.showVersion())
			{
				showVersion();
				retval = false;
			}
			else if (null != options.getCmdFilename())
				retval = (null != (commands = parseCommands(options.getCmdFilename())));
			else
				retval = (null != (commands = parseCommands(args, options.getNextArg())));
		}
		
		return (retval);
	}

	/**
	 * Parse the Options from the command-line
	 * @param args Raw command-line
	 * @return Parsed options
	 */
	protected static CommandOptions parseOptions(String[] args)
	{
		CommandOptions retval = new CommandOptions();
		if (null == args)
			return (retval);

		boolean done = false;
		for (int i = 0; i < args.length && !done; i++)
		{
			if (args[i].equals(Command.VERSION_FLAG))
				retval.setShowVersion(true);

			else if (args[i].equals(Command.HELP_1_FLAG) || args[i].equals(Command.HELP_2_FLAG))
				retval.setShowHelp(true);

			else if (args[i].equals(Command.CMD_INFILE))
			{
				String[] cmds = XmlUtil.expandCommand(args[i], STDIN_FILENAME);
				retval.setCmdFilename(cmds[i]);
			}
				
			else
			{
				retval.setNextArg(i);
				done = true;
			}
		}

		if ( !done)
			retval.setNextArg(0);

		return (retval);
	}


	/**
	 * Parse the Commands from the command-line
	 * @param args Raw command-line
	 * @return A Vector of Command objects
	 */
	protected static Vector<Command> parseCommands(String[] args, int start)
	{
		Vector<Command> retval = null;

		if ((null == args) || (args.length <= start))
			return (retval);

		retval = new Vector<Command>();
		boolean done = false;
		int i = start;
		Command tmpCmd;
		while ((i < args.length) && !done)
		{
			String tmpStr = args[i].toLowerCase();
			if (tmpStr.startsWith(Command.CMD_EXPORT) ||
					tmpStr.startsWith(Command.CMD_APPEND))
			{
				tmpCmd = parseExportCmd(args, i);
				if (null != tmpCmd)
					retval.add(tmpCmd);
			}
			else if (tmpStr.startsWith(Command.CMD_IMPORT))
			{
				tmpCmd = parseImportCmd(args, i);
				if (null != tmpCmd)
					retval.add(tmpCmd);
			}
			else
				mainLog.error("Unknown or invalid Command: " + args[i]);

			done = true;
		}

		return (retval);
	}

	/**
	 * Parse the Options from a command file
	 * @param cmdFilename The file containing the commands
	 * @return A Vector of Command objects
	 */
	protected static Vector<Command> parseCommands(String cmdFilename)
	{
		Vector<Command> retval = new Vector();

		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(cmdFilename));
			String buf;
			while (null != (buf = reader.readLine()))
			{
				String[] args = buf.split(" ");
				retval.addAll(parseCommands(args, 0));
			}
		}
		catch (FileNotFoundException e)
		{
			mainLog.error("Command file \"" + cmdFilename + "\" not found.");
		}
		catch (IOException e) { e.printStackTrace(); }

		return (retval);
	}

	/**
	 * Parse an Export command
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @return A Command object with the export details
	 */
	protected static Command parseExportCmd(String[] args, int start)
	{
		Command tmpCmd = new Command();
		String[] expandedCmd = XmlUtil.expandCommand(args[start], STDOUT_FILENAME);
		tmpCmd.setCommand(expandedCmd[0]);
		tmpCmd.setFilename(expandedCmd[1]);
//		tmpCmd.setCommand(args[start].trim().toLowerCase());
		int i = start + 1;
		boolean done = false;
		while ((i < args.length) && !done)
		{
//        assignment | permission | proxy | scopeTree | subject | subsystem
			if (args[i].equals(Command.EX_ASSGN) ||
					args[i].equals(Command.EX_PERM) ||
					args[i].equals(Command.EX_PROXY) ||
					args[i].equals(Command.EX_SCOPE) ||
					args[i].equals(Command.EX_SUBJ) ||
					args[i].equals(Command.EX_SUBSYS))
			{
				tmpCmd.setType(args[i]);
				parseParams(args, i, tmpCmd);
				i = tmpCmd.getNextArg();
			}
			else
			{
				System.out.println("SignetXml.parseExportCmd: Invalid argument found for Export command - \"" + args[i] + "\"");
				done = true;
			}
		}

		return (tmpCmd);
	}

	/**
	 * Parse an Export command's parameters
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @param cmd The Command object to be filled in
	 * @return A Command object with the export details
	 */
	protected static void parseParams(String[] args, int start, Command cmd)
	{
		int i = start + 1;
		boolean done = false;
		while ((i < args.length) && !done)
		{
			String[] vals = args[i].split(Command.EQUALS);
			if ((null != vals) && (2 == vals.length))
			{
				cmd.getParams().put(vals[0], vals[1]);
				i++;
			}
			else
				done = true;
		}

		cmd.setNextArg(i);
	}

	/**
	 * Parse an Import command
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @return A Command object with the import details
	 */
	protected static Command parseImportCmd(String[] args, int start)
	{
		Command tmpCmd = new Command();
		String[] expandedCmd = XmlUtil.expandCommand(args[start], STDIN_FILENAME);
		tmpCmd.setCommand(expandedCmd[0]);
		tmpCmd.setFilename(expandedCmd[1]);
//		tmpCmd.setCommand(args[start].trim().toLowerCase());
		int i = start + 1;
//        add | update | deactivate | delete
		if (args[i].equals(Command.IM_ADD) ||
				args[i].equals(Command.IM_DEACT) ||
				args[i].equals(Command.IM_DEL) ||
				args[i].equals(Command.IM_UPD))
		{
			tmpCmd.setType(args[i]);
			tmpCmd.setNextArg(++i);
		}

		return (tmpCmd);
	}

	/**
	 * Display on-screen help
	 */
	protected static void showUsage()
	{
		showVersion();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(Command.HELP_FILE));
			String str;
			while (null != (str = br.readLine()))
				System.out.println(str);
			br.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Display version info
	 */
	protected static void showVersion()
	{
		System.out.println(SignetXml.class.getSimpleName() + ": " + version);
		System.out.println("Signet: " + Signet.getVersion());
	}

}
