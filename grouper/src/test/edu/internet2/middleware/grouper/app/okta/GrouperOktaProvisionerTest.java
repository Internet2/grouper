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
    TestRunner.run(new GrouperOktaProvisionerTest("testFullSyncOktaReal"));
    
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
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      
      OktaProvisionerTestUtils.setupOktaExternalSystem();
      
      OktaProvisioningStartWith startWith = new OktaProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("googleExternalSystemConfigId", "myOkta");
      startWithSuffixToValue.put("googlePattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("selectAllGroups", "true");
      startWithSuffixToValue.put("manageGroups", "true");
      startWithSuffixToValue.put("groupNameAttributeValue", "extension");
      startWithSuffixToValue.put("groupEmailAttributeValue", "name");
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityEmailSubjectAttribute", "email");
      startWithSuffixToValue.put("entityFamilyName", "name");
      startWithSuffixToValue.put("entityGivenName", "subjectId");
      
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
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
  
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
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
//      
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
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
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
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
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
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

    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
    //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
    
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
    
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));

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
  
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
//      //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
      
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
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
  
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
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
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
  
  
  public void testFullSyncOktaReal() throws IOException {
    
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

}
