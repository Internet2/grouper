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
    suite.addTestSuite(XmlExportGroupTypeTest.class);
    suite.addTestSuite(XmlExportMainTest.class);
    suite.addTestSuite(XmlExportAttributeDefTest.class);
    suite.addTestSuite(XmlExportAttributeDefNameTest.class);
    suite.addTestSuite(XmlExportAttributeDefNameSetTest.class);
    suite.addTestSuite(XmlExportRoleSetTest.class);
    suite.addTestSuite(XmlExportAttributeAssignActionTest.class);
    suite.addTestSuite(XmlExportCompositeTest.class);
    suite.addTestSuite(XmlExportAttributeAssignTest.class);
    suite.addTestSuite(XmlExportGroupTypeTupleTest.class);
    suite.addTestSuite(XmlExportMemberTest.class);
    suite.addTestSuite(XmlExportAttributeDefScopeTest.class);
    suite.addTestSuite(XmlExportAttributeTest.class);
    //$JUnit-END$
    return suite;
  }

}
