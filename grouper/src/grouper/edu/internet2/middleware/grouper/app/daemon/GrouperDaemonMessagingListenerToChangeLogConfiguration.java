package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.messaging.MessagingListenerToChangeLogConsumer;

public class GrouperDaemonMessagingListenerToChangeLogConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## Messaging listener using the change log consumer API
  //  #####################################
  //
  //  # note, change "messagingListenerChangeLogConsumer" in key to be the name of the listener.  e.g. messaging.listener.myAzureListener.class
  //  # keep this class to be MessagingListenerToChangeLogConsumer
  //  # {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.messaging.MessagingListenerToChangeLogConsumer", regex: "^messaging\\.listener\\.([^.]+)\\.class$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.class = edu.internet2.middleware.grouper.messaging.MessagingListenerToChangeLogConsumer
  //
  //  # Class extends: edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase
  //  # {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase", regex: "^messaging\\.listener\\.([^.]+)\\.changeLogConsumerClass$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.changeLogConsumerClass = edu.internet2.middleware.grouper.messaging.SomethingExtendsChangeLogConsumerBase
  //
  //  # messaging listener quartz cron
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.quartzCron$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.quartzCron = 0 * * * * ?
  //
  //  # system name
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.messagingSystemName$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.messagingSystemName = grouperBuiltinMessaging
  //
  //  # queue name in messaging system
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.queueName$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.queueName = abc
  //
  //  # number of tries per iteration
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.numberOfTriesPerIteration$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.numberOfTriesPerIteration = 3
  //
  //  # polling timeout seconds
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.pollingTimeoutSeconds$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.pollingTimeoutSeconds = 18
  //
  //  # sleep seconds in between iteration
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.sleepSecondsInBetweenIterations$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.sleepSecondsInBetweenIterations = 0
  //
  //  # max messages to receive at once
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.maxMessagesToReceiveAtOnce$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.maxMessagesToReceiveAtOnce = 20
  //
  //  # max outer loops
  //  # if there are 20 messages to receive at once, then do this 50 times per call max
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.maxOuterLoops$"}
  //  #messaging.listener.messagingListenerChangeLogConsumer.maxOuterLoops = 50

      
  @Override
  public String getConfigIdRegex() {
    return "^(messaging\\.listener)\\.([^.]+)\\.(.+)$";
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "messaging.listener." + this.getConfigId() + ".";
  }
    
  
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return MessagingListenerToChangeLogConsumer.class.getName();
  }

  @Override
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_MESSAGING_LISTENER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    if (jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_MESSAGING_LISTENER_PREFIX)) {
      if (StringUtils.equals(this.getPropertyValueThatIdentifiesThisConfig(),
          GrouperLoaderConfig.retrieveConfig().propertyValueString(this.getConfigItemPrefix() 
              + this.getPropertySuffixThatIdentifiesThisConfig()))) {
        return true;
      }
    }
    return false;
  }
}
