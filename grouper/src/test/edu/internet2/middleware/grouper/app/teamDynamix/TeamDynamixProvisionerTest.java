package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.duo.DuoProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.duo.DuoProvisioningStartWith;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoGroup;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoMembership;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoUser;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
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

public class TeamDynamixProvisionerTest extends GrouperProvisioningBaseTest {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new TeamDynamixProvisionerTest("testFullSyncTeamDynamixStartWithAndDiagnostics"));
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
  
  
  public void testIncrementalProvisionTeamDynamix() {
    
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
            
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_teamdynamix_group").select(int.class));
      
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
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myTeamDynamixProvisioner");
      attributeValue.setTargetName("myTeamDynamixProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_teamdynamix_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      TeamDynamixGroup grouperDuoGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", grouperDuoGroup.getName());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
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
      
      incrementalProvision();
      
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
      
      incrementalProvision();
      
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
  
  
  public void testFullSyncTeamDynamixStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput());
      
      TeamDynamixProvisioningStartWith startWith = new TeamDynamixProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("teamDynamixExternalSystemConfigId", "teamdx");
      startWithSuffixToValue.put("teamDynamixPattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("selectAllGroups", "false");
      startWithSuffixToValue.put("manageGroups", "true");
      startWithSuffixToValue.put("groupNameAttributeValue", "name");
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "false");
      startWithSuffixToValue.put("entityUserFirstName", "name");
      startWithSuffixToValue.put("entityUserLastName", "name");
      startWithSuffixToValue.put("entityPrimaryEmail", "email");
      startWithSuffixToValue.put("entityUsername", "subjectId");
      startWithSuffixToValue.put("entityExternalId", "subjectId");
      startWithSuffixToValue.put("entitySecurityRoleId", "name");
      startWithSuffixToValue.put("entityCompany", "name");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myTeamDynamixProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myTeamDynamixProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myTeamDynamixProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myTeamDynamixProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myTeamDynamixProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myTeamDynamixProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myTeamDynamixProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myTeamDynamixProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myTeamDynamixProvisioner.provisionerConfigId").value("myTeamDynamixProvisioner").store();
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup1").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myTeamDynamixProvisioner");
      attributeValue.setTargetName("myTeamDynamixProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
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
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myTeamDynamixProvisioner");
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
  
  
  

}
