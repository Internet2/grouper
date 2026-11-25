package edu.internet2.middleware.grouper.app.workflow;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperWorkflowSettings {
  
  public final static String DEFAULT_WORKFLOW_CONFIG_TYPE = "grouper";
  
  /**
   * @return the stem name with no last colon
   */
  public static String workflowStemName() {
    return GrouperUtil.stripSuffix(GrouperConfig.retrieveConfig().propertyValueString("workflow.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":workflow"), ":");
  }
  
  /**
   * 
   * @return group name where membership means a subject can edit/add workflow
   */
  public static String workflowEditorsGroup() {
    return GrouperUtil.stripSuffix(GrouperConfig.retrieveConfig().propertyValueString("workflow.editorsGroup",
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":workflowEditors"), ":");
  }
  
  /**
   * if workflow is enabled.  Off unless workflow.enable is true in grouper.properties
   * (or the configuration database): sites that do not use workflow do not expose its screens
   * or config.  This default must match the defaultValue in grouper.base.properties.
   * @return if workflow is enabled
   */
  public static boolean workflowEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("workflow.enable", false);
  }

  /**
   * for the workflow daemons: if workflow is off, say so in the daemon log and tell the caller to
   * stop.  Workflow is off by default, so a site that does not use workflow but has old workflow
   * data would otherwise get an error every run (reading a workflow config is refused while
   * workflow is off).  Nothing is done either way: no workflow data is read or changed.
   * Call inside a root session.
   * @param hib3GrouperLoaderLog the daemon log row to put the message (and WARNING status) on, may be null
   * @return true if workflow is off and the daemon should stop
   */
  public static boolean daemonShouldStopSinceWorkflowDisabled(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

    if (workflowEnabled()) {
      return false;
    }

    // only count, so the message can say whether there is leftover data; this does not read any
    // workflow config (that is what fails while workflow is off)
    int groupsWithInstances = GrouperUtil.length(GrouperWorkflowInstanceService.findGroupsWithWorkflowInstance());

    if (hib3GrouperLoaderLog != null) {
      if (groupsWithInstances == 0) {
        hib3GrouperLoaderLog.appendJobMessage("Workflow is disabled (workflow.enable is not true), nothing done");
      } else {
        // a warning, not an error: nothing is broken, but a site with workflow data may have meant to keep workflow on
        hib3GrouperLoaderLog.setStatus(GrouperLoaderStatus.WARNING.name());
        hib3GrouperLoaderLog.appendJobMessage("Workflow is disabled (workflow.enable is not true), but "
            + groupsWithInstances + " group(s) have workflow instances, nothing done.  "
            + "If workflow is used, set workflow.enable = true.  If not, disable this daemon or delete the old workflow data");
      }
    }
    return true;
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
