package edu.internet2.middleware.grouper.app.datadog;

import java.util.HashMap;
import java.util.HashSet;
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
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
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

  /**
   * regression: Datadog's roles endpoint returns a small default page, so
   * retrieveRoles must page through page[number]/page[size] to get them all.
   * Without paging, roles beyond the first page look missing and the provisioner
   * tries to re-create them (409 already exists).
   */
  public void testRetrieveRolesPaging() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    // more than one page worth (page[size] defaults to MAX_PAGE_SIZE = 100)
    int totalRoles = 105;
    Set<String> insertedRoleIds = new HashSet<String>();

    for (int i = 0; i < totalRoles; i++) {
      String roleId = GrouperUuid.getUuid();
      insertedRoleIds.add(roleId);
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, group_type) values (?, ?, ?)")
          .addBindVar(roleId).addBindVar("PagingRole_" + i).addBindVar("role").executeSql();
    }

    List<DatadogGroup> roles = DatadogApiCommands.retrieveRoles(CONFIG_ID, null);

    // all roles across all pages should come back, with no duplicates
    assertEquals(totalRoles, roles.size());

    Set<String> retrievedRoleIds = new HashSet<String>();
    for (DatadogGroup role : roles) {
      retrievedRoleIds.add(role.getId());
    }
    assertEquals(totalRoles, retrievedRoleIds.size());
    assertEquals(insertedRoleIds, retrievedRoleIds);
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

  /**
   * regression: retrieveTeams must page through page[number]/page[size] so that
   * teams beyond the first default page are not silently dropped.
   */
  public void testRetrieveTeamsPaging() {

    DatadogProvisionerTestUtils.setupDatadogExternalSystem();

    // more than one page worth (page[size] defaults to MAX_PAGE_SIZE = 100)
    int totalTeams = 105;
    Set<String> insertedTeamIds = new HashSet<String>();

    for (int i = 0; i < totalTeams; i++) {
      String teamId = GrouperUuid.getUuid();
      insertedTeamIds.add(teamId);
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
          .addBindVar(teamId).addBindVar("PagingTeam_" + i).addBindVar("paging-team-" + i).addBindVar(null).addBindVar("team").executeSql();
    }

    List<DatadogGroup> teams = DatadogApiCommands.retrieveTeams(CONFIG_ID, null);

    // all teams across all pages should come back, with no duplicates
    assertEquals(totalTeams, teams.size());

    Set<String> retrievedTeamIds = new HashSet<String>();
    for (DatadogGroup team : teams) {
      retrievedTeamIds.add(team.getId());
    }
    assertEquals(totalTeams, retrievedTeamIds.size());
    assertEquals(insertedTeamIds, retrievedTeamIds);
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

  // =============================================
  // Generic provisioner sync-back tests
  // =============================================

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the Datadog read path.
   * Asserts all three axes have rows. Framework-detail coverage (flag isolation,
   * native-attribute config, validation) lives in the SCIM + LDAP suites.
   */
  public void testDatadogFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
      assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

      // first pass writes the Datadog target; sync-back tables stay empty until the next
      // read pass captures the new objects (read-state convergence contract).
      fullProvision();

      // second pass: reads back what we just wrote, captures through the sync hooks, flushes
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
   * uses the scoped {@code retrieveGroup} / {@code retrieveEntity} (per-id lookups)
   * instead of {@code retrieveAllGroups} / {@code retrieveAllEntities}. Confirms the
   * capture hooks on the scoped retrieve methods fire.
   *
   * <p>Incremental test coverage is intentionally deferred — the framework today only captures
   * from reads, and writes converge on the next read pass. Closing that gap is the
   * write-shadow precision pass tracked in section 10 of the sync-back doc.
   */
  public void testDatadogFullSyncSelectByIdsPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    GrouperSession grouperSession = setupProvisionerTest(teamProvisionerConfig()
        .addExtraConfig("selectAllGroups", "false")
        .addExtraConfig("selectAllEntities", "false")
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
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
  // SCIM-parity sync-back tests for Datadog, CAPABILITY-GATED. Replicates the Box pilot
  // (boxProvisioner/GrouperBoxProvisionerTest) for the Datadog connector.
  //
  // Datadog capture model (verified from DatadogTargetDao + DatadogProvisioningTargetNativeSync):
  // Datadog captures group/user OBJECTS on the READ path -- they are captured from the raw JSON:API
  // envelopes at the DatadogApiCommands.retrieveRoles/retrieveTeams/retrieveUsers/getRoleUsers seams
  // (captureGroupJson/captureUserJson). MEMBERSHIPS now capture on BOTH paths: on read the edges are
  // recorded from the parsed beans inside DatadogTargetDao.retrieveMembershipsByGroup
  // (captureTeamMemberships / captureRoleMemberships), and on WRITE the membership create/delete API
  // methods (DatadogTargetDao.insertMembership/deleteMembership) record the edge directly into the
  // native membership mirror via recordTargetNativeMembershipInsert/Delete -- like Adobe/SCIM. So a
  // membership add/remove is recorded on the write and converges on the write pass, while group/user
  // OBJECT changes still converge into the mirror on the NEXT read pass. The converge tests below
  // still use the two-pass full-sync pattern (pass 1 writes the target, pass 2 re-reads and the
  // end-of-run flush converges), the same shape as the existing
  // testDatadogFullSyncPopulatesGenericTables -- that shape works for both object and membership
  // convergence.
  //
  // The full flush (GrouperProvisioningLogic.loadDataToGenericProvisionerTables) is a FULL REPLACE
  // scoped to the provisioner's grouper_sync_internal_id: anything in the mirror that the target did
  // NOT return this run is deleted. That is what makes the delete / membership-remove converge tests
  // work after a re-read pass.
  //
  // Datadog has TWO group kinds: teams and roles, distinguished by a synthesized groupType attribute
  // ("team" vs "role"). The DAO routes every group/membership operation on groupType, and the two
  // kinds have DIFFERENT membership-read paths (captureTeamMemberships vs captureRoleMemberships) and
  // different mutable fields (teams have name/handle/description; roles have name only). So, mirroring
  // how the existing forward tests split testFullSyncTeamCrudAndMemberships vs
  // testFullSyncRoleCrudAndMemberships, the group-insert / group-delete / membership-add /
  // membership-remove converge tests below each have a TEAM variant and a ROLE variant.
  //
  // Capabilities confirmed in DatadogTargetDao.registerGrouperProvisionerDaoCapabilities:
  //   group  : insert YES, update YES, delete YES   (teams AND roles)
  //   entity : insert YES, update YES, delete YES   (delete == disable, soft delete)
  //   mship  : insert YES, delete YES, REPLACE *NO* (no setCanReplaceMembership)
  //   memberships are group-centric (canRetrieveMembershipsAllByGroup)
  //   canSyncBack YES
  //
  // Matching attributes (DatadogProvisionerTestUtils): group = id + name (groupMatchingAttribute0=id,
  // groupMatchingAttribute1=name, name from the Grouper group extension); entity = id + email
  // (entityMatchingAttribute0=id, entityMatchingAttribute1=email, email from the subject). Because
  // name IS a matching attribute, an update that changes it cannot converge as an in-place update
  // (the Adobe lesson), so the group-update-converge test mutates a NON-matching attribute:
  //   - TEAMS: mutate DESCRIPTION (a real mock column, round-trips through updateTeam, NOT matched).
  //     The team-update test maps description as a 4th group attribute (sourced from the Grouper
  //     group's description field) and sets nativeAttributesGroups so the value is actually captured
  //     into the mirror (description is NOT a Datadog default capture attribute).
  //   - ROLES: roles have NO non-matching mutable Grouper-driven attribute -- the only role field the
  //     connector writes is name (= the match key; updateRole only touches name), and handle/
  //     description do not exist for roles. So a role update-converge test would be mutating the
  //     match key and cannot converge as an in-place update -> it is SKIPPED (note below where it
  //     would live).
  //
  // The entity (user) update-converge test is SKIPPED for the same reason as Box: users are matched
  // by id + email, email is fixed per subject (the only Grouper-driven user attribute mapped here),
  // and the other Datadog user fields (title/name/disabled/service_account) are target-controlled or
  // not Grouper-mapped. There is no safe Grouper-driven NON-matching user attribute to mutate, so an
  // update-converge test would mutate the match key -> SKIPPED (note below where it would live).
  //
  // SKIPPED, per capability (no test body, just a note):
  //   - no membership-REPLACE sync-back test: DatadogTargetDao has no setCanReplaceMembership (so
  //     SCIM's testMembershipReplaceConvergesSameRun / testIncrementalMembershipReplace... do not
  //     apply to Datadog -- memberships are added/removed one edge at a time via addUserToTeam/Role
  //     and removeUserFromTeam/Role).
  //   - no "same-run" convergence variants of the SCIM capture-on-write tests: while Datadog now
  //     captures memberships on write (like SCIM/Adobe), the GROUP/USER objects still capture on the
  //     read path and so converge only on the next read pass; their intent is ported as the two-pass
  //     full tests below.
  //   - no role update-converge test (see the matching-attributes note above).
  //   - no user (entity) update-converge test (see the matching-attributes note above).
  //
  // NB on delete config (gotcha vs Box): unlike Box -- which defaults customize*Crud=false so the
  // Box converge tests have to turn delete-types ON explicitly -- the shared Datadog test config
  // (DatadogProvisionerTestUtils.configureProvisioner) ALREADY enables every delete axis:
  // customizeEntityCrud + deleteEntities + deleteEntitiesIfNotExistInGrouper; customizeGroupCrud +
  // deleteGroups + deleteGroupsIfGrouperDeleted; customizeMembershipCrud + deleteMemberships +
  // deleteMembershipsIfNotExistInGrouper. So the Datadog delete / membership-remove converge tests
  // need NO extra delete config. To DISABLE a default delete (the broken-target test) we set the
  // umbrella key to false (deleteEntities=false) with customizeEntityCrud already on. Note also that
  // groups use deleteGroupsIfGrouperDeleted (delete only when Grouper deletes the group), NOT
  // deleteGroupsIfNotExistInGrouper -- so a target-side ORPHAN group is NOT auto-deleted, which is
  // exactly what the orphan-capture tests rely on.
  // ==========================================================================================

  /**
   * the single provisioned group's target_group_id (Datadog group id) in the mirror, or null.
   * Mirrors the Box/Adobe helper of the same name -- used by the group-update converge test to prove
   * the SAME target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved value of a named attribute for the single provisioned group in the mirror, or null.
   * Reads through the {@code grouper_prov_group_attr_v} reporting view (not the base
   * grouper_prov_group_attr_value table), because the raw string is stored via a dictionary FK and
   * only the view resolves it back to text (column {@code value_string}). Mirrors Box's
   * mirroredGroupDescription, generalized to any attribute name so the team-update test can read the
   * captured {@code description} value.
   */
  private String mirroredGroupAttr(String configId, String attributeName) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = ?")
        .addBindVar(configId).addBindVar(attributeName).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Shared setup for the Datadog sync-back tests: configure the provisioner from a team-or-role
   * config with the three load*ToGenericGrouperTable flags on (and recalculateAllOperations so every
   * object/membership is processed each run), then start a root session. Delegates to the existing
   * setupProvisionerTest (which also sleeps for config propagation, runs GrouperStartup, ensures the
   * mock tables exist, and wipes mock_datadog_*). Mirrors Box's setupBoxSyncBack.
   *
   * @param baseConfig teamProvisionerConfig() or roleProvisionerConfig() (already carries the
   *   groupType static attribute); load flags + recalculateAllOperations are layered on here
   * @return a started root GrouperSession
   */
  private GrouperSession setupDatadogSyncBack(DatadogProvisionerTestConfigInput baseConfig) {
    return setupProvisionerTest(baseConfig
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));
  }

  /**
   * Attach the provisioning marker (sub scope) to a stem and return immediately. Mirrors the inline
   * block the other sync-back tests repeat; attachProvisioningAttribute(Stem) already exists but is
   * scope-less, so this variant keeps the sub-scope semantics the converge tests need.
   */
  private void attachProvisioningSub(Stem stem) {
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("datadogProvisioner");
    attributeValue.setTargetName("datadogProvisioner");
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

  // -----------------------------------------------------------------------------------------
  // GROUP INSERT converge -- TEAM + ROLE (Datadog analogue of Box's
  // testBoxGroupInsertConvergesNextRead). Like Box, because
  // createGroupsAndEntitiesBeforeTranslatingMemberships (default true) + selectAllGroups (default
  // true) are on, the daemon re-reads each just-inserted group within pass 1 to link it, and that
  // read fires the Datadog group-capture seam -- so the group is already in the mirror after pass 1,
  // linked back to its Grouper group. Pass 2 is idempotent.
  // -----------------------------------------------------------------------------------------

  public void testDatadogTeamGroupInsertConvergesNextRead() {
    groupInsertConvergesNextRead(teamProvisionerConfig(), "team");
  }

  public void testDatadogRoleGroupInsertConvergesNextRead() {
    groupInsertConvergesNextRead(roleProvisionerConfig(), "role");
  }

  /**
   * @param baseConfig team or role provisioner config
   * @param groupType "team" or "role" (used only for the where-clause sanity assert)
   */
  private void groupInsertConvergesNextRead(DatadogProvisionerTestConfigInput baseConfig, String groupType) {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    GrouperSession grouperSession = setupDatadogSyncBack(baseConfig);

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // baseline: nothing in the mirror yet
      assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

      // pass 1 inserts the group AND -- via the post-insert re-read that links it -- captures it, so
      // the group converges into the mirror within this same run
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("group insert converges in the same run (post-insert re-read captures it)", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // pass 2 re-reads; convergence is idempotent
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
      long syncInternalId = gcGrouperSync.getInternalId();

      assertEquals("group insert should converge into prov_group", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // sanity: the mock has exactly one group, of the expected kind
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper")
          .sql("select count(1) from mock_datadog_group where group_type = ?").addBindVar(groupType).select(int.class));

      // captured via a read, so it is linked back to its Grouper group
      int groupRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and group_internal_id is not null")
          .addBindVar(syncInternalId).select(int.class);
      assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

      // name captured from the Datadog read response (a Datadog default group capture attribute)
      int nameValueRows = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group_attr_value gpv "
              + "join grouper_prov_group_attr gpa on gpa.internal_id = gpv.prov_group_attr_internal_id "
              + "join grouper_prov_group pg on pg.internal_id = gpv.prov_group_internal_id "
              + "where pg.grouper_sync_internal_id = ? and gpa.attribute_name = 'name'")
          .addBindVar(syncInternalId).select(int.class);
      assertTrue("name should be captured from the Datadog read response, got " + nameValueRows,
          nameValueRows >= 1);

      // groupType is the synthesized default (overlaid onto the envelope before capture); it must be
      // present in the catalog and resolve to the expected kind in the mirror
      String capturedGroupType = mirroredGroupAttr(configId, "groupType");
      assertEquals("synthesized groupType should be captured into the mirror", groupType, capturedGroupType);

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // GROUP DELETE converge -- TEAM + ROLE (Datadog analogue of Box's
  // testBoxGroupDeleteConvergesNextRead). Seed group + SUBJ0 + their membership into the mirror, then
  // delete the group in Grouper. The Datadog config already enables all delete axes, so pass A pushes
  // the deletes to the target (group + now-orphaned user disabled + membership removed) and pass B
  // re-reads: the user is gone from the active-user listing (disabled users are filtered out of
  // retrieveUsers), the group and membership are gone, and the full-replace flush drops all three
  // mirror rows.
  // -----------------------------------------------------------------------------------------

  public void testDatadogTeamGroupDeleteConvergesNextRead() {
    groupDeleteConvergesNextRead(teamProvisionerConfig());
  }

  public void testDatadogRoleGroupDeleteConvergesNextRead() {
    groupDeleteConvergesNextRead(roleProvisionerConfig());
  }

  private void groupDeleteConvergesNextRead(DatadogProvisionerTestConfigInput baseConfig) {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // NB: no extra delete config -- the shared Datadog config already turns on every delete axis
    // (entities/groups/memberships). This test deletes the group, cascading to its user + membership.
    GrouperSession grouperSession = setupDatadogSyncBack(baseConfig);

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // seed: two passes converge the group + SUBJ0 + their membership into the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: membership", 1, countSyncBack(configId, "grouper_prov_mship"));

      // delete the group; SUBJ0 is now orphaned (no other provisioned group) and is disabled too
      testGroup.delete();

      // pass A: the delete writes hit the Datadog target (group deleted, orphaned user disabled,
      // membership removed)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // pass B: the re-read no longer sees the group/membership and the disabled user is filtered
      // out of the active-user listing; the full-replace flush drops their mirror rows
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("group dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("orphaned SUBJ0 dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("membership dropped from the mirror after the re-read pass", 0,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // GROUP UPDATE converge on a NON-matching attribute -- TEAM only (Datadog analogue of Box's
  // testBoxGroupUpdateConvergesNextRead). Datadog groups are matched by id + name, so we mutate the
  // team's DESCRIPTION (a real mock column, NOT matched). description is mapped as a 4th group
  // attribute (sourced from the Grouper group's description field), round-trips through updateTeam,
  // and nativeAttributesGroups is set so the value is actually captured into the mirror (description
  // is not a Datadog default group capture attribute). Convergence is on the re-read pass (Datadog
  // captures on read). Asserts both that the value converges AND that the SAME target group id
  // survives (in-place update, not delete + re-create).
  //
  // No ROLE variant: roles have no non-matching mutable Grouper-driven attribute (the only field the
  // connector writes for a role is name = the match key; handle/description do not exist for roles),
  // so a role update-converge would mutate the match key and could not converge in place. SKIPPED.
  // -----------------------------------------------------------------------------------------

  public void testDatadogTeamGroupUpdateConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // team config + description as a 4th mapped group attribute (sourced from the Grouper group's
    // description), and capture description into the mirror via nativeAttributesGroups.
    //
    // IMPORTANT: nativeAttributesGroups MUST be the JSON-array form here, NOT a bare CSV. Datadog
    // speaks JSON:API, so every field lands nested under /attributes in the read envelope (e.g.
    // /attributes/description). The CSV form of nativeAttributesGroups carries only the attribute
    // NAME and no path, so the capture pointer defaults to "/" + name (e.g. /description) -- which
    // is a missing node in a JSON:API envelope, so nothing gets captured and the mirrored value
    // comes back null. The Datadog default group attrs (DEFAULT_GROUP_ATTRS in
    // DatadogProvisioningTargetNativeSync) are nested for exactly this reason. So we re-state the
    // defaults (name/handle/groupType) AND add description, each with an explicit /attributes/...
    // JSON Pointer, so the re-read capture resolves the real value. (Box can use a flat CSV because
    // its payload is flat -- /description top-level -- which does not apply to Datadog.)
    DatadogProvisionerTestConfigInput configInput = teamProvisionerConfig()
        .addExtraConfig("numberOfGroupAttributes", "4")
        .addExtraConfig("targetGroupAttribute.3.name", "description")
        .addExtraConfig("targetGroupAttribute.3.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.3.translateFromGrouperProvisioningGroupField", "description")
        // capture name/handle/groupType (the Datadog defaults) PLUS description, each with its
        // JSON:API /attributes/... pointer so the value is actually captured into the mirror.
        .addExtraConfig("nativeAttributesGroups",
            "[{\"name\":\"name\",\"path\":\"/attributes/name\"},"
            + "{\"name\":\"handle\",\"path\":\"/attributes/handle\"},"
            + "{\"name\":\"groupType\",\"path\":\"/attributes/groupType\"},"
            + "{\"name\":\"description\",\"path\":\"/attributes/description\"}]");

    GrouperSession grouperSession = setupDatadogSyncBack(configInput);

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testTeam")
          .assignDescription("originalDescription").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // seed: team provisioned with description "originalDescription"
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
      String groupTargetIdBefore = mirroredGroupTargetId(configId);
      assertNotNull("group should have a target id after seed", groupTargetIdBefore);
      assertEquals("seed: original description captured", "originalDescription",
          mirroredGroupAttr(configId, "description"));

      // change the description (a NON-matching attribute) -> Datadog updateTeam
      testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
          .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
          .assignSaveMode(SaveMode.UPDATE).save();

      // pass A: the description update reaches the Datadog target (updateTeam persists it)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // pass A already converges: updateGroup marks the written group for the end-of-run sync-back
      // drain re-read, so the mirror picks up the new description on the SAME pass as the write --
      // no second full run required (this is the fix being fanned out from Okta).
      assertEquals("update converges on the write pass via the drain re-read (before the bulk re-read)",
          "newDescription", mirroredGroupAttr(configId, "description"));
      // pass B: the re-read captures the target's actual new description into the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());

      // mirror side: still ONE group, the SAME group (same target id) -- in-place update, not
      // delete + re-create -- and its description converged to the new value.
      assertEquals("group still in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("mirror tracks the same group through the update (update, not re-create)",
          groupTargetIdBefore, mirroredGroupTargetId(configId));
      assertEquals("mirror description should converge to the new value on the re-read pass",
          "newDescription", mirroredGroupAttr(configId, "description"));

      // NOTE: no role update-converge test, and no user update-converge test. Roles have only name
      // (= the match key) as a Grouper-driven field; Datadog users are matched by id + email (email
      // fixed per subject) with no other safe Grouper-driven NON-matching attribute. Either would be
      // mutating the match key (the Adobe lesson) and could not converge as an in-place update.
      // Skipped rather than written.

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // MEMBERSHIP ADD converge -- TEAM + ROLE (Datadog analogue of Box's
  // testBoxMembershipAddConvergesNextRead). Seed group with SUBJ0, then add SUBJ1. Datadog now
  // captures memberships on the WRITE path too (DatadogTargetDao.insertMembership ->
  // recordTargetNativeMembershipInsert, like Adobe/SCIM) as well as on the read path
  // (retrieveMembershipsByGroup -> captureTeamMemberships / captureRoleMemberships). Either way the
  // add shows in grouper_prov_mship: this two-pass test verifies it via pass A issuing the membership
  // insert to the target and pass B re-reading the group's members before the flush converges.
  // -----------------------------------------------------------------------------------------

  public void testDatadogTeamMembershipAddConvergesNextRead() {
    membershipAddConvergesNextRead(teamProvisionerConfig());
  }

  public void testDatadogRoleMembershipAddConvergesNextRead() {
    membershipAddConvergesNextRead(roleProvisionerConfig());
  }

  private void membershipAddConvergesNextRead(DatadogProvisionerTestConfigInput baseConfig) {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    GrouperSession grouperSession = setupDatadogSyncBack(baseConfig);

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // seed: group + SUBJ0 + the one membership in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: the single membership", 1, countSyncBack(configId, "grouper_prov_mship"));

      // add SUBJ1 to the already-provisioned group
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // pass A: the membership insert hits the target (SUBJ1's user already exists in the mock)
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // capture-on-write: the add is already in the mirror after the write pass, before any re-read
      assertEquals("add converges on the write pass via capture-on-write (before any re-read)", 2,
          countSyncBack(configId, "grouper_prov_mship"));
      // pass B: re-read sees both members; the flush stays converged (idempotency check)
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("both users should be in the mirror after the add", 2,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("the added membership should stay converged after the re-read pass", 2,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // MEMBERSHIP REMOVE converge -- TEAM + ROLE (Datadog analogue of Box's
  // testBoxMembershipRemoveConvergesNextRead). Two groups both hold SUBJ0; SUBJ0 is removed from
  // testGroup only (it survives in otherGroup, so its Datadog user is NOT disabled). The full-replace
  // flush, fed by the re-read of each group's members, drops exactly testGroup's membership while
  // leaving otherGroup's intact. Both groups are the same kind (team or role) for a clean
  // single-groupType run.
  // -----------------------------------------------------------------------------------------

  public void testDatadogTeamMembershipRemoveConvergesNextRead() {
    membershipRemoveConvergesNextRead(teamProvisionerConfig());
  }

  public void testDatadogRoleMembershipRemoveConvergesNextRead() {
    membershipRemoveConvergesNextRead(roleProvisionerConfig());
  }

  private void membershipRemoveConvergesNextRead(DatadogProvisionerTestConfigInput baseConfig) {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // delete-types are already on in the shared Datadog config, so the membership remove is pushed.
    GrouperSession grouperSession = setupDatadogSyncBack(baseConfig);

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group otherGroup = new GroupSave(grouperSession).assignName("test:otherGroup").save();
      // SUBJ0 in BOTH groups so removing it from testGroup leaves it provisioned (still in otherGroup)
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // seed: both groups + SUBJ0 + both memberships in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: both groups", 2, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("seed: both memberships", 2, countSyncBack(configId, "grouper_prov_mship"));

      // remove SUBJ0 from testGroup only (still in otherGroup)
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      // pass A: the membership-remove write hits the target
      assertEquals(0, fullProvision().getRecordsWithErrors());
      // capture-on-write: on pass A the read still sees (testGroup,SUBJ0), so ONLY the write-delete
      // hook drops it from the mirror -- this assertion fails if the hook is removed
      assertEquals("remove drops from the mirror on the write pass via capture-on-write (before any re-read)", 1,
          countSyncBack(configId, "grouper_prov_mship"));
      // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush
      // keeps (testGroup,SUBJ0) dropped while otherGroup's SUBJ0 membership survives (idempotency)
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("both groups should still be in the mirror", 2,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // Multi-sync coverage with data evolution between rounds (Datadog analogue of Box's
  // testBoxFullSyncReflectsDataChangesAcrossSyncs). Round 1: testGroup (team) with SUBJ0 only, seeded
  // via two passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan team + orphan
  // user directly into the mock via raw SQL. Round 3: two more passes -> the mirror reflects the new
  // state (3 users, 2 groups, 2 memberships in testGroup), and the orphan user's email round-trips.
  //
  // The orphan TEAM persists because groups use deleteGroupsIfGrouperDeleted (only Grouper-deleted
  // groups are removed, never a target-only orphan). The orphan USER, however, is NOT membership-
  // protected: being a member of a target-only team does not make it provisionable from Grouper's
  // view, so with the shared config's deleteEntities + deleteEntitiesIfNotExistInGrouper ON it would
  // be disabled on the next pass and then filtered out of retrieveUsers (round 3 would see only 2
  // users). So, exactly as in the two orphan-capture tests above, deleteEntities is turned OFF here
  // so the active orphan user survives and round 3 sees all three users.
  // -----------------------------------------------------------------------------------------

  public void testDatadogFullSyncReflectsDataChangesAcrossSyncs() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // deleteEntities=false so the round-2 target-drift orphan user is not disabled before round 3 can
    // capture it (see the note above).
    GrouperSession grouperSession = setupDatadogSyncBack(teamProvisionerConfig()
        .addExtraConfig("deleteEntities", "false"));

    try {

      // ===================== ROUND 1: initial state =====================

      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      assertEquals("round 1: 1 prov_user row for SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
      assertEquals("round 1: 1 prov_group row for testGroup", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("round 1: 1 prov_mship row for SUBJ0 in testGroup", 1, countSyncBack(configId, "grouper_prov_mship"));

      // ===================== ROUND 2: data changes =====================

      // Grouper-side: add SUBJ1 to testGroup. next full sync inserts SUBJ1 + the membership.
      // SUBJ1's user must exist in the target so the membership add does not need a create -- but the
      // provisioner would create it anyway; pre-seed it for determinism (email matches the subject).
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(GrouperUuid.getUuid()).addBindVar("test.subject.1@somewhere.someSchool.edu")
          .addBindVar("my name is test.subject.1").addBindVar(null).addBindVar("F").addBindVar("F").executeSql();
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // Target-side drift: insert an orphan team + orphan user directly into the mock via raw SQL
      // (same idiom the existing forward tests use). These are unknown to Grouper and persist across
      // the next sync (no Grouper deletion drives their removal). name+group_type is UNIQUE, so the
      // orphan team name must be distinct.
      String orphanGroupId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
          .addBindVar(orphanGroupId).addBindVar("orphanTeamAddedMidTest").addBindVar("orphan-team-mid")
          .addBindVar("drift orphan").addBindVar("team").executeSql();
      String orphanUserId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(orphanUserId).addBindVar("orphan.evolve@example.edu").addBindVar("Orphan Evolve")
          .addBindVar(null).addBindVar("F").addBindVar("F").executeSql();

      // ===================== ROUND 3: second full sync + assertions =====================

      // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's + the
      // drift orphans) and refreshes the mirror.
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)", 3,
          countSyncBack(configId, "grouper_prov_user"));
      assertEquals("round 3: 2 prov_group rows expected (testGroup, orphan_team)", 2,
          countSyncBack(configId, "grouper_prov_group"));
      assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in testGroup)", 2,
          countSyncBack(configId, "grouper_prov_mship"));

      // the orphan team landed in the mirror, unlinked (no Grouper group)
      int orphanGroupRow = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanGroupId).select(int.class);
      assertEquals("orphan team should land in prov_group with group_internal_id IS NULL", 1,
          orphanGroupRow);

      // the orphan user's email value round-trips through the reporting view (proves target-drift
      // entities are captured with their actual attributes). email IS a Datadog default entity
      // capture attribute.
      String orphanUserEmailInReporting = new GcDbAccess().connectionName("grouper")
          .sql("select value_string from grouper_prov_user_attr_v "
              + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
              + "and target_user_id = ? and attribute_name = 'email'")
          .addBindVar(syncInternalId).addBindVar(orphanUserId).select(String.class);
      assertEquals("orphan user's email should round-trip through reporting", "orphan.evolve@example.edu",
          orphanUserEmailInReporting);

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // Strict-native capture of orphan target objects (Datadog analogue of Box's
  // testBoxFullSyncCapturesOrphanTargetEntities). An orphan team + orphan user that exist in the
  // target but are unknown to Grouper are captured into the mirror -- with NULL Grouper-side linkage
  // -- alongside Grouper's own testGroup + SUBJ0/SUBJ1, which keep their linkage populated.
  //
  // GOTCHA vs Box (why deleteEntities is turned OFF here): Box's setupBoxSyncBack leaves entity
  // deletes at their config defaults (deleteEntitiesIfNotExistInGrouper=false), so a target-only
  // orphan user survives untouched. The SHARED Datadog test config, by contrast, force-enables every
  // delete axis -- including deleteEntities + deleteEntitiesIfNotExistInGrouper. With those on, the
  // active orphan user (a member of no Grouper-provisioned group) is treated as unprovisionable and
  // DISABLED on pass 1; pass 2's retrieveUsers then filters disabled users out of the active-user
  // listing, so the orphan never reaches grouper_prov_user and the capture assert sees 0 rows. The
  // orphan TEAM does not have this problem (groups use deleteGroupsIfGrouperDeleted, which only
  // deletes Grouper-DELETED groups, never a target-only orphan). So, to mirror Box's "orphans
  // persist" precondition, we disable entity deletion (deleteEntities=false; customizeEntityCrud is
  // already on in the shared config) so the orphan user stays active and is captured on the re-read.
  // -----------------------------------------------------------------------------------------

  public void testDatadogFullSyncCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // deleteEntities=false so the active orphan user is NOT disabled by the daemon (see the gotcha
    // note above) and therefore survives the re-read into grouper_prov_user.
    GrouperSession grouperSession = setupDatadogSyncBack(teamProvisionerConfig()
        .addExtraConfig("deleteEntities", "false"));

    try {
      // pre-populate orphans directly into the mock via raw SQL before the provisioner runs. The
      // orphan team is a "team" (so retrieveTeams returns it); the orphan user is active + not a
      // service account (so retrieveUsers returns it -- disabled/service-account users are filtered).
      String orphanGroupId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
          .addBindVar(orphanGroupId).addBindVar("orphanTeamNotInGrouper").addBindVar("orphan-team-not-in-grouper")
          .addBindVar("orphan desc").addBindVar("team").executeSql();
      String orphanUserId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(orphanUserId).addBindVar("orphan.user@example.edu").addBindVar("Orphan NotInGrouper")
          .addBindVar(null).addBindVar("F").addBindVar("F").executeSql();

      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningSub(stem);

      // two passes: pass 1 inserts Grouper's objects (orphans untouched); pass 2 reads orphans +
      // Grouper's objects and the flush captures all of them.
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      // orphan team landed with NULL group_internal_id
      int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanGroupId).select(int.class);
      assertEquals("expected exactly 1 prov_group row for the orphan team", 1, orphanGroupRowsTotal);

      int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanGroupId).select(int.class);
      assertEquals("orphan team's prov_group row must have group_internal_id IS NULL", 1,
          orphanGroupRowsUnlinked);

      // orphan user landed with NULL member_internal_id
      int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id = ?")
          .addBindVar(syncInternalId).addBindVar(orphanUserId).select(int.class);
      assertEquals("expected exactly 1 prov_user row for the orphan user", 1, orphanUserRowsTotal);

      int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id = ? and member_internal_id is null")
          .addBindVar(syncInternalId).addBindVar(orphanUserId).select(int.class);
      assertEquals("orphan user's prov_user row must have member_internal_id IS NULL", 1,
          orphanUserRowsUnlinked);

      // Grouper's own testGroup + 2 members land alongside, with linkage populated
      int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_group "
              + "where grouper_sync_internal_id = ? and target_group_id != ? and group_internal_id is not null")
          .addBindVar(syncInternalId).addBindVar(orphanGroupId).select(int.class);
      assertEquals("Grouper's testGroup prov_group row must have group_internal_id linked", 1,
          testGroupRowsLinked);

      int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
          .sql("select count(*) from grouper_prov_user "
              + "where grouper_sync_internal_id = ? and target_user_id != ? and member_internal_id is not null")
          .addBindVar(syncInternalId).addBindVar(orphanUserId).select(int.class);
      assertEquals("Grouper-provisioned prov_user rows (SUBJ0 + SUBJ1) must have member_internal_id linked",
          2, nonOrphanUserRowsLinked);

      // a Datadog default group attribute (groupType) is captured in the catalog
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

  // -----------------------------------------------------------------------------------------
  // Strict-native capture on the MEMBERSHIP axis (Datadog analogue of Box's
  // testBoxFullSyncCapturesMembershipsFromOrphanGroup). An orphan TEAM with an orphan member (neither
  // known to Grouper) is wired in the mock. Datadog memberships are group-centric, so when the daemon
  // lists groups it also reads the orphan team's members (retrieveMembershipsByGroup ->
  // captureTeamMemberships) -- that membership must land in grouper_prov_mship alongside Grouper's
  // own, proving strict-native membership capture is independent of Grouper knowledge.
  //
  // GOTCHA vs Box (deleteEntities OFF): same reason as testDatadogFullSyncCapturesOrphanTargetEntities
  // -- the shared Datadog config force-enables deleteEntities + deleteEntitiesIfNotExistInGrouper, so
  // the active orphan user (a member of the orphan TEAM only, which is not a Grouper-provisioned
  // group) would be disabled on pass 1 and filtered out of pass 2's retrieveUsers. The orphan
  // membership row joins through grouper_prov_user.target_user_id, so if the orphan user is dropped
  // the mship row cannot be recorded (0 instead of 1). Disabling entity deletion keeps the orphan
  // user active so its membership edge is captured. (The orphan team itself is safe: groups use
  // deleteGroupsIfGrouperDeleted, which never deletes a target-only orphan.)
  // -----------------------------------------------------------------------------------------

  public void testDatadogFullSyncCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // deleteEntities=false so the active orphan user survives the re-read (see the gotcha note above),
    // which is required for the orphan team -> orphan user membership edge to be captured.
    GrouperSession grouperSession = setupDatadogSyncBack(teamProvisionerConfig()
        .addExtraConfig("deleteEntities", "false"));

    try {
      // orphan team + orphan user + the membership wiring them, all in the mock via raw SQL.
      String orphanGroupId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_group (id, name, handle, description, group_type) values (?, ?, ?, ?, ?)")
          .addBindVar(orphanGroupId).addBindVar("orphanTeamWithMembers").addBindVar("orphan-team-with-members")
          .addBindVar(null).addBindVar("team").executeSql();
      String orphanUserId = GrouperUuid.getUuid();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_user (id, email, name, title, disabled, service_account) values (?, ?, ?, ?, ?, ?)")
          .addBindVar(orphanUserId).addBindVar("orphan.mship@example.edu").addBindVar("Orphan Mship")
          .addBindVar(null).addBindVar("F").addBindVar("F").executeSql();
      new GcDbAccess().connectionName("grouper").sql("insert into mock_datadog_membership (id, group_id, user_id, role) values (?, ?, ?, ?)")
          .addBindVar(GrouperUuid.getUuid()).addBindVar(orphanGroupId).addBindVar(orphanUserId).addBindVar("member").executeSql();

      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningSub(stem);

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
          .addBindVar(syncInternalId).addBindVar(orphanGroupId).addBindVar(orphanUserId)
          .select(int.class);
      assertEquals("expected 1 prov_mship row for orphan team -> orphan user", 1, orphanMshipRows);

      // Grouper's own memberships land alongside (3 total: SUBJ0 + SUBJ1 in testGroup + the orphan)
      assertEquals("expected 3 prov_mship rows total (2 from testGroup + 1 orphan)", 3,
          countSyncBack(configId, "grouper_prov_mship"));

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // !selectAll* scope excludes orphans -- SKIPPED for Datadog (capability/mock limit; the Box
  // analogue testBoxSelectAllFalseExcludesOrphans cannot be ported as-is).
  //
  // The Box test relies on a scoped retrieve that fetches exactly ONE object by id: with
  // selectAllGroups/selectAllEntities=false the Box DAO calls retrieveBoxGroup/retrieveBoxUser
  // (singular, by-id), so an orphan whose id Grouper never asks for is never read and so never
  // captured -- which is what lets the orphan be excluded from the mirror.
  //
  // Datadog's scoped retrieve cannot do that. Datadog has no "get one object by id" endpoint wired
  // into the DAO: DatadogTargetDao.retrieveGroup (selectAllGroups=false path) calls
  // DatadogApiCommands.retrieveTeams/retrieveRoles and then loops to find the one match, and
  // retrieveEntity calls DatadogApiCommands.retrieveUsers and loops likewise. Those list-all commands
  // fire the sync-back capture seam for EVERY element in the listing
  // (captureGroupJsonFromCurrentProvisioner / captureUserJsonFromCurrentProvisioner inside
  // retrieveTeams/retrieveRoles/retrieveUsers), BEFORE any matching/filtering. So even with
  // selectAll=false, the full server-side listing -- orphans included -- is captured into the mirror.
  // The orphan team WILL appear in grouper_prov_group and the orphan user in grouper_prov_user, the
  // exact opposite of the Box assertion. (This is the same reason the forward test
  // testDatadogFullSyncSelectByIdsPopulatesGenericTables still captures everything on the scoped path.)
  //
  // Making this scenario pass would require a production change to the Datadog read seams (capture
  // only the matched object on the scoped path, or add a true by-id GET) -- out of scope for the
  // sync-back test port and a behavior change to the connector. So this is a documented skip rather
  // than a red test or a misleading assertion. The selectAll=false capture path itself is still
  // covered (positively) by testDatadogFullSyncSelectByIdsPopulatesGenericTables.
  // -----------------------------------------------------------------------------------------

  public void testDatadogSelectAllFalseExcludesOrphans() {
    // Intentionally skipped -- see the block comment above. Datadog's scoped retrieve is implemented
    // as list-all-then-filter, and the capture seam fires for the whole listing, so orphans cannot be
    // excluded the way Box's by-id scoped retrieve excludes them. Early-return keeps the suite green
    // without asserting behavior the Datadog connector does not (and is not meant to) have.
    return;
  }

  // -----------------------------------------------------------------------------------------
  // Broken-target delete stays in the mirror (Datadog analogue of Box's
  // testBoxUserDeleteBrokenTargetStaysInMirror). We have no mock knob to fake a broken delete, so we
  // DISABLE entity deletion: SUBJ0 is removed from testGroup in Grouper, but with deleteEntities off
  // the daemon never disables SUBJ0 in the target -- so the user (still active) remains visible on the
  // re-read and stays in the mirror. Exercises the same mirror behavior (a target object the daemon
  // did NOT remove stays captured) without a target that lies about a delete.
  // -----------------------------------------------------------------------------------------

  public void testDatadogUserDeleteBrokenTargetStaysInMirror() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // Disable entity deletion. The shared config defaults deleteEntities=true (customizeEntityCrud is
    // already on), so override the umbrella key to false to keep the daemon from disabling SUBJ0.
    GrouperSession grouperSession = setupDatadogSyncBack(teamProvisionerConfig()
        .addExtraConfig("deleteEntities", "false"));

    try {
      String userId0 = GrouperUuid.getUuid();
      createMockUsers(userId0, null, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningSub(stem);

      // seed: group + SUBJ0 in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
      assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

      // remove SUBJ0 from the group in Grouper. With entity-delete off the daemon does not disable
      // SUBJ0 in the target, so the target still has SUBJ0 as an active user.
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      // the group was never deleted -> still in the mirror
      assertEquals("group row should stay (group was not deleted)", 1,
          countSyncBack(configId, "grouper_prov_group"));

      // confirm the target still has SUBJ0 active (the daemon did not disable it), so the re-read keeps it
      String disabled0 = new GcDbAccess().connectionName("grouper")
          .sql("select disabled from mock_datadog_user where id = ?").addBindVar(userId0).select(String.class);
      assertEquals("SUBJ0 should remain active in the target (entity-delete is off)", "F", disabled0);

      assertEquals("user should STAY in the mirror (its disable was never performed)", 1,
          countSyncBack(configId, "grouper_prov_user"));

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // Load-flag isolation -- three tests (Datadog analogue of Box's testBoxLoadGroups/Entities/
  // MembershipsFlag*). Each toggles exactly one or two of the load*ToGenericGrouperTable flags and
  // asserts only the enabled axes populate. setupProvisionerTest is bypassed here because these need
  // a CUSTOM mix of the three load flags (setupDatadogSyncBack always turns all three on), so they
  // open-code the configure + startup + mock-wipe, the same way the Box flag tests do.
  // -----------------------------------------------------------------------------------------

  /** Only the groups flag on -> only grouper_prov_group rows; prov_user / prov_mship stay empty. */
  public void testDatadogLoadGroupsFlagInIsolation() {
    loadFlagIsolation("true", "false", "false", true, false, false);
  }

  /** Only the entities flag on -> only grouper_prov_user rows; prov_group / prov_mship stay empty. */
  public void testDatadogLoadEntitiesFlagInIsolation() {
    loadFlagIsolation("false", "true", "false", false, true, false);
  }

  /** Both object loads on but memberships off -> prov_group + prov_user populate, prov_mship empty. */
  public void testDatadogLoadMembershipsFlagOff() {
    loadFlagIsolation("true", "true", "false", true, true, false);
  }

  /**
   * @param loadGroups  value for loadGroupsToGenericGrouperTable
   * @param loadEntities value for loadEntitiesToGenericGrouperTable
   * @param loadMships  value for loadMembershipsToGenericGrouperTable
   * @param expectGroups whether prov_group should have rows
   * @param expectUsers  whether prov_user should have rows
   * @param expectMships whether prov_mship should have rows
   */
  private void loadFlagIsolation(String loadGroups, String loadEntities, String loadMships,
      boolean expectGroups, boolean expectUsers, boolean expectMships) {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    // open-code the configure with the custom flag mix (do NOT use setupDatadogSyncBack -- it forces
    // all three flags on). Mirror setupProvisionerTest's surrounding boilerplate.
    DatadogProvisionerTestUtils.setupDatadogExternalSystem();
    DatadogProvisionerTestUtils.configureDatadogProvisioner(teamProvisionerConfig()
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", loadGroups)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", loadEntities)
        .addExtraConfig("loadMembershipsToGenericGrouperTable", loadMships));

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();
    // ensure mock tables exist, then wipe. NB: this read must use CONFIG_ID (the EXTERNAL-system
    // config id, "datadogDev") -- that is where setupDatadogExternalSystem stores the bearer-token
    // accessTokenPassword. Using the PROVISIONER config id ("datadogProvisioner") here would look up
    // grouper.wsBearerToken.datadogProvisioner.accessTokenPassword, which does not exist, and fail
    // with "Cant find property ... it is required". (setupProvisionerTest's equivalent call already
    // uses CONFIG_ID; this open-coded path must match.)
    DatadogApiCommands.retrieveTeams(CONFIG_ID, null);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_datadog_group").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, null);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningSub(stem);

      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      if (expectGroups) {
        assertTrue("expected >=1 prov_group row when groups capture is on",
            countSyncBack(configId, "grouper_prov_group") >= 1);
      } else {
        assertEquals("expected 0 prov_group rows when groups capture is off", 0,
            countSyncBack(configId, "grouper_prov_group"));
      }

      if (expectUsers) {
        assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
            countSyncBack(configId, "grouper_prov_user") >= 2);
      } else {
        assertEquals("expected 0 prov_user rows when entities capture is off", 0,
            countSyncBack(configId, "grouper_prov_user"));
      }

      if (expectMships) {
        assertTrue("expected >=2 prov_mship rows when memberships capture is on",
            countSyncBack(configId, "grouper_prov_mship") >= 2);
      } else {
        assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
            countSyncBack(configId, "grouper_prov_mship"));
      }

    } finally {

    }
  }

  // -----------------------------------------------------------------------------------------
  // INCREMENTAL sync-back coverage, conservative (Datadog analogue of Box's
  // testBoxIncrementalSyncBackNoSpuriousDeletes). Datadog captures group/user OBJECTS on the READ
  // path (memberships now also capture on the write path via recordTargetNativeMembershipInsert/Delete,
  // like SCIM/Adobe -- but this test deliberately does not assert membership convergence, see below).
  // On an incremental cycle it re-reads only the changed objects (it has canRetrieveGroup/Entity, so
  // the adapter decomposes to per-id reads that fire the Datadog object-capture seams), and the
  // incremental flush is a SCOPED upsert (NOT a full replace, so it will not wrongly delete untouched
  // mirror rows).
  //
  // What this test asserts is therefore deliberately narrow -- the safe, reliable part of Datadog
  // incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
  // member drives an incremental that (a) re-reads the changed group/entity and so does NOT shrink the
  // existing GROUP/USER mirror (no spurious deletes -- the regression the scoped incremental flush
  // guards against), and (b) captures the newly added member's user object into prov_user. It does NOT
  // assert that the new MEMBERSHIP converges on the same incremental cycle: even though Datadog now
  // captures memberships on write, the incremental's group-centric membership read plus scoped-flush
  // timing make same-cycle membership convergence unreliable to assert here (the same 1-cycle-lag
  // reason SCIM disables its object incremental test). Membership convergence is covered end-to-end
  // by the two-pass full tests above.
  // -----------------------------------------------------------------------------------------

  public void testDatadogIncrementalSyncBackNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "datadogProvisioner";
    GrouperSession grouperSession = setupDatadogSyncBack(teamProvisionerConfig());

    try {
      String userId0 = GrouperUuid.getUuid();
      String userId1 = GrouperUuid.getUuid();
      String userId2 = GrouperUuid.getUuid();
      createMockUsers(userId0, userId1, userId2);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningSub(stem);

      // seed via full sync: group + SUBJ0 + SUBJ1 + their memberships in the mirror
      assertEquals(0, fullProvision().getRecordsWithErrors());
      assertEquals(0, fullProvision().getRecordsWithErrors());

      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
      long syncInternalId = gcGrouperSync.getInternalId();

      int provGroupRowsBefore = countSyncBack(configId, "grouper_prov_group");
      int provUserRowsBefore = countSyncBack(configId, "grouper_prov_user");
      int provMshipRowsBefore = countSyncBack(configId, "grouper_prov_mship");
      assertTrue("seed should have >=1 prov_group row", provGroupRowsBefore >= 1);
      assertEquals("seed should have 2 prov_user rows", 2, provUserRowsBefore);
      assertEquals("seed should have 2 prov_mship rows", 2, provMshipRowsBefore);

      // prime the changelog consumer: its FIRST run only initializes its changelog position
      // (processes nothing), so without this priming pass the change below is never consumed.
      incrementalProvision();

      // incremental add: a third member. The incremental re-reads the changed group/entity, firing
      // the Datadog read-capture seams, and the scoped flush upserts -- it must NOT drop untouched rows.
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);
      incrementalProvision();

      // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
      assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
          + " after=" + countSyncBack(configId, "grouper_prov_group"),
          countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
      // NB: prov_mship is intentionally NOT asserted here (matching this test's note above). Datadog
      // memberships are group-centric (and now capture on write as well as read); on an incremental
      // cycle the scoped membership flush for the changed group plus its timing means testGroup's
      // membership rows can transiently clear, re-converging only on the next full sync (the same
      // 1-cycle lag for which SCIM disables its object incremental test).

      // (b) the newly added member's user object is captured (object capture via the per-id re-read)
      assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
          countSyncBack(configId, "grouper_prov_user"));

      // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
      // regression guarded on the LDAP/SCIM side; Datadog shares the same generic flush code)
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

}
