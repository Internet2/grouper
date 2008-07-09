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

package edu.internet2.middleware.ldappcTest.dbBuilder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * This class builds a TestSuite out of the individual test classes.
 * It assembles all of the test classes for the configuration management of
 * the Ldappc application. 
 */

public class AllJUnitBuilderTests extends TestCase 
{

    /** 
     * Class for running all of the configuration management test cases.
     */

    public AllJUnitBuilderTests(String name) 
    {
        super(name);
    }

    /** 
     * The suite of test cases.
     */

    public static Test suite() 
    {
        TestSuite suite = new TestSuite(GroupBuilderTest.class);
        // Removing these tests to reduce test running time.  These tests
        // were initial tests to verify developer understanding of Grouper,
        // Signet, and LDAP methods and exercise local provisioning not
        // connected to the provisioning based on the ldappc.xml file.
        //suite.addTest(new TestSuite(SignetSubjectProvisionerTest.class));
        //suite.addTest(new TestSuite(GrouperSubjectProvisionerTest.class));
        suite.addTest(new TestSuite(SignetProvisionerPermissionsTest.class));
        // Use the following format to add additional test
        // (change the class name).
        //suite.addTest(new TestSuite(FooTest.class));
        return suite;
   }
}
