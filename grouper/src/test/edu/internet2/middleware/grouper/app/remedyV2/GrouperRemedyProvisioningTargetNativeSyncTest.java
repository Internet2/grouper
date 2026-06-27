package edu.internet2.middleware.grouper.app.remedyV2;

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
 * Unit tests for {@link GrouperRemedyProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperRemedyGroup} / {@link GrouperRemedyUser} typed
 * beans: the friendlier default keys ("permissionGroup" from {@code /values/Permission Group},
 * "remedyLoginId" from {@code /values/Remedy Login ID}), exclusion of the id field (it is the
 * target id column), type coercion, and -- the whole point of capturing from raw JSON -- that an
 * operator can capture ANY Remedy JSON field by name/path, including one the typed bean does not
 * model at all.
 *
 * <p><b>Remedy "values" envelope:</b> a Remedy entry is shaped
 * <code>{"values":{...},"_links":{...}}</code> and the capture seam hands over the whole entry
 * node, so every pointer is rooted at {@code /values/...} (and {@code _links} is reachable too).
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code GrouperRemedyApiCommands} hands
 * the capture seam.
 */
public class GrouperRemedyProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperRemedyProvisioningTargetNativeSyncTest() {
  }

  public GrouperRemedyProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperRemedyProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Remedy user entry JSON in the shape {@code GrouperRemedyApiCommands.convertRemedyUsersFromJson}
   * parses (the elements of the {@code entries} array from CTM:People). Fields live under the
   * {@code values} envelope. Includes {@code Full Name}, which the {@link GrouperRemedyUser} typed
   * bean does NOT model -- used to prove raw-JSON capture can reach a field the bean would have
   * silently dropped -- and the {@code _links} envelope (also unmodeled).
   */
  private static final String USER_JSON = "{"
      + "\"values\":{"
      + "\"Person ID\":\"PPL000000000616\","
      + "\"Remedy Login ID\":\"benoff\","
      + "\"Full Name\":\"Ben Off\","
      + "\"Profile Status\":\"Enabled\""
      + "},"
      + "\"_links\":{\"self\":[{\"href\":\"https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000616\"}]}"
      + "}";

  /**
   * A sample Remedy group entry JSON in the shape {@code GrouperRemedyApiCommands.retrieveRemedyGroups}
   * parses (the elements of the {@code entries} array from ENT:SYS-Access Permission Grps). Fields
   * live under the {@code values} envelope; {@code Permission Group ID} is a JSON number (the source
   * sends it numeric). {@code Status} is not modeled by the typed group bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"values\":{"
      + "\"Status\":\"Enabled\","
      + "\"Permission Group\":\"2000000001\","
      + "\"Permission Group ID\":2000000001"
      + "},"
      + "\"_links\":{\"self\":[{\"href\":\"https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/2000000001\"}]}"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"values\":{\"Remedy Login ID\":\"alice\"}}");
    assertNull("user without /values/Person ID should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Remedy user defaults: remedyLoginId (from
   * {@code /values/Remedy Login ID}). {@code Person ID} is the target_user_id column (not an
   * attribute); other JSON fields are not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("PPL000000000616", bean.getTargetId());                            // from /values/Person ID
    assertEquals("benoff", bean.getAttributes().get("remedyLoginId"));              // from /values/Remedy Login ID
    assertFalse("Person ID is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("personId"));
    assertFalse("Person ID is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("Person ID"));
    assertFalse("Full Name is not a default", bean.getAttributes().containsKey("Full Name"));
    assertFalse("Profile Status is not a default", bean.getAttributes().containsKey("Profile Status"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"values\":{\"Person ID\":\"PPL-2\"}}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("PPL-2", bean.getTargetId());
    assertFalse(bean.getAttributes().containsKey("remedyLoginId"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Remedy user JSON field by
   * name/path -- including {@code Full Name} under the values envelope, which the
   * {@link GrouperRemedyUser} typed bean does not model and the old switch-on-getter capture could
   * never have reached. Also captures the unmodeled {@code _links} href.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperRemedyProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("profileStatus", "/values/Profile Status", null),
        attr("fullName", "/values/Full Name", null),       // not on the typed bean
        attr("selfHref", "/_links/self/0/href", null)));    // envelope field, not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Enabled", bean.getAttributes().get("profileStatus"));
    assertEquals("Ben Off", bean.getAttributes().get("fullName"));
    assertEquals("https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000616",
        bean.getAttributes().get("selfHref"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"values\":{\"Permission Group\":\"g\"}}");
    assertNull("group without /values/Permission Group ID should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Remedy group defaults: permissionGroup (from
   * {@code /values/Permission Group}). targetId comes from {@code /values/Permission Group ID}
   * (not captured as an attribute), and the numeric source value is rendered as its text form.
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("2000000001", bean.getTargetId());                                 // from /values/Permission Group ID
    assertEquals("2000000001", bean.getAttributes().get("permissionGroup"));        // from /values/Permission Group
    assertFalse("Permission Group ID is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("permissionGroupId"));
    assertFalse("Permission Group ID is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("Permission Group ID"));
    assertFalse("Status is not a default", bean.getAttributes().containsKey("Status"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code Status}) is captured, and a
   * declared {@code integer} type coerces the JSON number {@code Permission Group ID} to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperRemedyProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("permissionGroupIdNumber", "/values/Permission Group ID", "integer"),
        attr("status", "/values/Status", null)));           // not on the typed group bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(2000000001L), bean.getAttributes().get("permissionGroupIdNumber"));
    assertEquals("Enabled", bean.getAttributes().get("status"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperRemedyProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperRemedyProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Remedy defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperRemedyProvisioningTargetNativeSync defaultsSync() {
    return new GrouperRemedyProvisioningTargetNativeSync() {
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
  private static GrouperRemedyProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperRemedyProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperRemedyProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperRemedyProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
