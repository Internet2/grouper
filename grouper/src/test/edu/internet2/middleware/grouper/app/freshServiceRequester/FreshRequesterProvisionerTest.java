package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class FreshRequesterProvisionerTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {

    FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();
    TestRunner.run(new FreshRequesterProvisionerTest("testDeactivateRequesterUser"));

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
    assertEquals(1001L, group.getId());
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

    List<FreshRequesterUser> users = FreshRequesterApiCommands.retrieveRequesterUsers("freshServiceDev");

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
    FreshRequesterUser user = FreshRequesterApiCommands.retrieveRequesterUser("freshServiceDev", 2001L);

    assertNotNull(user);
    assertEquals(2001L, user.getId());
    assertEquals("jsmith@test.edu", user.getEmail());
    assertEquals("John", user.getFirstName());
    assertEquals("Smith", user.getLastName());
    assertEquals(Boolean.TRUE, user.getActive());

    // retrieve non-existing user should return null
    FreshRequesterUser notFound = FreshRequesterApiCommands.retrieveRequesterUser("freshServiceDev", 9999L);

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
    FreshRequesterUser user = FreshRequesterApiCommands.retrieveRequesterUserByEmail("freshServiceDev", "jsmith@test.edu");

    assertNotNull(user);
    assertEquals(2001L, user.getId());
    assertEquals("jsmith@test.edu", user.getEmail());
    assertEquals("John", user.getFirstName());
    assertEquals("Smith", user.getLastName());

    // retrieve non-existing email should return null
    FreshRequesterUser notFound = FreshRequesterApiCommands.retrieveRequesterUserByEmail("freshServiceDev", "nobody@test.edu");

    assertNull(notFound);
  }

  public void testCreateRequesterUser() {

    FreshRequesterProvisionerTestUtils.setupFreshRequesterExternalSystem();

    FreshRequesterUser userToCreate = new FreshRequesterUser();
    userToCreate.setFirstName("John");
    userToCreate.setLastName("Smith");
    userToCreate.setEmail("jsmith@test.edu");
    userToCreate.setActive(true);

    // create the user
    FreshRequesterUser createdUser = FreshRequesterApiCommands.createRequesterUser("freshServiceDev", userToCreate);

    assertNotNull(createdUser);
    assertTrue(createdUser.getId() > 0);
    assertEquals("John", createdUser.getFirstName());
    assertEquals("Smith", createdUser.getLastName());
    assertEquals("jsmith@test.edu", createdUser.getEmail());

    // verify it can be retrieved
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUser("freshServiceDev", createdUser.getId());

    assertNotNull(retrievedUser);
    assertEquals(createdUser.getId(), retrievedUser.getId());
    assertEquals("John", retrievedUser.getFirstName());
    assertEquals("Smith", retrievedUser.getLastName());
    assertEquals("jsmith@test.edu", retrievedUser.getEmail());

    // creating a user with the same email should throw an exception (409)
    FreshRequesterUser duplicateUser = new FreshRequesterUser();
    duplicateUser.setFirstName("Johnny");
    duplicateUser.setLastName("Smythe");
    duplicateUser.setEmail("jsmith@test.edu");

    try {
      FreshRequesterApiCommands.createRequesterUser("freshServiceDev", duplicateUser);
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
    FreshRequesterUser retrievedUser = FreshRequesterApiCommands.retrieveRequesterUser("freshServiceDev", createdUser.getId());
    assertNotNull(retrievedUser);
    assertEquals(Boolean.TRUE, retrievedUser.getActive());

    // deactivate the user
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", createdUser.getId());

    // verify user still exists but active is now false
    FreshRequesterUser deactivatedUser = FreshRequesterApiCommands.retrieveRequesterUser("freshServiceDev", createdUser.getId());
    assertNotNull(deactivatedUser);
    assertEquals(createdUser.getId(), deactivatedUser.getId());
    assertEquals("jsmith@test.edu", deactivatedUser.getEmail());
    assertEquals(Boolean.FALSE, deactivatedUser.getActive());

    // deactivate again should not throw an error (still 204 since user exists but inactive)
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", createdUser.getId());

    // deactivate non-existing user should not throw an error (404 is acceptable)
    FreshRequesterApiCommands.deactivateRequesterUser("freshServiceDev", 9999L);
  }

}
