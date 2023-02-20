package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.io.IOException;
import java.util.List;

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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
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
    TestRunner.run(new GrouperBoxProvisionerTest("testIncrementalSyncBox2"));
    
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

  public void testIncrementalSyncBox2() throws IOException {
    
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
      // the old member(ship) is here too
      assertEquals(1, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
      assertEquals(3, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
      assertEquals(3, GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()));

      provisioningMembershipWrapper0 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member0.getId()));

      provisioningMembershipWrapper3 = grouperProvisioner.retrieveGrouperProvisioningDataIndex()
          .getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(new MultiKey(testGroup.getId(), member3.getId()));


      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper0.getProvisioningStateMembership().isDelete());

      assertEquals(true, provisioningMembershipWrapper3.getProvisioningStateMembership().isRecalcObject());
      assertEquals(true, provisioningMembershipWrapper3.getProvisioningStateMembership().isDelete());
      

    } finally {
      
    }
    
  }

}
