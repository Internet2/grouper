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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
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

public class GrouperGithubProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperGithubProvisionerTest("testAWSFullSyncProvisionGroupAndThenDeleteTheGroup"));

  }
  
  public static boolean startTomcat = false;
  
  @Override
  public String defaultConfigId() {
    return "githubProvisioner";
  }

  public GrouperGithubProvisionerTest(String name) {
    super(name);
  }
  
  public void testGithubIncrementalSync() {
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
    
    ScimProvisionerTestUtils.setupGithubExternalSystem();
    
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignSubjectLinkCache0("${subject.getAttributeValue('email')}")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(testGroup)
        .assignScimMembershipType("group")
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignEntityAttribute4name("emailValue")
        );
        
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    

    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
      
      //lets sync these over      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.addMember(SubjectTestHelper.SUBJ2);
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
    } finally {
      
    }
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
    
    ScimProvisionerTestUtils.setupGithubExternalSystem();
        
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId("githubProvisioner").assignChangelogConsumerConfigId("githubScimProvTestCLC")
        .assignAcceptHeader("application/vnd.github.v3+json")
        .assignBearerTokenExternalSystemConfigId("githubExternalSystem")
        .assignSubjectLinkCache0("${subject.getAttributeValue('email')}")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupOfUsersToProvision(testGroup)
        .assignScimMembershipType("group")
        .assignScimType("Github")
        .assignSelectAllEntities(true)
        .assignGroupAttributeCount(0)
        .assignEntityAttribute4name("emailValue")
        );

    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperScim2User> grouperScimUsers = GrouperScim2ApiCommands.retrieveScimUsers("githubExternalSystem", null);
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();

    //lets sync these over
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_scim_group").select(int.class));
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioningOutput = fullProvision();
      
      GrouperUtil.sleep(2000);

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperScim2Membership").list(GrouperScim2Membership.class).size());
      
      // now delete the group and sync again
      testGroup.delete();
      try {
        grouperProvisioningOutput = fullProvision();
        fail();
      } catch (Exception e) {
        // good
      }
      
    } finally {
      
    }
  }
  
}
