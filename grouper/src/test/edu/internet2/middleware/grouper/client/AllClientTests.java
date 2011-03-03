/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.client;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 *
 */
public class AllClientTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.client");
    //$JUnit-BEGIN$
    if (GrouperConfig.getPropertyBoolean("junit.test.groupSync", false)) {
      suite.addTestSuite(GroupSyncDaemonTest.class);
    }

    //$JUnit-END$
    return suite;
  }

}
