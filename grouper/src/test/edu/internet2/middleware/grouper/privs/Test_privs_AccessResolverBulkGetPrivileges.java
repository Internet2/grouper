/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.privs;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Tests for the batched {@link AccessResolver#getPrivileges(java.util.Collection, Subject, Set)}.
 * The key contract is that the batched result matches the per-row {@code group.canHavePrivilege(...)}
 * for the access-resolver chain, across a mix of privileges and across multiple stems.
 */
public class Test_privs_AccessResolverBulkGetPrivileges extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_privs_AccessResolverBulkGetPrivileges("testBulkMatchesPerRow"));
  }

  /**
   * @param name
   */
  public Test_privs_AccessResolverBulkGetPrivileges(String name) {
    super(name);
  }

  /**
   * The GrouperTest base sets groups.create.grant.all.read=true, which auto-grants GrouperAll READ
   * on every group as it is created. That would make the explicit gAllRead.grantPriv(GrouperAll,
   * READ) below a duplicate that throws GrantPrivilegeAlreadyExistsException. Turn it off so this
   * test controls GrouperAll read explicitly per its scenario.
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
  }

  /** the privileges the UI can* getters resolve */
  private static final Set<Privilege> PRIVS_TO_CHECK = GrouperUtil.toSet(
      AccessPrivilege.READ, AccessPrivilege.UPDATE, AccessPrivilege.ADMIN, AccessPrivilege.GROUP_ATTR_READ);

  /**
   * batched result must match per-row canHavePrivilege for a mix of privileges spread across MULTIPLE
   * stems (reader, updater, admin-only, GrouperAll-read, and a group with nothing).
   */
  public void testBulkMatchesPerRow() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    Stem root = StemFinder.findRootStem(rootSession);
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");

    //groups spread across two stems
    Group gReader = stem1.addChildGroup("gReader", "gReader");
    Group gUpdater = stem1.addChildGroup("gUpdater", "gUpdater");
    Group gAdmin = stem2.addChildGroup("gAdmin", "gAdmin");
    Group gAllRead = stem2.addChildGroup("gAllRead", "gAllRead");
    Group gNothing = stem2.addChildGroup("gNothing", "gNothing");

    Subject subj0 = SubjectTestHelper.SUBJ0;

    gReader.grantPriv(subj0, AccessPrivilege.READ);
    gUpdater.grantPriv(subj0, AccessPrivilege.UPDATE);
    gAdmin.grantPriv(subj0, AccessPrivilege.ADMIN);
    gAllRead.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ);

    rootSession.stop();

    GrouperSession session = GrouperSession.start(subj0);
    try {
      Set<Group> groups = GrouperUtil.toSet(gReader, gUpdater, gAdmin, gAllRead, gNothing);

      Map<Group, Set<Privilege>> held = session.getAccessResolver().getPrivileges(
          groups, subj0, PRIVS_TO_CHECK);

      //every requested group must be present
      assertEquals("entry per group", 5, held.size());

      //parity with the per-row path for every group and every capability
      for (Group group : groups) {
        assertCapabilityParity(group, subj0, held.get(group), AccessPrivilege.READ, AccessPrivilege.ADMIN);
        assertCapabilityParity(group, subj0, held.get(group), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);
        assertCapabilityParity(group, subj0, held.get(group), AccessPrivilege.ADMIN);
        assertCapabilityParity(group, subj0, held.get(group), AccessPrivilege.GROUP_ATTR_READ, AccessPrivilege.ADMIN);
      }

      //explicit expectations
      //plain reader: read yes, update no, admin no
      assertCapability(true, held.get(gReader), AccessPrivilege.READ, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gReader), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gReader), AccessPrivilege.ADMIN);

      //plain updater: update yes; update does NOT imply read, so read no; admin no
      assertCapability(true, held.get(gUpdater), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gUpdater), AccessPrivilege.READ, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gUpdater), AccessPrivilege.ADMIN);

      //admin-only: admin yes, and read+update both true via implication on the can* getters
      assertCapability(true, held.get(gAdmin), AccessPrivilege.ADMIN);
      assertCapability(true, held.get(gAdmin), AccessPrivilege.READ, AccessPrivilege.ADMIN);
      assertCapability(true, held.get(gAdmin), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);

      //GrouperAll READ: subj0 is not a direct member but should get read via GrouperAll
      assertCapability(true, held.get(gAllRead), AccessPrivilege.READ, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gAllRead), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);

      //nothing granted
      assertCapability(false, held.get(gNothing), AccessPrivilege.READ, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gNothing), AccessPrivilege.UPDATE, AccessPrivilege.ADMIN);
      assertCapability(false, held.get(gNothing), AccessPrivilege.ADMIN);

    } finally {
      session.stop();
    }
  }

  /**
   * root (sysadmin) gets every requested access privilege on every group.
   */
  public void testBulkRootGetsAll() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    Stem root = StemFinder.findRootStem(rootSession);
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Group g1 = stem1.addChildGroup("g1", "g1");
    Group g2 = stem1.addChildGroup("g2", "g2");

    try {
      Set<Group> groups = GrouperUtil.toSet(g1, g2);
      Map<Group, Set<Privilege>> held = rootSession.getAccessResolver().getPrivileges(
          groups, rootSession.getSubject(), PRIVS_TO_CHECK);

      for (Group group : groups) {
        Set<Privilege> heldForGroup = held.get(group);
        assertTrue("root read " + group.getName(), heldForGroup.contains(AccessPrivilege.READ));
        assertTrue("root update " + group.getName(), heldForGroup.contains(AccessPrivilege.UPDATE));
        assertTrue("root admin " + group.getName(), heldForGroup.contains(AccessPrivilege.ADMIN));
        assertTrue("root groupAttrRead " + group.getName(), heldForGroup.contains(AccessPrivilege.GROUP_ATTR_READ));
      }
    } finally {
      rootSession.stop();
    }
  }

  /**
   * assert that the batched "holds any of capabilityPrivileges" matches the per-row
   * group.canHavePrivilege for the primary (first) privilege of the capability.
   * @param group
   * @param subject
   * @param heldForGroup batched result for the group
   * @param capabilityPrivileges the privileges any of which grants the capability (primary first)
   */
  private void assertCapabilityParity(Group group, Subject subject, Set<Privilege> heldForGroup,
      Privilege... capabilityPrivileges) {

    boolean bulk = false;
    for (Privilege capabilityPrivilege : capabilityPrivileges) {
      if (heldForGroup != null && heldForGroup.contains(capabilityPrivilege)) {
        bulk = true;
        break;
      }
    }

    boolean perRow = group.canHavePrivilege(subject, capabilityPrivileges[0].getName(), false);

    assertEquals("bulk vs per-row for " + capabilityPrivileges[0].getName() + " on " + group.getName(),
        perRow, bulk);
  }

  /**
   * assert the batched "holds any of capabilityPrivileges" equals expected.
   * @param expected
   * @param heldForGroup
   * @param capabilityPrivileges
   */
  private void assertCapability(boolean expected, Set<Privilege> heldForGroup, Privilege... capabilityPrivileges) {
    boolean bulk = false;
    for (Privilege capabilityPrivilege : capabilityPrivileges) {
      if (heldForGroup != null && heldForGroup.contains(capabilityPrivilege)) {
        bulk = true;
        break;
      }
    }
    assertEquals(expected, bulk);
  }
}
