package edu.internet2.middleware.grouper.userLifecycle;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Unit tests for UserLifecycleEngine.evaluateLifecycleJexl - the single substitution
 * code path shared by UserLifecycleFullDaemon (all five trigger branches) and by the
 * UserLifecycleEventConfiguration pre-save validator. Each trigger's variable set is
 * covered here so the helper signature cannot drift out of sync with the daemon.
 */
public class UserLifecycleEngineTest extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new UserLifecycleEngineTest("testGroupUserRemoveFromFolderVariables"));
  }

  public UserLifecycleEngineTest() {
    super();
  }

  public UserLifecycleEngineTest(String name) {
    super(name);
  }

  /** Plain text with no ${...} blocks must pass through verbatim (no JEXL evaluation). */
  public void testPlainTextPassesThrough() {
    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Job loss", null, null, null, null, null, true);
    assertEquals("Job loss", result);
  }

  /** Empty / null templates should not blow up. */
  public void testEmptyTemplate() {
    assertEquals("", UserLifecycleEngine.evaluateLifecycleJexl(
        "", null, null, null, null, null, true));
  }

  /** groupUserAdd / groupUserRemove expose the five group string variables. */
  public void testGroupUserAddRemoveVariables() {
    Group group = new Group();
    group.setNameDb("test:dept:accounting");
    group.setDisplayNameDb("Test:Departments:Accounting");
    group.setExtensionDb("accounting");
    group.setDisplayExtensionDb("Accounting");
    group.setDescriptionDb("Accounting department");

    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Job loss from ${groupDisplayExtension} (${groupName})",
        group, null, null, null, null, true);
    assertEquals("Job loss from Accounting (test:dept:accounting)", result);

    // verify all five group variables are bound
    result = UserLifecycleEngine.evaluateLifecycleJexl(
        "${groupName}|${groupDisplayName}|${groupExtension}|${groupDisplayExtension}|${groupDescription}",
        group, null, null, null, null, true);
    assertEquals("test:dept:accounting|Test:Departments:Accounting|accounting|Accounting|Accounting department", result);
  }

  /** groupUserRemoveFromFolder exposes both group* and stem* variables. */
  public void testGroupUserRemoveFromFolderVariables() {
    Group group = new Group();
    group.setNameDb("test:dept:accounting");
    group.setDisplayNameDb("Test:Departments:Accounting");
    group.setExtensionDb("accounting");
    group.setDisplayExtensionDb("Accounting");
    group.setDescriptionDb("Accounting department");

    Stem stem = new Stem();
    stem.setNameDb("test:dept");
    stem.setDisplayNameDb("Test:Departments");
    stem.setExtensionDb("dept");
    stem.setDisplayExtensionDb("Departments");
    stem.setDescriptionDb("Departments folder");

    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Removed from ${groupDisplayExtension} in ${stemDisplayExtension}",
        group, stem, null, null, null, true);
    assertEquals("Removed from Accounting in Departments", result);

    result = UserLifecycleEngine.evaluateLifecycleJexl(
        "${stemName}|${stemDisplayName}|${stemExtension}|${stemDisplayExtension}|${stemDescription}",
        group, stem, null, null, null, true);
    assertEquals("test:dept|Test:Departments|dept|Departments|Departments folder", result);
  }

  /** dataFieldRemove exposes configId and value. */
  public void testDataFieldRemoveVariables() {
    GrouperDataField dataField = new GrouperDataField();
    dataField.setConfigId("position");

    // string value
    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Lost ${configId} access (value: ${value})",
        null, null, dataField, "manager", null, true);
    assertEquals("Lost position access (value: manager)", result);

    // integer value
    result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Lost ${configId} (value: ${value})",
        null, null, dataField, Integer.valueOf(42), null, true);
    assertEquals("Lost position (value: 42)", result);
  }

  /** dataRowRemove exposes configId only. */
  public void testDataRowRemoveVariables() {
    GrouperDataRow dataRow = new GrouperDataRow();
    dataRow.setConfigId("pursual");

    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Lost ${configId} row",
        null, null, null, null, dataRow, true);
    assertEquals("Lost pursual row", result);
  }

  /** grouperUtil is always available even without any objects bound. */
  public void testGrouperUtilAvailable() {
    Group group = new Group();
    group.setNameDb("test:group");
    group.setDisplayNameDb("Test Group");
    group.setExtensionDb("group");
    group.setDisplayExtensionDb("Group <script>alert(1)</script>");
    group.setDescriptionDb("desc");

    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Job loss from ${grouperUtil.escapeHtml(groupDisplayExtension, true)}",
        group, null, null, null, null, true);
    assertEquals("Job loss from Group &lt;script&gt;alert(1)&lt;/script&gt;", result);
  }

  /** Lenient mode (daemon): undefined variables render as empty, don't crash production. */
  public void testLenientModeSwallowsUndefinedVariable() {
    String result = UserLifecycleEngine.evaluateLifecycleJexl(
        "Job loss from ${doesNotExist}",
        null, null, null, null, null, true);
    // doesn't throw; renders the undefined variable as empty
    assertNotNull(result);
    assertTrue("expected literal prefix to survive, got: " + result, result.startsWith("Job loss from "));
  }

  /** Strict mode (validator): undefined variables throw, so typos surface at save time. */
  public void testStrictModeThrowsOnUndefinedVariable() {
    try {
      UserLifecycleEngine.evaluateLifecycleJexl(
          "Job loss from ${doesNotExist}",
          null, null, null, null, null, false);
      fail("Expected exception for undefined variable in strict mode");
    } catch (Exception expected) {
      // good - validator surfaces this as a field-level error
    }
  }

  /** buildJexlVariables with all-null inputs returns an empty map (no variables bound). */
  public void testBuildJexlVariablesAllNull() {
    assertTrue(UserLifecycleEngine.buildJexlVariables(null, null, null, null, null).isEmpty());
  }
}
