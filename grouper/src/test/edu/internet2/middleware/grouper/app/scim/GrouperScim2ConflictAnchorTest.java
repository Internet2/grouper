package edu.internet2.middleware.grouper.app.scim;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ConflictAnchor;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimSettings;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import junit.textui.TestRunner;

/**
 * Security tests for SCIM 409 "uniqueness" conflict recovery (issue
 * scimConflictAccountPreclaimRelink): a conflicting target account may only be linked (create) or
 * re-linked (rename) when it carries the exact externalId Grouper expects.  A userName-only match,
 * a different externalId, or the renamed account itself must leave an object error, keep the
 * existing link, and must not delete or disable the subject's account.
 * <p>
 * The first tests are plain unit tests.  The others need the mock SCIM servlet (tomcatRunTests),
 * like GrouperAwsProvisionerTest2; run the mock Tomcat with the daemon off.
 * </p>
 */
public class GrouperScim2ConflictAnchorTest extends GrouperProvisioningBaseTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperScim2ConflictAnchorTest("testUpdateConflictUserNameOnlyRefused"));
  }

  /**
   * @param name
   */
  public GrouperScim2ConflictAnchorTest(String name) {
    super(name);
  }

  @Override
  public String defaultConfigId() {
    return "awsProvisioner";
  }

  // ---------------------------------------------------------------- unit tests (no mock)

  /**
   * parse a json string into a node
   * @param json
   * @return node
   */
  private static JsonNode node(String json) {
    try {
      return new ObjectMapper().readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * re-linking after an update conflict is off unless the current provisioner opts in (2026-11-10);
   * with no current provisioner (or no setting) it is refused
   */
  public void testUpdateConflictRelinkOffByDefault() {
    assertFalse(edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands.updateConflictRelinkAllowed());
  }

  /**
   * exact, case-sensitive externalId compare; missing or blank values never match
   */
  public void testNodeMatchesAnchor() {
    assertTrue(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":\"ext-a\"}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":\"ext-b\"}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":\"EXT-A\"}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"userName\":\"ext-a\"}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":null}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":{\"x\":\"ext-a\"}}"), "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"id\":\"1\",\"externalId\":\"\"}"), ""));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(null, "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.nodeMatchesAnchor(node("{\"externalId\":\"ext-a\"}"), null));

    JsonNode good = node("{\"id\":\"1\",\"externalId\":\"ext-a\"}");
    assertSame(good, GrouperScim2ConflictAnchor.anchoredOrNull(good, "ext-a"));
    assertNull(GrouperScim2ConflictAnchor.anchoredOrNull(good, "ext-b"));
  }

  /**
   * the userName fallback is never allowed, with or without an expected externalId
   */
  public void testAllowUserNameFallbackNever() {
    assertFalse(GrouperScim2ConflictAnchor.allowUserNameFallback("awsConfigId", "ext-a"));
    assertFalse(GrouperScim2ConflictAnchor.allowUserNameFallback("awsConfigId", null));
    assertFalse(GrouperScim2ConflictAnchor.allowUserNameFallback("awsConfigId", ""));
  }

  /**
   * the renamed account itself is not a re-link target
   */
  public void testIsSameAccount() {
    assertTrue(GrouperScim2ConflictAnchor.isSameAccount(node("{\"id\":\"abc\"}"), "abc"));
    assertFalse(GrouperScim2ConflictAnchor.isSameAccount(node("{\"id\":\"abd\"}"), "abc"));
    assertFalse(GrouperScim2ConflictAnchor.isSameAccount(node("{}"), "abc"));
    assertFalse(GrouperScim2ConflictAnchor.isSameAccount(null, "abc"));
    assertFalse(GrouperScim2ConflictAnchor.isSameAccount(node("{\"id\":\"abc\"}"), null));
  }

  // ---------------------------------------------------------------- mock SCIM tests

  /**
   * configure the aws scim provisioner against the mock and clear the mock tables
   * @param extraConfig extra provisioner config, may be null
   */
  private void setupMock(Map<String, String> extraConfig) {
    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestConfigInput configInput = new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignUseFirstLastName(true)
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper");
    if (extraConfig != null) {
      for (String key : extraConfig.keySet()) {
        configInput.addExtraConfig(key, extraConfig.get(key));
      }
    }
    ScimProvisionerTestUtils.configureScimProvisioner(configInput);

    GrouperStartup.startup();

    // an earlier test in this JVM (e.g. LDAP) may have left its provisioner in the inheritable
    // thread local (it is not cleared when a provision throws); GrouperScim2User.fromJson would
    // then cast that provisioner's config to the SCIM config, so clear it
    GrouperProvisioner.removeCurrentGrouperProvisioner();

    // hitting the mock once makes sure the mock tables exist before we clear them
    GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId", null);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
  }

  /**
   * @return number of users in the mock
   */
  private static int mockUserCount() {
    return HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class).size();
  }

  /**
   * create a user in the mock
   * @param userName
   * @param externalId may be null
   * @return id
   */
  private static String createMockUser(String userName, String externalId) {
    GrouperScim2User user = new GrouperScim2User();
    user.setUserName(userName);
    user.setExternalId(externalId);
    user.setDisplayName(userName);
    String id = GrouperScim2ApiCommands.createScimUser("awsConfigId", user, null, new ScimSettings()).getId();
    assertNotNull(id);
    return id;
  }

  /**
   * patch A to the colliding userName and expect an error with nothing stashed
   * @param idA
   * @param externalId
   * @param fieldsToUpdate
   */
  private void assertUpdateRefused(String idA, String externalId, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {
    GrouperScim2User patch = new GrouperScim2User();
    patch.setId(idA);
    patch.setUserName("kimberley_serpico");
    patch.setExternalId(externalId);
    ScimSettings scimSettings = new ScimSettings();
    try {
      GrouperScim2ApiCommands.patchScimUser("awsConfigId", patch, fieldsToUpdate, scimSettings);
      fail("rename conflict must not be recovered with externalId " + externalId);
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), GrouperUtil.getFullStackTrace(re).contains("exact externalId"));
    }
    assertEquals(0, scimSettings.getUpdateConflictOldTargetIdToExistingUser().size());
  }

  /**
   * store one awsProvisioner property in the database config
   * @param suffix property name after provisioner.awsProvisioner.
   * @param value
   */
  private static void storeProvisionerConfig(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
        .propertyName("provisioner.awsProvisioner." + suffix).value(value).store();
  }

  @Override
  protected void setupConfigs() {
    // same subject setup as GrouperAwsProvisionerTest2 (first/last name attributes)
    RegistrySubject.assignCreateOtherAttributes(true);
    super.setupConfigs();
  }

  @Override
  protected void setupConfigsPostClearCache() {
    RegistrySubject.addFirstLastNameSubjectAttributes();
    RegistrySubject.assignCreateOtherAttributes(false);
    super.setupConfigsPostClearCache();
  }

}
