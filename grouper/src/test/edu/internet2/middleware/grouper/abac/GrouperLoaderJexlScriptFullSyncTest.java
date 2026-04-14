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


}
