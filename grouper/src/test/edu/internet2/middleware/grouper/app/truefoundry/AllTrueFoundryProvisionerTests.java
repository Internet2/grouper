package edu.internet2.middleware.grouper.app.truefoundry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTrueFoundryProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllTrueFoundryProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(TrueFoundryProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
