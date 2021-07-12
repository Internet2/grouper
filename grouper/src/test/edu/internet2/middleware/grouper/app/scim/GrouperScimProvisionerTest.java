package edu.internet2.middleware.grouper.app.scim;

import java.sql.Timestamp;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
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
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJobState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import junit.textui.TestRunner;

public class GrouperScimProvisionerTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperScimProvisionerTest("testGithubFullSync"));

  }
  
  public static boolean startTomcat = false;
  
  public GrouperScimProvisionerTest(String name) {
    super(name);
  }
  
  public void testGithubFullSync() {
    if (!tomcatRunTests()) {
      return;
    }

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
    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/githubScim/v2/organizations/orgName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.githubExternalSystem.accessTokenPassword").value(token).store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.acceptHeader").value("application/vnd.github.v3+json").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.bearerTokenExternalSystemConfigId").value("githubExternalSystem").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.class").value("edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.groupIdOfUsersToProvision").value(testGroup.getUuid()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.scimType").value("Github").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.selectAllEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.selectMemberships").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.name").value("userName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.1.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.name").value("familyName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.name").value("emailValue").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("email").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.targetEntityAttribute.4.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.githubProvisioner.updateGroups").value("false").store();
    
    
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

    //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("githubProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("githubProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      try {
        grouperProvisioner = GrouperProvisioner.retrieveProvisioner("githubProvisioner");
        grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
        fail();
      } catch (Exception e) {
        // good
      }
      
    } finally {
      
    }
  }
  
  
  public void testAWSFullSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    
    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/awsScim/v2/").store();
    
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value("https://scim.us-east-1.amazonaws.com/f3v2c1eb369-9368-4f26-b09d-fc7d7aed9f43/scim/v2/").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value("700eb94d-c8d3-48b5-bf49-d45e277f0001:c0b74e0a-5ad2-48ae-bbe4-d6d3845bb7b1:LVZa3AF0PVFLbtKJJpuAUMCeKTEtmhRVMu/NAm8qVeP+O+dicL0D7JyU/lCnF03BwqioFU6jidrvOjA93EMEMEbFhGn0wItqN1YaIQwZ01PDz7kqhh1gCkQegogOWnnvHFIsa5CqMjcJ6Q0Tu+TCYiaHk9YdQb/PNbQ=:Kdzcqlfpqox6DcBFzYwZAp1RbXHEBoc4fGHZ2XtAhy4+JMwZhesfTXVBjApTV7ZV9dpSRn+4VyyMRGP1auGcrCmFs7hNyaMz//tBXSxf1vVFq0SNNZRCk95u8APWYMdOvFMYZ8exNljg6covW0Qli5TxbcJD3olaGcXVRFbn70QAxIsU2Vf6Fp/PRmX5W0TtqXRRCXYsNmw78QWW0o8q+pu/zLGk66gNJFitHjMxoTGIMjJmq4+af6PkmzdAhAA7SR9JIPcNDDOQNx9t7gpkWt16gtrekfsn1XwFQXrVVDrKkJGYWe261DgRkn+1KnanI9k19ab5p7QZDje5lIMm/A==").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value(token).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.bearerTokenExternalSystemConfigId").value("awsConfigId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.class").value("edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.hasTargetGroupLink").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.numberOfGroupAttributes").value("2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.replaceMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.scimMembershipType").value("groups").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.scimType").value("AWS").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectAllEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectMemberships").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.name").value("userName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.name").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.name").value("familyName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.fieldName").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("extension").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.translateToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.updateGroups").value("false").store();

    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

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
      attributeValue.setDoProvision("awsProvisioner");
      attributeValue.setTargetName("awsProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("awsProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2Group grouperScimGroup = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).get(0);
//      
      assertEquals("testGroup", grouperScimGroup.getDisplayName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(2, gcGrouperSync.getUserCount().intValue());
      assertEquals(1+2+2, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(started <  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
          System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupToId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId3());
      assertNull(gcGrouperSyncGroup.getGroupToId3());
      assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      Member testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      
      //assertEquals(grouperScimGroup.getGroup_id(), gcGrouperSyncGroup.getGroupToId2());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_scim_membership also
      started = System.currentTimeMillis();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("awsProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(1, gcGrouperSync.getUserCount().intValue());
      assertEquals(1+1+1, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
          System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncGroup.getInTargetStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started > gcGrouperSyncGroup.getProvisionableStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started > gcGrouperSyncGroup.getLastUpdated().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupToId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId3());
      assertNull(gcGrouperSyncGroup.getGroupToId3());
      assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncMember.getInTargetStart().getTime()); // because no change from previous run
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started > gcGrouperSyncMember.getProvisionableStart().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started > gcGrouperSyncMember.getLastUpdated().getTime()); // because no change from previous run
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      //now delete the group and sync again
      testGroup.delete();
      
      started = System.currentTimeMillis();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("awsProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      assertEquals(0, gcGrouperSync.getGroupCount().intValue());
      
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(0, gcGrouperSync.getUserCount().intValue());
      assertEquals(0, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(new Timestamp(System.currentTimeMillis()) + ", " + gcGrouperSync.getLastFullSyncRun(), 
          System.currentTimeMillis() >=  gcGrouperSync.getLastFullSyncRun().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("fullProvisionFull");
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("F", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("F", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupToId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId3());
      assertNull(gcGrouperSyncGroup.getGroupToId3());
      assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started > gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("F", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNotNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("F", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNotNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  
  public void testAWSIncrementalSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    int port = GrouperConfig.retrieveConfig().propertyValueInt("junit.test.tomcat.port", 8080);
    boolean ssl = GrouperConfig.retrieveConfig().propertyValueBoolean("junit.test.tomcat.ssl", false);
    String domainName = GrouperConfig.retrieveConfig().propertyValueString("junit.test.tomcat.domainName", "localhost");
    
    String token = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken.myWsBearerToken.accessTokenPassword");
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value(ssl ? "https://": "http://" +  domainName+":"+port+"/grouper/mockServices/awsScim/v2/").store();
    
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.endpoint").value("https://scim.us-east-1.amazonaws.com/f3v2c1eb369-9368-4f26-b09d-fc7d7aed9f43/scim/v2/").store();
//    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value("700eb94d-c8d3-48b5-bf49-d45e277f0001:c0b74e0a-5ad2-48ae-bbe4-d6d3845bb7b1:LVZa3AF0PVFLbtKJJpuAUMCeKTEtmhRVMu/NAm8qVeP+O+dicL0D7JyU/lCnF03BwqioFU6jidrvOjA93EMEMEbFhGn0wItqN1YaIQwZ01PDz7kqhh1gCkQegogOWnnvHFIsa5CqMjcJ6Q0Tu+TCYiaHk9YdQb/PNbQ=:Kdzcqlfpqox6DcBFzYwZAp1RbXHEBoc4fGHZ2XtAhy4+JMwZhesfTXVBjApTV7ZV9dpSRn+4VyyMRGP1auGcrCmFs7hNyaMz//tBXSxf1vVFq0SNNZRCk95u8APWYMdOvFMYZ8exNljg6covW0Qli5TxbcJD3olaGcXVRFbn70QAxIsU2Vf6Fp/PRmX5W0TtqXRRCXYsNmw78QWW0o8q+pu/zLGk66gNJFitHjMxoTGIMjJmq4+af6PkmzdAhAA7SR9JIPcNDDOQNx9t7gpkWt16gtrekfsn1XwFQXrVVDrKkJGYWe261DgRkn+1KnanI9k19ab5p7QZDje5lIMm/A==").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("grouper.wsBearerToken.awsConfigId.accessTokenPassword").value(token).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.bearerTokenExternalSystemConfigId").value("awsConfigId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.class").value("edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Provisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.debugLog").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteEntitiesIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteGroupsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.deleteMembershipsIfNotExistInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.hasTargetGroupLink").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.hasTargetEntityLink").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.logAllObjectsVerbose").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.numberOfEntityAttributes").value("5").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.numberOfGroupAttributes").value("2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.provisioningType").value("membershipObjects").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.replaceMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.scimMembershipType").value("groups").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.scimType").value("AWS").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectAllEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.selectMemberships").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.select").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.name").value("userName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.1.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.name").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.2.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.name").value("familyName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.3.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.isFieldElseAttribute").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.name").value("givenName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.translateFromGrouperProvisioningEntityField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetEntityAttribute.4.update").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.fieldName").value("displayName").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("extension").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.fieldName").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.isFieldElseAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.targetGroupAttribute.1.translateToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.updateEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.awsProvisioner.updateGroups").value("false").store();

    GrouperStartup.startup();
    
    
    // edu.internet2.middleware.grouper.changeLog.esb.consumer
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.class", EsbConsumer.class.getName());
    // edu.internet2.middleware.grouper.app.provisioning
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.publisher.class", ProvisioningConsumer.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.quartzCron",  "0 0 5 * * 2000");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.provisionerConfigId", "awsProvisioner");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.provisionerJobSyncType", GrouperProvisioningType.incrementalProvisionChangeLog.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("changeLog.consumer.awsScimProvTestCLC.publisher.debug", "true");

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("awsProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      runJobs(true, true);
      
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
      attributeValue.setDoProvision("awsProvisioner");
      attributeValue.setTargetName("awsProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      runJobs(true, true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      GrouperScim2Group grouperScim2Group = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).get(0);
      
      assertEquals("testGroup", grouperScim2Group.getDisplayName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "awsProvisioner");
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      assertEquals(2, gcGrouperSync.getUserCount().intValue());
      assertEquals(1+2+2, gcGrouperSync.getRecordsCount().intValue());
      assertTrue(started < gcGrouperSync.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSync.getLastUpdated().getTime());
      
      GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveBySyncType("incrementalProvisionChangeLog");
      assertEquals(100, gcGrouperSyncJob.getPercentComplete().intValue());
      assertEquals(GcGrouperSyncJobState.notRunning, gcGrouperSyncJob.getJobState());
      assertTrue(started < gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastSyncStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncTimestamp().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastSyncStart().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastTimeWorkWasDone().getTime());
      assertTrue(started < gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getHeartbeat().getTime());
      assertTrue(started < gcGrouperSyncJob.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncJob.getLastUpdated().getTime());
      assertNull(gcGrouperSyncJob.getErrorMessage());
      assertNull(gcGrouperSyncJob.getErrorTimestamp());
      
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(testGroup.getIdIndex(), gcGrouperSyncGroup.getGroupIdIndex());
      assertEquals("T", gcGrouperSyncGroup.getProvisionableDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetDb());
      assertEquals("T", gcGrouperSyncGroup.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncGroup.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getInTargetStart().getTime());
      assertNull(gcGrouperSyncGroup.getInTargetEnd());
      assertTrue(started < gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncGroup.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncGroup.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncGroup.getLastUpdated().getTime());
      assertNotNull(gcGrouperSyncGroup.getGroupToId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId2());
      assertNull(gcGrouperSyncGroup.getGroupFromId3());
      assertNull(gcGrouperSyncGroup.getGroupToId3());
      assertNull(gcGrouperSyncGroup.getLastGroupMetadataSync());
      assertNull(gcGrouperSyncGroup.getErrorMessage());
      assertNull(gcGrouperSyncGroup.getErrorTimestamp());
      assertNull(gcGrouperSyncGroup.getLastGroupSync());
      
      Member testSubject0member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
      Member testSubject1member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject0member.getId());
      assertEquals(testSubject0member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject0member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject0member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject0member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());

      gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(testSubject1member.getId());
      assertEquals(testSubject1member.getId(), gcGrouperSyncMember.getMemberId());
      assertEquals(testSubject1member.getSubjectId(), gcGrouperSyncMember.getSubjectId());
      assertEquals(testSubject1member.getSubjectSourceId(), gcGrouperSyncMember.getSourceId());
      assertEquals(testSubject1member.getSubjectIdentifier0(), gcGrouperSyncMember.getSubjectIdentifier());
      assertEquals("T", gcGrouperSyncMember.getProvisionableDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetDb());
      assertEquals("T", gcGrouperSyncMember.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMember.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMember.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMember.getProvisionableStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getProvisionableStart().getTime());
      assertNull(gcGrouperSyncMember.getProvisionableEnd());
      assertTrue(started < gcGrouperSyncMember.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMember.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMember.getMemberFromId2());
      assertNull(gcGrouperSyncMember.getMemberFromId3());
      assertNotNull(gcGrouperSyncMember.getMemberToId2());
      assertNull(gcGrouperSyncMember.getMemberToId3());
      assertNull(gcGrouperSyncMember.getLastUserMetadataSync());
      assertNull(gcGrouperSyncMember.getErrorMessage());
      assertNull(gcGrouperSyncMember.getErrorTimestamp());
      assertNull(gcGrouperSyncMember.getLastUserSync());
      
      GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject0member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(testGroup.getId(), testSubject1member.getId());
      assertEquals("T", gcGrouperSyncMembership.getInTargetDb());
      assertEquals("T", gcGrouperSyncMembership.getInTargetInsertOrExistsDb());
      assertTrue(started < gcGrouperSyncMembership.getInTargetStart().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getInTargetStart().getTime());
      assertNull(gcGrouperSyncMembership.getInTargetEnd());
      assertTrue(started < gcGrouperSyncMembership.getLastUpdated().getTime());
      assertTrue(System.currentTimeMillis() >= gcGrouperSyncMembership.getLastUpdated().getTime());
      assertNull(gcGrouperSyncMembership.getMembershipId());
      assertNull(gcGrouperSyncMembership.getMembershipId2());
      assertNull(gcGrouperSyncMembership.getErrorMessage());
      assertNull(gcGrouperSyncMembership.getErrorTimestamp());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      runJobs(true, true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      
      
      
      
      //now add a subject to test group
      testGroup.addMember(SubjectTestHelper.SUBJ3, false);
      runJobs(true, true);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      
      
      
      //now delete the group and sync again
      testGroup.delete();
      
      runJobs(true, true);
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
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
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_awsScimProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("awsScimProvTestCLC", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }

}
