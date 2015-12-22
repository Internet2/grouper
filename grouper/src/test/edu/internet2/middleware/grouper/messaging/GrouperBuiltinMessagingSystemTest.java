/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
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
  public static void main(String[] args) {
    TestRunner.run(new GrouperBuiltinMessagingSystemTest("testMessageSecurity"));
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
    
    Thread[] threads = new Thread[NUM_OF_THREADS];
    
    //threads
    for (int i=0;i<NUM_OF_THREADS;i++) {

      Runnable runnable = new Runnable() {

        public void run() {
          //messages
          for (int j=0;j<500;j++) {
            GrouperMessagingEngine.send(
                new GrouperMessageSendParam().assignGropuerMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME));
          }
        }
        
      };
    }
    
  }

}
