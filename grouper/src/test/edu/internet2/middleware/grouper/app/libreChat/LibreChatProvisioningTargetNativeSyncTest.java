package edu.internet2.middleware.grouper.app.libreChat;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link LibreChatProvisioningTargetNativeSync}: the typed bean to native bean build, with
 * no Tomcat and no provisioning run.  Locks in the captured keys (users: username, name, email; groups:
 * name, description, source and the raw idOnTheSource) and that the dispatchers are no-ops outside a run.
 *
 * GRP-7595
 */
public class LibreChatProvisioningTargetNativeSyncTest extends GrouperTest {

  public LibreChatProvisioningTargetNativeSyncTest() {
  }

  public LibreChatProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(LibreChatProvisioningTargetNativeSyncTest.class);
  }

  /**
   * @return a sync whose effective attributes are the defaults, so no provisioner is needed
   */
  private static LibreChatProvisioningTargetNativeSync defaultsSync() {
    return new LibreChatProvisioningTargetNativeSync() {
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

  public void testBuildNativeUser() {
    LibreChatUser user = new LibreChatUser();
    user.setId("0123456789abcdef01234567");
    user.setUsername("jdoe@upenn.edu");
    user.setName("Jane Doe");
    user.setEmail("jdoe@isc.upenn.edu");

    GrouperProvisioningTargetNativeUser nativeUser = defaultsSync().buildNativeUser(user);
    assertEquals("0123456789abcdef01234567", nativeUser.getTargetId());
    assertEquals("jdoe@upenn.edu", nativeUser.getAttributes().get("username"));
    assertEquals("Jane Doe", nativeUser.getAttributes().get("name"));
    assertEquals("jdoe@isc.upenn.edu", nativeUser.getAttributes().get("email"));
    assertEquals(3, nativeUser.getAttributes().size());
  }

  public void testBuildNativeGroupKeepsRawIdOnTheSource() {
    LibreChatGroup group = new LibreChatGroup();
    group.setId("0123456789abcdef01234568");
    group.setName("Team one");
    group.setSource(null);
    group.setIdOnTheSource("grouper:abc");

    GrouperProvisioningTargetNativeGroup nativeGroup = defaultsSync().buildNativeGroup(group);
    assertEquals("0123456789abcdef01234568", nativeGroup.getTargetId());
    assertEquals("Team one", nativeGroup.getAttributes().get("name"));
    // a missing source is LibreChat's default, local
    assertEquals("local", nativeGroup.getAttributes().get("source"));
    // stored as LibreChat has it, with the prefix, so the managed rule can be applied to cached groups
    assertEquals("grouper:abc", nativeGroup.getAttributes().get("idOnTheSource"));
    assertFalse("no description, no key", nativeGroup.getAttributes().containsKey("description"));
  }

  public void testBuildWithoutIdReturnsNull() {
    assertNull(defaultsSync().buildNativeUser(null));
    assertNull(defaultsSync().buildNativeUser(new LibreChatUser()));
    assertNull(defaultsSync().buildNativeGroup(null));
    assertNull(defaultsSync().buildNativeGroup(new LibreChatGroup()));
  }

  public void testDispatchersNoOpOutsideProvisioner() {
    LibreChatProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(new LibreChatUser());
    LibreChatProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(new LibreChatGroup());
    LibreChatProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner("g", GrouperUtil.toList("u"));
    LibreChatProvisioningTargetNativeSync.captureGroupWriteFromCurrentProvisioner("g", null);
    LibreChatProvisioningTargetNativeSync.captureGroupDeleteFromCurrentProvisioner("g");
    LibreChatProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("g", "u");
    LibreChatProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("g", "u");
  }

}
