package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveIncrementalDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveMembershipsResponse;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMembershipMessage;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.ProvisioningMessage;
import edu.internet2.middleware.grouper.messaging.GrouperBuiltinMessagingSystem;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupUser;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.ExpirableCache.ExpirableCacheUnit;

public class GrouperProvisioningLogicIncremental {

  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  public GrouperProvisioningLogicIncremental() {
  }

  /**
   * cache of queues with messages, get with the method
   */
  private static ExpirableCache<Boolean, Set<String>> provisioningMessageQueuesWithMessages = null;

  /**
   * get the cache of queues with messages
   * @return the cache
   */
  private static ExpirableCache<Boolean, Set<String>> provisioningMessageQueuesWithMessages() {
    if (provisioningMessageQueuesWithMessages == null) {
      // TODO see what the fastest cron is set to, and set this accordingly
      int secondsToCache = GrouperLoaderConfig.retrieveConfig().propertyValueInt("provisioningMessagesCheckCacheSeconds", 14);
      provisioningMessageQueuesWithMessages = new ExpirableCache<Boolean, Set<String>>(ExpirableCacheUnit.SECOND, secondsToCache);
    }
    return provisioningMessageQueuesWithMessages;
  }


  /**
   * see if queue has messages
   * @param provisionerName
   * @return true if has messages
   */
  public boolean provisioningMessageQueueHasMessages(String provisionerName) {
    ExpirableCache<Boolean, Set<String>> provisioningMessageQueuesWithMessagesCache = provisioningMessageQueuesWithMessages();
    Set<String> provisioningQueuesWithMessages = provisioningMessageQueuesWithMessagesCache.get(Boolean.TRUE);
    if (provisioningQueuesWithMessages == null) {
      synchronized (provisioningMessageQueuesWithMessagesCache) {
        provisioningQueuesWithMessages = provisioningMessageQueuesWithMessagesCache.get(Boolean.TRUE);
        if (provisioningQueuesWithMessages == null) {
          provisioningQueuesWithMessages = GrouperDAOFactory.getFactory().getMessage().queuesWithMessagesByPrefix("grouperProvisioningControl_");
          provisioningMessageQueuesWithMessagesCache.put(Boolean.TRUE, provisioningQueuesWithMessages);
        }
      }
    }
    return provisioningQueuesWithMessages.contains("grouperProvisioningControl_" + provisionerName);
  }

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
   * acknowledgeMessages processed
   * @param esbEventContainersToProcess
   * @param gcGrouperSync
   * @param lastProcessedSequenceNumber
   */
  public void acknowledgeMessagesProcessed() {
    
    if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperMessages()) > 0) {
      GrouperMessagingEngine.acknowledge(
          new GrouperMessageAcknowledgeParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
            .assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed)
            .assignGrouperMessages(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperMessages())
            .assignQueueName("grouperProvisioningControl_" + this.getGrouperProvisioner().getConfigId()));
      this.getGrouperProvisioner().getDebugMap().put("grouperMessagesAcknowledged", 
          GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperMessages()));
    }
  }

  /**
   * take out events that are not provisionable to this target
   * @param esbEventContainersToProcess
   * @param grouperProvisioningProcessingResult
   */
  public void filterByGroupNotProvisionable() {
    
    int filterByNotProvisionable = 0;
    
    Iterator<ProvisioningGroupWrapper> iterator = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()).iterator();

    Set<String> validGroupIds = new HashSet<String>();

    while(iterator.hasNext()) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = iterator.next();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup == null) {
        iterator.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().remove(provisioningGroupWrapper.getGroupId());
        filterByNotProvisionable++;
        continue;
      }
      
      // allow null since we dont know if its in the target or not
      if (gcGrouperSyncGroup.isProvisionable() || (gcGrouperSyncGroup.getInTarget() == null || gcGrouperSyncGroup.getInTarget())) {
        
        validGroupIds.add(gcGrouperSyncGroup.getGroupId());
        continue; 
      } 
      
      // TODO batch these? and is query correct?  should it check grouper memberships to?
      int count = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMembershipDao().
          internal_membershipRetrieveFromDbCountByGroupSyncId(gcGrouperSyncGroup.getId());
        
      // if we're doing entity attributes and a group is deleted, it's not provisionable but if there are still memberships for this group
      // we need to address them
      if (count > 0) {
        validGroupIds.add(gcGrouperSyncGroup.getGroupId());
        continue;
      }
        
      filterByNotProvisionable++;
      iterator.remove();
    }
    
    Iterator<ProvisioningMembershipWrapper> iteratorMemberships = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()).iterator();
   
    while (iteratorMemberships.hasNext()) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iteratorMemberships.next();
      if (provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
      
      if (!validGroupIds.contains(groupId)) {
        iteratorMemberships.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
        filterByNotProvisionable++;
      }
    }
    
    if (filterByNotProvisionable > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByNotProvisionable", filterByNotProvisionable);
    }

  }


  /**
     * filter events that happened before the last full sync
     * @param esbEventContainers
     * @param gcGrouperSync
     */
  public void filterByProvisioningFullSync() {

    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    // check for full sync, only if it finished
    Timestamp lastFullSync = gcGrouperSync.getLastFullSyncRun() != null ? gcGrouperSync.getLastFullSyncStart() : null;

    //TODO    Timestamp lastFullMetadataSync = gcGrouperSync.getLastFullMetadataSyncRun();
    int skippedEventsDueToFullSync = 0;
    
    if (lastFullSync != null) {
      long lastFullSyncMillis = lastFullSync.getTime();
      
      GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
      
      if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp() != null) {
        if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp().getTime() < lastFullSyncMillis) {
          grouperProvisioningDataIncrementalInput.setFullSyncMessageTimestamp(null);
          grouperProvisioningDataIncrementalInput.setFullSync(false);
          skippedEventsDueToFullSync++;
        }
      }

      Iterator<ProvisioningGroupWrapper> iteratorGroups = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()).iterator();

      while(iteratorGroups.hasNext()) {
        
        ProvisioningGroupWrapper provisioningGroupWrapper = iteratorGroups.next();
        
        Long millisSince1970 = provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 < lastFullSyncMillis) {
          skippedEventsDueToFullSync++;
          iteratorGroups.remove();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().remove(provisioningGroupWrapper.getGroupId());

        }
        
      }
      
      Iterator<ProvisioningEntityWrapper> iteratorEntities = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()).iterator();

      while(iteratorEntities.hasNext()) {
        
        ProvisioningEntityWrapper provisioningEntityWrapper = iteratorEntities.next();
        
        Long millisSince1970 = provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 < lastFullSyncMillis) {
          skippedEventsDueToFullSync++;
          iteratorEntities.remove();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().remove(provisioningEntityWrapper.getMemberId());

        }
        
      }
      
      Iterator<ProvisioningMembershipWrapper> iteratorMemberships = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()).iterator();

      while(iteratorMemberships.hasNext()) {
        
        ProvisioningMembershipWrapper provisioningMembershipWrapper = iteratorMemberships.next();
        
        Long millisSince1970 = provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 < lastFullSyncMillis) {
          skippedEventsDueToFullSync++;
          iteratorMemberships.remove();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper()
            .remove(provisioningMembershipWrapper.getGroupIdMemberId());
        }
        
      }


    }
    if (skippedEventsDueToFullSync > 0) {
      this.getGrouperProvisioner().getDebugMap().put("skippedEventsDueToFullSync", skippedEventsDueToFullSync);
    }
  }

  /**
   * filter events that happened before the last group sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void filterByGroupSync() {
  
    int filterByGroupSyncGroups = 0;
    int filterByGroupSyncMemberships = 0;
    
    Map<String, Long> groupIdToLastGroupSync = new HashMap<String, Long>();
    
    Iterator<ProvisioningGroupWrapper> iteratorGroups = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()).iterator();

    while(iteratorGroups.hasNext()) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = iteratorGroups.next();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      Long millisSince1970 = provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970();
      if (millisSince1970 == null) {
        continue;
      }

      if (gcGrouperSyncGroup == null || gcGrouperSyncGroup.getLastGroupSyncStart() == null) {
        continue;
      }

      long lastGroupSync = gcGrouperSyncGroup.getLastGroupSyncStart().getTime();
      
      groupIdToLastGroupSync.put(gcGrouperSyncGroup.getGroupId(), lastGroupSync);
      
      if (millisSince1970 < lastGroupSync) {
        filterByGroupSyncGroups++;
        iteratorGroups.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().remove(provisioningGroupWrapper.getGroupId());

      }
      
    }
    
    Iterator<ProvisioningMembershipWrapper> iteratorMemberships = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()).iterator();

    while(iteratorMemberships.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iteratorMemberships.next();
      
      Long millisSince1970 = provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970();
      if (millisSince1970 == null || provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      
      Long lastGroupSync = groupIdToLastGroupSync.get((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0));
      
      if (lastGroupSync == null) {
        continue;
      }
      
      if (millisSince1970 < lastGroupSync) {
        filterByGroupSyncMemberships++;
        iteratorMemberships.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper()
          .remove(provisioningMembershipWrapper.getGroupIdMemberId());
      }
      
    }
  
    if (filterByGroupSyncGroups > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByGroupSyncGroups", filterByGroupSyncGroups);
    }
    if (filterByGroupSyncMemberships > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByGroupSyncMemberships", filterByGroupSyncMemberships);
    }

  }

  /**
   * filter events that happened before the last entity sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void filterByEntitySync() {
  
    int filterByEntitySyncEntities = 0;
    int filterByEntitySyncMemberships = 0;
    
    Map<String, Long> memberIdToLastEntitySync = new HashMap<String, Long>();
    
    Iterator<ProvisioningEntityWrapper> iteratorEntities = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()).iterator();

    while(iteratorEntities.hasNext()) {
      
      ProvisioningEntityWrapper provisioningEntityWrapper = iteratorEntities.next();

      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      
      Long millisSince1970 = provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970();
      if (millisSince1970 == null) {
        continue;
      }

      if (gcGrouperSyncMember == null || gcGrouperSyncMember.getLastUserSyncStart() == null) {
        continue;
      }

      long lastEntitySync = gcGrouperSyncMember.getLastUserSyncStart().getTime();
      
      memberIdToLastEntitySync.put(gcGrouperSyncMember.getMemberId(), lastEntitySync);
      
      if (millisSince1970 < lastEntitySync) {
        filterByEntitySyncEntities++;
        iteratorEntities.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().remove(provisioningEntityWrapper.getMemberId());

      }
      
    }
    
    Iterator<ProvisioningMembershipWrapper> iteratorMemberships = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()).iterator();

    while(iteratorMemberships.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iteratorMemberships.next();
      
      Long millisSince1970 = provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970();
      if (millisSince1970 == null || provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      
      Long lastEntitySync = memberIdToLastEntitySync.get((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1));
      
      if (lastEntitySync == null) {
        continue;
      }
      
      if (millisSince1970 < lastEntitySync) {
        filterByEntitySyncMemberships++;
        iteratorMemberships.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper()
          .remove(provisioningMembershipWrapper.getGroupIdMemberId());
      }
      
    }
  
    if (filterByEntitySyncEntities > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByEntitySyncEntities", filterByEntitySyncEntities);
    }
    if (filterByEntitySyncMemberships > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByEntitySyncMemberships", filterByEntitySyncMemberships);
    }

  }

//  /**
//   * go through esb event containers and 
//   * @param esbEventContainers
//   * @return groupIds to investigate
//   */
//  private Set<String> groupIdsToQueryProvisioningAttributes() {
//    
//    Set<String> groupIdsToInvestigate = new HashSet<String>();
//    
//    Set<String> attributeAssignIdsToInvestigate = new HashSet<String>();
//    
//    // target name
//    String provisioningTargetAttributeDefNameId = GrouperProvisioningAttributeNames.retrieveAttributeDefNameTarget().getId();
//  
//    // do provision
//    String provisioningDoProvisionAttributeDefNameId = GrouperProvisioningAttributeNames.retrieveAttributeDefNameDoProvision().getId();
//  
//    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
//  
//      // for logging
//      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
//  
//      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
//      
//      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
//  
//      Map<String, Object> debugMapForEvent = esbEventContainer.getDebugMapForEvent();
//      
//      switch (esbEventType) {
//        
//        
//        case ATTRIBUTE_ASSIGN_ADD:
//        case ATTRIBUTE_ASSIGN_DELETE:
//          
//          String attributeAssignType = esbEvent.getAttributeAssignType();
//          if (!AttributeAssignType.group_asgn.name().equals(attributeAssignType)) {
//            
//            debugMapForEvent.put("ignoreProvisioningUpdatesDueToAssignType", attributeAssignType);
//            
//            continue;
//          }
//  
//          // fall through
//          
//        case ATTRIBUTE_ASSIGN_VALUE_ADD:
//        case ATTRIBUTE_ASSIGN_VALUE_DELETE:
//          
//          String esbEventAttributeDefNameId = esbEvent.getAttributeDefNameId();
//          
//          if (!StringUtils.equals(provisioningTargetAttributeDefNameId, esbEventAttributeDefNameId)
//              && !StringUtils.equals(provisioningDoProvisionAttributeDefNameId, esbEventAttributeDefNameId)) {
//            
//            debugMapForEvent.put("ignoreProvisioningUpdatesDueToAttributeDefName", esbEvent.getAttributeDefNameName());
//            
//            continue;
//            
//          }
//  
//          debugMapForEvent.put("processProvisioningUpdatesForAssignId", esbEvent.getAttributeAssignId());
//  
//          //lets look at attributeAssignOnAssignIds
//          attributeAssignIdsToInvestigate.add(esbEvent.getAttributeAssignId());
//          
//          break;
//          
//        case GROUP_DELETE:
//        case GROUP_ADD:
//          
//          debugMapForEvent.put("processProvisioningUpdatesForGroupId", esbEvent.getGroupId());
//          groupIdsToInvestigate.add(esbEvent.getGroupId());
//          
//          break;
//          
//      }
//      
//    }
//    // for logging
//    debugMapOverall.put("currentSequenceNumber", null);
//    logIntegerIfNotZero(debugMapOverall, "groupIdCountAddOrDelete", GrouperUtil.length(groupIdsToInvestigate));
//  
//    
//    if (GrouperUtil.length(attributeAssignIdsToInvestigate) > 0) {
//      logIntegerIfNotZero(debugMapOverall, "attributeAssignIdsToInvestigate", GrouperUtil.length(attributeAssignIdsToInvestigate));
//      Set<String> groupIds = GrouperProvisioningService.findAllGroupIdsFromAttributeAssignIdsOnIds(attributeAssignIdsToInvestigate);
//      logIntegerIfNotZero(debugMapOverall, "groupIdCountFromAttributeAssignIds", GrouperUtil.length(groupIds));
//      groupIdsToInvestigate.addAll(GrouperUtil.nonNull(groupIds));
//    }
//    
//    return groupIdsToInvestigate;
//  }


  /**
     * 
     * @return if should continue
     */
    public boolean incrementalCheckForAndRunFullSync() {
  //    //look for full sync before we configure the real time
  //    boolean ranFullSync = false;
  //    GrouperProvisioningOutput grouperProvisioningOutput = null;
  //    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
  //      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
  //      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
  //            
  //      switch (esbEventType) {
  //        
  //        case PROVISIONING_SYNC_FULL:
  //          
  //          GrouperProvisioningType grouperProvisioningType = null;
  //          
  //          if (!StringUtils.isBlank(esbEvent.getProvisionerSyncType())) {
  //            grouperProvisioningType = GrouperProvisioningType.valueOfIgnoreCase(esbEvent.getProvisionerSyncType(), true);
  //          } else {
  //            grouperProvisioningType = GrouperProvisioningType.fullProvisionFull;
  //          }
  //          
  //          grouperProvisioningOutput = grouperProvisioner.provision(grouperProvisioningType); 
  //          ranFullSync = true;
  //          break;
  //        default: 
  //          break;
  //      }
  //    }
      return true;
    }


  public void incrementalCheckMessages() {
    
    int messageCountForProvisioner = 0;
    
    if (provisioningMessageQueueHasMessages(this.getGrouperProvisioner().getConfigId())) {

      GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
      
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      
      GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(
          new GrouperMessageReceiveParam().assignGrouperMessageSystemName(GrouperBuiltinMessagingSystem.BUILTIN_NAME)
            .assignQueueType(GrouperMessageQueueType.queue)
            .assignQueueName("grouperProvisioningControl_" + gcGrouperSync.getProvisionerName()));
  
      // list of messages
      List<GrouperMessage> grouperMessages = new ArrayList<GrouperMessage>(GrouperUtil.nonNull(grouperMessageReceiveResult.getGrouperMessages()));
      
      grouperProvisioningDataIncrementalInput.setGrouperMessages(grouperMessages);
      
      // sets dont add if the element is there, so reverse these, so the most recent messages are added to the set, and
      // old duplicates are ignored
      Collections.reverse(grouperMessages);
      for (GrouperMessage grouperMessage : grouperMessages) {
        String messageBody = grouperMessage.getMessageBody();
  
        ProvisioningMessage provisioningMessage = ProvisioningMessage.fromJson(messageBody);
  
        if (provisioningMessage.getFullSync() != null && provisioningMessage.getFullSync()) {
          boolean useThisFullSync = false;
          
          if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp() == null) {
            useThisFullSync = true;
          } else if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp() != null && provisioningMessage.getMillisSince1970() > grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp().getTime()) {
            useThisFullSync = true;
          }

          if (useThisFullSync) {
            grouperProvisioningDataIncrementalInput.setFullSync(true);
            grouperProvisioningDataIncrementalInput.setFullSyncMessageTimestamp(new Timestamp(provisioningMessage.getMillisSince1970()));
          }

          messageCountForProvisioner++;
        } else {
          
          if (GrouperUtil.length(provisioningMessage.getGroupIdsForSync()) > 0) {

            for (String groupId : provisioningMessage.getGroupIdsForSync()) {
              
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc()
                  , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc(), provisioningMessage.getMillisSince1970(), null);

              messageCountForProvisioner++;
            }
            
          }
          
          if (GrouperUtil.length(provisioningMessage.getMemberIdsForSync()) > 0) {
            for (String memberId : provisioningMessage.getMemberIdsForSync()) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(memberId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc()
                  , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntityMembershipsForRecalc(), provisioningMessage.getMillisSince1970(), null);
              messageCountForProvisioner++;
            }
            
          }
          
          if (GrouperUtil.length(provisioningMessage.getMembershipsForSync()) > 0) {
            for (ProvisioningMembershipMessage provisioningMembershipMessage : provisioningMessage.getMembershipsForSync()) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(provisioningMembershipMessage.getGroupId(), provisioningMembershipMessage.getMemberId(),
                  this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc(), provisioningMessage.getMillisSince1970(), null);
            }
            messageCountForProvisioner++;
            
          }
          
        }
        
      }
    }
    
    if (messageCountForProvisioner > 0) {
      this.getGrouperProvisioner().getDebugMap().put("messageCountForProvisioner", messageCountForProvisioner);
    }
  
    
  }



//  /**
//   * 
//   * @param esbEventContainersToProcess
//   * @param gcGrouperSync
//   */
//  private GrouperProvisioningProcessingResult processProvisioningMetadata() {
//    
//    // get group ids which need to be analyzed
//    Set<String> groupIds = groupIdsToQueryProvisioningAttributes(esbEventContainersToProcess);
//    
//    this.grouperProvisioningProcessingResult =
//        GrouperProvisioningService.processProvisioningMetadataForGroupIds(gcGrouperSync, groupIds);
//    
//    grouperProvisioningProcessingResult.setGcGrouperSyncJob(gcGrouperSyncJob);
//    
//    grouperProvisioningProcessingResult.setGcGrouperSyncLog(gcGrouperSync.getGcGrouperSyncJobDao().jobCreateLog(gcGrouperSyncJob));
//  
//    grouperProvisioningProcessingResult.getGcGrouperSyncLog().setStatus(null);
//    
//    logIntegerIfNotZero(debugMapOverall, "groupIdCountToAddToTarget", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget()));
//    this.internal_esbConsumerTestingData.groupIdCountToAddToTarget = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget());
//    logIntegerIfNotZero(debugMapOverall, "groupIdCountToRemoveFromTarget", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget()));
//    this.internal_esbConsumerTestingData.groupIdCountToRemoveFromTarget = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget());
//    logIntegerIfNotZero(debugMapOverall, "gcGrouperSyncGroupsCountInitial", GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap()));
//    this.internal_esbConsumerTestingData.gcGrouperSyncGroupsCountInitial = GrouperUtil.length(grouperProvisioningProcessingResult.getGroupIdToGcGrouperSyncGroupMap());
//    
//    // setup heartbeat thread
//    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
//    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
//    gcGrouperSyncHeartbeat.addHeartbeatLogic(provisioningHeartbeatLogic());
//    this.grouperProvisioningProcessingResult.setGcGrouperSyncHeartbeat(gcGrouperSyncHeartbeat);
//    if (!gcGrouperSyncHeartbeat.isStarted()) {
//      gcGrouperSyncHeartbeat.runHeartbeatThread();
//    }
//    
//    return grouperProvisioningProcessingResult;
//  }


//  /**
//   * get the member objects currently in the db
//   * @param esbEventContainers
//   * @param gcGrouperSync
//   */
//  private void retrieveProvisioningMemberSyncObjects(List<EsbEventContainer> esbEventContainers, GcGrouperSync gcGrouperSync) {
//  
//    // we need all the memberIds
//    Set<String> memberIdsToRetrieve = new HashSet<String>();
//  
//    if (this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap() == null) {
//      this.grouperProvisioningProcessingResult.setMemberIdToGcGrouperSyncMemberMap(new HashMap<String, GcGrouperSyncMember>());
//    }
//  
//    for (EsbEventContainer esbEventContainer : esbEventContainers) {
//      
//      // for logging
//      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
//  
//      String memberId = esbEventContainer.getEsbEvent().getMemberId();
//      
//      // dont worry if there is no group id
//      if (StringUtils.isBlank(memberId)) {
//        continue;
//      }
//      
//      GcGrouperSyncMember gcGrouperSyncMember = this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().get(memberId);
//      
//      // if this is there, when we done
//      if (gcGrouperSyncMember != null) {
//        esbEventContainer.setGcGrouperSyncMember(gcGrouperSyncMember);
//      } else {
//        memberIdsToRetrieve.add(memberId);
//      }
//      
//    }
//    
//    // for logging
//    debugMapOverall.put("currentSequenceNumber", null);
//  
//    // lets retrieve all those
//    Map<String, GcGrouperSyncMember> memberIdToSyncMemberMap = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);
//  
//    logIntegerIfNotZero(debugMapOverall, "gcGrouperSyncMembersRetrievedByEvents", GrouperUtil.length(memberIdToSyncMemberMap));
//  
//    if (this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap() == null) {
//      this.grouperProvisioningProcessingResult.setMemberIdToGcGrouperSyncMemberMap(new HashMap<String, GcGrouperSyncMember>());
//    }
//  
//    this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().putAll(memberIdToSyncMemberMap);
//  
//    //setup in the event objects
//    for (EsbEventContainer esbEventContainer : esbEventContainers) {
//      
//      // for logging
//      debugMapOverall.put("currentSequenceNumber", esbEventContainer.getSequenceNumber());
//      
//      String memberId = esbEventContainer.getEsbEvent().getMemberId();
//      
//      // dont worry if there is no group id or if we already have it
//      if (esbEventContainer.getGcGrouperSyncMember() != null || StringUtils.isBlank(memberId)) {
//        continue;
//      }
//      
//      GcGrouperSyncMember gcGrouperSyncMember = this.grouperProvisioningProcessingResult.getMemberIdToGcGrouperSyncMemberMap().get(memberId);
//      
//      // if this is there, when we done
//      if (gcGrouperSyncMember != null) {
//        esbEventContainer.setGcGrouperSyncMember(gcGrouperSyncMember);
//      }
//      
//    }
//    
//    // for logging
//    debugMapOverall.put("currentSequenceNumber", null);
//    
//    
//  }



  public void propagateProvisioningAttributes() {
    List<EsbEventContainer> esbEventContainers = new ArrayList<EsbEventContainer>(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));

    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningGroupAttributesToProcess = new HashMap<String, GrouperProvisioningObjectAttributes>();
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningMemberAttributesToProcess = new HashMap<String, GrouperProvisioningObjectAttributes>();
    Map<String, GrouperProvisioningObjectAttributes> allAncestorProvisioningGroupAttributes = new HashMap<String, GrouperProvisioningObjectAttributes>();
    String grouperObjectTypeName = GrouperObjectTypesSettings.objectTypesStemName() + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
    
    Set<String> queriedPITAttributeAssignIds = new HashSet<String>();
    
    List<String> memberIdsToFetchProvisioningAttributesFor = new ArrayList<String>();
    
    boolean shouldCheckForMemberSyncsToDelete = false;
    
    Map<String, Long> memberIdToEventTime = new HashMap<>();
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      if (esbEventType == EsbEventType.MEMBERSHIP_ADD) {
        
        memberIdsToFetchProvisioningAttributesFor.add(esbEvent.getMemberId());
        
      } else if (esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
        shouldCheckForMemberSyncsToDelete = true;
      } else if (esbEventType == EsbEventType.GROUP_ADD) {
        
        GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupAttributesByGroup(esbEvent.getGroupId());
        if (grouperProvisioningObjectAttributes == null) {
          grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(esbEvent.getGroupId(), esbEvent.getGroupName(), Long.parseLong(esbEvent.getGroupIdIndex()), null);
          grouperProvisioningObjectAttributes.setOwnedByGroup(true);
        }

        grouperProvisioningGroupAttributesToProcess.put(esbEvent.getGroupId(), grouperProvisioningObjectAttributes);

        String parentFolderName = GrouperUtil.parentStemNameFromName(esbEvent.getGroupName());
        if (!allAncestorProvisioningGroupAttributes.containsKey(parentFolderName)) {
          Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningGroupAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(esbEvent.getParentStemId());
          allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningGroupAttributes);
        }
      } else if (esbEventType == EsbEventType.GROUP_UPDATE && "name".equals(esbEvent.getPropertyChanged())) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(esbEvent.getGroupId(), false);

        if (group != null) {
          GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupAttributesByGroup(esbEvent.getGroupId());
          if (grouperProvisioningObjectAttributes == null) {
            grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(esbEvent.getGroupId(), esbEvent.getGroupName(), group.getIdIndex(), null);
            grouperProvisioningObjectAttributes.setOwnedByGroup(true);
            grouperProvisioningObjectAttributes.setUpdated(true);
          }
  
          grouperProvisioningGroupAttributesToProcess.put(esbEvent.getGroupId(), grouperProvisioningObjectAttributes);
  
          String parentFolderName = GrouperUtil.parentStemNameFromName(esbEvent.getGroupName());
          if (!allAncestorProvisioningGroupAttributes.containsKey(parentFolderName)) {
            Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningGroupAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(esbEvent.getParentStemId());
            allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningGroupAttributes);
          }
        }
      } else if (esbEventType == EsbEventType.GROUP_DELETE) {
        GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(esbEvent.getGroupId(), esbEvent.getGroupName(), Long.parseLong(esbEvent.getGroupIdIndex()), null);
        grouperProvisioningObjectAttributes.setOwnedByGroup(true);
        grouperProvisioningObjectAttributes.setDeleted(true);
        grouperProvisioningGroupAttributesToProcess.put(esbEvent.getGroupId(), grouperProvisioningObjectAttributes);
        shouldCheckForMemberSyncsToDelete = true;
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) &&
          esbEvent.getAttributeDefNameName().startsWith(GrouperProvisioningSettings.provisioningConfigStemName())) {

        if (esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) {
          shouldCheckForMemberSyncsToDelete = true;
        }
        
        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(esbEvent.getAttributeAssignId(), false);

        if (pitAttributeAssign != null) {
          // query pit to see if this is for a folder and for this provisioner and is direct
          if (!queriedPITAttributeAssignIds.contains(pitAttributeAssign.getOwnerAttributeAssignId())) {
            queriedPITAttributeAssignIds.add(pitAttributeAssign.getOwnerAttributeAssignId());
            
            
            
            String stemId = grouperProvisioner.retrieveGrouperDao().getStemIdIfDirectStemAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
            String groupId = grouperProvisioner.retrieveGrouperDao().getGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
            String memberId = grouperProvisioner.retrieveGrouperDao().getMemberIdIfDirectMemberAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
            
            if (stemId != null) {
              Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningFolderAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(stemId);
              Map<String, GrouperProvisioningObjectAttributes> childProvisioningFolderAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveChildProvisioningFolderAttributesByFolder(stemId);
              grouperProvisioningGroupAttributesToProcess.putAll(this.grouperProvisioner.retrieveGrouperDao().retrieveChildProvisioningGroupAttributesByFolder(stemId));
              allAncestorProvisioningGroupAttributes.putAll(childProvisioningFolderAttributes);
              allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningFolderAttributes);
            } else if (groupId != null) {
              
              Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(groupId, false);

              if (group != null) {
                GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupAttributesByGroup(group.getId());
                if (grouperProvisioningObjectAttributes == null) {
                  grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(group.getId(), group.getName(), group.getIdIndex(), null);
                  grouperProvisioningObjectAttributes.setOwnedByGroup(true);
                }

                grouperProvisioningGroupAttributesToProcess.put(group.getId(), grouperProvisioningObjectAttributes);

                String parentFolderName = GrouperUtil.parentStemNameFromName(group.getName());
                if (!allAncestorProvisioningGroupAttributes.containsKey(parentFolderName)) {
                  Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningGroupAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(group.getParentUuid());
                  allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningGroupAttributes);
                }
              }
            } else if (StringUtils.isNotBlank(memberId)) {
              memberIdsToFetchProvisioningAttributesFor.add(memberId);
              memberIdToEventTime.put(memberId, esbEvent.getCreatedOnMicros()/1000);
            }
          }
        }
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) &&
          esbEvent.getAttributeDefNameName().equals(grouperObjectTypeName) &&
          ("policy".equals(esbEvent.getPropertyNewValue()) || "policy".equals(esbEvent.getPropertyOldValue()))) {

        PITAttributeAssign pitAttributeAssign = GrouperDAOFactory.getFactory().getPITAttributeAssign().findBySourceIdMostRecent(esbEvent.getAttributeAssignId(), false);
        if (pitAttributeAssign != null && pitAttributeAssign.getOwnerAttributeAssignId() != null) {
          PITAttributeAssign pitAttributeAssignOwner = GrouperDAOFactory.getFactory().getPITAttributeAssign().findById(pitAttributeAssign.getOwnerAttributeAssignId(), true);
          
          if (pitAttributeAssignOwner.getOwnerGroupId() != null) {
            PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findById(pitAttributeAssignOwner.getOwnerGroupId(), true);
            Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(pitGroup.getSourceId(), false);
  
            if (group != null) {
              GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupAttributesByGroup(group.getId());
              if (grouperProvisioningObjectAttributes == null) {
                grouperProvisioningObjectAttributes = new GrouperProvisioningObjectAttributes(group.getId(), group.getName(), group.getIdIndex(), null);
                grouperProvisioningObjectAttributes.setOwnedByGroup(true);
              }
  
              grouperProvisioningGroupAttributesToProcess.put(group.getId(), grouperProvisioningObjectAttributes);
  
              String parentFolderName = GrouperUtil.parentStemNameFromName(group.getName());
              if (!allAncestorProvisioningGroupAttributes.containsKey(parentFolderName)) {
                Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningGroupAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(group.getParentUuid());
                allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningGroupAttributes);
              }
            }
          }
        }
      }
    }
    
    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesForMembers = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningMemberAttributes(false, memberIdsToFetchProvisioningAttributesFor);
    
    Set<String> syncMemberIdsToRetrieve = new HashSet<String>();
    
    for (GrouperProvisioningObjectAttributes objectAttributes : grouperProvisioningObjectAttributesForMembers.values()) {
      syncMemberIdsToRetrieve.add(objectAttributes.getId());
    }
    
    Map<String, GcGrouperSyncMember> grouperSyncMemberIdToSyncMember = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(syncMemberIdsToRetrieve);
    
    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();
    this.getGrouperProvisioner().setProvisioningSyncResult(provisioningSyncResult);
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningSyncIntegration()
      .fullSyncMembers(grouperProvisioningObjectAttributesForMembers, new HashSet<GcGrouperSyncMember>(grouperSyncMemberIdToSyncMember.values()));
    
    Set<String> memberIdsToRecalc = new HashSet(GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningSyncResult().getMemberIdsToUpdate()));
    memberIdsToRecalc.addAll(GrouperUtil.nonNull(this.getGrouperProvisioner().getProvisioningSyncResult().getMemberIdsWithChangedSubjectIds()));
    
    for (String memberId: memberIdsToRecalc) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(memberId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc(), false, memberIdToEventTime.get(memberId), null);
    }
    
    this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    
//    if (shouldCheckForMemberSyncsToDelete) {
//      List<GcGrouperSyncMember> memberRetrieveFromDbDeletables = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao()
//          .internal_memberRetrieveFromDbDeletables();
//      if (GrouperUtil.length(memberRetrieveFromDbDeletables) > 0) {
//        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao().memberDelete(memberRetrieveFromDbDeletables, true, false);
//      }
//    }
    
    Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess = new HashSet<GrouperProvisioningObjectAttributes>();
    grouperProvisioningObjectAttributesToProcess.addAll(grouperProvisioningGroupAttributesToProcess.values());
    
    Set<String> policyGroupIds = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupIdsThatArePolicyGroups(grouperProvisioningGroupAttributesToProcess.keySet());
    
    if (grouperProvisioningObjectAttributesToProcess.size() > 0) {
      Map<String, GrouperProvisioningObjectAttributes> calculatedProvisioningAttributes = GrouperProvisioningService.calculateProvisioningAttributes(this.getGrouperProvisioner(), grouperProvisioningObjectAttributesToProcess, allAncestorProvisioningGroupAttributes, policyGroupIds);
      
      Set<String> syncGroupIdsToRetrieve = new HashSet<String>();
      
      // we need to know later which groups were updated vs not so that we can put the updated groups
      // in the right bucket: grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc().getGroupUuidsForGroupOnly()
      // and for non-updated ones, we put them in grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc().getGroupUuidsForGroupMembershipSync()
      // basically, when a group is updated, we only want to update the group in the target and not touch it's memberships
      Set<String> groupIdsThatWereUpdated = new HashSet<String>();
      
      for (GrouperProvisioningObjectAttributes objectAttributes : grouperProvisioningObjectAttributesToProcess) {
        syncGroupIdsToRetrieve.add(objectAttributes.getId());
        if (objectAttributes.isUpdated()) {
          groupIdsThatWereUpdated.add(objectAttributes.getId());
        }
      }

      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(syncGroupIdsToRetrieve);
      
      this.getGrouperProvisioner().retrieveGrouperProvisioningSyncIntegration()
        .fullSyncGroups(calculatedProvisioningAttributes, new HashSet<GcGrouperSyncGroup>(grouperSyncGroupIdToSyncGroup.values()));
      
      Set<String> groupIdsToTriggerSync = new HashSet<String>();
      for (GcGrouperSyncGroup gcGrouperSyncGroup : this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().retrieveUpdatedCacheSyncGroups()) {
        groupIdsToTriggerSync.add(gcGrouperSyncGroup.getGroupId());
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();

      for (String groupId : groupIdsToTriggerSync) {
        // not using this anymore, used to be used to see if a group can be updated without syncing memberships
        // groupIdsThatWereUpdated
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc()
            , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc(), System.currentTimeMillis(), null);
      }      
    }
  }
  
  public void addGroupsFromSqlAttributes() {
    
    GrouperProvisioningConfiguration provisioningConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    boolean resolveAttributesWithSql = provisioningConfiguration.isResolveGroupAttributesWithSql();
    if (!resolveAttributesWithSql) {
      return;
    }
    
    String lastUpdatedColumn = provisioningConfiguration.getGroupAttributesLastUpdatedColumn();
    String lastUpdatedColumnType = provisioningConfiguration.getGroupAttributesLastUpdatedType();
    
    if (StringUtils.isBlank(lastUpdatedColumn) || StringUtils.isBlank(lastUpdatedColumnType)) {
      return; // don't do anything because we need last updated column and type both to retrieve groups that have changed
    }
    
    String dbConnectionName = provisioningConfiguration.getGroupAttributesSqlExternalSystem();
    
    String tableOrViewName = provisioningConfiguration.getGroupAttributesTableViewName();
    
    String groupMatchingColumn = provisioningConfiguration.getGroupAttributesGroupMatchingColumn();
    
    Timestamp lastFullSyncTimestamp = this.grouperProvisioner.getGcGrouperSync().getLastFullSyncStart();
    Timestamp lastIncrementalSyncTimestamp = this.grouperProvisioner.getGcGrouperSyncJob().getLastSyncStart();
    
    int compare = GrouperUtil.compare(lastFullSyncTimestamp, lastIncrementalSyncTimestamp);
    
    Timestamp lastSyncTimestamp = null;
    if (compare == 0) {
      if (lastFullSyncTimestamp != null) {
        lastSyncTimestamp = lastFullSyncTimestamp;
      }
    } else if (compare > 0) {
      lastSyncTimestamp = lastFullSyncTimestamp;
    } else if (compare < 0) {
      lastSyncTimestamp = lastIncrementalSyncTimestamp;
    }
    
    if (lastSyncTimestamp == null) {
      throw new RuntimeException("No last sync timestamp found for provisioner "+this.grouperProvisioner.getConfigId());
    }
    
    StringBuilder sqlInitial = new StringBuilder("select ");
    sqlInitial.append(groupMatchingColumn);
    sqlInitial.append(" from ");
    sqlInitial.append(tableOrViewName);
    sqlInitial.append(" where " + lastUpdatedColumn + " > ?");
    
    GcDbAccess gcDbAccess = new GcDbAccess().connectionName(dbConnectionName).sql(sqlInitial.toString());
    
    if (StringUtils.equals(lastUpdatedColumnType, "timestamp")) {
      gcDbAccess.addBindVar(lastSyncTimestamp);
    } else if (StringUtils.equals(lastUpdatedColumnType, "millisSince1970")) {
      long lastSyncMillisSince1970 = lastSyncTimestamp.getTime();
      gcDbAccess.addBindVar(lastSyncMillisSince1970);
    } else {
      throw new RuntimeException("Invalid groupAttributesLastUpdatedType for provisioner: "+this.grouperProvisioner.getConfigId());
    }
    
    List<String> groupNames = gcDbAccess.selectList(String.class);
    
    List<ProvisioningGroup> groupsToAddForProcessing = this.getGrouperProvisioner().retrieveGrouperDao().retrieveGroupsFromNames(groupNames);
    
    boolean selectGroupsForRecalc = getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc();
    boolean recalcMembershipsOnIncremental = getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupAttributesRecalcMembershipsOnIncremental();
    
    for(ProvisioningGroup provisioningGroup: groupsToAddForProcessing) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(provisioningGroup.getId(), selectGroupsForRecalc,
          recalcMembershipsOnIncremental, null, null);
    }
    
    if (GrouperUtil.length(groupsToAddForProcessing) > 0) {
      this.grouperProvisioner.getDebugMap().put("groupAttributeIncrementalCount", GrouperUtil.length(groupsToAddForProcessing));
    }
      
  }

  public void incrementalCheckChangeLog() {
    
    int recalcEntityDueToGroupMembershipChange = 0;
    
    Set<MultiKey> groupIdFieldIds = new HashSet<MultiKey>();
    Map<MultiKey, Set<MultiKey>> groupIdFieldIdToMemberIdCreateOns = new HashMap<>();
    
    // see if we are getting memberships or privs
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();

    boolean recalcOnly = false;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isRecalculateAllOperations()) {
      recalcOnly = true;
    }
    
    int changeLogCount = 0;
    
    this.getGrouperProvisioner().getDebugMap().put("changeLogRawCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));

    this.grouperProvisioner.retrieveGrouperProvisioningOutput().setTotalCount(GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));
    
    Set<String> sourceIdsToProvision = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision();
    
    // these events are already filtered
    // make a new array list so we dont re-order the existing object
    List<EsbEventContainer> esbEventContainers = new ArrayList<EsbEventContainer>(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));

    // the sets wont add if the element is already there, so add newer actions first, and ignore older actions
    Collections.reverse(esbEventContainers);
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();

      // this cant be null
      long createdOnMillis = esbEvent.getCreatedOnMicros()/1000;
      boolean syncThisMembership = false;
      
      MultiKey groupIdFieldId = null;

      switch (esbEventType) {
        
//        case ATTRIBUTE_ASSIGN_VALUE_ADD:
//        case ATTRIBUTE_ASSIGN_VALUE_DELETE:
//          
//          if (StringUtils.equals(GrouperProvisioningSettings.provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_METADATA_JSON, esbEvent.getAttributeDefNameName())) {
//            
//            String attributeAssignId = esbEvent.getAttributeAssignId();
//            
//            AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
//            if (attributeAssign != null) {
//              AttributeAssign ownerAttributeAssign = attributeAssign.getOwnerAttributeAssign();
//              if (ownerAttributeAssign != null) {
//                String ownerMemberId = ownerAttributeAssign.getOwnerMemberId();
//                String ownerGroupId = ownerAttributeAssign.getOwnerGroupId();
//                if (StringUtils.isNotBlank(ownerMemberId)) {
//                  this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(ownerMemberId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc(), false, createdOnMillis, null);
//                } else if (StringUtils.isNotBlank(ownerGroupId)) {
//                  this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(ownerGroupId, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc(), false, createdOnMillis, null);
//                }
//              }
//            }
//            
//          }
//          
//          break;
        case PRIVILEGE_ADD:
        case PRIVILEGE_DELETE:
        case PRIVILEGE_UPDATE:
          
          // skip if wrong source
          if (!StringUtils.isBlank(esbEvent.getSourceId()) && GrouperUtil.length(sourceIdsToProvision) > 0 && !sourceIdsToProvision.contains(esbEvent.getSourceId())) {
            continue;
          }
          if (!StringUtils.isBlank(esbEvent.getGroupId())) {
            Field field = FieldFinder.find(esbEvent.getPrivilegeName(), true);
            groupIdFieldId = new MultiKey(esbEvent.getGroupId(), field.getId());
            groupIdFieldIds.add(groupIdFieldId);
            Set<MultiKey> memberIdCreatedOns = groupIdFieldIdToMemberIdCreateOns.get(groupIdFieldId);
            if (memberIdCreatedOns == null) {
              memberIdCreatedOns = new HashSet<>();
              groupIdFieldIdToMemberIdCreateOns.put(groupIdFieldId, memberIdCreatedOns);
            }
            memberIdCreatedOns.add(new MultiKey(esbEvent.getMemberId(), esbEvent.getCreatedOnMicros()/1000));
          }
          
          switch (membershipFieldType) {
            case admin:
              syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName());
              break;
            case readAdmin:
              syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName()) || StringUtils.equals("readers", esbEvent.getFieldName()) ;
              
              break;
              
            case updateAdmin:
              syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName()) || StringUtils.equals("updaters", esbEvent.getFieldName()) ;
              
              break;
            default:
              // skip
          }
          
          break;
          
        case GROUP_ADD:
        case GROUP_DELETE:
        case GROUP_UPDATE:
          
          if (StringUtils.isBlank(esbEvent.getGroupId())) {
            continue;
          }

          changeLogCount++;
          
//      if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups() || !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForGroup()) {

          GrouperIncrementalDataAction grouperIncrementalDataAction = null;
          if (esbEventType == EsbEventType.GROUP_ADD) {
            grouperIncrementalDataAction = GrouperIncrementalDataAction.insert;
          } else if (esbEventType == EsbEventType.GROUP_UPDATE) {
            grouperIncrementalDataAction = GrouperIncrementalDataAction.update;
          } else if (esbEventType == EsbEventType.GROUP_DELETE) {
            grouperIncrementalDataAction = GrouperIncrementalDataAction.delete;
          } else {
            throw new RuntimeException("Unexpected esbEventType: " + esbEventType);
          }
          
          // if we are insert or delete, lets recalc memberships
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(esbEvent.getGroupId(), 
              this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc(),
              esbEventType != EsbEventType.GROUP_UPDATE && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc(), createdOnMillis, grouperIncrementalDataAction);
            
          break;
        case MEMBERSHIP_ADD:
        case MEMBERSHIP_DELETE:
        case MEMBERSHIP_UPDATE:

          // skip if wrong source
          if (!StringUtils.isBlank(esbEvent.getSourceId()) && GrouperUtil.length(sourceIdsToProvision) > 0 && !sourceIdsToProvision.contains(esbEvent.getSourceId())) {
            continue;
          }
          
          groupIdFieldId = new MultiKey(esbEvent.getGroupId(), Group.getDefaultList().getId());
          groupIdFieldIds.add(groupIdFieldId);
          Set<MultiKey> memberIdCreatedOns = groupIdFieldIdToMemberIdCreateOns.get(groupIdFieldId);
          if (memberIdCreatedOns == null) {
            memberIdCreatedOns = new HashSet<>();
            groupIdFieldIdToMemberIdCreateOns.put(groupIdFieldId, memberIdCreatedOns);
          }
          memberIdCreatedOns.add(new MultiKey(esbEvent.getMemberId(), esbEvent.getCreatedOnMicros()/1000));
          
          if (membershipFieldType == GrouperProvisioningMembershipFieldType.members) {
            syncThisMembership = true;
          }

          break;
        
        default:
          continue;
      }
      if (syncThisMembership) {
        // group_id, member_id, field_id
        
        GrouperIncrementalDataAction grouperIncrementalDataAction = null;
        
        // keep track of event just in case
//        if (recalcOnly) {
//          // dont worry about actions on recalc
//          grouperIncrementalDataAction = null;
        if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.PRIVILEGE_ADD) {
          grouperIncrementalDataAction = GrouperIncrementalDataAction.insert;
        } else if (esbEventType == EsbEventType.MEMBERSHIP_UPDATE || esbEventType == EsbEventType.PRIVILEGE_UPDATE) {
          grouperIncrementalDataAction = GrouperIncrementalDataAction.update;
        } else if (esbEventType == EsbEventType.MEMBERSHIP_DELETE || esbEventType == EsbEventType.PRIVILEGE_DELETE) {
          grouperIncrementalDataAction = GrouperIncrementalDataAction.delete;
        } else {
          throw new RuntimeException("Unexpected esbEventType: " + esbEventType);
        }
        
        String groupIdOfUsersToProvision = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupIdOfUsersToProvision();
        
        // it's group of users and should be treated as member sync
        if (StringUtils.isNotBlank(groupIdOfUsersToProvision) &&
            StringUtils.equals(esbEvent.getGroupId(), groupIdOfUsersToProvision)) {
                    
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(esbEvent.getMemberId(), this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc()
              , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntityMembershipsForRecalc(), createdOnMillis, null);
          
        }
          
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(esbEvent.getGroupId(), esbEvent.getMemberId(),
            false, createdOnMillis, grouperIncrementalDataAction);
        
        changeLogCount++;
      }
      
    }
    
    boolean provisionEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities();
    if (provisionEntities) {
      
      Map<MultiKey, GcGrouperSyncDependencyGroupUser> groupIdFieldIdToDependencyGroupUser = 
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao().dependencyGroupUserRetrieveFromDbOrCacheByGroupIdsFieldIds(groupIdFieldIds);

      for (MultiKey groupIdFieldId : groupIdFieldIdToDependencyGroupUser.keySet()) {
        Set<MultiKey> memberIdCreatedOns = groupIdFieldIdToMemberIdCreateOns.get(groupIdFieldId);
        for (MultiKey memberIdCreatedOn : GrouperUtil.nonNull(memberIdCreatedOns)) {
          String memberId = (String)memberIdCreatedOn.getKey(0);
          Long createdOnMillis = (Long)memberIdCreatedOn.getKey(1);
          this.grouperProvisioner.retrieveGrouperProvisioningData().addIncrementalEntity(memberId, true, false, createdOnMillis, null);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().getGroupIdFieldIdLookedUpForGroupUserDependencies().put(groupIdFieldId, false);
      }
    }
    boolean provisionGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups();
    if (provisionGroups) {
      
      // TODO change to use cache
      Map<MultiKey, GcGrouperSyncDependencyGroupGroup> groupIdFieldIdToDependencyGroupGroup = 
          this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupGroupDao().internal_dependencyGroupGroupRetrieveFromDbByGroupIdsFieldIds(groupIdFieldIds);
      
      for (MultiKey groupIdFieldId : groupIdFieldIdToDependencyGroupGroup.keySet()) {
        GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup = groupIdFieldIdToDependencyGroupGroup.get(groupIdFieldId);
        String provisionableGroupId = gcGrouperSyncDependencyGroupGroup.getProvisionableGroupId();
        
        Set<MultiKey> memberIdCreatedOns = groupIdFieldIdToMemberIdCreateOns.get(groupIdFieldId);
        for (MultiKey memberIdCreatedOn : GrouperUtil.nonNull(memberIdCreatedOns)) {
          Long createdOnMillis = (Long)memberIdCreatedOn.getKey(1);
          this.grouperProvisioner.retrieveGrouperProvisioningData().addIncrementalGroup(provisionableGroupId, true, false, createdOnMillis, null);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().getGroupIdFieldIdLookedUpForGroupGroupDependencies().put(groupIdFieldId, false);
      }
    }
    
    if (changeLogCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("changeLogItemsApplicableByType", changeLogCount);
    }

  }


  /**
   * filter events that happened after the last full sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void recalcEventsDuringFullSync() {
  
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    // check for full sync, only if it finished
    Timestamp lastFullSyncStart = gcGrouperSync.getLastFullSyncStart();
    Timestamp lastFullSyncEnd = gcGrouperSync.getLastFullSyncRun();
  
    int recalcEventsDuringFullSync = 0;
    
    if (lastFullSyncStart != null && lastFullSyncEnd != null) {
      long lastFullSyncStartMillis = lastFullSyncStart.getTime();
      long lastFullSyncEndMillis = lastFullSyncEnd.getTime();

      Iterator<ProvisioningGroupWrapper> iteratorGroups = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()).iterator();

      while(iteratorGroups.hasNext()) {
        
        ProvisioningGroupWrapper provisioningGroupWrapper = iteratorGroups.next();
        
        Long millisSince1970 = provisioningGroupWrapper.getProvisioningStateGroup().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 >= lastFullSyncStartMillis
            && millisSince1970 < lastFullSyncEndMillis) {
          recalcEventsDuringFullSync++;
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc());
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
          
        }
        
      }
      
      Iterator<ProvisioningEntityWrapper> iteratorEntities = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()).iterator();

      while(iteratorEntities.hasNext()) {
        
        ProvisioningEntityWrapper provisioningEntityWrapper = iteratorEntities.next();
        
        Long millisSince1970 = provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 >= lastFullSyncStartMillis
            && millisSince1970 < lastFullSyncEndMillis) {
          recalcEventsDuringFullSync++;
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntityMembershipsForRecalc());
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());

        }
        
      }
      
      Iterator<ProvisioningMembershipWrapper> iteratorMemberships = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()).iterator();

      while(iteratorMemberships.hasNext()) {
        
        ProvisioningMembershipWrapper provisioningMembershipWrapper = iteratorMemberships.next();
        
        Long millisSince1970 = provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970();
        if (millisSince1970 == null) {
          continue;
        }

        if (millisSince1970 >= lastFullSyncStartMillis
            && millisSince1970 < lastFullSyncEndMillis) {
          recalcEventsDuringFullSync++;
          provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
        }
        
      }

    }
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringFullSync", recalcEventsDuringFullSync);
  }

  /**
   * 
   */
  public void addErrorsToQueue() {
    
    int addErrorsToQueue = 0;
    
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    long random100 = (long)(Math.random() * 100L);

    // always check 2 minutes back
    float secondsToCheck = -1;

    //  this.errorHandlingPercentLevel1 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel1", false), 1);
    //  this.errorHandlingMinutesLevel1 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel1", false), 180);
    //  this.errorHandlingPercentLevel2 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel2", false), 5);
    //  this.errorHandlingMinutesLevel2 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel2", false), 120);
    //  this.errorHandlingPercentLevel3 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel3", false), 10);
    //  this.errorHandlingMinutesLevel3 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel3", false), 12);
    //  this.errorHandlingPercentLevel4 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingPercentLevel4", false), 100);
    //  this.errorHandlingMinutesLevel4 = GrouperUtil.floatValue(this.retrieveConfigDouble("errorHandlingMinutesLevel4", false), 3);

    
    if (random100 < this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingPercentLevel1()) {
      // 1/100th of the time get all errors 120 minutes back
      secondsToCheck = 60*this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingMinutesLevel1() + 20;
    } if (random100 < this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingPercentLevel2()) {
      // 1/20th of the time get all errors 120 minutes back
      secondsToCheck = 60*this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingMinutesLevel2() + 20;
    } else if (random100 < this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingPercentLevel3()) {
      // 1/10th of the time get all errors 12 minutes back
      secondsToCheck = 60*this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingMinutesLevel3() + 20;
    } else if (random100 < this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingPercentLevel4()) {
      secondsToCheck = 60*this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getErrorHandlingMinutesLevel4() + 20;
      // all the time check 3 minutes back
    }

    if (secondsToCheck > 0) {
      this.getGrouperProvisioner().getDebugMap().put("checkErrorsMinutes", GrouperUtil.intValue(secondsToCheck/60));

    }
    
    long millisToCheckFrom = gcGrouperSync.getLastFullSyncStart() == null ? -1 : gcGrouperSync.getLastFullSyncStart().getTime();

    if (secondsToCheck > 0) {
      long newMillisToCheckFrom = GrouperUtil.longValue(System.currentTimeMillis() - (secondsToCheck * 1000));
      millisToCheckFrom = Math.max(millisToCheckFrom, newMillisToCheckFrom);
    }
  
    Set<String> groupIdsSet = null;
    {
      List<String> groupIds = gcGrouperSync.getGcGrouperSyncGroupDao().retrieveGroupIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      groupIdsSet = new HashSet<String>(GrouperUtil.nonNull(groupIds));
      for (String groupId : GrouperUtil.nonNull(groupIds)) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId,  this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc()
            , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc(), null, null);
        addErrorsToQueue++;
      }
    }
    
    Set<String> memberIdsSet = null;
    {
      List<String> memberIds = gcGrouperSync.getGcGrouperSyncMemberDao().retrieveMemberIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      memberIdsSet = new HashSet<String>(GrouperUtil.nonNull(memberIds));
      for (String memberId : GrouperUtil.nonNull(memberIds)) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(memberId, 
            this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc()
            , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntityMembershipsForRecalc(), null, null);
        addErrorsToQueue++;
      }
    }

    {
      List<Object[]> groupIdMemberIds = gcGrouperSync.getGcGrouperSyncMembershipDao().retrieveGroupIdMemberIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      
      for (Object[] groupIdMemberId : GrouperUtil.nonNull(groupIdMemberIds)) {
        String groupUuid = (String)groupIdMemberId[0];
        if (groupIdsSet.contains(groupUuid)) {
          continue;
        }
        String memberUuid = (String)groupIdMemberId[1];
        // see if already handled
        if (memberIdsSet.contains(memberUuid)) {
          continue;
        }
        
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(groupUuid, memberUuid, this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc(),
            null, null);
        addErrorsToQueue++;
      }
    }

    if (addErrorsToQueue > 0) {
      this.getGrouperProvisioner().getDebugMap().put("addErrorsToQueue", addErrorsToQueue);
    }
  }


  
  public void filterNonRecalcActionsCapturedByRecalc() {
    
    int filterNonRecalcActionsCapturedByRecalc = 0;

    Set<String> recalcGroupMembershipIds = new HashSet<String>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
        recalcGroupMembershipIds.add(provisioningGroupWrapper.getGroupId());
      }
    }
    
    Set<String> recalcEntityMembershipIds = new HashSet<String>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
        recalcEntityMembershipIds.add(provisioningEntityWrapper.getMemberId());
      }
    }
    
    Iterator<ProvisioningMembershipWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();

    while(iterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iterator.next();
      
      if (provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      
      // this is already recalc
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        continue;
      }
      
      // if this is incremental
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() == null) {
        continue;
      }
      
      if (recalcGroupMembershipIds.contains((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0))) {
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForGroup()) {
          iterator.remove();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
          filterNonRecalcActionsCapturedByRecalc++;
        }
        
      }
      
      if (recalcEntityMembershipIds.contains((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1))) {
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForEntity()) {
          iterator.remove();
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
          filterNonRecalcActionsCapturedByRecalc++;
        }
      }
    }
    
    if (filterNonRecalcActionsCapturedByRecalc > 0) {
      Integer filterNonRecalcActionsCapturedByRecalcInLog = GrouperUtil.intValue(this.getGrouperProvisioner().getDebugMap().get("filterNonRecalcActionsCapturedByRecalc"), 0);
      this.getGrouperProvisioner().getDebugMap().put("filterNonRecalcActionsCapturedByRecalc", filterNonRecalcActionsCapturedByRecalcInLog + filterNonRecalcActionsCapturedByRecalc);
    }
  }

  /**
   * convert many membership changes to a group sync
   */
  public void convertToGroupSync() {
    
    // TODO is entity sync better if the dao can do that?
    boolean convertAllRecalcMembershipChangesToGroupSync = 
         !(GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false));
    
    if (!convertAllRecalcMembershipChangesToGroupSync && !this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForGroup()) {
      return;
    }
    
    int convertToGroupSyncGroups = 0;
    int convertToGroupSyncMemberships = 0;
    int groupsWithRecalcMembershipsThatCannotSelectMemberships = 0;
    
    int membershipsConvertToGroupSyncThreshold = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipsConvertToGroupSyncThreshold();
        

    // we might convert to group sync for other reasons
    //  if (membershipsConvertToGroupSyncThreshold < 0) {
    //    return;
    //  }
    
    Map<String, Integer> groupUuidToMembershipCount = new HashMap<String, Integer>();
    Map<String, Long> groupUuidToLatestMillisSince1970 = new HashMap<String, Long>();
    
    Iterator<ProvisioningMembershipWrapper> membershipWrapperIterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();

    Map<String, Boolean> groupIdToHasRecalcMembership = new HashMap<>();

    //recalc or not
    while(membershipWrapperIterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = membershipWrapperIterator.next();
      if (provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
      Integer count = groupUuidToMembershipCount.get(groupId);
      if (count == null) {
        count = 0;
      }
      count++;
      groupUuidToMembershipCount.put(groupId, count);
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        groupIdToHasRecalcMembership.put(groupId, true);
      }
      
      Long latestMillisSince1970 = groupUuidToLatestMillisSince1970.get(groupId);
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() == null) {
        continue;
      }
        
      if (latestMillisSince1970 == null) {
        
        groupUuidToLatestMillisSince1970.put(groupId, provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970());
        continue;
      }
      
      // if it didnt exist, or if it is about to be null
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() > latestMillisSince1970) {
        groupUuidToLatestMillisSince1970.put(groupId, provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970());
      }
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForGroup()) {
      groupsWithRecalcMembershipsThatCannotSelectMemberships = groupIdToHasRecalcMembership.size();
    }
    
    // lets see whats over the threshold
    for (String groupId : groupUuidToMembershipCount.keySet()) {
      
      int membershipCount = groupUuidToMembershipCount.get(groupId);
      
      if (membershipCount >= membershipsConvertToGroupSyncThreshold || (groupIdToHasRecalcMembership.containsKey(groupId) && convertAllRecalcMembershipChangesToGroupSync)) {
       
        convertToGroupSyncGroups++;        
        
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId,  this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc()
            , this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc(), null, null);

        membershipWrapperIterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();

        //recalc or not
        while(membershipWrapperIterator.hasNext()) {
          
          ProvisioningMembershipWrapper provisioningMembershipWrapper = membershipWrapperIterator.next();
          if (provisioningMembershipWrapper.getGroupIdMemberId() == null) {
            continue;
          }

          if (StringUtils.equals(groupId, (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0))) {
            
            membershipWrapperIterator.remove();
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
            convertToGroupSyncMemberships++;
            
          }
        }
      }
    }
        
    if (convertToGroupSyncGroups > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertToGroupSyncGroups", convertToGroupSyncGroups);
      this.getGrouperProvisioner().getDebugMap().put("convertToGroupSyncMemberships", convertToGroupSyncMemberships);
    }
    if (groupsWithRecalcMembershipsThatCannotSelectMemberships > 0) {
      this.getGrouperProvisioner().getDebugMap().put("groupsWithRecalcMembershipsThatCannotSelectMemberships", groupsWithRecalcMembershipsThatCannotSelectMemberships);
    }

  }


  public void convertToFullSync() {
    
    int convertToFullSyncScore = 0;

    int scoreConvertToFullSyncThreshold = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getScoreConvertToFullSyncThreshold();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
        convertToFullSyncScore += 10;
      } else {
        convertToFullSyncScore++;
      }
      
    }
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
        convertToFullSyncScore += 10;
      } else {
        convertToFullSyncScore++;
      }
      
    }

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      convertToFullSyncScore++;      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        convertToFullSyncScore++;
      }
    }
    

    if (convertToFullSyncScore > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertToFullSyncScore", convertToFullSyncScore);
    }
    if (convertToFullSyncScore >= scoreConvertToFullSyncThreshold && scoreConvertToFullSyncThreshold >= 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertToFullSync", true);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setFullSync(true);
    }
  }


  /**
   * filter events that happened after the last group sync TODO check to see if can do membership recalc... or change back later
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void recalcEventsDuringGroupSync() {
  
    int recalcEventsDuringGroupSync = 0;

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningMembershipWrapper.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        continue;
      }
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();

      // if there wasnt a timestamp in this message, dont filter
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() == null) {
        continue;
      }
      // if there wasnt a last group sync start
      if (gcGrouperSyncGroup == null || gcGrouperSyncGroup.getLastGroupSyncStart() == null || gcGrouperSyncGroup.getLastGroupSync() == null) {
        continue;
      }
      if (gcGrouperSyncGroup.getLastGroupSyncStart().getTime() <= provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970()
          && provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() <= gcGrouperSyncGroup.getLastGroupSync().getTime()) {
        recalcEventsDuringGroupSync++;
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
      }
      
    }
    
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringGroupSync", recalcEventsDuringGroupSync);
  }
  
  /**
   * filter events that happened after the last entity sync TODO check to see if can do membership recalc... or change back later
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void recalcEventsDuringEntitySync() {
  
    int recalcEventsDuringEntitySync = 0;

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningMembershipWrapper.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper == null) {
        continue;
      }
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();

      // if there wasnt a timestamp in this message, dont filter
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() == null) {
        continue;
      }
      // if there wasnt a last group sync start
      if (gcGrouperSyncMember == null || gcGrouperSyncMember.getLastUserSyncStart() == null || gcGrouperSyncMember.getLastUserSync() == null) {
        continue;
      }
      if (gcGrouperSyncMember.getLastUserSyncStart().getTime() <= provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970()
          && provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() <= gcGrouperSyncMember.getLastUserSync().getTime()) {
        recalcEventsDuringEntitySync++;
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
      }
      
    }
    
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringEntitySync", recalcEventsDuringEntitySync);
  }

  
  
  
  /**
   * if a non recalc action is expected to not change the target, then ignore it
   */
  public void filterUnneededActions() {
    int filterUnneededMemberships = 0;
    
    Iterator<ProvisioningMembershipWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();

    //go through and remove from elsewhere
    while(iterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iterator.next();
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction() == null 
          || provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        continue;
      }
      
      boolean shouldRemoveMembershipAction = false;
      
      ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      boolean hasGrouperMembership = provisioningMembershipWrapper.getProvisioningStateMembership().isExistInGrouper();

      if (!shouldRemoveMembershipAction) {
        switch (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction()) {
          case delete:
            
            if (!hasGrouperMembership && gcGrouperSyncMembership != null && !gcGrouperSyncMembership.isInTarget()) {
              shouldRemoveMembershipAction = true;
            }
            
            break;
            
          case insert:

            if (hasGrouperMembership && gcGrouperSyncMembership != null && gcGrouperSyncMembership.isInTarget()) {
              shouldRemoveMembershipAction = true;
            }

            break;
        }
      }
      
      if (shouldRemoveMembershipAction) {
        filterUnneededMemberships++;
        iterator.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
      }
      
    }
  
    if (filterUnneededMemberships > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterUnneededMemberships", filterUnneededMemberships);
    }

  }

  public void convertInconsistentMembershipEventActions() {
    int convertInconsistentEventsActions = 0;

    Iterator<ProvisioningMembershipWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData()
        .getProvisioningMembershipWrappers().iterator();
    
    while (iterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iterator.next();

      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()
          || provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction() == null) {
        continue;
      }
      
      switch (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction()) {
        case delete:
          
          if (provisioningMembershipWrapper.getProvisioningStateMembership().isExistInGrouper()) {
            convertInconsistentEventsActions++;
            provisioningMembershipWrapper.getProvisioningStateMembership().setDelete(false);
            continue;
          }
          
          break;
          
        case insert:

//          // if there is no sync member, then it might need to get created, we should recalc
//          if (gcGrouperSyncMembership.getGrouperSyncMember() == null) {
//            convertMissingEntityEventsToRecalc++;
//            iterator.remove();
//            groupUuidsMemberUuidsForMembershipSyncWithRecalc.add(grouperIncrementalDataItem);
//            continue;
//          }

          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isExistInGrouper()) {
            convertInconsistentEventsActions++;
            provisioningMembershipWrapper.getProvisioningStateMembership().setDelete(true);
            continue;
          }

          break;
          
      }
      
    }
    if (convertInconsistentEventsActions > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertInconsistentEventsActions", convertInconsistentEventsActions);
    }
  }
  
  public void retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc() {
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsInGeneral()) {
      return;
    }
    
    TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest = new TargetDaoRetrieveMembershipsRequest();
    
    // groupAttributes is the same as group memberships
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      // group A has John and Sally in grouper and target
      // remove John and add Ed on the grouper side // group A is not a recalc
      
      Set<ProvisioningGroupWrapper> groupWrappersFromMembershipsWithoutRecalc = new HashSet<ProvisioningGroupWrapper>();
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        
        ProvisioningGroupWrapper groupWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningGroup().getProvisioningGroupWrapper();
        if (!groupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships() && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          groupWrappersFromMembershipsWithoutRecalc.add(groupWrapper);
        }
      }
      
      // we need to send this list to the target dao and ask about certain memberships
      List<Object> requestGrouperTargetGroups = new ArrayList<Object>();
      
      String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
      
      for (ProvisioningGroupWrapper groupWrapperFromMembership: groupWrappersFromMembershipsWithoutRecalc) {
        
        ProvisioningGroup clonedGrouperTargetGroup = groupWrapperFromMembership.getGrouperTargetGroup().clone();
        
        Object attributeValue = clonedGrouperTargetGroup.retrieveAttributeValue(attributeForMemberships);
        
        ProvisioningAttribute grouperAttribute = GrouperUtil.nonNull(clonedGrouperTargetGroup.getAttributes()).get(attributeForMemberships);
        
        if (attributeValue instanceof Collection) {
          
          Iterator valueIterator = ((Collection) attributeValue).iterator();
          
          while (valueIterator.hasNext()) {
            Object value = valueIterator.next();
            ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
            if (grouperAttribute != null && grouperAttribute.getValueToProvisioningMembershipWrapper() != null) {
              provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(value);
            }
            if (provisioningMembershipWrapper != null && !provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
              valueIterator.remove();
            }
           
          }
        }
        // should we check to see if we found any applicable memberships???
        requestGrouperTargetGroups.add(clonedGrouperTargetGroup);
        
      }
      if (GrouperUtil.length(requestGrouperTargetGroups) == 0) {
        return;
      }
//      targetDaoRetrieveMembershipsRequest.setTargetMemberships(requestGrouperTargetGroups);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<ProvisioningGroup> targetGroupsWithMemberships = membershipsResponse.getTargetGroups();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(targetGroupsWithMemberships);
      
      for (ProvisioningGroup provisioningGroupFromTarget: GrouperUtil.nonNull(targetGroupsWithMemberships)) { // because memberships are stored in group attributes, so we receive groups for memberships call
        
        Set<Object> attributeValueSet = (Set<Object>)provisioningGroupFromTarget.retrieveAttributeValueSetForMemberships();
        
        ProvisioningGroupWrapper originalTargetGroupWrapper = provisioningGroupFromTarget.getProvisioningGroupWrapper();
        
        ProvisioningGroup originalTargetGroup = originalTargetGroupWrapper.getTargetProvisioningGroup();
        
        for (Object value: GrouperUtil.nonNull(attributeValueSet)) {
          
          originalTargetGroup.addAttributeValueForMembership(value, null, false);
          
        }
        
      }
      if (GrouperUtil.length(targetGroupsWithMemberships) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc, targetGroupsWithMemberships);
      }

    } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
        
      Set<ProvisioningEntityWrapper> entityWrappersFromMembershipsWithoutRecalc = new HashSet<ProvisioningEntityWrapper>();
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        
        ProvisioningEntityWrapper entityWrapper = provisioningMembershipWrapper.getGrouperProvisioningMembership().getProvisioningEntity().getProvisioningEntityWrapper();
        if (!entityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships() && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          entityWrappersFromMembershipsWithoutRecalc.add(entityWrapper);
        }
      }
      
      // we need to send this list to the target dao and ask about certain memberships
      List<Object> requestGrouperTargetEntities = new ArrayList<Object>();
      
      String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
      
      for (ProvisioningEntityWrapper entityWrapperFromMembership: entityWrappersFromMembershipsWithoutRecalc) {
        
        ProvisioningEntity clonedGrouperTargetEntity = entityWrapperFromMembership.getGrouperTargetEntity().clone();
        
        Object attributeValue = clonedGrouperTargetEntity.retrieveAttributeValue(attributeForMemberships);
        
        ProvisioningAttribute grouperAttribute = GrouperUtil.nonNull(clonedGrouperTargetEntity.getAttributes()).get(attributeForMemberships);
        
        if (attributeValue instanceof Collection) {
          
          Iterator valueIterator = ((Collection) attributeValue).iterator();
          
          while (valueIterator.hasNext()) {
            Object value = valueIterator.next();
            ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(value);
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
              valueIterator.remove();
            }
           
          }
        }
        
        requestGrouperTargetEntities.add(clonedGrouperTargetEntity);
        
      }
      
      if (GrouperUtil.length(requestGrouperTargetEntities) == 0) {
        return;
      }

//      targetDaoRetrieveMembershipsRequest.setTargetMemberships(requestGrouperTargetEntities);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<ProvisioningEntity> targetEntitiesWithMemberships = membershipsResponse.getTargetEntities();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities((List<ProvisioningEntity>)(Object)targetEntitiesWithMemberships);
      
      for (ProvisioningEntity provisioningEntityFromTarget: GrouperUtil.nonNull(targetEntitiesWithMemberships)) { // because memberships are stored in group attributes, so we receive groups for memberships call
        
        Set<Object> attributeValueSet = (Set<Object>)provisioningEntityFromTarget.retrieveAttributeValueSetForMemberships();
        
        ProvisioningEntityWrapper originalTargetEntityWrapper = provisioningEntityFromTarget.getProvisioningEntityWrapper();
        
        ProvisioningEntity originalTargetEntity = originalTargetEntityWrapper.getTargetProvisioningEntity();
        
        for (Object value: GrouperUtil.nonNull(attributeValueSet)) {
          
          originalTargetEntity.addAttributeValueForMembership(value, null, false);
          
        }
        
      }
      if (GrouperUtil.length(targetEntitiesWithMemberships) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc, targetEntitiesWithMemberships);
      }

      
    } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      
      List<ProvisioningMembership> membershipsWithRecalc = new ArrayList<ProvisioningMembership>();
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        
        if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject() && provisioningMembershipWrapper.getGrouperProvisioningMembership() != null) {
          membershipsWithRecalc.add(provisioningMembershipWrapper.getGrouperTargetMembership());
        }
      }
      
      if (GrouperUtil.length(membershipsWithRecalc) == 0) {
        return;
      }

//      targetDaoRetrieveMembershipsRequest.setTargetMemberships((List<Object>)(Object)membershipsWithRecalc);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<ProvisioningMembership> targetMemberships = membershipsResponse.getTargetMemberships();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(targetMemberships);
      
      if (GrouperUtil.length(targetMemberships) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc, targetMemberships);
      }

      
    } else {
      throw new RuntimeException("Not expecting membership type: " + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
    }
    
  }
  
  
  public GrouperProvisioningLists retrieveIncrementalTargetMemberships() {
    GrouperProvisioningLists result = new GrouperProvisioningLists();
    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setTargetDaoRetrieveIncrementalDataRequest(targetDaoRetrieveIncrementalDataRequest);
    boolean needsData = false;

    {
      List<ProvisioningGroup> grouperTargetGroupsRecalcForAllMembershipSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningGroup> grouperTargetGroupsRecalcForSomeMembershipSync = new ArrayList<ProvisioningGroup>();

      for (ProvisioningGroupWrapper provisioningGroupWrapper: this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships()) {
          grouperTargetGroupsRecalcForAllMembershipSync.add(provisioningGroupWrapper.getGrouperTargetGroup());
          continue;
        }
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships()) {
          grouperTargetGroupsRecalcForSomeMembershipSync.add(provisioningGroupWrapper.getGrouperTargetGroup());
            continue;
        }
      }

      if (grouperTargetGroupsRecalcForAllMembershipSync.size() > 0) {
        //collapse these into unique
        grouperTargetGroupsRecalcForAllMembershipSync = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(grouperTargetGroupsRecalcForAllMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupAllMembershipSync(grouperTargetGroupsRecalcForAllMembershipSync);
        needsData = true;
      }
      if (grouperTargetGroupsRecalcForSomeMembershipSync.size() > 0) {
        //collapse these into unique
        grouperTargetGroupsRecalcForSomeMembershipSync = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(grouperTargetGroupsRecalcForSomeMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupSomeMembershipSync(grouperTargetGroupsRecalcForSomeMembershipSync);
        needsData = true;
      }
    }    
    {
      List<ProvisioningEntity> grouperTargetEntitiesRecalcForAllMembershipSync = new ArrayList<ProvisioningEntity>();
      List<ProvisioningEntity> grouperTargetEntitiesRecalcForSomeMembershipSync = new ArrayList<ProvisioningEntity>();

      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectAllMemberships()) {
          grouperTargetEntitiesRecalcForAllMembershipSync.add(provisioningEntityWrapper.getGrouperTargetEntity());
          continue;
        }
        
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectSomeMemberships()) {
          grouperTargetEntitiesRecalcForSomeMembershipSync.add(provisioningEntityWrapper.getGrouperTargetEntity());
          continue;
        }
      }
      if (grouperTargetEntitiesRecalcForAllMembershipSync.size() > 0) {
        grouperTargetEntitiesRecalcForAllMembershipSync = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(grouperTargetEntitiesRecalcForAllMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityAllMembershipSync(grouperTargetEntitiesRecalcForAllMembershipSync);
        needsData = true;
      }
      if (grouperTargetEntitiesRecalcForSomeMembershipSync.size() > 0) {
        grouperTargetEntitiesRecalcForSomeMembershipSync = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(grouperTargetEntitiesRecalcForSomeMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntitySomeMembershipSync(grouperTargetEntitiesRecalcForSomeMembershipSync);
        needsData = true;
      }
    }
    
    {
      List<ProvisioningMembership> provisioningMembershipsRecalcForMembershipSync = new ArrayList<ProvisioningMembership>();
      List<ProvisioningGroup> provisioningGroupSomeMembershipsSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningEntity> provisioningEntitySomeMembershipsSync = new ArrayList<ProvisioningEntity>();
      
      Set<ProvisioningGroup> grouperTargetGroupsRecalcForAllMembershipSyncSet = new HashSet<ProvisioningGroup>(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupAllMembershipSync()));
      Set<ProvisioningEntity> grouperTargetEntitiesRecalcForAllMembershipSyncSet = new HashSet<ProvisioningEntity>(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityAllMembershipSync()));

      Set<ProvisioningGroup> grouperTargetGroupsRecalcForSomeMembershipSyncSet = new HashSet<ProvisioningGroup>(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupSomeMembershipSync()));
      Set<ProvisioningEntity> grouperTargetEntitiesRecalcForSomeMembershipSyncSet = new HashSet<ProvisioningEntity>(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntitySomeMembershipSync()));

      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        ProvisioningGroupWrapper provisioningGroupWrapper = provisioningMembershipWrapper.getProvisioningGroupWrapper();
        if (provisioningGroupWrapper != null && grouperTargetGroupsRecalcForAllMembershipSyncSet.contains(provisioningGroupWrapper.getGrouperTargetGroup())) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
        }
        if (provisioningGroupWrapper != null && grouperTargetGroupsRecalcForSomeMembershipSyncSet.contains(provisioningGroupWrapper.getGrouperTargetGroup())) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
        }

        ProvisioningEntityWrapper provisioningEntityWrapper = provisioningMembershipWrapper.getProvisioningEntityWrapper();
        if (provisioningEntityWrapper != null && grouperTargetEntitiesRecalcForAllMembershipSyncSet.contains(provisioningEntityWrapper.getGrouperTargetEntity())) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
        }
        if (provisioningEntityWrapper != null && grouperTargetEntitiesRecalcForSomeMembershipSyncSet.contains(provisioningEntityWrapper.getGrouperTargetEntity())) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
        }
      }

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != null) {
        switch(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
          
          case membershipObjects:
  
            for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
              if (provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
                ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
                provisioningMembershipWrapper.getProvisioningStateMembership().setSelectResultProcessed(true);
                provisioningMembershipsRecalcForMembershipSync.add(grouperTargetMembership);
              }
            }
  
            break;
  
          case entityAttributes:
            {
              Set<String> memberIdsAdded = new HashSet<String>();
              for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
                ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
    
                if (provisioningEntityWrapper != null && !provisioningEntityWrapper.getProvisioningStateEntity().isSelectSomeMemberships()) {
                  continue;
                }
                String memberId = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getId();
                ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperTargetEntity();
                if (memberId != null && !memberIdsAdded.contains(memberId) && grouperTargetEntity != null) {
                  provisioningEntitySomeMembershipsSync.add(grouperTargetEntity);
                  provisioningEntityWrapper.getProvisioningStateEntity().setSelectSomeMembershipsResultProcessed(true);
                  memberIdsAdded.add(memberId);
                }
              }
            }
            
            break;
            
          case groupAttributes:
            {
              Set<String> groupIdsAdded = new HashSet<String>();
              for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
                ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
    
                if (provisioningGroupWrapper != null && !provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships()) {
                  continue;
                }
                String groupId = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getId();
                ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroup();
                if (groupId != null && !groupIdsAdded.contains(groupId) && grouperTargetGroup != null) {
                  provisioningGroupSomeMembershipsSync.add(grouperTargetGroup);
                  provisioningGroupWrapper.getProvisioningStateGroup().setSelectSomeMembershipsResultProcessed(true);
                  groupIdsAdded.add(groupId);
                }
              }
            }
            
            break;
            
            default: 
              throw new RuntimeException("Not expecting GrouperProvisioningBehaviorMembershipType: "
                  + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
        }
      }
      if (provisioningMembershipsRecalcForMembershipSync.size() > 0) {
        provisioningMembershipsRecalcForMembershipSync = new ArrayList<ProvisioningMembership>(new HashSet<ProvisioningMembership>(provisioningMembershipsRecalcForMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetMembershipObjectsForMembershipSync(provisioningMembershipsRecalcForMembershipSync);
        needsData = true;
      }
      if (provisioningGroupSomeMembershipsSync.size() > 0) {
        provisioningGroupSomeMembershipsSync = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(provisioningGroupSomeMembershipsSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupSomeMembershipSync(provisioningGroupSomeMembershipsSync);
        needsData = true;
      }
      if (provisioningEntitySomeMembershipsSync.size() > 0) {
        provisioningEntitySomeMembershipsSync = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(provisioningEntitySomeMembershipsSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntitySomeMembershipSync(provisioningEntitySomeMembershipsSync);
        needsData = true;
      }
      
    }
    if (!needsData) {
      return null;
    }
    
    TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse 
      = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveIncrementalData(targetDaoRetrieveIncrementalDataRequest);
    
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups()) > 0) {
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();
      result.setProvisioningGroups(provisioningGroups);
      provisioningGroups.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities()) > 0) {
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      result.setProvisioningEntities(provisioningEntities);
      provisioningEntities.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) > 0) {
      
      List<ProvisioningMembership> provisioningMemberships = result.getProvisioningMemberships();
      
      if (provisioningMemberships == null) {
        provisioningMemberships = new ArrayList<ProvisioningMembership>();
        result.setProvisioningMemberships(provisioningMemberships);
      }
      
      provisioningMemberships.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships());
      
    }
    return result;
  }
  
  public GrouperProvisioningLists retrieveIncrementalTargetGroupsAndEntities() {
    
    GrouperProvisioningLists result = new GrouperProvisioningLists();
    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setTargetDaoRetrieveIncrementalDataRequest(targetDaoRetrieveIncrementalDataRequest);
    boolean needsData = false;
    
    {
      List<ProvisioningGroup> grouperTargetGroupsRecalcForGroupOnly = new ArrayList<ProvisioningGroup>();

      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelect() && 
            provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed() == false) {
          grouperTargetGroupsRecalcForGroupOnly.add(provisioningGroupWrapper.getGrouperTargetGroup());
          provisioningGroupWrapper.getProvisioningStateGroup().setSelectResultProcessed(true);
          continue;
        }
      }
      if (grouperTargetGroupsRecalcForGroupOnly.size() > 0) {
        grouperTargetGroupsRecalcForGroupOnly = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(grouperTargetGroupsRecalcForGroupOnly));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsRecalcForGroupOnly);
        needsData = true;
      }
    }    
    {
      List<ProvisioningEntity> grouperTargetEntitiesRecalcForEntityOnly = new ArrayList<ProvisioningEntity>();

      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
          if (provisioningEntityWrapper.getProvisioningStateEntity().isSelect() && 
              provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed() == false) {
            grouperTargetEntitiesRecalcForEntityOnly.add(provisioningEntityWrapper.getGrouperTargetEntity());
            provisioningEntityWrapper.getProvisioningStateEntity().setSelectResultProcessed(true);
            continue;
          }
      }
      
      if (grouperTargetEntitiesRecalcForEntityOnly.size() > 0) {
        grouperTargetEntitiesRecalcForEntityOnly = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(grouperTargetEntitiesRecalcForEntityOnly));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesRecalcForEntityOnly);
        needsData = true;
      }
    }
    
    if (!needsData) {
      return null;
    }
    
    TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse 
      = this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().retrieveIncrementalData(targetDaoRetrieveIncrementalDataRequest);
    
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups()) > 0) {
      List<ProvisioningGroup> provisioningGroups = new ArrayList<ProvisioningGroup>();
      result.setProvisioningGroups(provisioningGroups);
      provisioningGroups.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities()) > 0) {
      List<ProvisioningEntity> provisioningEntities = new ArrayList<ProvisioningEntity>();
      result.setProvisioningEntities(provisioningEntities);
      provisioningEntities.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities());
    }
    return result;
  }
  
  public void determineGroupsToSelect() {
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (provisioningGroupWrapper.getGrouperTargetGroup() != null) {
        
        if ( (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject() ||
            provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships())
            && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroups()) {
          provisioningGroupWrapper.getProvisioningStateGroup().setSelect(true);
        }
        
        if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()
            && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForGroup()) {
          provisioningGroupWrapper.getProvisioningStateGroup().setSelectAllMemberships(true);
        }
      }
    }
    
  }
  
  public void determineEntitiesToSelect() {
   
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      if (provisioningEntityWrapper.getGrouperTargetEntity() != null) {
        // isIncrementalSyncMemberships - some memberships attached to this entity are recalc
        // isRecalcEntityMemberships - all memberships attached to this entity are recalc
        if ( (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject() ||
            provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships())
            && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectEntities()) {
          provisioningEntityWrapper.getProvisioningStateEntity().setSelect(true);
        }
        
        if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()
            && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForEntity()) {
          provisioningEntityWrapper.getProvisioningStateEntity().setSelectAllMemberships(true);
        }
        
      }
    }
  }
  
  public void determineMembershipsToSelect() {
    
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsSomeForGroup()) {
      int groupsWithSelectSomeMemberships = 0;
      int selectSomeMemberships = 0;
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
          ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();

          if (provisioningGroupWrapper == null) {
            continue;
          }
          if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships()) {
            continue;
          }
          if (!provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setSelectSomeMemberships(true);
            groupsWithSelectSomeMemberships++;
          }
          
        }
      }
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
        ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();

        if (provisioningGroupWrapper == null) {
          continue;
        }
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships()) {
          continue;
        }
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectSomeMemberships()) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelect(true);
          selectSomeMemberships++;
        }
      }

      
      if (groupsWithSelectSomeMemberships > 0) {
        this.getGrouperProvisioner().getDebugMap().put("groupsWithSelectSomeMemberships", groupsWithSelectSomeMemberships);
      }
      if (selectSomeMemberships > 0) {
        this.getGrouperProvisioner().getDebugMap().put("selectSomeMemberships", selectSomeMemberships);
      }
    }

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsSomeForEntity()) {
      int entitiesWithSelectSomeMemberships = 0;
      int selectSomeMemberships = 0;
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
          ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();

          if (provisioningEntityWrapper == null) {
            continue;
          }
          if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectAllMemberships()) {
            continue;
          }
          if (!provisioningEntityWrapper.getProvisioningStateEntity().isSelectSomeMemberships()) {
            provisioningEntityWrapper.getProvisioningStateEntity().setSelectSomeMemberships(true);
            entitiesWithSelectSomeMemberships++;
          }
          
        }
      }
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
        ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
        ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();

        if (provisioningEntityWrapper == null) {
          continue;
        }
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectAllMemberships()) {
          continue;
        }
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectSomeMemberships()) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelect(true);
          selectSomeMemberships++;
        }
      }

      
      if (entitiesWithSelectSomeMemberships > 0) {
        this.getGrouperProvisioner().getDebugMap().put("entitiesWithSelectSomeMemberships", entitiesWithSelectSomeMemberships);
      }
      if (selectSomeMemberships > 0) {
        this.getGrouperProvisioner().getDebugMap().put("selectSomeMemberships", selectSomeMemberships);
      }
    }

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForMembership()) {
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          continue;
        }
        
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
        ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
        ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
        ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
  
        // by the time, it reaches here, determineGroupsToSelect might have set selectSomeMemberships to true and
        // that's why, we skip them here
        if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isSelectAllMemberships()) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelect(false);
          continue;
        }
        // by the time, it reaches here, determineEntitiesToSelect might have set selectSomeMemberships to true and
        // that's why, we skip them here
        if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isSelectAllMemberships()) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelect(false);
          continue;
        } 
  
        // when provisioning type is membership objects and it's recalc, 
        // then only the following code is going to execute
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForMembership()) {
          provisioningMembershipWrapper.getProvisioningStateMembership().setSelect(true);
        }
      }
    }
  }

  /**
   * dont double request groups/entities
   * @param targetDaoRetrieveIncrementalDataRequest
   */
  public void removeSomeMembershipRequestsAreInTheOnlyRequestsAlso(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupAllMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupAllMembershipSync());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityAllMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityAllMembershipSync());
    }
    
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupSomeMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupSomeMembershipSync());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntitySomeMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntitySomeMembershipSync());
    }
    
  }

  /**
     * make sure that all the target requests that include memberships also are requesting the group or entities in the "only" lists.
   * @param targetDaoRetrieveIncrementalDataRequest
   */
  public void ensureAllMembershipRequestsAreInTheOnlyRequestsAlso(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
      
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupAllMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      Set<ProvisioningGroup> targetGroupsForGroupOnlySet = new HashSet<ProvisioningGroup>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()));
      // indexing the groups that are already there
      for (ProvisioningGroup targetGroupForGroupOnly: targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) {
        targetGroupsForGroupOnlySet.add(targetGroupForGroupOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningGroup targetGroupForGroupMembershipSync: targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupAllMembershipSync()) {
        if (!targetGroupsForGroupOnlySet.contains(targetGroupForGroupMembershipSync)) {
          targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().add(targetGroupForGroupMembershipSync);
          targetGroupsForGroupOnlySet.add(targetGroupForGroupMembershipSync);
        }
      }
    }

    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityAllMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      Set<ProvisioningEntity> targetEntitiesForEntityOnlySet = new HashSet<ProvisioningEntity>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()));
      // indexing the entities that are already there
      for (ProvisioningEntity targetEntityForEntityOnly: targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()) {
        targetEntitiesForEntityOnlySet.add(targetEntityForEntityOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningEntity targetEntityForEntityMembershipSync: targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityAllMembershipSync()) {
        if (!targetEntitiesForEntityOnlySet.contains(targetEntityForEntityMembershipSync)) {
          targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().add(targetEntityForEntityMembershipSync);
          targetEntitiesForEntityOnlySet.add(targetEntityForEntityMembershipSync);
        }
      }
    }

  }


  /**
   * If a group or entity has any incremental recalc memberships inside then set all the
   * membership events for that group or entity to be recalc
   * We do this so that we don't have to clone the group or entity to make the specific target query by example
   */
  public void convertMembershipsToRecalc() {
    
    int membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity = 0;
    // ######### Mark memberships retrieved by group or entity as recalc
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersforMembershipSync = new HashSet<ProvisioningGroupWrapper>();
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
        provisioningGroupWrappersforMembershipSync.add(provisioningGroupWrapper);
      }
    }
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersforMembershipSync = new HashSet<ProvisioningEntityWrapper>();
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
        provisioningEntityWrappersforMembershipSync.add(provisioningEntityWrapper);
      }
    }
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        continue;
      }
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isRecalculateAllOperations()
          || provisioningGroupWrappersforMembershipSync.contains(provisioningMembershipWrapper.getProvisioningGroupWrapper())
          || provisioningEntityWrappersforMembershipSync.contains(provisioningMembershipWrapper.getProvisioningEntityWrapper())) {
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForRecalc());
        membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity++;
      }
    }
    if (membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertMembershipsToRecalc", membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity);
    }
  }

  /**
   * Convert group to recalc if it's an insert or delete
   * 
   * Convert group to recalc  there's a group link that needs data. 
   * e.g. we only know the group name and need to fetch the uuid from the target
   * 
   * marks group as having recalc memberships
   */
  public void convertGroupsToRecalc(boolean includeMemberships) {
    
    int convertGroupsToRecalc = 0;
    
    boolean selectMembershipsForGroup = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForGroup();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {

      boolean changeConvert = false;
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()
          && provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
        continue;
      }
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null || (provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget())) {
        // we need to retrieve or create this, its probably already a recalc but...
        if (!provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
          changeConvert = true;
        }
        if (includeMemberships && selectMembershipsForGroup 
            && !provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupMembershipsForRecalc());
          changeConvert = true;
        }
      }
      if (!provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()
          && this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isRecalculateAllOperations()) {
        provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
        changeConvert = true;
        
      }
      if (changeConvert) {
        convertGroupsToRecalc++;
      }
    }
    // we need to retrieve non recalc groups for entity attribute provisioning, if there is a group link and no data
    // if there's cache, only pull the ones that we don't know about
    // if not using cache, pull all of them
    List<ProvisioningGroup> grouperTargetGroupsToRetrieveForLinks = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic()
        .retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks(
            this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
    for (ProvisioningGroup grouperTargetGroupToRetrieveForLinks : grouperTargetGroupsToRetrieveForLinks) {
      if (grouperTargetGroupToRetrieveForLinks.getProvisioningGroupWrapper() != null) {
        grouperTargetGroupToRetrieveForLinks.getProvisioningGroupWrapper().getProvisioningStateGroup().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc());
        convertGroupsToRecalc++;
      }
    }
              
    if (convertGroupsToRecalc > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "convertGroupsToRecalc", convertGroupsToRecalc);
    }
    
  }
  
  /**
   * Convert entity to recalc if it's an insert or delete
   * 
   * Convert entity to recalc when there's an entity link that needs data. 
   * e.g. we only know the entity name and need to fetch the uuid from the target
   * 
   * marks entity as having recalc memberships
   */
  public void convertEntitiesToRecalc(boolean includeMemberships) {
    
    int convertEntitiesToRecalc = 0;
    boolean selectMembershipsForEntity = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAllForEntity();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      
      boolean changeConvert = false;
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject() &&
          provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships() ) {
        continue;
      }
      if (provisioningEntityWrapper.getGcGrouperSyncMember() == null || (provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget() == null || !provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget())) {
        // we need to retrieve or create this, its probably already a recalc but...
        if (!provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());
          changeConvert = true;
        }
        if (includeMemberships && selectMembershipsForEntity 
            && !provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntityMembershipsForRecalc());
          changeConvert = true;
        }
      }
      if (!provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()
          && this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isRecalculateAllOperations()) {
        provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());
        changeConvert = true;
        
      }
      if (changeConvert) {
        convertEntitiesToRecalc++;
      }
    }
    
    // we need to retrieve non recalc entities for entity attribute provisioning, if there is a entity link and no data
    List<ProvisioningEntity> grouperTargetEntitiesToRetrieveForLinks = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic()
        .retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks(
            this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
    for (ProvisioningEntity grouperTargetEntityToRetrieveForLinks : grouperTargetEntitiesToRetrieveForLinks) {
      if (grouperTargetEntityToRetrieveForLinks.getProvisioningEntityWrapper().getProvisioningStateEntity().isRecalcObject()) {
        continue;
      }
      if (grouperTargetEntityToRetrieveForLinks.getProvisioningEntityWrapper() != null) {
        grouperTargetEntityToRetrieveForLinks.getProvisioningEntityWrapper().getProvisioningStateEntity().setRecalcObject(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesForRecalc());
        convertEntitiesToRecalc++;
      }
    }
    
    
    if (convertEntitiesToRecalc > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "convertEntitiesToRecalc", convertEntitiesToRecalc);
    }
    
  }

  /**
   * based on group events, determine which action to perform on group (if not recalc already)
   */
  public void calculateGroupAction() {
    
    int calculateGroupActionInsert = 0;
    int calculateGroupActionUpdate = 0;
    int calculateGroupActionDelete = 0;
    int calculateGroupActionReset = 0;
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null) {
        continue;
      }

      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()
          && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectGroupsForRecalc()) {
        continue;
      } else if ((provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget()) && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()) {
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
        provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
        calculateGroupActionInsert++;
      } else if ((provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget()) && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()
          && provisioningGroupWrapper.getProvisioningStateGroup().isUpdate()) {
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
        calculateGroupActionUpdate++;
      } else if ((provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget()) && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()
          && provisioningGroupWrapper.getProvisioningStateGroup().isDelete() && provisioningGroupWrapper.getProvisioningStateGroup().isCreate()) {
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
        calculateGroupActionUpdate++;
      } else if ((provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().getInTarget()) && !provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()) {
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
        calculateGroupActionDelete++;
      } else {
        provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(false);
        provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
        calculateGroupActionReset++;
      }
      
    }
    if (calculateGroupActionInsert > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "calculateGroupActionInsert", calculateGroupActionInsert);
    }
    if (calculateGroupActionUpdate > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "calculateGroupActionUpdate", calculateGroupActionUpdate);
    }
    if (calculateGroupActionDelete > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "calculateGroupActionDelete", calculateGroupActionDelete);
    }
    if (calculateGroupActionReset > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "calculateGroupActionReset", calculateGroupActionReset);
    }

  }

}
