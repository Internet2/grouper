package edu.internet2.middleware.grouper.app.serviceLifecycle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllServiceLifecycleTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllServiceLifecycleTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperGracePeriodTest.class);
    //$JUnit-END$
    return suite;
  }

}
