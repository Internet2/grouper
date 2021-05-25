package edu.internet2.middleware.grouper.app.duo;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureApiCommands;
import edu.internet2.middleware.grouper.app.azure.GrouperAzureGroup;
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
    TestRunner.run(new GrouperDuoProvisionerTest("testDuoMembershipCrud"));

  }
  
  public GrouperDuoProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoProvisionerTest() {

  }
  
  public static boolean startTomcat = false;
  
  public void testDuoGroupCrud() {
    if (!tomcatRunTests()) {
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1HH").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N55").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.setName("test name");
    
    GrouperDuoGroup duoGroup = GrouperDuoApiCommands.createDuoGroup("duo1", grouperDuoGroup);
    assertEquals("test name", duoGroup.getName());
    assertNotNull(duoGroup.getGroup_id());
    assertTrue(StringUtils.isBlank(duoGroup.getDesc()));
    
    grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.setGroup_id(duoGroup.getGroup_id());
    grouperDuoGroup.setName("new test name");
    grouperDuoGroup.setDesc("new desc");
    
    duoGroup = GrouperDuoApiCommands.updateDuoGroup("duo1", grouperDuoGroup);
    assertEquals("new test name", duoGroup.getName());
    assertNotNull(duoGroup.getGroup_id());
    assertEquals("new desc", duoGroup.getDesc());
    
    //retrieve single group
    duoGroup = GrouperDuoApiCommands.retrieveDuoGroup("duo1", duoGroup.getGroup_id());
    assertEquals("new test name", duoGroup.getName());
    assertNotNull(duoGroup.getGroup_id());
    assertEquals("new desc", duoGroup.getDesc());
    
    //delete single one
    GrouperDuoApiCommands.deleteDuoGroup("duo1", duoGroup.getGroup_id());
    
    //verify it's deleted
    duoGroup = GrouperDuoApiCommands.retrieveDuoGroup("duo1", duoGroup.getGroup_id());
    assertNull(duoGroup);
    
    //create more than 100 so we can test internal pagination
    for (int i=0; i<200; i++) {
      grouperDuoGroup = new GrouperDuoGroup();
      grouperDuoGroup.setName("test name "+i);
      duoGroup = GrouperDuoApiCommands.createDuoGroup("duo1", grouperDuoGroup);
    }
    
    List<GrouperDuoGroup> duoGroups = GrouperDuoApiCommands.retrieveDuoGroups("duo1");
    assertEquals(200, duoGroups.size());
    //now delete all of them
    for (GrouperDuoGroup duoGroup1: duoGroups) {
      GrouperDuoApiCommands.deleteDuoGroup("duo1", duoGroup1.getGroup_id());
    }
    
    duoGroups = GrouperDuoApiCommands.retrieveDuoGroups("duo1");
    assertEquals(0, duoGroups.size());
    
  }
  
  public void testDuoUserCrud() {
    if (!tomcatRunTests()) {
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1HH").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N55").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    grouperDuoUser.setEmail("test@example.com");
    grouperDuoUser.setUserName("username");
    grouperDuoUser.setFirstName("first");
    grouperDuoUser.setLastName("last");
    
    GrouperDuoUser duoUser = GrouperDuoApiCommands.createDuoUser("duo1", grouperDuoUser);
    assertEquals("username", duoUser.getUserName());
    assertEquals("first", duoUser.getFirstName());
    assertEquals("last", duoUser.getLastName());
    assertNotNull(duoUser.getId());
    assertTrue(StringUtils.isBlank(duoUser.getRealName()));
    
    grouperDuoUser = new GrouperDuoUser();
    grouperDuoUser.setId(duoUser.getId());
    grouperDuoUser.setEmail("test1@example.com");
    grouperDuoUser.setUserName("username1");
    grouperDuoUser.setFirstName("first1");
    grouperDuoUser.setLastName("last1");
    
    duoUser = GrouperDuoApiCommands.updateDuoUser("duo1", grouperDuoUser);
    assertEquals("username1", duoUser.getUserName());
    assertEquals("first1", duoUser.getFirstName());
    assertEquals("last1", duoUser.getLastName());
    assertNotNull(duoUser.getId());
    assertTrue(StringUtils.isBlank(duoUser.getRealName()));
    
    //retrieve single user by id
    duoUser = GrouperDuoApiCommands.retrieveDuoUser("duo1", duoUser.getId());
    assertEquals("username1", duoUser.getUserName());
    assertEquals("first1", duoUser.getFirstName());
    assertEquals("last1", duoUser.getLastName());
    assertNotNull(duoUser.getId());
    assertTrue(StringUtils.isBlank(duoUser.getRealName()));
    
    //retrieve single user by username
    duoUser = GrouperDuoApiCommands.retrieveDuoUserByName("duo1", duoUser.getUserName());
    assertEquals("username1", duoUser.getUserName());
    assertEquals("first1", duoUser.getFirstName());
    assertEquals("last1", duoUser.getLastName());
    assertNotNull(duoUser.getId());
    assertTrue(StringUtils.isBlank(duoUser.getRealName()));
    
    //delete single one
    GrouperDuoApiCommands.deleteDuoUser("duo1", duoUser.getId());
    
    //verify it's deleted
    duoUser = GrouperDuoApiCommands.retrieveDuoUser("duo1", duoUser.getId());
    assertNull(duoUser);
    
    //create more than 100 so we can test internal pagination
    for (int i=0; i<200; i++) {
      grouperDuoUser = new GrouperDuoUser();
      grouperDuoUser.setEmail("test"+i+"@example.com");
      grouperDuoUser.setUserName("username"+i);
      grouperDuoUser.setFirstName("first"+i);
      grouperDuoUser.setLastName("last"+i);
      
      duoUser = GrouperDuoApiCommands.createDuoUser("duo1", grouperDuoUser);
    }
    
    List<GrouperDuoUser> duoUsers = GrouperDuoApiCommands.retrieveDuoUsers("duo1");
    assertEquals(200, duoUsers.size());
    //now delete all of them
    for (GrouperDuoUser duoUser1: duoUsers) {
      GrouperDuoApiCommands.deleteDuoUser("duo1", duoUser1.getId());
    }
    
    duoUsers = GrouperDuoApiCommands.retrieveDuoUsers("duo1");
    assertEquals(0, duoUsers.size());
    
  }
  
  public void testDuoMembershipCrud() {
    if (!tomcatRunTests()) {
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1HH").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N55").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    grouperDuoUser.setEmail("test@example.com");
    grouperDuoUser.setUserName("username");
    grouperDuoUser.setFirstName("first");
    grouperDuoUser.setLastName("last");
    
    GrouperDuoUser duoUser = GrouperDuoApiCommands.createDuoUser("duo1", grouperDuoUser);
    
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.setName("test name");
    
    GrouperDuoGroup duoGroup = GrouperDuoApiCommands.createDuoGroup("duo1", grouperDuoGroup);
    
    GrouperDuoApiCommands.associateUserToGroup("duo1", duoUser.getId(), duoGroup.getGroup_id());
    
    GrouperDuoUser duoUser1 = GrouperDuoApiCommands.retrieveDuoUser("duo1", duoUser.getId());
    assertEquals(1, duoUser1.getGroups().size());

    assertEquals("test name", duoUser1.getGroups().iterator().next().getName());
    
    List<GrouperDuoGroup> groupsByUser = GrouperDuoApiCommands.retrieveDuoGroupsByUser("duo1", duoUser.getId());
    
    assertEquals(1, groupsByUser.size());

    assertEquals("test name", groupsByUser.iterator().next().getName());
    
    //now disassociate
    GrouperDuoApiCommands.disassociateUserFromGroup("duo1", duoUser.getId(), duoGroup.getGroup_id());
    
    groupsByUser = GrouperDuoApiCommands.retrieveDuoGroupsByUser("duo1", duoUser.getId());
    
    assertEquals(0, groupsByUser.size());
    
    //delete user and group
    GrouperDuoApiCommands.deleteDuoUser("duo1", duoUser.getId());
    GrouperDuoApiCommands.deleteDuoGroup("duo1", duoGroup.getGroup_id());
    
  }
  
  
  /**
   * 
   */
  public void testGroupCreateThenDownloadUuid() {
    
    if (!tomcatRunTests()) {
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value(domainName+":"+port+"/grouper/mockServices/duo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1HH").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N55").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();

    //TODO change following properties
    /**
     * provisioner.myDuoProvisioner.class = edu.internet2.middleware.grouper.app.duo.GrouperDuoProvisioner
provisioner.myDuoProvisioner.debugLog = true
provisioner.myDuoProvisioner.deleteGroups = true
provisioner.myDuoProvisioner.deleteGroupsIfNotExistInGrouper = true
provisioner.myDuoProvisioner.deleteMemberships = true
provisioner.myDuoProvisioner.deleteMembershipsIfNotExistInGrouper = true
provisioner.myDuoProvisioner.duoExternalSystemConfigId = duo1
provisioner.myDuoProvisioner.hasTargetEntityLink = true
provisioner.myDuoProvisioner.hasTargetGroupLink = true
provisioner.myDuoProvisioner.insertEntities = true
provisioner.myDuoProvisioner.insertGroups = true
provisioner.myDuoProvisioner.insertMemberships = true
provisioner.myDuoProvisioner.logAllObjectsVerbose = true
provisioner.myDuoProvisioner.numberOfEntityAttributes = 2
provisioner.myDuoProvisioner.numberOfGroupAttributes = 3
provisioner.myDuoProvisioner.operateOnGrouperEntities = true
provisioner.myDuoProvisioner.operateOnGrouperGroups = true
provisioner.myDuoProvisioner.operateOnGrouperMemberships = true
provisioner.myDuoProvisioner.provisioningType = membershipObjects
provisioner.myDuoProvisioner.selectEntities = true
provisioner.myDuoProvisioner.selectGroups = true
provisioner.myDuoProvisioner.selectMemberships = true
provisioner.myDuoProvisioner.showAdvanced = true
provisioner.myDuoProvisioner.subjectSourcesToProvision = jdbc,vivekSource
provisioner.myDuoProvisioner.targetEntityAttribute.0.fieldName = loginId
provisioner.myDuoProvisioner.targetEntityAttribute.0.insert = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.isFieldElseAttribute = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.matchingId = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.searchAttribute = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.select = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.translateExpressionType = grouperProvisioningEntityField
provisioner.myDuoProvisioner.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField = subjectId
provisioner.myDuoProvisioner.targetEntityAttribute.0.update = true
provisioner.myDuoProvisioner.targetEntityAttribute.0.valueType = string
provisioner.myDuoProvisioner.targetEntityAttribute.1.fieldName = id
provisioner.myDuoProvisioner.targetEntityAttribute.1.isFieldElseAttribute = true
provisioner.myDuoProvisioner.targetEntityAttribute.1.select = true
provisioner.myDuoProvisioner.targetEntityAttribute.1.translateToMemberSyncField = memberToId2
provisioner.myDuoProvisioner.targetEntityAttribute.1.valueType = string
provisioner.myDuoProvisioner.targetGroupAttribute.0.fieldName = id
provisioner.myDuoProvisioner.targetGroupAttribute.0.isFieldElseAttribute = true
provisioner.myDuoProvisioner.targetGroupAttribute.0.select = true
provisioner.myDuoProvisioner.targetGroupAttribute.0.translateToGroupSyncField = groupToId2
provisioner.myDuoProvisioner.targetGroupAttribute.0.valueType = string
provisioner.myDuoProvisioner.targetGroupAttribute.1.fieldName = name
provisioner.myDuoProvisioner.targetGroupAttribute.1.insert = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.isFieldElseAttribute = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.matchingId = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.searchAttribute = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.select = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.translateExpressionType = grouperProvisioningGroupField
provisioner.myDuoProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField = extension
provisioner.myDuoProvisioner.targetGroupAttribute.1.update = true
provisioner.myDuoProvisioner.targetGroupAttribute.1.valueType = string
provisioner.myDuoProvisioner.targetGroupAttribute.2.insert = true
provisioner.myDuoProvisioner.targetGroupAttribute.2.isFieldElseAttribute = false
provisioner.myDuoProvisioner.targetGroupAttribute.2.name = description
provisioner.myDuoProvisioner.targetGroupAttribute.2.select = true
provisioner.myDuoProvisioner.targetGroupAttribute.2.translateExpressionType = grouperProvisioningGroupField
provisioner.myDuoProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField = attribute__description
provisioner.myDuoProvisioner.targetGroupAttribute.2.update = true
provisioner.myDuoProvisioner.targetGroupAttribute.2.valueType = string
provisioner.myDuoProvisioner.updateEntities = true
provisioner.myDuoProvisioner.updateGroups = true
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
