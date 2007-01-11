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

package edu.internet2.middleware.ldappcTest.dbBuilder;

import junit.framework.TestCase;

import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.subject.Subject;



import edu.internet2.middleware.signet.ObjectNotFoundException;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

/**
 * Class for testing creating a stem and group so that other
 * LDAP provisioning tests will have data to work on.
 * This is really a test that Grouper methods used in
 * other tests will behave as expected, before running
 * Ldappc tests that depend on those methods.
 *
 * @author Gil Singer 
 */
public class SignetSubjectProvisionerTest extends TestCase
{
    /**
     * the Signet subject provisioner
     */

    private SignetSubjectProvisioner signetSubjectProvisioner;

    /**
     * a subject
     */
    private Subject subject;
    
    /**
     * Ldap Context
     */
    private LdapContext ldapCtx;

    /**
     * Constructor
     */
    public SignetSubjectProvisionerTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());
        
        try
        {       
            ldapCtx = LdapUtil.getLdapContext();
        }
        catch (NamingException ne)
        {
            fail("Could not get LdapContext in SignetSubjectProvisionerTest");
        }
        signetSubjectProvisioner = new SignetSubjectProvisioner(ldapCtx);
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
         junit.textui.TestRunner.run(SignetSubjectProvisionerTest.class);
    }
    
    /**
     * A test of adding a stem to a root stem
     */
    public void testProvision() 
    {
        String failureMessage = null;
        String subjectOuName = "testSignetSubjects";
        String testSubjectsRdn = "ou=" + subjectOuName;

        // TODO: Remove the data from the LDAP directory in DatabaseCleaner
        
        //
        // Check that LDAP directory contains the expected contents.
        //
        
        String rootDn = ConfigManager.getInstance().getGroupDnRoot();
        //
        // Create the DN that is used as a base for adding subjects.
        //
        String testSubjectsDn = testSubjectsRdn + "," + rootDn;

        DisplayTest.showRunTitle("testProvision", "Signet subjects are provisioned to " 
                + testSubjectsDn);
        
        try
        {
            failureMessage = signetSubjectProvisioner.provision(testSubjectsRdn,
                    subjectOuName);
        }
        catch(ObjectNotFoundException onfe)
        {
            fail("In SignerSubjectProvisioner, object not found: " 
                   + onfe.getMessage() );
        }
        catch (NamingException ne)
        {
            fail("Could not provision Signet subjects." + ne.getMessage()); 
        }
        assertNull(failureMessage, failureMessage);
    }
}
