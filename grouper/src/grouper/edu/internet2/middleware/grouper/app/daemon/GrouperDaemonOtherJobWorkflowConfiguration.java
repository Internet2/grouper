package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobWorkflowConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Workflow daemon that updates instances and send emails
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.grouperWorkflowDaemon.class = edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowDaemonJob
  //
  //  # Object Type Job cron
  //  # {valueType: "string"}
  //  otherJob.grouperWorkflowDaemon.quartzCron = 0 0/5 * ? * * *

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.grouperWorkflowDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.grouperWorkflowDaemon.";
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
    return "OTHER_JOB_grouperWorkflowDaemon".equals(jobName);
  }
  
  

}
