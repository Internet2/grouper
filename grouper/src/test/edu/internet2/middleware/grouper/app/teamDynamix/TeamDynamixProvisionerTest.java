package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.List;

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
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

public class TeamDynamixProvisionerTest extends GrouperProvisioningBaseTest {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new TeamDynamixProvisionerTest("testFullProvisionGroupAndThenDeleteTheGroup"));
  }
  
  
  @Override
  public String defaultConfigId() {
    return "myTeamDynamixProvisioner";
  }
  
  public TeamDynamixProvisionerTest(String name) {
    super(name);
  }

  public TeamDynamixProvisionerTest() {
  }
  
  public void setUp() {
    super.setUp();
    
    TeamDynamixProvisionerTestUtils.setupTeamDynamixExternalSystem();
    
    try {
      TeamDynamixApiCommands.retrieveTeamDynamixGroups("teamdx");
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();  
    } catch (Exception e) {
      
    }
  }
  
  public static boolean startTomcat = false;
  
  /**
   * 
   */
  public void testFullProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();
      
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
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myTeamDynamixProvisioner");
      attributeValue.setTargetName("myTeamDynamixProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_group").select(int.class));
//  
//      
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      TeamDynamixGroup grouperDuoGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", grouperDuoGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myTeamDynamixProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_duo_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      
      List<TeamDynamixUser> users = HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class);
      
      for (TeamDynamixUser user: users) {
        
        if (user.getFirstName().equals("test.subject.0")) {
          assertEquals(user.getActiveDb(), "T");
        }
        
        if (user.getFirstName().equals("test.subject.1")) {
          assertEquals(user.getActiveDb(), "F");
        }
       
      }
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      
      List<TeamDynamixGroup> groups = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class);
      
      for (TeamDynamixGroup group: groups) {
        assertEquals(group.getActiveDb(), "F");
      }
      
      users = HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class);
      
      for (TeamDynamixUser user: users) {
        assertEquals(user.getActiveDb(), "F");       
      }
      
      // create the same group again and add one of the inactive subjects
      testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ5, false);
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      grouperDuoGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", grouperDuoGroup.getName());
      assertEquals(grouperDuoGroup.getActiveDb(), "T");
      
      users = HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class);
      
      for (TeamDynamixUser user: users) {
        
        if (user.getFirstName().equals("test.subject.0")) {
          assertEquals(user.getActiveDb(), "T");
        }
        
        if (user.getFirstName().equals("test.subject.1")) {
          assertEquals(user.getActiveDb(), "F");
        }
        
        if (user.getFirstName().equals("test.subject.5")) {
          assertEquals(user.getActiveDb(), "T");
        }
       
      }
      
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  
  

}
