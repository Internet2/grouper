package edu.internet2.middleware.grouper.app.ccure;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureGroup;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureMembership;
import edu.internet2.middleware.grouper.app.ccure.CCureTargetDao.CCureUser;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Tests for the CCure provisioner, against the CCure mock service.
 *
 * <p><b>CCure is membership-only.</b> Personnel and Clearances are managed in CCure itself -- this
 * provisioner never inserts, updates, or deletes them.  The only thing Grouper writes is the
 * PersonnelClearancePair that grants a person a clearance.  So every test here pre-seeds
 * mock_ccure_personnel and mock_ccure_clearance, then asserts on mock_ccure_clearance_pair.
 *
 * <p><b>Everything needs Tomcat.</b> Unlike some other provisioner tests, every test in this class
 * -- including the API-level ones -- is gated behind {@link #tomcatRunTests()}, because the CCure
 * commands have no non-HTTP path: they always go through the mock service servlet.  Set
 * {@code junit.test.tomcat = true} (and {@code grouper.is.mockServices = true} in
 * grouper.hibernate.properties) to run them.
 *
 * <p><b>Run the mock Tomcat with the daemon off.</b> A daemon running against the same registry can
 * race fullProvision's inline change log convert and blow up on a PIT duplicate key.  That is an
 * environment problem, not a provisioner bug.
 */
public class CCureProvisionerTest extends GrouperProvisioningBaseTest {

  /** provisioner config id */
  private static final String PROVISIONER_CONFIG_ID = "ccureProvisioner";

  /** ids seeded by the tests; CCure ids are integers */
  private static final int PERSONNEL_ID_0 = 1001;
  private static final int PERSONNEL_ID_1 = 1002;
  private static final int PERSONNEL_ID_2 = 1003;

  private static final int CLEARANCE_ID_0 = 2001;
  private static final int CLEARANCE_ID_1 = 2002;

  public static void main(String[] args) {
    TestRunner.run(new CCureProvisionerTest("testRetrieveGroups"));
    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return PROVISIONER_CONFIG_ID;
  }

  public CCureProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    if (!tomcatRunTests()) {
      return;
    }

    CCureMockServiceHandler.ensureCcureMockTables();

    clearMockTables();

    // writes every external system config this class needs, once per JVM.  No test may write
    // external system config itself: the mock reads it from the Tomcat JVM, and a write immediately
    // before a login races Tomcat's config reload and comes back 401 "User not in system".
    CCureProvisionerTestUtils.setupCcureExternalSystem();
  }

  private void clearMockTables() {
    new GcDbAccess().connectionName("grouper").sql("delete from mock_ccure_clearance_pair").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_ccure_personnel").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_ccure_clearance").executeSql();
  }

  // =============================================
  // Mock seeding helpers
  // =============================================

  /**
   * Insert a Personnel row.  int1 holds the subject id, which is what the provisioner matches on.
   */
  private void insertMockPersonnel(int personnelId, String name, String int1) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_ccure_personnel (personnel_id, guid, name, int1) values (?, ?, ?, ?)")
        .addBindVar(personnelId).addBindVar("guid-personnel-" + personnelId).addBindVar(name).addBindVar(int1)
        .executeSql();
  }

  /**
   * Insert a Clearance row.  name is what the provisioner matches against the group display extension.
   */
  private void insertMockClearance(int objectId, String name) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_ccure_clearance (object_id, guid, name, partition_id) values (?, ?, ?, ?)")
        .addBindVar(objectId).addBindVar("guid-clearance-" + objectId).addBindVar(name).addBindVar("1")
        .executeSql();
  }

  /** Insert a PersonnelClearancePair row directly, bypassing the provisioner. */
  private void insertMockPair(int objectId, int personnelId, int clearanceId) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_ccure_clearance_pair (object_id, personnel_id, clearance_id) values (?, ?, ?)")
        .addBindVar(objectId).addBindVar(personnelId).addBindVar(clearanceId)
        .executeSql();
  }

  /** Seed the three test subjects as Personnel, with int1 = subject id. */
  private void insertMockPersonnelForTestSubjects() {
    insertMockPersonnel(PERSONNEL_ID_0, "my name is test.subject.0", SubjectTestHelper.SUBJ0_ID);
    insertMockPersonnel(PERSONNEL_ID_1, "my name is test.subject.1", SubjectTestHelper.SUBJ1_ID);
    insertMockPersonnel(PERSONNEL_ID_2, "my name is test.subject.2", SubjectTestHelper.SUBJ2_ID);
  }

  private int countPairs() {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_ccure_clearance_pair").select(int.class).intValue();
  }

  private int countPairsForPersonnel(int personnelId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_ccure_clearance_pair where personnel_id = ?")
        .addBindVar(personnelId).select(int.class).intValue();
  }

  /**
   * A freshly authenticated external system.
   *
   * <p>Deliberately NOT {@code CCureApiCommands.retrieveExternalSystem(configId)}: that caches the
   * external system (and its session) in a static map keyed by configId, so a test that changes the
   * config would keep talking to the previous session.  Tests build their own.
   */
  private CCureExternalSystem ccureExternalSystem() {
    return ccureExternalSystem(CCureProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID);
  }

  /**
   * A freshly authenticated external system on a specific configId.  All the configs are written
   * up front by {@code setupCcureExternalSystem}; a test picks one rather than writing config.
   * @param configId
   * @return the authenticated external system
   */
  private CCureExternalSystem ccureExternalSystem(String configId) {
    CCureExternalSystem externalSystem = new CCureExternalSystem();
    externalSystem.setConfigId(configId);
    externalSystem.authenticate();
    return externalSystem;
  }

  // =============================================
  // External system tests
  // =============================================

  /** A good login gets a session, and logout clears it. */
  public void testAuthenticateAndLogout() {
    if (!tomcatRunTests()) {
      return;
    }

    CCureExternalSystem externalSystem = ccureExternalSystem();
    assertNotNull("should have a session id after login", externalSystem.getSessionId());

    externalSystem.logout();
    assertNull("logout should clear the session id", externalSystem.getSessionId());
  }

  /**
   * Bad credentials fail the login rather than silently proceeding.
   *
   * <p>NB the client posts from a SEPARATE config here.  The mock validates against
   * {@code EXTERNAL_SYSTEM_CONFIG_ID}'s config, which is the same config the client normally posts
   * from -- so simply changing the password there moves both sides at once and the login still
   * succeeds.  The good config is left untouched, so nothing has to be restored afterwards.
   */
  public void testAuthenticateBadPassword() {
    if (!tomcatRunTests()) {
      return;
    }

    CCureExternalSystem externalSystem = new CCureExternalSystem();
    externalSystem.setConfigId(CCureProvisionerTestUtils.BAD_PASSWORD_CONFIG_ID);

    try {
      externalSystem.authenticate();
      fail("should not authenticate with the wrong password");
    } catch (RuntimeException e) {
      assertTrue("message should mention the failure: " + e.getMessage(),
          e.getMessage().contains("CCure authentication failed"));
      assertTrue("should have been rejected with a 401: " + e.getMessage(),
          e.getMessage().contains("code=401"));
    }

    assertNull("a failed login must not leave a session id", externalSystem.getSessionId());
  }

  /** test() reports no errors against a healthy mock. */
  public void testExternalSystemTestConnection() {
    if (!tomcatRunTests()) {
      return;
    }

    CCureExternalSystem externalSystem = new CCureExternalSystem();
    externalSystem.setConfigId(CCureProvisionerTestUtils.EXTERNAL_SYSTEM_CONFIG_ID);

    List<String> errors = externalSystem.test();
    assertEquals("expected no errors but got: " + errors, 0, GrouperUtil.length(errors));
  }

  // =============================================
  // Group (Clearance) API tests
  // =============================================

  public void testRetrieveGroups() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockClearance(CLEARANCE_ID_1, "clearanceOne");

    List<CCureGroup> groups = CCureApiCommands.retrieveGroups(ccureExternalSystem());

    assertEquals(2, GrouperUtil.length(groups));

    Map<Integer, CCureGroup> groupByObjectId = new HashMap<Integer, CCureGroup>();
    for (CCureGroup group : groups) {
      groupByObjectId.put(Integer.valueOf(group.objectId()), group);
    }

    CCureGroup group0 = groupByObjectId.get(Integer.valueOf(CLEARANCE_ID_0));
    assertNotNull(group0);
    assertEquals("clearanceZero", group0.name());
    assertEquals("guid-clearance-" + CLEARANCE_ID_0, group0.guid());
    assertEquals("1", group0.partitionKey());

    assertNotNull(groupByObjectId.get(Integer.valueOf(CLEARANCE_ID_1)));
  }

  public void testRetrieveGroupsNone() {
    if (!tomcatRunTests()) {
      return;
    }

    List<CCureGroup> groups = CCureApiCommands.retrieveGroups(ccureExternalSystem());
    assertEquals(0, GrouperUtil.length(groups));
  }

  public void testRetrieveGroupByName() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockClearance(CLEARANCE_ID_1, "clearanceOne");

    CCureGroup group = CCureApiCommands.retrieveGroupByName(ccureExternalSystem(), "clearanceOne");

    assertNotNull(group);
    assertEquals(CLEARANCE_ID_1, group.objectId());
    assertEquals("clearanceOne", group.name());
  }

  public void testRetrieveGroupByObjectId() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");

    CCureGroup group = CCureApiCommands.retrieveGroupByObjectId(ccureExternalSystem(), "" + CLEARANCE_ID_0);

    assertNotNull(group);
    assertEquals(CLEARANCE_ID_0, group.objectId());
    assertEquals("clearanceZero", group.name());
  }

  // =============================================
  // Entity (Personnel) API tests
  // =============================================

  public void testRetrieveUsers() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    List<CCureUser> users = CCureApiCommands.retrieveUsers(ccureExternalSystem());

    assertEquals(3, GrouperUtil.length(users));

    Map<Integer, CCureUser> userByPersonnelId = new HashMap<Integer, CCureUser>();
    for (CCureUser user : users) {
      userByPersonnelId.put(Integer.valueOf(user.personnelId()), user);
    }

    CCureUser user0 = userByPersonnelId.get(Integer.valueOf(PERSONNEL_ID_0));
    assertNotNull(user0);
    assertEquals(SubjectTestHelper.SUBJ0_ID, user0.int1());
    assertEquals("my name is test.subject.0", user0.name());
    assertEquals("guid-personnel-" + PERSONNEL_ID_0, user0.guid());
  }

  /**
   * The Personnel read is paged.  With a page size of 2 and 3 rows the loop has to make more than
   * one call and still return every row exactly once.
   */
  public void testRetrieveUsersPaging() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    List<CCureUser> users = CCureApiCommands.retrieveUsers(
        ccureExternalSystem(CCureProvisionerTestUtils.SMALL_PAGES_CONFIG_ID));

    assertEquals("paging must not drop or duplicate rows", 3, GrouperUtil.length(users));
  }

  public void testRetrieveUsersNone() {
    if (!tomcatRunTests()) {
      return;
    }

    List<CCureUser> users = CCureApiCommands.retrieveUsers(ccureExternalSystem());
    assertEquals(0, GrouperUtil.length(users));
  }

  public void testRetrieveEntityByObjectId() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    CCureUser user = CCureApiCommands.retrieveEntityByObjectId(ccureExternalSystem(), "" + PERSONNEL_ID_1);

    assertNotNull(user);
    assertEquals(PERSONNEL_ID_1, user.personnelId());
    assertEquals(SubjectTestHelper.SUBJ1_ID, user.int1());
  }

  public void testRetrieveEntityByMatchField() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    CCureUser user = CCureApiCommands.retrieveEntityByMatchField(
        ccureExternalSystem(), "Int1", "'" + SubjectTestHelper.SUBJ2_ID + "'");

    assertNotNull(user);
    assertEquals(PERSONNEL_ID_2, user.personnelId());
  }

  // =============================================
  // Membership (PersonnelClearancePair) API tests
  // =============================================

  public void testRetrieveMemberships() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockClearance(CLEARANCE_ID_1, "clearanceOne");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);
    insertMockPair(3003, PERSONNEL_ID_1, CLEARANCE_ID_1);

    List<CCureMembership> memberships = CCureApiCommands.retrieveMemberships(ccureExternalSystem());

    assertEquals(3, GrouperUtil.length(memberships));

    boolean found = false;
    for (CCureMembership membership : memberships) {
      if (membership.objectId() == 3003) {
        assertEquals(PERSONNEL_ID_1, membership.personnelId());
        assertEquals(CLEARANCE_ID_1, membership.clearanceId());
        found = true;
      }
    }
    assertTrue("should have found the pair with object id 3003", found);
  }

  /** The clearance-pair read is paged too. */
  public void testRetrieveMembershipsPaging() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);
    insertMockPair(3003, PERSONNEL_ID_2, CLEARANCE_ID_0);

    List<CCureMembership> memberships = CCureApiCommands.retrieveMemberships(
        ccureExternalSystem(CCureProvisionerTestUtils.SMALL_PAGES_CONFIG_ID));

    assertEquals("paging must not drop or duplicate rows", 3, GrouperUtil.length(memberships));
  }

  public void testRetrieveMembershipsForUser() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockClearance(CLEARANCE_ID_1, "clearanceOne");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);
    insertMockPair(3003, PERSONNEL_ID_1, CLEARANCE_ID_1);

    List<CCureMembership> memberships =
        CCureApiCommands.retrieveMembershipsForUser(ccureExternalSystem(), "" + PERSONNEL_ID_1);

    assertEquals(2, GrouperUtil.length(memberships));
    for (CCureMembership membership : memberships) {
      assertEquals(PERSONNEL_ID_1, membership.personnelId());
    }
  }

  public void testRetrieveMembershipsForGroup() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockClearance(CLEARANCE_ID_1, "clearanceOne");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);
    insertMockPair(3003, PERSONNEL_ID_1, CLEARANCE_ID_1);

    List<CCureMembership> memberships =
        CCureApiCommands.retrieveMembershipsForGroup(ccureExternalSystem(), "" + CLEARANCE_ID_0);

    assertEquals(2, GrouperUtil.length(memberships));
    for (CCureMembership membership : memberships) {
      assertEquals(CLEARANCE_ID_0, membership.clearanceId());
    }
  }

  /**
   * The lookup the delete path uses when an incremental does not carry the pair ObjectID.
   */
  public void testRetrieveMembershipByGroupAndUser() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);

    CCureMembership membership = CCureApiCommands.retrieveMembershipByGroupAndUser(
        ccureExternalSystem(), "" + CLEARANCE_ID_0, "" + PERSONNEL_ID_1);

    assertNotNull(membership);
    assertEquals(3002, membership.objectId());
    assertEquals(PERSONNEL_ID_1, membership.personnelId());
    assertEquals(CLEARANCE_ID_0, membership.clearanceId());
  }

  public void testInsertMembershipsForUser() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");

    assertEquals(0, countPairs());

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Exception exception = CCureApiCommands.insertMembershipsForUser(
        ccureExternalSystem(), debugMap, "" + PERSONNEL_ID_0, GrouperUtil.toList("" + CLEARANCE_ID_0));

    assertNull("insert should not have returned an exception: " + exception, exception);
    assertEquals(1, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_0));
  }

  /** A blank clearance list is a no-op, not a call. */
  public void testInsertMembershipsForUserEmptyList() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Exception exception = CCureApiCommands.insertMembershipsForUser(
        ccureExternalSystem(), debugMap, "" + PERSONNEL_ID_0, GrouperUtil.toList(new String[] {}));

    assertNull(exception);
    assertEquals(0, countPairs());
  }

  /** An unknown clearance comes back as an exception on the return, not a thrown error. */
  public void testInsertMembershipsForUserUnknownClearance() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Exception exception = CCureApiCommands.insertMembershipsForUser(
        ccureExternalSystem(), debugMap, "" + PERSONNEL_ID_0, GrouperUtil.toList("987654"));

    assertNotNull("an unknown clearance should surface as a returned exception", exception);
    assertEquals(0, countPairs());
  }

  public void testDeleteMembershipsForUser() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");
    insertMockPair(3001, PERSONNEL_ID_0, CLEARANCE_ID_0);
    insertMockPair(3002, PERSONNEL_ID_1, CLEARANCE_ID_0);

    assertEquals(2, countPairs());

    // delete is keyed on the pair ObjectID, not the clearance id
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Exception exception = CCureApiCommands.deleteMembershipsForUser(
        ccureExternalSystem(), debugMap, "" + PERSONNEL_ID_0, GrouperUtil.toList("3001"));

    assertNull("delete should not have returned an exception: " + exception, exception);
    assertEquals(1, countPairs());
    assertEquals(0, countPairsForPersonnel(PERSONNEL_ID_0));
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_1));
  }

  /** Deleting a pair that is not there surfaces as a returned exception. */
  public void testDeleteMembershipsForUserNotFound() {
    if (!tomcatRunTests()) {
      return;
    }

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "clearanceZero");

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    Exception exception = CCureApiCommands.deleteMembershipsForUser(
        ccureExternalSystem(), debugMap, "" + PERSONNEL_ID_0, GrouperUtil.toList("999999"));

    assertNotNull("deleting a missing pair should surface as a returned exception", exception);
  }

  // =============================================
  // Helper methods for provisioner tests
  // =============================================

  private CCureProvisionerTestConfigInput ccureProvisionerConfig() {
    return new CCureProvisionerTestConfigInput().assignConfigId(PROVISIONER_CONFIG_ID);
  }

  /**
   * Set up the provisioner, restart, and clear the mock tables so each test starts from a known
   * state.  Returns a root session.
   */
  private GrouperSession setupProvisionerTest(CCureProvisionerTestConfigInput configInput) {
    CCureProvisionerTestUtils.setupCcureExternalSystem();
    CCureProvisionerTestUtils.configureCcureProvisioner(configInput);

    // the provisioner config is read in this JVM (the provisioner runs in-process), so the cache
    // clear in configureCcureProvisioner covers it; this settle is for the loader config generally
    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    CCureMockServiceHandler.ensureCcureMockTables();
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

  /**
   * Establish incremental baseline state, so an incremental run has something to diff against.
   */
  private void initIncrementalState(boolean isFull) {
    if (!isFull) {
      fullProvision();
      incrementalProvision();
    }
  }

  /**
   * Run a full sync that is expected to fail the daemon job, and return nothing but the assertion
   * that it did.  Used for the "target object does not exist" cases: CCure never creates Clearances
   * or Personnel, so a group or member with no counterpart in CCure is a DNE error, and with the
   * default {@code errorHandlingProvisionerDaemonShouldFailOnObjectError} that fails the job.
   */
  private void fullProvisionExpectingObjectError() {
    try {
      fullProvision();
      fail("expected the daemon job to fail because a target object does not exist");
    } catch (RuntimeException e) {
      String message = GrouperUtil.getFullStackTrace(e);
      assertTrue("expected a does-not-exist object error, got: " + e.getMessage(),
          message.contains("error DNE count"));
    }
  }

  private void attachProvisioningAttribute(Stem stem) {
    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(PROVISIONER_CONFIG_ID);
    attributeValue.setTargetName(PROVISIONER_CONFIG_ID);
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

  // =============================================
  // Membership add / remove / re-add
  // =============================================

  public void testFullSyncMembershipAddRemoveReAdd() {
    membershipAddRemoveReAdd(true);
  }

  public void testIncrementalMembershipAddRemoveReAdd() {
    membershipAddRemoveReAdd(false);
  }

  /**
   * The core CCure flow: members of a Grouper group become clearance pairs, removing a member drops
   * its pair, and re-adding the same member creates a fresh pair.
   */
  public void membershipAddRemoveReAdd(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig());

    // Personnel and the Clearance must already exist - Grouper never creates them.  The clearance
    // name has to match the group display extension for the group to link up.
    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "testGroup");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    initIncrementalState(isFull);
    attachProvisioningAttribute(stem);

    assertEquals(0, countPairs());

    // the first provision always needs a full sync to establish the baseline
    fullProvision();

    assertEquals("both members should have been paired to the clearance", 2, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_0));
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_1));

    // the clearance and personnel rows must be untouched - this provisioner only writes pairs
    assertEquals(Integer.valueOf(1), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_ccure_clearance").select(int.class));
    assertEquals(Integer.valueOf(3), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_ccure_personnel").select(int.class));

    // remove a member
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);

    provision(isFull);

    assertEquals(1, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_0));
    assertEquals(0, countPairsForPersonnel(PERSONNEL_ID_1));

    // add a different member
    testGroup.addMember(SubjectTestHelper.SUBJ2, false);

    provision(isFull);

    assertEquals(2, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_2));

    // re-add the member that was removed
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    provision(isFull);

    assertEquals(3, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_1));
  }

  // =============================================
  // Deleting the Grouper group removes the pairs but not the clearance
  // =============================================

  public void testFullSyncGroupDeleteLeavesClearance() {
    groupDeleteLeavesClearance(true);
  }

  public void testIncrementalGroupDeleteLeavesClearance() {
    groupDeleteLeavesClearance(false);
  }

  /**
   * Deleting the Grouper group must drop its clearance pairs and leave the Clearance itself alone
   * -- deleteGroups is false, because clearances belong to CCure.
   */
  public void groupDeleteLeavesClearance(boolean isFull) {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig());

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "testGroup");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    initIncrementalState(isFull);
    attachProvisioningAttribute(stem);

    fullProvision();

    assertEquals(2, countPairs());

    testGroup.delete();

    provision(isFull);

    assertEquals("the pairs should be gone", 0, countPairs());
    assertEquals("the clearance must survive - Grouper does not delete clearances",
        Integer.valueOf(1), new GcDbAccess().connectionName("grouper")
            .sql("select count(1) from mock_ccure_clearance").select(int.class));
  }

  // =============================================
  // A group with no matching clearance provisions nothing
  // =============================================

  /**
   * A group with no matching Clearance is an ERROR, not a silent skip.  CCure cannot create
   * Clearances (insertGroups is false), so the group is "missing from the target and cannot be
   * created" -- which is what {@code errorHandlingTargetObjectDoesNotExistIsAnError} means, and it
   * defaults to true.  That is the right default: it surfaces the misconfiguration rather than
   * quietly provisioning nothing.  Nothing is written either way.
   */
  public void testFullSyncNoMatchingClearanceIsAnError() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig());

    insertMockPersonnelForTestSubjects();
    // deliberately NOT inserting a clearance named "testGroup"
    insertMockClearance(CLEARANCE_ID_1, "someOtherClearance");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    attachProvisioningAttribute(stem);

    fullProvisionExpectingObjectError();

    assertEquals("no clearance to pair to, so no pairs", 0, countPairs());
    assertEquals("and Grouper must not have created the clearance",
        Integer.valueOf(1), new GcDbAccess().connectionName("grouper")
            .sql("select count(1) from mock_ccure_clearance").select(int.class));
  }

  /**
   * The same case with {@code errorHandlingTargetObjectDoesNotExistIsAnError} turned off: the run
   * succeeds and the unprovisionable group is skipped.  This is the setting a deployment needs if
   * some provisionable groups legitimately have no Clearance yet.
   */
  public void testFullSyncNoMatchingClearanceToleratedWhenNotAnError() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig()
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false"));

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_1, "someOtherClearance");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    attachProvisioningAttribute(stem);

    fullProvision();

    assertEquals("no clearance to pair to, so no pairs", 0, countPairs());
  }

  // =============================================
  // A member with no matching Personnel provisions nothing for that member
  // =============================================

  /**
   * A member with no Personnel record is also a does-not-exist error under the defaults, for the
   * same reason as a missing Clearance: CCure never creates Personnel.
   *
   * <p>Worth knowing operationally: any provisionable group containing someone without a CCure
   * badge record will fail the daemon on stock config.  See the tolerated variant below.
   */
  public void testFullSyncMemberWithNoPersonnelIsAnError() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig());

    // only subject 0 has a Personnel record
    insertMockPersonnel(PERSONNEL_ID_0, "my name is test.subject.0", SubjectTestHelper.SUBJ0_ID);
    insertMockClearance(CLEARANCE_ID_0, "testGroup");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    attachProvisioningAttribute(stem);

    fullProvisionExpectingObjectError();

    assertEquals("and Grouper must not have created Personnel",
        Integer.valueOf(1), new GcDbAccess().connectionName("grouper")
            .sql("select count(1) from mock_ccure_personnel").select(int.class));
  }

  /**
   * The same case with the does-not-exist error switched off: the member who has a Personnel record
   * is provisioned and the one who does not is skipped, with the run succeeding.  This is the
   * behavior a real deployment wants, since not every group member will have a CCure badge.
   */
  public void testFullSyncMemberWithNoPersonnelSkippedWhenNotAnError() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig()
        .addExtraConfig("errorHandlingShow", "true")
        .addExtraConfig("errorHandlingTargetObjectDoesNotExistIsAnError", "false"));

    // only subject 0 has a Personnel record
    insertMockPersonnel(PERSONNEL_ID_0, "my name is test.subject.0", SubjectTestHelper.SUBJ0_ID);
    insertMockClearance(CLEARANCE_ID_0, "testGroup");

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    attachProvisioningAttribute(stem);

    fullProvision();

    assertEquals("only the subject with a Personnel record gets a pair", 1, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_0));
    assertEquals("and Grouper must not have created Personnel",
        Integer.valueOf(1), new GcDbAccess().connectionName("grouper")
            .sql("select count(1) from mock_ccure_personnel").select(int.class));
  }

  // =============================================
  // Pairs created outside Grouper are removed when deleteMembershipsIfNotExistInGrouper is on
  // =============================================

  public void testFullSyncRemovesUnmanagedPair() {

    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(ccureProvisionerConfig());

    insertMockPersonnelForTestSubjects();
    insertMockClearance(CLEARANCE_ID_0, "testGroup");

    // a pair that Grouper did not create, for a subject who is not in the group
    insertMockPair(3001, PERSONNEL_ID_2, CLEARANCE_ID_0);

    Stem stem = new StemSave(grouperSession).assignName("test").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    testGroup.addMember(SubjectTestHelper.SUBJ0, false);

    attachProvisioningAttribute(stem);

    assertEquals(1, countPairs());

    fullProvision();

    assertEquals("the unmanaged pair should be gone and the group member's pair added", 1, countPairs());
    assertEquals(1, countPairsForPersonnel(PERSONNEL_ID_0));
    assertEquals(0, countPairsForPersonnel(PERSONNEL_ID_2));
  }

}
