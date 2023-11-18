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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
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
 * Test use of the UPDATE {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivUPDATE.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestPrivUPDATE extends GrouperTest {

  /**
   * 
   */
  public TestPrivUPDATE() {
    super();
    
  }

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    //junit.textui.TestRunner.run(new TestPrivUPDATE("testDelMembersWithADMIN"));
    junit.textui.TestRunner.run(TestPrivUPDATE.class);
  }

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestPrivUPDATE.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;


  public TestPrivUPDATE(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;
    m     = MemberHelper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    super.tearDown();
  }

  // Tests

  public void testAddMembersWithoutADMIN() {
    LOG.info("testAddMembersWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(nrs, a, subj1);
  } // public void testAddMembersWithoutADMIN
 
  public void testAddMembersWithADMIN() {
    LOG.info("testAddMembersWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1);
  } // public void testAddMembersWithADMIN

  public void testAddMembersWithoutUPDATE() {
    LOG.info("testAddMembersWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(nrs, a, subj1);
  } // public void testAddMembersWithoutUPDATE
 
  public void testAddMembersWithUPDATE() {
    LOG.info("testAddMembersWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1);
  } // public void testAddMembersWithUPDATE

  public void testDelMembersWithoutADMIN() {
    LOG.info("testDelMembersWithoutADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(nrs, a, subj1);
  } // public void testDelMembersWithoutADMIN
 
  public void testDelMembersWithADMIN() {
    LOG.info("testDelMembersWithADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    try {
    	Thread.currentThread().sleep(2000);
    }catch(InterruptedException e){}
    GroupHelper.delMemberUpdate(a, subj1);
  } // public void testDelMembersWithADMIN

  public void testDelMembersWithoutUPDATE() {
    LOG.info("testDelMembersWithoutUPDATE");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(nrs, a, subj1);
  } // public void testDelMembersWithoutUPDATE
 
  public void testDelMembersWithUPDATE() {
    LOG.info("testDelMembersWithUPDATE");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    try {
    	Thread.currentThread().sleep(3000);
    }catch(InterruptedException e){}
    GroupHelper.delMemberUpdate(a, subj1);
  } // public void testDelMembersWithUPDATE

  public void testGrantOptinWithoutADMIN() {
    LOG.info("testGrantOptinWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithoutADMIN()
 
  public void testGrantOptinWithADMIN() {
    LOG.info("testGrantOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithADMIN

  public void testGrantOptinWithoutUPDATE() {
    LOG.info("testGrantOptinWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithoutUPDATE()
 
  public void testGrantOptinWithUPDATE() {
    LOG.info("testGrantOptinWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithUPDATE

  public void testGrantOptoutWithoutADMIN() {
    LOG.info("testGrantOptoutWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithoutADMIN()
 
  public void testGrantOptoutWithADMIN() {
    LOG.info("testGrantOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithADMIN

  public void testGrantOptoutWithoutUPDATE() {
    LOG.info("testGrantOptoutWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithoutUPDATE()
 
  public void testGrantOptoutWithUPDATE() {
    LOG.info("testGrantOptoutWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithUPDATE
  
  public void testRevokeOptinWithoutADMIN() {
    LOG.info("testRevokeOptinWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithoutADMIN()
 
  public void testRevokeOptinWithADMIN() {
    LOG.info("testRevokeOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithADMIN
  
  public void testRevokeOptinWithoutUPDATE() {
    LOG.info("testRevokeOptinWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithoutUPDATE()
 
  public void testRevokeOptinWithUPDATE() {
    LOG.info("testRevokeOptinWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithUPDATE

  public void testRevokeOptoutWithoutADMIN() {
    LOG.info("testRevokeOptoutWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testRevokeOptoutWithoutADMIN()
 
  public void testRevokeOptoutWithADMIN() {
    LOG.info("testRevokeOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testRevokeOptoutWithADMIN

  public void testRevokeOptoutWithoutUPDATE() {
    LOG.info("testRevokeOptoutWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testRevokeOptoutWithoutUPDATE()
 
  public void testRevokeOptoutWithUPDATE() {
    LOG.info("testRevokeOptoutWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testRevokeOptoutWithUPDATE
}

