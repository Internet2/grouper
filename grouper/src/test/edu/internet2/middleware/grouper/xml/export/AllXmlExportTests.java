/**
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
 */
/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.xml.export;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllXmlExportTests {

  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.xml.export");
    //$JUnit-BEGIN$
    suite.addTestSuite(XmlExportStemTest.class);
    suite.addTestSuite(XmlExportFieldTest.class);
    suite.addTestSuite(XmlExportAttributeAssignActionSetTest.class);
    suite.addTestSuite(XmlExportAttributeAssignValueTest.class);
    suite.addTestSuite(XmlExportGroupTest.class);
    suite.addTestSuite(XmlExportMembershipTest.class);
    suite.addTestSuite(XmlExportMainTest.class);
    suite.addTestSuite(XmlExportAttributeDefTest.class);
    suite.addTestSuite(XmlExportAttributeDefNameTest.class);
    suite.addTestSuite(XmlExportAttributeDefNameSetTest.class);
    suite.addTestSuite(XmlExportRoleSetTest.class);
    suite.addTestSuite(XmlExportAttributeAssignActionTest.class);
    suite.addTestSuite(XmlExportCompositeTest.class);
    suite.addTestSuite(XmlExportAttributeAssignTest.class);
    suite.addTestSuite(XmlExportMemberTest.class);
    suite.addTestSuite(XmlExportAttributeDefScopeTest.class);
    //$JUnit-END$
    return suite;
  }

}
