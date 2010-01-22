/*
 * @author mchyzer
 * $Id: AllHibernateTests.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
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
    //$JUnit-END$
    return suite;
  }
  
}
