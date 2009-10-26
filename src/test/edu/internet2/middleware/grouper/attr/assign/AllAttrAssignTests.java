/**
 * @author mchyzer
 * $Id: AllAttrAssignTests.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllAttrAssignTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.attr.assign");
    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeAssignActionTest.class);
    //$JUnit-END$
    return suite;
  }

}
