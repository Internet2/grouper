/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllRulesTests {

  /**
   * suite
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.rules");
    //$JUnit-BEGIN$
    suite.addTestSuite(RuleDefinitionTest.class);
    //$JUnit-END$
    return suite;
  }

}
