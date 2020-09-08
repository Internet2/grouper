package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * does the logic to use the data from the DAOs and call the correct methods to synnc things up or dry run or send messages for async
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogic {
  
  /**
   * see if there are any objects that need to be fixed or removed
   */
  public void validateGrouperProvisioningData() {
    
    
  }
  

  /**
   * 
   */
  public void provision() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    try {
      debugMap.put("state", "retrieveDataPass1");
      long start = System.currentTimeMillis();
      this.getGrouperProvisioner().getGrouperProvisioningType().retrieveDataPass1(this.grouperProvisioner);
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("retrieveDataPass1");
    }

    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.retrieveSubjectLink();
      
      debugMap.put("state", "retrieveTargetGroupLink");
      this.retrieveTargetGroupLink();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.retrieveTargetEntityLink();
      
      debugMap.put("state", "validateInitialProvisioningData");
      this.validateGrouperProvisioningData();
  
      debugMap.put("state", "translateGrouperToTarget");
      this.grouperProvisioner.retrieveTranslator().translateGrouperToTarget();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateGrouperToTarget");
    }

    try {
      debugMap.put("state", "targetIdGrouperObjects");
      this.grouperProvisioner.retrieveTranslator().targetIdGrouperObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("targetIdGrouperObjects");
    }

    debugMap.put("state", "indexTargetIdOfGrouperObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfGrouperObjects();

    try {
      debugMap.put("state", "retrieveDataPass2");
      long start = System.currentTimeMillis();
      this.getGrouperProvisioner().getGrouperProvisioningType().retrieveDataPass2(this.grouperProvisioner);
      long retrieveDataPass2 = System.currentTimeMillis()-start;
      // if full dont log this
      if (retrieveDataPass2 > 1) {
        debugMap.put("retrieveDataPass2", retrieveDataPass2);
      }
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("retrieveDataPass2");
    }

    try {
      debugMap.put("state", "targetIdTargetObjects");
      this.grouperProvisioner.retrieveTranslator().targetIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("targetIdTargetObjects");
    }

    debugMap.put("state", "indexTargetIdOfTargetObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfTargetObjects();

    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("compareTargetObjects");
    }
    
    this.countInsertsUpdatesDeletes();

    try {
      debugMap.put("state", "sendChangesToTarget");
      this.getGrouperProvisioner().retrieveTargetDao().sendChangesToTarget();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("sendChangesToTarget");
    }
    // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//    this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//    this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//    this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

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


  public void retrieveTargetEntityLink() {
    // TODO If using target entity link and the ID is not in the member sync cache object, then resolve the target entity, and put the id in the member sync object
    
  }


  public void retrieveTargetGroupLink() {
    // TODO If using target group link and the ID is not in the group sync cache object, then resolve the target group, and put the id in the group sync object
    
  }


  public void retrieveSubjectLink() {
    // TODO If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    
  }

  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  protected void retrieveAllData() {
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao().retrieveAllData();
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncData(GrouperProvisioningLogic.this.grouperProvisioner.getGrouperProvisioningType());
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperData(this.grouperProvisioner.getGrouperProvisioningType());
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();
    
    this.grouperProvisioner.retrieveGrouperDao().calculateProvisioningDataToDelete();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
  }
  
  protected void countInsertsUpdatesDeletes() {
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningMemberships());
    
  }
  
  protected void countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction provisioningObjectChangeAction, List<? extends ProvisioningUpdatable> provisioningUpdatables) {
    // maybe not count fields?
    if (provisioningUpdatables == null) {
      return;
    }
    switch(provisioningObjectChangeAction) {
      case insert:
        this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(GrouperUtil.length(provisioningUpdatables));  
        break;
      case update:
        this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(GrouperUtil.length(provisioningUpdatables));  
        break;
      case delete:
        this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(GrouperUtil.length(provisioningUpdatables));  
        break;
    }
    for (ProvisioningUpdatable provisioningUpdatable : provisioningUpdatables) {
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningUpdatable.getInternal_objectChanges())) {
        switch (provisioningObjectChange.getProvisioningObjectChangeAction()) {
          case insert:
            this.grouperProvisioner.getGrouperProvisioningOutput().addInsert(1);  
            break;
          case update:
            this.grouperProvisioner.getGrouperProvisioningOutput().addUpdate(1);  
            break;
          case delete:
            this.grouperProvisioner.getGrouperProvisioningOutput().addDelete(1);  
            break;
          
        }
      }
    }
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningLogic.class);

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

  /**
   * get data from change log
   */
  public void retrieveIncrementalGrouperData() {
    
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    // lets get the grouper side first
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncData(GrouperProvisioningLogic.this.grouperProvisioner.getGrouperProvisioningType());
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperData(this.grouperProvisioner.getGrouperProvisioningType());
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();
    
    this.grouperProvisioner.retrieveGrouperDao().calculateProvisioningDataToDelete();    

  }


}
