/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem;


/**
 * built in messaging system from database
 */
public class GrouperBuiltinMessagingSystem implements GrouperMessagingSystem {

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
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#sendMessages(java.util.Collection)
   */
  public void sendMessages(Collection<GrouperMessage> messages) {
    if (messages == null) {
      return;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(true);
    Member fromMember = grouperSession.getMember();
    for (GrouperMessage grouperMessage : messages) {
      GrouperMessageHibernate grouperMessageHibernate = new GrouperMessageHibernate();
      if (StringUtils.isBlank(grouperMessage.getQueueOrTopic())) {
        throw new RuntimeException("queueOrTopic cant be null in a message");
      }
      if (!StringUtils.isBlank(grouperMessage.getFromMemberId())) {
        throw new RuntimeException("fromMemberId must be null in a message");
      }
      if (!StringUtils.isBlank(grouperMessage.getId())) {
        throw new RuntimeException("id must be null in a message");
      }
      grouperMessageHibernate.setFromMemberId(fromMember.getId());
      grouperMessageHibernate.setId(GrouperUuid.getUuid());
      grouperMessageHibernate.setMessageBody(grouperMessage.getMessageBody());
      grouperMessageHibernate.setQueueName(grouperMessage.getQueueOrTopic());
      grouperMessageHibernate.setSentTimeMicros(messageId());
      grouperMessageHibernate.setState(GrouperBuiltinMessageState.IN_QUEUE.name());
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#messagesAreProcessed(java.util.Collection)
   */
  public void messagesAreProcessed(Collection<GrouperMessage> messages) {
  }

  /**
   * @see edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem#receiveMessages()
   */
  public Collection<GrouperMessage> receiveMessages() {
    return null;
  }

}
