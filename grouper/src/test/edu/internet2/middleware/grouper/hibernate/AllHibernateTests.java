/*
 * @author mchyzer
 * $Id: AllHibernateTests.java,v 1.3 2009-12-28 06:08:37 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllHibernateTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hibernate");
    //$JUnit-BEGIN$
    suite.addTestSuite(HibernateSessionTest.class);
    suite.addTestSuite(HibUtilsTest.class);
    //$JUnit-END$
    return suite;
  }
  
}
