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

package edu.internet2.middleware.grouper.privs;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MemberHelper;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the READ {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivREAD.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestPrivREAD extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestPrivREAD.class);

  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;


  public TestPrivREAD(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0   = SubjectTestHelper.SUBJ0;
    subj1   = SubjectTestHelper.SUBJ1;
    m     = MemberHelper.getMemberBySubject(nrs, subj1);
    GroupHelper.addMember(i2, subj1, m);
    i2.setDescription("a description");
    i2.store();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new TestPrivREAD("testGrantedToCreator"));
    TestRunner.run(TestPrivREAD.class);
  }

  // Tests

  public void testReadAttrsWithoutADMIN() {
    LOG.info("testReadAttrsWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    // Succeeds because ALL has READ by default
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithoutADMIN()

  public void testReadAttrsWithADMIN() {
    LOG.info("testReadAttrsWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithADMIN()

  public void testReadAttrsWithAllGroupAttrRead() {
    LOG.info("testReadAttrsWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithAllADMIN()

  public void testReadAttrsWithoutREAD() {
    LOG.info("testReadAttrsWithoutREAD");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    // Succeeds because ALL has READ by default
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithoutREAD()

  public void testReadAttrsWithREAD() {
    LOG.info("testReadAttrsWithREAD");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithREAD()

  public void testReadAttrsWithAllREAD() {
    LOG.info("testReadAttrsWithAllREAD");
    // Created by default 
    // PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.READ);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.testAttrs(i2, a);
  } // public void testReadAttrsWithAllREAD()

  public void testReadMembersWithoutADMIN() {
    LOG.info("testReadMembersWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    Assert.assertTrue("members",      a.getMembers().size()               == 1);
    Assert.assertTrue("imm members",  a.getImmediateMembers().size()      == 1);
    Assert.assertTrue("eff members",  a.getEffectiveMembers().size()      == 0);
    Assert.assertTrue("mships",       a.getMemberships().size()           == 1);
    Assert.assertTrue("imm mships",   a.getImmediateMemberships().size()  == 1);
    Assert.assertTrue("eff mships",   a.getEffectiveMemberships().size()  == 0);
  } // public void testReadMembersWithoutADMIN()

  public void testReadMembersWithADMIN() {
    LOG.info("testReadMembersWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    Assert.assertTrue("members",      a.getMembers().size()               == 1);
    Assert.assertTrue("imm members",  a.getImmediateMembers().size()      == 1);
    Assert.assertTrue("eff members",  a.getEffectiveMembers().size()      == 0);
    Assert.assertTrue("mships",       a.getMemberships().size()           == 1);
    Assert.assertTrue("imm mships",   a.getImmediateMemberships().size()  == 1);
    Assert.assertTrue("eff mships",   a.getEffectiveMemberships().size()  == 0);
  } // public void testReadMembersWithADMIN()

  public void testReadMembersWithoutREAD() {
    LOG.info("testReadMembersWithoutREAD");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    Assert.assertTrue("members",      a.getMembers().size()               == 1);
    Assert.assertTrue("imm members",  a.getImmediateMembers().size()      == 1);
    Assert.assertTrue("eff members",  a.getEffectiveMembers().size()      == 0);
    Assert.assertTrue("mships",       a.getMemberships().size()           == 1);
    Assert.assertTrue("imm mships",   a.getImmediateMemberships().size()  == 1);
    Assert.assertTrue("eff mships",   a.getEffectiveMemberships().size()  == 0);
  } // public void testReadMembersWithoutREAD()

  public void testReadMembersWithREAD() {
    LOG.info("testReadMembersWithREAD");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.READ);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    Assert.assertTrue("members",      a.getMembers().size()               == 1);
    Assert.assertTrue("imm members",  a.getImmediateMembers().size()      == 1);
    Assert.assertTrue("eff members",  a.getEffectiveMembers().size()      == 0);
    Assert.assertTrue("mships",       a.getMemberships().size()           == 1);
    Assert.assertTrue("imm mships",   a.getImmediateMemberships().size()  == 1);
    Assert.assertTrue("eff mships",   a.getEffectiveMemberships().size()  == 0);
  } // public void testReadMembersWithREAD()

  public void testReadMembersWithAllREAD() {
    LOG.info("testReadMembersWithAllREAD");
    i2.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    Assert.assertTrue("members",      a.getMembers().size()               == 1);
    Assert.assertTrue("imm members",  a.getImmediateMembers().size()      == 1);
    Assert.assertTrue("eff members",  a.getEffectiveMembers().size()      == 0);
    Assert.assertTrue("mships",       a.getMemberships().size()           == 1);
    Assert.assertTrue("imm mships",   a.getImmediateMemberships().size()  == 1);
    Assert.assertTrue("eff mships",   a.getEffectiveMemberships().size()  == 0);
  } // public void testReadMembersWithAllREAD()

}

