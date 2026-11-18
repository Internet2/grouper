/****
 * Copyright 2026 Internet2
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.unboundid.scim2.common.types.UserResource;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Issue scimUserHiddenGroupMembershipDisclosure: the SCIM User resource (GET /Users/{id} and the
 * id / identifier filters, which all call TierScimUtil.convertGrouperMemberToScimUser) must list
 * only the groups whose membership the caller may READ (READ or ADMIN on the group).
 *
 * <p>Setup as root, under test:scimUserGroups: SUBJ2 is an immediate member of every group below.
 * SUBJ0 is the caller.</p>
 * <ul>
 *   <li>readable: SUBJ0 has READ</li>
 *   <li>adminable: SUBJ0 has ADMIN</li>
 *   <li>viewOnly: SUBJ0 has VIEW only</li>
 *   <li>updateOnly: SUBJ0 has UPDATE only (UPDATE does not imply READ)</li>
 *   <li>hidden: SUBJ0 has nothing</li>
 *   <li>hiddenOuter: SUBJ0 has nothing; its member is group readable, so SUBJ2 is an effective member</li>
 *   <li>readableOuter: SUBJ0 has READ; its member is group hidden, so SUBJ2 is an effective member</li>
 * </ul>
 * <p>GrouperAll has no VIEW or READ on any of them.</p>
 *
 * <p>Before the fix GroupFinder ran with no privileges (no filter at all), so the caller got every
 * group of SUBJ2 (name, uuid, display extension) and the "not listed" assertions fail.</p>
 */
public class TierScimUserGroupsPrivilegeTest extends GrouperTest {

  /** folder of the test groups */
  private static final String STEM = "test:scimUserGroups";

  /** SUBJ0 has READ */
  private static final String READABLE = STEM + ":readable";

  /** SUBJ0 has ADMIN */
  private static final String ADMINABLE = STEM + ":adminable";

  /** SUBJ0 has VIEW only */
  private static final String VIEW_ONLY = STEM + ":viewOnly";

  /** SUBJ0 has UPDATE only */
  private static final String UPDATE_ONLY = STEM + ":updateOnly";

  /** SUBJ0 has no privilege */
  private static final String HIDDEN = STEM + ":hidden";

  /** SUBJ0 has no privilege, SUBJ2 is an effective member (through READABLE) */
  private static final String HIDDEN_OUTER = STEM + ":hiddenOuter";

  /** SUBJ0 has READ, SUBJ2 is an effective member (through HIDDEN) */
  private static final String READABLE_OUTER = STEM + ":readableOuter";

  /** uuids of the groups the caller must not see, to check refs as well as names */
  private Set<String> hiddenUuids = new LinkedHashSet<String>();

  /** display extensions of the groups the caller must not see */
  private Set<String> hiddenDisplayExtensions = new LinkedHashSet<String>();

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TierScimUserGroupsPrivilegeTest("testCallerSeesOnlyReadableGroups"));
  }

  /**
   *
   */
  public TierScimUserGroupsPrivilegeTest() {
    super();
  }

  /**
   * @param name
   */
  public TierScimUserGroupsPrivilegeTest(String name) {
    super(name);
  }

  /**
   * create the groups and privileges as root
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // open a root session for the API writes (6.x and older assert without a session)
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group readable = saveGroup(rootSession, READABLE, SubjectTestHelper.SUBJ2);
      readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

      Group adminable = saveGroup(rootSession, ADMINABLE, SubjectTestHelper.SUBJ2);
      adminable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      Group viewOnly = saveGroup(rootSession, VIEW_ONLY, SubjectTestHelper.SUBJ2);
      viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

      Group updateOnly = saveGroup(rootSession, UPDATE_ONLY, SubjectTestHelper.SUBJ2);
      updateOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);

      Group hidden = saveGroup(rootSession, HIDDEN, SubjectTestHelper.SUBJ2);

      // nested: SUBJ2 is an effective member of both outer groups
      Group hiddenOuter = saveGroup(rootSession, HIDDEN_OUTER, readable.toSubject());

      Group readableOuter = saveGroup(rootSession, READABLE_OUTER, hidden.toSubject());
      readableOuter.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

      for (Group group : new Group[] {viewOnly, updateOnly, hidden, hiddenOuter}) {
        this.hiddenUuids.add(group.getUuid());
        this.hiddenDisplayExtensions.add(group.getDisplayExtension());
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * save a group with one immediate member and no GrouperAll VIEW or READ
   * @param rootSession
   * @param name
   * @param member SUBJ2, or a group subject for a nested (effective) membership
   * @return the group
   */
  private static Group saveGroup(GrouperSession rootSession, String name, Subject member) {
    Group group = new GroupSave(rootSession).assignName(name)
        .assignCreateParentStemsIfNotExist(true).save();
    // do not depend on the create-time GrouperAll defaults of the test config
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    group.addMember(member, false);
    return group;
  }

  /**
   * convert SUBJ2 to a SCIM user as the given caller, the way TierUserProvider does
   * @param caller
   * @return the scim user resource
   */
  private static UserResource scimUserOfSubj2(Subject caller) {
    GrouperSession grouperSession = GrouperSession.start(caller);
    try {
      Subject subj2 = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true);
      return TierScimUtil.convertGrouperMemberToScimUser(grouperSession, subj2);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param userResource
   * @return the group names (SCIM value) in the user resource's groups
   */
  private static Set<String> groupNames(UserResource userResource) {
    Set<String> names = new LinkedHashSet<String>();
    List<com.unboundid.scim2.common.types.Group> groups = userResource.getGroups();
    if (groups != null) {
      for (com.unboundid.scim2.common.types.Group group : groups) {
        names.add(group.getValue());
      }
    }
    return names;
  }

  /**
   * As SUBJ0: groups with READ or ADMIN are listed (immediate and effective membership);
   * groups with VIEW only, UPDATE only or no privilege are not, by name, uuid ref or display
   * extension.
   */
  public void testCallerSeesOnlyReadableGroups() {
    UserResource userResource = scimUserOfSubj2(SubjectTestHelper.SUBJ0);
    Set<String> names = groupNames(userResource);

    assertTrue("READ group listed: " + names, names.contains(READABLE));
    assertTrue("ADMIN group listed: " + names, names.contains(ADMINABLE));
    assertTrue("READ group with effective membership listed: " + names, names.contains(READABLE_OUTER));

    assertFalse("VIEW only group not listed: " + names, names.contains(VIEW_ONLY));
    assertFalse("UPDATE only group not listed: " + names, names.contains(UPDATE_ONLY));
    assertFalse("group without privilege not listed: " + names, names.contains(HIDDEN));
    assertFalse("group without privilege, effective membership, not listed: " + names,
        names.contains(HIDDEN_OUTER));

    // nothing else of a removed group leaks through the ref or display either
    for (com.unboundid.scim2.common.types.Group group : userResource.getGroups()) {
      String ref = String.valueOf(group.getRef());
      for (String uuid : this.hiddenUuids) {
        assertFalse("hidden uuid in ref " + ref, ref.contains(uuid));
      }
      if (group.getValue() != null && group.getValue().startsWith(STEM + ":")) {
        assertFalse("hidden display extension " + group.getDisplay(),
            this.hiddenDisplayExtensions.contains(group.getDisplay()));
      }
    }
  }

  /**
   * As root every group of SUBJ2 is listed, immediate and effective (the fix does not drop
   * groups for a caller who can read them all).
   */
  public void testRootSeesAllGroups() {
    Set<String> names = groupNames(scimUserOfSubj2(SubjectFinder.findRootSubject()));
    for (String name : new String[] {READABLE, ADMINABLE, VIEW_ONLY, UPDATE_ONLY, HIDDEN,
        HIDDEN_OUTER, READABLE_OUTER}) {
      assertTrue(name + " listed for root: " + names, names.contains(name));
    }
  }

  /**
   * A subject looking at its own SCIM user gets the same rule: SUBJ2 has no privilege on the
   * test groups, so none of them is listed (membership alone does not grant READ).
   */
  public void testSelfSeesOnlyReadableGroups() {
    Set<String> names = groupNames(scimUserOfSubj2(SubjectTestHelper.SUBJ2));
    for (String name : names) {
      assertFalse("own membership in an unreadable group not listed: " + names,
          name.startsWith(STEM + ":"));
    }
  }

  /**
   * A caller with no privilege on any of the groups gets an empty or unrelated groups list,
   * never one of the test groups.
   */
  public void testUnprivilegedCallerSeesNoGroups() {
    Set<String> names = groupNames(scimUserOfSubj2(SubjectTestHelper.SUBJ3));
    for (String name : names) {
      assertFalse("no test group for an unprivileged caller: " + names, name.startsWith(STEM + ":"));
    }
  }
}
