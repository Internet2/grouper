package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;

public class GrouperMessagingConfiguration extends GrouperProvisioningConfigurationBase {

  private String messagingExternalSystemConfigId;
  
  private String routingKey; //applicable for rabbitmq only
  
  private GrouperMessageQueueType queueType; 
  
  private GrouperMessagingExchangeType exchangeType; //applicable for rabbitmq only
  
  private String queueOrTopicName;
  
  private int numberOfQueueArguments;
  
  private GrouperMessagingFormatType messagingFormatType;
  
  private Map<String, Object> queueArguments = new HashMap<String, Object>();

  @Override
  public void configureSpecificSettings() {
    
    this.messagingExternalSystemConfigId = this.retrieveConfigString("messagingExternalSystemConfigId", true);
    this.routingKey = this.retrieveConfigString("routingKey", false);
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

  public String getMessagingExternalSystemConfigId() {
    return messagingExternalSystemConfigId;
  }

  
  public void setMessagingExternalSystemConfigId(String messagingExternalSystemConfigId) {
    this.messagingExternalSystemConfigId = messagingExternalSystemConfigId;
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
  
}
