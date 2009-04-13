/*
 * @author mchyzer
 * $Id: AllInternalDaoTests.java,v 1.2 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.internal.dao.hib3.AllDaoHib3Tests;
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
    suite.addTestSuite(TestGrouperDAOFactory.class);
    suite.addTestSuite(QuerySortTest.class);
    suite.addTestSuite(QueryPagingTest.class);
    //$JUnit-END$
    
    suite.addTest(AllDaoHib3Tests.suite());
    return suite;
  }

}
