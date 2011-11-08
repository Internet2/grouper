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
    TestRunner.run(new PITSyncTests("testNoChanges"));
  }

  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    
    GrouperLoaderConfig.testConfig.put("changeLog.includeRolesWithPermissionChanges", "true");
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
      PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(assign1.getId());
      assertNotNull(pitAssign);
      assertEquals(assign1.getAttributeDefNameId(), pitAssign.getAttributeDefNameId());
      assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getAttributeAssignActionId());
      assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
      assertNull(pitAssign.getOwnerAttributeAssignId());
      assertNull(pitAssign.getOwnerAttributeDefId());
      assertNull(pitAssign.getOwnerMemberId());
      assertNull(pitAssign.getOwnerMembershipId());
      assertNull(pitAssign.getOwnerStemId());
      assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerGroupId());
      assertEquals("T", pitAssign.getActiveDb());
      assertTrue(pitAssign.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAssign.getStartTimeDb().longValue() < endTime);
      assertNull(pitAssign.getEndTimeDb());
      assertEquals(assign1.getContextId(), pitAssign.getContextId());
    }
    
    // check attribute def
    {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef1.getId());
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getStemId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("T", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getEndTimeDb());
      assertEquals(attributeDef1.getContextId(), pitAttributeDef.getContextId());
    }
    
    // check action
    {
      PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action1.getId());
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getAttributeDefId());
      assertEquals("T", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAction.getStartTimeDb().longValue() < endTime);
      assertNull(pitAction.getEndTimeDb());
      assertEquals(action1.getContextId(), pitAction.getContextId());
    }
    
    // check action set
    {
      AttributeAssignActionSet action1Set = GrouperDAOFactory.getFactory().getAttributeAssignActionSet().findByIfHasAttributeAssignActionId(action1.getId()).iterator().next();
      PITAttributeAssignActionSet pitActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(action1Set.getId());
      assertNotNull(pitActionSet);
      assertEquals(action1Set.getDepth(), pitActionSet.getDepth());
      assertEquals(action1Set.getIfHasAttrAssignActionId(), pitActionSet.getIfHasAttrAssignActionId());
      assertEquals(action1Set.getThenHasAttrAssignActionId(), pitActionSet.getThenHasAttrAssignActionId());
      assertEquals(action1Set.getParentAttrAssignActionSetId(), pitActionSet.getParentAttrAssignActionSetId());
      assertEquals("T", pitActionSet.getActiveDb());
      assertTrue(pitActionSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitActionSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitActionSet.getEndTimeDb());
      assertEquals(action1Set.getContextId(), pitActionSet.getContextId());
    }
    
    // check attribute assign value
    {
      PITAttributeAssignValue pitValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value.getId());
      assertNotNull(pitValue);
      assertEquals(value.getAttributeAssignId(), pitValue.getAttributeAssignId());
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
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName1.getId());
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getStemId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getAttributeDefId());
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
      PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefName1Set.getId());
      assertNotNull(pitAttributeDefNameSet);
      assertEquals(attributeDefName1Set.getDepth(), pitAttributeDefNameSet.getDepth());
      assertEquals(attributeDefName1Set.getIfHasAttributeDefNameId(), pitAttributeDefNameSet.getIfHasAttributeDefNameId());
      assertEquals(attributeDefName1Set.getThenHasAttributeDefNameId(), pitAttributeDefNameSet.getThenHasAttributeDefNameId());
      assertEquals(attributeDefName1Set.getParentAttrDefNameSetId(), pitAttributeDefNameSet.getParentAttrDefNameSetId());
      assertEquals("T", pitAttributeDefNameSet.getActiveDb());
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefNameSet.getEndTimeDb());
      assertEquals(attributeDefName1Set.getContextId(), pitAttributeDefNameSet.getContextId());
    }
    
    // check field
    {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(testField.getUuid());
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
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(role.getId());
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getStemId());
      assertEquals("T", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroup.getEndTimeDb());
      assertEquals(((Group)role).getContextId(), pitGroup.getContextId());
    }
    
    // check group set (depth=0)
    {
      GroupSet groupSet = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(role.getId(), Group.getDefaultList().getUuid());
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet.getId());
      assertNotNull(pitGroupSet);
      assertEquals(groupSet.getOwnerId(), pitGroupSet.getOwnerId());
      assertEquals(groupSet.getOwnerGroupId(), pitGroupSet.getOwnerGroupId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerStemId());
      assertEquals(groupSet.getMemberId(), pitGroupSet.getMemberId());
      assertEquals(groupSet.getMemberGroupId(), pitGroupSet.getMemberGroupId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet.getFieldId(), pitGroupSet.getFieldId());
      assertEquals(groupSet.getMemberFieldId(), pitGroupSet.getMemberFieldId());
      assertEquals(groupSet.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet.getParentId(), pitGroupSet.getParentId());
      assertEquals("T", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getEndTimeDb());
      assertEquals(groupSet.getContextId(), pitGroupSet.getContextId());
    }
    
    // check group set (depth=1)
    {
      GroupSet groupSet = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(edu.getUuid(), role.getId(), FieldFinder.find(Field.FIELD_NAME_CREATORS, true));
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet.getId());
      assertNotNull(pitGroupSet);
      assertEquals(groupSet.getOwnerId(), pitGroupSet.getOwnerId());
      assertEquals(groupSet.getOwnerStemId(), pitGroupSet.getOwnerStemId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerGroupId());
      assertEquals(groupSet.getMemberId(), pitGroupSet.getMemberId());
      assertEquals(groupSet.getMemberGroupId(), pitGroupSet.getMemberGroupId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet.getFieldId(), pitGroupSet.getFieldId());
      assertEquals(groupSet.getMemberFieldId(), pitGroupSet.getMemberFieldId());
      assertEquals(groupSet.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet.getParentId(), pitGroupSet.getParentId());
      assertEquals("T", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getEndTimeDb());
      assertEquals(groupSet.getContextId(), pitGroupSet.getContextId());
    }
    
    // check member
    {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(((Group)role).toMember().getUuid());
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
      PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(membership.getImmediateMembershipId());
      assertNotNull(pitMembership);
      assertEquals(membership.getOwnerId(), pitMembership.getOwnerId());
      assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerGroupId());
      assertNull(pitMembership.getOwnerStemId());
      assertNull(pitMembership.getOwnerAttrDefId());
      assertEquals(membership.getMemberUuid(), pitMembership.getMemberId());
      assertEquals(membership.getFieldId(), pitMembership.getFieldId());
      assertEquals("T", pitMembership.getActiveDb());
      assertTrue(pitMembership.getStartTimeDb().longValue() > startTime);
      assertTrue(pitMembership.getStartTimeDb().longValue() < endTime);
      assertNull(pitMembership.getEndTimeDb());
      assertEquals(membership.getContextId(), pitMembership.getContextId());
    }
    
    // check role set
    {
      RoleSet roleSet = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(role.getId()).iterator().next();
      PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet.getId());
      assertNotNull(pitRoleSet);
      assertEquals(roleSet.getDepth(), pitRoleSet.getDepth());
      assertEquals(roleSet.getIfHasRoleId(), pitRoleSet.getIfHasRoleId());
      assertEquals(roleSet.getThenHasRoleId(), pitRoleSet.getThenHasRoleId());
      assertEquals(roleSet.getParentRoleSetId(), pitRoleSet.getParentRoleSetId());
      assertEquals("T", pitRoleSet.getActiveDb());
      assertTrue(pitRoleSet.getStartTimeDb().longValue() > startTime);
      assertTrue(pitRoleSet.getStartTimeDb().longValue() < endTime);
      assertNull(pitRoleSet.getEndTimeDb());
      assertEquals(roleSet.getContextId(), pitRoleSet.getContextId());
    }
    
    // check stem
    {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(edu.getUuid());
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentStemId());
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
      PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(assign1.getId());
      assertNotNull(pitAssign);
      assertEquals(assign1.getAttributeDefNameId(), pitAssign.getAttributeDefNameId());
      assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getAttributeAssignActionId());
      assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
      assertNull(pitAssign.getOwnerAttributeAssignId());
      assertNull(pitAssign.getOwnerAttributeDefId());
      assertNull(pitAssign.getOwnerMemberId());
      assertNull(pitAssign.getOwnerMembershipId());
      assertNull(pitAssign.getOwnerStemId());
      assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerGroupId());
      assertEquals("F", pitAssign.getActiveDb());
      assertTrue(pitAssign.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAssign.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAssign.getEndTimeDb().longValue() < endTime);
      assertNull(pitAssign.getContextId());
    }
    
    // check attribute def
    {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef1.getId());
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getStemId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("F", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDef.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getContextId());
    }
    
    // check action
    {
      PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action1.getId());
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getAttributeDefId());
      assertEquals("F", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAction.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAction.getEndTimeDb().longValue() < endTime);
      assertNull(pitAction.getContextId());
    }
    
    // check action set
    {
      PITAttributeAssignActionSet pitActionSet = GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().findById(action1Set.getId());
      assertNotNull(pitActionSet);
      assertEquals(action1Set.getDepth(), pitActionSet.getDepth());
      assertEquals(action1Set.getIfHasAttrAssignActionId(), pitActionSet.getIfHasAttrAssignActionId());
      assertEquals(action1Set.getThenHasAttrAssignActionId(), pitActionSet.getThenHasAttrAssignActionId());
      assertEquals(action1Set.getParentAttrAssignActionSetId(), pitActionSet.getParentAttrAssignActionSetId());
      assertEquals("F", pitActionSet.getActiveDb());
      assertTrue(pitActionSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitActionSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitActionSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitActionSet.getContextId());
    }
    
    // check attribute assign value
    {
      PITAttributeAssignValue pitValue = GrouperDAOFactory.getFactory().getPITAttributeAssignValue().findById(value.getId());
      assertNotNull(pitValue);
      assertEquals(value.getAttributeAssignId(), pitValue.getAttributeAssignId());
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
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName1.getId());
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getStemId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getAttributeDefId());
      assertEquals(attributeDefName1.getName(), pitAttributeDefName.getName());
      assertEquals("F", pitAttributeDefName.getActiveDb());
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDefName.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefName.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefName.getContextId());
    }
    
    // check attribute def name set
    {
      PITAttributeDefNameSet pitAttributeDefNameSet = GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().findById(attributeDefName1Set.getId());
      assertNotNull(pitAttributeDefNameSet);
      assertEquals(attributeDefName1Set.getDepth(), pitAttributeDefNameSet.getDepth());
      assertEquals(attributeDefName1Set.getIfHasAttributeDefNameId(), pitAttributeDefNameSet.getIfHasAttributeDefNameId());
      assertEquals(attributeDefName1Set.getThenHasAttributeDefNameId(), pitAttributeDefNameSet.getThenHasAttributeDefNameId());
      assertEquals(attributeDefName1Set.getParentAttrDefNameSetId(), pitAttributeDefNameSet.getParentAttrDefNameSetId());
      assertEquals("F", pitAttributeDefNameSet.getActiveDb());
      assertTrue(pitAttributeDefNameSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitAttributeDefNameSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefNameSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefNameSet.getContextId());
    }
    
    // check field
    {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(testField.getUuid());
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
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(role.getId());
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getStemId());
      assertEquals("F", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroup.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroup.getContextId());
    }
    
    // check group set (depth=0)
    {
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet1.getId());
      assertNotNull(pitGroupSet);
      assertEquals(groupSet1.getOwnerId(), pitGroupSet.getOwnerId());
      assertEquals(groupSet1.getOwnerGroupId(), pitGroupSet.getOwnerGroupId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerStemId());
      assertEquals(groupSet1.getMemberId(), pitGroupSet.getMemberId());
      assertEquals(groupSet1.getMemberGroupId(), pitGroupSet.getMemberGroupId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet1.getFieldId(), pitGroupSet.getFieldId());
      assertEquals(groupSet1.getMemberFieldId(), pitGroupSet.getMemberFieldId());
      assertEquals(groupSet1.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet1.getParentId(), pitGroupSet.getParentId());
      assertEquals("F", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroupSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getContextId());
    }
    
    // check group set (depth=1)
    {
      PITGroupSet pitGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findById(groupSet2.getId());
      assertNotNull(pitGroupSet);
      assertEquals(groupSet2.getOwnerId(), pitGroupSet.getOwnerId());
      assertEquals(groupSet2.getOwnerStemId(), pitGroupSet.getOwnerStemId());
      assertNull(pitGroupSet.getOwnerAttrDefId());
      assertNull(pitGroupSet.getOwnerGroupId());
      assertEquals(groupSet2.getMemberId(), pitGroupSet.getMemberId());
      assertEquals(groupSet2.getMemberGroupId(), pitGroupSet.getMemberGroupId());
      assertNull(pitGroupSet.getMemberAttrDefId());
      assertNull(pitGroupSet.getMemberStemId());
      assertEquals(groupSet2.getFieldId(), pitGroupSet.getFieldId());
      assertEquals(groupSet2.getMemberFieldId(), pitGroupSet.getMemberFieldId());
      assertEquals(groupSet2.getDepth(), pitGroupSet.getDepth());
      assertEquals(groupSet2.getParentId(), pitGroupSet.getParentId());
      assertEquals("F", pitGroupSet.getActiveDb());
      assertTrue(pitGroupSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitGroupSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitGroupSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitGroupSet.getContextId());
    }
    
    // check member
    {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(newMember1.getUuid());
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
      PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(membership.getImmediateMembershipId());
      assertNotNull(pitMembership);
      assertEquals(membership.getOwnerId(), pitMembership.getOwnerId());
      assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerGroupId());
      assertNull(pitMembership.getOwnerStemId());
      assertNull(pitMembership.getOwnerAttrDefId());
      assertEquals(membership.getMemberUuid(), pitMembership.getMemberId());
      assertEquals(membership.getFieldId(), pitMembership.getFieldId());
      assertEquals("F", pitMembership.getActiveDb());
      assertTrue(pitMembership.getStartTimeDb().longValue() < startTime);
      assertTrue(pitMembership.getEndTimeDb().longValue() > startTime);
      assertTrue(pitMembership.getEndTimeDb().longValue() < endTime);
      assertNull(pitMembership.getContextId());
    }
    
    // check role set
    {
      PITRoleSet pitRoleSet = GrouperDAOFactory.getFactory().getPITRoleSet().findById(roleSet.getId());
      assertNotNull(pitRoleSet);
      assertEquals(roleSet.getDepth(), pitRoleSet.getDepth());
      assertEquals(roleSet.getIfHasRoleId(), pitRoleSet.getIfHasRoleId());
      assertEquals(roleSet.getThenHasRoleId(), pitRoleSet.getThenHasRoleId());
      assertEquals(roleSet.getParentRoleSetId(), pitRoleSet.getParentRoleSetId());
      assertEquals("F", pitRoleSet.getActiveDb());
      assertTrue(pitRoleSet.getStartTimeDb().longValue() < startTime);
      assertTrue(pitRoleSet.getEndTimeDb().longValue() > startTime);
      assertTrue(pitRoleSet.getEndTimeDb().longValue() < endTime);
      assertNull(pitRoleSet.getContextId());
    }
    
    // check stem
    {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(edu.getUuid());
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentStemId());
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
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findById(attributeDef1.getId());
      assertNotNull(pitAttributeDef);
      assertEquals(attributeDef1.getName(), pitAttributeDef.getName());
      assertEquals(attributeDef1.getStemId(), pitAttributeDef.getStemId());
      assertEquals(attributeDef1.getAttributeDefTypeDb(), pitAttributeDef.getAttributeDefTypeDb());
      assertEquals("T", pitAttributeDef.getActiveDb());
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDef.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDef.getEndTimeDb());
      assertEquals(attributeDef1.getContextId(), pitAttributeDef.getContextId());
    }
    
    // check action
    {
      PITAttributeAssignAction pitAction = GrouperDAOFactory.getFactory().getPITAttributeAssignAction().findById(action1.getId());
      assertNotNull(pitAction);
      assertEquals(action1.getName(), pitAction.getName());
      assertEquals(action1.getAttributeDefId(), pitAction.getAttributeDefId());
      assertEquals("T", pitAction.getActiveDb());
      assertTrue(pitAction.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAction.getStartTimeDb().longValue() < endTime);
      assertNull(pitAction.getEndTimeDb());
      assertEquals(action1.getContextId(), pitAction.getContextId());
    }
    
    // check attribute def name
    {
      PITAttributeDefName pitAttributeDefName = GrouperDAOFactory.getFactory().getPITAttributeDefName().findById(attributeDefName1.getId());
      assertNotNull(pitAttributeDefName);
      assertEquals(attributeDefName1.getStemId(), pitAttributeDefName.getStemId());
      assertEquals(attributeDefName1.getAttributeDefId(), pitAttributeDefName.getAttributeDefId());
      assertEquals(attributeDefName1.getName(), pitAttributeDefName.getName());
      assertEquals("T", pitAttributeDefName.getActiveDb());
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() > startTime);
      assertTrue(pitAttributeDefName.getStartTimeDb().longValue() < endTime);
      assertNull(pitAttributeDefName.getEndTimeDb());
      assertEquals(attributeDefName1.getContextId(), pitAttributeDefName.getContextId());
    }
    
    // check field
    {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(testField.getUuid());
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
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(role.getId());
      assertNotNull(pitGroup);
      assertEquals(role.getName(), pitGroup.getName());
      assertEquals(role.getStemId(), pitGroup.getStemId());
      assertEquals("T", pitGroup.getActiveDb());
      assertTrue(pitGroup.getStartTimeDb().longValue() > startTime);
      assertTrue(pitGroup.getStartTimeDb().longValue() < endTime);
      assertNull(pitGroup.getEndTimeDb());
      assertEquals(((Group)role).getContextId(), pitGroup.getContextId());
    }
    
    // check member
    {
      PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(newMember1.getUuid());
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
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findById(edu.getUuid());
      assertNotNull(pitStem);
      assertEquals(edu.getName(), pitStem.getName());
      assertEquals(edu.getParentUuid(), pitStem.getParentStemId());
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
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(membership.getImmediateMembershipId());
    assertNotNull(pitMembership);
    assertEquals(membership.getOwnerId(), pitMembership.getOwnerId());
    assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerGroupId());
    assertNull(pitMembership.getOwnerStemId());
    assertNull(pitMembership.getOwnerAttrDefId());
    assertEquals(membership.getMemberUuid(), pitMembership.getMemberId());
    assertEquals(membership.getFieldId(), pitMembership.getFieldId());
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
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findById(membership.getImmediateMembershipId());
    assertNotNull(pitMembership);
    assertEquals(membership.getOwnerId(), pitMembership.getOwnerId());
    assertEquals(membership.getOwnerGroupId(), pitMembership.getOwnerGroupId());
    assertNull(pitMembership.getOwnerStemId());
    assertNull(pitMembership.getOwnerAttrDefId());
    assertEquals(membership.getMemberUuid(), pitMembership.getMemberId());
    assertEquals(membership.getFieldId(), pitMembership.getFieldId());
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
    PITAttributeAssign pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(assign1.getId());
    assertNotNull(pitAssign);
    assertEquals(assign1.getAttributeDefNameId(), pitAssign.getAttributeDefNameId());
    assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getAttributeAssignActionId());
    assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
    assertNull(pitAssign.getOwnerAttributeAssignId());
    assertNull(pitAssign.getOwnerAttributeDefId());
    assertNull(pitAssign.getOwnerMemberId());
    assertNull(pitAssign.getOwnerMembershipId());
    assertNull(pitAssign.getOwnerStemId());
    assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerGroupId());
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
    pitAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(assign1.getId());
    assertNotNull(pitAssign);
    assertEquals(assign1.getAttributeDefNameId(), pitAssign.getAttributeDefNameId());
    assertEquals(assign1.getAttributeAssignActionId(), pitAssign.getAttributeAssignActionId());
    assertEquals(assign1.getAttributeAssignTypeDb(), pitAssign.getAttributeAssignTypeDb());
    assertNull(pitAssign.getOwnerAttributeAssignId());
    assertNull(pitAssign.getOwnerAttributeDefId());
    assertNull(pitAssign.getOwnerMemberId());
    assertNull(pitAssign.getOwnerMembershipId());
    assertNull(pitAssign.getOwnerStemId());
    assertEquals(assign1.getOwnerGroupId(), pitAssign.getOwnerGroupId());
    assertEquals("T", pitAssign.getActiveDb());
    assertTrue(pitAssign.getStartTimeDb().longValue() > startTime);
    assertTrue(pitAssign.getStartTimeDb().longValue() < endTime);
    assertNull(pitAssign.getEndTimeDb());
    assertEquals(assign1.getContextId(), pitAssign.getContextId());
  }
}
