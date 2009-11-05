/*
 * @author mchyzer $Id: AllXmlTests.java,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml;

import edu.internet2.middleware.grouper.xml.userAudit.AllXmlUserAuditTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllXmlTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.xml");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_U_API_XmlExporter_internal_groupToXML.class);
    suite.addTestSuite(TestXmlReader.class);
    suite.addTestSuite(Test_U_API_XmlExporter_internal_subjectToXML.class);
    suite.addTestSuite(TestXmlImport.class);
    suite.addTestSuite(Test_U_API_XmlExporter_internal_membershipToXML.class);
    suite.addTestSuite(TestXmlExport.class);
    suite.addTestSuite(Test_U_Util_XML_escape.class);
    suite.addTestSuite(TestXml.class);
    //$JUnit-END$

    suite.addTest(AllXmlUserAuditTests.suite());

    return suite;
  }

}
