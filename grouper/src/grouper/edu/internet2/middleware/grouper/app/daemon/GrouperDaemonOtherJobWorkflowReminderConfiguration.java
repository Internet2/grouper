package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobWorkflowReminderConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Workflow reminder email daemon
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.grouperWorkflowReminderDaemon.class = edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowReminderEmailJob
  //
  //  # Object Type Job cron
  //  # {valueType: "string"}
  //  otherJob.grouperWorkflowReminderDaemon.quartzCron = 0 0 4 * * ? 

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.grouperWorkflowReminderDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.grouperWorkflowReminderDaemon.";
  }

  @Override
  public boolean isMultiple() {
    return false;
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_OTHER_JOB_PREFIX;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "OTHER_JOB_grouperWorkflowReminderDaemon".equals(jobName);
  }
  
  

}
