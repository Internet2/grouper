package edu.internet2.middleware.grouper.app.jamf;

import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link JamfProvisioningTargetNativeSync}: exercise the typed-bean build path
 * (account -> native user, role -> native group) in isolation -- no Tomcat, no provisioning cycle,
 * no mock. Mirrors {@code TrueFoundryProvisioningTargetNativeSyncTest} /
 * {@code GrouperAzureProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in: the default captured keys (name/fullName/email/accessLevel for accounts,
 * name/accessLevel/privilegeSet/site for roles), exclusion of the id field (it is the target id
 * column), null/id-less handling, and that the static dispatchers are safe no-ops when there is no
 * current provisioner.</p>
 */
public class JamfProvisioningTargetNativeSyncTest extends GrouperTest {

  public JamfProvisioningTargetNativeSyncTest() {
  }

  public JamfProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new JamfProvisioningTargetNativeSyncTest("testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A native-sync whose effective attribute configs are just the protocol defaults, so the build
   * path can be tested without standing up a provisioner (the provisioner is what supplies
   * operator-configured native attributes and name exceptions).
   */
  private static JamfProvisioningTargetNativeSync defaultsSync() {
    return new JamfProvisioningTargetNativeSync() {
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

  private static JamfAccount account(String id, String name, String fullName, String email, String accessLevel) {
    JamfAccount account = new JamfAccount();
    account.setId(id);
    account.setName(name);
    account.setFullName(fullName);
    account.setEmail(email);
    account.setAccessLevel(accessLevel);
    account.setEnabled("Enabled");
    return account;
  }

  private static JamfAccountGroup role(String id, String name, String accessLevel, String privilegeSet, String site) {
    JamfAccountGroup group = new JamfAccountGroup();
    group.setId(id);
    group.setName(name);
    group.setAccessLevel(accessLevel);
    group.setPrivilegeSet(privilegeSet);
    group.setSiteName(site);
    return group;
  }

  // ----- users -----

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUser(null));
  }

  public void testBuildNativeUserMissingIdReturnsNull() {
    JamfAccount account = account(null, "jdoe@upenn.edu", "Jane Doe", "jdoe@upenn.edu", "Group Access");
    assertNull(defaultsSync().buildNativeUser(account));
  }

  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(
        account("101", "jdoe@upenn.edu", "Jane Doe", "jdoe@upenn.edu", "Group Access"));

    assertEquals("101", bean.getTargetId());
    assertEquals("jdoe@upenn.edu", bean.getAttributes().get("name"));
    assertEquals("Jane Doe", bean.getAttributes().get("fullName"));
    assertEquals("jdoe@upenn.edu", bean.getAttributes().get("email"));
    assertEquals("Group Access", bean.getAttributes().get("accessLevel"));
    assertFalse("id is the target_user_id column, not an attribute", bean.getAttributes().containsKey("id"));
    assertFalse("enabled is not a default attribute", bean.getAttributes().containsKey("enabled"));
  }

  public void testBuildNativeUserSkipsMissingDefaults() {
    // only id + name present (as from the accounts list read) -- other defaults are simply absent
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(
        account("102", "jdoe@upenn.edu", null, null, null));

    assertEquals("102", bean.getTargetId());
    assertEquals("jdoe@upenn.edu", bean.getAttributes().get("name"));
    assertFalse(bean.getAttributes().containsKey("fullName"));
    assertFalse(bean.getAttributes().containsKey("email"));
    assertFalse(bean.getAttributes().containsKey("accessLevel"));
  }

  // ----- groups (roles) -----

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroup(null));
  }

  public void testBuildNativeGroupMissingIdReturnsNull() {
    JamfAccountGroup group = role(null, "roleAlpha", "Full Access", "Auditor", "NONE");
    assertNull(defaultsSync().buildNativeGroup(group));
  }

  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroup(
        role("201", "roleAlpha", "Full Access", "Auditor", "University of Pennsylvania - Nursing"));

    assertEquals("201", bean.getTargetId());
    assertEquals("roleAlpha", bean.getAttributes().get("name"));
    assertEquals("Full Access", bean.getAttributes().get("accessLevel"));
    assertEquals("Auditor", bean.getAttributes().get("privilegeSet"));
    assertEquals("University of Pennsylvania - Nursing", bean.getAttributes().get("site"));
    assertFalse("id is the target_group_id column, not an attribute", bean.getAttributes().containsKey("id"));
  }

  public void testBuildNativeGroupSkipsMissingDefaults() {
    // list read gives id + name (+ site) only; access_level / privilege_set absent until detail read
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroup(
        role("202", "roleBeta", null, null, null));

    assertEquals("202", bean.getTargetId());
    assertEquals("roleBeta", bean.getAttributes().get("name"));
    assertFalse(bean.getAttributes().containsKey("accessLevel"));
    assertFalse(bean.getAttributes().containsKey("privilegeSet"));
    assertFalse(bean.getAttributes().containsKey("site"));
  }

  // ----- static dispatchers are safe no-ops with no current provisioner -----

  public void testCaptureAccountFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    JamfProvisioningTargetNativeSync.captureAccountFromCurrentProvisioner(
        account("101", "jdoe@upenn.edu", "Jane Doe", "jdoe@upenn.edu", "Group Access"));
    // no exception == pass
  }

  public void testCaptureGroupFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    JamfProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(
        role("201", "roleAlpha", "Full Access", "Auditor", "NONE"));
    JamfProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(
        "201", java.util.Arrays.asList("101", "102"));
    JamfProvisioningTargetNativeSync.captureMembershipReplaceFromCurrentProvisioner(
        "201", java.util.Arrays.asList("101"));
    JamfProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("201", "103");
    JamfProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("201", "103");
    // no exception == pass
  }

}
