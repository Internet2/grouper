package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonCleanLogsConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #quartz cron-like schedule for clean logs daemon.
  //  # {valueType: "string", required: true, defaultValue="0 0 6 * * ?"}
  //  changeLog.cleanLogs.quartz.cron = 0 0 6 * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.cleanLogs)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.cleanLogs.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "MAINTENANCE_";
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "MAINTENANCE_cleanLogs".equals(jobName);
  }
}
