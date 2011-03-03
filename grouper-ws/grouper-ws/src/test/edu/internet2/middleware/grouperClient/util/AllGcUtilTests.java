/*
 * @author mchyzer
 * $Id$
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
