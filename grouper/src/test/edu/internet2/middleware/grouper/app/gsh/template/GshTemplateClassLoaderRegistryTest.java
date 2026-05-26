package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry.GshTemplateCachedClass;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry.GshTemplateResolveResult;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateSourceParser.GshTemplateSourceParseResult;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for the Phase 4a per-template classloader registry, the source
 * parser, and the byte-array classloader. Verifies that compile + define +
 * cache + swap all hang together correctly.
 *
 * GRP-7010
 */
public class GshTemplateClassLoaderRegistryTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserWithPackage"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserNoPackage"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserLeadingComments"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserPublicFinalClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserPublicAbstractClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testSourceParserNoPublicClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testByteArrayClassLoaderDefinesClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testByteArrayClassLoaderUnknownClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryFirstCallCompiles"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryCacheHitReturnsSameClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistrySourceChangeCreatesNewClass"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryOldVersionStaysLoadableViaHeldReference"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryParseError"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryCompileError"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryReturnedClassInstantiates"));
    TestRunner.run(new GshTemplateClassLoaderRegistryTest("testRegistryInnerClassLoads"));
  }

  /**
   *
   */
  public GshTemplateClassLoaderRegistryTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateClassLoaderRegistryTest(String name) {
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

  // ---------- GshTemplateSourceParser ----------

  /**
   * Standard happy path: package + public class produces a clean FQN.
   */
  public void testSourceParserWithPackage() {
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class FooTemplate {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertTrue("parse should succeed, error=" + result.getErrorMessage(), result.isSuccess());
    assertEquals("edu.internet2.middleware.grouper.gshTest.FooTemplate",
        result.getFullyQualifiedClassName());
  }

  /**
   * Missing package declaration is allowed — default package, FQN is just
   * the class name. Not Java best practice, but legal and convenient for
   * short GSH templates.
   */
  public void testSourceParserNoPackage() {
    String source = "public class NoPackageTemplate {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertTrue("default-package source should parse successfully, error=" + result.getErrorMessage(),
        result.isSuccess());
    assertEquals("NoPackageTemplate", result.getFullyQualifiedClassName());
  }

  /**
   * Block + line comments before the package/class declarations must not
   * confuse the parser.
   */
  public void testSourceParserLeadingComments() {
    String source = ""
        + "/* license header\n"
        + " * with a // line-comment marker inside\n"
        + " * and the word package as filler\n"
        + " */\n"
        + "// another comment with public class inside it\n"
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "\n"
        + "// final comment\n"
        + "public class CommentedTemplate {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertTrue("parse should succeed, error=" + result.getErrorMessage(), result.isSuccess());
    assertEquals("edu.internet2.middleware.grouper.gshTest.CommentedTemplate",
        result.getFullyQualifiedClassName());
  }

  /**
   * `public final class` should be recognized.
   */
  public void testSourceParserPublicFinalClass() {
    String source = ""
        + "package edu.x;\n"
        + "public final class FinalTemplate {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertTrue(result.isSuccess());
    assertEquals("edu.x.FinalTemplate", result.getFullyQualifiedClassName());
  }

  /**
   * `public abstract class` should be recognized.
   */
  public void testSourceParserPublicAbstractClass() {
    String source = ""
        + "package edu.x;\n"
        + "public abstract class AbstractTemplate {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertTrue(result.isSuccess());
    assertEquals("edu.x.AbstractTemplate", result.getFullyQualifiedClassName());
  }

  /**
   * A package-private (non-public) class is not valid for a template.
   */
  public void testSourceParserNoPublicClass() {
    String source = ""
        + "package edu.x;\n"
        + "class NotPublic {}\n";

    GshTemplateSourceParseResult result = GshTemplateSourceParser.parse(source);

    assertFalse(result.isSuccess());
    assertTrue("error should mention public class, got: " + result.getErrorMessage(),
        result.getErrorMessage().toLowerCase().contains("public class"));
  }

  // ---------- ByteArrayClassLoader ----------

  /**
   * Define a class from bytecode produced by GshTemplateJavaCompiler and
   * confirm the loader returns a usable Class.
   */
  public void testByteArrayClassLoaderDefinesClass() throws Exception {
    String fqn = "edu.internet2.middleware.grouper.gshTest.LoadableClass";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class LoadableClass {\n"
        + "  public int answer() { return 42; }\n"
        + "}\n";

    GshTemplateCompileResult compileResult = GshTemplateJavaCompiler.compile(fqn, source);
    assertTrue("expected compile success, diagnostics=" + compileResult.getDiagnostics(),
        compileResult.isSuccess());

    ByteArrayClassLoader loader = new ByteArrayClassLoader(
        GshTemplateV2.class.getClassLoader(),
        compileResult.getClassNameToBytecode());

    Class<?> loaded = loader.loadClass(fqn);
    assertNotNull(loaded);
    assertEquals(fqn, loaded.getName());

    Object instance = loaded.getDeclaredConstructor().newInstance();
    Object result = loaded.getDeclaredMethod("answer").invoke(instance);
    assertEquals(Integer.valueOf(42), result);
  }

  /**
   * Asking the loader for a class that's not in its bytecode map AND not in
   * the parent must throw ClassNotFoundException.
   */
  public void testByteArrayClassLoaderUnknownClass() {
    Map<String, byte[]> empty = new LinkedHashMap<String, byte[]>();
    ByteArrayClassLoader loader = new ByteArrayClassLoader(
        GshTemplateV2.class.getClassLoader(), empty);

    try {
      loader.loadClass("edu.does.not.Exist");
      fail("expected ClassNotFoundException");
    } catch (ClassNotFoundException e) {
      // expected
    }
  }

  // ---------- GshTemplateClassLoaderRegistry ----------

  /**
   * First resolve of a template name compiles and caches.
   */
  public void testRegistryFirstCallCompiles() {
    String templateName = "testFirstCall";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class FirstCallTemplate extends GshTemplateV2 {\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "}\n";

    GshTemplateResolveResult result = GshTemplateClassLoaderRegistry.resolve(templateName, source);

    assertTrue("expected success, parseError=" + result.getParseError()
        + " compile=" + (result.getCompileResult() == null
            ? "null" : result.getCompileResult().getDiagnostics()),
        result.isSuccess());
    assertNotNull(result.getTemplateClass());
    assertEquals("edu.internet2.middleware.grouper.gshTest.FirstCallTemplate",
        result.getTemplateClass().getName());
    assertTrue(GshTemplateV2.class.isAssignableFrom(result.getTemplateClass()));
  }

  /**
   * Second resolve with the same source returns the SAME Class object —
   * not a recompile.
   */
  public void testRegistryCacheHitReturnsSameClass() {
    String templateName = "testCacheHit";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class CacheHitTemplate extends GshTemplateV2 {\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "}\n";

    GshTemplateResolveResult first = GshTemplateClassLoaderRegistry.resolve(templateName, source);
    GshTemplateResolveResult second = GshTemplateClassLoaderRegistry.resolve(templateName, source);

    assertTrue(first.isSuccess());
    assertTrue(second.isSuccess());
    assertSame("cache hit should return the identical Class object",
        first.getTemplateClass(), second.getTemplateClass());
  }

  /**
   * Resolving with different source for the same template name produces a
   * different Class object (different classloader, recompiled bytecode).
   */
  public void testRegistrySourceChangeCreatesNewClass() {
    String templateName = "testSourceChange";
    String sourceV1 = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class ChangingTemplate extends GshTemplateV2 {\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "  public int version() { return 1; }\n"
        + "}\n";
    String sourceV2 = sourceV1.replace("return 1;", "return 2;");

    GshTemplateResolveResult v1 = GshTemplateClassLoaderRegistry.resolve(templateName, sourceV1);
    GshTemplateResolveResult v2 = GshTemplateClassLoaderRegistry.resolve(templateName, sourceV2);

    assertTrue(v1.isSuccess());
    assertTrue(v2.isSuccess());
    assertNotSame("source change should produce a different Class object",
        v1.getTemplateClass(), v2.getTemplateClass());
    assertNotSame("source change should produce a different classloader",
        v1.getCachedClass().getByteArrayClassLoader(),
        v2.getCachedClass().getByteArrayClassLoader());
  }

  /**
   * After a source change, the OLD Class object is still usable as long as
   * something holds a reference to it (modeling an in-flight execution).
   * This is the property that lets in-flight runs against the old version
   * drain cleanly.
   */
  public void testRegistryOldVersionStaysLoadableViaHeldReference() throws Exception {
    String templateName = "testOldVersionDrains";
    String sourceV1 = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class DrainingTemplate extends GshTemplateV2 {\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "  public int version() { return 1; }\n"
        + "}\n";
    String sourceV2 = sourceV1.replace("return 1;", "return 2;");

    GshTemplateResolveResult v1Result = GshTemplateClassLoaderRegistry.resolve(templateName, sourceV1);
    Class<? extends GshTemplateV2> v1Class = v1Result.getTemplateClass();
    Object v1Instance = v1Class.getDeclaredConstructor().newInstance();

    // Swap the cache to v2
    GshTemplateResolveResult v2Result = GshTemplateClassLoaderRegistry.resolve(templateName, sourceV2);
    Class<? extends GshTemplateV2> v2Class = v2Result.getTemplateClass();

    // v1's class and instance still work
    Object v1Version = v1Class.getDeclaredMethod("version").invoke(v1Instance);
    assertEquals(Integer.valueOf(1), v1Version);

    // v2 reports the new version
    Object v2Instance = v2Class.getDeclaredConstructor().newInstance();
    Object v2Version = v2Class.getDeclaredMethod("version").invoke(v2Instance);
    assertEquals(Integer.valueOf(2), v2Version);

    // Registry's current cached entry is v2
    GshTemplateCachedClass peek = GshTemplateClassLoaderRegistry.peekForTesting(templateName);
    assertSame(v2Class, peek.getTemplateClass());
  }

  /**
   * Source with no public class declaration surfaces as a parse error, not
   * a compile error or a crash. (Missing package is fine — default package
   * is allowed — so we test the actual fatal case: no public class.)
   */
  public void testRegistryParseError() {
    String templateName = "testParseError";
    String source = ""
        + "package edu.x;\n"
        + "class NotPublicAndNoOtherClass {}\n";

    GshTemplateResolveResult result = GshTemplateClassLoaderRegistry.resolve(templateName, source);

    assertFalse(result.isSuccess());
    assertNotNull(result.getParseError());
    assertNull(result.getCompileResult());
    assertNull(result.getTemplateClass());
  }

  /**
   * Source with a Java syntax error surfaces as a compile error with
   * diagnostics, not a parse error or a crash.
   */
  public void testRegistryCompileError() {
    String templateName = "testCompileError";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class BrokenTemplate {\n"
        + "  public int broken() { return 42 }\n" // missing semicolon
        + "}\n";

    GshTemplateResolveResult result = GshTemplateClassLoaderRegistry.resolve(templateName, source);

    assertFalse(result.isSuccess());
    assertNull(result.getParseError());
    assertNotNull(result.getCompileResult());
    assertTrue(result.getCompileResult().hasErrors());
    assertNull(result.getTemplateClass());
  }

  /**
   * The returned Class must actually be instantiable as a GshTemplateV2 —
   * not just compile.
   */
  public void testRegistryReturnedClassInstantiates() throws Exception {
    String templateName = "testInstantiates";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class InstantiableTemplate extends GshTemplateV2 {\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "}\n";

    GshTemplateResolveResult result = GshTemplateClassLoaderRegistry.resolve(templateName, source);
    assertTrue(result.isSuccess());

    GshTemplateV2 instance =
        (GshTemplateV2) result.getTemplateClass().getDeclaredConstructor().newInstance();
    assertNotNull(instance);
  }

  /**
   * A template with an inner class produces multiple class files; the
   * loader must hold all of them so the outer class can reach the inner
   * via normal Java semantics.
   */
  public void testRegistryInnerClassLoads() throws Exception {
    String templateName = "testInnerClass";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class WithInnerTemplate extends GshTemplateV2 {\n"
        + "  public static class Inner {\n"
        + "    public int value() { return 99; }\n"
        + "  }\n"
        + "  @Override public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { }\n"
        + "  public Inner makeInner() { return new Inner(); }\n"
        + "}\n";

    GshTemplateResolveResult result = GshTemplateClassLoaderRegistry.resolve(templateName, source);
    assertTrue("expected success, parseError=" + result.getParseError()
        + " compile=" + (result.getCompileResult() == null
            ? "null" : result.getCompileResult().getDiagnostics()),
        result.isSuccess());

    GshTemplateV2 instance =
        (GshTemplateV2) result.getTemplateClass().getDeclaredConstructor().newInstance();
    Object inner = result.getTemplateClass().getDeclaredMethod("makeInner").invoke(instance);
    assertNotNull(inner);
    Object value = inner.getClass().getDeclaredMethod("value").invoke(inner);
    assertEquals(Integer.valueOf(99), value);
  }

}
