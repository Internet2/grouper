package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependency;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

public class GrouperLoaderJexlScriptFullSyncTest extends GrouperTest {


  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderJexlScriptFullSyncTest("testRecentMemberOf"));
  }

  /**
   * @param name
   */
  public GrouperLoaderJexlScriptFullSyncTest(String name) {
    super(name);
  }

  public void testJexlShouldntHaveGroup() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);
    testGroupA.addMember(testSubject3);

    testGroupB.addMember(testSubject0);

    testGroupC.addMember(testGroupA.toSubject());


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB')");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    //GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> members = testGroupE.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));

  }

  public void testJexlMemberOfAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);

    testGroupB.addMember(testSubject0);
    testGroupB.addMember(testSubject2);

    testGroupC.addMember(testSubject3);


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOfAny(['test:GroupA', 'test:GroupB'])");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    //GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> members = testGroupE.getMembers();
    assertEquals(3, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));

  }

  public void testSimpleAttributeAssignmentStringIncremental() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('org', '123')");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");


    List<SqlCacheDependencyType> sqlCacheDependencyTypes = SqlCacheDependencyTypeDao.retrieveByDependencyCategory("abac");
    Map<String, SqlCacheDependencyType> nameToSqlCacheDependencyType = new HashMap<String, SqlCacheDependencyType>();
    for (SqlCacheDependencyType sqlCacheDependencyType : sqlCacheDependencyTypes) {
      nameToSqlCacheDependencyType.put(sqlCacheDependencyType.getName(), sqlCacheDependencyType);
    }

    SqlCacheDependencyType sqlCacheDependencyTypeAbacAttribute = nameToSqlCacheDependencyType.get(SqlCacheDependencyTypeDao.NAME_ABAC_ATTRIBUTE);

    //  grouper_sql_cache_group
    //  group_internal_id
    //  field_internal_id
    MultiKey groupInternalIdFieldInternalId = new MultiKey(testGroup.getInternalId(), Group.getDefaultList().getInternalId());
    Map<MultiKey, SqlCacheGroup> groupInternalIdsFieldInternalIdToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(GrouperUtil.toSet(groupInternalIdFieldInternalId));
    SqlCacheGroup sqlCacheGroup = groupInternalIdsFieldInternalIdToSqlCacheGroup.get(groupInternalIdFieldInternalId);
    List<SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveAllByDependentId(sqlCacheGroup.getInternalId());
    Set<Long> attributeInternalIdsInDatabase = new HashSet<>();
    for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
      if (GrouperUtil.equals(sqlCacheDependencyTypeAbacAttribute.getInternalId(), sqlCacheDependency.getDependencyTypeInternalId())) {
        attributeInternalIdsInDatabase.add(sqlCacheDependency.getOwnerInternalId());
      }
    }

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    grouperDataEngine.loadFieldsAndRows(grouperConfig);

    GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.getGrouperDataProviderIndex().getFieldWrapperByLowerAlias().get("org");
    GrouperDataField grouperDataField = grouperDataFieldWrapper.getGrouperDataField();

    assertTrue(attributeInternalIdsInDatabase.contains(grouperDataField.getInternalId()));


    //GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    //GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    //  # Object Type Job class
    //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase"}
    //  changeLog.consumer.grouperObjectTypeIncremental.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
    //
    //  # {valueType: "class", readOnly: true, mustExtendClass: "edu.internet2.middleware.grouper.esb.listener.EsbListenerBase"}
    //  changeLog.consumer.grouperObjectTypeIncremental.publisher.class = edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesEsbListener
    //
    //  # object type incremental runs as change log consumer
    //  # {valueType: "cron"}
    //  changeLog.consumer.grouperObjectTypeIncremental.quartzCron = 0 * * * * ?
    //
    //  # if you want to bump up the number of change log entries for a particular consumer, you can enter that here, per change log consumer
    //  # defaults to grouper-loader.properties changeLog.changeLogConsumerBatchSize which defaults to 1000
    //  # {valueType: "integer"}
    //  changeLog.consumer.grouperObjectTypeIncremental.changeLogConsumerBatchSize =


//    hib3GrouploaderLog.setJobName("");
//    hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
//    EsbConsumer esbConsumer = new EsbConsumer();
//    ChangeLogHelper.processRecords("grouperObjectTypeIncremental", hib3GrouploaderLog, esbConsumer);
//
//
//sdf
//    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
//    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
//    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
//    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
//
//    Set<Member> members = testGroup.getMembers();
//    assertEquals(2, members.size());
//
//    assertTrue(members.contains(member0));
//    assertTrue(members.contains(member1));

  }

  public void testRecentMemberOf() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    SqlCacheDependencyType sqlCacheDependencyTypeViaAttribute = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_viaAttribute");
    SqlCacheDependencyType sqlCacheDependencyTypeAbac = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_abac");

    Group g1 = new GroupSave().assignName("test:g1").assignCreateParentStemsIfNotExist(true).save();
    Group g2 = new GroupSave().assignName("test:g2").assignCreateParentStemsIfNotExist(true).save();
    Group g3 = new GroupSave().assignName("test:g3").assignCreateParentStemsIfNotExist(true).save();
    Group g4 = new GroupSave().assignName("test:g4").assignCreateParentStemsIfNotExist(true).save();
    Group g5 = new GroupSave().assignName("test:g5").assignCreateParentStemsIfNotExist(true).save();

    Group abac1 = new GroupSave().assignName("test:abac1").assignCreateParentStemsIfNotExist(true).save();
    Group abac2 = new GroupSave().assignName("test:abac2").assignCreateParentStemsIfNotExist(true).save();

    g1.addMember(SubjectTestHelper.SUBJ0);
    g1.addMember(SubjectTestHelper.SUBJ1);
    g1.addMember(SubjectTestHelper.SUBJ2);
    g1.addMember(SubjectTestHelper.SUBJ3);
    g1.addMember(SubjectTestHelper.SUBJ4);

    g2.addMember(SubjectTestHelper.SUBJ0);
    g2.addMember(SubjectTestHelper.SUBJ1);
    g2.addMember(SubjectTestHelper.SUBJ2);
    g2.addMember(SubjectTestHelper.SUBJ3);
    g2.addMember(SubjectTestHelper.SUBJ4);

    g3.addMember(SubjectTestHelper.SUBJ0);
    g3.addMember(SubjectTestHelper.SUBJ1);
    g3.addMember(SubjectTestHelper.SUBJ2);
    g3.addMember(SubjectTestHelper.SUBJ3);
    g3.addMember(SubjectTestHelper.SUBJ4);

    g4.addMember(SubjectTestHelper.SUBJ0);
    g4.addMember(SubjectTestHelper.SUBJ1);
    g4.addMember(SubjectTestHelper.SUBJ2);
    g4.addMember(SubjectTestHelper.SUBJ3);
    g4.addMember(SubjectTestHelper.SUBJ4);

    g5.addMember(SubjectTestHelper.SUBJ5);

    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, false);
    Member member5 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ5, false);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    long g1MembersSqlCacheGroupId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(g1.getInternalId()).addBindVar(Group.getDefaultList().getInternalId()).select(Long.class);
    long g2MembersSqlCacheGroupId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(g2.getInternalId()).addBindVar(Group.getDefaultList().getInternalId()).select(Long.class);
    long g3MembersSqlCacheGroupId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(g3.getInternalId()).addBindVar(Group.getDefaultList().getInternalId()).select(Long.class);
    long g4MembersSqlCacheGroupId = new GcDbAccess().sql("select internal_id from grouper_sql_cache_group where group_internal_id = ? and field_internal_id = ?").addBindVar(g4.getInternalId()).addBindVar(Group.getDefaultList().getInternalId()).select(Long.class);

    long initialDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    long initialHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);

    // add a manual history dependency - this shouldn't conflict with the abac history dependency
    g3.getAttributeDelegate().assignAttributeByName(SqlCacheGroup.sqlCacheableHistoryGroupMembersAttributeName());

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign1 = new AttributeAssignSave(grouperSession).assignOwnerGroup(abac1).assignAttributeDefName(attributeDefNameMarker).save();
    attributeAssign1.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.recentMemberOf('test:g1', '2 days') || entity.recentMemberOf('test:g2', '2 hours')");

    AttributeAssign attributeAssign2 = new AttributeAssignSave(grouperSession).assignOwnerGroup(abac2).assignAttributeDefName(attributeDefNameMarker).save();
    attributeAssign2.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.recentMemberOf('test:g3', '2 days') || entity.recentMemberOf('test:g4', '2 hours') || entity.memberOf('test:g5')");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheFullSync");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheHistoryFullSync");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    assertEquals(0, abac1.getMembers().size());
    assertEquals(1, abac2.getMembers().size());

    g1.deleteMember(SubjectTestHelper.SUBJ1);
    g1.deleteMember(SubjectTestHelper.SUBJ2);
    g2.deleteMember(SubjectTestHelper.SUBJ3);
    g2.deleteMember(SubjectTestHelper.SUBJ4);

    g3.deleteMember(SubjectTestHelper.SUBJ1);
    g3.deleteMember(SubjectTestHelper.SUBJ2);
    g4.deleteMember(SubjectTestHelper.SUBJ3);
    g4.deleteMember(SubjectTestHelper.SUBJ4);

    // some memberships get added, deleted and re-added and should make no difference since recent member of doesn't include current members
    g1.addMember(SubjectTestHelper.SUBJ5);
    g3.addMember(SubjectTestHelper.SUBJ5);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    g1.deleteMember(SubjectTestHelper.SUBJ5);
    g3.deleteMember(SubjectTestHelper.SUBJ5);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    g1.addMember(SubjectTestHelper.SUBJ5);
    g3.addMember(SubjectTestHelper.SUBJ5);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    {
      Set<Member> members = abac1.getMembers();
      assertEquals(4, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
    }

    {
      Set<Member> members = abac2.getMembers();
      assertEquals(5, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
      assertTrue(members.contains(member5));
    }

    // check expected counts
    long newDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    long newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialDependencyCount + 5, newDependencyCount);
    assertEquals(initialHistoryCount + 10, newHistoryCount);

    // check dependencies
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(g3MembersSqlCacheGroupId).addBindVar(g3MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g1MembersSqlCacheGroupId).addBindVar(g1MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g2MembersSqlCacheGroupId).addBindVar(g2MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g3MembersSqlCacheGroupId).addBindVar(g3MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g4MembersSqlCacheGroupId).addBindVar(g4MembersSqlCacheGroupId).select(Integer.class));

    // fake some of the end dates to be before and after the time period in the jexl to test that
    new GcDbAccess().sql("update grouper_sql_cache_mship_hst set end_time = ? where sql_cache_group_internal_id = ? and member_internal_id = ?").addBindVar(System.currentTimeMillis() * 1000 - 49L * 60 * 60 * 1000 * 1000).addBindVar(g1MembersSqlCacheGroupId).addBindVar(member1.getInternalId()).executeSql();
    new GcDbAccess().sql("update grouper_sql_cache_mship_hst set end_time = ? where sql_cache_group_internal_id = ? and member_internal_id = ?").addBindVar(System.currentTimeMillis() * 1000 - 47L * 60 * 60 * 1000 * 1000).addBindVar(g1MembersSqlCacheGroupId).addBindVar(member2.getInternalId()).executeSql();
    new GcDbAccess().sql("update grouper_sql_cache_mship_hst set end_time = ? where sql_cache_group_internal_id = ? and member_internal_id = ?").addBindVar(System.currentTimeMillis() * 1000 - 121L * 60 * 1000 * 1000).addBindVar(g2MembersSqlCacheGroupId).addBindVar(member3.getInternalId()).executeSql();
    new GcDbAccess().sql("update grouper_sql_cache_mship_hst set end_time = ? where sql_cache_group_internal_id = ? and member_internal_id = ?").addBindVar(System.currentTimeMillis() * 1000 - 119L * 60 * 1000 * 1000).addBindVar(g2MembersSqlCacheGroupId).addBindVar(member4.getInternalId()).executeSql();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    {
      Set<Member> members = abac1.getMembers();
      assertEquals(2, members.size());
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member4));
    }

    {
      Set<Member> members = abac2.getMembers();
      assertEquals(5, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
      assertTrue(members.contains(member5));
    }

    // check expected counts
    newDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialDependencyCount + 5, newDependencyCount);
    assertEquals(initialHistoryCount + 10, newHistoryCount);

    // test dependency cleanup - delete first abac
    attributeAssign1.delete();

    // there's a 2 hour period where the dependency isn't cleaned up
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    {
      Set<Member> members = abac1.getMembers();
      assertEquals(2, members.size());
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member4));
    }

    {
      Set<Member> members = abac2.getMembers();
      assertEquals(5, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
      assertTrue(members.contains(member5));
    }

    newDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialDependencyCount + 5, newDependencyCount);
    assertEquals(initialHistoryCount + 10, newHistoryCount);

    // update the dependency timestamps and check again
    new GcDbAccess().sql("update grouper_sql_cache_dependency set created_on = ?").addBindVar(System.currentTimeMillis() * 1000 - 3L * 60 * 60 * 1000 * 1000).executeSql();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    {
      Set<Member> members = abac1.getMembers();
      assertEquals(2, members.size());
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member4));
    }

    {
      Set<Member> members = abac2.getMembers();
      assertEquals(5, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
      assertTrue(members.contains(member5));
    }

    newDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialDependencyCount + 3, newDependencyCount);
    assertEquals(initialHistoryCount + 10, newHistoryCount);
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(g3MembersSqlCacheGroupId).addBindVar(g3MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g3MembersSqlCacheGroupId).addBindVar(g3MembersSqlCacheGroupId).select(Integer.class));
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeAbac.getInternalId()).addBindVar(g4MembersSqlCacheGroupId).addBindVar(g4MembersSqlCacheGroupId).select(Integer.class));

    // history full sync will clean up the history
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheHistoryFullSync");
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialHistoryCount + 5, newHistoryCount);

    // delete the other abac
    attributeAssign2.delete();

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    {
      Set<Member> members = abac1.getMembers();
      assertEquals(2, members.size());
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member4));
    }

    {
      Set<Member> members = abac2.getMembers();
      assertEquals(5, members.size());
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
      assertTrue(members.contains(member5));
    }

    newDependencyCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and dependency_category='mshipHistory'").select(Long.class);
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialDependencyCount + 1, newDependencyCount);
    assertEquals(initialHistoryCount + 5, newHistoryCount);
    assertEquals(1, (int)new GcDbAccess().sql("select count(*) from grouper_sql_cache_dependency where dep_type_internal_id = ? and owner_internal_id = ? and dependent_internal_id = ?").addBindVar(sqlCacheDependencyTypeViaAttribute.getInternalId()).addBindVar(g3MembersSqlCacheGroupId).addBindVar(g3MembersSqlCacheGroupId).select(Integer.class));

    // history full sync will clean up the history
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheHistoryFullSync");
    newHistoryCount = new GcDbAccess().sql("select count(*) from grouper_sql_cache_mship_hst").select(Long.class);
    assertEquals(initialHistoryCount + 3, newHistoryCount);
  }

  public void testJexlFullDependencies() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);
    testGroupA.addMember(testSubject3);

    testGroupB.addMember(testSubject0);

    testGroupC.addMember(testGroupA.toSubject());


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    // testE has the jexl script
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    // testGroupC: 0, 1, 2, 3
    // testGroupB is 0
    // testgrouper_field_row_affil
    // affiliation less than 300: 0, 3
    // staff: 0, 2
    // job number 123, 234, nobody
    // org with %2%: nobody
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
        """
        (entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB'))
        || entity.hasRow('affiliation', 'affiliationDeptNumber<300')
        || entity.hasRow('affiliation', 'affiliationCode==staff')
        || entity.hasAttributeAny('jobNumber', [123, 234])
        || entity.hasAttributeLike(org, '%2%')
        """);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");

    //GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> members = testGroupE.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));


    // query to check the group dependencies
    Set<String> ownerGroupNames = new HashSet<>(new GcDbAccess().sql("select owner_group_name from grouper_sql_dependency_group_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(2, ownerGroupNames.size());
    assertTrue(ownerGroupNames.contains(testGroupC.getName()));
    assertTrue(ownerGroupNames.contains(testGroupB.getName()));

    // query to check the attribute dependencies
    Set<String> ownerAttributeNames = new HashSet<>(new GcDbAccess().sql("select owner_data_field_config_id from grouper_sql_dependency_attr_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(4, ownerAttributeNames.size());
    assertTrue(ownerAttributeNames.contains("org"));
    assertTrue(ownerAttributeNames.contains("affiliationDeptNumber"));
    assertTrue(ownerAttributeNames.contains("affiliationCode"));
    assertTrue(ownerAttributeNames.contains("jobNumber"));

    // query to check the row dependencies
    Set<String> ownerRowNames = new HashSet<>(new GcDbAccess().sql("select owner_data_row_config_id from grouper_sql_dependency_row_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(1, ownerRowNames.size());
    assertTrue(ownerRowNames.contains("affiliation"));

  }

  public void testJexlIncrementalDependencies() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");


    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);
    testGroupA.addMember(testSubject3);

    testGroupB.addMember(testSubject0);

    testGroupC.addMember(testGroupA.toSubject());


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    // testE has the jexl script
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    // testGroupC: 0, 1, 2, 3
    // testGroupB is 0
    // testgrouper_field_row_affil
    // affiliation less than 300: 0, 3
    // staff: 0, 2
    // job number 123, 234, nobody
    // org with %2%: nobody
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
        """
        (entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB'))
        || entity.hasRow('affiliation', 'affiliationDeptNumber<300')
        || entity.hasRow('affiliation', 'affiliationCode==staff')
        || entity.hasAttributeAny('jobNumber', [123, 234])
        || entity.hasAttributeLike(org, '%2%')
        """);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    // TODO check status of the incremental job

    Set<Member> members = testGroupE.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));


    // query to check the group dependencies
    Set<String> ownerGroupNames = new HashSet<>(new GcDbAccess().sql("select owner_group_name from grouper_sql_dependency_group_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(2, ownerGroupNames.size());
    assertTrue(ownerGroupNames.contains(testGroupC.getName()));
    assertTrue(ownerGroupNames.contains(testGroupB.getName()));

    // query to check the attribute dependencies
    Set<String> ownerAttributeNames = new HashSet<>(new GcDbAccess().sql("select owner_data_field_config_id from grouper_sql_dependency_attr_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(4, ownerAttributeNames.size());
    assertTrue(ownerAttributeNames.contains("org"));
    assertTrue(ownerAttributeNames.contains("affiliationDeptNumber"));
    assertTrue(ownerAttributeNames.contains("affiliationCode"));
    assertTrue(ownerAttributeNames.contains("jobNumber"));

    // query to check the row dependencies
    Set<String> ownerRowNames = new HashSet<>(new GcDbAccess().sql("select owner_data_row_config_id from grouper_sql_dependency_row_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertEquals(1, ownerRowNames.size());
    assertTrue(ownerRowNames.contains("affiliation"));

  }

  public void testJexlIncrementalChangeAttribute() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");


    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Subject testSubject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Member member4 = MemberFinder.findBySubject(grouperSession, testSubject4, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);
    testGroupA.addMember(testSubject3);

    testGroupB.addMember(testSubject0);

    testGroupC.addMember(testGroupA.toSubject());


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    // testE has the jexl script
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    // testGroupC: 0, 1, 2, 3
    // testGroupB is 0
    // testgrouper_field_row_affil
    // affiliation less than 300: 0, 3
    // staff: 0, 2
    // job number 123, 234, nobody
    // org with %2%: nobody
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
        """
        (entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB'))
        || entity.hasRow('affiliation', 'affiliationDeptNumber<300')
        || entity.hasRow('affiliation', 'affiliationCode==staff')
        || entity.hasAttributeAny('jobNumber', [123, 234])
        || entity.hasAttributeLike(org, '%2%')
        """);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    // TODO check status of the incremental job

    Set<Member> members = testGroupE.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));


    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();

    batchBindVars.clear();

    batchBindVars.add(GrouperUtil.toList("test.subject.4", "staff", "T", "phys", 123));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org, dept_number) values (?, ?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    members = testGroupE.getMembers();
    assertEquals(5, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    assertTrue(members.contains(member4));


  }

  public void testJexlIncrementalChangeGroup() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");


    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Subject testSubject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Member member4 = MemberFinder.findBySubject(grouperSession, testSubject4, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);
    testGroupA.addMember(testSubject3);

    testGroupB.addMember(testSubject0);

    testGroupC.addMember(testGroupA.toSubject());


    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    // testE has the jexl script

    List<Group> groups = new ArrayList<Group>();
    for (int i=1; i<=30; i++) {
      Group testGroup = new GroupSave().assignName("test:GroupE_"+i).assignCreateParentStemsIfNotExist(true).save();
      groups.add(testGroup);
    }

    for (Group group: groups) {

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
          .assignAttributeDefName(attributeDefNameMarker).save();

      // testGroupC: 0, 1, 2, 3
      // testGroupB is 0
      // testgrouper_field_row_affil
      // affiliation less than 300: 0, 3
      // staff: 0, 2
      // job number 123, 234, nobody
      // org with %2%: nobody
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
          """
          (entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB'))
          || entity.hasRow('affiliation', 'affiliationDeptNumber<300')
          || entity.hasRow('affiliation', 'affiliationCode==staff')
          || entity.hasAttributeAny('jobNumber', [123, 234])
          || entity.hasAttributeLike(org, '%2%')
          """);
    }



    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    // TODO check status of the incremental job

    for (Group group: groups) {
      Set<Member> members = group.getMembers();
      assertEquals(4, members.size());

      assertTrue(members.contains(member0));
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
    }


    testGroupC.addMember(testSubject4);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

    for (Group group: groups) {
      Set<Member> members = group.getMembers();
      assertEquals(5, members.size());

      assertTrue(members.contains(member0));
      assertTrue(members.contains(member1));
      assertTrue(members.contains(member2));
      assertTrue(members.contains(member3));
      assertTrue(members.contains(member4));
    }

  }

  /**
   * test that default subject source config (blank) includes jdbc source subjects
   */
  public void testSubjectSourceDefaultConfig() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> members = testGroupE.getMembers();
    assertEquals(2, members.size());
  }

  /**
   * test that configuring specific global default source IDs works
   */
  public void testSubjectSourceConfiguredGlobalDefaults() {
    GrouperAbacTestHelper.setupDataFields();

    // configure global defaults to only include "jdbc"
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("jdbc").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      testGroupA.addMember(testSubject0);

      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");

      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      Set<Member> members = testGroupE.getMembers();
      assertEquals(1, members.size());
    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test that configuring a nonexistent source excludes all subjects
   */
  public void testSubjectSourceConfiguredNonexistentSource() {
    GrouperAbacTestHelper.setupDataFields();

    // configure global defaults to a source that doesn't exist
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("nonexistentSource").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      testGroupA.addMember(testSubject0);

      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");

      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      Set<Member> members = testGroupE.getMembers();
      assertEquals(0, members.size());
    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test per-group override with allowUserOverride enabled
   */
  public void testSubjectSourcePerGroupOverride() {
    GrouperAbacTestHelper.setupDataFields();

    // enable override and set available sources
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc, grouperEntities").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      testGroupA.addMember(testSubject0);

      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
      AttributeDefName attributeDefNameSourceIds = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptSubjectSourceIds", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");
      // set per-group to jdbc only
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameSourceIds.getName(), "jdbc");

      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      Set<Member> members = testGroupE.getMembers();
      assertEquals(1, members.size());
    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test that per-group override is ignored when allowUserOverride is false
   */
  public void testSubjectSourceOverrideDisabled() {
    GrouperAbacTestHelper.setupDataFields();

    // override disabled, but per-group attribute set to nonexistent source
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      testGroupA.addMember(testSubject0);

      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
      AttributeDefName attributeDefNameSourceIds = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptSubjectSourceIds", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");
      // set per-group to nonexistent source - should be ignored since override is disabled
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameSourceIds.getName(), "nonexistentSource");

      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      // should still find the member because override is disabled, global defaults used
      Set<Member> members = testGroupE.getMembers();
      assertEquals(1, members.size());
    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test that g:gsa and g:isa are always excluded even if configured
   */
  public void testSubjectSourceInternalAlwaysExcluded() {
    GrouperAbac.clearCaches();

    // test effectiveSubjectSourceIds directly
    Set<String> result = GrouperAbac.effectiveSubjectSourceIds("jdbc, g:gsa, g:isa");
    assertTrue(result.contains("jdbc"));
    assertFalse(result.contains("g:gsa"));
    assertFalse(result.contains("g:isa"));

    // test subjectSourceInClause
    Set<String> sourceIds = new LinkedHashSet<String>();
    sourceIds.add("jdbc");
    sourceIds.add("g:gsa");
    sourceIds.add("g:isa");
    MultiKey inClause = GrouperAbac.subjectSourceInClause(sourceIds);
    String sql = (String)inClause.getKey(0);
    List<String> bindVars = (List<String>)inClause.getKey(1);
    assertEquals("gm.subject_source in (?)", sql);
    assertEquals(1, bindVars.size());
    assertEquals("jdbc", bindVars.get(0));
  }

  /**
   * test the showSubjectSourcePicker logic
   */
  public void testShowSubjectSourcePicker() {
    // default: override false, available blank -> no picker
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
    GrouperAbac.clearCaches();
    assertFalse(GrouperAbac.showSubjectSourcePicker());

    // override true but available blank -> no picker
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("true").store();
    GrouperAbac.clearCaches();
    assertFalse(GrouperAbac.showSubjectSourcePicker());

    // override true, available has 1 -> no picker
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc").store();
    GrouperAbac.clearCaches();
    assertFalse(GrouperAbac.showSubjectSourcePicker());

    // override true, available has 2 -> show picker
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc, other").store();
    GrouperAbac.clearCaches();
    assertTrue(GrouperAbac.showSubjectSourcePicker());

    // cleanup
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
    GrouperAbac.clearCaches();
  }

  /**
   * test that ABAC scripts work with both jdbc and grouperEntities subject sources.
   * global default is jdbc only, available is jdbc + grouperEntities, and the per-group override
   * configures both sources so that entities are also evaluated.
   */
  public void testSubjectSourceMultipleSourcesWithEntities() {
    GrouperAbacTestHelper.setupDataFields();

    // global default is jdbc only
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("jdbc").store();
    // available for override: jdbc and grouperEntities
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc, grouperEntities").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      // create a group that both jdbc subjects and entities will be members of
      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      // the ABAC result group
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      // add jdbc subjects to GroupA
      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
      testGroupA.addMember(testSubject0);
      testGroupA.addMember(testSubject1);

      // create local entities in a folder and add them to GroupA
      new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:entities:serviceAccountA").save();
      new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:entities:serviceAccountB").save();
      // a third entity NOT in GroupA (should not end up in result)
      new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:entities:serviceAccountC").save();

      Subject entitySubjectA = SubjectFinder.findByIdentifierAndSource("test:entities:serviceAccountA", "grouperEntities", true);
      Subject entitySubjectB = SubjectFinder.findByIdentifierAndSource("test:entities:serviceAccountB", "grouperEntities", true);
      Subject entitySubjectC = SubjectFinder.findByIdentifierAndSource("test:entities:serviceAccountC", "grouperEntities", true);

      // ensure entities are in grouper_members
      MemberFinder.findBySubject(grouperSession, entitySubjectA, true);
      MemberFinder.findBySubject(grouperSession, entitySubjectB, true);
      MemberFinder.findBySubject(grouperSession, entitySubjectC, true);

      testGroupA.addMember(entitySubjectA);
      testGroupA.addMember(entitySubjectB);

      // set up the ABAC script on GroupE
      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
      AttributeDefName attributeDefNameSourceIds = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptSubjectSourceIds", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");
      // per-group override: both jdbc and grouperEntities
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameSourceIds.getName(), "jdbc, grouperEntities");

      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      // should have 4 members: test.subject.0, test.subject.1, serviceAccountA, serviceAccountB
      Set<Member> members = testGroupE.getMembers();
      assertEquals(4, members.size());

      // verify both source types are present
      Set<String> sourceIds = new HashSet<String>();
      for (Member member : members) {
        sourceIds.add(member.getSubjectSourceId());
      }
      assertTrue("should contain jdbc source", sourceIds.contains("jdbc"));
      assertTrue("should contain grouperEntities source", sourceIds.contains("grouperEntities"));

      // now test with only the default (jdbc) -- entities should NOT be in the result
      // remove the per-group override
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameSourceIds.getName(), "");
      GrouperAbac.clearCaches();

      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      members = testGroupE.getMembers();
      assertEquals("only jdbc subjects should remain when using defaults", 2, members.size());
      for (Member member : members) {
        assertEquals("jdbc", member.getSubjectSourceId());
      }

    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test that per-group override source IDs are filtered against the available list.
   * if someone puts a forbidden source in the override attribute, it should be ignored.
   */
  public void testSubjectSourceOverrideForbiddenSourceFiltered() {
    GrouperAbacTestHelper.setupDataFields();

    // global default is jdbc
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("jdbc").store();
    // available for override: only jdbc (grouperEntities is NOT available)
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc, grouperEntities").store();
    GrouperAbac.clearCaches();

    try {
      // test effectiveSubjectSourceIds directly: forbidden source should be filtered out
      Set<String> result = GrouperAbac.effectiveSubjectSourceIds("jdbc, notAllowedSource");
      assertTrue("should contain jdbc", result.contains("jdbc"));
      assertFalse("should NOT contain notAllowedSource", result.contains("notAllowedSource"));
      assertEquals(1, result.size());

      // test that if ALL per-group sources are forbidden, falls back to global defaults
      result = GrouperAbac.effectiveSubjectSourceIds("notAllowedSource, anotherBadSource");
      assertTrue("should fall back to global defaults and contain jdbc", result.contains("jdbc"));
      assertFalse("should NOT contain notAllowedSource", result.contains("notAllowedSource"));
      assertFalse("should NOT contain anotherBadSource", result.contains("anotherBadSource"));

    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * test that changing the subject source IDs attribute on an ABAC group triggers
   * the incremental daemon to recalculate membership.
   * starts with global default (jdbc only), does a full sync, then changes the per-group
   * source IDs to include grouperEntities and verifies the incremental picks it up.
   */
  public void testIncrementalSubjectSourceChange() {
    GrouperAbacTestHelper.setupDataFields();

    // global default is jdbc only
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("jdbc, grouperEntities").store();
    GrouperAbac.clearCaches();

    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();

      // prime the incremental consumer
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

      Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
      Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

      // add jdbc subjects to GroupA
      Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
      Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
      testGroupA.addMember(testSubject0);
      testGroupA.addMember(testSubject1);

      // create an entity and add it to GroupA
      new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:entities:serviceAccountA").save();
      Subject entitySubjectA = SubjectFinder.findByIdentifierAndSource("test:entities:serviceAccountA", "grouperEntities", true);
      MemberFinder.findBySubject(grouperSession, entitySubjectA, true);
      testGroupA.addMember(entitySubjectA);

      // set up the ABAC script on GroupE -- no per-group source override yet (uses global default = jdbc)
      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
      AttributeDefName attributeDefNameSourceIds = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptSubjectSourceIds", true);

      AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
          .assignAttributeDefName(attributeDefNameMarker).save();

      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('test:GroupA')");

      // full sync with default sources (jdbc only) -- should get 2 members
      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

      Set<Member> members = testGroupE.getMembers();
      assertEquals("should have 2 jdbc members with default sources", 2, members.size());

      // now change the per-group source IDs to include grouperEntities
      attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameSourceIds.getName(), "jdbc, grouperEntities");

      // process the attribute change through incremental
      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_grouperLoaderJexlScriptIncremental");

      // should now have 3 members (2 jdbc + 1 entity)
      members = testGroupE.getMembers();
      assertEquals("incremental should recalculate after source IDs change", 3, members.size());

      Set<String> sourceIds = new HashSet<String>();
      for (Member member : members) {
        sourceIds.add(member.getSubjectSourceId());
      }
      assertTrue("should contain jdbc source", sourceIds.contains("jdbc"));
      assertTrue("should contain grouperEntities source", sourceIds.contains("grouperEntities"));

    } finally {
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.globalDefaultSubjectSourceIds").value("").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.allowUserOverrideSubjectSourceIds").value("false").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.abac.availableSubjectSourceIds").value("").store();
      GrouperAbac.clearCaches();
    }
  }

  /**
   * Builds a scripted group with a multi-row, parenthesized JEXL script that mirrors
   * the shape of a complicated production ABAC policy: a 3-way top-level OR, where one
   * branch is itself an OR of two hasRow leaves, plus one hasRow leaf with a nested
   * inner OR. Each hasRow has multiple AND'd column predicates including =~ array
   * matching. Intended primarily for viewing in the Visualization tab against a dev
   * UI — assertions are intentionally minimal because the test data only populates
   * the 'affiliation' row.
   */
  /**
   * Builds test:GroupE as a scripted group with a policy that exercises every visualization
   * shape we want to see: nested OR-of-hasRow, a hasRow whose predicate contains a parens-OR,
   * an included group reference, and an excluded NOT-group reference.
   *
   * <p>Test data:
   * <ul>
   *   <li>test:GroupA members: test.subject.0, test.subject.1, test.subject.2</li>
   *   <li>test:GroupB members: test.subject.3</li>
   *   <li>No affiliation row data is loaded, so all three hasRow branches resolve to population 0.</li>
   * </ul>
   *
   * <p>Resulting GroupE membership after full sync:
   * <ul>
   *   <li>test.subject.0: MEMBER (in GroupA, not in GroupB)</li>
   *   <li>test.subject.1: MEMBER (in GroupA, not in GroupB)</li>
   *   <li>test.subject.2: MEMBER (in GroupA, not in GroupB)</li>
   *   <li>test.subject.3: NOT a member (excluded by !memberOf GroupB)</li>
   * </ul>
   *
   * <p>Visualization expectations when opening test:GroupE:
   * <ul>
   *   <li>GroupE renders as a 3-member scripted-group start node.</li>
   *   <li>The top-level AND flattens, so two edges leave GroupE:
   *     <ul>
   *       <li>a (+) "must be in" edge to the (hasRow-X OR hasRow-Y OR hasRow-Z OR GroupA)
   *           OR-compound ellipse with population 3,</li>
   *       <li>a (-) dashed "must not be in" edge directly to the GroupB excluded-group
   *           node with population 1.</li>
   *     </ul>
   *   </li>
   *   <li>The OR-compound has 4 children connected by (+or) edges:
   *     <ul>
   *       <li>hasRow-X box (affiliationCode IN ['staff','faculty','student','temp'] AND
   *           affiliationDeptNumber==200 AND affiliationActive) with three inner per-attribute
   *           AND-children (each population 0).</li>
   *       <li>hasRow-Y box (affiliationCodePrimary IN ['staff','faculty'] AND
   *           affiliationDeptNumberPrimary==200 AND affiliationActive) with three inner
   *           per-attribute children.</li>
   *       <li>hasRow-Z box (affiliationCode=='staff' AND affiliationActive AND
   *           (affiliationDeptNumber==100 OR affiliationDeptNumberPrimary==100)) with three
   *           inner AND-children — the third is itself an OR-compound ellipse with two leaves.</li>
   *       <li>GroupA required-group node (population 3) connected via the (+or) "any-of-these" edge.</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <p>Visualization when filtering by subject on test:GroupE:
   * <ul>
   *   <li>subject.0 / subject.1 / subject.2: GroupA highlights "is member"; GroupB highlights
   *       "is not member"; every hasRow attribute leaf highlights "is not member"; the
   *       OR-compound and GroupE both highlight "is member".</li>
   *   <li>subject.3: GroupA highlights "is not member"; GroupB highlights "is member"; GroupE
   *       highlights "is not member" — the !GroupB branch excludes them.</li>
   * </ul>
   */
  public void testVisualizationComplexHasRow() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);

    // GroupA: subjects 0, 1, 2 (the included group in the policy)
    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);

    // GroupB: subject 3 (the excluded / NOT group in the policy)
    testGroupB.addMember(testSubject3);

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
        """
        (
            (
                entity.hasRow('affiliation', "
                    affiliationCode =~ [
                        'staff',
                        'faculty',
                        'student',
                        'temp'
                    ]
                    && affiliationDeptNumber == 200
                    && affiliationActive
                ")

                ||

                entity.hasRow('affiliation', "
                    affiliationCodePrimary =~ [
                        'staff',
                        'faculty'
                    ]
                    && affiliationDeptNumberPrimary == 200
                    && affiliationActive
                ")
            )
            ||
            entity.hasRow('affiliation', "
                affiliationCode == 'staff'
                && affiliationActive
                && (
                    affiliationDeptNumber == 100
                    ||
                    affiliationDeptNumberPrimary == 100
                )
            ")
            ||
            entity.memberOf('test:GroupA')
        )
        && !entity.memberOf('test:GroupB')
        """);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // No affiliation row data is loaded, so the hasRow branches contribute zero. Membership
    // reduces to: (member of GroupA) AND NOT (member of GroupB).
    //   GroupA = {0,1,2}, GroupB = {3}, so GroupE = {0,1,2}.
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroupE.getMembers();
    assertEquals(3, members.size());
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertFalse(members.contains(member3));
  }

  /**
   * Verifies that the visualization tree exposes per-attribute sub-checks beneath a
   * hasRow leaf — i.e. the inner predicate AST is parsed, registered in
   * astNodeToPart, and recursed into by buildAbacReferenceFromAst.
   */
  public void testVisualizationHasRowExposesInnerAttributes() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    String script = "entity.hasRow('affiliation', \"affiliationCode == 'staff' and affiliationActive\")";

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertNotNull("expected visualization references to be populated", refs);
    assertEquals("expected a single top-level row reference", 1, refs.size());

    AbacReference row = refs.get(0);
    assertEquals(AbacReference.RefType.ROW, row.getRefType());

    List<AbacReference> children = row.getChildren();
    assertNotNull("hasRow leaf should have inner-attribute children", children);
    assertEquals("expected two AND'd inner-attribute children", 2, children.size());
  }

  /**
   * Same as the previous test but with the more complex shape we actually want to render:
   * an AND that contains a parenthesized OR. Asserts the row leaf has 3 children
   * (attr, attr, OR-compound) and the OR-compound itself has 2 children.
   */
  public void testVisualizationHasRowExposesInnerAttributesWithNestedOr() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    String script = "entity.hasRow('affiliation', \"affiliationCode == 'staff' and affiliationActive and (affiliationDeptNumber == 100 or affiliationDeptNumberPrimary == 100)\")";

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals(1, refs.size());

    AbacReference row = refs.get(0);
    assertEquals(AbacReference.RefType.ROW, row.getRefType());

    List<AbacReference> children = row.getChildren();
    assertNotNull(children);
    assertEquals("expected 3 AND'd children (attr, attr, OR-group)", 3, children.size());

    // The third child should be the OR-compound with 2 row-attribute leaves
    AbacReference last = children.get(2);
    assertEquals("third child should be the OR compound",
        AbacReference.RefType.COMPOUND, last.getRefType());
    assertNotNull(last.getChildren());
    assertEquals("OR compound should have 2 children", 2, last.getChildren().size());

    // Each leaf child must have a distinct, non-empty displayDescription so the
    // RelationGraph computeId produces unique node IDs (otherwise fetchOrCreateNode
    // dedupes them onto the same GraphNode and only one edge renders).
    for (int i = 0; i < 2; i++) {
      AbacReference leaf = children.get(i);
      assertNotNull("leaf " + i + " should have a displayDescription",
          leaf.getDisplayDescription());
      assertTrue("leaf " + i + " displayDescription should be non-empty",
          leaf.getDisplayDescription().length() > 0);
    }
    for (int i = 0; i < last.getChildren().size(); i++) {
      AbacReference innerLeaf = last.getChildren().get(i);
      assertNotNull("inner leaf " + i + " should have a displayDescription",
          innerLeaf.getDisplayDescription());
    }
    // Computed IDs must all differ
    java.util.Set<String> ids = new java.util.HashSet<String>();
    for (AbacReference c : children) {
      ids.add(c.computeId());
    }
    assertEquals("all top-level child IDs should be distinct", children.size(), ids.size());
  }

  /**
   * Reproduces the live-visualization bug: with an outer OR wrapping two hasRow calls,
   * the per-attribute inner clones of each hasRow must have parentPart pointing at THEIR
   * hasRow leaf, not at the top-level OR root. Previously the inner AST root was bridged
   * to whatever the accumulator was during the first recurse (= root), so parent walks
   * resolved to root and the per-attribute clones appeared as direct children of the
   * ABAC group instead of nested under their hasRow.
   */
  public void testVisualizationOuterOrEachHasRowKeepsItsInnerChildren() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    String script = "entity.hasRow('affiliation', \"affiliationCode == 'staff' and affiliationActive\")"
        + " || entity.hasRow('affiliation', \"affiliationCodePrimary == 'staff' and affiliationActive\")";

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, script, testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    // Top-level compound is flattened, so we get the two ROW hasRow leaves directly.
    assertEquals("expected 2 top-level hasRow leaves after flattening the outer OR", 2, refs.size());

    for (int i = 0; i < refs.size(); i++) {
      AbacReference row = refs.get(i);
      assertEquals("ref " + i + " should be ROW", AbacReference.RefType.ROW, row.getRefType());
      List<AbacReference> rowChildren = row.getChildren();
      assertNotNull("ref " + i + " hasRow leaf must carry inner-attribute children", rowChildren);
      assertEquals("ref " + i + " hasRow leaf should have 2 inner-attribute children",
          2, rowChildren.size());
    }
  }

  /**
   * Exercises nested NOT shapes: double negation (!!X) should net to unnegated, and a
   * NOT wrapping a compound (!(A and B)) should negate the compound itself, not its
   * children. The check is on the AbacReference.isNegated() flag, which downstream
   * drives the dashed-red excluded edge style.
   */
  public void testVisualizationNestedNot() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    // !!memberOf(A) → top is a single GROUP leaf, NOT NEGATED (double negation cancels).
    GrouperJexlScriptAnalysis doubleNeg = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "!!entity.memberOf('test:GroupA')",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> doubleNegRefs = doubleNeg.getVisualizationReferences();
    assertEquals(1, doubleNegRefs.size());
    AbacReference doubleNegLeaf = doubleNegRefs.get(0);
    assertEquals(AbacReference.RefType.GROUP, doubleNegLeaf.getRefType());
    assertFalse("!!X should net to unnegated", doubleNegLeaf.isNegated());

    // !(memberOf(A) && memberOf(B)) → top is a single negated COMPOUND AND with 2 children,
    // and the children themselves are NOT marked negated (the NOT applies to the compound).
    GrouperJexlScriptAnalysis notCompound = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "!(entity.memberOf('test:GroupA') && entity.memberOf('test:GroupB'))",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> notRefs = notCompound.getVisualizationReferences();
    assertEquals(1, notRefs.size());
    AbacReference notRef = notRefs.get(0);
    assertEquals(AbacReference.RefType.COMPOUND, notRef.getRefType());
    assertTrue("!(...) should mark the compound itself negated", notRef.isNegated());
    assertNotNull(notRef.getChildren());
    assertEquals(2, notRef.getChildren().size());
    for (AbacReference child : notRef.getChildren()) {
      assertFalse("compound's children are not individually negated", child.isNegated());
    }
  }

  /**
   * Pure-memberOf policy with no hasRow at all: outer AND of an included group and a
   * NOT'd excluded group. Common policy shape for app gating (must be in role X, must
   * not be in lockout). Verifies the visualization correctly identifies the included
   * group and marks only the excluded one as negated.
   */
  public void testVisualizationPureMemberOfNoHasRow() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOf('test:GroupA') && !entity.memberOf('test:GroupB')",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    // Top AND flattens, so we get two top-level refs.
    assertEquals(2, refs.size());

    // One should be a non-negated GROUP for test:GroupA, the other a negated GROUP for test:GroupB.
    boolean sawGroupA = false;
    boolean sawNotGroupB = false;
    for (AbacReference ref : refs) {
      assertEquals(AbacReference.RefType.GROUP, ref.getRefType());
      if ("test:GroupA".equals(ref.getName())) {
        assertFalse("GroupA should be a positive (included) reference", ref.isNegated());
        sawGroupA = true;
      } else if ("test:GroupB".equals(ref.getName())) {
        assertTrue("!GroupB should be marked negated", ref.isNegated());
        sawNotGroupB = true;
      }
    }
    assertTrue("expected a non-negated GroupA ref", sawGroupA);
    assertTrue("expected a negated GroupB ref", sawNotGroupB);
  }

  /**
   * memberOfAny([A,B,C]) collapses multiple group references into a single GROUP
   * AbacReference with memberOfAny=true. Verifies the visualization renders it that
   * way rather than producing one node per group.
   */
  public void testVisualizationMemberOfAny() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOfAny(['test:GroupA', 'test:GroupB', 'test:GroupC'])",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals("memberOfAny should produce one collapsed GROUP ref", 1, refs.size());
    AbacReference ref = refs.get(0);
    assertEquals(AbacReference.RefType.GROUP, ref.getRefType());
    assertTrue("memberOfAny flag should be set on the collapsed group ref", ref.isMemberOfAny());
  }

  /**
   * recentMemberOf('group', '2 days') analyzes to a GROUP leaf. Verifies the
   * visualization treats it like any other group reference.
   */
  public void testVisualizationRecentMemberOf() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.recentMemberOf('test:GroupA', '2 days')",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals(1, refs.size());
    assertEquals(AbacReference.RefType.GROUP, refs.get(0).getRefType());
    assertEquals("test:GroupA", refs.get(0).getName());
  }

  /**
   * hasAttribute('alias', value) analyzes to an ATTRIBUTE leaf. Verifies the
   * visualization produces an ATTRIBUTE reference type, distinct from the ROW type
   * that hasRow produces.
   */
  public void testVisualizationHasAttribute() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.hasAttribute('active', 'true')",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals(1, refs.size());
    AbacReference ref = refs.get(0);
    assertEquals(AbacReference.RefType.ATTRIBUTE, ref.getRefType());
    assertEquals("active", ref.getName());
  }

  /**
   * hasRow with a single boolean attribute (no AND/OR inside the predicate) should
   * render as a ROW leaf with no children — there are no per-attribute sub-parts to
   * break out because the predicate is already atomic.
   */
  public void testVisualizationHasRowSingleAttributePredicate() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.hasRow('affiliation', \"affiliationActive\")",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals(1, refs.size());
    AbacReference row = refs.get(0);
    assertEquals(AbacReference.RefType.ROW, row.getRefType());
    // Single-attribute predicate has no inner AND/OR, so no per-attribute clones get
    // created and the leaf has no children.
    assertTrue("single-attribute hasRow should have no children",
        row.getChildren() == null || row.getChildren().isEmpty());
  }

  /**
   * When the analyzer is given a {@code subjectForIsMemberCheck} subject, each leaf
   * AbacReference must carry containsSubject=true if that subject satisfies the leaf's
   * condition. This drives the "is member / is not member" highlighting in the
   * visualization (e.g. green vs red node coloring).
   */
  public void testVisualizationContainsSubjectFlag() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    // testSubject1 is in GroupA only; testSubject2 is in GroupB only.
    groupA.addMember(testSubject1);
    groupB.addMember(testSubject2);

    // containsSubject is computed by running each part's SQL against grouper_sql_cache_mship.
    // The membership add above doesn't populate that cache synchronously — running the
    // change-log consumer + sql-cache full sync flushes it so the analysis can see the
    // memberships.
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOf('test:GroupA') || entity.memberOf('test:GroupB')",
        testSubject1, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals("top-level OR flattens to two GROUP leaves", 2, refs.size());

    boolean checkedA = false;
    boolean checkedB = false;
    for (AbacReference ref : refs) {
      if ("test:GroupA".equals(ref.getName())) {
        assertTrue("subject1 is in GroupA — leaf should be flagged containsSubject",
            ref.isContainsSubject());
        checkedA = true;
      } else if ("test:GroupB".equals(ref.getName())) {
        assertFalse("subject1 is NOT in GroupB — leaf must not be flagged",
            ref.isContainsSubject());
        checkedB = true;
      }
    }
    assertTrue(checkedA);
    assertTrue(checkedB);
  }

  /**
   * For a negated group reference like {@code !entity.memberOf('test:GroupB')}, the
   * registered clone's population count should be the count of MEMBERS of the group
   * (un-negated), not the count of non-members (which is typically a huge number that
   * makes no sense for visualization or the screen analysis table).
   *
   * <p>Also: the analyzer must NOT produce a second "orphan" part with the un-negated
   * "Member of group X" description. Exactly one part per negated reference.
   */
  public void testVisualizationNegatedGroupShowsMemberCount() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    // GroupB has 2 members so we expect the negated-leaf populationCount to be 2.
    groupB.addMember(testSubject1);
    groupB.addMember(testSubject2);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOf('test:GroupA') && !entity.memberOf('test:GroupB')",
        testSubject1, grouperSession.getSubject(), true, null, true);

    // Visualization: AND flattens, so we have 2 top-level refs.
    List<AbacReference> refs = analysis.getVisualizationReferences();
    assertEquals(2, refs.size());

    AbacReference negatedB = null;
    for (AbacReference ref : refs) {
      if ("test:GroupB".equals(ref.getName()) && ref.isNegated()) {
        negatedB = ref;
      }
    }
    assertNotNull("expected to find the negated test:GroupB ref", negatedB);
    assertEquals("negated GROUP ref's populationCount should equal MEMBERS of the group, "
        + "not subjects NOT in the group", 2, negatedB.getPopulationCount());

    // Screen analysis table: among LEAF parts (skipping the combined root accumulator,
    // whose description naturally mentions both sides), exactly one part should describe
    // the negated GroupB condition. The prior implementation emitted both
    // "Not member of group test:GroupB" and a second un-negated "Member of group
    // test:GroupB" orphan; only the negated one should remain.
    int negatedRowCount = 0;
    int orphanPositiveRowCount = 0;
    for (GrouperJexlScriptPart part : analysis.getGrouperJexlScriptParts()) {
      if (part.getConnective() != GrouperJexlScriptPart.Connective.LEAF) {
        continue;
      }
      String desc = part.getDisplayDescription().toString();
      if (desc.contains("test:GroupB")) {
        if (desc.toLowerCase().contains("not member of")) {
          negatedRowCount++;
        } else if (desc.toLowerCase().contains("member of")) {
          orphanPositiveRowCount++;
        }
      }
    }
    assertEquals("expected one 'Not member of test:GroupB' LEAF part", 1, negatedRowCount);
    assertEquals("there should be no orphan 'Member of test:GroupB' LEAF part",
        0, orphanPositiveRowCount);
  }


}
