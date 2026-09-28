package edu.internet2.middleware.grouper.app.adobe;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllAdobeProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllAdobeProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperAdobeProvisionerTest.class);
    suite.addTestSuite(GrouperAdobeProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
