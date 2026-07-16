package edu.internet2.middleware.grouper.app.dropbox;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite aggregator for the Dropbox provisioner tests.
 */
public class AllDropboxProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllDropboxProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(DropboxProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
