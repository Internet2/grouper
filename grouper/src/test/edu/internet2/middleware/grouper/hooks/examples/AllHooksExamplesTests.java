/**
 * Copyright 2014 Internet2
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
/*
 * @author mchyzer $Id: AllHooksExamplesTests.java,v 1.1 2009-03-21 19:48:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllHooksExamplesTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hooks.examples");
    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeAutoCreateHookTest.class);
    suite.addTestSuite(AttributeDefAttributeNameValidationHookTest.class);
    suite.addTestSuite(AttributeDefNameAttributeNameValidationHookTest.class);
    suite.addTestSuite(AttributeSecurityFromTypeHookTest.class);
    suite.addTestSuite(GroupAttributeNameValidationHookTest.class);
    suite.addTestSuite(GroupUniqueNameCaseInsensitiveHookTest.class);
    suite.addTestSuite(LDAPProvisioningHookTest.class);
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.membershipCannotAddSelfToGroupHook", false)) {
      suite.addTestSuite(MembershipCannotAddSelfToGroupHookTest.class);
    }
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.membershipCannotAddSelfToGroupHook", false)) {
      suite.addTestSuite(MembershipCannotAddEveryEntityHookTest.class);
    }
    suite.addTestSuite(MembershipOneInFolderMaxHookTest.class);
    suite.addTestSuite(StemAttributeNameValidationHookTest.class);
    suite.addTestSuite(StemUniqueNameCaseInsensitiveHookTest.class);
    suite.addTestSuite(UniqueObjectHookTest.class);
    suite.addTestSuite(GroupUniqueExtensionHookTest.class);
    //$JUnit-END$
    return suite;
  }

}
