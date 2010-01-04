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
    suite.addTestSuite(XmlExportMemberTest.class);
    //$JUnit-END$
    return suite;
  }

}
