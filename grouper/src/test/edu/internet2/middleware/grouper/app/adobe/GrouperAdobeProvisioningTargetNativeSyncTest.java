package edu.internet2.middleware.grouper.app.adobe;

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
 * Unit tests for {@link GrouperAdobeProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperScim2ProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link GrouperAdobeGroup} / {@link GrouperAdobeUser} typed
 * beans: the friendlier default keys ("name" from {@code /groupName}, "userName" from
 * {@code /username}), exclusion of the id field (it is the target id column), type coercion, and
 * -- the whole point of capturing from raw JSON -- that an operator can capture ANY Adobe JSON
 * field by name/path, including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same
 * parser the production read path uses), so the shapes match what {@code GrouperAdobeApiCommands}
 * hands the capture seam.
 */
public class GrouperAdobeProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperAdobeProvisioningTargetNativeSyncTest() {
  }

  public GrouperAdobeProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperAdobeProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Adobe user JSON in the shape {@link GrouperAdobeUser#fromJson} parses. Includes
   * {@code phoneNumber}, which the typed bean does NOT model -- used to prove raw-JSON capture
   * can reach a field the bean would have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"id\":\"abc123\","
      + "\"email\":\"abc@school.edu\","
      + "\"status\":\"active\","
      + "\"username\":\"ABC@UPENN.EDU\","
      + "\"domain\":\"upenn.edu\","
      + "\"firstname\":\"Dave\","
      + "\"lastname\":\"Smith\","
      + "\"type\":\"federatedID\","
      + "\"country\":\"US\","
      + "\"phoneNumber\":\"555-1234\","
      + "\"groups\":[\"Group name 1\",\"Group name 2\"]"
      + "}";

  /**
   * A sample Adobe group JSON in the shape {@link GrouperAdobeGroup#fromJson} parses (the elements
   * of the {@code groups} array from retrieveAdobeGroups). {@code description} is not modeled by
   * the typed bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"groupId\":12345,"
      + "\"groupName\":\"testGroup\","
      + "\"productName\":\"Creative Cloud\","
      + "\"type\":\"USER\","
      + "\"memberCount\":7,"
      + "\"description\":\"the test group\","
      + "\"licenseQuota\":\"UNLIMITED\""
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"username\":\"alice\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Adobe user defaults: userName (from {@code /username}), email,
   * status. {@code id} is the target_user_id column (not an attribute); other JSON fields are
   * not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc123", bean.getTargetId());
    assertEquals("ABC@UPENN.EDU", bean.getAttributes().get("userName")); // from /username
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));
    assertEquals("active", bean.getAttributes().get("status"));
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("firstname is not a default", bean.getAttributes().containsKey("firstname"));
    assertFalse("phoneNumber is not a default", bean.getAttributes().containsKey("phoneNumber"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"id\":\"u-2\",\"email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("userName"));
    assertFalse(bean.getAttributes().containsKey("status"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Adobe user JSON field by
   * name/path -- including {@code phoneNumber}, which the {@link GrouperAdobeUser} typed bean does
   * not model and the old switch-on-getter capture could never have reached.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    GrouperAdobeProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("firstname", null, null),
        attr("country", null, null),
        attr("phoneNumber", null, null))); // not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave", bean.getAttributes().get("firstname"));
    assertEquals("US", bean.getAttributes().get("country"));
    assertEquals("555-1234", bean.getAttributes().get("phoneNumber"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"groupName\":\"g\"}");
    assertNull("group without /groupId should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Adobe group defaults: name (from {@code /groupName}), productName,
   * type. targetId comes from {@code /groupId} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("12345", bean.getTargetId());                               // from /groupId
    assertEquals("testGroup", bean.getAttributes().get("name"));             // from /groupName
    assertEquals("Creative Cloud", bean.getAttributes().get("productName"));
    assertEquals("USER", bean.getAttributes().get("type"));
    assertFalse("groupId is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("groupId"));
    assertFalse("memberCount is not a default", bean.getAttributes().containsKey("memberCount"));
    assertFalse("description is not a default", bean.getAttributes().containsKey("description"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code description}) is captured, and a
   * declared {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    GrouperAdobeProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("memberCount", null, "integer"),
        attr("description", null, null))); // not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("memberCount"));
    assertEquals("the test group", bean.getAttributes().get("description"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperAdobeProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GrouperAdobeProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in Adobe defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static GrouperAdobeProvisioningTargetNativeSync defaultsSync() {
    return new GrouperAdobeProvisioningTargetNativeSync() {
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
  private static GrouperAdobeProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new GrouperAdobeProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static GrouperAdobeProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new GrouperAdobeProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
