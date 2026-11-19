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
 * Stored XSS in the subject entity-data assignments page
 * (subjectViewDataFieldConfigContents.jsp): string values imported by an entity-data provider
 * (scalar data fields and data-row columns) were printed raw with JSP EL, so a provider value
 * holding markup ran script in the viewer's UI session.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP (every EL expression printed as page text must go through grouper:escapeHtml, except
 * the admin-configured externalized text labels) plus a check that the function behind
 * grouper:escapeHtml escapes markup. No database or Grouper config is needed.
 */
public class SubjectViewDataFieldConfigContentsJspTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectViewDataFieldConfigContents.jsp";

  /** escaped outputs that must be present, else the test would be checking nothing */
  private static final String[] REQUIRED_ESCAPED = {
      "grouper:escapeHtml(guiSubjectDataFieldConfig.aliases)",
      "grouper:escapeHtml(guiSubjectDataFieldConfig.uiFriendlyValue)",
      "grouper:escapeHtml(dataRowConfigIdToFieldConfigId.key)"
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(SubjectViewDataFieldConfigContentsJspTest.class);
  }

  /**
   *
   */
  public SubjectViewDataFieldConfigContentsJspTest() {
    super();
  }

  /**
   * @param name
   */
  public SubjectViewDataFieldConfigContentsJspTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property
   * subjectViewDataFieldConfigContentsJsp overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("subjectViewDataFieldConfigContentsJsp");
    if (override != null && override.length() > 0) {
      return new File(override);
    }
    String withoutProject = JSP_REPO_PATH.substring("grouper-ui/".length());
    String[] candidates = {
        JSP_REPO_PATH,
        withoutProject,
        "../" + JSP_REPO_PATH,
        "../grouper-ui/" + withoutProject
    };
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + JSP_REPO_PATH + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DsubjectViewDataFieldConfigContentsJsp=<path>");
    return null;
  }

  /**
   * every EL expression written as page text must be inside grouper:escapeHtml(...), except
   * externalized text labels. EL inside tag attributes (c:forEach items, c:when test, c:set
   * value) is not output, so it is removed before the check.
   * @throws Exception
   */
  public void testDynamicValuesAreEscapedInJsp() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    // JSP comments are not output
    String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

    // EL used as a whole tag attribute value is not output
    text = text.replaceAll("=\\s*\"\\$\\{[^}]*\\}\"", "=\"\"");

    List<String> rawUses = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
    while (matcher.find()) {
      String expression = matcher.group(1).trim();
      if (expression.startsWith("grouper:escapeHtml(") && expression.endsWith(")")) {
        continue;
      }
      // admin-configured externalized text, same as every other UI screen
      if (expression.startsWith("textContainer.text[")) {
        continue;
      }
      rawUses.add("${" + expression + "}");
    }
    assertTrue("Unescaped EL output in " + JSP_REPO_PATH + ": " + rawUses, rawUses.isEmpty());

    for (String required : REQUIRED_ESCAPED) {
      assertTrue("expected " + required + " in the JSP", jsp.contains(required));
    }

    // row cells: row[fieldConfigId] (6.3.0+ / 7.3.0+) or entry.value[rowIndex] (older shape)
    assertTrue("expected an escaped data-row cell value in the JSP",
        jsp.contains("grouper:escapeHtml(row[fieldConfigId])")
        || jsp.contains("grouper:escapeHtml(entry.value[rowIndex])"));
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize markup and
   * encode each character exactly once
   */
  public void testEscapeHtmlFunction() {
    String payload = "<img src=\"xss-proof-invalid\" onerror=\"document.title='GROUPER_XSS_PROOF'\">";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));
    assertFalse(escaped, escaped.contains("\""));

    // ampersand encoded once, ordinary text unchanged
    assertEquals("AT&amp;T", GrouperUiFunctions.escapeHtml("AT&T"));
    assertEquals("Jane Smith 12345", GrouperUiFunctions.escapeHtml("Jane Smith 12345"));
  }

}
