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
 * @version $Id: SuiteMembers.java,v 1.8 2006-11-13 16:47:50 blair Exp $
 */
public class SuiteMembers extends TestCase {

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
    suite.addTestSuite( TestMember9.class  );   // fail: Member.canOptin(Group) == null
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
    // setSubjectId()
    suite.addTestSuite( TestMember24.class );   // setSubjectId() - FAIL: !root
    suite.addTestSuite( TestMember25.class );   // setSubjectId() - OK: root
    suite.addTestSuite( TestMember26.class );   // setSubjectId() - FAIL: m == GrouperSystem
    suite.addTestSuite( TestMember27.class );   // setSubjectId() - FAIL: m == GrouperAll
    // setSubjectSourceId()
    suite.addTestSuite( TestMember28.class );   // setSubjectSourceId() - FAIL: null val
    suite.addTestSuite( TestMember29.class );   // setSubjectSourceId() - FAIL: !root
    suite.addTestSuite( TestMember30.class );   // setSubjectSourceId() - FAIL: m == GrouperSystem
    suite.addTestSuite( TestMember31.class );   // setSubjectSourceId() - FAIL: m == GrouperAll
    suite.addTestSuite( TestMember32.class );   // setSubjectSourceId() - OK: root

    // TODO 20060927 Split pre-existing tests
    suite.addTestSuite( TestMember.class        );
    suite.addTestSuite( TestMemberToGroup.class );
    return suite;
  } // static public Test suite()

} // public class SuiteMembers

