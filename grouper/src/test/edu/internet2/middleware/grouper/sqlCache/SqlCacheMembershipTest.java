package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.List;

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
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class SqlCacheMembershipTest extends GrouperTest {

  public SqlCacheMembershipTest(String name) {
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
    TestRunner.run(new SqlCacheMembershipTest("testCacheMembershipsFlattenedTimestampFix"));
  }

  public void testSimpleChangeLog() {
    
    GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    long millisPreAdd = System.currentTimeMillis();
    GrouperUtil.sleep(1000);
    
    testGroup.addMember(SubjectTestHelper.SUBJ0);

    GrouperUtil.sleep(1000);
    long millisPostAdd = System.currentTimeMillis();


    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperUtil.sleep(2000);

    String sqlMembershipView = "select gscmv.group_name, gscmv.list_name, gscmv.subject_source, gscmv.subject_id, gscmv.flattened_add_timestamp from grouper_sql_cache_mship_v gscmv where gscmv.group_name='test:testGroup' and gscmv.list_name='members' order by 1, 2, 3, 4";
    
    List<Object[]> groupNameFieldNameSubjectIdSourceIdInDbs = new GcDbAccess().sql(
        sqlMembershipView)
      .selectList(Object[].class);

    assertEquals(1, GrouperUtil.length(groupNameFieldNameSubjectIdSourceIdInDbs));

    assertEquals(testGroup.getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[0]);
    assertEquals(Group.getDefaultList().getName(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[1]);
    assertEquals(SubjectTestHelper.SUBJ0.getSourceId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[2]);
    assertEquals(SubjectTestHelper.SUBJ0.getId(), groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[3]);
    assertTrue(millisPreAdd < ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());
    assertTrue(millisPostAdd > ((Timestamp)groupNameFieldNameSubjectIdSourceIdInDbs.get(0)[4]).getTime());
  }
  
  public void testCacheMembershipsFlattened() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // we want to make the id index and internal id for groups out of sync to make sure we're saving the actual internal id.
    TableIndex.reserveIds(TableIndexType.groupInternalId, 10000);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(false).save();
    Stem testStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(false).save();

    Group childGroup1 = new GroupSave().assignName("test:childGroup1").assignCreateParentStemsIfNotExist(false).save();
    Group childGroup2 = new GroupSave().assignName("test:childGroup2").assignCreateParentStemsIfNotExist(false).save();
    testGroup.addMember(childGroup1.toSubject());
    testStem.grantPriv(childGroup2.toSubject(), NamingPrivilege.STEM_ADMIN, true);
    
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class);

    sleep(10);
    long time1 = System.currentTimeMillis();
    sleep(10);
    
    childGroup1.addMember(SubjectTestHelper.SUBJ1);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 2, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));
    
    sleep(10);
    long time2 = System.currentTimeMillis();
    sleep(10);
    
    testGroup.addMember(SubjectTestHelper.SUBJ1);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 2, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    sleep(10);
    long time3 = System.currentTimeMillis();
    sleep(10);
    
    testStem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, true);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 3, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    sleep(10);
    long time4 = System.currentTimeMillis();
    sleep(10);
    
    childGroup2.addMember(SubjectTestHelper.SUBJ1);
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 4, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));
    
    sleep(10);
    long time5 = System.currentTimeMillis();
    sleep(10);
    
    // clear out the membership table
    new GcDbAccess().sql("delete from grouper_sql_cache_mship").executeSql();
    assertEquals(0, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    // should add it all back
    runRullSync(true);
    assertEquals(originalSize + 4, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    String sql = "select gscm.flattened_add_timestamp from grouper_sql_cache_mship gscm, grouper_sql_cache_group gscg, grouper_fields gf where gscm.sql_cache_group_internal_id=gscg.internal_id and gscg.field_internal_id=gf.internal_id and gscg.group_internal_id = ? and gf.name = ? and gscm.member_internal_id = ?";

    long timestampStemToSubj1 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();
    long timestampChildGroup2ToSubj1 = new GcDbAccess().sql(sql).addBindVar(childGroup2.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();
    long timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();
    long timestampChildGroup1ToSubj1 = new GcDbAccess().sql(sql).addBindVar(childGroup1.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertTrue(timestampChildGroup1ToSubj1 > time1);
    assertTrue(timestampChildGroup1ToSubj1 < time2);

    assertTrue(timestampTestGroupToSubj1 > time1);
    assertTrue(timestampTestGroupToSubj1 < time2);
    
    assertTrue(timestampStemToSubj1 > time3);
    assertTrue(timestampStemToSubj1 < time4);
    
    assertTrue(timestampChildGroup2ToSubj1 > time4);
    assertTrue(timestampChildGroup2ToSubj1 < time5);
    
    testGroup.deleteMember(childGroup1.toSubject());
    testStem.revokePriv(childGroup2.toSubject(), NamingPrivilege.STEM_ADMIN, true);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 2, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    // clear out the membership table
    new GcDbAccess().sql("delete from grouper_sql_cache_mship").executeSql();
    assertEquals(0, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    // should add it all back
    runRullSync(true);
    assertEquals(originalSize + 2, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    timestampStemToSubj1 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();
    timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertTrue(timestampTestGroupToSubj1 > time1);
    assertTrue(timestampTestGroupToSubj1 < time2);
    
    assertTrue(timestampStemToSubj1 > time3);
    assertTrue(timestampStemToSubj1 < time4);
  }
  
  public void testCacheMembershipsFlattenedTimestampFix() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // we want to make the id index and internal id for groups out of sync to make sure we're saving the actual internal id.
    TableIndex.reserveIds(TableIndexType.groupInternalId, 10000);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(false).save();
    Stem testStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(false).save();

    Group childGroup1 = new GroupSave().assignName("test:childGroup1").assignCreateParentStemsIfNotExist(false).save();
    Group childGroup2 = new GroupSave().assignName("test:childGroup2").assignCreateParentStemsIfNotExist(false).save();
    Group childGroup3 = new GroupSave().assignName("test:childGroup3").assignCreateParentStemsIfNotExist(false).save();
    Group childGroup4 = new GroupSave().assignName("test:childGroup4").assignCreateParentStemsIfNotExist(false).save();
    testGroup.addMember(childGroup1.toSubject());
    testGroup.addMember(childGroup2.toSubject());
    testStem.grantPriv(childGroup3.toSubject(), NamingPrivilege.STEM_ADMIN, true);
    testStem.grantPriv(childGroup4.toSubject(), NamingPrivilege.STEM_ADMIN, true);
    
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class);

    // add some stuff that won't matter in the end
    childGroup1.addMember(SubjectTestHelper.SUBJ1);
    childGroup1.deleteMember(SubjectTestHelper.SUBJ1);
    
    childGroup3.addMember(SubjectTestHelper.SUBJ2);
    childGroup3.deleteMember(SubjectTestHelper.SUBJ2);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    sleep(1100);
    long time1 = System.currentTimeMillis();
    sleep(1100);
    
    childGroup1.addMember(SubjectTestHelper.SUBJ1);
    childGroup3.addMember(SubjectTestHelper.SUBJ2);

    sleep(1100);
    long time2 = System.currentTimeMillis();
    sleep(1100);
    
    childGroup2.addMember(SubjectTestHelper.SUBJ1);
    childGroup4.addMember(SubjectTestHelper.SUBJ2);
    
    childGroup1.deleteMember(SubjectTestHelper.SUBJ1);
    childGroup3.deleteMember(SubjectTestHelper.SUBJ2);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    assertEquals(originalSize + 4, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));
    
    runRullSync(false);

    String sql = "select gscm.flattened_add_timestamp from grouper_sql_cache_mship gscm, grouper_sql_cache_group gscg, grouper_fields gf where gscm.sql_cache_group_internal_id=gscg.internal_id and gscg.field_internal_id=gf.internal_id and gscg.group_internal_id = ? and gf.name = ? and gscm.member_internal_id = ?";

    long timestampStemToSubj2 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member2.getInternalId()).select(Timestamp.class).getTime();
    long timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertTrue(timestampStemToSubj2 > time1);
    assertTrue(timestampTestGroupToSubj1 > time1);

    assertTrue(timestampStemToSubj2 < time2);
    assertTrue(timestampTestGroupToSubj1 < time2);
    
    // now mess up the time
    new GcDbAccess().sql("update grouper_sql_cache_mship set flattened_add_timestamp = ?").addBindVar(new Timestamp(time1 - 5000)).executeSql();
    
    timestampStemToSubj2 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member2.getInternalId()).select(Timestamp.class).getTime();
    timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertFalse(timestampStemToSubj2 > time1);
    assertFalse(timestampTestGroupToSubj1 > time1);

    assertTrue(timestampStemToSubj2 < time2);
    assertTrue(timestampTestGroupToSubj1 < time2);
    
    // and have it fixed
    runRullSync(true);
    
    assertEquals(originalSize + 4, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    timestampStemToSubj2 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member2.getInternalId()).select(Timestamp.class).getTime();
    timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertTrue(timestampStemToSubj2 > time1);
    assertTrue(timestampTestGroupToSubj1 > time1);

    assertTrue(timestampStemToSubj2 < time2);
    assertTrue(timestampTestGroupToSubj1 < time2);
    
    // mess it up one more time
    new GcDbAccess().sql("update grouper_sql_cache_mship set member_internal_id = ? where member_internal_id = ?").addBindVar(member1.getInternalId()).addBindVar(member2.getInternalId()).executeSql();
    
    assertNull(new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member2.getInternalId()).select(Timestamp.class));

    // and have it fixed
    runRullSync(true);
    
    assertEquals(originalSize + 4, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    timestampStemToSubj2 = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member2.getInternalId()).select(Timestamp.class).getTime();
    timestampTestGroupToSubj1 = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member1.getInternalId()).select(Timestamp.class).getTime();

    assertTrue(timestampStemToSubj2 > time1);
    assertTrue(timestampTestGroupToSubj1 > time1);

    assertTrue(timestampStemToSubj2 < time2);
    assertTrue(timestampTestGroupToSubj1 < time2);
  }
  
  public void testCacheMemberships() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // we want to make the id index and internal id for groups out of sync to make sure we're saving the actual internal id.
    TableIndex.reserveIds(TableIndexType.groupInternalId, 10000);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    new StemSave().assignName("test").save();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // make sure nothing is out of sync to begin with
    runRullSync(true);
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(false).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(false).save();

    Group testEntity = (Group)new EntitySave(GrouperSession.staticGrouperSession()).assignName("test:testEntity").assignCreateParentStemsIfNotExist(false).save();
    Group testEntity2 = (Group)new EntitySave(GrouperSession.staticGrouperSession()).assignName("test:testEntity2").assignCreateParentStemsIfNotExist(false).save();

    Stem testStem = new StemSave().assignName("test:testStem").assignCreateParentStemsIfNotExist(false).save();

    AttributeDef testAttrDef = new AttributeDefSave().assignName("test:testAttributeDef").assignCreateParentStemsIfNotExist(false).save();
    AttributeDef testAttrDef2 = new AttributeDefSave().assignName("test:testAttributeDef2").assignCreateParentStemsIfNotExist(false).save();
    
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    Member member5 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ5, true);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    long originalSize =  new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class);

    // these don't do anything in the end
    testGroup.addMember(SubjectTestHelper.SUBJ1);
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    testGroup.addMember(SubjectTestHelper.SUBJ2);
    testGroup.deleteMember(SubjectTestHelper.SUBJ2);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testGroup.revokePriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testEntity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testEntity.revokePriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    testStem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    testStem.revokePriv(SubjectTestHelper.SUBJ2, NamingPrivilege.CREATE);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_OPTIN, true);
    testAttrDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_OPTIN, true);
    
    
    // add some random memberships to verify
    testGroup.addMember(SubjectTestHelper.SUBJ3);
    sleep(10);
    testGroup.addMember(SubjectTestHelper.SUBJ4);
    sleep(10);
    testGroup2.addMember(SubjectTestHelper.SUBJ3);
    sleep(10);
    testGroup.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, true);
    sleep(10);
    testGroup.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW, true);
    sleep(10);
    
    testEntity.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    sleep(10);
    testEntity.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
    sleep(10);
    testEntity.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.GROUP_ATTR_READ);
    sleep(10);
    testEntity.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.GROUP_ATTR_UPDATE);
    sleep(10);

    testStem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN);    
    sleep(10);
    testStem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_UPDATE);   
    sleep(10);
    
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);
    sleep(10);
    testAttrDef2.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, true);
    sleep(10);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 13, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    verifyMemberships(testGroup, testGroup2, testEntity, testEntity2, testStem, testAttrDef, testAttrDef2, true);
    
    // should be no changes
    runRullSync(false);
    
    
    
    // clear out the membership table
    new GcDbAccess().sql("delete from grouper_sql_cache_mship").executeSql();
    assertEquals(0, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    // should add it all back
    runRullSync(true);
    assertEquals(originalSize + 13, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    verifyMemberships(testGroup, testGroup2, testEntity, testEntity2, testStem, testAttrDef, testAttrDef2, true);
    
    // should be no changes
    runRullSync(false);
    
    
    
    // deletes and adds should end up the same way
    testGroup.deleteMember(SubjectTestHelper.SUBJ3);
    testGroup.addMember(SubjectTestHelper.SUBJ3);
    testEntity.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    testEntity.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    testEntity.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    testEntity.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    testAttrDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize + 13, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    verifyMemberships(testGroup, testGroup2, testEntity, testEntity2, testStem, testAttrDef, testAttrDef2, true);

    // should be no changes
    runRullSync(false);
    
    
    // add some bad memberships that need to be deleted
    new GcDbAccess().sql("update grouper_sql_cache_mship set member_internal_id = ? where member_internal_id = ?").addBindVar(member1.getInternalId()).addBindVar(member3.getInternalId()).executeSql();
    new GcDbAccess().sql("update grouper_sql_cache_mship set member_internal_id = ? where member_internal_id = ?").addBindVar(member2.getInternalId()).addBindVar(member4.getInternalId()).executeSql();
    new GcDbAccess().sql("update grouper_sql_cache_mship set member_internal_id = ? where member_internal_id = ?").addBindVar(member3.getInternalId()).addBindVar(member5.getInternalId()).executeSql();
    
    // fix
    runRullSync(true);
    
    assertEquals(originalSize + 13, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));

    verifyMemberships(testGroup, testGroup2, testEntity, testEntity2, testStem, testAttrDef, testAttrDef2, true);
    
    // should be no changes
    runRullSync(false);
    
    
    // delete everything now
    testGroup.deleteMember(SubjectTestHelper.SUBJ3);
    testGroup.deleteMember(SubjectTestHelper.SUBJ4);
    testGroup2.deleteMember(SubjectTestHelper.SUBJ3);
    testGroup.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN, true);
    testGroup.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW, true);
    testEntity.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
    testEntity.revokePriv(SubjectTestHelper.SUBJ3, AccessPrivilege.VIEW);
    testEntity.revokePriv(SubjectTestHelper.SUBJ4, AccessPrivilege.GROUP_ATTR_READ);
    testEntity.revokePriv(SubjectTestHelper.SUBJ5, AccessPrivilege.GROUP_ATTR_UPDATE);
    testStem.revokePriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN);    
    testStem.revokePriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ATTR_UPDATE);   
    testAttrDef.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN, true);
    testAttrDef2.getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, true);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    assertEquals(originalSize, (long)new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship").select(Long.class));
    
    // should be no changes
    runRullSync(false);
  }
  
  public void verifyMemberships(Group testGroup, Group testGroup2, Group testEntity, Group testEntity2, Stem testStem, AttributeDef testAttrDef, AttributeDef testAttrDef2, boolean checkTimestamp) {

    Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);
    Member member4 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ4, false);
    Member member5 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ5, false);

    String sql = "select gscm.flattened_add_timestamp from grouper_sql_cache_mship gscm, grouper_sql_cache_group gscg, grouper_fields gf where gscm.sql_cache_group_internal_id=gscg.internal_id and gscg.field_internal_id=gf.internal_id and gscg.group_internal_id = ? and gf.name = ? and gscm.member_internal_id = ?";
    
    Timestamp timestamp = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member3.getInternalId()).select(Timestamp.class);
    Membership membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup, SubjectTestHelper.SUBJ3, FieldFinder.find("members", true), true);
    PITMembership pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }
  
    timestamp = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("members").addBindVar(member4.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup, SubjectTestHelper.SUBJ4, FieldFinder.find("members", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }
    
    timestamp = new GcDbAccess().sql(sql).addBindVar(testGroup2.getInternalId()).addBindVar("members").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup2, SubjectTestHelper.SUBJ3, FieldFinder.find("members", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("admins").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup, SubjectTestHelper.SUBJ3, FieldFinder.find("admins", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testGroup.getInternalId()).addBindVar("viewers").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testGroup, SubjectTestHelper.SUBJ3, FieldFinder.find("viewers", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testEntity.getInternalId()).addBindVar("admins").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testEntity, SubjectTestHelper.SUBJ3, FieldFinder.find("admins", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testEntity.getInternalId()).addBindVar("viewers").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testEntity, SubjectTestHelper.SUBJ3, FieldFinder.find("viewers", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testEntity.getInternalId()).addBindVar("groupAttrReaders").addBindVar(member4.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testEntity, SubjectTestHelper.SUBJ4, FieldFinder.find("groupAttrReaders", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testEntity.getInternalId()).addBindVar("groupAttrUpdaters").addBindVar(member5.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testEntity, SubjectTestHelper.SUBJ5, FieldFinder.find("groupAttrUpdaters", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAdmins").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem, SubjectTestHelper.SUBJ3, FieldFinder.find("stemAdmins", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testStem.getIdIndex()).addBindVar("stemAttrUpdaters").addBindVar(member3.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testStem, SubjectTestHelper.SUBJ3, FieldFinder.find("stemAttrUpdaters", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testAttrDef.getIdIndex()).addBindVar("attrAdmins").addBindVar(member4.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testAttrDef, SubjectTestHelper.SUBJ4, FieldFinder.find("attrAdmins", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }

    timestamp = new GcDbAccess().sql(sql).addBindVar(testAttrDef2.getIdIndex()).addBindVar("attrReaders").addBindVar(member5.getInternalId()).select(Timestamp.class);
    membership = MembershipFinder.findImmediateMembership(GrouperSession.staticGrouperSession(), testAttrDef2, SubjectTestHelper.SUBJ5, FieldFinder.find("attrReaders", true), true);
    pitMembership = GrouperDAOFactory.getFactory().getPITMembership().findBySourceIdActive(membership.getImmediateMembershipId(), true);
    if (checkTimestamp) {
      assertEquals(pitMembership.getStartTime().getTime(), timestamp.getTime());
    } else {
      assertNotNull(timestamp);
    }
  }
  
  private void sleep(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      // ignore
    }
  }
  
  private Hib3GrouperLoaderLog runRullSync(boolean expectChanges) {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);

    new SqlCacheFullSyncDaemon().run(otherJobInput);
    
    if (!expectChanges) {
      assertEquals(0, hib3GrouperLoaderLog.getInsertCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getUpdateCount().intValue());
      assertEquals(0, hib3GrouperLoaderLog.getDeleteCount().intValue());
    }
    
    return hib3GrouperLoaderLog;
  }
}
