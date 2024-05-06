package edu.internet2.middleware.grouper.abac;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllAbacTests {
  
  /**
   * suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.abac");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperLoaderJexlScriptFullSyncTest.class);
    //$JUnit-END$
    return suite;
  }

}
