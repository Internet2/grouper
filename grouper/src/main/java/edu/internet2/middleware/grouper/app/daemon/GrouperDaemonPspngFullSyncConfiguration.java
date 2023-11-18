package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonPspngFullSyncConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Schedule full synchronizations. Defaults to 5 am : 0 0 5 * * ?.
  //  # {valueType: "cron"}
  //  # changeLog.psp.fullSync.quartzCron = 0 0 5 * * ?
  
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.psp\\.fullSync)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.psp.fullSync.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "PSP_FULL_SYNC";
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "PSP_FULL_SYNC".equals(jobName);
  }
}
