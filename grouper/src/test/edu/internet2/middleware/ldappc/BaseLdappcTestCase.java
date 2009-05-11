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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Base test case class which our tests will extend. Provides a static method to run the
 * tests in the class with an LDAP and database wrapper around them.
 */
public class BaseLdappcTestCase extends GrouperTest {

  public static final String TEST_CONFIG = "edu/internet2/middleware/ldappc/ldappc-test.xml";

  protected GrouperSession grouperSession = SessionHelper.getRootSession();

  protected Stem edu;

  protected Stem root;

  /**
   * 
   * @param name
   */
  public BaseLdappcTestCase(String name) {
    super(name);
  }

  /**
   * Prompt the user before making ldap changes.
   * 
   * @param name
   * @param ldappcConfigFile
   *          path to ldappc config file
   */
  public BaseLdappcTestCase(String name, String ldappcConfigFile) {
    super(name);

    GrouperUtil.promptUserAboutLdapChanges("test ldap", true, ldappcConfigFile);
  }

  public void setUp() {
    super.setUp();
    grouperSession = SessionHelper.getRootSession();
    root = StemHelper.findRootStem(grouperSession);
    edu = StemHelper.addChildStem(root, "edu", "education");
  }

  public void tearDown() {
    super.tearDown();
  }
}
