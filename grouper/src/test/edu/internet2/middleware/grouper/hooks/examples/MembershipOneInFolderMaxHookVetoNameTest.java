/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.hooks.examples;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 * The MembershipOneInFolderMaxHook veto reason must not name a group in the folder that the
 * calling subject cannot VIEW (security advisory L1).  The hook finds the other memberships as
 * GrouperSystem, then tries to remove them as the caller; when that fails the veto reason used to
 * name the group, which disclosed a hidden group and that the added subject is in it.
 * <p>
 * Same file for every release (2.6.19, 4.x, 5.x, 6.x, 7.x): GrouperTest base, Java 8, and the hook
 * registered with a hook override like MembershipOneInFolderMaxHookTest.
 * </p>
 */
public class MembershipOneInFolderMaxHookVetoNameTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipOneInFolderMaxHookVetoNameTest("testVetoReasonHidesGroupCallerCannotView"));
  }

  /**
   *
   */
  public MembershipOneInFolderMaxHookVetoNameTest() {

  }

  /**
   * @param testName
   */
  public MembershipOneInFolderMaxHookVetoNameTest(String testName) {
    super(testName);
  }

  /**
   * SUBJ0 is ADMIN of test1:openGroup in a one-membership folder.  SUBJ2 is in test1:secretGroup,
   * which SUBJ0 cannot see; SUBJ3 is in test1:visibleGroup, which SUBJ0 can VIEW but not UPDATE.
   * Adding either subject to openGroup as SUBJ0 is vetoed (SUBJ0 cannot remove the other membership);
   * the reason names visibleGroup but not secretGroup.
   */
  public void testVetoReasonHidesGroupCallerCannotView() {

    // ---- setup as root
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      //let the test subjects read and assign the folder attribute, like MembershipOneInFolderMaxHookTest
      MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate()
        .grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
      MembershipOneInFolderMaxHook.membershipOneInFolderAttributeDefName().getAttributeDef().getPrivilegeDelegate()
        .grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);

      Stem testStem1 = new StemSave(rootSession).assignName("test1").save();

      //hidden from SUBJ0: GrouperAll has no READ or VIEW
      Group secretGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test1:secretGroup").save();
      secretGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      secretGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      secretGroup.addMember(SubjectTestHelper.SUBJ2, false);

      //visible to SUBJ0 (explicit VIEW), but SUBJ0 cannot UPDATE it
      Group visibleGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test1:visibleGroup").save();
      visibleGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      visibleGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      visibleGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      visibleGroup.addMember(SubjectTestHelper.SUBJ3, false);

      //the caller's own group
      Group openGroup = new GroupSave(rootSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test1:openGroup").save();
      openGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      //turn on the hook for the folder after the memberships are in place
      MembershipOneInFolderMaxHook.assignMembershipOneInFolderAttributeDefName(testStem1);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // ---- as SUBJ0, the caller
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {

      //precondition: SUBJ0 cannot see the secret group
      assertNull("SUBJ0 must not be able to see test1:secretGroup",
          GroupFinder.findByName(subj0Session, "test1:secretGroup", false));
      assertNotNull("SUBJ0 must be able to see test1:visibleGroup",
          GroupFinder.findByName(subj0Session, "test1:visibleGroup", false));

      Group openGroup = GroupFinder.findByName(subj0Session, "test1:openGroup", true);

      //hidden group: vetoed, and the reason does not name it
      try {
        openGroup.addMember(SubjectTestHelper.SUBJ2);
        fail("Should veto, SUBJ0 cannot remove SUBJ2 from test1:secretGroup");
      } catch (HookVeto hv) {
        assertEquals(MembershipOneInFolderMaxHook.HOOK_VETO_MEMBERSHIP_ONE_IN_FOLDER_CANT_DELETE_MEMBER, hv.getReasonKey());
        String reason = hv.getReason();
        assertFalse("veto reason must not name a group the caller cannot view: " + reason,
            reason != null && reason.contains("secretGroup"));
        assertTrue("veto reason should use the generic phrase: " + reason,
            reason != null && reason.contains("another group in this folder"));
      }

      //visible group: vetoed, and the reason still names it
      try {
        openGroup.addMember(SubjectTestHelper.SUBJ3);
        fail("Should veto, SUBJ0 cannot remove SUBJ3 from test1:visibleGroup");
      } catch (HookVeto hv) {
        assertEquals(MembershipOneInFolderMaxHook.HOOK_VETO_MEMBERSHIP_ONE_IN_FOLDER_CANT_DELETE_MEMBER, hv.getReasonKey());
        String reason = hv.getReason();
        assertTrue("veto reason should name a group the caller can view: " + reason,
            reason != null && reason.contains("test1:visibleGroup"));
      }

    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    // ---- nothing changed: the vetoed adds did not happen and the other memberships stayed
    rootSession = GrouperSession.startRootSession();
    try {
      Group openGroup = GroupFinder.findByName(rootSession, "test1:openGroup", true);
      assertFalse(openGroup.hasMember(SubjectTestHelper.SUBJ2));
      assertFalse(openGroup.hasMember(SubjectTestHelper.SUBJ3));
      assertTrue(GroupFinder.findByName(rootSession, "test1:secretGroup", true).hasMember(SubjectTestHelper.SUBJ2));
      assertTrue(GroupFinder.findByName(rootSession, "test1:visibleGroup", true).hasMember(SubjectTestHelper.SUBJ3));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * register the hook and reset the registry, like MembershipOneInFolderMaxHookTest
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    try {

      GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(),
        MembershipOneInFolderMaxHook.class);

      EhcacheController.ehcacheController().flushCache();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();

      //clears the folder cache, and on 2.6.19 / 5.0-5.7 also the init flag for the attribute
      MembershipOneInFolderMaxHook.clearHook();

      GrouperTest.initGroupsAndAttributes();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * unregister the hook
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperHookType.addHookOverride(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), (Class<?>)null);
    MembershipOneInFolderMaxHook.clearHook();
  }

}
