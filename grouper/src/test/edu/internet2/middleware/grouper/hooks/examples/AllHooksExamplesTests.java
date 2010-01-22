/*
 * @author mchyzer
 * $Id: AllHooksExamplesTests.java,v 1.1 2009-03-21 19:48:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllHooksExamplesTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hooks.examples");
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupAttributeNameValidationHookTest.class);
    //$JUnit-END$
    return suite;
  }

}
