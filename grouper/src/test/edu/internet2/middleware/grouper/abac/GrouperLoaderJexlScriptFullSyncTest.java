package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    TestRunner.run(new GrouperLoaderJexlScriptFullSyncTest("testJexlIncrementalChangeGroup"));
  }
  
  /**
   * @param name
   */
  public GrouperLoaderJexlScriptFullSyncTest(String name) {
    super(name);
  }
  
  public void testSimpleAttributeAssignmentBoolean() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('active')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    
  }
  
  public void testJexlShouldntHaveGroup() {
    setupDataFields();
    
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
  
  public void testSimpleAttributeAssignmentString() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('org', '123')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    
  }
  
  public void testSimpleAttributeGreaterThan() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeGreaterThan('jobNumber', 300)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
    
  }
  
  public void testSimpleAttributeLessThan() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLessThan('jobNumber', 300)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }
  
  public void testSimpleAttributeLessThanOrEqual() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLessThanOrEqual('jobNumber', 234)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }
  
  public void testSimpleAttributeGreaterThanOrEqual() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeGreaterThanOrEqual('jobNumber', 456)");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
    
  }
  
  public void testSimpleAttributeAssignmentStringIncremental() {
    setupDataFields();
    
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
  
  public void testSimpleHasAttributeAssignmentString() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute('org')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    
  }
  
  public void testSimpleAttributeAssignmentStringArrayAny() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeAny('org', ['123', '234'])");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }
  
  public void testSimpleAttributeAssignmentStringLike() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLike(org, '%2%')");
    
    //  test.subject.0
    //  test.subject.2
    //  test.subject.1
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }
  
  public void testSimpleAttributeAssignmentNumberArrayAny() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeAny('jobNumber', [123, 234])");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }

  public void testRowAttributeAssignmentString() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode == staff')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    
    int exists = new GcDbAccess().sql("""
        select count(1)
        from grouper_data_row_field_asgn_v gdrfav1,
        grouper_data_row_field_asgn_v gdrfav2
        where gdrfav1.data_row_assign_internal_id = gdrfav2.data_row_assign_internal_id 
        and gdrfav1.data_field_config_id = 'affiliationCode'
        and gdrfav1.value_text = 'emer'
        and gdrfav2.data_field_config_id = 'affiliationOrg'
        and gdrfav2.value_text is null
        """).select(Integer.class);

    assertEquals(0, exists);
    
    
  }
  
  public void testRowAttributeLessThan() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber<300')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member3));
        
  }
  
  public void testRowAttributeLessThanOrEqual() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber<=468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member3));
        
  }
  
  public void testRowAttributeGreaterThanOrEqual() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>=468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
        
  }
  public void testRowAttributeGreaterThanOrEqualNegative() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>=-468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());

    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    
  }

  public void testRowAttributeGreaterThan() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber>468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(1, members.size());
    
    assertTrue(members.contains(member2));
        
  }

  public void testRowAttributeEqualNumber() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationDeptNumber == 468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(1, members.size());
    
    assertTrue(members.contains(member1));
        
  }

  public void testRowAttributeNotEqualNumber() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber != 468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
        
  }

  public void testRowAttributeNotEqualNegativeNumber() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber != -468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);

    Set<Member> members = testGroup.getMembers();
    assertEquals(4, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
        
  }

  public void testRowAttributeEqualNegativeNumber() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', ' affiliationDeptNumber != null && affiliationDeptNumber == -468')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(0, members.size());
    
        
  }

  public void testRowAttributeAssignmentStringOneEquals() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode==staff')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    
  }
  
  public void testRowAttributeAssignmentStringAny() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode =~ [staff, fac, alum]')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    
  }
  
  public void testRowAttributeAssignmentStringLike() {
    setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeLike(affiliationCode, '%f%')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    
  }
  
  public void testRowAttributeAssignmentStringRegex() {
    setupDataFields();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', \"hasAttributeRegex(affiliationCode, '^.*f.*$')\")");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member3 = MemberFinder.findBySubject(grouperSession, testSubject3, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member2));
    assertTrue(members.contains(member3));
    
  }
  
  private void setupDataFields() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperDataProviderTest.createTableAffiliation();
    GrouperDataProviderTest.createTableAttributes();
    GrouperDataProviderTest.createTableAttributesMulti();
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();

    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));
    
    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();
    
    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "456"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "789"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "456"));

    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values (?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl", 135));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "alum", "T", "math", 246));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "stu", "F", "comp", null));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "contr", "T", "phys", 468));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "staff", "F", "span", 579));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "fac", "T", "engl", 135));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "emer", "T", null, 246));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org, dept_number) values (?, ?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.quartzCron").value("59 59 23 31 12 ? 2099").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();
        
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled, hasTwoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.descriptionHtml").value("<b>description html </b>").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.org.fieldAliases").value("org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.org.fieldDataType").value("string").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.org.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.org.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.org.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.descriptionHtml").value("<b>description html </b>").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumber.fieldAliases").value("affiliationDeptNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumber.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumber.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumber.descriptionHtml").value("<b>dept number</b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("4").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.3.colDataFieldConfigId").value("affiliationDeptNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("<b>description html </b>").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlQuery").value("select subject_id, active, two_step_enrolled, employee from testgrouper_field_attr").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataStructure").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.0.providerDataFieldConfigId").value("isActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.0.providerDataFieldAttribute").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.1.providerDataFieldConfigId").value("twoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.1.providerDataFieldAttribute").value("two_step_enrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.2.providerDataFieldConfigId").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataField.2.providerDataFieldAttribute").value("employee").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySqlQuery").value("select subject_id, attribute_value as job_number from testgrouper_field_attr_multi").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataStructure").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryNumberOfDataFields").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldConfigId").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldAttribute").value("job_number").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQuerySqlQuery").value("select subject_id, attribute_value as org from testgrouper_field_attr_multi").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryDataStructure").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryNumberOfDataFields").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryDataField.0.providerDataFieldConfigId").value("org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.orgAttrMulti.providerQueryDataField.0.providerDataFieldAttribute").value("org").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlQuery").value("select subject_id, affiliation_code, active, org, dept_number from testgrouper_field_row_affil").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryNumberOfDataFields").value("4").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldAttribute").value("affiliation_code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldAttribute").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldAttribute").value("org").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.3.providerDataFieldConfigId").value("affiliationDeptNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.3.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.3.providerDataFieldAttribute").value("dept_number").store();


    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlQuery").value("select id, subject_id, create_timestamp1 from testgrouper_dp_changelog").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryPrimaryKeyAttribute").value("id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryTimestampAttribute").value("create_timestamp1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectSourceId").value("jdbc").store();
    
    // load data
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
  }
  
  public void testSimpleAttributeAssignmentStringRegex() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeRegex(org, '^.*2.*$')");
    
    //  test.subject.0
    //  test.subject.2
    //  test.subject.1
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(3, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    assertTrue(members.contains(member2));
    
  }



  public void testSimpleAttributeAssignmentStringIdentifier() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttribute(org, '123')");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");
    
    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);
    
    Set<Member> members = testGroup.getMembers();
    assertEquals(2, members.size());
    
    assertTrue(members.contains(member0));
    assertTrue(members.contains(member1));
    
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
    setupDataFields();
    
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
    setupDataFields();
    
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
    setupDataFields();
    
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
    setupDataFields();
    
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
  
  
}
