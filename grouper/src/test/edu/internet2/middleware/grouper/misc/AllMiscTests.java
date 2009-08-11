/*
 * @author mchyzer $Id: AllMiscTests.java,v 1.2 2009-08-11 20:34:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllMiscTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.misc");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperReportTest.class);
    suite.addTest(AllMiscTests.suite());
    //$JUnit-END$
    return suite;
  }

}
