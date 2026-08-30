package edu.internet2.middleware.grouper.app.jamf;

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

  private int accountCount(String name) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_jamf_account where lower(name) = ?")
        .addBindVar(name.toLowerCase()).select(int.class);
  }

  private String accountEnabled(String name) {
    return new GcDbAccess().connectionName("grouper")
        .sql("select enabled from mock_jamf_account where lower(name) = ?")
        .addBindVar(name.toLowerCase()).select(String.class);
  }

  private boolean isMember(String groupId, String accountName) {
    int count = new GcDbAccess().connectionName("grouper")
        .sql("select count(1) from mock_jamf_membership where group_id = ? and lower(account_name) = ?")
        .addBindVar(groupId).addBindVar(accountName.toLowerCase()).select(int.class);
    return count > 0;
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

  public void testDeleteAccount() {
    insertMockAccount("601", "del@upenn.edu", "Del User");
    assertNotNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID, "del@upenn.edu"));

    JamfApiCommands.deleteAccount(CONFIG_ID, "601");
    assertNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID, "del@upenn.edu"));
  }

  public void testDeleteAccountNotFoundIsNoOp() {
    // deleting a non-existent account (404) must not throw -- it is treated as already gone
    JamfApiCommands.deleteAccount(CONFIG_ID, "999999999");
  }

  public void testRetrieveAccountById() {
    insertMockAccount("701", "byid@upenn.edu", "By Id");
    JamfAccount account = JamfApiCommands.retrieveAccountById(CONFIG_ID, "701");
    assertNotNull(account);
    assertEquals("701", account.getId());
    assertEquals("byid@upenn.edu", account.getName());
  }

  public void testRetrieveAccountByIdNotFound() {
    assertNull(JamfApiCommands.retrieveAccountById(CONFIG_ID, "999999999"));
  }

  public void testCreateAccountReReadsRealId() {
    // createAccount must return the account re-read BY NAME (authoritative id), not trust the
    // create-response id -- this is what makes match-by-id reliable
    JamfAccount toCreate = new JamfAccount();
    toCreate.setName("reread@upenn.edu");
    toCreate.setFullName("Re Read");
    toCreate.setEmail("reread@upenn.edu");
    toCreate.setAccessLevel("Group Access");

    JamfAccount created = JamfApiCommands.createAccount(CONFIG_ID, toCreate);
    assertNotNull(created.getId());
    // the returned id must match what a fresh lookup by name reports
    JamfAccount byName = JamfApiCommands.retrieveAccountByName(CONFIG_ID, "reread@upenn.edu");
    assertNotNull(byName);
    assertEquals(byName.getId(), created.getId());
  }

  public void testUpdateAccount() {
    insertMockAccount("801", "upd@upenn.edu", "Old Name");
    JamfAccount account = new JamfAccount();
    account.setName("upd@upenn.edu");
    account.setFullName("New Name");
    account.setEmail("newemail@upenn.edu");
    // partial update: only fullName + email
    JamfApiCommands.updateAccount(CONFIG_ID, "801", account, GrouperUtil.toSet("fullName", "email"));

    JamfAccount readBack = JamfApiCommands.retrieveAccountById(CONFIG_ID, "801");
    assertEquals("New Name", readBack.getFullName());
    assertEquals("newemail@upenn.edu", readBack.getEmail());
    // name untouched (not in the update set)
    assertEquals("upd@upenn.edu", readBack.getName());
  }

  public void testUpdateAccountRenameInPlace() {
    insertMockAccount("802", "old@upenn.edu", "Person");
    JamfAccount account = new JamfAccount();
    account.setName("new@upenn.edu");
    // rename: id stays, name changes -- safe because we match by id
    JamfApiCommands.updateAccount(CONFIG_ID, "802", account, GrouperUtil.toSet("name"));

    JamfAccount byId = JamfApiCommands.retrieveAccountById(CONFIG_ID, "802");
    assertEquals("new@upenn.edu", byId.getName());
    assertNotNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID, "new@upenn.edu"));
    assertNull(JamfApiCommands.retrieveAccountByName(CONFIG_ID, "old@upenn.edu"));
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

  public void testRetrieveAccountsIgnore() {
    insertMockAccount("131", "keep@upenn.edu", "Keep");
    insertMockAccount("132", "skip@upenn.edu", "Skip");

    // ignored accounts are filtered out of the select
    Set<String> ignore = JamfApiCommands.parseIgnoreSet("SKIP@upenn.edu");
    List<JamfAccount> accounts = JamfApiCommands.retrieveAccounts(CONFIG_ID, ignore);
    assertEquals(1, accounts.size());
    assertEquals("keep@upenn.edu", accounts.get(0).getName());
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
  // Disable-instead-of-delete + ignore-on-3-fields (deterministic)
  // =============================================

  public void testIsDisabledSemantics() {
    JamfAccount account = new JamfAccount();
    // null/blank enabled -> treated as enabled (the account exists and is usable)
    assertFalse(account.isDisabled());
    account.setEnabled("Enabled");
    assertFalse(account.isDisabled());
    account.setEnabled("Disabled");
    assertTrue(account.isDisabled());
    // some payloads use true/false
    account.setEnabled("false");
    assertTrue(account.isDisabled());
    account.setEnabled("true");
    assertFalse(account.isDisabled());
  }

  public void testIsAccountIgnoredByNameEmailOrEmailAddress() {
    JamfAccount account = new JamfAccount();
    account.setName("svc@upenn.edu");
    account.setEmail("break-glass@upenn.edu");
    account.setEmailAddress("alt@upenn.edu");

    // matches on name
    assertTrue(JamfApiCommands.isAccountIgnored(account, JamfApiCommands.parseIgnoreSet("SVC@upenn.edu")));
    // matches on email
    assertTrue(JamfApiCommands.isAccountIgnored(account, JamfApiCommands.parseIgnoreSet("break-glass@upenn.edu")));
    // matches on email_address (case-insensitive)
    assertTrue(JamfApiCommands.isAccountIgnored(account, JamfApiCommands.parseIgnoreSet("ALT@upenn.edu")));
    // no match
    assertFalse(JamfApiCommands.isAccountIgnored(account, JamfApiCommands.parseIgnoreSet("other@upenn.edu")));
    // empty ignore set never matches
    assertFalse(JamfApiCommands.isAccountIgnored(account, JamfApiCommands.parseIgnoreSet("")));
  }

  public void testSetAccountEnabledDisableThenEnable() {
    insertMockAccount("901", "dis@upenn.edu", "Dis User");

    // disable (the soft-delete op)
    JamfApiCommands.setAccountEnabled(CONFIG_ID, "901", JamfAccount.DISABLED);
    JamfAccount disabled = JamfApiCommands.retrieveAccountById(CONFIG_ID, "901");
    assertEquals("Disabled", disabled.getEnabled());
    assertTrue(disabled.isDisabled());

    // re-enable (the reactivate op)
    JamfApiCommands.setAccountEnabled(CONFIG_ID, "901", JamfAccount.ENABLED);
    JamfAccount enabled = JamfApiCommands.retrieveAccountById(CONFIG_ID, "901");
    assertEquals("Enabled", enabled.getEnabled());
    assertFalse(enabled.isDisabled());
  }

  public void testUpdateAccountReactivateResetsFieldsAndEnabled() {
    // the reactivate path PUTs name/full_name/email + enabled in one call
    insertMockAccount("902", "react@upenn.edu", "Old Name");
    JamfApiCommands.setAccountEnabled(CONFIG_ID, "902", JamfAccount.DISABLED);

    JamfAccount account = new JamfAccount();
    account.setName("react@upenn.edu");
    account.setFullName("New Name");
    account.setEmail("react@upenn.edu");
    account.setEnabled(JamfAccount.ENABLED);
    JamfApiCommands.updateAccount(CONFIG_ID, "902", account,
        GrouperUtil.toSet("name", "fullName", "email", "enabled"));

    JamfAccount back = JamfApiCommands.retrieveAccountById(CONFIG_ID, "902");
    assertFalse(back.isDisabled());
    assertEquals("New Name", back.getFullName());
  }

  public void testEmailAddressIsReadSeparately() {
    // reads populate both email and email_address (the mock echoes email into both)
    insertMockAccount("903", "ea@upenn.edu", "EA User");
    JamfAccount account = JamfApiCommands.retrieveAccountById(CONFIG_ID, "903");
    assertEquals("ea@upenn.edu", account.getEmail());
    assertEquals("ea@upenn.edu", account.getEmailAddress());
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

  /**
   * End-to-end disable-instead-of-delete + reactivate. A member's account is created, then when the
   * member is removed the account is DISABLED (not deleted); a disabled account is filtered out of
   * the select (so a repeat sync is a no-op and never duplicates or re-enables it); re-adding the
   * member reactivates the same account.
   */
  public void testDisableInsteadOfDeleteAndReactivate() {
    if (!tomcatRunTests()) {
      return;
    }

    JamfProvisionerTestConfigInput configInput = new JamfProvisionerTestConfigInput()
        .assignConfigId("jamfProvisioner")
        .addExtraConfig("deleteEntities", "true")
        .addExtraConfig("deleteEntitiesIfNotExistInGrouper", "true")
        .addExtraConfig("disableEntitiesInsteadOfDelete", "true");
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockRole("501", "role-a", "Full Access", "Auditor");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

      // baseline: both accounts created (Enabled) and members
      fullProvision();
      assertEquals(2, membershipCount("501"));
      assertEquals(1, accountCount(subj1Email));
      assertEquals("Enabled", accountEnabled(subj1Email));

      // remove SUBJ1 -> account DISABLED (not deleted), membership removed
      roleGroupA.deleteMember(SubjectTestHelper.SUBJ1);
      fullProvision();
      assertEquals(1, membershipCount("501"));
      assertEquals(1, accountCount(subj1Email));            // still there -- not hard deleted
      assertEquals("Disabled", accountEnabled(subj1Email)); // soft-deleted

      // filter + idempotency: a disabled account is filtered out of the select, so another full sync
      // treats it as absent and is a no-op -- it stays disabled, is not re-enabled, no duplicate
      fullProvision();
      assertEquals(1, accountCount(subj1Email));
      assertEquals("Disabled", accountEnabled(subj1Email));
      assertEquals(1, membershipCount("501"));

      // re-add SUBJ1 -> the disabled account is reactivated (Enabled), NOT duplicated, membership back
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);
      fullProvision();
      assertEquals(1, accountCount(subj1Email));            // reactivated in place, no duplicate
      assertEquals("Enabled", accountEnabled(subj1Email));
      assertEquals(2, membershipCount("501"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * End-to-end account ignore: a subject whose name/email is on the ignore list is never created and
   * never added to a role. Because that subject is a member of a provisioned group, reaching it is a
   * conflict, so it must surface as a per-member ERROR (not a silent skip) -- that is how an admin
   * learns an ignored person was placed into a provisioned group. A non-ignored subject provisions
   * normally alongside it.
   */
  public void testIgnoredAccountNotProvisioned() {
    if (!tomcatRunTests()) {
      return;
    }

    String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
    String subj1Email = SubjectTestHelper.SUBJ1.getAttributeValue("email");

    JamfProvisionerTestConfigInput configInput = new JamfProvisionerTestConfigInput()
        .assignConfigId("jamfProvisioner")
        .addExtraConfig("jamfIgnoreAccountNames", subj1Email);
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      insertMockRole("501", "role-a", "Full Access", "Auditor");

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);
      roleGroupA.addMember(SubjectTestHelper.SUBJ1, false);

      attachProvisioningAttribute(stem);

      // allowErrors: the ignored-but-provisioned subject is expected to error
      GrouperProvisioningOutput output = fullProvision("jamfProvisioner", true);

      // the ignored subject surfaced as an error rather than being silently skipped
      assertTrue("ignored subject in a provisioned group must surface an error",
          output.getRecordsWithErrors() >= 1);
      Member subj1Member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
      GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveByProvisionerName(null, "jamfProvisioner");
      GcGrouperSyncMember gcGrouperSyncMember =
          gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(subj1Member.getId());
      assertNotNull("ignored subject should have a sync-member error code", gcGrouperSyncMember.getErrorCode());

      // SUBJ0 provisions normally; the ignored SUBJ1 gets no account and is not added to the role
      assertEquals(1, accountCount(subj0Email));
      assertEquals(0, accountCount(subj1Email));
      assertEquals(1, membershipCount("501"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * End-to-end: a full-sync membership replace must NOT remove an ignored account that is a member of
   * the role out-of-band. Grouper adds its own member while the ignored member is preserved.
   */
  public void testIgnoredMemberPreservedOnFullReplace() {
    if (!tomcatRunTests()) {
      return;
    }

    String svcName = "svc-jamf@upenn.edu";
    JamfProvisionerTestConfigInput configInput = new JamfProvisionerTestConfigInput()
        .assignConfigId("jamfProvisioner")
        .addExtraConfig("jamfIgnoreAccountNames", svcName);
    GrouperSession grouperSession = setupProvisionerTest(configInput);

    try {
      // role with an out-of-band ignored member already on it (not managed by Grouper)
      insertMockRole("501", "role-a", "Full Access", "Auditor");
      insertMockAccount("990", svcName, "Service Admin");
      insertMockMembership("501", svcName);

      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Group roleGroupA = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:roles:role-a").save();
      roleGroupA.addMember(SubjectTestHelper.SUBJ0, false);

      attachProvisioningAttribute(stem);

      // full sync: Grouper adds SUBJ0; the ignored member must survive the full-list replace
      fullProvision();

      String subj0Email = SubjectTestHelper.SUBJ0.getAttributeValue("email");
      assertTrue("Grouper member should be added", isMember("501", subj0Email));
      assertTrue("ignored out-of-band member must be preserved by a full replace",
          isMember("501", svcName));
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
