package edu.internet2.middleware.grouper.permissions.limits;

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
    return suite;
  }

}
