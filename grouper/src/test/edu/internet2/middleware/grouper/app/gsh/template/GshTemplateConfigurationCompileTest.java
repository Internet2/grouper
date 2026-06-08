package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the compile-on-save check used by GshTemplateConfiguration when a
 * template is saved in templateMode=compiled.
 *
 * GRP-7033
 */
public class GshTemplateConfigurationCompileTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateConfigurationCompileTest("testValidSourceCompilesClean"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testSyntaxErrorReturnsDiagnostics"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testParseErrorReturnsMessage"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testBlankSourceIsNotAnError"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testBaseClassCorrectForType"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testBaseClassWrongForType"));
    TestRunner.run(new GshTemplateConfigurationCompileTest("testBaseClassLibraryHasNoRequirement"));
  }

  /**
   * @param name
   */
  public GshTemplateConfigurationCompileTest(String name) {
    super(name);
  }

  /**
   * A valid compiled template body compiles clean — no diagnostics.
   */
  public void testValidSourceCompilesClean() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class SaveValidTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    out.getGsh_builtin_gshTemplateOutput().addOutputLine(\"ok\");\n"
        + "  }\n"
        + "}\n";

    assertNull(GshTemplateConfiguration.compileDiagnosticsOrNull(source));
  }

  /**
   * A Java compile error returns diagnostics that block save.
   */
  public void testSyntaxErrorReturnsDiagnostics() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class SaveBadTemplate {\n"
        + "  public void foo() { int x = ; }\n"
        + "}\n";

    String diagnostics = GshTemplateConfiguration.compileDiagnosticsOrNull(source);
    assertNotNull("a syntax error should produce diagnostics", diagnostics);
  }

  /**
   * A source the parser rejects (no public class) returns the parse error.
   */
  public void testParseErrorReturnsMessage() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "class SaveNotPublicTemplate {\n"
        + "}\n";

    String diagnostics = GshTemplateConfiguration.compileDiagnosticsOrNull(source);
    assertNotNull("a parse failure should produce a message", diagnostics);
  }

  /**
   * Blank source is left to required-field validation, not the compile check.
   */
  public void testBlankSourceIsNotAnError() {
    assertNull(GshTemplateConfiguration.compileDiagnosticsOrNull(null));
    assertNull(GshTemplateConfiguration.compileDiagnosticsOrNull("   "));
  }

  /**
   * A class extending the right base for its type passes base-class validation.
   */
  public void testBaseClassCorrectForType() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemon;\n"
        + "import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput;\n"
        + "public class BaseValidDaemon extends GrouperTemplateDaemon {\n"
        + "  public void runDaemon(OtherJobTemplateInput otherJobTemplateInput) { }\n"
        + "}\n";

    assertNull(GshTemplateConfiguration.baseClassErrorOrNull(source, GshTemplateType.daemon));
  }

  /**
   * A class extending the wrong base for its type is rejected (here a
   * GshTemplateV2 body declared as a daemon).
   */
  public void testBaseClassWrongForType() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class BaseWrongDaemon extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "}\n";

    String error = GshTemplateConfiguration.baseClassErrorOrNull(source, GshTemplateType.daemon);
    assertNotNull("a GshTemplateV2 body should not validate as a daemon", error);
    assertTrue("error should name the required base: " + error,
        error.contains("GrouperTemplateDaemon"));
  }

  /**
   * Library templates have no required base class.
   */
  public void testBaseClassLibraryHasNoRequirement() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class BaseLibraryAnything {\n"
        + "  public String hi() { return \"hi\"; }\n"
        + "}\n";

    assertNull(GshTemplateConfiguration.baseClassErrorOrNull(source, GshTemplateType.library));
  }

}
