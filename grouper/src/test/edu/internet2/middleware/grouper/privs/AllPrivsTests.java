/*
 * @author mchyzer $Id: AllPrivsTests.java,v 1.2 2009-11-05 06:10:51 mchyzer Exp $
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
    suite.addTestSuite(TestAttributeDefPriv.class);
    suite.addTestSuite(TestPrivOPTIN.class);
    suite.addTestSuite(Test_privs_NamingWrapper.class);
    suite.addTestSuite(TestPrivADMIN.class);
    suite.addTestSuite(Test_privs_NamingResolverFactory.class);
    suite.addTestSuite(Test_privs_AccessResolver.class);
    suite.addTestSuite(Test_privs_CachingAccessResolver.class);
    suite.addTestSuite(Test_privs_AccessResolverFactory.class);
    suite.addTestSuite(TestPrivAdmin0.class);
    suite.addTestSuite(TestPrivOPTOUT.class);
    suite.addTestSuite(Test_uc_WheelGroup.class);
    suite.addTestSuite(TestPrivVIEW.class);
    suite.addTestSuite(TestPrivileges.class);
    suite.addTestSuite(TestPrivCREATE.class);
    suite.addTestSuite(Test_privs_NamingResolver.class);
    suite.addTestSuite(Test_privs_AccessWrapper.class);
    suite.addTestSuite(Test_util_ParameterHelper.class);
    suite.addTestSuite(TestPrivREAD.class);
    suite.addTestSuite(Test_uc_NamingPrivs.class);
    suite.addTestSuite(TestPrivSTEM.class);
    suite.addTestSuite(Test_privs_CachingNamingResolver.class);
    suite.addTestSuite(TestPrivUPDATE.class);
    suite.addTestSuite(TestAccessPrivilege.class);
    suite.addTestSuite(TestNamingPrivilege.class);
    //$JUnit-END$
    return suite;
  }

}
