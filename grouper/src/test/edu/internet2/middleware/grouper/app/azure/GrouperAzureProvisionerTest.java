package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoAdapter;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;


public class GrouperAzureProvisionerTest extends GrouperProvisioningBaseTest {
  
  /**
   * set to true for large stress test
   */
  private static final boolean AZURE_STRESS = false; 

  /**
   * if all users and groups should be deleted before test
   */
  private static final boolean AZURE_DELETE_OBJECTS_BEFORE_TESTS = true; 
  

  /**
   * 
   */
  private static final int AZURE_USERS_TO_CREATE = AZURE_STRESS ? 5000 : 50;
  
  /**
   * 
   */
  private static final int AZURE_GROUPS_TO_CREATE = AZURE_STRESS ? 5000 : 50;
  
  /**
   * 
   */
  private static final int AZURE_MEMBERSHIPS_TO_CREATE = AZURE_STRESS ? 200000 : 2000;
  
  public static void main(String[] args) {
    TestRunner.run(new GrouperAzureProvisionerTest("testDeleteMembershipsInTrackedGroupsOnlyTrue"));
  }

  public GrouperAzureProvisionerTest(String name) {
    super(name);
  }

  public GrouperAzureProvisionerTest() {
    
  }
  
  public static boolean startTomcat = false;
  
  public void setUp() {
    super.setUp();

    // this will create tables
    AzureMockServiceHandler.ensureAzureMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_azure_user").executeSql();

  }
  
  @Override
  public String defaultConfigId() {
    return "myAzureProvisioner";
  }

  public static void realAzureAddUsersAndGroups() {
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(4).assignRealAzure(true).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true"));
    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    List<GrouperAzureUser> azureUsersToCreate = new ArrayList<>();
    
//    List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(name + "@" + domain), "userPrincipalName");
    
    for (int i=AZURE_USERS_TO_CREATE;i<AZURE_USERS_TO_CREATE*2;i++) {
      String name = "Fred" + i;
      
      
//      if (azureUsers == null || azureUsers.size()  == 0) {
        GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
        grouperAzureUser.setUserPrincipalName(name + "@" + domain);
        grouperAzureUser.setDisplayName(name);
        grouperAzureUser.setMailNickname(name);
        
        azureUsersToCreate.add(grouperAzureUser);
//      } else {
//        break;
//      }
    }
    
    if (azureUsersToCreate.size() > 0) {
      GrouperAzureApiCommands.createAzureUsers("myAzure", azureUsersToCreate, null);
    }
    
    List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    
//    assertTrue(GrouperUtil.length(grouperAzureUsers) > 210);
    
    for (int i=AZURE_GROUPS_TO_CREATE;i<AZURE_GROUPS_TO_CREATE*2;i++) {
      String name = "test" + i;
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(name),  "displayName");
      if (grouperAzureGroups == null || grouperAzureGroups.size() == 0) {
        GrouperAzureGroup grouperAzureGroup = new GrouperAzureGroup();
        grouperAzureGroup.setDisplayName(name);
        grouperAzureGroup.setMailNickname(name);
        
        Map<GrouperAzureGroup, Set<String>> map = new HashMap<>();
        map.put(grouperAzureGroup, null);
        
        GrouperAzureApiCommands.createAzureGroups("myAzure", map);
      } else {
        break;
      }
    }

  }
  
  public static void realAzureDeleteUsersAndGroups() {
    
    GrouperSession.startRootSession();
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2).assignRealAzure(true).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true"));
    
    List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    
    List<GrouperAzureUser> grouperAzureUsersToDelete = new ArrayList<>();
    for (GrouperAzureUser grouperAzureUser: grouperAzureUsers) {
      if (grouperAzureUser.getUserPrincipalName().startsWith("Fred")) {
        grouperAzureUsersToDelete.add(grouperAzureUser);
      }
    }
    
    GrouperAzureApiCommands.deleteAzureUsers("myAzure", grouperAzureUsersToDelete);

    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
    
    GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups);

//    for (int i=0;i<350;i++) {
//      String name = "Fred" + i;
//      List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(name + "@" + domain), "userPrincipalName");
//      if (grouperAzureUsers != null && grouperAzureUsers.size() > 0) {
//        GrouperAzureApiCommands.deleteAzureUsers("myAzure", grouperAzureUsers);
//      }
//    }
//    
//    for (int i=0;i<350;i++) {
//      String name = "test" + i;
//      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(name), "displayName");
//      if (grouperAzureGroups != null && grouperAzureGroups.size() > 0) {
//        GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups);
//      }
//    }

  }
  
  public void testUdelFull() {
    udelHelper(true);
  }

  public void testUdelIncremental() {
    udelHelper(false);
  }
  
  public void testDeleteMembershipsInTrackedGroupsOnlyFalse() {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
        .assignRealAzure(false).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true").addExtraConfig("makeChangesToEntities", "true")
        .addExtraConfig("welcomeEmailDisabled", "true")
        .addExtraConfig("customizeMembershipCrud", "true")
        .addExtraConfig("deleteMembershipsIfNotExistInGrouper", "true")
        .addExtraConfig("deleteMembershipsIfGrouperDeleted", "true")
        .addExtraConfig("customizeGroupCrud", "true")
        .addExtraConfig("deleteGroupsIfGrouperDeleted", "true")
        .addExtraConfig("deleteMembershipsOnlyInTrackedGroups", "false")
        );
    
    RegistrySubject.add(grouperSession, "Fred400@" + domain, "person", "Fred400@" + domain);
    Subject fred = SubjectFinder.findById("Fred400@" + domain, true);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_azureGroupType", "security");

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Map<GrouperAzureGroup, Set<String>> groupToFieldNamesToInsert = new HashMap<>();
    
    GrouperAzureGroup azureGroup = new GrouperAzureGroup();
    azureGroup.setDisplayName("testDisplayName");
    azureGroup.setDescription("test description");
    azureGroup.setMailNickname("mailNickName");
    groupToFieldNamesToInsert.put(azureGroup, null);
    
    Map<GrouperAzureGroup, Exception> azureGroups = GrouperAzureApiCommands.createAzureGroups("myAzure", groupToFieldNamesToInsert);
    
    List<GrouperAzureUser> azureUsersToCreate = new ArrayList<>();
  
    String name = "Fred";
  
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    grouperAzureUser.setUserPrincipalName(name + "@" + domain);
    grouperAzureUser.setDisplayName(name);
    grouperAzureUser.setMailNickname(name);
    
    azureUsersToCreate.add(grouperAzureUser);
    Map<GrouperAzureUser, Exception> azureUsers = GrouperAzureApiCommands.createAzureUsers("myAzure", azureUsersToCreate, null);
    
    List<GrouperAzureUser> users = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    
    List<String> userIds = new ArrayList<>();
    
    for (GrouperAzureUser azureUser : users) {
      if (azureUser.getUserPrincipalName().startsWith("Fred")) {
        userIds.add(azureUser.getId());
      }
    }
    
    GrouperAzureApiCommands.createAzureMemberships("myAzure", azureGroup.getId(), userIds);
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    
    fullProvision();
 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    Set<String> groupMembers = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", azureGroups.keySet().iterator().next().getId());
    
    assertTrue(groupMembers.size() == 0);
    
  }
  
  public void testDeleteMembershipsInTrackedGroupsOnlyTrue() {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
        .assignRealAzure(false).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true").addExtraConfig("makeChangesToEntities", "true")
        .addExtraConfig("welcomeEmailDisabled", "true")
        .addExtraConfig("customizeMembershipCrud", "true")
        .addExtraConfig("deleteMembershipsIfNotExistInGrouper", "true")
        .addExtraConfig("deleteMembershipsIfGrouperDeleted", "true")
        .addExtraConfig("customizeGroupCrud", "true")
        .addExtraConfig("deleteGroupsIfGrouperDeleted", "true")
        .addExtraConfig("deleteMembershipsOnlyInTrackedGroups", "true")
        );
    
    RegistrySubject.add(grouperSession, "Fred400@" + domain, "person", "Fred400@" + domain);
    Subject fred = SubjectFinder.findById("Fred400@" + domain, true);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_azureGroupType", "security");

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
    
    Map<GrouperAzureGroup, Set<String>> groupToFieldNamesToInsert = new HashMap<>();
    
    GrouperAzureGroup azureGroup = new GrouperAzureGroup();
    azureGroup.setDisplayName("testDisplayName");
    azureGroup.setDescription("test description");
    azureGroup.setMailNickname("mailNickName");
    groupToFieldNamesToInsert.put(azureGroup, null);
    
    Map<GrouperAzureGroup, Exception> azureGroups = GrouperAzureApiCommands.createAzureGroups("myAzure", groupToFieldNamesToInsert);
    
    List<GrouperAzureUser> azureUsersToCreate = new ArrayList<>();
  
    String name = "Fred";
  
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    grouperAzureUser.setUserPrincipalName(name + "@" + domain);
    grouperAzureUser.setDisplayName(name);
    grouperAzureUser.setMailNickname(name);
    
    azureUsersToCreate.add(grouperAzureUser);
    Map<GrouperAzureUser, Exception> azureUsers = GrouperAzureApiCommands.createAzureUsers("myAzure", azureUsersToCreate, null);
    
    List<GrouperAzureUser> users = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    
    List<String> userIds = new ArrayList<>();
    
    for (GrouperAzureUser azureUser : users) {
      if (azureUser.getUserPrincipalName().startsWith("Fred")) {
        userIds.add(azureUser.getId());
      }
    }
    
    GrouperAzureApiCommands.createAzureMemberships("myAzure", azureGroup.getId(), userIds);
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    
    fullProvision();
 
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    Set<String> groupMembers = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", azureGroups.keySet().iterator().next().getId());
    
    assertEquals(1, groupMembers.size());
    
  }
  
  public void testUdelLargeOperationsFull() {
    assertEquals("memberships must be divisible by 2", 0, AZURE_MEMBERSHIPS_TO_CREATE%2);
    assertTrue("memberships must be less than equals users * groups", AZURE_MEMBERSHIPS_TO_CREATE <= (AZURE_USERS_TO_CREATE*AZURE_GROUPS_TO_CREATE));
    
    GrouperStartup.startup();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    if (AZURE_DELETE_OBJECTS_BEFORE_TESTS) {
      realAzureDeleteUsersAndGroups();
      GrouperUtil.sleep(10000);
    }
    
    List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    int initialUserSize = GrouperUtil.length(azureUsers);

    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2).assignRealAzure(true).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true").addExtraConfig("makeChangesToEntities", "true")
        .addExtraConfig("welcomeEmailDisabled", "true")
        .addExtraConfig("deleteMembershipsOnlyInTrackedGroups", "false")
        );
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    List<Group> groups = new ArrayList<>();
    for (int i=0;i<AZURE_GROUPS_TO_CREATE;i++) {
      Group testGroup = new GroupSave(grouperSession).assignName("test:test"+i).save();
      groups.add(testGroup);
    }
    
    for (int i=0;i<AZURE_USERS_TO_CREATE;i++) {
      RegistrySubject.add(grouperSession, "Fred"+i+"@" + domain, "person", "Fred"+i+"@" + domain);
    }
    
    int membershipCount = 0;
    int userIndex = 0;
    int loopCount = 0;
    OUTER: while(true) {
      userIndex = loopCount;
      for (Group testGroup: groups) {
        
        Subject fred = SubjectFinder.findById("Fred"+userIndex+"@" + domain, true);
        if (!testGroup.hasMember(fred)) {
          testGroup.addMember(fred, false);
          membershipCount++;
          if (membershipCount >= AZURE_MEMBERSHIPS_TO_CREATE) {
            break OUTER;
          }
        }
        userIndex++;
        if (userIndex>=AZURE_USERS_TO_CREATE) {
          userIndex = 0;
        }
      }
      loopCount++;
    }
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_azureGroupType", "security");
//    metadataNameValues.put("md_grouper_welcomeEmailDisabled", true);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    fullProvision();
    GrouperUtil.sleep(10000);
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
    assertEquals(AZURE_GROUPS_TO_CREATE, azureGroups.size());
    
    azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    assertTrue(""+azureUsers.size(), azureUsers.size() >= AZURE_USERS_TO_CREATE);

    membershipCount = 0;
    for (GrouperAzureGroup azureGroup: azureGroups) {
      Set<String> groupMembers = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", azureGroup.getId());
      membershipCount += groupMembers.size();
    }

    assertEquals(AZURE_MEMBERSHIPS_TO_CREATE, membershipCount);
    
    // now delete half the members
    membershipCount = 0;
    userIndex = 0;
    loopCount = 0;
    OUTER: while(true) {
      userIndex = loopCount;
      for (Group testGroup: groups) {
        
        Subject fred = SubjectFinder.findById("Fred"+userIndex+"@" + domain, true);
        if (testGroup.hasMember(fred)) {
          testGroup.deleteMember(fred, false);
          membershipCount++;
          if (membershipCount >= AZURE_MEMBERSHIPS_TO_CREATE/2) {
            break OUTER;
          }
        }
        userIndex++;
        if (userIndex>=AZURE_USERS_TO_CREATE) {
          userIndex = 0;
        }
      }
      loopCount++;
    }

    fullProvision();
    GrouperUtil.sleep(10000);
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
    assertTrue(azureGroups.size() == AZURE_GROUPS_TO_CREATE);
    
    azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    assertTrue(azureUsers.size()+"", azureUsers.size() >= AZURE_USERS_TO_CREATE);

    membershipCount = 0;
    for (GrouperAzureGroup azureGroup: azureGroups) {
      Set<String> groupMembers = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", azureGroup.getId());
      membershipCount += groupMembers.size();
    }

    assertEquals(AZURE_MEMBERSHIPS_TO_CREATE/2, membershipCount);
    
    //delete all the groups
    for (Group testGroup: groups) {
      testGroup.delete();
    }
    fullProvision();
    GrouperUtil.sleep(10000);
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
    assertTrue(azureGroups.size() == 0);

    azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    // maybe there are a few there but not a lot
    assertEquals(initialUserSize, azureUsers.size());

  }

  public void udelHelper(boolean isFull) {

    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
   
    RegistrySubject.add(grouperSession, "Fred400@" + domain, "person", "Fred400@" + domain);
    Subject fred = SubjectFinder.findById("Fred400@" + domain, true);
    
    RegistrySubject.add(grouperSession, "Fred401@" + domain, "person", "Fred401@" + domain);
    Subject fred1 = SubjectFinder.findById("Fred401@" + domain, true);
    
    RegistrySubject.add(grouperSession, "Fred402@" + domain, "person", "Fred402@" + domain);
    Subject fred2 = SubjectFinder.findById("Fred402@" + domain, true);
    
    RegistrySubject.add(grouperSession, "Fred403@" + domain, "person", "Fred403@" + domain);
    Subject fred3 = SubjectFinder.findById("Fred403@" + domain, true);
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
        .assignRealAzure(false).assignUdelUseCase(true)
        .assignDisplayNameMapping("extension").addExtraConfig("azureGroupType", "true").addExtraConfig("makeChangesToEntities", "true")
        .addExtraConfig("welcomeEmailDisabled", "true")
        );
        
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }

    // this will create tables
    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    
    testGroup.addMember(fred, false);
    testGroup.addMember(fred1, false);
    
    testGroup2.addMember(fred2, false);
    testGroup2.addMember(fred3, false);
    
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_azureGroupType", "security");
//    metadataNameValues.put("md_grouper_welcomeEmailDisabled", true);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    //lets sync these over
    
    List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName");
    if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
      GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
      GrouperUtil.sleep(10000);
    }
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertTrue(1 <= grouperProvisioningOutput.getInsert());
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName");
    assertNotNull(grouperAzureGroups1);

    List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    List<GrouperAzureUser> grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertTrue(grouperAzureUsers1.size() > 0);

    Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(2, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    assertTrue(userIds.contains(grouperAzureUsers1.get(0).getId()));
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      assertTrue(provisioningGroupWrapper.isRecalcObject());
      // ? should this be here?
      assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      assertTrue(provisioningEntityWrapper.isRecalcObject());

      // ? should this be here?
      assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (isFull) {
        assertTrue(provisioningMembershipWrapper.isRecalcObject());
      }
    }
    
    GrouperAzureGroup grouperAzureGroup = grouperAzureGroups1.get(0);
    assertEquals("test0", grouperAzureGroup.getDisplayName());
    assertEquals("F", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
    assertEquals("F", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
    assertEquals("F", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
    assertEquals(1, gcGrouperSync.getGroupCount().intValue());
    
    GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
    assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
    assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
    assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
    
    
    //now remove one of the subjects from the testGroup
    testGroup.deleteMember(fred1);
    
    // now run the full sync again and the member should be deleted from mock_azure_membership also
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName");
    assertNotNull(grouperAzureGroups);
    assertTrue(grouperAzureGroups.size() > 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertTrue(grouperAzureUsers.size() == 0);
    
    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroup.getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    
    //now add one subject
    testGroup.addMember(fred3);
    
    // now run the full sync again
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName");
    assertNotNull(grouperAzureGroups);
    assertTrue(grouperAzureGroups.size() > 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertTrue(grouperAzureUsers1.size() > 0);
    List<GrouperAzureUser> grouperAzureUsers3 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred3.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers3);
    assertTrue(grouperAzureUsers3.size() > 0);

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroup.getId());
    assertEquals(2, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    assertTrue(userIds.contains(grouperAzureUsers3.get(0).getId()));

    //now delete the group and sync again
    testGroup.delete();
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName");
    assertTrue(grouperAzureGroups.size() == 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() == 0);
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertTrue(grouperAzureUsers1.size() == 0);
    grouperAzureUsers3 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred3.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers3);
    assertTrue(grouperAzureUsers3.size() == 0);
    
  }
  
  public void testFullSyncAzure() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput().assignGroupAttributeCount(5));
            
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
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_allowOnlyMembersToPost", true);
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.isRecalcObject());

        // ? should this be here?
        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.isRecalcObject());
      }
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_azure_membership also
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAll")));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllGroups")));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllEntities")));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllMemberships")));
      
    } finally {
      
    }

  }
  
  public void testFullSyncAzureExternal() {
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      RegistrySubject.add(grouperSession, "Fred@erviveksachdevagrouperoutlo.onmicrosoft.com", "person", "Fred@erviveksachdevagrouperoutlo.onmicrosoft.com");
      Subject fred = SubjectFinder.findById("Fred@erviveksachdevagrouperoutlo.onmicrosoft.com", true);
      
//      RegistrySubject.add(grouperSession, "Fred1@erviveksachdevagrouperoutlo.onmicrosoft.com", "person", "Fred1@erviveksachdevagrouperoutlo.onmicrosoft.com");
//      Subject fred1 = SubjectFinder.findById("Fred1@erviveksachdevagrouperoutlo.onmicrosoft.com", true);
//      
      RegistrySubject.add(grouperSession, "Fred2@erviveksachdevagrouperoutlo.onmicrosoft.com", "person", "Fred2@erviveksachdevagrouperoutlo.onmicrosoft.com");
      Subject fred2 = SubjectFinder.findById("Fred2@erviveksachdevagrouperoutlo.onmicrosoft.com", true);
//      
//      RegistrySubject.add(grouperSession, "Fred3@erviveksachdevagrouperoutlo.onmicrosoft.com", "person", "Fred3@erviveksachdevagrouperoutlo.onmicrosoft.com");
//      Subject fred3 = SubjectFinder.findById("Fred3@erviveksachdevagrouperoutlo.onmicrosoft.com", true);
      
      
      List<Subject> subjects = new ArrayList<>();
      
      for (int i=0; i<900; i++) {
        RegistrySubject.add(grouperSession, "Chris"+i+"@erviveksachdevagrouperoutlo.onmicrosoft.com", "person", "Chris"+i+"@erviveksachdevagrouperoutlo.onmicrosoft.com");
        Subject fred1 = SubjectFinder.findById("Chris"+i+"@erviveksachdevagrouperoutlo.onmicrosoft.com", true);
        subjects.add(fred1);
      }
      
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput()
          .assignRealAzure(false)
          .assignGroupAttributeCount(5));
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      for (Subject subj: subjects) {
        testGroup.addMember(subj, false);
      }
      
      testGroup.addMember(fred, false);
//      testGroup.addMember(fred1, false);
      
//      testGroup2.addMember(fred2, false);
//      testGroup2.addMember(fred3, false);
      
//      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
//      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
//      
//      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
//      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myAzureProvisioner");
      attributeValue.setTargetName("myAzureProvisioner");
      attributeValue.setStemScopeString("sub");
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_allowOnlyMembersToPost", true);
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
//      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
//  
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
//      assertTrue(1 <= grouperProvisioningOutput.getInsert());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
//      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
//      
//      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
//        assertTrue(provisioningGroupWrapper.isRecalcObject());
//        // ? should this be here?
//        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
//      }
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
//      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
//        assertTrue(provisioningEntityWrapper.isRecalcObject());
//
//        // ? should this be here?
//        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
//      }
//      
//      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
//      
//      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
//        assertTrue(provisioningMembershipWrapper.isRecalcObject());
//      }
//      
//      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
//      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
//      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
//      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
//      
//      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
//      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
//      
//      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
//      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
//      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
//      assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(fred);
      
      // now run the full sync again and the member should be deleted from mock_azure_membership also
      
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now add one subject
      testGroup.addMember(fred2);
      
      // now run the full sync again
//      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();

//      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
//      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
//      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
//      
      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
//      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
    } finally {
      
    }


  }
  
  public void testFullSyncAzureDisplayName() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput().assignGroupAttributeCount(5).assignDisplayNameMapping("displayName")
          .addExtraConfig("allowOnlyMembersToPost", "true")
          .addExtraConfig("resourceProvisioningOptionsTeam", "true"));
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
      
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
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_allowOnlyMembersToPost", true);
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.isRecalcObject());
        
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.isRecalcObject());
      }
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_azure_membership also
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      // now edit the group
      testGroup = new GroupSave().assignUuid(testGroup.getUuid()).assignDisplayExtension("newDisplayExtension").assignReplaceAllSettings(false).save();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      GrouperAzureGroup azureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      assertEquals("test:newDisplayExtension", azureGroup.getDisplayName());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
    } finally {
      
    }

  }
  
  public void testIncrementalSyncAzure() {
    
    if (!tomcatRunTests()) {
      return;
    }

    
    AzureProvisionerTestUtils.configureAzureProvisioner(
       new AzureProvisionerTestConfigInput());

    GrouperStartup.startup();
    

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      fullProvision();
      
      incrementalProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput(); 

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
  
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      incrementalProvision();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      incrementalProvision();
      
      //assertEquals(1, grouperProvisioningOutput.getDelete());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
  public void testFullSyncAzureGroupType() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput().assignGroupAttributeCount(5)
            .addExtraConfig("azureGroupType", "true")
            .addExtraConfig("allowOnlyMembersToPost", "true")
            .addExtraConfig("assignableToRole", "true")
            .addExtraConfig("resourceProvisioningOptionsTeam", "true"));
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myAzureProvisioner");
      attributeValue.setTargetName("myAzureProvisioner");
      attributeValue.setStemScopeString("sub");
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_allowOnlyMembersToPost", true);
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      metadataNameValues.put("md_grouper_azureGroupType", "security");
      metadataNameValues.put("md_grouper_assignableToRole", true);
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.isRecalcObject());
      }
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      assertTrue(grouperAzureGroup.isSecurityEnabled());
      assertFalse(grouperAzureGroup.isMailEnabled());
      assertFalse(grouperAzureGroup.isGroupTypeUnified());
      assertFalse(grouperAzureGroup.isGroupTypeDynamic());
      assertTrue(grouperAzureGroup.isAssignableToRole());
      
    } finally {
      
    }

  }

  public void testFullSyncAzureDontSelectAll() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5)
          .addExtraConfig("selectAllGroups", "false")
          );
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure");
  
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
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_grouper_allowOnlyMembersToPost", true);
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        assertTrue(provisioningGroupWrapper.isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.isRecalcObject());
  
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.isRecalcObject());
      }
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "myAzureProvisioner");
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());
      
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      assertEquals(testGroup.getId(), gcGrouperSyncGroup.getGroupId());
      assertEquals(testGroup.getName(), gcGrouperSyncGroup.getGroupName());
      assertEquals(grouperAzureGroup.getId(), gcGrouperSyncGroup.getGroupAttributeValueCache2());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_azure_membership also
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now add one subject
      testGroup.addMember(SubjectTestHelper.SUBJ3);
      
      // now run the full sync again
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
  
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      //now delete the group and sync again
      testGroup.delete();
      
      grouperProvisioningOutput = fullProvision();
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAll")));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllGroups")));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllEntities")));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllMemberships")));

    } finally {
      
    }
  
  }
  
}
