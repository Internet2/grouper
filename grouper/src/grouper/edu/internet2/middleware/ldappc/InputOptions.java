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

import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

/**
 * Class for processing command line input arguments, to verify them, and to make them or
 * values calculated from them available to the rest of the code.
 * 
 * @author Gil Singer
 */
public class InputOptions implements GrouperProvisionerOptions {

  /**
   * Make the lower limit of the frequency of polling a reasonable number of seconds.
   */
  private static final int MIN_INTERVAL = 10;

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
   * The doPolling. An indicator that polling is to be performed. It is set if the
   * -interval parameter is used as has a non-zero value.
   */
  private boolean doPolling;

  /**
   * The interval in seconds between polling actions.
   */
  private int interval;

  /**
   * The configManagerLocation is the location of an alternative configuration manager.
   */
  private String configManagerLocation;

  /**
   * A flag indicating that the program had a fatal error and is to terminate.
   */
  private boolean fatal;

  /**
   * Constructor: This no argument constructor is needed for testing so that setIsTest can
   * be called before initialization. Except for the JUnit test cases use the construct
   * that has input arguments.
   */
  public InputOptions() {
  }

  /**
   * The Constructor for input processing of command line options.
   * 
   * @param args
   *          Command line arguments to parse.
   */
  public InputOptions(String[] args) {
    init(args);
  }

  /**
   * This is the setter for isTest.
   * 
   * @param isTest
   *          isTest is true if this is a test case run.
   */
  public void setIsTest(boolean isTest) {
    this.isTest = isTest;
  }

  /**
   * This is the getter for isTest.
   * 
   * @return The current value of isTest.
   */
  public boolean isTest() {
    return isTest;
  }

  /**
   * This is the setter for subjectId.
   * 
   * @param subjectId
   *          The subjectId.
   */
  protected void setSubjectId(String subjectId) {
    this.subjectId = subjectId;
  }

  /**
   * This is the getter for subjectId.
   * 
   * @return The current value of subjectId.
   */
  public String getSubjectId() {
    return subjectId;
  }

  /**
   * This is the setter for doGroups.
   * 
   * @param doGroups
   *          The doGroups.
   */
  protected void setDoGroups(boolean doGroups) {
    this.doGroups = doGroups;
  }

  /**
   * This is the getter for doGroups.
   * 
   * @return The current value of doGroups.
   */
  public boolean getDoGroups() {
    return doGroups;
  }

  /**
   * This is the setter for doMemberships.
   * 
   * @param doMemberships
   *          The doMemberships.
   */
  protected void setDoMemberships(boolean doMemberships) {
    this.doMemberships = doMemberships;
  }

  /**
   * This is the getter for doMemberships.
   * 
   * @return The current value of doMemberships.
   */
  public boolean getDoMemberships() {
    return doMemberships;
  }

  /**
   * This is the setter for lastModifyTime.
   * 
   * @param lastModifyTime
   *          The lastModifyTime.
   */
  public void setLastModifyTime(Date lastModifyTime) {
    this.lastModifyTime = lastModifyTime;
  }

  /**
   * This is the getter for lastModifyTime.
   * 
   * @return The current value of lastModifyTime.
   */
  public Date getLastModifyTime() {
    return lastModifyTime;
  }

  /**
   * This is the setter for doPolling.
   * 
   * @param doPolling
   *          The doPolling.
   */
  private void setDoPolling(boolean doPolling) {
    this.doPolling = doPolling;
  }

  /**
   * This is the getter for doPolling.
   * 
   * @return The current value of doPolling.
   */
  public boolean getDoPolling() {
    return doPolling;
  }

  /**
   * This is the setter for interval.
   * 
   * @param interval
   *          The interval.
   */
  private void setInterval(int interval) {
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

  /**
   * This is the setter for configManagerLocation.
   * 
   * @param configManagerLocation
   *          The location of the configuration manager xml file.
   */
  private void setConfigManagerLocation(String configManagerLocation) {
    this.configManagerLocation = "file:" + configManagerLocation;
  }

  /**
   * This is the getter for configManagerLocation.
   * 
   * @return The current value of configManagerLocation.
   */
  public String getConfigManagerLocation() {
    return configManagerLocation;
  }

  /**
   * This is the setter for fatal.
   * 
   * @param fatal
   *          The fatal.
   */
  private void setFatal(boolean fatal) {
    this.fatal = fatal;
  }

  /**
   * This is the getter for fatal.
   * 
   * @return The current value of fatal.
   */
  public boolean isFatal() {
    return fatal;
  }

  /**
   * This initializer verifies the input arguments and makes them accessible through the
   * use of getter methods.
   * 
   * @param args
   *          May be any of the following in any order, where (Opt) implies optional;
   *          however, when a key of -xxx is followed by a name, a value must be present
   *          following the key. The Keys shown below must appear exactly as shown; the
   *          values column represents fields that the user inputs as appropriate.
   * 
   *          See the User Manual for more information.
   */
  public void init(String[] args) {
    try {
      if (args.length == 0) {
        System.out.println("Ldappc allowed arguments= -subject -groups -memberships "
            + "-permissions -lastModifyTime lastModifyTimeValue "
            + "\n-interval intervalValue -configManager yourConfigurationManagerXmlFile");
        System.out.println("The format for lastModifyTimeValue is: "
            + "\nyyyy-MM-dd_hh:mm:ss or yyyy-MM-dd_hh:mm or yyyy-MM-dd_hh or yyyy-MM-dd");
        System.out.println("The intervalValue is in seconds.");
        ErrorLog.fatal(this.getClass(),
            "Failed attempting to start program with no arguments.");
      }
      DebugLog.info("Running version: " + Ldappc.VERSION_NUMBER);
      DebugLog.info("Created on: " + Ldappc.VERSION_DATE);
      DebugLog.info(this.getClass(), "Starting Ldappc with the following arguments:");
      for (int i = 0; i < args.length; i++) {
        DebugLog.info(this.getClass(), args[i]);
      }

      //
      // Loop over all input arguments, perform checking, and set program
      // variables from this input.
      //

      //
      // Note that for the -subject and -interval and -lastModifyTime, the
      // index i is
      // incremented inside the loop.
      //

      for (int i = 0; i < args.length; i++) {
        args[i] = args[i].toLowerCase();
        if (args[i].startsWith("-")) {
          if (args[i].equalsIgnoreCase("-subject")) {
            if (i + 1 >= args.length) {
              if (!isTest()) {
                ErrorLog.fatal(this.getClass(),
                    "FATAL ERROR, Missing a value for subject.");
              }
              setFatal(true);
            } else {
              if (args[i + 1].startsWith("-")) {
                if (!isTest()) {
                  ErrorLog.fatal(this.getClass(),
                      "FATAL ERROR, Missing a value for -subject, value encountered was:"
                          + args[i + 1]);
                }
                setFatal(true);
              } else {
                setSubjectId(args[++i]);
              }
            }
          } else if (args[i].equalsIgnoreCase("-groups")) {
            setDoGroups(true);
          } else if (args[i].equalsIgnoreCase("-memberships")) {
            setDoMemberships(true);
          } else if (args[i].equalsIgnoreCase("-lastModifyTime")) {
            if (i + 1 >= args.length) {
              if (!isTest()) {
                ErrorLog.fatal(this.getClass(),
                    "FATAL ERROR, Missing a value for -lastModifyTime.");
              }
              setFatal(true);
            } else {

              if (args[i + 1].startsWith("-")) {
                if (!isTest()) {
                  ErrorLog.fatal(this.getClass(),
                      "FATAL ERROR, Missing a value for lastModifyTime, value encountered was:"
                          + args[i + 1]);
                }
                setFatal(true);
              } else {
                //
                // Change from String to a Date and check String
                // for format.
                // If short, fill in zeros for hours, minutes
                // and/or seconds.
                //

                StringBuffer dateInput = new StringBuffer(args[++i]);
                int len = dateInput.length();
                if (len == 10) {
                  dateInput.append("_00:00:00");
                } else if (len == 13) {
                  dateInput.append(":00:00");
                } else if (len == 16) {
                  dateInput.append(":00");
                }
                // DateFormat df =
                // DateFormat.getDateTimeInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'_'hh:mm:ss");
                df.setLenient(true);
                String dateInputAdjusted = dateInput.toString();
                try {
                  Date lastModTime = df.parse(dateInputAdjusted);
                  setLastModifyTime(lastModTime);
                } catch (ParseException pe) {
                  ErrorLog.fatal(this.getClass(), dateInputAdjusted
                      + " is incorrect format for lastModifyTime.  "
                      + "\nUse format: yyyy-MM-dd_hh:mm:ss or yyyy-MM-dd_hh:mm or "
                      + "yyyy-MM-dd_hh or yyyy-MM-dd");
                }
              }
            }
          } else if (args[i].equalsIgnoreCase("-interval")) {
            if (i + 1 >= args.length) {
              if (!isTest()) {
                ErrorLog.fatal(this.getClass(),
                    "FATAL ERROR, Missing a value for -interval.");
              }
              setFatal(true);
            } else {

              if (args[i + 1].startsWith("-")) {
                if (!isTest()) {
                  ErrorLog.fatal(this.getClass(),
                      "FATAL ERROR, Missing a value for interval, value encountered was:"
                          + args[i + 1]);
                }
                setFatal(true);
              } else {
                try {
                  setInterval(Integer.parseInt(args[++i]));
                } catch (NumberFormatException nfe) {
                  if (!isTest()) {
                    ErrorLog.fatal(this.getClass(),
                        "FATAL ERROR, the value following -interval must be an integer.");
                  }
                  setFatal(true);
                  i++;
                }
              }
            }

            if (getInterval() != 0) {
              setDoPolling(true);
              // Check for reasonable value and if too small, set
              // to MIN_INTERVAL.
              if (interval < MIN_INTERVAL) {
                if (!isTest()) {
                  ErrorLog.warn(this.getClass(),
                      "WARNING, the interval value input was too small, resetting to: "
                          + MIN_INTERVAL + " secs.");
                }
                setInterval(MIN_INTERVAL);
              }
            } else {
              setDoPolling(false);
              ErrorLog.warn(this.getClass(),
                  "WARNING, the interval value was zero so polling is ignored.");
            }
          } else if (args[i].equalsIgnoreCase("-configManager")) {
            if (i + 1 >= args.length) {
              ErrorLog.fatal(this.getClass(),
                  "FATAL ERROR, Missing a value for -configManager.");
              setFatal(true);
            } else {
              if (args[i + 1].startsWith("-")) {
                ErrorLog.fatal(this.getClass(),
                    "FATAL ERROR, Missing a value for configManager, value encountered was:"
                        + args[i + 1]);
                setFatal(true);
              } else {
                String configMgr = args[i + 1];
                File configMgrFile = new File(configMgr);
                if (configMgrFile.exists()) {
                  setConfigManagerLocation(args[++i]);
                } else {
                  ErrorLog
                      .fatal(this.getClass(),
                          "FATAL ERROR, the value following -configManager is not an existing file.");
                  setFatal(true);
                  i++;
                }
              }
            }
          }

        } else {
          if (!isTest()) {
            ErrorLog.warn(this.getClass(), "WARNING, BAD INPUT, Ignoring parameter: arg["
                + i + "]=" + args[i]);
          }
        }
      }

      //
      // The input has been read into program variables,
      // now check that required values are set.
      //

      // Allow no arguments at all which will produce a console message of
      // allowed values.
      if (getSubjectId() == null && args.length > 0) {
        if (!isTest()) {
          ErrorLog.fatal(this.getClass(),
              "FATAL ERROR, No subject value entered, '-subject subjectId' is required");
        }
        setFatal(true);

      }
    } catch (Exception e) {
      if (!isTest()) {
        ErrorLog.fatal(this.getClass(),
            "FATAL ERROR, Unexpected Exception Type in InputOptions.");
      }
      setFatal(true);
      e.printStackTrace();
    }
  }
}
