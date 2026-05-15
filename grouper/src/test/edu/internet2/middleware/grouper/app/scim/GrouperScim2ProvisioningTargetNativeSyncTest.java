package edu.internet2.middleware.grouper.app.scim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeMembership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2MembershipCache;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisioningTargetNativeSync;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link GrouperScim2ProvisioningTargetNativeSync}. Exercise the JsonNode-
 * driven build path, the membership cache drain, and the static dispatchers in isolation.
 *
 * <p>Build tests construct JsonNodes from raw JSON via
 * {@link GrouperUtil#jsonJacksonNode(String)} (the same parser the production code uses),
 * so they reflect the actual SCIM response shape the daemon would see.
 *
 * <p>The base recorder methods consult a live provisioner; the tests use a friend subclass
 * to bypass the behavior gate where needed.
 */
public class GrouperScim2ProvisioningTargetNativeSyncTest extends GrouperTest {

  public GrouperScim2ProvisioningTargetNativeSyncTest() {
  }

  public GrouperScim2ProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperScim2ProvisioningTargetNativeSyncTest(
        "testBuildNativeUserAppliesDefaults"));
  }

  // ===================== build (JsonNode → native bean) =====================

  public void testBuildNativeUserNullReturnsNull() {
    GrouperScim2ProvisioningTargetNativeSync sync = noProvisionerSync();
    assertNull(sync.buildNativeUserFromJson(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    GrouperScim2ProvisioningTargetNativeSync sync = noProvisionerSync();
    JsonNode resource = GrouperUtil.jsonJacksonNode("{\"userName\":\"alice\"}");
    assertNull("user without /id should not produce a bean",
        sync.buildNativeUserFromJson(resource));
  }

  /**
   * No provisioner config → defaults apply. Expect exactly the five SCIM core fields:
   * userName, displayName, active, externalId, emailValue (from /emails/0/value).
   */
  public void testBuildNativeUserAppliesDefaults() {
    GrouperScim2ProvisioningTargetNativeSync sync = noProvisionerSync();
    String json = "{"
        + "\"id\":\"u-1\","
        + "\"userName\":\"alice\","
        + "\"displayName\":\"Alice Anderson\","
        + "\"active\":true,"
        + "\"externalId\":\"ext-1\","
        + "\"emails\":[{\"value\":\"alice@example.edu\",\"type\":\"work\",\"primary\":true}],"
        + "\"unmappedExtra\":\"shouldNotBeCaptured\""  // not in defaults → must not be captured
        + "}";
    JsonNode resource = GrouperUtil.jsonJacksonNode(json);
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser
        bean = sync.buildNativeUserFromJson(resource);

    assertEquals("u-1", bean.getTargetId());
    assertEquals("alice", bean.getAttributes().get("userName"));
    assertEquals("Alice Anderson", bean.getAttributes().get("displayName"));
    assertEquals(Boolean.TRUE, bean.getAttributes().get("active"));
    assertEquals("ext-1", bean.getAttributes().get("externalId"));
    assertEquals("alice@example.edu", bean.getAttributes().get("emailValue"));
    // id is the target_user_id column — not a separate attribute
    assertFalse("id should NOT be captured as an attribute (it's already targetId)",
        bean.getAttributes().containsKey("id"));
    // unmapped server-sent field → must not appear unless operator configured it
    assertFalse("non-default attributes must not be captured",
        bean.getAttributes().containsKey("unmappedExtra"));
  }

  /**
   * If a default-path field isn't on the JSON, no attribute row is written. Missing fields
   * are silently skipped (no NULL row).
   */
  public void testBuildNativeUserSkipsMissingDefaults() {
    GrouperScim2ProvisioningTargetNativeSync sync = noProvisionerSync();
    String json = "{\"id\":\"u-2\",\"userName\":\"bob\"}";  // only id + userName
    JsonNode resource = GrouperUtil.jsonJacksonNode(json);
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser
        bean = sync.buildNativeUserFromJson(resource);

    assertEquals("u-2", bean.getTargetId());
    assertEquals("bob", bean.getAttributes().get("userName"));
    assertFalse(bean.getAttributes().containsKey("displayName"));
    assertFalse(bean.getAttributes().containsKey("active"));
    assertFalse(bean.getAttributes().containsKey("externalId"));
    assertFalse(bean.getAttributes().containsKey("emailValue"));
  }

  public void testBuildNativeGroupAppliesDefaults() {
    GrouperScim2ProvisioningTargetNativeSync sync = noProvisionerSync();
    String json = "{"
        + "\"id\":\"g-1\","
        + "\"displayName\":\"engineering\","
        + "\"externalId\":\"ext-g-1\","
        + "\"meta\":{\"created\":\"2020-01-01T00:00:00Z\"}"  // not in defaults → skipped
        + "}";
    JsonNode resource = GrouperUtil.jsonJacksonNode(json);
    edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup
        bean = sync.buildNativeGroupFromJson(resource);

    assertEquals("g-1", bean.getTargetId());
    assertEquals("engineering", bean.getAttributes().get("displayName"));
    assertEquals("ext-g-1", bean.getAttributes().get("externalId"));
    assertFalse("id should NOT be captured (already targetId)",
        bean.getAttributes().containsKey("id"));
    assertFalse("meta.created is not in defaults, must not be captured",
        bean.getAttributes().containsKey("metaCreated"));
  }

  // ===================== membership cache drain =====================

  public void testCaptureMembershipsFromCacheNullCacheIsNoOp() {
    CapturingScimSync sync = new CapturingScimSync();
    sync.captureMembershipsFromCache(null);
    assertTrue("recorded list should be empty after null cache", sync.recordedMemberships.isEmpty());
  }

  public void testCaptureMembershipsFromCacheEmptyCacheIsNoOp() {
    CapturingScimSync sync = new CapturingScimSync();
    sync.captureMembershipsFromCache(new GrouperScim2MembershipCache());
    assertTrue("recorded list should be empty after empty cache", sync.recordedMemberships.isEmpty());
  }

  public void testCaptureMembershipsFromCacheTranslatesPairs() {
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    cache.addMembership("group-1", "user-a");
    cache.addMembership("group-1", "user-b");
    cache.addMembership("group-2", "user-a");

    CapturingScimSync sync = new CapturingScimSync();
    sync.captureMembershipsFromCache(cache);

    assertEquals(3, sync.recordedMemberships.size());
    Set<String> pairs = new HashSet<String>();
    for (GrouperProvisioningTargetNativeMembership m : sync.recordedMemberships) {
      pairs.add(m.getTargetGroupId() + ":" + m.getTargetUserId());
    }
    assertTrue(pairs.contains("group-1:user-a"));
    assertTrue(pairs.contains("group-1:user-b"));
    assertTrue(pairs.contains("group-2:user-a"));
  }

  public void testCaptureMembershipsFromCacheSkipsGroupsWithNoUsers() {
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    cache.addMembership("group-real", "user-real");
    cache.addMembershipsForGroup("group-with-no-users");

    CapturingScimSync sync = new CapturingScimSync();
    sync.captureMembershipsFromCache(cache);

    assertEquals(1, sync.recordedMemberships.size());
    assertEquals("group-real", sync.recordedMemberships.get(0).getTargetGroupId());
    assertEquals("user-real", sync.recordedMemberships.get(0).getTargetUserId());
  }

  // ===================== static dispatchers (no provisioner on ThreadLocal) =====================

  public void testCaptureUserJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    JsonNode resource = GrouperUtil.jsonJacksonNode("{\"id\":\"u\",\"userName\":\"a\"}");
    GrouperScim2ProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(resource);
  }

  public void testCaptureGroupJsonFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    JsonNode resource = GrouperUtil.jsonJacksonNode("{\"id\":\"g\",\"displayName\":\"g\"}");
    GrouperScim2ProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(resource);
  }

  public void testCaptureMembershipsFromCacheIfActiveNoCrashWhenNoProvisioner() {
    GrouperScim2MembershipCache cache = new GrouperScim2MembershipCache();
    cache.addMembership("group-x", "user-y");
    GrouperScim2ProvisioningTargetNativeSync.captureMembershipsFromCacheIfActive(cache);
  }

  // ===================== helpers =====================

  /**
   * Subclass that returns the SCIM defaults without consulting a live provisioner
   * configuration. The build methods only need {@code effectiveNativeAttributeConfigs*()},
   * which itself only calls into the provisioner for the configured list — by overriding
   * that here we make the build path safe to call with no provisioner attached.
   */
  private static GrouperScim2ProvisioningTargetNativeSync noProvisionerSync() {
    return new GrouperScim2ProvisioningTargetNativeSync() {
      @Override
      public List<edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig>
          effectiveNativeAttributeConfigsEntities() {
        return getDefaultNativeAttributeConfigsEntities();
      }
      @Override
      public List<edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig>
          effectiveNativeAttributeConfigsGroups() {
        return getDefaultNativeAttributeConfigsGroups();
      }
    };
  }

  /**
   * Subclass that bypasses the behavior gate by overriding the record* methods to write to
   * an in-memory list instead of consulting the provisioner. Lets us verify membership
   * translation logic in isolation.
   */
  private static class CapturingScimSync extends GrouperScim2ProvisioningTargetNativeSync {
    final List<GrouperProvisioningTargetNativeMembership> recordedMemberships =
        new ArrayList<GrouperProvisioningTargetNativeMembership>();

    @Override
    public void recordTargetNativeMemberships(
        List<GrouperProvisioningTargetNativeMembership> grouperProvisioningTargetNativeMemberships) {
      if (grouperProvisioningTargetNativeMemberships != null) {
        recordedMemberships.addAll(grouperProvisioningTargetNativeMemberships);
      }
    }
  }
}
