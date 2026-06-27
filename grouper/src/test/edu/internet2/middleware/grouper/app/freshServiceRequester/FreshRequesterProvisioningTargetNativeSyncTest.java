package edu.internet2.middleware.grouper.app.freshServiceRequester;

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
 * Unit tests for {@link FreshRequesterProvisioningTargetNativeSync}: exercise the raw-JSON build
 * path (group + user) in isolation -- no Tomcat, no provisioning cycle, no mock. Mirrors
 * {@code GrouperAdobeProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link FreshRequesterGroup} / {@link FreshRequesterUser} typed
 * beans: the friendlier default key {@code email} resolving from the Freshservice {@code /primary_email}
 * field (the field the old {@code getEmail()} getter read), exclusion of the id field (it is the
 * target id column), type coercion, and -- the whole point of capturing from raw JSON -- that an
 * operator can capture ANY Freshservice JSON field by name/path, including one the typed bean does
 * not model at all (here {@code time_zone} for users and {@code auto_ticket_assign} for groups).
 *
 * <p>Build JsonNodes from raw JSON via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser
 * the production read path uses). The shapes are the <b>inner</b> object of the Freshservice
 * envelope -- {@code FreshRequesterApiCommands} unwraps {@code {"requester":{...}}} /
 * {@code {"requester_group":{...}}} (and the array envelopes) before handing the node to the capture
 * seam -- so the target id is at {@code /id}, matching {@code FreshRequesterUser.fromJson} /
 * {@code FreshRequesterGroup.fromJson}.
 */
public class FreshRequesterProvisioningTargetNativeSyncTest extends GrouperTest {

  public FreshRequesterProvisioningTargetNativeSyncTest() {
  }

  public FreshRequesterProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new FreshRequesterProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Freshservice requester JSON in the inner shape {@link FreshRequesterUser#fromJson}
   * parses (the element of the {@code requesters} array / the inner of {@code {"requester":{...}}}).
   * Includes {@code time_zone}, which the typed bean does NOT model -- used to prove raw-JSON
   * capture can reach a field the bean would have silently dropped. Note the email lives under
   * {@code primary_email} (not {@code email}), which is exactly why the {@code email} default points
   * at {@code /primary_email}.
   */
  private static final String USER_JSON = "{"
      + "\"id\":987654,"
      + "\"primary_email\":\"jsmith@upenn.edu\","
      + "\"first_name\":\"Jane\","
      + "\"last_name\":\"Smith\","
      + "\"job_title\":\"Analyst\","
      + "\"work_phone_number\":\"555-1234\","
      + "\"department_ids\":[42],"
      + "\"is_agent\":false,"
      + "\"active\":true,"
      + "\"time_zone\":\"Eastern Time (US & Canada)\""
      + "}";

  /**
   * A sample Freshservice requester-group JSON in the inner shape
   * {@link FreshRequesterGroup#fromJson} parses (the element of the {@code requester_groups} array /
   * the inner of {@code {"requester_group":{...}}}). {@code auto_ticket_assign} is not modeled by the
   * typed bean.
   */
  private static final String GROUP_JSON = "{"
      + "\"id\":8070026,"
      + "\"name\":\"testGroup\","
      + "\"description\":\"the test group\","
      + "\"type\":\"manual\","
      + "\"member_count\":7,"
      + "\"auto_ticket_assign\":true"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"primary_email\":\"alice@upenn.edu\"}");
    assertNull("user without /id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> FreshRequester user defaults: email (from {@code /primary_email}) and
   * active. {@code id} is the target_user_id column (not an attribute); other JSON fields are not
   * captured unless an operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("987654", bean.getTargetId());                              // from /id
    assertEquals("jsmith@upenn.edu", bean.getAttributes().get("email"));     // from /primary_email
    assertEquals(Boolean.TRUE, bean.getAttributes().get("active"));          // auto-detected boolean
    assertFalse("id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("first_name is not a default", bean.getAttributes().containsKey("first_name"));
    assertFalse("time_zone is not a default", bean.getAttributes().containsKey("time_zone"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"id\":222,\"primary_email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("222", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("active"));
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Freshservice user JSON field
   * by name/path -- including {@code time_zone}, which the {@link FreshRequesterUser} typed bean does
   * not model and the old switch-on-getter capture could never have reached. Also shows the
   * friendly-key/path form ({@code firstName} from {@code /first_name}).
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFieldsIncludingUnmodeled() {
    FreshRequesterProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("firstName", "/first_name", null),     // friendly key over a path
        attr("job_title", null, null),
        attr("time_zone", null, null)));            // not on the typed bean
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("Jane", bean.getAttributes().get("firstName"));
    assertEquals("Analyst", bean.getAttributes().get("job_title"));
    assertEquals("Eastern Time (US & Canada)", bean.getAttributes().get("time_zone"));
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
   * No provisioner config -> FreshRequester group default: name (from {@code /name}). targetId comes
   * from {@code /id} (not captured as an attribute).
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("8070026", bean.getTargetId());                  // from /id
    assertEquals("testGroup", bean.getAttributes().get("name"));  // from /name
    assertFalse("id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("id"));
    assertFalse("description is not a default", bean.getAttributes().containsKey("description"));
    assertFalse("member_count is not a default", bean.getAttributes().containsKey("member_count"));
  }

  /**
   * Operator-configured group fields: an unmodeled field ({@code auto_ticket_assign}) is captured, a
   * declared {@code integer} type coerces the JSON number to a Long, and {@code description} (modeled
   * but not a default) is captured by name.
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    FreshRequesterProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("member_count", null, "integer"),
        attr("description", null, null),
        attr("auto_ticket_assign", null, null)));   // not on the typed bean
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("member_count"));
    assertEquals("the test group", bean.getAttributes().get("description"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("auto_ticket_assign"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    FreshRequesterProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    FreshRequesterProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
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
   * Sync that returns the built-in FreshRequester defaults without consulting a live provisioner.
   * The build methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to
   * the protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static FreshRequesterProvisioningTargetNativeSync defaultsSync() {
    return new FreshRequesterProvisioningTargetNativeSync() {
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
  private static FreshRequesterProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new FreshRequesterProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static FreshRequesterProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new FreshRequesterProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
