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
 * Security advisory H6 (visualizationObjectIdReflectedXss): the visualization screen
 * (visualization/visualizationMain.jsp) echoed the raw objectId request parameter into a
 * double-quoted hidden input value.  For objectType=subject only the first two "|" separated
 * parts are looked up, so a crafted
 * UiV2Main.index?operation=UiV2Visualization.subjectView link with
 * objectId=g:isa||||GrouperSystem||||&lt;payload&gt; resolved and ran script in any logged-in
 * user's UI session.
 *
 * <p>There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP: every EL output of visualizationContainer.objectId must go through fn:escapeXml (what
 * upstream used in 7.8.0) or grouper:escapeHtml.  Only this value is checked (the issue and the
 * upstream commit cover only objectId; operation and objectType are not reachable with a
 * payload, see the security report).  VisualizationObjectIdBrowserTest checks the rendered page in
 * the running UI.  No database or Grouper config is needed.  Same file for every release
 * (Java 8 compatible).</p>
 */
public class VisualizationMainJspEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/visualization/visualizationMain.jsp";

  /** the fixed output (so the check is not vacuous) */
  private static final String REQUIRED_ESCAPED =
      "value=\"${fn:escapeXml(grouperRequestContainer.visualizationContainer.objectId)}\"";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(VisualizationMainJspEscapingTest.class);
  }

  /**
   *
   */
  public VisualizationMainJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public VisualizationMainJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property visualizationMainJsp
   * overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("visualizationMainJsp");
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
        + ", set -DvisualizationMainJsp=<path>");
    return null;
  }

  /**
   * every EL expression that writes visualizationContainer.objectId to the page must be escaped.
   * JSP comments and JSTL tag attributes (c:set value, c:forEach items, c:if / c:when test) are
   * removed first, since they are not output.
   * @throws Exception
   */
  public void testObjectIdEscapedInJsp() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    // JSP comments are not output
    String text = jsp.replaceAll("(?s)<%--.*?--%>", "");

    // EL in JSTL tag attributes is evaluated, not output
    text = text.replaceAll("(?s)(<c:[a-zA-Z]+[^>]*?)\\b(value|items|test)\\s*=\\s*\"\\$\\{[^}]*\\}\"", "$1$2=\"\"");

    List<String> rawUses = new ArrayList<String>();
    Matcher matcher = Pattern.compile("\\$\\{([^}]*)\\}").matcher(text);
    while (matcher.find()) {
      String expression = matcher.group(1).trim();
      if (!expression.matches("(?s).*visualizationContainer\\.objectId\\b.*")) {
        continue;
      }
      if ((expression.startsWith("fn:escapeXml(") || expression.startsWith("grouper:escapeHtml("))
          && expression.endsWith(")")) {
        continue;
      }
      rawUses.add("${" + expression + "}");
    }
    assertTrue("Unescaped objectId output in " + JSP_REPO_PATH + ": " + rawUses, rawUses.isEmpty());

    assertTrue("expected " + REQUIRED_ESCAPED + " in the JSP", jsp.contains(REQUIRED_ESCAPED));
  }

}
