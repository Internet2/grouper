/**
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
 */
package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M6 (deprovisioning UI guardrails): the deprovision user submit
 * (UiV2Deprovisioning.deprovisionUserDeprovisionSubmit) now goes through
 * GrouperDeprovisioningAllowedAccess.deprovisionUser, which only acts on the membership ids the
 * results page offered for the member and affiliation, applies the guardrails, and does
 * everything in one transaction.</p>
 *
 * <p>Before the fix this class does not compile (GrouperDeprovisioningAllowedAccess is new), and
 * the old submit removed any membership uuid it was given.</p>
 *
 * <p>Setup: affiliation "test", SUBJ0 is a manager of it (not wheel), SUBJ1 is the person being
 * deprovisioned.</p>
 */
public class GrouperDeprovisioningAllowedAccessTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningAllowedAccessTest("testAllowedRowsRemovedOnHiddenObject"));
  }

  /**
   */
  public GrouperDeprovisioningAllowedAccessTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperDeprovisioningAllowedAccessTest(String name) {
    super(name);
  }

  /** root session for setup and asserts */
  private GrouperSession rootSession;

  /** the affiliation */
  private GrouperDeprovisioningAffiliation affiliation;

  /**
   * deprovisioning on with one affiliation
   */
  @Override
  protected void setupConfigs() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "test");
  }

  /**
   * make the affiliation groups and the manager
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    this.rootSession = GrouperSession.startRootSession();
    this.affiliation = GrouperDeprovisioningAffiliation.retrieveAllAffiliations().get("test");
    assertNotNull(this.affiliation);

    // the affiliation groups are usually made by GrouperCheckConfig, make sure they exist
    Group managers = new GroupSave(this.rootSession).assignName(this.affiliation.getManagersGroupName())
        .assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(this.rootSession).assignName(this.affiliation.getUsersWhoHaveBeenDeprovisionedGroupName())
        .assignCreateParentStemsIfNotExist(true).save();
    managers.addMember(SubjectTestHelper.SUBJ0, false);
  }

  /**
   * stop the root session
   */
  @Override
  protected void tearDown() {
    VetoDeprovisionedGroupAddHook.veto = false;
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * @param name
   * @return a group, made if not there
   */
  private Group group(String name) {
    return new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
  }

  /**
   * @param group
   * @param subject
   * @return the immediate members list membership id
   */
  private String membershipId(Group group, Subject subject) {
    Member member = MemberFinder.findBySubject(this.rootSession, subject, true);
    Membership membership = MembershipFinder.findImmediateMembership(this.rootSession, group, subject, true);
    assertEquals(member.getId(), membership.getMemberUuid());
    return membership.getUuid();
  }

  /**
   * @param subject
   * @return member id
   */
  private String memberId(Subject subject) {
    return MemberFinder.findBySubject(this.rootSession, subject, true).getId();
  }

  /**
   * @param ids
   * @return set
   */
  private static Set<String> set(String... ids) {
    Set<String> result = new LinkedHashSet<String>();
    for (String id : ids) {
      result.add(id);
    }
    return result;
  }

  /**
   * @return the deprovisioned users group
   */
  private Group deprovisionedGroup() {
    return this.affiliation.getUsersWhoHaveBeenDeprovisionedGroup();
  }

  /**
   * a membership of another person is not on the page for SUBJ1, so the whole request is
   * refused, including the valid id in the same request
   */
  public void testUnofferedIdRefusesWholeRequest() {

    Group groupA = group("test:a");
    Group groupB = group("test:b");
    groupA.addMember(SubjectTestHelper.SUBJ1, false);
    groupB.addMember(SubjectTestHelper.SUBJ2, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
        set(membershipId(groupA, SubjectTestHelper.SUBJ1), membershipId(groupB, SubjectTestHelper.SUBJ2)));

    assertTrue(result.isRefused());
    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertEquals(1, result.getRefusedIds().size());
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(groupB.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(deprovisionedGroup().hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * a row with showForRemoval false has no checkbox, so its id is refused
   */
  public void testShowForRemovalFalseRefused() {

    Group groupC = group("test:c");
    groupC.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperDeprovisioningOverallConfiguration overallConfiguration =
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(groupC, false);
    GrouperDeprovisioningConfiguration configuration = new GrouperDeprovisioningConfiguration();
    configuration.setGrouperDeprovisioningOverallConfiguration(overallConfiguration);
    overallConfiguration.getAffiliationToConfiguration().put("test", configuration);
    GrouperDeprovisioningAttributeValue attributeValue = new GrouperDeprovisioningAttributeValue();
    attributeValue.setGrouperDeprovisioningConfiguration(configuration);
    configuration.setNewConfig(attributeValue);
    attributeValue.setAffiliationString("test");
    attributeValue.setDirectAssignment(true);
    attributeValue.setDeprovisionString("true");
    attributeValue.setShowForRemovalString("false");
    configuration.storeConfiguration();
    GrouperDeprovisioningOverallConfiguration.cacheClear();

    String membershipId = membershipId(groupC, SubjectTestHelper.SUBJ1);
    assertFalse(GrouperDeprovisioningAllowedAccess.allowedMembershipIdsForUser(
        MemberFinder.findByUuid(this.rootSession, memberId(SubjectTestHelper.SUBJ1), true), this.affiliation).contains(membershipId));

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1), set(membershipId));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertTrue(groupC.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * a group as member, or GrouperAll, is never deprovisioned (the memberId is refused): removing
   * a group from an excludes group grants access to all its members
   */
  public void testGroupSubjectRefused() {

    Group excludes = group("test:excludes");
    Group someGroup = group("test:someGroupAsMember");
    excludes.addMember(someGroup.toSubject(), false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(someGroup.toSubject()),
        set(membershipId(excludes, someGroup.toSubject())));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_MEMBER_NOT_ALLOWED, result.getRefusedReason());
    assertTrue(excludes.hasMember(someGroup.toSubject()));

    result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJA), set());
    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_MEMBER_NOT_ALLOWED, result.getRefusedReason());
  }

  /**
   * someone who is not a manager of the affiliation is refused
   */
  public void testNonManagerRefused() {

    Group groupA = group("test:a");
    groupA.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ5, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
        set(membershipId(groupA, SubjectTestHelper.SUBJ1)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_CALLER_NOT_ALLOWED, result.getRefusedReason());
    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * the excludes side of a policy group (right factor of a complement), and a group nested in
   * it, are skipped; the include side is removed
   */
  public void testExcludeOrDenyGroupSkipped() {

    Group includes = group("test:policy_includes");
    Group excludes = group("test:policy_excludes");
    Group nestedExcludes = group("test:policy_nestedExcludes");
    Group overall = group("test:policy");
    overall.addCompositeMember(CompositeType.COMPLEMENT, includes, excludes);
    excludes.addMember(nestedExcludes.toSubject(), false);

    includes.addMember(SubjectTestHelper.SUBJ1, false);
    excludes.addMember(SubjectTestHelper.SUBJ1, false);
    nestedExcludes.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
        set(membershipId(includes, SubjectTestHelper.SUBJ1), membershipId(excludes, SubjectTestHelper.SUBJ1),
            membershipId(nestedExcludes, SubjectTestHelper.SUBJ1)));

    assertFalse(result.isRefused());
    assertEquals(GrouperUtil.toStringForLog(result.getSkipped()), 2, result.getSkipped().size());
    assertTrue(result.getSkipped().get(0), result.getSkipped().get(0).contains(GrouperDeprovisioningAllowedAccess.GUARDRAIL_EXCLUDE_OR_DENY_GROUP));
    assertFalse(includes.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(excludes.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(nestedExcludes.hasMember(SubjectTestHelper.SUBJ1));
    assertNotNull(GrouperDeprovisioningAllowedAccess.skippedMessage(result));
  }

  /**
   * groups that reach the right side of a complement through a union, an intersection, or several
   * levels of composites and nesting are exclude groups too; a factor of a union that feeds nothing
   * else is not (2026-11-10)
   */
  public void testExcludeThroughCompositeFactors() {
    // D = X - (A union B)
    Group x = group("test:cf_x");
    Group a = group("test:cf_a");
    Group b = group("test:cf_b");
    Group unionAB = group("test:cf_unionAB");
    Group d = group("test:cf_d");
    unionAB.addCompositeMember(CompositeType.UNION, a, b);
    d.addCompositeMember(CompositeType.COMPLEMENT, x, unionAB);
    assertTrue("left factor of a union on the excludes side", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(a));
    assertTrue("right factor of a union on the excludes side", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(b));
    assertFalse("the include side is not an exclude group", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(x));

    // E = Y - (P intersection Q), and N nested in P
    Group y = group("test:cf_y");
    Group p = group("test:cf_p");
    Group q = group("test:cf_q");
    Group n = group("test:cf_n");
    Group intersectPQ = group("test:cf_intersectPQ");
    Group e = group("test:cf_e");
    p.addMember(n.toSubject(), false);
    intersectPQ.addCompositeMember(CompositeType.INTERSECTION, p, q);
    e.addCompositeMember(CompositeType.COMPLEMENT, y, intersectPQ);
    assertTrue("factor of an intersection on the excludes side", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(q));
    assertTrue("nested in a factor of an intersection on the excludes side", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(n));

    // control: a union that is not used by any complement
    Group r = group("test:cf_r");
    Group s2 = group("test:cf_s");
    Group unionRS = group("test:cf_unionRS");
    unionRS.addCompositeMember(CompositeType.UNION, r, s2);
    assertFalse("a union factor that feeds no complement", GrouperDeprovisioningAllowedAccess.isExcludeOrDenyGroup(r));
  }

  /**
   * a group under etc needs a wheel caller: skipped for a plain manager, removed for wheel
   */
  public void testSystemGroupNeedsWheel() {

    Group systemGroup = group("etc:deprovisioningTest:someSystemGroup");
    systemGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
        set(membershipId(systemGroup, SubjectTestHelper.SUBJ1)));

    assertFalse(result.isRefused());
    assertEquals(1, result.getSkipped().size());
    assertTrue(result.getSkipped().get(0).contains(GrouperDeprovisioningAllowedAccess.GUARDRAIL_SYSTEM_OBJECT_REQUIRES_WHEEL));
    assertTrue(systemGroup.hasMember(SubjectTestHelper.SUBJ1));

    // wheel caller
    Group wheel = group("etc:deprovisioningTestWheel");
    wheel.addMember(SubjectTestHelper.SUBJ3, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ3, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
        set(membershipId(systemGroup, SubjectTestHelper.SUBJ1)));

    assertFalse(result.isRefused());
    assertEquals(0, result.getSkipped().size());
    assertFalse(systemGroup.hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * allowed case: an ordinary manager removes the offered rows on an object the manager cannot
   * see, the member is added to the deprovisioned users group (the manager has no UPDATE there),
   * and the audit entry names the deprovisioned member and the manager
   */
  public void testAllowedRowsRemovedOnHiddenObject() {

    Group hidden = group("test:hidden");
    hidden.revokePriv(SubjectTestHelper.SUBJA, AccessPrivilege.READ, false);
    hidden.revokePriv(SubjectTestHelper.SUBJA, AccessPrivilege.VIEW, false);
    hidden.addMember(SubjectTestHelper.SUBJ1, false);
    hidden.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    assertFalse(hidden.hasView(SubjectTestHelper.SUBJ0));
    assertFalse(deprovisionedGroup().hasUpdate(SubjectTestHelper.SUBJ0));

    Member member1 = MemberFinder.findBySubject(this.rootSession, SubjectTestHelper.SUBJ1, true);
    Membership adminMembership = MembershipFinder.findImmediateMembership(this.rootSession, hidden,
        SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN.getField(), true);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, member1.getId(),
        set(membershipId(hidden, SubjectTestHelper.SUBJ1), adminMembership.getUuid()));

    assertFalse(result.isRefused());
    assertEquals(0, result.getSkipped().size());
    assertEquals(2, result.getRemovedMemberships().size());
    assertFalse(hidden.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(hidden.hasAdmin(SubjectTestHelper.SUBJ1));
    assertTrue(result.isAddedToDeprovisionedGroup());
    assertTrue(deprovisionedGroup().hasMember(SubjectTestHelper.SUBJ1));

    // the audit entry names the deprovisioned member and the manager
    String auditTypeId = AuditTypeFinder.find("memberDeprovisioning", "deprovision", true).getId();
    Set<AuditEntry> auditEntries = HibernateSession.byHqlStatic()
        .createQuery("from AuditEntry where auditTypeId = :theAuditTypeId and string01 = :theMemberId")
        .setString("theAuditTypeId", auditTypeId).setString("theMemberId", member1.getId()).listSet(AuditEntry.class);
    assertEquals(1, auditEntries.size());
    AuditEntry auditEntry = auditEntries.iterator().next();
    assertEquals(memberId(SubjectTestHelper.SUBJ0), auditEntry.getLoggedInMemberId());
    assertTrue(auditEntry.getDescription(), auditEntry.getDescription().contains(SubjectTestHelper.SUBJ1.getId()));
  }

  /**
   * a failure after the first removal (here a veto on the add to the deprovisioned users group)
   * rolls back every removal
   */
  public void testFailureRollsBackEverything() {

    Group groupA = group("test:a");
    Group groupB = group("test:b");
    groupA.addMember(SubjectTestHelper.SUBJ1, false);
    groupB.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperHooksUtils.addHookManual(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), VetoDeprovisionedGroupAddHook.class);
    VetoDeprovisionedGroupAddHook.veto = true;
    VetoDeprovisionedGroupAddHook.groupName = this.affiliation.getUsersWhoHaveBeenDeprovisionedGroupName();

    try {
      GrouperDeprovisioningAllowedAccess.deprovisionUser(
          SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1),
          set(membershipId(groupA, SubjectTestHelper.SUBJ1), membershipId(groupB, SubjectTestHelper.SUBJ1)));
      fail("expected the veto");
    } catch (RuntimeException re) {
      // expected
    } finally {
      VetoDeprovisionedGroupAddHook.veto = false;
    }

    assertTrue(groupA.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(groupB.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(deprovisionedGroup().hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * deprovisioning a person with nothing left to remove still adds them to the deprovisioned
   * users group
   */
  public void testNoRowsStillDeprovisions() {

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.deprovisionUser(
        SubjectTestHelper.SUBJ0, this.affiliation, memberId(SubjectTestHelper.SUBJ1), set());

    assertFalse(result.isRefused());
    assertTrue(deprovisionedGroup().hasMember(SubjectTestHelper.SUBJ1));
  }

  /**
   * test hook that vetoes adds to one group while the flag is on
   */
  public static class VetoDeprovisionedGroupAddHook extends MembershipHooks {

    /** veto on */
    static boolean veto = false;

    /** group to veto adds to */
    static String groupName = null;

    /**
     * @see MembershipHooks#membershipPreAddMember(HooksContext, HooksMembershipChangeBean)
     */
    @Override
    public void membershipPreAddMember(HooksContext hooksContext, HooksMembershipChangeBean preAddMemberBean) {
      if (veto && preAddMemberBean.getGroup() != null && GrouperUtil.equals(groupName, preAddMemberBean.getGroup().getName())) {
        throw new HookVeto("test.veto", "test veto");
      }
    }
  }
}
