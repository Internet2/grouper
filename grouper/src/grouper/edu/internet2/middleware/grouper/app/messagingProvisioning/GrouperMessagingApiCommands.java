package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperMessagingApiCommands {
  
  
  private static ObjectNode createMessageWrapperAndEnhanceMessage(ObjectNode message) {
    
    message.put("changeOccurred", true);
    message.put("createdOnMicros", System.currentTimeMillis() * 1000);
    
    
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode wrapper = objectMapper.createObjectNode();
    
    wrapper.put("encrypted", false);
    
    ArrayNode messagesNode = objectMapper.createArrayNode();
    messagesNode.add(message);
    
    wrapper.set("esbEvent", messagesNode);
    
    return wrapper;
    
  }
  
  public static GrouperMessageSendResult sendInsertEntityMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingEntity grouperMessagingEntity, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendInsertEntityMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingEntity.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBER_ADD");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendDeleteEntityMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingEntity grouperMessagingEntity, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendDeleteEntityMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingEntity.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBER_DELETE");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendUpdateEntityMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingEntity grouperMessagingEntity, Long indexToBeAdded) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "sendUpdateEntityMesssage");
    
    long startTime = System.nanoTime();
    
    try {
      
      ObjectNode messageObjectNode = grouperMessagingEntity.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBER_UPDATE");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendInsertGroupMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingGroup grouperMessagingGroup, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendInsertGroupMesssage");

    long startTime = System.nanoTime();
   

    try {
      
      ObjectNode messageObjectNode = grouperMessagingGroup.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "GROUP_ADD");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);

      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendUpdateGroupMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingGroup grouperMessagingGroup, Long indexToBeAdded,
      String propertyChanged, Object propertyOldValue, Object propertyNewValue) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendUpdateGroupMesssage");

    long startTime = System.nanoTime();
   

    try {
      
      ObjectNode messageObjectNode = grouperMessagingGroup.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "GROUP_UPDATE");
      
      messageObjectNode.put("propertyChanged", propertyChanged);
      messageObjectNode.put("propertyOldValue", propertyOldValue == null ? null: propertyOldValue.toString());
      messageObjectNode.put("propertyNewValue", propertyNewValue == null ? null: propertyNewValue.toString());
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);

      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendDeleteGroupMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingGroup grouperMessagingGroup, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendDeleteGroupMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingGroup.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "GROUP_DELETE");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendInsertMembershipMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingMembership grouperMessagingMembership, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendInsertMembershipMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingMembership.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBERSHIP_ADD");
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  
  public static GrouperMessageSendResult sendDeleteMembershipMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingMembership grouperMessagingMembership, Long indexToBeAdded) {
    
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendDeleteMembershipMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingMembership.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBERSHIP_DELETE"); 
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  public static GrouperMessageSendResult sendUpdateMembershipMesssage(GrouperMessagingConfiguration grouperMessagingConfiguration,
      GrouperMessagingMembership grouperMessagingMembership, Long indexToBeAdded,
      String propertyChanged, Object propertyOldValue, Object propertyNewValue) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "sendUpdateMembershipMesssage");

    long startTime = System.nanoTime();

    try {
      
      ObjectNode messageObjectNode = grouperMessagingMembership.toJson(grouperMessagingConfiguration);
      
      messageObjectNode.put("eventType", "MEMBERSHIP_UPDATE"); 
      
      messageObjectNode.put("propertyChanged", propertyChanged);
      messageObjectNode.put("propertyOldValue", propertyOldValue == null ? null: propertyOldValue.toString());
      messageObjectNode.put("propertyNewValue", propertyNewValue == null ? null: propertyNewValue.toString());
      
      return sendMessage(grouperMessagingConfiguration, messageObjectNode, indexToBeAdded);

      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperMessagingLog.messagingLog(debugMap, startTime);
    }
    
  }
  
  private static GrouperMessageSendResult sendMessage(GrouperMessagingConfiguration grouperMessagingConfiguration, ObjectNode messageObjectNode, Long indexToBeAdded) {
    
    messageObjectNode.put("sequenceNumber", indexToBeAdded);
    
    boolean autocreateObjects = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.messaging.settings.autocreate.objects", true);
    
    ObjectNode jsonToSend = createMessageWrapperAndEnhanceMessage(messageObjectNode);
    
    String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
    
    GrouperMessageSendResult grouperMessageSendResult = GrouperMessagingEngine.send(new GrouperMessageSendParam().assignGrouperMessageSystemName(grouperMessagingConfiguration.getMessagingExternalSystemConfigId())
        .assignAutocreateObjects(autocreateObjects)
        .assignQueueType(grouperMessagingConfiguration.getQueueType())
        .assignQueueOrTopicName(grouperMessagingConfiguration.getQueueOrTopicName())
        .addMessageBody(jsonStringToSend)
        .assignRoutingKey(grouperMessagingConfiguration.getRoutingKey())
        .assignExchangeType(grouperMessagingConfiguration.getExchangeType() == null? null: grouperMessagingConfiguration.getExchangeType().name())
        .assignQueueArguments(grouperMessagingConfiguration.getQueueArguments()));
    
    return grouperMessageSendResult;
    
  }

}
