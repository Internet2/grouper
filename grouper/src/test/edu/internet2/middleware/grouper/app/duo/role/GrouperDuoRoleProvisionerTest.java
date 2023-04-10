package edu.internet2.middleware.grouper.app.duo.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.duo.DuoProvisioningStartWith;
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
import junit.textui.TestRunner;

public class GrouperDuoRoleProvisionerTest extends GrouperProvisioningBaseTest {

  public GrouperDuoRoleProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoRoleProvisionerTest() {

  }

  @Override
  public String defaultConfigId() {
    return "myDuoRoleProvisioner";
  }

  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new GrouperDuoRoleProvisionerTest("testFullSyncDuoRoleStartWithAndDiagnostics"));
  }

  public void setUp() {
    super.setUp();

    DuoRoleProvisionerTestUtils.setupDuoRoleExternalSystem();
    
    try {
      GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();  
    } catch (Exception e) {
      
    }
    
  }
  
  public static boolean startTomcat = false;
  
  /**
   * 
   */
  public void testFullProvisionInsertUpdateDeleteAdministrator() {
    
    if (!tomcatRunTests()) {
      return;
    }

    DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(new DuoRoleProvisionerTestConfigInput());

    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoRoleUser> grouperDuoRoleUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      testGroup.addMember(SubjectTestHelper.SUBJ4, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.0@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true));
      
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.1@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true));
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDuoRoleProvisioner");
      assertEquals(3, gcGrouperSync.getUserCount().intValue());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioningOutput = fullProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
//      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  
  public void testFullSyncDuoRoleStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      DuoRoleProvisionerTestUtils.setupDuoRoleExternalSystem();
      
      DuoRoleProvisioningStartWith startWith = new DuoRoleProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("duoExternalSystemConfigId", "duo1");
      startWithSuffixToValue.put("duoRolePattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("roleAttributeValue", "script");
      startWithSuffixToValue.put("roleTranslationScript", "grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_duoRoles')");
      startWithSuffixToValue.put("manageEntities", "true");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityNameSubjectAttribute", "name");
      startWithSuffixToValue.put("entityEmailSubjectAttribute", "script");
      startWithSuffixToValue.put("entityEmailTranslationScript", "grouperProvisioningEntity.retrieveAttributeValueString('md_grouper_duoEmail')");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myDuoRoleProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myDuoRoleProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoRoleProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoRoleProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoRoleProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoRoleProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myDuoRoleProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myDuoRoleProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myDuoRoleProvisioner.provisionerConfigId").value("myDuoRoleProvisioner").store();
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.0@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true));
      
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.1@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true));
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDuoRoleProvisioner");
      assertEquals(2, gcGrouperSync.getUserCount().intValue());
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myDuoRoleProvisioner");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.0");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
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
  
  public void testIncrementalProvisionInsertUpdateDeleteAdministrator() {
    
    if (!tomcatRunTests()) {
      return;
    }

    DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(new DuoRoleProvisionerTestConfigInput());
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoRoleUser> grouperDuoRoleUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
//  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      incrementalProvision();

      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      testGroup.addMember(SubjectTestHelper.SUBJ4, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.0@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true));
      
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoEmail", "test.subject.1@grouper.com");
      attributeValue.setMetadataNameValues(metadataNameValues);
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
          MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true));
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
//      
      incrementalProvision();
      
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
}
