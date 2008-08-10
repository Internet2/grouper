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
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import edu.internet2.middleware.ldappc.ConfigManager;

/**
 * Class for doing a simple LDAP search
 * @author Gil Singer 
 */
public class LdapSchemaTest extends BaseTestCase
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
    public LdapSchemaTest(String name) 
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
     * Let's test to make sure our schema elements have been installed and are
     * ready to be used.
     */
    public void testSchemaPresence() throws NamingException {
        Attributes attrs = ctx.getAttributes("cn=schema,ou=system", new String[] { "attributeTypes", "objectClasses" });

        // -------------------------------------------------------------------
        // Check that our 3 attributeTypes are present in cn=schema,ou=system
        // -------------------------------------------------------------------

        Attribute attributeTypes = attrs.get("objectClasses");
        boolean isEduMemberFound = false;
        boolean isEduPermissionFound = false;
        boolean isEduPersonFound = false;
        for (NamingEnumeration ii = attributeTypes.getAll(); ii.hasMore(); /**/) {
            String id = (String) ii.next();

            if (id.indexOf("1.3.6.1.4.1.5923.1.5.2.1") != -1) {
                isEduMemberFound = true;
            }

            if (id.indexOf("1.3.6.1.4.1.5923.1.1.123456789") != -1) {
                isEduPermissionFound = true;
            }

            if (id.indexOf("1.3.6.1.4.1.5923.1.1.2") != -1) {
                isEduPersonFound = true;
            }
        }
        assertTrue(isEduMemberFound);
        assertTrue(isEduPermissionFound);
        assertTrue(isEduPersonFound);
    }
}
