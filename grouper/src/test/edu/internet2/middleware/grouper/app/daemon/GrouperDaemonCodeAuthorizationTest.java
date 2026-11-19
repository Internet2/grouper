/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.app.daemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H4 (daemon admin script authority): members of
 * uiV2.admin.daemonJobs.must.be.in.group who are not wheel or root must not be able to add, edit
 * or delete, from the UI (fromUi true), a daemon type which carries code or runs SQL, since
 * daemons run as GrouperSystem: script daemons (gsh, sql, compiledJava, file), change log script,
 * generic other job and change log consumer class daemons, CSV report, notification with a SQL
 * query population, sync to Grouper from SQL, SFTP to SQL, LDAP to SQL and table sync.  They also
 * must not change the class of any daemon.  They can still manage the other daemon types.  Wheel
 * and root are not limited, and API / GSH callers (fromUi false) are unchanged.
 * <p>
 * SUBJ0 plays the delegated daemon admin (not wheel), SUBJ1 is in the wheel group.  The UI
 * starts a grouper session as the logged-in user before calling insertConfig / editConfig /
 * deleteConfig, so the tests do the same.
 * </p>
 * Before the fix the test does not compile (GrouperDaemonCodeAuthorization is new), and without
 * the GrouperDaemonConfiguration overrides SUBJ0's inserts, edits and deletes succeed.
 */
public class GrouperDaemonCodeAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonCodeAuthorizationTest("testNonWheelCannotInsertScriptDaemonFromUi"));
  }

  /**
   *
   */
  public GrouperDaemonCodeAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperDaemonCodeAuthorizationTest(String name) {
    super(name);
  }

  /** config id of the script daemon in these tests */
  private static final String SCRIPT_CONFIG_ID = "secPatchDaemonScriptDaemon";

  /** harmless gsh written by a sysadmin */
  private static final String SYSADMIN_SOURCE = "System.out.println(\"sysadmin script\");";

  /** what the delegate tries to write */
  private static final String DELEGATE_SOURCE = "System.out.println(\"delegate script\");";

  /** script daemon job class */
  private static final String OTHER_JOB_SCRIPT_CLASS = "edu.internet2.middleware.grouper.app.loader.OtherJobScript";

  /** usdu job class (the configured default of otherJob.usduDaemon.class) */
  private static final String USDU_CLASS = "edu.internet2.middleware.grouper.app.usdu.UsduJob";

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession.startRootSession();
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * a gsh script daemon config with values set as the form would post them
   * @param source
   * @return the config
   */
  private static GrouperDaemonOtherJobScriptConfiguration scriptDaemon(String source) {
    GrouperDaemonOtherJobScriptConfiguration config = new GrouperDaemonOtherJobScriptConfiguration();
    config.setConfigId(SCRIPT_CONFIG_ID);
    config.retrieveAttributes().get("quartzCron").setValue("0 0 5 * * ?");
    config.retrieveAttributes().get("scriptType").setValue("gsh");
    config.retrieveAttributes().get("fileType").setValue("script");
    config.retrieveAttributes().get("scriptSource").setValue(source);
    return config;
  }

  /**
   * the saved script source, or null
   * @return the source
   */
  private static String savedScriptSource() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + SCRIPT_CONFIG_ID + ".scriptSource");
  }

  /**
   * insert as root through the API, as a sysadmin would
   */
  private static void insertScriptDaemonAsRoot() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      List<String> errors = new ArrayList<String>();
      Map<String, String> validationErrors = new HashMap<String, String>();
      scriptDaemon(SYSADMIN_SOURCE).insertConfig(false, new StringBuilder(), errors, validationErrors, new ArrayList<String>());
      assertEquals(GrouperUtil.toStringForLog(errors), 0, errors.size());
      assertEquals(GrouperUtil.toStringForLog(validationErrors), 0, validationErrors.size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertEquals(SYSADMIN_SOURCE, savedScriptSource());
  }

  /**
   * delete the script daemon as root through the API
   */
  private static void deleteScriptDaemonAsRoot() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      GrouperDaemonOtherJobScriptConfiguration config = new GrouperDaemonOtherJobScriptConfiguration();
      config.setConfigId(SCRIPT_CONFIG_ID);
      config.deleteConfig(false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * make SUBJ1 wheel
   * @return SUBJ1
   */
  private static Subject setupWheelSubject() {
    // older releases (6.x and earlier) need an open session for GroupSave and addMember
    GrouperSession rootSession = GrouperSession.startRootSession();
    Group wheel;
    try {
      wheel = new GroupSave(rootSession).assignName("etc:secPatchDaemonWheel").assignCreateParentStemsIfNotExist(true).save();
      wheel.addMember(SubjectTestHelper.SUBJ1, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    return SubjectTestHelper.SUBJ1;
  }

  // ---------------------------------------------------------------------------------------------
  // script daemon, not wheel
  // ---------------------------------------------------------------------------------------------

  /**
   * a non wheel subject cannot add a script daemon from the UI, nothing is saved
   */
  public void testNonWheelCannotInsertScriptDaemonFromUi() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<String> errors = new ArrayList<String>();
      scriptDaemon(DELEGATE_SOURCE).insertConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull(savedScriptSource());
    assertNull(GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob." + SCRIPT_CONFIG_ID + ".class"));
  }

  /**
   * a non wheel subject cannot edit a sysadmin's script daemon from the UI, the source is unchanged
   */
  public void testNonWheelCannotEditScriptDaemonFromUi() {
    insertScriptDaemonAsRoot();
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<String> errors = new ArrayList<String>();
      scriptDaemon(DELEGATE_SOURCE).editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertEquals(SYSADMIN_SOURCE, savedScriptSource());
    deleteScriptDaemonAsRoot();
  }

  /**
   * a non wheel subject cannot delete a sysadmin's script daemon from the UI
   */
  public void testNonWheelCannotDeleteScriptDaemonFromUi() {
    insertScriptDaemonAsRoot();
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperDaemonOtherJobScriptConfiguration config = new GrouperDaemonOtherJobScriptConfiguration();
      config.setConfigId(SCRIPT_CONFIG_ID);
      config.deleteConfig(true);
      fail("delete of a script daemon by a non wheel subject should throw");
    } catch (InsufficientPrivilegeException ipe) {
      // expected
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertEquals(SYSADMIN_SOURCE, savedScriptSource());
    deleteScriptDaemonAsRoot();
  }

  // ---------------------------------------------------------------------------------------------
  // other wheel only types
  // ---------------------------------------------------------------------------------------------

  /**
   * the other daemon types which carry code or run SQL are wheel only too, and an insert from the
   * UI by a non wheel subject is refused before validation or save
   */
  public void testOtherWheelOnlyTypes() {

    List<GrouperDaemonConfiguration> wheelOnly = new ArrayList<GrouperDaemonConfiguration>();
    wheelOnly.add(new GrouperDaemonOtherJobScriptConfiguration());
    // the change log script daemon type is not in every release (e.g. 2.6.19): by name, if present
    GrouperDaemonConfiguration changeLogScript = daemonConfigurationIfInRelease(
        "edu.internet2.middleware.grouper.app.daemon.GrouperDaemonChangeLogScriptConfiguration");
    if (changeLogScript != null) {
      wheelOnly.add(changeLogScript);
    }
    wheelOnly.add(new GrouperDaemonOtherJobConfiguration());
    wheelOnly.add(new GrouperDaemonChangeLogConsumerConfiguration());
    wheelOnly.add(new GrouperDaemonOtherJobCsvReportConfiguration());
    wheelOnly.add(new edu.internet2.middleware.grouper.app.syncToGrouper.GrouperDaemonOtherJobSyncToGrouperFromSqlConfiguration());
    wheelOnly.add(new GrouperDaemonOtherJobSftpToSqlConfiguration());
    wheelOnly.add(new edu.internet2.middleware.grouper.app.ldapToSql.GrouperDaemonOtherJobLdapToSqlConfiguration());
    wheelOnly.add(new GrouperDaemonOtherJobTableSyncConfiguration());

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      for (GrouperDaemonConfiguration config : wheelOnly) {
        assertTrue(config.getClass().getName(), GrouperDaemonCodeAuthorization.isWheelOnlyDaemonType(config));
        config.setConfigId("secPatchDaemon" + config.getClass().getSimpleName());
        List<String> errors = new ArrayList<String>();
        config.insertConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
        assertTrue(config.getClass().getName() + ": " + GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
        assertNull(config.getClass().getName(), GrouperLoaderConfig.retrieveConfig().propertyValueString(config.getConfigItemPrefix() + "quartzCron"));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // ordinary types are not wheel only
    assertFalse(GrouperDaemonCodeAuthorization.isWheelOnlyDaemonType(new GrouperDaemonOtherJobLoaderIncrementalConfiguration()));
    assertFalse(GrouperDaemonCodeAuthorization.isWheelOnlyDaemonType(new GrouperDaemonOtherJobUsduConfiguration()));
    assertFalse(GrouperDaemonCodeAuthorization.isWheelOnlyDaemonType(new GrouperDaemonOtherJobNotificationConfiguration()));

    // class values which run scripts or SQL
    assertTrue(GrouperDaemonCodeAuthorization.isWheelOnlyClassValue(OTHER_JOB_SCRIPT_CLASS));
    assertTrue(GrouperDaemonCodeAuthorization.isWheelOnlyClassValue("edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript"));
    assertFalse(GrouperDaemonCodeAuthorization.isWheelOnlyClassValue(USDU_CLASS));

    // nobody (no subject) is not allowed
    assertFalse(GrouperDaemonCodeAuthorization.isSubjectAllowedToEditWheelOnlyDaemons(null));
  }

  /**
   * a notification daemon is wheel only when its population is a SQL query
   */
  public void testNotificationSqlQueryIsWheelOnly() {

    GrouperDaemonOtherJobNotificationConfiguration config = new GrouperDaemonOtherJobNotificationConfiguration();
    config.setConfigId("secPatchDaemonNotification");
    config.retrieveAttributes().get("populationType").setValue("groupMembership");
    assertFalse(GrouperDaemonCodeAuthorization.isWheelOnly(config));

    config.retrieveAttributes().get("populationType").setValue("sqlQuery");
    assertTrue(GrouperDaemonCodeAuthorization.isWheelOnly(config));

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<String> errors = new ArrayList<String>();
      config.insertConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull(GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.secPatchDaemonNotification.populationType"));
  }

  /**
   * a non wheel subject cannot change the class of a daemon (here the usdu daemon, whose class
   * field is editable) to a script class, but can still edit it without changing the class
   */
  public void testNonWheelCannotChangeClassOnEdit() {

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperDaemonOtherJobUsduConfiguration config = new GrouperDaemonOtherJobUsduConfiguration();
      assertNotNull(config.retrieveAttributes().get("class"));
      config.retrieveAttributes().get("class").setValue(OTHER_JOB_SCRIPT_CLASS);
      List<String> errors = new ArrayList<String>();
      config.editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
      assertEquals(USDU_CLASS, GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.usduDaemon.class"));

      // any other class change is refused too
      config = new GrouperDaemonOtherJobUsduConfiguration();
      config.retrieveAttributes().get("class").setValue("edu.internet2.middleware.grouper.app.loader.GrouperDaemonDeleteOldRecords");
      errors = new ArrayList<String>();
      config.editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
      assertEquals(USDU_CLASS, GrouperLoaderConfig.retrieveConfig().propertyValueString("otherJob.usduDaemon.class"));

      // same class, other field changed: allowed
      config = new GrouperDaemonOtherJobUsduConfiguration();
      config.retrieveAttributes().get("quartzCron").setValue("0 0 4 * * ?");
      errors = new ArrayList<String>();
      config.editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertFalse(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // controls: what is still allowed
  // ---------------------------------------------------------------------------------------------

  /**
   * a non wheel subject manages but does not author daemons (decision 2026-11-10): adding any daemon
   * is refused, an edit that changes only the schedule is allowed, an edit of any other setting is
   * refused, and deleting is refused.  The incremental loader is used because a delegate could
   * previously author it (it runs as root on a table the delegate picks)
   */
  public void testNonWheelManagesButDoesNotAuthorDaemons() {

    String configId = "secPatchDaemonIncrementalLoader";
    String prefix = "otherJob." + configId + ".";

    // a delegate cannot add one
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperDaemonOtherJobLoaderIncrementalConfiguration config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.retrieveAttributes().get("quartzCron").setValue("0 0 5 * * ?");
      config.retrieveAttributes().get("tableName").setValue("secPatchDaemon_incremental");
      List<String> errors = new ArrayList<String>();
      config.insertConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
      assertNull(GrouperLoaderConfig.retrieveConfig().propertyValueString(prefix + "tableName"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // a sysadmin adds it
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      GrouperDaemonOtherJobLoaderIncrementalConfiguration config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.retrieveAttributes().get("quartzCron").setValue("0 0 5 * * ?");
      config.retrieveAttributes().get("tableName").setValue("secPatchDaemon_incremental");
      List<String> errors = new ArrayList<String>();
      Map<String, String> validationErrors = new HashMap<String, String>();
      config.insertConfig(true, new StringBuilder(), errors, validationErrors, new ArrayList<String>());
      assertEquals(GrouperUtil.toStringForLog(errors), 0, errors.size());
      assertEquals(GrouperUtil.toStringForLog(validationErrors), 0, validationErrors.size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      // the schedule only: allowed (the other fields are posted with their saved values, as the form does)
      GrouperDaemonOtherJobLoaderIncrementalConfiguration config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.retrieveAttributes().get("quartzCron").setValue("0 0 6 * * ?");
      config.retrieveAttributes().get("tableName").setValue("secPatchDaemon_incremental");
      List<String> errors = new ArrayList<String>();
      Map<String, String> validationErrors = new HashMap<String, String>();
      config.editConfig(true, new StringBuilder(), errors, validationErrors, new ArrayList<String>());
      assertEquals(GrouperUtil.toStringForLog(errors), 0, errors.size());
      assertEquals("0 0 6 * * ?", GrouperLoaderConfig.retrieveConfig().propertyValueString(prefix + "quartzCron"));

      // another setting: refused, nothing changes
      config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.retrieveAttributes().get("quartzCron").setValue("0 0 6 * * ?");
      config.retrieveAttributes().get("tableName").setValue("secPatchDaemon_otherTable");
      errors = new ArrayList<String>();
      config.editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
      assertEquals("secPatchDaemon_incremental", GrouperLoaderConfig.retrieveConfig().propertyValueString(prefix + "tableName"));

      // the schedule as a new expression (would be saved as quartzCron.elConfig and evaluated
      // with static classes by the config loader): refused, nothing changes (2026-11-12)
      config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.retrieveAttributes().get("quartzCron").setExpressionLanguage(true);
      config.retrieveAttributes().get("quartzCron").setExpressionLanguageScript("${'0 0 7' + ' * * ?'}");
      config.retrieveAttributes().get("tableName").setValue("secPatchDaemon_incremental");
      errors = new ArrayList<String>();
      config.editConfig(true, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertTrue(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
      assertEquals("0 0 6 * * ?", GrouperLoaderConfig.retrieveConfig().propertyValueString(prefix + "quartzCron"));

      // delete: refused
      config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      try {
        config.deleteConfig(true);
        fail("a delegate must not delete a daemon");
      } catch (InsufficientPrivilegeException ipe) {
        // expected
      }
      assertEquals("0 0 6 * * ?", GrouperLoaderConfig.retrieveConfig().propertyValueString(prefix + "quartzCron"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // clean up as root
    rootSession = GrouperSession.startRootSession();
    try {
      GrouperDaemonOtherJobLoaderIncrementalConfiguration config = new GrouperDaemonOtherJobLoaderIncrementalConfiguration();
      config.setConfigId(configId);
      config.deleteConfig(false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * wheel and root can add, edit and delete a script daemon from the UI
   */
  public void testWheelAndRootCanManageScriptDaemonFromUi() {

    // root adds
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      List<String> errors = new ArrayList<String>();
      Map<String, String> validationErrors = new HashMap<String, String>();
      scriptDaemon(SYSADMIN_SOURCE).insertConfig(true, new StringBuilder(), errors, validationErrors, new ArrayList<String>());
      assertEquals(GrouperUtil.toStringForLog(errors), 0, errors.size());
      assertEquals(GrouperUtil.toStringForLog(validationErrors), 0, validationErrors.size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertEquals(SYSADMIN_SOURCE, savedScriptSource());

    Subject wheelSubject = setupWheelSubject();

    // wheel edits and deletes
    GrouperSession grouperSession = GrouperSession.start(wheelSubject);
    try {
      List<String> errors = new ArrayList<String>();
      Map<String, String> validationErrors = new HashMap<String, String>();
      scriptDaemon(DELEGATE_SOURCE).editConfig(true, new StringBuilder(), errors, validationErrors, new ArrayList<String>());
      assertEquals(GrouperUtil.toStringForLog(errors), 0, errors.size());
      assertEquals(GrouperUtil.toStringForLog(validationErrors), 0, validationErrors.size());
      assertEquals(DELEGATE_SOURCE, savedScriptSource());

      GrouperDaemonOtherJobScriptConfiguration config = new GrouperDaemonOtherJobScriptConfiguration();
      config.setConfigId(SCRIPT_CONFIG_ID);
      config.deleteConfig(true);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull(savedScriptSource());
  }

  /**
   * API and GSH callers (fromUi false) are not changed by this fix
   */
  public void testApiPathUnchanged() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      List<String> errors = new ArrayList<String>();
      scriptDaemon(SYSADMIN_SOURCE).insertConfig(false, new StringBuilder(), errors, new HashMap<String, String>(), new ArrayList<String>());
      assertFalse(GrouperUtil.toStringForLog(errors), errors.contains(GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertEquals(SYSADMIN_SOURCE, savedScriptSource());
    deleteScriptDaemonAsRoot();
  }

  /**
   * @param className daemon configuration class
   * @return a new instance, or null if this release does not have the class (looked up by name so
   * this one file compiles on every release)
   */
  private static GrouperDaemonConfiguration daemonConfigurationIfInRelease(String className) {
    Class<?> theClass = null;
    try {
      theClass = Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      System.out.println("GrouperDaemonCodeAuthorizationTest: no " + className + " in this release, skipped");
      return null;
    }
    try {
      return (GrouperDaemonConfiguration)theClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
