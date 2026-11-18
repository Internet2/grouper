package edu.internet2.middleware.grouper.app.workflow;

import static edu.internet2.middleware.grouper.app.workflow.GrouperWorkflowConstants.INITIATE_STATE;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import junit.textui.TestRunner;

/**
 * Tests for advisory H1 (workflowRawHtmlStoredXssSysadminTakeover): a workflow editor must not be
 * able to store script or event handlers in the workflow form HTML, which is shown to initiators
 * and approvers (possibly wheel) in the Grouper UI.
 *
 * Before the fix GrouperWorkflowHtmlSanitizer does not exist (this class does not compile) and
 * GrouperWorkflowConfigValidator accepts the reporter's payload.
 */
public class GrouperWorkflowHtmlSanitizerTest extends GrouperTest {

  /**
   * the reporter's payload (an img onerror handler next to a valid param textarea)
   */
  private static final String REPORTER_FORM = "<div id=\"rawWorkflowProof\">Approval request</div>\n"
      + "<img src=\"x-workflow-proof\" onerror=\"document.title='WF_RAW_XSS'\">\n"
      + "<textarea name=\"notes\" id=\"notes\"></textarea>";

  /**
   * a plain form with the default params (notes, notesForApprovers)
   */
  private static final String PLAIN_FORM = "<p>Submit this <b>form</b>. See "
      + "<a href=\"https://www.example.edu/help\">help</a>.</p>"
      + "<table class=\"table\"><tr><td><label for=\"notesId\">Notes</label></td>"
      + "<td><textarea rows=\"4\" cols=\"50\" name=\"notes\" id=\"notesId\"></textarea></td></tr>"
      + "<tr><td>Approvers</td><td><textarea name=\"notesForApprovers\"></textarea></td></tr></table>";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWorkflowHtmlSanitizerTest("testValidatorRejectsReporterForm"));
  }

  /**
   * @param name
   */
  public GrouperWorkflowHtmlSanitizerTest(String name) {
    super(name);
  }

  /**
   * root session
   */
  private GrouperSession grouperSession;

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    grouperSession = grouperSessionResult.getGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("workflow.enable", "true");
  }

  // ---------------------------------------------------------------- sanitize()

  /**
   * active content is removed, the form controls stay
   */
  public void testSanitizeRemovesActiveContent() {

    String reporter = GrouperWorkflowHtmlSanitizer.sanitize(REPORTER_FORM);
    assertNoActiveContent(reporter);
    assertFalse(reporter, reporter.contains("<img"));
    assertTrue(reporter, reporter.contains("name=\"notes\""));
    assertTrue(reporter, reporter.contains("Approval request"));

    String nasty = "<svg onload=\"alert(1)\"><circle/></svg>"
        + "<script>alert(2)</script>"
        + "<iframe srcdoc=\"&lt;script&gt;alert(3)&lt;/script&gt;\"></iframe>"
        + "<object data=\"x.swf\"></object><embed src=\"x.swf\">"
        + "<math><mtext>m</mtext></math>"
        + "<a href=\"javascript:alert(4)\">js</a>"
        + "<a href=\"data:text/html,x\">data</a>"
        + "<a href=\"https://ok.example.edu/\" target=\"_blank\">ok</a>"
        + "<form action=\"https://evil.example.com/\"><input name=\"notes\" formaction=\"javascript:alert(5)\" "
        + "onfocus=\"alert(6)\" autofocus style=\"position:fixed;top:0\"></form>"
        + "<button onclick=\"alert(7)\">b</button>"
        + "<div onmouseover=\"alert(8)\" style=\"position:fixed\">d</div>"
        + "<style>body{display:none}</style><base href=\"https://evil.example.com/\">"
        + "<meta http-equiv=\"refresh\" content=\"0;url=https://evil.example.com/\">";

    String clean = GrouperWorkflowHtmlSanitizer.sanitize(nasty);
    assertNoActiveContent(clean);
    for (String gone : new String[] {"<svg", "<script", "<iframe", "srcdoc", "<object", "<embed",
        "<math", "javascript:", "data:", "<form", "formaction", "autofocus", "<button", "style",
        "<base", "<meta", "target=", "evil.example.com"}) {
      assertFalse(gone + " in " + clean, clean.toLowerCase().contains(gone));
    }
    // allowed things survive
    assertTrue(clean, clean.contains("href=\"https://ok.example.edu/\""));
    assertTrue(clean, clean.contains("<input name=\"notes\">"));
    assertTrue(clean, clean.contains(">d</div>"));
  }

  /**
   * a stored instance snapshot (a whole document with populated values and the audit line)
   * keeps every control, its value and its disabled / checked state
   */
  public void testSanitizeKeepsSnapshotControls() {

    String snapshot = "<html><head></head><body>"
        + "<div style='margin-bottom: 10px'><span style='font-weight: bold'>Name</span></div>"
        + "<table class='table table-condensed table-striped'><tr>"
        + "<td><strong><label>Notes</label></strong></td><td>"
        + "<textarea cols='20' rows='3' disabled name=notes>please &lt;b&gt;add&lt;/b&gt; me</textarea>"
        + "<input type='text'disabled name=reason value='because'></input>"
        + "<input type='checkbox' disabled name=agree checked></input>"
        + "</td></tr></table>"
        + "<div>jdbc: 123, <img src=x onerror=alert(1)> clicked submit at state initiate</div>"
        + "</body></html>";

    String clean = GrouperWorkflowHtmlSanitizer.sanitize(snapshot);
    assertNoActiveContent(clean);

    Document document = Jsoup.parseBodyFragment(clean);
    assertEquals("please <b>add</b> me", document.selectFirst("[name=notes]").val());
    assertTrue(document.selectFirst("[name=notes]").hasAttr("disabled"));
    assertEquals("because", document.selectFirst("[name=reason]").val());
    assertEquals("text", document.selectFirst("[name=reason]").attr("type"));
    assertTrue(document.selectFirst("[name=agree]").hasAttr("checked"));
    assertEquals("checkbox", document.selectFirst("[name=agree]").attr("type"));
    assertTrue(clean, clean.contains("clicked submit at state initiate"));
    assertTrue(clean, clean.contains("class=\"table table-condensed table-striped\""));
  }

  /**
   * Grouper's own forms (the default config form text, and the form built from params) keep
   * every param control after sanitizing
   */
  public void testSanitizeKeepsGrouperGeneratedForms() {

    String defaultForm = GrouperTextContainer.retrieveFromRequest().getText().get("workflowConfigDefaultHtmlForm");
    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe(defaultForm));

    GrouperWorkflowConfigParams params = GrouperWorkflowConfigParams.buildParamsFromJsonString(
        GrouperWorkflowConfigParams.getDefaultConfigParamsString());

    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    config.setConfigParams(params);
    config.setWorkflowConfigName("Join <script>alert(1)</script> group");
    config.setWorkflowConfigDescription("desc");

    // from the default config form
    String fromForm = GrouperWorkflowHtmlSanitizer.sanitize(config.buildInitialHtml(INITIATE_STATE));
    // from params only (blank form)
    config.setWorkflowConfigForm(null);
    String fromParams = GrouperWorkflowHtmlSanitizer.sanitize(config.buildInitialHtml(INITIATE_STATE));
    assertNoActiveContent(fromParams);

    for (String html : new String[] {fromForm, fromParams}) {
      Document document = Jsoup.parseBodyFragment(html);
      for (GrouperWorkflowConfigParam param : params.getParams()) {
        assertNotNull(param.getParamName() + " in " + html,
            document.selectFirst("[name=" + param.getParamName() + "]"));
      }
    }
  }

  /**
   * null and blank pass through
   */
  public void testNullAndBlank() {
    assertNull(GrouperWorkflowHtmlSanitizer.sanitize(null));
    assertEquals("", GrouperWorkflowHtmlSanitizer.sanitize(""));
    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe(null));
    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe("  "));
  }

  // ---------------------------------------------------------------- isSafe()

  /**
   * unsafe forms are detected, plain ones are not
   */
  public void testIsSafe() {
    assertFalse(GrouperWorkflowHtmlSanitizer.isSafe(REPORTER_FORM));
    assertFalse(GrouperWorkflowHtmlSanitizer.isSafe("<a href=\"javascript:alert(1)\">x</a>"));
    assertFalse(GrouperWorkflowHtmlSanitizer.isSafe("<svg><a onclick=\"alert(1)\">x</a></svg>"));
    assertFalse(GrouperWorkflowHtmlSanitizer.isSafe("<input name=\"notes\" onfocus=\"alert(1)\" autofocus>"));
    assertFalse(GrouperWorkflowHtmlSanitizer.isSafe("<script>alert(1)</script>"));

    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe(PLAIN_FORM));
    // the existing validator test uses this
    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe("<html></html>"));
    // harmless parse errors (stray end tags) are not unsafe
    assertTrue(GrouperWorkflowHtmlSanitizer.isSafe("<input type='text' name='notes'></input>"));
  }

  // ---------------------------------------------------------------- validator

  /**
   * the validator rejects the reporter's form and accepts a plain one
   */
  public void testValidatorRejectsReporterForm() {

    Group group = new GroupSave(grouperSession).assignName("workflow:test57")
        .assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.group).save();

    String invalidFormError = GrouperTextContainer.retrieveFromRequest().getText().get("workflowConfigInvalidConfigForm");

    List<String> errors = new GrouperWorkflowConfigValidator().validate(
        buildConfig(group, "test57unsafe", REPORTER_FORM), group, true);
    assertTrue(errors.toString(), errors.contains(invalidFormError));

    errors = new GrouperWorkflowConfigValidator().validate(
        buildConfig(group, "test57safe", PLAIN_FORM), group, true);
    assertFalse(errors.toString(), errors.contains(invalidFormError));
  }

  /**
   * build a valid config (except for the form) on this group
   * @param ownerGroup
   * @param workflowId
   * @param form
   * @return the config
   */
  private GrouperWorkflowConfig buildConfig(Group ownerGroup, String workflowId, String form) {
    GrouperWorkflowConfig config = new GrouperWorkflowConfig();
    String approvalStatesString = GrouperWorkflowApprovalStates.getDefaultApprovalStatesString(ownerGroup.getId());
    config.setWorkflowConfigApprovalsString(approvalStatesString);
    config.setWorkflowApprovalStates(GrouperWorkflowApprovalStates.buildApprovalStatesFromJsonString(approvalStatesString));
    config.setOwnerGroup(ownerGroup);
    config.setWorkflowConfigDescription("test 57");
    config.setWorkflowConfigEnabled(GrouperWorkflowConstants.WORKFLOW_CONFIG_ENABLED_TRUE);
    config.setWorkflowConfigForm(form);
    config.setWorkflowConfigId(workflowId);
    config.setWorkflowConfigName(workflowId);
    String paramsString = GrouperWorkflowConfigParams.getDefaultConfigParamsString();
    config.setWorkflowConfigParamsString(paramsString);
    config.setConfigParams(GrouperWorkflowConfigParams.buildParamsFromJsonString(paramsString));
    config.setWorkflowConfigSendEmail(false);
    config.setWorkflowConfigType(GrouperWorkflowSettings.configTypes().iterator().next());
    return config;
  }

  /**
   * no script, event handler or scriptable url in this html
   * @param html
   */
  private static void assertNoActiveContent(String html) {
    String lower = html.toLowerCase();
    assertFalse(html, lower.contains("<script"));
    assertFalse(html, lower.contains("javascript:"));
    assertFalse(html, lower.matches("(?s).*\\son[a-z]+\\s*=.*"));
  }
}
