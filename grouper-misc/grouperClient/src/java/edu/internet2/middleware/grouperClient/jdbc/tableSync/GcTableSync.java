/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.time.DurationFormatUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * sync a table
 */
public class GcTableSync {

  /**
   * keep the latest incremental value when started on full
   */
  private Long latestIncrementalValueBeforeStarted;
  
  /**
   * keep the latest incremental value when started on full
   * @return the number
   */
  public Long getLatestIncrementalValueBeforeStarted() {
    return this.latestIncrementalValueBeforeStarted;
  }

  /**
   * keep the latest incremental value when started on full
   * @param latestIncrementalValueBeforeStarted1
   */
  public void setLatestIncrementalValueBeforeStarted(
      Long latestIncrementalValueBeforeStarted1) {
    this.latestIncrementalValueBeforeStarted = latestIncrementalValueBeforeStarted1;
  }

  /**
   * millis since 1970 when the sync started
   */
  private long millisWhenSyncStarted = -1;

  /**
   * millis since 1970 when the sync started
   * @return when started
   */
  public long getMillisWhenSyncStarted() {
    return this.millisWhenSyncStarted;
  }

  /**
   * millis since 1970 when the sync started
   * @param millisWhenSyncStarted1
   */
  public void setMillisWhenSyncStarted(long millisWhenSyncStarted1) {
    this.millisWhenSyncStarted = millisWhenSyncStarted1;
  }

  /**
   * output object
   */
  private GcTableSyncOutput gcTableSyncOutput;
  
  /**
   * output object
   * @return output
   */
  public GcTableSyncOutput getGcTableSyncOutput() {
    return this.gcTableSyncOutput;
  }

  /**
   * output object
   * @param gcTableSyncOutput1
   */
  public void setGcTableSyncOutput(GcTableSyncOutput gcTableSyncOutput1) {
    this.gcTableSyncOutput = gcTableSyncOutput1;
  }

  /**
   * log every minute
   */
  private long lastLog = System.currentTimeMillis();
  
  /**
   * data bean in the from table copying data from
   */
  private GcTableSyncTableBean dataBeanFrom;
  
  /**
   * data bean in the from table
   * @return the fromData
   */
  public GcTableSyncTableBean getDataBeanFrom() {
    return this.dataBeanFrom;
  }
  
  /**
   * data in the from table
   * @param fromData1 the fromData to set
   */
  public void setDataBeanFrom(GcTableSyncTableBean fromData1) {
    this.dataBeanFrom = fromData1;
  }

  /**
   * data in the to table where copying data to
   */
  private GcTableSyncTableBean dataBeanTo;
  
  /**
   * data in the to table
   * @return the toData
   */
  public GcTableSyncTableBean getDataBeanTo() {
    return this.dataBeanTo;
  }
  
  /**
   * data in the to table
   * @param toData1 the toData to set
   */
  public void setDataBeanTo(GcTableSyncTableBean toData1) {
    this.dataBeanTo = toData1;
  }

  /**
   * log for this sync
   */
  private GcGrouperSyncLog gcGrouperSyncLog;

  /**
   * log for this sync
   * @return
   */
  public GcGrouperSyncLog getGcGrouperSyncLog() {
    return this.gcGrouperSyncLog;
  }

  /**
   * log for this sync
   * @param gcGrouperSyncLog1
   */
  public void setGcGrouperSyncLog(GcGrouperSyncLog gcGrouperSyncLog1) {
    this.gcGrouperSyncLog = gcGrouperSyncLog1;
  }

  /**
   * provisioning table about this provisioner
   */
  private GcGrouperSync gcGrouperSync;
  
  /**
   * provisioning table about this provisioner
   * @return sync
   */
  public GcGrouperSync getGcGrouperSync() {
    return this.gcGrouperSync;
  }

  /**
   * provisioning table about this provisioner
   * @param gcGrouperSync1
   */
  public void setGcGrouperSync(GcGrouperSync gcGrouperSync1) {
    this.gcGrouperSync = gcGrouperSync1;
  }

  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   */
  private GcGrouperSyncJob gcGrouperSyncJob;
  
  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   * @return job
   */
  public GcGrouperSyncJob getGcGrouperSyncJob() {
    return gcGrouperSyncJob;
  }

  /**
   * provisioning table about this job (full, incremental, etc) in this provisioner
   * @param gcGrouperSyncJob1
   */
  public void setGcGrouperSyncJob(GcGrouperSyncJob gcGrouperSyncJob1) {
    this.gcGrouperSyncJob = gcGrouperSyncJob1;
  }

  /**
   * data in the realtime table which gives events of what to process
   */
  private GcTableSyncTableBean dataBeanRealTime;
  
  /**
   * data in the realtime table
   * @return the dataBeanRealTime
   */
  public GcTableSyncTableBean getDataBeanRealTime() {
    return this.dataBeanRealTime;
  }

  
  /**
   * data in the realtime table
   * @param dataBeanRealTime1 the dataBeanRealTime to set
   */
  public void setDataBeanRealTime(GcTableSyncTableBean dataBeanRealTime1) {
    this.dataBeanRealTime = dataBeanRealTime1;
  }

  /**
   * 
   */
  public GcTableSync() {
  }

  /**
   * debug map for this table sync
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

  /**
   * configuration for this table sync
   */
  private GcTableSyncConfiguration gcTableSyncConfiguration = null;
  
  /**
   * configuration for this table sync
   * @return the gcTableSyncConfiguration
   */
  public GcTableSyncConfiguration getGcTableSyncConfiguration() {
    return this.gcTableSyncConfiguration;
  }

  /**
   * dont re-use instances
   */
  private boolean done = false;
  
  /**
   * pass in the output which will update as it runs
   * @param gcTableSyncOutputArray 
   * 
   */
  public GcTableSyncOutput sync(String configKey, GcTableSyncSubtype gcTableSyncSubtype) {

    if (this.done) {
      throw new RuntimeException("Dont re-use instances of this class: " + GcTableSync.class.getName());
    }
    
    this.millisWhenSyncStarted = System.currentTimeMillis();
    
    this.gcTableSyncOutput = new GcTableSyncOutput();
    
    this.gcTableSyncConfiguration = new GcTableSyncConfiguration();
    this.gcTableSyncConfiguration.configureTableSync(this.debugMap, this, configKey, gcTableSyncSubtype);
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.nanoTime();
    
    GcDbAccess.threadLocalQueryCountReset();
    
    try {

      debugMap.put("finalLog", false);
      
      debugMap.put("state", "init");

      this.gcGrouperSyncHeartbeat.setGcGrouperSyncJob(this.gcGrouperSyncJob);
      this.gcGrouperSyncHeartbeat.setFullSync(gcTableSyncSubtype.isFullSync());
      this.gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {

        @Override
        public void run() {

          logPeriodically(debugMap, GcTableSync.this.gcTableSyncOutput);

          
        }
        
      });
      if (!this.gcGrouperSyncHeartbeat.isStarted()) {
        this.gcGrouperSyncHeartbeat.runHeartbeatThread();
      }

      debugMap.put("sync", GcGrouperSync.SQL_SYNC_ENGINE);
      debugMap.put("provisionerName", this.getGcTableSyncConfiguration().getConfigKey());
      debugMap.put("syncType", this.getGcTableSyncConfiguration().getGcTableSyncSubtype());

      this.dataBeanFrom = new GcTableSyncTableBean(this);
      this.dataBeanFrom.configureMetadata(this.gcTableSyncConfiguration.getDatabaseFrom(), this.gcTableSyncConfiguration.getTableFrom());
      this.dataBeanFrom.getTableMetadata().setConnectionNameOrReadonly(this.gcTableSyncConfiguration.getDatabaseFrom());
      this.dataBeanFrom.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getColumnsString());
      this.dataBeanFrom.getTableMetadata().assignPrimaryKeyColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString());
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getChangeFlagColumnString())) {
        this.dataBeanFrom.getTableMetadata().assignChangeFlagColumn(this.gcTableSyncConfiguration.getChangeFlagColumnString());
      }
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getGroupColumnString())) {
        this.dataBeanFrom.getTableMetadata().assignGroupColumn(this.gcTableSyncConfiguration.getGroupColumnString());
      }
      // if the incremental is there, use it
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getIncrementalAllColumnsColumnString())) {
        this.dataBeanFrom.getTableMetadata().assignIncrementalAllCoumnsColumn(this.gcTableSyncConfiguration.getIncrementalAllColumnsColumnString());
      }
      debugMap.put("databaseFrom", this.getDataBeanFrom().getTableMetadata().getConnectionName());
      debugMap.put("tableFrom", this.getDataBeanFrom().getTableMetadata().getTableName());
      
      this.dataBeanTo = new GcTableSyncTableBean(this);
      this.dataBeanTo.configureMetadata(this.gcTableSyncConfiguration.getDatabaseTo(), this.gcTableSyncConfiguration.getTableTo());
      this.dataBeanTo.getTableMetadata().setConnectionNameOrReadonly(this.gcTableSyncConfiguration.getDatabaseToOrReadonly());
      this.dataBeanTo.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getColumnsString());
      this.dataBeanTo.getTableMetadata().assignPrimaryKeyColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString());
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getChangeFlagColumnString())) {
        this.dataBeanTo.getTableMetadata().assignChangeFlagColumn(this.gcTableSyncConfiguration.getChangeFlagColumnString());
      }
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getGroupColumnString())) {
        this.dataBeanTo.getTableMetadata().assignGroupColumn(this.gcTableSyncConfiguration.getGroupColumnString());
      }
      debugMap.put("databaseTo", this.getDataBeanTo().getTableMetadata().getConnectionName());
      debugMap.put("tableTo", this.getDataBeanTo().getTableMetadata().getTableName());

      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getIncrementalPrimaryKeyTable())) {
        this.dataBeanRealTime = new GcTableSyncTableBean(this);
        this.dataBeanRealTime.configureMetadata(this.gcTableSyncConfiguration.getDatabaseFrom(), this.gcTableSyncConfiguration.getIncrementalPrimaryKeyTable());
        this.dataBeanRealTime.getTableMetadata().setConnectionNameOrReadonly(this.gcTableSyncConfiguration.getDatabaseFrom());
        this.dataBeanRealTime.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString() + ", " + this.gcTableSyncConfiguration.getIncrementalProgressColumnString());
        this.dataBeanRealTime.getTableMetadata().assignPrimaryKeyColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString());
        this.dataBeanRealTime.getTableMetadata().assignIncrementalProgressColumn(this.gcTableSyncConfiguration.getIncrementalProgressColumnString());
        debugMap.put("tableIncremental", this.getDataBeanRealTime().getTableMetadata().getTableName());
      }
      
      // step 0, do we have groups to do?
      if (GrouperClientUtils.length(this.groupIdsToSync) > 0) {
        GcTableSyncSubtype.syncGroupings(debugMap, this, this.groupIdsToSync);
      }
      
      this.convertPrimaryKeysFromMembershipsToPrimaryKeys();
      
      if ((GrouperClientUtils.length(this.primaryKeysToSync) > 0 ) 
          && (this.getGcTableSyncConfiguration().getGcTableSyncSubtype() != GcTableSyncSubtype.incrementalFromIdentifiedPrimaryKeys)) {
        throw new RuntimeException("If passing in primaryKeysToSync, then the sync subtype must be incrementalFromIdentifiedPrimaryKeys");
      }
      if ((GrouperClientUtils.length(this.primaryKeysToSync) == 0 ) 
          && (this.getGcTableSyncConfiguration().getGcTableSyncSubtype() == GcTableSyncSubtype.incrementalFromIdentifiedPrimaryKeys)) {
        throw new RuntimeException("If incrementalFromIdentifiedPrimaryKeys, then you ust pass in primaryKeysToSync");
      }
      
      // step 1
      debugMap.put("state", "retrieveData");
      this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
      
      this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));

      if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
        debugMap.put("interrupted", true);
        debugMap.put("state", "done");
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
        return this.gcTableSyncOutput;
      }
      
      // step 2
      debugMap.put("state", "syncData");
      {
        Integer recordsChanged = this.gcTableSyncConfiguration.getGcTableSyncSubtype().syncData(debugMap, this);
        if (recordsChanged != null) {
          this.gcGrouperSyncLog.setRecordsChanged(recordsChanged);
          this.gcGrouperSyncJob.setLastTimeWorkWasDone(new Timestamp(System.currentTimeMillis()));
        }
      }

      debugMap.put("state", "done");

      // change micros to millis in the logs
      for (String label : debugMap.keySet()) {
        if (label.endsWith("Millis")) {
          Object value = debugMap.get(label);
          if (value instanceof Number) {
            long millis = ((Number)value).longValue()/1000;
            debugMap.put(label, millis);
          }
        }
      }
      
      // try to get the retrieved and updated times
      {  
        long retrieveMillis = 0;
        long syncMillis = 0;

        {
          //  retrieveDataFromMillis: 412, 
          //  retrieveDataToMillis: 420
          Long retrieveDataFromMillis = (Long)debugMap.get("retrieveDataFromMillis");
          Long retrieveDataToMillis = (Long)debugMap.get("retrieveDataToMillis");

          // if we have both then we did this in threads so just take the max
          if (retrieveDataFromMillis != null && retrieveDataToMillis != null) {
            retrieveMillis += Math.max(retrieveDataFromMillis, retrieveDataToMillis);
          } else if (retrieveDataFromMillis != null) {
            retrieveMillis += retrieveDataFromMillis;
          } else if (retrieveDataToMillis != null) {
            retrieveMillis += retrieveDataToMillis;
          }
        }
        {          
          //  selectAllColumnsMillis: 1,
          Long selectAllColumnsMillis = (Long)debugMap.get("selectAllColumnsMillis");
          if (selectAllColumnsMillis != null) {
            retrieveMillis += selectAllColumnsMillis;
          }
        }
        {
          //  retrieveChangeFlagFromMillis: 1
          //  retrieveChangeFlagToMillis: 0
          Long retrieveChangeFlagFromMillis = (Long)debugMap.get("retrieveChangeFlagFromMillis");
          Long retrieveChangeFlagToMillis = (Long)debugMap.get("retrieveChangeFlagToMillis");

          // if we have both then we did this in threads so just take the max
          if (retrieveChangeFlagToMillis != null && retrieveChangeFlagFromMillis != null) {
            retrieveMillis += Math.max(retrieveChangeFlagToMillis, retrieveChangeFlagFromMillis);
          } else if (retrieveChangeFlagToMillis != null) {
            retrieveMillis += retrieveChangeFlagToMillis;
          } else if (retrieveChangeFlagFromMillis != null) {
            retrieveMillis += retrieveChangeFlagFromMillis;
          }
        }
        {
          //  retrieveGroupsFromMillis: 30
          //  retrieveGroupsToMillis: 0
          Long retrieveGroupsFromMillis = (Long)debugMap.get("retrieveGroupsFromMillis");
          Long retrieveGroupsToMillis = (Long)debugMap.get("retrieveGroupsToMillis");

          // if we have both then we did this in threads so just take the max
          if (retrieveGroupsToMillis != null && retrieveGroupsFromMillis != null) {
            retrieveMillis += Math.max(retrieveGroupsToMillis, retrieveGroupsFromMillis);
          } else if (retrieveGroupsToMillis != null) {
            retrieveMillis += retrieveGroupsToMillis;
          } else if (retrieveGroupsFromMillis != null) {
            retrieveMillis += retrieveGroupsFromMillis;
          }
        }
        {          
          //  incrementalChangesMillis: 6, 
          Long incrementalChangesMillis = (Long)debugMap.get("incrementalChangesMillis");
          if (incrementalChangesMillis != null) {
            retrieveMillis += incrementalChangesMillis;
          }
        }
        {          
          //  deletesMillis
          Long deletesMillis = (Long)debugMap.get("deletesMillis");
          if (deletesMillis != null) {
            syncMillis += deletesMillis;
          }
        }
        {          
          //  insertsMillis
          Long insertsMillis = (Long)debugMap.get("insertsMillis");
          if (insertsMillis != null) {
            syncMillis += insertsMillis;
          }
        }
        {          
          //  updatesMillis
          Long updatesMillis = (Long)debugMap.get("updatesMillis");
          if (updatesMillis != null) {
            syncMillis += updatesMillis;
          }
        }
        this.gcTableSyncOutput.setMillisGetData(retrieveMillis);
        this.gcTableSyncOutput.setMillisLoadData(syncMillis);
        
        if (this.gcGrouperSync != null && this.gcGrouperSync.getRecordsCount() != null) {
          this.gcTableSyncOutput.setTotalCount(this.gcGrouperSync.getRecordsCount());
        }
      }
      
      this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));

      if (GrouperClientUtils.isBlank(gcGrouperSyncLog.getStatus())) {
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.SUCCESS);
      }

      // make sure at least some time has passed
      if (System.currentTimeMillis() - this.millisWhenSyncStarted < 20) {
        GrouperClientUtils.sleep(20);
      }
      this.getGcGrouperSyncJob().setPercentComplete(100);
      Timestamp milliswhenSyncStartedTimestamp = new Timestamp(this.millisWhenSyncStarted);
      this.getGcGrouperSyncJob().setLastSyncTimestamp(milliswhenSyncStartedTimestamp);

      if (gcTableSyncSubtype.isFullMetadataSync()) {
        this.getGcGrouperSync().setLastFullMetadataSyncRun(milliswhenSyncStartedTimestamp);
      }
      if (gcTableSyncSubtype.isFullSync()) {
        this.getGcGrouperSync().setLastFullSyncRun(milliswhenSyncStartedTimestamp);
      }
      if (gcTableSyncSubtype.isIncrementalSync()) {
        this.getGcGrouperSync().setLastIncrementalSyncRun(milliswhenSyncStartedTimestamp);
      }
      
      int gcSyncObjectChanges = this.getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      debugMap.put("gcSyncObjectChanges", gcSyncObjectChanges + gcGrouperSync.getInternalObjectsCreatedCount());

    } catch (RuntimeException re) {
      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {

      this.done = true;
      
      GcGrouperSyncHeartbeat.endAndWaitForThread(this.gcGrouperSyncHeartbeat);

      debugMap.put("finalLog", true);
      
      synchronized (this) {
        try {
          if (this.gcGrouperSyncJob != null) {
            this.gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          if (this.gcGrouperSyncLog != null) {
            this.gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
          }
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
        }
      }

      this.gcTableSyncOutput.setQueryCount(GcDbAccess.threadLocalQueryCountRetrieve());
      debugMap.put("queryCount", this.gcTableSyncOutput.getQueryCount());
      
      int durationMillis = (int)((System.nanoTime()-now)/1000000);
      debugMap.put("tookMillis", durationMillis);
      debugMap.put("took", DurationFormatUtils.formatDurationHMS(durationMillis));
      
      String debugString = GrouperClientUtils.mapToString(debugMap);

      try {
        if (gcGrouperSyncLog != null) {
          gcGrouperSyncLog.setDescriptionToSave(debugString);
          gcGrouperSyncLog.setJobTookMillis(durationMillis);
          gcGrouperSync.getGcGrouperSyncLogDao().internal_logStore(gcGrouperSyncLog);
        }
      } catch (RuntimeException re3) {
        debugMap.put("exception3", GrouperClientUtils.getFullStackTrace(re3));
        debugString = GrouperClientUtils.mapToString(debugMap);
      }

      GcTableSyncLog.debugLog(debugString);

      // already set total
      //gcTableSyncOutput.setTotal();
      this.gcTableSyncOutput.setMessage(debugString);

      // this isnt good
      if (debugMap.containsKey("exception") || debugMap.containsKey("exception2") || debugMap.containsKey("exception3")) {
        throw new RuntimeException(debugString);
      }
      
    }
    return this.gcTableSyncOutput;
  }

  /**
   * goes from column name to index of passed in memberships array
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Integer> membershipsColumnToIndex = (Map<String, Integer>)(Object)GrouperClientUtils.toMap(
      "group_id", 0, "member_id", 1, "field_id", 2);
  
  /**
   * the memberships primary keys come in as 
   * group_id, group_name, member_id, subject_source_id, subject_id, list_name
   * need to convert that into the form of the primary key
   */
  private void convertPrimaryKeysFromMembershipsToPrimaryKeys() {
    
    if (GrouperClientUtils.length(this.primaryKeysToSyncFromMemberships) > 0) {
      
      if (this.primaryKeysToSync == null) {
        this.primaryKeysToSync = new LinkedHashSet<MultiKey>();
      }
      
      int primaryKeySize = GrouperClientUtils.length(this.dataBeanFrom.getTableMetadata().getPrimaryKey());

      //make sure primary keys valid
      int[] primaryKeyIndexes = new int[primaryKeySize];
      int i=0;
      
      boolean hasGroupId = false;
      boolean hasMemberId = false;
      boolean hasFieldId = false;
      
      
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.dataBeanFrom.getTableMetadata().getPrimaryKey()) {
        String columnName = gcTableSyncColumnMetadata.getColumnName().toLowerCase();
        Integer membershipIndex = membershipsColumnToIndex.get(columnName);
        if (membershipIndex == null) {
          throw new RuntimeException("Cant find membership column in primary key, should be one of: " 
              + GrouperClientUtils.toStringForLog(membershipsColumnToIndex.keySet() + " -- primary key -- " + columnName));
        }
        primaryKeyIndexes[i++] = membershipIndex;
        
        if ("group_id".equals(columnName)) {
          hasGroupId = true;
        }
        
        if ("member_id".equals(columnName)) {
          hasMemberId = true;
        }
        
        if ("field_id".equals(columnName)) {
          hasFieldId = true;
        }
        
      }

      boolean columnsOk = false;
      if (primaryKeySize == 2 && hasGroupId && hasMemberId) {
        columnsOk = true;
      }
      if (primaryKeySize == 3 && hasGroupId && hasMemberId && hasFieldId) {
        columnsOk = true;
      }
      if (!columnsOk) {
        throw new RuntimeException("Primary key of membership sync must have either group_id, member_id...  or group_id, member_id, field_id");
      }
      
      boolean standardConfiguration = primaryKeySize == 3 && primaryKeyIndexes[0] == 0 
          && primaryKeyIndexes[1] == 1 && primaryKeyIndexes[2] == 2;
      
      // group_id, group_name, member_id, subject_source_id, subject_id, list_name
      for (MultiKey membershipArray : this.primaryKeysToSyncFromMemberships) {

        if (standardConfiguration) {
          this.primaryKeysToSync.add(membershipArray);
          continue;
        }
        
        Object[] primaryKey = new Object[primaryKeySize];
        for (i=0;i<primaryKeySize;i++) {
          
          int primaryKeyIndex = primaryKeyIndexes[i];
          primaryKey[i] = membershipArray.getKey(primaryKeyIndex);
          if (primaryKey[i] == null || (primaryKey[i] instanceof String && "".equals((String)primaryKey[i]))) {
            throw new RuntimeException("Cant have a null column here: " + GrouperClientUtils.toStringForLog(membershipArray));
          }
          
        }
        
        this.primaryKeysToSync.add(new MultiKey(primaryKey));
      }

    }
    
  }
  
  /**
   * log periodically
   * @param debugMap
   * @param gcTableSyncOutput 
   */
  public void logPeriodically(Map<String, Object> debugMap, GcTableSyncOutput gcTableSyncOutput) {
    
    if (System.currentTimeMillis() - this.lastLog > (1000 * 60) - 10) {
    
      String debugString = GrouperClientUtils.mapToString(debugMap);
      gcTableSyncOutput.setMessage(debugString);
      GcTableSyncLog.debugLog(debugString);
      this.lastLog = System.currentTimeMillis();

    }
    
  }
  
  /**
   * if passing in primary keys to sync, these are them
   */
  private Set<MultiKey> primaryKeysToSync = null;
  
  /**
   * if passing in primary keys to sync, these are them
   * @return primary keys to syn
   */
  public Set<MultiKey> getPrimaryKeysToSync() {
    return this.primaryKeysToSync;
  }

  /**
   * if passing in primary keys to sync, these are them
   * @param primaryKeysToSync1
   */
  public void setPrimaryKeysToSync(Set<MultiKey> primaryKeysToSync1) {
    this.primaryKeysToSync = primaryKeysToSync1;
  }

  /**
   * if passing in primary keys to sync, these are them in this format: group_id, member_id, field_id
   */
  private Set<MultiKey> primaryKeysToSyncFromMemberships = null;
  
  /**
   * if passing in primary keys to sync, these are them in this format: group_id, member_id, field_id
   * @return primary keys to syn
   */
  public Set<MultiKey> getPrimaryKeysToSyncFromMemberships() {
    return this.primaryKeysToSyncFromMemberships;
  }

  /**
   * if passing in primary keys to sync, these are them in this format: group_id, member_id, field_id
   * @param primaryKeysToSyncFromMemberships1
   */
  public void setPrimaryKeysToSyncFromMemberships(Set<MultiKey> primaryKeysToSyncFromMemberships1) {
    this.primaryKeysToSyncFromMemberships = primaryKeysToSyncFromMemberships1;
  }

  /**
   * if we need to pass in some member ids to sync before the main sync
   */
  private Collection<Object> memberIdsToSync = null;
  
  /**
   * if we need to pass in some member ids to sync before the main sync
   * @return member ids
   */
  public Collection<Object> getMemberIdsToSync() {
    return this.memberIdsToSync;
  }

  /**
   * if we need to pass in some member ids to sync before the main sync
   * @param memberIdsToSync1
   */
  public void setMemberIdsToSync(Collection<Object> memberIdsToSync1) {
    this.memberIdsToSync = memberIdsToSync1;
  }

  /**
   * if we need to pass in some group ids to sync before the main sync
   */
  private Collection<Object> groupIdsToSync = null;
  
  /**
   * if we need to pass in some group ids to sync before the main sync
   * @return
   */
  public Collection<Object> getGroupIdsToSync() {
    return this.groupIdsToSync;
  }

  /**
   * if we need to pass in some group ids to sync before the main sync
   * @param groupIdsToSync1
   */
  public void setGroupIdsToSync(Collection<Object> groupIdsToSync1) {
    this.groupIdsToSync = groupIdsToSync1;
  }

  /**
   * heartbeat thread
   */
  private GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
  
  
  
  /**
   * heartbeat thread
   * @return heartbeat
   */
  public GcGrouperSyncHeartbeat getGcGrouperSyncHeartbeat() {
    return this.gcGrouperSyncHeartbeat;
  }

  /**
   * heartbeat thread
   * @param gcGrouperSyncHeartbeat1
   */
  public void setGcGrouperSyncHeartbeat(GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat1) {
    this.gcGrouperSyncHeartbeat = gcGrouperSyncHeartbeat1;
  }

  /**
   * log object
   */
  private static final Log LOG = LogFactory.getLog(GcTableSync.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    GcTableSyncOutput gcTableSyncOutput = new GcTableSync().sync(args[0], GcTableSyncSubtype.valueOfIgnoreCase(args[1], true));
    System.out.println(gcTableSyncOutput.toString());
  }

}
