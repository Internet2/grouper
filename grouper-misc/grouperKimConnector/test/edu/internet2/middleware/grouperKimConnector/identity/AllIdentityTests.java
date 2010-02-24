/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.identity;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllIdentityTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.identity");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperKimIdentityServiceImplTest.class);
    suite.addTestSuite(GrouperKimEntityNameInfoTest.class);
    //$JUnit-END$
    return suite;
  }

}
