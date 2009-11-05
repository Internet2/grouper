/**
 * @author mchyzer
 * $Id: AllLdappcSpmlRequestTests.java,v 1.1 2009-11-05 06:10:50 mchyzer Exp $
 */
package edu.internet2.middleware.ldappc.spml.request;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllLdappcSpmlRequestTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.ldappc.spml.request");
    //$JUnit-BEGIN$
    suite.addTestSuite(RequestTests.class);
    //$JUnit-END$
    return suite;
  }

}
