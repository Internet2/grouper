package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import junit.textui.TestRunner;

/**
 * Tests for the Smartsheet provisioner.
 *
 * <p>API-level tests exercise {@link SmartsheetApiCommands} over HTTP against
 * {@link SmartsheetMockServiceHandler} and the mock tables. The full-sync / incremental tests are
 * gated behind {@code tomcatRunTests()} because they drive the whole provisioner against the mock
 * running in the test Tomcat.</p>
 */
public class SmartsheetProvisionerTest extends GrouperProvisioningBaseTest {

  /** external system config id (matches SmartsheetProvisionerTestUtils.CONFIG_ID) */
  private static final String CONFIG_ID = SmartsheetProvisionerTestUtils.CONFIG_ID;

  /** provisioner config id */
  private static final String PROVISIONER_ID = "smartsheetProvisioner";

  public static void main(String[] args) {
    TestRunner.run(new SmartsheetProvisionerTest("testRetrieveUsersPaging"));
    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return PROVISIONER_ID;
  }

  public SmartsheetProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    SmartsheetMockServiceHandler.ensureSmartsheetMockTables();
    clearMockTables();

    SmartsheetProvisionerTestUtils.setupSmartsheetExternalSystem();
  }

  // =============================================
  // Mock seed / inspect helpers
  // =============================================

  private void clearMockTables() {
    new GcDbAccess().connectionName("grouper").sql("delete from mock_smartsheet_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_smartsheet_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_smartsheet_group").executeSql();
  }

  private void insertMockUser(String id, String email, String status) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_smartsheet_user (id, email, first_name, last_name, status, admin, licensed_sheet_creator, group_admin, resource_viewer) "
            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(id).addBindVar(email).addBindVar("First").addBindVar("Last").addBindVar(status)
        .addBindVar("F").addBindVar("F").addBindVar("F").addBindVar("F").executeSql();
  }

  private void insertMockGroup(String id, String name) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_smartsheet_group (id, name, owner, owner_id) values (?, ?, ?, ?)")
        .addBindVar(id).addBindVar(name).addBindVar("owner@example.edu").addBindVar("1").executeSql();
  }

  private void insertMockMembership(String groupId, String userId) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_smartsheet_membership (id, group_id, user_id) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(groupId).addBindVar(userId).executeSql();
  }

  private int groupCount(String name) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_smartsheet_group where lower(name) = ?")
        .addBindVar(name.toLowerCase()).select(int.class);
  }

  private String groupIdByName(String name) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select id from mock_smartsheet_group where lower(name) = ?")
        .addBindVar(name.toLowerCase()).select(String.class);
  }

  private int userCount(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_smartsheet_user where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(int.class);
  }

  private String userStatus(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select status from mock_smartsheet_user where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(String.class);
  }

  private int membershipCount(String groupId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_smartsheet_membership where group_id = ?")
        .addBindVar(groupId).select(int.class);
  }

  private boolean isMember(String groupId, String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_smartsheet_membership m, mock_smartsheet_user u "
            + "where m.user_id = u.id and m.group_id = ? and lower(u.email) = ?")
        .addBindVar(groupId).addBindVar(email.toLowerCase()).select(int.class) > 0;
  }

  // =============================================
  // API-level tests
  // =============================================

  public void testRetrieveUsersPaging() {
    // more than one page (page size 100) to exercise the page loop
    for (int i = 0; i < 150; i++) {
      insertMockUser(String.valueOf(1000000000000000L + i), "user" + i + "@example.edu", "ACTIVE");
    }
    List<SmartsheetUser> users = SmartsheetApiCommands.retrieveUsers(CONFIG_ID, Collections.<String>emptySet());
    assertEquals(150, users.size());
  }

  public void testRetrieveUsersIgnore() {
    insertMockUser("1000000000000001", "keep@example.edu", "ACTIVE");
    insertMockUser("1000000000000002", "skip@example.edu", "ACTIVE");

    Set<String> ignore = SmartsheetApiCommands.parseIgnoreSet("SKIP@example.edu");
    List<SmartsheetUser> users = SmartsheetApiCommands.retrieveUsers(CONFIG_ID, ignore);
    assertEquals(1, users.size());
    assertEquals("keep@example.edu", users.get(0).getEmail());
  }

  public void testRetrieveUserByEmailAndId() {
    insertMockUser("1000000000000011", "jdoe@example.edu", "ACTIVE");

    SmartsheetUser byEmail = SmartsheetApiCommands.retrieveUserByEmail(CONFIG_ID, "JDoe@example.edu");
    assertNotNull(byEmail);
    assertEquals("1000000000000011", byEmail.getId());
    assertEquals("ACTIVE", byEmail.getStatus());
    assertEquals(Boolean.FALSE, byEmail.getAdmin());

    SmartsheetUser byId = SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000011");
    assertNotNull(byId);
    assertEquals("jdoe@example.edu", byId.getEmail());

    assertNull(SmartsheetApiCommands.retrieveUserByEmail(CONFIG_ID, "nobody@example.edu"));
    assertNull(SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "999"));
  }

  public void testCreateUser() {
    SmartsheetUser toCreate = new SmartsheetUser();
    toCreate.setEmail("new@example.edu");
    toCreate.setFirstName("New");
    toCreate.setLastName("User");
    toCreate.setGroupAdmin(true);

    SmartsheetUser created = SmartsheetApiCommands.createUser(CONFIG_ID, toCreate, false);
    assertNotNull(created.getId());

    SmartsheetUser readBack = SmartsheetApiCommands.retrieveUserById(CONFIG_ID, created.getId());
    assertEquals("new@example.edu", readBack.getEmail());
    assertEquals("New", readBack.getFirstName());
    assertEquals(Boolean.TRUE, readBack.getGroupAdmin());
    // admin and licensedSheetCreator default to false on create
    assertEquals(Boolean.FALSE, readBack.getAdmin());
    assertEquals(Boolean.FALSE, readBack.getLicensedSheetCreator());

    // a second add of the same email is rejected by Smartsheet
    try {
      SmartsheetApiCommands.createUser(CONFIG_ID, toCreate, false);
      fail("duplicate user add should fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("1016"));
    }
  }

  public void testUpdateUserPartial() {
    insertMockUser("1000000000000021", "upd@example.edu", "ACTIVE");

    SmartsheetUser user = new SmartsheetUser();
    user.setFirstName("Changed");
    user.setLastName("NotSent");
    user.setLicensedSheetCreator(true);
    // only firstName and licensedSheetCreator
    SmartsheetApiCommands.updateUser(CONFIG_ID, "1000000000000021", user,
        GrouperUtil.toSet("firstName", "licensedSheetCreator"));

    SmartsheetUser readBack = SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000021");
    assertEquals("Changed", readBack.getFirstName());
    assertEquals("Last", readBack.getLastName());
    assertEquals(Boolean.TRUE, readBack.getLicensedSheetCreator());
    assertEquals(Boolean.FALSE, readBack.getGroupAdmin());
  }

  public void testDeactivateReactivateRemoveUser() {
    insertMockUser("1000000000000031", "dis@example.edu", "ACTIVE");

    SmartsheetApiCommands.deactivateUser(CONFIG_ID, "1000000000000031");
    SmartsheetUser deactivated = SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000031");
    assertTrue(deactivated.isDeactivated());

    SmartsheetApiCommands.reactivateUser(CONFIG_ID, "1000000000000031");
    assertFalse(SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000031").isDeactivated());

    SmartsheetApiCommands.removeUser(CONFIG_ID, "1000000000000031", null, false);
    assertNull(SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000031"));

    // removing again (404) is treated as already gone
    SmartsheetApiCommands.removeUser(CONFIG_ID, "1000000000000031", null, false);
  }

  public void testGroupCrud() {
    SmartsheetGroup toCreate = new SmartsheetGroup();
    toCreate.setName("team-a");
    toCreate.setDescription("Team A");
    SmartsheetGroup created = SmartsheetApiCommands.createGroup(CONFIG_ID, toCreate);
    assertNotNull(created.getId());
    assertEquals(1, groupCount("team-a"));

    // names are unique in the org
    try {
      SmartsheetApiCommands.createGroup(CONFIG_ID, toCreate);
      fail("duplicate group name should fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("1103"));
    }

    // rename only (description not sent)
    SmartsheetGroup rename = new SmartsheetGroup();
    rename.setName("team-b");
    rename.setDescription("not sent");
    SmartsheetApiCommands.updateGroup(CONFIG_ID, created.getId(), rename, GrouperUtil.toSet("name"));
    SmartsheetGroup readBack = SmartsheetApiCommands.retrieveGroup(CONFIG_ID, created.getId());
    assertEquals("team-b", readBack.getName());
    assertEquals("Team A", readBack.getDescription());

    assertNotNull(SmartsheetApiCommands.retrieveGroupByName(CONFIG_ID, "TEAM-B", Collections.<String>emptySet()));

    SmartsheetApiCommands.deleteGroup(CONFIG_ID, created.getId());
    assertNull(SmartsheetApiCommands.retrieveGroup(CONFIG_ID, created.getId()));
    // deleting again (404) is treated as already gone
    SmartsheetApiCommands.deleteGroup(CONFIG_ID, created.getId());
  }

  public void testRetrieveGroupsIgnore() {
    insertMockGroup("2000000000000001", "keep-group");
    insertMockGroup("2000000000000002", "Skip-Group");

    List<SmartsheetGroup> groups = SmartsheetApiCommands.retrieveGroups(CONFIG_ID,
        SmartsheetApiCommands.parseIgnoreSet("skip-group"));
    assertEquals(1, groups.size());
    assertEquals("keep-group", groups.get(0).getName());
  }

  public void testGroupMembers() {
    insertMockGroup("2000000000000011", "members-group");
    insertMockUser("1000000000000041", "a@example.edu", "ACTIVE");
    insertMockUser("1000000000000042", "b@example.edu", "ACTIVE");
    insertMockMembership("2000000000000011", "1000000000000041");

    // existing member a is skipped in a bulk add, b is added
    SmartsheetApiCommands.addGroupMembers(CONFIG_ID, "2000000000000011", Arrays.asList("a@example.edu", "b@example.edu"));
    assertEquals(2, membershipCount("2000000000000011"));

    SmartsheetGroup group = SmartsheetApiCommands.retrieveGroup(CONFIG_ID, "2000000000000011");
    assertEquals(2, GrouperUtil.length(group.getMembers()));

    SmartsheetApiCommands.removeGroupMember(CONFIG_ID, "2000000000000011", "1000000000000041");
    assertEquals(1, membershipCount("2000000000000011"));
    assertTrue(isMember("2000000000000011", "b@example.edu"));

    // removing a non-member (404) is a no-op
    SmartsheetApiCommands.removeGroupMember(CONFIG_ID, "2000000000000011", "1000000000000041");
  }

  public void testParseIgnoreSet() {
    Set<String> ignore = SmartsheetApiCommands.parseIgnoreSet("a@x.edu, B@x.edu\nc@x.edu\r\n");
    assertEquals(3, ignore.size());
    assertTrue(SmartsheetApiCommands.isIgnored("A@x.edu", ignore));
    assertTrue(SmartsheetApiCommands.isIgnored("b@x.edu", ignore));
    assertTrue(SmartsheetApiCommands.isIgnored("C@X.EDU", ignore));
    assertFalse(SmartsheetApiCommands.isIgnored("d@x.edu", ignore));
    assertFalse(SmartsheetApiCommands.isIgnored(null, ignore));
  }

  // =============================================
  // Full-sync / incremental (Tomcat required)
  // =============================================

  public void testFullSyncGroupsAndMemberships() {
    groupsAndMemberships(true);
  }

  public void testIncrementalGroupsAndMemberships() {
    groupsAndMemberships(false);
  }

  /**
   * Grouper creates the Smartsheet group and adds the users to the org and the group, then removes
   * and re-adds a member, renames the group, and finally deletes it.
   * @param isFull true for full sync, false for incremental
   */
  private void groupsAndMemberships(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new SmartsheetProvisionerTestConfigInput().assignConfigId(PROVISIONER_ID));

    try {
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group groupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-a").assignDescription("Team A").save();
      groupA.addMember(SubjectTestHelper.SUBJ0, false);
      groupA.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      fullProvision();

      String groupId = groupIdByName("team-a");
      assertNotNull("group should be created", groupId);
      assertEquals(2, membershipCount(groupId));
      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      assertEquals(1, userCount(subj0Email));
      assertEquals("ACTIVE", userStatus(subj0Email));

      // remove SUBJ1
      groupA.deleteMember(SubjectTestHelper.SUBJ1);
      provision(isFull);
      assertEquals(1, membershipCount(groupId));

      // re-add SUBJ1
      groupA.addMember(SubjectTestHelper.SUBJ1, false);
      provision(isFull);
      assertEquals(2, membershipCount(groupId));

      // rename: matched by the cached id, so this is an update of the same group
      groupA = new GroupSave(grouperSession).assignGroupNameToEdit("test:smartsheet:team-a")
          .assignName("test:smartsheet:team-renamed").assignReplaceAllSettings(false).save();
      provision(isFull);
      assertEquals(0, groupCount("team-a"));
      assertEquals(groupId, groupIdByName("team-renamed"));
      assertEquals(2, membershipCount(groupId));

      // delete in Grouper: Grouper created it, so it is deleted in Smartsheet
      groupA.delete();
      provision(isFull);
      assertEquals(0, groupCount("team-renamed"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * A group that already exists in Smartsheet links by name; Grouper manages its members but does
   * not delete it when the Grouper group is deleted, because Grouper did not create it.
   */
  public void testPreexistingGroupNotDeleted() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new SmartsheetProvisionerTestConfigInput().assignConfigId(PROVISIONER_ID));

    try {
      insertMockGroup("2000000000000101", "existing");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:existing").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningAttribute(stem);
      fullProvision();

      assertEquals(1, groupCount("existing"));
      assertEquals(1, membershipCount("2000000000000101"));

      group.delete();
      fullProvision();
      assertEquals("a group Grouper did not create must not be deleted", 1, groupCount("existing"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Deactivate-instead-of-delete (the default): a user leaving the provisioned population is
   * deactivated, not removed; a repeat sync is a no-op; re-adding reactivates the same user.
   */
  public void testDeactivateAndReactivate() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-d").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

      fullProvision();
      String groupId = groupIdByName("team-d");
      assertEquals(2, membershipCount(groupId));
      assertEquals("ACTIVE", userStatus(subj1Email));

      // remove SUBJ1 -> deactivated (not removed), membership removed
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals(1, membershipCount(groupId));
      assertEquals(1, userCount(subj1Email));
      assertEquals("DEACTIVATED", userStatus(subj1Email));

      // deactivated users are filtered from reads, so another sync is a no-op
      fullProvision();
      assertEquals(1, userCount(subj1Email));
      assertEquals("DEACTIVATED", userStatus(subj1Email));

      // re-add SUBJ1 -> reactivated in place, no duplicate
      group.addMember(SubjectTestHelper.SUBJ1, false);
      fullProvision();
      assertEquals(1, userCount(subj1Email));
      assertEquals("ACTIVE", userStatus(subj1Email));
      assertEquals(2, membershipCount(groupId));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * With disableEntitiesInsteadOfDelete=false a user leaving the population is removed from the org.
   */
  public void testRemoveUserFromOrg() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true")
        .addExtraConfig("disableEntitiesInsteadOfDelete", "false");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-r").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

      fullProvision();
      assertEquals(1, userCount(subj1Email));

      group.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals(0, userCount(subj1Email));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * An ignored user who is a member of a provisioned group surfaces as a per-member error and is
   * never added; an ignored user already in the Smartsheet group out-of-band is left alone.
   */
  public void testIgnoredUser() {
    if (!tomcatRunTests()) {
      return;
    }

    String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
    String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");
    String svcEmail = "svc-smartsheet@example.edu";

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("smartsheetIgnoreUserEmails", subj1Email + "\n" + svcEmail);
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // pre-existing group with an out-of-band ignored member
      insertMockGroup("2000000000000201", "team-i");
      insertMockUser("1000000000000201", svcEmail, "ACTIVE");
      insertMockMembership("2000000000000201", "1000000000000201");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-i").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision(PROVISIONER_ID, true);

      assertTrue("ignored subject in a provisioned group must surface an error", output.getRecordsWithErrors() >= 1);
      Member subj1Member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, PROVISIONER_ID);
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(subj1Member.getId());
      assertNotNull("ignored subject should have a sync-member error code", gcGrouperSyncMember.getErrorCode());

      assertEquals(1, userCount(subj0Email));
      assertEquals(0, userCount(subj1Email));
      assertTrue(isMember("2000000000000201", subj0Email));
      assertTrue("ignored out-of-band member must be preserved", isMember("2000000000000201", svcEmail));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning harness helpers
  // =============================================

  private GrouperSession setupProvisionerTest(SmartsheetProvisionerTestConfigInput configInput) {
    SmartsheetProvisionerTestUtils.setupSmartsheetExternalSystem();
    SmartsheetProvisionerTestUtils.configureSmartsheetProvisioner(configInput);

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
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(PROVISIONER_ID);
    attributeValue.setTargetName(PROVISIONER_ID);
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

}
