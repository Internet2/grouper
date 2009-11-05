/**
 * @author mchyzer $Id: AllAttrAssignTests.java,v 1.2 2009-11-05 06:10:51 mchyzer Exp $
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
    suite.addTestSuite(AttributeAssignActionSetTest.class);
    //$JUnit-END$
    return suite;
  }

}
