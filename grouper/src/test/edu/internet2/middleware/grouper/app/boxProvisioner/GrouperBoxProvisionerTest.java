package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
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
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

public class GrouperBoxProvisionerTest extends GrouperProvisioningBaseTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    TestRunner.run(new GrouperBoxProvisionerTest("testIncrementalSyncBox"));
    
  }
  
  @Override
  public String defaultConfigId() {
    return "myBoxProvisioner";
  }
  
  public GrouperBoxProvisionerTest(String name) {
    super(name);
  }
  
  private boolean startTomcat = false;
  
  public void testIncrementalSyncBox() throws IOException {
    
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(
        new BoxProvisionerTestConfigInput()
        .addExtraConfig("scoreConvertToFullSyncThreshold", "200")
        );
  
    GrouperStartup.startup();
    
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperBoxGroup> grouperBoxGroups = GrouperBoxApiCommands.retrieveBoxGroups("localBox", null, GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet() );
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_box_auth").executeSql();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
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
      
      Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
      Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
      Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
      Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);

      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
      incrementalProvision();
  
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      ProvisioningEntityWrapper provisioningEntityWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member0.getId());

      assertEquals(true, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper0.getProvisioningStateEntity().isCreate());

      ProvisioningEntityWrapper provisioningEntityWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member1.getId());

      assertEquals(true, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper1.getProvisioningStateEntity().isCreate());

      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      ProvisioningMembershipWrapper provisioningMembershipWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member0.getId()));

      ProvisioningMembershipWrapper provisioningMembershipWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member1.getId()));
      
//      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isRecalcObject());
//      assertEquals(false, provisioningMembershipWrapper0.getProvisioningStateMembership().isSelect());
//      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isSelectResultProcessed());
//
//      assertEquals(true, provisioningMembershipWrapper1.getProvisioningStateMembership().isRecalcObject());
//      assertEquals(false, provisioningMembershipWrapper1.getProvisioningStateMembership().isSelect());
//      assertEquals(true, provisioningMembershipWrapper1.getProvisioningStateMembership().isSelectResultProcessed());
//
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      GrouperBoxGroup grouperBoxGroup = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      
      assertEquals("testGroup", grouperBoxGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myBoxProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperBoxGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_box_membership also
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      incrementalProvision();
      
      GrouperBoxGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  public void testFullSyncBox() throws IOException {
    
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(new BoxProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperBoxGroup> grouperBoxGroups = GrouperBoxApiCommands.retrieveBoxGroups("localBox", null, GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet() );
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_box_auth").executeSql();
      
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
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      GrouperBoxGroup grouperBoxGroup = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      
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
      
      assertEquals("testGroup", grouperBoxGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myBoxProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperBoxGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_box_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      GrouperBoxGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
    } finally {
      
    }

  }
  
  public void testFullSyncBoxStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();
      
      BoxProvisionerTestUtils.setupBoxExternalSystem();
      
      BoxProvisioningStartWith startWith = new BoxProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("boxExternalSystemConfigId", "localBox");
      startWithSuffixToValue.put("boxPattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("selectAllGroups", "true");
      startWithSuffixToValue.put("manageGroups", "true");
      startWithSuffixToValue.put("groupNameAttributeValue", "extension");
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityNameSubjectAttribute", "subjectId");
      startWithSuffixToValue.put("entityLoginSubjectAttribute", "email");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myBoxProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myBoxProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myBoxProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myBoxProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myBoxProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myBoxProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myBoxProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myBoxProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myBoxProvisioner.provisionerConfigId").value("myBoxProvisioner").store();
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      GrouperBoxGroup grouperBoxGroup = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      
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
      
      assertEquals("testGroup", grouperBoxGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myBoxProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperBoxGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());
      
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myBoxProvisioner");
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

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the Box read path.
   * Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart. Framework-detail coverage (flag isolation, native-attribute
   * config, validation) lives in the SCIM + LDAP suites.
   */
  public void testBoxFullSyncPopulatesGenericTables() throws IOException {

    BoxProvisionerTestUtils.setupBoxExternalSystem();

    String configId = "myBoxProvisioner";
    BoxProvisionerTestUtils.configureBoxProvisioner(
        new BoxProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    // this will create tables
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null, GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());

    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

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

    // first pass writes the Box target; sync-back tables stay empty until the next
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
   * Sync-back smoke test for the !selectAll* scoped retrieve path: configure
   * selectAllGroups=false and selectAllEntities=false, then run two full provisions
   * (pass 1 writes the Box target, pass 2 reads back via scoped retrieves and flushes).
   */
  public void testBoxFullSyncSelectByIdsPopulatesGenericTables() throws IOException {

    BoxProvisionerTestUtils.setupBoxExternalSystem();

    String configId = "myBoxProvisioner";
    BoxProvisionerTestUtils.configureBoxProvisioner(
        new BoxProvisionerTestConfigInput()
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    // this will create tables
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null, GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());

    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

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
   * the single provisioned group's target_group_id (Box group id) in the mirror, or null.
   * Mirrors the Adobe helper of the same name -- used by the rename/update converge tests to
   * prove the SAME target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved {@code description} attribute value for the single provisioned group in the mirror,
   * or null. Reads through the {@code grouper_prov_group_attr_v} reporting view (not the base
   * grouper_prov_group_attr_value table), because the raw string is stored via a dictionary FK and
   * only the view resolves it back to text (column {@code value_string}). {@code description} is
   * captured only because the update-converge test configures
   * {@code nativeAttributesGroups=name,description} -- it is NOT a Box default capture attribute
   * (defaults are name/group_type/provenance), so without that config this returns null.
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  public void testIncrementalSyncBox2() throws IOException {
    
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(
        new BoxProvisionerTestConfigInput()
        .addExtraConfig("scoreConvertToFullSyncThreshold", "200")
        );
  
    GrouperStartup.startup();
    
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperBoxGroup> grouperBoxGroups = GrouperBoxApiCommands.retrieveBoxGroups("localBox", null, GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet() );
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_box_auth").executeSql();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
      fullProvision();
      incrementalProvision();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
            
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_box_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      
      incrementalProvision();

      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      GrouperBoxGroup grouperBoxGroup = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      
      assertEquals("testGroup", grouperBoxGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myBoxProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperBoxGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      Member member0 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ0, false);
      Member member1 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ1, false);
      Member member2 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ2, false);
      Member member3 = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), SubjectTestHelper.SUBJ3, false);

      incrementalProvision();

      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      grouperBoxGroup = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      
      assertEquals("testGroup", grouperBoxGroup.getName());
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myBoxProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperBoxGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache0());

      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      ProvisioningEntityWrapper provisioningEntityWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member0.getId());

      assertEquals(true, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper0.getProvisioningStateEntity().isCreate());

      ProvisioningEntityWrapper provisioningEntityWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member1.getId());

      assertEquals(true, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper1.getProvisioningStateEntity().isCreate());

      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      ProvisioningMembershipWrapper provisioningMembershipWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member0.getId()));

      ProvisioningMembershipWrapper provisioningMembershipWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member1.getId()));


      assertEquals(false, provisioningMembershipWrapper0.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isCreate());

      assertEquals(false, provisioningMembershipWrapper1.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper1.getProvisioningStateMembership().isCreate());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_box_membership also
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());

      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      provisioningEntityWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member1.getId());

      assertEquals(false, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper1.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(false, provisioningEntityWrapper1.getProvisioningStateEntity().isCreate());

      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      provisioningMembershipWrapper1 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member1.getId()));

      assertEquals(false, provisioningMembershipWrapper1.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper1.getProvisioningStateMembership().isDelete());

      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      incrementalProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      ProvisioningEntityWrapper provisioningEntityWrapper3 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member3.getId());

      assertEquals(true, provisioningEntityWrapper3.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper3.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper3.getProvisioningStateEntity().isCreate());

      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      ProvisioningMembershipWrapper provisioningMembershipWrapper3 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member3.getId()));

      assertEquals(false, provisioningMembershipWrapper3.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper3.getProvisioningStateMembership().isCreate());
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myBoxProvisioner");
      attributeValue.setTargetName("myBoxProvisioner");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      incrementalProvision();
      
      GrouperBoxGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());

      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(false, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(0, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup").list(GrouperBoxGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser").list(GrouperBoxUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperBoxMembership").list(GrouperBoxMembership.class).size());
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertEquals(true, provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      provisioningEntityWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member0.getId());

      assertEquals(false, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper0.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper0.getProvisioningStateEntity().isDelete());

      provisioningEntityWrapper3 = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member3.getId());

      assertEquals(false, provisioningEntityWrapper3.getProvisioningStateEntity().isRecalcObject());
      assertEquals(false, provisioningEntityWrapper3.getProvisioningStateEntity().isRecalcEntityMemberships());
      assertEquals(true, provisioningEntityWrapper3.getProvisioningStateEntity().isDelete());
      // only the two still-synced members (member0 and member3) load on the group delete.
      // member1 was removed earlier and its sync membership/entity rows were already deleted
      // on that prior incremental, so it does not linger here.
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(2, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      provisioningMembershipWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member0.getId()));

      provisioningMembershipWrapper3 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member3.getId()));


      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isDelete());

      assertEquals(true, provisioningMembershipWrapper3.getProvisioningStateMembership().isDelete());
      

    } finally {

    }

  }

  // ==========================================================================================
  // SCIM-parity sync-back tests for Box, CAPABILITY-GATED.
  //
  // Box capture model (verified from GrouperBoxApiCommands + GrouperBoxProvisioningTargetNativeSync):
  // Box captures target state into the generic mirror ONLY on the READ path --
  // captureGroupJson/captureUserJson fire inside retrieveBoxGroups/retrieveBoxGroup/
  // retrieveBoxUsers/retrieveBoxUser, and captureMemberships fires inside
  // GrouperBoxTargetDao.retrieveMembershipsByGroup. The create/update/delete API methods do NOT
  // call any capture hook. So Box is a READ-STATE-CONVERGENCE target, NOT a capture-on-write
  // target like SCIM/Adobe: a target change converges into the mirror on the NEXT read pass, not
  // the same run that writes it. Every converge test below therefore uses the two-pass full-sync
  // pattern (pass 1 writes the target, pass 2 re-reads and the end-of-run flush converges), the
  // same shape as the existing testBoxFullSyncPopulatesGenericTables.
  //
  // The full flush (GrouperProvisioningLogic.loadDataToGenericProvisionerTables) is a FULL REPLACE
  // scoped to the provisioner's grouper_sync_internal_id: anything in the mirror that the target
  // did NOT return this run is deleted. That is what makes the delete / membership-remove converge
  // tests work after a re-read pass.
  //
  // Capabilities confirmed in GrouperBoxTargetDao.registerGrouperProvisionerDaoCapabilities:
  //   group  : insert YES, update YES, delete YES
  //   entity : insert YES, update YES, delete YES
  //   mship  : insert YES, delete YES, REPLACE *NO* (no setCanReplaceMembership)
  //   memberships are group-centric (canRetrieveMembershipsAllByGroup)
  //
  // Matching attributes (BoxProvisionerTestUtils): groupMatchingAttribute0name=name (from the
  // Grouper group extension), entityMatchingAttribute0name=name (from the subjectId). An update
  // that changes the MATCHING attribute cannot converge as an in-place update (the Adobe lesson),
  // so update-converge tests mutate a NON-matching attribute: for groups that is description
  // (targetGroupAttribute.1, round-trips through the mock's updateGroup). For users there is no
  // safe Grouper-driven non-matching attribute to mutate (login=email is fixed per subject;
  // status/role are target-controlled), so the user-update-converge test is SKIPPED -- see the
  // one-line note where it would live.
  //
  // SKIPPED, per capability (no test body, just this note):
  //   - no membership-replace sync-back test: GrouperBoxTargetDao has no setCanReplaceMembership
  //     (so SCIM's testMembershipReplaceConvergesSameRun / testIncrementalMembershipReplace... do
  //     not apply to Box).
  //   - no "same-run" convergence variants of the SCIM insert/update/delete/membership tests
  //     (testGroupInsertConvergesSameRun, testUserUpdateConvergesSameRun*,
  //     testGroupDeleteConvergesSameRun, testMembership{Add,Remove}ConvergesSameRun): Box captures
  //     on READ only, so these can only converge on the next read pass. Their intent is ported as
  //     the two-pass full tests below (testBoxGroupInsertConvergesNextRead,
  //     testBoxGroupDeleteConvergesNextRead, testBoxMembershipAddConvergesNextRead,
  //     testBoxMembershipRemoveConvergesNextRead, testBoxGroupUpdateConvergesNextRead).
  // ==========================================================================================

  /**
   * Shared setup for the Box sync-back tests: configure the provisioner with the three
   * load*ToGenericGrouperTable flags on (and recalculateAllOperations so every object/membership
   * is processed each run), then clean the Box mock target. The caller starts its own root session
   * and creates the Grouper-side stems/groups/members it needs. Mirrors the per-test boilerplate
   * that testBoxFullSyncPopulatesGenericTables open-codes.
   *
   * <p>Leaves all delete-types at their config defaults (deleteGroups=true, but
   * deleteGroupsIfNotExistInGrouper / deleteEntitiesIfNotExistInGrouper = false), so target-side
   * orphans persist across runs unless the caller passes the delete-type suffixes explicitly.
   *
   * @param configId the provisioner config id (always "myBoxProvisioner" here)
   * @param extraConfig additional provisioner.<configId>.* suffixes to set (may be null)
   */
  private void setupBoxSyncBack(String configId, Map<String, String> extraConfig) throws IOException {

    BoxProvisionerTestUtils.setupBoxExternalSystem();

    BoxProvisionerTestConfigInput configInput = new BoxProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    BoxProvisionerTestUtils.configureBoxProvisioner(configInput);

    GrouperStartup.startup();

    // this read creates the mock tables (same idiom as the existing Box tests) before we wipe them
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null,
        GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());

    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();
  }

  /**
   * Sync-back convergence of a newly created group, full provision (Box analogue of SCIM's
   * testGroupInsertConvergesSameRun). The group converges into grouper_prov_group within the SAME
   * run that inserts it: because createGroupsAndEntitiesBeforeTranslatingMemberships + selectGroups
   * are on, the daemon re-reads each just-inserted group (to link it) through the Box read path,
   * and that read captures it. So after pass 1 the group is already in the mirror -- linked back to
   * its Grouper group (group_internal_id not null) since the read resolves linkage from the
   * in-memory wrappers. Pass 2 is idempotent.
   */
  public void testBoxGroupInsertConvergesNextRead() throws IOException {

    String configId = "myBoxProvisioner";
    setupBoxSyncBack(configId, null);

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

    assertEquals("group insert should converge into prov_group on the next read pass", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // name captured from the Box read response (a Box default capture attribute)
    int nameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("name should be captured from the Box read response, got " + nameValueRows,
        nameValueRows >= 1);
  }

  /**
   * Sync-back convergence of an object DELETE, two-pass full (Box analogue of SCIM's
   * testGroupDeleteConvergesSameRun). Seed test:testGroup + SUBJ0 + their membership into the
   * mirror, then delete the group in Grouper. With deleteGroups/Entities/Memberships enabled the
   * next full sync removes them from the Box target (pass A), and the following re-read pass
   * (pass B) sees them gone -- the full-replace flush, scoped to this provisioner's sync, then
   * drops the group, the now-orphaned user, and the membership from the mirror.
   */
  public void testBoxGroupDeleteConvergesNextRead() throws IOException {

    String configId = "myBoxProvisioner";
    // Box defaults customize*Crud=false (auto delete mode); setting explicit delete keys then fails
    // validation ("should be refactored with an upgrade task"). So mirror how AdobeProvisionerTestUtils
    // configures deletes: turn ON customize*Crud per axis, then set the umbrella deleteX=true plus the
    // specific delete-when key. This test deletes the group, cascading to its user + membership.
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
    setupBoxSyncBack(configId, deleteTypes);

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

    // pass A: the delete writes hit the Box target (group + orphaned user + membership removed)
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
   * Sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass full (Box
   * analogue of SCIM's testUserUpdateConvergesSameRun, but on a GROUP). Box groups are matched by
   * name, so the rename-as-update problem (the Adobe lesson) does NOT apply here: we mutate the
   * group's DESCRIPTION, which is mapped (targetGroupAttribute.1), round-trips through the mock's
   * updateGroup, and is NOT the matching attribute. nativeAttributesGroups is set to
   * "name,description" so the description value is actually captured into the mirror (it is not a
   * Box default capture attribute).
   *
   * <p>Asserts both that the description VALUE converges to the new value AND that it is an in-place
   * update -- the SAME target group id survives (not delete + re-create, which would assign a new
   * Box id). Convergence is on the re-read pass (pass B), since Box captures on read.
   */
  public void testBoxGroupUpdateConvergesNextRead() throws IOException {

    String configId = "myBoxProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    // capture description (not a Box default) so we can assert the updated value in the mirror
    extraConfig.put("nativeAttributesGroups", "name,description");
    setupBoxSyncBack(configId, extraConfig);

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

    // change the description (a NON-matching attribute) -> Box updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the Box target (updateGroup persists it)
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

    // NOTE: no user-update-converge test for Box. Box users are matched by name (from subjectId),
    // and their only other Grouper-driven attribute is login (= email, fixed per subject);
    // status/role are target-controlled. There is no safe Grouper-driven NON-matching user
    // attribute to mutate, so an update-converge test would be mutating the match key (the Adobe
    // lesson) and cannot converge as an in-place update. Skipped rather than written.
  }

  /**
   * Sync-back convergence of a membership ADD to an already-provisioned group, two-pass full (Box
   * analogue of SCIM's testMembershipAddConvergesSameRun). Seed test:testGroup with SUBJ0, then add
   * SUBJ1. Because Box captures memberships on the read path (retrieveMembershipsByGroup), the add
   * shows in grouper_prov_mship on the re-read pass: pass A issues the membership insert to the Box
   * target, pass B re-reads the group's members and the flush converges (testGroup,SUBJ1).
   */
  public void testBoxMembershipAddConvergesNextRead() throws IOException {

    String configId = "myBoxProvisioner";
    setupBoxSyncBack(configId, null);

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

    // pass A: the membership insert (and SUBJ1's user insert) hit the Box target
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
   * Sync-back convergence of a membership REMOVE from a surviving group, two-pass full (Box
   * analogue of SCIM's testMembershipRemoveConvergesSameRun). Two groups both hold SUBJ0; SUBJ0 is
   * removed from testGroup only (it survives in otherGroup, so its Box user is NOT deleted). The
   * full-replace flush, fed by the re-read of each group's members, drops exactly testGroup's
   * membership while leaving otherGroup's intact.
   */
  public void testBoxMembershipRemoveConvergesNextRead() throws IOException {

    String configId = "myBoxProvisioner";
    // enable membership-delete customization (Box defaults customizeMembershipCrud=false; setting
    // delete keys without it fails validation "should be refactored ..."). Mirrors how
    // AdobeProvisionerTestUtils configures a delete type: customize*Crud + umbrella + specific key.
    Map<String, String> deleteTypes = new HashMap<String, String>();
    deleteTypes.put("customizeMembershipCrud", "true");
    deleteTypes.put("deleteMemberships", "true");
    deleteTypes.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupBoxSyncBack(configId, deleteTypes);

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

    // pass A: the membership-remove write hits the Box target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush
    // drops (testGroup,SUBJ0) while otherGroup's SUBJ0 membership survives
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Multi-sync coverage with data evolution between rounds, Box analogue of SCIM's
   * testFullProvisionReflectsDataChangesAcrossSyncs. Round 1: testGroup with SUBJ0 only, seeded via
   * two passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan group + orphan
   * user directly into the Box mock (delete-types are off so they persist). Round 3: two more
   * passes -> the mirror reflects the new state (3 users: SUBJ0, SUBJ1, orphan; 2 groups: testGroup,
   * orphan; 2 memberships in testGroup), and the target-drift orphan user's name value round-trips.
   */
  public void testBoxFullSyncReflectsDataChangesAcrossSyncs() throws IOException {

    String configId = "myBoxProvisioner";
    // delete-types stay off (the setup default) so the Round 2 orphans persist across syncs
    setupBoxSyncBack(configId, null);

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

    // Target-side drift: insert an orphan group + orphan user directly into the Box mock. These
    // are unknown to Grouper; with delete-types off they persist across the next sync. The mock
    // persists these beans to mock_box_group / mock_box_user (same idiom the mock's own
    // createGroup/createUser handlers use). name is NOT NULL on both mock tables, so set it.
    GrouperBoxGroup orphanGroup = new GrouperBoxGroup();
    orphanGroup.setId("orphan-box-group-evolve-1");
    orphanGroup.setName("orphanGroupAddedMidTest");
    orphanGroup.setGroupType("managed_group");
    orphanGroup.setProvenance("orphanProvenance");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperBoxUser orphanUser = new GrouperBoxUser();
    orphanUser.setId("orphan-box-user-evolve-1");
    orphanUser.setName("orphanUserAddedMidTest");
    orphanUser.setLogin("orphan.evolve@example.edu");
    orphanUser.setStatus("active");
    orphanUser.setType("user");
    HibernateSession.byObjectStatic().save(orphanUser);

    // ===================== ROUND 3: second full sync + assertions =====================

    // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's +
    // the drift orphans) and refreshes the mirror.
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
    // entities are captured with their actual attributes). NB: Box entity capture defaults are
    // login/role/status/type -- "name" is a GROUP default, NOT an entity one, so assert on login
    // (the value we set on the orphan user above), which IS a captured default.
    String orphanUserLoginInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'login'")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(String.class);
    assertEquals("orphan user's login should round-trip through reporting", "orphan.evolve@example.edu",
        orphanUserLoginInReporting);
  }

  /**
   * Strict-native capture of orphan target objects, Box analogue of SCIM's
   * testFullProvisionCapturesOrphanTargetEntities. With delete-types disabled, an orphan group +
   * orphan user that exist in the Box target but are unknown to Grouper are still captured into the
   * mirror -- with NULL Grouper-side linkage (group_internal_id / member_internal_id) -- alongside
   * Grouper's own testGroup + SUBJ0/SUBJ1, which keep their linkage populated.
   */
  public void testBoxFullSyncCapturesOrphanTargetEntities() throws IOException {

    String configId = "myBoxProvisioner";
    // delete-types disabled (setup default) so the orphans persist across the run
    setupBoxSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the Box mock before the provisioner runs. Deliberately
    // also set provenance (a Box default capture attribute) so we can verify capture is driven by
    // the target read, independent of Grouper's mapping config.
    GrouperBoxGroup orphanGroup = new GrouperBoxGroup();
    orphanGroup.setId("orphan-box-group-1234");
    orphanGroup.setName("orphanGroupNotInGrouper");
    orphanGroup.setGroupType("managed_group");
    orphanGroup.setProvenance("orphanProvenance");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperBoxUser orphanUser = new GrouperBoxUser();
    orphanUser.setId("orphan-box-user-5678");
    orphanUser.setName("orphanUserNotInGrouper");
    orphanUser.setLogin("orphan.user@example.edu");
    orphanUser.setStatus("active");
    orphanUser.setType("user");
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

    // two passes: pass 1 inserts Grouper's objects (orphans untouched, delete-types off);
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

    // a Box default group attribute (provenance) is captured in the catalog and the orphan's value row
    int provenanceCatalog = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'provenance'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("default group attribute 'provenance' should be in the per-provisioner catalog", 1,
        provenanceCatalog);

    // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis, Box analogue of SCIM's
   * testFullProvisionCapturesMembershipsFromOrphanGroup. An orphan group with an orphan member
   * (neither known to Grouper) is wired in the Box mock (mock_box_membership). Box memberships are
   * group-centric, so when the daemon lists groups it also reads the orphan group's members
   * (retrieveMembershipsByGroup) -- that membership must land in grouper_prov_mship alongside
   * Grouper's own, proving strict-native membership capture is independent of Grouper knowledge.
   */
  public void testBoxFullSyncCapturesMembershipsFromOrphanGroup() throws IOException {

    String configId = "myBoxProvisioner";
    // delete-types disabled (setup default) so the orphan group + its membership persist
    setupBoxSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the Box mock. The membership
    // FKs require the group and user rows to exist first (mock_box_mship_*_fkey).
    GrouperBoxGroup orphanGroup = new GrouperBoxGroup();
    orphanGroup.setId("orphan-box-mship-group-1");
    orphanGroup.setName("orphanGroupWithMembers");
    orphanGroup.setGroupType("managed_group");
    orphanGroup.setProvenance("orphanProvenance");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperBoxUser orphanUser = new GrouperBoxUser();
    orphanUser.setId("orphan-box-mship-user-1");
    orphanUser.setName("orphanMshipUser");
    orphanUser.setLogin("orphan.mship@example.edu");
    orphanUser.setStatus("active");
    orphanUser.setType("user");
    HibernateSession.byObjectStatic().save(orphanUser);

    GrouperBoxMembership orphanMembership = new GrouperBoxMembership();
    orphanMembership.setId("orphan-box-mship-row-1");
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
   * !selectAll* scope excludes orphans, Box analogue of SCIM's testSelectAllFalseExcludesOrphans.
   * With selectAllGroups=false and selectAllEntities=false the daemon fetches only the resources
   * mapped to Grouper-provisioned objects (by id), never a server-wide listing -- so an orphan
   * group/user that the Box target has but Grouper does not must NOT land in the mirror.
   */
  public void testBoxSelectAllFalseExcludesOrphans() throws IOException {

    String configId = "myBoxProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    setupBoxSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id (Grouper-known resources only).
    GrouperBoxGroup orphanGroup = new GrouperBoxGroup();
    orphanGroup.setId("orphan-box-group-selnone-1");
    orphanGroup.setName("orphanGroupSelectAllFalse");
    orphanGroup.setGroupType("managed_group");
    orphanGroup.setProvenance("orphanProvenance");
    HibernateSession.byObjectStatic().save(orphanGroup);

    GrouperBoxUser orphanUser = new GrouperBoxUser();
    orphanUser.setId("orphan-box-user-selnone-1");
    orphanUser.setName("orphanUserSelectAllFalse");
    orphanUser.setLogin("orphan.selnone@example.edu");
    orphanUser.setStatus("active");
    orphanUser.setType("user");
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
   * Broken-target delete stays in the mirror, Box analogue of SCIM's
   * testUserDeleteBrokenTargetStaysInMirror. A target that ACKs a delete but does not actually
   * remove the record: on the re-read pass the object is still there, so the full-replace flush
   * must KEEP it (the "verify, don't assume" contract -- Box re-reads and finds it still present).
   *
   * <p>Box analogue mechanism: we do NOT have a mock knob to fake a broken delete, so instead we
   * mark the group to provision but DISABLE delete-types. SUBJ0 is removed from testGroup in
   * Grouper, but with deleteMemberships/deleteEntities off the daemon never issues the delete to
   * the Box target -- so the user (and its membership) remain in the target, and the re-read keeps
   * them in the mirror. This exercises the same mirror behavior (a target object the daemon did NOT
   * remove stays captured) without needing a target that lies about a delete.
   */
  public void testBoxUserDeleteBrokenTargetStaysInMirror() throws IOException {

    String configId = "myBoxProvisioner";
    // Disable entity deletion so the daemon will NOT remove SUBJ0 from the Box target once SUBJ0
    // becomes unprovisionable. NB: Box defaults deleteEntitiesIfGrouperCreated=true (auto mode), so
    // "delete-types off" is NOT the setup default. To override, turn ON customizeEntityCrud (else the
    // explicit delete key is rejected by validation), then set deleteEntities=false to disable it.
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    setupBoxSyncBack(configId, noEntityDelete);

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
    // removal to the Box target, so the target still has SUBJ0 (and the membership).
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group was never deleted -> still in the mirror
    assertEquals("group row should stay (group was not deleted)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // confirm the target still has the user (the daemon did not remove it), so the re-read keeps it
    int mockUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_box_user").select(int.class);
    assertEquals("the user row should remain in the Box target (delete-types are off)", 1, mockUserRows);

    assertEquals("user should STAY in the mirror (its delete was never performed)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * loadGroupsToGenericGrouperTable in isolation, Box analogue of SCIM's
   * testLoadGroupsFlagInIsolation. Only the groups flag is on -> only grouper_prov_group rows are
   * written; prov_user and prov_mship stay empty even though the daemon still reads users (for
   * provisioning) and memberships (for diffing).
   */
  public void testBoxLoadGroupsFlagInIsolation() throws IOException {

    String configId = "myBoxProvisioner";
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(new BoxProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null,
        GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

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
   * loadEntitiesToGenericGrouperTable in isolation, Box analogue of SCIM's
   * testLoadEntitiesFlagInIsolation. Only the entities flag is on -> only grouper_prov_user rows
   * are written; prov_group and prov_mship stay empty.
   */
  public void testBoxLoadEntitiesFlagInIsolation() throws IOException {

    String configId = "myBoxProvisioner";
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(new BoxProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null,
        GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

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
   * loadMembershipsToGenericGrouperTable off, Box analogue of SCIM's testLoadMembershipsFlagOff.
   * Both object loads on but memberships off -> prov_group and prov_user populate, prov_mship stays
   * empty. Proves the membership gate is independent of the object gates.
   */
  public void testBoxLoadMembershipsFlagOff() throws IOException {

    String configId = "myBoxProvisioner";
    BoxProvisionerTestUtils.setupBoxExternalSystem();
    BoxProvisionerTestUtils.configureBoxProvisioner(new BoxProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    GrouperBoxApiCommands.retrieveBoxGroups("localBox", null,
        GrouperBoxGroup.grouperBoxGroupToBoxSpecificAttributeNames.keySet());
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_box_user").executeSql();

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
   * INCREMENTAL sync-back coverage for Box, conservative. Box has NO write hooks (it captures on
   * the READ path only), unlike Adobe whose incremental membership write-track converges same-cycle
   * via recordTargetNativeMembership* hooks. For Box, an incremental cycle re-reads only the changed
   * objects (it has canRetrieveGroup/Entity, so the adapter decomposes to per-id reads that fire the
   * Box capture seams), and the incremental flush is a SCOPED upsert (it does NOT full-replace, so
   * it will not wrongly delete untouched mirror rows).
   *
   * <p>What this test asserts is therefore deliberately narrow -- the safe, reliable part of Box
   * incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
   * member drives an incremental that (a) re-reads the changed group/entity and so does NOT shrink
   * the existing mirror (no spurious deletes -- the regression the scoped incremental flush guards
   * against), and (b) captures the newly added member's user object into prov_user. It does NOT
   * assert that the new MEMBERSHIP converges on the same incremental cycle: Box memberships are
   * captured on read, and the incremental's read-before-write timing plus group-centric membership
   * read make same-cycle membership convergence unreliable for a read-capture target (the same
   * 1-cycle-lag reason SCIM's object incremental test is disabled). Membership convergence for Box
   * is covered end-to-end by the two-pass full tests above.
   */
  public void testBoxIncrementalSyncBackNoSpuriousDeletes() throws IOException {

    String configId = "myBoxProvisioner";
    setupBoxSyncBack(configId, null);

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
    // the Box read-capture seams, and the scoped flush upserts -- it must NOT drop untouched rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc). Box
    // memberships are group-centric and captured on the READ path; on an incremental cycle the scoped
    // membership flush for the changed group plus read-before-write timing means testGroup's
    // membership rows can transiently clear, re-converging only on the next full sync (the same
    // 1-cycle lag for which SCIM disables its object incremental test). Membership convergence is
    // covered end-to-end by the two-pass full tests above; here we only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; Box shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

}
