/*
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.spml;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PSPOptions {

  private Options options;

  private String confDir;

  private String beanName;

  private boolean logSpml;

  /**
   * Constructor
   * 
   * @param args
   * @throws ParseException
   */
  public PSPOptions(String[] args) throws ParseException {

    options = new Options();

    options.addOption("confDir", true, "configuration directory");

    CommandLineParser parser = new GnuParser();

    CommandLine line = parser.parse(options, args);

    if (line.hasOption("confDir")) {
      setConfDir(line.getOptionValue("confDir"));
    }

  }

  public void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("ldappc", options, true);
  }

  /**
   * @return Returns the beanName.
   */
  public String getBeanName() {
    return beanName;
  }

  /**
   * @param beanName
   *          The beanName to set.
   */
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

  /**
   * @return Returns the confDir.
   */
  public String getConfDir() {
    return confDir;
  }

  /**
   * @param confDir
   *          The confDir to set.
   */
  public void setConfDir(String confDir) {
    this.confDir = confDir;
  }

  /**
   * @return Returns the logSpml.
   */
  public boolean isLogSpml() {
    return logSpml;
  }

  /**
   * @param logSpml
   *          The logSpml to set.
   */
  public void setLogSpml(boolean logSpml) {
    this.logSpml = logSpml;
  }
}
