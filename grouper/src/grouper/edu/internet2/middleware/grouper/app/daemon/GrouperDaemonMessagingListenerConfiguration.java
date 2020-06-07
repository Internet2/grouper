package edu.internet2.middleware.grouper.app.daemon;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.messaging.MessagingListenerBase;

public class GrouperDaemonMessagingListenerConfiguration extends GrouperDaemonConfiguration {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  #####################################
  //  ## Messaging listener using the messaging API
  //  ## note, change "messagingListener" in key to be the name of the listener.  e.g. messaging.listener.myAzureListener.class
  //  ## extends edu.internet2.middleware.grouper.messaging.MessagingListenerBase
  //  ## note, routingKey property is valid only for rabbitmq. For other messaging systems, it is ignored.
  //  ## this listener will just print out messages: edu.internet2.middleware.grouper.messaging.MessagingListenerPrint
  //  #####################################
  //
  //  # messaging listener class
  //  # {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.messaging.MessagingListenerBase", regex: "^messaging\\.listener\\.([^.]+)\\.class$"}
  //  #messaging.listener.messagingListener.class = edu.internet2.middleware.grouper.messaging.MessagingListener
  //
  //  # messaging listener quartz cron
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.quartzCron$"}
  //  #messaging.listener.messagingListener.quartzCron = 0 * * * * ?
  //
  //  # messaging listener messaging system name
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.messagingSystemName$"}
  //  #messaging.listener.messagingListener.messagingSystemName = grouperBuiltinMessaging
  //
  //  # messaging listener queue name
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.queueName$"}
  //  #messaging.listener.messagingListener.queueName = abc
  //
  //  # messaging listener routing key
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.routingKey$"}
  //  #messaging.listener.messagingListener.routingKey =
  //
  //  # messaging listener exchange type. Valid options are DIRECT, HEADERS, TOPIC, FANOUT
  //  # {valueType: "string", regex: "^messaging\\.listener\\.([^.]+)\\.exchangeType$"}
  //  #messaging.listener.messagingListener.exchangeType =
  //
  //  # messaging listener number of tries per iteration
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.numberOfTriesPerIteration$"}
  //  #messaging.listener.messagingListener.numberOfTriesPerIteration = 3
  //
  //  # messaging listener polling timeout seconds
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.pollingTimeoutSeconds$"}
  //  #messaging.listener.messagingListener.pollingTimeoutSeconds = 18
  //
  //  # messaging listener sleep seconds in between iterations
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.sleepSecondsInBetweenIterations$"}
  //  #messaging.listener.messagingListener.sleepSecondsInBetweenIterations = 0
  //
  //  # messaging listener max messages to receive at once
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.maxMessagesToReceiveAtOnce$"}
  //  #messaging.listener.messagingListener.maxMessagesToReceiveAtOnce = 20
  //
  //  # if there are 20 messages to receive at once, then do this 50 times per call max
  //  # {valueType: "integer", regex: "^messaging\\.listener\\.([^.]+)\\.maxOuterLoops$"}
  //  #messaging.listener.messagingListener.maxOuterLoops = 50

      
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
  public String getDaemonJobPrefix() {
    return GrouperLoaderType.GROUPER_MESSAGING_LISTENER_PREFIX;
  }

  @Override
  public boolean isMultiple() {
    return true;
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisDaemon() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisDaemon() {
    return MessagingListenerBase.class.getName();
  }

  @Override
  public boolean matchesQuartzJobName(String jobName) {
    return jobName != null && jobName.startsWith(GrouperLoaderType.GROUPER_MESSAGING_LISTENER_PREFIX);
  }
}
