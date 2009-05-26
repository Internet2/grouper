/*
 * @author mchyzer
 * $Id: AllChangeLogTests.java,v 1.2 2009-05-26 06:50:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllChangeLogTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.changeLog");
    //$JUnit-BEGIN$
    suite.addTestSuite(ChangeLogTypeTest.class);
    suite.addTestSuite(ChangeLogIdTest.class);
    suite.addTestSuite(ChangeLogTest.class);
    //$JUnit-END$
    return suite;
  }

}
