package edu.internet2.middleware.grouper.abac;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationDaemonLogic;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationObjectAttributes;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperLoaderJexlScriptIncremental extends EsbListenerBase{

  public GrouperLoaderJexlScriptIncremental() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void disconnect() {
    
  }

  /**
   * events to process
   */
  private List<EsbEventContainer> eventsToProcess;

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers) {

    eventsToProcess = esbEventContainers;
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("attestation");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.ATTESTATION_PROPAGATION);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(false);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    RuntimeException runtimeException = null;
    
    try {
     
//      populateIdsAndNamesToWorkOn();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//
//      groupIdToNamesAddAndAttributeChange.putAll(groupIdsToNamesAdd);
//      groupIdToNamesAddAndAttributeChange.putAll(groupIdsToNamesAttributeChange);
//      
//      populateAttributesAssignedToGroupsIncremental();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//
//      populateAttributesAssignedToStemsIncremental();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//      
//      populateAncestorsIncremental();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//
//      populateChildrenWithAttributesIncremental();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//      
//      groupsWithOrWithoutAttributesToProcess.putAll(groupsWithAttributesToProcess);
//      groupsWithOrWithoutAttributesToProcess.putAll(childrenGroupsAttestationAttributes);
//      
//      populateEventObjectsWhichDoNotHaveAttributes();
//      
//      populateChildrenWhichMayOrMayNotHaveAttributes();
//      GrouperDaemonUtils.stopProcessingIfJobPaused();
//      
//      Set<GrouperAttestationObjectAttributes> grouperAttestationObjectAttributesToProcess = new HashSet<GrouperAttestationObjectAttributes>();
//      grouperAttestationObjectAttributesToProcess.addAll(groupsWithOrWithoutAttributesToProcess.values());
//      
//      propagateAttestationAttributes(grouperAttestationObjectAttributesToProcess, ancestorStemsAttestationAttributes, debugMap);

    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperAttestationDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
//      if (LOG.isDebugEnabled()) {
//        LOG.debug(GrouperUtil.mapToString(debugMap));
//      }
      
    }

    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    
    return provisioningSyncConsumerResult;
    
  }

}
