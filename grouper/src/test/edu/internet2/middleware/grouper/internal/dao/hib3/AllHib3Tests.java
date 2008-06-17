/*
 * @author mchyzer
 * $Id: AllHib3Tests.java,v 1.1.2.1 2008-06-17 17:00:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllHib3Tests {

  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.internal.dao.hib3");
    //$JUnit-BEGIN$
    suite.addTestSuite(Hib3GroupDAOTest.class);
    //$JUnit-END$
    return suite;
  }

}
