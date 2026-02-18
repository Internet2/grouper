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

}
