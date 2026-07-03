package edu.internet2.middleware.grouper.app.scim;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningType;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GenericScim2MockServiceHandler;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ApiCommands;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2MembershipCache;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisionerConfiguration;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2TargetDao;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.app.scim2Provisioning.ScimSettings;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Coverage for SCIM provisioner settings that were previously untested against the AWS mock:
 * <ul>
 *   <li>{@code disableEntitiesInsteadOfDelete} - on deprovision the entity is disabled (active=false)
 *       instead of deleted, and on re-provision the same disabled entity is re-enabled (no duplicate).</li>
 *   <li>{@code scimEmailFilterStrategy} - the three non-default outgoing filter syntaxes
 *       (emails.value, emails[value], emails[typeWork and value]).</li>
 *   <li>{@code scimNamePatchStrategy} (qualified/nested) and {@code scimEmailPatchStrategy}
 *       (noPath/pathEmailsQualified) - the non-default outgoing PATCH bodies.</li>
 * </ul>
 *
 * <p>Tests hit the AWS SCIM mock that runs in a separate Tomcat process, so exact outgoing
 * requests are asserted via the DB-backed {@code mock_scim_capture} table that the mock writes
 * ({@code lastUsersFilter}, {@code lastUserPatchBody}) and read-back is asserted against
 * {@code mock_scim_user}.
 */
public class GrouperAwsProvisionerScimSettingsTest extends GrouperProvisioningBaseTest {

  public static void main(String[] args) {
    TestRunner.run(new GrouperAwsProvisionerScimSettingsTest("testDisableEntitiesInsteadOfDelete"));
  }

  public static boolean startTomcat = false;

  public GrouperAwsProvisionerScimSettingsTest(String name) {
    super(name);
  }

  @Override
  public String defaultConfigId() {
    return "awsProvisioner";
  }

  /**
   * delete all mock scim rows (including the capture table) so each test starts clean
   */
  private void clearScimMockTables() {
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_group").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_user").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_capture").executeSql();
  }

  /**
   * read a captured outgoing request value the mock recorded
   * @param key e.g. lastUsersFilter or lastUserPatchBody
   * @return the captured value
   */
  private String captureValue(String key) {
    return new GcDbAccess().connectionName("grouper").sql("select capture_value from mock_scim_capture where capture_key = ?")
        .addBindVar(key).select(String.class);
  }

  /**
   * read a single column for a mock scim user
   * @param column column name
   * @param id scim user id
   * @return the column value
   */
  private String userColumn(String column, String id) {
    return new GcDbAccess().connectionName("grouper").sql("select " + column + " from mock_scim_user where id = ?")
        .addBindVar(id).select(String.class);
  }

  /**
   * configure a standard AWS provisioner and (re)create the mock tables so direct
   * GrouperScim2ApiCommands calls (tasks 2 and 3) have a provisioner the mock can resolve.
   */
  private void prepareDirectApiProvisioner() {
    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true));

    GrouperStartup.startup();

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }

    GenericScim2MockServiceHandler.ensureScimMockTables();
    clearScimMockTables();
  }

  /**
   * pre-create a scim user in the mock target and return its assigned id
   * @param grouperScim2User the user to create
   * @return the created user id
   */
  private String createMockUser(GrouperScim2User grouperScim2User) {
    GrouperScim2User created = GrouperScim2ApiCommands.createScimUser("awsConfigId", grouperScim2User, null, new ScimSettings());
    return created.getId();
  }

  // =====================================================================================
  // Task 1: disableEntitiesInsteadOfDelete
  // =====================================================================================

  public void testDisableEntitiesInsteadOfDelete() {

    if (!tomcatRunTests()) {
      return;
    }

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
        .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
        .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("disableEntitiesInsteadOfDelete", "true"));

    GrouperStartup.startup();

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }

    // this will create the mock tables
    GrouperScim2ApiCommands.retrieveScimUsers("awsConfigId", null);
    clearScimMockTables();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();

    testGroup.addMember(SubjectTestHelper.SUBJ0, false);
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setDoProvision("awsProvisioner");
    attributeValue.setTargetName("awsProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);

    // ---- initial provision: both users created and active ----
    GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
    GrouperUtil.sleep(2000);
    assertTrue(1 <= grouperProvisioningOutput.getInsert());

    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_scim_user").select(int.class));

    String subj1UserName = SubjectTestHelper.SUBJ1.getId();
    String subj0UserName = SubjectTestHelper.SUBJ0.getId();

    String subj1IdBefore = new GcDbAccess().connectionName("grouper")
        .sql("select id from mock_scim_user where user_name = ?").addBindVar(subj1UserName).select(String.class);
    assertNotNull(subj1IdBefore);
    assertEquals("T", userColumn("active", subj1IdBefore));

    // ---- remove SUBJ1 -> on next sync the entity should be DISABLED, not deleted ----
    testGroup.deleteMember(SubjectTestHelper.SUBJ1);

    fullProvision();
    GrouperUtil.sleep(2000);

    // still two rows in the target (nothing was deleted)
    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_scim_user").select(int.class));

    // SUBJ1 disabled, SUBJ0 still active
    assertEquals("F", userColumn("active", subj1IdBefore));
    String subj0Id = new GcDbAccess().connectionName("grouper")
        .sql("select id from mock_scim_user where user_name = ?").addBindVar(subj0UserName).select(String.class);
    assertEquals("T", userColumn("active", subj0Id));

    // ---- re-add SUBJ1 -> the SAME disabled entity should be re-enabled (no duplicate) ----
    testGroup.addMember(SubjectTestHelper.SUBJ1, false);

    fullProvision();
    GrouperUtil.sleep(2000);

    // still exactly two rows - the disabled user was re-enabled, not recreated
    assertEquals(new Integer(2), new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_scim_user").select(int.class));

    String subj1IdAfter = new GcDbAccess().connectionName("grouper")
        .sql("select id from mock_scim_user where user_name = ?").addBindVar(subj1UserName).select(String.class);
    assertEquals(subj1IdBefore, subj1IdAfter);
    assertEquals("T", userColumn("active", subj1IdAfter));
  }

  // =====================================================================================
  // Task 2: scimEmailFilterStrategy (three non-default values)
  // =====================================================================================

  private GrouperScim2User retrieveByEmailWithStrategy(String emailValue, String filterStrategy) {
    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimEmailFilterStrategy(filterStrategy);
    return GrouperScim2ApiCommands.retrieveScimUser("awsConfigId", "email", emailValue,
        new GrouperScim2MembershipCache(), scimSettings);
  }

  private void emailFilterStrategyHelper(String filterStrategy, String expectedFilter) {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();

    // mock now enforces a single email-filter strategy; tell it to support the one under test
    setMockStrategyMode("emailFilterStrategy", filterStrategy);

    String emailValue = "task2@example.com";

    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("task2user");
    user.setEmailValue(emailValue);
    user.setEmailType("work");
    String id = createMockUser(user);

    GrouperScim2User found = retrieveByEmailWithStrategy(emailValue, filterStrategy);

    // behavioral: the user was found through the configured filter syntax
    assertNotNull("expected to find user via filter strategy " + filterStrategy, found);
    assertEquals(id, found.getId());
    assertEquals(emailValue, found.getEmailValue());

    // exact: the outgoing SCIM filter the mock received
    assertEquals(expectedFilter, captureValue("lastUsersFilter"));
  }

  public void testScimEmailFilterStrategyEmailsValue() {
    emailFilterStrategyHelper("emails.value", "emails.value eq \"task2@example.com\"");
  }

  public void testScimEmailFilterStrategyBracketValue() {
    emailFilterStrategyHelper("emails[value]", "emails[value eq \"task2@example.com\"]");
  }

  public void testScimEmailFilterStrategyBracketTypeValue() {
    emailFilterStrategyHelper("emails[typeWork and value]", "emails[type eq \"work\" and value eq \"task2@example.com\"]");
  }

  // =====================================================================================
  // Task 3: scimNamePatchStrategy (qualified/nested) and scimEmailPatchStrategy (noPath/pathEmailsQualified)
  // =====================================================================================

  public void testScimNamePatchStrategyQualified() {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();

    // mock now enforces a single name-patch strategy; tell it to support the one under test
    setMockStrategyMode("namePatchStrategy", "qualified");

    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("task3qual");
    user.setGivenName("givenOld");
    user.setFamilyName("familyOld");
    user.setMiddleName("middleOld");
    user.setFormattedName("formattedOld");
    String id = createMockUser(user);

    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setGivenName("givenNew");
    toPatch.setFamilyName("familyNew");
    toPatch.setMiddleName("middleNew");
    toPatch.setFormattedName("formattedNew");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("givenName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("familyName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("middleName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("formattedName", ProvisioningObjectChangeAction.update);

    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimNamePatchStrategy("qualified");

    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);

    // exact: qualified strategy sends dotted name paths
    String body = captureValue("lastUserPatchBody");
    assertTrue(body, body.contains("name.givenName"));
    assertTrue(body, body.contains("name.familyName"));
    assertTrue(body, body.contains("name.middleName"));
    assertTrue(body, body.contains("name.formatted"));

    // read-back
    assertEquals("givenNew", userColumn("given_name", id));
    assertEquals("familyNew", userColumn("family_name", id));
    assertEquals("middleNew", userColumn("middle_name", id));
    assertEquals("formattedNew", userColumn("formatted_name", id));
  }

  public void testScimNamePatchStrategyNested() {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();

    // mock now enforces a single name-patch strategy; tell it to support the one under test
    setMockStrategyMode("namePatchStrategy", "nested");

    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("task3nested");
    user.setGivenName("givenOld");
    user.setFamilyName("familyOld");
    user.setMiddleName("middleOld");
    user.setFormattedName("formattedOld");
    String id = createMockUser(user);

    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setGivenName("givenNew");
    toPatch.setFamilyName("familyNew");
    toPatch.setMiddleName("middleNew");
    toPatch.setFormattedName("formattedNew");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("givenName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("familyName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("middleName", ProvisioningObjectChangeAction.update);
    fieldsToUpdate.put("formattedName", ProvisioningObjectChangeAction.update);

    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimNamePatchStrategy("nested");

    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);

    // exact: nested strategy sends a single op with path "name" and a nested value object
    String body = captureValue("lastUserPatchBody");
    assertTrue(body, body.contains("\"path\":\"name\""));
    assertTrue(body, body.contains("\"givenName\":\"givenNew\""));
    assertTrue(body, body.contains("\"familyName\":\"familyNew\""));
    assertTrue(body, body.contains("\"middleName\":\"middleNew\""));
    assertTrue(body, body.contains("\"formatted\":\"formattedNew\""));
    // nested strategy must NOT use dotted name paths
    assertFalse(body, body.contains("name.givenName"));

    // read-back
    assertEquals("givenNew", userColumn("given_name", id));
    assertEquals("familyNew", userColumn("family_name", id));
    assertEquals("middleNew", userColumn("middle_name", id));
    assertEquals("formattedNew", userColumn("formatted_name", id));
  }

  public void testScimEmailPatchStrategyNoPath() {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();

    // mock now enforces a single email-patch strategy; tell it to support the one under test
    setMockStrategyMode("emailPatchStrategy", "noPath");

    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("task3nopath");
    user.setEmailValue("old@example.com");
    user.setEmailType("work");
    String id = createMockUser(user);

    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setEmailValue("new@example.com");
    toPatch.setEmailType("work");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("emailValue", ProvisioningObjectChangeAction.update);

    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimEmailPatchStrategy("noPath");

    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);

    // exact: noPath strategy sends a replace op with no "path" and an emails array in the value
    String body = captureValue("lastUserPatchBody");
    assertFalse(body, body.contains("\"path\""));
    assertTrue(body, body.contains("emails"));
    assertTrue(body, body.contains("new@example.com"));

    // read-back
    assertEquals("new@example.com", userColumn("email_value", id));
  }

  public void testScimEmailPatchStrategyPathEmailsQualified() {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();

    // mock now enforces a single email-patch strategy; tell it to support the one under test
    setMockStrategyMode("emailPatchStrategy", "pathEmailsQualified");

    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("task3qualemail");
    user.setEmailValue("old@example.com");
    user.setEmailType("work");
    String id = createMockUser(user);

    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setEmailValue("new@example.com");
    toPatch.setEmailType("work");

    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("emailValue", ProvisioningObjectChangeAction.update);

    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimEmailPatchStrategy("pathEmailsQualified");

    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);

    // exact: pathEmailsQualified sends a type-qualified emails path with a string value
    String body = captureValue("lastUserPatchBody");
    assertTrue(body, body.contains("emails[type eq"));
    assertTrue(body, body.contains("new@example.com"));

    // read-back
    assertEquals("new@example.com", userColumn("email_value", id));
  }

  // =====================================================================================
  // Task 4: mock strict-mode gating - the mock simulates a SCIM server that supports exactly
  // ONE strategy per dimension (grouperTest.scim2.mock.*Strategy.mode). The configured strategy
  // is accepted; every other strategy in that dimension is rejected. This is what lets the
  // diagnostics "strategy discovery" detect which strategy the target actually supports.
  // =====================================================================================

  /**
   * set a mock strict-mode property: which single strategy the simulated SCIM server supports for
   * a dimension (blank = lenient/accept all; "none" for email filter = reject all email filters).
   * @param dimension property suffix, e.g. namePatchStrategy, emailPatchStrategy, emailFilterStrategy
   * @param mode the single supported strategy value, or blank to clear
   */
  private void setMockStrategyMode(String dimension, String mode) {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperTest.scim2.mock." + dimension + ".mode")
        .value(mode == null ? "" : mode).store();
  }

  @Override
  protected void tearDown() {
    // clear the mock strict-mode overrides so they do not leak into other tests in the same JVM
    setMockStrategyMode("namePatchStrategy", "");
    setMockStrategyMode("emailPatchStrategy", "");
    setMockStrategyMode("emailFilterStrategy", "");
    super.tearDown();
  }

  private void patchGivenName(String id, String namePatchStrategy, String newGivenName) {
    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setGivenName(newGivenName);
    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("givenName", ProvisioningObjectChangeAction.update);
    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimNamePatchStrategy(namePatchStrategy);
    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);
  }

  private void patchEmailValue(String id, String emailPatchStrategy, String newEmailValue) {
    GrouperScim2User toPatch = new GrouperScim2User();
    toPatch.setId(id);
    toPatch.setEmailValue(newEmailValue);
    toPatch.setEmailType("work");
    Map<String, ProvisioningObjectChangeAction> fieldsToUpdate = new LinkedHashMap<>();
    fieldsToUpdate.put("emailValue", ProvisioningObjectChangeAction.update);
    ScimSettings scimSettings = new ScimSettings();
    scimSettings.setScimEmailPatchStrategy(emailPatchStrategy);
    GrouperScim2ApiCommands.patchScimUser("awsConfigId", toPatch, fieldsToUpdate, scimSettings);
  }

  /**
   * configure the mock to support exactly one name patch strategy, then verify the supported one
   * is accepted (value updates) and every other one is rejected (mock 500 -> RuntimeException,
   * value unchanged).
   * @param supportedStrategy the one name patch strategy the mock should accept
   */
  private void mockNamePatchModeHelper(String supportedStrategy) {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();
    setMockStrategyMode("namePatchStrategy", supportedStrategy);

    for (String strategy : new String[] {"nonqualified", "qualified", "nested"}) {

      GrouperScim2User user = new GrouperScim2User();
      user.setUserName("nameMode_" + supportedStrategy + "_" + strategy);
      user.setGivenName("givenOld");
      user.setFamilyName("familyOld");
      String id = createMockUser(user);

      if (strategy.equals(supportedStrategy)) {
        patchGivenName(id, strategy, "givenNew");
        assertEquals("name patch strategy '" + strategy + "' should be accepted when mock mode=" + supportedStrategy,
            "givenNew", userColumn("given_name", id));
      } else {
        try {
          patchGivenName(id, strategy, "givenNew");
          fail("mock (namePatchStrategy.mode=" + supportedStrategy + ") should have rejected name patch strategy '" + strategy + "'");
        } catch (RuntimeException e) {
          // expected: the unsupported strategy was rejected; value must be unchanged
          assertEquals("givenOld", userColumn("given_name", id));
        }
      }
    }
  }

  public void testMockNamePatchModeNonqualified() {
    mockNamePatchModeHelper("nonqualified");
  }

  public void testMockNamePatchModeQualified() {
    mockNamePatchModeHelper("qualified");
  }

  public void testMockNamePatchModeNested() {
    mockNamePatchModeHelper("nested");
  }

  /**
   * configure the mock to support exactly one email patch strategy, then verify the supported one
   * is accepted (value updates) and every other one is rejected (mock 500 -> RuntimeException,
   * value unchanged).
   * @param supportedStrategy the one email patch strategy the mock should accept
   */
  private void mockEmailPatchModeHelper(String supportedStrategy) {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();
    setMockStrategyMode("emailPatchStrategy", supportedStrategy);

    for (String strategy : new String[] {"pathEmails", "noPath", "pathEmailsQualified"}) {

      GrouperScim2User user = new GrouperScim2User();
      user.setUserName("emailMode_" + supportedStrategy + "_" + strategy);
      user.setEmailValue("old@example.com");
      user.setEmailType("work");
      String id = createMockUser(user);

      if (strategy.equals(supportedStrategy)) {
        patchEmailValue(id, strategy, "new@example.com");
        assertEquals("email patch strategy '" + strategy + "' should be accepted when mock mode=" + supportedStrategy,
            "new@example.com", userColumn("email_value", id));
      } else {
        try {
          patchEmailValue(id, strategy, "new@example.com");
          fail("mock (emailPatchStrategy.mode=" + supportedStrategy + ") should have rejected email patch strategy '" + strategy + "'");
        } catch (RuntimeException e) {
          // expected: the unsupported strategy was rejected; value must be unchanged
          assertEquals("old@example.com", userColumn("email_value", id));
        }
      }
    }
  }

  public void testMockEmailPatchModePathEmails() {
    mockEmailPatchModeHelper("pathEmails");
  }

  public void testMockEmailPatchModeNoPath() {
    mockEmailPatchModeHelper("noPath");
  }

  public void testMockEmailPatchModePathEmailsQualified() {
    mockEmailPatchModeHelper("pathEmailsQualified");
  }

  /**
   * configure the mock to support exactly one email filter strategy, then verify the supported one
   * returns the user and every other one returns nothing (mock returns an empty result set).
   * @param supportedStrategy the one email filter strategy the mock should honor
   */
  private void mockEmailFilterModeHelper(String supportedStrategy) {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();
    setMockStrategyMode("emailFilterStrategy", supportedStrategy);

    String emailValue = "filtermode@example.com";
    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("filterMode_" + supportedStrategy);
    user.setEmailValue(emailValue);
    user.setEmailType("work");
    String id = createMockUser(user);

    for (String strategy : new String[] {"email", "emails.value", "emails[value]", "emails[typeWork and value]"}) {

      GrouperScim2User found = retrieveByEmailWithStrategy(emailValue, strategy);

      if (strategy.equals(supportedStrategy)) {
        assertNotNull("email filter strategy '" + strategy + "' should find the user when mock mode=" + supportedStrategy, found);
        assertEquals(id, found.getId());
      } else {
        assertNull("mock (emailFilterStrategy.mode=" + supportedStrategy + ") should not return the user for filter strategy '" + strategy + "'", found);
      }
    }
  }

  public void testMockEmailFilterModeEmail() {
    mockEmailFilterModeHelper("email");
  }

  public void testMockEmailFilterModeEmailsValue() {
    mockEmailFilterModeHelper("emails.value");
  }

  public void testMockEmailFilterModeBracketValue() {
    mockEmailFilterModeHelper("emails[value]");
  }

  public void testMockEmailFilterModeBracketTypeValue() {
    mockEmailFilterModeHelper("emails[typeWork and value]");
  }

  /**
   * mode "none" simulates a SCIM server (e.g. GitHub) that does not support email filtering at all:
   * every email filter strategy returns nothing.
   */
  public void testMockEmailFilterModeNone() {

    if (!tomcatRunTests()) {
      return;
    }

    prepareDirectApiProvisioner();
    setMockStrategyMode("emailFilterStrategy", "none");

    String emailValue = "filternone@example.com";
    GrouperScim2User user = new GrouperScim2User();
    user.setUserName("filterNoneUser");
    user.setEmailValue(emailValue);
    user.setEmailType("work");
    createMockUser(user);

    for (String strategy : new String[] {"email", "emails.value", "emails[value]", "emails[typeWork and value]"}) {
      assertNull("emailFilterStrategy.mode=none should reject every email filter, including '" + strategy + "'",
          retrieveByEmailWithStrategy(emailValue, strategy));
    }
  }

  // =====================================================================================
  // Task 4: group matching by externalId
  //
  // when the provisioner is configured with externalId as a group SEARCH attribute (distinct from
  // the displayName MATCHING attribute), a group that cannot be found by id or displayName must
  // still be located through a server-side "externalId eq ..." filter.  externalId is a stable,
  // rename-proof key, so this lets a group be re-matched in the target even after its displayName
  // changes.  before this fix retrieveGroupHelper only ever searched by id or displayName.
  // =====================================================================================

  public void testGroupSearchByExternalId() {

    if (!tomcatRunTests()) {
      return;
    }

    ScimProvisionerTestUtils.setupAwsExternalSystem();

    // add a third group attribute (externalId) and make externalId the group SEARCH attribute while
    // leaving displayName as the MATCHING attribute.  addExtraConfig values win over the builder
    // defaults, so numberOfGroupAttributes is bumped to 3 and the search config is overridden.
    ScimProvisionerTestUtils.configureScimProvisioner(new ScimProvisionerTestConfigInput()
        .assignChangelogConsumerConfigId("awsScimProvTestCLC").assignConfigId("awsProvisioner")
        .assignBearerTokenExternalSystemConfigId("awsConfigId")
        .assignScimType("AWS")
        .assignGroupAttributeCount(2)
        .assignBearer(true)
        .addExtraConfig("numberOfGroupAttributes", "3")
        .addExtraConfig("targetGroupAttribute.2.name", "externalId")
        .addExtraConfig("targetGroupAttribute.2.translateExpressionType", "grouperProvisioningGroupField")
        .addExtraConfig("targetGroupAttribute.2.translateFromGrouperProvisioningGroupField", "idIndexString")
        .addExtraConfig("groupMatchingAttributeSameAsSearchAttribute", "false")
        .addExtraConfig("groupSearchAttributeCount", "1")
        .addExtraConfig("groupSearchAttribute0name", "externalId"));

    GrouperStartup.startup();

    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }

    GenericScim2MockServiceHandler.ensureScimMockTables();
    clearScimMockTables();

    // pre-create a group in the target with a known externalId and a displayName that the lookup
    // below will deliberately NOT search by, so the group can only be found via the externalId filter
    String externalId = "ext:penngroups:testGroupExtId";
    GrouperScim2Group mockGroup = new GrouperScim2Group();
    mockGroup.setDisplayName("realTargetDisplayName");
    mockGroup.setExternalId(externalId);
    GrouperScim2Group createdGroup = GrouperScim2ApiCommands.createScimGroup("awsConfigId", mockGroup,
        GrouperUtil.toSet("displayName", "externalId"), new ScimSettings());
    assertNotNull(createdGroup.getId());

    // clear only the captures (not the group we just created) so the assertion below sees the
    // filters from the retrieve, not from the create
    new GcDbAccess().connectionName("grouper").sql("delete from mock_scim_capture").executeSql();

    GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("awsProvisioner");
    provisioner.initialize(GrouperProvisioningType.fullProvisionFull);

    GrouperScim2ProvisionerConfiguration scimConfiguration =
        (GrouperScim2ProvisionerConfiguration) provisioner.retrieveGrouperProvisioningConfiguration();

    // sanity check: externalId is actually a configured group search attribute
    boolean externalIdIsGroupSearchAttribute = false;
    for (GrouperProvisioningConfigurationAttribute groupSearchAttribute : GrouperUtil.nonNull(scimConfiguration.getGroupSearchAttributes())) {
      if ("externalId".equals(groupSearchAttribute.getName())) {
        externalIdIsGroupSearchAttribute = true;
      }
    }
    assertTrue("externalId should be configured as a group search attribute", externalIdIsGroupSearchAttribute);

    GrouperScim2TargetDao scim2TargetDao =
        (GrouperScim2TargetDao) provisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao();

    // a target group with no id and a displayName that is NOT in the target, but the right externalId
    ProvisioningGroup groupToFind = new ProvisioningGroup();
    groupToFind.setDisplayName("displayNameNotInTarget");
    groupToFind.assignAttributeValue("externalId", externalId);

    GrouperScim2Group found = scim2TargetDao.retrieveGroupHelper(scimConfiguration, groupToFind);

    // behavioral: the group was located even though id/displayName did not match
    assertNotNull("group should be found via the externalId filter", found);
    assertEquals(createdGroup.getId(), found.getId());
    assertEquals("realTargetDisplayName", found.getDisplayName());
    assertEquals(externalId, found.getExternalId());

    // exact: the last outgoing group filter was the externalId filter (the displayName search ran
    // first and returned nothing, then the externalId search found the group)
    assertEquals("externalId eq \"" + externalId + "\"", captureValue("lastGroupsFilter"));
  }
}
