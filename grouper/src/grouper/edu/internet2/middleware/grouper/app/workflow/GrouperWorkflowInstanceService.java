package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowApprovalState.REJECTED_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceLogEntry.INITIATE_ACTION;
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
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.StaticEncryptionMaterialsProvider;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
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
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstanceService {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowInstanceService.class);
  
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
   * get workflow instances for a given group and worklfow config id
   * @param group
   * @return
   */
  public static List<GrouperWorkflowInstance> getWorkflowInstances(Group group, String grouperWorkflowConfigId) {
    
    List<GrouperWorkflowInstance> result = new ArrayList<GrouperWorkflowInstance>();
    
    for (GrouperWorkflowInstance instance: getWorkflowInstances(group)) {
      if (instance.getGrouperWorkflowConfig().getWorkflowConfigId().equals(grouperWorkflowConfigId)) {
        result.add(instance);
      }
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
      Map<GrouperWorkflowConfigParam, String> paramNamesValues) {
    
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
    
    //String htmlForm = workflowConfig.buildHtmlFromParams(true, nextState);
    String htmlForm = null;
    if (StringUtils.isNotBlank(workflowConfig.getWorkflowConfigForm())) {
      htmlForm = workflowConfig.buildHtmlFromConfigForm(nextState);
    } else {      
      htmlForm = workflowConfig.buildHtmlFromParams(false, nextState);
    }
    
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
    
    Document document = Jsoup.parse(htmlForm);
    
    GrouperWorkflowConfigParams configParams = workflowConfig.getConfigParams();
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      String paramName = workflowConfigParam.getParamName();
      String newValue = paramNamesValues.get(workflowConfigParam);
      try {
        
        Method getParamMethod = instance.getClass().getMethod("getGrouperWorkflowInstanceParamValue"+String.valueOf(i));
        GrouperWorkflowInstanceParamValue paramValueObjectBeforeUpdate = (GrouperWorkflowInstanceParamValue) getParamMethod.invoke(instance);
        
        String valueBeforeUpdate = paramValueObjectBeforeUpdate.getParamValue();
        List<String> editableStates = workflowConfigParam.getEditableInStates();
        
        if (!editableStates.contains(currentState) && !StringUtils.equals(valueBeforeUpdate, newValue)) {
          throw new RuntimeException("Values cannot be changed");
        }
        
        if (StringUtils.isBlank(newValue) && workflowConfigParam.isRequired()) {
          //TODO send error back
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
        
        Element element = document.selectFirst("[name="+paramName+"]");
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(newValue) && newValue.equals("on")) {
            element.attr("checked", "checked");
            // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          element.val(newValue);
          // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", newValue);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(currentState);
    
    instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().add(fileInfo);
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    
    saveWorkflowFile(encryptionKey, document.html(), instance, fileInfo, workflowConfig);
    
    saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
    
  }
  
  /**
   * disapprove workflow
   * @param instance
   * @param subject
   */
  public static void disapproveWorkflow(GrouperWorkflowInstance instance, Subject subject, 
      Map<String, String> paramNamesValues) {
    
    Date now = new Date();
    String currentState = instance.getWorkflowInstanceState();
    
    GrouperWorkflowConfig workflowConfig = instance.getGrouperWorkflowConfig();
    String nextState = workflowConfig.getWorkflowApprovalStates()
        .stateAfter(currentState).getStateName();
    
    instance.setWorkflowInstanceState(REJECTED_STATE);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntry logEntry = new GrouperWorkflowInstanceLogEntry();
    logEntry.setState(REJECTED_STATE);
    logEntry.setAction("disapprove");
    logEntry.setSubjectId(subject.getId());
    logEntry.setSubjectSourceId(subject.getSourceId());
    logEntry.setMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(logEntry);
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    
    // String htmlForm = workflowConfig.buildHtmlFromParams(true, nextState);
    String htmlForm = null;
    if (StringUtils.isNotBlank(workflowConfig.getWorkflowConfigForm())) {
      htmlForm = workflowConfig.buildHtmlFromConfigForm(nextState);
    } else {      
      htmlForm = workflowConfig.buildHtmlFromParams(false, nextState);
    }
    
    // add auditing at the bottom
    StringBuilder htmlFormWithAudit = new StringBuilder(htmlForm);
    
    String auditLine = GrouperTextContainer.retrieveFromRequest().getText().get("workflowFormAuditLine");
    
    auditLine = auditLine.replace("$$subjectSource$$", subject.getSource().getName());
    auditLine = auditLine.replace("$$subjectId$$", subject.getId());
    auditLine = auditLine.replace("$$subjectName$$", subject.getName());
    auditLine = auditLine.replace("$$buttonText$$", "disapprove");
    auditLine = auditLine.replace("$$state$$", nextState);
    
    String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(now);
    
    auditLine = auditLine.replace("$$timestamp$$", timestamp);
    
    htmlFormWithAudit.append("<div>");
    htmlFormWithAudit.append(auditLine);
    htmlFormWithAudit.append("</div>");
    
    htmlForm = htmlFormWithAudit.toString();
    
    Document document = Jsoup.parse(htmlForm);
    
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
        
        //TODO make sure required fields are not being changed to blank
        
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
        
        Element element = document.selectFirst("[name="+paramName+"]");
        
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(newValue) && newValue.equals("on")) {
            element.attr("checked", "checked");
            // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          element.val(newValue);
          // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", newValue);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(currentState);
    
    instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().add(fileInfo);
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    
    saveWorkflowFile(encryptionKey, document.html(), instance, fileInfo, workflowConfig);
    
    saveWorkflowInstanceAttributes(instance, instance.getOwnerGrouperObject());
    
  }
  
  public static List<String> validateInitiateFormValues(Map<GrouperWorkflowConfigParam, String> paramNamesValues) {
    
    List<String> errors = new ArrayList<String>();
    
    final Map<String, String> contentKeys = GrouperTextContainer.retrieveFromRequest().getText();
    
    for (Map.Entry<GrouperWorkflowConfigParam, String> entry: paramNamesValues.entrySet()) {
      
      GrouperWorkflowConfigParam param = entry.getKey();
      String paramValue = entry.getValue();
      
      if (StringUtils.isNotBlank(paramValue) && !param.getEditableInStates().contains(INITIATE_STATE)) {
        String error = contentKeys.get("workflowSubmitFormFieldNotEditable");
        error = error.replace("$$fieldName$$", param.getParamName());
        errors.add(error);
      }
      
      if (StringUtils.isBlank(paramValue) && param.isRequired()) {
        String error = contentKeys.get("workflowSubmitFormFieldRequired");
        error = error.replace("$$fieldName$$", param.getParamName());
        errors.add(error);
      }
      
    }
    
    return errors;
    
  }
  
  /**
   * save instance when workflow is initiated
   */
  public static void saveInitiateStateInstance(GrouperWorkflowConfig grouperWorkflowConfig, 
      Subject subject, Map<GrouperWorkflowConfigParam, String> paramNamesValues, Group group) {
    
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
    
    String htmlForm = null;
    if (StringUtils.isNotBlank(grouperWorkflowConfig.getWorkflowConfigForm())) {
      htmlForm = grouperWorkflowConfig.buildHtmlFromConfigForm(INITIATE_STATE);
    } else {
      htmlForm = grouperWorkflowConfig.buildHtmlFromParams(false, INITIATE_STATE);
    }
    
    // String htmlForm = grouperWorkflowConfig.buildHtmlFromParams(true, INITIATE_STATE);
    
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
    
    Document document = Jsoup.parse(htmlForm);
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      String paramName = workflowConfigParam.getParamName();
      String value = paramNamesValues.get(workflowConfigParam);
      try {
        
        GrouperWorkflowInstanceParamValue paramValue = new GrouperWorkflowInstanceParamValue();
        paramValue.setParamName(paramName);
        paramValue.setParamValue(value);
        paramValue.setEditedByMemberId(subject.getId());
        paramValue.setEditedInState(INITIATE_STATE);
        paramValue.setLastUpdatedMillis(now.getTime());
       
        Method method = instance.getClass().getMethod("setGrouperWorkflowInstanceParamValue"+String.valueOf(i), GrouperWorkflowInstanceParamValue.class);
        method.invoke(instance, paramValue);
        Element element = document.selectFirst("[name="+paramName+"]");
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(value) && value.equals("on")) {
            element.attr("checked", "checked");
            // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          element.val(value);
          // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", value);
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
    
    saveWorkflowFile(randomEncryptionKey, document.html(), instance, fileInfo, grouperWorkflowConfig);
    
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
      uploadFileToS3(fileContents, instance, fileInfo, randomEncryptionKey);
    } else {
      String workflowFileOutputDirectory = GrouperConfig.retrieveConfig().propertyValueString("workflow.file.system.path");
      if (StringUtils.isBlank(workflowFileOutputDirectory)) {          
        throw new RuntimeException("workflow.file.system.path cannot be blank");
      }
      saveFileToFileSystem(workflowFileOutputDirectory, randomEncryptionKey, 
          instance, fileContents, fileInfo, workflowConfig);
    }
    
  }
  
  public static List<GrouperWorkflowInstance> getWorkflowInstancesSubmitted(Subject subject) {
    
    List<GrouperWorkflowInstance> instancesSubmitted = new ArrayList<GrouperWorkflowInstance>();
    Set<Group> groupsWithWorkflowInstance = findGroupsWithWorkflowInstance();
    
    for (Group group: groupsWithWorkflowInstance) {
      
      List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: workflowInstances) {
        
        GrouperWorkflowConfig grouperWorkflowConfig = instance.getGrouperWorkflowConfig();
        
        GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
            .getLogEntryByActionName(INITIATE_ACTION);
          
          String subjetWhoInitiatedId = workflowInstanceLogEntry.getSubjectId();
          Subject subjetWhoInitiated = SubjectFinder.findById(subjetWhoInitiatedId, false);
          if (subjetWhoInitiated == null) {
            LOG.error("For workflow config id: "+grouperWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists. subject id is "+subjetWhoInitiatedId);
            continue;
          }
          
          if (subject.getId().equals(subjetWhoInitiated.getId())) {
            instancesSubmitted.add(instance);
          }
        
      }
    }
    
    return instancesSubmitted;
    
  }
  
  public static List<GrouperWorkflowInstance> getWorkflowInstancesWaitingForApproval(Subject subject) {
    
    List<GrouperWorkflowInstance> instancesWaitingForApproval = new ArrayList<GrouperWorkflowInstance>();
    
    Set<Group> groupsWithWorkflowInstance = findGroupsWithWorkflowInstance();
    
    for (Group group: groupsWithWorkflowInstance) {
      
      List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
      
      for (GrouperWorkflowInstance instance: workflowInstances) {
        if (canInstanceBeApproved(instance, subject)) {
          instancesWaitingForApproval.add(instance);
        }
      }
    }
    
    return instancesWaitingForApproval;
    
  }
  
  public static boolean canInstanceBeViewed(GrouperWorkflowInstance instance, Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    List<GrouperWorkflowInstanceLogEntry> logEntries = instance.getGrouperWorkflowInstanceLogEntries().getLogEntries();
    for (GrouperWorkflowInstanceLogEntry entry: logEntries) {
      if (StringUtils.isNotBlank(entry.getSubjectId()) && entry.getSubjectId().equals(subject.getId())) {
        return true;
      }
    }
    
    List<GrouperWorkflowInstance> instancesWaitingForApproval = getWorkflowInstancesWaitingForApproval(subject);
    if (instancesWaitingForApproval.contains(instance)) {
      return true;
    }
    
    return false;
  }
  
  public static boolean canInstanceBeApproved(GrouperWorkflowInstance instance, Subject subject) {
    
    List<String> statesToIgnore = Arrays.asList(COMPLETE_STATE, REJECTED_STATE, EXCEPTION_STATE);
    
    if (!statesToIgnore.contains(instance.getWorkflowInstanceState())) {
      GrouperWorkflowConfig grouperWorkflowConfig = instance.getGrouperWorkflowConfig();
      GrouperWorkflowApprovalStates approvalStates = grouperWorkflowConfig.getWorkflowApprovalStates();
      
      GrouperWorkflowApprovalState workflowApprovalState = approvalStates.getStateByName(instance.getWorkflowInstanceState());
      if (workflowApprovalState == null) {
        throw new RuntimeException("Could not find state with name: "+instance.getWorkflowInstanceState());
      }
      
      if (PrivilegeHelper.isWheelOrRoot(subject)) {
        return true;
      } 
      
      List<Subject> approvers = getApprovers(workflowApprovalState);
      if (approvers.contains(subject)) {
        return true;
      }
    }
    
    return false;
  }
  
  //TODO move to GrouperWorkflowApprovalState or GrouperWorkflowInstance
  private static List<Subject> getApprovers(GrouperWorkflowApprovalState approvalState) {
    
    List<Subject> approvers = new ArrayList<Subject>();
    
    String approverGroupId = approvalState.getApproverGroupId();
    if (StringUtils.isNotBlank(approverGroupId)) {
      Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), approverGroupId, false);
      if (approverGroup == null) {
        LOG.error("group not found for id: "+approverGroupId);
      } else {
        for (Member member: approverGroup.getMembers()) {
          approvers.add(member.getSubject());
        }
      }
    }
    
    String managersOfGroupId = approvalState.getApproverManagersOfGroupId();
    if (StringUtils.isNotBlank(managersOfGroupId)) {
      Group managersGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), managersOfGroupId, false);
      if (managersGroup == null) {
        LOG.error("group not found for id: "+managersOfGroupId);
      } else {
        approvers.addAll(managersGroup.getAdmins());
        approvers.addAll(managersGroup.getUpdaters());
      }
    }
    
    String approverSubjectId = approvalState.getApproverSubjectId();
    if (StringUtils.isNotBlank(approverSubjectId)) {
      Subject approverSubject = SubjectFinder.findById(approverSubjectId, false);
      approvers.add(approverSubject);
    }
    
    return approvers;
    
  }
  
  /**
   * upload given file to file system
   * @param file
   * @param encryptionKey
   * @return s3 url of file 
   */
  private static void uploadFileToS3(String fileContents, GrouperWorkflowInstance instance,
      GrouperWorkflowInstanceFileInfo fileInfo, String encryptionKey) {
    
    String bucketName = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.bucket.name");
    String region = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.region");
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.secret.key");
    
    GrouperWorkflowConfig workflowConfig = instance.getGrouperWorkflowConfig();
    
    String fileName = workflowConfig.getWorkflowConfigId()+
        "_"+instance.getWorkflowInstanceState()+"_"+new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());
    
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
    
    try {
      
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
      
      AmazonS3 s3Client = AmazonS3EncryptionClientBuilder.standard()
              .withRegion(region)
              .withCredentials(credentialsProvider)
              .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(encryptionMaterials))
              .build();
      
      s3Client.putObject(bucketName, fileName, fileContents);
      
      fileInfo.setFileName(fileName);
      fileInfo.setFilePointer(s3Client.getUrl(bucketName, fileName).toString());
    } catch(AmazonServiceException e) {
      LOG.error("Error creating workflow file in S3. file name: "+fileName+" instance id: "+instance.getAttributeAssignId());
      throw new RuntimeException("Error creating workflow file in S3");
    } catch(SdkClientException e) {
      LOG.error("Error creating workflow file in S3. file name: "+fileName+" instance id: "+instance.getAttributeAssignId());
      throw new RuntimeException("Error creating workflow file in S3");
    }
    
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
