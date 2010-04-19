/*
 * @author mchyzer
 * $Id: AllGcTests.java,v 1.4 2009-11-17 06:25:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouperClient.poc.GrouperClientWsTest;
import edu.internet2.middleware.grouperClient.util.AllGcUtilTests;


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
    //suite.addTestSuite(GrouperClientLdapTest.class);
    suite.addTestSuite(GrouperClientWsTest.class);
    //$JUnit-END$
    
    suite.addTest(AllGcUtilTests.suite());
    
    return suite;
  }

}
