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
package edu.internet2.middleware.grouper.member;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.usdu.UsduJob;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SyncPITTables;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.BaseSourceAdapter;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;

/**
 * @author shilen
 * $Id$
 */
public class TestMemberAttributes extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMemberAttributes("testMemberAttributesDuplicateIdentifierChangeLog"));
    //TestRunner.run(TestMemberAttributes.class);
  }
  
  /** top level stems */
  private Stem edu, edu2;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public TestMemberAttributes(String name) {
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
    edu2   = StemHelper.addChildStem(root, "edu2", "education2");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  public void testMemberAttributesDuplicateIdentifierChangeLog() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute1", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "description");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject1", "person", "testSubjectName1", "testName1", "testLogin1", "testDescription1", "testEmail1@sdf.sdf");
    Subject subj1 = SubjectFinder.findById("testSubject1", true);
    edu.grantPriv(subj1, NamingPrivilege.CREATE);
    Member member1 = GrouperDAOFactory.getFactory().getMember().findBySubject(subj1, true);

    deleteSubject(subj1);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    // now add a new subject that's a duplicate
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName2", "testName2", "testLogin2", "testDescription1", "testEmail2@sdf.sdf");
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(3, (int)HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry_v where action_name='updateMember'"));
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier0'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail1@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier0", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testLogin1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier1'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail1@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testName1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier2'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail1@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testDescription1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals(null, changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
  }
  
  public void testMemberAttributesUpdateChangeLog() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute1", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "description");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);

    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryTemp").executeUpdate();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();
    
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject1", "person", "testSubjectName1", "testName1", "testLogin1", "testDescription1", "testEmail1@sdf.sdf");
    Subject subj1 = SubjectFinder.findById("testSubject1", true);
    edu.grantPriv(subj1, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();
    HibernateSession.byHqlStatic().createQuery("delete from ChangeLogEntryEntity").executeUpdate();

    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testLogin2', searchvalue='testlogin2' where subjectid='testSubject1' and name='loginid'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testName2', searchvalue='testname2' where subjectid='testSubject1' and name='name'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testDescription2', searchvalue='testdescription2' where subjectid='testSubject1' and name='description'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testEmail2@sdf.sdf', searchvalue='testemail2@sdf.sdf' where subjectid='testSubject1' and name='email'", null, null);

    SubjectFinder.flushCache();
    subj1 = SubjectFinder.findById("testSubject1", true);
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    GrouperCacheUtils.clearAllCaches();
    
    Member member1 = GrouperDAOFactory.getFactory().getMember().findBySubject(subj1, true);
    assertEquals("testLogin2", member1.getSubjectIdentifier0());
    assertEquals("testName2", member1.getSubjectIdentifier1());
    assertEquals("testDescription2", member1.getSubjectIdentifier2());
    assertEquals("testEmail2@sdf.sdf", member1.getEmail0());
    
    ChangeLogTempToEntity.convertRecords();

    assertEquals(4, (int)HibernateSession.bySqlStatic().select(int.class, "select count(1) from grouper_change_log_entry"));
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='email0'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals("testLogin2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals("testName2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals("testDescription2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail2@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("email0", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testEmail1@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals("testEmail2@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier0'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals("testLogin2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals("testName2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals("testDescription2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail2@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier0", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testLogin1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals("testLogin2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier1'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals("testLogin2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals("testName2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals("testDescription2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail2@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testName1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals("testName2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
    
    {
      ChangeLogEntry changeLogEntry = HibernateSession.byHqlStatic()
        .createQuery("from ChangeLogEntryEntity where changeLogTypeId = :theChangeLogType and string06='subjectIdentifier2'")
        .setString("theChangeLogType", ChangeLogTypeBuiltin.MEMBER_UPDATE.getChangeLogType().getId())
        .uniqueResult(ChangeLogEntry.class);
          
      assertEquals(member1.getUuid(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.id));
      assertEquals(member1.getSubjectId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectId));
      assertEquals(member1.getSubjectSourceId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectSourceId));
      assertEquals(member1.getSubjectTypeId(), changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectTypeId));
      assertEquals("testLogin2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier0));
      assertEquals("testName2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier1));
      assertEquals("testDescription2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.subjectIdentifier2));
      assertEquals("testEmail2@sdf.sdf", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.email0));
      assertEquals("subjectIdentifier2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyChanged));
      assertEquals("testDescription1", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyOldValue));
      assertEquals("testDescription2", changeLogEntry.retrieveValueForLabel(ChangeLogLabels.MEMBER_UPDATE.propertyNewValue));
    }
  }
  
  public void testSubjectIdentifierDuplicatesUsdu() {
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // testSubject1/2 will be duplicates with both unsolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject1", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier1", "testSubjectDescription", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier1", "testSubjectDescription", null);
    Subject subj1 = SubjectFinder.findById("testSubject1", true);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj1, NamingPrivilege.CREATE);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    deleteSubject(subj1);
    deleteSubject(subj2);
    
    // testSubject3/4 will be duplicates with one unsolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier3", "testSubjectDescription", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier3", "testSubjectDescription", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    deleteSubject(subj4);
    
    // testSubject5/6 will be duplicates with both resolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject5", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier5", "testSubjectDescription", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject6", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier5", "testSubjectDescription", null);
    Subject subj5 = SubjectFinder.findById("testSubject5", true);
    Subject subj6 = SubjectFinder.findById("testSubject6", true);
    edu.grantPriv(subj5, NamingPrivilege.CREATE);
    edu.grantPriv(subj6, NamingPrivilege.CREATE);
    
    // testSubject7/8/9 will be duplicates with two unresolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject7", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier7", "testSubjectDescription", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject8", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier7", "testSubjectDescription", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject9", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier7", "testSubjectDescription", null);
    Subject subj7 = SubjectFinder.findById("testSubject7", true);
    Subject subj8 = SubjectFinder.findById("testSubject8", true);
    Subject subj9 = SubjectFinder.findById("testSubject9", true);
    edu.grantPriv(subj7, NamingPrivilege.CREATE);
    edu.grantPriv(subj8, NamingPrivilege.CREATE);
    edu.grantPriv(subj9, NamingPrivilege.CREATE);
    deleteSubject(subj8);
    deleteSubject(subj9);

    ChangeLogTempToEntity.convertRecords();
    Hib3MemberDAO.membersCacheClear();
    SubjectFinder.flushCache();

    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject1", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject5", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject6", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject7", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject8", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject9", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals(1, UsduJob.checkDuplicateSubjectIdentifiers(null));
    
    ChangeLogTempToEntity.convertRecords();
    Hib3MemberDAO.membersCacheClear();
    
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject1", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject5", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject6", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject7", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject8", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject9", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  public void testMultipleSubjectIdentifiersDuplicatesUsdu() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute1", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "description");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // testSubject1/2 will be duplicates with both unsolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject1", "person", "testSubjectName1", "testAnotherIdentifier1", "testSubjectIdentifier1", "testSubjectDescription1", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName2", "testSubjectName2", "testSubjectIdentifier2", "testAnotherIdentifier1", null);
    Subject subj1 = SubjectFinder.findById("testSubject1", true);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj1, NamingPrivilege.CREATE);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    deleteSubject(subj1);
    deleteSubject(subj2);
    
    // testSubject3/4 will be duplicates with one unsolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName3", "testAnotherIdentifier3", "testSubjectIdentifier3", "testSubjectDescription3", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName4", "testSubjectName4", "testSubjectIdentifier4", "testAnotherIdentifier3", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    deleteSubject(subj4);
    
    // testSubject5/6 will be duplicates with both resolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject5", "person", "testSubjectName5", "testAnotherIdentifier5", "testSubjectIdentifier5", "testSubjectDescription5", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject6", "person", "testSubjectName6", "testSubjectName6", "testSubjectIdentifier6", "testAnotherIdentifier5", null);
    Subject subj5 = SubjectFinder.findById("testSubject5", true);
    Subject subj6 = SubjectFinder.findById("testSubject6", true);
    edu.grantPriv(subj5, NamingPrivilege.CREATE);
    edu.grantPriv(subj6, NamingPrivilege.CREATE);
    
    // testSubject7/8/9 will be duplicates with two unresolvable
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject7", "person", "testSubjectName7", "testAnotherIdentifier7", "testSubjectIdentifier7", "testSubjectDescription7", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject8", "person", "testSubjectName8", "testAnotherIdentifier7", "testSubjectIdentifier8", "testSubjectDescription8", null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject9", "person", "testSubjectName9", "testSubjectName9", "testSubjectIdentifier9", "testAnotherIdentifier7", null);
    Subject subj7 = SubjectFinder.findById("testSubject7", true);
    Subject subj8 = SubjectFinder.findById("testSubject8", true);
    Subject subj9 = SubjectFinder.findById("testSubject9", true);
    edu.grantPriv(subj7, NamingPrivilege.CREATE);
    edu.grantPriv(subj8, NamingPrivilege.CREATE);
    edu.grantPriv(subj9, NamingPrivilege.CREATE);
    deleteSubject(subj8);
    deleteSubject(subj9);

    ChangeLogTempToEntity.convertRecords();
    Hib3MemberDAO.membersCacheClear();
    SubjectFinder.flushCache();

    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("testAnotherIdentifier1", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier1", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject1", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("testSubjectName4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier1());
    assertEquals("testAnotherIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier6", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier0());
    assertEquals("testSubjectName6", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier1());
    assertEquals("testAnotherIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject5", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier6", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject6", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier8", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription8", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier9", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier0());
    assertEquals("testSubjectName9", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier1());
    assertEquals("testAnotherIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject7", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier8", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject8", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier9", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject9", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals(1, UsduJob.checkDuplicateSubjectIdentifiers(null));
    
    ChangeLogTempToEntity.convertRecords();
    Hib3MemberDAO.membersCacheClear();
    
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject1", true).getSubjectIdentifier2());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject1", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject5", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier6", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier0());
    assertEquals("testSubjectName6", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier1());
    assertEquals("testAnotherIdentifier5", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject6", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier5", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject5", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier6", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject6", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier0());
    assertEquals("testAnotherIdentifier7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription7", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject7", true).getSubjectIdentifier2());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject8", true).getSubjectIdentifier2());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject9", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier7", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject7", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject8", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject9", "jdbc", "person").iterator().next().getSubjectIdentifier0());
 
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  /**
   * 
   */
  public void testSubjectIdentifierDuplicatesNewSubject() {
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // add a subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj = SubjectFinder.findById("testSubject", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());

    // now delete it
    deleteSubject(subj);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());

    ChangeLogTempToEntity.convertRecords();
    
    // now add a new subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - test when duplicate subject isn't deleted but has another identifier now
    SubjectFinder.flushCache();
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifierSomethingElse', searchvalue='testsubjectidentifiersomethingelse' where subjectid='testSubject2' and name='loginid'", null, null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    GrouperCacheUtils.clearAllCaches();
    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - this time the duplicate is real
    SubjectFinder.flushCache();
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  /**
   * 
   */
  public void testMultipleSubjectIdentifiersDuplicatesNewSubject() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute1", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "description");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // add a subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject", "person", "testSubjectName", "duplicateIdentifier", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj = SubjectFinder.findById("testSubject", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());

    // now delete it
    deleteSubject(subj);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());

    ChangeLogTempToEntity.convertRecords();
    
    // now add a new subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName2", "testSubjectName2", "testSubjectIdentifier2", "duplicateIdentifier", null);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - test when duplicate subject isn't deleted but has another identifier now
    SubjectFinder.flushCache();
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifierSomethingElse', searchvalue='testsubjectidentifiersomethingelse' where subjectid='testSubject2' and name='description'", null, null);
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName3", "testSubjectName3", "testSubjectIdentifier3", "duplicateIdentifier", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    GrouperCacheUtils.clearAllCaches();
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectName3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - this time the duplicate is real
    SubjectFinder.flushCache();
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName4", "duplicateIdentifier", "testSubjectIdentifier4", "testSubjectDescription4", null);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectName3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());
  
    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  /**
   * 
   */
  public void testSubjectIdentifierDuplicatesUpdateSubject() {
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // add a subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj = SubjectFinder.findById("testSubject", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());

    // now delete it
    deleteSubject(subj);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());

    ChangeLogTempToEntity.convertRecords();
    
    // now add a new subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier2", "testSubjectDescription", null);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifier', searchvalue='testsubjectidentifier' where subjectid='testSubject2' and name='loginid'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject2", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - test when the duplicate subject isn't deleted but instead has another identifier now
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier3", "testSubjectDescription", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifier', searchvalue='testsubjectidentifier' where subjectid='testSubject3' and name='loginid'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifierSomethingElse', searchvalue='testsubjectidentifiersomethingelse' where subjectid='testSubject2' and name='loginid'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject3", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    

    // now add another new subject - this time the duplicate is real
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName", "testSubjectName", "testSubjectIdentifier4", "testSubjectDescription", null);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifier', searchvalue='testsubjectidentifier' where subjectid='testSubject4' and name='loginid'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject4", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  /**
   * 
   */
  public void testMultipleSubjectIdentifiersDuplicatesUpdateSubject() {
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectIdentifierAttribute1", "lfname");
    source.addInitParam("subjectIdentifierAttribute2", "description");
    ExpirableCache.clearAll();
    source.setSubjectIdentifierAttributesAll(null);
    
    new SyncPITTables().showResults(false).syncAllPITTables();
    GrouperSession.startRootSession();
    
    // add a subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject", "person", "testSubjectName", "duplicateIdentifier", "testSubjectIdentifier", "testSubjectDescription", null);
    Subject subj = SubjectFinder.findById("testSubject", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());

    // now delete it
    deleteSubject(subj);
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());

    ChangeLogTempToEntity.convertRecords();
    
    // now add a new subject
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject2", "person", "testSubjectName2", "testSubjectName2", "testSubjectIdentifier2", "testSubjectDescription2", null);
    Subject subj2 = SubjectFinder.findById("testSubject2", true);
    edu.grantPriv(subj2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='duplicateIdentifier', searchvalue='duplicateidentifier' where subjectid='testSubject2' and name='description'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject2", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier0());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier1());
    assertNull(GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertNull(GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    
    // now add another new subject - test when the duplicate subject isn't deleted but instead has another identifier now
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject3", "person", "testSubjectName3", "testSubjectName3", "testSubjectIdentifier3", "testSubjectDescription3", null);
    Subject subj3 = SubjectFinder.findById("testSubject3", true);
    edu.grantPriv(subj3, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("testSubjectName3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='duplicateIdentifier', searchvalue='duplicateidentifier' where subjectid='testSubject3' and name='name'", null, null);
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='testSubjectIdentifierSomethingElse', searchvalue='testsubjectidentifiersomethingelse' where subjectid='testSubject2' and name='description'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject3", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier0());
    assertEquals("testSubjectName2", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier1());
    assertEquals("testSubjectIdentifierSomethingElse", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject2", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier2", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject2", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
    

    // now add another new subject - this time the duplicate is real
    RegistrySubject.add(GrouperSession.staticGrouperSession(), "testSubject4", "person", "testSubjectName4", "testSubjectName4", "testSubjectIdentifier4", "testSubjectDescription4", null);
    Subject subj4 = SubjectFinder.findById("testSubject4", true);
    edu.grantPriv(subj4, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("testSubjectName4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier2());
  
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value='duplicateIdentifier', searchvalue='duplicateidentifier' where subjectid='testSubject4' and name='description'", null, null);
    SubjectFinder.flushCache();
    SubjectFinder.findById("testSubject4", true);
    
    // wait for thread that updates data to finish
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // ignore
    }
    
    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier0());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier1());
    assertEquals("testSubjectDescription3", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject3", true).getSubjectIdentifier2());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier0());
    assertEquals("testSubjectName4", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier1());
    assertEquals("duplicateIdentifier", GrouperDAOFactory.getFactory().getMember().findBySubject("testSubject4", true).getSubjectIdentifier2());
  
    ChangeLogTempToEntity.convertRecords();

    Hib3MemberDAO.membersCacheClear();
    assertEquals("testSubjectIdentifier3", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject3", "jdbc", "person").iterator().next().getSubjectIdentifier0());
    assertEquals("testSubjectIdentifier4", GrouperDAOFactory.getFactory().getPITMember().findPITMembersBySubjectIdSourceAndType("testSubject4", "jdbc", "person").iterator().next().getSubjectIdentifier0());

    assertEquals(0, new SyncPITTables().showResults(false).syncAllPITTables());
    GrouperSession.startRootSession();
  }
  
  /**
   * 
   */
  public void testPersonMember() {
    Subject subj = SubjectTestHelper.SUBJ0;
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);

    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    //not case sensitive
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("lfname"), member.getSortString0());

    assertEquals(subj.getAttributeValue("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValue("LFNAME", true), member.getSortString0());
    
    assertTrue(subj.getAttributes().keySet().contains("lfname"));
    
    assertEquals(subj.getAttributes().get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes().get("LFNAME").iterator().next(), member.getSortString0());
    
    assertEquals(subj.getAttributes(false).get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes(false).get("LFNAME").iterator().next(), member.getSortString0());
    
    assertEquals(subj.getAttributes(true).get("lfname").iterator().next(), member.getSortString0());
    assertEquals(subj.getAttributes(true).get("LFNAME").iterator().next(), member.getSortString0());
    
    assertTrue(subj.getAttributeValues("lfname").contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME").contains(member.getSortString0()));
    
    assertTrue(subj.getAttributeValues("lfname", false).contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME", false).contains(member.getSortString0()));
    
    assertTrue(subj.getAttributeValues("lfname", true).contains(member.getSortString0()));
    assertTrue(subj.getAttributeValues("LFNAME", true).contains(member.getSortString0()));
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname"), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME"), member.getSortString0());
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname", false), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME", false), member.getSortString0());
    
    assertEquals(subj.getAttributeValueOrCommaSeparated("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValueOrCommaSeparated("LFNAME", true), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname"), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME"), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname", false), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME", false), member.getSortString0());
    
    assertEquals(subj.getAttributeValueSingleValued("lfname", true), member.getSortString0());
    assertEquals(subj.getAttributeValueSingleValued("LFNAME", true), member.getSortString0());
    
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());

    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(subj.getName() + "," + subj.getAttributeValue("LFNAME") + "," + subj.getAttributeValue("LOGINID") + "," + subj.getDescription() + "," + subj.getAttributeValue("EMAIL").toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    
    // verify internal attributes
    assertFalse(subj.getAttributes().containsKey("searchAttribute0"));
    assertTrue(subj.getAttributes(false).containsKey("searchAttribute0"));
    
    // verify subject identifier
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSubjectIdentifier0());
    
    // verify that an update will work
    member.setSortString0("bogus");
    member.setSortString1(null);
    member.store();
    
    // member record should get corrected by this
    SubjectFinder.flushCache();
    SubjectFinder.findById(subj.getId(), true);
    
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    
    GrouperCacheUtils.clearAllCaches();
    
    member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());
  }
  
  /**
   * 
   */
  public void testPersonMemberNonDefault() {
    
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectVirtualAttribute_2_sortAttribute2", "test2");
    source.addInitParam("sortAttribute2", "sortAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_sortAttribute3", "test3");
    source.addInitParam("sortAttribute3", "sortAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_sortAttribute4", "test4");
    source.addInitParam("sortAttribute4", "sortAttribute4");
    source.addInitParam("subjectVirtualAttribute_1_searchAttribute1", "test5");
    source.addInitParam("searchAttribute1", "searchAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_searchAttribute2", "test6");
    source.addInitParam("searchAttribute2", "searchAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_searchAttribute3", "test7");
    source.addInitParam("searchAttribute3", "searchAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_searchAttribute4", "test8");
    source.addInitParam("searchAttribute4", "searchAttribute4");
    source.addInitParam("subjectIdentifierAttribute0", "lfname");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    source.setSubjectIdentifierAttributesAll(null);
    
    Subject subj = SubjectFinder.findById("test.subject.0", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    assertEquals(subj.getAttributeValue("LFNAME"), member.getSortString0());
    assertEquals(subj.getAttributeValue("LOGINID"), member.getSortString1());
    assertEquals("test2", member.getSortString2());
    assertEquals("test3", member.getSortString3());
    assertEquals("test4", member.getSortString4());
    assertEquals(subj.getName() + "," + subj.getAttributeValue("LFNAME") + "," + subj.getAttributeValue("LOGINID") + "," + subj.getDescription() + "," + subj.getAttributeValue("EMAIL").toLowerCase(), member.getSearchString0());
    assertEquals("test5", member.getSearchString1());
    assertEquals("test6", member.getSearchString2());
    assertEquals("test7", member.getSearchString3());
    assertEquals("test8", member.getSearchString4());
    assertEquals(subj.getAttributeValue("lfname"), member.getSubjectIdentifier0());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_2_sortAttribute2");
    source.removeInitParam("sortAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_sortAttribute3");
    source.removeInitParam("sortAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_sortAttribute4");
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("subjectVirtualAttribute_1_searchAttribute1");
    source.removeInitParam("searchAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_searchAttribute2");
    source.removeInitParam("searchAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_searchAttribute3");
    source.removeInitParam("searchAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_searchAttribute4");
    source.removeInitParam("searchAttribute4");
    source.removeInitParam("subjectIdentifierAttribute0");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    source.setSubjectIdentifierAttributes(null);
  }
  
  /**
   * 
   */
  public void testInternalMembers() {
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ROOT, true);
    assertEquals(GrouperConfig.ROOT_NAME, member.getName());
    assertEquals(GrouperConfig.ROOT_NAME, member.getDescription());
    
    assertEquals(GrouperConfig.ROOT_NAME, member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(GrouperConfig.ROOT_NAME.toLowerCase() + "," + GrouperConfig.ROOT.toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(GrouperConfig.ROOT, member.getSubjectIdentifier0());

    edu.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE);
    member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ALL, true);
    assertEquals(GrouperConfig.ALL_NAME, member.getName());
    assertEquals(GrouperConfig.ALL_NAME, member.getDescription());
    
    assertEquals(GrouperConfig.ALL_NAME, member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(GrouperConfig.ALL_NAME.toLowerCase() + "," + GrouperConfig.ALL.toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(GrouperConfig.ALL, member.getSubjectIdentifier0());

    // verify internal attributes
    assertFalse(SubjectFinder.findAllSubject().getAttributes().containsKey("searchAttribute0"));
    assertTrue(SubjectFinder.findAllSubject().getAttributes(false).containsKey("searchAttribute0"));
  }
  
  /**
   * 
   */
  public void testInternalMembersNonDefaultAttributes() {
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute1.el", "test1");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute2.el", "test2");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute3.el", "test3");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.sortAttribute4.el", "test4");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute1.el", "test5");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute2.el", "test6");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute3.el", "test7");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("internalSubjects.searchAttribute4.el", "test8");
      // looks like we're not using this singleton anymore???
      // InternalSourceAdapter.instance().init();
      
      SubjectFinder.findById("GrouperAll", true).getSource().init();
      
      // need a 1 ms sleep here to make sure call to ExpirableCache.clearAll() is effective
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        // ignore
      }

      ExpirableCache.clearAll();
      SubjectFinder.flushCache();
      Hib3MemberDAO.membersCacheClear();

      edu.grantPriv(SubjectFinder.findById("GrouperAll", true), NamingPrivilege.CREATE);

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
  
      Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(GrouperConfig.ALL, true);
  
      assertEquals(GrouperConfig.ALL_NAME, member.getName());
      assertEquals(GrouperConfig.ALL_NAME, member.getDescription());
      
      assertEquals(GrouperConfig.ALL_NAME, member.getSortString0());
      assertEquals("test1", member.getSortString1());
      assertEquals("test2", member.getSortString2());
      assertEquals("test3", member.getSortString3());
      assertEquals("test4", member.getSortString4());
      assertEquals(GrouperConfig.ALL_NAME.toLowerCase() + "," + GrouperConfig.ALL.toLowerCase(), member.getSearchString0());
      assertEquals("test5", member.getSearchString1());
      assertEquals("test6", member.getSearchString2());
      assertEquals("test7", member.getSearchString3());
      assertEquals("test8", member.getSearchString4());
    } finally {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();
      
      SubjectFinder.findById("GrouperAll", true).getSource().init();
      ExpirableCache.clearAll();
      SubjectFinder.flushCache();
      Hib3MemberDAO.membersCacheClear();
      GrouperCacheUtils.clearAllCaches();
      edu.revokePriv(SubjectFinder.findById("GrouperAll", true), NamingPrivilege.CREATE);

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
  
  /**
   * 
   */
  public void testGroupAdd() {
    Group group = edu.addChildGroup("Test", "Test Display");
    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(group.getName(), member.getSubjectIdentifier0());
    
    // verify internal attributes
    assertFalse(group.toSubject().getAttributes().containsKey("searchAttribute0"));
    assertTrue(group.toSubject().getAttributes(false).containsKey("searchAttribute0"));
  }
  
  /**
   * 
   */
  public void testGroupAddNonDefault() {
    BaseSourceAdapter source = (BaseSourceAdapter) SubjectFinder.internal_getGSA();
    source.addInitParam("subjectVirtualAttribute_1_sortAttribute1", "test1");
    source.addInitParam("sortAttribute1", "sortAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_sortAttribute2", "test2");
    source.addInitParam("sortAttribute2", "sortAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_sortAttribute3", "test3");
    source.addInitParam("sortAttribute3", "sortAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_sortAttribute4", "test4");
    source.addInitParam("sortAttribute4", "sortAttribute4");
    source.addInitParam("subjectVirtualAttribute_1_searchAttribute1", "test5");
    source.addInitParam("searchAttribute1", "searchAttribute1");
    source.addInitParam("subjectVirtualAttribute_2_searchAttribute2", "test6");
    source.addInitParam("searchAttribute2", "searchAttribute2");
    source.addInitParam("subjectVirtualAttribute_3_searchAttribute3", "test7");
    source.addInitParam("searchAttribute3", "searchAttribute3");
    source.addInitParam("subjectVirtualAttribute_4_searchAttribute4", "test8");
    source.addInitParam("searchAttribute4", "searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    SourceManager.getInstance().loadSource(source);
    Group group = edu.addChildGroup("Test", "Test Display");

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertEquals("test1", member.getSortString1());
    assertEquals("test2", member.getSortString2());
    assertEquals("test3", member.getSortString3());
    assertEquals("test4", member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertEquals("test5", member.getSearchString1());
    assertEquals("test6", member.getSearchString2());
    assertEquals("test7", member.getSearchString3());
    assertEquals("test8", member.getSearchString4());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_1_sortAttribute1");
    source.removeInitParam("sortAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_sortAttribute2");
    source.removeInitParam("sortAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_sortAttribute3");
    source.removeInitParam("sortAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_sortAttribute4");
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("subjectVirtualAttribute_1_searchAttribute1");
    source.removeInitParam("searchAttribute1");
    source.removeInitParam("subjectVirtualAttribute_2_searchAttribute2");
    source.removeInitParam("searchAttribute2");
    source.removeInitParam("subjectVirtualAttribute_3_searchAttribute3");
    source.removeInitParam("searchAttribute3");
    source.removeInitParam("subjectVirtualAttribute_4_searchAttribute4");
    source.removeInitParam("searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
  }
  
  /**
   * 
   */
  public void testGroupUpdateDescription() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setDescription("Test Description");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertEquals(group.getDescription(), member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testGroupUpdateDisplayExtension() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setDisplayExtension("Test Display2");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
  }
  
  /**
   * 
   */
  public void testGroupRename() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.setExtension("Test2");
    group.store();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(group.getName(), member.getSubjectIdentifier0());
  }
  
  /**
   * 
   */
  public void testGroupMove() {
    Group group = edu.addChildGroup("Test", "Test Display");
    group.move(edu2);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);

    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(group.getName(), member.getSubjectIdentifier0());
  }
  
  /**
   * 
   */
  public void testStemRename() {
    Group group = edu.addChildGroup("Test", "Test Display");
    edu.setExtension("edu3");
    edu.store();
    group = GrouperDAOFactory.getFactory().getGroup().findByUuid(group.getId(), true);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + "," + group.getAlternateNameDb().toLowerCase(), member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(group.getName(), member.getSubjectIdentifier0());
  }

  /**
   * 
   */
  public void testStemDisplayExtensionUpdate() {
    Group group = edu.addChildGroup("Test", "Test Display");
    edu.setDisplayExtension("edu3");
    edu.store();
    group = GrouperDAOFactory.getFactory().getGroup().findByUuid(group.getId(), true);

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      // ignore
    }

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(group.getId(), true);
    
    assertEquals(group.getName(), member.getName());
    assertNull(member.getDescription());
    
    assertEquals(group.getDisplayExtension(), member.getSortString0());
    assertNull(member.getSortString1());
    assertNull(member.getSortString2());
    assertNull(member.getSortString3());
    assertNull(member.getSortString4());
    assertEquals(group.getName().toLowerCase() + "," + group.getDisplayName().toLowerCase() + ",", member.getSearchString0());
    assertNull(member.getSearchString1());
    assertNull(member.getSearchString2());
    assertNull(member.getSearchString3());
    assertNull(member.getSearchString4());
    assertEquals(group.getName(), member.getSubjectIdentifier0());
  }
  
  /**
   * 
   */
  public void testBasicNonSecureSearchAndSort() {
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    // this should return all people members
    Member[] members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Someschool test").toArray(new Member[0]);
    assertEquals(5, members.length);
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // this should only return one member
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "  Someschool 3  test  ").toArray(new Member[0]);
    assertEquals(1, members.length);
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[0].getName());
    
    // this shouldn't return anybody
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Someschool 5 test").toArray(new Member[0]);
    assertEquals(0, members.length);
    
    // this should return the people and the group
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // this should return the people and the group too
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    
    // this should return the people and the group too
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, null).toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // use query sort this time - asc
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sortAsc("sortString0");
    
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, queryOptions, true, 
        null, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[1].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[4].getName());
    
    // use query sort this time - desc
    queryOptions = new QueryOptions();
    queryOptions.sortDesc("sortString0");
    
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, queryOptions, true, 
        null, SearchStringEnum.getDefaultSearchString(), "Test").toArray(new Member[0]);
    assertEquals(6, members.length);
    assertEquals(group2.getName(), members[0].getName());
    assertEquals(SubjectTestHelper.SUBJ0.getName(), members[5].getName());
    assertEquals(SubjectTestHelper.SUBJ1.getName(), members[4].getName());
    assertEquals(SubjectTestHelper.SUBJ2.getName(), members[3].getName());
    assertEquals(SubjectTestHelper.SUBJ3.getName(), members[2].getName());
    assertEquals(SubjectTestHelper.SUBJ4.getName(), members[1].getName());
  }
  
  /**
   * 
   */
  public void testGetDefaultSearchIndex() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.search.defaultIndexOrder", "3,1,4,0,2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "true");

    Group allowGroup1 = edu.addChildGroup("allowGroup1", "allowGroup1");
    Group allowGroup2 = edu.addChildGroup("allowGroup2", "allowGroup2");
    allowGroup1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    allowGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    
    // now verify that subj1 doesn't have a default index
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertNull(SearchStringEnum.getDefaultSearchString());
    assertFalse(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_2, SearchStringEnum.getDefaultSearchString());

    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_0, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_4, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "false");
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.allowOnlyGroup", "edu:allowGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.allowOnlyGroup", "edu:allowGroup2");

    // again, subj1 should have no access
    assertNull(SearchStringEnum.getDefaultSearchString());
    assertFalse(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // add subj1 to one of the allow groups and test again
    allowGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertFalse(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // add subj1 to the other allow group and test again
    allowGroup2.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    assertTrue(SearchStringEnum.SEARCH_STRING_0.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_1.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_2.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_3.hasAccess());
    assertTrue(SearchStringEnum.SEARCH_STRING_4.hasAccess());
    
    // remove subj1 from both groups and verify access as each restriction is lifted.
    allowGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    allowGroup2.deleteMember(SubjectTestHelper.SUBJ0);
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string2.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_2, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string0.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_0, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string4.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_4, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string1.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_1, SearchStringEnum.getDefaultSearchString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.search.string3.allowOnlyGroup");
    assertEquals(SearchStringEnum.SEARCH_STRING_3, SearchStringEnum.getDefaultSearchString());
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testGetDefaultSortIndex() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("member.sort.defaultIndexOrder", "3,1,4,0,2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "true");

    Group allowGroup1 = edu.addChildGroup("allowGroup1", "allowGroup1");
    Group allowGroup2 = edu.addChildGroup("allowGroup2", "allowGroup2");
    allowGroup1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    allowGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);
    
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_3.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_4.hasAccess());
    
    
    // now verify that subj1 doesn't have a default index
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    assertNull(SortStringEnum.getDefaultSortString());
    assertFalse(SortStringEnum.SORT_STRING_0.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_1.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_2, SortStringEnum.getDefaultSortString());

    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_0, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_4, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "false");
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.allowOnlyGroup", "edu:allowGroup1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.allowOnlyGroup", "edu:allowGroup2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.allowOnlyGroup", "edu:allowGroup2");

    // again, subj1 should have no access
    assertNull(SortStringEnum.getDefaultSortString());
    assertFalse(SortStringEnum.SORT_STRING_0.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_1.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // add subj1 to one of the allow groups and test again
    allowGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_3.hasAccess());
    assertFalse(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // add subj1 to the other allow group and test again
    allowGroup2.addMember(SubjectTestHelper.SUBJ0);
    
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    assertTrue(SortStringEnum.SORT_STRING_0.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_1.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_2.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_3.hasAccess());
    assertTrue(SortStringEnum.SORT_STRING_4.hasAccess());
    
    // remove subj1 from both groups and verify access as each restriction is lifted.
    allowGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    allowGroup2.deleteMember(SubjectTestHelper.SUBJ0);
    
    // now verify that subj1 can use index 2
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string2.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_2, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string0.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_0, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 4
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string4.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_4, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 1
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string1.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_1, SortStringEnum.getDefaultSortString());
    
    // now verify that subj1 can use index 3
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("security.member.sort.string3.allowOnlyGroup");
    assertEquals(SortStringEnum.SORT_STRING_3, SortStringEnum.getDefaultSortString());
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testSearchSecurity() {
    
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);

    // these should all work
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_0, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_2, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_3, "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_4, "Someschool test");
    
    // now set restictions
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.search.string4.wheelOnly", "true");
    
    // and subj1 cannot use any of the search strings now
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_0, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_1, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_2, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_3, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.getDefaultSortString(), SearchStringEnum.SEARCH_STRING_4, "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testSortSecurity() {
    
    Group group = edu.addChildGroup("test", "test");
    Group group2 = edu.addChildGroup("test2", "test2");
    group.addMember(SubjectTestHelper.SUBJ2);
    group.addMember(SubjectTestHelper.SUBJ1);
    group.addMember(SubjectTestHelper.SUBJ0);
    group.addMember(SubjectTestHelper.SUBJ3);
    group.addMember(SubjectTestHelper.SUBJ4);
    group.addMember(group2.toSubject());
    
    GrouperSession s = GrouperSession.start(SubjectTestHelper.SUBJ0);

    // these should all work
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_0, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_2, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_3, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.getDefaultSearchString(), "Someschool test");
    
    
    // now set restictions
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string0.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string1.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string2.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string3.wheelOnly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("security.member.sort.string4.wheelOnly", "true");
    
    // and subj1 cannot use any of the sort strings now
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_0, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_1, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_2, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_3, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
          group.getId(), Group.getDefaultList(), null, null, null, true, 
          SortStringEnum.SORT_STRING_4, SearchStringEnum.getDefaultSearchString(), "Someschool test");
      fail("Failed to throw RuntimeException");
    } catch (RuntimeException e) {
      // good
    }
    
    s.stop();
  }
  
  /**
   * 
   */
  public void testNonDefaultSearchAndSort() {
    
    // we are going to sort by EMAIL and search by LOGINID
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("sortAttribute4", "EMAIL");
    source.addInitParam("searchAttribute4", "LOGINID");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
    
    // update subject attributes
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'def' where subjectid='test.subject.0' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'ghi' where subjectid='test.subject.1' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'abc' where subjectid='test.subject.2' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'mno' where subjectid='test.subject.3' and name='email'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'jkl' where subjectid='test.subject.4' and name='email'");

    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Second,Third' where subjectid='test.subject.0' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Third' where subjectid='test.subject.1' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'Third,Forth' where subjectid='test.subject.2' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'First,Second,Third,Forth' where subjectid='test.subject.3' and name='loginid'");
    HibernateSession.bySqlStatic().executeSql("update subjectattribute set value = 'Second,Forth' where subjectid='test.subject.4' and name='loginid'");

    Subject subj0 = SubjectFinder.findById("test.subject.0", true);
    Subject subj1 = SubjectFinder.findById("test.subject.1", true);
    Subject subj2 = SubjectFinder.findById("test.subject.2", true);
    Subject subj3 = SubjectFinder.findById("test.subject.3", true);
    Subject subj4 = SubjectFinder.findById("test.subject.4", true);
    
    // add a few subjects to a group
    Group group = edu.addChildGroup("test", "test");
    group.addMember(subj0);
    group.addMember(subj1);
    group.addMember(subj2);
    group.addMember(subj3);
    group.addMember(subj4);
    
    // this should return all members since there's no searching
    Member[] members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, null, null).toArray(new Member[0]);
    assertEquals(5, members.length);
    assertEquals(subj2.getName(), members[0].getName());
    assertEquals(subj0.getName(), members[1].getName());
    assertEquals(subj1.getName(), members[2].getName());
    assertEquals(subj4.getName(), members[3].getName());
    assertEquals(subj3.getName(), members[4].getName());
    
    // this should return subj2, subj3, and subj4 since we're searching for "FORTH"
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.SEARCH_STRING_4, "FORTH").toArray(new Member[0]);
    assertEquals(3, members.length);
    assertEquals(subj2.getName(), members[0].getName());
    assertEquals(subj4.getName(), members[1].getName());
    assertEquals(subj3.getName(), members[2].getName());
    
    // this should return subj0 and subj3 since we're searching for "THIRD SECOND"
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByOwnerAndFieldAndType(
        group.getId(), Group.getDefaultList(), null, null, null, true, 
        SortStringEnum.SORT_STRING_4, SearchStringEnum.SEARCH_STRING_4, "THIRD SECOND").toArray(new Member[0]);
    assertEquals(2, members.length);
    assertEquals(subj0.getName(), members[0].getName());
    assertEquals(subj3.getName(), members[1].getName());
    
    // reset the state
    source.removeInitParam("sortAttribute4");
    source.removeInitParam("searchAttribute4");
    ExpirableCache.clearAll();
    source.setSearchAttributes(null);
    source.setSortAttributes(null);
  }
  
  /**
   * 
   */
  public void testEntityAdd() {
    Group testEntity = new GroupSave(grouperSession).assignName("etc:entity").assignTypeOfGroup(TypeOfGroup.entity).save();
    assertEquals(testEntity.getName(), testEntity.toMember().getSubjectIdentifier0());
  }

  /**
   *
   */
  public void testVirtualAttributeInName() {
    
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectVirtualAttribute_1_namev", "${subject.getAttributeValue('EMAIL').replace('@', '%')}");
    source.addInitParam("Name_AttributeType", "namev");
    ExpirableCache.clearAll();
    source.init();
    
    Subject subj = SubjectFinder.findById("test.subject.0", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    
    assertEquals("test.subject.0%somewhere.someSchool.edu", subj.getName());
    
    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_1_namev");
    source.addInitParam("Name_AttributeType", "name");
    ExpirableCache.clearAll();
    source.init();
  }
  
  /**
   * 
   */
  public void testVirtualAttributeInDescription() {
    
    BaseSourceAdapter source = (BaseSourceAdapter) SourceManager.getInstance().getSource("jdbc");
    source.addInitParam("subjectVirtualAttribute_1_descriptionv", "${subject.getAttributeValue('EMAIL').replace('@', '%')}");
    source.addInitParam("Description_AttributeType", "descriptionv");
    ExpirableCache.clearAll();
    source.init();
    
    Subject subj = SubjectFinder.findById("test.subject.0", true);
    edu.grantPriv(subj, NamingPrivilege.CREATE);

    Member member = GrouperDAOFactory.getFactory().getMember().findBySubject(subj, true);
    
    assertEquals("test.subject.0%somewhere.someSchool.edu", subj.getDescription());
    
    assertEquals(subj.getName(), member.getName());
    assertEquals(subj.getDescription(), member.getDescription());
    
    // reset the state
    source.removeInitParam("subjectVirtualAttribute_1_descriptionv");
    source.addInitParam("Description_AttributeType", "description");
    ExpirableCache.clearAll();
    source.init();
  }
  
  private void deleteSubject(Subject subject) {
    
    String subjectId = subject.getId();
    
    HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid='" + subjectId + "'", null, null);
    HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid='" + subjectId + "'", null, null);
 

    SubjectFinder.flushCache();

    try {
      SubjectFinder.findById(subject.getId(), true);
      fail("should not find subject " + subject.getId());
    } catch (SubjectNotFoundException e) {
      // OK
    } catch (SubjectNotUniqueException e) {
      fail("subject should be unique " + subject.getId());
    }
  }
}
