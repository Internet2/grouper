/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 *
 */
@DisallowConcurrentExecution
public class TableSyncOtherJob extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TableSyncOtherJob.class);

  /**
   * 
   */
  public TableSyncOtherJob() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(final OtherJobInput otherJobInput) {

    String jobName = otherJobInput.getJobName();
    
    // grouperClientTableSyncConfigKey = OTHER_JOB_personSourceSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());
    final String grouperClientConfigKeyName = "otherJob." + jobName + ".grouperClientTableSyncConfigKey";
    
    String grouperClientTableSyncConfigKey = GrouperLoaderConfig.retrieveConfig().propertyValueString(grouperClientConfigKeyName);
    
    if (StringUtils.isBlank(grouperClientTableSyncConfigKey)) {
      grouperClientTableSyncConfigKey = jobName;
    }

    final String syncTypeKeyName = "otherJob." + jobName + ".syncType";
    String syncTypeKeyNameString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(syncTypeKeyName);

    GcTableSyncSubtype gcTableSyncSubtype = GcTableSyncSubtype.valueOfIgnoreCase(syncTypeKeyNameString, true);
    final GcTableSync gcTableSync = new GcTableSync();

    // do a progress thread
    // if the job finished (e.g. status thread should end)
    final boolean[] done = new boolean[]{false};

    Thread heartbeatThread = null;
    try {
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
              synchronized (TableSyncOtherJob.this) {
                if (done[0]) {
                  return;
                }
                TableSyncOtherJob.this.updateHib3LoaderLog(otherJobInput.getHib3GrouperLoaderLog(), gcTableSync, true);
              }
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning status and logging", e);
          }
          
        }
        
      });
      
      heartbeatThread.start();
  
      gcTableSync.sync(grouperClientTableSyncConfigKey, gcTableSyncSubtype);
    } finally {
      done[0]=true;
      try {
        heartbeatThread.interrupt();
      } catch (Exception e) {
        
      }
      GrouperClientUtils.join(heartbeatThread);
      try {
        this.updateHib3LoaderLog(otherJobInput.getHib3GrouperLoaderLog(), gcTableSync, false);
      } catch (RuntimeException re) {
        LOG.error("error", re);
      }
    }
    
    return null;
  }

  /**
   * 
   * @param hib3GrouperLoaderLog
   * @param gcTableSync
   * @param store if should store
   */
  private void updateHib3LoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog, GcTableSync gcTableSync, boolean store) {
    GcTableSyncOutput gcTableSyncOutput = gcTableSync.getGcTableSyncOutput();
    if (gcTableSyncOutput == null) {
      return;
    }
    
    hib3GrouperLoaderLog.setDeleteCount(gcTableSyncOutput.getDelete());
    hib3GrouperLoaderLog.setInsertCount(gcTableSyncOutput.getInsert());
    hib3GrouperLoaderLog.setUpdateCount(gcTableSyncOutput.getUpdate());
    //hib3GrouperLoaderLog.setTotalCount(gcTableSyncOutput.get);  TODO
    hib3GrouperLoaderLog.setJobMessage(gcTableSyncOutput.getMessage());
    if (store) {
      hib3GrouperLoaderLog.store();
    }    
  }
}
