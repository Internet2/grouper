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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (loaderJobHistoryTooltipStoredXss): the daemon job history chart
 * (UiV2Admin.jobHistoryChart) built each bar's tooltip HTML with the loader log job name and
 * status unescaped, and gantt-chart-d3.js inserts the tooltip with d3 .html().  A group loader
 * job name holds the group name, so a group named with markup ran script for whoever hovered the
 * bar.  The tooltip is now built by UiV2AdminJobHistoryTooltip, which escapes the stored values
 * and keeps the br separators.
 *
 * Plain TestCase: no database, no Grouper config, no servlet request (the externalized text map
 * is passed in).  Does not compile before the fix (the class is new).  Same file for every
 * release (Java 8 compatible).
 */
public class UiV2AdminJobHistoryTooltipTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(UiV2AdminJobHistoryTooltipTest.class);
  }

  /**
   *
   */
  public UiV2AdminJobHistoryTooltipTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AdminJobHistoryTooltipTest(String name) {
    super(name);
  }

  /**
   * labels like the real externalized text (short so the expected strings stay readable)
   * @return the text map
   */
  private static Map<String, String> text() {
    Map<String, String> text = new HashMap<String, String>();
    text.put("adminJobHistoryTooltipStatus", "Status");
    text.put("adminJobHistoryTooltipStarted", "Started");
    text.put("adminJobHistoryTooltipFinished", "Finished");
    text.put("adminJobHistoryTooltipElapsed", "Elapsed");
    text.put("adminJobHistoryTooltipSecondsSuffix", "sec");
    text.put("adminJobHistoryTooltipLastUpdated", "Last updated");
    text.put("adminJobHistoryTooltipInsertPrefix", "I");
    text.put("adminJobHistoryTooltipUpdatePrefix", "U");
    text.put("adminJobHistoryTooltipDeletePrefix", "D");
    text.put("adminJobHistoryTooltipTotalPrefix", "T");
    return text;
  }

  /**
   * a finished loader log row
   * @param jobName
   * @param status
   * @return the row (not saved)
   */
  private static Hib3GrouperLoaderLog log(String jobName, String status) {
    Hib3GrouperLoaderLog log = new Hib3GrouperLoaderLog();
    log.setJobName(jobName);
    log.setStatus(status);
    log.setStartedTime(new Timestamp(1000000000000L));
    log.setEndedTime(new Timestamp(1000000007000L));
    log.setInsertCount(1);
    log.setUpdateCount(2);
    log.setDeleteCount(3);
    log.setTotalCount(4);
    return log;
  }

  /**
   * a job name (group name) and status with markup come out as text; the br separators stay
   */
  public void testHostileJobNameEscaped() {
    String jobName = "SQL_SIMPLE__test:</div><img id=\"secPatch143xss\" src=x onerror=alert(143)>__abc123";
    String status = "SUCCESS<script>alert(1)</script>";
    String tooltip = UiV2AdminJobHistoryTooltip.tooltipHtml(log(jobName, status),
        "2001/09/09 01:46:40", "2001/09/09 01:46:47", "null", text());

    assertFalse(tooltip, tooltip.contains("<img"));
    assertFalse(tooltip, tooltip.contains("</div>"));
    assertFalse(tooltip, tooltip.contains("<script"));
    assertFalse(tooltip, tooltip.contains("\"secPatch143xss\""));
    assertTrue(tooltip, tooltip.startsWith(
        "SQL_SIMPLE__test:&lt;/div&gt;&lt;img id=&quot;secPatch143xss&quot; src=x onerror=alert(143)&gt;__abc123<br/>"));
    assertTrue(tooltip, tooltip.contains("Status: SUCCESS&lt;script&gt;alert(1)&lt;/script&gt;<br/>"));

    // the only markup left is the br separators
    assertEquals(tooltip, tooltip.replace("<br/>", "").indexOf('<'), -1);
  }

  /**
   * an ordinary finished row renders exactly as before the fix
   */
  public void testOrdinaryFinishedRowUnchanged() {
    String tooltip = UiV2AdminJobHistoryTooltip.tooltipHtml(
        log("SQL_SIMPLE__test:loaderGroup__abc123", "SUCCESS"),
        "2001/09/09 01:46:40", "2001/09/09 01:46:47", "null", text());
    assertEquals("SQL_SIMPLE__test:loaderGroup__abc123<br/>Status: SUCCESS<br/>Started: 2001/09/09 01:46:40"
        + "<br/>Finished: 2001/09/09 01:46:47<br/>Elapsed:7 sec<br/>I:1 U:2 D:3 T:4", tooltip);
  }

  /**
   * a running row (no end time) shows the last updated time; ampersands are escaped once
   */
  public void testRunningRowShowsLastUpdated() {
    Hib3GrouperLoaderLog log = log("OTHER_JOB_a&b", "STARTED");
    log.setEndedTime(null);
    log.setLastUpdated(new Timestamp(1000000005000L));
    String tooltip = UiV2AdminJobHistoryTooltip.tooltipHtml(log,
        "2001/09/09 01:46:40", "null", "2001/09/09 01:46:45", text());
    assertEquals("OTHER_JOB_a&amp;b<br/>Status: STARTED<br/>Started: 2001/09/09 01:46:40"
        + "<br/>Last updated: 2001/09/09 01:46:45<br/>I:1 U:2 D:3 T:4", tooltip);
  }

}
