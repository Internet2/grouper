package edu.internet2.middleware.grouper.stress;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shilen
 */
public class AllStressTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.stress");

    //$JUnit-BEGIN$
    suite.addTestSuite(GroupDeleteTest.class);
    //$JUnit-END$
    return suite;
  }

}
