/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;

import org.apache.commons.logging.Log;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem.GrouperBuiltinMessageState;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class GrouperBuiltinMessagingSystemTest extends GrouperTest {

  /**
   * log
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperBuiltinMessagingSystemTest.class);

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new GrouperBuiltinMessagingSystemTest("testCleanMessages"));
    //TestRunner.run(new GrouperBuiltinMessagingSystemTest("testMessageSecurity"));
    
    
    //Class clazz = Class.forName("org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
    
       
  }
  
  
  
  /**
   * 
   */
  public GrouperBuiltinMessagingSystemTest() {
    super();
    
  }



  /**
   * @param name
   */
  public GrouperBuiltinMessagingSystemTest(String name) {
    super(name);
    
  }

  /**
   * 
   */
  public void testMessageSecurity() {

    try {
      assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));
    } catch (Exception e) {
      //queue doesnt exist
      assertTrue(GrouperUtil.getFullStackTrace(e).toLowerCase().contains("queue doesnt exist"));
    }
    try {
      GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    } catch (Exception e) {
      //queue doesnt exist
      assertTrue(GrouperUtil.getFullStackTrace(e).toLowerCase().contains("queue doesnt exist"));
    }
    assertTrue(GrouperBuiltinMessagingSystem.createQueue("abc"));
    assertFalse(GrouperBuiltinMessagingSystem.createQueue("abc"));
    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));
    assertFalse(GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));

    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0));
    assertFalse(GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0));

    try {
      assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));
    } catch (Exception e) {
      //queue doesnt exist
      assertTrue(GrouperUtil.getFullStackTrace(e).toLowerCase().contains("topic doesnt exist"));
    }

    assertTrue(GrouperBuiltinMessagingSystem.createTopic("def"));
    assertFalse(GrouperBuiltinMessagingSystem.createTopic("def"));
    assertTrue(GrouperBuiltinMessagingSystem.topicAddSendToQueue("def", "abc"));
    assertFalse(GrouperBuiltinMessagingSystem.topicAddSendToQueue("def", "abc"));

    Collection<String> queues = GrouperBuiltinMessagingSystem.queuesTopicSendsTo("def");
    assertEquals(1, queues.size());
    assertEquals("abc", queues.iterator().next());

    assertTrue(GrouperBuiltinMessagingSystem.topicRemoveSendToQueue("def", "abc"));
    assertFalse(GrouperBuiltinMessagingSystem.topicRemoveSendToQueue("def", "abc"));

    queues = GrouperBuiltinMessagingSystem.queuesTopicSendsTo("def");
    assertEquals(0, queues.size());

    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowSendToTopic("def", SubjectTestHelper.SUBJ0));
    assertFalse(GrouperBuiltinMessagingSystem.allowSendToTopic("def", SubjectTestHelper.SUBJ0));
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));

  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)}.
   */
  public void testCleanMessages() {

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.builtin.messaging.deleteAllMessagesMoreThanHoursOld", "72");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.builtin.messaging.deleteProcessedMessagesMoreThanMinutesOld", "180");
    
    GrouperBuiltinMessagingSystem.createQueue("abc");
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0);

    GrouperBuiltinMessagingSystem.createQueue("dlq");
    GrouperBuiltinMessagingSystem.allowSendToQueue("dlq", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("dlq", SubjectTestHelper.SUBJ0);
    
//    grouperSession = GrouperSession.startRootSession();
//    subject = SubjectFinder.findById("GrouperSystem", true); 
//    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", subject);

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
        
    @SuppressWarnings("unused")
    GrouperMessageSendResult grouperMessageSendResult = GrouperMessagingEngine.send(
        new GrouperMessageSendParam()
          .assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName("abc").addMessageBody("message body"));

    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GrouperMessageHibernate grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);
    
    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(0 ,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);

    GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    GrouperMessage grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertEquals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState());

    //this makes it old enough to delete processed but its not processed yet
    grouperMessageHibernate.setSentTimeMicros(grouperMessageHibernate.getSentTimeMicros() - (200 * 60  * 1000 * 1000));
    grouperMessageHibernate.saveOrUpdate();

    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(0 ,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());

    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);

    //lets mark as processed
    @SuppressWarnings("unused")
    GrouperMessageAcknowledgeResult grouperMessageProcessedResult = GrouperMessagingEngine.acknowledge(
        new GrouperMessageAcknowledgeParam().assignAcknowledgeType(
            GrouperMessageAcknowledgeType.mark_as_processed).assignQueueName("abc").addGrouperMessage(grouperMessage));
    
    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(0 ,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());

    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);

    //lets make it old enough to be deleted
    grouperMessageHibernate.setGetTimeMillis(grouperMessageHibernate.getGetTimeMillis() - (200 * 60  * 1000));
    grouperMessageHibernate.saveOrUpdate();

    assertEquals(1, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(0 ,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());

    assertEquals(0, GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).size());
    
    //make another message which is just old
    grouperMessageSendResult = GrouperMessagingEngine.send(
        new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName("abc").addMessageBody("message body"));

    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();

    grouperMessageHibernate.setSentTimeMicros(grouperMessageHibernate.getSentTimeMicros() - (73L * 60 * 60L * 1000 * 1000L));
    
    grouperMessageHibernate.saveOrUpdate();

    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(1,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());

    assertEquals(0, GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).size());
    
    
    //send a message
    grouperMessageSendResult = GrouperMessagingEngine.send(
        new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName("abc").addMessageBody("message body"));
    
    //receive it
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertEquals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState());
    
    //receive it
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(0, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    //put it back on the queue
    grouperMessageProcessedResult = GrouperMessagingEngine.acknowledge(
        new GrouperMessageAcknowledgeParam().assignAcknowledgeType(
            GrouperMessageAcknowledgeType.return_to_queue).assignQueueName("abc").addGrouperMessage(grouperMessage));

    //receive it
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertEquals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState());

    grouperMessageProcessedResult = GrouperMessagingEngine.acknowledge(
        new GrouperMessageAcknowledgeParam().assignAcknowledgeType(
            GrouperMessageAcknowledgeType.mark_as_processed).assignQueueName("abc").addGrouperMessage(grouperMessage));

    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(0, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));

    //## test dlq
    
    //send a message
    grouperMessageSendResult = GrouperMessagingEngine.send(
        new GrouperMessageSendParam().assignQueueType(GrouperMessageQueueType.queue)
          .assignQueueOrTopicName("abc").addMessageBody("message body"));
    
    //receive it
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));
    
    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();
    
    //put it back on the queue
    grouperMessageProcessedResult = GrouperMessagingEngine.acknowledge(
        new GrouperMessageAcknowledgeParam().assignAcknowledgeType(
            GrouperMessageAcknowledgeType.send_to_another_queue).assignQueueName("abc")
            .assignAnotherQueueParam(new GrouperMessageQueueParam().assignQueueOrTopicName("dlq")
                .assignQueueType(GrouperMessageQueueType.queue))
            .addGrouperMessage(grouperMessage));

    //receive it
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("abc"));

    assertEquals(0, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));
    
    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("dlq"));

    assertEquals(1, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));

    grouperMessage = grouperMessageReceiveResult.getGrouperMessages().iterator().next();
    
    grouperMessageHibernate = (GrouperMessageHibernate)grouperMessage;
    
    assertEquals(GrouperBuiltinMessageState.GET_ATTEMPTED.name(), grouperMessageHibernate.getState());

    grouperMessageProcessedResult = GrouperMessagingEngine.acknowledge(
        new GrouperMessageAcknowledgeParam().assignAcknowledgeType(
            GrouperMessageAcknowledgeType.mark_as_processed).assignQueueName("dlq").addGrouperMessage(grouperMessage));

    grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueueName("dlq"));

    assertEquals(0, GrouperUtil.length(grouperMessageReceiveResult.getGrouperMessages()));

  }


  /**
   * Test method for {@link edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)}.
   */
  public void testSend() {

    final int NUM_OF_SENDERS = 5;
    final int NUM_OF_RECEIVERS = 6;
    final int NUM_OF_MESSAGES = 1000;

    final boolean[] hasError = new boolean[]{false};
    
    GrouperBuiltinMessagingSystem.createQueue("queue0");
    GrouperBuiltinMessagingSystem.createQueue("queue1");
    GrouperBuiltinMessagingSystem.createQueue("queue2");
    GrouperBuiltinMessagingSystem.createQueue("queue3");
    GrouperBuiltinMessagingSystem.createTopic("topic3");
    GrouperBuiltinMessagingSystem.topicAddSendToQueue("topic3", "queue3");
    GrouperBuiltinMessagingSystem.createQueue("queue4");
    GrouperBuiltinMessagingSystem.createQueue("queue4a");
    GrouperBuiltinMessagingSystem.createTopic("topic4");
    GrouperBuiltinMessagingSystem.topicAddSendToQueue("topic4", "queue4");
    GrouperBuiltinMessagingSystem.topicAddSendToQueue("topic4", "queue4a");

    GrouperBuiltinMessagingSystem.allowSendToQueue("queue0", SubjectTestHelper.SUBJ0);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue0", SubjectTestHelper.SUBJ1);
    
    GrouperBuiltinMessagingSystem.allowSendToQueue("queue1", SubjectTestHelper.SUBJ2);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue1", SubjectTestHelper.SUBJ3);
    
    GrouperBuiltinMessagingSystem.allowSendToQueue("queue2", SubjectTestHelper.SUBJ4);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue2", SubjectTestHelper.SUBJ5);
    
    GrouperBuiltinMessagingSystem.allowSendToTopic("topic3", SubjectTestHelper.SUBJ6);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue3", SubjectTestHelper.SUBJ7);
    
    GrouperBuiltinMessagingSystem.allowSendToTopic("topic4", SubjectTestHelper.SUBJ8);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue4", SubjectTestHelper.SUBJ9);
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue4a", SubjectTestHelper.SUBJ2);

    Thread[] senderThreads = new Thread[NUM_OF_SENDERS];

    //threads
    for (int i=0;i<NUM_OF_SENDERS;i++) {

      final int I = i;

      Runnable runnable = new Runnable() {

        public void run() {

          boolean isQueue = I <= 2;
          String queueName = null;          
          String topicName = null;          
          if (isQueue) {
            
            queueName = "queue" + I;
            
          } else {

            topicName = "topic" + I;

          }
          GrouperSession grouperSession = GrouperSession.startRootSession();
          Subject subject = SubjectFinder.findById("test.subject." + (I * 2), true);
          GrouperSession.stopQuietly(grouperSession);
          grouperSession = GrouperSession.start(subject);
          
          if (sendMessages(isQueue ? queueName : topicName, 
              isQueue ? GrouperMessageQueueType.queue : GrouperMessageQueueType.topic, NUM_OF_MESSAGES, 
                  "messageBody_" + I + "_")) {
            hasError[0] = true;
          }
          
          GrouperSession.stopQuietly(grouperSession);
          
        }

      };
      senderThreads[i] = new Thread(runnable);
      senderThreads[i].start();
    }

    Thread[] receiverThreads = new Thread[NUM_OF_RECEIVERS];

    //threads
    for (int i=0;i<NUM_OF_RECEIVERS;i++) {

      final int I = i;

      Runnable runnable = new Runnable() {

        public void run() {

          String queueName = "queue" + I;
          if (I >= 5) {
            queueName = "queue4a";
          }
          
          int queueIndex = I;
          if (I >= 5) {
            queueIndex = 4;
          }
          
          String subjectId = "test.subject." + (I * 2 + 1);
          
          if (I >= 5) {
            subjectId = SubjectTestHelper.SUBJ2.getId();
          }

          GrouperSession grouperSession = GrouperSession.startRootSession();
          Subject subject = SubjectFinder.findById(subjectId, true);
          GrouperSession.stopQuietly(grouperSession);
          grouperSession = GrouperSession.start(subject);

          if (receiveMessages(queueName, NUM_OF_MESSAGES, "messageBody_" + queueIndex + "_")) {
            hasError[0] = true;
          }
          
          GrouperSession.stopQuietly(grouperSession);
          
        }

      };
      receiverThreads[i] = new Thread(runnable);
      receiverThreads[i].start();
    }

    for (int i=0;i<senderThreads.length;i++) {
      
      GrouperUtil.threadJoin(senderThreads[i]);
      LOG.debug("sender " + i + " done");
    
    }
    
    for (int i=0;i<receiverThreads.length;i++) {
      
      GrouperUtil.threadJoin(receiverThreads[i]);
      LOG.debug("receiver " + i + " done");

    }

    if (hasError[0]) {
      assertTrue("Thread had error, see output", false);
    }
  }

  /**
   * 
   * @param queueOrTopicName
   * @param numberOfMessages
   * @param grouperMessageQueueType
   * @param messageBodyPrefix
   * @return true if error
   */
  private static boolean sendMessages(String queueOrTopicName, GrouperMessageQueueType grouperMessageQueueType, 
      int numberOfMessages, String messageBodyPrefix) {
    
    try {
      //messages
      for (int j=0;j<numberOfMessages;j++) {

        GrouperMessagingEngine.send(
            new GrouperMessageSendParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
              .assignQueueType(grouperMessageQueueType)
              .assignQueueOrTopicName(queueOrTopicName)
              .addMessageBody(messageBodyPrefix + j));
        
        if ((j + 1) % 100 == 0) {
          LOG.debug("Sender " + GrouperUtil.subjectToString(GrouperSession.staticGrouperSession().getSubject()) + ", message: " + (j+1));
        }
        
      }
    } catch (RuntimeException re) {
      LOG.error("Error in thread: " + Thread.currentThread().getName(), re);
      re.printStackTrace();
      assertTrue(false);
      return true;
    }
    return false;
  }

  
  /**
   * 
   * @param queueName
   * @param numberOfMessages
   * @param messageBodyPrefix
   * @return true if error
   */
  private static boolean receiveMessages(String queueName,
      int numberOfMessages, String messageBodyPrefix) {

    int receivedIndex = 0;
    
    try {
      //messages
      for (int j=0;j<numberOfMessages * 2;j++) {

        
        GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(
            new GrouperMessageReceiveParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
              .assignQueueName(queueName));

        Collection<GrouperMessage> grouperMessages = grouperMessageReceiveResult.getGrouperMessages();

        for (GrouperMessage grouperMessage : GrouperUtil.nonNull(grouperMessages)) {

          assertEquals(messageBodyPrefix + receivedIndex, grouperMessage.getMessageBody());
          receivedIndex++;

          if ((receivedIndex + 1) % 100 == 0) {
            LOG.debug("Receiver " + GrouperUtil.subjectToString(GrouperSession.staticGrouperSession().getSubject()) + ", message: " + (receivedIndex+1)
                + ", receiveCalls: " + j);
          }

          GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam()
            .assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
            .assignQueueName(queueName).assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed)
            .addGrouperMessage(grouperMessage));

        }

        if (receivedIndex == numberOfMessages && GrouperUtil.length(grouperMessages) == 0) {
          LOG.debug("Receiver " + GrouperUtil.subjectToString(GrouperSession.staticGrouperSession().getSubject()) + " done");
          break;
        }
        
      }
      
      if (receivedIndex != numberOfMessages) {
        throw new RuntimeException("Did not receive enough messages: " + receivedIndex + ", not " + numberOfMessages);
      }

    } catch (RuntimeException re) {
      LOG.error("Error in thread: " + Thread.currentThread().getName(), re);
      re.printStackTrace();
      assertTrue(false);
      return true;
    }
    return false;
  }
  
  /**
   * Test method for {@link edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)}.
   */
  @SuppressWarnings("unused")
  public void testPerformance() {
  
    final int NUM_OF_THREADS = 10;
    final int NUM_OF_MESSAGES = 1000;
  
    final boolean[] hasError = new boolean[]{false};

    for (int i=0; i==0 || i<NUM_OF_THREADS;i++) {
      GrouperBuiltinMessagingSystem.createQueue("queue" + i);
      GrouperBuiltinMessagingSystem.allowSendToQueue("queue" + i, SubjectTestHelper.SUBJ0);
      GrouperBuiltinMessagingSystem.allowReceiveFromQueue("queue" + i, SubjectTestHelper.SUBJ0);
    }
    
  
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    //init everything
    assertFalse(sendMessages("queue0", GrouperMessageQueueType.queue, 100, "message_"));
    assertFalse(receiveMessages("queue0", 100, "message_"));

    long startNanos = System.nanoTime();

    assertFalse(sendMessages("queue0", GrouperMessageQueueType.queue, NUM_OF_MESSAGES * NUM_OF_THREADS, "message_"));
    assertFalse(receiveMessages("queue0", NUM_OF_MESSAGES * NUM_OF_THREADS, "message_"));

    String oneThreadReport = "Sent/received/acknowledge " + (NUM_OF_MESSAGES * NUM_OF_THREADS) + " messages in one thread in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms";

    startNanos = System.nanoTime();

    Thread[] threads = new Thread[NUM_OF_THREADS];
    
    for (int i=0;i<NUM_OF_THREADS;i++) {
      final int I = i;
      Runnable runnable = new Runnable() {

        public void run() {
          GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

          String queueName = "queue" + I;
          String messageBodyPrefix = "message_" + I + "_";
          if (sendMessages(queueName, GrouperMessageQueueType.queue, NUM_OF_MESSAGES, messageBodyPrefix)) {
            hasError[0] = true;
          }
          if (receiveMessages(queueName, NUM_OF_MESSAGES, messageBodyPrefix)) {
            hasError[0] = true;
          }

        }
      };
      threads[i] = new Thread(runnable);
      threads[i].start();
    }
    
    for (int i=0;i<threads.length;i++) {
      GrouperUtil.threadJoin(threads[i]);
    }
    
    LOG.debug(oneThreadReport);
    LOG.debug("Sent/received/acknowledge " + (NUM_OF_MESSAGES * NUM_OF_THREADS) + " messages in " + NUM_OF_THREADS + " threads in " + ((System.nanoTime() - startNanos) / 1000000L) + "ms");

    GrouperSession.stopQuietly(grouperSession);
    
    assertFalse(hasError[0]);
    
  }

}
