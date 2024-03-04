package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobEnabledDisabledConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # enabled/disabled daemon
//  # {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
//  otherJob.enabledDisabled.class = edu.internet2.middleware.grouper.app.loader.GrouperDaemonEnabledDisabledCheck
//
//  # quartz cron-like schedule for enabled/disabled daemon.
//  # {valueType: "cron", required: true}
//  otherJob.enabledDisabled.quartzCron = 5 * * * * ?
//
//  # seconds between re-querying upcoming updates and caching.  You probably do not want this to be greater than the default (3600 = 1 hour),
//  # but you might want to adjust it lower if you expect someone to configure an expire time less than that amount of time in the future.
//  # e.g. if you expect someone to configure an expire time 45 minutes in the future, you should set this to 40 minutes at max.
//  # {valueType: "integer", required: true}
//  otherJob.enabledDisabled.queryIntervalInSeconds = 3600

  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.enabledDisabled)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.enabledDisabled.";
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
    return "OTHER_JOB_enabledDisabled".equals(jobName);
  }
  
  

}
