package edu.internet2.middleware.grouper.shibboleth;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.shibboleth.dataConnector.AllShibDataConnectorTests;

/**
 * All shibboleth attribute resolver tests
 */
public class AllShibbolethTests extends TestCase {

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
    suite.addTest(AllShibDataConnectorTests.suite());
    //$JUnit-END$    
    return suite;
  }

}
