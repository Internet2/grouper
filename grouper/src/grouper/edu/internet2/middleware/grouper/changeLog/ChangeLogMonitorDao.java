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
package edu.internet2.middleware.grouper.changeLog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * GRP-7510: queries for the change log monitor: how big and old grouper_change_log_entry_temp is,
 * the range of grouper_change_log_entry, what is pending, and the counts the change log daemons
 * log in grouper_loader_log.  these do no privilege checks, callers must only show them to
 * Grouper sysadmins since pending changes name groups and subjects
 */
public class ChangeLogMonitorDao {

  /** job name prefix of the loader log rows long running jobs write every minute or so, see ChangeLogTempToEntity */
  public static final String LOADER_LOG_SUBJOB_PREFIX = "subjobFor_";

  /**
   * @param jobName e.g. CHANGE_LOG_changeLogTempToChangeLog
   * @param longRunning if the job is long running, i.e. logs a sub job every minute or so and adds those up in its overall row
   * @return the job name of the loader log rows that have the counts of each run, so they are only counted once
   */
  public static String loaderLogJobName(String jobName, boolean longRunning) {
    return longRunning ? LOADER_LOG_SUBJOB_PREFIX + jobName : jobName;
  }

  /**
   * @return number of rows in grouper_change_log_entry_temp
   */
  public static long retrieveTempCount() {
    Long count = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from ChangeLogEntryTemp").setCacheable(false).uniqueResult(Long.class);
    return count == null ? 0 : count;
  }

  /**
   * @return created on (micros since 1970) of the oldest row in grouper_change_log_entry_temp, or null if empty
   */
  public static Long retrieveTempOldestCreatedOnMicros() {
    return HibernateSession.byHqlStatic().createQuery(
        "select min(cle.createdOnDb) from ChangeLogEntryTemp cle").setCacheable(false).uniqueResult(Long.class);
  }

  /**
   * @return created on (micros since 1970) of the oldest row in grouper_change_log_entry, or null if empty
   */
  public static Long retrieveEntryOldestCreatedOnMicros() {
    return HibernateSession.byHqlStatic().createQuery(
        "select min(cle.createdOnDb) from ChangeLogEntryEntity cle").setCacheable(false).uniqueResult(Long.class);
  }

  /**
   * entries before this were deleted by the delete old records daemon
   * @return lowest sequence number in grouper_change_log_entry, or null if empty
   */
  public static Long retrieveEntryMinSequenceNumber() {
    return HibernateSession.byHqlStatic().createQuery(
        "select min(cle.sequenceNumber) from ChangeLogEntryEntity cle").setCacheable(false).uniqueResult(Long.class);
  }

  /**
   * @param loaderLogJobName job name of the loader log rows, see loaderLogJobName()
   * @param startedSinceMillis
   * @return sum of the total count of the loader log rows of the job started since then
   */
  public static long retrieveLoaderLogTotalCountSince(String loaderLogJobName, long startedSinceMillis) {
    Long total = HibernateSession.byHqlStatic().createQuery(
        "select sum(gll.totalCount) from Hib3GrouperLoaderLog gll where gll.jobName = :theJobName and gll.startedTime >= :theStartedTime")
        .setCacheable(false)
        .setString("theJobName", loaderLogJobName)
        .setTimestamp("theStartedTime", new Timestamp(startedSinceMillis))
        .uniqueResult(Long.class);
    return total == null ? 0 : total;
  }

  /**
   * @param jobName e.g. CHANGE_LOG_consumer_compositeMemberships
   * @return latest updated loader log row of the job or its sub jobs, or null if there is none
   */
  public static Hib3GrouperLoaderLog retrieveLatestLoaderLog(String jobName) {
    List<Hib3GrouperLoaderLog> loaderLogs = HibernateSession.byHqlStatic().createQuery(
        "from Hib3GrouperLoaderLog gll where gll.jobName = :theJobName or gll.jobName = :theSubjobName order by gll.lastUpdated desc")
        .setCacheable(false)
        .setString("theJobName", jobName)
        .setString("theSubjobName", LOADER_LOG_SUBJOB_PREFIX + jobName)
        .options(new QueryOptions().paging(1, 1, false))
        .list(Hib3GrouperLoaderLog.class);
    return GrouperUtil.length(loaderLogs) == 0 ? null : loaderLogs.get(0);
  }

  /**
   * @param loaderLogJobNames job names of the loader log rows, see loaderLogJobName()
   * @param startedSinceMillis
   * @return a row per loader log row of the jobs started since then: job name (String), started time (Timestamp), total count (Integer)
   */
  public static List<Object[]> retrieveLoaderLogTotalCounts(Collection<String> loaderLogJobNames, long startedSinceMillis) {
    if (GrouperUtil.length(loaderLogJobNames) == 0) {
      return new ArrayList<Object[]>();
    }
    List<String> jobNames = new ArrayList<String>(loaderLogJobNames);
    StringBuilder hql = new StringBuilder("select gll.jobName, gll.startedTime, gll.totalCount from Hib3GrouperLoaderLog gll "
        + "where gll.startedTime >= :theStartedTime and (");
    for (int i = 0; i < jobNames.size(); i++) {
      if (i > 0) {
        hql.append(" or ");
      }
      hql.append("gll.jobName = :theJobName").append(i);
    }
    hql.append(")");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic().createQuery(hql.toString()).setCacheable(false)
        .setTimestamp("theStartedTime", new Timestamp(startedSinceMillis));
    for (int i = 0; i < jobNames.size(); i++) {
      byHqlStatic.setString("theJobName" + i, jobNames.get(i));
    }
    return byHqlStatic.list(Object[].class);
  }

  /**
   * group by over all of grouper_change_log_entry_temp
   * @return a row per change log type with pending rows: change log type id (String), count (Long), oldest created on micros (Long)
   */
  public static List<Object[]> retrieveTempCountsByType() {
    return HibernateSession.byHqlStatic().createQuery(
        "select cle.changeLogTypeId, count(*), min(cle.createdOnDb) from ChangeLogEntryTemp cle group by cle.changeLogTypeId")
        .setCacheable(false).list(Object[].class);
  }

  /**
   * the most common values of one label of one change log type in grouper_change_log_entry_temp, e.g. the
   * groups with the most pending membership adds
   * @param changeLogTypeBuiltin
   * @param label label of the type, e.g. groupName
   * @param maxRows
   * @return a row per value, most first: value (String), count (Long).  empty if the type does not have the label
   */
  public static List<Object[]> retrieveTempCountsByLabel(ChangeLogTypeBuiltin changeLogTypeBuiltin, String label, int maxRows) {

    ChangeLogType changeLogType = changeLogTypeBuiltin.getChangeLogType();
    String field = changeLogType == null ? null : changeLogType.retrieveChangeLogEntryFieldForLabel(label);

    // this goes in the query, make sure it is a column
    if (field == null || !field.matches("^string[0-9]{2}$")) {
      return new ArrayList<Object[]>();
    }

    return HibernateSession.byHqlStatic().createQuery(
        "select cle." + field + ", count(*) from ChangeLogEntryTemp cle where cle.changeLogTypeId = :theChangeLogTypeId "
        + "group by cle." + field + " order by count(*) desc")
        .setCacheable(false)
        .setString("theChangeLogTypeId", changeLogType.getId())
        .options(new QueryOptions().paging(maxRows, 1, false))
        .list(Object[].class);
  }

  /**
   * the actions (context ids) that caused the most rows in grouper_change_log_entry_temp
   * @param maxRows
   * @return a row per context id, most first: context id (String, can be null), count (Long), oldest created on micros (Long)
   */
  public static List<Object[]> retrieveTempCountsByContextId(int maxRows) {
    return HibernateSession.byHqlStatic().createQuery(
        "select cle.contextId, count(*), min(cle.createdOnDb) from ChangeLogEntryTemp cle group by cle.contextId order by count(*) desc")
        .setCacheable(false)
        .options(new QueryOptions().paging(maxRows, 1, false))
        .list(Object[].class);
  }

  /**
   * @param contextId
   * @return an audit entry of the action, or null if it was not audited
   */
  public static AuditEntry retrieveAuditEntryByContextId(String contextId) {
    List<AuditEntry> auditEntries = HibernateSession.byHqlStatic().createQuery(
        "from AuditEntry ae where ae.contextId = :theContextId")
        .setCacheable(false)
        .setString("theContextId", contextId)
        .options(new QueryOptions().paging(1, 1, false))
        .list(AuditEntry.class);
    return GrouperUtil.length(auditEntries) == 0 ? null : auditEntries.get(0);
  }
}
