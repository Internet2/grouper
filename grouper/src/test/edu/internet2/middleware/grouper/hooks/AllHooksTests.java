/*
 * @author mchyzer
 * $Id: AllHooksTests.java,v 1.12 2009-03-21 19:48:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.examples.AllHooksExamplesTests;
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
    suite.addTest(AllHooksExamplesTests.suite());
    return suite;
  }

}
