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
 * @version $Id: TestPrivADMIN.java,v 1.10 2006-02-21 17:11:33 blair Exp $
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
  private static Group          uofc;


  public TestPrivADMIN(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    nrs   = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
    m     = Helper.getMemberBySubject(nrs, subj1);
    s.waitForTx();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  // Tests

  public void testGrantedToCreator() {
    LOG.info("testGrantedToCreator");
    PrivHelper.grantPriv(s, edu, subj0, NamingPrivilege.CREATE);
    a = GroupHelper.findByName(nrs, i2.getName());
    LOG.info("testGrantedToCreator.0");
    Stem  stem  = StemHelper.findByName(nrs, edu.getName());
    LOG.info("testGrantedToCreator.1");
    Group group = StemHelper.addChildGroup(stem, "group", "a group");
    LOG.info("testGrantedToCreator.2");
    PrivHelper.hasPriv(nrs, group, nrs.getSubject(), AccessPrivilege.ADMIN, true);
    LOG.info("testGrantedToCreator.3");
  } // public void testGrantedToCreator()

  public void testGrantAdminWithoutADMIN() {
    LOG.info("testGrantAdminWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantAdminWithADMIN() {
    LOG.info("testGrantAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithADMIN()

  public void testGrantAdminWithAllADMIN() {
    LOG.info("testGrantAdminWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testGrantAdminWithADMIN()

  public void testGrantOptinWithoutADMIN() {
    LOG.info("testGrantOptinWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantAdminWithoutADMIN()

  public void testGrantOptinWithADMIN() {
    LOG.info("testGrantOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantOptinWithADMIN()

  public void testGrantOptinWithAllADMIN() {
    LOG.info("testGrantOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testGrantOptinWithAllADMIN()

  public void testGrantOptoutWithoutADMIN() {
    LOG.info("testGrantOptoutWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithoutADMIN()

  public void testGrantOptoutWithADMIN() {
    LOG.info("testGrantOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithADMIN()

  public void testGrantOptoutWithAllADMIN() {
    LOG.info("testGrantOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testGrantOptoutWithAllADMIN()

  public void testGrantReadWithoutADMIN() {
    LOG.info("testGrantReadWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithoutREAD()

  public void testGrantReadWithADMIN() {
    LOG.info("testGrantReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithADMIN()

  public void testGrantReadWithAllADMIN() {
    LOG.info("testGrantReadWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testGrantReadWithAllADMIN()

  public void testGrantUpdateWithoutADMIN() {
    LOG.info("testGrantUpdateWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithoutADMIN()

  public void testGrantUpdateWithADMIN() {
    LOG.info("testGrantUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithADMIN()

  public void testGrantUpdateWithAllADMIN() {
    LOG.info("testGrantUpdateWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testGrantUpdateWithADMIN()

  public void testGrantViewWithoutADMIN() {
    LOG.info("testGrantViewWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithoutADMIN()

  public void testGrantViewWithADMIN() {
    LOG.info("testGrantViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithADMIN()

  public void testGrantViewWithAllADMIN() {
    LOG.info("testGrantViewWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.grantPriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testGrantViewWithADMIN()

  public void testRevokeAdminWithoutADMIN() {
    LOG.info("testRevokeAdminWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeAdminWithADMIN() {
    LOG.info("testRevokeAdminWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithADMIN()

  public void testRevokeAdminWithAllADMIN() {
    LOG.info("testRevokeAdminWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivAllHasPriv(nrs, a, subj1, AccessPrivilege.ADMIN);     
  } // public void testRevokeAdminWithADMIN()

  public void testRevokeOptinWithoutADMIN() {
    LOG.info("testRevokeOptinWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeAdminWithoutADMIN()

  public void testRevokeOptinWithADMIN() {
    LOG.info("testRevokeOptinWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeOptinWithADMIN()

  public void testRevokeOptinWithAllADMIN() {
    LOG.info("testRevokeOptinWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTIN);     
  } // public void testRevokeOptinWithAllADMIN()

  public void testRevokeOptoutWithoutADMIN() {
    LOG.info("testRevokeOptoutWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithoutADMIN()

  public void testRevokeOptoutWithADMIN() {
    LOG.info("testRevokeOptoutWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithADMIN()

  public void testRevokeOptoutWithAllADMIN() {
    LOG.info("testRevokeOptoutWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.OPTOUT);     
  } // public void testRevokeOptoutWithAllADMIN()

  public void testRevokeReadWithoutADMIN() {
    LOG.info("testRevokeReadWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithoutREAD()

  public void testRevokeReadWithADMIN() {
    LOG.info("testRevokeReadWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithADMIN()

  public void testRevokeReadWithAllADMIN() {
    LOG.info("testRevokeReadWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.READ);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.READ);     
  } // public void testRevokeReadWithAllADMIN()

  public void testRevokeUpdateWithoutADMIN() {
    LOG.info("testRevokeUpdateWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithoutADMIN()

  public void testRevokeUpdateWithADMIN() {
    LOG.info("testRevokeUpdateWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithADMIN()

  public void testRevokeUpdateWithAllADMIN() {
    LOG.info("testRevokeUpdateWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.UPDATE);     
  } // public void testRevokeUpdateWithADMIN()

  public void testRevokeViewWithoutADMIN() {
    LOG.info("testRevokeViewWithoutADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePrivFail(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithoutADMIN()

  public void testRevokeViewWithADMIN() {
    LOG.info("testRevokeViewWithADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithADMIN()

  public void testRevokeViewWithAllADMIN() {
    LOG.info("testRevokeViewWithAllADMIN");
    PrivHelper.grantPriv(s, i2, subj1, AccessPrivilege.VIEW);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    PrivHelper.revokePriv(nrs, a, subj1, AccessPrivilege.VIEW);     
  } // public void testRevokeViewWithADMIN()

  public void testDeleteGroupWithoutADMIN() {
    LOG.info("testDeleteGroupWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.deleteFail(nrs, a, i2.getName());
  } // public void testDeleteGroupWithoutADMIN()

  public void testDeleteGroupWithADMIN() {
    LOG.info("testDeleteGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithADMIN()

  public void testDeleteGroupWithAllADMIN() {
    LOG.info("testDeleteGroupWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithAllADMIN()

  public void testDeleteGroupWithMemberWithoutADMIN() {
    LOG.info("testDeleteGroupWithMemberWithoutADMIN");
    GroupHelper.addMember(i2, subj1, m);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.deleteFail(nrs, a, i2.getName());
  } // public void testDeleteGroupWithMemberWithoutADMIN()

  public void testDeleteGroupWithMemberWithADMIN() {
    LOG.info("testDeleteGroupWithMemberWithADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithMemberWithADMIN()

  public void testDeleteGroupWithMemberWithAllADMIN() {
    LOG.info("testDeleteGroupWithMemberWithAllADMIN");
    GroupHelper.addMember(i2, subj1, m);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupWithMemberWithAllADMIN()

  public void testDeleteGroupIsMemberWithoutADMIN() {
    LOG.info("testDeleteGroupIsMemberWithoutADMIN");
    GroupHelper.addMember(uofc, i2);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.deleteFail(nrs, a, i2.getName());
  } // public void testDeleteGroupIsMemberWithoutADMIN()

  public void testDeleteGroupIsMemberWithADMIN() {
    LOG.info("testDeleteGroupIsMemberWithADMIN");
    GroupHelper.addMember(uofc, i2);
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupIsMemberWithADMIN()

  public void testDeleteGroupIsMemberWithAllADMIN() {
    LOG.info("testDeleteGroupIsMemberWithAllADMIN");
    GroupHelper.addMember(uofc, i2);
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delete(nrs, a, i2.getName());
  } // public void testDeleteGroupIsMemberWithAllADMIN()

  public void testSetAttrsWithoutADMIN() {
    LOG.info("testSetAttrsWithoutADMIN");
    String val = "new value";
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.setAttrFail(a, ""                 , ""  );
    GroupHelper.setAttrFail(a, ""                 , null);
    GroupHelper.setAttrFail(a, null               , null);
    GroupHelper.setAttrFail(a, null               , ""  );
    GroupHelper.setAttrFail(a, "attr"             , val );
    GroupHelper.setAttrFail(a, "description"      , val );
    GroupHelper.setAttrFail(a, "displayName"      , val );
    GroupHelper.setAttrFail(a, "displayExtension" , val );
    GroupHelper.setAttrFail(a, "extension"        , val );
    GroupHelper.setAttrFail(a, "name"             , val );
  } // public void testSetAttrsWithoutADMIN()

  public void testSetAttrsWithADMIN() {
    LOG.info("testSetAttrsWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String val = "new value";
    GroupHelper.setAttrFail(a, ""                 , ""  );
    GroupHelper.setAttrFail(a, ""                 , null);
    GroupHelper.setAttrFail(a, null               , null);
    GroupHelper.setAttrFail(a, null               , ""  );
    GroupHelper.setAttrFail(a, "attr"             , val );
    GroupHelper.setAttr(    a, "description"      , val );
    GroupHelper.setAttrFail(a, "displayName"      , val );
    GroupHelper.setAttr(    a, "displayExtension" , val );
    GroupHelper.setAttr(    a, "extension"        , val );
    GroupHelper.setAttrFail(a, "name"             , val );
  } // public void testSetAttrsWithADMIN()

  public void testSetAttrsWithAllADMIN() {
    LOG.info("testSetAttrsWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String val = "new value";
    GroupHelper.setAttrFail(a, ""                 , ""  );
    GroupHelper.setAttrFail(a, ""                 , null);
    GroupHelper.setAttrFail(a, null               , null);
    GroupHelper.setAttrFail(a, null               , ""  );
    GroupHelper.setAttrFail(a, "attr"             , val );
    GroupHelper.setAttr(    a, "description"      , val );
    GroupHelper.setAttrFail(a, "displayName"      , val );
    GroupHelper.setAttr(    a, "displayExtension" , val );
    GroupHelper.setAttr(    a, "extension"        , val );
    GroupHelper.setAttrFail(a, "name"             , val );
  } // public void testSetAttrsWithAllADMIN()

  public void testDelAttrsWithoutADMIN() {
    LOG.info("testDelAttrsWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    GroupHelper.delAttrFail(a, ""                 );
    GroupHelper.delAttrFail(a, null               );
    GroupHelper.delAttrFail(a, "attr"             );
    GroupHelper.delAttrFail(a, "description"      );
    GroupHelper.delAttrFail(a, "displayName"      );
    GroupHelper.delAttrFail(a, "displayExtension" );
    GroupHelper.delAttrFail(a, "extension"        );
    GroupHelper.delAttrFail(a, "name"             );
  } // public void testDelAttrsWithoutADMIN()

  public void testDelAttrsWithADMIN() {
    LOG.info("testDelAttrsWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String val = "new value";
    GroupHelper.delAttrFail(a, ""                 );
    GroupHelper.delAttrFail(a, null               );
    GroupHelper.delAttrFail(a, "attr"             );
    GroupHelper.delAttrFail(a, "description"      );
    GroupHelper.setAttr(    a, "description", val );
    GroupHelper.delAttr(    a, "description"      );
    GroupHelper.delAttrFail(a, "displayName"      );
    GroupHelper.delAttrFail(a, "displayExtension" );
    GroupHelper.delAttrFail(a, "extension"        );
    GroupHelper.delAttrFail(a, "name"             );
  } // public void testDelAttrsWithADMIN()

  public void testDelAttrsWithAllADMIN() {
    LOG.info("testDelAttrsWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String val = "new value";
    GroupHelper.delAttrFail(a, ""                 );
    GroupHelper.delAttrFail(a, null               );
    GroupHelper.delAttrFail(a, "attr"             );
    GroupHelper.delAttrFail(a, "description"      );
    GroupHelper.setAttr(    a, "description", val );
    GroupHelper.delAttr(    a, "description"      );
    GroupHelper.delAttrFail(a, "displayName"      );
    GroupHelper.delAttrFail(a, "displayExtension" );
    GroupHelper.delAttrFail(a, "extension"        );
    GroupHelper.delAttrFail(a, "name"             );
  } // public void testDelAttrsWithAllADMIN()

  public void testRenameGroupWithoutADMIN() {
    LOG.info("testRenameGroupWithoutADMIN");
    a = GroupHelper.findByName(nrs, i2.getName());
    String orig = a.getExtension();
    try {
      a.setExtension("foo");
      Assert.fail("set extension");
    }
    catch (Exception e) {
      Assert.assertTrue("failed to set extension", true);
      Assert.assertTrue("extension", a.getExtension().equals(orig));
    } 
    orig = a.getDisplayExtension();
    try {
      a.setDisplayExtension("foo");
      Assert.fail("set displayExtension");
    }
    catch (Exception e) {
      Assert.assertTrue("failed to set displayExtension", true);
      Assert.assertTrue("displayExtension", a.getDisplayExtension().equals(orig));
    } 
  } // public void testRenameGroupWithoutADMIN()

  public void testRenameGroupWithADMIN() {
    LOG.info("testRenameGroupWithADMIN");
    PrivHelper.grantPriv(s, i2, subj0, AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String  orig  = a.getExtension();
    String  val   = "foo";
    try {
      a.setExtension(val);
      Assert.assertTrue("set extension", true);
      Assert.assertTrue("extension", a.getExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set extension");
    } 
    orig = a.getDisplayExtension();
    try {
      a.setDisplayExtension("foo");
      Assert.assertTrue("set displayExtension", true);
      Assert.assertTrue("displayExtension", a.getDisplayExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set displayExtension");
    } 
  } // public void testRenameGroupWithADMIN()

  public void testRenameGroupWithAllADMIN() {
    LOG.info("testRenameGroupWithAllADMIN");
    PrivHelper.grantPriv(s, i2, SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    a = GroupHelper.findByName(nrs, i2.getName());
    String  orig  = a.getExtension();
    String  val   = "foo";
    try {
      a.setExtension("foo");
      Assert.assertTrue("set extension", true);
      Assert.assertTrue("extension", a.getExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set extension");
    } 
    orig = a.getDisplayExtension();
    try {
      a.setDisplayExtension("foo");
      Assert.assertTrue("set displayExtension", true);
      Assert.assertTrue("displayExtension", a.getDisplayExtension().equals(val));
    }
    catch (Exception e) {
      Assert.fail("failed to set displayExtension");
    } 
  } // public void testRenameGroupWithAllADMIN()

}

