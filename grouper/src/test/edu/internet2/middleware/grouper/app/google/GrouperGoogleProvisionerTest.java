package edu.internet2.middleware.grouper.app.google;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;


public class GrouperGoogleProvisionerTest extends GrouperProvisioningBaseTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    TestRunner.run(new GrouperGoogleProvisionerTest("testFullSyncGoogle"));
    
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
  
  public void testIncrementalSyncGoogle() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput());
  
    GrouperStartup.startup();
    
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
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
  
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      
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
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
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

  public void testFullSyncGoogle() throws IOException {
    
    GoogleProvisionerTestUtils.setupGoogleExternalSystem();
    GoogleProvisionerTestUtils.configureGoogleProvisioner(new GoogleProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle");
  
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
//      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      assertTrue(1 <= grouperProvisioningOutput.getInsert());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
//      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
//      
//      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
//        assertTrue(provisioningGroupWrapper.isRecalcObject());
//        
//        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
//      }
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
//      
//      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
//        assertTrue(provisioningEntityWrapper.isRecalcObject());
//        
//        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
//        
//      }
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
//      
//      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
//        assertTrue(provisioningMembershipWrapper.isRecalcObject());
//      }
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
//      
//      
//      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      GrouperUtil.sleep(10000);
//      
//      // now run the full sync again and the member should be deleted from mock_google_membership also
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
//      
//      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
//      
      GrouperUtil.sleep(10000);
//      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
//      
//      
//      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
//      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      GrouperUtil.sleep(10000);
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      
//      GrouperGoogleGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
//      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
//      assertEquals("ALL_MEMBERS_CAN_VIEW", groupWithUpdatedDescription.getWhoCanViewGroup());
//      
//      //now delete the group and sync again
      testGroup.delete();
      GrouperUtil.sleep(10000);
//      
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
    } finally {
      
    }

  }

}
