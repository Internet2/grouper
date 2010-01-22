/*
 * @author mchyzer
 * $Id: AllGcUtilTests.java,v 1.1 2008-11-30 10:57:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.util;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGcUtilTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperClient.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperClientUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
