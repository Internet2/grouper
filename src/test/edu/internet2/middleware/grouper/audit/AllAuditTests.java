/*
 * @author mchyzer
 * $Id: AllAuditTests.java,v 1.2 2009-02-06 16:33:18 mchyzer Exp $
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
    suite.addTestSuite(AuditTest.class);
    suite.addTestSuite(AuditTypeTest.class);
    suite.addTestSuite(AuditEntryTest.class);
    //$JUnit-END$
    return suite;
  }

}
