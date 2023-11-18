package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobAttestationConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Atttestation Job class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  otherJob.attestationDaemon.class = edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob
  //
  //  # Atttestation Job cron
  //  # {valueType: "string"}
  //  otherJob.attestationDaemon.quartzCron = 0 0 1 * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.attestationDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.attestationDaemon.";
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
    return "OTHER_JOB_attestationDaemon".equals(jobName);
  }
}
