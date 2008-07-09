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
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.ldappc.GroupProcessor;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.LdappcProvisionControl;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappcTest.wrappers.DatabaseWrapperTestSetup;
import edu.internet2.middleware.ldappcTest.wrappers.LdapWrapperTestSetup;
import edu.internet2.middleware.subject.Subject;

/**
 * Class for testing creating members of a group so that other
 * LDAP provisioning tests will have data to work on.
 *
 * The following is the structure of the data that is used for
 * adding test data: 
 * testStem1
 *     teststem2
 *         teststem3
 *             testgroup31
 *                 testmember31 
 *                 testmember32 
 *                 testmember33 
 *         testgroup21
 *             testmember21 
 *             testmember22 
 *             testmember23 

 * @author Gil Singer 
 */
public class GrouperProvisionerTest extends BaseTestCase
{
    /**
     * the grouper session
     */
    private GrouperSession grouperSession;

    /**
     * the group processor
     */
    private GroupProcessor groupProcessor;

    /**
     * the grouper session controller
     */
    private GrouperSessionControl grouperSessionControl;

    /**
     * a subject
     */
    private Subject subject;

    /**
     * Constructor
     */
    public GrouperProvisionerTest(String name) 
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

        groupProcessor = new GroupProcessor(grouperSession);
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
        try
        { 
            grouperSession.stop();
        }
        catch (SessionException se) 
        {
            fail("Could not stop the session: " + se.getMessage());
        }
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) 
    {
         BaseTestCase.runTestRunner(GrouperProvisionerTest.class);
    }

    /**
     * A test of adding a member to a group.
     */
    public void testProvisionGroups() 
    {
        DisplayTest.showRunTitle("test provisioning groups");
        
        String[] args = {"-subject", "GrouperSystem", "-groups"};
        InputOptions options = new InputOptions(args);
        
        boolean success = doProvisioning(options);

        if (!success)
        {
            fail("Provisioning of groups failed to complete.");
        }

        /* TODO: replace below with appropriate changes.
        try
        {
            testGroup31 = GroupFinder.findByName(grouperSession, "testStem1:testStem2:testStem3:testGroup31");
        }
        catch (GroupNotFoundException gnfe) 
        {
            ErrorLog.error(this.getClass(), "Group not found: " + gnfe.getMessage());
            System.out.println("DEBUG Group not found: " + gnfe.getMessage());
        }

        groupProcessor.addMember(testGroup31, testGroup21.toSubject());
        String testGroup21Uuid = testGroup21.getUuid();
        
        //
        // Make sure the member exists.
        //
        try
        { 
            //Member.findBySubject(grouperSession, "testStem1");
            Member testGroup31Member = MemberFinder.findByUuid(grouperSession, testGroup21Uuid);
        } 
        catch (MemberNotFoundException mnfe)
        {
            fail("Could not create member for testGroup21."); 
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
            String msg = "A fatal error occurred in Ldappc -- see the error log file";
            DebugLog.info(this.getClass(), msg);
            ErrorLog.warn(this.getClass(), "A fatal error occurred in Ldappc, check earlier messages.");
        }     
        else
        {
            //
            // Create provision control
            //
            LdappcProvisionControl pc = new LdappcProvisionControl(options);
            //
            // TODO: Need to wrap this with loop to full test polling
            //
            pc.run();
            success = true;
        }
        return success;
    }
}
