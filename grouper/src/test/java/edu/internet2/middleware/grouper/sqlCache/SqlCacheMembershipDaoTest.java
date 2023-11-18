package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


public class SqlCacheMembershipDaoTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new SqlCacheMembershipDaoTest("testInsertDeleteIfCacheableUsingChangeLog"));
  }
  
  public SqlCacheMembershipDaoTest(String name) {
    super(name);
  }

  protected void setUp() {
    super.setUp();
  }

  protected void tearDown() {
    super.tearDown();
  }

  public void testStoreRetrieve() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Field members = Group.getDefaultList();
    
    Subject subject = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    group.addMember(subject);
    Member member = MemberFinder.findBySubject(grouperSession, subject, false, null);
    
    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
    sqlCacheGroup.setFieldInternalId(members.getInternalId());
    sqlCacheGroup.setGroupInternalId(group.getInternalId());
    sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() + 10*1000*60));
    SqlCacheGroupDao.store(sqlCacheGroup);
    
    
    SqlCacheMembership sqlCacheMembership = new SqlCacheMembership();
    sqlCacheMembership.setSqlCacheGroupInternalId(sqlCacheGroup.getInternalId());
    sqlCacheMembership.setMemberInternalId(member.getInternalId());
    sqlCacheMembership.setFlattenedAddTimestamp(new Timestamp(System.currentTimeMillis()));
    SqlCacheMembershipDao.store(sqlCacheMembership);
    
    sqlCacheMembership = SqlCacheMembershipDao.retrieveByInternalId(sqlCacheMembership.getInternalId());
    
    SqlCacheMembershipDao.delete(sqlCacheMembership);

    sqlCacheMembership = SqlCacheMembershipDao.retrieveByInternalId(sqlCacheMembership.getInternalId());
    
    assertNull(sqlCacheMembership);
    
  }
  

  public void testRetrieveOrCreate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Field members = Group.getDefaultList();
    
    SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
    sqlCacheGroup.setFieldInternalId(members.getInternalId());
    sqlCacheGroup.setGroupInternalId(group.getInternalId());
    sqlCacheGroup.setEnabledOn(new Timestamp(System.currentTimeMillis() + 10*1000*60));

    SqlCacheGroupDao.retrieveOrCreateBySqlGroupCache(GrouperUtil.toList(sqlCacheGroup));
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());

    
    MultiKey multiKey = new MultiKey(group.getInternalId(), members.getInternalId());
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(GrouperUtil.toList(multiKey)).get(multiKey);
    
    assertNotNull(sqlCacheGroup);

    multiKey = new MultiKey(group.getName(), members.getName());
    
    sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(multiKey)).get(multiKey);
    
    assertNotNull(sqlCacheGroup);
    
    SqlCacheGroupDao.delete(sqlCacheGroup);

    sqlCacheGroup = SqlCacheGroupDao.retrieveByInternalId(sqlCacheGroup.getInternalId());
    
    assertNull(sqlCacheGroup);
    
  }
  
  public void testInsertDeleteIfCacheable() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // normal group
    Group group1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    
    // admins
    Group group2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();

    // not cacheable
    Group group3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    
    // not enabled yet
    Group group4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    
    // disabled
    Group group5 = new GroupSave().assignName("test:testGroup5").assignCreateParentStemsIfNotExist(true).save();
    
    Field members = Group.getDefaultList();
    Field admins = FieldFinder.find("admins", true);

    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, true);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, true);
    
    group1.addMember(subject1);
    group2.grantPriv(subject2, AccessPrivilege.ADMIN);
    group3.addMember(subject3);
    group4.addMember(subject4);
    group5.addMember(subject5);

    SqlCacheGroup sqlCacheGroup1 = new SqlCacheGroup();
    sqlCacheGroup1.setFieldInternalId(members.getInternalId());
    sqlCacheGroup1.setGroupInternalId(group1.getInternalId());
    long currentTimeMillis = System.currentTimeMillis();
    sqlCacheGroup1.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

    SqlCacheGroup sqlCacheGroup2 = new SqlCacheGroup();
    sqlCacheGroup2.setFieldInternalId(admins.getInternalId());
    sqlCacheGroup2.setGroupInternalId(group2.getInternalId());
    sqlCacheGroup2.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

//    SqlCacheGroup sqlCacheGroup3 = new SqlCacheGroup();
//    sqlCacheGroup3.setFieldInternalId(members.getInternalId());
//    sqlCacheGroup3.setGroupInternalId(group1.getInternalId());
//    sqlCacheGroup3.setEnabledOn(new Timestamp(System.currentTimeMillis() - 10*1000*60));

    SqlCacheGroup sqlCacheGroup4 = new SqlCacheGroup();
    sqlCacheGroup4.setFieldInternalId(members.getInternalId());
    sqlCacheGroup4.setGroupInternalId(group4.getInternalId());
    sqlCacheGroup4.setEnabledOn(new Timestamp(currentTimeMillis + 10*1000*60));

    SqlCacheGroup sqlCacheGroup5 = new SqlCacheGroup();
    sqlCacheGroup5.setFieldInternalId(members.getInternalId());
    sqlCacheGroup5.setGroupInternalId(group5.getInternalId());
    sqlCacheGroup5.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));
    sqlCacheGroup5.setDisabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

    
    
    SqlCacheGroupDao.retrieveOrCreateBySqlGroupCache(GrouperUtil.toList(sqlCacheGroup1, sqlCacheGroup2, sqlCacheGroup4, sqlCacheGroup5));
    
    String sqlMembershipView = "select gscmv.group_name, gscmv.list_name, gscmv.subject_source, gscmv.subject_id, gscmv.flattened_add_timestamp from grouper_sql_cache_mship_v gscmv order by 1, 2, 3, 4";
    
    List<Object[]> groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(
        sqlMembershipView)
      .selectList(Object[].class);
    
    assertEquals(0, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));
    
    List<MultiKey> groupNameFieldNameSubjectIdSourceIdStartedMillisToInsert = GrouperUtil.toList(
        new MultiKey(group1.getName(), members.getName(), subject1.getSourceId(), subject1.getId(), currentTimeMillis - 1000),
        new MultiKey(group2.getName(), admins.getName(), subject2.getSourceId(), subject2.getId(), currentTimeMillis - 2000),
        new MultiKey(group3.getName(), members.getName(), subject3.getSourceId(), subject3.getId(), currentTimeMillis - 3000),
        new MultiKey(group4.getName(), members.getName(), subject3.getSourceId(), subject4.getId(), currentTimeMillis - 4000),
        new MultiKey(group5.getName(), members.getName(), subject4.getSourceId(), subject5.getId(), currentTimeMillis - 5000));
    
    int inserts = SqlCacheMembershipDao.insertSqlCacheMembershipsIfCacheable(groupNameFieldNameSubjectIdSourceIdStartedMillisToInsert);

    assertEquals(2, inserts);

    groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(sqlMembershipView).selectList(Object[].class);

    assertEquals(2, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));

    assertEquals(group1.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[0]);
    assertEquals(members.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[1]);
    assertEquals(subject1.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[2]);
    assertEquals(subject1.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[3]);
    assertEquals(currentTimeMillis - 1000, ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());
    
    assertEquals(group2.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[0]);
    assertEquals(admins.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[1]);
    assertEquals(subject2.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[2]);
    assertEquals(subject2.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[3]);
    assertEquals(currentTimeMillis - 2000, ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[4]).getTime());

    List<MultiKey> groupNameFieldNameSubjectIdSourceIdStartedMillisToDelete = GrouperUtil.toList(
        new MultiKey(group1.getName(), members.getName(), subject1.getSourceId(), subject1.getId()),
        new MultiKey(group2.getName(), admins.getName(), subject2.getSourceId(), subject2.getId()));

    int deletes = SqlCacheMembershipDao.deleteSqlCacheMembershipsIfCacheable(groupNameFieldNameSubjectIdSourceIdStartedMillisToDelete);

    assertEquals(2, deletes);

    groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(sqlMembershipView).selectList(Object[].class);
    assertEquals(0, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));
  }
  
  public void testInsertDeleteIfCacheableUsingChangeLog() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // normal group
    Group group1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    
    // admins
    Group group2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();

    // not cacheable
    Group group3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    
    // not enabled yet
    Group group4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    
    // disabled
    Group group5 = new GroupSave().assignName("test:testGroup5").assignCreateParentStemsIfNotExist(true).save();
    
    Field members = Group.getDefaultList();
    Field admins = FieldFinder.find("admins", true);

    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, true);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, true);
    
    // these don't do anything in the end
    group1.addMember(subject1);
    group1.deleteMember(subject1);
    group1.addMember(subject2);
    group1.deleteMember(subject2);
    group2.grantPriv(subject1, AccessPrivilege.ADMIN);
    group2.revokePriv(subject1, AccessPrivilege.ADMIN);
    
    group1.addMember(subject1);
    group2.grantPriv(subject2, AccessPrivilege.ADMIN);
    group3.addMember(subject3);
    group4.addMember(subject4);
    group5.addMember(subject5);
    
    Membership g1ToS1 = MembershipFinder.findImmediateMembership(grouperSession, group1, subject1, members, true);
    Membership g2ToS2 = MembershipFinder.findImmediateMembership(grouperSession, group2, subject2, admins, true);

    SqlCacheGroup sqlCacheGroup1 = new SqlCacheGroup();
    sqlCacheGroup1.setFieldInternalId(members.getInternalId());
    sqlCacheGroup1.setGroupInternalId(group1.getInternalId());
    long currentTimeMillis = System.currentTimeMillis();
    sqlCacheGroup1.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

    SqlCacheGroup sqlCacheGroup2 = new SqlCacheGroup();
    sqlCacheGroup2.setFieldInternalId(admins.getInternalId());
    sqlCacheGroup2.setGroupInternalId(group2.getInternalId());
    sqlCacheGroup2.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

//    SqlCacheGroup sqlCacheGroup3 = new SqlCacheGroup();
//    sqlCacheGroup3.setFieldInternalId(members.getInternalId());
//    sqlCacheGroup3.setGroupInternalId(group1.getInternalId());
//    sqlCacheGroup3.setEnabledOn(new Timestamp(System.currentTimeMillis() - 10*1000*60));

    SqlCacheGroup sqlCacheGroup4 = new SqlCacheGroup();
    sqlCacheGroup4.setFieldInternalId(members.getInternalId());
    sqlCacheGroup4.setGroupInternalId(group4.getInternalId());
    sqlCacheGroup4.setEnabledOn(new Timestamp(currentTimeMillis + 10*1000*60));

    SqlCacheGroup sqlCacheGroup5 = new SqlCacheGroup();
    sqlCacheGroup5.setFieldInternalId(members.getInternalId());
    sqlCacheGroup5.setGroupInternalId(group5.getInternalId());
    sqlCacheGroup5.setEnabledOn(new Timestamp(currentTimeMillis - 10*1000*60));
    sqlCacheGroup5.setDisabledOn(new Timestamp(currentTimeMillis - 10*1000*60));

    
    
    SqlCacheGroupDao.retrieveOrCreateBySqlGroupCache(GrouperUtil.toList(sqlCacheGroup1, sqlCacheGroup2, sqlCacheGroup4, sqlCacheGroup5));
    
    String sqlMembershipView = "select gscmv.group_name, gscmv.list_name, gscmv.subject_source, gscmv.subject_id, gscmv.flattened_add_timestamp from grouper_sql_cache_mship_v gscmv order by 1, 2, 3, 4";
    
    List<Object[]> groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(
        sqlMembershipView)
      .selectList(Object[].class);
    
    assertEquals(0, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));
    
    ChangeLogTempToEntity.convertRecords();
    PITMembership pitG1ToS1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(g1ToS1.getImmediateMembershipId(), true);
    PITMembership pitG2ToS2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(g2ToS2.getImmediateMembershipId(), true);
    
    groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(sqlMembershipView).selectList(Object[].class);

    assertEquals(2, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));

    assertEquals(group1.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[0]);
    assertEquals(members.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[1]);
    assertEquals(subject1.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[2]);
    assertEquals(subject1.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[3]);
    assertEquals(pitG1ToS1.getStartTime().getTime(), ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());
    
    assertEquals(group2.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[0]);
    assertEquals(admins.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[1]);
    assertEquals(subject2.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[2]);
    assertEquals(subject2.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[3]);
    assertEquals(pitG2ToS2.getStartTime().getTime(), ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(1)[4]).getTime());

    group1.deleteMember(subject1);
    group2.revokePriv(subject2, AccessPrivilege.ADMIN);
    group3.deleteMember(subject3);
    group4.deleteMember(subject4);
    group5.deleteMember(subject5);
    ChangeLogTempToEntity.convertRecords();
    
    groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(sqlMembershipView).selectList(Object[].class);
    assertEquals(0, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));
  }
  
  
}
