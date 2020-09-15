package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

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
      debugMap.put("retrieveDataPass1_millis", retrieveDataPass1);
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
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("linkData");
    }
      
    try {
      debugMap.put("state", "validateInitialProvisioningData");
      this.validateGrouperProvisioningData();
  
      debugMap.put("state", "translateGrouperToTarget");
      this.grouperProvisioner.retrieveTranslator().translateGrouperToTarget();
      // note in a full sync this wont do anything since there are no includeDelete objects there
      this.grouperProvisioner.retrieveTranslator().translateGrouperToTargetIncludeDeletes();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateGrouperToTarget");
    }

    try {
      debugMap.put("state", "targetIdGrouperObjects");
      this.grouperProvisioner.retrieveTranslator().targetIdGrouperObjects();
      this.grouperProvisioner.retrieveTranslator().targetIdGrouperObjectsIncludeDeletes();
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
        debugMap.put("retrieveDataPass2_millis", retrieveDataPass2);
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
      TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();
      targetDaoSendChangesToTargetRequest.setTargetObjectInserts(this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectInserts());
      targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates());
      targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectDeletes());
      this.getGrouperProvisioner().retrieveTargetDao().sendChangesToTarget(targetDaoSendChangesToTargetRequest);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("sendChangesToTarget");
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


  public void setupIncrementalGrouperTargetObjectsToRetrieveFromTarget() {

    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = 
        this.getGrouperProvisioner().getGrouperProvisioningData().getTargetDaoRetrieveIncrementalDataRequest();

    GrouperIncrementalUuidsToRetrieveFromGrouper grouperIncrementalUuidsToRetrieveFromGrouper =
        this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper();

    {
      //the groups only should be the full list of grouper target groups
      List<ProvisioningGroup> grouperTargetGroupsForGroupOnly = new ArrayList<ProvisioningGroup>(
          GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups()));
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsForGroupOnly);
    }

    {
      // generally these arent needed, but if there are no target groups (e.g. only entities or memberships), maybe needed
      List<ProvisioningGroupWrapper> groupWrappersForGroupOnly = new ArrayList<ProvisioningGroupWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper()).values());
      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupOnly(groupWrappersForGroupOnly);
    }
    {
      //the entities only should be the full list of grouper target entities
      List<ProvisioningEntity> grouperTargetEntitiesForEntityOnly = new ArrayList<ProvisioningEntity>(
          GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities()));
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesForEntityOnly);
    }
    {
      // generally these arent needed, but if there are no target entities (e.g. only groups or memberships), maybe needed
      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly = new ArrayList<ProvisioningEntityWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper()).values());
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
        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
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
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
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
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperTargetEntityIncludeDelete();

        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroupIncludeDelete();

        ProvisioningMembershipWrapper provisioningMembershipWrapper = this.grouperProvisioner.getGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(groupUuidMemberUuid);
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


  public void retrieveTargetEntityLink() {
    // TODO If using target entity link and the ID is not in the member sync cache object, then resolve the target entity, and put the id in the member sync object
    
  }

  public void updateGroupLinkIncremental() {
    // If using target group link and the ID is not in the group sync cache object, then resolve the target group, and put the id in the group sync object
    Collection<GcGrouperSyncGroup> gcGrouperSyncGroups = GrouperUtil.nonNull(
        this.grouperProvisioner.getGrouperProvisioningData().getGroupUuidToSyncGroup()).values();

    if (GrouperUtil.length(gcGrouperSyncGroups) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String groupLinkGroupFromId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId2 = !StringUtils.isBlank(groupLinkGroupFromId2);
    
    String groupLinkGroupFromId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupFromId3 = !StringUtils.isBlank(groupLinkGroupFromId3);

    String groupLinkGroupToId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId2 = !StringUtils.isBlank(groupLinkGroupToId2);

    String groupLinkGroupToId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupToId3();
    boolean hasGroupLinkGroupToId3 = !StringUtils.isBlank(groupLinkGroupToId3);

    if (!hasGroupLinkGroupFromId2 && !hasGroupLinkGroupFromId3 && !hasGroupLinkGroupToId2 && !hasGroupLinkGroupToId3) {
      return;
    }
    
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToRefreshGroupLink = new ArrayList<GcGrouperSyncGroup>();
    
    int refreshGroupLinkIfLessThanAmount = this.grouperProvisioner.retrieveProvisioningConfiguration().getRefreshSubjectLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncGroups) <= refreshGroupLinkIfLessThanAmount) {
      gcGrouperSyncGroupsToRefreshGroupLink.addAll(gcGrouperSyncGroups);
    } else {
      for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
        boolean needsRefresh = false;
        needsRefresh = needsRefresh || (hasGroupLinkGroupFromId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId2()));
        needsRefresh = needsRefresh || (hasGroupLinkGroupFromId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId3()));
        needsRefresh = needsRefresh || (hasGroupLinkGroupToId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId2()));
        needsRefresh = needsRefresh || (hasGroupLinkGroupToId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId3()));
        if (needsRefresh) {
          gcGrouperSyncGroupsToRefreshGroupLink.add(gcGrouperSyncGroup);
        }
      }
    }
    int subjectsNeedsRefreshDueToLink = GrouperUtil.length(gcGrouperSyncGroupsToRefreshGroupLink);
    this.grouperProvisioner.getDebugMap().put("subjectsNeedRefreshDueToLink", subjectsNeedsRefreshDueToLink);
    if (subjectsNeedsRefreshDueToLink == 0) {
      return;
    }
    // TODO retrieve groups and updateGroupLink(gcGrouperSyncGroupsToRefreshGroupLink);
  }

  public void updateGroupLinkFull() {
    updateGroupLink(GrouperUtil.nonNull(
        this.grouperProvisioner.getGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningGroups()));
  }

  /**
   * TODO make a link class and move logic there
   */
  public void retrieveTargetGroupLink() {
    this.grouperProvisioner.getGrouperProvisioningType().updateGroupLink(this.grouperProvisioner);
  }

  /**
   * update group link for these groups
   * @param gcGrouperSyncGroupsToRefreshGroupLink
   */
  public void updateGroupLink(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.length(targetGroups) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String groupLinkGroupFromId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId2 = !StringUtils.isBlank(groupLinkGroupFromId2);
    
    String groupLinkGroupFromId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupFromId3 = !StringUtils.isBlank(groupLinkGroupFromId3);

    String groupLinkGroupToId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId2 = !StringUtils.isBlank(groupLinkGroupToId2);

    String groupLinkGroupToId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getGroupLinkGroupToId3();
    boolean hasGroupLinkGroupToId3 = !StringUtils.isBlank(groupLinkGroupToId3);

    if (!hasGroupLinkGroupFromId2 && !hasGroupLinkGroupFromId3 && !hasGroupLinkGroupToId2 && !hasGroupLinkGroupToId3) {
      return;
    }

    int groupsCannotFindLinkData = 0;

    int groupsCannotFindSyncGroup = 0;

    Set<MultiKey> sourceIdSubjectIds = new HashSet<MultiKey>();
    
    for (ProvisioningGroup targetGroup : targetGroups) {

      GcGrouperSyncGroup gcGrouperSyncGroup = targetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      
      if (gcGrouperSyncGroup == null) {
        groupsCannotFindSyncGroup++;
        continue;
      }

      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetGroup", targetGroup);
      
      if (hasGroupLinkGroupFromId2) {
        String groupFromId2Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId2, variableMap);
        gcGrouperSyncGroup.setGroupFromId2(groupFromId2Value);
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupFromId3Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId3, variableMap);
        gcGrouperSyncGroup.setGroupFromId3(groupFromId3Value);
      }
      
      if (hasGroupLinkGroupToId2) {
        String groupToId2Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId2, variableMap);
        gcGrouperSyncGroup.setGroupToId2(groupToId2Value);
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupToId3Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId3, variableMap);
        gcGrouperSyncGroup.setGroupToId3(groupToId3Value);
      }
      
    }

    if (groupsCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("groupsCannotFindLinkData", groupsCannotFindLinkData);
    }
    if (groupsCannotFindSyncGroup > 0) {
      this.grouperProvisioner.getDebugMap().put("groupsCannotFindSyncGroup", groupsCannotFindSyncGroup);
    }
    
    
  }


  public void retrieveSubjectLink() {

    Collection<GcGrouperSyncMember> gcGrouperSyncMembers = GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData().getMemberUuidToSyncMember()).values();

    if (GrouperUtil.length(gcGrouperSyncMembers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String subjectLinkMemberFromId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getSubjectLinkMemberFromId2();
    boolean hasSubjectLinkMemberFromId2 = !StringUtils.isBlank(subjectLinkMemberFromId2);
    
    String subjectLinkMemberFromId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getSubjectLinkMemberFromId3();
    boolean hasSubjectLinkMemberFromId3 = !StringUtils.isBlank(subjectLinkMemberFromId3);

    String subjectLinkMemberToId2 = this.grouperProvisioner.retrieveProvisioningConfiguration().getSubjectLinkMemberToId2();
    boolean hasSubjectLinkMemberToId2 = !StringUtils.isBlank(subjectLinkMemberToId2);

    String subjectLinkMemberToId3 = this.grouperProvisioner.retrieveProvisioningConfiguration().getSubjectLinkMemberToId3();
    boolean hasSubjectLinkMemberToId3 = !StringUtils.isBlank(subjectLinkMemberToId3);

    if (!hasSubjectLinkMemberFromId2 && !hasSubjectLinkMemberFromId3 && !hasSubjectLinkMemberToId2 && !hasSubjectLinkMemberToId3) {
      return;
    }
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink = new ArrayList<GcGrouperSyncMember>();
    
    int refreshSubjectLinkIfLessThanAmount = this.grouperProvisioner.retrieveProvisioningConfiguration().getRefreshSubjectLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncMembers) <= refreshSubjectLinkIfLessThanAmount) {
      gcGrouperSyncMembersToRefreshSubjectLink.addAll(gcGrouperSyncMembers);
    } else {
      for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
        boolean needsRefresh = false;
        needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId2()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId3()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId2()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberToId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId3()));
        if (needsRefresh) {
          gcGrouperSyncMembersToRefreshSubjectLink.add(gcGrouperSyncMember);
        }
      }
    }
    int subjectsNeedsRefreshDueToLink = GrouperUtil.length(gcGrouperSyncMembersToRefreshSubjectLink);
    this.grouperProvisioner.getDebugMap().put("subjectsNeedRefreshDueToLink", subjectsNeedsRefreshDueToLink);
    if (subjectsNeedsRefreshDueToLink == 0) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperSyncDao().updateSubjectLink(gcGrouperSyncMembersToRefreshSubjectLink);
  }

  /**
   * get data from change log
   */
  public void retrieveIncrementalDataPass1() {
        
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);

    this.retrieveGrouperData();
  }
  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  public void retrieveAllData() {
    
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          TargetDaoRetrieveAllDataResponse targetDaoRetrieveAllDataResponse
            = GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao()
              .retrieveAllData(new TargetDaoRetrieveAllDataRequest());
          GrouperProvisioningLogic.this.grouperProvisioner.getGrouperProvisioningData()
            .setTargetProvisioningObjects(targetDaoRetrieveAllDataResponse.getTargetData());
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    retrieveGrouperData();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
  }


  public void retrieveGrouperData() {
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
    
    // incrementals need to clone and setup sync objects as deletes
    this.grouperProvisioner.getGrouperProvisioningType().setupClonesOfGroupProvisioningObjects(grouperProvisioner);

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperSyncDao().fixSyncObjects();

    // incrementals need to consult sync objects to know what to delete
    this.grouperProvisioner.getGrouperProvisioningType().calculateProvisioningDataToDelete(grouperProvisioner);

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
   * make a deep copy of grouper provisioning data into the grouper provisioning objects to delete
   */
  public void setupIncrementalClonesOfGroupProvisioningObjects() {
    GrouperProvisioningLists grouperProvisioningObjects = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects();
    GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjectsIncludeDeletes();
  
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
  
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember();
  
    List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningEntities();
    
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningMemberWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
  
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
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningEntities(grouperProvisioningEntities);
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
  
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup();
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
  
    int provisioningGroupsToDeleteCount = 0;
  
    List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().getProvisioningGroups();
  
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
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningGroups(grouperProvisioningGroups);
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
  
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership();
  
    List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().getGrouperProvisioningData()
        .getGrouperProvisioningObjects().getProvisioningMemberships();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .getGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
  
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper();
  
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
          this.getGrouperProvisioner().getGrouperProvisioningData().getGrouperProvisioningObjects().setProvisioningMemberships(grouperProvisioningMemberships);
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

}
