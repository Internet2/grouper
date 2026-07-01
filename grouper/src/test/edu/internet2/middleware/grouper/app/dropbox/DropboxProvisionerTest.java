package edu.internet2.middleware.grouper.app.dropbox;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Main JUnit test for the Dropbox Business team provisioner.
 *
 * <p>Modeled closely on {@code TrueFoundryProvisionerTest} (same base class, same
 * setUp/full-sync/incremental machinery and {@link #tomcatRunTests()} gating) but
 * adapted to Dropbox's matching model, which is closer to Datadog: the native id
 * (team_member_id / group_id) is assigned by the target on create, and matching is
 * done on a Grouper-set attribute (externalId, sourced from the Grouper subjectId /
 * group idIndex).</p>
 *
 * <p>Three areas are covered:</p>
 * <ol>
 *   <li>{@link #testDropboxApiCommands()} — direct API-level CRUD against the in-JVM
 *       Dropbox mock service via {@link DropboxApiCommands}.  Like TrueFoundry's
 *       API-level tests this runs against the in-JVM mock external system (registered
 *       by {@code setupDropboxExternalSystem}) and is therefore NOT gated behind
 *       {@link #tomcatRunTests()}.</li>
 *   <li>{@link #testFullSyncGroupsEntitiesMemberships()} — a full provisioning sync
 *       creating groups, entities (members) and memberships, then re-syncing after a
 *       membership change and after deletes.</li>
 *   <li>{@link #testAdminRoleOverlay()} — the admin-role overlay: groups under the
 *       configured admin-role folder are role markers (NOT created as Dropbox groups)
 *       and instead drive each member's effective Dropbox admin role (highest tier
 *       wins) via {@link DropboxProvisioningTranslator}.</li>
 * </ol>
 *
 * <p>The two provisioner-driven tests run the real provisioning pipeline against the
 * tomcat-hosted mock servlet, so they are gated behind {@link #tomcatRunTests()}
 * exactly as the TrueFoundry provisioner tests are.</p>
 */
public class DropboxProvisionerTest extends GrouperProvisioningBaseTest {

  /**
   * External-system config id used by the API-level tests.  This is the
   * WsBearerToken config id wired up by {@code setupDropboxExternalSystem} and is
   * what {@link DropboxApiCommands} resolves the bearer token + base URL from.
   */
  private static final String CONFIG_ID = "dropboxDev";

  /** provisioner config id used by the full-sync / overlay provisioner tests */
  private static final String PROVISIONER_CONFIG_ID = "dropboxProvisioner";

  /**
   * Run a single test from the IDE/command line.
   * @param args ignored
   */
  public static void main(String[] args) {
    TestRunner.run(new DropboxProvisionerTest("testDropboxApiCommands"));
    System.exit(0);
  }

  /**
   * The provisioner config id used by {@link #fullProvision()} /
   * {@link #incrementalProvision()} in the base class.
   * @return the default provisioner config id
   */
  @Override
  public String defaultConfigId() {
    return PROVISIONER_CONFIG_ID;
  }

  /**
   * @param name the test method name
   */
  public DropboxProvisionerTest(String name) {
    super(name);
  }

  /**
   * Ensure the mock Dropbox tables exist and start each test from an empty target
   * (mirrors TrueFoundryProvisionerTest.setUp).
   */
  @Override
  protected void setUp() {
    super.setUp();

    DropboxMockServiceHandler.ensureDropboxMockTables();

    // start every test with an empty mock target so counts are deterministic
    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_group").executeSql();
  }

  // =============================================
  // 1. API-level CRUD against the mock service
  // =============================================

  /**
   * Exercise the full {@link DropboxApiCommands} surface directly against the in-JVM
   * mock service: group create/retrieve/update/delete, user create/retrieve/remove,
   * group member add/remove/list, and admin-role set + name-to-id resolution.
   *
   * <p>Not gated behind {@link #tomcatRunTests()} because, like the TrueFoundry and
   * Datadog API-level tests, it runs against the in-JVM mock external system rather
   * than a real tomcat servlet.</p>
   */
  public void testDropboxApiCommands() {

    DropboxProvisionerTestUtils.setupDropboxExternalSystem();

    // ---- group create ----
    DropboxGroup newGroup = new DropboxGroup();
    newGroup.setName("Engineering");
    newGroup.setExternalId("ext-eng");
    newGroup.setManagementType(DropboxGroup.MANAGEMENT_TYPE_COMPANY_MANAGED);

    DropboxGroup createdGroup = DropboxApiCommands.createDropboxGroup(CONFIG_ID, newGroup);
    assertNotNull(createdGroup);
    assertNotNull("Dropbox assigns the group_id on create", createdGroup.getId());
    String groupId = createdGroup.getId();
    assertEquals("Engineering", createdGroup.getName());

    // verify the row landed in the mock table
    assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_dropbox_group where id = ?").addBindVar(groupId).select(int.class));

    // ---- group retrieve (list + single) ----
    List<DropboxGroup> allGroups = DropboxApiCommands.retrieveDropboxGroups(CONFIG_ID);
    assertEquals(1, allGroups.size());
    assertEquals(groupId, allGroups.get(0).getId());

    DropboxGroup retrievedGroup = DropboxApiCommands.retrieveDropboxGroup(CONFIG_ID, groupId);
    assertNotNull(retrievedGroup);
    assertEquals("Engineering", retrievedGroup.getName());

    // ---- group update ----
    retrievedGroup.setName("Engineering-Renamed");
    DropboxApiCommands.updateDropboxGroup(CONFIG_ID, retrievedGroup);
    String updatedName = new GcDbAccess().connectionName("grouper")
        .sql("select name from mock_dropbox_group where id = ?").addBindVar(groupId).select(String.class);
    assertEquals("Engineering-Renamed", updatedName);

    // ---- user create ----
    DropboxUser newUser = new DropboxUser();
    newUser.setEmail("alice@example.com");
    newUser.setExternalId("alice");
    newUser.setGivenName("Alice");
    newUser.setSurname("Anderson");

    DropboxUser createdUser = DropboxApiCommands.createDropboxUser(CONFIG_ID, newUser);
    assertNotNull(createdUser);
    assertNotNull("Dropbox assigns the team_member_id on create", createdUser.getId());
    String teamMemberId = createdUser.getId();
    assertEquals("alice@example.com", createdUser.getEmail());

    // ---- user retrieve (list + single by email) ----
    List<DropboxUser> allUsers = DropboxApiCommands.retrieveDropboxUsers(CONFIG_ID);
    assertEquals(1, allUsers.size());
    assertEquals(teamMemberId, allUsers.get(0).getId());

    DropboxUser byEmail = DropboxApiCommands.retrieveDropboxUser(CONFIG_ID, "email", "alice@example.com");
    assertNotNull(byEmail);
    assertEquals(teamMemberId, byEmail.getId());

    // ---- group member add + list ----
    DropboxMembership membership = new DropboxMembership();
    membership.setGroupId(groupId);
    membership.setTeamMemberId(teamMemberId);
    membership.setAccessType(DropboxMembership.ACCESS_TYPE_MEMBER);
    DropboxApiCommands.addDropboxGroupMembers(CONFIG_ID, groupId, GrouperUtil.toList(membership));

    List<DropboxMembership> memberships = DropboxApiCommands.retrieveDropboxGroupMemberships(CONFIG_ID, groupId);
    assertEquals(1, memberships.size());
    assertEquals(teamMemberId, memberships.get(0).getTeamMemberId());

    // ---- admin roles: set + retrieveAdminRoleNameToId ----
    // the mock maps each admin-role NAME to a fixed synthetic role_id; assign Team_Admin
    String teamAdminRoleId = DropboxMockServiceHandler.roleIdForName("Team_Admin");
    DropboxApiCommands.setDropboxAdminRoles(CONFIG_ID, teamMemberId, GrouperUtil.toList(teamAdminRoleId));

    DropboxUser afterRole = DropboxApiCommands.retrieveDropboxUser(CONFIG_ID, "team_member_id", teamMemberId);
    assertNotNull(afterRole);
    assertEquals("Team_Admin", afterRole.getAdminRole());

    // the role catalog is harvested from member responses during retrieve; the member
    // now carries Team_Admin, so the name->id map should resolve it
    Map<String, String> roleNameToId = DropboxApiCommands.retrieveAdminRoleNameToId(CONFIG_ID);
    assertEquals(teamAdminRoleId, roleNameToId.get("Team_Admin"));

    // ---- group member remove ----
    DropboxApiCommands.removeDropboxGroupMembers(CONFIG_ID, groupId, GrouperUtil.toList(teamMemberId));
    assertEquals(0, DropboxApiCommands.retrieveDropboxGroupMemberships(CONFIG_ID, groupId).size());

    // ---- user remove (hard delete in the mock) ----
    DropboxApiCommands.removeDropboxUser(CONFIG_ID, teamMemberId, true, false);
    assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_dropbox_user where id = ?").addBindVar(teamMemberId).select(int.class));

    // ---- group delete ----
    DropboxApiCommands.deleteDropboxGroup(CONFIG_ID, groupId);
    assertEquals(0, DropboxApiCommands.retrieveDropboxGroups(CONFIG_ID).size());
  }

  /**
   * UC3 recovery: when createDropboxUser re-adds an email already associated with a member (Dropbox's
   * removed-but-recoverable / 7-day-window conflict, which members/add_v2 reports as
   * {@code user_already_on_team}), the API commands recover the account via team/members/recover and
   * re-read it -- rather than failing the add or creating a duplicate.  Mirrors the Wharton IIQ
   * AfterProvisioning recover-on-conflict behavior.
   *
   * <p>Not gated behind {@link #tomcatRunTests()} -- like {@link #testDropboxApiCommands()} it runs
   * against the in-JVM mock external system.</p>
   */
  public void testDropboxRecoverOnReAdd() {

    DropboxProvisionerTestUtils.setupDropboxExternalSystem();

    String email = "recover.me@wharton.upenn.edu";

    DropboxUser first = new DropboxUser();
    first.setEmail(email);
    first.setExternalId("99887766");
    first.setGivenName("Re");
    first.setSurname("Cover");

    DropboxUser created = DropboxApiCommands.createDropboxUser(CONFIG_ID, first);
    assertNotNull(created);
    String teamMemberId = created.getId();
    assertNotNull("Dropbox assigns the team_member_id on create", teamMemberId);

    // re-add the SAME email: the mock returns user_already_on_team, so createDropboxUser must recover
    // the existing account and return it (NOT fail, NOT create a duplicate)
    DropboxUser again = new DropboxUser();
    again.setEmail(email);
    again.setExternalId("99887766");
    again.setGivenName("Re");
    again.setSurname("Cover");

    DropboxUser recovered = DropboxApiCommands.createDropboxUser(CONFIG_ID, again);

    assertNotNull("re-add of an existing email should recover, not fail", recovered);
    assertEquals("recover returns the SAME member (no duplicate created)", teamMemberId, recovered.getId());

    // exactly one member row exists for that email (recover did not insert a second)
    int userCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_dropbox_user where email = ?").addBindVar(email).select(int.class);
    assertEquals("no duplicate member created", 1, userCount);
  }

  /**
   * Edge case: requesting a downgrade (keep_account=true) for an invited-but-never-accepted member is
   * rejected by Dropbox with 409 {@code cannot_keep_invited_user_account} (there is no personal
   * account to keep).  {@code removeDropboxUser} falls back to a plain delete (keep_account=false) so
   * the invitation is removed rather than erroring -- the same net behavior as the Wharton IIQ flow.
   *
   * <p>Not gated behind {@link #tomcatRunTests()} -- runs against the in-JVM mock external system.</p>
   */
  public void testDropboxKeepAccountFallbackForInvitedMember() {

    DropboxProvisionerTestUtils.setupDropboxExternalSystem();

    DropboxUser invited = new DropboxUser();
    invited.setEmail("invited.member@wharton.upenn.edu");
    invited.setExternalId("11223344");
    invited.setGivenName("In");
    invited.setSurname("Vited");
    DropboxUser created = DropboxApiCommands.createDropboxUser(CONFIG_ID, invited);
    assertNotNull(created);
    String teamMemberId = created.getId();

    // mark the member as invited-but-never-accepted
    new GcDbAccess().connectionName("grouper")
        .sql("update mock_dropbox_user set status = ? where id = ?")
        .addBindVar(DropboxUser.STATUS_INVITED).addBindVar(teamMemberId).executeSql();

    // request a downgrade (keep_account=true). Dropbox 409s for an invited member, so removeDropboxUser
    // must fall back to a plain delete rather than throwing.
    DropboxApiCommands.removeDropboxUser(CONFIG_ID, teamMemberId, false, true);

    // the invitation was removed (the fallback delete succeeded)
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_dropbox_user where id = ?").addBindVar(teamMemberId).select(int.class);
    assertEquals("invited member with keep_account should fall back to delete", 0, count);
  }

  /**
   * OAuth2 refresh-token auth: when the accessTokenPassword is a JSON object carrying a refreshToken
   * (appKey/appSecret/refreshToken), DropboxApiCommands transparently exchanges it at /oauth2/token
   * for a short-lived access token, caches it (reused until ~5 min before expiry), and uses it as the
   * bearer -- so an API call succeeds without a static token.  Mirrors the Wharton IIQ
   * getDropboxAccessToken refresh-token model.
   *
   * <p>Not gated behind {@link #tomcatRunTests()} -- runs against the in-JVM mock external system.</p>
   */
  public void testDropboxRefreshTokenAuth() {

    DropboxProvisionerTestUtils.setupDropboxRefreshTokenExternalSystem();
    // force a fresh exchange (the access-token cache is static across tests in the JVM)
    DropboxApiCommands.clearAccessTokenCache();

    DropboxGroup group = new DropboxGroup();
    group.setName("RefreshTokenGroup");
    group.setExternalId("rtg-1");
    group.setManagementType(DropboxGroup.MANAGEMENT_TYPE_COMPANY_MANAGED);

    DropboxGroup created = DropboxApiCommands.createDropboxGroup(CONFIG_ID, group);

    assertNotNull("refresh-token auth should mint + use an access token", created);
    assertNotNull("Dropbox assigns the group_id on create", created.getId());
  }

  // =============================================
  // Helper methods for provisioner-driven tests
  // =============================================

  /**
   * Build the default Dropbox provisioner config input.  Most settings come from
   * {@code DropboxProvisionerTestUtils.configureDropboxProvisioner}; tests layer extra
   * config on top via {@code addExtraConfig}.
   * @return a fresh config input bound to {@link #PROVISIONER_CONFIG_ID}
   */
  private DropboxProvisionerTestConfigInput provisionerConfig() {
    return new DropboxProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID);
  }

  /**
   * Stand up the common provisioner test infrastructure: external system + provisioner
   * config + daemon jobs, restart Grouper so the new config is live, clear the mock
   * target, and return a root session (mirrors TrueFoundryProvisionerTest).
   * @param configInput the provisioner config input
   * @return a started root GrouperSession
   */
  private GrouperSession setupProvisionerTest(DropboxProvisionerTestConfigInput configInput) {
    DropboxProvisionerTestUtils.setupDropboxExternalSystem();
    DropboxProvisionerTestUtils.configureDropboxProvisioner(configInput);

    // let the config cache settle, then restart so the provisioner + jobs are registered
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_dropbox_group").executeSql();

    return GrouperSession.startRootSession();
  }

  /**
   * Attach the Dropbox provisioning marker to a stem so its sub-tree of groups is
   * provisioned (mirrors TrueFoundryProvisionerTest.attachProvisioningAttribute).
   * @param stem the stem to mark for provisioning
   */
  private void attachProvisioningAttribute(Stem stem) {
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(PROVISIONER_CONFIG_ID);
    attributeValue.setTargetName(PROVISIONER_CONFIG_ID);
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

  // =============================================
  // 2. Full sync: groups, entities, memberships
  // =============================================

  /**
   * Full-sync lifecycle test:
   * <ul>
   *   <li>create a folder, a group and two members, full sync, and assert the group,
   *       both members (entities) and both memberships are created in the mock target;</li>
   *   <li>remove one member and re-sync, asserting the membership is removed;</li>
   *   <li>delete the group and re-sync, asserting the group and all of its memberships
   *       are removed.</li>
   * </ul>
   *
   * <p>Gated behind {@link #tomcatRunTests()} because it runs the real provisioning
   * pipeline against the tomcat-hosted mock servlet.</p>
   */
  public void testFullSyncGroupsEntitiesMemberships() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {

      // folder marked for provisioning; one group with two members
      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // initial full sync establishes the baseline in the target
      fullProvision();

      // the group was created (matched/written by externalId; native group_id assigned by mock)
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("engineering").select(int.class));

      String groupId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_dropbox_group where name = ?").addBindVar("engineering").select(String.class);
      assertNotNull(groupId);

      // both members were created as entities (insertEntities=true)
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_user").select(int.class));

      // both memberships were created
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(groupId).select(int.class));

      // ---- membership change: drop SUBJ1, re-sync ----
      engineering.deleteMember(SubjectTestHelper.SUBJ1);

      fullProvision();

      // group still there, only one membership remains
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("engineering").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(groupId).select(int.class));

      // ---- group delete: delete the group in Grouper, re-sync ----
      engineering.delete();

      fullProvision();

      // the group and all of its memberships are gone from the target
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("engineering").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(groupId).select(int.class));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Incremental sync of all three object types (groups + entities + memberships).  After a full
   * baseline load, each change is applied through {@link #incrementalProvision()} alone:
   * <ul>
   *   <li>a membership add that also creates a brand-new entity (insertEntities=true);</li>
   *   <li>a membership remove;</li>
   *   <li>a brand-new group created incrementally (with its first membership);</li>
   *   <li>a group delete that removes the group and all of its memberships.</li>
   * </ul>
   * Gated behind {@link #tomcatRunTests()}.
   */
  public void testIncrementalSyncGroupsEntitiesMemberships() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // full baseline load, then drain the change log so each step below is a clean incremental
      fullProvision();
      incrementalProvision();

      String engId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_dropbox_group where name = ?").addBindVar("engineering").select(String.class);
      assertNotNull(engId);
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_user").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(engId).select(int.class));

      // ---- incremental membership add that also creates a new entity (SUBJ2) ----
      engineering.addMember(SubjectTestHelper.SUBJ2, false);
      incrementalProvision();

      assertEquals("incremental created the new member", new Integer(3), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_user").select(int.class));
      assertEquals("incremental added the membership", new Integer(3), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(engId).select(int.class));

      // ---- incremental membership remove (SUBJ1 leaves; entity stays, selectAllEntities) ----
      engineering.deleteMember(SubjectTestHelper.SUBJ1, false);
      incrementalProvision();

      assertEquals("incremental removed the membership", new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(engId).select(int.class));

      // ---- incremental group create (brand-new group + its first membership) ----
      Group product = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:product").save();
      product.addMember(SubjectTestHelper.SUBJ0, false);
      incrementalProvision();

      assertEquals("incremental created the new group", new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("product").select(int.class));
      String prodId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_dropbox_group where name = ?").addBindVar("product").select(String.class);
      assertEquals("new group has its membership", new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(prodId).select(int.class));

      // ---- incremental group delete (engineering): group + its memberships removed ----
      engineering.delete();
      incrementalProvision();

      assertEquals("deleted group is gone", new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("engineering").select(int.class));
      assertEquals("deleted group's memberships are gone", new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_membership where group_id = ?").addBindVar(engId).select(int.class));
      // the other group is untouched
      assertEquals("surviving group remains", new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("product").select(int.class));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // 3. Admin-role overlay
  // =============================================

  /**
   * Admin-role overlay test.  With {@code dropboxAdminRoleFolderName} configured, groups
   * directly under that folder are role markers whose extension is one of the 8 built-in
   * Dropbox admin role names.  They are NOT created as Dropbox groups; instead each
   * member's effective Dropbox admin role is the highest tier (per
   * {@link DropboxUser#ADMIN_ROLE_HIERARCHY}) among the marker groups they belong to.
   *
   * <p>Scenario: SUBJ1 is in both {@code Team_Admin} and {@code Support_Admin} marker
   * groups (Team_Admin is the higher tier and must win); SUBJ0 is in no marker group and
   * must end up member_only (no admin role).  Both are members of a normal group so they
   * are provisioned as team members.  Asserts:</p>
   * <ul>
   *   <li>SUBJ1's mock_dropbox_user.admin_role == Team_Admin;</li>
   *   <li>the Team_Admin / Support_Admin marker groups created NO rows in mock_dropbox_group;</li>
   *   <li>SUBJ0's admin_role is not an admin role (member_only / null).</li>
   * </ul>
   *
   * <p>Gated behind {@link #tomcatRunTests()} (runs the real provisioning pipeline).</p>
   */
  public void testAdminRoleOverlay() {

    if (!tomcatRunTests()) {
      return;
    }

    // configure the admin-role folder BEFORE building the provisioner, and expose an
    // "adminRole" target entity attribute (set by the translator) plus a grouper-sourced
    // cache so the framework tracks the value across the diff.
    DropboxProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("dropboxAdminRoleFolderName", "test:adminRoles")
        // grow the entity attribute set to 4 and add adminRole (no translate expression:
        // DropboxProvisioningTranslator stamps it directly)
        .addExtraConfig("numberOfEntityAttributes", "4")
        .addExtraConfig("targetEntityAttribute.3.name", "adminRole")
        .addExtraConfig("targetEntityAttribute.3.showAdvancedAttribute", "true")
        .addExtraConfig("targetEntityAttribute.3.showAttributeValueSettings", "true")
        // cache adminRole from the grouper side so it survives translate -> diff
        .addExtraConfig("entityAttributeValueCache2has", "true")
        .addExtraConfig("entityAttributeValueCache2source", "grouper")
        .addExtraConfig("entityAttributeValueCache2type", "entityAttribute")
        .addExtraConfig("entityAttributeValueCache2entityAttribute", "adminRole");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {

      new StemSave(grouperSession).assignName("test").save();

      // real groups live under test:groups -- only this folder is marked provisionable
      Stem groupsStem = new StemSave(grouperSession).assignName("test:groups").save();

      // a normal group so both subjects are provisioned as team members
      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:groups:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      // admin-role marker groups live OUTSIDE the provisioned folder (extensions match the
      // Dropbox admin role names exactly).  They are NOT marked provisionable -- the translator
      // reads their membership directly via GroupFinder to drive each member's admin role, so
      // they never enter the group/membership pipeline and create no Dropbox groups.
      Group teamAdminMarker = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:adminRoles:Team_Admin").save();
      Group supportAdminMarker = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:adminRoles:Support_Admin").save();

      // SUBJ1 is in BOTH marker groups; Team_Admin (higher tier) must win
      teamAdminMarker.addMember(SubjectTestHelper.SUBJ1, false);
      supportAdminMarker.addMember(SubjectTestHelper.SUBJ1, false);

      // mark only the real-groups folder for provisioning (NOT test:adminRoles)
      attachProvisioningAttribute(groupsStem);

      fullProvision();

      // the marker groups must NOT have been created as Dropbox groups
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name in (?, ?)")
          .addBindVar("Team_Admin").addBindVar("Support_Admin").select(int.class));

      // the normal group WAS created
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("engineering").select(int.class));

      // SUBJ1's effective admin role is the highest tier among their marker groups
      String subj1AdminRole = new GcDbAccess().connectionName("grouper")
          .sql("select admin_role from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ1.getId()).select(String.class);
      assertEquals("Team_Admin", subj1AdminRole);

      // SUBJ0 is in no marker group -> member_only (mock stores null admin_role)
      String subj0AdminRole = new GcDbAccess().connectionName("grouper")
          .sql("select admin_role from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ0.getId()).select(String.class);
      assertNull("non-admin subject should be member_only (no admin role)", subj0AdminRole);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // 3b. Lifecycle overlay: suspend + downgrade
  // =============================================

  /**
   * Lifecycle suspend: a member in the {@code <lifecycleFolder>:Suspended} marker group is provisioned
   * suspended (not fully active).  The lifecycle marker folder is NOT provisioned as a Dropbox group --
   * only the real groups folder is marked -- and the suspend state is read directly from the marker
   * group.  Gated behind {@link #tomcatRunTests()}.
   */
  public void testLifecycleSuspend() {

    if (!tomcatRunTests()) {
      return;
    }

    DropboxProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("dropboxLifecycleFolderName", "test:lifecycle")
        // expose lifecycleState as a target entity attribute (the translator stamps it) + cache it
        .addExtraConfig("numberOfEntityAttributes", "4")
        .addExtraConfig("targetEntityAttribute.3.name", "lifecycleState")
        .addExtraConfig("targetEntityAttribute.3.showAdvancedAttribute", "true")
        .addExtraConfig("targetEntityAttribute.3.showAttributeValueSettings", "true")
        .addExtraConfig("entityAttributeValueCache2has", "true")
        .addExtraConfig("entityAttributeValueCache2source", "grouper")
        .addExtraConfig("entityAttributeValueCache2type", "entityAttribute")
        .addExtraConfig("entityAttributeValueCache2entityAttribute", "lifecycleState");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {

      new StemSave(grouperSession).assignName("test").save();
      Stem groupsStem = new StemSave(grouperSession).assignName("test:groups").save();

      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:groups:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      // lifecycle marker: SUBJ1 is suspended (the lifecycle folder is NOT provisioned)
      Group suspendedMarker = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:lifecycle:Suspended").save();
      suspendedMarker.addMember(SubjectTestHelper.SUBJ1, false);

      // mark only the real-groups folder for provisioning
      attachProvisioningAttribute(groupsStem);

      fullProvision();

      // SUBJ1 was provisioned suspended; SUBJ0 active
      String subj1Status = new GcDbAccess().connectionName("grouper")
          .sql("select status from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ1.getId()).select(String.class);
      assertEquals("suspended", subj1Status);

      String subj0Status = new GcDbAccess().connectionName("grouper")
          .sql("select status from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ0.getId()).select(String.class);
      assertEquals("active", subj0Status);

      // the lifecycle marker group did NOT create a Dropbox group
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_group where name = ?").addBindVar("Suspended").select(int.class));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Lifecycle downgrade: when a member in the {@code <lifecycleFolder>:Downgrade} marker group is
   * deprovisioned (leaves the provisioned group population), the account is converted to a free Basic
   * account (keep_account=true) instead of being deleted, with its email set to the translated
   * {@code downgradeEmail} first.  Entities are scoped to the provisioned group so leaving it triggers
   * an entity delete.  Gated behind {@link #tomcatRunTests()}.
   */
  public void testLifecycleDowngrade() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:groups:engineering").save();
    GrouperSession.stopQuietly(grouperSession);

    DropboxProvisionerTestConfigInput configInput = provisionerConfig()
        .assignGroupOfUsersToProvision(engineering)
        .addExtraConfig("dropboxLifecycleFolderName", "test:lifecycle")
        // the downgrade email is read from this subject attribute at delete time (the subject's
        // "description" attribute, e.g. "description.test.subject.0")
        .addExtraConfig("dropboxDowngradeEmailSubjectAttribute", "description")
        // entities are scoped to the engineering group, so leaving it deletes the entity
        .addExtraConfig("selectAllEntities", "false");

    grouperSession = setupProvisionerTest(configInput);

    try {

      engineering = new GroupSave(grouperSession).assignName("test:groups:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);

      // lifecycle marker: SUBJ0 should be downgraded (not deleted) when leaving
      Group downgradeMarker = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:lifecycle:Downgrade").save();
      downgradeMarker.addMember(SubjectTestHelper.SUBJ0, false);

      Stem groupsStem = new StemSave(grouperSession).assignName("test:groups").save();
      attachProvisioningAttribute(groupsStem);

      // first sync creates SUBJ0 as an active member
      fullProvision();
      assertEquals("active", new GcDbAccess().connectionName("grouper")
          .sql("select status from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ0.getId()).select(String.class));

      // SUBJ0 leaves the provisioned population -> entity delete -> downgrade (in the Downgrade group)
      engineering.deleteMember(SubjectTestHelper.SUBJ0, false);
      fullProvision();

      // the account was downgraded (kept, status=removed), NOT hard-deleted; its email was set to the
      // translated downgradeEmail (the subject's name field) before the downgrade
      String status = new GcDbAccess().connectionName("grouper")
          .sql("select status from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ0.getId()).select(String.class);
      assertEquals("downgraded account is kept (not deleted), marked removed", "removed", status);

      String email = new GcDbAccess().connectionName("grouper")
          .sql("select email from mock_dropbox_user where external_id = ?")
          .addBindVar(SubjectTestHelper.SUBJ0.getId()).select(String.class);
      assertEquals("downgrade set the account email from the subject attribute",
          "description.test.subject.0", email);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // 4. Group + membership only (users pre-exist and are left unchanged)
  // =============================================

  /**
   * Build a provisioner config that manages groups + memberships only: entities are read and
   * matched/linked (so memberships can resolve each member's target team_member_id) but never
   * inserted, updated, or deleted.  This models the common case where the Dropbox team members
   * already exist (provisioned by some other process) and Grouper only sorts them into groups.
   * @return the config input
   */
  private DropboxProvisionerTestConfigInput groupMembershipOnlyConfig() {
    return provisionerConfig()
        // read + match + link entities, but make NO entity changes. makeChangesToEntities=false is the
        // master gate: it DERIVES insertEntities/updateEntities/deleteEntities=false. The granular
        // insert*/delete* entity keys are DERIVED, not settable -- setting them (even to false) fails
        // validation "'<key>' should be refactored with an upgrade task". So we set only
        // makeChangesToEntities=false, and pass the default config's deleteEntities* keys as BLANK so
        // they are never written (TestUtils' configureProvisionerSuffix skips a key present in
        // extraConfig, and the extraConfig loop skips blank values).
        .addExtraConfig("makeChangesToEntities", "false")
        .addExtraConfig("deleteEntities", "")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "");
  }

  /**
   * Pre-create a Dropbox team member directly in the mock target so it "already exists" before
   * provisioning.  external_id is the Grouper match key (= the subject id), so the provisioner
   * links to this row instead of creating a new member.
   * @param teamMemberId the native team_member_id
   * @param email the member email
   * @param externalId the external_id match key (subject id)
   */
  private void createMockDropboxUser(String teamMemberId, String email, String externalId) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_dropbox_user (id, email, external_id, status) values (?, ?, ?, ?)")
        .addBindVar(teamMemberId).addBindVar(email).addBindVar(externalId)
        .addBindVar(DropboxUser.STATUS_ACTIVE).executeSql();
  }

  /**
   * @param table a mock table name (literal, not user input)
   * @return row count of the mock table
   */
  private int countMock(String table) {
    return new GcDbAccess().connectionName("grouper").sql("select count(1) from " + table).select(int.class);
  }

  /**
   * Full sync, groups + memberships only: the group and its memberships are provisioned while the
   * pre-existing team members are left completely untouched (no inserts / updates / deletes).
   * Gated behind {@link #tomcatRunTests()}.
   */
  public void testGroupMembershipOnlyFull() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(groupMembershipOnlyConfig());

    try {

      // the team members already exist in Dropbox (pre-created in the mock) and must stay untouched
      createMockDropboxUser("dbmid:pre0", "test.subject.0@somewhere.someSchool.edu", SubjectTestHelper.SUBJ0.getId());
      createMockDropboxUser("dbmid:pre1", "test.subject.1@somewhere.someSchool.edu", SubjectTestHelper.SUBJ1.getId());

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      fullProvision();

      // group + both memberships provisioned
      assertEquals("group created", 1, countMock("mock_dropbox_group"));
      assertEquals("both memberships provisioned", 2, countMock("mock_dropbox_membership"));

      // the pre-existing members are untouched: still exactly the 2 rows we created, same ids
      assertEquals("no members created or deleted", 2, countMock("mock_dropbox_user"));
      int pre0Count = new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_user where id = ?").addBindVar("dbmid:pre0").select(int.class);
      assertEquals("pre-existing member 0 still present exactly once", 1, pre0Count);
      int pre1Count = new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_dropbox_user where id = ?").addBindVar("dbmid:pre1").select(int.class);
      assertEquals("pre-existing member 1 still present exactly once", 1, pre1Count);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Incremental sync, groups + memberships only: after a baseline full sync, an incremental member
   * add and an incremental member remove are each provisioned as a single membership change, again
   * without ever touching the pre-existing team members.  Gated behind {@link #tomcatRunTests()}.
   */
  public void testGroupMembershipOnlyIncremental() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(groupMembershipOnlyConfig());

    try {

      createMockDropboxUser("dbmid:pre0", "test.subject.0@somewhere.someSchool.edu", SubjectTestHelper.SUBJ0.getId());
      createMockDropboxUser("dbmid:pre1", "test.subject.1@somewhere.someSchool.edu", SubjectTestHelper.SUBJ1.getId());
      createMockDropboxUser("dbmid:pre2", "test.subject.2@somewhere.someSchool.edu", SubjectTestHelper.SUBJ2.getId());

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // baseline, then drain the change log so the incremental starts from a clean slate
      fullProvision();
      incrementalProvision();
      assertEquals("baseline memberships", 2, countMock("mock_dropbox_membership"));

      // incremental ADD: SUBJ2 joins the group -> one membership added
      engineering.addMember(SubjectTestHelper.SUBJ2, false);
      incrementalProvision();
      assertEquals("incremental add -> 3 memberships", 3, countMock("mock_dropbox_membership"));

      // incremental REMOVE: SUBJ0 leaves the group -> one membership removed
      engineering.deleteMember(SubjectTestHelper.SUBJ0, false);
      incrementalProvision();
      assertEquals("incremental remove -> 2 memberships", 2, countMock("mock_dropbox_membership"));

      // across the whole incremental run the members were never created or deleted by the provisioner
      assertEquals("members unchanged", 3, countMock("mock_dropbox_user"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // 5. Sync-back: a provisioning run captures the TARGET state into the generic
  //    grouper_prov_group / grouper_prov_user / grouper_prov_mship tables via the read path.
  // =============================================

  /**
   * Count the sync-back rows captured for a provisioner config in one of the generic
   * grouper_prov_* tables.
   * @param configId the provisioner (sync) config id
   * @param tableName a grouper_prov_* table name (literal, not user input)
   * @return the captured row count for this provisioner
   */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  /**
   * Enable sync-back capture into all three generic tables (plus recalc so every object is
   * processed on each pass).
   * @return the config input with the sync-back load flags set
   */
  private DropboxProvisionerTestConfigInput syncBackConfig() {
    return provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
  }

  /**
   * Full sync-back: a full provision populates grouper_prov_group / _user / _mship from the Dropbox
   * read path.  The framework captures sync-back from READS, and a write converges on the next read
   * pass, so two full passes are run: pass 1 writes the target, pass 2 reads it back and captures.
   * Gated behind {@link #tomcatRunTests()}.
   */
  public void testSyncBackFull() {

    if (!tomcatRunTests()) {
      return;
    }

    DropboxProvisionerTestConfigInput configInput = syncBackConfig();
    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // nothing captured yet
      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

      // pass 1 writes the Dropbox target; pass 2 reads it back and captures into grouper_prov_*
      fullProvision();
      fullProvision();

      assertTrue("group captured into grouper_prov_group",
          countSyncBack(configId, "grouper_prov_group") >= 1);
      assertTrue("both members captured into grouper_prov_user",
          countSyncBack(configId, "grouper_prov_user") >= 2);
      assertTrue("both memberships captured into grouper_prov_mship",
          countSyncBack(configId, "grouper_prov_mship") >= 2);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Incremental sync-back.  After a full baseline that captured the target into grouper_prov_*, a
   * member ADD and a member REMOVE are each APPLIED incrementally (via {@link #incrementalProvision()}):
   * the new member account is created and captured into grouper_prov_user immediately (read-state
   * convergence), and the membership write-hooks record the membership delta.  The generic-table
   * membership mirror then settles on the next full read pass, so the membership counts are asserted
   * after a converging {@link #fullProvision()} -- the same convergence model the SCIM generic-table
   * test uses (which does not assert a pure-incremental membership count, since the mirror reconciles
   * memberships from reads on a full).
   * Gated behind {@link #tomcatRunTests()}.
   */
  public void testSyncBackIncremental() {

    if (!tomcatRunTests()) {
      return;
    }

    DropboxProvisionerTestConfigInput configInput = syncBackConfig();
    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group engineering = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:engineering").save();
      engineering.addMember(SubjectTestHelper.SUBJ0, false);
      engineering.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // baseline: two full passes populate + converge the sync-back tables, then drain the change log
      fullProvision();
      fullProvision();
      incrementalProvision();

      assertTrue("baseline group captured", countSyncBack(configId, "grouper_prov_group") >= 1);
      int baselineUsers = countSyncBack(configId, "grouper_prov_user");
      assertTrue("baseline users captured", baselineUsers >= 2);

      // incremental ADD: a new member joins. Applied incrementally -- the member account is created
      // and captured into grouper_prov_user immediately, and the membership write-hook records the add.
      engineering.addMember(SubjectTestHelper.SUBJ2, false);
      incrementalProvision();

      assertTrue("incremental created + captured the new member into grouper_prov_user",
          countSyncBack(configId, "grouper_prov_user") >= 3);

      // the generic-table membership mirror settles on the next full read pass -> assert after converging
      fullProvision();
      assertEquals("after the incremental add converges: 3 memberships in grouper_prov_mship",
          3, countSyncBack(configId, "grouper_prov_mship"));

      // incremental REMOVE: SUBJ1 leaves. Applied incrementally, then converged with a full read.
      engineering.deleteMember(SubjectTestHelper.SUBJ1, false);
      incrementalProvision();

      fullProvision();
      assertEquals("after the incremental remove converges: 2 memberships in grouper_prov_mship",
          2, countSyncBack(configId, "grouper_prov_mship"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
