package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonRulesConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # rules daemon
//  # {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
//  otherJob.rules.class = edu.internet2.middleware.grouper.rules.GrouperRulesDaemon
//
//  # when the rules validations and daemons run.
//  # {valueType: "cron"}
//  otherJob.rules.quartzCron = 0 0 7 * * ?
      
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.rules)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.rules.";
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
    return "OTHER_JOB_rules".equals(jobName);
  }
}