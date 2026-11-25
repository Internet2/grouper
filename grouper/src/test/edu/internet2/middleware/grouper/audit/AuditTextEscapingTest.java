package edu.internet2.middleware.grouper.audit;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <p>SECURITY (GRP-7582): the audits_* text templates in the grouperText base files are HTML that
 * the UI audit screens print as is, with ${...} expressions filled in from the audit entry.  A
 * value someone else stored (a provisioner name, a GSH template config id, a report config id, a
 * subject string from an unresolvable subject deletion) must be escaped, or it can carry script
 * that runs in the browser of an admin viewing audits.</p>
 *
 * <p>Every ${...} in an audits_* key must either be inside grouperUtil.xmlEscape(...), or be one
 * of the GuiAuditEntry accessors that already escape (or build links from escaped text, or are
 * numbers).  A new audits_* key that prints a raw stored value fails this test.</p>
 *
 * <p>A static check of the text files; no database or Grouper config is needed.</p>
 */
public class AuditTextEscapingTest extends TestCase {

  /**
   * expressions that are safe without xmlEscape: GuiAuditEntry getters that escape, short links
   * built from escaped names, and numbers
   */
  private static final Set<String> SAFE_EXPRESSIONS = new HashSet<String>(Arrays.asList(
      "grouperRequestContainer.guiAuditEntry.action",
      "grouperRequestContainer.guiAuditEntry.attributeValue",
      "grouperRequestContainer.guiAuditEntry.file",
      "grouperRequestContainer.guiAuditEntry.exportSize",
      "grouperRequestContainer.guiAuditEntry.importTotalAdded",
      "grouperRequestContainer.guiAuditEntry.importTotalDeleted",
      "grouperRequestContainer.guiAuditEntry.guiAttributeDef.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiAttributeDefName.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiEntity.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiGroup.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiMember.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiMember.shortLinkWithIcon",
      "grouperRequestContainer.guiAuditEntry.guiPrivilege.shortLink",
      "grouperRequestContainer.guiAuditEntry.guiStem.shortLink"
      ));

  /** the text base files to check, repo-relative */
  private static final String[] TEXT_FILES = new String[] {
      "grouper/conf/grouperText/grouper.textNg.en.us.base.properties",
      "grouper/conf/grouperText/grouper.textNg.fr.fr.base.properties"
  };

  /** an expression; the body may contain parentheses and quotes but not a closing brace */
  private static final Pattern EXPRESSION = Pattern.compile("\\$\\{([^}]*)\\}");

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AuditTextEscapingTest.class);
  }

  /**
   *
   */
  public AuditTextEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public AuditTextEscapingTest(String name) {
    super(name);
  }

  /**
   * find a repo file whether the test runs from the repo root or a module (Eclipse working dirs differ)
   * @param repoPath
   * @return the file
   */
  private static File findRepoFile(String repoPath) {
    String withoutModule = repoPath.substring("grouper/".length());
    String[] candidates = {repoPath, withoutModule, "../" + repoPath, "../../" + repoPath};
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isFile()) {
        return file;
      }
    }
    fail("Cannot find " + repoPath + " from working dir " + new File(".").getAbsolutePath());
    return null;
  }

  /**
   * every expression in every audits_* key is escaped or known safe
   * @throws Exception
   */
  public void testAuditTextValuesAreEscaped() throws Exception {
    List<String> problems = new ArrayList<String>();
    int auditKeys = 0;
    for (String repoPath : TEXT_FILES) {
      List<String> lines = Files.readAllLines(findRepoFile(repoPath).toPath(), StandardCharsets.UTF_8);
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i);
        if (!line.startsWith("audits_")) {
          continue;
        }
        auditKeys++;
        Matcher matcher = EXPRESSION.matcher(line);
        while (matcher.find()) {
          String body = matcher.group(1).trim();
          if (body.startsWith("grouperUtil.xmlEscape(") || SAFE_EXPRESSIONS.contains(body)) {
            continue;
          }
          problems.add(repoPath + " line " + (i + 1) + ": ${" + body + "}");
        }
      }
    }
    assertTrue("expected audits_* keys, found " + auditKeys, auditKeys > 100);
    assertTrue("audit text values must be escaped with grouperUtil.xmlEscape: " + problems, problems.isEmpty());
  }

}
