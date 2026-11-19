/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.abac;

import java.lang.reflect.Method;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security patch 120 (GRP-7542): a scripted group's script could use a group the user cannot
 * READ (one not in the sql cache yet, named by an old name, or made later), and the analysis
 * told the user whether a group they cannot see exists.  Adapted from GrouperAbacScriptedGroupTest
 * in commit 79de898a461f so the same file compiles and runs on every patched release (5.13.x, whose
 * set also backports GRP-6031's five argument analysis, and 5.17.1 and later):
 * the save checks are GrouperAbacScriptedGroup.checkScriptForSave where that class exists
 * (GROUPER_7_BRANCH, called by reflection), else the save-mode analysis the release's UI refuses
 * a save on.  Goes in grouper/src/test/edu/internet2/middleware/grouper/abac/
 */
public class GrouperJexlScriptHiddenGroupTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperJexlScriptHiddenGroupTest.class);
  }

  /**
   * @param name
   */
  public GrouperJexlScriptHiddenGroupTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /** the scripted group the scripts are checked for, which SUBJ0 is an admin of */
  private Group target;

  /**
   * test:target, which SUBJ0 is an admin of; test:readable with SUBJ1, which SUBJ0 can read;
   * test:viewOnly with SUBJ5, which SUBJ0 can see but not read; and secret:hidden with SUBJ4 and
   * SUBJ1, which SUBJ0 cannot see.  the sql cache is synced, since that is what a script reads
   */
  private void createGroups() {

    GrouperSession.startRootSession();

    this.target = new GroupSave().assignName("test:target").assignCreateParentStemsIfNotExist(true).save();
    this.target.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    Group readable = new GroupSave().assignName("test:readable").assignCreateParentStemsIfNotExist(true).save();
    readable.addMember(SubjectTestHelper.SUBJ1, false);
    readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    Group viewOnly = new GroupSave().assignName("test:viewOnly").assignCreateParentStemsIfNotExist(true).save();
    viewOnly.addMember(SubjectTestHelper.SUBJ5, false);
    viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    // SUBJ1 is in this one too, so a count which used its real members would differ from one
    // which treats it as empty, e.g. readable && !hidden would be 0 and not 1
    Group hidden = new GroupSave().assignName("secret:hidden").assignCreateParentStemsIfNotExist(true).save();
    hidden.addMember(SubjectTestHelper.SUBJ4, false);
    hidden.addMember(SubjectTestHelper.SUBJ1, false);

    syncSqlCache();
  }

  /**
   * bring the sql cache up to date, as the daemons would
   */
  private static void syncSqlCache() {
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheFullSync");
  }

  /**
   * @return a loaded data engine
   */
  private static GrouperDataEngine dataEngine() {
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    return grouperDataEngine;
  }

  /**
   * the checks a save of this script on test:target by SUBJ0 makes
   * @param script a script
   * @return why the save is refused, or null if it is allowed
   */
  private String saveRefusalAsSubj0(String script) {
    GrouperDataEngine grouperDataEngine = dataEngine();
    Class<?> scriptedGroupClass = null;
    try {
      scriptedGroupClass = Class.forName("edu.internet2.middleware.grouper.abac.GrouperAbacScriptedGroup");
    } catch (ClassNotFoundException cnfe) {
      // a release: the UI saves only if the save-mode analysis has no error and no warning
      GrouperJexlScriptAnalysis analysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
          grouperDataEngine, script, null, SubjectTestHelper.SUBJ0, false);
      if (analysis.getErrorMessage() != null && analysis.getErrorMessage().length() > 0) {
        return analysis.getErrorMessage();
      }
      if (analysis.getWarningMessage() != null && analysis.getWarningMessage().length() > 0) {
        return analysis.getWarningMessage();
      }
      return null;
    }
    // GROUPER_7_BRANCH (GRP-7541 and later): the save checks in core
    try {
      Method checkScriptForSave = scriptedGroupClass.getMethod("checkScriptForSave", GrouperDataEngine.class,
          Group.class, String.class, Set.class, Subject.class);
      Object saveCheck = checkScriptForSave.invoke(null, grouperDataEngine, this.target, script, null,
          SubjectTestHelper.SUBJ0);
      Method isAllowed = saveCheck.getClass().getMethod("isAllowed");
      isAllowed.setAccessible(true);
      Method getErrorMessage = saveCheck.getClass().getMethod("getErrorMessage");
      getErrorMessage.setAccessible(true);
      if ((Boolean)isAllowed.invoke(saveCheck)) {
        return null;
      }
      String errorMessage = (String)getErrorMessage.invoke(saveCheck);
      return errorMessage == null ? "refused" : errorMessage;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param script a script
   * @return the read only analysis of it, as SUBJ0, the way the Analyze button runs it
   */
  private static GrouperJexlScriptAnalysis analyzeAsSubj0(String script) {
    return GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(dataEngine(), script, null,
        SubjectTestHelper.SUBJ0, true);
  }

  /**
   * recentMemberOf came in 5.15.4.  it keeps its group names on the data engine (5.15.4 to 5.17.1),
   * in a thread local of GrouperLoaderJexlScriptFullSync (5.17.2), or on the analysis (5.18.0 on)
   * @return true if this release's scripts can use recentMemberOf
   */
  private static boolean releaseHasRecentMemberOf() {
    try {
      GrouperJexlScriptAnalysis.class.getMethod("getRecentMemberOfGroupNames");
      return true;
    } catch (NoSuchMethodException nsme) {
      // not this way
    }
    try {
      GrouperDataEngine.class.getMethod("getRecentMemberOfGroupNames");
      return true;
    } catch (NoSuchMethodException nsme) {
      // not this way
    }
    try {
      GrouperLoaderJexlScriptFullSync.class.getDeclaredField("recentMemberOfGroupNames");
      return true;
    } catch (NoSuchFieldException nsfe) {
      return false;
    }
  }

  /**
   * a group SUBJ0 can read can be used
   */
  public void testReadableGroup() {
    createGroups();

    String refusal = saveRefusalAsSubj0("entity.memberOf('test:readable')");
    assertNull(refusal, refusal);
    assertEquals(1, analyzeAsSubj0("entity.memberOf('test:readable')").getGrouperJexlScriptParts().get(0).getPopulationCount());
  }

  /**
   * a group which does not exist yet is refused, since one made later with that name, in a folder
   * the user cannot see, would put its members in the scripted group
   */
  public void testGroupNotFound() {
    createGroups();

    String refusal = saveRefusalAsSubj0("entity.memberOf('secret:notYet')");
    assertNotNull(refusal);
    assertTrue(refusal, refusal.contains("secret:notYet"));

    // in a bigger script too
    assertNotNull(saveRefusalAsSubj0("entity.memberOf('test:readable') || entity.memberOf('secret:notYet')"));
  }

  /**
   * a group named by a name it used to have is refused: the cache only knows its current name, so
   * the script would treat it as empty until a group was made with the old name
   */
  public void testOldName() {
    createGroups();

    Group renamed = new GroupSave().assignName("test:oldName").assignCreateParentStemsIfNotExist(true).save();
    renamed.addMember(SubjectTestHelper.SUBJ2, false);
    renamed.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    new GroupSave().assignGroupNameToEdit("test:oldName").assignName("test:newName")
        .assignSetAlternateNameIfRename(true).assignReplaceAllSettings(false)
        .assignSaveMode(SaveMode.UPDATE).save();
    syncSqlCache();

    String refusal = saveRefusalAsSubj0("entity.memberOf('test:oldName')");
    assertNotNull(refusal);
    assertTrue(refusal, refusal.contains("test:oldName"));

    refusal = saveRefusalAsSubj0("entity.memberOf('test:newName')");
    assertNull(refusal, refusal);
  }

  /**
   * a group the sql cache does not have yet, because it was just made, still needs READ
   */
  public void testNotCachedYet() {
    createGroups();

    // no sync after these, so the cache does not have them
    Group fresh = new GroupSave().assignName("secret:fresh").assignCreateParentStemsIfNotExist(true).save();
    fresh.addMember(SubjectTestHelper.SUBJ6, false);
    Group freshReadable = new GroupSave().assignName("test:freshReadable").assignCreateParentStemsIfNotExist(true).save();
    freshReadable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    // or this would test nothing
    assertEquals(0, GrouperUtil.length(SqlCacheGroupDao.retrieveByGroupNamesFieldNames(GrouperUtil.toList(
        new MultiKey("secret:fresh", "members"), new MultiKey("test:freshReadable", "members")))));

    String refusal = saveRefusalAsSubj0("entity.memberOf('secret:fresh')");
    assertNotNull(refusal);
    assertTrue(refusal, refusal.contains("secret:fresh"));

    refusal = saveRefusalAsSubj0("entity.memberOf('test:freshReadable')");
    assertNull(refusal, refusal);
  }

  /**
   * a group SUBJ0 cannot see looks exactly like one which does not exist, in the analysis and on
   * save, so neither tells them whether it is there.  one they can see but not read is refused as
   * not readable, since they know it is there
   */
  public void testHiddenGroupLooksMissing() {
    createGroups();

    GrouperJexlScriptAnalysis hidden = analyzeAsSubj0("entity.memberOf('secret:hidden')");
    GrouperJexlScriptAnalysis missing = analyzeAsSubj0("entity.memberOf('secret:doesNotExist')");
    assertNull(hidden.getErrorMessage(), hidden.getErrorMessage());
    assertNull(missing.getErrorMessage(), missing.getErrorMessage());
    assertEquals(0, hidden.getGrouperJexlScriptParts().get(0).getPopulationCount());
    assertEquals(missing.getGrouperJexlScriptParts().get(0).getDisplayDescription().toString()
        .replace("secret:doesNotExist", "x"),
        hidden.getGrouperJexlScriptParts().get(0).getDisplayDescription().toString()
        .replace("secret:hidden", "x"));

    String hiddenMessage = saveRefusalAsSubj0("entity.memberOf('secret:hidden')");
    String missingMessage = saveRefusalAsSubj0("entity.memberOf('secret:doesNotExist')");
    assertNotNull(hiddenMessage);
    assertNotNull(missingMessage);
    assertEquals(missingMessage.replace("secret:doesNotExist", "x"), hiddenMessage.replace("secret:hidden", "x"));

    // not a count of a group SUBJ0 cannot see, whatever else is in the script.  SUBJ1 is in both,
    // so this would be 0 if the hidden group's real members were used
    GrouperJexlScriptAnalysis negated = analyzeAsSubj0("entity.memberOf('test:readable') && !entity.memberOf('secret:hidden')");
    assertNull(negated.getErrorMessage(), negated.getErrorMessage());
    assertEquals(1, negated.getGrouperJexlScriptParts().get(0).getPopulationCount());

    // the same through recentMemberOf, where the release has it (5.15.4 and later)
    if (releaseHasRecentMemberOf()) {
      GrouperJexlScriptAnalysis recent = analyzeAsSubj0("entity.recentMemberOf('secret:hidden', '2 days')");
      assertNull(recent.getErrorMessage(), recent.getErrorMessage());
      assertEquals(0, recent.getGrouperJexlScriptParts().get(0).getPopulationCount());
      assertNotNull(saveRefusalAsSubj0("entity.recentMemberOf('secret:hidden', '2 days')"));
    } else {
      System.out.println("skip testHiddenGroupLooksMissing recentMemberOf: this release has no recentMemberOf");
    }

    GrouperJexlScriptAnalysis viewOnly = analyzeAsSubj0("entity.memberOf('test:viewOnly')");
    assertNotNull(viewOnly.getErrorMessage());
    assertTrue(viewOnly.getErrorMessage(), viewOnly.getErrorMessage().contains("test:viewOnly"));
    assertNotNull(saveRefusalAsSubj0("entity.memberOf('test:viewOnly')"));
  }

  /**
   * the unresolvable or deleted entities warning does not list members of a group SUBJ0 cannot
   * see.  releases before 6.2.0 / 7.1.0 have no such warning: there only the first half applies
   */
  public void testUnresolvableMembersOfHiddenGroup() {
    createGroups();

    new GcDbAccess().sql("update grouper_members set subject_resolution_resolvable = 'F' "
        + "where subject_id in (?, ?) and subject_source = 'jdbc'")
        .addBindVar(SubjectTestHelper.SUBJ4.getId()).addBindVar(SubjectTestHelper.SUBJ1.getId()).executeSql();

    // with no error, the analysis went on to the unresolvable check, which before treated the
    // group as having its real members
    GrouperJexlScriptAnalysis hidden = analyzeAsSubj0("entity.memberOf('secret:hidden')");
    assertNull(hidden.getErrorMessage(), hidden.getErrorMessage());
    assertNull(hidden.getWarningMessage(), hidden.getWarningMessage());

    // where the release has the warning, it still lists them for a group SUBJ0 can read
    GrouperJexlScriptAnalysis readable = analyzeAsSubj0("entity.memberOf('test:readable')");
    if (readable.getWarningMessage() != null) {
      assertTrue(readable.getWarningMessage(), readable.getWarningMessage().contains(SubjectTestHelper.SUBJ1.getId()));
    }
  }

}
