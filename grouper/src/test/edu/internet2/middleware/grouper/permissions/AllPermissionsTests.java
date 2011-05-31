package edu.internet2.middleware.grouper.permissions;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllPermissionsTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    //$JUnit-BEGIN$
    suite.addTestSuite(RoleSetTest.class);
    suite.addTestSuite(PermissionEntryTest.class);
    suite.addTestSuite(PermissionHeuristicTest.class);
    //$JUnit-END$
    return suite;
  }

}
