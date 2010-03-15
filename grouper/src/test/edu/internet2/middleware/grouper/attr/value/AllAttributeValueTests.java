/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.attr.value;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllAttributeValueTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.attr.value");
    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeAssignValueTest.class);
    //$JUnit-END$
    return suite;
  }

}
