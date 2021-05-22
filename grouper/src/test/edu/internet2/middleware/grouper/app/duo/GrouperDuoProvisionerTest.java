package edu.internet2.middleware.grouper.app.duo;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureApiCommands;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureGroup;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisionerTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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

public class GrouperDuoProvisionerTest extends GrouperTest {
  
  public static void main(String[] args) {
    //TestRunner.run(GrouperAzureProvisionerTest.class);
    TestRunner.run(new GrouperAzureProvisionerTest("testGroupCreateThenDownloadUuid"));
  }
  
  public GrouperDuoProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoProvisionerTest() {

  }
  
  /**
   * 
   */
  public void testGroupCreateThenDownloadUuid() {
    
    if (!tomcatRunTests()) {
      return;
    }
    /*
grouper.azureConnector.myAzure.clientId = myClient
grouper.azureConnector.myAzure.clientSecret = pass
grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
grouper.azureConnector.myAzure.graphVersion = v1.0
grouper.azureConnector.myAzure.groupLookupAttribute = displayName
grouper.azureConnector.myAzure.groupLookupValueFormat = ${group.getName()}
grouper.azureConnector.myAzure.loginEndpoint = http://localhost:8400/grouper/mockServices/azure/auth/
grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
grouper.azureConnector.myAzure.resourceEndpoint = http://localhost:8400/grouper/mockServices/azure/
grouper.azureConnector.myAzure.tenantId = myTenant

*/

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8400);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value("localhost:8400/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1H").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.myConnector.useSsl").value("true").store();
/*
provisioner.myAzureProvisioner.azureExternalSystemConfigId = myAzure
provisioner.myAzureProvisioner.class = edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner
provisioner.myAzureProvisioner.hasTargetGroupLink = true
provisioner.myAzureProvisioner.insertGroups = true
provisioner.myAzureProvisioner.logAllObjectsVerbose = true
provisioner.myAzureProvisioner.numberOfGroupAttributes = 2
provisioner.myAzureProvisioner.operateOnGrouperGroups = true
provisioner.myAzureProvisioner.selectGroups = true
provisioner.myAzureProvisioner.showAdvanced = true
provisioner.myAzureProvisioner.targetGroupAttribute.0.fieldName = id
provisioner.myAzureProvisioner.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.myAzureProvisioner.targetGroupAttribute.0.matchingId = true
provisioner.myAzureProvisioner.targetGroupAttribute.0.searchAttribute = true
provisioner.myAzureProvisioner.targetGroupAttribute.0.select = true
provisioner.myAzureProvisioner.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.myAzureProvisioner.targetGroupAttribute.0.valueType = string
provisioner.myAzureProvisioner.targetGroupAttribute.1.fieldName = displayName
provisioner.myAzureProvisioner.targetGroupAttribute.1.insert = true
provisioner.myAzureProvisioner.targetGroupAttribute.1.isFieldElseAttribute = true
provisioner.myAzureProvisioner.targetGroupAttribute.1.required = true
provisioner.myAzureProvisioner.targetGroupAttribute.1.select = true
provisioner.myAzureProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.myAzureProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = name
provisioner.myAzureProvisioner.targetGroupAttribute.1.valueType = string

     */
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.azureExternalSystemConfigId").value("myAzure").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.class").value("edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.numberOfGroupAttributes").value("2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.fieldName").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.valueType").value("string").store();

    GrouperStartup.startup();
    
    // start tomcat
    CommandLineExec commandLineExec = tomcatStart();
    
    try {
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_user").executeSql();
      
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
      attributeValue.setDoProvision("myAzureProvisioner");
      attributeValue.setTargetName("myAzureProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myAzureProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupToId2());
    } finally {
      tomcatStop();
      if (commandLineExec != null) {
        GrouperUtil.threadJoin(commandLineExec.getThread());
      }
    }
    
  }

}
