/*
 * @author mchyzer $Id: AllMiscTests.java,v 1.3 2009-09-02 05:57:26 mchyzer Exp $
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
    //$JUnit-END$
    return suite;
  }

}
