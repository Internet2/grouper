package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperWorkflowSettings {
  
  public final static ObjectMapper objectMapper = new ObjectMapper();
  
  public final static String DEFAULT_WORKFLOW_CONFIG_TYPE = "grouper";
  
  static {
    objectMapper.setSerializationInclusion(Include.NON_NULL);
  }
  
  /**
   * @return the stem name with no last colon
   */
  public static String workflowStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("workflow.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":workflow"), ":");
  }
  
  /**
   * 
   * @return group name where membership means a subject can edit/add workflow
   */
  public static String workflowEditorsGroup() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("workflow.editorsGroup",
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":workflowEditors"), ":");
  }
  
  /**
   * if workflow is enabled
   * @return if workflow is enabled
   */
  public static boolean workflowEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("workflow.enable", true);
  }
  
  /**
   * 
   * @return the list of configured config types
   */
  public static List<String> configTypes() {
    String configTypes = GrouperConfig.retrieveConfig().propertyValueString("workflow.configTypes");
    String[] configs = configTypes.split(",");
    List<String> configList = Arrays.asList(configs);
    if (!configList.contains(DEFAULT_WORKFLOW_CONFIG_TYPE)) {
      throw new RuntimeException("grouper must be in the list of workflow.configTypes");
    }
    return configList;
  }
  
}
