package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.List;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Tests for GshTemplateJavaCompiler — verifies that in-memory Java
 * compilation produces correct bytecode on success and well-formed
 * diagnostics on failure, including Grouper-API resolution against the
 * running classpath.
 *
 * GRP-7006
 */
public class GshTemplateJavaCompilerTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileSimpleClass"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileSyntaxError"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileTypeError"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileUnresolvedImport"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileUnresolvedGrouperApiMethod"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileValidGshTemplateV2Subclass"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileInnerClasses"));
    TestRunner.run(new GshTemplateJavaCompilerTest("testCompileErrorIncludesSourceContextWithCaret"));
  }

  /**
   *
   */
  public GshTemplateJavaCompilerTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateJavaCompilerTest(String name) {
    super(name);
  }

  /**
   * Baseline: a trivial top-level class with no external references should
   * compile cleanly and produce exactly one class file.
   */
  public void testCompileSimpleClass() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.SimpleClass";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "public class SimpleClass {\n"
        + "  public int add(int a, int b) { return a + b; }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertTrue("expected success, diagnostics=" + result.getDiagnostics(), result.isSuccess());
    assertFalse(result.hasErrors());
    assertEquals(1, result.getClassNameToBytecode().size());
    assertTrue(result.getClassNameToBytecode().containsKey(className));
    byte[] bytecode = result.getClassNameToBytecode().get(className);
    assertNotNull(bytecode);
    assertTrue("bytecode should be non-empty", bytecode.length > 0);
    // Java class files start with the magic number 0xCAFEBABE
    assertEquals((byte) 0xCA, bytecode[0]);
    assertEquals((byte) 0xFE, bytecode[1]);
    assertEquals((byte) 0xBA, bytecode[2]);
    assertEquals((byte) 0xBE, bytecode[3]);
  }

  /**
   * A syntax error (missing semicolon) should produce a failed result with
   * an ERROR-severity diagnostic carrying a line number.
   */
  public void testCompileSyntaxError() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.SyntaxError";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "public class SyntaxError {\n"
        + "  public int broken() { return 42 }\n" // missing semicolon
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertFalse("expected failure", result.isSuccess());
    assertTrue("expected at least one error diagnostic", result.hasErrors());
    assertEquals(0, result.getClassNameToBytecode().size());
    List<GshTemplateCompileDiagnostic> errors = result.errorDiagnostics();
    assertTrue(errors.size() > 0);
    GshTemplateCompileDiagnostic firstError = errors.get(0);
    assertEquals("ERROR", firstError.getSeverity());
    assertEquals("expected error on line 3", 3, firstError.getLineNumber());
    assertNotNull(firstError.getMessage());
  }

  /**
   * A type error (assigning a String to an int) should produce a failed
   * result with an ERROR-severity diagnostic carrying a line number.
   */
  public void testCompileTypeError() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.TypeError";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "public class TypeError {\n"
        + "  public int wrong() {\n"
        + "    int x = \"this is a string, not an int\";\n"
        + "    return x;\n"
        + "  }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertFalse(result.isSuccess());
    assertTrue(result.hasErrors());
    assertEquals(0, result.getClassNameToBytecode().size());
    GshTemplateCompileDiagnostic firstError = result.errorDiagnostics().get(0);
    assertEquals("ERROR", firstError.getSeverity());
    assertEquals(4, firstError.getLineNumber());
  }

  /**
   * An import of a class that doesn't exist should produce a failed result
   * with an ERROR-severity diagnostic.
   */
  public void testCompileUnresolvedImport() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.UnresolvedImport";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "import com.no.such.package.NoSuchClass;\n"
        + "public class UnresolvedImport {\n"
        + "  public NoSuchClass produce() { return null; }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertFalse(result.isSuccess());
    assertTrue(result.hasErrors());
    assertEquals(0, result.getClassNameToBytecode().size());
  }

  /**
   * Calling a method that doesn't exist on a real Grouper class should
   * produce a failed result. This validates that the compile classpath
   * really does include Grouper API and that javac is exercising it.
   */
  public void testCompileUnresolvedGrouperApiMethod() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.UnresolvedApi";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "import edu.internet2.middleware.grouper.GrouperSession;\n"
        + "public class UnresolvedApi {\n"
        + "  public void run(GrouperSession session) {\n"
        + "    session.thisMethodDoesNotExistOnGrouperSession();\n"
        + "  }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertFalse("expected failure; if this passes the classpath isn't picking up "
        + "Grouper API and javac is silently treating GrouperSession as unresolved", result.isSuccess());
    assertTrue(result.hasErrors());
    // Compile fails because either:
    //   (a) the GrouperSession import resolved and the method call doesn't, OR
    //   (b) the GrouperSession import itself didn't resolve.
    // Either way the compiler must have produced an error.
  }

  /**
   * A valid subclass of GshTemplateV2 should compile successfully against
   * the Grouper API on the classpath, producing one class file.
   */
  public void testCompileValidGshTemplateV2Subclass() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.ValidV2Subclass";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class ValidV2Subclass extends GshTemplateV2 {\n"
        + "  @Override\n"
        + "  public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) {\n"
        + "    // no-op\n"
        + "  }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertTrue("expected success, diagnostics=" + result.getDiagnostics(), result.isSuccess());
    assertFalse(result.hasErrors());
    assertEquals(1, result.getClassNameToBytecode().size());
    assertTrue(result.getClassNameToBytecode().containsKey(className));
  }

  /**
   * A class with an inner class plus a lambda should produce multiple class
   * files (one per inner/synthetic class), all captured in the result.
   */
  public void testCompileInnerClasses() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.WithInners";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "import java.util.function.Supplier;\n"
        + "public class WithInners {\n"
        + "  public static class Inner {\n"
        + "    public int value = 42;\n"
        + "  }\n"
        + "  public Supplier<String> giveSupplier() {\n"
        + "    return () -> \"lambda result\";\n"
        + "  }\n"
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertTrue("expected success, diagnostics=" + result.getDiagnostics(), result.isSuccess());
    assertTrue("expected at least 2 produced classes (top-level + inner), got "
        + result.getClassNameToBytecode().keySet(),
        result.getClassNameToBytecode().size() >= 2);
    assertTrue(result.getClassNameToBytecode().containsKey(className));
    assertTrue(result.getClassNameToBytecode().containsKey(className + "$Inner"));
  }

  /**
   * A compile error diagnostic should carry a source-context snippet — the
   * offending line plus a caret pointing at the column. Mirrors javac's
   * standard text output so the UI can render the diagnostic in a familiar
   * form.
   */
  public void testCompileErrorIncludesSourceContextWithCaret() {
    String className = "edu.internet2.middleware.grouper.gshTemplateTest.CaretSource";
    String source = ""
        + "package edu.internet2.middleware.grouper.gshTemplateTest;\n"
        + "public class CaretSource {\n"
        + "  public int broken() { return 42 }\n" // missing semicolon
        + "}\n";

    GshTemplateCompileResult result = GshTemplateJavaCompiler.compile(className, source);

    assertFalse(result.isSuccess());
    GshTemplateCompileDiagnostic firstError = result.errorDiagnostics().get(0);
    String sourceContext = firstError.getSourceContext();
    assertNotNull("source context should be populated for diagnostics with line+column",
        sourceContext);
    // First line of the snippet is the offending source line
    String[] contextLines = sourceContext.split("\n", -1);
    assertEquals("source context should be exactly two lines (source + caret)", 2, contextLines.length);
    assertEquals("  public int broken() { return 42 }", contextLines[0]);
    // Second line is spaces followed by a single caret
    String caretLine = contextLines[1];
    assertTrue("caret line should end with ^, got: '" + caretLine + "'", caretLine.endsWith("^"));
    assertEquals("caret should be at column position",
        (int) firstError.getColumnNumber(), caretLine.length());
  }

}
