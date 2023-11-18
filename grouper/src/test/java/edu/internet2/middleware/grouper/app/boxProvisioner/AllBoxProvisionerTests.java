package edu.internet2.middleware.grouper.app.boxProvisioner;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllBoxProvisionerTests {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllBoxProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperBoxProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
