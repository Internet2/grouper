package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.ui.tags.GrouperUiFunctions;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Stored XSS in the provisioning screens: values from the grouper_sync* tables (attribute value
 * caches 0-3, group names, subject ids and identifiers, membership ids, job error messages, log
 * descriptions) were printed with raw ${...} in the provisioning target details, provisioner
 * activity, errors, job details and config details JSPs.  A group admin could set a group
 * display name with HTML, a sync copied it into the cache column, and it ran in the session of
 * a wheel or provisioner viewer who opened those screens.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * each JSP: every EL expression written as page text (outside an HTML or JSP tag) must go
 * through grouper:escapeHtml, except externalized text labels and the page title helper.
 * EL inside tag attributes (c:if test, c:set value, grouper:abbreviateTextarea text, the
 * metadata form element tag) is not page text and is not checked here.  No database or Grouper
 * config is needed.
 */
public class ProvisioningSyncJspEscapingTest extends TestCase {

  /** JSP folder under the repo root */
  private static final String UI2 = "grouper-ui/webapp/WEB-INF/grouperUi2/";

  /**
   * repo path of each JSP, and an escaped output that must be present so the check is not
   * vacuous (the reported sink, or the most dangerous sync column in that file)
   */
  private static final String[][] JSPS = {
      {UI2 + "provisioning/provisioningGroupTargetDetailsHelper.jsp",
          "${grouper:escapeHtml(grouperRequestContainer.provisioningContainer.gcGrouperSyncGroup.groupAttributeValueCache0)}"},
      {UI2 + "provisioning/provisioningSubjectTargetDetailsHelper.jsp",
          "${grouper:escapeHtml(grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMember.entityAttributeValueCache0)}"},
      {UI2 + "provisioning/provisioningGroupOrSubjectMembershipDetailsHelper.jsp",
          "${grouper:escapeHtml(grouperRequestContainer.provisioningContainer.guiGrouperSyncObject.gcGrouperSyncMembership.membershipId)}"},
      {UI2 + "provisioning/provisioningMembershipDetailsHelper.jsp",
          "${grouper:escapeHtml(grouperSyncMembership.membershipId)}"},
      {UI2 + "provisionerConfigs/provisioningConfigActivity.jsp",
          "${grouper:escapeHtml(provisionerActivity.objectAttributeValueCache0)}"},
      {UI2 + "provisionerConfigs/provisioningConfigActivityGroup.jsp",
          "${grouper:escapeHtml(grouperSyncGroup.groupAttributeValueCache0)}"},
      {UI2 + "provisionerConfigs/provisioningConfigActivityMember.jsp",
          "${grouper:escapeHtml(grouperSyncMember.entityAttributeValueCache1)}"},
      {UI2 + "provisionerConfigs/provisioningConfigActivityMembership.jsp",
          "${grouper:escapeHtml(grouperSyncMembership.grouperSyncGroup.groupName)}"},
      {UI2 + "provisionerConfigs/provisioningConfigErrors.jsp",
          "${grouper:escapeHtml(error.groupName)}"},
      {UI2 + "provisionerConfigs/provisionerConfigJobDetails.jsp",
          "${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.grouperSyncJob.errorMessage)}"},
      {UI2 + "provisionerConfigs/provisionerConfigDetails.jsp",
          "${grouper:escapeHtml(syncJob.gcGrouperSyncLog.description)}"}
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ProvisioningSyncJspEscapingTest.class);
  }

  /**
   *
   */
  public ProvisioningSyncJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public ProvisioningSyncJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find a JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property provisioningSyncJspRepoRoot
   * (the repo root directory) overrides.
   * @param repoPath path from the repo root
   * @return the JSP file
   */
  private static File findJsp(String repoPath) {
    String override = System.getProperty("provisioningSyncJspRepoRoot");
    if (override != null && override.length() > 0) {
      return new File(override, repoPath);
    }
    String withoutProject = repoPath.substring("grouper-ui/".length());
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
        + ", set -DprovisioningSyncJspRepoRoot=<repo root>");
    return null;
  }

  /**
   * EL expressions written as page text, i.e. not inside an HTML or JSP tag and not in a JSP
   * comment.  Scans character by character so EL that spans a quote or contains a brace in a
   * string literal is skipped as one unit.
   * @param jsp JSP source
   * @return the expressions (without ${ and }), trimmed
   */
  static List<String> textElExpressions(String jsp) {

    // JSP comments are not output
    String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

    List<String> result = new ArrayList<String>();
    boolean inTag = false;
    int length = text.length();
    int i = 0;
    while (i < length) {
      char c = text.charAt(i);
      if (text.startsWith("${", i)) {
        // find the matching close brace, ignoring braces in quoted literals
        int j = i + 2;
        int depth = 1;
        char quote = 0;
        while (j < length && depth > 0) {
          char ch = text.charAt(j);
          if (quote != 0) {
            if (ch == quote) {
              quote = 0;
            }
          } else if (ch == '\'' || ch == '"') {
            quote = ch;
          } else if (ch == '{') {
            depth++;
          } else if (ch == '}') {
            depth--;
          }
          j++;
        }
        if (!inTag) {
          result.add(text.substring(i + 2, Math.max(i + 2, j - 1)).trim());
        }
        i = j;
        continue;
      }
      if (!inTag && c == '<' && i + 1 < length
          && (Character.isLetter(text.charAt(i + 1)) || "/%!".indexOf(text.charAt(i + 1)) >= 0)) {
        inTag = true;
      } else if (inTag && c == '>') {
        inTag = false;
      }
      i++;
    }
    return result;
  }

  /**
   * every EL expression written as page text in the provisioning JSPs must be escaped: every
   * grouper_sync* column is untrusted (it can come from a group name, a subject source or the
   * target system)
   * @throws Exception
   */
  public void testSyncValuesAreEscapedInJsps() throws Exception {
    List<String> problems = new ArrayList<String>();
    for (String[] jspAndRequired : JSPS) {
      String repoPath = jspAndRequired[0];
      String jsp = new String(Files.readAllBytes(findJsp(repoPath).toPath()), StandardCharsets.UTF_8);

      for (String expression : textElExpressions(jsp)) {
        if (expression.startsWith("grouper:escapeHtml(") && expression.endsWith(")")) {
          continue;
        }
        // admin-configured externalized text, same as every other UI screen
        if (expression.startsWith("textContainer.")) {
          continue;
        }
        // page title from an externalized text key (6.3.0+ / 7.3.0+)
        if (expression.startsWith("grouper:title(")) {
          continue;
        }
        problems.add(repoPath + ": ${" + expression + "}");
      }

      // not vacuous: the reported sink is there, escaped
      if (!jsp.contains(jspAndRequired[1])) {
        problems.add(repoPath + ": missing " + jspAndRequired[1]);
      }
    }
    assertTrue("Unescaped EL output: " + problems, problems.isEmpty());
  }

  /**
   * the activity entity table printed the group bean (always empty there) instead of the
   * member's entity cache 1
   * @throws Exception
   */
  public void testActivityMemberCache1ReadsMember() throws Exception {
    String repoPath = UI2 + "provisionerConfigs/provisioningConfigActivityMember.jsp";
    String jsp = new String(Files.readAllBytes(findJsp(repoPath).toPath()), StandardCharsets.UTF_8);
    assertFalse(jsp.contains("grouperSyncGroup.entityAttributeValueCache1"));
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld neutralizes the reproduction
   * payload, encodes each character once, and leaves ordinary values alone
   */
  public void testEscapeHtmlFunction() {
    String payload = "securityReviewProvXss:REVIEW_PROVXSS<img src=x onerror=alert(document.domain)>";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));
    assertTrue(escaped, escaped.contains("&lt;img src=x onerror=alert(document.domain)&gt;"));

    assertEquals("AT&amp;T", GrouperUiFunctions.escapeHtml("AT&T"));
    assertEquals("test:provisioning:group1", GrouperUiFunctions.escapeHtml("test:provisioning:group1"));
    assertEquals("2026-11-08 10:15:00.0", GrouperUiFunctions.escapeHtml("2026-11-08 10:15:00.0"));
  }

}
