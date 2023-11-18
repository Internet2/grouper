/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    suite.addTest(Suite_Integration_ImmediateMembershipValidator.suite());
    suite.addTest(Suite_Unit_API_ImmediateMembershipValidator.suite());
    suite.addTest(Suite_Unit_API_EffectiveMembershipValidator.suite());
    suite.addTest(Suite_Unit_API_CompositeMembershipValidator.suite());
    suite.addTest(Suite_Integration_CompositeValidator.suite());
    suite.addTestSuite(Test_Unit_API_CompositeValidator_validate.class);
    //$JUnit-END$
    return suite;
  }

}
