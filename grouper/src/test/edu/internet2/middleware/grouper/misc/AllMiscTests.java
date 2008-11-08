/*
 * @author mchyzer
 * $Id: AllMiscTests.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
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
