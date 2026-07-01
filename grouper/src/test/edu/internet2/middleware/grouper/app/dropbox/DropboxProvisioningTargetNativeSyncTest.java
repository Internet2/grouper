package edu.internet2.middleware.grouper.app.dropbox;

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
 * Unit tests for {@link DropboxProvisioningTargetNativeSync}: exercise the raw-JSON build path
 * (group + user) and the membership capture in isolation -- no Tomcat, no provisioning cycle, no
 * mock. Mirrors {@code TrueFoundryProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in the move off the {@link DropboxUser} / {@link DropboxGroup} typed beans: the
 * default keys ({@code email} + {@code status} for users; {@code name} + {@code externalId} for
 * groups), exclusion of the id field (it is the target id column), JSON-Pointer capture of nested
 * fields (the {@code /profile/...} wrapper, Dropbox union {@code .tag} values), type coercion, and
 * -- the whole point of capturing from raw JSON -- that an operator can capture ANY Dropbox JSON
 * field by name/path, including one the typed bean does not model at all.</p>
 *
 * <p>Dropbox JSON is UNIFORM across endpoints, so (unlike TrueFoundry) there is no normalized
 * capture node: the fixtures below are the raw Dropbox nodes exactly as the read path parses them.
 * Build JsonNodes via {@link GrouperUtil#jsonJacksonNode(String)} (the same parser the production
 * read path uses).</p>
 */
public class DropboxProvisioningTargetNativeSyncTest extends GrouperTest {

  public DropboxProvisioningTargetNativeSyncTest() {
  }

  public DropboxProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new DropboxProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A sample Dropbox member node in the shape members/list_v2 / get_info_v2 return: the profile is
   * wrapped under {@code /profile}, {@code status} is a Dropbox union (value under {@code .tag}),
   * {@code name} is nested, {@code roles[]} is a sibling of the profile. {@code account_id} and
   * {@code external_id} are real fields the default capture omits (provable via operator config).
   */
  private static final String USER_JSON = "{"
      + "\"profile\":{"
      + "\"team_member_id\":\"dbmid:abc123\","
      + "\"email\":\"abc@school.edu\","
      + "\"external_id\":\"abc\","
      + "\"account_id\":\"dbid:acc999\","
      + "\"status\":{\".tag\":\"active\"},"
      + "\"name\":{\"given_name\":\"Dave\",\"surname\":\"Smith\",\"display_name\":\"Dave Smith\"}"
      + "},"
      + "\"roles\":[{\"role_id\":\"pid_dbtmr:team_admin\",\"name\":\"Team_Admin\"}]"
      + "}";

  /**
   * A sample Dropbox group node as groups/list and groups/get_info return: {@code group_id},
   * {@code group_name}, {@code group_external_id}, the {@code group_management_type} union, and a
   * numeric {@code member_count} (for integer coercion).
   */
  private static final String GROUP_JSON = "{"
      + "\"group_id\":\"g:abc123\","
      + "\"group_name\":\"engineering\","
      + "\"group_external_id\":\"1000037\","
      + "\"group_management_type\":{\".tag\":\"company_managed\"},"
      + "\"member_count\":7"
      + "}";

  // ===================== user build (JSON -> native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode member = GrouperUtil.jsonJacksonNode("{\"profile\":{\"email\":\"alice@x.edu\"}}");
    assertNull("member without a team_member_id should not produce a bean",
        defaultsSync().buildNativeUserFromJson(member));
  }

  /**
   * No provisioner config -> Dropbox user defaults: email (from /profile/email) and status (from the
   * /profile/status/.tag union). {@code team_member_id} is the target_user_id column (not an
   * attribute); other fields (external_id, account_id, the nested name) are not captured unless an
   * operator configures them.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean =
        defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("dbmid:abc123", bean.getTargetId());                       // from /profile/team_member_id
    assertEquals("abc@school.edu", bean.getAttributes().get("email"));
    assertEquals("active", bean.getAttributes().get("status"));             // union .tag
    assertFalse("team_member_id is the target_user_id column, not an attribute",
        bean.getAttributes().containsKey("team_member_id"));
    assertFalse("external_id is not a default", bean.getAttributes().containsKey("external_id"));
    assertFalse("account_id is not a default", bean.getAttributes().containsKey("account_id"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode member = GrouperUtil.jsonJacksonNode(
        "{\"profile\":{\"team_member_id\":\"dbmid:u2\",\"email\":\"x@y.edu\"}}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(member);

    assertEquals("dbmid:u2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse("status absent from JSON -> no attribute row", bean.getAttributes().containsKey("status"));
  }

  /**
   * A bare profile node (team_member_id at the top level, no {@code profile} wrapper) still resolves
   * the target id via the {@code /team_member_id} fallback path.
   */
  public void testBuildNativeUserBareProfileFallbackId() {
    JsonNode member = GrouperUtil.jsonJacksonNode(
        "{\"team_member_id\":\"dbmid:bare\",\"email\":\"bare@x.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(member);

    assertNotNull("bare profile should still produce a bean via the fallback id", bean);
    assertEquals("dbmid:bare", bean.getTargetId());
  }

  /**
   * The point of capturing from raw JSON: an operator can capture any Dropbox member field by
   * name/path -- including {@code account_id} and {@code external_id} (real fields the default list
   * omits), a nested name field via a JSON Pointer {@code path} (= {@code /profile/name/given_name}),
   * and even the admin role name from the sibling {@code roles[]} array.
   */
  public void testBuildNativeUserCapturesOperatorConfiguredFields() {
    DropboxProvisioningTargetNativeSync sync = syncWithEntityAttrs(Arrays.asList(
        attr("externalId", "/profile/external_id", null),
        attr("accountId", "/profile/account_id", null),
        attr("givenName", "/profile/name/given_name", null),
        attr("adminRole", "/roles/0/name", null)));
    GrouperProvisioningTargetNativeUser bean =
        sync.buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("abc", bean.getAttributes().get("externalId"));
    assertEquals("dbid:acc999", bean.getAttributes().get("accountId"));
    assertEquals("Dave", bean.getAttributes().get("givenName"));
    assertEquals("Team_Admin", bean.getAttributes().get("adminRole"));
  }

  // ===================== group build (JSON -> native bean) =====================

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroupFromJson(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"group_name\":\"g\"}");
    assertNull("group without /group_id should not produce a bean",
        defaultsSync().buildNativeGroupFromJson(group));
  }

  /**
   * No provisioner config -> Dropbox group defaults: name (from /group_name) and externalId (from
   * /group_external_id, the Grouper match key). targetId comes from /group_id (not captured as an
   * attribute). The management-type union and member_count are present but not defaults.
   */
  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean =
        defaultsSync().buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals("g:abc123", bean.getTargetId());                           // from /group_id
    assertEquals("engineering", bean.getAttributes().get("name"));          // from /group_name
    assertEquals("1000037", bean.getAttributes().get("externalId"));        // from /group_external_id
    assertFalse("group_id is the target_group_id column, not an attribute",
        bean.getAttributes().containsKey("group_id"));
    assertFalse("member_count is not a default", bean.getAttributes().containsKey("member_count"));
    assertFalse("management type is not a default", bean.getAttributes().containsKey("managementType"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeGroupSkipsMissingDefaults() {
    JsonNode group = GrouperUtil.jsonJacksonNode("{\"group_id\":\"g:2\",\"group_name\":\"ops\"}");
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroupFromJson(group);

    assertEquals("g:2", bean.getTargetId());
    assertEquals("ops", bean.getAttributes().get("name"));
    assertFalse("group_external_id absent -> no externalId attribute row",
        bean.getAttributes().containsKey("externalId"));
  }

  /**
   * Operator-configured group fields: an integer-typed {@code member_count} coerces the JSON number
   * to a Long, and the management type is reached inside the Dropbox union via a JSON Pointer
   * {@code path} (= {@code /group_management_type/.tag}).
   */
  public void testBuildNativeGroupCapturesOperatorConfiguredFieldsWithCoercion() {
    DropboxProvisioningTargetNativeSync sync = syncWithGroupAttrs(Arrays.asList(
        attr("memberCount", "/member_count", "integer"),
        attr("managementType", "/group_management_type/.tag", null)));
    GrouperProvisioningTargetNativeGroup bean =
        sync.buildNativeGroupFromJson(GrouperUtil.jsonJacksonNode(GROUP_JSON));

    assertEquals(Long.valueOf(7L), bean.getAttributes().get("memberCount"));
    assertEquals("company_managed", bean.getAttributes().get("managementType"));
  }

  /**
   * Type coercion: a declared {@code integer} coerces a JSON number to a Long, a declared
   * {@code boolean} coerces a JSON boolean to a Boolean, and auto-detect (no declared type) yields a
   * Boolean for a JSON boolean. Uses a synthetic node so the coercion is exercised independent of
   * any specific Dropbox field.
   */
  public void testCoercionIntegerAndBoolean() {
    JsonNode node = GrouperUtil.jsonJacksonNode("{\"group_id\":\"g:c\",\"num\":5,\"flag\":true}");

    DropboxProvisioningTargetNativeSync declared = syncWithGroupAttrs(Arrays.asList(
        attr("num", "/num", "integer"),
        attr("flag", "/flag", "boolean")));
    GrouperProvisioningTargetNativeGroup declaredBean = declared.buildNativeGroupFromJson(node);
    assertEquals(Long.valueOf(5L), declaredBean.getAttributes().get("num"));
    assertEquals(Boolean.TRUE, declaredBean.getAttributes().get("flag"));

    DropboxProvisioningTargetNativeSync auto = syncWithGroupAttrs(Arrays.asList(
        attr("flag", "/flag", null)));
    assertEquals(Boolean.TRUE, auto.buildNativeGroupFromJson(node).getAttributes().get("flag"));
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DropboxProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DropboxProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(
        GrouperUtil.jsonJacksonNode(GROUP_JSON));
  }

  public void testCaptureMembershipInsertFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DropboxProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("g:abc123", "dbmid:abc123");
  }

  public void testCaptureMembershipDeleteFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    DropboxProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("g:abc123", "dbmid:abc123");
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
   * Sync that returns the built-in Dropbox defaults without consulting a live provisioner. The build
   * methods only need {@code effectiveNativeAttributeConfigs*()}, so overriding those (to the
   * protected default lists) makes the build path safe to call with no provisioner attached.
   */
  private static DropboxProvisioningTargetNativeSync defaultsSync() {
    return new DropboxProvisioningTargetNativeSync() {
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
  private static DropboxProvisioningTargetNativeSync syncWithEntityAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> entityAttrs) {
    return new DropboxProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsEntities() {
        return entityAttrs;
      }
    };
  }

  /** Sync whose group capture list is exactly {@code groupAttrs} (simulates operator config). */
  private static DropboxProvisioningTargetNativeSync syncWithGroupAttrs(
      final List<GrouperProvisioningNativeAttributeConfig> groupAttrs) {
    return new DropboxProvisioningTargetNativeSync() {
      @Override
      public List<GrouperProvisioningNativeAttributeConfig> effectiveNativeAttributeConfigsGroups() {
        return groupAttrs;
      }
    };
  }

}
