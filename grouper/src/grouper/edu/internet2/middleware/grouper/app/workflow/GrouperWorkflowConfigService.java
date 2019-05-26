package edu.internet2.middleware.grouper.app.workflow;


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
import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowSettings.workflowStemName;
import static edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils.toStringTrueFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperWorkflowConfigService {
  
  public static GrouperWorkflowConfig getWorkflowConfig(final Group group, final String workflowId) {
    
    AttributeAssign attributeAssign = getAttributeAssign(group, workflowId);
    if (attributeAssign == null) {
      return null;
    }
    
    return buildGrouperWorkflowConfig(attributeAssign);
  }
     
  public static List<GrouperWorkflowConfig> getWorkflowConfigs(final Group group) {
    
    List<GrouperWorkflowConfig> result = new ArrayList<GrouperWorkflowConfig>();
    Set<AttributeAssign> attributeAssigns = group.getAttributeDelegate().retrieveAssignments(GrouperWorkflowConfigAttributeNames.retrieveAttributeDefNameBase());
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      result.add(buildGrouperWorkflowConfig(attributeAssign));
    }
    
    return result;
  }
  
  public static void saveOrUpdateGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig, Group group) {
    
    AttributeAssign attributeAssign = getAttributeAssign(group, grouperWorkflowConfig.getWorkflowConfigId());
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_APPROVALS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigApprovals());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_DESCRIPTION, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigDescription());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ENABLED, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperWorkflowConfig.isWorkflowConfigEnabled()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_FORM, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigForm());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigName());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_PARAMS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigParams());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_SEND_EMAIL, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), toStringTrueFalse(grouperWorkflowConfig.isWorkflowConfigSendEmail()));
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_TYPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigType());
    
    attributeDefName = AttributeDefNameFinder.findByName(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), grouperWorkflowConfig.getWorkflowConfigViewersGroupId());
    
    attributeAssign.saveOrUpdate();
    
  }
  
  private static GrouperWorkflowConfig buildGrouperWorkflowConfig(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperWorkflowConfig result = new GrouperWorkflowConfig();
    
    AttributeAssignValue attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_APPROVALS);
    result.setWorkflowConfigApprovals(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_DESCRIPTION);
    result.setWorkflowConfigDescription(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ENABLED);
    String workflowConfigEnabledStr = attributeAssignValue != null ? attributeAssignValue.getValueString(): null;
    boolean workflowConfigEnabled = BooleanUtils.toBoolean(workflowConfigEnabledStr);
    result.setWorkflowConfigEnabled(workflowConfigEnabled);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_FORM);
    result.setWorkflowConfigForm(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ID);
    result.setWorkflowConfigId(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_NAME);
    result.setWorkflowConfigName(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
    attributeAssignValue = attributeValueDelegate.retrieveAttributeAssignValue(workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_PARAMS);
    result.setWorkflowConfigParams(attributeAssignValue != null ? attributeAssignValue.getValueString(): null);
    
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
