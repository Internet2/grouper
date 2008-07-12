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

import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * This is just a sanity test that an instance of the Ldappc class
 * can be created.
 * @author Gil Singer 
 */

public class LdappcTest extends BaseTestCase
{

    /*
     * An instance of the main class, just to check that it can be created.
     */
    private Ldappc ldappc;

    /**
     * Ldappc Test
     */
    public LdappcTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());
        //
        // Try to perform the set up
        //
        // Dummy args:
        //
        String args[] = {"dummySubjectId"};
        try
        {
            ldappc = new Ldappc();
        }
        catch( Exception e )
        {
            fail( e.toString() );
        }
        
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
        BaseTestCase.runTestRunner(LdappcTest.class);
    }
    
    /**
     * Trivial test
     */
    public void testCreateLdappc()
    {
        DisplayTest.showRunTitle("testCreateLdappc", "Empty Ldappc is created.");
        //
        // Try to perform the test
        //
        try
        {
            // Trivial test, add more significant ones later:
            // Make sure an agency was created by the constructor.
            assertTrue("Failed to create an ldappc instance", ldappc != null);
        }
        catch( Exception e )
        { 
            //
            // Any exception means the test failed.
            //
            fail( e.toString() );
        }
        finally
        {
            //
        }
    }
}
