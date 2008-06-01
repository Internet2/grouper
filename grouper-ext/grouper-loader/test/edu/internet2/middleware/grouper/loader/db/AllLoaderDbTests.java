/*
 * @author mchyzer
 * $Id: AllLoaderDbTests.java,v 1.1 2008-06-01 21:27:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.db;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * test suite
 */
public class AllLoaderDbTests {

  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.loader.db");
    //$JUnit-BEGIN$
    suite.addTestSuite(Hib3GrouploaderLogTest.class);
    suite.addTestSuite(Hib3GrouperDdlTest.class);
    //$JUnit-END$
    return suite;
  }

}
