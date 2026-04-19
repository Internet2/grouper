package edu.internet2.middleware.grouper.app.datadog;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class DatadogProvisionerTest extends GrouperProvisioningBaseTest {

  private static final String CONFIG_ID = "datadogDev";

  public static void main(String[] args) {

    DatadogMockServiceHandler.ensureDatadogMockTables();
    TestRunner.run(new DatadogProvisionerTest("testRetrieveUsers"));

    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return "datadogProvisioner";
  }

  public static boolean startTomcat = false;

  public DatadogProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    DatadogMockServiceHandler.ensureDatadogMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_group").executeSql();
  }

  public void testRetrieveUsers() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String userId3 = GrouperUuid.getUuid();
    String userId4 = GrouperUuid.getUuid();

    // insert some users directly into the mock table
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("jane.smith@example.com").addBindVar("Jane Smith").addBindVar("Developer").addBindVar("F").addBindVar("F").executeSql();
    // disabled user should be filtered out
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId3).addBindVar("disabled.user@example.com").addBindVar("Disabled User").addBindVar("Analyst").addBindVar("T").addBindVar("F").executeSql();
    // service account should be filtered out
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId4).addBindVar("svc-account@example.com").addBindVar("Service Bot").addBindVar(null).addBindVar("F").addBindVar("T").executeSql();

    List<DatadogUser> users = DatadogApiCommands.retrieveUsers(CONFIG_ID, null);

    // should only get 2 (disabled users and service accounts filtered out)
    assertEquals(2, users.size());

    Map<String, DatadogUser> userById = new HashMap<String, DatadogUser>();
    for (DatadogUser user : users) {
      userById.put(user.getId(), user);
    }

    DatadogUser user1 = userById.get(userId1);
    assertNotNull(user1);
    assertEquals("john.doe@example.com", user1.getEmail());
    assertEquals("John Doe", user1.getName());
    assertEquals("Engineer", user1.getTitle());
    assertEquals(Boolean.FALSE, user1.getDisabled());

    DatadogUser user2 = userById.get(userId2);
    assertNotNull(user2);
    assertEquals("jane.smith@example.com", user2.getEmail());
    assertEquals("Jane Smith", user2.getName());
    assertEquals("Developer", user2.getTitle());

    // disabled user and service account should not be in results
    assertNull(userById.get(userId3));
    assertNull(userById.get(userId4));
  }

  public void testRetrieveUsersIgnoreEmail() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("john.doe@example.com").addBindVar("John Doe").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("admin@example.com").addBindVar("Admin User").addBindVar("Admin").addBindVar("F").addBindVar("F").executeSql();

    DatadogSettings settings = new DatadogSettings();
    settings.getIgnoreUserEmails().add("admin@example.com");

    List<DatadogUser> users = DatadogApiCommands.retrieveUsers(CONFIG_ID, settings);

    // admin@example.com should be filtered out
    assertEquals(1, users.size());
    assertEquals(userId1, users.get(0).getId());
  }

  public void testCreateUser() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogUser newUser = new DatadogUser();
    newUser.setEmail("new.user@example.com");
    newUser.setName("New User");
    newUser.setTitle("Analyst");

    DatadogUser createdUser = DatadogApiCommands.createUser(CONFIG_ID, null, newUser);

    assertNotNull(createdUser);
    assertNotNull(createdUser.getId());
    assertEquals("new.user@example.com", createdUser.getEmail());
    assertEquals("New User", createdUser.getName());
    assertEquals("Analyst", createdUser.getTitle());
    assertEquals(Boolean.FALSE, createdUser.getDisabled());

    // verify it's in the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_user where id = ?")
        .addBindVar(createdUser.getId()).select(int.class);
    assertEquals(1, count);
  }

  public void testCreateUserIgnoredEmail() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogUser newUser = new DatadogUser();
    newUser.setEmail("admin@example.com");
    newUser.setName("Admin User");

    DatadogSettings settings = new DatadogSettings();
    settings.getIgnoreUserEmails().add("admin@example.com");

    try {
      DatadogApiCommands.createUser(CONFIG_ID, settings, newUser);
      fail("Should have thrown exception for ignored email");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("datadogIgnoreUserEmails"));
    }
  }

  public void testCreateUserConflictDisabled() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    // insert a disabled user directly into the mock table
    String existingId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(existingId).addBindVar("existing@example.com").addBindVar("Old Name").addBindVar("Old Title").addBindVar("T").addBindVar("F").executeSql();

    // create a user with the same email - should find existing, re-enable, and update
    DatadogUser newUser = new DatadogUser();
    newUser.setEmail("existing@example.com");
    newUser.setName("Updated Name");
    newUser.setTitle("Updated Title");

    DatadogUser result = DatadogApiCommands.createUser(CONFIG_ID, null, newUser);

    assertNotNull(result);
    assertEquals(existingId, result.getId());
    assertEquals("existing@example.com", result.getEmail());
    assertEquals("Updated Name", result.getName());
    assertEquals("Updated Title", result.getTitle());
    assertEquals(Boolean.FALSE, result.getDisabled());

    // verify the mock DB was updated (not a new row)
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_user").select(int.class);
    assertEquals(1, count);

    // verify disabled is now false
    String disabledVal = new GcDbAccess().connectionName("grouper")
        .sql("select disabled from mock_datadog_user where id = ?")
        .addBindVar(existingId).select(String.class);
    assertEquals("F", disabledVal);
  }

  public void testUpdateUser() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    // insert a user
    String userId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("update.me@example.com").addBindVar("Original Name").addBindVar("Original Title").addBindVar("F").addBindVar("F").executeSql();

    DatadogUser userToUpdate = new DatadogUser();
    userToUpdate.setId(userId);
    userToUpdate.setName("New Name");
    userToUpdate.setTitle("New Title");

    Set<String> fieldsToUpdate = new LinkedHashSet<String>();
    fieldsToUpdate.add("name");
    fieldsToUpdate.add("title");

    DatadogUser updatedUser = DatadogApiCommands.updateUser(CONFIG_ID, null, userToUpdate, fieldsToUpdate);

    assertNotNull(updatedUser);
    assertEquals(userId, updatedUser.getId());
    assertEquals("update.me@example.com", updatedUser.getEmail());
    assertEquals("New Name", updatedUser.getName());
    assertEquals("New Title", updatedUser.getTitle());
  }

  public void testRetrieveUserByEmail() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String userId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("find.me@example.com").addBindVar("Find Me").addBindVar("Tester").addBindVar("F").addBindVar("F").executeSql();

    DatadogUser found = DatadogApiCommands.retrieveUserByEmail(CONFIG_ID, null, "find.me@example.com", false);
    assertNotNull(found);
    assertEquals(userId, found.getId());
    assertEquals("find.me@example.com", found.getEmail());
    assertEquals("Find Me", found.getName());

    // non-existent email returns null
    DatadogUser notFound = DatadogApiCommands.retrieveUserByEmail(CONFIG_ID, null, "nobody@example.com", false);
    assertNull(notFound);

    // service account email should return null (filtered)
    String svcId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(svcId).addBindVar("svc@example.com").addBindVar("Service").addBindVar(null).addBindVar("F").addBindVar("T").executeSql();

    DatadogUser svcNotFound = DatadogApiCommands.retrieveUserByEmail(CONFIG_ID, null, "svc@example.com", false);
    assertNull(svcNotFound);

    // disabled user should be excluded by default
    String disabledId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(disabledId).addBindVar("disabled@example.com").addBindVar("Disabled").addBindVar(null).addBindVar("T").addBindVar("F").executeSql();

    DatadogUser disabledNotFound = DatadogApiCommands.retrieveUserByEmail(CONFIG_ID, null, "disabled@example.com", false);
    assertNull(disabledNotFound);

    // disabled user should be included when includeDisabledUsers=true
    DatadogUser disabledFound = DatadogApiCommands.retrieveUserByEmail(CONFIG_ID, null, "disabled@example.com", true);
    assertNotNull(disabledFound);
    assertEquals(disabledId, disabledFound.getId());
    assertEquals(Boolean.TRUE, disabledFound.getDisabled());
  }

  public void testDisableUser() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String userId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("disable.me@example.com").addBindVar("Disable Me").addBindVar("Tester").addBindVar("F").addBindVar("F").executeSql();

    DatadogUser userToDisable = new DatadogUser();
    userToDisable.setId(userId);
    userToDisable.setEmail("disable.me@example.com");

    DatadogUser result = DatadogApiCommands.disableUser(CONFIG_ID, null, userToDisable);

    assertNotNull(result);
    assertEquals(userId, result.getId());
    assertEquals(Boolean.TRUE, result.getDisabled());

    // verify in mock DB
    String disabledVal = new GcDbAccess().connectionName("grouper")
        .sql("select disabled from mock_datadog_user where id = ?")
        .addBindVar(userId).select(String.class);
    assertEquals("T", disabledVal);
  }

  // ==================== Role tests ====================

  public void testRetrieveRoles() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId1 = GrouperUuid.getUuid();
    String roleId2 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId1).addBindVar("Datadog Admin Role").addBindVar("role").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId2).addBindVar("Datadog Read Only Role").addBindVar("role").executeSql();

    // also insert a team - should NOT appear in role results
    String teamId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(teamId).addBindVar("Some Team").addBindVar("team").executeSql();

    List<DatadogGroup> roles = DatadogApiCommands.retrieveRoles(CONFIG_ID, null);

    assertEquals(2, roles.size());

    Map<String, DatadogGroup> roleById = new HashMap<String, DatadogGroup>();
    for (DatadogGroup role : roles) {
      roleById.put(role.getId(), role);
    }

    DatadogGroup role1 = roleById.get(roleId1);
    assertNotNull(role1);
    assertEquals("Datadog Admin Role", role1.getName());
    assertEquals("role", role1.getGroupType());

    DatadogGroup role2 = roleById.get(roleId2);
    assertNotNull(role2);
    assertEquals("Datadog Read Only Role", role2.getName());

    // team should not be in results
    assertNull(roleById.get(teamId));
  }

  public void testRetrieveRolesIgnoreRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId1 = GrouperUuid.getUuid();
    String roleId2 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId1).addBindVar("Datadog Admin Role").addBindVar("role").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId2).addBindVar("Datadog Standard Role").addBindVar("role").executeSql();

    DatadogSettings settings = new DatadogSettings();
    settings.getIgnoreRoles().add("datadog admin role");

    List<DatadogGroup> roles = DatadogApiCommands.retrieveRoles(CONFIG_ID, settings);

    assertEquals(1, roles.size());
    assertEquals(roleId2, roles.get(0).getId());
  }

  public void testCreateRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogGroup newRole = new DatadogGroup();
    newRole.setName("New Custom Role");

    DatadogGroup createdRole = DatadogApiCommands.createRole(CONFIG_ID, null, newRole);

    assertNotNull(createdRole);
    assertNotNull(createdRole.getId());
    assertEquals("New Custom Role", createdRole.getName());
    assertEquals("role", createdRole.getGroupType());

    // verify it's in the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_group where id = ? and group_type = 'role'")
        .addBindVar(createdRole.getId()).select(int.class);
    assertEquals(1, count);
  }

  public void testCreateRoleIgnored() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogGroup newRole = new DatadogGroup();
    newRole.setName("Datadog Admin Role");

    DatadogSettings settings = new DatadogSettings();
    settings.getIgnoreRoles().add("datadog admin role");

    try {
      DatadogApiCommands.createRole(CONFIG_ID, settings, newRole);
      fail("Should have thrown exception for ignored role");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("datadogIgnoreRoles"));
    }
  }

  public void testUpdateRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Original Role Name").addBindVar("role").executeSql();

    DatadogGroup roleToUpdate = new DatadogGroup();
    roleToUpdate.setId(roleId);
    roleToUpdate.setName("Updated Role Name");

    Set<String> fieldsToUpdate = new LinkedHashSet<String>();
    fieldsToUpdate.add("name");

    DatadogGroup updatedRole = DatadogApiCommands.updateRole(CONFIG_ID, null, roleToUpdate, fieldsToUpdate);

    assertNotNull(updatedRole);
    assertEquals(roleId, updatedRole.getId());
    assertEquals("Updated Role Name", updatedRole.getName());
    assertEquals("role", updatedRole.getGroupType());

    // verify in mock DB
    String dbName = new GcDbAccess().connectionName("grouper")
        .sql("select name from mock_datadog_group where id = ?")
        .addBindVar(roleId).select(String.class);
    assertEquals("Updated Role Name", dbName);
  }

  public void testDeleteRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Role To Delete").addBindVar("role").executeSql();

    DatadogGroup roleToDelete = new DatadogGroup();
    roleToDelete.setId(roleId);
    roleToDelete.setName("Role To Delete");

    DatadogApiCommands.deleteRole(CONFIG_ID, null, roleToDelete);

    // verify it's gone from the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_group where id = ?")
        .addBindVar(roleId).select(int.class);
    assertEquals(0, count);
  }

  public void testDeleteRoleIgnored() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Datadog Admin Role").addBindVar("role").executeSql();

    DatadogGroup roleToDelete = new DatadogGroup();
    roleToDelete.setId(roleId);
    roleToDelete.setName("Datadog Admin Role");

    DatadogSettings settings = new DatadogSettings();
    settings.getIgnoreRoles().add("datadog admin role");

    try {
      DatadogApiCommands.deleteRole(CONFIG_ID, settings, roleToDelete);
      fail("Should have thrown exception for ignored role");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("datadogIgnoreRoles"));
    }

    // verify it's still in the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_group where id = ?")
        .addBindVar(roleId).select(int.class);
    assertEquals(1, count);
  }

  // ==================== Role membership tests ====================

  public void testGetRoleUsers() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String userId3 = GrouperUuid.getUuid();

    // create role
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Test Role").addBindVar("role").executeSql();

    // create users
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("user1@example.com").addBindVar("User One").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("user2@example.com").addBindVar("User Two").addBindVar("Developer").addBindVar("F").addBindVar("F").executeSql();
    // service account - should be filtered out
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId3).addBindVar("svc@example.com").addBindVar("Service Bot").addBindVar(null).addBindVar("F").addBindVar("T").executeSql();

    // add memberships
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(roleId).addBindVar(userId1).executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(roleId).addBindVar(userId2).executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(roleId).addBindVar(userId3).executeSql();

    List<DatadogUser> roleUsers = DatadogApiCommands.getRoleUsers(CONFIG_ID, null, roleId);

    // service account should be filtered out
    assertEquals(2, roleUsers.size());

    Map<String, DatadogUser> userById = new HashMap<String, DatadogUser>();
    for (DatadogUser user : roleUsers) {
      userById.put(user.getId(), user);
    }

    assertNotNull(userById.get(userId1));
    assertEquals("user1@example.com", userById.get(userId1).getEmail());

    assertNotNull(userById.get(userId2));
    assertEquals("user2@example.com", userById.get(userId2).getEmail());

    // service account filtered
    assertNull(userById.get(userId3));
  }

  public void testAddUserToRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Test Role").addBindVar("role").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();

    DatadogApiCommands.addUserToRole(CONFIG_ID, null, roleId, userId);

    // verify in mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(roleId).addBindVar(userId).select(int.class);
    assertEquals(1, count);

    // adding again should be idempotent (no duplicate)
    DatadogApiCommands.addUserToRole(CONFIG_ID, null, roleId, userId);

    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(roleId).addBindVar(userId).select(int.class);
    assertEquals(1, count);
  }

  public void testRemoveUserFromRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Test Role").addBindVar("role").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();

    // add membership
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(roleId).addBindVar(userId).executeSql();

    DatadogApiCommands.removeUserFromRole(CONFIG_ID, null, roleId, userId);

    // verify removed from mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(roleId).addBindVar(userId).select(int.class);
    assertEquals(0, count);
  }

  public void testRemoveUserFromRoleNotFound() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String roleId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
        .addBindVar(roleId).addBindVar("Test Role").addBindVar("role").executeSql();

    // removing a non-existent membership should accept 404
    DatadogApiCommands.removeUserFromRole(CONFIG_ID, null, roleId, userId);
  }

  // ==================== Team tests ====================

  public void testRetrieveTeams() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId1 = GrouperUuid.getUuid();
    String teamId2 = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId1).addBindVar("Engineering Team").addBindVar("engineering-team").addBindVar("The engineering team").addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId2).addBindVar("Support Team").addBindVar("support-team").addBindVar(null).addBindVar("team").executeSql();

    // also insert a role - should NOT appear in team results
    String roleId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(roleId).addBindVar("Admin Role").addBindVar(null).addBindVar(null).addBindVar("role").executeSql();

    List<DatadogGroup> teams = DatadogApiCommands.retrieveTeams(CONFIG_ID, null);

    assertEquals(2, teams.size());

    Map<String, DatadogGroup> teamById = new HashMap<String, DatadogGroup>();
    for (DatadogGroup team : teams) {
      teamById.put(team.getId(), team);
    }

    DatadogGroup team1 = teamById.get(teamId1);
    assertNotNull(team1);
    assertEquals("Engineering Team", team1.getName());
    assertEquals("engineering-team", team1.getHandle());
    assertEquals("The engineering team", team1.getDescription());
    assertEquals("team", team1.getGroupType());

    DatadogGroup team2 = teamById.get(teamId2);
    assertNotNull(team2);
    assertEquals("Support Team", team2.getName());
    assertEquals("support-team", team2.getHandle());

    // role should not be in results
    assertNull(teamById.get(roleId));
  }

  public void testCreateTeam() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogGroup newTeam = new DatadogGroup();
    newTeam.setName("New Team");
    newTeam.setHandle("new-team");
    newTeam.setDescription("A new team");

    DatadogGroup createdTeam = DatadogApiCommands.createTeam(CONFIG_ID, null, newTeam);

    assertNotNull(createdTeam);
    assertNotNull(createdTeam.getId());
    assertEquals("New Team", createdTeam.getName());
    assertEquals("new-team", createdTeam.getHandle());
    assertEquals("A new team", createdTeam.getDescription());
    assertEquals("team", createdTeam.getGroupType());

    // verify it's in the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_group where id = ? and group_type = 'team'")
        .addBindVar(createdTeam.getId()).select(int.class);
    assertEquals(1, count);
  }

  public void testUpdateTeam() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Original Team").addBindVar("original-team").addBindVar("Original desc").addBindVar("team").executeSql();

    DatadogGroup teamToUpdate = new DatadogGroup();
    teamToUpdate.setId(teamId);
    teamToUpdate.setName("Updated Team");
    teamToUpdate.setHandle("updated-team");
    teamToUpdate.setDescription("Updated desc");

    Set<String> fieldsToUpdate = new LinkedHashSet<String>();
    fieldsToUpdate.add("name");
    fieldsToUpdate.add("handle");
    fieldsToUpdate.add("description");

    DatadogGroup updatedTeam = DatadogApiCommands.updateTeam(CONFIG_ID, null, teamToUpdate, fieldsToUpdate);

    assertNotNull(updatedTeam);
    assertEquals(teamId, updatedTeam.getId());
    assertEquals("Updated Team", updatedTeam.getName());
    assertEquals("updated-team", updatedTeam.getHandle());
    assertEquals("Updated desc", updatedTeam.getDescription());
    assertEquals("team", updatedTeam.getGroupType());

    // verify in mock DB
    String dbName = new GcDbAccess().connectionName("grouper")
        .sql("select name from mock_datadog_group where id = ?")
        .addBindVar(teamId).select(String.class);
    assertEquals("Updated Team", dbName);

    String dbHandle = new GcDbAccess().connectionName("grouper")
        .sql("select handle from mock_datadog_group where id = ?")
        .addBindVar(teamId).select(String.class);
    assertEquals("updated-team", dbHandle);
  }

  public void testDeleteTeam() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Team To Delete").addBindVar("team-to-delete").addBindVar(null).addBindVar("team").executeSql();

    DatadogGroup teamToDelete = new DatadogGroup();
    teamToDelete.setId(teamId);

    DatadogApiCommands.deleteTeam(CONFIG_ID, null, teamToDelete);

    // verify it's gone from the mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_group where id = ?")
        .addBindVar(teamId).select(int.class);
    assertEquals(0, count);
  }

  public void testDeleteTeamNotFound() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    DatadogGroup teamToDelete = new DatadogGroup();
    teamToDelete.setId(GrouperUuid.getUuid());

    // deleting a non-existent team should accept 404
    DatadogApiCommands.deleteTeam(CONFIG_ID, null, teamToDelete);
  }

  // ==================== Team membership tests ====================

  public void testGetTeamMemberships() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String membershipId1 = GrouperUuid.getUuid();
    String membershipId2 = GrouperUuid.getUuid();

    // create team
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Test Team").addBindVar("test-team").addBindVar(null).addBindVar("team").executeSql();

    // create users
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("user1@example.com").addBindVar("User One").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("user2@example.com").addBindVar("User Two").addBindVar("Developer").addBindVar("F").addBindVar("F").executeSql();

    // add memberships with roles
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(membershipId1).addBindVar(teamId).addBindVar(userId1).addBindVar("admin").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(membershipId2).addBindVar(teamId).addBindVar(userId2).addBindVar("member").executeSql();

    List<DatadogMembership> memberships = DatadogApiCommands.getTeamMemberships(CONFIG_ID, null, teamId);

    assertEquals(2, memberships.size());

    Map<String, DatadogMembership> membershipByUserId = new HashMap<String, DatadogMembership>();
    for (DatadogMembership m : memberships) {
      membershipByUserId.put(m.getUserId(), m);
    }

    DatadogMembership m1 = membershipByUserId.get(userId1);
    assertNotNull(m1);
    assertEquals(teamId, m1.getGroupId());
    assertEquals("admin", m1.getRole());

    DatadogMembership m2 = membershipByUserId.get(userId2);
    assertNotNull(m2);
    assertEquals(teamId, m2.getGroupId());
    assertEquals("member", m2.getRole());
  }

  public void testAddUserToTeam() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Test Team").addBindVar("test-team").addBindVar(null).addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();

    DatadogApiCommands.addUserToTeam(CONFIG_ID, null, teamId, userId);

    // verify in mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(int.class);
    assertEquals(1, count);

    // verify role is "member" (default)
    String role = new GcDbAccess().connectionName("grouper")
        .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(String.class);
    assertEquals("member", role);

    // adding again should be idempotent (no duplicate)
    DatadogApiCommands.addUserToTeam(CONFIG_ID, null, teamId, userId);

    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(int.class);
    assertEquals(1, count);
  }

  public void testRemoveUserFromTeam() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Test Team").addBindVar("test-team").addBindVar(null).addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();

    // add membership
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar(userId).addBindVar("member").executeSql();

    DatadogApiCommands.removeUserFromTeam(CONFIG_ID, null, teamId, userId);

    // verify removed from mock DB
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(int.class);
    assertEquals(0, count);
  }

  public void testRemoveUserFromTeamNotFound() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();

    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Test Team").addBindVar("test-team").addBindVar(null).addBindVar("team").executeSql();

    // removing a non-existent membership should accept 404
    DatadogApiCommands.removeUserFromTeam(CONFIG_ID, null, teamId, userId);
  }

  public void testUpdateTeamMembershipRole() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId = GrouperUuid.getUuid();
    String membershipId = GrouperUuid.getUuid();

    // create team and user
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Test Team").addBindVar("test-team").addBindVar(null).addBindVar("team").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId).addBindVar("user@example.com").addBindVar("Test User").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();

    // add membership as "member"
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(membershipId).addBindVar(teamId).addBindVar(userId).addBindVar("member").executeSql();

    // promote to admin
    DatadogApiCommands.updateTeamMembershipRole(CONFIG_ID, null, teamId, userId, "admin");

    // verify role changed in mock DB
    String role = new GcDbAccess().connectionName("grouper")
        .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(String.class);
    assertEquals("admin", role);

    // demote back to member (omit role; Datadog API only accepts "admin" or omitted)
    DatadogApiCommands.updateTeamMembershipRole(CONFIG_ID, null, teamId, userId, null);

    role = new GcDbAccess().connectionName("grouper")
        .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
        .addBindVar(teamId).addBindVar(userId).select(String.class);
    assertEquals("member", role);
  }

  public void testRetrieveTeamsWithAdmins() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    String teamId = GrouperUuid.getUuid();
    String userId1 = GrouperUuid.getUuid();
    String userId2 = GrouperUuid.getUuid();
    String userId3 = GrouperUuid.getUuid();

    // create team
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
        .addBindVar(teamId).addBindVar("Admin Test Team").addBindVar("admin-test-team").addBindVar("team for admin testing").addBindVar("team").executeSql();

    // create users
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId1).addBindVar("user1@example.com").addBindVar("User One").addBindVar("Engineer").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId2).addBindVar("user2@example.com").addBindVar("User Two").addBindVar("Manager").addBindVar("F").addBindVar("F").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId3).addBindVar("user3@example.com").addBindVar("User Three").addBindVar("Developer").addBindVar("F").addBindVar("F").executeSql();

    // add memberships: user1 is admin, user2 is admin, user3 is member
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar(userId1).addBindVar("admin").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar(userId2).addBindVar("admin").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(teamId).addBindVar(userId3).addBindVar("member").executeSql();

    // retrieve teams - this calls the API which fetches memberships and filters for admins
    List<DatadogGroup> teams = DatadogApiCommands.retrieveTeams(CONFIG_ID, null);

    assertEquals(1, teams.size());
    DatadogGroup team = teams.get(0);
    assertEquals(teamId, team.getId());
    assertEquals("Admin Test Team", team.getName());

    // Note: retrieveTeams itself does not populate admins - that happens in DatadogTargetDao.retrieveAllGroups
    // So here we test the building blocks: getTeamMemberships + filtering
    List<DatadogMembership> memberships = DatadogApiCommands.getTeamMemberships(CONFIG_ID, null, teamId);
    assertEquals(3, memberships.size());

    Set<String> adminUserIds = new LinkedHashSet<String>();
    for (DatadogMembership membership : memberships) {
      if ("admin".equals(membership.getRole())) {
        adminUserIds.add(membership.getUserId());
      }
    }

    assertEquals(2, adminUserIds.size());
    assertTrue(adminUserIds.contains(userId1));
    assertTrue(adminUserIds.contains(userId2));
    assertFalse(adminUserIds.contains(userId3));

    // now test the promote/demote cycle
    // promote user3 to admin
    DatadogApiCommands.updateTeamMembershipRole(CONFIG_ID, null, teamId, userId3, "admin");

    memberships = DatadogApiCommands.getTeamMemberships(CONFIG_ID, null, teamId);
    adminUserIds = new LinkedHashSet<String>();
    for (DatadogMembership membership : memberships) {
      if ("admin".equals(membership.getRole())) {
        adminUserIds.add(membership.getUserId());
      }
    }
    assertEquals(3, adminUserIds.size());
    assertTrue(adminUserIds.contains(userId3));

    // demote user1 back to member (omit role; Datadog API only accepts "admin" or omitted)
    DatadogApiCommands.updateTeamMembershipRole(CONFIG_ID, null, teamId, userId1, null);

    memberships = DatadogApiCommands.getTeamMemberships(CONFIG_ID, null, teamId);
    adminUserIds = new LinkedHashSet<String>();
    for (DatadogMembership membership : memberships) {
      if ("admin".equals(membership.getRole())) {
        adminUserIds.add(membership.getUserId());
      }
    }
    assertEquals(2, adminUserIds.size());
    assertTrue(adminUserIds.contains(userId2));
    assertTrue(adminUserIds.contains(userId3));
    assertFalse(adminUserIds.contains(userId1));
  }

  // =============================================
  // Helper methods for provisioner tests
  // =============================================

  /**
   * Helper to configure a team provisioner with groupType = "team" as a static attribute
   */
  private DatadogProvisionerTestConfigInput teamProvisionerConfig() {
    return new DatadogProvisionerTestConfigInput()
        .assignConfigId("datadogProvisioner")
        .addExtraConfig("numberOfGroupAttributes", "3")
        .addExtraConfig("targetGroupAttribute.0.name", "id")
        .addExtraConfig("targetGroupAttribute.1.name", "name")
        .addExtraConfig("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension")
        .addExtraConfig("targetGroupAttribute.2.name", "groupType")
        .addExtraConfig("targetGroupAttribute.2.translateExpressionType", "staticValues")
        .addExtraConfig("targetGroupAttribute.2.translateExpression", "'team'");
  }

  /**
   * Helper to configure a role provisioner with groupType = "role" as a static attribute
   */
  private DatadogProvisionerTestConfigInput roleProvisionerConfig() {
    return new DatadogProvisionerTestConfigInput()
        .assignConfigId("datadogProvisioner")
        .addExtraConfig("numberOfGroupAttributes", "3")
        .addExtraConfig("targetGroupAttribute.0.name", "id")
        .addExtraConfig("targetGroupAttribute.1.name", "name")
        .addExtraConfig("targetGroupAttribute.1.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.1.translateFromGrouperProvisioningGroupField", "extension")
        .addExtraConfig("targetGroupAttribute.2.name", "groupType")
        .addExtraConfig("targetGroupAttribute.2.translateExpressionType", "staticValues")
        .addExtraConfig("targetGroupAttribute.2.translateExpression", "'role'");
  }

  /**
   * Helper to set up the common provisioner test infrastructure
   */
  private GrouperSession setupProvisionerTest(DatadogProvisionerTestConfigInput configInput) {
    DatadogProvisionerTestUtils.setupDatadogExternalSystem();
    DatadogProvisionerTestUtils.configureDatadogProvisioner(configInput);

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    // ensure mock tables exist
    DatadogApiCommands.retrieveTeams(CONFIG_ID, null);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_group").executeSql();

    return GrouperSession.startRootSession();
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
    new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(userId0).addBindVar("test.subject.0@somewhere.someSchool.edu").addBindVar("my name is test.subject.0").addBindVar(null).addBindVar("F").addBindVar("F").executeSql();
    if (userId1 != null) {
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(userId1).addBindVar("test.subject.1@somewhere.someSchool.edu").addBindVar("my name is test.subject.1").addBindVar(null).addBindVar("F").addBindVar("F").executeSql();
    }
    if (userId2 != null) {
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(userId2).addBindVar("test.subject.2@somewhere.someSchool.edu").addBindVar("my name is test.subject.2").addBindVar(null).addBindVar("F").addBindVar("F").executeSql();
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
    attributeValue.setDoProvision("datadogProvisioner");
    attributeValue.setTargetName("datadogProvisioner");
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

    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      String userId2 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, userId2);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      String groupName = new GcDbAccess().connectionName("grouper").sql("select name from mock_datadog_group").select(String.class);
      assertEquals("testGroup", groupName);

      // remove one member and provision again
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // add a different member and provision again
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // delete the group entirely and provision again
      testGroup.delete();

      provision(isFull);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

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

    GrouperSession grouperSession = setupProvisionerTest(roleProvisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      String userId2 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, userId2);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testRole").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'role'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      String groupName = new GcDbAccess().connectionName("grouper").sql("select name from mock_datadog_group").select(String.class);
      assertEquals("testRole", groupName);

      // remove one member and provision again
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // add a different member and provision again
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // delete the group entirely and provision again
      testGroup.delete();

      provision(isFull);

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

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

    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testTeam").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // remove member, provision again: membership removed, team remains
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // re-add member, provision again: membership re-created
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

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

    GrouperSession grouperSession = setupProvisionerTest(roleProvisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testRole").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // initial provision always needs full sync to establish baseline
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'role'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // remove member, provision again: membership removed, role remains
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // re-add member, provision again: membership re-created
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      provision(isFull);

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

    } finally {

    }
  }

  // =============================================
  // Team admin metadata via provisioner
  // =============================================

  public void testFullSyncTeamAdminMetadata() {
    teamAdminMetadata(true);
  }

  public void testIncrementalTeamAdminMetadata() {
    teamAdminMetadata(false);
  }

  /**
   * With datadogAddTeamAdminMetadata enabled:
   * Two Grouper groups: the provisionable team group and a separate admin group.
   * The admin group path is stored in metadata (md_adminGroupName) on the team group.
   * The DatadogProvisioningTranslator automatically resolves the admin group members
   * to their target entity IDs (from the entity attribute value cache) when no explicit
   * translation is configured for the admins attribute.
   * Verify that members of the admin group become team admins in Datadog.
   */
  public void teamAdminMetadata(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    DatadogProvisionerTestConfigInput configInput = teamProvisionerConfig()
        .addExtraConfig("datadogAddTeamAdminMetadata", "true")
        // group attributes: id, name, groupType, admins
        .addExtraConfig("numberOfGroupAttributes", "4")
        .addExtraConfig("targetGroupAttribute.3.name", "admins")
        .addExtraConfig("targetGroupAttribute.3.showAttributeValueSettings", "true")
        .addExtraConfig("targetGroupAttribute.3.multiValued", "true");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // pre-create users in the target
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      // the admin group: only SUBJ0 is an admin
      Group adminGroup = new GroupSave(grouperSession).assignName("test:testTeamAdmins").save();
      adminGroup.addMember(SubjectTestHelper.SUBJ0, false);

      // the team group: SUBJ0 and SUBJ1 are team members. SUBJ0 and SUBJ1 are added directly
      // so that swapping admin group membership does not change effective team membership.
      // The admin group is also nested as a member so that admin-group membership changes
      // generate change-log entries on the team group, enabling incremental provisioning
      // to detect admin role changes (matching the expected production pattern).
      Group testTeam = new GroupSave(grouperSession).assignName("test:testTeam").save();
      testTeam.addMember(SubjectTestHelper.SUBJ0, false);
      testTeam.addMember(SubjectTestHelper.SUBJ1, false);
      testTeam.addMember(adminGroup.toSubject(), false);

      initIncrementalState(isFull);

      // attach provisioning to the team group directly with metadata pointing to the admin group
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("datadogProvisioner");
      attributeValue.setTargetName("datadogProvisioner");
      Map<String, Object> metadataNameValues = new HashMap<String, Object>();
      metadataNameValues.put("md_adminGroupName", "test:testTeamAdmins");
      attributeValue.setMetadataNameValues(metadataNameValues);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, testTeam);

      // first run: creates the team and adds members with default "member" role
      fullProvision();
      // second run: translator populates admins attribute, compare detects the difference,
      // updateGroup promotes admin members
      fullProvision();

      // verify team was created
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'team'").select(int.class));

      // get the team id
      String teamId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_datadog_group where name = 'testTeam' and group_type = 'team'").select(String.class);

      // verify both users are team members
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership where group_id = ?").addBindVar(teamId).select(int.class));

      // verify SUBJ0 (userId0) is admin (promoted)
      String user0Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
          .addBindVar(teamId).addBindVar(userId0).select(String.class);
      assertEquals("admin", user0Role);

      // verify SUBJ1 (userId1) is regular member
      String user1Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
          .addBindVar(teamId).addBindVar(userId1).select(String.class);
      assertEquals("member", user1Role);

      // now swap admin membership: demote SUBJ0, promote SUBJ1
      adminGroup.deleteMember(SubjectTestHelper.SUBJ0, false);
      adminGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // provision again - compare should detect the admins attribute difference and
      // issue two updateTeamMembershipRole calls: one to demote SUBJ0 (omit role),
      // one to promote SUBJ1 (role=admin). Use fullProvision because an admin swap does
      // not change effective team membership (both users are still members via direct
      // assignment), so incremental provisioning has no change-log entry to process.
      // Admin role changes are picked up by the hourly full sync.
      fullProvision();

      // verify SUBJ0 (userId0) is now demoted to regular member
      user0Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
          .addBindVar(teamId).addBindVar(userId0).select(String.class);
      assertEquals("member", user0Role);

      // verify SUBJ1 (userId1) is now promoted to admin
      user1Role = new GcDbAccess().connectionName("grouper")
          .sql("select role from mock_datadog_membership where group_id = ? and user_id = ?")
          .addBindVar(teamId).addBindVar(userId1).select(String.class);
      assertEquals("admin", user1Role);

      // team membership count should be unchanged - demote/promote should not add or remove memberships
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership where group_id = ?").addBindVar(teamId).select(int.class));

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
   * Users who are members of provisionable groups should be created in Datadog
   * if they don't already exist. Verify the provisioner calls createUser.
   */
  public void userInsert(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig());

    try {
      // no pre-created mock users - provisioner should create them
      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // no users in mock DB yet
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_user").select(int.class));

      fullProvision();

      // provisioner should have created both users
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_user").select(int.class));

      // verify user emails
      String email0 = new GcDbAccess().connectionName("grouper")
          .sql("select email from mock_datadog_user where email = ?")
          .addBindVar("test.subject.0@somewhere.someSchool.edu").select(String.class);
      assertNotNull(email0);

      String email1 = new GcDbAccess().connectionName("grouper")
          .sql("select email from mock_datadog_user where email = ?")
          .addBindVar("test.subject.1@somewhere.someSchool.edu").select(String.class);
      assertNotNull(email1);

      // verify team was created and memberships established
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_group where group_type = 'team'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

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
   * If a user exists in Datadog but is disabled, the provisioner should re-enable them
   * when they are added to a provisionable group (createUser checks for existing disabled users).
   */
  public void userReEnable(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig());

    try {
      // pre-create a disabled user in the mock DB
      String existingUserId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(existingUserId).addBindVar("test.subject.0@somewhere.someSchool.edu").addBindVar("my name is test.subject.0").addBindVar(null).addBindVar("T").addBindVar("F").executeSql();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // user exists but is disabled
      String disabledBefore = new GcDbAccess().connectionName("grouper")
          .sql("select disabled from mock_datadog_user where id = ?")
          .addBindVar(existingUserId).select(String.class);
      assertEquals("T", disabledBefore);

      fullProvision();

      // user should be re-enabled (createUser finds existing disabled user and re-enables)
      String disabledAfter = new GcDbAccess().connectionName("grouper")
          .sql("select disabled from mock_datadog_user where id = ?")
          .addBindVar(existingUserId).select(String.class);
      assertEquals("F", disabledAfter);

      // should still be only 1 user (not a duplicate)
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_user").select(int.class));

      // membership should be established
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

    } finally {

    }
  }

  public void testFullSyncUserDisable() {
    userDisable(true);
  }

  public void testIncrementalUserDisable() {
    userDisable(false);
  }

  /**
   * When a user is removed from all provisionable groups and deleteEntities is enabled,
   * the provisioner should disable them in Datadog (soft delete).
   */
  public void userDisable(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    DatadogProvisionerTestConfigInput configInput = teamProvisionerConfig()
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true");

    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // pre-create users in the target
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      fullProvision();

      // both users active, both in team
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

      // remove SUBJ1 from all groups (simulate user leaving)
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      provision(isFull);

      // SUBJ1 should be disabled in Datadog
      String disabled1 = new GcDbAccess().connectionName("grouper")
          .sql("select disabled from mock_datadog_user where id = ?")
          .addBindVar(userId1).select(String.class);
      assertEquals("T", disabled1);

      // SUBJ0 should still be active
      String disabled0 = new GcDbAccess().connectionName("grouper")
          .sql("select disabled from mock_datadog_user where id = ?")
          .addBindVar(userId0).select(String.class);
      assertEquals("F", disabled0);

      // only 1 membership remaining
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_datadog_membership").select(int.class));

    } finally {

    }
  }

}
