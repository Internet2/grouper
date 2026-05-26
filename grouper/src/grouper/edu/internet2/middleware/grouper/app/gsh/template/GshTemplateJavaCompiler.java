package edu.internet2.middleware.grouper.app.gsh.template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Compiles a Java source body in memory using the JDK's
 * javax.tools.JavaCompiler. No filesystem I/O — source comes in as a String,
 * bytecode goes out as a byte[]. Diagnostics (errors, warnings) are collected
 * with line and column numbers and returned alongside any produced bytecode.
 *
 * Intended consumers:
 *   - the GSH template save UI (validates a saved Java source body and either
 *     blocks save on errors or stores the produced bytecode), and
 *   - the GSH template runtime registry (compiles on first execute, or on
 *     source change, before defining the class in a per-template
 *     ByteArrayClassLoader).
 *
 * The classpath used for compilation is derived from the running JVM's
 * java.class.path plus the URLs of any URLClassLoader in the current thread's
 * context classloader chain, so references to Grouper API and to anything
 * else on the JVM's effective classpath resolve at compile time. This matches
 * what the eventual runtime classloader will see, so "compiles here" implies
 * "loadable there."
 *
 * Phase 2 of the GSH Java template compilation effort. This class is
 * deliberately standalone — it does no integration with the existing GSH
 * execution path, has no static state, and can be unit-tested in isolation.
 *
 * GRP-7006
 */
public class GshTemplateJavaCompiler {

  /**
   * Compile a single top-level Java class from source. The source must
   * declare a top-level class whose fully-qualified name matches
   * fullyQualifiedClassName (compiler enforces this).
   *
   * On success, the returned result's classNameToBytecode contains the
   * top-level class's bytecode plus the bytecode of any inner classes,
   * anonymous classes, or lambda-generated classes the compiler emitted.
   *
   * On failure, classNameToBytecode is empty and diagnostics() lists the
   * errors (and possibly warnings) with line and column numbers.
   *
   * @param fullyQualifiedClassName e.g.
   *   "edu.internet2.middleware.grouper.gsh.userTemplates.FooTemplate"
   * @param javaSource the full Java source — package declaration, imports,
   *   class definition. No prepending or wrapping is performed; the source is
   *   compiled exactly as given (unlike the legacy Groovy GSH path which
   *   prepends imports automatically).
   * @return compile result with success flag, bytecode (on success), and
   *   diagnostics
   */
  public static GshTemplateCompileResult compile(String fullyQualifiedClassName, String javaSource) {

    GshTemplateCompileResult gshTemplateCompileResult = new GshTemplateCompileResult();

    JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
    if (javaCompiler == null) {
      GshTemplateCompileDiagnostic gshTemplateCompileDiagnostic = new GshTemplateCompileDiagnostic();
      gshTemplateCompileDiagnostic.setSeverity("ERROR");
      gshTemplateCompileDiagnostic.setMessage(
          "No Java compiler available from ToolProvider.getSystemJavaCompiler(); "
              + "Grouper must run on a JDK, not a JRE, for GSH template compilation");
      gshTemplateCompileResult.getDiagnostics().add(gshTemplateCompileDiagnostic);
      return gshTemplateCompileResult;
    }

    DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
    StandardJavaFileManager standardJavaFileManager =
        javaCompiler.getStandardFileManager(diagnosticCollector, Locale.US, null);

    InMemoryJavaFileManager inMemoryJavaFileManager =
        new InMemoryJavaFileManager(standardJavaFileManager);

    InMemoryJavaSource inMemoryJavaSource =
        new InMemoryJavaSource(fullyQualifiedClassName, javaSource);

    List<String> compilerOptions = buildCompilerOptions();

    JavaCompiler.CompilationTask compilationTask = javaCompiler.getTask(
        null, // writer for compiler messages — null routes through diagnostics
        inMemoryJavaFileManager,
        diagnosticCollector,
        compilerOptions,
        null, // class names for annotation processing — none
        Arrays.asList(inMemoryJavaSource));

    boolean compileOk = false;
    try {
      compileOk = compilationTask.call();
    } catch (Throwable t) {
      GshTemplateCompileDiagnostic gshTemplateCompileDiagnostic = new GshTemplateCompileDiagnostic();
      gshTemplateCompileDiagnostic.setSeverity("ERROR");
      gshTemplateCompileDiagnostic.setMessage(
          "Java compiler threw " + t.getClass().getSimpleName()
              + (t.getMessage() == null ? "" : ": " + t.getMessage()));
      gshTemplateCompileResult.getDiagnostics().add(gshTemplateCompileDiagnostic);
    }

    for (Diagnostic<? extends JavaFileObject> rawDiagnostic : diagnosticCollector.getDiagnostics()) {
      GshTemplateCompileDiagnostic gshTemplateCompileDiagnostic = new GshTemplateCompileDiagnostic();
      gshTemplateCompileDiagnostic.setSeverity(rawDiagnostic.getKind().name());
      long line = rawDiagnostic.getLineNumber();
      long col = rawDiagnostic.getColumnNumber();
      if (line != Diagnostic.NOPOS) {
        gshTemplateCompileDiagnostic.setLineNumber(line);
      }
      if (col != Diagnostic.NOPOS) {
        gshTemplateCompileDiagnostic.setColumnNumber(col);
      }
      gshTemplateCompileDiagnostic.setMessage(rawDiagnostic.getMessage(Locale.US));
      if (rawDiagnostic.getSource() != null) {
        gshTemplateCompileDiagnostic.setSourceClassName(rawDiagnostic.getSource().getName());
      }
      if (line != Diagnostic.NOPOS && col != Diagnostic.NOPOS) {
        String sourceContext = buildSourceContext(javaSource, (int) line, (int) col);
        if (sourceContext != null) {
          gshTemplateCompileDiagnostic.setSourceContext(sourceContext);
        }
      }
      gshTemplateCompileResult.getDiagnostics().add(gshTemplateCompileDiagnostic);
    }

    try {
      inMemoryJavaFileManager.close();
    } catch (IOException ignore) {
      // best effort
    }

    Map<String, byte[]> bytecode = inMemoryJavaFileManager.getBytecode();
    gshTemplateCompileResult.getClassNameToBytecode().putAll(bytecode);
    gshTemplateCompileResult.setSuccess(compileOk && !bytecode.isEmpty());

    return gshTemplateCompileResult;
  }

  /**
   * Build the -classpath option for javac from the system classpath plus any
   * URLClassLoader URLs in the current thread's context classloader chain.
   * The second part picks up Tomcat webapp classloader URLs which are not
   * exposed via java.class.path.
   *
   * @return list of compiler options ready to pass to JavaCompiler.getTask
   */
  private static List<String> buildCompilerOptions() {
    List<String> compilerOptions = new ArrayList<>();
    String classpath = currentClasspath();
    if (classpath != null && !classpath.isEmpty()) {
      compilerOptions.add("-classpath");
      compilerOptions.add(classpath);
    }
    return compilerOptions;
  }

  /**
   * Assemble the effective classpath visible to the running JVM. Combines
   * java.class.path with the URLs of any URLClassLoader in the current
   * thread's context classloader chain. Duplicates are removed but order is
   * preserved.
   *
   * @return classpath string with entries separated by File.pathSeparator
   */
  private static String currentClasspath() {
    Set<String> classpathEntries = new LinkedHashSet<>();
    String systemClasspath = System.getProperty("java.class.path");
    if (systemClasspath != null && !systemClasspath.isEmpty()) {
      for (String entry : systemClasspath.split(File.pathSeparator)) {
        if (entry != null && !entry.isEmpty()) {
          classpathEntries.add(entry);
        }
      }
    }
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    while (classLoader != null) {
      if (classLoader instanceof URLClassLoader) {
        for (URL url : ((URLClassLoader) classLoader).getURLs()) {
          String entry;
          try {
            entry = new File(url.toURI()).getPath();
          } catch (Exception e) {
            entry = url.getPath();
          }
          if (entry != null && !entry.isEmpty()) {
            classpathEntries.add(entry);
          }
        }
      }
      classLoader = classLoader.getParent();
    }
    return String.join(File.pathSeparator, classpathEntries);
  }

  /**
   * In-memory JavaFileObject that hands the compiler a source body as a
   * String. The URI shape (string:///path/to/Class.java) is what javac
   * expects from a programmatic source.
   */
  private static class InMemoryJavaSource extends SimpleJavaFileObject {
    private final String javaSource;

    InMemoryJavaSource(String fullyQualifiedClassName, String javaSource) {
      super(
          URI.create("string:///"
              + fullyQualifiedClassName.replace('.', '/')
              + Kind.SOURCE.extension),
          Kind.SOURCE);
      this.javaSource = javaSource;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return this.javaSource;
    }
  }

  /**
   * In-memory JavaFileObject that captures one compiled class's bytecode.
   * Used as the output destination for each class the compiler produces (top
   * level, inner, anonymous, lambda — javac emits one per).
   */
  private static class InMemoryCompiledClass extends SimpleJavaFileObject {
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    InMemoryCompiledClass(String fullyQualifiedClassName) {
      super(
          URI.create("string:///"
              + fullyQualifiedClassName.replace('.', '/')
              + Kind.CLASS.extension),
          Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
      return this.byteArrayOutputStream;
    }

    byte[] bytecode() {
      return this.byteArrayOutputStream.toByteArray();
    }
  }

  /**
   * JavaFileManager that captures compiler output (.class bytes) in memory
   * instead of writing to disk. Reads (classpath resolution, imports, etc.)
   * delegate to the standard file manager so the compiler can find Grouper
   * API and JDK classes normally.
   */
  private static class InMemoryJavaFileManager
      extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, InMemoryCompiledClass> compiledClasses = new LinkedHashMap<>();

    InMemoryJavaFileManager(JavaFileManager standardJavaFileManager) {
      super(standardJavaFileManager);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
        Location location,
        String className,
        JavaFileObject.Kind kind,
        FileObject sibling) throws IOException {
      if (kind == JavaFileObject.Kind.CLASS) {
        InMemoryCompiledClass inMemoryCompiledClass = new InMemoryCompiledClass(className);
        this.compiledClasses.put(className, inMemoryCompiledClass);
        return inMemoryCompiledClass;
      }
      return super.getJavaFileForOutput(location, className, kind, sibling);
    }

    Map<String, byte[]> getBytecode() {
      Map<String, byte[]> classNameToBytecode = new LinkedHashMap<>();
      for (Map.Entry<String, InMemoryCompiledClass> entry : this.compiledClasses.entrySet()) {
        classNameToBytecode.put(entry.getKey(), entry.getValue().bytecode());
      }
      return classNameToBytecode;
    }
  }

  /**
   * Build a two-line "source context" snippet — the offending line of source
   * followed by a line of spaces with a single '^' pointing at the column.
   * Mirrors javac's text output so a UI rendering this in a fixed-width font
   * gets a familiar look.
   *
   * @param javaSource the full source body
   * @param lineNumber 1-based line number where the diagnostic occurred
   * @param columnNumber 1-based column number where the diagnostic occurred
   * @return the two-line snippet, or null if the line cannot be extracted
   *   (line number past end of source, malformed inputs, etc.)
   */
  static String buildSourceContext(String javaSource, int lineNumber, int columnNumber) {
    if (javaSource == null || lineNumber < 1 || columnNumber < 1) {
      return null;
    }
    String sourceLine = extractLine(javaSource, lineNumber);
    if (sourceLine == null) {
      return null;
    }
    StringBuilder caretLine = new StringBuilder();
    for (int i = 1; i < columnNumber; i++) {
      caretLine.append(' ');
    }
    caretLine.append('^');
    return sourceLine + "\n" + caretLine.toString();
  }

  /**
   * Extract the specified 1-based line from the source string.
   *
   * @param source the full source body
   * @param lineNumber 1-based line to extract
   * @return the line content (no trailing newline or carriage return), or
   *   null if the line is past the end of the source
   */
  static String extractLine(String source, int lineNumber) {
    int currentLine = 1;
    int lineStart = 0;
    int sourceLength = source.length();
    for (int i = 0; i < sourceLength; i++) {
      if (source.charAt(i) == '\n') {
        if (currentLine == lineNumber) {
          int end = i;
          if (end > lineStart && source.charAt(end - 1) == '\r') {
            end--;
          }
          return source.substring(lineStart, end);
        }
        currentLine++;
        lineStart = i + 1;
      }
    }
    if (currentLine == lineNumber) {
      // Last line, no trailing newline
      int end = sourceLength;
      if (end > lineStart && source.charAt(end - 1) == '\r') {
        end--;
      }
      return source.substring(lineStart, end);
    }
    return null;
  }

  /**
   * Static-utility class; no instantiation.
   */
  private GshTemplateJavaCompiler() {
  }

}
