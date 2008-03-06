/*
 * @author mchyzer
 * $Id: AllUtilTests.java,v 1.1 2008-03-06 19:10:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * test suite for util pkg
 */
public class AllUtilTests {

  /**
   * test suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperUtilTest.class);
    //$JUnit-END$
    return suite;
  }

}
