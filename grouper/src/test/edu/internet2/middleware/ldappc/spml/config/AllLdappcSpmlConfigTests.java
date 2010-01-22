/**
 * @author mchyzer
 * $Id: AllLdappcSpmlConfigTests.java,v 1.1 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.ldappc.spml.config;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllLdappcSpmlConfigTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.ldappc.spml.config");
    //$JUnit-BEGIN$
    suite.addTestSuite(ConfigBeanDefinitionParserTest.class);
    //$JUnit-END$
    return suite;
  }

}
