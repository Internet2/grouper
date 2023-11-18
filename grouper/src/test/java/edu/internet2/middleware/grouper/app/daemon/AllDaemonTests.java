package edu.internet2.middleware.grouper.app.daemon;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllDaemonTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllDaemonTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperDaemonConfigurationTest.class);
    //$JUnit-END$
    return suite;
  }

}
