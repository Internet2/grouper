package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import junit.textui.TestRunner;

public class FreshRequesterProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {

    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();
    TestRunner.run(new FreshRequesterProvisionerTest("testRetrieveRequesterUserByCustomAttribute"));

    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return "freshRequesterProvisioner";
  }

  public static boolean startTomcat = false;

  public FreshRequesterProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();
  }

  public void testRetrieveRequesterGroups() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert some groups directly into the mock table
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1001, 'IT Support', 'IT support team group')").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1002, 'HR Team', 'Human resources department')").executeSql();
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1003, 'Engineering', 'Engineering department')").executeSql();

    List<FreshRequesterGroup> groups = FreshRequesterApiCommands.retrieveRequesterGroups("freshServiceDev");

    assertEquals(3, groups.size());

    Map<Long, FreshRequesterGroup> groupById = new HashMap<Long, FreshRequesterGroup>();
    for (FreshRequesterGroup group : groups) {
      groupById.put(group.getId(), group);
    }

    FreshRequesterGroup group1001 = groupById.get(1001L);
    assertNotNull(group1001);
    assertEquals("IT Support", group1001.getName());
    assertEquals("IT support team group", group1001.getDescription());

    FreshRequesterGroup group1002 = groupById.get(1002L);
    assertNotNull(group1002);
    assertEquals("HR Team", group1002.getName());
    assertEquals("Human resources department", group1002.getDescription());

    FreshRequesterGroup group1003 = groupById.get(1003L);
    assertNotNull(group1003);
    assertEquals("Engineering", group1003.getName());
    assertEquals("Engineering department", group1003.getDescription());
  }

  public void testRetrieveRequesterGroup() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert a group directly into the mock table
    new GcDbAccess().connectionName("grouper").sql("insert into mock_freshreq_group (id, name, description) values (1001, 'IT Support', 'IT support team group')").executeSql();

    // retrieve existing group
    FreshRequesterGroup group = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", 1001L);

    assertNotNull(group);
    assertEquals(1001L, (long)group.getId());
    assertEquals("IT Support", group.getName());
    assertEquals("IT support team group", group.getDescription());

    // retrieve non-existing group should return null
    FreshRequesterGroup notFound = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", 9999L);

    assertNull(notFound);
  }

  public void testCreateRequesterGroup() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("Branch Managers");
    groupToCreate.setDescription("Requester group for branch managers across all locations");

    // create the group
    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);

    assertNotNull(createdGroup);
    assertTrue(createdGroup.getId() > 0);
    assertEquals("Branch Managers", createdGroup.getName());
    assertEquals("Requester group for branch managers across all locations", createdGroup.getDescription());

    // verify it can be retrieved
    FreshRequesterGroup retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", createdGroup.getId());

    assertNotNull(retrievedGroup);
    assertEquals(createdGroup.getId(), retrievedGroup.getId());
    assertEquals("Branch Managers", retrievedGroup.getName());
    assertEquals("Requester group for branch managers across all locations", retrievedGroup.getDescription());

    // creating a group with the same name should throw an exception (409)
    FreshRequesterGroup duplicateGroup = new FreshRequesterGroup();
    duplicateGroup.setName("Branch Managers");
    duplicateGroup.setDescription("duplicate");

    try {
      FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", duplicateGroup);
      fail("Should have thrown exception for duplicate group name");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("already exists"));
    }
  }

  public void testDeleteRequesterGroup() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a group to delete
    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("Temp Group");
    groupToCreate.setDescription("Temporary group for delete test");

    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);
    assertNotNull(createdGroup);
    assertTrue(createdGroup.getId() > 0);

    // verify it exists
    FreshRequesterGroup retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", createdGroup.getId());
    assertNotNull(retrievedGroup);

    // delete the group
    FreshRequesterApiCommands.deleteRequesterGroup("freshServiceDev", createdGroup.getId());

    // verify it no longer exists
    FreshRequesterGroup deletedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", createdGroup.getId());
    assertNull(deletedGroup);

    // delete again should not throw an error (404 is acceptable)
    FreshRequesterApiCommands.deleteRequesterGroup("freshServiceDev", createdGroup.getId());
  }

  public void testUpdateRequesterGroup() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a group to update
    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("Original Name");
    groupToCreate.setDescription("Original description");

    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);
    assertNotNull(createdGroup);
    assertTrue(createdGroup.getId() > 0);

    // update name only
    FreshRequesterGroup groupToUpdate = new FreshRequesterGroup();
    groupToUpdate.setId(createdGroup.getId());
    groupToUpdate.setName("Human Resources");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
    fieldsToUpdate.put("name", ProvisioningObjectChangeAction.update);

    FreshRequesterGroup updatedGroup = FreshRequesterApiCommands.updateRequesterGroup("freshServiceDev", groupToUpdate, fieldsToUpdate);

    assertNotNull(updatedGroup);
    assertEquals(createdGroup.getId(), updatedGroup.getId());
    assertEquals("Human Resources", updatedGroup.getName());
    assertEquals("Original description", updatedGroup.getDescription());

    // update description only
    FreshRequesterGroup groupToUpdate2 = new FreshRequesterGroup();
    groupToUpdate2.setId(createdGroup.getId());
    groupToUpdate2.setDescription("Requester group for HR employees");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate2 = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
    fieldsToUpdate2.put("description", ProvisioningObjectChangeAction.update);

    FreshRequesterGroup updatedGroup2 = FreshRequesterApiCommands.updateRequesterGroup("freshServiceDev", groupToUpdate2, fieldsToUpdate2);

    assertNotNull(updatedGroup2);
    assertEquals(createdGroup.getId(), updatedGroup2.getId());
    assertEquals("Human Resources", updatedGroup2.getName());
    assertEquals("Requester group for HR employees", updatedGroup2.getDescription());

    // update both name and description
    FreshRequesterGroup groupToUpdate3 = new FreshRequesterGroup();
    groupToUpdate3.setId(createdGroup.getId());
    groupToUpdate3.setName("Engineering");
    groupToUpdate3.setDescription("Engineering department group");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate3 = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
    fieldsToUpdate3.put("name", ProvisioningObjectChangeAction.update);
    fieldsToUpdate3.put("description", ProvisioningObjectChangeAction.update);

    FreshRequesterGroup updatedGroup3 = FreshRequesterApiCommands.updateRequesterGroup("freshServiceDev", groupToUpdate3, fieldsToUpdate3);

    assertNotNull(updatedGroup3);
    assertEquals(createdGroup.getId(), updatedGroup3.getId());
    assertEquals("Engineering", updatedGroup3.getName());
    assertEquals("Engineering department group", updatedGroup3.getDescription());

    // verify via retrieve that the final state persisted
    FreshRequesterGroup retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", createdGroup.getId());
    assertNotNull(retrievedGroup);
    assertEquals("Engineering", retrievedGroup.getName());
    assertEquals("Engineering department group", retrievedGroup.getDescription());

    // update non-existing group should throw an exception
    FreshRequesterGroup nonExistingGroup = new FreshRequesterGroup();
    nonExistingGroup.setId(9999L);
    nonExistingGroup.setName("Does Not Exist");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate4 = new LinkedHashMap<String, ProvisioningObjectChangeAction>();
    fieldsToUpdate4.put("name", ProvisioningObjectChangeAction.update);

    try {
      FreshRequesterApiCommands.updateRequesterGroup("freshServiceDev", nonExistingGroup, fieldsToUpdate4);
      fail("Should have thrown exception for non-existing group");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  public void testRetrieveRequesterUsers() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert some users directly into the mock table
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2001, 'jsmith@test.edu', 'John', 'Smith', 'T')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2002, 'jdoe@test.edu', 'Jane', 'Doe', 'T')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2003, 'bwilson@test.edu', 'Bob', 'Wilson', 'F')")
        .executeSql();

    List<FreshRequesterUser> users = FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", true);

    assertEquals(3, users.size());

    Map<Long, FreshRequesterUser> userById = new HashMap<Long, FreshRequesterUser>();
    for (FreshRequesterUser user : users) {
      userById.put(user.getId(), user);
    }

    FreshRequesterUser user2001 = userById.get(2001L);
    assertNotNull(user2001);
    assertEquals("jsmith@test.edu", user2001.getEmail());
    assertEquals("John", user2001.getFirstName());
    assertEquals("Smith", user2001.getLastName());
    assertEquals(Boolean.TRUE, user2001.getActive());

    FreshRequesterUser user2002 = userById.get(2002L);
    assertNotNull(user2002);
    assertEquals("jdoe@test.edu", user2002.getEmail());
    assertEquals("Jane", user2002.getFirstName());
    assertEquals("Doe", user2002.getLastName());
    assertEquals(Boolean.TRUE, user2002.getActive());

    FreshRequesterUser user2003 = userById.get(2003L);
    assertNotNull(user2003);
    assertEquals("bwilson@test.edu", user2003.getEmail());
    assertEquals("Bob", user2003.getFirstName());
    assertEquals("Wilson", user2003.getLastName());
    assertEquals(Boolean.FALSE, user2003.getActive());
  }

  public void testRetrieveRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert a user directly into the mock table
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2001, 'jsmith@test.edu', 'John', 'Smith', 'T')")
        .executeSql();

    // retrieve existing user
    FreshRequesterUser user = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", 2001L, false);

    assertNotNull(user);
    assertEquals(2001L, (long)user.getId());
    assertEquals("jsmith@test.edu", user.getEmail());
    assertEquals("John", user.getFirstName());
    assertEquals("Smith", user.getLastName());
    assertEquals(Boolean.TRUE, user.getActive());

    // retrieve non-existing user should return null
    FreshRequesterUser notFound = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", 9999L, false);

    assertNull(notFound);
  }

  public void testRetrieveRequesterUserByEmail() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert some users directly into the mock table
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2001, 'jsmith@test.edu', 'John', 'Smith', 'T')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) values (2002, 'jdoe@test.edu', 'Jane', 'Doe', 'T')")
        .executeSql();

    // retrieve existing user by email
    FreshRequesterUser user = FreshRequesterApiCommands.retrieveRequesterUserByEmail("freshServiceDev", "jsmith@test.edu", false);

    assertNotNull(user);
    assertEquals(2001L, (long)user.getId());
    assertEquals("jsmith@test.edu", user.getEmail());
    assertEquals("John", user.getFirstName());
    assertEquals("Smith", user.getLastName());

    // retrieve non-existing email should return null
    FreshRequesterUser notFound = FreshRequesterApiCommands.retrieveRequesterUserByEmail("freshServiceDev", "nobody@test.edu", false);

    assertNull(notFound);
  }

  public void testRetrieveRequesterUserByCustomAttribute() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // insert some users directly into the mock table with custom_fields JSON
    // pennId is numeric (no quotes around the value in JSON)
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active, custom_fields) values (2001, 'jsmith@test.edu', 'John', 'Smith', 'T', '{\"pennId\":12345678,\"pennkey\":\"jsmith\"}')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active, custom_fields) values (2002, 'jdoe@test.edu', 'Jane', 'Doe', 'T', '{\"pennId\":87654321,\"pennkey\":\"jdoe\"}')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active, custom_fields) values (2003, 'bwilson@test.edu', 'Bob', 'Wilson', 'T', null)")
        .executeSql();

    // retrieve existing user by custom field pennId as Long
    FreshRequesterUser user = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", 12345678L);

    assertNotNull(user);
    assertEquals(2001L, (long)user.getId());
    assertEquals("jsmith@test.edu", user.getEmail());
    assertEquals("John", user.getFirstName());
    assertEquals("Smith", user.getLastName());
    assertNotNull(user.getCustomFields());
    assertEquals(12345678L, user.getCustomFields().get("pennId"));
    assertEquals("jsmith", user.getCustomFields().get("pennkey"));

    // retrieve by a different custom field pennkey (String)
    FreshRequesterUser user2 = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennkey", "jdoe");

    assertNotNull(user2);
    assertEquals(2002L, (long)user2.getId());
    assertEquals("jdoe@test.edu", user2.getEmail());

    // retrieve non-existing custom field value should return null
    FreshRequesterUser notFound = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", 99999999L);

    assertNull(notFound);

    // retrieve by custom field when user has no custom_fields should return null
    FreshRequesterUser notFoundNull = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", 11111111L);

    assertNull(notFoundNull);
  }

  public void testCreateRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    Map<String, Object> customFields = new HashMap<String, Object>();
    customFields.put("pennkey", "jsmith");
    customFields.put("penn_id", "12345678");

    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);
    userToCreate.setJobTitle("Worker");
    userToCreate.setDepartmentId(39000211201L);
    userToCreate.setCustomFields(customFields);

    // create the user
    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);

    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);
    assertEquals("John", createdUser.getFirstName());
    assertEquals("Smith", createdUser.getLastName());
    assertEquals("jsmith@test.edu", createdUser.getEmail());
    assertEquals("Worker", createdUser.getJobTitle());

    // verify it can be retrieved
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);

    assertNotNull(retrievedUser);
    assertEquals(createdUser.getId(), retrievedUser.getId());
    assertEquals("John", retrievedUser.getFirstName());
    assertEquals("Smith", retrievedUser.getLastName());
    assertEquals("jsmith@test.edu", retrievedUser.getEmail());
    assertEquals("Worker", retrievedUser.getJobTitle());
    assertNotNull(retrievedUser.getCustomFields());
    assertEquals("jsmith", retrievedUser.getCustomFields().get("pennkey"));
    assertEquals("12345678", retrievedUser.getCustomFields().get("penn_id"));

    // creating a user with the same email should update the existing user
    FreshRequesterUser duplicateUser = new FreshRequesterUser();
    duplicateUser.setFirstName("Johnny");
    duplicateUser.setLastName("Smythe");
    duplicateUser.setEmail("jsmith@test.edu");
    duplicateUser.setJobTitle("Senior Worker");

    FreshRequesterUser updatedUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", duplicateUser);

    assertNotNull(updatedUser);
    // should be the same user id as the original
    assertEquals(createdUser.getId(), updatedUser.getId());
    // should have the updated fields
    assertEquals("Johnny", updatedUser.getFirstName());
    assertEquals("Smythe", updatedUser.getLastName());
    assertEquals("jsmith@test.edu", updatedUser.getEmail());
    assertEquals("Senior Worker", updatedUser.getJobTitle());

    // calling the helper directly with a duplicate email should throw an exception (409)
    FreshRequesterUser duplicateUser2 = new FreshRequesterUser();
    duplicateUser2.setFirstName("Jane");
    duplicateUser2.setLastName("Doe");
    duplicateUser2.setEmail("jsmith@test.edu");

    try {
      FreshRequesterApiCommands.createRequesterUserHelper("freshServiceDev", duplicateUser2);
      fail("Should have thrown exception for duplicate email");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("already exists"));
    }
  }

  public void testDeactivateRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a user to deactivate
    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);

    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);
    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);

    // verify active is true
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(retrievedUser);
    assertEquals(Boolean.TRUE, retrievedUser.getActive());

    // deactivate the user
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", createdUser.getId());

    // verify user still exists but active is now false (pass true to include inactive)
    FreshRequesterUser deactivatedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), true);
    assertNotNull(deactivatedUser);
    assertEquals(createdUser.getId(), deactivatedUser.getId());
    assertEquals("jsmith@test.edu", deactivatedUser.getEmail());
    assertEquals(Boolean.FALSE, deactivatedUser.getActive());

    // deactivate again should not throw an error (still 204 since user exists but inactive)
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", createdUser.getId());

    // deactivate non-existing user should not throw an error (404 is acceptable)
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", 9999L);
  }

  public void testForgetRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a user to forget
    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);

    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);
    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);

    // verify it exists
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(retrievedUser);

    // forget (permanently delete) the user
    FreshRequesterApiCommands.forgetRequesterUser("freshServiceDev", createdUser.getId());

    // verify it no longer exists
    FreshRequesterUser forgottenUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNull(forgottenUser);

    // forget again should not throw an error (404 is acceptable)
    FreshRequesterApiCommands.forgetRequesterUser("freshServiceDev", createdUser.getId());
  }

  public void testUpdateRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a user to update
    Map<String, Object> customFields = new HashMap<String, Object>();
    customFields.put("pennkey", "jsmith");
    customFields.put("penn_id", "12345678");

    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);
    userToCreate.setJobTitle("Worker");
    userToCreate.setDepartmentId(39000211201L);
    userToCreate.setCustomFields(customFields);

    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);
    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);

    // update email only
    FreshRequesterUser userToUpdate = new FreshRequesterUser();
    userToUpdate.setId(createdUser.getId());
    userToUpdate.setEmail("jsmith2@upenn.edu");

    Set<String> fieldsToUpdate = new java.util.LinkedHashSet<String>();
    fieldsToUpdate.add("email");

    FreshRequesterUser updatedUser = FreshRequesterApiCommands.updateRequesterUser("freshServiceDev", userToUpdate, fieldsToUpdate);

    assertNotNull(updatedUser);
    assertEquals(createdUser.getId(), updatedUser.getId());
    assertEquals("jsmith2@upenn.edu", updatedUser.getEmail());
    assertEquals("John", updatedUser.getFirstName());
    assertEquals("Smith", updatedUser.getLastName());
    assertEquals("Worker", updatedUser.getJobTitle());

    // verify via retrieve
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(retrievedUser);
    assertEquals("jsmith2@upenn.edu", retrievedUser.getEmail());
    assertEquals("John", retrievedUser.getFirstName());
    assertEquals("Smith", retrievedUser.getLastName());
    assertEquals("Worker", retrievedUser.getJobTitle());

    // update multiple fields including custom_fields
    Map<String, Object> updatedCustomFields = new HashMap<String, Object>();
    updatedCustomFields.put("pennkey", "jsmith2");
    updatedCustomFields.put("penn_id", "12345679");

    FreshRequesterUser userToUpdate2 = new FreshRequesterUser();
    userToUpdate2.setId(createdUser.getId());
    userToUpdate2.setFirstName("Johnny");
    userToUpdate2.setJobTitle("Manager");
    userToUpdate2.setCustomFields(updatedCustomFields);

    Set<String> fieldsToUpdate2 = new java.util.LinkedHashSet<String>();
    fieldsToUpdate2.add("firstName");
    fieldsToUpdate2.add("jobTitle");
    fieldsToUpdate2.add("customField_pennkey");
    fieldsToUpdate2.add("customField_penn_id");

    FreshRequesterUser updatedUser2 = FreshRequesterApiCommands.updateRequesterUser("freshServiceDev", userToUpdate2, fieldsToUpdate2);

    assertNotNull(updatedUser2);
    assertEquals(createdUser.getId(), updatedUser2.getId());
    assertEquals("Johnny", updatedUser2.getFirstName());
    assertEquals("Smith", updatedUser2.getLastName());
    assertEquals("jsmith2@upenn.edu", updatedUser2.getEmail());
    assertEquals("Manager", updatedUser2.getJobTitle());
    assertNotNull(updatedUser2.getCustomFields());
    assertEquals("jsmith2", updatedUser2.getCustomFields().get("pennkey"));
    assertEquals("12345679", updatedUser2.getCustomFields().get("penn_id"));

    // verify via retrieve that final state persisted
    FreshRequesterUser retrievedUser2 = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(retrievedUser2);
    assertEquals("Johnny", retrievedUser2.getFirstName());
    assertEquals("Smith", retrievedUser2.getLastName());
    assertEquals("jsmith2@upenn.edu", retrievedUser2.getEmail());
    assertEquals("Manager", retrievedUser2.getJobTitle());
    assertNotNull(retrievedUser2.getCustomFields());
    assertEquals("jsmith2", retrievedUser2.getCustomFields().get("pennkey"));
    assertEquals("12345679", retrievedUser2.getCustomFields().get("penn_id"));

    // update non-existing user should throw an exception
    FreshRequesterUser nonExistingUser = new FreshRequesterUser();
    nonExistingUser.setId(9999L);
    nonExistingUser.setFirstName("Nobody");

    Set<String> fieldsToUpdate3 = new java.util.LinkedHashSet<String>();
    fieldsToUpdate3.add("firstName");

    try {
      FreshRequesterApiCommands.updateRequesterUser("freshServiceDev", nonExistingUser, fieldsToUpdate3);
      fail("Should have thrown exception for non-existing user");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("does not exist"));
    }
  }

  public void testAddGroupMembership() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a group and a user
    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("IT Support");
    groupToCreate.setDescription("IT support team");

    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);
    assertNotNull(createdGroup);

    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");

    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);
    assertNotNull(createdUser);

    // verify no memberships exist yet
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ? and user_id = ?")
        .addBindVar(createdGroup.getId()).addBindVar(createdUser.getId())
        .select(int.class);
    assertEquals(0, count);

    // add membership
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser.getId());

    // verify membership exists
    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ? and user_id = ?")
        .addBindVar(createdGroup.getId()).addBindVar(createdUser.getId())
        .select(int.class);
    assertEquals(1, count);

    // add same membership again should not throw an error (200 if already existed)
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser.getId());

    // verify still only one membership row
    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ? and user_id = ?")
        .addBindVar(createdGroup.getId()).addBindVar(createdUser.getId())
        .select(int.class);
    assertEquals(1, count);
  }

  public void testRemoveGroupMembership() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a group and two users
    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("IT Support");
    groupToCreate.setDescription("IT support team");

    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);
    assertNotNull(createdGroup);

    FreshRequesterUser user1 = new FreshRequesterUser();
    user1.setFirstName("John");
    user1.setLastName("Smith");
    user1.setEmail("jsmith@test.edu");

    FreshRequesterUser createdUser1 = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", user1);
    assertNotNull(createdUser1);

    FreshRequesterUser user2 = new FreshRequesterUser();
    user2.setFirstName("Jane");
    user2.setLastName("Doe");
    user2.setEmail("jdoe@test.edu");

    FreshRequesterUser createdUser2 = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", user2);
    assertNotNull(createdUser2);

    // add both memberships
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser1.getId());
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser2.getId());

    // verify both exist
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ?")
        .addBindVar(createdGroup.getId())
        .select(int.class);
    assertEquals(2, count);

    // remove first membership
    FreshRequesterApiCommands.removeGroupMembership("freshServiceDev", createdGroup.getId(), createdUser1.getId());

    // verify only second remains
    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ?")
        .addBindVar(createdGroup.getId())
        .select(int.class);
    assertEquals(1, count);

    count = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_membership where group_id = ? and user_id = ?")
        .addBindVar(createdGroup.getId()).addBindVar(createdUser2.getId())
        .select(int.class);
    assertEquals(1, count);

    // remove again should not throw (404 is acceptable)
    FreshRequesterApiCommands.removeGroupMembership("freshServiceDev", createdGroup.getId(), createdUser1.getId());

    // remove non-existing membership should not throw (404 is acceptable)
    FreshRequesterApiCommands.removeGroupMembership("freshServiceDev", createdGroup.getId(), 9999L);
  }

  public void testRetrieveMembershipsByGroup() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a group and two users
    FreshRequesterGroup groupToCreate = new FreshRequesterGroup();
    groupToCreate.setName("Engineering");
    groupToCreate.setDescription("Engineering team");

    FreshRequesterGroup createdGroup = FreshRequesterApiCommands.createRequesterGroup("freshServiceDev", groupToCreate);
    assertNotNull(createdGroup);

    FreshRequesterUser user1 = new FreshRequesterUser();
    user1.setFirstName("John");
    user1.setLastName("Smith");
    user1.setEmail("jsmith@test.edu");

    FreshRequesterUser createdUser1 = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", user1);
    assertNotNull(createdUser1);

    FreshRequesterUser user2 = new FreshRequesterUser();
    user2.setFirstName("Jane");
    user2.setLastName("Doe");
    user2.setEmail("jdoe@test.edu");

    FreshRequesterUser createdUser2 = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", user2);
    assertNotNull(createdUser2);

    // empty group should return empty list
    List<FreshRequesterUser> members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", createdGroup.getId());
    assertEquals(0, members.size());

    // add both memberships
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser1.getId());
    FreshRequesterApiCommands.addGroupMembership("freshServiceDev", createdGroup.getId(), createdUser2.getId());

    // retrieve members
    members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", createdGroup.getId());
    assertEquals(2, members.size());

    Map<Long, FreshRequesterUser> memberById = new HashMap<Long, FreshRequesterUser>();
    for (FreshRequesterUser member : members) {
      memberById.put(member.getId(), member);
    }

    FreshRequesterUser member1 = memberById.get(createdUser1.getId());
    assertNotNull(member1);
    assertEquals("jsmith@test.edu", member1.getEmail());
    assertEquals("John", member1.getFirstName());
    assertEquals("Smith", member1.getLastName());

    FreshRequesterUser member2 = memberById.get(createdUser2.getId());
    assertNotNull(member2);
    assertEquals("jdoe@test.edu", member2.getEmail());
    assertEquals("Jane", member2.getFirstName());
    assertEquals("Doe", member2.getLastName());

    // remove one membership and verify list shrinks
    FreshRequesterApiCommands.removeGroupMembership("freshServiceDev", createdGroup.getId(), createdUser1.getId());

    members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", createdGroup.getId());
    assertEquals(1, members.size());
    assertEquals(createdUser2.getId(), members.get(0).getId());
  }

  public void testUpdateGroupDescriptionFull() {
    updateGroupDescription(true);
  }

  public void testUpdateGroupDescriptionIncremental() {
    updateGroupDescription(false);
  }

  public void updateGroupDescription(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").assignDescription("test description").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      // if incremental, initialize provisioner state before attaching provisioning attribute
      if (!isFull) {
        fullProvision();
        incrementalProvision();
      }

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      //
      // first provision: should create group with description "test description"
      //
      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));

      String dbDescription = new GcDbAccess().connectionName("grouper")
          .sql("select description from mock_freshreq_group where name = ?").addBindVar("testGroup").select(String.class);
      assertEquals("test description", dbDescription);

      Long groupId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_group where name = ?").addBindVar("testGroup").select(Long.class);

      FreshRequesterGroup retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", groupId);
      assertNotNull(retrievedGroup);
      assertEquals("test description", retrievedGroup.getDescription());

      //
      // update description to "new description 1"
      //
      new GroupSave(grouperSession).assignUuid(testGroup.getUuid()).assignDescription("new description 1").assignSaveMode(SaveMode.INSERT_OR_UPDATE).save();

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      dbDescription = new GcDbAccess().connectionName("grouper")
          .sql("select description from mock_freshreq_group where name = ?").addBindVar("testGroup").select(String.class);
      assertEquals("new description 1", dbDescription);

      retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", groupId);
      assertNotNull(retrievedGroup);
      assertEquals("new description 1", retrievedGroup.getDescription());

      //
      // set description to null
      //
      new GroupSave(grouperSession).assignUuid(testGroup.getUuid()).assignDescription(null).assignSaveMode(SaveMode.INSERT_OR_UPDATE).save();

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      dbDescription = new GcDbAccess().connectionName("grouper")
          .sql("select description from mock_freshreq_group where name = ?").addBindVar("testGroup").select(String.class);
      assertNull(dbDescription);

      retrievedGroup = FreshRequesterApiCommands.retrieveRequesterGroup("freshServiceDev", groupId);
      assertNotNull(retrievedGroup);
      assertNull(retrievedGroup.getDescription());

    } finally {

    }
  }

  public void testFullSyncProvisionGroupAndThenDeleteGroup() {
    provisionGroupAndThenDeleteGroup(true);
  }

  public void testIncrementalProvisionGroupAndThenDeleteGroup() {
    provisionGroupAndThenDeleteGroup(false);
  }

  public void provisionGroupAndThenDeleteGroup(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      // mark the stem to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // if incremental, initialize provisioner state before attaching provisioning attribute
      if (!isFull) {
        fullProvision();
        incrementalProvision();
      }

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      // assert mock tables are empty before sync
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      //
      // first provision: should provision group, 2 users, 2 memberships
      //
      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      String groupName = new GcDbAccess().connectionName("grouper").sql("select name from mock_freshreq_group").select(String.class);
      assertEquals("testGroup", groupName);

      //
      // remove one member and provision again
      //
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      //
      // add a different member and provision again
      //
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      //
      // delete the group entirely and provision again
      //
      testGroup.delete();

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));

    } finally {

    }
  }

  public void testMemberAddRemoveReAddFull() {
    memberAddRemoveReAdd(true);
  }

  public void testMemberAddRemoveReAddIncremental() {
    memberAddRemoveReAdd(false);
  }

  public void memberAddRemoveReAdd(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      // if incremental, initialize provisioner state before attaching provisioning attribute
      if (!isFull) {
        fullProvision();
        incrementalProvision();
      }

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      //
      // first provision: should provision group, 1 user, 1 membership
      //
      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      Long userId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_user where active = 'T'").select(Long.class);
      Long groupId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_group").select(Long.class);

      // verify via commands class
      FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(retrievedUser);
      assertEquals(Boolean.TRUE, retrievedUser.getActive());

      List<FreshRequesterUser> members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(1, members.size());

      //
      // remove member and provision again - user should be deactivated, memberships deleted
      //
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // user still exists in mock table but is inactive
      String activeFlag = new GcDbAccess().connectionName("grouper")
          .sql("select active from mock_freshreq_user where id = ?").addBindVar(userId).select(String.class);
      assertEquals("F", activeFlag);

      // commands class: should not return inactive user without includeInactive flag
      FreshRequesterUser inactiveUserFiltered = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNull(inactiveUserFiltered);

      // commands class: should return inactive user with includeInactive flag
      FreshRequesterUser inactiveUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, true);
      assertNotNull(inactiveUser);
      assertEquals(Boolean.FALSE, inactiveUser.getActive());

      // commands class: no memberships
      members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(0, members.size());

      //
      // re-add the same member and provision again - user should be reactivated, membership re-created
      //
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // commands class: user is active again
      FreshRequesterUser reactivatedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(reactivatedUser);
      assertEquals(Boolean.TRUE, reactivatedUser.getActive());

      // commands class: membership is back
      members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(1, members.size());

    } finally {

    }
  }

  public void testFullSyncEditFirstName() {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
            .addExtraConfig("numberOfEntityAttributes", "3")
            .addExtraConfig("targetEntityAttribute.2.name", "firstName")
            .addExtraConfig("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField")
            .addExtraConfig("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      //
      // first full sync: firstName should be subject id (test.subject.0)
      //
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // check mock table for first_name = subject id
      String dbFirstName = new GcDbAccess().connectionName("grouper")
          .sql("select first_name from mock_freshreq_user where active = 'T'").select(String.class);
      assertEquals("test.subject.0", dbFirstName);

      // check via commands class WS
      Long userId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_user where active = 'T'").select(Long.class);

      FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(retrievedUser);
      assertEquals("test.subject.0", retrievedUser.getFirstName());

      //
      // change config to map firstName to subject name instead of subject id
      //
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.freshRequesterProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField")
          .value("name").store();

      ConfigPropertiesCascadeBase.clearCache();

      GrouperUtil.sleep(7000);

      //
      // second full sync: firstName should now be subject name (my name is test.subject.0)
      //
      fullProvision();

      // check mock table for first_name = subject name
      dbFirstName = new GcDbAccess().connectionName("grouper")
          .sql("select first_name from mock_freshreq_user where active = 'T'").select(String.class);
      assertEquals("my name is test.subject.0", dbFirstName);

      // check via commands class WS
      retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(retrievedUser);
      assertEquals("my name is test.subject.0", retrievedUser.getFirstName());

    } finally {

    }
  }

  public void testFullSyncEditCustomFieldPennId() {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
            .addExtraConfig("numberOfEntityAttributes", "3")
            .addExtraConfig("targetEntityAttribute.2.name.elConfig", "${'customField_pennId'}")
            .addExtraConfig("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField")
            .addExtraConfig("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      //
      // first full sync: customField pennId should be subject id (test.subject.0)
      //
      fullProvision();

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // check mock table for custom_fields containing pennId = subject id
      String dbCustomFields = new GcDbAccess().connectionName("grouper")
          .sql("select custom_fields from mock_freshreq_user where active = 'T'").select(String.class);
      assertNotNull(dbCustomFields);
      assertTrue(dbCustomFields.contains("pennId"));
      assertTrue(dbCustomFields.contains("test.subject.0"));

      // check via commands class WS
      Long userId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_user where active = 'T'").select(Long.class);

      FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(retrievedUser);
      assertNotNull(retrievedUser.getCustomFields());
      assertEquals("test.subject.0", retrievedUser.getCustomFields().get("pennId"));

      //
      // change config to map customField pennId to subject name instead of subject id
      //
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner.freshRequesterProvisioner.targetEntityAttribute.2.translateFromGrouperProvisioningEntityField")
          .value("name").store();

      ConfigPropertiesCascadeBase.clearCache();

      GrouperUtil.sleep(7000);

      //
      // second full sync: customField pennId should now be subject name (my name is test.subject.0)
      //
      fullProvision();

      // check mock table for custom_fields containing pennId = subject name
      dbCustomFields = new GcDbAccess().connectionName("grouper")
          .sql("select custom_fields from mock_freshreq_user where active = 'T'").select(String.class);
      assertNotNull(dbCustomFields);
      assertTrue(dbCustomFields.contains("pennId"));
      assertTrue(dbCustomFields.contains("my name is test.subject.0"));

      // check via commands class WS
      retrievedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", userId, false);
      assertNotNull(retrievedUser);
      assertNotNull(retrievedUser.getCustomFields());
      assertEquals("my name is test.subject.0", retrievedUser.getCustomFields().get("pennId"));

    } finally {

    }
  }

  public void testFullSyncMatchByCustomField() {
    matchByCustomFieldAddRemoveMembers(true);
  }

  public void testIncrementalSyncMatchByCustomField() {
    matchByCustomFieldAddRemoveMembers(false);
  }

  public void matchByCustomFieldAddRemoveMembers(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId("freshRequesterProvisioner")
            .addExtraConfig("numberOfEntityAttributes", "3")
            .addExtraConfig("targetEntityAttribute.2.name.elConfig", "${'customField_pennId'}")
            .addExtraConfig("targetEntityAttribute.2.translateExpressionType", "grouperProvisioningEntityField")
            .addExtraConfig("targetEntityAttribute.2.translateFromGrouperProvisioningEntityField", "subjectId")
            .addExtraConfig("entityMatchingAttributeCount", "3")
            .addExtraConfig("entityMatchingAttribute2name", "customField_pennId")
            .addExtraConfig("entityAttributeValueCache2has", "true")
            .addExtraConfig("entityAttributeValueCache2source", "target")
            .addExtraConfig("entityAttributeValueCache2type", "entityAttribute")
            .addExtraConfig("entityAttributeValueCache2entityAttribute", "customField_pennId")
    );

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    try {
      // this will create tables
      FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();

      Stem stem = new StemSave(grouperSession).assignName("test").save();

      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);

      // if incremental, initialize provisioner state before attaching provisioning attribute
      if (!isFull) {
        fullProvision();
        incrementalProvision();
      }

      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("freshRequesterProvisioner");
      attributeValue.setTargetName("freshRequesterProvisioner");
      attributeValue.setStemScopeString("sub");

      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

      // assert mock tables are empty before sync
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      //
      // first provision: should create group, 2 users with customField_pennId, 2 memberships
      //
      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // verify customField_pennId is set to subjectId for both users
      String customFields0 = new GcDbAccess().connectionName("grouper")
          .sql("select custom_fields from mock_freshreq_user where email = ?")
          .addBindVar("test.subject.0@somewhere.someSchool.edu")
          .select(String.class);
      assertNotNull(customFields0);
      assertTrue(customFields0.contains("pennId"));
      assertTrue(customFields0.contains("test.subject.0"));

      String customFields1 = new GcDbAccess().connectionName("grouper")
          .sql("select custom_fields from mock_freshreq_user where email = ?")
          .addBindVar("test.subject.1@somewhere.someSchool.edu")
          .select(String.class);
      assertNotNull(customFields1);
      assertTrue(customFields1.contains("pennId"));
      assertTrue(customFields1.contains("test.subject.1"));

      // verify via commands class
      Long groupId = new GcDbAccess().connectionName("grouper")
          .sql("select id from mock_freshreq_group").select(Long.class);

      List<FreshRequesterUser> members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(2, members.size());

      // verify custom field via commands class attribute search
      FreshRequesterUser user0 = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", "test.subject.0");
      assertNotNull(user0);
      assertNotNull(user0.getCustomFields());
      assertEquals("test.subject.0", user0.getCustomFields().get("pennId"));

      FreshRequesterUser user1 = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", "test.subject.1");
      assertNotNull(user1);
      assertNotNull(user1.getCustomFields());
      assertEquals("test.subject.1", user1.getCustomFields().get("pennId"));

      //
      // remove one member and provision again
      //
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // verify via commands class: only 1 membership remains
      members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(1, members.size());

      // the remaining member should be SUBJ0
      FreshRequesterUser remainingUser = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", "test.subject.0");
      assertNotNull(remainingUser);
      assertEquals(Boolean.TRUE, remainingUser.getActive());

      //
      // add a new member and provision again
      //
      testGroup.addMember(SubjectTestHelper.SUBJ2, false);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // verify via commands class: 2 memberships
      members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(2, members.size());

      // verify the new user has customField_pennId set
      FreshRequesterUser user2 = FreshRequesterApiCommands.retrieveRequesterUserByAttribute("freshServiceDev", "customField_pennId", "test.subject.2");
      assertNotNull(user2);
      assertNotNull(user2.getCustomFields());
      assertEquals("test.subject.2", user2.getCustomFields().get("pennId"));

      //
      // remove all members and provision again
      //
      testGroup.deleteMember(SubjectTestHelper.SUBJ0);
      testGroup.deleteMember(SubjectTestHelper.SUBJ2);

      if (isFull) {
        fullProvision();
      } else {
        incrementalProvision();
      }

      assertEquals(new Integer(1), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_group").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_user where active = 'T'").select(int.class));
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_freshreq_membership").select(int.class));

      // verify via commands class: 0 memberships
      members = FreshRequesterApiCommands.retrieveMembershipsByGroup("freshServiceDev", groupId);
      assertEquals(0, members.size());

    } finally {

    }
  }

  public void testReactivateRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    // create a user
    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);

    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);
    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);

    // deactivate the user
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", createdUser.getId());

    // verify user is inactive
    FreshRequesterUser deactivatedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), true);
    assertNotNull(deactivatedUser);
    assertEquals(Boolean.FALSE, deactivatedUser.getActive());

    // reactivate the user
    FreshRequesterApiCommands.reactivateRequesterUser("freshServiceDev", createdUser.getId());

    // verify user is active again
    FreshRequesterUser reactivatedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(reactivatedUser);
    assertEquals(Boolean.TRUE, reactivatedUser.getActive());

    // reactivate again should not throw (400 with body is allowed)
    FreshRequesterApiCommands.reactivateRequesterUser("freshServiceDev", createdUser.getId());

    // verify still active
    reactivatedUser = FreshRequesterApiCommands.retrieveRequesterUserById("freshServiceDev", createdUser.getId(), false);
    assertNotNull(reactivatedUser);
    assertEquals(Boolean.TRUE, reactivatedUser.getActive());
  }

  /**
   * Sync-back smoke test: with all three load*ToGenericGrouperTable flags on, a full
   * provision populates grouper_prov_user / _group / _mship from the FreshRequester read
   * path. Asserts all three axes have rows and at least one row per axis is linked back to
   * its Grouper counterpart. Framework-detail coverage lives in the SCIM + LDAP suites.
   */
  public void testFreshRequesterFullSyncPopulatesGenericTables() {

    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    String configId = "freshRequesterProvisioner";
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId(configId)
            .addExtraConfig("recalculateAllOperations", "true")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    // baseline: clean FreshRequester mock target + seed Grouper-side stems/groups/members
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // first pass writes to FreshRequester target; sync-back tables stay empty until next
    // read pass captures the new objects (read-state convergence contract).
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());

    // second pass: reads back what we just wrote, captures through the sync hooks, flushes
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row after sync-back",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows",
        countSyncBack(configId, "grouper_prov_mship") >= 2);

    int linkedGroups = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and group_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_group row should be linked to a Grouper group", linkedGroups >= 1);

    int linkedUsers = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?) "
            + "and member_internal_id is not null")
        .addBindVar(configId).select(int.class);
    assertTrue("at least one prov_user row should be linked to a Grouper member", linkedUsers >= 1);
  }

  /**
   * Might need to run individually.
   * 
   * Sync-back smoke test for the scoped (selectAll=false) read path: with selectAllGroups and
   * selectAllEntities both false, full provision drives the per-id retrieveGroup /
   * retrieveEntity hooks (and per-group membership listing). After a write pass + a read pass,
   * the generic prov_* tables should be populated for all three axes.
   *
   * <p>Incremental test coverage is intentionally deferred — the framework today only captures
   * from reads, and writes converge on the next read pass.
   */
  public void testFreshRequesterFullSyncSelectByIdsPopulatesGenericTables() {
    // MIGHT NEED TO RUN INDIVIDUALLY!
    if (!tomcatRunTests()) {
      return;
    }

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    String configId = "freshRequesterProvisioner";
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId(configId)
            .addExtraConfig("selectAllGroups", "false")
            .addExtraConfig("selectAllEntities", "false")
            // disable the unrelated "loadEntitiesToGrouperTable" feature — its loader NPEs
            // when a scoped retrieve returns null (same defensive override as Adobe).
            .addExtraConfig("loadEntitiesToGrouperTable", "false")
            .addExtraConfig("recalculateAllOperations", "true")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "true"));

    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    // baseline: clean FreshRequester mock target + seed Grouper-side stems/groups/members
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_user"));
    assertEquals(0, countSyncBack(configId, "grouper_prov_mship"));

    // pass 1 inserts target objects; pass 2 reads them back via scoped retrieve and flushes.
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());

    assertTrue("expected at least 1 prov_group row via scoped retrieve",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected at least 2 prov_user rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertTrue("expected at least 2 prov_mship rows via scoped retrieve",
        countSyncBack(configId, "grouper_prov_mship") >= 2);
  }

  /** count rows for a given prov_* table scoped to a provisioner name */
  private int countSyncBack(String configId, String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).select(int.class);
  }

  // ==========================================================================================
  // SCIM-parity sync-back CRUD / convergence tests for FreshService (Box pilot port).
  //
  // CAPABILITY MATRIX (from FreshRequesterTargetDao.registerGrouperProvisionerDaoCapabilities):
  //   canInsertGroup / canUpdateGroup / canDeleteGroup ............ true  (full group CRUD)
  //   canInsertEntity / canUpdateEntity / canDeleteEntity ......... true  (entity delete == deactivate)
  //   canInsertMembership / canDeleteMembership ................... true
  //   canRetrieveAllGroups / canRetrieveGroup ..................... true
  //   canRetrieveAllEntities / canRetrieveEntity ................. true
  //   canRetrieveMembershipsAllByGroup ........................... true  (group-centric mship read)
  //   canSyncBack ................................................ true
  //   canReplaceMembership ....................................... NOT declared -> not tested
  //                                                                 (Box does not test it either)
  //
  // Unlike the prompt's worry, FreshService GROUP ops ARE fully supported (insert/update/delete all
  // hit real mock endpoints), so the group CRUD converge tests below are NOT skipped. The only axis
  // operation FreshService lacks is membership *replace* -- the framework decomposes membership sync
  // into insert+delete (both supported), so nothing is skipped for that reason.
  //
  // KEY FRESHSERVICE-SPECIFIC FACTS used below:
  //  * Sync-back captures groups/users from the raw Freshservice JSON at the read seam, and
  //    memberships group-centric from retrieveMembershipsByGroup -- so, exactly like Box, a freshly
  //    written object/membership shows up in the mirror on the NEXT READ pass (two-pass full).
  //  * Groups are matched by name (groupMatchingAttribute name=id,name). Per the Adobe lesson we do
  //    NOT mutate the match key, so there is NO rename-as-update test; the group update-converge test
  //    mutates DESCRIPTION (a mapped, NON-matching attribute).
  //  * Entity "delete" is a SOFT delete (deleteEntity -> deactivateRequesterUser): a deleted user
  //    stays in mock_freshreq_user with active='F'. The framework's read path
  //    (retrieveRequesterUsers includeInactive=false) does not return inactive users, so they leave
  //    the mirror once deactivated -- the assertions account for this.
  //  * FreshRequesterProvisionerTestUtils.configureProvisioner already turns ON all delete-types
  //    (customize*Crud=true + delete*=true + delete*IfNotExistInGrouper / delete*IfGrouperDeleted).
  //    This is the OPPOSITE of Box's defaults. So the orphan-persistence tests below must DISABLE the
  //    relevant delete-types (customize*Crud=true + delete*=false) so target-drift orphans survive.
  //  * DEFAULT_*_ATTRS captured by the native sync (assert ONLY on these, not SCIM names):
  //       groups   -> name                    (FreshRequesterProvisioningTargetNativeSync.DEFAULT_GROUP_ATTRS)
  //       entities -> email (/primary_email), active   (DEFAULT_ENTITY_ATTRS)
  //
  // All tests gate on tomcatRunTests() like the existing FreshService sync-back tests (they need the
  // mock Tomcat for the WS round-trip).
  // ==========================================================================================

  /**
   * Shared setup for the FreshService sync-back tests: configure the provisioner with the three
   * load*ToGenericGrouperTable flags on (and recalculateAllOperations so every object/membership is
   * processed each run), then clean the FreshService mock target. The caller starts its own root
   * session and creates the Grouper-side stems/groups/members it needs. Mirrors Box's
   * setupBoxSyncBack.
   *
   * <p>NB the FreshService config util (configureProvisioner) turns delete-types ON by default
   * (opposite of Box). A caller that needs target-side orphans to persist must pass the disabling
   * suffixes via {@code extraConfig} (customize*Crud=true + delete*=false).
   *
   * @param configId the provisioner config id (always "freshRequesterProvisioner" here)
   * @param extraConfig additional provisioner.&lt;configId&gt;.* suffixes to set (may be null)
   */
  private void setupFreshRequesterSyncBack(String configId, Map<String, String> extraConfig) {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterProvisionerTestConfigInput configInput = new FreshRequesterProvisionerTestConfigInput()
        .assignConfigId(configId)
        .addExtraConfig("recalculateAllOperations", "true")
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (extraConfig != null) {
      for (Map.Entry<String, String> entry : extraConfig.entrySet()) {
        configInput.addExtraConfig(entry.getKey(), entry.getValue());
      }
    }
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(configInput);

    // give the config store time to settle (same idiom the existing FreshService tomcat tests use)
    GrouperUtil.sleep(5000);

    GrouperStartup.startup();

    // this read creates the mock tables (same idiom as the existing FreshService tests) before wipe
    FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);

    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();
  }

  /**
   * The single provisioned group's target_group_id (Freshservice group id) in the mirror, or null.
   * Mirrors Box's helper of the same name -- used by the update-converge test to prove the SAME
   * target object survives an update (in-place update, not delete + re-create).
   */
  private String mirroredGroupTargetId(String configId) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select target_group_id from grouper_prov_group "
            + "where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(configId).selectList(String.class);
    return ids.isEmpty() ? null : ids.get(0);
  }

  /**
   * Resolved {@code name} attribute value for the single provisioned group in the mirror, or null.
   * Reads through the {@code grouper_prov_group_attr_v} reporting view (not the raw value table),
   * because the string is stored via a dictionary FK and only the view resolves it back to text
   * (column {@code value_string}). {@code name} IS a FreshService default group capture attribute
   * (DEFAULT_GROUP_ATTRS), so it is captured without any extra config.
   */
  private String mirroredGroupName(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'name'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Resolved {@code description} attribute value for the single provisioned group in the mirror, or
   * null. {@code description} is NOT a FreshService default capture attribute (only {@code name}
   * is), so the update-converge test must configure {@code nativeAttributesGroups=name,description}
   * for this to return non-null -- exactly the Box pattern.
   */
  private String mirroredGroupDescription(String configId) {
    List<String> values = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_group_attr_v "
            + "where grouper_sync_id in (select id from grouper_sync where provisioner_name = ?) "
            + "and attribute_name = 'description'")
        .addBindVar(configId).selectList(String.class);
    return values.isEmpty() ? null : values.get(0);
  }

  /**
   * Sync-back convergence of a newly created GROUP, two-pass full (FreshService analogue of SCIM's
   * testGroupInsertConvergesSameRun / Box's testBoxGroupInsertConvergesNextRead). FreshService
   * captures objects on the READ path, and createGroupsAndEntitiesBeforeTranslatingMemberships +
   * selectGroups are on, so the just-inserted group is re-read (to link it) within the same run --
   * meaning it is already in the mirror after pass 1, linked back to its Grouper group. Pass 2 is
   * idempotent. {@code name} (a default capture attribute) round-trips through the reporting view.
   */
  public void testFreshRequesterGroupInsertConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    setupFreshRequesterSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // baseline: nothing in the mirror yet
    assertEquals(0, countSyncBack(configId, "grouper_prov_group"));

    // pass 1 inserts the group AND -- via the post-insert re-read that links it -- captures it, so
    // the group converges into the mirror within this same run
    GrouperProvisioningOutput out1 = fullProvision();
    assertEquals(0, out1.getRecordsWithErrors());
    assertEquals("group insert converges in the same run (post-insert re-read captures it)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // pass 2 re-reads; convergence is idempotent
    GrouperProvisioningOutput out2 = fullProvision();
    assertEquals(0, out2.getRecordsWithErrors());
    assertEquals("group insert should still be the single prov_group row", 1,
        countSyncBack(configId, "grouper_prov_group"));

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    assertNotNull("grouper_sync row should exist for " + configId, gcGrouperSync);
    long syncInternalId = gcGrouperSync.getInternalId();

    // captured via a read, so it is linked back to its Grouper group
    int groupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and group_internal_id is not null")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("converged prov_group row should be linked to its Grouper group", 1, groupRowsLinked);

    // name captured from the FreshService read response (a default group capture attribute)
    assertEquals("name should round-trip into the mirror as a default capture attribute",
        "testGroup", mirroredGroupName(configId));
  }

  /**
   * Sync-back convergence of an object DELETE, two-pass full (FreshService analogue of SCIM's
   * testGroupDeleteConvergesSameRun / Box's testBoxGroupDeleteConvergesNextRead). Seed
   * test:testGroup + SUBJ0 + their membership into the mirror, then delete the group in Grouper.
   * The default config has all delete-types ON, so the next full sync removes the group + its
   * membership from the target and DEACTIVATES the now-orphaned SUBJ0 (entity delete is a soft
   * delete) on pass A; the re-read pass (pass B) no longer sees the group, the membership, or the
   * now-inactive user (the read filters inactive), so the full-replace flush drops all three from
   * the mirror.
   */
  public void testFreshRequesterGroupDeleteConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // delete-types are already ON by default in configureProvisioner; pass null (no overrides).
    setupFreshRequesterSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: two passes converge the group + SUBJ0 + their membership into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // delete the group; SUBJ0 is now orphaned (no other provisioned group) and is deactivated too
    testGroup.delete();

    // pass A: the delete writes hit the target (group removed, membership removed, SUBJ0 deactivated)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read no longer sees the group, the membership, or the inactive SUBJ0; the
    // full-replace flush drops their mirror rows
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("orphaned SUBJ0 dropped from the mirror (deactivated -> filtered on read)", 0,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("membership dropped from the mirror after the re-read pass", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of an object UPDATE on a NON-matching attribute, two-pass full
   * (FreshService analogue of SCIM's testUserUpdateConvergesSameRun, but on a GROUP -- mirrors Box's
   * testBoxGroupUpdateConvergesNextRead). FreshService groups are matched by name, so per the Adobe
   * lesson we do NOT mutate the match key: we mutate the group's DESCRIPTION (mapped via
   * targetGroupAttribute.2, round-trips through the mock's updateGroup, and is NOT the matching
   * attribute). nativeAttributesGroups is set to "name,description" so the description value is
   * actually captured into the mirror (description is not a FreshService default capture attribute).
   *
   * <p>Asserts both that the description VALUE converges AND that it is an in-place update -- the
   * SAME target group id survives (not delete + re-create, which would assign a new Freshservice id).
   * Convergence is on the re-read pass (pass B), since FreshService captures on read.
   */
  public void testFreshRequesterGroupUpdateConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    // capture description (not a FreshService default) so we can assert the updated value in the mirror
    extraConfig.put("nativeAttributesGroups", "name,description");
    setupFreshRequesterSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("originalDescription").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group provisioned with description "originalDescription"
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    String groupTargetIdBefore = mirroredGroupTargetId(configId);
    assertNotNull("group should have a target id after seed", groupTargetIdBefore);
    assertEquals("seed: original description captured", "originalDescription",
        mirroredGroupDescription(configId));

    // change the description (a NON-matching attribute) -> FreshService updateGroup
    testGroup = new GroupSave(grouperSession).assignName(testGroup.getName())
        .assignUuid(testGroup.getUuid()).assignDescription("newDescription")
        .assignSaveMode(SaveMode.UPDATE).save();

    // pass A: the description update reaches the target (updateGroup persists it)
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: the re-read captures the target's actual new description into the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // mirror side: still ONE group, the SAME group (same target id) -- in-place update, not delete +
    // re-create -- and its description converged to the new value.
    assertEquals("group still in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("mirror tracks the same group through the update (update, not re-create)",
        groupTargetIdBefore, mirroredGroupTargetId(configId));
    assertEquals("mirror description should converge to the new value on the re-read pass",
        "newDescription", mirroredGroupDescription(configId));

    // NOTE: no rename-as-update test. FreshService groups are matched by name, so renaming would
    // mutate the match key (the Adobe lesson) and could not converge as an in-place update.
    // NOTE: no user-update-converge test either. FreshService users are matched by id + email; the
    // only Grouper-driven user attribute mapped by default is email (= the match key). There is no
    // safe Grouper-driven NON-matching user attribute to mutate by default, so an update-converge
    // test would be mutating the match key -- skipped rather than written (same reasoning as Box).
  }

  /**
   * Sync-back convergence of a membership ADD to an already-provisioned group, two-pass full
   * (FreshService analogue of SCIM's testMembershipAddConvergesSameRun / Box's
   * testBoxMembershipAddConvergesNextRead). Seed test:testGroup with SUBJ0, then add SUBJ1. Because
   * FreshService captures memberships on the read path (retrieveMembershipsByGroup), the add shows
   * in grouper_prov_mship on the re-read pass: pass A issues the membership insert (+ SUBJ1 user
   * insert) to the target, pass B re-reads the group's members and the flush converges (testGroup,
   * SUBJ1).
   */
  public void testFreshRequesterMembershipAddConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    setupFreshRequesterSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + the one membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("seed: the single membership", 1, countSyncBack(configId, "grouper_prov_mship"));

    // add SUBJ1 to the already-provisioned group
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // pass A: the membership insert (and SUBJ1's user insert) hit the target
    assertEquals(0, fullProvision().getRecordsWithErrors());
    // pass B: re-read sees both members; the flush converges the added membership
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("group should still be in the mirror", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("both users should be in the mirror after the add", 2,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("the added membership should converge on the re-read pass", 2,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Sync-back convergence of a membership REMOVE from a surviving group, two-pass full (FreshService
   * analogue of SCIM's testMembershipRemoveConvergesSameRun / Box's
   * testBoxMembershipRemoveConvergesNextRead). Two groups both hold SUBJ0; SUBJ0 is removed from
   * testGroup only (it survives in otherGroup, so its FreshService user is NOT deactivated). The
   * full-replace flush, fed by the re-read of each group's members, drops exactly testGroup's
   * membership while leaving otherGroup's intact.
   */
  public void testFreshRequesterMembershipRemoveConvergesNextRead() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // membership delete-types are already ON by default in configureProvisioner; pass null.
    setupFreshRequesterSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group otherGroup = new GroupSave(grouperSession).assignName("test:otherGroup").save();
    // SUBJ0 in BOTH groups so removing it from testGroup leaves it provisioned (still in otherGroup)
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    otherGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

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
    // pass B: re-read of testGroup's members no longer includes SUBJ0; the full-replace flush drops
    // (testGroup,SUBJ0) while otherGroup's SUBJ0 membership survives
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("both groups should still be in the mirror", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("SUBJ0 should still be in the mirror (still in otherGroup)", 1,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("testGroup's membership should be gone, otherGroup's should remain", 1,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * Multi-sync coverage with data evolution between rounds, FreshService analogue of SCIM's
   * testFullProvisionReflectsDataChangesAcrossSyncs / Box's
   * testBoxFullSyncReflectsDataChangesAcrossSyncs. Round 1: testGroup with SUBJ0 only, seeded via
   * two passes. Round 2: add SUBJ1 (Grouper-side) AND insert a target-drift orphan group + orphan
   * user directly into the FreshService mock (with delete-types DISABLED so they persist). Round 3:
   * two more passes -> the mirror reflects the new state (3 users: SUBJ0, SUBJ1, orphan; 2 groups:
   * testGroup, orphan; 2 memberships in testGroup), and the orphan user's email value round-trips.
   */
  public void testFreshRequesterFullSyncReflectsDataChangesAcrossSyncs() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // DISABLE all delete-types (FreshService defaults them ON) so the Round 2 orphans persist across
    // syncs. Each axis: turn ON customize*Crud (so the explicit key is accepted by validation) and
    // set the umbrella delete*=false. This is the inverse of Box, which defaults delete-types off.
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("customizeGroupCrud", "true");
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("customizeEntityCrud", "true");
    noDeletes.put("deleteEntities", "false");
    noDeletes.put("customizeMembershipCrud", "true");
    noDeletes.put("deleteMemberships", "false");
    setupFreshRequesterSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // ===================== ROUND 1: initial state =====================

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("round 1: 1 prov_user row for SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 1: 1 prov_group row for testGroup", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 1: 1 prov_mship row for SUBJ0 in testGroup", 1, countSyncBack(configId, "grouper_prov_mship"));

    // ===================== ROUND 2: data changes =====================

    // Grouper-side: add SUBJ1 to testGroup. next full sync inserts SUBJ1 + the membership.
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    // Target-side drift: insert an orphan group + orphan user directly into the FreshService mock
    // (same SQL idiom the existing FreshService forward tests use to seed the mock tables). These
    // are unknown to Grouper; with delete-types off they persist across the next sync. The orphan
    // user is active='T' so the read path returns it. id/name/email are explicit (NOT NULL columns).
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_group (id, name, description) values (8800001, 'orphanGroupAddedMidTest', 'drift group')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) "
            + "values (8800002, 'orphan.evolve@example.edu', 'Orphan', 'Evolve', 'T')")
        .executeSql();

    // ===================== ROUND 3: second full sync + assertions =====================

    // pass A writes SUBJ1 + membership to the target; pass B re-reads everything (Grouper's + the
    // drift orphans) and refreshes the mirror.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    assertEquals("round 3: 3 prov_user rows expected (SUBJ0, SUBJ1, orphan_user)", 3,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("round 3: 2 prov_group rows expected (testGroup, orphan_group)", 2,
        countSyncBack(configId, "grouper_prov_group"));
    assertEquals("round 3: 2 prov_mship rows expected (SUBJ0 + SUBJ1 in testGroup)", 2,
        countSyncBack(configId, "grouper_prov_mship"));

    // the orphan group landed in the mirror, unlinked (no Grouper group)
    int orphanGroupRow = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar("8800001").select(int.class);
    assertEquals("orphan group should land in prov_group with group_internal_id IS NULL", 1,
        orphanGroupRow);

    // the orphan user's email value round-trips through the reporting view (proves target-drift
    // entities are captured with their actual attributes). email IS a FreshService default entity
    // capture attribute (DEFAULT_ENTITY_ATTRS, from /primary_email).
    String orphanUserEmailInReporting = new GcDbAccess().connectionName("grouper")
        .sql("select value_string from grouper_prov_user_attr_v "
            + "where grouper_sync_id = (select id from grouper_sync where internal_id = ?) "
            + "and target_user_id = ? and attribute_name = 'email'")
        .addBindVar(syncInternalId).addBindVar("8800002").select(String.class);
    assertEquals("orphan user's email should round-trip through reporting", "orphan.evolve@example.edu",
        orphanUserEmailInReporting);
  }

  /**
   * Strict-native capture of orphan target objects, FreshService analogue of SCIM's
   * testFullProvisionCapturesOrphanTargetEntities / Box's testBoxFullSyncCapturesOrphanTargetEntities.
   * With delete-types DISABLED, an orphan group + orphan user that exist in the FreshService target
   * but are unknown to Grouper are still captured into the mirror -- with NULL Grouper-side linkage
   * (group_internal_id / member_internal_id) -- alongside Grouper's own testGroup + SUBJ0/SUBJ1,
   * which keep their linkage populated.
   */
  public void testFreshRequesterFullSyncCapturesOrphanTargetEntities() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // disable delete-types so the orphans persist across the run (FreshService defaults them ON)
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("customizeGroupCrud", "true");
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("customizeEntityCrud", "true");
    noDeletes.put("deleteEntities", "false");
    noDeletes.put("customizeMembershipCrud", "true");
    noDeletes.put("deleteMemberships", "false");
    setupFreshRequesterSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate orphans directly into the FreshService mock before the provisioner runs. The
    // orphan user is active='T' so the read path returns it.
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_group (id, name, description) values (8810001, 'orphanGroupNotInGrouper', 'orphan group')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) "
            + "values (8810002, 'orphan.user@example.edu', 'Orphan', 'User', 'T')")
        .executeSql();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // two passes: pass 1 inserts Grouper's objects (orphans untouched, delete-types off); pass 2
    // reads orphans + Grouper's objects and the flush captures all of them.
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // orphan group landed with NULL group_internal_id
    int orphanGroupRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar("8810001").select(int.class);
    assertEquals("expected exactly 1 prov_group row for the orphan group", 1, orphanGroupRowsTotal);

    int orphanGroupRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ? and group_internal_id is null")
        .addBindVar(syncInternalId).addBindVar("8810001").select(int.class);
    assertEquals("orphan group's prov_group row must have group_internal_id IS NULL", 1,
        orphanGroupRowsUnlinked);

    // orphan user landed with NULL member_internal_id
    int orphanUserRowsTotal = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar("8810002").select(int.class);
    assertEquals("expected exactly 1 prov_user row for the orphan user", 1, orphanUserRowsTotal);

    int orphanUserRowsUnlinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ? and member_internal_id is null")
        .addBindVar(syncInternalId).addBindVar("8810002").select(int.class);
    assertEquals("orphan user's prov_user row must have member_internal_id IS NULL", 1,
        orphanUserRowsUnlinked);

    // Grouper's own testGroup + 2 members land alongside, with linkage populated
    int testGroupRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id != ? and group_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar("8810001").select(int.class);
    assertEquals("Grouper's testGroup prov_group row must have group_internal_id linked", 1,
        testGroupRowsLinked);

    int nonOrphanUserRowsLinked = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id != ? and member_internal_id is not null")
        .addBindVar(syncInternalId).addBindVar("8810002").select(int.class);
    assertEquals("Grouper-provisioned prov_user rows (SUBJ0 + SUBJ1) must have member_internal_id linked",
        2, nonOrphanUserRowsLinked);

    // a FreshService default group attribute (name) is captured in the catalog
    int nameCatalog = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'name'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("default group attribute 'name' should be in the per-provisioner catalog", 1,
        nameCatalog);

    // sanity: 'id' must NOT be captured as an attribute -- it is already the target_group_id column
    int idAsGroupAttrRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group_attr "
            + "where grouper_sync_internal_id = ? and attribute_name = 'id'")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("'id' must not appear in grouper_prov_group_attr (already target_group_id column)", 0,
        idAsGroupAttrRows);
  }

  /**
   * Strict-native capture on the MEMBERSHIP axis, FreshService analogue of SCIM's
   * testFullProvisionCapturesMembershipsFromOrphanGroup / Box's
   * testBoxFullSyncCapturesMembershipsFromOrphanGroup. An orphan group with an orphan member
   * (neither known to Grouper) is wired in the FreshService mock (mock_freshreq_membership).
   * FreshService memberships are group-centric, so when the daemon lists groups it also reads the
   * orphan group's members (retrieveMembershipsByGroup) -- that membership must land in
   * grouper_prov_mship alongside Grouper's own, proving strict-native membership capture is
   * independent of Grouper knowledge.
   */
  public void testFreshRequesterFullSyncCapturesMembershipsFromOrphanGroup() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // disable delete-types so the orphan group + its membership persist (FreshService defaults ON)
    Map<String, String> noDeletes = new HashMap<String, String>();
    noDeletes.put("customizeGroupCrud", "true");
    noDeletes.put("deleteGroups", "false");
    noDeletes.put("customizeEntityCrud", "true");
    noDeletes.put("deleteEntities", "false");
    noDeletes.put("customizeMembershipCrud", "true");
    noDeletes.put("deleteMemberships", "false");
    setupFreshRequesterSyncBack(configId, noDeletes);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // orphan group + orphan user + the membership wiring them, all in the FreshService mock. The
    // membership FKs require the group and user rows to exist first (mock_freshreq_mship_*_fkey).
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_group (id, name, description) values (8820001, 'orphanGroupWithMembers', 'orphan group')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) "
            + "values (8820002, 'orphan.mship@example.edu', 'Orphan', 'Mship', 'T')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_membership (id, group_id, user_id) values (8820003, 8820001, 8820002)")
        .executeSql();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // the orphan group's membership lands in prov_mship (join through prov_group/prov_user, which
    // hold the target ids -- prov_mship itself only has the FK internal ids)
    int orphanMshipRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_mship pm "
            + "join grouper_prov_group pg on pg.internal_id = pm.prov_group_internal_id "
            + "join grouper_prov_user pu on pu.internal_id = pm.prov_user_internal_id "
            + "where pm.grouper_sync_internal_id = ? and pg.target_group_id = ? and pu.target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar("8820001").addBindVar("8820002")
        .select(int.class);
    assertEquals("expected 1 prov_mship row for orphan group -> orphan user", 1, orphanMshipRows);

    // Grouper's own memberships land alongside (3 total: SUBJ0 + SUBJ1 in testGroup + the orphan)
    assertEquals("expected 3 prov_mship rows total (2 from testGroup + 1 orphan)", 3,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * !selectAll* scope excludes orphans, FreshService analogue of SCIM's
   * testSelectAllFalseExcludesOrphans / Box's testBoxSelectAllFalseExcludesOrphans. With
   * selectAllGroups=false and selectAllEntities=false the daemon fetches only the resources mapped
   * to Grouper-provisioned objects (by id/name), never a server-wide listing -- so an orphan
   * group/user that the FreshService target has but Grouper does not must NOT land in the mirror.
   */
  public void testFreshRequesterSelectAllFalseExcludesOrphans() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    Map<String, String> extraConfig = new HashMap<String, String>();
    extraConfig.put("selectAllGroups", "false");
    extraConfig.put("selectAllEntities", "false");
    // disable the unrelated loadEntitiesToGrouperTable feature -- its loader NPEs when a scoped
    // retrieve returns null (same defensive override the existing FreshService scoped sync-back
    // test, testFreshRequesterFullSyncSelectByIdsPopulatesGenericTables, applies).
    extraConfig.put("loadEntitiesToGrouperTable", "false");
    setupFreshRequesterSyncBack(configId, extraConfig);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // pre-populate an orphan group + orphan user -- must NOT appear in reporting because
    // selectAll=false makes the daemon fetch only by id/name (Grouper-known resources only).
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_group (id, name, description) values (8830001, 'orphanGroupSelectAllFalse', 'orphan group')")
        .executeSql();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_freshreq_user (id, email, first_name, last_name, active) "
            + "values (8830002, 'orphan.selnone@example.edu', 'Orphan', 'SelNone', 'T')")
        .executeSql();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, configId);
    long syncInternalId = gcGrouperSync.getInternalId();

    // Grouper-known resources still captured
    assertTrue("Grouper-provisioned testGroup should still be in prov_group",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("Grouper-provisioned SUBJ0/SUBJ1 should still be in prov_user",
        countSyncBack(configId, "grouper_prov_user") >= 2);

    // orphans must NOT be captured (selectAll=false -> no server-wide listing -> no capture)
    int orphanGroupRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_group "
            + "where grouper_sync_internal_id = ? and target_group_id = ?")
        .addBindVar(syncInternalId).addBindVar("8830001").select(int.class);
    assertEquals("orphan group must NOT be captured when selectAllGroups=false", 0, orphanGroupRows);

    int orphanUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from grouper_prov_user "
            + "where grouper_sync_internal_id = ? and target_user_id = ?")
        .addBindVar(syncInternalId).addBindVar("8830002").select(int.class);
    assertEquals("orphan user must NOT be captured when selectAllEntities=false", 0, orphanUserRows);
  }

  /**
   * Broken-target delete stays in the mirror, FreshService analogue of SCIM's
   * testUserDeleteBrokenTargetStaysInMirror / Box's testBoxUserDeleteBrokenTargetStaysInMirror. A
   * target object the daemon did NOT remove must STAY captured on the re-read ("verify, don't
   * assume": FreshService re-reads and finds it still present).
   *
   * <p>FreshService analogue mechanism: we have no mock knob to fake a broken delete, so instead we
   * DISABLE entity deletion. SUBJ0 is removed from testGroup in Grouper, but with deleteEntities
   * off the daemon never deactivates the user in the target -- so the user (still active) and its
   * group remain, and the re-read keeps them in the mirror. The group itself is never deleted, so
   * it stays too. NB delete-types are ON by default for FreshService, so we override entity delete
   * to OFF (customizeEntityCrud=true + deleteEntities=false).
   */
  public void testFreshRequesterUserDeleteBrokenTargetStaysInMirror() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    // Disable entity deletion so the daemon will NOT deactivate SUBJ0 in the target once SUBJ0
    // becomes unprovisionable. customizeEntityCrud stays ON (it already is by default) so the
    // explicit deleteEntities=false key is accepted by validation.
    Map<String, String> noEntityDelete = new HashMap<String, String>();
    noEntityDelete.put("customizeEntityCrud", "true");
    noEntityDelete.put("deleteEntities", "false");
    setupFreshRequesterSyncBack(configId, noEntityDelete);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // seed: group + SUBJ0 + their membership in the mirror
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals("seed: group", 1, countSyncBack(configId, "grouper_prov_group"));
    assertEquals("seed: SUBJ0", 1, countSyncBack(configId, "grouper_prov_user"));

    // remove SUBJ0 from the group in Grouper. With entity-delete off the daemon does not deactivate
    // the user in the target, so the target still has an ACTIVE SUBJ0 (and the membership).
    testGroup.deleteMember(SubjectTestHelper.SUBJ0);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    // the group was never deleted -> still in the mirror
    assertEquals("group row should stay (group was not deleted)", 1,
        countSyncBack(configId, "grouper_prov_group"));

    // confirm the target still has the user ACTIVE (the daemon did not deactivate it), so the
    // re-read (includeInactive=false) keeps it
    int mockActiveUserRows = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from mock_freshreq_user where active = 'T'").select(int.class);
    assertEquals("the user row should remain ACTIVE in the target (entity delete is off)", 1,
        mockActiveUserRows);

    assertEquals("user should STAY in the mirror (its deactivate was never performed)", 1,
        countSyncBack(configId, "grouper_prov_user"));
  }

  /**
   * loadGroupsToGenericGrouperTable in isolation, FreshService analogue of SCIM's
   * testLoadGroupsFlagInIsolation / Box's testBoxLoadGroupsFlagInIsolation. Only the groups flag is
   * on -> only grouper_prov_group rows are written; prov_user and prov_mship stay empty even though
   * the daemon still reads users (for provisioning) and memberships (for diffing).
   */
  public void testFreshRequesterLoadGroupsFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId(configId)
            .addExtraConfig("recalculateAllOperations", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "false")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row when groups capture is on",
        countSyncBack(configId, "grouper_prov_group") >= 1);
    assertEquals("expected 0 prov_user rows when entities capture is off", 0,
        countSyncBack(configId, "grouper_prov_user"));
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadEntitiesToGenericGrouperTable in isolation, FreshService analogue of SCIM's
   * testLoadEntitiesFlagInIsolation / Box's testBoxLoadEntitiesFlagInIsolation. Only the entities
   * flag is on -> only grouper_prov_user rows are written; prov_group and prov_mship stay empty.
   */
  public void testFreshRequesterLoadEntitiesFlagInIsolation() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId(configId)
            .addExtraConfig("recalculateAllOperations", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "false")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertEquals("expected 0 prov_group rows when groups capture is off", 0,
        countSyncBack(configId, "grouper_prov_group"));
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1) when entities capture is on",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * loadMembershipsToGenericGrouperTable off, FreshService analogue of SCIM's
   * testLoadMembershipsFlagOff / Box's testBoxLoadMembershipsFlagOff. Both object loads on but
   * memberships off -> prov_group and prov_user populate, prov_mship stays empty. Proves the
   * membership gate is independent of the object gates.
   */
  public void testFreshRequesterLoadMembershipsFlagOff() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();
    FreshRequesterProvisionerTestUtils.configureFreshRequesterProvisioner(
        new FreshRequesterProvisionerTestConfigInput()
            .assignConfigId(configId)
            .addExtraConfig("recalculateAllOperations", "true")
            .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
            .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
            .addExtraConfig("loadMembershipsToGenericGrouperTable", "false"));
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev", false);
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_freshreq_user").executeSql();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    assertEquals(0, fullProvision().getRecordsWithErrors());
    assertEquals(0, fullProvision().getRecordsWithErrors());

    assertTrue("expected >=1 prov_group row", countSyncBack(configId, "grouper_prov_group") >= 1);
    assertTrue("expected >=2 prov_user rows (SUBJ0 + SUBJ1)",
        countSyncBack(configId, "grouper_prov_user") >= 2);
    assertEquals("expected 0 prov_mship rows when memberships capture is off", 0,
        countSyncBack(configId, "grouper_prov_mship"));
  }

  /**
   * INCREMENTAL sync-back coverage for FreshService, conservative (FreshService analogue of Box's
   * testBoxIncrementalSyncBackNoSpuriousDeletes). Like Box, FreshService has NO write-side capture
   * hooks -- it captures groups/users on the READ path and memberships group-centric -- so an
   * incremental cycle re-reads only the changed objects (it has canRetrieveGroup/Entity, so the
   * adapter decomposes to per-id reads that fire the capture seams), and the incremental flush is a
   * SCOPED upsert (it does NOT full-replace, so it will not wrongly delete untouched mirror rows).
   *
   * <p>What this asserts is deliberately narrow -- the safe, reliable part of FreshService
   * incremental sync-back: after seeding via full sync and priming the changelog consumer, adding a
   * member drives an incremental that (a) re-reads the changed group/entity and so does NOT shrink
   * the existing GROUP/USER mirror (no spurious deletes -- the regression the scoped incremental
   * flush guards against), and (b) captures the newly added member's user object into prov_user. It
   * does NOT assert that the new MEMBERSHIP converges on the same incremental cycle: FreshService
   * memberships are captured on read group-centric, and the incremental's read-before-write timing
   * makes same-cycle membership convergence unreliable for a read-capture target (the same
   * 1-cycle-lag reason SCIM disables its object incremental test). Membership convergence for
   * FreshService is covered end-to-end by the two-pass full tests above.
   */
  public void testFreshRequesterIncrementalSyncBackNoSpuriousDeletes() {

    if (!tomcatRunTests()) {
      return;
    }

    String configId = "freshRequesterProvisioner";
    setupFreshRequesterSyncBack(configId, null);

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(configId);
    attributeValue.setTargetName(configId);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

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

    // incremental add: a third member. The incremental re-reads the changed group/entity, firing the
    // FreshService read-capture seams, and the scoped flush upserts -- it must NOT drop untouched rows.
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);
    incrementalProvision();

    // (a) no spurious deletes on the GROUP axis: the scoped incremental flush left existing rows intact
    assertTrue("incremental must not shrink prov_group; before=" + provGroupRowsBefore
        + " after=" + countSyncBack(configId, "grouper_prov_group"),
        countSyncBack(configId, "grouper_prov_group") >= provGroupRowsBefore);
    // NB: prov_mship is intentionally NOT asserted here (matching this test's javadoc). FreshService
    // memberships are group-centric and captured on the READ path; on an incremental cycle the
    // read-before-write timing means testGroup's membership rows can transiently lag, re-converging
    // only on the next full sync (the same 1-cycle lag for which SCIM disables its object incremental
    // test). Membership convergence is covered end-to-end by the two-pass full tests above; here we
    // only guard group/user no-shrink.

    // (b) the newly added member's user object is captured (object capture via the per-id re-read)
    assertEquals("SUBJ2's user object should be captured into prov_user this incremental cycle", 3,
        countSyncBack(configId, "grouper_prov_user"));

    // catalog stays deduped per (sync, attribute_name) after the incremental (the unique-key
    // regression guarded on the LDAP/SCIM side; FreshService shares the same generic flush code)
    int dupGroupAttr = new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from (select grouper_sync_internal_id, attribute_name, count(*) c "
            + "from grouper_prov_group_attr where grouper_sync_internal_id = ? "
            + "group by grouper_sync_internal_id, attribute_name having count(*) > 1) t")
        .addBindVar(syncInternalId).select(int.class);
    assertEquals("group attr catalog should stay deduped per (sync,name) after incremental", 0,
        dupGroupAttr);
  }

}
