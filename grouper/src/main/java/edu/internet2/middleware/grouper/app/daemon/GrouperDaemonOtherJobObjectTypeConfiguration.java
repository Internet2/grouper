package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobObjectTypeConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Object Type Job class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.grouperObjectTypeDaemon.class = edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesJob
  //
  //  # Object Type Job cron
  //  # {valueType: "string"}
  //  otherJob.grouperObjectTypeDaemon.quartzCron = 0 0 3 * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.grouperObjectTypeDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.grouperObjectTypeDaemon.";
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
    return "OTHER_JOB_grouperObjectTypeDaemon".equals(jobName);
  }
  
  

}
