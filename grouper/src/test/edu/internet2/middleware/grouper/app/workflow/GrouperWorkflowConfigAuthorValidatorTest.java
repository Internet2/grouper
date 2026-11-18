package edu.internet2.middleware.grouper.app.workflow;

import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix: a group admin must not be able to save a workflow config whose completion adds
 * people to a group they have no privileges on (e.g. the wheel group), and the daemon must not
 * add anyone to a wheel group from a config that is already stored.
 * Before the fix this does not compile (GrouperWorkflowConfigAuthorValidator is missing), and
 * the daemon case adds the member to the wheel group.
 * </pre>
 */
public class GrouperWorkflowConfigAuthorValidatorTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** owner group of the workflow, SUBJ0 is ADMIN */
  private Group groupA;

  /** group with no privileges for SUBJ0 */
  private Group groupB;

  /** group where SUBJ0 has UPDATE */
  private Group groupC;

  /** the wheel group */
  private Group wheelGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowConfigAuthorValidatorTest("testDaemonDoesNotAssignToWheel"));
  }

  /**
   */
  public GrouperWorkflowConfigAuthorValidatorTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowConfigAuthorValidatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    this.grouperSession = grouperSessionResult.getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");

    this.groupA = saveGroup("test:wf:a");
    this.groupB = saveGroup("test:wf:b");
    this.groupC = saveGroup("test:wf:c");

    String wheelGroupName = GrouperConfig.retrieveConfig().propertyValueString("groups.wheel.group");
    this.wheelGroup = saveGroup(wheelGroupName);

    // SUBJ0: ADMIN on a, UPDATE on c, nothing on b or wheel
    this.groupA.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    this.groupC.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
  }

  /**
   * save a group with no GrouperAll privileges
   * @param name
   * @return the group
   */
  private Group saveGroup(String name) {
    Group group = new GroupSave(this.grouperSession).assignName(name).assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();
    // tests should not depend on default GrouperAll grants
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    return group;
  }

  /**
   * build a config on the owner group (the default approvals) with a different assignToGroup
   * and optionally an approver group in the groupManager state
   * @param ownerGroup
   * @param workflowId
   * @param assignToGroupRef
   * @param approverGroupRef null to keep the default managers-of the owner group
   * @return the config
   */
  private static GrouperWorkflowConfig buildConfig(Group ownerGroup, String workflowId, String assignToGroupRef, String approverGroupRef) {
    GrouperWorkflowApprovalStates states = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(ownerGroup.getId()));
    states.getStateByName(GrouperWorkflowConstants.COMPLETE_STATE).getActions().get(0).setActionArg0(assignToGroupRef);
    if (approverGroupRef != null) {
      GrouperWorkflowApprovalState groupManager = states.getStateByName("groupManager");
      groupManager.setApproverManagersOfGroupId(null);
      groupManager.setApproverGroupId(approverGroupRef);
    }

    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowApprovalStates(states);
    config.setOwnerGroup(ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled(toStringTrueFalse(true));
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName("test name");
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    // no emails from the daemon
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    return config;
  }

  /**
   * save time checks for a non-wheel author
   */
  public void testValidateAuthorAssignToGroup() {

    // wheel group by name: not allowed
    List<String> errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.wheelGroup.getName(), null), this.groupA, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 1, errors.size());

    // group with no privileges, by uuid: not allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.groupB.getId(), null), this.groupA, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 1, errors.size());

    // the owner group: allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.groupA.getId(), null), this.groupA, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 0, errors.size());

    // a group with UPDATE: allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.groupC.getName(), null), this.groupA, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 0, errors.size());

    // approver group that SUBJ0 cannot READ: not allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.groupA.getId(), this.groupB.getName()), this.groupA, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 1, errors.size());

    // the json string form (no parsed states) is checked too
    GrouperWorkflowConfig stringConfig = buildConfig(this.groupA, "wf1", this.wheelGroup.getName(), null);
    stringConfig.setWorkflowConfigApprovalsString(GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.wheelGroup.getName()));
    stringConfig.setWorkflowApprovalStates(null);
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(stringConfig, this.groupA, SubjectTestHelper.SUBJ0, null);
    assertTrue(errors.toString(), errors.size() > 0);

    // root can reference anything
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.wheelGroup.getName(), this.groupB.getName()), this.groupA, SubjectFinder.findRootSubject(), null);
    assertEquals(errors.toString(), 0, errors.size());

    // no author: not allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf1", this.groupA.getId(), null), this.groupA, null, null);
    assertEquals(errors.toString(), 1, errors.size());
  }

  /**
   * on edit, every reference is checked again, including ones unchanged from the stored config:
   * keeping a sysadmin-made assignToGroup while changing the approvers is the same as adding it
   */
  public void testValidateAuthorEdit() {

    // stored by a sysadmin, assigns to b which SUBJ0 has no privileges on
    GrouperWorkflowConfig previousConfig = buildConfig(this.groupA, "wf2", this.groupB.getId(), null);

    // SUBJ0 keeps b and changes the approvers to a group SUBJ0 controls: not allowed (b)
    List<String> errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf2", this.groupB.getId(), this.groupA.getName()), this.groupA, SubjectTestHelper.SUBJ0, previousConfig);
    assertEquals(errors.toString(), 1, errors.size());

    // SUBJ0 edits something else and keeps b: still not allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf2", this.groupB.getId(), null), this.groupA, SubjectTestHelper.SUBJ0, previousConfig);
    assertEquals(errors.toString(), 1, errors.size());

    // SUBJ0 changes it to the wheel group: not allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf2", this.wheelGroup.getName(), null), this.groupA, SubjectTestHelper.SUBJ0, previousConfig);
    assertEquals(errors.toString(), 1, errors.size());

    // SUBJ0 changes it to c (UPDATE): allowed
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(this.groupA, "wf2", this.groupC.getName(), null), this.groupA, SubjectTestHelper.SUBJ0, previousConfig);
    assertEquals(errors.toString(), 0, errors.size());
  }

  /**
   * with no assignToGroup the daemon adds the requester to the workflow's own group, so the author
   * needs UPDATE or ADMIN on it (a workflowEditors member may only VIEW the group)
   */
  public void testValidateAuthorOwnerGroupFallback() {

    // d: SUBJ0 can only VIEW it (as a workflowEditors member could)
    Group groupD = saveGroup("test:wf:d");
    groupD.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    // no assignToGroup on d, approvers a group SUBJ0 controls: not allowed (d is the target)
    GrouperWorkflowConfig config = buildConfig(groupD, "wf5", this.groupA.getId(), this.groupA.getName());
    config.getWorkflowApprovalStates().getStateByName(GrouperWorkflowConstants.COMPLETE_STATE).getActions().clear();
    List<String> errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(config, groupD, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 1, errors.size());
    assertTrue(errors.toString(), errors.get(0).contains(groupD.getName()));

    // same on c (SUBJ0 has UPDATE): allowed
    config = buildConfig(this.groupC, "wf6", this.groupA.getId(), this.groupA.getName());
    config.getWorkflowApprovalStates().getStateByName(GrouperWorkflowConstants.COMPLETE_STATE).getActions().clear();
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(config, this.groupC, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 0, errors.size());

    // explicit assignToGroup d on owner d: not allowed either (the owner is not exempt)
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(
        buildConfig(groupD, "wf7", groupD.getId(), this.groupA.getName()), groupD, SubjectTestHelper.SUBJ0, null);
    assertEquals(errors.toString(), 1, errors.size());

    // root may
    errors = GrouperWorkflowConfigAuthorValidator.validateAuthor(config, groupD, SubjectFinder.findRootSubject(), null);
    assertEquals(errors.toString(), 0, errors.size());
  }

  /**
   * daemon guard on its own
   */
  public void testIsDaemonAssignAllowed() {
    GrouperWorkflowConfig config = buildConfig(this.groupA, "wf3", this.wheelGroup.getName(), null);
    assertFalse(GrouperWorkflowConfigAuthorValidator.isDaemonAssignAllowed(config, this.wheelGroup));
    assertTrue(GrouperWorkflowConfigAuthorValidator.isDaemonAssignAllowed(config, this.groupA));
    assertTrue(GrouperWorkflowConfigAuthorValidator.isDaemonAssignAllowed(config, this.groupB));
    assertFalse(GrouperWorkflowConfigAuthorValidator.isDaemonAssignAllowed(config, null));
  }

  /**
   * a stored config (e.g. saved before the fix) that assigns to the wheel group: the daemon puts
   * the instance in the exception state and does not add the member
   */
  public void testDaemonDoesNotAssignToWheel() {

    GrouperWorkflowConfig config = buildConfig(this.groupA, "wfDaemon", this.wheelGroup.getName(), null);
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, this.groupA);
    GrouperWorkflowConfig storedConfig = GrouperWorkflowConfigService.getWorkflowConfig(this.groupA, "wfDaemon");
    assertNotNull(storedConfig);

    // an instance by SUBJ0 that has been approved (complete state)
    GrouperWorkflowInstance instance = new GrouperWorkflowInstance();
    instance.setGrouperWorkflowInstanceFilesInfo(new GrouperWorkflowInstanceFilesInfo());
    instance.setGrouperWorkflowInstanceParamValue0(new GrouperWorkflowInstanceParamValue());
    instance.setOwnerGrouperObject(this.groupA);
    instance.setWorkflowInstanceConfigMarkerAssignmentId(storedConfig.getAttributeAssignmentMarkerId());
    instance.setWorkflowInstanceEncryptionKey("encryption key");
    instance.setWorkflowInstanceFileInfoString("{}");
    instance.setWorkflowInstanceUuid("test uuid");
    instance.setWorkflowInstanceState(GrouperWorkflowConstants.COMPLETE_STATE);
    instance.setWorkflowInstanceParamValue0String("{}");
    instance.setWorkflowInstanceInitiatedMillisSince1970(11111L);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(11111L);
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setAction(GrouperWorkflowConstants.INITIATE_ACTION);
    logEntry.setSubjectId(SubjectTestHelper.SUBJ0_ID);
    // workflow identity is source plus id (advisory H1), record the source like createLogEntry does
    logEntry.setSubjectSourceId(SubjectTestHelper.SUBJ0.getSourceId());
    logEntry.setState(GrouperWorkflowConstants.INITIATE_STATE);
    GrouperWorkflowInstanceLogEntries logEntries = new GrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(logEntry);
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    GrouperWorkflowInstanceService.saveOrUpdateWorkflowInstance(instance, this.groupA);

    assertFalse(this.wheelGroup.hasMember(SubjectTestHelper.SUBJ0));

    new GrouperWorkflowDaemonJob().run(new OtherJobInput());

    assertFalse("the daemon must not add the requester to the wheel group", this.wheelGroup.hasMember(SubjectTestHelper.SUBJ0));

    List<GrouperWorkflowInstance> instances = GrouperWorkflowInstanceService.getWorkflowInstances(this.groupA, "wfDaemon");
    assertEquals(1, instances.size());
    assertEquals(GrouperWorkflowConstants.EXCEPTION_STATE, instances.get(0).getWorkflowInstanceState());
  }
}
