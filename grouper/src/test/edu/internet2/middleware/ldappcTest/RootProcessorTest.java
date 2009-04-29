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

package edu.internet2.middleware.ldappcTest;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import edu.internet2.middleware.ldappc.ConfigManager;

/**
 * Class for testing the RootProcessor class
 * 
 * @author Gil Singer
 */
public class RootProcessorTest extends BaseLdappcTestCase {

  /**
   * The configuration manager
   */
  private ConfigManager configManager;

  /**
   * The LDAP parameters
   */
  private Hashtable ldapParameters;

  /**
   * The directory context
   */
  private DirContext ctx;

  /**
   * Constructor
   */
  public RootProcessorTest(String name) {
    super(name);
  }

  /**
   * Setup the fixture.
   */
  protected void setUp() {
    DisplayTest.showRunClass(getClass().getName());
    configManager = ConfigManager.getInstance();
    ldapParameters = configManager.getLdapContextParameters();
    try {
      ctx = new InitialDirContext(ldapParameters);
    } catch (NamingException ne) {
      fail("Could not create inital directory context -- naming exception: "
          + ne.getMessage());
    }

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
    BaseLdappcTestCase.runTestRunner(RootProcessorTest.class);
  }

  /**
   * A test of whether an administrator context exists
   */
  public void testContextExists() {
    DisplayTest.showRunTitle("testUseOrCreateContext", "Test Context Exists");

    Hashtable contextParameters = configManager.getLdapContextParameters();
    String administrator = (String) contextParameters.get(Context.SECURITY_PRINCIPAL);

    try {
      ctx.lookup(administrator);
    } catch (NamingException e) {
      fail(administrator + " does not exist but is required for testing.");
    }

    assertTrue(true);
  }
}
