package edu.internet2.middleware.grouper.app.interfolio;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite for the Interfolio provisioner.
 */
public class AllInterfolioProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllInterfolioProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(InterfolioProvisionerTest.class);
    suite.addTestSuite(InterfolioProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
