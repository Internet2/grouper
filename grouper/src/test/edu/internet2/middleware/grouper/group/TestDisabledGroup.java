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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
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
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestDisabledGroup("testFixEnabledDisabledDisableViaDisableDate"));
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
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    group2.delete();
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    
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
    
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertFalse(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(1, Group.internal_fixEnabledDisabled());

    group2 = GroupFinder.findByUuid(grouperSession, group2.getUuid(), true, new QueryOptions().secondLevelCache(false));
    assertTrue(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    // first change log entry should be group enable, second should be group add, 11 total
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
    
    assertEquals(11, HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry").intValue());
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
    
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group1.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(edu.getUuid(), MemberFinder.findBySubject(grouperSession, group2.toSubject(), true).getUuid(), FieldFinder.find("stemAttrRead", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, group3.toSubject(), true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group2.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true).getUuid(), FieldFinder.find("updaters", true), "immediate", true, false).isEnabled());
    assertTrue(GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(group3.getUuid(), MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true).getUuid(), Group.getDefaultList(), "immediate", true, false).isEnabled());
    
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    assertEquals(1, Group.internal_fixEnabledDisabled());

    group2 = GroupFinder.findByUuid(grouperSession, group2.getUuid(), true, new QueryOptions().secondLevelCache(false));
    assertFalse(group2.isEnabled());

    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    grouperSession = GrouperSession.startRootSession();
    assertEquals(0, Group.internal_fixEnabledDisabled());
    assertEquals(0, Membership.internal_fixEnabledDisabled());
    
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
  }
}