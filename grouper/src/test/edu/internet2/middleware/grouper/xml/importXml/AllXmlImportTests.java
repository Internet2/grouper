/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.xml.importXml;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllXmlImportTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.xml.importXml");
    //$JUnit-BEGIN$
    suite.addTestSuite(XmlImportMainTest.class);
    //$JUnit-END$
    return suite;
  }

}
