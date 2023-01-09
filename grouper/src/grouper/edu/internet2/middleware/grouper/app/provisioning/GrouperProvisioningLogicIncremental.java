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
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
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
      
      if (gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.isInTarget()) {
        
        validGroupIds.add(gcGrouperSyncGroup.getGroupId());
        continue; 
      } 
      
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
        iterator.remove();
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
      
      long millisSince1970 = provisioningEntityWrapper.getProvisioningStateEntity().getMillisSince1970();
      if (millisSince1970 == -1) {
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
              
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, true, true, provisioningMessage.getMillisSince1970(), null);

              messageCountForProvisioner++;
            }
            
          }
          
          if (GrouperUtil.length(provisioningMessage.getMemberIdsForSync()) > 0) {
            for (String memberId : provisioningMessage.getMemberIdsForSync()) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(memberId, true, true, provisioningMessage.getMillisSince1970(), null);
              messageCountForProvisioner++;
            }
            
          }
          
          if (GrouperUtil.length(provisioningMessage.getMembershipsForSync()) > 0) {
            for (ProvisioningMembershipMessage provisioningMembershipMessage : provisioningMessage.getMembershipsForSync()) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(provisioningMembershipMessage.getGroupId(), provisioningMembershipMessage.getMemberId(), true, provisioningMessage.getMillisSince1970(), null);
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
    Map<String, GrouperProvisioningObjectAttributes> allAncestorProvisioningGroupAttributes = new HashMap<String, GrouperProvisioningObjectAttributes>();
    String grouperObjectTypeName = GrouperObjectTypesSettings.objectTypesStemName() + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
    
    Set<String> queriedPITAttributeAssignIds = new HashSet<String>();
    
    List<String> memberIdsToFetchProvisioningAttributesFor = new ArrayList<String>();
    
    boolean shouldCheckForMemberSyncsToDelete = false;
    
    
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
  
            if (stemId != null) {
              Map<String, GrouperProvisioningObjectAttributes> ancestorProvisioningFolderAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveAncestorProvisioningAttributesByFolder(stemId);
              Map<String, GrouperProvisioningObjectAttributes> childProvisioningFolderAttributes = this.grouperProvisioner.retrieveGrouperDao().retrieveChildProvisioningFolderAttributesByFolder(stemId);
              grouperProvisioningGroupAttributesToProcess.putAll(this.grouperProvisioner.retrieveGrouperDao().retrieveChildProvisioningGroupAttributesByFolder(stemId));
              allAncestorProvisioningGroupAttributes.putAll(childProvisioningFolderAttributes);
              allAncestorProvisioningGroupAttributes.putAll(ancestorProvisioningFolderAttributes);
            } else {
              String groupId = grouperProvisioner.retrieveGrouperDao().getGroupIdIfDirectGroupAssignmentByPITMarkerAttributeAssignId(pitAttributeAssign.getOwnerAttributeAssignId());
              Group group = groupId != null ? GrouperDAOFactory.getFactory().getGroup().findByUuid(groupId, false) : null;

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
    
    ProvisioningSyncIntegration.fullSyncMembers(this.grouperProvisioner, provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(), 
        new HashSet<GcGrouperSyncMember>(grouperSyncMemberIdToSyncMember.values()),
        grouperProvisioningObjectAttributesForMembers);
    
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

      ProvisioningSyncIntegration.fullSyncGroups(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
          new HashSet<GcGrouperSyncGroup>(grouperSyncGroupIdToSyncGroup.values()),
          calculatedProvisioningAttributes);
      
      
      Set<String> groupIdsToTriggerSync = new HashSet<String>();
      for (GcGrouperSyncGroup gcGrouperSyncGroup : this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().retrieveUpdatedCacheSyncGroups()) {
        groupIdsToTriggerSync.add(gcGrouperSyncGroup.getGroupId());
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();

      for (String groupId : groupIdsToTriggerSync) {
        // not using this anymore, used to be used to see if a group can be updated without syncing memberships
        // groupIdsThatWereUpdated
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, true, true, System.currentTimeMillis(), null);
      }      
    }
  }

  public void incrementalCheckChangeLog() {
    
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
      
      switch (esbEventType) {
        
//        case ATTRIBUTE_ASSIGN_ADD:
//        case ATTRIBUTE_ASSIGN_DELETE:
//        case ATTRIBUTE_ASSIGN_VALUE_ADD:
//        case ATTRIBUTE_ASSIGN_VALUE_DELETE:
//          
//          if (StringUtils.equals(doProvisionAttributeName, esbEvent.getAttributeDefNameName()) 
//              && !StringUtils.isBlank(esbEvent.())) {
//            grouperIncrementalDataToProcess.getGroupUuidsForGroupOnly().add(new GrouperIncrementalDataItem(esbEvent.getGroupId(), createdOnMillis));
//            grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync().add(new GrouperIncrementalDataItem(esbEvent.getGroupId(), createdOnMillis));
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
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(esbEvent.getGroupId(), false, esbEventType != EsbEventType.GROUP_UPDATE, createdOnMillis, grouperIncrementalDataAction);
            
          break;
        case MEMBERSHIP_ADD:
        case MEMBERSHIP_DELETE:
        case MEMBERSHIP_UPDATE:

          // skip if wrong source
          if (!StringUtils.isBlank(esbEvent.getSourceId()) && GrouperUtil.length(sourceIdsToProvision) > 0 && !sourceIdsToProvision.contains(esbEvent.getSourceId())) {
            continue;
          }
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
        if (recalcOnly) {
          // dont worry about actions on recalc
          grouperIncrementalDataAction = null;
        } else if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.PRIVILEGE_ADD) {
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
                    
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(esbEvent.getMemberId(), true, true, createdOnMillis, null);
          
        } else { 

          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(esbEvent.getGroupId(), esbEvent.getMemberId(), false, createdOnMillis, grouperIncrementalDataAction);
        }
        
        changeLogCount++;
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
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(true);
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(true);
          
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
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(true);
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(true);

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
          provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
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
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, true, true, null, null);
        addErrorsToQueue++;
      }
    }
    
    Set<String> memberIdsSet = null;
    {
      List<String> memberIds = gcGrouperSync.getGcGrouperSyncMemberDao().retrieveMemberIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      memberIdsSet = new HashSet<String>(GrouperUtil.nonNull(memberIds));
      for (String memberId : GrouperUtil.nonNull(memberIds)) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(memberId, true, true, null, null);
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
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalMembership(groupUuid, memberUuid, true, null, null);
        addErrorsToQueue++;
      }
    }

    if (addErrorsToQueue > 0) {
      this.getGrouperProvisioner().getDebugMap().put("addErrorsToQueue", addErrorsToQueue);
    }
  }


  /**
   * filter events that happened after the last full sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
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
      
      if (recalcGroupMembershipIds.contains((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0))
          || recalcEntityMembershipIds.contains((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1))) {
        iterator.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
        filterNonRecalcActionsCapturedByRecalc++;
      }
    }
    
    if (filterNonRecalcActionsCapturedByRecalc > 0) {
      Integer filterNonRecalcActionsCapturedByRecalcInLog = GrouperUtil.intValue(this.getGrouperProvisioner().getDebugMap().get("filterNonRecalcActionsCapturedByRecalc"), 0);
      this.getGrouperProvisioner().getDebugMap().put("filterNonRecalcActionsCapturedByRecalc", filterNonRecalcActionsCapturedByRecalcInLog + filterNonRecalcActionsCapturedByRecalc);
    }
  }


//  /**
//   * make sure the list of groups/entities (without memberships) includes all the groups/entities for memberships
//   * @param esbEventContainers
//   * @param gcGrouperSync
//   */
//  public void organizeRecalcAndNonRecalcRequestsGroups() {
//
//    int[] organizeRecalcAndNonRecalcRequests = new int[] {0};
//
//    GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
//    
//    organizeRecalcAndNonRecalcRequestsGroups(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc(), organizeRecalcAndNonRecalcRequests, false);
//
//    organizeRecalcAndNonRecalcRequestsGroups(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc(), organizeRecalcAndNonRecalcRequests, true);
//
//    if (organizeRecalcAndNonRecalcRequests[0] > 0) {
//      this.getGrouperProvisioner().getDebugMap().put("organizeRecalcAndNonRecalcRequestsGroups", organizeRecalcAndNonRecalcRequests[0]);
//    }
//
//  }
//
//  /**
//   * make sure the list of groups/entities (without memberships) includes all the groups/entities for memberships
//   * @param grouperIncrementalDataToProcess
//   * @param organizeRecalcAndNonRecalcRequests
//   */
//  public void organizeRecalcAndNonRecalcRequestsGroups(
//      GrouperIncrementalDataToProcess grouperIncrementalDataToProcess,
//      int[] organizeRecalcAndNonRecalcRequests, boolean recalc) {
//    
//    Set<GrouperIncrementalDataItem> groupUuidsForGroupOnly = grouperIncrementalDataToProcess.getGroupUuidsForGroupOnly();
//    Set<GrouperIncrementalDataItem> groupUuidsForGroupMembershipSync = grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync();
//    Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsFieldIdsForMembershipSync = grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsFieldIdsForMembershipSync();
//
//    int origSize = GrouperUtil.length(groupUuidsForGroupOnly)
//      + GrouperUtil.length(groupUuidsForGroupMembershipSync)
//      + GrouperUtil.length(groupUuidsMemberUuidsFieldIdsForMembershipSync);
//
//    Set<String> groupUuidsForGroupOnlyString = new HashSet<String>();
//    
//    // add existing, though there shouldnt really be any here yet
//    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(groupUuidsForGroupOnly)) {
//      groupUuidsForGroupOnlyString.add((String)grouperIncrementalDataItem.getItem());
//    }
//
//    // check for new
//    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(groupUuidsForGroupMembershipSync)) {
//      String groupId = (String)grouperIncrementalDataItem.getItem();
//      if (!groupUuidsForGroupOnlyString.contains(groupId)) {
//        groupUuidsForGroupOnlyString.add(groupId);
//        groupUuidsForGroupOnly.add(new GrouperIncrementalDataItem(groupId, null));
//      }
//    }
//    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(groupUuidsMemberUuidsFieldIdsForMembershipSync)) {
//      {
//        String groupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
//        if (!groupUuidsForGroupOnlyString.contains(groupId)) {
//          groupUuidsForGroupOnlyString.add(groupId);
//          groupUuidsForGroupOnly.add(new GrouperIncrementalDataItem(groupId, null));
//        }
//      }
//    }
//    
//    int newSize = GrouperUtil.length(groupUuidsForGroupOnly)
//      + GrouperUtil.length(groupUuidsForGroupMembershipSync)
//      + GrouperUtil.length(groupUuidsMemberUuidsFieldIdsForMembershipSync);
//    
//    organizeRecalcAndNonRecalcRequests[0] += newSize-origSize;
//  }


  /**
   * make sure the list of groups/entities (without memberships) includes all the groups/entities for memberships
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void organizeRecalcAndNonRecalcRequestsEntities() {

    int organizeRecalcAndNonRecalcRequests = 0;

    // 1. for every recalc of entity with memberships, it should recalc the entity
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships() && !provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
        provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(true);
        organizeRecalcAndNonRecalcRequests++;
      }
      
    }

    // 2. for every recalc of membership, it should recalc the entity only
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      
      if (provisioningMembershipWrapper.getGroupIdMemberId() == null) {
        continue;
      }
      String memberId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1);
      
      ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
      
      if (provisioningEntityWrapper == null) {
        // this is a recalc if the membership is a recalc
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalEntity(memberId, provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject(), false, null, null);
        organizeRecalcAndNonRecalcRequests++;
        continue;
      }

      if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject() ) {
        continue;
      }

      if (!provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
        
        // this exists, but the membership is recalc and the entity is not
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
        organizeRecalcAndNonRecalcRequests++;
      }
      
    }
  
    if (organizeRecalcAndNonRecalcRequests > 0) {
      this.getGrouperProvisioner().getDebugMap().put("organizeRecalcAndNonRecalcRequestsEntities", organizeRecalcAndNonRecalcRequests);
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
    
    if (!convertAllRecalcMembershipChangesToGroupSync && !this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsForGroup()) {
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

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
      groupsWithRecalcMembershipsThatCannotSelectMemberships = groupIdToHasRecalcMembership.size();
    }
    
    // lets see whats over the threshold
    for (String groupId : groupUuidToMembershipCount.keySet()) {
      
      int membershipCount = groupUuidToMembershipCount.get(groupId);
      
      if (membershipCount >= membershipsConvertToGroupSyncThreshold || (groupIdToHasRecalcMembership.containsKey(groupId) && convertAllRecalcMembershipChangesToGroupSync)) {
       
        convertToGroupSyncGroups++;        
        
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addIncrementalGroup(groupId, true, true, null, null);

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
    if (convertToFullSyncScore >= scoreConvertToFullSyncThreshold) {
      this.getGrouperProvisioner().getDebugMap().put("convertToFullSync", true);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setFullSync(true);
    }
  }


  /**
   * filter events that happened after the last group sync
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
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
      }
      
    }
    
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringGroupSync", recalcEventsDuringGroupSync);
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
      
      ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      switch (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction()) {
        case delete:
          
          if (provisioningMembership == null && gcGrouperSyncMembership != null && !gcGrouperSyncMembership.isInTarget()) {
            filterUnneededMemberships++;
            iterator.remove();
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
            continue;
          }
          
          break;
          
        case insert:

          if (provisioningMembership != null && gcGrouperSyncMembership != null && gcGrouperSyncMembership.isInTarget()) {
            filterUnneededMemberships++;
            iterator.remove();
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
            continue;
          }

          break;
          
      }
      
    }
  
    if (filterUnneededMemberships > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterUnneededMemberships", filterUnneededMemberships);
    }

  }

  public void convertInconsistentEventsToRecalc() {
    int convertInconsistentEventsToRecalc = 0;
    int convertMissingEntityEventsToRecalc = 0;

    Iterator<ProvisioningMembershipWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData()
        .getProvisioningMembershipWrappers().iterator();
    
    while (iterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = iterator.next();

      ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()
          || provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction() == null) {
        continue;
      }
      
      switch (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction()) {
        case delete:
          
          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isDelete() && provisioningMembership != null) {
            convertInconsistentEventsToRecalc++;
            provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
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

          if (provisioningMembershipWrapper.getProvisioningStateMembership().isDelete() || provisioningMembership == null) {
            convertInconsistentEventsToRecalc++;
            provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
            continue;
          }

          break;
          
      }
      
    }
    if (convertInconsistentEventsToRecalc > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertInconsistentEventsToRecalc", convertInconsistentEventsToRecalc);
    }
    if (convertMissingEntityEventsToRecalc > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertMissingEntityEventsToRecalc", convertMissingEntityEventsToRecalc);
    }
  }


  public void copyIncrementalStateToWrappers() {
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
        continue;
      }
      
      switch (provisioningGroupWrapper.getProvisioningStateGroup().getGrouperIncrementalDataAction()) {
        case insert:
          if (!provisioningGroupWrapper.getProvisioningStateGroup().isUpdate() && !provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
          } else if (provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
            provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
          } 
          break;
        case update:
          if (!provisioningGroupWrapper.getProvisioningStateGroup().isCreate() && !provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
          } else if (provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setDelete(false);
            provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(true);
          } 
          break;
        case delete:
          if (!provisioningGroupWrapper.getProvisioningStateGroup().isCreate()) {
            provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
            provisioningGroupWrapper.getProvisioningStateGroup().setUpdate(false);
          } else {
            provisioningGroupWrapper.getProvisioningStateGroup().setCreate(false);
          } 
          break;
        default:
          throw new RuntimeException("Invalid");
      }
      
    }
  }

  
  public void retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc() {
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships()) {
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
      targetDaoRetrieveMembershipsRequest.setTargetMemberships(requestGrouperTargetGroups);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<Object> targetGroupsWithMemberships = membershipsResponse.getTargetMemberships();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups((List<ProvisioningGroup>)(Object)targetGroupsWithMemberships);
      
      for (Object provisioningGroupObject: GrouperUtil.nonNull(targetGroupsWithMemberships)) { // because memberships are stored in group attributes, so we receive groups for memberships call
        
        ProvisioningGroup provisioningGroupFromTarget = (ProvisioningGroup) provisioningGroupObject;
        
        Set<Object> attributeValueSet = (Set<Object>)provisioningGroupFromTarget.retrieveAttributeValueSet(attributeForMemberships);
        
        ProvisioningGroupWrapper originalTargetGroupWrapper = provisioningGroupFromTarget.getProvisioningGroupWrapper();
        
        ProvisioningGroup originalTargetGroup = originalTargetGroupWrapper.getTargetProvisioningGroup();
        
        for (Object value: GrouperUtil.nonNull(attributeValueSet)) {
          
          originalTargetGroup.addAttributeValue(attributeForMemberships, value);
          
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

      targetDaoRetrieveMembershipsRequest.setTargetMemberships(requestGrouperTargetEntities);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<Object> targetEntitiesWithMemberships = membershipsResponse.getTargetMemberships();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities((List<ProvisioningEntity>)(Object)targetEntitiesWithMemberships);
      
      for (Object provisioningEntityObject: GrouperUtil.nonNull(targetEntitiesWithMemberships)) { // because memberships are stored in group attributes, so we receive groups for memberships call
        
        ProvisioningEntity provisioningEntityFromTarget = (ProvisioningEntity) provisioningEntityObject;
        
        Set<Object> attributeValueSet = (Set<Object>)provisioningEntityFromTarget.retrieveAttributeValueSet(attributeForMemberships);
        
        ProvisioningEntityWrapper originalTargetEntityWrapper = provisioningEntityFromTarget.getProvisioningEntityWrapper();
        
        ProvisioningEntity originalTargetEntity = originalTargetEntityWrapper.getTargetProvisioningEntity();
        
        for (Object value: GrouperUtil.nonNull(attributeValueSet)) {
          
          originalTargetEntity.addAttributeValue(attributeForMemberships, value);
          
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

      targetDaoRetrieveMembershipsRequest.setTargetMemberships((List<Object>)(Object)membershipsWithRecalc);
      
      TargetDaoRetrieveMembershipsResponse membershipsResponse = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().retrieveMemberships(targetDaoRetrieveMembershipsRequest);

      List<Object> targetMemberships = membershipsResponse.getTargetMemberships();
      
      this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships((List<ProvisioningMembership>)(Object)targetMemberships);
      
      if (GrouperUtil.length(targetMemberships) > 0) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveTargetIncrementalMembershipsWithRecalcWhereContainerIsNotRecalc, targetMemberships);
      }

      
    } else {
      throw new RuntimeException("Not expecting membership type: " + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
    }
    
  }
  
  public GrouperProvisioningLists retrieveIncrementalTargetData() {
    GrouperProvisioningLists result = new GrouperProvisioningLists();
    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setTargetDaoRetrieveIncrementalDataRequest(targetDaoRetrieveIncrementalDataRequest);
    boolean needsData = false;
    
    {
      List<ProvisioningGroup> grouperTargetGroupsRecalcForMembershipSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningGroup> grouperTargetGroupsRecalcForGroupOnly = new ArrayList<ProvisioningGroup>();

      boolean canRetrieveMembershipsByGroup = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningBehavior().canSelectMembershipsForGroup(); 
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (provisioningGroupWrapper.getGrouperTargetGroup() != null) {
          if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships() && provisioningGroupWrapper.getProvisioningStateGroup().isIncrementalSyncMemberships() && canRetrieveMembershipsByGroup) {
            grouperTargetGroupsRecalcForMembershipSync.add(provisioningGroupWrapper.getGrouperTargetGroup());
            continue;
          }
          // we arent recalcing memberships, dont let the framework think we are
          provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(false);
          if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
            grouperTargetGroupsRecalcForGroupOnly.add(provisioningGroupWrapper.getGrouperTargetGroup());
            continue;
          }
          // for messaging we arent selecting groups
          if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
            if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
              // we need to retrieve or create this, its probably already a recalc but...
              grouperTargetGroupsRecalcForGroupOnly.add(provisioningGroupWrapper.getGrouperTargetGroup());
              provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(true);
              provisioningGroupWrapper.getProvisioningStateGroup().setRecalcGroupMemberships(true);
              continue;
            }
          }
        }
      }
      {
        // we need to retrieve non recalc groups for entity attribute provisioning, if there is a group link and no data
        List<ProvisioningGroup> grouperTargetGroupsToRetrieveForLinks = 
            this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic()
            .retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
        grouperTargetGroupsRecalcForGroupOnly.addAll(grouperTargetGroupsToRetrieveForLinks);
        for (ProvisioningGroup grouperTargetGroupToRetrieveForLinks : grouperTargetGroupsToRetrieveForLinks) {
          if (grouperTargetGroupToRetrieveForLinks.getProvisioningGroupWrapper() != null) {
            grouperTargetGroupToRetrieveForLinks.getProvisioningGroupWrapper().getProvisioningStateGroup().setRecalcObject(true);
          }
        }
      }
      if (grouperTargetGroupsRecalcForMembershipSync.size() > 0) {
        //collapse these into unique
        grouperTargetGroupsRecalcForMembershipSync = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(grouperTargetGroupsRecalcForMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupMembershipSync(grouperTargetGroupsRecalcForMembershipSync);
        needsData = true;
      }
      if (grouperTargetGroupsRecalcForGroupOnly.size() > 0) {
        grouperTargetGroupsRecalcForGroupOnly = new ArrayList<ProvisioningGroup>(new HashSet<ProvisioningGroup>(grouperTargetGroupsRecalcForGroupOnly));
        targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsRecalcForGroupOnly);
        needsData = true;
      }
    }    
    {
      List<ProvisioningEntity> grouperTargetEntitiesRecalcForMembershipSync = new ArrayList<ProvisioningEntity>();
      List<ProvisioningEntity> grouperTargetEntitiesRecalcForEntityOnly = new ArrayList<ProvisioningEntity>();

      boolean canRetrieveMembershipsByEntity = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningBehavior().canSelectMembershipsForEntity();
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        if (provisioningEntityWrapper.getGrouperTargetEntity() != null) {
          
          if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships() 
              && provisioningEntityWrapper.getProvisioningStateEntity().isIncrementalSyncMemberships() && canRetrieveMembershipsByEntity) {
            grouperTargetEntitiesRecalcForMembershipSync.add(provisioningEntityWrapper.getGrouperTargetEntity());
            continue;
          }
          // we arent recalcing memberships, dont let the framework think we are
          provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(false);
          if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
            grouperTargetEntitiesRecalcForEntityOnly.add(provisioningEntityWrapper.getGrouperTargetEntity());
            continue;
          }
          // for messaging we arent selecting groups
          if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities()) {
            if (provisioningEntityWrapper.getGcGrouperSyncMember() == null || !provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
              // we need to retrieve or create this, its probably already a recalc but...
              grouperTargetEntitiesRecalcForEntityOnly.add(provisioningEntityWrapper.getGrouperTargetEntity());
              provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(true);
              provisioningEntityWrapper.getProvisioningStateEntity().setRecalcEntityMemberships(true);
              continue;
            }
          }
        }
      }
      {
        // we need to retrieve non recalc entities for group attribute provisioning, if there is an entity link and no data
        List<ProvisioningEntity> grouperTargetEntitiesToRetrieveForLinks = 
            this.getGrouperProvisioner().retrieveGrouperProvisioningLinkLogic()
            .retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks(
                this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
        grouperTargetEntitiesRecalcForEntityOnly.addAll(grouperTargetEntitiesToRetrieveForLinks);
        for (ProvisioningEntity grouperTargetEntityToRetrieveForLinks : grouperTargetEntitiesToRetrieveForLinks) {
          if (grouperTargetEntityToRetrieveForLinks.getProvisioningEntityWrapper() != null) {
            grouperTargetEntityToRetrieveForLinks.getProvisioningEntityWrapper().getProvisioningStateEntity().setRecalcObject(true);
          }
        }
      }
      if (grouperTargetEntitiesRecalcForMembershipSync.size() > 0) {
        grouperTargetEntitiesRecalcForMembershipSync = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(grouperTargetEntitiesRecalcForMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityMembershipSync(grouperTargetEntitiesRecalcForMembershipSync);
        needsData = true;
      }
      if (grouperTargetEntitiesRecalcForEntityOnly.size() > 0) {
        grouperTargetEntitiesRecalcForEntityOnly = new ArrayList<ProvisioningEntity>(new HashSet<ProvisioningEntity>(grouperTargetEntitiesRecalcForEntityOnly));
        targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesRecalcForEntityOnly);
        needsData = true;
      }
    }
    
    {
      List<Object> provisioningObjectsRecalcForMembershipSync = new ArrayList<Object>();
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != null) {
        switch(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
          
          case membershipObjects:
  
            for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
              if (provisioningMembershipWrapper.getGrouperTargetMembership() != null && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
                ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
                ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
  
                if ((provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isIncrementalSyncMemberships())
                    || (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isIncrementalSyncMemberships())) {
                  // we are already retrieving this
                } else {
                  provisioningObjectsRecalcForMembershipSync.add(provisioningMembershipWrapper.getGrouperTargetMembership());
                }
              }
            }
  
            break;
  
          case entityAttributes:
            {
              Set<String> memberIdsAdded = new HashSet<String>();
              for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
                if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
                  ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                  ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                  ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                  ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
                  ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
      
                  if ((provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isIncrementalSyncMemberships())
                      || (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isIncrementalSyncMemberships())) {
                    // we are already retrieving this
                  } else {
                    String memberId = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getId();
                    ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperTargetEntity();
                    if (memberId != null && !memberIdsAdded.contains(memberId) && grouperTargetEntity != null) {
                      provisioningObjectsRecalcForMembershipSync.add(grouperTargetEntity);
                      memberIdsAdded.add(memberId);
                    }
                  }
                }
              }
            }
            
            break;
            
          case groupAttributes:
            {
              Set<String> groupIdsAdded = new HashSet<String>();
              for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
                if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
                  ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                  ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                  ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                  ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
                  ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
      
                  if ((provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isIncrementalSyncMemberships())
                      || (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isIncrementalSyncMemberships())) {
                    // we are already retrieving this
                  } else {
                    String groupId = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getId();
                    ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroup();
                    if (groupId != null && !groupIdsAdded.contains(groupId) && grouperTargetGroup != null) {
                      provisioningObjectsRecalcForMembershipSync.add(grouperTargetGroup);
                      groupIdsAdded.add(groupId);
                    }
                  }
                }
              }
            }
            
            break;
            
            default: 
              throw new RuntimeException("Not expecting GrouperProvisioningBehaviorMembershipType: "
                  + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
        }
      }
      if (provisioningObjectsRecalcForMembershipSync.size() > 0) {
        provisioningObjectsRecalcForMembershipSync = new ArrayList<Object>(new HashSet<Object>(provisioningObjectsRecalcForMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetMembershipObjectsForMembershipSync(provisioningObjectsRecalcForMembershipSync);
        needsData = true;
      }
      {
        Set<ProvisioningGroup> groupsToRetrieve = new HashSet<ProvisioningGroup>();
        Set<ProvisioningEntity> entitiesToRetrieve = new HashSet<ProvisioningEntity>();
        for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync()) ) {
          groupsToRetrieve.add(grouperTargetGroup);
        }
        for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) ) {
          groupsToRetrieve.add(grouperTargetGroup);
        }
        for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync())) {
          entitiesToRetrieve.add(grouperTargetEntity);
        }
        for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly())) {
          entitiesToRetrieve.add(grouperTargetEntity);
        }
        
        // we need to add groups that are there for entity recalcs, and entities there for group recalcs, and both for membership recalcs
        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
          boolean retrieveGroupAndMember = false;
          if (provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
            retrieveGroupAndMember = true;
          }
          ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
          ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
          ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
          if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
            retrieveGroupAndMember = true;
          }
          if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isRecalcEntityMemberships()) {
            retrieveGroupAndMember = true;
          }
          if (retrieveGroupAndMember) {
            {
              ProvisioningGroup thisGrouperTargetGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperTargetGroup();
              if (thisGrouperTargetGroup != null && !groupsToRetrieve.contains(thisGrouperTargetGroup)) {
                if (targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly() == null) {
                  targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(new ArrayList<ProvisioningGroup>());
                }
                needsData = true;
                targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().add(provisioningGroupWrapper.getGrouperTargetGroup());
                provisioningGroupWrapper.getProvisioningStateGroup().setRecalcObject(true);
              }
            }
            {
              ProvisioningEntity thisGrouperTargetEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperTargetEntity();
              if (!entitiesToRetrieve.contains(thisGrouperTargetEntity) && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
                if (targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly() == null) {
                  targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(new ArrayList<ProvisioningEntity>());
                }
                needsData = true;
                targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().add(provisioningEntityWrapper.getGrouperTargetEntity());
                provisioningEntityWrapper.getProvisioningStateEntity().setRecalcObject(true);
              }
            }
          }
        }        
      }
    }
    if (!needsData) {
      return null;
    }
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().filterNonRecalcActionsCapturedByRecalc();
    
    ensureAllMembershipRequestsAreInTheOnlyRequestsAlso(targetDaoRetrieveIncrementalDataRequest);
    removeSomeMembershipRequestsAreInTheOnlyRequestsAlso(targetDaoRetrieveIncrementalDataRequest);
    
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync())) {
      if (provisioningEntity.getProvisioningEntityWrapper() != null) {
        provisioningEntity.getProvisioningEntityWrapper().getProvisioningStateEntity().setRecalcEntityMemberships(true);
        provisioningEntity.getProvisioningEntityWrapper().getProvisioningStateEntity().setRecalcObject(true);
      }
    }
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly())) {
      if (provisioningEntity.getProvisioningEntityWrapper() != null) {
        provisioningEntity.getProvisioningEntityWrapper().getProvisioningStateEntity().setRecalcObject(true);
      }
    }
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync())) {
      if (provisioningGroup.getProvisioningGroupWrapper() != null) {
        provisioningGroup.getProvisioningGroupWrapper().getProvisioningStateGroup().setRecalcGroupMemberships(true);
        provisioningGroup.getProvisioningGroupWrapper().getProvisioningStateGroup().setRecalcObject(true);
      }
    }
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly())) {
      if (provisioningGroup.getProvisioningGroupWrapper() != null) {
        provisioningGroup.getProvisioningGroupWrapper().getProvisioningStateGroup().setRecalcObject(true);
      }
    }
    for (Object provisioningMembershipObject : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetMembershipObjectsForMembershipSync())) {
      if (!(provisioningMembershipObject instanceof ProvisioningMembership)) {
        continue;
      }
      ProvisioningMembership provisioningMembership = (ProvisioningMembership)provisioningMembershipObject;
      if (provisioningMembership.getProvisioningMembershipWrapper() != null) {
        provisioningMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setRecalcObject(true);
      }
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
      
      switch(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        
        case membershipObjects:

          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            addMembershipToProvisioningLists(object, result);
          }
          
          break;

        case entityAttributes:

          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            addMembershipToProvisioningLists(object, result);
          }

          break;
          
        case groupAttributes:
          
          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            addMembershipToProvisioningLists(object, result);
          }

          break;
          
        default: 
          throw new RuntimeException("Not expecting GrouperProvisioningBehaviorMembershipType: "
              + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
      }
    }
    return result;
  }

  /**
   * dont double request groups/entities
   * @param targetDaoRetrieveIncrementalDataRequest
   */
  public void removeSomeMembershipRequestsAreInTheOnlyRequestsAlso(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()) > 0 
        && GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().removeAll(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync());
    }
  }

  /**
     * make sure that all the target requests that include memberships also are requesting the group or entities in the "only" lists.
   * @param targetDaoRetrieveIncrementalDataRequest
   */
  public void ensureAllMembershipRequestsAreInTheOnlyRequestsAlso(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest) {
      
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      
      Set<ProvisioningGroup> targetGroupsForGroupOnlySet = new HashSet<ProvisioningGroup>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()));
      // indexing the groups that are already there
      for (ProvisioningGroup targetGroupForGroupOnly: targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) {
        targetGroupsForGroupOnlySet.add(targetGroupForGroupOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningGroup targetGroupForGroupMembershipSync: targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync()) {
        if (!targetGroupsForGroupOnlySet.contains(targetGroupForGroupMembershipSync)) {
          targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().add(targetGroupForGroupMembershipSync);
          targetGroupsForGroupOnlySet.add(targetGroupForGroupMembershipSync);
        }
      }
    }

    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync()) > 0 
        && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      
      Set<ProvisioningEntity> targetEntitiesForEntityOnlySet = new HashSet<ProvisioningEntity>();
      
      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()));
      // indexing the entities that are already there
      for (ProvisioningEntity targetEntityForEntityOnly: targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly()) {
        targetEntitiesForEntityOnlySet.add(targetEntityForEntityOnly);
      }
      
      // if a membership object is not there then add it
      for (ProvisioningEntity targetEntityForEntityMembershipSync: targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync()) {
        if (!targetEntitiesForEntityOnlySet.contains(targetEntityForEntityMembershipSync)) {
          targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().add(targetEntityForEntityMembershipSync);
          targetEntitiesForEntityOnlySet.add(targetEntityForEntityMembershipSync);
        }
      }
    }

  }

  

  private void addMembershipToProvisioningLists(Object object, GrouperProvisioningLists result) {
    
    if (object instanceof ProvisioningGroup) {
      
      List<ProvisioningGroup> provisioningGroups = result.getProvisioningGroups();
      
      if (provisioningGroups == null) {
        provisioningGroups = new ArrayList<ProvisioningGroup>();
        result.setProvisioningGroups(provisioningGroups);
      }
      
      provisioningGroups.add((ProvisioningGroup)object);
      
    } else if (object instanceof ProvisioningEntity) {
      
      List<ProvisioningEntity> provisioningEntities = result.getProvisioningEntities();
      
      if (provisioningEntities == null) {
        provisioningEntities = new ArrayList<ProvisioningEntity>();
        result.setProvisioningEntities(provisioningEntities);
      }
      
      provisioningEntities.add((ProvisioningEntity)object);
      
    } else if (object instanceof ProvisioningMembership) {
      List<ProvisioningMembership> provisioningMemberships = result.getProvisioningMemberships();
      
      if (provisioningMemberships == null) {
        provisioningMemberships = new ArrayList<ProvisioningMembership>();
        result.setProvisioningMemberships(provisioningMemberships);
      }
      
      provisioningMemberships.add((ProvisioningMembership)object);
      
    } else {
      throw new RuntimeException("Invalid object type. It needs to be one of ProvisioningGroup or ProvisioningEntity or ProvisioningMembership");
    }
    
  }


  public void markMembershipsRecalcIfRetrievedByGroupOrEntity() {
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
      if (provisioningGroupWrappersforMembershipSync.contains(provisioningMembershipWrapper.getProvisioningGroupWrapper())
          || provisioningEntityWrappersforMembershipSync.contains(provisioningMembershipWrapper.getProvisioningEntityWrapper())) {
        provisioningMembershipWrapper.getProvisioningStateMembership().setRecalcObject(true);
        membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity++;
      }
    }
    if (membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity > 0) {
      this.getGrouperProvisioner().getDebugMap().put("membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity", membershipsMarkedAsRecalcIfRetrievedByGroupOrEntity);
    }
  }

  

}
