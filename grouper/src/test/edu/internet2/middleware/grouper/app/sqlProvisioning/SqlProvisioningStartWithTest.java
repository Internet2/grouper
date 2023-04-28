package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

/**
 *
 */
public class SqlProvisioningStartWithTest extends GrouperProvisioningBaseTest {
  
  
  public SqlProvisioningStartWithTest() {
    super();
  }

  public SqlProvisioningStartWithTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new SqlProvisioningStartWithTest("testFullProvisionerGroupTableEntityTableMembershipTable"));
  }
  
  public void testFullProvisionerGroupTableEntityTableMembershipTable() {
    
    new GcDbAccess().connectionName("grouper").sql("delete from testgrouper_prov_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from testgrouper_prov_entity").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from testgrouper_prov_mship3").executeSql();
    
    SqlProvisioningStartWith startWith = new SqlProvisioningStartWith();
    
    Map<String, String> startWithSuffixToValue = new HashMap<>();
    
    startWithSuffixToValue.put("dbExternalSystemConfigId", "grouper");
    startWithSuffixToValue.put("sqlPattern", "groupTableEntityTableMembershipTable");
    
    startWithSuffixToValue.put("userAttributesType", "core");
    startWithSuffixToValue.put("membershipStructure", "membershipObjects");
    
    startWithSuffixToValue.put("hasGroupTable", "true");
    startWithSuffixToValue.put("groupTableName", "testgrouper_prov_group");
    startWithSuffixToValue.put("groupTableIdColumn", "uuid");
    startWithSuffixToValue.put("groupTablePrimaryKeyValue", "uuid");
    startWithSuffixToValue.put("groupTableColumnNames", "name,display_name,description");
    
    startWithSuffixToValue.put("hasEntityTable", "true");
    startWithSuffixToValue.put("entityTableName", "testgrouper_prov_entity");
    startWithSuffixToValue.put("entityTableIdColumn", "uuid");
    startWithSuffixToValue.put("entityTablePrimaryKeyValue", "uuid");
    startWithSuffixToValue.put("entityTableColumnNames", "name,subject_id_or_identifier,description");
    startWithSuffixToValue.put("manageEntities", "true");
    
    startWithSuffixToValue.put("membershipTableName", "testgrouper_prov_mship3");
    startWithSuffixToValue.put("membershipTableGroupColumn", "group_uuid");
    startWithSuffixToValue.put("membershipTableGroupValue", "groupPrimaryKey");
    startWithSuffixToValue.put("membershipTableEntityColumn", "entity_uuid");
    startWithSuffixToValue.put("membershipTableEntityValue", "entityPrimaryKey");
    
    Map<String, Object> provisionerSuffixToValue = new HashMap<>();
    
    startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
    
    startWith.manipulateProvisionerConfigurationValue("sqlProvTest", startWithSuffixToValue, provisionerSuffixToValue);
    
    for (String key: provisionerSuffixToValue.keySet()) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner.sqlProvTest."+key)
        .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.logCommandsAlways").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.sqlProvTest.subjectSourcesToProvision").value("jdbc").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_sqlProvTest.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_sqlProvTest.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_sqlProvTest.provisionerConfigId").value("sqlProvTest").store();
          
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("sqlProvTest");
    attributeValue.setTargetName("sqlProvTest");
    attributeValue.setStemScopeString("sub");
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertTrue(1 <= grouperProvisioningOutput.getInsert());
    
    assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_group").select(int.class));
    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_entity").select(int.class));
    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from testgrouper_prov_mship3").select(int.class));
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
    }
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "sqlProvTest");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    
    GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    provisioner.initialize(GrouperProvisioningType.diagnostics);
    GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.4");
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(true);
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(true);
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
    grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(true);
    grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    validateNoErrors(grouperProvisioningDiagnosticsContainer);
  }
  
  private void validateNoErrors(GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    String[] lines = grouperProvisioningDiagnosticsContainer.getReportFinal().split("\n"); 
    List<String> errorLines = new ArrayList<String>();
    for (String line : lines) {
      if (line.contains("'red'") || line.contains("Error:")) {
        errorLines.add(line);
      }
    }
    
    if (errorLines.size() > 0) {
      fail("There are " + errorLines.size() + " errors in report: " + errorLines);
    }
  }


  @Override
  public String defaultConfigId() {
    return "sqlProvTest";
  }

}
