package edu.internet2.middleware.grouper.dataField;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


public class GrouperDataProviderTest extends GrouperTest {

  public GrouperDataProviderTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDataProviderTest("testProvider"));
  }

  public void setUp() {
    super.setUp();
    createTableAffiliation();
    createTableAttributes();
    createTableAttributesMulti();

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
  public void testProvider() {
    
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

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldAliases").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.isActive.fieldDataType").value("boolean").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldAliases").value("twoStepEnrolled").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.twoStep.fieldDataType").value("boolean").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldAliases").value("employee").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.employee.fieldDataType").value("boolean").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldAliases").value("jobNumber").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldDataType").value("integer").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.jobNumber.fieldMultiValued").value("true").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldAliases").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationCode.fieldDataStructure").value("rowColumn").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldAliases").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataStructure").value("rowColumn").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationActive.fieldDataType").value("boolean").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldAliases").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataField.affiliationOrg.fieldDataStructure").value("rowColumn").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowAliases").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowNumberOfDataFields").value("3").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.0.colDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.1.colDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataRow.affiliation.rowDataField.2.colDataFieldConfigId").value("affiliationOrg").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProvider.idm.name").value("idm").store();

    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerConfigId").value("idm").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryType").value("sql").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQuerySqlQuery").value("select subject_id, active, two_step_enrolled, employee from testgrouper_field_attr").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttr.providerQueryDataStructure").value("dataFields").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAttrMulti.providerQueryDataStructure").value("dataFields").store();
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
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataStructure").value("dataRow").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryRowConfigId").value("affiliation").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdAttribute").value("subject_id").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQuerySubjectSourceId").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryNumberOfDataFields").value("1").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldConfigId").value("affiliationCode").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.0.providerDataFieldAttribute").value("affiliation_code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldConfigId").value("affiliationActive").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.1.providerDataFieldAttribute").value("active").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldConfigId").value("affiliationOrg").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldMappingType").value("attribute").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperDataProviderQuery.idmAffiliations.providerQueryDataField.2.providerDataFieldAttribute").value("org").store();
    

    String abac = "entity.hasAttribute('jobNumber', 123) || entity.hasAttribute('jobNumber', 234)";
    abac = "entity.hasAttribute('twoStepEnrolled')";
    abac = "entity.hasAttribute('affiliation', 'affiliationCode!=alum && affiliationActive && affiliationOrg==engl')";
        
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
