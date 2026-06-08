package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the shared compiled-Java dispatch helper — the resolve &rarr; cast
 * &rarr; instantiate sequence and its error reporting that every compiled
 * template dispatcher funnels through.
 *
 * GRP-7026
 */
public class GshTemplateCompiledDispatchTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateCompiledDispatchTest("testInstantiateValidGshTemplateV2Subclass"));
    TestRunner.run(new GshTemplateCompiledDispatchTest("testInstantiateWrongBaseThrowsClearError"));
    TestRunner.run(new GshTemplateCompiledDispatchTest("testInstantiateCompileErrorThrows"));
    TestRunner.run(new GshTemplateCompiledDispatchTest("testInstantiateParseErrorThrows"));
    TestRunner.run(new GshTemplateCompiledDispatchTest("testInstantiateHotReloadNewClassOnSourceChange"));
  }

  /**
   *
   */
  public GshTemplateCompiledDispatchTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateCompiledDispatchTest(String name) {
    super(name);
  }

  /**
   * Clear registry state between tests so cached entries from one test don't
   * leak into another.
   */
  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
  }

  /**
   * A config whose readSource() returns a fixed string, so the helper can be
   * exercised without a populated database config.
   * @param source the source the config should hand back
   * @return a GshTemplateConfig returning that source
   */
  private static GshTemplateConfig configReturningSource(final String source) {
    return new GshTemplateConfig("ignoredConfigId") {
      @Override
      public String readSource() {
        return source;
      }
    };
  }

  /**
   * Happy path: a public class that extends GshTemplateV2 resolves, casts, and
   * instantiates.
   */
  public void testInstantiateValidGshTemplateV2Subclass() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class ValidDispatchTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    out.getGsh_builtin_gshTemplateOutput().addOutputLine(\"ran\");\n"
        + "  }\n"
        + "}\n";

    GshTemplateV2 instance = GshTemplateCompiledDispatch.instantiate(
        "validDispatchConfig", configReturningSource(source), GshTemplateV2.class);

    assertNotNull(instance);
    assertEquals("edu.internet2.middleware.grouper.gshTest.ValidDispatchTemplate",
        instance.getClass().getName());
  }

  /**
   * A class that does not extend the requested base surfaces a clear error
   * (wrapped ClassCastException) rather than a confusing failure later.
   */
  public void testInstantiateWrongBaseThrowsClearError() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class WrongBaseTemplate {\n"
        + "}\n";

    try {
      GshTemplateCompiledDispatch.instantiate(
          "wrongBaseConfig", configReturningSource(source), GshTemplateV2.class);
      fail("should have thrown — class does not extend GshTemplateV2");
    } catch (RuntimeException re) {
      assertTrue("message should explain the base mismatch: " + re.getMessage(),
          re.getMessage().contains("must extend/implement"));
      assertTrue("message should name the required base: " + re.getMessage(),
          re.getMessage().contains("GshTemplateV2"));
    }
  }

  /**
   * A source with a Java compile error surfaces a "did not compile" failure.
   */
  public void testInstantiateCompileErrorThrows() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class BadCompileTemplate {\n"
        + "  public void foo() { int x = ; }\n"
        + "}\n";

    try {
      GshTemplateCompiledDispatch.instantiate(
          "badCompileConfig", configReturningSource(source), GshTemplateV2.class);
      fail("should have thrown — source does not compile");
    } catch (RuntimeException re) {
      assertTrue("message should report a compile failure: " + re.getMessage(),
          re.getMessage().contains("did not compile"));
    }
  }

  /**
   * A source the parser rejects (no public class) surfaces a "did not compile"
   * failure with the parse error.
   */
  public void testInstantiateParseErrorThrows() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "class NotPublicTemplate {\n"
        + "}\n";

    try {
      GshTemplateCompiledDispatch.instantiate(
          "parseErrorConfig", configReturningSource(source), GshTemplateV2.class);
      fail("should have thrown — no public class to load");
    } catch (RuntimeException re) {
      assertTrue("message should report a compile/parse failure: " + re.getMessage(),
          re.getMessage().contains("did not compile"));
    }
  }

  /**
   * Re-resolving the same config id with changed source produces a new class
   * (hot-reload), not the cached one.
   */
  public void testInstantiateHotReloadNewClassOnSourceChange() {
    String sourceA = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class HotReloadTemplate extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    out.getGsh_builtin_gshTemplateOutput().addOutputLine(\"A\");\n"
        + "  }\n"
        + "}\n";

    String sourceB = sourceA.replace("addOutputLine(\"A\")", "addOutputLine(\"B\")");

    GshTemplateV2 instanceA = GshTemplateCompiledDispatch.instantiate(
        "hotReloadConfig", configReturningSource(sourceA), GshTemplateV2.class);
    Class<?> classA = instanceA.getClass();

    GshTemplateV2 instanceB = GshTemplateCompiledDispatch.instantiate(
        "hotReloadConfig", configReturningSource(sourceB), GshTemplateV2.class);
    Class<?> classB = instanceB.getClass();

    assertNotSame("source change should produce a fresh class", classA, classB);
  }

}
