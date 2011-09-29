/*
 * @author mchyzer
 * $Id: AllLoaderDbTests.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.ldap;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * test suite
 */
public class AllLoaderLdapTests {

  /**
   * test
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.loader.db");
    //$JUnit-BEGIN$
    suite.addTestSuite(LoaderLdapElUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
