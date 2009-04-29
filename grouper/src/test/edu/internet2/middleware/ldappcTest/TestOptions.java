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

package edu.internet2.middleware.ldappcTest;

/**
 * Class to contain test options.
 * 
 * @author Gil Singer
 */
public class TestOptions {

  /**
   * This is the setter for testCaseSet.
   * 
   * @param testCaseSet
   *          The testCaseSet.
   */

  /*
   * private static void setTestCaseSet(String testCaseSet) { this.testCaseSet =
   * testCaseSet; }
   */

  /*
   * This identifies the set of test cases other than ones that are independent of the
   * environment (e.g database) being used. Typically this is set to a machine type plus
   * additional identifier, like biofix1 or windows1.
   * 
   * @return The current value of testCaseSet.
   */

  /*
   * Restore only to run new test case sets that require different logic. public static
   * String getTestCaseSet() { return ResourceBundleUtil.getString("testCaseSet");
   * //return testCaseSet; }
   */

  /**
   * When SHOW_NAMES is true, detailed test output is displayed to the console when
   * running the automated test cases.
   */
  public static boolean SHOW_NAMES = true;

  /**
   * When SHOW_CONTENTS is true, database content information is displayed to the console
   * when running the automated test cases.
   */
  public static boolean SHOW_CONTENTS = true;

  /**
   * Constructor
   */
  public TestOptions() {
    // testCaseSet = ResourceBundleUtil.getString("testCaseSet");
  }
}
