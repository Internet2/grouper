package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher;

public class GrouperDaemonChangeLogEsbToMessagingConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## Messaging integration with ESB, send change log entries to a messaging system
  //  #####################################
  //
  //  # note, change "messagingEsb" in key to be the name of the consumer.  e.g. changeLog.consumer.myAzureConsumer.class
  //  # note, routingKey property is valid only for rabbitmq. For other messaging systems, it is ignored.
  //  #changeLog.consumer.messagingEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
  //
  //  # quartz cron
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.quartzCron$"}
  //  #changeLog.consumer.messagingEsb.quartzCron = 0 * * * * ?
  //
  //  # el filter
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.elfilter$"}
  //  #changeLog.consumer.messagingEsb.elfilter = event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'
  //
  //  # publishing class
  //  # {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.class$"}
  //  #changeLog.consumer.messagingEsb.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher
  //
  //  # messaging system name
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.messagingSystemName$"}
  //  #changeLog.consumer.messagingEsb.publisher.messagingSystemName = grouperBuiltinMessaging
  //
  //  # routing key
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.routingKey$"}
  //  #changeLog.consumer.messagingEsb.publisher.routingKey = 
  //
  //  # EL replacement definition. groupName is the variable for the name of the group. grouperUtil is the class GrouperUtilElSafe can be used for utility methods. 
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.regexRoutingKeyReplacementDefinition$"}
  //  #changeLog.consumer.messagingEsb.regexRoutingKeyReplacementDefinition = ${groupName.replaceFirst('hawaii.edu', 'group.modify').replace(':enrolled', '').replace(':waitlisted', '').replace(':withdrawn', '')}
  //
  //  # replace routing key with periods
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.replaceRoutingKeyColonsWithPeriods$"}
  //  #changeLog.consumer.messagingEsb.replaceRoutingKeyColonsWithPeriods = true
  //
  //  # queue or topic
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.messageQueueType$"}
  //  #changeLog.consumer.messagingEsb.publisher.messageQueueType = queue
  //
  //  # queue or topic name
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.queueOrTopicName$"}
  //  #changeLog.consumer.messagingEsb.publisher.queueOrTopicName = abc
  //
  //  # exchange type for rabbitmq. valid options are DIRECT, TOPIC, HEADERS, FANOUT
  //  # {valueType: "string", regex: "^changeLog\\.consumer\\.([^.]+)\\.publisher\\.exchangeType$"}
  //  #changeLog.consumer.messagingEsb.publisher.exchangeType = 

      
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
    return "publisher.class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return EsbMessagingPublisher.class.getName();
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
