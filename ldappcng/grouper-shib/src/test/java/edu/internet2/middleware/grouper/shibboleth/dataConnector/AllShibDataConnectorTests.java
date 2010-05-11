/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * All Shibboleth Data Connector Tests
 */
public class AllShibDataConnectorTests {

  public static void main(String[] args) {
    TestRunner.run(AllShibDataConnectorTests.suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.shibboleth.dataConnector");
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupDataConnectorTests.class);
    suite.addTestSuite(StemDataConnectorTests.class);
    suite.addTestSuite(MemberDataConnectorTests.class);
    //$JUnit-END$
    return suite;
  }

}
