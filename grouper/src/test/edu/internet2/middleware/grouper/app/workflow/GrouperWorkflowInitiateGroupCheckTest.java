package edu.internet2.middleware.grouper.app.workflow;

import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import junit.textui.TestRunner;

/**
 * <pre>
 * Security fix: a workflow initiate request must be for the workflow config's own group.  The
 * UI submit takes the config and the group as two request params, so the check that binds them
 * is tested here.
 * Before the fix this does not compile (GrouperWorkflowInitiateGroupCheck is missing).
 * </pre>
 */
public class GrouperWorkflowInitiateGroupCheckTest extends GrouperTest {

  /** root session */
  private GrouperSession grouperSession;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowInitiateGroupCheckTest("testGroupMatchesConfig"));
  }

  /**
   */
  public GrouperWorkflowInitiateGroupCheckTest() {
  }

  /**
   * @param name
   */
  public GrouperWorkflowInitiateGroupCheckTest(String name) {
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
  }

  /**
   * config C on group a: a matches, b (no workflow) and null do not
   */
  public void testGroupMatchesConfig() {

    Group groupA = new GroupSave(this.grouperSession).assignName("test:wf:a").assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();
    Group groupB = new GroupSave(this.grouperSession).assignName("test:wf:b").assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group).save();

    // workflow config C on a, stored the way the UI stores it
    String approvals = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(groupA.getId());
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setWorkflowConfigApprovalsString(approvals);
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(approvals));
    config.setOwnerGroup(groupA);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled(toStringTrueFalse(true));
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId("wfInitiateCheck");
    config.setWorkflowConfigName("test name");
    String params = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(params);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(params));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType("grouper");
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(config, groupA);

    // load it by marker id, as workflowInitiateSubmit does with workflowAttributeAssignId
    String markerId = GrouperWorkflowConfigService.getWorkflowConfig(groupA, "wfInitiateCheck").getAttributeAssignmentMarkerId();
    GrouperWorkflowConfig loadedConfig = GrouperWorkflowConfigService.getWorkflowConfig(markerId);
    assertNotNull(loadedConfig.getOwnerGroup());

    assertTrue(GrouperWorkflowInitiateGroupCheck.groupMatchesConfig(loadedConfig, groupA));
    assertFalse(GrouperWorkflowInitiateGroupCheck.groupMatchesConfig(loadedConfig, groupB));
    assertFalse(GrouperWorkflowInitiateGroupCheck.groupMatchesConfig(loadedConfig, null));
    assertFalse(GrouperWorkflowInitiateGroupCheck.groupMatchesConfig(null, groupA));

    // a config with no owner group does not match anything
    assertFalse(GrouperWorkflowInitiateGroupCheck.groupMatchesConfig(new GrouperWorkflowConfig(), groupA));

    GrouperWorkflowInitiateGroupCheck.assertGroupMatchesConfig(loadedConfig, groupA);
    try {
      GrouperWorkflowInitiateGroupCheck.assertGroupMatchesConfig(loadedConfig, groupB);
      fail("group b is not the config's group");
    } catch (RuntimeException re) {
      // expected, message has ids only
      assertTrue(re.getMessage(), re.getMessage().contains(groupB.getId()));
      assertFalse(re.getMessage(), re.getMessage().contains(groupB.getName()));
    }
  }
}
