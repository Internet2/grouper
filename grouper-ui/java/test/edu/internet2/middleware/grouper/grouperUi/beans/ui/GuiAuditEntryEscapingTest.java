package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.ui.tags.GrouperUiFunctions;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Stored XSS in the UI audit pages (advisory H6): the five audit JSPs printed the audit
 * description, server host, user IP, id, query count, server user and engine raw, and
 * GuiAuditEntry handed the import/export file name, permission action and deleted (point in
 * time) group name raw to the audits_* text templates, which print them as markup.
 *
 * There is no JSP rendering harness in grouper-ui tests, so the JSP part is a static source
 * check (every audit row value must go through grouper:escapeHtml). The bean part calls the
 * getters directly. No database or Grouper config is needed.
 */
public class GuiAuditEntryEscapingTest extends TestCase {

  /** proof payload, as an attacker would store it */
  private static final String PAYLOAD = "<img src=x onerror=\"alert('AUDIT_XSS_PROOF')\">";

  /** repo paths of the audit JSPs under test */
  private static final String[] JSP_REPO_PATHS = {
      "grouper-ui/webapp/WEB-INF/grouperUi2/group/groupViewAuditsContents.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemViewAuditsContents.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectViewAuditsContents.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/group/groupAttestationViewAuditsContents.jsp",
      "grouper-ui/webapp/WEB-INF/grouperUi2/subjectResolution/subjectDeleteAuditsContents.jsp"
  };

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GuiAuditEntryEscapingTest.class);
  }

  /**
   *
   */
  public GuiAuditEntryEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiAuditEntryEscapingTest(String name) {
    super(name);
  }

  /**
   * find a JSP whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ). System property auditJspDir (the
   * grouper-ui/webapp/WEB-INF/grouperUi2 directory) overrides.
   * @param repoPath
   * @return the JSP file
   */
  private static File findJsp(String repoPath) {
    String marker = "WEB-INF/grouperUi2/";
    String override = System.getProperty("auditJspDir");
    if (override != null && override.length() > 0) {
      return new File(override, repoPath.substring(repoPath.indexOf(marker) + marker.length()));
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
        + ", set -DauditJspDir=<path to grouper-ui/webapp/WEB-INF/grouperUi2>");
    return null;
  }

  /**
   * every output of an audit entry field (and the engine label) in the five audit JSPs must be
   * directly inside grouper:escapeHtml(...), and the description must still be shown.
   * Fails before the fix.
   * @throws Exception
   */
  public void testAuditRowValuesAreEscapedInJsps() throws Exception {

    // ${auditEntry.x} (the c:set var) or ${guiAuditEntry.grouperEngineLabel}; not
    // ${guiAuditEntry.auditEntry} (that is the c:set source, not output)
    Pattern pattern = Pattern.compile("(?<![\\w.])(auditEntry\\.\\w+|guiAuditEntry\\.grouperEngineLabel)");
    String escapePrefix = "grouper:escapeHtml(";

    List<String> rawUses = new ArrayList<String>();
    for (String repoPath : JSP_REPO_PATHS) {
      String jsp = new String(Files.readAllBytes(findJsp(repoPath).toPath()), StandardCharsets.UTF_8);
      Matcher matcher = pattern.matcher(jsp);
      int uses = 0;
      boolean descriptionEscaped = false;
      while (matcher.find()) {
        uses++;
        String before = jsp.substring(Math.max(0, matcher.start() - escapePrefix.length()), matcher.start());
        if (!escapePrefix.equals(before)) {
          int line = jsp.substring(0, matcher.start()).split("\n", -1).length;
          rawUses.add(repoPath + ":" + line + " " + matcher.group(1));
        } else if ("auditEntry.description".equals(matcher.group(1))) {
          descriptionEscaped = true;
        }
      }
      // the row must still be shown, else this test is checking nothing
      assertTrue("expected audit fields in " + repoPath, uses > 0);
      assertTrue("expected escaped description in " + repoPath, descriptionEscaped);
    }
    assertTrue("Unescaped audit values: " + rawUses, rawUses.isEmpty());
  }

  /**
   * the function mapped to grouper:escapeHtml in grouper-el.tld must neutralize markup
   */
  public void testEscapeHtmlFunction() {
    String escaped = GrouperUiFunctions.escapeHtml(PAYLOAD);
    assertTrue(escaped, escaped.startsWith("&lt;img"));
    assertFalse(escaped, escaped.contains("<"));
    assertFalse(escaped, escaped.contains("\""));
    // ordinary values are unchanged
    assertEquals("10.1.2.3", GrouperUiFunctions.escapeHtml("10.1.2.3"));
  }

  /**
   * the values GuiAuditEntry gives the audits_* text templates must be escaped.
   * Fails before the fix.
   * @throws Exception
   */
  public void testBeanValuesForTextTemplatesAreEscaped() throws Exception {
    GuiAuditEntry guiAuditEntry = new GuiAuditEntry();

    // permission action (audits_ATTRIBUTE_ASSIGN_*ROLE*)
    guiAuditEntry.setAction(PAYLOAD);
    assertEscaped("action", guiAuditEntry.getAction());

    // import / export file name (audits_MEMBERSHIP_GROUP_IMPORT / EXPORT), set by a private
    // setup method from the audit entry, so set the field directly
    Field fileField = GuiAuditEntry.class.getDeclaredField("file");
    fileField.setAccessible(true);
    fileField.set(guiAuditEntry, PAYLOAD + ".csv");
    assertEscaped("file", guiAuditEntry.getFile());

    // deleted group, name from point in time.  GuiPITGroup / getGroupLinkOrName are not in every
    // release (e.g. 2.6.19 has neither, so nothing to escape there): reached by reflection so this
    // one file compiles on every release
    Class<?> guiPitGroupClass = null;
    try {
      guiPitGroupClass = Class.forName("edu.internet2.middleware.grouper.grouperUi.beans.api.GuiPITGroup");
      GuiAuditEntry.class.getMethod("getGroupLinkOrName");
    } catch (ClassNotFoundException cnfe) {
      guiPitGroupClass = null;
    } catch (NoSuchMethodException nsme) {
      guiPitGroupClass = null;
    }
    if (guiPitGroupClass != null) {
      PITGroup pitGroup = new PITGroup();
      pitGroup.setNameDb("a:" + PAYLOAD);
      Object guiPitGroup = guiPitGroupClass.getConstructor(PITGroup.class).newInstance(pitGroup);
      GuiAuditEntry.class.getMethod("setGuiPITGroup", guiPitGroupClass).invoke(guiAuditEntry, guiPitGroup);
      assertEscaped("groupLinkOrName", (String)GuiAuditEntry.class.getMethod("getGroupLinkOrName").invoke(guiAuditEntry));
    } else {
      System.out.println("GuiAuditEntryEscapingTest: no GuiPITGroup / getGroupLinkOrName in this release, skipped that value");
    }

    // ordinary values unchanged, null stays null
    guiAuditEntry.setAction("assign");
    assertEquals("assign", guiAuditEntry.getAction());
    fileField.set(guiAuditEntry, "members.csv");
    assertEquals("members.csv", guiAuditEntry.getFile());
    fileField.set(guiAuditEntry, null);
    assertNull(guiAuditEntry.getFile());
  }

  /**
   * @param label
   * @param value
   */
  private static void assertEscaped(String label, String value) {
    assertNotNull(label, value);
    assertTrue(label + ": " + value, value.contains("&lt;img"));
    assertFalse(label + ": " + value, value.contains("<"));
    assertFalse(label + ": " + value, value.contains("\""));
  }

}
