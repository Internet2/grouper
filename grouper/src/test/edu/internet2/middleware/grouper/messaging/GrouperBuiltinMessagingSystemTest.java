/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
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
//    TestRunner.run(new GrouperBuiltinMessagingSystemTest("testSend"));

    
    //Class clazz = Class.forName("org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
    
    
//    Runtime.getRuntime().addShutdownHook(new Thread() {
//        public void run() {
//            Hib3DAO.getSessionFactory().close();
//            System.out.println("session factory closed");
//        }
//    });
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    GroupFinder.findByName(grouperSession, "abc", false);
    
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
    GrouperBuiltinMessagingSystem.createQueue("abc");
    assertFalse(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));
    GrouperBuiltinMessagingSystem.allowReceiveFromQueue("abc", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToReceiveFromQueue("abc", SubjectTestHelper.SUBJ0));

    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0));
    GrouperBuiltinMessagingSystem.allowSendToQueue("abc", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToQueue("abc", SubjectTestHelper.SUBJ0));

    try {
      assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));
    } catch (Exception e) {
      //queue doesnt exist
      assertTrue(GrouperUtil.getFullStackTrace(e).toLowerCase().contains("topic doesnt exist"));
    }

    GrouperBuiltinMessagingSystem.createTopic("def");
    
    assertFalse(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));
    GrouperBuiltinMessagingSystem.allowSendToTopic("def", SubjectTestHelper.SUBJ0);
    assertTrue(GrouperBuiltinMessagingSystem.allowedToSendToTopic("def", SubjectTestHelper.SUBJ0));

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
        new GrouperMessageSendParam().assignGropuerMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
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
//                new GrouperMessageSendParam().assignGropuerMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME));
//          }
//        }
//        
//      };
//    }
    
  }

}
