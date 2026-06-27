package edu.internet2.middleware.grouper.app.truefoundry;

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
 * Unit tests for {@link TrueFoundryProvisioningTargetNativeSync}: exercise the raw-JSON build path
 * (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest} / {@code GrouperDuoProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link TrueFoundryUser} / {@link TrueFoundryGroup} typed beans:
 * the default keys ({@code email} + {@code active} for users; {@code name} + {@code groupType} for
 * groups), exclusion of the id field (it is the target id column), type coercion, and -- the whole
 * point of capturing from raw JSON -- that an operator can capture ANY TrueFoundry JSON field by
 * name/path, including one the typed bean does not model at all and a nested field via a JSON
 * Pointer {@code path}.
 *
 * <p><b>Group JSON is the NORMALIZED capture node</b>, not the raw API node. In production the
 * commands seam ({@code TrueFoundryApiCommands.normalizeTeamJsonForCapture} /
 * {@code normalizeRoleJsonForCapture}) stamps the two synthesized fields onto a copy of the raw
 * node before handing it to the native-sync: {@code groupType} (not a TrueFoundry JSON field at
 * all) and, for teams, a top-level {@code name} aliased from {@code /teamName}. The group fixtures
 * below are shaped exactly as those helpers produce them so {@code buildNativeGroupFromJson} can be
 * tested in isolation. User JSON needs no normalization -- the subjects {@code users[]} element is
 * captured as-is.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what the capture seam hands the native-sync.
 */
public class TrueFoundryProvisioningTargetNativeSyncTest extends GrouperTest {

  public TrueFoundryProvisioningTargetNativeSyncTest() {
  }

  public TrueFoundryProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new TrueFoundryProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample TrueFoundry user JSON in the shape {@link TrueFoundryUser#fromJson} parses (an element
   * of the {@code users} array from the subjects endpoint). {@code displayName} is nested under
   * {@code metadata} (reachable only via a JSON Pointer {@code path}). {@code tenantName} is a
   * scalar the typed bean does NOT model -- used to prove raw-JSON capture can reach a field the
   * bean would have silently dropped. {@code active} is a JSON boolean for coercion.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"pt3vuwlxupmefpk8i9cj11du\","
      + "\"email\":\"abc@school.edu\","
      + "\"active\":true,"
      + "\"tenantName\":\"upenn-prod\","
      + "\"metadata\":{\"displayName\":\"Dave Smith\"},"
      + "\"rolesWithResource\":[{\"roleId\":\"r-1\"}]"
      + "}";

  /**
   * A sample TrueFoundry TEAM capture node, shaped exactly as
   * {@code TrueFoundryApiCommands.normalizeTeamJsonForCapture} produces it: the raw team node
   * ({@code id}, {@code teamName}, {@code manifest}) with the synthesized {@code groupType="team"}
   * and a top-level {@code name} aliased from {@code teamName} stamped on. {@code memberCount} is a
   * JSON number the typed bean does not model (for coercion); {@code teamName} is the raw field the
   * bean reads but is not itself a native-sync default.
   */
  private static final String TEAM_CAPTURE_JSON = "{"
      + "\"id\":\"team-123\","
      + "\"teamName\":\"platform\","
      + "\"name\":\"platform\","
      + "\"groupType\":\"team\","
      + "\"memberCount\":7,"
      + "\"manifest\":{\"members\":[\"a@x.edu\",\"b@x.edu\"]}"
      + "}";

  /**
   * A sample TrueFoundry ROLE capture node, shaped exactly as
   * {@code TrueFoundryApiCommands.normalizeRoleJsonForCapture} produces it: the raw role node
   * ({@code id}, {@code name}, {@code resourceType}, {@code isDefault}, {@code manifest}) with the
   * synthesized {@code groupType="role"} stamped on (a role's {@code name} is already the raw
   * top-level field, so no alias is added). {@code resourceType} is a raw field the typed bean
   * models but is not a native-sync default; {@code isDefault} is a JSON boolean for coercion.
   */
  private static final String ROLE_CAPTURE_JSON = "{"
      + "\"id\":\"role-456\","
      + "\"name\":\"member\","
      + "\"groupType\":\"role\","
      + "\"resourceType\":\"account\","
      + "\"isDefault\":true,"
      + "\"manifest\":{\"displayName\":\"Member\",\"description\":\"the member role\"}"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"email\":\"alice@x.edu\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> TrueFoundry user defaults: email, active. {@code id} is the
   * target_user_id column (not an attribute); other JSON fields (the nested displayName, the
   * unmodeled tenantName) are not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("pt3vuwlxupmefpk8i9cj11du", bean.getTargetId());           // from /id
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));
    // active is a JSON boolean; auto-detect coerces it to a Boolean
    assertEquals(Boolean.TRUE, bean.getAttributes().get("active"));
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("displayName is nested and not a default", bean.getAttributes().containsKey("displayName"));
    assertFalse("tenantName is not a default", bean.getAttributes().containsKey("tenantName"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"id\":\"u-2\",\"email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse("active absent from JSON -> no attribute row", bean.getAttributes().containsKey("active"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any TrueFoundry user JSON field
   * by name/path -- including {@code tenantName}, which the {@link TrueFoundryUser} typed bean does
   * not model and the old switch-on-getter capture could never have reached, and the nested
   * {@code displayName} via a JSON Pointer {@code path} (= {@code /metadata/displayName}).
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    TrueFoundryProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("tenantName", null, null),                       // not on the typed bean
        attr("displayName", "/metadata/displayName", null))); // nested, reached via JSON Pointer
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("upenn-prod", bean.getAttributes().get("tenantName"));
    assertEquals("Dave Smith", bean.getAttributes().get("displayName"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"name\":\"g\",\"groupType\":\"team\"}");
    assertNull("group without /id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> TrueFoundry group defaults applied to a TEAM capture node:
   * name + groupType. targetId comes from {@code /id} (not captured as an attribute). The raw
   * {@code teamName} and the embedded {@code manifest} are present on the node but not defaults.
   */
  public void testBuildNativeGroupAppliesDefaultsTeam() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(TEAM_CAPTURE_JSON));

    assertEquals("team-123", bean.getTargetId());                            // from /id
    assertEquals("platform", bean.getAttributes().get("name"));              // normalized from /teamName
    assertEquals("team", bean.getAttributes().get("groupType"));             // synthesized on the node
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("teamName is not a default", bean.getAttributes().containsKey("teamName"));
    assertFalse("memberCount is not a default", bean.getAttributes().containsKey("memberCount"));
    assertFalse("manifest is not a default", bean.getAttributes().containsKey("manifest"));
  }

  /**
   * The same defaults applied to a ROLE capture node: name (raw top-level for a role) + groupType.
   * Proves the single default list resolves uniformly across both TrueFoundry group shapes once
   * the seam has normalized them.
   */
  public void testBuildNativeGroupAppliesDefaultsRole() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(ROLE_CAPTURE_JSON));

    assertEquals("role-456", bean.getTargetId());                            // from /id
    assertEquals("member", bean.getAttributes().get("name"));                // raw /name for a role
    assertEquals("role", bean.getAttributes().get("groupType"));             // synthesized on the node
    assertFalse("resourceType is not a default", bean.getAttributes().containsKey("resourceType"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code memberCount}) is captured with a
   * declared {@code integer} type coercing the JSON number to a Long, the raw {@code teamName}
   * field the bean reads is capturable directly, and a nested {@code manifest} field is reachable
   * via a JSON Pointer {@code path}.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    TrueFoundryProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("memberCount", null, "integer"),  // unmodeled by the typed bean, coerced to Long
        attr("teamName", null, null),           // the raw field the bean reads for a team's name
        attr("firstMember", "/manifest/members/0", null))); // nested, reached via JSON Pointer
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(TEAM_CAPTURE_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("memberCount"));
    assertEquals("platform", bean.getAttributes().get("teamName"));
    assertEquals("a@x.edu", bean.getAttributes().get("firstMember"));
  }

  /**
   * A declared {@code boolean} type coerces a JSON boolean on a role node ({@code isDefault}),
   * and auto-detect (no declared type) also yields a Boolean.
   */
  public void testBuildNativeGroupCoercesBoolean() {
    TrueFoundryProvisioningTargetNativeSync declared = syncWithGroupAttrs(Arrays.asList(
        attr("isDefault", null, "boolean")));
    assertEquals(Boolean.TRUE, declared.buildNativeGroupFromJson(
        GrouperUtil.jsonJacksonNode(ROLE_CAPTURE_JSON)).getAttributes().get("isDefault"));

    TrueFoundryProvisioningTargetNativeSync auto = syncWithGroupAttrs(Arrays.asList(
        attr("isDefault", null, null)));
    assertEquals(Boolean.TRUE, auto.buildNativeGroupFromJson(
        GrouperUtil.jsonJacksonNode(ROLE_CAPTURE_JSON)).getAttributes().get("isDefault"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    TrueFoundryProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    TrueFoundryProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(TEAM_CAPTURE_JSON));
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
   * Sync that returns the built-in TrueFoundry defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static TrueFoundryProvisioningTargetNativeSync defaultsSync() {
    return new TrueFoundryProvisioningTargetNativeSync() {
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
  private static TrueFoundryProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new TrueFoundryProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static TrueFoundryProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new TrueFoundryProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
