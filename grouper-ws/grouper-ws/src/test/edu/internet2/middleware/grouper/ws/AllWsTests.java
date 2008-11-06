/*
 * @author mchyzer
 * $Id: AllWsTests.java,v 1.2 2008-11-06 21:51:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.ws.rest.contentType.AllRestContentTests;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.AllGrouperPrivilegeTests;
import edu.internet2.middleware.grouper.ws.util.AllWsUtilTests;

/**
 *
 */
public class AllWsTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.ws");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperServiceLogicTest.class);
    //$JUnit-END$
    suite.addTest(AllGrouperPrivilegeTests.suite());
    suite.addTest(AllRestContentTests.suite());
    suite.addTest(AllWsUtilTests.suite());
    return suite;
  }

}
