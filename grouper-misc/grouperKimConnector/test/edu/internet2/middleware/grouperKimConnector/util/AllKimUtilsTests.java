/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.util;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllKimUtilsTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperKimUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
