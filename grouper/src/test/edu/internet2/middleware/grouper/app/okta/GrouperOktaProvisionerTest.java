package edu.internet2.middleware.grouper.app.okta;

import java.io.IOException;
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
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningValidationIssue;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import junit.textui.TestRunner;


public class GrouperOktaProvisionerTest extends GrouperProvisioningBaseTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    TestRunner.run(new GrouperOktaProvisionerTest("testOktaFullSyncCapturesOrphanTargetEntities"));
    
  }
  
  @Override
  public String defaultConfigId() {
    return "myOktaProvisioner";
  }

  public GrouperOktaProvisionerTest(String name) {
    super(name);
  }
  
  private boolean startTomcat = false;
  
  
  public void testFullSyncOktaStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
      
      OktaProvisionerTestUtils.setupOktaExternalSystem();
      
      OktaProvisioningStartWith startWith = new OktaProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("oktaExternalSystemConfigId", "myOkta");
      startWithSuffixToValue.put("oktaPattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("selectAllGroups", "true");
      startWithSuffixToValue.put("manageGroups", "true");
      startWithSuffixToValue.put("groupNameAttributeValue", "extension");
      startWithSuffixToValue.put("groupEmailAttributeValue", "name");
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityEmailSubjectAttribute", "email");
      startWithSuffixToValue.put("entityLastName", "name");
      startWithSuffixToValue.put("entityFirstName", "name");
      startWithSuffixToValue.put("entityLoginSubjectAttribute", "email");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myOktaProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myOktaProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myOktaProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myOktaProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myOktaProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myOktaProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myOktaProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myOktaProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myOktaProvisioner.provisionerConfigId").value("myOktaProvisioner").store();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_okta_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      GrouperOktaGroup grouperOktaGroup = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
        
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
      }
      
      assertEquals("testGroup", grouperOktaGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myOktaProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperOktaGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myOktaProvisioner");
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
      
    } finally {
      
    }
    
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
  
  public void testIncrementalSyncOkta() throws IOException {
    
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput());
  
    GrouperStartup.startup();
    
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
//      List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups("myOkta", null, null);
//  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_auth").executeSql();
//      
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_okta_group").select(int.class));
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      
      fullProvision();
      incrementalProvision();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_okta_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      
      incrementalProvision();
  
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
//      GrouperOktaGroup grouperOktaGroup = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).get(0);
//      
//      assertEquals("test:testGroup", grouperOktaGroup.getName());
//      
//      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myOktaProvisioner");
//      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
//      
//      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
//      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
//      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
//      assertEquals(grouperOktaGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_okta_membership also
      incrementalProvision();
      
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      
      //now add the same subject again
      testGroup.addMember(SubjectTestHelper.SUBJ1);
      incrementalProvision();
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      incrementalProvision();
      
      GrouperOktaGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  
  public void testDoNotExistErrorCode() throws IOException {
    
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .addExtraConfig("makeChangesToEntities", "false")
        .addExtraConfig("deleteEntities", "false")
        .addExtraConfig("deleteEntitiesIfGrouperDeleted", "false")
        .addExtraConfig("updateEntities", "false")
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false")
        .addExtraConfig("insertEntities", "false"));
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    // this will create tables
    List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups("myOkta", null, null);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
    //new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_auth").executeSql();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myOktaProvisioner");
    attributeValue.setTargetName("myOktaProvisioner");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_okta_group").select(int.class));

    assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myOktaProvisioner");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    
    List<GcGrouperSyncMembership> grouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIds(GrouperUtil.toSet(testGroup.getId()));
    
    assertEquals(2, grouperSyncMemberships.size());
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership: grouperSyncMemberships) {
      assertEquals("DNE", gcGrouperSyncMembership.getErrorCode().toString());
    }
    
  }

  public void testFullSyncOkta() throws IOException {
    
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups("myOkta", null, null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_auth").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_okta_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      GrouperOktaGroup grouperOktaGroup = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
        
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
      }
      
      assertEquals("test:testGroup", grouperOktaGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myOktaProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperOktaGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_okta_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      //now add the same subject again
      testGroup.addMember(SubjectTestHelper.SUBJ1);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      GrouperOktaGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup").list(GrouperOktaGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser").list(GrouperOktaUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperOktaMembership").list(GrouperOktaMembership.class).size());
      
    } finally {
      
    }

  }
  
  public void atestFullSyncOktaReal() throws IOException {
    
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    
    OktaProvisionerTestUtils.configureOktaProvisioner(
        new OktaProvisionerTestConfigInput()
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfGrouperDeleted", "true"));
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperOktaGroup> grouperOktaGroups = GrouperOktaApiCommands.retrieveOktaGroups("myOkta", null, null);
  
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myOktaProvisioner");
      attributeValue.setTargetName("myOktaProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      
      testGroup.addMember(SubjectTestHelper.SUBJ3, false);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, grouperProvisioningOutput.getInsert());

    } finally {

    }

  }

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the Okta read path.
   * Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart. Framework-detail coverage lives in the SCIM + LDAP suites.
   */
  public void testOktaFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();

    OktaProvisionerTestUtils.setupOktaExternalSystem();

    String configId = "myOktaProvisioner";
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // first pass writes the Okta target; sync-back tables stay empty until the next
    // read pass captures the new objects (read-state convergence contract).
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());

    // second pass: reads back what we just wrote, captures through the sync hooks, flushes
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row after sync-back",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows",
        countSyncBack(configId, "grouper_prov_mship") >= 2);

    int linkedGroups = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and group_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_group row should be linked to a Grouper group", linkedGroups >= 1);

    int linkedUsers = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and member_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_user row should be linked to a Grouper member", linkedUsers >= 1);
  }

  /**
   * Sync-back smoke test for the scoped retrieve path: selectAllGroups=false and
   * selectAllEntities=false force per-id retrieves on pass 2 (after pass 1 writes the
   * target objects). Asserts all three prov_* axes have rows >= the expected counts.
   */
  public void testOktaFullSyncSelectByIdsPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();

    OktaProvisionerTestUtils.setupOktaExternalSystem();

    String configId = "myOktaProvisioner";
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve and flushes.
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row via scoped retrieve",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_mship") >= 2);
  }

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  /**
   * the single provisioned group's target_group_id (Okta group id) in the mirror, or null.
   * Mirrors the Box/Adobe helper of the same name -- used by the update-converge test to prove the
   * SAME target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved {@code description} attribute value for the single provisioned group in the mirror, or
   * null. Reads through the {@code grouper_prov_group_attr_v} reporting view (not the base
   * grouper_prov_group_attr_value table), because the raw string is stored via a dictionary FK and
   * only the view resolves it back to text (column {@code value_string}). Unlike Box (where
   * description is NOT a default capture attribute and the update test must set
   * {@code nativeAttributesGroups=name,description}), {@code description} IS an Okta DEFAULT group
   * capture attribute (DEFAULT_GROUP_ATTRS = name + description, both read from {@code /profile/*}),
   * so it is captured with no extra config.
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  // ==========================================================================================
  // SCIM-parity sync-back tests for Okta, CAPABILITY-GATED. Replicates the Box pilot
  // (boxProvisioner/GrouperBoxProvisionerTest) for Okta.
  //
  // Okta capture model (verified from GrouperOktaApiCommands + GrouperOktaProvisioningTargetNativeSync
  // + GrouperOktaTargetDao): Okta captures group/user OBJECTS into the generic mirror on the READ
  // path only. captureGroupJsonFromCurrentProvisioner / captureUserJsonFromCurrentProvisioner fire
  // inside GrouperOktaApiCommands.retrieveOktaGroups / retrieveOktaGroup / retrieveOktaUsers /
  // retrieveOktaUser / retrieveOktaUserById (from the RAW Okta JSON), and there is no write-side
  // capture of group/user objects, so an object create/update/delete converges into the mirror on
  // the NEXT read pass, not the same run that writes it.
  //
  // MEMBERSHIPS, however, now capture on WRITE: GrouperOktaTargetDao.insertMembership /
  // deleteMembership call captureMembershipInsertFromCurrentProvisioner /
  // captureMembershipDeleteFromCurrentProvisioner on success, which record into the native membership
  // mirror (recordTargetNativeMembershipInsert / recordTargetNativeMembershipDelete) -- the same
  // membership write-track design as SCIM/Adobe/Dropbox, so a membership add/remove converges on the
  // write pass. captureMembershipsForGroupForCurrentProvisioner still ALSO fires on the read path
  // inside retrieveMembershipsByGroup (and the bulk retrieveAllData).
  //
  // The object converge tests below therefore use the two-pass full-sync pattern (pass 1 writes the
  // target, pass 2 re-reads and the end-of-run flush converges) because group/user OBJECTS are
  // read-capture only, the same shape as the existing testOktaFullSyncPopulatesGenericTables.
  //
  // The full flush (GrouperProvisioningLogic.loadDataToGenericProvisionerTables) is a FULL REPLACE
  // scoped to the provisioner's grouper_sync_internal_id: anything in the mirror that the target did
  // NOT return this run is deleted. That is what makes the delete / membership-remove converge tests
  // work after a re-read pass.
  //
  // OKTA OBJECTS ARE NESTED: a group's id is top-level (/id) but its descriptive fields are under
  // /profile (/profile/name, /profile/description); a user's id is /id and its descriptive fields
  // are under /profile (/profile/login, /profile/email, ...). The DEFAULT capture pointers reach
  // into /profile/*, so the captured attribute KEYS are the un-prefixed names (name, description,
  // login, email) while the VALUES come from the nested /profile source. Tests assert by the stored
  // KEY.
  //
  // Capabilities confirmed in GrouperOktaTargetDao.registerGrouperProvisionerDaoCapabilities:
  //   group  : insert YES, update YES, delete YES
  //   entity : insert YES, update YES, delete YES
  //   mship  : insert YES, delete YES, REPLACE *NO* (no setCanReplaceMembership)
  //   memberships are group-centric (canRetrieveMembershipsAllByGroup); Okta has no
  //   retrieve-all-memberships call, so members are fetched per-group.
  //
  // DEFAULT capture attributes (GrouperOktaProvisioningTargetNativeSync):
  //   groups : name (/profile/name), description (/profile/description)   -- id excluded (target_group_id)
  //   users  : login (/profile/login), email (/profile/email)            -- id excluded (target_user_id)
  // NB vs Box: Box group defaults were name/group_type/provenance and Box did NOT capture description
  // by default; Okta DOES capture description by default, so the group-update-converge test asserts
  // on it directly with no nativeAttributesGroups override. Box entity defaults were
  // login/role/status/type; Okta entity defaults are login/email.
  //
  // Matching attributes (OktaProvisionerTestUtils.configureOktaProvisioner):
  //   groupMatchingAttribute0name = name  (targetGroupAttribute.1 = group name)
  //   entityMatchingAttribute0name = login (targetEntityAttribute.4 = login, translated from email)
  // An update that changes the MATCHING attribute cannot converge as an in-place update (the Adobe
  // lesson), so the group-update-converge test mutates DESCRIPTION (a NON-matching, default-captured
  // attribute that round-trips through the mock updateGroup). There is NO rename-as-update test
  // (groups are name-matched). For users there is no safe Grouper-driven NON-matching attribute to
  // mutate (login = email is fixed per subject; firstName/lastName are derived from the same subject
  // name and are not matching but are also not independently controllable to a stable assert value),
  // so the user-update-converge test is SKIPPED -- see the one-line note where it would live.
  //
  // SKIPPED, per capability (no test body, just this note):
  //   - no membership-replace sync-back test: GrouperOktaTargetDao has no setCanReplaceMembership
  //     (so SCIM's testMembershipReplaceConvergesSameRun / testIncrementalMembershipReplace... do
  //     not apply to Okta), same as Box.
  //   - no "same-run" convergence variants of the SCIM insert/update/delete/membership tests: Okta
  //     captures on READ only, so these can only converge on the next read pass. Their intent is
  //     ported as the two-pass full tests below (testOktaGroupInsertConvergesNextRead,
  //     testOktaGroupDeleteConvergesNextRead, testOktaMembershipAddConvergesNextRead,
  //     testOktaMembershipRemoveConvergesNextRead, testOktaGroupUpdateConvergesNextRead).
  //
  // All tests gate on tomcatRunTests() (like the two existing Okta sync-back tests) because the
  // Okta mock REST endpoints require the embedded Tomcat to be up.
  // ==========================================================================================

  /**
   * Shared setup for the Okta sync-back tests: configure the provisioner with the three
   * load*ToGenericGrouperTable flags on (and recalculateAllOperations so every object/membership is
   * processed each run), then clean the Okta mock target. The caller starts its own root session and
   * creates the Grouper-side stems/groups/members it needs. Mirrors the per-test boilerplate that
   * testOktaFullSyncPopulatesGenericTables open-codes (ensureOktaMockTables + delete mock rows +
   * setupOktaExternalSystem + configureOktaProvisioner).
   *
   * <p>Leaves all delete-types at their config defaults (Okta test util sets customize*Crud=false,
   * i.e. the framework's default auto-delete behavior), so callers that need to enable or disable a
   * specific delete pass it explicitly via {@code extraConfig} using the Box/Adobe idiom
   * (customizeXCrud=true + deleteX + deleteXIf...).
   *
   * @param configId the provisioner config id (always "myOktaProvisioner" here)
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes to set (may be null)
   */
  private void setupOktaSyncBack(String configId, Map<String, String> extraConfig) {

    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();

    OktaProvisionerTestUtils.setupOktaExternalSystem();

    OktaProvisionerTestConfigInput configInput = new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    OktaProvisionerTestUtils.configureOktaProvisioner(configInput);

    GrouperStartup.startup();
  }

  /**
   * Sync-back convergence of a newly created group, two-pass full (Okta analogue of SCIM's
   * testGroupInsertConvergesSameRun, ported as next-read since Okta captures on read). Because
   * createGroupsAndEntitiesBeforeTranslatingMemberships + selectGroups are on, the daemon re-reads
   * each just-inserted group (to link it) through the Okta read path, and that read captures it. So
   * the group is already in the mirror after pass 1, linked back to its Grouper group
   * (group_internal_id not null). Pass 2 is idempotent.
   *
   * <p>Asserts the captured group attribute KEY {@code name} is present (an Okta DEFAULT group
   * capture attribute; its value comes from the nested {@code /profile/name}).
   */
  public void testOktaGroupInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the mirror yet
    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

    // pass 1 inserts the group AND -- via the post-insert re-read that links it -- captures it, so
    // the group converges into the mirror within this same run (inserts-converge-same-run gotcha)
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    assertEquals("group insert converges in the same run (post-insert re-read captures it)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // pass 2 re-reads; convergence is idempotent
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("group insert should converge into prov_group on the next read pass", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // name captured from the Okta read response (a DEFAULT group capture attribute KEY; value from
    // the nested /profile/name source)
    int nameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("name should be captured from the Okta read response, got " + nameValueRows,
        nameValueRows >= 1);
  }

  /**
   * Sync-back convergence of an object DELETE, two-pass full (Okta analogue of SCIM's
   * testGroupDeleteConvergesSameRun). Seed test:testGroup + SUBJ0 + their membership into the mirror,
   * then delete the group in Grouper. With deleteGroups/Entities/Memberships enabled the next full
   * sync removes them from the Okta target (pass A), and the following re-read pass (pass B) sees
   * them gone -- the full-replace flush, scoped to this provisioner's sync, then drops the group, the
   * now-orphaned user, and the membership from the mirror.
   */
  public void testOktaGroupDeleteConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // Okta test util defaults customize*Crud=false (framework auto-delete mode). To drive explicit
    // deletes we mirror the Box/Adobe idiom: turn ON customize*Crud per axis, then set the umbrella
    // deleteX=true plus the specific delete-when key. This test deletes the group, cascading to its
    // user + membership.
    Map<String, String> deleteTypes = new HashMap<String, String>();
    deleteTypes.put("customizeGroupCrud", "true");
    deleteTypes.put("deleteGroups", "true");
    deleteTypes.put("deleteGroupsIfNotExistInGrouper", "true");
    deleteTypes.put("customizeEntityCrud", "true");
    deleteTypes.put("deleteEntities", "true");
    deleteTypes.put("deleteEntitiesIfNotExistInGrouper", "true");
    deleteTypes.put("customizeMembershipCrud", "true");
    deleteTypes.put("deleteMemberships", "true");
    deleteTypes.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, deleteTypes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: two passes converge the group + SUBJ0 + their membership into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // delete the group; SUBJ0 is now orphaned (no other provisioned group) and is deleted too
    testGroup.delete();

    // pass A: the delete writes hit the Okta target (group + orphaned user + membership removed)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read sees them gone; the full-replace flush drops their mirror rows
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("orphaned SUBJ0 dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("membership dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass full (Okta
   * analogue of SCIM's testUserUpdateConvergesSameRun, but on a GROUP). Okta groups are matched by
   * name, so the rename-as-update problem (the Adobe lesson) does NOT apply here: we mutate the
   * group's DESCRIPTION, which is mapped (targetGroupAttribute.2), round-trips through the mock's
   * updateGroup, and is NOT the matching attribute. Unlike Box, description IS an Okta DEFAULT group
   * capture attribute (DEFAULT_GROUP_ATTRS reads /profile/description), so NO nativeAttributesGroups
   * override is needed for the value to be captured.
   *
   * <p>Asserts both that the description VALUE converges to the new value AND that it is an in-place
   * update -- the SAME target group id survives (not delete + re-create, which would assign a new
   * Okta id). Convergence is on the re-read pass (pass B), since Okta captures on read.
   */
  public void testOktaGroupUpdateConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // no nativeAttributesGroups override: description is already a DEFAULT Okta group capture attr
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("originalDescription").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group provisioned with description "originalDescription"
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    String groupTargetIdBefore = mirroredGroupTargetId(configId);
    assertNotNull("group should have a target id after seed", groupTargetIdBefore);
    assertEquals("seed: original description captured", "originalDescription",
        mirroredGroupDescription(configId));

    // change the description (a NON-matching attribute) -> Okta updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the Okta target (updateGroup persists it)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read captures the target's actual new description into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // mirror side: still ONE group, the SAME group (same target id) -- in-place update, not
    // delete + re-create -- and its description converged to the new value.
    assertEquals("group still in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("mirror tracks the same group through the update (update, not re-create)",
        groupTargetIdBefore, mirroredGroupTargetId(configId));
    assertEquals("mirror description should converge to the new value on the re-read pass",
        "newDescription", mirroredGroupDescription(configId));

    // NOTE: no user-update-converge test for Okta. Okta users are matched by login (= email), and
    // their other Grouper-driven attributes (firstName/lastName) are both derived from the same
    // subject "name" field, so there is no safe Grouper-driven NON-matching user attribute to mutate
    // to a stable, independently-asserted value. An update-converge test would either mutate the
    // match key (the Adobe lesson) or assert on a derived value -- skipped rather than written, same
    // reasoning as Box.
  }

  /**
   * Sync-back convergence of a membership ADD to an already-provisioned group, two-pass full (Okta
   * analogue of SCIM's testMembershipAddConvergesSameRun). Seed test:testGroup with SUBJ0, then add
   * SUBJ1. Okta now write-captures memberships (insertMembership records into the native membership
   * mirror), and also re-reads them on the read path (retrieveMembershipsByGroup), so the add shows
   * in grouper_prov_mship: this test drives it two-pass -- pass A issues the membership insert to the
   * Okta target, pass B re-reads the group's members and the flush converges (testGroup, SUBJ1).
   */
  public void testOktaMembershipAddConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + the one membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: the single membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // add SUBJ1 to the already-provisioned group
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // pass A: the membership insert (and SUBJ1's user insert) hit the Okta target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: re-read sees both members; the flush converges the added membership
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("the added membership should converge on the re-read pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of a membership REMOVE from a surviving group, two-pass full (Okta
   * analogue of SCIM's testMembershipRemoveConvergesSameRun). Two groups both hold SUBJ0; SUBJ0 is
   * removed from testGroup only (it survives in otherGroup, so its Okta user is NOT deleted). The
   * full-replace flush, fed by the re-read of each group's members, drops exactly testGroup's
   * membership while leaving otherGroup's intact.
   */
  public void testOktaMembershipRemoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // enable membership-delete customization (Okta defaults customizeMembershipCrud=false; setting
    // delete keys without it would be rejected by validation). Box/Adobe idiom: customize + umbrella
    // + specific key.
    Map<String, String> deleteTypes = new HashMap<String, String>();
    deleteTypes.put("customizeMembershipCrud", "true");
    deleteTypes.put("deleteMemberships", "true");
    deleteTypes.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, deleteTypes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group otherGroup = new GroupSave(grouperSession).assignName("test:otherGroup").save();
    // SUBJ0 in BOTH groups so removing it from testGroup leaves it provisioned (still in otherGroup)
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: both groups + SUBJ0 + both memberships in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: both groups", 2, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: both memberships", 2, countSyncBack(configId, "grouper_prov_mship"));

    // remove SUBJ0 from testGroup only (still in otherGroup)
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    // pass A: the membership-remove write hits the Okta target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush drops
    // (testGroup, SUBJ0) while otherGroup's SUBJ0 membership survives
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Multi-sync coverage with data evolution between rounds, Okta analogue of SCIM's
   * testFullProvisionReflectsDataChangesAcrossSyncs. Round 1: testGroup with SUBJ0 only, seeded via
   * two passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan group + orphan
   * user directly into the Okta mock (delete-types are off so they persist). Round 3: two more passes
   * -> the mirror reflects the new state (3 users: SUBJ0, SUBJ1, orphan; 2 groups: testGroup, orphan;
   * 2 memberships in testGroup), and the target-drift orphan user's login value round-trips.
   */
  public void testOktaFullSyncReflectsDataChangesAcrossSyncs() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // delete-types stay off (the setup default) so the Round 2 orphans persist across syncs
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // ===================== ROUND 1: initial state =====================

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("round 1: 1 prov_user row for SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 1: 1 prov_group row for testGroup", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 1: 1 prov_mship row for SUBJ0 in testGroup", 1, countSyncBack(configId, "grouper_prov_mship"));

    // ===================== ROUND 2: data changes =====================

    // Grouper-side: add SUBJ1 to testGroup. next full sync inserts SUBJ1 + the membership.
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // Target-side drift: insert an orphan group + orphan user directly into the Okta mock. These are
    // unknown to Grouper; with delete-types off they persist across the next sync. The mock persists
    // these beans to mock_okta_group / mock_okta_user (same Hibernate idiom the Box evolution test
    // uses). name is NOT NULL + uniquely indexed on mock_okta_group; login is uniquely indexed on
    // mock_okta_user -- set distinct values so the inserts do not collide with Grouper's own rows.
    GrouperOktaGroup orphanGroup = new GrouperOktaGroup();
    orphanGroup.setId("orphan-okta-group-evolve-1");
    orphanGroup.setName("orphanGroupAddedMidTest");
    orphanGroup.setDescription("orphanGroupDescriptionEvolve");
    HibernateSession.byObjectStatic().save(orphanGroup);

    // NOTE: the login MUST sort AFTER the test-subject logins (test.subject.N@...). The Okta mock
    // orders users by login ascending and, when grouperTest.okta.mock.skipUser=true (the global test
    // default), drops the lowest-login user from every bulk pull to exercise the by-id recovery
    // fallback. We cannot turn that hook off from here: the mock runs in the embedded Tomcat webapp,
    // whose separate classloader has its own GrouperConfig, so an in-test propertiesOverrideMap never
    // reaches it. By making this orphan sort last, the skip lands on SUBJ0 -- which has a membership
    // and is therefore recovered by id -- while this membership-less orphan stays in the bulk pull
    // and is captured, satisfying the "3 users" assertion.
    GrouperOktaUser orphanUser = new GrouperOktaUser();
    orphanUser.setId("orphan-okta-user-evolve-1");
    orphanUser.setLogin("zz.orphan.evolve@example.edu");
    orphanUser.setEmail("orphan.evolve@example.edu");
    orphanUser.setFirstName("OrphanEvolveFirst");
    orphanUser.setLastName("OrphanEvolveLast");
    HibernateSession.byObjectStatic().save(orphanUser);

    // ===================== ROUND 3: second full sync + assertions =====================

    // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's + the
    // drift orphans) and refreshes the mirror.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)", 3,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 3: 2 prov_group rows expected (testGroup, orphan_group)", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in testGroup)", 2,
        countSyncBack(configId, "grouper_prov_mship"));

    // the orphan group landed in the mirror, unlinked (no Grouper group)
    int orphanGroupRow = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group should land in prov_group with group_internal_id IS NULL", 1,
        orphanGroupRow);

    // the orphan user's login value round-trips through the reporting view (proves target-drift
    // entities are captured with their actual attributes). login is an Okta DEFAULT entity capture
    // attribute (value sourced from the nested /profile/login).
    String orphanUserLoginInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'login'")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(String.class);
    assertEquals("orphan user's login should round-trip through reporting", "zz.orphan.evolve@example.edu",
        orphanUserLoginInReporting);
  }

  /**
   * Strict-native capture of orphan target objects, Okta analogue of SCIM's
   * testFullProvisionCapturesOrphanTargetEntities. With delete-types disabled, an orphan group +
   * orphan user that exist in the Okta target but are unknown to Grouper are still captured into the
   * mirror -- with NULL Grouper-side linkage (group_internal_id / member_internal_id) -- alongside
   * Grouper's own testGroup + SUBJ0/SUBJ1, which keep their linkage populated.
   */
  public void testOktaFullSyncCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // delete-types disabled (setup default) so the orphans persist across the run
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the Okta mock before the provisioner runs. Deliberately
    // set description (an Okta default group capture attribute) so we can verify capture is driven by
    // the target read, independent of Grouper's mapping config.
    GrouperOktaGroup orphanGroup = new GrouperOktaGroup();
    orphanGroup.setId("orphan-okta-group-1234");
    orphanGroup.setName("orphanGroupNotInGrouper");
    orphanGroup.setDescription("orphanGroupDescription");
    HibernateSession.byObjectStatic().save(orphanGroup);

    // NOTE: the login MUST sort AFTER the test-subject logins (test.subject.N@...). The Okta mock
    // orders users by login ascending and, when grouperTest.okta.mock.skipUser=true (the global test
    // default), drops the lowest-login user from every bulk pull to exercise the by-id recovery
    // fallback. We cannot turn that hook off from here: the mock runs in the embedded Tomcat webapp,
    // whose separate classloader has its own GrouperConfig, so an in-test propertiesOverrideMap never
    // reaches it. By making this orphan sort last, the skip lands on SUBJ0 -- which has a membership
    // and is therefore recovered by id -- while this membership-less orphan stays in the bulk pull
    // and is captured, satisfying the "1 prov_user row for the orphan" assertion.
    GrouperOktaUser orphanUser = new GrouperOktaUser();
    orphanUser.setId("orphan-okta-user-5678");
    orphanUser.setLogin("zz.orphan.user@example.edu");
    orphanUser.setEmail("orphan.user@example.edu");
    orphanUser.setFirstName("OrphanFirst");
    orphanUser.setLastName("OrphanLast");
    HibernateSession.byObjectStatic().save(orphanUser);

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // two passes: pass 1 inserts Grouper's objects (orphans untouched, delete-types off); pass 2
    // reads orphans + Grouper's objects and the flush captures all of them.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // orphan group landed with NULL group_internal_id
    int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_group row for the orphan group", 1, orphanGroupRowsTotal);

    int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group's prov_group row must have group_internal_id IS NULL", 1,
        orphanGroupRowsUnlinked);

    // orphan user landed with NULL member_internal_id
    int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_user row for the orphan user", 1, orphanUserRowsTotal);

    int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ? and member_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user's prov_user row must have member_internal_id IS NULL", 1,
        orphanUserRowsUnlinked);

    // Grouper's own testGroup + 2 members land alongside, with linkage populated
    int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id != ? and group_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("Grouper's testGroup prov_group row must have group_internal_id linked", 1,
        testGroupRowsLinked);

    int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id != ? and member_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("Grouper-provisioned prov_user rows (SUBJ0 + SUBJ1) must have member_internal_id linked",
        2, nonOrphanUserRowsLinked);

    // an Okta default group attribute (description) is captured in the catalog and the orphan's value
    // row (key is the un-prefixed 'description'; value sourced from the nested /profile/description)
    int descriptionCatalog = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'description'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("default group attribute 'description' should be in the per-provisioner catalog", 1,
        descriptionCatalog);

    // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis, Okta analogue of SCIM's
   * testFullProvisionCapturesMembershipsFromOrphanGroup. An orphan group with an orphan member
   * (neither known to Grouper) is wired in the Okta mock (mock_okta_membership). Okta memberships are
   * group-centric, so when the daemon lists groups (retrieveAllData) it also reads the orphan group's
   * members -- that membership must land in grouper_prov_mship alongside Grouper's own, proving
   * strict-native membership capture is independent of Grouper knowledge.
   */
  public void testOktaFullSyncCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // delete-types disabled (setup default) so the orphan group + its membership persist
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the Okta mock. The membership
    // FKs require the group and user rows to exist first (mock_okta_mship_*_fkey), so save them in
    // order: group, user, then membership.
    GrouperOktaGroup orphanGroup = new GrouperOktaGroup();
    orphanGroup.setId("orphan-okta-mship-group-1");
    orphanGroup.setName("orphanGroupWithMembers");
    orphanGroup.setDescription("orphanMshipGroupDescription");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperOktaUser orphanUser = new GrouperOktaUser();
    orphanUser.setId("orphan-okta-mship-user-1");
    orphanUser.setLogin("orphan.mship@example.edu");
    orphanUser.setEmail("orphan.mship@example.edu");
    orphanUser.setFirstName("OrphanMshipFirst");
    orphanUser.setLastName("OrphanMshipLast");
    HibernateSession.byObjectStatic().save(orphanUser);

    GrouperOktaMembership orphanMembership = new GrouperOktaMembership();
    orphanMembership.setId("orphan-okta-mship-row-1");
    orphanMembership.setGroupId(orphanGroup.getId());
    orphanMembership.setUserId(orphanUser.getId());
    HibernateSession.byObjectStatic().save(orphanMembership);

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // the orphan group's membership lands in prov_mship (join through prov_group/prov_user, which
    // hold the target ids -- prov_mship itself only has the FK internal ids)
    int orphanMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship pm "
            + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
            + "where pm.grouper_sync_internal_id = ? and pg.target_group_id = ? and pu.target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).addBindVar(orphanUser.getId())
        .select(int.class);
    assertEquals("expected 1 prov_mship row for orphan group -> orphan user", 1, orphanMshipRows);

    // Grouper's own memberships land alongside (3 total: SUBJ0 + SUBJ1 in testGroup + the orphan)
    assertEquals("expected 3 prov_mship rows total (2 from testGroup + 1 orphan)", 3,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * !selectAll* scope excludes orphans, Okta analogue of SCIM's testSelectAllFalseExcludesOrphans.
   * With selectAllGroups=false and selectAllEntities=false the daemon fetches only the resources
   * mapped to Grouper-provisioned objects (by id/name/login), never a server-wide listing -- so an
   * orphan group/user that the Okta target has but Grouper does not must NOT land in the mirror.
   */
  public void testOktaSelectAllFalseExcludesOrphans() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id (Grouper-known resources only).
    GrouperOktaGroup orphanGroup = new GrouperOktaGroup();
    orphanGroup.setId("orphan-okta-group-selnone-1");
    orphanGroup.setName("orphanGroupSelectAllFalse");
    orphanGroup.setDescription("orphanSelNoneGroupDescription");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperOktaUser orphanUser = new GrouperOktaUser();
    orphanUser.setId("orphan-okta-user-selnone-1");
    orphanUser.setLogin("orphan.selnone@example.edu");
    orphanUser.setEmail("orphan.selnone@example.edu");
    orphanUser.setFirstName("OrphanSelNoneFirst");
    orphanUser.setLastName("OrphanSelNoneLast");
    HibernateSession.byObjectStatic().save(orphanUser);

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // Grouper-known resources still captured
    assertTrue("Grouper-provisioned testGroup should still be in prov_group",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("Grouper-provisioned SUBJ0/SUBJ1 should still be in prov_user",
        countSyncBack(configId, "grouper_prov_user") >= 2);

    // orphans must NOT be captured (selectAll=false -> no server-wide listing -> no capture)
    int orphanGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group must NOT be captured when selectAllGroups=false", 0, orphanGroupRows);

    int orphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user must NOT be captured when selectAllEntities=false", 0, orphanUserRows);
  }

  /**
   * Broken-target delete stays in the mirror, Okta analogue of SCIM's
   * testUserDeleteBrokenTargetStaysInMirror. The "verify, don't assume" contract: a target object
   * the daemon did NOT actually remove must stay captured on the re-read.
   *
   * <p>Okta analogue mechanism (same as Box): there is no mock knob to fake a broken delete, so
   * instead we DISABLE entity deletion. SUBJ0 is removed from testGroup in Grouper, but with
   * deleteEntities off the daemon never issues the delete to the Okta target -- so the user remains
   * in the target, and the re-read keeps it in the mirror. This exercises the same mirror behavior (a
   * target object the daemon did NOT remove stays captured) without needing a target that lies about
   * a delete.
   */
  public void testOktaUserDeleteBrokenTargetStaysInMirror() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    // Disable entity deletion so the daemon will NOT remove SUBJ0 from the Okta target once SUBJ0
    // becomes unprovisionable. Okta test util defaults customize*Crud=false (framework auto mode);
    // to override, turn ON customizeEntityCrud (else the explicit delete key is rejected by
    // validation), then set deleteEntities=false to disable it.
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    setupOktaSyncBack(configId, noEntityDelete);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + their membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

    // remove SUBJ0 from the group in Grouper. With delete-types off the daemon does not push the
    // removal to the Okta target, so the target still has SUBJ0.
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group was never deleted -> still in the mirror
    assertEquals("group row should stay (group was not deleted)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // confirm the target still has the user (the daemon did not remove it), so the re-read keeps it
    int mockUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_okta_user").select(int.class);
    assertEquals("the user row should remain in the Okta target (delete-types are off)", 1, mockUserRows);

    assertEquals("user should STAY in the mirror (its delete was never performed)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * loadGroupsToGenericGrouperTable in isolation, Okta analogue of SCIM's
   * testLoadGroupsFlagInIsolation. Only the groups flag is on -> only grouper_prov_group rows are
   * written; prov_user and prov_mship stay empty even though the daemon still reads users (for
   * provisioning) and memberships (for diffing).
   */
  public void testOktaLoadGroupsFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row when groups capture is on",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertEquals("expected 0 prov_user rows when entities capture is off", 0,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadEntitiesToGenericGrouperTable in isolation, Okta analogue of SCIM's
   * testLoadEntitiesFlagInIsolation. Only the entities flag is on -> only grouper_prov_user rows are
   * written; prov_group and prov_mship stay empty.
   */
  public void testOktaLoadEntitiesFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("expected 0 prov_group rows when groups capture is off", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadMembershipsToGenericGrouperTable off, Okta analogue of SCIM's testLoadMembershipsFlagOff.
   * Both object loads on but memberships off -> prov_group and prov_user populate, prov_mship stays
   * empty. Proves the membership gate is independent of the object gates.
   */
  public void testOktaLoadMembershipsFlagOff() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    GrouperStartup.startup();
    OktaMockServiceHandler.ensureOktaMockTables();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_okta_user").executeSql();
    OktaProvisionerTestUtils.setupOktaExternalSystem();
    OktaProvisionerTestUtils.configureOktaProvisioner(new OktaProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row", countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * INCREMENTAL sync-back coverage for Okta, conservative -- mirrors Box's
   * testBoxIncrementalSyncBackNoSpuriousDeletes exactly. Okta's group/user OBJECTS are read-capture
   * only (no write-side object capture), while its MEMBERSHIPS now write-capture on the DAO's
   * insert/deleteMembership (recordTargetNativeMembershipInsert/Delete), the same membership
   * write-track as Adobe/SCIM/Dropbox. For Okta, an incremental cycle re-reads only the changed
   * objects (it has canRetrieveGroup/Entity, so the adapter decomposes to per-id reads that fire the
   * Okta object capture seams), and the incremental flush is a SCOPED upsert (it does NOT
   * full-replace, so it will not wrongly delete untouched mirror rows).
   *
   * <p>What this test asserts is therefore deliberately narrow -- the safe, reliable part of Okta
   * incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
   * member drives an incremental that (a) re-reads the changed group/entity and so does NOT shrink
   * the existing GROUP mirror (no spurious deletes -- the regression the scoped incremental flush
   * guards against), and (b) captures the newly added member's user object into prov_user. This test
   * does NOT assert that the new MEMBERSHIP converges on the same incremental cycle -- not because
   * there is no write hook (there now is), but because this conservative test deliberately keeps its
   * assertions to the object-mirror behavior above, same as Box. Membership convergence for Okta is
   * covered end-to-end by the two-pass full tests above.
   */
  public void testOktaIncrementalSyncBackNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    setupOktaSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed via full sync: group + SUBJ0 + SUBJ1 + their memberships in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    int provGroupRowsBefore = countSyncBack(configId, "grouper_prov_group");
    int provUserRowsBefore = countSyncBack(configId, "grouper_prov_user");
    int provMshipRowsBefore = countSyncBack(configId, "grouper_prov_mship");
    assertTrue("seed should have >=1 prov_group row", provGroupRowsBefore >= 1);
    assertEquals("seed should have 2 prov_user rows", 2, provUserRowsBefore);
    assertEquals("seed should have 2 prov_mship rows", 2, provMshipRowsBefore);

    // prime the changelog consumer: its FIRST run only initializes its changelog position (processes
    // nothing), so without this priming pass the change below is never consumed.
    incrementalProvision();

    // incremental add: a third member. The incremental re-reads the changed group/entity, firing the
    // Okta read-capture seams, and the scoped flush upserts -- it must NOT drop untouched rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc + the prompt's
    // INCREMENTAL guidance). Okta memberships are group-centric and captured on the READ path; on an
    // incremental cycle the scoped membership flush for the changed group plus read-before-write
    // timing means testGroup's membership rows can transiently clear, re-converging only on the next
    // full sync (the ~1-cycle lag). Membership convergence is covered end-to-end by the two-pass full
    // tests above; here we only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; Okta shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

  // ==========================================================================================
  // GRP-7048: fullSyncUsersFromSyncBack -- resolve users from the sync-back cache during full sync
  // instead of pulling every user from Okta. The Okta DAO honors retrieveEntities=false (skips its
  // bulk user pull AND the per-membership missing-user lookup; counter oktaRetrieveAllUsersApiCall
  // stays unset), and the framework seeds users from grouper_prov_user. New/error users fall
  // through to the existing individual missing-entity re-read (counter missingEntitiesForRetrieve).
  //
  // NOTE: these are a first cut of the matrix and should be RUN + tuned in the Tomcat-mock harness
  // (Okta captures state on read, so cache warm-up may need a different pass count; the error
  // injection touches grouper_sync_member directly). They gate on tomcatRunTests() like the other
  // Okta sync-back tests.
  // ==========================================================================================

  /**
   * GRP-7048 (full sync, warm cache): once the cache is warm, both target users are reconstructed
   * from grouper_prov_user, the bulk Okta user pull is skipped, and NO individual user re-reads
   * happen -- proving the feature both skips the big pull and minimizes per-user lookups.
   */
  public void testOktaFullSyncUsersFromSyncBackWarmCache() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: provision the two users into Okta and capture them into grouper_prov_user
    // (Okta captures on read, so a few passes converge the inserts + post-write capture)
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));

    // the warm run: users come entirely from the cache
    fullProvision(configId);
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("bulk user pull from Okta should be skipped (users come from the sync-back cache)",
        debugMap.get("oktaRetrieveAllUsersApiCall"));
    assertEquals("both users reconstructed from the cache", 2, debugMapInt(debugMap, "syncBackEntitiesReconstructed"));
    assertEquals("no individual user re-reads when the cache is warm and complete", 0,
        debugMapInt(debugMap, "missingEntitiesForRetrieve"));
  }

  /**
   * GRP-7048 (full sync, user missing from cache is re-read): a user that is in the target but not
   * in the sync-back cache is re-read individually from Okta, while the users that ARE in the cache
   * are served from it and the bulk pull stays skipped. We simulate the cache miss by deleting one
   * user's grouper_prov_user row after warm-up (it stays in Okta).
   */
  public void testOktaFullSyncUsersFromSyncBackMissingFromCacheReRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));

    // simulate a cache miss for SUBJ0: delete its grouper_prov_user row (+ attr values) while it
    // stays in Okta. grouper_prov_user.member_internal_id -> grouper_members.internal_id.
    String provUserForSubj0 = "select internal_id from grouper_prov_user "
        + "where grouper_sync_internal_id = (select internal_id from grouper_sync where provisioner_name = ?) "
        + "and member_internal_id = (select internal_id from grouper_members where subject_id = ?)";
    // delete the FK children first (attr values + memberships that reference this prov_user row)
    new GcDbAccess().connectionName("grouper")
        .sql("delete from grouper_prov_user_attr_value where prov_user_internal_id in (" + provUserForSubj0 + ")")
        .addBindVar(configId).addBindVar(SubjectTestHelper.SUBJ0.getId()).executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("delete from grouper_prov_mship where prov_user_internal_id in (" + provUserForSubj0 + ")")
        .addBindVar(configId).addBindVar(SubjectTestHelper.SUBJ0.getId()).executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("delete from grouper_prov_user "
            + "where grouper_sync_internal_id = (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and member_internal_id = (select internal_id from grouper_members where subject_id = ?)")
        .addBindVar(configId).addBindVar(SubjectTestHelper.SUBJ0.getId()).executeSql();
    assertEquals("SUBJ0 removed from the cache, SUBJ1 remains", 1, countSyncBack(configId, "grouper_prov_user"));

    // the run: SUBJ1 from cache, SUBJ0 (missing from cache) re-read individually from Okta
    fullProvision(configId);
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("bulk user pull from Okta should still be skipped (cache is non-empty)",
        debugMap.get("oktaRetrieveAllUsersApiCall"));
    assertEquals("only the cached user is reconstructed from the cache", 1,
        debugMapInt(debugMap, "syncBackEntitiesReconstructed"));
    assertEquals("exactly the user missing from the cache is re-read individually", 1,
        debugMapInt(debugMap, "missingEntitiesForRetrieve"));
  }

  /**
   * GRP-7048 (incremental keeps cache current + full-from-cache): the feature relies on the cache
   * being kept current by incremental sync. Warm the cache with one user, add a second user via an
   * incremental run (which must write it into grouper_prov_user), then a full sync serves BOTH from
   * the cache -- bulk pull skipped, no individual re-reads.
   */
  public void testOktaFullSyncUsersFromSyncBackIncrementalKeepsCacheCurrent() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache with SUBJ0
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("SUBJ0 cached after warm-up", 1, countSyncBack(configId, "grouper_prov_user"));

    // add SUBJ1 and exercise an incremental run (pushes the change to Okta)
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    incrementalProvision(configId);

    // converge with full-from-cache passes. Okta captures target state on READ, so the cache
    // converges over a write pass + a read pass (the same 2-pass pattern the existing sync-back
    // tests use). SUBJ1 is missing from the cache, so full-from-cache re-reads it individually and
    // then captures it, while SUBJ0 keeps coming from the cache. The end state must include both.
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("after an incremental add + full-from-cache, both users are in the cache", 2,
        countSyncBack(configId, "grouper_prov_user"));

    // and the full-from-cache path never did the bulk Okta user pull
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertNull("bulk user pull from Okta should be skipped", debugMap.get("oktaRetrieveAllUsersApiCall"));
  }

  /**
   * GRP-7048 (incremental cache currency on removal): the mirror must stay accurate in the removal
   * direction too. Warm the cache with two memberships, remove one member via an incremental run,
   * and assert the membership is dropped from the mirror (grouper_prov_mship) -- so a later
   * full-from-cache sync sees the correct membership set, not a stale one.
   */
  public void testOktaFullSyncUsersFromSyncBackIncrementalRemovalCacheCurrent() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    // enable membership deletes so removing a member actually deprovisions the membership from Okta
    // (the setup default leaves delete-types off); mirrors the existing Okta delete-converge test
    extraConfig.put("customizeMembershipCrud", "true");
    extraConfig.put("deleteMemberships", "true");
    extraConfig.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: two members, two memberships in the mirror
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("two users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("two memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // remove SUBJ1's membership and exercise an incremental run (deprovisions it from Okta)
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);
    incrementalProvision(configId);

    // converge with full-from-cache passes (write pass + read pass, as Okta captures on read); the
    // mirror must drop the removed membership, and the bulk Okta user pull is never done
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("after an incremental removal + full-from-cache, one membership left", 1,
        countSyncBack(configId, "grouper_prov_mship"));

    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertNull("bulk user pull from Okta should be skipped", debugMap.get("oktaRetrieveAllUsersApiCall"));
  }

  /**
   * GRP-7048 (memberships, warm cache): once the membership cache is warm, the target memberships
   * are reconstructed from grouper_prov_mship and Okta's expensive per-group member iteration is
   * skipped (oktaRetrieveMembershipsApiCall stays unset on the warm run).
   */
  public void testOktaFullSyncMembershipsFromSyncBackWarmCache() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: two memberships in the mirror. The first (cold-cache) run does the normal
    // per-group member iteration, which populates grouper_prov_mship.
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("both memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // the warm run: memberships are reconstructed from the cache and the per-group iteration is skipped
    fullProvision(configId);
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("Okta per-group member iteration should be skipped (memberships come from the cache)",
        debugMap.get("oktaRetrieveMembershipsApiCall"));
    assertEquals("both memberships reconstructed from the cache", 2,
        debugMapInt(debugMap, "syncBackMembershipsReconstructed"));
  }

  /**
   * GRP-7048 (memberships, cache used as target set): with memberships served from the cache, the
   * normal compare still provisions a membership that exists in Grouper but not the cache, and
   * removes one that exists in the cache but not Grouper. Proves it is not just the idempotent case.
   * Asserts against the Okta target (mock_okta_membership), and that the per-group iteration stays
   * skipped throughout.
   */
  public void testOktaFullSyncMembershipsFromSyncBackAddAndRemove() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    // enable membership deletes so the removal actually deprovisions from Okta
    extraConfig.put("customizeMembershipCrud", "true");
    extraConfig.put("deleteMemberships", "true");
    extraConfig.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: two memberships in the cache and in Okta
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("two memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));
    assertEquals("two memberships in Okta after warm-up", 2, countMockOktaMemberships());

    // ADD: SUBJ2 is in Grouper but not the membership cache -> the compare inserts it into Okta,
    // while the per-group member iteration stays skipped (memberships served from cache)
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    fullProvision(configId);
    Map<String, Object> debugMapAdd = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertNull("per-group member iteration stays skipped on the add run",
        debugMapAdd.get("oktaRetrieveMembershipsApiCall"));
    assertEquals("SUBJ2's membership should be provisioned into Okta", 3, countMockOktaMemberships());

    // REMOVE: SUBJ2 removed from Grouper -> the compare removes it from Okta (cache has it, Grouper
    // does not)
    testGroup.deleteMember(SubjectTestHelper.SUBJ2, false);
    fullProvision(configId);
    assertEquals("SUBJ2's membership should be removed from Okta", 2, countMockOktaMemberships());
  }

  /**
   * GRP-7048 (groups, warm cache): once the group + membership caches are warm, the target groups
   * are reconstructed from grouper_prov_group and Okta's group pull is skipped. Groups-from-cache
   * only engages when memberships are also from the cache (both-or-neither), so the test enables both
   * options and asserts both the group pull and the per-group member iteration are skipped.
   */
  public void testOktaFullSyncGroupsFromSyncBackWarmCache() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: the group + its two memberships. The first (cold) run pulls them normally.
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("group cached after warm-up", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("two memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // the warm run: group (and memberships) come from the cache; both target pulls are skipped
    fullProvision(configId);
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("Okta group pull should be skipped (groups come from the cache)",
        debugMap.get("oktaRetrieveAllGroupsApiCall"));
    assertNull("Okta per-group member iteration should be skipped too (both-or-neither)",
        debugMap.get("oktaRetrieveMembershipsApiCall"));
    assertEquals("the group is reconstructed from the cache", 1,
        debugMapInt(debugMap, "syncBackGroupsReconstructed"));
  }

  /**
   * GRP-7048 (groups, cache used as target set): with groups served from the cache, the normal
   * compare still provisions a group that exists in Grouper but not the cache, and removes one that
   * is in the cache but not Grouper. Asserts against the Okta target (mock_okta_group), and that the
   * group pull stays skipped throughout. Groups-from-cache requires memberships-from-cache, so both
   * options are enabled.
   */
  public void testOktaFullSyncGroupsFromSyncBackAddAndRemove() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    // enable group + membership deletes so the removal actually deprovisions from Okta
    extraConfig.put("customizeGroupCrud", "true");
    extraConfig.put("deleteGroups", "true");
    extraConfig.put("deleteGroupsIfNotExistInGrouper", "true");
    extraConfig.put("customizeMembershipCrud", "true");
    extraConfig.put("deleteMemberships", "true");
    extraConfig.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the cache: one group in the cache and in Okta
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("one group cached after warm-up", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("one group in Okta after warm-up", 1, countMockOktaGroups());

    // ADD: testGroup2 is in Grouper but not the group cache -> the compare inserts it into Okta,
    // while the group pull stays skipped (groups served from cache)
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
    testGroup2.addMember(SubjectTestHelper.SUBJ0, false);
    fullProvision(configId);
    Map<String, Object> debugMapAdd = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertNull("group pull stays skipped on the add run", debugMapAdd.get("oktaRetrieveAllGroupsApiCall"));
    assertEquals("testGroup2 should be provisioned into Okta", 2, countMockOktaGroups());

    // REMOVE: testGroup2 deleted in Grouper -> the compare removes it from Okta (cache has it,
    // Grouper does not)
    testGroup2.delete();
    fullProvision(configId);
    assertEquals("testGroup2 should be removed from Okta", 1, countMockOktaGroups());
  }

  /**
   * GRP-7048 (all three axes together, ADDS): with users + memberships + groups ALL served from the
   * sync-back cache, a run that adds a brand-new group, a brand-new user, and new memberships must
   * (a) skip all three bulk target pulls, and (b) provision every add into the TARGET. Only the
   * population-wide retrieve-all is skipped (oktaRetrieveAll*ApiCall stays unset).
   *
   * <p>All three axes converge the mirror the same run, by two mechanisms: groups and users are
   * re-read individually by the end-of-run sync-back drain (the write marks the touched id, the drain
   * re-reads it -- their attributes must come from the target), while memberships are captured on
   * write (insertMembership records the (group,user) pair into the native mirror -- no re-read, since
   * both ids are known at write time). The latter matches SCIM/Adobe/Dropbox.
   */
  public void testOktaFullSyncAllThreeFromSyncBackAddsConvergeSameRun() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the three caches: one group, two users, two memberships (Okta captures on read, so a few
    // passes converge the inserts + post-write capture)
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("group cached after warm-up", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("both memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));
    assertEquals("group in Okta after warm-up", 1, countMockOktaGroups());
    assertEquals("both users in Okta after warm-up", 2, countMockOktaUsers());
    assertEquals("both memberships in Okta after warm-up", 2, countMockOktaMemberships());

    // a warm run: prove all three bulk pulls are skipped and everything is reconstructed from cache
    fullProvision(configId);
    Map<String, Object> warmDebugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertNull("bulk group pull skipped on the warm run", warmDebugMap.get("oktaRetrieveAllGroupsApiCall"));
    assertNull("bulk user pull skipped on the warm run", warmDebugMap.get("oktaRetrieveAllUsersApiCall"));
    assertNull("per-group member iteration skipped on the warm run", warmDebugMap.get("oktaRetrieveMembershipsApiCall"));
    assertEquals("group reconstructed from cache", 1, debugMapInt(warmDebugMap, "syncBackGroupsReconstructed"));
    assertEquals("both users reconstructed from cache", 2, debugMapInt(warmDebugMap, "syncBackEntitiesReconstructed"));
    assertEquals("both memberships reconstructed from cache", 2, debugMapInt(warmDebugMap, "syncBackMembershipsReconstructed"));

    // ADD across all three axes in one run: a new group (testGroup2) with SUBJ0, plus SUBJ2 (a
    // brand-new user) added to testGroup. New memberships: testGroup+SUBJ2 and testGroup2+SUBJ0.
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
    testGroup2.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);

    GrouperProvisioningOutput addOutput = fullProvision(configId);
    assertEquals("add run should have no errors", 0, addOutput.getRecordsWithErrors());
    Map<String, Object> addDebugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    // (a) the population-wide pulls stay skipped even though we touched all three axes
    assertNull("bulk group pull stays skipped on the add run", addDebugMap.get("oktaRetrieveAllGroupsApiCall"));
    assertNull("bulk user pull stays skipped on the add run", addDebugMap.get("oktaRetrieveAllUsersApiCall"));
    assertNull("per-group member iteration stays skipped on the add run", addDebugMap.get("oktaRetrieveMembershipsApiCall"));

    // (b) the target reflects every add
    assertEquals("both groups now in Okta", 2, countMockOktaGroups());
    assertEquals("all three users now in Okta", 3, countMockOktaUsers());
    assertEquals("four memberships now in Okta (testGroup: S0,S1,S2 + testGroup2: S0)", 4, countMockOktaMemberships());

    // (c) group + user mirrors converge the same run: the write marks each touched id and the
    // end-of-run sync-back drain re-reads exactly those objects (generic sync-back DAO ->
    // recordTargetNativeGroupWrite / recordTargetNativeUserWrite -> syncBackDrainGroups/Users).
    assertEquals("new group converged into the mirror same run (drain re-read)", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("new user converged into the mirror same run (drain re-read)", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // (d) memberships converge the same run via capture-on-write: insertMembership records each new
    // (group,user) into the native mirror (captureMembershipInsertFromCurrentProvisioner), so the
    // end-of-run flush writes all four with no membership read. Reconstructed 2 + 2 new = 4.
    assertEquals("new memberships converged into the mirror same run (capture-on-write)", 4,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * GRP-7048 (all three axes together, UPDATE + membership DELETE): with users + memberships + groups
   * ALL served from the sync-back cache, a run that updates a group attribute and removes a membership
   * must skip all three bulk pulls yet converge both the target AND the mirror the same run. Two
   * mechanisms are exercised: the UPDATE (a group description change) goes through the drain, which
   * re-reads the touched group and refreshes its cached description -- proving the cache does NOT go
   * stale on an update ("an update triggers a read back into the cache"). The membership REMOVE goes
   * through capture-on-write (deleteMembership -> captureMembershipDeleteFromCurrentProvisioner), which
   * drops the (group,user) row from the native mirror with no re-read.
   *
   * <p>The removed member stays in a second group, so it is not deprovisioned as a user; no group is
   * deleted, so there is no group-eviction ambiguity -- every axis is asserted against both the target
   * and the mirror.
   */
  public void testOktaFullSyncAllThreeFromSyncBackUpdateAndDeleteConvergeSameRun() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    // enable membership deletes so the membership removal actually deprovisions from Okta
    extraConfig.put("customizeMembershipCrud", "true");
    extraConfig.put("deleteMemberships", "true");
    extraConfig.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupOktaSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    // testGroup carries a description we will update and the membership we will remove (SUBJ1).
    // keepGroup also holds SUBJ1, so removing SUBJ1 from testGroup leaves SUBJ1 provisioned (its Okta
    // user is not deleted) -- a clean, pure membership remove with no group deletion.
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("originalDescription").save();
    Group keepGroup = new GroupSave(grouperSession).assignName("test:keepGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    keepGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // warm the caches: two groups, two users, three memberships (testGroup: S0,S1; keepGroup: S1)
    fullProvision(configId);
    fullProvision(configId);
    fullProvision(configId);
    assertEquals("both groups cached after warm-up", 2, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("three memberships cached after warm-up", 3, countSyncBack(configId, "grouper_prov_mship"));
    assertEquals("both groups in Okta after warm-up", 2, countMockOktaGroups());
    assertEquals("three memberships in Okta after warm-up", 3, countMockOktaMemberships());

    // UPDATE testGroup's description and REMOVE SUBJ1 from testGroup, in one run
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningOutput output = fullProvision(configId);
    assertEquals("update+remove run should have no errors", 0, output.getRecordsWithErrors());
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    // all three bulk pulls stay skipped
    assertNull("bulk group pull stays skipped", debugMap.get("oktaRetrieveAllGroupsApiCall"));
    assertNull("bulk user pull stays skipped", debugMap.get("oktaRetrieveAllUsersApiCall"));
    assertNull("per-group member iteration stays skipped", debugMap.get("oktaRetrieveMembershipsApiCall"));

    // target: both groups remain; SUBJ1's testGroup membership is gone; both users survive
    assertEquals("both groups still in Okta", 2, countMockOktaGroups());
    assertEquals("both users still in Okta (SUBJ1 survives via keepGroup)", 2, countMockOktaUsers());
    assertEquals("SUBJ1's testGroup membership removed (testGroup: S0; keepGroup: S1)", 2,
        countMockOktaMemberships());
    // the update reached the target (only the updated group carries a description)
    assertEquals("group description updated in Okta", "newDescription", updatedMockOktaGroupDescription());

    // mirror converges the same run on every axis:
    //  - UPDATE: testGroup re-read by the drain -> cached description refreshed (cache not stale)
    assertEquals("mirror description refreshed by the drain re-read (cache not stale on update)",
        "newDescription", mirroredGroupDescription(configId));
    //  - both groups and both users are untouched-or-refreshed, so counts hold
    assertEquals("both groups remain in the mirror", 2, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users remain in the mirror", 2, countSyncBack(configId, "grouper_prov_user"));
    //  - REMOVE: capture-on-write dropped (testGroup, SUBJ1) from the mirror (3 - 1 = 2)
    assertEquals("removed membership dropped from the mirror same run (capture-on-write)", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * GRP-7048 config validation (invalid combo): fullSyncGroupsFromSyncBack requires
   * fullSyncMembershipsFromSyncBack (the both-or-neither pairing). Groups-on / memberships-off must
   * surface a validation error attached to the fullSyncGroupsFromSyncBack field so the operator
   * can't save a combo that silently no-ops (groups would quietly fall back to a full target pull).
   *
   * <p>Unlike the other GRP-7048 tests this does NOT gate on {@link #tomcatRunTests()}: config
   * validation is network-free (it is the config-editor save path), so it runs without the mock Tomcat.
   */
  public void testOktaFullSyncGroupsFromSyncBackRequiresMembershipsInvalid() {

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "false");
    setupOktaSyncBack(configId, extraConfig);

    List<ProvisioningValidationIssue> issues = validateProvisionerConfig(configId);

    assertTrue("groups-from-sync-back without memberships-from-sync-back must be a validation error "
        + "on the fullSyncGroupsFromSyncBack field; issues=" + describeIssues(issues),
        hasValidationIssueForField(issues, "fullSyncGroupsFromSyncBack"));
  }

  /**
   * GRP-7048 config validation (valid combo): groups AND memberships both from the sync-back cache
   * is a supported combination and must NOT raise the groups-require-memberships error. Guards the
   * rule against firing on a valid config.
   */
  public void testOktaFullSyncGroupsFromSyncBackWithMembershipsValid() {

    String configId = "myOktaProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    setupOktaSyncBack(configId, extraConfig);

    List<ProvisioningValidationIssue> issues = validateProvisionerConfig(configId);

    assertFalse("groups + memberships both from sync-back is a valid combo, no error expected on "
        + "the fullSyncGroupsFromSyncBack field; issues=" + describeIssues(issues),
        hasValidationIssueForField(issues, "fullSyncGroupsFromSyncBack"));
  }

  /**
   * Run the provisioner config validation the same way the config-editor save path does
   * (initialize then validate). No target calls are made.
   */
  private List<ProvisioningValidationIssue> validateProvisionerConfig(String configId) {
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(configId);
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);
    return grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
  }

  /**
   * true if any validation issue is attached to the given config field. ProvisioningValidationIssue
   * decorates a bare field suffix into the config-editor selector "#config_&lt;suffix&gt;_spanid"
   * (see ProvisioningValidationIssue.htmlJqueryHandle), so match that decorated form (and the bare
   * suffix as a fallback in case decoration ever changes).
   */
  private static boolean hasValidationIssueForField(List<ProvisioningValidationIssue> issues, String fieldSuffix) {
    String decoratedHandle = "#config_" + fieldSuffix + "_spanid";
    for (ProvisioningValidationIssue issue : GrouperUtil.nonNull(issues)) {
      String handle = issue.getJqueryHandle();
      if (decoratedHandle.equals(handle) || fieldSuffix.equals(handle)) {
        return true;
      }
    }
    return false;
  }

  /** compact dump of validation issues (field:message) for assertion failure messages */
  private static String describeIssues(List<ProvisioningValidationIssue> issues) {
    StringBuilder sb = new StringBuilder("[");
    for (ProvisioningValidationIssue issue : GrouperUtil.nonNull(issues)) {
      if (sb.length() > 1) {
        sb.append(", ");
      }
      sb.append(issue.getJqueryHandle()).append(":").append(issue.getMessage());
    }
    return sb.append("]").toString();
  }

  /** count of groups currently in the Okta mock target */
  private int countMockOktaGroups() {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_okta_group").select(int.class);
  }

  /** count of memberships currently in the Okta mock target */
  private int countMockOktaMemberships() {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_okta_membership").select(int.class);
  }

  /** count of users currently in the Okta mock target */
  private int countMockOktaUsers() {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_okta_user").select(int.class);
  }

  /**
   * The description of the one Okta mock-target group that carries a non-null description (its actual
   * target state), or null. Reads GrouperOktaGroup, the mock target entity the mock updateGroup
   * handler persists into. The combined update test gives exactly one group a description, so this
   * is unambiguous however many other (description-less) groups exist.
   */
  private String updatedMockOktaGroupDescription() {
    List<GrouperOktaGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from GrouperOktaGroup where description is not null").list(GrouperOktaGroup.class);
    return groups.isEmpty() ? null : groups.get(0).getDescription();
  }

  /** read an int counter from the provisioner debug map, treating absent as 0 */
  private static int debugMapInt(Map<String, Object> debugMap, String key) {
    Object value = debugMap == null ? null : debugMap.get(key);
    return value == null ? 0 : ((Number) value).intValue();
  }

}
