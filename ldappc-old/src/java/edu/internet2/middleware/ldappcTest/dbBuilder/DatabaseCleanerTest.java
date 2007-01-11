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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappc.StemProcessor;
import edu.internet2.middleware.ldappc.logging.DebugLog;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.ldappcTest.TestOptions;
import edu.internet2.middleware.ldappcTest.dbBuilder.DatabaseCleaner;
import edu.internet2.middleware.ldappcTest.dbBuilder.DatabaseDisplayer;

/**
 * Class for a grouper database so that other
 * LDAP provisioning tests will have data to work on.
 * This adds data to the existing quickstart database
 * and deletes it afterword.  In case it is not delete
 * afterword, all of the data is delete prior to 
 * running the database as well.
 *
 * This test is depending on many capabilities working properly.
 * Therefore, it should run only after an initial set of test
 * cases that individual test basic capabilities. 
 *
 * The test database has the following structure added to it by the test cases:
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
 * 
 * @author Gil Singer 
 */
public class DatabaseCleanerTest extends TestCase
{
    /**
     * the grouper session
     */
    private GrouperSession grouperSession;

    /**
     * the grouper subject retriever
     */
    private GrouperSubjectRetriever grouperSubjectRetriever;

    /**
     * a subject
     */
    private Subject subject;

    /**
     * the root stem
     */
    private Stem rootStem;

    /**
     * testStem1 
     */
    private Stem testStem1;

    /**
     * topLevelGroup1 
     */
    private Group testGroup1;

    /**
     * Constructor
     */
    public DatabaseCleanerTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName()); 

        grouperSubjectRetriever = new GrouperSubjectRetriever();
        subject = grouperSubjectRetriever.findSubjectById("GrouperSystem");
         
        try 
        {
            grouperSession = GrouperSession.start(subject);
            DebugLog.info("Started GrouperSession: " + grouperSession);
        }
        catch (SessionException se) 
        {
            ErrorLog.error(this.getClass(), "Failed to start GrouperSession for subjectId= " 
                   + "GrouperSystem" + ":    "  + se.getMessage());
            fail("Failed to start GrouperSession for subjectId= " 
                   + "GrouperSystem" + ":    "  + se.getMessage());
        }
        catch (Exception e) 
        {
            ErrorLog.error(this.getClass(), "Failed to find GrouperSession: "  + e.getMessage());
            fail("Failed to find GrouperSession: "  + e.getMessage());
        }

        // Find root stem.
        rootStem = StemFinder.findRootStem(grouperSession);

    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() 
    {
        //
        //Do not destroy the database yet as other test cases will use it.  
        // It is to be destroyed by a separate class run after all test cases.
        //

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
         junit.textui.TestRunner.run(DatabaseCleanerTest.class);
    }
    
    /**
     * A test a new root stem was created and that topLevelStem1 does not yet exist.
     */
    public void testGrouperRootStem() 
    {
        DisplayTest.showRunTitle("testGrouperRootStem", "RootStem is retrieved.");
    
        assertNotNull("root stem not found", rootStem);
    }
    
    /**
     * A test a new root stem was created and that topLevelStem1 does not yet exist.
     */
    public void testClean() 
    {
        DisplayTest.showRunTitle("testGrouperRootStem", "Database is cleaned.");
        
        //display("Before");
        
        clean();
        
        display("After");
        

        boolean stemExists = StemProcessor.doesStemExist(grouperSession, "testStem1:testStem2");
        assertFalse("testStem1:testStem2 should not exist", stemExists);
        stemExists = StemProcessor.doesStemExist(grouperSession, "testStem1");
        assertFalse("testStem1 should not exist", stemExists);
    }
    /**
     * Display database contents.
     */
    public void display(String beforeOrAfter) 
    {
        // 
        // Display the database contents.
        //
        if (TestOptions.SHOW_CONTENTS)
        {
            System.out.println("--- " + beforeOrAfter + " Clean ---");
            DatabaseDisplayer displayer = new DatabaseDisplayer();
            displayer.display();
        }
    }
    
    /**
     * Display selected database contents.
     */
    public void clean()
    {
               
        // 
        // Remove all data created by this test before rerunning
        //
        
        DatabaseCleaner cleaner = new DatabaseCleaner();
        cleaner.clean();
            
        // 
        // Display the database contents.
        //
        /*
        if (TestOptions.SHOW_CONTENTS)
        {
            System.out.println("--- After Clean ---");
            DatabaseDisplayer displayer = new DatabaseDisplayer();
            displayer.display();
        }
        */
    }

}
