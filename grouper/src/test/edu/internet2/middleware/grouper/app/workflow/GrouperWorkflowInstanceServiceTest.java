package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;
import static org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.List;
import java.util.Set;

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
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstanceServiceTest extends GrouperTest {
  
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
  
  public void testGetWorkflowInstanceByAttributeAssignId() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId");
    AttributeAssign instanceAttributeAssign = saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    // when
    GrouperWorkflowInstance workfowInstance = GrouperWorkflowInstanceService.getWorkflowInstance(instanceAttributeAssign.getId());
    
    // then
    assertEquals(configAttributeAssign.getId(), workfowInstance.getWorkflowInstanceConfigMarkerAssignmentId());
    assertEquals("encryption key", workfowInstance.getWorkflowInstanceEncryptionKey());
    assertNull(workfowInstance.getWorkflowInstanceError());
    assertEquals("initiate", workfowInstance.getWorkflowInstanceState());
    assertEquals("test uuid", workfowInstance.getWorkflowInstanceUuid());
  }
  
  public void testGetWorkflowInstancesByGroup() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId");
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    // when
    List<GrouperWorkflowInstance> workfowInstances = GrouperWorkflowInstanceService.getWorkflowInstances(group);
    
    // then
    assertEquals(2, workfowInstances.size());
    
  }
  
  public void testFindGroupsWithWorkflowInstances() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId");
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    // when
    Set<Group> groups = GrouperWorkflowInstanceService.findGroupsWithWorkflowInstance();
    
    // then
    assertEquals(1, groups.size());
    assertEquals(group.getId(), groups.iterator().next().getId());
    
  }
  
  public void testSaveOrUpdateWorkflowInstance() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId11");
    
    GrouperWorkflowInstance instanceToBeSaved = buildSampleWorkflowInstance(group, configAttributeAssign.getId());
    
    // when
    GrouperWorkflowInstanceService.saveOrUpdateWorkflowInstance(instanceToBeSaved, group);
    
    // then
    List<GrouperWorkflowInstance> workflowInstances = GrouperWorkflowInstanceService.getWorkflowInstances(group, "testId11");
    assertEquals(1, workflowInstances.size());
    
  }
  
  public void testSubjectAlreadySubmittedWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId11");
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    // when
    boolean shouldBeTrue = GrouperWorkflowInstanceService.subjectAlreadySubmittedWorkflow(SubjectTestHelper.SUBJ0, group);
    boolean shouldBeFalse = GrouperWorkflowInstanceService.subjectAlreadySubmittedWorkflow(SubjectTestHelper.SUBJ1, group);
    
    // then
    assertEquals(true, shouldBeTrue);
    assertEquals(false, shouldBeFalse);
    
  }
  
  public void testWorkflowInstancesSubmittedByASubject() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId11");
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    // when
    List<GrouperWorkflowInstance> instancesSubmitted = GrouperWorkflowInstanceService.getWorkflowInstancesSubmitted(SubjectTestHelper.SUBJ0);
    
    // then
    assertEquals(1, instancesSubmitted.size());
    
  }
  
  public void testSubjectWhoInitiatedWorkflow() {
    
    // given
    GroupSave groupSave = new GroupSave(grouperSession).assignName("workflow:test").assignCreateParentStemsIfNotExist(true).assignDisplayName("workflow:test").assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    
    AttributeAssign configAttributeAssign = saveWorkflowConfigAttributeMetadata(group, "testId11");
    saveWorkflowInstanceAttributeMetadata(group, configAttributeAssign.getId());
    
    List<GrouperWorkflowInstance> workflowInstances = GrouperWorkflowInstanceService.getWorkflowInstances(group, "testId11");
    
    // when
    Subject subject = GrouperWorkflowInstanceService.subjectWhoInitiatedWorkflow(workflowInstances.get(0));
    
    // then
    assertEquals(SubjectTestHelper.SUBJ0_ID, subject.getId());
    
  }
  
  private GrouperWorkflowInstance buildSampleWorkflowInstance(Group ownerGroup, String configAttributeAssignId) {
    GrouperWorkflowInstance instance = new GrouperWorkflowInstance();
    instance.setGrouperWorkflowInstanceFilesInfo(new GrouperWorkflowInstanceFilesInfo());
    instance.setGrouperWorkflowInstanceLogEntries(new GrouperWorkflowInstanceLogEntries());
    instance.setGrouperWorkflowInstanceParamValue0(new GrouperWorkflowInstanceParamValue());
    instance.setOwnerGrouperObject(ownerGroup);
    instance.setWorkflowInstanceConfigMarkerAssignmentId(configAttributeAssignId);
    instance.setWorkflowInstanceEncryptionKey("encryption key");
    instance.setWorkflowInstanceFileInfoString("{}");
    instance.setWorkflowInstanceUuid("test uuid");
    instance.setWorkflowInstanceState("initiate");
    instance.setWorkflowInstanceParamValue0String("{}");
    instance.setWorkflowInstanceLogEntriesString("{}");
    instance.setWorkflowInstanceInitiatedMillisSince1970(11111L);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(11111L);
    return instance;
  }
  
  
  private static AttributeAssign saveWorkflowInstanceAttributeMetadata(Group group, 
      String attributeAssignOfWorkflowConfig) {
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().
        addAttribute(GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ENCRYPTION_KEY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "encryption key");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), attributeAssignOfWorkflowConfig);
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_FILE_INFO, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "{}");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_INITIATED_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(111111L));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_UPDATED_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(111111L));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LOG, true);
    
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setAction("initiate");
    logEntry.setSubjectId(SubjectTestHelper.SUBJ0_ID);
    logEntry.setState("initiate");
    
    GrouperWorkflowInstanceLogEntries logEntries = new GrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(logEntry);
    
    try {      
      String logEntriesString = GrouperWorkflowSettings.objectMapper.writeValueAsString(logEntries);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), logEntriesString);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_0, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "{}");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_STATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "initiate");
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_UUID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "test uuid");
    
    return attributeAssign;
    
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
