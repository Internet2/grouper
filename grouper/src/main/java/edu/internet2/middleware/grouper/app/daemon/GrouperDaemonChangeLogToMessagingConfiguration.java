package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerToMessage;

public class GrouperDaemonChangeLogToMessagingConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## Messaging integration with change log, send change log entries to a messaging system
  //  #####################################
  //
  //  # note, change "messaging" in key to be the name of the consumer.  e.g. changeLog.consumer.myAzureConsumer.class
  //  # note, routingKey property is valid only for rabbitmq. For other messaging systems, it is ignored.
  //  # {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerToMessage", regex: "^changeLog\\.consumer\\.([^.]+)\\.class$"}
  //  #changeLog.consumer.messaging.class = edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerToMessage
  //
  //  # quartz cron
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.quartzCron$"}
  //  #changeLog.consumer.messaging.quartzCron = 0 * * * * ?
  //
  //  # system name
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.messagingSystemName$"}
  //  #changeLog.consumer.messaging.messagingSystemName = grouperBuiltinMessaging
  //
  //  # routing key
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.routingKey$"}
  //  #changeLog.consumer.messaging.routingKey = 
  //
  //  # exchange type. valid options are DIRECT, TOPIC, HEADERS, FANOUT
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.exchangeType$"}
  //  #changeLog.consumer.messaging.exchangeType = 
  //
  //  # queue or topic
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.messageQueueType$"}
  //  #changeLog.consumer.messaging.messageQueueType = queue
  //
  //  # queue or topic name
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.queueOrTopicName$"}
  //  #changeLog.consumer.messaging.queueOrTopicName = abc

      
  @Override
  public String getConfigIdRegex() {
    return "^(changeLog\\.consumer)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "changeLog.consumer." + this.getConfigId() + ".";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return ChangeLogConsumerToMessage.class.getName();
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX)) {
      if (StringUtils.equals(this.getPropertyValueThatIdentifiesThisConfig(),
          GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() 
              + this.getPropertySuffixThatIdentifiesThisConfig()))) {
        return true;
      }
    }
    return false;
  }
}
