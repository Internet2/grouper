package edu.internet2.middleware.grouper.xmpp;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllXmppTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.xmpp");
    //$JUnit-BEGIN$
    suite.addTestSuite(XmppConnectionBeanTest.class);
    //$JUnit-END$
    return suite;
  }

}
