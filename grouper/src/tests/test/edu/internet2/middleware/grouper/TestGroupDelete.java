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
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Group.delete()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGroupDelete.java,v 1.7 2005-12-09 07:35:38 blair Exp $
 */
public class TestGroupDelete extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupDelete.class);


  public TestGroupDelete(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testGroupDelete() {
    LOG.info("testGroupDelete");
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "educational");
    Group i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    try {
      i2.delete();
      Assert.assertTrue("group deleted", true);
    }
    catch (Exception e) {
      Assert.fail("failed to delete group: " + e.getMessage());
    }
  } // public void testGroupDelete()

  public void testGroupDeleteWhenMemberAndHasMembers() {
    LOG.info("testGroupDeleteWhenMemberAndHasMembers");
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.0");
    GrouperSession  s     = SessionHelper.getRootSession();
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.1");
    Stem            root  = StemHelper.findRootStem(s);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.2");
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.3");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.4");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.5");
    Member          m     = Helper.getMemberBySubject(s, SubjectHelper.SUBJ0);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.6");
    GroupHelper.addMember(uofc, SubjectHelper.SUBJ0, m);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.7");
    MembershipHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.8");
    MembershipHelper.testNumMship(i2,   Group.getDefaultList(), 0, 0, 0);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.9");
    MembershipHelper.testImmMship(s, uofc, SubjectHelper.SUBJ0, Group.getDefaultList());
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.10");
    GroupHelper.addMember(i2, uofc);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.11");
    MembershipHelper.testNumMship(uofc, Group.getDefaultList(), 1, 1, 0);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.12");
    MembershipHelper.testNumMship(i2,   Group.getDefaultList(), 2, 1, 1);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.13");
    MembershipHelper.testImmMship(s, uofc, SubjectHelper.SUBJ0, Group.getDefaultList());
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.14");
    MembershipHelper.testImmMship(s, i2,   uofc,                Group.getDefaultList());
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.15");
    MembershipHelper.testEffMship(
      s, i2, SubjectHelper.SUBJ0, Group.getDefaultList(), uofc, 1
    );
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.16");
    try {
      uofc.delete();
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.17");
      Assert.assertTrue("group deleted", true);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.18");
      MembershipHelper.testNumMship(i2,   Group.getDefaultList(), 0, 0, 0);
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.19");
    }
    catch (Exception e) {
    LOG.debug("testGroupDeleteWhenMemberAndHasMembers.20");
      Assert.fail(e.getMessage());
    }
  } // public void testGroupDeleteWhenMemberAndHasMembers()

}

