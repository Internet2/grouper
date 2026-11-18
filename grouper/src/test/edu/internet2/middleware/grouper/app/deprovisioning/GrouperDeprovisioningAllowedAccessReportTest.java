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
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M6 (deprovisioning report submit acts on members the report did not list):
 * the three report submits (UiV2Deprovisioning.deprovisioningOnGroupReportSubmit,
 * deprovisioningOnFolderReportSubmit, deprovisioningOnAttributeDefReportSubmit) now go through
 * GrouperDeprovisioningAllowedAccess.removeAccessFromReport, which re-checks that deprovisioning
 * is enabled and the report privileges, only acts on members the report lists, applies the
 * guardrails, and removes access in one transaction.</p>
 *
 * <p>Before the fix this class does not compile (GrouperDeprovisioningAllowedAccess is new), and
 * the old submit let a user with UPDATE remove an active ADMIN of the group.</p>
 *
 * <p>Setup: affiliation "test", group test:g with deprovisioning on for it; SUBJ0 has UPDATE and
 * READ; SUBJ1 is an active ADMIN (not deprovisioned, not on the report); SUBJ2 is a member and
 * ADMIN who has been deprovisioned (on the report).</p>
 */
public class GrouperDeprovisioningAllowedAccessReportTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningAllowedAccessReportTest("testUnlistedActiveAdminRefused"));
  }

  /**
   */
  public GrouperDeprovisioningAllowedAccessReportTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperDeprovisioningAllowedAccessReportTest(String name) {
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
   * make the affiliation groups
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    this.rootSession = GrouperSession.startRootSession();
    this.affiliation = GrouperDeprovisioningAffiliation.retrieveAllAffiliations().get("test");
    assertNotNull(this.affiliation);
    new GroupSave(this.rootSession).assignName(this.affiliation.getManagersGroupName())
        .assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(this.rootSession).assignName(this.affiliation.getUsersWhoHaveBeenDeprovisionedGroupName())
        .assignCreateParentStemsIfNotExist(true).save();
  }

  /**
   * stop the root session
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  /**
   * turn deprovisioning on for the affiliation directly on this object
   * @param owner
   */
  private static void configureDeprovisioning(GrouperObject owner) {
    GrouperDeprovisioningOverallConfiguration overallConfiguration =
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(owner, false);
    GrouperDeprovisioningConfiguration configuration = new GrouperDeprovisioningConfiguration();
    configuration.setGrouperDeprovisioningOverallConfiguration(overallConfiguration);
    overallConfiguration.getAffiliationToConfiguration().put("test", configuration);
    GrouperDeprovisioningAttributeValue attributeValue = new GrouperDeprovisioningAttributeValue();
    attributeValue.setGrouperDeprovisioningConfiguration(configuration);
    configuration.setNewConfig(attributeValue);
    attributeValue.setAffiliationString("test");
    attributeValue.setDirectAssignment(true);
    attributeValue.setDeprovisionString("true");
    configuration.storeConfiguration();
    GrouperDeprovisioningOverallConfiguration.cacheClear();
  }

  /**
   * mark this subject as deprovisioned for the affiliation
   * @param subject
   */
  private void deprovision(Subject subject) {
    this.affiliation.getUsersWhoHaveBeenDeprovisionedGroup().addMember(subject, false);
  }

  /**
   * group test:g (or the given name) on the report, with SUBJ0 UPDATE+READ, SUBJ1 active ADMIN,
   * SUBJ2 deprovisioned member and ADMIN
   * @param name
   * @return the group
   */
  private Group setupGroup(String name) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.addMember(SubjectTestHelper.SUBJ2, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN, false);
    configureDeprovisioning(group);
    deprovision(SubjectTestHelper.SUBJ2);
    return group;
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
   * the attack: a user with UPDATE submits the member id of an active ADMIN who is not on the
   * report; refused, ADMIN kept
   */
  public void testUnlistedActiveAdminRefused() {

    Group group = setupGroup("test:g");
    assertFalse(GrouperDeprovisioningAllowedAccess.reportAllowedMemberIds(group).contains(memberId(SubjectTestHelper.SUBJ1)));

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, group, set(memberId(SubjectTestHelper.SUBJ1)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * allowed case: a deprovisioned subject on the report loses the membership and ADMIN
   */
  public void testListedDeprovisionedRemoved() {

    Group group = setupGroup("test:g");
    assertTrue(GrouperDeprovisioningAllowedAccess.reportAllowedMemberIds(group).contains(memberId(SubjectTestHelper.SUBJ2)));

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, group, set(memberId(SubjectTestHelper.SUBJ2)));

    assertFalse(result.isRefused());
    assertEquals(1, result.getRemovedMembers().size());
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ2));
    assertFalse(group.hasAdmin(SubjectTestHelper.SUBJ2));
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * one listed and one unlisted id: nothing is removed
   */
  public void testMixedRequestRemovesNothing() {

    Group group = setupGroup("test:g");

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, group, set(memberId(SubjectTestHelper.SUBJ2), memberId(SubjectTestHelper.SUBJ1)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ2));
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ2));
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * deprovisioning turned off: the submit removes nothing
   */
  public void testDisabledRemovesNothing() {

    Group group = setupGroup("test:g");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "false");

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, group, set(memberId(SubjectTestHelper.SUBJ2)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_DEPROVISIONING_DISABLED, result.getRefusedReason());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ2));
  }

  /**
   * a caller without the report's privileges (READ only) is refused
   */
  public void testCallerWithoutUpdateRefused() {

    Group group = setupGroup("test:g");
    group.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.READ, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ3, group, set(memberId(SubjectTestHelper.SUBJ2)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_CALLER_NOT_ALLOWED, result.getRefusedReason());
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ2));
  }

  /**
   * the report is on an excludes group (right factor of a complement): the listed member is
   * skipped, removing them would grant access
   */
  public void testExcludeGroupSkipped() {

    Group excludes = setupGroup("test:policy_excludes");
    Group includes = new GroupSave(this.rootSession).assignName("test:policy_includes").save();
    Group overall = new GroupSave(this.rootSession).assignName("test:policy").save();
    overall.addCompositeMember(CompositeType.COMPLEMENT, includes, excludes);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, excludes, set(memberId(SubjectTestHelper.SUBJ2)));

    assertFalse(result.isRefused());
    assertEquals(1, result.getSkipped().size());
    assertTrue(result.getSkipped().get(0).contains(GrouperDeprovisioningAllowedAccess.GUARDRAIL_EXCLUDE_OR_DENY_GROUP));
    assertTrue(excludes.hasMember(SubjectTestHelper.SUBJ2));
  }

  /**
   * a report on an etc group: skipped for a non-wheel caller
   */
  public void testSystemGroupSkippedForNonWheel() {

    Group group = setupGroup("etc:deprovisioningReportTest:g");

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, group, set(memberId(SubjectTestHelper.SUBJ2)));

    assertFalse(result.isRefused());
    assertEquals(1, result.getSkipped().size());
    assertTrue(result.getSkipped().get(0).contains(GrouperDeprovisioningAllowedAccess.GUARDRAIL_SYSTEM_OBJECT_REQUIRES_WHEEL));
    assertTrue(group.hasAdmin(SubjectTestHelper.SUBJ2));
  }

  /**
   * attribute definition: an active ATTR_ADMIN who is not on the report is not touched
   */
  public void testAttributeDefUnlistedRefused() {

    AttributeDef attributeDef = new AttributeDefSave(this.rootSession).assignName("test:def")
        .assignCreateParentStemsIfNotExist(true).save();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, attributeDef, set(memberId(SubjectTestHelper.SUBJ1)));

    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertTrue(attributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ1));
  }

  /**
   * folder: an active STEM_ADMIN who is not on the report is not touched; a deprovisioned one is
   * removed
   */
  public void testFolderUnlistedRefusedListedRemoved() {

    Stem stem = new StemSave(this.rootSession).assignName("test:folder").assignCreateParentStemsIfNotExist(true).save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ADMIN, false);
    configureDeprovisioning(stem);
    deprovision(SubjectTestHelper.SUBJ2);

    GrouperDeprovisioningAllowedAccess.Result result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, stem, set(memberId(SubjectTestHelper.SUBJ1)));
    assertEquals(GrouperDeprovisioningAllowedAccess.REFUSED_ID_NOT_OFFERED, result.getRefusedReason());
    assertTrue(stem.hasStemAdmin(SubjectTestHelper.SUBJ1));

    result = GrouperDeprovisioningAllowedAccess.removeAccessFromReport(
        SubjectTestHelper.SUBJ0, stem, set(memberId(SubjectTestHelper.SUBJ2)));
    assertFalse(result.isRefused());
    assertFalse(stem.hasStemAdmin(SubjectTestHelper.SUBJ2));
    assertTrue(stem.hasStemAdmin(SubjectTestHelper.SUBJ1));
  }
}
