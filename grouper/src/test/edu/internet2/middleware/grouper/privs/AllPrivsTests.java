/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
