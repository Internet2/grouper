package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
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

  private void insertMockUser(String id, String email, String status, String seatType, boolean internal) {
    insertMockUser(id, email, status);
    new GcDbAccess().connectionName("grouper")
        .sql("update mock_smartsheet_user set seat_type = ?, is_internal = ? where id = ?")
        .addBindVar(seatType).addBindVar(internal ? "T" : "F").addBindVar(id).executeSql();
  }

  private String userSeatType(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select seat_type from mock_smartsheet_user where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(String.class);
  }

  private String userFlag(String email, String column) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select " + column + " from mock_smartsheet_user where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(String.class);
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
    // admin defaults to false on create; licensedSheetCreator is not managed here, so a Group Admin
    // is created licensed (Smartsheet refuses an unlicensed Group Admin)
    assertEquals(Boolean.FALSE, readBack.getAdmin());
    assertEquals(Boolean.TRUE, readBack.getLicensedSheetCreator());

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

    // existing member a (sent by id) is skipped in a bulk add, b (sent by email only) is added
    SmartsheetUser memberA = new SmartsheetUser();
    memberA.setId("1000000000000041");
    memberA.setEmail("a@example.edu");
    SmartsheetUser memberB = new SmartsheetUser();
    memberB.setEmail("b@example.edu");
    SmartsheetApiCommands.addGroupMembers(CONFIG_ID, "2000000000000011", Arrays.asList(memberA, memberB));
    assertEquals(2, membershipCount("2000000000000011"));

    SmartsheetGroup group = SmartsheetApiCommands.retrieveGroup(CONFIG_ID, "2000000000000011");
    assertEquals(2, GrouperUtil.length(group.getMembers()));

    SmartsheetApiCommands.removeGroupMember(CONFIG_ID, "2000000000000011", "1000000000000041");
    assertEquals(1, membershipCount("2000000000000011"));
    assertTrue(isMember("2000000000000011", "b@example.edu"));

    // removing a non-member (404) is a no-op
    SmartsheetApiCommands.removeGroupMember(CONFIG_ID, "2000000000000011", "1000000000000041");
  }

  public void testAddGroupMemberById() {
    insertMockGroup("2000000000000012", "by-id-group");
    insertMockUser("1000000000000043", "c@example.edu", "ACTIVE");

    // the id wins even if the email Grouper has drifted from the Smartsheet primary email
    SmartsheetUser member = new SmartsheetUser();
    member.setId("1000000000000043");
    member.setEmail("old-address@example.edu");
    SmartsheetApiCommands.addGroupMembers(CONFIG_ID, "2000000000000012", Arrays.asList(member));
    assertTrue(isMember("2000000000000012", "c@example.edu"));
  }

  public void testMemberJsonForAdd() {
    SmartsheetUser member = new SmartsheetUser();
    member.setId("1000000000000044");
    member.setEmail("d@example.edu");
    // id first, as a number, and no email alongside it
    assertEquals("{\"id\":1000000000000044}", GrouperUtil.jsonJacksonToString(SmartsheetApiCommands.memberJsonForAdd(member)));

    // no id (or a non-numeric one) falls back to email
    member.setId(null);
    assertEquals("{\"email\":\"d@example.edu\"}", GrouperUtil.jsonJacksonToString(SmartsheetApiCommands.memberJsonForAdd(member)));
    member.setId("notANumber");
    assertEquals("{\"email\":\"d@example.edu\"}", GrouperUtil.jsonJacksonToString(SmartsheetApiCommands.memberJsonForAdd(member)));

    // neither: nothing to send
    member.setEmail(null);
    assertNull(SmartsheetApiCommands.memberJsonForAdd(member));
  }

  public void testUpdateUserUnsetFlagNotSent() {
    SmartsheetUser user = new SmartsheetUser();
    user.setFirstName("Jane");
    user.setGroupAdmin(true);
    // admin is being "updated" but has no value: it must not be sent as false
    String json = GrouperUtil.jsonJacksonToString(user.toJsonForUpdate(GrouperUtil.toSet("firstName", "admin", "groupAdmin")));
    assertTrue(json, json.contains("\"groupAdmin\":true"));
    assertFalse(json, json.contains("admin\":false"));
    assertFalse(json, json.contains("\"admin\""));

    // end to end: an existing System Admin keeps admin when the flag is unset
    insertMockUser("1000000000000051", "sysadmin@example.edu", "ACTIVE");
    new GcDbAccess().connectionName("grouper").sql("update mock_smartsheet_user set admin = 'T' where id = ?")
        .addBindVar("1000000000000051").executeSql();
    SmartsheetApiCommands.updateUser(CONFIG_ID, "1000000000000051", user, GrouperUtil.toSet("firstName", "admin"));
    SmartsheetUser readBack = SmartsheetApiCommands.retrieveUserById(CONFIG_ID, "1000000000000051");
    assertEquals(Boolean.TRUE, readBack.getAdmin());
    assertEquals("Jane", readBack.getFirstName());
  }

  /** plan id used by the seat type tests */
  private static final String PLAN_ID = "1234567890123456";

  public void testSeatTypeReadAndChange() {
    insertMockUser("1000000000000061", "seat@example.edu", "ACTIVE", "MEMBER", true);

    // no seatType without a planId (like the real API)
    assertNull(SmartsheetApiCommands.retrieveUserByEmail(CONFIG_ID, "seat@example.edu").getSeatType());
    assertEquals("MEMBER", SmartsheetApiCommands.retrieveUserByEmail(CONFIG_ID, "seat@example.edu", PLAN_ID).getSeatType());

    SmartsheetApiCommands.changeUserSeatType(CONFIG_ID, "1000000000000061", PLAN_ID, "CONTRIBUTOR", false);
    assertEquals("CONTRIBUTOR", userSeatType("seat@example.edu"));

    // a contributor cannot be downgraded again
    try {
      SmartsheetApiCommands.changeUserSeatType(CONFIG_ID, "1000000000000061", PLAN_ID, "CONTRIBUTOR", false);
      fail("downgrade of a contributor should fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("not eligible"));
    }

    SmartsheetApiCommands.changeUserSeatType(CONFIG_ID, "1000000000000061", PLAN_ID, "MEMBER", true);
    assertEquals("MEMBER", userSeatType("seat@example.edu"));
    // upgrading to the current seat is fine
    SmartsheetApiCommands.changeUserSeatType(CONFIG_ID, "1000000000000061", PLAN_ID, "MEMBER", true);

    // deactivated users must be reactivated first
    SmartsheetApiCommands.deactivateUser(CONFIG_ID, "1000000000000061");
    try {
      SmartsheetApiCommands.changeUserSeatType(CONFIG_ID, "1000000000000061", PLAN_ID, "CONTRIBUTOR", false);
      fail("seat change of a deactivated user should fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("deactivated"));
    }
  }

  public void testExternalUsersOnlyListedWithPlanId() {
    insertMockUser("1000000000000071", "inside@example.edu", "ACTIVE", "MEMBER", true);
    insertMockUser("1000000000000072", "outside@partner.org", "ACTIVE", "GUEST", false);

    assertEquals(1, SmartsheetApiCommands.retrieveUsers(CONFIG_ID, Collections.<String>emptySet()).size());
    List<SmartsheetUser> withPlan = SmartsheetApiCommands.retrieveUsers(CONFIG_ID, Collections.<String>emptySet(), PLAN_ID);
    assertEquals(2, withPlan.size());
    for (SmartsheetUser user : withPlan) {
      assertEquals(user.getEmail(), user.getEmail().endsWith("@partner.org"), user.isExternal());
    }
  }

  public void testIsSeatUpgrade() {
    assertTrue(SmartsheetApiCommands.isSeatUpgrade("CONTRIBUTOR", "MEMBER"));
    assertTrue(SmartsheetApiCommands.isSeatUpgrade("PROVISIONAL_MEMBER", "MEMBER"));
    assertTrue(SmartsheetApiCommands.isSeatUpgrade(null, "MEMBER"));
    assertFalse(SmartsheetApiCommands.isSeatUpgrade("MEMBER", "CONTRIBUTOR"));
    assertFalse(SmartsheetApiCommands.isSeatUpgrade("PROVISIONAL_MEMBER", "CONTRIBUTOR"));
    assertTrue(SmartsheetApiCommands.isSeatUpgrade("CONTRIBUTOR", "GUEST"));
    assertFalse(SmartsheetApiCommands.isSeatUpgrade("MEMBER", "GUEST"));
    assertFalse(SmartsheetApiCommands.isSeatUpgrade("PROVISIONAL_MEMBER", "GUEST"));
    try {
      SmartsheetApiCommands.isSeatUpgrade("MEMBER", "PROVISIONAL_MEMBER");
      fail("provisional cannot be set");
    } catch (RuntimeException e) {
      // expected
    }
  }

  public void testSeatTypeNormalizeAndProvisionalRule() {
    assertEquals("CONTRIBUTOR", SmartsheetUser.normalizeSeatType("viewer"));
    assertEquals("MEMBER", SmartsheetUser.normalizeSeatType(" member "));
    assertNull(SmartsheetUser.normalizeSeatType(" "));

    SmartsheetProvisionerConfiguration config = new SmartsheetProvisionerConfiguration();
    config.setSmartsheetDowngradeProvisionalMembers(false);
    assertTrue(SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, "PROVISIONAL_MEMBER", "CONTRIBUTOR"));
    assertTrue(SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, "PROVISIONAL_MEMBER", "GUEST"));
    // upgrading a provisional member is always done
    assertFalse(SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, "PROVISIONAL_MEMBER", "MEMBER"));
    assertFalse(SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, "MEMBER", "CONTRIBUTOR"));

    config.setSmartsheetDowngradeProvisionalMembers(true);
    assertFalse(SmartsheetProvisioningCompare.isProvisionalLeftAlone(config, "PROVISIONAL_MEMBER", "CONTRIBUTOR"));
  }

  public void testCreateUserLicensesGroupAdmin() {
    SmartsheetUser user = new SmartsheetUser();
    user.setEmail("ga@example.edu");
    user.setResourceViewer(true);
    assertTrue(GrouperUtil.jsonJacksonToString(user.toJsonForCreate()).contains("\"licensedSheetCreator\":true"));
    // an explicit value wins
    user.setLicensedSheetCreator(false);
    assertTrue(GrouperUtil.jsonJacksonToString(user.toJsonForCreate()).contains("\"licensedSheetCreator\":false"));
    // nothing needing a license: unlicensed
    SmartsheetUser plain = new SmartsheetUser();
    plain.setEmail("plain@example.edu");
    assertTrue(GrouperUtil.jsonJacksonToString(plain.toJsonForCreate()).contains("\"licensedSheetCreator\":false"));
  }

  public void testUserRuleSameForLiveAndCache() {
    SmartsheetProvisionerConfiguration config = new SmartsheetProvisionerConfiguration();
    Set<String> ignore = SmartsheetApiCommands.parseIgnoreSet("svc@example.edu");

    config.setDisableEntitiesInsteadOfDelete(true);
    assertTrue(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "SVC@example.edu", "ACTIVE"));
    assertTrue(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "a@example.edu", "DEACTIVATED"));
    assertFalse(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "a@example.edu", "ACTIVE"));
    assertFalse(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "a@example.edu", "PENDING"));
    assertFalse(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "a@example.edu", null));

    // removing (not deactivating): a deactivated user is still an org user, so present
    config.setDisableEntitiesInsteadOfDelete(false);
    assertFalse(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "a@example.edu", "DEACTIVATED"));
    assertTrue(SmartsheetTargetDao.isUserFilteredOut(config, ignore, "svc@example.edu", "ACTIVE"));
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

  public void testFullSyncSeatTypes() {
    seatTypes(true);
  }

  public void testIncrementalSeatTypes() {
    seatTypes(false);
  }

  /**
   * Seat groups (not provisionable) drive the seat: highest wins, a user in no seat group is left
   * alone (no default), and a membership change in a seat group reaches the incremental sync.
   * @param isFull true for full sync, false for incremental
   */
  private void seatTypes(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("smartsheetPlanId", PLAN_ID)
        .addExtraConfig("smartsheetSeatTypeMemberGroupName", "policy:seatTypesToSmartsheet:member")
        .addExtraConfig("smartsheetSeatTypeContributorGroupName", "policy:seatTypesToSmartsheet:contributor");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");
      String subj2Email = SubjectTestHelper.SUBJ2.getAttributeValue("email");
      insertMockUser("1000000000000401", subj0Email, "ACTIVE", "CONTRIBUTOR", true);
      insertMockUser("1000000000000402", subj1Email, "ACTIVE", "MEMBER", true);
      insertMockUser("1000000000000403", subj2Email, "ACTIVE", "MEMBER", true);

      // seat groups live outside the provisioned folder
      Group memberSeatGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("policy:seatTypesToSmartsheet:member").save();
      Group contributorSeatGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("policy:seatTypesToSmartsheet:contributor").save();
      memberSeatGroup.addMember(SubjectTestHelper.SUBJ0, false);
      contributorSeatGroup.addMember(SubjectTestHelper.SUBJ0, false);
      contributorSeatGroup.addMember(SubjectTestHelper.SUBJ1, false);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-s").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);
      group.addMember(SubjectTestHelper.SUBJ2, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);
      fullProvision();

      // SUBJ0 in both seat groups: highest (MEMBER) wins; SUBJ1 contributor; SUBJ2 in none: left alone
      assertEquals("MEMBER", userSeatType(subj0Email));
      assertEquals("CONTRIBUTOR", userSeatType(subj1Email));
      assertEquals("MEMBER", userSeatType(subj2Email));

      // SUBJ1 into the member seat group (a group that is not provisionable)
      memberSeatGroup.addMember(SubjectTestHelper.SUBJ1, false);
      provision(isFull);
      assertEquals("MEMBER", userSeatType(subj1Email));

      // SUBJ0 out of the member seat group: still contributor -> downgraded
      memberSeatGroup.deleteMember(SubjectTestHelper.SUBJ0);
      provision(isFull);
      assertEquals("CONTRIBUTOR", userSeatType(subj0Email));

      // SUBJ1 out of every seat group, no default: seat left alone
      memberSeatGroup.deleteMember(SubjectTestHelper.SUBJ1);
      contributorSeatGroup.deleteMember(SubjectTestHelper.SUBJ1);
      provision(isFull);
      assertEquals("MEMBER", userSeatType(subj1Email));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * smartsheetSeatTypeDefault gives everyone provisioned who is in no seat group that seat, and a
   * provisional member is not downgraded unless smartsheetDowngradeProvisionalMembers.
   */
  public void testSeatTypeDefaultAndProvisional() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("smartsheetPlanId", PLAN_ID)
        .addExtraConfig("smartsheetSeatTypeMemberGroupName", "policy:seatTypesToSmartsheet:member")
        .addExtraConfig("smartsheetSeatTypeDefault", "CONTRIBUTOR");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");
      String subj2Email = SubjectTestHelper.SUBJ2.getAttributeValue("email");
      insertMockUser("1000000000000501", subj0Email, "ACTIVE", "CONTRIBUTOR", true);
      insertMockUser("1000000000000502", subj1Email, "ACTIVE", "MEMBER", true);
      insertMockUser("1000000000000503", subj2Email, "ACTIVE", "PROVISIONAL_MEMBER", true);

      Group memberSeatGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("policy:seatTypesToSmartsheet:member").save();
      memberSeatGroup.addMember(SubjectTestHelper.SUBJ0, false);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-p").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);
      group.addMember(SubjectTestHelper.SUBJ2, false);

      attachProvisioningAttribute(stem);
      fullProvision();

      assertEquals("MEMBER", userSeatType(subj0Email));
      // in no seat group: the default reclaims the paid seat
      assertEquals("CONTRIBUTOR", userSeatType(subj1Email));
      // provisional members are left alone by default
      assertEquals("PROVISIONAL_MEMBER", userSeatType(subj2Email));

      // a repeat sync is stable
      fullProvision();
      assertEquals("PROVISIONAL_MEMBER", userSeatType(subj2Email));

      // once allowed, the provisional member is downgraded
      new edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + PROVISIONER_ID + ".smartsheetDowngradeProvisionalMembers").value("true").store();
      edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase.clearCache();
      fullProvision();
      assertEquals("CONTRIBUTOR", userSeatType(subj2Email));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Flag groups: a configured flag is owned by Grouper (true for group members, false for everyone
   * else provisioned); a flag whose group is not configured is never changed.
   */
  public void testFlagGroups() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("smartsheetAdminGroupName", "policy:smartsheetRoles:systemAdmin")
        .addExtraConfig("smartsheetResourceViewerGroupName", "policy:smartsheetRoles:resourceViewer");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");
      insertMockUser("1000000000000601", subj0Email, "ACTIVE");
      insertMockUser("1000000000000602", subj1Email, "ACTIVE");
      // SUBJ1 was made admin and group admin by hand in Smartsheet
      new GcDbAccess().connectionName("grouper")
          .sql("update mock_smartsheet_user set admin = 'T', group_admin = 'T', licensed_sheet_creator = 'T' where id = ?")
          .addBindVar("1000000000000602").executeSql();

      Group adminGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("policy:smartsheetRoles:systemAdmin").save();
      adminGroup.addMember(SubjectTestHelper.SUBJ0, false);
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("policy:smartsheetRoles:resourceViewer").save();

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-f").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      // start the change log consumer so the later incremental sees the admin group change
      initIncrementalState(false);
      attachProvisioningAttribute(stem);
      fullProvision();

      assertEquals("T", userFlag(subj0Email, "admin"));
      // Grouper owns admin: SUBJ1 is not in the admin group
      assertEquals("F", userFlag(subj1Email, "admin"));
      // groupAdmin has no group configured: untouched
      assertEquals("T", userFlag(subj1Email, "group_admin"));
      assertEquals("F", userFlag(subj0Email, "resource_viewer"));

      // incremental: SUBJ1 into the admin group
      adminGroup.addMember(SubjectTestHelper.SUBJ1, false);
      incrementalProvision();
      assertEquals("T", userFlag(subj1Email, "admin"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * A configured group that does not exist fails the sync instead of quietly unassigning everyone.
   */
  public void testMissingSeatGroupFails() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("smartsheetPlanId", PLAN_ID)
        .addExtraConfig("smartsheetSeatTypeMemberGroupName", "policy:seatTypesToSmartsheet:typo");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockUser("1000000000000701", SubjectTestHelper.SUBJ0.getAttributeValue("email"), "ACTIVE", "MEMBER", true);
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-m").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      try {
        fullProvision();
        fail("a missing seat group should fail the sync");
      } catch (RuntimeException e) {
        assertTrue(GrouperUtil.getFullStackTrace(e), GrouperUtil.getFullStackTrace(e).contains("typo"));
      }
      assertEquals("MEMBER", userSeatType(SubjectTestHelper.SUBJ0.getAttributeValue("email")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Sync-back (Tomcat required)
  // =============================================

  private int countSyncBack(String tableName) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName
            + " where grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(PROVISIONER_ID).select(int.class);
  }

  private boolean inSyncBack(String tableName, String idColumn, String targetId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(*) from " + tableName + " where " + idColumn + " = ?"
            + " and grouper_sync_internal_id in (select internal_id from grouper_sync where provisioner_name = ?)")
        .addBindVar(targetId).addBindVar(PROVISIONER_ID).select(int.class) > 0;
  }

  private static Integer debugMapInt(Map<String, Object> debugMap, String key) {
    Object value = debugMap == null ? null : debugMap.get(key);
    return value == null ? null : ((Number) value).intValue();
  }

  /** sync-back config: mirror users, groups and memberships */
  private SmartsheetProvisionerTestConfigInput syncBackConfig(boolean fullSyncFromCache) {
    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true")
        .addExtraConfig("loadGroupsToGenericGrouperTable", "true")
        .addExtraConfig("loadMembershipsToGenericGrouperTable", "true");
    if (fullSyncFromCache) {
      configInput.addExtraConfig("fullSyncUsersFromSyncBack", "true")
          .addExtraConfig("fullSyncGroupsFromSyncBack", "true")
          .addExtraConfig("fullSyncMembershipsFromSyncBack", "true");
    }
    return configInput;
  }

  /**
   * The mirror is the full view of Smartsheet: ignored users, deactivated users and ignored groups
   * (with their members) are mirrored too, even though the provisioner treats them as absent.
   */
  public void testSyncBackMirrorIsFullView() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = syncBackConfig(false)
        .addExtraConfig("smartsheetIgnoreUserEmails", "svc-smartsheet@example.edu")
        .addExtraConfig("smartsheetIgnoreGroupNames", "manual-group");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockUser("1000000000000801", "svc-smartsheet@example.edu", "ACTIVE");
      insertMockUser("1000000000000802", "gone@example.edu", "DEACTIVATED");
      insertMockGroup("2000000000000801", "manual-group");
      insertMockMembership("2000000000000801", "1000000000000801");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-v").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      fullProvision();

      assertTrue("ignored user is mirrored", inSyncBack("grouper_prov_user", "target_user_id", "1000000000000801"));
      assertTrue("deactivated user is mirrored", inSyncBack("grouper_prov_user", "target_user_id", "1000000000000802"));
      assertTrue("ignored group is mirrored", inSyncBack("grouper_prov_group", "target_group_id", "2000000000000801"));
      // SUBJ0 in team-v, plus the ignored group's member
      assertEquals(2, countSyncBack("grouper_prov_mship"));
      // and the provisioner still left them alone
      assertEquals("ACTIVE", userStatus("svc-smartsheet@example.edu"));
      assertEquals(1, membershipCount("2000000000000801"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Warm cache: a full sync is served from the mirror (no user pull, no group list, no per-group
   * member reads), and it still provisions changes.
   */
  public void testFullSyncFromSyncBackWarmCache() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(syncBackConfig(true));

    try {
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-w").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);
      attachProvisioningAttribute(stem);

      // warm the cache (the first run is a cold, live read)
      fullProvision();
      fullProvision();
      fullProvision();
      String groupId = groupIdByName("team-w");
      assertEquals(2, membershipCount(groupId));
      assertEquals(2, countSyncBack("grouper_prov_user"));
      assertEquals(1, countSyncBack("grouper_prov_group"));
      assertEquals(2, countSyncBack("grouper_prov_mship"));

      // the warm run comes entirely from the cache
      fullProvision();
      Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
      assertNull("user pull skipped", debugMap.get("smartsheetRetrieveAllUsersApiCall"));
      assertNull("group list skipped", debugMap.get("smartsheetRetrieveAllGroupsApiCall"));
      assertNull("per-group member reads skipped", debugMap.get("smartsheetRetrieveMembershipsApiCall"));
      assertEquals(2, membershipCount(groupId));

      // a change still provisions from the cache-served view
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals(1, membershipCount(groupId));
      assertFalse(isMember(groupId, SubjectTestHelper.SUBJ1.getAttributeValue("email")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * A full sync served from the full-view mirror applies the same rules as a live read: a deactivated
   * user is treated as absent (not deactivated again, and reactivated when re-added), and an ignored
   * out-of-band group member is left alone.
   */
  public void testFullSyncFromSyncBackAppliesSameRules() {
    if (!tomcatRunTests()) {
      return;
    }

    String svcEmail = "svc-smartsheet@example.edu";
    SmartsheetProvisionerTestConfigInput configInput = syncBackConfig(true)
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true")
        .addExtraConfig("smartsheetIgnoreUserEmails", svcEmail);
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // the Smartsheet group already exists (Grouper links to it by name) with an out-of-band ignored
      // member. It must be there before the first (cold, live) run: once users are served from the
      // cache, a user added outside Grouper only reaches the mirror on a live read
      String groupId = "2000000000000901";
      insertMockGroup(groupId, "team-r2");
      insertMockUser("1000000000000901", svcEmail, "ACTIVE");
      insertMockMembership(groupId, "1000000000000901");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-r2").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);
      attachProvisioningAttribute(stem);

      // warm the cache (the first run is a cold, live read)
      fullProvision();
      fullProvision();
      fullProvision();
      assertTrue("ignored user is mirrored", inSyncBack("grouper_prov_user", "target_user_id", "1000000000000901"));

      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

      // SUBJ1 leaves: deactivated, membership removed
      group.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals("DEACTIVATED", userStatus(subj1Email));
      assertFalse(isMember(groupId, subj1Email));

      // served from the cache, where SUBJ1 (deactivated) and the ignored user are mirrored: the same
      // rules drop them from the compare, so nothing changes
      fullProvision();
      Map<String, Object> debugMap = GrouperProvisioner.retrieveInternalLastProvisioner().getDebugMap();
      assertNull("user pull skipped", debugMap.get("smartsheetRetrieveAllUsersApiCall"));
      assertNull("per-group member reads skipped", debugMap.get("smartsheetRetrieveMembershipsApiCall"));
      assertTrue("deactivated and ignored users filtered from the cache view: " + debugMap,
          debugMapInt(debugMap, "syncBackEntitiesFilteredByDao") != null && debugMapInt(debugMap, "syncBackEntitiesFilteredByDao") >= 2);
      assertEquals("DEACTIVATED", userStatus(subj1Email));
      assertEquals("ACTIVE", userStatus(svcEmail));
      assertTrue("ignored member left alone", isMember(groupId, svcEmail));

      // SUBJ1 back: reactivated (not duplicated) from a cache-served run, membership back
      group.addMember(SubjectTestHelper.SUBJ1, false);
      fullProvision();
      assertEquals(1, userCount(subj1Email));
      assertEquals("ACTIVE", userStatus(subj1Email));
      assertTrue(isMember(groupId, subj1Email));
      assertTrue(isMember(groupId, svcEmail));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Link-only mode: insertEntities=false plus errorHandlingTargetObjectDoesNotExistIsAnError=false.
   * Grouper never adds users to the org; a member who has no Smartsheet user yet is skipped quietly
   * (no error) and is added to the group by the first full sync after they show up in Smartsheet
   * (e.g. after their first SSO login).
   */
  public void testLinkOnlyMode() {
    if (!tomcatRunTests()) {
      return;
    }

    SmartsheetProvisionerTestConfigInput configInput = new SmartsheetProvisionerTestConfigInput()
        .assignConfigId(PROVISIONER_ID)
        .addExtraConfig("insertEntities", "false")
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

      // SUBJ0 already uses Smartsheet, SUBJ1 has never logged in
      insertMockUser("1000000000000301", subj0Email, "ACTIVE");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:smartsheet:team-l").save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
      group.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // no allowErrors: a missing user must not count as an error in this mode
      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      String groupId = groupIdByName("team-l");
      assertNotNull(groupId);
      assertTrue(isMember(groupId, subj0Email));
      assertEquals("Grouper must not add users in link-only mode", 0, userCount(subj1Email));
      assertEquals(1, membershipCount(groupId));

      // SUBJ1 logs in (Smartsheet adds them), the next full sync puts them in the group
      insertMockUser("1000000000000302", subj1Email, "ACTIVE");
      fullProvision();
      assertTrue(isMember(groupId, subj1Email));
      assertEquals(2, membershipCount(groupId));

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
