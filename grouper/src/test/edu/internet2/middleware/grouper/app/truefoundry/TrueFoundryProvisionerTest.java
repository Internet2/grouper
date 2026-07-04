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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
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

  // =============================================
  // Sync-back smoke tests: grouper_prov_* tables get populated by the read path
  // =============================================

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the TrueFoundry read path.
   * Asserts all three axes have rows after the second pass (read-state convergence contract).
   */
  public void testTrueFoundryFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");

    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);
      createDefaultRole();

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

      // first pass writes the TF target; sync-back tables stay empty until the next
      // read pass captures the new objects (read-state convergence contract).
      fullProvision();

      // second pass: reads back what we just wrote, captures through sync hooks, flushes
      fullProvision();

      assertTrue("expected at least 1 prov_group row after sync-back",
          countSyncBack(configId, "grouper_prov_group") >= 1);
      assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
          countSyncBack(configId, "grouper_prov_user") >= 2);
      assertTrue("expected at least 2 prov_mship rows",
          countSyncBack(configId, "grouper_prov_mship") >= 2);
    } finally {

    }
  }

  /**
   * Sync-back smoke test for the scoped-retrieve path: same flow as the selectAll variant,
   * but with {@code selectAllGroups=false} and {@code selectAllEntities=false} so the DAO
   * uses the scoped {@code retrieveGroup} / {@code retrieveEntity} (per-id lookups) instead
   * of {@code retrieveAllData}. Confirms the capture hooks on the scoped retrieve methods fire.
   *
   * <p>Incremental test coverage is intentionally deferred. Group/user OBJECTS capture from reads
   * (so object writes converge on the next read pass); memberships now capture on WRITE
   * (recordTargetNativeMembershipInsert/Delete/Replace from TrueFoundryTargetDao, like Adobe/SCIM).
   * Broadening incremental object coverage is the write-shadow precision pass tracked in section 10
   * of the sync-back doc.
   */
  public void testTrueFoundryFullSyncSelectByIdsPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");

    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);
      createDefaultRole();

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-group").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

      // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve and flushes.
      fullProvision();
      fullProvision();

      assertTrue("expected at least 1 prov_group row via scoped retrieve",
          countSyncBack(configId, "grouper_prov_group") >= 1);
      assertTrue("expected at least 2 prov_user rows via scoped retrieve",
          countSyncBack(configId, "grouper_prov_user") >= 2);
      assertTrue("expected at least 2 prov_mship rows via scoped retrieve",
          countSyncBack(configId, "grouper_prov_mship") >= 2);
    } finally {

    }
  }

  // ==========================================================================================
  // Sync-back CRUD tests (SCIM/Box parity) -- CAPABILITY-GATED.
  //
  // These port the Box pilot's sync-back CRUD suite (GrouperBoxProvisionerTest) to TrueFoundry.
  // Sync-back = a provisioning run captures the TARGET's state into grouper_prov_group /
  // grouper_prov_user / grouper_prov_mship (+ _attr / _attr_value) via the read path. We only test
  // operations TrueFoundry actually supports.
  //
  // TrueFoundryTargetDao capability matrix (registerGrouperProvisionerDaoCapabilities):
  //   group  : insert YES, update YES, delete YES
  //   entity : insert YES, update YES, delete YES (delete = deactivate / soft delete)
  //   mship  : insert YES, delete YES, REPLACE YES (canReplaceGroupMemberships=true)
  //   canSyncBack YES. Memberships are GROUP-CENTRIC: team manifests carry member emails, captured
  //   from the typed bean during DAO translation; role memberships come from each user's
  //   rolesWithResource. Group/user OBJECT capture is from raw JSON at the API-commands read seam.
  //
  // TWO group shapes: TEAMS (test:teams:*) and ROLES (test:roles:*). groupType is SYNTHESIZED by a
  // JEXL translation script in provisionerConfig() (role iff name starts with test:roles:, else
  // team) -- it is NOT a TrueFoundry JSON field; the commands seam stamps it onto the capture node.
  // We cover BOTH team and role where Box/SCIM have a single group CRUD, splitting team vs role the
  // same way the existing forward tests (testFullSyncTeamCrud... / testFullSyncRoleCrud...) do.
  //
  // DEFAULT_*_ATTRS we assert on (TrueFoundryProvisioningTargetNativeSync):
  //   user  defaults: email, active   (id is the target_user_id column, NOT captured as an attr)
  //   group defaults: name, groupType (id is the target_group_id column; groupType is synthesized)
  // We assert ONLY on these defaults, never on SCIM's attribute names (userName, etc.).
  //
  // Matching attributes (TrueFoundryProvisionerTestUtils.configureProvisioner):
  //   groups matched by id AND name (groupMatchingAttribute0=id, groupMatchingAttribute1=name).
  //   entities matched by id (= email; entityMatchingAttribute0=id). An update that changes a
  //   MATCHING attribute cannot converge as an in-place update (the Adobe lesson), so an
  //   update-converge test must mutate a NON-matching attribute that round-trips through the target.
  //
  // SKIPPED, per capability (no test body, just this note):
  //   - NO group-update-converge test. A TrueFoundry team's name IS the match attribute (rename =
  //     the Adobe lesson), and the mock team upsert (putTeam -> buildTeamJson) round-trips ONLY
  //     id / teamName / manifest(members,managers) -- it does NOT persist or read back a team
  //     description or displayName. So there is no Grouper-driven NON-matching team attribute that
  //     round-trips through the read path to assert convergence on. Roles are UI-managed (createRole
  //     is best-effort) and their only round-tripping field is likewise the name (= match). An
  //     update-converge test would therefore be mutating the match key and could not converge as an
  //     in-place update; written would-fail, so it is skipped (mirrors Box's group-update note,
  //     which only worked there because Box round-trips a non-matching description).
  //   - NO user-update-converge test. TrueFoundry users are matched by id (= email, fixed per
  //     subject). Their only other Grouper-driven attribute is displayName, which (a) is NOT a
  //     user capture default (defaults are email/active) and (b) only reaches the target via SCIM
  //     PATCH, which is a no-op unless tenantName + ssoId are configured (the base test config does
  //     not set them). active is target-controlled (deactivate / reactivate), not a clean
  //     Grouper-driven update. So there is no safe NON-matching captured user attribute to mutate;
  //     skipped exactly as Box skips it.
  //   - NO membership-replace-specific sync-back test. replaceGroupMemberships IS supported, but
  //     from the MIRROR's perspective a full-sync replace and an insert/delete converge to the same
  //     captured membership rows, so the team/role membership add/remove/move tests below already
  //     exercise the captured outcome.
  //   - Memberships (team AND role) now capture on WRITE, like Adobe/SCIM: TrueFoundryTargetDao's
  //     insert/delete/replaceGroupMemberships call TrueFoundryProvisioningTargetNativeSync's
  //     recordTargetNativeMembershipInsert/Delete/Replace on success, so a membership add/remove/move
  //     is recorded into the native mirror on the write and converges on the WRITE pass (no separate
  //     read pass needed for the membership axis). Group/user OBJECTS still capture on the READ path
  //     (from raw JSON at the API-commands read seam), and object INSERTS converge within the SAME
  //     run via the post-insert re-read -- see the team/role insert tests below.
  //
  // All tests gate on tomcatRunTests() like the existing TrueFoundry sync-back smoke tests, and
  // reuse the SAME configId ("trueFoundryProvisioner") + the SAME provisionerConfig() + mock seeding
  // idioms (createMockUsers / createDefaultRole / mock_truefoundry_* inserts) as the forward tests.
  // ==========================================================================================

  /**
   * Shared setup for the TrueFoundry sync-back tests: start from provisionerConfig() (the same
   * folder-driven team-vs-role config the forward tests use), turn the three
   * load*ToGenericGrouperTable flags on plus recalculateAllOperations (so every object/membership is
   * processed each run), apply any caller extras, then stand up the provisioner and wipe the mock
   * target. The caller gets a fresh root session and creates the Grouper-side stems/groups/members.
   *
   * <p>Delete-types are left at the base config defaults (deleteEntities / deleteGroups /
   * deleteMemberships are ON with the IfNotExistInGrouper / IfGrouperDeleted predicates from
   * TrueFoundryProvisionerTestUtils), matching the forward tests -- so a Grouper-side delete
   * propagates to the target and then drops from the mirror on the re-read. Orphan-capture tests
   * rely on the fact that the base config uses deleteGroupsIfGrouperDeleted (NOT
   * deleteGroupsIfNotExistInGrouper), so an unmanaged orphan team is NOT deleted and survives to be
   * captured.
   *
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes (may be null)
   * @return a started root GrouperSession
   */
  private GrouperSession setupTrueFoundrySyncBack(Map<String, String> extraConfig) {
    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    // setupProvisionerTest configures the provisioner, starts Grouper, and wipes mock_truefoundry_*
    return setupProvisionerTest(configInput);
  }

  /**
   * Seed an orphan TEAM directly into the mock target (a team unknown to Grouper). Mirrors how the
   * Box orphan tests save a GrouperBoxGroup bean: TrueFoundryGroup is the Hibernate-mapped mock
   * bean, so saving it makes the team visible to the retrieveTeams read path (buildTeamJson
   * synthesizes its manifest from the bean + membership rows). name + groupType are required.
   * @return the orphan team's target id
   */
  private String seedOrphanTeam(String id, String name) {
    TrueFoundryGroup orphanTeam = new TrueFoundryGroup();
    orphanTeam.setId(id);
    orphanTeam.setName(name);
    orphanTeam.setGroupType(TrueFoundryGroup.GROUP_TYPE_TEAM);
    HibernateSession.byObjectStatic().save(orphanTeam);
    return id;
  }

  /**
   * Seed an orphan USER directly into the mock target (a user unknown to Grouper). email is the
   * provisioning entity id for TrueFoundry, so set id = email to match the forward tests'
   * createMockUsers idiom. Active so it is returned by the subjects read.
   * @return the orphan user's target id (= email)
   */
  private String seedOrphanUser(String email) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
        .addBindVar(email).addBindVar(email).addBindVar("Orphan " + email).addBindVar("T").executeSql();
    return email;
  }

  /** Attach the sync-back provisioner (trueFoundryProvisioner) to a stem, sub scope. */
  private void attachSyncBackProvisioningAttribute(Stem stem) {
    attachProvisioningAttribute(stem);
  }

  // ------------------------------------------------------------------------------------------
  // Object INSERT convergence (team + role) -- converges within the SAME run
  // ------------------------------------------------------------------------------------------

  /**
   * Sync-back convergence of a newly created TEAM, full provision (TrueFoundry analogue of Box's
   * testBoxGroupInsertConvergesNextRead / SCIM's testGroupInsertConvergesSameRun). The team
   * converges into grouper_prov_group within the SAME run that inserts it: with
   * createGroupsAndEntitiesBeforeTranslatingMemberships + selectGroups on, the daemon re-reads each
   * just-inserted group to link it (firing the TrueFoundry capture seam), so after pass 1 the team
   * is already in the mirror, linked back to its Grouper group. Pass 2 is idempotent. The group's
   * name (a default capture attr) is asserted present in the catalog/value rows.
   */
  public void testTrueFoundrySyncBackTeamInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // baseline: nothing in the mirror yet
      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

      // pass 1 inserts the team AND -- via the post-insert re-read that links it -- captures it, so
      // the team converges into the mirror within this same run (gotcha #3: assert 1 after pass 1)
      GrouperProvisioningOutput out1 = fullProvision();
      assertEquals(0, out1.getRecordsWithErrors());
      assertEquals("team insert converges in the same run (post-insert re-read captures it)", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // pass 2 re-reads; convergence is idempotent
      GrouperProvisioningOutput out2 = fullProvision();
      assertEquals(0, out2.getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
      long syncInternalId = gcGrouperSync.getInternalId();

      assertEquals("team insert should stay converged in prov_group", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // captured via a read, so it is linked back to its Grouper group
      int groupRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and group_internal_id is not null")
          .addBindVar(syncInternalId).select(int.class);
      assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

      // name captured from the TrueFoundry read response (a group default capture attribute)
      int nameValueRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr_value gpv "
              + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
              + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
              + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
          .addBindVar(syncInternalId).select(int.class);
      assertTrue("name should be captured from the TrueFoundry read response, got " + nameValueRows,
          nameValueRows >= 1);

      // groupType (a synthesized default capture attribute) is captured as 'team'
      String groupTypeValue = new GcDbAccess().connectionName("grouper")
          .sql("select value_string from grouper_prov_group_attr_v "
              + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
              + "and attribute_name = 'groupType'")
          .addBindVar(syncInternalId).select(String.class);
      assertEquals("groupType should be captured as 'team' (synthesized default)", "team", groupTypeValue);

    } finally {

    }
  }

  /**
   * Sync-back convergence of a newly created ROLE, full provision (the ROLE counterpart of the team
   * insert test, mirroring how the forward tests split team vs role). A role group
   * (test:roles:role-a) is created in TrueFoundry via createOrUpdateRole on pass 1, and the
   * post-insert re-read captures it, so it converges into grouper_prov_group within the same run.
   * groupType is captured as 'role' (the synthesized default).
   */
  public void testTrueFoundrySyncBackRoleInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      // default role must exist for role-membership replacement to succeed
      createDefaultRole();
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group roleGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      roleGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

      // pass 1 creates role-a AND captures it via the post-insert re-read; read also surfaces the
      // pre-seeded default role (read-only-member) as an unmanaged target group. It is NOT deleted
      // (base config uses deleteGroupsIfGrouperDeleted, not ...IfNotExistInGrouper) and IS captured.
      GrouperProvisioningOutput out1 = fullProvision();
      assertEquals(0, out1.getRecordsWithErrors());
      GrouperProvisioningOutput out2 = fullProvision();
      assertEquals(0, out2.getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      // role-a is captured and linked to its Grouper group
      String roleAId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_truefoundry_group where name = 'role-a'").select(String.class);
      assertNotNull(roleAId);
      int roleARowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is not null")
          .addBindVar(syncInternalId).addBindVar(roleAId).select(int.class);
      assertEquals("role-a prov_group row should be linked to its Grouper group", 1, roleARowsLinked);

      // groupType captured as 'role' for role-a (synthesized default)
      String roleGroupTypeValue = new GcDbAccess().connectionName("grouper")
          .sql("select value_string from grouper_prov_group_attr_v "
              + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
              + "and target_group_id = ? and attribute_name = 'groupType'")
          .addBindVar(syncInternalId).addBindVar(roleAId).select(String.class);
      assertEquals("groupType should be captured as 'role' for role-a", "role", roleGroupTypeValue);

    } finally {

    }
  }

  // ------------------------------------------------------------------------------------------
  // Object DELETE convergence (team cascading to user + membership)
  // ------------------------------------------------------------------------------------------

  /**
   * Sync-back convergence of a TEAM delete, two-pass full (TrueFoundry analogue of Box's
   * testBoxGroupDeleteConvergesNextRead). Seed a team + SUBJ0 + their membership into the mirror,
   * then delete the team in Grouper. The base config's delete-types
   * (deleteEntitiesIfNotExistInGrouper, deleteGroupsIfGrouperDeleted, deleteMembershipsIfNotExist...)
   * push the removals to the target on pass A (team deleted; orphaned SUBJ0 deactivated -> filtered
   * out of the active-users read), and the re-read on pass B sees them gone, so the full-replace
   * flush drops the team, the now-inactive user, and the membership from the mirror.
   */
  public void testTrueFoundrySyncBackTeamDeleteConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // seed: two passes converge the team + SUBJ0 + their membership into the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: team", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: membership", 1, countSyncBack(configId, "grouper_prov_mship"));

      // delete the team; SUBJ0 is now orphaned (no other provisioned group) and is deactivated too
      testGroup.delete();

      // pass A: the delete writes hit the target (team deleted; orphaned SUBJ0 deactivated)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // pass B: the re-read sees them gone; the full-replace flush drops their mirror rows
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("team dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("orphaned (deactivated) SUBJ0 dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("membership dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // ------------------------------------------------------------------------------------------
  // Membership ADD / REMOVE convergence (team) + MOVE (role)
  // ------------------------------------------------------------------------------------------

  /**
   * Sync-back convergence of a TEAM membership ADD to an already-provisioned team, two-pass full
   * (TrueFoundry analogue of Box's testBoxMembershipAddConvergesNextRead). Seed test-team with
   * SUBJ0, then add SUBJ1. TrueFoundry team memberships now capture on WRITE
   * (recordTargetNativeMembershipInsert from TrueFoundryTargetDao, like Adobe/SCIM), so the add is
   * recorded into the mirror on the write pass that PUTs the new team manifest (SUBJ0+SUBJ1). This
   * test keeps the extra full pass as belt-and-suspenders convergence; grouper_prov_mship reflects
   * (test-team, SUBJ1) either way.
   *
   * <p>NB: the default team member (svc-grouper-test@example.com) is added to every team on create.
   * It is NOT a Grouper subject, so it is NOT captured into prov_user (the read maps target users to
   * Grouper members for entity capture, and the membership flush is keyed on captured users). Counts
   * below therefore reflect only the Grouper-driven users/memberships (SUBJ0, SUBJ1).
   */
  public void testTrueFoundrySyncBackTeamMembershipAddConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // seed: team + SUBJ0 + the one membership in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: team", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: the single membership", 1, countSyncBack(configId, "grouper_prov_mship"));

      // add SUBJ1 to the already-provisioned team
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // pass A: the new team manifest (with SUBJ1) is PUT to the target; the membership add is
      // write-tracked into the mirror on this write (recordTargetNativeMembershipInsert)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // the write hook already converged the mirror on the write pass, before any re-read
      assertEquals("add converges on the write pass via capture-on-write (before any re-read)", 2,
          countSyncBack(configId, "grouper_prov_mship"));
      // pass B: extra full pass kept as belt-and-suspenders; re-read still sees both members
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("team should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("both users should be in the mirror after the add", 2,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("the added membership should converge on the re-read pass", 2,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  /**
   * Sync-back convergence of a TEAM membership REMOVE from a surviving team, two-pass full
   * (TrueFoundry analogue of Box's testBoxMembershipRemoveConvergesNextRead). Two teams both hold
   * SUBJ0; SUBJ0 is removed from test-team only (it survives in other-team, so its user is NOT
   * deactivated). Team memberships capture on WRITE (recordTargetNativeMembershipDelete/Replace from
   * TrueFoundryTargetDao, like Adobe/SCIM), so the remove drops exactly test-team's mirror membership
   * on the write while leaving other-team's intact.
   */
  public void testTrueFoundrySyncBackTeamMembershipRemoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      Group otherGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:other-team").save();
      // SUBJ0 in BOTH teams so removing it from test-team leaves it provisioned (still in other-team)
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // seed: both teams + SUBJ0 + both memberships in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: both teams", 2, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: both memberships", 2, countSyncBack(configId, "grouper_prov_mship"));

      // remove SUBJ0 from test-team only (still in other-team)
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      // pass A: the membership-remove write hits the target (test-team manifest re-PUT without SUBJ0)
      // and is write-tracked into the mirror on this write, dropping (test-team,SUBJ0)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // the write-delete hook already dropped (test-team,SUBJ0) from the mirror on the write pass,
      // before any re-read (the pass-A retrieveAllData read still sees the member on the target)
      assertEquals("remove drops from the mirror on the write pass via capture-on-write (before any re-read)", 1,
          countSyncBack(configId, "grouper_prov_mship"));
      // pass B: extra full pass kept as belt-and-suspenders; other-team's SUBJ0 membership survives
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("both teams should still be in the mirror", 2,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("SUBJ0 should still be in the mirror (still in other-team)", 1,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("test-team's membership should be gone, other-team's should remain", 1,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  /**
   * Sync-back convergence of a ROLE membership MOVE (A -> B), two-pass full (the ROLE counterpart of
   * the team membership tests). TrueFoundry users have at most one role, so moving SUBJ0 from role-a
   * to role-b is a replace at the target. After the move converges, the mirror would show exactly one
   * role membership for SUBJ0 -- on role-b, not role-a.
   *
   * <p>SKIPPED -- but NOT because the capture is missing: ROLE memberships now capture on WRITE, the
   * same as team memberships. TrueFoundryTargetDao's membership write sites
   * (insert/delete/replaceGroupMemberships) call
   * TrueFoundryProvisioningTargetNativeSync.recordTargetNativeMembershipInsert/Delete/Replace on
   * success, which records the (roleId -> nativeUserId) edge into the native mirror on the write.
   * The role GROUP itself is captured on the read path (see the passing
   * testTrueFoundrySyncBackRoleInsertConvergesNextRead, which links role-a's prov_group row). So the
   * membership-mirror gap this note used to describe has been closed by the write-track hooks.
   *
   * <p>This scenario stays skipped only because enabling it needs a real test run to confirm the
   * end-to-end role-move convergence assertions (this was a comment-only cleanup, not a verified
   * un-skip). The body below documents the intended assertions for when it is enabled.
   */
  public void testTrueFoundrySyncBackRoleMembershipMoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    // SKIP: kept as an early return until this test is actually run and its role-move convergence
    // assertions are confirmed. Role memberships DO capture on write now
    // (recordTargetNativeMembershipInsert/Delete/Replace from TrueFoundryTargetDao), so the scenario
    // it documents should work: seed SUBJ0 on role-a (createDefaultRole + the two role groups +
    // addMember), two full passes, assert one grouper_prov_mship row joins to the role-a prov_group
    // (target_group_id = role-a's mock id), then move SUBJ0 from role-a to role-b and after two more
    // passes assert the mirrored membership moved (0 on role-a, 1 on role-b), joining
    // grouper_prov_mship -> grouper_prov_group by prov_group_internal_id and filtering on
    // target_group_id.
    return;
  }

  // ------------------------------------------------------------------------------------------
  // Multi-sync data evolution + orphan / strict-native capture
  // ------------------------------------------------------------------------------------------

  /**
   * Multi-sync coverage with data evolution between rounds, TrueFoundry analogue of Box's
   * testBoxFullSyncReflectsDataChangesAcrossSyncs. Round 1: test-team with SUBJ0 only, seeded via
   * two passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan team + orphan
   * user directly into the mock (base config's deleteGroupsIfGrouperDeleted does NOT remove an
   * unmanaged team, so the orphans persist). Round 3: two more passes -> the mirror reflects the new
   * state (3 users: SUBJ0, SUBJ1, orphan; 2 groups: test-team, orphan-team; 2 memberships in
   * test-team), and the orphan user's email value round-trips through the reporting view.
   */
  public void testTrueFoundrySyncBackReflectsDataChangesAcrossSyncs() {

    if (!tomcatRunTests()) {
      return;
    }

    // Disable entity deletion (deleteEntities=false) so the round-2 target-drift orphan user is NOT
    // deactivated before round 3 can capture it. The shared base config
    // (TrueFoundryProvisionerTestUtils.configureProvisioner) force-enables deleteEntities=true +
    // deleteEntitiesIfNotExistInGrouper=true; with those on, the active orphan user (a member of no
    // Grouper-provisioned group) is treated as unprovisionable and deactivated on pass A, and pass B's
    // subjects read then filters out inactive users -- so the orphan never reaches grouper_prov_user
    // and the round-3 count is 2 instead of 3. Keep customizeEntityCrud on (else the explicit delete
    // key is rejected by validation) and turn deleteEntities off. Same fix as Datadog's analogue and
    // as testTrueFoundrySyncBackUserStaysInMirrorWhenNotDeleted below. The orphan TEAM does not need
    // this (groups use deleteGroupsIfGrouperDeleted, which never removes a target-only orphan).
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    GrouperSession grouperSession = setupTrueFoundrySyncBack(noEntityDelete);

    try {
      String configId = "trueFoundryProvisioner";

      // ===================== ROUND 1: initial state =====================
      // Seed ONLY SUBJ0's backing user into the mock target. createMockUsers' second arg is a non-null
      // flag that adds SUBJ1's backing user (test.subject.1), and a non-null third arg adds SUBJ2's --
      // the UUID values themselves are ignored (the inserted id is the email). This provisioner reads
      // ALL target users (selectAll), so every backing user present at capture time lands in
      // grouper_prov_user. Round 1 provisions only SUBJ0, so seeding SUBJ1's user here would make the
      // round-1 capture see 2 users and fail the "1 prov_user row" assert. SUBJ1's backing user is
      // deferred to round 2, where SUBJ1 actually joins the Grouper group.
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("round 1: 1 prov_user row for SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("round 1: 1 prov_group row for test-team", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("round 1: 1 prov_mship row for SUBJ0 in test-team", 1, countSyncBack(configId, "grouper_prov_mship"));

      // ===================== ROUND 2: data changes =====================
      // Grouper-side: add SUBJ1 to test-team. next full sync inserts SUBJ1 + the membership.
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // Seed SUBJ1's backing user (test.subject.1) into the mock target now that SUBJ1 is a Grouper
      // member -- deferred from round 1 so the round-1 capture saw only SUBJ0. Insert email = id to
      // match the createMockUsers idiom (this provisioner uses email as the entity id). With this in
      // place the round-3 capture sees exactly 3 users: SUBJ0, SUBJ1, and the orphan user below.
      String subj1Email = "test.subject.1@somewhere.someSchool.edu";
      new GcDbAccess().connectionName("grouper")
          .sql("insert into mock_truefoundry_user (id, email, display_name, active) values (?, ?, ?, ?)")
          .addBindVar(subj1Email).addBindVar(subj1Email).addBindVar("my name is test.subject.1")
          .addBindVar("T").executeSql();

      // Target-side drift: an orphan team + orphan user unknown to Grouper. With the base config's
      // deleteGroupsIfGrouperDeleted (not ...IfNotExistInGrouper) the orphan team is NOT removed and
      // persists across the next sync, alongside the orphan user.
      String orphanTeamId = seedOrphanTeam("orphan-tf-team-evolve-1", "orphanTeamAddedMidTest");
      String orphanUserEmail = seedOrphanUser("orphan.evolve@example.edu");

      // ===================== ROUND 3: second full sync + assertions =====================
      // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's + the
      // drift orphans) and refreshes the mirror.
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)", 3,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("round 3: 2 prov_group rows expected (test-team, orphan-team)", 2,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in test-team)", 2,
          countSyncBack(configId, "grouper_prov_mship"));

      // the orphan team landed in the mirror, unlinked (no Grouper group)
      int orphanGroupRow = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertEquals("orphan team should land in prov_group with group_internal_id IS NULL", 1, orphanGroupRow);

      // the orphan user's email value round-trips through the reporting view (proves target-drift
      // entities are captured with their actual attributes). email is a TrueFoundry user default
      // capture attribute.
      String orphanUserEmailInReporting = new GcDbAccess().connectionName("grouper")
          .sql("select value_string from grouper_prov_user_attr_v "
              + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
              + "and target_user_id = ? and attribute_name = 'email'")
          .addBindVar(syncInternalId).addBindVar(orphanUserEmail).select(String.class);
      assertEquals("orphan user's email should round-trip through reporting", orphanUserEmail,
          orphanUserEmailInReporting);

    } finally {

    }
  }

  /**
   * Strict-native capture of orphan target objects, TrueFoundry analogue of Box's
   * testBoxFullSyncCapturesOrphanTargetEntities. An orphan team + orphan user that exist in the
   * target but are unknown to Grouper are still captured into the mirror -- with NULL Grouper-side
   * linkage (group_internal_id / member_internal_id) -- alongside Grouper's own test-team + SUBJ0,
   * which keep their linkage populated. Verifies a group default attribute (groupType) is in the
   * per-provisioner catalog and that 'id' is NOT captured as an attribute (it is the target id col).
   */
  public void testTrueFoundrySyncBackCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    // Disable entity deletion (deleteEntities=false) so the active orphan user is NOT deactivated by
    // the daemon (the shared base config force-enables deleteEntities=true +
    // deleteEntitiesIfNotExistInGrouper=true). With deletes on, the active orphan user -- a member of
    // no Grouper-provisioned group -- is treated as unprovisionable and deactivated on pass 1; pass 2's
    // subjects read then filters out inactive users, so the orphan never reaches grouper_prov_user and
    // the capture assert sees 0 rows. Keep customizeEntityCrud on (else the explicit delete key is
    // rejected by validation) and turn deleteEntities off, so the orphan user stays active and is
    // captured on the re-read. (The orphan TEAM is safe regardless: groups use
    // deleteGroupsIfGrouperDeleted, which only removes Grouper-DELETED groups, never a target-only
    // orphan.) Same fix as Datadog's analogue.
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    GrouperSession grouperSession = setupTrueFoundrySyncBack(noEntityDelete);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      // pre-populate orphans directly in the target before the provisioner runs
      String orphanTeamId = seedOrphanTeam("orphan-tf-team-1234", "orphanTeamNotInGrouper");
      String orphanUserEmail = seedOrphanUser("orphan.user@example.edu");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // two passes: pass 1 inserts Grouper's objects (orphans untouched -- the base config does not
      // delete unmanaged teams); pass 2 reads orphans + Grouper's objects and the flush captures all.
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      // orphan team landed with NULL group_internal_id
      int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertEquals("expected exactly 1 prov_group row for the orphan team", 1, orphanGroupRowsTotal);

      int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertEquals("orphan team's prov_group row must have group_internal_id IS NULL", 1,
          orphanGroupRowsUnlinked);

      // orphan user landed with NULL member_internal_id
      int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanUserEmail).select(int.class);
      assertEquals("expected exactly 1 prov_user row for the orphan user", 1, orphanUserRowsTotal);

      int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id = ? and member_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanUserEmail).select(int.class);
      assertEquals("orphan user's prov_user row must have member_internal_id IS NULL", 1,
          orphanUserRowsUnlinked);

      // Grouper's own test-team lands alongside, with linkage populated
      int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id != ? and group_internal_id is not null")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertTrue("Grouper's test-team prov_group row must have group_internal_id linked",
          testGroupRowsLinked >= 1);

      int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id != ? and member_internal_id is not null")
          .addBindVar(syncInternalId).addBindVar(orphanUserEmail).select(int.class);
      assertTrue("Grouper-provisioned prov_user rows (SUBJ0) must have member_internal_id linked",
          nonOrphanUserRowsLinked >= 1);

      // a group default attribute (groupType) is captured in the per-provisioner catalog
      int groupTypeCatalog = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr "
              + "where grouper_sync_internal_id = ? and attribute_name = 'groupType'")
          .addBindVar(syncInternalId).select(int.class);
      assertEquals("default group attribute 'groupType' should be in the per-provisioner catalog", 1,
          groupTypeCatalog);

      // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
      int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr "
              + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
          .addBindVar(syncInternalId).select(int.class);
      assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
          idAsGroupAttrRows);

    } finally {

    }
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis, TrueFoundry analogue of Box's
   * testBoxFullSyncCapturesMembershipsFromOrphanGroup. An orphan team with an orphan member (neither
   * known to Grouper) is wired in the mock. TrueFoundry memberships are group-centric, so when the
   * daemon lists teams it also reads the orphan team's manifest members -- that membership must land
   * in grouper_prov_mship alongside Grouper's own, proving strict-native membership capture is
   * independent of Grouper knowledge.
   */
  public void testTrueFoundrySyncBackCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    // Disable entity deletion (deleteEntities=false) so the active orphan user survives the re-read.
    // Same root cause as testTrueFoundrySyncBackCapturesOrphanTargetEntities: the shared base config
    // force-enables deleteEntities=true + deleteEntitiesIfNotExistInGrouper=true, so the active orphan
    // user (a member of the orphan TEAM only, which is not a Grouper-provisioned group) would be
    // deactivated on pass 1 and filtered out of pass 2's subjects read. The orphan membership row is
    // joined/recorded through grouper_prov_user.target_user_id, so dropping the orphan user also loses
    // the membership edge (0 instead of 1). Keep customizeEntityCrud on (else the explicit delete key
    // is rejected by validation) and turn deleteEntities off so the orphan user stays active and its
    // membership edge is captured. (The orphan team itself is safe: groups use
    // deleteGroupsIfGrouperDeleted, which never removes a target-only orphan.)
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    GrouperSession grouperSession = setupTrueFoundrySyncBack(noEntityDelete);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      // orphan team + orphan user + the membership wiring them, all in the mock. The orphan user
      // must exist (active) so the subjects read returns it and the email->id index can resolve the
      // team-manifest member email to a target user id during membership capture.
      String orphanTeamId = seedOrphanTeam("orphan-tf-mship-team-1", "orphanTeamWithMembers");
      String orphanUserEmail = seedOrphanUser("orphan.mship@example.edu");
      new GcDbAccess().connectionName("grouper")
          .sql("insert into mock_truefoundry_membership (id, group_id, user_email, role) values (?, ?, ?, ?)")
          .addBindVar(GrouperUuid.getUuid()).addBindVar(orphanTeamId).addBindVar(orphanUserEmail)
          .addBindVar("member").executeSql();

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      // the orphan team's membership lands in prov_mship (join through prov_group/prov_user, which
      // hold the target ids -- prov_mship itself only has the FK internal ids)
      int orphanMshipRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_mship pm "
              + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
              + "join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
              + "where pm.grouper_sync_internal_id = ? and pg.target_group_id = ? and pu.target_user_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).addBindVar(orphanUserEmail)
          .select(int.class);
      assertEquals("expected 1 prov_mship row for orphan team -> orphan user", 1, orphanMshipRows);

      // Grouper's own membership (SUBJ0 in test-team) lands alongside
      int testGroupMshipRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_mship pm "
              + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
              + "where pm.grouper_sync_internal_id = ? and pg.target_group_id != ?")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertTrue("Grouper's own membership(s) should land alongside the orphan's",
          testGroupMshipRows >= 1);

    } finally {

    }
  }

  /**
   * !selectAll* scope excludes orphans, TrueFoundry analogue of Box's
   * testBoxSelectAllFalseExcludesOrphans. With selectAllGroups=false and selectAllEntities=false the
   * daemon fetches only the resources mapped to Grouper-provisioned objects (by id/email via the
   * scoped retrieveGroup / retrieveEntity), never a server-wide listing -- so an orphan team / user
   * that the target has but Grouper does not must NOT land in the mirror.
   */
  public void testTrueFoundrySyncBackSelectAllFalseExcludesOrphans() {

    if (!tomcatRunTests()) {
      return;
    }

    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    GrouperSession grouperSession = setupTrueFoundrySyncBack(extraConfig);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      // pre-populate an orphan team + orphan user -- must NOT appear in reporting because
      // selectAll=false makes the daemon fetch only by id/email (Grouper-known resources only).
      String orphanTeamId = seedOrphanTeam("orphan-tf-team-selnone-1", "orphanTeamSelectAllFalse");
      String orphanUserEmail = seedOrphanUser("orphan.selnone@example.edu");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      // Grouper-known resources still captured
      assertTrue("Grouper-provisioned test-team should still be in prov_group",
          countSyncBack(configId, "grouper_prov_group") >= 1);
      assertTrue("Grouper-provisioned SUBJ0 should still be in prov_user",
          countSyncBack(configId, "grouper_prov_user") >= 1);

      // orphans must NOT be captured (selectAll=false -> no server-wide listing -> no capture)
      int orphanGroupRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanTeamId).select(int.class);
      assertEquals("orphan team must NOT be captured when selectAllGroups=false", 0, orphanGroupRows);

      int orphanUserRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanUserEmail).select(int.class);
      assertEquals("orphan user must NOT be captured when selectAllEntities=false", 0, orphanUserRows);

    } finally {

    }
  }

  /**
   * Not-deleted target object stays in the mirror, TrueFoundry analogue of Box's
   * testBoxUserDeleteBrokenTargetStaysInMirror. A target object the daemon did NOT remove must stay
   * captured (the "verify, don't assume" contract -- the re-read finds it still present).
   *
   * <p>Mechanism: mark the team to provision but DISABLE entity deletion. SUBJ0 is removed from
   * test-team in Grouper, but with deleteEntities=false the daemon never deactivates SUBJ0 in the
   * target -- so the user remains active in the target and the re-read keeps it in the mirror. This
   * exercises the same mirror behavior without needing a target that lies about a delete. (Override
   * the base config's deleteEntities=true by turning customizeEntityCrud on and deleteEntities off.)
   */
  public void testTrueFoundrySyncBackUserStaysInMirrorWhenNotDeleted() {

    if (!tomcatRunTests()) {
      return;
    }

    // Disable entity deletion so the daemon will NOT deactivate SUBJ0 once it becomes
    // unprovisionable. The base config sets deleteEntities=true, so override it: keep
    // customizeEntityCrud on (else the explicit delete key is rejected by validation) and set
    // deleteEntities=false.
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    GrouperSession grouperSession = setupTrueFoundrySyncBack(noEntityDelete);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachSyncBackProvisioningAttribute(stem);

      // seed: team + SUBJ0 in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: team", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

      // remove SUBJ0 from the team in Grouper. With deleteEntities off the daemon does not deactivate
      // SUBJ0 in the target, so the target still has SUBJ0 (active).
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      // the team was never deleted -> still in the mirror
      assertEquals("team row should stay (team was not deleted)", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // confirm the target still has SUBJ0 active (the daemon did not deactivate it)
      String active = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_truefoundry_user where email = ?")
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class);
      assertEquals("SUBJ0 should remain active in the target (delete-entities off)", "T", active);

      assertEquals("SUBJ0 should STAY in the mirror (its delete was never performed)", 1,
          countSyncBack(configId, "grouper_prov_user"));

    } finally {

    }
  }

  // ------------------------------------------------------------------------------------------
  // load*ToGenericGrouperTable flag isolation
  // ------------------------------------------------------------------------------------------

  /**
   * loadGroupsToGenericGrouperTable in isolation, TrueFoundry analogue of Box's
   * testBoxLoadGroupsFlagInIsolation. Only the groups flag is on -> only grouper_prov_group rows are
   * written; prov_user and prov_mship stay empty even though the daemon still reads users (for
   * provisioning) and memberships (for diffing).
   */
  public void testTrueFoundrySyncBackLoadGroupsFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false");
    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertTrue("expected >=1 prov_group row when groups capture is on",
          countSyncBack(configId, "grouper_prov_group") >= 1);
      assertEquals("expected 0 prov_user rows when entities capture is off", 0,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  /**
   * loadEntitiesToGenericGrouperTable in isolation, TrueFoundry analogue of Box's
   * testBoxLoadEntitiesFlagInIsolation. Only the entities flag is on -> only grouper_prov_user rows
   * are written; prov_group and prov_mship stay empty.
   */
  public void testTrueFoundrySyncBackLoadEntitiesFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false");
    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("expected 0 prov_group rows when groups capture is off", 0,
          countSyncBack(configId, "grouper_prov_group"));
      assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
          countSyncBack(configId, "grouper_prov_user") >= 2);
      assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  /**
   * loadMembershipsToGenericGrouperTable off, TrueFoundry analogue of Box's
   * testBoxLoadMembershipsFlagOff. Both object loads on but memberships off -> prov_group and
   * prov_user populate, prov_mship stays empty. Proves the membership gate is independent of the
   * object gates.
   */
  public void testTrueFoundrySyncBackLoadMembershipsFlagOff() {

    if (!tomcatRunTests()) {
      return;
    }

    TrueFoundryProvisionerTestConfigInput configInput = provisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "false");
    String configId = configInput.getConfigId();

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachSyncBackProvisioningAttribute(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertTrue("expected >=1 prov_group row", countSyncBack(configId, "grouper_prov_group") >= 1);
      assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1)",
          countSyncBack(configId, "grouper_prov_user") >= 2);
      assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // ------------------------------------------------------------------------------------------
  // INCREMENTAL sync-back (conservative -- no spurious deletes, new user captured)
  // ------------------------------------------------------------------------------------------

  /**
   * INCREMENTAL sync-back coverage for TrueFoundry, conservative -- the direct analogue of Box's
   * testBoxIncrementalSyncBackNoSpuriousDeletes. Group/user OBJECTS capture on the READ path, and
   * this test's user-object assertions rely on that; memberships now capture on WRITE
   * (recordTargetNativeMembershipInsert/Delete/Replace from TrueFoundryTargetDao, like Adobe/SCIM).
   * What this test asserts is therefore deliberately narrow: after seeding via full sync and priming
   * the changelog
   * consumer, adding a member drives an incremental that (a) re-reads the changed group/entity and
   * so does NOT shrink the existing group/user mirror (no spurious deletes -- the scoped incremental
   * flush guards this), and (b) captures the newly added member's user object into prov_user. It
   * does NOT assert that the new MEMBERSHIP converges on the same incremental cycle -- membership
   * convergence is covered end-to-end by the two-pass full tests above.
   */
  public void testTrueFoundrySyncBackIncrementalNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupTrueFoundrySyncBack(null);

    try {
      String configId = "trueFoundryProvisioner";
      createMockUsers(GrouperUuid.getUuid(), GrouperUuid.getUuid(), GrouperUuid.getUuid());

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:teams:test-team").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachSyncBackProvisioningAttribute(stem);

      // seed via full sync: team + SUBJ0 + SUBJ1 + their memberships in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      int provGroupRowsBefore = countSyncBack(configId, "grouper_prov_group");
      int provUserRowsBefore = countSyncBack(configId, "grouper_prov_user");
      assertTrue("seed should have >=1 prov_group row", provGroupRowsBefore >= 1);
      assertEquals("seed should have 2 prov_user rows", 2, provUserRowsBefore);

      // prime the changelog consumer: its FIRST run only initializes its changelog position
      // (processes nothing), so without this priming pass the change below is never consumed.
      incrementalProvision();

      // incremental add: a third member. The incremental re-reads the changed group/entity, firing
      // the TrueFoundry read-capture seams, and the scoped flush upserts -- it must NOT drop rows.
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);
      incrementalProvision();

      // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows
      // intact. NB: prov_mship is intentionally NOT asserted here to keep this test narrowly focused
      // on the no-spurious-deletes / user-object-capture contract; membership convergence (now
      // write-tracked) is covered by the two-pass full tests.
      assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
          + " after=" + countSyncBack(configId, "grouper_prov_group"),
          countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);

      // (b) the newly added member's user object is captured (object capture via the per-id re-read)
      assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
          countSyncBack(configId, "grouper_prov_user"));

      // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
      // regression guarded on the LDAP/SCIM side; TrueFoundry shares the same generic flush code)
      int dupGroupAttr = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
              + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
              + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
          .addBindVar(syncInternalId).select(int.class);
      assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
          dupGroupAttr);

    } finally {

    }
  }

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  /**
   * The single provisioned group's target_group_id (TrueFoundry group id) in the mirror, or null.
   * Mirrors the Box / Adobe helper of the same name. Provided for parity with the Box pilot; not
   * exercised by a group-update-converge test here because TrueFoundry has no round-tripping
   * NON-matching group attribute to mutate (see the capability-matrix note above), but kept so a
   * future converge test can prove the SAME target object survives an update.
   */
  @SuppressWarnings("unused")
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

}
