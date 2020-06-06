package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobDeprovisioningConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # Deprovisioning Job class
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
//  otherJob.deprovisioningDaemon.class = edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningJob
//
//  # Deprovisioning Job cron
//  # {valueType: "string"}
//  otherJob.deprovisioningDaemon.quartzCron = 0 0 2 * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.deprovisioningDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.deprovisioningDaemon.";
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
    return "OTHER_JOB_deprovisioningDaemon".equals(jobName);
  }
}
