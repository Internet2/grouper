package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemon;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class GrouperDaemonOtherJobWsMessagingBridgeConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## Message to WS Daemon Job
  //  #####################################
  //
  //  # message to ws daemon job class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
  //  #otherJob.messageConsumerDaemon.class = edu.internet2.middleware.grouper.app.messaging.MessageConsumerDaemon
  //
  //  # message to ws daemon job cron
  //  # {valueType: "string"}
  //  #otherJob.messageConsumerDaemon.quartzCron = 0 * * ? * *

      
  @Override
  public String getConfigIdRegex() {
    return "^(otherJob\\.messageConsumerDaemon)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "otherJob.messageConsumerDaemon.";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisDaemon() {
    return "class";
  }
  
  
  @Override
  public String getPropertyValueThatIdentifiesThisDaemon() {
    return MessageConsumerDaemon.class.getName();
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
    return "OTHER_JOB_messageConsumerDaemon".equals(jobName);
  }
}
