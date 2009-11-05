/**
 * @author mchyzer
 * $Id: AllShibDataConnectorTests.java,v 1.1 2009-11-05 06:10:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.shibboleth.dataConnector;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllShibDataConnectorTests {

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
