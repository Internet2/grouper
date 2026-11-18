package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.ui.tags.GrouperUiFunctions;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Stored XSS in subject resolution (USDU): the subject resolution search result
 * (subjectResolutionViewSubject.jsp) printed subject source values (source id, identifier,
 * name, description) raw to a wheel user, and the unresolved subjects list
 * (unresolvedSubjectsHelper.jsp) printed the cached member subject id raw as link text.
 * Anyone who can set a displayed value in an upstream subject source could run script in the
 * wheel user's session.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the two JSPs (every EL expression that outputs a subject derived value must go through
 * grouper:escapeHtml or grouper:escapeUrl) plus a check that the function behind
 * grouper:escapeHtml escapes markup. No database or Grouper config is needed.
 */
public class SubjectResolutionJspEscapingTest extends TestCase {

  /** repo paths of the JSPs under test */
  private static final String[] JSP_REPO_PATHS = {
      "grouper-ui/webapp/WEB-INF/grouperUi2/subjectResolution/subjectResolutionViewSubject.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/subjectResolution/unresolvedSubjectsHelper.jsp"
  };

  /** one EL expression, ${...} (EL in these JSPs has no nested braces) */
  private static final Pattern EL_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

  /**
   * subject derived values: member columns copied from the subject source, live subject
   * fields, and the subjectId / sourceId page variables set from them
   */
  private static final Pattern SUBJECT_VALUE_PATTERN = Pattern.compile(
      "(\\bmember\\.(subjectSourceId|subjectId|subjectIdentifier0|name|description)\\b)"
      + "|(\\bsubject\\.(sourceId|id|name|description)\\b)"
      + "|(^\\s*(subjectId|sourceId)\\s*$)");

  /** expression is safely encoded for HTML output */
  private static final Pattern ESCAPED_PATTERN = Pattern.compile(
      "^\\s*grouper:(escapeHtml|escapeUrl)\\(.*\\)\\s*$");

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(SubjectResolutionJspEscapingTest.class);
  }

  /**
   *
   */
  public SubjectResolutionJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public SubjectResolutionJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find a JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property
   * subjectResolutionJspDir (the subjectResolution folder) overrides.
   * @param jspRepoPath
   * @return the JSP file
   */
  private static File findJsp(String jspRepoPath) {
    String override = System.getProperty("subjectResolutionJspDir");
    if (override != null && override.length() > 0) {
      return new File(override, new File(jspRepoPath).getName());
    }
    String withoutProject = jspRepoPath.substring("grouper-ui/".length());
    String[] candidates = {
        jspRepoPath,
        withoutProject,
        "../" + jspRepoPath,
        "../grouper-ui/" + withoutProject
    };
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + jspRepoPath + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DsubjectResolutionJspDir=<path to subjectResolution folder>");
    return null;
  }

  /**
   * every EL expression that outputs a subject derived value must be wrapped in
   * grouper:escapeHtml(...) (or grouper:escapeUrl(...) inside link urls). Expressions in a
   * c:set value attribute only assign a page variable (the variable is checked where it is
   * output), so they are skipped.
   * @throws Exception
   */
  public void testSubjectValuesAreEscapedInJsps() throws Exception {
    List<String> rawUses = new ArrayList<String>();
    for (String jspRepoPath : JSP_REPO_PATHS) {
      String jsp = new String(Files.readAllBytes(findJsp(jspRepoPath).toPath()),
          StandardCharsets.UTF_8);

      Matcher matcher = EL_PATTERN.matcher(jsp);
      int outputUses = 0;
      while (matcher.find()) {
        String expression = matcher.group(1);
        if (!SUBJECT_VALUE_PATTERN.matcher(expression).find()) {
          continue;
        }

        // c:set value="${...}" is an assignment, not output
        int lineStart = jsp.lastIndexOf('\n', matcher.start()) + 1;
        String lineBefore = jsp.substring(lineStart, matcher.start());
        if (lineBefore.contains("<c:set") && lineBefore.endsWith("value=\"")) {
          continue;
        }

        outputUses++;
        if (!ESCAPED_PATTERN.matcher(expression).matches()) {
          int line = jsp.substring(0, matcher.start()).split("\n", -1).length;
          rawUses.add(new File(jspRepoPath).getName() + " line " + line + ": ${" + expression + "}");
        }
      }
      // subject values must still be shown, else this test is checking nothing
      assertTrue("expected subject values to be output in " + jspRepoPath, outputUses > 0);
    }
    assertTrue("Unescaped subject values in subject resolution JSPs: " + rawUses,
        rawUses.isEmpty());
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize markup
   */
  public void testEscapeHtmlFunction() {
    String payload = "<img src=x onerror=alert(7331)>";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));

    // ordinary subject names are unchanged
    assertEquals("my name is test.subject.2",
        GrouperUiFunctions.escapeHtml("my name is test.subject.2"));
  }

}
