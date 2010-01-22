/*
 * @author mchyzer
 * $Id: AllLoaderDbTests.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

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
