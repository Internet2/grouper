package edu.internet2.middleware.grouper.app.jamf;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite aggregator for the Jamf provisioner tests.
 */
public class AllJamfProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllJamfProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(JamfProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
