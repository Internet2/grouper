package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.HashMap;
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
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouper.app.truefoundry.TrueFoundrySettings;
import junit.textui.TestRunner;

public class TrueFoundryProvisionerTest extends GrouperProvisioningBaseTest {

  private static final String CONFIG_ID = "trueFoundryDev";

  private static TrueFoundrySettings testSettings() {
    return new TrueFoundrySettings();
  }

  public static void main(String[] args) {

    TestRunner.run(new TrueFoundryProvisionerTest("testRetrieveUsers"));

    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return "trueFoundryProvisioner";
  }

  public static boolean startTomcat = false;

  public TrueFoundryProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    TrueFoundryMockServiceHandler.ensureTrueFoundryMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_group").executeSql();
  }

  // =============================================
  // API-level tests: Users
  // =============================================

  public void testRetrieveUsers() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String userId3 = GrouperUuid.getUuid();

    // insert active users
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("jane.smith@example.com").addBindVar("Jane Smith").addBindVar("T").executeSql();
    // deactivated user should still be returned (showInvalidUsers=true)
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId3).addBindVar("inactive@example.com").addBindVar("Inactive User").addBindVar("F").executeSql();

    List<TrueFoundryUser> allUsers = TrueFoundryApiCommands.retrieveUsers(CONFIG_ID, true, testSettings());

    // all 3 should be returned when including inactive
    assertEquals(3, allUsers.size());

    // only active users when not including inactive
    List<TrueFoundryUser> activeUsers = TrueFoundryApiCommands.retrieveUsers(CONFIG_ID, false, testSettings());
    assertEquals(2, activeUsers.size());

    Map<String, TrueFoundryUser> userById = new HashMap<String, TrueFoundryUser>();
    for (TrueFoundryUser user : allUsers) {
      userById.put(user.getId(), user);
    }

    TrueFoundryUser user1 = userById.get(userId1);
    assertNotNull(user1);
    assertEquals("john.doe@example.com", user1.getEmail());

    TrueFoundryUser user2 = userById.get(userId2);
    assertNotNull(user2);
    assertEquals("jane.smith@example.com", user2.getEmail());

    TrueFoundryUser user3 = userById.get(userId3);
    assertNotNull(user3);
    assertEquals("inactive@example.com", user3.getEmail());
    assertEquals(Boolean.FALSE, user3.getActive());
  }

  public void testRetrieveUserByEmail() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId1 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("T").executeSql();

    TrueFoundryUser user = TrueFoundryApiCommands.retrieveUserByEmail(CONFIG_ID, testSettings(), "john.doe@example.com", false);

    assertNotNull(user);
    assertEquals(userId1, user.getId());
    assertEquals("john.doe@example.com", user.getEmail());

    // non-existent email should return null
    TrueFoundryUser notFound = TrueFoundryApiCommands.retrieveUserByEmail(CONFIG_ID, testSettings(), "nonexistent@example.com", false);
    assertNull(notFound);
  }

  public void testCreateUser() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    TrueFoundryUser newUser = new TrueFoundryUser();
    newUser.setEmail("new.user@example.com");

    TrueFoundryUser created = TrueFoundryApiCommands.createUser(CONFIG_ID, testSettings(), newUser);

    assertNotNull(created);
    assertNotNull(created.getId());
    assertEquals("new.user@example.com", created.getEmail());

    // verify user was created in mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_user where email = ?")
        .addBindVar("new.user@example.com").select(int.class);
    assertEquals(1, count);
  }

  public void testCreateUserReactivatesInactive() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    // pre-create an inactive user
    String existingId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(existingId).addBindVar("existing@example.com").addBindVar("Existing User").addBindVar("F").executeSql();

    // createUser should find the inactive user and reactivate
    TrueFoundryUser newUser = new TrueFoundryUser();
    newUser.setEmail("existing@example.com");

    TrueFoundryUser result = TrueFoundryApiCommands.createUser(CONFIG_ID, testSettings(), newUser);

    assertNotNull(result);
    assertEquals(existingId, result.getId());
    assertEquals(Boolean.TRUE, result.getActive());

    // should still be only 1 user (not a duplicate)
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_user where email = ?")
        .addBindVar("existing@example.com").select(int.class);
    assertEquals(1, count);

    // verify activated in DB
    String active = new GcDbAccess().connectionName("grouper")
        .sql("select active from mock_truefoundry_user where id = ?")
        .addBindVar(existingId).select(String.class);
    assertEquals("T", active);
  }

  public void testCreateUserWithDisplayName() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    TrueFoundryUser newUser = new TrueFoundryUser();
    newUser.setEmail("display.test@example.com");
    newUser.setDisplayName("Display Test User");

    // SCIM tenant/sso must be set so createUser invokes updateUserDisplayName via SCIM
    TrueFoundrySettings settings = testSettings();
    settings.setTenantName("mock-tenant");
    settings.setSsoId("mock-sso");
    TrueFoundryUser created = TrueFoundryApiCommands.createUser(CONFIG_ID, settings, newUser);

    assertNotNull(created);
    assertEquals("display.test@example.com", created.getEmail());
    assertEquals("Display Test User", created.getDisplayName());

    // verify in mock DB
    String displayName = new GcDbAccess().connectionName("grouper")
        .sql("select display_name from mock_truefoundry_user where email = ?")
        .addBindVar("display.test@example.com").select(String.class);
    assertEquals("Display Test User", displayName);
  }

  public void testDeactivateUser() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId1 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("T").executeSql();

    TrueFoundryApiCommands.deactivateUser(CONFIG_ID, testSettings(), "john.doe@example.com");

    String active = new GcDbAccess().connectionName("grouper")
        .sql("select active from mock_truefoundry_user where id = ?")
        .addBindVar(userId1).select(String.class);
    assertEquals("F", active);
  }

  public void testActivateUser() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId1 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("F").executeSql();

    TrueFoundryApiCommands.activateUser(CONFIG_ID, testSettings(), "john.doe@example.com");

    String active = new GcDbAccess().connectionName("grouper")
        .sql("select active from mock_truefoundry_user where id = ?")
        .addBindVar(userId1).select(String.class);
    assertEquals("T", active);
  }

  // =============================================
  // API-level tests: Roles
  // =============================================

  public void testRetrieveRoles() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String roleId1 = GrouperUuid.getUuid();
    String roleId2 = GrouperUuid.getUuid();
    String roleId3 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(roleId1).addBindVar("member").addBindVar("Member").addBindVar("Default member role").addBindVar("role").addBindVar("account").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(roleId2).addBindVar("custom-role").addBindVar("Custom Role").addBindVar("A custom role").addBindVar("role").addBindVar("tenant").addBindVar("F").executeSql();
    // workspace-scoped role should be filtered out
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(roleId3).addBindVar("ws-role").addBindVar("Workspace Role").addBindVar("A workspace role").addBindVar("role").addBindVar("workspace").addBindVar("F").executeSql();

    List<TrueFoundryGroup> roles = TrueFoundryApiCommands.retrieveRoles(CONFIG_ID, testSettings());

    // only account and tenant scoped roles returned
    assertEquals(2, roles.size());

    Map<String, TrueFoundryGroup> roleById = new HashMap<String, TrueFoundryGroup>();
    for (TrueFoundryGroup role : roles) {
      roleById.put(role.getId(), role);
    }

    TrueFoundryGroup role1 = roleById.get(roleId1);
    assertNotNull(role1);
    assertEquals("member", role1.getName());
    assertEquals("account", role1.getResourceType());
    assertEquals(Boolean.TRUE, role1.getIsDefault());

    TrueFoundryGroup role2 = roleById.get(roleId2);
    assertNotNull(role2);
    assertEquals("custom-role", role2.getName());
    assertEquals("tenant", role2.getResourceType());

    // workspace role should not be in results
    assertNull(roleById.get(roleId3));
  }

  public void testCreateRole() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    TrueFoundryGroup role = new TrueFoundryGroup();
    role.setName("new-role");
    role.setDisplayName("New Role");
    role.setDescription("A new custom role");
    role.setGroupType(TrueFoundryGroup.GROUP_TYPE_ROLE);
    role.setResourceType("account");

    TrueFoundryGroup created = TrueFoundryApiCommands.createOrUpdateRole(CONFIG_ID, testSettings(), role);

    assertNotNull(created);
    assertNotNull(created.getId());
    assertEquals("new-role", created.getName());

    // verify in mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_group where name = ? and group_type = 'role'")
        .addBindVar("new-role").select(int.class);
    assertEquals(1, count);
  }

  public void testDeleteRole() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String roleId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(roleId).addBindVar("delete-me").addBindVar("Delete Me").addBindVar("To be deleted").addBindVar("role").addBindVar("account").addBindVar("F").executeSql();

    TrueFoundryApiCommands.deleteRole(CONFIG_ID, testSettings(), roleId);

    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_group where id = ?")
        .addBindVar(roleId).select(int.class);
    assertEquals(0, count);
  }

  // =============================================
  // API-level tests: Teams
  // =============================================

  public void testRetrieveTeams() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String teamId1 = GrouperUuid.getUuid();
    String teamId2 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId1).addBindVar("team-alpha").addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId2).addBindVar("team-beta").addBindVar("team").executeSql();

    // add members and a manager to team-alpha
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId1).addBindVar("member1@example.com").addBindVar("member").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId1).addBindVar("manager1@example.com").addBindVar("manager").executeSql();

    List<TrueFoundryGroup> teams = TrueFoundryApiCommands.retrieveTeams(CONFIG_ID, testSettings());

    assertEquals(2, teams.size());

    Map<String, TrueFoundryGroup> teamById = new HashMap<String, TrueFoundryGroup>();
    for (TrueFoundryGroup team : teams) {
      teamById.put(team.getId(), team);
    }

    assertNotNull(teamById.get(teamId1));
    assertEquals("team-alpha", teamById.get(teamId1).getName());
    // members list = regular member + manager
    assertEquals(2, GrouperUtil.nonNull(teamById.get(teamId1).getMembers()).size());
    // managers list = manager only
    assertEquals(1, GrouperUtil.nonNull(teamById.get(teamId1).getManagers()).size());
    assertEquals("manager1@example.com", teamById.get(teamId1).getManagers().get(0));

    assertNotNull(teamById.get(teamId2));
    assertEquals("team-beta", teamById.get(teamId2).getName());
    // team-beta has no members
    assertTrue(GrouperUtil.nonNull(teamById.get(teamId2).getMembers()).isEmpty());
  }

  public void testRetrieveSubjectsData() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String teamId = GrouperUuid.getUuid();

    // insert 2 active users
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("jane.smith@example.com").addBindVar("Jane Smith").addBindVar("T").executeSql();

    // insert a team with one regular member and one manager
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId).addBindVar("team-alpha").addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar("john.doe@example.com").addBindVar("member").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar("jane.smith@example.com").addBindVar("manager").executeSql();

    TrueFoundryApiCommands.SubjectsData data = TrueFoundryApiCommands.retrieveSubjectsData(CONFIG_ID, testSettings());

    // verify users
    assertEquals(2, data.users.size());
    Map<String, TrueFoundryUser> userById = new HashMap<String, TrueFoundryUser>();
    for (TrueFoundryUser user : data.users) {
      userById.put(user.getId(), user);
    }
    assertNotNull(userById.get(userId1));
    assertEquals("john.doe@example.com", userById.get(userId1).getEmail());
    assertNotNull(userById.get(userId2));
    assertEquals("jane.smith@example.com", userById.get(userId2).getEmail());

    // verify teams
    assertEquals(1, data.teams.size());
    TrueFoundryGroup team = data.teams.get(0);
    assertEquals(teamId, team.getId());
    assertEquals("team-alpha", team.getName());

    // members list = regular member + manager (both in manifest.members)
    assertEquals(2, GrouperUtil.nonNull(team.getMembers()).size());
    assertTrue(team.getMembers().contains("john.doe@example.com"));
    assertTrue(team.getMembers().contains("jane.smith@example.com"));

    // managers list = manager only
    assertEquals(1, GrouperUtil.nonNull(team.getManagers()).size());
    assertEquals("jane.smith@example.com", team.getManagers().get(0));

    // verify role memberships from rolesWithResource
    String roleId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type, resource_type, is_default) values (?, ?, ?, ?, ?)")
        .addBindVar(roleId).addBindVar("member").addBindVar("role").addBindVar("account").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(roleId).addBindVar("john.doe@example.com").addBindVar(null).executeSql();

    TrueFoundryApiCommands.SubjectsData dataWithRoles = TrueFoundryApiCommands.retrieveSubjectsData(CONFIG_ID, testSettings());
    assertEquals(1, dataWithRoles.roleMembershipsByRoleId.size());
    assertTrue(dataWithRoles.roleMembershipsByRoleId.containsKey(roleId));
    assertTrue(dataWithRoles.roleMembershipsByRoleId.get(roleId).contains("john.doe@example.com"));
    assertEquals(1, dataWithRoles.roleMembershipsByRoleId.get(roleId).size());

    // inactive users should be filtered out
    String inactiveId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(inactiveId).addBindVar("inactive@example.com").addBindVar("Inactive").addBindVar("F").executeSql();
    TrueFoundryApiCommands.SubjectsData dataWithInactive = TrueFoundryApiCommands.retrieveSubjectsData(CONFIG_ID, testSettings());
    assertEquals(2, dataWithInactive.users.size()); // inactive still filtered

    // ignored emails should be filtered out
    TrueFoundrySettings filteredSettings = new TrueFoundrySettings();
    filteredSettings.setIgnoreUserEmails(GrouperUtil.toSet("john.doe@example.com"));
    TrueFoundryApiCommands.SubjectsData dataFiltered = TrueFoundryApiCommands.retrieveSubjectsData(
        CONFIG_ID, filteredSettings);
    assertEquals(1, dataFiltered.users.size());
    assertEquals("jane.smith@example.com", dataFiltered.users.get(0).getEmail());
    // teams are still returned regardless of ignore filter
    assertEquals(1, dataFiltered.teams.size());
  }

  public void testCreateTeam() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String defaultMemberEmail = "svc-grouper-test@example.com";

    TrueFoundryGroup team = new TrueFoundryGroup();
    team.setName("new-team");
    team.setGroupType(TrueFoundryGroup.GROUP_TYPE_TEAM);

    TrueFoundrySettings teamSettings = new TrueFoundrySettings();
    teamSettings.setDefaultTeamMemberEmail(defaultMemberEmail);
    TrueFoundryGroup created = TrueFoundryApiCommands.createTeam(CONFIG_ID, teamSettings, team);

    assertNotNull(created);
    assertNotNull(created.getId());
    assertEquals("new-team", created.getName());

    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_group where name = ? and group_type = 'team'")
        .addBindVar("new-team").select(int.class);
    assertEquals(1, count);

    // default team member should be in the team immediately after creation
    int memberCount = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_membership where group_id = ? and user_email = ?")
        .addBindVar(created.getId()).addBindVar(defaultMemberEmail).select(int.class);
    assertEquals("Default team member should be in team after create", 1, memberCount);
  }

  public void testDeleteTeam() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String teamId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId).addBindVar("delete-team").addBindVar("team").executeSql();

    TrueFoundryApiCommands.deleteTeam(CONFIG_ID, testSettings(), teamId);

    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_group where id = ?")
        .addBindVar(teamId).select(int.class);
    assertEquals(0, count);
  }

  public void testRemoveTeamMembersKeepsDefaultMember() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String defaultMemberEmail = "svc-grouper-test@example.com";
    String regularMemberEmail = "regular@example.com";

    // create users in the mock DB
    String svcUserId = GrouperUuid.getUuid();
    String regularUserId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(svcUserId).addBindVar(defaultMemberEmail).addBindVar("Service Account").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(regularUserId).addBindVar(regularMemberEmail).addBindVar("Regular User").addBindVar("T").executeSql();

    // create a team with the default member as the initial member
    TrueFoundryGroup team = new TrueFoundryGroup();
    team.setName("test-keep-default");
    TrueFoundrySettings teamSettings = new TrueFoundrySettings();
    teamSettings.setDefaultTeamMemberEmail(defaultMemberEmail);
    TrueFoundryGroup created = TrueFoundryApiCommands.createTeam(CONFIG_ID, teamSettings, team);
    assertNotNull(created);
    String teamId = created.getId();

    // add a regular member too
    TrueFoundryApiCommands.addTeamMembers(CONFIG_ID, teamSettings, teamId, null,
        GrouperUtil.toList(regularMemberEmail));

    // remove the regular member — default member should be kept since it's the only one left
    TrueFoundryApiCommands.removeTeamMembers(CONFIG_ID, teamSettings, teamId,
        GrouperUtil.toList(regularMemberEmail));

    TrueFoundryGroup afterRemove = TrueFoundryApiCommands.getTeamById(CONFIG_ID, testSettings(), teamId);
    assertNotNull(afterRemove);
    assertFalse("Regular member should be removed",
        afterRemove.getMembers() != null && afterRemove.getMembers().contains(regularMemberEmail));
    assertTrue("Default member should still be in team",
        afterRemove.getMembers() != null && afterRemove.getMembers().contains(defaultMemberEmail));

    // now try removing the default member itself — it should be re-added because the list would be empty
    TrueFoundryApiCommands.removeTeamMembers(CONFIG_ID, teamSettings, teamId,
        GrouperUtil.toList(defaultMemberEmail));

    TrueFoundryGroup afterRemoveDefault = TrueFoundryApiCommands.getTeamById(CONFIG_ID, testSettings(), teamId);
    assertNotNull(afterRemoveDefault);
    assertTrue("Default member should be kept even when explicitly removed (prevents empty team)",
        afterRemoveDefault.getMembers() != null && afterRemoveDefault.getMembers().contains(defaultMemberEmail));
  }

  public void testGetTeamById() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String teamId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId).addBindVar("lookup-team").addBindVar("team").executeSql();

    TrueFoundryGroup team = TrueFoundryApiCommands.getTeamById(CONFIG_ID, testSettings(), teamId);

    assertNotNull(team);
    assertEquals(teamId, team.getId());
    assertEquals("lookup-team", team.getName());

    // non-existent team should return null
    TrueFoundryGroup notFound = TrueFoundryApiCommands.getTeamById(CONFIG_ID, testSettings(), GrouperUuid.getUuid());
    assertNull(notFound);
  }

  // =============================================
  // API-level tests: Role assignment
  // =============================================

  public void testAssignUserRole() {

    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();

    String userId = GrouperUuid.getUuid();
    String roleId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("T").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(roleId).addBindVar("member").addBindVar("Member").addBindVar("Default member role").addBindVar("role").addBindVar("account").addBindVar("T").executeSql();

    TrueFoundryApiCommands.assignUserRole(CONFIG_ID, testSettings(), "user@example.com", "member");

    // verify membership was created in mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_truefoundry_membership where group_id = ? and user_email = ?")
        .addBindVar(roleId).addBindVar("user@example.com").select(int.class);
    assertEquals(1, count);
  }

  // =============================================
  // Helper methods for provisioner tests
  // =============================================

  /**
   * Helper to configure provisioner with folder-based groupType.
   * Groups in test:teams:* are teams, groups in test:roles:* are roles.
   * The groupType is derived from the parent folder name via JEXL.
   */
  private TrueFoundryProvisionerTestConfigInput provisionerConfig() {
    return new TrueFoundryProvisionerTestConfigInput()
        .assignConfigId("trueFoundryProvisioner")
        .addExtraConfig("numberOfGroupAttributes", "3")
        .addExtraConfig("targetGroupAttribute.0.name", "id")
        .addExtraConfig("targetGroupAttribute.1.name", "name")
        .addExtraConfig("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension")
        .addExtraConfig("targetGroupAttribute.2.name", "groupType")
        .addExtraConfig("targetGroupAttribute.2.translateExpressionType", "translationScript")
        .addExtraConfig("targetGroupAttribute.2.translateExpression",
            "${grouperProvisioningGroup.getName().startsWith('test:roles:') ? 'role' : 'team'}")
        // cache groupType so it's available during incremental
        .addExtraConfig("groupAttributeValueCache2has", "true")
        .addExtraConfig("groupAttributeValueCache2source", "grouper")
        .addExtraConfig("groupAttributeValueCache2type", "groupAttribute")
        .addExtraConfig("groupAttributeValueCache2groupAttribute", "groupType")
        // ignore the default role so it's not treated as an unmanaged target group
        .addExtraConfig("trueFoundryIgnoreRoles", "read-only-member");
  }

  /**
   * Helper to set up the common provisioner test infrastructure
   */
  private GrouperSession setupProvisionerTest(TrueFoundryProvisionerTestConfigInput configInput) {
    TrueFoundryProvisionerTestUtils.setupTrueFoundryExternalSystem();
    TrueFoundryProvisionerTestUtils.configureTrueFoundryProvisioner(configInput);

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_truefoundry_group").executeSql();

    return GrouperSession.startRootSession();
  }

  /**
   * Helper to pre-create the default role in the mock target DB so that
   * role membership deletes (which assign the default role) succeed.
   */
  private void createDefaultRole() {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_truefoundry_group (id, name, display_name, description, group_type, resource_type, is_default) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar("read-only-member").addBindVar("Read Only Member")
        .addBindVar("Default read-only role").addBindVar("role").addBindVar("account").addBindVar("T").executeSql();
  }

  /**
   * Helper to provision based on full or incremental mode
   */
  private void provision(boolean isFull) {
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
  }

  /**
   * Helper to pre-create test users in the mock target DB
   */
  private void createMockUsers(String userId0, String userId1, String userId2) {
    // provisioner config uses email as the entity "id", so the mock user id must also be email
    // otherwise the provisioner sees target id=UUID, grouper-computed id=email and treats them
    // as two different entities (insert one, delete the other)
    String email0 = "test.subject.0@somewhere.someSchool.edu";
    String email1 = "test.subject.1@somewhere.someSchool.edu";
    String email2 = "test.subject.2@somewhere.someSchool.edu";
    new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(email0).addBindVar(email0).addBindVar("my name is test.subject.0").addBindVar("T").executeSql();
    if (userId1 != null) {
      new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
          .addBindVar(email1).addBindVar(email1).addBindVar("my name is test.subject.1").addBindVar("T").executeSql();
    }
    if (userId2 != null) {
      new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
          .addBindVar(email2).addBindVar(email2).addBindVar("my name is test.subject.2").addBindVar("T").executeSql();
    }
  }

  /**
   * Helper to initialize incremental provisioner state (call after groups/members are created, before attaching provisioning attribute)
   */
  private void initIncrementalState(boolean isFull) {
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
  }

  /**
   * Helper to attach provisioning attribute to a stem
   */
  private void attachProvisioningAttribute(Stem stem) {
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("trueFoundryProvisioner");
    attributeValue.setTargetName("trueFoundryProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

  // =============================================
  // Team CRUD and membership tests
  // =============================================

  public void testFullSyncTeamCrudAndMemberships() {
    teamCrudAndMemberships(true);
  }

  public void testIncrementalTeamCrudAndMemberships() {
    teamCrudAndMemberships(false);
  }

  /**
   * Create team, add members, remove member, add different member, delete team
   */
  public void teamCrudAndMemberships(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      String userId2 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, userId2);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      String groupName = new GcDbAccess().connectionName("grouper").sql("select name from mock_truefoundry_group where group_type = 'team'").select(String.class);
      assertEquals("test-group", groupName);

      // remove one member and provision again
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // add a different member and provision again
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // delete the group entirely and provision again
      testGroup.delete();

      provision(isFull);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

    } finally {

    }
  }

  // =============================================
  // Role CRUD and membership tests
  // =============================================

  public void testFullSyncRoleCrudAndMemberships() {
    roleCrudAndMemberships(true);
  }

  public void testIncrementalRoleCrudAndMemberships() {
    roleCrudAndMemberships(false);
  }

  /**
   * Create role, add members, remove member, add different member, delete role
   */
  public void roleCrudAndMemberships(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      // default role must exist in the mock DB for role replacement
      createDefaultRole();

      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      // two role groups — users always have exactly one role
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:roles:role-a").save();
      Group roleGroupB = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:roles:role-b").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // initial provision always needs full sync to establish baseline
      fullProvision();

      String roleAId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-a'").select(String.class);
      String roleBId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-b'").select(String.class);
      assertNotNull(roleAId);
      assertNotNull(roleBId);

      // both users should be assigned to roleA
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));

      // move SUBJ1 from roleA to roleB
      roleGroupA.deleteMember(SubjectTestHelper.SUBJ1);
      roleGroupB.addMember(SubjectTestHelper.SUBJ1, false);

      provision(isFull);

      // SUBJ0 still on roleA
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));
      // SUBJ1 now on roleB
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleBId).select(int.class));

      // add SUBJ1 back to roleA (move from roleB)
      roleGroupB.deleteMember(SubjectTestHelper.SUBJ1);
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);

      provision(isFull);

      // both users back on roleA
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));

    } finally {

    }
  }

  // =============================================
  // Team member add, remove, re-add
  // =============================================

  public void testFullSyncTeamMemberAddRemoveReAdd() {
    teamMemberAddRemoveReAdd(true);
  }

  public void testIncrementalTeamMemberAddRemoveReAdd() {
    teamMemberAddRemoveReAdd(false);
  }

  /**
   * Add member to team, remove member, re-add member
   */
  public void teamMemberAddRemoveReAdd(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-team' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // remove member, provision again: membership removed, team remains
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-team' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // re-add member, provision again: membership re-created
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-team' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

    } finally {

    }
  }

  // =============================================
  // Role member add, remove, re-add
  // =============================================

  public void testFullSyncRoleMemberAddRemoveReAdd() {
    roleMemberAddRemoveReAdd(true);
  }

  public void testIncrementalRoleMemberAddRemoveReAdd() {
    roleMemberAddRemoveReAdd(false);
  }

  /**
   * Add member to role, remove member, re-add member
   */
  public void roleMemberAddRemoveReAdd(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      // default role must exist in the mock DB for role replacement
      createDefaultRole();

      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      // two role groups — user always has exactly one role
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:roles:role-a").save();
      Group roleGroupB = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:roles:role-b").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // initial provision always needs full sync to establish baseline
      fullProvision();

      String roleAId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-a'").select(String.class);
      String roleBId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-b'").select(String.class);
      assertNotNull(roleAId);
      assertNotNull(roleBId);

      // user should be assigned to roleA
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));

      // move user from roleA to roleB
      roleGroupA.deleteMember(SubjectTestHelper.SUBJ0);
      roleGroupB.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      // user should now be on roleB (TrueFoundry replaces the role)
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleBId).select(int.class));

      // move user back to roleA
      roleGroupB.deleteMember(SubjectTestHelper.SUBJ0);
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      // user should be back on roleA
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));

    } finally {

    }
  }

  // =============================================
  // User (entity) CRUD via provisioner
  // =============================================

  public void testFullSyncUserInsert() {
    userInsert(true);
  }

  public void testIncrementalUserInsert() {
    userInsert(false);
  }

  /**
   * Users who are members of provisionable groups should be created in TrueFoundry
   * if they don't already exist. Verify the provisioner calls registerUser.
   */
  public void userInsert(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      // no pre-created mock users - provisioner should create them
      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // no users in mock DB yet
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_user").select(int.class));

      fullProvision();

      // provisioner should have created both users
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_user").select(int.class));

      // verify user emails
      String email0 = new GcDbAccess().connectionName("grouper")
          .sql("select email from mock_truefoundry_user where email = ?")
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class);
      assertNotNull(email0);

      String email1 = new GcDbAccess().connectionName("grouper")
          .sql("select email from mock_truefoundry_user where email = ?")
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class);
      assertNotNull(email1);

      // verify team was created and memberships established
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

    } finally {

    }
  }

  public void testFullSyncUserReEnable() {
    userReEnable(true);
  }

  public void testIncrementalUserReEnable() {
    userReEnable(false);
  }

  /**
   * If a user exists in TrueFoundry but is deactivated, the provisioner should re-activate them
   * when they are added to a provisionable group.
   */
  public void userReEnable(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(provisionerConfig());

    try {
      // pre-create a deactivated user in the mock DB
      String existingUserId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
          .addBindVar(existingUserId).addBindVar("test.subject.0@somewhere.someSchool.edu").addBindVar("my name is test.subject.0").addBindVar("F").executeSql();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // user exists but is deactivated
      String activeBefore = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_truefoundry_user where id = ?")
          .addBindVar(existingUserId).select(String.class);
      assertEquals("F", activeBefore);

      fullProvision();

      // user should be re-activated
      String activeAfter = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_truefoundry_user where id = ?")
          .addBindVar(existingUserId).select(String.class);
      assertEquals("T", activeAfter);

      // should still be only 1 user (not a duplicate)
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_truefoundry_user").select(int.class));

      // membership should be established
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

    } finally {

    }
  }

  public void testFullSyncUserDeactivate() {
    userDeactivate(true);
  }

  public void testIncrementalUserDeactivate() {
    userDeactivate(false);
  }

  /**
   * When a user is removed from all provisionable groups and deleteEntities is enabled,
   * the provisioner should deactivate them in TrueFoundry (soft delete).
   */
  public void userDeactivate(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // pre-create users in the target
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      fullProvision();

      // both users active, both in team
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

      // remove SUBJ1 from all groups (simulate user leaving)
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      provision(isFull);

      // SUBJ1 should be deactivated in TrueFoundry
      String active1 = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_truefoundry_user where email = ?")
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class);
      assertEquals("F", active1);

      // SUBJ0 should still be active
      String active0 = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_truefoundry_user where email = ?")
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class);
      assertEquals("T", active0);

      // only 1 membership remaining
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership m join mock_truefoundry_group g on m.group_id = g.id where g.name = 'test-group' and m.user_email != 'svc-grouper-test@example.com'").select(int.class));

    } finally {

    }
  }

  // =============================================
  // Team manager metadata via provisioner
  // =============================================

  public void testFullSyncTeamManagerMetadata() {
    teamManagerMetadata(true);
  }

  public void testIncrementalTeamManagerMetadata() {
    teamManagerMetadata(false);
  }

  /**
   * With trueFoundryAddTeamManagerMetadata enabled, the per-membership metadata attribute
   * md_trueFoundryTeamManager (boolean, showForMembership=true) determines who is a manager.
   * This test verifies the round-trip via the API commands:
   *   addTeamMembers with managers → stored in mock DB → retrieveSubjectsData returns correct roles.
   * Full provisioner tests for membership metadata require configuring translated membership
   * attributes and are covered by integration tests.
   */
  public void teamManagerMetadata(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("trueFoundryAddTeamManagerMetadata", "true")
        // add membership attribute md_trueFoundryTeamManager translated from group membership
        .addExtraConfig("numberOfMembershipAttributes", "1")
        .addExtraConfig("targetMembershipAttribute.0.name", "md_trueFoundryTeamManager")
        .addExtraConfig("targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningEntityField")
        .addExtraConfig("targetMembershipAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      // use the API directly to add members with manager role and verify round-trip
      String teamId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_truefoundry_group (id, name, group_type) values (?, ?, ?)")
          .addBindVar(teamId).addBindVar("test-team").addBindVar("team").executeSql();

      // add SUBJ0 as manager and SUBJ1 as regular member via the API
      TrueFoundryApiCommands.addTeamMembers(CONFIG_ID, testSettings(), teamId,
          GrouperUtil.toList("test.subject.0@somewhere.someSchool.edu"),
          GrouperUtil.toList("test.subject.1@somewhere.someSchool.edu"));

      // verify in mock DB: SUBJ0 is manager, SUBJ1 is regular member
      String user0Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class);
      assertEquals("manager", user0Role);

      String user1Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class);
      assertEquals("member", user1Role);

      // verify retrieveSubjectsData returns the correct manager/member split
      TrueFoundryApiCommands.SubjectsData data = TrueFoundryApiCommands.retrieveSubjectsData(CONFIG_ID, testSettings());
      assertEquals(1, data.teams.size());
      TrueFoundryGroup team = data.teams.get(0);
      assertEquals(2, GrouperUtil.nonNull(team.getMembers()).size());
      assertEquals(1, GrouperUtil.nonNull(team.getManagers()).size());
      assertEquals("test.subject.0@somewhere.someSchool.edu", team.getManagers().get(0));

    } finally {

    }
  }

  // =============================================
  // Team managers via provisioner translator + replaceMemberships
  // =============================================

  public void testFullSyncTeamManagersFromManagerGroup() {
    teamManagersFromManagerGroup(true);
  }

  public void testIncrementalTeamManagersFromManagerGroup() {
    teamManagersFromManagerGroup(false);
  }

  /**
   * End-to-end test of the team manager flow:
   *   - team group has md_trueFoundryManagerGroupName metadata pointing at a separate Grouper group
   *   - the managers group is also a member of the team group (so managers are team members too)
   *   - TrueFoundryProvisioningTranslator populates the target group's "managers" attribute
   *     (set of native TF entity IDs) from that managers group
   *   - replaceGroupMemberships (full sync, replaceMemberships=true) consumes the attribute and
   *     PUTs the team manifest with the correct member/manager split
   *   - insertMemberships (incremental) does the same check for new memberships
   * Also verifies the manager swap: removing a user from the managers group removes them
   * from the team (since they were only in the team via the managers group), and adding a
   * user to the managers group promotes them from regular member to manager.
   */
  public void teamManagersFromManagerGroup(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("trueFoundryAddTeamManagerMetadata", "true")
        .addExtraConfig("replaceMemberships", "true")
        // expose "managers" as a multivalued group attribute so the translator's
        // grouperTargetGroup.assignAttributeValue("managers", ...) is retained through the diff
        .addExtraConfig("numberOfGroupAttributes", "4")
        .addExtraConfig("targetGroupAttribute.3.name", "managers")
        .addExtraConfig("targetGroupAttribute.3.multiValued", "true")
        .addExtraConfig("targetGroupAttribute.3.showAdvancedAttribute", "true")
        .addExtraConfig("targetGroupAttribute.3.showAttributeValueSettings", "true");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      String userId2 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, userId2);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      // team group (will be provisioned as a TF team)
      Group teamGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team-mgr").save();

      // managers group lives outside the provisioned stem so it is NOT provisioned as a team.
      // Its membership drives the team manager list via the md_trueFoundryManagerGroupName metadata.
      Group managersGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("aux:teamManagers:testTeam_managers").save();

      // SUBJ1 is a direct team member; SUBJ2 is a manager via the managers group.
      // The managers group is a member of the team group so its members are also team members.
      // NOTE: SUBJ0 is intentionally NOT a direct team member — later the test adds him to the
      // managers group, which must cause an effective membership ADD on the team group so
      // incremental provisioning sees a team change and re-translates managers.
      teamGroup.addMember(SubjectTestHelper.SUBJ1, false);
      managersGroup.addMember(SubjectTestHelper.SUBJ2, false);
      teamGroup.addMember(managersGroup.toSubject(), false);

      initIncrementalState(isFull);

      // attach provisioning to the team stem (not aux — so the managers group stays unprovisioned)
      attachProvisioningAttribute(stem);

      // set the managers-group metadata on the team group
      final GrouperProvisioningAttributeValue teamValue = new GrouperProvisioningAttributeValue();
      teamValue.setDirectAssignment(true);
      teamValue.setDoProvision("trueFoundryProvisioner");
      teamValue.setTargetName("trueFoundryProvisioner");
      Map<String, Object> metadata = new HashMap<String, Object>();
      metadata.put("md_trueFoundryManagerGroupName", "aux:teamManagers:testTeam_managers");
      teamValue.setMetadataNameValues(metadata);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(teamValue, teamGroup);

      fullProvision();

      // second full sync needed so entity attribute cache is populated for manager resolution
      fullProvision();

      // one team created
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_group where group_type = 'team'").select(int.class));

      String teamId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'test-team-mgr'").select(String.class);
      assertNotNull(teamId);

      // SUBJ0 is not in the team yet; SUBJ1 is a regular member; SUBJ2 is a manager via managersGroup
      assertNull(new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class));
      assertEquals("member", new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class));
      assertEquals("manager", new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.2@somewhere.someSchool.edu").select(String.class));

      // Step 1: remove SUBJ2 from managersGroup — since that was his only path to the team,
      // he should be removed from the team entirely on the next sync.
      managersGroup.deleteMember(SubjectTestHelper.SUBJ2);

      provision(isFull);

      assertNull(new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.2@somewhere.someSchool.edu").select(String.class));

      // Step 2: add SUBJ0 to managersGroup — he enters the team transitively as a manager.
      managersGroup.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      // SUBJ0 now a manager
      assertEquals("manager", new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class));
      // SUBJ1 still regular member
      assertEquals("member", new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(teamId).addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class));

    } finally {

    }
  }

  // =============================================
  // Role default-role fallback
  // =============================================

  public void testFullSyncRoleDefaultFallback() {
    roleDefaultFallback(true);
  }

  public void testIncrementalRoleDefaultFallback() {
    roleDefaultFallback(false);
  }

  /**
   * A user removed from all provisioned role groups should be demoted to the configured
   * default role in TrueFoundry (rather than keeping their previous role forever).
   * Also verifies the X→Y transition is not clobbered by the default-role fallback.
   */
  public void roleDefaultFallback(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("trueFoundryDefaultRole", "read-only-member");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createDefaultRole();

      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group roleA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      Group roleB = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-b").save();

      roleA.addMember(SubjectTestHelper.SUBJ0, false);
      roleA.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      fullProvision();

      String roleAId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-a'").select(String.class);
      String defaultRoleId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'read-only-member'").select(String.class);
      assertNotNull(roleAId);
      assertNotNull(defaultRoleId);

      // both users on roleA, nobody on default
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(roleAId).select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ?")
          .addBindVar(defaultRoleId).select(int.class));

      // remove SUBJ0 from all role groups — should fall back to default role
      roleA.deleteMember(SubjectTestHelper.SUBJ0);

      provision(isFull);

      // SUBJ0 now on default role
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(defaultRoleId)
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(int.class));
      // SUBJ0 is no longer on roleA
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(roleAId)
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(int.class));
      // SUBJ1 still on roleA
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(roleAId)
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(int.class));

      // Now move SUBJ1 from roleA to roleB — should land on roleB, NOT default
      roleA.deleteMember(SubjectTestHelper.SUBJ1);
      roleB.addMember(SubjectTestHelper.SUBJ1, false);

      provision(isFull);

      String roleBId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-b'").select(String.class);
      assertNotNull(roleBId);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(roleBId)
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(int.class));
      // SUBJ1 should NOT have been demoted to default
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_truefoundry_membership where group_id = ? and user_email = ?")
          .addBindVar(defaultRoleId)
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(int.class));

    } finally {

    }
  }

}
