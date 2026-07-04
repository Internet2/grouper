package edu.internet2.middleware.grouper.app.datadog;

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
 * Unit tests for {@link DatadogProvisioningTargetNativeSync}: exercise the raw-JSON build path
 * (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link DatadogGroup} / {@link DatadogUser} typed beans. Because
 * Datadog speaks JSON:API, every object is an envelope
 * <code>{ "id": ..., "type": ..., "attributes": { ... } }</code>: the target id is the top-level
 * {@code /id} and the real fields live under {@code /attributes}, so the defaults are nested
 * pointers ({@code /attributes/handle}, {@code /attributes/name}, ...). The tests assert default
 * capture, exclusion of the id field (it is the target id column), type coercion, and -- the whole
 * point of capturing from raw JSON -- that an operator can capture ANY Datadog JSON field by
 * name/path, including one the typed bean does not model at all.
 *
 * <p>{@code groupType} is special: it is NOT in the Datadog response JSON (the connector knows it
 * from which endpoint produced the object), so the commands overlay it onto the envelope via
 * {@link DatadogProvisioningTargetNativeSync#nodeWithGroupType(JsonNode, String)} before capture.
 * These tests cover both the overlaid case (default groupType captured) and the raw case (no
 * groupType present).
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses), so the shapes match what {@code DatadogApiCommands} hands the
 * capture seam.
 */
public class DatadogProvisioningTargetNativeSyncTest extends GrouperTest {

  public DatadogProvisioningTargetNativeSyncTest() {
  }

  public DatadogProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new DatadogProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Datadog user JSON:API envelope in the shape {@link DatadogUser#fromJson} parses
   * (the elements of the {@code data} array from /api/v2/users). Includes
   * {@code attributes.verified}, which the typed bean does NOT model -- used to prove raw-JSON
   * capture can reach a field the bean would have silently dropped.
   */
  private static final String USER_JSON = "{"
      + "\"type\":\"users\","
      + "\"id\":\"abc123\","
      + "\"attributes\":{"
      + "\"email\":\"abc@school.edu\","
      + "\"name\":\"Dave Smith\","
      + "\"handle\":\"dsmith\","
      + "\"title\":\"Engineer\","
      + "\"disabled\":false,"
      + "\"service_account\":false,"
      + "\"verified\":true,"
      + "\"login_count\":7"
      + "}"
      + "}";

  /**
   * A sample Datadog team JSON:API envelope in the shape {@link DatadogGroup#fromJson} parses (the
   * elements of the {@code data} array from /api/v2/team), already overlaid with the synthetic
   * {@code attributes.groupType} the commands inject via {@code nodeWithGroupType}.
   * {@code description} is modeled by the bean; {@code avatar} is NOT.
   */
  private static final String GROUP_JSON = "{"
      + "\"type\":\"team\","
      + "\"id\":\"team-987\","
      + "\"attributes\":{"
      + "\"name\":\"testTeam\","
      + "\"handle\":\"test-team\","
      + "\"description\":\"the test team\","
      + "\"groupType\":\"team\","
      + "\"avatar\":\"http://example.com/a.png\","
      + "\"user_count\":3"
      + "}"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    // id lives at the top of the envelope (/id); an envelope with only attributes has no target id
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"attributes\":{\"handle\":\"alice\"}}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Datadog user defaults: handle, email, disabled (all from
   * {@code /attributes}). {@code id} is the target_user_id column (not an attribute); other JSON
   * fields are not captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc123", bean.getTargetId());                            // from top-level /id
    assertEquals("dsmith", bean.getAttributes().get("handle"));            // from /attributes/handle
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));     // from /attributes/email
    assertEquals(Boolean.FALSE, bean.getAttributes().get("disabled"));     // boolean auto-detected
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("name is not a default", bean.getAttributes().containsKey("name"));
    assertFalse("verified is not a default", bean.getAttributes().containsKey("verified"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode(
        "{\"id\":\"u-2\",\"attributes\":{\"email\":\"x@y.edu\"}}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("handle"));
    assertFalse(bean.getAttributes().containsKey("disabled"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Datadog user JSON field by
   * name/path -- including {@code verified}, which the {@link DatadogUser} typed bean does not model
   * and the old switch-on-getter capture could never have reached. Also proves a declared
   * {@code integer} type coerces the JSON number to a Long.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    DatadogProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("name", "/attributes/name", null),
        attr("title", "/attributes/title", null),
        attr("verified", "/attributes/verified", null),       // not on the typed bean
        attr("login_count", "/attributes/login_count", "integer")));
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Dave Smith", bean.getAttributes().get("name"));
    assertEquals("Engineer", bean.getAttributes().get("title"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("verified"));
    assertEquals(Long.valueOf(7L), bean.getAttributes().get("login_count"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"attributes\":{\"name\":\"g\"}}");
    assertNull("group without /id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Datadog group defaults: name, handle (from {@code /attributes}), and
   * groupType (also from {@code /attributes}, but only because the commands overlaid it via
   * {@code nodeWithGroupType} -- here the sample JSON already carries it). targetId comes from
   * the top-level {@code /id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("team-987", bean.getTargetId());                          // from top-level /id
    assertEquals("testTeam", bean.getAttributes().get("name"));            // from /attributes/name
    assertEquals("test-team", bean.getAttributes().get("handle"));         // from /attributes/handle
    assertEquals("team", bean.getAttributes().get("groupType"));           // overlaid groupType
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("description is not a default", bean.getAttributes().containsKey("description"));
    assertFalse("avatar is not a default", bean.getAttributes().containsKey("avatar"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code avatar}) is captured, a modeled
   * one ({@code description}) is captured, and a declared {@code integer} type coerces the JSON
   * number to a Long.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    DatadogProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("description", "/attributes/description", null),
        attr("avatar", "/attributes/avatar", null),           // not on the typed bean
        attr("user_count", "/attributes/user_count", "integer")));
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("the test team", bean.getAttributes().get("description"));
    assertEquals("http://example.com/a.png", bean.getAttributes().get("avatar"));
    assertEquals(Long.valueOf(3L), bean.getAttributes().get("user_count"));
  }

  // ===================== nodeWithGroupType (the groupType overlay) =====================

  /**
   * {@code groupType} is not in the raw Datadog group JSON, so without the overlay the default
   * groupType pointer resolves to nothing. {@link DatadogProvisioningTargetNativeSync#nodeWithGroupType}
   * injects it into a COPY of the envelope (original untouched), so the default capture writes it.
   */
  public void testNodeWithGroupTypeOverlaysGroupTypeWithoutMutatingOriginal() {
    // raw role envelope as Datadog returns it -- no groupType anywhere
    JsonNode rawRole = GrouperUtil.jsonJacksonNode(
        "{\"type\":\"roles\",\"id\":\"role-1\",\"attributes\":{\"name\":\"Admin\"}}");

    // without overlay: groupType default is silently skipped (not in the JSON)
    GrouperProvisioningTargetNativeGroup beanNoType = defaultsSync().buildNativeGroupFromJson(rawRole);
    assertEquals("role-1", beanNoType.getTargetId());
    assertEquals("Admin", beanNoType.getAttributes().get("name"));
    assertFalse("groupType is not in the raw JSON",
        beanNoType.getAttributes().containsKey("groupType"));

    // with overlay: groupType default now resolves to the injected "role"
    JsonNode withType = DatadogProvisioningTargetNativeSync.nodeWithGroupType(rawRole, "role");
    GrouperProvisioningTargetNativeGroup beanWithType = defaultsSync().buildNativeGroupFromJson(withType);
    assertEquals("role", beanWithType.getAttributes().get("groupType"));

    // the original node must be untouched (defensive copy)
    assertTrue("nodeWithGroupType must not mutate the original envelope",
        rawRole.at("/attributes/groupType").isMissingNode());
  }

  /** Null-safe: a null node or blank groupType returns the input unchanged (no crash). */
  public void testNodeWithGroupTypeNullSafe() {
    assertNull(DatadogProvisioningTargetNativeSync.nodeWithGroupType(null, "role"));
    JsonNode node = GrouperUtil.jsonJacksonNode("{\"id\":\"x\"}");
    // blank groupType -> unchanged input instance
    assertSame(node, DatadogProvisioningTargetNativeSync.nodeWithGroupType(node, null));
    assertSame(node, DatadogProvisioningTargetNativeSync.nodeWithGroupType(node, ""));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DatadogProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DatadogProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(GROUP_JSON));
  }

  public void testCaptureMembershipInsertFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DatadogProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("group123", "user123");
  }

  public void testCaptureMembershipDeleteFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DatadogProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("group123", "user123");
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
   * Sync that returns the built-in Datadog defaults without consulting a live provisioner. The
   * build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static DatadogProvisioningTargetNativeSync defaultsSync() {
    return new DatadogProvisioningTargetNativeSync() {
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
  private static DatadogProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new DatadogProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static DatadogProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new DatadogProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
