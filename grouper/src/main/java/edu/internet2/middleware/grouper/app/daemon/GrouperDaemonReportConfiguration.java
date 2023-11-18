package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonReportConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #quartz cron-like schedule for enabled/disabled daemon.  Note, this has nothing to do with the changelog
  //  #leave blank to disable this, the default is 12:01am, 11:01am, 3:01pm every day: 0 1 0,11,15 * * ? 
  //  # {valueType: "string"}
  //  changeLog.enabledDisabled.quartz.cron = 0 1 0,11,15 * * ?
  
  @Override
  public String getConfigIdRegex() {
    return "^(daily\\.report)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "daily.report.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "MAINTENANCE__";
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "MAINTENANCE__grouperReport".equals(jobName);
  }
}
