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
import edu.internet2.middleware.grouper.app.duo.DuoProvisionerTestUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
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

public class GrouperDuoRoleProvisionerTest extends GrouperProvisioningBaseTest {

  public GrouperDuoRoleProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoRoleProvisionerTest() {

  }

  @Override
  public String defaultConfigId() {
    return "myDuoRoleProvisioner";
  }

  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new GrouperDuoRoleProvisionerTest("testFullProvisionInsertUpdateDeleteAdministrator"));
  }

  public void setUp() {
    super.setUp();

    DuoRoleProvisionerTestUtils.setupDuoRoleExternalSystem();
    
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

    DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(new DuoRoleProvisionerTestConfigInput());

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
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
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
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDuoRoleProvisioner");
      assertEquals(3, gcGrouperSync.getUserCount().intValue());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      grouperProvisioningOutput = fullProvision();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
//      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      
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

    DuoRoleProvisionerTestUtils.configureDuoRoleProvisioner(new DuoRoleProvisionerTestConfigInput());
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoRoleUser> grouperDuoRoleUsers = GrouperDuoRoleApiCommands.retrieveDuoAdministrators("duo1");
//  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_role_user").executeSql();
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_role_user").select(int.class));
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      incrementalProvision();

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
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_duoRoles", "Help Desk");
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDoProvision("myDuoRoleProvisioner");
      attributeValue.setTargetName("myDuoRoleProvisioner");
      
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
      incrementalProvision();
      
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      incrementalProvision();

      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoRoleUser").list(GrouperDuoRoleUser.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
}
