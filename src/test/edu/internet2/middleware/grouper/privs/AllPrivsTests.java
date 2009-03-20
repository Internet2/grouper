/*
 * @author mchyzer
 * $Id: AllPrivsTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllPrivsTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.privs");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_privs_NamingResolver.class);
    suite.addTestSuite(TestPrivSTEM.class);
    suite.addTestSuite(TestPrivAdmin0.class);
    suite.addTestSuite(Test_privs_AccessWrapper.class);
    suite.addTestSuite(TestAccessPrivilege.class);
    suite.addTestSuite(Test_privs_NamingWrapper.class);
    suite.addTestSuite(TestPrivOPTIN.class);
    suite.addTestSuite(TestPrivOPTOUT.class);
    suite.addTestSuite(TestNamingPrivilege.class);
    suite.addTestSuite(Test_privs_NamingResolverFactory.class);
    suite.addTestSuite(Test_privs_AccessResolver.class);
    suite.addTestSuite(TestPrivCREATE.class);
    suite.addTestSuite(Test_privs_CachingNamingResolver.class);
    suite.addTestSuite(Test_uc_WheelGroup.class);
    suite.addTestSuite(TestPrivADMIN.class);
    suite.addTestSuite(Test_privs_AccessResolverFactory.class);
    suite.addTestSuite(Test_uc_NamingPrivs.class);
    suite.addTestSuite(TestPrivVIEW.class);
    suite.addTestSuite(Test_privs_CachingAccessResolver.class);
    suite.addTestSuite(TestPrivUPDATE.class);
    suite.addTestSuite(TestPrivileges.class);
    suite.addTestSuite(TestPrivREAD.class);
    suite.addTestSuite(Test_util_ParameterHelper.class);
    //$JUnit-END$
    return suite;
  }

}
