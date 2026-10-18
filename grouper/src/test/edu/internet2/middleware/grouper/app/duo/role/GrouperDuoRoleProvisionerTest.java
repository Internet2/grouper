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
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.subject.Subject;
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
      //this will create tables
      List<GrouperDuoRoleUser> grouperDuoRoleUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
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

  /**
   * role values resolve to Duo role_id: built-in ids, built-in and custom names (case insensitive),
   * legacy aliases, unknown values pass through, and the highest priority role wins
   */
  public void testDuoRoleResolver() {
    
    GrouperDuoRoleResolver grouperDuoRoleResolver = new GrouperDuoRoleResolver(null, 
        "{\"Tier 2 Support\": 350, \"RLMOCKAUDITOR0000001\": 750, \"billing\": 50}");
    // custom roles like GET /admin/v1/admin_roles would return them
    grouperDuoRoleResolver.assignCustomRoles(GrouperUtil.toMap("RLMOCKTIER2SUPPORT01", "Tier 2 Support", "RLMOCKAUDITOR0000001", "Auditor"));
    
    // resolve to role ids
    assertNull(grouperDuoRoleResolver.resolveRoleId(" "));
    assertEquals("help_desk", grouperDuoRoleResolver.resolveRoleId("help_desk"));
    assertEquals("help_desk", grouperDuoRoleResolver.resolveRoleId("Help Desk"));
    assertEquals("help_desk", grouperDuoRoleResolver.resolveRoleId("help desk"));
    assertEquals("service_manager", grouperDuoRoleResolver.resolveRoleId("Administrator"));
    assertEquals("read_only", grouperDuoRoleResolver.resolveRoleId("read-only"));
    assertEquals("application_manager", grouperDuoRoleResolver.resolveRoleId("integration_manager"));
    assertEquals("RLMOCKTIER2SUPPORT01", grouperDuoRoleResolver.resolveRoleId("tier 2 support"));
    assertEquals("RLMOCKTIER2SUPPORT01", grouperDuoRoleResolver.resolveRoleId("RLMOCKTIER2SUPPORT01"));
    // not known, sent to Duo as a role id
    assertEquals("RLUNKNOWN", grouperDuoRoleResolver.resolveRoleId("RLUNKNOWN"));
    
    // priorities: built-ins, config by name, config by id, config override of a built-in, unranked custom
    assertEquals(800, grouperDuoRoleResolver.priority("owner"));
    assertEquals(700, grouperDuoRoleResolver.priority("service_manager"));
    assertEquals(100, grouperDuoRoleResolver.priority("read_only"));
    assertEquals(350, grouperDuoRoleResolver.priority("RLMOCKTIER2SUPPORT01"));
    assertEquals(750, grouperDuoRoleResolver.priority("RLMOCKAUDITOR0000001"));
    assertEquals(50, grouperDuoRoleResolver.priority("billing"));
    assertEquals(GrouperDuoRoleResolver.DEFAULT_CUSTOM_ROLE_PRIORITY, grouperDuoRoleResolver.priority("RLUNKNOWN"));
    
    // pick the highest priority
    assertNull(grouperDuoRoleResolver.pickHighestPriorityRoleId(null));
    assertEquals("owner", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("Owner", "Help Desk")));
    assertEquals("RLMOCKTIER2SUPPORT01", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("Help Desk", "Tier 2 Support")));
    assertEquals("RLMOCKAUDITOR0000001", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("Administrator", "Auditor")));
    // unranked custom role only beats read_only
    assertEquals("help_desk", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("help_desk", "RLUNKNOWN")));
    assertEquals("RLUNKNOWN", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("read_only", "RLUNKNOWN")));
    // billing is overridden below read_only
    assertEquals("read_only", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("billing", "read_only")));
    // ties go to the alphabetically first role id
    assertEquals("RLAAA", grouperDuoRoleResolver.pickHighestPriorityRoleId(GrouperUtil.toSet("RLBBB", "RLAAA")));
    
    // rolePriority config validation
    assertEquals(0, GrouperDuoRoleResolver.parseRolePriorityJson("").size());
    for (String invalidJson : new String[] {"not json", "[350]", "{\"Help Desk\": \"high\"}", "{\"Help Desk\": 3.5}"}) {
      try {
        GrouperDuoRoleResolver.parseRolePriorityJson(invalidJson);
        fail("should be invalid: " + invalidJson);
      } catch (RuntimeException re) {
        // good
      }
    }
  }
  
  /**
   * mark a group to be provisioned to Duo with a role
   * @param group
   * @param role
   */
  private void duoRoleProvisionGroup(Group group, String role) {
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myDuoRoleProvisioner");
    attributeValue.setTargetName("myDuoRoleProvisioner");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_duoRoles", role);
    attributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group);
  }
  
  /**
   * set the Duo admin email of a subject
   * @param grouperSession
   * @param subject
   * @param email
   */
  private void duoRoleAssignEmail(GrouperSession grouperSession, Subject subject, String email) {
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDoProvision("myDuoRoleProvisioner");
    attributeValue.setTargetName("myDuoRoleProvisioner");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_duoEmail", email);
    attributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, 
        MemberFinder.findBySubject(grouperSession, subject, true));
  }
  
  /**
   * @param email
   * @return the role id the mock Duo has for this admin
   */
  private String duoRoleMockRoleId(String email) {
    return new GcDbAccess().connectionName("grouper").sql("select role from mock_duo_role_user where email = ?")
        .addBindVar(email).select(String.class);
  }
  
  /**
   * role comes from the group display extension like PennGroups does it: names in any case and custom
   * role names are sent as role_id, a resync makes no changes, and rolePriority decides between role groups
   */
  public void testFullProvisionRoleIdCustomRolesAndPriority() {
    
    if (!tomcatRunTests()) {
      return;
    }

    // role from the group display extension instead of metadata
    DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(new DuoRoleProvisionerTestConfigInput()
        .addExtraConfig("targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "displayExtension")
        .addExtraConfig("targetGroupAttribute.0.translateExpression", ""));

    GrouperStartup.startup();
    
    if (startTomcat) {
      tomcatStart();
    }
    
    // this will create tables
    GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
    new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    new StemSave(grouperSession).assignName("test").save();
    
    // lower case built-in name, built-in name, custom role name
    Group adminGroup = new GroupSave(grouperSession).assignName("test:adminGroup").assignDisplayExtension("administrator").save();
    Group helpDeskGroup = new GroupSave(grouperSession).assignName("test:helpDeskGroup").assignDisplayExtension("Help Desk").save();
    Group tier2Group = new GroupSave(grouperSession).assignName("test:tier2Group").assignDisplayExtension("Tier 2 Support").save();
    
    adminGroup.addMember(SubjectTestHelper.SUBJ0, false);
    helpDeskGroup.addMember(SubjectTestHelper.SUBJ1, false);
    helpDeskGroup.addMember(SubjectTestHelper.SUBJ2, false);
    
    duoRoleProvisionGroup(adminGroup, "Administrator");
    duoRoleProvisionGroup(helpDeskGroup, "Help Desk");
    duoRoleProvisionGroup(tier2Group, "Read-only");
    
    duoRoleAssignEmail(grouperSession, SubjectTestHelper.SUBJ0, "test.subject.0@grouper.com");
    duoRoleAssignEmail(grouperSession, SubjectTestHelper.SUBJ1, "test.subject.1@grouper.com");
    duoRoleAssignEmail(grouperSession, SubjectTestHelper.SUBJ2, "test.subject.2@grouper.com");
    
    fullProvision();
    
    assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
    // Administrator is role_id service_manager in Duo
    assertEquals("service_manager", duoRoleMockRoleId("test.subject.0@grouper.com"));
    assertEquals("help_desk", duoRoleMockRoleId("test.subject.1@grouper.com"));
    assertEquals("help_desk", duoRoleMockRoleId("test.subject.2@grouper.com"));
    
    // names in grouper and role ids from Duo should compare equal, so a resync changes nothing
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    assertEquals(0, grouperProvisioningOutput.getInsert());
    assertEquals(0, grouperProvisioningOutput.getUpdate());
    assertEquals(0, grouperProvisioningOutput.getDelete());
    
    // in help desk and an unranked custom role: help desk (300) beats the custom default (150)
    tier2Group.addMember(SubjectTestHelper.SUBJ2, false);
    fullProvision();
    assertEquals("help_desk", duoRoleMockRoleId("test.subject.2@grouper.com"));
    
    // rank the custom role (by name) above help desk
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoRoleProvisioner.rolePriority")
      .value("{\"Tier 2 Support\": 350}").store();
    ConfigPropertiesCascadeBase.clearCache();
    fullProvision();
    assertEquals("RLMOCKTIER2SUPPORT01", duoRoleMockRoleId("test.subject.2@grouper.com"));
    
    // only in the custom role
    helpDeskGroup.deleteMember(SubjectTestHelper.SUBJ2);
    fullProvision();
    assertEquals("RLMOCKTIER2SUPPORT01", duoRoleMockRoleId("test.subject.2@grouper.com"));
    assertEquals("help_desk", duoRoleMockRoleId("test.subject.1@grouper.com"));
    assertEquals("service_manager", duoRoleMockRoleId("test.subject.0@grouper.com"));
    
    // fail closed: move the Administrator to a role Duo rejects (like a custom role name that could not
    // be resolved), they should end up read_only instead of keeping the higher role
    Group badRoleGroup = new GroupSave(grouperSession).assignName("test:badRoleGroup").assignDisplayExtension("Not A Duo Role").save();
    duoRoleProvisionGroup(badRoleGroup, "Read-only");
    badRoleGroup.addMember(SubjectTestHelper.SUBJ0, false);
    adminGroup.deleteMember(SubjectTestHelper.SUBJ0);
    grouperProvisioningOutput = fullProvision("myDuoRoleProvisioner", true);
    assertEquals("read_only", duoRoleMockRoleId("test.subject.0@grouper.com"));
  }
}
