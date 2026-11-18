package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix: saving a workflow config grants and revokes VIEW on the workflow's group in a
 * root session.  A workflow editor (workflow.editorsGroup) who is not a group ADMIN must not be
 * able to change who can VIEW the group that way, an edit must not strip EveryEntity VIEW the
 * group already had, and allowedGroupId given as a group name must work.
 * Before the fix this does not compile (GrouperWorkflowConfigViewPrivileges is missing); with the
 * old GrouperWorkflowConfigService the save by group name throws and the edit revokes
 * EveryEntity VIEW.
 * Uses only APIs that exist from Grouper 2.6 on (GrouperTest base, no commons-lang3).
 * </pre>
 */
public class GrouperWorkflowConfigViewPrivilegesTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** the workflow's group, private: SUBJ0 has VIEW and UPDATE, SUBJ1 has ADMIN */
  private Group privateGroup;

  /** a group of people allowed to initiate */
  private Group allowed1;

  /** another group of people allowed to initiate */
  private Group allowed2;

  /** the workflow editors group, SUBJ0 is a member */
  private Group editors;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowConfigViewPrivilegesTest("testEditKeepsEveryEntityView"));
  }

  /**
   */
  public GrouperWorkflowConfigViewPrivilegesTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowConfigViewPrivilegesTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    this.grouperSession = GrouperSession.startRootSessionIfNotStarted().getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.editorsGroup", "test:wf189:editors");

    this.privateGroup = saveGroup("test:wf189:private");
    this.allowed1 = saveGroup("test:wf189:allowed1");
    this.allowed2 = saveGroup("test:wf189:allowed2");
    this.editors = saveGroup("test:wf189:editors");

    // SUBJ0: workflow editor with VIEW and UPDATE on the group, not ADMIN.  SUBJ1: group ADMIN
    this.editors.addMember(SubjectTestHelper.SUBJ0, false);
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
    this.privateGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
  }

  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.enable");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("workflow.editorsGroup");
    super.tearDown();
  }

  /**
   * save a group with no GrouperAll privileges
   * @param name
   * @return the group
   */
  private Group saveGroup(String name) {
    Group group = new GroupSave(this.grouperSession).assignName(name).assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    return group;
  }

  /**
   * build a config on the group with the default approvals and this allowedGroupId
   * @param ownerGroup
   * @param workflowId
   * @param allowedGroupRef uuid or name, or null for everyone
   * @return the config
   */
  private static GrouperWorkflowConfig buildConfig(Group ownerGroup, String workflowId, String allowedGroupRef) {
    GrouperWorkflowApprovalStates states = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(ownerGroup.getId()));
    states.getStateByName(GrouperWorkflowConstants.INITIATE_STATE).setAllowedGroupId(allowedGroupRef);

    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowApprovalStates(states);
    config.setOwnerGroup(ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled("true");
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName("test name " + workflowId);
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    return config;
  }

  /**
   * does the subject have VIEW on the private group as an immediate privilege
   * @param subject
   * @return true if so
   */
  private boolean hasImmediateView(Subject subject) {
    return MembershipFinder.findImmediateMembership(this.grouperSession, this.privateGroup, subject,
        AccessPrivilege.VIEW.getField(), false) != null;
  }

  /**
   * a workflow editor without ADMIN cannot save a workflow that changes VIEW on the group; a group
   * ADMIN and root can; once the VIEW is in place the editor can save again
   */
  public void testEditorWithoutAdminCannotChangeView() {

    // precondition: SUBJ0 may open and design workflows on the group (editors group)
    assertTrue(GrouperWorkflowConfigService.canSubjectConfigureWorkflow(this.privateGroup, SubjectTestHelper.SUBJ0));
    assertFalse(this.privateGroup.hasAdmin(SubjectTestHelper.SUBJ0));

    // blank allowedGroupId would grant EveryEntity VIEW on a private group
    GrouperWorkflowConfig everyoneConfig = buildConfig(this.privateGroup, "wf189a", null);
    List<String> errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(everyoneConfig, this.privateGroup, SubjectTestHelper.SUBJ0);
    assertEquals(errors.toString(), 1, errors.size());
    assertTrue(errors.get(0), errors.get(0).contains("EveryEntity"));

    // a specific allowed group (by name) would grant it VIEW
    GrouperWorkflowConfig allowedConfig = buildConfig(this.privateGroup, "wf189a", this.allowed1.getName());
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(allowedConfig, this.privateGroup, SubjectTestHelper.SUBJ0);
    assertEquals(errors.toString(), 1, errors.size());

    // group admin and root may
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(everyoneConfig, this.privateGroup, SubjectTestHelper.SUBJ1);
    assertEquals(errors.toString(), 0, errors.size());
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(everyoneConfig, this.privateGroup, SubjectFinder.findRootSubject());
    assertEquals(errors.toString(), 0, errors.size());

    // no author
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(everyoneConfig, this.privateGroup, null);
    assertEquals(errors.toString(), 1, errors.size());

    // the admin saves it (allowed1 gets VIEW); now the editor's re-save changes nothing: allowed
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(allowedConfig, this.privateGroup);
    assertTrue(hasImmediateView(this.allowed1.toSubject()));
    GrouperWorkflowConfig editedConfig = buildConfig(this.privateGroup, "wf189a", this.allowed1.getName());
    editedConfig.setWorkflowConfigDescription("edited by the editor");
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(editedConfig, this.privateGroup, SubjectTestHelper.SUBJ0);
    assertEquals(errors.toString(), 0, errors.size());

    // but switching the allowed group (revoke allowed1, grant allowed2) is refused
    GrouperWorkflowConfig switchedConfig = buildConfig(this.privateGroup, "wf189a", this.allowed2.getId());
    errors = GrouperWorkflowConfigViewPrivileges.validateAuthor(switchedConfig, this.privateGroup, SubjectTestHelper.SUBJ0);
    assertEquals(errors.toString(), 1, errors.size());
    assertTrue(errors.get(0), errors.get(0).contains("revoke VIEW"));
  }

  /**
   * allowedGroupId by group name resolves as a group (SubjectFinder.findById threw before)
   */
  public void testSaveAllowedGroupByName() {
    GrouperWorkflowConfig config = buildConfig(this.privateGroup, "wf189b", this.allowed1.getName());
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, this.privateGroup);
    assertTrue(hasImmediateView(this.allowed1.toSubject()));
    assertFalse(hasImmediateView(SubjectFinder.findAllSubject()));
    assertNotNull(GrouperWorkflowConfigService.getWorkflowConfig(this.privateGroup, "wf189b"));
  }

  /**
   * an edit keeps EveryEntity VIEW the group had, and only revokes a previous allowed group when
   * allowedGroupId changed
   */
  public void testEditKeepsEveryEntityView() {

    // the group was visible to everyone before any workflow
    this.privateGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189c", null), this.privateGroup);
    assertTrue(hasImmediateView(SubjectFinder.findAllSubject()));

    // edit to a specific allowed group: EveryEntity VIEW stays (the old code revoked it)
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189c", this.allowed1.getId()), this.privateGroup);
    assertTrue(hasImmediateView(SubjectFinder.findAllSubject()));
    assertTrue(hasImmediateView(this.allowed1.toSubject()));

    // edit with the same allowed group (by name this time): nothing revoked
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189c", this.allowed1.getName()), this.privateGroup);
    assertTrue(hasImmediateView(this.allowed1.toSubject()));

    // change the allowed group: allowed1 revoked, allowed2 granted
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189c", this.allowed2.getName()), this.privateGroup);
    assertFalse(hasImmediateView(this.allowed1.toSubject()));
    assertTrue(hasImmediateView(this.allowed2.toSubject()));
    assertTrue(hasImmediateView(SubjectFinder.findAllSubject()));
  }

  /**
   * a previous allowed group that another workflow on the group still uses keeps VIEW
   */
  public void testEditKeepsViewUsedByOtherWorkflow() {
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189d", this.allowed1.getId()), this.privateGroup);
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189e", this.allowed1.getName()), this.privateGroup);
    assertTrue(hasImmediateView(this.allowed1.toSubject()));

    // wf189d moves to allowed2, wf189e still uses allowed1
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(buildConfig(this.privateGroup, "wf189d", this.allowed2.getId()), this.privateGroup);
    assertTrue(hasImmediateView(this.allowed1.toSubject()));
    assertTrue(hasImmediateView(this.allowed2.toSubject()));
    assertFalse(hasImmediateView(SubjectFinder.findAllSubject()));
  }
}
