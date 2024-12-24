package edu.internet2.middleware.grouper.sqlCache;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class SqlCacheMembershipHstTest extends GrouperTest {

  public SqlCacheMembershipHstTest(String name) {
    super(name);
  }

  protected void setUp() {
    super.setUp();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  protected void tearDown() {
    super.tearDown();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new SqlCacheMembershipHstTest("testMembershipHistoryAddDeleteRepeatSingleIncrementalSync"));
  }
  
  public void testMembershipHistoryAddDeleteRepeatSingleIncrementalSync() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);

    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
   
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
   
    // get the dependency table populated
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);
    
    // will be used later
    testGroup1.addMember(SubjectTestHelper.SUBJ2);
    ChangeLogTempToEntity.convertRecords();
   
    SqlCacheGroup sqlCacheGroupTestGroup1MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup1.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
   
    // add/delete/add/delete
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    Membership testGroup1Subj0Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    Membership testGroup1Subj0Membership2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    
    // add/delete/add
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    GrouperUtil.sleep(10);
    Membership testGroup1Subj1Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ1, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ1);
    GrouperUtil.sleep(10);
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    
    // delete/add
    Membership testGroup1Subj2Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ2, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    testGroup1.addMember(SubjectTestHelper.SUBJ2);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(4L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
  
    Member subj0Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    Member subj1Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
    Member subj2Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
  
    PITMembership testGroup1Subj0PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup1Subj0PITMembership2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership2.getImmediateMembershipId(), true);
    PITMembership testGroup1Subj1PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj1Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup1Subj2PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj2Membership1.getImmediateMembershipId(), true);

    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership2.getEndTimeDb())
        .select(Long.class));

    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj1Member.getInternalId())
        .addBindVar(testGroup1Subj1PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj1PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup1Subj2PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj2PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // no changes expected
    runFullSync(false);
  }
  
  public void testMembershipHistoryMultipleDeleteSingleIncrementalSync() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);

    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    Stem testStem1 = new StemSave().assignName("test:testStem1").save();
    Stem testStem2 = new StemSave().assignName("test:testStem2").save();

    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testGroup3.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupAdminsAttributeName());
    testGroup3.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupReadersAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemCreatorsAttributeName());

    // get the dependency table populated
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);
    
    // add some stuff to delete later
    
    // history for testGroup1 -> subj1
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    GrouperUtil.sleep(10);
    
    // history for testGroup1 -> subj2
    testGroup1.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    
    // history for testGroup2 -> subj2
    testGroup2.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    
    // history for testGroup2 -> subj3
    testGroup2.addMember(SubjectTestHelper.SUBJ3);
    GrouperUtil.sleep(10);
    
    // this is nothing
    testGroup3.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    
    // this is nothing
    testGroup4.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    
    // this is nothing
    testGroup1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    GrouperUtil.sleep(10);
    
    // testGroup3 -> subj2 (admins)
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    GrouperUtil.sleep(10);
    
    // testGroup3 -> subj2 (readers)
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    GrouperUtil.sleep(10);
    
    // this is nothing
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    GrouperUtil.sleep(10);
    
    // testStem1 -> subj2 (creators)
    testStem1.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    
    // this is nothing
    testStem2.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(0L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));

    SqlCacheGroup sqlCacheGroupTestGroup1MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup1.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup2MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup2.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup3AdminsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup3.getInternalId(), FieldFinder.find("admins", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup3ReadersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup3.getInternalId(), FieldFinder.find("readers", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestStem1CreatorsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testStem1.getIdIndex(), FieldFinder.find("creators", true).getInternalId(), null);

    // this is nothing
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    // history for testGroup1 -> subj1
    Membership testGroup1Subj1Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ1, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ1);

    // history for testGroup1 -> subj2
    Membership testGroup1Subj2Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ2, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ2);
    
    // history for testGroup2 -> subj2
    Membership testGroup2Subj2Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup2, SubjectTestHelper.SUBJ2, true);
    testGroup2.deleteMember(SubjectTestHelper.SUBJ2);

    // history for testGroup2 -> subj3
    Membership testGroup2Subj3Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup2, SubjectTestHelper.SUBJ3, true);
    testGroup2.deleteMember(SubjectTestHelper.SUBJ3);
    
    // this is nothing
    testGroup3.deleteMember(SubjectTestHelper.SUBJ2);
    
    // this is nothing
    testGroup4.deleteMember(SubjectTestHelper.SUBJ2);
    
    // this is nothing
    testGroup1.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    // testGroup3 -> subj2 (admins)
    Membership testGroup3Subj2AdminsMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup3, SubjectTestHelper.SUBJ2, FieldFinder.find("admins", true), true);
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    // testGroup3 -> subj2 (readers)
    Membership testGroup3Subj2ReadersMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup3, SubjectTestHelper.SUBJ2, FieldFinder.find("readers", true), true);
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    // this is nothing
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    
    // testStem1 -> subj2 (creators)
    Membership testStem1Subj2CreatorsMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem1, SubjectTestHelper.SUBJ2, FieldFinder.find("creators", true), true);
    testStem1.revokePriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    
    // this is nothing
    testStem2.revokePriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(7L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
  
    Member subj1Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
    Member subj2Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
    Member subj3Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);

    PITMembership testGroup1Subj1PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj1Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup1Subj2PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj2Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup2Subj2PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup2Subj2Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup2Subj3PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup2Subj3Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup3Subj2AdminsPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup3Subj2AdminsMembership1.getImmediateMembershipId(), true);
    PITMembership testGroup3Subj2ReadersPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup3Subj2ReadersMembership1.getImmediateMembershipId(), true);
    PITMembership testStem1Subj2CreatorsPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testStem1Subj2CreatorsMembership1.getImmediateMembershipId(), true);

    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj1Member.getInternalId())
        .addBindVar(testGroup1Subj1PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj1PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup1Subj2PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj2PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup2MembersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup2Subj2PITMembership1.getStartTimeDb())
        .addBindVar(testGroup2Subj2PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup2MembersField.getInternalId())
        .addBindVar(subj3Member.getInternalId())
        .addBindVar(testGroup2Subj3PITMembership1.getStartTimeDb())
        .addBindVar(testGroup2Subj3PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup3AdminsField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup3Subj2AdminsPITMembership1.getStartTimeDb())
        .addBindVar(testGroup3Subj2AdminsPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup3ReadersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup3Subj2ReadersPITMembership1.getStartTimeDb())
        .addBindVar(testGroup3Subj2ReadersPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testStem1Subj2CreatorsPITMembership1.getStartTimeDb())
        .addBindVar(testStem1Subj2CreatorsPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // no changes expected
    runFullSync(false);
  }
  
  public void testMembershipHistoryAddDeleteSingleIncrementalSync() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);

    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    Stem testStem1 = new StemSave().assignName("test:testStem1").save();
    Stem testStem2 = new StemSave().assignName("test:testStem2").save();

    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testGroup3.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupAdminsAttributeName());
    testGroup3.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupReadersAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemCreatorsAttributeName());

    // get the dependency table populated
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);
    
    SqlCacheGroup sqlCacheGroupTestGroup1MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup1.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup2MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup2.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup3AdminsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup3.getInternalId(), FieldFinder.find("admins", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestGroup3ReadersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup3.getInternalId(), FieldFinder.find("readers", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestStem1CreatorsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testStem1.getIdIndex(), FieldFinder.find("creators", true).getInternalId(), null);

    // this is nothing
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    
    // history for testGroup1 -> subj1
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    GrouperUtil.sleep(10);
    Membership testGroup1Subj1Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ1, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ1);

    // history for testGroup1 -> subj2
    testGroup1.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    Membership testGroup1Subj2Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ2, true);
    testGroup1.deleteMember(SubjectTestHelper.SUBJ2);
    
    // history for testGroup2 -> subj2
    testGroup2.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    Membership testGroup2Subj2Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup2, SubjectTestHelper.SUBJ2, true);
    testGroup2.deleteMember(SubjectTestHelper.SUBJ2);

    // history for testGroup2 -> subj3
    testGroup2.addMember(SubjectTestHelper.SUBJ3);
    GrouperUtil.sleep(10);
    Membership testGroup2Subj3Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup2, SubjectTestHelper.SUBJ3, true);
    testGroup2.deleteMember(SubjectTestHelper.SUBJ3);
    
    // this is nothing
    testGroup3.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    testGroup3.deleteMember(SubjectTestHelper.SUBJ2);
    
    // this is nothing
    testGroup4.addMember(SubjectTestHelper.SUBJ2);
    GrouperUtil.sleep(10);
    testGroup4.deleteMember(SubjectTestHelper.SUBJ2);
    
    // this is nothing
    testGroup1.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    GrouperUtil.sleep(10);
    testGroup1.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    // testGroup3 -> subj2 (admins)
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    GrouperUtil.sleep(10);
    Membership testGroup3Subj2AdminsMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup3, SubjectTestHelper.SUBJ2, FieldFinder.find("admins", true), true);
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    
    // testGroup3 -> subj2 (readers)
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    GrouperUtil.sleep(10);
    Membership testGroup3Subj2ReadersMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup3, SubjectTestHelper.SUBJ2, FieldFinder.find("readers", true), true);
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ);
    
    // this is nothing
    testGroup3.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    GrouperUtil.sleep(10);
    testGroup3.revokePriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE);
    
    // testStem1 -> subj2 (creators)
    testStem1.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    Membership testStem1Subj2CreatorsMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem1, SubjectTestHelper.SUBJ2, FieldFinder.find("creators", true), true);
    testStem1.revokePriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    
    // this is nothing
    testStem2.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    testStem2.revokePriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    
    ChangeLogTempToEntity.convertRecords();
    
    assertEquals(7L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
  
    Member subj1Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
    Member subj2Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
    Member subj3Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);

    PITMembership testGroup1Subj1PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj1Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup1Subj2PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj2Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup2Subj2PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup2Subj2Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup2Subj3PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup2Subj3Membership1.getImmediateMembershipId(), true);
    PITMembership testGroup3Subj2AdminsPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup3Subj2AdminsMembership1.getImmediateMembershipId(), true);
    PITMembership testGroup3Subj2ReadersPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup3Subj2ReadersMembership1.getImmediateMembershipId(), true);
    PITMembership testStem1Subj2CreatorsPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testStem1Subj2CreatorsMembership1.getImmediateMembershipId(), true);

    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj1Member.getInternalId())
        .addBindVar(testGroup1Subj1PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj1PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup1Subj2PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj2PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup2MembersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup2Subj2PITMembership1.getStartTimeDb())
        .addBindVar(testGroup2Subj2PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup2MembersField.getInternalId())
        .addBindVar(subj3Member.getInternalId())
        .addBindVar(testGroup2Subj3PITMembership1.getStartTimeDb())
        .addBindVar(testGroup2Subj3PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup3AdminsField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup3Subj2AdminsPITMembership1.getStartTimeDb())
        .addBindVar(testGroup3Subj2AdminsPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup3ReadersField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testGroup3Subj2ReadersPITMembership1.getStartTimeDb())
        .addBindVar(testGroup3Subj2ReadersPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj2Member.getInternalId())
        .addBindVar(testStem1Subj2CreatorsPITMembership1.getStartTimeDb())
        .addBindVar(testStem1Subj2CreatorsPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // no changes expected
    runFullSync(false);
  }
  
  public void testMembershipHistoryFlattenedMembershipsIncrementalSync() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);

    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Stem testStem1 = new StemSave().assignName("test:testStem1").save();
    Stem testStem2 = new StemSave().assignName("test:testStem2").save();
    AttributeDef testAttributeDef1 = new AttributeDefSave().assignName("test:testAttributeDef1").save();
    AttributeDef testAttributeDef2 = new AttributeDefSave().assignName("test:testAttributeDef2").save();
    
    Group childGroup = new GroupSave().assignName("test:childGroup").assignCreateParentStemsIfNotExist(true).save();
    testGroup1.addMember(childGroup.toSubject());
    testGroup2.addMember(childGroup.toSubject());
    testStem1.grantPriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    testStem2.grantPriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    testAttributeDef1.getPrivilegeDelegate().grantPriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);
    testAttributeDef2.getPrivilegeDelegate().grantPriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);

    Membership testGroup1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, childGroup.toSubject(), FieldFinder.find("members", true), true);
    Membership testStem1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem1, childGroup.toSubject(), FieldFinder.find("creators", true), true);
    Membership testAttributeDef1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testAttributeDef1, childGroup.toSubject(), FieldFinder.find("attrAdmins", true), true);

    GroupSet testGroup1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(testGroup1.getId(), childGroup.getId(), FieldFinder.find("members", true));
    GroupSet testStem1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(testStem1.getId(), childGroup.getId(), FieldFinder.find("creators", true));
    GroupSet testAttributeDef1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerAttrDefAndMemberGroupAndField(testAttributeDef1.getId(), childGroup.getId(), FieldFinder.find("attrAdmins", true));
    
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemCreatorsAttributeName());
    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefAdminsAttributeName());

    // get the dependency table populated
    ChangeLogTempToEntity.convertRecords();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheHistoryIncremental", false);

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Member subj0Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    Membership testGroup1Subj0Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    SqlCacheGroup sqlCacheGroupTestGroup1MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup1.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestStem1CreatorsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testStem1.getIdIndex(), FieldFinder.find("creators", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestAttributeDef1AdminsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testAttributeDef1.getIdIndex(), FieldFinder.find("attrAdmins", true).getInternalId(), null);

    // only active membership - table should be empty
    assertEquals(0L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));

    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    ChangeLogTempToEntity.convertRecords();
    PITMembership testGroup1Subj0PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership1.getImmediateMembershipId(), true);
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));

    // add active membership for testGroup1 -> subj0 again
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Membership testGroup1Subj0Membership2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // add indirect membership for testGroup1 -> subj0
    childGroup.addMember(SubjectTestHelper.SUBJ0);
    Membership childGroupSubj0Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), childGroup, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // end the immediate membership for testGroup1 -> subj0
    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // end the indirect membership
    childGroup.deleteMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    PITMembership testGroup1Subj0PITMembership2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership2.getImmediateMembershipId(), true);
    PITMembership childGroupSubj0PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(childGroupSubj0Membership1.getImmediateMembershipId(), true);
    
    // 4 membership now in the cache history
    assertEquals(4L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // add memberships again in reverse order - indirect first
    childGroup.addMember(SubjectTestHelper.SUBJ0);
    Membership childGroupSubj0Membership2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), childGroup, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Membership testGroup1Subj0Membership3 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    
    // no changes
    assertEquals(4L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // remove testGroup1 -> childGroup, testStem1 -> childGroup, testAttributeDef1 -> childGroup 
    // - this causes a change for the stem and attribute def (for subj0 and childGroup) and the actual testGroup1->childGroup
    testGroup1.deleteMember(childGroup.toSubject());
    GrouperUtil.sleep(10);
    testStem1.revokePriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    testAttributeDef1.getPrivilegeDelegate().revokePriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    PITMembership childGroupSubj0PITMembership2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(childGroupSubj0Membership2.getImmediateMembershipId(), true);
    
    PITMembership testGroup1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1ChildGroupMembership1.getImmediateMembershipId(), true);
    PITMembership testStem1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testStem1ChildGroupMembership1.getImmediateMembershipId(), true);
    PITMembership testAttributeDef1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testAttributeDef1ChildGroupMembership1.getImmediateMembershipId(), true);
    
    PITGroupSet testStem1ChildGroupPITGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(testStem1ChildGroupGroupSet1.getId(), true);
    PITGroupSet testAttributeDef1ChildGroupPITGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(testAttributeDef1ChildGroupGroupSet1.getId(), true);
        
    // 5 more rows now
    assertEquals(9L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testGroup1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testGroup1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testStem1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
  }
  
  public void testMembershipHistoryFlattenedMembershipsFullSync() {
    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Stem testStem1 = new StemSave().assignName("test:testStem1").save();
    Stem testStem2 = new StemSave().assignName("test:testStem2").save();
    AttributeDef testAttributeDef1 = new AttributeDefSave().assignName("test:testAttributeDef1").save();
    AttributeDef testAttributeDef2 = new AttributeDefSave().assignName("test:testAttributeDef2").save();
    
    Group childGroup = new GroupSave().assignName("test:childGroup").assignCreateParentStemsIfNotExist(true).save();
    testGroup1.addMember(childGroup.toSubject());
    testGroup2.addMember(childGroup.toSubject());
    testStem1.grantPriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    testStem2.grantPriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    testAttributeDef1.getPrivilegeDelegate().grantPriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);
    testAttributeDef2.getPrivilegeDelegate().grantPriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, false);

    Membership testGroup1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, childGroup.toSubject(), FieldFinder.find("members", true), true);
    Membership testStem1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem1, childGroup.toSubject(), FieldFinder.find("creators", true), true);
    Membership testAttributeDef1ChildGroupMembership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testAttributeDef1, childGroup.toSubject(), FieldFinder.find("attrAdmins", true), true);

    GroupSet testGroup1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerGroupAndMemberGroupAndField(testGroup1.getId(), childGroup.getId(), FieldFinder.find("members", true));
    GroupSet testStem1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerStemAndMemberGroupAndField(testStem1.getId(), childGroup.getId(), FieldFinder.find("creators", true));
    GroupSet testAttributeDef1ChildGroupGroupSet1 = GrouperDAOFactory.getFactory().getGroupSet().findImmediateByOwnerAttrDefAndMemberGroupAndField(testAttributeDef1.getId(), childGroup.getId(), FieldFinder.find("attrAdmins", true));
    
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemCreatorsAttributeName());
    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefAdminsAttributeName());

    runFullSync(false);

    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Member subj0Member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
    Membership testGroup1Subj0Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    runFullSync(true);
    
    SqlCacheGroup sqlCacheGroupTestGroup1MembersField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testGroup1.getInternalId(), FieldFinder.find("members", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestStem1CreatorsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testStem1.getIdIndex(), FieldFinder.find("creators", true).getInternalId(), null);
    SqlCacheGroup sqlCacheGroupTestAttributeDef1AdminsField = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(testAttributeDef1.getIdIndex(), FieldFinder.find("attrAdmins", true).getInternalId(), null);

    // only active membership - table should be empty
    assertEquals(0L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));

    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    ChangeLogTempToEntity.convertRecords();
    PITMembership testGroup1Subj0PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership1.getImmediateMembershipId(), true);
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));

    // add active membership for testGroup1 -> subj0 again
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Membership testGroup1Subj0Membership2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // add indirect membership for testGroup1 -> subj0
    childGroup.addMember(SubjectTestHelper.SUBJ0);
    Membership childGroupSubj0Membership1 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), childGroup, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // end the immediate membership for testGroup1 -> subj0
    testGroup1.deleteMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // no changes
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // end the indirect membership
    childGroup.deleteMember(SubjectTestHelper.SUBJ0);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    PITMembership testGroup1Subj0PITMembership2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1Subj0Membership2.getImmediateMembershipId(), true);
    PITMembership childGroupSubj0PITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(childGroupSubj0Membership1.getImmediateMembershipId(), true);
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // 4 membership now in the cache history
    assertEquals(4L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // add memberships again in reverse order - indirect first
    childGroup.addMember(SubjectTestHelper.SUBJ0);
    Membership childGroupSubj0Membership2 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), childGroup, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    Membership testGroup1Subj0Membership3 = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup1, SubjectTestHelper.SUBJ0, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // no changes
    assertEquals(4L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    // remove testGroup1 -> childGroup, testStem1 -> childGroup, testAttributeDef1 -> childGroup 
    // - this causes a change for the stem and attribute def (for subj0 and childGroup) and the actual testGroup1->childGroup
    testGroup1.deleteMember(childGroup.toSubject());
    GrouperUtil.sleep(10);
    testStem1.revokePriv(childGroup.toSubject(), NamingPrivilege.CREATE);
    GrouperUtil.sleep(10);
    testAttributeDef1.getPrivilegeDelegate().revokePriv(childGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN, true);
    GrouperUtil.sleep(10);
    
    ChangeLogTempToEntity.convertRecords();
    PITMembership childGroupSubj0PITMembership2 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(childGroupSubj0Membership2.getImmediateMembershipId(), true);
    
    PITMembership testGroup1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testGroup1ChildGroupMembership1.getImmediateMembershipId(), true);
    PITMembership testStem1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testStem1ChildGroupMembership1.getImmediateMembershipId(), true);
    PITMembership testAttributeDef1ChildGroupPITMembership1 = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdMostRecent(testAttributeDef1ChildGroupMembership1.getImmediateMembershipId(), true);
    
    PITGroupSet testStem1ChildGroupPITGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(testStem1ChildGroupGroupSet1.getId(), true);
    PITGroupSet testAttributeDef1ChildGroupPITGroupSet1 = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(testAttributeDef1ChildGroupGroupSet1.getId(), true);
    
    new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst").executeSql();
    runFullSync(true);
    
    // 5 more rows now
    assertEquals(9L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership1.getStartTimeDb())
        .addBindVar(testGroup1Subj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testGroup1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testGroup1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testStem1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));

    
    // test membership ending more than 2 years ago - one row less
    long twoYearsAgoMicros = (System.currentTimeMillis() - 2*365*24*60*60*1000L) * 1000L;
    GroupSet testGroup1MembersSelfGroupSet = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testGroup1.getId(), FieldFinder.find("members", true).getId());
    PITGroupSet testGroup1MembersSelfPITGroupSet = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdUnique(testGroup1MembersSelfGroupSet.getId(), true);
    testGroup1MembersSelfPITGroupSet.setStartTimeDb(twoYearsAgoMicros - 40000L);
    GrouperDAOFactory.getFactory().getPITGroupSet().saveOrUpdate(testGroup1MembersSelfPITGroupSet);
    
    testGroup1Subj0PITMembership1.setStartTimeDb(twoYearsAgoMicros - 20000L);
    testGroup1Subj0PITMembership1.setEndTimeDb(twoYearsAgoMicros);
    testGroup1Subj0PITMembership1.setActiveDb("F");
    testGroup1Subj0PITMembership1.save();
    
    runFullSync(true);
    
    assertEquals(8L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testGroup1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testGroup1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testStem1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    
    // row would get added back if start date is before 2 years ago but end date is after
    testGroup1Subj0PITMembership1.setStartTimeDb(twoYearsAgoMicros - 20000L);
    testGroup1Subj0PITMembership1.setEndTimeDb(twoYearsAgoMicros + 60*60*1000*1000L);
    testGroup1Subj0PITMembership1.setActiveDb("T");
    testGroup1Subj0PITMembership1.save();
    
    runFullSync(true);
    
    assertEquals(9L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(twoYearsAgoMicros - 20000L)
        .addBindVar(twoYearsAgoMicros + 60*60*1000*1000L)
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(testGroup1Subj0PITMembership2.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestGroup1MembersField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testGroup1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testGroup1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testStem1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    
    // delete testGroup1
    testGroup1.delete();
    

    runFullSync(true);

    assertEquals(6L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership1.getStartTimeDb())
        .addBindVar(childGroupSubj0PITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testStem1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(childGroup.toMember().getInternalId())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITMembership1.getEndTimeDb())
        .select(Long.class));
    
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestStem1CreatorsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testStem1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
    assertEquals(1L, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst where sql_cache_group_internal_id=? and member_internal_id=? and start_time=? and end_time=?")
        .addBindVar(sqlCacheGroupTestAttributeDef1AdminsField.getInternalId())
        .addBindVar(subj0Member.getInternalId())
        .addBindVar(childGroupSubj0PITMembership2.getStartTimeDb())
        .addBindVar(testAttributeDef1ChildGroupPITGroupSet1.getEndTimeDb())
        .select(Long.class));
  }

  public void testMembershipHistoryDependencyViaAttributeFullSync() {
    runFullSync(false);
    
    SqlCacheDependencyType sqlCacheDependencyTypeViaAttribute = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_viaAttribute");
    SqlCacheDependencyType sqlCacheDependencyTypeAbac = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_abac");

    
    long initialCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency").select(Long.class);
    
    Group testGroup1 = new GroupSave().assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Stem testStem1 = new StemSave().assignName("test:testStem1").save();
    Stem testStem2 = new StemSave().assignName("test:testStem2").save();
    AttributeDef testAttributeDef1 = new AttributeDefSave().assignName("test:testAttributeDef1").save();
    AttributeDef testAttributeDef2 = new AttributeDefSave().assignName("test:testAttributeDef2").save();
    
    ChangeLogTempToEntity.convertRecords();
    
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupAdminsAttributeName());
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupOptoutsAttributeName());
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupOptinsAttributeName());
    testGroup1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupReadersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupUpdatersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupViewersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupAttrReadersAttributeName());
    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupAttrUpdatersAttributeName());
    
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemCreatorsAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemAdminsAttributeName());
    testStem1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemViewersAttributeName());
    testStem2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemAttrReadersAttributeName());
    testStem2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemAttrUpdatersAttributeName());

    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefAdminsAttributeName());
    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefOptoutsAttributeName());
    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefOptinsAttributeName());
    testAttributeDef1.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefReadersAttributeName());
    testAttributeDef2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefUpdatersAttributeName());
    testAttributeDef2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefViewersAttributeName());
    testAttributeDef2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefAttrReadersAttributeName());
    testAttributeDef2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryAttributeDefAttrUpdatersAttributeName());

    runFullSync(true);
    long newCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency").select(Long.class); 
    assertEquals(initialCount + 22, newCount);
    
    runFullSync(false);
    newCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency").select(Long.class); 
    assertEquals(initialCount + 22, newCount);
    
    // change a few
    testGroup1.getAttributeDelegate().removeAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupReadersAttributeName());
    testStem1.getAttributeDelegate().removeAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemViewersAttributeName());

    testGroup2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupReadersAttributeName());
    testStem2.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryStemViewersAttributeName());
    
    runFullSync(true);
    newCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency").select(Long.class); 
    assertEquals(initialCount + 22, newCount);
    
    // let's verify all 22 - one per field
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup1.getInternalId()).addBindVar(FieldFinder.find("members", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup1.getInternalId()).addBindVar(FieldFinder.find("admins", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup1.getInternalId()).addBindVar(FieldFinder.find("optouts", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup1.getInternalId()).addBindVar(FieldFinder.find("optins", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup2.getInternalId()).addBindVar(FieldFinder.find("readers", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup2.getInternalId()).addBindVar(FieldFinder.find("updaters", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup2.getInternalId()).addBindVar(FieldFinder.find("viewers", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup2.getInternalId()).addBindVar(FieldFinder.find("groupAttrReaders", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testGroup2.getInternalId()).addBindVar(FieldFinder.find("groupAttrUpdaters", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testStem1.getIdIndex()).addBindVar(FieldFinder.find("creators", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testStem1.getIdIndex()).addBindVar(FieldFinder.find("stemAdmins", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testStem2.getIdIndex()).addBindVar(FieldFinder.find("stemViewers", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testStem2.getIdIndex()).addBindVar(FieldFinder.find("stemAttrReaders", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testStem2.getIdIndex()).addBindVar(FieldFinder.find("stemAttrUpdaters", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef1.getIdIndex()).addBindVar(FieldFinder.find("attrAdmins", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef1.getIdIndex()).addBindVar(FieldFinder.find("attrOptouts", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef1.getIdIndex()).addBindVar(FieldFinder.find("attrOptins", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef1.getIdIndex()).addBindVar(FieldFinder.find("attrReaders", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef2.getIdIndex()).addBindVar(FieldFinder.find("attrUpdaters", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef2.getIdIndex()).addBindVar(FieldFinder.find("attrViewers", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef2.getIdIndex()).addBindVar(FieldFinder.find("attrDefAttrReaders", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    {
      long sqlCacheInternalId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(testAttributeDef2.getIdIndex()).addBindVar(FieldFinder.find("attrDefAttrUpdaters", true).getInternalId()).select(Long.class);
      assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(sqlCacheInternalId).addBindVar(sqlCacheInternalId).select(Integer.class));
    }
    
    // add something else and it shouldn't be touched
    SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
    sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeAbac.getInternalId());
    sqlCacheDependency.setOwnerInternalId(999);
    sqlCacheDependency.setDependentInternalId(999);
    SqlCacheDependencyDao.store(sqlCacheDependency);
    
    runFullSync(false);
    newCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency").select(Long.class); 
    assertEquals(initialCount + 23, newCount);
  }
  
  private Hib3GrouperLoaderLog runFullSync(boolean expectChanges) {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);

    new SqlCacheHistoryFullSyncDaemon().run(otherJobInput);
    
    if (!expectChanges) {
      assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());
    }
    
    return hib3GrouperLoaderLog;
  }
}
