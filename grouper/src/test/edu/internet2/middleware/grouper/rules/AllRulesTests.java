/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.rules;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllRulesTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllRulesTests.suite());
  }
  
  /**
   * suite
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.rules");
    //$JUnit-BEGIN$
    suite.addTestSuite(RuleApiTest.class);
    suite.addTestSuite(RuleTest.class);
    suite.addTestSuite(RuleDefinitionTest.class);
    suite.addTestSuite(RuleHookTest.class);
    suite.addTestSuite(RuleNameChangeTest.class);
    //$JUnit-END$
    return suite;
  }

}
