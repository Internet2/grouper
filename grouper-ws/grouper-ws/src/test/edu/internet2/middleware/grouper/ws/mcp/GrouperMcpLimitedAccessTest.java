/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperTool;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.mcp.GrouperToolRegistry;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * tests for the two tiers of the MCP sql, admin readonly and admin readwrite categories
 * (GRP-7415).  the all tier is the original group plus a Grouper sysadmin flavor, and gets every
 * tool and external system.  the limited tier is a second group with no sysadmin requirement,
 * whose members only get the tools and external systems opened to them with a
 * limitedAccessGroup in etc:mcp, and for admin readwrite only the loader jobs they could refresh
 * in the UI.  anybody on neither tier gets nothing.
 *
 * <p>the tier config is pointed at test groups, and the MCP membership and wheel caches are
 * cleared after membership changes, so each check sees the current state.</p>
 */
public class GrouperMcpLimitedAccessTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpLimitedAccessTest("testNeitherTierGetsNothing"));
  }

  /**
   *
   */
  public GrouperMcpLimitedAccessTest() {
  }

  /**
   * @param name
   */
  public GrouperMcpLimitedAccessTest(String name) {
    super(name);
  }

  /** folder the tier and wheel test groups go in */
  private static final String TEST_STEM = "test:mcpLimited";

  /** root session for setting up groups */
  private GrouperSession grouperSession;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    this.grouperSession = GrouperSession.startRootSession();

    // the sysadmin flavors, pointed at test groups so the test controls who is in them
    overrideConfig("groups.wheel.use", "true");
    overrideConfig("groups.wheel.group", testGroup("wheel"));
    overrideConfig("groups.wheel.readonly.use", "true");
    overrideConfig("groups.wheel.readonly.group", testGroup("readonlyWheel"));

    // the wheel check throws if the configured group does not exist, so create them empty
    saveGroup(testGroup("wheel"));
    saveGroup(testGroup("readonlyWheel"));

    // the MCP tier groups
    overrideConfig(GrouperToolAccess.GROUP_SQL_READONLY, testGroup("sqlAll"));
    overrideConfig(GrouperToolAccess.GROUP_SQL_READONLY_LIMITED, testGroup("sqlLimited"));
    overrideConfig(GrouperToolAccess.GROUP_ADMIN_READONLY, testGroup("adminReadonlyAll"));
    overrideConfig(GrouperToolAccess.GROUP_ADMIN_READONLY_LIMITED,
        testGroup("adminReadonlyLimited"));
    overrideConfig(GrouperToolAccess.GROUP_ADMIN_READWRITE, testGroup("adminReadWriteAll"));
    overrideConfig(GrouperToolAccess.GROUP_ADMIN_READWRITE_LIMITED,
        testGroup("adminReadWriteLimited"));

    GrouperMcpTestUtils.clearCaches();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(null);
    GrouperMcpTestUtils.clearCaches();
    GrouperSession.stopQuietly(this.grouperSession);
    GrouperContext.deleteDefaultContext();
    super.tearDown();
  }

  /**
   * a member of the all tier group who is not a sysadmin gets nothing from it, the sysadmin
   * requirement is enforced in code
   */
  public void testAllTierRequiresSysadmin() {

    Subject subject = SubjectTestHelper.SUBJ0;
    addMember(testGroup("sqlAll"), subject);
    addMember(testGroup("adminReadonlyAll"), subject);
    addMember(testGroup("adminReadWriteAll"), subject);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);

    assertFalse(GrouperToolAccess.isSqlReadonlyAllTier(subject));
    assertFalse(GrouperToolAccess.isAdminReadonlyAllTier(subject));
    assertFalse(GrouperToolAccess.isAdminReadwriteAllTier(subject));
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolCategory.sql, authUser));
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readonly, authUser));
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readwrite, authUser));

    // a readonly sysadmin gets admin readonly, but not sql or admin readwrite, which take wheel
    addMember(testGroup("readonlyWheel"), subject);
    assertFalse(GrouperToolAccess.isSqlReadonlyAllTier(subject));
    assertTrue(GrouperToolAccess.isAdminReadonlyAllTier(subject));
    assertFalse(GrouperToolAccess.isAdminReadwriteAllTier(subject));

    // a sysadmin gets all three
    addMember(testGroup("wheel"), subject);
    assertTrue(GrouperToolAccess.isSqlReadonlyAllTier(subject));
    assertTrue(GrouperToolAccess.isAdminReadonlyAllTier(subject));
    assertTrue(GrouperToolAccess.isAdminReadwriteAllTier(subject));
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.sql, authUser));
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readonly, authUser));
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readwrite, authUser));

    // a sysadmin who is not in the MCP group still gets nothing, as before
    Subject wheelOnly = SubjectTestHelper.SUBJ1;
    addMember(testGroup("wheel"), wheelOnly);
    assertFalse(GrouperToolAccess.isSqlReadonlyAllTier(wheelOnly));
    assertFalse(GrouperToolAccess.isAdminReadonlyAllTier(wheelOnly));
  }

  /**
   * a caller on the limited sql tier only sees and queries the databases opened to them, is told
   * the same thing about a database which is not configured as about one which is not theirs,
   * and a sysadmin on the all tier sees all of them
   */
  public void testSqlLimitedPerDatabase() {

    overrideConfig("grouper.mcp.sql.mcpLimitedDbA.grouperDatabase", "true");
    overrideConfig("grouper.mcp.sql.mcpLimitedDbB.grouperDatabase", "true");
    overrideConfig("grouper.mcp.sql.mcpLimitedDbB.limitedAccessGroup", mcpGroup("testDbBUsers"));

    Subject limited = SubjectTestHelper.SUBJ2;
    addMember(testGroup("sqlLimited"), limited);
    GrouperMcpAuthUser limitedUser = new GrouperMcpAuthUser(limited);

    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.sql, limitedUser));
    assertFalse(GrouperToolAccess.isAllTier(GrouperToolCategory.sql, limited));
    assertTrue(GrouperToolAccess.isLimitedTier(GrouperToolCategory.sql, limited));

    // nothing opened yet
    assertFalse(GrouperMcpSqlSelect.anyAvailableFor(limitedUser));

    addMember(mcpGroup("testDbBUsers"), limited);
    Set<String> ids = GrouperMcpSqlSelect.externalSystemIdsFor(limitedUser);
    assertTrue(ids.contains("mcpLimitedDbB"));
    assertFalse(ids.contains("mcpLimitedDbA"));
    assertTrue(GrouperMcpSqlSelect.anyAvailableFor(limitedUser));

    assertNull(GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedDbB", limitedUser));
    String error = GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedDbA", limitedUser);
    assertNotNull(error);
    assertTrue(error, error.contains("not available to you"));
    assertFalse("the database not opened to them is not named", error.contains("mcpLimitedDbA,"));

    // a database which does not exist gets the same answer, so they cannot find out which exist
    String notConfigured = GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedNoSuchDb",
        limitedUser);
    assertEquals(error.replace("mcpLimitedDbA", "X"), notConfigured.replace("mcpLimitedNoSuchDb", "X"));

    // the sql tools check databases themselves, so the tool level check lets them through
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolRegistry.find("sql_select"),
        GrouperToolCategory.sql, limitedUser));

    // a sysadmin on the all tier sees every database
    Subject all = SubjectTestHelper.SUBJ3;
    addMember(testGroup("sqlAll"), all);
    addMember(testGroup("wheel"), all);
    GrouperMcpAuthUser allUser = new GrouperMcpAuthUser(all);
    ids = GrouperMcpSqlSelect.externalSystemIdsFor(allUser);
    assertTrue(ids.contains("mcpLimitedDbA"));
    assertTrue(ids.contains("mcpLimitedDbB"));
    assertNull(GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedDbA", allUser));
    error = GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedNoSuchDb", allUser);
    assertTrue("the all tier is told how to configure it", error.contains("not configured"));
  }

  /**
   * a caller on the limited admin readonly tier only gets the tools opened to them, and the ldap
   * tool only on the LDAP systems opened to them
   */
  public void testAdminReadonlyLimitedPerToolAndSystem() {

    Subject limited = SubjectTestHelper.SUBJ4;
    addMember(testGroup("adminReadonlyLimited"), limited);
    GrouperMcpAuthUser limitedUser = new GrouperMcpAuthUser(limited);

    GrouperTool daemonLogs = GrouperToolRegistry.find("admin_daemon_logs");
    GrouperTool configSearch = GrouperToolRegistry.find("admin_config_search");
    GrouperTool ldap = GrouperToolRegistry.find("ldap");

    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readonly, limitedUser));

    // no tool is opened until a limitedAccessGroup is configured and they are in it
    assertFalse(GrouperToolAccess.isAllowed(daemonLogs, GrouperToolCategory.admin_readonly,
        limitedUser));

    overrideConfig("grouper.mcp.tool.admin_daemon_logs.limitedAccessGroup",
        mcpGroup("testDaemonLogsUsers"));
    assertFalse(GrouperToolAccess.isAllowed(daemonLogs, GrouperToolCategory.admin_readonly,
        limitedUser));

    addMember(mcpGroup("testDaemonLogsUsers"), limited);
    assertTrue(GrouperToolAccess.isAllowed(daemonLogs, GrouperToolCategory.admin_readonly,
        limitedUser));
    assertFalse("only the opened tool", GrouperToolAccess.isAllowed(configSearch,
        GrouperToolCategory.admin_readonly, limitedUser));

    // ldap is opened per LDAP system rather than per tool
    overrideConfig("grouper.mcp.ldap.mcpLimitedLdapA.baseDn", "dc=a,dc=edu");
    overrideConfig("grouper.mcp.ldap.mcpLimitedLdapB.baseDn", "dc=b,dc=edu");
    overrideConfig("grouper.mcp.ldap.mcpLimitedLdapB.limitedAccessGroup",
        mcpGroup("testLdapBUsers"));

    assertTrue(GrouperToolAccess.isAllowed(ldap, GrouperToolCategory.admin_readonly,
        limitedUser));
    assertFalse("not offered until a system is opened",
        GrouperMcpLdapSearch.anyAvailableFor(limitedUser));

    addMember(mcpGroup("testLdapBUsers"), limited);
    assertTrue(GrouperMcpLdapSearch.anyAvailableFor(limitedUser));
    Set<String> ids = GrouperMcpLdapSearch.externalSystemIdsFor(limitedUser);
    assertTrue(ids.contains("mcpLimitedLdapB"));
    assertFalse(ids.contains("mcpLimitedLdapA"));
    assertNull(GrouperMcpLdapSearch.validateExternalSystemAllowed("mcpLimitedLdapB",
        limitedUser));
    String error = GrouperMcpLdapSearch.validateExternalSystemAllowed("mcpLimitedLdapA",
        limitedUser);
    assertNotNull(error);
    assertTrue(error, error.contains("not available to you"));

    // a system which does not exist gets the same answer, so they cannot find out which exist
    String notConfigured = GrouperMcpLdapSearch.validateExternalSystemAllowed(
        "mcpLimitedNoSuchLdap", limitedUser);
    assertEquals(error.replace("mcpLimitedLdapA", "X"),
        notConfigured.replace("mcpLimitedNoSuchLdap", "X"));

    // a readonly sysadmin on the all tier gets every tool and every LDAP system
    Subject all = SubjectTestHelper.SUBJ5;
    addMember(testGroup("adminReadonlyAll"), all);
    addMember(testGroup("readonlyWheel"), all);
    GrouperMcpAuthUser allUser = new GrouperMcpAuthUser(all);
    assertTrue(GrouperToolAccess.isAllowed(configSearch, GrouperToolCategory.admin_readonly,
        allUser));
    ids = GrouperMcpLdapSearch.externalSystemIdsFor(allUser);
    assertTrue(ids.contains("mcpLimitedLdapA"));
    assertTrue(ids.contains("mcpLimitedLdapB"));
  }

  /**
   * a limitedAccessGroup has to be in etc:mcp, where MCP cannot change it.  one anywhere else,
   * or one moved out of etc:mcp and still found by its old name, grants nothing
   */
  public void testLimitedAccessGroupMustBeInMcpFolder() {

    overrideConfig("grouper.mcp.sql.mcpLimitedDbC.grouperDatabase", "true");

    Subject limited = SubjectTestHelper.SUBJ7;
    addMember(testGroup("sqlLimited"), limited);
    GrouperMcpAuthUser limitedUser = new GrouperMcpAuthUser(limited);

    // a group outside etc:mcp is ignored even though they are in it
    overrideConfig("grouper.mcp.sql.mcpLimitedDbC.limitedAccessGroup", testGroup("dbCUsers"));
    addMember(testGroup("dbCUsers"), limited);
    assertFalse(GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.sql,
        GrouperToolAccess.SQL_CONFIG_PREFIX, "mcpLimitedDbC", limitedUser));

    // the same for a per tool group
    addMember(testGroup("adminReadonlyLimited"), limited);
    overrideConfig("grouper.mcp.tool.admin_daemon_logs.limitedAccessGroup",
        testGroup("dbCUsers"));
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolRegistry.find("admin_daemon_logs"),
        GrouperToolCategory.admin_readonly, limitedUser));

    // in etc:mcp it counts
    overrideConfig("grouper.mcp.sql.mcpLimitedDbC.limitedAccessGroup", mcpGroup("testDbCUsers"));
    addMember(mcpGroup("testDbCUsers"), limited);
    assertTrue(GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.sql,
        GrouperToolAccess.SQL_CONFIG_PREFIX, "mcpLimitedDbC", limitedUser));

    // moved out of etc:mcp, and still found by its old name, it no longer counts
    Group dbCUsers = saveGroup(mcpGroup("testDbCUsers"));
    Stem outside = new StemSave(this.grouperSession).assignName(TEST_STEM + ":moved")
        .assignCreateParentStemsIfNotExist(true).save();
    new GroupMove(dbCUsers, outside).assignAlternateName(true).save();
    GrouperMcpTestUtils.clearCaches();
    assertFalse(GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.sql,
        GrouperToolAccess.SQL_CONFIG_PREFIX, "mcpLimitedDbC", limitedUser));
  }

  /**
   * somebody on neither tier gets nothing from a tool, even one reached without the category
   * check and even with every limitedAccessGroup, so the tools fail closed
   */
  public void testNeitherTierGetsNothing() {

    overrideConfig("grouper.mcp.sql.mcpLimitedDbD.grouperDatabase", "true");
    overrideConfig("grouper.mcp.sql.mcpLimitedDbD.limitedAccessGroup", mcpGroup("testDbDUsers"));
    overrideConfig("grouper.mcp.ldap.mcpLimitedLdapD.baseDn", "dc=d,dc=edu");
    overrideConfig("grouper.mcp.ldap.mcpLimitedLdapD.limitedAccessGroup",
        mcpGroup("testDbDUsers"));
    overrideConfig("grouper.mcp.tool.admin_daemon_logs.limitedAccessGroup",
        mcpGroup("testDbDUsers"));

    Subject nobody = SubjectTestHelper.SUBJ8;
    addMember(mcpGroup("testDbDUsers"), nobody);
    GrouperMcpAuthUser nobodyUser = new GrouperMcpAuthUser(nobody);

    assertFalse(GrouperToolAccess.isAllowed(GrouperToolRegistry.find("admin_daemon_logs"),
        GrouperToolCategory.admin_readonly, nobodyUser));

    assertFalse(GrouperToolAccess.isExternalSystemAllowed(GrouperToolCategory.sql,
        GrouperToolAccess.SQL_CONFIG_PREFIX, "mcpLimitedDbD", nobodyUser));
    assertTrue(GrouperMcpSqlSelect.externalSystemIdsFor(nobodyUser).isEmpty());
    assertFalse(GrouperMcpSqlSelect.anyAvailableFor(nobodyUser));
    assertEquals(GrouperMcpSqlSelect.NONE_AVAILABLE_MESSAGE,
        GrouperMcpSqlSelect.validateExternalSystemAllowed("mcpLimitedDbD", nobodyUser));

    assertTrue(GrouperMcpLdapSearch.externalSystemIdsFor(nobodyUser).isEmpty());
    assertFalse(GrouperMcpLdapSearch.anyAvailableFor(nobodyUser));
    assertNotNull(GrouperMcpLdapSearch.validateExternalSystemAllowed("mcpLimitedLdapD",
        nobodyUser));

    assertTrue(GrouperMcpAdminExternalSystemGet.externalSystemIdsFor(nobodyUser).isEmpty());
    assertFalse(GrouperMcpAdminExternalSystemGet.anyAvailableFor(nobodyUser));

    // not even the loader job of a group they have ADMIN on
    Group loaderGroup = saveGroup(testGroup("nobodyLoaderGroup"));
    loaderGroup.grantPriv(nobody, AccessPrivilege.ADMIN, false);
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(true);
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(
        "SQL_SIMPLE__" + loaderGroup.getName() + "__" + loaderGroup.getUuid(), nobodyUser));
  }

  /**
   * a caller on the limited admin readwrite tier can only run the loader job of a group they
   * could refresh in the UI, never a provisioning or other daemon job, and nothing at all if
   * this server cannot see grouper-ui.properties
   */
  public void testAdminReadwriteLimitedLoaderJob() {

    // the outcome must not depend on whether the test classpath has grouper-ui.properties
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(true);

    Subject limited = SubjectTestHelper.SUBJ6;
    addMember(testGroup("adminReadWriteLimited"), limited);
    GrouperMcpAuthUser limitedUser = new GrouperMcpAuthUser(limited);

    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readwrite, limitedUser));
    assertFalse(GrouperToolAccess.isAllTier(GrouperToolCategory.admin_readwrite, limited));
    assertTrue(GrouperToolAccess.isLimitedTier(GrouperToolCategory.admin_readwrite, limited));

    // admin readwrite on the limited tier does not give admin readonly
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readonly, limitedUser));

    // the run tool checks each job itself
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolRegistry.find("admin_daemon_job_run"),
        GrouperToolCategory.admin_readwrite, limitedUser));

    Group loaderGroup = saveGroup(testGroup("loaderGroup"));
    String jobName = "SQL_SIMPLE__" + loaderGroup.getName() + "__" + loaderGroup.getUuid();

    // provisioning and other daemon jobs are never allowed on the limited tier
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(
        "OTHER_JOB_rules", limitedUser));
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(
        "OTHER_JOB_provisioner_full_someProvisioner", limitedUser));

    // without ADMIN on the group they cannot run its loader job
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));

    loaderGroup.grantPriv(limited, AccessPrivilege.ADMIN, false);
    assertNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));
    assertTrue(PrivilegeHelper.canRunLoaderJob(limited, loaderGroup));
    assertTrue(PrivilegeHelper.canRunLoaderJobOutsideUi(limited, loaderGroup));

    // the name has to be this group's loader job, not a made up name ending in its uuid
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(
        "SQL_SIMPLE__test:somethingElse__" + loaderGroup.getUuid(), limitedUser));

    // if this server cannot see grouper-ui.properties, it cannot tell how the UI loader settings
    // are set, so it allows nothing rather than going by the defaults.  the UI's own rule is
    // unchanged
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(false);
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));
    assertFalse(PrivilegeHelper.canRunLoaderJobOutsideUi(limited, loaderGroup));
    assertTrue(PrivilegeHelper.canRunLoaderJob(limited, loaderGroup));
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(true);

    // the UI switch which stops group admins refreshing loader jobs applies to MCP too
    overrideUiConfig("uiV2.group.allowGroupAdminsToRefreshLoaderJobs", "false");
    try {
      assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));
      assertFalse(PrivilegeHelper.canRunLoaderJob(limited, loaderGroup));
    } finally {
      GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap()
          .remove("uiV2.group.allowGroupAdminsToRefreshLoaderJobs");
    }
  }

  /**
   * without grouper-ui.properties on this server's classpath, MCP only takes access away: the
   * limited admin readwrite tier can run no job, but wheel and the all tier are unaffected, and
   * so is the UI's own rule
   */
  public void testMissingUiConfigOnlyRemovesAccess() {

    Group loaderGroup = saveGroup(testGroup("missingUiConfigLoaderGroup"));
    String jobName = "SQL_SIMPLE__" + loaderGroup.getName() + "__" + loaderGroup.getUuid();

    // a group admin on the limited tier
    Subject limited = SubjectTestHelper.SUBJ6;
    addMember(testGroup("adminReadWriteLimited"), limited);
    loaderGroup.grantPriv(limited, AccessPrivilege.ADMIN, false);
    GrouperMcpAuthUser limitedUser = new GrouperMcpAuthUser(limited);

    // a sysadmin on the all tier
    Subject all = SubjectTestHelper.SUBJ7;
    addMember(testGroup("adminReadWriteAll"), all);
    addMember(testGroup("wheel"), all);

    // with the file there, the group admin can run their loader job
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(true);
    assertNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));

    // without it, the group admin can run nothing from MCP
    GrouperUiConfigInApi.assignMainConfigFileOnClasspathForTesting(false);
    assertNotNull(GrouperMcpAdminRunDaemonJob.validateLimitedTierJob(jobName, limitedUser));
    assertFalse(PrivilegeHelper.canRunLoaderJobOutsideUi(limited, loaderGroup));

    // the UI's own rule does not change
    assertTrue(PrivilegeHelper.canRunLoaderJob(limited, loaderGroup));

    // a sysadmin still can, and is on the all tier so is not limited per job at all
    assertTrue(PrivilegeHelper.canRunLoaderJobOutsideUi(all, loaderGroup));
    assertTrue(GrouperToolAccess.isAllTier(GrouperToolCategory.admin_readwrite, all));
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readwrite,
        new GrouperMcpAuthUser(all)));

    // the limited tier itself is still allowed in the category, just with no job it can run
    assertTrue(GrouperToolAccess.isAllowed(GrouperToolCategory.admin_readwrite, limitedUser));
  }

  /**
   * the loader editors group decides who can run loader jobs from MCP, so MCP cannot change it
   * even when it is configured outside etc
   */
  public void testLoaderEditorsGroupIsProtected() {

    // the base config sets this as uiV2.loader.edit.if.in.group.elConfig, and an .elConfig is
    // read before the override map, so the override has to be the .elConfig too.  a value with
    // no ${} in it is used as is
    String loaderEditors = testGroup("loaderEditors");
    overrideUiConfig("uiV2.loader.edit.if.in.group.elConfig", loaderEditors);
    GrouperMcpProtectedResources.clearCache();
    try {
      assertEquals(loaderEditors, GrouperUiConfigInApi.retrieveConfig()
          .propertyValueString("uiV2.loader.edit.if.in.group"));
      assertTrue(GrouperMcpProtectedResources.isProtectedGroupName(loaderEditors));
    } finally {
      GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap()
          .remove("uiV2.loader.edit.if.in.group.elConfig");
      GrouperMcpProtectedResources.clearCache();
    }
  }

  /**
   * set a grouper.properties override for this test
   * @param key
   * @param value
   */
  private static void overrideConfig(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  /**
   * set a grouper-ui.properties override, as seen from the API, for this test
   * @param key
   * @param value
   */
  private static void overrideUiConfig(String key, String value) {
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  /**
   * @param extension the group extension
   * @return the name of a group in the test folder, where the tier and wheel groups go
   */
  private static String testGroup(String extension) {
    return TEST_STEM + ":" + extension;
  }

  /**
   * @param extension the group extension
   * @return the name of a group in etc:mcp, where every limitedAccessGroup has to be
   */
  private static String mcpGroup(String extension) {
    return GrouperToolAccess.limitedAccessGroupFolderName() + ":" + extension;
  }

  /**
   * add a subject to a group, creating it if needed, then clear the caches so the next check
   * sees it
   * @param groupName the group
   * @param subject who to add
   */
  private void addMember(String groupName, Subject subject) {
    Group group = saveGroup(groupName);
    group.addMember(subject, false);
    GrouperMcpTestUtils.clearCaches();
  }

  /**
   * create a group if it does not exist
   * @param groupName the group
   * @return the group
   */
  private Group saveGroup(String groupName) {
    return new GroupSave(this.grouperSession).assignName(groupName)
        .assignCreateParentStemsIfNotExist(true).save();
  }

}
