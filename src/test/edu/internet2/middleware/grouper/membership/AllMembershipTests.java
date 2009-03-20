/*
 * @author mchyzer
 * $Id: AllMembershipTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.membership;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllMembershipTests {

  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.membership");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestFindBadMemberships.class);
    suite.addTestSuite(TestMembershipFinder.class);
    suite.addTestSuite(TestMembershipDeletes1.class);
    suite.addTestSuite(TestMembership12.class);
    suite.addTestSuite(TestMembershipDeletes5.class);
    suite.addTestSuite(TestMembershipDeletes2.class);
    suite.addTestSuite(TestMembershipDeletes4.class);
    suite.addTestSuite(TestMembership5.class);
    suite.addTestSuite(TestMembership8.class);
    suite.addTestSuite(TestMembership6.class);
    suite.addTestSuite(TestMembership7.class);
    suite.addTestSuite(TestMemberOf.class);
    suite.addTestSuite(TestMembership11.class);
    suite.addTestSuite(TestMembership.class);
    suite.addTestSuite(TestMembership4.class);
    suite.addTestSuite(TestMemberOf0.class);
    suite.addTestSuite(TestMemberOf1.class);
    suite.addTestSuite(TestMembership3.class);
    suite.addTestSuite(TestMembership9.class);
    suite.addTestSuite(TestMembership10.class);
    suite.addTestSuite(TestMembership2.class);
    suite.addTestSuite(TestMembership0.class);
    suite.addTestSuite(TestMembership1.class);
    suite.addTestSuite(TestMembershipDeletes0.class);
    suite.addTestSuite(TestMembershipDeletes3.class);
    suite.addTestSuite(Test_Unit_API_ImmediateMembershipValidator_validate.class);
    //$JUnit-END$
    return suite;
  }

}
