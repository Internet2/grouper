/*
 * @author mchyzer
 * $Id: AllHooksTests.java,v 1.5 2008-07-10 00:46:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllHooksTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllHooksTests.suite());
  }
  

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.hooks");
    //$JUnit-BEGIN$
    suite.addTestSuite(FieldHooksTest.class);
    suite.addTestSuite(GroupHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(GrouperSessionHooksTest.class);
    suite.addTestSuite(GroupTypeHooksTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(MemberHooksTest.class);
    suite.addTestSuite(GroupTypeTupleHooksTest.class);
    //$JUnit-END$
    return suite;
  }

}
