package edu.internet2.middleware.grouper.app.messagingProvisioning;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMessagingProvisioningTests {
  
  
  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.messagingProvisioning");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperMessagingProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
