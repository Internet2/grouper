package edu.internet2.middleware.grouper.app.freshServiceRequester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllFreshRequesterProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllFreshRequesterProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(FreshRequesterProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
