package edu.internet2.middleware.grouper.app.google;

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
 * Unit tests for {@link GrouperGoogleProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest} /
 * {@code GrouperScim2ProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperGoogleGroup} / {@link GrouperGoogleUser} typed
 * beans: the default keys (group name/email, user primaryEmail/orgUnitPath), exclusion of the id
 * field (it is the target id column), type coercion, and -- the whole point of capturing from raw
 * JSON -- that an operator can capture ANY Google JSON field by name/path, including one the typed
 * bean does not model at all and including a value nested under {@code /name}.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same
 * parser the production read path uses), so the shapes match what {@code GrouperGoogleApiCommands}
 * hands the capture seam.
 */
public class GrouperGoogleProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperGoogleProvisioningTargetNativeSyncTest() {
  }

  public GrouperGoogleProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperGoogleProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Google user JSON in the shape {@link GrouperGoogleUser#fromJson} parses (id,
   * primaryEmail, orgUnitPath, and a nested name object). Includes {@code suspended} and
   * {@code isAdmin}, which the typed bean does NOT model -- used to prove raw-JSON capture can
   * reach a field (and a nested name field) the bean would have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"117982484919189471202\","
      + "\"primaryEmail\":\"liz@example.com\","
      + "\"orgUnitPath\":\"/Students\","
      + "\"suspended\":false,"
      + "\"isAdmin\":true,"
      + "\"name\":{"
      + "  \"givenName\":\"Elizabeth\","
      + "  \"familyName\":\"Smith\","
      + "  \"fullName\":\"Elizabeth Smith\""
      + "}"
      + "}";

  /**
   * A sample Google Directory group JSON in the shape {@link GrouperGoogleGroup#fromJson} parses
   * (id, email, name, description). {@code directMembersCount} and {@code adminCreated} are not
   * modeled by the typed bean -- used to prove operator-configured raw-JSON capture (incl. type
   * coercion) reaches them.
   */
  private static final String GROUP_JSON = "{"
      + "\"id\":\"02fk6b3p14s9iie\","
      + "\"email\":\"test-group@example.com\","
      + "\"name\":\"test-group\","
      + "\"description\":\"test group for grouper\","
      + "\"directMembersCount\":\"7\","
      + "\"adminCreated\":true"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"primaryEmail\":\"alice@example.com\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Google user defaults: primaryEmail, orgUnitPath. {@code id} is the
   * target_user_id column (not an attribute); other JSON fields (incl. the nested name fields) are
   * not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("117982484919189471202", bean.getTargetId());
    assertEquals("liz@example.com", bean.getAttributes().get("primaryEmail"));
    assertEquals("/Students", bean.getAttributes().get("orgUnitPath"));
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("givenName is not a default", bean.getAttributes().containsKey("givenName"));
    assertFalse("suspended is not a default", bean.getAttributes().containsKey("suspended"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode(
        "{\"id\":\"u-2\",\"primaryEmail\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("primaryEmail"));
    assertFalse(bean.getAttributes().containsKey("orgUnitPath"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Google user JSON field by
   * name/path -- including {@code givenName} (NESTED under {@code /name}, reachable only by an
   * explicit JSON Pointer) and {@code suspended} (a boolean the {@link GrouperGoogleUser} typed
   * bean does not model and the old switch-on-getter capture could never have reached). The
   * declared {@code boolean} type coerces the JSON boolean to a Boolean.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeledAndNested() {
    GrouperGoogleProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("givenName", "/name/givenName", null), // nested -- needs explicit JSON Pointer
        attr("suspended", null, "boolean")));       // not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Elizabeth", bean.getAttributes().get("givenName"));
    assertEquals(Boolean.FALSE, bean.getAttributes().get("suspended"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"name\":\"g\"}");
    assertNull("group without /id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Google group defaults: name, email. targetId comes from {@code /id}
   * (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("02fk6b3p14s9iie", bean.getTargetId());                  // from /id
    assertEquals("test-group", bean.getAttributes().get("name"));
    assertEquals("test-group@example.com", bean.getAttributes().get("email"));
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("description is not a default", bean.getAttributes().containsKey("description"));
    assertFalse("directMembersCount is not a default",
        bean.getAttributes().containsKey("directMembersCount"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code adminCreated}) is captured, and a
   * declared {@code integer} type coerces the JSON value to a Long. {@code description} (modeled on
   * the bean but not a default) is also captured when configured.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperGoogleProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("directMembersCount", null, "integer"), // not on the typed bean; coerce to Long
        attr("description", null, null)));
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("directMembersCount"));
    assertEquals("test group for grouper", bean.getAttributes().get("description"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperGoogleProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperGoogleProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Google defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperGoogleProvisioningTargetNativeSync defaultsSync() {
    return new GrouperGoogleProvisioningTargetNativeSync() {
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
  private static GrouperGoogleProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperGoogleProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperGoogleProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperGoogleProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
