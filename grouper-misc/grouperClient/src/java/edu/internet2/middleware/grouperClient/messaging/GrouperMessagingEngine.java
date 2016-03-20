/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import java.util.Collection;

import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * engine sends, receives, marks messages complete
 */
public class GrouperMessagingEngine {

  /**
   * 
   */
  public GrouperMessagingEngine() {
  }

  /**
   * send one or more messages 
   * @param grouperMessageSendParam parameters to send message
   * @return result
   */
  public static GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {

    Collection<GrouperMessage> grouperMessages = grouperMessageSendParam.getGrouperMessages();
    
    if (GrouperClientUtils.length(grouperMessages) == 0) {
      throw new NullPointerException("Why no messages to send?");
    }
    
    String grouperMessageSystemName = retrieveGrouperMessageSystemName(grouperMessageSendParam.getGrouperMessageSystemParam());

    GrouperMessageQueueParam grouperMessageQueueParam = grouperMessageSendParam.getGrouperMessageQueueParam();
    
    if (grouperMessageQueueParam == null || GrouperClientUtils.isBlank(grouperMessageQueueParam.getQueueOrTopic())) {
      throw new RuntimeException("You must specify a queue or topic in grouperMessageQueueConfig");
    }

    for (GrouperMessage grouperMessage : grouperMessages) {
      
      if (grouperMessage == null) {
        throw new RuntimeException("grouperMessage is null");
      }
      
    }
    
    GrouperMessagingSystem grouperMessagingSystem = retrieveGrouperMessageSystem(grouperMessageSystemName);

    return grouperMessagingSystem.send(grouperMessageSendParam);
    
  }

  /**
   * @param grouperMessageSystemConfig
   * @return the string
   */
  public static String retrieveGrouperMessageSystemName(
      GrouperMessageSystemParam grouperMessageSystemConfig) {
    String grouperMessageSystemName = null;
    if (grouperMessageSystemConfig == null || GrouperClientUtils.isBlank(grouperMessageSystemConfig.getMessageSystemName())) {
      grouperMessageSystemName = GrouperClientConfig.retrieveConfig().propertyValueString("grouper.messaging.default.name.of.messaging.system");
      if (GrouperClientUtils.isBlank(grouperMessageSystemName)) {
        throw new RuntimeException("You either need to specify the messageSystemName in a message send config or you need to have a grouper.client.properties entry for grouper.messaging.default.name.of.messaging.system");
      }
    } else {
      grouperMessageSystemName = grouperMessageSystemConfig.getMessageSystemName();
    }
    return grouperMessageSystemName;
  }

  /**
   * @param grouperMessageSystemName
   * @return the system
   */
  public static GrouperMessagingSystem retrieveGrouperMessageSystem(String grouperMessageSystemName) {
    GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigs().get(grouperMessageSystemName);
    
    //valid name?
    if (grouperMessagingConfig == null) {
      throw new NullPointerException("Cant find config for messageSystemName: '" + grouperMessageSystemName + "'");
    }
    Class<GrouperMessagingSystem> grouperMessagingSystemClass = grouperMessagingConfig.getTheClass();
    
    if (grouperMessagingSystemClass == null) {
      throw new NullPointerException("Cant find class in grouper.client.properties: grouper.messaging.system." + grouperMessagingConfig.getName() + ".class");
    }
    
    GrouperMessagingSystem grouperMessagingSystem = null;
    
    try {
      grouperMessagingSystem = GrouperClientUtils.newInstance(grouperMessagingSystemClass);
    } catch (Exception e) {
      throw new RuntimeException("Cant instantiate class: " + grouperMessagingSystemClass.getName(), e);
    }
    
    return grouperMessagingSystem;
  }
  
  /**
   * this will generally block until there are messages to process.  These messages
   * are ordered in the order that they were sent.
   * @param grouperMessageReceiveParam param to receive messages
   * @return a message or multiple messages.  It will block until there are messages
   * available for this recipient to process
   */
  public static GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {

    String grouperMessageSystemName = retrieveGrouperMessageSystemName(grouperMessageReceiveParam.getGrouperMessageSystemParam());

    GrouperMessageQueueParam grouperMessageQueueParam = grouperMessageReceiveParam.getGrouperMessageQueueParam();
    
    if (grouperMessageQueueParam == null || GrouperClientUtils.isBlank(grouperMessageQueueParam.getQueueOrTopic())) {
      throw new RuntimeException("You must specify a queue or topic in grouperMessageQueueConfig");
    }

    GrouperMessagingSystem grouperMessagingSystem = retrieveGrouperMessageSystem(grouperMessageSystemName);
    
    return grouperMessagingSystem.receive(grouperMessageReceiveParam);
    
  }
  
  /**
   * mark messages that we received and processed
   * @param grouperMessageProcessedParam
   * @return result
   */
  public static GrouperMessageProcessedResult markAsProcessed(GrouperMessageProcessedParam grouperMessageProcessedParam) {
   Collection<GrouperMessage> grouperMessages = grouperMessageProcessedParam.getGrouperMessages();
    
    if (GrouperClientUtils.length(grouperMessages) == 0) {
      throw new NullPointerException("Why no messages to send?");
    }
    
    String grouperMessageSystemName = retrieveGrouperMessageSystemName(grouperMessageProcessedParam.getGrouperMessageSystemParam());

    GrouperMessageQueueParam grouperMessageQueueParam = grouperMessageProcessedParam.getGrouperMessageQueueParam();
    
    if (grouperMessageQueueParam == null || GrouperClientUtils.isBlank(grouperMessageQueueParam.getQueueOrTopic())) {
      throw new RuntimeException("You must specify a queue or topic in grouperMessageQueueConfig");
    }

    for (GrouperMessage grouperMessage : grouperMessages) {
      
      if (grouperMessage == null) {
        throw new RuntimeException("grouperMessage is null");
      }
      
    }
    
    GrouperMessagingSystem grouperMessagingSystem = retrieveGrouperMessageSystem(grouperMessageSystemName);

    return grouperMessagingSystem.markAsProcessed(grouperMessageProcessedParam);
    
  }
  
}
