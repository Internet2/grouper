/*
 * @author mchyzer
 * $Id: AllHibernateTests.java,v 1.1.2.1 2009-03-27 21:23:25 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hibernate;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;

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
