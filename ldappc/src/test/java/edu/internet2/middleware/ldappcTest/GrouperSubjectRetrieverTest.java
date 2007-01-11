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

package edu.internet2.middleware.ldappcTest;

import junit.framework.TestCase;

import edu.internet2.middleware.ldappc.GrouperSubjectRetriever;
import edu.internet2.middleware.ldappcTest.DisplayTest;
import edu.internet2.middleware.subject.Subject;

/**
 * Class for doing a simple LDAP search
 * @author Gil Singer 
 */
public class GrouperSubjectRetrieverTest extends TestCase
{

    /**
     * Constructor
     */
    public GrouperSubjectRetrieverTest(String name) 
    {
        super(name);
    }
    
    /**
     * Setup the fixture.
     */
    protected void setUp() 
    {
        DisplayTest.showRunClass(getClass().getName());       
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
         junit.textui.TestRunner.run(GrouperSubjectRetrieverTest.class);
    }
    

    /**
     * A test of LDAP search capability.
     */
    public void testGrouperSubjectRetriever() 
    {
        DisplayTest.showRunTitle("testGrouperSubjectRetriever", "GrouperSystem is retrieved.");

        GrouperSubjectRetriever subjectRetriever = new GrouperSubjectRetriever();
        Subject subject = subjectRetriever.findSubjectById("GrouperSystem");

        assertNotNull(subject);
        if (subject != null)
        {
            assertEquals("GrouperSystem", subject.getId());
            assertEquals("GrouperSystem", subject.getName());
        }
    }
}
        



