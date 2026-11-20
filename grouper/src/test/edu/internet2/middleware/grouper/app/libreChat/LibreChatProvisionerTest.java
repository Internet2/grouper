package edu.internet2.middleware.grouper.app.libreChat;

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
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Tests for the LibreChat provisioner (GRP-7594).
 *
 * <p>API-level tests exercise {@link LibreChatApiCommands} over HTTP against
 * {@link LibreChatMockServiceHandler} and the mock tables.  The full sync and incremental tests are gated
 * behind tomcatRunTests() because they need the mock service in the test Tomcat.</p>
 */
public class LibreChatProvisionerTest extends GrouperProvisioningBaseTest {

  /** external system config id */
  private static final String CONFIG_ID = LibreChatProvisionerTestUtils.CONFIG_ID;

  /** default prefix */
  private static final String PREFIX = LibreChatProvisionerConfiguration.DEFAULT_GROUP_ID_ON_THE_SOURCE_PREFIX;

  /** provisioner config id */
  private static final String PROVISIONER_CONFIG_ID = "libreChatProvisioner";

  public static void main(String[] args) {
    TestRunner.run(new LibreChatProvisionerTest("testFullSyncMembership"));
    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return PROVISIONER_CONFIG_ID;
  }

  public LibreChatProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    LibreChatMockServiceHandler.ensureLibreChatMockTables();
    clearMockTables();
    LibreChatProvisionerTestUtils.setupLibreChatExternalSystem();
  }

  // =============================================
  // Mock seed and check helpers
  // =============================================

  private static void clearMockTables() {
    new GcDbAccess().connectionName("grouper").sql("delete from mock_librechat_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_librechat_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_librechat_user").executeSql();
  }

  /**
   * @param username login name
   * @return new user id
   */
  private static String insertMockUser(String username) {
    String id = LibreChatMockServiceHandler.newObjectId();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_librechat_user (id, username, name, email) values (?, ?, ?, ?)")
        .addBindVar(id).addBindVar(username).addBindVar("Name " + username).addBindVar(username).executeSql();
    return id;
  }

  /**
   * @param name group name
   * @param source local or entra
   * @param idOnTheSource as stored, with any prefix
   * @return new group id
   */
  private static String insertMockGroup(String name, String source, String idOnTheSource) {
    String id = LibreChatMockServiceHandler.newObjectId();
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_librechat_group (id, name, source, id_on_the_source) values (?, ?, ?, ?)")
        .addBindVar(id).addBindVar(name).addBindVar(source).addBindVar(idOnTheSource).executeSql();
    return id;
  }

  private static void insertMockMembership(String groupId, String userId) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_librechat_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(LibreChatMockServiceHandler.newObjectId()).addBindVar(groupId).addBindVar(userId).executeSql();
  }

  private static int memberCount(String groupId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_librechat_membership where group_id = ?").addBindVar(groupId).select(int.class);
  }

  /**
   * @param grouperGroup the Grouper group
   * @return the LibreChat group id provisioned for it, or null
   */
  private static String libreChatGroupIdFor(Group grouperGroup) {
    List<String> ids = new GcDbAccess().connectionName("grouper")
        .sql("select id from mock_librechat_group where id_on_the_source = ?")
        .addBindVar(PREFIX + grouperGroup.getId()).selectList(String.class);
    return ids.size() == 0 ? null : ids.get(0);
  }

  private static String email(Subject subject) {
    return subject.getAttributeValue("email");
  }

  // =============================================
  // API-level tests
  // =============================================

  public void testRetrieveUserByUsernameExactCaseInsensitive() {
    String id = insertMockUser("jdoe@upenn.edu");
    // a prefix match that is not the same username must not be returned
    insertMockUser("jdoe@upenn.edu.other");

    LibreChatUser user = LibreChatApiCommands.retrieveUserByUsername(CONFIG_ID, "JDoe@upenn.edu");
    assertNotNull(user);
    assertEquals(id, user.getId());
    assertEquals("jdoe@upenn.edu", user.getUsername());

    assertNull(LibreChatApiCommands.retrieveUserByUsername(CONFIG_ID, "nobody@upenn.edu"));
  }

  public void testRetrieveUsersPaging() {
    // more than one page of 200
    for (int i = 0; i < 205; i++) {
      insertMockUser("user" + i + "@upenn.edu");
    }
    assertEquals(205, LibreChatApiCommands.retrieveUsers(CONFIG_ID).size());
  }

  public void testRetrieveManagedGroupsOnlyWithPrefix() {
    String managedId = insertMockGroup("Managed", "local", PREFIX + "abc");
    insertMockGroup("Other prefix", "local", "other:abc");
    insertMockGroup("No external id", "local", null);
    insertMockGroup("Entra", "entra", PREFIX + "def");

    List<LibreChatGroup> groups = LibreChatApiCommands.retrieveManagedGroups(CONFIG_ID, PREFIX);
    assertEquals(1, groups.size());
    assertEquals(managedId, groups.get(0).getId());
    assertEquals("abc", groups.get(0).toProvisioningGroup(PREFIX).retrieveAttributeValueString("idOnTheSource"));
  }

  public void testCreateUpdateDeleteGroup() {
    LibreChatGroup toCreate = new LibreChatGroup();
    toCreate.setName("Team one");
    toCreate.setDescription("Managed in Grouper");
    toCreate.setIdOnTheSource(PREFIX + "uuid1");
    LibreChatGroup created = LibreChatApiCommands.createGroup(CONFIG_ID, toCreate);
    assertTrue(LibreChatApiCommands.isObjectId(created.getId()));

    LibreChatApiCommands.updateGroup(CONFIG_ID, created.getId(), GrouperUtil.toMap("name", "Team 1"));
    LibreChatGroup readBack = LibreChatApiCommands.retrieveGroup(CONFIG_ID, created.getId());
    assertEquals("Team 1", readBack.getName());
    assertEquals("local", readBack.getSource());
    assertEquals(PREFIX + "uuid1", readBack.getIdOnTheSource());

    LibreChatApiCommands.deleteGroup(CONFIG_ID, created.getId());
    assertNull(LibreChatApiCommands.retrieveGroup(CONFIG_ID, created.getId()));
    // deleting again is fine
    LibreChatApiCommands.deleteGroup(CONFIG_ID, created.getId());
  }

  public void testAddRemoveMemberIdempotent() {
    String groupId = insertMockGroup("Team", "local", PREFIX + "uuid2");
    String userId = insertMockUser("member@upenn.edu");

    LibreChatApiCommands.addGroupMember(CONFIG_ID, groupId, userId);
    LibreChatApiCommands.addGroupMember(CONFIG_ID, groupId, userId);
    assertEquals(1, memberCount(groupId));
    assertEquals(GrouperUtil.toList(userId), LibreChatApiCommands.retrieveGroupMemberIds(CONFIG_ID, groupId));

    LibreChatApiCommands.removeGroupMember(CONFIG_ID, groupId, userId);
    LibreChatApiCommands.removeGroupMember(CONFIG_ID, groupId, userId);
    assertEquals(0, memberCount(groupId));
  }

  public void testIdsMustBeObjectIds() {
    try {
      LibreChatApiCommands.removeGroupMember(CONFIG_ID, "../users", "x");
      fail("a non-ObjectId must never go into the URL");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Not a valid LibreChat id"));
    }
    // ids from LibreChat responses are checked too
    try {
      LibreChatApiCommands.retrieveGroupMemberIds(CONFIG_ID, "x/../../users");
      fail("a non-ObjectId from a response must never go into the URL");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("Not a valid LibreChat id"));
    }
    // a group with a malformed id is never treated as managed (so a full sync skips it)
    LibreChatGroup badId = new LibreChatGroup();
    badId.setId("x/../../users");
    badId.setSource("local");
    badId.setIdOnTheSource(PREFIX + "abc");
    assertFalse(badId.isManaged(PREFIX));
  }

  /**
   * If a LibreChat admin takes a group away from Grouper (changes its idOnTheSource), an incremental sync must
   * not keep changing its members through the id cached in the sync table
   */
  public void testNoMemberChangesAfterGroupUnmanaged() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new LibreChatProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID));

    try {
      insertMockUser(email(SubjectTestHelper.SUBJ0));
      insertMockUser(email(SubjectTestHelper.SUBJ1));

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:chat:team1").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);

      initIncrementalState(false);
      attachProvisioningAttribute(stem);
      fullProvision();
      incrementalProvision();

      String libreChatGroupId = libreChatGroupIdFor(group);
      assertNotNull(libreChatGroupId);
      assertEquals(1, memberCount(libreChatGroupId));

      // a LibreChat admin re-labels the group, so it is no longer Grouper's
      new GcDbAccess().connectionName("grouper").sql("update mock_librechat_group set id_on_the_source = ? where id = ?")
          .addBindVar("other:taken").addBindVar(libreChatGroupId).executeSql();

      group.addMember(SubjectTestHelper.SUBJ1, false);
      try {
        incrementalProvision();
      } catch (RuntimeException re) {
        // the membership fails with "not managed by this provisioner", which fails the job
      }
      assertEquals("members of a group Grouper no longer manages must not change", 1, memberCount(libreChatGroupId));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning tests (need the test Tomcat)
  // =============================================

  public void testFullSyncMembership() {
    membershipAddRemove(true);
  }

  public void testIncrementalMembership() {
    membershipAddRemove(false);
  }

  /**
   * Grouper creates the LibreChat group and manages its members.  A subject without a LibreChat user is
   * skipped until the user exists.
   * @param isFull true for full sync, false for incremental
   */
  private void membershipAddRemove(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new LibreChatProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID));

    try {
      String user0 = insertMockUser(email(SubjectTestHelper.SUBJ0));
      String user1 = insertMockUser(email(SubjectTestHelper.SUBJ1));

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:chat:team1").assignDisplayName("test:chat:Team one").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);
      fullProvision();

      String libreChatGroupId = libreChatGroupIdFor(group);
      assertNotNull("group should be created with idOnTheSource = prefix + Grouper group id", libreChatGroupId);
      assertEquals("Team one", LibreChatApiCommands.retrieveGroup(CONFIG_ID, libreChatGroupId).getName());
      assertEquals(2, memberCount(libreChatGroupId));

      // remove one
      group.deleteMember(SubjectTestHelper.SUBJ1);
      provision(isFull);
      assertEquals(GrouperUtil.toList(user0), LibreChatApiCommands.retrieveGroupMemberIds(CONFIG_ID, libreChatGroupId));

      // add a subject who has never logged in to LibreChat: skipped, no user is created
      group.addMember(SubjectTestHelper.SUBJ2, false);
      provision(isFull);
      assertEquals(1, memberCount(libreChatGroupId));

      // once they log in (the user exists), a full sync adds them
      insertMockUser(email(SubjectTestHelper.SUBJ2));
      group.addMember(SubjectTestHelper.SUBJ1, false);
      fullProvision();
      assertEquals(3, memberCount(libreChatGroupId));
      assertTrue(LibreChatApiCommands.retrieveGroupMemberIds(CONFIG_ID, libreChatGroupId).contains(user1));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Groups without the prefix are never read, changed or deleted, even with members Grouper does not have
   */
  public void testUnmanagedGroupsUntouched() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new LibreChatProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID));

    try {
      String user0 = insertMockUser(email(SubjectTestHelper.SUBJ0));
      String handMade = insertMockGroup("Hand made", "local", null);
      String otherTool = insertMockGroup("Other tool", "local", "other:x");
      insertMockMembership(handMade, user0);
      insertMockMembership(otherTool, user0);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:chat:team1").save();
      attachProvisioningAttribute(stem);
      fullProvision();

      assertNotNull(LibreChatApiCommands.retrieveGroup(CONFIG_ID, handMade));
      assertNotNull(LibreChatApiCommands.retrieveGroup(CONFIG_ID, otherTool));
      assertEquals(1, memberCount(handMade));
      assertEquals(1, memberCount(otherTool));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  public void testDeleteGroup() {
    deleteGroupHelper(false);
  }

  public void testEmptyGroupInsteadOfDelete() {
    deleteGroupHelper(true);
  }

  /**
   * Deleting the Grouper group deletes the LibreChat group, or only removes its members when
   * libreChatEmptyGroupsInsteadOfDelete is true
   * @param emptyInsteadOfDelete setting
   */
  private void deleteGroupHelper(boolean emptyInsteadOfDelete) {
    if (!tomcatRunTests()) {
      return;
    }

    LibreChatProvisionerTestConfigInput configInput = new LibreChatProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID);
    if (emptyInsteadOfDelete) {
      configInput.addExtraConfig("libreChatEmptyGroupsInsteadOfDelete", "true");
    }
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockUser(email(SubjectTestHelper.SUBJ0));

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:chat:team1").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);
      fullProvision();

      String libreChatGroupId = libreChatGroupIdFor(group);
      assertNotNull(libreChatGroupId);
      assertEquals(1, memberCount(libreChatGroupId));

      group.delete();
      fullProvision();

      if (emptyInsteadOfDelete) {
        assertNotNull(LibreChatApiCommands.retrieveGroup(CONFIG_ID, libreChatGroupId));
        assertEquals(0, memberCount(libreChatGroupId));
      } else {
        assertNull(LibreChatApiCommands.retrieveGroup(CONFIG_ID, libreChatGroupId));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning harness helpers
  // =============================================

  private GrouperSession setupProvisionerTest(LibreChatProvisionerTestConfigInput configInput) {
    LibreChatProvisionerTestUtils.setupLibreChatExternalSystem();
    LibreChatProvisionerTestUtils.configureLibreChatProvisioner(configInput);

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    clearMockTables();

    return GrouperSession.startRootSession();
  }

  private void provision(boolean isFull) {
    if (isFull) {
      fullProvision();
    } else {
      incrementalProvision();
    }
  }

  private void initIncrementalState(boolean isFull) {
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
  }

  private void attachProvisioningAttribute(Stem stem) {
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(PROVISIONER_CONFIG_ID);
    attributeValue.setTargetName(PROVISIONER_CONFIG_ID);
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

}
