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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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
    TestRunner.run(new GrouperLoaderJexlScriptFullSyncTest("testSimpleAttributeAssignmentStringIncremental"));
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
    
    SqlCacheDependencyType sqlCacheDependencyTypeAbacAttribute = nameToSqlCacheDependencyType.get("abac_attribute");

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
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasAttributeLike(org, '%\\_2%')");
    
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
  
  public void testRowAttributeAssignmentStringOneEquals() {
    setupDataFields();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.hasRow('affiliation', 'affiliationCode=staff')");
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
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "alum", "T", "math"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "stu", "F", "comp"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "contr", "T", "phys"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "staff", "F", "span"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "fac", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "emer", "T", "math"));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

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
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlQuery").value("select subject_id, affiliation_code, active, org from testgrouper_field_row_affil").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldAttribute").value("affiliation_code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldAttribute").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldAttribute").value("org").store();
        

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

}
