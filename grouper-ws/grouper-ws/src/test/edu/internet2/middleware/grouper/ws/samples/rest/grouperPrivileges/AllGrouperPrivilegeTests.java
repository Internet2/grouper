/*
 * @author mchyzer
 * $Id: AllGrouperPrivilegeTests.java,v 1.1 2008-10-27 21:28:14 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllGrouperPrivilegeTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges");
    //$JUnit-BEGIN$
    suite.addTestSuite(WsSampleGetAssignGrouperPrivilegesRestLiteTest.class);
    //$JUnit-END$
    return suite;
  }

}
