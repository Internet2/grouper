/*
 * @author mchyzer
 * $Id: AllHooksTests.java,v 1.4 2008-06-29 17:42:41 mchyzer Exp $
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
    suite.addTestSuite(FieldHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(GrouperSessionHooksTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(GroupTypeHooksTest.class);
    suite.addTestSuite(MemberHooksTest.class);
    suite.addTestSuite(GroupTypeTupleHooksTest.class);
    //$JUnit-END$
    return suite;
  }

}
