package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobProvisioningConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Provisioning Job class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.grouperProvisioningDaemon.class = edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningJob
  //
  //  # Provisioning Job cron
  //  # {valueType: "string"}
  //  otherJob.grouperProvisioningDaemon.quartzCron = 0 0 4 * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.grouperProvisioningDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.grouperProvisioningDaemon.";
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
    return "OTHER_JOB_grouperProvisioningDaemon".equals(jobName);
  }
  
  

}
