/*
 * @author mchyzer
 * $Id: AllInternalDaoTests.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllInternalDaoTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.internal.dao");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestGrouperDAOFactory.class);
    //$JUnit-END$
    return suite;
  }

}
