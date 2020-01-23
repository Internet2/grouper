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


/**
 * sync a table
 */
public class GcTableSync {

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
   * configure the table sync for a certain type of sync
   * @param configKey
   * @param gcTableSyncSubtype
   */
  public void configure(String configKey, GcTableSyncSubtype gcTableSyncSubtype) {
    try {
      this.gcTableSyncConfiguration = new GcTableSyncConfiguration();
      this.gcTableSyncConfiguration.setConfigKey(configKey);
      this.gcTableSyncConfiguration.setGcTableSyncSubtype(gcTableSyncSubtype);
      this.gcGrouperSync = GcGrouperSync.retrieveOrCreateByProvisionerName("grouper", "sqlTableSync", configKey);
      this.gcGrouperSyncJob = this.gcGrouperSync.retrieveJobOrCreateBySyncType(gcTableSyncSubtype.name());
      this.gcGrouperSyncLog = this.gcGrouperSyncJob.retrieveGrouperSyncLogOrCreate();
      this.gcGrouperSyncLog.setSyncTimestamp(new Timestamp(System.currentTimeMillis()));

      this.gcTableSyncConfiguration.configureTableSync(this.debugMap);
    } catch (RuntimeException re) {
      if (this.gcGrouperSyncLog != null) {
        this.gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.CONFIG_ERROR);
        this.gcGrouperSyncLog.store();
      }
      throw re;
    }
  }

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
  public void sync(final GcTableSyncOutput gcTableSyncOutput) {
    
    if (this.gcTableSyncConfiguration == null) {
      throw new RuntimeException("Table sync is not configured, call configure before sync");
    }

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
              // its been a minute, update the heartbeat, see if a more important job is running
              boolean shouldKeepRunning = GcTableSync.this.gcGrouperSyncJob.assignHeartbeatAndCheckForPendingJobs(GcTableSync.this.gcTableSyncConfiguration.getGcTableSyncSubtype().isFullSync());
              if (!shouldKeepRunning) {
                interrupted[0]=true;
                debugMap.put("interrupted", true);
              }
              logPeriodically(debugMap, gcTableSyncOutput);
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning status and logging", e);
          }
          
        }
        
      });
      
      heartbeatThread.start();
      
      this.dataBeanFrom = new GcTableSyncTableBean();
      this.dataBeanFrom.configureMetadata(this.gcTableSyncConfiguration.getDatabaseFrom(), this.gcTableSyncConfiguration.getTableFrom());
      this.dataBeanFrom.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getColumnsString());
      this.dataBeanFrom.getTableMetadata().assignPrimaryKeyColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString());
      this.dataBeanFrom.getTableMetadata().assignChangeFlagColumn(this.gcTableSyncConfiguration.getChangeFlagColumnString());
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getGroupColumnString())) {
        this.dataBeanFrom.getTableMetadata().assignGroupColumn(this.gcTableSyncConfiguration.getGroupColumnString());
      }
      // if the incremental is there, use it, otherwise it might be in the real time table
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getIncrementalProgressColumnString())) {
        if (this.dataBeanFrom.getTableMetadata().lookupColumn(this.gcTableSyncConfiguration.getIncrementalProgressColumnString(), false) != null) {
          this.dataBeanFrom.getTableMetadata().assignIncrementalProgressColumn(this.gcTableSyncConfiguration.getIncrementalProgressColumnString());
        }
      }
      
      this.dataBeanTo = new GcTableSyncTableBean();
      this.dataBeanTo.configureMetadata(this.gcTableSyncConfiguration.getDatabaseTo(), this.gcTableSyncConfiguration.getTableTo());
      this.dataBeanTo.getTableMetadata().setConnectionNameOrReadonly(this.gcTableSyncConfiguration.getDatabaseToOrReadonly());
      this.dataBeanTo.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getColumnsString());
      this.dataBeanTo.getTableMetadata().assignPrimaryKeyColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString());
      this.dataBeanTo.getTableMetadata().assignChangeFlagColumn(this.gcTableSyncConfiguration.getChangeFlagColumnString());
      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getGroupColumnString())) {
        this.dataBeanTo.getTableMetadata().assignGroupColumn(this.gcTableSyncConfiguration.getGroupColumnString());
      }

      if (!GrouperClientUtils.isBlank(this.gcTableSyncConfiguration.getIncrementalPrimaryKeyTable())) {
        this.dataBeanRealTime = new GcTableSyncTableBean();
        this.dataBeanRealTime.configureMetadata(this.gcTableSyncConfiguration.getDatabaseFrom(), this.gcTableSyncConfiguration.getIncrementalPrimaryKeyTable());
        this.dataBeanRealTime.getTableMetadata().assignColumns(this.gcTableSyncConfiguration.getPrimaryKeyColumnsString() + ", " + this.gcTableSyncConfiguration.getIncrementalProgressColumnString());
        this.dataBeanRealTime.getTableMetadata().assignChangeFlagColumn(this.gcTableSyncConfiguration.getIncrementalProgressColumnString());
      }
      
      debugMap.put("databaseFrom", this.getDataBeanFrom().getTableMetadata().getConnectionName());
      debugMap.put("tableFrom", this.getDataBeanFrom().getTableMetadata().getTableName());
      debugMap.put("databaseTo", this.getDataBeanTo().getTableMetadata().getConnectionName());
      debugMap.put("tableTo", this.getDataBeanTo().getTableMetadata().getTableName());
      
      // step 1
      this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
      
      gcTableSyncOutput.setRowsSelectedFrom(GrouperClientUtils.length(this.dataBeanFrom.getDataInitialQuery().getRows()));
      gcTableSyncOutput.setRowsSelectedTo(GrouperClientUtils.length(this.dataBeanTo.getDataInitialQuery().getRows()));
      this.gcGrouperSyncLog.setRecordsProcessed(Math.max(gcTableSyncOutput.getRowsSelectedFrom(), gcTableSyncOutput.getRowsSelectedTo()));

      if (done[0]) {
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
        return;
      }
      
      // step 2
      debugMap.put("state", "syncData");
      {
        Integer recordsChanged = this.gcTableSyncConfiguration.getGcTableSyncSubtype().syncData(debugMap, this);
        if (recordsChanged != null) {
          this.gcGrouperSyncLog.setRecordsChanged(recordsChanged);
        }
      }
      
      gcTableSyncOutput.setRowsSelectedFrom(GrouperClientUtils.length(this.dataBeanFrom.getDataInitialQuery().getRows()));
      gcTableSyncOutput.setRowsSelectedTo(GrouperClientUtils.length(this.dataBeanTo.getDataInitialQuery().getRows()));
      this.gcGrouperSyncLog.setRecordsProcessed(Math.max(gcTableSyncOutput.getRowsSelectedFrom(), gcTableSyncOutput.getRowsSelectedTo()));

      if (GrouperClientUtils.isBlank(gcGrouperSyncLog.getStatus())) {
        gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.SUCCESS);
      }
    } catch (RuntimeException re) {
      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.ERROR);
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
    } finally {
      done[0]=true;
      GrouperClientUtils.join(heartbeatThread);
      
      debugMap.put("finalLog", true);

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
      
      gcTableSyncOutput.setQueryCount(GcDbAccess.threadLocalQueryCountRetrieve());
      debugMap.put("queryCount", gcTableSyncOutput.getQueryCount());
      
      int durationMillis = (int)((System.currentTimeMillis()-now)/1000000);
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
      gcTableSyncOutput.setMessage(debugString);

      // this isnt good
      if (debugMap.containsKey("exception") || debugMap.containsKey("exception2") || debugMap.containsKey("exception3")) {
        throw new RuntimeException(debugString);
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
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GcTableSync.class);

  /**
   * @param args
   */
  public static void main(String[] args) {

    
//    GcTableSync gcTableSync = new GcTableSync();
//    gcTableSync.setKey("personSource");
//    gcTableSync.selectStuffTest();
  }

  /**
   * 
   */
  public void selectStuffTest() {

  }

  
}
