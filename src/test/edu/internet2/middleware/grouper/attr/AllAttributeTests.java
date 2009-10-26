package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.attr.assign.AllAttrAssignTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 * @author mchyzer
 *
 */
public class AllAttributeTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.attr");
    suite.addTest(AllAttrAssignTests.suite());

    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeAssignValueTest.class);
    suite.addTestSuite(EffMshipAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefTest.class);
    suite.addTestSuite(AttributeDefScopeTest.class);
    suite.addTestSuite(MemberAttributeSecurityTest.class);
    suite.addTestSuite(GroupAttributeSecurityTest.class);
    suite.addTestSuite(AttributeAssignTest.class);
    suite.addTestSuite(AttributeDefNameSetTest.class);
    suite.addTestSuite(AttrAssignAttributeSecurityTest.class);
    suite.addTestSuite(AttributeDefNameTest.class);
    suite.addTestSuite(StemAttributeSecurityTest.class);
    suite.addTestSuite(MembershipAttributeSecurityTest.class);
    //$JUnit-END$

    return suite;
  }

}
