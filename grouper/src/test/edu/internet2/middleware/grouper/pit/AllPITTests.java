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
    suite.addTestSuite(PITAttributeAssignTests.class);
    suite.addTestSuite(PITAttributeAssignValueTests.class);
    suite.addTestSuite(PITPermissionTests.class);
    suite.addTestSuite(PITUtilsTests.class);
    suite.addTestSuite(PITGroupFinderTests.class);
    suite.addTestSuite(PITAttributeDefFinderTests.class);
    suite.addTestSuite(PITAttributeDefNameFinderTests.class);
    suite.addTestSuite(PITAttributeAssignValueFinderTests.class);
    suite.addTestSuite(PITGroupTests.class);
    suite.addTestSuite(PITMemberTests.class);
    suite.addTestSuite(PITSyncTests.class);
    return suite;
  }
}
