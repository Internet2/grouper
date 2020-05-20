package edu.internet2.middleware.grouper.app.daemon;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonChangeLogRules extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

//  # rules consumer, needed for some of the Grouper rule types to run (e.g. flattenedMembershipRemove, flattenedMembershipAdd)
//  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase"}
//  changeLog.consumer.grouperRules.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.RuleConsumer
//
//  # rules consumer, needed for some of the Grouper rule types to run (e.g. flattenedMembershipRemove, flattenedMembershipAdd)
//  # {valueType: "string"}
//  changeLog.consumer.grouperRules.quartzCron =
      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer\\.grouperRules)\\.(.*)$";
  }

  @Override
  public String getConfigItemPrefix() {
    return "changeLog.consumer.grouperRules.";
  }
    
  @Override
  public boolean isMultiple() {
    return false;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return "CHANGE_LOG_consumer_grouperRules".equals(jobName);
  }
}
