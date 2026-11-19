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
 * Reflected XSS on the provisioner "View errors" screen (provisionerConfigViewErrors.jsp): the
 * group and entity filter request parameters were echoed raw into double-quoted value
 * attributes, so a crafted UiV2Main.index?operation=UiV2ProvisionerConfiguration.viewProvisionerErrorsSubmit
 * link closed the attribute and ran script in a provisioner viewer's UI session.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP (every EL expression that is written to the page must go through grouper:escapeHtml,
 * except externalized text labels, the page title helper and selected="selected" ternaries)
 * plus a check that the function behind grouper:escapeHtml neutralizes the reproduction
 * payload. No database or Grouper config is needed.
 */
public class ProvisionerConfigViewErrorsJspEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/provisionerConfigs/provisionerConfigViewErrors.jsp";

  /** escaped outputs that must be present, else the test would be checking nothing */
  private static final String[] REQUIRED_ESCAPED = {
      "value=\"${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorGroup)}\"",
      "value=\"${grouper:escapeHtml(grouperRequestContainer.provisionerConfigurationContainer.provisionerConfigErrorEntity)}\""
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ProvisionerConfigViewErrorsJspEscapingTest.class);
  }

  /**
   *
   */
  public ProvisionerConfigViewErrorsJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public ProvisionerConfigViewErrorsJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property
   * provisionerConfigViewErrorsJsp overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("provisionerConfigViewErrorsJsp");
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
        + ", set -DprovisionerConfigViewErrorsJsp=<path>");
    return null;
  }

  /**
   * every EL expression written to the page (text or HTML attribute) must be escaped.  Unlike
   * a text-only page, HTML attribute values are output here (the reported sinks are
   * value="${...}"), so only JSTL tag attributes (c:set value, c:forEach items, c:if / c:when
   * test) are removed before the check.
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
      if (expression.startsWith("grouper:escapeHtml(") && expression.endsWith(")")) {
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
      // x == y ? 'selected="selected"' : '' only ever outputs a constant
      if (expression.matches("(?s).*\\?\\s*'selected=\"selected\"'\\s*:\\s*''\\s*")) {
        continue;
      }
      rawUses.add("${" + expression + "}");
    }
    assertTrue("Unescaped EL output in " + JSP_REPO_PATH + ": " + rawUses, rawUses.isEmpty());

    for (String required : REQUIRED_ESCAPED) {
      assertTrue("expected " + required + " in the JSP", jsp.contains(required));
    }
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must keep the reproduction
   * payload inside the value attribute and encode each character exactly once
   */
  public void testEscapeHtmlFunction() {
    String payload = "\"><img src=x onerror=alert(1)>";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("&quot;&gt;&lt;img"));
    assertFalse(escaped, escaped.contains("\""));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));

    // ampersand encoded once, ordinary filter values unchanged
    assertEquals("AT&amp;T", GrouperUiFunctions.escapeHtml("AT&T"));
    assertEquals("test:provisioning:group1", GrouperUiFunctions.escapeHtml("test:provisioning:group1"));
    assertEquals("jsmith 12345", GrouperUiFunctions.escapeHtml("jsmith 12345"));
  }

}
