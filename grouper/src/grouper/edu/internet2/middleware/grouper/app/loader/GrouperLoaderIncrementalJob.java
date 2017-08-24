/**
 * Copyright 2016 Internet2
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
package edu.internet2.middleware.grouper.app.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.Trigger.TriggerState;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 */
@DisallowConcurrentExecution
public class GrouperLoaderIncrementalJob implements Job {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderIncrementalJob.class);
    
  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    String jobName = context.getJobDetail().getKey().getName();
    GrouperSession grouperSession = null;

    try {
      grouperSession = GrouperSession.startRootSession();
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.LOADER, false, true);
      
      if (GrouperLoader.isJobRunning(jobName)) {
        LOG.warn("Data in grouper_loader_log suggests that job " + jobName + " is currently running already.  Aborting this run.");
        return;
      }
      
      runJob(grouperSession, jobName);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * @param hib3GrouperloaderLog
   * @param throwException 
   * @param startTime
   */
  private static void storeLogInDb(Hib3GrouperLoaderLog hib3GrouperloaderLog,
      boolean throwException, long startTime) {
    //store this safely
    try {
      
      long endTime = System.currentTimeMillis();
      hib3GrouperloaderLog.setEndedTime(new Timestamp(endTime));
      hib3GrouperloaderLog.setMillis((int)(endTime-startTime));
      
      hib3GrouperloaderLog.store();
      
    } catch (RuntimeException e) {
      LOG.error("Problem storing final log", e);
      //dont preempt an existing exception
      if (throwException) {
        throw e;
      }
    }
  }
  
  /**
   * @param grouperSession 
   * @param jobName
   * @throws JobExecutionException 
   */
  public static void runJob(GrouperSession grouperSession, String jobName) throws JobExecutionException {
    long startTime = System.currentTimeMillis();
    final Hib3GrouperLoaderLog hib3GrouperloaderLog = new Hib3GrouperLoaderLog();
    
    boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap("overallLog");

    try {
      hib3GrouperloaderLog.setJobName(jobName);
      hib3GrouperloaderLog.setHost(GrouperUtil.hostname());
      hib3GrouperloaderLog.setStartedTime(new Timestamp(startTime));
      hib3GrouperloaderLog.setJobType("OTHER_JOB");
      hib3GrouperloaderLog.setStatus(GrouperLoaderStatus.STARTED.name());
      hib3GrouperloaderLog.store();
      
      String jobProperty = jobName.replaceFirst("^OTHER_JOB_", "");

      String databaseName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobProperty + ".databaseName");
      final String tableName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobProperty + ".tableName");
      int fullSyncThreshold = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob." + jobProperty + ".fullSyncThreshold", 100);

      boolean useThreads = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.incrementalThreads", true);
      int threadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.incrementalThreadPoolSize", 10);

      final GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(databaseName);
      
      Connection connection = null;
      Statement statement = null;
      ResultSet resultSet = null;
      String query = "select * from " + tableName + " where completed_timestamp is null order by timestamp";
      List<String> columnNames = new ArrayList<String>();
      
      try {
        connection = grouperLoaderDb.connection();
        connection.setAutoCommit(false);
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);

        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

        int columnCount = resultSetMetaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
          columnNames.add(resultSetMetaData.getColumnLabel(i + 1));
        }
        
        Map<String, Map<MultiKey, Row>> rowsByGroup = new LinkedHashMap<String, Map<MultiKey, Row>>();
        
        while (resultSet.next()) {
          long id = resultSet.getLong("id");
          long timestamp = resultSet.getLong("timestamp");
          String loaderGroupName = resultSet.getString("loader_group_name");

          String subjectId = null;
          String subjectIdentifier = null;
          String subjectIdOrIdentifier = null;
          String sourceId = null;
          String subjectColumn = null;
          String subjectValue = null;
          
          for (int i = 0; i < columnCount; i++) {
            if (columnNames.get(i).equalsIgnoreCase("subject_id")) {
              subjectId = resultSet.getString(i + 1);
              
              if (!StringUtils.isEmpty(subjectId)) {
                subjectColumn = "subject_id";
                subjectValue = subjectId;
              }
            } else if (columnNames.get(i).equalsIgnoreCase("subject_identifier")) {
              subjectIdentifier = resultSet.getString(i + 1);
              
              if (!StringUtils.isEmpty(subjectIdentifier)) {
                subjectColumn = "subject_identifier";
                subjectValue = subjectIdentifier;
              }
            } else if (columnNames.get(i).equalsIgnoreCase("subject_id_or_identifier")) {
              subjectIdOrIdentifier = resultSet.getString(i + 1);
              
              if (!StringUtils.isEmpty(subjectIdOrIdentifier)) {
                subjectColumn = "subject_id_or_identifier";
                subjectValue = subjectIdOrIdentifier;
              }
            } else if (columnNames.get(i).equalsIgnoreCase("source_id")) {
              sourceId = resultSet.getString(i + 1);
            }
          }
          
          if (subjectColumn == null) {
            throw new RuntimeException("Didn't find the subject column!");
          }
          
          Row row = new Row(id, timestamp, loaderGroupName, subjectId, subjectIdentifier, subjectIdOrIdentifier, sourceId, subjectColumn, subjectValue);
          MultiKey key = new MultiKey(loaderGroupName, subjectId, subjectIdentifier, subjectIdOrIdentifier, sourceId);
          
          if (!rowsByGroup.containsKey(loaderGroupName)) {
            rowsByGroup.put(loaderGroupName, new LinkedHashMap<MultiKey, Row>());
          }
          
          if (rowsByGroup.get(loaderGroupName).containsKey(key)) {
            // mark row done
            setRowCompleted(connection, tableName, id);
          } else {
            rowsByGroup.get(loaderGroupName).put(key, row);
          }
        }
        
        List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
        List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();
          
        for (String loaderGroupName : rowsByGroup.keySet()) {
          
          final Group loaderGroup = GroupFinder.findByName(grouperSession, loaderGroupName, false);
          
          if (loaderGroup == null) {
            LOG.warn("Loader group " + loaderGroupName + " does not exist.  Marking incremental updates as complete.");
            setAllRowsForGroupCompleted(connection, tableName, loaderGroupName);
            continue;
          }
          
          if (rowsByGroup.get(loaderGroupName).size() >= fullSyncThreshold) {
            LOG.warn("Loader group " + loaderGroupName + " has too many changes.  Threshold=" + fullSyncThreshold + ".  Changes=" + rowsByGroup.get(loaderGroupName).size() + ".  Marking incremental updates as complete and triggering full sync.");
            scheduleJobNow(loaderGroup);
            setAllRowsForGroupCompleted(connection, tableName, loaderGroupName);
            continue;
          }
          
          final String grouperLoaderQuery = GrouperLoaderType.attributeValueOrDefaultOrNull(loaderGroup, GrouperLoader.GROUPER_LOADER_QUERY);
          final String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(loaderGroup, GrouperLoader.GROUPER_LOADER_TYPE);
          final String grouperLoaderAndGroups = GrouperLoaderType.attributeValueOrDefaultOrNull(loaderGroup, GrouperLoader.GROUPER_LOADER_AND_GROUPS);

          final String OVERALL_LOGGER_ID = GrouperLoaderLogger.retrieveOverallId();

          for (final Row row : rowsByGroup.get(loaderGroupName).values()) {

            GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("processOneRow") {
              
              @Override
              public Void callLogic() {
                GrouperLoaderLogger.assignOverallId(OVERALL_LOGGER_ID);
                processOneRow(GrouperSession.staticGrouperSession(), grouperLoaderDb, row, tableName, loaderGroup, grouperLoaderQuery, grouperLoaderType, grouperLoaderAndGroups, hib3GrouperloaderLog);
                return null;
              }
            };
           
    
            if (!useThreads || threadPoolSize <= 1){
              grouperCallable.callLogic();
            } else {
              GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
              futures.add(future);          
              GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
            }            
          }
        }
        
        GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
        GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
        
        deleteRowsCompleted(connection, tableName);
      } catch (SQLException se) {
        throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
      } finally {
        GrouperUtil.closeQuietly(resultSet);
        GrouperUtil.closeQuietly(statement);
        GrouperUtil.closeQuietly(connection);
      }
      
      hib3GrouperloaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      storeLogInDb(hib3GrouperloaderLog, true, startTime);
    } catch (Exception e) {
      
      GrouperLoaderLogger.addLogEntry("overallLog", "exception", ExceptionUtils.getFullStackTrace(e));

      LOG.error("Error running job", e);
      hib3GrouperloaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
      hib3GrouperloaderLog.appendJobMessage(ExceptionUtils.getFullStackTrace(e));
      
      if (!(e instanceof JobExecutionException)) {
        e = new JobExecutionException(e);
      }
      JobExecutionException jobExecutionException = (JobExecutionException)e;
      storeLogInDb(hib3GrouperloaderLog, false, startTime);
      throw jobExecutionException;
    } finally {
      if (loggerInitted) {
        GrouperLoaderLogger.doTheLogging("overallLog");
      }
    }
  }
  
  private static void setRowCompleted(Connection connection, String tableName, long id) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("update " + tableName + " set completed_timestamp = ? where id = ?");
      statement.setLong(1, System.currentTimeMillis());
      statement.setLong(2, id);
      statement.executeUpdate();
      connection.commit();
    } finally {
      GrouperUtil.closeQuietly(statement);
    }
  }
  
  private static void setAllRowsForGroupCompleted(Connection connection, String tableName, String loaderGroupName) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("update " + tableName + " set completed_timestamp = ? where loader_group_name = ? and completed_timestamp is null");
      statement.setLong(1, System.currentTimeMillis());
      statement.setString(2, loaderGroupName);
      statement.executeUpdate();
      connection.commit();
    } finally {
      GrouperUtil.closeQuietly(statement);
    }
  }
  
  private static void deleteRowsCompleted(Connection connection, String tableName) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("delete from " + tableName + " where completed_timestamp is not null and completed_timestamp <= ?");
      statement.setLong(1, System.currentTimeMillis() - 1000 * 60 * 60 * 24L);
      statement.executeUpdate();
      connection.commit();
    } finally {
      GrouperUtil.closeQuietly(statement);
    }
  }
  
  private synchronized static void scheduleJobNow(Group loaderGroup) throws SchedulerException {
    String grouperLoaderType = GrouperLoaderType.attributeValueOrDefaultOrNull(loaderGroup, GrouperLoader.GROUPER_LOADER_TYPE);
    GrouperLoaderType grouperLoaderTypeEnum = GrouperLoaderType.valueOfIgnoreCase(grouperLoaderType, true);
    String jobName = grouperLoaderTypeEnum.name() + "__" + loaderGroup.getName() + "__" + loaderGroup.getUuid();

    Scheduler scheduler = GrouperLoader.schedulerFactory().getScheduler();
    List<? extends Trigger> currentTriggers = scheduler.getTriggersOfJob(new JobKey(jobName));
    boolean alreadyScheduled = false;
    for (Trigger currentTrigger : currentTriggers) {
      TriggerKey currentTriggerKey = currentTrigger.getKey();
      if (TriggerState.BLOCKED == scheduler.getTriggerState(currentTriggerKey)) {
        alreadyScheduled = true;
        break;
      }
    }
    
    // what if it doesn't get moved to the blocked state quick enough.  there really shouldn't be more than 3 triggers.  look at this again later.
    if (currentTriggers.size() > 2) {
      alreadyScheduled = true;
    }
    
    if (!alreadyScheduled) {
      scheduler.triggerJob(new JobKey(jobName));
      LOG.info("Scheduled full sync job for group " + loaderGroup.getName() + ".");
    } else {
      LOG.info("Not scheduling full sync job for group " + loaderGroup.getName() + " because pending jobs already exist.");
    }
  }
  
  
  private static void processOneRow(GrouperSession grouperSession, GrouperLoaderDb grouperLoaderDb, Row row, String tableName, Group loaderGroup, String grouperLoaderQuery, String grouperLoaderType, String grouperLoaderAndGroups, Hib3GrouperLoaderLog hib3GrouperloaderLog) {

    Connection connection = null;

    try {
      connection = grouperLoaderDb.connection();
      connection.setAutoCommit(false);
      
      long id = row.getId();
      String subjectId = row.getSubjectId();
      String subjectIdentifier = row.getSubjectIdentifier();
      String subjectIdOrIdentifier = row.getSubjectIdOrIdentifier();
      String sourceId = row.getSourceId();
      String subjectColumn = row.getSubjectColumn();
      String subjectValue = row.getSubjectValue();
        
      Subject subject = null;
  
      if (subjectId != null) {
        if (sourceId == null) {
          subject = SubjectFinder.findById(subjectId, false);
        } else {
          subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
        }
      } else if (subjectIdentifier != null) {
        if (sourceId == null) {
          subject = SubjectFinder.findByIdentifier(subjectIdentifier, false);
        } else {
          subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, sourceId, false);
        }
      } else {
        if (sourceId == null) {
          subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
        } else {
          subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectIdOrIdentifier, sourceId, false);
        }
      }
      
      if (subject == null) {
        LOG.warn("Found unresolvable subject: subjectColumn=" + subjectColumn + ", subjectValue=" + subjectValue);
        synchronized (hib3GrouperloaderLog) {
          hib3GrouperloaderLog.addUnresolvableSubjectCount(1);
        }
        setRowCompleted(connection, tableName, id);
        return;
      }
      
      if (GrouperLoaderType.SQL_SIMPLE.name().equals(grouperLoaderType)) {
        String loaderQueryForUser = "select 1 from (" + grouperLoaderQuery + ") innerQuery where " + subjectColumn + " = ?";
        if (sourceId != null) {
          loaderQueryForUser += " and source_id = ?";
        }
        
        PreparedStatement statement2 = null;
        ResultSet resultSet2 = null;
        boolean isMemberInSource = false;
  
        try {
          statement2 = connection.prepareStatement(loaderQueryForUser);
          statement2.setString(1, subjectValue);
          if (sourceId != null) {
            statement2.setString(2, sourceId);
          }
          resultSet2 = statement2.executeQuery();
          if (resultSet2.next()) {
            isMemberInSource = true;
          }
        } finally {
          GrouperUtil.closeQuietly(resultSet2);
          GrouperUtil.closeQuietly(statement2);
        }
        
        boolean isMemberInGroup = loaderGroup.hasImmediateMember(subject);
        
        boolean isMemberInSourceAfterAndGroupsConsideration = isMemberInSource;
        
        if (isMemberInSource) {
          if (!StringUtils.isBlank(grouperLoaderAndGroups)) {
            String[] groupNames = GrouperUtil.splitTrim(grouperLoaderAndGroups, ",");
            
            for (String groupName : groupNames) {
              Group andGroup = GroupFinder.findByName(grouperSession, groupName, true);
              if (!andGroup.hasMember(subject)) {
                isMemberInSourceAfterAndGroupsConsideration = false;
                break;
              }
            }
          }
        }
        
        boolean added = false;
        boolean removed = false;
        
        if (isMemberInGroup && !isMemberInSourceAfterAndGroupsConsideration) {
          loaderGroup.deleteMember(subject);
          removed = true;
          synchronized (hib3GrouperloaderLog) {
            hib3GrouperloaderLog.addDeleteCount(1);
          }
        } else if (!isMemberInGroup && isMemberInSourceAfterAndGroupsConsideration) {
          loaderGroup.addMember(subject);
          added = true;
          synchronized (hib3GrouperloaderLog) {
            hib3GrouperloaderLog.addInsertCount(1);
          }
        }
        
        if (added || removed) {
          GrouperLoaderLogger.initializeThreadLocalMap("membershipManagement");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "groupName", loaderGroup.getName());
          GrouperLoaderLogger.addLogEntry("membershipManagement", "subject", GrouperUtil.subjectToString(subject));
          GrouperLoaderLogger.addLogEntry("membershipManagement", "operation", added ? "add" : "remove");
          GrouperLoaderLogger.addLogEntry("membershipManagement", "reason", "incremental");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "success", true);

          GrouperLoaderLogger.doTheLogging("membershipManagement");

        }
        
      } else if (GrouperLoaderType.SQL_GROUP_LIST.name().equals(grouperLoaderType)) {
        String grouperLoaderGroupsLike = GrouperLoaderType.attributeValueOrDefaultOrNull(loaderGroup, GrouperLoader.GROUPER_LOADER_GROUPS_LIKE);
        if (StringUtils.isEmpty(grouperLoaderGroupsLike)) {
          throw new RuntimeException("grouperLoaderGroupsLike is required for SQL_GROUP_LIST");
        }
  
        String loaderQueryForUser = "select group_name from (" + grouperLoaderQuery + ") innerQuery where " + subjectColumn + " = ?";
        if (sourceId != null) {
          loaderQueryForUser += " and source_id = ?";
        }
        
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        Set<String> membershipsInSource = new LinkedHashSet<String>();
  
        try {
          statement = connection.prepareStatement(loaderQueryForUser);
          statement.setString(1, subjectValue);
          if (sourceId != null) {
            statement.setString(2, sourceId);
          }
          resultSet = statement.executeQuery();
          while (resultSet.next()) {
            String groupName = resultSet.getString("group_name");
            if (!StringUtils.isEmpty(groupName)) {
              membershipsInSource.add(groupName);
            }
          }
        } finally {
          GrouperUtil.closeQuietly(resultSet);
          GrouperUtil.closeQuietly(statement);
        }
        
        String sql = "select g.nameDb "
            + " from Member m, MembershipEntry ms, Group g "
            + " where ms.ownerGroupId = g.uuid and ms.memberUuid = m.uuid "
            + " and g.nameDb like :sqlLike "
            + " and m.subjectIdDb like :subjectId "
            + " and m.subjectSourceIdDb like :subjectSourceId "
            + " and ms.type = 'immediate' and ms.enabledDb = 'T' "
            + " and ms.fieldId = '" + Group.getDefaultList().getUuid() + "'";
        
        Set<String> membershipsInGrouper = HibernateSession.byHqlStatic()
          .createQuery(sql)
          .setString("sqlLike", grouperLoaderGroupsLike)
          .setString("subjectId", subject.getId())
          .setString("subjectSourceId", subject.getSourceId())
          .listSet(String.class);
        
        Set<String> membershipsInSourceAfterAndGroupsConsideration = new LinkedHashSet<String>(membershipsInSource);
        if (!StringUtils.isBlank(grouperLoaderAndGroups)) {
          String[] andGroupNames = GrouperUtil.splitTrim(grouperLoaderAndGroups, ",");
          for (String groupName : membershipsInSource) {
            for (String andGroupName : andGroupNames) {
              Group andGroup = GroupFinder.findByName(grouperSession, andGroupName, true);
              if (!andGroup.hasMember(subject)) {
                membershipsInSourceAfterAndGroupsConsideration.remove(groupName);
                break;
              }
            }
          }
        }
        
        Set<String> membershipsToAdd = new LinkedHashSet<String>(membershipsInSourceAfterAndGroupsConsideration);
        Set<String> membershipsToRemove = new LinkedHashSet<String>(membershipsInGrouper);
        membershipsToAdd.removeAll(membershipsInGrouper);
        membershipsToRemove.removeAll(membershipsInSourceAfterAndGroupsConsideration);
        
        for (String groupName : membershipsToAdd) {
          Group theGroup = GroupFinder.findByName(grouperSession, groupName, false);
          
          if (theGroup == null) {
            // if group doesn't exist, full sync and set completion time
            scheduleJobNow(loaderGroup);
            
            // just setting this one row completed instead of all from this group list just in case the full sync takes a long time
            setRowCompleted(connection, tableName, id);
            continue;
          }
          theGroup.addMember(subject);
          
          GrouperLoaderLogger.initializeThreadLocalMap("membershipManagement");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "groupName", loaderGroup.getName());
          GrouperLoaderLogger.addLogEntry("membershipManagement", "subject", GrouperUtil.subjectToString(subject));
          GrouperLoaderLogger.addLogEntry("membershipManagement", "operation", "add");
          GrouperLoaderLogger.addLogEntry("membershipManagement", "reason", "incremental");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "success", true);

          GrouperLoaderLogger.doTheLogging("membershipManagement");

          synchronized (hib3GrouperloaderLog) {
            hib3GrouperloaderLog.addInsertCount(1);
          }
        }
        
        for (String groupName : membershipsToRemove) {
          Group theGroup = GroupFinder.findByName(grouperSession, groupName, true);
          theGroup.deleteMember(subject);

          GrouperLoaderLogger.initializeThreadLocalMap("membershipManagement");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "groupName", loaderGroup.getName());
          GrouperLoaderLogger.addLogEntry("membershipManagement", "subject", GrouperUtil.subjectToString(subject));
          GrouperLoaderLogger.addLogEntry("membershipManagement", "operation", "remove");
          GrouperLoaderLogger.addLogEntry("membershipManagement", "reason", "incremental");

          GrouperLoaderLogger.addLogEntry("membershipManagement", "success", true);

          GrouperLoaderLogger.doTheLogging("membershipManagement");

          synchronized (hib3GrouperloaderLog) {
            hib3GrouperloaderLog.addDeleteCount(1);
          }
        }
      } else {
        throw new RuntimeException("Unsupported loader type: " + grouperLoaderType);
      }
      
      setRowCompleted(connection, tableName, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperUtil.closeQuietly(connection);
    }
  }
  
  /**
   *
   */
  public static class Row {
    private long id;
    private long timestamp;
    private String loaderGroupName;

    private String subjectId;
    private String subjectIdentifier;
    private String subjectIdOrIdentifier;
    private String sourceId;
    private String subjectColumn;
    private String subjectValue;
    
    
    /**
     * @param id
     * @param timestamp
     * @param loaderGroupName
     * @param subjectId
     * @param subjectIdentifier
     * @param subjectIdOrIdentifier
     * @param sourceId
     * @param subjectColumn
     * @param subjectValue
     */
    public Row(long id, long timestamp, String loaderGroupName, String subjectId,
        String subjectIdentifier, String subjectIdOrIdentifier, String sourceId,
        String subjectColumn, String subjectValue) {
      super();
      this.id = id;
      this.timestamp = timestamp;
      this.loaderGroupName = loaderGroupName;
      this.subjectId = subjectId;
      this.subjectIdentifier = subjectIdentifier;
      this.subjectIdOrIdentifier = subjectIdOrIdentifier;
      this.sourceId = sourceId;
      this.subjectColumn = subjectColumn;
      this.subjectValue = subjectValue;
    }

    /**
     * @return the id
     */
    public long getId() {
      return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(long id) {
      this.id = id;
    }
    
    /**
     * @return the timestamp
     */
    public long getTimestamp() {
      return timestamp;
    }
    
    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
    
    /**
     * @return the loaderGroupName
     */
    public String getLoaderGroupName() {
      return loaderGroupName;
    }
    
    /**
     * @param loaderGroupName the loaderGroupName to set
     */
    public void setLoaderGroupName(String loaderGroupName) {
      this.loaderGroupName = loaderGroupName;
    }
    
    /**
     * @return the subjectId
     */
    public String getSubjectId() {
      return subjectId;
    }
    
    /**
     * @param subjectId the subjectId to set
     */
    public void setSubjectId(String subjectId) {
      this.subjectId = subjectId;
    }
    
    /**
     * @return the subjectIdentifier
     */
    public String getSubjectIdentifier() {
      return subjectIdentifier;
    }
    
    /**
     * @param subjectIdentifier the subjectIdentifier to set
     */
    public void setSubjectIdentifier(String subjectIdentifier) {
      this.subjectIdentifier = subjectIdentifier;
    }
    
    /**
     * @return the subjectIdOrIdentifier
     */
    public String getSubjectIdOrIdentifier() {
      return subjectIdOrIdentifier;
    }
    
    /**
     * @param subjectIdOrIdentifier the subjectIdOrIdentifier to set
     */
    public void setSubjectIdOrIdentifier(String subjectIdOrIdentifier) {
      this.subjectIdOrIdentifier = subjectIdOrIdentifier;
    }
    
    /**
     * @return the sourceId
     */
    public String getSourceId() {
      return sourceId;
    }
    
    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(String sourceId) {
      this.sourceId = sourceId;
    }

    
    /**
     * @return the subjectColumn
     */
    public String getSubjectColumn() {
      return subjectColumn;
    }

    
    /**
     * @param subjectColumn the subjectColumn to set
     */
    public void setSubjectColumn(String subjectColumn) {
      this.subjectColumn = subjectColumn;
    }

    
    /**
     * @return the subjectValue
     */
    public String getSubjectValue() {
      return subjectValue;
    }

    
    /**
     * @param subjectValue the subjectValue to set
     */
    public void setSubjectValue(String subjectValue) {
      this.subjectValue = subjectValue;
    }
  }
}
