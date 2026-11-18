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
 * <p>Security issue externalInviteSuccessMessageStoredXss: when an external person registers
 * from an invite, UiV2ExternalSubjectSelfRegister.processInvitedPerson put the display
 * extensions of the invite's groups and the inviter's subject name into the success message
 * unescaped, and the message is shown as HTML (GuiScreenAction.newMessage).  A group admin who
 * invites to a group whose display name holds markup ran script in the browser of whoever
 * accepted the invite.  The same raw substitution was used for the identifier in the other
 * self-registration messages.  After the fix every substituted value goes through
 * escapeForMessage.</p>
 *
 * <p>The invite flow needs external subjects, self-registration and a stored invite, so the
 * wiring is checked in the source (every {0} / {1} substitution of a value, and the roles list,
 * must go through escapeForMessage) and the helper is called directly.  No database or Grouper
 * config is needed.  Hand steps for the whole flow are in the manual test steps.</p>
 */
public class UiV2ExternalSubjectSelfRegisterMessageEscapingTest extends TestCase {

  /** proof payload, as a group admin would store it in a display name */
  private static final String PAYLOAD = "Staff<img id=sec170xss src=x onerror=\"alert('170')\">";

  /** repo path of the source under test */
  private static final String SOURCE_REPO_PATH = "grouper-ui/java/src/edu/internet2/middleware/grouper/grouperUi/serviceLogic/UiV2ExternalSubjectSelfRegister.java";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2ExternalSubjectSelfRegisterMessageEscapingTest.class);
  }

  /**
   *
   */
  public UiV2ExternalSubjectSelfRegisterMessageEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ExternalSubjectSelfRegisterMessageEscapingTest(String name) {
    super(name);
  }

  /**
   * find the source whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property externalRegisterSourceFile
   * overrides.
   * @return the source file
   */
  private static File findSource() {
    String override = System.getProperty("externalRegisterSourceFile");
    if (override != null && override.length() > 0) {
      return new File(override);
    }
    String withoutProject = SOURCE_REPO_PATH.substring("grouper-ui/".length());
    String[] candidates = {
        SOURCE_REPO_PATH,
        withoutProject,
        "../" + SOURCE_REPO_PATH,
        "../grouper-ui/" + withoutProject
    };
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + SOURCE_REPO_PATH + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DexternalRegisterSourceFile=<path to UiV2ExternalSubjectSelfRegister.java>");
    return null;
  }

  /**
   * the helper must neutralize markup and keep ordinary values and null
   */
  public void testEscapeForMessage() {
    String escaped = UiV2ExternalSubjectSelfRegister.escapeForMessage(PAYLOAD);
    assertTrue(escaped, escaped.startsWith("Staff&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains("\""));
    assertEquals("Project Staff", UiV2ExternalSubjectSelfRegister.escapeForMessage("Project Staff"));
    assertEquals("jsmith@example.edu", UiV2ExternalSubjectSelfRegister.escapeForMessage("jsmith@example.edu"));
    assertNull(UiV2ExternalSubjectSelfRegister.escapeForMessage(null));
  }

  /**
   * every message substitution of a value in UiV2ExternalSubjectSelfRegister must be escaped, and
   * the group display extensions in the roles list too.  Fails before the fix.
   * @throws Exception
   */
  public void testMessageSubstitutionsAreEscaped() throws Exception {
    String source = new String(Files.readAllBytes(findSource().toPath()), StandardCharsets.UTF_8);

    // StringUtils.replace(x, "{0}", value) / "{1}": the value must be escapeForMessage(...), except
    // the roles list (built escaped) and a register field label (externalized text / config)
    Pattern pattern = Pattern.compile("StringUtils\\.replace\\(\\s*\\w+\\s*,\\s*\"\\{[01]\\}\"\\s*,\\s*([^;]*)\\)\\s*;");
    Matcher matcher = pattern.matcher(source);
    List<String> raw = new ArrayList<String>();
    int escapedCount = 0;
    while (matcher.find()) {
      String value = matcher.group(1).trim();
      if (value.startsWith("escapeForMessage(")) {
        escapedCount++;
        continue;
      }
      if (value.equals("allRoles.toString()") || value.equals("registerField.getLabel()")) {
        continue;
      }
      int line = source.substring(0, matcher.start()).split("\n", -1).length;
      raw.add("line " + line + ": " + value);
    }
    assertTrue("Unescaped message values: " + raw, raw.isEmpty());
    // identifier (5), inviter name (3): the substitutions are still there
    assertTrue("expected escaped substitutions, found " + escapedCount, escapedCount >= 8);

    // the roles list
    assertFalse("raw display extension in the roles list", source.contains("allRoles.append(group.getDisplayExtension())"));
    assertTrue("escaped display extension in the roles list", source.contains("allRoles.append(escapeForMessage(group.getDisplayExtension()))"));
  }
}
