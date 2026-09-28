package edu.internet2.middleware.grouper.app.github;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Tests for the GitHub provisioner. The API-level tests drive
 * {@link GithubApiCommands} against the mock servlet and assert on the
 * mock_github_* tables. The sync test drives a full/incremental provision and is
 * gated behind {@link #tomcatRunTests()}.
 */
public class GithubProvisionerTest extends GrouperProvisioningBaseTest {

  /** external system config id (direct API calls) */
  private static final String CONFIG_ID = GithubProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID;

  private static final String ORG = "myorg";

  public static void main(String[] args) {
    GithubMockServiceHandler.ensureGithubMockTables();
    TestRunner.run(new GithubProvisionerTest("testAddAndRetrieveTeamMembership"));
    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return "githubProvisioner";
  }

  public GithubProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    GithubMockServiceHandler.ensureGithubMockTables();
    truncateMockTables();
  }

  private void truncateMockTables() {
    new GcDbAccess().connectionName("grouper").sql("delete from mock_github_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_github_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_github_team").executeSql();
  }

  private void insertMockTeam(String id, String slug, String name) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_github_team (id, org, slug, name, team_type, privacy, description) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(id).addBindVar(ORG).addBindVar(slug).addBindVar(name).addBindVar("organization").addBindVar("closed").addBindVar(null)
        .executeSql();
  }

  private void insertMockUser(String login, String id, String samlNameId) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_github_user (login, id, saml_name_id, email, org_state, role, invitation_id) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(login).addBindVar(id).addBindVar(samlNameId).addBindVar(null).addBindVar(null).addBindVar(null).addBindVar(null)
        .executeSql();
  }

  private GithubSettings settingsWithEnterprise() {
    GithubSettings githubSettings = new GithubSettings();
    githubSettings.setEnterpriseSlug("myenterprise");
    githubSettings.getManagedOrgs().add(ORG);
    return githubSettings;
  }

  // ============================
  // API-level tests
  // ============================

  public void testRetrieveTeams() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();

    insertMockTeam("1001", "team-a", "Team A");
    insertMockTeam("1002", "team-b", "Team B");

    List<GithubTeam> teams = GithubApiCommands.retrieveTeams(CONFIG_ID, null, ORG);
    assertEquals(2, GrouperUtil.length(teams));
    assertEquals("team-a", teams.get(0).getSlug());
    assertEquals("organization", teams.get(0).getTeamType());
  }

  public void testAddAndRetrieveTeamMembership() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();

    insertMockTeam("1001", "team-a", "Team A");

    // add
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0", "member");

    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_github_membership where org = ? and team_slug = ? and user_login = ?")
        .addBindVar(ORG).addBindVar("team-a").addBindVar("ghuser0").select(int.class);
    assertEquals(1, count);

    // idempotent add
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0", "member");
    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_github_membership where org = ? and team_slug = ? and user_login = ?")
        .addBindVar(ORG).addBindVar("team-a").addBindVar("ghuser0").select(int.class);
    assertEquals(1, count);

    // team members read
    List<GithubMembership> members = GithubApiCommands.retrieveTeamMemberships(CONFIG_ID, null, ORG, "team-a");
    assertEquals(1, GrouperUtil.length(members));
    assertEquals("ghuser0", members.get(0).getUserLogin());

    // derived org membership is active
    GithubUser orgMembership = GithubApiCommands.retrieveOrgMembership(CONFIG_ID, null, ORG, "ghuser0");
    assertNotNull(orgMembership);
    assertEquals("active", orgMembership.getOrgState());

    // org members list is derived from team memberships
    List<GithubUser> orgMembers = GithubApiCommands.retrieveOrgMembers(CONFIG_ID, null, ORG);
    assertEquals(1, GrouperUtil.length(orgMembers));
    assertEquals("ghuser0", orgMembers.get(0).getLogin());

    // remove (idempotent)
    GithubApiCommands.removeTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0");
    GithubApiCommands.removeTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0");
    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_github_membership where org = ? and team_slug = ? and user_login = ?")
        .addBindVar(ORG).addBindVar("team-a").addBindVar("ghuser0").select(int.class);
    assertEquals(0, count);

    // and org membership is now gone (404 -> null)
    assertNull(GithubApiCommands.retrieveOrgMembership(CONFIG_ID, null, ORG, "ghuser0"));
  }

  public void testRemoveOrgMembershipDeprovisions() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();

    insertMockTeam("1001", "team-a", "Team A");
    insertMockTeam("1002", "team-b", "Team B");
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0", "member");
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-b", "ghuser0", "member");

    // full deprovision removes from all teams in the org
    GithubApiCommands.removeOrgMembership(CONFIG_ID, null, ORG, "ghuser0");
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_github_membership where org = ? and user_login = ?")
        .addBindVar(ORG).addBindVar("ghuser0").select(int.class);
    assertEquals(0, count);

    // idempotent
    GithubApiCommands.removeOrgMembership(CONFIG_ID, null, ORG, "ghuser0");
  }

  public void testRetrieveExternalIdentities() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();

    insertMockUser("ghuser0", "500", "test.subject.0");
    insertMockUser("ghuser1", "501", "test.subject.1");

    List<GithubUser> identities = GithubApiCommands.retrieveExternalIdentities(CONFIG_ID, settingsWithEnterprise());
    assertEquals(2, GrouperUtil.length(identities));
    assertEquals("ghuser0", identities.get(0).getLogin());
    assertEquals("test.subject.0", identities.get(0).getSamlNameId());

    // no enterprise slug -> empty (SAML lookup disabled)
    assertEquals(0, GrouperUtil.length(GithubApiCommands.retrieveExternalIdentities(CONFIG_ID, new GithubSettings())));
  }

  public void testRetrieveTeamBySlugAndNotFound() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();
    insertMockTeam("1001", "team-a", "Team A");

    GithubTeam team = GithubApiCommands.retrieveTeam(CONFIG_ID, null, ORG, "team-a");
    assertNotNull(team);
    assertEquals("Team A", team.getName());
    assertEquals("organization", team.getTeamType());

    assertNull(GithubApiCommands.retrieveTeam(CONFIG_ID, null, ORG, "does-not-exist"));
  }

  public void testEnterpriseTeamParsedAsEnterpriseType() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();
    insertMockTeam("1001", "team-a", "Team A");
    // an enterprise team is returned by the org teams list with type=enterprise
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_github_team (id, org, slug, name, team_type, privacy, description) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar("9001").addBindVar(ORG).addBindVar("ent:sec").addBindVar("Enterprise Sec").addBindVar("enterprise").addBindVar(null).addBindVar(null)
        .executeSql();

    GithubTeam enterpriseTeam = null;
    for (GithubTeam team : GithubApiCommands.retrieveTeams(CONFIG_ID, null, ORG)) {
      if ("ent:sec".equals(team.getSlug())) {
        enterpriseTeam = team;
      }
    }
    assertNotNull(enterpriseTeam);
    assertTrue(enterpriseTeam.isEnterpriseTeam());
    assertEquals("enterprise", enterpriseTeam.getTeamType());
  }

  public void testIgnoreTeamSlugsFiltersRetrieveTeams() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();
    insertMockTeam("1001", "team-a", "Team A");
    insertMockTeam("1002", "team-b", "Team B");

    GithubSettings settings = new GithubSettings();
    settings.getIgnoreTeamSlugs().add("team-b");

    List<GithubTeam> teams = GithubApiCommands.retrieveTeams(CONFIG_ID, settings, ORG);
    assertEquals(1, GrouperUtil.length(teams));
    assertEquals("team-a", teams.get(0).getSlug());
  }

  public void testIgnoreLoginsFiltersOrgMembers() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();
    insertMockTeam("1001", "team-a", "Team A");
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser0", "member");
    GithubApiCommands.addTeamMembership(CONFIG_ID, null, ORG, "team-a", "ghuser1", "member");

    GithubSettings settings = new GithubSettings();
    settings.getIgnoreLogins().add("ghuser1");

    List<GithubUser> members = GithubApiCommands.retrieveOrgMembers(CONFIG_ID, settings, ORG);
    assertEquals(1, GrouperUtil.length(members));
    assertEquals("ghuser0", members.get(0).getLogin());
  }

  public void testInviteToOrgReturnsPendingRecord() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();

    GithubUser invitation = GithubApiCommands.inviteToOrg(CONFIG_ID, null, ORG, "newperson@example.edu", null);
    assertNull(invitation.getLogin());
    assertEquals("newperson@example.edu", invitation.getEmail());
    assertEquals("pending", invitation.getOrgState());
    assertNotNull(invitation.getInvitationId());
  }

  public void testCancelInvitationIsIdempotent() {
    GithubProvisionerTestUtils.setupGithubExternalSystem();
    // cancel is a blind, idempotent DELETE (204 even if the invitation does not exist)
    GithubApiCommands.cancelOrgInvitation(CONFIG_ID, null, ORG, "12345");
    GithubApiCommands.cancelOrgInvitation(CONFIG_ID, null, ORG, "12345");
  }

  // ============================
  // Sync test (gated on tomcat)
  // ============================

  public void testFullSyncMemberships() {
    membershipSync(true);
  }

  public void testIncrementalSyncMemberships() {
    membershipSync(false);
  }

  /**
   * Seed a pre-existing team plus SAML-linked accounts, provision a Grouper group
   * whose members match those accounts by SAML nameId, and assert the team
   * memberships converge in the mock. Then remove a member and re-provision.
   * @param isFull true for full sync, false for incremental
   */
  private void membershipSync(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }

    GithubProvisionerTestUtils.setupGithubExternalSystem();
    GithubProvisionerTestUtils.configureGithubProvisioner(
        new GithubProvisionerTestConfigInput().assignConfigId(defaultConfigId()));

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    // ensure mock tables and start clean
    GithubApiCommands.retrieveTeams(CONFIG_ID, null, ORG);
    truncateMockTables();

    // the team pre-exists (v1 does not create teams); its slug matches the group extension
    insertMockTeam("2001", "testGroup", "testGroup");
    // SAML-linked accounts whose nameId equals each subject's id
    insertMockUser("ghuser0", "600", SubjectTestHelper.SUBJ0.getId());
    insertMockUser("ghuser1", "601", SubjectTestHelper.SUBJ1.getId());

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
    attachProvisioningAttribute(stem);

    // establish baseline
    fullProvision();

    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_github_membership where team_slug = 'testGroup'").select(int.class));

    // remove a member and re-provision
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }

    assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_github_membership where team_slug = 'testGroup'").select(int.class));
    assertEquals("ghuser0", new GcDbAccess().connectionName("grouper")
        .sql("select user_login from mock_github_membership where team_slug = 'testGroup'").select(String.class));
  }

  /**
   * Full sync reconciles: a tracked Grouper member is added to the team, and an
   * untracked target membership (a login with no matching Grouper subject) is
   * removed. Exercises the insert + delete/deprovision diff in one pass.
   */
  public void testFullSyncReconcilesAddAndRemoveUntracked() {
    if (!tomcatRunTests()) {
      return;
    }

    GithubProvisionerTestUtils.setupGithubExternalSystem();
    GithubProvisionerTestUtils.configureGithubProvisioner(
        new GithubProvisionerTestConfigInput().assignConfigId(defaultConfigId()));
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();
    GithubApiCommands.retrieveTeams(CONFIG_ID, null, ORG);
    truncateMockTables();

    insertMockTeam("2001", "testGroup", "testGroup");
    insertMockUser("ghuser0", "600", SubjectTestHelper.SUBJ0.getId());
    // an orphan account in the SAML map whose nameId matches no Grouper subject
    insertMockUser("ghorphan", "699", "orphan.no.subject");
    // a stale team membership for the orphan, which Grouper does not back
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_github_membership (id, org, team_slug, team_id, user_login, role, state) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(ORG).addBindVar("testGroup").addBindVar("2001").addBindVar("ghorphan").addBindVar("member").addBindVar("active")
        .executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    attachProvisioningAttribute(stem);
    fullProvision();

    // ghuser0 added (tracked), ghorphan removed (untracked / not in Grouper)
    assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_github_membership where team_slug = 'testGroup'").select(int.class));
    assertEquals("ghuser0", new GcDbAccess().connectionName("grouper")
        .sql("select user_login from mock_github_membership where team_slug = 'testGroup'").select(String.class));
  }

  /**
   * Full sync is a no-op when Grouper and the target already agree: a member that
   * is already in the team stays, with no duplicate insert and no removal.
   */
  public void testFullSyncNoOpWhenAlreadyInSync() {
    if (!tomcatRunTests()) {
      return;
    }

    GithubProvisionerTestUtils.setupGithubExternalSystem();
    GithubProvisionerTestUtils.configureGithubProvisioner(
        new GithubProvisionerTestConfigInput().assignConfigId(defaultConfigId()));
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();
    GithubApiCommands.retrieveTeams(CONFIG_ID, null, ORG);
    truncateMockTables();

    insertMockTeam("2001", "testGroup", "testGroup");
    insertMockUser("ghuser0", "600", SubjectTestHelper.SUBJ0.getId());
    // ghuser0 is already a member of the team (already in sync)
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_github_membership (id, org, team_slug, team_id, user_login, role, state) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(ORG).addBindVar("testGroup").addBindVar("2001").addBindVar("ghuser0").addBindVar("member").addBindVar("active")
        .executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    attachProvisioningAttribute(stem);
    fullProvision();

    // exactly one membership remains (no duplicate, no removal)
    assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_github_membership where team_slug = 'testGroup' and user_login = 'ghuser0'").select(int.class));
  }

  private void attachProvisioningAttribute(Stem stem) {
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(defaultConfigId());
    attributeValue.setTargetName(defaultConfigId());
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

}
