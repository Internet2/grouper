package edu.internet2.middleware.grouper.app.scim;

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
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimProvisioningStartWith;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperGithubProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperGithubProvisionerTest("testGithubFullSync"));

  }
  
  public static boolean startTomcat = false;
  
  @Override
  public String defaultConfigId() {
    return "githubProvisioner";
  }

  public GrouperGithubProvisionerTest(String name) {
    super(name);
  }
  
  public void testGithubIncrementalSync() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    Group usersToProvisionGroup = new GroupSave(grouperSession).assignName("test2:usersToProvisionGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    
    usersToProvisionGroup.addMember(testGroup.toSubject());
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(true);
    
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignSubjectLinkCache0("${subject.getAttributeValue('email')}")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(usersToProvisionGroup)
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignEntityAttribute4name("emailValue")
        );
        
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    

    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      //lets sync these over      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.addMember(SubjectTestHelper.SUBJ2);
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.delete();
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
  }
  
  public void testGithubFullSync() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    
    Group usersToProvisionGroup = new GroupSave(grouperSession).assignName("test2:usersToProvisionGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    usersToProvisionGroup.addMember(testGroup.toSubject());
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(true);
        
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignSubjectLinkCache0("${subject.getAttributeValue('email')}")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(usersToProvisionGroup)
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignEntityAttribute4name("emailValue")
        .addExtraConfig("entityAttributeValueCache0nullChecksInScript", "true")
        .addExtraConfig("entityAttributeValueCache0translationContinueCondition", "${subject != null}")
        );

    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

    //lets sync these over
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioningOutput = fullProvision();
      
      GrouperUtil.sleep(2000);

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      grouperProvisioningOutput = fullProvision();

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
  }
  public void testGithubTwoEmailsFullSync() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    
    Group usersToProvisionGroup = new GroupSave(grouperSession).assignName("test2:usersToProvisionGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    usersToProvisionGroup.addMember(testGroup.toSubject());
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(true);
        
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignUseFirstLastName(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(usersToProvisionGroup)
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignUseEmails(true)
        );
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

      //lets sync these over
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2User grouperScim2User = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).get(0);
      
      assertEquals(grouperScim2User.getEmailValue(), "test.subject.0@somewhere.someSchool.edu");
      assertEquals(grouperScim2User.getEmailValue2(), "test.subject.0@somewhere.someSchool.edu2");
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);
      
      grouperProvisioningOutput = fullProvision();
      
      GrouperUtil.sleep(2000);

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      grouperProvisioningOutput = fullProvision();

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
  }
  
  public void testFullSyncGithubStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      ScimProvisionerTestUtils.setupGithubExternalSystem(true);
      
      ScimProvisioningStartWith startWith = new ScimProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("bearerTokenExternalSystemConfigId", "githubExternalSystem"); 
      startWithSuffixToValue.put("scimPattern", "githubEntities");
      startWithSuffixToValue.put("scimType", "Github");
      startWithSuffixToValue.put("userAttributesType", "core");
      
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityEmailSubjectAttribute", "email");
      
      startWithSuffixToValue.put("subjectLastNameAttribute", "name");
      startWithSuffixToValue.put("subjectFirstNameAttribute", "name");
      startWithSuffixToValue.put("entityUsername", "subjectId");
      startWithSuffixToValue.put("entityDisplayName", "name");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("githubProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.githubProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_githubProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_githubProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_githubProvisioner.provisionerConfigId").value("githubProvisioner").store();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("githubProvisioner");
      attributeValue.setTargetName("githubProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
    
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("githubProvisioner");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.4");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(false);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(false);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(false);
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

  public void testGithubTwoEmailsIncremental() {
    if (!tomcatRunTests()) {
      return;
    }
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    
    Group usersToProvisionGroup = new GroupSave(grouperSession).assignName("test2:usersToProvisionGroup").save();
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(true);
        
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignUseFirstLastName(true)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(usersToProvisionGroup)
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignUseEmails(true)
        );
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    incrementalProvision();

    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    usersToProvisionGroup.addMember(testGroup.toSubject());
    

    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
  
      //lets sync these over
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      incrementalProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput(); 


      GrouperUtil.sleep(2000);
  
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2User grouperScim2User = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).get(0);
      
      assertEquals(grouperScim2User.getEmailValue(), "test.subject.0@somewhere.someSchool.edu");
      assertEquals(grouperScim2User.getEmailValue2(), "test.subject.0@somewhere.someSchool.edu2");
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);
      
      incrementalProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput(); 
      
      GrouperUtil.sleep(2000);
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      incrementalProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput(); 

  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
  }
  
  
  /**
   * 
   */
  public void testFullProvisionLoadEntitiesIntoScimUsersTable() {
    
    if (!tomcatRunTests()) {
      return;
    }
    
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    
    Group usersToProvisionGroup = new GroupSave(grouperSession).assignName("test2:usersToProvisionGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
    
    usersToProvisionGroup.addMember(testGroup.toSubject());
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(true);
    
    ScimProvisionerTestConfigInput scimProvisioningStrategy = new ScimProvisionerTestConfigInput().assignProvisioningStrategy("scimGithubOrgs");
    scimProvisioningStrategy.assignConfigId("githubProvisioner");
    scimProvisioningStrategy.assignGroupOfUsersToProvision(usersToProvisionGroup);
    scimProvisioningStrategy.addExtraConfig("loadEntitiesToGrouperTable", "true");
    ScimProvisionerTestUtils.configureScimProvisioner(scimProvisioningStrategy);

    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
      
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      new GcDbAccess().connectionName("grouper").sql("insert into mock_scim_user (active,cost_center,display_name,email_type,email_value,email_type2,email_value2,employee_number,external_id,family_name,formatted_name,given_name,id,middle_name,user_name,user_type,org,phone_type,phone_value,phone_type2,phone_value2,title,division,department,service_now_emp_num) values "
          + "('T',NULL,NULL,NULL,'test.subject.1@somewhere.someSchool.edu',NULL,NULL,NULL,NULL,'my name is test.subject.1',NULL,'my name is test.subject.1','f6c99c1cf021454dbfd24ebd80e29916',NULL,'test.subject.1',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'10000351')").executeSql();
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("githubProvisioner");
      attributeValue.setTargetName("githubProvisioner");
      attributeValue.setStemScopeString("sub");
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
 
//      assertTrue(1 <= grouperProvisioningOutput.getInsert());
//     
//      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_prov_duo_user").select(int.class));
//      
//      List<Object[]> results = new GcDbAccess().connectionName("grouper").sql("select config_id, user_id, aliases, phones, is_push_enabled, "
//          + " email, first_name, last_name, is_enrolled, last_directory_sync, notes, real_name, status, user_name, created_at, last_login_time from grouper_prov_duo_user").selectList(Object[].class);
//      
//      Object[] oneRowOfData = results.get(0);
//      
//      assertEquals("myDuoProvisioner", oneRowOfData[0]);
//      assertEquals("123abc", oneRowOfData[1]);
//      assertEquals("abc,test", oneRowOfData[2]);
//      assertEquals("123-456-7890", oneRowOfData[3]);
//      assertEquals("T", oneRowOfData[4]);
//      assertEquals("test.subject.0@test.com", oneRowOfData[5]);
//      assertEquals("first", oneRowOfData[6]);
//      assertEquals("last", oneRowOfData[7]);
//      assertEquals("T", oneRowOfData[8]);
//      assertTrue(BigDecimal.valueOf(72832323223L).equals(oneRowOfData[9]));
//      assertEquals("test notes", oneRowOfData[10]);
//      assertEquals("real name", oneRowOfData[11]);
//      assertEquals("active", oneRowOfData[12]);
//      assertEquals("user name", oneRowOfData[13]);
//      assertTrue(BigDecimal.valueOf(87877787878L).equals(oneRowOfData[14]));
//      assertTrue(BigDecimal.valueOf(78787777888L).equals(oneRowOfData[15]));
      
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }

  public void testGithubMultipleOrgsFull() {
    githubMultipleOrgs(true);
  }
  
  public void testGithubMultipleOrgsIncremental() {
    githubMultipleOrgs(false);
  }
  
  public void githubMultipleOrgs(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:orgName").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:orgName2").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    testGroup2.addMember(SubjectTestHelper.SUBJ1, false);
    testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
    
    ScimProvisionerTestUtils.setupGithubExternalSystem(false);
    
    ScimProvisionerTestConfigInput scimProvisioningStrategy = new ScimProvisionerTestConfigInput().assignProvisioningStrategy("scimGithubOrgs");
    scimProvisioningStrategy.assignConfigId("githubProvisioner");
    ScimProvisionerTestUtils.configureScimProvisioner(scimProvisioningStrategy);
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("githubProvisioner");
    attributeValue.setTargetName("githubProvisioner");
    attributeValue.setStemScopeString("sub");
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null, "orgName");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
  
      //lets sync these over
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
  
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(4, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioningOutput = fullProvision();
      
      GrouperUtil.sleep(2000);
  
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      grouperProvisioningOutput = fullProvision();
  
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
  }
  
}
