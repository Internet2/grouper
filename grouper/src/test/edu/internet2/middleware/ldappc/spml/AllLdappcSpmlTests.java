/**
 * @author mchyzer
 * $Id: AllLdappcSpmlTests.java,v 1.1 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.ldappc.spml;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.ldappc.spml.config.AllLdappcSpmlConfigTests;
import edu.internet2.middleware.ldappc.spml.request.AllLdappcSpmlRequestTests;


/**
 *
 */
public class AllLdappcSpmlTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.ldappc.spml");
    //$JUnit-BEGIN$
    // suite.addTestSuite(PSPMultipleLdapTest.class);
    suite.addTest(AllLdappcSpmlConfigTests.suite());
    suite.addTest(AllLdappcSpmlRequestTests.suite());
    suite.addTestSuite(PSPOptionsTest.class);
    suite.addTestSuite(PSPTest.class);
    suite.addTestSuite(PSPLdapTest.class);
    suite.addTestSuite(PSPLdapNotADTest.class);
    //$JUnit-END$
    
    return suite;
  }

}
