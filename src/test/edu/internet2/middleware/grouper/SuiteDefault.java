/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.gsh.AllGshTests;
import edu.internet2.middleware.grouper.app.loader.db.AllLoaderDbTests;
import edu.internet2.middleware.grouper.app.usdu.AllUsduTests;
import edu.internet2.middleware.grouper.ddl.AllDdlTests;
import edu.internet2.middleware.grouper.hooks.AllHooksTests;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.util.AllUtilTests;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.rijndael.AllRijndaelTests;

/**
 * Run default tests.
 * @author  blair christensen.
 * @version $Id: SuiteDefault.java,v 1.48 2008-10-20 15:27:31 mchyzer Exp $
 */
public class SuiteDefault extends TestCase {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(SuiteDefault.class);

  /**
   * run tests from bat or sh file.  Note, dont change this method unless you want the 
   * bat or sh test runer to be changed
   * @param args
   */
  public static void main(String[] args) {
    try {
      //dont keep prompting user about DB
      GrouperUtil.stopPromptingUser = true;
      RegistryInitializeSchema.initializeSchemaForTests();
      TestRunner.run(SuiteDefault.suite());
    } catch (RuntimeException re) {
      LOG.error("Error in testing", re);
      throw re;
    }
  }

  /**
   * 
   * @return the suite
   */
  static public Test suite() {
    GrouperUtil.stopPromptingUser = true;
    TestSuite suite = new TestSuite();
    
    //do this first so all tests are done on new ddl
    //suite.addTest(AllDdlTests.suite());

    suite.addTestSuite( GrouperVersionTest.class );
    suite.addTestSuite( Test_api_ChildGroupFilter.class );
    suite.addTestSuite( Test_api_ChildStemFilter.class );
    suite.addTestSuite( Test_api_Group.class );
    suite.addTestSuite( Test_api_GrouperAPI.class );
    suite.addTestSuite( Test_api_GrouperConfig.class );
    suite.addTestSuite( Test_api_GrouperDAOFactory.class );
    suite.addTestSuite( Test_api_GrouperSession.class );
    suite.addTestSuite( Test_api_MembershipFinder.class );
    suite.addTestSuite( Test_api_Stem.class );
    suite.addTestSuite( TestMemberChangeSubject.class);

    suite.addTestSuite( Test_cache_EhcacheStats.class );

    suite.addTestSuite( Test_cfg_ApiConfig.class );
    suite.addTestSuite( Test_cfg_ConfigurationHelper.class );
    suite.addTestSuite( Test_cfg_PropertiesConfiguration.class );

    suite.addTestSuite( Test_dao_hibernate_HibernateDaoConfig.class );

    suite.addTestSuite( Test_subj_CachingResolver.class );
    suite.addTestSuite( Test_subj_SourcesXmlResolver.class );
    suite.addTestSuite( Test_subj_SubjectResolver.class );
    suite.addTestSuite( Test_subj_SubjectResolverFactory.class );
    suite.addTestSuite( Test_subj_ValidatingResolver.class );

    suite.addTestSuite( Test_privs_AccessResolver.class );
    suite.addTestSuite( Test_privs_AccessResolverFactory.class );
    suite.addTestSuite( Test_privs_AccessWrapper.class );
    suite.addTestSuite( Test_privs_CachingAccessResolver.class );
    suite.addTestSuite( Test_privs_CachingNamingResolver.class );
    suite.addTestSuite( Test_privs_NamingResolver.class );
    suite.addTestSuite( Test_privs_NamingResolverFactory.class );
    suite.addTestSuite( Test_privs_NamingWrapper.class );

    suite.addTestSuite( Test_uc_NamingPrivs.class );
    suite.addTestSuite( Test_uc_WheelGroup.class );

    suite.addTestSuite( Test_util_ParameterHelper.class );

    suite.addTestSuite(GroupDataTest.class);

    suite.addTest(SuiteRefactor.suite()); 

    suite.addTest(AllGshTests.suite());
    suite.addTest(AllLoaderDbTests.suite());
    suite.addTest(AllUsduTests.suite());
    suite.addTest(AllHooksTests.suite());
    suite.addTest(AllUtilTests.suite());
    suite.addTest(AllRijndaelTests.suite());

    return suite;
  }

} 

