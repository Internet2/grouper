/*
 * @author mchyzer
 * $Id: AllXmlTests.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllXmlTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.xml");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestXmlReader.class);
    suite.addTestSuite(Test_U_API_XmlExporter_internal_groupToXML.class);
    suite.addTestSuite(Test_U_API_XmlExporter_internal_subjectToXML.class);
    suite.addTestSuite(Test_U_API_XmlExporter_internal_membershipToXML.class);
    suite.addTestSuite(TestXmlExport.class);
    suite.addTestSuite(TestXml.class);
    suite.addTestSuite(TestXmlImport.class);
    suite.addTestSuite(Test_U_Util_XML_escape.class);
    //$JUnit-END$
    return suite;
  }

}
