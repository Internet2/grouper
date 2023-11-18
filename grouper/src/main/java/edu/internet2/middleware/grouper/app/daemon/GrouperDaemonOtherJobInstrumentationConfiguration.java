package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobInstrumentationConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # set this to enable the instrumentation
  //  # {valueType: "class", readOnly:true, mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase"}
  //  otherJob.tierInstrumentationDaemon.class = edu.internet2.middleware.grouper.instrumentation.TierInstrumentationDaemon
  //
  //  # cron string
  //  # {valueType: "string"}
  //  otherJob.tierInstrumentationDaemon.quartzCron = 0 0 2 * * ?
  //
  //  # collector url
  //  # {valueType: "string"}
  //  otherJob.tierInstrumentationDaemon.collectorUrl = http://collector.testbed.tier.internet2.edu:5001

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.tierInstrumentationDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.tierInstrumentationDaemon.";
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
    return "OTHER_JOB_tierInstrumentationDaemon".equals(jobName);
  }
  
  

}
