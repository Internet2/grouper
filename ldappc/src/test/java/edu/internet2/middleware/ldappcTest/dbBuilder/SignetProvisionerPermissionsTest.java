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

import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;
import edu.internet2.middleware.ldappc.LdappcSignetProvisioner;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappc.util.SubjectCache;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

/**
 * Class for testing provisioning permissions.
 *
 * @author Gil Singer 
 */
public class SignetProvisionerPermissionsTest extends TestCase
{
    /**
     * the LdappcSignetProvisioner instance
     */
    private LdappcSignetProvisioner ldappcSignetProvisioner;

    /**
     * a subject
     */
    private Subject subject;

    /**
     * the input options
     */
    private InputOptions options;
    /**
     * Constructor
     */
    public SignetProvisionerPermissionsTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName()); 

        SubjectCache subjectCache = new SubjectCache();
        // TODO Must populate subject cache with hash tables for sources.

        String[] args = {"-subject", "GrouperSystem", "-permissions"};
        options = new InputOptions(args);
        ldappcSignetProvisioner = new LdappcSignetProvisioner(options, subjectCache);
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
         junit.textui.TestRunner.run(SignetProvisionerPermissionsTest.class);
    }

    /**
     * A test of adding a member to a group.
     */
    public void testProvisionPermissions() 
    {
        DisplayTest.showRunTitle("Test provisioning permissions");
        
        
        boolean success = doProvisioning(options);

        if (!success)
        {
            fail("Provisioning of permissions failed to complete.");
        }

        /* TODO: replace below with appropriate changes.
        try
        {
        }
        catch (???NotFoundException gnfe) 
        {
            ErrorLog.error(this.getClass(), "Permission not found: " + gnfe.getMessage());
            System.out.println("DEBUG Group not found: " + gnfe.getMessage());
        }
        */
     }

     /**
      * Do the provisioning
      * @return false if fails 
      */
    private boolean doProvisioning(InputOptions options)
     {
        boolean success = false;

        if (options.isFatal())
        {
            String msg = "A fatal error occurred in SignetProvisionerPermissionsTest -- see the error log file";
            DebugLog.info(this.getClass(), msg);
            ErrorLog.warn(this.getClass(), "A fatal error occurred in SignetProvisionerPermissionsTest.");
        }     
        else
        {
            ldappcSignetProvisioner.provisionPermissions();
            success = true;
        }

        return success;
    }
    
    /**
     * A test of finding a PrivilegedSubject's permission, changing it
     * in Signet, doing the provisioning, verifying that the LDAP directory
     * reflects the change, changing it back and verifying the the
     * LDAP directory values is back to the original.  (TODO)
     */
    public void testChangePermission() 
    {
        DisplayTest.showRunTitle("Test changing a permission");
        
        //
        // Find a PrivilegedSubject's permission and change it
        // in the Signet database. 
        //
        
        Signet signet = null;
        try
        {
            signet = new Signet();
        }
        catch (SignetRuntimeException sre)
        {
            LdappcConfigurationException ace = new LdappcConfigurationException(
                    "Failed to create Signet instance: Signet database may not be running.",
                    sre);
            throw(ace);
        }

        // Uncomment this to display the data accessed below.
        //SignetUtil.displaySubjectPrivileges("person", "SD00009");
        
        //
        // Get the PrivilegedSubject, which should be uid=lsaito, Saito, Lee
        //
        PrivilegedSubject privSubject = null;
        try
        { 
             privSubject = signet.getPrivilegedSubject("person", "SD00009");
        } 
        catch(ObjectNotFoundException onfe)
        { 
             fail("Could not find PrivilegedSubject.  " + onfe.getMessage());
        } 
        Set privileges = privSubject.getPrivileges();
        Privilege privilege = null;
        Iterator it = privileges.iterator();
        while (it.hasNext())
        {
            privilege = (Privilege) it.next();
            Permission permission = privilege.getPermission();
            TreeNode scope = privilege.getScope();
            Set limitValues = privilege.getLimitValues();
            LimitValue limitValue = null;
            Limit limit = null;
            Iterator it2 = limitValues.iterator();
            while (it2.hasNext())
            {
                limitValue = (LimitValue)it2.next();
                limit = limitValue.getLimit(); 
                if ("Spending limit".equals(limit.getName()))
                {
                    assertEquals( "Value of LimitValue is incorrect.", "10000", limitValue.getValue() );
                    assertEquals( "Id of Limit is incorrect", "spending-limit", limit.getId() );
                    assertEquals( "Id of subsystem is incorrect", "projectx", limit.getSubsystem().getId() );
                }
            }
        }

        //
        // I can not find methods for changing or deleting values in the Signet database; TODO: finish
        // the rest of this when I figure out how.  
        //

        //
        // Provision the Signet change to the LDAP directory
        //
        
        boolean success = doProvisioning(options);

        if (!success)
        {
            fail("Provisioning of permissions failed to complete.");
        }
        
        // TODO the following tests:

        //
        // Verify that the LDAP directory has the changed value.
        //
          
        //
        // Change the value back to the original value in the Signet database.
        //
          
        //
        // Do the provisioning again
        //

        //
        // Verify that the LDAP directory has the original value.
        //
     }

}
