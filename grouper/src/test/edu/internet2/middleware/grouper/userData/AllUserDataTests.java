package edu.internet2.middleware.grouper.userData;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllUserDataTests extends TestCase {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(AllUserDataTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperFavoriteFinderTest.class);
    suite.addTestSuite(GrouperUserDataApiTest.class);
    suite.addTestSuite(UserDataListTest.class);
    //$JUnit-END$
    return suite;
  }

}
