/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem.GrouperBuiltinMessageState;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageProcessedParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageProcessedResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;


/**
 *
 */
public class GrouperBuiltinMessagingSystemTest extends GrouperTest {

  /**
   * 
   * @param args
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
      assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0, true));
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
    GrouperBuiltinMessagingSystem.createQueue("abc");
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0, true));
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0, true));

    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0, true));
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0, true));

    try {
      assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0, true));
    } catch (Exception e) {
      //queue doesnt exist
      assertTrue(GrouperUtil.getFullStackTrace(e).toLowerCase().contains("topic doesnt exist"));
    }

    GrouperBuiltinMessagingSystem.createTopic("def");
    GrouperBuiltinMessagingSystem.topicSendsToQueue("def", "abc");
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0, true));
    GrouperBuiltinMessagingSystem.allowSendToTopic("def", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0, true));

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

//    grouperSession = GrouperSession.startRootSession();
//    subject = SubjectFinder.findById("GrouperSystem", true); 
//    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", subject);

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
        
    GrouperMessageSendResult grouperMessageSendResult = GrouperMessagingEngine.send(
        new GrouperMessageSendParam()
          .assignQueueOrTopic("abc").addMessageBody("message body"));

    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    
    GrouperMessageHibernate grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);
    
    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(0 ,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());
    
    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();
    
    assertNotNull(grouperMessageHibernate);

    GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(new GrouperMessageReceiveParam().assignQueue("abc"));
    
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
    GrouperMessageProcessedResult grouperMessageProcessedResult = GrouperMessagingEngine.markAsProcessed(new GrouperMessageProcessedParam().assignQueue("abc").addGrouperMessage(grouperMessage));
    
    
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
        new GrouperMessageSendParam()
          .assignQueueOrTopic("abc").addMessageBody("message body"));

    grouperMessageHibernate = GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).iterator().next();

    grouperMessageHibernate.setSentTimeMicros(grouperMessageHibernate.getSentTimeMicros() - (73L * 60 * 60L * 1000 * 1000L));
    
    grouperMessageHibernate.saveOrUpdate();

    assertEquals(0, GrouperBuiltinMessagingSystem.cleanOldProcessedMessages());
    assertEquals(1,GrouperBuiltinMessagingSystem.cleanOldUnprocessedMessages());

    assertEquals(0, GrouperDAOFactory.getFactory().getMessage().findByFromMemberId(member.getId()).size());
    
    
  }

  
  /**
   * Test method for {@link edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem#send(edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam)}.
   */
  public void testSend() {

    final int NUM_OF_THREADS = 9;
    final int NUM_OF_MESSAGES = 500;

    GrouperBuiltinMessagingSystem.createQueue("abc");
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);

    
    GrouperMessagingEngine.send(
        new GrouperMessageSendParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
          .assignQueueOrTopic("abc").addMessageBody("message body"));

    
    
//    Thread[] threads = new Thread[NUM_OF_THREADS];
//    
//    //threads
//    for (int i=0;i<NUM_OF_THREADS;i++) {
//
//      Runnable runnable = new Runnable() {
//
//        public void run() {
//          //messages
//          for (int j=0;j<500;j++) {
//            GrouperMessagingEngine.send(
//                new GrouperMessageSendParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME));
//          }
//        }
//        
//      };
//    }
    
  }

}
