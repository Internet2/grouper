package edu.internet2.middleware.grouper.app.azure;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
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


public class GrouperAzureProvisionerTest extends GrouperTest {

  public static void main(String[] args) {
    //TestRunner.run(GrouperAzureProvisionerTest.class);
    TestRunner.run(new GrouperAzureProvisionerTest("testFullSyncAzure"));
  }
  
  public GrouperAzureProvisionerTest(String name) {
    super(name);
  }

  public GrouperAzureProvisionerTest() {
    
  }
  
  public static boolean startTomcat = false;
  
  public void testFullSyncAzure() {
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientId").value("myClient").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientSecret").value("pass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphEndpoint").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphVersion").value("v1.0").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupAttribute").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupValueFormat").value("${group.getName()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.loginEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/auth/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resource").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.tenantId").value("myTenant").store();
   
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.azureExternalSystemConfigId").value("myAzure").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.class").value("edu.internet2.middleware.grouper.app.azure.GrouperAzureProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.numberOfGroupAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectAllEntitiesDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectAllGroupsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectAllMembershipsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.selectMemberships").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.common.subjectLink.memberFromId2").value("${subject.getAttributeValue(\"email\")}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.subjectSourcesToProvision").value("jdbc").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.name").value("accountEnabled").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.translateExpression").value("${'true'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.name").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.name").value("mailNickname").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.name").value("userPrincipalName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.update").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetEntityAttribute.4.translateExpression").value("${gcGrouperSyncMember.memberFromId2}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.fieldName").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.1.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.name").value("mailEnabled").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.translateExpression").value("${'true'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.name").value("mailNickname").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.name").value("securityEnabled").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.translateExpression").value("${'true'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.targetGroupAttribute.4.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.updateGroups").value("true").store();
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
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
      
    }

    
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

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientId").value("myClient").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.clientSecret").value("pass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphEndpoint").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.graphVersion").value("v1.0").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupAttribute").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.groupLookupValueFormat").value("${group.getName()}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.loginEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/auth/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resource").value("https://graph.microsoft.com").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.resourceEndpoint").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/azure/").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.azureConnector.myAzure.tenantId").value("myTenant").store();
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
