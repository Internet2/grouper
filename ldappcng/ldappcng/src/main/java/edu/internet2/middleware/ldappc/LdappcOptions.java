/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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

package edu.internet2.middleware.ldappc;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.ldappc.exception.LdappcException;

/**
 * Class for processing command line input arguments, to verify them, and to make them or
 * values calculated from them available to the rest of the code.
 */
public class LdappcOptions {

  /**
   * The subjectId.
   */
  private String subjectId = GrouperConfig.ROOT;

  /**
   * Flag for skipping logging of error messages when doing a JUnit test run. This is
   * initialized to false by default.
   */
  private boolean isTest;

  /**
   * An indicator that groups are to be provisioned.
   */
  private boolean doGroups = false;

  /**
   * An indicator that memberships are to be provisioned.
   */
  private boolean doMemberships = false;

  /**
   * The lastModifyTime.
   */
  private Date lastModifyTime;

  /**
   * The interval in seconds between polling actions.
   */
  private int interval = 0;

  /**
   * The configManagerLocation is the location of an alternative configuration manager.
   */
  private String configManagerLocation;

  /**
   * The propertiesFileLocation is the location of an alternative propertiesFile.
   */
  private String propertiesFileLocation;

  /**
   * The path to the output file.
   */
  private String outputFileLocation = "ldappc.ldif";

  /**
   * While provisioning, write changes as ldif to file.
   */
  private boolean logLdif = false;

  /**
   * Path to AttributeResolver configuration files.
   */
  private String attributeResolverLocation;

  /**
   * The name of the "internal" Spring configuration file for the Attribute Resolver.
   */
  public static final String ATTRIBUTE_RESOLVER_FILE_NAME_INTERNAL = "ldappc-internal.xml";

  /**
   * The name of the "services" Spring configuration file for the Attribute Resolver.
   */
  public static final String ATTRIBUTE_RESOLVER_FILE_NAME_SERVICES = "ldappc-services.xml";

  /**
   * The name of the Attribute Authority service.
   */
  public static final String ATTRIBUTE_AUTHORITY_NAME = "grouper.AttributeAuthority";
  
  /**
   * The name of the Attribute Resolver service.
   */
  public static final String ATTRIBUTE_RESOLVER_NAME = "grouper.AttributeResolver";

  /**
   * Modes of operation.
   */
  public enum ProvisioningMode {
    /**
     * Calculate and print provisioning.
     */
    CALCULATE,

    /**
     * Show what would be provisioned but don't provision.
     */
    DRYRUN,

    /**
     * Provision, the default.
     */
    PROVISION,
  };

  /**
   * The mode of operation.
   */
  private ProvisioningMode mode = ProvisioningMode.PROVISION;

  /**
   * CLI options follow
   */
  private Options options = new Options();

  private Option subjectOption = new Option("subject", true,
      "The SubjectId used to establish Grouper API sessions. Defaults to GrouperSystem.");

  private Option groupsOption = new Option("groups", false, "Provision groups.");

  private Option membershipsOption = new Option("memberships", false,
      "Provision memberships.");

  private Option lastModifyTimeOption = new Option("lastModifyTime", true,
      "Select objects changed since this time.");

  private Option configManagerOption = new Option("configManager", true,
      "Path to configuration file. Defaults to classpath resource ldappc.xml.");

  private Option propertiesFileOption = new Option("properties", true,
      "Path to properties file. Defaults to classpath resource ldappc.properties.");

  private Option intervalOption = new Option(
      "interval",
      true,
      "Number of seconds between provisioning cycles. If omitted, only one provisioning cycle is performed.");

  private Option calculateOption = new Option("calc", true,
      "Calculate provisioning and write to file.");

  private Option dryRunOption = new Option("dryRun", true,
      "Write provisioning changes to file only, do not provision changes.");

  private Option logLdifOption = new Option("logLDIF", false,
      "While provisioning, log changes in LDIF format.");

  private Option attributeResolverLocationOption = new Option("resolver", true,
      "Path to directory containing Shibboleth Attribute Resolver configuration files.");

  public LdappcOptions() {

    subjectOption.setArgName("subjectId");
    options.addOption(subjectOption);

    options.addOption(groupsOption);

    options.addOption(membershipsOption);

    lastModifyTimeOption.setArgName("yyyy-MM-dd[_hh:mm:ss]");
    options.addOption(lastModifyTimeOption);

    intervalOption.setArgName("seconds");
    options.addOption(intervalOption);

    configManagerOption.setArgName("path");
    options.addOption(configManagerOption);

    propertiesFileOption.setArgName("path");
    options.addOption(propertiesFileOption);

    options.addOption(logLdifOption);

    OptionGroup modeOptionGroup = new OptionGroup();
    calculateOption.setArgName("file");
    modeOptionGroup.addOption(calculateOption);
    dryRunOption.setArgName("file");
    modeOptionGroup.addOption(dryRunOption);
    options.addOptionGroup(modeOptionGroup);

    attributeResolverLocationOption.setArgName("path");
    options.addOption(attributeResolverLocationOption);
  }

  /**
   * The Constructor for input processing of command line options.
   * 
   * @param args
   *          Command line arguments to parse.
   * @throws org.apache.commons.cli.ParseException
   * @throws ParseException
   */
  public LdappcOptions(String[] args) throws org.apache.commons.cli.ParseException,
      ParseException {
    this();
    init(args);
  }

  /**
   * This initializer verifies the input arguments and makes them accessible through the
   * use of getter methods.
   * 
   * @param args
   * 
   * @throws org.apache.commons.cli.ParseException
   * @throws ParseException
   */
  public void init(String[] args) throws org.apache.commons.cli.ParseException,
      ParseException {

    CommandLineParser parser = new PosixParser();

    CommandLine line = parser.parse(options, args);

    if (line.hasOption(subjectOption.getOpt())) {
      this.setSubjectId(line.getOptionValue(subjectOption.getOpt()));
    }

    if (line.hasOption(groupsOption.getOpt())) {
      this.setDoGroups(true);
    }

    if (line.hasOption(membershipsOption.getOpt())) {
      this.setDoMemberships(true);
    }

    if (!(this.getDoGroups() || this.getDoMemberships())) {
      throw new LdappcException("Specify either " + groupsOption.toString() + " or "
          + membershipsOption.toString());
    }

    if (line.hasOption(lastModifyTimeOption.getOpt())) {

      StringBuffer dateInput = new StringBuffer(line.getOptionValue(lastModifyTimeOption
          .getOpt()));
      int len = dateInput.length();
      if (len == 10) {
        dateInput.append("_00:00:00");
      } else if (len == 13) {
        dateInput.append(":00:00");
      } else if (len == 16) {
        dateInput.append(":00");
      }

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'_'hh:mm:ss");
      df.setLenient(true);
      setLastModifyTime(df.parse(dateInput.toString()));
    }

    if (line.hasOption(intervalOption.getOpt())) {
      this.setInterval(Integer.parseInt(line.getOptionValue(intervalOption.getOpt())));
    }

    if (line.hasOption(configManagerOption.getOpt())) {
      String file = line.getOptionValue(configManagerOption.getOpt());
      if (new File(file).exists()) {
        this.setConfigManagerLocation(file);
      } else {
        throw new LdappcException("Cannot find config file : " + file);
      }
    }

    if (line.hasOption(propertiesFileOption.getOpt())) {
      String file = line.getOptionValue(propertiesFileOption.getOpt());
      if (new File(file).exists()) {
        this.setPropertiesFileLocation(file);
      } else {
        throw new LdappcException("Cannot find properties file : " + file);
      }
    }

    if (line.hasOption(calculateOption.getOpt())) {
      this.setOutputFileLocation(line.getOptionValue(calculateOption.getOpt()));
      this.setMode(ProvisioningMode.CALCULATE);
    }

    if (line.hasOption(dryRunOption.getOpt())) {
      this.setOutputFileLocation(line.getOptionValue(dryRunOption.getOpt()));
      this.setMode(ProvisioningMode.DRYRUN);
    }

    if (line.hasOption(logLdifOption.getOpt())) {
      this.setLogLdif(true);
    }

    if (line.hasOption(attributeResolverLocationOption.getOpt())) {
      this.setAttributeResolverLocation(line
          .getOptionValue(attributeResolverLocationOption.getOpt()));
    }
  }

  public void setIsTest(boolean isTest) {
    this.isTest = isTest;
  }

  public boolean isTest() {
    return isTest;
  }

  protected void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * Returns the Subject ID used to select data for provisioning.
   * 
   * @return The current value of subjectId.
   */
  public String getSubjectId() {
    return subjectId;
  }

  protected void setDoGroups(boolean doGroups) {
    this.doGroups = doGroups;
  }

  /**
   * This returns a boolean indicating whether or not groups are provisioned.
   * 
   * @return <code>true</code> if groups are to be provisioned and <code>false</code>
   *         otherwise.
   */
  public boolean getDoGroups() {
    return doGroups;
  }

  protected void setDoMemberships(boolean doMemberships) {
    this.doMemberships = doMemberships;
  }

  /**
   * This returns a boolean indicating whether or not memberships are provisioned.
   * 
   * @return <code>true</code> if memberships are to be provisioned and <code>false</code>
   *         otherwise.
   */
  public boolean getDoMemberships() {
    return doMemberships;
  }

  public void setLastModifyTime(Date lastModifyTime) {
    this.lastModifyTime = lastModifyTime;
  }

  /**
   * This returns the last modify time.
   * 
   * @return Last modify time or <code>null</code> if not defined.
   */
  public Date getLastModifyTime() {
    return lastModifyTime;
  }

  protected void setInterval(int interval) {
    this.interval = interval;
  }

  /**
   * This is the getter for interval.
   * 
   * @return The current value of interval.
   */
  public int getInterval() {
    return interval;
  }

  protected void setConfigManagerLocation(String configManagerLocation) {
    // this.configManagerLocation = "file:" + configManagerLocation;
    this.configManagerLocation = configManagerLocation;
  }

  /**
   * Get the location of the alternative configuration manager.
   * 
   * @return URI of the alternative configuration XML file.
   */
  public String getConfigManagerLocation() {
    return configManagerLocation;
  }

  public void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("ldappc", options, true);
  }

  /**
   * The mode of operation.
   * 
   * @return {@link #mode}
   */
  public ProvisioningMode getMode() {
    return mode;
  }

  protected void setMode(ProvisioningMode mode) {
    this.mode = mode;
  }

  /**
   * The path to the file written during calculate mode.
   * 
   * @return the path
   */
  public String getOutputFileLocation() {
    return outputFileLocation;
  }

  protected void setOutputFileLocation(String outputFileLocation) {
    this.outputFileLocation = outputFileLocation;
  }

  /**
   * The path to the properties file.
   * 
   * @return
   */
  public String getPropertiesFileLocation() {
    return propertiesFileLocation;
  }

  public void setPropertiesFileLocation(String propertiesFileLocation) {
    this.propertiesFileLocation = propertiesFileLocation;
  }

  protected void setLogLdif(boolean logLdif) {
    this.logLdif = logLdif;
  }

  /**
   * This returns a boolean indicating whether or not to log changes in LDIF format.
   * 
   * @return <code>true</code> if log LDIF changes <code>false</code> otherwise.
   */
  public boolean getLogLdif() {
    return logLdif;
  }

  /**
   * The path to the Attribute Resolver configuration files.
   * 
   * @return
   */
  public String getAttributeResolverLocation() {
    return attributeResolverLocation;
  }

  protected void setAttributeResolverLocation(String attributeResolverLocation) {
    this.attributeResolverLocation = attributeResolverLocation;
  }
}
