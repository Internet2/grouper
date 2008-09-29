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
 * @version $Id: SuiteMemberships.java,v 1.10 2008-09-29 03:38:27 mchyzer Exp $
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

    suite.addTestSuite(TestFindBadMemberships0.class); // test with effective memberships without composite groups
    suite.addTestSuite(TestFindBadMemberships1.class); // test with effective memberships with composite groups
    suite.addTestSuite(TestFindBadMemberships2.class); // test with effective memberships with access privileges
    suite.addTestSuite(TestFindBadMemberships3.class); // test with effective memberships with access and naming privileges and composite groups

    suite.addTestSuite(TestMembership.class);
    return suite;
  } // static public Test suite()

}

