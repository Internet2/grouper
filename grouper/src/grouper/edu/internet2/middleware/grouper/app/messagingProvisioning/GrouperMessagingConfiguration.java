package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperMessagingConfiguration extends GrouperProvisioningConfiguration {

  private String messagingType;
  
  private String routingKey; //applicable for rabbitmq only
  
  private GrouperMessageQueueType queueType;
  
  private GrouperMessagingExchangeType exchangeType; //applicable for rabbitmq only
  
  private String queueOrTopicName;
  
  private int numberOfQueueArguments;
  
  private GrouperMessagingFormatType messagingFormatType;
  
  private Map<String, Object> queueArguments = new HashMap<String, Object>();

  private String messagingExternalSystemConfigId;

  @Override
  public void configureSpecificSettings() {
    
    this.messagingType = this.retrieveConfigString("messagingType", true);
    if (StringUtils.equals(this.messagingType, "ActiveMQ")) {
      this.messagingExternalSystemConfigId = this.retrieveConfigString("messagingActiveMqExternalSystemConfigId", true);
    } else if (StringUtils.equals(this.messagingType, "AWS_SQS")) {
      this.messagingExternalSystemConfigId = this.retrieveConfigString("messagingAwsSqsExternalSystemConfigId", true);
    } else if (StringUtils.equals(this.messagingType, "RabbitMQ")) {
      this.messagingExternalSystemConfigId = this.retrieveConfigString("messagingRabbitMqExternalSystemConfigId", true);
      this.routingKey = this.retrieveConfigString("routingKey", false);
    } else if (StringUtils.equals(this.messagingType, "Grouper_Builtin")) {
      this.messagingExternalSystemConfigId = "grouperBuiltinMessaging";
    } else {
      throw new RuntimeException("Invalid messaging type");
    } 
    
    this.queueOrTopicName = this.retrieveConfigString("queueOrTopicName", true);
    this.queueType = GrouperMessageQueueType.valueOfIgnoreCase(this.retrieveConfigString("queueType", true), true);
    this.exchangeType = GrouperMessagingExchangeType.valueOfIgnoreCase(this.retrieveConfigString("exchangeType", false), false);
    this.messagingFormatType = GrouperMessagingFormatType.valueOfIgnoreCase(this.retrieveConfigString("messagingFormatType", true), true);
    
    this.numberOfQueueArguments = GrouperUtil.intValue(this.retrieveConfigInt("numberOfQueueArguments", false), 0);
    
    for (int i=0;i<this.numberOfQueueArguments;i++) {
      
      String key = this.retrieveConfigString("queueArgument."+i+".key", true);
      String value = this.retrieveConfigString("queueArgument."+i+".value", true);
      
      queueArguments.put(key, value);
    }
  }

  public String getRoutingKey() {
    return routingKey;
  }

  
  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

  
  public GrouperMessageQueueType getQueueType() {
    return queueType;
  }

  
  public void setQueueType(GrouperMessageQueueType queueType) {
    this.queueType = queueType;
  }

  
  public GrouperMessagingExchangeType getExchangeType() {
    return exchangeType;
  }

  
  public void setExchangeType(GrouperMessagingExchangeType exchangeType) {
    this.exchangeType = exchangeType;
  }

  
  public String getQueueOrTopicName() {
    return queueOrTopicName;
  }

  
  public void setQueueOrTopicName(String queueOrTopicName) {
    this.queueOrTopicName = queueOrTopicName;
  }

  
  public int getNumberOfQueueArguments() {
    return numberOfQueueArguments;
  }

  
  public void setNumberOfQueueArguments(int numberOfQueueArguments) {
    this.numberOfQueueArguments = numberOfQueueArguments;
  }

  
  public Map<String, Object> getQueueArguments() {
    return queueArguments;
  }

  
  public void setQueueArguments(Map<String, Object> queueArguments) {
    this.queueArguments = queueArguments;
  }

  
  public GrouperMessagingFormatType getMessagingFormatType() {
    return messagingFormatType;
  }

  
  public void setMessagingFormatType(GrouperMessagingFormatType messagingFormatType) {
    this.messagingFormatType = messagingFormatType;
  }

  
  public String getMessagingType() {
    return messagingType;
  }

  
  public void setMessagingType(String messagingType) {
    this.messagingType = messagingType;
  }
  
  public String getMessagingExternalSystemConfigId() {
    return this.messagingExternalSystemConfigId;
  }

  
  public void setMessagingExternalSystemConfigId(String messagingExternalSystemConfigId) {
    this.messagingExternalSystemConfigId = messagingExternalSystemConfigId;
  }
  
}
