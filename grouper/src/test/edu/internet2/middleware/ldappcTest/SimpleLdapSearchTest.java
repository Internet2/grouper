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

import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import edu.internet2.middleware.ldappc.ConfigManager;

/**
 * Class for doing a simple LDAP search
 * @author Gil Singer 
 */
public class SimpleLdapSearchTest extends BaseLdappcTestCase
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
    public SimpleLdapSearchTest(String name) 
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

        // The following shows samples of the type of values that the ConfigManager might set:
        // example: env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        // example: env.put(Context.PROVIDER_URL, "ldap://localhost:389/o=JNDITutorial");
        // example: env.put(Context.PROVIDER_URL, "ldap://localhost:389");
        // example: env.put(Context.SECURITY_AUTHENTICATION, "simple");
        // example: env.put(Context.SECURITY_PRINCIPAL, "cn=S. User, ou=NewHires, o=JNDITutorial");
        // example: env.put(Context.SECURITY_PRINCIPAL, "cn=manager, dc=oocs, dc=com");
        // example: env.put(Context.SECURITY_CREDENTIALS, "secret");
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
        BaseLdappcTestCase.runTestRunner(SimpleLdapSearchTest.class);
    }

    /**
     * A test of LDAP search capability.
     */
    public void testSimpleSearch() 
    {
        DisplayTest.showRunTitle("testSimpleSearch", "Simple search for cn=manager.");


        // Create the initial context
        /* Sample for search:
            NamingEnumeration answer = ctx.search("ou=NewHires", 
                    "(&(mySpecialKey={0}) (cn=*{1}))",      // Filter expression
            new Object[]{key, name},                // Filter arguments
                    null);    // Default search controls
             
            I need to use: "dc=my-domain,dc=com" (objectclass=*));
        */

        // Specify the attributes to match
        // Ask for objects that has a common name ("cn") attribute with 
        // the value "manager"
        Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
        matchAttrs.put(new BasicAttribute("cn", "Manager"));

        try
        {
            // Search for objects that have those matching attributes
            if (matchAttrs == null)
            {
                fail("In SimpleLdapsearchTest, matchAttrs is null.");
            }
            if (ctx == null)
            {
                fail("In SimpleLdapsearchTest, ctx is null.");
            }
            NamingEnumeration answer = ctx.search(AllLdappcJunitTests.DN_TEST_BASE, matchAttrs);
            //e.g.: NamingEnumeration answer = ctx.search("dc=my-domain,dc=com", matchAttrs);
            if (answer != null)
            {
                if (!answer.hasMore())
                {
                    fail("Could not find any search match.");
                }
            
            }
            else
            {
                fail("value returned from search is null.");
            }
        
            Attributes searchAttributes = null;
            while (answer.hasMore()) {
                SearchResult sr = (SearchResult)answer.next();
                assertEquals("cn=manager", sr.getName().toLowerCase());
                searchAttributes = sr.getAttributes();
                for (NamingEnumeration e = searchAttributes.getAll(); e.hasMore();)
                {
                    String value = (String) e.next().toString();
                    // Check for value equal to objectClass: organizationalRole
                    // or 
                    // Check for value equal to cn: manager 
                    
                    //
                    // Check that the available attributes are cn or objectClass values.
                    //

                    if (value.indexOf("cn:") < 0 && value.indexOf("objectClass:") < 0)
                    {
                        fail("Values other than cn: or ObjectClass found");
                    }

                    //
                    // Check that the value of cn is manager
                    //
                    
                    if (value.indexOf("cn:") >= 0)
                    {
                        if (value.indexOf("manager") < 0 && value.indexOf("Manager") < 0 )
                        {
                            fail("cn: manager not found");
                        }
                    }
                     
                    //
                    // Check that the value of objectClass isorganizationalRole
                    //
                      
                    if (value.indexOf("objectClass:") >= 0)
                    {
                        if (value.indexOf("organizationalRole") < 0)
                        {
                            fail("objectClass: organizationalRole not found");
                        }
                    }

                } 
            }
    
        }
        catch(NamingException ne)
        {
            fail("Could not get search attributes -- naming exception: "
                    + ne.getMessage());
        }
    }
}
