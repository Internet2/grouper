package edu.internet2.middleware.grouper.app.messagingProvisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxGroup;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxMembership;
import edu.internet2.middleware.grouper.app.boxProvisioner.GrouperBoxUser;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
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

public class GrouperMessagingProvisionerTest extends GrouperProvisioningBaseTest {
  
  public static boolean startTomcat = false;
  
  public GrouperMessagingProvisionerTest(String name) {
    super(name);
  }

  @Override
  public String defaultConfigId() {
    return "myMessagingProvisioner";
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperMessagingProvisionerTest("testIncrementalSyncMessagingStartWithAndDiagnostics"));
  }
  
  public void testIncrementalSyncMessagingWithBuiltinMessaging() {
    
    if (!tomcatRunTests()) {
      return;
    }
    
    MessagingProvisionerTestUtils.configureMessagingProvisioner(new MessagingProvisionerTestConfigInput());
    GrouperStartup.startup();

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      
      try {        
        new GcDbAccess().connectionName("grouper").sql("delete from grouper_message").executeSql();
      } catch (Exception e) {
        // TODO: handle exception
      }

      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
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
      attributeValue.setDoProvision("myMessagingProvisioner");
      attributeValue.setTargetName("myMessagingProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      assertEquals(new Integer(5), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      // update group description and it should generate one message
      testGroup = new GroupSave(grouperSession).assignDescription("new description")
          .assignName(testGroup.getName()+"New")
          .assignUuid(testGroup.getUuid()).assignSaveMode(SaveMode.UPDATE).save();
      
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      assertEquals(new Integer(6), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one member remove, one membership remove
      assertEquals(new Integer(8), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one member remove, one membership remove
      // (2) one member add, one membership add
      assertEquals(new Integer(10), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now delete the group and sync again
      testGroup.delete();
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one membership remove, one member remove
      // (2) one member add, one membership add
      // (5) two membership remove, two member remove, one group delete
      assertEquals(new Integer(15), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  public void testIncrementalSyncMessagingStartWithAndDiagnostics() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {        
      new GcDbAccess().connectionName("grouper").sql("delete from grouper_message").executeSql();
    } catch (Exception e) {
      // TODO: handle exception
    }
    
    try {
      
      MessagingProvisioningStartWith startWith = new MessagingProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("messagingType", "Grouper_Builtin");
      startWithSuffixToValue.put("queueType", "queue");
      startWithSuffixToValue.put("queueOrTopicName", "test");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myMessagingProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myMessagingProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myMessagingProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myMessagingProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myMessagingProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myMessagingProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myMessagingProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myMessagingProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myMessagingProvisioner.provisionerConfigId").value("myMessagingProvisioner").store();
            
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myMessagingProvisioner.class").value(EsbConsumer.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myMessagingProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myMessagingProvisioner.provisionerConfigId").value("myMessagingProvisioner").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myMessagingProvisioner.publisher.class").value(ProvisioningConsumer.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myMessagingProvisioner.publisher.debug").value("true").store();
    
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
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
      attributeValue.setDoProvision("myMessagingProvisioner");
      attributeValue.setTargetName("myMessagingProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      assertEquals(new Integer(5), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      // update group description and it should generate one message
      testGroup = new GroupSave(grouperSession).assignDescription("new description")
          .assignName(testGroup.getName()+"New")
          .assignUuid(testGroup.getUuid()).assignSaveMode(SaveMode.UPDATE).save();
      
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      assertEquals(new Integer(6), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one member remove, one membership remove
      assertEquals(new Integer(8), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one member remove, one membership remove
      // (2) one member add, one membership add
      assertEquals(new Integer(10), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now delete the group and sync again
      testGroup.delete();
      incrementalProvision();
      
      // (5) one group add, two members add, two memberships add
      // (1) one group update
      // (2) one membership remove, one member remove
      // (2) one member add, one membership add
      // (5) two membership remove, two member remove, one group delete
      assertEquals(new Integer(15), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myMessagingProvisioner");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.4");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
//      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(true);
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
