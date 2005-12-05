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
 * Test use of the ADMIN {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestPrivADMIN.java,v 1.2 2005-12-05 14:56:37 blair Exp $
 */
public class TestPrivADMIN extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestPrivADMIN.class);


  // Private Class Variables
  private static Group          a;
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;


  public TestPrivADMIN(String name) {
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
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.VIEW);
    a     = GroupHelper.findByName(nrs, i2.getName());
    m     = Helper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testGrantedToCreator() {
    LOG.info("testGrantedToCreator");
    PrivHelper.grantPriv(s, edu, subj0, NamingPrivilege.CREATE);
    Stem  stem  = StemHelper.findByName(nrs, edu.getName());
    Group group = StemHelper.addChildGroup(stem, "group", "a group");
    PrivHelper.hasPriv(nrs, group, nrs.getSubject(), AccessPrivilege.ADMIN, true);
  } // public void testGrantedToCreator()

  public void testGrantAdminWithoutADMIN() {
    LOG.info("testGrantAdminWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantAdminWithADMIN() {
    LOG.info("testGrantAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithADMIN()

  public void testGrantAdminWithAllADMIN() {
    LOG.info("testGrantAdminWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithADMIN()

  public void testGrantOptinWithoutADMIN() {
    LOG.info("testGrantOptinWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantOptinWithADMIN() {
    LOG.info("testGrantOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantOptinWithADMIN()

  public void testGrantOptinWithAllADMIN() {
    LOG.info("testGrantOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantOptinWithAllADMIN()

  public void testGrantOptoutWithoutADMIN() {
    LOG.info("testGrantOptoutWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithoutADMIN()

  public void testGrantOptoutWithADMIN() {
    LOG.info("testGrantOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithADMIN()

  public void testGrantOptoutWithAllADMIN() {
    LOG.info("testGrantOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithAllADMIN()

  public void testGrantReadWithoutADMIN() {
    LOG.info("testGrantReadWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithoutREAD()

  public void testGrantReadWithADMIN() {
    LOG.info("testGrantReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithADMIN()

  public void testGrantReadWithAllADMIN() {
    LOG.info("testGrantReadWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithAllADMIN()

  public void testGrantUpdateWithoutADMIN() {
    LOG.info("testGrantUpdateWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithoutADMIN()

  public void testGrantUpdateWithADMIN() {
    LOG.info("testGrantUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithADMIN()

  public void testGrantUpdateWithAllADMIN() {
    LOG.info("testGrantUpdateWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithADMIN()

  public void testGrantViewWithoutADMIN() {
    LOG.info("testGrantViewWithoutADMIN");
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithoutADMIN()

  public void testGrantViewWithADMIN() {
    LOG.info("testGrantViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithADMIN()

  public void testGrantViewWithAllADMIN() {
    LOG.info("testGrantViewWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithADMIN()

  public void testRevokeAdminWithoutADMIN() {
    LOG.info("testRevokeAdminWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeAdminWithADMIN() {
    LOG.info("testRevokeAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithADMIN()

  public void testRevokeAdminWithAllADMIN() {
    LOG.info("testRevokeAdminWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePrivAllHasPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithADMIN()

  public void testRevokeOptinWithoutADMIN() {
    LOG.info("testRevokeOptinWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeOptinWithADMIN() {
    LOG.info("testRevokeOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeOptinWithADMIN()

  public void testRevokeOptinWithAllADMIN() {
    LOG.info("testRevokeOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeOptinWithAllADMIN()

  public void testRevokeOptoutWithoutADMIN() {
    LOG.info("testRevokeOptoutWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithoutADMIN()

  public void testRevokeOptoutWithADMIN() {
    LOG.info("testRevokeOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithADMIN()

  public void testRevokeOptoutWithAllADMIN() {
    LOG.info("testRevokeOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithAllADMIN()

  public void testRevokeReadWithoutADMIN() {
    LOG.info("testRevokeReadWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithoutREAD()

  public void testRevokeReadWithADMIN() {
    LOG.info("testRevokeReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithADMIN()

  public void testRevokeReadWithAllADMIN() {
    LOG.info("testRevokeReadWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithAllADMIN()

  public void testRevokeUpdateWithoutADMIN() {
    LOG.info("testRevokeUpdateWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithoutADMIN()

  public void testRevokeUpdateWithADMIN() {
    LOG.info("testRevokeUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithADMIN()

  public void testRevokeUpdateWithAllADMIN() {
    LOG.info("testRevokeUpdateWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithADMIN()

  public void testRevokeViewWithoutADMIN() {
    LOG.info("testRevokeViewWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithoutADMIN()

  public void testRevokeViewWithADMIN() {
    LOG.info("testRevokeViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithADMIN()

  public void testRevokeViewWithAllADMIN() {
    LOG.info("testRevokeViewWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithADMIN()

  public void testDeleteGroupWithoutADMIN() {
    LOG.info("testDeleteGroupWithoutADMIN");
    GroupHelper.deleteFail(nrs, a, i2.getName());
  } // public void testDeleteGroupWithoutADMIN()

  public void testDeleteGroupWithADMIN() {
    LOG.info("testDeleteGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithADMIN()

  public void testDeleteGroupWithAllADMIN() {
    LOG.info("testDeleteGroupWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithAllADMIN()

  // Set + delete group attributes
  // Rename group

}

