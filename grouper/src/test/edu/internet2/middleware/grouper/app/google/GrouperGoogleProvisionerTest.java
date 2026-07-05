package edu.internet2.middleware.grouper.app.google;

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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import junit.textui.TestRunner;


public class GrouperGoogleProvisionerTest extends GrouperProvisioningBaseTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    TestRunner.run(new GrouperGoogleProvisionerTest("testSkipPreExistingTargetGroup"));
    
  }
  
  @Override
  public String defaultConfigId() {
    return "myGoogleProvisioner";
  }

  public GrouperGoogleProvisionerTest(String name) {
    super(name);
  }
  
  private boolean startTomcat = false;
  
  
  public void testNyuGoogleProvisioner() throws IOException {
    
    /**
     * 
         * changeLog.consumer.nyushanghaiggl.allowExternalMembers = false
    changeLog.consumer.nyushanghaiggl.allowGoogleCommunication = false
    changeLog.consumer.nyushanghaiggl.allowWebPosting = true
    changeLog.consumer.nyushanghaiggl.class = edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsChangeLogConsumer
    changeLog.consumer.nyushanghaiggl.defaultMessageDenyNotificationText = Your message has been denied.
    changeLog.consumer.nyushanghaiggl.deprovisionUsers = false
    changeLog.consumer.nyushanghaiggl.domain = gqa.nyu.edu
    changeLog.consumer.nyushanghaiggl.googleGroupCacheValidityPeriod = 30
    changeLog.consumer.nyushanghaiggl.googleGroupFilter = ^shanghai.(.*)-grpr$
    changeLog.consumer.nyushanghaiggl.googleUserCacheValidityPeriod = 30
    changeLog.consumer.nyushanghaiggl.groupIdentifierExpression = shanghai.${groupPath.replace("app:nyushanghai:", "")}-grpr
    changeLog.consumer.nyushanghaiggl.grouperIsAuthoritative = TRUE
    changeLog.consumer.nyushanghaiggl.handleDeletedGroup = archive
    changeLog.consumer.nyushanghaiggl.ignoreExtraGoogleMembers = false
    changeLog.consumer.nyushanghaiggl.includeInGlobalAddressList = false
    changeLog.consumer.nyushanghaiggl.isArchived = false
    changeLog.consumer.nyushanghaiggl.maxMessageBytes = 26214400
    changeLog.consumer.nyushanghaiggl.membersCanPostAsTheGroup = false
    changeLog.consumer.nyushanghaiggl.messageDisplayFont = DEFAULT_FONT
    changeLog.consumer.nyushanghaiggl.messageModerationLevel = MODERATE_NONE
    changeLog.consumer.nyushanghaiggl.prefillGoogleCachesForConsumer = true
    changeLog.consumer.nyushanghaiggl.prefillGoogleCachesForFullSync = true
    changeLog.consumer.nyushanghaiggl.primaryLanguage = en
    changeLog.consumer.nyushanghaiggl.provisionUsers = false
    changeLog.consumer.nyushanghaiggl.quartzCron = 0 * * * * ?
    changeLog.consumer.nyushanghaiggl.replyTo = REPLY_TO_IGNORE
    changeLog.consumer.nyushanghaiggl.retryOnError = false
    changeLog.consumer.nyushanghaiggl.sendMessageDenyNotification = true
    changeLog.consumer.nyushanghaiggl.serviceAccountEmail = qagrouper@grouper-337401.iam.gserviceaccount.com
    changeLog.consumer.nyushanghaiggl.serviceAccountPKCS12FilePath = /etc/pki/tls/grouper-337401-608cedd32f87.p12
    changeLog.consumer.nyushanghaiggl.serviceImpersonationUser = grouperadmin@gqa.nyu.edu
    changeLog.consumer.nyushanghaiggl.showInGroupDirectory = false
    changeLog.consumer.nyushanghaiggl.simpleSubjectNaming = false
    changeLog.consumer.nyushanghaiggl.spamModerationLevel = ALLOW
    changeLog.consumer.nyushanghaiggl.subjectGivenNameField = givenName
    changeLog.consumer.nyushanghaiggl.subjectIdentifierExpression = ${subjectId}
    changeLog.consumer.nyushanghaiggl.subjectSurnameField = sn
    changeLog.consumer.nyushanghaiggl.useBatch = true
    changeLog.consumer.nyushanghaiggl.whoCanInvite = ALL_MANAGERS_CAN_INVITE
    changeLog.consumer.nyushanghaiggl.whoCanJoin = INVITED_CAN_JOIN
    changeLog.consumer.nyushanghaiggl.whoCanManage = update
    changeLog.consumer.nyushanghaiggl.whoCanPostMessage = ALL_MANAGERS_CAN_POST
    changeLog.consumer.nyushanghaiggl.whoCanViewMembership = ALL_MEMBERS_CAN_VIEW
    otherJob.nyushanghaiggl_full.changeLogConsumer = nyushanghaiggl
    otherJob.nyushanghaiggl_full.class = edu.internet2.middleware.changelogconsumer.googleapps.GoogleAppsOtherJob
    otherJob.nyushanghaiggl_full.quartzCron = 46 50 14 * * ?
     */
    
  }
  
  
  //need to run individually
  public void testFullSyncGoogleStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      
      GoogleProvisionerTestUtils.setupGoogleExternalSystem();
      
      GoogleProvisioningStartWith startWith = new GoogleProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("googleExternalSystemConfigId", "myGoogle");
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
      
      startWith.manipulateProvisionerConfigurationValue("myGoogleProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myGoogleProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myGoogleProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myGoogleProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myGoogleProvisioner.provisionerConfigId").value("myGoogleProvisioner").store();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      
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
      
      assertEquals("testGroup", grouperGoogleGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperGoogleGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
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
  
  public void testIncrementalSyncGoogle() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput());
  
    GrouperStartup.startup();
    
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
//  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
//      
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
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
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      incrementalProvision();
  
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
//      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
//      
//      assertEquals("test:testGroup", grouperGoogleGroup.getName());
//      
//      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
//      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
//      
//      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
//      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
//      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
//      assertEquals(grouperGoogleGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
      incrementalProvision();
      
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
      //now add the same subject again
      testGroup.addMember(SubjectTestHelper.SUBJ1);
      incrementalProvision();
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      incrementalProvision();
      
      GrouperGoogleGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      assertEquals("ALL_MEMBERS_CAN_VIEW", groupWithUpdatedDescription.getWhoCanViewGroup());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  
  public void testDoNotExistErrorCode() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
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
    List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);

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
    attributeValue.setDoProvision("myGoogleProvisioner");
    attributeValue.setTargetName("myGoogleProvisioner");
    attributeValue.setStemScopeString("sub");
    
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));

    assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
    
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    
    List<GcGrouperSyncMembership> grouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIds(GrouperUtil.toSet(testGroup.getId()));
    
    assertEquals(2, grouperSyncMemberships.size());
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership: grouperSyncMemberships) {
      assertEquals("DNE", gcGrouperSyncMembership.getErrorCode().toString());
    }
    
  }

  public void testFullSyncGoogle() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
      
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
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      
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
      
      assertEquals("test:testGroup", grouperGoogleGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperGoogleGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add the same subject again
      testGroup.addMember(SubjectTestHelper.SUBJ1);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      GrouperGoogleGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      assertEquals("ALL_MEMBERS_CAN_VIEW", groupWithUpdatedDescription.getWhoCanViewGroup());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
    } finally {
      
    }

  }
  
  
  /**
   * verifies that when skipIfTargetGroupExists is on and a target group already exists on first encounter,
   * Grouper skips it (no insert, no membership sync), the grouper_sync_group row gets the SKP error code,
   * and inTarget is not set to true.
   */
  public void testSkipPreExistingTargetGroup() throws IOException {

    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput());

    GrouperStartup.startup();

    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");

      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      // phase 1: run a normal full sync so Grouper creates the target group naturally.
      // this also lets the translation engine compute the matching email for us.
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());

      GrouperGoogleGroup preExistingTargetGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      String preExistingTargetGroupEmail = preExistingTargetGroup.getEmail();
      String preExistingTargetGroupId = preExistingTargetGroup.getId();

      // phase 2: simulate "first encounter" by wiping the sync rows AND clearing memberships from the target.
      // the target group itself stays, mimicking a pre-existing target group Grouper has never seen.
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      new GcDbAccess().connectionName("grouper").sql("delete from grouper_sync_membership where grouper_sync_id = ?").addBindVar(gcGrouperSync.getId()).executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from grouper_sync_group where grouper_sync_id = ?").addBindVar(gcGrouperSync.getId()).executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from grouper_sync_member where grouper_sync_id = ?").addBindVar(gcGrouperSync.getId()).executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();

      // flip the new flag on
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.skipIfTargetGroupExists").value("true").store();

      // sanity: target group still exists, sync rows are wiped, target memberships are wiped
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_membership").select(int.class));

      // phase 3: run full sync again. Grouper should match by email, detect that this is a first encounter
      // (no gcGrouperSyncGroup with inTarget=true), and SKIP the group entirely.
      grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      // target group is still there, untouched
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      GrouperGoogleGroup targetGroupAfter = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      assertEquals(preExistingTargetGroupId, targetGroupAfter.getId());
      assertEquals(preExistingTargetGroupEmail, targetGroupAfter.getEmail());

      // no memberships were synced into the target (skip blocked membership pipeline)
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());

      // no inserts/updates/deletes attributed to this run
      assertEquals(0, grouperProvisioningOutput.getInsert());
      assertEquals(0, grouperProvisioningOutput.getUpdate());
      assertEquals(0, grouperProvisioningOutput.getDelete());

      // debug map records the skip count
      assertEquals(1, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("skipPreExistingTargetGroups"), 0));

      // grouper_sync_group row: SKP error code set, inTarget not claimed
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertNotNull("expected sync group row to exist for skipped group", gcGrouperSyncGroup);
      assertEquals("expected SKP error code for skipped group", GcGrouperSyncErrorCode.SKP, gcGrouperSyncGroup.getErrorCode());
      assertFalse("expected inTarget not claimed for skipped group", Boolean.TRUE.equals(gcGrouperSyncGroup.getInTarget()));

      // phase 4: flip the flag off and run again - Grouper should now take over the pre-existing group
      // (sanity-check that the skip is conditional on the flag).
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.skipIfTargetGroupExists").value("false").store();

      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      // still only one target group
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());

      // memberships are now synced
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());

      // sync row now claims the group
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertNotNull(gcGrouperSyncGroup);
      assertTrue(Boolean.TRUE.equals(gcGrouperSyncGroup.getInTarget()));
      assertNull(gcGrouperSyncGroup.getErrorCode());

    } finally {

    }

  }


  public void atestFullSyncGoogleReal() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput().addExtraConfig("selectAllGroups", "false"));
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
  
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
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
   * provision populates grouper_prov_user / _group / _mship from the Google read path.
   * Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart.
   */
  public void testGoogleFullSyncPopulatesGenericTables() throws IOException {

    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    String configId = "myGoogleProvisioner";
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    // ensure mock Google tables exist
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

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

    // first pass writes the Google target; sync-back tables stay empty until the next
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
   * Sync-back smoke test for the scoped-retrieve path: same flow as the selectAll variant,
   * but with {@code selectAllGroups=false} and {@code selectAllEntities=false} so the DAO
   * uses the scoped {@code retrieveGroup} / {@code retrieveEntity} (per-id lookups)
   * instead of {@code retrieveAllGroups} / {@code retrieveAllEntities}. Confirms the
   * capture hooks on the scoped retrieve methods fire.
   *
   * <p>Incremental test coverage is intentionally deferred — the framework today only captures
   * from reads, and writes converge on the next read pass. Closing that gap is the
   * write-shadow precision pass tracked in section 10 of the sync-back doc.
   */
  public void testGoogleFullSyncSelectByIdsPopulatesGenericTables() throws IOException {

    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    String configId = "myGoogleProvisioner";
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    // ensure mock Google tables exist
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

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

  // =========================================================================================
  // Sync-back CRUD parity tests for the GOOGLE provisioner (capability-gated).
  //
  // Replicates the Box pilot (boxProvisioner/GrouperBoxProvisionerTest) for Google. Sync-back =
  // a provisioning run captures the target's state into grouper_prov_group / grouper_prov_user /
  // grouper_prov_mship (+ _attr / _attr_value), driven by the three load*ToGenericGrouperTable
  // flags. These tests cover only operations Google supports.
  //
  // CAPABILITY MATRIX (from GrouperGoogleTargetDao.registerGrouperProvisionerDaoCapabilities):
  //   canInsertGroup        = true   -> group insert-converge test
  //   canUpdateGroup        = true   -> group update-converge test (mutate NON-matching attr)
  //   canDeleteGroup        = true   -> group delete-converge test
  //   canInsertEntity       = true   -> covered by membership-add (SUBJ insert) + orphan capture
  //   canUpdateEntity       = true   -> NO standalone user-update-converge test; see SKIP note below
  //   canDeleteEntity       = true   -> covered by group-delete cascade (orphaned user dropped)
  //   canInsertMembership   = true   -> membership-add-converge test
  //   canDeleteMembership   = true   -> membership-remove-converge test
  //   (replaceMembership)   = N/A    -> Google DAO never calls setCanReplaceMembership(...), so the
  //                                     framework has no replace-membership capability for Google.
  //                                     SKIPPED: there is no replace-membership operation to exercise.
  //
  // GROUP MATCHING ATTRIBUTE = "name" (groupMatchingAttribute0name=name in
  //   GoogleProvisionerTestUtils.configureGoogleProvisioner; the "name" target attribute is the FULL
  //   Grouper group system name, e.g. "test:testGroup"). So, exactly like Box, the group
  //   update-converge test mutates the group's DESCRIPTION (a NON-matching attribute) -- the
  //   rename-as-update hazard (the Adobe lesson) does not apply. A group RENAME (extension change)
  //   would mutate the match key and cannot converge as an in-place update, so it is NOT tested.
  //
  // ENTITY MATCHING ATTRIBUTE = "email" (entityMatchingAttribute0name=email). The only other
  //   Grouper-driven user attributes here are givenName/familyName (both translate from the subject
  //   "name" field, so they are not independently mutable from Grouper) and email (the match key).
  //   There is no safe Grouper-driven NON-matching, independently-mutable user attribute to mutate,
  //   so a standalone user-update-converge test would be mutating the match key (the Adobe lesson)
  //   and could not converge as an in-place update. SKIPPED, exactly as the Box pilot skips it.
  //
  // DEFAULT CAPTURE ATTRIBUTES (asserted on -- from GrouperGoogleProvisioningTargetNativeSync):
  //   groups  -> name, email     (JSON pointers /name, /email on the merged Directory+settings node)
  //   users   -> primaryEmail, orgUnitPath (JSON pointers /primaryEmail, /orgUnitPath)
  //   "id" is NEVER captured as an attribute (it is already the target_group_id / target_user_id
  //   column). The group update test additionally captures "description" via nativeAttributesGroups;
  //   description IS reachable from the merged group JSON (the Directory read uses
  //   fields=...groups(id,email,name,description), and mergeGoogleGroupJsonForCapture overlays the
  //   Directory node), so the captured-value assertion is sound.
  //
  // MEMBERSHIP MODEL: group-centric, captured on the WRITE path (like Adobe/SCIM). GrouperGoogleTargetDao
  //   .insertMembership / deleteMembership call GrouperGoogleProvisioningTargetNativeSync
  //   .captureMembershipInsert/DeleteFromCurrentProvisioner -> recordTargetNativeMembershipInsert/Delete
  //   on success, so a membership add/remove is recorded into the native mirror on the write and
  //   converges on that same (write) pass -- no re-read pass is needed. The read path
  //   (retrieveMembershipsByGroup / retrieveAllData, which list a group's member ids via
  //   GrouperGoogleApiCommands.retrieveGoogleGroupMembers -- ALL members regardless of role -- and
  //   record them via captureMembershipsForGroupFromCurrentProvisioner) still re-confirms the mirror
  //   idempotently. Manager/owner ROLES are roles ON members and appear in that same member list, so
  //   for sync-back a membership is simply "(group, member)". The two-pass full membership tests
  //   assert convergence on pass A (the write pass); pass B is the idempotent re-read.
  // =========================================================================================

  /**
   * Shared setup for the Google sync-back tests: configure the provisioner with the three
   * load*ToGenericGrouperTable flags on (and recalculateAllOperations so every object/membership is
   * processed each run), then clean the Google mock target. The caller starts its own root session
   * and creates the Grouper-side stems/groups/members it needs. Mirrors the per-test boilerplate
   * that testGoogleFullSyncPopulatesGenericTables open-codes, and the Box pilot's setupBoxSyncBack.
   *
   * <p>Unlike Box (which defaults customize*Crud=false), GoogleProvisionerTestUtils already turns ON
   * customize*Crud and the delete keys (deleteGroups/deleteEntities/deleteMemberships with their
   * *IfGrouperDeleted qualifiers). So a Grouper-side delete cascades to the Google target out of the
   * box; callers do NOT need to add delete-type suffixes. Conversely, the *IfNotExistInGrouper keys
   * for groups/entities default to false, so a pure target-side orphan (unknown to Grouper) is NOT
   * deleted -- which is what the orphan-capture tests rely on.
   *
   * @param configId the provisioner config id (always "myGoogleProvisioner" here)
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes to set (may be null)
   */
  private void setupGoogleSyncBack(String configId, Map<String, String> extraConfig) throws IOException {

    GoogleProvisionerTestUtils.setupGoogleExternalSystem();

    GoogleProvisionerTestConfigInput configInput = new GoogleProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    GoogleProvisionerTestUtils.configureGoogleProvisioner(configInput);

    GrouperStartup.startup();

    // this read creates the mock Google tables (same idiom as the existing Google tests) before we wipe them
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
  }

  /**
   * the single provisioned group's target_group_id (Google group id) in the mirror, or null.
   * Mirrors the Box / Adobe helper of the same name -- used by the update-converge test to prove the
   * SAME target object survives an update (in-place update, not delete + re-create, which would
   * assign a new Google group id).
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
   * null. Reads through the {@code grouper_prov_group_attr_v} reporting view (not the raw
   * grouper_prov_group_attr_value table), because the value is stored via a dictionary FK and only
   * the view resolves it back to text (column {@code value_string}). {@code description} is captured
   * only because the update-converge test sets {@code nativeAttributesGroups=name,email,description}
   * -- it is NOT a Google default capture attribute (defaults are name/email), so without that
   * config this returns null.
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * canInsertGroup -> sync-back convergence of a newly created group, two-pass full (Google analogue
   * of Box's testBoxGroupInsertConvergesNextRead / SCIM's testGroupInsertConvergesSameRun). Because
   * createGroupsAndEntitiesBeforeTranslatingMemberships + selectGroups are on, pass 1 inserts the
   * group AND re-reads it (to link it) through the Google read path, and that read captures it -- so
   * the group is already in the mirror after pass 1 (assert 1, not 0). Pass 2 is idempotent.
   */
  public void testGoogleGroupInsertConvergesNextRead() throws IOException {

    String configId = "myGoogleProvisioner";
    setupGoogleSyncBack(configId, null);

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
    // the group converges into the mirror within this same run
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

    assertEquals("group insert should be present in prov_group", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // name captured from the Google Directory read response (a Google default capture attribute)
    int nameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("name should be captured from the Google read response, got " + nameValueRows,
        nameValueRows >= 1);
  }

  /**
   * canDeleteGroup (+ cascade canDeleteEntity / canDeleteMembership) -> sync-back convergence of an
   * object DELETE, two-pass full (Google analogue of Box's testBoxGroupDeleteConvergesNextRead).
   * Seed test:testGroup + SUBJ0 + their membership into the mirror, then delete the group in
   * Grouper. Google's test config already enables deleteGroups/deleteEntities/deleteMemberships with
   * their *IfGrouperDeleted qualifiers, so the next full sync removes them from the Google target
   * (pass A); the following re-read pass (pass B) sees them gone and the full-replace flush, scoped
   * to this provisioner's sync, drops the group, the now-orphaned user, and the membership from the
   * mirror.
   */
  public void testGoogleGroupDeleteConvergesNextRead() throws IOException {

    String configId = "myGoogleProvisioner";
    // Google defaults already turn ON customize*Crud + deleteGroups/deleteEntities/deleteMemberships
    // (with *IfGrouperDeleted), so a Grouper-side delete cascades to the target with NO extra config
    // -- this is the inverse of Box, which defaults customize*Crud=false and needs the delete keys
    // added explicitly. Nothing to add here.
    setupGoogleSyncBack(configId, null);

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

    // pass A: the delete writes hit the Google target (group + orphaned user + membership removed)
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
   * canUpdateGroup -> sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass
   * full (Google analogue of Box's testBoxGroupUpdateConvergesNextRead). Google groups are matched
   * by name, so the rename-as-update hazard (the Adobe lesson) does not apply: we mutate the group's
   * DESCRIPTION, which is mapped (targetGroupAttribute.2), round-trips through the mock's updateGroup
   * (the forward test testFullSyncGoogle proves the Google bean's description converges), and is NOT
   * the matching attribute. nativeAttributesGroups is set to "name,email,description" so the
   * description value is actually captured into the mirror (it is not a Google default capture
   * attribute) -- description is reachable because the Directory read includes it and
   * mergeGoogleGroupJsonForCapture overlays the Directory node.
   *
   * <p>Asserts both that the description VALUE converges to the new value AND that it is an in-place
   * update -- the SAME target group id survives (not delete + re-create, which would assign a new
   * Google id). Convergence is on the re-read pass (pass B), since Google captures on read.
   */
  public void testGoogleGroupUpdateConvergesNextRead() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    // capture description (not a Google default) so we can assert the updated value in the mirror
    extraConfig.put("nativeAttributesGroups", "name,email,description");
    setupGoogleSyncBack(configId, extraConfig);

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

    // change the description (a NON-matching attribute) -> Google updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the Google target (updateGroup persists it)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // update converges on the write pass: updateGroup marks the group for the drain re-read, which
    // re-reads it and captures the new description into the mirror on this same pass -- BEFORE the
    // bulk re-read (which is skipped under groups-from-cache). This is the regression guard.
    assertEquals("update converges on the write pass via the drain re-read (before the bulk re-read)",
        "newDescription", mirroredGroupDescription(configId));
    // pass B: the re-read captures the target's actual new description into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // mirror side: still ONE group, the SAME group (same target id) -- in-place update, not
    // delete + re-create -- and its description converged to the new value.
    assertEquals("group still in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("mirror tracks the same group through the update (update, not re-create)",
        groupTargetIdBefore, mirroredGroupTargetId(configId));
    assertEquals("mirror description should converge to the new value on the re-read pass",
        "newDescription", mirroredGroupDescription(configId));

    // NOTE: no standalone user-update-converge test for Google. Google users are matched by email,
    // and their only other Grouper-driven attributes (givenName/familyName) both translate from the
    // subject "name" field, so there is no safe Grouper-driven NON-matching, independently-mutable
    // user attribute to mutate. An update test would be mutating the match key (the Adobe lesson)
    // and could not converge as an in-place update. Skipped rather than written, exactly as Box does.
  }

  /**
   * canInsertMembership (+ canInsertEntity) -> sync-back convergence of a membership ADD to an
   * already-provisioned group, two-pass full (Google analogue of Box's
   * testBoxMembershipAddConvergesNextRead). Seed test:testGroup with SUBJ0, then add SUBJ1. Because
   * Google now captures memberships on the WRITE path (GrouperGoogleTargetDao.insertMembership ->
   * recordTargetNativeMembershipInsert, like Adobe/SCIM), the add shows in grouper_prov_mship on the
   * write pass: pass A issues the membership insert (and SUBJ1's user insert) to the Google target
   * and the insert hook records (testGroup, SUBJ1) into the native mirror on that same pass, so the
   * flush converges it without waiting for a re-read. Pass B re-reads the group's members and
   * re-converges idempotently.
   */
  public void testGoogleMembershipAddConvergesNextRead() throws IOException {

    String configId = "myGoogleProvisioner";
    setupGoogleSyncBack(configId, null);

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

    // pass A: the membership insert (and SUBJ1's user insert) hit the Google target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // capture-on-write: the insertMembership hook write-tracks (testGroup,SUBJ1) into the native
    // membership map on this same pass, so the flush converges the added membership WITHOUT waiting
    // for a re-read pass (the memberships-from-sync-back-cache / GRP-7048 contract). This is the
    // regression guard for the fixed capture-on-write gap.
    assertEquals("the added membership should converge on the write pass (capture-on-write)", 2,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read sees both members; the flush re-converges the added membership (idempotent)
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("the added membership should converge on the re-read pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * canDeleteMembership -> sync-back convergence of a membership REMOVE from a surviving group,
   * two-pass full (Google analogue of Box's testBoxMembershipRemoveConvergesNextRead). Two groups
   * both hold SUBJ0; SUBJ0 is removed from testGroup only (it survives in otherGroup, so its Google
   * user is NOT deleted). The full-replace flush, fed by the re-read of each group's members, drops
   * exactly testGroup's membership while leaving otherGroup's intact. (The forward test
   * testFullSyncGoogle already proves member-remove keeps the surviving user; here we assert the
   * mirror reflects it.)
   */
  public void testGoogleMembershipRemoveConvergesNextRead() throws IOException {

    String configId = "myGoogleProvisioner";
    // Google config already enables deleteMemberships + deleteMembershipsIfNotExistInGrouper, so no
    // extra delete-type config is needed (inverse of Box, which must add them).
    setupGoogleSyncBack(configId, null);

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

    // pass A: the membership-remove write hits the Google target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // capture-on-write: the deleteMembership hook drops (testGroup,SUBJ0) from the native membership
    // map on this same pass, so the flush removes it WITHOUT waiting for a re-read pass. otherGroup's
    // SUBJ0 membership is untouched. This is the regression guard for the fixed capture-on-write gap.
    assertEquals("the removed membership should be dropped on the write pass (capture-on-write)", 1,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush
    // re-confirms (testGroup,SUBJ0) is gone while otherGroup's SUBJ0 membership survives (idempotent)
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Strict-native capture of orphan target objects (Google analogue of Box's
   * testBoxFullSyncCapturesOrphanTargetEntities). With the *IfNotExistInGrouper delete keys at their
   * config default (false), an orphan group + orphan user that exist in the Google target but are
   * unknown to Grouper are still captured into the mirror -- with NULL Grouper-side linkage
   * (group_internal_id / member_internal_id) -- alongside Grouper's own testGroup + SUBJ0/SUBJ1,
   * which keep their linkage populated. The orphans are seeded directly into the mock Google tables
   * (the GrouperGoogleGroup / GrouperGoogleUser beans double as the mock-table entities).
   */
  public void testGoogleFullSyncCapturesOrphanTargetEntities() throws IOException {

    String configId = "myGoogleProvisioner";
    setupGoogleSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the Google mock before the provisioner runs. name + email
    // are Google group defaults; primaryEmail is a Google user default -- set them so the capture
    // has values to record.
    GrouperGoogleGroup orphanGroup = new GrouperGoogleGroup();
    orphanGroup.setId("orphan-google-group-1234");
    orphanGroup.setName("test:orphanGroupNotInGrouper");
    orphanGroup.setEmail("orphan.group@viveksachdeva.com");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperGoogleUser orphanUser = new GrouperGoogleUser();
    orphanUser.setId("orphan-google-user-5678");
    orphanUser.setPrimaryEmail("orphan.user@viveksachdeva.com");
    orphanUser.setOrgUnitPath("/orphans");
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

    // two passes: pass 1 inserts Grouper's objects (orphans untouched, *IfNotExistInGrouper=false);
    // pass 2 reads orphans + Grouper's objects and the flush captures all of them.
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

    // a Google default group attribute (email) is captured in the catalog
    int emailCatalog = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'email'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("default group attribute 'email' should be in the per-provisioner catalog", 1,
        emailCatalog);

    // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis (Google analogue of Box's
   * testBoxFullSyncCapturesMembershipsFromOrphanGroup). An orphan group with an orphan member
   * (neither known to Grouper) is wired in the Google mock (mock_google_membership). Google
   * memberships are group-centric, so when the daemon lists groups it also reads the orphan group's
   * members (retrieveAllData -> retrieveGoogleGroupMembers) -- that membership must land in
   * grouper_prov_mship alongside Grouper's own, proving strict-native membership capture is
   * independent of Grouper knowledge.
   */
  public void testGoogleFullSyncCapturesMembershipsFromOrphanGroup() throws IOException {

    String configId = "myGoogleProvisioner";
    setupGoogleSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the Google mock.
    GrouperGoogleGroup orphanGroup = new GrouperGoogleGroup();
    orphanGroup.setId("orphan-google-mship-group-1");
    orphanGroup.setName("test:orphanGroupWithMembers");
    orphanGroup.setEmail("orphan.mship.group@viveksachdeva.com");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperGoogleUser orphanUser = new GrouperGoogleUser();
    orphanUser.setId("orphan-google-mship-user-1");
    orphanUser.setPrimaryEmail("orphan.mship.user@viveksachdeva.com");
    orphanUser.setOrgUnitPath("/orphans");
    HibernateSession.byObjectStatic().save(orphanUser);

    // a plain (MEMBER-role) membership; sync-back lists all members regardless of role
    GrouperGoogleMembership orphanMembership = new GrouperGoogleMembership();
    orphanMembership.setId("orphan-google-mship-row-1");
    orphanMembership.setGroupId(orphanGroup.getId());
    orphanMembership.setUserId(orphanUser.getId());
    orphanMembership.setRole("MEMBER");
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
   * !selectAll* scope excludes orphans (Google analogue of Box's testBoxSelectAllFalseExcludesOrphans).
   * With selectAllGroups=false and selectAllEntities=false the daemon fetches only the resources
   * mapped to Grouper-provisioned objects (by id/email), never a domain-wide listing -- so an orphan
   * group/user that the Google target has but Grouper does not must NOT land in the mirror.
   */
  public void testGoogleSelectAllFalseExcludesOrphans() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    setupGoogleSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id/email (Grouper-known resources only).
    GrouperGoogleGroup orphanGroup = new GrouperGoogleGroup();
    orphanGroup.setId("orphan-google-group-selnone-1");
    orphanGroup.setName("test:orphanGroupSelectAllFalse");
    orphanGroup.setEmail("orphan.selnone.group@viveksachdeva.com");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperGoogleUser orphanUser = new GrouperGoogleUser();
    orphanUser.setId("orphan-google-user-selnone-1");
    orphanUser.setPrimaryEmail("orphan.selnone.user@viveksachdeva.com");
    orphanUser.setOrgUnitPath("/orphans");
    HibernateSession.byObjectStatic().save(orphanUser);

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

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // the orphan group is NOT captured (scoped retrieve never fetched it)
    int orphanGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group must NOT be in the mirror under selectAllGroups=false", 0, orphanGroupRows);

    // the orphan user is NOT captured either
    int orphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user must NOT be in the mirror under selectAllEntities=false", 0, orphanUserRows);

    // Grouper's own testGroup + SUBJ0 ARE captured (they are fetched by id/email)
    assertTrue("Grouper's own group should still be captured", countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("Grouper's own user should still be captured", countSyncBack(configId, "grouper_prov_user") >= 1);
  }

  /**
   * loadGroupsToGenericGrouperTable in isolation (Google analogue of Box's
   * testBoxLoadGroupsFlagInIsolation). Only the groups flag is on -> only grouper_prov_group rows
   * are written; prov_user and prov_mship stay empty even though the daemon still reads users (for
   * provisioning) and memberships (for diffing).
   */
  public void testGoogleLoadGroupsFlagInIsolation() throws IOException {

    String configId = "myGoogleProvisioner";
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

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
   * loadEntitiesToGenericGrouperTable in isolation (Google analogue of Box's
   * testBoxLoadEntitiesFlagInIsolation). Only the entities flag is on -> only grouper_prov_user rows
   * are written; prov_group and prov_mship stay empty.
   */
  public void testGoogleLoadEntitiesFlagInIsolation() throws IOException {

    String configId = "myGoogleProvisioner";
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

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
   * loadMembershipsToGenericGrouperTable off (Google analogue of Box's testBoxLoadMembershipsFlagOff).
   * Both object loads on but memberships off -> prov_group and prov_user populate, prov_mship stays
   * empty. Proves the membership gate is independent of the object gates.
   */
  public void testGoogleLoadMembershipsFlagOff() throws IOException {

    String configId = "myGoogleProvisioner";
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle", null, null, false, false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();

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
   * INCREMENTAL sync-back coverage for Google, conservative (Google analogue of Box's
   * testBoxIncrementalSyncBackNoSpuriousDeletes). Google GROUP and USER objects still capture on the
   * READ path; memberships now capture on the WRITE path (GrouperGoogleTargetDao.insertMembership /
   * deleteMembership -> recordTargetNativeMembershipInsert/Delete). An incremental cycle re-reads only
   * the changed objects (it has canRetrieveGroup/Entity, so the adapter decomposes to per-id reads
   * that fire the Google object capture seams), and the incremental flush is a SCOPED upsert (it does
   * NOT full-replace, so it will not wrongly delete untouched mirror rows).
   *
   * <p>What this test asserts is deliberately narrow -- the safe, reliable part of Google incremental
   * sync-back: after seeding via full sync and priming the changelog consumer, adding a member drives
   * an incremental that (a) re-reads the changed group/entity and so does NOT shrink the existing
   * mirror (no spurious deletes -- the regression the scoped incremental flush guards against), and
   * (b) captures the newly added member's user object into prov_user. It does not additionally assert
   * membership-row convergence on this incremental cycle; membership convergence via the write-path
   * capture hooks is covered end-to-end by the two-pass full tests above.
   */
  public void testGoogleIncrementalSyncBackNoSpuriousDeletes() throws IOException {

    String configId = "myGoogleProvisioner";
    setupGoogleSyncBack(configId, null);

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

    // prime the changelog consumer: its FIRST run only initializes its changelog position
    // (processes nothing), so without this priming pass the change below is never consumed.
    incrementalProvision();

    // incremental add: a third member. The incremental re-reads the changed group/entity, firing
    // the Google object read-capture seams, and the scoped flush upserts -- it must NOT drop untouched rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc). Membership
    // write-path capture (GrouperGoogleTargetDao.insertMembership/deleteMembership ->
    // recordTargetNativeMembershipInsert/Delete) plus the scoped incremental membership flush for the
    // changed group are exercised, but this narrow test does not assert the resulting prov_mship row
    // count. Membership convergence via the write-path capture hooks is covered end-to-end by the
    // two-pass full tests above; here we only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; Google shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

  // ==========================================================================================
  // GRP-7048 full-sync-from-sync-back-cache tests for GOOGLE (mirrors the Okta set in
  // GrouperOktaProvisionerTest and is collected by FullSyncFromSyncBackSuite).
  //
  // Google is group-centric (canRetrieveMembershipsAllByGroup) and declares canRetrieveAllData, so
  // it takes the COMBINED retrieveAllData path: GrouperGoogleTargetDao.retrieveAllData honors the
  // per-axis retrieve*=false flags, serving that axis from the sync-back mirror
  // (grouper_prov_group/user/mship) instead of pulling from Google. That per-axis guarding in
  // retrieveAllData is exactly the production change these tests cover.
  //
  // NB: groups-from-cache REQUIRES memberships-from-cache for a group-centric target (the framework
  // couples the two, and config validation enforces it), so retrieveAllData is never asked to
  // iterate members over a skipped/empty group list -- the one unsafe axis combination cannot occur.
  //
  // Like the other Google sync-back tests these need the mock-services Tomcat; they run through the
  // same setupGoogleSyncBack + fullProvision() harness (no tomcatRunTests() gate is used elsewhere
  // in this class, so none is used here either).
  // ==========================================================================================

  /**
   * GRP-7048 (users, warm cache): once the cache is warm, both target users are reconstructed from
   * grouper_prov_user, the bulk Google user pull is skipped, and NO individual user re-reads happen
   * -- proving the feature both skips the big pull and does not fall back to per-user lookups.
   */
  public void testGoogleFullSyncUsersFromSyncBackWarmCache() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    // capture the entity MATCHING attribute (email) into the shadow, mapped from the Google user's
    // /primaryEmail. A normal read maps primaryEmail -> the "email" target attribute (the match key),
    // but the default shadow captures only the raw primaryEmail/orgUnitPath, so a cache-reconstructed
    // entity would carry no "email" and fail to match its Grouper entity -- forcing an individual
    // re-read of every member. Capturing email:/primaryEmail lets reconstruction match.
    extraConfig.put("nativeAttributesEntities",
        "[{\"name\":\"primaryEmail\"},{\"name\":\"orgUnitPath\"},{\"name\":\"email\",\"path\":\"/primaryEmail\"}]");
    setupGoogleSyncBack(configId, extraConfig);

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

    // warm the cache: Google captures groups/users on read, so a few passes converge the inserts +
    // the post-write read-capture into grouper_prov_user
    fullProvision();
    fullProvision();
    fullProvision();
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));

    // the warm run: users come entirely from the cache
    fullProvision();
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("bulk user pull from Google should be skipped (users come from the sync-back cache)",
        debugMap.get("googleRetrieveAllUsersApiCall"));
    assertEquals("both users reconstructed from the cache", 2,
        debugMapInt(debugMap, "syncBackEntitiesReconstructed"));
    assertEquals("no individual user re-reads when the cache is warm and complete", 0,
        debugMapInt(debugMap, "missingEntitiesForRetrieve"));
  }

  /**
   * GRP-7048 (user missing from cache is re-read): a user in the target but not in the sync-back
   * cache is re-read individually from Google, while the cached users are served from the cache and
   * the bulk pull stays skipped. We simulate the cache miss by deleting one user's grouper_prov_user
   * row (and its FK children) after warm-up; it stays in Google.
   */
  public void testGoogleFullSyncUsersFromSyncBackMissingFromCacheReRead() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncUsersFromSyncBack", "true");
    // capture the entity matching attribute (email) into the shadow so reconstructed entities match
    // -- see testGoogleFullSyncUsersFromSyncBackWarmCache for why this is required for Google
    extraConfig.put("nativeAttributesEntities",
        "[{\"name\":\"primaryEmail\"},{\"name\":\"orgUnitPath\"},{\"name\":\"email\",\"path\":\"/primaryEmail\"}]");
    setupGoogleSyncBack(configId, extraConfig);

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
    fullProvision();
    fullProvision();
    fullProvision();
    assertEquals("both users cached after warm-up", 2, countSyncBack(configId, "grouper_prov_user"));

    // simulate a cache miss for SUBJ0: delete its grouper_prov_user row (+ FK children) while it
    // stays in Google. grouper_prov_user.member_internal_id -> grouper_members.internal_id.
    String provUserForSubj0 = "select internal_id from grouper_prov_user "
        + "where grouper_sync_internal_id = (select internal_id from grouper_sync where provisioner_name = ?) "
        + "and member_internal_id = (select internal_id from grouper_members where subject_id = ?)";
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

    // the run: SUBJ1 from cache, SUBJ0 (missing from cache) re-read individually from Google
    fullProvision();
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("bulk user pull from Google should still be skipped (cache is non-empty)",
        debugMap.get("googleRetrieveAllUsersApiCall"));
    assertEquals("only the cached user is reconstructed from the cache", 1,
        debugMapInt(debugMap, "syncBackEntitiesReconstructed"));
    assertEquals("exactly the user missing from the cache is re-read individually", 1,
        debugMapInt(debugMap, "missingEntitiesForRetrieve"));
  }

  /**
   * GRP-7048 (memberships, warm cache): once the membership cache is warm, the target memberships
   * are reconstructed from grouper_prov_mship and Google's per-group member iteration is skipped
   * (googleRetrieveMembershipsApiCall stays unset on the warm run).
   */
  public void testGoogleFullSyncMembershipsFromSyncBackWarmCache() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    setupGoogleSyncBack(configId, extraConfig);

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

    // warm the cache: two memberships in the mirror (the cold run does the per-group iteration)
    fullProvision();
    fullProvision();
    fullProvision();
    assertEquals("both memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // the warm run: memberships reconstructed from the cache, per-group iteration skipped
    fullProvision();
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("Google per-group member iteration should be skipped (memberships come from the cache)",
        debugMap.get("googleRetrieveMembershipsApiCall"));
    assertEquals("both memberships reconstructed from the cache", 2,
        debugMapInt(debugMap, "syncBackMembershipsReconstructed"));
  }

  /**
   * GRP-7048 (memberships, cache used as the target set): with memberships served from the cache,
   * the normal compare still provisions a membership that exists in Grouper but not the cache, and
   * removes one that exists in the cache but not Grouper -- proving it is not just the idempotent
   * case. Google captures memberships on the WRITE path, so each change converges into the mirror on
   * the same (write) pass, and the per-group iteration stays skipped throughout.
   */
  public void testGoogleFullSyncMembershipsFromSyncBackAddAndRemove() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    // Google's setup already turns on customize*Crud + deleteMemberships (with *IfGrouperDeleted),
    // so a Grouper-side membership removal deprovisions from the target with no extra config.
    setupGoogleSyncBack(configId, extraConfig);

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

    // warm the cache: two memberships
    fullProvision();
    fullProvision();
    fullProvision();
    assertEquals("two memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // ADD: SUBJ2 is in Grouper but not the membership cache -> the compare inserts it into Google,
    // and the write-path capture records it into the mirror on this same pass
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    fullProvision();
    Map<String, Object> addDebugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertEquals("added membership captured into the mirror on the write pass", 3,
        countSyncBack(configId, "grouper_prov_mship"));
    assertNull("per-group member iteration stays skipped on the add pass (memberships from the cache)",
        addDebugMap.get("googleRetrieveMembershipsApiCall"));

    // REMOVE: SUBJ1 leaves Grouper but is in the cache -> the compare deletes it from Google, and
    // the write-path delete-capture drops it from the mirror on this same pass
    testGroup.deleteMember(SubjectTestHelper.SUBJ1, false);
    fullProvision();
    Map<String, Object> removeDebugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
    assertEquals("removed membership dropped from the mirror on the write pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
    assertNull("per-group member iteration stays skipped on the remove pass",
        removeDebugMap.get("googleRetrieveMembershipsApiCall"));
  }

  /**
   * GRP-7048 (groups, warm cache): once the cache is warm, the group is reconstructed from
   * grouper_prov_group and the bulk Google group pull is skipped. Groups-from-cache requires
   * memberships-from-cache for a group-centric target, so both options are enabled and the per-group
   * member iteration is skipped too (both-or-neither).
   */
  public void testGoogleFullSyncGroupsFromSyncBackWarmCache() throws IOException {

    String configId = "myGoogleProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("fullSyncGroupsFromSyncBack", "true");
    extraConfig.put("fullSyncMembershipsFromSyncBack", "true");
    setupGoogleSyncBack(configId, extraConfig);

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

    // warm the cache: the group + its two memberships (the cold run pulls them normally)
    fullProvision();
    fullProvision();
    fullProvision();
    assertEquals("group cached after warm-up", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("two memberships cached after warm-up", 2, countSyncBack(configId, "grouper_prov_mship"));

    // the warm run: group (and memberships) come from the cache; both target pulls are skipped
    fullProvision();
    Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();

    assertNull("bulk group pull from Google should be skipped (groups come from the cache)",
        debugMap.get("googleRetrieveAllGroupsApiCall"));
    assertNull("per-group member iteration should be skipped too (both-or-neither)",
        debugMap.get("googleRetrieveMembershipsApiCall"));
    assertEquals("the group is reconstructed from the cache", 1,
        debugMapInt(debugMap, "syncBackGroupsReconstructed"));
  }

  /** read an int counter from the provisioner debug map, treating absent as 0 */
  private static int debugMapInt(Map<String, Object> debugMap, String key) {
    Object value = debugMap == null ? null : debugMap.get(key);
    return value == null ? 0 : ((Number) value).intValue();
  }

}
