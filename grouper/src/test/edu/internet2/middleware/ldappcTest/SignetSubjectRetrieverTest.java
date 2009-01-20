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

import java.util.List;

import edu.internet2.middleware.ldappc.SignetSubjectRetriever;

/**
 * Class for doing a simple LDAP search
 * 
 * @author Gil Singer
 */
public class SignetSubjectRetrieverTest extends BaseLdappcTestCase {

    /**
     * Constructor
     */
    public SignetSubjectRetrieverTest(String name) {
        super(name);
    }

    /**
     * Setup the fixture.
     */
    protected void setUp() {
        DisplayTest.showRunClass(getClass().getName());
    }

    /**
     * Tear down the fixture.
     */
    protected void tearDown() {
    }

    /**
     * The main method for running the test.
     */
    public static void main(String args[]) {
        BaseLdappcTestCase.runTestRunner(SignetSubjectRetrieverTest.class);
    }

    /**
     * A test of LDAP search capability.
     */
    public void testSignetSubjectRetriever() {
        DisplayTest.showRunTitle("testSignetSubjectRetriever", "All privileged subjects are retrieved.");

        SignetSubjectRetriever signetSubjectRetriever = new SignetSubjectRetriever();
        List subjectList = signetSubjectRetriever.getAllPrivilegedSubjects();
        assertNotNull(subjectList);
        // 
        // Different test database will return different subjectList sizes.
        // For example, the Signet demo database returns ? while the test
        // database on
        // the Unix biofix system has 8. However, this figure may change.
        // Therefore, this just tests to make sure that there are at least 2
        // subject in the
        // list.
        //

        String msg = "Bad subject list size = " + subjectList.size();
        String expected = "Subject list size is okay ( > 2 ).";
        if (subjectList.size() > 2) {
            msg = expected;
        }
        assertEquals(expected, msg);
    }
}
