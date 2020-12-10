package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
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
    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncStart(startTimestamp);

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
      // do not assign defaults to target
      // filter groups and manipulate attributes and types
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships(), true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
          this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships());
              
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
    }

    try {
      debugMap.put("state", "matchingIdTargetObjects");
      // assign matching id to target objects
      this.grouperProvisioner.retrieveGrouperTranslator().matchingIdTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
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
        List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
        List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
      }
      
      {
        List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
        List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
            grouperProvisioningEntities, false, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
    }

    try {
      debugMap.put("state", "manipulateGrouperTargetAttributes");
      List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
  
      List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroupsEntitiesAttributes);
    }
    
    try {
      debugMap.put("state", "matchingIdGrouperGroupsEntities");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
    }

    {
      debugMap.put("state", "indexMatchingIdGroups");
      
      // index the groups and entity matching ids
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
      
      debugMap.put("state", "indexMatchingIdEntities");
      
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
    }

    {
      debugMap.put("state", "assignRecalc");
      // everything in a full sync is a recalc
      for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
        provisioningGroupWrapper.setRecalc(true);
      }
      for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
        provisioningEntityWrapper.setRecalc(true);
      }
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
        provisioningMembershipWrapper.setRecalc(true);
      }

    }
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
        List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships();
        List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
            grouperProvisioningMemberships, false);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
      }    

    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
    }

    try {
      debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
      List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships();
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
  
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
    }

    try {
      debugMap.put("state", "matchingIdGrouperMemberships");
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships());
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
    }

    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();

    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
    this.grouperProvisioner.retrieveGrouperSyncDao().processResultsSelectMembershipsFull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.countInsertsUpdatesDeletes();

    RuntimeException runtimeException = null;
    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } catch (RuntimeException e) {
      runtimeException = e;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
      //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);

    }
  
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
      gcGrouperSync.setLastFullSyncStart(startTimestamp);
      gcGrouperSync.setLastFullSyncRun(nowTimestamp);

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(null);
      gcGrouperSyncJob.setErrorTimestamp(null);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
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
    if (GrouperClientUtils.isBlank(this.getGrouperProvisioner().getGcGrouperSyncLog().getStatus())) {
      this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.SUCCESS);
    }

  }

  /**
   * 
   */
  public void provisionIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GrouperProvisioningLogicIncremental grouperProvisioningLogicIncremental = this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental();

    try {
      // ######### STEP 1: check messages
      debugMap.put("state", "incrementalCheckMessages");
      grouperProvisioningLogicIncremental.incrementalCheckMessages();
      
      // ######### STEP 2: check for esb events
      debugMap.put("state", "incrementalCheckChangeLog");
      grouperProvisioningLogicIncremental.incrementalCheckChangeLog();
    } finally {      
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataUnprocessed);
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
    
      // ######### STEP 3: see if any actions happened before the last full sync
      debugMap.put("state", "filterByProvisioningFullSync");
      grouperProvisioningLogicIncremental.filterByProvisioningFullSync();
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

      debugMap.put("runFullSync", "true");

      runFullSyncFromIncremental(); 
      
    } else {
    
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 4: events without recalc that occurred during full sync (after start before finish), should be recalc'ed
        debugMap.put("state", "recalcActionsDuringFullSync");
        grouperProvisioningLogicIncremental.recalcEventsDuringFullSync();
      }
      
      // ######### STEP 5: look for errors based on algorithm and retry those actions
      debugMap.put("state", "addErrorsToQueue");
      grouperProvisioningLogicIncremental.addErrorsToQueue();
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 6: filter out non recalc actions captures in recalc
        debugMap.put("state", "filterNonRecalcActionsCapturedByRecalc");
        grouperProvisioningLogicIncremental.filterNonRecalcActionsCapturedByRecalc();
      }
  
//      // ######### STEP 7: organize recalc and non recalc requests groups
//      debugMap.put("state", "organizeRecalcAndNonRecalcRequestsGroups");
//      grouperProvisioningLogicIncremental.organizeRecalcAndNonRecalcRequestsGroups();

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 7: retrieve all group sync objects for 
        debugMap.put("state", "retrieveIncrementalSyncGroups");
        {
          this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncGroups();
          Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
          assignSyncObjectsToWrappersGroups(grouperSyncGroupIdToProvisioningGroupWrapper);
        }
        
        // ######### STEP 8: retrieve provisioning attributes for recalc groups and adjust sync objects
        debugMap.put("state", "retrieveProvisioningGroupAttributesAndFixGroupSync");
        grouperProvisioningLogicIncremental.retrieveProvisioningGroupAttributesAndFixGroupSync();
      
        // ######### STEP 9: filter if not provisionable
        debugMap.put("state", "filterByNotProvisionable");
        grouperProvisioningLogicIncremental.filterByGroupNotProvisionable();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 10: filter by group sync
        debugMap.put("state", "filterByGroupSync");
        grouperProvisioningLogicIncremental.filterByGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 11: convert to group sync
        debugMap.put("state", "convertToGroupSync");
        grouperProvisioningLogicIncremental.convertToGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 12: convert to full sync
        debugMap.put("state", "convertToFullSync");
        grouperProvisioningLogicIncremental.convertToFullSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

        debugMap.put("runFullSync", "true");

        runFullSyncFromIncremental(); 
        
      } else {
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 13: events without recalc that occurred during group sync (after start before finish), should be recalc'ed
          debugMap.put("state", "recalcActionsDuringGroupSync");
          grouperProvisioningLogicIncremental.recalcEventsDuringGroupSync();
        }
        
        this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataToProcess);

        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 14: retrieve all membership sync objects
          // ######### STEP 15: retrieve all members sync objects
          debugMap.put("state", "retrieveIncrementalSyncMemberships");
          {
            this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncMemberships();
            this.getGrouperProvisioner().retrieveGrouperSyncDao().retrieveIncrementalSyncMembers();
  
            Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
            assignSyncObjectsToWrappersMembers(grouperSyncMemberIdToProvisioningEntityWrapper);
  
            Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
            Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper 
              = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper();
            assignSyncObjectsToWrappersMemberships(grouperSyncGroupIdToProvisioningGroupWrapper, grouperSyncMemberIdToProvisioningEntityWrapper, grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper);
          }
  
          // ######### STEP 16: retrieve grouper data
          try {
            debugMap.put("state", "retrieveIncrementalDataFromGrouper");
            long start = System.currentTimeMillis();
            grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveGrouperDataIncremental();
            long retrieveGrouperDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveGrouperDataMillis", retrieveGrouperDataMillis);
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIncrementalDataFromGrouper);
          }
          
          // ######### STEP 17: filter unneeded actions
          debugMap.put("state", "filterUnneededActions");
          grouperProvisioningLogicIncremental.filterUnneededActions();
          
          // ######### STEP 18: convert inconsistent events to recalc
          debugMap.put("state", "convertInconsistentEventsToRecalc");
          grouperProvisioningLogicIncremental.convertInconsistentEventsToRecalc();
          
          // ######### STEP 19: convert inconsistent events to recalc
          debugMap.put("state", "copyIncrementalStateToWrappers");
          grouperProvisioningLogicIncremental.copyIncrementalStateToWrappers();
          
          // ######### STEP 20: resolve subjects for subject link if recalc or for subjects missing data
          try {
            debugMap.put("state", "retrieveSubjectLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink);
          }
        }
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 21: translate grouper groups/entities to target format
          try {
            debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");
  
            {
              List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
              List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
            }
            
            {
              List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
              List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(
                  grouperProvisioningEntities, false, false);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
            }    
  
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsEntitiesToTarget);
          }
  
          // ######### STEP 22: based on configs manipulate the defaults, types, etc for grouper target groups/entities translated attributes and fields
          try {
            debugMap.put("state", "manipulateGrouperTargetAttributes");
            List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
        
            List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
            
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroupsEntitiesAttributes);
          }
          
          // ######### STEP 23: calculate the matching id of grouper translated groups/entities
          try {
            debugMap.put("state", "matchingIdGrouperGroupsEntities");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
          }
  
          // ######### STEP 24: take all the matching ids of grouper groups/entities and index those for quick lookups
          {
            debugMap.put("state", "indexMatchingIdGroups");
            
            // index the groups and entity matching ids
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
            
            debugMap.put("state", "indexMatchingIdEntities");
            
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
            
  
          }
          
          // ######### STEP 25: recalc retrieve data from target
          try {
            debugMap.put("state", "retrieveIncrementalTargetData");
            long start = System.currentTimeMillis();
            grouperProvisioningLogicIncremental.retrieveIncrementalTargetData();
            long retrieveTargetDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveTargetDataMillis", retrieveTargetDataMillis);
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetDataIncremental);
          }

          // ######### STEP 26: target object attribute manipulation
          try {
            debugMap.put("state", "targetAttributeManipulation");
            // do not assign defaults to target
            // filter groups and manipulate attributes and types
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships(), true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups());
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities());
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
                this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships());
                    
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.targetAttributeManipulation);
          }
  
          // ######### STEP 27: matching id target objects
          try {
            debugMap.put("state", "matchingIdTargetObjects");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups());
            for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups())) {
              ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper().get(targetGroup.getMatchingId());
              if (provisioningGroupWrapper != null) {
                provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
              }
            }
            
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities());
            for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities())) {
              ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper().get(targetEntity.getMatchingId());
              if (provisioningEntityWrapper != null) {
                provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);
              }
            }

            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships());
            for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships())) {
              ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper().get(targetMembership.getMatchingId());
              if (provisioningMembershipWrapper != null) {
                provisioningMembershipWrapper.setTargetProvisioningMembership(targetMembership);
              }
            }

          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdTargetObjects);
          }
      
          // ######### STEP 28: create groups / entities
          debugMap.put("state", "createMissingGroups");
          createMissingGroupsFull();
  
          debugMap.put("state", "createMissingEntities");
          createMissingEntitiesFull();
  
          // ######### STEP 29: retrieve target group and entity link
          try {
            debugMap.put("state", "retrieveTargetGroupLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
            
            debugMap.put("state", "retrieveTargetEntityLink");
            this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.linkData);
          }
            
          // ######### STEP 30: translate grouper memberships to target format
          try {
            debugMap.put("state", "translateGrouperMembershipsToTarget");
  
            {
              List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships();
              List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(
                  grouperProvisioningMemberships, false);
              this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
            }    
  
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
          }

          // ######### STEP 31: based on configs manipulate the defaults, types, etc for grouper target memberships translated attributes and fields
          try {
            debugMap.put("state", "manipulateGrouperTargetMembershipAttributes");

            List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships();
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
            
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
          }
          
          // ######### STEP 32: calculate the matching id of grouper translated membership data
          try {
            debugMap.put("state", "matchingIdGrouperMemberships");
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships());
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
          }

          // ######### STEP 33: index matching ID of grouper and target objects
          debugMap.put("state", "indexMatchingIdOfGrouperObjects");
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();
            
          // ######### STEP 34: compare target objects
          try {
            debugMap.put("state", "compareTargetObjectsIncremental");
            this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
          } finally {
            this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
          }
          
          this.countInsertsUpdatesDeletes();
      
          // ######### STEP 35: send changes to target
          RuntimeException runtimeException = null;
          try {
            debugMap.put("state", "sendChangesToTarget");
            TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
            targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
            targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
            targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
          } catch (RuntimeException e) {
            runtimeException = e;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
            //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);
  
          }
        }
        
        {
          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

          GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
          gcGrouperSyncJob.setErrorMessage(null);
          gcGrouperSyncJob.setErrorTimestamp(null);
          gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
          if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
            gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
          }
          gcGrouperSyncJob.setPercentComplete(100);

          // do this in the right spot, after assigning correct sync info about sync
          int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
          this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
      
          this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
        }

        // TODO flesh this out, resolve subjects, linked cached data, etc, try individually again
//        this.getGrouperProvisioner().retrieveTargetDao().resolveErrors();
//        this.getGrouperProvisioner().retrieveTargetDao().sendErrorFixesToTarget();

//        this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedGroupsToTarget();
//        this.getGrouperProvisioner().getGrouperProvisioningLogicAlgorithm().syncGrouperTranslatedMembershipsToTarget();

        // make sure the sync objects are correct
//        new ProvisioningSyncIntegration().assignTarget(this.getGrouperProvisioner().getConfigId()).fullSync();

//        // step 1
//        debugMap.put("state", "retrieveData");
//        this.gcTableSyncConfiguration.getGcTableSyncSubtype().retrieveData(debugMap, this);
//        
//        this.gcGrouperSyncLog.setRecordsProcessed(Math.max(this.gcTableSyncOutput.getRowsSelectedFrom(), this.gcTableSyncOutput.getRowsSelectedTo()));
    //
//        if (this.gcGrouperSyncHeartbeat.isInterrupted()) {
//          debugMap.put("interrupted", true);
//          debugMap.put("state", "done");
//          gcGrouperSyncLog.setStatus(GcGrouperSyncLogState.INTERRUPTED);
//          return;
//        }
        if (GrouperClientUtils.isBlank(this.getGrouperProvisioner().getGcGrouperSyncLog().getStatus())) {
          this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.SUCCESS);
        }

      }
  
    }
    
    // ######### STEP 36: acknowledge messages
    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().acknowledgeMessagesProcessed();

  }

  public void storeAllSyncObjects() {
    {
      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }
  }

  public void runFullSyncFromIncremental() {
    
    // end out this provisioner
    storeAllSyncObjects();
    this.getGrouperProvisioner().provisionFinallyBlock();
    
    // run a full sync
    GrouperProvisioner grouperProvisionerFullSync = GrouperProvisioner.retrieveProvisioner(this.getGrouperProvisioner().getConfigId());
    grouperProvisionerFullSync.setGcGrouperSyncHeartbeat(this.getGrouperProvisioner().getGcGrouperSyncHeartbeat());
    Map<String, Object> newDebugMap = new LinkedHashMap<String, Object>();
    newDebugMap.put("incrementalDebugMap", GrouperUtil.mapToString(this.getGrouperProvisioner().getDebugMap()) + "\n\n");
    
    grouperProvisionerFullSync.setDebugMap(newDebugMap);
    grouperProvisionerFullSync.setGrouperProvisioningOutput(this.getGrouperProvisioner().getGrouperProvisioningOutput());
    grouperProvisionerFullSync.provision(GrouperProvisioningType.fullProvisionFull);
  }

  public void createMissingGroupsFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsInsert(), false)) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroupWrapper> missingGroupWrappers = new ArrayList<ProvisioningGroupWrapper>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      ProvisioningGroup provisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      
      if (provisioningGroup == null || !provisioningGroupWrapper.isRecalc()) {
        continue;
      }
      
      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();

      if (!gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }

      ProvisioningGroup targetGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      
      if (targetGroup != null) {
        continue;
      }
      
      missingGroups.add(provisioningGroup);
      missingGroupWrappers.add(provisioningGroupWrapper);
    }

    if (GrouperUtil.length(missingGroups) == 0) {
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForCreate", GrouperUtil.length(missingGroups));
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate);

    List<ProvisioningGroup> grouperTargetGroupsToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, true);

    if (GrouperUtil.length(grouperTargetGroupsToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoGroupsOnInsert", true);
      return;
    }
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroupsToInsert, null);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroupsToInsert, false, true, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroupsToInsert);

    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroupsToInsert);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroupsToInsert);
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
    
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetGroupsForCreate);

    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
        
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    //retrieve so we have a copy
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(targetGroups, true, false, false);
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsCreated);

    Map<Object, ProvisioningGroupWrapper> matchingIdToProvisioningGroupWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroupWrapper provisioningGroupWrapper = matchingIdToProvisioningGroupWrapper.get(targetGroup.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      provisioningGroupWrapper.setTargetProvisioningGroup(targetGroup);
      
      // this is already created!  :)
      provisioningGroupWrapper.setCreate(false);
    }
    
  }

  public void createMissingEntitiesFull() {
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesInsert(), false)) {
      return;
    }
      
    
    //do we have missing entities?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      
      ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      if (provisioningEntity == null || !provisioningEntityWrapper.isRecalc()) {
        continue;
      }
      
      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      
      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      
      ProvisioningEntity targetEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      
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
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningEntities(missingEntities);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate);
    
    // translate
    List<ProvisioningEntity> grouperTargetEntitiesToInsert = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(missingEntities, false, true);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntitiesToInsert);

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
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingCreated().setProvisioningEntities(targetEntities);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesCreated);

    // match these up with retrieved entities
    Map<Object, ProvisioningEntityWrapper> matchingIdToProvisioningEntityWrapper = grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper();
    
    // match these up with retrieved groups
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      
      // look up the grouper group that looked this up
      ProvisioningEntityWrapper provisioningEntityWrapper = matchingIdToProvisioningEntityWrapper.get(targetEntity.getMatchingId());
      
      // not sure why it wouldnt match or exist...
      provisioningEntityWrapper.setTargetProvisioningEntity(targetEntity);

    }

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
          // retrieve all the target data and put in GrouperProvisioningDataTarget
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningDataTarget()
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
    
    processTargetWrappers();
    
  }


  /**
   * take target data and add wrapper and add to data store
   */
  public void processTargetWrappers() {
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
    
    // add wrappers for all groups
    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningGroups())) {
      ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
      provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningGroupWrappers.add(provisioningGroupWrapper);

      provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
    }

    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers();
    
    // add wrappers for all groups
    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningEntities())) {
      ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningEntityWrappers.add(provisioningEntityWrapper);

      provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
    }
    
    Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers();
    
    // add wrappers for all groups
    for (ProvisioningMembership targetProvisioningMembership : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().getProvisioningMemberships())) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
      provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
      provisioningMembershipWrappers.add(provisioningMembershipWrapper);

      provisioningMembershipWrapper.setTargetProvisioningMembership(targetProvisioningMembership);
    }
    
  }

  public void retrieveGrouperDataFull() {
    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          // retrieve all grouper sync data and put in GrouperProvisioningDataSync
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperSyncDao().retrieveSyncDataFull();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataSyncGrouper
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataFull();
    
    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
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

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 

    GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
    gcGrouperSync.setGroupCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups()));
    gcGrouperSync.setUserCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities()));
    gcGrouperSync.setRecordsCount(
        GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningEntities())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningGroups())
        + GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects().getProvisioningMemberships())
        );
    
  }


  public void assignSyncObjectsToWrappers() {
    
    Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();
    assignSyncObjectsToWrappersGroups(grouperSyncGroupIdToProvisioningGroupWrapper);

    Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
    assignSyncObjectsToWrappersMembers(grouperSyncMemberIdToProvisioningEntityWrapper);

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    assignSyncObjectsToWrappersMemberships(grouperSyncGroupIdToProvisioningGroupWrapper,
        grouperSyncMemberIdToProvisioningEntityWrapper,
        groupUuidMemberUuidToProvisioningMembershipWrapper);
  }

  public void assignSyncObjectsToWrappersMemberships(
      Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper,
      Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper,
      Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper) {
    {

      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMemberships();

      Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

      if (GrouperUtil.length(gcGrouperSyncMemberships) > 0) {
        
        int syncMembershipReferenceMissing = 0;
        
        for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
          
          // data is not consistent just ignore for now
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperSyncGroupIdToProvisioningGroupWrapper.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
          if (provisioningGroupWrapper == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
                    
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperSyncMemberIdToProvisioningEntityWrapper.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
          if (provisioningEntityWrapper == null) {
            syncMembershipReferenceMissing++;
            continue;
          }
          GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
          
          MultiKey groupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(),
              gcGrouperSyncMember.getMemberId());
          
          ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
          
          if (provisioningMembershipWrapper == null) {
            provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
            provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
            groupUuidMemberUuidToProvisioningMembershipWrapper.put(groupIdMemberId, provisioningMembershipWrapper);
            this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().add(provisioningMembershipWrapper);
          }
          
          provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          
          MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId());
          provisioningMembershipWrapper.setSyncGroupIdSyncMemberId(syncGroupIdSyncMemberId);
          grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper.put(syncGroupIdSyncMemberId, provisioningMembershipWrapper);
        }
        if (syncMembershipReferenceMissing > 0) {
          this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
        }
      }
      
    }
  }

  public void assignSyncObjectsToWrappersMembers(
      Map<String, ProvisioningEntityWrapper> grouperSyncMemberIdToProvisioningEntityWrapper) {
    {
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();

      // loop through sync groups
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMembers())) {
    
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(gcGrouperSyncMember.getMemberId());
        
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
          memberUuidToProvisioningEntityWrapper.put(gcGrouperSyncMember.getMemberId(), provisioningEntityWrapper);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().add(provisioningEntityWrapper);
        }
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);

        grouperSyncMemberIdToProvisioningEntityWrapper.put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
      }
    }
  }

  public void assignSyncObjectsToWrappersGroups(
      Map<String, ProvisioningGroupWrapper> grouperSyncGroupIdToProvisioningGroupWrapper) {
    {
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

      // loop through sync groups
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncGroups())) {
    
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());
        
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
          groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(provisioningGroupWrapper);
        }
        provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
        
        grouperSyncGroupIdToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getId(), provisioningGroupWrapper);
      }
    }
  }
  
  public void retrieveGrouperDataIncremental() {

    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataGrouper
    this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncremental();

    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
//    // incrementals need to clone and setup sync objects as deletes
//    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().setupIncrementalClonesOfGroupProvisioningObjects();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // put the sync objects in their respective wrapper objects
    assignSyncObjectsToWrappers();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 
    
  }
  
  protected void countInsertsUpdatesDeletes() {
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships());
    
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




  public void calculateProvisioningDataToDelete() {
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }


  /**
   * take the sync members and see which ones do not correspond to a grouper member
   */
  public void calculateProvisioningEntitiesToDelete() {
  
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
  
    int provisioningEntitiesToDelete = 0;
  
    // loop through sync groups
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
  
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (grouperProvisioningEntity == null && gcGrouperSyncMember != null) {
        
        provisioningEntitiesToDelete++;
        
        grouperProvisioningEntity = new ProvisioningEntity();
        grouperProvisioningEntity.setId(gcGrouperSyncMember.getMemberId());
        //TODO select from grouper dao again, the subject might not be provisionable but it might exist in subject source
        grouperProvisioningEntity.setSubjectId(gcGrouperSyncMember.getSubjectId());
        grouperProvisioningEntity.assignAttributeValue("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
  
        provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
        provisioningEntityWrapper.setDelete(true);
        
        memberUuidToProvisioningMemberWrapper.put(grouperProvisioningEntity.getId(), provisioningEntityWrapper);
      }
        
      
    }
    
    if (provisioningEntitiesToDelete > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntitiesToDelete", provisioningEntitiesToDelete);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningGroupsToDelete() {
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    
    int provisioningGroupsToDeleteCount = 0;
  
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
  
    // loop through sync groups
    for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
  
      // if a group has been deleted in grouper_groups table but copy still exists in grouper_sync_group
      // we are sending the copy over to the target so that target can also delete
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (grouperProvisioningGroup == null && gcGrouperSyncGroup != null) {
        
        provisioningGroupsToDeleteCount++;
        
        // create a provisioning group to delete
        grouperProvisioningGroup = new ProvisioningGroup();
        grouperProvisioningGroup.setId(gcGrouperSyncGroup.getGroupId());
        grouperProvisioningGroup.setName(gcGrouperSyncGroup.getGroupName());
        grouperProvisioningGroup.setIdIndex(gcGrouperSyncGroup.getGroupIdIndex());
        
        provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
        provisioningGroupWrapper.setDelete(true);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
    }
  
  }


  /**
   * take the sync groups and see which ones do not correspond to a grouper group
   */
  public void calculateProvisioningMembershipsToDelete() {
  
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> gcGrouperSyncMemberIdToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncMemberIdToProvisioningEntityWrapper();
    Map<String, ProvisioningGroupWrapper> gcGrouperSyncGroupIdToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdToProvisioningGroupWrapper();

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    int provisioningMshipsToDelete = 0;
    
    // loop through sync groups
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {

      ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();

      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

      if (grouperProvisioningMembership == null && gcGrouperSyncMembership != null) {

        provisioningMshipsToDelete++;
        
        ProvisioningGroupWrapper provisioningGroupWrapper = gcGrouperSyncGroupIdToProvisioningGroupWrapper.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
        if (provisioningGroupWrapper == null) {
          throw new RuntimeException("Cant find groupId: '" + gcGrouperSyncMembership.getGrouperSyncGroupId() + "'");
        }
        ProvisioningEntityWrapper provisioningEntityWrapper = gcGrouperSyncMemberIdToProvisioningEntityWrapper.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
        if (provisioningEntityWrapper == null) {
          throw new RuntimeException("Cant find entityId: '" + gcGrouperSyncMembership.getGrouperSyncMemberId() + "'");
        }

        GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
        GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();

        // these can be null if in the index
        
        String groupId = gcGrouperSyncGroup.getGroupId();
        String memberId = gcGrouperSyncMember.getMemberId();


        // create a provisioning group to delete
        ProvisioningMembership provisioningMembership = new ProvisioningMembership();
        provisioningMembership.setProvisioningGroupId(groupId);
        provisioningMembership.setProvisioningEntityId(memberId);
        
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          provisioningMembership.setProvisioningGroup(provisioningGroupWrapper.getGrouperProvisioningGroup());
        } else {
          throw new RuntimeException("Cant find provisioning group: '" + groupId + "'");
        }
  
        // the group is either the provisioning group or provisioning group to delete
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          provisioningMembership.setProvisioningEntity(provisioningEntityWrapper.getGrouperProvisioningEntity());
        } else {
          throw new RuntimeException("Cant find provisioning entity: '" + memberId + "'");
        }
          
        provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembership);
        provisioningMembershipWrapper.setDelete(true);
        
        groupUuidMemberUuidToProvisioningMembershipWrapper.put(provisioningMembershipWrapper.getGroupIdMemberId(), provisioningMembershipWrapper);
        
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
    
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups())) {
      
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
    
    this.grouperProvisioner.retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsMissing().setProvisioningGroups(missingGroups);

    // log this
    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroups);

    // translate
    List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(missingGroups, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroups);

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

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjectsMissingRetrieved().setProvisioningGroups(targetGroups);

    this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsRetrieved);
    
    Map<Object, ProvisioningGroup> matchingIdToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      matchingIdToGrouperTargetGroup.put(grouperTargetGroup.getMatchingId(), grouperTargetGroup);
    }
    
    // set these in the wrapper so they are linked with grouper group
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      
      // look up the grouper group that looked this up
      ProvisioningGroup grouperTargetGroup = matchingIdToGrouperTargetGroup.get(targetGroup.getMatchingId());
      
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
