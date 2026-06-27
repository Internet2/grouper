package edu.internet2.middleware.grouper.app.boxProvisioner;

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
 * Unit tests for {@link GrouperBoxProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest} / {@code GrouperScim2ProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperBoxGroup} / {@link GrouperBoxUser} typed beans:
 * the default keys (group name/group_type/provenance, user login/role/status/type), the exclusion
 * of the {@code id} field (it is the target id column), type coercion, and -- the whole point of
 * capturing from raw JSON -- that an operator can capture ANY Box JSON field by name/path,
 * including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code GrouperBoxApiCommands} hands the
 * capture seam.
 */
public class GrouperBoxProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperBoxProvisioningTargetNativeSyncTest() {
  }

  public GrouperBoxProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperBoxProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Box user JSON in the shape {@link GrouperBoxUser#fromJson} parses. Includes
   * {@code language}, which the typed bean does NOT model -- used to prove raw-JSON capture can
   * reach a field the bean would have silently dropped. {@code space_used} is a JSON number used to
   * exercise integer coercion.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"11446498\","
      + "\"type\":\"user\","
      + "\"login\":\"abc@school.edu\","
      + "\"name\":\"Aaron Levie\","
      + "\"role\":\"admin\","
      + "\"status\":\"active\","
      + "\"language\":\"en\","
      + "\"space_used\":2147483648,"
      + "\"space_amount\":11345156112,"
      + "\"max_upload_size\":2147483648"
      + "}";

  /**
   * A sample Box group JSON in the shape {@link GrouperBoxGroup#fromJson} parses (the elements of
   * the {@code entries} array from retrieveBoxGroups). {@code memberships_url} is not modeled by the
   * typed bean. {@code permissions.can_invite_as_collaborator} is a nested field, used to exercise
   * an explicit nested JSON Pointer path. {@code group_count} is a JSON number used to exercise
   * integer coercion.
   */
  private static final String GROUP_JSON = "{"
      + "\"id\":\"255224\","
      + "\"type\":\"group\","
      + "\"name\":\"Support\","
      + "\"group_type\":\"managed_group\","
      + "\"provenance\":\"Okta\","
      + "\"description\":\"the support group\","
      + "\"external_sync_identifier\":\"idp-group-0001\","
      + "\"group_count\":7,"
      + "\"memberships_url\":\"https://api.box.com/2.0/groups/255224/memberships\","
      + "\"permissions\":{\"can_invite_as_collaborator\":true}"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"login\":\"alice\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Box user defaults: login, role, status, type. {@code id} is the
   * target_user_id column (not an attribute); other JSON fields are not captured unless an operator
   * configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("11446498", bean.getTargetId());                  // from /id
    assertEquals("abc@school.edu", bean.getAttributes().get("login"));
    assertEquals("admin", bean.getAttributes().get("role"));
    assertEquals("active", bean.getAttributes().get("status"));
    assertEquals("user", bean.getAttributes().get("type"));
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("name is not a default", bean.getAttributes().containsKey("name"));
    assertFalse("language is not a default", bean.getAttributes().containsKey("language"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"id\":\"u-2\",\"login\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("login"));
    assertFalse(bean.getAttributes().containsKey("role"));
    assertFalse(bean.getAttributes().containsKey("status"));
    assertFalse(bean.getAttributes().containsKey("type"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Box user JSON field by
   * name/path -- including {@code language}, which the {@link GrouperBoxUser} typed bean does not
   * model and the old switch-on-getter capture could never have reached -- and a declared
   * {@code integer} type coerces the JSON number {@code space_used} to a Long.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperBoxProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("name", null, null),
        attr("language", null, null),         // not on the typed bean
        attr("space_used", null, "integer"))); // JSON number -> Long
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Aaron Levie", bean.getAttributes().get("name"));
    assertEquals("en", bean.getAttributes().get("language"));
    assertEquals(Long.valueOf(2147483648L), bean.getAttributes().get("space_used"));
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
   * No provisioner config -> Box group defaults: name, group_type, provenance. targetId comes from
   * {@code /id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("255224", bean.getTargetId());                          // from /id
    assertEquals("Support", bean.getAttributes().get("name"));
    assertEquals("managed_group", bean.getAttributes().get("group_type"));
    assertEquals("Okta", bean.getAttributes().get("provenance"));
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("description is not a default", bean.getAttributes().containsKey("description"));
    assertFalse("memberships_url is not a default",
        bean.getAttributes().containsKey("memberships_url"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code memberships_url}), a declared
   * {@code integer} type coercing the JSON number {@code group_count} to a Long, and a nested field
   * reached via an explicit JSON Pointer path ({@code /permissions/can_invite_as_collaborator}).
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperBoxProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("group_count", null, "integer"),
        attr("memberships_url", null, null), // not on the typed bean
        attr("canInviteAsCollaborator", "/permissions/can_invite_as_collaborator", "boolean")));
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("group_count"));
    assertEquals("https://api.box.com/2.0/groups/255224/memberships",
        bean.getAttributes().get("memberships_url"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("canInviteAsCollaborator"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperBoxProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperBoxProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Box defaults without consulting a live provisioner. The build
   * methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperBoxProvisioningTargetNativeSync defaultsSync() {
    return new GrouperBoxProvisioningTargetNativeSync() {
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
  private static GrouperBoxProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperBoxProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperBoxProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperBoxProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
