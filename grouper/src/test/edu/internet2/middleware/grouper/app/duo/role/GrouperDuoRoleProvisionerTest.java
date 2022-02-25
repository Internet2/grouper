package edu.internet2.middleware.grouper.app.duo.role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import junit.textui.TestRunner;

public class GrouperDuoRoleProvisionerTest extends GrouperTest {

  public GrouperDuoRoleProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoRoleProvisionerTest() {

  }
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new GrouperDuoRoleProvisionerTest("testFullProvisionInsertUpdateDeleteAdministrator"));
  }
  
  public void setUp() {
    super.setUp();
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminDomainName").value("api-8b39645d.duosecurity.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminIntegrationKey").value("DI3GFYRTLYKA0J3E3U1H").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.adminSecretKey").value("PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.duoConnector.duo1.useSsl").value(ssl ? "true":"false").store();
//    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.duo.mock.configId").value("duo1").store();
    
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

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.class").value("edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.duoExternalSystemConfigId").value("duo1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteEntitiesIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.numberOfEntityAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.numberOfGroupAttributes").value("1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.provisioningType").value("entityAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.subjectSourcesToProvision").value("jdbc").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.fieldName").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateExpression").value("grouperProvisioningEntity.retrieveAttributeValueString('md_grouper_duoEmail')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateGrouperToMemberSyncField").value("memberFromId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.fieldName").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.translateToMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.name").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.select").value("true").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateFromGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.name").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateExpression").value("grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_duoRoles')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.updateEntities").value("true").store();

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
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      
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
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDuoProvisioner");
      assertEquals(3, gcGrouperSync.getUserCount().intValue());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
//      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  public void testIncrementalProvisionInsertUpdateDeleteAdministrator() {
    
    if (!tomcatRunTests()) {
      return;
    }

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.class").value("edu.internet2.middleware.grouper.app.duo.role.GrouperDuoRoleProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.duoExternalSystemConfigId").value("duo1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.deleteEntitiesIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.numberOfEntityAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.numberOfGroupAttributes").value("1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.provisioningType").value("entityAttributes").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.subjectSourcesToProvision").value("jdbc").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.fieldName").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateExpression").value("grouperProvisioningEntity.retrieveAttributeValueString('md_grouper_duoEmail')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.0.translateGrouperToMemberSyncField").value("memberFromId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.fieldName").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.translateToMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.2.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.name").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.select").value("true").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetEntityAttribute.3.translateFromGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.name").value("role").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateExpression").value("grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_duoRoles')").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.required").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.targetGroupAttribute.0.translateGrouperToGroupSyncField").value("groupFromId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myDuoProvisioner.updateEntities").value("true").store();

    GrouperStartup.startup();
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.provisionerConfigId", "myDuoProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.duoRoleProvTestCLC.publisher.debug", "true");

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoRoleUser> grouperDuoRoleUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
//  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      runJobs(true, true);
      
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
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      
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
      runJobs(true, true);
      
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      runJobs(true, true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      runJobs(true, true);
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  private Hib3GrouperLoaderLog runJobs(boolean runChangeLog, boolean runConsumer) {
    
    // wait for message cache to clear
    GrouperUtil.sleep(10000);
    
    if (runChangeLog) {
      ChangeLogTempToEntity.convertRecords();
    }
    
    if (runConsumer) {
      Hib3GrouperLoaderLog hib3GrouploaderLog = new Hib3GrouperLoaderLog();
      hib3GrouploaderLog.setHost(GrouperUtil.hostname());
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_duoRoleProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("duoRoleProvTestCLC", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }
}
