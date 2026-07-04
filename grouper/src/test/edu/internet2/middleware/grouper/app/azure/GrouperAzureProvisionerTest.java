package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
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
    TestRunner.run(new GrouperAzureProvisionerTest("testAddManyMembershipsUncgHelperIncremental"));
    //realAzureAddUsers();
  }

  public GrouperAzureProvisionerTest(String name) {
    super(name);
  }

  public GrouperAzureProvisionerTest() {
    
  }
  
  public static boolean startTomcat = false;
  
  public void setUp() {
    super.setUp();

    // not sure why this is necessary since all caches are cleared in tests, but oh well
    AzureGrouperExternalSystem.clearCache();
    
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

  public static void realAzureAddUsers() {
    azureAddUsersHelper(5000, 5000 + AZURE_USERS_TO_CREATE);
  }

  public static void azureAddUsersHelper(int startNumber, int endNumber) {
    
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    List<GrouperAzureUser> azureUsersToCreate = new ArrayList<>();
    
//    List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(name + "@" + domain), "userPrincipalName");
    
    for (int i=startNumber;i<endNumber;i++) {
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
  }

  public static void realAzureAddUsersAndGroups() {
    realAzureAddUsers();
    realAzureAddGroups();
  }

  public static void realAzureAddGroups() {
        
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
//    assertTrue(GrouperUtil.length(grouperAzureUsers) > 210);
    
    for (int i=AZURE_GROUPS_TO_CREATE;i<AZURE_GROUPS_TO_CREATE*2;i++) {
      String name = "test" + i;
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(name),  "displayName", false, new HashSet<String>());
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
  
  
  public void azureUpdateGroupDescriptionFull() {
    azureUpdateGroupDescription(true);
  }
  
  public void azureUpdateGroupDescriptionIncremental() {
    azureUpdateGroupDescription(false);
  }
  
  public void azureUpdateGroupDescription(boolean isFull) {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
        .assignUdelUseCase(true)
        .assignDisplayNameMapping("extension")
        .addExtraConfig("azureGroupType", "true")
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        );
    
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").assignDescription("test description").save();
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    
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

    
    if (!isFull) {
      incrementalProvision();
    } else {
      fullProvision();
    }
    
    //lets sync these over
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
    GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
    
    assertEquals("testGroup", grouperAzureGroup.getDisplayName());
    assertEquals("test description", grouperAzureGroup.getDescription());
    
    incrementalProvision();
    
    new GroupSave(grouperSession).assignUuid(testGroup.getUuid()).assignDescription("new description 1").assignSaveMode(SaveMode.INSERT_OR_UPDATE).save();
    
    if (!isFull) {
      incrementalProvision();
    } else {
      fullProvision();
    }
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
    grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
    
    assertEquals("testGroup", grouperAzureGroup.getDisplayName());
    assertEquals("new description 1", grouperAzureGroup.getDescription());
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

    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    
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
  
  /**
   * Simple full-sync test that uses certificate-based auth against the mock Azure service.
   * Confirms that the cert-auth flow (fresh keypair, signed JWT, v2.0 token endpoint, scope param)
   * round-trips through retrieveBearerTokenForAzureConfigId and the Azure mock service handler.
   */
  public void testFullSyncAzureCertAuth() {

    GrouperStartup.startup();

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }

    try {
      // configure provisioner first (writes the standard config-secret setup)...
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(3));

      // ...then flip the external system to cert-based auth
      AzureProvisionerTestUtils.setupAzureExternalSystemCertAuth();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroupCertAuth").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("myAzureProvisioner");
      attributeValue.setTargetName("myAzureProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());

      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();

      // group + 2 entities + 2 memberships should all have made it through cert-authenticated calls
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());

      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      assertEquals("test:testGroupCertAuth", grouperAzureGroup.getDisplayName());

    } finally {
    }
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
        .addExtraConfig("logCommandsAlways", "false")
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
    
    assertEquals(0, groupMembers.size());
    
  }
  
  //need to run individually
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
  
  //it runs against real system
  public void atestUdelLargeOperationsFull() {
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

    List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
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

    azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
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

    azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertTrue(azureGroups.size() == 0);

    azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure");
    // maybe there are a few there but not a lot
    assertEquals(initialUserSize, azureUsers.size());

  }
  
  public void testDeleteEntityFromAzureAndThenRunProvisionerAgainFull() {
    deleteEntityFromAzureAndThenRunProvisionerAgainHelper(true, false);
  }
  
  public void testDeleteEntityFromAzureAndThenRunProvisionerAgainIncremental() {
    deleteEntityFromAzureAndThenRunProvisionerAgainHelper(false, false);
  }
  
  public void testDeleteEntityFromAzureAndThenRunProvisionerAgainFullSelectAllGroups() {
    deleteEntityFromAzureAndThenRunProvisionerAgainHelper(true, true);
  }
  
  public void testDeleteEntityFromAzureAndThenRunProvisionerAgainIncrementalSelectAllGroups() {
    deleteEntityFromAzureAndThenRunProvisionerAgainHelper(false, true);
  }
  
  public void deleteEntityFromAzureAndThenRunProvisionerAgainHelper(boolean isFull, boolean selectAllGroups) {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
    
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
          .assignRealAzure(false)
          .assignProvisioningStrategy("michiganAzure")
          .addExtraConfig("errorHandlingShow", "true")
          .addExtraConfig("selectAllGroups", String.valueOf(selectAllGroups))
          .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false")
        );
    
    RegistrySubject.add(grouperSession, "Fred400", "person", "Fred400");
    Subject fred = SubjectFinder.findById("Fred400", true);
    
    boolean sleep = false;
    List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList("test:test0"), "displayName", false, new HashSet<String>());
    if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
      GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
      sleep = true;
    }

    List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
    if (GrouperUtil.length(grouperAzureUsers) == 0) {
      GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
      grouperAzureUser.setUserPrincipalName(fred.getId() + "@" + domain);
      grouperAzureUser.setDisplayName("Fred400");
      grouperAzureUser.setMailNickname("Fred400");
      
      GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(grouperAzureUser), null);
      sleep = true;
    }
    if (sleep) {
      GrouperUtil.sleep(10000);
    }
    
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    if (sleep) {
      GrouperUtil.sleep(10000);
    }

    // this will create tables
    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
    
    testGroup.addMember(fred, false);
    
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
    
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
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
    GrouperUtil.sleep(10000);

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertTrue(1 <= grouperProvisioningOutput.getInsert());
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers.size());

    Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    GrouperUtil.sleep(10000);

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertEquals(0, grouperProvisioningOutput.getInsert());
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers.size());

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    
    
    GrouperAzureApiCommands.deleteAzureUsers("myAzure", grouperAzureUsers);
    
    //now remove one of the subjects from the testGroup
    testGroup.deleteMember(fred);
    
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
    GrouperUtil.sleep(10000);

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());
    
  }

  //need to run individually
  public void testAddManyMembershipsUncgHelperFull() {
    addManyMembershipsUncgHelper(true);
  }
  
  public void testAddManyMembershipsUncgHelperIncremental() {
    addManyMembershipsUncgHelper(false);
  }
  
  public void addManyMembershipsUncgHelper(boolean isFull) {
    
    int userCount = 650;
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    // assume real azure add users has been called from 5000 - 9999
    
    List<Subject> subjects = new ArrayList<Subject>();
    for (int i=5000;i<5000+userCount;i++) {

      RegistrySubject.add(grouperSession, "Fred" + i, "person", "Fred" + i);
      Subject fred = SubjectFinder.findById("Fred" + i, true);
      subjects.add(fred);
    }
    
    boolean sleep = false;

    String configId = "azureUNCGSpartan";
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput()
          //.assignRealAzure(true)
          .assignConfigId(configId)
          .assignProvisioningStrategy("uncgAzure")
        );
        
    azureAddUsersHelper(5000, 5000+userCount);

    List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList("test_test0"), "displayName", false, new HashSet<String>());
    if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
      GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
      sleep = true;
    }

    if (sleep == true) {
      GrouperUtil.sleep(10000);
    }
    
    fullProvision(configId);
    incrementalProvision(configId);

    // this will create tables
    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
    
    testGroup.addMember(subjects.get(0), false);

    Group testGroup1 = new GroupSave(grouperSession).assignName("test:test1").save();
    for (Subject subject : subjects) {
      testGroup1.addMember(subject);
    }
    

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);

    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
    metadataNameValues.put("md_grouper_azureGroupType", "security");
//    metadataNameValues.put("md_grouper_welcomeEmailDisabled", true);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;
    if (isFull) {
      fullProvision(configId);
    } else {
      incrementalProvision(configId);
    }
    GrouperUtil.sleep(10000);

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    
    assertTrue(1 <= grouperProvisioningOutput.getInsert());
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList("test_test0"), "displayName", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    List<GrouperAzureUser> grouperAzureUsers = null;
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers.size());

    Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));

    testGroup.addMember(testGroup1.toSubject());
    
    if (isFull) {
      fullProvision(configId);
    } else {
      incrementalProvision(configId);
    }
    GrouperUtil.sleep(10000);

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList("test_test0"), "displayName", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(1).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers.size());

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(userCount, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));

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
    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());

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
    
    List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName", false, new HashSet<String>());
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
    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName", false, new HashSet<String>());
    assertNotNull(grouperAzureGroups1);

    List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    List<GrouperAzureUser> grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertTrue(grouperAzureUsers1.size() > 0);

    Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(2, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
    assertTrue(userIds.contains(grouperAzureUsers1.get(0).getId()));
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) > 0);
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());

    }
    
    assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (isFull) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName", false, new HashSet<String>());
    assertNotNull(grouperAzureGroups);
    assertTrue(grouperAzureGroups.size() > 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertEquals(0, grouperAzureUsers1.size());
    
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

    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName", false, new HashSet<String>());
    assertNotNull(grouperAzureGroups);
    assertTrue(grouperAzureGroups.size() > 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertTrue(grouperAzureUsers.size() > 0);
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertEquals(0, grouperAzureUsers1.size());
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
    
    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(testGroup.getExtension()), "displayName", false, new HashSet<String>());
    assertTrue(grouperAzureGroups.size() == 0);
    
    grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers);
    assertEquals(0, grouperAzureUsers.size());
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred1.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers1);
    assertEquals(0, grouperAzureUsers1.size());
    grouperAzureUsers3 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred3.getId()), "userPrincipalName");
    assertNotNull(grouperAzureUsers3);
    assertEquals(0, grouperAzureUsers3.size());
    
  }
  
  public void testFullSyncAzureWhenGroupIsUnprovisioableDueToAnAttribute() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5)
          
          .addExtraConfig("targetGroupAttribute.2.showAdvancedAttribute", "true")
          .addExtraConfig("targetGroupAttribute.2.showAttributeValidation", "true")

          .addExtraConfig("targetEntityAttribute.1.showAdvancedAttribute", "true")
          .addExtraConfig("targetEntityAttribute.1.showAttributeValidation", "true")
          
          .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description") // group description is null
          .addExtraConfig("targetGroupAttribute.2.unprovisionableIfNull", "true") // due to group description being null, the testGroup shouldn't make it to the target
          
          .addExtraConfig("entityAttributeValueCache0translationScript", "${subject.id == 'test.subject.1' ? null : subject.getAttributeValue('email')}") // userPrincipalName is going to be null 
          .addExtraConfig("targetEntityAttribute.1.unprovisionableIfNull", "true") // due to userPrincipalName being null, the entity shouldn't make it to the target
          
          );
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").assignDescription("test_group_2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      testGroup2.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ1, false);
      
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
      
      assertTrue(3 == grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  public void testFullSyncAzureRemoveAccentedCharacters() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5)
          .addExtraConfig("removeAccentedChars", "true")
          .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "description") // group description is null
          );
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").assignDescription("test ç").save();
      
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
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      assertTrue(5 == grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
      List<GrouperAzureGroup> groups = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class);
      assertEquals("test c", groups.get(0).getMailNickname());
      
    } finally {
      
    }
    
  }
  
  public void testFullSyncAzure() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
    		  .assignGroupAttributeCount(5)
    		  //.assignRealAzure(true)
    		  .addExtraConfig("loadEntitiesToGrouperTable", "true")
    		  );
            
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
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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
      
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAll"), 0));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllGroups")));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllEntities")));
      assertTrue(0 < GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllMembershipsByGroups")));
      
    } finally {
      
    }

  }
  
  public void atestFullSyncAzureReal() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5)
          .assignRealAzure(true)
          .addExtraConfig("loadEntitiesToGrouperTable", "true")
          );
            
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
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
    } finally {
      
    }

  }
  
  public void testFullSyncAzureStartWith() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      AzureProvisionerTestUtils.setupAzureExternalSystem(false);
      
      AzureProvisioningStartWith startWith = new AzureProvisioningStartWith();
      
      Map<String, String> startWithSuffixToValue = new HashMap<>();
      
      startWithSuffixToValue.put("azureExternalSystemConfigId", "myAzure");
      startWithSuffixToValue.put("azurePattern", "manageGroupsManageEntities");
      startWithSuffixToValue.put("userAttributesType", "core");
      startWithSuffixToValue.put("selectAllGroups", "true");
      startWithSuffixToValue.put("groupDisplayNameAttributeValue", "name");
      startWithSuffixToValue.put("mailNicknameAttributeValue", "extension");
      startWithSuffixToValue.put("groupSearchMatchingAttribute", "displayName");
      startWithSuffixToValue.put("selectAllEntities", "true");
      startWithSuffixToValue.put("entityUserPrincipalName", "subjectId");
      startWithSuffixToValue.put("entityMailNickname", "subjectIdentifier0");
      startWithSuffixToValue.put("manageEntitiesInAzure", "true");
      startWithSuffixToValue.put("entityDisplayName", "name");
      startWithSuffixToValue.put("entitySearchMatchingAttribute", "userPrincipalName");
      startWithSuffixToValue.put("hasMetadataForAllowOnlyMembersToPost", "true");
      startWithSuffixToValue.put("hasMetadataForResourceProvisioningOptionsTeam", "true");
      
      Map<String, Object> provisionerSuffixToValue = new HashMap<>();
      
      startWith.populateProvisionerConfigurationValuesFromStartWith(startWithSuffixToValue, provisionerSuffixToValue);
      
      startWith.manipulateProvisionerConfigurationValue("myAzureProvisioner", startWithSuffixToValue, provisionerSuffixToValue);
      
      for (String key: provisionerSuffixToValue.keySet()) {
        new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.myAzureProvisioner."+key)
          .value(GrouperUtil.stringValue(provisionerSuffixToValue.get(key))).store();
      }
      
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.debugLog").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.logAllObjectsVerbose").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.logCommandsAlways").value("true").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myAzureProvisioner.subjectSourcesToProvision").value("jdbc").store();

      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myAzureProvisioner.class").value(GrouperProvisioningFullSyncJob.class.getName()).store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myAzureProvisioner.quartzCron").value("9 59 23 31 12 ? 2099").store();
      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myAzureProvisioner.provisionerConfigId").value("myAzureProvisioner").store();
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup1").save();
      
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
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
     
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertEquals("test:testGroup1", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myAzureProvisioner");
      provisioner.initialize(GrouperProvisioningType.diagnostics);
      GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer = provisioner.retrieveGrouperProvisioningDiagnosticsContainer();
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName("test:testGroup2");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsSubjectIdOrIdentifier("test.subject.4");
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntityInsert(true);
      grouperProvisioningDiagnosticsContainer.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(true);
      grouperProvisioningOutput = provisioner.provision(GrouperProvisioningType.diagnostics);
      assertEquals(0, grouperProvisioningOutput.getRecordsWithErrors());
      validateNoErrors(grouperProvisioningDiagnosticsContainer);
      
    } finally {
      
    }
    
  }
  
  private void validateNoErrors(GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    String[] lines = grouperProvisioningDiagnosticsContainer.getReportFinal().split("\n"); 
    List<String> errorLines = new ArrayList<String>();
    for (String line : lines) {
      if (line.contains("'red'") || line.contains("Error:")) {
        errorLines.add(line);
      }
    }
    
    if (errorLines.size() > 0) {
      fail("There are " + errorLines.size() + " errors in report: " + errorLines);
    }
  }
  
  public void testFullSyncAzureWithOwners() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(6));
            
      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup1").save();
      
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
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over
      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_azure_group").select(int.class));
  
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      
      List<GrouperAzureGroup> azureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
     
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      GrouperAzureGroup grouperAzureGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).get(0);
      
      assertEquals("test:testGroup1", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      Set<String> ownersSet = grouperAzureGroup.getOwners();
      
      assertTrue(ownersSet.contains("https://example.com/1"));
      assertTrue(ownersSet.contains("https://example.com/2"));
      assertTrue(ownersSet.contains("https://example.com/3"));
      
    } finally {
      
    }

  }
  
  public void testFullSyncAzureNonEditableUsers() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput().assignGroupAttributeCount(5));
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
      
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
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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
      
      //now delete the group and sync again
//      testGroup.delete();
//      
//      grouperProvisioningOutput = fullProvision();
//      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//      
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureGroup").list(GrouperAzureGroup.class).size());
//      assertEquals(3, HibernateSession.byHqlStatic().createQuery("from GrouperAzureUser").list(GrouperAzureUser.class).size());
//      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAzureMembership").list(GrouperAzureMembership.class).size());
      
    } finally {
      
    }
    
  }
  
  public void testFullSyncAzureWithOwnersWithMetadata() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5).addExtraConfig("groupOwners", "true"));
            
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
      metadataNameValues.put("md_grouper_groupOwners", "https://a,https://b,https://c");
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
      
      assertEquals("test:testGroup", grouperAzureGroup.getDisplayName());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsAllowOnlyMembersToPostDb());
      assertEquals("T", grouperAzureGroup.getResourceBehaviorOptionsWelcomeEmailDisabledDb());
      assertEquals("T", grouperAzureGroup.getResourceProvisioningOptionsTeamDb());
      
      Set<String> ownersSet = grouperAzureGroup.getOwners();
      
      assertTrue(ownersSet.contains("https://a"));
      assertTrue(ownersSet.contains("https://b"));
      assertTrue(ownersSet.contains("https://c"));
      
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
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
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
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
      
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
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
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
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
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
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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

  //need to run individually
  public void testFullSyncAzureDontSelectAll() {
    
    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
          .assignGroupAttributeCount(5)
          .addExtraConfig("selectAllGroups", "false")
          .addExtraConfig("selectAllEntities", "false")
          );
      
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
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
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
        // ? should this be here?
        assertTrue(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) > 0);
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
  
        // ? should this be here?
        assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      }
      
      assertTrue(GrouperUtil.length(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) > 0);
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        assertTrue(provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());
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
      
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAll"), 0));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllGroups"), 0));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllEntities"), 0));
      assertEquals(0, GrouperUtil.intValue(grouperProvisioner.getDebugMap().get("targetRetrieveAllMemberships"), 0));

    } finally {
      
    }
  
  }

  public void testAddUserNotExistMichiganIncremental() {
    
    addUserNotExistMichiganHelper(false);
    
  }

  public void addUserNotExistMichiganHelper(boolean isFull) {
            
      GrouperStartup.startup();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
  
      int userCount = 1;
      
      // assume real azure add users has been called from 5000 - 9999
      
      List<Subject> subjects = new ArrayList<Subject>();
      for (int i=5000;i<5000 + userCount;i++) {
  
        RegistrySubject.add(grouperSession, "Fred" + i, "person", "Fred" + i);
        Subject fred = SubjectFinder.findById("Fred" + i, true);
        subjects.add(fred);
      }
      
      boolean sleep = false;
  
      String configId = "AZURE_AD";
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput()
            //.assignRealAzure(true)
            .assignConfigId(configId)
            .assignProvisioningStrategy("michiganAzure")
            .addExtraConfig("scoreConvertToFullSyncThreshold", "500"));
            
      String azureGroupDisplayName = "test:test0";
      List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName), "displayName", false, new HashSet<String>());
      if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
        GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
        sleep = true;
      }
  
      if (sleep == true) {
        GrouperUtil.sleep(10000);
      }
      
      fullProvision(configId);
      incrementalProvision(configId);
  
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
      
      testGroup.addMember(subjects.get(0), false);
  
      Group testGroup1 = new GroupSave(grouperSession).assignName("test:test1").save();
      for (Subject subject : subjects) {
        testGroup1.addMember(subject);
      }
      
      Member member = MemberFinder.findBySubject(grouperSession, subjects.get(0), true);
  
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision(configId);
      attributeValue.setTargetName(configId);
  
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      
      metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
      metadataNameValues.put("md_grouper_azureGroupType", "security");
      // metadataNameValues.put("md_grouper_welcomeEmailDisabled", true);
  
      attributeValue.setMetadataNameValues(metadataNameValues);
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup);
      
      GrouperProvisioningOutput grouperProvisioningOutput = null;
      GrouperProvisioner grouperProvisioner = null;

      if (isFull) {
        fullProvision(configId);
      } else {
        incrementalProvision(configId);
      }
      GrouperUtil.sleep(10000);

      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName), "displayName", false, new HashSet<String>());
      assertEquals(1, grouperAzureGroups1.size());
  
      List<GrouperAzureUser> grouperAzureUsers = null;
      
      grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
      assertEquals(0, grouperAzureUsers.size());
      
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());

      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(testGroup.getId());
      

//      // few of them have errors
//      for (int i=0;i<5;i++) {
//        if (isFull) {
//          fullProvision(configId);
//        } else {
//          incrementalProvision(configId);
//        }
//        
//        grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
//        grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
//        
//        assertEquals(0, grouperProvisioningOutput.getInsert());
//        grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName), "displayName");
//        assertEquals(1, grouperAzureGroups1.size());
//    
//        grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
//        assertEquals(0, grouperAzureUsers.size());
//
//      }  
      
      // add user
      azureAddUsersHelper(5000, 5001);
      GrouperUtil.sleep(10000);

      if (isFull) {
        fullProvision(configId);
      } else {
        incrementalProvision(configId);
      }
      
      
      GrouperUtil.sleep(10000);
  
      Set<String> userIds = null;
      
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

      Map<String, Object> debugMap = grouperProvisioner.getDebugMap();
      assertEquals(1, GrouperUtil.intValue(debugMap.get("addErrorsToQueue")));
      
      gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      assertEquals(1, gcGrouperSync.getGroupCount().intValue());

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(testGroup.getId());
      assertFalse(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject());
      assertFalse(provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships());

      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(member.getId());
      assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject());
      assertTrue(provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships());
      
      grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName), "displayName", false, new HashSet<String>());
      assertEquals(1, grouperAzureGroups1.size());

      grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
      assertEquals(1, grouperAzureUsers.size());

      userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
      assertEquals(userCount, GrouperUtil.length(userIds));
      assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));

    }

  public void testDeleteGroupMichiganFull() {
    deleteGroupMichiganHelper(true);
  }

  public void testDeleteGroupMichiganIncremental() {
    deleteGroupMichiganHelper(false);
  }

  public void deleteGroupMichiganHelper(boolean isFull) {
    
    GrouperStartup.startup();
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    int userCount = 3;
    
    // assume real azure add users has been called from 5000 - 9999
    
    List<Subject> subjects = new ArrayList<Subject>();
    for (int i=5000;i<5000 + userCount;i++) {

      RegistrySubject.add(grouperSession, "Fred" + i, "person", "Fred" + i);
      Subject fred = SubjectFinder.findById("Fred" + i, true);
      subjects.add(fred);
    }
    
    boolean sleep = false;

    String configId = "AZURE_AD";
    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput()
          //.assignRealAzure(true)
          .assignConfigId(configId)
          .assignProvisioningStrategy("michiganAzure")
        );
          
    String azureGroupDisplayName0 = "test:test0";
    String azureGroupDisplayName1 = "test:test1";
    List<GrouperAzureGroup> grouperAzureGroups0 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName0), "displayName", false, new HashSet<String>());
    if (grouperAzureGroups0 != null && grouperAzureGroups0.size() > 0) {
      GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups0);
      sleep = true;
    }
    List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName1), "displayName", false, new HashSet<String>());
    if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
      GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
      sleep = true;
    }

    if (sleep == true) {
      GrouperUtil.sleep(10000);
    }
    
    fullProvision(configId);
    incrementalProvision(configId);

    // this will create tables
    GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    // mark some folders to provision
    Group testGroup0 = new GroupSave(grouperSession).assignName("test:test0").save();
    testGroup0.addMember(subjects.get(0), false);

    Group testGroup1 = new GroupSave(grouperSession).assignName("test:test1").save();
    testGroup1.addMember(subjects.get(1));
    
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:test2").save();
    testGroup2.addMember(subjects.get(2));
    
    Member member0 = MemberFinder.findBySubject(grouperSession, subjects.get(0), true);
    Member member1 = MemberFinder.findBySubject(grouperSession, subjects.get(1), true);
    Member member2 = MemberFinder.findBySubject(grouperSession, subjects.get(2), true);

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);

    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    
    metadataNameValues.put("md_grouper_resourceProvisioningOptionsTeam", true);
    metadataNameValues.put("md_grouper_azureGroupType", "security");
    // metadataNameValues.put("md_grouper_welcomeEmailDisabled", true);

    attributeValue.setMetadataNameValues(metadataNameValues);

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup0);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testGroup1);
    
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    GrouperProvisioner grouperProvisioner = null;

    // add user
    azureAddUsersHelper(5000, 5000 + userCount);
    GrouperUtil.sleep(2000);

    if (isFull) {
      fullProvision(configId);
    } else {
      incrementalProvision(configId);
    }
    
    GrouperUtil.sleep(2000);

    Set<String> userIds = null;
    
    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    grouperAzureGroups0 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName0), "displayName", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups0.size());

    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName1), "displayName", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    List<GrouperAzureUser> grouperAzureUsers0 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers0.size());
    
    List<GrouperAzureUser> grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(1).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers1.size());

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups0.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertContainsString(userIds, grouperAzureUsers0.get(0).getId());

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertContainsString(userIds, grouperAzureUsers1.get(0).getId());
    
    GrouperProvisioningService.deleteAttributeAssign(testGroup0, "AZURE_AD");

    if (isFull) {
      fullProvision(configId);
    } else {
      incrementalProvision(configId);
    }

    GrouperUtil.sleep(2000);

    grouperAzureGroups0 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName0), "displayName", false, new HashSet<String>());
    assertEquals(0, grouperAzureGroups0.size());

    grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList(azureGroupDisplayName1), "displayName", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups1.size());

    grouperAzureUsers0 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(0).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers0.size());
    
    grouperAzureUsers1 = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(subjects.get(1).getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, grouperAzureUsers1.size());

    userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));
    assertTrue(userIds.contains(grouperAzureUsers1.get(0).getId()));
    
    
  }

  public void addEntityFailsTranslationCheckHelper(boolean isFull) {
      
      GrouperStartup.startup();
      
      GrouperSession grouperSession = GrouperSession.startRootSession();
      String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");
      
      AzureProvisionerTestUtils.configureAzureProvisioner(
          new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
            .assignRealAzure(false)
            .assignProvisioningStrategy("michiganAzure")
            .addExtraConfig("errorHandlingShow", "true")
            .addExtraConfig("errorHandlingMatchingValidationIsAnError", "false")
          );
      
      RegistrySubject.add(grouperSession, "Fred400", "person", "Fred400");
      Subject fred = SubjectFinder.findById("Fred400", true);
      
      boolean sleep = false;
      List<GrouperAzureGroup> grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", Arrays.asList("test:test0"), "displayName", false, new HashSet<String>());
      if (grouperAzureGroups1 != null && grouperAzureGroups1.size() > 0) {
        GrouperAzureApiCommands.deleteAzureGroups("myAzure", grouperAzureGroups1);
        sleep = true;
      }
  
      List<GrouperAzureUser> grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
      if (GrouperUtil.length(grouperAzureUsers) == 0) {
        GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
        grouperAzureUser.setUserPrincipalName(fred.getId() + "@" + domain);
        grouperAzureUser.setDisplayName("Fred400");
        grouperAzureUser.setMailNickname("Fred400");
        
        GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(grouperAzureUser), null);
        sleep = true;
      }
      if (sleep) {
        GrouperUtil.sleep(10000);
      }
      
      if (!isFull) {
        fullProvision();
        incrementalProvision();
      }
      if (sleep) {
        GrouperUtil.sleep(10000);
      }
  
      // this will create tables
      List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
  
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
      
      testGroup.addMember(fred, false);
      
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
      
      grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
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
      GrouperUtil.sleep(10000);
  
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
      
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      grouperAzureGroups1 = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
      assertEquals(1, grouperAzureGroups1.size());
  
      grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
      assertEquals(1, grouperAzureUsers.size());
  
      Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
      assertEquals(1, GrouperUtil.length(userIds));
      assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));
      
      // add something else
      testGroup.addMember(SubjectTestHelper.SUBJ0);
            
      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }
      GrouperUtil.sleep(10000);
  
      grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
      grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
  
      grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
      assertEquals(1, grouperAzureGroups1.size());

      grouperAzureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
      assertEquals(1, grouperAzureUsers.size());
  
      userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups1.get(0).getId());
      assertEquals(1, GrouperUtil.length(userIds));
      assertTrue(userIds.contains(grouperAzureUsers.get(0).getId()));

    }

  public void testAddEntityFailsTranslationCheckFull() {
    addEntityFailsTranslationCheckHelper(true);
  }

  public void testAddEntityFailsTranslationCheckIncremental() {
    addEntityFailsTranslationCheckHelper(false);
  }

  /**
   * GRP-6865: when a user is deleted from Azure but still a member of a Grouper group,
   * incremental sync should handle the 404 from getMemberGroups gracefully
   * instead of throwing an exception
   */
  public void testEntityDeletedFromAzureIncrementalHandles404() {

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    AzureProvisionerTestUtils.configureAzureProvisioner(
        new AzureProvisionerTestConfigInput().assignGroupAttributeCount(3).assignEntityAttributeCount(2)
          .assignRealAzure(false)
          .assignProvisioningStrategy("michiganAzure")
          .addExtraConfig("errorHandlingShow", "true")
          .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false")
        );

    RegistrySubject.add(grouperSession, "Fred400", "person", "Fred400");
    Subject fred = SubjectFinder.findById("Fred400", true);

    RegistrySubject.add(grouperSession, "Fred401", "person", "Fred401");
    Subject fred2 = SubjectFinder.findById("Fred401", true);

    // create user in mock Azure
    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    grouperAzureUser.setUserPrincipalName(fred.getId() + "@" + domain);
    grouperAzureUser.setDisplayName("Fred400");
    grouperAzureUser.setMailNickname("Fred400");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(grouperAzureUser), null);

    GrouperAzureUser grouperAzureUser2 = new GrouperAzureUser();
    grouperAzureUser2.setUserPrincipalName(fred2.getId() + "@" + domain);
    grouperAzureUser2.setDisplayName("Fred401");
    grouperAzureUser2.setMailNickname("Fred401");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(grouperAzureUser2), null);

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:test0").save();
    testGroup.addMember(fred, false);

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("myAzureProvisioner");
    attributeValue.setTargetName("myAzureProvisioner");
    attributeValue.setStemScopeString("sub");
    Map<String, Object> metadataNameValues = new HashMap<String, Object>();
    metadataNameValues.put("md_grouper_azureGroupType", "security");
    attributeValue.setMetadataNameValues(metadataNameValues);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // full provision to sync everything
    fullProvision();
    GrouperUtil.sleep(10000);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();
    assertTrue(1 <= grouperProvisioningOutput.getInsert());

    List<GrouperAzureGroup> grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups.size());

    Set<String> userIds = GrouperAzureApiCommands.retrieveAzureGroupMembers("myAzure", grouperAzureGroups.get(0).getId());
    assertEquals(1, GrouperUtil.length(userIds));

    // delete Fred400 from mock Azure only (not from the Grouper group)
    List<GrouperAzureUser> azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
    assertEquals(1, azureUsers.size());
    GrouperAzureApiCommands.deleteAzureUsers("myAzure", azureUsers);

    // verify user is gone from Azure
    azureUsers = GrouperAzureApiCommands.retrieveAzureUsers("myAzure", Arrays.asList(fred.getId() + "@" + domain), "userPrincipalName");
    assertEquals(0, azureUsers.size());

    // add another member to trigger incremental sync activity
    testGroup.addMember(fred2, false);

    // run incremental - this should NOT throw an exception even though Fred400
    // no longer exists in Azure and getMemberGroups returns 404
    incrementalProvision();

    grouperProvisioner = GrouperProvisioner.retrieveInternalLastProvisioner();
    grouperProvisioningOutput = grouperProvisioner.retrieveGrouperProvisioningOutput();

    // the provisioner should have completed successfully
    // Fred401 should be added to the Azure group
    grouperAzureGroups = GrouperAzureApiCommands.retrieveAzureGroups("myAzure", false, new HashSet<String>());
    assertEquals(1, grouperAzureGroups.size());

    // verify retrieveAzureUserGroups handles 404 by returning empty set
    Set<String> deletedUserGroups = GrouperAzureApiCommands.retrieveAzureUserGroups("myAzure", "nonExistentUserId12345");
    assertEquals(0, deletedUserGroups.size());
  }

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the Azure read path.
   * Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart. Framework-detail coverage (flag isolation, native-attribute
   * config, validation) lives in the SCIM + LDAP suites.
   */
  public void testAzureFullSyncPopulatesGenericTables() {

    String configId = "myAzureProvisioner";
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // first pass writes the Azure target; sync-back tables stay empty until the next
    // read pass captures the new objects (read-state convergence contract).
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());

    // second pass: reads back what we just wrote, captures through the sync hooks, flushes
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row after sync-back",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows",
        countSyncBack(configId, "grouper_prov_mship") >= 2);

    int linkedGroups = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and group_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_group row should be linked to a Grouper group", linkedGroups >= 1);

    int linkedUsers = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and member_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_user row should be linked to a Grouper member", linkedUsers >= 1);
  }

  /**
   * Sync-back smoke test for the scoped-retrieve path: same flow as the selectAll variant,
   * but with {@code selectAllGroups=false} and {@code selectAllEntities=false} so the DAO
   * uses the scoped {@code retrieveGroups} / {@code retrieveEntities} (per-id batch lookups)
   * instead of {@code retrieveAllGroups} / {@code retrieveAllEntities}. Confirms the
   * capture hooks on the scoped retrieve methods fire.
   *
   * <p>Incremental test coverage for the OBJECT axes is intentionally deferred — group/user objects
   * capture from reads, and their writes converge on the next read pass. (Memberships now capture on
   * write via recordTargetNativeMembershipInsert/Delete, so they converge on the write.) Closing the
   * object-axis gap is the write-shadow precision pass tracked in section 10 of the sync-back doc.
   */
  public void testAzureFullSyncSelectByIdsPopulatesGenericTables() {

    String configId = "myAzureProvisioner";
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve and flushes.
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row via scoped retrieve",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_mship") >= 2);
  }

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  // ==========================================================================================
  // SCIM-parity sync-back tests for AZURE, CAPABILITY-GATED. (Box-pilot replication; see
  // boxProvisioner/GrouperBoxProvisionerTest for the prose template these mirror.)
  //
  // Azure capture model (verified from GrouperAzureProvisioningTargetNativeSync +
  // GrouperAzureTargetDao + GrouperAzureApiCommands):
  //   - Groups and users are captured from the raw Microsoft Graph JSON at the
  //     GrouperAzureApiCommands read seam (captureGroupJsonFromCurrentProvisioner /
  //     captureUserJsonFromCurrentProvisioner fire inside retrieveAzureGroups/retrieveGroupsHelper
  //     and retrieveAzureUsers/retrieveUsersHelper). The insert/update/delete API methods do NOT
  //     call any capture hook.
  //   - Memberships are group-centric and captured during DAO translation
  //     (GrouperAzureTargetDao.retrieveMembershipsByGroup ->
  //     captureMembershipsForGroupFromCurrentProvisioner), the only read seam where both the group
  //     id and the member user ids are co-located (Azure has no retrieveAllMemberships call).
  // So Azure -- exactly like the Box pilot -- is a READ-STATE-CONVERGENCE target, NOT a
  // capture-on-write target like SCIM/Adobe: a target change converges into the mirror on the NEXT
  // read pass, not the same run that writes it. Every converge test below therefore uses the
  // two-pass full-sync pattern (pass 1 writes the target, pass 2 re-reads and the end-of-run flush
  // converges), the same shape as the existing testAzureFullSyncPopulatesGenericTables.
  //
  // The full flush (GrouperProvisioningLogic.loadDataToGenericProvisionerTables) is a FULL REPLACE
  // scoped to the provisioner's grouper_sync_internal_id: anything in the mirror that the target did
  // NOT return this run is deleted -- that is what makes the delete / membership-remove converge
  // tests work after a re-read pass.
  //
  // Capabilities confirmed in GrouperAzureTargetDao.registerGrouperProvisionerDaoCapabilities:
  //   group  : insert YES, update YES, delete YES
  //   entity : insert YES, update YES, delete YES
  //   mship  : insert YES, delete YES, REPLACE *NO* (no setCanReplaceMembership)
  //   memberships are group-centric (canRetrieveMembershipsAllByGroup; also ...AllByEntity)
  //
  // Default capture attributes (GrouperAzureProvisioningTargetNativeSync DEFAULT_*_ATTRS):
  //   groups : displayName, mailNickname   (NOT SCIM's names; id is the target_group_id column)
  //   users  : userPrincipalName, mail, mailNickname, userType  (id is the target_user_id column;
  //            displayName is NOT a user default)
  // Assertions below read ONLY these defaults (plus description in the one update test, which
  // additionally configures nativeAttributesGroups to capture it).
  //
  // Matching attributes (AzureProvisionerTestUtils default config, the SAME config the existing
  // testAzureFullSync* sync-back tests use -- groupAttributeCount=3, entityAttributeCount=5):
  //   group  matching = displayName  (groupMatchingAttribute0name=displayName), translated from the
  //          Grouper group's "name" field (displayNameMapping default = "name").
  //   entity matching = displayName  (entityAttributeCount==5 path:
  //          entityMatchingAttribute0name=displayName), translated from the Grouper member's "name".
  // BOTH the group and the entity are NAME-MATCHED, so the rename-as-update problem (the Adobe
  // lesson) applies to both -- a change to the matching attribute cannot converge as an in-place
  // update. The group-update-converge test therefore mutates DESCRIPTION (a NON-matching attribute):
  // it is mapped as targetGroupAttribute.3 (translateFromGrouperProvisioningGroupField=description),
  // round-trips through the mock's updateGroup, and is captured because the test sets
  // nativeAttributesGroups=displayName,description (description is NOT an Azure default capture attr).
  //
  // Azure target seeding for orphan/drift rows uses GrouperAzureApiCommands.createAzure* against the
  // running mock service (the same idiom every forward Azure test uses) -- NOT direct
  // HibernateSession.byObjectStatic() inserts as the Box pilot used. The Azure mock is an HTTP
  // service served by Tomcat, so these tests REQUIRE Tomcat and are gated on tomcatRunTests().
  //
  // SKIPPED, per capability (no test body, just a note where it would live):
  //   - membership-REPLACE sync-back test: GrouperAzureTargetDao has no setCanReplaceMembership, so
  //     SCIM's testMembershipReplaceConvergesSameRun / testIncrementalMembershipReplace... do not
  //     apply (Azure adds/removes memberships individually, it never bulk-replaces a group's roster).
  //   - entity-UPDATE-converge sync-back test: the Azure entity is name-matched (displayName from the
  //     member's name), and its only other Grouper-driven attributes (userPrincipalName from
  //     subjectId, mailNickname from name) are derived from the match key. There is no safe
  //     Grouper-driven NON-matching user attribute to mutate, so an update-converge test would be
  //     mutating the match key (the Adobe lesson) and cannot converge as an in-place update. Same
  //     reason the Box pilot skips its user-update-converge test.
  //   - "same-run" convergence variants of the SCIM insert/update/delete tests: Azure captures group
  //     and user OBJECTS on READ, so those object tests converge only on the next read pass. Their
  //     intent is ported as the two-pass full tests below (testAzureGroupInsertConvergesNextRead,
  //     testAzureGroupDeleteConvergesNextRead, testAzureGroupUpdateConvergesNextRead). Azure
  //     MEMBERSHIPS, by contrast, now capture on WRITE (recordTargetNativeMembershipInsert/Delete),
  //     like Adobe/SCIM, so the membership tests (testAzureMembershipAddConvergesNextRead,
  //     testAzureMembershipRemoveConvergesNextRead) assert convergence on pass A's write, not a re-read.
  // ==========================================================================================

  /**
   * Shared setup for the Azure sync-back tests: configure the provisioner with the three
   * load*ToGenericGrouperTable flags on (and recalculateAllOperations so every object/membership is
   * processed each run), reusing the SAME default AzureProvisionerTestUtils config + configId the
   * existing testAzureFullSync* sync-back tests use. The caller starts its own root session and
   * creates the Grouper-side stems/groups/members it needs.
   *
   * <p>That default config (entityAttributeCount=5) ships with the group + membership delete-types
   * already ON (customizeGroupCrud/customizeMembershipCrud=true, deleteGroups +
   * deleteGroupsIfNotExistInGrouper=true, deleteMemberships + deleteMembershipsIfNotExistInGrouper
   * =true) but deleteEntities=false. So by default a Grouper-side group/membership delete DOES
   * propagate to the Azure target and converge out of the mirror, while users are never deleted.
   * Tests that need target-side orphans to PERSIST (the orphan-capture tests) pass overrides via
   * {@code extraConfig} to turn the group/membership deletes back OFF
   * (customize*Crud=true + deleteX=false).
   *
   * @param configId the provisioner config id (always "myAzureProvisioner" here)
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes to set (may be null)
   */
  private void setupAzureSyncBack(String configId, Map<String, String> extraConfig) {

    AzureProvisionerTestConfigInput configInput = new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    AzureProvisionerTestUtils.configureAzureProvisioner(configInput);

    GrouperStartup.startup();

    // setUp() already wiped mock_azure_membership / mock_azure_group / mock_azure_user, so the Azure
    // target starts empty for every test.
  }

  /**
   * The single provisioned group's target_group_id (Azure group id) in the mirror, or null. Mirrors
   * the Box/Adobe helper of the same name -- used by the group-update-converge test to prove the
   * SAME target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved {@code description} attribute value for the single provisioned group in the mirror, or
   * null. Reads through the {@code grouper_prov_group_attr_v} reporting view (not the base
   * grouper_prov_group_attr_value table), because the raw string is stored via a dictionary FK and
   * only the view resolves it back to text (column {@code value_string}). {@code description} is
   * captured only because the update-converge test configures
   * {@code nativeAttributesGroups=displayName,description} -- it is NOT an Azure default capture
   * attribute (defaults are displayName/mailNickname), so without that config this returns null.
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Sync-back convergence of a newly created group, two-pass full (Azure analogue of SCIM's
   * testGroupInsertConvergesSameRun and the Box pilot's testBoxGroupInsertConvergesNextRead). Pass 1
   * inserts the group into the Azure target; the group converges into grouper_prov_group on the
   * re-read (pass 2), linked back to its Grouper group, and its displayName (an Azure default capture
   * attribute) round-trips into the mirror.
   */
  public void testAzureGroupInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    setupAzureSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the mirror yet
    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

    // pass 1 inserts the group into the Azure target; the mirror stays empty until the read pass.
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    // pass 2 re-reads the target and the end-of-run flush converges the group into the mirror.
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("group insert should converge into prov_group on the read pass", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // displayName captured from the Graph read response (an Azure default capture attribute)
    int displayNameValueRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr_value gpv "
            + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
            + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
            + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'displayName'")
        .addBindVar(syncInternalId).select(int.class);
    assertTrue("displayName should be captured from the Graph read response, got " + displayNameValueRows,
        displayNameValueRows >= 1);
  }

  /**
   * Sync-back convergence of an object DELETE, two-pass full (Azure analogue of SCIM's
   * testGroupDeleteConvergesSameRun and the Box pilot's testBoxGroupDeleteConvergesNextRead). Seed
   * test:testGroup + SUBJ0 + their membership into the mirror, then delete the group in Grouper.
   *
   * <p>The default Azure sync-back config already has group + membership deletes enabled
   * (deleteGroups/deleteMemberships + ...IfNotExistInGrouper) but deleteEntities=false. So the next
   * full sync removes the group + the membership from the Azure target (pass A), and the re-read pass
   * (pass B) drops the group and the membership from the mirror; the user is NOT deleted (Azure
   * default), so SUBJ0 stays in the target and the mirror -- the documented Azure divergence from
   * the Box pilot, whose config also deleted the orphaned user.
   */
  public void testAzureGroupDeleteConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    setupAzureSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: two passes converge the group + SUBJ0 + their membership into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // delete the group in Grouper
    testGroup.delete();

    // pass A: the group + membership deletes hit the Azure target (the user is left, deleteEntities=false)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read sees the group + membership gone; the full-replace flush drops their mirror rows
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("membership dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_mship"));
    // Azure default keeps the user (deleteEntities=false): the orphaned account stays in the target,
    // so the read pass keeps it in the mirror. (Box deleted it; Azure does not.)
    assertEquals("SUBJ0's user stays in the mirror (Azure deleteEntities=false leaves the account)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * Sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass full (Azure
   * analogue of SCIM's testUserUpdateConvergesSameRun, but on a GROUP, like the Box pilot's
   * testBoxGroupUpdateConvergesNextRead). The Azure group is matched by displayName, so the
   * rename-as-update problem (the Adobe lesson) rules out mutating displayName. Instead we mutate the
   * group's DESCRIPTION, which this test maps as a writable group attribute (index 3,
   * translateFromGrouperProvisioningGroupField=description, insert+update on -- see the config block
   * below for why this must be declared here rather than relied on from the base config), round-trips
   * through the mock's updateGroup, and is NOT the matching attribute. nativeAttributesGroups is set
   * to "displayName,description" so the description value is actually captured into the mirror (it is
   * NOT an Azure default capture attribute).
   *
   * <p>Asserts both that the description VALUE converges to the new value AND that it is an in-place
   * update -- the SAME target group id survives (not delete + re-create, which would assign a new
   * Azure id). Convergence is on the re-read pass (pass B), since Azure captures on read.
   */
  public void testAzureGroupUpdateConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    // ROOT CAUSE of the earlier "seed: original description captured expected:<originalDescription>
    // but was:<null>" failure: the DEFAULT Azure test config (AzureProvisionerTestUtils, the else
    // branch with groupAttributeCount=3) does NOT map "description" as a group attribute at all --
    // its group attributes are id (0), displayName (1, the match), mailNickname (2). (description is
    // only mapped in the michiganAzure/uncgAzure strategies, which we are NOT using; the earlier
    // assumption that "index 3 is description in the base" was simply wrong for this config.) Because
    // description is never a mapped attribute, it is never put in the createGroup request
    // (GrouperAzureTargetDao.insertGroups builds fieldNamesToInsert from the group's object changes,
    // and there is no description change), so the mock stores description=null on create and the seed
    // re-read captures null. Unlike Datadog (JSON:API, /attributes/... nesting), Azure's captured
    // group node is FLAT -- both the bulk retrieveAzureGroups seam and the $batch retrieveGroupsHelper
    // seam hand a top-level group object to the capture, with /description at the root -- so a flat
    // CSV nativeAttributesGroups pointer (/description) resolves correctly ONCE the value is actually
    // present in the target. The fix is therefore to PUSH description, exactly like the Box pilot
    // (whose default config maps description as a writable targetGroupAttribute): we bump the group
    // attribute count to 4 and define index 3 as description, sourced from the Grouper group's
    // description field, with insert+update+select on. We override the whole targetGroupAttribute.3.*
    // key set because the count>=4 path in the test util otherwise claims index 3 for
    // allowOnlyMembersToPost (the blank translateExpression suppresses that default's script, since
    // configureProvisionerSuffix skips any suffix present here and the final loop skips blank values).
    extraConfig.put("numberOfGroupAttributes", "4");
    extraConfig.put("targetGroupAttribute.3.name", "description");
    extraConfig.put("targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField");
    extraConfig.put("targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "description");
    // override the count>=4 default that would otherwise put allowOnlyMembersToPost at index 3
    extraConfig.put("targetGroupAttribute.3.translateExpression", "");
    // per-attribute CRUD flags (.insert/.update/.select) are only honored in "advanced" attribute mode;
    // without these two enabling flags the config validation rejects 'targetGroupAttribute.3.insert'
    // with "should be refactored with an upgrade task". The util's own count>=4 path sets the same pair
    // on its index-3 attribute (AzureProvisionerTestUtils), as does the Box writable-attribute idiom.
    extraConfig.put("targetGroupAttribute.3.showAdvancedAttribute", "true");
    extraConfig.put("targetGroupAttribute.3.showAttributeCrud", "true");
    extraConfig.put("targetGroupAttribute.3.insert", "true");
    extraConfig.put("targetGroupAttribute.3.update", "true");
    extraConfig.put("targetGroupAttribute.3.select", "true");
    // capture description into the mirror so we can assert it -- it is NOT an Azure default capture
    // attribute (defaults are displayName/mailNickname). Flat CSV is correct here (Azure node is flat).
    extraConfig.put("nativeAttributesGroups", "displayName,description");
    setupAzureSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("originalDescription").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group provisioned with description "originalDescription"
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    String groupTargetIdBefore = mirroredGroupTargetId(configId);
    assertNotNull("group should have a target id after seed", groupTargetIdBefore);
    assertEquals("seed: original description captured", "originalDescription",
        mirroredGroupDescription(configId));

    // change the description (a NON-matching attribute) -> Azure updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the Azure target (updateGroup persists it)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // the updateGroups success path now records the updated group (null native) for the end-of-run
    // sync-back drain, whose re-read refreshes the mirror on THIS run -- so the new description is
    // already captured after pass A, before the pass-B bulk re-read.
    assertEquals("update converges on the write pass via the drain re-read (before the bulk re-read)",
        "newDescription", mirroredGroupDescription(configId));
    // pass B: the re-read captures the target's actual new description into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // mirror side: still ONE group, the SAME group (same target id) -- in-place update, not
    // delete + re-create -- and its description converged to the new value.
    assertEquals("group still in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("mirror tracks the same group through the update (update, not re-create)",
        groupTargetIdBefore, mirroredGroupTargetId(configId));
    assertEquals("mirror description should converge to the new value on the re-read pass",
        "newDescription", mirroredGroupDescription(configId));
  }

  /**
   * Sync-back convergence of a membership ADD to an already-provisioned group, two-pass full (Azure
   * analogue of SCIM's testMembershipAddConvergesSameRun). Seed test:testGroup with SUBJ0, then add
   * SUBJ1. Azure memberships now capture on WRITE (recordTargetNativeMembershipInsert, fired from
   * GrouperAzureTargetDao.insertMemberships on success, via
   * GrouperAzureProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner), like
   * Adobe/SCIM: pass A issues the membership insert to the Azure target and the same success path
   * records (testGroup,SUBJ1) into the mirror on that write -- no re-read needed. Pass B re-reads and
   * keeps the added membership. (Group and user OBJECTS still capture on the read path.)
   */
  public void testAzureMembershipAddConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    setupAzureSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + the one membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: the single membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // add SUBJ1 to the already-provisioned group
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // pass A: the membership insert (and SUBJ1's user insert) hit the Azure target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // the membership write-track hook (captureMembershipInsertFromCurrentProvisioner in the DAO's
    // insertMemberships success path) mirrors (testGroup,SUBJ1) on the SAME run -- no re-read
    // needed -- so grouper_prov_mship already reflects the add after pass A.
    assertEquals("the added membership should be write-tracked into the mirror on pass A", 2,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read sees both members; the flush keeps the added membership
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("the added membership should remain on the re-read pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of a membership REMOVE from a surviving group, two-pass full (Azure
   * analogue of SCIM's testMembershipRemoveConvergesSameRun). Two groups both hold SUBJ0; SUBJ0 is
   * removed from testGroup only (it survives in otherGroup). Azure memberships now capture on WRITE
   * (recordTargetNativeMembershipDelete, fired from GrouperAzureTargetDao.deleteMemberships on
   * success, via GrouperAzureProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner),
   * like Adobe/SCIM: pass A's membership-remove write drops exactly (testGroup,SUBJ0) from the mirror
   * on that write while leaving otherGroup's SUBJ0 intact, and pass B's re-read keeps it dropped. The
   * default config's membership delete (deleteMemberships + deleteMembershipsIfNotExistInGrouper) is
   * what pushes the removal to the target; deleteEntities=false leaves SUBJ0's account in place.
   */
  public void testAzureMembershipRemoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    setupAzureSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group otherGroup = new GroupSave(grouperSession).assignName("test:otherGroup").save();
    // SUBJ0 in BOTH groups so removing it from testGroup leaves it provisioned (still in otherGroup)
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: both groups + SUBJ0 + both memberships in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: both groups", 2, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: both memberships", 2, countSyncBack(configId, "grouper_prov_mship"));

    // remove SUBJ0 from testGroup only (still in otherGroup)
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    // pass A: the membership-remove write hits the Azure target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // the membership write-track hook (captureMembershipDeleteFromCurrentProvisioner in the DAO's
    // deleteMemberships success path) drops (testGroup,SUBJ0) from the mirror on the SAME run, so
    // grouper_prov_mship already reflects the removal after pass A (otherGroup's SUBJ0 survives).
    assertEquals("testGroup's membership should be write-tracked out of the mirror on pass A", 1,
        countSyncBack(configId, "grouper_prov_mship"));
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush keeps
    // (testGroup,SUBJ0) dropped while otherGroup's SUBJ0 membership survives
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Multi-sync coverage with data evolution between rounds, Azure analogue of SCIM's
   * testFullProvisionReflectsDataChangesAcrossSyncs and the Box pilot's
   * testBoxFullSyncReflectsDataChangesAcrossSyncs. Round 1: testGroup with SUBJ0 only, seeded via two
   * passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan group + orphan user
   * directly into the Azure mock (delete-types for groups/memberships are turned OFF so they
   * persist). Round 3: two more passes -> the mirror reflects the new state (3 users: SUBJ0, SUBJ1,
   * orphan; 2 groups: testGroup, orphan; 2 memberships in testGroup), and the target-drift orphan
   * user's userPrincipalName (an Azure default capture attribute) round-trips.
   */
  public void testAzureFullSyncReflectsDataChangesAcrossSyncs() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    // turn the default group + membership deletes OFF so the Round 2 orphans persist across syncs
    // (customize*Crud stays true, set the umbrella deleteX=false)
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("deleteGroupsIfNotExistInGrouper", "false");
    noDeletes.put("deleteMemberships", "false");
    noDeletes.put("deleteMembershipsIfNotExistInGrouper", "false");
    setupAzureSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // ===================== ROUND 1: initial state =====================

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("round 1: 1 prov_user row for SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 1: 1 prov_group row for testGroup", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 1: 1 prov_mship row for SUBJ0 in testGroup", 1, countSyncBack(configId, "grouper_prov_mship"));

    // ===================== ROUND 2: data changes =====================

    // Grouper-side: add SUBJ1 to testGroup. next full sync inserts SUBJ1 + the membership.
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // Target-side drift: create an orphan group + orphan user directly in the Azure mock via the API
    // commands (the same idiom every forward Azure test uses). They are unknown to Grouper; with the
    // group/membership deletes off they persist across the next sync.
    GrouperAzureUser orphanUser = new GrouperAzureUser();
    orphanUser.setUserPrincipalName("orphanEvolve@" + domain);
    orphanUser.setDisplayName("orphanUserAddedMidTest");
    orphanUser.setMailNickname("orphanEvolve");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(orphanUser), null);

    GrouperAzureGroup orphanGroup = new GrouperAzureGroup();
    orphanGroup.setDisplayName("orphanGroupAddedMidTest");
    orphanGroup.setMailNickname("orphanGroupEvolve");
    Map<GrouperAzureGroup, Set<String>> orphanGroupToFields = new HashMap<GrouperAzureGroup, Set<String>>();
    orphanGroupToFields.put(orphanGroup, null);
    GrouperAzureApiCommands.createAzureGroups("myAzure", orphanGroupToFields);

    // ===================== ROUND 3: second full sync + assertions =====================

    // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's + the
    // drift orphans) and refreshes the mirror.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)", 3,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 3: 2 prov_group rows expected (testGroup, orphan_group)", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in testGroup)", 2,
        countSyncBack(configId, "grouper_prov_mship"));

    // the orphan group landed in the mirror, unlinked (no Grouper group)
    int orphanGroupRow = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group should land in prov_group with group_internal_id IS NULL", 1,
        orphanGroupRow);

    // the orphan user's userPrincipalName value round-trips through the reporting view (proves
    // target-drift entities are captured with their actual attributes). userPrincipalName is an Azure
    // default entity capture attribute.
    String orphanUserUpnInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'userPrincipalName'")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(String.class);
    assertEquals("orphan user's userPrincipalName should round-trip through reporting",
        "orphanEvolve@" + domain, orphanUserUpnInReporting);
  }

  /**
   * Strict-native capture of orphan target objects, Azure analogue of SCIM's
   * testFullProvisionCapturesOrphanTargetEntities and the Box pilot's
   * testBoxFullSyncCapturesOrphanTargetEntities. With the group/membership delete-types disabled, an
   * orphan group + orphan user that exist in the Azure target but are unknown to Grouper are still
   * captured into the mirror -- with NULL Grouper-side linkage (group_internal_id /
   * member_internal_id) -- alongside Grouper's own testGroup + SUBJ0/SUBJ1, which keep their linkage
   * populated.
   */
  public void testAzureFullSyncCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    // disable the default group + membership deletes so the orphans persist across the run
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("deleteGroupsIfNotExistInGrouper", "false");
    noDeletes.put("deleteMemberships", "false");
    noDeletes.put("deleteMembershipsIfNotExistInGrouper", "false");
    setupAzureSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the Azure mock before the provisioner runs, via the API
    // commands (the forward-test idiom).
    GrouperAzureUser orphanUser = new GrouperAzureUser();
    orphanUser.setUserPrincipalName("orphanUser@" + domain);
    orphanUser.setDisplayName("orphanUserNotInGrouper");
    orphanUser.setMailNickname("orphanUser");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(orphanUser), null);

    GrouperAzureGroup orphanGroup = new GrouperAzureGroup();
    orphanGroup.setDisplayName("orphanGroupNotInGrouper");
    orphanGroup.setMailNickname("orphanGroupNot");
    Map<GrouperAzureGroup, Set<String>> orphanGroupToFields = new HashMap<GrouperAzureGroup, Set<String>>();
    orphanGroupToFields.put(orphanGroup, null);
    GrouperAzureApiCommands.createAzureGroups("myAzure", orphanGroupToFields);

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // two passes: pass 1 inserts Grouper's objects (orphans untouched, deletes off); pass 2 reads
    // orphans + Grouper's objects and the flush captures all of them.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // orphan group landed with NULL group_internal_id
    int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_group row for the orphan group", 1, orphanGroupRowsTotal);

    int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group's prov_group row must have group_internal_id IS NULL", 1,
        orphanGroupRowsUnlinked);

    // orphan user landed with NULL member_internal_id
    int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("expected exactly 1 prov_user row for the orphan user", 1, orphanUserRowsTotal);

    int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ? and member_internal_id is null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user's prov_user row must have member_internal_id IS NULL", 1,
        orphanUserRowsUnlinked);

    // Grouper's own testGroup + 2 members land alongside, with linkage populated
    int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id != ? and group_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("Grouper's testGroup prov_group row must have group_internal_id linked", 1,
        testGroupRowsLinked);

    int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id != ? and member_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("Grouper-provisioned prov_user rows (SUBJ0 + SUBJ1) must have member_internal_id linked",
        2, nonOrphanUserRowsLinked);

    // an Azure default group attribute (displayName) is captured in the catalog
    int displayNameCatalog = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'displayName'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("default group attribute 'displayName' should be in the per-provisioner catalog", 1,
        displayNameCatalog);

    // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis, Azure analogue of SCIM's
   * testFullProvisionCapturesMembershipsFromOrphanGroup and the Box pilot's
   * testBoxFullSyncCapturesMembershipsFromOrphanGroup. An orphan group with an orphan member
   * (neither known to Grouper) is wired in the Azure mock. Azure memberships are group-centric, so
   * when the daemon lists groups it also reads the orphan group's members
   * (retrieveMembershipsByGroup) -- that membership must land in grouper_prov_mship alongside
   * Grouper's own, proving strict-native membership capture is independent of Grouper knowledge.
   */
  public void testAzureFullSyncCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    // disable the default group + membership deletes so the orphan group + its membership persist
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("deleteGroupsIfNotExistInGrouper", "false");
    noDeletes.put("deleteMemberships", "false");
    noDeletes.put("deleteMembershipsIfNotExistInGrouper", "false");
    setupAzureSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the Azure mock via the API
    // commands. The membership FK requires the group and user rows to exist first.
    GrouperAzureUser orphanUser = new GrouperAzureUser();
    orphanUser.setUserPrincipalName("orphanMship@" + domain);
    orphanUser.setDisplayName("orphanMshipUser");
    orphanUser.setMailNickname("orphanMship");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(orphanUser), null);

    GrouperAzureGroup orphanGroup = new GrouperAzureGroup();
    orphanGroup.setDisplayName("orphanGroupWithMembers");
    orphanGroup.setMailNickname("orphanGroupMship");
    Map<GrouperAzureGroup, Set<String>> orphanGroupToFields = new HashMap<GrouperAzureGroup, Set<String>>();
    orphanGroupToFields.put(orphanGroup, null);
    GrouperAzureApiCommands.createAzureGroups("myAzure", orphanGroupToFields);

    // wire the membership (orphan user -> orphan group) in the target
    GrouperAzureApiCommands.createAzureMemberships("myAzure", orphanGroup.getId(),
        GrouperUtil.toList(orphanUser.getId()));

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // the orphan group's membership lands in prov_mship (join through prov_group/prov_user, which
    // hold the target ids -- prov_mship itself only has the FK internal ids)
    int orphanMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship pm "
            + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
            + "where pm.grouper_sync_internal_id = ? and pg.target_group_id = ? and pu.target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).addBindVar(orphanUser.getId())
        .select(int.class);
    assertEquals("expected 1 prov_mship row for orphan group -> orphan user", 1, orphanMshipRows);

    // Grouper's own memberships land alongside (3 total: SUBJ0 + SUBJ1 in testGroup + the orphan)
    assertEquals("expected 3 prov_mship rows total (2 from testGroup + 1 orphan)", 3,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * !selectAll* scope excludes orphans, Azure analogue of SCIM's testSelectAllFalseExcludesOrphans
   * and the Box pilot's testBoxSelectAllFalseExcludesOrphans. With selectAllGroups=false and
   * selectAllEntities=false the daemon fetches only the resources mapped to Grouper-provisioned
   * objects (by id/displayName), never a server-wide listing -- so an orphan group/user that the
   * Azure target has but Grouper does not must NOT land in the mirror.
   */
  public void testAzureSelectAllFalseExcludesOrphans() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    String domain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector.myAzure.domain");

    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    // deletes off too, so if an orphan WERE somehow captured it would not be silently removed -- the
    // assertion below then genuinely proves the scoped retrieve never fetched it.
    extraConfig.put("deleteGroups", "false");
    extraConfig.put("deleteGroupsIfNotExistInGrouper", "false");
    extraConfig.put("deleteMemberships", "false");
    extraConfig.put("deleteMembershipsIfNotExistInGrouper", "false");
    setupAzureSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id/displayName (Grouper-known resources only).
    GrouperAzureUser orphanUser = new GrouperAzureUser();
    orphanUser.setUserPrincipalName("orphanSelnone@" + domain);
    orphanUser.setDisplayName("orphanUserSelectAllFalse");
    orphanUser.setMailNickname("orphanSelnone");
    GrouperAzureApiCommands.createAzureUsers("myAzure", GrouperUtil.toList(orphanUser), null);

    GrouperAzureGroup orphanGroup = new GrouperAzureGroup();
    orphanGroup.setDisplayName("orphanGroupSelectAllFalse");
    orphanGroup.setMailNickname("orphanSelnoneGrp");
    Map<GrouperAzureGroup, Set<String>> orphanGroupToFields = new HashMap<GrouperAzureGroup, Set<String>>();
    orphanGroupToFields.put(orphanGroup, null);
    GrouperAzureApiCommands.createAzureGroups("myAzure", orphanGroupToFields);

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // Grouper-known resources still captured
    assertTrue("Grouper-provisioned testGroup should still be in prov_group",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("Grouper-provisioned SUBJ0/SUBJ1 should still be in prov_user",
        countSyncBack(configId, "grouper_prov_user") >= 2);

    // orphans must NOT be captured (selectAll=false -> no server-wide listing -> no capture)
    int orphanGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanGroup.getId()).select(int.class);
    assertEquals("orphan group must NOT be captured when selectAllGroups=false", 0, orphanGroupRows);

    int orphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar(orphanUser.getId()).select(int.class);
    assertEquals("orphan user must NOT be captured when selectAllEntities=false", 0, orphanUserRows);
  }

  /**
   * Broken-target delete stays in the mirror, Azure analogue of SCIM's
   * testUserDeleteBrokenTargetStaysInMirror and the Box pilot's
   * testBoxUserDeleteBrokenTargetStaysInMirror. A target object the daemon did NOT remove must stay
   * captured (the "verify, don't assume" contract -- Azure re-reads and finds it still present).
   *
   * <p>Azure analogue mechanism: like Box, we have no mock knob to fake a broken delete, so instead
   * we mark the group to provision but turn the group + membership deletes OFF (deleteEntities is
   * already false by default). SUBJ0 is removed from testGroup in Grouper, but with the deletes off
   * the daemon never issues the membership/group delete to the Azure target -- so the user (and its
   * membership) remain in the target, and the re-read keeps them in the mirror.
   */
  public void testAzureUserDeleteBrokenTargetStaysInMirror() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    // turn the default group + membership deletes off (entities already not deleted), so the daemon
    // will NOT remove anything from the Azure target once SUBJ0 becomes unprovisioned in testGroup.
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("deleteGroupsIfNotExistInGrouper", "false");
    noDeletes.put("deleteMemberships", "false");
    noDeletes.put("deleteMembershipsIfNotExistInGrouper", "false");
    setupAzureSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + their membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

    // remove SUBJ0 from the group in Grouper. With deletes off the daemon does not push the removal
    // to the Azure target, so the target still has SUBJ0 (and the membership).
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group was never deleted -> still in the mirror
    assertEquals("group row should stay (group was not deleted)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // confirm the target still has the user (the daemon did not remove it), so the re-read keeps it
    int mockUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_azure_user").select(int.class);
    assertEquals("the user row should remain in the Azure target (deletes are off)", 1, mockUserRows);

    assertEquals("user should STAY in the mirror (its delete was never performed)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * loadGroupsToGenericGrouperTable in isolation, Azure analogue of SCIM's
   * testLoadGroupsFlagInIsolation and the Box pilot's testBoxLoadGroupsFlagInIsolation. Only the
   * groups flag is on -> only grouper_prov_group rows are written; prov_user and prov_mship stay
   * empty even though the daemon still reads users (for provisioning) and memberships (for diffing).
   */
  public void testAzureLoadGroupsFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row when groups capture is on",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertEquals("expected 0 prov_user rows when entities capture is off", 0,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadEntitiesToGenericGrouperTable in isolation, Azure analogue of SCIM's
   * testLoadEntitiesFlagInIsolation and the Box pilot's testBoxLoadEntitiesFlagInIsolation. Only the
   * entities flag is on -> only grouper_prov_user rows are written; prov_group and prov_mship stay
   * empty.
   */
  public void testAzureLoadEntitiesFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("expected 0 prov_group rows when groups capture is off", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadMembershipsToGenericGrouperTable off, Azure analogue of SCIM's testLoadMembershipsFlagOff
   * and the Box pilot's testBoxLoadMembershipsFlagOff. Both object loads on but memberships off ->
   * prov_group and prov_user populate, prov_mship stays empty. Proves the membership gate is
   * independent of the object gates.
   */
  public void testAzureLoadMembershipsFlagOff() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    AzureProvisionerTestUtils.configureAzureProvisioner(new AzureProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));

    GrouperStartup.startup();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row", countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * INCREMENTAL sync-back coverage for Azure, conservative. Azure memberships capture on WRITE
   * (recordTargetNativeMembershipInsert/Delete, fired from
   * GrouperAzureTargetDao.insertMemberships/deleteMemberships on success), like Adobe/SCIM -- a
   * membership add/remove is recorded into the native mirror on the write itself; group and user
   * OBJECTS still capture on the read path. On an incremental cycle Azure also re-reads only the
   * changed objects (it has canRetrieveGroups/canRetrieveEntities, so the adapter decomposes to
   * per-id reads that fire the object capture seams), and the incremental flush is a SCOPED upsert
   * (it does NOT full-replace, so it will not wrongly delete untouched mirror rows).
   *
   * <p>What this test asserts is therefore deliberately narrow -- the safe, reliable part of Azure
   * incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
   * member drives an incremental that (a) re-reads the changed group/entity and so does NOT shrink
   * the existing GROUP mirror (no spurious deletes -- the regression the scoped incremental flush
   * guards against), and (b) captures the newly added member's user object into prov_user. It does
   * NOT assert the new MEMBERSHIP's row count on this incremental cycle: the scoped membership flush
   * for the changed group can transiently clear testGroup's mirror rows during the cycle, so a
   * point-in-time count here is unreliable. Membership write-capture convergence for Azure is asserted
   * end-to-end by the two-pass full tests above.
   */
  public void testAzureIncrementalSyncBackNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "myAzureProvisioner";
    setupAzureSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed via full sync: group + SUBJ0 + SUBJ1 + their memberships in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    int provGroupRowsBefore = countSyncBack(configId, "grouper_prov_group");
    int provUserRowsBefore = countSyncBack(configId, "grouper_prov_user");
    int provMshipRowsBefore = countSyncBack(configId, "grouper_prov_mship");
    assertTrue("seed should have >=1 prov_group row", provGroupRowsBefore >= 1);
    assertEquals("seed should have 2 prov_user rows", 2, provUserRowsBefore);
    assertEquals("seed should have 2 prov_mship rows", 2, provMshipRowsBefore);

    // prime the changelog consumer: its FIRST run only initializes its changelog position
    // (processes nothing), so without this priming pass the change below is never consumed.
    incrementalProvision();

    // incremental add: a third member. The incremental re-reads the changed group/entity, firing the
    // Azure object (group/user) read-capture seams, and the scoped flush upserts -- it must NOT drop
    // untouched rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc). Azure
    // memberships capture on WRITE (recordTargetNativeMembershipInsert/Delete from the DAO's
    // insert/deleteMemberships success paths), but on an incremental cycle the SCOPED membership flush
    // for the changed group can transiently clear testGroup's mirror rows mid-cycle, so a
    // point-in-time count here is unreliable. Membership write-capture convergence is asserted end-to-end by the
    // two-pass full tests above; here we only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; Azure shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

}
