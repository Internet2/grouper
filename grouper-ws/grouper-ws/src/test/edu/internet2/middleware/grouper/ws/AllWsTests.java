/*
 * @author mchyzer
 * $Id: AllWsTests.java,v 1.1 2008-10-27 21:28:15 mchyzer Exp $
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
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.ws");
    //$JUnit-BEGIN$
    //$JUnit-END$
    suite.addTest(AllGrouperPrivilegeTests.suite());
    suite.addTest(AllRestContentTests.suite());
    suite.addTest(AllWsUtilTests.suite());
    return suite;
  }

}
