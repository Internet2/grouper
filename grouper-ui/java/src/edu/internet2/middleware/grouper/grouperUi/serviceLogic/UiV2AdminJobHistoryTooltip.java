/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.time.Duration;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Builds the HTML tooltip for one bar of the daemon job history chart
 * (UiV2Admin.jobHistoryChart).  The browser (gantt-chart-d3.js) inserts the tooltip with
 * d3 .html(), so the tooltip is HTML: the line breaks are real &lt;br/&gt; tags, and every
 * value that comes from stored data (the loader log job name and status, and the date
 * strings) is HTML-escaped here.  A job name contains the group name for group loader jobs,
 * so without escaping a group name with markup in it would run script for whoever hovers
 * the bar.
 *
 * The labels are externalized text (admin-configured, may hold markup on purpose) and are
 * output as-is, like on every other UI screen.  The counts are numbers.
 *
 * This logic was moved out of UiV2Admin so it can be unit tested and so the same class can
 * be dropped into every release (UiV2Admin.java differs from release to release, the
 * tooltip code did not).  Java 8 compatible on purpose (2.6.19).
 */
public class UiV2AdminJobHistoryTooltip {

  /**
   * no instances
   */
  private UiV2AdminJobHistoryTooltip() {
  }

  /**
   * HTML-escape a stored value for the tooltip (escapes &amp; &lt; &gt; and double quote).
   * Null prints "null", as the tooltip did before.
   * @param value
   * @return the escaped value
   */
  public static String escape(Object value) {
    return GrouperUtil.xmlEscape(String.valueOf(value), true);
  }

  /**
   * build the tooltip HTML for one loader log row.  Same text and layout as before the fix,
   * only the stored values are escaped.
   * @param log loader log row (job name, status, times, counts)
   * @param startDateString formatted started time (or "null")
   * @param endDateString formatted ended time (or "null")
   * @param lastUpdatedDateString formatted last updated time (or "null")
   * @param text externalized text map, i.e. TextContainer.retrieveFromRequest().getText()
   * @return the tooltip HTML
   */
  public static String tooltipHtml(Hib3GrouperLoaderLog log, String startDateString,
      String endDateString, String lastUpdatedDateString, Map<String, String> text) {

    // header: job name and status (stored data, escaped)
    StringBuilder tooltipBuilder = new StringBuilder()
      .append(escape(log.getJobName()))
      .append("<br/>")
      .append(text.get("adminJobHistoryTooltipStatus"))
      .append(": ").append(escape(log.getStatus()))
      .append("<br/>")
      .append(text.get("adminJobHistoryTooltipStarted"))
      .append(": ").append(escape(startDateString));

    // finished time and elapsed seconds, or last updated time if still running
    if (log.getEndedTime() != null) {
      tooltipBuilder.append("<br/>")
        .append(text.get("adminJobHistoryTooltipFinished"))
        .append(": " + escape(endDateString));
      if (log.getStartedTime() != null) {
        Duration duration = Duration.between(log.getStartedTime().toInstant(), log.getEndedTime().toInstant());
        tooltipBuilder.append("<br/>")
            .append(text.get("adminJobHistoryTooltipElapsed"))
            .append(":" + duration.getSeconds() + " ")
            .append(text.get("adminJobHistoryTooltipSecondsSuffix"))
        ;
      }
    } else if (log.getLastUpdated() != null) {
      // no end date but still in progress, so show the last updated time
      tooltipBuilder.append("<br/>")
            .append(text.get("adminJobHistoryTooltipLastUpdated"))
            .append(": " + escape(lastUpdatedDateString));
    }

    // counts (Integer, so nothing to escape)
    tooltipBuilder.append("<br/>")
      .append(text.get("adminJobHistoryTooltipInsertPrefix"))
      .append(":" + log.getInsertCount())
      .append(" ")
      .append(text.get("adminJobHistoryTooltipUpdatePrefix"))
      .append(":" + log.getUpdateCount())
      .append(" ")
      .append(text.get("adminJobHistoryTooltipDeletePrefix"))
      .append(":" + log.getDeleteCount())
      .append(" ")
      .append(text.get("adminJobHistoryTooltipTotalPrefix"))
      .append(":" + log.getTotalCount());

    return tooltipBuilder.toString();
  }

}
