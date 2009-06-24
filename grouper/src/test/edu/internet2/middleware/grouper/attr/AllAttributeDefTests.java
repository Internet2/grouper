package edu.internet2.middleware.grouper.attr;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllAttributeDefTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeDefTest.class);
    //$JUnit-END$
    return suite;
  }

}
