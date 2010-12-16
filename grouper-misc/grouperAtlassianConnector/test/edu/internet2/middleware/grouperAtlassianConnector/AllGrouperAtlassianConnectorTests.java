/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGrouperAtlassianConnectorTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperAtlassianConnector");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperAccessProviderTest.class);
    //$JUnit-END$
    return suite;
  }

}
