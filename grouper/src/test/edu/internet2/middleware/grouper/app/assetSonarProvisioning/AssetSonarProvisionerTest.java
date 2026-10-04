package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import junit.textui.TestRunner;

/**
 * Tests for the AssetSonar provisioner.
 *
 * <p>The first group needs nothing running: they cover the guards against the API's quirks
 * (blank values, external_id, ignored filters). Everything else talks HTTP to
 * {@link AssetSonarMockServiceHandler} in the test Tomcat and is gated on tomcatRunTests().</p>
 */
public class AssetSonarProvisionerTest extends GrouperProvisioningBaseTest {

  private static final String CONFIG_ID = AssetSonarProvisionerTestUtils.CONFIG_ID;

  /** test subject emails (test.subject.N@somewhere.someSchool.edu) */
  private static final String EMAIL0 = "test.subject.0@somewhere.someSchool.edu";
  private static final String EMAIL1 = "test.subject.1@somewhere.someSchool.edu";

  public static void main(String[] args) {
    GrouperStartup.startup();
    TestRunner.run(new AssetSonarProvisionerTest("testFullSyncTiersDeactivateReactivate"));
  }

  @Override
  public String defaultConfigId() {
    return "myAssetSonarProvisioner";
  }

  public AssetSonarProvisionerTest(String name) {
    super(name);
  }

  public AssetSonarProvisionerTest() {
  }

  @Override
  protected void setUp() {
    super.setUp();
    AssetSonarMockServiceHandler.ensureAssetSonarMockTables();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_asset_sonar_member").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from " + AssetSonarMockServiceHandler.FAULT_TABLE).executeSql();
    AssetSonarProvisionerTestUtils.setupAssetSonarExternalSystem();
    // retry tests would otherwise sleep 1s, 2s, 4s
    AssetSonarApiCommands.retrySleepMillisForTests = 10;
  }

  @Override
  protected void tearDown() {
    AssetSonarApiCommands.retrySleepMillisForTests = null;
    super.tearDown();
  }

  /**
   * Make the mock commit the next {@code count} successful writes for this email and then answer 502,
   * like the production gateway did.
   */
  private void schedule502(String email, int count) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into " + AssetSonarMockServiceHandler.FAULT_TABLE + " (email, remaining_502) values (?, ?)")
        .addBindVar(email.toLowerCase()).addBindVar(count).executeSql();
  }

  private int remaining502(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select remaining_502 from " + AssetSonarMockServiceHandler.FAULT_TABLE + " where email = ?")
        .addBindVar(email.toLowerCase()).select(int.class);
  }

  // =============================================
  // Mock seed and read helpers
  // =============================================

  private void insertMockMember(String id, String email, String roleId, String status) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_asset_sonar_member (id, email, first_name, last_name, role_id, status) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(id).addBindVar(email).addBindVar("First" + id).addBindVar("Last" + id)
        .addBindVar(roleId).addBindVar(status).executeSql();
  }

  private String mockColumn(String column, String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select " + column + " from mock_asset_sonar_member where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(String.class);
  }

  private int mockCount(String email) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_asset_sonar_member where lower(email) = ?")
        .addBindVar(email.toLowerCase()).select(int.class);
  }

  /**
   * Raw call to the mock, bypassing AssetSonarApiCommands, to prove the mock reproduces the real
   * API's behavior (otherwise the provisioner tests prove nothing).
   * @return {code, body}
   */
  private Object[] rawCall(String method, String pathAndQuery, Map<String, String> userParams) {
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignUrl(AssetSonarApiCommands.retrieveBaseUrl(CONFIG_ID) + pathAndQuery);
    grouperHttpClient.assignGrouperHttpMethod(method);
    grouperHttpClient.addHeader("token", AssetSonarProvisionerTestUtils.TEST_TOKEN);
    if (userParams != null) {
      for (String name : userParams.keySet()) {
        grouperHttpClient.addBodyParameter("user[" + name + "]", userParams.get(name));
      }
    }
    grouperHttpClient.executeRequest();
    return new Object[] {grouperHttpClient.getResponseCode(), grouperHttpClient.getResponseBody()};
  }

  // =============================================
  // Guards (no Tomcat needed)
  // =============================================

  public void testFilterUserParamsSkipsBlankValues() {
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "a@x.edu");
    params.put("first_name", "");
    params.put("last_name", null);
    params.put("role_id", "2");
    Map<String, String> filtered = AssetSonarApiCommands.filterUserParams(params);
    // a source data gap must not blank a good target value
    assertEquals(Arrays.asList("email", "role_id"), Arrays.asList(filtered.keySet().toArray()));
  }

  public void testFilterUserParamsRefusesExternalId() {
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "a@x.edu");
    params.put("external_id", "CN=someone");
    try {
      AssetSonarApiCommands.filterUserParams(params);
      fail("user[external_id] nulls the email and must never be sent");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("external_id"));
    }
  }

  public void testSelectEmailMatchDetectsIgnoredFilter() {
    AssetSonarMember match = new AssetSonarMember();
    match.setId("1");
    match.setEmail("A@x.edu");
    assertSame(match, AssetSonarApiCommands.selectEmailMatch(Arrays.asList(match), "a@X.edu"));
    assertNull(AssetSonarApiCommands.selectEmailMatch(new java.util.ArrayList<AssetSonarMember>(), "a@x.edu"));

    // an unfiltered page came back: must fail, not be read as "no match" or the wrong member
    AssetSonarMember other = new AssetSonarMember();
    other.setId("2");
    other.setEmail("b@x.edu");
    try {
      AssetSonarApiCommands.selectEmailMatch(Arrays.asList(match, other), "a@x.edu");
      fail("an unfiltered response must be detected");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("ignored filter=email"));
    }
  }

  public void testInactiveFilterIgnoredFailsLoudly() {
    AssetSonarMember inactive = new AssetSonarMember();
    inactive.setId("1");
    inactive.setStatus("0");
    AssetSonarApiCommands.assertInactiveFilterHonored(inactive);

    AssetSonarMember active = new AssetSonarMember();
    active.setId("2");
    active.setStatus("1");
    try {
      AssetSonarApiCommands.assertInactiveFilterHonored(active);
      fail("an active member in the inactive list means the filter was ignored");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("ignored filter=status"));
    }
  }


  // =============================================
  // Mock fidelity (Tomcat)
  // =============================================

  public void testMockIgnoresUnknownFilter() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("101", "a@x.edu", "2", "1");
    insertMockMember("102", "b@x.edu", "1734", "1");
    // role_id is not a supported filter: the real API returns the unfiltered page
    Object[] result = rawCall("GET", "/members.api?filter=role_id&filter_val=1734", null);
    assertEquals(200, result[0]);
    JsonNode members = GrouperUtil.jsonJacksonNode((String) result[1]).get("members");
    assertEquals(2, members.size());
  }

  public void testMockExternalIdNullsEmail() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("103", "c@x.edu", "2", "1");
    // also proves a form-encoded PUT works through GrouperHttpClient.addBodyParameter
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("external_id", "CN=c");
    Object[] result = rawCall("PUT", "/members/103.api", params);
    assertEquals(200, result[0]);
    AssetSonarMember member = AssetSonarApiCommands.retrieveMemberById(CONFIG_ID, "103");
    assertNull("user[external_id] nulls the email", member.getEmail());
  }

  // =============================================
  // API commands (Tomcat)
  // =============================================

  public void testRetrieveAllMembersPagesAndIncludesInactive() {
    if (!tomcatRunTests()) {
      return;
    }
    // more than one page (25/page)
    for (int i = 0; i < 30; i++) {
      insertMockMember(String.valueOf(1000 + i), "active" + i + "@x.edu", "2", "1");
    }
    insertMockMember("2001", "gone1@x.edu", "2", "0");
    insertMockMember("2002", "gone2@x.edu", "2", "0");

    assertEquals(2, AssetSonarApiCommands.retrieveMembersPage(CONFIG_ID, false, 1).getTotalPages());

    // the default list hides deactivated members
    Map<String, AssetSonarMember> activeOnly = AssetSonarApiCommands.retrieveAllMembers(CONFIG_ID, false);
    assertEquals(30, activeOnly.size());
    assertFalse(activeOnly.containsKey("2001"));

    Map<String, AssetSonarMember> all = AssetSonarApiCommands.retrieveAllMembers(CONFIG_ID, true);
    assertEquals(32, all.size());
    assertTrue(all.get("2001").isInactive());
  }

  public void testRetrieveMemberByEmailFindsInactive() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("301", "Hidden@x.edu", "2", "0");
    insertMockMember("302", "other@x.edu", "2", "1");
    AssetSonarMember member = AssetSonarApiCommands.retrieveMemberByEmail(CONFIG_ID, "hidden@x.edu");
    assertNotNull(member);
    assertEquals("301", member.getId());
    assertTrue(member.isInactive());
    assertNull(AssetSonarApiCommands.retrieveMemberByEmail(CONFIG_ID, "nobody@x.edu"));
  }

  public void testRetrieveMemberByIdNotFound() {
    if (!tomcatRunTests()) {
      return;
    }
    assertNull(AssetSonarApiCommands.retrieveMemberById(CONFIG_ID, "999999"));
  }

  public void testCreateAndDuplicateEmail() {
    if (!tomcatRunTests()) {
      return;
    }
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "new@x.edu");
    params.put("first_name", "New");
    params.put("last_name", "");
    params.put("role_id", "2");
    String memberId = AssetSonarApiCommands.createMember(CONFIG_ID, params);
    assertNotNull(memberId);
    AssetSonarMember created = AssetSonarApiCommands.retrieveMemberById(CONFIG_ID, memberId);
    assertEquals("New", created.getFirstName());
    assertNull(created.getLastName());

    // email of an INACTIVE member, different case: the real API answers HTTP 200 with
    // {"errors":...,"status":403}; createMember must still see "taken" (null), not a missing member_id
    insertMockMember("401", "Taken@x.edu", "2", "0");
    params.put("email", "taken@x.edu");
    assertNull(AssetSonarApiCommands.createMember(CONFIG_ID, params));
    assertEquals(1, mockCount("taken@x.edu"));

    // email of an ACTIVE member: a real HTTP 403, same result
    insertMockMember("402", "active@x.edu", "2", "1");
    params.put("email", "ACTIVE@x.edu");
    assertNull(AssetSonarApiCommands.createMember(CONFIG_ID, params));
    assertEquals(1, mockCount("active@x.edu"));
  }

  /** a create without role_id is refused by AssetSonar (400 "Role Id is invalid") */
  public void testCreateWithoutRoleIdRejected() {
    if (!tomcatRunTests()) {
      return;
    }
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "norole@x.edu");
    try {
      AssetSonarApiCommands.createMember(CONFIG_ID, params);
      fail("a create without role_id must fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("'400'"));
      assertTrue(e.getMessage(), e.getMessage().contains("Role Id is invalid"));
    }
    assertEquals(0, mockCount("norole@x.edu"));
  }

  /** a 2xx whose body carries "errors" is an error, with the status from the body (no network) */
  public void testErrorStatusFromBody() {
    assertEquals(403, AssetSonarApiCommands.errorStatusFromBody(200,
        "{\"errors\":{\"base\":[\"The email is already taken by an Inactive Member.\"]},\"status\":403}"));
    assertEquals(400, AssetSonarApiCommands.errorStatusFromBody(200, "{\"errors\":\"Role Id is invalid\"}"));
    assertEquals(-1, AssetSonarApiCommands.errorStatusFromBody(200, "{\"message\":\"Member created.\",\"member_id\":5}"));
    assertEquals(-1, AssetSonarApiCommands.errorStatusFromBody(403, "{\"errors\":{},\"status\":403}"));
    assertEquals(-1, AssetSonarApiCommands.errorStatusFromBody(200, "<h1>not json \"errors\"</h1>"));
    assertEquals(-1, AssetSonarApiCommands.errorStatusFromBody(200, null));
  }

  public void testUpdateMemberRequiresEmail() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("501", "upd@x.edu", "2", "1");
    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("role_id", "1734");
    try {
      AssetSonarApiCommands.updateMember(CONFIG_ID, "501", params);
      fail("every update must carry the email");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("email is required"));
    }
    params.put("email", "upd@x.edu");
    AssetSonarApiCommands.updateMember(CONFIG_ID, "501", params);
    assertEquals("1734", mockColumn("role_id", "upd@x.edu"));
    // partial update leaves other fields alone
    assertEquals("First501", mockColumn("first_name", "upd@x.edu"));
  }

  public void testExternalSystemConnectionTest() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("601", "t@x.edu", "2", "1");
    insertMockMember("602", "t2@x.edu", "2", "0");
    // the generic WsBearerToken test button, configured as documented for AssetSonar
    WsBearerTokenExternalSystem externalSystem = new WsBearerTokenExternalSystem();
    externalSystem.setConfigId(CONFIG_ID);
    List<String> errors = externalSystem.test();
    assertEquals(GrouperUtil.toStringForLog(errors), 0, GrouperUtil.length(errors));

    // a wrong token fails the test (401 instead of 200)
    Map<String, String> overrides = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    try {
      overrides.put("grouper.wsBearerToken." + CONFIG_ID + ".accessTokenPassword", "wrongToken");
      errors = externalSystem.test();
      assertTrue(GrouperUtil.toStringForLog(errors), GrouperUtil.length(errors) > 0);
    } finally {
      overrides.remove("grouper.wsBearerToken." + CONFIG_ID + ".accessTokenPassword");
    }
  }

  // =============================================
  // 502 retry (Tomcat)
  // =============================================

  /** an update that commits but loses its response to a 502 is retried and succeeds */
  public void testUpdateRetriesAfter502() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("701", "r1@x.edu", "2", "1");
    schedule502("r1@x.edu", 1);

    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "r1@x.edu");
    params.put("role_id", "1734");
    AssetSonarApiCommands.updateMember(CONFIG_ID, "701", params);

    assertEquals("1734", mockColumn("role_id", "r1@x.edu"));
    assertEquals("the 502 was used", 0, remaining502("r1@x.edu"));
  }

  /**
   * A create that commits but loses its response: the retry gets the duplicate-email 403, so
   * createMember returns null (the DAO then looks the member up and reactivates it) -- and there is
   * still exactly one member, no fork.
   */
  public void testCreateRetryAfterCommitted502() {
    if (!tomcatRunTests()) {
      return;
    }
    schedule502("new502@x.edu", 1);

    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "new502@x.edu");
    params.put("first_name", "New");
    params.put("role_id", "2");
    assertNull("retry lands on the 403 path", AssetSonarApiCommands.createMember(CONFIG_ID, params));
    assertEquals(1, mockCount("new502@x.edu"));
    assertEquals(0, remaining502("new502@x.edu"));
  }

  /** a 502 that never clears fails after the configured retries, with the attempts in the message */
  public void testRetriesExhausted() {
    if (!tomcatRunTests()) {
      return;
    }
    insertMockMember("702", "r2@x.edu", "2", "1");
    schedule502("r2@x.edu", 10);

    Map<String, String> params = new LinkedHashMap<String, String>();
    params.put("email", "r2@x.edu");
    params.put("role_id", "1734");
    try {
      AssetSonarApiCommands.updateMember(CONFIG_ID, "702", params);
      fail("a 502 on every attempt must fail");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("'502'"));
      assertTrue(e.getMessage(), e.getMessage().contains("after " + AssetSonarApiCommands.DEFAULT_RETRY_COUNT + " retries"));
    }
    // first attempt plus DEFAULT_RETRY_COUNT retries, and no more
    assertEquals(10 - 1 - AssetSonarApiCommands.DEFAULT_RETRY_COUNT, remaining502("r2@x.edu"));
  }

  /**
   * Full sync with a lost response on both an insert and an update: no errors, the inserted member is
   * not duplicated and is active, the update landed, and the next sync has nothing to do.
   */
  public void testFullSyncSurvives502() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new AssetSonarProvisionerTestConfigInput());

    try {
      // SUBJ1 exists with a stale first name (update); SUBJ0 does not (insert)
      insertMockMember("710", EMAIL1, AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, "1");
      schedule502(EMAIL0, 1);
      schedule502(EMAIL1, 1);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      assertEquals(1, mockCount(EMAIL0));
      assertEquals("1", mockColumn("status", EMAIL0));
      assertEquals(SubjectTestHelper.SUBJ1.getId(), mockColumn("first_name", EMAIL1));
      assertEquals(0, remaining502(EMAIL0));
      assertEquals(0, remaining502(EMAIL1));

      // converged: nothing left to write
      output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());
      assertEquals(1, mockCount(EMAIL0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /** when the 502s outlast the retries the entity is reported as an error, not silently skipped */
  public void testFullSyncRetriesExhaustedIsAnError() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new AssetSonarProvisionerTestConfigInput()
        .addExtraConfig("assetSonarRetryCount", "1"));

    try {
      schedule502(EMAIL0, 20);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision(defaultConfigId(), true);
      assertTrue("the failed entity must be reported", output.getRecordsWithErrors() >= 1);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning (Tomcat)
  // =============================================

  /**
   * Create with tiers, leave a hand-made Administrator alone, deactivate on removal, reactivate on
   * re-add (same member, no duplicate).
   */
  public void testFullSyncTiersDeactivateReactivate() {
    if (!tomcatRunTests()) {
      return;
    }

    AssetSonarProvisionerTestConfigInput configInput = new AssetSonarProvisionerTestConfigInput()
        .addExtraConfig("assetSonarAgentGroupName", "tiers:agent");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // a hand-made Administrator not in Grouper; no administrator group is configured, so that tier
      // is unmanaged and the member must survive deleteEntitiesIfNotExistInGrouper
      insertMockMember("900", "admin@x.edu", AssetSonarProvisionerTestUtils.ADMINISTRATOR_ROLE_ID, "1");
      // a plain Staff User not in Grouper: managed tier, so it is deactivated
      insertMockMember("901", "stray@x.edu", AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, "1");

      Group agentGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("tiers:agent").save();
      agentGroup.addMember(SubjectTestHelper.SUBJ1, false);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      assertEquals(1, mockCount(EMAIL0));
      assertEquals(AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, mockColumn("role_id", EMAIL0));
      assertEquals(AssetSonarProvisionerTestUtils.AGENT_ROLE_ID, mockColumn("role_id", EMAIL1));
      assertEquals("1", mockColumn("status", EMAIL1));
      assertEquals("1", mockColumn("status", "admin@x.edu"));
      assertEquals("0", mockColumn("status", "stray@x.edu"));
      String subj1MemberId = mockColumn("id", EMAIL1);

      // removal deactivates, never deletes
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals(1, mockCount(EMAIL1));
      assertEquals("0", mockColumn("status", EMAIL1));

      // a repeat sync is a no-op for an already deactivated member
      output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());
      assertEquals("0", mockColumn("status", EMAIL1));

      // re-add reactivates the same member
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      fullProvision();
      assertEquals(1, mockCount(EMAIL1));
      assertEquals("1", mockColumn("status", EMAIL1));
      assertEquals(subj1MemberId, mockColumn("id", EMAIL1));

      // the hand-made Administrator was never demoted or deactivated
      assertEquals(AssetSonarProvisionerTestUtils.ADMINISTRATOR_ROLE_ID, mockColumn("role_id", "admin@x.edu"));
      assertEquals("1", mockColumn("status", "admin@x.edu"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * With the inactive read off, a deactivated member is invisible to the select. Creating it hits
   * the 403 (no id), so the DAO must look it up by email and reactivate it rather than fail forever
   * or create a duplicate.
   */
  public void testInsertCollidesWithHiddenInactiveMember() {
    if (!tomcatRunTests()) {
      return;
    }

    AssetSonarProvisionerTestConfigInput configInput = new AssetSonarProvisionerTestConfigInput()
        .addExtraConfig("assetSonarSelectInactiveMembers", "false");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockMember("950", EMAIL0, AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, "0");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      assertEquals(1, mockCount(EMAIL0));
      assertEquals("950", mockColumn("id", EMAIL0));
      assertEquals("1", mockColumn("status", EMAIL0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * An attribute update sends the email too, so the email is never lost.
   */
  public void testFullSyncUpdateKeepsEmail() {
    if (!tomcatRunTests()) {
      return;
    }

    GrouperSession grouperSession = setupProvisionerTest(new AssetSonarProvisionerTestConfigInput());

    try {
      // pre-existing member matched by email, with a stale name and no institutional id
      insertMockMember("960", EMAIL0, AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, "1");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      assertEquals(1, mockCount(EMAIL0));
      assertEquals("960", mockColumn("id", EMAIL0));
      assertEquals(SubjectTestHelper.SUBJ0.getId(), mockColumn("first_name", EMAIL0));
      assertEquals(SubjectTestHelper.SUBJ0.getId(), mockColumn("employee_identification_number", EMAIL0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * Sync-back: a full sync captures every member (including one Grouper created, re-read by the
   * drain after its write) into grouper_prov_user. Then, with fullSyncUsersFromSyncBack on, the
   * next full sync is served from that cache instead of paging the members: a member added to
   * AssetSonar behind Grouper's back is not in the cache, so it is not seen and therefore not
   * deactivated -- which a normal full sync (deleteEntitiesIfNotExistInGrouper) would do.
   */
  public void testSyncBackCaptureAndFullSyncFromCache() {
    if (!tomcatRunTests()) {
      return;
    }

    AssetSonarProvisionerTestConfigInput configInput = new AssetSonarProvisionerTestConfigInput()
        .addExtraConfig("loadEntitiesToGenericGrouperTable", "true");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockMember("900", "admin@x.edu", AssetSonarProvisionerTestUtils.ADMINISTRATOR_ROLE_ID, "1");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group testGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:testGroup").save();
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      attachProvisioningAttribute(stem);

      GrouperProvisioningOutput output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      String subj0MemberId = mockColumn("id", EMAIL0);
      assertNotNull(subj0MemberId);

      Long syncInternalId = GcGrouperSyncDao.retrieveByProvisionerName(null, defaultConfigId()).getInternalId();
      List<String> cachedIds = new GcDbAccess().connectionName("grouper")
          .sql("select target_user_id from grouper_prov_user where grouper_sync_internal_id = ?")
          .addBindVar(syncInternalId).selectList(String.class);
      assertTrue("read member captured: " + cachedIds, cachedIds.contains("900"));
      assertTrue("created member captured via the drain re-read: " + cachedIds, cachedIds.contains(subj0MemberId));

      // now serve full syncs from the cache
      new GrouperDbConfig().configFileName("grouper-loader.properties")
          .propertyName("provisioner." + defaultConfigId() + ".fullSyncUsersFromSyncBack").value("true").store();
      ConfigPropertiesCascadeBase.clearCache();

      // added in AssetSonar directly, so not in the cache
      insertMockMember("901", "stray@x.edu", AssetSonarProvisionerTestUtils.STAFF_USER_ROLE_ID, "1");

      output = fullProvision();
      assertEquals(0, output.getRecordsWithErrors());

      assertEquals("the uncached member was never selected, so never deactivated",
          "1", mockColumn("status", "stray@x.edu"));
      assertEquals("1", mockColumn("status", EMAIL0));
      assertEquals(1, mockCount(EMAIL0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning harness helpers
  // =============================================

  private GrouperSession setupProvisionerTest(AssetSonarProvisionerTestConfigInput configInput) {
    AssetSonarProvisionerTestUtils.setupAssetSonarExternalSystem();
    AssetSonarProvisionerTestUtils.configureAssetSonarProvisioner(configInput);

    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_asset_sonar_member").executeSql();

    return GrouperSession.startRootSession();
  }

  private void attachProvisioningAttribute(Stem stem) {
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision(defaultConfigId());
    attributeValue.setTargetName(defaultConfigId());
    attributeValue.setStemScopeString("sub");
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

}
