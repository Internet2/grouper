/*
 * @author mchyzer
 * $Id: AllHooksTests.java,v 1.9.2.2 2009-02-13 20:54:18 mchyzer Exp $
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
    @SuppressWarnings("unused")
    long start = System.currentTimeMillis();
    TestRunner.run(AllHooksTests.suite());
    //System.err.println("Took: " + (System.currentTimeMillis() - start));
  }

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hooks");
    //$JUnit-BEGIN$
    suite.addTestSuite(MemberHooksTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(AttributeHooksTest.class);
    suite.addTestSuite(FieldHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(GroupTypeHooksTest.class);
    suite.addTestSuite(GroupHooksAddTypePostCommitTest.class);
    suite.addTestSuite(GroupTypeTupleHooksTest.class);
    suite.addTestSuite(GroupHooksAddTypeTest.class);
    suite.addTestSuite(GroupHooksDbVersionTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(GroupHooksTest.class);
    suite.addTestSuite(LifecycleHooksTest.class);
    //$JUnit-END$
    return suite;
  }

}
