/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.pit;

import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SyncPITTables;
import edu.internet2.middleware.grouper.pit.finder.PITGroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITMembershipTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** amount of time to sleep between operations */
  private long sleepTime = 100;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITMembershipTests("testGroupSetAddDeleteAddSameTransaction"));
  }
  
  /**
   * @param name
   */
  public PITMembershipTests(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   */
  public void testGroupSetAddDeleteAdd() {

    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Group group3 = edu.addChildGroup("group3", "group3");    
    Group group4 = edu.addChildGroup("group4", "group4");    

    group2.addMember(group3.toSubject());
    group3.addMember(group4.toSubject());
    
    ChangeLogTempToEntity.convertRecords();

    group1.addMember(group2.toSubject());
    group1.deleteMember(group2.toSubject());
    group1.addMember(group2.toSubject());

    ChangeLogTempToEntity.convertRecords();
    
    // verify
    PITGroup pitGroup1 = PITGroupFinder.findBySourceId(group1.getId(), true).iterator().next();
    assertEquals(3, pitGroup1.getMembers(Group.getDefaultList().getUuid(), null, null, null, null).size());
    
    GroupSet immediateGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(group1.getUuid(), group2.getUuid(), Group.getDefaultList());
    PITGroupSet pitImmediateGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(immediateGroupSet.getId(), false);
    assertTrue(pitImmediateGroupSet.isActive());
  }
  
  /**
   * 
   */
  public void testImmediateMemberships() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.addMember(member0.getSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.deleteMember(member0.getSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    
    // make sure the PIT records still exist after the group is deleted.
    group1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();

    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testImmediateAccessPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    String fieldId = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.grantPriv(member0.getSubject(), AccessPrivilege.UPDATE);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.revokePriv(member0.getSubject(), AccessPrivilege.UPDATE);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    group1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testImmediateNamingPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Stem stem1 = edu.addChildStem("stem1", "stem1");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    String fieldId = FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    stem1.grantPriv(member0.getSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    stem1.revokePriv(member0.getSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    stem1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testImmediateAttributeDefPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    AttributeDef attrDef1 = edu.addChildAttributeDef("attrDef1", AttributeDefType.perm);    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    String fieldId = FieldFinder.find("attrUpdaters", true).getUuid();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    attrDef1.getPrivilegeDelegate().grantPriv(member0.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    attrDef1.getPrivilegeDelegate().revokePriv(member0.getSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    attrDef1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  

  /**
   * 
   */
  public void testEffectiveMemberships() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Group group3 = edu.addChildGroup("group3", "group3");    
    Group group4 = edu.addChildGroup("group4", "group4");    
    Group group5 = edu.addChildGroup("group5", "group5");    
    Group group6 = edu.addChildGroup("group6", "group6");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group4.addMember(group5.toSubject());
    group5.addMember(group6.toSubject());
    group6.addMember(member0.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group3.addMember(group4.toSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group3.deleteMember(group4.toSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    group1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testEffectiveAccessPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group0 = edu.addChildGroup("group0", "group0");    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Group group3 = edu.addChildGroup("group3", "group3");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    String fieldId = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true).getUuid();

    group2.addMember(group3.toSubject());
    group3.addMember(member0.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.grantPriv(group2.toSubject(), AccessPrivilege.UPDATE);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.revokePriv(group2.toSubject(), AccessPrivilege.UPDATE);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    group1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group0.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(member1.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getUuid())
      .setMemberId(group2.toMember().getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
  }
  
  /**
   * 
   */
  public void testEffectiveNamingPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Stem stem1 = edu.addChildStem("stem1", "stem1");
    Stem stem2 = edu.addChildStem("stem2", "stem2");
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    String fieldId = FieldFinder.find(Field.FIELD_NAME_CREATORS, true).getUuid();

    group1.addMember(group2.toSubject());
    group2.addMember(member0.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    stem1.grantPriv(group1.toSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    stem1.revokePriv(group1.toSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    stem1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem2.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(member1.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerStemId(stem1.getUuid())
      .setMemberId(group2.toMember().getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
  }

  /**
   * 
   */
  public void testEffectiveAttributeDefPrivileges() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    AttributeDef attrDef1 = edu.addChildAttributeDef("attrDef1", AttributeDefType.perm);
    AttributeDef attrDef2 = edu.addChildAttributeDef("attrDef2", AttributeDefType.perm);
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    String fieldId = FieldFinder.find("attrUpdaters", true).getUuid();

    group1.addMember(group2.toSubject());
    group2.addMember(member0.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    attrDef1.getPrivilegeDelegate().grantPriv(group1.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    attrDef1.getPrivilegeDelegate().revokePriv(group1.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    attrDef1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef2.getUuid())
      .setMemberId(member0.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(member1.getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerAttrDefId(attrDef1.getUuid())
      .setMemberId(group2.toMember().getUuid())
      .setFieldId(fieldId)
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
  }
  

  /**
   * 
   */
  public void testCompositeMemberships() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Group group2 = edu.addChildGroup("group2", "group2");    
    Group group3 = edu.addChildGroup("group3", "group3");      
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    group2.addMember(member0.getSubject());
    
    ChangeLogTempToEntity.convertRecords();

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.addCompositeMember(CompositeType.UNION, group2, group3);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.deleteCompositeMember();
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();
    group1.delete();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();
    
    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(Group.getDefaultList().getUuid())
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testOtherDateOptions() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    Group group1 = edu.addChildGroup("group1", "group1");    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.addMember(member0.getSubject());
    group1.addMember(member1.getSubject());
    group1.addMember(member2.getSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.deleteMember(member0.getSubject());
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setStartDateAfter(beforeAddTime)
      .execute();
    
    assertEquals(3, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setStartDateAfter(afterAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setStartDateBefore(beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setStartDateBefore(afterAddTime)
      .execute();
    
    assertEquals(3, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateAfter(beforeDeleteTime)
      .execute();
    
    assertEquals(3, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateAfter(afterDeleteTime)
      .execute();
    
    assertEquals(2, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateBefore(beforeDeleteTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setFieldId(Group.getDefaultList().getUuid())
      .setEndDateBefore(afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
  }
  

  /**
   * 
   */
  public void testGroupTypeAssignmentDelete() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    GroupType groupType = GroupType.createType(grouperSession, "testType");
    Field field = groupType.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);

    Group group1 = edu.addChildGroup("group1", "group1");  
    group1.addType(groupType);
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.addMember(member0.getSubject(), field);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.deleteType(groupType);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();

    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * 
   */
  public void testFieldDelete() {
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    GroupType groupType = GroupType.createType(grouperSession, "testType");
    Field field = groupType.addList(grouperSession, "list1", AccessPrivilege.READ, AccessPrivilege.UPDATE);

    Group group1 = edu.addChildGroup("group1", "group1");  
    group1.addType(groupType);
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);

    Date beforeAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    group1.addMember(member0.getSubject(), field);
    GrouperUtil.sleep(sleepTime);
    Date afterAddTime = new Date();
    GrouperUtil.sleep(sleepTime);
    Date beforeDeleteTime = new Date();
    ChangeLogTempToEntity.convertRecords();
    GrouperUtil.sleep(sleepTime);
    group1.deleteMember(member0.getSubject(), field);
    GrouperUtil.sleep(sleepTime);
    Date afterDeleteTime = new Date();

    // make sure the PIT records still exist after the field is deleted.
    groupType.deleteField(grouperSession, field.getName());

    ChangeLogTempToEntity.convertRecords();

    Set<PITMembershipView> results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(new Date(beforeAddTime.getTime() - 1), beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, afterAddTime)
      .execute();

    assertEquals(1, results.size());

    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterAddTime, beforeDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeDeleteTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterDeleteTime, new Date(afterDeleteTime.getTime() + 1))
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(null, beforeAddTime)
      .execute();
    
    assertEquals(0, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(null, afterDeleteTime)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(beforeAddTime, null)
      .execute();
    
    assertEquals(1, results.size());
    
    results = new PITMembershipViewQuery()
      .setOwnerGroupId(group1.getId())
      .setMemberId(member0.getUuid())
      .setFieldId(field.getUuid())
      .setActiveDateRange(afterDeleteTime, null)
      .execute();
    
    assertEquals(0, results.size());
  }
  
  /**
   * requireInGroups have this odd issue where an unassign causes the system of record group
   * to be added, deleted, and added again all in one transaction with the same context id.
   */
  public void testRequireInGroups() {
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    setupTestConfigForIncludeExclude();
    GrouperStartup.initIncludeExcludeType();
    String requireInGroupsTypeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.type.name");
    String requireInGroupsAttributeName = GrouperConfig.getProperty("grouperIncludeExclude.requireGroups.attributeName");
    
    GroupType type = GroupTypeFinder.find(requireInGroupsTypeName, true);
    
    Group group1 = edu.addChildGroup("group1", "group1");
    Group requireGroup = edu.addChildGroup("requireGroup", "requireGroup");
    group1.addType(type);
    group1.setAttribute(requireInGroupsAttributeName, requireGroup.getName());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteType(type);
    
    ChangeLogTempToEntity.convertRecords();
    
    // sync script shouldn't see any reason to sync anything..
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * 
   */
  public void testGroupSetAddDeleteAddSameTransaction() {
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();

    // clear change log
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    final Group group1 = edu.addChildGroup("group1", "group1");    
    final Group group2 = edu.addChildGroup("group2", "group2");    
    final Group group3 = edu.addChildGroup("group3", "group3");    
    final Group group4 = edu.addChildGroup("group4", "group4");    
    final Group group5 = edu.addChildGroup("group5", "group5");    
    final Group group6 = edu.addChildGroup("group6", "group6");    

    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group4.addMember(group5.toSubject());
    group5.addMember(group6.toSubject());
    
    ChangeLogTempToEntity.convertRecords();

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

        group3.addMember(group4.toSubject());
        group3.deleteMember(group4.toSubject());
        group3.addMember(group4.toSubject());
        
        return null;
      }
    });

    ChangeLogTempToEntity.convertRecords();
    
    // sync script shouldn't see any reason to sync anything..
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
  }
}
