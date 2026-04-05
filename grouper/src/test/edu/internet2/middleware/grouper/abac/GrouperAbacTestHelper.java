package edu.internet2.middleware.grouper.abac;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderFullSyncJob;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperAbacTestHelper {

  public static void setupDataFields() {

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

    batchBindVars.add(GrouperUtil.toList("test.subject.0", "staff", "T", "engl", 135, "staff", 135));
    batchBindVars.add(GrouperUtil.toList("test.subject.0", "alum", "T", "math", 246, "staff", 200));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "stu", "F", "comp", null, "stu", null));
    batchBindVars.add(GrouperUtil.toList("test.subject.1", "contr", "T", "phys", 468, "staff", 468));
    batchBindVars.add(GrouperUtil.toList("test.subject.2", "staff", "F", "span", 579, "fac", 580));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "fac", "T", "engl", 135, "fac", 135));
    batchBindVars.add(GrouperUtil.toList("test.subject.3", "emer", "T", null, 246, "staff", 100));

    new GcDbAccess().sql("insert into testgrouper_field_row_affil (subject_id, affiliation_code, active, org, dept_number, affiliation_code_primary, dept_number_primary) values (?, ?, ?, ?, ?, ?, ?)")
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

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCodePrimary.fieldAliases").value("affiliationCodePrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCodePrimary.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCodePrimary.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCodePrimary.descriptionHtml").value("<b>affiliation code primary</b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumberPrimary.fieldAliases").value("affiliationDeptNumberPrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumberPrimary.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumberPrimary.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumberPrimary.fieldPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationDeptNumberPrimary.descriptionHtml").value("<b>dept number primary</b>").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowPrivacyRealm").value("public").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("6").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.rowKeyField").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.3.colDataFieldConfigId").value("affiliationDeptNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.4.colDataFieldConfigId").value("affiliationCodePrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.5.colDataFieldConfigId").value("affiliationDeptNumberPrimary").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySqlQuery").value("select subject_id, affiliation_code, active, org, dept_number, affiliation_code_primary, dept_number_primary from testgrouper_field_row_affil").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataStructure").value("row").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryNumberOfDataFields").value("6").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.4.providerDataFieldConfigId").value("affiliationCodePrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.4.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.4.providerDataFieldAttribute").value("affiliation_code_primary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.5.providerDataFieldConfigId").value("affiliationDeptNumberPrimary").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.5.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.5.providerDataFieldAttribute").value("dept_number_primary").store();


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

}
