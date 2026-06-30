package edu.internet2.middleware.grouper.app.interfolio;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link InterfolioProvisioningTargetNativeSync}: exercise the raw users/search JSON
 * build path in isolation - no Tomcat, no provisioning cycle, no mock.
 */
public class InterfolioProvisioningTargetNativeSyncTest extends GrouperTest {

  public InterfolioProvisioningTargetNativeSyncTest() {
  }

  public InterfolioProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new InterfolioProvisioningTargetNativeSyncTest("testBuildNativeUserAppliesDefaults"));
  }

  /** A sample Interfolio users/search result (pid is the id; external_user is not a default). */
  private static final String USER_JSON = "{"
      + "\"id\":1000001,"
      + "\"pid\":\"8000001\","
      + "\"first_name\":\"John\","
      + "\"last_name\":\"Smith\","
      + "\"email\":\"jsmith@upenn.edu\","
      + "\"external_user\":false,"
      + "\"role\":null"
      + "}";

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"email\":\"x@y.edu\"}");
    assertNull("user without /pid should not produce a bean", defaultsSync().buildNativeUserFromJson(user));
  }

  /**
   * No provisioner config -> Interfolio user defaults: first_name, last_name, email.  pid is the
   * target_user_id column (not an attribute); other JSON fields are not captured.
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(GrouperUtil.jsonJacksonNode(USER_JSON));

    assertEquals("8000001", bean.getTargetId());                            // from /pid
    assertEquals("John", bean.getAttributes().get("first_name"));
    assertEquals("Smith", bean.getAttributes().get("last_name"));
    assertEquals("jsmith@upenn.edu", bean.getAttributes().get("email"));
    assertFalse("pid is the target_user_id column, not an attribute", bean.getAttributes().containsKey("pid"));
    assertFalse("external_user is not a default", bean.getAttributes().containsKey("external_user"));
  }

  /** A default field absent from the JSON writes no attribute row (silently skipped). */
  public void testBuildNativeUserSkipsMissingDefaults() {
    JsonNode user = GrouperUtil.jsonJacksonNode("{\"pid\":\"u-2\",\"email\":\"x@y.edu\"}");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUserFromJson(user);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("x@y.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("first_name"));
    assertFalse(bean.getAttributes().containsKey("last_name"));
  }

  /** The static dispatcher is a no-op (does not crash) when there is no current provisioner. */
  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    InterfolioProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(GrouperUtil.jsonJacksonNode(USER_JSON));
  }

  /**
   * Sync that returns the built-in Interfolio defaults without consulting a live provisioner (the
   * build path only needs effectiveNativeAttributeConfigsEntities()).
   */
  private static InterfolioProvisioningTargetNativeSync defaultsSync() {
    return new InterfolioProvisioningTargetNativeSync() {
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
