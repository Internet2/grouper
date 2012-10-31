package edu.internet2.middleware.authzStandardApiServer.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllUtilsTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(AllUtilsTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(StandardApiServerUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
