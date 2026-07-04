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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
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
import junit.textui.TestRunner;

public class TeamDynamixProvisionerTest extends GrouperProvisioningBaseTest {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new TeamDynamixProvisionerTest("testIncrementalProvisionTeamDynamix"));
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
      
//      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser").list(TeamDynamixUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership").list(TeamDynamixMembership.class).size());
      TeamDynamixGroup teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", teamDynamixGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myTeamDynamixProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_teamdynamix_membership also
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
//        assertEquals(group.getActiveDb(), "F");
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
      teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", teamDynamixGroup.getName());
//      assertEquals(teamDynamixGroup.getActiveDb(), "T");
      
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
      TeamDynamixGroup teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", teamDynamixGroup.getName());
      
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
//        assertEquals(group.getActiveDb(), "F");
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
      teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);
      
      assertEquals("test:testGroup", teamDynamixGroup.getName());
//      assertEquals(teamDynamixGroup.getActiveDb(), "T");
      
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

    if (!tomcatRunTests()) {
      return;
    }

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
      TeamDynamixGroup teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup").list(TeamDynamixGroup.class).get(0);

      assertEquals("test:testGroup1", teamDynamixGroup.getName());

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
  
  /**
   * test that batch sizes from the DAO capabilities flow through the behavior class correctly,
   * that user config can reduce but not increase beyond the DAO max,
   * and that invalid batch sizes are rejected by validation
   */
  public void testBatchSizeConfiguration() {

    if (!tomcatRunTests()) {
      return;
    }

    // test 1: default batch sizes from TDX DAO capabilities
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput());

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myTeamDynamixProvisioner");
    attributeValue.setTargetName("myTeamDynamixProvisioner");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperProvisioningBehavior behavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    // TDX DAO sets insertMemberships=400, deleteMemberships=400, default stays at 20
    assertEquals(400, behavior.getProvisionerBatchingInsertMemberships());
    assertEquals(400, behavior.getProvisionerBatchingDeleteMemberships());
    // these fall back to DAO defaultBatchSize of 20
    assertEquals(20, behavior.getProvisionerBatchingUpdateGroups());
    assertEquals(20, behavior.getProvisionerBatchingRetrieveGroups());
    assertEquals(20, behavior.getProvisionerBatchingRetrieveEntities());
    assertEquals(20, behavior.getProvisionerBatchingInsertGroups());

    // verify batch sizes are in the debug map
    Map<String, Object> debugMap = grouperProvisioner.getDebugMap();
    assertEquals(400, debugMap.get("provisionerBatchingInsertMemberships"));

    // test 2: user config can reduce batch sizes
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

    TeamDynamixProvisionerTestUtils.configureProvisioner(
        new TeamDynamixProvisionerTestConfigInput()
            .addExtraConfig("provisionerBatchingInsertMemberships", "50")
            .addExtraConfig("provisionerBatchingDefault", "5")
    );

    fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    behavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    // user set insertMemberships to 50, which is less than DAO's 400, so it should be 50
    assertEquals(50, behavior.getProvisionerBatchingInsertMemberships());
    // user set default to 5, which is less than DAO's 20, so it should be 5
    assertEquals(5, behavior.getProvisionerBatchingDefault());
    // updateGroups was not configured by user, but default is now 5 (min of 5 and 20)
    assertEquals(5, behavior.getProvisionerBatchingUpdateGroups());

    // test 3: user config cannot increase beyond DAO max
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

    TeamDynamixProvisionerTestUtils.configureProvisioner(
        new TeamDynamixProvisionerTestConfigInput()
            .addExtraConfig("provisionerBatchingInsertMemberships", "999")
    );

    fullProvision();
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    behavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    // user set 999 but DAO max is 400, so effective should be 400
    assertEquals(400, behavior.getProvisionerBatchingInsertMemberships());

    // test 4: invalid batch size (0) is rejected by validation
    GrouperProvisioningConfigurationValidation validation = new GrouperProvisioningConfigurationValidation();
    validation.getSuffixToConfigValue().put("provisionerBatchingInsertMemberships", "0");
    validation.validateBatchSizes();
    assertTrue(validation.getProvisioningValidationIssues().size() > 0);

    // test 5: valid batch size passes validation
    GrouperProvisioningConfigurationValidation validation2 = new GrouperProvisioningConfigurationValidation();
    validation2.getSuffixToConfigValue().put("provisionerBatchingInsertMemberships", "100");
    validation2.validateBatchSizes();
    assertEquals(0, validation2.getProvisioningValidationIssues().size());
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
   * provision populates grouper_prov_user / _group / _mship from the TeamDynamix read
   * path. Asserts all three axes have rows and at least one row per axis is linked back
   * to its Grouper counterpart.
   */
  public void testTeamDynamixFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

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

    // first pass writes the TeamDynamix target; second pass reads it back and flushes
    fullProvision();
    fullProvision();

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
  public void testTeamDynamixFullSyncSelectByIdsPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

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

  // ==========================================================================================
  // SCIM-parity sync-back CRUD coverage for TeamDynamix (mirrors the Box pilot --
  // boxProvisioner/GrouperBoxProvisionerTest). Sync-back = a provisioning RUN reads the target's
  // state and captures it into grouper_prov_group / grouper_prov_user / grouper_prov_mship
  // (+ _attr/_attr_value). All tests are gated on tomcatRunTests() like the existing TeamDynamix
  // sync-back tests, reuse the SAME configId ("myTeamDynamixProvisioner") + config builder
  // (TeamDynamixProvisionerTestUtils.configureProvisioner) + mock-table seeding idiom, and stay
  // capability-gated.
  //
  // ---- CAPABILITY -> TEST MATRIX (capabilities from TeamDynamixTargetDao
  //      registerGrouperProvisionerDaoCapabilities; defaults from
  //      TeamDynamixProvisioningTargetNativeSync.DEFAULT_*_ATTRS) ----
  //
  //   canInsertGroup=true          -> testTeamDynamixGroupInsertConvergesNextRead
  //   canInsertEntity=true         -> covered by the same test (SUBJ0 user captured)
  //   canInsertMemberships=true    -> testTeamDynamixMembershipAddConvergesNextRead
  //   canDeleteMemberships=true    -> testTeamDynamixMembershipRemoveConvergesNextRead
  //                                   (mock deleteMemberships is a HARD delete, so this converges)
  //   canUpdateGroup=true          -> testTeamDynamixGroupUpdateConvergesNextRead (mutates the
  //                                   NON-matching attribute Description; group is Name-matched)
  //   canUpdateEntity=true         -> NO user-update-converge test. See the skip note on
  //                                   testTeamDynamixGroupUpdateConvergesNextRead: every
  //                                   Grouper-driven user attribute is either the match key
  //                                   (ExternalID) or fixed per subject (FirstName/LastName/
  //                                   UserName/PrimaryEmail from subjectId/name/email); Company +
  //                                   SecurityRoleID are static. There is no safe Grouper-driven
  //                                   NON-matching user attribute to mutate, so an update-converge
  //                                   test would mutate a fixed/match value and cannot demonstrate
  //                                   in-place convergence. Skipped rather than written.
  //   canDeleteGroup / canDeleteEntity=true BUT NOT a hard delete on the target:
  //                                   -> NO object-delete-converge test (the Box
  //                                   testBoxGroupDeleteConvergesNextRead analogue is IMPOSSIBLE
  //                                   here). TeamDynamixTargetDao.deleteGroup calls
  //                                   updateTeamDynamixGroup (the mock's updateGroup does
  //                                   saveOrUpdate -> the group ROW PERSISTS in
  //                                   mock_teamdynamix_group), and deleteEntity is a SOFT delete
  //                                   (updateTeamDynamixUserStatus sets active=false; the user ROW
  //                                   PERSISTS in mock_teamdynamix_user). The existing forward
  //                                   tests (testFullProvisionGroupAndThenDeleteTheGroup) confirm
  //                                   group/user counts do NOT drop after a Grouper-side delete.
  //                                   Because sync-back captures whatever the target still has on
  //                                   the re-read, a deleted group/user would STAY in the mirror --
  //                                   so a "converges to 0" assertion would FAIL. The surviving-row
  //                                   behavior is instead asserted by
  //                                   testTeamDynamixObjectDeleteStaysInMirror below.
  //   ReplaceMembership: NOT a registered capability (no setCanReplaceMembership) -> skipped.
  //
  // ---- DEFAULT_*_ATTRS asserted on (PascalCase JSON source -> friendly stored key) ----
  //   groups:   name      (from /Name)         -- DEFAULT_GROUP_ATTRS
  //   entities: userName  (from /UserName),
  //             primaryEmail (from /PrimaryEmail),
  //             active    (from /IsActive)      -- DEFAULT_ENTITY_ATTRS
  //   (Description is NOT a default group capture attribute, so the update test configures
  //    nativeAttributesGroups=name,Description to capture its value.)
  // ==========================================================================================

  /**
   * Shared setup for the TeamDynamix sync-back tests: configure the provisioner (same builder the
   * existing sync-back tests use) with the three load*ToGenericGrouperTable flags on and
   * recalculateAllOperations so every object/membership is processed each run, start Grouper, then
   * clean the TeamDynamix mock target. The caller starts its own root session and creates the
   * Grouper-side stems/groups/members it needs. Mirrors setupBoxSyncBack in the Box pilot.
   *
   * <p>Leaves the delete-types at their config defaults unless the caller passes the delete-type
   * suffixes explicitly (TeamDynamix defaults customize*Crud=false; setting explicit delete keys
   * without it fails validation -- the Box/Adobe lesson -- so enabling a delete needs
   * customizeXCrud=true + deleteX=true + the specific deleteXIf... key).
   *
   * @param configId the provisioner config id (always "myTeamDynamixProvisioner" here)
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes to set (may be null)
   */
  private void setupTeamDynamixSyncBack(String configId, Map<String, String> extraConfig) {

    TeamDynamixProvisionerTestConfigInput configInput = new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    TeamDynamixProvisionerTestUtils.configureProvisioner(configInput);

    GrouperStartup.startup();

    // wipe the mock target (same idiom as the existing TeamDynamix tests); setUp() has already
    // ensured the mock tables exist via the TeamDynamixApiCommands read.
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();
  }

  /**
   * The single provisioned group's target_group_id (TeamDynamix group ID) in the mirror, or null.
   * Mirrors the Box helper of the same name -- used by the update-converge test to prove the SAME
   * target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved {@code Description} attribute value for the single provisioned group in the mirror, or
   * null. Reads through the {@code grouper_prov_group_attr_v} reporting view (column
   * {@code value_string}), because the raw string is stored via a dictionary FK and only the view
   * resolves it back to text. {@code Description} is captured only because the update-converge test
   * configures {@code nativeAttributesGroups=name,Description} -- it is NOT a TeamDynamix default
   * capture attribute (the only default group attribute is {@code name} from {@code /Name}), so
   * without that config this returns null. The stored attribute_name is {@code Description}
   * (PascalCase) because that is the configured native-attribute {@code name} (its JSON Pointer
   * defaults to {@code /Description}).
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'Description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Build + persist an orphan TeamDynamix user directly into the mock (mock_teamdynamix_user),
   * unknown to Grouper. Sets every NOT NULL column (first_name/last_name/primary_email/
   * security_role_id/user_name/external_id) plus active, so the row inserts and so the default
   * capture attributes (userName/primaryEmail/active) and the target id (/UID = id) round-trip on a
   * server-wide read. Mirrors the inline orphan-user seeding in the Box pilot.
   */
  private TeamDynamixUser saveOrphanTeamDynamixUser(String id, String userName, String primaryEmail) {
    TeamDynamixUser orphanUser = new TeamDynamixUser();
    orphanUser.setId(id);
    orphanUser.setFirstName(userName);
    orphanUser.setLastName("orphanLast");
    orphanUser.setPrimaryEmail(primaryEmail);
    orphanUser.setSecurityRoleId("573ef9e3-e01f-422b-bb1d-a5efbc8553a5");
    orphanUser.setUserName(userName);
    orphanUser.setExternalId(id);
    orphanUser.setActive(true);
    HibernateSession.byObjectStatic().save(orphanUser);
    return orphanUser;
  }

  /**
   * Build + persist an orphan TeamDynamix group directly into the mock (mock_teamdynamix_group),
   * unknown to Grouper. Sets name (NOT NULL) and description. The default capture attribute name
   * (from /Name) and the target id (/ID = id) round-trip on a server-wide read. Mirrors the inline
   * orphan-group seeding in the Box pilot.
   */
  private TeamDynamixGroup saveOrphanTeamDynamixGroup(String id, String name, String description) {
    TeamDynamixGroup orphanGroup = new TeamDynamixGroup();
    orphanGroup.setId(id);
    orphanGroup.setName(name);
    orphanGroup.setDescription(description);
    HibernateSession.byObjectStatic().save(orphanGroup);
    return orphanGroup;
  }

  /**
   * Sync-back convergence of a newly created GROUP, two-pass full (TeamDynamix analogue of Box's
   * testBoxGroupInsertConvergesNextRead). TeamDynamix captures objects on the READ path (the
   * TeamDynamixApiCommands read seam), so the group converges into grouper_prov_group on the
   * re-read pass: pass 1 inserts the group into the TeamDynamix target, pass 2 reads it back and the
   * flush captures it -- linked to its Grouper group (group_internal_id not null) and with its
   * default capture attribute {@code name} (from {@code /Name}) recorded. NB: unlike the Box pilot
   * (which asserts same-run convergence after pass 1), this mirrors the EXISTING TeamDynamix
   * full-sync-back tests and asserts after two passes -- conservative, and provably what the
   * existing tests rely on.
   */
  public void testTeamDynamixGroupInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    setupTeamDynamixSyncBack(configId, null);

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

    // pass 1 inserts the group into the TeamDynamix target (no capture yet); pass 2 reads it back
    // and the flush captures it
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("group insert should converge into prov_group on the re-read pass", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // name captured from the TeamDynamix read response (a default group capture attribute, /Name)
    int nameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("name should be captured from the TeamDynamix read response, got " + nameValueRows,
        nameValueRows >= 1);
  }

  /**
   * Sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass full
   * (TeamDynamix analogue of Box's testBoxGroupUpdateConvergesNextRead). TeamDynamix groups are
   * matched by {@code Name}, so the rename-as-update problem (the Adobe lesson) does NOT apply: we
   * mutate the group's {@code Description}, which is mapped (targetGroupAttribute.2), round-trips
   * through the mock's updateGroup, and is NOT the matching attribute. {@code nativeAttributesGroups}
   * is set to {@code name,Description} so the Description value is actually captured into the mirror
   * (it is not a TeamDynamix default capture attribute).
   *
   * <p>Asserts both that the Description VALUE converges to the new value AND that it is an in-place
   * update -- the SAME target group id survives (not delete + re-create). Convergence is on the
   * re-read pass, since TeamDynamix captures on read.
   *
   * <p>NOTE: no rename-as-update test (groups are Name-matched, so renaming mutates the match key)
   * and no user-update-converge test -- see the capability-matrix comment above (canUpdateEntity):
   * there is no safe Grouper-driven NON-matching user attribute to mutate.
   */
  public void testTeamDynamixGroupUpdateConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    // capture Description (PascalCase JSON field /Description; not a TeamDynamix default) so we can
    // assert the updated value in the mirror. "name" stays so the default group attr is unaffected.
    extraConfig.put("nativeAttributesGroups", "name,Description");
    setupTeamDynamixSyncBack(configId, extraConfig);

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

    // change the description (a NON-matching attribute) -> TeamDynamix updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the TeamDynamix target (updateGroup persists it)
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
  }

  /**
   * Sync-back convergence of a membership ADD to an already-provisioned group, two-pass full
   * (TeamDynamix analogue of Box's testBoxMembershipAddConvergesNextRead). Seed test:testGroup with
   * SUBJ0, then add SUBJ1. TeamDynamix now captures memberships on the WRITE path
   * (TeamDynamixTargetDao.insertMemberships -> recordTargetNativeMembershipInsert), like Adobe/SCIM,
   * so a membership add is recorded into the native mirror on the write and converges on the write
   * pass; it also still shows on a re-read via the read-path capture
   * (retrieveMembershipsByGroup -> captureMembershipsForGroup). This test drives two full passes:
   * pass A issues the membership insert to the target, pass B re-reads and the mirror holds
   * (testGroup, SUBJ1). The group/user OBJECTS still capture on the read path.
   */
  public void testTeamDynamixMembershipAddConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    setupTeamDynamixSyncBack(configId, null);

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

    // pass A: the membership insert (and SUBJ1's user insert) hit the TeamDynamix target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // the capture-on-write hook mirrors the inserted membership on the write pass itself,
    // so the count is already correct BEFORE any re-read converges it
    assertEquals("add converges on the write pass via capture-on-write (before any re-read)", 2,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read sees both members; the flush converges the added membership
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("the added membership should converge on the re-read pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of a membership REMOVE from a surviving group, two-pass full (TeamDynamix
   * analogue of Box's testBoxMembershipRemoveConvergesNextRead). Two groups both hold SUBJ0; SUBJ0
   * is removed from testGroup only (it survives in otherGroup, so its TeamDynamix user is not
   * deleted). The membership delete is a HARD delete on the TeamDynamix mock
   * (deleteMemberships -> "delete from TeamDynamixMembership"), so the full-replace flush, fed by
   * the re-read of each group's members, drops exactly testGroup's membership while leaving
   * otherGroup's intact.
   *
   * <p>Enables membership-delete customization (TeamDynamix defaults customize*Crud unset; setting
   * delete keys without it fails validation) the way Adobe/Box configure a delete type:
   * customizeMembershipCrud + the umbrella deleteMemberships + the specific
   * deleteMembershipsIfNotExistInGrouper.
   */
  public void testTeamDynamixMembershipRemoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    Map<String, String> deleteTypes = new HashMap<String, String>();
    deleteTypes.put("customizeMembershipCrud", "true");
    deleteTypes.put("deleteMemberships", "true");
    deleteTypes.put("deleteMembershipsIfNotExistInGrouper", "true");
    setupTeamDynamixSyncBack(configId, deleteTypes);

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

    // pass A: the membership-remove write hits the TeamDynamix target (hard delete)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // capture-on-write is the ONLY thing that can drop the mirror row here: pass A's retrieveAllData
    // read still sees SUBJ0 in testGroup (not yet removed from the target when the read ran), so only
    // the write-delete hook removes (testGroup,SUBJ0) from the mirror before any re-read
    assertEquals("remove drops from the mirror on the write pass via capture-on-write (before any re-read)", 1,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush drops
    // (testGroup,SUBJ0) while otherGroup's SUBJ0 membership survives
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Surviving-row behavior on an object DELETE (the TeamDynamix substitute for Box's
   * testBoxGroupDeleteConvergesNextRead, which is IMPOSSIBLE here -- see the capability-matrix
   * comment). TeamDynamix never hard-deletes a group or user on the target: deleteGroup is an
   * updateGroup (the row persists) and deleteEntity is a soft delete (active=false; the row
   * persists). So after a Grouper-side delete, the next read still finds the group + user on the
   * target and the mirror KEEPS them. This is the "verify, don't assume" contract: sync-back
   * reflects the target's ACTUAL state, and a target that retains the object retains it in the
   * mirror.
   *
   * <p>We enable GROUP delete customization so the daemon actually issues the (soft) group delete,
   * then prove the mirror still holds the group and user after the re-read.
   *
   * <p>TWO sync-back gotchas drive the extra config here (both rooted in the fact that TeamDynamix
   * captures only on the active-filtered READ path -- retrieveTeamDynamixUsers/Groups send
   * IsActive=true -- and the shared TeamDynamix config sets selectAllEntities=false, unlike the
   * Box/Datadog reference configs which set it true):
   * <ul>
   *   <li><b>entity delete is turned OFF via customizeEntityCrud=true + deleteEntities=false.</b> This
   *   is the load-bearing fix and the same idiom as the green Okta/Datadog "stays in mirror" tests.
   *   GOTCHA: the shared TeamDynamix config sets makeChangesToEntities=true, which DEFAULTS
   *   deleteEntities to true (GrouperProvisioningConfiguration resets insert/update/deleteEntities=true
   *   when makeChangesToEntities is on). That default is only overridable inside the
   *   {@code if (customizeEntityCrud)} block -- so merely NOT setting customizeEntityCrud leaves entity
   *   delete ON, not off. With it on, once the group is deleted in Grouper SUBJ0 is unprovisionable and
   *   the daemon SOFT-deletes it (deleteEntity -> updateTeamDynamixUserStatus sets active=false).
   *   TeamDynamix's user read then filters IsActive=true, so the now-inactive SUBJ0 is excluded from the
   *   re-read and DROPS from the mirror (0 instead of 1) -- even though its row physically survives in
   *   mock_teamdynamix_user. The whole point of this test is that the target ROW survives a delete; so to
   *   keep the captured-from-read mirror in sync with that surviving row, SUBJ0 must stay active. Hence
   *   customizeEntityCrud=true (so the override block runs) + deleteEntities=false (so it stays active).
   *   The GROUP delete stays on: deleteGroup is an updateGroup (row persists) and the mock's group
   *   search ignores IsActive (groups have no active field), so the group survives the read and the
   *   mirror keeps it regardless.</li>
   *   <li><b>selectAllEntities=true is added.</b> With the shared config's selectAllEntities=false the
   *   framework scoped-retrieves only entities still attached to a provisioned group; after the group
   *   is deleted SUBJ0 is attached to nothing, so it would never be re-read (and never re-captured)
   *   even while active. Setting selectAllEntities=true makes the daemon do the server-wide
   *   retrieveAllEntities listing (active filter), which lists the still-active SUBJ0 so it is
   *   captured and STAYS in the mirror -- the surviving-row behavior this test asserts. (selectAllGroups
   *   already defaults to true here, which is why the group axis needs no extra config.)</li>
   * </ul>
   */
  public void testTeamDynamixObjectDeleteStaysInMirror() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    // turn on GROUP delete customization (customize*Crud + umbrella + specific key) so the daemon
    // issues the soft group delete; the point is that the target row survives it anyway.
    //
    // Entity delete must be explicitly turned OFF: the shared config sets makeChangesToEntities=true,
    // which DEFAULTS deleteEntities to true. The only place that default is overridable is the
    // GrouperProvisioningConfiguration {@code if (customizeEntityCrud)} block -- so we set
    // customizeEntityCrud=true AND deleteEntities=false (the same idiom the green Okta/Datadog
    // stays-in-mirror tests use). Without this, the orphaned SUBJ0 gets soft-deleted (active=false),
    // is filtered out of the IsActive=true re-read, and its mirror row is deleted -- the failure.
    //
    // selectAllEntities=true forces the server-wide retrieveAllEntities listing so the still-active
    // SUBJ0 is re-read and stays captured (the shared config sets selectAllEntities=false, under which
    // a group-less SUBJ0 would never be re-read at all).
    Map<String, String> deleteTypes = new HashMap<String, String>();
    deleteTypes.put("customizeGroupCrud", "true");
    deleteTypes.put("deleteGroups", "true");
    deleteTypes.put("deleteGroupsIfNotExistInGrouper", "true");
    deleteTypes.put("customizeEntityCrud", "true");
    deleteTypes.put("deleteEntities", "false");
    deleteTypes.put("selectAllEntities", "true");
    setupTeamDynamixSyncBack(configId, deleteTypes);

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

    // seed: group + SUBJ0 in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

    // delete the group in Grouper. SUBJ0 becomes orphaned (a member of no provisioned group). The
    // daemon issues the (soft) GROUP delete; with entity delete off, SUBJ0 is left active.
    testGroup.delete();

    // pass A: the daemon issues deleteGroup (an update -- the row persists); SUBJ0 stays active
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read still finds the group (its row persists, group search ignores IsActive) and
    // still-active SUBJ0 (listed by the server-wide retrieveAllEntities via selectAllEntities=true),
    // so the mirror keeps both
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group row was never removed from the target -> still present in the mock
    int mockGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_teamdynamix_group").select(int.class);
    assertEquals("group row should remain in the TeamDynamix target (delete is an update)", 1,
        mockGroupRows);
    // the user row was never removed either (no hard delete; entity delete is off so it stays active)
    int mockUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_teamdynamix_user").select(int.class);
    assertEquals("user row should remain in the TeamDynamix target", 1, mockUserRows);

    // ...so both stay captured in the mirror (the target still has them, and both pass the active-
    // filtered re-read)
    assertEquals("group should STAY in the mirror (target still has it)", 1,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("user should STAY in the mirror (target still has it)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * Strict-native capture of orphan target objects (TeamDynamix analogue of Box's
   * testBoxFullSyncCapturesOrphanTargetEntities). With delete-types disabled, an orphan group +
   * orphan user that exist in the TeamDynamix target but are unknown to Grouper are still captured
   * into the mirror -- with NULL Grouper-side linkage (group_internal_id / member_internal_id) --
   * alongside Grouper's own testGroup + SUBJ0/SUBJ1, which keep their linkage populated. Also
   * asserts the orphan user's default capture attribute {@code userName} (from {@code /UserName})
   * round-trips through the reporting view, and that {@code id} is NOT captured as a group attribute
   * (it is already the target_group_id column).
   *
   * <p>GOTCHA vs Box/Datadog (why selectAllEntities=true is added): orphan capture depends on a
   * server-wide read that lists target objects Grouper does not know about. The Box and Datadog
   * reference configs set selectAllEntities=true in their shared setup, so their orphan-capture tests
   * inherit it; the shared TeamDynamix config instead sets selectAllEntities=false (so by default the
   * framework scoped-retrieves only Grouper-known entities and an orphan USER is never read -> never
   * captured -> 0 rows). We add selectAllEntities=true here so the daemon runs the server-wide
   * retrieveAllEntities listing and captures the orphan user. selectAllGroups already defaults to true
   * here (so the orphan GROUP is captured without help), but we set it explicitly for symmetry and to
   * make the orphan-capture precondition self-documenting. Delete-types stay off (setup default), so
   * neither orphan is removed during the run.
   */
  public void testTeamDynamixFullSyncCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    // selectAll*=true so the server-wide retrieveAll listing captures the target-only orphans (the
    // shared config sets selectAllEntities=false, under which orphan ENTITIES are never read -- see
    // the gotcha note in the javadoc). Delete-types stay disabled (setup default) so the orphans
    // persist across the run.
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllEntities", "true");
    extraConfig.put("selectAllGroups", "true");
    setupTeamDynamixSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the TeamDynamix mock before the provisioner runs.
    TeamDynamixGroup orphanGroup = saveOrphanTeamDynamixGroup(
        "orphan-tdx-group-1234", "orphanGroupNotInGrouper", "orphanDescription");
    TeamDynamixUser orphanUser = saveOrphanTeamDynamixUser(
        "orphan-tdx-user-5678", "orphan.user.not.in.grouper", "orphan.user@example.edu");

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

    // the orphan user's userName (a TeamDynamix default entity attribute, from /UserName)
    // round-trips through the reporting view (proves target-drift entities capture actual attrs)
    String orphanUserNameInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'userName'")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(String.class);
    assertEquals("orphan user's userName should round-trip through reporting",
        "orphan.user.not.in.grouper", orphanUserNameInReporting);

    // sanity: 'id' must NOT be captured as a group attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis (TeamDynamix analogue of Box's
   * testBoxFullSyncCapturesMembershipsFromOrphanGroup). An orphan group with an orphan member
   * (neither known to Grouper) is wired in the TeamDynamix mock (mock_teamdynamix_membership).
   * TeamDynamix memberships are group-centric, so when the daemon lists groups it also reads the
   * orphan group's members (retrieveMembershipsByGroup) -- that membership must land in
   * grouper_prov_mship alongside Grouper's own, proving strict-native membership capture is
   * independent of Grouper knowledge.
   *
   * <p>GOTCHA vs Box/Datadog (why selectAllEntities=true is added): the asserted orphan mship row is
   * found by joining grouper_prov_mship -> grouper_prov_user on the orphan USER's target_user_id, so
   * the orphan user must have a grouper_prov_user row. That row is populated only by the ENTITY read
   * path (retrieveAllEntities / retrieveEntity); the group's members read
   * (retrieveTeamDynamixUsersByGroup) captures the membership edge but NOT the user object. The shared
   * TeamDynamix config sets selectAllEntities=false (unlike Box/Datadog, which set it true), so by
   * default the orphan user -- unknown to Grouper -- is never read on the entity axis, its
   * grouper_prov_user row is absent, and the join yields 0. We add selectAllEntities=true so the
   * server-wide retrieveAllEntities listing captures the orphan user and the membership join resolves.
   * selectAllGroups already defaults to true (so the orphan group + its membership are read without
   * help); we set it explicitly for symmetry.
   */
  public void testTeamDynamixFullSyncCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    // selectAll*=true so the orphan user is captured on the entity axis (its grouper_prov_user row is
    // required for the prov_mship -> prov_user join below); the shared config's selectAllEntities=false
    // would leave the orphan user unread and the join empty -- see the gotcha note in the javadoc.
    // Delete-types stay disabled (setup default) so the orphan group + its membership persist.
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllEntities", "true");
    extraConfig.put("selectAllGroups", "true");
    setupTeamDynamixSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the TeamDynamix mock. The
    // membership FKs require the group and user rows to exist first
    // (mock_teamdynamix_mship_gid_fkey / _uid_fkey).
    TeamDynamixGroup orphanGroup = saveOrphanTeamDynamixGroup(
        "orphan-tdx-mship-group-1", "orphanGroupWithMembers", "orphanDescription");
    TeamDynamixUser orphanUser = saveOrphanTeamDynamixUser(
        "orphan-tdx-mship-user-1", "orphan.mship.user", "orphan.mship@example.edu");

    TeamDynamixMembership orphanMembership = new TeamDynamixMembership();
    orphanMembership.setId("orphan-tdx-mship-row-1");
    orphanMembership.setGroupId(orphanGroup.getId());
    orphanMembership.setUserId(orphanUser.getId());
    orphanMembership.setIsNotified(false);
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
   * !selectAll* scope excludes orphans (TeamDynamix analogue of Box's
   * testBoxSelectAllFalseExcludesOrphans). With selectAllGroups=false and selectAllEntities=false
   * the daemon fetches only the resources mapped to Grouper-provisioned objects (by id / name /
   * ExternalID via the scoped retrieve path), never a server-wide listing -- so an orphan
   * group/user that the TeamDynamix target has but Grouper does not must NOT land in the mirror.
   */
  public void testTeamDynamixSelectAllFalseExcludesOrphans() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    setupTeamDynamixSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only Grouper-known resources via scoped retrieve.
    TeamDynamixGroup orphanGroup = saveOrphanTeamDynamixGroup(
        "orphan-tdx-group-selnone-1", "orphanGroupSelectAllFalse", "orphanDescription");
    TeamDynamixUser orphanUser = saveOrphanTeamDynamixUser(
        "orphan-tdx-user-selnone-1", "orphan.selnone.user", "orphan.selnone@example.edu");

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
   * loadGroupsToGenericGrouperTable in isolation (TeamDynamix analogue of Box's
   * testBoxLoadGroupsFlagInIsolation). Only the groups flag is on -> only grouper_prov_group rows
   * are written; prov_user and prov_mship stay empty even though the daemon still reads users (for
   * provisioning) and memberships (for diffing).
   */
  public void testTeamDynamixLoadGroupsFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

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
   * loadEntitiesToGenericGrouperTable in isolation (TeamDynamix analogue of Box's
   * testBoxLoadEntitiesFlagInIsolation). Only the entities flag is on -> only grouper_prov_user
   * rows are written; prov_group and prov_mship stay empty.
   */
  public void testTeamDynamixLoadEntitiesFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

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
   * loadMembershipsToGenericGrouperTable off (TeamDynamix analogue of Box's
   * testBoxLoadMembershipsFlagOff). Both object loads on but memberships off -> prov_group and
   * prov_user populate, prov_mship stays empty. Proves the membership gate is independent of the
   * object gates.
   */
  public void testTeamDynamixLoadMembershipsFlagOff() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    TeamDynamixProvisionerTestUtils.configureProvisioner(new TeamDynamixProvisionerTestConfigInput()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_teamdynamix_user").executeSql();

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
   * INCREMENTAL sync-back coverage for TeamDynamix, conservative (TeamDynamix analogue of Box's
   * testBoxIncrementalSyncBackNoSpuriousDeletes). TeamDynamix captures memberships on the WRITE
   * path (TeamDynamixTargetDao.insert/deleteMemberships -> recordTargetNativeMembershipInsert/Delete,
   * like Adobe/SCIM) and captures group/user OBJECTS on the READ path; it has
   * canRetrieveGroup/Entity, so an incremental cycle re-reads only the changed objects (per-id reads
   * that fire the TeamDynamix object capture seams) and the incremental flush is a SCOPED upsert (it
   * does NOT full-replace, so it will not wrongly delete untouched mirror rows).
   *
   * <p>What this test asserts is deliberately narrow -- the safe, reliable part of TeamDynamix
   * incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
   * member drives an incremental that (a) does NOT shrink the existing GROUP mirror (no spurious
   * deletes -- the regression the scoped incremental flush guards against), and (b) captures the
   * newly added member's USER object into prov_user. It does NOT assert that the new MEMBERSHIP
   * converges on the same incremental cycle: even though TeamDynamix now captures memberships on the
   * write path (recordTargetNativeMembershipInsert/Delete, like Adobe/SCIM), the group-centric
   * scoped membership flush plus read-before-write timing on an incremental cycle makes same-cycle
   * membership convergence unreliable (the same 1-cycle-lag reason SCIM/Box disable membership
   * assertions on incremental). Membership convergence is covered end-to-end by the two-pass full
   * membership tests above.
   */
  public void testTeamDynamixIncrementalSyncBackNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myTeamDynamixProvisioner";
    setupTeamDynamixSyncBack(configId, null);

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
    // the TeamDynamix read-capture seams, and the scoped flush upserts -- it must NOT drop untouched
    // rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc). TeamDynamix
    // memberships now capture on the WRITE path (recordTargetNativeMembershipInsert/Delete, like
    // Adobe/SCIM), but they are group-centric and, on an incremental cycle, the scoped membership
    // flush plus read-before-write timing means testGroup's membership rows can transiently clear,
    // re-converging only on the next full sync (the same 1-cycle lag for which SCIM/Box disable
    // membership assertions on incremental). Membership convergence is covered end-to-end by the
    // two-pass full tests above; here we only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; TeamDynamix shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

}
