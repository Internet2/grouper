/*
 * @author mchyzer
 * $Id: AllGroupTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.group;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllGroupTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.group");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_I_API_Group_addCompositeMember.class);
    suite.addTestSuite(Test_I_API_Group_deleteAttribute.class);
    suite.addTestSuite(TestGAttr.class);
    suite.addTestSuite(Test_api_GrouperAPI.class);
    suite.addTestSuite(TestGroup1.class);
    suite.addTestSuite(TestGroupAddDeleteMember.class);
    suite.addTestSuite(Test_api_Group.class);
    suite.addTestSuite(TestGroup.class);
    suite.addTestSuite(Test_I_API_Group_deleteMember.class);
    suite.addTestSuite(Test_I_API_Group_delete.class);
    suite.addTestSuite(TestGroupModifyAttributes.class);
    suite.addTestSuite(Test_Integration_HibernateGroupDAO_delete.class);
    suite.addTestSuite(TestGroupFinder.class);
    suite.addTestSuite(TestWrongFieldType.class);
    suite.addTestSuite(GroupDataTest.class);
    //$JUnit-END$
    return suite;
  }

}
