package edu.internet2.middleware.grouper.app.workflow;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperWorkflowSettings {
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String workflowStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("workflow.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":workflow"), ":");
  }
  
  /**
   * if workflow is enabled
   * @return if workflow is enabled
   */
  public static boolean workflowEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("workflow.enable", true);
  }

}
