/**
 * @author mchyzer
 * $Id: AllTests.java,v 1.1 2009-12-21 06:15:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouperKimConnector.group.AllGroupTests;
import edu.internet2.middleware.grouperKimConnector.groupUpdate.AllGroupUpdateTests;
import edu.internet2.middleware.grouperKimConnector.identity.AllIdentityTests;
import edu.internet2.middleware.grouperKimConnector.util.AllKimUtilsTests;


/**
 *
 */
public class AllTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.group");
    //$JUnit-BEGIN$
    //$JUnit-END$
    suite.addTest(AllGroupTests.suite());
    suite.addTest(AllGroupUpdateTests.suite());
    suite.addTest(AllIdentityTests.suite());
    suite.addTest(AllKimUtilsTests.suite());
    return suite;
  }

}
