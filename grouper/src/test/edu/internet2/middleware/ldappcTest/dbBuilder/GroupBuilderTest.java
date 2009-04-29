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

package edu.internet2.middleware.ldappcTest.dbBuilder;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappcTest.BaseLdappcTestCase;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * Class for testing creating a stem and group so that other LDAP provisioning tests will
 * have data to work on. This is really a test that Grouper methods used in other tests
 * will behave as expected, before running Ldappc tests that depend on those methods.
 * 
 * @author Gil Singer
 */
public class GroupBuilderTest extends BaseLdappcTestCase {

  /**
   * the grouper session
   */
  private GrouperSession grouperSession;

  /**
   * the root stem
   */
  private Stem rootStem;

  /**
   * topLevelStem1
   */
  private Stem topLevelStem1;

  /**
   * topLevelGroup1
   */
  private Group topLevelGroup1;

  /**
   * Constructor
   */
  public GroupBuilderTest(String name) {
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

    // Find root stem.
    rootStem = StemFinder.findRootStem(grouperSession);
  }

  /**
   * Tear down the fixture.
   */
  protected void tearDown() {
    //
    // Delete the stem and make sure the stem no longer exists.
    // TODO: Determine why this does not delete the Stem.
    //
    /*
     * try { if (topLevelStem1 != null) {
     * System.out.println("DEBUG in GroupBuilderTest, topLevelStem1.getName()=" +
     * topLevelStem1.getName() );
     * System.out.println("DEBUG in GroupBuilderTest at delete, uuid=" +
     * topLevelStem1.getUuid()); topLevelStem1.delete(); } } catch (StemDeleteException
     * sde) { System.out.println("DEBUG in GroupBuilderTest in StemDeleteException" +
     * sde.getMessage()); // Okay if already deleted. } catch
     * (InsufficientPrivilegeException ipe) {
     * fail("Insufficent privilege for deleting toplevelStem1: " + ipe.getMessage()); }
     */

    try {
      Group group1 = GroupFinder.findByName(grouperSession,
          "topLevelStem1:topLevelGroup1", true);
      group1.delete();
    } catch (GroupNotFoundException gnfe) {
      // System.out.println("DEBUG in GroupBuilderTest in GroupNotFoundException: " +
      // gnfe.getMessage());
      // Okay if already deleted.
    } catch (GroupDeleteException gde) {
      // System.out.println("DEBUG in GroupBuilderTest in GroupDeleteException" +
      // gde.getMessage());
      // Okay if already deleted.
    } catch (InsufficientPrivilegeException ipe) {
      fail("Insufficent privilege for deleting topLevelGroup1: " + ipe.getMessage());
    }

    try {
      Stem stem1 = StemFinder.findByName(grouperSession, "topLevelStem1", true);
      stem1.delete();
    } catch (StemNotFoundException snfe) {
      // System.out.println("DEBUG in GroupBuilderTest in StemNotFoundException: " +
      // snfe.getMessage());
      // Okay if already deleted.
    } catch (StemDeleteException sde) {
      // System.out.println("DEBUG in GroupBuilderTest in StemDeleteException: " +
      // sde.getMessage());
      // Okay if already deleted.
    } catch (InsufficientPrivilegeException ipe) {
      fail("Insufficent privilege for deleting topLevelStem1 or topLevelGroup1: "
          + ipe.getMessage());
    } finally {
      try {
        grouperSession.stop();
      } catch (SessionException se) {
        fail("Could not stop the session: " + se.getMessage());
      }
    }
  }

  /**
   * The main method for running the test.
   */
  public static void main(String args[]) {
    BaseLdappcTestCase.runTestRunner(GroupBuilderTest.class);
  }

  /**
   * A test a new root stem was created and that topLevelStem1 does not yet exist.
   */
  public void testGrouperRootStem() {
    DisplayTest.showRunTitle("testGrouperRootStem", "RootStem is retrieved.");

    assertNotNull("root stem not found", rootStem);

    // Stem topLevelStem1 = null;

    /*
     * try { grouperSession.stop(); } catch (SessionException se) {
     * fail("Could not stop session." + se.getMessage()); }
     */
  }

  /**
   * A test of adding a stem to a root stem
   */
  public void testAddNewStemToRootStem() {
    DisplayTest.showRunTitle("testAddNewStemToRootStem",
        "New top level stem is added then deleted.");

    deleteTopLevel1Entries();
    try {
      topLevelStem1 = rootStem.addChildStem("topLevelStem1", "Top Level Stem 1");
    } catch (InsufficientPrivilegeException ipe) {
      fail("Insufficent privilege for creating stem off of the root stem"
          + ipe.getMessage());
    } catch (StemAddException sae) {
      fail("Could not add stem topLevelStem1 to root stem: " + sae.getMessage());
    }
    assertEquals("topLevelStem1", topLevelStem1.getName());

    //
    // Make sure the stem exists now.
    //
    try {
      topLevelStem1 = StemFinder.findByName(grouperSession, "topLevelStem1", true);
    } catch (StemNotFoundException snfe) {
      fail("topLevelStem1 does not exist.");
    }

    //
    // Create a topLevelGroup1 group
    //

    try {
      topLevelGroup1 = topLevelStem1.addChildGroup("topLevelGroup1", "Top Level Group 1");
    } catch (GroupAddException gae) {
      fail("Could not add topLevelGroup1 Group: " + gae.getMessage());
    } catch (InsufficientPrivilegeException ipe) {
      fail("You do not have privileges to add the group" + ipe.getMessage());
    }

    //
    // Now delete the group.
    //

    try {
      topLevelGroup1.delete();
    } catch (GroupDeleteException gae) {
      fail("Could not delete Group: " + gae.getMessage());
    } catch (InsufficientPrivilegeException ipe) {
      fail("You do not have privileges to delete the group" + ipe.getMessage());
    }

    //
    // Delete the stem and make sure the stem no longer exists.
    //
    try {
      topLevelStem1.delete();
    } catch (StemDeleteException sde) {
      fail("Could not delete topLevelStem1: " + sde.getMessage());
    } catch (InsufficientPrivilegeException ipe) {
      fail("Insufficent privilege for deleting toplevelStem1: " + ipe.getMessage());
    }

    try {
      topLevelStem1 = StemFinder.findByName(grouperSession, "topLevelStem1", true);
      fail("topLevelStem1 exists but should have been deleted.");
    } catch (StemNotFoundException snfe) {
      // This is the normal case when the stem does not exist.
    }

  }

  /**
   * Delete topLevelStem1 if it exists.
   */
  public void deleteTopLevel1Entries() {
    // Test finding topLevelStem1 stem (not yet created).

    try {
      topLevelStem1 = StemFinder.findByName(grouperSession, "topLevelStem1", true);
      if (topLevelStem1 != null) {
        try {
          topLevelGroup1 = GroupFinder.findByName(grouperSession,
              "topLevelStem1:topLevelGroup1", true);
          try {
            //
            // Delete the group and make sure the stem no longer exists.
            //
            topLevelGroup1.delete();
          } catch (GroupDeleteException gde) {
            // System.out.println("GroupDeleteException: " + gde.getMessage());
            // Okay if we can not delete because it does not exist.
          } catch (InsufficientPrivilegeException ipe) {
            fail("Insufficent privilege for deleting toplevelGroup1: " + ipe.getMessage());
          }
        } catch (GroupNotFoundException gnfe) {
          // This is the normal case if the group does not exist.
          // System.out.println("GroupNotFoundException: " + gnfe.getMessage());
        }
        try {
          //
          // Delete the stem and make sure the stem no longer exists.
          //
          topLevelStem1.delete();
        } catch (StemDeleteException sde) {
          // Okay if we can not delete because it does not exist.
          // System.out.println("StemDeleteException: " + sde.getMessage());
        } catch (InsufficientPrivilegeException ipe) {
          fail("Insufficent privilege for deleting toplevelStem1: " + ipe.getMessage());
        }
      }

    } catch (StemNotFoundException snfe) {
      // This is the normal case; should not exist yet.
    }

  }

}
