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

}
