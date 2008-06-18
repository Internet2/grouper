/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/CommandArg.java,v 1.1 2008-06-18 01:21:39 ddonn Exp $

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

import java.util.Hashtable;

/**
 * CommandArg 
 * 
 */
public class CommandArg
{
	////////////////////////////////
	// statics
	////////////////////////////////

	// parameter flags
	public static final String CMD_INFILE		= "-c";
	public static final String HELP_2_FLAG		= "-?";
	public static final String HELP_1_FLAG		= "-h";
	public static final String HELP_FILE		= "commandHelp.txt";
	public static final String VERSION_FLAG		= "-v";
	public static final String EQUALS			= ":"; // DOS treats "=" as a separator
//	public static final String EQUALS			= "="; // a bit overboard, I know
	public static final String XML_FILE_FLAG	= "-f";
	public static final String XML_EXTENSION	= ".xml";

	// parameters
	public static final String PARAM_FLAG		= "-";
	public static final String PARAM_SUBJID		= PARAM_FLAG + "subjId";
	public static final String PARAM_SCOPEID	= PARAM_FLAG + "scopeId";
	public static final String PARAM_STATUS		= PARAM_FLAG + "status";
	public static final String PARAM_FUNCID		= PARAM_FLAG + "functionId";
	public static final String PARAM_SUBSYSID	= PARAM_FLAG + "subsysId";
	public static final String PARAM_PERMID		= PARAM_FLAG + "permId";
	public static final String PARAM_SOURCEID	= PARAM_FLAG + "source";
	public static final String PARAM_NAME		= PARAM_FLAG + "name";

	// commands
	public static final String CMD_IMPORT		= "import";
//	public static final String CMD_APPEND		= "append";
	public static final String CMD_EXPORT		= "export";

	// import command types: add | update | deactivate | delete
	public static final String IM_ADD			= "add";
	public static final String IM_UPD			= "update";
	public static final String IM_DEACT			= "deactivate";
	public static final String IM_DEL			= "delete";

	// export command types: assignment | permission | proxy | scopeTree | subject | subsystem
	public static final String EX_ASSGN			= "assignment";
	public static final String EX_PERM			= "permission";
//	public static final String EX_PROXY			= "proxy";
	public static final String EX_SCOPE			= "scopeTree";
	public static final String EX_SUBJ			= "subject";
	public static final String EX_SUBSYS		= "subsystem";


	//////////////////////////
	// class variables
	//////////////////////////

//	protected String			command;		// import or export
	protected String			type;			// assignment, scopetree, etc.
	protected Hashtable<String, String>	params;	// param / value pairs
	protected int				nextArg;		// next command-line arg
//	protected String			filename;		// the file to use for this cmd
//	protected InputStream		inFile;			// import file stream
//	protected OutputStream		outFile;		// export/append file stream


	public CommandArg()
	{
//		setCommand("");
		setNextArg(0);
		setType("");
		setParams(new Hashtable<String, String>());
//		setFilename(null);
//		setInFile(null);
//		setOutFile(null);
	}

//	public String getCommand() { return command; }
//	public void setCommand(String command) { this.command = command; }

	public void setNextArg(int nextArg) { this.nextArg = nextArg; }
	public int getNextArg() { return (nextArg); }

	public Hashtable<String, String> getParams() { return params; }
	public void setParams(Hashtable<String, String> params) { this.params = params; }

	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

//	public void setFilename(String filename)
//	{
//		this.filename = filename;
//		if ((null != this.filename) && (0 < this.filename.length()))
//		{
//			File f = new File(this.filename);
//			if ( !f.getAbsolutePath().trim().toLowerCase().endsWith(XML_EXTENSION))
//				this.filename = this.filename + XML_EXTENSION;
//		}
//	}
//	public String getFilename() { return (filename); }
//
//	public InputStream getInFile() { return inFile; }
//	public void setInFile(InputStream inFile) { this.inFile = inFile; }
//
//	public OutputStream getOutFile() { return outFile; }
//	public void setOutFile(OutputStream outFile) { this.outFile = outFile; }

	/////////////////////////////
	// overrides Object
	/////////////////////////////

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("[CommandArg:");
//		buf.append(" command=" + getCommand());
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
