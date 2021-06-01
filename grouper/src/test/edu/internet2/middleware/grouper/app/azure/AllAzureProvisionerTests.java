package edu.internet2.middleware.grouper.app.azure;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllAzureProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllAzureProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperAzureProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
