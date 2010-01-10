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
    suite.addTestSuite(XmlExportGroupTest.class);
    suite.addTestSuite(XmlExportCompositeTest.class);
    suite.addTestSuite(XmlExportGroupTypeTest.class);
    suite.addTestSuite(XmlExportGroupTypeTupleTest.class);
    suite.addTestSuite(XmlExportMemberTest.class);
    suite.addTestSuite(XmlExportAttributeTest.class);
    //$JUnit-END$
    return suite;
  }

}
