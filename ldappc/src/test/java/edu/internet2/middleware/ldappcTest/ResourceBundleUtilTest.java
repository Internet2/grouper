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

import junit.framework.TestCase;

import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * Class for making sure a connection can be make to the Hsql database.
 * This does not use any of the non-test classes.
 * @author Gil Singer 
 */
public class ResourceBundleUtilTest extends TestCase
{

    /**
     * Constructor
     */
    public ResourceBundleUtilTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());       
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
         junit.textui.TestRunner.run(ResourceBundleUtilTest.class);
    }

    /**
     * A test accessing an ldappc resource bundle property.
     */
    public void testResourceBundleUtil() 
    {
        DisplayTest.showRunTitle("testResourceBundleUtil", "Process Properties File");
        String dbUrl = ResourceBundleUtil.getString("signetDbUrl");
        String msg = "Does not contain localhost";
        String expected = "Contains localhost";
        if (dbUrl.indexOf("localhost") != -1)
        {
            msg = expected;
        }    
        assertEquals(expected, msg);
    }
    
}
