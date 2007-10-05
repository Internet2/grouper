/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/SignetXml.java,v 1.1 2007-10-05 08:40:13 ddonn Exp $

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
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import edu.internet2.middleware.signet.AssignmentImpl;
import edu.internet2.middleware.signet.ProxyImpl;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.SubsystemImpl;
import edu.internet2.middleware.signet.TreeImpl;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.AssignmentSetXa;
import edu.internet2.middleware.signet.util.xml.adapter.ProxyImplXa;
import edu.internet2.middleware.signet.util.xml.adapter.ScopeTreeXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetSubjectXa;
import edu.internet2.middleware.signet.util.xml.adapter.SignetXa;
import edu.internet2.middleware.signet.util.xml.adapter.SubsystemImplXa;
import edu.internet2.middleware.signet.util.xml.binder.AssignmentImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ObjectFactory;
import edu.internet2.middleware.signet.util.xml.binder.ProxyImplXb;
import edu.internet2.middleware.signet.util.xml.binder.ScopeTreeXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetSubjectXb;
import edu.internet2.middleware.signet.util.xml.binder.SignetXb;
import edu.internet2.middleware.signet.util.xml.binder.SubsystemImplXb;

/**
 * SignetXml - A command-line utility for using the Signet XML API.
 */
public class SignetXml
{
	public static final String STDIN_FILENAME = "stdin";
	public static final String STDOUT_FILENAME = "stdout";
	public static final String JAXB_CONTEXT_PATH = "edu.internet2.middleware.signet.util.xml.binder";

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
		if (cmd.getCommand().equals(CMD_EXPORT) ||
			cmd.getCommand().equals(CMD_APPEND))
		{
			try
			{
				if (cmd.getFilename().equals(STDOUT_FILENAME))
					cmd.setOutFile(System.out);
				else
				{
					File outFile = new File(cmd.getFilename());
					StringBuffer buf = new StringBuffer();
					if (outFile.exists())
					{
						if (cmd.getCommand().equals(CMD_EXPORT))
							buf.append("Overwriting");
						else
							buf.append("Appending to");
					}
					else
						buf.append("Creating");
					buf.append(" XML file " + outFile.getCanonicalPath());
					log.info(buf.toString());
					cmd.setOutFile(new FileOutputStream(outFile,
									cmd.getCommand().equals(CMD_APPEND)));
				}
				exportXml(cmd);
				cmd.getOutFile().flush();
				if ( !cmd.getOutFile().equals(System.out))
					cmd.getOutFile().close();
			}
			catch (FileNotFoundException e) { e.printStackTrace(); }
			catch (IOException e) { e.printStackTrace(); }
		}

		else if (cmd.getCommand().equals(CMD_IMPORT))
		{
			if (cmd.getFilename().equals(STDIN_FILENAME))
				cmd.setInFile(System.in);
			else
			{
				try
				{
					File infile = new File(cmd.getFilename());
					cmd.setInFile(new FileInputStream(infile));
					log.info("Reading XML file \"" + cmd.getFilename() + "\"");

					importXml(cmd);

					if ( !cmd.getInFile().equals(System.in))
						cmd.getInFile().close();
				}
				catch (FileNotFoundException e)
				{
					log.error("Input XML file \"" + cmd.getFilename() + "\" not found.");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		else
			log.error("Unknown command: " + cmd.getCommand());
	}

	public void exportXml(Command cmd)
	{
		if (cmd.getType().equals(EX_ASSGN))
			exportAssignment(cmd);
		else if (cmd.getType().equals(EX_PERM))
			log.warn("That Type is not supported yet: " + cmd.toString());
		else if (cmd.getType().equals(EX_PROXY))
			log.warn("That Type is not supported yet: " + cmd.toString());
		else if (cmd.getType().equals(EX_SCOPE))
			exportScopeTree(cmd);
		else if (cmd.getType().equals(EX_SUBJ))
			log.warn("That Type is not supported yet: " + cmd.toString());
		else if (cmd.getType().equals(EX_SUBSYS))
			log.warn("That Type is not supported yet: " + cmd.toString());
		else
			log.error("Invalid Type in command: " + cmd.toString());
	}

	protected void exportAssignment(Command cmd)
	{
		Status status = null;
		String[] subjId = null;
		String[] functionId = null;
		String[] scopeId = null;

		Hashtable<String, String> params = cmd.getParams();
		for (String key : params.keySet())
		{
			if (key.equalsIgnoreCase(PARAM_SUBJID))
				subjId = parseList(params.get(key));
			else if (key.equalsIgnoreCase(PARAM_STATUS))
				status = (Status)Status.getInstanceByName(params.get(key));
			else if (key.equalsIgnoreCase(PARAM_FUNCID))
				functionId = parseList(params.get(key));
			else if (key.equalsIgnoreCase(PARAM_SCOPEID))
				scopeId = parseList(params.get(key));
			else
				log.error("Invalid Parameter (" + key + ") in command - " + cmd.toString());
		}

		SignetXa adapter = new SignetXa(signet);
		SignetXb xml = adapter.getXmlSignet();
		List<AssignmentImplXb> assignList = xml.getAssignment();
		if ((null != subjId) && (0 < subjId.length))
		{
			for (int i = 0; i < subjId.length; i++)
			{
				SignetSubject subj = signet.getSubjectByIdentifier(subjId[i]);
				if (null != subj)
				{
					Set<AssignmentImpl> assigns;
					if (null != status)
						assigns = subj.getAssignmentsReceived(status.getName());
					else
						assigns = subj.getAssignmentsReceived();
					AssignmentSetXa set = new AssignmentSetXa(assigns, signet);
					assignList.addAll(set.getXmlAssignments().getAssignments());
				}
				else
					log.error("No Subject found with ID=" + subjId[i] + " during export");
			}
			marshalXml(xml, cmd.getOutFile());
		}
		else if ((null != functionId) && (0 < functionId.length))
		{
//TODO export assignments by functionId
		}
		else if ((null != scopeId) && (0 < scopeId.length))
		{
//TODO export assignments by scopeId
		}
	}

	protected void exportScopeTree(Command cmd)
	{
//TODO export ScopeTree
	}

	protected void marshalXml(SignetXb signetXb, OutputStream fos)
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			ObjectFactory factory = new ObjectFactory();
			JAXBElement<SignetXb> element = factory.createSignetRoot(signetXb);
			marshaller.marshal(element, fos);
			fos.flush();
		}
		catch (JAXBException ejax) { ejax.printStackTrace(); }
		catch (FileNotFoundException efnf) { efnf.printStackTrace(); }
		catch (IOException eio) { eio.printStackTrace(); }
	}


	public void importXml(Command cmd)
	{
		SignetXb signetXml = unmarshalSignet(cmd);
		if (null == signetXml)
			return;

		String cmdType = cmd.getType();
		if (cmdType.equals(IM_ADD) || cmdType.equals(IM_UPD))
		{
			updateXml(signetXml);
		}
		else if (cmdType.equals(IM_DEACT))
		{
log.info("Import+deactivate not supported yet");
		}
		else if (cmdType.equals(IM_DEL))
		{
log.info("Import+delete not supported yet");
		}
		else
			log.error("Unknown Command Type: \"" + cmdType + "\"");
	}

	protected SignetXb unmarshalSignet(Command cmd)
	{
		SignetXb signetXml = null;

		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			signetXml =((JAXBElement<SignetXb>)unmarshaller.unmarshal(cmd.getInFile())).getValue();
		}
		catch (JAXBException ejax)
		{
			ejax.printStackTrace();
		}

		return (signetXml);
	}

	protected void updateXml(SignetXb signetXml)
	{
//		updateScopeTrees(signetXml.getScopeTree());
//		updateSubsystems(signetXml.getSubsystems());
//		updateSubjects(signetXml.getSubject());
//		updateProxies(signetXml.getProxy());
		updateAssignments(signetXml.getAssignment());
	}

	protected void updateScopeTrees(List<ScopeTreeXb> scopeTrees)
	{
		if (null == scopeTrees)
			return;

		List<TreeImpl> signetTrees = new Vector<TreeImpl>();
		for (ScopeTreeXb xmlTree : scopeTrees)
		{
			TreeImpl signetTree = new ScopeTreeXa(xmlTree, signet).getSignetScopeTree();
			signetTrees.add(signetTree);
System.out.println("SignetXml.updateScopeTrees: ScopeTree=" + signetTree);
		}

		updateSignetObjects(signetTrees);
	}

	protected void updateSubsystems(List<SubsystemImplXb> subsystems)
	{
		if (null == subsystems)
			return;

		List<SubsystemImpl> signetSubsystems = new Vector<SubsystemImpl>();
		for (SubsystemImplXb xmlSubsys : subsystems)
		{
			SubsystemImpl sigSubsys = new SubsystemImplXa(xmlSubsys, signet).getSignetSubsystem();
			signetSubsystems.add(sigSubsys);
System.out.println("SignetXml.updateSubsystems: Subsystem=" + sigSubsys);
		}

		updateSignetObjects(signetSubsystems);
	}

	protected void updateSubjects(List<SignetSubjectXb> subjects)
	{
		if (null == subjects)
			return;

		List<SignetSubject> sigSubjects = new Vector<SignetSubject>();
		for (SignetSubjectXb xmlSubject : subjects)
		{
			SignetSubject sigSubject = new SignetSubjectXa(xmlSubject, signet).getSignetSubject();
			sigSubjects.add(sigSubject);
System.out.println("SignetXml.updateSubjects: Subject=" + sigSubject);
		}

		updateSignetObjects(sigSubjects);
	}

	protected void updateProxies(List<ProxyImplXb> proxies)
	{
		if (null == proxies)
			return;

		List<ProxyImpl> sigProxies = new Vector<ProxyImpl>();
		for (ProxyImplXb xmlProxy : proxies)
		{
			ProxyImpl sigProxy = new ProxyImplXa(xmlProxy, signet).getSignetProxy();
			sigProxies.add(sigProxy);
System.out.println("SignetXml.updateProxies: Proxy=" + sigProxy);
		}

		updateSignetObjects(sigProxies);
	}

	protected void updateAssignments(List<AssignmentImplXb> assigns)
	{
		if (null == assigns)
			return;

		List<AssignmentImpl> signetAssigns = new Vector<AssignmentImpl>();
		for (AssignmentImplXb xmlAssign : assigns)
		{
			AssignmentImpl signetAssign = 
				new AssignmentImplXa(xmlAssign, signet).getSignetAssignment();
			signetAssigns.add(signetAssign);
System.out.println("SignetXml.importAssignments: assignment=" + signetAssign);
		}

		updateSignetObjects(signetAssigns);
	}

	protected void updateSignetObjects(List signetObjects)
	{
		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();
		hibr.save(hs, signetObjects);
		hibr.closeSession(hs);
	}

//	/**
//	 * @param filename
//	 */
//	public void importXml(String filename)
//	{
//		try
//		{
//			JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_PATH);
//			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
//
//System.out.println("JaxbTestMail.importXml: importing file \"" + filename + "\"");
//			File xmlDocument = null;
//			if ( !filename.equals(STDIN_FILENAME))
//				xmlDocument = new File(filename);
//
//			InputStream fis;
//			if (null != xmlDocument)
//				fis = new FileInputStream(xmlDocument);
//			else
//				fis = System.in;
//
//			SignetXb signetXml =
//				((JAXBElement<SignetXb>)unmarshaller.unmarshal(fis)).getValue();
//System.out.println("SignetXml.importXml: SignetXml version=" + signetXml.getVersion());
//			Signet signet = new SignetXa(signetXml).getSignet();
//
//			if (0 < signetXml.getSubject().size())
//				importSubjects(signetXml, signet);
//
//			if (0 < signetXml.getAssignment().size())
//				importAssignments(signetXml, signet);
//
//			if (0 < signetXml.getProxy().size())
//				importProxies(signetXml);
//
//			if (null != xmlDocument)
//				fis.close();
//		}
//		catch (JAXBException ejax)
//		{
//			ejax.printStackTrace();
//		}
//		catch (FileNotFoundException efnf)
//		{
//			efnf.printStackTrace();
//		}
//		catch (IOException eio)
//		{
//			eio.printStackTrace();
//		}
//
//	}


	/**
	 * @param signetXml
	 * @param signet
	 */
	public void importSubjects(SignetXb signetXml, Signet signet)
	{
		for (SignetSubjectXb xmlSubj : signetXml.getSubject())
		{
			SignetSubjectXa adapter = new SignetSubjectXa(xmlSubj, signet);
System.out.println("SignetXml.importSubjects: subject=" + adapter.getSignetSubject().toString());
System.out.println("  importSubjects not implemented yet");
		}
	}


	/**
	 * @param signetXml
	 * @param signet
	 */
	public void importAssignments(SignetXb signetXml, Signet signet)
	{
		List<AssignmentImpl> signetAssignments = new Vector<AssignmentImpl>();

		for (AssignmentImplXb xmlAssign : signetXml.getAssignment())
		{
			AssignmentImpl signetAssignment = 
				new AssignmentImplXa(xmlAssign, signet).getSignetAssignment();
			signetAssignments.add(signetAssignment);
System.out.println("SignetXml.importAssignments: assignment=" + signetAssignment);
		}

		HibernateDB hibr = signet.getPersistentDB();
		Session hs = hibr.openSession();
		hibr.save(hs, signetAssignments);
		hibr.closeSession(hs);
	}


	/**
	 * @param signetXml
	 */
	public void importProxies(SignetXb signetXml)
	{
System.out.println("SignetXml.importProxies: not implemented yet.");
//TODO Import proxies
//		for (ProxyImplXb xmlProxy : signetXml.getProxy())
//		{
//			ProxyImplXa adapter = new ProxyImplXa(xmlProxy);
//System.out.println("SignetXml.importProxies: proxy=" + adapter.getSignetProxy());
//		}
	}


	//////////////////////////////
	// utility methods
	//////////////////////////////

	/** parse a comma-separated list into an String array */
	protected String[] parseList(String str)
	{
		String[] retval;

		if (null != str)
			retval = str.split(",");
		else
			retval = new String[0];

		for (int i = 0; i < retval.length; i++)
			retval[i] = retval[i].trim();

		return (retval);
	}


	////////////////////////////////
	// statics
	////////////////////////////////

	protected static final String CMD_EXPORT	= "export";
	protected static final String CMD_APPEND	= "append";
	protected static final String CMD_IMPORT	= "import";
	protected static final String EQUALS		= "="; // a bit overboard, I know
	protected static final String VERSION_FLAG	= "-v";
	protected static final String HELP_1_FLAG	= "-h";
	protected static final String HELP_2_FLAG	= "-?";
	protected static final String CMD_INFILE	= "-c";

// export commands: assignment | permission | proxy | scopeTree | subject | subsystem
	protected static final String EX_ASSGN		= "assignment";
	protected static final String EX_PERM		= "permission";
	protected static final String EX_PROXY		= "proxy";
	protected static final String EX_SCOPE		= "scopeTree";
	protected static final String EX_SUBJ		= "subject";
	protected static final String EX_SUBSYS		= "subsystem";
// import commands: add | update | deactivate | delete
	protected static final String IM_ADD		= "add";
	protected static final String IM_UPD		= "update";
	protected static final String IM_DEACT		= "deactivate";
	protected static final String IM_DEL		= "delete";

	protected static final String PARAM_SUBJID	= "subjId";
	protected static final String PARAM_SCOPEID	= "scopeId";
	protected static final String PARAM_STATUS	= "status";
	protected static final String PARAM_FUNCID	= "functionId";

	protected static final String HELP_FILE = "commandHelp.txt";

	protected static CommandOptions		options;
	protected static Vector<Command>	commands;
	protected static String				version = "$Revision: 1.1 $";
	protected static Log				mainLog;


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		mainLog = LogFactory.getLog(SignetXml.class);
		if (parseArgs(args))
			new SignetXml(commands);
	}


	/**
	 * @param args
	 * @return
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
			if (args[i].equals(VERSION_FLAG))
				retval.setShowVersion(true);

			else if (args[i].equals(HELP_1_FLAG) || args[i].equals(HELP_2_FLAG))
				retval.setShowHelp(true);

			else if (args[i].equals(CMD_INFILE))
			{
				String[] cmds = expandCommand(args[i], STDIN_FILENAME);
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
			if (tmpStr.startsWith(CMD_EXPORT) ||
					tmpStr.startsWith(CMD_APPEND))
			{
				tmpCmd = parseExportCmd(args, i);
				if (null != tmpCmd)
					retval.add(tmpCmd);
			}
			else if (tmpStr.startsWith(CMD_IMPORT))
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

	protected static Command parseExportCmd(String[] args, int start)
	{
		Command tmpCmd = new Command();
		String[] expandedCmd = expandCommand(args[start], STDOUT_FILENAME);
		tmpCmd.setCommand(expandedCmd[0]);
		tmpCmd.setFilename(expandedCmd[1]);
//		tmpCmd.setCommand(args[start].trim().toLowerCase());
		int i = start + 1;
		boolean done = false;
		while ((i < args.length) && !done)
		{
//        assignment | permission | proxy | scopeTree | subject | subsystem
			if (args[i].equals(EX_ASSGN) ||
					args[i].equals(EX_PERM) ||
					args[i].equals(EX_PROXY) ||
					args[i].equals(EX_SCOPE) ||
					args[i].equals(EX_SUBJ) ||
					args[i].equals(EX_SUBSYS))
			{
				tmpCmd.setType(args[i]);
				parseParams(args, i, tmpCmd);
				i = tmpCmd.getNextArg();
			}
		}

		return (tmpCmd);
	}

	protected static void parseParams(String[] args, int start, Command cmd)
	{
		int i = start + 1;
		boolean done = false;
		while ((i < args.length) && !done)
		{
			String[] vals = args[i].split(EQUALS);
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

	protected static Command parseImportCmd(String[] args, int start)
	{
		Command tmpCmd = new Command();
		String[] expandedCmd = expandCommand(args[start], STDIN_FILENAME);
		tmpCmd.setCommand(expandedCmd[0]);
		tmpCmd.setFilename(expandedCmd[1]);
//		tmpCmd.setCommand(args[start].trim().toLowerCase());
		int i = start + 1;
//        add | update | deactivate | delete
		if (args[i].equals(IM_ADD) ||
				args[i].equals(IM_DEACT) ||
				args[i].equals(IM_DEL) ||
				args[i].equals(IM_UPD))
		{
			tmpCmd.setType(args[i]);
			tmpCmd.setNextArg(++i);
		}

		return (tmpCmd);
	}

	public static String[] expandCommand(String cmd, String defValue)
	{
		String[] retval = new String[] {null, null};
		if (null == cmd)
			return (retval);

		String[] expandedCmd = cmd.split(EQUALS);
		if (null != expandedCmd)
		{
			retval[0] = expandedCmd[0];
			if (2 <= expandedCmd.length)
				retval[1] = expandedCmd[1];
			else
				retval[1] = defValue;
		}
		return (retval);
	}

//	/**
//	 * Scan all args for Help option
//	 * @param args
//	 * @return
//	 */
//	protected static boolean askForHelp(String[] args)
//	{
//		boolean retval = false;
//		for (int i = 0; (i < args.length) && !retval; i++)
//			if (args[i].equalsIgnoreCase(HELP_1_FLAG) ||
//					args[i].equalsIgnoreCase(HELP_2_FLAG))
//				retval = true;
//		return (retval);
//	}

	/**
	 * Display on-screen help
	 */
	protected static void showUsage()
	{
		showVersion();
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(HELP_FILE));
			String str;
			while (null != (str = br.readLine()))
				System.out.println(str);
			br.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

	protected static void showVersion()
	{
		System.out.println(SignetXml.class.getSimpleName() + ": " + version);
		System.out.println("Signet: " + Signet.getVersion());
	}

}

class CommandOptions
{
	protected String			cmdFilename;
	protected FileInputStream	cmdFile;
	protected int				nextArg;
	protected boolean			showVersion;
	protected boolean			showHelp;

	public CommandOptions()
	{
		setCmdFile(null);
		setNextArg(0);
		setShowVersion(false);
		setShowHelp(false);
	}

	public void setNextArg(int nextArg) { this.nextArg = nextArg; }
	public int getNextArg() { return (nextArg); }

	public boolean showHelp() { return showHelp; }
	public void setShowHelp(boolean showHelp) { this.showHelp = showHelp; }

	public boolean showVersion() { return showVersion; }
	public void setShowVersion(boolean showVersion) { this.showVersion = showVersion; }

	public FileInputStream getCmdFile() { return cmdFile; }
	public void setCmdFile(FileInputStream cmdFile) { this.cmdFile = cmdFile; }

	public String getCmdFilename() { return cmdFilename; }
	public void setCmdFilename(String cmdFilename) { this.cmdFilename = cmdFilename; }

}

class Command
{
	protected String			command;		// import or export or append
	protected String			type;			// assignment, scopetree, etc.
	protected Hashtable<String, String>	params;	// param / value pairs
	protected int				nextArg;		// next command-line arg
	protected String			filename;		// the file to use for this cmd
	protected InputStream		inFile;			// import file stream
	protected OutputStream		outFile;		// export/append file stream

	public Command()
	{
		setCommand("");
		setNextArg(0);
		setType("");
		setParams(new Hashtable<String, String>());
		setFilename(null);
		setInFile(null);
		setOutFile(null);
	}

	public String getCommand() { return command; }
	public void setCommand(String command) { this.command = command; }

	public void setNextArg(int nextArg) { this.nextArg = nextArg; }
	public int getNextArg() { return (nextArg); }

	public Hashtable<String, String> getParams() { return params; }
	public void setParams(Hashtable<String, String> params) { this.params = params; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public void setFilename(String filename) { this.filename = filename; }
	public String getFilename() { return (filename); }

	public InputStream getInFile() { return inFile; }
	public void setInFile(InputStream inFile) { this.inFile = inFile; }

	public OutputStream getOutFile() { return outFile; }
	public void setOutFile(OutputStream outFile) { this.outFile = outFile; }

	/////////////////////////////
	// overrides Object
	/////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("[Command:");
		buf.append(" command=" + getCommand());
		buf.append(" type=" + getType());
		buf.append(" nextArg=" + getNextArg());
		buf.append(" params=[");
		for (String key : getParams().keySet())
			buf.append(key + "=" + getParams().get(key) + " ");
		buf.append("]");
		buf.append("]");
		return (buf.toString());
	}

}
