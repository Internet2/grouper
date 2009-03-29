/*
 * @author mchyzer
 * $Id: AllInternalDaoTests.java,v 1.1.2.1 2009-03-29 03:56:38 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllInternalDaoTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.internal.dao");
    //$JUnit-BEGIN$
    suite.addTestSuite(QuerySortTest.class);
    suite.addTestSuite(QueryPagingTest.class);
    //$JUnit-END$
    return suite;
  }

}
