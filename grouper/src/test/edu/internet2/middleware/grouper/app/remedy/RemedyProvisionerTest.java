package edu.internet2.middleware.grouper.app.remedy;

import java.util.Map;

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
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

public class RemedyProvisionerTest extends GrouperTest {
  
  public RemedyProvisionerTest(String name) {
    super(name);
  }

  public RemedyProvisionerTest() {

  }
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new RemedyProvisionerTest("testFullSyncProvision"));
  }
  
  public void setUp() {
    super.setUp();
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    //ssl = true;
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.remedy1.url").value(domainName+":"+port+"/grouper/mockServices/remedy").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.remedy1.username").value("test").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.remedy1.password").value("test").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.remedyConnector.remedy1.enabled").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperTest.remedy.mock.configId").value("remedy1").store();
    
    try {
      GrouperRemedyApiCommands.retrieveRemedyGroups("remedy1");
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_user").executeSql();  
    } catch (Exception e) {
      
    }
    
  }
  
  public void testFullSyncProvision() {
    
    this.setUp();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.class").value("edu.internet2.middleware.grouper.app.remedy.GrouperRemedyProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.remedyExternalSystemConfigId").value("remedy1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.numberOfEntityAttributes").value("2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.numberOfGroupAttributes").value("3").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.fieldName").value("personId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.fieldName").value("remedyLoginId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("loginid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetEntityAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.fieldName").value("permissionGroup").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.0.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.fieldName").value("permissionGroupId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.1.valueType").value("string").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.name").value("statusString").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("attribute__description").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myRemedyProvisioner.targetGroupAttribute.2.valueType").value("string").store();

    GrouperStartup.startup();
    
    try {
      // this will create tables
      Map<Long, GrouperRemedyGroup> grouperRemedyGroups = GrouperRemedyApiCommands.retrieveRemedyGroups("remedy1");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_remedy_user").executeSql();
      
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
      attributeValue.setDoProvision("myRemedyProvisioner");
      attributeValue.setTargetName("myRemedyProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myRemedyProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_remedy_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      GrouperRemedyGroup grouperRemedyGroup = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).get(0);
      
      assertEquals("testGroup", grouperRemedyGroup.getPermissionGroup());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myRemedyProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperRemedyGroup.getPermissionGroupId(), gcGrouperSyncGroup.getGroupToId2());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_remedy_membership also
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myRemedyProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myRemedyProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup").list(GrouperRemedyGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser").list(GrouperRemedyUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership").list(GrouperRemedyMembership.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
    
  }

}
