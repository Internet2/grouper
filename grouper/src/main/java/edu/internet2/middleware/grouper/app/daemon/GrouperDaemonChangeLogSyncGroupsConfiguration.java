package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogSyncGroupsConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # consumer for syncing groups to other groupers
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase"}
//  changeLog.consumer.syncGroups.class = edu.internet2.middleware.grouper.client.GroupSyncConsumer
//
//  # consumer for syncing groups to other groupers
//  # {valueType: "string"}
//  changeLog.consumer.syncGroups.quartzCron =

      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer\\.syncGroups)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.consumer.syncGroups.";
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }
  
//  @Override
//  public String getPropertySuffixThatIdentifiesThisConfig() {
//    return "class";
//  }
//
//  @Override
//  public String getPropertyValueThatIdentifiesThisConfig() {
//    return RuleConsumer.class.getName();
//  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "CHANGE_LOG_consumer_syncGroups".equals(jobName);
  }
}
