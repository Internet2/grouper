/*
 * @author mchyzer
 * $Id: AllMorphStringTests.java,v 1.1 2008-09-13 19:21:09 mchyzer Exp $
 */
package edu.internet2.middleware.morphString;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllMorphStringTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.morphString");
    //$JUnit-BEGIN$
    suite.addTestSuite(MorphTest.class);
    //$JUnit-END$
    return suite;
  }

}
