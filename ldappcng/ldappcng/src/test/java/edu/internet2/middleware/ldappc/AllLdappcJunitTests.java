/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.ldappc.configuration.AllJUnitConfigurationTests;
import edu.internet2.middleware.ldappc.synchronize.AllJUnitSynchronizerTests;
import edu.internet2.middleware.ldappc.util.AllJUnitUtilTests;

/**
 * This class builds a master TestSuite out of the individual test suites.
 * 
 * @author Gil Singer
 */
public class AllLdappcJunitTests extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(AllLdappcJunitTests.suite());
  }
  
  /**
   * Constructor
   */
  public AllLdappcJunitTests(String name) {
    super(name);
  }

  /**
   * This method builds a master TestSuite out of the individual test suites.
   */
  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTest(AllJUnitBaseDirTests.suite());
    suite.addTest(AllJUnitConfigurationTests.suite());
    suite.addTest(AllJUnitSynchronizerTests.suite());
    suite.addTest(AllJUnitUtilTests.suite());

    return new TestSetup(suite);
  }
}
