/*
 * @author mchyzer
 * $Id: AllValidatorTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
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
    suite.addTest(Suite_Unit_API_ImmediateMembershipValidator.suite());
    suite.addTestSuite(Test_Unit_API_EffectiveMembershipValidator_validate.class);
    suite.addTestSuite(Test_Integration_ImmediateMembershipValidator_validate.class);
    suite.addTest(Suite_Unit_API_EffectiveMembershipValidator.suite());
    suite.addTestSuite(Test_subj_ValidatingResolver.class);
    suite.addTest(Suite_Unit_API_CompositeMembershipValidator.suite());
    suite.addTestSuite(Test_Unit_API_CompositeMembershipValidator_validate.class);
    suite.addTest(Suite_Integration_ImmediateMembershipValidator.suite());
    suite.addTestSuite(Test_Integration_CompositeValidator_validate.class);
    suite.addTest(Suite_Integration_CompositeValidator.suite());
    suite.addTestSuite(Test_Unit_API_CompositeValidator_validate.class);
    //$JUnit-END$
    return suite;
  }

}
