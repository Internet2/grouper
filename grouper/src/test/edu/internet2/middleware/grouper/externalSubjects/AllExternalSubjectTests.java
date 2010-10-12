/*
 * @author mchyzer
 * $Id: AllDdlTests.java,v 1.1 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.externalSubjects;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * tests
 */
public class AllExternalSubjectTests {

  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.externalSubjects");
    //$JUnit-BEGIN$
    suite.addTestSuite(ExternalSubjectTest.class);
    //$JUnit-END$
    return suite;
  }

}
