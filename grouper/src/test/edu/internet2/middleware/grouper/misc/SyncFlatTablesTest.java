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

package edu.internet2.middleware.grouper.misc;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.flat.FlatAttributeDef;
import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.flat.FlatMembership;
import edu.internet2.middleware.grouper.flat.FlatStem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;

/**
 * @author shilen
 * $Id$
 */
public class SyncFlatTablesTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

  }
  
  /**
   * @param name
   */
  public SyncFlatTablesTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp() {
    super.setUp();
    
    // make sure flat tables are in sync before we start...
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(false).syncAllFlatTables();
  }

  /**
   * @throws Exception
   */
  public void testFlatMembershipsNoFalsePositives() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
      
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());

    // verify adding membership doesn't have false positives
    g3.addMember(g4.toSubject());
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // verify deleting membership
    g3.deleteMember(g4.toSubject());
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    g3.addMember(g4.toSubject());
    ChangeLogTempToEntity.convertRecords();

    // verify deleting privs
    s0.revokePriv(g3.toSubject(), NamingPrivilege.CREATE);
    g1.revokePriv(g3.toSubject(), AccessPrivilege.UPDATE);
    a0.getPrivilegeDelegate().revokePriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();

    // verify adding privs
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();

    // verify deleting and adding
    g3.deleteMember(g4.toSubject());
    g3.addMember(g4.toSubject());
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    
    // verify adding and deleting
    g3.deleteMember(g4.toSubject());
    ChangeLogTempToEntity.convertRecords();
    g3.addMember(g4.toSubject());
    g3.deleteMember(g4.toSubject());
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    g3.addMember(g4.toSubject());
    ChangeLogTempToEntity.convertRecords();
    
    // disable membership
    Membership g3g4Mship = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g3.getUuid(), g4.toMember().getUuid(), 
          Group.getDefaultList(), "immediate", true, true);
    
    g3g4Mship.setEnabled(false);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() + 100000));

    final Membership MEMBERSHIP1 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP1);

        return null;
      }
    });
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());

    // enable membership
    g3g4Mship.setEnabled(true);
    g3g4Mship.setEnabledTime(new Timestamp(new Date().getTime() - 100000));

    final Membership MEMBERSHIP2 = g3g4Mship;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP2);

        return null;
      }
    });
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    
    // disable priv
    Membership g1g3Priv = GrouperDAOFactory.getFactory().getMembership()
      .findByGroupOwnerAndMemberAndFieldAndType(g1.getUuid(), g3.toMember().getUuid(), 
          FieldFinder.find("updaters", true), "immediate", true, true);
    
    g1g3Priv.setEnabled(false);
    g1g3Priv.setEnabledTime(new Timestamp(new Date().getTime() + 100000));
  
    final Membership MEMBERSHIP3 = g1g3Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP3);
  
        return null;
      }
    });
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());

    // enable priv
    g1g3Priv.setEnabled(true);
    g1g3Priv.setEnabledTime(new Timestamp(new Date().getTime() - 100000));
  
    final Membership MEMBERSHIP4 = g1g3Priv;
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
  
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
  
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").update(MEMBERSHIP4);
  
        return null;
      }
    });
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testFlatMembershipsFullSync() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    Member subj2 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ2, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    g3.addMember(g4.toSubject());
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    int expectedCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    
    // delete all flat memberships and add back without notifications
    HibernateSession.byHqlStatic().createQuery("delete from FlatMembership").executeUpdate();
    
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(false).syncAllFlatTables();
    int newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp"));
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(false).syncAllFlatTables());

    // delete all flat memberships and add back with notifications
    HibernateSession.byHqlStatic().createQuery("delete from FlatMembership").executeUpdate();
    
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(true).syncAllFlatTables();
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(0, newCount);
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(true).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(true).sendNotifications(true).syncAllFlatTables());
    
    // delete all flat memberships and run sync without updates
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from FlatMembership").executeUpdate();
    
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables();
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(0, newCount);
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp"));

    // now lets have some bad flat memberships and run sync without updates
    g5.addMember(subj2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertTrue(newCount > 0);
    g5.deleteMember(subj2.getSubject());
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables();
    int newCount2 = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(newCount, newCount2);
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp"));
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatMembershipsWithUpdatesNoNotifications() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    g3.addMember(g4.toSubject());
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    int expectedCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    
    // delete some flat memberships
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()).delete();
    
    // should fix 4 flat memberships
    assertEquals(4, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(false).syncAllFlatTables());
    
    // should have no more to fix
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(false).syncAllFlatTables());

    // change log should be empty
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp"));

    // verify total flat memberships
    int newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()));
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatMembershipsWithUpdatesNoNotifications() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    
    ChangeLogTempToEntity.convertRecords();
    int expectedCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");

    g3.addMember(g4.toSubject());
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();
    int newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    int diffCount = newCount - expectedCount;
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete some memberships
    g3.deleteMember(g4.toSubject());
    a0.getPrivilegeDelegate().revokePriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    g1.revokePriv(g3.toSubject(), AccessPrivilege.UPDATE);
    s0.revokePriv(g3.toSubject(), NamingPrivilege.CREATE);

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // should fix some flat memberships
    assertEquals(diffCount, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(false).syncAllFlatTables());
    
    // should have no more to fix
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(false).syncAllFlatTables());

    // change log should be empty
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    assertEquals(0, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_temp"));

    // verify total flat memberships
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()));
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatMembershipsWithUpdatesNotifications() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    g3.addMember(g4.toSubject());
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    int expectedCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    
    // delete some flat memberships
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g5.toMember().getUuid(), Group.getDefaultList().getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()).delete();
    GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()).delete();
    
    // should fix 5 flat memberships
    assertEquals(5, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(true).syncAllFlatTables());
    
    // should have no more to fix
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(true).syncAllFlatTables());

    // verify total flat memberships
    int newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount - 5, newCount);
    
    ChangeLogTempToEntity.convertRecords();
    
    // verify total flat memberships
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g5.toMember().getUuid(), Group.getDefaultList().getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()));
    assertNotNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()));
    
    assertEquals(5, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));

    // now lets verify the 5 entries in the change log...
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g4.toMember().getSubjectId())
      .setString("groupId", g3.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(g4.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(g4.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g4.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_ADD.getChangeLogType().getId())
      .setString("subjectId", g5.toMember().getSubjectId())
      .setString("groupId", g3.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.fieldId));
    assertEquals(g5.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.memberId));
    assertEquals(g5.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.subjectId));
    assertEquals(g5.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.sourceId));
    assertEquals(g3.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupId));
    assertEquals(g3.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_ADD.groupName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", a0.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(a0.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(a0.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", g1.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_ADD.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", s0.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.sourceId));
    assertEquals(s0.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerId));
    assertEquals(s0.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_ADD.ownerName));
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatMembershipsWithUpdatesNotifications() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    Group g0 = top.addChildGroup("group0", "group0");
    Group g1 = top.addChildGroup("group1", "group1");
    Group g2 = top.addChildGroup("group2", "group2");
    Group g3 = top.addChildGroup("group3", "group3");
    Group g4 = top.addChildGroup("group4", "group4");
    Group g5 = top.addChildGroup("group5", "group5");
    Stem s0 = top.addChildStem("stem0", "stem0");
    Member subj1 = MemberFinder.findBySubject(session, SubjectTestHelper.SUBJ1, true);
    AttributeDef a0 = top.addChildAttributeDef("attributeDef0", AttributeDefType.perm);
        
    g0.addMember(g1.toSubject());
    g2.addMember(g3.toSubject());
    g4.addMember(g5.toSubject());
    g5.addMember(subj1.getSubject());
    g3.addMember(subj1.getSubject());
    
    ChangeLogTempToEntity.convertRecords();
    int expectedCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");

    g3.addMember(g4.toSubject());
    a0.getPrivilegeDelegate().grantPriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    g1.grantPriv(g3.toSubject(), AccessPrivilege.UPDATE);
    s0.grantPriv(g3.toSubject(), NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();
    int newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    int diffCount = newCount - expectedCount;
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    // delete some memberships
    g3.deleteMember(g4.toSubject());
    a0.getPrivilegeDelegate().revokePriv(g3.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, true);
    g1.revokePriv(g3.toSubject(), AccessPrivilege.UPDATE);
    s0.revokePriv(g3.toSubject(), NamingPrivilege.CREATE);

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    
    // should fix some flat memberships
    assertEquals(diffCount, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(true).syncAllFlatTables());
    
    // should have no more to fix
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).sendNotifications(true).syncAllFlatTables());

    // verify total flat memberships
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount + diffCount, newCount);
    
    ChangeLogTempToEntity.convertRecords();
    
    // verify total flat memberships
    newCount = HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_flat_memberships");
    assertEquals(expectedCount, newCount);
    
    assertEquals(16, (int) HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g3.getUuid(), g4.toMember().getUuid(), Group.getDefaultList().getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        a0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("attrUpdaters", true).getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        g1.getUuid(), g3.toMember().getUuid(), FieldFinder.find("updaters", true).getUuid()));
    assertNull(GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerAndMemberAndField(
        s0.getUuid(), g3.toMember().getUuid(), FieldFinder.find("creators", true).getUuid()));
    
    // now lets verify some entries in the change log...
    ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string06 = :groupId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBERSHIP_DELETE.getChangeLogType().getId())
      .setString("subjectId", g4.toMember().getSubjectId())
      .setString("groupId", g3.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("members", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldName));
    assertEquals(Group.getDefaultList().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.fieldId));
    assertEquals(g4.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.memberId));
    assertEquals(g4.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.subjectId));
    assertEquals(g4.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.sourceId));
    assertEquals(g3.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupId));
    assertEquals(g3.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBERSHIP_DELETE.groupName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", a0.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("attrUpdate", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("attributeDef", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("attrUpdaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(a0.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(a0.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));

    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", g1.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("update", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("access", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("group", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("updaters", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(g1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(g1.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
    
    changeLogEntry = HibernateSession.byHqlStatic()
      .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string03 = :subjectId and string07 = :ownerId")
      .setString("theChangeLogType", ChangeLogTypeBuiltin.PRIVILEGE_DELETE.getChangeLogType().getId())
      .setString("subjectId", g3.toMember().getSubjectId())
      .setString("ownerId", s0.getUuid())
      .uniqueResult(ChangeLogEntry.class);
  
    assertEquals("create", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeName));
    assertEquals("naming", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.privilegeType));
    assertEquals("stem", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerType));
    assertEquals(FieldFinder.find("creators", true).getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.fieldId));
    assertEquals(g3.toMember().getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.memberId));
    assertEquals(g3.toMember().getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.subjectId));
    assertEquals(g3.toMember().getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.sourceId));
    assertEquals(s0.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerId));
    assertEquals(s0.getName(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.PRIVILEGE_DELETE.ownerName));
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatGroupsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat group, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(group2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatGroup flatGroup2 = GrouperDAOFactory.getFactory().getFlatGroup().findById(group2.getUuid());
    flatGroup2.delete();
    
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatGroupsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat group, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(group2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatGroup flatGroup2 = GrouperDAOFactory.getFactory().getFlatGroup().findById(group2.getUuid());
    flatGroup2.delete();

    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatStemsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    root.addChildStem("top", "top");
    Stem top2 = root.addChildStem("top2", "top2");
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat stem, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(top2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatStem flatStem2 = GrouperDAOFactory.getFactory().getFlatStem().findById(top2.getUuid());
    flatStem2.delete();
    
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatStemsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    root.addChildStem("top", "top");
    Stem top2 = root.addChildStem("top2", "top2");
    
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat stem, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(top2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatStem flatStem2 = GrouperDAOFactory.getFactory().getFlatStem().findById(top2.getUuid());
    flatStem2.delete();
    
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatAttributeDefsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildAttributeDef("attrdef1", AttributeDefType.perm);
    AttributeDef resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);

    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat attr def, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(resourcesDef2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatAttributeDef flatAttributeDef2 = GrouperDAOFactory.getFactory().getFlatAttributeDef().findById(resourcesDef2.getUuid());
    flatAttributeDef2.delete();
    
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testAddMissingFlatAttributeDefsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildAttributeDef("attrdef1", AttributeDefType.perm);
    AttributeDef resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);

    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // before we delete the flat attr def, we have to delete the flat memberships due to constraints
    Set<FlatMembership> mships = GrouperDAOFactory.getFactory().getFlatMembership().findByOwnerId(resourcesDef2.getUuid());
    int flatMshipCount = mships.size();
    Iterator<FlatMembership> iter = mships.iterator();
    while (iter.hasNext()) {
      FlatMembership mship = iter.next();
      mship.delete();
    }
    
    FlatAttributeDef flatAttributeDef2 = GrouperDAOFactory.getFactory().getFlatAttributeDef().findById(resourcesDef2.getUuid());
    flatAttributeDef2.delete();
    
    assertEquals(flatMshipCount + 1, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatGroupsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    Group group3 = top.addChildGroup("group3", "group3");
    group2.delete();
    group3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    group2 = top.addChildGroup("group2", "group2");
    group3 = top.addChildGroup("group3", "group3");
    ChangeLogTempToEntity.convertRecords();
    group2.delete();
    group3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatGroupsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("group1", "group1");
    Group group2 = top.addChildGroup("group2", "group2");
    Group group3 = top.addChildGroup("group3", "group3");
    group2.delete();
    group3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    group2 = top.addChildGroup("group2", "group2");
    group3 = top.addChildGroup("group3", "group3");
    ChangeLogTempToEntity.convertRecords();
    group2.delete();
    group3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatStemsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("stem1", "stem1");
    Stem stem2 = top.addChildStem("stem2", "stem2");
    Stem stem3 = top.addChildStem("stem3", "stem3");
    stem2.delete();
    stem3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    stem2 = top.addChildStem("stem2", "stem2");
    stem3 = top.addChildStem("stem3", "stem3");
    ChangeLogTempToEntity.convertRecords();
    stem2.delete();
    stem3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatStemsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");
    
    top.addChildGroup("stem1", "stem1");
    Stem stem2 = top.addChildStem("stem2", "stem2");
    Stem stem3 = top.addChildStem("stem3", "stem3");
    stem2.delete();
    stem3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    stem2 = top.addChildStem("stem2", "stem2");
    stem3 = top.addChildStem("stem3", "stem3");
    ChangeLogTempToEntity.convertRecords();
    stem2.delete();
    stem3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatAttributeDefsWithNoUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");

    top.addChildAttributeDef("attrdef1", AttributeDefType.perm);
    AttributeDef resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    AttributeDef resourcesDef3 = top.addChildAttributeDef("attrdef3", AttributeDefType.perm);
    
    resourcesDef2.delete();
    resourcesDef3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    resourcesDef3 = top.addChildAttributeDef("attrdef3", AttributeDefType.perm);
    ChangeLogTempToEntity.convertRecords();
    resourcesDef2.delete();
    resourcesDef3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    // if we run this again, we should get the same result since updates were not saved
    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
  }
  
  /**
   * @throws Exception
   */
  public void testRemoveBadFlatAttributeDefsWithUpdates() throws Exception {
    GrouperSession session = GrouperSession.startRootSession();
    
    Stem root = StemFinder.findRootStem(session);
    Stem top = root.addChildStem("top", "top");

    top.addChildAttributeDef("attrdef1", AttributeDefType.perm);
    AttributeDef resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    AttributeDef resourcesDef3 = top.addChildAttributeDef("attrdef3", AttributeDefType.perm);
    
    resourcesDef2.delete();
    resourcesDef3.delete();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    ChangeLogTempToEntity.convertRecords();
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).saveUpdates(false).syncAllFlatTables());
    
    resourcesDef2 = top.addChildAttributeDef("attrdef2", AttributeDefType.perm);
    resourcesDef3 = top.addChildAttributeDef("attrdef3", AttributeDefType.perm);
    ChangeLogTempToEntity.convertRecords();
    resourcesDef2.delete();
    resourcesDef3.delete();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();

    // fix bad flat memberships so they don't get in the way
    new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).removeBadFlatMemberships();

    assertEquals(2, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
    
    // if we run this again, there shouldn't be any updates
    assertEquals(0, new edu.internet2.middleware.grouper.misc.SyncFlatTables().showResults(false).syncAllFlatTables());
  }
}

