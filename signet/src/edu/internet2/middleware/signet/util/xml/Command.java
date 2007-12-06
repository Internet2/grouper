/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/Command.java,v 1.1 2007-12-06 01:18:32 ddonn Exp $

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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * Command 
 * 
 */
public class Command
{
	////////////////////////////////
	// statics
	////////////////////////////////

	// parameter flags
	protected static final String CMD_INFILE	= "-c";
	protected static final String HELP_2_FLAG	= "-?";
	protected static final String HELP_1_FLAG	= "-h";
	protected static final String HELP_FILE		= "commandHelp.txt";
	protected static final String VERSION_FLAG	= "-v";
	protected static final String EQUALS		= "="; // a bit overboard, I know

	// parameters
	protected static final String PARAM_SUBJID	= "subjId";
	protected static final String PARAM_SCOPEID	= "scopeId";
	protected static final String PARAM_STATUS	= "status";
	protected static final String PARAM_FUNCID	= "functionId";
	protected static final String PARAM_SUBSYSID = "subsysId";

	// commands
	protected static final String CMD_IMPORT	= "import";
	protected static final String CMD_APPEND	= "append";
	protected static final String CMD_EXPORT	= "export";

	// import commands: add | update | deactivate | delete
	protected static final String IM_ADD		= "add";
	protected static final String IM_UPD		= "update";
	protected static final String IM_DEACT		= "deactivate";
	protected static final String IM_DEL		= "delete";

	// export commands: assignment | permission | proxy | scopeTree | subject | subsystem
	protected static final String EX_ASSGN		= "assignment";
	protected static final String EX_PERM		= "permission";
	protected static final String EX_PROXY		= "proxy";
	protected static final String EX_SCOPE		= "scopeTree";
	protected static final String EX_SUBJ		= "subject";
	protected static final String EX_SUBSYS		= "subsystem";


	//////////////////////////
	// class variables
	//////////////////////////

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
