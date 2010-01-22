/*
 * @author mchyzer
 * $Id: AllWsUtilTests.java,v 1.3 2008-03-29 10:50:45 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllWsUtilTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.ws.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperServiceUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
