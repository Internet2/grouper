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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * GRP-7510: what the change log monitor screen shows, filled in by UiV2ChangeLogMonitor
 */
public class ChangeLogMonitorContainer {

  /**
   * Grouper sysadmins only, since pending changes name groups and subjects
   * @return true if the logged in user can see the change log monitor
   */
  public boolean isCanViewChangeLogMonitor() {
    return PrivilegeHelper.isWheelOrRoot(GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /**
   * a count for the screen, with the digits grouped the way the browser's locale does it, e.g. 1,234,567
   * @param number the count
   * @return the count, formatted
   */
  public String formatNumber(long number) {
    Locale locale = GrouperUiFilter.retrieveLocale();
    return NumberFormat.getIntegerInstance(locale == null ? Locale.getDefault() : locale).format(number);
  }

  /** when the numbers on the screen were computed */
  private String asOf;

  /** hours the graphs show */
  private int rangeHours;

  /** how much time each point in the graphs covers, e.g. 15 minutes */
  private String bucketLabel;

  /** if any change log consumer processed entries in the range of the graphs */
  private boolean consumerChartHasData;

  /** rows in grouper_change_log_entry_temp */
  private long pendingCount;

  /** when the oldest pending change happened, or null if nothing is pending */
  private String oldestPendingDate;

  /** how long ago the oldest pending change happened, or null if nothing is pending */
  private String oldestPendingAge;

  /** temp change log rows moved to the change log in the last hour */
  private long movedLastHour;

  /** average temp change log rows moved per minute in the last hour */
  private long movedPerMinute;

  /** how long the pending changes take at the rate of the last hour if no new changes arrive, or null */
  private String estimatedTimeToEmpty;

  /** latest run of the job that moves temp change log rows to the change log, or null if never run */
  private ChangeLogMonitorJobRun tempToChangeLogLastRun;

  /** highest sequence number in grouper_change_log_entry, or null if empty */
  private Long maxSequenceNumber;

  /** when the oldest change in grouper_change_log_entry happened, or null if empty */
  private String oldestEntryDate;

  /** loader.retain.db.change_log_entry.days */
  private int changeLogRetentionDays;

  /** loader.retain.db.logs.days, how far back the graphs can go */
  private int loaderLogRetentionDays;

  /** change log consumers */
  private List<ChangeLogMonitorConsumer> consumers = new ArrayList<ChangeLogMonitorConsumer>();

  /** pending changes by change log type, most first */
  private List<ChangeLogMonitorPendingType> pendingByType = new ArrayList<ChangeLogMonitorPendingType>();

  /** groups and folders with the most pending membership and privilege changes */
  private List<ChangeLogMonitorPendingOwner> pendingByOwner = new ArrayList<ChangeLogMonitorPendingOwner>();

  /** actions (contexts) that caused the most pending changes */
  private List<ChangeLogMonitorPendingSource> pendingBySource = new ArrayList<ChangeLogMonitorPendingSource>();

  /** if there could be more groups and folders with pending changes than are shown */
  private boolean pendingByOwnerTruncated;

  /** if there could be more actions with pending changes than are shown */
  private boolean pendingBySourceTruncated;

  /**
   * @return if there could be more groups and folders with pending changes than are shown
   */
  public boolean isPendingByOwnerTruncated() {
    return this.pendingByOwnerTruncated;
  }

  /**
   * @param pendingByOwnerTruncated1 if there could be more groups and folders with pending changes than are shown
   */
  public void setPendingByOwnerTruncated(boolean pendingByOwnerTruncated1) {
    this.pendingByOwnerTruncated = pendingByOwnerTruncated1;
  }

  /**
   * @return if there could be more actions with pending changes than are shown
   */
  public boolean isPendingBySourceTruncated() {
    return this.pendingBySourceTruncated;
  }

  /**
   * @param pendingBySourceTruncated1 if there could be more actions with pending changes than are shown
   */
  public void setPendingBySourceTruncated(boolean pendingBySourceTruncated1) {
    this.pendingBySourceTruncated = pendingBySourceTruncated1;
  }

  /**
   * @return when the numbers on the screen were computed
   */
  public String getAsOf() {
    return this.asOf;
  }

  /**
   * @param asOf1 when the numbers on the screen were computed
   */
  public void setAsOf(String asOf1) {
    this.asOf = asOf1;
  }

  /**
   * @return hours the graphs show
   */
  public int getRangeHours() {
    return this.rangeHours;
  }

  /**
   * @param rangeHours1 hours the graphs show
   */
  public void setRangeHours(int rangeHours1) {
    this.rangeHours = rangeHours1;
  }

  /**
   * @return how much time each point in the graphs covers
   */
  public String getBucketLabel() {
    return this.bucketLabel;
  }

  /**
   * @param bucketLabel1 how much time each point in the graphs covers
   */
  public void setBucketLabel(String bucketLabel1) {
    this.bucketLabel = bucketLabel1;
  }

  /**
   * @return if any change log consumer processed entries in the range of the graphs
   */
  public boolean isConsumerChartHasData() {
    return this.consumerChartHasData;
  }

  /**
   * @param consumerChartHasData1 if any change log consumer processed entries in the range of the graphs
   */
  public void setConsumerChartHasData(boolean consumerChartHasData1) {
    this.consumerChartHasData = consumerChartHasData1;
  }

  /**
   * @return rows in grouper_change_log_entry_temp
   */
  public long getPendingCount() {
    return this.pendingCount;
  }

  /**
   * @param pendingCount1 rows in grouper_change_log_entry_temp
   */
  public void setPendingCount(long pendingCount1) {
    this.pendingCount = pendingCount1;
  }

  /**
   * @return when the oldest pending change happened, or null
   */
  public String getOldestPendingDate() {
    return this.oldestPendingDate;
  }

  /**
   * @param oldestPendingDate1 when the oldest pending change happened
   */
  public void setOldestPendingDate(String oldestPendingDate1) {
    this.oldestPendingDate = oldestPendingDate1;
  }

  /**
   * @return how long ago the oldest pending change happened, or null
   */
  public String getOldestPendingAge() {
    return this.oldestPendingAge;
  }

  /**
   * @param oldestPendingAge1 how long ago the oldest pending change happened
   */
  public void setOldestPendingAge(String oldestPendingAge1) {
    this.oldestPendingAge = oldestPendingAge1;
  }

  /**
   * @return temp change log rows moved to the change log in the last hour
   */
  public long getMovedLastHour() {
    return this.movedLastHour;
  }

  /**
   * @param movedLastHour1 temp change log rows moved to the change log in the last hour
   */
  public void setMovedLastHour(long movedLastHour1) {
    this.movedLastHour = movedLastHour1;
  }

  /**
   * @return average temp change log rows moved per minute in the last hour
   */
  public long getMovedPerMinute() {
    return this.movedPerMinute;
  }

  /**
   * @param movedPerMinute1 average temp change log rows moved per minute in the last hour
   */
  public void setMovedPerMinute(long movedPerMinute1) {
    this.movedPerMinute = movedPerMinute1;
  }

  /**
   * @return how long the pending changes take at the rate of the last hour, or null
   */
  public String getEstimatedTimeToEmpty() {
    return this.estimatedTimeToEmpty;
  }

  /**
   * @param estimatedTimeToEmpty1 how long the pending changes take at the rate of the last hour
   */
  public void setEstimatedTimeToEmpty(String estimatedTimeToEmpty1) {
    this.estimatedTimeToEmpty = estimatedTimeToEmpty1;
  }

  /**
   * @return latest run of the job that moves temp change log rows to the change log, or null
   */
  public ChangeLogMonitorJobRun getTempToChangeLogLastRun() {
    return this.tempToChangeLogLastRun;
  }

  /**
   * @param tempToChangeLogLastRun1 latest run of the job that moves temp change log rows to the change log
   */
  public void setTempToChangeLogLastRun(ChangeLogMonitorJobRun tempToChangeLogLastRun1) {
    this.tempToChangeLogLastRun = tempToChangeLogLastRun1;
  }

  /**
   * @return highest sequence number in grouper_change_log_entry, or null
   */
  public Long getMaxSequenceNumber() {
    return this.maxSequenceNumber;
  }

  /**
   * @param maxSequenceNumber1 highest sequence number in grouper_change_log_entry
   */
  public void setMaxSequenceNumber(Long maxSequenceNumber1) {
    this.maxSequenceNumber = maxSequenceNumber1;
  }

  /**
   * @return when the oldest change in grouper_change_log_entry happened, or null
   */
  public String getOldestEntryDate() {
    return this.oldestEntryDate;
  }

  /**
   * @param oldestEntryDate1 when the oldest change in grouper_change_log_entry happened
   */
  public void setOldestEntryDate(String oldestEntryDate1) {
    this.oldestEntryDate = oldestEntryDate1;
  }

  /**
   * @return loader.retain.db.change_log_entry.days
   */
  public int getChangeLogRetentionDays() {
    return this.changeLogRetentionDays;
  }

  /**
   * @param changeLogRetentionDays1 loader.retain.db.change_log_entry.days
   */
  public void setChangeLogRetentionDays(int changeLogRetentionDays1) {
    this.changeLogRetentionDays = changeLogRetentionDays1;
  }

  /**
   * @return loader.retain.db.logs.days
   */
  public int getLoaderLogRetentionDays() {
    return this.loaderLogRetentionDays;
  }

  /**
   * @param loaderLogRetentionDays1 loader.retain.db.logs.days
   */
  public void setLoaderLogRetentionDays(int loaderLogRetentionDays1) {
    this.loaderLogRetentionDays = loaderLogRetentionDays1;
  }

  /**
   * @return change log consumers
   */
  public List<ChangeLogMonitorConsumer> getConsumers() {
    return this.consumers;
  }

  /**
   * @param consumers1 change log consumers
   */
  public void setConsumers(List<ChangeLogMonitorConsumer> consumers1) {
    this.consumers = consumers1;
  }

  /**
   * @return pending changes by change log type, most first
   */
  public List<ChangeLogMonitorPendingType> getPendingByType() {
    return this.pendingByType;
  }

  /**
   * @param pendingByType1 pending changes by change log type, most first
   */
  public void setPendingByType(List<ChangeLogMonitorPendingType> pendingByType1) {
    this.pendingByType = pendingByType1;
  }

  /**
   * @return groups and folders with the most pending membership and privilege changes
   */
  public List<ChangeLogMonitorPendingOwner> getPendingByOwner() {
    return this.pendingByOwner;
  }

  /**
   * @param pendingByOwner1 groups and folders with the most pending membership and privilege changes
   */
  public void setPendingByOwner(List<ChangeLogMonitorPendingOwner> pendingByOwner1) {
    this.pendingByOwner = pendingByOwner1;
  }

  /**
   * @return actions (contexts) that caused the most pending changes
   */
  public List<ChangeLogMonitorPendingSource> getPendingBySource() {
    return this.pendingBySource;
  }

  /**
   * @param pendingBySource1 actions (contexts) that caused the most pending changes
   */
  public void setPendingBySource(List<ChangeLogMonitorPendingSource> pendingBySource1) {
    this.pendingBySource = pendingBySource1;
  }

  /**
   * latest grouper_loader_log row of a change log job (or one of its sub jobs)
   */
  public static class ChangeLogMonitorJobRun {

    /** e.g. SUCCESS, STARTED, ERROR */
    private final String status;

    /** host that ran it */
    private final String host;

    /** when the log row was last updated */
    private final String lastUpdated;

    /** how long ago the log row was last updated */
    private final String lastUpdatedAge;

    /**
     * @param theStatus e.g. SUCCESS, STARTED, ERROR
     * @param theHost host that ran it
     * @param theLastUpdated when the log row was last updated
     * @param theLastUpdatedAge how long ago the log row was last updated
     */
    public ChangeLogMonitorJobRun(String theStatus, String theHost, String theLastUpdated, String theLastUpdatedAge) {
      this.status = theStatus;
      this.host = theHost;
      this.lastUpdated = theLastUpdated;
      this.lastUpdatedAge = theLastUpdatedAge;
    }

    /**
     * @return e.g. SUCCESS, STARTED, ERROR
     */
    public String getStatus() {
      return this.status;
    }

    /**
     * @return host that ran it
     */
    public String getHost() {
      return this.host;
    }

    /**
     * @return when the log row was last updated
     */
    public String getLastUpdated() {
      return this.lastUpdated;
    }

    /**
     * @return how long ago the log row was last updated
     */
    public String getLastUpdatedAge() {
      return this.lastUpdatedAge;
    }

    /**
     * @return true if the status is an error status, to highlight it
     */
    public boolean isError() {
      return this.status != null && this.status.startsWith("ERROR");
    }
  }

  /**
   * one row of grouper_change_log_consumer and how far behind it is
   */
  public static class ChangeLogMonitorConsumer {

    /** consumer name */
    private final String name;

    /** loader job name of the consumer, e.g. CHANGE_LOG_consumer_compositeMemberships */
    private final String jobName;

    /** if there is still a changeLog.consumer.name.class config for it */
    private final boolean configured;

    /** last sequence processed, or null */
    private final Long lastSequenceProcessed;

    /** change log entries after the last one processed */
    private final long entriesBehind;

    /** how long ago the oldest unprocessed entry happened, or null if none */
    private final String oldestUnprocessedAge;

    /** when the consumer last moved its last sequence processed, or null */
    private final String lastProgress;

    /** if change log entries this consumer had not processed were deleted for being too old */
    private final boolean entriesDeletedBeforeProcessed;

    /** latest run of the consumer job, or null */
    private final ChangeLogMonitorJobRun lastRun;

    /**
     * @param theName consumer name
     * @param theJobName loader job name of the consumer
     * @param theConfigured if there is still config for it
     * @param theLastSequenceProcessed last sequence processed, or null
     * @param theEntriesBehind change log entries after the last one processed
     * @param theOldestUnprocessedAge how long ago the oldest unprocessed entry happened, or null
     * @param theLastProgress when the consumer last moved forward, or null
     * @param theEntriesDeletedBeforeProcessed if unprocessed entries were deleted for being too old
     * @param theLastRun latest run of the consumer job, or null
     */
    public ChangeLogMonitorConsumer(String theName, String theJobName, boolean theConfigured,
        Long theLastSequenceProcessed, long theEntriesBehind, String theOldestUnprocessedAge,
        String theLastProgress, boolean theEntriesDeletedBeforeProcessed, ChangeLogMonitorJobRun theLastRun) {
      this.name = theName;
      this.jobName = theJobName;
      this.configured = theConfigured;
      this.lastSequenceProcessed = theLastSequenceProcessed;
      this.entriesBehind = theEntriesBehind;
      this.oldestUnprocessedAge = theOldestUnprocessedAge;
      this.lastProgress = theLastProgress;
      this.entriesDeletedBeforeProcessed = theEntriesDeletedBeforeProcessed;
      this.lastRun = theLastRun;
    }

    /**
     * @return consumer name
     */
    public String getName() {
      return this.name;
    }

    /**
     * @return loader job name of the consumer
     */
    public String getJobName() {
      return this.jobName;
    }

    /**
     * @return if there is still a changeLog.consumer.name.class config for it
     */
    public boolean isConfigured() {
      return this.configured;
    }

    /**
     * @return last sequence processed, or null
     */
    public Long getLastSequenceProcessed() {
      return this.lastSequenceProcessed;
    }

    /**
     * @return change log entries after the last one processed
     */
    public long getEntriesBehind() {
      return this.entriesBehind;
    }

    /**
     * @return how long ago the oldest unprocessed entry happened, or null
     */
    public String getOldestUnprocessedAge() {
      return this.oldestUnprocessedAge;
    }

    /**
     * @return when the consumer last moved forward, or null
     */
    public String getLastProgress() {
      return this.lastProgress;
    }

    /**
     * @return if unprocessed entries were deleted for being too old
     */
    public boolean isEntriesDeletedBeforeProcessed() {
      return this.entriesDeletedBeforeProcessed;
    }

    /**
     * @return latest run of the consumer job, or null
     */
    public ChangeLogMonitorJobRun getLastRun() {
      return this.lastRun;
    }
  }

  /**
   * pending changes of one change log type
   */
  public static class ChangeLogMonitorPendingType {

    /** change log category, e.g. membership */
    private final String category;

    /** change log action, e.g. addMembership */
    private final String action;

    /** pending changes of this type */
    private final long count;

    /** how long ago the oldest pending change of this type happened */
    private final String oldestAge;

    /**
     * @param theCategory change log category
     * @param theAction change log action
     * @param theCount pending changes of this type
     * @param theOldestAge how long ago the oldest pending change of this type happened
     */
    public ChangeLogMonitorPendingType(String theCategory, String theAction, long theCount, String theOldestAge) {
      this.category = theCategory;
      this.action = theAction;
      this.count = theCount;
      this.oldestAge = theOldestAge;
    }

    /**
     * @return change log category
     */
    public String getCategory() {
      return this.category;
    }

    /**
     * @return change log action
     */
    public String getAction() {
      return this.action;
    }

    /**
     * @return pending changes of this type
     */
    public long getCount() {
      return this.count;
    }

    /**
     * @return how long ago the oldest pending change of this type happened
     */
    public String getOldestAge() {
      return this.oldestAge;
    }
  }

  /**
   * a group or folder with pending membership or privilege changes
   */
  public static class ChangeLogMonitorPendingOwner {

    /** group or folder name */
    private final String name;

    /** pending membership changes */
    private long membershipCount;

    /** pending privilege changes */
    private long privilegeCount;

    /**
     * @param theName group or folder name
     */
    public ChangeLogMonitorPendingOwner(String theName) {
      this.name = theName;
    }

    /**
     * @return group or folder name
     */
    public String getName() {
      return this.name;
    }

    /**
     * @return pending membership changes
     */
    public long getMembershipCount() {
      return this.membershipCount;
    }

    /**
     * @param count pending membership changes to add
     */
    public void addMembershipCount(long count) {
      this.membershipCount += count;
    }

    /**
     * @return pending privilege changes
     */
    public long getPrivilegeCount() {
      return this.privilegeCount;
    }

    /**
     * @param count pending privilege changes to add
     */
    public void addPrivilegeCount(long count) {
      this.privilegeCount += count;
    }

    /**
     * @return pending membership and privilege changes
     */
    public long getTotalCount() {
      return this.membershipCount + this.privilegeCount;
    }
  }

  /**
   * an action (context) and the pending changes it caused, with its audit entry if there is one
   */
  public static class ChangeLogMonitorPendingSource {

    /** context id of the action */
    private final String contextId;

    /** pending changes the action caused */
    private final long count;

    /** how long ago the first pending change of the action happened */
    private final String firstPendingAge;

    /** audit category and action, or null if not audited */
    private final String auditAction;

    /** audit description, or null */
    private final String description;

    /** e.g. UI, WS, GSH, loader, or null */
    private final String engine;

    /** server that made the change, or null */
    private final String serverHost;

    /** sourceId:subjectId of the user who made the change, or null */
    private final String loggedInSubject;

    /**
     * @param theContextId context id of the action
     * @param theCount pending changes the action caused
     * @param theFirstPendingAge how long ago the first pending change of the action happened
     * @param theAuditAction audit category and action, or null if not audited
     * @param theDescription audit description, or null
     * @param theEngine e.g. UI, WS, GSH, loader, or null
     * @param theServerHost server that made the change, or null
     * @param theLoggedInSubject sourceId:subjectId of the user who made the change, or null
     */
    public ChangeLogMonitorPendingSource(String theContextId, long theCount, String theFirstPendingAge,
        String theAuditAction, String theDescription, String theEngine, String theServerHost, String theLoggedInSubject) {
      this.contextId = theContextId;
      this.count = theCount;
      this.firstPendingAge = theFirstPendingAge;
      this.auditAction = theAuditAction;
      this.description = theDescription;
      this.engine = theEngine;
      this.serverHost = theServerHost;
      this.loggedInSubject = theLoggedInSubject;
    }

    /**
     * @return context id of the action
     */
    public String getContextId() {
      return this.contextId;
    }

    /**
     * @return pending changes the action caused
     */
    public long getCount() {
      return this.count;
    }

    /**
     * @return how long ago the first pending change of the action happened
     */
    public String getFirstPendingAge() {
      return this.firstPendingAge;
    }

    /**
     * @return audit category and action, or null if not audited
     */
    public String getAuditAction() {
      return this.auditAction;
    }

    /**
     * @return audit description, or null
     */
    public String getDescription() {
      return this.description;
    }

    /**
     * @return e.g. UI, WS, GSH, loader, or null
     */
    public String getEngine() {
      return this.engine;
    }

    /**
     * @return server that made the change, or null
     */
    public String getServerHost() {
      return this.serverHost;
    }

    /**
     * @return sourceId:subjectId of the user who made the change, or null
     */
    public String getLoggedInSubject() {
      return this.loggedInSubject;
    }
  }
}
