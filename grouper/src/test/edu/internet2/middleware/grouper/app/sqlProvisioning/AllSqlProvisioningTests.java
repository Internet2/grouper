package edu.internet2.middleware.grouper.app.sqlProvisioning;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllSqlProvisioningTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSqlProvisioningTests.class.getName());
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.sqlProvisioning", false)) {
    //$JUnit-BEGIN$
    suite.addTestSuite(SqlProvisionerTest.class);
    suite.addTestSuite(SqlProvisioningStartWithTest.class);
    //$JUnit-END$
    }
    return suite;
  }

}
