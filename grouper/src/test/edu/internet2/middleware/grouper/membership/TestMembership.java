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

package edu.internet2.middleware.grouper.membership;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Membership}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMembership.java,v 1.2 2009-08-12 12:44:45 shilen Exp $
 */
public class TestMembership extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership("testIllegalEffectiveDelete"));
  }
  
  // Private Class Constants
  private static final Log        LOG   = GrouperUtil.getLog(TestMembership.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestMembership(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectTestHelper.SUBJ0;
    subj1 = SubjectTestHelper.SUBJ1;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testNoParentAndNoChildMemberships() {
    LOG.info("testNoParentAndNoChildMemberships");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2, subj0, "members");
    MembershipTestHelper.testNumMship(i2, "members", 1, 1, 0);
    Membership imm = MembershipTestHelper.getImm(s, i2, subj0, "members");
    MembershipTestHelper.testNoParent(imm);
    MembershipTestHelper.testNoChildren(imm);
  } // public void testNoParentAndNoChildMemberships()

  public void testParentAndChildMemberships() {
    LOG.info("testParentAndChildMemberships");
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 0, 0, 0);

    GroupHelper.addMember(uofc, i2.toSubject(), "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject(), "members"); 
    MembershipTestHelper.testNumMship(i2   , "members", 0, 0, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 1, 1, 0);
    Membership uofc_i2 = MembershipTestHelper.getImm(s, uofc, i2.toSubject(), "members");
    MembershipTestHelper.testNoParent(uofc_i2);
    MembershipTestHelper.testNoChildren(uofc_i2);

    GroupHelper.addMember(i2, subj0, "members");
    MembershipTestHelper.testImm(s, i2   , subj0          , "members");
    MembershipTestHelper.testImm(s, uofc , i2.toSubject() , "members"); 
    MembershipTestHelper.testEff(s, uofc,  subj0          , "members", i2, 1);
    MembershipTestHelper.testNumMship(i2   , "members", 1, 1, 0);
    MembershipTestHelper.testNumMship(uofc , "members", 2, 1, 1);
    Membership i2_subj0 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    MembershipTestHelper.testNoParent(i2_subj0);
    MembershipTestHelper.testNoChildren(i2_subj0);
    Set uofc_i2_subj0 = MembershipTestHelper.getEff(s, uofc, subj0, "members", 1, i2);
    Set children      = new LinkedHashSet();
    Iterator iter = uofc_i2_subj0.iterator();
    while (iter.hasNext()) {
      Membership eff = (Membership) iter.next();
      MembershipTestHelper.testParent(uofc_i2, eff);
      children.add(eff);
    }
    MembershipTestHelper.testChildren(uofc_i2, children);
  } // public void testParentAndChildMemberships()

  public void testEqualNotEqual() {
    LOG.info("testEqualNotEqual");
    MembershipTestHelper.testNumMship(i2, "members", 0, 0, 0);
    GroupHelper.addMember(i2, subj0, "members");
    GroupHelper.addMember(i2, subj1, "members");
    Membership imm0 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    Membership imm1 = MembershipTestHelper.getImm(s, i2, subj1, "members");
    Membership imm2 = MembershipTestHelper.getImm(s, i2, subj0, "members");
    Assert.assertTrue("equal",      imm0.equals(imm2));
    Assert.assertTrue("not equal",  !imm0.equals(imm1));
  } // public void testEqualNotEqual()

  // @source  Gary Brown, 20051206, <6513d0390512060544q3fff7944vb8e1cedae7d4f92c@mail.gmail.com>
  // @status  fixed
  public void testBadEffMshipDepthCalcExposedByGroupDelete() {
    LOG.info("testBadEffMshipDepthCalcExposedByGroupDelete");
    try {
      Subject kebe = SubjectTestHelper.SUBJ0;
      Subject iata = SubjectTestHelper.SUBJ1;
      Subject iawi = SubjectTestHelper.SUBJ2;
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      GrouperSession s = GrouperSession.startRootSession();
      Subject subj = SubjectFinder.findById("GrouperSystem", true);
      Stem root = StemFinder.findRootStem(s);
  		Stem qsuob = root.addChildStem("qsuob","qsuob");
      Group admins = qsuob.addChildGroup(Field.FIELD_NAME_ADMINS,Field.FIELD_NAME_ADMINS);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.0");
      admins.addMember(kebe);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.1");
      Group staff = qsuob.addChildGroup("staff","staff");
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.2");
      staff.addMember(iata);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testNumMship(staff, "members", 1, 1, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.3");
      staff.addMember(iawi);
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.4");
      Group all_staff = qsuob.addChildGroup("all_staff","all staff");
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.5");
      all_staff.addMember(staff.toSubject());
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.6");
      admins.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, admins, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, Field.FIELD_NAME_ADMINS, 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.7");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("create"));
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.8");
      qsuob.grantPriv(admins.toSubject(),Privilege.getInstance("stem"));
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.9");
      staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, admins, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, Field.FIELD_NAME_ADMINS, 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testImm(s, staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, staff, iawi, "readers", staff, 2);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.10");
      staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, admins, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, Field.FIELD_NAME_ADMINS, 3, 2, 1);
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, staff, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, Field.FIELD_NAME_ADMINS, 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11");
      all_staff.grantPriv(all_staff.toSubject(),Privilege.getInstance("read"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, admins, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, Field.FIELD_NAME_ADMINS, 3, 2, 1);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.0");
  
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipTestHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.11.1");
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, staff, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, Field.FIELD_NAME_ADMINS, 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.12");
      all_staff.grantPriv(admins.toSubject(),Privilege.getInstance("admin"));
      MembershipTestHelper.testImm(s, admins, kebe, "members");
      MembershipTestHelper.testImm(s, admins, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, admins, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, admins, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(admins, "members", 1, 1, 0);
      MembershipTestHelper.testNumMship(admins, Field.FIELD_NAME_ADMINS, 3, 2, 1);
  
      MembershipTestHelper.testImm(s, all_staff, staff.toSubject() , "members");
      MembershipTestHelper.testEff(s, all_staff, iata, "members", staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iawi, "members", staff, 1);
      MembershipTestHelper.testNumMship(all_staff, "members", 3, 1, 2);
      MembershipTestHelper.testImm(s, all_staff, all_staff.toSubject(), "readers");
      MembershipTestHelper.testEff(s, all_staff, staff.toSubject(), "readers", all_staff, 1);
      MembershipTestHelper.testEff(s, all_staff, iata, "readers", staff, 2);
      MembershipTestHelper.testEff(s, all_staff, iawi, "readers", staff, 2);
      MembershipTestHelper.testNumMship(all_staff, "readers", 5, 2, 3);
      MembershipTestHelper.testImm(s, all_staff, subj, Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testImm(s, all_staff, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, all_staff, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
  
      MembershipTestHelper.testImm(s, staff, iata , "members");
      MembershipTestHelper.testImm(s, staff, iawi , "members");
      MembershipTestHelper.testImm(s, staff, admins.toSubject(), Field.FIELD_NAME_ADMINS);
      MembershipTestHelper.testEff(s, staff, kebe, Field.FIELD_NAME_ADMINS, admins, 1);
      MembershipTestHelper.testNumMship(staff, "members", 2, 2, 0);
      MembershipTestHelper.testNumMship(staff, Field.FIELD_NAME_ADMINS, 3, 2, 1);
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.13");
      GroupHelper.delete(s, admins, admins.getName());
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.14");
      GroupHelper.delete(s, staff, staff.getName());
  
      LOG.debug("testBadEffMshipDepthCalcExposedByGroupDelete.15");
      GroupHelper.delete(s, all_staff, all_staff.getName());
  
      s.stop();
    }
    catch (Exception e) {
      //Assert.fail("exception: " + e.getMessage());
      throw new RuntimeException(e);
    }
  } // public void testBadEffMshipDepthCalcExposedByGroupDelete() 

  public void testChildrenOfViaInMofDeletion() {
    LOG.info("testChildrenOfViaInMofDeletion");
    try {
      R       r     = R.populateRegistry(1, 3, 1);
      Group   gA    = r.getGroup("a", "a");   
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
  
      gB.addMember( gA.toSubject() );
      // gA -> gB
  
      gC.addMember( gB.toSubject() );
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC
  
      gA.addMember(subjA);
      // gA -> gB
      // gB -> gC
      // gA -> gB -> gC
      // sA -> gA
      // sA -> gA -> gB
      // sA -> gA -> gB -> gC
  
      try {
        gB.deleteMember( gA.toSubject() );
        // gB -> gC
        // sA -> gA
        Assert.assertTrue("no exception thrown", true);
        T.getMemberships( gA, 1 );
        T.getMemberships( gB, 0 );
        T.getMemberships( gC, 1 );
      }
      catch (GrouperException eGRT) {
        Assert.fail("runtime exception thrown: " + eGRT.getMessage());
      }
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testChildrenOfViaInMofDeletion()

  
  /**
   * make an example stem for testing
   * @return an example membership
   */
  public static Membership exampleMembership() {
    Membership membership = new Membership();
    membership.setContextId("contextId");
    membership.setCreateTimeLong(5L);
    membership.setCreatorUuid("creatorId");
    membership.setDisabledTimeDb(4L);
    membership.setEnabledDb("T");
    membership.setEnabledTimeDb(6L);
    membership.setFieldId("fieldId");
    membership.setHibernateVersionNumber(3L);
    membership.setMemberUuid("memberId");
    membership.setOwnerAttrDefId("ownerAttrDefId");
    membership.setOwnerGroupId("ownerGroupId");
    membership.setOwnerStemId("ownerStemId");
    membership.setType("type");
    membership.setImmediateMembershipId("uuid");
    membership.setViaCompositeId("viaCompositeId");
    
    return membership;
  }
  
  /**
   * make an example membership for testing
   * @return an example membership
   */
  public static Membership exampleMembershipDb() {
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipTest").assignName("test:membershipTest").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Subject subject = SubjectTestHelper.SUBJ0;
    
    group.addMember(subject, false);
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    
    //find the membership
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    
    return membership;
  }

  
  /**
   * make an example stem for testing
   * @return an example stem
   */
  public static Membership exampleRetrieveMembershipDb() {
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:membershipTest", true);
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    
    //find the membership
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    
    return membership;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignGroupNameToEdit("test:membershipInsertTest").assignName("test:membershipInsertTest").assignCreateParentStemsIfNotExist(true)
      .assignDescription("description").save();
    Subject subject = SubjectTestHelper.SUBJ0;
    
    group.addMember(subject, false);
    
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    
    //find the membership
    Membership membershipOriginal = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    
    //do this because last membership update isnt there, only in db
    Membership membershipCopy = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    Membership membershipCopy2 = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    group.deleteMember(SubjectTestHelper.SUBJ0);
    
    //lets insert the original
    membershipCopy2.xmlSaveBusinessProperties(null);
    membershipCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    membershipCopy = GrouperDAOFactory.getFactory().getMembership().findByImmediateUuidOrKey(null, member.getUuid(),
        Group.getDefaultList().getUuid(), null, group.getUuid(), null, true);
    
    assertFalse(membershipCopy == membershipOriginal);
    assertFalse(membershipCopy.xmlDifferentBusinessProperties(membershipOriginal));
    assertFalse(membershipCopy.xmlDifferentUpdateProperties(membershipOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Membership membership = null;
    Membership exampleMembership = null;

    
    //TEST UPDATE PROPERTIES
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();
      
      membership.setContextId("abc");
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertTrue(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setContextId(exampleMembership.getContextId());
      membership.xmlSaveUpdateProperties();

      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
      
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setCreateTimeLong(99);
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertTrue(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setCreateTimeLong(exampleMembership.getCreateTimeLong());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setCreatorUuid("abc");
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertTrue(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setCreatorUuid(exampleMembership.getCreatorUuid());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setHibernateVersionNumber(99L);
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertTrue(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setHibernateVersionNumber(exampleMembership.getHibernateVersionNumber());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    }
    //TEST BUSINESS PROPERTIES

    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setDisabledTimeDb(5L);
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setDisabledTimeDb(exampleMembership.getDisabledTimeDb());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setEnabledDb("F");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setEnabledDb(exampleMembership.getEnabledDb());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setEnabledTimeDb(99L);
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setEnabledTimeDb(exampleMembership.getEnabledTimeDb());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setFieldId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setFieldId(exampleMembership.getFieldId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setMemberUuid("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setMemberUuid(exampleMembership.getMemberUuid());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setOwnerAttrDefId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setOwnerAttrDefId(exampleMembership.getOwnerAttrDefId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setOwnerGroupId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setOwnerGroupId(exampleMembership.getOwnerGroupId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }

    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setOwnerStemId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setOwnerStemId(exampleMembership.getOwnerStemId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setImmediateMembershipId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setImmediateMembershipId(exampleMembership.getImmediateMembershipId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setType(MembershipType.NONIMMEDIATE.name());
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setType(exampleMembership.getType());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
    
    {
      membership = exampleMembershipDb();
      exampleMembership = exampleRetrieveMembershipDb();

      membership.setViaCompositeId("abc");
      
      assertTrue(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));

      membership.setViaCompositeId(exampleMembership.getViaCompositeId());
      membership.xmlSaveBusinessProperties(exampleRetrieveMembershipDb());
      membership.xmlSaveUpdateProperties();
      
      membership = exampleRetrieveMembershipDb();
      
      assertFalse(membership.xmlDifferentBusinessProperties(exampleMembership));
      assertFalse(membership.xmlDifferentUpdateProperties(exampleMembership));
    
    }
  }

  /**
   * 
   */
  public void testDisabledDateRange() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    //subject 1,2 is just more data in the mix
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    
    groupEmployee.addMember(subject1, false);
    groupEmployee.addMember(subject2, false);

    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembershipsByGroupOwnerFieldDisabledRange(
          groupEmployee.getUuid(), Group.getDefaultList(), null, null);

      fail("should need either disabled from or to");
    } catch (Exception e) {
      //good
    }

    groupEmployee.addMember(subject0, false);

    Timestamp timestamp5daysForward = new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000));
    Timestamp timestamp6daysForward = new Timestamp(System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    Timestamp timestamp7daysForward = new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));
    Timestamp timestamp8daysForward = new Timestamp(System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));
    Timestamp timestamp9daysForward = new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000));
    
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals(0, memberships.size());
    
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, true);
    
    Membership membership = groupEmployee.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    //############### set disabled 7 days in the future
    membership.setDisabledTime(timestamp7daysForward);
    membership.update();
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
    .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
        Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals(1, memberships.size());

    groupProgrammer.addMember(subject0, false);

    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
        Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals("there is a membership in another path, not going to expire", 0, memberships.size());
    
    //################# BACK TO ONE RECORD
    groupProgrammer.deleteMember(subject0, false);
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
    .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
        Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals(1, memberships.size());

    //################# BACK TO ONE RECORD, MIXED UP ORDER
    memberships = GrouperDAOFactory.getFactory().getMembership()
    .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
        Group.getDefaultList(), timestamp8daysForward, timestamp6daysForward);

    assertEquals(1, memberships.size());

    //################# SET TO 5 DAYS
    membership.setDisabledTime(timestamp5daysForward);
    membership.update();
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals("out of range", 0, memberships.size());

    //################# SET TO 9 DAYS
    membership.setDisabledTime(timestamp9daysForward);
    membership.update();
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), timestamp6daysForward, timestamp8daysForward);

    assertEquals("out of range", 0, memberships.size());

    //################ TRY ONLY FROM
    membership.setDisabledTime(timestamp7daysForward);
    membership.update();
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), timestamp6daysForward, null);

    assertEquals("in range", 1, memberships.size());

    //################ TRY ONLY FROM
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), timestamp8daysForward, null);

    assertEquals("not in range", 0, memberships.size());

    //################ TRY ONLY TO
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), null, timestamp8daysForward);

    assertEquals("in range", 1, memberships.size());

    //################ TRY ONLY TO
    
    memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembershipsByGroupOwnerFieldDisabledRange(groupEmployee.getUuid(), 
          Group.getDefaultList(), null, timestamp6daysForward);

    assertEquals("not in range", 0, memberships.size());

    
  }
  
  /**
   * 
   */
  public void testIllegalEffectiveDelete() {
    Group group1 = edu.addChildGroup("group1", "group1");
    Group group2 = edu.addChildGroup("group2", "group2");
    group1.addMember(group2.toSubject());
    group2.addMember(subj0);
    
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndType(group1.getUuid(), Group.getDefaultList(), "effective", false).iterator().next();
    
    try {
      ms.delete();
      fail("expected failure");
    } catch (Exception e) {
      // good
    }
  }
  
}

