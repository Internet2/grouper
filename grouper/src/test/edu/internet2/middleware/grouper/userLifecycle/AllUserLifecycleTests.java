package edu.internet2.middleware.grouper.userLifecycle;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllUserLifecycleTests {
  
  public static Test suite() {
    TestSuite suite = new TestSuite(AllUserLifecycleTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GroupPolicyUserLifecycleFullDaemonTest.class);
    suite.addTestSuite(UserLifecycleEngineTest.class);
    //$JUnit-END$
    return suite;
  }

}
