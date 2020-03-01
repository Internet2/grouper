/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
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

    gcTableSync.getGcGrouperSyncHeartbeat().addHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        TableSyncOtherJob.this.updateHib3LoaderLog(otherJobInput.getHib3GrouperLoaderLog(), gcTableSync, true);
      }
    });
  
    gcTableSync.sync(grouperClientTableSyncConfigKey, gcTableSyncSubtype);
    this.updateHib3LoaderLog(otherJobInput.getHib3GrouperLoaderLog(), gcTableSync, false);
    
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
    
    if (gcTableSync.getGcGrouperSync() != null) {
      hib3GrouperLoaderLog.setJobDescription(gcTableSync.getGcGrouperSync().getSyncEngine() + "." + gcTableSync.getGcGrouperSync().getProvisionerName());
    }
    hib3GrouperLoaderLog.setDeleteCount(gcTableSyncOutput.getDelete());
    hib3GrouperLoaderLog.setInsertCount(gcTableSyncOutput.getInsert());
    hib3GrouperLoaderLog.setUpdateCount(gcTableSyncOutput.getUpdate());
    hib3GrouperLoaderLog.setTotalCount(gcTableSyncOutput.getTotalCount());
    hib3GrouperLoaderLog.setJobMessage(gcTableSyncOutput.getMessage());
    hib3GrouperLoaderLog.setMillisGetData((int)gcTableSyncOutput.getMillisGetData());
    hib3GrouperLoaderLog.setMillisLoadData((int)gcTableSyncOutput.getMillisLoadData());
    if (store) {
      hib3GrouperLoaderLog.store();
    }    
  }
}
