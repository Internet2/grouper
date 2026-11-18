package edu.internet2.middleware.grouper.app.workflow;

import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
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
 * Security fix 112: viewing a workflow config (the configs list and the config details screen)
 * is decided per config: people who can configure workflows on the group, else members of that
 * config's own viewers group.  VIEW on the group, or being a viewer of another config on the
 * group, is not enough.
 * Before the fix this does not compile (GrouperWorkflowConfigViewCheck is missing); the old
 * group-wide check (canSubjectViewWorkflow) is true for SUBJ1 on config B, which the last
 * assertion of testPerConfig documents.
 * </pre>
 */
public class GrouperWorkflowConfigViewCheckTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /** owner group, SUBJ0 and SUBJ1 have VIEW, SUBJ3 is ADMIN */
  private Group ownerGroup;

  /** viewers of config A, SUBJ1 is a member */
  private Group viewersA;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowConfigViewCheckTest("testPerConfig"));
  }

  /**
   */
  public GrouperWorkflowConfigViewCheckTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowConfigViewCheckTest(String name) {
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

    this.ownerGroup = saveGroup("test:wf:owner");
    this.viewersA = saveGroup("test:wf:viewersA");
    this.viewersA.addMember(SubjectTestHelper.SUBJ1, false);

    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);
    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, false);
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
   * save a config on the owner group
   * @param workflowId
   * @param viewersGroupId blank for no viewers group
   * @return the stored config
   */
  private GrouperWorkflowConfig saveConfig(String workflowId, String viewersGroupId) {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(
        GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(this.ownerGroup.getId())));
    config.setOwnerGroup(this.ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled(toStringTrueFalse(true));
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName(workflowId);
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    config.setWorkflowConfigViewersGroupId(viewersGroupId);
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, this.ownerGroup);
    GrouperWorkflowConfig stored = GrouperWorkflowConfigService.getWorkflowConfig(this.ownerGroup, workflowId);
    assertNotNull(stored);
    return stored;
  }

  /**
   * viewers of A see only A, VIEW only sees nothing, admin and root see both
   */
  public void testPerConfig() {

    GrouperWorkflowConfig configA = saveConfig("wfViewA", this.viewersA.getId());
    GrouperWorkflowConfig configB = saveConfig("wfViewB", null);

    // viewer of A
    assertTrue(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configA, SubjectTestHelper.SUBJ1));
    assertFalse(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configB, SubjectTestHelper.SUBJ1));

    // VIEW on the group only
    assertFalse(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configA, SubjectTestHelper.SUBJ0));
    assertFalse(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configB, SubjectTestHelper.SUBJ0));

    // group admin and root
    assertTrue(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configA, SubjectTestHelper.SUBJ3));
    assertTrue(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configB, SubjectTestHelper.SUBJ3));
    assertTrue(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configB, SubjectFinder.findRootSubject()));

    // nulls
    assertFalse(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, null, SubjectTestHelper.SUBJ3));
    assertFalse(GrouperWorkflowConfigViewCheck.canSubjectViewWorkflowConfig(this.ownerGroup, configA, null));

    // the old group wide check said yes for SUBJ1 (viewer of A), which is how B leaked
    assertTrue(GrouperWorkflowConfigService.canSubjectViewWorkflow(this.ownerGroup, SubjectTestHelper.SUBJ1));
  }
}
