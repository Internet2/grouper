package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.APPROVE_ACTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.COMPLETE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.DISAPPROVE_ACTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.EXCEPTION_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_ACTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.REJECTED_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.EncryptionMaterials;
import com.amazonaws.services.s3.model.S3Object;
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
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowInstanceService {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowInstanceService.class);
  
  private static ExpirableCache<Subject, List<String>> subjectInitiatedInstances = new ExpirableCache<Subject, List<String>>(5);
  
  private static ExpirableCache<Subject, List<String>> subjectWaitingForApprovalInstances = new ExpirableCache<Subject, List<String>>(5);
  
  
  /**
   * get workflow instance by attribute assign id
   * @param attributeAssignId
   * @return
   */
  public static GrouperWorkflowInstance getWorkflowInstance(String attributeAssignId) {
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
    
    return new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
      .assignNameOfAttributeDefName(workflowStemName()+":"+GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME)
      .findGroups();
    
  }
  
  /**
   * save workflow instance attributes
   * @param workflowInstance
   * @param grouperObject
   */
  public static void saveOrUpdateWorkflowInstance(GrouperWorkflowInstance workflowInstance, GrouperObject grouperObject) {
    
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
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_UUID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), workflowInstance.getWorkflowInstanceUuid());
    
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
    
    String htmlForm = getCurrentHtmlContent(instance);
        
    String htmlFormWithAudit = appendAuditLine(htmlForm, subject, currentState, APPROVE_ACTION, now);
    
    String populatedHtmlForm = populateHtmlForm(instance, workflowConfig, subject, 
        now, htmlFormWithAudit, paramNamesValues);
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(currentState);
    
    instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().add(fileInfo);
    
    instance.setWorkflowInstanceState(nextState);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(GrouperWorkflowInstanceLogEntry.createLogEntry(subject, now, nextState, APPROVE_ACTION));
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    
    saveWorkflowFile(encryptionKey, populatedHtmlForm, instance, fileInfo, workflowConfig);
    
    saveOrUpdateWorkflowInstance(instance, instance.getOwnerGrouperObject());
    
  }
  
  /**
   * disapprove workflow
   * @param instance
   * @param subject
   */
  public static void disapproveWorkflow(GrouperWorkflowInstance instance, Subject subject, 
      Map<GrouperWorkflowConfigParam, String> paramNamesValues) {
    
    Date now = new Date();
    String currentState = instance.getWorkflowInstanceState();
    
    GrouperWorkflowConfig workflowConfig = instance.getGrouperWorkflowConfig();
    
    String htmlForm = getCurrentHtmlContent(instance);
    
    String htmlFormWithAudit = appendAuditLine(htmlForm, subject, currentState, DISAPPROVE_ACTION, now);
    
    String populatedHtmlForm = populateHtmlForm(instance, workflowConfig,
        subject, now, htmlFormWithAudit, paramNamesValues);
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(currentState);
    instance.getGrouperWorkflowInstanceFilesInfo().getFileNamesAndPointers().add(fileInfo);
    
    instance.setWorkflowInstanceState(REJECTED_STATE);
    instance.setWorkflowInstanceLastUpdatedMillisSince1970(now.getTime());
    
    GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
    logEntries.getLogEntries().add(GrouperWorkflowInstanceLogEntry.createLogEntry(subject, now, REJECTED_STATE, DISAPPROVE_ACTION));
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    
    saveWorkflowFile(encryptionKey, populatedHtmlForm, instance, fileInfo, workflowConfig);
    
    saveOrUpdateWorkflowInstance(instance, instance.getOwnerGrouperObject());
    
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
    instance.setGrouperWorkflowConfig(grouperWorkflowConfig);
    
    GrouperWorkflowInstanceLogEntries logEntries = new GrouperWorkflowInstanceLogEntries();
    logEntries.setLogEntries(Arrays.asList(GrouperWorkflowInstanceLogEntry.createLogEntry(subject, now, INITIATE_STATE, INITIATE_ACTION)));
    
    instance.setGrouperWorkflowInstanceLogEntries(logEntries);
   
    String htmlForm = grouperWorkflowConfig.buildInitialHtml(INITIATE_STATE);
    
    String htmlFormWithAudit = appendAuditLine(htmlForm, subject, INITIATE_STATE, INITIATE_ACTION, now);
    
    String populatedHtmlForm = populateHtmlForm(instance, grouperWorkflowConfig, subject, 
        now, htmlFormWithAudit, paramNamesValues);
    
    GrouperWorkflowInstanceFileInfo fileInfo = new GrouperWorkflowInstanceFileInfo();
    fileInfo.setState(INITIATE_STATE);
    GrouperWorkflowInstanceFilesInfo filesInfo = new GrouperWorkflowInstanceFilesInfo();
    filesInfo.getFileNamesAndPointers().add(fileInfo);
    instance.setGrouperWorkflowInstanceFilesInfo(filesInfo);
    
    saveWorkflowFile(randomEncryptionKey, populatedHtmlForm, instance, fileInfo, grouperWorkflowConfig);
    
    saveOrUpdateWorkflowInstance(instance, group);
    
  }
  
  /**
   * did this subject already submit workflow
   * @param subject
   * @param group
   * @return
   */
  public static boolean subjectAlreadySubmittedWorkflow(Subject subject, Group group) {
    
    List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
    
    for (GrouperWorkflowInstance instance: workflowInstances) {
      if (!instance.getWorkflowInstanceState().equals(COMPLETE_STATE)) {
        GrouperWorkflowInstanceLogEntries logEntries = instance.getGrouperWorkflowInstanceLogEntries();
        for (GrouperWorkflowInstanceLogEntry entry: logEntries.getLogEntries()) {
          if (entry.getAction().equals(INITIATE_ACTION) && entry.getSubjectId().equals(subject.getId())) {
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
  
  /**
   * get list of instances submitted
   * @param subject
   * @return
   */
  public static List<GrouperWorkflowInstance> getWorkflowInstancesSubmitted(Subject subject) {
    
    List<String> attributeAssignIds = subjectInitiatedInstances.get(subject);
    
    List<GrouperWorkflowInstance> instancesSubmitted = new ArrayList<GrouperWorkflowInstance>();
    
    if (attributeAssignIds != null) {
      
      for (String attributeAssignId: attributeAssignIds) {
        GrouperWorkflowInstance workfowInstance = getWorkflowInstance(attributeAssignId);
        instancesSubmitted.add(workfowInstance);
      }
      
    } else {
      
      attributeAssignIds = new ArrayList<String>();
      Set<Group> groupsWithWorkflowInstance = findGroupsWithWorkflowInstance();
      
      for (Group group: groupsWithWorkflowInstance) {
        
        List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
        
        for (GrouperWorkflowInstance instance: workflowInstances) {
          
          GrouperWorkflowConfig grouperWorkflowConfig = instance.getGrouperWorkflowConfig();
          
          Subject subjetWhoInitiated = subjectWhoInitiatedWorkflow(instance);
          if (subjetWhoInitiated == null) {
            LOG.error("For workflow config id: "+grouperWorkflowConfig.getWorkflowConfigId()+" subject that requested to join the group no longer exists.");
            continue;
          }
          
          if (subject.getId().equals(subjetWhoInitiated.getId())) {
            instancesSubmitted.add(instance);
            attributeAssignIds.add(instance.getAttributeAssignId());
          }
        }
      }
      
      subjectInitiatedInstances.put(subject, attributeAssignIds);
    }
    
    return instancesSubmitted;
  }
  
  /**
   * find subject who initiated workflow from log entries
   * @param instance
   * @return
   */
  public static Subject subjectWhoInitiatedWorkflow(GrouperWorkflowInstance instance) {
    
    GrouperWorkflowInstanceLogEntry workflowInstanceLogEntry = instance.getGrouperWorkflowInstanceLogEntries()
        .getLogEntryByActionName(INITIATE_ACTION);
      
    String subjetWhoInitiatedId = workflowInstanceLogEntry.getSubjectId();
    Subject subjetWhoInitiated = SubjectFinder.findById(subjetWhoInitiatedId, true);
    return subjetWhoInitiated;
  }
   
  /**
   * get instances waiting for approval for this subject
   * @param subject
   * @return
   */
  public static List<GrouperWorkflowInstance> getWorkflowInstancesWaitingForApproval(Subject subject) {
    
    List<String> attributeAssignIds = subjectWaitingForApprovalInstances.get(subject);
    List<GrouperWorkflowInstance> instancesWaitingForApproval = new ArrayList<GrouperWorkflowInstance>();
    
    if (attributeAssignIds != null) {
      
      for (String attributeAssignId: attributeAssignIds) {
        GrouperWorkflowInstance workflowInstance = getWorkflowInstance(attributeAssignId);
        instancesWaitingForApproval.add(workflowInstance);
      }
      
    } else {
      attributeAssignIds = new ArrayList<String>();
      Set<Group> groupsWithWorkflowInstance = findGroupsWithWorkflowInstance();
      
      for (Group group: groupsWithWorkflowInstance) {
        
        List<GrouperWorkflowInstance> workflowInstances = getWorkflowInstances(group);
        
        for (GrouperWorkflowInstance instance: workflowInstances) {
          if (canInstanceBeApproved(instance, subject)) {
            instancesWaitingForApproval.add(instance);
            attributeAssignIds.add(instance.getAttributeAssignId());
          }
        }
      }
      
      subjectWaitingForApprovalInstances.put(subject, attributeAssignIds);
    }
    
    return instancesWaitingForApproval;
    
  }
  
  /**
   * can the given instance be viewed by the given subject 
   * @param instance
   * @param subject
   * @return
   */
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
  
  /**
   * can the given instance be approved by the given subject
   * @param instance
   * @param subject
   * @return
   */
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
  
  /**
   * get list of approvers for given approval state
   * @param approvalState
   * @return
   */
  public static List<Subject> getApprovers(GrouperWorkflowApprovalState approvalState) {
    
    List<Subject> approvers = new ArrayList<Subject>();
    
    String approverGroupId = approvalState.getApproverGroupId();
    if (StringUtils.isNotBlank(approverGroupId)) {
      Group approverGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), approverGroupId, false);
      if (approverGroup == null) {
        approverGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), approverGroupId, false);
      }
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
        managersGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), managersOfGroupId, false);
      }
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
   * upload object to s3
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
  
  
  /**
   * retrieves current html content from workflow instance
   * @param workflow instance
   * @return
   */
  public static String getCurrentHtmlContent(GrouperWorkflowInstance instance) {
    String state = instance.getWorkflowInstanceState();
    int lastIndex = instance.getGrouperWorkflowInstanceFilesInfo()
        .getFileNamesAndPointers().size() - 1;
    
    GrouperWorkflowInstanceFileInfo fileInfo = instance.getGrouperWorkflowInstanceFilesInfo()
    .getFileNamesAndPointers().get(lastIndex);
    
    String html = fileInfo.getFilePointer().startsWith("https://") ? 
        getCurrentHtmlContentFromS3(instance): getCurrentHtmlFormFromFileSystem(instance);
    
    Document document = Jsoup.parse(html);
    GrouperWorkflowConfigParams configParams = instance.getGrouperWorkflowConfig().getConfigParams();
    for (GrouperWorkflowConfigParam param : configParams.getParams()) {

      String elementName = param.getParamName();
      Element element = document.selectFirst("[name="+elementName+"]");
      List<String> editableInStates = param.getEditableInStates();
      
      if (editableInStates.contains(state)) {
        element.removeAttr("disabled");
      } else {
        element.attr("disabled", "disabled");
      }
     
    }
        
    return document.html();
  }
  
  
  /**
   * @param instance
   * @return html file content
   */
  private static String getCurrentHtmlFormFromFileSystem(GrouperWorkflowInstance instance) {
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    
    Cipher cipher;
    try {
      cipher = Cipher.getInstance("AES");
      cipher.init(Cipher.DECRYPT_MODE, encryptionKeySecret);
      
      int lastIndex = instance.getGrouperWorkflowInstanceFilesInfo()
          .getFileNamesAndPointers().size() - 1;
      
      GrouperWorkflowInstanceFileInfo fileInfo = instance.getGrouperWorkflowInstanceFilesInfo()
      .getFileNamesAndPointers().get(lastIndex);
      
      FileInputStream inputStream = new FileInputStream(fileInfo.getFilePointer());
      File file = new File(fileInfo.getFilePointer());
      byte[] inputBytes = new byte[(int) file.length()];
      inputStream.read(inputBytes);
       
      byte[] outputBytes = cipher.doFinal(inputBytes);
      inputStream.close();
      return new String(outputBytes);
      
    } catch (Exception e) {
      throw new RuntimeException("Error in getting report content from file system", e);
    }
    
  }
  
  /**
   * @param instance
   * @return html file content
   */
  private static String getCurrentHtmlContentFromS3(GrouperWorkflowInstance instance) {
    
    String accessKey = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.access.key");
    String secretKey = GrouperConfig.retrieveConfig().propertyValueString("workflow.s3.secret.key");
    
    String encryptionKey = Morph.decrypt(instance.getWorkflowInstanceEncryptionKey());
    SecretKey encryptionKeySecret = new SecretKeySpec(encryptionKey.getBytes(), "AES");
    EncryptionMaterials encryptionMaterials = new EncryptionMaterials(encryptionKeySecret);
    
    try {
      
      AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
      AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
      
      int lastIndex = instance.getGrouperWorkflowInstanceFilesInfo()
          .getFileNamesAndPointers().size() - 1;
      
      GrouperWorkflowInstanceFileInfo fileInfo = instance.getGrouperWorkflowInstanceFilesInfo()
      .getFileNamesAndPointers().get(lastIndex);
      
      AmazonS3URI s3Uri = new AmazonS3URI(fileInfo.getFilePointer());
      AmazonS3 s3Client = AmazonS3EncryptionClientBuilder.standard()
              .withRegion(s3Uri.getRegion())
              .withCredentials(credentialsProvider)
              .withEncryptionMaterials(new StaticEncryptionMaterialsProvider(encryptionMaterials))
              .build();
      
      // Download and decrypt the object.
      S3Object downloadedObject = s3Client.getObject(s3Uri.getBucket(), s3Uri.getKey());     
      byte[] decrypted = com.amazonaws.util.IOUtils.toByteArray(downloadedObject.getObjectContent());
      return new String(decrypted);
    } catch (Exception e) {
      throw new RuntimeException("Error getting report content from S3", e);
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
       
      byte[] outputBytes = cipher.doFinal(fileContents.getBytes());
      // byte[] outputBytes = fileContents.getBytes();
      
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
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_UUID);
    result.setWorkflowInstanceUuid(assignValue != null ? assignValue.getValueString(): null);
    
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
    
    Subject subjetWhoInitiated = subjectWhoInitiatedWorkflow(result);
    result.setInitiatorSubject(subjetWhoInitiated);
    
    return result;
    
  }
  
  private static boolean isParamAllowedToBeChagned(GrouperWorkflowInstanceParamValue paramValueObjectBeforeUpdate,
      String newValue, GrouperWorkflowConfigParam configParam, String currentState) {
    
    if (configParam.getEditableInStates().contains(currentState)) {
      return true;
    }
    
    if (paramValueObjectBeforeUpdate != null) {
      String oldValue = paramValueObjectBeforeUpdate.getParamValue();
      
      if (StringUtils.equals(newValue, oldValue)) {
        return true;
      }
    } else {
      if (StringUtils.isBlank(newValue)) {
        return true;
      }
    }
    
    return false;
  }
  
  private static String populateHtmlForm(GrouperWorkflowInstance instance, 
      GrouperWorkflowConfig workflowConfig,
      Subject subject, Date date, String formToPopulate,
      Map<GrouperWorkflowConfigParam, String> paramNamesValues) {
    
    Document document = Jsoup.parse(formToPopulate);
    
    GrouperWorkflowConfigParams configParams = workflowConfig.getConfigParams();
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      
      String paramName = workflowConfigParam.getParamName();
      String newValue = paramNamesValues.get(workflowConfigParam);
      try {
        
        Method getParamMethod = instance.getClass().getMethod("getGrouperWorkflowInstanceParamValue"+String.valueOf(i));
        GrouperWorkflowInstanceParamValue paramValueObjectBeforeUpdate = (GrouperWorkflowInstanceParamValue) getParamMethod.invoke(instance);
        
        if (!isParamAllowedToBeChagned(paramValueObjectBeforeUpdate, newValue, 
            workflowConfigParam, instance.getWorkflowInstanceState())) {
          throw new RuntimeException("Operation not permitted");
        }
        
        // nothing to do when user did not update the value and workflow is already in progress
        if (paramValueObjectBeforeUpdate != null &&
            StringUtils.equals(paramValueObjectBeforeUpdate.getParamValue(), newValue)) {
          continue;
        }
        
        // nothing to update when user did not enter any value and this is the first time
        if (paramValueObjectBeforeUpdate == null && StringUtils.isBlank(newValue)) {
          continue;
        }
        
        GrouperWorkflowInstanceParamValue paramValueObjectAfterUpdate = new GrouperWorkflowInstanceParamValue();
        paramValueObjectAfterUpdate.setEditedByMemberId(subject.getId());
        paramValueObjectAfterUpdate.setEditedInState(instance.getWorkflowInstanceState());
        paramValueObjectAfterUpdate.setLastUpdatedMillis(date.getTime());
        paramValueObjectAfterUpdate.setParamValue(newValue);
        
        Method method = instance.getClass().getMethod("setGrouperWorkflowInstanceParamValue"+String.valueOf(i), GrouperWorkflowInstanceParamValue.class);
        method.invoke(instance, paramValueObjectAfterUpdate);
        
        Element element = document.selectFirst("[name="+paramName+"]");
        
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(newValue) && newValue.equals("on")) {
            element.attr("checked", "checked");
          }
        } else {
          element.val(newValue);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    return document.html();
  }
  
  private static String appendAuditLine(String htmlForm, Subject subject, 
      String state, String action, Date date) {
    
    StringBuilder htmlFormWithAudit = new StringBuilder(htmlForm);
    
    String auditLine = GrouperTextContainer.retrieveFromRequest().getText().get("workflowFormAuditLine");
    
    auditLine = auditLine.replace("$$subjectSource$$", subject.getSource().getName());
    auditLine = auditLine.replace("$$subjectId$$", subject.getId());
    auditLine = auditLine.replace("$$subjectName$$", subject.getName());
    auditLine = auditLine.replace("$$buttonText$$", action);
    auditLine = auditLine.replace("$$state$$", state);
    
    String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    
    auditLine = auditLine.replace("$$timestamp$$", timestamp);
    
    htmlFormWithAudit.append("<div>");
    htmlFormWithAudit.append(auditLine);
    htmlFormWithAudit.append("</div>");
    
    return htmlFormWithAudit.toString();
  }

}
