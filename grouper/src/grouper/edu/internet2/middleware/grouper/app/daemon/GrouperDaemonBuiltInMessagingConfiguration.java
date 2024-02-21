package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonBuiltInMessagingConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # this daemon does cleanup on the builtin messaging table
//  # {valueType: "class", readOnly: true, mustImplementInterface: "org.quartz.Job"}
//  otherJob.builtinMessagingDaemon.class = edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingDaemon
//
//  # quartz cron-like schedule for grouper messaging daemon.
//  # {valueType: "cron"}
//  otherJob.builtinMessagingDaemon.quartzCron = 0 10 * * * ?
//
//  # after three days of not consuming messages, delete them, if -1, dont run this daemon
//  # {valueType: "integer", required: true}
//  grouper.builtin.messaging.deleteAllMessagesMoreThanHoursOld = 72
//
//  # after three hours of having processed messages, delete them.  Note, if this is -1 just delete when marking processed
//  # {valueType: "integer", required: true}
//  grouper.builtin.messaging.deleteProcessedMessagesMoreThanMinutesOld = 180

  public GrouperDaemonBuiltInMessagingConfiguration() {
    this.extraConfigKeys.add("grouper.builtin.messaging.deleteAllMessagesMoreThanHoursOld");
    this.extraConfigKeys.add("grouper.builtin.messaging.deleteProcessedMessagesMoreThanMinutesOld");
  }
      
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.builtinMessagingDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "otherJob.builtinMessagingDaemon.";
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
    return "OTHER_JOB_builtinMessagingDaemon".equals(jobName);
  }
}
