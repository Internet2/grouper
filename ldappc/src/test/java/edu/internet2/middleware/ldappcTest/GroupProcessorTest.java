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

import edu.internet2.middleware.subject.Subject;


import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.MemberAddException;
import edu.internet2.middleware.grouper.GrouperRuntimeException;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;

import edu.internet2.middleware.ldappc.GroupProcessor;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappcTest.DisplayTest;

import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import java.util.Set;
import java.util.Iterator;

/**
 * Class for testing creating members of a group so that other
 * LDAP provisioning tests will have data to work on.
 * This class assumes the following database structure:
 *
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
public class GroupProcessorTest extends BaseTestCase
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
     * the grouper subject retriever
     */
    private GrouperSubjectRetriever grouperSubjectRetriever;

    /**
     * a subject
     */
    private Subject subject;

    /**
     * testGroup1 
     */
    private Group testGroup31;

    /**
     * testGroup2 
     */
    private Group testGroup21;

    /**
     * Constructor
     */
    public GroupProcessorTest(String name) 
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
        BaseTestCase.runTestRunner(GroupProcessorTest.class);
    }

    /**
     * A test of adding a member to a group.
     */
    /**
     * A test of adding a member to a group.
     */
    public void testAddMember() 
    {
        DisplayTest.showRunTitle("test adding members");
        
        //
        // Add testGroup21 as a member of testGroup31
        //

        try
        {
            testGroup31 = GroupFinder.findByName(grouperSession, "testStem1:testStem2:testStem3:testGroup31");
            testGroup21 = GroupFinder.findByName(grouperSession, "testStem1:testStem2:testGroup21");
        }
        catch (GroupNotFoundException gnfe) 
        {
            ErrorLog.error(this.getClass(), "Group not found: " + gnfe.getMessage());
        }

        Subject groupAsSubject = testGroup21.toSubject();
        /* The following attempt to check for membership resulted in displaying
           that the member does not already exist, but the addMember complained
           that it did already exist.  It should not exist and attempts to
           delete it failed. TODO: Resolve why add member says it already exists.
          
        if (testGroup31.hasMember(groupAsSubject))
        {
            // Remove the member if it already exists in the group; then add it.
            System.out.println("DEBUG, member already exists.");
            groupProcessor.deleteMember(testGroup31, groupAsSubject);
        }  
        else
        {
            System.out.println("DEBUG, member does not already exist.");
        }
        */
        boolean wasAdded = groupProcessor.addMember(testGroup31, groupAsSubject);
       
        // 
        // The following handles two different database contents for subjects.  
        // The first section handles the built-in database which contains a subject, "babl",
        // while second section handles a database that contains a subject, "test.subject.2".
        // 
        // TODO: Eliminate this dependency by creating a common subject.
        //
          
        Subject testSubject = null;
        String subjectTestString = null;
        subjectTestString = "babl";
//        Set testSubjects = SubjectFinder.findAll(subjectTestString);
//
//        Iterator it = testSubjects.iterator();
//        while (it.hasNext())
//        {            
//           testSubject = (Subject)it.next();
//           // Just use the first subject with "babl" as part of the name.
//           break;
//        }
        // We know there's only one subject matching "babl". Let's just find by Id.
        try
        {
            testSubject = SubjectFinder.findById(subjectTestString);
        }
        catch (SubjectNotFoundException e)
        {
            fail("Could not find subject: " + subjectTestString);
            System.out.println("Could not find subject: " + subjectTestString);
        }
        catch (SubjectNotUniqueException e)
        {
            fail("Subject not unique: " + subjectTestString);
            System.out.println("Subject not unique: " + subjectTestString);
        }
        
        if (testSubject == null)
        {
            String subjectTestString2 = "test.subject.2";
            try
            {
                testSubject = SubjectFinder.findById(subjectTestString2);
            }
            catch (SubjectNotFoundException eSNF)  
            {
                fail("Could not find subject " + subjectTestString + " or " + subjectTestString2);
                System.out.println("Could not find subject: " + subjectTestString2);
            }
            catch (SubjectNotUniqueException eSU) 
            {
                System.out.println("Subject not unique: " + subjectTestString2);
                fail("Subject not unique:" + subjectTestString + " or " + subjectTestString2);
            }
            
        }

        if (testGroup31.hasMember(testSubject))
        {
            // Remove the member if it already exists in the group; then add it.
            groupProcessor.deleteMember(testGroup31, testSubject);
        }
        
        wasAdded = groupProcessor.addMember(testGroup31, testSubject);

        assertTrue("Failed to add member: testSubject.getName()", wasAdded);
        
        // TODO: Remove members first before adding and add checks that wasAdded is true;
        // TODO: currently it may be true because it already exists.
        //
        // Make sure the member exists.
        //
        try
        { 
            Group testGroup31Member = GroupFinder.findByUuid(grouperSession, testGroup21.getUuid());
        } 
        catch (GroupNotFoundException mnfe)
        {
            fail("Could not find testGroup21 by Id: " + mnfe.getMessage()); 
        }
            try 
            {
                testGroup21.addMember(subject);
                groupProcessor.addMember(testGroup31, testGroup21.toSubject());
            }
            catch (MemberAddException mae) 
            {
                // Skip adding member if it already exists.
            }
            catch (InsufficientPrivilegeException ipe) 
            {
                ErrorLog.error(this.getClass(), "Insufficent privilege for adding member: " 
                        + testGroup21.toSubject().getName() + " -- " + ipe.getMessage());
            }
        
        //
        // Make sure the member exists.
        //
        
        Set retrievedMembers = null;
        try
        { 
            retrievedMembers = testGroup31.getMembers();
        } 
        catch (GrouperRuntimeException mnfe)
        {
            fail("Failure trying to find members for testGroup21: " + mnfe.getMessage()); 
        }
        if (retrievedMembers.size() <= 0)
        {
            fail("Member set empty for testGroup31");
        }
        
        // Remove the member if it already exists in the group; then add it.
        groupProcessor.deleteMember(testGroup31, groupAsSubject);
        // Remove the member so it is clean for the next run of test cases.
        groupProcessor.deleteMember(testGroup31, testSubject);
     }

    /**
     * 
     * @param stem
     * @return the stem created
     */
    /*
    public Member addMember(Group group, Subject subject)
    {
        Member member = null;
        try
        {
            // If it already exists, use it.
            member = StemFinder.findByName(grouperSession, stemFullPath);
        }
        catch(MemberNotFoundException mnfe)
        {
            // Member does not exist so create it.

            member = groupProcessor.addmember(group, subject);
            if (member == null)
            {
                fail("Could not create " + subject.getName());
            }
        }
    return stem;
    }
    */
}
