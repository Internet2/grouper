package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogRecentMembershipsConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # recent-memberships consumer will update recent-membership groups as memberships/attributes change
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase"}
//  changeLog.consumer.recentMemberships.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
//
//  # recent-memberships runs as change log consumer
//  # {valueType: "string"}
//  changeLog.consumer.recentMemberships.quartzCron = 
//
//  # if this many records happens in one change log session, just do a full loader job
//  # {valueType: "integer", defaultValue: "100"}
//  changeLog.consumer.recentMemberships.maxUntilFullSync = 100
//
//  # publishing class for recent-memberships
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher"}
//  changeLog.consumer.recentMemberships.publisher.class = edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMembershipsChangeLogConsumer

      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer\\.recentMemberships)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.consumer.recentMemberships.";
  }
  
  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "CHANGE_LOG_consumer_recentMemberships".equals(jobName);
  }
}
