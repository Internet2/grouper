package edu.internet2.middleware.grouper.cache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllGrouperCacheTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllGrouperCacheTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperCacheDatabaseTest.class);
    //$JUnit-END$
    return suite;
  }

}
