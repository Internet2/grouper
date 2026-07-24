package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
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
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldType;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignDao;
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
    TestRunner.run(new GrouperLoaderJexlScriptFullSyncTest("testJexlTimeFromNowSync"));
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

  /**
   * globalAttributeValue('alias') reads the value of a data field assigned to the abacGlobal group and
   * uses it as a SQL bind variable (or LIKE/REGEX pattern) in the scripted group membership query.  This
   * exercises every operator that accepts a global, across integer, string and boolean global values, and
   * multiple globals combined in one script.
   */
  public void testJexlGlobalAttributeValue() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // the abacGlobal group holds the data field values used as global variables in abac scripts
    Group abacGlobalGroup = new GroupSave().assignName(GrouperAbac.abacGlobalGroupName())
        .assignCreateParentStemsIfNotExist(true).save();

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperDataField jobNumberField = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get("jobnumber").getGrouperDataField();
    GrouperDataField orgField = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get("org").getGrouperDataField();
    GrouperDataField employeeField = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get("employee").getGrouperDataField();

    long dataProviderInternalId = GrouperDataProviderDao.selectByText("idm").getInternalId();
    long abacGlobalMemberInternalId = abacGlobalGroup.toMember().getInternalId();

    // assign the global variable values to the abacGlobal group: jobNumber=456 (integer), org="234"
    // (string), employee=true (boolean)
    assignFieldValue(GrouperDataFieldType.integer, jobNumberField.getInternalId(), 456L, abacGlobalMemberInternalId, dataProviderInternalId);
    assignFieldValue(GrouperDataFieldType.string, orgField.getInternalId(), "234", abacGlobalMemberInternalId, dataProviderInternalId);
    assignFieldValue(GrouperDataFieldType.bool, employeeField.getInternalId(), Boolean.TRUE, abacGlobalMemberInternalId, dataProviderInternalId);

    // make sure the global value cache picks up the new assignments
    GrouperAbac.clearCaches();

    // sanity check the global lookup returns the assigned scalars (string dictionary text, integer/bool value_integer),
    // each wrapped in a single-entry list — the map is Map<Long, List<Object>> so multi-value globals can be represented
    assertEquals(GrouperUtil.toList(456L), GrouperAbac.globalAttributeValueByDataFieldInternalId().get(jobNumberField.getInternalId()));
    assertEquals(GrouperUtil.toList("234"), GrouperAbac.globalAttributeValueByDataFieldInternalId().get(orgField.getInternalId()));
    assertEquals(GrouperUtil.toList(1L), GrouperAbac.globalAttributeValueByDataFieldInternalId().get(employeeField.getInternalId()));

    // subject jobNumbers (integer, multivalued): s0={123,234} s1={123,456} s2={234} s3={789,456}
    // subject org (string, multivalued, same source values): s0={"123","234"} s1={"123","456"} s2={"234"} s3={"789","456"}
    // subject employee (boolean): s0=true s1=false s2=true s3=false

    // integer global (jobNumber = 456): equals, every comparison operator, and between
    Group gIntEq = createScriptedGroup(grouperSession, "test:gIntEq", "entity.hasAttribute('jobNumber', globalAttributeValue('jobNumber'))");
    Group gIntGt = createScriptedGroup(grouperSession, "test:gIntGt", "entity.hasAttributeGreaterThan('jobNumber', globalAttributeValue('jobNumber'))");
    Group gIntGe = createScriptedGroup(grouperSession, "test:gIntGe", "entity.hasAttributeGreaterThanOrEqual('jobNumber', globalAttributeValue('jobNumber'))");
    Group gIntLt = createScriptedGroup(grouperSession, "test:gIntLt", "entity.hasAttributeLessThan('jobNumber', globalAttributeValue('jobNumber'))");
    Group gIntLe = createScriptedGroup(grouperSession, "test:gIntLe", "entity.hasAttributeLessThanOrEqual('jobNumber', globalAttributeValue('jobNumber'))");
    Group gIntBetween = createScriptedGroup(grouperSession, "test:gIntBetween", "entity.hasAttributeBetween(globalAttributeValue('jobNumber') <= jobNumber, jobNumber <= 500)");

    // string global (org = "234"): equals, like and regex (the global supplies the pattern)
    Group gStrEq = createScriptedGroup(grouperSession, "test:gStrEq", "entity.hasAttribute('org', globalAttributeValue('org'))");
    Group gStrLike = createScriptedGroup(grouperSession, "test:gStrLike", "entity.hasAttributeLike('org', globalAttributeValue('org'))");
    Group gStrRegex = createScriptedGroup(grouperSession, "test:gStrRegex", "entity.hasAttributeRegex('org', globalAttributeValue('org'))");

    // boolean global (employee = true)
    Group gBoolEq = createScriptedGroup(grouperSession, "test:gBoolEq", "entity.hasAttribute('employee', globalAttributeValue('employee'))");

    // multiple globals (and different types) combined in one script
    Group gComboAnd = createScriptedGroup(grouperSession, "test:gComboAnd", "entity.hasAttribute('employee', globalAttributeValue('employee')) && entity.hasAttributeLessThan('jobNumber', globalAttributeValue('jobNumber'))");
    Group gComboOr = createScriptedGroup(grouperSession, "test:gComboOr", "entity.hasAttribute('jobNumber', globalAttributeValue('jobNumber')) || entity.hasAttribute('org', globalAttributeValue('org'))");

    // a global used inside a hasRow condition (row column comparison)
    Group gRow = createScriptedGroup(grouperSession, "test:gRow", "entity.hasRow('affiliation', \"affiliationDeptNumber < globalAttributeValue('jobNumber')\")");

    // build the sql cache group rows for all the new groups, then run the full sync once
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // integer operators vs global jobNumber 456
    assertEquals(2, gIntEq.getMembers().size());        // == 456   -> s1, s3
    assertEquals(1, gIntGt.getMembers().size());        // >  456   -> s3 (789)
    assertEquals(2, gIntGe.getMembers().size());        // >= 456   -> s1, s3
    assertEquals(3, gIntLt.getMembers().size());        // <  456   -> s0, s1, s2
    assertEquals(4, gIntLe.getMembers().size());        // <= 456   -> s0, s1, s2, s3
    assertEquals(2, gIntBetween.getMembers().size());   // 456..500 -> s1, s3

    // string operators vs global org "234"
    assertEquals(2, gStrEq.getMembers().size());        // == "234"    -> s0, s2
    assertEquals(2, gStrLike.getMembers().size());      // like "234"  -> s0, s2
    assertEquals(2, gStrRegex.getMembers().size());     // regex "234" -> s0, s2

    // boolean operator vs global employee true
    assertEquals(2, gBoolEq.getMembers().size());       // employee true -> s0, s2

    // multiple globals combined
    assertEquals(2, gComboAnd.getMembers().size());     // employee true AND jobNumber < 456 -> s0, s2
    assertEquals(4, gComboOr.getMembers().size());      // jobNumber == 456 OR org == "234"  -> all four

    // global inside a hasRow condition
    assertEquals(2, gRow.getMembers().size());          // affiliation row with deptNumber < 456 -> s0, s3

    // spot check the actual subjects for the equals case
    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Set<Member> intEqMembers = gIntEq.getMembers();
    assertTrue(intEqMembers.contains(MemberFinder.findBySubject(grouperSession, testSubject1, true)));
    assertTrue(intEqMembers.contains(MemberFinder.findBySubject(grouperSession, testSubject3, true)));
  }

  /**
   * A global attribute alias can be multi-valued: the abacGlobal group can have multiple data field
   * assignments for the same field, in which case {@code globalAttributeValues('alias')} returns the
   * whole list and can be dropped into list-taking operators (hasAttributeAny for scalar attributes,
   * =~ / !~ for hasRow inner predicates). The singular {@code globalAttributeValue('alias')} still
   * works, but only if the alias is single-valued — otherwise the analyzer throws so the ambiguity
   * doesn't get silently resolved to "some random one of them".
   */
  public void testJexlGlobalAttributeValueMultiValued() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group abacGlobalGroup = new GroupSave().assignName(GrouperAbac.abacGlobalGroupName())
        .assignCreateParentStemsIfNotExist(true).save();

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperDataField jobNumberField = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get("jobnumber").getGrouperDataField();
    GrouperDataField orgField = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get("org").getGrouperDataField();

    long dataProviderInternalId = GrouperDataProviderDao.selectByText("idm").getInternalId();
    long abacGlobalMemberInternalId = abacGlobalGroup.toMember().getInternalId();

    // multi-value globals: jobNumber = [456, 789] (integer, two rows in grouper_data_field_assign)
    // and org = ["engl", "math"] (string). Each call to assignFieldValue inserts one row.
    assignFieldValue(GrouperDataFieldType.integer, jobNumberField.getInternalId(), 456L, abacGlobalMemberInternalId, dataProviderInternalId);
    assignFieldValue(GrouperDataFieldType.integer, jobNumberField.getInternalId(), 789L, abacGlobalMemberInternalId, dataProviderInternalId);
    assignFieldValue(GrouperDataFieldType.string, orgField.getInternalId(), "engl", abacGlobalMemberInternalId, dataProviderInternalId);
    assignFieldValue(GrouperDataFieldType.string, orgField.getInternalId(), "math", abacGlobalMemberInternalId, dataProviderInternalId);

    GrouperAbac.clearCaches();

    // sanity check the global lookup returns both values (order is DAO-defined; use a set-compare via containsAll)
    List<Object> jobNumberList = GrouperAbac.globalAttributeValueByDataFieldInternalId().get(jobNumberField.getInternalId());
    assertEquals(2, jobNumberList.size());
    assertTrue(jobNumberList.contains(456L));
    assertTrue(jobNumberList.contains(789L));
    List<Object> orgList = GrouperAbac.globalAttributeValueByDataFieldInternalId().get(orgField.getInternalId());
    assertEquals(2, orgList.size());
    assertTrue(orgList.contains("engl"));
    assertTrue(orgList.contains("math"));

    // subject jobNumbers (integer, multivalued): s0={123,234} s1={123,456} s2={234} s3={789,456}
    // subject affiliation rows: s0={engl,math} s1={comp,phys} s2={span} s3={engl,null}

    // hasAttributeAny with an integer multi-valued global — matches subjects with any jobNumber in [456, 789]
    Group gIntAny = createScriptedGroup(grouperSession, "test:gIntAny", "entity.hasAttributeAny('jobNumber', globalAttributeValues('jobNumber'))");

    // hasAttributeAny with a string multi-valued global — matches subjects with any org in ["engl", "math"]
    Group gStrAny = createScriptedGroup(grouperSession, "test:gStrAny", "entity.hasAttributeAny('org', globalAttributeValues('org'))");

    // row-inner =~ with a string multi-valued global — matches subjects with an affiliation row whose org is in ["engl","math"]
    Group gRowIn = createScriptedGroup(grouperSession, "test:gRowIn", "entity.hasRow('affiliation', \"affiliationOrg =~ globalAttributeValues('org')\")");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // hasAttributeAny('jobNumber', [456, 789]) → s1 (has 456), s3 (has 789 and 456)
    assertEquals(2, gIntAny.getMembers().size());

    // hasAttributeAny('org', ["engl", "math"]) — subject org strings are {"123","234","456","789"}, none match
    assertEquals(0, gStrAny.getMembers().size());

    // hasRow affiliationOrg =~ ["engl", "math"] → s0 (engl AND math), s3 (engl)
    assertEquals(2, gRowIn.getMembers().size());

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject testSubject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Set<Member> intAnyMembers = gIntAny.getMembers();
    assertTrue(intAnyMembers.contains(MemberFinder.findBySubject(grouperSession, testSubject1, true)));
    assertTrue(intAnyMembers.contains(MemberFinder.findBySubject(grouperSession, testSubject3, true)));

    // singular globalAttributeValue on a multi-valued alias must be rejected — verify the analyzer throws
    // with a message that names both the alias and directs the author to the plural form.
    try {
      GrouperLoaderJexlScriptFullSync.analyzeJexlScript(grouperDataEngine,
          "entity.hasAttribute('jobNumber', globalAttributeValue('jobNumber'))");
      fail("expected RuntimeException on singular globalAttributeValue against a multi-valued global");
    } catch (RuntimeException expected) {
      assertTrue("expected message to name the alias, got: " + expected.getMessage(),
          expected.getMessage().contains("jobNumber"));
      assertTrue("expected message to mention globalAttributeValues plural, got: " + expected.getMessage(),
          expected.getMessage().contains("globalAttributeValues"));
    }
  }

  /**
   * Fails right now. Pre-existing bug in the row-inner =~ / !~ handler in
   * {@link GrouperLoaderJexlScriptFullSync}: the ASTUnaryMinusNode branch checks
   * {@code jexlNode.jjtGetChild(1) instanceof ASTUnaryMinusNode} — but child(1) is the whole
   * ASTArrayLiteral, never a unary-minus — so the branch is dead. A negative-number entry falls
   * through every branch, {@code rightPartSingleValue} stays null, and a null bind var is
   * appended to the args list. SQL then binds {@code col IN (null, 246)}, silently dropping the
   * -135 half. Analyzer-only assertion (no sync needed) — checks the bind args directly.
   */
  public void testHasRowInListNegativeNumberBug() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScript(
        grouperDataEngine,
        "entity.hasRow('affiliation', \"affiliationDeptNumber =~ [-135, 246]\")");

    // walk every part's args and collect the "attributeValue" bind values — with correct behavior
    // both "-135" and "246" show up; with the bug one of them is null.
    List<Object> attributeValues = new ArrayList<Object>();
    for (GrouperJexlScriptPart part : analysis.getGrouperJexlScriptParts()) {
      for (MultiKey arg : part.getArguments()) {
        if ("attributeValue".equals(arg.getKey(0))) {
          attributeValues.add(arg.getKey(1));
        }
      }
    }

    assertFalse("row-inner =~ bound a null for the negative-number entry (args: " + attributeValues + ")",
        attributeValues.contains(null));
    assertTrue("expected the -135 entry to be bound as \"-135\" (args: " + attributeValues + ")",
        attributeValues.contains("-135"));
    assertTrue("expected the 246 entry to be bound as \"246\" (args: " + attributeValues + ")",
        attributeValues.contains("246"));
  }

  /**
   * Assign a data field value to a member — equivalent to what the data provider daemon inserts
   * into grouper_data_field_assign, but done directly so a test can seed known values without
   * running the daemon.
   *
   * The same helper works for the abacGlobal group's member (feeding globalAttributeValue('alias')
   * scripts) and for regular subject members (feeding hasAttribute-style scripts) — nothing in
   * the DAO write path is special-cased for the global group.
   * @param fieldType the data field type, drives whether the value lands in value_integer or a dictionary
   * @param dataFieldInternalId the data field
   * @param value the value to assign
   * @param memberInternalId the member internal id (abacGlobal for globals, regular subjects for per-subject values)
   * @param dataProviderInternalId the data provider internal id
   */
  private static void assignFieldValue(GrouperDataFieldType fieldType, long dataFieldInternalId, Object value,
      long memberInternalId, long dataProviderInternalId) {
    GrouperDataFieldAssign fieldAssign = new GrouperDataFieldAssign();
    fieldAssign.setDataFieldInternalId(dataFieldInternalId);
    fieldAssign.setMemberInternalId(memberInternalId);
    fieldAssign.setDataProviderInternalId(dataProviderInternalId);
    fieldType.assignValue(fieldAssign, value, null);
    GrouperDataFieldAssignDao.store(fieldAssign);
  }

  /**
   * create a group marked as an abac jexl scripted group with the given script.
   * @param grouperSession the session
   * @param groupName the group name
   * @param script the abac jexl script
   * @return the created group
   */
  private static Group createScriptedGroup(GrouperSession grouperSession, String groupName, String script) {
    Group group = new GroupSave().assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(group)
        .assignAttributeDefName(attributeDefNameMarker).save();
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), script);
    return group;
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

  /**
   * GRP-7061: a group referenced by a scripted group could not be deleted after the scripted group
   * itself was deleted, because the now-orphaned grouper_sql_cache_dependency rows still made the
   * referenced group look like it was in use.  Verify that once the scripted group is gone, the
   * group it used to reference can be deleted.
   */
  public void testJexlDeleteGroupReferencedByDeletedScriptedGroup() {
    GrouperAbacTestHelper.setupDataFields();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupC = new GroupSave().assignName("test:GroupC").assignCreateParentStemsIfNotExist(true).save();
    Group testGroupE = new GroupSave().assignName("test:GroupE").assignCreateParentStemsIfNotExist(true).save();

    testGroupA.addMember(SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true));
    testGroupC.addMember(testGroupA.toSubject());

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);

    // testGroupE is the scripted group, and it references testGroupB and testGroupC
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroupE)
        .assignAttributeDefName(attributeDefNameMarker).save();

    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(),
        """
        entity.memberOf('test:GroupC') && !entity.memberOf('test:GroupB')
        """);

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // the dependency from the scripted group to the referenced groups should now exist
    Set<String> ownerGroupNames = new HashSet<>(new GcDbAccess().sql("select owner_group_name from grouper_sql_dependency_group_v where depen_group_name = ?").addBindVar(testGroupE.getName()).selectList(String.class));
    assertTrue(ownerGroupNames.contains(testGroupC.getName()));
    assertTrue(ownerGroupNames.contains(testGroupB.getName()));

    // while the scripted group exists, the referenced group cannot be deleted
    try {
      testGroupC.delete();
      fail("should not be able to delete a group referenced by an existing scripted group");
    } catch (GrouperReferentialIntegrityException e) {
      // expected
    }

    // delete the scripted group - this leaves the dependency rows orphaned (dependent group gone)
    testGroupE.delete();

    // GRP-7061: deleting the now-unreferenced group used to fail because the orphaned dependency
    // rows still pointed at it.  It should succeed now.
    testGroupC.delete();
    assertNull(GroupFinder.findByName(grouperSession, "test:GroupC", false));
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

  /**
   * ABAC scripts can filter on the subject's source with member.subjectSourceId == 'jdbc' — useful
   * to require "real people only" and not service principals. This exercises the top-level ==/!=
   * dispatch through the sql where-clause build to the final scripted-group membership; verifies
   * combinations with entity.memberOf and negation; and confirms the analyzer's description and
   * visualization tree render the subject-source condition as an attribute-style leaf.
   */
  public void testJexlSubjectSourceId() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperAbac.clearCaches();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group testGroupA = new GroupSave().assignName("test:GroupA").assignCreateParentStemsIfNotExist(true).save();

    Subject testSubject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, testSubject0, true);

    Subject testSubject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Member member1 = MemberFinder.findBySubject(grouperSession, testSubject1, true);

    Subject testSubject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Member member2 = MemberFinder.findBySubject(grouperSession, testSubject2, true);

    testGroupA.addMember(testSubject0);
    testGroupA.addMember(testSubject1);
    testGroupA.addMember(testSubject2);

    // groupJdbc: subject source restricted to jdbc, in group A -> all three
    Group groupJdbc = createScriptedGroup(grouperSession, "test:groupJdbc",
        "entity.memberOf('test:GroupA') && member.subjectSourceId == 'jdbc'");

    // groupNotJdbc: subject source explicitly not jdbc, in group A -> none (only source is jdbc)
    Group groupNotJdbc = createScriptedGroup(grouperSession, "test:groupNotJdbc",
        "entity.memberOf('test:GroupA') && member.subjectSourceId != 'jdbc'");

    // groupBogusSource: unknown subject source, in group A -> none
    Group groupBogusSource = createScriptedGroup(grouperSession, "test:groupBogusSource",
        "entity.memberOf('test:GroupA') && member.subjectSourceId == 'bogusSource'");

    // groupNotEqNegated: !(subject source == jdbc), same as != jdbc -> none
    Group groupNotEqNegated = createScriptedGroup(grouperSession, "test:groupNotEqNegated",
        "entity.memberOf('test:GroupA') && !(member.subjectSourceId == 'jdbc')");

    // groupEntityAlias: entity.subjectSourceId form is also accepted, in group A -> all three
    Group groupEntityAlias = createScriptedGroup(grouperSession, "test:groupEntityAlias",
        "entity.memberOf('test:GroupA') && entity.subjectSourceId == 'jdbc'");

    // groupOrSources: OR of two sources at the top level, in group A -> all three
    // (jdbc matches every subject; the ldap disjunct is present but never fires)
    Group groupOrSources = createScriptedGroup(grouperSession, "test:groupOrSources",
        "entity.memberOf('test:GroupA') "
            + "&& (member.subjectSourceId == 'ldap' || member.subjectSourceId == 'jdbc')");

    // groupOrSourcesNone: OR whitelist of non-matching sources, in group A -> none
    Group groupOrSourcesNone = createScriptedGroup(grouperSession, "test:groupOrSourcesNone",
        "entity.memberOf('test:GroupA') "
            + "&& (member.subjectSourceId == 'ldap' || member.subjectSourceId == 'other')");

    // groupPerGroupSources: each disjunct pairs a group with its own allowed source
    Group testGroupB = new GroupSave().assignName("test:GroupB").assignCreateParentStemsIfNotExist(true).save();
    testGroupB.addMember(testSubject1);
    Group groupPerGroupSources = createScriptedGroup(grouperSession, "test:groupPerGroupSources",
        "(entity.memberOf('test:GroupA') && member.subjectSourceId == 'jdbc') "
            + "|| (entity.memberOf('test:GroupB') && member.subjectSourceId == 'ldap')");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    Set<Member> jdbcMembers = groupJdbc.getMembers();
    assertEquals("subjectSourceId == 'jdbc' should match every group-A jdbc subject",
        3, jdbcMembers.size());
    assertTrue(jdbcMembers.contains(member0));
    assertTrue(jdbcMembers.contains(member1));
    assertTrue(jdbcMembers.contains(member2));

    assertEquals("subjectSourceId != 'jdbc' should not match any jdbc subjects",
        0, groupNotJdbc.getMembers().size());
    assertEquals("subjectSourceId == 'bogusSource' should not match any jdbc subjects",
        0, groupBogusSource.getMembers().size());
    assertEquals("!(subjectSourceId == 'jdbc') should not match any jdbc subjects",
        0, groupNotEqNegated.getMembers().size());
    assertEquals("entity.subjectSourceId == 'jdbc' should match every group-A jdbc subject",
        3, groupEntityAlias.getMembers().size());
    assertEquals("OR of sources with jdbc in the list should match every group-A subject",
        3, groupOrSources.getMembers().size());
    assertEquals("OR whitelist with no matching source should not match any group-A subject",
        0, groupOrSourcesNone.getMembers().size());
    // per-group whitelist: only the group-A branch fires (jdbc); group-B branch requires ldap
    assertEquals("per-group source whitelist should match only the jdbc-in-A branch",
        3, groupPerGroupSources.getMembers().size());

    // Analysis + visualization: assert the description and the AbacReference tree shape
    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    // stand-alone subjectSourceId == 'jdbc' — top level is an EQ node; visualization root
    // should be a single attribute-style leaf named 'subjectSourceId'
    GrouperJexlScriptAnalysis analysisEq = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "member.subjectSourceId == 'jdbc'",
        testSubject, grouperSession.getSubject(), true, null, true);

    List<AbacReference> refsEq = analysisEq.getVisualizationReferences();
    assertNotNull("expected visualization references for subjectSourceId ==", refsEq);
    assertEquals(1, refsEq.size());
    AbacReference eqRef = refsEq.get(0);
    assertEquals(AbacReference.RefType.ATTRIBUTE, eqRef.getRefType());
    assertEquals("subjectSourceId", eqRef.getName());
    assertEquals("jdbc", eqRef.getValue());
    assertFalse("== is not a negated reference", eqRef.isNegated());
    assertNotNull(eqRef.getDisplayDescription());
    assertTrue("description should mention subject source (was: '" + eqRef.getDisplayDescription() + "')",
        eqRef.getDisplayDescription().toLowerCase().contains("subject source"));
    assertTrue("description should mention the source id (was: '" + eqRef.getDisplayDescription() + "')",
        eqRef.getDisplayDescription().contains("jdbc"));

    // subjectSourceId != 'jdbc' — the visualization must mark the ref as negated so the edge
    // renders as "must not be in" (matching the !(==) form). Otherwise the box "Subject source
    // is jdbc" combined with a positive edge would read as the OPPOSITE of the script's intent.
    // The standalone leaf shows the negated count (# of non-jdbc subjects), matching how
    // !memberOf(X) standalone shows the negated count too — the "clone shows the un-negated
    // count" convention only applies when there's a separate AND/OR clone.
    GrouperJexlScriptAnalysis analysisNe = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "member.subjectSourceId != 'jdbc'",
        testSubject, grouperSession.getSubject(), true, null, true);
    AbacReference neRef = analysisNe.getVisualizationReferences().get(0);
    assertEquals(AbacReference.RefType.ATTRIBUTE, neRef.getRefType());
    assertEquals("subjectSourceId", neRef.getName());
    assertTrue("!= must produce a negated ref so the edge shows 'must not be in'", neRef.isNegated());
    assertEquals("!= standalone leaf count should be # of non-jdbc subjects (0 in this env)",
        0, analysisNe.getGrouperJexlScriptParts().get(0).getPopulationCount());

    // !(member.subjectSourceId == 'jdbc') — ASTNotNode wrapping ASTEQNode; visualization
    // flags the ref as negated. Standalone leaf shows the negated count (matches != standalone).
    GrouperJexlScriptAnalysis analysisNotEq = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "!(member.subjectSourceId == 'jdbc')",
        testSubject, grouperSession.getSubject(), true, null, true);
    AbacReference notEqRef = analysisNotEq.getVisualizationReferences().get(0);
    assertEquals(AbacReference.RefType.ATTRIBUTE, notEqRef.getRefType());
    assertTrue("!(... == ...) should be marked negated", notEqRef.isNegated());

    // AND clone consistency: for A && != 'jdbc' and A && !(== 'jdbc') the negated subject-source
    // leaf must show the un-negated jdbc count (memberOf convention). Regression guard that !=
    // and !(==) don't drift apart — both route through the same accumulator-vs-clone logic and
    // should produce identical clone counts.
    int prevNegatedLeafCount = -1;
    for (String script : new String[] {
        "entity.memberOf('test:GroupA') && member.subjectSourceId != 'jdbc'",
        "entity.memberOf('test:GroupA') && !(member.subjectSourceId == 'jdbc')"}) {
      GrouperJexlScriptAnalysis a = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
          grouperDataEngine, script, testSubject, grouperSession.getSubject(), true, null, true);
      int negatedLeafCount = -1;
      for (GrouperJexlScriptPart part : a.getGrouperJexlScriptParts()) {
        String desc = part.getDisplayDescription().toString().toLowerCase();
        if (desc.startsWith("not ") && desc.contains("subject source")) {
          negatedLeafCount = part.getPopulationCount();
          break;
        }
      }
      // The clone represents the condition being negated. Its count = |jdbc| > 0 — the same
      // convention !memberOf(X) uses. Earlier the != form gave |¬jdbc| here while !(==) gave
      // |jdbc|; they now agree.
      assertTrue("negated subject-source leaf count should be > 0 (# of jdbc subjects, "
          + "memberOf convention) for script: " + script,
          negatedLeafCount > 0);
      if (prevNegatedLeafCount >= 0) {
        assertEquals("!= and !(==) should produce identical clone counts", prevNegatedLeafCount, negatedLeafCount);
      }
      prevNegatedLeafCount = negatedLeafCount;
    }

    // Nested-not regression: A && !(!= 'jdbc') is semantically A && (== 'jdbc'). The
    // top-level AND must stay a positive compound (not marked negated) so the flatten pass
    // produces two positive edges — one to the group leaf, one to the subjectSourceId leaf.
    // Earlier a stale isAccumulator check toggled root.negated when the != was nested inside
    // a NOT, which prevented flattening and drew a single "must not be in" edge into a
    // merged "Must be in group ... and subject source is jdbc" box.
    GrouperJexlScriptAnalysis analysisNestedNot = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOf('test:GroupA') && !(member.subjectSourceId != 'jdbc')",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> refsNestedNot = analysisNestedNot.getVisualizationReferences();
    assertEquals("A && !(!= X) should flatten to 2 top-level refs", 2, refsNestedNot.size());
    for (AbacReference ref : refsNestedNot) {
      assertFalse("neither top-level ref should be negated (!(!=) cancels to positive)",
          ref.isNegated());
    }

    // Combined with entity.memberOf: root is a flattened AND yielding two top-level refs —
    // the GROUP leaf and the ATTRIBUTE-style subjectSourceId leaf
    GrouperJexlScriptAnalysis analysisAnd = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.memberOf('test:GroupA') && member.subjectSourceId == 'jdbc'",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> refsAnd = analysisAnd.getVisualizationReferences();
    assertEquals("top-level AND should flatten to 2 refs", 2, refsAnd.size());
    boolean sawGroup = false;
    boolean sawSubjectSource = false;
    for (AbacReference ref : refsAnd) {
      if (ref.getRefType() == AbacReference.RefType.GROUP && "test:GroupA".equals(ref.getName())) {
        sawGroup = true;
      } else if (ref.getRefType() == AbacReference.RefType.ATTRIBUTE
          && "subjectSourceId".equals(ref.getName())) {
        sawSubjectSource = true;
      }
    }
    assertTrue("expected the memberOf group leaf under the flattened AND", sawGroup);
    assertTrue("expected the subjectSourceId leaf under the flattened AND", sawSubjectSource);

    // Whitelist shape: entity.memberOf('A') && (source == 'jdbc' || source == 'ldap').
    // Top-level AND flattens to a group leaf + an OR-compound. The compound holds two
    // subjectSourceId ATTRIBUTE leaves (one per allowed source).
    GrouperJexlScriptAnalysis analysisWhitelist = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.memberOf('test:GroupA') && (member.subjectSourceId == 'jdbc' || member.subjectSourceId == 'ldap')",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> refsWhitelist = analysisWhitelist.getVisualizationReferences();
    assertEquals("top-level AND should flatten to 2 refs (group leaf + OR compound)",
        2, refsWhitelist.size());
    AbacReference orCompound = null;
    for (AbacReference ref : refsWhitelist) {
      if (ref.getRefType() == AbacReference.RefType.COMPOUND) {
        orCompound = ref;
      }
    }
    assertNotNull("expected an OR compound under the flattened AND", orCompound);
    assertEquals("compound should be an OR", "or", orCompound.getName());
    assertNotNull(orCompound.getChildren());
    assertEquals("OR compound should carry both subjectSourceId leaves",
        2, orCompound.getChildren().size());
    java.util.Set<String> orValues = new java.util.HashSet<String>();
    for (AbacReference child : orCompound.getChildren()) {
      assertEquals("OR children should be subjectSourceId attribute leaves",
          AbacReference.RefType.ATTRIBUTE, child.getRefType());
      assertEquals("subjectSourceId", child.getName());
      orValues.add(child.getValue());
    }
    assertTrue("expected both 'jdbc' and 'ldap' under the OR", orValues.contains("jdbc"));
    assertTrue("expected both 'jdbc' and 'ldap' under the OR", orValues.contains("ldap"));

    // Per-group whitelist shape: two AND-compound siblings under a top-level OR. Each side
    // carries its own group leaf + its own subjectSourceId leaf, so each AND-compound has 2
    // children — one GROUP, one ATTRIBUTE ('subjectSourceId').
    GrouperJexlScriptAnalysis analysisPerGroup = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "(entity.memberOf('test:GroupA') && member.subjectSourceId == 'jdbc') "
            + "|| (entity.memberOf('test:GroupB') && member.subjectSourceId == 'ldap')",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> refsPerGroup = analysisPerGroup.getVisualizationReferences();
    assertEquals("top-level OR should flatten to 2 AND-compound refs", 2, refsPerGroup.size());
    for (AbacReference andCompound : refsPerGroup) {
      assertEquals("expected an AND compound branch",
          AbacReference.RefType.COMPOUND, andCompound.getRefType());
      assertEquals("compound should be an AND", "and", andCompound.getName());
      assertNotNull(andCompound.getChildren());
      assertEquals("AND branch should have a group + subjectSourceId leaf",
          2, andCompound.getChildren().size());
      boolean sawBranchGroup = false;
      boolean sawBranchSource = false;
      for (AbacReference child : andCompound.getChildren()) {
        if (child.getRefType() == AbacReference.RefType.GROUP) {
          sawBranchGroup = true;
        } else if (child.getRefType() == AbacReference.RefType.ATTRIBUTE
            && "subjectSourceId".equals(child.getName())) {
          sawBranchSource = true;
        }
      }
      assertTrue("branch missing its group leaf", sawBranchGroup);
      assertTrue("branch missing its subjectSourceId leaf", sawBranchSource);
    }

    // Missing-source warning: an unknown source id should append the
    // "unknown subject source" warning to the description, mirroring the memberOf missing-group
    // behavior. The count still runs (naturally producing zero members).
    GrouperJexlScriptAnalysis analysisMissing = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "member.subjectSourceId == 'notARealSourceId'",
        testSubject, grouperSession.getSubject(), true, null, true);
    GrouperJexlScriptPart missingPart = analysisMissing.getGrouperJexlScriptParts().get(0);
    assertTrue("missing-source warning should be appended to the description (was: '"
        + missingPart.getDisplayDescription() + "')",
        missingPart.getDisplayDescription().toString().toLowerCase().contains("unknown subject source"));

    // A known source id (jdbc is configured in the test env) should NOT trip the warning.
    GrouperJexlScriptAnalysis analysisKnown = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "member.subjectSourceId == 'jdbc'",
        testSubject, grouperSession.getSubject(), true, null, true);
    assertFalse("known source id should not trip the missing-source warning",
        analysisKnown.getGrouperJexlScriptParts().get(0).getDisplayDescription().toString()
            .toLowerCase().contains("unknown subject source"));

    // Blank source id: rejected at parse time with a clear RuntimeException, not silently
    // compiled to an always-false predicate.
    try {
      GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
          grouperDataEngine, "member.subjectSourceId == ''",
          testSubject, grouperSession.getSubject(), true, null, true);
      fail("blank source id should throw at parse time");
    } catch (RuntimeException re) {
      String msg = re.getMessage() == null ? "" : re.getMessage().toLowerCase();
      assertTrue("error should mention non-blank / blank source id (was: '" + re.getMessage() + "')",
          msg.contains("non-blank") || msg.contains("blank"));
    }
  }

  /**
   * Exercises timeFromNow(n, 'unit') across every unit and inside every value-accepting
   * operator: hasAttributeLessThan / GreaterThan / Between (outer), hasAttribute equality
   * (outer), and a hasRow inner-predicate column comparison. Verifies the analyzer:
   *  - resolves each unit to a millis value in the expected range from now
   *  - accepts negative and positive offsets
   *  - does not error out on any of the shapes
   *  - binds the resolved Long as an attributeValue arg
   *
   * The integer field 'jobNumber' stands in for a timestamp column here — the analyzer only
   * cares that the field is integer/timestamp shape (value_integer column), not what the
   * number semantically represents. No sync is run; the point is to verify the analysis-time
   * plumbing for the new helper.
   */
  public void testJexlTimeFromNow() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperAbac.clearCaches();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject testSubject = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    // Unit sweep: one script per unit, each in the simplest possible shape. Assert the analysis
    // succeeds and the bind var is a millis value matching what unit + offset predicts. Expected
    // values are absolute millis computed from a reference "now" captured just before the loop;
    // the tolerance covers the small delta between that reference "now" and the moment the
    // analyzer captured its own "now". Month/year cases MUST use java.util.Calendar to compute
    // expected — naive "N * 30 days" or "N * 365 days" arithmetic drifts by several days
    // depending on the starting month and leap years, and produces flaky failures.
    long tolerance = 60_000L; // 1 min slack for the "now" moment shifting between call and impl
    long nowApprox = System.currentTimeMillis();
    Object[][] cases = new Object[][] {
        // {jexlOffsetExpr, expectedResolvedMillis}
        {"timeFromNow('-5 minutes')",  nowApprox + (-5L * 60L * 1000L)},
        {"timeFromNow('-5 hours')",    nowApprox + (-5L * 60L * 60L * 1000L)},
        {"timeFromNow('5 days')",      nowApprox + (5L * 24L * 60L * 60L * 1000L)},
        {"timeFromNow('5 weeks')",     nowApprox + (5L * 7L * 24L * 60L * 60L * 1000L)},
        {"timeFromNow('5 months')",    expectedCalendarMillis(nowApprox, java.util.Calendar.MONTH, 5)},
        {"timeFromNow('5 years')",     expectedCalendarMillis(nowApprox, java.util.Calendar.YEAR, 5)},
    };
    for (Object[] c : cases) {
      String offsetExpr = (String) c[0];
      long expectedMillis = ((Long) c[1]).longValue();
      String script = "entity.hasAttributeLessThan('jobNumber', " + offsetExpr + ")";
      GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
          grouperDataEngine, script, testSubject, grouperSession.getSubject(), true, null, false);
      assertNull("no error expected for " + offsetExpr + ": " + analysis.getErrorMessage(), analysis.getErrorMessage());

      Long resolved = null;
      for (MultiKey arg : analysis.getGrouperJexlScriptParts().get(0).getArguments()) {
        if ("attributeValue".equals(arg.getKey(0)) && arg.getKey(1) instanceof Long) {
          resolved = (Long) arg.getKey(1);
          break;
        }
      }
      assertNotNull("expected a resolved Long timestamp bind var for " + offsetExpr, resolved);
      long actualDiff = Math.abs(resolved.longValue() - expectedMillis);
      assertTrue(offsetExpr + " resolved value " + resolved
              + " should be within " + tolerance + "ms of expected " + expectedMillis
              + " (actual diff: " + actualDiff + "ms)",
          actualDiff < tolerance);
    }

    // timeFromNow('now') resolves to current millis
    GrouperJexlScriptAnalysis analysisNow = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.hasAttributeLessThan('jobNumber', timeFromNow('now'))",
        testSubject, grouperSession.getSubject(), true, null, false);
    assertNull(analysisNow.getErrorMessage());
    Long resolvedNow = null;
    for (MultiKey arg : analysisNow.getGrouperJexlScriptParts().get(0).getArguments()) {
      if ("attributeValue".equals(arg.getKey(0)) && arg.getKey(1) instanceof Long) {
        resolvedNow = (Long) arg.getKey(1);
        break;
      }
    }
    assertNotNull("timeFromNow('now') should resolve to a Long millis value", resolvedNow);
    assertTrue("timeFromNow('now') should be within a minute of System.currentTimeMillis(); got delta "
        + (resolvedNow.longValue() - nowApprox),
        Math.abs(resolvedNow.longValue() - nowApprox) < tolerance);

    // Compound shape: hasAttributeBetween with timeFromNow on both bounds — exercises the
    // parseBetweenComparisonArg -> extractLiteralValue path through the new helper.
    GrouperJexlScriptAnalysis analysisBetween = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.hasAttributeBetween(timeFromNow('now') <= jobNumber, jobNumber <= timeFromNow('30 days'))",
        testSubject, grouperSession.getSubject(), true, null, false);
    assertNull("hasAttributeBetween with timeFromNow bounds should analyze cleanly: "
        + analysisBetween.getErrorMessage(), analysisBetween.getErrorMessage());

    // Row-inner shape: timeFromNow inside a hasRow predicate's column comparison exercises the
    // analyzeJexlRowToSqlHelper AST-EQ/LT/etc branch.
    GrouperJexlScriptAnalysis analysisRow = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine,
        "entity.hasRow('affiliation', \"affiliationDeptNumber < timeFromNow('now') "
            + "&& affiliationDeptNumber > timeFromNow('-30 days')\")",
        testSubject, grouperSession.getSubject(), true, null, false);
    assertNull("timeFromNow inside hasRow inner predicate should analyze cleanly: "
        + analysisRow.getErrorMessage(), analysisRow.getErrorMessage());

    // Friendly display: the visualization ATTRIBUTE ref's attributeValues list carries the
    // natural-language phrase for the timeFromNow bound, not the raw millis
    GrouperJexlScriptAnalysis analysisDisplay = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
        grouperDataEngine, "entity.hasAttributeLessThan('jobNumber', timeFromNow('-30 days'))",
        testSubject, grouperSession.getSubject(), true, null, true);
    List<AbacReference> refs = analysisDisplay.getVisualizationReferences();
    assertEquals(1, refs.size());
    AbacReference ref = refs.get(0);
    assertEquals("30 days ago", ref.getAttributeValues().get(0));

    // Unknown unit — should surface a clear error
    try {
      GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(grouperDataEngine,
          "entity.hasAttributeLessThan('jobNumber', timeFromNow('5 fortnights'))",
          testSubject, grouperSession.getSubject(), true, null, false);
      fail("unknown unit should throw");
    } catch (RuntimeException re) {
      String msg = re.getMessage() == null ? "" : re.getMessage().toLowerCase();
      assertTrue("error should mention the unit / format (was: '" + re.getMessage() + "')",
          msg.contains("unit") || msg.contains("fortnights") || msg.contains("must be"));
    }

    // Wrong argument shape (positional int + string) — should now throw
    try {
      GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(grouperDataEngine,
          "entity.hasAttributeLessThan('jobNumber', timeFromNow(5, 'days'))",
          testSubject, grouperSession.getSubject(), true, null, false);
      fail("two-arg timeFromNow(N, 'unit') form is no longer accepted");
    } catch (RuntimeException re) {
      String msg = re.getMessage() == null ? "" : re.getMessage().toLowerCase();
      assertTrue("error should mention single string argument (was: '" + re.getMessage() + "')",
          msg.contains("single") || msg.contains("string"));
    }
  }

  /**
   * Compute expected millis by mirroring the impl's Calendar-based arithmetic. Necessary for the
   * month/year cases in testJexlTimeFromNow because "N months" is not a fixed number of days —
   * it depends on the starting month (Aug=31, Sep=30, ...), and "N years" is affected by leap
   * years. A naive N*30-days / N*365-days expectation drifts multiple days and causes flakes.
   */
  private static long expectedCalendarMillis(long baseMillis, int calendarField, int amount) {
    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTimeInMillis(baseMillis);
    cal.add(calendarField, amount);
    return cal.getTimeInMillis();
  }

  /**
   * End-to-end sync test for timeFromNow covering BOTH:
   *   - hasAttribute-family predicates (Greater / Less / Between) against a real timestamp-typed
   *     data field, and
   *   - hasRow inner-predicate column comparisons against a real timestamp-typed row.
   *
   * Registers all fields / row config inline (nothing added to the shared setupDataFields), then
   * seeds known millis-shape values via GrouperDataFieldType.timestamp so the timestamp
   * assignValueHelper actually runs (both the GrouperDataFieldAssign and GrouperDataRowFieldAssign
   * overloads). One provider daemon skip, one data engine reload, one ABAC full-sync run — the
   * hasRow and hasAttribute worlds share the same four subjects and the same "now", so their
   * fixtures line up and failures narrow to whichever operator regressed.
   */
  public void testJexlTimeFromNowSync() {
    GrouperAbacTestHelper.setupDataFields();
    GrouperAbac.clearCaches();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, true);

    // ---- config: one attribute-typed timestamp field, plus a row with two rowColumn timestamps.

    String attrFieldConfigId  = "testJexlTfnLastLogin";
    String attrAlias          = "tfnLastLoginAt";
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + attrFieldConfigId + ".fieldAliases").value(attrAlias).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + attrFieldConfigId + ".fieldDataType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + attrFieldConfigId + ".fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + attrFieldConfigId + ".descriptionHtml").value("<b>test timestamp field</b>").store();

    String startFieldConfigId = "testJexlTfnRowStart";
    String endFieldConfigId   = "testJexlTfnRowEnd";
    String rowConfigId        = "testJexlTfnRowEnrollment";
    String startAlias         = "tfnEnrollmentStart";
    String endAlias           = "tfnEnrollmentEnd";
    String rowAlias           = "tfnEnrollment";
    registerTimestampRowColumn(startFieldConfigId, startAlias);
    registerTimestampRowColumn(endFieldConfigId, endAlias);
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowAliases").value(rowAlias).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowNumberOfDataFields").value("2").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowDataField.0.colDataFieldConfigId").value(startFieldConfigId).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".rowDataField.1.colDataFieldConfigId").value(endFieldConfigId).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow." + rowConfigId + ".descriptionHtml").value("<b>test enrollment row</b>").store();

    // The daemon normally creates the GrouperDataField / GrouperDataRow DB rows from config;
    // we skip the daemon and create them directly so the analyzer sees the new field / row.
    GrouperDataFieldDao.insertMissingConfigIds(GrouperUtil.toSet(attrFieldConfigId, startFieldConfigId, endFieldConfigId));
    GrouperDataRowDao.insertMissingConfigIds(GrouperUtil.toSet(rowConfigId));

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    long attrFieldId = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get(attrAlias.toLowerCase()).getGrouperDataField().getInternalId();
    long rowId = grouperDataEngine.getGrouperDataProviderIndex()
        .getRowWrapperByLowerAlias().get(rowAlias.toLowerCase()).getGrouperDataRow().getInternalId();
    long startFieldId = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get(startAlias.toLowerCase()).getGrouperDataField().getInternalId();
    long endFieldId = grouperDataEngine.getGrouperDataProviderIndex()
        .getFieldWrapperByLowerAlias().get(endAlias.toLowerCase()).getGrouperDataField().getInternalId();
    long providerId = GrouperDataProviderDao.selectByText("idm").getInternalId();

    // ---- fixture times.

    long now = System.currentTimeMillis();
    long tenDaysAgo         = now - 10L * 24L * 60L * 60L * 1000L;
    long fiveHoursAgo       = now -  5L * 60L * 60L * 1000L;
    long oneDayFromNow      = now +  1L * 24L * 60L * 60L * 1000L;
    long hundredDaysAgo     = now - 100L * 24L * 60L * 60L * 1000L;
    long hundredDaysFromNow = now + 100L * 24L * 60L * 60L * 1000L;

    // ---- attribute values (all four subjects have one value each).

    assignFieldValue(GrouperDataFieldType.timestamp, attrFieldId, tenDaysAgo,         member0.getInternalId(), providerId);
    assignFieldValue(GrouperDataFieldType.timestamp, attrFieldId, fiveHoursAgo,       member1.getInternalId(), providerId);
    assignFieldValue(GrouperDataFieldType.timestamp, attrFieldId, oneDayFromNow,      member2.getInternalId(), providerId);
    assignFieldValue(GrouperDataFieldType.timestamp, attrFieldId, hundredDaysFromNow, member3.getInternalId(), providerId);

    // ---- row values (subjects 0-2 have one enrollment row each; subject 3 has none).
    // subject 0: currently valid   (start 10 days ago, end 100 days from now)
    // subject 1: already expired   (start 100 days ago, end 5 hours ago)
    // subject 2: not yet started   (start 1 day from now, end 100 days from now)
    // subject 3: no row at all     (expect exclusion regardless of window)
    assignTimestampRow(rowId, member0.getInternalId(), providerId,
        startFieldId, tenDaysAgo, endFieldId, hundredDaysFromNow);
    assignTimestampRow(rowId, member1.getInternalId(), providerId,
        startFieldId, hundredDaysAgo, endFieldId, fiveHoursAgo);
    assignTimestampRow(rowId, member2.getInternalId(), providerId,
        startFieldId, oneDayFromNow, endFieldId, hundredDaysFromNow);

    // ---- scripted groups: 6 attribute-family + 1 hasRow. Suffixes encode the expected match.

    Group groupPastMonth = createScriptedGroup(grouperSession, "test:tfnPastMonth",
        "entity.hasAttributeGreaterThan('" + attrAlias + "', timeFromNow('-30 days'))");
    Group groupPastDay = createScriptedGroup(grouperSession, "test:tfnPastDay",
        "entity.hasAttributeGreaterThan('" + attrAlias + "', timeFromNow('-1 days'))");
    Group groupBeforeNow = createScriptedGroup(grouperSession, "test:tfnBeforeNow",
        "entity.hasAttributeLessThan('" + attrAlias + "', timeFromNow('now'))");
    Group groupAfterNow = createScriptedGroup(grouperSession, "test:tfnAfterNow",
        "entity.hasAttributeGreaterThan('" + attrAlias + "', timeFromNow('now'))");
    Group groupNextWeek = createScriptedGroup(grouperSession, "test:tfnNextWeek",
        "entity.hasAttributeBetween(timeFromNow('now') <= " + attrAlias + ", " + attrAlias + " <= timeFromNow('7 days'))");
    Group groupLastWeek = createScriptedGroup(grouperSession, "test:tfnLastWeek",
        "entity.hasAttributeBetween(timeFromNow('-7 days') <= " + attrAlias + ", " + attrAlias + " <= timeFromNow('now'))");
    Group groupCurrentlyValid = createScriptedGroup(grouperSession, "test:tfnCurrentlyValid",
        "entity.hasRow('" + rowAlias + "', "
            + "\"" + startAlias + " <= timeFromNow('now') "
            + "&& " + endAlias + " >= timeFromNow('now')\")");

    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

    // ---- attribute-family assertions.

    // > 30 days ago: all four subjects (even 10 days ago is more recent than 30 days ago)
    Set<Member> pastMonth = groupPastMonth.getMembers();
    assertEquals("hasAttributeGreaterThan('-30 days') should match all 4 subjects", 4, pastMonth.size());
    assertTrue(pastMonth.contains(member0));
    assertTrue(pastMonth.contains(member1));
    assertTrue(pastMonth.contains(member2));
    assertTrue(pastMonth.contains(member3));

    // > 1 day ago: subject 0 (10 days ago) excluded; others included
    Set<Member> pastDay = groupPastDay.getMembers();
    assertEquals("hasAttributeGreaterThan('-1 days') should exclude 10-days-ago subject",
        3, pastDay.size());
    assertFalse("subject with 10-days-ago should NOT match > 1-day-ago", pastDay.contains(member0));
    assertTrue(pastDay.contains(member1));
    assertTrue(pastDay.contains(member2));
    assertTrue(pastDay.contains(member3));

    // < now: subjects in the past
    Set<Member> beforeNow = groupBeforeNow.getMembers();
    assertEquals("hasAttributeLessThan('now') should match past subjects only", 2, beforeNow.size());
    assertTrue(beforeNow.contains(member0));
    assertTrue(beforeNow.contains(member1));

    // > now: subjects in the future
    Set<Member> afterNow = groupAfterNow.getMembers();
    assertEquals("hasAttributeGreaterThan('now') should match future subjects only", 2, afterNow.size());
    assertTrue(afterNow.contains(member2));
    assertTrue(afterNow.contains(member3));

    // between now and +7 days: subject 2 (1 day from now); subject 3 (100 days) is outside window
    Set<Member> nextWeek = groupNextWeek.getMembers();
    assertEquals("hasAttributeBetween(now..+7 days) should match only the 1-day-from-now subject",
        1, nextWeek.size());
    assertTrue(nextWeek.contains(member2));

    // between -7 days and now: subject 1 (5 hours ago); subject 0 (10 days ago) is outside window
    Set<Member> lastWeek = groupLastWeek.getMembers();
    assertEquals("hasAttributeBetween(-7 days..now) should match only the 5-hours-ago subject",
        1, lastWeek.size());
    assertTrue(lastWeek.contains(member1));

    // ---- hasRow assertion.

    Set<Member> currentlyValid = groupCurrentlyValid.getMembers();
    assertEquals("hasRow with timeFromNow('now') bounds should match only the currently-valid subject",
        1, currentlyValid.size());
    assertTrue("expected the subject with (10 days ago .. 100 days from now)",
        currentlyValid.contains(member0));
    assertFalse(currentlyValid.contains(member1));
    assertFalse(currentlyValid.contains(member2));
    assertFalse(currentlyValid.contains(member3));
  }

  /**
   * Register a single timestamp-typed field with fieldDataStructure=rowColumn — i.e., a column
   * on a data row rather than a standalone attribute. Values go through the same timestamp
   * assignValueHelper as attribute-typed timestamp fields.
   */
  private static void registerTimestampRowColumn(String fieldConfigId, String alias) {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + fieldConfigId + ".fieldAliases").value(alias).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + fieldConfigId + ".fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + fieldConfigId + ".fieldDataType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + fieldConfigId + ".fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField." + fieldConfigId + ".descriptionHtml").value("<b>test rowColumn timestamp</b>").store();
  }

  /**
   * Insert one row assign + two timestamp row-field assigns via the same DAO layer the data
   * provider daemon uses. Values are set via GrouperDataFieldType.timestamp.assignValue so the
   * timestamp code path runs (Long → value_integer, matching what a real TIMESTAMPTZ provider
   * query would eventually produce).
   */
  private static void assignTimestampRow(long rowInternalId, long memberInternalId, long providerId,
      long startFieldId, long startMillis, long endFieldId, long endMillis) {
    GrouperDataRowAssign rowAssign = new GrouperDataRowAssign();
    rowAssign.setDataRowInternalId(rowInternalId);
    rowAssign.setMemberInternalId(memberInternalId);
    rowAssign.setDataProviderInternalId(providerId);
    GrouperDataRowAssignDao.store(rowAssign);

    GrouperDataRowFieldAssign startField = new GrouperDataRowFieldAssign();
    startField.setDataRowAssignInternalId(rowAssign.getInternalId());
    startField.setDataFieldInternalId(startFieldId);
    GrouperDataFieldType.timestamp.assignValue(startField, startMillis, null);

    GrouperDataRowFieldAssign endField = new GrouperDataRowFieldAssign();
    endField.setDataRowAssignInternalId(rowAssign.getInternalId());
    endField.setDataFieldInternalId(endFieldId);
    GrouperDataFieldType.timestamp.assignValue(endField, endMillis, null);

    GrouperDataRowFieldAssignDao.store(startField);
    GrouperDataRowFieldAssignDao.store(endField);
  }


}
