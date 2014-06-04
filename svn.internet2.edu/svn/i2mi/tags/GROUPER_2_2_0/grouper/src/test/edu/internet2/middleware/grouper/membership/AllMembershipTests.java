/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer $Id: AllMembershipTests.java,v 1.6 2009-11-05 06:10:51 mchyzer Exp $
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
    suite.addTestSuite(TestMembership1.class);
    suite.addTestSuite(TestMembership12.class);
    suite.addTestSuite(TestMembership10.class);
    suite.addTestSuite(Test_Unit_API_ImmediateMembershipValidator_validate.class);
    suite.addTestSuite(TestFindBadMemberships.class);
    suite.addTestSuite(TestMembership4.class);
    suite.addTestSuite(TestDisabledMembership.class);
    suite.addTestSuite(TestMemberOf.class);
    suite.addTestSuite(TestMembership.class);
    suite.addTestSuite(TestMembershipDeletes2.class);
    suite.addTestSuite(TestMemberOf0.class);
    suite.addTestSuite(TestMembership7.class);
    suite.addTestSuite(TestMembership2.class);
    suite.addTestSuite(TestMembershipDeletes4.class);
    suite.addTestSuite(TestMembershipDeletes1.class);
    suite.addTestSuite(TestMembership0.class);
    suite.addTestSuite(TestMembership6.class);
    suite.addTestSuite(TestMembership9.class);
    suite.addTestSuite(AddMissingGroupSetsTest.class);
    suite.addTestSuite(TestMembershipDeletes0.class);
    suite.addTestSuite(TestMembershipFinder.class);
    suite.addTestSuite(TestMembership3.class);
    suite.addTestSuite(TestMembership5.class);
    suite.addTestSuite(TestMemberChangeInMembership.class);
    suite.addTestSuite(TestMembership11.class);
    suite.addTestSuite(TestMembership8.class);
    suite.addTestSuite(TestMemberOf1.class);
    suite.addTestSuite(TestMembershipDeletes3.class);
    suite.addTestSuite(TestMembershipDeletes5.class);
    //$JUnit-END$
    return suite;
  }

}
