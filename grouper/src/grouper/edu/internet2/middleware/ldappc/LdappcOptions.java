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

import edu.internet2.middleware.ldappc.exception.LdappcException;

/**
 * Class for processing command line input arguments, to verify them, and to make them or
 * values calculated from them available to the rest of the code.
 */
public class LdappcOptions {

  /**
   * The subjectId.
   */
  private String subjectId;

  /**
   * Flag for skipping logging of error messages when doing a JUnit test run. This is
   * initialized to false by default.
   */
  private boolean isTest;

  /**
   * An indicator that groups are to be provisioned.
   */
  private boolean doGroups;

  /**
   * An indicator that memberships are to be provisioned.
   */
  private boolean doMemberships;

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
   * The path to the file written when calculating provisioning.
   */
  private String calculateOutputFileLocation = "ldappc.ldif";

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

  private Option subjectOption = new Option("s", "subject", true,
      "The id of the subject used to establish the Grouper session");

  private Option groupsOption = new Option("g", "groups", false, "Provision groups");

  private Option membershipsOption = new Option("m", "memberships", false,
      "Provision memberships");

  private Option lastModifyTimeOption = new Option("l", "lastModifyTime", true,
      "Only select objects changed after this time");

  private Option configManagerOption = new Option("c", "configManager", true,
      "Location of substitute configuration file, other than default, ldappc.xml");

  private Option intervalOption = new Option(
      "i",
      "interval",
      true,
      "Number of seconds between provisioning cycles. If omitted, only one provisioning cycle is performed.");

  private Option calculateOption = new Option("calc", "calculate", true,
      "Calculate provisioning and write to file, defaults to ldappc.ldif");

  private Option dryRunOption = new Option("n", "dry-run", false,
      "Show what would be done without doing it");

  public LdappcOptions() {

    subjectOption.setArgName("subjectId");
    options.addOption(subjectOption);

    OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(groupsOption);
    optionGroup.addOption(membershipsOption);
    optionGroup.setRequired(true);
    options.addOptionGroup(optionGroup);

    lastModifyTimeOption.setArgName("yyyy-MM-dd[_hh:mm:ss]");
    options.addOption(lastModifyTimeOption);

    intervalOption.setArgName("seconds");
    options.addOption(intervalOption);

    configManagerOption.setArgName("path");
    options.addOption(configManagerOption);

    OptionGroup modeOptionGroup = new OptionGroup();
    calculateOption.setArgName("ldappc.ldif");
    modeOptionGroup.addOption(calculateOption);
    modeOptionGroup.addOption(dryRunOption);
    options.addOptionGroup(modeOptionGroup);
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

    this.setSubjectId(line.getOptionValue(subjectOption.getOpt(), "GrouperSystem"));

    if (line.hasOption(groupsOption.getOpt())) {
      this.setDoGroups(true);
    }

    if (line.hasOption(membershipsOption.getOpt())) {
      this.setDoMemberships(true);
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

    if (line.hasOption(calculateOption.getOpt())) {
      String file = line.getOptionValue(calculateOption.getOpt());
      this.setCalculateOutputFileLocation(file);
      this.setMode(ProvisioningMode.CALCULATE);
    }

    if (line.hasOption(this.dryRunOption.getOpt())) {
      this.setMode(ProvisioningMode.DRYRUN);
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
    this.configManagerLocation = "file:" + configManagerLocation;
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
  public String getCalculateOutputFileLocation() {
    return calculateOutputFileLocation;
  }

  protected void setCalculateOutputFileLocation(String calculateOutputFileLocation) {
    this.calculateOutputFileLocation = calculateOutputFileLocation;
  }
}
