package edu.internet2.middleware.grouper.app.azure;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link GrouperAzureProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperAzureGroup} / {@link GrouperAzureUser} typed
 * beans: the default keys (displayName/mailNickname for groups; userPrincipalName/mail/
 * mailNickname/userType for users), exclusion of the id field (it is the target id column), type
 * coercion, and -- the whole point of capturing from raw JSON -- that an operator can capture ANY
 * Microsoft Graph JSON field by name/path, including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same
 * parser the production read path uses), so the shapes match what {@code GrouperAzureApiCommands}
 * hands the capture seam.
 */
public class GrouperAzureProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperAzureProvisioningTargetNativeSyncTest() {
  }

  public GrouperAzureProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperAzureProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Microsoft Graph user JSON in the shape {@link GrouperAzureUser#fromJson} parses
   * (an element of the {@code value} array from retrieveAzureUsers). Includes {@code jobTitle}
   * and {@code department}, which the typed bean does NOT model -- used to prove raw-JSON capture
   * can reach a field the bean would have silently dropped. {@code userPrincipalName} must be
   * present for {@code GrouperAzureUser.fromJson} to parse, but capture does not depend on the bean.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"54396678-d966-46b7-98cb-868a3001587e\","
      + "\"userPrincipalName\":\"adele@contoso.onmicrosoft.com\","
      + "\"mail\":\"adele@contoso.com\","
      + "\"mailNickname\":\"adele\","
      + "\"userType\":\"Member\","
      + "\"displayName\":\"Adele Vance\","
      + "\"jobTitle\":\"Retail Manager\","
      + "\"department\":\"Sales\","
      + "\"accountEnabled\":true"
      + "}";

  /**
   * A sample Microsoft Graph group JSON in the shape {@link GrouperAzureGroup#fromJson} parses
   * (an element of the {@code value} array from retrieveAzureGroups). {@code description} and
   * {@code createdDateTime} are not among the Azure defaults, and {@code memberCount} is a numeric
   * field the typed bean does not model. {@code displayName} must be present for
   * {@code GrouperAzureGroup.fromJson} to parse, but capture does not depend on the bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"id\":\"dcba5d8d-7986-432d-b23a-0342887e8fba\","
      + "\"displayName\":\"testGroup\","
      + "\"mailNickname\":\"testgroup\","
      + "\"description\":\"the test group\","
      + "\"securityEnabled\":true,"
      + "\"mailEnabled\":false,"
      + "\"memberCount\":7,"
      + "\"createdDateTime\":\"2021-01-01T00:00:00Z\""
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"userPrincipalName\":\"alice@contoso.com\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Azure user defaults: userPrincipalName, mail, mailNickname, userType.
   * {@code id} is the target_user_id column (not an attribute); other JSON fields are not captured
   * unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("54396678-d966-46b7-98cb-868a3001587e", bean.getTargetId());   // from /id
    assertEquals("adele@contoso.onmicrosoft.com", bean.getAttributes().get("userPrincipalName"));
    assertEquals("adele@contoso.com", bean.getAttributes().get("mail"));
    assertEquals("adele", bean.getAttributes().get("mailNickname"));
    assertEquals("Member", bean.getAttributes().get("userType"));
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("displayName is not a user default", bean.getAttributes().containsKey("displayName"));
    assertFalse("jobTitle is not a default", bean.getAttributes().containsKey("jobTitle"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode(
        "{\"id\":\"u-2\",\"userPrincipalName\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("userPrincipalName"));
    assertFalse(bean.getAttributes().containsKey("mail"));
    assertFalse(bean.getAttributes().containsKey("mailNickname"));
    assertFalse(bean.getAttributes().containsKey("userType"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Graph user JSON field by
   * name/path -- including {@code jobTitle} and {@code department}, which the
   * {@link GrouperAzureUser} typed bean does not model and the old switch-on-getter capture could
   * never have reached.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperAzureProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("displayName", null, null),
        attr("jobTitle", null, null),    // not on the typed bean
        attr("department", null, null))); // not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Adele Vance", bean.getAttributes().get("displayName"));
    assertEquals("Retail Manager", bean.getAttributes().get("jobTitle"));
    assertEquals("Sales", bean.getAttributes().get("department"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"displayName\":\"g\"}");
    assertNull("group without /id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Azure group defaults: displayName, mailNickname, description
   * (description is a managed group attribute, captured so a cache-reconstructed group matches a
   * live read). targetId comes from {@code /id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("dcba5d8d-7986-432d-b23a-0342887e8fba", bean.getTargetId());    // from /id
    assertEquals("testGroup", bean.getAttributes().get("displayName"));
    assertEquals("testgroup", bean.getAttributes().get("mailNickname"));
    assertEquals("the test group", bean.getAttributes().get("description"));
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("memberCount is not a default", bean.getAttributes().containsKey("memberCount"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code description}) is captured, and a
   * declared {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperAzureProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("memberCount", null, "integer"), // not on the typed bean
        attr("description", null, null)));     // not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("memberCount"));
    assertEquals("the test group", bean.getAttributes().get("description"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperAzureProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperAzureProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(GROUP_JSON));
  }

  // ===================== helpers =====================

  private static GrouperProvisioningNativeAttributeConfig attr(String name, String path, String type) {
    GrouperProvisioningNativeAttributeConfig cfg = new GrouperProvisioningNativeAttributeConfig();
    cfg.setName(name);
    cfg.setPath(path);
    cfg.setType(type);
    return cfg;
  }

  /**
   * Sync that returns the built-in Azure defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperAzureProvisioningTargetNativeSync defaultsSync() {
    return new GrouperAzureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return getDefaultNativeAttributeConfigsEntities();
      }
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return getDefaultNativeAttributeConfigsGroups();
      }
    };
  }

  /** Sync whose entity capture list is exactly {@code entityAttrs} (simulates operator config). */
  private static GrouperAzureProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperAzureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperAzureProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperAzureProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
