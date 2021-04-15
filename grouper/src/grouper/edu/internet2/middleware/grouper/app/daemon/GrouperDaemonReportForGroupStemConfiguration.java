package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonReportForGroupStemConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return null;
  }

  //  #quartz cron-like schedule for enabled/disabled daemon.  Note, this has nothing to do with the changelog
  //  #leave blank to disable this, the default is 12:01am, 11:01am, 3:01pm every day: 0 1 0,11,15 * * ? 
  //  # {valueType: "string"}
  //  changeLog.enabledDisabled.quartz.cron = 0 1 0,11,15 * * ?
  
  @Override
  public String getConfigIdRegex() {
    return null;
  }

  @Override
  public String getConfigItemPrefix() {
    return null;
  }
    
  @Override
  public boolean isMultiple() {
    return true;
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "grouper_report_";
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return jobName.startsWith("grouper_report_");
  }
}
