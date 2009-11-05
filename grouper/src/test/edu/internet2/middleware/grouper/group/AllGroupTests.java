/*
 * @author mchyzer $Id: AllGroupTests.java,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.group;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllGroupTests {

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
    suite.addTestSuite(TestGroupFinder.class);
    suite.addTestSuite(TestGroup1.class);
    suite.addTestSuite(TestWrongFieldType.class);
    suite.addTestSuite(TestGAttr.class);
    suite.addTestSuite(Test_I_API_Group_deleteAttribute.class);
    suite.addTestSuite(TestGroupModifyAttributes.class);
    suite.addTestSuite(TestGroup.class);
    suite.addTestSuite(Test_I_API_Group_addCompositeMember.class);
    suite.addTestSuite(TestGroupAddDeleteMember.class);
    suite.addTestSuite(Test_api_Group.class);
    suite.addTestSuite(Test_Integration_HibernateGroupDAO_delete.class);
    suite.addTestSuite(GroupDataTest.class);
    suite.addTestSuite(Test_I_API_Group_delete.class);
    suite.addTestSuite(Test_I_API_Group_deleteMember.class);
    suite.addTestSuite(Test_api_GrouperAPI.class);
    //$JUnit-END$
    return suite;
  }

}
