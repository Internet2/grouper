package edu.internet2.middleware.grouper.app.jamf;

import java.util.List;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * Tests for the Jamf provisioner.
 *
 * <p>API-level tests (deterministic, run without a live Tomcat) exercise
 * {@link JamfApiCommands} over HTTP against {@link JamfMockServiceHandler} + the mock DB. The
 * full-sync / incremental tests are gated behind {@code tomcatRunTests()} because they need the
 * mock service running in the test Tomcat.</p>
 */
public class JamfProvisionerTest extends GrouperProvisioningBaseTest {

  /** external system config id (matches JamfProvisionerTestUtils.CONFIG_ID) */
  private static final String CONFIG_ID = JamfProvisionerTestUtils.CONFIG_ID;

  public static void main(String[] args) {
    TestRunner.run(new JamfProvisionerTest("testRetrieveAccountByName"));
    System.exit(0);
  }

  @Override
  public String defaultConfigId() {
    return "jamfProvisioner";
  }

  public JamfProvisionerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();

    JamfMockServiceHandler.ensureJamfMockTables();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_account").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_account_group").executeSql();

    JamfProvisionerTestUtils.setupJamfExternalSystem();
  }

  // =============================================
  // Mock seed helpers
  // =============================================

  private void insertMockAccount(String id, String name, String fullName) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_jamf_account (id, name, full_name, email, access_level, enabled, directory_user) values (?, ?, ?, ?, ?, ?, ?)")
        .addBindVar(id).addBindVar(name).addBindVar(fullName).addBindVar(name)
        .addBindVar("Group Access").addBindVar("Enabled").addBindVar("F").executeSql();
  }

  private void insertMockRole(String id, String name, String accessLevel, String privilegeSet) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_jamf_account_group (id, name, access_level, privilege_set, site_id, site_name) values (?, ?, ?, ?, ?, ?)")
        .addBindVar(id).addBindVar(name).addBindVar(accessLevel).addBindVar(privilegeSet)
        .addBindVar("-1").addBindVar("NONE").executeSql();
  }

  private void insertMockMembership(String groupId, String accountName) {
    new GcDbAccess().connectionName("grouper")
        .sql("insert into mock_jamf_membership (id, group_id, account_name) values (?, ?, ?)")
        .addBindVar(GrouperUuid.getUuid()).addBindVar(groupId).addBindVar(accountName).executeSql();
  }

  private int membershipCount(String groupId) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_jamf_membership where group_id = ?")
        .addBindVar(groupId).select(int.class);
  }

  // =============================================
  // API-level tests (no Tomcat needed)
  // =============================================

  public void testRetrieveAccountByName() {
    insertMockAccount("101", "jdoe@upenn.edu", "Jane Doe");

    JamfAccount account = JamfApiCommands.retrieveAccountByName(CONFIG_ID, "jdoe@upenn.edu");
    assertNotNull(account);
    assertEquals("101", account.getId());
    assertEquals("jdoe@upenn.edu", account.getName());
    assertEquals("Jane Doe", account.getFullName());
    assertEquals("Group Access", account.getAccessLevel());
    assertEquals(Boolean.FALSE, account.getDirectoryUser());
  }

  public void testRetrieveAccountByNameCaseInsensitive() {
    insertMockAccount("102", "jdoe@upenn.edu", "Jane Doe");
    // mixed-case lookup should still resolve (names are matched lowercased)
    JamfAccount account = JamfApiCommands.retrieveAccountByName(CONFIG_ID, "JDoe@upenn.edu");
    assertNotNull(account);
    assertEquals("jdoe@upenn.edu", account.getName());
  }

  public void testRetrieveAccountByNameNotFound() {
    assertNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID, "nobody@upenn.edu"));
  }

  public void testCreateAccount() {
    JamfAccount toCreate = new JamfAccount();
    toCreate.setName("newuser@upenn.edu");
    toCreate.setFullName("New User");
    toCreate.setEmail("newuser@upenn.edu");
    toCreate.setAccessLevel("Group Access");

    JamfAccount created = JamfApiCommands.createAccount(CONFIG_ID, toCreate);
    assertNotNull(created.getId());

    JamfAccount readBack = JamfApiCommands.retrieveAccountByName(CONFIG_ID, "newuser@upenn.edu");
    assertNotNull(readBack);
    assertEquals("New User", readBack.getFullName());
    assertEquals("Group Access", readBack.getAccessLevel());
  }

  public void testRetrieveAccountGroups() {
    insertMockRole("201", "roleAlpha", "Full Access", "Auditor");
    insertMockRole("202", "roleBeta", "Full Access", "Administrator");

    List<JamfAccountGroup> roles = JamfApiCommands.retrieveAccountGroups(CONFIG_ID, java.util.Collections.<String>emptySet());
    assertEquals(2, roles.size());
  }

  public void testRetrieveAccountGroupsIgnore() {
    insertMockRole("201", "roleAlpha", "Full Access", "Auditor");
    insertMockRole("202", "roleBeta", "Full Access", "Administrator");

    Set<String> ignore = JamfApiCommands.parseIgnoreSet("rolealpha");
    List<JamfAccountGroup> roles = JamfApiCommands.retrieveAccountGroups(CONFIG_ID, ignore);
    assertEquals(1, roles.size());
    assertEquals("roleBeta", roles.get(0).getName());
  }

  public void testRetrieveAccountGroupWithMembers() {
    insertMockRole("301", "roleGamma", "Full Access", "Auditor");
    insertMockAccount("111", "a@upenn.edu", "A");
    insertMockAccount("112", "b@upenn.edu", "B");
    insertMockMembership("301", "a@upenn.edu");
    insertMockMembership("301", "b@upenn.edu");

    JamfAccountGroup role = JamfApiCommands.retrieveAccountGroup(CONFIG_ID, "301");
    assertNotNull(role);
    assertEquals("roleGamma", role.getName());
    assertEquals("Auditor", role.getPrivilegeSet());
    assertEquals(2, GrouperUtil.length(role.getMembers()));
    assertTrue(role.getMembers().contains("a@upenn.edu"));
    assertTrue(role.getMembers().contains("b@upenn.edu"));
  }

  public void testReplaceAccountGroupMembers() {
    insertMockRole("401", "roleDelta", "Full Access", "Auditor");
    insertMockAccount("121", "x@upenn.edu", "X");
    insertMockAccount("122", "y@upenn.edu", "Y");

    // add two members
    JamfApiCommands.replaceAccountGroupMembers(CONFIG_ID, "401", "roleDelta",
        java.util.Arrays.asList("x@upenn.edu", "y@upenn.edu"));
    assertEquals(2, membershipCount("401"));

    // replace with just one
    JamfApiCommands.replaceAccountGroupMembers(CONFIG_ID, "401", "roleDelta",
        java.util.Arrays.asList("x@upenn.edu"));
    assertEquals(1, membershipCount("401"));
    JamfAccountGroup role = JamfApiCommands.retrieveAccountGroup(CONFIG_ID, "401");
    assertTrue(role.getMembers().contains("x@upenn.edu"));

    // clear
    JamfApiCommands.replaceAccountGroupMembers(CONFIG_ID, "401", "roleDelta",
        java.util.Collections.<String>emptyList());
    assertEquals(0, membershipCount("401"));

    // a membership replace must never damage the role's privilege set
    role = JamfApiCommands.retrieveAccountGroup(CONFIG_ID, "401");
    assertEquals("Auditor", role.getPrivilegeSet());
    assertEquals("Full Access", role.getAccessLevel());
  }

  public void testParseIgnoreSet() {
    Set<String> ignore = JamfApiCommands.parseIgnoreSet("a@x.edu, B@x.edu");
    assertTrue(JamfApiCommands.isIgnored("A@x.edu", ignore));
    assertTrue(JamfApiCommands.isIgnored("b@x.edu", ignore));
    assertFalse(JamfApiCommands.isIgnored("c@x.edu", ignore));
  }

  // =============================================
  // Full-sync / incremental membership (Tomcat required)
  // =============================================

  public void testFullSyncMembership() {
    membershipAddRemove(true);
  }

  public void testIncrementalMembership() {
    membershipAddRemove(false);
  }

  /**
   * Roles pre-exist (read-only). Grouper creates member accounts and manages role membership:
   * add two members, remove one, then re-add.
   * @param isFull true for full sync, false for incremental
   */
  private void membershipAddRemove(boolean isFull) {
    if (!tomcatRunTests()) {
      return;
    }

    JamfProvisionerTestConfigInput configInput = provisionerConfig();
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // role must pre-exist -- Grouper never creates roles
      insertMockRole("501", "role-a", "Full Access", "Auditor");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      // group extension must equal the Jamf role name (group matched by name = extension)
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);

      initIncrementalState(isFull);
      attachProvisioningAttribute(stem);

      // baseline
      fullProvision();

      // both subjects should now be members of role-a, and their accounts created
      assertEquals(2, membershipCount("501"));
      assertNotNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID,
          SubjectTestHelper.SUBJ0.getAttributeValue("email")));

      // remove SUBJ1
      roleGroupA.deleteMember(SubjectTestHelper.SUBJ1);
      provision(isFull);
      assertEquals(1, membershipCount("501"));

      // re-add SUBJ1
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);
      provision(isFull);
      assertEquals(2, membershipCount("501"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // =============================================
  // Provisioning harness helpers
  // =============================================

  private JamfProvisionerTestConfigInput provisionerConfig() {
    return new JamfProvisionerTestConfigInput().assignConfigId("jamfProvisioner");
  }

  private GrouperSession setupProvisionerTest(JamfProvisionerTestConfigInput configInput) {
    JamfProvisionerTestUtils.setupJamfExternalSystem();
    JamfProvisionerTestUtils.configureJamfProvisioner(configInput);

    GrouperUtil.sleep(5000);
    GrouperStartup.startup();

    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_membership").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_account").executeSql();
    new GcDbAccess().connectionName("grouper").sql("delete from mock_jamf_account_group").executeSql();

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
    attributeValue.setDoProvision("jamfProvisioner");
    attributeValue.setTargetName("jamfProvisioner");
    attributeValue.setStemScopeString("sub");

    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  }

}
