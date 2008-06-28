/*
 * @author mchyzer
 * $Id: AllHooksTests.java,v 1.3 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllHooksTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.hooks");
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(MemberHooksTest.class);
    //$JUnit-END$
    return suite;
  }

}
