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
 * Stored XSS in the workflow screens: a group admin who can configure a workflow could save HTML
 * in the workflow name, description, approval states (state names) or params, and the workflow
 * JSPs printed those values raw, so script ran for the next user who opened the workflow config
 * details, the group's workflow list, the instance lists, or the edit form (textarea breakout).
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of the
 * workflow JSPs: every EL expression that prints one of the user-controlled workflow properties
 * as a bare bean path must go through grouper:escapeHtml. JSTL tag attributes (c:set, c:if, ...)
 * and the grouper:combobox2 tag attributes are not page output and are removed first. No database
 * or Grouper config is needed.
 */
public class WorkflowJspEscapingTest extends TestCase {

  /** folder of the workflow JSPs */
  private static final String WORKFLOW_DIR = "grouper-ui/webapp/WEB-INF/grouperUi2/workflow/";

  /** JSPs under test, and one escaped output each must contain (so the scan is not vacuous) */
  private static final String[][] JSP_AND_REQUIRED = {
      {"grouperWorkflowConfigDetails.jsp", "${grouper:escapeHtml(workflowConfig.workflowConfigName)}"},
      {"groupWorkflowConfig.jsp", "${grouper:escapeHtml(guiWorkflowConfig.grouperWorkflowConfig.workflowConfigName)}"},
      {"grouperWorkflowInstancesSubjectInitiated.jsp", "${grouper:escapeHtml(instance.workflowInstanceState)}"},
      {"grouperWorkflowWaitingForApprovals.jsp", "${grouper:escapeHtml(instance.workflowInstanceState)}"},
      {"grouperWorkflowViewInstances.jsp", "${grouper:escapeHtml(instance.workflowInstanceState)}"},
      {"workflowConfigObjectAddHelper.jsp", "${grouper:escapeHtml(guiWorkflowConfig.workflowApprovalStates)}"},
      {"workflowConfigObjectEditHelper.jsp", "${grouper:escapeHtml(guiWorkflowConfig.grouperWorkflowConfig.workflowConfigApprovalsString)}"}
  };

  /**
   * a bare bean path ending in a user-controlled workflow property (set by whoever can configure
   * the workflow). Expressions like "a == b ? 'selected' : ''" print a literal and do not match.
   */
  private static final Pattern SENSITIVE_PATH = Pattern.compile(
      "^[A-Za-z0-9_.]+\\.(workflowConfigId|workflowConfigName|workflowConfigDescription"
      + "|workflowConfigType|workflowApprovalStates|workflowConfigParams"
      + "|workflowConfigApprovalsString|workflowConfigParamsString|workflowInstanceState)$");

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WorkflowJspEscapingTest.class);
  }

  /**
   *
   */
  public WorkflowJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public WorkflowJspEscapingTest(String name) {
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
   * user-controlled workflow values must only be printed through grouper:escapeHtml
   * @throws Exception
   */
  public void testWorkflowValuesAreEscapedInJsps() throws Exception {
    for (String[] jspAndRequired : JSP_AND_REQUIRED) {
      String repoPath = WORKFLOW_DIR + jspAndRequired[0];
      String jsp = new String(Files.readAllBytes(findJsp(repoPath).toPath()), StandardCharsets.UTF_8);

      // JSP comments are not output
      String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

      // JSTL tags: their attribute EL is evaluated, not printed
      text = text.replaceAll("<c:(set|if|when|forEach|choose|otherwise)\\b(?:[^>\"]|\"[^\"]*\")*>", "");

      // combobox2 attributes go to the tag handler, not the page (the viewers group value has to
      // resolve to a group uuid, GuiGrouperWorkflowConfig looks it up with exceptionIfNull)
      text = text.replaceAll("<grouper:combobox2\\b(?:[^>\"]|\"[^\"]*\")*>", "");

      List<String> rawUses = new ArrayList<String>();
      Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
      while (matcher.find()) {
        String expression = matcher.group(1).trim();
        if (SENSITIVE_PATH.matcher(expression).matches()) {
          rawUses.add("${" + expression + "}");
        }
      }
      assertTrue("Unescaped workflow value in " + repoPath + ": " + rawUses, rawUses.isEmpty());

      assertTrue("expected " + jspAndRequired[1] + " in " + repoPath, jsp.contains(jspAndRequired[1]));
    }
  }

  /**
   * the function mapped to grouper:escapeHtml must make the reported payload plain text, close no
   * textarea, and keep ordinary workflow text (and JSON) readable
   */
  public void testEscapeHtmlFunction() {
    String payload = "Workflow <img src=x onerror=\"document.body.dataset.workflowXss=1\">";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("Workflow &lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains("\""));

    String textareaBreakout = GrouperUiFunctions.escapeHtml("</textarea><script>alert(1)</script>");
    assertFalse(textareaBreakout, textareaBreakout.contains("</textarea>"));

    // ordinary values unchanged, ampersand encoded once
    assertEquals("Access request", GrouperUiFunctions.escapeHtml("Access request"));
    assertEquals("a&amp;b", GrouperUiFunctions.escapeHtml("a&b"));
    assertEquals("{ &quot;approvers&quot;: [] }", GrouperUiFunctions.escapeHtml("{ \"approvers\": [] }"));
  }

}
