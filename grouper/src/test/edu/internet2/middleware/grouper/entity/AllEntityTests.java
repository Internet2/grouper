/*
 * @author mchyzer $Id: AllGroupTests.java,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.entity;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllEntityTests {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.group");
    //$JUnit-BEGIN$
    suite.addTestSuite(EntityFinderTest.class);
    //$JUnit-END$
    return suite;
  }

}
