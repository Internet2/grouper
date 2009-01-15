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

import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.ldappcTest.wrappers.DatabaseWrapperTestSetup;
import edu.internet2.middleware.ldappcTest.wrappers.LdapWrapperTestSetup;

/**
 * Base test case class which our tests will extend. Provides a static method to
 * run the tests in the class with an LDAP and database wrapper around them.
 */
public class BaseTestCase extends GrouperTest {

    public BaseTestCase(String name) {
        super(name);
    }

    /**
     * Run the class's tests with an LDAP and a database wrapper around them.
     * 
     * @param clazz
     *            the class to run tests for.
     */
    public static void runTestRunner(Class clazz) {
        junit.textui.TestRunner.run(new LdapWrapperTestSetup(new DatabaseWrapperTestSetup(new TestSuite(clazz))));
    }
}
