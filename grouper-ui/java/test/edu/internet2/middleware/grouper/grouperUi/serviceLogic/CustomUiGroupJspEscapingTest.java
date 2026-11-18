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
 * <p>Security issue customUiUserEnvironmentTableXss: the custom UI group screen
 * (customUiGroup.jsp) shows members of groupCanSeeUserEnvironment a table of the user query
 * variables, and screen-state viewers a table of the text scripts.  The variable value (for a
 * url query type or allowAssignFromUrl, the cu_ request parameter itself; otherwise LDAP / SQL /
 * Azure / Duo data), the label (EL with the operated-on subject) and the other cells were printed
 * raw, so a crafted link ran script in the viewer's session.  After the fix those cells go
 * through grouper:escapeHtml.  The description stays raw on purpose: it is built from Grouper's
 * own text templates, which hold links (guiGroup short links).</p>
 *
 * <p>There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP.  No database or Grouper config is needed.  Rendering is in the manual test steps.</p>
 */
public class CustomUiGroupJspEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH = "grouper-ui/webapp/WEB-INF/grouperUi2/group/customUiGroup.jsp";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(CustomUiGroupJspEscapingTest.class);
  }

  /**
   *
   */
  public CustomUiGroupJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public CustomUiGroupJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property customUiGroupJsp overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("customUiGroupJsp");
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
        + ", set -DcustomUiGroupJsp=<path to customUiGroup.jsp>");
    return null;
  }

  /**
   * every output of a user query display bean value (except the description) and of a text
   * result's script result, default and end-if-matches must be directly inside
   * grouper:escapeHtml(...).  Fails before the fix.
   * @throws Exception
   */
  public void testUserEnvironmentCellsAreEscaped() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    // output expressions only: ${customUiUserQueryDisplayBean.x} as a whole EL, not inside a
    // c:if test (forLoggedInUserBoolean)
    Pattern pattern = Pattern.compile("\\$\\{\\s*((?:grouper:escapeHtml\\()?)"
        + "(customUiUserQueryDisplayBean\\.(?:label|variableValue|variableName|variableType|userQueryType|forLoggedInUser)"
        + "|customUiTextResult\\.(?:scriptResult|theDefault|endIfMatches))\\b");
    Matcher matcher = pattern.matcher(jsp);
    List<String> rawUses = new ArrayList<String>();
    int escaped = 0;
    while (matcher.find()) {
      if (matcher.group(1).length() > 0) {
        escaped++;
      } else {
        int line = jsp.substring(0, matcher.start()).split("\n", -1).length;
        rawUses.add("line " + line + " " + matcher.group(2));
      }
    }
    assertTrue("Unescaped custom UI table values: " + rawUses, rawUses.isEmpty());
    // the nine cells must still be shown, else this test is checking nothing
    assertTrue("expected 9 escaped cells, found " + escaped, escaped >= 9);
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize a cu_ value
   */
  public void testEscapeHtmlFunction() {
    String escaped = GrouperUiFunctions.escapeHtml("<img src=x onerror=alert(document.domain)>");
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertEquals("true", GrouperUiFunctions.escapeHtml("true"));
  }
}
