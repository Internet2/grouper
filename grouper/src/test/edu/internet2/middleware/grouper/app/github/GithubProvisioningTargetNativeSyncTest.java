package edu.internet2.middleware.grouper.app.github;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningNativeAttributeConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeGroup;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTargetNativeUser;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link GithubProvisioningTargetNativeSync}: exercise the typed-bean build path
 * (account -&gt; native user, team -&gt; native group) in isolation -- no Tomcat, no provisioning
 * cycle, no mock. Mirrors {@code JamfProvisioningTargetNativeSyncTest}.
 *
 * <p>These lock in: the default captured keys (samlNameId/email/githubId for accounts,
 * name/org/teamType/privacy/description for teams), that the target id is the login (accounts) /
 * slug (teams) rather than the numeric GitHub id, null/id-less handling, and that the static
 * dispatchers are safe no-ops when there is no current provisioner.</p>
 */
public class GithubProvisioningTargetNativeSyncTest extends GrouperTest {

  public GithubProvisioningTargetNativeSyncTest() {
  }

  public GithubProvisioningTargetNativeSyncTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GithubProvisioningTargetNativeSyncTest("testBuildNativeUserAppliesDefaults"));
  }

  /**
   * A native-sync whose effective attribute configs are just the protocol defaults, so the build
   * path can be tested without standing up a provisioner.
   */
  private static GithubProvisioningTargetNativeSync defaultsSync() {
    return new GithubProvisioningTargetNativeSync() {
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

  private static GithubUser account(String login, String id, String samlNameId, String email) {
    GithubUser account = new GithubUser();
    account.setLogin(login);
    account.setId(id);
    account.setSamlNameId(samlNameId);
    account.setEmail(email);
    return account;
  }

  private static GithubTeam team(String slug, String name, String org, String teamType, String privacy, String description) {
    GithubTeam team = new GithubTeam();
    team.setSlug(slug);
    team.setName(name);
    team.setOrg(org);
    team.setTeamType(teamType);
    team.setPrivacy(privacy);
    team.setDescription(description);
    return team;
  }

  // ----- users -----

  public void testBuildNativeUserNullReturnsNull() {
    assertNull(defaultsSync().buildNativeUser(null));
  }

  public void testBuildNativeUserMissingLoginReturnsNull() {
    // login is the target id; without it there is nothing to key on
    assertNull(defaultsSync().buildNativeUser(account(null, "500", "test.subject.0", null)));
  }

  public void testBuildNativeUserAppliesDefaults() {
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(
        account("ghuser0", "500", "test.subject.0", "user@example.edu"));

    assertEquals("ghuser0", bean.getTargetId());
    assertEquals("test.subject.0", bean.getAttributes().get("samlNameId"));
    assertEquals("user@example.edu", bean.getAttributes().get("email"));
    assertEquals("500", bean.getAttributes().get("githubId"));
    assertFalse("login is the target_user_id column, not an attribute", bean.getAttributes().containsKey("login"));
  }

  public void testBuildNativeUserSkipsMissingDefaults() {
    // only login present (as from the org members list read) -- other defaults are simply absent
    GrouperProvisioningTargetNativeUser bean = defaultsSync().buildNativeUser(
        account("ghuser1", null, null, null));

    assertEquals("ghuser1", bean.getTargetId());
    assertFalse(bean.getAttributes().containsKey("samlNameId"));
    assertFalse(bean.getAttributes().containsKey("email"));
    assertFalse(bean.getAttributes().containsKey("githubId"));
  }

  // ----- groups (teams) -----

  public void testBuildNativeGroupNullReturnsNull() {
    assertNull(defaultsSync().buildNativeGroup(null));
  }

  public void testBuildNativeGroupMissingSlugReturnsNull() {
    assertNull(defaultsSync().buildNativeGroup(team(null, "Team A", "myorg", "organization", "closed", null)));
  }

  public void testBuildNativeGroupAppliesDefaults() {
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroup(
        team("team-a", "Team A", "myorg", "organization", "closed", "the A team"));

    assertEquals("team-a", bean.getTargetId());
    assertEquals("Team A", bean.getAttributes().get("name"));
    assertEquals("myorg", bean.getAttributes().get("org"));
    assertEquals("organization", bean.getAttributes().get("teamType"));
    assertEquals("closed", bean.getAttributes().get("privacy"));
    assertEquals("the A team", bean.getAttributes().get("description"));
    assertFalse("slug is the target_group_id column, not an attribute", bean.getAttributes().containsKey("slug"));
  }

  public void testBuildNativeGroupSkipsMissingDefaults() {
    // teams list read gives slug + name (+ org) only
    GrouperProvisioningTargetNativeGroup bean = defaultsSync().buildNativeGroup(
        team("team-b", "Team B", "myorg", null, null, null));

    assertEquals("team-b", bean.getTargetId());
    assertEquals("Team B", bean.getAttributes().get("name"));
    assertEquals("myorg", bean.getAttributes().get("org"));
    assertFalse(bean.getAttributes().containsKey("teamType"));
    assertFalse(bean.getAttributes().containsKey("privacy"));
    assertFalse(bean.getAttributes().containsKey("description"));
  }

  // ----- static dispatchers are safe no-ops with no current provisioner -----

  public void testCaptureAccountFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GithubProvisioningTargetNativeSync.captureAccountFromCurrentProvisioner(
        account("ghuser0", "500", "test.subject.0", null));
    // no exception == pass
  }

  public void testCaptureGroupFromCurrentProvisionerNoCrashWhenNoProvisioner() {
    GithubProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(
        team("team-a", "Team A", "myorg", "organization", "closed", null));
    GithubProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(
        "team-a", Arrays.asList("ghuser0", "ghuser1"));
    GithubProvisioningTargetNativeSync.captureMembershipInsertFromCurrentProvisioner("team-a", "ghuser2");
    GithubProvisioningTargetNativeSync.captureMembershipDeleteFromCurrentProvisioner("team-a", "ghuser2");
    // no exception == pass
  }

}
