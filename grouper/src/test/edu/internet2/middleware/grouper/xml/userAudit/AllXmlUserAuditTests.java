/*
 * @author mchyzer
 * $Id: AllXmlUserAuditTests.java,v 1.1 2009-03-31 06:58:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.xml.userAudit;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllXmlUserAuditTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.xml.userAudit");
    //$JUnit-BEGIN$
    suite.addTestSuite(XmlUserAuditExportTest.class);
    //$JUnit-END$
    return suite;
  }

}
