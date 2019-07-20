package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;

public class GrouperWorkflowConfigServiceTest extends GrouperTest {
  
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
  
  public void testGetWorklowConfigByGroupAndConfigName() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    saveWorkflowConfigAttributeMetadata(group, "testId");
    
    // when
    GrouperWorkflowConfig workflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(group, "testId");
    
    // then
    assertEquals(GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(), workflowConfig.getWorkflowConfigApprovalsString());
    assertEquals("test description", workflowConfig.getWorkflowConfigDescription());
    assertEquals("true", workflowConfig.getWorkflowConfigEnabled());
    assertEquals("<html></html>", workflowConfig.getWorkflowConfigForm());
    assertEquals("testId", workflowConfig.getWorkflowConfigId());
    assertEquals(true, workflowConfig.isWorkflowConfigSendEmail());
    assertEquals("config name", workflowConfig.getWorkflowConfigName());
    assertEquals(GrouperWorkflowConfigParams.getDefaultConfigParamsString(), workflowConfig.getWorkflowConfigParamsString());
    assertEquals("grouper", workflowConfig.getWorkflowConfigType());
    assertEquals(group.getId(), workflowConfig.getOwnerGroup().getId());
    assertEquals(null, workflowConfig.getWorkflowConfigViewersGroupId());
  }
  
  public void testGetWorklowConfigByAttributeAssignId() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign attributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId1");
    
    // when
    GrouperWorkflowConfig workflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(attributeAssign.getId());
    
    // then
    assertEquals(GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(), workflowConfig.getWorkflowConfigApprovalsString());
    assertEquals("test description", workflowConfig.getWorkflowConfigDescription());
    assertEquals("true", workflowConfig.getWorkflowConfigEnabled());
    assertEquals("<html></html>", workflowConfig.getWorkflowConfigForm());
    assertEquals("testId1", workflowConfig.getWorkflowConfigId());
    assertEquals(true, workflowConfig.isWorkflowConfigSendEmail());
    assertEquals("config name", workflowConfig.getWorkflowConfigName());
    assertEquals(GrouperWorkflowConfigParams.getDefaultConfigParamsString(), workflowConfig.getWorkflowConfigParamsString());
    assertEquals("grouper", workflowConfig.getWorkflowConfigType());
    assertEquals(group.getId(), workflowConfig.getOwnerGroup().getId());
    assertEquals(null, workflowConfig.getWorkflowConfigViewersGroupId());
  }
  
  public void testWorkflowIdExists() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    saveWorkflowConfigAttributeMetadata(group, "testId2");
    
    // when
    boolean shouldBeTrue = GrouperWorkflowConfigService.workflowIdExists("testId2");
    boolean shouldBeFalse = GrouperWorkflowConfigService.workflowIdExists("testIdNonExistent");
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
    
  }
  
  public void testGetWorkflowConfigsForGroup() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    saveWorkflowConfigAttributeMetadata(group, "testId5");
    saveWorkflowConfigAttributeMetadata(group, "testId6");
    
    // when
    List<GrouperWorkflowConfig> workflowConfigs = GrouperWorkflowConfigService.getWorkflowConfigs(group);
    
    // then
    assertEquals(2, workflowConfigs.size());
    List<String> ids = Arrays.asList(workflowConfigs.get(0).getWorkflowConfigId(), workflowConfigs.get(1).getWorkflowConfigId());
    
    assertTrue(ids.contains("testId5"));
    assertTrue(ids.contains("testId6"));
    
  }
  
  public void testWheelRootCanConfigureWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    // when
    boolean shouldBeTrue = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJR);
    boolean shouldBeFalse = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJ0);
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
  }
  
  public void testGroupAdminCanConfigureWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    group.addOrEditMember(SubjectTestHelper.SUBJ0, false, false, true, false, false, false, false, false, false, false, null, null, false);
    
    // when
    boolean shouldBeTrue = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJ0);
    boolean shouldBeFalse = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJ1);
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
  }
  
  public void testEditorGroupCanConfigureWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    GroupSave groupSaveEditor = new GroupSave(grouperSession).assignName("workflow:editor").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:editor").assignTypeOfGroup(TypeOfGroup.group);
    Group groupEditor = groupSaveEditor.save();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.editorsGroup", groupEditor.getUuid());
    
    groupEditor.addMember(SubjectTestHelper.SUBJ0);
    
    // when
    boolean shouldBeTrue = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJ0);
    boolean shouldBeFalse = GrouperWorkflowConfigService.canSubjectConfigureWorkflow(group, SubjectTestHelper.SUBJ1);
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
  }
  
  public void testSubjectCanViewWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    GroupSave viewersGroupSave = new GroupSave(grouperSession).assignName("workflow:viewers").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:viewers").assignTypeOfGroup(TypeOfGroup.group);
    Group viewersGroup = viewersGroupSave.save();
    
    viewersGroup.addMember(SubjectTestHelper.SUBJ0);
    
    AttributeAssign attributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId7");
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), viewersGroup.getUuid());
    
    attributeAssign.saveOrUpdate();
    
    // when
    boolean shouldBeTrue = GrouperWorkflowConfigService.canSubjectViewWorkflow(group, SubjectTestHelper.SUBJ0);
    boolean shouldBeFalse = GrouperWorkflowConfigService.canSubjectViewWorkflow(group, SubjectTestHelper.SUBJ1);
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
  }
  
  public void testSaveOrUpdateWorkflowConfig() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    GrouperWorkflowConfig configToBeSaved = buildSampleGrouperWorfklowConfig(group, "testId9");
    
    // when
    GrouperWorkflowConfigService.saveOrUpdateGrouperWorkflowConfig(configToBeSaved, group);
    
    // then
    boolean shouldBeTrue = GrouperWorkflowConfigService.workflowIdExists("testId9");
    assertEquals(true, shouldBeTrue);
    
  }
  
  private GrouperWorkflowConfig buildSampleGrouperWorfklowConfig(Group ownerGroup, String workflowId) {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    String defaultApprovalStatesString = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString();
    config.setWorkflowConfigApprovalsString(defaultApprovalStatesString);
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(defaultApprovalStatesString));
    config.setOwnerGroup(ownerGroup);
    config.setWorkflowConfigDescription("test description");
    config.setWorkflowConfigEnabled(toStringTrueFalse(true));
    config.setWorkflowConfigForm("<html></html>");
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName("test name");
    String defaultConfigParamsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString(); 
    config.setWorkflowConfigParamsString(defaultConfigParamsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(defaultConfigParamsString));
    config.setWorkflowConfigSendEmail(true);
    config.setWorkflowConfigType("grouper");
    config.setWorkflowConfigViewersGroupId("viewersGroupId");
    return config;
  }
  
  private AttributeAssign saveWorkflowConfigAttributeMetadata(Group group, String workflowId) {
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_APPROVALS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), GrouperWorkflowApprovalStates.getDefaultApprovalStatesString());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test description");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_FORM, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "<html></html>");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowId);
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "config name");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_PARAMS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), GrouperWorkflowConfigParams.getDefaultConfigParamsString());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(true));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_TYPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "grouper");
    
    attributeAssign.saveOrUpdate();
    
    return attributeAssign;
    
  }

}
