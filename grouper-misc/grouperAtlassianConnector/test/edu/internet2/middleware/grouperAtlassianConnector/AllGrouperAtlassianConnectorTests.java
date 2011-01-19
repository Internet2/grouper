/**
 * @author mchyzer $Id: AllGrouperAtlassianConnectorTests.java 7078 2010-12-16 06:20:33Z
 *         mchyzer $
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
    suite.addTestSuite(GrouperCredentialsProviderTest.class);
    suite.addTestSuite(GrouperAccessProviderTest.class);
    suite.addTestSuite(GrouperProfileProviderTest.class);
    //$JUnit-END$
    return suite;
  }

}
