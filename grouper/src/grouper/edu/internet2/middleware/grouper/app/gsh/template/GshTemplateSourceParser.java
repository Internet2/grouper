package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the package declaration and the public class name from a Java
 * source string, producing the fully-qualified class name. Used by
 * GshTemplateClassLoaderRegistry to determine the FQN before handing the
 * source to GshTemplateJavaCompiler — javac needs the source URI to match
 * the declared FQN, so we have to figure out the FQN before compiling.
 *
 * Approach: strip line and block comments, then regex-match `package X;` and
 * the first `public class Y` (or `public final class Y`, `public abstract
 * class Y`, etc.). Concatenate to X.Y.
 *
 * Limitations: a regex parser is not a Java parser. Pathological sources
 * with `public class` appearing inside a string literal or with comment
 * markers inside string literals can confuse it. For real-world GSH template
 * authoring — a single GshTemplateV2 subclass per template — this is fine.
 *
 * GRP-7010
 */
public class GshTemplateSourceParser {

  /**
   * Matches block comments, including those spanning multiple lines.
   * Non-greedy so adjacent block comments don't merge.
   */
  private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*[\\s\\S]*?\\*/");

  /**
   * Matches single-line comments to end of line.
   */
  private static final Pattern LINE_COMMENT = Pattern.compile("//[^\n\r]*");

  /**
   * Matches a Java package declaration: `package some.qualified.name;`
   */
  private static final Pattern PACKAGE_DECLARATION =
      Pattern.compile("\\bpackage\\s+([a-zA-Z_$][\\w$]*(?:\\.[a-zA-Z_$][\\w$]*)*)\\s*;");

  /**
   * Matches a public top-level class declaration. Allows the modifiers
   * abstract, final, and static (in any order before `class`).
   */
  private static final Pattern PUBLIC_CLASS_DECLARATION =
      Pattern.compile("\\bpublic\\s+(?:(?:abstract|final|static)\\s+)*class\\s+([A-Za-z_$][\\w$]*)");

  /**
   * Outcome of a parse attempt — either a fully-qualified class name on
   * success, or a human-readable error string on failure.
   */
  public static class GshTemplateSourceParseResult {

    private final String fullyQualifiedClassName;
    private final String errorMessage;

    private GshTemplateSourceParseResult(String fullyQualifiedClassName, String errorMessage) {
      this.fullyQualifiedClassName = fullyQualifiedClassName;
      this.errorMessage = errorMessage;
    }

    static GshTemplateSourceParseResult success(String fullyQualifiedClassName) {
      return new GshTemplateSourceParseResult(fullyQualifiedClassName, null);
    }

    static GshTemplateSourceParseResult failure(String errorMessage) {
      return new GshTemplateSourceParseResult(null, errorMessage);
    }

    /**
     * @return true if parsing succeeded
     */
    public boolean isSuccess() {
      return this.errorMessage == null;
    }

    /**
     * @return the fully-qualified class name on success; null on failure
     */
    public String getFullyQualifiedClassName() {
      return this.fullyQualifiedClassName;
    }

    /**
     * @return the error message on failure; null on success
     */
    public String getErrorMessage() {
      return this.errorMessage;
    }
  }

  /**
   * Parse the given Java source body and return the fully-qualified name of
   * its top-level public class. On any failure (no package declaration, no
   * public class, null source) returns a failure result with a clear
   * message; never throws.
   *
   * @param javaSource the full Java source body
   * @return parse result with the FQN, or with an error message
   */
  public static GshTemplateSourceParseResult parse(String javaSource) {
    if (javaSource == null) {
      return GshTemplateSourceParseResult.failure("Java source is null");
    }

    String stripped = stripComments(javaSource);

    // Package declaration is optional — default package is allowed. (Not Java
    // best practice, but legal and convenient for short GSH templates.)
    String packageName = null;
    Matcher packageMatcher = PACKAGE_DECLARATION.matcher(stripped);
    if (packageMatcher.find()) {
      packageName = packageMatcher.group(1);
    }

    Matcher classMatcher = PUBLIC_CLASS_DECLARATION.matcher(stripped);
    if (!classMatcher.find()) {
      return GshTemplateSourceParseResult.failure(
          "No public class declaration found in source. The parser is looking for one of:\n"
              + "    public class Name { ... }\n"
              + "    public final class Name { ... }\n"
              + "    public abstract class Name { ... }\n"
              + "(any combination of 'abstract', 'final', 'static' modifiers before 'class' is accepted)\n"
              + "Common causes if you have a class declaration but the parser is not finding it:\n"
              + "  - The class is not declared 'public' (top-level template classes must be public)\n"
              + "  - The class declaration is inside a string literal or unusual comment shape\n"
              + "    that confuses the regex parser. Restructure so the declaration is at\n"
              + "    file top level on its own line.\n"
              + "  - You declared a 'public interface' or 'public enum' — Java GSH templates\n"
              + "    must be a regular class extending GshTemplateV2.");
    }
    String className = classMatcher.group(1);

    String fullyQualifiedClassName;
    if (packageName == null || packageName.length() == 0) {
      fullyQualifiedClassName = className;
    } else {
      fullyQualifiedClassName = packageName + "." + className;
    }
    return GshTemplateSourceParseResult.success(fullyQualifiedClassName);
  }

  /**
   * Remove block comments and line comments from source. Block comments are
   * stripped first to handle '//' inside '/* ... *_/' correctly. (Slash-star
   * intentionally broken in this javadoc to avoid ending the enclosing
   * comment block.)
   *
   * @param source raw Java source
   * @return source with all // and /* *_/ comments replaced by empty string
   */
  static String stripComments(String source) {
    String withoutBlockComments = BLOCK_COMMENT.matcher(source).replaceAll("");
    String withoutLineComments = LINE_COMMENT.matcher(withoutBlockComments).replaceAll("");
    return withoutLineComments;
  }

  /**
   * Static-utility class; no instantiation.
   */
  private GshTemplateSourceParser() {
  }

}
