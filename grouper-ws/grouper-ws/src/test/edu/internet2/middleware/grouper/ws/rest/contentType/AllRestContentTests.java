/*
 * @author mchyzer
 * $Id: AllRestContentTests.java,v 1.1 2008-03-25 05:15:09 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.rest.contentType;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllRestContentTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.ws.rest.contentType");
    //$JUnit-BEGIN$
    suite.addTestSuite(WsXhtmlOutputConverterTest.class);
    suite.addTestSuite(RestConverterTest.class);
    //$JUnit-END$
    return suite;
  }

}
