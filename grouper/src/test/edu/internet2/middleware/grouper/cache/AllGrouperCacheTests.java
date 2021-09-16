package edu.internet2.middleware.grouper.cache;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllGrouperCacheTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllGrouperCacheTests.class.getName());
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.databaseCache", false)) {
      suite.addTestSuite(GrouperCacheDatabaseTest.class);
    }
    return suite;
  }

}
