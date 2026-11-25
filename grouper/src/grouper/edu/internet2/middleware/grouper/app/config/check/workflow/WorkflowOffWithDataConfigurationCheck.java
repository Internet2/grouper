package edu.internet2.middleware.grouper.app.config.check.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConfigAttributeNames;
import edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowInstanceAttributeNames;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * GRP-7380 (complements GRP-7585): workflow is off by default (grouper.properties workflow.enable).
 * When it is off but the database still has workflow data (attribute assigns of workflowConfigMarker
 * or workflowInstanceMarker), the grouperWorkflowDaemon and grouperWorkflowReminderDaemon daemons
 * do nothing and end with a WARNING on every run.  This check flags each of those daemons that is
 * still scheduled (class and quartzCron set in grouper-loader.properties) in that case (WARNING).
 */
public class WorkflowOffWithDataConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * workflow on / off switch (grouper.properties), default false
   */
  public static final String WORKFLOW_ENABLE_PROPERTY_NAME = "workflow.enable";

  /**
   * names of the workflow daemons (otherJob.&lt;name&gt;.* in grouper-loader.properties)
   */
  public static final String[] WORKFLOW_DAEMON_NAMES = new String[] {"grouperWorkflowDaemon", "grouperWorkflowReminderDaemon"};

  @Override
  public String getName() {
    return "workflowOffWithData";
  }

  /**
   * count the workflow attribute assigns (the query in the v7 upgrade instructions, as a single
   * count).  Protected so a unit test can stand in for the database.
   * @return the number of workflow config and instance attribute assigns
   */
  protected long retrieveWorkflowAttributeAssignCount() {
    // int, like the other count queries that use GcDbAccess (works across the supported databases)
    Integer count = new GcDbAccess().sql("select count(*) from grouper_attribute_assign gaa "
        + "join grouper_attribute_def_name gadn on gaa.attribute_def_name_id = gadn.id "
        + "where gadn.extension in (?, ?)")
        .addBindVar(GrouperWorkflowConfigAttributeNames.GROUPER_WORKFLOW_CONFIG_ATTRIBUTE_NAME)
        .addBindVar(GrouperWorkflowInstanceAttributeNames.GROUPER_WORKFLOW_INSTANCE_ATTRIBUTE_NAME)
        .select(Integer.class);
    return count == null ? 0 : count.longValue();
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // same read and default as GrouperWorkflowSettings.workflowEnabled
    if (GrouperConfig.retrieveConfig().propertyValueBoolean(WORKFLOW_ENABLE_PROPERTY_NAME, false)) {
      return results;
    }

    // which workflow daemons are scheduled (GrouperLoader skips an otherJob with a blank class or cron);
    // check this before the database so a site with the daemons off never runs the query
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    List<String> scheduledDaemonNames = new ArrayList<String>();
    for (String daemonName : WORKFLOW_DAEMON_NAMES) {
      String className = grouperLoaderConfig.propertyValueString("otherJob." + daemonName + ".class");
      String quartzCron = grouperLoaderConfig.propertyValueString("otherJob." + daemonName + ".quartzCron");
      if (StringUtils.isNotBlank(className) && StringUtils.isNotBlank(quartzCron)) {
        scheduledDaemonNames.add(daemonName);
      }
    }
    if (scheduledDaemonNames.size() == 0) {
      return results;
    }

    long workflowAttributeAssignCount = this.retrieveWorkflowAttributeAssignCount();
    if (workflowAttributeAssignCount <= 0) {
      return results;
    }

    for (String daemonName : scheduledDaemonNames) {
      String cronPropertyName = "otherJob." + daemonName + ".quartzCron";
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, cronPropertyName,
          grouperLoaderConfig.propertyValueString(cronPropertyName),
          "workflow.enable is false but the database has " + workflowAttributeAssignCount + " workflow "
              + "attribute assignment(s), so the " + daemonName + " daemon does nothing and ends with a "
              + "WARNING on every run.",
          "If you do not use workflow, disable the " + daemonName + " daemon (blank " + cronPropertyName
              + ") or delete the old workflow data.  If you do use workflow, set workflow.enable=true in "
              + "grouper.properties (or the configuration database) for every container."));
    }

    return results;
  }

}
