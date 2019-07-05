package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstanceService {
  
  /**
   * get workflow instance by attribute assign id
   * @param attributeAssignId
   * @return
   */
  public static GrouperWorkflowInstance getWorkfowInstance(String attributeAssignId) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    return buildWorkflowInstance(attributeAssign);
  }
  
  /**
   * get workflow instances for a given group
   * @param group
   * @return
   */
  public static List<GrouperWorkflowInstance> getWorkflowInstances(Group group) {
    
    List<GrouperWorkflowInstance> result = new ArrayList<GrouperWorkflowInstance>();
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefNameBase());
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      result.add(buildWorkflowInstance(attributeAssign));
    }
    
    return result;
    
  }
  
  /**
   * @return set of groups that have workflow instances
   */
  public static Set<Group> findGroupsWithWorkflowInstance() {
    
    if (!GrouperWorkflowSettings.workflowEnabled()) {
      return new HashSet<Group>();
    }
    
    Set<Group> groups = new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
      .assignNameOfAttributeDefName(workflowStemName()+":"+GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME)
      .findGroups();
    
    return groups;
  }
  
  /**
   * save workflow instance attributes
   * @param workflowInstance
   * @param grouperObject
   */
  public static void saveWorkflowInstanceAttributes(GrouperWorkflowInstance workflowInstance, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = null;
    
    if (StringUtils.isNotBlank(workflowInstance.getAttributeAssignId())) {
      attributeAssign = AttributeAssignFinder.findById(workflowInstance.getAttributeAssignId(), true);
    } else {
      attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceConfigMarkerAssignmentId());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_STATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceState());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ENCRYPTION_KEY, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceEncryptionKey());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ERROR, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(workflowInstance.getWorkflowInstanceError()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_FILE_INFO, true);
    try {
      String fileInfo = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceFilesInfo());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), fileInfo);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance file info to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_INITIATED_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(workflowInstance.getWorkflowInstanceInitiatedMillisSince1970()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_DATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceLastEmailedDate());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_STATE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceLastEmailedState());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_UPDATED_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(workflowInstance.getWorkflowInstanceLastUpdatedMillisSince1970()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LOG, true);
    try {
      String logEntries = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceLogEntries());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), logEntries);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance log entries to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_0, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue0());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_1, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue1());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_2, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue2());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_3, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue3());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_4, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue4());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_5, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue5());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_6, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue6());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_7, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue7());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_8, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue8());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_9, true);
    try {
      String paramValue = GrouperWorkflowSettings.objectMapper.writeValueAsString(workflowInstance.getGrouperWorkflowInstanceParamValue9());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), paramValue);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow instance param value to json string");
    }
    
    attributeAssign.saveOrUpdate();
    workflowInstance.setAttributeAssignId(attributeAssign.getId());
    
  }
  
  /**
   * approve workflow
   * @param instance
   * @param subject
   */
  public static void approveWorkflow(GrouperWorkflowInstance instance, Subject subject, 
      Map<String, String> paramNamesValues) {
    
    Date now = new Date();
    String currentState = instance.getWorkflowInstanceState();
    
    GrouperWorkflowConfig workflowConfig = instance.getGrouperWorkflowConfig();
    String nextState = workflowConfig.getWorkflowApprovalStates()
        .stateAfter(currentState).getStateName();
    
    instance.setWorkflowInstanceState(nextState);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setState(nextState);
    logEntry.setAction("approve");
    logEntry.setSubjectId(subject.getId());
    logEntry.setSubjectSourceId(subject.getSourceId());
    logEntry.setMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(logEntry);
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    
    String htmlForm = workflowConfig.buildHtmlFromParams(true, nextState);
    
    // add auditing at the bottom
    StringBuilder htmlFormWithAudit = new StringBuilder(htmlForm);
    
    String auditLine = GrouperTextContainer.retrieveFromRequest().getText().get("workflowFormAuditLine");
    
    auditLine = auditLine.replace("$$subjectSource$$", subject.getSource().getName());
    auditLine = auditLine.replace("$$subjectId$$", subject.getId());
    auditLine = auditLine.replace("$$subjectName$$", subject.getName());
    auditLine = auditLine.replace("$$buttonText$$", "approve");
    auditLine = auditLine.replace("$$state$$", nextState);
    
    String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now);
    
    auditLine = auditLine.replace("$$timestamp$$", timestamp);
    
    htmlFormWithAudit.append("<div>");
    htmlFormWithAudit.append(auditLine);
    htmlFormWithAudit.append("</div>");
    
    htmlForm = htmlFormWithAudit.toString();
    
    GrouperWorkflowConfigParams configParams = workflowConfig.getConfigParams();
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      String paramName = workflowConfigParam.getParamName();
      String newValue = paramNamesValues.get(paramName);
      try {
        
        Method getParamMethod = instance.getClass().getMethod("getGrouperWorkflowInstanceParamValue"+String.valueOf(i));
        GrouperWorkflowInstanceParamValue paramValueObjectBeforeUpdate = (GrouperWorkflowInstanceParamValue) getParamMethod.invoke(instance);
        
        String valueBeforeUpdate = paramValueObjectBeforeUpdate.getParamValue();
        List<String> editableStates = workflowConfigParam.getEditableInStates();
        
        if (!editableStates.contains(currentState) && !StringUtils.equals(valueBeforeUpdate, newValue)) {
          throw new RuntimeException("Values cannot be changed");
        }
        
        // if value not changed, nothing to update - just store the same object again
        GrouperWorkflowInstanceParamValue paramValueObjectAfterUpdate = paramValueObjectBeforeUpdate;
        
        if (!StringUtils.equals(valueBeforeUpdate, newValue)) {
          paramValueObjectAfterUpdate.setEditedByMemberId(subject.getId());
          paramValueObjectAfterUpdate.setEditedInState(currentState);
          paramValueObjectAfterUpdate.setLastUpdatedMillis(now.getTime());
          paramValueObjectAfterUpdate.setParamValue(newValue);
        }
       
        Method method = instance.getClass().getMethod("setGrouperWorkflowInstanceParamValue"+String.valueOf(i), GrouperWorkflowInstanceParamValue.class);
        method.invoke(instance, paramValueObjectAfterUpdate);
        
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(newValue) && newValue.equals("on")) {
            htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", newValue);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(currentState);
    
    instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().add(fileInfo);
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    
    saveWorkflowFile(encryptionKey, htmlForm, instance, fileInfo, workflowConfig);
    
    saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
    
  }
  
  /**
   * save instance when workflow is initiated
   */
  
  public static void saveInitiateStateInstance(GrouperWorkflowConfig grouperWorkflowConfig, 
      Subject subject, Map<String, String> paramNamesValues, Group group) {
    
    Date now = new Date();
    String randomEncryptionKey = RandomStringUtils.random(16, true, true);
    
    GrouperWorkflowInstance instance = new GrouperWorkflowInstance();
    instance.setWorkflowInstanceState(INITIATE_STATE); 
    instance.setWorkflowInstanceConfigMarkerAssignmentId(grouperWorkflowConfig.getAttributeAssignmentMarkerId());
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(now.getTime());
    instance.setWorkflowInstanceInitiatedMillisSince1970(now.getTime());
    instance.setWorkflowInstanceUuid(GrouperUuid.getUuid());
    instance.setWorkflowInstanceEncryptionKey(Morph.encrypt(randomEncryptionKey));
    
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setState(INITIATE_STATE);
    logEntry.setAction(INITIATE_STATE);
    logEntry.setSubjectId(subject.getId());
    logEntry.setSubjectSourceId(subject.getSourceId());
    logEntry.setMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntries logEntries = new GrouperWorkflowInstanceLogEntries();
    logEntries.setLogEntries(Arrays.asList(logEntry));
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
   
    GrouperWorkflowConfigParams configParams = grouperWorkflowConfig.getConfigParams();
    
    String htmlForm = grouperWorkflowConfig.buildHtmlFromParams(true, INITIATE_STATE);
    
    // add auditing at the bottom
    StringBuilder htmlFormWithAudit = new StringBuilder(htmlForm);
    
    String auditLine = GrouperTextContainer.retrieveFromRequest().getText().get("workflowFormAuditLine");
    
    auditLine = auditLine.replace("$$subjectSource$$", subject.getSource().getName());
    auditLine = auditLine.replace("$$subjectId$$", subject.getId());
    auditLine = auditLine.replace("$$subjectName$$", subject.getName());
    auditLine = auditLine.replace("$$buttonText$$", "submit");
    auditLine = auditLine.replace("$$state$$", INITIATE_STATE);
    
    String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now);
    
    auditLine = auditLine.replace("$$timestamp$$", timestamp);
    
    htmlFormWithAudit.append("<div>");
    htmlFormWithAudit.append(auditLine);
    htmlFormWithAudit.append("</div>");
    
    htmlForm = htmlFormWithAudit.toString();
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      String paramName = workflowConfigParam.getParamName();
      String value = paramNamesValues.get(paramName);
      try {
        
        GrouperWorkflowInstanceParamValue paramValue = new GrouperWorkflowInstanceParamValue();
        paramValue.setParamName(paramName);
        paramValue.setParamValue(value);
        paramValue.setEditedByMemberId(subject.getId());
        paramValue.setEditedInState(INITIATE_STATE);
        paramValue.setLastUpdatedMillis(now.getTime());
       
        Method method = instance.getClass().getMethod("setGrouperWorkflowInstanceParamValue"+String.valueOf(i), GrouperWorkflowInstanceParamValue.class);
        method.invoke(instance, paramValue);
        
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(value) && value.equals("on")) {
            htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", value);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(INITIATE_STATE);
    GrouperWorkflowInstanceFilesInfo filesInfo = new GrouperWorkflowInstanceFilesInfo();
    filesInfo.getFileNamesAndPointers().add(fileInfo);
    instance.setGrouperWorkflowInstanceFilesInfo(filesInfo);
    
    saveWorkflowFile(randomEncryptionKey, htmlForm, instance, fileInfo, grouperWorkflowConfig);
    
    saveWorkflowInstanceAttributes(instance, group);
    
  }
  
  public static boolean subjectAlreadySubmittedWorkflow(Subject subject, Group group) {
    
    List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
    
    for (GrouperWorkflowInstance instance: workflowInstances) {
      if (!instance.getWorkflowInstanceState().equals(COMPLETE_STATE)) {
        GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
        for (GrouperWorkflowInstanceLogEntry entry: logEntries.getLogEntries()) {
          if (entry.getAction().equals(INITIATE_STATE) && entry.getSubjectId().equals(subject.getId())) {
            return true;
          }
        }
      }
    }
    
    return false;
  }
  
  
  private static void saveWorkflowFile(String randomEncryptionKey,  String fileContents, GrouperWorkflowInstance instance,
      GrouperWorkflowInstanceFileInfo fileInfo, GrouperWorkflowConfig workflowConfig) {
    
    String workflowFileDestinationType = GrouperConfig.retrieveConfig().propertyValueString("workflow.storage.option");
    if (StringUtils.isBlank(workflowFileDestinationType)) {
      throw new RuntimeException("workflow.storage.option cannot be blank. Valid values are S3 and fileSystem");
    }
    
    if (!workflowFileDestinationType.equals("S3") && !workflowFileDestinationType.equals("fileSystem")) {
      throw new RuntimeException("workflow.storage.option is not valid. Use S3 or fileSystem");
    }
      
    if (workflowFileDestinationType.equals("S3")) {
      //String s3Url = uploadFileToS3(instance, encryptionKey);
      //instance.setInstanceFilePointer(s3Url);
    } else {
      String workflowFileOutputDirectory = GrouperConfig.retrieveConfig().propertyValueString("workflow.file.system.path");
      if (StringUtils.isBlank(workflowFileOutputDirectory)) {          
        throw new RuntimeException("workflow.file.system.path cannot be blank");
      }
      saveFileToFileSystem(workflowFileOutputDirectory, randomEncryptionKey, 
          instance, fileContents, fileInfo, workflowConfig);
    }
    
  }
  
  public static List<GrouperWorkflowInstance> getWorkflowInstancesWaitingForApproval(Subject subject) {
    
    List<GrouperWorkflowInstance> instancesWaitingForApproval = new ArrayList<GrouperWorkflowInstance>();
    
    Set<Group> groupsWithWorkflowInstance = findGroupsWithWorkflowInstance();
    
    List<String> statesToIgnore = Arrays.asList(INITIATE_STATE, COMPLETE_STATE, EXCEPTION_STATE);
    
    for (Group group: groupsWithWorkflowInstance) {
      
      List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: workflowInstances) {
        
        if (!statesToIgnore.contains(instance.getWorkflowInstanceState())) {
          GrouperWorkflowConfig grouperWorkflowConfig = instance.getGrouperWorkflowConfig();
          GrouperWorkflowApprovalStates approvalStates = grouperWorkflowConfig.getWorkflowApprovalStates();
          
          GrouperWorkflowApprovalState workflowApprovalState = approvalStates.getStateByName(instance.getWorkflowInstanceState());
          if (workflowApprovalState == null) {
            throw new RuntimeException("Could not find state with name: "+instance.getWorkflowInstanceState());
          }
          
          // check if the subject is in approver group
          String approverManagersOfGroupId = workflowApprovalState.getApproverManagersOfGroupId();
          Group managersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), approverManagersOfGroupId, false);
          if (managersGroup == null) {
            throw new RuntimeException("Could not find group with id: "+approverManagersOfGroupId);
          }
          
          if (managersGroup.hasMember(subject)) {
            instancesWaitingForApproval.add(instance);
          }
          
        }

      }
    }
    
    return instancesWaitingForApproval;
    
  }
  
  private static void saveFileToFileSystem(String directory, String encryptionKey, GrouperWorkflowInstance instance, 
      String fileContents, GrouperWorkflowInstanceFileInfo fileInfo, GrouperWorkflowConfig workflowConfig) {
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(System.currentTimeMillis());
    
    String baseDirectory = directory.endsWith(File.separator) ? directory: directory + File.separator;
    
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    
    String fileName = workflowConfig.getWorkflowConfigId()+
          "_"+instance.getWorkflowInstanceState()+"_"+new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
    
    try {
      
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.ENCRYPT_MODE, encryptionKeySecret);
       
      //byte[] outputBytes = cipher.doFinal(fileContents.getBytes());
      byte[] outputBytes = fileContents.getBytes();
      
      GrouperUtil.mkdirs(new File(baseDirectory));
       
      File outputFile = new File(baseDirectory+fileName);
      GrouperUtil.fileCreateNewFile(outputFile);
       
      FileOutputStream outputStream = new FileOutputStream(outputFile);
      outputStream.write(outputBytes);
       
      outputStream.close();
      
      fileInfo.setFileName(fileName);
      fileInfo.setFilePointer(baseDirectory+fileName);

    } catch (Exception e) {
      throw new RuntimeException("Could not write html file");
    }
        
  }
  
  /**
   * build workflow instance from attribute assign values
   * @param attributeAssign
   * @return
   */
  private static GrouperWorkflowInstance buildWorkflowInstance(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperWorkflowInstance result = new GrouperWorkflowInstance();
    
    result.setAttributeAssignId(attributeAssign.getId());
    
    result.setOwnerGrouperObject(attributeAssign.getOwnerGroup());
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID);
    result.setWorkflowInstanceConfigMarkerAssignmentId(assignValue != null ? assignValue.getValueString(): null);
    if (assignValue != null) {
      GrouperWorkflowConfig grouperWorkflowConfig = GrouperWorkflowConfigService.getWorkflowConfig(assignValue.getValueString());
      result.setGrouperWorkflowConfig(grouperWorkflowConfig);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ENCRYPTION_KEY);
    result.setWorkflowInstanceEncryptionKey(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_STATE);
    result.setWorkflowInstanceState(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ERROR);
    result.setWorkflowInstanceError(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_FILE_INFO);
    result.setGrouperWorkflowInstanceFilesInfo(GrouperWorkflowInstance.buildInstanceFileInfoFromJsonString(assignValue.getValueString()));
    
    result.setWorkflowInstanceFileInfoString(assignValue.getValueString());
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_INITIATED_MILLIS_SINCE_1970);
    result.setWorkflowInstanceInitiatedMillisSince1970(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_DATE);
    result.setWorkflowInstanceLastEmailedDate(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_STATE);
    result.setWorkflowInstanceLastEmailedState(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LAST_UPDATED_MILLIS_SINCE_1970);
    result.setWorkflowInstanceLastUpdatedMillisSince1970(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_LOG);
    result.setGrouperWorkflowInstanceLogEntries(GrouperWorkflowInstance.buildInstanceLogEntriesFromJsonString(assignValue.getValueString()));
    
    result.setWorkflowInstanceLogEntriesString(assignValue != null ? assignValue.getValueString(): null);
    
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_0);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue0(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue0String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_1);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue1(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue1String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_2);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue2(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue2String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_3);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue3(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue3String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_4);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue4(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue4String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_5);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue5(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue5String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_6);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue6(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue6String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_7);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue7(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue7String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_8);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue8(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue8String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_9);
    if (assignValue != null && StringUtils.isNotBlank(assignValue.getValueString())) {
      result.setGrouperWorkflowInstanceParamValue9(GrouperWorkflowInstance.buildParamValueFromJsonString(assignValue.getValueString()));
      result.setWorkflowInstanceParamValue9String(assignValue != null ? assignValue.getValueString(): null);
    }
    
    return result;
    
  }

}
