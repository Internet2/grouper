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
 * Stored XSS in the permission-limit value editors (groupPermissionLimitValueEdit.jsp and
 * subjectPermissions/permissionLimitValueEdit.jsp): the stored limit value was printed raw inside
 * value="...", so a delegated permission manager could store
 * <code>true" autofocus onfocus="..."</code> and run script in the next administrator's session
 * who opened "Edit limit value". The action name and limit name on the same screens were raw too.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of the
 * two JSPs (every EL expression written to the page must go through grouper:escapeHtml, except
 * externalized text labels) plus a check that the function behind grouper:escapeHtml makes the
 * reproduction payload inert inside a quoted attribute. No database or Grouper config is needed.
 */
public class PermissionLimitValueEditJspEscapingTest extends TestCase {

  /** repo paths of the JSPs under test */
  private static final String[] JSP_REPO_PATHS = {
      "grouper-ui/webapp/WEB-INF/grouperUi2/groupPermissions/groupPermissionLimitValueEdit.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/subjectPermissions/permissionLimitValueEdit.jsp"
  };

  /** escaped outputs that must be present in each JSP, else the test would be checking nothing */
  private static final String[] REQUIRED_ESCAPED = {
      // inside double quotes: grouper:escapeHtml does not escape the single quote
      "value=\"${grouper:escapeHtml(permissionUpdateRequestContainer.attributeAssignValue.valueFriendly)}\"",
      "grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssign.attributeAssign.attributeAssignAction.name)",
      "grouper:escapeHtml(permissionUpdateRequestContainer.guiAttributeAssignAssign.attributeAssign.attributeDefName.displayName)"
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(PermissionLimitValueEditJspEscapingTest.class);
  }

  /**
   *
   */
  public PermissionLimitValueEditJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public PermissionLimitValueEditJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find a JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ). System property grouperUiWebappDir (the
   * grouper-ui/webapp directory) overrides.
   * @param repoPath
   * @return the JSP file
   */
  static File findJsp(String repoPath) {
    String withoutProject = repoPath.substring("grouper-ui/".length());
    String override = System.getProperty("grouperUiWebappDir");
    if (override != null && override.length() > 0) {
      return new File(override, withoutProject.substring("webapp/".length()));
    }
    String[] candidates = {
        repoPath,
        withoutProject,
        "../" + repoPath,
        "../grouper-ui/" + withoutProject
    };
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + repoPath + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DgrouperUiWebappDir=<path to grouper-ui/webapp>");
    return null;
  }

  /**
   * every EL expression written to the page must be inside grouper:escapeHtml(...), except
   * externalized text labels. EL in JSTL tag attributes (c:set value, c:if test, ...) is not
   * output, so those tags are removed before the check.
   * @throws Exception
   */
  public void testDynamicValuesAreEscapedInJsps() throws Exception {
    for (String repoPath : JSP_REPO_PATHS) {
      String jsp = new String(Files.readAllBytes(findJsp(repoPath).toPath()), StandardCharsets.UTF_8);

      // JSP comments are not output
      String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

      // JSTL tags: their attribute EL is evaluated, not printed
      text = text.replaceAll("<c:(set|if|when|forEach|choose|otherwise)\\b(?:[^>\"]|\"[^\"]*\")*>", "");

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
      assertTrue("Unescaped EL output in " + repoPath + ": " + rawUses, rawUses.isEmpty());

      for (String required : REQUIRED_ESCAPED) {
        assertTrue("expected " + required + " in " + repoPath, jsp.contains(required));
      }
    }
  }

  /**
   * the function mapped to grouper:escapeHtml must make the reproduction payload inert inside a
   * double-quoted attribute (it does not escape the single quote, so the JSPs only use it inside
   * double quotes), and encode each character exactly once
   */
  public void testEscapeHtmlFunction() {
    String payload = "true\" autofocus onfocus=\"document.title='PERMISSION_LIMIT_XSS'";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertFalse(escaped, escaped.contains("\""));
    assertTrue(escaped, escaped.startsWith("true&quot; autofocus"));

    String tags = GrouperUiFunctions.escapeHtml("<svg/onload=alert(1)>");
    assertFalse(tags, tags.contains("<"));
    assertFalse(tags, tags.contains(">"));

    // ampersand encoded once, ordinary limit values unchanged
    assertEquals("a&amp;b", GrouperUiFunctions.escapeHtml("a&b"));
    assertEquals("amount = 5", GrouperUiFunctions.escapeHtml("amount = 5"));
  }

}
