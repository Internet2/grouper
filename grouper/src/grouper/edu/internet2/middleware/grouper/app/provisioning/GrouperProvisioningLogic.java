package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * does the logic to use the data from the DAOs and call the correct methods to synnc things up or dry run or send messages for async
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogic {

  /**
   * 
   */
  public void fullProvisionFull() {

    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];

    //lets get all from one side and the other and time it and do it in a thread so its faster
//    Thread selectFromThread = new Thread(new Runnable() {
//      
//      @Override
//      public void run() {
//        
//        try {
//          result[0] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanFrom(), primaryKeys, true);
//        } catch (RuntimeException re) {
//          if (RUNTIME_EXCEPTION[0] != null) {
//            LOG.error("Error retrieve by primary key", re);
//          }
//          RUNTIME_EXCEPTION[0] = re;
//        }
//        
//      }
//    });
//    
//    selectFromThread.start();
//    
//    result[1] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeys, false);
//    
//    GrouperClientUtils.join(selectFromThread);
//    if (RUNTIME_EXCEPTION[0] != null) {
//      throw RUNTIME_EXCEPTION[0];
//    }
//

    // make sure the sync objects are correct
    new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//    // step 1
//    debugMap.put("state", "retrieveData");
//    this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//    
//    this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
//
//    if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//      debugMap.put("interrupted", true);
//      debugMap.put("state", "done");
//      gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//      return;
//    }
    
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

}
