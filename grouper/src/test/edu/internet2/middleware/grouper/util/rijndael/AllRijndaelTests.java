/*
 * @author mchyzer
 * $Id: AllRijndaelTests.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util.rijndael;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllRijndaelTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.util.rijndael");
    //$JUnit-BEGIN$
    suite.addTestSuite(MorphTest.class);
    //$JUnit-END$
    return suite;
  }

}
