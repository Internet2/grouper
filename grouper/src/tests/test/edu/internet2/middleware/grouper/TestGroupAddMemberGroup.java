/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link Group.addMember()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupAddMemberGroup.java,v 1.1.2.4 2005-11-10 16:36:18 blair Exp $
 */
public class TestGroupAddMemberGroup extends TestCase {

  public TestGroupAddMemberGroup(String name) {
    super(name);
  }

  protected void setUp () {
    Db.refreshDb();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testAddMember() {
    GrouperSession  s     = Helper.getRootSession();
    Stem            root  = StemHelper.getRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    GroupHelper.addMember(i2, uofc);
    // mships
    MembershipHelper.testNumMship(i2,   Group.LIST, 1,    1, 0);
    MembershipHelper.testNumMship(uofc, Group.LIST, 0,    0, 0);
    MembershipHelper.testImmMship(s,    i2,         uofc, Group.LIST);
  } // public void testAddMember()

  public void testAddMemberWithNonGroupMember() {
    GrouperSession  s     = Helper.getRootSession();
    Stem            root  = StemHelper.getRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Member          m     = Helper.getMemberBySubject(
      s, SubjectHelper.getSubjectById(Helper.GOOD_SUBJ_ID)
    );
    GroupHelper.addMember(uofc, m);
    GroupHelper.addMember(i2, uofc);
    // mships
    MembershipHelper.testNumMship(i2,   Group.LIST, 2,    1, 1);
    MembershipHelper.testNumMship(uofc, Group.LIST, 1,    1, 0);
    MembershipHelper.testImmMship(s,    i2,         uofc, Group.LIST);
    MembershipHelper.testImmMship(s,    uofc,       m,    Group.LIST);
    MembershipHelper.testEffMship(s,    i2,         m,    Group.LIST, uofc, 1);
  } // public void testAddMember()

}

