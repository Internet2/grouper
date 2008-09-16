/*
 * @author mchyzer
 * $Id: AllSubjectTests.java,v 1.1 2008-09-16 05:12:09 mchyzer Exp $
 */
package edu.internet2.middleware.subject.provider;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * all tests
 */
public class AllSubjectTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.subject.provider");
    //$JUnit-BEGIN$
    suite.addTestSuite(JNDISourceAdapterTest.class);
    suite.addTestSuite(SourceManagerTests.class);
    suite.addTestSuite(JDBCSourceAdapterTest.class);
    //$JUnit-END$
    return suite;
  }

}
