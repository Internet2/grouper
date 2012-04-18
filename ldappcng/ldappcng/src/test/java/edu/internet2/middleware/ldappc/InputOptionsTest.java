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

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcOptions.ProvisioningMode;

/**
 * InputOptionsTest verifies that arguments input to the InputOptions class constructor
 * (that typically are passed in copies of the command line parameters) are in proper
 * format and that the InputOptions class successfully makes this data available through
 * public getter methods. It also verifies that bad input arguments result in setting the
 * value of the isFatal() method to "true".
 * 
 * @author Gil Singer
 */

public class InputOptionsTest extends TestCase {

  /*
   * An instance of the main class, just to check that it can be created.
   */
  private LdappcOptions inputOptions;

  /*
   * An instance of the main class, to check for property setting of options.
   */
  private LdappcOptions inputOptions1;

  /*
   * An instance of the main class, to check for property setting of options.
   */
  private LdappcOptions inputOptions2;

  /**
   * InputOptions Test
   */
  public InputOptionsTest(String name) {
    super(name);
  }

  /**
   * Setup the fixture.
   */
  protected void setUp() {
    //
    // Try to perform the set up
    //
    // Test one set of args and then its complement.
    //

    String args[] = { "-subject", "subjectIdA", "-groups" };
    String args1[] = { "-subject", "subjectIdA", "-memberships", "-lastModifyTime",
        "2006-09-14_20:55" };
    String args2[] = { "-subject", "subjectIdB", "-groups", "-interval", "123" };

    try {
      inputOptions = new LdappcOptions(args);
      inputOptions1 = new LdappcOptions(args1);
      inputOptions2 = new LdappcOptions(args2);
    } catch (Exception e) {
      fail(e.toString());
    }
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    TestRunner.run(InputOptionsTest.class);
  }

  /**
   * Trivial test
   */
  public void testCreateInputOptions() {
    //
    // Try to perform the test
    //
    try {
      // Trivial test, add more significant ones later:
      // Make sure an agency was created by the constructor.
      assertTrue("Failed to create an inputOptions instance", inputOptions != null);
    } catch (Exception e) {
      //
      // Any exception means the test failed.
      //
      fail("Failure in testCreateInputOptons: " + e.getMessage());
    } finally {
      //
    }
  }

  /**
   * Test the first set of input options This tests correct input.
   */
  public void testInputOptions1() {

    try {
      // String args1[] = {"-subject", "subjectIdA", "-memberships", "-lastModifyTime",
      // "2006-09-14_20:55"};
      assertEquals("Failed on set 1, subjectIdA", "subjectIdA", inputOptions1
          .getSubjectId());
      assertTrue("Failed on set 1, memberships", inputOptions1.getDoMemberships());
      assertTrue("Failed on set 1, groups", !inputOptions1.getDoGroups());
      assertTrue("Failed on set 1, groups", !inputOptions1.getDoGroups());
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(inputOptions1.getLastModifyTime());
      int day = calendar.get(Calendar.DAY_OF_MONTH);
      int minute = calendar.get(Calendar.MINUTE);
      // The ideal test would be like the following except that the 3rd arg returns a
      // Date, not a String.
      // assertEquals("Failed on lastModifyTime", "2006-09-14_20:55",
      // inputOptions1.getLastModifyTime());
      assertEquals("Failed on lastModifyTime month", 14, day);
      assertEquals("Failed on lastModifyTime minute", 55, minute);
    } catch (Exception e) {
      fail("Failure in testInputOptions1: " + e.getMessage());
    }
  }

  /**
   * Test the second set of input options This tests correct input.
   */
  public void testInputOptions2() {

    try {
      // String args2[] = {"-subject", "subjectIdB", "-groups", "-permissions",
      // "-interval", "123"};
      assertEquals("Failed on set 2, subjectIdB", "subjectIdB", inputOptions2
          .getSubjectId());
      assertTrue("Failed on set 2, memberships", !inputOptions2.getDoMemberships());
      assertTrue("Failed on set 2, groups", inputOptions2.getDoGroups());
      assertTrue("Failed on set 2, groups", inputOptions2.getDoGroups());
      assertNull("Failed on lastModifyTime", inputOptions2.getLastModifyTime());
    } catch (Exception e) {
      fail("Failure in testInputOptions2: " + e.getMessage());
    }
  }

  public void testInputOptionsNoGroupsNorMemberships() {
    String[] args = {};
    try {
      inputOptions = new LdappcOptions(args);
      fail("should fail without groups or memberships option");
    } catch (Exception e) {
      // OK
    }
  }

  public void testInputOptionsCalculate() {
    String[] args = { "-groups", "-calc", "filename" };
    try {
      inputOptions = new LdappcOptions(args);
      assertEquals(ProvisioningMode.CALCULATE, inputOptions.getMode());
      assertEquals("filename", inputOptions.getOutputFileLocation());
    } catch (Exception e) {
      fail("Failure : " + e);
    }
  }

  public void testInputOptionsDryRun() {
    String[] args = { "-groups", "-dryRun", "filename" };
    try {
      inputOptions = new LdappcOptions(args);
      assertEquals(ProvisioningMode.DRYRUN, inputOptions.getMode());
      assertEquals("filename", inputOptions.getOutputFileLocation());
    } catch (Exception e) {
      fail("Failure : " + e);
    }
  }

  public void testInputOptionsProvision() {
    String[] args = { "-groups" };
    try {
      inputOptions = new LdappcOptions(args);
      assertEquals(ProvisioningMode.PROVISION, inputOptions.getMode());
    } catch (Exception e) {
      fail("Failure : " + e);
    }
  }

  public void testInputOptionsConfigFile() {
    String path = GrouperUtil.fileFromResourceName(BaseLdappcTestCase.CONFIG_RESOURCE)
        .getAbsolutePath();
    String[] args = { "-groups", "-configManager", path };
    try {
      inputOptions = new LdappcOptions(args);
      assertEquals(path, inputOptions.getConfigManagerLocation());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failure : " + e);
    }
  }

  public void testInputOptionsPropertiesFile() {
    String path = GrouperUtil
        .fileFromResourceName(BaseLdappcTestCase.PROPERTIES_RESOURCE).getAbsolutePath();
    String[] args = { "-groups", "-properties", path };
    try {
      inputOptions = new LdappcOptions(args);
      assertEquals(path, inputOptions.getPropertiesFileLocation());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Failure : " + e);
    }
  }

}
