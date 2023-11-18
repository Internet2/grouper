/**
 * Copyright 2014 Internet2
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
 */
package edu.internet2.middleware.grouper.attr;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.attr.assign.AllAttrAssignTests;
import edu.internet2.middleware.grouper.attr.finder.AllAttrFinderTests;
import edu.internet2.middleware.grouper.attr.value.AllAttributeValueTests;

/**
 * 
 * @author mchyzer
 *
 */
public class AllAttributeTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    suite.addTest(AllAttrAssignTests.suite());
    suite.addTest(AllAttrFinderTests.suite());

    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeDefScopeTest.class);
    suite.addTestSuite(EffMshipAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefNameSetTest.class);
    suite.addTestSuite(MemberAttributeSecurityTest.class);
    suite.addTestSuite(MembershipAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefNameFinderTest.class);
    suite.addTestSuite(AttributeDefNameSaveTest.class);
    suite.addTestSuite(AttributeDefTest.class);
    suite.addTestSuite(AttrAssignAttributeSecurityTest.class);
    suite.addTestSuite(AttributeAssignTest.class);
    suite.addTestSuite(AttributeDefNameTest.class);
    suite.addTestSuite(AttributeDefSaveTest.class);
    suite.addTestSuite(GroupAttributeSecurityTest.class);
    suite.addTestSuite(StemAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefAttributeSecurityTest.class);
    //$JUnit-END$

    suite.addTest(AllAttributeValueTests.suite());

    return suite;
  }

}
