/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (viewAuditsAuditTypeReflectedXss): the group and subject audit screens
 * (group/groupViewAudits.jsp, subject/subjectViewAudits.jsp) echoed the raw auditType request
 * parameter into a double-quoted hidden input value, so a crafted
 * UiV2Main.index?operation=UiV2Group.viewAudits (or UiV2Subject.viewAudits) link closed the
 * attribute and ran script in the victim's UI session.  Upstream fixed it in GRP-7374 (7.7.0).
 *
 * <p>There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the two JSPs: every EL output of auditType must go through fn:escapeXml (what upstream used)
 * or grouper:escapeHtml.  Only this value is checked: the rest of each file is outside this
 * issue.  ViewAuditsAuditTypeBrowserTest checks the rendered pages in the running UI.  No
 * database or Grouper config is needed.  Same file for every release (Java 8 compatible).</p>
 */
public class ViewAuditsJspEscapingTest extends TestCase {

  /** repo path of the group audit JSP */
  private static final String GROUP_JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/group/groupViewAudits.jsp";

  /** repo path of the subject audit JSP */
  private static final String SUBJECT_JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectViewAudits.jsp";

  /** the fixed group output (so the check is not vacuous) */
  private static final String GROUP_REQUIRED_ESCAPED =
      "value=\"${fn:escapeXml(grouperRequestContainer.groupContainer.auditType)}\"";

  /** the fixed subject output (so the check is not vacuous) */
  private static final String SUBJECT_REQUIRED_ESCAPED =
      "value=\"${fn:escapeXml(grouperRequestContainer.subjectContainer.auditType)}\"";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ViewAuditsJspEscapingTest.class);
  }

  /**
   *
   */
  public ViewAuditsJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public ViewAuditsJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find a JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  The system property overrides.
   * @param repoPath repo-relative path of the JSP
   * @param overrideProperty system property that can point at the file
   * @return the JSP file
   */
  private static File findJsp(String repoPath, String overrideProperty) {
    String override = System.getProperty(overrideProperty);
    if (override != null && override.length() > 0) {
      return new File(override);
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
        + ", set -D" + overrideProperty + "=<path>");
    return null;
  }

  /**
   * every EL expression that writes auditType to the page must be escaped.  JSP comments and
   * JSTL tag attributes (c:set value, c:forEach items, c:if / c:when test) are removed first,
   * since they are not output.
   * @param repoPath repo-relative path of the JSP
   * @param overrideProperty system property that can point at the file
   * @param requiredEscaped the escaped output that must be present
   * @throws Exception
   */
  private static void assertAuditTypeEscaped(String repoPath, String overrideProperty,
      String requiredEscaped) throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp(repoPath, overrideProperty).toPath()), StandardCharsets.UTF_8);

    // JSP comments are not output
    String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

    // EL in JSTL tag attributes is evaluated, not output
    text = text.replaceAll("(?s)(<c:[a-zA-Z]+[^>]*?)\\b(value|items|test)\\s*=\\s*\"\\$\\{[^}]*\\}\"", "$1$2=\"\"");

    List<String> rawUses = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
    while (matcher.find()) {
      String expression = matcher.group(1).trim();
      // the container property (groupContainer.auditType / subjectContainer.auditType)
      if (!expression.contains(".auditType")) {
        continue;
      }
      if ((expression.startsWith("fn:escapeXml(") || expression.startsWith("grouper:escapeHtml("))
          && expression.endsWith(")")) {
        continue;
      }
      rawUses.add("${" + expression + "}");
    }
    assertTrue("Unescaped auditType output in " + repoPath + ": " + rawUses, rawUses.isEmpty());

    assertTrue("expected " + requiredEscaped + " in " + repoPath, jsp.contains(requiredEscaped));
  }

  /**
   * group audit screen
   * @throws Exception
   */
  public void testGroupViewAuditsAuditTypeEscaped() throws Exception {
    assertAuditTypeEscaped(GROUP_JSP_REPO_PATH, "groupViewAuditsJsp", GROUP_REQUIRED_ESCAPED);
  }

  /**
   * subject audit screen
   * @throws Exception
   */
  public void testSubjectViewAuditsAuditTypeEscaped() throws Exception {
    assertAuditTypeEscaped(SUBJECT_JSP_REPO_PATH, "subjectViewAuditsJsp", SUBJECT_REQUIRED_ESCAPED);
  }

}
