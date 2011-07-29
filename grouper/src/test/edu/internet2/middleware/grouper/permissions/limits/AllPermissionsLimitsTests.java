package edu.internet2.middleware.grouper.permissions.limits;

import edu.internet2.middleware.grouper.permissions.limits.impl.AllPermissionsLimitsImplTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllPermissionsLimitsTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    //$JUnit-BEGIN$
    suite.addTestSuite(PermissionLimitTest.class);
    //$JUnit-END$
    suite.addTest(AllPermissionsLimitsImplTests.suite());
    return suite;
  }

}
