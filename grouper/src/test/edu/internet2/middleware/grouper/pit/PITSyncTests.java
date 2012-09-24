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

import java.sql.Timestamp;
import java.util.Date;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SyncPITTables;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;

/**
 * @author shilen
 * $Id$
 */
public class PITSyncTests extends GrouperTest {
  
  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** */
  private Stem edu;
  
  /** */
  private Role role;
  
  /** */
  private Member newMember1;
  
  /** */
  private GroupType groupType;
  
  /** */
  private Field testField;
  
  /** */
  private AttributeDef attributeDef1;
  
  /** */
  private AttributeDefName attributeDefName1;
  
  /** */
  private AttributeAssignAction action1;
  
  /** */
  private AttributeAssign assign1;
  
  /** */
  private AttributeDef attributeDef2;
  
  /** */
  private AttributeDefName attributeDefName2;
  
  /** */
  private AttributeAssign assign2;
  
  /** */
  private AttributeAssignValue value;
  
  /**
   * @param name
   */
  public PITSyncTests(String name) {
    super(name);
  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PITSyncTests("testNotifications"));
  }

  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.includeRolesWithPermissionChanges", "true");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  private void addData() {
    edu = root.addChildStem("edu", "education");
    role = edu.addChildRole("testGroup", "testGroup");
    newMember1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    role.addMember(newMember1.getSubject(), true);
    edu.grantPriv(((Group)role).toSubject(), NamingPrivilege.CREATE);
    
    attributeDef1 = edu.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef1.setAssignToGroup(true);
    attributeDef1.store();
    attributeDefName1 = edu.addChildAttributeDefName(attributeDef1, "testAttribute1", "testAttribute1");
    action1 = attributeDef1.getAttributeDefActionDelegate().addAction("testAction1");
    assign1 = role.getPermissionRoleDelegate().assignRolePermission("testAction1", attributeDefName1, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    attributeDef2 = edu.addChildAttributeDef("attributeDef2", AttributeDefType.attr);
    attributeDef2.setAssignToGroup(true);
    attributeDef2.setValueType(AttributeDefValueType.string);
    attributeDef2.store();
    attributeDefName2 = edu.addChildAttributeDefName(attributeDef2, "testAttribute2", "testAttribute2");
    attributeDef2.getAttributeDefActionDelegate().addAction("testAction2");
    assign2 = ((Group)role).getAttributeDelegate().assignAttribute("testAction2", attributeDefName2).getAttributeAssign();
    value = assign2.getValueDelegate().assignValueString("test").getAttributeAssignValue();
    
    groupType = GroupType.createType(grouperSession, "testType");
    testField = groupType.addList(grouperSession, "testList", AccessPrivilege.READ, AccessPrivilege.ADMIN);
  }
  
  private void deleteData() {
    groupType.deleteField(grouperSession, testField.getName());
    groupType.delete(grouperSession);
    
    attributeDef1.delete();
    attributeDef2.delete();
    role.delete();
    HibernateSession.byObjectStatic().delete(newMember1);
    edu.delete();
  }
  
  private void updateData() {
    action1.setNameDb(action1.getName() + "-a");
    action1.save();
    attributeDefName1.setExtensionDb(attributeDefName1.getExtension() + "-a");
    attributeDefName1.setNameDb(attributeDefName1.getName() + "-a");
    attributeDefName1.store();
    attributeDef1.setExtensionDb(attributeDef1.getExtension() + "-a");
    attributeDef1.setNameDb(attributeDef1.getName() + "-a");
    attributeDef1.store();
    testField.setName(testField.getName() + "-a");
    testField.store();
    ((Group)role).setExtension(role.getExtension() + "-a");
    ((Group)role).store();
    newMember1.setSubjectId(newMember1.getSubjectId() + "-a");
    newMember1.store();
    edu.setExtension(edu.getExtension() + "-a");
    edu.store();
    
    attributeDef1 = GrouperDAOFactory.getFactory().getAttributeDef().findById(attributeDef1.getId(), true);    
    attributeDefName1 = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(attributeDefName1.getId(), true);
    role = GrouperDAOFactory.getFactory().getRole().findById(role.getId(), true);
  }
  
  /**
   * 
   */
  public void testNoChanges() {
    new SyncPITTables().showResults(false).syncAllPITTables();
    grouperSession = GrouperSession.startRootSession();
    addData();
    
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    
    // now delete everything...
    grouperSession = GrouperSession.startRootSession();
    deleteData();
    
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * 
   */
  public void testNoChanges2() {
    
    // in this test case, we will try updating objects...
    
    addData();
    ChangeLogTempToEntity.convertRecords();

    // now update objects
    updateData();
    
    // verify that changes aren't made
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    assertTrue(ChangeLogTempToEntity.convertRecords() > 0);
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
  }
  
  /**
   * 
   */
  public void testNoUpdates() {
    addData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // verify that updates aren't made
    long updates = new SyncPITTables().showResults(false).saveUpdates(false).syncAllPITTables();
    assertTrue(updates > 0);
    assertEquals(updates, new SyncPITTables().showResults(false).saveUpdates(false).syncAllPITTables());
    
    // now sync, delete data, and clear temp change log
    new SyncPITTables().showResults(false).syncAllPITTables();
    grouperSession = GrouperSession.startRootSession();
    deleteData();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    updates = new SyncPITTables().showResults(false).saveUpdates(false).syncAllPITTables();
    assertTrue(updates > 0);
    assertEquals(updates, new SyncPITTables().showResults(false).saveUpdates(false).syncAllPITTables());
  }
  
  /**
   * 
   */
  public void testNoNotifications() {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    addData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // let's sync and make sure there's nothing in the change log
    long updates = new SyncPITTables().showResults(false).sendFlattenedNotifications(false).sendPermissionNotifications(false).syncAllPITTables();
    assertTrue(updates > 0);
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertEquals(0, changeLogTempCount);
    assertEquals(0, changeLogCount);
    
    // now delete data, clear temp change log, and check again
    grouperSession = GrouperSession.startRootSession();
    deleteData();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
  
    updates = new SyncPITTables().showResults(false).sendFlattenedNotifications(false).sendPermissionNotifications(false).syncAllPITTables();
    assertTrue(updates > 0);

    changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    changeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertEquals(0, changeLogTempCount);
    assertEquals(0, changeLogCount);
  }
  
  /**
   * 
   */
  public void testNotifications() {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    addData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // let's sync and make sure there's nothing in the change log
    long updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertTrue(updates > 0);
    
    int changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    int changeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertEquals(0, changeLogTempCount);
    
    // 1 group membership, 2 imm stem privilege, 1 eff stem privilege, 3 group privileges, 2 attribute def privileges, and 1 permission
    // the change log entries are being added by code that's tested elsewhere (not by the sync script) 
    // so we are just verifying the number of entries...
    assertEquals(10, changeLogCount);

    // now delete data, clear temp change log, and check again
    grouperSession = GrouperSession.startRootSession();
    deleteData();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertTrue(updates > 0);

    changeLogTempCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp");
    changeLogCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry");

    assertEquals(0, changeLogTempCount);
    assertEquals(20, changeLogCount);
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testInserts() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    addData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // let's sync and verify that there were updates
    long startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    long updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertTrue(updates > 0);
    Thread.sleep(100);
    long endTime = System.currentTimeMillis() * 1000;
    
    // if we sync again, there should be no updates
    updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertEquals(0, updates);
    grouperSession = GrouperSession.startRootSession();
    
    // now let's verify some of the updates (at least 1 per table)...
    
    
    // check attribute assignment
    {
      PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign1.getId(), false);
      assertNotNull(pitAssign);
      assertEquals(assign1.getAttributeDefNameId(), pitAssign.getPITAttributeDefName().getSourceId());
      assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getPITAttributeAssignAction().getSourceId());
      assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
      assertNull(pitAssign.getOwnerAttributeAssignId());
      assertNull(pitAssign.getOwnerAttributeDefId());
      assertNull(pitAssign.getOwnerMemberId());
      assertNull(pitAssign.getOwnerMembershipId());
      assertNull(pitAssign.getOwnerStemId());
      assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerPITGroup().getSourceId());
      assertEquals("T", pitAssign.getActiveDb());
      assertTrue(pitAssign.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAssign.getStartTimeDb().longValue() < endTime);
      assertNull(pitAssign.getEndTimeDb());
      assertEquals(assign1.getContextId(), pitAssign.getContextId());
    }
    
    // check attribute def
    {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDef1.getId(), false);
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getPITStem().getSourceId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("T", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getEndTimeDb());
      assertEquals(attributeDef1.getContextId(), pitAttributeDef.getContextId());
    }
    
    // check action
    {
      PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action1.getId(), false);
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getPITAttributeDef().getSourceId());
      assertEquals("T", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAction.getStartTimeDb().longValue() < endTime);
      assertNull(pitAction.getEndTimeDb());
      assertEquals(action1.getContextId(), pitAction.getContextId());
    }
    
    // check action set
    {
      AttributeAssignActionSet action1Set = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1.getId()).iterator().next();
      PITAttributeAssignActionSet pitActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(action1Set.getId(), false);
      assertNotNull(pitActionSet);
      assertEquals(action1Set.getDepth(), pitActionSet.getDepth());
      assertEquals(action1Set.getIfHasAttrAssignActionId(), pitActionSet.getIfHasPITAttributeAssignAction().getSourceId());
      assertEquals(action1Set.getThenHasAttrAssignActionId(), pitActionSet.getThenHasPITAttributeAssignAction().getSourceId());
      assertEquals(action1Set.getParentAttrAssignActionSetId(), pitActionSet.getParentPITAttributeAssignActionSet().getSourceId());
      assertEquals("T", pitActionSet.getActiveDb());
      assertTrue(pitActionSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitActionSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitActionSet.getEndTimeDb());
      assertEquals(action1Set.getContextId(), pitActionSet.getContextId());
    }
    
    // check attribute assign value
    {
      PITAttributeAssignValue pitValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdActive(value.getId(), false);
      assertNotNull(pitValue);
      assertEquals(value.getAttributeAssignId(), pitValue.getPITAttributeAssign().getSourceId());
      assertNull(pitValue.getValueInteger());
      assertNull(pitValue.getValueFloating());
      assertNull(pitValue.getValueMemberId());
      assertEquals(value.getValueString(), pitValue.getValueString());
      assertEquals("T", pitValue.getActiveDb());
      assertTrue(pitValue.getStartTimeDb().longValue() > startTime);
      assertTrue(pitValue.getStartTimeDb().longValue() < endTime);
      assertNull(pitValue.getEndTimeDb());
      assertEquals(value.getContextId(), pitValue.getContextId());
    }
    
    // check attribute def name
    {
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefName1.getId(), false);
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getPITStem().getSourceId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getPITAttributeDef().getSourceId());
      assertEquals(attributeDefName1.getName(), pitAttributeDefName.getName());
      assertEquals("T", pitAttributeDefName.getActiveDb());
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefName.getEndTimeDb());
      assertEquals(attributeDefName1.getContextId(), pitAttributeDefName.getContextId());
    }
    
    // check attribute def name set
    {
      AttributeDefNameSet attributeDefName1Set = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1.getId()).iterator().next();
      PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(attributeDefName1Set.getId(), false);
      assertNotNull(pitAttributeDefNameSet);
      assertEquals(attributeDefName1Set.getDepth(), pitAttributeDefNameSet.getDepth());
      assertEquals(attributeDefName1Set.getIfHasAttributeDefNameId(), pitAttributeDefNameSet.getIfHasPITAttributeDefName().getSourceId());
      assertEquals(attributeDefName1Set.getThenHasAttributeDefNameId(), pitAttributeDefNameSet.getThenHasPITAttributeDefName().getSourceId());
      assertEquals(attributeDefName1Set.getParentAttrDefNameSetId(), pitAttributeDefNameSet.getParentPITAttributeDefNameSet().getSourceId());
      assertEquals("T", pitAttributeDefNameSet.getActiveDb());
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefNameSet.getEndTimeDb());
      assertEquals(attributeDefName1Set.getContextId(), pitAttributeDefNameSet.getContextId());
    }
    
    // check field
    {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(testField.getUuid(), false);
      assertNotNull(pitField);
      assertEquals(testField.getName(), pitField.getName());
      assertEquals(testField.getTypeString(), pitField.getType());
      assertEquals("T", pitField.getActiveDb());
      assertTrue(pitField.getStartTimeDb().longValue() > startTime);
      assertTrue(pitField.getStartTimeDb().longValue() < endTime);
      assertNull(pitField.getEndTimeDb());
      assertEquals(testField.getContextId(), pitField.getContextId());
    }
    
    // check group
    {
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(role.getId(), false);
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getPITStem().getSourceId());
      assertEquals("T", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroup.getEndTimeDb());
      assertEquals(((Group)role).getContextId(), pitGroup.getContextId());
    }
    
    // check group set (depth=0)
    {
      GroupSet groupSet = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role.getId(), Group.getDefaultList().getUuid());
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getId(), false);
      assertNotNull(pitGroupSet);
      assertEquals(groupSet.getOwnerId(), pitGroupSet.getOwnerPITGroup().getSourceId());
      assertEquals(groupSet.getOwnerGroupId(), pitGroupSet.getOwnerPITGroup().getSourceId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerStemId());
      assertEquals(groupSet.getMemberId(), pitGroupSet.getMemberPITGroup().getSourceId());
      assertEquals(groupSet.getMemberGroupId(), pitGroupSet.getMemberPITGroup().getSourceId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet.getFieldId(), pitGroupSet.getPITField().getSourceId());
      assertEquals(groupSet.getMemberFieldId(), pitGroupSet.getMemberPITField().getSourceId());
      assertEquals(groupSet.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet.getParentId(), pitGroupSet.getParentPITGroupSet().getSourceId());
      assertEquals("T", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getEndTimeDb());
      assertEquals(groupSet.getContextId(), pitGroupSet.getContextId());
    }
    
    // check group set (depth=1)
    {
      GroupSet groupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(edu.getUuid(), role.getId(), FieldFinder.find(Field.FIELD_NAME_CREATORS, true));
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet.getId(), false);
      assertNotNull(pitGroupSet);
      assertEquals(groupSet.getOwnerId(), pitGroupSet.getOwnerPITStem().getSourceId());
      assertEquals(groupSet.getOwnerStemId(), pitGroupSet.getOwnerPITStem().getSourceId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerGroupId());
      assertEquals(groupSet.getMemberId(), pitGroupSet.getMemberPITGroup().getSourceId());
      assertEquals(groupSet.getMemberGroupId(), pitGroupSet.getMemberPITGroup().getSourceId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet.getFieldId(), pitGroupSet.getPITField().getSourceId());
      assertEquals(groupSet.getMemberFieldId(), pitGroupSet.getMemberPITField().getSourceId());
      assertEquals(groupSet.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet.getParentId(), pitGroupSet.getParentPITGroupSet().getSourceId());
      assertEquals("T", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getEndTimeDb());
      assertEquals(groupSet.getContextId(), pitGroupSet.getContextId());
    }
    
    // check member
    {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(((Group)role).toMember().getUuid(), false);
      assertNotNull(pitMember);
      assertEquals(((Group)role).toMember().getSubjectId(), pitMember.getSubjectId());
      assertEquals(((Group)role).toMember().getSubjectSourceId(), pitMember.getSubjectSourceId());
      assertEquals(((Group)role).toMember().getSubjectTypeId(), pitMember.getSubjectTypeId());
      assertEquals("T", pitMember.getActiveDb());
      assertTrue(pitMember.getStartTimeDb().longValue() > startTime);
      assertTrue(pitMember.getStartTimeDb().longValue() < endTime);
      assertNull(pitMember.getEndTimeDb());
      assertEquals(((Group)role).toMember().getContextId(), pitMember.getContextId());
    }
    
    // check membership
    {
      Membership membership = MembershipFinder.findImmediateMembership(grouperSession, (Group)role, newMember1.getSubject(), Group.getDefaultList(), true);
      PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), false);
      assertNotNull(pitMembership);
      assertEquals(membership.getOwnerId(), pitMembership.getOwnerPITGroup().getSourceId());
      assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerPITGroup().getSourceId());
      assertNull(pitMembership.getOwnerStemId());
      assertNull(pitMembership.getOwnerAttrDefId());
      assertEquals(membership.getMemberUuid(), pitMembership.getPITMember().getSourceId());
      assertEquals(membership.getFieldId(), pitMembership.getPITField().getSourceId());
      assertEquals("T", pitMembership.getActiveDb());
      assertTrue(pitMembership.getStartTimeDb().longValue() > startTime);
      assertTrue(pitMembership.getStartTimeDb().longValue() < endTime);
      assertNull(pitMembership.getEndTimeDb());
      assertEquals(membership.getContextId(), pitMembership.getContextId());
    }
    
    // check role set
    {
      RoleSet roleSet = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role.getId()).iterator().next();
      PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(roleSet.getId(), false);
      assertNotNull(pitRoleSet);
      assertEquals(roleSet.getDepth(), pitRoleSet.getDepth());
      assertEquals(roleSet.getIfHasRoleId(), pitRoleSet.getIfHasPITRole().getSourceId());
      assertEquals(roleSet.getThenHasRoleId(), pitRoleSet.getThenHasPITRole().getSourceId());
      assertEquals(roleSet.getParentRoleSetId(), pitRoleSet.getParentPITRoleSet().getSourceId());
      assertEquals("T", pitRoleSet.getActiveDb());
      assertTrue(pitRoleSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitRoleSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitRoleSet.getEndTimeDb());
      assertEquals(roleSet.getContextId(), pitRoleSet.getContextId());
    }
    
    // check stem
    {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(edu.getUuid(), false);
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentPITStem().getSourceId());
      assertEquals("T", pitStem.getActiveDb());
      assertTrue(pitStem.getStartTimeDb().longValue() > startTime);
      assertTrue(pitStem.getStartTimeDb().longValue() < endTime);
      assertNull(pitStem.getEndTimeDb());
      assertEquals(edu.getContextId(), pitStem.getContextId());
    }
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testDeletes() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    addData();
    ChangeLogTempToEntity.convertRecords();
    
    AttributeAssignActionSet action1Set = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1.getId()).iterator().next();
    AttributeDefNameSet attributeDefName1Set = GrouperDAOFactory.getFactory().getAttributeDefNameSet().findByIfHasAttributeDefNameId(attributeDefName1.getId()).iterator().next();
    GroupSet groupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role.getId(), Group.getDefaultList().getUuid());
    GroupSet groupSet2 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(edu.getUuid(), role.getId(), FieldFinder.find(Field.FIELD_NAME_CREATORS, true));
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, (Group)role, newMember1.getSubject(), Group.getDefaultList(), true);
    RoleSet roleSet = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role.getId()).iterator().next();

    // need to keep the point in time ids
    PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign1.getId(), false);
    PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDef1.getId(), false);
    PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action1.getId(), false);
    PITAttributeAssignActionSet pitActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findBySourceIdActive(action1Set.getId(), false);
    PITAttributeAssignValue pitValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findBySourceIdActive(value.getId(), false);
    PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefName1.getId(), false);
    PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findBySourceIdActive(attributeDefName1Set.getId(), false);
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(testField.getUuid(), false);
    PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(role.getId(), false);
    PITGroupSet pitGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet1.getId(), false);
    PITGroupSet pitGroupSet2 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(groupSet2.getId(), false);
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(newMember1.getUuid(), false);
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), false);
    PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findBySourceIdActive(roleSet.getId(), false);
    PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(edu.getUuid(), false);

    deleteData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // let's sync and verify that there were updates
    long startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    long updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertTrue(updates > 0);
    Thread.sleep(100);
    long endTime = System.currentTimeMillis() * 1000;
    
    // if we sync again, there should be no updates
    updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertEquals(0, updates);
    grouperSession = GrouperSession.startRootSession();
    
    // now let's verify some of the updates (at least 1 per table)...
    
    
    // check attribute assignment
    {
      pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(pitAssign.getId(), false);
      assertNotNull(pitAssign);
      assertEquals(assign1.getAttributeDefNameId(), pitAssign.getPITAttributeDefName().getSourceId());
      assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getPITAttributeAssignAction().getSourceId());
      assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
      assertNull(pitAssign.getOwnerAttributeAssignId());
      assertNull(pitAssign.getOwnerAttributeDefId());
      assertNull(pitAssign.getOwnerMemberId());
      assertNull(pitAssign.getOwnerMembershipId());
      assertNull(pitAssign.getOwnerStemId());
      assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerPITGroup().getSourceId());
      assertEquals("F", pitAssign.getActiveDb());
      assertTrue(pitAssign.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAssign.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAssign.getEndTimeDb().longValue() < endTime);
      assertNull(pitAssign.getContextId());
    }
    
    // check attribute def
    {
      pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(pitAttributeDef.getId(), false);
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getPITStem().getSourceId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("F", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDef.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getContextId());
    }
    
    // check action
    {
      pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(pitAction.getId(), false);
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getPITAttributeDef().getSourceId());
      assertEquals("F", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAction.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAction.getEndTimeDb().longValue() < endTime);
      assertNull(pitAction.getContextId());
    }
    
    // check action set
    {
      pitActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(pitActionSet.getId(), false);
      assertNotNull(pitActionSet);
      assertEquals(action1Set.getDepth(), pitActionSet.getDepth());
      assertEquals(action1Set.getIfHasAttrAssignActionId(), pitActionSet.getIfHasPITAttributeAssignAction().getSourceId());
      assertEquals(action1Set.getThenHasAttrAssignActionId(), pitActionSet.getThenHasPITAttributeAssignAction().getSourceId());
      assertEquals(action1Set.getParentAttrAssignActionSetId(), pitActionSet.getParentPITAttributeAssignActionSet().getSourceId());
      assertEquals("F", pitActionSet.getActiveDb());
      assertTrue(pitActionSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitActionSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitActionSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitActionSet.getContextId());
    }
    
    // check attribute assign value
    {
      pitValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(pitValue.getId(), false);
      assertNotNull(pitValue);
      assertEquals(value.getAttributeAssignId(), pitValue.getPITAttributeAssign().getSourceId());
      assertNull(pitValue.getValueInteger());
      assertNull(pitValue.getValueFloating());
      assertNull(pitValue.getValueMemberId());
      assertEquals(value.getValueString(), pitValue.getValueString());
      assertEquals("F", pitValue.getActiveDb());
      assertTrue(pitValue.getStartTimeDb().longValue() < startTime);
      assertTrue(pitValue.getEndTimeDb().longValue() > startTime);
      assertTrue(pitValue.getEndTimeDb().longValue() < endTime);
      assertNull(pitValue.getContextId());
    }
    
    // check attribute def name
    {
      pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(pitAttributeDefName.getId(), false);
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getPITStem().getSourceId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getPITAttributeDef().getSourceId());
      assertEquals(attributeDefName1.getName(), pitAttributeDefName.getName());
      assertEquals("F", pitAttributeDefName.getActiveDb());
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDefName.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefName.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefName.getContextId());
    }
    
    // check attribute def name set
    {
      pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(pitAttributeDefNameSet.getId(), false);
      assertNotNull(pitAttributeDefNameSet);
      assertEquals(attributeDefName1Set.getDepth(), pitAttributeDefNameSet.getDepth());
      assertEquals(attributeDefName1Set.getIfHasAttributeDefNameId(), pitAttributeDefNameSet.getIfHasPITAttributeDefName().getSourceId());
      assertEquals(attributeDefName1Set.getThenHasAttributeDefNameId(), pitAttributeDefNameSet.getThenHasPITAttributeDefName().getSourceId());
      assertEquals(attributeDefName1Set.getParentAttrDefNameSetId(), pitAttributeDefNameSet.getParentPITAttributeDefNameSet().getSourceId());
      assertEquals("F", pitAttributeDefNameSet.getActiveDb());
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDefNameSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefNameSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefNameSet.getContextId());
    }
    
    // check field
    {
      pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitField.getId(), false);
      assertNotNull(pitField);
      assertEquals(testField.getName(), pitField.getName());
      assertEquals(testField.getTypeString(), pitField.getType());
      assertEquals("F", pitField.getActiveDb());
      assertTrue(pitField.getStartTimeDb().longValue() < startTime);
      assertTrue(pitField.getEndTimeDb().longValue() > startTime);
      assertTrue(pitField.getEndTimeDb().longValue() < endTime);
      assertNull(pitField.getContextId());
    }
    
    // check group
    {
      pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitGroup.getId(), false);
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getPITStem().getSourceId());
      assertEquals("F", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroup.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroup.getContextId());
    }
    
    // check group set (depth=0)
    {
      pitGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findById(pitGroupSet1.getId(), false);
      assertNotNull(pitGroupSet2);
      assertEquals(groupSet1.getOwnerId(), pitGroupSet1.getOwnerPITGroup().getSourceId());
      assertEquals(groupSet1.getOwnerGroupId(), pitGroupSet1.getOwnerPITGroup().getSourceId());
      assertNull(pitGroupSet1.getOwnerAttrDefId());
      assertNull(pitGroupSet1.getOwnerStemId());
      assertEquals(groupSet1.getMemberId(), pitGroupSet1.getMemberPITGroup().getSourceId());
      assertEquals(groupSet1.getMemberGroupId(), pitGroupSet1.getMemberPITGroup().getSourceId());
      assertNull(pitGroupSet1.getMemberAttrDefId());
      assertNull(pitGroupSet1.getMemberStemId());
      assertEquals(groupSet1.getFieldId(), pitGroupSet1.getPITField().getSourceId());
      assertEquals(groupSet1.getMemberFieldId(), pitGroupSet1.getMemberPITField().getSourceId());
      assertEquals(groupSet1.getDepth(), pitGroupSet1.getDepth());
      assertEquals(groupSet1.getParentId(), pitGroupSet1.getParentPITGroupSet().getSourceId());
      assertEquals("F", pitGroupSet1.getActiveDb());
      assertTrue(pitGroupSet1.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroupSet1.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet1.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroupSet1.getContextId());
    }
    
    // check group set (depth=1)
    {
      pitGroupSet2 = GrouperDAOFactory.getFactory().getPITGroupSet().findById(pitGroupSet2.getId(), false);
      assertNotNull(pitGroupSet2);
      assertEquals(groupSet2.getOwnerId(), pitGroupSet2.getOwnerPITStem().getSourceId());
      assertEquals(groupSet2.getOwnerStemId(), pitGroupSet2.getOwnerPITStem().getSourceId());
      assertNull(pitGroupSet2.getOwnerAttrDefId());
      assertNull(pitGroupSet2.getOwnerGroupId());
      assertEquals(groupSet2.getMemberId(), pitGroupSet2.getMemberPITGroup().getSourceId());
      assertEquals(groupSet2.getMemberGroupId(), pitGroupSet2.getMemberPITGroup().getSourceId());
      assertNull(pitGroupSet2.getMemberAttrDefId());
      assertNull(pitGroupSet2.getMemberStemId());
      assertEquals(groupSet2.getFieldId(), pitGroupSet2.getPITField().getSourceId());
      assertEquals(groupSet2.getMemberFieldId(), pitGroupSet2.getMemberPITField().getSourceId());
      assertEquals(groupSet2.getDepth(), pitGroupSet2.getDepth());
      assertEquals(groupSet2.getParentId(), pitGroupSet2.getParentPITGroupSet().getSourceId());
      assertEquals("F", pitGroupSet2.getActiveDb());
      assertTrue(pitGroupSet2.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroupSet2.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet2.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroupSet2.getContextId());
    }
    
    // check member
    {
      pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(pitMember.getId(), false);
      assertNotNull(pitMember);
      assertEquals(newMember1.getSubjectId(), pitMember.getSubjectId());
      assertEquals(newMember1.getSubjectSourceId(), pitMember.getSubjectSourceId());
      assertEquals(newMember1.getSubjectTypeId(), pitMember.getSubjectTypeId());
      assertEquals("F", pitMember.getActiveDb());
      assertTrue(pitMember.getStartTimeDb().longValue() < startTime);
      assertTrue(pitMember.getEndTimeDb().longValue() > startTime);
      assertTrue(pitMember.getEndTimeDb().longValue() < endTime);
      assertNull(pitMember.getContextId());
    }
    
    // check membership
    {
      pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(pitMembership.getId(), false);
      assertNotNull(pitMembership);
      assertEquals(membership.getOwnerId(), pitMembership.getOwnerPITGroup().getSourceId());
      assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerPITGroup().getSourceId());
      assertNull(pitMembership.getOwnerStemId());
      assertNull(pitMembership.getOwnerAttrDefId());
      assertEquals(membership.getMemberUuid(), pitMembership.getPITMember().getSourceId());
      assertEquals(membership.getFieldId(), pitMembership.getPITField().getSourceId());
      assertEquals("F", pitMembership.getActiveDb());
      assertTrue(pitMembership.getStartTimeDb().longValue() < startTime);
      assertTrue(pitMembership.getEndTimeDb().longValue() > startTime);
      assertTrue(pitMembership.getEndTimeDb().longValue() < endTime);
      assertNull(pitMembership.getContextId());
    }
    
    // check role set
    {
      pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findById(pitRoleSet.getId(), false);
      assertNotNull(pitRoleSet);
      assertEquals(roleSet.getDepth(), pitRoleSet.getDepth());
      assertEquals(roleSet.getIfHasRoleId(), pitRoleSet.getIfHasPITRole().getSourceId());
      assertEquals(roleSet.getThenHasRoleId(), pitRoleSet.getThenHasPITRole().getSourceId());
      assertEquals(roleSet.getParentRoleSetId(), pitRoleSet.getParentPITRoleSet().getSourceId());
      assertEquals("F", pitRoleSet.getActiveDb());
      assertTrue(pitRoleSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitRoleSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitRoleSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitRoleSet.getContextId());
    }
    
    // check stem
    {
      pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(pitStem.getId(), false);
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentPITStem().getSourceId());
      assertEquals("F", pitStem.getActiveDb());
      assertTrue(pitStem.getStartTimeDb().longValue() < startTime);
      assertTrue(pitStem.getEndTimeDb().longValue() > startTime);
      assertTrue(pitStem.getEndTimeDb().longValue() < endTime);
      assertNull(pitStem.getContextId());
    }
  }
  
  /**
   * @throws Exception 
   * 
   */
  public void testUpdates() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    long startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    addData();
    ChangeLogTempToEntity.convertRecords();

    Thread.sleep(100);
    long endTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);

    // now update some data
    updateData();
    
    // clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // let's sync and verify that there were updates
    long updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertTrue(updates > 0);

    // if we sync again, there should be no updates
    updates = new SyncPITTables().showResults(false).syncAllPITTables();
    assertEquals(0, updates);
    grouperSession = GrouperSession.startRootSession();
    
    // now let's verify some of the updates (at least 1 per table)...
    
    
    // check attribute def
    {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(attributeDef1.getId(), false);
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getPITStem().getSourceId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("T", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getEndTimeDb());
      assertEquals(attributeDef1.getContextId(), pitAttributeDef.getContextId());
    }
    
    // check action
    {
      PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findBySourceIdActive(action1.getId(), false);
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getPITAttributeDef().getSourceId());
      assertEquals("T", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAction.getStartTimeDb().longValue() < endTime);
      assertNull(pitAction.getEndTimeDb());
      assertEquals(action1.getContextId(), pitAction.getContextId());
    }
    
    // check attribute def name
    {
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findBySourceIdActive(attributeDefName1.getId(), false);
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getPITStem().getSourceId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getPITAttributeDef().getSourceId());
      assertEquals(attributeDefName1.getName(), pitAttributeDefName.getName());
      assertEquals("T", pitAttributeDefName.getActiveDb());
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefName.getEndTimeDb());
      assertEquals(attributeDefName1.getContextId(), pitAttributeDefName.getContextId());
    }
    
    // check field
    {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(testField.getUuid(), false);
      assertNotNull(pitField);
      assertEquals(testField.getName(), pitField.getName());
      assertEquals(testField.getTypeString(), pitField.getType());
      assertEquals("T", pitField.getActiveDb());
      assertTrue(pitField.getStartTimeDb().longValue() > startTime);
      assertTrue(pitField.getStartTimeDb().longValue() < endTime);
      assertNull(pitField.getEndTimeDb());
      
      if (testField.getContextId() == null) {
        assertNull(pitField.getContextId());
      } else {
        assertEquals(testField.getContextId(), pitField.getContextId());
      }
    }
    
    // check group
    {
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(role.getId(), false);
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getPITStem().getSourceId());
      assertEquals("T", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroup.getEndTimeDb());
      assertEquals(((Group)role).getContextId(), pitGroup.getContextId());
    }
    
    // check member
    {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findBySourceIdActive(newMember1.getUuid(), false);
      assertNotNull(pitMember);
      assertEquals(newMember1.getSubjectId(), pitMember.getSubjectId());
      assertEquals(newMember1.getSubjectSourceId(), pitMember.getSubjectSourceId());
      assertEquals(newMember1.getSubjectTypeId(), pitMember.getSubjectTypeId());
      assertEquals("T", pitMember.getActiveDb());
      assertTrue(pitMember.getStartTimeDb().longValue() > startTime);
      assertTrue(pitMember.getStartTimeDb().longValue() < endTime);
      assertNull(pitMember.getEndTimeDb());
      assertEquals(newMember1.getContextId(), pitMember.getContextId());
    }
    
    // check stem
    {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(edu.getUuid(), false);
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentPITStem().getSourceId());
      assertEquals("T", pitStem.getActiveDb());
      assertTrue(pitStem.getStartTimeDb().longValue() > startTime);
      assertTrue(pitStem.getStartTimeDb().longValue() < endTime);
      assertNull(pitStem.getEndTimeDb());
      assertEquals(edu.getContextId(), pitStem.getContextId());
    }
  }

  /**
   * @throws Exception 
   */
  public void testDisabledMembership() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    addData();
    ChangeLogTempToEntity.convertRecords();
    
    // now disable a membership
    Membership membership = MembershipFinder.findImmediateMembership(grouperSession, (Group)role, newMember1.getSubject(), Group.getDefaultList(), true);
    String pitMembershipId = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true).getId();
    membership.setEnabled(false);
    membership.setDisabledTime(new Timestamp(new Date().getTime() - 10000));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    // there should be no updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // now clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    Thread.sleep(100);
    long startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there should be one update
    assertEquals(1, new SyncPITTables().showResults(false).syncAllPITTables());

    Thread.sleep(100);
    long endTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there shouldn't be any updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // verify the point in time update
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(pitMembershipId, false);
    assertNotNull(pitMembership);
    assertEquals(membership.getOwnerId(), pitMembership.getOwnerPITGroup().getSourceId());
    assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerPITGroup().getSourceId());
    assertNull(pitMembership.getOwnerStemId());
    assertNull(pitMembership.getOwnerAttrDefId());
    assertEquals(membership.getMemberUuid(), pitMembership.getPITMember().getSourceId());
    assertEquals(membership.getFieldId(), pitMembership.getPITField().getSourceId());
    assertEquals("F", pitMembership.getActiveDb());
    assertTrue(pitMembership.getStartTimeDb().longValue() < startTime);
    assertTrue(pitMembership.getEndTimeDb().longValue() > startTime);
    assertTrue(pitMembership.getEndTimeDb().longValue() < endTime);
    assertNull(pitMembership.getContextId());
    
    // now activate the membership
    membership.setEnabled(true);
    membership.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    
    // there should be no updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // now clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    Thread.sleep(100);
    startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there should be one update
    assertEquals(1, new SyncPITTables().showResults(false).syncAllPITTables());

    Thread.sleep(100);
    endTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there shouldn't be any updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // verify the point in time update
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), false);
    assertNotNull(pitMembership);
    assertEquals(membership.getOwnerId(), pitMembership.getOwnerPITGroup().getSourceId());
    assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerPITGroup().getSourceId());
    assertNull(pitMembership.getOwnerStemId());
    assertNull(pitMembership.getOwnerAttrDefId());
    assertEquals(membership.getMemberUuid(), pitMembership.getPITMember().getSourceId());
    assertEquals(membership.getFieldId(), pitMembership.getPITField().getSourceId());
    assertEquals("T", pitMembership.getActiveDb());
    assertTrue(pitMembership.getStartTimeDb().longValue() > startTime);
    assertTrue(pitMembership.getStartTimeDb().longValue() < endTime);
    assertNull(pitMembership.getEndTimeDb());
    assertEquals(membership.getContextId(), pitMembership.getContextId());
  }
  
  /**
   * @throws Exception 
   */
  public void testDisabledAttributeAssignment() throws Exception {
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    addData();
    ChangeLogTempToEntity.convertRecords();
    String pitAssignId = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign1.getId(), true).getId();
    
    // now disable an assignment
    assign1.setEnabled(false);
    assign1.setDisabledTime(new Timestamp(new Date().getTime() - 10000));
    GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(assign1);
    
    // there should be no updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // now clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    Thread.sleep(100);
    long startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there should be one update
    assertEquals(1, new SyncPITTables().showResults(false).syncAllPITTables());

    Thread.sleep(100);
    long endTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there shouldn't be any updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // verify the point in time update
    PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(pitAssignId, false);
    assertNotNull(pitAssign);
    assertEquals(assign1.getAttributeDefNameId(), pitAssign.getPITAttributeDefName().getSourceId());
    assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getPITAttributeAssignAction().getSourceId());
    assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
    assertNull(pitAssign.getOwnerAttributeAssignId());
    assertNull(pitAssign.getOwnerAttributeDefId());
    assertNull(pitAssign.getOwnerMemberId());
    assertNull(pitAssign.getOwnerMembershipId());
    assertNull(pitAssign.getOwnerStemId());
    assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerPITGroup().getSourceId());
    assertEquals("F", pitAssign.getActiveDb());
    assertTrue(pitAssign.getStartTimeDb().longValue() < startTime);
    assertTrue(pitAssign.getEndTimeDb().longValue() > startTime);
    assertTrue(pitAssign.getEndTimeDb().longValue() < endTime);
    assertNull(pitAssign.getContextId());
    
    // now activate the membership
    assign1.setEnabled(true);
    assign1.setDisabledTime(null);
    GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(assign1);
    
    // there should be no updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // now clear temp change log
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    Thread.sleep(100);
    startTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there should be one update
    assertEquals(1, new SyncPITTables().showResults(false).syncAllPITTables());

    Thread.sleep(100);
    endTime = System.currentTimeMillis() * 1000;
    Thread.sleep(100);
    
    // now there shouldn't be any updates
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());

    // verify the point in time update
    pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdActive(assign1.getId(), false);
    assertNotNull(pitAssign);
    assertEquals(assign1.getAttributeDefNameId(), pitAssign.getPITAttributeDefName().getSourceId());
    assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getPITAttributeAssignAction().getSourceId());
    assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
    assertNull(pitAssign.getOwnerAttributeAssignId());
    assertNull(pitAssign.getOwnerAttributeDefId());
    assertNull(pitAssign.getOwnerMemberId());
    assertNull(pitAssign.getOwnerMembershipId());
    assertNull(pitAssign.getOwnerStemId());
    assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerPITGroup().getSourceId());
    assertEquals("T", pitAssign.getActiveDb());
    assertTrue(pitAssign.getStartTimeDb().longValue() > startTime);
    assertTrue(pitAssign.getStartTimeDb().longValue() < endTime);
    assertNull(pitAssign.getEndTimeDb());
    assertEquals(assign1.getContextId(), pitAssign.getContextId());
  }
}
