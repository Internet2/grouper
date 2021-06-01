package edu.internet2.middleware.grouper.app.duo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllDuoProvisionerTests extends TestCase {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllDuoProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperDuoProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
