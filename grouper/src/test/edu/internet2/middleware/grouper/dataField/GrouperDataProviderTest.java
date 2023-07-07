package edu.internet2.middleware.grouper.dataField;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
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
    TestRunner.run(new GrouperDataProviderTest("testLdapProvider"));
  }

  public void setUp() {
    super.setUp();
    createTableAffiliation();
    createTableAttributes();
    createTableAttributesMulti();

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
  public void testSqlProvider() {
    
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
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    GrouperDataEngine.syncDataProviders(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
    GrouperDataEngine.syncDataRows(grouperConfig);
    GrouperDataEngine.syncDataAliases(grouperConfig);
    
    new GrouperDataEngine().loadFieldsAndRows(grouperConfig);

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText("idm");
    
    // load data
    GrouperDataEngine.loadFull(grouperDataProvider);


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

  }
  
  /**
   * 
   */
  public void testLdapProvider() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    LdapProvisionerTestUtils.stopAndRemoveLdapContainer();
    LdapProvisionerTestUtils.startLdapContainer();
    LdapProvisionerTestUtils.setupSubjectSource();

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

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    GrouperDataEngine.syncDataProviders(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
    GrouperDataEngine.syncDataRows(grouperConfig);
    GrouperDataEngine.syncDataAliases(grouperConfig);
    
    new GrouperDataEngine().loadFieldsAndRows(grouperConfig);

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText("ldap");
    
    // load data
    GrouperDataEngine.loadFull(grouperDataProvider);


    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field").select(int.class).intValue());

    assertEquals(0, new GcDbAccess().sql("select count(1) from grouper_data_row").select(int.class).intValue());

    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_alias").select(int.class).intValue());

    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_provider").select(int.class).intValue());

    
    // check synced data
    
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-ngonazles'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jvales975'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-kmartinez977'").select(int.class).intValue());
    assertEquals(3, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'a-jbutler985'").select(int.class).intValue());
    assertEquals(2, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson'").select(int.class).intValue());

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
    
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson' and data_field_config_id = 'affiliation' and value_text is null").select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(1) from grouper_data_field_assign_v where subject_id = 'banderson' and data_field_config_id = 'businessCategory' and value_text is null").select(int.class).intValue());
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
}
