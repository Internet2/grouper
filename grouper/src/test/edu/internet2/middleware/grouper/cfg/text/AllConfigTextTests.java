package edu.internet2.middleware.grouper.cfg.text;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllConfigTextTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllConfigTextTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperTextContainerTest.class);
    //$JUnit-END$
    return suite;
  }

}
