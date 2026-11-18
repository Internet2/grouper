package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * A delegated loader editor (not wheel or root) may only save a SQL or LDAP loader whose groups
 * (SQL group list query and group query results, and-groups, groups-like value) are under a
 * folder the editor administers, and a group list loader saved by such an editor stops at run
 * time (nothing written) if its result names a group outside those folders.  Wheel and root are
 * not limited, and loaders with no recorded editor run as before.
 *
 * Checks GrouperLoaderTargetAuthorization (the call UiV2GrouperLoader.editGrouperLoaderSave makes
 * with the submitted fields, and the call GrouperLoaderType.syncGroupList makes before writing),
 * then runs a real SQL_GROUP_LIST loader that names a group the editor cannot manage (standing in
 * for the wheel group): with the editor recorded nothing is written; the control run (record
 * removed, as after a wheel save) writes the membership, which shows the loader works here.
 * Before the fix the test does not compile (the class does not exist) and the loader writes the
 * protected group.
 *
 * More limits (2026-11-13): a delegated editor needs READ on every and-group (save, import and
 * diagnostics), may not set the LDAP results transformation class, and may not use the Grouper
 * database; the import (checkImportAndRecordEditor, called by GrouperLoaderImportExport) applies
 * the same rules and records the editor.  The import is tested through the helper so the test
 * compiles on releases without the import screen.
 *
 * Queries run against the Grouper database (loader database "grouper"), so they select from
 * grouper_groups / grouper_members to work on every database.
 *
 * Same file for every release (2.6.19 on): Java 8, no commons-lang import, GrouperTest base.
 * </pre>
 */
public class GrouperLoaderTargetAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderTargetAuthorizationTest("testRunTimeLoaderWritesNothing"));
  }

  /** */
  public GrouperLoaderTargetAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperLoaderTargetAuthorizationTest(String name) {
    super(name);
  }

  /** folder the editor administers */
  private static final String MINE = "test:secPatch192:mine";

  /** a group the editor has no rights on (stands in for the wheel group) */
  private static final String PROTECTED_GROUP = "test:secPatch192:protected:target";

  /** root session */
  private GrouperSession rootSession;

  /** existing group used as the one-row anchor of the test queries */
  private Group anchorGroup;

  /** the protected group */
  private Group protectedGroup;

  // SUBJ0: non-wheel loader editor with STEM_ADMIN on MINE, SUBJ3: wheel

  @Override
  protected void setUp() {
    // the registry reset deletes child attribute assigns, then the rest; Grouper can recreate
    // built-in assign-on-assign rows (attribute auto create) in the background in between, which
    // fails the second delete with a foreign key error when this class runs after other tests.
    // That is a test-harness race, not this fix: retry the reset once after a short pause
    try {
      super.setUp();
    } catch (RuntimeException re) {
      if (!String.valueOf(GrouperUtil.getFullStackTrace(re)).contains("fk_attr_assign_")) {
        throw re;
      }
      System.out.println("GrouperLoaderTargetAuthorizationTest: registry reset raced a background write, retrying once");
      GrouperUtil.sleep(2000);
      super.setUp();
    }
    this.rootSession = GrouperSession.startRootSession();

    Group wheel = new GroupSave(this.rootSession).assignName("test:secPatch192wheel")
        .assignCreateParentStemsIfNotExist(true).save();
    wheel.addMember(SubjectTestHelper.SUBJ3, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    PrivilegeHelper.wheelMemberCacheClear();

    Stem mine = new StemSave(this.rootSession).assignName(MINE).assignCreateParentStemsIfNotExist(true).save();
    mine.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    this.anchorGroup = new GroupSave(this.rootSession).assignName(MINE + ":anchor").save();
    this.protectedGroup = new GroupSave(this.rootSession).assignName(PROTECTED_GROUP)
        .assignCreateParentStemsIfNotExist(true).save();

    // the editor needs a member row for the run-time loader query
    MemberFinder.findBySubject(this.rootSession, SubjectTestHelper.SUBJ0, true);
  }

  /** loader thread pool switches turned off for the run-time loader test */
  private static final String[] LOADER_THREAD_KEYS = {"loader.use.groupThreads", "loader.use.membershipThreads",
      "loader.incrementalThreads"};

  @Override
  protected void tearDown() {
    PrivilegeHelper.wheelMemberCacheClear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.use");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.wheel.group");
    for (String key : LOADER_THREAD_KEYS) {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * a query that returns one row with the given group_name
   * @param groupName
   * @return the sql
   */
  private String groupNameQuery(String groupName) {
    return "select '" + groupName + "' as group_name from grouper_groups where name = '" + this.anchorGroup.getName() + "'";
  }

  /**
   * the save check the loader screen makes for a SQL loader
   * @param subject
   * @param sqlType SQL_SIMPLE or SQL_GROUP_LIST
   * @param query
   * @param groupQuery
   * @param groupsLike
   * @param andGroups
   * @return null if allowed, else the message
   */
  private static String sqlSave(Subject subject, String sqlType, String query, String groupQuery, String groupsLike, String andGroups) {
    return GrouperLoaderTargetAuthorization.checkUiSave(subject, true, "SQL", sqlType, "grouper", query, groupQuery,
        null, groupsLike, andGroups);
  }

  /**
   * a SQL loader group, set up as root as the loader screen does
   * @param name
   * @param type
   * @param query
   * @return the group
   */
  private Group sqlLoaderGroup(String name, String type, String query) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    AttributeDefName loaderType = AttributeDefNameFinder.findByName(
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc")
        + ":legacy:attribute:legacyGroupType_grouperLoader", true);
    group.getAttributeDelegate().assignAttribute(loaderType);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_TYPE, type, false);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_DB_NAME, "grouper", false);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_SCHEDULE_TYPE, "CRON", false);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_QUARTZ_CRON, "0 0 5 * * ?", false);
    group.setAttribute(GrouperLoader.GROUPER_LOADER_QUERY, query, false);
    return group;
  }

  /**
   * @param subject
   * @return member id
   */
  private String memberId(Subject subject) {
    Member member = MemberFinder.findBySubject(this.rootSession, subject, true);
    return member.getUuid();
  }

  // ---------------------------------------------------------------- tests

  /**
   * folder name helpers
   */
  public void testNameHelpers() {
    assertEquals(Arrays.asList("a:b", "a"), GrouperLoaderTargetAuthorization.ancestorFolderNames("a:b:c"));
    assertEquals(0, GrouperLoaderTargetAuthorization.ancestorFolderNames("top").size());
    assertEquals("a:b", GrouperLoaderTargetAuthorization.groupsLikeFolderName("a:b:%"));
    assertEquals("a:b", GrouperLoaderTargetAuthorization.groupsLikeFolderName("a:b:x%"));
    // _ is a LIKE wildcard, so the limit is the folder before it
    assertEquals("a", GrouperLoaderTargetAuthorization.groupsLikeFolderName("a:my_groups:%"));
    assertNull(GrouperLoaderTargetAuthorization.groupsLikeFolderName("%"));
    assertNull(GrouperLoaderTargetAuthorization.groupsLikeFolderName("abc%"));
  }

  /**
   * the reported attack: a non-wheel SQL_GROUP_LIST whose query names a group outside the
   * editor's folders (here the protected group, in the report the wheel group) is refused at
   * save, the UI call throws a veto and records nothing
   */
  public void testNonWheelGroupListOutsideFoldersRefused() {
    String error = sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(PROTECTED_GROUP), null, null, null);
    assertNotNull(error);
    assertTrue(error, error.startsWith(GrouperLoaderTargetAuthorization.MESSAGE_SAVE_PREFIX));
    assertTrue(error, error.contains(PROTECTED_GROUP));

    // a query naming one allowed and one refused group is refused
    error = sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST",
        groupNameQuery(MINE + ":a") + " union " + groupNameQuery(PROTECTED_GROUP), null, null, null);
    assertNotNull(error);
    assertFalse(error, error.contains(MINE + ":a"));

    Group loaderGroup = new GroupSave(this.rootSession).assignName(MINE + ":loaderRefused").save();
    try {
      GrouperLoaderTargetAuthorization.assertUiSaveAllowedAndRecordEditor(SubjectTestHelper.SUBJ0, loaderGroup, true, "SQL",
          "SQL_GROUP_LIST", "grouper", groupNameQuery(PROTECTED_GROUP), null, null, null, null);
      fail("expected a veto");
    } catch (HookVeto hookVeto) {
      assertTrue(hookVeto.getReason(), hookVeto.getReason().startsWith(GrouperLoaderTargetAuthorization.MESSAGE_SAVE_PREFIX));
    }
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(loaderGroup));
  }

  /**
   * the group query, and-groups and groups-like are checked too
   */
  public void testGroupQueryAndGroupsGroupsLikeChecked() {
    assertNotNull(sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(MINE + ":a"),
        groupNameQuery(PROTECTED_GROUP), null, null));
    assertNotNull(sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(MINE + ":a"), null, null, PROTECTED_GROUP));
    // and-groups are checked for simple loaders too
    assertNotNull(sqlSave(SubjectTestHelper.SUBJ0, "SQL_SIMPLE", "select subject_id from grouper_members", null, null,
        MINE + ":anchor, " + PROTECTED_GROUP));
    String error = sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(MINE + ":a"), null, "test:%", null);
    assertNotNull(error);
    assertTrue(error, error.contains(GrouperLoaderTargetAuthorization.GROUPS_LIKE_LABEL + "test:%"));
    assertNotNull(sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(MINE + ":a"), null, "%", null));

    // controls: all inside the editor's folder
    assertNull(sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", groupNameQuery(MINE + ":a"),
        groupNameQuery(MINE + ":sub:b"), MINE + ":%", MINE + ":anchor"));
    // the LDAP group list: groups like and and-groups at save (names at run time)
    assertNotNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, true, "LDAP", null, null, null, null,
        "LDAP_GROUP_LIST", "test:%", null));
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, true, "LDAP", null, null, null, null,
        "LDAP_GROUP_LIST", MINE + ":%", null));
  }

  /**
   * a query that cannot be run is refused (fail closed), removing a loader and other types are
   * not checked
   */
  public void testQueryErrorAndOtherCases() {
    assertEquals(GrouperLoaderTargetAuthorization.MESSAGE_SAVE_QUERY_FAILED,
        sqlSave(SubjectTestHelper.SUBJ0, "SQL_GROUP_LIST", "select group_name from no_such_table_secpatch192", null, null, null));
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, false, "SQL", "SQL_GROUP_LIST", "grouper",
        groupNameQuery(PROTECTED_GROUP), null, null, null, null));
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, true, "JEXL_SCRIPT", null, null,
        null, null, null, null, PROTECTED_GROUP));
  }

  /**
   * an allowed non-wheel save records the editor; a wheel save is allowed with any groups and
   * removes the record; root is allowed too
   */
  public void testRecordAndWheel() {
    Group loaderGroup = new GroupSave(this.rootSession).assignName(MINE + ":loaderRecord").save();
    GrouperLoaderTargetAuthorization.assertUiSaveAllowedAndRecordEditor(SubjectTestHelper.SUBJ0, loaderGroup, true, "SQL",
        "SQL_GROUP_LIST", "grouper", groupNameQuery(MINE + ":a"), null, null, null, null);
    assertEquals(memberId(SubjectTestHelper.SUBJ0), GrouperLoaderTargetAuthorization.recordedEditorMemberId(loaderGroup));

    assertNull(sqlSave(SubjectTestHelper.SUBJ3, "SQL_GROUP_LIST", groupNameQuery(PROTECTED_GROUP), null, "%", PROTECTED_GROUP));
    assertNull(sqlSave(SubjectFinder.findRootSubject(), "SQL_GROUP_LIST", groupNameQuery(PROTECTED_GROUP), null, null, null));

    GrouperLoaderTargetAuthorization.assertUiSaveAllowedAndRecordEditor(SubjectTestHelper.SUBJ3, loaderGroup, true, "SQL",
        "SQL_GROUP_LIST", "grouper", groupNameQuery(PROTECTED_GROUP), null, null, null, null);
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(loaderGroup));
  }

  /**
   * the run-time check on the names the loader got
   */
  public void testRunTimeCheck() {
    Group loaderGroup = new GroupSave(this.rootSession).assignName(MINE + ":loaderRun").save();

    // no record: runs as before
    assertNull(GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(PROTECTED_GROUP), null, null, null));

    GrouperLoaderTargetAuthorization.recordEditor(loaderGroup, SubjectTestHelper.SUBJ0);
    String error = GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(MINE + ":a"),
        Arrays.asList(PROTECTED_GROUP), null, null);
    assertNotNull(error);
    assertTrue(error, error.startsWith(GrouperLoaderTargetAuthorization.MESSAGE_RUN_PREFIX));
    assertTrue(error, error.contains(PROTECTED_GROUP));

    List<Group> andGroups = new ArrayList<Group>();
    andGroups.add(this.protectedGroup);
    assertNotNull(GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(MINE + ":a"), null, andGroups, null));
    assertNotNull(GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(MINE + ":a"), null, null, "test:%"));
    assertNull(GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(MINE + ":a", MINE + ":b:c"), null, null,
        MINE + ":%"));

    try {
      GrouperLoaderTargetAuthorization.assertRunAllowed(loaderGroup.getUuid(), Arrays.asList(PROTECTED_GROUP), null, null, null);
      fail("expected a refusal");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().startsWith(GrouperLoaderTargetAuthorization.MESSAGE_RUN_PREFIX));
    }

    // the editor becomes wheel: allowed
    Group wheel = new GroupSave(this.rootSession).assignName("test:secPatch192wheel").save();
    wheel.addMember(SubjectTestHelper.SUBJ0, false);
    PrivilegeHelper.wheelMemberCacheClear();
    assertNull(GrouperLoaderTargetAuthorization.checkRun(loaderGroup.getUuid(), Arrays.asList(PROTECTED_GROUP), null, null, null));
  }

  /**
   * a real SQL_GROUP_LIST loader saved by the non-wheel editor that names the protected group
   * writes nothing; control: the same loader with no editor record (as after a wheel save) writes it
   */
  public void testRunTimeLoaderWritesNothing() {
    // run the loader on this thread only: the refused run ends in an exception, and pool threads
    // could still be writing loader metadata when the next test resets the registry
    for (String key : LOADER_THREAD_KEYS) {
      GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(key, "false");
    }
    String query = "select '" + PROTECTED_GROUP + "' as group_name, subject_id, subject_source as subject_source_id "
        + "from grouper_members where subject_id = '" + SubjectTestHelper.SUBJ0.getId() + "' and subject_source = '"
        + SubjectTestHelper.SUBJ0.getSourceId() + "'";
    Group loaderGroup = sqlLoaderGroup(MINE + ":loaderGroupList", "SQL_GROUP_LIST", query);
    GrouperLoaderTargetAuthorization.recordEditor(loaderGroup, SubjectTestHelper.SUBJ0);

    try {
      GrouperLoader.runJobOnceForGroup(this.rootSession, loaderGroup);
    } catch (RuntimeException re) {
      // the refused run ends in error
    }
    assertFalse("refused run must not write the protected group", this.protectedGroup.hasImmediateMember(SubjectTestHelper.SUBJ0));

    // control: no record (as after a wheel save), the loader writes the group
    GrouperLoaderTargetAuthorization.recordEditor(loaderGroup, SubjectTestHelper.SUBJ3);
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(loaderGroup));
    GrouperLoader.runJobOnceForGroup(this.rootSession, loaderGroup);
    assertTrue("control run writes the group", this.protectedGroup.hasImmediateMember(SubjectTestHelper.SUBJ0));
  }

  // ---------------------------------------------------------------- more limits (2026-11-13)

  /**
   * @param readable true to grant SUBJ0 READ, false to take READ away from everyone
   * @param name group name
   * @return the and-group
   */
  private Group andGroup(String name, boolean readable) {
    Group group = new GroupSave(this.rootSession).assignName(name).assignCreateParentStemsIfNotExist(true).save();
    group.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    if (readable) {
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    }
    return group;
  }

  /**
   * the full save check for an LDAP loader (no database involved)
   * @param subject
   * @param ldapType
   * @param groupsLike
   * @param andGroups
   * @param transformationClass
   * @return null if allowed, else the message
   */
  private static String ldapSaveFull(Subject subject, String ldapType, String groupsLike, String andGroups, String transformationClass) {
    return GrouperLoaderTargetAuthorization.checkUiSave(subject, null, true, "LDAP", null, null, null, null, ldapType, groupsLike,
        andGroups, transformationClass);
  }

  /**
   * a non-wheel editor needs READ on every and-group; a missing group gets the same answer
   */
  public void testAndGroupsNeedRead() {
    Group privateAnd = andGroup(MINE + ":andPrivate", false);
    Group readableAnd = andGroup(MINE + ":andReadable", true);

    String error = ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, privateAnd.getName(), null);
    assertNotNull(error);
    assertTrue(error, error.startsWith(GrouperLoaderTargetAuthorization.MESSAGE_AND_GROUPS_PREFIX));
    assertTrue(error, error.contains(privateAnd.getName()));

    // same message for a group that does not exist (no existence test)
    error = ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, MINE + ":noSuchGroupSecPatch192", null);
    assertNotNull(error);
    assertTrue(error, error.startsWith(GrouperLoaderTargetAuthorization.MESSAGE_AND_GROUPS_PREFIX));

    // one readable and one not: refused, only the unreadable one named
    error = ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, readableAnd.getName() + ", " + privateAnd.getName(), null);
    assertNotNull(error);
    assertFalse(error, error.contains(readableAnd.getName()));

    // controls
    assertNull(ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, readableAnd.getName(), null));
    assertNull(ldapSaveFull(SubjectTestHelper.SUBJ3, "LDAP_SIMPLE", null, privateAnd.getName(), null));

    // the UI call throws a veto and records nothing
    Group loaderGroup = new GroupSave(this.rootSession).assignName(MINE + ":loaderAndRead").save();
    try {
      GrouperLoaderTargetAuthorization.assertUiSaveAllowedAndRecordEditor(SubjectTestHelper.SUBJ0, loaderGroup, true, "LDAP",
          null, null, null, null, "LDAP_SIMPLE", null, privateAnd.getName(), null);
      fail("expected a veto");
    } catch (HookVeto hookVeto) {
      assertTrue(hookVeto.getReason(), hookVeto.getReason().startsWith(GrouperLoaderTargetAuthorization.MESSAGE_AND_GROUPS_PREFIX));
    }
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(loaderGroup));

    // diagnostics: refused before the stored and-groups are looked up as root
    Group sqlLoader = sqlLoaderGroup(MINE + ":loaderDiagnostics", "SQL_SIMPLE", "select subject_id from grouper_members");
    sqlLoader.setAttribute(GrouperLoader.GROUPER_LOADER_AND_GROUPS, privateAnd.getName(), false);
    error = GrouperLoaderTargetAuthorization.checkDiagnostics(SubjectTestHelper.SUBJ0, sqlLoader);
    assertNotNull(error);
    assertTrue(error, error.startsWith(GrouperLoaderTargetAuthorization.MESSAGE_DIAGNOSTICS_PREFIX));
    assertNull(GrouperLoaderTargetAuthorization.checkDiagnostics(SubjectTestHelper.SUBJ3, sqlLoader));
    sqlLoader.setAttribute(GrouperLoader.GROUPER_LOADER_AND_GROUPS, readableAnd.getName(), false);
    assertNull(GrouperLoaderTargetAuthorization.checkDiagnostics(SubjectTestHelper.SUBJ0, sqlLoader));
  }

  /**
   * only wheel or root may set the LDAP results transformation class
   */
  public void testTransformationClassWheelOnly() {
    String className = "edu.internet2.middleware.grouper.app.loader.ldap.LdapResultsTransformationBase";
    assertEquals(GrouperLoaderTargetAuthorization.MESSAGE_TRANSFORMATION_CLASS,
        ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, null, className));
    // a group with no stored value: any value is a change
    Group loaderGroup = new GroupSave(this.rootSession).assignName(MINE + ":loaderTransform").save();
    assertEquals(GrouperLoaderTargetAuthorization.MESSAGE_TRANSFORMATION_CLASS,
        GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, loaderGroup, true, "LDAP", null, null, null, null,
            "LDAP_SIMPLE", null, null, className));
    // controls: no class, or wheel
    assertNull(ldapSaveFull(SubjectTestHelper.SUBJ0, "LDAP_SIMPLE", null, null, null));
    assertNull(ldapSaveFull(SubjectTestHelper.SUBJ3, "LDAP_SIMPLE", null, null, className));
  }

  /**
   * a non-wheel SQL loader may not use the Grouper database (or one that cannot be looked up)
   */
  public void testGrouperDatabaseRefused() {
    assertTrue(GrouperLoaderTargetAuthorization.isGrouperDatabase("grouper"));
    assertTrue(GrouperLoaderTargetAuthorization.isGrouperDatabase(" GROUPER "));
    assertTrue(GrouperLoaderTargetAuthorization.isGrouperDatabase(null));
    assertTrue(GrouperLoaderTargetAuthorization.isGrouperDatabase("noSuchDbSecPatch192"));

    for (String dbName : new String[] {"grouper", "Grouper", "", "noSuchDbSecPatch192"}) {
      assertEquals(dbName, GrouperLoaderTargetAuthorization.MESSAGE_GROUPER_DATABASE,
          GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, null, true, "SQL", "SQL_SIMPLE", dbName,
              "select subject_id from grouper_members", null, null, null, null, null));
    }
    // wheel and root may
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ3, null, true, "SQL", "SQL_GROUP_LIST", "grouper",
        groupNameQuery(PROTECTED_GROUP), null, null, null, null, null));
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectFinder.findRootSubject(), null, true, "SQL", "SQL_SIMPLE", "grouper",
        "select subject_id from grouper_members", null, null, null, null, null));
    // removing a loader is not checked
    assertNull(GrouperLoaderTargetAuthorization.checkUiSave(SubjectTestHelper.SUBJ0, null, false, "SQL", "SQL_SIMPLE", "grouper",
        null, null, null, null, null, null));
  }

  /**
   * @param loaderType
   * @param keysAndValues attribute name, value, name, value...
   * @return the export json
   */
  private static String importJson(String loaderType, String... keysAndValues) {
    StringBuilder json = new StringBuilder("{\"loaderType\":\"" + loaderType + "\",\"attributes\":[");
    for (int i = 0; i < keysAndValues.length; i += 2) {
      if (i > 0) {
        json.append(",");
      }
      json.append("{\"attributeName\":\"").append(keysAndValues[i]).append("\",\"attributeValue\":\"")
          .append(keysAndValues[i + 1]).append("\"}");
    }
    return json.append("]}").toString();
  }

  /**
   * @param subject
   * @param json
   * @param group
   * @return the import errors, checked with subject as the session user
   */
  private List<String> importAs(Subject subject, String json, Group group) {
    GrouperSession.stopQuietly(this.rootSession);
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      return GrouperLoaderTargetAuthorization.checkImportAndRecordEditor(json, group);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      this.rootSession = GrouperSession.startRootSession();
    }
  }

  /**
   * the config import applies the same rules, and records the editor when allowed
   */
  public void testImportChecked() {
    assertEquals("grouperLoaderQuery", GrouperLoaderTargetAuthorization.importAttributeKey(
        "etc:legacy:attribute:legacyAttribute_grouperLoaderQuery"));
    assertEquals("grouperLoaderGroupQuery", GrouperLoaderTargetAuthorization.importAttributeKey(
        "etc:legacy:attribute:legacyAttribute_grouperLoaderGroupQuery"));
    assertEquals("grouperLoaderLdapAndGroups", GrouperLoaderTargetAuthorization.importAttributeKey(
        "etc:attribute:loaderLdap:grouperLoaderLdapAndGroups"));

    String legacy = "etc:legacy:attribute:legacyAttribute_";
    String ldap = "etc:attribute:loaderLdap:";
    Group privateAnd = andGroup(MINE + ":andPrivateImport", false);
    Group target = new GroupSave(this.rootSession).assignName(MINE + ":importTarget").save();

    // SQL on the grouper database, and SQL with no database (the default)
    List<String> errors = importAs(SubjectTestHelper.SUBJ0, importJson("SQL", legacy + "grouperLoaderType", "SQL_SIMPLE",
        legacy + "grouperLoaderDbName", "grouper", legacy + "grouperLoaderQuery", "select subject_id from grouper_members"), target);
    assertEquals(Arrays.asList(GrouperLoaderTargetAuthorization.MESSAGE_GROUPER_DATABASE), errors);
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("SQL", legacy + "grouperLoaderQuery",
        "select subject_id from grouper_members"), target);
    assertEquals(Arrays.asList(GrouperLoaderTargetAuthorization.MESSAGE_GROUPER_DATABASE), errors);

    // LDAP transformation class, unreadable and-group, groups-like outside the editor's folders
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_SIMPLE",
        ldap + "grouperLoaderLdapResultsTransformationClass", "some.Clazz"), target);
    assertEquals(Arrays.asList(GrouperLoaderTargetAuthorization.MESSAGE_TRANSFORMATION_CLASS), errors);
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_SIMPLE",
        ldap + "grouperLoaderLdapAndGroups", privateAnd.getName()), target);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0), errors.get(0).startsWith(GrouperLoaderTargetAuthorization.MESSAGE_AND_GROUPS_PREFIX));
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_GROUP_LIST",
        ldap + "grouperLoaderLdapGroupsLike", "test:%"), target);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0), errors.get(0).startsWith(GrouperLoaderTargetAuthorization.MESSAGE_SAVE_PREFIX));
    // an LDAP loader with no type is checked as a group list
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapGroupsLike", "test:%"), target);
    assertEquals(1, errors.size());
    // a key named twice
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_SIMPLE",
        ldap + "grouperLoaderLdapType", "LDAP_GROUP_LIST"), target);
    assertEquals(Arrays.asList(GrouperLoaderTargetAuthorization.MESSAGE_IMPORT_DUPLICATE), errors);
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(target));

    // allowed: LDAP group list inside the editor's folder, records the editor
    errors = importAs(SubjectTestHelper.SUBJ0, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_GROUP_LIST",
        ldap + "grouperLoaderLdapGroupsLike", MINE + ":%"), target);
    assertEquals(0, errors.size());
    assertEquals(memberId(SubjectTestHelper.SUBJ0), GrouperLoaderTargetAuthorization.recordedEditorMemberId(target));

    // wheel may import anything and leaves no record; other loader types are not checked here
    assertEquals(0, importAs(SubjectTestHelper.SUBJ3, importJson("LDAP", ldap + "grouperLoaderLdapType", "LDAP_GROUP_LIST",
        ldap + "grouperLoaderLdapResultsTransformationClass", "some.Clazz", ldap + "grouperLoaderLdapGroupsLike", "%"), target).size());
    assertNull(GrouperLoaderTargetAuthorization.recordedEditorMemberId(target));
    assertEquals(0, importAs(SubjectTestHelper.SUBJ0, importJson("JEXL_SCRIPT"), target).size());
  }

}
