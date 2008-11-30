/*
 * @author mchyzer
 * $Id: AllGcTests.java,v 1.1 2008-11-30 10:57:31 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import edu.internet2.middleware.grouperClient.util.AllGcUtilTests;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllGcTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouperClient");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperClientLdapTest.class);
    suite.addTestSuite(GrouperClientWsTest.class);
    //$JUnit-END$
    
    suite.addTest(AllGcUtilTests.suite());
    
    return suite;
  }

}
