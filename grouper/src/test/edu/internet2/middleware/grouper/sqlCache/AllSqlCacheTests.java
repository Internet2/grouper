package edu.internet2.middleware.grouper.sqlCache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class AllSqlCacheTests extends TestCase {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSqlCacheTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SqlCacheGroupTest.class);
    //$JUnit-END$
    return suite;
  }

}
