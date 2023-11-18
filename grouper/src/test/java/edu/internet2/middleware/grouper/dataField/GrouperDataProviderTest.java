package edu.internet2.middleware.grouper.dataField;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderChangeLogQuery;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSyncType;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.config.SubjectConfig;
import edu.internet2.middleware.subject.provider.SourceManager;
import junit.textui.TestRunner;


public class GrouperDataProviderTest extends GrouperTest {

  public GrouperDataProviderTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataProviderTest("testFullJobDates"));
  }

  public void setUp() {
    super.setUp();
    createTableAffiliation();
    createTableAttributes();
    createTableAttributesMulti();
    createTableChangeLog();

  }
  

  protected void tearDown() {

    SubjectConfig.retrieveConfig().propertiesOverrideMap().clear();
    GrouperClientConfig.retrieveConfig().propertiesOverrideMap().clear();
    SourceManager.getInstance().internal_removeSource("personLdapSource");

    super.tearDown();
  }
  
  /**
   * 
   */
  public void testInsert() {
    Long internalId = GrouperDataProviderDao.findOrAdd("test");
    System.out.println(internalId);
  }
  
  /**
   * 
   */
  public void testSqlProviderFull() {
    internal_testSqlProvider(GrouperDataProviderSyncType.fullSyncFull);
  }
  
  /**
   * 
   */
  public void testSqlProviderIncremental() {
    internal_testSqlProvider(GrouperDataProviderSyncType.incrementalSyncChangeLog);
  }
  
  /**
   * 
   */
  public void testLdapProviderFull() {
    internal_testLdapProvider(GrouperDataProviderSyncType.fullSyncFull);
  }
  
  /**
   * 
   */
  public void testLdapProviderIncremental() {
    internal_testLdapProvider(GrouperDataProviderSyncType.incrementalSyncChangeLog);
  }
  
  public void testFullJobDates() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_idm");
    GcGrouperSyncJob gcGrouperSyncFullJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    assertNotNull(gcGrouperSyncFullJob.getLastSyncStart());
    assertNotNull(gcGrouperSyncFullJob.getLastSyncTimestamp());
    assertTrue(gcGrouperSyncFullJob.getLastSyncStart().getTime() == gcGrouperSyncFullJob.getLastSyncTimestamp().getTime());
    
    // force a failure - want to make sure lastSyncTimestamp doesn't get updated.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryType").value("bogus").store();

    try {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Was trying to force an exception but there was no exception!");
    } catch (Exception e) {
      // good
    }
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_idm");
    GcGrouperSyncJob gcGrouperSyncFullJob2 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    assertTrue(gcGrouperSyncFullJob2.getLastSyncStart().getTime() > gcGrouperSyncFullJob.getLastSyncStart().getTime());
    assertTrue(gcGrouperSyncFullJob2.getLastSyncTimestamp().getTime() == gcGrouperSyncFullJob.getLastSyncTimestamp().getTime());    
  }
  
  public void testIncrementalJobDates() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();
    
    GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_idm");
    GcGrouperSyncJob gcGrouperSyncIncrementalJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    assertNotNull(gcGrouperSyncIncrementalJob.getLastSyncStart());
    assertNotNull(gcGrouperSyncIncrementalJob.getLastSyncTimestamp());
    assertTrue(gcGrouperSyncIncrementalJob.getLastSyncStart().getTime() == gcGrouperSyncIncrementalJob.getLastSyncTimestamp().getTime());
    
    // force a failure - want to make sure lastSyncTimestamp doesn't get updated.
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      // ignore
    }
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryType").value("bogus").store();

    try {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Was trying to force an exception but there was no exception!");
    } catch (Exception e) {
      // good
    }
    
    gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_idm");
    GcGrouperSyncJob gcGrouperSyncIncrementalJob2 = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    assertTrue(gcGrouperSyncIncrementalJob2.getLastSyncStart().getTime() > gcGrouperSyncIncrementalJob.getLastSyncStart().getTime());
    assertTrue(gcGrouperSyncIncrementalJob2.getLastSyncTimestamp().getTime() == gcGrouperSyncIncrementalJob.getLastSyncTimestamp().getTime());    
  }
  
  public void testIncrementalChangeLogDates() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlQuery").value("select id, subject_id, create_timestamp1 from testgrouper_dp_changelog").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryPrimaryKeyAttribute").value("id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryTimestampAttribute").value("create_timestamp1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectSourceId").value("jdbc").store();
    
    List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
    
    Date date1 = new Date();
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      // ignore
    }
    Date date2 = new Date();
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      // ignore
    }
    Date date3 = new Date();
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      // ignore
    }
    Date date4 = new Date();
    try {
      Thread.sleep(2);
    } catch (InterruptedException e) {
      // ignore
    }

    batchBindVarsChangeLog.add(GrouperUtil.toList(1, "user1", date1, null));
    batchBindVarsChangeLog.add(GrouperUtil.toList(2, "user2", date2, null));
    batchBindVarsChangeLog.add(GrouperUtil.toList(3, "user3", date3, null));
    batchBindVarsChangeLog.add(GrouperUtil.toList(4, "user4", date4, null));
    
    new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1, create_timestamp2) values (?, ?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
    
    GrouperDataProviderSync grouperDataProviderSync = GrouperDataProviderSync.retrieveDataProviderSync("idm");
    grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    GrouperDataProviderChangeLogQuery changeLogQuery = grouperDataProviderSync.retrieveGrouperDataProviderChangeLogQueries().iterator().next();
    List<Object[]> rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(System.currentTimeMillis()));
    assertEquals(4, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(date3.getTime()));
    assertEquals(3, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(date3.getTime() - 1L));
    assertEquals(2, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), new Timestamp(date2.getTime()), new Timestamp(System.currentTimeMillis()));
    assertEquals(2, rows.size());
    
    // now use the integer field instead of date
    new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlQuery").value("select id, subject_id, create_timestamp2 from testgrouper_dp_changelog").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryTimestampAttribute").value("create_timestamp2").store();
    
    grouperDataProviderSync = GrouperDataProviderSync.retrieveDataProviderSync("idm");
    grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    changeLogQuery = grouperDataProviderSync.retrieveGrouperDataProviderChangeLogQueries().iterator().next();
    
    batchBindVarsChangeLog.clear();
    batchBindVarsChangeLog.add(GrouperUtil.toList(1, "user1", null, date1.getTime()));
    batchBindVarsChangeLog.add(GrouperUtil.toList(2, "user2", null, date2.getTime()));
    batchBindVarsChangeLog.add(GrouperUtil.toList(3, "user3", null, date3.getTime()));
    batchBindVarsChangeLog.add(GrouperUtil.toList(4, "user4", null, date4.getTime()));
    
    new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1, create_timestamp2) values (?, ?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(System.currentTimeMillis()));
    assertEquals(4, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(date3.getTime()));
    assertEquals(3, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), null, new Timestamp(date3.getTime() - 1L));
    assertEquals(2, rows.size());
    
    rows = changeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(new HashMap<>(), new Timestamp(date2.getTime()), new Timestamp(System.currentTimeMillis()));
    assertEquals(2, rows.size());
  }
  
  /**
   * 
   */
  private void internal_testSqlProvider(GrouperDataProviderSyncType syncType) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
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

    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();
        
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled, hasTwoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldPrivacyRealm").value("public").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    
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
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(7, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(9, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());

    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      // nothing would have happened since the change log wasn't populated
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
      
      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();

      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(2, "test.subject.1", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(3, "test.subject.2", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(4, "test.subject.3", new Date()));
      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
      
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");  
    }
    
    // check synced data
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 234").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    long rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // change sync data (insert, update, delete)
    
    // check synced data
    
    
    // abac data
    String abac = "entity.hasAttribute('affiliationCode', 'staf') || entity.hasAttribute('affiliationCode', 'stu')";
//    abac = "entity.hasAttribute('twoStepEnrolled')";
//    abac = "entity.hasRow('affiliation', \"affiliationCode !='alumni / alumnae' && affiliationActive && affiliationOrg==engl\")";

    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    
    testGroup2.addMember(testSubject1);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");

    AttributeAssignResult markerAttributeResult = testGroup2.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup3.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup4.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "${entity.memberOf('test:testGroup2')}");

    
    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup3)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), 
        "${entity.hasAttribute('jobNumber', '456') || entity.hasAttribute('active', 'false')}");

    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup4)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), 
        "${entity.hasRow('affiliation', 'affiliationActive && affiliationOrg == math')}");

    GrouperLoaderJexlScriptFullSync.runDaemonStandalone();

    assertEquals(1, testGroup.getMembers().size());
    assertTrue(testGroup.hasMember(testSubject1));

    assertEquals(2, testGroup3.getMembers().size());
    assertTrue(testGroup3.hasMember(testSubject3));
    assertTrue(testGroup3.hasMember(testSubject1));

    assertEquals(2, testGroup4.getMembers().size());
    assertTrue(testGroup4.hasMember(testSubject3));
    assertTrue(testGroup4.hasMember(testSubject0));


    // make some updates in db - update single valued attribute, update multi-valued attribute, and update affiliation in row data
    new GcDbAccess().sql("update testgrouper_field_attr set two_step_enrolled='T' where subject_id='test.subject.0'").executeBatchSql();
    new GcDbAccess().sql("update testgrouper_field_attr_multi set attribute_value='999' where subject_id='test.subject.0' and attribute_value='234'").executeBatchSql();
    new GcDbAccess().sql("update testgrouper_field_row_affil set affiliation_code='faculty' where subject_id='test.subject.0' and affiliation_code='staff'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    
    // make some updates in db - update another field in row data
    new GcDbAccess().sql("update testgrouper_field_row_affil set org='english' where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // make some updates in db - null a field
    new GcDbAccess().sql("update testgrouper_field_row_affil set org=null where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals(0, new GcDbAccess().sql("select count(*) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // make some updates in db - bring value back from null
    new GcDbAccess().sql("update testgrouper_field_row_affil set org='english' where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // make some updates in db - update a boolean
    new GcDbAccess().sql("update testgrouper_field_row_affil set active='F' where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // delete a row
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // add row back
    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values('test.subject.0', 'faculty', 'F', 'english')").executeBatchSql();
    
    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "test.subject.0", new Date()));      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();   
    }
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
  }

  /**
   * 
   */
  public void testSqlProviderUsingSubjectIdentifier() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();

    batchBindVars.add(GrouperUtil.toList("id.test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.3", "F", "F", "F"));
    
    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();
    
    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("id.test.subject.0", "123"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.0", "234"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.1", "123"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.1", "456"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.2", "234"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.3", "789"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.3", "456"));

    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values (?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("id.test.subject.0", "staff", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.0", "alum", "T", "math"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.1", "stu", "F", "comp"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.1", "contr", "T", "phys"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.2", "staff", "F", "span"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.3", "fac", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("id.test.subject.3", "emer", "T", "math"));

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();
        
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled, hasTwoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldPrivacyRealm").value("public").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlQuery").value("select subject_id, active, two_step_enrolled, employee from testgrouper_field_attr").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataStructure").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySubjectIdType").value("subjectIdentifier").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySubjectIdType").value("subjectIdentifier").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryNumberOfDataFields").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldConfigId").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataField.0.providerDataFieldAttribute").value("job_number").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlQuery").value("select subject_id, affiliation_code, active, org from testgrouper_field_row_affil").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdType").value("subjectIdentifier").store();
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
        
    // load data
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(7, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(9, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());

    
    // check synced data
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 234").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    long rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // change sync data (insert, update, delete)
    
    // check synced data
    
    
    // abac data
    String abac = "entity.hasAttribute('affiliationCode', 'staf') || entity.hasAttribute('affiliationCode', 'stu')";
//    abac = "entity.hasAttribute('twoStepEnrolled')";
//    abac = "entity.hasRow('affiliation', \"affiliationCode !='alumni / alumnae' && affiliationActive && affiliationOrg==engl\")";

    
    Group testGroup = new GroupSave().assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup2 = new GroupSave().assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup3 = new GroupSave().assignName("test:testGroup3").assignCreateParentStemsIfNotExist(true).save();
    Group testGroup4 = new GroupSave().assignName("test:testGroup4").assignCreateParentStemsIfNotExist(true).save();
    
    Subject testSubject0 = SubjectFinder.findById("test.subject.0", true);
    Subject testSubject1 = SubjectFinder.findById("test.subject.1", true);
    Subject testSubject2 = SubjectFinder.findById("test.subject.2", true);
    Subject testSubject3 = SubjectFinder.findById("test.subject.3", true);
    
    testGroup2.addMember(testSubject1);
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");

    AttributeAssignResult markerAttributeResult = testGroup2.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup3.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    markerAttributeResult = testGroup4.getAttributeDelegate().assignAttribute(AttributeDefNameFinder.findByName(SqlCacheGroup.attributeDefNameMarkerName(), true));
    markerAttributeResult.getAttributeAssign().getAttributeValueDelegate().assignValueString(SqlCacheGroup.attributeDefNameNameListName(), "members");

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_consumer_sqlCacheGroup");

    AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
    AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
    
    AttributeAssign attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "${entity.memberOf('test:testGroup2')}");

    
    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup3)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), 
        "${entity.hasAttribute('jobNumber', '456') || entity.hasAttribute('active', 'false')}");

    attributeAssign = new AttributeAssignSave(grouperSession).assignOwnerGroup(testGroup4)
        .assignAttributeDefName(attributeDefNameMarker).save();
    
    attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), 
        "${entity.hasRow('affiliation', 'affiliationActive && affiliationOrg == math')}");

    GrouperLoaderJexlScriptFullSync.runDaemonStandalone();

    assertEquals(1, testGroup.getMembers().size());
    assertTrue(testGroup.hasMember(testSubject1));

    assertEquals(2, testGroup3.getMembers().size());
    assertTrue(testGroup3.hasMember(testSubject3));
    assertTrue(testGroup3.hasMember(testSubject1));

    assertEquals(2, testGroup4.getMembers().size());
    assertTrue(testGroup4.hasMember(testSubject3));
    assertTrue(testGroup4.hasMember(testSubject0));


    // make some updates in db - update single valued attribute, update multi-valued attribute, and update affiliation in row data
    new GcDbAccess().sql("update testgrouper_field_attr set two_step_enrolled='T' where subject_id='id.test.subject.0'").executeBatchSql();
    new GcDbAccess().sql("update testgrouper_field_attr_multi set attribute_value='999' where subject_id='id.test.subject.0' and attribute_value='234'").executeBatchSql();
    new GcDbAccess().sql("update testgrouper_field_row_affil set affiliation_code='faculty' where subject_id='id.test.subject.0' and affiliation_code='staff'").executeBatchSql();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    
    // make some updates in db - update another field in row data
    new GcDbAccess().sql("update testgrouper_field_row_affil set org='english' where subject_id='id.test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // make some updates in db - null a field
    new GcDbAccess().sql("update testgrouper_field_row_affil set org=null where subject_id='id.test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals(0, new GcDbAccess().sql("select count(*) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    // make some updates in db - bring value back from null
    new GcDbAccess().sql("update testgrouper_field_row_affil set org='english' where subject_id='id.test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // make some updates in db - update a boolean
    new GcDbAccess().sql("update testgrouper_field_row_affil set active='F' where subject_id='id.test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // delete a row
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='id.test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
    // add row back
    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values('id.test.subject.0', 'faculty', 'F', 'english')").executeBatchSql();
    
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 999").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'faculty'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());


    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'alum'").select(long.class);

    assertEquals("math", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());
    
  }
  
  /**
   * 
   */
  public void internal_testLdapProvider(GrouperDataProviderSyncType syncType) {
    
    GrouperSession.startRootSession();
    
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    LdapProvisionerTestUtils.setupSubjectSource();
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    } else {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderIncrementalSyncJob").store();
    }
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("ldap").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();
        
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation.fieldAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation.fieldDataType").value("string").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldAliases").value("businessCategory").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldDataType").value("string").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldPrivacyRealm").value("public").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.ldap.name").value("ldap").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerConfigId").value("ldap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryType").value("ldap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryLdapConfigId").value("personLdap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryLdapBaseDn").value("ou=People,dc=example,dc=edu").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryLdapSearchScope").value("SUBTREE_SCOPE").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryLdapFilter").value("(|(uid=a-jbutler985)(uid=a-kmartinez977)(uid=a-jvales975)(uid=a-ngonazles)(uid=banderson))").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQuerySubjectIdAttribute").value("uid").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQuerySubjectSourceId").value("personLdapSource").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryNumberOfDataFields").value("2").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.0.providerDataFieldConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.0.providerDataFieldAttribute").value("eduPersonAffiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.1.providerDataFieldConfigId").value("businessCategory").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.ldapAttrs.providerQueryDataField.1.providerDataFieldAttribute").value("businessCategory").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerConfigId").value("ldap").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySqlQuery").value("select id, subject_id, create_timestamp1 from testgrouper_dp_changelog").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryPrimaryKeyAttribute").value("id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQueryTimestampAttribute").value("create_timestamp1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectIdType").value("subjectIdentifier").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderChangeLogQuery.cl1.providerChangeLogQuerySubjectSourceId").value("personLdapSource").store();
    
    // load data
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());

    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      // nothing would have happened since the change log wasn't populated
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());
      
      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1, "a-jbutler985", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(2, "a-kmartinez977", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(3, "a-jvales975", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(4, "a-ngonazles", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(5, "banderson", new Date()));
      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
      
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1"); 
    }
    
    // check synced data
    
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-kmartinez977'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'affiliation' and value_text = 'faculty'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'affiliation' and value_text = 'alum'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'businessCategory' and value_text = 'Account Payable'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'affiliation' and value_text = 'community'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'businessCategory' and value_text = 'Language Arts'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-kmartinez977' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-kmartinez977' and data_field_config_id = 'businessCategory' and value_text = 'Account Payable'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'affiliation' and value_text = 'student'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'businessCategory' and value_text = 'Purchasing'").select(int.class).intValue());
        
    // now try updates
    
    // make some updates in ldap
    List<LdapModificationItem> ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonAffiliation", "staff")));
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("eduPersonAffiliation", "member")));
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("businessCategory", "Something")));
    new LdapSyncDaoForLdap().modify("personLdap", "uid=banderson,ou=People,dc=example,dc=edu", ldapModificationItems);
    
    ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("eduPersonAffiliation")));
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("businessCategory")));
    new LdapSyncDaoForLdap().modify("personLdap", "uid=a-kmartinez977,ou=People,dc=example,dc=edu", ldapModificationItems);

    ldapModificationItems = new ArrayList<LdapModificationItem>();
    ldapModificationItems.add(new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, new LdapAttribute("businessCategory", "Something else")));
    new LdapSyncDaoForLdap().modify("personLdap", "uid=a-ngonazles,ou=People,dc=example,dc=edu", ldapModificationItems);

    if (syncType == GrouperDataProviderSyncType.incrementalSyncChangeLog) {
      new GcDbAccess().sql("delete from testgrouper_dp_changelog").executeSql();

      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      //batchBindVarsChangeLog.add(GrouperUtil.toList(1, "a-jbutler985", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(2, "a-kmartinez977", new Date()));
      //batchBindVarsChangeLog.add(GrouperUtil.toList(3, "a-jvales975", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(4, "a-ngonazles", new Date()));
      batchBindVarsChangeLog.add(GrouperUtil.toList(5, "banderson", new Date()));
      
      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();    
    }
    
    // load data updates
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());

    
    // check synced data
    
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-kmartinez977'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'affiliation' and value_text = 'faculty'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'affiliation' and value_text = 'alum'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles' and data_field_config_id = 'businessCategory' and value_text = 'Something else'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'affiliation' and value_text = 'community'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975' and data_field_config_id = 'businessCategory' and value_text = 'Language Arts'").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'affiliation' and value_text = 'student'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985' and data_field_config_id = 'businessCategory' and value_text = 'Purchasing'").select(int.class).intValue());
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson' and data_field_config_id = 'affiliation' and value_text = 'member'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson' and data_field_config_id = 'affiliation' and value_text = 'staff'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson' and data_field_config_id = 'businessCategory' and value_text = 'Something'").select(int.class).intValue());
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAffiliation() {
  
    String tableName = "testgrouper_field_row_affil";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "affiliation_code", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "active", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "org", Types.VARCHAR, "40", false, false);
        }
        
      });
    }
    new GcDbAccess().sql("delete from " + tableName).executeSql();
    
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAttributes() {
  
    String tableName = "testgrouper_field_attr";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "active", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "two_step_enrolled", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "employee", Types.VARCHAR, "1", false, true);
        }
        
      });
    }
    new GcDbAccess().sql("delete from " + tableName).executeSql();

  }
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAttributesMulti() {
  
    String tableName = "testgrouper_field_attr_multi";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "attribute_value", Types.VARCHAR, "100", false, true);
        }
        
      });
    }
    new GcDbAccess().sql("delete from " + tableName).executeSql();

  }
  
  private static void createTableChangeLog() {
    
    String tableName = "testgrouper_dp_changelog";
    
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
    
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          
          Database database = ddlVersionBean.getDatabase();
    
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "id", Types.BIGINT, "20", true, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "create_timestamp1", Types.TIMESTAMP, null, false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "create_timestamp2", Types.BIGINT, "20", false, false);
        }
        
      });
    }
    new GcDbAccess().sql("delete from " + tableName).executeSql();

  }
}
