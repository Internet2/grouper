/*
 * @author mchyzer
 * $Id: AllDTOTests.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dto;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllDTOTests {

  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.internal.dao.hib3");
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupDTOTest.class);
    //$JUnit-END$
    return suite;
  }

}
