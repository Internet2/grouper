package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the library template type — compiled-Java classes with no required
 * base class and no dispatcher of their own, invoked by other templates via
 * GshTemplateCompiledDispatch.instanceForTemplate(). Covers the two call
 * patterns (interface cast and reflection), the compiled-mode requirement, and
 * static-state hot-reload semantics.
 *
 * GRP-7027
 */
public class GshTemplateLibraryTest extends GrouperTest {

  /**
   * config id reused across tests; cleared/reset per test
   */
  private static final String CONFIG_ID = "testGshTemplateConfig";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateLibraryTest("testInstanceForTemplateReflection"));
    TestRunner.run(new GshTemplateLibraryTest("testInstanceForTemplateInterfaceCast"));
    TestRunner.run(new GshTemplateLibraryTest("testInstanceForTemplateRequiresCompiledMode"));
    TestRunner.run(new GshTemplateLibraryTest("testInstanceForTemplateHotReloadResetsStatics"));
  }

  /**
   *
   */
  public GshTemplateLibraryTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateLibraryTest(String name) {
    super(name);
  }

  /**
   * Clear registry state between tests so cached classes don't leak.
   */
  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
  }

  /**
   * Set up CONFIG_ID as a compiled library template with the given Java source,
   * reusing the sample config scaffolding for the required common fields.
   * @param javaSource the library template's Java body
   */
  private void configureLibrary(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".templateType", "library");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".gshTemplate", javaSource);
  }

  /**
   * Option C — call a library method by name through GrouperUtil.callMethod,
   * with no parent-jar interface.
   */
  public void testInstanceForTemplateReflection() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class GshLibraryGreeter {\n"
        + "  public String greet(String who) {\n"
        + "    return \"hello \" + who;\n"
        + "  }\n"
        + "}\n";

    configureLibrary(source);

    Object library = GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
    assertNotNull(library);

    String result = (String) GrouperUtil.callMethod(library.getClass(), library, "greet",
        String.class, "world");

    assertEquals("hello world", result);
  }

  /**
   * Option B — cast the library instance to a parent-loader interface (here the
   * JDK's Callable) and call it directly. Proves an interface defined on the
   * parent classloader is the same Class on both sides of the per-template
   * ByteArrayClassLoader boundary.
   */
  public void testInstanceForTemplateInterfaceCast() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import java.util.concurrent.Callable;\n"
        + "public class GshLibraryCallable implements Callable<String> {\n"
        + "  public String call() {\n"
        + "    return \"called\";\n"
        + "  }\n"
        + "}\n";

    configureLibrary(source);

    Object library = GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
    assertTrue("library should implement the parent-loader interface", library instanceof Callable);

    @SuppressWarnings("unchecked")
    Callable<String> callable = (Callable<String>) library;
    try {
      assertEquals("called", callable.call());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * A library template with templateMode != compiled cannot be loaded and
   * raises a clear error.
   */
  public void testInstanceForTemplateRequiresCompiledMode() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class GshLibraryNotCompiled {\n"
        + "  public String greet() { return \"hi\"; }\n"
        + "}\n";

    configureLibrary(source);
    // override back to interpreted
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + ".templateMode", "interpreted");

    try {
      GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
      fail("should have thrown — library must be templateMode=compiled");
    } catch (RuntimeException re) {
      assertTrue("message should explain the compiled-mode requirement: " + re.getMessage(),
          re.getMessage().contains("must be templateMode=compiled"));
    }
  }

  /**
   * Statics on a library class persist across instances on the same JVM (same
   * cached Class) and reset when the source changes (new class, hot-reload).
   */
  public void testInstanceForTemplateHotReloadResetsStatics() {

    String sourceVersion1 = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "// version 1\n"
        + "public class GshLibraryCounter {\n"
        + "  private static int count = 0;\n"
        + "  public int increment() {\n"
        + "    count++;\n"
        + "    return count;\n"
        + "  }\n"
        + "}\n";

    configureLibrary(sourceVersion1);

    Object library1 = GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
    assertEquals(1, ((Integer) GrouperUtil.callMethod(library1.getClass(), library1, "increment")).intValue());

    // a fresh instance shares the cached class's static state
    Object library2 = GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
    assertEquals(2, ((Integer) GrouperUtil.callMethod(library2.getClass(), library2, "increment")).intValue());

    // change the source -> new class -> statics reset
    String sourceVersion2 = sourceVersion1.replace("// version 1", "// version 2");
    configureLibrary(sourceVersion2);

    Object library3 = GshTemplateCompiledDispatch.instanceForTemplate(CONFIG_ID);
    assertEquals(1, ((Integer) GrouperUtil.callMethod(library3.getClass(), library3, "increment")).intValue());
  }

}
