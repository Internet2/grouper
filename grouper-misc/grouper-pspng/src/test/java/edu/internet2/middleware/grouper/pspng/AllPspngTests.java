package edu.internet2.middleware.grouper.pspng;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllPspngTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllPspngTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(PspngFullSyncTest.class);
    suite.addTestSuite(PspngRealTimeSyncTest.class);
    suite.addTestSuite(PspUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
