package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonBuiltInMessagingConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  #quartz cron-like schedule for grouper messaging daemon.
//  #leave blank to disable this, the default is every hour, 10 minutes after the hour 
//  #this daemon does cleanup on the builtin messaging table
//  # {valueType: "cron"}
//  changeLog.builtinMessagingDaemon.quartz.cron = 0 10 * * * ?
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
    return "^(changeLog\\.builtinMessagingDaemon)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.builtinMessagingDaemon.";
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return "MAINTENANCE__";
  }

  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "MAINTENANCE__builtinMessagingDaemon".equals(jobName);
  }
}
