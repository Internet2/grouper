package edu.internet2.middleware.grouper.app.smartsheet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite aggregator for the Smartsheet provisioner tests.
 */
public class AllSmartsheetProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSmartsheetProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SmartsheetProvisionerTest.class);
    suite.addTestSuite(SmartsheetProvisioningTargetNativeSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
