/*
 * @author mchyzer
 * $Id: AllMemberTests.java,v 1.2 2009-08-12 12:44:45 shilen Exp $
 */
package edu.internet2.middleware.grouper.member;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllMemberTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.member");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestMemberToGroup.class);
    suite.addTestSuite(TestMember.class);
    suite.addTestSuite(TestMember1.class);
    suite.addTestSuite(TestAddMember.class);
    suite.addTestSuite(TestMemberChangeSubject.class);
    suite.addTestSuite(TestMemberFinder.class);
    suite.addTestSuite(TestMemberAttributes.class);
    //$JUnit-END$
    return suite;
  }

}
