/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.tableSync;

import org.apache.commons.lang.StringUtils;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;


/**
 *
 */
@DisallowConcurrentExecution
public class TableSyncOtherJob extends OtherJobBase {

  /**
   * 
   */
  public TableSyncOtherJob() {
  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    String jobName = otherJobInput.getJobName();
    
    // grouperClientTableSyncConfigKey = OTHER_JOB_personSourceSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());
    final String grouperClientConfigKeyName = "otherJob." + jobName + ".grouperClientTableSyncConfigKey";
    
    String grouperClientTableSyncConfigKey = GrouperLoaderConfig.retrieveConfig().propertyValueString(grouperClientConfigKeyName);
    
    if (StringUtils.isBlank(grouperClientTableSyncConfigKey)) {
      grouperClientTableSyncConfigKey = jobName;
    }
    
    final String grouperClientTableFromName = "grouperClient.syncTable." + grouperClientTableSyncConfigKey + ".tableFrom";
    String tableFrom = GrouperClientConfig.retrieveConfig().propertyValueString(grouperClientTableFromName);
    if (StringUtils.isBlank(tableFrom)) {
      throw new RuntimeException("Cant find grouperClient config for " + grouperClientTableSyncConfigKey 
          + " tableSync, checking property: " + grouperClientTableFromName);
    }
    
    GcTableSync gcTableSync = new GcTableSync();
    gcTableSync.setKey(grouperClientTableSyncConfigKey);
    
    String tableSyncType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".syncType");
    GcTableSyncOutput gcTableSyncOutput = null;
    if (StringUtils.equalsIgnoreCase(tableSyncType, "full")) {
      gcTableSyncOutput = gcTableSync.fullSync();
    } else if (StringUtils.equalsIgnoreCase(tableSyncType, "incremental")) {
      gcTableSyncOutput = gcTableSync.incrementalSync();
    } else {
      throw new RuntimeException("Wrong tableSync type: '" + tableSyncType + "' for job: '" + jobName + "'");
    }
    
    otherJobInput.getHib3GrouperLoaderLog().setDeleteCount(gcTableSyncOutput.getDelete());
    otherJobInput.getHib3GrouperLoaderLog().setInsertCount(gcTableSyncOutput.getInsert());
    otherJobInput.getHib3GrouperLoaderLog().setUpdateCount(gcTableSyncOutput.getUpdate());
    otherJobInput.getHib3GrouperLoaderLog().setTotalCount(gcTableSyncOutput.getTotal());
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(gcTableSyncOutput.getMessage());
    
    return null;
  }

}
