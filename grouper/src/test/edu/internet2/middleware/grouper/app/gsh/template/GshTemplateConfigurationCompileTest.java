package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
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
    seedGshTemplateInventory();
    System.exit(0);
  }

  /**
   * Seed 40 GSH template configs for eyeballing the inventory list / filters.
   * Persists to DB-backed config, so run against the DB the UI reads, then open
   * the GSH templates screen. Cleanup: delete the rows from the screen, or
   * delete config keys matching grouperGshTemplate.invTemplate%.
   */
  public static void seedGshTemplateInventory() {

    GrouperSession.startRootSession();

    String[] types = new String[] {
        "gsh", "abac", "provisioner", "daemon", "daemonChangeLog",
        "report", "customUi", "hook", "library" };

    for (int i = 1; i <= 40; i++) {

      String num = i < 10 ? "0" + i : "" + i;
      String configId = "invTemplate" + num;
      String type = types[i % types.length];
      boolean compiled = (i % 2 == 0);

      storeGshTemplateConfig(configId, "templateType", type);
      storeGshTemplateConfig(configId, "templateMode", compiled ? "compiled" : "interpreted");
      storeGshTemplateConfig(configId, "enabled", "true");

      if (!compiled) {
        // interpreted -> em-dash compile status
        storeGshTemplateConfig(configId, "gshTemplate", "println 'hello from " + configId + "';");
      } else if (i % 10 == 0) {
        // file source with a missing file -> "Source file missing"
        storeGshTemplateConfig(configId, "gshTemplateSourceType", "file");
        storeGshTemplateConfig(configId, "gshTemplateFileName", "/tmp/missing-" + configId + ".java");
      } else if (i % 6 == 0) {
        // broken Java -> "Compile failed"
        storeGshTemplateConfig(configId, "gshTemplate", brokenJavaSource(num));
      } else {
        // valid Java -> "Compiled OK"
        storeGshTemplateConfig(configId, "gshTemplate", validJavaSource(num));
      }
    }
  }

  private static void storeGshTemplateConfig(String configId, String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperGshTemplate." + configId + "." + suffix).value(value).store();
  }

  private static String validJavaSource(String num) {
    return "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class InvTemplate" + num + " {\n"
        + "  public String hi() { return \"hi from " + num + "\"; }\n"
        + "}\n";
  }

  private static String brokenJavaSource(String num) {
    return "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class InvTemplate" + num + " {\n"
        + "  public void broken() { int x = ; }\n"
        + "}\n";
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
