package edu.internet2.middleware.grouper.app.workflow;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

public class GrouperWorkflowConfigAttributeNames {
  
  /**
   * main attribute definition assigned to groups
   */
  public static final String GROUPER_WORKFLOW_CONFIG_DEF = "workflowConfigDef";
  
  /**
   * main attribute name assigned to workflowConfigDef
   */
  public static final String GROUPER_WORKFLOW_CONFIG_ATTRIBUTE_NAME = "workflowConfigMarker";
  
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups
   */
  public static final String GROUPER_WORKFLOW_CONFIG_VALUE_DEF = "workflowConfigValueDef";
  
  /**
   * workflow implementation type. default is grouper
   */
  public static final String GROUPER_WORKFLOW_CONFIG_TYPE = "workflowConfigType";

  /**
   * JSON config of the workflow approvals
   */
  public static final String GROUPER_WORKFLOW_CONFIG_APPROVALS = "workflowConfigApprovals";
  
  /**
   * workflow config name. No two workflows in the same owner should have the same name
   */
  public static final String GROUPER_WORKFLOW_CONFIG_NAME = "workflowConfigName";
  
  /**
   * Camel-case alphanumeric id of workflow.  
   * No two workflows in all of Grouper can have the same ID
   */
  public static final String GROUPER_WORKFLOW_CONFIG_ID = "workflowConfigId";
  
  /**
   * workflow config description
   */
  public static final String GROUPER_WORKFLOW_CONFIG_DESCRIPTION = "workflowConfigDescription";
  
  /**
   * workflow config params
   */
  public static final String GROUPER_WORKFLOW_CONFIG_PARAMS = "workflowConfigParams";
  
  /**
   * workflow config form
   */
  public static final String GROUPER_WORKFLOW_CONFIG_FORM = "workflowConfigForm";
  
  /**
   * GroupId of people who can view this workflow and instances of this workflow. 
   * Grouper admins can view any workflow (blank means admin only).  Anyone in an approver group can view the workflow.
   */
  public static final String GROUPER_WORKFLOW_CONFIG_VIEWERS_GROUP_ID = "workflowConfigViewersGroupId";
  
  /**
   * true/false if email should be sent
   */
  public static final String GROUPER_WORKFLOW_CONFIG_SEND_EMAIL = "workflowConfigSendEmail";
  
  /**
   * Could by "true", "false", or "noNewSubmissions", i.e. let current forms go through
   */
  public static final String GROUPER_WORKFLOW_CONFIG_ENABLED = "workflowConfigEnabled";
  
  /**
   * attribute value def assigned to group
   * @return the attribute def name
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefFinder.findByName(GrouperWorkflowSettings.workflowStemName() + ":" + GROUPER_WORKFLOW_CONFIG_DEF, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant workflowConfigDef attribute def be found?");
    }
    
    return attributeDef;
  }

  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperWorkflowSettings.workflowStemName()+":"+GROUPER_WORKFLOW_CONFIG_ATTRIBUTE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant workflowConfigMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
}
