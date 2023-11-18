package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobTimeConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # Keep the current time in a database independent way
  //  # {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
  //  otherJob.timeDaemon.class = edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperTimeDaemon
  //
  //  # Run the time daemon every minute
  //  # {valueType: "cron"}
  //  otherJob.timeDaemon.quartzCron = 45 * * * * ?

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.timeDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.timeDaemon.";
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
    return "OTHER_JOB_timeDaemon".equals(jobName);
  }
  
  

}
