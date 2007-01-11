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


import edu.internet2.middleware.subject.Subject;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.ldappc.GrouperSessionControl;



import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappcTest.DisplayTest;


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
public class GrouperSubjectProvisionerTest extends TestCase
{
    /**
     * the Grouper subject provisioner
     */

    private GrouperSubjectProvisioner grouperSubjectProvisioner;

    /**
     * a subject
     */
    private Subject subject;
    
    /**
     * the grouper session
     */
    private GrouperSession grouperSession;

    /**
     * the grouper session controller
     */
    private GrouperSessionControl grouperSessionControl;


    /**
     * Constructor
     */
    public GrouperSubjectProvisionerTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());
        
        GrouperSessionControl grouperSessionControl = new GrouperSessionControl();
        boolean started = grouperSessionControl.startSession("GrouperSystem");
        if (!started)
        {
            fail("Could not start grouper session");
        }
        grouperSession = grouperSessionControl.getSession();


        LdapContext ctx = null;
        try
        {       
            ctx = LdapUtil.getLdapContext();
        }
        catch (NamingException ne)
        {
            fail("Could not get LdapContext in GrouperSubjectProvisionerTest");
        }
        grouperSubjectProvisioner = new GrouperSubjectProvisioner(ctx);
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
         junit.textui.TestRunner.run(GrouperSubjectProvisionerTest.class);
    }
    
    /**
     * A test of adding a stem to a root stem
     */
    public void testGrouperSubjectProvisioning() 
    {
        DisplayTest.showRunTitle("testGrouperSubjectProvisioning", "Grouper subjects are provisioned.");

        String testSubjectsName = "grouperTestSubjects";
        String testSubjectsRdn = "ou=" + testSubjectsName;

        try
        {
            grouperSubjectProvisioner.provision(testSubjectsName, testSubjectsRdn);
        }
        catch (NamingException ne)
        {
            fail("Could not provision Grouper subjects." + ne.getMessage()); 
        }
    }
}
