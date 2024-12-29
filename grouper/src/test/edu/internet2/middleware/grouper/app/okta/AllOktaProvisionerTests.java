package edu.internet2.middleware.grouper.app.okta;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllOktaProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllOktaProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperOktaProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
