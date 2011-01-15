/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.client;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Encrypt;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;


/**
 *
 */
public class GroupSyncDaemonTest extends GrouperTest {

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    ApiConfig.testConfig.put("configuration.autocreate.system.groups", "false");

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GroupSyncDaemonTest("testSyncGroupPush"));
    System.out.println(Morph.encrypt("ur483mvnc32fh"));
  }
  
  /**
   * 
   */
  public GroupSyncDaemonTest() {
    super();
    
  }

  /**
   * @param name
   */
  public GroupSyncDaemonTest(String name) {
    super(name);
    
  }

  /**
   * note, this isnt a real test since the demo server needs to be setup correctly...
   * test a sync group push
   */
  public void testSyncGroupPush() {
    
  }
  
}
