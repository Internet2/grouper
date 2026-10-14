package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link SmartsheetProvisioningTargetNativeSync}: exercise the typed-bean build path
 * (user -> native user, group -> native group) in isolation -- no Tomcat, no provisioning cycle, no
 * mock. Mirrors {@code JamfProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in: the default captured keys (every managed attribute, so a cache-reconstructed user
 * or group compares equal), the representation ("true" / "false" flags, normalized seatType, the
 * isInternal marker), exclusion of the id field (it is the target id column), null / id-less
 * handling, and that the static dispatchers are safe no-ops with no current provisioner.</p>
 */
public class SmartsheetProvisioningTargetNativeSyncTest extends GrouperTest {

  public SmartsheetProvisioningTargetNativeSyncTest() {
  }

  public SmartsheetProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new SmartsheetProvisioningTargetNativeSyncTest("testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A native-sync whose effective attribute configs are just the defaults, so the build path can be
   * tested without standing up a provisioner.
   */
  private static SmartsheetProvisioningTargetNativeSync defaultsSync() {
    return new SmartsheetProvisioningTargetNativeSync() {
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

  private static SmartsheetUser user(String id) {
    SmartsheetUser user = new SmartsheetUser();
    user.setId(id);
    user.setEmail("jdoe@school.edu");
    user.setFirstName("Jane");
    user.setLastName("Doe");
    user.setStatus("DEACTIVATED");
    user.setAdmin(false);
    user.setLicensedSheetCreator(true);
    user.setGroupAdmin(true);
    user.setResourceViewer(false);
    user.setSeatType("viewer");
    user.setInternal(false);
    return user;
  }

  private static SmartsheetGroup group(String id) {
    SmartsheetGroup group = new SmartsheetGroup();
    group.setId(id);
    group.setName("team-a");
    group.setDescription("Team A");
    group.setOwner("api@school.edu");
    group.setOwnerId("94094820842");
    return group;
  }

  // ----- users -----

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUser(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    assertNull(defaultsSync().buildNativeUser(user(null)));
  }

  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(user("4583173393803140"));

    assertEquals("4583173393803140", bean.getTargetId());
    assertEquals("jdoe@school.edu", bean.getAttributes().get("email"));
    assertEquals("Jane", bean.getAttributes().get("firstName"));
    assertEquals("Doe", bean.getAttributes().get("lastName"));
    // deactivated users are mirrored too (the mirror is the full view), with their status
    assertEquals("DEACTIVATED", bean.getAttributes().get("status"));
    // flags as the same "true" / "false" strings the DAO compares
    assertEquals("false", bean.getAttributes().get("admin"));
    assertEquals("true", bean.getAttributes().get("licensedSheetCreator"));
    assertEquals("true", bean.getAttributes().get("groupAdmin"));
    assertEquals("false", bean.getAttributes().get("resourceViewer"));
    // seatType normalized (VIEWER is CONTRIBUTOR)
    assertEquals("CONTRIBUTOR", bean.getAttributes().get("seatType"));
    assertEquals("false", bean.getAttributes().get("isInternal"));
    assertFalse("id is the target_user_id column, not an attribute", bean.getAttributes().containsKey("id"));
  }

  public void testBuildNativeUserSkipsMissingValues() {
    SmartsheetUser user = new SmartsheetUser();
    user.setId("4583173393803141");
    user.setEmail("plain@school.edu");
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(user);

    assertEquals("plain@school.edu", bean.getAttributes().get("email"));
    assertFalse(bean.getAttributes().containsKey("admin"));
    assertFalse(bean.getAttributes().containsKey("seatType"));
    assertFalse(bean.getAttributes().containsKey("isInternal"));
  }

  public void testDefaultEntityAttributesCoverManagedAttributes() {
    // every attribute the provisioner manages must be captured, or a cache-reconstructed user would
    // look changed and update every run
    List<String> names = new java.util.ArrayList<String>();
    for (GrouperProvisioningNativeAttributeConfig cfg : defaultsSync().effectiveNativeAttributeConfigsEntities()) {
      names.add(cfg.getName());
    }
    for (String managed : Arrays.asList("email", "firstName", "lastName", "admin", "licensedSheetCreator",
        "groupAdmin", "resourceViewer", "seatType", "status", "isInternal")) {
      assertTrue(managed + " should be captured: " + names, names.contains(managed));
    }
  }

  // ----- groups -----

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroup(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    assertNull(defaultsSync().buildNativeGroup(group(null)));
  }

  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroup(group("2331373580117892"));

    assertEquals("2331373580117892", bean.getTargetId());
    assertEquals("team-a", bean.getAttributes().get("name"));
    assertEquals("Team A", bean.getAttributes().get("description"));
    assertEquals("api@school.edu", bean.getAttributes().get("owner"));
    assertFalse("id is the target_group_id column, not an attribute", bean.getAttributes().containsKey("id"));
    assertFalse("ownerId is not a default attribute", bean.getAttributes().containsKey("ownerId"));
  }

  // ----- static dispatchers are safe no-ops with no current provisioner -----

  public void testDispatchersNoCrashWhenNoProvisioner() {
    SmartsheetProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(user("4583173393803140"));
    SmartsheetProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(null);
    SmartsheetProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(group("2331373580117892"));
    SmartsheetProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(
        "2331373580117892", Arrays.asList("4583173393803140", "4583173393803141"));
    SmartsheetProvisioningTargetNativeSync.captureGroupWriteFromCurrentProvisioner("2331373580117892", group("2331373580117892"));
    SmartsheetProvisioningTargetNativeSync.captureUserWriteFromCurrentProvisioner("4583173393803140");
    SmartsheetProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("2331373580117892", "4583173393803142");
    SmartsheetProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("2331373580117892", "4583173393803142");
    // no exception == pass
  }

}
