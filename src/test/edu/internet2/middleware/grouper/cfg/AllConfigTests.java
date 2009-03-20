/*
 * @author mchyzer
 * $Id: AllConfigTests.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cfg;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllConfigTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.cfg");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_dao_hibernate_HibernateDaoConfig.class);
    suite.addTestSuite(Test_cfg_ApiConfig.class);
    suite.addTestSuite(Test_api_GrouperConfig.class);
    suite.addTestSuite(Test_cfg_ConfigurationHelper.class);
    suite.addTestSuite(Test_cfg_PropertiesConfiguration.class);
    //$JUnit-END$
    return suite;
  }

}
