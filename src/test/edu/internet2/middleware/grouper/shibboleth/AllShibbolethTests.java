package edu.internet2.middleware.grouper.shibboleth;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.GroupDataConnectorTests;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.MemberDataConnectorTests;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.StemDataConnectorTests;

/**
 * All shibboleth attribute resolver tests
 */
public class AllShibbolethTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.shibboleth");
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupDataConnectorTests.class);
    suite.addTestSuite(MemberDataConnectorTests.class);
    suite.addTestSuite(StemDataConnectorTests.class);
    //$JUnit-END$
    return suite;
  }

}
