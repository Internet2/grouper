package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link AssetSonarProvisioningTargetNativeSync}: the raw member JSON build path in
 * isolation -- no Tomcat, no provisioning cycle, no mock.
 */
public class AssetSonarProvisioningTargetNativeSyncTest extends GrouperTest {

  public AssetSonarProvisioningTargetNativeSyncTest() {
  }

  public AssetSonarProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new AssetSonarProvisioningTargetNativeSyncTest("testBuildNativeUserAppliesDefaults"));
  }

  /** A trimmed AssetSonar member payload: numeric id, role_id and status, plus fields not captured */
  private static final String MEMBER_JSON = "{"
      + "\"id\":767537,"
      + "\"email\":\"jsmith@school.edu\","
      + "\"first_name\":\"John\","
      + "\"last_name\":\"Smith\","
      + "\"employee_id\":\"jsmith\","
      + "\"employee_identification_number\":null,"
      + "\"role_id\":1734,"
      + "\"role_name\":\"Agent\","
      + "\"status\":1,"
      + "\"external_id\":null,"
      + "\"creation_source\":\"ldap\""
      + "}";

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode member = GrouperUtil.jsonJacksonNode("{\"email\":\"x@y.edu\"}");
    assertNull("member without /id should not produce a bean", defaultsSync().buildNativeUserFromJson(member));
  }

  /**
   * Defaults are the managed attributes. role_id and status must come back as STRINGS: the
   * provisioner compares them as strings, so a numeric cache value would look changed every run
   * under fullSyncUsersFromSyncBack.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(MEMBER_JSON));

    assertEquals("767537", bean.getTargetId());
    assertEquals("jsmith@school.edu", bean.getAttributes().get("email"));
    assertEquals("John", bean.getAttributes().get("first_name"));
    assertEquals("Smith", bean.getAttributes().get("last_name"));
    assertEquals("jsmith", bean.getAttributes().get("employee_id"));
    assertEquals("1734", bean.getAttributes().get("role_id"));
    assertEquals("1", bean.getAttributes().get("status"));
    assertFalse("id is the target_user_id column, not an attribute", bean.getAttributes().containsKey("id"));
    assertFalse("role_name is not a default", bean.getAttributes().containsKey("role_name"));
    assertFalse("creation_source is not a default", bean.getAttributes().containsKey("creation_source"));
    // null in the JSON: no attribute, same as the provisioning entity which leaves it unassigned
    assertFalse(bean.getAttributes().containsKey("employee_identification_number"));
  }

  /** A blank string is skipped like a null, matching AssetSonarMember.toProvisioningEntity */
  public void testBuildNativeUserSkipsBlankValues() {
    JsonNode member = GrouperUtil.jsonJacksonNode("{\"id\":5,\"email\":\"x@y.edu\",\"first_name\":\"\",\"status\":0}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(member);

    assertEquals("5", bean.getTargetId());
    assertEquals("0", bean.getAttributes().get("status"));
    assertFalse(bean.getAttributes().containsKey("first_name"));
    assertFalse(bean.getAttributes().containsKey("last_name"));
  }

  /** The static hooks are no-ops (no crash) when there is no current provisioner */
  public void testStaticHooksNoCrashWhenNoProvisioner() {
    AssetSonarProvisioningTargetNativeSync.captureMemberJsonFromCurrentProvisioner(GrouperUtil.jsonJacksonNode(MEMBER_JSON));
    AssetSonarProvisioningTargetNativeSync.recordMemberWriteFromCurrentProvisioner("767537");
  }

  /**
   * Sync that returns the built-in defaults without consulting a live provisioner (the build path
   * only needs effectiveNativeAttributeConfigsEntities()).
   */
  private static AssetSonarProvisioningTargetNativeSync defaultsSync() {
    return new AssetSonarProvisioningTargetNativeSync() {
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

}
