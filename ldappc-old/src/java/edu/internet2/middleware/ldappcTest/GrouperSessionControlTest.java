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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.ldappc.GrouperSessionControl;
import edu.internet2.middleware.ldappcTest.DisplayTest;

/**
 * Class for testing the GrouperSessionControl class
 * @author Gil Singer 
 */
public class GrouperSessionControlTest extends TestCase
{
    /**
     * Instance for starting and stopping a GrouperSession
     */
    private GrouperSessionControl grouperSessionControl;
    
    /**
     * Constructor
     */
    public GrouperSessionControlTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName()); 
        grouperSessionControl = new GrouperSessionControl();
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
         junit.textui.TestRunner.run(GrouperSessionControlTest.class);
    }
    

    /**
     * A test of starting and stopping a grouper session.
     */
    public void testGrouperSessionControl() 
    {
        DisplayTest.showRunTitle("testGrouperSessionControl", "GrouperSystem is retrieved.");


        boolean started = grouperSessionControl.startSession("GrouperSystem");

        assertTrue("Failed to start GrouperSystem session", started);

        boolean stopped = grouperSessionControl.stopSession();

        assertTrue("Failed to stop GrouperSystem session", stopped);
    }
    

    /**
     * A test of finding the root stem
     */
    public void testGrouperRootStem() 
    {
        DisplayTest.showRunTitle("testGrouperRootStem", "RootStem is retrieved.");


        boolean started = grouperSessionControl.startSession("GrouperSystem");

        assertTrue("Failed to start GrouperSystem session", started);

        GrouperSession grouperSession = grouperSessionControl.getSession();

        // Find root stem.
        Stem rootStem = StemFinder.findRootStem(grouperSession);
        assertNotNull("root stem not found", rootStem);

        boolean stopped = grouperSessionControl.stopSession();

        assertTrue("Failed to stop GrouperSystem session", stopped);
    }
}
        



