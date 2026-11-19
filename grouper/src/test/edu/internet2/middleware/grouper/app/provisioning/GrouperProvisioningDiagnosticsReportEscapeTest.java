package edu.internet2.middleware.grouper.app.provisioning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * The provisioner diagnostics report (GrouperProvisioningDiagnosticsContainer) is HTML that the
 * UI prints raw.  Values read from the target or produced by translation (target group names,
 * matching ids, membership values, object change old/new values) must be HTML escaped before they
 * go into it, or someone who controls a target-side name can run script in the session of the
 * Grouper admin who runs diagnostics.
 *
 * No database or provisioner is needed: the tests use the container directly, its private escape
 * helper by reflection, and (when the source tree is reachable from the working directory, as in
 * Eclipse) a scan of the container source for the known unescaped appends.
 *
 * Fails before the fix: the font-tag pass-through in appendReportLineIfNotBlank, the missing
 * escapeForReport helper, and the raw appends in the source.  Works on 2.6.19 through 7.x
 * (Java 8 source, TestCase base, no release-specific APIs).
 */
public class GrouperProvisioningDiagnosticsReportEscapeTest extends TestCase {

  /** markup a target-side name could carry */
  private static final String PAYLOAD = "<img src=x onerror=alert(1)>";

  /**
   * run from the IDE
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperProvisioningDiagnosticsReportEscapeTest.class);
  }

  /**
   * @param name
   */
  public GrouperProvisioningDiagnosticsReportEscapeTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------------------------------
  // appendReportLineIfNotBlank: lines that look like report markup are still escaped
  // ---------------------------------------------------------------------------------------

  /**
   * Callers pass object toString() and stack traces (which can hold target data).  Before the fix
   * a line starting with a font tag went into the report raw.
   * @throws Exception
   */
  public void testAppendReportLineEscapesFontTagLines() throws Exception {
    GrouperProvisioningDiagnosticsContainer container = new GrouperProvisioningDiagnosticsContainer();

    // inDiagnostics is only set while a diagnostics run is in progress; set it directly
    Field inDiagnostics = GrouperProvisioningDiagnosticsContainer.class.getDeclaredField("inDiagnostics");
    inDiagnostics.setAccessible(true);
    inDiagnostics.setBoolean(container, true);

    container.appendReportLineIfNotBlank("Error in group: name=x\n<font color='red'>" + PAYLOAD + "</font>\n");

    String report = container.getReportFinal();
    assertFalse("report must not contain raw markup: " + report, report.contains(PAYLOAD));
    assertFalse("report must not contain a raw font tag from the data: " + report, report.contains("<font color='red'><img"));
    assertTrue("escaped payload expected: " + report, report.contains("&lt;img src=x onerror=alert(1)&gt;"));
  }

  // ---------------------------------------------------------------------------------------
  // escapeForReport helper
  // ---------------------------------------------------------------------------------------

  /**
   * The helper every data-bearing append goes through.
   * @throws Exception
   */
  public void testEscapeForReportHelper() throws Exception {
    Method method;
    try {
      method = GrouperProvisioningDiagnosticsContainer.class.getDeclaredMethod("escapeForReport", Object.class);
    } catch (NoSuchMethodException nsme) {
      fail("GrouperProvisioningDiagnosticsContainer.escapeForReport(Object) is missing: the fix is not applied");
      return;
    }
    method.setAccessible(true);

    String escaped = (String)method.invoke(null, PAYLOAD);
    assertFalse(escaped, escaped.contains("<"));
    assertTrue(escaped, escaped.contains("&lt;img"));

    // like string concatenation, null prints as "null"
    assertEquals("null", method.invoke(null, new Object[] {null}));

    // collections (translated multi values) are escaped as a whole
    List<String> values = new ArrayList<String>();
    values.add(PAYLOAD);
    String escapedList = (String)method.invoke(null, values);
    assertFalse(escapedList, escapedList.contains("<img"));
  }

  // ---------------------------------------------------------------------------------------
  // source scan for the unescaped appends the fix wraps
  // ---------------------------------------------------------------------------------------

  /**
   * Scan the container source for the appends that put target or translated data in raw.  Skips
   * (with a printed line) when the source is not reachable from the working directory.
   * @throws Exception
   */
  public void testSourceHasNoRawDataAppends() throws Exception {
    String relative = "edu/internet2/middleware/grouper/app/provisioning/GrouperProvisioningDiagnosticsContainer.java";
    File sourceFile = null;
    for (String prefix : new String[] {"src/grouper/", "grouper/src/grouper/", "../grouper/src/grouper/"}) {
      File candidate = new File(prefix + relative);
      if (candidate.isFile()) {
        sourceFile = candidate;
        break;
      }
    }
    if (sourceFile == null) {
      System.out.println("skip testSourceHasNoRawDataAppends: source not found from " + new File(".").getAbsolutePath());
      return;
    }

    // each pattern is a raw append of a data value that the fix wraps in escapeForReport
    String[] rawPatterns = new String[] {
        "\" + value + \"",
        "\" + values + \"",
        "\" + provisioningObjectChange.getOldValue() + \"",
        "\" + provisioningObjectChange.getNewValue() + \"",
        "\" + provisioningObjectChange.getAttributeName() + \"",
        "\" + GrouperUtil.toStringForLog(firstTen, 1000))",
        "\" + validationKeyError + \"",
        "(grouperProvisioner, this.report, \"",
        "this.report.append(line)",
    };

    List<String> problems = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
    try {
      String line;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        for (String rawPattern : rawPatterns) {
          if (line.contains(rawPattern)) {
            problems.add(lineNumber + ": " + line.trim());
          }
        }
      }
    } finally {
      reader.close();
    }
    assertTrue("unescaped data appends in " + sourceFile.getPath() + ": " + problems, problems.isEmpty());
  }
}
