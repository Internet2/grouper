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
 * Stored XSS through subject ids on the group Members page: groupContents.jsp put the member's
 * subject id raw into the href and the inline onclick JavaScript string of the row action links
 * (attribute assignments, provisioning, view group). A subject id from an upstream subject
 * source such as x"&gt;&lt;img src=x onerror=...&gt; became active markup for every user who
 * viewed the group's members.
 *
 * There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * groupContents.jsp (every subject id or source id in a link url must go through
 * grouper:escapeUrl, and every other output of them through an escape function) plus a check
 * that the function behind grouper:escapeUrl neutralizes the payload for both the href
 * attribute and the single quoted JavaScript string. No database or Grouper config is needed.
 */
public class GroupContentsSubjectIdEscapingTest extends TestCase {

  /** repo path of the JSP under test */
  private static final String JSP_REPO_PATH =
      "grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp";

  /** the reproduction payload from the report */
  private static final String PAYLOAD = "x\"><img src=x onerror=\"document.title='SID_IMG_XSS'\">";

  /** one EL expression, ${...} (EL in this JSP has no nested braces) */
  private static final Pattern EL_PATTERN = Pattern.compile("\\$\\{([^}]*)\\}");

  /** subject derived identifiers */
  private static final Pattern SUBJECT_ID_PATTERN = Pattern.compile(
      "\\bsubject\\.(id|sourceId)\\b");

  /** url encoded, safe in an href and in a single quoted JavaScript string */
  private static final Pattern URL_ESCAPED_PATTERN = Pattern.compile(
      "^\\s*grouper:escapeUrl\\(.*\\)\\s*$");

  /** encoded for an HTML attribute (e.g. the member checkbox data attribute) */
  private static final Pattern ANY_ESCAPED_PATTERN = Pattern.compile(
      "^\\s*(grouper:(escapeUrl|escapeHtml)|grouperUtil\\.xmlEscape)\\(.*\\)\\s*$");

  /** query string parameter right before an EL expression */
  private static final Pattern URL_PARAM_BEFORE_PATTERN = Pattern.compile("[A-Za-z]+=$");

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GroupContentsSubjectIdEscapingTest.class);
  }

  /**
   *
   */
  public GroupContentsSubjectIdEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public GroupContentsSubjectIdEscapingTest(String name) {
    super(name);
  }

  /**
   * find the JSP whether the test runs from the repo root, the grouper-ui project, or a
   * sibling project (Eclipse working directories differ). System property groupContentsJsp
   * (path to groupContents.jsp) overrides.
   * @return the JSP file
   */
  private static File findJsp() {
    String override = System.getProperty("groupContentsJsp");
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
        + ", set -DgroupContentsJsp=<path to groupContents.jsp>");
    return null;
  }

  /**
   * every subject id / source id in a link url (subjectId=, groupId=, sourceId=, ...) must be
   * wrapped in grouper:escapeUrl(...); any other output of them must be wrapped in an escape
   * function. Fails before the fix (6 raw uses in 7.9.0).
   * @throws Exception
   */
  public void testSubjectIdsAreUrlEscapedInGroupContents() throws Exception {
    String jsp = new String(Files.readAllBytes(findJsp().toPath()), StandardCharsets.UTF_8);

    List<String> rawUses = new ArrayList<String>();
    int urlUses = 0;
    Matcher matcher = EL_PATTERN.matcher(jsp);
    while (matcher.find()) {
      String expression = matcher.group(1);
      if (!SUBJECT_ID_PATTERN.matcher(expression).find()) {
        continue;
      }
      int lineStart = jsp.lastIndexOf('\n', matcher.start()) + 1;
      String lineBefore = jsp.substring(lineStart, matcher.start());

      // c:set value="${...}" is an assignment, not output
      if (lineBefore.contains("<c:set") && lineBefore.endsWith("value=\"")) {
        continue;
      }

      boolean inUrl = URL_PARAM_BEFORE_PATTERN.matcher(lineBefore).find();
      Pattern required = inUrl ? URL_ESCAPED_PATTERN : ANY_ESCAPED_PATTERN;
      if (inUrl) {
        urlUses++;
      }
      if (!required.matcher(expression).matches()) {
        int line = jsp.substring(0, matcher.start()).split("\n", -1).length;
        rawUses.add("line " + line + (inUrl ? " (url)" : "") + ": ${" + expression + "}");
      }
    }
    // the action links must still carry the subject id, else this test is checking nothing
    assertTrue("expected subject ids in link urls in " + JSP_REPO_PATH, urlUses > 0);
    assertTrue("Unescaped subject ids in groupContents.jsp: " + rawUses, rawUses.isEmpty());
  }

  /**
   * the function mapped to grouper:escapeUrl in grouper-el.tld must leave nothing that can end
   * the href attribute, open a tag, or end the single quoted JavaScript string
   */
  public void testEscapeUrlNeutralizesPayload() {
    String escaped = GrouperUiFunctions.escapeUrl(PAYLOAD);
    for (String bad : new String[] {"<", ">", "\"", "'", "\\", "\r", "\n", " "}) {
      assertFalse("escaped id contains [" + bad + "]: " + escaped, escaped.contains(bad));
    }
    assertEquals("a%27%5C%0D%0A%3C%2Fscript%3E",
        GrouperUiFunctions.escapeUrl("a'\\\r\n</script>"));

    // ordinary subject ids are unchanged
    assertEquals("test.subject.0", GrouperUiFunctions.escapeUrl("test.subject.0"));
    assertEquals("e88be02cabba4a94be4d86fb4b26c5a8",
        GrouperUiFunctions.escapeUrl("e88be02cabba4a94be4d86fb4b26c5a8"));
  }

}
