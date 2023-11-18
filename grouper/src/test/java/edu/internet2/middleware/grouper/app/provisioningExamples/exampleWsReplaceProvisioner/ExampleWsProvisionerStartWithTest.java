package edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioningExamples.exampleWsReplaceProvisioner.ExampleWsProvisioningStartWith;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class ExampleWsProvisionerStartWithTest extends GrouperProvisioningBaseTest {
  
  @Override
  public String defaultConfigId() {
    return "exampleWsProvTest";
  }
  
  public static void main(String[] args) {

    GrouperStartup.startup();
    TestRunner.run(new ExampleWsProvisionerStartWithTest("testFullExampleWsProvisionerWithStartWith"));
  
  }

  public ExampleWsProvisionerStartWithTest() {
    super();
  }
  
  public ExampleWsProvisionerStartWithTest(String name) {
    super(name);
  }
  
  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  @Override
  protected void setUp() {
    super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
      new GcDbAccess().sql("delete from mock_example_ws").executeSql();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }
  
  
  public void testFullExampleWsProvisionerWithStartWith() {
    
    // junit will clear all the grouper tables
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.exampleWsExternalSystem.myExampleExternalSystem1.endpointPrefix").value("http://localhost:8080/grouper/mockServices/exampleWs").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.exampleWsExternalSystem.myExampleExternalSystem1.password").value("123456").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.exampleWsExternalSystem.myExampleExternalSystem1.userName").value("testUserName").store();
    
    ExampleWsProvisioningStartWith startWith = new ExampleWsProvisioningStartWith();
    
    Map<String, String> startWithSuffixToValue = new HashMap<>();
    
    startWithSuffixToValue.put("exampleWsExternalSystemConfigId", "myExampleExternalSystem1");
    startWithSuffixToValue.put("exampleWsSource", "testSource");
    startWithSuffixToValue.put("groupTranslation", "extension");
    startWithSuffixToValue.put("entityTranslation", "subjectIdentifier0");
    
    Map<String, Object> provisionerSuffixToValue = new HashMap<>();
    
    startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
    
    startWith.manipulateProvisionerConfigurationValue("exampleWsProvTest", startWithSuffixToValue, provisionerSuffixToValue);
    
    for (String key: provisionerSuffixToValue.keySet()) {
      new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner.exampleWsProvTest."+key)
        .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
    }
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.exampleWsProvTest.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.exampleWsProvTest.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.exampleWsProvTest.logCommandsAlways").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_exampleWsProvTest.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_exampleWsProvTest.quartzCron").value("9 59 23 31 12 ? 2099").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_exampleWsProvTest.provisionerConfigId").value("exampleWsProvTest").store();
    
    Stem stem = new StemSave(this.grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(this.grouperSession).assignName("test:testGroup").save();
    
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("exampleWsProvTest");
    attributeValue.setTargetName("exampleWsProvTest");
    attributeValue.setStemScopeString("sub");
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    //lets sync these over
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
    
    List<Object[]> result = new GcDbAccess().connectionName("grouper").sql("select group_name, net_id, source from mock_example_ws").selectList(Object[].class);
    
    assertEquals(2, result.size());
    Set<String> netIds = new HashSet<>();
    
    for (Object[] row: result) {
      assertEquals("testGroup", GrouperUtil.stringValue(row[0]));
      netIds.add(GrouperUtil.stringValue(row[1]));
      assertEquals("testSource", GrouperUtil.stringValue(row[2]));
    }
    
    assertEquals(true, netIds.contains("id.test.subject.1"));
    assertEquals(true, netIds.contains("id.test.subject.0"));
    
  }


}
