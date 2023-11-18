package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobUpgradeTasksConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Run upgrade tasks
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.upgradeTasks.class = edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasksJob
  //
  //  # Run upgrade tasks cron
  //  # {valueType: "string"}
  //  otherJob.upgradeTasks.quartzCron = 5 25 * * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.upgradeTasks)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.upgradeTasks.";
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
    return "OTHER_JOB_upgradeTasks".equals(jobName);
  }
  
  

}
