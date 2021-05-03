package edu.internet2.middleware.grouper.app.sqlProvisioning;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllSqlProvisioningTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSqlProvisioningTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SqlProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
