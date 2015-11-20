/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import edu.internet2.middleware.grouper.helper.GrouperTest;
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
