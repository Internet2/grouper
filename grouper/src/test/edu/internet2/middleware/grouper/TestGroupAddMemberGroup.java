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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;

/**
 * Test {@link Group.addMember()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupAddMemberGroup.java,v 1.4 2006-09-06 19:50:21 blair Exp $
 */
public class TestGroupAddMemberGroup extends TestCase {

  public TestGroupAddMemberGroup(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testAddMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    GroupHelper.addMember(i2, uofc);
    // mships
    MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 1,    1, 0);
    MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 0,    0, 0);
    MembershipTestHelper.testImmMship(s, i2, uofc, Group.getDefaultList());
  } // public void testAddMember()

  public void testAddGroupMemberWithNonGroupMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Subject         subj  = SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ROOT);
    Member          m     = MemberHelper.getMemberBySubject(s, subj);
    // add subj to uofc   
    GroupHelper.addMember(uofc, subj, m);
    MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 0, 0, 0);
    MembershipTestHelper.testImmMship(s, uofc, subj, Group.getDefaultList());
    // add uofc to i2
    GroupHelper.addMember(i2, uofc);
    MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(s, uofc, subj, Group.getDefaultList());
    MembershipTestHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
    MembershipTestHelper.testEffMship(s, i2, subj, Group.getDefaultList(), uofc, 1);
  } // public void testAddGroupMemberWithNonGroupMember()

  public void testAddMemberToGroupThatIsMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Subject         subj  = SubjectTestHelper.SUBJ0;
    Member          m     = MemberHelper.getMemberBySubject(s, subj);
    // add uofc to i2
    GroupHelper.addMember(i2, uofc);
    MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 0, 0, 0);
    MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 1, 1, 0);
    MembershipTestHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
    // add subj to uofc   
    GroupHelper.addMember(uofc, subj, m);
    MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 2, 1, 1);
    MembershipTestHelper.testImmMship(s, uofc, subj, Group.getDefaultList());
    MembershipTestHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
    MembershipTestHelper.testEffMship(s, i2, subj, Group.getDefaultList(), uofc, 1);
  } // public void testAddMemberToGroupThatIsMember()

}

