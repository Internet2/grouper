/*
 * @author mchyzer
 * $Id: AllUsduTests.java,v 1.1 2008-07-21 18:47:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.usdu;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * usdu tests
 */
public class AllUsduTests {

  /**
   * suite
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.usdu");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestUSDU.class);
    //$JUnit-END$
    return suite;
  }

}
