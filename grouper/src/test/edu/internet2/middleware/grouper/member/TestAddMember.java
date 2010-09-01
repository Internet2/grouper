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

package edu.internet2.middleware.grouper.member;
import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MemberHelper;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestAddMember.java,v 1.2 2009-11-05 13:44:48 shilen Exp $
 */
public class TestAddMember extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestAddMember.class);

  public TestAddMember(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddMember() {
    LOG.info("testAddMember");
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

  public void testAddGroupAsMember() {
    LOG.info("testAddGroupAsMember");
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
  } // public void testAddGroupAsMember()

  public void testAddGroupMemberWithNonGroupMember() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Subject         subj  = SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ROOT);
    MemberHelper.getMemberBySubject(s, subj);
    // add subj to uofc   
    GroupHelper.addMember(uofc, subj, "members");
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
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
      Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
      Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
      Subject         subj  = SubjectTestHelper.SUBJ0;
      MemberHelper.getMemberBySubject(s, subj);
      // add uofc to i2
      GroupHelper.addMember(i2, uofc);
      MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 0, 0, 0);
      MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 1, 1, 0);
      MembershipTestHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
      // add subj to uofc   
      GroupHelper.addMember(uofc, subj, "members");
      MembershipTestHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
      MembershipTestHelper.testNumMship(i2,   Group.getDefaultList(), 2, 1, 1);
      MembershipTestHelper.testImmMship(s, uofc, subj, Group.getDefaultList());
      MembershipTestHelper.testImmMship(s, i2,   uofc, Group.getDefaultList());
      MembershipTestHelper.testEffMship(s, i2, subj, Group.getDefaultList(), uofc, 1);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddMemberToGroupThatIsMember()

  public void testNoListRecursion() {
    LOG.info("testNoListRecursion");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            ns    = StemHelper.addChildStem(root  , "ns_a", "stem a");
    Group           g     = StemHelper.addChildGroup(ns   , "g_a" , "group a");
    try {
      g.addMember(g.toSubject());
      Assert.fail("fail: MemberAddException not thrown");
    }
    catch (RuntimeException e) {
      if (e.getCause() instanceof MemberAddException) {
        Assert.assertTrue("pass: MemberAddException thrown", true);
      } else {
        throw e;
      }
    }
    finally {
      s.stop();
    }
  } // public void testNoListRecursion()

}

