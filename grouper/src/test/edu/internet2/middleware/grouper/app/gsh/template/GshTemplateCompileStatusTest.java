package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompileStatus.GshTemplateCompileStatusResult;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the per-JVM compile-status cache used by the GSH template inventory.
 *
 * GRP-7034
 */
public class GshTemplateCompileStatusTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateCompileStatusTest("testValidSourceStatusSuccess"));
    TestRunner.run(new GshTemplateCompileStatusTest("testBadSourceStatusFailed"));
    TestRunner.run(new GshTemplateCompileStatusTest("testCacheHitReturnsSameResult"));
    TestRunner.run(new GshTemplateCompileStatusTest("testSourceChangeRecomputes"));
  }

  /**
   * @param name
   */
  public GshTemplateCompileStatusTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateCompileStatus.clearCache();
  }

  private static final String VALID_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
      + "public class StatusValidTemplate extends GshTemplateV2 {\n"
      + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
      + "}\n";

  private static final String BAD_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "public class StatusBadTemplate {\n"
      + "  public void foo() { int x = ; }\n"
      + "}\n";

  /**
   * Valid source compiles clean — success, no diagnostics, timestamp set.
   */
  public void testValidSourceStatusSuccess() {
    GshTemplateCompileStatusResult result = GshTemplateCompileStatus.statusForSource("statusValid", VALID_SOURCE);
    assertTrue(result.isSuccess());
    assertNull(result.getDiagnostics());
    assertTrue(result.getLastCompiledMillis() > 0);
  }

  /**
   * Bad source fails with diagnostics.
   */
  public void testBadSourceStatusFailed() {
    GshTemplateCompileStatusResult result = GshTemplateCompileStatus.statusForSource("statusBad", BAD_SOURCE);
    assertFalse(result.isSuccess());
    assertNotNull(result.getDiagnostics());
  }

  /**
   * Same config id + same source returns the cached result (no recompile).
   */
  public void testCacheHitReturnsSameResult() {
    GshTemplateCompileStatusResult first = GshTemplateCompileStatus.statusForSource("statusCache", VALID_SOURCE);
    GshTemplateCompileStatusResult second = GshTemplateCompileStatus.statusForSource("statusCache", VALID_SOURCE);
    assertSame("same source should return the cached result", first, second);
  }

  /**
   * Changing the source recomputes (new result).
   */
  public void testSourceChangeRecomputes() {
    GshTemplateCompileStatusResult good = GshTemplateCompileStatus.statusForSource("statusChange", VALID_SOURCE);
    assertTrue(good.isSuccess());
    GshTemplateCompileStatusResult bad = GshTemplateCompileStatus.statusForSource("statusChange", BAD_SOURCE);
    assertNotSame(good, bad);
    assertFalse(bad.isSuccess());
  }

}
