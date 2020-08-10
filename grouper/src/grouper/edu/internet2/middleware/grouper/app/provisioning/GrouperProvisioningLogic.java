package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

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
    
    @SuppressWarnings("unchecked")
    final Map<String, TargetGroup>[] TARGET_RESULT = new HashMap[1];

    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TARGET_RESULT[0] = grouperProvisioner.retrieveTargetDao().retrieveAllGroups();
        } catch (RuntimeException re) {
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();
    
    Map<String, TargetGroup> grouperTargetGroups = grouperProvisioner.retrieveGrouperDao().retrieveAllGroups();
    Map<String, TargetEntity> grouperTargetEntities = grouperProvisioner.retrieveGrouperDao().retrieveAllMembers();
    Map<String, TargetMembership> grouperTargetMemberships = grouperProvisioner.retrieveGrouperDao().retrieveAllMemberships();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
    
    Map<String, TargetGroup> actualTargetResult = TARGET_RESULT[0];
    Map<String, TargetGroup> translatedTargetResult = this.grouperProvisioner.retrieveTranslator().translateToTarget(grouperTargetGroups, grouperTargetEntities, grouperTargetMemberships);
    
    // TODO issues with dn comparison with case/spacing differences
    
    for (String key : actualTargetResult.keySet()) {
      TargetGroup actualTargetGroup = actualTargetResult.get(key);
      if (!translatedTargetResult.containsKey(key)) {
        this.grouperProvisioner.retrieveTargetDao().deleteGroup(actualTargetGroup);
      }
    }
    
    for (String key : translatedTargetResult.keySet()) {
      TargetGroup targetGroup = translatedTargetResult.get(key);
      if (!actualTargetResult.containsKey(key)) {
        this.grouperProvisioner.retrieveTargetDao().createGroup(targetGroup);
      } else {
        this.grouperProvisioner.retrieveTargetDao().updateGroupIfNeeded(targetGroup, actualTargetResult.get(key));
      }
    }

    // make sure the sync objects are correct
//    new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

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
