package edu.internet2.middleware.grouper.app.workflow;


import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_APPROVALS;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_DESCRIPTION;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ENABLED;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_FORM;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ID;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_NAME;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_PARAMS;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_SEND_EMAIL;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_TYPE;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;
import static edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperWorkflowConfigService {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowConfigService.class);
  
  /**
   * get workflow config for a given group and workflow id
   * @param group
   * @param workflowId
   * @return
   */
  public static GrouperWorkflowConfig getWorkflowConfig(final Group group, final String workflowId) {
    
    AttributeAssign attributeAssign = getAttributeAssign(group, workflowId);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperWorkflowConfig(attributeAssign);
  }
  
  
  /**
   * retrieve workflow config bean from owner object and attribute assign marker id
   * @param attributeAssignmentMarkerId
   * @return
   */
  public static GrouperWorkflowConfig getWorkflowConfig(String attributeAssignmentMarkerId) {
    
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignmentMarkerId, true);
    
    return buildGrouperWorkflowConfig(attributeAssign);
    
  }
  
  /**
   * check if workflow config exists for a given workflow id
   * @param workflowId
   * @return
   */
  public static boolean workflowIdExists(final String workflowId) {
        
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.retrieveCount(true);
    queryOptions.retrieveResults(false);
    
    new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
    .assignNameOfAttributeDefName(GrouperWorkflowSettings.workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ID)
    .addAttributeValuesOnAssignment(workflowId)
    .assignQueryOptions(queryOptions)
    .findGroups();
    
    return queryOptions.getCount() > 0;
  }
     
  /**
   * get all workflow configs configured for a given group 
   * @param group
   * @return
   */
  public static List<GrouperWorkflowConfig> getWorkflowConfigs(final Group group) {
    
    List<GrouperWorkflowConfig> result = new ArrayList<GrouperWorkflowConfig>();
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase());
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      result.add(buildGrouperWorkflowConfig(attributeAssign));
    }
    
    return result;
  }
  
  /**
   * can subject configure workflow
   * @param group
   * @param subject
   * @return
   */
  public static boolean canSubjectConfigureWorkflow(Group group, Subject subject) {
    
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    if (group != null && group.getAdmins().contains(subject)) {
      return true;
    }
    
    final String workflowEditorsGroup = GrouperWorkflowSettings.workflowEditorsGroup();
    
    if (StringUtils.isNotBlank(workflowEditorsGroup)) {
      
      Group editorGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), workflowEditorsGroup, false);
      if (editorGroup == null) {
        editorGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession(), workflowEditorsGroup, false);
      }
      if (editorGroup == null) {
        LOG.error("workflow editor group "+workflowEditorsGroup+" not found.");
        return false;
      }
      return editorGroup.hasMember(subject);
    }
    
    return false;
  }
  
  /**
   * can subject view workflow
   * @param group
   * @param subject
   * @return
   */
  public static boolean canSubjectViewWorkflow(Group group, Subject subject) {
    
    if (canSubjectConfigureWorkflow(group, subject)) {
      return true;
    }
    
    if (group != null) {
      List<GrouperWorkflowConfig> workflowConfigs = GrouperWorkflowConfigService.getWorkflowConfigs(group);
      
      for (GrouperWorkflowConfig workflowConfig: workflowConfigs) {
        if (workflowConfig.isSubjectInViewersGroup(subject)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  /**
   * save or update workflow config
   * @param grouperWorkflowConfig
   * @param group
   */
  public static void saveOrUpdateGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig, Group group) {
    
    GrouperWorkflowApprovalStates existingApprovalStates = null;
    AttributeAssign attributeAssign = getAttributeAssign(group, grouperWorkflowConfig.getWorkflowConfigId());
    if (attributeAssign == null) {
      attributeAssign = group.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    } else {
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate()
          .retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_APPROVALS);
      existingApprovalStates = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(attributeAssignValue.getValueString());
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_APPROVALS, true);
    try {
      String approvals = GrouperWorkflowSettings.objectMapper.writeValueAsString(grouperWorkflowConfig.getWorkflowApprovalStates());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), approvals);
    } catch (Exception e) {
      throw new RuntimeException("could not convert workflow approval states to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigDescription());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigEnabled());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_FORM, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigForm());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigName());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigId());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_PARAMS, true);
    try {      
      String configParams = GrouperWorkflowSettings.objectMapper.writeValueAsString(grouperWorkflowConfig.getConfigParams());
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), configParams);
    } catch (Exception e) {
      throw new RuntimeException("could not convert config params to json string");
    }
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperWorkflowConfig.isWorkflowConfigSendEmail()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_TYPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigType());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigViewersGroupId());
    
    // when we are updating a workflow config, based on the allowedGroupId value, 
    // either remove the allowedGroup VIEW privilege from the group on which we are adding the workflow config
    // or remove the every entity VIEW privilege from the group on which we are adding the workflow config
    if (existingApprovalStates != null) {
      GrouperWorkflowApprovalState previousInitiateState = existingApprovalStates.getStateByName(INITIATE_STATE);
      if (StringUtils.isNotBlank(previousInitiateState.getAllowedGroupId())) {
        Subject previousAllowedGroup = SubjectFinder.findById(previousInitiateState.getAllowedGroupId(), false);
        if (previousAllowedGroup != null) {
          group.revokePriv(previousAllowedGroup, AccessPrivilege.VIEW, false);
        }
      } else {
        Subject everyEntitySubject = SubjectFinder.findAllSubject();
        group.revokePriv(everyEntitySubject, AccessPrivilege.VIEW, false);
      }
    }
    
    // give view privilege to allowedGroupId or put every entity with VIEW privilege to the group
    // on which we are saving the workflow config
    // this is necessary so that subjects can view the group and can initiate the workflow
    // without view privilege, group won't show up in the tree on the left
    GrouperWorkflowApprovalState initiateState = grouperWorkflowConfig.getWorkflowApprovalStates().getStateByName(INITIATE_STATE);
    String alloweGroupId = initiateState.getAllowedGroupId();
    if (StringUtils.isNotBlank(alloweGroupId)) {
      Subject allowedGroup = SubjectFinder.findById(alloweGroupId, true);
      group.addOrEditMember(allowedGroup, false, false, false, false, false, true, 
          false, false, false, false, null, null, false);
    } else {
      Subject everyEntitySubject = SubjectFinder.findAllSubject();
      group.addOrEditMember(everyEntitySubject, false, false, false, false, false, true, 
          false, false, false, false, null, null, false);
    }
    
    attributeAssign.saveOrUpdate();
    
  }
  
  private static GrouperWorkflowConfig buildGrouperWorkflowConfig(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperWorkflowConfig result = new GrouperWorkflowConfig();
    
    result.setOwnerGroup(attributeAssign.getOwnerGroup());
    
    result.setAttributeAssignmentMarkerId(attributeAssign.getId());
    
    AttributeAssignValue attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_APPROVALS);
    GrouperWorkflowApprovalStates workflowApprovalStates = GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(attributeAssignValue.getValueString());
    result.setWorkflowApprovalStates(workflowApprovalStates);
    
    result.setWorkflowConfigApprovalsString(attributeAssignValue.getValueString());
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_DESCRIPTION);
    result.setWorkflowConfigDescription(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ENABLED);
    result.setWorkflowConfigEnabled(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_FORM);
    result.setWorkflowConfigForm(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ID);
    result.setWorkflowConfigId(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_NAME);
    result.setWorkflowConfigName(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_PARAMS);
    GrouperWorkflowConfigParams configParams = GrouperWorkflowConfigParams.buildParamsFromJsonString(attributeAssignValue.getValueString());
    result.setConfigParams(configParams);
    
    result.setWorkflowConfigParamsString(attributeAssignValue.getValueString());
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_SEND_EMAIL);
    String workflowConfigSendEmailStr = attributeAssignValue != null ? attributeAssignValue.getValueString(): null;
    boolean workflowConfigSendEmail = BooleanUtils.toBoolean(workflowConfigSendEmailStr);
    result.setWorkflowConfigSendEmail(workflowConfigSendEmail);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_TYPE);
    result.setWorkflowConfigType(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID);
    result.setWorkflowConfigViewersGroupId(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    return result;
  }
  
  private static AttributeAssign getAttributeAssign(Group group, String workflowId) {
    
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase());
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate()
          .retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ID);
      if (attributeAssignValue == null || StringUtils.isBlank(attributeAssignValue.getValueString())) {
        return null;
      }
      
      String workflowIdFromDb = attributeAssignValue.getValueString();
      if (workflowId.equals(workflowIdFromDb)) {
       return attributeAssign;
      }
    }
    
    return null;
    
  } 

}
