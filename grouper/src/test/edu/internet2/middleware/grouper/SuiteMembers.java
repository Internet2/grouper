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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteMembers.java,v 1.1 2006-06-13 17:40:39 blair Exp $
 */
public class SuiteMembers extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteMembers.class); 

  public SuiteMembers(String name) {
    super(name);
  } // public SuiteMembers(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestMember0.class );    // fail: Member.canCreate(Stem) == null
    suite.addTestSuite( TestMember1.class );    // fail: Member.canCreate(Stem) == !priv'd
    suite.addTestSuite( TestMember2.class );    // Member.canCreate(Stem) 
    suite.addTestSuite( TestMember3.class );    // fail: Member.canStem(Stem) == null
    suite.addTestSuite( TestMember4.class );    // fail: Member.canStem(Stem) == !priv'd
    suite.addTestSuite( TestMember5.class );    // Member.canStem(Stem) 
    suite.addTestSuite( TestMember6.class );    // fail: Member.canAdmin(Group) == null
    suite.addTestSuite( TestMember7.class );    // fail: Member.canAdmin(Group) == !priv'd
    suite.addTestSuite( TestMember8.class );    // Member.canAdmin(Group) 
    suite.addTestSuite( TestMember9.class );    // fail: Member.canOptin(Group) == null
    suite.addTestSuite( TestMember10.class );   // fail: Member.canOptin(Group) == !priv'd
    suite.addTestSuite( TestMember11.class );   // Member.canOptin(Group) 
    suite.addTestSuite( TestMember12.class );   // fail: Member.canOptout(Group) == null
    suite.addTestSuite( TestMember13.class );   // fail: Member.canOptout(Group) == !priv'd
    suite.addTestSuite( TestMember14.class );   // Member.canOptout(Group) 
    suite.addTestSuite( TestMember15.class );   // fail: Member.canRead(Group) == null
    suite.addTestSuite( TestMember16.class );   // fail: Member.canRead(Group) == !priv'd
    suite.addTestSuite( TestMember17.class );   // Member.canRead(Group) 
    suite.addTestSuite( TestMember18.class );   // fail: Member.canUpdate(Group) == null
    suite.addTestSuite( TestMember19.class );   // fail: Member.canUpdate(Group) == !priv'd
    suite.addTestSuite( TestMember20.class );   // Member.canUpdate(Group) 
    suite.addTestSuite( TestMember21.class );   // fail: Member.canView(Group) == null
    suite.addTestSuite( TestMember22.class );   // fail: Member.canView(Group) == !priv'd
    suite.addTestSuite( TestMember23.class );   // Member.canView(Group) 
    // TODO Split
    suite.addTestSuite( TestMember.class        );
    suite.addTestSuite( TestMemberToGroup.class );
    return suite;
  } // static public Test suite()

}

