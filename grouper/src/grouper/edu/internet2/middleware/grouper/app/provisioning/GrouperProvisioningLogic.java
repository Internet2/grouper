package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsByGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncConfiguration;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncRowData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
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

    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.configure);

    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
    this.getGrouperProvisioner().getGcGrouperSyncJob().setLastSyncStart(startTimestamp);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().provision(this.grouperProvisioner);
    
  }

  /**
   * 
   */
  public void provisionFull() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());

    this.getGrouperProvisioner().getGcGrouperSyncJob().setErrorMessage(null);
    this.getGrouperProvisioner().getGcGrouperSyncJob().setErrorTimestamp(null);

    {
      debugMap.put("state", "propagateProvisioningAttributes");
      long start = System.currentTimeMillis();
      grouperProvisioner.propagateProvisioningAttributes();
      long propagateProvisioningAttributes = System.currentTimeMillis()-start;
      debugMap.put("propagateProvisioningAttributes_millis", propagateProvisioningAttributes);
      
      if (grouperProvisioner.getConfigId().startsWith("junitProvisioningAttributePropagationTest")) {
        // just testing attribute propagation
        return;
      }
    }
        
    // validate the perhaps throw exception
    this.validateAndThrowExceptionIfInvalid();
    
    debugMap.put("state", "retrieveAllDataFromGrouperAndTarget");
    grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveAllData();
    
    debugMap.put("state", "retrieveSubjectLink");
    this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();

    debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");

    {
      List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
      List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
      List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(
          grouperProvisioningEntities, false, false);
    }    

    {
      debugMap.put("state", "manipulateGrouperTargetAttributes");
      List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
      Set<ProvisioningGroup> affectedGroups = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(
          grouperTargetGroups, true, true, false, false);
      if (GrouperUtil.length(affectedGroups) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroups, affectedGroups);
      }
    }

    {
      List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
      Set<ProvisioningEntity> affectedEntities = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(
          grouperTargetEntities, true, true, false, false);
  
      if (GrouperUtil.length(affectedEntities) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetEntities, affectedEntities);
      }
    }
    
    try {
      debugMap.put("state", "matchingIdGrouperGroupsEntities");
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
    } finally {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
    }

    retrieveFullIndividualTargetGroupsAndEntities();
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualTargetGroupsAndEntities);
    
    {
      debugMap.put("state", "indexMatchingIdGroups");
      
      // index the groups and entity matching ids
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(null);
      
      debugMap.put("state", "indexMatchingIdEntities");
      
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(null);
    }

    assignRecalcForGroupsAndEntities();
    
    debugMap.put("state", "retrieveIndividualMissingGroups");
    this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualMissingGroups();
    
    debugMap.put("state", "retrieveIndividualMissingEntities");
    this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualMissingEntities();
    
    assignRecalcForGroupsAndEntities();
    
    debugMap.put("state", "loadDataToGrouper");
    long start = System.currentTimeMillis();
    grouperProvisioner.retrieveGrouperProvisioningLogic().loadDataToGrouper();
    long retrieveDataPass1 = System.currentTimeMillis()-start;
    debugMap.put("loadDataToGrouper_millis", retrieveDataPass1);

    debugMap.put("state", "insertGroups");
    createMissingGroupsFull();

    debugMap.put("state", "insertEntities");
    createMissingEntitiesFull();

    debugMap.put("state", "retrieveTargetGroupLink");
    this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
    
    debugMap.put("state", "retrieveTargetEntityLink");
    this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();

    // validate
    debugMap.put("state", "validateGroupsAndEntities");
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroupsHaveMembers(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false, false, false);

    retrieveFullIndividualTargetMemberships();
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualTargetMemberships);

    {
  
      debugMap.put("state", "translateGrouperMembershipsToTarget");
      {
        List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(false);
        List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(
            grouperProvisioningMemberships, false);
      }    

    }

    {
      debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
      List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false);
      
      Set<ProvisioningMembership> affectedMemberships = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(
          grouperTargetMemberships, true, true, false, false);
      if (GrouperUtil.length(affectedMemberships) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMemberships, affectedMemberships);
      }

    }

    try {
      debugMap.put("state", "matchingIdGrouperMemberships");
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false));
    } finally {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
    }

    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(null);
    
    if (this.grouperProvisioner.getProvisioningStateGlobal().isSelectResultProcessedMemberships()) {
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
        if (provisioningMembershipWrapper.getProvisioningGroupWrapper() != null) {
          provisioningMembershipWrapper.getProvisioningGroupWrapper().getProvisioningStateGroup().setSelectAllMembershipResultProcessed(true);
        }
      }
    }
    
    {
      debugMap.put("state", "retrieveIndividualMissingMemberships");
      this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualMissingMemberships(); 
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
      }
    }

    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectMembershipsFull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
    
    // validate memberships
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateMemberships(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false), false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, true, false);
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
    }
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafesAtStart();
    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafes();
    
    RuntimeException runtimeException = null;
    try {
      debugMap.put("state", "sendChangesToTarget");
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } catch (RuntimeException e) {
      runtimeException = e;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }

    }
  
    this.errorHandling();
    
    { 
      // counts for sync
      this.countInsertsUpdatesDeletes();

      // count total for full sync
      int totalCount = 0;
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveAll())) {
        if (gcGrouperSyncGroup.isInTarget()) {
          totalCount++;
        }
      }
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveAll())) {
        if (gcGrouperSyncMember.isInTarget()) {
          totalCount++;
        }
      }
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMembershipDao().membershipRetrieveAll())) {
        if (gcGrouperSyncMembership.isInTarget()) {
          totalCount++;
        }
      }
      GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningOutput().setTotalCount(totalCount);
      GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningOutput().copyToHib3LoaderLog();
    }
    
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
      gcGrouperSync.setLastFullSyncStart(startTimestamp);
      gcGrouperSync.setLastFullSyncRun(nowTimestamp);

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);
      // 257 this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao()
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

  public void assignRecalcForGroupsAndEntities() {
    
    this.grouperProvisioner.getDebugMap().put("state", "assignRecalc");
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectGroups()) {
    
      // everything in a full sync is a recalc if it can be
      for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
        provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(true);
        provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(true);
      }
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectEntities()) {
      for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
        provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(true);
        provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(true);
      }
    }
  }

  public void retrieveFullIndividualTargetGroupsAndEntities() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    long start = System.currentTimeMillis();
    debugMap.put("state", "retrieveIndividualEntitiesIfNeeded");
    
    // when select all entities is false e.g AWS then we need to fetch entities one by one.
    List<ProvisioningEntity> targetEntities = this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualTargetEntitiesIfNeeded();
    if (targetEntities != null) {
      processTargetDataEntities(targetEntities);
    }
    debugMap.put("state", "retrieveIndividualGroupsIfNeeded");
    
    // when select all groups is false e.g AWS then we need to fetch groups one by one.
    List<ProvisioningGroup> targetGroups = this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualTargetGroupsIfNeeded();
    if (targetGroups != null) {
      processTargetDataGroups(targetGroups);
    }
    
    // get the total count of what is in the target
    int totalTargetCount = 0;
            
    GrouperProvisioningLogic.this.grouperProvisioner.getDebugMap().put("originalTargetGroupCount", GrouperUtil.length(targetGroups));
    GrouperProvisioningLogic.this.grouperProvisioner.getDebugMap().put("originalTargetEntityCount", GrouperUtil.length(targetEntities));

    totalTargetCount += GrouperUtil.length(targetEntities)
        + GrouperUtil.length(targetGroups);

    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "originalTargetTotalCount", totalTargetCount);
    
    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "targetGroupsRetrieved", GrouperUtil.length(targetGroups));
    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "targetEntitiesRetrieved", GrouperUtil.length(targetEntities));

    long retrieveTargetDataMillis = System.currentTimeMillis()-start;
    debugMap.put("retrieveTargetGroupsAndEntitiesMillis", retrieveTargetDataMillis);

  }

  public void retrieveFullIndividualTargetMemberships() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    long start = System.currentTimeMillis();

    debugMap.put("state", "retrieveIndividualMembershipsIfNeeded");
    
    // when select all memberships is false fetch groups one by one.
    List<ProvisioningMembership> targetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveIndividualTargetMembershipsIfNeeded();
    if (targetMemberships != null) {
      processTargetDataMemberships(targetMemberships);
    }

    if (this.grouperProvisioner.getProvisioningStateGlobal().isSelectResultProcessedIndividualMemberships()) {
      Set<ProvisioningMembershipWrapper> membershipWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers();
      
      for (ProvisioningMembershipWrapper membershipWrapper: membershipWrappers) {
        membershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
      }
    }
    
    // get the total count of what is in the target
    int totalTargetCount = 0;
            
    int originalTargetMembershipCount = GrouperUtil.length(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships());
    
    String membershipAttribute = GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    if (!StringUtils.isBlank(membershipAttribute)) {
      if (GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
          == GrouperProvisioningBehaviorMembershipType.groupAttributes) {

        for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups())) {
          originalTargetMembershipCount += GrouperUtil.length(provisioningGroup.retrieveAttributeValueSet(membershipAttribute));
        }
      }
      if (GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
          == GrouperProvisioningBehaviorMembershipType.entityAttributes) {

        for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities())) {
          originalTargetMembershipCount += GrouperUtil.length(provisioningEntity.retrieveAttributeValueSet(membershipAttribute));
        }
      }
    }
    totalTargetCount += originalTargetMembershipCount;

    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "originalTargetMembershipCount", originalTargetMembershipCount);
    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "originalTargetTotalCount", totalTargetCount);
    
    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "targetMembershipsRetrieved", GrouperUtil.length(targetMemberships));

    long retrieveTargetDataMillis = System.currentTimeMillis()-start;
    debugMap.put("retrieveTargetMembershipsMillis", retrieveTargetDataMillis);

  }

  public void validateAndThrowExceptionIfInvalid() {
    int fatalValidationProblems = 0;
    int nonfatalValidationProblems = 0;
    
    List<ProvisioningValidationIssue> validationIssues = this.grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validate();
    
    ProvisioningValidationIssue fatalError = null;
    for (ProvisioningValidationIssue provisioningValidationIssue : GrouperUtil.nonNull(validationIssues)) {
      if (provisioningValidationIssue.isRuntimeError()) {
        if (fatalError == null) {
          fatalError = provisioningValidationIssue;
        }
        fatalValidationProblems++;
      } else {
        nonfatalValidationProblems++;
      }
    }

    if (fatalValidationProblems > 0) {
      this.getGrouperProvisioner().getDebugMap().put("fatalValidationProblems", fatalValidationProblems);
    }
    if (nonfatalValidationProblems > 0) {
      this.getGrouperProvisioner().getDebugMap().put("nonfatalValidationProblems", nonfatalValidationProblems);
    }
    
    if (fatalError != null) {
      throw new RuntimeException("Fatal validation problem: " + fatalError.getMessage());
    }
  }

  public void loadDataToGrouper() {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isLoadEntitiesToGrouperTable()) {
      return;
    }
    
 // we need to get data from the table
    GcTableSync loadUsersToTableGcTableSync = new GcTableSync();
    
    //retrieve the existing data from database
    GcTableSyncTableBean loadUsersToTableGcTableSyncTableBeanSql = new GcTableSyncTableBean(loadUsersToTableGcTableSync);
    
    GrouperProvisioningLoader grouperProvisioningLoader = this.grouperProvisioner.retrieveGrouperProvisioningLoader();
    String tableName = grouperProvisioningLoader.getLoaderEntityTableName();
    GrouperUtil.assertion(StringUtils.isNotBlank(tableName), "grouperLoaderEntityTableName is blank.");
    
    
    loadUsersToTableGcTableSyncTableBeanSql.configureMetadata("grouper", tableName);
    loadUsersToTableGcTableSync.setDataBeanTo(loadUsersToTableGcTableSyncTableBeanSql);

    Set<String> databaseColumnNames = new LinkedHashSet(grouperProvisioningLoader.getLoaderEntityColumnNames());

    Set<String> loadUsersToTableUniqueKeyColumnNames = new LinkedHashSet(grouperProvisioningLoader.getLoaderEntityKeyColumnNames());

    GcTableSyncTableMetadata gcTableSyncTableMetadata = loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata();

    gcTableSyncTableMetadata.assignColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
    gcTableSyncTableMetadata.assignPrimaryKeyColumns(GrouperUtil.join(loadUsersToTableUniqueKeyColumnNames.iterator(), ','));
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");
    
    String configId = this.getGrouperProvisioner().getConfigId();
    
    String sql = "select " + gcTableSyncTableMetadata.columnListAll() + " from " + gcTableSyncTableMetadata.getTableName() + " where config_id = ?";
    
    List<Object[]> results = gcDbAccess.sql(sql).addBindVar(configId).selectList(Object[].class);

    GcTableSyncTableData loadUsersToTableGcTableSyncTableDataSql = new GcTableSyncTableData();
    loadUsersToTableGcTableSyncTableDataSql.init(loadUsersToTableGcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().columnListAll()), results);
    loadUsersToTableGcTableSyncTableDataSql.indexData();
 
    loadUsersToTableGcTableSyncTableBeanSql.setDataInitialQuery(loadUsersToTableGcTableSyncTableDataSql);
    loadUsersToTableGcTableSyncTableBeanSql.setGcTableSync(loadUsersToTableGcTableSync);

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    debugMap.put("loadUsersDbUniqueKeys", loadUsersToTableGcTableSyncTableDataSql.allPrimaryKeys().size());
    
    GcTableSyncTableBean gcTableSyncTableBeanFrom = new GcTableSyncTableBean();
    loadUsersToTableGcTableSync.setDataBeanFrom(gcTableSyncTableBeanFrom);
    gcTableSyncTableBeanFrom.setTableMetadata(loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata());
    gcTableSyncTableBeanFrom.setGcTableSync(loadUsersToTableGcTableSync);

    GcTableSyncTableData loadUsersToTableGcTableSyncTableDataLdap = new GcTableSyncTableData();
    loadUsersToTableGcTableSync.getDataBeanFrom().setDataInitialQuery(loadUsersToTableGcTableSyncTableDataLdap);

    loadUsersToTableGcTableSyncTableDataLdap.setColumnMetadata(loadUsersToTableGcTableSyncTableDataSql.getColumnMetadata());

    loadUsersToTableGcTableSyncTableDataLdap.setGcTableSyncTableBean(loadUsersToTableGcTableSyncTableDataSql.getGcTableSyncTableBean());

    List<GcTableSyncRowData> gcTableSyncRowDatas = new ArrayList<GcTableSyncRowData>();
    
    List<Object[]> targetTableData = grouperProvisioningLoader.retrieveLoaderEntityTableDataFromDataBean();
    
    for (Object[] rowData: targetTableData) {
      
      GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
      gcTableSyncRowDatas.add(gcTableSyncRowData);
      
      gcTableSyncRowData.setGcTableSyncTableData(loadUsersToTableGcTableSyncTableDataLdap);
      
      gcTableSyncRowData.setData(rowData);
      
    }
    
    
    loadUsersToTableGcTableSyncTableDataLdap.setRows(gcTableSyncRowDatas);

    // compare and sync
    GcTableSyncConfiguration gcTableSyncConfiguration = new GcTableSyncConfiguration();
    loadUsersToTableGcTableSync.setGcTableSyncConfiguration(gcTableSyncConfiguration);

    loadUsersToTableGcTableSync.setGcTableSyncOutput(new GcTableSyncOutput());

    Map<String, Object> debugMapLocal = new LinkedHashMap<String, Object>();
    GcTableSyncSubtype.fullSyncFull.syncData(debugMapLocal, loadUsersToTableGcTableSync);

    // merge the debug maps
    for (String key : debugMapLocal.keySet()) {
      
      Object newValue = debugMapLocal.get(key);
 
      // convert micros to millis
      if (key.endsWith("Millis")) {
        if (newValue instanceof Number) {
          newValue = ((Number)newValue).longValue()/1000;
        }
      }

      
      String newKey = "loadUsers" + StringUtils.capitalize(key);
      debugMap.put(newKey, newValue);

    }
    
  }

  /**
   * when data was retrieved (i.e. when the group syncs start)
   */
  private long retrieveDataStartMillisSince1970 = -1;
  
  /**
   * when data was retrieved (i.e. when the group syncs start)
   * @return when data retrieved
   */
  public long getRetrieveDataStartMillisSince1970() {
    return retrieveDataStartMillisSince1970;
  }
  
  /**
   * 
   */
  public List<ProvisioningEntity> retrieveIndividualTargetEntitiesIfNeeded() {
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntitiesAll() || !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      return null;
    }
    
    // Step 1 - Get all the grouper target entities and select them from the target (Call the batch method that gets all at once)
    
    List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
    
    for (ProvisioningEntity provisioningEntity: grouperTargetEntities) {
      provisioningEntity.getProvisioningEntityWrapper().getProvisioningStateEntity().setSelectResultProcessed(true);
    }
    
    TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest = new TargetDaoRetrieveEntitiesRequest(grouperTargetEntities, true);
    
    TargetDaoRetrieveEntitiesResponse targetEntitiesResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(targetDaoRetrieveEntitiesRequest);

    return targetEntitiesResponse.getTargetEntities();
    

  }

  /**
   * 
   */
  public List<ProvisioningGroup> retrieveIndividualTargetGroupsIfNeeded() {
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroupsAll() || !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      return null;
    }
    
    // Step 1 - Get all the grouper target entities and select them from the target (Call the batch method that gets all at once)
    
    List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
    
    for (ProvisioningGroup provisioningGroup: grouperTargetGroups) {
      provisioningGroup.getProvisioningGroupWrapper().getProvisioningStateGroup().setSelectResultProcessed(true);
    }
    
    TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest = new TargetDaoRetrieveGroupsRequest(grouperTargetGroups, true);
    
    TargetDaoRetrieveGroupsResponse targetGroupsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(targetDaoRetrieveGroupsRequest);

    return targetGroupsResponse.getTargetGroups();
    
  }

  /**
   * 
   */
  public void provisionIncremental() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();


    GrouperProvisioningLogicIncremental grouperProvisioningLogicIncremental = this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental();

    this.getGrouperProvisioner().getGcGrouperSyncJob().setErrorMessage(null);
    this.getGrouperProvisioner().getGcGrouperSyncJob().setErrorTimestamp(null);

    this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafesAtStart();

    try {
      // ######### STEP 1: propagate provisioning data to group sync table
      debugMap.put("state", "propagateProvisioningAttributes");
      grouperProvisioningLogicIncremental.propagateProvisioningAttributes();
      
      // validate the perhaps throw exception
      this.validateAndThrowExceptionIfInvalid();

      // ######### STEP 2: check messages
      debugMap.put("state", "incrementalCheckMessages");
      grouperProvisioningLogicIncremental.incrementalCheckMessages();
      
      if (this.getGrouperProvisioner().getConfigId().startsWith("junitProvisioningAttributePropagationTest")) {
        // just testing attribute propagation
        return;
      }
      
      // ######### STEP 3: check for esb events
      debugMap.put("state", "incrementalCheckChangeLog");
      grouperProvisioningLogicIncremental.incrementalCheckChangeLog();
    } finally {      
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataUnprocessed);
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
    
      // ######### STEP 4: see if any actions happened before the last full sync
      debugMap.put("state", "filterByProvisioningFullSync");
      grouperProvisioningLogicIncremental.filterByProvisioningFullSync();
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

      debugMap.put("runFullSync", "true");

      runFullSyncFromIncremental(); 
      
    } else {
    
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 5: events without recalc that occurred during full sync (after start before finish), should be recalc'ed
        debugMap.put("state", "recalcActionsDuringFullSync");
        grouperProvisioningLogicIncremental.recalcEventsDuringFullSync();
      }
      
      // ######### STEP 6: look for errors based on algorithm and retry those actions
      debugMap.put("state", "addErrorsToQueue");
      grouperProvisioningLogicIncremental.addErrorsToQueue();
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 7: filter out non recalc actions captures in recalc
        debugMap.put("state", "filterNonRecalcActionsCapturedByRecalc");
        grouperProvisioningLogicIncremental.filterNonRecalcActionsCapturedByRecalc();
      }
  
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 8: retrieve all group sync objects for 
        debugMap.put("state", "retrieveIncrementalSyncGroups");
        {
          this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().retrieveIncrementalSyncGroups("Pass1");
        }
        
        
        // ######### STEP 9: filter if not provisionable
        debugMap.put("state", "filterByNotProvisionable");
        grouperProvisioningLogicIncremental.filterByGroupNotProvisionable();
        
        // ######### STEP 10: retrieve all member sync objects for 
        debugMap.put("state", "retrieveIncrementalSyncMembers");
        {
          this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().retrieveIncrementalSyncMembers("Pass1");
        }
        
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 11: filter by group sync
        debugMap.put("state", "filterByGroupSync");
        grouperProvisioningLogicIncremental.filterByGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 11: filter by entity sync
        debugMap.put("state", "filterByEntitySync");
        grouperProvisioningLogicIncremental.filterByEntitySync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 12: convert to group sync
        debugMap.put("state", "convertToGroupSync");
        grouperProvisioningLogicIncremental.convertToGroupSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
        // ######### STEP 13: convert to full sync
        debugMap.put("state", "convertToFullSync");
        grouperProvisioningLogicIncremental.convertToFullSync();
      }
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isFullSync()) {

        debugMap.put("runFullSync", "true");

        runFullSyncFromIncremental(); 
        
      } else {
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 14: events without recalc that occurred during group sync (after start before finish), should be recalc'ed
          debugMap.put("state", "recalcActionsDuringGroupSync");
          grouperProvisioningLogicIncremental.recalcEventsDuringGroupSync();
          debugMap.put("state", "recalcActionsDuringEntitySync");
          grouperProvisioningLogicIncremental.recalcEventsDuringEntitySync();
        }
        
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.logIncomingDataToProcess);

        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 15: retrieve grouper groups entities
          {
            debugMap.put("state", "retrieveIncrementalGrouperGroupsEntities");
            long start = System.currentTimeMillis();
            this.retrieveDataStartMillisSince1970 = start;
            // keep track of when this started so we can update when group syncs occurred
            debugMap.put("retrieveDataStartMillisSince1970", start);
            this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveGrouperDataIncrementalGroupsEntities();
            long retrieveGrouperDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveGrouperGroupsEntitiesMillis", retrieveGrouperDataMillis);
          }
          
          // ######### STEP 16: convert groups and entities to recalc when needed
          // e.g. a new group should be recalc to be sure
          debugMap.put("state", "convertGroupsToRecalc");
          grouperProvisioningLogicIncremental.convertGroupsToRecalc(true);
          debugMap.put("state", "convertEntitiesToRecalc");
          grouperProvisioningLogicIncremental.convertEntitiesToRecalc(true);

          // ######### STEP 17: retrieve all membership sync objects
          debugMap.put("state", "retrieveIncrementalSyncMemberships");
          {
            
            this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().retrieveIncrementalSyncMemberships(); 
            this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().retrieveIncrementalSyncGroups("Pass2");
            this.getGrouperProvisioner().retrieveGrouperProvisioningSyncDao().retrieveIncrementalSyncMembers("Pass2");
  
          }

          // ######### STEP 19: retrieve grouper data memberships
          try {
            debugMap.put("state", "retrieveIncrementalMembershipsFromGrouper");
            long start = System.currentTimeMillis();
            this.retrieveDataStartMillisSince1970 = start;
            // keep track of when this started so we can update when group syncs occurred
            debugMap.put("retrieveDataStartMillisSince1970", start);
            this.grouperProvisioner.retrieveGrouperProvisioningLogic().retrieveGrouperDataIncrementalMemberships();
            long retrieveGrouperDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveGrouperMembershipsMillis", retrieveGrouperDataMillis);
          } finally {
            this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveDataFromGrouper);
          }

          // ######### STEP 20: filter unneeded actions
          // e.g action is no-op for example user is already in target and also in grouper so it's a no-op, don't send it to target 
          debugMap.put("state", "filterUnneededActions");
          grouperProvisioningLogicIncremental.filterUnneededActions();
          
          // ######### STEP 20.1: convert inconsistent events to recalc
          // e.g. change log action is delete and membership does exist in grouper so we don't know who is right so in that case
          // we just recalc to be on the safe side
          debugMap.put("state", "convertInconsistentMembershipEventActions");
          grouperProvisioningLogicIncremental.convertInconsistentMembershipEventActions();
          
          // ######### STEP 20.2: convert groups and entities to membership sync
          // e.g. if any memberships are recalc, recalc them all for that group/entity
          debugMap.put("state", "convertMembershipsToIncrementalSync");
          grouperProvisioningLogicIncremental.convertMembershipsToIncrementalSync();

          // ######### STEP 20.3: convert groups and entities to recalc when needed
          // e.g. new groups/entities from extra memberships
          debugMap.put("state", "convertGroupsToRecalc");
          grouperProvisioningLogicIncremental.convertGroupsToRecalc(false);
          debugMap.put("state", "convertEntitiesToRecalc");
          grouperProvisioningLogicIncremental.convertEntitiesToRecalc(false);


          // ######### STEP 21: resolve subjects for subject link if recalc or for subjects missing data
          debugMap.put("state", "retrieveSubjectLink");
          this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().retrieveSubjectLink();
        }
        
        if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().isHasIncrementalDataToProcess()) {
          // ######### STEP 22: translate grouper groups/entities to target format
          {
            debugMap.put("state", "translateGrouperGroupsEntitiesToTarget");
            List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
            List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
          }
          
          {
            List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
            
            List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(
                grouperProvisioningEntities, false, false);
          }    
  
          // ######### STEP 23: based on configs manipulate the defaults, types, etc for grouper target groups/entities translated attributes and fields
          {
            
            debugMap.put("state", "manipulateGrouperTargetGroups");
            List<ProvisioningGroup> grouperTargetGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups();
            Set<ProvisioningGroup> affectedGroups = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(
                grouperTargetGroups, true, true, false, false);
            if (GrouperUtil.length(affectedGroups) > 0) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetGroups, affectedGroups);
            }              
          }
          {
            List<ProvisioningEntity> grouperTargetEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities();
            Set<ProvisioningEntity> affectedEntities = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(
                grouperTargetEntities, true, true, false, false);
            if (GrouperUtil.length(affectedEntities) > 0) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetEntities, affectedEntities);
            }

          }
          
          // ######### STEP 24: calculate the matching id of grouper translated groups/entities
          try {
            debugMap.put("state", "matchingIdGrouperGroupsEntities");
            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetEntities());
          } finally {
            this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperGroupsEntities);
          }
  
          // ######### STEP 25: take all the matching ids of grouper groups/entities and index those for quick lookups
          {
            debugMap.put("state", "indexMatchingIdGroups");
            // index the groups and entity matching ids
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(null);
            
            debugMap.put("state", "indexMatchingIdEntities");
            
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(null);
            
          }
          
          // ######### STEP 26: validate groups
          // validate
          debugMap.put("state", "validate");
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, false, true);
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroupsHaveMembers(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false);
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false, false, true);

          // ######### STEP 27: retrieve recalc groups and entities from target
          try {
            
            grouperProvisioningLogicIncremental.determineGroupsToSelect();
            grouperProvisioningLogicIncremental.determineEntitiesToSelect();
            
            debugMap.put("state", "retrieveIncrementalTargetGroupsAndEntities");
            
            long start = System.currentTimeMillis();
            GrouperProvisioningLists targetProvisioningLists = grouperProvisioningLogicIncremental.retrieveIncrementalTargetGroupsAndEntities();
            if (targetProvisioningLists != null) {
              processTargetDataEntities(targetProvisioningLists.getProvisioningEntities());
              processTargetDataGroups(targetProvisioningLists.getProvisioningGroups());
            }
            long retrieveTargetDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveIncrementalTargetGroupsAndEntitiesMillis", retrieveTargetDataMillis);
          } finally {
            this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetDataGroupsAndEntities);
          }

          // ######### STEP 30: create groups / entities
          debugMap.put("state", "insertGroups");
          createMissingGroupsFull();
  
          debugMap.put("state", "insertEntities");
          createMissingEntitiesFull();
  
          // ######### STEP 31: retrieve target group and entity link
          debugMap.put("state", "updateGroupLinkFull");
          this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateGroupLinkFull();
          
          debugMap.put("state", "updateEntityLinkFull");
          this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().updateEntityLinkFull();
          
          
          // ######### STEP 35: index matching ID of grouper and target objects
          debugMap.put("state", "indexMatchingIdOfGrouperObjects");
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(null);
          this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(null);

          // ######### STEP 31.5: convert memberships to recalc when needed
          // e.g. inconsistent membership should be recalc
          debugMap.put("state", "convertMembershipsToRecalc");
          grouperProvisioningLogicIncremental.convertMembershipsToRecalc();


          // ######### STEP 32: translate grouper memberships to target format
          {
            debugMap.put("state", "translateGrouperMembershipsToTarget");
  
            {
              List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(false);
              List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(
                  grouperProvisioningMemberships, true);
              this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget, grouperTargetMemberships);
            }    
  
          }

          // ######### STEP 33: based on configs manipulate the defaults, types, etc for grouper target memberships translated attributes and fields
          {
            debugMap.put("state", "manipulateGrouperTargetMembershipAttributes");

            List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false);
            
            Set<ProvisioningMembership> affectedMemberships = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(
                grouperTargetMemberships, true, true, false, false);
            if (GrouperUtil.length(affectedMemberships) > 0) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMemberships, affectedMemberships);
            }
          }
          
          // ######### STEP 34: calculate the matching id of grouper translated membership data
          try {
            debugMap.put("state", "matchingIdGrouperMemberships");
            this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false));
            
          } finally {
            if (GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
                == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
            }
          }
          
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateMemberships(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(false), true);

          grouperProvisioningLogicIncremental.determineMembershipsToSelect();
          
          // ######## Retrieve memberships from target that are recalc where the group is not recalc
          {
            debugMap.put("state", "retrieveIncrementalTargetMemberships");
            long start = System.currentTimeMillis();
            GrouperProvisioningLists targetProvisioningLists = grouperProvisioningLogicIncremental.retrieveIncrementalTargetMemberships();
            
            if (targetProvisioningLists != null) {
              processTargetDataGroups(targetProvisioningLists.getProvisioningGroups());
              processTargetDataEntities(targetProvisioningLists.getProvisioningEntities());
              processTargetDataMemberships(targetProvisioningLists.getProvisioningMemberships());
            }
            
//            grouperProvisioningLogicIncremental.retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc();
            long retrieveTargetDataMillis = System.currentTimeMillis()-start;
            debugMap.put("retrieveIncrementalTargetMemberships", retrieveTargetDataMillis);
          }
          {
            // index the memberships
            this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(null);

//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
            this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectGroupsFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
//            }
            
//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
            this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectEntitiesFull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
//            }
            
//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
            this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsSelectMembershipsFull(
                this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers(),
                this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers(),
                this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
//            }
            
//            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), false, true, false);
//            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), false, true, false);
          }
          
          // ######### Mark memberships retrieved by group or entity as recalc
          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
            if (provisioningMembershipWrapper.getTargetProvisioningMembership() != null) {
              provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
            }
          }
          
          // ######### STEP 36: compare target objects
          try {
            debugMap.put("state", "compareTargetObjectsIncremental");
            this.grouperProvisioner.retrieveGrouperProvisioningCompare().compareTargetObjects();
          } finally {
            this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.compareTargetObjects);
          }
                
          this.getGrouperProvisioner().retrieveGrouperProvisioningFailsafe().processFailsafes();

          // ######### STEP 37: send changes to target
          RuntimeException runtimeException = null;
          try {
            debugMap.put("state", "sendChangesToTarget");
            TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
            targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
            targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
            targetDaoSendChangesToTargetRequest.setTargetObjectReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
            targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
          } catch (RuntimeException e) {
            runtimeException = e;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInserts(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts());
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsUpdatesFull(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates());
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsReplaces(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces());
              this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsDeletes(this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes());
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
            //TODO this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.sendChangesToTarget);
  
          }
          
          this.countInsertsUpdatesDeletes();

          this.errorHandling();

        }
        
        {
          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          
          List<EsbEventContainer> esbEventContainers = this.grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers();
          if (GrouperUtil.length(esbEventContainers) > 0) {
            EsbEventContainer lastEvent = esbEventContainers.get(esbEventContainers.size()-1);
            Long createdOnMicros = lastEvent.getEsbEvent().getCreatedOnMicros();
            if (createdOnMicros != null) {
              Timestamp incrementalTimeStamp = new Timestamp(createdOnMicros/1000);
              gcGrouperSync.setIncrementalTimestamp(incrementalTimeStamp);
            }
          }
          
          gcGrouperSync.setLastIncrementalSyncRun(nowTimestamp);
          
          GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();

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

  /**
   * 
   * @param objectTypeErrorCodeToCount
   * @param gcGrouperSyncErrorCode
   * @return true if error
   */
  public boolean errorHandlingHandleError(Map<MultiKey, Integer> objectTypeErrorCodeToCount, ProvisioningUpdatableWrapper provisioningUpdatableWrapper, GcGrouperSyncErrorCode gcGrouperSyncErrorCode, String errorMessage, Exception exception) {
    
    boolean logErrors = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingLogErrors();
    boolean daemonShouldFail = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingProvisionerDaemonShouldFailOnObjectError();

    // we arent doing anything so ignore
    if (!logErrors && !daemonShouldFail) {
      return false;
    }
    
    // is this an error?
    boolean errorCodeIsError = true;
    boolean errorCodeIsLoggable = true;
    switch(gcGrouperSyncErrorCode) {
      case DNE:
        if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingTargetObjectDoesNotExistIsAnError()) {
          errorCodeIsError = false;
        }
        break;
      case INV:
        if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingInvalidDataIsAnError()) {
          errorCodeIsError = false;
        }
        break;
      case LEN:
        if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingLengthValidationIsAnError()) {
          errorCodeIsError = false;
        }
        break;
      case MEM:
        errorCodeIsError = false;
        errorCodeIsLoggable = false;
        break;
      case REQ:
        if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingRequiredValidationIsAnError()) {
          errorCodeIsError = false;
        }
        break;
      case MAT:
        if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isErrorHandlingMatchingValidationIsAnError()) {
          errorCodeIsError = false;
        }
        break;
      case ERR:
        break;
      default:
        throw new RuntimeException("Not expecting error code! " + gcGrouperSyncErrorCode);
    }
    
    if (!errorCodeIsError) {
      return false;
    }
    if (daemonShouldFail) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningOutput().getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.ERROR.name());
      this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR);
    }

    MultiKey objectTypeErrorCode = new MultiKey(provisioningUpdatableWrapper.objectTypeName(), gcGrouperSyncErrorCode);
    int count = GrouperUtil.mapAddValueObjectKey((Map<Object, Object>)(Object)objectTypeErrorCodeToCount, objectTypeErrorCode, 1);
    int maxCount = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getErrorHandlingLogCountPerType();

    // is this an update?  maybe?
    this.grouperProvisioner.retrieveGrouperProvisioningOutput().addRecordsWithUpdateErrors(1);
    
    if (errorCodeIsLoggable && count <= maxCount) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().error("Error with " + provisioningUpdatableWrapper.objectTypeName() + ", " + provisioningUpdatableWrapper.toStringForError() + ", " + gcGrouperSyncErrorCode + ", " + errorMessage, exception);
    }
    return true;
  }
  
  public void errorHandling() {

    Map<MultiKey, Integer> objectTypeErrorCodeToCount = new HashMap<MultiKey, Integer>();

    boolean hasError = false;
    
    // look at errors
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      Exception exception = grouperTargetGroup == null ? null : grouperTargetGroup.getException();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup == null) {
        continue;
      }
      if (gcGrouperSyncGroup.getErrorCode() == null) {
        continue;
      }
      hasError = errorHandlingHandleError(objectTypeErrorCodeToCount,  provisioningGroupWrapper, gcGrouperSyncGroup.getErrorCode(), gcGrouperSyncGroup.getErrorMessage(), exception) || hasError;
    }

    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      Exception exception = grouperTargetEntity == null ? null : grouperTargetEntity.getException();
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      if (gcGrouperSyncMember == null) {
        continue;
      }
      if (gcGrouperSyncMember.getErrorCode() == null) {
        continue;
      }
      hasError = errorHandlingHandleError(objectTypeErrorCodeToCount,  provisioningEntityWrapper, gcGrouperSyncMember.getErrorCode(), gcGrouperSyncMember.getErrorMessage(), exception) || hasError;
    }

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      Exception exception = grouperTargetMembership == null ? null : grouperTargetMembership.getException();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      if (gcGrouperSyncMembership == null) {
        continue;
      }
      if (gcGrouperSyncMembership.getErrorCode() == null) {
        continue;
      }
      hasError = errorHandlingHandleError(objectTypeErrorCodeToCount,  provisioningMembershipWrapper, gcGrouperSyncMembership.getErrorCode(), gcGrouperSyncMembership.getErrorMessage(), exception) || hasError;
    }

    if (!hasError) {
      return;
    }
    StringBuilder errorSummary = new StringBuilder();
    
    for (MultiKey objectTypeErrorCode : objectTypeErrorCodeToCount.keySet()) {
      if (errorSummary.length() != 0) {
        errorSummary.append(", ");
      }
      errorSummary.append(objectTypeErrorCode.getKey(0) + " error " + objectTypeErrorCode.getKey(1) + " count " + objectTypeErrorCodeToCount.get(objectTypeErrorCode));
    }

    String errorSummaryString = errorSummary.toString();
    this.getGrouperProvisioner().getDebugMap().put("objectErrors", errorSummaryString);
    
    if (hasError && this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isErrorHandlingProvisionerDaemonShouldFailOnObjectError()) {
      this.grouperProvisioner.retrieveGrouperProvisioningOutput().getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.ERROR.name());
    }
    // maybe not mess with job status?
//    if (GcGrouperSyncLogState.ERROR.equals(this.getGrouperProvisioner().getGcGrouperSyncLog().getStatus())) {
//      if (StringUtils.isBlank(this.getGrouperProvisioner().getGcGrouperSyncJob().getErrorMessage())) {
//        this.getGrouperProvisioner().getGcGrouperSyncJob().setErrorMessage(errorSummaryString);
//      }
//      if (this.getGrouperProvisioner().getGcGrouperSyncJob().getErrorTimestamp() == null) {
//        this.grouperProvisioner.getGcGrouperSyncJob().setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
//      }
//    }
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
    grouperProvisionerFullSync.setGrouperProvisioningOutput(this.getGrouperProvisioner().retrieveGrouperProvisioningOutput());
    grouperProvisionerFullSync.provision(GrouperProvisioningType.fullProvisionFull);
  }

  public void createMissingGroupsFull() {
      
    //do we have missing groups?
    List<ProvisioningGroup> missingGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroupWrapper> missingGroupWrappers = new ArrayList<ProvisioningGroupWrapper>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      ProvisioningGroup provisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      
      boolean shouldSkip = provisioningGroup == null || !provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject();

      // shouldnt be null at this point
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();

      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups() 
          && !this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()
          && provisioningGroupWrapper.getProvisioningStateGroup().isCreate()
          && gcGrouperSyncGroup != null
          && !gcGrouperSyncGroup.isInTarget()) {
        shouldSkip = false;
      }
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
        shouldSkip = true;
      }

      if (shouldSkip) {
        continue;
      }
      
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

    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertGroups(), false)) {

      // TODO maybe this should be moved somewhere else, where things are validated, if we are not inserting, then why mark as errors?
      if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isSelectGroups()) {
        // entity not there
        for (ProvisioningGroup missingGroup : missingGroups) {
          if (missingGroup.getProvisioningGroupWrapper() != null) {
            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignGroupError(
                missingGroup.getProvisioningGroupWrapper(), GcGrouperSyncErrorCode.DNE, 
                "Group does not exist in target or cannot be found, and not creating groups");
          }
        }
      }
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForCreate", GrouperUtil.length(missingGroups));
    
    // log this
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate, missingGroups);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic().deleteGroupLink(missingGroupWrappers);

    List<ProvisioningGroup> grouperTargetGroupsToInsert = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(missingGroups, false, true);

    if (GrouperUtil.length(grouperTargetGroupsToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoGroupsOnInsert", true);
      return;
    }
    
    translateAndManipulateMembershipsForGroupsEntitiesCreate();
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(grouperTargetGroupsToInsert, true, false, true, false);

    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(grouperTargetGroupsToInsert);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(null);

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroupsToInsert);
    
    // validate that groups have members
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroupsHaveMembers(grouperTargetGroupsToInsert, true);
    
    // validate
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(grouperTargetGroupsToInsert, true, false, true);
    
    if (GrouperUtil.length(grouperTargetGroupsToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoGroupsOnInsert", true);
      return;
    }

    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetGroupsForCreate, grouperTargetGroupsToInsert);

    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
        
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    // remove errors
    Iterator<ProvisioningGroup> iterator = grouperTargetGroupsToInsert.iterator();
    while (iterator.hasNext()) {
      ProvisioningGroup grouperTargetGroupToInsert = iterator.next();
      if (grouperTargetGroupToInsert.getProvisioned() == null || !grouperTargetGroupToInsert.getProvisioned()) {
        iterator.remove();
      }
    }
    
    List<ProvisioningGroup> targetGroups = null;
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      
      int sleepMillis = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getDaoSleepBeforeSelectAfterInsertMillis();
      GrouperUtil.sleep(sleepMillis);
      
      //retrieve so we have a copy
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
      
      targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
      
      if (GrouperUtil.length(grouperTargetGroupsToInsert) != GrouperUtil.length(targetGroups)) {
        // maybe this should be an exception???
        throw new RuntimeException("Searched for " + GrouperUtil.length(grouperTargetGroupsToInsert) + " but retrieved " + GrouperUtil.length(targetGroups) + " maybe a config is off?");
      }
      
      registerRetrievedGroups(grouperTargetGroupsToInsert, targetGroups);
      
      for (ProvisioningGroup groupInsertedAndReadyToBeFetched: grouperTargetGroupsToInsert) {
        if (groupInsertedAndReadyToBeFetched.getProvisioningGroupWrapper() != null) {
          groupInsertedAndReadyToBeFetched.getProvisioningGroupWrapper().getProvisioningStateGroup().setSelectResultProcessed(true);
        }
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetGroups, false, true, false, false);

      // index
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(targetGroups);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(targetGroups);
            
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsCreated, targetGroups);

    }

    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper != null) {
        // this is already created!  :)
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
      }
    }

  }

  //TODO duplicate method in the translator. Try to merge to have only one copy.
  public void translateAndManipulateMembershipsForGroupsEntitiesCreate() {
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isCreateGroupsAndEntitiesBeforeTranslatingMemberships()) {
      
      Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
      
      {
        debugMap.put("state", "translateGrouperMembershipsToTarget");
        {
          List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>(this.getGrouperProvisioner().
              retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(true));
          
          List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(
              grouperProvisioningMemberships, false);
        }    

      }

      {
        debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
        List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true);
        Set<ProvisioningMembership> affectedMemberships = this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(
            grouperTargetMemberships, true, true, false, false);
        if (GrouperUtil.length(affectedMemberships) > 0) {
          this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMemberships, affectedMemberships);
        }

      }

      try {
        debugMap.put("state", "matchingIdGrouperMemberships");
        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true));
      } finally {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
      }

      // index the memberships
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(null);

    }
  }

  /**
   * 
   * @param grouperTargetGroups
   * @param targetProvisioningGroups
   */
  public void registerRetrievedGroups(
      List<ProvisioningGroup> grouperTargetGroups,
      List<ProvisioningGroup> targetProvisioningGroups) {
    
    GrouperProvisioningConfigurationAttribute searchAttribute = null;

    // TODO handle multiple search attributes
    if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) > 0) {
      searchAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes().get(0);
    }
    
    if (searchAttribute == null) {
      throw new RuntimeException("Identify a group search attribute!");
    }

    Map<Object, ProvisioningGroup> searchAttributeValueToGrouperTargetGroup = new HashMap<Object, ProvisioningGroup>();

    // index by search attribute
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroups)) {
      Object searchAttributeValue = grouperTargetGroup.retrieveAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        searchAttributeValueToGrouperTargetGroup.put(searchAttributeValue, grouperTargetGroup);
      }
    }

    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(targetProvisioningGroups)) {
      Object searchAttributeValue = targetProvisioningGroup.retrieveAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        ProvisioningGroup grouperTargetGroup = searchAttributeValueToGrouperTargetGroup.get(searchAttributeValue);
        if (grouperTargetGroup != null) {
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
          if (provisioningGroupWrapper != null) {
            targetProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);
            provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
          }
        }
      }
    }    
  }

  /**
   * 
   * @param grouperTargetEntitiesToInsert
   * @param targetProvisioningEntities
   */
  public void registerRetrievedEntities(
      List<ProvisioningEntity> grouperTargetEntities,
      List<ProvisioningEntity> targetProvisioningEntities) {
    
    GrouperProvisioningConfigurationAttribute searchAttribute = null;
    
    // TODO handle multiple search attributes
    if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) > 0) {
      searchAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes().get(0);
    }
    
    if (searchAttribute == null) {
      throw new RuntimeException("Identify an entity search attribute!");
    }

    Map<Object, ProvisioningEntity> searchAttributeValueToGrouperTargetEntity = new HashMap<Object, ProvisioningEntity>();

    // index by search attribute
    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(grouperTargetEntities)) {
      Object searchAttributeValue = grouperTargetEntity.retrieveAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        searchAttributeValueToGrouperTargetEntity.put(searchAttributeValue, grouperTargetEntity);
      }
    }

    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(targetProvisioningEntities)) {
      Object searchAttributeValue = targetProvisioningEntity.retrieveAttributeValue(searchAttribute);
      if (searchAttributeValue != null) {
        ProvisioningEntity grouperTargetEntity = searchAttributeValueToGrouperTargetEntity.get(searchAttributeValue);
        if (grouperTargetEntity != null) {
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
          if (provisioningEntityWrapper != null) {
            targetProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);
            provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
          }
        }
      }
    }    
  }

  
  public void createMissingEntitiesFull() {
    
    //do we have missing entities?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    List<ProvisioningEntityWrapper> missingEntityWrappers = new ArrayList<ProvisioningEntityWrapper>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      
      ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      boolean shouldSkip = provisioningEntity == null || !provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject();

      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();

      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities() 
          && !this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()
          // && provisioningEntityWrapper.isCreate()
          && gcGrouperSyncMember != null
          && !gcGrouperSyncMember.isInTarget()) {
        shouldSkip = false;
      }
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
        shouldSkip = true;
      }
      
      boolean isUnresolvableSubject = provisioningEntityWrapper.getGrouperProvisioningEntity() != null && provisioningEntityWrapper.getGrouperProvisioningEntity().getSubjectResolutionResolvable() == Boolean.FALSE;
            
      // check if we're inserting unresolvable subjects
      if (isUnresolvableSubject && !this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isUnresolvableSubjectsInsert()) {
        shouldSkip = true;
      }
      
      if (shouldSkip) {
        continue;
      }

      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      ProvisioningEntity targetEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      
      if (targetEntity != null) {
        continue;
      }
      
      missingEntities.add(provisioningEntity);
      missingEntityWrappers.add(provisioningEntityWrapper);    
    }
    if (GrouperUtil.length(missingEntities) == 0) {
      return;
    }
    // first lets see if we should even be doing this
    if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertEntities(), false)) {

      // TODO maybe this should be moved somewhere else, where things are validated, if we are not inserting, then why mark as errors?
      if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isSelectEntities()) {
        // entity not there
        for (ProvisioningEntity missingEntity : missingEntities) {
          if (missingEntity.getProvisioningEntityWrapper() != null) {
            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignEntityError(
                missingEntity.getProvisioningEntityWrapper(), GcGrouperSyncErrorCode.DNE, 
                "Entity does not exist in target or cannot be found, and not creating entities");
          }
        }
      }
      return;
    }
    
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingEntitiesForCreate", GrouperUtil.length(missingEntities));
    
    // log this
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate, missingEntities);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic().deleteEntityLink(missingEntityWrappers);
    
    // translate
    List<ProvisioningEntity> grouperTargetEntitiesToInsert = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(missingEntities, false, true);
    
    if (GrouperUtil.length(grouperTargetEntitiesToInsert) == 0) {
      this.grouperProvisioner.getDebugMap().put("groupTranslationEndedInNoEntitiesOnInsert", true);
      return;
    }
    
    
    translateAndManipulateMembershipsForGroupsEntitiesCreate();
    
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(grouperTargetEntitiesToInsert, true, false, true, false);

    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(grouperTargetEntitiesToInsert);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(null);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntitiesToInsert);
    // validate
    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(grouperTargetEntitiesToInsert, true, null, true);
    // add object change entries
    this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
        
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGrouperTargetEntitiesForCreate, grouperTargetEntitiesToInsert);
    //lets create these
    RuntimeException runtimeException = null;
    try {
      this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      try {
        this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
        
      } catch (RuntimeException e) {
        GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
      }
    }
    
    // remove errors
    Iterator<ProvisioningEntity> iterator = grouperTargetEntitiesToInsert.iterator();
    while (iterator.hasNext()) {
      ProvisioningEntity grouperTargetEntityToInsert = iterator.next();
      if (grouperTargetEntityToInsert.getProvisioned() == null || !grouperTargetEntityToInsert.getProvisioned()) {
        iterator.remove();
      }
    }

    List<ProvisioningEntity> targetEntities = null;
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      
      int sleepMillis = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getDaoSleepBeforeSelectAfterInsertMillis();
      GrouperUtil.sleep(sleepMillis);
      
     //retrieve so we have a copy
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, false));
      
      targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
      
      if (GrouperUtil.length(grouperTargetEntitiesToInsert) != GrouperUtil.length(targetEntities)) {
        // maybe this should be an exception???
        throw new RuntimeException("Searched for " + GrouperUtil.length(grouperTargetEntitiesToInsert) + " but retrieved " + GrouperUtil.length(targetEntities) + " maybe a config is off?");
      }

      registerRetrievedEntities(grouperTargetEntitiesToInsert, targetEntities);
      
      for (ProvisioningEntity entityInsertedAndReadyToBeFetched: grouperTargetEntitiesToInsert) {
        if (entityInsertedAndReadyToBeFetched.getProvisioningEntityWrapper() != null) {
          entityInsertedAndReadyToBeFetched.getProvisioningEntityWrapper().getProvisioningStateEntity().setSelectResultProcessed(true);
        }
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(targetEntities, false, true, false, false);

      // index
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(targetEntities);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(targetEntities);
      
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesCreated, targetEntities);

    }
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(grouperTargetEntitiesToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper != null) {
        // this is already created!  :)
        provisioningEntityWrapper.getProvisioningStateEntity().setCreate(true);
      }
    }
    
  }

  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  public void retrieveAllData() {
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    long start = System.currentTimeMillis();

    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse
            = GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
              .retrieveAllData(new TargetDaoRetrieveAllDataRequest());
          
          if (GrouperProvisioningLogic.this.getGrouperProvisioner().getProvisioningStateGlobal().isSelectResultProcessedEntities() 
              || GrouperProvisioningLogic.this.getGrouperProvisioner().getProvisioningStateGlobal().isSelectResultProcessedGroups()
              || GrouperProvisioningLogic.this.getGrouperProvisioner().getProvisioningStateGlobal().isSelectResultProcessedMemberships()) {
            
            GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetData);
          }

        } catch (RuntimeException re) {
          String logMessage = "error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId();
          LOG.error(logMessage, re);
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningObjectLog().getObjectLog().append(new Timestamp(System.currentTimeMillis())).append(": ERRROR: ").append(logMessage).append("\n\n");
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    GrouperProvisioningLists grouperProvisioningList = retrieveGrouperDataFull();
    
    enhanceEntityAttributesWithSqlResolver(true);
    
    enhanceEntityAttributesWithLdapResolver();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
    
    GrouperProvisioningLists extraTargetData = retrieveExtraTargetData(grouperProvisioningList);
    if (extraTargetData != null) {
      processTargetDataEntities(extraTargetData.getProvisioningEntities());
      processTargetDataGroups(extraTargetData.getProvisioningGroups());
      processTargetDataMemberships(extraTargetData.getProvisioningMemberships());
    }
    
    long retrieveDataPass1 = System.currentTimeMillis()-start;
    this.getGrouperProvisioner().getDebugMap().put("retrieveDataPass1_millis", retrieveDataPass1);

    this.getGrouperProvisioner().getDebugMap().put("grouperGroupsRetrieved", GrouperUtil.length(grouperProvisioningList.getProvisioningGroups()));
    this.getGrouperProvisioner().getDebugMap().put("grouperEntitiesRetrieved", GrouperUtil.length(grouperProvisioningList.getProvisioningEntities()));
    this.getGrouperProvisioner().getDebugMap().put("grouperMembershipsRetrieved", GrouperUtil.length(grouperProvisioningList.getProvisioningMemberships()));
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveDataFromGrouper);
    
  }
  
  public void enhanceEntityAttributesWithSqlResolver(boolean isFullSync) {
    
    GrouperProvisioningConfiguration provisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (!provisioningConfiguration.isResolveAttributesWithSql()) {
      return;
    }
    
    String dbConnectionName = null;
    String grouperAttributeThatMatchesRow = null;
    String subjectSearchMatchingColumn = null;
    String subjectSourceIdColumn = null;
    String tableOrViewName = null;
    String expression = null;
    String commaSeparatedColumns = null;
    String lastUpdatedColumn = null;
    String lastUpdatedColumnType = null;
    
    String globalSqlResolver = provisioningConfiguration.getGlobalSqlResolver();
    
    if (StringUtils.isNotBlank(globalSqlResolver)) {
      
      boolean isEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("entityAttributeResolver."+globalSqlResolver+".enabled", true);
      
      if (!isEnabled) {
        return;
      }
      
      dbConnectionName = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".sqlConfigId");
      
      grouperAttributeThatMatchesRow = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".grouperAttributeThatMatchesRow");

      subjectSearchMatchingColumn = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".subjectSearchMatchingColumn");

      tableOrViewName = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".tableOrViewName");

      commaSeparatedColumns = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalSqlResolver+".columnNames");

      subjectSourceIdColumn = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".subjectSourceIdColumn");

      lastUpdatedColumn = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".lastUpdatedColumn");
      
      lastUpdatedColumnType = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalSqlResolver+".lastUpdatedType");
        
    } else {
      
      dbConnectionName = provisioningConfiguration.getEntityAttributesSqlExternalSystem();
      
      grouperAttributeThatMatchesRow = provisioningConfiguration.getEntityAttributesSqlMappingEntityAttribute();

      subjectSearchMatchingColumn = provisioningConfiguration.getEntityAttributesSubjectSearchMatchingColumn();
      
      tableOrViewName = provisioningConfiguration.getEntityAttributesTableViewName();
      
      commaSeparatedColumns = provisioningConfiguration.getEntityAttributesColumnNames();
      
      subjectSourceIdColumn = provisioningConfiguration.getEntityAttributesSubjectSourceIdColumn();
      
      expression = provisioningConfiguration.getEntityAttributesSqlMappingExpression();
      
      lastUpdatedColumn = provisioningConfiguration.getEntityAttributesLastUpdatedColumn();

      lastUpdatedColumnType = provisioningConfiguration.getEntityAttributesLastUpdatedType();
      
    }
    
    Set<String> columnsWhichAreAttributes = GrouperUtil.splitTrimToSet(commaSeparatedColumns, ",");
    
    Set<String> columnNamesToFetch = GrouperUtil.splitTrimToSet(commaSeparatedColumns, ",");
    
    if (StringUtils.isNotBlank(lastUpdatedColumn) && !columnNamesToFetch.contains(lastUpdatedColumn.trim())) {
      columnNamesToFetch.add(lastUpdatedColumn.trim());
    }
    
    if (!columnNamesToFetch.contains(subjectSearchMatchingColumn.trim())) {
      columnNamesToFetch.add(subjectSearchMatchingColumn.trim());
    }
    
    String commaSeparatedColNames = GrouperUtil.setToString(columnNamesToFetch);
      
    boolean selectAllSqlOnFull = provisioningConfiguration.isSelectAllSqlOnFull();
    
    List<ProvisioningEntity> provisioningEntities = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
        
    StringBuilder sqlInitial = new StringBuilder("select ");
    sqlInitial.append(commaSeparatedColNames);
    sqlInitial.append(" from ");
    sqlInitial.append(tableOrViewName);
    
    List<Object[]> attributesFromTable = new ArrayList<Object[]>();
    
    if ( (isFullSync && !selectAllSqlOnFull) || !isFullSync) {
      if (provisioningEntities.size() == 0) {
        return;
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(provisioningEntities.size(), 900);
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      
      for (int i = 0; i < numberOfBatches; i++) {
        
        List<ProvisioningEntity> currentBatchProvisioningEntities = GrouperUtil.batchList(provisioningEntities, 900, i);
        StringBuilder sql = new StringBuilder(sqlInitial);
        
        sql.append(" where "+ subjectSearchMatchingColumn + " in ( ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbConnectionName);
        
        for (int j=0; j<currentBatchProvisioningEntities.size();j++) {
          ProvisioningEntity provisioningEntity = currentBatchProvisioningEntities.get(j);
          
          String subjectMatchingIdentifier = null;
          if (StringUtils.isNotBlank(expression)) {
            elVariableMap.clear();
            elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
            Object object = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().runScript(expression, elVariableMap);
            subjectMatchingIdentifier = GrouperUtil.stringValue(object);
          } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectId")) {
              subjectMatchingIdentifier = provisioningEntity.getSubjectId();
          } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectIdentifier0")) {
            subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
          } else {
              throw new RuntimeException("invalid grouperAttributeThatMatchesRow: "+grouperAttributeThatMatchesRow + " expected 'subjectId' or 'subjectIdentifier0'");
          }
          
          gcDbAccess.addBindVar(subjectMatchingIdentifier);
          if (j>0) {
            sql.append(",");
          }
          sql.append("?");
        }
        sql.append(" ) ");
        attributesFromTable.addAll(gcDbAccess.sql(sql.toString()).selectList(Object[].class));
      
      }
    } else {
      attributesFromTable.addAll(new GcDbAccess().connectionName(dbConnectionName).sql(sqlInitial.toString()).selectList(Object[].class));
    }

    String[] colNamesFromAttributesTable = GrouperUtil.splitTrim(commaSeparatedColNames, ",");
    
    Map<MultiKey, Object[]> subjectSearchMatchingColumnToAttributes = new HashMap<MultiKey, Object[]>();
    
    int indexOfSubjectSearchMatchingColumn = GrouperUtil.indexOf(colNamesFromAttributesTable, subjectSearchMatchingColumn);
    
    for (Object[] oneRowOfAttributes: attributesFromTable) {
      Object subjectSearchMatchingValue = oneRowOfAttributes[indexOfSubjectSearchMatchingColumn];
      if (subjectSearchMatchingValue != null) {
        
        String subjectSearchMatchingValueString = GrouperUtil.stringValue(subjectSearchMatchingValue);
        
        MultiKey identifier = null;
        if (StringUtils.isNotBlank(subjectSourceIdColumn)) {
          identifier = new MultiKey(subjectSearchMatchingValueString, subjectSourceIdColumn);
        } else {
          identifier = new MultiKey(new String[] {subjectSearchMatchingValueString});
        }
        
        subjectSearchMatchingColumnToAttributes.put(identifier, oneRowOfAttributes);
      }
    }
    
    /**
     * subjectSearchMatchingColumnToAttributes looks like
     * test.subject.0 -> [school0, description0,....]
     */
    
    Map<String, Object> elVariableMap = new HashMap<String, Object>();
    
    for (ProvisioningEntity provisioningEntity: provisioningEntities) {
      
      String subjectMatchingIdentifier = null;
      
      if (StringUtils.isNotBlank(expression)) {
        elVariableMap.clear();
        elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
        Object object = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().runScript(expression, elVariableMap);
        subjectMatchingIdentifier = GrouperUtil.stringValue(object);
      } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectId")) {
          subjectMatchingIdentifier = provisioningEntity.getSubjectId();
      } else if (StringUtils.equals(grouperAttributeThatMatchesRow, "subjectIdentifier0")) {
        subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
      } else {
          throw new RuntimeException("invalid grouperAttributeThatMatchesRow: "+grouperAttributeThatMatchesRow + " expected 'subjectId' or 'subjectIdentifier0'");
      }
      
      MultiKey identifier = null;
      if (StringUtils.isNotBlank(subjectSourceIdColumn)) {
        String subjectSourceIdFromProvisioningEntity = provisioningEntity.retrieveAttributeValueString("subjectSourceId");
        identifier = new MultiKey(subjectMatchingIdentifier, subjectSourceIdFromProvisioningEntity);
      } else {
        identifier = new MultiKey(new String[] {subjectMatchingIdentifier});
      }
        
      Object[] attributeValues = subjectSearchMatchingColumnToAttributes.get(identifier);
      if (attributeValues != null) {
        int i = 0;
        for (String attributeName: colNamesFromAttributesTable) {
          
          if (columnsWhichAreAttributes.contains(attributeName)) {
            
            provisioningEntity.assignAttributeValue("entityAttributeResolverSql__"+attributeName.toLowerCase(), attributeValues[i]);
          }

          i++;
         
        }
      }
      
    }
        
  }
  
  public void enhanceEntityAttributesWithLdapResolver() {
    
    GrouperProvisioningConfiguration provisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (!provisioningConfiguration.isResolveAttributesWithLdap()) {
      return;
    }
    
    List<ProvisioningEntity> provisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
    
    String globalLdapResolver = provisioningConfiguration.getGlobalLdapResolver();
    
    String ldapConfigId = null;
    String baseDn = null;
    String searchScope = null;
    String ldapAttributes = null;
    String subjectSearchMatchingAttribute = null;
    String subjectSourceId = null;
    String grouperAttributeThatMatchesRecord = null;
    String filterPart = null;
    String lastUpdatedAttribute = null;
    String multiValuedLdapAttributes = null;
    String expression = null;
    String lastUpdatedAttributeFormat = null;
    boolean filterAllLdapOnFull = true;
    
    if (StringUtils.isNotBlank(globalLdapResolver)) {
      
      boolean isEnabled = GrouperConfig.retrieveConfig().propertyValueBoolean("entityAttributeResolver."+globalLdapResolver+".enabled", true);
      
      if (!isEnabled) {
        return;
      }
      
      ldapConfigId = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".ldapConfigId");
      baseDn = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".baseDn");
      subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".subjectSourceId");
      searchScope = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".searchScope");
      ldapAttributes = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".ldapAttributes");
      subjectSearchMatchingAttribute = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".subjectSearchMatchingAttribute");
      grouperAttributeThatMatchesRecord = GrouperConfig.retrieveConfig().propertyValueStringRequired("entityAttributeResolver."+globalLdapResolver+".grouperAttributeThatMatchesRecord");
      filterPart = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".filterPart");
      lastUpdatedAttribute = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".lastUpdatedAttribute");
      lastUpdatedAttributeFormat = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".ldapLastUpdatedFormat");
      multiValuedLdapAttributes = GrouperConfig.retrieveConfig().propertyValueString("entityAttributeResolver."+globalLdapResolver+".multiValuedLdapAttributes");
      
    } else {
      
      ldapConfigId = provisioningConfiguration.getEntityAttributesLdapExternalSystem();
      baseDn = provisioningConfiguration.getEntityAttributesLdapBaseDn();
      subjectSourceId = provisioningConfiguration.getEntityAttributesLdapSubjectSource();
      searchScope = provisioningConfiguration.getEntityAttributesLdapSearchScope();
      ldapAttributes = provisioningConfiguration.getEntityAttributesLdapAttributes();
      subjectSearchMatchingAttribute = provisioningConfiguration.getEntityAttributesLdapMatchingSearchAttribute();
      grouperAttributeThatMatchesRecord = provisioningConfiguration.getEntityAttributesLdapMappingEntityAttribute();
      filterPart = provisioningConfiguration.getEntityAttributesLdapFilterPart();
      lastUpdatedAttribute = provisioningConfiguration.getEntityAttributesLdapLastUpdatedAttribute();
      lastUpdatedAttributeFormat = provisioningConfiguration.getEntityAttributesLdapLastUpdatedAttributeFormat();
      expression = provisioningConfiguration.getEntityAttributesLdapMatchingExpression();
    }
    
    List<LdapEntry> ldapEntries = new ArrayList<LdapEntry>();
    
    filterAllLdapOnFull = provisioningConfiguration.isFilterAllLDAPOnFull();
    
    Set<String> ldapAttributesSet = GrouperUtil.splitTrimToSet(ldapAttributes, ",");
    
    Set<String> multiValuedAttributesSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(multiValuedLdapAttributes, ","));
    
    if (StringUtils.isNotBlank(multiValuedLdapAttributes)) {
      ldapAttributesSet.addAll(multiValuedAttributesSet);
    }
    
    if (StringUtils.isNotBlank(subjectSearchMatchingAttribute)) {
      ldapAttributesSet.add(subjectSearchMatchingAttribute);
    }
    
    String[] ldapAttributesArray = GrouperUtil.toArray(ldapAttributesSet, String.class);
    
    LdapSearchScope ldapSearchScope = LdapSearchScope.valueOfIgnoreCase(searchScope, true);
    
    if (!filterAllLdapOnFull) {
      
      if (provisioningEntities.size() == 0) {
        return;
      }
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(provisioningEntities.size(), 900);
      
      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      
      for (int i = 0; i < numberOfBatches; i++) {
        
        List<ProvisioningEntity> currentBatchProvisioningEntities = GrouperUtil.batchList(provisioningEntities, 900, i);
        
        String filter = null;
        if (StringUtils.isNotBlank(filterPart)) {
          filter = "(&";
          filterPart = filterPart.trim();
          if (filterPart.startsWith("(")) {
            filter += filterPart;
          } else {
            filter += "(" + filterPart + ")";
          }
        } else {
          filter = "(|";
        }
        
        
        for (int j=0; j<currentBatchProvisioningEntities.size(); j++) {
          
          ProvisioningEntity provisioningEntity = currentBatchProvisioningEntities.get(j);
          
          String subjectMatchingIdentifier = null;
          if (StringUtils.isNotBlank(expression)) {
            elVariableMap.clear();
            elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
            Object object = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().runScript(expression, elVariableMap);
            subjectMatchingIdentifier = GrouperUtil.stringValue(object);
          } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectId")) {
              subjectMatchingIdentifier = provisioningEntity.getSubjectId();
          } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectIdentifier0")) {
            subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
          } else {
              throw new RuntimeException("invalid grouperAttributeThatMatchesRecord: "+grouperAttributeThatMatchesRecord + " expected 'subjectId' or 'subjectIdentifier0'");
          }
          
          filter += "("+subjectSearchMatchingAttribute+"="+subjectMatchingIdentifier+")";  
        }
        
        filter += ")";
        ldapEntries.addAll(LdapSessionUtils.ldapSession().list(ldapConfigId, baseDn, ldapSearchScope, filter, ldapAttributesArray, null));
      
      }
    } else {
      
      String filter = null;
      if (StringUtils.isNotBlank(filterPart)) {
        filter = "(&";
        filterPart = filterPart.trim();
        if (filterPart.startsWith("(")) {
          filter += filterPart;
        } else {
          filter += "(" + filterPart + ")";
        }
        filter += "("+subjectSearchMatchingAttribute+"=*))"; 
        
      } else {
        filter = "("+subjectSearchMatchingAttribute+"=*)"; 
      }
      
      ldapEntries.addAll(LdapSessionUtils.ldapSession().list(ldapConfigId, baseDn, ldapSearchScope, filter, ldapAttributesArray, null));
      
    }
    
    Map<MultiKey, LdapEntry> identifierToLdapEntry = new HashMap<MultiKey, LdapEntry>();
    
    for (LdapEntry ldapEntry: GrouperUtil.nonNull(ldapEntries)) {
      
      LdapAttribute attribute = ldapEntry.getAttribute(subjectSearchMatchingAttribute);
      if (attribute != null) {
        
        Collection<String> stringValues = attribute.getStringValues();
        if (GrouperUtil.length(stringValues) == 1) {
          
          MultiKey identifier = null;
          if (StringUtils.isNotBlank(subjectSourceId)) {
            identifier = new MultiKey(stringValues.iterator().next(), subjectSourceId);
          } else {
            identifier = new MultiKey(new String[] {stringValues.iterator().next()});
          }
          identifierToLdapEntry.put(identifier, ldapEntry);
        }
      }
    }
    
    Map<String, Object> elVariableMap = new HashMap<String, Object>();
    
    for (ProvisioningEntity provisioningEntity: provisioningEntities) {
      
      String subjectMatchingIdentifier = null;
      if (StringUtils.isNotBlank(expression)) {
        elVariableMap.clear();
        elVariableMap.put("grouperProvisioningEntity", provisioningEntity);
        Object object = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().runScript(expression, elVariableMap);
        subjectMatchingIdentifier = GrouperUtil.stringValue(object);
      } else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectId")) {
          subjectMatchingIdentifier = provisioningEntity.getSubjectId();
      } 
      else if (StringUtils.equals(grouperAttributeThatMatchesRecord, "subjectIdentifier0")) {
        subjectMatchingIdentifier = (String)provisioningEntity.retrieveAttributeValueString("subjectIdentifier0");
      } else {
          throw new RuntimeException("invalid grouperAttributeThatMatchesRecord: "+grouperAttributeThatMatchesRecord + " expected 'subjectId' or 'subjectIdentifier0'");
      }
        
      MultiKey identifier = null;
      if (StringUtils.isNotBlank(subjectSourceId)) {
        String subjectSourceIdFromProvisioningEntity = provisioningEntity.retrieveAttributeValueString("subjectSourceId");
        identifier = new MultiKey(subjectMatchingIdentifier, subjectSourceIdFromProvisioningEntity);
      } else {
        identifier = new MultiKey(new String[] {subjectMatchingIdentifier});
      }
      
      LdapEntry ldapEntry = identifierToLdapEntry.get(identifier);
      if (ldapEntry != null) {
        
        for (String ldapAttributeName: ldapAttributesArray) {
          
          if (StringUtils.equals(ldapAttributeName, "lastUpdatedAttribute")) {
            continue;
          }
          
          LdapAttribute attribute = ldapEntry.getAttribute(ldapAttributeName);
          if (attribute != null) {
            
            if (multiValuedAttributesSet.contains(attribute.getName())) {
              
              for ( String attributeValue: GrouperUtil.nonNull(attribute.getStringValues())) {
                provisioningEntity.addAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), attributeValue);
              }
              
            } else {
              
              if (GrouperUtil.length(attribute.getStringValues()) == 0) {
                continue;
              }
              
              if (GrouperUtil.length(attribute.getStringValues()) == 1) {
                provisioningEntity.assignAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), attribute.getStringValues().iterator().next());
              } else {
                
                String concatenatedAttributeValues = GrouperUtil.join(attribute.getStringValues().iterator(), ",");
                provisioningEntity.assignAttributeValue("entityAttributeResolverLdap__"+attribute.getName().toLowerCase(), concatenatedAttributeValues);
                
              }
            }
          }
        }
      }
    }
  }

  /** 
   * override this method to do some logic after all target data is retrieved (e.g. if there are DN overrides)
   * @param grouperProvisioningLists grouper data to get extra target data from
   * @return newly selected data
   */
  public GrouperProvisioningLists retrieveExtraTargetData(GrouperProvisioningLists grouperProvisioningLists) {
    return null;
  }

  /**
   * take target data and add wrapper and add to data store.
   * put them in wrappers irrespective of matching or whatever
   * @param targetProvisioningLists 
   */
  public void processTargetDataGroups(List<ProvisioningGroup> targetProvisioningGroups) {
    if (GrouperUtil.length(targetProvisioningGroups) == 0) {
      return;
    }
    
    // add wrappers for all groups
    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(targetProvisioningGroups)) {
      if (targetProvisioningGroup.getProvisioningGroupWrapper() == null) {
        ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
        provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
      }
    }

    // Go through the full logic and see if any other processing is done on the target entities
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetProvisioningGroups, true, true, false, false);
    
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(targetProvisioningGroups);
  }

  /**
   * take target data and add wrapper and add to data store.
   * put them in wrappers irrespective of matching or whatever
   * @param targetProvisioningEntities 
   */
  public void processTargetDataEntities(List<ProvisioningEntity> targetProvisioningEntities) {
    if (GrouperUtil.length(targetProvisioningEntities) == 0) {
      return;
    }
    
    // never null
    Map<ProvisioningEntity, Object> targetEntityToTargetNativeEntity = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetEntityToTargetNativeEntity();
    
    // add wrappers for all entities
    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(targetProvisioningEntities)) {
      if (targetProvisioningEntity.getProvisioningEntityWrapper() == null) {
        // note: should any of this happen if already registered?  register again?
        ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
  
        provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
        provisioningEntityWrapper.setTargetNativeEntity(targetEntityToTargetNativeEntity.get(targetProvisioningEntity));

      }
    }

    // Go through the full logic and see if any other processing is done on the target entities
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(targetProvisioningEntities, true, true, false, false);

    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(targetProvisioningEntities);
   

  }


  /**
   * take target data and add wrapper and add to data store.
   * put them in wrappers irrespective of matching or whatever
   * @param targetProvisioningMemberships 
   */
  public void processTargetDataMemberships(List<ProvisioningMembership> targetProvisioningMemberships) {
    if (GrouperUtil.length(targetProvisioningMemberships) == 0) {
      return;
    }
    // add wrappers for all memberships
    for (ProvisioningMembership targetProvisioningMembership : GrouperUtil.nonNull(targetProvisioningMemberships)) {
      if (targetProvisioningMembership.getProvisioningMembershipWrapper() == null) {
        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setTargetProvisioningMembership(targetProvisioningMembership);
      }
      
    }

    // Go through the full logic and see if any other processing is done on the target memberships
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesMemberships(targetProvisioningMemberships, true, true, false, false);

    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(targetProvisioningMemberships);
   
  }

  public GrouperProvisioningLists retrieveGrouperDataFull() {
    
    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataSyncGrouper
    GrouperProvisioningLists grouperProvisioningLists = this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataFull();
    
    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers(grouperProvisioningLists);
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    calculateProvisioningDataToDelete(); 

    GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
    gcGrouperSync.setGroupCount(GrouperUtil.length(grouperProvisioningLists.getProvisioningGroups()));
    gcGrouperSync.setUserCount(GrouperUtil.length(grouperProvisioningLists.getProvisioningEntities()));
    gcGrouperSync.setRecordsCount(GrouperUtil.length(grouperProvisioningLists.getProvisioningMemberships())
        );
   return grouperProvisioningLists; 
  }


  public void retrieveGrouperDataIncrementalGroupsEntities() {

    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataGrouper
    GrouperProvisioningLists grouperProvisioningLists = this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncrementalGroupsEntities();

    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers(grouperProvisioningLists);
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
//    // incrementals need to clone and setup sync objects as deletes
//    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().setupIncrementalClonesOfGroupProvisioningObjects();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    
  }

  public void retrieveGrouperDataIncrementalMemberships() {

    // get all grouper data for the provisioner
    // and put in GrouperProvisioningDataGrouper
    GrouperProvisioningLists grouperProvisioningLists = this.grouperProvisioner.retrieveGrouperDao().retrieveGrouperDataIncrementalMemberships();

    // put wrappers on the grouper objects and put in the grouper uuid maps in data object
    // put these wrapper in the GrouperProvisioningData and GrouperProvisioningDataIndex
    this.grouperProvisioner.retrieveGrouperDao().processWrappers(grouperProvisioningLists);
    
    // point the membership pointers to groups and entities to what they should point to
    // and fix data problems (for instance race conditions as data was retrieved)
    this.grouperProvisioner.retrieveGrouperDao().fixGrouperProvisioningMembershipReferences();
    
//    // incrementals need to clone and setup sync objects as deletes
//    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().setupIncrementalClonesOfGroupProvisioningObjects();

    // add / update / delete sync objects based on grouper data
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    this.calculateProvisioningGroupsToDelete();
    this.calculateProvisioningEntitiesToDelete();
    this.calculateProvisioningMembershipsToDelete();
    
  }

  protected void countInsertsUpdatesDeletes() {
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships());

    {
      //TODO: Is this correct?
      //countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.replace, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces().getProvisioningMemberships());
      List<ProvisioningMembership> targetMemberships = new ArrayList<ProvisioningMembership>();
      
      Collection<List<ProvisioningMembership>> targetMembershipsLists = this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces().getProvisioningMemberships().values();
      for (List<ProvisioningMembership> provisioningMemberships: targetMembershipsLists) {
        targetMemberships.addAll(provisioningMemberships);
      }
      this.grouperProvisioner.retrieveGrouperProvisioningOutput().addReplace(GrouperUtil.length(targetMemberships));  
        
    }
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.insert, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().getProvisioningEntities());
    
    
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.update, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities());
    countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction.delete, this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships());
    
    GrouperProvisioningReplacesObjects targetObjectReplaces = this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces();
    Set<ProvisioningMembershipWrapper> provisioningMembershipWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers();
    if (targetObjectReplaces != null) {    
      for (ProvisioningGroup provisioningGroup : targetObjectReplaces.getProvisioningMemberships().keySet()) {
        
        Timestamp inTargetStart = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup().getInTargetStart();
        Timestamp inTargetEnd = provisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup().getInTargetEnd();
        if (inTargetStart != null && inTargetStart.getTime() >= this.getGrouperProvisioner().getMillisWhenSyncStarted()) {
          this.grouperProvisioner.retrieveGrouperProvisioningOutput().addInsert(1);  
        }
        if (inTargetEnd != null && inTargetEnd.getTime() >= this.getGrouperProvisioner().getMillisWhenSyncStarted()) {
          this.grouperProvisioner.retrieveGrouperProvisioningOutput().addDelete(1);
        }
        List<ProvisioningMembership> memberships = targetObjectReplaces.getProvisioningMemberships().get(provisioningGroup);
        for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(memberships)) {
          inTargetStart = provisioningMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTargetStart();
          if (inTargetStart != null && inTargetStart.getTime() >= this.getGrouperProvisioner().getMillisWhenSyncStarted()) {
            this.grouperProvisioner.retrieveGrouperProvisioningOutput().addInsert(1);  
          }
        }
        
        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(provisioningMembershipWrappers)) {
          inTargetEnd = provisioningMembershipWrapper.getGcGrouperSyncMembership().getInTargetEnd();
          if (inTargetEnd != null && inTargetEnd.getTime() >= this.getGrouperProvisioner().getMillisWhenSyncStarted()) {

            this.grouperProvisioner.retrieveGrouperProvisioningOutput().addDelete(1);
          }
        }
        
        
      }
    }
  }

  /**
   * make sure we dont doublecount actions
   */
  private Set<MultiKey> alreadyCounted = new HashSet<MultiKey>();

  protected void countAttributesFieldsInsertsUpdatesDeletes(ProvisioningObjectChangeAction provisioningObjectChangeAction, List<? extends ProvisioningUpdatable> provisioningUpdatables) {
    // maybe not count fields?
    if (provisioningUpdatables == null) {
      return;
    }

    String membershipAttribute = GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    boolean groupAttributes = GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
        == GrouperProvisioningBehaviorMembershipType.groupAttributes;
    boolean entityAttributes = GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
        == GrouperProvisioningBehaviorMembershipType.entityAttributes;

    
    for (ProvisioningUpdatable provisioningUpdatable : GrouperUtil.nonNull(provisioningUpdatables)) {

      MultiKey multiKey = new MultiKey(provisioningObjectChangeAction, provisioningUpdatable.provisioningUpdatableTypeShort(), provisioningUpdatable);
      
      // if this is an update, dont doublecount the inserts if we are only updating the membership objects
      MultiKey insertMultiKey = provisioningObjectChangeAction == ProvisioningObjectChangeAction.update ? new MultiKey(ProvisioningObjectChangeAction.insert, provisioningUpdatable.provisioningUpdatableTypeShort(), provisioningUpdatable) : null;
      
      if (!alreadyCounted.contains(multiKey) && provisioningUpdatable.getProvisioned() != null && provisioningUpdatable.getProvisioned()) {
        switch(provisioningObjectChangeAction) {
          case insert:
            this.grouperProvisioner.retrieveGrouperProvisioningOutput().addInsert(1);  
            alreadyCounted.add(multiKey);
            break;
          case update:
            // its an update if we are doing something other than membership stuff and it is also an insert 
            if (!alreadyCounted.contains(insertMultiKey) || StringUtils.isBlank(membershipAttribute) || (!groupAttributes && !entityAttributes)
              || (groupAttributes && (!(provisioningUpdatable instanceof ProvisioningGroup)))
                  || (entityAttributes && (!(provisioningUpdatable instanceof ProvisioningEntity)))) {
              this.grouperProvisioner.retrieveGrouperProvisioningOutput().addUpdate(1);  
              alreadyCounted.add(multiKey);
            } else {
                  
              for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningUpdatable.getInternal_objectChanges())) {
                if (!StringUtils.equals(provisioningObjectChange.getAttributeName(), membershipAttribute)
                    && provisioningObjectChange.getProvisioned() != null && provisioningObjectChange.getProvisioned()) {
                  this.grouperProvisioner.retrieveGrouperProvisioningOutput().addUpdate(1);  
                  alreadyCounted.add(multiKey);
                  break;
                }
              }
            }
            break;
          case replace:
            this.grouperProvisioner.retrieveGrouperProvisioningOutput().addReplace(1);  
            alreadyCounted.add(multiKey);
            break;
          case delete:
            this.grouperProvisioner.retrieveGrouperProvisioningOutput().addDelete(1);
            alreadyCounted.add(multiKey);
            break;
        }
        
      }
      
      // go through the membership attributes and count as memberships
      if (!StringUtils.isBlank(membershipAttribute) && (groupAttributes || entityAttributes)) {
        if ((groupAttributes && provisioningUpdatable instanceof ProvisioningGroup)
            || entityAttributes && provisioningUpdatable instanceof ProvisioningEntity) {

          for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(provisioningUpdatable.getInternal_objectChanges())) {
            
            if (!StringUtils.equals(provisioningObjectChange.getAttributeName(), membershipAttribute)) {
              
            }
            
            if (StringUtils.equals(provisioningObjectChange.getAttributeName(), membershipAttribute)
                && provisioningObjectChange.getProvisioned() != null && provisioningObjectChange.getProvisioned()) {

              ProvisioningObjectChangeAction attributeProvisioningObjectChangeAction = provisioningObjectChange.getProvisioningObjectChangeAction();
              Object value = null;
              switch (attributeProvisioningObjectChangeAction) {
              case insert:
              case update:
                value = provisioningObjectChange.getNewValue();
                break;
              case delete:
                value = provisioningObjectChange.getOldValue();
                break;
              }

              multiKey = new MultiKey(attributeProvisioningObjectChangeAction, provisioningUpdatable.provisioningUpdatableTypeShort(), provisioningUpdatable, value);

              if (alreadyCounted.contains(multiKey)) {
                continue;
              }
              alreadyCounted.add(multiKey);

              switch (attributeProvisioningObjectChangeAction) {
              case insert:
                this.grouperProvisioner.retrieveGrouperProvisioningOutput().addInsert(1);  
                break;
              case update:
                this.grouperProvisioner.retrieveGrouperProvisioningOutput().addUpdate(1);  
                break;
              case delete:
                this.grouperProvisioner.retrieveGrouperProvisioningOutput().addDelete(1);  
                break;

              }
            }
          }
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
    
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    Map<String, ProvisioningEntityWrapper> memberIdToEntityWrapperToDelete = new HashMap<>();
  
    // loop through sync groups
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
  
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      // if a entity has been deleted in grouper_members table but copy still exists in grouper_sync_member
      // we are sending the copy over to the target so that target can also delete
      if (grouperProvisioningEntity == null && gcGrouperSyncMember != null) {
        
        memberIdToEntityWrapperToDelete.put(gcGrouperSyncMember.getMemberId(), provisioningEntityWrapper);
        provisioningEntitiesToDelete++;
        
      }
    }
    
    if (memberIdToEntityWrapperToDelete.size() == 0) {
      return;
    }
    
    List<ProvisioningEntity> membersNonProvisionable = this.getGrouperProvisioner().retrieveGrouperDao().retrieveMembersNonProvisionable(memberIdToEntityWrapperToDelete.keySet());
    
    Map<String, ProvisioningEntity> memberIdToProvisioningEntityToDelete = new HashMap<>();
    
    for (ProvisioningEntity provisioningEntityNotProvisionable: GrouperUtil.nonNull(membersNonProvisionable)) {
      memberIdToProvisioningEntityToDelete.put(provisioningEntityNotProvisionable.getId(), provisioningEntityNotProvisionable);
    }
    
    for (String memberIdToDelete: memberIdToEntityWrapperToDelete.keySet()) {
      
      ProvisioningEntity grouperProvisioningEntity = memberIdToProvisioningEntityToDelete.get(memberIdToDelete);
      
      ProvisioningEntityWrapper provisioningEntityWrapper = memberIdToEntityWrapperToDelete.get(memberIdToDelete);
      
      if (grouperProvisioningEntity == null) {
        
        GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
        
        grouperProvisioningEntity = new ProvisioningEntity();
        grouperProvisioningEntity.setId(gcGrouperSyncMember.getMemberId());
        grouperProvisioningEntity.setSubjectId(gcGrouperSyncMember.getSubjectId());
        grouperProvisioningEntity.assignAttributeValue("subjectSourceId", gcGrouperSyncMember.getSourceId());
        if ("subjectIdentifier1".equals(grouperProvisioner.retrieveGrouperProvisioningBehavior().getSubjectIdentifierForMemberSyncTable())) {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier1", gcGrouperSyncMember.getSubjectIdentifier());
        } else if ("subjectIdentifier2".equals(grouperProvisioner.retrieveGrouperProvisioningBehavior().getSubjectIdentifierForMemberSyncTable())) {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier2", gcGrouperSyncMember.getSubjectIdentifier());
        } else {
          grouperProvisioningEntity.assignAttributeValue("subjectIdentifier0", gcGrouperSyncMember.getSubjectIdentifier());
        }
        
        if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
          
          String jsonMetadata = gcGrouperSyncMember.getMetadataJson();
          
          if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
            JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
            for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
              if (grouperProvisioningObjectMetadataItem.isShowForMember()) {
                
                String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
                if (metadataItemName.startsWith("md_")) {
                  if (jsonNode.has(metadataItemName)) {
                    GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                        GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                    String value = GrouperUtil.jsonJacksonGetString(jsonNode, metadataItemName);
                    grouperProvisioningEntity.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                  }
                }
              }
            }
          }
        }
        
//        //TODO select in bulk from grouper members
//        Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), gcGrouperSyncMember.getMemberId(), false);
//        if (member != null) {
//          grouperProvisioningEntity.setName(member.getName());
//          grouperProvisioningEntity.setEmail(member.getEmail0());
//          
//          grouperProvisioningEntity.assignAttributeValue("description", member.getDescription());
//        }

      }
      
      provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
      provisioningEntityWrapper.getProvisioningStateEntity().setDelete(true);
      
      memberUuidToProvisioningMemberWrapper.put(grouperProvisioningEntity.getId(), provisioningEntityWrapper);
      
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
  
    List<GrouperProvisioningObjectMetadataItem> grouperProvisioningObjectMetadataItems = 
        this.grouperProvisioner.retrieveGrouperProvisioningObjectMetadata().getGrouperProvisioningObjectMetadataItems();
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();
  
    // loop through sync groups
    for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
  
      //TODO Perhaps look at grouper_groups information for unprovisionable groups that aren't deleted from grouper just like 
      // we did for entities.
      
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
        
        if (GrouperUtil.length(grouperProvisioningObjectMetadataItems) > 0) {
          
          String jsonMetadata = gcGrouperSyncGroup.getMetadataJson();
          
          if (!StringUtils.isBlank(jsonMetadata) && !StringUtils.equals("{}", jsonMetadata)) {
            JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonMetadata);
            for (GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem : grouperProvisioningObjectMetadataItems) {
              if (grouperProvisioningObjectMetadataItem.isShowForMember()) {
                
                String metadataItemName = grouperProvisioningObjectMetadataItem.getName();
                if (metadataItemName.startsWith("md_")) {
                  if (jsonNode.has(metadataItemName)) {
                    GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
                        GrouperUtil.defaultIfNull(grouperProvisioningObjectMetadataItem.getValueType(), GrouperProvisioningObjectMetadataItemValueType.STRING);
                    String value = GrouperUtil.jsonJacksonGetString(jsonNode, metadataItemName);
                    grouperProvisioningGroup.assignAttributeValue(metadataItemName, grouperProvisioningObjectMetadataItemValueType.convert(value));
                  }
                }
              }
            }
          }
        }
        
        provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
        
        groupUuidToProvisioningGroupWrapper.put(gcGrouperSyncGroup.getGroupId(), provisioningGroupWrapper);
        
      }
      
    }
    if (provisioningGroupsToDeleteCount > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "provisioningGroupsToDeleteCount", provisioningGroupsToDeleteCount);
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
        provisioningMembershipWrapper.getProvisioningStateMembership().setDelete(true);
        
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
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroupMissingIncremental()) {
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
    
    // log this
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingGroupsForCreate, missingGroups);

    // translate
    List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(missingGroups, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningGroups(grouperTargetGroups);

    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(grouperTargetGroups);
    
    //lets retrieve these
    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroups, false));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
    
    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(targetGroups);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(targetGroups);

    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "targetGroupsRetrieved", GrouperUtil.length(targetGroups));

    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetGroupsRetrieved, targetGroups);

    
  }

  /**
   * 
   */
  public void retrieveIndividualMissingGroups() {
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
      return;
    }
    
    List<ProvisioningGroupWrapper> missingGroupWrappers = new ArrayList<ProvisioningGroupWrapper>();
    List<ProvisioningGroup> missingGrouperTargetGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      if (provisioningGroupWrapper.getGrouperProvisioningGroup() == null || provisioningGroupWrapper.getGrouperTargetGroup() == null
          || !provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
        continue;
      }

      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      if (gcGrouperSyncGroup == null) {
        continue;
      }

      // the case we have a problem with is its in target but the target group is not there
      if (!gcGrouperSyncGroup.isInTarget() || provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
        continue;
      }
            
      
      provisioningGroupWrapper.getProvisioningStateGroup().setSelectResultProcessed(true);
      
      missingGrouperTargetGroups.add(provisioningGroupWrapper.getGrouperTargetGroup());
      missingGroupWrappers.add(provisioningGroupWrapper);
    }

    if (GrouperUtil.length(missingGroupWrappers) == 0) {
      return;
    }

    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingGroupsForRetrieve", GrouperUtil.length(missingGroupWrappers));

    // Step 1 - Get all the grouper target entities and select them from the target (Call the batch method that gets all at once)

    TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter()
        .retrieveGroups(new TargetDaoRetrieveGroupsRequest(missingGrouperTargetGroups, true));
    
    List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

    this.grouperProvisioner.getDebugMap().put("missingGroupsForRetrieveFound", GrouperUtil.length(targetGroups));

    if (GrouperUtil.length(targetGroups) == 0) {
      return;
    }
    
    // Step 2 - Go through retrieveAllData method and whatever processing is done on the target entities; perform them here as well
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers();

    // add wrappers for all groups
    for (ProvisioningGroup targetProvisioningGroup : GrouperUtil.nonNull(targetGroups)) {
      if (targetProvisioningGroup.getProvisioningGroupWrapper() == null) {
        ProvisioningGroupWrapper provisioningGroupWrapper = new ProvisioningGroupWrapper();
        provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningGroupWrappers.add(provisioningGroupWrapper);
    
        provisioningGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
      }
    }
    
    // Step 3 - Go through the full logic and see if any other processing is done on the target entities
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesGroups(targetGroups, false, true, false, false);
    
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator()
      .idTargetGroups(targetGroups);

    // index the groups and entity matching ids
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups(targetGroups);

    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualMissingGroups, targetGroups);

  }
  
  /**
   * 
   */
  public void retrieveIndividualMissingMemberships() {
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
      return;
    }
    
    List<ProvisioningMembership> membershipsRetrieved = new ArrayList<>();
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForGroup()) {
      List<ProvisioningGroup> groupsToRetrieve = new ArrayList<ProvisioningGroup>();
      for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
        
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMembershipResultProcessed()) {
          continue;
        }
        provisioningGroupWrapper.getProvisioningStateGroup().setSelectAllMembershipResultProcessed(true);
        
        if (provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
          continue;
        }
        groupsToRetrieve.add(provisioningGroupWrapper.getTargetProvisioningGroup());
      }
      this.grouperProvisioner.getDebugMap().put("missingGroupsForMembershipsRetrieve", GrouperUtil.length(groupsToRetrieve));
      if (groupsToRetrieve.size() == 0) {
        return;
      }
      TargetDaoRetrieveMembershipsByGroupsRequest membershipsByGroupsRequest = new TargetDaoRetrieveMembershipsByGroupsRequest();
      membershipsByGroupsRequest.setTargetGroups(groupsToRetrieve);
      
      
      TargetDaoRetrieveMembershipsByGroupsResponse membershipsByGroups = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembershipsByGroups(membershipsByGroupsRequest);
      membershipsRetrieved.addAll(GrouperUtil.nonNull(membershipsByGroups.getTargetMemberships()));
    } else if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForEntity()) {
      List<ProvisioningEntity> entitiesToRetrieve = new ArrayList<ProvisioningEntity>();
      for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectAllMembershipResultProcessed()) {
          continue;
        }
        provisioningEntityWrapper.getProvisioningStateEntity().setSelectAllMembershipResultProcessed(true);
        
        if (provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
          continue;
        }
        
        entitiesToRetrieve.add(provisioningEntityWrapper.getTargetProvisioningEntity());
      }
      this.grouperProvisioner.getDebugMap().put("missingEntitiesForMembershipsRetrieve", GrouperUtil.length(entitiesToRetrieve));
      if (entitiesToRetrieve.size() == 0) {
        return;
      }
      TargetDaoRetrieveMembershipsByEntitiesRequest membershipsByEntitiesRequest = new TargetDaoRetrieveMembershipsByEntitiesRequest();
      membershipsByEntitiesRequest.setTargetEntities(entitiesToRetrieve);
      TargetDaoRetrieveMembershipsByEntitiesResponse membershipsByEntities= this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembershipsByEntities(membershipsByEntitiesRequest);
      membershipsRetrieved.addAll(GrouperUtil.nonNull(membershipsByEntities.getTargetMemberships()));
    } else {
      throw new RuntimeException("cannot retrieve partial memberships");
    }
    
    // we have retrieved memberships by this point from the target so mark all the grouper memberships retrieved
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers())) {
      provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
    }
    
    this.grouperProvisioner.getDebugMap().put("missingMembershipsForRetrieveFound", GrouperUtil.length(membershipsRetrieved));
    
    if (GrouperUtil.length(membershipsRetrieved) == 0) {
      return;
    }

    this.getGrouperProvisioner().retrieveGrouperProvisioningLogic().processTargetDataMemberships(membershipsRetrieved);
    
    // index the memberships
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships(membershipsRetrieved);

    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualMissingMemberships, membershipsRetrieved);

  }

  /**
   * if incremental, and there are missing groups or entities, then retrieve them
   */
  public void retrieveMissingEntitiesIncremental() {
    
    // first lets see if we should even be doing this
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntityMissingIncremental()) {
      return;
    }
      
    //do we have missing groups?
    List<ProvisioningEntity> missingEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities())) {
      
      // shouldnt be null at this point
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      
      if (!gcGrouperSyncMember.isProvisionable()) {
        continue;
      }
      if (this.grouperProvisioner.retrieveGrouperProvisioningLinkLogic().entityLinkMissing(gcGrouperSyncMember)) {
        missingEntities.add(provisioningEntity);
      }
    }

    if (GrouperUtil.length(missingEntities) == 0) {
      return;
    }
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingIncrementalEntitiesForRetrieve", missingEntities);
    
    // log this
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingEntitiesForCreate, missingEntities);

    // translate
    List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(missingEntities, false, false);
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getGrouperTargetObjectsMissing().setProvisioningEntities(grouperTargetEntities);

    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(grouperTargetEntities);
    
    //lets retrieve these
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntities, false));
    
    List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
    
    // index
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(targetEntities);
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(targetEntities);

    GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "targetEntitiesRetrieved", GrouperUtil.length(targetEntities));

    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.missingTargetEntitiesRetrieved, targetEntities);

    
  }

  /**
   * 
   */
  public void retrieveIndividualMissingEntities() {
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
      return;
    }
    
    List<ProvisioningEntityWrapper> missingEntityWrappers = new ArrayList<ProvisioningEntityWrapper>();
    List<ProvisioningEntity> missingGrouperTargetEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      
      if (provisioningEntityWrapper.getGrouperProvisioningEntity() == null || provisioningEntityWrapper.getGrouperTargetEntity() == null
          || !provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
        continue;
      }
  
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      
      if (gcGrouperSyncMember == null) {
        continue;
      }
  
      // the case we have a problem with is its in target but the target group is not there
      if (!gcGrouperSyncMember.isInTarget() || provisioningEntityWrapper.getTargetProvisioningEntity() != null) {
        continue;
      }
      
      provisioningEntityWrapper.getProvisioningStateEntity().setSelectResultProcessed(true);
            
      missingGrouperTargetEntities.add(provisioningEntityWrapper.getGrouperTargetEntity());
      missingEntityWrappers.add(provisioningEntityWrapper);
    }
  
    if (GrouperUtil.length(missingEntityWrappers) == 0) {
      return;
    }
  
    // how many do we have 
    this.grouperProvisioner.getDebugMap().put("missingEntitiesForRetrieve", GrouperUtil.length(missingEntityWrappers));
  
    // Step 1 - Get all the grouper target entities and select them from the target (Call the batch method that gets all at once)
  
    TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter()
        .retrieveEntities(new TargetDaoRetrieveEntitiesRequest(missingGrouperTargetEntities, true));
    
    List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
  
    GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetEntityToTargetNativeEntity().putAll(
        GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse.getTargetEntityToTargetNativeEntity()));

    this.grouperProvisioner.getDebugMap().put("missingEntitiesForRetrieveFound", GrouperUtil.length(targetEntities));
  
    if (GrouperUtil.length(targetEntities) == 0) {
      return;
    }
    
    // Step 2 - Go through retrieveAllData method and whatever processing is done on the target entities; perform them here as well
    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappers = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers();
  
    // add wrappers for all groups
    for (ProvisioningEntity targetProvisioningEntity : GrouperUtil.nonNull(targetEntities)) {
      if (targetProvisioningEntity.getProvisioningEntityWrapper() == null) {
        ProvisioningEntityWrapper provisioningEntityWrapper = new ProvisioningEntityWrapper();
        provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningEntityWrappers.add(provisioningEntityWrapper);
    
        provisioningEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
      }
    }
    
    // Step 3 - Go through the full logic and see if any other processing is done on the target entities
    this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateDefaultsFilterAttributesEntities(targetEntities, false, true, false, false);
    
    this.grouperProvisioner.retrieveGrouperProvisioningTranslator()
      .idTargetEntities(targetEntities);
  
    // index the groups and entity matching ids
    this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities(targetEntities);
  
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveIndividualMissingEntities, targetEntities);
  
  }

  /**
   * 
   */
  public List<ProvisioningMembership> retrieveIndividualTargetMembershipsIfNeeded() {
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAll() || !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
      return null;
    }

    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isSelectAllEntities() && this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isSelectAllGroups()) {
      return null;
    }
    
    
    if (this.grouperProvisioner.getProvisioningStateGlobal().isSelectResultProcessedMemberships()) {
      return null;
    }
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      return null;
    }
    
    // see if we can get memberships by group
    if (GrouperUtil.booleanValue(
        this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
          .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)
        || GrouperUtil.booleanValue(
            this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {
      
      // deal with all groups, get the target groups already selected
      List<ProvisioningGroup> targetProvisioningGroups = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups();

      if (GrouperUtil.length(targetProvisioningGroups) == 0) {
        return null;
      }
      
      for (ProvisioningGroup targetProvisioningGroup: targetProvisioningGroups) {
        targetProvisioningGroup.getProvisioningGroupWrapper()
          .getProvisioningStateGroup().setSelectAllMembershipResultProcessed(true);
      }
      
      TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest = new TargetDaoRetrieveMembershipsByGroupsRequest(targetProvisioningGroups);
      
      TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembershipsByGroups(targetDaoRetrieveMembershipsByGroupsRequest);

      List<ProvisioningMembership> membershipObjects = retrieveMembershipsByGroups.getTargetMemberships();
      
      this.getGrouperProvisioner().getProvisioningStateGlobal().setSelectResultProcessedIndividualMemberships(true);
      
      return membershipObjects;
    } else if (GrouperUtil.booleanValue(
          this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)
          || GrouperUtil.booleanValue(
              this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
              .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {
        
      // deal with all entities, get the target entities already selected
      List<ProvisioningEntity> targetProvisioningEntities = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities();

      if (GrouperUtil.length(targetProvisioningEntities) == 0) {
        return null;
      }
      
      for (ProvisioningEntity targetProvisioningEntity: targetProvisioningEntities) {
        targetProvisioningEntity.getProvisioningEntityWrapper()
          .getProvisioningStateEntity().setSelectAllMembershipResultProcessed(true);
      }
      
      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest = new TargetDaoRetrieveMembershipsByEntitiesRequest(targetProvisioningEntities);
      
      TargetDaoRetrieveMembershipsByEntitiesResponse retrieveMembershipsByEntities = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningTargetDaoAdapter().retrieveMembershipsByEntities(targetDaoRetrieveMembershipsByEntitiesRequest);

      List<ProvisioningMembership> membershipObjects = retrieveMembershipsByEntities.getTargetMemberships();
      
      this.getGrouperProvisioner().getProvisioningStateGlobal().setSelectResultProcessedIndividualMemberships(true);
      
      return membershipObjects;
    } else {
      throw new RuntimeException("Not expecting DAO capabilities when selecting memberships!  Should be able to select memberships by group or entity!");
    }
  
  }


}
