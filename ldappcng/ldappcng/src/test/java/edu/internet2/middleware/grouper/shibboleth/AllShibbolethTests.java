package edu.internet2.middleware.grouper.shibboleth;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.AllShibDataConnectorTests;

/**
 * All shibboleth attribute resolver tests
 */
public class AllShibbolethTests {

  public static void main(String[] args) {
    TestRunner.run(AllShibbolethTests.suite());
  }
  
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
