package edu.internet2.middleware.grouper.app.ccure;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test suite for the CCure provisioner.
 */
public class AllCCureProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllCCureProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(CCureProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
