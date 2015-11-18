/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageProcessedParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageProcessedResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;
import edu.internet2.middleware.subject.Subject;


/**
 * built in messaging system from database
 */
public class GrouperBuiltinMessagingSystem implements GrouperMessagingSystem {

  /**
   * cache stuff in messaging, key is Boolean.TRUE
   */
  private static GrouperCache<Boolean, GrouperBuiltinMessagingCache> messageCache = new GrouperCache(
      "edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem.messageCache");

  /**
   * get the cache, if needed recalculate everything
   * @return the cache
   */
  private static GrouperBuiltinMessagingCache retrieveCache() {
    
    GrouperBuiltinMessagingCache grouperBuiltinMessagingCache = messageCache.get(Boolean.TRUE);
    if (grouperBuiltinMessagingCache == null) {
      synchronized (GrouperBuiltinMessagingCache.class) {

        grouperBuiltinMessagingCache = messageCache.get(Boolean.TRUE);
        
        if (grouperBuiltinMessagingCache == null) {

          GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
          GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
          
          try {
            
            GrouperBuiltinMessagingCache tempGrouperBuiltinMessagingCache = new GrouperBuiltinMessagingCache();
            
            Stem messageStem = StemFinder.findByName(grouperSession, messageRootStemName(), true, new QueryOptions().secondLevelCache(false));
            
            tempGrouperBuiltinMessagingCache.setRootStem(messageStem);

            {
              AttributeDef queueDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageQueueNameOfDef()).findAttribute();
              
              tempGrouperBuiltinMessagingCache.setQueueAttributeDef(queueDef);
              
              Map<String, AttributeDefName> queueMap = new HashMap<String, AttributeDefName>();
              
              Stem queueStem = StemFinder.findByName(grouperSession, queueStemName(), true, new QueryOptions().secondLevelCache(false));
              
              Set<AttributeDefName> queueAttributeDefNames = new AttributeDefNameFinder().assignParentStemId(messageStem.getId())
                  .assignAttributeDefId(queueDef.getId()).assignParentStemId(queueStem.getId()).findAttributeNames();
  
              for (AttributeDefName queueAttributeDefName : queueAttributeDefNames) {
                queueMap.put(queueAttributeDefName.getExtension(), queueAttributeDefName);
              }
              
              tempGrouperBuiltinMessagingCache.setExtensionOfQueueToAttributeDefName(queueMap);
            }
            
            {
              AttributeDef topicDef = new AttributeDefFinder().assignFindByUuidOrName(false).assignScope(grouperMessageTopicNameOfDef()).findAttribute();
              
              tempGrouperBuiltinMessagingCache.setTopicAttributeDef(topicDef);
              
              Map<String, AttributeDefName> topicMap = new HashMap<String, AttributeDefName>();
              
              Stem topicStem = StemFinder.findByName(grouperSession, topicStemName(), true, new QueryOptions().secondLevelCache(false));
              
              Set<AttributeDefName> topicAttributeDefNames = new AttributeDefNameFinder().assignParentStemId(messageStem.getId())
                  .assignAttributeDefId(topicDef.getId()).assignParentStemId(topicStem.getId()).findAttributeNames();
  
              for (AttributeDefName topicAttributeDefName : topicAttributeDefNames) {
                topicMap.put(topicAttributeDefName.getExtension(), topicAttributeDefName);
              }
              
              tempGrouperBuiltinMessagingCache.setExtensionOfTopicToAttributeDefName(topicMap);
            }
            messageCache.put(Boolean.TRUE, tempGrouperBuiltinMessagingCache);

            grouperBuiltinMessagingCache = tempGrouperBuiltinMessagingCache;
          } finally {
            if (grouperSessionResult.isCreated()) {
              GrouperSession.stopQuietly(grouperSession);
            }
          }
        }
        
      }
    }
    return grouperBuiltinMessagingCache;
  }
  
  /**
   * cache stuff for messaging
   */
  private static class GrouperBuiltinMessagingCache {
    
    /**
     * root message stem
     */
    private Stem rootStem = null;

    /**
     * queue attribute def
     */
    private AttributeDef queueAttributeDef;

    /**
     * topic attribute def
     */
    private AttributeDef topicAttributeDef;

    /**
     * map of queue extension to attribute def name
     */
    private Map<String, AttributeDefName> extensionOfQueueToAttributeDefName = null;

    /**
     * map of topic extension to attribute def name
     */
    private Map<String, AttributeDefName> extensionOfTopicToAttributeDefName = null;

    /**
     * topic extension sends to queue extension, lazy loaded
     */
    private Map<String, String> topicExtSendsToQueueExtMap = new HashMap<String, String>();

    /**
     * hashmap of source id, subject id, topic name, boolean if allow or not, lazy loaded
     */
    private Map<MultiKey, Boolean> allowedToSendToTopic = new HashMap<MultiKey, Boolean>();
    
    
    /**
     * hashmap of source id, subject id, topic name, boolean if allow or not, lazy loaded
     * @return the allowedToSendToTopic
     */
    public Map<MultiKey, Boolean> getAllowedToSendToTopic() {
      return this.allowedToSendToTopic;
    }

    
    /**
     * hashmap of source id, subject id, topic name, boolean if allow or not, lazy loaded
     * @param allowedToSendToTopic1 the allowedToSendToTopic to set
     */
    public void setAllowedToSendToTopic(Map<MultiKey, Boolean> allowedToSendToTopic1) {
      this.allowedToSendToTopic = allowedToSendToTopic1;
    }

    /**
     * topic extension sends to queue extension, lazy loaded
     * @return the topicExtSendsToQueueExtMap
     */
    public Map<String, String> getTopicExtSendsToQueueExtMap() {
      return this.topicExtSendsToQueueExtMap;
    }
    
    /**
     * topic extension sends to queue extension, lazy loaded
     * @param topicExtSendsToQueueExtMap1 the topicExtSendsToQueueExtMap to set
     */
    public void setTopicExtSendsToQueueExtMap(Map<String, String> topicExtSendsToQueueExtMap1) {
      this.topicExtSendsToQueueExtMap = topicExtSendsToQueueExtMap1;
    }


    /**
     * root message stem
     * @return the rootStem
     */
    public Stem getRootStem() {
      return this.rootStem;
    }

    
    /**
     * root message stem
     * @param rootStem1 the rootStem to set
     */
    public void setRootStem(Stem rootStem1) {
      this.rootStem = rootStem1;
    }

    
    /**
     * queue attribute def
     * @return the queueAttributeDef
     */
    public AttributeDef getQueueAttributeDef() {
      return this.queueAttributeDef;
    }

    
    /**
     * queue attribute def
     * @param queueAttributeDef1 the queueAttributeDef to set
     */
    public void setQueueAttributeDef(AttributeDef queueAttributeDef1) {
      this.queueAttributeDef = queueAttributeDef1;
    }

    
    /**
     * topic attribute def
     * @return the topicAttributeDef
     */
    public AttributeDef getTopicAttributeDef() {
      return this.topicAttributeDef;
    }

    
    /**
     * topic attribute def
     * @param topicAttributeDef1 the topicAttributeDef to set
     */
    public void setTopicAttributeDef(AttributeDef topicAttributeDef1) {
      this.topicAttributeDef = topicAttributeDef1;
    }

    
    /**
     * map of queue extension to attribute def name
     * @return the extensionOfQueueToAttributeDefName
     */
    public Map<String, AttributeDefName> getExtensionOfQueueToAttributeDefName() {
      return this.extensionOfQueueToAttributeDefName;
    }

    
    /**
     * map of queue extension to attribute def name
     * @param extensionOfQueueToAttributeDefName1 the extensionOfQueueToAttributeDefName to set
     */
    public void setExtensionOfQueueToAttributeDefName(
        Map<String, AttributeDefName> extensionOfQueueToAttributeDefName1) {
      this.extensionOfQueueToAttributeDefName = extensionOfQueueToAttributeDefName1;
    }

    
    /**
     * map of topic extension to attribute def name
     * @return the extensionOfTopicToAttributeDefName
     */
    public Map<String, AttributeDefName> getExtensionOfTopicToAttributeDefName() {
      return this.extensionOfTopicToAttributeDefName;
    }

    
    /**
     * map of topic extension to attribute def name
     * @param extensionOfTopicToAttributeDefName1 the extensionOfTopicToAttributeDefName to set
     */
    public void setExtensionOfTopicToAttributeDefName(
        Map<String, AttributeDefName> extensionOfTopicToAttributeDefName1) {
      this.extensionOfTopicToAttributeDefName = extensionOfTopicToAttributeDefName1;
    }
    
    
    
  }
  
  /**
   * message root stem
   * @return the message root stem
   */
  public static String messageRootStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":messages";
  }

  /**
   * grouper message topic name of attribute def
   * @return the name
   */
  public static String grouperMessageTopicNameOfDef() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageTopicDef";
  }

  /**
   * grouper message queue name of attribute def
   * @return the name
   */
  public static String grouperMessageQueueNameOfDef() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageQueueDef";
  }

  /**
   * topic stem name
   * @return topic stem name GrouperBuiltinMessagingSystem.
   */
  public static String topicStemName() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageTopics";
  }
  
  /**
   * queue stem name GrouperBuiltinMessagingSystem.
   * @return queue stem name
   */
  public static String queueStemName() {
    return GrouperBuiltinMessagingSystem.messageRootStemName() + ":grouperMessageQueues";
  }

  /**
   * name of built in messaging system
   */
  public final static String BUILTIN_NAME = "grouperBuiltinMessaging";
  
  /**
   * 
   * @param topicName
   */
  public static void createTopic(String topicName) {
  }
  
  /**
   * 
   * @param queueName
   */
  public static void createQueue(String queueName) {
    
  }
  
  /**
   * 
   * @param topicName
   * @param queueName
   */
  public static void topicSendsToQueue(String topicName, String queueName) {
    
  }

  /**
   * 
   * @param topicName
   * @param subject
   */
  public static void allowSendToTopic(String topicName, Subject subject) {
    
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   */
  public static void allowSendToQueue(String queueName, Subject subject) {
    
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   */
  public static void allowReceiveFromQueue(String queueName, Subject subject) {
    
  }
  
  /**
   * 
   * @param queueName
   * @param subject
   * @return true if allowed to send to queue
   */
  public static boolean allowedToSendToQueue(String queueName, Subject subject) {
    return false;
  }

  /**
   * 
   * @param queueName
   * @param subject
   * @return true if allowed to send to queue
   */
  public static boolean allowedToReceiveFromQueue(String queueName, Subject subject) {
    return false;
  }

  /**
   * 
   * @param topicName
   * @param subject
   * @return true if allowed to send to topic
   */
  public static boolean allowedToSendToTopic(String topicName, Subject subject) {
    return false;
  }

  /**
   * 
   * @param topicName
   * @return the queue names that the topic sends to
   */
  public static Collection<String> queuesTopicSendsTo(String topicName) {
    return null;
  }
  
  /** last millis number */
  private static long lastMillis = -1;
  
  /** nanos when the last millis was taken (since nanos are diffs) */
  private static long millisNanos = -1;
  
  /** last id generated */
  private static long lastResult = -1;
  /**
   * get a change log id
   * @return a change log id value
   */
  public static long messageId() {
    long currentMillis = System.currentTimeMillis();
    long currentNanos = System.nanoTime();
    int currentThousandthsMicros = 0;
    long result = -1L;
    synchronized (GrouperBuiltinMessagingSystem.class) {
      //see if a milli has gone by since the last check
      if (currentMillis > lastMillis) {
        lastMillis = currentMillis;
        millisNanos = currentNanos;
      } else {
        
        //if less, then must have incremented
        currentMillis = lastMillis;
        
        //see if the micros are more.  if the number is 123456789, we want to get the 123456 number
        //note, this might add millis too, thats ok
        currentThousandthsMicros = (int)((currentNanos - millisNanos) / 1000);
      }
      
      //calculate and return
      result = (currentMillis * 1000) + currentThousandthsMicros;
      
      //make sure greater
      if (result <= lastResult) {
        result = lastResult + 1;
      }
      lastResult = result;
      
    }
    return result;
    
  }

  /**
   * 
   */
  public GrouperBuiltinMessagingSystem() {
  }

  /**
   * state of a message
   */
  public static enum GrouperBuiltinMessageState {
    
    /** if in queue waiting to be retrieved */
    IN_QUEUE, 
    
    /** if it is delivered but not confirmed */
    GET_ATTEMPTED, 
    
    /** if it is processed and ready to be deleted */
    PROCESSED;

    /**
     * convert a string to a message state
     * @param input
     * @param exceptionIfNotFound
     * @return the state or null
     */
    public static GrouperBuiltinMessageState valueOfIgnoreCase(String input, boolean exceptionIfNotFound) {
      return GrouperUtil.enumValueOfIgnoreCase(GrouperBuiltinMessageState.class, input, exceptionIfNotFound, true);
    }
    
  }
  
  /*
   * topics to queues
   * 
   * folder for topics: permission resource topics, add queues to them (implied by), whatever queues a topic implies will be sent to when sent to topic
   * action for send_to_topic for topic, granted to subjects
   * 
   * folder for queues: permission resource queues, action send_to_queue/receive for queue, granted to subjects
   * 
   * 
   */
  
  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam) {
    
    String queueOrTopic = grouperMessageSendParam.getGrouperMessageQueueParam().getQueueOrTopic();
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    Member fromMember = grouperSession.getMember();
    for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessageSendParam.getGrouperMessages())) {
      GrouperMessageHibernate grouperMessageHibernate = new GrouperMessageHibernate();
      if (!StringUtils.isBlank(grouperMessage.getFromMemberId())) {
        throw new RuntimeException("fromMemberId must be null in a message");
      }
      if (!StringUtils.isBlank(grouperMessage.getId())) {
        throw new RuntimeException("id must be null in a message");
      }
      grouperMessageHibernate.setFromMemberId(fromMember.getId());
      grouperMessageHibernate.setId(GrouperUuid.getUuid());
      grouperMessageHibernate.setMessageBody(grouperMessage.getMessageBody());
      grouperMessageHibernate.setQueueName(queueOrTopic);
      grouperMessageHibernate.setSentTimeMicros(messageId());
      grouperMessageHibernate.setState(GrouperBuiltinMessageState.IN_QUEUE.name());
      grouperMessageHibernate.saveOrUpdate();
    }
    return new GrouperMessageSendResult();
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#markAsProcessed(edu.internet2.middleware.grouperClient.messaging.GrouperMessageProcessedParam)
   */
  public GrouperMessageProcessedResult markAsProcessed(GrouperMessageProcessedParam grouperMessageProcessedParam) {
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receive(GrouperMessageReceiveParam)
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam) {
    return null;
  }

}
