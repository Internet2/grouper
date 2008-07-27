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

/**
 * This class builds a TestSuite out of the individual test classes.
 * It assembles all of the test classes is the "root" directory of
 * the Ldappc application. 
 * @author Gil Singer
 */

public class AllJUnitBaseDirTests extends TestCase 
{

    /** 
     * Class for running all of the test cases for the base directory.
     */

    public AllJUnitBaseDirTests(String name) 
    {
        super(name);
    }

    /** 
     * The suite of test cases.
     */

    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(InputOptionsTest.class));
        suite.addTest(new TestSuite(RootProcessorTest.class));
        suite.addTest(new TestSuite(LdapSchemaTest.class));
        suite.addTest(new TestSuite(SimpleLdapSearchTest.class));
        suite.addTest(new TestSuite(GrouperSubjectRetrieverTest.class));
        suite.addTest(new TestSuite(HsqlConnectionTest.class));
        suite.addTest(new TestSuite(SignetSubjectRetrieverTest.class));
        suite.addTest(new TestSuite(GrouperSessionControlTest.class));
        suite.addTest(new TestSuite(LdapNewEntryTest.class));
        suite.addTest(new TestSuite(StemProcessorTest.class));
        suite.addTest(new TestSuite(GroupProcessorTest.class));
        suite.addTest(new TestSuite(GrouperProvisionerTest.class));
        suite.addTest(new TestSuite(GrouperProvisionerLastModifyTest.class));

        //suite.addTest(new TestSuite(SchemaTest.class));
        return suite;
   }

    /**
     * A sanity test -- must always be okay or something is drastically wrong.
     */
    public void testAssert() 
    {
        assertTrue(true);
    }

}
