/*
 * @author mchyzer $Id: AllValidatorTests.java,v 1.2 2009-11-05 06:10:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.validator;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllValidatorTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.validator");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_subj_ValidatingResolver.class);
    suite.addTestSuite(Test_Unit_API_CompositeMembershipValidator_validate.class);
    suite.addTestSuite(Test_Integration_ImmediateMembershipValidator_validate.class);
    suite.addTest(Suite_Integration_ImmediateMembershipValidator.suite());
    suite.addTest(Suite_Unit_API_ImmediateMembershipValidator.suite());
    suite.addTest(Suite_Unit_API_EffectiveMembershipValidator.suite());
    suite.addTestSuite(Test_Unit_API_EffectiveMembershipValidator_validate.class);
    suite.addTest(Suite_Unit_API_CompositeMembershipValidator.suite());
    suite.addTest(Suite_Integration_CompositeValidator.suite());
    suite.addTestSuite(Test_Unit_API_CompositeValidator_validate.class);
    suite.addTestSuite(Test_Integration_CompositeValidator_validate.class);
    //$JUnit-END$
    return suite;
  }

}
