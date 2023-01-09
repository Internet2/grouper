package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

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

public class DigitalMarketplaceProvisionerTest extends GrouperProvisioningBaseTest {

  @Override
  public String defaultConfigId() {
    return "myDigitalMarketplaceProvisioner";
  }
  
  public static void main(String[] args) {

    GrouperStartup.startup();
    TestRunner.run(new DigitalMarketplaceProvisionerTest("testFullDigitalMarketplaceProvisioner"));
  
  }

  public DigitalMarketplaceProvisionerTest() {
    super();
  }
  
  public DigitalMarketplaceProvisionerTest(String name) {
    super(name);
  }
  
  public static boolean startTomcat = false;
  
  public void testFullDigitalMarketplaceProvisioner() {
    
    DigitalMarketplaceProvisionerTestUtils.setupDigitalMarketplaceExternalSystem();
    DigitalMarketplaceProvisionerTestUtils.configureDigitalMarketplaceProvisioner(new DigitalMarketplaceProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      Map<String, GrouperDigitalMarketplaceGroup> grouperDigitalMarketplaceGroups = GrouperDigitalMarketplaceApiCommands.retrieveDigitalMarketplaceGroups("myDigitalMarketplace");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_mp_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_marketplace_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_marketplace_user").executeSql();
//      new GcDbAccess().connectionName("grouper").sql("delete from mock_digital_marketplace_auth").executeSql();

      new GcDbAccess().connectionName("grouper").sql("insert into mock_digital_marketplace_user values ('id.test.subject.0', 'test.subject.0')").executeSql();
      
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
      attributeValue.setDoProvision("myDigitalMarketplaceProvisioner");
      attributeValue.setTargetName("myDigitalMarketplaceProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup").list(GrouperDigitalMarketplaceGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceUser").list(GrouperDigitalMarketplaceUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership").list(GrouperDigitalMarketplaceMembership.class).size());
      GrouperDigitalMarketplaceMembership grouperDigitalMarketplaceMembership = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership").list(GrouperDigitalMarketplaceMembership.class).get(0);
      
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
      
      assertEquals("testGroup", grouperDigitalMarketplaceMembership.getGroupName());
      assertEquals("id.test.subject.0", grouperDigitalMarketplaceMembership.getLoginName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDigitalMarketplaceProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);
      
      // now run the full sync again and the member should be deleted from mock_box_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup").list(GrouperDigitalMarketplaceGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceUser").list(GrouperDigitalMarketplaceUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership").list(GrouperDigitalMarketplaceMembership.class).size());
      
      List<GrouperDigitalMarketplaceMembership> list = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership").list(GrouperDigitalMarketplaceMembership.class);
      
      assertEquals(0, list.size());
      
//      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup").list(GrouperDigitalMarketplaceGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceUser").list(GrouperDigitalMarketplaceUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership").list(GrouperDigitalMarketplaceMembership.class).size());
      
    } finally {
      
    }
    
  }
}
