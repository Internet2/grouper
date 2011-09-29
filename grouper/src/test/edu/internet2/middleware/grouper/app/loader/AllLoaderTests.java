/*
 * @author mchyzer
 * $Id: AllLoaderTests.java,v 1.2 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.app.loader.db.AllLoaderDbTests;
import edu.internet2.middleware.grouper.app.loader.ldap.AllLoaderLdapTests;

/**
 *
 */
public class AllLoaderTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.app.loader");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperLoaderTest.class);
    suite.addTestSuite(GrouperLoaderSecurityTest.class);
    //$JUnit-END$
    suite.addTest(AllLoaderDbTests.suite());
    suite.addTest(AllLoaderLdapTests.suite());
    return suite;
  }

}
