/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  junit.framework.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteMemberships.java,v 1.3 2006-08-22 19:48:22 blair Exp $
 */
public class SuiteMemberships extends TestCase {

  public SuiteMemberships(String name) {
    super(name);
  } // public SuiteMemberships(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestMembership0.class );  // eff mship uuid does not change
    suite.addTestSuite( TestMembership1.class );  // parent and child memberships
    // TODO Split
    suite.addTestSuite(TestMembership.class);
    return suite;
  } // static public Test suite()

}

