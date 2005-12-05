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
import  org.apache.commons.logging.*;


/**
 * Test use of the UPDATE {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivUPDATE.java,v 1.2 2005-12-05 05:48:35 blair Exp $
 */
public class TestPrivUPDATE extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestPrivUPDATE.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestPrivUPDATE(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
    s     = SessionHelper.getRootSession();
    nrs   = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
    m     = Helper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testAddMembersWithoutADMIN() {
    LOG.info("testAddMembersWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(a, subj1, m);
  } // public void testAddMembersWithoutADMIN
 
  public void testAddMembersWithADMIN() {
    LOG.info("testAddMembersWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1, m);
  } // public void testAddMembersWithADMIN

  public void testAddMembersWithAllADMIN() {
    LOG.info("testAddMembersWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1, m);
  } // public void testAddMembersWithAllADMIN

  public void testAddMembersWithoutUPDATE() {
    LOG.info("testAddMembersWithoutUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdateFail(a, subj1, m);
  } // public void testAddMembersWithoutUPDATE
 
  public void testAddMembersWithUPDATE() {
    LOG.info("testAddMembersWithUPDATE");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1, m);
  } // public void testAddMembersWithUPDATE

  public void testAddMembersWithAllUPDATE() {
    LOG.info("testAddMembersWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.addMemberUpdate(a, subj1, m);
  } // public void testAddMembersWithAllUPDATE

  public void testDelMembersWithoutADMIN() {
    LOG.info("testDelMembersWithoutADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(a, subj1, m);
  } // public void testDelMembersWithoutADMIN
 
  public void testDelMembersWithADMIN() {
    LOG.info("testDelMembersWithADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj1, m);
  } // public void testDelMembersWithADMIN

  public void testDelMembersWithAllADMIN() {
    LOG.info("testDelMembersWithAllADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj1, m);
  } // public void testDelMembersWithAllADMIN

  public void testDelMembersWithoutUPDATE() {
    LOG.info("testDelMembersWithoutUPDATE");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdateFail(a, subj1, m);
  } // public void testDelMembersWithoutUPDATE
 
  public void testDelMembersWithUPDATE() {
    LOG.info("testDelMembersWithUPDATE");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj1, m);
  } // public void testDelMembersWithUPDATE

  public void testDelMembersWithAllUPDATE() {
    LOG.info("testDelMembersWithAllUPDATE");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delMemberUpdate(a, subj1, m);
  } // public void testDelMembersWithAllUPDATE

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

  public void testGrantOptinWithAllADMIN() {
    LOG.info("testGrantOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithAllADMIN

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

  public void testGrantOptinWithAllUDPATE() {
    LOG.info("testGrantOptinWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testGrantOptinWithAllUPDATE

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

  public void testGrantOptoutWithAllADMIN() {
    LOG.info("testGrantOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithAllADMIN

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

  public void testGrantOptoutWithAllUDPATE() {
    LOG.info("testGrantOptoutWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithAllUPDATE

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

  public void testRevokeOptinWithAllADMIN() {
    LOG.info("testRevokeOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithAllADMIN

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

  public void testRevokeOptinWithAllUDPATE() {
    LOG.info("testRevokeOptinWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);
  } // public void testRevokeOptinWithAllUPDATE

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

  public void testRevokeOptoutWithAllADMIN() {
    LOG.info("testRevokeOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testGrantOptoutWithAllADMIN

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

  public void testRevokeOptoutWithAllUDPATE() {
    LOG.info("testRevokeOptoutWithAllUPDATE");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    Group a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);
  } // public void testRevokeOptoutWithAllUPDATE

}

