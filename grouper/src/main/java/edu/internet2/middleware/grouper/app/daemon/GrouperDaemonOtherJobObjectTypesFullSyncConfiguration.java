package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobObjectTypesFullSyncConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # set this to enable the object types full sync
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
//  otherJob.objectTypesFullSyncDaemon.class = edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesDaemonLogic
//
//  # cron string
//  # {valueType: "cron"}
//  otherJob.objectTypesFullSyncDaemon.quartzCron = 0 0 2 17 39 ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.objectTypesFullSyncDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.objectTypesFullSyncDaemon.";
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
    return "OTHER_JOB_objectTypesFullSyncDaemon".equals(jobName);
  }
  
  
}
