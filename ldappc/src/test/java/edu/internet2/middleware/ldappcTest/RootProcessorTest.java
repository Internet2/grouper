/*
    Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
    Copyright 2004-2006 The University Of Chicago
  
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


import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.RootProcessor;
import edu.internet2.middleware.ldappcTest.DisplayTest;

import javax.naming.Context;

import javax.naming.NamingException;

import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.util.Hashtable;

/**
 * Class for testing the RootProcessor class
 * @author Gil Singer 
 */
public class RootProcessorTest extends TestCase
{

    /**
     * The configuration manager
     */
    private ConfigManager configManager;

    /**
     * The LDAP parameters
     */
    private Hashtable ldapParameters;

    /**
     * The directory context
     */
    private DirContext ctx; 

    /**
     * Constructor
     */
    public RootProcessorTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());       
        configManager = ConfigManager.getInstance();
        ldapParameters = configManager.getLdapContextParameters();
        try
        { 
            ctx = new InitialDirContext(ldapParameters);
        }
        catch(NamingException ne)
        { 
            fail("Could not create inital directory context -- naming exception: "
                    + ne.getMessage());
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
         junit.textui.TestRunner.run(RootProcessorTest.class);
    }

    /**
     * A test of whether an administrator context exists
     */
    public void testContextExists() 
    {
        DisplayTest.showRunTitle("testUseOrCreateContext", "Test Context Exists");
        //
        RootProcessor rootProcessor = new RootProcessor();
        Hashtable contextParameters = ConfigManager.getInstance().getLdapContextParameters();
        String administrator = (String)contextParameters.get(Context.SECURITY_PRINCIPAL);
        boolean exists = rootProcessor.contextExists(administrator);

        if (!exists)
        {
            fail(administrator + " does not exist but is required for testing.");
        }
     }
}
