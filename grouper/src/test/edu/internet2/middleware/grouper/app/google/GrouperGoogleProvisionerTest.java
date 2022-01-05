package edu.internet2.middleware.grouper.app.google;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
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
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
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


public class GrouperGoogleProvisionerTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    TestRunner.run(new GrouperGoogleProvisionerTest("testFullSyncGoogle"));
    
  }
  
  public GrouperGoogleProvisionerTest(String name) {
    super(name);
  }
  
  private boolean startTomcat = false;
  
  public void testIncrementalSyncGoogle() throws IOException {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.domain").value("viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountEmail").value("vivek-grouper@industrial-keep-335804.iam.gserviceaccount.com").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.tokenUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/token/").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.directoryApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.groupSettingsApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/settings").store();
    
    String[] rsaKeypair = GrouperUtil.generateRsaKeypair(2048);
    String privateKey = rsaKeypair[1];
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.publicKey").value(rsaKeypair[0]).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.configId").value("myGoogle").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountPrivateKeyPEM").value(privateKey).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceImpersonationUser").value("vivek@viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.enabled").value("true").store();
   
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.googleExternalSystemConfigId").value("myGoogle").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.class").value("edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.debugLog").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntitiesIfNotExistInGrouper").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntitiesIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroupsIfNotExistInGrouper").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroupsIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.numberOfEntityAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.numberOfGroupAttributes").value("6").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllEntitiesDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllGroupsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllMembershipsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectMemberships").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.whoCanViewGroup").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.allowWebPosting").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.common.subjectLink.memberFromId2").value("${subject.getAttributeValue(\"email\")}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.subjectSourcesToProvision").value("jdbc").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.update").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.name").value("familyName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.fieldName").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.translateExpression").value("${grouperProvisioningEntity.getId() + '@viveksachdeva.com'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.fieldName").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.translateExpression").value("${grouperProvisioningGroup.getId() + '@viveksachdeva.com'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.name").value("allowWebPosting").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.translateExpression").value("${'true'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.name").value("whoCanViewGroup").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.translateExpression").value("${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_whoCanViewGroup'), 'ALL_IN_DOMAIN_CAN_VIEW')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.updateGroups").value("true").store();
    
    GrouperStartup.startup();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.provisionerConfigId", "myGoogleProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.googleProvTestCLC.publisher.debug", "true");
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      runJobs(true, true);
      
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
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      runJobs(true, true);
  
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      
      assertEquals("test:testGroup", grouperGoogleGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperGoogleGroup.getId(), gcGrouperSyncGroup.getGroupToId2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
      runJobs(true,true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      runJobs(true,true);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      runJobs(true,true);
      
      GrouperGoogleGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      assertEquals("ALL_MEMBERS_CAN_VIEW", groupWithUpdatedDescription.getWhoCanViewGroup());
      
      //now delete the group and sync again
      testGroup.delete();
      
      runJobs(true,true);
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  public void testFullSyncGoogle() throws IOException {
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.domain").value("viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountEmail").value("vivek-grouper@industrial-keep-335804.iam.gserviceaccount.com").store();

    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.tokenUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/token/").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.directoryApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.groupSettingsApiBaseUrl").value("http" + (ssl?"s":"") + "://" + domainName + ":" + port + "/grouper/mockServices/google/settings").store();
    
    String[] rsaKeypair = GrouperUtil.generateRsaKeypair(2048);
    String privateKey = rsaKeypair[1];
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.publicKey").value(rsaKeypair[0]).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.google.mock.configId").value("myGoogle").store();
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceAccountPrivateKeyPEM").value(privateKey).store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.serviceImpersonationUser").value("vivek@viveksachdeva.com").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.googleConnector.myGoogle.enabled").value("true").store();
   
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.googleExternalSystemConfigId").value("myGoogle").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.class").value("edu.internet2.middleware.grouper.app.google.GrouperGoogleProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.debugLog").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntitiesIfNotExistInGrouper").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteEntitiesIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroupsIfNotExistInGrouper").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteGroupsIfGrouperDeleted").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.hasTargetGroupLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.numberOfEntityAttributes").value("4").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.numberOfGroupAttributes").value("6").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllEntitiesDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllGroupsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectAllMembershipsDuringDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.selectMemberships").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.whoCanViewGroup").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.allowWebPosting").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.common.subjectLink.memberFromId2").value("${subject.getAttributeValue(\"email\")}").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.showProvisioningDiagnostics").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.subjectSourcesToProvision").value("jdbc").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.1.update").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.name").value("familyName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.fieldName").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.translateExpression").value("${grouperProvisioningEntity.getId() + '@viveksachdeva.com'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetEntityAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.0.translateToGroupSyncField").value("groupToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.fieldName").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.1.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.name").value("description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.2.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.name").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.translateExpression").value("${grouperProvisioningGroup.getId() + '@viveksachdeva.com'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.translateExpressionType").value("translationScript").store(); 
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.3.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.name").value("allowWebPosting").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.translateExpression").value("${'true'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.name").value("whoCanViewGroup").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.translateExpression").value("${grouperUtil.defaultString(grouperProvisioningGroup.retrieveAttributeValueString('md_grouper_whoCanViewGroup'), 'ALL_IN_DOMAIN_CAN_VIEW')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.targetGroupAttribute.5.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myGoogleProvisioner.updateGroups").value("true").store();
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperGoogleGroup> grouperGoogleGroups = GrouperGoogleApiCommands.retrieveGoogleGroups("myGoogle");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_google_user").executeSql();
      //new GcDbAccess().connectionName("grouper").sql("delete from mock_google_auth").executeSql();
      
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
      
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      attributeValue.setStemScopeString("sub");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MANAGERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_google_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      GrouperGoogleGroup grouperGoogleGroup = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.isRecalc());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.isRecalc());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.isRecalc());
      }
      
      assertEquals("test:testGroup", grouperGoogleGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myGoogleProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperGoogleGroup.getId(), gcGrouperSyncGroup.getGroupToId2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_google_membership also
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
      
      // update group description and settings
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myGoogleProvisioner");
      attributeValue.setTargetName("myGoogleProvisioner");
      
      metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_whoCanViewGroup", "ALL_MEMBERS_CAN_VIEW");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 
      
      GrouperGoogleGroup groupWithUpdatedDescription = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).get(0);
      assertEquals("newDescription", groupWithUpdatedDescription.getDescription());
      assertEquals("ALL_MEMBERS_CAN_VIEW", groupWithUpdatedDescription.getWhoCanViewGroup());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myGoogleProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup").list(GrouperGoogleGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser").list(GrouperGoogleUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperGoogleMembership").list(GrouperGoogleMembership.class).size());
      
    } finally {
      
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
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_googleProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("googleProvTestCLC", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }

}
