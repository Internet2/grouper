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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.gsh.AllGshTests;
import edu.internet2.middleware.grouper.app.loader.db.AllLoaderDbTests;
import edu.internet2.middleware.grouper.app.usdu.AllUsduTests;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.AllDdlTests;
import edu.internet2.middleware.grouper.hooks.AllHooksTests;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.util.AllUtilTests;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.util.rijndael.AllRijndaelTests;

/**
 * Run default tests.
 * @author  blair christensen.
 * @version $Id: SuiteDefault.java,v 1.50 2008-11-06 17:08:23 isgwb Exp $
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
	Test test = null;
	boolean noPrompt=false;
	
	if(args==null || args.length==0 || args[0].equalsIgnoreCase("-h")) {
		System.out.println(_getUsage());
		return;
	}
	if(args.length==2 && args[1].equalsIgnoreCase("-noprompt")) {
		noPrompt=true;
	}else if((args.length==2 && !args[1].equalsIgnoreCase("-noprompt")) || args.length>2 ) {
		System.out.println("Invalid argumants. Please check usage:");
		System.out.println(_getUsage());
		return;
	}
		
	if(!args[0].equalsIgnoreCase("-all")) {
		String testName = "edu.internet2.middleware.grouper." + args[0];
		Class claz = null;
		Exception ex = null;
		
		try {
			claz=Class.forName(testName);
			Method method = null;
			try {
				method = claz.getMethod("suite");
				test = (Test)method.invoke(null);
			}catch(NoSuchMethodException e) {
				TestSuite testSuite=new TestSuite();
				testSuite.addTestSuite(claz);
				test=testSuite;
			}
			
		}catch(ClassNotFoundException e) {
			LOG.error("Error finding test class: " + testName, e);
			ex=e;
		}catch(ClassCastException e) {
			LOG.error("Test class is not of type Test: " + testName, e);
			ex=e;
		}catch(InvocationTargetException e) {
			LOG.error("Error invoking suite(): " + testName, e);
			ex=e;
		}catch(IllegalAccessException e) {
			LOG.error("Error invoking suite(): " + testName, e);
			ex=e;
		}
		if(ex!=null) {
			throw new RuntimeException(ex);
		}
	}
	
	
	if(test==null) {
		test=SuiteDefault.suite();
	}
	if(noPrompt) {
		System.setProperty("grouper.allow.db.changes", "true");
	}
    try {
      TestRunner.run(test);
    } catch (RuntimeException re) {
      LOG.error("Error in testing", re);
      throw re;
    }
  }

  /**
   * 
   */
  public static void setupTests() {
    //dont keep prompting user about DB
    GrouperUtil.stopPromptingUser = true;
    RegistryInitializeSchema.initializeSchemaForTests();
  }

  /**
   * 
   * @return the suite
   */
  static public Test suite() {
    setupTests();

    TestSuite suite = new TestSuite();
    
    //do this first so all tests are done on new ddl
    suite.addTest(AllDdlTests.suite());

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
  
  private static String _getUsage() {
	    return  "Usage:"                                                                + GrouperConfig.NL
	            + "args: -h,            Prints this message"                            + GrouperConfig.NL
	            + "args: (-all | testName [-noprompt]"                                  + GrouperConfig.NL
	            
	            + "  -all,              Run all JUnit tests"                            + GrouperConfig.NL
	            + "  testName,          Run specific test - omit package name"          + GrouperConfig.NL
	            + "                     Grouper data e.g. root stem and fields"         + GrouperConfig.NL
	            + "  -noprompt,         Do not prompt user about data loss"             + GrouperConfig.NL
	          
	            ;
	  } // private static String _getUsage()


} 

