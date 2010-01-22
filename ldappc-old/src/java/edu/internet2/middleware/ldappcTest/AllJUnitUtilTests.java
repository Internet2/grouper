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

import edu.internet2.middleware.ldappcTest.ResourceBundleUtilTest;
import edu.internet2.middleware.ldappcTest.util.LdapUtilTest;

/**
 * This class builds a TestSuite out of the individual test classes.
 * It assembles all of the test classes is the "root" directory of
 * the Ldappc application. 
 * @author Gil Singer
 */

public class AllJUnitUtilTests extends TestCase 
{

    /** 
     * Class for running all of the test cases for the base directory.
     */

    public AllJUnitUtilTests(String name) 
    {
        super(name);
    }

    /** 
     * The suite of test cases.
     */

    public static Test suite() 
    {
        TestSuite suite = new TestSuite(ResourceBundleUtilTest.class);
        // This test takes about 7 minutes on Grouper1.1 so run only
        // occasionally.
        //suite.addTest(new TestSuite(GrouperExportTest.class));
        suite.addTest( new TestSuite(LdapUtilTest.class));
        return suite;
   }
}
