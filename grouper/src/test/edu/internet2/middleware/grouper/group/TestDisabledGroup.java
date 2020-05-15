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

package edu.internet2.middleware.grouper.group;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SyncPITTables;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.textui.TestRunner;

/**
 * @author shilen
 */
public class TestDisabledGroup extends GrouperTest {

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** */
  private AttributeDef testAttributeDef;
  
  /** */
  private AttributeDefName testAttributeDefName;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestDisabledGroup("testMembershipDisabledDateWithAttributeAssignments"));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    
    testAttributeDef = edu.addChildAttributeDef("testAttributeDef", AttributeDefType.attr);
    testAttributeDef.setAssignToGroup(true);
    testAttributeDef.setAssignToImmMembership(true);
    testAttributeDef.setAssignToGroupAssn(true);
    testAttributeDef.setAssignToImmMembershipAssn(true);
    testAttributeDef.setAssignToEffMembership(true);
    testAttributeDef.setAssignToEffMembershipAssn(true);
    testAttributeDef.setAssignToMember(true);
    testAttributeDef.setAssignToMemberAssn(true);
    testAttributeDef.setAssignToStem(true);
    testAttributeDef.setAssignToStemAssn(true);
    testAttributeDef.setValueType(AttributeDefValueType.string);
    testAttributeDef.store();
    testAttributeDefName = edu.addChildAttributeDefName(testAttributeDef, "testAttributeDefName", "testAttributeDefName");
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    grouperSession = GrouperSession.startRootSession();
  }
  
  /**
   * @param name
   */
  public TestDisabledGroup(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testAdminPrivilege() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group4 = edu.addChildGroup("test4", "test4");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    group2.grantPriv(group1.toSubject(), AccessPrivilege.ADMIN);
    group2.grantPriv(group2.toSubject(), AccessPrivilege.ADMIN);
    group2.grantPriv(group4.toSubject(), AccessPrivilege.ADMIN);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    group4.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group4.store();
    assertFalse(group4.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group1.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());    
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry set enabledDb='F' where ownerGroupId = '" + group2.getUuid() + "'").executeUpdate();
    for (Membership membership : GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAsList(group2.getUuid(), true)) {
      membership.setEnabled(false);
      membership.update();
    }
    
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group1.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertEquals(3, Membership.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group1.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    ChangeLogTempToEntity.convertRecords();
    new SyncPITTables().showResults(false).syncAllPITTables();
    grouperSession = GrouperSession.startRootSession();

    group2.setEnabledTime(null);
    group2.store();
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group1.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());  
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    
    group4.setEnabledTime(null);
    group4.store();
    assertTrue(group4.isEnabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());    

    group4.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group4.store();
    assertFalse(group4.isEnabled());
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false);
    ms.setEnabledDb("T");
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    assertEquals(1, Membership.internal_fixEnabledDisabled());
    
    group4.setEnabledTime(null);
    group4.store();
    assertTrue(group4.isEnabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());

    group2.setEnabledTime(null);
    group2.store();
    assertTrue(group2.isEnabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group4.toSubject(), true).getUuid(), FieldFinder.find("admins", true), "immediate", true, false).isEnabled());
  }

  /**
   * 
   */
  public void testEnabledDate() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group disable, last should be group delete, 11 total
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(11L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(11, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(null);
    group2.store();
    
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 11 total
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(11, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  
  /**
   * 
   */
  public void testDisabledDate() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setDisabledTime(new Timestamp(System.currentTimeMillis() - 100000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group disable, last should be group delete, 11 total
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(11L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(11, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setDisabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 11 total
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(11, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  
  /**
   * 
   */
  public void testDeleteDisabledGroup() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.delete();
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    // just one delete group change log
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertNotNull(changeLogEntry);
    
    assertEquals(1, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
    
  /**
   * 
   */
  public void testCompositeFactor() {
    Group top = edu.addChildGroup("top", "top");
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addCompositeMember(CompositeType.UNION, group2, group3);
    top.addMember(group1.toSubject());
    
    group2.addMember(SubjectTestHelper.SUBJ0);
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(top.hasMember(SubjectTestHelper.SUBJ0));
    
    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 100000L));
    group2.store();
    
    assertFalse(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(top.hasMember(SubjectTestHelper.SUBJ0));
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.addMember(SubjectTestHelper.SUBJ1);
    
    assertFalse(group2.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(group1.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(top.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() - 100000L));
    group2.store();
    
    assertTrue(group2.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(top.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(group2.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(group1.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(top.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
  }
  
  /**
   * 
   */
  public void testCompositeOwner() {
    Group topComposite = edu.addChildGroup("topComposite", "topComposite");
    Group topNothing = edu.addChildGroup("topNothing", "topNothing");
    
    Group composite = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    
    composite.addCompositeMember(CompositeType.UNION, group2, group3);
    
    topComposite.addCompositeMember(CompositeType.UNION, composite, topNothing);
    
    group2.addMember(SubjectTestHelper.SUBJ0);
    
    assertTrue(composite.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(topComposite.hasMember(SubjectTestHelper.SUBJ0));
    
    composite.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    composite.store();
    
    assertFalse(composite.hasMember(SubjectTestHelper.SUBJ0));
    assertFalse(topComposite.hasMember(SubjectTestHelper.SUBJ0));
    
    group2.addMember(SubjectTestHelper.SUBJ1);
    
    assertTrue(group2.hasMember(SubjectTestHelper.SUBJ1));
    
    assertFalse(composite.hasMember(SubjectTestHelper.SUBJ1));
    assertFalse(topComposite.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    composite.setEnabledTime(new Timestamp(System.currentTimeMillis() - 100000L));
    composite.store();
    
    assertTrue(composite.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(topComposite.hasMember(SubjectTestHelper.SUBJ0));
    assertTrue(composite.hasMember(SubjectTestHelper.SUBJ1));
    assertTrue(topComposite.hasMember(SubjectTestHelper.SUBJ1));
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
  }
  
  /**
   * 
   */
  public void testFixEnabledDisabledEnableViaEnableDate() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    AttributeAssign group2Assn = group2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign group2AssnAssn = group2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();

    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(AttributeAssignFinder.findById(group2Assn.getId(), true).isEnabled());
    assertFalse(AttributeAssignFinder.findById(group2AssnAssn.getId(), true).isEnabled());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(1, Group.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    group2 = GroupFinder.findByUuid(grouperSession, group2.getUuid(), true, new QueryOptions().secondLevelCache(false));
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(AttributeAssignFinder.findById(group2Assn.getId(), true).isEnabled());
    assertTrue(AttributeAssignFinder.findById(group2AssnAssn.getId(), true).isEnabled());

    // first change log entry should be group enable, second should be group add, 13 total (including 2 attribute assignments)
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(13, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  

  /**
   * 
   */
  public void testFixEnabledDisabledDisableViaDisableDate() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    AttributeAssign group2Assn = group2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign group2AssnAssn = group2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();

    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setDisabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    group2.store();
    assertTrue(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(AttributeAssignFinder.findById(group2Assn.getId(), true).isEnabled());
    assertTrue(AttributeAssignFinder.findById(group2AssnAssn.getId(), true).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(1, Group.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    group2 = GroupFinder.findByUuid(grouperSession, group2.getUuid(), true, new QueryOptions().secondLevelCache(false));
    assertFalse(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(AttributeAssignFinder.findById(group2Assn.getId(), true).isEnabled());
    assertFalse(AttributeAssignFinder.findById(group2AssnAssn.getId(), true).isEnabled());

    // first change log entry should be group disable, last should be group delete, 13 total (including 2 attribute assignments)
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(13L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(13, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  
  /**
   * 
   */
  public void testWithDisabledMembership() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    // disable group2 -> group3 membership
    Membership group2ToGroup3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    group2ToGroup3.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2ToGroup3.update();
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group disable, last should be group delete, 5 total
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(5L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(5, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(null);
    group2.store();
    
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 5 total
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(5, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  

  /**
   * 
   */
  public void testEnabledDateWithAttributeAssignments() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);

    Membership g3ToSubj0 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g1ToG2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g2ToG3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    
    // set up attributes - assignment on group
    AttributeAssign group1Assn = group1.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group1AssnAssn = group1Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group2Assn = group2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign group2AssnAssn = group2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    
    // set up attributes - assignment on immediate membership
    AttributeAssign immG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG3ToSubj0AssnAssn = immG3ToSubj0Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG1ToG2Assn = g1ToG2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG1ToG2AssnAssn = immG1ToG2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3Assn = g2ToG3.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3AssnAssn = immG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled

    // set up attributes - assignment on effective membership
    AttributeAssign effG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign effG3ToSubj0AssnAssn = effG3ToSubj0Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign effG1ToG2Assn = g1ToG2.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG1ToG2AssnAssn = effG1ToG2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG2ToG3Assn = g2ToG3.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG2ToG3AssnAssn = effG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled

    // set up attributes - assignment on member
    AttributeAssign group1MemberAssn = group1.toMember().getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group1MemberAssnAssn = group1MemberAssn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group2MemberAssn = group2.toMember().getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign group2MemberAssnAssn = group2MemberAssn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssnAssn.getId(), true, false).isEnabled());
    
    // first change log entry should be group disable, last should be group delete, 12 deleteAttributeAssign, 4 deletePrivilege, 5 deleteMembership, 23 total
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(23L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(23, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(null);
    group2.store();
    
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssnAssn.getId(), true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 23 total
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(23, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  
  /**
   * 
   */
  public void testWithDisabledAttributeAssignments() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);
    
    Membership g3ToSubj0 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g1ToG2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g2ToG3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    
    // set up attributes - assignment on group
    AttributeAssign group1Assn = group1.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group1AssnAssn = group1Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group2Assn = group2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    
    // set up attributes - assignment on immediate membership
    AttributeAssign immG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG3ToSubj0AssnAssn = immG3ToSubj0Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG1ToG2Assn = g1ToG2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3Assn = g2ToG3.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3AssnAssn = immG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled

    // set up attributes - assignment on effective membership
    AttributeAssign effG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign effG3ToSubj0AssnAssn = effG3ToSubj0Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign effG1ToG2Assn = g1ToG2.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG1ToG2AssnAssn = effG1ToG2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG2ToG3Assn = g2ToG3.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign effG2ToG3AssnAssn = effG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled

    // set up attributes - assignment on member
    AttributeAssign group1MemberAssn = group1.toMember().getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group1MemberAssnAssn = group1MemberAssn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign group2MemberAssn = group2.toMember().getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign group2MemberAssnAssn = group2MemberAssn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    
    // disable group2Assn
    group2Assn = AttributeAssignFinder.findById(group2Assn.getId(), true);
    group2Assn.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2Assn.saveOrUpdate();
    
    // disable immG1ToG2Assn
    immG1ToG2Assn = AttributeAssignFinder.findById(immG1ToG2Assn.getId(), true);
    immG1ToG2Assn.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    immG1ToG2Assn.saveOrUpdate();
    
    // these two should be disabled on creation
    AttributeAssign group2AssnAssn = group2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG1ToG2AssnAssn = immG1ToG2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());

    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssnAssn.getId(), true, false).isEnabled());
    
    // first change log entry should be group disable, last should be group delete, 19 total (2 are disabled using timestamps on assignments and 2 more that are assignments on those0
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DISABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_DELETE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(19L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(19, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.setEnabledTime(null);
    group2.store();
    
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG2ToG3AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2MemberAssnAssn.getId(), true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 19 total
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ENABLE.getChangeLogType().getId())
      .uniqueResult(ChangeLogEntry.class);
    assertEquals(1L, changeLogEntry.getSequenceNumber().longValue());
    
    changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.GROUP_ADD.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
    assertEquals(2L, changeLogEntry.getSequenceNumber().longValue());
    
    assertEquals(19, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
  }
  
  /**
   * 
   */
  public void testAssignmentsOnDisabledObjects() {
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    group2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    group2.store();
    assertFalse(group2.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    group2.addMember(group3.toSubject());
   
    Membership g2ToG3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    assertFalse(g2ToG3.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    AttributeAssign immG2ToG3Assn = g2ToG3.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign immG2ToG3AssnAssn = immG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();

    assertFalse(immG2ToG3Assn.isEnabled());
    assertFalse(immG2ToG3AssnAssn.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
  }
  
  /**
   * 
   */
  public void testWithLocalEntity() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Entity testEntity = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("edu:entity").save();
    group1.addMember(testEntity.toSubject());
    
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), testEntity.toMember().getUuid(), Group.getDefaultList(), "immediate", true, false);
    assertTrue(ms.isEnabled());
    
    AttributeAssign immAssn = ms.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign immAssnAssn = immAssn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    
    assertTrue(immAssn.isEnabled());
    assertTrue(immAssnAssn.isEnabled());
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    // disable the local entity
    Group testEntityAsGroup = GrouperDAOFactory.getFactory().getGroup().findByUuid(testEntity.getId(), true);    
    testEntityAsGroup.setEnabledTime(new Timestamp(System.currentTimeMillis() + 1000000L));
    testEntityAsGroup.store();
    
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), testEntity.toMember().getUuid(), Group.getDefaultList(), "immediate", true, false);
    assertFalse(ms.isEnabled());
    assertFalse(ms.internal_isEnabledUsingTimestamps());
    
    immAssn = AttributeAssignFinder.findById(immAssn.getId(), true);
    assertFalse(immAssn.isEnabled());
    assertFalse(immAssn.internal_isEnabledUsingTimestamps());
    
    immAssnAssn = AttributeAssignFinder.findById(immAssnAssn.getId(), true);
    assertFalse(immAssnAssn.isEnabled());
    assertFalse(immAssnAssn.internal_isEnabledUsingTimestamps());
    
    // enable it
    testEntityAsGroup.setEnabledTime(new Timestamp(System.currentTimeMillis() - 1000000L));
    testEntityAsGroup.store();

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), testEntity.toMember().getUuid(), Group.getDefaultList(), "immediate", true, false);
    assertTrue(ms.isEnabled());
    assertTrue(ms.internal_isEnabledUsingTimestamps());
    
    immAssn = AttributeAssignFinder.findById(immAssn.getId(), true);
    assertTrue(immAssn.isEnabled());
    assertTrue(immAssn.internal_isEnabledUsingTimestamps());
    
    immAssnAssn = AttributeAssignFinder.findById(immAssnAssn.getId(), true);
    assertTrue(immAssnAssn.isEnabled());
    assertTrue(immAssnAssn.internal_isEnabledUsingTimestamps());
  }
  
  /**
   * 
   */
  public void testFixEnabledDisabledStatusOnAttributes() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);

    Membership g3ToSubj0 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g1ToG2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    
    // set up attributes - assignment on group
    AttributeAssign group1Assn = group1.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign group2Assn = group2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign group2AssnAssn = group2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    
    // set up attributes - assignment on immediate membership
    AttributeAssign immG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    
    // set up attributes - assignment on folder
    AttributeAssign eduAssn = edu.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    
    // set up attributes - assignment on member
    AttributeAssign group1MemberAssn = group1.toMember().getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign subj0MemberAssn = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false).getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    
    // set up attributes - assignment on effective membership
    AttributeAssign effG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
    AttributeAssign effG1ToG2Assn = g1ToG2.getAttributeValueDelegateEffMship().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();
  
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledTimeDb = :enabledTime where id = :id").setLong("enabledTime", System.currentTimeMillis()+1000000).setString("id", group1Assn.getId()).executeUpdate();
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1Assn.getId(), true, false).isEnabled());

    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set disabledTimeDb = :disabledTime where id = :id").setLong("disabledTime", System.currentTimeMillis()-1000000).setString("id", group2AssnAssn.getId()).executeUpdate();
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group2AssnAssn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledTimeDb = :enabledTime, enabledDb='F' where id = :id").setLong("enabledTime", System.currentTimeMillis()-1000000).setString("id", eduAssn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(eduAssn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(eduAssn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledDb = 'F' where id = :id").setString("id", immG3ToSubj0Assn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledDb = 'F' where id = :id").setString("id", subj0MemberAssn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(subj0MemberAssn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(subj0MemberAssn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledDb = 'F' where id = :id").setString("id", group1MemberAssn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(group1MemberAssn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledDb = 'F' where id = :id").setString("id", effG3ToSubj0Assn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG3ToSubj0Assn.getId(), true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("update AttributeAssign set enabledDb = 'F' where id = :id").setString("id", effG1ToG2Assn.getId()).executeUpdate();
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
    assertEquals(1, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(effG1ToG2Assn.getId(), true, false).isEnabled());
  }
  
  /**
   * 
   */
  public void testMembershipDisabledDateWithAttributeAssignments() {
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE);
    group3.addMember(SubjectTestHelper.SUBJ0);
    edu.grantPriv(group2.toSubject(), NamingPrivilege.STEM_ATTR_READ);

    Membership g3ToSubj0 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g1ToG2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    Membership g2ToG3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    
    // set up attributes - assignment on immediate membership
    AttributeAssign immG3ToSubj0Assn = g3ToSubj0.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG3ToSubj0AssnAssn = immG3ToSubj0Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would not be disabled
    AttributeAssign immG1ToG2Assn = g1ToG2.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG1ToG2AssnAssn = immG1ToG2Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3Assn = g2ToG3.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled
    AttributeAssign immG2ToG3AssnAssn = immG2ToG3Assn.getAttributeValueDelegate().assignValue(testAttributeDefName.getName(), "test").getAttributeAssignResult().getAttributeAssign();  // this would be disabled

    g3ToSubj0.setDisabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g3ToSubj0.update();
    g1ToG2.setDisabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g1ToG2.update();
    g2ToG3.setDisabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g2ToG3.update();
    assertTrue(g3ToSubj0.isEnabled());
    assertTrue(g1ToG2.isEnabled());
    assertTrue(g2ToG3.isEnabled());
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      // ignore
    }
        
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(3, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
 
    ChangeLogTempToEntity.convertRecords();
    assertEquals(9, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

    g1ToG2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    g2ToG3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    g3ToSubj0 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false);
    
    g3ToSubj0.setDisabledTime(null);
    g3ToSubj0.setEnabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g3ToSubj0.update();
    g1ToG2.setDisabledTime(null);
    g1ToG2.setEnabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g1ToG2.update();
    g2ToG3.setDisabledTime(null);
    g2ToG3.setEnabledTime(new Timestamp(System.currentTimeMillis() + 5000L));
    g2ToG3.update();
    assertFalse(g3ToSubj0.isEnabled());
    assertFalse(g1ToG2.isEnabled());
    assertFalse(g2ToG3.isEnabled());
    try {
      Thread.sleep(5000);
    } catch (Exception e) {
      // ignore
    }
        
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());
    assertEquals(3, Membership.internal_fixEnabledDisabled());
    assertEquals(0, AttributeAssign.internal_fixEnabledDisabled());

    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG3ToSubj0AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG1ToG2AssnAssn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3Assn.getId(), true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getAttributeAssign().findById(immG2ToG3AssnAssn.getId(), true, false).isEnabled());
 
    ChangeLogTempToEntity.convertRecords();
    assertEquals(9, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());

  }
}