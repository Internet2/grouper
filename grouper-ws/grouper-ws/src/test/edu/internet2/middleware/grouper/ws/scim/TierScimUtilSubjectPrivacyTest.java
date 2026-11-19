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

import com.unboundid.scim2.common.types.GroupResource;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectCustomizerBase;
import edu.internet2.middleware.grouper.ws.scim.resources.TierMembershipResource;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Issue scimCachedSubjectPrivacyAuthorization: SCIM group and membership resources must not
 * disclose a subject that the SubjectCustomizer hides from the caller, by serializing the cached
 * columns of the grouper_members row (name, subject id, member uuid).
 *
 * <p>Setup as root: group test:scimPrivacy:group with members SUBJ1 (hidden from non-wheel
 * callers by {@link HideSubject1Customizer}) and SUBJ2; SUBJ0 has READ on the group. The subject
 * cache is primed as root first, so the test also proves root-resolved data is not reused for
 * the caller.</p>
 *
 * <p>Before the fix the SCIM group lists SUBJ1 with its cached name, and the membership
 * resource of SUBJ1 carries SUBJ1's id and cached name, so the SUBJ0 assertions fail.</p>
 */
public class TierScimUtilSubjectPrivacyTest extends GrouperTest {

  /** group used by every test */
  private static final String GROUP_NAME = "test:scimPrivacy:group";

  /**
   * Test customizer: hides jdbc / test.subject.1 entirely from anyone who is not wheel or root.
   * Public static with a no-arg constructor because the customizer is instantiated by class name.
   */
  public static class HideSubject1Customizer extends SubjectCustomizerBase {

    /**
     * @see SubjectCustomizerBase#filterSubjects(GrouperSession, Set, String)
     */
    @Override
    public Set<Subject> filterSubjects(GrouperSession grouperSession, Set<Subject> subjects,
        String findSubjectsInStemName) {
      if (subjects == null || grouperSession == null
          || PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
        return subjects;
      }
      Set<Subject> result = new LinkedHashSet<Subject>();
      for (Subject subject : subjects) {
        if (subject != null && SubjectTestHelper.SUBJ1_ID.equals(subject.getId())
            && "jdbc".equals(subject.getSourceId())) {
          continue;
        }
        result.add(subject);
      }
      return result;
    }
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TierScimUtilSubjectPrivacyTest("testGroupHidesProtectedMember"));
  }

  /**
   *
   */
  public TierScimUtilSubjectPrivacyTest() {
    super();
  }

  /**
   * @param name
   */
  public TierScimUtilSubjectPrivacyTest(String name) {
    super(name);
  }

  /** the group */
  private Group group;

  /** member row of the hidden subject */
  private Member member1;

  /** member row of the visible subject */
  private Member member2;

  /**
   * create the group as root, prime the subject cache as root, then turn on the customizer
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      this.group = new GroupSave(rootSession).assignName(GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      this.group.addMember(SubjectTestHelper.SUBJ1, false);
      this.group.addMember(SubjectTestHelper.SUBJ2, false);
      this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.member1 = MemberFinder.findBySubject(rootSession, SubjectTestHelper.SUBJ1, true);
      this.member2 = MemberFinder.findBySubject(rootSession, SubjectTestHelper.SUBJ2, true);

      // the cached name the leak would disclose really is in grouper_members
      assertEquals(SubjectTestHelper.SUBJ1_NAME, this.member1.getName());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("subjects.customizer.className",
        HideSubject1Customizer.class.getName());
    SubjectFinder.internalClearSubjectCustomizerCache();

    // prime the subject cache as root (root sees SUBJ1)
    rootSession = GrouperSession.startRootSession();
    try {
      assertNotNull(SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ1_ID, "jdbc", true));
      assertEquals(2, TierScimUtil.convertGrouperGroupToScimGroup(this.group).getMembers().size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * turn the customizer off again
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("subjects.customizer.className");
    SubjectFinder.internalClearSubjectCustomizerCache();
    super.tearDown();
  }

  /**
   * As SUBJ0 (READ on the group), the normal subject lookup hides SUBJ1, and the SCIM group
   * resource must not list SUBJ1 by ref, member uuid or name; SUBJ2 is listed normally.
   */
  public void testGroupHidesProtectedMember() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertNull("customizer hides SUBJ1 from SUBJ0",
          SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ1_ID, "jdbc", false));

      GroupResource groupResource = TierScimUtil.convertGrouperGroupToScimGroup(this.group);
      List<com.unboundid.scim2.common.types.Member> members = groupResource.getMembers();
      assertEquals("only the visible member is listed", 1, members.size());

      for (com.unboundid.scim2.common.types.Member member : members) {
        assertFalse(String.valueOf(member.getRef()).contains(SubjectTestHelper.SUBJ1_ID));
        assertFalse(this.member1.getId().equals(member.getValue()));
        assertFalse(SubjectTestHelper.SUBJ1_NAME.equals(member.getDisplay()));
      }

      com.unboundid.scim2.common.types.Member visible = members.get(0);
      assertEquals("../Users/" + SubjectTestHelper.SUBJ2_ID, String.valueOf(visible.getRef()));
      assertEquals(this.member2.getId(), visible.getValue());
      assertEquals(SubjectTestHelper.SUBJ2_NAME, visible.getDisplay());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * SCIM user resources list only groups the caller can READ (advisory M4, 2026-11-10)
   */
  public void testUserResourceListsOnlyReadableGroups() {
    // a private group SUBJ2 is in, which SUBJ0 cannot see (advisory M4, 2026-11-10)
    Group privateGroup = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      privateGroup = new GroupSave(rootSession).assignName(GROUP_NAME + "Private")
          .assignCreateParentStemsIfNotExist(true).save();
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      privateGroup.addMember(SubjectTestHelper.SUBJ2, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      edu.internet2.middleware.subject.Subject subj2 = SubjectFinder.findByIdAndSource(SubjectTestHelper.SUBJ2_ID, "jdbc", true);
      com.unboundid.scim2.common.types.UserResource userResource = TierScimUtil.convertGrouperMemberToScimUser(grouperSession, subj2);
      java.util.Set<String> groupNames = new java.util.HashSet<String>();
      for (com.unboundid.scim2.common.types.Group scimGroup : userResource.getGroups()) {
        groupNames.add(scimGroup.getValue());
      }
      assertTrue("the readable group is listed: " + groupNames, groupNames.contains(GROUP_NAME));
      assertFalse("a group SUBJ0 cannot read is not listed: " + groupNames, groupNames.contains(GROUP_NAME + "Private"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * As root and as a wheel member the customizer hides nothing, so both members are listed
   * with their real names (the fix does not drop visible members).
   */
  public void testRootSeesAllMembers() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      List<com.unboundid.scim2.common.types.Member> members =
          TierScimUtil.convertGrouperGroupToScimGroup(this.group).getMembers();
      assertEquals(2, members.size());
      Set<String> displays = new LinkedHashSet<String>();
      for (com.unboundid.scim2.common.types.Member member : members) {
        displays.add(member.getDisplay());
      }
      assertTrue(displays.contains(SubjectTestHelper.SUBJ1_NAME));
      assertTrue(displays.contains(SubjectTestHelper.SUBJ2_NAME));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * As SUBJ0 the membership resource of the hidden subject carries no ref, value or display;
   * the membership resource of the visible subject carries its id and name.
   */
  public void testMembershipHidesProtectedMember() {
    Membership membership1;
    Membership membership2;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      membership1 = MembershipFinder.findImmediateMembership(rootSession, this.group,
          SubjectTestHelper.SUBJ1, true);
      membership2 = MembershipFinder.findImmediateMembership(rootSession, this.group,
          SubjectTestHelper.SUBJ2, true);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      TierMembershipResource resource1 = TierScimUtil.convertGrouperMembershipToScimResource(membership1);
      com.unboundid.scim2.common.types.Member hidden = resource1.getMember();
      assertNotNull(hidden);
      assertNull(hidden.getRef());
      assertNull(hidden.getValue());
      assertNull(hidden.getDisplay());

      TierMembershipResource resource2 = TierScimUtil.convertGrouperMembershipToScimResource(membership2);
      com.unboundid.scim2.common.types.Member visible = resource2.getMember();
      assertEquals("../Users/" + SubjectTestHelper.SUBJ2_ID, String.valueOf(visible.getRef()));
      assertEquals(SubjectTestHelper.SUBJ2_ID, visible.getValue());
      assertEquals(SubjectTestHelper.SUBJ2_NAME, visible.getDisplay());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
