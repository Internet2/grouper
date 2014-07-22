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

package edu.internet2.middleware.grouper.group;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MemberHelper;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Group.addMember()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupAddDeleteMember.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestGroupAddDeleteMember extends GrouperTest {

  /**
   * 
   */
  public TestGroupAddDeleteMember() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(new TestGroupAddDeleteMember("testAddMember"));
  }

  /**
   * 
   * @param name name
   */
  public TestGroupAddDeleteMember(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();
  }


  // Tests

  public void testAddMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Subject         subj  = SubjectTestHelper.getSubjectById(
      SubjectTestHelper.SUBJ_ROOT
    );
    Member          m     = MemberHelper.getMemberBySubject(s, subj);
    GroupHelper.addMember(i2, subj, m);
    // mships
    MembershipTestHelper.testNumMship(i2, Group.getDefaultList(), 1, 1, 0);
    MembershipTestHelper.testImmMship(s, i2, subj, Group.getDefaultList());
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

  // Tests
  
  public void testAddMember2() {
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

  // Tests
  
  public void testDeleteMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Subject         subj  = SubjectTestHelper.getSubjectById(
      SubjectTestHelper.SUBJ_ROOT
    );
    Member          m     = MemberHelper.getMemberBySubject(s, subj);
    GroupHelper.addMember(i2, subj, m);
    GroupHelper.deleteMember(i2, subj, m);
  } // public void testDeleteMember()

}

