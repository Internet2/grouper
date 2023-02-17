package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
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
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

public class RemedyProvisionerTest extends GrouperProvisioningBaseTest {

  @Override
  public String defaultConfigId() {
    return "myRemedyProvisioner";
  }
  
  public static void main(String[] args) {

    GrouperStartup.startup();
    TestRunner.run(new RemedyProvisionerTest("testFullRemedyProvisioner"));
  
  }

  public RemedyProvisionerTest() {
    super();
  }
  
  public RemedyProvisionerTest(String name) {
    super(name);
  }
  
  public static boolean startTomcat = false;
  
  public void testFullRemedyProvisioner() {
    
    RemedyProvisionerTestUtils.setupRemedyExternalSystem();
    RemedyProvisionerTestUtils.configureRemedyProvisioner(new RemedyProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      Map<Long, GrouperRemedyGroup> grouperRemedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups("myRemedy");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_user").executeSql();
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_auth").executeSql();

      new GcDbAccess().connectionName("grouper").sql("insert into mock_remedy_group values ('testGroup', 123456)").executeSql();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_remedy_user values ('P123', 'id.test.subject.0')").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myRemedyProvisioner");
      attributeValue.setTargetName("myRemedyProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_remedy_group").select(int.class));
  
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      GrouperRemedyMembership grouperRemedyMembership = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).get(0);
      
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
      
      assertEquals("P123", grouperRemedyMembership.getPersonId());
      assertEquals("testGroup", grouperRemedyMembership.getPermissionGroup());
      assertEquals(Long.valueOf(123456L), grouperRemedyMembership.getPermissionGroupId());
      assertEquals("id.test.subject.0", grouperRemedyMembership.getRemedyLoginId());
      assertEquals("Enabled", grouperRemedyMembership.getStatus());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myRemedyProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);
      
      // now run the full sync again and the member should be deleted from mock_box_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      
      grouperRemedyMembership = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).get(0);
      
      assertEquals("P123", grouperRemedyMembership.getPersonId());
      assertEquals("testGroup", grouperRemedyMembership.getPermissionGroup());
      assertEquals(Long.valueOf(123456L), grouperRemedyMembership.getPermissionGroupId());
      assertEquals("id.test.subject.0", grouperRemedyMembership.getRemedyLoginId());
      assertEquals("Delete", grouperRemedyMembership.getStatus());
//      
//      //now add one subject
//      testGroup.addMember(SubjectTestHelper.SUBJ3);
//      
//      // now run the full sync again
//      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
//      
//      
//      // update group description and settings
//      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
//          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
//          .assignSaveMode(SaveMode.UPDATE).save();
//      
//      attributeValue = new GrouperProvisioningAttributeValue();
//      attributeValue.setDirectAssignment(true);
//      attributeValue.setDoProvision("myRemedyProvisioner");
//      attributeValue.setTargetName("myRemedyProvisioner");
//      
//      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
//      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      
//      GrouperRemedyGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).get(0);
//      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
//      
//      //now delete the group and sync again
//      testGroup.delete();
//      
//      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      
    } finally {
      
    }
    
  }
}
