/*
 * @author mchyzer
 * $Id: AllWsUtilTests.java,v 1.2 2008-03-25 05:15:11 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllWsUtilTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.ws.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperServiceUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
