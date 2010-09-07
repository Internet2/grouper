package edu.internet2.middleware.grouper.pit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author shilen
 * $Id$
 */
public class AllPITTests {

  /**
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.pit");
    suite.addTestSuite(PITMembershipTests.class);
    return suite;
  }
}
