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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.StemProcessor;

/**
 * Class for testing creating a stem and group so that other LDAP provisioning tests will
 * have data to work on. This is really a test that Grouper methods used in other tests
 * will behave as expected, before running Ldappc tests that depend on those methods. This
 * class builds the following database structure:
 * 
 * testStem1 teststem2 teststem3 testgroup31 testgroup21
 * 
 * @author Gil Singer
 */
public class StemProcessorTest extends BaseLdappcTestCase {

  /**
   * the grouper session
   */
  private GrouperSession grouperSession;

  /**
   * the stem processor
   */
  private StemProcessor stemProcessor;

  /**
   * testStem1
   */
  private Stem testStem1;

  /**
   * testStem2
   */
  private Stem testStem2;

  /**
   * testStem3
   */
  private Stem testStem3;

  /**
   * testGroup1
   */
  private Group testGroup31;

  /**
   * testGroup2
   */
  Group testGroup21;

  /**
   * Constructor
   */
  public StemProcessorTest(String name) {
    super(name);
  }

  /**
   * Setup the fixture.
   */
  protected void setUp() {
    DisplayTest.showRunClass(getClass().getName());
    GrouperSessionControl grouperSessionControl = new GrouperSessionControl();
    boolean started = grouperSessionControl.startSession("GrouperSystem");
    if (!started) {
      fail("Could not start grouper session");
    }
    grouperSession = grouperSessionControl.getSession();

    stemProcessor = new StemProcessor(grouperSession);
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {

    //
    // Delete the stem and make sure the stem no longer exists.
    // TODO: Determine why this does not delete the Stem.
    //
    try {
      if (testStem1 != null) {
        testStem1.delete();
      }
    } catch (StemDeleteException sde) {
      // Okay if already deleted.
    } catch (InsufficientPrivilegeException ipe) {
      fail("Insufficent privilege for deleting toplevelStem1: " + ipe.getMessage());
    }

    try {
      grouperSession.stop();
    } catch (SessionException se) {
      fail("Could not stop the session: " + se.getMessage());
    }
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    BaseLdappcTestCase.runTestRunner(StemProcessorTest.class);
  }

  /**
   * A test of adding new stems to a root stem
   */
  public void testAddStemsAndGroups() {
    DisplayTest.showRunTitle("testAddNewStems", "Three stems are added.");

    // First stem is added to the root
    try {
      // If it already exists, use it.
      testStem1 = StemFinder.findByName(grouperSession, "testStem1", true);
    } catch (StemNotFoundException snfe) {
      // Stem does not exist so create it.

      // The two argument method adds to the root stem
      testStem1 = stemProcessor.addStem("testStem1", "testStem1 extension display");
      if (testStem1 == null) {
        fail("Could not create testStem1.");
      }
    }

    try {
      // If it already exists, use it.
      testStem2 = StemFinder.findByName(grouperSession, "testStem1:testStem2", true);
    } catch (StemNotFoundException snfe) {
      // Stem does not exist so create it.

      testStem2 = stemProcessor.addStem(testStem1, "testStem2",
          "stemTest2 extension display");
      if (testStem2 == null) {
        fail("Could not create testStem2.");
      }
    }

    // First argument is the stem to be added to
    testStem3 = addStem(testStem2, "testStem3", "testStem1:testStem2:testStem3",
        "stemTest3 extension display");
    /*
     * try { // If it already exists, use it. testStem3 =
     * StemFinder.findByName(grouperSession, "testStem1:testStem2:testStem3"); }
     * catch(StemNotFoundException snfe) { // Stem does not exist so create it.
     * 
     * testStem3 = stemProcessor.addStem(testStem1, "testStem3",
     * "stemTest3 extension display"); if (testStem3 == null) {
     * fail("Could not create testStem3."); } }
     */
    //
    // Make sure the stems exist.
    //
    try {
      StemFinder.findByName(grouperSession, "testStem1", true);
    } catch (StemNotFoundException snfe) {
      fail("Could not create testStem1.");
    }

    try {
      StemFinder.findByName(grouperSession, "testStem1:testStem2", true);
      StemFinder.findByName(grouperSession, "testStem1:testStem2:testStem3", true);
    } catch (StemNotFoundException snfe) {
      fail("Could not create testStem2 or testStem3.");
    }
    addGroups();
  }

  /**
   * 
   * @param baseStem
   *          stem the stem is being added to
   * @param stemExtension
   *          stem extension of the added stem
   * @param stemExtensionDisplay
   *          stem extension display of the added stem
   * @return the stem created
   */
  public Stem addStem(Stem baseStem, String stemExtension, String stemFullPath,
      String stemExtensionDisplay) {
    Stem stem = null;
    try {
      // If it already exists, use it.
      stem = StemFinder.findByName(grouperSession, stemFullPath, true);
    } catch (StemNotFoundException snfe) {
      // Stem does not exist so create it.

      stem = stemProcessor.addStem(baseStem, stemExtension, stemExtensionDisplay);
      if (stem == null) {
        fail("Could not create " + stemExtension);
      }
    }
    return stem;
  }

  /**
   * A test of adding new groups to a root stem
   */
  public void addGroups() {
    DisplayTest.showRunTitle("testAddNewGroups", "Two groups are added.");

    // Add to testStem2

    try {
      // If found, use the one that already exists; assume not an error.
      testGroup31 = GroupFinder.findByName(grouperSession,
          "testStem1:testStem2:testStem3:testGroup31", true);
    } catch (GroupNotFoundException gnfe) {
      // Normal case

      testGroup31 = stemProcessor.addGroup(testStem3, "testGroup31",
          "testGroup31 extension display");
      if (testGroup31 == null) {
        fail("Could not create testGroup31");
      }
    }

    try {
      // If found, use the one that already exists; assume not an error.
      testGroup21 = GroupFinder.findByName(grouperSession,
          "testStem1:testStem2:testGroup21", true);
    } catch (GroupNotFoundException gnfe) {
      // Normal case

      testGroup21 = stemProcessor.addGroup(testStem2, "testGroup21",
          "testGroup21 extension display");
      if (testGroup21 == null) {
        fail("Could not create testGroup21");
      }
    }
    //
    // Make sure the groups exist.
    //
    try {
      GroupFinder.findByName(grouperSession, "testStem1:testStem2:testStem3:testGroup31",
          true);
    } catch (GroupNotFoundException snfe) {
      fail("Could not create testGroup31.");
    }

    try {
      GroupFinder.findByName(grouperSession, "testStem1:testStem2:testGroup21", true);
    } catch (GroupNotFoundException snfe) {
      fail("Could not create testGroup2 or testGroup21.");
    }
  }

}
