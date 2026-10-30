/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.abac;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

/**
 * the checks made before a scripted group's script is saved, and what the analysis shows about
 * groups the user cannot see
 */
public class GrouperAbacScriptedGroupTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperAbacScriptedGroupTest("testGroupNotFound"));
  }

  /**
   * @param name
   */
  public GrouperAbacScriptedGroupTest(String name) {
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
   * test:viewOnly with SUBJ5, which SUBJ0 can see but not read; and secret:hidden with SUBJ4,
   * which SUBJ0 cannot see.  the sql cache is synced, since that is what a script reads
   */
  private void createGroups() {

    GrouperAbacTestHelper.setupDataFields();
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
   * @param script a script
   * @return the save checks for it on test:target, as SUBJ0
   */
  private GrouperAbacScriptedGroup.SaveCheck checkAsSubj0(String script) {
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    return GrouperAbacScriptedGroup.checkScriptForSave(grouperDataEngine, this.target, script,
        GrouperAbac.globalDefaultSubjectSourceIds(), SubjectTestHelper.SUBJ0);
  }

  /**
   * @param script a script
   * @return the read only analysis of it, as SUBJ0, the way the Analyze button runs it
   */
  private static GrouperJexlScriptAnalysis analyzeAsSubj0(String script) {
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    return GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(grouperDataEngine, script, null,
        SubjectTestHelper.SUBJ0, true, null, false);
  }

  /**
   * a group SUBJ0 can read can be used
   */
  public void testReadableGroup() {
    createGroups();

    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf('test:readable')");
    assertTrue(saveCheck.getErrorMessage(), saveCheck.isAllowed());
    assertEquals(1, saveCheck.getPopulationCount());
  }

  /**
   * a group which does not exist yet is refused, since one made later with that name, in a folder
   * the user cannot see, would put its members in the scripted group
   */
  public void testGroupNotFound() {
    createGroups();

    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf('secret:notYet')");
    assertFalse(saveCheck.isAllowed());
    assertTrue(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().contains("secret:notYet"));

    // in a bigger script too
    saveCheck = checkAsSubj0("entity.memberOf('test:readable') || entity.memberOf('secret:notYet')");
    assertFalse(saveCheck.isAllowed());
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

    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf('test:oldName')");
    assertFalse(saveCheck.isAllowed());
    assertTrue(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().contains("test:oldName"));

    assertTrue(checkAsSubj0("entity.memberOf('test:newName')").isAllowed());
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

    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf('secret:fresh')");
    assertFalse(saveCheck.isAllowed());
    assertTrue(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().contains("secret:fresh"));

    assertTrue(checkAsSubj0("entity.memberOf('test:freshReadable')").isAllowed());
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
    assertNull(hidden.getErrorMessage());
    assertNull(missing.getErrorMessage());
    assertEquals(0, hidden.getGrouperJexlScriptParts().get(0).getPopulationCount());
    assertEquals(missing.getGrouperJexlScriptParts().get(0).getDisplayDescription().toString()
        .replace("secret:doesNotExist", "x"),
        hidden.getGrouperJexlScriptParts().get(0).getDisplayDescription().toString()
        .replace("secret:hidden", "x"));

    String hiddenMessage = checkAsSubj0("entity.memberOf('secret:hidden')").getErrorMessage();
    String missingMessage = checkAsSubj0("entity.memberOf('secret:doesNotExist')").getErrorMessage();
    assertEquals(missingMessage.replace("secret:doesNotExist", "x"), hiddenMessage.replace("secret:hidden", "x"));

    // not a count of a group SUBJ0 cannot see, whatever else is in the script.  SUBJ1 is in both,
    // so this would be 0 if the hidden group's real members were used
    GrouperJexlScriptAnalysis negated = analyzeAsSubj0("entity.memberOf('test:readable') && !entity.memberOf('secret:hidden')");
    assertEquals(1, negated.getGrouperJexlScriptParts().get(0).getPopulationCount());

    // the same through memberOfAny and recentMemberOf
    GrouperJexlScriptAnalysis any = analyzeAsSubj0("entity.memberOfAny(['test:readable', 'secret:hidden'])");
    assertNull(any.getErrorMessage());
    assertEquals(1, any.getGrouperJexlScriptParts().get(0).getPopulationCount());
    assertFalse(checkAsSubj0("entity.memberOfAny(['test:readable', 'secret:hidden'])").isAllowed());

    GrouperJexlScriptAnalysis recent = analyzeAsSubj0("entity.recentMemberOf('secret:hidden', '2 days')");
    assertNull(recent.getErrorMessage());
    assertEquals(0, recent.getGrouperJexlScriptParts().get(0).getPopulationCount());
    assertFalse(checkAsSubj0("entity.recentMemberOf('secret:hidden', '2 days')").isAllowed());

    GrouperJexlScriptAnalysis viewOnly = analyzeAsSubj0("entity.memberOf('test:viewOnly')");
    assertNotNull(viewOnly.getErrorMessage());
    assertTrue(viewOnly.getErrorMessage(), viewOnly.getErrorMessage().contains("test:viewOnly"));
    assertFalse(checkAsSubj0("entity.memberOf('test:viewOnly')").isAllowed());
  }

  /**
   * a script which uses the group it is for is refused: on its own it does nothing, with or it only
   * ever grows, and with a not its members flip on every sync
   */
  public void testRefersToItself() {
    createGroups();

    String refersToItselfMessage = text("grouperLoaderEditJexlScriptRefersToItself");
    for (String script : new String[] {"entity.memberOf('test:target')",
        "entity.memberOf('test:readable') || entity.memberOf('test:target')",
        "!entity.memberOf('test:target')",
        "entity.memberOfAny(['test:readable', 'test:target'])",
        "entity.recentMemberOf('test:target', '2 days')",
        "'test:target'",
        "${entity.memberOf('test:target')}"}) {
      GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0(script);
      assertFalse(script, saveCheck.isAllowed());
      assertEquals(script, refersToItselfMessage, saveCheck.getErrorMessage());
    }
  }

  /**
   * @param key a text key the checks use
   * @return its text, which has to be there, or a comparison with a missing message would pass
   */
  private static String text(String key) {
    String text = GrouperTextContainer.textOrNull(key);
    assertNotNull(key, text);
    return text;
  }

  /**
   * a script longer than the column it is kept in is refused before anything else is done with it
   */
  public void testTooLong() {
    createGroups();

    String script = "entity.memberOf('test:readable')";
    String tooLongMessage = text("grouperLoaderEditJexlScriptTooLong");

    // exactly the limit fits
    String fits = script + StringUtils.repeat(" ", GrouperAbacScriptedGroup.MAX_SCRIPT_BYTES - script.length());
    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0(fits);
    assertTrue(saveCheck.getErrorMessage(), saveCheck.isAllowed());

    saveCheck = checkAsSubj0(fits + " ");
    assertFalse(saveCheck.isAllowed());
    assertEquals(tooLongMessage, saveCheck.getErrorMessage());

    // counted in bytes, not characters: about 2060 characters, but over 4000 bytes
    String multiByte = script + " || entity.memberOf('" + StringUtils.repeat("\u00e9", 2000) + "')";
    assertTrue(multiByte.length() < GrouperAbacScriptedGroup.MAX_SCRIPT_BYTES);
    saveCheck = checkAsSubj0(multiByte);
    assertFalse(saveCheck.isAllowed());
    assertEquals(tooLongMessage, saveCheck.getErrorMessage());
  }

  /**
   * a script which cannot be parsed gets the invalid message with the parser's error, rather than
   * an exception
   */
  public void testCannotBeParsed() {
    createGroups();

    String invalidMessage = text("grouperLoaderEditJexlScriptInvalid");
    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf(");
    assertFalse(saveCheck.isAllowed());
    assertTrue(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().startsWith(invalidMessage + "\n"));
    assertTrue(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().length() > invalidMessage.length() + 1);
    // not where in Grouper the parser was called from
    assertFalse(saveCheck.getErrorMessage(), saveCheck.getErrorMessage().contains("GrouperLoaderJexlScriptFullSync"));
  }

  /**
   * a script which parses but cannot be built into parts, e.g. one using a data field which does
   * not exist, still fails the way it always has, with an exception for the log.  its message says
   * whether a data field exists, before the privacy realm checks, so it is not shown to the user
   */
  public void testOtherFailureNotShown() {
    createGroups();

    try {
      GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.hasAttribute('noSuchDataField')");
      fail("expected an exception, got: " + saveCheck.getErrorMessage());
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("noSuchDataField"));
    }
  }

  /**
   * a composite cannot be a scripted group: its members come from its factors, and the sync adds
   * and removes members directly, which fails on a composite
   */
  public void testComposite() {
    createGroups();

    this.target.addCompositeMember(CompositeType.COMPLEMENT,
        GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:readable", true),
        GroupFinder.findByName(GrouperSession.staticGrouperSession(), "test:viewOnly", true));

    GrouperAbacScriptedGroup.SaveCheck saveCheck = checkAsSubj0("entity.memberOf('test:readable')");
    assertFalse(saveCheck.isAllowed());
    assertEquals(text("grouperLoaderEditJexlScriptComposite"), saveCheck.getErrorMessage());
  }

  /**
   * the unresolvable or deleted entities warning does not list members of a group SUBJ0 cannot
   * see, but still lists them for a group they can read
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

    GrouperJexlScriptAnalysis readable = analyzeAsSubj0("entity.memberOf('test:readable')");
    assertNotNull(readable.getWarningMessage());
    assertTrue(readable.getWarningMessage(), readable.getWarningMessage().contains(SubjectTestHelper.SUBJ1.getId()));
  }

}
