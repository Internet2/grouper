package edu.internet2.middleware.grouper.app.syncToGrouper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllSyncToGrouperTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSyncToGrouperTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SyncToGrouperTest.class);
    //$JUnit-END$
    return suite;
  }

}
