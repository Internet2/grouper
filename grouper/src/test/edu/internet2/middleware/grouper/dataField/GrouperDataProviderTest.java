package edu.internet2.middleware.grouper.dataField;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderChangeLogQuery;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderLogic;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSync;
import edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSyncType;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.ldapProvisioning.ldapSyncDao.LdapSyncDaoForLdap;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
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
    TestRunner.run(new GrouperDataProviderTest("testSetSparseDataRowForUi"));
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
    assertNotNull(internalId);
  }

  /**
   * Tiny, self-contained test that leaves test.subject.0 with exactly two "affiliation" data rows
   * where the "affiliationOrg" field is SPARSE - populated on the student row and null on the staff
   * row.  It does a full provider sync (which commits to the DB and correctly handles the text/dictionary
   * values) and then STOPS without any cleanup, so the data persists for viewing in the UI.
   *
   * After running this, start the UI and view subject "test.subject.0".  On the data field assignments
   * page you should see an "affiliation" data-row table with columns affiliationCode / affiliationActive /
   * affiliationOrg and two rows:
   *   staff   / true  / -        (no org)
   *   student / true  / wharton  (org set)
   * Before the fix, the "wharton" org rendered against the staff row; after the fix it stays on the
   * student row.
   */
  public void testSetSparseDataRowForUi() {

    GrouperSession.startRootSession();

    // two affiliation rows for the same subject; the staff row intentionally has a NULL org so that
    // affiliationOrg is a sparse column (null on one row, set on another) - the exact bug scenario
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff",   "T", null));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "student", "T", "wharton"));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    // run this provider as a full sync daemon
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    // public privacy realm so the fields/rows are viewable in the UI
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    // the three columns of the affiliation data row (rowColumn structure)
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.descriptionHtml").value("<b>affiliation code</b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.descriptionHtml").value("<b>affiliation active</b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.descriptionHtml").value("<b>affiliation org</b>").store();

    // the affiliation data row, keyed by affiliationCode so the two rows stay distinct
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("<b>affiliation</b>").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePitDays").value("800").store();

    // the provider and its row query over the mock source table
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();

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

    // do the full sync - this commits the row assignments (and dictionary text values) to the DB
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // sanity check: two affiliation rows for the subject
    assertEquals(2, new GcDbAccess().sql(
        "select count(1) from grouper_data_row_assign_v gdrav where gdrav.data_row_config_id = 'affiliation' and gdrav.subject_id = 'test.subject.0'")
        .select(int.class).intValue());

    // affiliationOrg 'wharton' must be tied to the row whose affiliationCode is 'student', never 'staff'
    assertEquals("student", new GcDbAccess().sql(
        "select code_v.value_text from grouper_data_row_field_asgn_v org_v, grouper_data_row_field_asgn_v code_v "
        + "where org_v.data_row_assign_internal_id = code_v.data_row_assign_internal_id "
        + "and org_v.data_field_config_id = 'affiliationOrg' and org_v.value_text = 'wharton' "
        + "and code_v.data_field_config_id = 'affiliationCode'")
        .select(String.class));

    // NOTE: intentionally no cleanup - the config and row assignments are left in the DB so you can
    // start the UI and view subject "test.subject.0" to eyeball the data field assignments page.
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
  public void testSqlProviderOneRowPerSubjectFull() {
    internal_testSqlProviderOneRowPerSubject(GrouperDataProviderSyncType.fullSyncFull);
  }
  
  /**
   * 
   */
  public void testSqlProviderOneRowPerSubjectIncremental() {
    internal_testSqlProviderOneRowPerSubject(GrouperDataProviderSyncType.incrementalSyncChangeLog);
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
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();

    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));

    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "F", "F", "F"));

    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();
    
    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", " "));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "456"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "789"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "456"));
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "456"));

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
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "emer", "T", "math"));

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled, hasTwoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.descriptionHtml").value("<b>description html </b>").store();

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePitDays").value("800").store();

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
    
    long startTimeMicros = System.currentTimeMillis() * 1000L;
    
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
      
      // bad subject shouldn't cause the load to fail
      batchBindVarsChangeLog.add(GrouperUtil.toList(5, "test.subject.bogus", new Date()));

      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
      
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");  
    }
    
    long afterFirstSyncMicros = System.currentTimeMillis() * 1000L;
    
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

    // no history data
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    
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

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

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

    Member testMember0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), testSubject0, false);
    GrouperDataField jobNumberDataField = GrouperDataFieldDao.selectByText("jobNumber");
    GrouperDataField twoStepDataField = GrouperDataFieldDao.selectByText("twoStep");
    GrouperDataField affiliationCodeDataField = GrouperDataFieldDao.selectByText("affiliationCode");
    GrouperDataField affiliationActiveDataField = GrouperDataFieldDao.selectByText("affiliationActive");
    GrouperDataField affiliationOrgDataField = GrouperDataFieldDao.selectByText("affiliationOrg");
    GrouperDataRow affiliationDataRow = GrouperDataRowDao.selectByConfigId("affiliation");
    long testMember0StaffAffiliationDataRowAssignId = new GcDbAccess().sql("select gdra.internal_id from grouper_data_row_assign gdra, grouper_data_row_field_assign gdrfa, grouper_dictionary gd where gdra.internal_id=gdrfa.data_row_assign_internal_id and gd.internal_id=gdrfa.value_dictionary_internal_id and gdra.member_internal_id=? and gdrfa.data_field_internal_id=? and gd.the_text='staff'").addBindVar(testMember0.getInternalId()).addBindVar(affiliationCodeDataField.getInternalId()).select(long.class);

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
    
    long afterSecondSyncMicros = System.currentTimeMillis() * 1000L;

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

    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    GrouperDataFieldAssignHst grouperDataFieldAssignHst1 = GrouperDataFieldAssignHstDao.selectByDataFieldInternalId(jobNumberDataField.getInternalId()).iterator().next();
    GrouperDataFieldAssignHst grouperDataFieldAssignHst2 = GrouperDataFieldAssignHstDao.selectByDataFieldInternalId(twoStepDataField.getInternalId()).iterator().next();
    
    assertEquals(testMember0.getInternalId().longValue(), grouperDataFieldAssignHst1.getMemberInternalId());
    assertEquals(234L, grouperDataFieldAssignHst1.getValueInteger().longValue());
    assertNull(grouperDataFieldAssignHst1.getValueDictionaryInternalId());
    assertTrue(grouperDataFieldAssignHst1.getStartTime() > startTimeMicros && grouperDataFieldAssignHst1.getStartTime() < afterFirstSyncMicros);
    assertTrue(grouperDataFieldAssignHst1.getEndTime() > afterFirstSyncMicros && grouperDataFieldAssignHst1.getEndTime() < afterSecondSyncMicros);
    
    assertEquals(testMember0.getInternalId().longValue(), grouperDataFieldAssignHst2.getMemberInternalId());
    assertEquals(0L, grouperDataFieldAssignHst2.getValueInteger().longValue());
    assertNull(grouperDataFieldAssignHst2.getValueDictionaryInternalId());
    assertTrue(grouperDataFieldAssignHst2.getStartTime() > startTimeMicros && grouperDataFieldAssignHst2.getStartTime() < afterFirstSyncMicros);
    assertTrue(grouperDataFieldAssignHst2.getEndTime() > afterFirstSyncMicros && grouperDataFieldAssignHst2.getEndTime() < afterSecondSyncMicros);

    GrouperDataRowAssignHst grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).iterator().next();
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0StaffAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > startTimeMicros && grouperDataRowAssignHst.getStartTime() < afterFirstSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterFirstSyncMicros && grouperDataRowAssignHst.getEndTime() < afterSecondSyncMicros);

    GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).iterator().next();
    GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst2 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationOrgDataField.getInternalId()).iterator().next();
    GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).iterator().next();

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("staff").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst2.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("engl").getInternalId(), grouperDataRowFieldAssignHst2.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst2.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(1, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    

    
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
    
    long afterThirdSyncMicros = System.currentTimeMillis() * 1000L;

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

    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    long testMember0FacultyAffiliationDataRowAssignId = new GcDbAccess().sql("select gdra.internal_id from grouper_data_row_assign gdra, grouper_data_row_field_assign gdrfa, grouper_dictionary gd where gdra.internal_id=gdrfa.data_row_assign_internal_id and gd.internal_id=gdrfa.value_dictionary_internal_id and gdra.member_internal_id=? and gdrfa.data_field_internal_id=? and gd.the_text='faculty'").addBindVar(testMember0.getInternalId()).addBindVar(affiliationCodeDataField.getInternalId()).select(long.class);

    grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0FacultyAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > afterFirstSyncMicros && grouperDataRowAssignHst.getStartTime() < afterSecondSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterSecondSyncMicros && grouperDataRowAssignHst.getEndTime() < afterThirdSyncMicros);

    grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst2 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationOrgDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("faculty").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst2.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("engl").getInternalId(), grouperDataRowFieldAssignHst2.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst2.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(1, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    
    
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
    
    long afterForthSyncMicros = System.currentTimeMillis() * 1000L;

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

    
    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(9, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0FacultyAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > afterSecondSyncMicros && grouperDataRowAssignHst.getStartTime() < afterThirdSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterThirdSyncMicros && grouperDataRowAssignHst.getEndTime() < afterForthSyncMicros);

    grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst2 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationOrgDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("faculty").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst2.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("english").getInternalId(), grouperDataRowFieldAssignHst2.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst2.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(1, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    
    // using a blank value doesn't change anything
    new GcDbAccess().sql("update testgrouper_field_row_affil set org=' ' where subject_id='test.subject.0' and affiliation_code='faculty'").executeBatchSql();
    
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
    
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    
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
    
    long afterFifthSyncMicros = System.currentTimeMillis() * 1000L;
    
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
    
    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(11, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0FacultyAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > afterThirdSyncMicros && grouperDataRowAssignHst.getStartTime() < afterForthSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterForthSyncMicros && grouperDataRowAssignHst.getEndTime() < afterFifthSyncMicros);

    grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("faculty").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(1, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    
    
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
    
    long afterSixthSyncMicros = System.currentTimeMillis() * 1000L;

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

    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(14, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0FacultyAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > afterForthSyncMicros && grouperDataRowAssignHst.getStartTime() < afterFifthSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterFifthSyncMicros && grouperDataRowAssignHst.getEndTime() < afterSixthSyncMicros);

    grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst2 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationOrgDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("faculty").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst2.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("english").getInternalId(), grouperDataRowFieldAssignHst2.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst2.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(1, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    
    
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
    
    long afterSeventhSyncMicros = System.currentTimeMillis() * 1000L;

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
    
    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(17, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
    assertEquals(affiliationDataRow.getInternalId(), grouperDataRowAssignHst.getDataRowInternalId());
    assertEquals(testMember0FacultyAffiliationDataRowAssignId, grouperDataRowAssignHst.getDataRowAssignInternalId());
    assertTrue(grouperDataRowAssignHst.getStartTime() > afterFifthSyncMicros && grouperDataRowAssignHst.getStartTime() < afterSixthSyncMicros);
    assertTrue(grouperDataRowAssignHst.getEndTime() > afterSixthSyncMicros && grouperDataRowAssignHst.getEndTime() < afterSeventhSyncMicros);

    grouperDataRowFieldAssignHst1 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationCodeDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst2 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationOrgDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);
    grouperDataRowFieldAssignHst3 = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalId(affiliationActiveDataField.getInternalId()).stream().max(Comparator.comparingLong(GrouperDataRowFieldAssignHst::getInternalId)).orElse(null);

    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst1.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("faculty").getInternalId(), grouperDataRowFieldAssignHst1.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst1.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst2.getDataRowAssignInternalId());
    assertEquals(GrouperDictionaryDao.selectByText("english").getInternalId(), grouperDataRowFieldAssignHst2.getValueDictionaryInternalId().longValue());
    assertNull(grouperDataRowFieldAssignHst2.getValueInteger());
   
    assertEquals(grouperDataRowAssignHst.getInternalId(), grouperDataRowFieldAssignHst3.getDataRowAssignInternalId());
    assertNull(grouperDataRowFieldAssignHst3.getValueDictionaryInternalId());
    assertEquals(0, grouperDataRowFieldAssignHst3.getValueInteger().longValue());
    
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
    
    // check history tables
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
    assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
    assertEquals(17, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    
    for (String action : new String[] {"addDataFieldAssign", "deleteDataFieldAssign", "addDataRowFieldAssign",
        "addDataRowAssign", "deleteDataRowAssign", "deleteDataRowFieldAssign"}) {
      
      int count = new GcDbAccess().sql("select count(1) from grouper_change_log_entry_v gclev where gclev.action_name = ?").addBindVar(action).select(int.class);
      
      assertTrue(action, count > 0);
    }
    
    
    Set<String> configIdsToInsert = GrouperUtil.toSet("configId1", "configId2");
    GrouperDataFieldDao.insertMissingConfigIds(configIdsToInsert);
    
    Set<GrouperDataField> insertedGrouperDataFields = GrouperDataFieldDao.selectByTexts(configIdsToInsert);
    
    int i = 0;
    for (GrouperDataField insertedGrouperDataField: insertedGrouperDataFields) {
      List<List<Object>> batchBindVarsChangeLog = new ArrayList<List<Object>>();
      batchBindVarsChangeLog.add(GrouperUtil.toList(1000 + i, insertedGrouperDataField.getInternalId(), insertedGrouperDataField.getConfigId() + "_name", 
          insertedGrouperDataField.getConfigId() + "_lower_name", "F", new Date() ));  
      new GcDbAccess().sql("insert into grouper_data_alias (internal_id, data_field_internal_id, name, lower_name, alias_type, created_on) "
          + "values (?, ?, ?, ?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
      i++;
    }
    
    i = 0;
    
    Long dataProviderInternalId = new GcDbAccess().sql("select internal_id from grouper_data_provider limit 1").select(long.class);
    
    for (GrouperDataField insertedGrouperDataField: insertedGrouperDataFields) {
      GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
      grouperDataFieldAssign.setDataFieldInternalId(insertedGrouperDataField.getInternalId());
      grouperDataFieldAssign.setCreatedOn(new Timestamp(System.currentTimeMillis()));
      grouperDataFieldAssign.setDataProviderInternalId(dataProviderInternalId);
      grouperDataFieldAssign.setInternalId(1000 + i);
      Long memberInternalId = testGroup.getMembers().iterator().next().getInternalId();
      grouperDataFieldAssign.setMemberInternalId(memberInternalId);
      GrouperDataFieldAssignDao.store(grouperDataFieldAssign);
      i++;
    }
    
    for (GrouperDataField insertedGrouperDataField: insertedGrouperDataFields) {
      GrouperDataGlobalAssign field = new GrouperDataGlobalAssign();
      field.setDataFieldInternalId(insertedGrouperDataField.getInternalId());
      field.setCreatedOn(new Timestamp(System.currentTimeMillis()));
      field.setValueInteger(123L);
      field.setInternalId(1000 + i);
      field.setDataProviderInternalId(dataProviderInternalId);
      GrouperDataGlobalAssignDao.store(field);
      i++;
    }
    
    configIdsToInsert = GrouperUtil.toSet("rowConfigId1", "rowConfigId2");
    GrouperDataRowDao.insertMissingConfigIds(configIdsToInsert);
    
    Set<GrouperDataRow> rowsInserted = GrouperDataRowDao.selectByTexts(configIdsToInsert);
    
    i = 0;
    for (GrouperDataRow row: rowsInserted) {
      GrouperDataAlias alias = new GrouperDataAlias();
      alias.setAliasType("R");
      alias.setCreatedOn(new Timestamp(System.currentTimeMillis()));
      alias.setDataRowInternalId(row.getInternalId());
      alias.setLowerName("lower_name_"+i);
      alias.setName("Name_"+i);
      GrouperDataAliasDao.store(alias);
      i++;
    }
    
    for (GrouperDataRow row: rowsInserted) {
      GrouperDataRowAssign rowAssign = new GrouperDataRowAssign();
      rowAssign.setDataProviderInternalId(dataProviderInternalId);
      rowAssign.setCreatedOn(new Timestamp(System.currentTimeMillis()));
      rowAssign.setDataRowInternalId(row.getInternalId());
      Long memberInternalId = testGroup.getMembers().iterator().next().getInternalId();
      rowAssign.setMemberInternalId(memberInternalId);
      GrouperDataRowAssignDao.store(rowAssign);
    }
    
    List<GrouperDataRowAssign> dataRowAssigns = 
        GrouperDataRowAssignDao.selectByDataRowInternalId(rowsInserted.iterator().next().getInternalId());
    
    long dataFieldInternalId = insertedGrouperDataFields.iterator().next().getInternalId();
    
    GrouperDataRowFieldAssign rowFieldAssign = new GrouperDataRowFieldAssign();
    rowFieldAssign.setDataFieldInternalId(insertedGrouperDataFields.iterator().next().getInternalId());
    rowFieldAssign.setCreatedOn(new Timestamp(System.currentTimeMillis()));
    rowFieldAssign.setDataRowAssignInternalId(dataRowAssigns.get(0).getInternalId());
    GrouperDataRowFieldAssignDao.store(rowFieldAssign);
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field where config_id = 'configId1' or config_id = 'configId2' ").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row where config_id = 'rowConfigId1' or config_id = 'rowConfigId2' ").select(int.class).intValue());
     
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_alias where name in ('configId2_name', 'configId1_name', 'Name_0', 'Name_1') ").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign where internal_id in (1000, 1001) ").select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_field_assign where data_field_internal_id = "+dataFieldInternalId).select(int.class).intValue());
      assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_global_assign where data_field_internal_id = "+dataFieldInternalId).select(int.class).intValue());
    } else {
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    }
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber' and value_integer = 123").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'employee'").select(int.class).intValue());
    
    if (syncType == GrouperDataProviderSyncType.fullSyncFull) {
      // confirm deleting old history
      GrouperDataFieldAssignHst grouperDataFieldAssignHst = GrouperDataFieldAssignHstDao.selectByDataFieldInternalId(twoStepDataField.getInternalId()).iterator().next();
      grouperDataFieldAssignHst.setEndTime(System.currentTimeMillis() * 1000L - 729L * 24 * 60 * 60 * 1000 * 1000);
      GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHst);
      
      grouperDataRowAssignHst = GrouperDataRowAssignHstDao.selectByMemberInternalId(testMember0.getInternalId()).stream().min(Comparator.comparingLong(GrouperDataRowAssignHst::getStartTime)).orElse(null);
      grouperDataRowAssignHst.setEndTime(System.currentTimeMillis() * 1000L - 799L * 24 * 60 * 60 * 1000 * 1000);
      GrouperDataRowAssignHstDao.store(grouperDataRowAssignHst);
      
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

      // no change
      assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
      assertEquals(6, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
      assertEquals(17, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
      
      
      // now set it to when it would get cleaned up
      grouperDataFieldAssignHst.setEndTime(System.currentTimeMillis() * 1000L - 731L * 24 * 60 * 60 * 1000 * 1000);
      GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHst);
      
      grouperDataRowAssignHst.setEndTime(System.currentTimeMillis() * 1000L - 801L * 24 * 60 * 60 * 1000 * 1000);
      GrouperDataRowAssignHstDao.store(grouperDataRowAssignHst);

      // history cleanup is now handled by cleanLogs daemon, not by the data provider full sync
      GrouperDataProviderLogic.deleteOldDataFieldRowHistory(null, null);

      assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_hst").select(int.class).intValue());
      assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_hst").select(int.class).intValue());
      assertEquals(14, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asn_hst").select(int.class).intValue());
    }
  
    // we shouldn't have foreign key errors deleting the fields/rows
    GrouperDataFieldDao.delete(jobNumberDataField);
    GrouperDataRowDao.delete(affiliationDataRow);
  }
  
  /**
   * 
   */
  /**
   * Test that boolean key fields, timestamp key fields, and timestamp non-key fields
   * do not cause thrashing (unnecessary deletes/inserts) on a second full sync.
   * This verifies the fix for the type mismatch in row key comparison where
   * the provider side used convertValue(Object) returning Boolean/Timestamp but
   * the existing side returned Long for non-string fields.
   */
  public void testRowKeyFieldTypesNoThrashing() {

    GrouperSession.startRootSession();

    // create the test table with boolean and timestamp columns
    String tableName = "testgrouper_row_key_types";
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "subject_id", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "row_code", Types.VARCHAR, "40", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "is_primary", Types.VARCHAR, "1", false, true);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "some_timestamp", Types.TIMESTAMP, null, false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "description", Types.VARCHAR, "40", false, false);
        }
      });
    }
    new GcDbAccess().sql("delete from " + tableName).executeSql();

    // insert test data
    // is_primary is T/F (will be boolean key), some_timestamp is a timestamp key, row_code is string key
    Timestamp timestamp1 = new Timestamp(1700000000000L);
    Timestamp timestamp2 = new Timestamp(1700100000000L);
    Timestamp timestamp3 = new Timestamp(1700200000000L);

    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "codeA", "T", timestamp1, "desc1"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "codeB", "F", timestamp2, "desc2"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "codeA", "T", timestamp3, "desc3"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "codeC", "F", timestamp1, "desc4"));

    new GcDbAccess().sql("insert into " + tableName + " (subject_id, row_code, is_primary, some_timestamp, description) values (?, ?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    // configure daemon job
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProviderKeyTypes.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProviderKeyTypes.dataProviderConfigId").value("keyTypes").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    // configure data fields
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktRowCode.fieldAliases").value("ktRowCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktRowCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktRowCode.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktRowCode.descriptionHtml").value("test").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktIsPrimary.fieldAliases").value("ktIsPrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktIsPrimary.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktIsPrimary.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktIsPrimary.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktIsPrimary.descriptionHtml").value("test").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktTimestampKey.fieldAliases").value("ktTimestampKey").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktTimestampKey.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktTimestampKey.fieldDataType").value("timestamp").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktTimestampKey.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktTimestampKey.descriptionHtml").value("test").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktDescription.fieldAliases").value("ktDescription").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktDescription.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktDescription.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.ktDescription.descriptionHtml").value("test").store();

    // configure data row with boolean key, timestamp key, string key, and non-key fields
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowAliases").value("keyTypesRow").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.descriptionHtml").value("test").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowNumberOfDataFields").value("4").store();
    // field 0: string key
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.0.colDataFieldConfigId").value("ktRowCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.0.rowKeyField").value("true").store();
    // field 1: boolean key
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.1.colDataFieldConfigId").value("ktIsPrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.1.rowKeyField").value("true").store();
    // field 2: timestamp key
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.2.colDataFieldConfigId").value("ktTimestampKey").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.2.rowKeyField").value("true").store();
    // field 3: non-key description
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.keyTypesRow.rowDataField.3.colDataFieldConfigId").value("ktDescription").store();

    // configure data provider
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.keyTypes.name").value("keyTypes").store();

    // configure query
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerConfigId").value("keyTypes").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQuerySqlQuery").value("select subject_id, row_code, is_primary, some_timestamp, description from " + tableName).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryRowConfigId").value("keyTypesRow").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryNumberOfDataFields").value("4").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.0.providerDataFieldConfigId").value("ktRowCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.0.providerDataFieldAttribute").value("row_code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.1.providerDataFieldConfigId").value("ktIsPrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.1.providerDataFieldAttribute").value("is_primary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.2.providerDataFieldConfigId").value("ktTimestampKey").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.2.providerDataFieldAttribute").value("some_timestamp").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.3.providerDataFieldConfigId").value("ktDescription").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.3.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.keyTypesQuery.providerQueryDataField.3.providerDataFieldAttribute").value("description").store();

    // ===== FIRST SYNC - should insert all data =====
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProviderKeyTypes");

    // verify data was loaded
    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where data_row_config_id = 'keyTypesRow'").select(int.class).intValue());
    assertEquals(16, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_row_config_id = 'keyTypesRow'").select(int.class).intValue());

    // verify boolean key value stored correctly
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'ktIsPrimary' and data_row_assign_internal_id = (select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'ktRowCode' and value_text = 'codeA')").select(int.class).intValue());

    // verify timestamp key value stored correctly (stored as epoch millis in value_integer)
    assertEquals(timestamp1.getTime(), new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'ktTimestampKey' and data_row_assign_internal_id = (select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'ktRowCode' and value_text = 'codeA')").select(long.class).longValue());

    // snapshot row assign internal IDs before second sync
    List<Long> rowAssignIdsBefore = new GcDbAccess().sql("select gdra.internal_id from grouper_data_row_assign gdra, grouper_data_row gdr where gdra.data_row_internal_id = gdr.internal_id and gdr.config_id = 'keyTypesRow' order by gdra.internal_id").selectList(Long.class);
    assertEquals(4, rowAssignIdsBefore.size());

    // ===== SECOND SYNC - should have zero inserts and zero deletes (no thrashing) =====
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProviderKeyTypes");

    // verify row assign IDs are identical (no deletes/re-inserts)
    List<Long> rowAssignIdsAfter = new GcDbAccess().sql("select gdra.internal_id from grouper_data_row_assign gdra, grouper_data_row gdr where gdra.data_row_internal_id = gdr.internal_id and gdr.config_id = 'keyTypesRow' order by gdra.internal_id").selectList(Long.class);
    assertEquals("Row assign count should not change on second sync (no thrashing)", rowAssignIdsBefore.size(), rowAssignIdsAfter.size());
    assertEquals("Row assign IDs should be identical on second sync (no thrashing)", rowAssignIdsBefore, rowAssignIdsAfter);

    // verify field assigns are also unchanged
    assertEquals(16, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_row_config_id = 'keyTypesRow'").select(int.class).intValue());
  }

  public void testSqlProviderFullSyncFailsafe() {
        
    GrouperSession grouperSession = GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();

    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));

    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "F", "F", "F"));

    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();
    
    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", " "));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "456"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "789"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "456"));
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "456"));

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
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "emer", "T", "math"));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("22").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();
        
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled, hasTwoStep").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.descriptionHtml").value("<b>description html </b>").store();

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePitDays").value("800").store();

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
        
    
    // have a second data provider to make sure failsafe only considers the data provider being run
    {
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider2.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider2.dataProviderConfigId").value("idm2").store();

      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus1.fieldAliases").value("bogus1").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus1.fieldDataType").value("integer").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus1.fieldPrivacyRealm").value("public").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus1.descriptionHtml").value("<b>bogus1 </b>").store();

      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus2.fieldAliases").value("bogus2").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus2.fieldDataStructure").value("rowColumn").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus2.fieldPrivacyRealm").value("public").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.bogus2.descriptionHtml").value("<b>bogus2 </b>").store();
      
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.rowPrivacyRealm").value("public").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.rowAliases").value("bogus").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.rowNumberOfDataFields").value("1").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.rowDataField.0.colDataFieldConfigId").value("bogus2").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.rowDataField.0.rowKeyField").value("true").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.bogus.descriptionHtml").value("<b>description html </b>").store();

      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm2.name").value("idm2").store();
      
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerConfigId").value("idm2").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryType").value("sql").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQuerySqlConfigId").value("grouper").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQuerySqlQuery").value("select subject_id, '5' as bogus1 from grouper_members where subject_id IN ('test.subject.0', 'test.subject.1') and subject_source='jdbc'").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryDataStructure").value("attribute").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQuerySubjectIdAttribute").value("subject_id").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQuerySubjectIdType").value("subjectId").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQuerySubjectSourceId").value("jdbc").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryNumberOfDataFields").value("1").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryDataField.0.providerDataFieldConfigId").value("bogus1").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus1.providerQueryDataField.0.providerDataFieldAttribute").value("bogus1").store();
      
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerConfigId").value("idm2").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryType").value("sql").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQuerySqlConfigId").value("grouper").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQuerySqlQuery").value("select subject_id, 'bogus string' as bogus2 from grouper_members where subject_id IN ('test.subject.0', 'test.subject.1') and subject_source='jdbc'").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryDataStructure").value("row").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryRowConfigId").value("bogus").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQuerySubjectIdAttribute").value("subject_id").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQuerySubjectIdType").value("subjectId").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQuerySubjectSourceId").value("jdbc").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryNumberOfDataFields").value("1").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryDataField.0.providerDataFieldConfigId").value("bogus2").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
      new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.bogus2.providerQueryDataField.0.providerDataFieldAttribute").value("bogus2").store();
    }
    
    
    
    // load data
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider2");

    assertEquals(9, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(12, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());
        
    // check synced data
    assertEquals(19, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(21, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    
    // make some updates
    
    // 1 remove
    new GcDbAccess().sql("update testgrouper_field_attr set two_step_enrolled='T' where subject_id='test.subject.0'").executeBatchSql();
    
    // 1 remove
    new GcDbAccess().sql("update testgrouper_field_attr_multi set attribute_value='999' where subject_id='test.subject.0' and attribute_value='234'").executeBatchSql();
    
    // 1 remove
    new GcDbAccess().sql("delete from testgrouper_field_attr_multi where subject_id='test.subject.1' and attribute_value='123'").executeBatchSql();
    
    // 3 removes
    new GcDbAccess().sql("update testgrouper_field_row_affil set affiliation_code='faculty' where subject_id='test.subject.0' and affiliation_code='staff'").executeBatchSql();
    
    // 3 removes
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='test.subject.1' and affiliation_code='stu'").executeBatchSql();

    
    // inserts don't matter
    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values('test.subject.0', 'stu', 'F', 'english')").executeBatchSql();
    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values('test.subject.4', 'alum', 'F', 'english')").executeBatchSql();
    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values('test.subject.3', '999')").executeBatchSql();
    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values('test.subject.3', '998')").executeBatchSql();

    // 9/40 changes so 22.5%
    
    // run daemon
    try {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Expected exception due to failsafe");
    } catch (Exception e) {
      // good
    }
    
    // no changes
    assertEquals(19, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(21, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    
    // change the failsafe
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("23").store();

    // run daemon
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // check synced data
    assertEquals(20, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(24, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    
    // remove the extra stuff that was added
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='test.subject.0' and affiliation_code='stu'").executeBatchSql();
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='test.subject.4' and affiliation_code='alum'").executeBatchSql();
    new GcDbAccess().sql("delete from testgrouper_field_attr_multi where subject_id='test.subject.3' and attribute_value='999'").executeBatchSql();
    new GcDbAccess().sql("delete from testgrouper_field_attr_multi where subject_id='test.subject.3' and attribute_value='998'").executeBatchSql();
    
    // change failsafe back down
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("18").store();

    // 8/44 changes so 18.18
    
    // run daemon
    try {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Expected exception due to failsafe");
    } catch (Exception e) {
      // good
    }
    
    // no changes
    assertEquals(20, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(24, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    
    // change the failsafe
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("19").store();

    // run daemon
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // check synced data
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id not like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where data_field_config_id like 'bogus%'").select(int.class).intValue());
  }
  
  /**
   * 
   */
  private void internal_testSqlProviderOneRowPerSubject(GrouperDataProviderSyncType syncType) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", " "));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "123"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "456"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "234"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "789"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "456"));
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "456"));

    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values (?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    batchBindVars.clear();
    
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "stu", "F", " "));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "staff", "F", "span"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "fac", "T", "engl"));
    
    // bad subject shouldn't cause the load to fail
    batchBindVars.add(GrouperUtil.toList("test.subject.bogus", "emer", "T", "math"));

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

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.descriptionHtml").value("<b>description html </b>").store();

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.oneRowPerSubject").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("4").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.3.colDataFieldConfigId").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("<b>description html </b>").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePit").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataStorePitDays").value("800").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();


    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQuerySqlQuery").value("select subject_id, attribute_value as job_number from testgrouper_field_attr_multi").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryRowConfigId").value("affiliation").store();
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
    
    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    
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
      
      // bad subject shouldn't cause the load to fail
      batchBindVarsChangeLog.add(GrouperUtil.toList(5, "test.subject.bogus", new Date()));

      new GcDbAccess().sql("insert into testgrouper_dp_changelog (id, subject_id, create_timestamp1) values (?, ?, ?)").batchBindVars(batchBindVarsChangeLog).executeBatchSql();
      
      GrouperDataProviderIncrementalSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");  
    }
        
    // check synced data
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    long rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    List<Integer> jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(2, jobNumbers.size());
    assertTrue(jobNumbers.contains(123));
    assertTrue(jobNumbers.contains(234));
    
    // make some updates in db
    new GcDbAccess().sql("update testgrouper_field_attr_multi set attribute_value='999' where subject_id='test.subject.0' and attribute_value='234'").executeBatchSql();
    
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
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("engl", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(2, jobNumbers.size());
    assertTrue(jobNumbers.contains(123));
    assertTrue(jobNumbers.contains(999));
    
    // make some updates in db - update another field in row data
    new GcDbAccess().sql("update testgrouper_field_row_affil set org='english' where subject_id='test.subject.0'").executeBatchSql();
    
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
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(2, jobNumbers.size());
    assertTrue(jobNumbers.contains(123));
    assertTrue(jobNumbers.contains(999));
    
    // remove the multi valued attribute
    new GcDbAccess().sql("delete from testgrouper_field_attr_multi where subject_id='test.subject.0'").executeBatchSql();
    
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
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(16, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(0, jobNumbers.size());
    
    // add it back
    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values ('test.subject.0', '123')").executeSql();
    new GcDbAccess().sql("insert into testgrouper_field_attr_multi (subject_id, attribute_value) values ('test.subject.0', '999')").executeSql();
    
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
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(18, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(2, jobNumbers.size());
    assertTrue(jobNumbers.contains(123));
    assertTrue(jobNumbers.contains(999));
    
    // delete a row
    new GcDbAccess().sql("delete from testgrouper_field_attr_multi where subject_id='test.subject.0'").executeBatchSql();
    new GcDbAccess().sql("delete from testgrouper_field_row_affil where subject_id='test.subject.0'").executeBatchSql();
    
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
    
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(13, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    // add row back
    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values('test.subject.0', 'staff', 'T', 'english')").executeBatchSql();
    
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

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    assertEquals(4, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where subject_id = 'test.subject.0' and data_row_config_id = 'affiliation'").select(int.class).intValue());
    
    assertEquals(16, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0'").select(int.class).intValue());

    rowAssignId = new GcDbAccess().sql("select data_row_assign_internal_id from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' "
        + "and data_field_config_id = 'affiliationCode' and value_text = 'staff'").select(long.class);

    assertEquals("english", new GcDbAccess().sql("select value_text from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationOrg' and data_row_assign_internal_id = " + rowAssignId).select(String.class));
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'affiliationActive' and data_row_assign_internal_id = " + rowAssignId).select(int.class).intValue());

    jobNumbers = new GcDbAccess().sql("select value_integer from grouper_data_row_field_asgn_v where subject_id = 'test.subject.0' and data_field_config_id = 'jobNumber'").selectList(Integer.class);
    assertEquals(0, jobNumbers.size());
  }

  /**
   * 
   */
  public void testSqlProviderUsingSubjectIdentifier() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

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

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliation.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldAliases").value("businessCategory").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldDataType").value("string").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.businessCategory.descriptionHtml").value("<b>description html </b>").store();

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
  /**
   * helper to set up data provider config for readonly tests
   */
  private void setupDataProviderForReadOnlyTest() {
    GrouperSession.startRootSession();
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
      .batchBindVars(batchBindVars).executeBatchSql();

    batchBindVars.clear();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "stu", "F", "comp"));
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

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("<b>description html </b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("<b>description html </b>").store();

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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("<b>description html </b>").store();
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
  }

  /**
   * test readonly mode with per-provider config
   */
  public void testSqlProviderFullReadOnlyPerProvider() {
    setupDataProviderForReadOnlyTest();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.readOnly").value("true").store();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // no field or row assigns should be written
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());

    // check the loader log for readonly message and counts
    String jobMessage = new GcDbAccess().sql("select job_message from grouper_loader_log where job_name = 'OTHER_JOB_dataProvider1' order by started_time desc")
      .select(String.class);
    assertTrue("job message should contain READONLY: " + jobMessage, jobMessage.contains("READONLY MODE"));
    assertTrue("job message should contain examples: " + jobMessage, jobMessage.contains("fieldAssignInserts"));

    // insert count should be > 0 (tracking what would have been done)
    int insertCount = new GcDbAccess().sql("select insert_count from grouper_loader_log where job_name = 'OTHER_JOB_dataProvider1' order by started_time desc")
      .select(int.class);
    assertTrue("insert count should be > 0: " + insertCount, insertCount > 0);
  }

  /**
   * test readonly mode with global default config
   */
  public void testSqlProviderFullReadOnlyGlobalDefault() {
    setupDataProviderForReadOnlyTest();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderDefault.readOnly").value("true").store();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // no field or row assigns should be written
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue());

    String jobMessage = new GcDbAccess().sql("select job_message from grouper_loader_log where job_name = 'OTHER_JOB_dataProvider1' order by started_time desc")
      .select(String.class);
    assertTrue("job message should contain READONLY: " + jobMessage, jobMessage.contains("READONLY MODE"));
  }

  /**
   * test per-provider readOnly=false overrides global readOnly=true
   */
  public void testSqlProviderFullReadOnlyPerProviderOverridesGlobal() {
    setupDataProviderForReadOnlyTest();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderDefault.readOnly").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.readOnly").value("false").store();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // field assigns SHOULD be written since per-provider overrides global
    assertTrue(new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue() > 0);
    assertTrue(new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v").select(int.class).intValue() > 0);
  }

  /**
   * test that examples appear in job message even when not readonly
   */
  public void testSqlProviderFullExamplesInJobMessage() {
    setupDataProviderForReadOnlyTest();

    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // field assigns should be written
    assertTrue(new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue() > 0);

    // check the job message contains examples
    String jobMessage = new GcDbAccess().sql("select job_message from grouper_loader_log where job_name = 'OTHER_JOB_dataProvider1' order by started_time desc")
      .select(String.class);
    assertTrue("job message should contain examples: " + jobMessage, jobMessage.contains("fieldAssignInserts"));
    assertTrue("job message should contain row examples: " + jobMessage, jobMessage.contains("rowAssignInserts"));
    assertFalse("job message should NOT contain READONLY: " + jobMessage, jobMessage.contains("READONLY MODE"));
  }

  /**
   * test that full sync produces the same results when batching by subject id.
   * uses a batch size of 2 so the 4 test subjects are split across 2 batches.
   * debug map assertions are in the standalone 9-subject test instead, since
   * internal_testSqlProvider runs the sync twice and overwrites the debug map.
   */
  public void testSqlProviderFullBatchBySubjectId() {
    // set a small batch size so the 4 test subjects require multiple batches
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.fullSyncSubjectIdBatchSize").value("2").store();

    internal_testSqlProvider(GrouperDataProviderSyncType.fullSyncFull);
  }

  /**
   * test that full sync works with a batch size of 1, the most extreme batching scenario.
   * each subject gets its own batch.
   */
  public void testSqlProviderFullBatchBySubjectIdSizeOne() {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.fullSyncSubjectIdBatchSize").value("1").store();

    internal_testSqlProvider(GrouperDataProviderSyncType.fullSyncFull);
  }

  /**
   * test that full sync with one-row-per-subject works with subject id batching.
   */
  public void testSqlProviderOneRowPerSubjectFullBatchBySubjectId() {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.fullSyncSubjectIdBatchSize").value("2").store();

    internal_testSqlProviderOneRowPerSubject(GrouperDataProviderSyncType.fullSyncFull);
  }

  /**
   * test batching with 9 subjects and a batch size of 2, resulting in 5 batches.
   * verifies field assigns and row assigns are correct for all subjects across batches.
   */
  public void testSqlProviderFullBatchBySubjectIdNineSubjects() {

    GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // insert 9 subjects with boolean field data
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.4", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.5", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.6", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.7", "F", "F", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.8", "T", "F", "T"));

    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
        .batchBindVars(batchBindVars).executeBatchSql();

    // insert row data (affiliations) for some subjects
    batchBindVars.clear();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "alum", "F", "math"));
    batchBindVars.add(GrouperUtil.toList("test.subject.4", "stu", "T", "comp"));
    batchBindVars.add(GrouperUtil.toList("test.subject.6", "fac", "T", "phys"));
    batchBindVars.add(GrouperUtil.toList("test.subject.8", "contr", "F", "span"));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org) values (?, ?, ?, ?)")
        .batchBindVars(batchBindVars).executeBatchSql();

    // configure daemon job
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    // configure data fields
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("is active").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("two step enrolled").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("employee").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.descriptionHtml").value("affiliation code").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.descriptionHtml").value("affiliation active").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.descriptionHtml").value("affiliation org").store();

    // configure data row
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.descriptionHtml").value("affiliation row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();

    // configure data provider
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.fullSyncSubjectIdBatchSize").value("2").store();

    // configure queries
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

    GrouperDataProviderLogic.testingDebugMap = null;

    // run the sync (9 subjects, batch size 2 = 5 batches)
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    // verify all 9 subjects have field assigns (3 boolean fields each = 27 field assigns)
    assertEquals(27, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where data_field_config_id in ('isActive', 'twoStep', 'employee')").select(int.class).intValue());

    // verify specific subjects (3 field assigns each)
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.0'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.4'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'test.subject.8'").select(int.class).intValue());

    // verify field values for a few subjects
    assertEquals(1, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'isActive'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.0' and data_field_config_id = 'twoStep'").select(int.class).intValue());
    assertEquals(0, new GcDbAccess().sql("select value_integer from grouper_data_field_assign_v where subject_id = 'test.subject.7' and data_field_config_id = 'isActive'").select(int.class).intValue());

    // verify row assigns (5 subjects have affiliations = 5 row assigns)
    assertEquals(5, new GcDbAccess().sql("select count(1) from grouper_data_row_assign_v where data_row_config_id = 'affiliation'").select(int.class).intValue());

    // verify row field assigns (5 rows x 3 fields = 15 row field assigns)
    assertEquals(15, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v").select(int.class).intValue());

    // verify specific row data
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.4' and data_field_config_id = 'affiliationCode' and value_text = 'stu'").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_row_field_asgn_v where subject_id = 'test.subject.6' and data_field_config_id = 'affiliationOrg' and value_text = 'phys'").select(int.class).intValue());

    // verify debug map accumulated across all 5 batches
    assertNotNull("testingDebugMap should be set", GrouperDataProviderLogic.testingDebugMap);
    assertEquals(27, ((Number)GrouperDataProviderLogic.testingDebugMap.get("fieldAssignInserts")).intValue());
    assertEquals(5, ((Number)GrouperDataProviderLogic.testingDebugMap.get("rowAssignInserts")).intValue());
  }

  /**
   * test LDAP data provider full sync with subject id batching.
   * uses a batch size of 2 so the 5 LDAP test subjects are split across 3 batches.
   */
  public void testLdapProviderFullBatchBySubjectId() {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.ldap.fullSyncSubjectIdBatchSize").value("2").store();

    GrouperDataProviderLogic.testingDebugMap = null;

    internal_testLdapProvider(GrouperDataProviderSyncType.fullSyncFull);

    assertNotNull("testingDebugMap should be set after LDAP full sync", GrouperDataProviderLogic.testingDebugMap);
    assertTrue("should have fieldAssignInserts in debug map",
        GrouperDataProviderLogic.testingDebugMap.containsKey("fieldAssignInserts"));
  }

  /**
   * test failsafe check #1: subject id count check.
   * load data for 4 subjects, then remove 2 from the source and re-sync with a low threshold.
   * the subject id count failsafe should trigger before any batches run.
   */
  public void testSqlProviderFullSyncFailsafeSubjectIdCount() {

    GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // insert 4 subjects
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));

    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
        .batchBindVars(batchBindVars).executeBatchSql();

    // configure with failsafe at 30% and minimum subject count of 1 (so it applies to small datasets)
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("30").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMinSubjectCount").value("1").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("is active").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("two step").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("employee").store();

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

    // first sync loads all 4 subjects
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(12, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());

    // remove 2 subjects from source (50% missing > 30% threshold)
    new GcDbAccess().sql("delete from testgrouper_field_attr where subject_id in ('test.subject.2', 'test.subject.3')").executeSql();

    // second sync should fail with subject id count failsafe
    try {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Expected failsafe exception due to missing subject ids");
    } catch (Exception e) {
      assertTrue("Should be failsafe error: " + e.getMessage(), e.getMessage().contains("missing from source"));
    }

    // no changes should have been made
    assertEquals(12, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());
  }

  /**
   * test failsafe check #2: per-batch field assign remove check.
   * load data for 4 subjects, then modify data to trigger removes within a batch.
   * uses batch size of 2 so the per-batch check applies to smaller groups.
   */
  public void testSqlProviderFullSyncFailsafePerBatch() {

    GrouperSession.startRootSession();

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");

    // insert 4 subjects with 3 boolean fields each (12 field assigns total after first sync)
    List<List<Object>> batchBindVars = new ArrayList<List<Object>>();
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "T", "F", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "F", "T", "F"));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "T", "T", "T"));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "F", "F", "F"));

    new GcDbAccess().sql("insert into testgrouper_field_attr (subject_id, active, two_step_enrolled, employee) values (?, ?, ?, ?)")
        .batchBindVars(batchBindVars).executeBatchSql();

    // configure with failsafe at 30%, batch size 2
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.class").value("edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.dataProviderConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.dataProvider1.failsafeMaxOverallPercentFieldAssignRemove").value("30").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.fullSyncSubjectIdBatchSize").value("2").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmName").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperPrivacyRealm.public.privacyRealmPublic").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.descriptionHtml").value("is active").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.descriptionHtml").value("two step").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.descriptionHtml").value("employee").store();

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

    // first sync loads all 4 subjects (12 field assigns)
    GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");

    assertEquals(12, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue());

    // change ALL values for test.subject.0 and test.subject.1 (these will be in the same batch)
    // each subject has 3 field assigns, all changing = 6 removes out of 6 in-batch = 100% > 30%
    new GcDbAccess().sql("update testgrouper_field_attr set active='F', two_step_enrolled='T', employee='F' where subject_id='test.subject.0'").executeSql();
    new GcDbAccess().sql("update testgrouper_field_attr set active='T', two_step_enrolled='F', employee='T' where subject_id='test.subject.1'").executeSql();

    // second sync should fail with per-batch failsafe
    try {
      GrouperDataProviderFullSyncJob.runDaemonStandalone("OTHER_JOB_dataProvider1");
      fail("Expected failsafe exception due to per-batch field assign removes");
    } catch (Exception e) {
      assertTrue("Should be failsafe error: " + e.getMessage(), e.getMessage().contains("field assigns being removed"));
    }

    // the batch with test.subject.0 and test.subject.1 should NOT have been written
    // (failsafe triggers before writing)
    // but we can't easily assert which batch ran first since subject id ordering may vary
    // just verify total count didn't decrease
    int fieldAssignCount = new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v").select(int.class).intValue();
    assertTrue("field assign count should not have decreased from 12, got " + fieldAssignCount, fieldAssignCount >= 12);
  }

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
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "dept_number", Types.BIGINT, "12", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "affiliation_code_primary", Types.VARCHAR, "40", false, false);
          GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "dept_number_primary", Types.BIGINT, "12", false, false);
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
  
  public static void createTableChangeLog() {
    
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
