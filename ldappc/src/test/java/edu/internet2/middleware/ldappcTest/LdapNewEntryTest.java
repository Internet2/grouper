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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.logging.ErrorLog;

/**
 * Class for doing a simple LDAP search
 * @author Gil Singer 
 */
public class LdapNewEntryTest extends BaseTestCase
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
    public LdapNewEntryTest(String name) 
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
        BaseTestCase.runTestRunner(LdapNewEntryTest.class);
    }
    

    /**
     * A test of LDAP search capability.
     */
    public void testLdapNewEntry() 
    {
        DisplayTest.showRunTitle("testLdapNewEntry", "Add new LDAP entry.");

        displayBindings(ctx, AllJUnitTests.DN_TEST_BASE); 
        Hashtable contextParameters = ConfigManager.getInstance().getLdapContextParameters();
        String administrator = (String)contextParameters.get(Context.SECURITY_PRINCIPAL);
        displayBindings(ctx, administrator); 
        
        //
        // Verify that we can list the base context names
        //

        try
        {
            NamingEnumeration list = ctx.list(AllJUnitTests.DN_TEST_BASE);

            while (list.hasMore()) 
            {
                javax.naming.NameClassPair nc = (javax.naming.NameClassPair)list.next();
            }
        }
        catch(NamingException ne)
        {
            fail("Could not bind at root level -- naming exception: "
                    + ne.getMessage());
        }
        
        //
        // Verify that we can list the manager entry names
        //        

        try
        {
            //NamingEnumeration list = ctx.list(AllJUnitTests.DN_TEST_MANAGER);
            NamingEnumeration list = ctx.list(administrator);
            
 
                   
            while (list.hasMore()) 
            {
                javax.naming.NameClassPair nc = (javax.naming.NameClassPair)list.next();
            }
        }
        catch(NamingException ne)
        {
            fail("Could not bind at root level -- naming exception: " + ne.getMessage());
        }
        
        //
        // Unbind the test ou and cn objects.
        //        

        try
        {
            //ctx.unbind("ou=testOrgUnit2,dc=my-domain,dc=com");
            //ctx.unbind("cn=testPerson,dc=my-domain,dc=com");
            ctx.unbind("ou=testOrgUnit2," + AllJUnitTests.DN_TEST_BASE);
            ctx.unbind("cn=testPerson," + AllJUnitTests.DN_TEST_BASE);
        }
        catch(NamingException ne)
        {
            // Normal case: they will already have been deleted at the end of the previous run.
        }

        Attributes matchAttrs = new BasicAttributes(true); // ignore attribute name case
        matchAttrs.put(new BasicAttribute("cn", "Manager"));
 
        
        //
        // Bind the test ou and cn objects.
        //        

        bindOrgUnitEntry("testOrgUnit2");
        bindInetOrgPersonEntry("testPerson"); 
        
        //
        // Verify that the organizational unit was created.
        //
        
        try
        {
            NamingEnumeration listBindings = ctx.listBindings("ou=testOrgUnit2," + AllJUnitTests.DN_TEST_BASE);

            while (listBindings.hasMore()) 
            {
                javax.naming.NameClassPair nc = (javax.naming.NameClassPair)listBindings.next();
            }
        }
        catch(NamingException ne)
        {
            fail("Could not bind at root level -- naming exception: "
                    + ne.getMessage());
        }

        try
        {
            NamingEnumeration list = ctx.list(administrator);
            ctx.unbind("ou=testOrgUnit2," + AllJUnitTests.DN_TEST_BASE);
            ctx.unbind("cn=testPerson," + AllJUnitTests.DN_TEST_BASE);
        }
        catch(NamingException ne)
        {
            fail("Could not unbind -- naming exception: " + ne.getMessage());
        }
    }
     
    /**
     * Add a new organization unit entry
     * @param orgUnit the name of the entry to be added
     */
    public void bindOrgUnitEntry(String orgUnit)
    {
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute("ou", orgUnit));
        matchAttrs.put(new BasicAttribute("description", orgUnit));
        matchAttrs.put(new BasicAttribute("objectclass", "top"));
        matchAttrs.put(new BasicAttribute("objectclass", "organizationalUnit"));
        String name="ou=" + orgUnit + "," + AllJUnitTests.DN_TEST_BASE;

        try
        {
            ctx.bind(name, ctx, matchAttrs);
         }
        catch(NamingException ne)
        {
            ErrorLog.error(this.getClass(), "Could not bind org unit: " + ne.getMessage());
            fail("Could not bind org unit: " + ne.getMessage());
        }
    }
   
    /**
     * Add a new person entry
     * @param personName the name of the entry to be added
     */
    public void bindInetOrgPersonEntry(String personName)
    {
        Attributes matchAttrs = new BasicAttributes(true);
        matchAttrs.put(new BasicAttribute("uid", personName));
        matchAttrs.put(new BasicAttribute("cn", personName));
        matchAttrs.put(new BasicAttribute("givenname", personName));
        matchAttrs.put(new BasicAttribute("sn", personName));
        matchAttrs.put(new BasicAttribute("userpassword", personName));
        matchAttrs.put(new BasicAttribute("objectclass", "top"));
        matchAttrs.put(new BasicAttribute("objectclass", "person"));
        matchAttrs.put(new BasicAttribute("objectclass", "organizationalPerson"));
        matchAttrs.put(new BasicAttribute("objectclass","inetorgperson"));
        //String name="cn=" + personName + ",dc=my-domain,dc=com";
        String name="cn=" + personName + "," + AllJUnitTests.DN_TEST_BASE;
        try
        {
            ctx.bind(name, ctx, matchAttrs);
        }
        catch(NamingException ne)
        {
            ErrorLog.error(this.getClass(), "Could not bind org unit: " + ne.getMessage());
            fail("Could not bind org unit: " + ne.getMessage());
        }
    }
     /**
     * Determine if an attribute exists
     * Example call: doesAttributeExist("cn", "Manager",AllJUnitTests.DN_TEST_BASE);
     * @param ctx The context
     * @param dn The distinguished name
     * @return true if attribute exists
     */
    public static boolean displayBindings(DirContext ctx, String dn)
    {
 
        boolean success = false;
        NamingEnumeration namingEnumeration = null;
        try
        {
            // Search for objects that have those matching attributes
            if (ctx == null)
            {
                //System.out.println("DEBUG ERROR in SimpleLdapsearchTest, ctx is null.");
            }
            else
            {
                namingEnumeration = ctx.listBindings(dn);
                if (namingEnumeration != null)    
                {
                    if (namingEnumeration.hasMore())
                    {
                       success = true;
                    }
                }
               else
                {
                    success = false;
                }
        
                Binding binding = null;
                while (namingEnumeration.hasMore()) 
                {
                    binding = (Binding)namingEnumeration.next();
                    //System.out.println("DEBUG in LdapNewEntryTest,binding=" + binding);
                }
            }    
        }
        catch(NamingException ne)
        {
            fail("Could not get search attributes -- naming exception: "
                    + ne.getMessage());
        }

        return success;
    }
    
}
        



