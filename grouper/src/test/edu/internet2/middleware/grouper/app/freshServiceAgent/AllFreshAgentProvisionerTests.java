package edu.internet2.middleware.grouper.app.freshServiceAgent;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllFreshAgentProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllFreshAgentProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(FreshAgentProvisionerTest.class);
    // native-sync (raw-JSON capture) unit test runs via AllProvisioningTargetNativeSyncTests
    suite.addTestSuite(FreshAgentProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
