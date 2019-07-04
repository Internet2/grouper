package edu.internet2.middleware.grouper.app.workflow;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

public class GrouperWorkflowInstanceAttributeNames {
  
  /**
   * main attribute definition assigned to groups
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_DEF = "workflowInstanceDef";
  
  /**
   * main attribute name assigned to workflowInstanceDef
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME = "workflowInstanceMarker";
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_VALUE_DEF = "workflowInstanceValueDef";
  
  /**
   * Any of the states, plus "exception" if there is a problem, workflows must have "initiate", and "complete", plus "rejected" if someone rejects it.
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_STATE = "workflowInstanceState";

  /**
   * number of millis since 1970 when this instance was last updated
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_LAST_UPDATED_MILLIS_SINCE_1970 = "workflowInstanceLastUpdatedMillisSince1970";

  /**
   * Attribute assign ID of the marker attribute of the config (same owner as this attribute, but there could be many workflows configured on one owner)
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID = "workflowInstanceConfigMarkerAssignmentId";

  /**
   * millis since 1970 that this workflow was submitted
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_INITIATED_MILLIS_SINCE_1970 = "workflowInstanceInitiatedMillisSince1970";

  /**
   * uuid assigned to this workflow instance
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_UUID = "workflowInstanceUuid";

  /**
   * workflow instance file info
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_FILE_INFO = "workflowInstanceFileInfo";

  /**
   * randomly generated 16 char alphanumeric encryption key (never allow display or edit of this)
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_ENCRYPTION_KEY = "workflowInstanceEncryptionKey";

  /**
   * yyyy/mm/dd date that this was last emailed so multiple emails dont go out on same day
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_DATE = "workflowInstanceLastEmailedDate";

  /**
   * the state of the workflow instance when it was last emailed
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_LAST_EMAILED_STATE = "workflowInstanceLastEmailedState";

  /**
   * has brief info about who did what when on this instance
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_LOG = "workflowInstanceLog";

  /**
   * error message including stack of why this instance is in "exception" state
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_ERROR = "workflowInstanceError";

  /**
   * param value 0
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_0 = "workflowInstanceParamValue0";
  
  /**
   * param value 1
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_1 = "workflowInstanceParamValue1";
  
  /**
   * param value 2
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_2 = "workflowInstanceParamValue2";
  
  /**
   * param value 3
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_3 = "workflowInstanceParamValue3";
  
  /**
   * param value 4
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_4 = "workflowInstanceParamValue4";
  
  /**
   * param value 5
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_5 = "workflowInstanceParamValue5";
  
  /**
   * param value 6
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_6 = "workflowInstanceParamValue6";
  
  /**
   * param value 7
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_7 = "workflowInstanceParamValue7";
  
  /**
   * param value 8
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_8 = "workflowInstanceParamValue8";
  
  /**
   * param value 9
   */
  public static final String GROUPER_WORKFLOW_INSTANCE_PARAM_VALUE_9 = "workflowInstanceParamValue9";
  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperWorkflowSettings.workflowStemName()+":"+GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant workflowInstanceMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
  
  
    /**
     * attribute value def assigned to stem or group
     * @return the attribute def name
     */
    public static AttributeDef retrieveAttributeDefBaseDef() {
      
      AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(GrouperWorkflowSettings.workflowStemName()+":"+GROUPER_WORKFLOW_INSTANCE_DEF, false, new QueryOptions().secondLevelCache(false));
          
        }
        
      });
    
      if (attributeDef == null) {
        throw new RuntimeException("Why cant workflowInstanceDef attribute def be found?");
      }
      
      return attributeDef;
    }
    

}
