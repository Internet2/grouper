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

package edu.internet2.middleware.ldappcTest.qs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.internet2.middleware.ldappcTest.SkeletonTest;

/**
 * This class builds a TestSuite out of the individual test classes
 * for checking overall provisioning of data from Grouper and
 * Signet into the LDAP directory.
 */

public class AllJUnitQuickStartTests extends TestCase 
{
    /** 
     * Class for running all of the quick start test cases.
     */

    public AllJUnitQuickStartTests(String name) 
    {
        super(name);
    }

    /** 
     * The suite of test cases.
     */

    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSuite(QuickStartTestU.class));
        suite.addTest(new TestSuite(EduPermissionProvisionTest.class));
        // Add additional tests using the following format:
        // suite.addTest(new TestSuite(TestClass.class));
        return suite;
   }
}
