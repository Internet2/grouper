/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/SignetXml.java,v 1.7 2008-09-27 01:02:09 ddonn Exp $

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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.adapter.Util;

/**
 * SignetXml - A command-line utility for using the Signet XML API.
 */
public class SignetXml extends XmlUtil
{
	public static final String STDIN_FILENAME = "stdin";
	public static final String STDOUT_FILENAME = "stdout";


	/**
	 * Constructor
	 * @param command The command to execute (e.g. import, export)
	 * @param commandArgs Arguments and Filters for the command
	 * @param xmlFilename The filename for input or output
	 */
	public SignetXml(String command, Vector<CommandArg> commandArgs, String xmlFilename)
	{
		Signet signet = new Signet();
		signetXmlAdapter = new SignetXa(signet);

		if (command.equals(CommandArg.CMD_EXPORT))
			processExportCmd(commandArgs, signetXmlAdapter, xmlFilename);
		else if (command.equals(CommandArg.CMD_IMPORT))
			processImportCmd(commandArgs, signetXmlAdapter, xmlFilename);

	}

	/**
	 * Top-level method to process an export command
	 * @param commandArgs Arguments and Filters for the command
	 * @param signetXmlAdapter An instance of SignetXa
	 * @param xmlFilename  The filename for output
	 */
	public void processExportCmd(Vector<CommandArg> commandArgs, SignetXa signetXmlAdapter, String xmlFilename)
	{
		OutputStream outFile = setupOutfile(xmlFilename);

		for (CommandArg arg : commandArgs)
			buildXml(arg, signetXmlAdapter);

		signetXmlAdapter.getXmlSignet().setXmlCreateDate(Util.convertDateToString(new Date()));

		marshalXml(signetXmlAdapter.getXmlSignet(), outFile);

		try
		{
			outFile.flush();
			if ( !outFile.equals(System.out))
				outFile.close();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	/**
	 * Top-level method to process an export
	 * @param commandArgs Arguments and Filters for the command
	 * @param signetXmlAdapter An instance of SignetXa
	 * @param xmlFilename The filename for input
	 */
	public void processImportCmd(Vector<CommandArg> commandArgs, SignetXa signetXmlAdapter, String xmlFilename)
	{
		InputStream inFile = setupInfile(xmlFilename);

		XmlImporter importer = new XmlImporter(signet, inFile);
		for (CommandArg arg : commandArgs)
			importer.importXml(arg);

		try
		{
			if ( !inFile.equals(System.in))
				inFile.close();
		}
		catch (IOException e) { e.printStackTrace(); }

	}

	/**
	 * Create an output stream for the given filename
	 * @param xmlFilename The filename for output
	 * @return An OutputStream
	 */
	protected OutputStream setupOutfile(String xmlFilename)
	{
		OutputStream retval = null;

		if (null == xmlFilename ||
				(0 >= xmlFilename.length()) ||
				(STDOUT_FILENAME.equals(xmlFilename)))
		{
			retval = System.out;
// don't muddy the stdout output in case it's redirected to a file
//			log.info("Writing XML to " + STDOUT_FILENAME);
		}
		else
		{
			try
			{
				StringBuilder buf = new StringBuilder();
				File outFile = new File(xmlFilename);
				if (outFile.exists())
					buf.append("Overwriting");
				else
					buf.append("Creating");
				buf.append(" XML file " + outFile.getCanonicalPath());
				retval = new FileOutputStream(outFile);
				log.info(buf);
			}
			catch (FileNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
		}

		return (retval);
	}

	/**
	 * Create an input stream for the given filename
	 * @param xmlFilename The filename for input
	 * @return An InputStream
	 */
	public InputStream setupInfile(String xmlFilename)
	{
		InputStream retval = null;
		StringBuffer buf = new StringBuffer("Reading XML from ");

		if (null == xmlFilename ||
				(0 >= xmlFilename.length()) ||
				(STDIN_FILENAME.equals(xmlFilename)))
		{
			retval = System.in;
			buf.append(STDIN_FILENAME);
		}
		else
		{
			try
			{
				File inFile = new File(xmlFilename);
				if (inFile.exists())
				{
					buf.append(inFile.getCanonicalPath());
					retval = new FileInputStream(inFile);
				}
				else
					buf = new StringBuffer("XML file \"" + xmlFilename + "\" not found.");
			}
			catch (IOException e) { e.printStackTrace(); }
		}

		log.info(buf.toString());
		return (retval);
	}

	/**
	 * Create instances of the binder classes (*Xb.java) based on the given
	 * command and filters
	 * @param cmd The command args and filters
	 * @param signetXmlAdapter An instance of SignetXa
	 */
	public void buildXml(CommandArg cmd, SignetXa signetXmlAdapter)
	{
		if (cmd.getType().equals(CommandArg.EX_ASSGN))
			new AssignmentXml(signetXmlAdapter).buildXml(cmd);
		else if (cmd.getType().equals(CommandArg.EX_PERM))
			new PermissionXml(signetXmlAdapter).buildXml(cmd);
//		else if (cmd.getType().equals(CommandArg.EX_PROXY))
//			exportProxy(cmd);
		else if (cmd.getType().equals(CommandArg.EX_SCOPE))
			new ScopeTreeXml(signetXmlAdapter).buildXml(cmd);
		else if (cmd.getType().equals(CommandArg.EX_SUBJ))
			new SubjectXml(signetXmlAdapter).buildXml(cmd);
		else if (cmd.getType().equals(CommandArg.EX_SUBSYS))
			new SubsystemXml(signetXmlAdapter).buildXml(cmd);
		else
			log.error("Invalid command: " + cmd.toString());
	}



	/////////////////////////////////////
	// static members
	/////////////////////////////////////

	protected static CommandOptions		options;
	protected static String				command;
	protected static Vector<CommandArg>	commandArgs;
	protected static String				xmlFile;
	protected static String				version = "$Revision: 1.7 $";
	/** logging */
	private static Log					log = LogFactory.getLog(SignetXml.class);



	/////////////////////////////////////
	// static methods
	/////////////////////////////////////

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args)
	{
		log = LogFactory.getLog(SignetXml.class);
		if (parseArgs(args))
			new SignetXml(command, commandArgs, options.getXmlFilename());
	}


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
				retval = (null != (commandArgs = parseCommands(options.getCmdFilename())));
			else
				retval = (null != (commandArgs = parseCommands(args, options.getNextArg())));
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
			if (args[i].equals(CommandArg.VERSION_FLAG))
				retval.setShowVersion(true);

			else if (args[i].equals(CommandArg.HELP_1_FLAG) || args[i].equals(CommandArg.HELP_2_FLAG))
				retval.setShowHelp(true);

//			else if (args[i].equals(CommandArg.CMD_INFILE))
//			{
//				String[] cmds = XmlUtil.expandCommand(args[i], STDIN_FILENAME);
//				retval.setCmdFilename(cmds[i]);
//			}

			else if (args[i].equals(CommandArg.XML_FILE_FLAG))
				retval.setXmlFilename(args[++i]);

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
	 * @return A Vector of CommandArg objects
	 */
	protected static Vector<CommandArg> parseCommands(String[] args, int start)
	{
		if ((null == args) || (args.length <= start))
			return (null);

		Vector<CommandArg> retval = new Vector<CommandArg>();
		boolean done = false;
		int i = start;
		CommandArg tmpCmd;
		while ((i < args.length) && !done)
		{
			String tmpStr = args[i].toLowerCase().trim();
			if (tmpStr.startsWith(CommandArg.CMD_EXPORT) /* || tmpStr.startsWith(CommandArg.CMD_APPEND) */ )
			{
				command = CommandArg.CMD_EXPORT;
				tmpCmd = parseExportCmd(args, i);
				if (null != tmpCmd)
					retval.add(tmpCmd);
			}
			else if (tmpStr.startsWith(CommandArg.CMD_IMPORT))
			{
				command = CommandArg.CMD_IMPORT;
				tmpCmd = parseImportCmd(args, i);
				if (null != tmpCmd)
					retval.add(tmpCmd);
			}
			else
				log.error("Unknown or invalid CommandArg: " + args[i]);

			done = true;
		}

		return (retval);
	}

	/**
	 * Parse the Options from a command file
	 * @param cmdFilename The file containing the commands
	 * @return A Vector of CommandArg objects
	 */
	protected static Vector<CommandArg> parseCommands(String cmdFilename)
	{
		Vector<CommandArg> retval = new Vector();

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
			log.error("CommandArg file \"" + cmdFilename + "\" not found.");
		}
		catch (IOException e) { e.printStackTrace(); }

		return (retval);
	}

	/**
	 * Parse an Export command
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @return A CommandArg object with the export details
	 */
	protected static CommandArg parseExportCmd(String[] args, int start)
	{
		CommandArg tmpCmd = new CommandArg();
		int i = start + 1;
		boolean done = false;
		while ((i < args.length) && !done)
		{
//        assignment | permission | proxy | scopeTree | subject | subsystem
			String arg = args[i].toLowerCase().trim();
			if (arg.equalsIgnoreCase(CommandArg.EX_ASSGN) ||
					arg.equalsIgnoreCase(CommandArg.EX_PERM) ||
//					arg.equalsIgnoreCase(CommandArg.EX_PROXY) ||
					arg.equalsIgnoreCase(CommandArg.EX_SCOPE) ||
					arg.equalsIgnoreCase(CommandArg.EX_SUBJ) ||
					arg.equalsIgnoreCase(CommandArg.EX_SUBSYS))
			{
				tmpCmd.setType(args[i]);
				parseParams(args, i, tmpCmd);
				i = tmpCmd.getNextArg();
			}
			else
			{
				log.error("SignetXml.parseExportCmd: Invalid argument found for Export command: \"" + arg + "\".");
				done = true;
			}
		}

		return (tmpCmd);
	}

	/**
	 * Parse an Export command's parameters
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @param cmd The CommandArg object to be filled in
	 */
	protected static void parseParams(String[] args, int start, CommandArg cmd)
	{
		int i = start + 1;
		while (i < args.length)
		{
			String arg = args[i].toLowerCase().trim();
			if (arg.startsWith(CommandArg.PARAM_FLAG))
			{
				if (args.length > (i + 1))
					cmd.getParams().put(arg, args[++i]);
				else
					log.error("Missing value for command filter \"" + arg + "\". Filter ignored.");
				i++;
			}
		}

		cmd.setNextArg(i);
	}

	/**
	 * Parse an Import command
	 * @param args Raw command-line
	 * @param start The starting position within the args array
	 * @return A CommandArg object with the import details
	 */
	protected static CommandArg parseImportCmd(String[] args, int start)
	{
		CommandArg tmpCmd = new CommandArg();
		int i = start + 1;
//        add | update | deactivate | delete
		if (args[i].equals(CommandArg.IM_ADD) ||
				args[i].equals(CommandArg.IM_DEACT) ||
				args[i].equals(CommandArg.IM_DEL) ||
				args[i].equals(CommandArg.IM_UPD))
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
			BufferedReader br = new BufferedReader(new FileReader(CommandArg.HELP_FILE));
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
