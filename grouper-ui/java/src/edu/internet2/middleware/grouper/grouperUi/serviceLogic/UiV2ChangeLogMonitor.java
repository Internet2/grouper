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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditType;
import edu.internet2.middleware.grouper.audit.AuditTypeFinder;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumer;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogMonitorDao;
import edu.internet2.middleware.grouper.changeLog.ChangeLogType;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer.ChangeLogMonitorConsumer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer.ChangeLogMonitorJobRun;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer.ChangeLogMonitorPendingOwner;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer.ChangeLogMonitorPendingSource;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ChangeLogMonitorContainer.ChangeLogMonitorPendingType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * GRP-7510: change log monitor.  shows how many changes are waiting in grouper_change_log_entry_temp,
 * how far behind each change log consumer is, how fast they are processed (from grouper_loader_log),
 * and on request what the pending changes are and what caused them.  Grouper sysadmins only, since
 * pending changes name groups and subjects
 */
public class UiV2ChangeLogMonitor extends UiServiceLogicBase {

  /** hours the graphs can show.  grouper_loader_log is kept 7 days by default */
  private static final int[] RANGE_HOURS = new int[] {1, 6, 24, 72, 168};

  /** hours the graphs show if not chosen */
  private static final int DEFAULT_RANGE_HOURS = 24;

  /** most rows in each breakdown table */
  private static final int BREAKDOWN_MAX_ROWS = 20;

  /**
   * the change log monitor screen
   * @param request
   * @param response
   */
  public void changeLogMonitor(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkAllowed()) {
        return;
      }

      ChangeLogMonitorContainer container = GrouperRequestContainer.retrieveFromRequestOrCreate().getChangeLogMonitorContainer();

      long now = System.currentTimeMillis();
      int rangeHours = rangeHours(request.getParameter("changeLogMonitorRangeHours"));

      container.setAsOf(formatDate(now));
      container.setRangeHours(rangeHours);
      container.setChangeLogRetentionDays(GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.retain.db.change_log_entry.days", 14));
      container.setLoaderLogRetentionDays(GrouperLoaderConfig.retrieveConfig().propertyValueInt(GrouperLoaderConfig.LOADER_RETAIN_DB_LOGS_DAYS, 7));

      Long maxSequenceNumber = ChangeLogEntry.maxSequenceNumber(false);
      container.setMaxSequenceNumber(maxSequenceNumber);

      populateSummary(container, now);
      populateConsumers(container, maxSequenceNumber, now);
      Map<String, Object> chartData = chartData(container, rangeHours, now);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/admin/adminChangeLogMonitor.jsp"));
      guiResponseJs.addAction(GuiScreenAction.newAssign("changeLogMonitorChartData", chartData));
      guiResponseJs.addAction(GuiScreenAction.newScript("drawChangeLogMonitorCharts()"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * what the pending changes are and what caused them.  these group by over all of
   * grouper_change_log_entry_temp, which is big when the change log is behind, so they only run
   * when asked for and not every time the screen is refreshed
   * @param request
   * @param response
   */
  public void changeLogMonitorBreakdown(HttpServletRequest request, HttpServletResponse response) {

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(GrouperUiFilter.retrieveSubjectLoggedIn());

      if (!checkAllowed()) {
        return;
      }

      ChangeLogMonitorContainer container = GrouperRequestContainer.retrieveFromRequestOrCreate().getChangeLogMonitorContainer();

      long now = System.currentTimeMillis();
      container.setAsOf(formatDate(now));

      populatePendingByType(container, now);
      populatePendingByOwner(container);
      populatePendingBySource(container, grouperSession, now);

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#changeLogMonitorBreakdownDivId",
          "/WEB-INF/grouperUi2/admin/adminChangeLogMonitorBreakdown.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @return true if the user is a Grouper sysadmin, otherwise shows a message and returns false
   */
  private static boolean checkAllowed() {
    if (GrouperRequestContainer.retrieveFromRequestOrCreate().getChangeLogMonitorContainer().isCanViewChangeLogMonitor()) {
      return true;
    }
    GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
    guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error, text("changeLogMonitorNotAllowed")));
    guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
        "/WEB-INF/grouperUi2/index/indexMain.jsp"));
    return false;
  }

  /**
   * temp change log size and age, how fast it is moving, and the change log itself
   * @param container
   * @param now
   */
  private static void populateSummary(ChangeLogMonitorContainer container, long now) {

    long pending = ChangeLogMonitorDao.retrieveTempCount();
    container.setPendingCount(pending);

    // created_on is micros since 1970
    Long oldestPendingMicros = ChangeLogMonitorDao.retrieveTempOldestCreatedOnMicros();
    if (oldestPendingMicros != null) {
      container.setOldestPendingDate(formatDate(oldestPendingMicros / 1000));
      container.setOldestPendingAge(age(now, oldestPendingMicros / 1000));
    }

    String tempJobName = GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG;
    long movedLastHour = ChangeLogMonitorDao.retrieveLoaderLogTotalCountSince(
        ChangeLogMonitorDao.loaderLogJobName(tempJobName, tempToChangeLogLongRunning()), now - 3600000L);
    container.setMovedLastHour(movedLastHour);
    container.setMovedPerMinute(Math.round(movedLastHour / 60.0));
    if (pending > 0 && movedLastHour > 0) {
      long seconds = (pending * 3600L) / movedLastHour;
      container.setEstimatedTimeToEmpty(GrouperUiUtils.convertSecondsToString((int)Math.min(seconds, Integer.MAX_VALUE)));
    }

    container.setTempToChangeLogLastRun(lastRun(tempJobName, now));

    Long oldestEntryMicros = ChangeLogMonitorDao.retrieveEntryOldestCreatedOnMicros();
    if (oldestEntryMicros != null) {
      container.setOldestEntryDate(formatDate(oldestEntryMicros / 1000));
    }
  }

  /**
   * every row of grouper_change_log_consumer, how far behind it is and its latest run.  the ones
   * that are furthest behind are first
   * @param container
   * @param maxSequenceNumber highest sequence number in the change log, or null if it is empty
   * @param now
   */
  private static void populateConsumers(ChangeLogMonitorContainer container, Long maxSequenceNumber, long now) {

    // entries before this were deleted by the delete old records daemon
    Long minSequenceNumber = ChangeLogMonitorDao.retrieveEntryMinSequenceNumber();

    Set<ChangeLogConsumer> changeLogConsumers = GrouperDAOFactory.getFactory().getChangeLogConsumer().findAll();

    List<ChangeLogMonitorConsumer> consumers = new ArrayList<ChangeLogMonitorConsumer>();

    for (ChangeLogConsumer changeLogConsumer : GrouperUtil.nonNull(changeLogConsumers)) {

      String name = changeLogConsumer.getName();
      String jobName = GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + name;
      boolean configured = !StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." + name + ".class"));
      Long lastSequenceProcessed = changeLogConsumer.getLastSequenceProcessed();

      long entriesBehind = 0;
      if (maxSequenceNumber != null && lastSequenceProcessed != null) {
        entriesBehind = Math.max(0, maxSequenceNumber - lastSequenceProcessed);
      }

      String oldestUnprocessedAge = null;
      boolean entriesDeletedBeforeProcessed = false;
      ChangeLogMonitorJobRun lastRun = null;

      // consumers that are not configured anymore are left behind forever, dont spend queries on them
      if (configured) {
        if (entriesBehind > 0) {
          List<ChangeLogEntry> nextEntries = GrouperDAOFactory.getFactory().getChangeLogEntry().retrieveBatch(lastSequenceProcessed, 1);
          if (GrouperUtil.length(nextEntries) > 0 && nextEntries.get(0).getCreatedOnDb() != null) {
            oldestUnprocessedAge = age(now, nextEntries.get(0).getCreatedOnDb() / 1000);
          }
          entriesDeletedBeforeProcessed = minSequenceNumber != null && lastSequenceProcessed + 1 < minSequenceNumber;
        }
        lastRun = lastRun(jobName, now);
      }

      String lastProgress = changeLogConsumer.getLastUpdatedDb() == null ? null : formatDate(changeLogConsumer.getLastUpdatedDb());

      consumers.add(new ChangeLogMonitorConsumer(name, configured ? jobName : null, configured, lastSequenceProcessed,
          entriesBehind, oldestUnprocessedAge, lastProgress, entriesDeletedBeforeProcessed, lastRun));
    }

    Collections.sort(consumers, new Comparator<ChangeLogMonitorConsumer>() {

      @Override
      public int compare(ChangeLogMonitorConsumer o1, ChangeLogMonitorConsumer o2) {
        if (o1.isConfigured() != o2.isConfigured()) {
          return o1.isConfigured() ? -1 : 1;
        }
        if (o1.getEntriesBehind() != o2.getEntriesBehind()) {
          return o1.getEntriesBehind() > o2.getEntriesBehind() ? -1 : 1;
        }
        return StringUtils.defaultString(o1.getName()).compareTo(StringUtils.defaultString(o2.getName()));
      }
    });

    container.setConsumers(consumers);
  }

  /**
   * data for the graphs: temp change log rows moved and change log entries processed by each consumer,
   * summed into buckets of time, from the total count of grouper_loader_log rows
   * @param container
   * @param rangeHours
   * @param now
   * @return the data for drawChangeLogMonitorCharts() in grouperChangeLogMonitorChart.js
   */
  private static Map<String, Object> chartData(ChangeLogMonitorContainer container, int rangeHours, long now) {

    long bucketMillis;
    if (rangeHours <= 1) {
      bucketMillis = 60000L;
      container.setBucketLabel(text("changeLogMonitorBucketMinute"));
    } else if (rangeHours <= 6) {
      bucketMillis = 5 * 60000L;
      container.setBucketLabel(text("changeLogMonitorBucket5Minutes"));
    } else if (rangeHours <= 24) {
      bucketMillis = 15 * 60000L;
      container.setBucketLabel(text("changeLogMonitorBucket15Minutes"));
    } else {
      bucketMillis = 3600000L;
      container.setBucketLabel(text("changeLogMonitorBucketHour"));
    }

    long start = ((now - (rangeHours * 3600000L)) / bucketMillis) * bucketMillis;
    int bucketCount = (int)((now - start) / bucketMillis) + 1;

    // the loader log job name to read, and the name of its line on the graph.  long running jobs log
    // a sub job every minute or so and add those up in the overall row, so only count one of them
    Map<String, String> loaderLogJobNameToSeriesName = new LinkedHashMap<String, String>();
    loaderLogJobNameToSeriesName.put(ChangeLogMonitorDao.loaderLogJobName(GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG,
        tempToChangeLogLongRunning()), GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    for (ChangeLogMonitorConsumer consumer : container.getConsumers()) {
      if (!consumer.isConfigured()) {
        continue;
      }
      boolean longRunning = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer." + consumer.getName() + ".longRunning", false);
      loaderLogJobNameToSeriesName.put(ChangeLogMonitorDao.loaderLogJobName(consumer.getJobName(), longRunning), consumer.getName());
    }

    Map<String, long[]> seriesValues = new LinkedHashMap<String, long[]>();
    for (String loaderLogJobName : loaderLogJobNameToSeriesName.keySet()) {
      seriesValues.put(loaderLogJobName, new long[bucketCount]);
    }

    List<String> loaderLogJobNames = new ArrayList<String>(loaderLogJobNameToSeriesName.keySet());

    List<Object[]> rows = ChangeLogMonitorDao.retrieveLoaderLogTotalCounts(loaderLogJobNames, start);
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      long[] values = seriesValues.get((String)row[0]);
      Date startedTime = (Date)row[1];
      if (values == null || startedTime == null || row[2] == null) {
        continue;
      }
      int bucket = (int)((startedTime.getTime() - start) / bucketMillis);
      if (bucket >= 0 && bucket < bucketCount) {
        values[bucket] += GrouperUtil.longValue(row[2]);
      }
    }

    List<Long> dates = new ArrayList<Long>();
    for (int i = 0; i < bucketCount; i++) {
      dates.add(start + (i * bucketMillis));
    }

    Map<String, Object> chartData = new LinkedHashMap<String, Object>();
    chartData.put("dates", dates);

    List<Map<String, Object>> consumerSeries = new ArrayList<Map<String, Object>>();
    boolean first = true;
    for (String loaderLogJobName : loaderLogJobNames) {
      long[] values = seriesValues.get(loaderLogJobName);
      List<Long> valuesList = new ArrayList<Long>();
      long total = 0;
      for (long value : values) {
        valuesList.add(value);
        total += value;
      }
      // the temp to change log job is first
      if (first) {
        chartData.put("tempName", text("changeLogMonitorChartTempSeries"));
        chartData.put("tempValues", valuesList);
        first = false;
        continue;
      }
      // leave out consumers that did nothing, otherwise there are a lot of lines on zero
      if (total == 0) {
        continue;
      }
      Map<String, Object> series = new LinkedHashMap<String, Object>();
      series.put("name", loaderLogJobNameToSeriesName.get(loaderLogJobName));
      series.put("values", valuesList);
      consumerSeries.add(series);
    }
    chartData.put("consumerSeries", consumerSeries);
    chartData.put("yLabel", text("changeLogMonitorChartYLabel"));

    container.setConsumerChartHasData(consumerSeries.size() > 0);

    return chartData;
  }

  /**
   * pending temp change log rows by type, most first
   * @param container
   * @param now
   */
  private static void populatePendingByType(ChangeLogMonitorContainer container, long now) {

    List<Object[]> rows = ChangeLogMonitorDao.retrieveTempCountsByType();

    List<ChangeLogMonitorPendingType> pendingByType = new ArrayList<ChangeLogMonitorPendingType>();
    for (Object[] row : GrouperUtil.nonNull(rows)) {
      String changeLogTypeId = (String)row[0];
      ChangeLogType changeLogType = changeLogTypeId == null ? null : ChangeLogTypeFinder.find(changeLogTypeId, false);
      String category = changeLogType == null ? changeLogTypeId : changeLogType.getChangeLogCategory();
      String action = changeLogType == null ? null : changeLogType.getActionName();
      String oldestAge = row[2] == null ? null : age(now, GrouperUtil.longValue(row[2]) / 1000);
      pendingByType.add(new ChangeLogMonitorPendingType(category, action, GrouperUtil.longValue(row[1]), oldestAge));
    }

    Collections.sort(pendingByType, new Comparator<ChangeLogMonitorPendingType>() {

      @Override
      public int compare(ChangeLogMonitorPendingType o1, ChangeLogMonitorPendingType o2) {
        return Long.compare(o2.getCount(), o1.getCount());
      }
    });

    container.setPendingByType(pendingByType);
  }

  /**
   * groups and folders with the most pending membership and privilege changes, e.g. a loader job
   * adding a lot of members to one group
   * @param container
   */
  private static void populatePendingByOwner(ChangeLogMonitorContainer container) {

    Map<String, ChangeLogMonitorPendingOwner> owners = new LinkedHashMap<String, ChangeLogMonitorPendingOwner>();

    // if any of these hit the most rows, there could be more owners than are shown
    boolean truncated = false;
    if (addPendingByOwner(owners, ChangeLogTypeBuiltin.MEMBERSHIP_ADD, ChangeLogLabels.MEMBERSHIP_ADD.groupName.name(), true)) {
      truncated = true;
    }
    if (addPendingByOwner(owners, ChangeLogTypeBuiltin.MEMBERSHIP_DELETE, ChangeLogLabels.MEMBERSHIP_DELETE.groupName.name(), true)) {
      truncated = true;
    }
    if (addPendingByOwner(owners, ChangeLogTypeBuiltin.PRIVILEGE_ADD, ChangeLogLabels.PRIVILEGE_ADD.ownerName.name(), false)) {
      truncated = true;
    }
    if (addPendingByOwner(owners, ChangeLogTypeBuiltin.PRIVILEGE_DELETE, ChangeLogLabels.PRIVILEGE_DELETE.ownerName.name(), false)) {
      truncated = true;
    }

    List<ChangeLogMonitorPendingOwner> pendingByOwner = new ArrayList<ChangeLogMonitorPendingOwner>(owners.values());

    Collections.sort(pendingByOwner, new Comparator<ChangeLogMonitorPendingOwner>() {

      @Override
      public int compare(ChangeLogMonitorPendingOwner o1, ChangeLogMonitorPendingOwner o2) {
        return Long.compare(o2.getTotalCount(), o1.getTotalCount());
      }
    });

    if (pendingByOwner.size() > BREAKDOWN_MAX_ROWS) {
      pendingByOwner = new ArrayList<ChangeLogMonitorPendingOwner>(pendingByOwner.subList(0, BREAKDOWN_MAX_ROWS));
      truncated = true;
    }

    container.setPendingByOwner(pendingByOwner);
    container.setPendingByOwnerTruncated(truncated);
  }

  /**
   * add the owners with the most pending changes of one change log type
   * @param owners owner name to its counts
   * @param changeLogTypeBuiltin
   * @param ownerNameLabel label of the group or folder name in the type
   * @param membership true for membership counts, false for privilege counts
   * @return true if the most rows came back, so there could be more owners of this type
   */
  private static boolean addPendingByOwner(Map<String, ChangeLogMonitorPendingOwner> owners,
      ChangeLogTypeBuiltin changeLogTypeBuiltin, String ownerNameLabel, boolean membership) {

    List<Object[]> rows = ChangeLogMonitorDao.retrieveTempCountsByLabel(changeLogTypeBuiltin, ownerNameLabel, BREAKDOWN_MAX_ROWS);

    for (Object[] row : GrouperUtil.nonNull(rows)) {
      String name = StringUtils.defaultString((String)row[0]);
      ChangeLogMonitorPendingOwner owner = owners.get(name);
      if (owner == null) {
        owner = new ChangeLogMonitorPendingOwner(name);
        owners.put(name, owner);
      }
      if (membership) {
        owner.addMembershipCount(GrouperUtil.longValue(row[1]));
      } else {
        owner.addPrivilegeCount(GrouperUtil.longValue(row[1]));
      }
    }
    return GrouperUtil.length(rows) >= BREAKDOWN_MAX_ROWS;
  }

  /**
   * the actions that caused the most pending changes, by context id, and their audit entry.  one
   * action, e.g. a loader job run, a bulk import, or deleting a big group, can cause a lot of changes
   * @param container
   * @param grouperSession
   * @param now
   */
  private static void populatePendingBySource(ChangeLogMonitorContainer container, GrouperSession grouperSession, long now) {

    List<Object[]> rows = ChangeLogMonitorDao.retrieveTempCountsByContextId(BREAKDOWN_MAX_ROWS);

    List<ChangeLogMonitorPendingSource> pendingBySource = new ArrayList<ChangeLogMonitorPendingSource>();

    for (Object[] row : GrouperUtil.nonNull(rows)) {

      String contextId = (String)row[0];
      long count = GrouperUtil.longValue(row[1]);
      String firstPendingAge = row[2] == null ? null : age(now, GrouperUtil.longValue(row[2]) / 1000);

      String auditAction = null;
      String description = null;
      String engine = null;
      String serverHost = null;
      String loggedInSubject = null;

      if (!StringUtils.isBlank(contextId)) {
        AuditEntry auditEntry = ChangeLogMonitorDao.retrieveAuditEntryByContextId(contextId);

        if (auditEntry != null) {
          AuditType auditType = AuditTypeFinder.find(auditEntry.getAuditTypeId(), false);
          auditAction = auditType == null ? auditEntry.getAuditTypeId() : auditType.getAuditCategory() + " - " + auditType.getActionName();
          description = StringUtils.abbreviate(auditEntry.getDescription(), 300);
          engine = auditEntry.getGrouperEngine();
          serverHost = auditEntry.getServerHost();
          if (!StringUtils.isBlank(auditEntry.getLoggedInMemberId())) {
            Member member = MemberFinder.findByUuid(grouperSession, auditEntry.getLoggedInMemberId(), false);
            if (member != null) {
              loggedInSubject = member.getSubjectSourceId() + ":" + member.getSubjectId();
            }
          }
        }
      }

      pendingBySource.add(new ChangeLogMonitorPendingSource(contextId, count, firstPendingAge, auditAction,
          description, engine, serverHost, loggedInSubject));
    }

    container.setPendingBySource(pendingBySource);
    container.setPendingBySourceTruncated(GrouperUtil.length(rows) >= BREAKDOWN_MAX_ROWS);
  }

  /**
   * latest loader log row of a job or its sub jobs
   * @param jobName
   * @param now
   * @return the run or null if there is none in grouper_loader_log
   */
  private static ChangeLogMonitorJobRun lastRun(String jobName, long now) {

    Hib3GrouperLoaderLog loaderLog = ChangeLogMonitorDao.retrieveLatestLoaderLog(jobName);
    if (loaderLog == null) {
      return null;
    }

    Timestamp lastUpdated = loaderLog.getLastUpdated();
    return new ChangeLogMonitorJobRun(loaderLog.getStatus(), loaderLog.getHost(),
        lastUpdated == null ? null : formatDate(lastUpdated.getTime()),
        lastUpdated == null ? null : age(now, lastUpdated.getTime()));
  }

  /**
   * @return if the temp to change log job is long running, i.e. logs a sub job every minute or so
   */
  private static boolean tempToChangeLogLongRunning() {
    return GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.changeLogTempToChangeLog.longRunning", true);
  }

  /**
   * @param rangeHoursString from the screen
   * @return the hours if they are one of the choices, otherwise the default
   */
  private static int rangeHours(String rangeHoursString) {
    if (!StringUtils.isBlank(rangeHoursString)) {
      for (int rangeHours : RANGE_HOURS) {
        if (Integer.toString(rangeHours).equals(rangeHoursString)) {
          return rangeHours;
        }
      }
    }
    return DEFAULT_RANGE_HOURS;
  }

  /**
   * @param now
   * @param millis
   * @return how long before now the millis are, e.g. 2 hours 3 minutes 4 seconds
   */
  private static String age(long now, long millis) {
    long seconds = Math.max(0, (now - millis) / 1000);
    return GrouperUiUtils.convertSecondsToString((int)Math.min(seconds, Integer.MAX_VALUE));
  }

  /**
   * @param millis
   * @return the date for the screen
   */
  private static String formatDate(long millis) {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date(millis));
  }

  /**
   * @param key
   * @return the externalized text
   */
  private static String text(String key) {
    return TextContainer.retrieveFromRequest().getText().get(key);
  }
}
