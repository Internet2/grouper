package edu.internet2.middleware.grouper.app.midPointProvisioning;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllMidPointProvisioningTests {
  
  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.midPointProvisioning");
    //$JUnit-BEGIN$
    suite.addTestSuite(MidPointProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
