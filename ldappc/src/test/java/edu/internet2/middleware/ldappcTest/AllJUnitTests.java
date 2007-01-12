/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
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

package edu.internet2.middleware.ldappcTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.ldappcTest.configuration.AllJUnitConfigurationTests;
import edu.internet2.middleware.ldappcTest.dbBuilder.AllJUnitBuilderTests;
import edu.internet2.middleware.ldappcTest.dbBuilder.DatabaseCleanerTest;
import edu.internet2.middleware.ldappcTest.synchronize.AllJUnitSynchronizerTests;
import edu.internet2.middleware.ldappcTest.qs.AllJUnitQuickStartTests;
import edu.internet2.middleware.ldappcTest.qs.QuickStartTestU;

/**
 * This class builds a master TestSuite out of the individual test suites.
 * @author Gil Singer 
 */
public class AllJUnitTests extends TestCase
{
    //
    // Set values for test case dependencies.
    // Currently two sets of test cases are allowed, biofix1 and windows1.
    // These parameter distinguish between different databases being used for testing
    // on different operating systems.
    //
    // Eventually these need to be replaced by having the test case setup build a 
    // common test database and delete it at the end of the test.
    //

    /**
     * Constant indicating the built-in database is to be used for testing on 
     * the biofix system.
     * This is set in the ldappc.properties file and must be consist with it.
     */ 
    public static final String TEST_CASE_BIOFIX_TEST = "biofixTest";

    /**
     * Constant indicating the UC database is to be used for testing on 
     * the biofix system.
     * This is set in the ldappc.properties file and must be consist with it.
     */ 
    public static final String TEST_CASE_BIOFIX_UC = "biofixUC";

    /**
     * Constant indicating the built-in database is to be used for testing on 
     * the Windows system.
     * This is set in the ldappc.properties file and must be consist with it.
     */ 
    public static final String TEST_CASE_WINDOWS_TEST = "windowsTest";

    /**
     * Constant indicating the Quickstart database is to be used for testing on 
     * the Windows system.
     * This is set in the ldappc.properties file and must be consist with it.
     */ 
    public static final String TEST_CASE_WINDOWS_QS = "windowsQS";
    
    /*
     * Constant indicating the URL for the Signet database used on the biofix system.
     */ 
    //public static final String DB_URL_BIOFIX1 = "jdbc:hsqldb:hsql://localhost:9002/demo-uc-people";
    
    /*
     * Constant indicating the URL for the Signet database used on a Windows system.
     */ 
    //public static final String DB_URL_WINDOWS1 = "jdbc:hsqldb:hsql://localhost:9001/xdb";
    
    /**
     * Key for obtaining the Dn for the test context base.
     */ 
    public static final String TEST_CONTEXT_BASE = "testContextBase";
    
    /**
     * Key for obtaining the Dn for the test context base.
     */ 
    public static final String TEST_CONTEXT_BASE_MANAGER = "testContextBaseManager";
    
    /**
     * Key for obtaining the DN for the context base.
     */ 
    public static final String ROOTOU = "rootou";
    
    /**
     * Constant indicating the identifier in the Signet database used on a Windows system.
     */ 
    public static final String SUBJECT_QUERY = "Subject";
    
    /**
     * Constant indicating the identifier in the Signet database used on a Windows system.
     */ 
    public static final String INDIVIDUAL_QUERY = "individual";    
    
    /**
     * The distinguished name for the base context used for testing.
     */ 
    public static String DN_TEST_BASE;
    
    /**
     * A convenience setting to allow easy switching between running all tests
     * and running a specific (hardwired) test.
     * Set ALL= true to run all tests.
     * When setting ALL = false, change the class in the else section if needed.
     */
    private static final boolean ALL = true;
    
    /**
     * Constructor
     */
    public AllJUnitTests(String name)
    {
        super(name);
    }

    /**
     * This method builds a master TestSuite out of the individual test suites.
     */
    public static Test suite()
    {
        // e.g.: DN_TEST_BASE = "dc=my-domain,dc=com";
        DN_TEST_BASE = ResourceBundleUtil.getString(TEST_CONTEXT_BASE);

        TestSuite suite = null;
        if (ALL)
        {    
            suite = new TestSuite(SkeletonTest.class);
            suite.addTest(AllJUnitQuickStartTests.suite());
            // Clean up the database before rerunning the tests.
            //suite.addTest(new TestSuite(DatabaseCleanerTest.class));
            suite.addTest(AllJUnitBaseDirTests.suite());
            suite.addTest(AllJUnitUtilTests.suite());
            suite.addTest(AllJUnitConfigurationTests.suite());
            suite.addTest(AllJUnitSynchronizerTests.suite());
            suite.addTest(AllJUnitBuilderTests.suite());
        }
        else
        {    
            suite = new TestSuite(SkeletonTest.class);
            //suite = new TestSuite(LdapNewEntryTest.class);
            //suite = new TestSuite(SimpleLdapSearchTest.class);
            //suite.addTest(new TestSuite(DatabaseCleanerTest.class));
            //suite = new TestSuite(StemProcessorTest.class);
            //suite.addTest(new TestSuite(GroupProcessorTest.class));
            //suite = new TestSuite(StemProcessorTest.class);
            //suite.addTest(new TestSuite(SignetSubjectProvisionerTest.class));
            //suite.addTest(new TestSuite(GrouperSubjectProvisionerTest.class));
            //suite.addTest(new TestSuite(SignetProvisionerPermissionsTest.class));
            //suite.addTest(new TestSuite(GrouperProvisionerLastModifyTest.class));
            suite.addTest(new TestSuite(QuickStartTestU.class));
        }
        return suite;
   }
}
