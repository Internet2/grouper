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
 * <p>Security issue grouperReportConfigurationStoredXss: the group and folder report screens
 * (WEB-INF/grouperUi2/grouperReport/*.jsp) printed stored report configuration values (name,
 * description, query, cron, file name, type, format) and report instance values (file name,
 * email error) raw, so a value with markup ran script for every viewer of the report screens.
 * After the fix every such output goes through grouper:escapeHtml (allReportConfigs.jsp and
 * the add/edit forms already escaped most of them).</p>
 *
 * <p>There is no JSP rendering harness in grouper-ui tests, so this is a static source check of
 * every JSP in the grouperReport folder.  No database or Grouper config is needed.  Rendering is
 * in the manual test steps.</p>
 */
public class GrouperReportJspEscapingTest extends TestCase {

  /** repo path of the folder under test */
  private static final String DIR_REPO_PATH = "grouper-ui/webapp/WEB-INF/grouperUi2/grouperReport";

  /** stored string values of a report config or report instance */
  private static final String FIELDS = "reportConfigName|reportConfigDescription|reportConfigQuery|reportConfigQuartzCron|"
      + "reportConfigFilename|reportConfigType|reportConfigFormat|reportInstanceFileName|"
      + "reportInstanceEmailToSubjectsError|userFriendlyCron";

  /** bean names that hold a stored report config or instance */
  private static final String BEANS = "reportConfigBean|configBean|reportInstance|mostRecentReportInstance|"
      + "guiReportConfig|guiReportInstance|guiReportConfigOverall";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperReportJspEscapingTest.class);
  }

  /**
   *
   */
  public GrouperReportJspEscapingTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperReportJspEscapingTest(String name) {
    super(name);
  }

  /**
   * find the folder whether the test runs from the repo root, the grouper-ui project, or a sibling
   * project (Eclipse working directories differ).  System property grouperReportJspDir overrides.
   * @return the folder
   */
  private static File findDir() {
    String override = System.getProperty("grouperReportJspDir");
    if (override != null && override.length() > 0) {
      return new File(override);
    }
    String withoutProject = DIR_REPO_PATH.substring("grouper-ui/".length());
    String[] candidates = {
        DIR_REPO_PATH,
        withoutProject,
        "../" + DIR_REPO_PATH,
        "../grouper-ui/" + withoutProject
    };
    for (String candidate : candidates) {
      File file = new File(candidate);
      if (file.isDirectory()) {
        return file;
      }
    }
    fail("Cannot find " + DIR_REPO_PATH + " from working dir " + new File(".").getAbsolutePath()
        + ", set -DgrouperReportJspDir=<path to grouper-ui/webapp/WEB-INF/grouperUi2/grouperReport>");
    return null;
  }

  /**
   * every whole-EL output of a stored report value must be ${grouper:escapeHtml(...)}, and the
   * report name must still be shown escaped on the group and folder report lists.
   * Fails before the fix.
   * @throws Exception
   */
  public void testReportValuesAreEscaped() throws Exception {
    // ${a.b.reportConfigBean.reportConfigName} as a whole EL; comparisons and tests are not
    // outputs and do not match (the EL must be just the bean path)
    Pattern raw = Pattern.compile("\\$\\{\\s*((?:[A-Za-z_]\\w*\\.)*(?:" + BEANS + ")\\.(?:" + FIELDS + "))\\s*\\}");
    List<String> rawUses = new ArrayList<String>();
    File dir = findDir();
    File[] jsps = dir.listFiles();
    assertNotNull(jsps);
    int jspCount = 0;
    for (File jsp : jsps) {
      if (!jsp.getName().endsWith(".jsp")) {
        continue;
      }
      jspCount++;
      String text = new String(Files.readAllBytes(jsp.toPath()), StandardCharsets.UTF_8);
      Matcher matcher = raw.matcher(text);
      while (matcher.find()) {
        int line = text.substring(0, matcher.start()).split("\n", -1).length;
        rawUses.add(jsp.getName() + ":" + line + " " + matcher.group(1));
      }
    }
    assertTrue("expected the report JSPs in " + dir.getAbsolutePath(), jspCount >= 10);
    assertTrue("Unescaped report values: " + rawUses, rawUses.isEmpty());

    // the list pages still show the name, escaped
    for (String name : new String[] {"groupReportConfig.jsp", "folderReportConfig.jsp"}) {
      String text = new String(Files.readAllBytes(new File(dir, name).toPath()), StandardCharsets.UTF_8);
      assertTrue(name, text.contains("${grouper:escapeHtml(guiReportConfig.reportConfigBean.reportConfigName)}"));
    }
  }
}
