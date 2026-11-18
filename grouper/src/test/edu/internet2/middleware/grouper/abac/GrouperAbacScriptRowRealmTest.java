package edu.internet2.middleware.grouper.abac;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowDao;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Advisory H2: the scripted group (ABAC) analysis, used by the UI save and the Analyze screen,
 * checked the privacy realm of fields named in a script but not of the row named in
 * entity.hasRow('row', ...), and not of the fields inside attributeCompare(...).  The fix checks
 * both with GrouperDataPrivacyRealmAccess, with the same rules as a field: no access or "view" is an
 * error, "read" is a warning when only analyzing and an error when saving, "update" is needed to save.
 *
 * Calls GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml directly (readOnly false is the save
 * check, readOnly true the Analyze screen).  Before the fix the hasRow and attributeCompare cases
 * come back with no error.
 */
public class GrouperAbacScriptRowRealmTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperAbacScriptRowRealmTest("testHasRowRowRealm"));
  }

  /**
   * @param name
   */
  public GrouperAbacScriptRowRealmTest(String name) {
    super(name);
  }

  /**
   * store a grouper.properties db config
   * @param name
   * @param value
   */
  private static void config(String name, String value) {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(name).value(value).store();
  }

  /**
   * a non public, non authenticated realm with the given groups (same group for every role if one)
   * @param realmConfigId
   * @param readersGroup
   * @param updatersGroup
   */
  private static void privateRealm(String realmConfigId, String readersGroup, String updatersGroup) {
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmName", realmConfigId);
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmPublic", "false");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmAuthenticated", "false");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmViewersGroupName", "test:realmViewers");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmReadersGroupName", readersGroup);
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmUpdatersGroupName", updatersGroup);
  }

  /**
   * a row column string field
   * @param fieldConfigId
   * @param realmConfigId
   */
  private static void rowColumnField(String fieldConfigId, String realmConfigId) {
    config("grouperDataField." + fieldConfigId + ".fieldAliases", fieldConfigId);
    config("grouperDataField." + fieldConfigId + ".fieldPrivacyRealm", realmConfigId);
    config("grouperDataField." + fieldConfigId + ".descriptionHtml", fieldConfigId);
    config("grouperDataField." + fieldConfigId + ".fieldDataType", "string");
    config("grouperDataField." + fieldConfigId + ".fieldDataStructure", "rowColumn");
    config("grouperDataField." + fieldConfigId + ".fieldDataAssignableTo", "individuals");
  }

  /**
   * a data row with these fields
   * @param rowConfigId
   * @param realmConfigId
   * @param fieldConfigIds
   */
  private static void row(String rowConfigId, String realmConfigId, String... fieldConfigIds) {
    config("grouperDataRow." + rowConfigId + ".rowAliases", rowConfigId);
    config("grouperDataRow." + rowConfigId + ".rowPrivacyRealm", realmConfigId);
    config("grouperDataRow." + rowConfigId + ".descriptionHtml", rowConfigId);
    config("grouperDataRow." + rowConfigId + ".oneRowPerSubject", "true");
    config("grouperDataRow." + rowConfigId + ".rowNumberOfDataFields", "" + fieldConfigIds.length);
    for (int i = 0; i < fieldConfigIds.length; i++) {
      config("grouperDataRow." + rowConfigId + ".rowDataField." + i + ".colDataFieldConfigId", fieldConfigIds[i]);
    }
  }

  /**
   * analyze a script as a user
   * @param script
   * @param loggedInSubject
   * @param readOnly true for the Analyze screen, false for the save check
   * @return the analysis
   */
  private static GrouperJexlScriptAnalysis analyze(String script, Subject loggedInSubject, boolean readOnly) {
    // a fresh engine each time, like the UI; loadFieldsAndRows reads the configured fields and rows
    // from the data tables (it does not insert them, see testHasRowRowRealm)
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    return GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(grouperDataEngine, script, null, loggedInSubject, readOnly);
  }

  /**
   * assert an error was returned, and that it names the alias
   * @param analysis
   * @param alias
   */
  private static void assertError(GrouperJexlScriptAnalysis analysis, String alias) {
    assertNotNull("expected an error naming " + alias, analysis.getErrorMessage());
    assertTrue(analysis.getErrorMessage(), analysis.getErrorMessage().contains(alias));
  }

  /**
   * hasRow checks the row realm (save and analyze), attributeCompare checks both fields, a missing
   * row or a row with a missing realm config fails closed for users and not for sysadmins
   */
  public void testHasRowRowRealm() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // SUBJ0: updater of the field realm only.  SUBJ1: also reader of the private row realm.
    // SUBJ2: also updater of the private row realm.  SUBJ3: viewer of every private realm only.
    Group fieldUpdaters = new GroupSave(grouperSession).assignName("test:abacRealmUpdaters").assignCreateParentStemsIfNotExist(true).save();
    Group rowReaders = new GroupSave(grouperSession).assignName("test:privRowReaders").assignCreateParentStemsIfNotExist(true).save();
    Group rowUpdaters = new GroupSave(grouperSession).assignName("test:privRowUpdaters").assignCreateParentStemsIfNotExist(true).save();
    Group realmViewers = new GroupSave(grouperSession).assignName("test:realmViewers").assignCreateParentStemsIfNotExist(true).save();

    privateRealm("privRowTest", "test:privRowReaders", "test:privRowUpdaters");
    privateRealm("abacUpd", "test:abacRealmUpdaters", "test:abacRealmUpdaters");

    rowColumnField("pubField", "abacUpd");
    rowColumnField("privRowField", "privRowTest");

    // private row with an updatable field and a private field
    row("privRow", "privRowTest", "pubField", "privRowField");
    // row in the realm SUBJ0 can update, with both fields
    row("openRow", "abacUpd", "pubField", "privRowField");
    // row whose realm config does not exist
    row("lostRealmRow", "noSuchRealm", "pubField");

    // loadFieldsAndRows only indexes fields and rows that are in grouper_data_field / grouper_data_row
    // (normally a data provider sync inserts them), so insert them here or the analysis cannot find
    // the rows and refuses every one as "not allowed".  noSuchRow is deliberately not inserted
    // findOrAdd (not insertMissingConfigIds) so this compiles on every patched release (5.13 has only findOrAdd)
    for (String fieldConfigId : new String[] {"pubField", "privRowField"}) {
      GrouperDataFieldDao.findOrAdd(fieldConfigId);
    }
    for (String rowConfigId : new String[] {"privRow", "openRow", "lostRealmRow"}) {
      GrouperDataRowDao.findOrAdd(rowConfigId);
    }

    fieldUpdaters.addMember(SubjectTestHelper.SUBJ0);
    fieldUpdaters.addMember(SubjectTestHelper.SUBJ1);
    fieldUpdaters.addMember(SubjectTestHelper.SUBJ2);
    rowReaders.addMember(SubjectTestHelper.SUBJ1);
    rowUpdaters.addMember(SubjectTestHelper.SUBJ2);
    realmViewers.addMember(SubjectTestHelper.SUBJ3);

    // the realm access calculation reads memberships through the sql cache
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    clearHighestLevelCache();

    String privRowScript = "entity.hasRow('privRow', \"pubField == 'X'\")";

    // control: the field check already worked
    assertError(analyze("entity.hasRow('openRow', \"privRowField == 'SECRET'\")", SubjectTestHelper.SUBJ0, false), "privRowField");
    assertNull(analyze("entity.hasRow('openRow', \"pubField == 'X'\")", SubjectTestHelper.SUBJ0, false).getErrorMessage());

    // no access to the row realm: refused to save, and no counts on the Analyze screen
    assertError(analyze(privRowScript, SubjectTestHelper.SUBJ0, false), "privRow");
    assertError(analyze(privRowScript, SubjectTestHelper.SUBJ0, true), "privRow");

    // view only on the row realm (and no field access): refused in both modes
    assertNotNull(analyze(privRowScript, SubjectTestHelper.SUBJ3, false).getErrorMessage());
    assertNotNull(analyze(privRowScript, SubjectTestHelper.SUBJ3, true).getErrorMessage());

    // read on the row realm: analyze with a warning, refused to save
    GrouperJexlScriptAnalysis analysis = analyze(privRowScript, SubjectTestHelper.SUBJ1, true);
    assertNull(analysis.getErrorMessage(), analysis.getErrorMessage());
    assertNotNull(analysis.getWarningMessage());
    assertTrue(analysis.getWarningMessage(), analysis.getWarningMessage().contains("privRow"));
    assertError(analyze(privRowScript, SubjectTestHelper.SUBJ1, false), "privRow");

    // update on the row realm and the field realm: allowed to save
    assertNull(analyze(privRowScript, SubjectTestHelper.SUBJ2, false).getErrorMessage());

    // attributeCompare: a private field on either side is refused, even in a row the user can update
    assertError(analyze("entity.hasRow('openRow', 'attributeCompare(pubField == privRowField)')", SubjectTestHelper.SUBJ0, false), "privRowField");
    assertError(analyze("entity.hasRow('openRow', 'attributeCompare(privRowField == pubField)')", SubjectTestHelper.SUBJ0, false), "privRowField");
    assertError(analyze("entity.hasRow('openRow', 'attributeCompare(privRowField == pubField)')", SubjectTestHelper.SUBJ0, true), "privRowField");
    // and inside a private row the row check refuses it first
    assertError(analyze("entity.hasRow('privRow', 'attributeCompare(privRowField == privRowField)')", SubjectTestHelper.SUBJ0, false), "privRow");
    // SUBJ2 can update the row realm and both field realms
    assertNull(analyze("entity.hasRow('privRow', 'attributeCompare(pubField == privRowField)')", SubjectTestHelper.SUBJ2, false).getErrorMessage());

    // a row that does not exist: a clear error (no NullPointerException), same message as not allowed
    GrouperJexlScriptAnalysis noSuchRow = analyze("entity.hasRow('noSuchRow', \"pubField == 'X'\")", SubjectTestHelper.SUBJ0, false);
    assertError(noSuchRow, "noSuchRow");
    String notAllowedMessage = analyze(privRowScript, SubjectTestHelper.SUBJ0, true).getErrorMessage();
    assertEquals(notAllowedMessage.replace("privRow", "X"), noSuchRow.getErrorMessage().replace("noSuchRow", "X"));

    // a row whose realm config is missing: refused for a user, allowed for a sysadmin
    assertError(analyze("entity.hasRow('lostRealmRow', \"pubField == 'X'\")", SubjectTestHelper.SUBJ0, false), "lostRealmRow");
    assertNull(analyze("entity.hasRow('lostRealmRow', \"pubField == 'X'\")", SubjectTestHelper.SUBJR, false).getErrorMessage());
  }

  /**
   * GrouperDataEngine.clearHighestLevelCache() is protected in 5.x and public from 6.0.0; call it
   * by reflection so this test compiles on every patched release
   */
  private static void clearHighestLevelCache() {
    try {
      java.lang.reflect.Method method = GrouperDataEngine.class.getDeclaredMethod("clearHighestLevelCache");
      method.setAccessible(true);
      method.invoke(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
