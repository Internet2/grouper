/*
 * @author mchyzer
 * $Id: AllGshTests.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.gsh;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGshTests {

  /**
   * suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.gsh");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestGsh.class);
    suite.addTestSuite(Test_Unit.class);
    //$JUnit-END$
    return suite;
  }

}
