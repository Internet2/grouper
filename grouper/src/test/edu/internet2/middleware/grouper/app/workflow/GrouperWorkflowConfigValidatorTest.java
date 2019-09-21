package edu.internet2.middleware.grouper.app.workflow;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;

public class GrouperWorkflowConfigValidatorTest extends GrouperTest {
  
private GrouperSession grouperSession;
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }
  
  public void testAllBasicValidations() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    GrouperWorkflowConfig workflowConfig = buildSampleGrouperWorfklowConfig(group, "not valid");
    
    // when
    List<String> errors = new GrouperWorkflowConfigValidator().validate(workflowConfig, group, true);
    
    // then
    List<String> expected = Arrays.asList(
        "Workflow type random is not configured",
        "Workflow config name is required",
        "Workflow config id is not valid. It can only be alphanumeric with underscores and hyphens.",
        "Workflow config description is required", 
        "Approver group id sdgf76gdf87 not found for workflow state groupManager",
        "Group not found for assignToGroup group id sgk234kh234",
        "Workflow viewers group id not found",
        "Workflow enabled can only have \"true\", \"false\" and \"noNewSubmissions\" values");
    assertEquals(expected, errors);
    
  }
  
  private GrouperWorkflowConfig buildSampleGrouperWorfklowConfig(Group ownerGroup, String workflowId) {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    String defaultApprovalStatesString = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(ownerGroup.getId());
    config.setWorkflowConfigApprovalsString(defaultApprovalStatesString);
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(defaultApprovalStatesString));
    config.setOwnerGroup(ownerGroup);
    config.setWorkflowConfigDescription("");
    config.setWorkflowConfigEnabled("blah blah");
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName(null);
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString(); 
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(true);
    config.setWorkflowConfigType("random");
    config.setWorkflowConfigViewersGroupId("viewersGroupId");
    return config;
  }

}
