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


import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.logging.ErrorLog;
import edu.internet2.middleware.ldappcTest.DisplayTest;


import java.util.GregorianCalendar;
import java.util.Calendar;

/**
 * InputOptionsTest verifies that arguments input to the InputOptions class constructor
 * (that typically are passed in copies of the command line parameters) are
 * in proper format and that the InputOptions class successfully makes this
 * data available through public getter methods.  It also verifies that bad
 * input arguments result in setting the value of the isFatal() method to "true".
 *
 * @author Gil Singer
 */

public class InputOptionsTest extends TestCase
{
    /*
     * An instance of the main class, just to check that it can be created.
     */
    private InputOptions inputOptions;

    /*
     * An instance of the main class, to check for property setting of options.
     */
    private InputOptions inputOptions1;

    /*
     * An instance of the main class, to check for property setting of options.
     */
    private InputOptions inputOptions2;

    /**
     * InputOptions Test
     */
    public InputOptionsTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());
        //
        // Try to perform the set up
        //
        // Test one set of args and then its complement.
        //

        String args[] = {"-subject", "subjectIdA"};
        String args1[] = {"-subject", "subjectIdA", "-memberships", "-lastModifyTime", "2006-09-14_20:55"};
        String args2[] = {"-subject", "subjectIdB", "-groups", "-permissions", "-interval", "123"};

        try
        {
            inputOptions = new InputOptions(args);
            inputOptions1 = new InputOptions(args1);
            inputOptions2 = new InputOptions(args2);
        }
        catch( Exception e )
        {
            fail( e.toString() );
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
         junit.textui.TestRunner.run(InputOptionsTest.class);
    }
    
    /**
     * Trivial test
     */
    public void testCreateInputOptions()
    {
        DisplayTest.showRunTitle("testCreateInputOptions", "Empty InputOptions is created.");
        //
        // Try to perform the test
        //
        try
        {
            // Trivial test, add more significant ones later:
            // Make sure an agency was created by the constructor.
            assertTrue("Failed to create an inputOptions instance", inputOptions != null);
        }
        catch( Exception e )
        { 
            //
            // Any exception means the test failed.
            //
            fail("Failure in testCreateInputOptons: " + e.getMessage());
        }
        finally
        {
            //
        }
    }
       
    /**
     * Test the first set of input options
     * This tests correct input.
     */
    public void testInputOptions1()
    {
        DisplayTest.showRunTitle("testInputOptions", "Set 1 InputOptions are processed.");
        
        try
        {
            //String args1[] = {"-subject", "subjectIdA", "-memberships", "-lastModifyTime", "2006-09-14_20:55"};
            assertEquals("Failed on set 1, subjectIdA", "subjectIdA", inputOptions1.getSubjectId());
            assertTrue("Failed on set 1, memberships", inputOptions1.getDoMemberships());
            assertTrue("Failed on set 1, groups", !inputOptions1.getDoGroups());
            assertTrue("Failed on set 1, permissions", !inputOptions1.getDoPermissions());
            assertTrue("Failed on set 1, groups", !inputOptions1.getDoGroups());
            assertTrue("Failed on set 1, doPolling", !inputOptions1.getDoPolling());
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(inputOptions1.getLastModifyTime());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int minute = calendar.get(Calendar.MINUTE);
            // The ideal test would be like the following except that the 3rd arg returns a Date, not a String.
            //assertEquals("Failed on lastModifyTime", "2006-09-14_20:55", inputOptions1.getLastModifyTime());
            assertEquals("Failed on lastModifyTime month", 14, day);
            assertEquals("Failed on lastModifyTime minute", 55, minute);
        }
        catch( Exception e )
        { 
            fail("Failure in testInputOptions1: " + e.getMessage());
        }
    }
       
    /**
     * Test the second set of input options
     * This tests correct input. 
     */
    public void testInputOptions2()
    {
        DisplayTest.showRunTitle("testInputOptions", "Set 2 InputOptions are processed.");
        
        try
        {
            //String args2[] = {"-subject", "subjectIdB", "-groups", "-permissions", "-interval", "123"};
            assertEquals("Failed on set 2, subjectIdB", "subjectIdB", inputOptions2.getSubjectId());
            assertTrue("Failed on set 2, memberships", !inputOptions2.getDoMemberships());
            assertTrue("Failed on set 2, groups", inputOptions2.getDoGroups());
            assertTrue("Failed on set 2, permissions", inputOptions2.getDoPermissions());
            assertTrue("Failed on set 2, groups", inputOptions2.getDoGroups());
            assertTrue("Failed on set 2, doPolling", inputOptions2.getDoPolling());
            assertNull("Failed on lastModifyTime", inputOptions2.getLastModifyTime());
        }
        catch( Exception e )
        { 
            fail("Failure in testInputOptions2: " + e.getMessage());
        }
    }
       
    /**
     * Test the second set of input options
     * This tests incorrect input. 
     */
    public void testBadInputOptions()
    {
        DisplayTest.showRunTitle("testBadInputOptions", "BadInputOptions are processed.");
        
        InputOptions badInputOptions = null;

        // Test total garbage
        String args[][] = 
            {
                // Not Fatal: just ignores extraneous data: {"aaa", "bbb", "ccc", "ddd"},
                // Bad interval type 
                {"-subject", "subjectIdA", "-interval", "badIntervalType"},
                // missing subjectId value
                {"-subject", "-groups"},
                // missing subject key
                {"subjectIdValue"}
            };

        for (int i = 0; i< args.length; i++)
        {
            try
            {
                badInputOptions = new InputOptions();
                // Avoid sending fatal error message to the log when doing these tests.
                badInputOptions.setIsTest(true);
                badInputOptions.init(args[i]);
            }
            catch( Exception e )
            {
                fail( e.toString() );
            }
            
            try
            {
                assertTrue("Failed (by not failing) on bad input options", badInputOptions.isFatal());
            }
            catch( Exception e )
            { 
                ErrorLog.error(this.getClass(), "Failure in testInputOptions2: " + e.getMessage());
            }
        }
    }

}
