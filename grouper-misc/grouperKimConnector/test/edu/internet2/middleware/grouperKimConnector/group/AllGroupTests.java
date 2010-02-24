/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.group;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGroupTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.group");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperKimGroupServiceImplTest.class);
    //$JUnit-END$
    return suite;
  }

}
