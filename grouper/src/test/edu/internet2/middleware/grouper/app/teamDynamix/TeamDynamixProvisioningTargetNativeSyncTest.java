package edu.internet2.middleware.grouper.app.teamDynamix;

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
 * Unit tests for {@link TeamDynamixProvisioningTargetNativeSync}: exercise the raw-JSON build path
 * (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link TeamDynamixGroup} / {@link TeamDynamixUser} typed beans:
 * the friendlier default keys ("name" from {@code /Name}, "userName" from {@code /UserName},
 * "primaryEmail" from {@code /PrimaryEmail}, "active" from {@code /IsActive}), exclusion of the id
 * field (TeamDynamix {@code ID}/{@code UID} is the target id column), type coercion, and -- the
 * whole point of capturing from raw JSON -- that an operator can capture ANY TeamDynamix JSON field
 * by name/path, including one the typed bean does not model at all.
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code TeamDynamixApiCommands} hands the
 * capture seam.
 */
public class TeamDynamixProvisioningTargetNativeSyncTest extends GrouperTest {

  public TeamDynamixProvisioningTargetNativeSyncTest() {
  }

  public TeamDynamixProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new TeamDynamixProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample TeamDynamix user JSON in the shape {@link TeamDynamixUser#fromJson} parses (PascalCase
   * keys; id is {@code UID}). Includes {@code Title}, which the typed bean does NOT model -- used to
   * prove raw-JSON capture can reach a field the bean would have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"UID\":\"abc123\","
      + "\"UserName\":\"ABC@UPENN.EDU\","
      + "\"PrimaryEmail\":\"abc@school.edu\","
      + "\"FirstName\":\"Dave\","
      + "\"LastName\":\"Smith\","
      + "\"Company\":\"Penn\","
      + "\"SecurityRoleID\":\"role-1\","
      + "\"ExternalID\":\"ext-9\","
      + "\"TypeID\":1,"
      + "\"IsActive\":true,"
      + "\"Title\":\"Engineer\""
      + "}";

  /**
   * A sample TeamDynamix group JSON in the shape {@link TeamDynamixGroup#fromJson} parses (the
   * elements of the groups array from retrieveTeamDynamixGroups; id is {@code ID}).
   * {@code IsActive} and {@code AppCount} are not modeled by the typed bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"ID\":12345,"
      + "\"Name\":\"testGroup\","
      + "\"Description\":\"the test group\","
      + "\"IsActive\":true,"
      + "\"AppCount\":7"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"UserName\":\"alice\"}");
    assertNull("user without /UID should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> TeamDynamix user defaults: userName (from {@code /UserName}),
   * primaryEmail (from {@code /PrimaryEmail}), active (boolean, from {@code /IsActive}). {@code UID}
   * is the target_user_id column (not an attribute); other JSON fields are not captured unless an
   * operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc123", bean.getTargetId());                                // from /UID
    assertEquals("ABC@UPENN.EDU", bean.getAttributes().get("userName"));       // from /UserName
    assertEquals("abc@school.edu", bean.getAttributes().get("primaryEmail"));  // from /PrimaryEmail
    assertEquals(Boolean.TRUE, bean.getAttributes().get("active"));            // boolean from /IsActive
    assertFalse("UID is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("UID"));
    assertFalse("FirstName is not a default", bean.getAttributes().containsKey("FirstName"));
    assertFalse("Title is not a default", bean.getAttributes().containsKey("Title"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"UID\":\"u-2\",\"PrimaryEmail\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("primaryEmail"));
    assertFalse(bean.getAttributes().containsKey("userName"));
    assertFalse(bean.getAttributes().containsKey("active"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any TeamDynamix user JSON field
   * by name/path -- including {@code Title}, which the {@link TeamDynamixUser} typed bean does not
   * model and the old switch-on-getter capture could never have reached.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    TeamDynamixProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("FirstName", null, null),
        attr("Company", null, null),
        attr("Title", null, null))); // not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave", bean.getAttributes().get("FirstName"));
    assertEquals("Penn", bean.getAttributes().get("Company"));
    assertEquals("Engineer", bean.getAttributes().get("Title"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"Name\":\"g\"}");
    assertNull("group without /ID should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> TeamDynamix group defaults: name (from {@code /Name}). targetId comes
   * from {@code /ID} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("12345", bean.getTargetId());                     // from /ID
    assertEquals("testGroup", bean.getAttributes().get("name"));   // from /Name
    assertFalse("ID is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("ID"));
    assertFalse("Description is not a default", bean.getAttributes().containsKey("Description"));
    assertFalse("AppCount is not a default", bean.getAttributes().containsKey("AppCount"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code AppCount}) is captured, a declared
   * {@code integer} type coerces the JSON number to a Long, and {@code Description} (modeled but not
   * a default) is captured by name.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    TeamDynamixProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("AppCount", null, "integer"), // not on the typed bean
        attr("Description", null, null)));
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("AppCount"));
    assertEquals("the test group", bean.getAttributes().get("Description"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    TeamDynamixProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    TeamDynamixProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in TeamDynamix defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static TeamDynamixProvisioningTargetNativeSync defaultsSync() {
    return new TeamDynamixProvisioningTargetNativeSync() {
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
  private static TeamDynamixProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new TeamDynamixProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static TeamDynamixProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new TeamDynamixProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
