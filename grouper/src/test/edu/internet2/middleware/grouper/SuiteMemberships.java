/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  blair christensen.
 * @version $Id: SuiteMemberships.java,v 1.13 2008-10-25 16:31:39 shilen Exp $
 */
public class SuiteMemberships extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestMembership0.class);  // eff mship uuid does not change
    suite.addTestSuite(TestMembership1.class);  // parent and child memberships
    suite.addTestSuite(TestMembership2.class);  // creation time and creator
    suite.addTestSuite(TestMembership3.class);  // test of effective memberships without composite groups
    suite.addTestSuite(TestMembership4.class);  // test of effective memberships with composite groups
    suite.addTestSuite(TestMembership5.class);  // test of effective memberships with access privileges
    suite.addTestSuite(TestMembership6.class);  // test of effective memberships with access and naming privileges and composite groups
    suite.addTestSuite(TestMembership7.class);  // test of circular memberships without composite groups
    suite.addTestSuite(TestMembership8.class);  // test of circular memberships without composite groups where a group is added as a member to itself
    suite.addTestSuite(TestMembership9.class);  // test of composite with a composite type of interesection
    suite.addTestSuite(TestMembership10.class);  // test of composite with a composite type of complement
    suite.addTestSuite(TestMembership11.class);  // test of nested composite groups with complements, unions, and intersections

    suite.addTestSuite(TestMembershipDeletes0.class); // test of membership deletes with union composite groups
    suite.addTestSuite(TestMembershipDeletes1.class); // test of membership deletes with nested composite groups including complements and intersections
    suite.addTestSuite(TestMembershipDeletes2.class); // test of membership deletes with access and naming privileges and composite groups
    suite.addTestSuite(TestMembershipDeletes3.class); // test of membership deletes without composites but deletes that involve largely nested memberships
    suite.addTestSuite(TestMembershipDeletes4.class); // test of membership deletes with circular memberships but without composites
    suite.addTestSuite(TestMembershipDeletes5.class); // test of membership deletes with circular and self memberships but without composites

    suite.addTestSuite(TestFindBadMemberships0.class); // test with effective memberships without composite groups
    suite.addTestSuite(TestFindBadMemberships1.class); // test with effective memberships with composite groups
    suite.addTestSuite(TestFindBadMemberships2.class); // test with effective memberships with access privileges
    suite.addTestSuite(TestFindBadMemberships3.class); // test with effective memberships with access and naming privileges and composite groups

    suite.addTestSuite(TestMembership.class);
    return suite;
  } // static public Test suite()

}

