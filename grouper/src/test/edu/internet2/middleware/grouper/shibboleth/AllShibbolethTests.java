package edu.internet2.middleware.grouper.shibboleth;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.AllShibDataConnectorTests;

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
    //$JUnit-END$
    suite.addTest(AllShibDataConnectorTests.suite());
    return suite;
  }

}
