package edu.internet2.middleware.grouper.app.duo;

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
 * Unit tests for {@link GrouperDuoProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest} /
 * {@code GrouperScim2ProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperDuoGroup} / {@link GrouperDuoUser} typed beans:
 * the friendlier default key ("userName" from {@code /username}), the target ids drawn from
 * {@code /user_id} and {@code /group_id} (the same JSON fields the old typed-bean build read via
 * getId()/getGroup_id()), exclusion of those id fields from the attribute map, type coercion, and
 * -- the whole point of capturing from raw JSON -- that an operator can capture ANY Duo JSON field
 * by name/path, including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code GrouperDuoApiCommands} hands the
 * capture seam (the elements of the {@code response} array / object).
 */
public class GrouperDuoProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperDuoProvisioningTargetNativeSyncTest() {
  }

  public GrouperDuoProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperDuoProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Duo user JSON in the shape {@link GrouperDuoUser#fromJson} parses (an element of the
   * {@code response} array from retrieveDuoUsers). Includes {@code enable_auto_prompt}, which the
   * typed bean does NOT model -- used to prove raw-JSON capture can reach a field the bean would
   * have silently dropped. {@code last_login} is a JSON number for coercion.
   */
  private static final String USER_JSON = "{"
      + "\"user_id\":\"abc123\","
      + "\"username\":\"mchyzer\","
      + "\"email\":\"abc@school.edu\","
      + "\"status\":\"active\","
      + "\"firstname\":\"Dave\","
      + "\"lastname\":\"Smith\","
      + "\"realname\":\"Dave Smith\","
      + "\"enable_auto_prompt\":true,"
      + "\"last_login\":1727537850,"
      + "\"notes\":\"some notes\""
      + "}";

  /**
   * A sample Duo group JSON in the shape {@link GrouperDuoGroup#fromJson} parses (an element of the
   * {@code response} array from retrieveDuoGroups). {@code status} is not modeled by the typed
   * bean; {@code member_count} is a JSON number for coercion.
   */
  private static final String GROUP_JSON = "{"
      + "\"group_id\":\"DGCXPKWT7MJ7WLQT7CMQ\","
      + "\"name\":\"EarlyAdopters\","
      + "\"desc\":\"the early adopters group\","
      + "\"status\":\"Active\","
      + "\"member_count\":7"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"username\":\"alice\"}");
    assertNull("user without /user_id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Duo user defaults: userName (from {@code /username}), email, status.
   * {@code user_id} is the target_user_id column (not an attribute); other JSON fields are not
   * captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc123", bean.getTargetId());                            // from /user_id
    assertEquals("mchyzer", bean.getAttributes().get("userName"));         // from /username
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));
    assertEquals("active", bean.getAttributes().get("status"));
    assertFalse("user_id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("user_id"));
    assertFalse("username is captured under userName, not username",
        bean.getAttributes().containsKey("username"));
    assertFalse("firstname is not a default", bean.getAttributes().containsKey("firstname"));
    assertFalse("enable_auto_prompt is not a default",
        bean.getAttributes().containsKey("enable_auto_prompt"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"user_id\":\"u-2\",\"email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("userName"));
    assertFalse(bean.getAttributes().containsKey("status"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Duo user JSON field by
   * name/path -- including {@code enable_auto_prompt}, which the {@link GrouperDuoUser} typed bean
   * does not model and the old switch-on-getter capture could never have reached. A declared
   * {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperDuoProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("firstname", null, null),
        attr("enable_auto_prompt", null, null),       // not on the typed bean
        attr("last_login", null, "integer")));        // coerce JSON number -> Long
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave", bean.getAttributes().get("firstname"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("enable_auto_prompt"));
    assertEquals(Long.valueOf(1727537850L), bean.getAttributes().get("last_login"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"name\":\"g\"}");
    assertNull("group without /group_id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Duo group defaults: name (from {@code /name}). targetId comes from
   * {@code /group_id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("DGCXPKWT7MJ7WLQT7CMQ", bean.getTargetId());            // from /group_id
    assertEquals("EarlyAdopters", bean.getAttributes().get("name"));     // from /name
    assertFalse("group_id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("group_id"));
    assertFalse("desc is not a default", bean.getAttributes().containsKey("desc"));
    assertFalse("status is not a default", bean.getAttributes().containsKey("status"));
    assertFalse("member_count is not a default", bean.getAttributes().containsKey("member_count"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code status}) is captured, and a
   * declared {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperDuoProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("member_count", null, "integer"),
        attr("status", null, null)));        // not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("member_count"));
    assertEquals("Active", bean.getAttributes().get("status"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperDuoProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperDuoProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Duo defaults without consulting a live provisioner. The build
   * methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperDuoProvisioningTargetNativeSync defaultsSync() {
    return new GrouperDuoProvisioningTargetNativeSync() {
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
  private static GrouperDuoProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperDuoProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperDuoProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
