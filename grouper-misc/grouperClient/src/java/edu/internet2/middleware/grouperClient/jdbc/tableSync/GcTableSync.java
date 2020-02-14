/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

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
   * put heartbeat logic to kick off at heartbeat
   */
  private Runnable heartbeatLogic;
  
  /**
   * if this is paused
   */
  private boolean paused = false;

  /**
   * put heartbeat logic to kick off at heartbeat
   * @return
   */
  public Runnable getHeartbeatLogic() {
    return this.heartbeatLogic;
  }

  /**
   * put heartbeat logic to kick off at heartbeat
   * @param heartbeatLogic1
   */
  public void setHeartbeatLogic(Runnable heartbeatLogic1) {
    this.heartbeatLogic = heartbeatLogic1;
  }

  /**
   * if this is paused
   * @return if paused
   */
  public boolean isPaused() {
    return this.paused;
  }

  /**
   * if this is paused
   * @param paused1
   */
  public void setPaused(boolean paused1) {
    this.paused = paused1;
  }

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
   * pass in the output which will update as it runs
   * @param gcTableSyncOutputArray 
   * 
   */
  public GcTableSyncOutput sync(String configKey, GcTableSyncSubtype gcTableSyncSubtype) {
    
    this.millisWhenSyncStarted = System.currentTimeMillis();
    
    this.gcTableSyncOutput = new GcTableSyncOutput();
    
    this.gcTableSyncConfiguration = new GcTableSyncConfiguration();
    this.gcTableSyncConfiguration.configureTableSync(this.debugMap, this, configKey, gcTableSyncSubtype);
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.nanoTime();
    
    // if the job finished (e.g. status thread should end)
    final boolean[] done = new boolean[]{false};
    
    // if a more important job is running
    final boolean[] interrupted = new boolean[]{false};

    GcDbAccess.threadLocalQueryCountReset();
    
    Thread heartbeatThread = null;
    
    try {

      debugMap.put("finalLog", false);
      
      debugMap.put("state", "retrieveData");

      // thread to keep heartbeat updated 
      heartbeatThread = new Thread(new Runnable() {

        public void run() {
          
          try {
            while(true) {
              long loopStarted = System.currentTimeMillis();
              for (int i=0;i<60;i++) {
                if (done[0]) {
                  return;
                }
                // maybe 60 sleeps dont add up due to CPU
                if (System.currentTimeMillis()-loopStarted > 60000) {
                  break;
                }
                Thread.sleep(1000);
                if (done[0]) {
                  return;
                }
              }
              if (!GcTableSync.this.isPaused()) {
                synchronized (GcTableSync.this) {
                  if (done[0]) {
                    return;
                  }
                  // its been a minute, update the heartbeat, see if a more important job is running
                  boolean shouldKeepRunning = GcTableSync.this.gcGrouperSyncJob.assignHeartbeatAndCheckForPendingJobs(GcTableSync.this.gcTableSyncConfiguration.getGcTableSyncSubtype().isFullSync());
                  if (!shouldKeepRunning) {
                    interrupted[0]=true;
                    debugMap.put("interrupted", true);
                  }
                }
              }
              logPeriodically(debugMap, GcTableSync.this.gcTableSyncOutput);
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning status and logging", e);
          }
          
        }
        
      });
      
      heartbeatThread.setDaemon(true);
      heartbeatThread.start();
      
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
      
      // step 1
      this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
      
      this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));

      if (done[0]) {
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
      this.getGcGrouperSync().store();

    } catch (RuntimeException re) {
      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {

      done[0]=true;
      try {
        heartbeatThread.interrupt();
      } catch (Exception e) {
        
      }
      GrouperClientUtils.join(heartbeatThread);
      
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
          gcGrouperSyncLog.setDescription(debugString);
          gcGrouperSyncLog.setJobTookMillis(durationMillis);
          gcGrouperSyncLog.store();
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
   * log object
   */
  private static final Log LOG = LogFactory.getLog(GcTableSync.class);

  /**
   * @param args
   */
  public static void main(String[] args) {
    new GcTableSync().sync(args[0], GcTableSyncSubtype.valueOfIgnoreCase(args[1], true));
  }

}
