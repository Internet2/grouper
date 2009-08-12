/*
 * @author mchyzer $Id: AllMiscTests.java,v 1.1.2.1 2009-08-12 14:40:01 mchyzer Exp $
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
    suite.addTestSuite(GrouperSessionTest.class);
    //$JUnit-END$
    return suite;
  }

}
