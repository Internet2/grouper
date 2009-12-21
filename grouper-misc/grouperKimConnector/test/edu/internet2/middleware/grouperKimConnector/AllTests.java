/**
 * @author mchyzer
 * $Id: AllTests.java,v 1.1 2009-12-21 06:15:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouperKimConnector.group.GrouperKimGroupServiceImplTest;
import edu.internet2.middleware.grouperKimConnector.groupUpdate.GrouperKimGroupUpdateServiceImplTest;
import edu.internet2.middleware.grouperKimConnector.util.GrouperKimUtilsTest;


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
    suite.addTestSuite(GrouperKimGroupServiceImplTest.class);
    suite.addTestSuite(GrouperKimGroupUpdateServiceImplTest.class);
    suite.addTestSuite(GrouperKimUtilsTest.class);
    return suite;
  }

}
