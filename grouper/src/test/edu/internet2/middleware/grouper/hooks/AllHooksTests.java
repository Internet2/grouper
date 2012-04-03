/*
 * @author mchyzer $Id: AllHooksTests.java,v 1.14 2009-11-05 06:10:51 mchyzer Exp $
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
    suite.addTestSuite(ESCOGroupHooksTest.class);
    suite.addTestSuite(GroupTypeTupleHooksTest.class);
    suite.addTestSuite(GroupHooksTest.class);
    suite.addTestSuite(GroupHookUniqueTest.class);
    suite.addTestSuite(GroupHooksAddTypePostCommitTest.class);
    suite.addTestSuite(GroupHooksAddTypeTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(AttributeHooksTest.class);
    suite.addTestSuite(AttributeAssignHooksTest.class);
    suite.addTestSuite(FieldHooksTest.class);
    suite.addTestSuite(MemberHooksTest.class);
    suite.addTestSuite(GroupHooksDbVersionTest.class);
    suite.addTestSuite(GroupTypeHooksTest.class);
    suite.addTestSuite(LifecycleHooksTest.class);
    suite.addTestSuite(AttributeDefNameHooksTest.class);
    //$JUnit-END$
    suite.addTest(AllHooksExamplesTests.suite());
    return suite;
  }

}
