package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobSchedulerCheckConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Find and fix scheduler issues class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.schedulerCheckDaemon.class = edu.internet2.middleware.grouper.app.loader.GrouperDaemonSchedulerCheck
  //
  //  # Find and fix scheduler issues cron
  //  # {valueType: "string"}
  //  otherJob.schedulerCheckDaemon.quartzCron = 25 0/30 * * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.schedulerCheckDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.schedulerCheckDaemon.";
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
    return "OTHER_JOB_schedulerCheckDaemon".equals(jobName);
  }
  
  

}
