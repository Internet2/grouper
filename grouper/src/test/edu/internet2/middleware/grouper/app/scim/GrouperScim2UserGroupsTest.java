package edu.internet2.middleware.grouper.app.scim;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheckEngine;
import edu.internet2.middleware.grouper.app.config.check.provisioning.ScimUserGroupsConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GenericScim2MockServiceHandler;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2MembershipCache;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import junit.textui.TestRunner;

/** Google-like membership reads with scimType=generic against the local SCIM mock, never Google. */
public class GrouperScim2UserGroupsTest extends GrouperProvisioningBaseTest {

  private static final String MOCK_PREFIX = "grouperTest.scim2.mock.";

  private Map<String, String> originalProvisionerSettings = new LinkedHashMap<String, String>();

  private Map<String, String> originalMockSettings = new LinkedHashMap<String, String>();

  private String originalRealScimOverride;

  public GrouperScim2UserGroupsTest() {
    super();
  }

  public GrouperScim2UserGroupsTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    GenericScim2MockServiceHandler.ensureScimMockTables();
    TestRunner.run(new GrouperScim2UserGroupsTest("testGroupMetadataCannotPopulateExplicitUserMemberships"));
  }

  @Override
  public String defaultConfigId() {
    // Legacy fixture names do not determine the provisioner's scimType.
    return "awsProvisioner";
  }

  private void mockSetting(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(MOCK_PREFIX + suffix).value(value).store();
  }

  private void provisionerSetting(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner." + defaultConfigId() + "." + suffix).value(value).store();
  }

  @Override
  protected void setUp() {
    super.setUp();
    GrouperStartup.startup();
    for (String suffix : new String[] {"scimType", "scimMembershipBatchSize",
        "scimUserGroupsRequireAttributes", "scimRetrieveMembershipsByUser",
        "scimRetrieveMembershipsByGroup", "membershipStrategy", "selectAllEntities", "selectAllGroups",
        "replaceMemberships", "selectMemberships", "loadEntitiesToGenericGrouperTable",
        "loadGroupsToGenericGrouperTable", "loadMembershipsToGenericGrouperTable"}) {
      this.originalProvisionerSettings.put(suffix, GrouperLoaderConfig.retrieveConfig()
          .propertyValueString("provisioner." + defaultConfigId() + "." + suffix));
    }
    for (String suffix : new String[] {"userGroupsRequireAttributes", "userGroupsAllowUnfilteredLists",
        "userGroupsStatus", "userGroupsResponse", "membershipStrategy.mode"}) {
      this.originalMockSettings.put(suffix,
          GrouperConfig.retrieveConfig().propertyValueString(MOCK_PREFIX + suffix));
    }
    // Never honor an environment's real AWS SCIM target setting in these tests.
    this.originalRealScimOverride = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap()
        .get("grouper.aws.scim.provisioning.real");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap()
        .put("grouper.aws.scim.provisioning.real", "false");
    ScimProvisionerTestUtils.setupAwsExternalSystem();
    GenericScim2MockServiceHandler.ensureScimMockTables();
    new GcDbAccess().sql("delete from mock_scim_membership").executeSql();
    new GcDbAccess().sql("delete from mock_scim_group").executeSql();
    new GcDbAccess().sql("delete from mock_scim_user").executeSql();
    new GcDbAccess().sql("delete from mock_scim_capture").executeSql();
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignConfigId(defaultConfigId()).assignChangelogConsumerConfigId("awsScimProvTestCLC")
        .assignProvisioningStrategy("generic")
        .assignBearerTokenExternalSystemConfigId("awsConfigId").assignScimType("generic")
        .assignGroupAttributeCount(2).assignBearer(true)
        .assignEntityDeleteType("deleteEntitiesIfGrouperDeleted")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("replaceMemberships", "false")
        .addExtraConfig("selectMemberships", "true")
        .addExtraConfig("scimRetrieveMembershipsByUser", "true")
        .addExtraConfig("scimRetrieveMembershipsByGroup", "false")
        .addExtraConfig("scimMembershipBatchSize", "1")
        .addExtraConfig("membershipStrategy", "membershipsInUserObjectsWhenRetrievingIndividualUsers")
        .addExtraConfig("scimUserGroupsRequireAttributes", "true"));
    assertEquals("Google-like tests must use the generic SCIM type", "generic",
        GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + defaultConfigId() + ".scimType"));
    mockSetting("userGroupsRequireAttributes", "true");
    mockSetting("userGroupsAllowUnfilteredLists", "");
    mockSetting("userGroupsStatus", "");
    mockSetting("userGroupsResponse", "");
    mockSetting("membershipStrategy.mode", "");
  }

  @Override
  protected void tearDown() {
    try {
      for (Map.Entry<String, String> entry : this.originalMockSettings.entrySet()) {
        mockSetting(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
      }
      for (Map.Entry<String, String> entry : this.originalProvisionerSettings.entrySet()) {
        provisionerSetting(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
      }
      if (this.originalRealScimOverride == null) {
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap()
            .remove("grouper.aws.scim.provisioning.real");
      } else {
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap()
            .put("grouper.aws.scim.provisioning.real", this.originalRealScimOverride);
      }
    } finally {
      super.tearDown();
    }
  }

  private ScimSettings explicitSettings() {
    ScimSettings settings = new ScimSettings();
    settings.setScimUserGroupsRequireAttributes(true);
    return settings;
  }

  private GrouperScim2User seedUser() {
    GrouperScim2User user = new GrouperScim2User();
    user.setId("explicit-groups-user");
    user.setUserName("explicit-groups@example.edu");
    user.setGivenName("Original");
    user.setFamilyName("User");
    user.setEmailValue("explicit-groups@example.edu");
    user.setActive(false);
    user.setSchemas("urn:ietf:params:scim:schemas:core:2.0:User");
    HibernateSession.byObjectStatic().save(user);
    return user;
  }

  private int groupsReadCount() {
    return new GcDbAccess().sql("select count(*) from mock_scim_capture where capture_key like ?")
        .addBindVar("userGroupsRead_%").select(int.class);
  }

  private int membershipCount() {
    return new GcDbAccess().sql("select count(*) from mock_scim_membership").select(int.class);
  }

  public void testOmittedGroupsAreEmptyOnlyAfterExplicitRead() {
    GrouperScim2User user = seedUser();
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    GrouperScim2User actual = GrouperScim2ApiCommands.retrieveScimUser("awsConfigId", "userName",
        user.getUserName(), cache, explicitSettings());
    assertEquals(user.getId(), actual.getId());
    assertEquals("Original", actual.getGivenName());
    assertEquals(Boolean.FALSE, actual.getActive());
    assertFalse(cache.getUserIdsRetrievedMemberships().contains(user.getId()));
    assertTrue(cache.getUserIdsRetrieved().contains(user.getId()));

    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), cache, explicitSettings());
    assertTrue(cache.getUserIdsRetrievedMemberships().contains(user.getId()));
    assertTrue(cache.getUserIdToMembershipGroupIds().isEmpty());
    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), cache, explicitSettings());
    assertEquals("An empty result must also be cached", 1, groupsReadCount());
    assertEquals("Original", actual.getGivenName());
    assertEquals(user.getEmailValue(), actual.getEmailValue());
    assertEquals(Boolean.FALSE, actual.getActive());
  }

  public void testMultipleDirectGroupsAndExplicitEmptyArray() {
    GrouperScim2User user = seedUser();
    mockSetting("userGroupsResponse", "{\"id\":\"" + user.getId() + "\",\"groups\":["
        + "{\"value\":\"one\"},{\"value\":\"two\",\"type\":\"direct\"},"
        + "{\"value\":\"nested\",\"type\":\"indirect\"},{\"value\":\"one\"}]}");
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), cache, explicitSettings());
    Set<String> groups = cache.getUserIdToMembershipGroupIds().get(user.getId());
    assertEquals(2, groups.size());
    assertTrue(groups.contains("one"));
    assertTrue(groups.contains("two"));
    assertFalse(groups.contains("nested"));

    mockSetting("userGroupsResponse", "{\"id\":\"" + user.getId() + "\",\"groups\":[]}");
    GrouperScim2MembershipCache nextRun = new GrouperScim2MembershipCache();
    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), nextRun, explicitSettings());
    assertTrue(nextRun.getUserIdsRetrievedMemberships().contains(user.getId()));
    assertTrue(nextRun.getUserIdToMembershipGroupIds().isEmpty());
    assertEquals("A fresh run must read memberships again", 2, groupsReadCount());
  }

  public void testFailedOrMalformedReadsDoNotPublishMemberships() {
    GrouperScim2User user = seedUser();
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    for (int status : new int[] {403, 404, 500}) {
      mockSetting("userGroupsStatus", Integer.toString(status));
      assertGroupsReadFails(user.getId(), cache);
    }
    mockSetting("userGroupsStatus", "");
    for (String response : new String[] {
        "{\"id\":\"someone-else\",\"groups\":[]}",
        "{\"id\":\"" + user.getId() + "\",\"groups\":{}}",
        "{\"id\":\"" + user.getId() + "\",\"groups\":null}",
        "{\"id\":\"" + user.getId() + "\",\"groups\":[{\"value\":\"one\"},{}]}"
    }) {
      mockSetting("userGroupsResponse", response);
      assertGroupsReadFails(user.getId(), cache);
    }
    mockSetting("userGroupsResponse", "");
    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), cache, explicitSettings());
    assertTrue("A failed read must not suppress a later successful read",
        cache.getUserIdsRetrievedMemberships().contains(user.getId()));
  }

  private void assertGroupsReadFails(String userId, GrouperScim2MembershipCache cache) {
    try {
      GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", userId, cache, explicitSettings());
      fail("Expected the user groups read to fail");
    } catch (RuntimeException expected) {
      assertFalse(cache.getUserIdsRetrievedMemberships().contains(userId));
      assertTrue("No partial result may escape a malformed response",
          cache.getUserIdToMembershipGroupIds().isEmpty());
    }
  }

  public void testFlagOffKeepsEmbeddedMembershipBehavior() {
    GrouperScim2User user = seedUser();
    GrouperScim2Group group = seedGroup("legacy-group");
    seedMembership(group.getId(), user.getId());
    mockSetting("userGroupsRequireAttributes", "false");
    mockSetting("membershipStrategy.mode", "membershipsInUserObjectsWhenRetrievingIndividualUsers");
    ScimSettings defaults = new ScimSettings();
    assertFalse(defaults.isScimUserGroupsRequireAttributes());
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    GrouperScim2ApiCommands.retrieveScimUser("awsConfigId", "id", user.getId(), cache, defaults);
    assertTrue(cache.getUserIdsRetrievedMemberships().contains(user.getId()));
    assertTrue(cache.getUserIdToMembershipGroupIds().get(user.getId()).contains(group.getId()));
    assertEquals(0, groupsReadCount());
  }

  private GrouperScim2Group seedGroup(String id) {
    GrouperScim2Group group = new GrouperScim2Group();
    group.setId(id);
    group.setDisplayName(id);
    group.setSchemas("urn:ietf:params:scim:schemas:core:2.0:Group");
    Timestamp now = new Timestamp(System.currentTimeMillis());
    group.setCreated(now);
    group.setLastModified(now);
    HibernateSession.byObjectStatic().save(group);
    return group;
  }

  public void testGroupMetadataCannotPopulateExplicitUserMemberships() {
    GrouperScim2User user = seedUser();
    GrouperScim2Group group = seedGroup("non-authoritative-group");
    seedMembership(group.getId(), user.getId());
    // Even if another endpoint embeds members, only the explicit user read is authoritative.
    mockSetting("membershipStrategy.mode", "fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups");
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    GrouperScim2ApiCommands.retrieveScimGroup("awsConfigId", "id", group.getId(), cache, explicitSettings());
    assertTrue(cache.getGroupIdToMembershipUserIds().isEmpty());
    assertTrue(cache.getGroupIdsRetrievedMemberships().isEmpty());
    mockSetting("userGroupsResponse", "{\"id\":\"" + user.getId() + "\",\"groups\":[]}");
    GrouperScim2ApiCommands.retrieveScimUserGroups("awsConfigId", user.getId(), cache, explicitSettings());
    assertTrue(cache.getUserIdsRetrievedMemberships().contains(user.getId()));
    assertTrue(cache.getGroupIdToMembershipUserIds().isEmpty());
  }

  private void seedMembership(String groupId, String userId) {
    GrouperScim2Membership membership = new GrouperScim2Membership();
    membership.setId(GrouperUuid.getUuid());
    membership.setGroupId(groupId);
    membership.setUserId(userId);
    HibernateSession.byObjectStatic().save(membership);
  }

  public void testFullAndIncrementalReconciliationWithoutTargetEnumeration() {
    provisionerSetting("loadEntitiesToGenericGrouperTable", "true");
    provisionerSetting("loadGroupsToGenericGrouperTable", "true");
    provisionerSetting("loadMembershipsToGenericGrouperTable", "true");
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignName("test").save();
    Group managed = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:managed").save();
    Group keepUser = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:keepUser").save();
    managed.addMember(SubjectTestHelper.SUBJ0);
    keepUser.addMember(SubjectTestHelper.SUBJ0);
    GrouperProvisioningAttributeValue assignment = new GrouperProvisioningAttributeValue();
    assignment.setDirectAssignment(true);
    assignment.setDoProvision(defaultConfigId());
    assignment.setTargetName(defaultConfigId());
    assignment.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(assignment, stem);
    fullProvision();
    assertEquals("generic", ((GrouperScim2ProvisionerConfiguration) GrouperProvisioner
        .retrieveInternalLastProvisioner().retrieveGrouperProvisioningConfiguration()).getScimType());
    assertEquals(2, membershipCount());
    int readsBefore = groupsReadCount();
    GrouperProvisioningOutput unchanged = fullProvision();
    assertEquals(0, unchanged.getInsert());
    assertEquals(0, unchanged.getUpdate());
    assertEquals(0, unchanged.getDelete());
    assertEquals("One membership fetch per user, not per group", readsBefore + 1, groupsReadCount());
    assertEquals("The projected read must not replace user reporting attributes", SubjectTestHelper.SUBJ0.getId(),
        new GcDbAccess().sql("select value_string from grouper_prov_user_attr_v "
            + "where provisioner_name = ? and attribute_name = 'userName'")
            .addBindVar(defaultConfigId()).select(String.class));
    assertEquals(2, new GcDbAccess()
        .sql("select count(*) from grouper_prov_mship where grouper_sync_internal_id = ?")
        .addBindVar(GcGrouperSyncDao.retrieveByProvisionerName(null, defaultConfigId()).getInternalId())
        .select(int.class).intValue());

    String userId = new GcDbAccess().sql("select id from mock_scim_user").select(String.class);
    GrouperScim2Group unmanaged = seedGroup("outside-provisioner");
    seedMembership(unmanaged.getId(), userId);
    managed.deleteMember(SubjectTestHelper.SUBJ0);
    fullProvision();
    assertEquals("Keep the remaining managed membership and the unmanaged membership", 2, membershipCount());

    incrementalProvision();
    managed.addMember(SubjectTestHelper.SUBJ0);
    incrementalProvision();
    assertEquals(3, membershipCount());
    managed.deleteMember(SubjectTestHelper.SUBJ0);
    incrementalProvision();
    assertEquals(2, membershipCount());
    assertEquals(1, new GcDbAccess()
        .sql("select count(*) from mock_scim_membership where group_id = ?")
        .addBindVar(unmanaged.getId()).select(int.class).intValue());
    assertEquals(1, new GcDbAccess().sql("select count(*) from mock_scim_user").select(int.class).intValue());
    assertFalse(GrouperProvisioner.retrieveInternalLastProvisioner()
        .retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities()
        .isCanFullSyncMembershipsFromSyncBack());
  }

  public void testConfigurationTransferAndReview() {
    GrouperScim2ProvisionerConfiguration config = new GrouperScim2ProvisionerConfiguration();
    assertFalse(config.isScimUserGroupsRequireAttributes());
    config.setScimUserGroupsRequireAttributes(true);
    ScimSettings settings = new ScimSettings();
    settings.loadFromScimProvisionerConfiguration(config);
    assertTrue(settings.isScimUserGroupsRequireAttributes());
    assertNull(GrouperScim2ProvisionerConfiguration.userGroupsConfigurationError(true, false, null, false));
    assertNotNull(GrouperScim2ProvisionerConfiguration.userGroupsConfigurationError(false, false, null, false));
    assertNotNull(GrouperScim2ProvisionerConfiguration.userGroupsConfigurationError(true, false,
        "fullGroupMembershipsInGroupObjectsWhenRetrievingIndividualGroups", false));
    assertNotNull(GrouperScim2ProvisionerConfiguration.userGroupsConfigurationError(true, false, null, true));

    String propertyName = "provisioner." + defaultConfigId() + ".scimUserGroupsRequireAttributes";
    for (ConfigurationCheckResult result : new ScimUserGroupsConfigurationCheck().checkConfiguration()) {
      assertFalse(propertyName.equals(result.getPropertyName()));
    }
    provisionerSetting("scimRetrieveMembershipsByGroup", "true");
    boolean found = false;
    for (ConfigurationCheckResult result : GrouperConfigurationCheckEngine.checkConfiguration()) {
      if (propertyName.equals(result.getPropertyName())) {
        assertEquals(ConfigurationCheckSeverity.ERROR, result.getSeverity());
        found = true;
      }
    }
    assertTrue("Configuration review must register and report incompatible selection", found);
  }

  public void testBulkUserMetadataStillLoadsIndividualMemberships() {
    // Non-Google compatibility: some generic targets allow listing users but expose groups only by ID.
    provisionerSetting("selectAllEntities", "true");
    mockSetting("userGroupsAllowUnfilteredLists", "true");
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignName("test").save();
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:bulkUsers").save();
    group.addMember(SubjectTestHelper.SUBJ0);
    GrouperProvisioningAttributeValue assignment = new GrouperProvisioningAttributeValue();
    assignment.setDirectAssignment(true);
    assignment.setDoProvision(defaultConfigId());
    assignment.setTargetName(defaultConfigId());
    assignment.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(assignment, stem);
    fullProvision();
    int readsBefore = groupsReadCount();
    GrouperProvisioningOutput unchanged = fullProvision();
    assertEquals(1, membershipCount());
    assertEquals(0, unchanged.getInsert());
    assertEquals(0, unchanged.getDelete());
    assertEquals(readsBefore + 1, groupsReadCount());
  }

  public void testMembershipReadFailurePreventsReconciliationWrites() {
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignName("test").save();
    Group group = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:readFailure").save();
    group.addMember(SubjectTestHelper.SUBJ0);
    GrouperProvisioningAttributeValue assignment = new GrouperProvisioningAttributeValue();
    assignment.setDirectAssignment(true);
    assignment.setDoProvision(defaultConfigId());
    assignment.setTargetName(defaultConfigId());
    assignment.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(assignment, stem);
    fullProvision();
    assertEquals(1, membershipCount());
    mockSetting("userGroupsStatus", "403");
    fullProvision(defaultConfigId(), true);
    GrouperProvisioner failed = GrouperProvisioner.retrieveInternalLastProvisioner();
    assertTrue("The read failure must fail provisioning instead of treating memberships as empty",
        failed.getDebugMap().containsKey("exception"));
    assertEquals(1, membershipCount());
    assertEquals(0, failed.retrieveGrouperProvisioningOutput().getInsert());
    assertEquals(0, failed.retrieveGrouperProvisioningOutput().getUpdate());
    assertEquals(0, failed.retrieveGrouperProvisioningOutput().getDelete());
  }
}
