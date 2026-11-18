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
 * Security advisory H6 (daemonJobHistoryFilterReflectedXss): the daemon job history chart screen
 * (admin/adminJobHistoryChart.jsp) echoed the namesLikeFilter, date, time and minimum elapsed
 * seconds request parameters raw into double-quoted value attributes, so a crafted
 * UiV2Main.index?operation=UiV2Admin.jobHistoryChart link closed the attribute and ran script
 * in a loader viewer's UI session.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of the
 * JSP: every EL expression written to the page must go through fn:escapeXml (what upstream used
 * in 7.8.0) or grouper:escapeHtml, except externalized text labels and the page title helper.
 * AdminJobHistoryChartBrowserTest checks the rendered page in the running UI.  No database or
 * Grouper config is needed.  Same file for every release (Java 8 compatible).
 */
public class AdminJobHistoryChartJspEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/admin/adminJobHistoryChart.jsp";

  /** the six request-derived inputs, which must all be escaped (so the check is not vacuous) */
  private static final String[] REQUIRED_ESCAPED = {
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryDateFrom)}\"",
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryTimeFrom)}\"",
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryDateTo)}\"",
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryTimeTo)}\"",
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryMinimumElapsedSeconds)}\"",
      "value=\"${fn:escapeXml(grouperRequestContainer.adminContainer.guiJobHistoryNamesLikeFilter)}\""
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AdminJobHistoryChartJspEscapingTest.class);
  }

  /**
   *
   */
  public AdminJobHistoryChartJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public AdminJobHistoryChartJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property adminJobHistoryChartJsp
   * overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("adminJobHistoryChartJsp");
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
        + ", set -DadminJobHistoryChartJsp=<path>");
    return null;
  }

  /**
   * every EL expression written to the page (text or HTML attribute) must be escaped.  Only
   * JSTL tag attributes (c:set value, c:forEach items, c:if / c:when test) are removed before
   * the check, since they are evaluated, not output.
   * @throws Exception
   */
  public void testDynamicValuesAreEscapedInJsp() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    // JSP comments are not output
    String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

    // EL in JSTL tag attributes is evaluated, not output
    text = text.replaceAll("(?s)(<c:[a-zA-Z]+[^>]*?)\\b(value|items|test)\\s*=\\s*\"\\$\\{[^}]*\\}\"", "$1$2=\"\"");

    List<String> rawUses = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
    while (matcher.find()) {
      String expression = matcher.group(1).trim();
      if ((expression.startsWith("fn:escapeXml(") || expression.startsWith("grouper:escapeHtml("))
          && expression.endsWith(")")) {
        continue;
      }
      // admin-configured externalized text, same as every other UI screen
      if (expression.startsWith("textContainer.text[") || expression.startsWith("textContainer.textEscapeXml[")) {
        continue;
      }
      // page title from an externalized text key (6.3.0+ / 7.3.0+)
      if (expression.startsWith("grouper:title('")) {
        continue;
      }
      rawUses.add("${" + expression + "}");
    }
    assertTrue("Unescaped EL output in " + JSP_REPO_PATH + ": " + rawUses, rawUses.isEmpty());

    for (String required : REQUIRED_ESCAPED) {
      assertTrue("expected " + required + " in the JSP", jsp.contains(required));
    }
  }

}
