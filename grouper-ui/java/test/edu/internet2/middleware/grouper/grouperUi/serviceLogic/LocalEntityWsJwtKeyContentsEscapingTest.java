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
 * Stored XSS in the local entity WS JWT key detail page (localEntityWsJwtKeyContents.jsp):
 * an ADMIN of one local entity could store HTML in the key's "Allowed from networks" value
 * (allowedFromCidrs), and the detail page printed it raw to any later viewer (e.g. wheel).
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP (every output of allowedFromCidrs must go through grouper:escapeHtml) plus a check
 * that the function behind grouper:escapeHtml escapes markup and leaves CIDR lists alone.
 * No database or Grouper config is needed.
 */
public class LocalEntityWsJwtKeyContentsEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/localEntity/localEntityWsJwtKeyContents.jsp";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(LocalEntityWsJwtKeyContentsEscapingTest.class);
  }

  /**
   *
   */
  public LocalEntityWsJwtKeyContentsEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public LocalEntityWsJwtKeyContentsEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property
   * localEntityJspDir (the directory holding the JSP) overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("localEntityJspDir");
    if (override != null && override.length() > 0) {
      return new File(override, new File(JSP_REPO_PATH).getName());
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
        + ", set -DlocalEntityJspDir=<dir>");
    return null;
  }

  /**
   * every output use of allowedFromCidrs must be inside grouper:escapeHtml(...).
   * Uses that are only null checks (field followed by == or !=) are not output.
   * @throws Exception
   */
  public void testAllowedFromCidrsEscaped() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    // match the whole reference, and what comes right after it, to skip null checks
    Pattern pattern = Pattern.compile("guiGrouperPassword\\.grouperPassword\\.allowedFromCidrs\\b(\\s*[!=]=)?");
    Matcher matcher = pattern.matcher(jsp);
    List<String> rawUses = new ArrayList<String>();
    int outputUses = 0;
    while (matcher.find()) {
      if (matcher.group(1) != null) {
        // null check, not output
        continue;
      }
      outputUses++;
      String before = jsp.substring(Math.max(0, matcher.start() - "grouper:escapeHtml(".length()),
          matcher.start());
      if (!"grouper:escapeHtml(".equals(before)) {
        int line = jsp.substring(0, matcher.start()).split("\n", -1).length;
        rawUses.add("allowedFromCidrs at line " + line);
      }
    }
    // the value must still be shown, else this test is checking nothing
    assertTrue("expected allowedFromCidrs to be output in the JSP", outputUses > 0);
    assertTrue("Unescaped allowedFromCidrs in " + JSP_REPO_PATH + ": " + rawUses,
        rawUses.isEmpty());
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize markup,
   * and must not change a normal CIDR list
   */
  public void testEscapeHtmlPayload() {
    String payload = "<img src=x onerror=alert(1)>";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));

    // ordinary IPv4 / IPv6 / CIDR lists are unchanged
    assertEquals("10.0.0.0/8, 192.168.1.5", GrouperUiFunctions.escapeHtml("10.0.0.0/8, 192.168.1.5"));
    assertEquals("2001:db8::/32", GrouperUiFunctions.escapeHtml("2001:db8::/32"));
  }

}
