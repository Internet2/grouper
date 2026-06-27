package edu.internet2.middleware.grouper.app.okta;

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
 * Unit tests for {@link GrouperOktaProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperOktaGroup} / {@link GrouperOktaUser} typed
 * beans: the NESTED Okta shape (id at the top level, descriptive fields under {@code profile}),
 * exclusion of the id field (it is the target id column), type coercion, and -- the whole point
 * of capturing from raw JSON -- that an operator can capture ANY Okta JSON field by name/path,
 * including one the typed bean does not model at all (e.g. the user's top-level {@code status} or
 * a custom {@code /profile/*} field).
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same
 * parser the production read path uses), so the shapes match what {@code GrouperOktaApiCommands}
 * hands the capture seam -- each per-object node carrying both {@code /id} and a nested
 * {@code profile}.
 */
public class GrouperOktaProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperOktaProvisioningTargetNativeSyncTest() {
  }

  public GrouperOktaProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperOktaProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Okta user JSON in the shape {@link GrouperOktaUser#fromJson} parses: {@code id} and
   * {@code status} at the top level, descriptive fields nested under {@code profile}. The
   * top-level {@code status} and the nested {@code profile.userType} are NOT modeled by the typed
   * bean -- used to prove raw-JSON capture can reach fields (top-level and nested) the bean would
   * have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"00u123\","
      + "\"status\":\"ACTIVE\","
      + "\"created\":\"2024-01-01T00:00:00.000Z\","
      + "\"profile\":{"
      + "  \"login\":\"abc@upenn.edu\","
      + "  \"email\":\"abc@school.edu\","
      + "  \"firstName\":\"Dave\","
      + "  \"lastName\":\"Smith\","
      + "  \"userType\":\"employee\","
      + "  \"loginAttempts\":3"
      + "}"
      + "}";

  /**
   * A sample Okta group JSON in the shape {@link GrouperOktaGroup#fromJson} parses (the elements
   * of the {@code data} array from retrieveOktaGroups): {@code id} and {@code type} at the top
   * level, {@code name}/{@code description} nested under {@code profile}. The top-level
   * {@code type} and the nested {@code profile.memberCount} are not modeled by the typed bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"id\":\"00g456\","
      + "\"type\":\"OKTA_GROUP\","
      + "\"created\":\"2024-01-01T00:00:00.000Z\","
      + "\"profile\":{"
      + "  \"name\":\"testGroup\","
      + "  \"description\":\"the test group\","
      + "  \"memberCount\":7"
      + "}"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"profile\":{\"login\":\"alice\"}}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Okta user defaults: login (from {@code /profile/login}), email (from
   * {@code /profile/email}). {@code id} is the target_user_id column (not an attribute); other
   * JSON fields (top-level or nested) are not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("00u123", bean.getTargetId());                              // from top-level /id
    assertEquals("abc@upenn.edu", bean.getAttributes().get("login"));        // from /profile/login
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));       // from /profile/email
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("firstName is not a default", bean.getAttributes().containsKey("firstName"));
    assertFalse("status is not a default", bean.getAttributes().containsKey("status"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode(
        "{\"id\":\"u-2\",\"profile\":{\"email\":\"x@y.edu\"}}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("login"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Okta user JSON field by
   * name/path -- including the top-level {@code status} (via an explicit {@code /status} path) and
   * the nested {@code profile.userType}, NEITHER of which the {@link GrouperOktaUser} typed bean
   * models and the old switch-on-getter capture could never have reached. Also coerces the nested
   * {@code loginAttempts} to a Long via a declared integer type.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperOktaProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("firstName", "/profile/firstName", null),
        attr("status", "/status", null),                  // top-level, not on the typed bean
        attr("userType", "/profile/userType", null),       // nested custom, not on the typed bean
        attr("loginAttempts", "/profile/loginAttempts", "integer")));
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave", bean.getAttributes().get("firstName"));
    assertEquals("ACTIVE", bean.getAttributes().get("status"));
    assertEquals("employee", bean.getAttributes().get("userType"));
    assertEquals(Long.valueOf(3L), bean.getAttributes().get("loginAttempts"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"profile\":{\"name\":\"g\"}}");
    assertNull("group without /id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Okta group defaults: name (from {@code /profile/name}), description
   * (from {@code /profile/description}). targetId comes from the top-level {@code /id} (not
   * captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("00g456", bean.getTargetId());                                 // from top-level /id
    assertEquals("testGroup", bean.getAttributes().get("name"));                // from /profile/name
    assertEquals("the test group", bean.getAttributes().get("description"));    // from /profile/description
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("type is not a default", bean.getAttributes().containsKey("type"));
    assertFalse("memberCount is not a default", bean.getAttributes().containsKey("memberCount"));
  }

  /**
   * Operator-configured group fields: an unmodeled top-level field ({@code type}) and an unmodeled
   * nested field ({@code profile.memberCount}) are captured, and a declared {@code integer} type
   * coerces the JSON number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperOktaProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("type", "/type", null),                          // top-level, not on the typed bean
        attr("memberCount", "/profile/memberCount", "integer"))); // nested, not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("OKTA_GROUP", bean.getAttributes().get("type"));
    assertEquals(Long.valueOf(7L), bean.getAttributes().get("memberCount"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperOktaProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperOktaProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Okta defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperOktaProvisioningTargetNativeSync defaultsSync() {
    return new GrouperOktaProvisioningTargetNativeSync() {
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
  private static GrouperOktaProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperOktaProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperOktaProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperOktaProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
