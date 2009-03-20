/*
 * @author mchyzer
 * $Id: AllMemberTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
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
    suite.addTestSuite(Test_I_API_MemberOf_addComposite.class);
    suite.addTestSuite(Test_I_API_MemberOf_deleteComposite.class);
    suite.addTestSuite(Test_I_API_MemberOf_deleteImmediate.class);
    suite.addTestSuite(TestMember.class);
    suite.addTestSuite(TestAddMember.class);
    suite.addTestSuite(Test_I_API_MemberOf_addImmediate.class);
    suite.addTestSuite(TestMemberChangeSubject.class);
    suite.addTestSuite(TestMemberFinder.class);
    //$JUnit-END$
    return suite;
  }

}
