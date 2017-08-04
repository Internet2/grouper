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


import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import bsh.Interpreter;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
public class TestFindBadMemberships extends GrouperTest {

  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestFindBadMemberships.class);

  private GrouperSession grouperSession;
  private Stem top;
  private Group owner1;
  private Group owner2;
  private Group owner3;
  private Group owner4;
  private Group owner5;
  private Group owner6;
  private Group owner7;
  private Group subjEGroup;
  private Subject subjA;
  private Subject subjB;
  private Subject subjC;
  private Subject subjD;
  private Subject subjE;
  private Subject subjF;
  
  /**
   * @param name
   */
  public TestFindBadMemberships(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestFindBadMemberships("testDuplicates"));
  }
  
  protected void setUp () {
    super.setUp();
    FindBadMemberships.clearResults();
    FindBadMemberships.printErrorsToSTOUT(false);
  }
  
  /**
   * @throws Exception
   */
  public void testDuplicates() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    Group test1 = top.addChildGroup("test1", "test1");
    Group test2 = top.addChildGroup("test2", "test2");
    Group test3 = top.addChildGroup("test3", "test3");
    Group test4 = top.addChildGroup("test4", "test4");
    Group test5 = top.addChildGroup("test5", "test5");
    
    Group test7 = top.addChildGroup("test7", "test7");
    
    test1.addMember(test2.toSubject());
    test2.addMember(test3.toSubject());
    test3.addMember(test4.toSubject());
    test4.addMember(test5.toSubject());
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();
    
    // all should be good right now
    assertEquals(0, FindBadMemberships.checkAll());
    
    {
      // 2 with same owner/member/field with 1 having a foreign key
      GroupSet selfGroupSetTest2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(test2.getId(), Group.getDefaultList().getId());
      
      GroupSet duplicate1 = new GroupSet();
      duplicate1.setId(GrouperUuid.getUuid());
      duplicate1.setCreatorId(selfGroupSetTest2.getCreatorId());
      duplicate1.setCreateTime(selfGroupSetTest2.getCreateTime());
      duplicate1.setDepth(selfGroupSetTest2.getDepth());
      duplicate1.setMemberGroupId(selfGroupSetTest2.getMemberGroupId());
      duplicate1.setOwnerGroupId(selfGroupSetTest2.getOwnerGroupId());
      duplicate1.setParentId(duplicate1.getId());
      duplicate1.setFieldId(selfGroupSetTest2.getFieldId());
      GrouperDAOFactory.getFactory().getGroupSet().save(duplicate1);
    }
    
    {
      // 3 with same owner/member/field with 1 having a foreign key
      GroupSet selfGroupSetTest3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(test3.getId(), Group.getDefaultList().getId());
      
      GroupSet duplicate1 = new GroupSet();
      duplicate1.setId(GrouperUuid.getUuid());
      duplicate1.setCreatorId(selfGroupSetTest3.getCreatorId());
      duplicate1.setCreateTime(selfGroupSetTest3.getCreateTime());
      duplicate1.setDepth(selfGroupSetTest3.getDepth());
      duplicate1.setMemberGroupId(selfGroupSetTest3.getMemberGroupId());
      duplicate1.setOwnerGroupId(selfGroupSetTest3.getOwnerGroupId());
      duplicate1.setParentId(duplicate1.getId());
      duplicate1.setFieldId(selfGroupSetTest3.getFieldId());
      GrouperDAOFactory.getFactory().getGroupSet().save(duplicate1);
      
      GroupSet duplicate2 = new GroupSet();
      duplicate2.setId(GrouperUuid.getUuid());
      duplicate2.setCreatorId(selfGroupSetTest3.getCreatorId());
      duplicate2.setCreateTime(selfGroupSetTest3.getCreateTime());
      duplicate2.setDepth(selfGroupSetTest3.getDepth());
      duplicate2.setMemberGroupId(selfGroupSetTest3.getMemberGroupId());
      duplicate2.setOwnerGroupId(selfGroupSetTest3.getOwnerGroupId());
      duplicate2.setParentId(duplicate2.getId());
      duplicate2.setFieldId(selfGroupSetTest3.getFieldId());
      GrouperDAOFactory.getFactory().getGroupSet().save(duplicate2);
    }
    
    {
      // 2 with same owner/member/field, no foreign keys
      GroupSet selfGroupSetTest7 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(test7.getId(), Group.getDefaultList().getId());
      
      GroupSet duplicate1 = new GroupSet();
      duplicate1.setId(GrouperUuid.getUuid());
      duplicate1.setCreatorId(selfGroupSetTest7.getCreatorId());
      duplicate1.setCreateTime(selfGroupSetTest7.getCreateTime());
      duplicate1.setDepth(selfGroupSetTest7.getDepth());
      duplicate1.setMemberGroupId(selfGroupSetTest7.getMemberGroupId());
      duplicate1.setOwnerGroupId(selfGroupSetTest7.getOwnerGroupId());
      duplicate1.setParentId(duplicate1.getId());
      duplicate1.setFieldId(selfGroupSetTest7.getFieldId());
      GrouperDAOFactory.getFactory().getGroupSet().save(duplicate1);
    }
    
    // now check it
    assertEquals(4, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time, there may be changes there to fix pit group sets
    ChangeLogTempToEntity.convertRecords();
    new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables();
  }
  
  /**
   * @throws Exception
   */
  public void testGrouperAllMembership() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    Group test = top.addChildGroup("test", "test");
    
    Field badField = Group.getDefaultList();
    PITField badPITField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(badField.getId(), true);

    test.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, test, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN.getField(), true);
    
    // all should be good right now
    assertEquals(0, FindBadMemberships.checkAll());
    
    // now update to the bad membership
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set field_id = '" + badField.getId() + "' where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setFieldId(badPITField.getId());
    pitMembership.update();
    
    // everything should seem to be okay as far as PIT goes
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // now check it
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * @throws Exception
   */
  public void testGrouperAllGroupAttrUpdatePrivilege() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    Group test = top.addChildGroup("test", "test");
    
    Field badField = AccessPrivilege.GROUP_ATTR_UPDATE.getField();
    PITField badPITField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(badField.getId(), true);

    test.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, test, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN.getField(), true);
    
    // all should be good right now
    assertEquals(0, FindBadMemberships.checkAll());
    
    // now update to the bad membership
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set field_id = '" + badField.getId() + "' where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setFieldId(badPITField.getId());
    pitMembership.update();
    
    // everything should seem to be okay as far as PIT goes
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // now check it
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * @throws Exception
   */
  public void testGrouperAllUpdatePrivilege() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    Group test = top.addChildGroup("test", "test");
    
    Field badField = AccessPrivilege.UPDATE.getField();
    PITField badPITField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(badField.getId(), true);

    test.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, test, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN.getField(), true);
    
    // all should be good right now
    assertEquals(0, FindBadMemberships.checkAll());
    
    // now update to the bad membership
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set field_id = '" + badField.getId() + "' where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setFieldId(badPITField.getId());
    pitMembership.update();
    
    // everything should seem to be okay as far as PIT goes
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // now check it
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * @throws Exception
   */
  public void testGrouperAllAdminPrivilege() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    Group test = top.addChildGroup("test", "test");
    
    Field badField = AccessPrivilege.ADMIN.getField();
    PITField badPITField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(badField.getId(), true);

    test.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, test, SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN.getField(), true);
    
    // all should be good right now
    assertEquals(0, FindBadMemberships.checkAll());
    
    // now update to the bad membership
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set field_id = '" + badField.getId() + "' where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setFieldId(badPITField.getId());
    pitMembership.update();
    
    // everything should seem to be okay as far as PIT goes
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // now check it
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }

  /**
   * Test bad composites without composites
   */
  public void testWithoutComposites() {
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * @throws Exception
   */
  private void setUpComposites() throws Exception {
    R r = R.populateRegistry(0, 0, 6);
    subjA = r.getSubject("a");
    subjB = r.getSubject("b");
    subjC = r.getSubject("c");
    subjD = r.getSubject("d");
    subjF = r.getSubject("f");

    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    
    subjEGroup = top.addChildGroup("subjE", "subjE");
    subjEGroup.addMember(subjC);
    subjE = subjEGroup.toSubject();
    
    owner1 = top.addChildGroup("owner1", "owner1");
    owner2 = top.addChildGroup("owner2", "owner2");
    owner3 = top.addChildGroup("owner3", "owner3");
    owner4 = top.addChildGroup("owner4", "owner4");
    owner5 = top.addChildGroup("owner5", "owner5");
    owner6 = top.addChildGroup("owner6", "owner6");
    owner7 = top.addChildGroup("owner7", "owner7");
    
    Group left1 = top.addChildGroup("left1", "left1");
    Group left2 = top.addChildGroup("left2", "left2");
    Group left3 = top.addChildGroup("left3", "left3");
    Group left4 = top.addChildGroup("left4", "left4");
    Group left5 = top.addChildGroup("left5", "left5");
    Group left6 = top.addChildGroup("left6", "left6");
    Group left7 = top.addChildGroup("left7", "left7");

    Group right1 = top.addChildGroup("right1", "right1");
    Group right2 = top.addChildGroup("right2", "right2");
    Group right3 = top.addChildGroup("right3", "right3");
    Group right4 = top.addChildGroup("right4", "right4");
    Group right5 = top.addChildGroup("right5", "right5");
    Group right6 = top.addChildGroup("right6", "right6");
    Group right7 = top.addChildGroup("right7", "right7");
    
    owner1.addCompositeMember(CompositeType.UNION, left1, right1);
    owner2.addCompositeMember(CompositeType.COMPLEMENT, left2, right2);
    owner3.addCompositeMember(CompositeType.INTERSECTION, left3, right3);
    owner4.addCompositeMember(CompositeType.UNION, left4, right4);
    owner5.addCompositeMember(CompositeType.COMPLEMENT, left5, right5);
    owner6.addCompositeMember(CompositeType.INTERSECTION, left6, right6);
    owner7.addCompositeMember(CompositeType.UNION, left7, right7);
    
    left1.addMember(subjA);
    left1.addMember(subjB);
    left1.addMember(subjE);
    left2.addMember(subjA);
    left2.addMember(subjB);
    left2.addMember(subjE);
    left2.addMember(subjF);
    right2.addMember(subjB);
    left3.addMember(subjA);
    right3.addMember(subjA);
    
    left4.addMember(subjA);
    left4.addMember(subjB);
    left4.addMember(subjE);
    left5.addMember(subjA);
    left5.addMember(subjB);
    left5.addMember(subjE);
    right5.addMember(subjB);
    left6.addMember(subjA);
    right6.addMember(subjA);
  }
  
  /**
   * Test bad composites with composites that are okay
   * @throws Exception 
   */
  public void testWithGoodComposites() throws Exception {
    setUpComposites();
    assertEquals(0, FindBadMemberships.checkAll());
  }
  
  /**
   * Don't want false positives with circular group sets
   * @throws Exception 
   */
  public void testCircular() throws Exception {
    setUpComposites();
    Group gA1 = top.addChildGroup("gA1", "gA1");
    Group gA2 = top.addChildGroup("gA2", "gA2");
    gA1.addMember(gA2.toSubject());
    gA2.addMember(gA1.toSubject());
    
    Group gB1 = top.addChildGroup("gB1", "gB1");
    Group gB2 = top.addChildGroup("gB2", "gB2");
    Group gB3 = top.addChildGroup("gB3", "gB3");
    gB1.addMember(gB2.toSubject());
    gB2.addMember(gB3.toSubject());
    gB3.addMember(gB1.toSubject());
    
    Group gC1 = top.addChildGroup("gC1", "gC1");
    Group gC2 = top.addChildGroup("gC2", "gC2");
    Group gC3 = top.addChildGroup("gC3", "gC3");
    Group gC4 = top.addChildGroup("gC4", "gC4");
    gC1.addMember(gC2.toSubject());
    gC2.addMember(gC3.toSubject());
    gC3.addMember(gC4.toSubject());
    gC4.addMember(gC1.toSubject());
    
    Group gD1 = top.addChildGroup("gD1", "gD1");
    Group gD2 = top.addChildGroup("gD2", "gD2");
    Group gD3 = top.addChildGroup("gD3", "gD3");
    gD1.addMember(gD2.toSubject());
    gD2.addMember(gD3.toSubject());
    gD3.addMember(gD2.toSubject());
    
    Stem sE1 = top.addChildStem("sE1", "sE1");
    Group gE2 = top.addChildGroup("gE2", "gE2");
    Group gE3 = top.addChildGroup("gE3", "gE3");
    sE1.grantPriv(gE2.toSubject(), NamingPrivilege.STEM);
    gE2.addMember(gE3.toSubject());
    gE3.addMember(gE2.toSubject());
    
    Stem sF1 = top.addChildStem("sF1", "sF1");
    Group gF2 = top.addChildGroup("gF2", "gF2");
    Group gF3 = top.addChildGroup("gF3", "gF3");
    Group gF4 = top.addChildGroup("gF4", "gF4");
    sF1.grantPriv(gF2.toSubject(), NamingPrivilege.STEM);
    gF2.addMember(gF3.toSubject());
    gF3.addMember(gF4.toSubject());
    gF4.addMember(gF2.toSubject());
    
    Group gG1 = top.addChildGroup("gG1", "gG1");
    gG1.grantPriv(gG1.toSubject(), AccessPrivilege.ADMIN);
    
    Group gH1 = top.addChildGroup("gH1", "gH1");
    Group gH2 = top.addChildGroup("gH2", "gH2");
    Group gH3 = top.addChildGroup("gH3", "gH3");
    gH1.grantPriv(gH2.toSubject(), AccessPrivilege.ADMIN);
    gH2.addMember(gH3.toSubject());
    gH3.addMember(gH1.toSubject());
    
    Group gI1 = top.addChildGroup("gI1", "gI1");
    Group gI2 = top.addChildGroup("gI2", "gI2");
    gI1.grantPriv(gI2.toSubject(), AccessPrivilege.ADMIN);
    gI2.addMember(gI1.toSubject());
    
    assertEquals(0, FindBadMemberships.checkAll());
    
    // now lets remove some group sets
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(gH1.getUuid(), gH1.getUuid(), FieldFinder.find("admins", true).getUuid());
    for (GroupSet gs : groupSets) {
      if (gs.getDepth() != 0) {
        gs.delete(false);
      }
    }
    
    groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(gI1.getUuid(), gI1.getUuid(), FieldFinder.find("admins", true).getUuid());
    for (GroupSet gs : groupSets) {
      if (gs.getDepth() != 0) {
        gs.delete(false);
      }
    }
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    assertEquals(2, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * Test bad composites when there are missing composites
   * @throws Exception
   */
  public void testWithMissingComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    Membership ms4 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjE, false);
    assertNull(ms4);
    
    ms1.delete();
    ms2.delete();
    ms3.delete();
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(3, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when there are extra composites
   * @throws Exception
   */
  public void testWithExtraComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    ms1.setHibernateVersionNumber(-1L);
    ms2.setHibernateVersionNumber(-1L);
    ms3.setHibernateVersionNumber(-1L);
    
    ms1.setImmediateMembershipId(GrouperUuid.getUuid());
    ms2.setImmediateMembershipId(GrouperUuid.getUuid());
    ms3.setImmediateMembershipId(GrouperUuid.getUuid());
    
    Member member = MemberFinder.findBySubject(grouperSession, subjD, true);
    ms1.setMemberUuid(member.getUuid());
    ms2.setMemberUuid(member.getUuid());
    ms3.setMemberUuid(member.getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().save(ms1);
    GrouperDAOFactory.getFactory().getMembership().save(ms2);
    GrouperDAOFactory.getFactory().getMembership().save(ms3);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(3, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong member, but the number of memberships
   * is still the same.
   * @throws Exception
   */
  public void testWithWrongComposite() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    Member member = MemberFinder.findBySubject(grouperSession, subjD, true);
    ms1.setMemberUuid(member.getUuid());
    ms2.setMemberUuid(member.getUuid());
    ms3.setMemberUuid(member.getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    GrouperDAOFactory.getFactory().getMembership().update(ms2);
    GrouperDAOFactory.getFactory().getMembership().update(ms3);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(6, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong type.
   * @throws Exception
   */
  public void testWithWrongType() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    Membership ms2 = MembershipFinder.findCompositeMembership(grouperSession, owner2, subjF, true);
    Membership ms3 = MembershipFinder.findCompositeMembership(grouperSession, owner3, subjA, true);
    
    ms1.setType(MembershipType.IMMEDIATE.getTypeString());
    ms2.setType(MembershipType.IMMEDIATE.getTypeString());
    ms3.setType(MembershipType.IMMEDIATE.getTypeString());
    
    ms1.setViaCompositeId(null);
    ms2.setViaCompositeId(null);
    ms3.setViaCompositeId(null);
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    GrouperDAOFactory.getFactory().getMembership().update(ms2);
    GrouperDAOFactory.getFactory().getMembership().update(ms3);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(6, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong type.
   * @throws Exception
   */
  public void testWithWrongType2() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findImmediateMembership(grouperSession, subjEGroup, subjC, true);
    
    ms1.setType(MembershipType.COMPOSITE.getTypeString());
    ms1.setViaCompositeId(owner1.getComposite(true).getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    
    // now we have 4 more bad memberships since we just deleted subjEGroup -> subjC
    FindBadMemberships.clearResults();
    assertEquals(4, FindBadMemberships.checkAll());
    gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());

    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test bad composites when composites have the wrong viaCompositeId.
   * @throws Exception
   */
  public void testWithWrongViaCompositeId() throws Exception {
    setUpComposites();
    
    Membership ms1 = MembershipFinder.findCompositeMembership(grouperSession, owner1, subjC, true);
    
    ms1.setViaCompositeId(owner2.getComposite(true).getUuid());
    
    GrouperDAOFactory.getFactory().getMembership().update(ms1);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    
    // now we have 1 more bad membership since we just deleted owner1 -> subjC
    FindBadMemberships.clearResults();
    assertEquals(1, FindBadMemberships.checkAll());
    gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());

    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * Test group sets using default field and depth=0 that have the wrong type
   * @throws Exception
   */
  public void testWithWrongGroupSetType() throws Exception {
    setUpComposites();
    
    GroupSet gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner1.getId(), Group.getDefaultList().getUuid());
    GroupSet gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner2.getId(), Group.getDefaultList().getUuid());
    GroupSet gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner3.getId(), Group.getDefaultList().getUuid());
    GroupSet gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(subjEGroup.getId(), Group.getDefaultList().getUuid());
    
    gs1.setType("immediate");
    gs2.setType("immediate");
    gs3.setType("immediate");
    gs4.setType("composite");
    
    GrouperDAOFactory.getFactory().getGroupSet().update(gs1);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs2);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs3);
    GrouperDAOFactory.getFactory().getGroupSet().update(gs4);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(4, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
  }

  /**
   * Test scenario where a composite group has group sets
   * @throws Exception
   */
  public void testWithBadGroupSetsForComposites() throws Exception {
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    g1.addMember(g2.toSubject());
    g2.addMember(owner2.toSubject());
    g3.addMember(g4.toSubject());
    owner1.grantPriv(g3.toSubject(), AccessPrivilege.READ); // group sets created by this are good..
    
    ChangeLogTempToEntity.convertRecords();

    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int pitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    int pitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    // add groupSet owner2 -> g3
    GroupSet gs1 = new GroupSet();
    gs1.setId(GrouperUuid.getUuid());
    gs1.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
    gs1.setDepth(1);
    gs1.setFieldId(Group.getDefaultList().getUuid());
    gs1.setMemberGroupId(g3.getUuid());
    gs1.setType(MembershipType.EFFECTIVE.getTypeString());
    gs1.setOwnerGroupId(owner2.getUuid());
    gs1.setParentId(GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(owner2.getUuid(), Group.getDefaultList().getUuid()).getId());
    GrouperDAOFactory.getFactory().getGroupSet().save(gs1);
    
    // add pitGroupSet owner2 -> g3
    PITField pitMemberField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(Group.getDefaultList().getUuid(), true);
    PITGroup pitOwner1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(owner2.getUuid(), true);
    PITGroup pitMember1 = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(g3.getUuid(), true);
    PITGroupSet pitParent1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(gs1.getParentId(), true);
    PITGroupSet pitGS1 = new PITGroupSet();
    pitGS1.setOwnerGroupId(pitOwner1.getId());
    pitGS1.setId(GrouperUuid.getUuid());
    pitGS1.setSourceId(gs1.getId());
    pitGS1.setFieldId(pitMemberField.getId());
    pitGS1.setMemberFieldId(pitMemberField.getId());
    pitGS1.setMemberGroupId(pitMember1.getId());
    pitGS1.setDepth(1);
    pitGS1.setParentId(pitParent1.getId());
    pitGS1.setActiveDb("T");
    pitGS1.setStartTimeDb(System.currentTimeMillis() * 1000);
    pitGS1.saveOrUpdate();
    
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    // verify that 8 group sets were added...
    assertEquals(groupSetCount + 6, newGroupSetCount);
    assertEquals(pitGroupSetCountTotal + 6, newPitGroupSetCountTotal);
    assertEquals(pitGroupSetCountActive + 6, newPitGroupSetCountActive);
        
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "import edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // check counts again..
    newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    newPitGroupSetCountTotal = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set");
    newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(pitGroupSetCountTotal + 6, newPitGroupSetCountTotal);
    assertEquals(pitGroupSetCountActive, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception 
   */
  public void testWithIncompleteGroupSetHierarchyAttrDefPrivs() throws Exception { 
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    Group g5 = top.addChildGroup("g5", "g5");
    Group g6 = top.addChildGroup("g6", "g6");
    
    Group g2b = top.addChildGroup("g2b", "g2b");
    Stem s2 = top.addChildStem("s2", "s2");
    AttributeDef a2 = top.addChildAttributeDef("a2", AttributeDefType.attr);
    
    // add some members...
    g1.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g5.addMember(SubjectTestHelper.SUBJ4);
    g6.addMember(SubjectTestHelper.SUBJ5);
    
    // add group sets now
    g1.addMember(g2.toSubject());
    g2.addMember(g3.toSubject());
    g3.addMember(g4.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(g6.toSubject());
    g2b.grantPriv(g3.toSubject(), AccessPrivilege.ADMIN);
    s2.grantPriv(g3.toSubject(), NamingPrivilege.STEM);
    a2.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_READ, true);
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets (assume race condition between g2 -> g3 (attr def priv) and g3 -> g4)
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g6.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g5.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g4.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);

    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception 
   */
  public void testWithIncompleteGroupSetHierarchyStemPrivs() throws Exception { 
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    Group g5 = top.addChildGroup("g5", "g5");
    Group g6 = top.addChildGroup("g6", "g6");
    
    Group g2b = top.addChildGroup("g2b", "g2b");
    Stem s2 = top.addChildStem("s2", "s2");
    AttributeDef a2 = top.addChildAttributeDef("a2", AttributeDefType.attr);
    
    // add some members...
    g1.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g5.addMember(SubjectTestHelper.SUBJ4);
    g6.addMember(SubjectTestHelper.SUBJ5);
    
    // add group sets now
    g1.addMember(g2.toSubject());
    g2.addMember(g3.toSubject());
    g3.addMember(g4.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(g6.toSubject());
    g2b.grantPriv(g3.toSubject(), AccessPrivilege.ADMIN);
    s2.grantPriv(g3.toSubject(), NamingPrivilege.STEM);
    a2.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_READ, true);
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets (assume race condition between g2 -> g3 (stem priv) and g4 -> g5)
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(s2.getUuid(), g6.getUuid(), FieldFinder.find("stemmers", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(s2.getUuid(), g5.getUuid(), FieldFinder.find("stemmers", true).getUuid()).iterator().next().delete(false);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception 
   */
  public void testWithIncompleteGroupSetHierarchyGroupMembers() throws Exception { 
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    Group g5 = top.addChildGroup("g5", "g5");
    Group g6 = top.addChildGroup("g6", "g6");
    
    Group g2b = top.addChildGroup("g2b", "g2b");
    Stem s2 = top.addChildStem("s2", "s2");
    AttributeDef a2 = top.addChildAttributeDef("a2", AttributeDefType.attr);
    
    // add some members...
    g1.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g5.addMember(SubjectTestHelper.SUBJ4);
    g6.addMember(SubjectTestHelper.SUBJ5);
    
    // add group sets now
    g1.addMember(g2.toSubject());
    g2.addMember(g3.toSubject());
    g3.addMember(g4.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(g6.toSubject());
    g2b.grantPriv(g3.toSubject(), AccessPrivilege.ADMIN);
    s2.grantPriv(g3.toSubject(), NamingPrivilege.STEM);
    a2.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_READ, true);
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets (assume race condition between g2 -> g3 and g4 -> g5)
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2.getUuid(), g6.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2.getUuid(), g5.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g1.getUuid(), g6.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g1.getUuid(), g5.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception 
   */
  public void testWithIncompleteGroupSetHierarchyGroupPrivs() throws Exception { 
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    Group g5 = top.addChildGroup("g5", "g5");
    Group g6 = top.addChildGroup("g6", "g6");
    
    Group g2b = top.addChildGroup("g2b", "g2b");
    Stem s2 = top.addChildStem("s2", "s2");
    AttributeDef a2 = top.addChildAttributeDef("a2", AttributeDefType.attr);
    
    // add some members...
    g1.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g5.addMember(SubjectTestHelper.SUBJ4);
    g6.addMember(SubjectTestHelper.SUBJ5);
    
    // add group sets now
    g1.addMember(g2.toSubject());
    g2.addMember(g3.toSubject());
    g3.addMember(g4.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(g6.toSubject());
    g2b.grantPriv(g3.toSubject(), AccessPrivilege.ADMIN);
    s2.grantPriv(g3.toSubject(), NamingPrivilege.STEM);
    a2.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_READ, true);
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets (assume race condition between g2 -> g3 (group priv) and g4 -> g5)
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2b.getUuid(), g6.getUuid(), FieldFinder.find("admins", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2b.getUuid(), g5.getUuid(), FieldFinder.find("admins", true).getUuid()).iterator().next().delete(false);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception 
   */
  public void testWithIncompleteGroupSetHierarchyAllTypes() throws Exception { 
    setUpComposites();
    
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Group g3 = top.addChildGroup("g3", "g3");
    Group g4 = top.addChildGroup("g4", "g4");
    Group g5 = top.addChildGroup("g5", "g5");
    Group g6 = top.addChildGroup("g6", "g6");
    
    Group g2b = top.addChildGroup("g2b", "g2b");
    Stem s2 = top.addChildStem("s2", "s2");
    AttributeDef a2 = top.addChildAttributeDef("a2", AttributeDefType.attr);
    
    // add some members...
    g1.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g5.addMember(SubjectTestHelper.SUBJ4);
    g6.addMember(SubjectTestHelper.SUBJ5);
    
    // add group sets now
    g1.addMember(g2.toSubject());
    g2.addMember(g3.toSubject());
    g3.addMember(g4.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(g6.toSubject());
    g2b.grantPriv(g3.toSubject(), AccessPrivilege.ADMIN);
    s2.grantPriv(g3.toSubject(), NamingPrivilege.STEM);
    a2.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_READ, true);
    
    int groupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    
    // let's delete group sets
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(s2.getUuid(), g6.getUuid(), FieldFinder.find("stemmers", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(s2.getUuid(), g5.getUuid(), FieldFinder.find("stemmers", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g6.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g5.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(a2.getUuid(), g4.getUuid(), FieldFinder.find("attrReaders", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2b.getUuid(), g6.getUuid(), FieldFinder.find("admins", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2b.getUuid(), g5.getUuid(), FieldFinder.find("admins", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2.getUuid(), g6.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g2.getUuid(), g5.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g1.getUuid(), g6.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    GrouperDAOFactory.getFactory().getGroupSet().findAllByOwnerAndMemberAndField(g1.getUuid(), g5.getUuid(), FieldFinder.find("members", true).getUuid()).iterator().next().delete(false);
    
    // add to point in time
    ChangeLogTempToEntity.convertRecords();

    // should have issues for g3 -> g4 and g4 -> g5
    assertEquals(2, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    // verify we don't mess up point in time
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    // verify counts
    int newGroupSetCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_group_set");
    int newPitGroupSetCountActive = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_pit_group_set where active='T'");
    assertEquals(groupSetCount, newGroupSetCount);
    assertEquals(groupSetCount, newPitGroupSetCountActive);
  }
  
  /**
   * @throws Exception
   */
  public void testDeletedGroupAsMember() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    
    // we will delete g2 but leave membership from g1 -> g2.
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    g1.addMember(g2.toSubject());
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, g1, g2.toSubject(), true);

    ChangeLogTempToEntity.convertRecords();
    g2.delete();
    ChangeLogTempToEntity.convertRecords();

    int originalMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");

    // save bad membership -- we need to disable the membership first or the save will try to add group sets
    membership.setHibernateVersionNumber(-1L);
    membership.setEnabled(false);
    membership.setDisabledTime(new Timestamp(new Date().getTime() - 10000));
    GrouperDAOFactory.getFactory().getMembership().save(membership);

    // now make the membership active again (just so we can test the pit sync as well)
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set enabled='T', disabled_timestamp=null where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setActiveDb("T");
    pitMembership.setEndTimeDb(null);
    pitMembership.update();
    
    int newMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");
    assertEquals(originalMembershipCount + 1, newMembershipCount);
    
    assertEquals(1, FindBadMemberships.checkAll());
    String gsh = "importCommands(\"edu.internet2.middleware.grouper.app.gsh\");\nimport edu.internet2.middleware.grouper.*;\nimport edu.internet2.middleware.grouper.misc.*;\n" + FindBadMemberships.gshScript.toString();
    new Interpreter(new StringReader(gsh), System.out, System.err, false).run();
    assertEquals(0, FindBadMemberships.checkAll());
    
    newMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");
    assertEquals(originalMembershipCount, newMembershipCount);
 
    // pit sync should work
    assertEquals(1, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    assertEquals("F", pitMembership.getActiveDb());
    assertNotNull(pitMembership.getEndTimeDb());
    
    // should be able to delete g1
    grouperSession = SessionHelper.getRootSession();
    g1.delete();
    ChangeLogTempToEntity.convertRecords();
  }
  
  /**
   * The difference here is that we're expecting that deleting the group will do the right thing 
   * (instead of running bad membership finder).
   * @throws Exception
   */
  public void testDeletedGroupAsMember2() throws Exception {
    grouperSession = SessionHelper.getRootSession();
    Stem root = StemFinder.findRootStem(grouperSession);
    top = root.addChildStem("top", "top");
    
    // we will delete g2 but leave membership from g1 -> g2.
    Group g1 = top.addChildGroup("g1", "g1");
    Group g2 = top.addChildGroup("g2", "g2");
    Subject g2Subject = g2.toSubject();
    g1.addMember(g2Subject);
    
    g1.revokePriv(AccessPrivilege.READ);
    g1.revokePriv(AccessPrivilege.VIEW);
    g1.revokePriv(AccessPrivilege.ADMIN);
    
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, g1, g2.toSubject(), true);

    ChangeLogTempToEntity.convertRecords();
    g2.delete();
    ChangeLogTempToEntity.convertRecords();

    int originalMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");

    // save bad membership -- we need to disable the membership first or the save will try to add group sets
    membership.setHibernateVersionNumber(-1L);
    membership.setEnabled(false);
    membership.setDisabledTime(new Timestamp(new Date().getTime() - 10000));
    GrouperDAOFactory.getFactory().getMembership().save(membership);

    // now make the membership active again (just so we can test the pit as well)
    HibernateSession.bySqlStatic().executeSql("update grouper_memberships set enabled='T', disabled_timestamp=null where id='" + membership.getImmediateMembershipId() + "'");
    
    // update bad pit membership
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    pitMembership.setActiveDb("T");
    pitMembership.setEndTimeDb(null);
    pitMembership.update();
    
    int newMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");
    assertEquals(originalMembershipCount + 1, newMembershipCount);

    g1.delete();

    newMembershipCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_memberships");
    assertEquals(originalMembershipCount, newMembershipCount);
 
    // change log should work
    ChangeLogTempToEntity.convertRecords();

    // pit sync should work
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncPITTables().showResults(false).syncAllPITTables());
    
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdUnique(membership.getImmediateMembershipId(), true);
    assertEquals("F", pitMembership.getActiveDb());
    assertNotNull(pitMembership.getEndTimeDb());
  }
}

