package edu.internet2.middleware.grouper.app.messagingProvisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapProvisionerJDBCSubjectSourceTest;
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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogHelper;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperMessagingProvisionerTest extends GrouperProvisioningBaseTest {
  
  public static boolean startTomcat = false;
  
  public GrouperMessagingProvisionerTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperMessagingProvisionerTest("testIncrementalSyncMessagingWithBuiltinMessaging"));
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

      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myMessagingProvisioner");
      
      GrouperProvisioningOutput grouperProvisioningOutput = super.fullProvision(grouperProvisioner);;
      
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
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myMessagingProvisioner");
      attributeValue.setTargetName("myMessagingProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      runJobs(true, true);
      
      // one group add, two members add, two memberships add
      assertEquals(new Integer(5), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      // update group description and it should generate one message
      testGroup = new GroupSave(grouperSession).assignDescription("new description")
          .assignName(testGroup.getName()+"New")
          .assignUuid(testGroup.getUuid()).assignSaveMode(SaveMode.UPDATE).save();
      
      runJobs(true, true);
      
      // one group add, two members add, two memberships add, one group update
      assertEquals(new Integer(6), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      runJobs(true, true);
      
      // one group add, two members add, two memberships add, one group update, one member remove, one membership remove
      assertEquals(new Integer(8), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      runJobs(true, true);
      
      // one group add, three members add, three memberships add, one group update, one member remove, one membership remove
      assertEquals(new Integer(10), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
      //now delete the group and sync again
      testGroup.delete();
      runJobs(true, true);
      
      assertEquals(new Integer(15), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_message").select(int.class));
      
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
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_messagingProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("messagingProvTestCLC", hib3GrouploaderLog, esbConsumer);
  
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      
      return hib3GrouploaderLog;
    }
    
    return null;
  }

}
