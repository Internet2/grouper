/*
 * @author mchyzer
 * $Id: AllLoaderTests.java,v 1.1 2008-11-08 08:15:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllLoaderTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.app.loader");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperLoaderTest.class);
    //$JUnit-END$
    return suite;
  }

}
