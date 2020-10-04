package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
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
  public void provision() {

    // let the target dao tell the framework what it can do
    this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getWrappedDao().registerGrouperProvisionerDaoCapabilities(
        this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities()
        );

    // let the provisioner tell the framework how the provisioner should behave with respect to the target
    this.getGrouperProvisioner().registerProvisioningBehaviors(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior());
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.configure);

    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().provision(this.grouperProvisioner);
    
  }

  /**
   * 
   */
  public void provisionFull() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    try {
      debugMap.put("state", "retrieveAllDataFromGrouperAndTarget");
      long start = System.currentTimeMillis();
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveAllData();
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveAllDataFromGrouperAndTarget);
    }

    try {
      debugMap.put("state", "targetAttributeManipulation");
      // assign target id to target objects
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterForSelect(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects());
      
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
    }
    

    try {
      debugMap.put("state", "targetIdTargetObjects");
      // assign target id to target objects
      this.grouperProvisioner.retrieveGrouperTranslator().targetIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetIdTargetObjects);
    }
    
    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
    }

    try {
  
      debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");

      {
        List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups();
        List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
      }
      
      {
        List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities();
        List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
            grouperProvisioningEntities, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
    }

    try {
      debugMap.put("state", "targetIdGrouperGroupsEntities");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetIdGrouperGroupsEntities);
    }

    {
      debugMap.put("state", "indexTargetIdOfGrouperGroups");
      
      // index the groups and entity target ids
      this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfGrouperGroups(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups());
      
      debugMap.put("state", "indexTargetIdOfGrouperEntities");
      
      this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfGrouperEntities(
          this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities());
    }

    debugMap.put("state", "indexTargetIdOfTargetGroupsEntities");
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfTargetGroups();
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfTargetEntities();

    debugMap.put("state", "createMissingGroups");
    createMissingGroupsFull();

    debugMap.put("state", "createMissingEntities");
    createMissingEntitiesFull();

    try {
      debugMap.put("state", "retrieveTargetGroupLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
    }
      
    try {
  
      debugMap.put("state", "translateGrouperMembershipsToTarget");

      {
        List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningMemberships();
        List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
            grouperProvisioningMemberships, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
    }

    try {
      debugMap.put("state", "targetIdGrouperMemberships");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetIdGrouperMemberships);
    }

    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfGrouperMemberships(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships());

    // index target memberships
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfTargetMemberships();

    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupTargetIdToProvisioningGroupWrapper().values());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getEntityTargetIdToProvisioningEntityWrapper().values());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectMembershipsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getMembershipTargetIdToProvisioningMembershipWrapper().values());
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes());
      } catch (Exception e) {
        LOG.error(e);
      }
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);

    }
  
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
      gcGrouperSync.setLastFullSyncRun(nowTimestamp);

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(null);
      gcGrouperSyncJob.setErrorTimestamp(null);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningData().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);
      
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
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

  /**
   * 
   */
  public void provisionIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    try {
      debugMap.put("state", "retrieveIncrementalDataFromGrouper");
      long start = System.currentTimeMillis();
      grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIncrementalDataFromGrouper();
      long retrieveDataPass1 = System.currentTimeMillis()-start;
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIncrementalDataFromGrouper);
    }

    try {
      debugMap.put("state", "targetIdTargetObjects");
      this.grouperProvisioner.retrieveGrouperTranslator().targetIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetIdTargetObjects);
    }

    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
    }

    debugMap.put("state", "retrieveMissingObjects");
    retrieveMissingObjectsIncremental();

    debugMap.put("state", "createMissingObjects");
    //TODO createMissingGroupsPass1();
    //TODO createMissingEntitiesPass1();

    try {
      debugMap.put("state", "retrieveTargetGroupLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkIncremental();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkIncremental();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
    }
      
    try {
  
      debugMap.put("state", "translateGrouperToTarget");
      this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTarget();
      // note in a full sync this wont do anything since there are no includeDelete objects there
      this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetIncludeDeletes();
    } finally {
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperToTarget);
    }

    try {
      debugMap.put("state", "targetIdGrouperObjects");
      this.grouperProvisioner.retrieveGrouperTranslator().targetIdGrouperObjects();
      this.grouperProvisioner.retrieveGrouperTranslator().targetIdGrouperObjectsIncludeDeletes();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetIdGrouperObjects);
    }

    debugMap.put("state", "indexTargetIdOfGrouperObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfGrouperObjects();

    try {
      debugMap.put("state", "retrieveDataPass2");
      long start = System.currentTimeMillis();
      
      grouperProvisioner.retrieveGrouperProvisioningLogic().setupIncrementalGrouperTargetObjectsToRetrieveFromTarget();
      TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse 
        = grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveIncrementalData(grouperProvisioner.retrieveGrouperProvisioningData().getTargetDaoRetrieveIncrementalDataRequest());
      if (targetDaoRetrieveIncrementalDataResponse != null) {
        grouperProvisioner.retrieveGrouperProvisioningData().setTargetProvisioningObjects(targetDaoRetrieveIncrementalDataResponse.getTargetData());
      }

      long retrieveDataPass2 = System.currentTimeMillis()-start;
      // if full dont log this
      if (retrieveDataPass2 > 1) {
        debugMap.put("retrieveDataPass2_millis", retrieveDataPass2);
      }
    } finally {
      // TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveDataPass2);
    }

    debugMap.put("state", "indexTargetIdOfTargetObjects");
    this.grouperProvisioner.retrieveGrouperProvisioningTargetIdIndex().indexTargetIdOfTargetObjects();

    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } finally {
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);
    }
  
    {
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
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

  public void createMissingGroupsFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsInsert(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups())) {
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();

      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }

      ProvisioningGroup targetGroup = provisioningGroup.getProvisioningGroupWrapper().getTargetProvisioningGroup();
      
      if (targetGroup != null) {
        continue;
      }
      
      missingGroups.add(provisioningGroup);
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForCreate", GrouperUtil.length(missingGroups));
    
    this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate);

    List<ProvisioningGroup> grouperTargetGroupsToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, true);

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroupsToInsert);
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroupsToInsert);

    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroupsToInsert);
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetGroupsForCreate);

    //lets create these
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
    
    //retrieve so we have a copy
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroups(targetGroups, true, false, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsCreated);

    Map<Object, ProvisioningGroupWrapper> targetIdToProvisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningData().getGroupTargetIdToProvisioningGroupWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroupWrapper provisioningGroupWrapper = targetIdToProvisioningGroupWrapper.get(targetGroup.getTargetId());
      
      // not sure why it wouldnt match or exist...
      provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
      targetGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
    }
    
  }

  public void createMissingEntitiesFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesInsert(), false)) {
      return;
    }
      
    
    //do we have missing entities?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities())) {
      
      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      
      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      
      ProvisioningEntity targetEntity = provisioningEntity.getProvisioningEntityWrapper().getTargetProvisioningEntity();
      
      if (targetEntity != null) {
        continue;
      }

      missingEntities.add(provisioningEntity);
    }

    if (GrouperUtil.length(missingEntities) == 0) {
      return;
    }
    
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingEntitiesForCreate", GrouperUtil.length(missingEntities));
    
    this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().setProvisioningEntities(missingEntities);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate);
    
    // translate
    List<ProvisioningEntity> grouperTargetEntitiesToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(missingEntities, false, true);
    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntitiesToInsert);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetEntitiesForCreate);

    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
    
    //lets create these
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
      } catch (Exception e) {
        LOG.error(e);
      }
    }

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(grouperTargetEntitiesToInsert);

    //retrieve so we have a copy
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, false));
    
    List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
    
    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(targetEntities);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingCreated().setProvisioningEntities(targetEntities);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesCreated);

    // match these up with retrieved entities
    Map<Object, ProvisioningEntityWrapper> targetIdToProvisioningEntityWrapper = grouperProvisioner.retrieveGrouperProvisioningData().getEntityTargetIdToProvisioningEntityWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      
      // look up the grouper group that looked this up
      ProvisioningEntityWrapper provisioningEntityWrapper = targetIdToProvisioningEntityWrapper.get(targetEntity.getTargetId());
      
      // not sure why it wouldnt match or exist...
      provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
      targetEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);

    }

  }

  public void setupIncrementalGrouperTargetObjectsToRetrieveFromTarget() {

    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetDaoRetrieveIncrementalDataRequest();

    GrouperIncrementalUuidsToRetrieveFromGrouper grouperIncrementalUuidsToRetrieveFromGrouper =
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper();

    {
      //the groups only should be the full list of grouper target groups
      List<ProvisioningGroup> grouperTargetGroupsForGroupOnly = new ArrayList<ProvisioningGroup>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups()));
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsForGroupOnly);
    }

    {
      // generally these arent needed, but if there are no target groups (e.g. only entities or memberships), maybe needed
      List<ProvisioningGroupWrapper> groupWrappersForGroupOnly = new ArrayList<ProvisioningGroupWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper()).values());
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupOnly(groupWrappersForGroupOnly);
    }
    {
      //the entities only should be the full list of grouper target entities
      List<ProvisioningEntity> grouperTargetEntitiesForEntityOnly = new ArrayList<ProvisioningEntity>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities()));
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesForEntityOnly);
    }
    {
      // generally these arent needed, but if there are no target entities (e.g. only groups or memberships), maybe needed
      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly = new ArrayList<ProvisioningEntityWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper()).values());
      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersForEntityOnly(entityWrappersForEntityOnly);
    }
    {
      int missingGrouperTargetGroupsForGroupSync = 0;
      //the groups for group memberships should be looked up from the wrapper objects
      List<ProvisioningGroup> grouperTargetGroupForGroupSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupSync = new ArrayList<ProvisioningGroupWrapper>();
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupMembershipSync(grouperTargetGroupForGroupSync);
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupMembershipSync(provisioningGroupWrappersForGroupSync);
      for (String groupUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync())) {
        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
        if (provisioningGroupWrapper == null) {
          missingGrouperTargetGroupsForGroupSync++;
          continue;
        }
        provisioningGroupWrappersForGroupSync.add(provisioningGroupWrapper);
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroupIncludeDelete();
        if (grouperTargetGroup == null) {
          // this might be expected
          continue;
        }
        grouperTargetGroupForGroupSync.add(grouperTargetGroup);
      }
      if (missingGrouperTargetGroupsForGroupSync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetGroupsForGroupSync", missingGrouperTargetGroupsForGroupSync);
      }
    }    
    {
      int missingGrouperTargetEntitiesForEntitySync = 0;
      //the groups for group memberships should be looked up from the wrapper objects
      List<ProvisioningEntity> grouperTargetEntityForEntitySync = new ArrayList<ProvisioningEntity>();
      List<ProvisioningEntityWrapper> provisioningEntityWrappersForEntitySync = new ArrayList<ProvisioningEntityWrapper>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityMembershipSync(grouperTargetEntityForEntitySync);
      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersforEntityMembershipSync(provisioningEntityWrappersForEntitySync);

      for (String memberUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityMembershipSync())) {
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
        if (provisioningEntityWrapper == null) {
          missingGrouperTargetEntitiesForEntitySync++;
          continue;
        }
        provisioningEntityWrappersForEntitySync.add(provisioningEntityWrapper);
        
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntityIncludeDelete();
        if (grouperTargetEntity == null) {
          continue;
        }
        grouperTargetEntityForEntitySync.add(grouperTargetEntity);
      }
      if (missingGrouperTargetEntitiesForEntitySync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetEntitiesForEntitySync", missingGrouperTargetEntitiesForEntitySync);
      }
    }    
    //lets get the memberships
    {
      int missingGrouperTargetMembershipsForMembershipSync = 0;
      //the groups only should be the full list of grouper target groups
      List<MultiKey> groupEntityMembershipWrappers = new ArrayList<MultiKey>();
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupMemberMembershipWrappersForMembershipSync(groupEntityMembershipWrappers);

      List<MultiKey> grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships = new ArrayList<MultiKey>();
      targetDaoRetrieveIncrementalDataRequest
        .setTargetGroupsEntitiesMembershipsForMembershipSync(grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships);
      
      //get the ones that were looked up in grouper
      for (MultiKey groupUuidMemberUuidFieldId : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsMemberUuidsFieldIdsForMembershipSync())) {
        
        String groupUuid = (String)groupUuidMemberUuidFieldId.getKey(0);
        String memberUuid = (String)groupUuidMemberUuidFieldId.getKey(1);
        
        MultiKey groupUuidMemberUuid = new MultiKey(groupUuid, memberUuid);
        
        // dont need field id
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperTargetEntityIncludeDelete();

        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroupIncludeDelete();

        ProvisioningMembershipWrapper provisioningMembershipWrapper = this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(groupUuidMemberUuid);
        ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGrouperTargetMembershipIncludeDelete();

        if (grouperTargetMembership != null || (grouperTargetGroup != null && grouperTargetEntity != null)) {
          grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships.add(new MultiKey(grouperTargetGroup, grouperTargetEntity, grouperTargetMembership));
        }
        if (provisioningMembershipWrapper != null || (provisioningGroupWrapper != null && provisioningEntityWrapper != null)) {
          groupEntityMembershipWrappers.add(new MultiKey(provisioningGroupWrapper, provisioningEntityWrapper, provisioningMembershipWrapper));
          
        } else {
          missingGrouperTargetMembershipsForMembershipSync++;
        }
        
      }
      if (missingGrouperTargetMembershipsForMembershipSync > 0) {
        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetMembershipsForMembershipSync", missingGrouperTargetMembershipsForMembershipSync);
      }
      
    }

  }


  /**
   * get data from change log
   */
  public void retrieveIncrementalDataFromGrouper() {
        
    this.retrieveGrouperDataIncremental();
  }
  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  public void retrieveAllData() {
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse
            = GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter()
              .retrieveAllData(new TargetDaoRetrieveAllDataRequest());
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningData()
            .setTargetProvisioningObjects(targetDaoRetrieveAllDataResponse.getTargetData());
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    retrieveGrouperDataFull();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
  }


  public void retrieveGrouperDataFull() {
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncDataFull();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    // get all grouper data for the provisioner
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataFull();
    
    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // put the sync objects in their respective wrapper objects
    assignSyncObjectsToWrappers();

    GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
    gcGrouperSync.setGroupCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups()));
    gcGrouperSync.setUserCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities()));
    gcGrouperSync.setRecordsCount(
        GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningMemberships())
        );
    
  }


  public void assignSyncObjectsToWrappers() {
    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    
      // loop through sync groups
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToSyncGroup()).values()) {
    
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
        
        provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
        
      }
    }

    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
    
      // loop through sync groups
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToSyncMember()).values()) {
    
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(gcGrouperSyncMember.getMemberId());
        
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
        
      }
    }

    {
      Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    
      // loop through sync groups
      for (MultiKey groupUuidMemberUuid : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership()).keySet()) {
    
        GcGrouperSyncMembership gcGrouperSyncMembership  = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership().get(groupUuidMemberUuid);
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupUuidMemberUuid);
        
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
        
      }
    }
  }
  
  public void retrieveGrouperDataIncremental() {
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncDataIncremental();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncremental();
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
    // incrementals need to clone and setup sync objects as deletes
    setupIncrementalClonesOfGroupProvisioningObjects();

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 
    
    assignSyncObjectsToWrappers();

  }
  
  protected void countInsertsUpdatesDeletes() {
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningMemberships());
    
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
   * make a deep copy of grouper provisioning data into the grouper provisioning objects to delete
   */
  public void setupIncrementalClonesOfGroupProvisioningObjects() {
    GrouperProvisioningLists grouperProvisioningObjects = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects();
    GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjectsIncludeDeletes();
  
    List<ProvisioningGroup> grouperProvisioningGroupsIncludeDeletes = (List<ProvisioningGroup>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningGroups());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningGroups(grouperProvisioningGroupsIncludeDeletes);

    List<ProvisioningEntity> grouperProvisioningEntitysIncludeDeletes = (List<ProvisioningEntity>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningEntities());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningEntities(grouperProvisioningEntitysIncludeDeletes);

    List<ProvisioningMembership> grouperProvisioningMembershipsIncludeDeletes = (List<ProvisioningMembership>)(Object)
        ProvisioningUpdatable.clone((List<ProvisioningUpdatable>)(Object)grouperProvisioningObjects.getProvisioningMemberships());
    grouperProvisioningObjectsIncludeDeletes.setProvisioningMemberships(grouperProvisioningMembershipsIncludeDeletes);

    Map<String, ProvisioningGroup> groupUuidToProvisioningGroupIncludeDelete = new HashMap<String, ProvisioningGroup>();
    for (ProvisioningGroup provisioningGroupIncludeDelete : GrouperUtil.nonNull(grouperProvisioningGroupsIncludeDeletes)) {
      groupUuidToProvisioningGroupIncludeDelete.put(provisioningGroupIncludeDelete.getId(), provisioningGroupIncludeDelete);
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroupIncludeDelete.getProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioningGroupIncludeDelete(provisioningGroupIncludeDelete);
    }

    Map<String, ProvisioningEntity> memberUuidToProvisioningEntityIncludeDelete = new HashMap<String, ProvisioningEntity>();
    for (ProvisioningEntity provisioningEntityIncludeDelete : GrouperUtil.nonNull(grouperProvisioningEntitysIncludeDeletes)) {
      memberUuidToProvisioningEntityIncludeDelete.put(provisioningEntityIncludeDelete.getId(), provisioningEntityIncludeDelete);
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntityIncludeDelete.getProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioningEntityIncludeDelete(provisioningEntityIncludeDelete);
    }

    int membershipReferenceNotMatchIncludeDeletes = 0;
    for (ProvisioningMembership provisioningMembershipIncludeDelete : GrouperUtil.nonNull(grouperProvisioningMembershipsIncludeDeletes)) {
      
      {
        ProvisioningGroup grouperProvisioningGroupIncludeDelete = groupUuidToProvisioningGroupIncludeDelete.get(provisioningMembershipIncludeDelete.getProvisioningGroupId());
        if (grouperProvisioningGroupIncludeDelete == null) {
          membershipReferenceNotMatchIncludeDeletes++;
        }
        provisioningMembershipIncludeDelete.setProvisioningGroup(grouperProvisioningGroupIncludeDelete);
      }
      {
        ProvisioningEntity grouperProvisioningEntityIncludeDelete = memberUuidToProvisioningEntityIncludeDelete.get(provisioningMembershipIncludeDelete.getProvisioningEntityId());
        if (grouperProvisioningEntityIncludeDelete == null) {
          membershipReferenceNotMatchIncludeDeletes++;
        }
        provisioningMembershipIncludeDelete.setProvisioningEntity(grouperProvisioningEntityIncludeDelete);
      }
      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningMembershipIncludeDelete.getProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioningMembershipIncludeDelete(provisioningMembershipIncludeDelete);

    }
    if (membershipReferenceNotMatchIncludeDeletes > 0) {
      this.grouperProvisioner.getDebugMap().put("membershipReferenceNotMatchIncludeDeletes", membershipReferenceNotMatchIncludeDeletes);
    }
  }


  public void calculateProvisioningDataToDelete() {
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }


  /**
   * take the sync members and see which ones do not correspond to a grouper member
   */
  public void calculateProvisioningEntitiesToDelete() {
  
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToSyncMember();
  
    List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities();
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningEntitiesToDelete = 0;
  
    // loop through sync groups
    for (GcGrouperSyncMember gcGrouperSyncMember : memberUuidToSyncMember.values()) {
  
      ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningMemberWrapper.get(gcGrouperSyncMember.getMemberId());
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (provisioningEntityWrapper == null) {
        
        provisioningEntitiesToDelete++;
        
        ProvisioningEntity provisioningEntity = new ProvisioningEntity();
        provisioningEntity.setId(gcGrouperSyncMember.getMemberId());
  
        provisioningEntity.assignAttributeValue("subjectId", gcGrouperSyncMember.getSubjectId());
        provisioningEntity.assignAttributeValue("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
        if (grouperProvisioningEntities == null) {
          grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningEntities(grouperProvisioningEntities);
        }
        grouperProvisioningEntities.add(provisioningEntity);
  
        provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
        provisioningEntityWrapper.setGrouperProvisioningEntityIncludeDelete(provisioningEntity);
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
        
        memberUuidToProvisioningMemberWrapper.put(provisioningEntity.getId(), provisioningEntityWrapper);
      }
        
      provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
      
    }
    
    if (provisioningEntitiesToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntitiesToDelete", provisioningEntitiesToDelete);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {
  
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToSyncGroup();
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
  
    int provisioningGroupsToDeleteCount = 0;
  
    List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups();
  
    // loop through sync groups
    for (GcGrouperSyncGroup gcGrouperSyncGroup : groupUuidToSyncGroup.values()) {
  
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningGroupWrapper == null) {
        
        provisioningGroupsToDeleteCount++;
        
        // create a provisioning group to delete
        ProvisioningGroup provisioningGroup = new ProvisioningGroup();
        provisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        provisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        provisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        if (grouperProvisioningGroups == null) {
          grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningGroups(grouperProvisioningGroups);
        }
        grouperProvisioningGroups.add(provisioningGroup);
        
        provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
  
        provisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
        provisioningGroupWrapper.setGrouperProvisioningGroupIncludeDelete(provisioningGroup);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningMembershipsToDelete() {
  
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership();
  
    List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData()
        .getGrouperProvisioningObjects().getProvisioningMemberships();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (MultiKey groupUuidMemberUuid : GrouperUtil.nonNull(groupUuidMemberUuidToSyncMembership).keySet()) {
  
      String groupId = (String)groupUuidMemberUuid.getKey(0);
      String memberId = (String)groupUuidMemberUuid.getKey(1);
  
      GcGrouperSyncMembership gcGrouperSyncMembership = groupUuidMemberUuidToSyncMembership.get(groupUuidMemberUuid);
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupUuidMemberUuid);
      
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      if (provisioningMembershipWrapper == null) {
        
        provisioningMshipsToDelete++;
        
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          throw new RuntimeException("Cant find groupId: '" + groupId + "'");
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          throw new RuntimeException("Cant find entityId: '" + memberId + "'");
        }
        
        // create a provisioning group to delete
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        provisioningMembership.setProvisioningGroupId(groupId);
        provisioningMembership.setProvisioningEntityId(memberId);
        
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        } else if (provisioningGroupWrapper.getGrouperProvisioningGroupIncludeDelete() != null) {
            provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroupIncludeDelete());
        } else {
          throw new RuntimeException("Cant find provisioning group: '" + groupId + "'");
        }
  
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        } else if (provisioningEntityWrapper.getGrouperProvisioningEntityIncludeDelete() != null) {
            provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntityIncludeDelete());
        } else {
          throw new RuntimeException("Cant find provisioning entity: '" + memberId + "'");
        }
  
        if (grouperProvisioningMemberships == null) {
          grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningMemberships(grouperProvisioningMemberships);
        }
        grouperProvisioningMemberships.add(provisioningMembership);
        
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioningMembershipIncludeDelete(provisioningMembership);
        
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupUuidMemberUuid, provisioningMembershipWrapper);
        
      }
      provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
    }      
    if (provisioningMshipsToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningMshipsToDelete", provisioningMshipsToDelete);
    }
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingObjectsIncremental() {
    retrieveMissingGroupsIncremental();
    retrieveMissingEntitiesIncremental();
  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingGroupsIncremental() {
    
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsRetrieveMissingIncremental(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups())) {
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      
      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }
      if (this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().groupLinkMissing(gcGrouperSyncGroup)) {
        missingGroups.add(provisioningGroup);
      }
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingIncrementalGroupsForRetrieve", missingGroups);
    
    this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroups);

    // translate
    List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);
    
    // log this
    //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroups);
    
    //lets retrieve these
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroups, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
    
    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);

    this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetProvisioningObjectsMissingRetrieved().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsRetrieved);
    
    Map<Object, ProvisioningGroup> targetIdToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      targetIdToGrouperTargetGroup.put(grouperTargetGroup.getTargetId(), grouperTargetGroup);
    }
    
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroup grouperTargetGroup = targetIdToGrouperTargetGroup.get(targetGroup.getTargetId());
      
      // not sure why it wouldnt match or exist...
      grouperTargetGroup.getProvisioningGroupWrapper().setTargetProvisioningGroup(targetGroup);
    }

  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingEntitiesIncremental() {
    
  }

}
