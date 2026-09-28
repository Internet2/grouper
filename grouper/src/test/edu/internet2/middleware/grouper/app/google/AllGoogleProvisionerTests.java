package edu.internet2.middleware.grouper.app.google;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllGoogleProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllGoogleProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperGoogleProvisionerTest.class);
    suite.addTestSuite(GrouperGoogleProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
