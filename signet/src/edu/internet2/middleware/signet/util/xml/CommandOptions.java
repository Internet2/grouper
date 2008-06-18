/*
	$Header: /home/hagleyj/i2mi/signet/src/edu/internet2/middleware/signet/util/xml/CommandOptions.java,v 1.2 2008-06-18 01:21:39 ddonn Exp $

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

import java.io.FileInputStream;

/**
 * CommandOptions 
 * 
 */
public class CommandOptions
{
	protected String			xmlFilename;
	protected String			cmdFilename;
	protected FileInputStream	cmdFile;
	protected int				nextArg;
	protected boolean			showVersion;
	protected boolean			showHelp;

	public CommandOptions()
	{
		setXmlFilename(null);
		setCmdFile(null);
		setNextArg(0);
		setShowVersion(false);
		setShowHelp(false);
	}

	public String getXmlFilename() { return (xmlFilename); }
	public void setXmlFilename(String xmlFilename) { this.xmlFilename = xmlFilename; }

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
