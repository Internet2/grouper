package edu.internet2.middleware.grouper.app.externalSystem;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllExternalSystemTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllExternalSystemTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperExternalSystemTest.class);
    //$JUnit-END$
    return suite;
  }

}
