/*
 * @author mchyzer
 * $Id: AllAuditTests.java,v 1.1 2009-02-01 22:38:48 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.audit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllAuditTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.audit");
    //$JUnit-BEGIN$
    suite.addTestSuite(AuditTypeTest.class);
    suite.addTestSuite(AuditEntryTest.class);
    //$JUnit-END$
    return suite;
  }

}
