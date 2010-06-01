package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllEsbConsumerTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.changeLog.esb.consumer");
    //$JUnit-BEGIN$
    suite.addTestSuite(EsbConsumerTest.class);
    //$JUnit-END$
    return suite;
  }

}
