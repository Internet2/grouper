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
 * Stored XSS in the overall / folder-subtree attestation settings table
 * (groupAttestationListOfSettingsHelper.jsp): a group updater or folder admin could store
 * HTML in the custom attestation email addresses, and the shared settings table printed it raw
 * to a wheel user.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * the JSP (every stored string attribute rendered in the table must go through
 * grouper:escapeHtml) plus a check that the function behind grouper:escapeHtml escapes markup.
 * No database or Grouper config is needed.
 */
public class AttestationListOfSettingsHelperEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/group/groupAttestationListOfSettingsHelper.jsp";

  /** stored string attributes printed in the settings table (user controlled on save) */
  private static final String[] STRING_FIELDS = {
      "grouperAttestationEmailAddresses",
      "grouperAttestationDaysUntilRecertify",
      "grouperAttestationStemScope"
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AttestationListOfSettingsHelperEscapingTest.class);
  }

  /**
   *
   */
  public AttestationListOfSettingsHelperEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public AttestationListOfSettingsHelperEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property
   * attestationHelperJsp overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("attestationHelperJsp");
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
        + ", set -DattestationHelperJsp=<path>");
    return null;
  }

  /**
   * every output use of a stored string field must be inside grouper:escapeHtml(...).
   * Uses that are only null checks (field followed by == or !=) are not output.
   * @throws Exception
   */
  public void testStoredStringsAreEscapedInJsp() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    List<String> rawUses = new ArrayList<String>();
    for (String field : STRING_FIELDS) {

      // match the whole reference, and what comes right after it, to skip null checks
      Pattern pattern = Pattern.compile("guiAttestation\\." + field + "\\b(\\s*[!=]=)?");
      Matcher matcher = pattern.matcher(jsp);
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
          rawUses.add(field + " at line " + line);
        }
      }
      // the field must still be shown, else this test is checking nothing
      assertTrue("expected " + field + " to be output in the JSP", outputUses > 0);
    }
    assertTrue("Unescaped stored attestation values in " + JSP_REPO_PATH + ": " + rawUses,
        rawUses.isEmpty());
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize markup
   */
  public void testEscapeHtmlFunction() {
    String payload = "<img src=x onerror=\"alert('ATTESTATION_XSS_PROOF')\">";
    String escaped = GrouperUiFunctions.escapeHtml(payload);
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains(">"));
    assertFalse(escaped, escaped.contains("\""));

    // ordinary lists are unchanged
    assertEquals("a@example.edu, b@example.edu",
        GrouperUiFunctions.escapeHtml("a@example.edu, b@example.edu"));
  }

}
