/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.groupUpdate;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGroupUpdateTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.groupUpdate");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperKimGroupUpdateServiceImplTest.class);
    //$JUnit-END$
    return suite;
  }

}
