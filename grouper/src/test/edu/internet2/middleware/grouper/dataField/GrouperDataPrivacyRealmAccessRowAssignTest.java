package edu.internet2.middleware.grouper.dataField;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Advisory H5: UiV2Subject.viewDataFieldAssignments showed every data row assignment of a subject
 * without checking privacy realms.  The fix filters the assignments with
 * GrouperDataPrivacyRealmAccess.filterReadableRowAssignments, which needs "read" or "update" on the
 * row's realm AND on the field's realm, and fails closed on a missing row, field or realm config.
 * The UI method itself is not unit tested here; this tests the filter it calls.
 *
 * Before the fix this does not compile (GrouperDataPrivacyRealmAccess does not exist).
 */
public class GrouperDataPrivacyRealmAccessRowAssignTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDataPrivacyRealmAccessRowAssignTest("testFilterReadableRowAssignments"));
  }

  /**
   * @param name
   */
  public GrouperDataPrivacyRealmAccessRowAssignTest(String name) {
    super(name);
  }

  /** a row assignment, only the two ids the filter looks at (GrouperDataRowAssignView has more) */
  private static class TestAssign {
    /** row config id */
    private final String rowConfigId;
    /** field config id */
    private final String fieldConfigId;
    /**
     * @param rowConfigId
     * @param fieldConfigId
     */
    TestAssign(String rowConfigId, String fieldConfigId) {
      this.rowConfigId = rowConfigId;
      this.fieldConfigId = fieldConfigId;
    }
    @Override
    public String toString() {
      return this.rowConfigId + "/" + this.fieldConfigId;
    }
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
   * a non public, non authenticated realm with the given groups
   * @param realmConfigId
   * @param viewersGroup
   * @param readersGroup
   * @param updatersGroup
   */
  private static void privateRealm(String realmConfigId, String viewersGroup, String readersGroup, String updatersGroup) {
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmName", realmConfigId);
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmPublic", "false");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmAuthenticated", "false");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmSysadminsCanView", "false");
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmViewersGroupName", viewersGroup);
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmReadersGroupName", readersGroup);
    config("grouperPrivacyRealm." + realmConfigId + ".privacyRealmUpdatersGroupName", updatersGroup);
  }

  /**
   * a row column field
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
   * clear the engine's cached access per realm and subject.  clearHighestLevelCache() exists from
   * 5.19.0; 5.17 and 5.18 keep the cache in a static map with no clear method, so clear that map
   */
  private static void clearEngineCache() {
    try {
      Method method = GrouperDataEngine.class.getMethod("clearHighestLevelCache");
      method.invoke(null);
      return;
    } catch (NoSuchMethodException nsme) {
      // older release, clear the static map below
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    try {
      Field field = GrouperDataEngine.class.getDeclaredField("highestLevelAccessForPrivacyRealmSubject");
      field.setAccessible(true);
      ((Map<?, ?>)field.get(null)).clear();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * which of the assignments the subject can read, as "row/field" strings
   * @param grouperDataEngine
   * @param assigns
   * @param subject
   * @return the readable ones
   */
  private static List<String> readable(GrouperDataEngine grouperDataEngine, List<TestAssign> assigns, Subject subject) {
    List<String> result = new ArrayList<String>();
    for (TestAssign testAssign : GrouperDataPrivacyRealmAccess.filterReadableRowAssignments(grouperDataEngine, assigns,
        a -> a.rowConfigId, a -> a.fieldConfigId, subject)) {
      result.add(testAssign.toString());
    }
    return result;
  }

  /**
   * row realm plus field realm, read or update for values, view is not enough, fail closed on
   * missing configs, sysadmins follow privacyRealmSysadminsCanView.  Only group based realms are
   * used: public realms are not honored by the engine before 6.0.0 and authenticated realms not
   * before 7.3.0, and the fix leaves that realm logic to the engine of each release
   */
  public void testFilterReadableRowAssignments() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    // realm groups.  everyone: SUBJ0 to SUBJ3.  SUBJ0: nothing else.  SUBJ1: row realm viewer.  SUBJ2: row realm reader, not a
    // reader of the private field realm.  SUBJ3: row realm updater and private field realm reader.
    Group rowViewers = new GroupSave(grouperSession).assignName("test:rowViewers").assignCreateParentStemsIfNotExist(true).save();
    Group rowReaders = new GroupSave(grouperSession).assignName("test:rowReaders").assignCreateParentStemsIfNotExist(true).save();
    Group rowUpdaters = new GroupSave(grouperSession).assignName("test:rowUpdaters").assignCreateParentStemsIfNotExist(true).save();
    Group fieldReaders = new GroupSave(grouperSession).assignName("test:fieldReaders").assignCreateParentStemsIfNotExist(true).save();
    Group everyone = new GroupSave(grouperSession).assignName("test:everyone").assignCreateParentStemsIfNotExist(true).save();

    privateRealm("rowRealm", "test:rowViewers", "test:rowReaders", "test:rowUpdaters");
    privateRealm("privFieldRealm", "", "test:fieldReaders", "");

    // every test subject can read, and sysadmins can view (stands in for an open realm)
    privateRealm("everyoneRealm", "", "test:everyone", "");
    config("grouperPrivacyRealm.everyoneRealm.privacyRealmSysadminsCanView", "true");

    rowColumnField("openField", "everyoneRealm");
    rowColumnField("privField", "privFieldRealm");
    rowColumnField("pubField", "everyoneRealm");
    rowColumnField("lostRealmField", "noSuchRealm");

    // privRow: private row realm, one open field and one private field
    row("privRow", "rowRealm", "openField", "privField");
    // pubRow: open row realm, open field
    row("pubRow", "everyoneRealm", "pubField");
    // a row whose realm config does not exist
    row("lostRealmRow", "noSuchRealm", "pubField");
    // an open row with a field whose realm config does not exist
    row("lostFieldRealmRow", "everyoneRealm", "lostRealmField");

    rowViewers.addMember(SubjectTestHelper.SUBJ1);
    rowReaders.addMember(SubjectTestHelper.SUBJ2);
    rowUpdaters.addMember(SubjectTestHelper.SUBJ3);
    fieldReaders.addMember(SubjectTestHelper.SUBJ3);
    everyone.addMember(SubjectTestHelper.SUBJ0);
    everyone.addMember(SubjectTestHelper.SUBJ1);
    everyone.addMember(SubjectTestHelper.SUBJ2);
    everyone.addMember(SubjectTestHelper.SUBJ3);

    // the realm access calculation reads memberships through the sql cache
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    clearEngineCache();

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    List<TestAssign> assigns = new ArrayList<TestAssign>();
    assigns.add(new TestAssign("privRow", "openField"));
    assigns.add(new TestAssign("privRow", "privField"));
    assigns.add(new TestAssign("pubRow", "pubField"));
    assigns.add(new TestAssign("lostRealmRow", "pubField"));
    assigns.add(new TestAssign("lostFieldRealmRow", "lostRealmField"));
    assigns.add(new TestAssign("noSuchRow", "pubField"));
    assigns.add(new TestAssign("pubRow", "noSuchField"));
    assigns.add(new TestAssign(null, "pubField"));

    // no role on the row realm: only the open row
    assertEquals("[pubRow/pubField]", readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJ0).toString());

    // viewer of the row realm: metadata only, no values
    assertEquals("[pubRow/pubField]", readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJ1).toString());

    // reader of the row realm: the open field, but not the field in a realm they cannot read
    assertEquals("[privRow/openField, pubRow/pubField]", readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJ2).toString());

    // updater of the row realm and reader of the field realm: both fields
    assertEquals("[privRow/openField, privRow/privField, pubRow/pubField]",
        readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJ3).toString());

    // GrouperSystem: the private realms say sysadmins cannot view, so no private row; a missing
    // realm config gives sysadmins access (same as the engine), so the lost realm rows show
    assertEquals("[pubRow/pubField, lostRealmRow/pubField, lostFieldRealmRow/lostRealmField]",
        readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJR).toString());

    // sysadmins can view: GrouperSystem now sees the private row too
    config("grouperPrivacyRealm.rowRealm.privacyRealmSysadminsCanView", "true");
    config("grouperPrivacyRealm.privFieldRealm.privacyRealmSysadminsCanView", "true");
    clearEngineCache();
    grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
    assertEquals("[privRow/openField, privRow/privField, pubRow/pubField, lostRealmRow/pubField, lostFieldRealmRow/lostRealmField]",
        readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJR).toString());

    // and a normal user is unchanged by the sysadmin setting
    assertEquals("[pubRow/pubField]", readable(grouperDataEngine, assigns, SubjectTestHelper.SUBJ0).toString());

    // null and empty lists
    assertEquals(0, GrouperDataPrivacyRealmAccess.filterReadableRowAssignments(grouperDataEngine, (List<TestAssign>)null,
        a -> a.rowConfigId, a -> a.fieldConfigId, SubjectTestHelper.SUBJ3).size());
    assertEquals(0, readable(grouperDataEngine, new ArrayList<TestAssign>(), SubjectTestHelper.SUBJ3).size());

    // the access levels themselves
    assertEquals("", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "privRow", SubjectTestHelper.SUBJ0));
    assertEquals("view", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "privRow", SubjectTestHelper.SUBJ1));
    assertEquals("read", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "privRow", SubjectTestHelper.SUBJ2));
    assertEquals("update", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "privRow", SubjectTestHelper.SUBJ3));
    assertEquals("", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "noSuchRow", SubjectTestHelper.SUBJ3));
    assertEquals("", GrouperDataPrivacyRealmAccess.rowAccessByConfigId(grouperDataEngine, "lostRealmRow", SubjectTestHelper.SUBJ3));
    assertEquals("read", GrouperDataPrivacyRealmAccess.fieldAccessByConfigId(grouperDataEngine, "openField", SubjectTestHelper.SUBJ0));
    assertEquals("", GrouperDataPrivacyRealmAccess.fieldAccessByConfigId(grouperDataEngine, "privField", SubjectTestHelper.SUBJ2));
    assertEquals("read", GrouperDataPrivacyRealmAccess.fieldAccessByConfigId(grouperDataEngine, "privField", SubjectTestHelper.SUBJ3));
    assertFalse(GrouperDataPrivacyRealmAccess.canReadValues("view"));
    assertFalse(GrouperDataPrivacyRealmAccess.canReadValues(""));
    assertFalse(GrouperDataPrivacyRealmAccess.canReadValues(null));
    assertTrue(GrouperDataPrivacyRealmAccess.canReadValues("read"));
    assertTrue(GrouperDataPrivacyRealmAccess.canReadValues("update"));
  }

}
