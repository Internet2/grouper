package edu.internet2.middleware.grouper.app.duo;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import junit.textui.TestRunner;

public class GrouperDuoProvisionerTest extends GrouperTest {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new GrouperDuoProvisionerTest("testIncrementalProvisionDuo"));
  }
  
  public GrouperDuoProvisionerTest(String name) {
    super(name);
  }

  public GrouperDuoProvisionerTest() {

  }
  
  public void setUp() {
    super.setUp();
    
    DuoProvisionerTestUtils.setupDuoExternalSystem();
    
    try {
      GrouperDuoApiCommands.retrieveDuoGroups("duo1");
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_user").executeSql();  
    } catch (Exception e) {
      
    }
    
  }
  
  public static boolean startTomcat = false;
  
  public void testDuoGroupCrud() {
    if (!tomcatRunTests()) {
      return;
    }
    
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
    
    Set<String> fieldsToUpdate = new HashSet<String>();
    fieldsToUpdate.add("name");
    fieldsToUpdate.add("desc");
    
    duoGroup = GrouperDuoApiCommands.updateDuoGroup("duo1", grouperDuoGroup, fieldsToUpdate);
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
    
    Set<String> fieldsToUpdate = new HashSet<String>();
    fieldsToUpdate.add("email");
    fieldsToUpdate.add("username");
    fieldsToUpdate.add("firstname");
    fieldsToUpdate.add("lastname");
    
    duoUser = GrouperDuoApiCommands.updateDuoUser("duo1", grouperDuoUser, fieldsToUpdate);
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
    
    List<GrouperDuoUser> duoUsers = GrouperDuoApiCommands.retrieveDuoUsers("duo1", false);
    assertEquals(200, duoUsers.size());
    //now delete all of them
    for (GrouperDuoUser duoUser1: duoUsers) {
      GrouperDuoApiCommands.deleteDuoUser("duo1", duoUser1.getId());
    }
    
    duoUsers = GrouperDuoApiCommands.retrieveDuoUsers("duo1", false);
    assertEquals(0, duoUsers.size());
    
  }
  
  public void testDuoMembershipCrud() {
    if (!tomcatRunTests()) {
      return;
    }

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
  public void testFullProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    DuoProvisionerTestUtils.configureDuoProvisioner(new DuoProvisionerTestConfigInput());
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoGroup> grouperDuoGroups = GrouperDuoApiCommands.retrieveDuoGroups("duo1");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_user").executeSql();
      
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
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      GrouperDuoGroup grouperDuoGroup = HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).get(0);
      
      assertEquals("testGroup", grouperDuoGroup.getName());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myDuoProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperDuoGroup.getGroup_id(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_duo_membership also
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  /**
   * 
   */
  public void testFullProvisionLoadEntitiesIntoDuoUsersTable() {
    
    if (!tomcatRunTests()) {
      return;
    }

    DuoProvisionerTestUtils.configureDuoProvisioner(new DuoProvisionerTestConfigInput());
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoGroup> grouperDuoGroups = GrouperDuoApiCommands.retrieveDuoGroups("duo1");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_user").executeSql();
      
      new GcDbAccess().connectionName("grouper").sql("insert into mock_duo_user (email, first_name, last_name, user_id, real_name, user_name, phones, push_enabled, aliases, enrolled, last_directory_sync, notes, status, created_at, last_login) values "
          + "('test.subject.0@test.com', 'first', 'last', '123abc', 'real name', 'user name', '123-456-7890', 'T', 'test,abc', 'T', 72832323223, 'test notes', 'active', 87877787878, 78787777888 )").executeSql();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
//      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
//      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
//      
//      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
//      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
//      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
     
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from grouper_prov_duo_user").select(int.class));
      
      List<Object[]> results = new GcDbAccess().connectionName("grouper").sql("select config_id, user_id, aliases, phones, is_push_enabled, "
          + " email, first_name, last_name, is_enrolled, last_directory_sync, notes, real_name, status, user_name, created_at, last_login_time from grouper_prov_duo_user").selectList(Object[].class);
      
      Object[] oneRowOfData = results.get(0);
      
      assertEquals("myDuoProvisioner", oneRowOfData[0]);
      assertEquals("123abc", oneRowOfData[1]);
      assertEquals("abc,test", oneRowOfData[2]);
      assertEquals("123-456-7890", oneRowOfData[3]);
      assertEquals("T", oneRowOfData[4]);
      assertEquals("test.subject.0@test.com", oneRowOfData[5]);
      assertEquals("first", oneRowOfData[6]);
      assertEquals("last", oneRowOfData[7]);
      assertEquals("T", oneRowOfData[8]);
      assertTrue(BigDecimal.valueOf(72832323223L).equals(oneRowOfData[9]));
      assertEquals("test notes", oneRowOfData[10]);
      assertEquals("real name", oneRowOfData[11]);
      assertEquals("active", oneRowOfData[12]);
      assertEquals("user name", oneRowOfData[13]);
      assertTrue(BigDecimal.valueOf(87877787878L).equals(oneRowOfData[14]));
      assertTrue(BigDecimal.valueOf(78787777888L).equals(oneRowOfData[15]));
      
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  
  public void testIncrementalProvisionDuo() {
    
    if (!tomcatRunTests()) {
      return;
    }

    DuoProvisionerTestUtils.configureDuoProvisioner(new DuoProvisionerTestConfigInput());

    GrouperStartup.startup();
    

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperDuoGroup> grouperDuoGroups = GrouperDuoApiCommands.retrieveDuoGroups("duo1");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_duo_user").executeSql();
      
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("myDuoProvisioner");
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      
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
      
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myDuoProvisioner");
      attributeValue.setTargetName("myDuoProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_duo_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      
      runJobs(true, true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      GrouperDuoGroup grouperDuoGroup = HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).get(0);
      
      assertEquals("testGroup", grouperDuoGroup.getName());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      runJobs(true, true);
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      runJobs(true, true);
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoGroup").list(GrouperDuoGroup.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoUser").list(GrouperDuoUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperDuoMembership").list(GrouperDuoMembership.class).size());
      
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
      hib3GrouploaderLog.setJobName("CHANGE_LOG_consumer_duoProvTestCLC");
      hib3GrouploaderLog.setStatus(GrouperLoaderStatus.RUNNING.name());
      EsbConsumer esbConsumer = new EsbConsumer();
      ChangeLogHelper.processRecords("duoProvTestCLC", hib3GrouploaderLog, esbConsumer);
  
      return hib3GrouploaderLog;
    }
    
    return null;
  }

}
