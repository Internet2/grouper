package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
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
    
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();

    Set<String> validGroupIds = new HashSet<String>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers())) {
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup == null) {
        continue;
      }
      
      if (gcGrouperSyncGroup.isProvisionable() ) {
        validGroupIds.add(gcGrouperSyncGroup.getGroupId());
      }
      
    }
    
    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly().iterator();

    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      String groupId = (String)grouperIncrementalDataItem.getItem();
      if (!validGroupIds.contains(groupId)) {
        iterator.remove();
        filterByNotProvisionable++;
      }
    }
    
    iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync().iterator();

    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      String groupId = (String)grouperIncrementalDataItem.getItem();
      if (!validGroupIds.contains(groupId)) {
        iterator.remove();
        filterByNotProvisionable++;
      }
    }
    
    iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();

    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      String groupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
      if (!validGroupIds.contains(groupId)) {
        iterator.remove();
        filterByNotProvisionable++;
      }
    }
    
    if (filterByNotProvisionable > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByNotProvisionable", filterByNotProvisionable);
    }

  }


  /**
     * filter events that happened after the last full sync
     * @param esbEventContainers
     * @param gcGrouperSync
     */
  public void filterByProvisioningFullSync() {

    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    // check for full sync, only if it finished
    Timestamp lastFullSync = gcGrouperSync.getLastFullSyncRun() != null ? gcGrouperSync.getLastFullSyncStart() : null;

    //TODO    Timestamp lastFullMetadataSync = gcGrouperSync.getLastFullMetadataSyncRun();
    int[] skippedEventsDueToFullSync = new int[] {0};
    
    if (lastFullSync != null) {
      long lastFullSyncMillis = lastFullSync.getTime();
      
      GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
      
      if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp() != null) {
        if (grouperProvisioningDataIncrementalInput.getFullSyncMessageTimestamp().getTime() < lastFullSyncMillis) {
          grouperProvisioningDataIncrementalInput.setFullSyncMessageTimestamp(null);
          grouperProvisioningDataIncrementalInput.setFullSync(false);
          skippedEventsDueToFullSync[0]++;
        }
      }

      filterByProvisioningFullSync(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc(), skippedEventsDueToFullSync, lastFullSyncMillis);

      filterByProvisioningFullSync(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc(), skippedEventsDueToFullSync, lastFullSyncMillis);

    }
    if (skippedEventsDueToFullSync[0] > 0) {
      this.getGrouperProvisioner().getDebugMap().put("skippedEventsDueToFullSync", skippedEventsDueToFullSync[0]);
    }
  }

  /**
   * 
   * @param grouperIncrementalDataToProcess
   * @param skippedEventsDueToFullSync
   * @param lastFullSyncMillis
   */
  public void filterByProvisioningFullSync(
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcess,
      int[] skippedEventsDueToFullSync, long lastFullSyncMillis) {
    
    filterByProvisioningFullSync(grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync(), 
        skippedEventsDueToFullSync, lastFullSyncMillis);

    filterByProvisioningFullSync(grouperIncrementalDataToProcess.getGroupUuidsForGroupOnly(), 
        skippedEventsDueToFullSync, lastFullSyncMillis);

    filterByProvisioningFullSync(grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync(),
        skippedEventsDueToFullSync, lastFullSyncMillis);

    filterByProvisioningFullSync(grouperIncrementalDataToProcess.getMemberUuidsForEntityMembershipSync(),
        skippedEventsDueToFullSync, lastFullSyncMillis);

    filterByProvisioningFullSync(grouperIncrementalDataToProcess.getMemberUuidsForEntityOnly(),
        skippedEventsDueToFullSync, lastFullSyncMillis);


  }


  public void filterByProvisioningFullSync(
      Set<GrouperIncrementalDataItem> grouperIncrementalDataItems,
      int[] skippedEventsDueToFullSync, long lastFullSyncMillis) {

    if (GrouperUtil.length(grouperIncrementalDataItems) == 0) {
      return;
    }

    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataItems.iterator();
    
    while (iterator.hasNext()) {
      
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      
      if (grouperIncrementalDataItem.getMillisSince1970() != null && grouperIncrementalDataItem.getMillisSince1970() < lastFullSyncMillis) {
        skippedEventsDueToFullSync[0]++;
        iterator.remove();
      }
      
    }
    
  }


  /**
   * filter events that happened after the last group sync
   * @param esbEventContainers
   * @param gcGrouperSync
   */
  public void filterByGroupSync() {
  
    int[] filterByGroupSyncGroups = new int[] {0};
    int[] filterByGroupSyncMemberships = new int[] {0};
    
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    
    //recalc or not
    for (Object grouperIncrementalDataToProcessObject : new Object[] {grouperIncrementalDataToProcessWithoutRecalc,
        grouperIncrementalDataToProcessWithRecalc}) {
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcess = (GrouperIncrementalDataToProcess)grouperIncrementalDataToProcessObject;
      
      //go through and remove from elsewhere
      Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync().iterator();

      while (iterator.hasNext()) {
        GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
        String currentGroupId = (String)grouperIncrementalDataItem.getItem();
        filterByGroupSyncHelper(filterByGroupSyncGroups,
            groupUuidToProvisioningGroupWrapper, iterator, grouperIncrementalDataItem,
            currentGroupId);
      }
      
      iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();

      while (iterator.hasNext()) {
        GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
        String currentGroupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
        filterByGroupSyncHelper(filterByGroupSyncGroups,
            groupUuidToProvisioningGroupWrapper, iterator, grouperIncrementalDataItem,
            currentGroupId);
      }
      
    }
  
    if (filterByGroupSyncGroups[0] > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByGroupSyncGroups", filterByGroupSyncGroups[0]);
    }
    if (filterByGroupSyncMemberships[0] > 0) {
      this.getGrouperProvisioner().getDebugMap().put("filterByGroupSyncMemberships", filterByGroupSyncMemberships[0]);
    }

  }

  /**
   * 
   * @param filterByGroupSyncGroups
   * @param groupUuidToProvisioningGroupWrapper
   * @param iterator
   * @param grouperIncrementalDataItem
   * @param currentGroupId
   */
  public void filterByGroupSyncHelper(int[] filterByGroupSyncGroups,
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper,
      Iterator<GrouperIncrementalDataItem> iterator,
      GrouperIncrementalDataItem grouperIncrementalDataItem, String currentGroupId) {
    ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(currentGroupId);
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
    // if there wasnt a timestamp in this message, dont filter
    if (grouperIncrementalDataItem.getMillisSince1970() == null) {
      return;
    }
    // if there wasnt a last group sync start
    if (gcGrouperSyncGroup == null || gcGrouperSyncGroup.getLastGroupSyncStart() == null) {
      return;
    }
    if (gcGrouperSyncGroup.getLastGroupSyncStart().getTime() > grouperIncrementalDataItem.getMillisSince1970()) {
      filterByGroupSyncGroups[0]++;
      iterator.remove();
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
              grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc().getGroupUuidsForGroupMembershipSync().add(new GrouperIncrementalDataItem(groupId, provisioningMessage.getMillisSince1970()));
              messageCountForProvisioner++;
            }
            
          }
          
          if (GrouperUtil.length(provisioningMessage.getMemberIdsForSync()) > 0) {
            for (String memberId : provisioningMessage.getMemberIdsForSync()) {
              grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc().getMemberUuidsForEntityMembershipSync().add(new GrouperIncrementalDataItem(memberId, provisioningMessage.getMillisSince1970()));
              messageCountForProvisioner++;
            }
            
          }
          // TODO if user sync then see if supports user sync and if not convert to membership syncs (current and recent)
          
          if (GrouperUtil.length(provisioningMessage.getMembershipsForSync()) > 0) {
            for (ProvisioningMembershipMessage provisioningMembershipMessage : provisioningMessage.getMembershipsForSync()) {
              grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc().getGroupUuidsMemberUuidsForMembershipSync().add(
                  new GrouperIncrementalDataItem(new MultiKey(provisioningMembershipMessage.getGroupId(), provisioningMembershipMessage.getMemberId()), provisioningMessage.getMillisSince1970()));
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


  /**
   * TODO what is this?
   * make a deep copy of grouper provisioning data into the grouper provisioning objects to delete
   */
  public void setupIncrementalClonesOfGroupProvisioningObjects() {
    GrouperProvisioningLists grouperProvisioningObjects = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjects();
    GrouperProvisioningLists grouperProvisioningObjectsIncludeDeletes = this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouper().getGrouperProvisioningObjectsIncludeDeletes();
  
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
      provisioningGroupWrapper.setGrouperProvisioningGroup(provisioningGroupIncludeDelete);
      provisioningGroupWrapper.setDelete(true);
    }
  
    Map<String, ProvisioningEntity> memberUuidToProvisioningEntityIncludeDelete = new HashMap<String, ProvisioningEntity>();
    for (ProvisioningEntity provisioningEntityIncludeDelete : GrouperUtil.nonNull(grouperProvisioningEntitysIncludeDeletes)) {
      memberUuidToProvisioningEntityIncludeDelete.put(provisioningEntityIncludeDelete.getId(), provisioningEntityIncludeDelete);
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntityIncludeDelete.getProvisioningEntityWrapper();
      provisioningEntityWrapper.setGrouperProvisioningEntity(provisioningEntityIncludeDelete);
      provisioningEntityWrapper.setDelete(true);
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
      provisioningMembershipWrapper.setGrouperProvisioningMembership(provisioningMembershipIncludeDelete);
      provisioningMembershipWrapper.setDelete(true);
  
    }
    if (membershipReferenceNotMatchIncludeDeletes > 0) {
      this.grouperProvisioner.getDebugMap().put("membershipReferenceNotMatchIncludeDeletes", membershipReferenceNotMatchIncludeDeletes);
    }
  }


// TODO
//  public void setupIncrementalGrouperTargetObjectsToRetrieveFromTarget() {
//  
//    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = 
//        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getTargetDaoRetrieveIncrementalDataRequest();
//  
//    GrouperIncrementalDataToProcess grouperIncrementalUuidsToRetrieveFromGrouper =
//        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
//  
//    {
//      //the groups only should be the full list of grouper target groups
//      List<ProvisioningGroup> grouperTargetGroupsForGroupOnly = new ArrayList<ProvisioningGroup>(
//          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningGroups()));
//      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(grouperTargetGroupsForGroupOnly);
//    }
//  
//    {
//      // generally these arent needed, but if there are no target groups (e.g. only entities or memberships), maybe needed
//      List<ProvisioningGroupWrapper> groupWrappersForGroupOnly = new ArrayList<ProvisioningGroupWrapper>(
//          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper()).values());
//      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupOnly(groupWrappersForGroupOnly);
//    }
//    {
//      //the entities only should be the full list of grouper target entities
//      List<ProvisioningEntity> grouperTargetEntitiesForEntityOnly = new ArrayList<ProvisioningEntity>(
//          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsIncludeDeletes().getProvisioningEntities()));
//      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(grouperTargetEntitiesForEntityOnly);
//    }
//    {
//      // generally these arent needed, but if there are no target entities (e.g. only groups or memberships), maybe needed
//      List<ProvisioningEntityWrapper> entityWrappersForEntityOnly = new ArrayList<ProvisioningEntityWrapper>(
//          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper()).values());
//      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersForEntityOnly(entityWrappersForEntityOnly);
//    }
//    {
//      int missingGrouperTargetGroupsForGroupSync = 0;
//      //the groups for group memberships should be looked up from the wrapper objects
//      List<ProvisioningGroup> grouperTargetGroupForGroupSync = new ArrayList<ProvisioningGroup>();
//      List<ProvisioningGroupWrapper> provisioningGroupWrappersForGroupSync = new ArrayList<ProvisioningGroupWrapper>();
//      targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupMembershipSync(grouperTargetGroupForGroupSync);
//      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupWrappersForGroupMembershipSync(provisioningGroupWrappersForGroupSync);
//      for (String groupUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync())) {
//        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
//        if (provisioningGroupWrapper == null) {
//          missingGrouperTargetGroupsForGroupSync++;
//          continue;
//        }
//        provisioningGroupWrappersForGroupSync.add(provisioningGroupWrapper);
//        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
//        if (grouperTargetGroup == null || !provisioningGroupWrapper.isDelete()) {
//          // this might be expected
//          continue;
//        }
//        grouperTargetGroupForGroupSync.add(grouperTargetGroup);
//      }
//      if (missingGrouperTargetGroupsForGroupSync > 0) {
//        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetGroupsForGroupSync", missingGrouperTargetGroupsForGroupSync);
//      }
//    }    
//    {
//      int missingGrouperTargetEntitiesForEntitySync = 0;
//      //the groups for group memberships should be looked up from the wrapper objects
//      List<ProvisioningEntity> grouperTargetEntityForEntitySync = new ArrayList<ProvisioningEntity>();
//      List<ProvisioningEntityWrapper> provisioningEntityWrappersForEntitySync = new ArrayList<ProvisioningEntityWrapper>();
//      
//      targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityMembershipSync(grouperTargetEntityForEntitySync);
//      targetDaoRetrieveIncrementalDataRequest.setProvisioningEntityWrappersforEntityMembershipSync(provisioningEntityWrappersForEntitySync);
//  
//      for (String memberUuid : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityMembershipSync())) {
//        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
//        if (provisioningEntityWrapper == null) {
//          missingGrouperTargetEntitiesForEntitySync++;
//          continue;
//        }
//        provisioningEntityWrappersForEntitySync.add(provisioningEntityWrapper);
//        
//        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
//        if (grouperTargetEntity == null || !provisioningEntityWrapper.isDelete()) {
//          continue;
//        }
//        grouperTargetEntityForEntitySync.add(grouperTargetEntity);
//      }
//      if (missingGrouperTargetEntitiesForEntitySync > 0) {
//        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetEntitiesForEntitySync", missingGrouperTargetEntitiesForEntitySync);
//      }
//    }    
//    //lets get the memberships
//    {
//      int missingGrouperTargetMembershipsForMembershipSync = 0;
//      //the groups only should be the full list of grouper target groups
//      List<MultiKey> groupEntityMembershipWrappers = new ArrayList<MultiKey>();
//      targetDaoRetrieveIncrementalDataRequest.setProvisioningGroupMemberMembershipWrappersForMembershipSync(groupEntityMembershipWrappers);
//  
//      List<MultiKey> grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships = new ArrayList<MultiKey>();
//      targetDaoRetrieveIncrementalDataRequest
//        .setTargetGroupsEntitiesMembershipsForMembershipSync(grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships);
//      
//      //get the ones that were looked up in grouper
//      for (MultiKey groupUuidMemberUuidFieldId : GrouperUtil.nonNull(grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsMemberUuidsFieldIdsForMembershipSync())) {
//        
//        String groupUuid = (String)groupUuidMemberUuidFieldId.getKey(0);
//        String memberUuid = (String)groupUuidMemberUuidFieldId.getKey(1);
//        
//        MultiKey groupUuidMemberUuid = new MultiKey(groupUuid, memberUuid);
//        
//        // dont need field id
//        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberUuid);
//        ProvisioningEntity grouperTargetEntity = (provisioningEntityWrapper == null || !provisioningEntityWrapper.isDelete()) ? null : provisioningEntityWrapper.getGrouperTargetEntity();
//  
//        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid);
//        ProvisioningGroup grouperTargetGroup = (provisioningGroupWrapper == null || !provisioningGroupWrapper.isDelete()) ? null : provisioningGroupWrapper.getGrouperTargetGroup();
//  
//        ProvisioningMembershipWrapper provisioningMembershipWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().get(groupUuidMemberUuid);
//        ProvisioningMembership grouperTargetMembership = (provisioningMembershipWrapper == null || !provisioningMembershipWrapper.isDelete()) ? null : provisioningMembershipWrapper.getGrouperTargetMembership();
//  
//        if (grouperTargetMembership != null || (grouperTargetGroup != null && grouperTargetEntity != null)) {
//          grouperTargetGroupsGrouperTargetEntitiesGrouperTargetMemberships.add(new MultiKey(grouperTargetGroup, grouperTargetEntity, grouperTargetMembership));
//        }
//        if (provisioningMembershipWrapper != null || (provisioningGroupWrapper != null && provisioningEntityWrapper != null)) {
//          groupEntityMembershipWrappers.add(new MultiKey(provisioningGroupWrapper, provisioningEntityWrapper, provisioningMembershipWrapper));
//          
//        } else {
//          missingGrouperTargetMembershipsForMembershipSync++;
//        }
//        
//      }
//      if (missingGrouperTargetMembershipsForMembershipSync > 0) {
//        this.grouperProvisioner.getDebugMap().put("missingGrouperTargetMembershipsForMembershipSync", missingGrouperTargetMembershipsForMembershipSync);
//      }
//      
//    }
//  
//  }

  public void propagateProvisioningAttributes() {
    List<EsbEventContainer> esbEventContainers = new ArrayList<EsbEventContainer>(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));

    Map<String, GrouperProvisioningObjectAttributes> grouperProvisioningGroupAttributesToProcess = new HashMap<String, GrouperProvisioningObjectAttributes>();
    Map<String, GrouperProvisioningObjectAttributes> allAncestorProvisioningGroupAttributes = new HashMap<String, GrouperProvisioningObjectAttributes>();
    String grouperObjectTypeName = GrouperObjectTypesSettings.objectTypesStemName() + ":" + GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
    
    Set<String> queriedPITAttributeAssignIds = new HashSet<String>();
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();

      if (esbEventType == EsbEventType.GROUP_ADD) {
        
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
      } else if ((esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) &&
          esbEvent.getAttributeDefNameName().startsWith(GrouperProvisioningSettings.provisioningConfigStemName())) {

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
    
    Set<GrouperProvisioningObjectAttributes> grouperProvisioningObjectAttributesToProcess = new HashSet<GrouperProvisioningObjectAttributes>();
    grouperProvisioningObjectAttributesToProcess.addAll(grouperProvisioningGroupAttributesToProcess.values());
    
    Set<String> policyGroupIds = this.grouperProvisioner.retrieveGrouperDao().retrieveProvisioningGroupIdsThatArePolicyGroups(grouperProvisioningGroupAttributesToProcess.keySet());
    
    if (grouperProvisioningObjectAttributesToProcess.size() > 0) {
      Map<String, GrouperProvisioningObjectAttributes> calculatedProvisioningAttributes = GrouperProvisioningService.calculateProvisioningAttributes(this.getGrouperProvisioner(), grouperProvisioningObjectAttributesToProcess, allAncestorProvisioningGroupAttributes, policyGroupIds);
      
      Set<String> syncGroupIdsToRetrieve = new HashSet<String>();
      for (GrouperProvisioningObjectAttributes objectAttributes : grouperProvisioningObjectAttributesToProcess) {
        syncGroupIdsToRetrieve.add(objectAttributes.getId());
      }

      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(syncGroupIdsToRetrieve);

      ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();
      this.getGrouperProvisioner().setProvisioningSyncResult(provisioningSyncResult);
      ProvisioningSyncIntegration.fullSyncGroups(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
          new ArrayList<GcGrouperSyncGroup>(grouperSyncGroupIdToSyncGroup.values()),
          calculatedProvisioningAttributes);

      Set<String> groupIdsToTriggerSync = new HashSet<String>();
      for (GcGrouperSyncGroup gcGrouperSyncGroup : this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().retrieveUpdatedCacheSyncGroups()) {
        groupIdsToTriggerSync.add(gcGrouperSyncGroup.getGroupId());
      }
      
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();

      for (String groupId : groupIdsToTriggerSync) {
        //ProvisioningMessage provisioningMessage = new ProvisioningMessage();
        //provisioningMessage.setGroupIdsForSync(new String[] {gcGrouperSyncGroup.getGroupId()});
        //provisioningMessage.setBlocking(false);
        //provisioningMessage.send(this.grouperProvisioner.getConfigId());
        GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
        grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc().getGroupUuidsForGroupMembershipSync().add(new GrouperIncrementalDataItem(groupId, System.currentTimeMillis()));
      }      
    }
  }

  public void incrementalCheckChangeLog() {
    
    // see if we are getting memberships or privs
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();

    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcess = null;
    boolean recalcOnly = false;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isRecalculateAllOperations()) {
      recalcOnly = true;
      grouperIncrementalDataToProcess = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();
    } else {
      grouperIncrementalDataToProcess = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    }
    
    int changeLogCount = 0;
    
    if (changeLogCount > 0) {
      this.getGrouperProvisioner().getDebugMap().put("changeLogRawCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getEsbEventContainers()));
    }
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
          
          if (!StringUtils.isBlank(esbEvent.getGroupId())) {

            // do we need to update memberships?  hmmm, maybe, so might as well
            grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync().add(new GrouperIncrementalDataItem(esbEvent.getGroupId(), createdOnMillis));
            changeLogCount++;
          }
          
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
      }
      if (syncThisMembership) {
        // group_id, member_id, field_id
        Field field = FieldFinder.find(esbEvent.getFieldName(), true);
        MultiKey membershipFields = new MultiKey(esbEvent.getGroupId(), esbEvent.getMemberId());
        
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
        grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync().add(
            new GrouperIncrementalDataItem(membershipFields, createdOnMillis, grouperIncrementalDataAction));
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
  
    int[] recalcEventsDuringFullSync = new int[] {0};
    
    if (lastFullSyncStart != null && lastFullSyncEnd != null) {
      long lastFullSyncStartMillis = lastFullSyncStart.getTime();
      long lastFullSyncEndMillis = lastFullSyncEnd.getTime();
      
      GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc();
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc();
      
      recalcEventsDuringFullSync(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync(),
          grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync(), 
          recalcEventsDuringFullSync, lastFullSyncStartMillis, lastFullSyncEndMillis);

      recalcEventsDuringFullSync(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupOnly(),
          grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly(), 
          recalcEventsDuringFullSync, lastFullSyncStartMillis, lastFullSyncEndMillis);

      recalcEventsDuringFullSync(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync(),
          grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync(), 
          recalcEventsDuringFullSync, lastFullSyncStartMillis, lastFullSyncEndMillis);

      recalcEventsDuringFullSync(grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync(),
          grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync(), 
          recalcEventsDuringFullSync, lastFullSyncStartMillis, lastFullSyncEndMillis);

      recalcEventsDuringFullSync(grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityOnly(),
          grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly(), 
          recalcEventsDuringFullSync, lastFullSyncStartMillis, lastFullSyncEndMillis);
  
    }
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringFullSync", recalcEventsDuringFullSync[0]);
  }

  public void recalcEventsDuringFullSync(
      Set<GrouperIncrementalDataItem> grouperIncrementalDataItemsWithRecalc,
      Set<GrouperIncrementalDataItem> grouperIncrementalDataItemsWithoutRecalc,
      int[] recalcEventsDuringFullSync, long lastFullSyncStartMillis,
      long lastFullSyncEndMillis) {

    
    if (GrouperUtil.length(grouperIncrementalDataItemsWithoutRecalc) == 0) {
      return;
    }

    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataItemsWithoutRecalc.iterator();
    
    while (iterator.hasNext()) {
      
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      
      if (grouperIncrementalDataItem.getMillisSince1970() != null && grouperIncrementalDataItem.getMillisSince1970() >= lastFullSyncStartMillis
          && grouperIncrementalDataItem.getMillisSince1970() < lastFullSyncEndMillis) {
        recalcEventsDuringFullSync[0]++;
        iterator.remove();
        grouperIncrementalDataItemsWithRecalc.add(grouperIncrementalDataItem);
      }
      
    }

  }

  /**
   * 
   */
  public void addErrorsToQueue() {
    
    int addErrorsToQueue = 0;
    
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    long random100 = (long)(Math.random() * 100L);

    // always check 2 minutes back
    int secondsToCheck = -1;

    if (random100 < 5) {
      // 1/20th of the time get all errors 10 minutes back
      this.getGrouperProvisioner().getDebugMap().put("checkErrors", "all");
      secondsToCheck = -1;
    } else if (random100 < 10) {
      // 1/10th of the time get all errors 12 minutes back
      this.getGrouperProvisioner().getDebugMap().put("checkErrorsBack", "12min");
      secondsToCheck = 60*12 + 20;
    } else {
      // all the time check 2 minutes back
      this.getGrouperProvisioner().getDebugMap().put("checkErrorsBack", "2min");
      secondsToCheck = 60*2 + 20;
    }

    long millisToCheckFrom = gcGrouperSync.getLastFullSyncStart() == null ? -1 : gcGrouperSync.getLastFullSyncStart().getTime();

    if (secondsToCheck > 0) {
      long newMillisToCheckFrom = System.currentTimeMillis() - (secondsToCheck * 1000);
      millisToCheckFrom = Math.max(millisToCheckFrom, newMillisToCheckFrom);
    }
  
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();

    Set<GrouperIncrementalDataItem> groupUuidsForGroupMembershipSync = grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync();
    Set<GrouperIncrementalDataItem> groupUuidsForGroupOnly = grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupOnly();

    {
      List<String> groupIds = gcGrouperSync.getGcGrouperSyncGroupDao().retrieveGroupIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      
      for (String groupId : GrouperUtil.nonNull(groupIds)) {
        groupUuidsForGroupMembershipSync.add(new GrouperIncrementalDataItem(groupId, null));
        groupUuidsForGroupOnly.add(new GrouperIncrementalDataItem(groupId, null));
        addErrorsToQueue++;
      }
    }
    
    Set<GrouperIncrementalDataItem> memberUuidsForEntityMembershipSync = grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync();
    Set<GrouperIncrementalDataItem> memberUuidsForEntityOnly = grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityOnly();

    {
      List<String> memberIds = gcGrouperSync.getGcGrouperSyncMemberDao().retrieveMemberIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      
      for (String memberId : GrouperUtil.nonNull(memberIds)) {
        memberUuidsForEntityMembershipSync.add(new GrouperIncrementalDataItem(memberId, null));
        memberUuidsForEntityOnly.add(new GrouperIncrementalDataItem(memberId, null));
        addErrorsToQueue++;
      }
    }

    Set<GrouperIncrementalDataItem> groupIdMemberIdsFieldIdsForMembershipSync = grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync();

    {
      List<Object[]> groupIdMemberIds = gcGrouperSync.getGcGrouperSyncMembershipDao().retrieveGroupIdMemberIdsWithErrorsAfterMillis(millisToCheckFrom > 0 ? new Timestamp(millisToCheckFrom) : null);
      
      for (Object[] groupIdMemberId : GrouperUtil.nonNull(groupIdMemberIds)) {
        MultiKey groupIdMemberIdFieldId = new MultiKey((String)groupIdMemberId[0], (String)groupIdMemberId[1], null);
        groupIdMemberIdsFieldIdsForMembershipSync.add(new GrouperIncrementalDataItem(groupIdMemberIdFieldId, null));
        groupUuidsForGroupOnly.add(new GrouperIncrementalDataItem((String)groupIdMemberId[0], null));
        memberUuidsForEntityOnly.add(new GrouperIncrementalDataItem((String)groupIdMemberId[1], null));
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
    
    GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc();
    
    int size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync());
    grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync());
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync());

    size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly());
    grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupOnly());
    grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync());
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly());
    
    size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync());
    grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync());
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync());

    size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly());
    grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityOnly());
    grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync());
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly());

    size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync());
    grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync());
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync());
    
    size = GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync());
    grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().removeAll(
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync());
    // take out membership syncs if there is a group sync or member sync
    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();
    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      {
        String groupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
        if (grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync().contains(groupId)
            || grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync().contains(groupId)) {
          iterator.remove();
          filterNonRecalcActionsCapturedByRecalc++;
          continue;
        }
      }
      {
        String memberId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(1);
        if (grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync().contains(memberId)
            || grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync().contains(memberId)) {
          iterator.remove();
          filterNonRecalcActionsCapturedByRecalc++;
          continue;
        }
      }
    }
    filterNonRecalcActionsCapturedByRecalc += size - GrouperUtil.length(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync());
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
  
    int[] organizeRecalcAndNonRecalcRequests = new int[] {0};
  
    GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
    
    organizeRecalcAndNonRecalcRequestsEntities(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc(), organizeRecalcAndNonRecalcRequests, false);
  
    organizeRecalcAndNonRecalcRequestsEntities(grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc(), organizeRecalcAndNonRecalcRequests, true);
  
    if (organizeRecalcAndNonRecalcRequests[0] > 0) {
      this.getGrouperProvisioner().getDebugMap().put("organizeRecalcAndNonRecalcRequestsEntities", organizeRecalcAndNonRecalcRequests[0]);
    }
  
  }


  /**
   * make sure the list of groups/entities (without memberships) includes all the groups/entities for memberships
   * @param grouperIncrementalDataToProcess
   * @param organizeRecalcAndNonRecalcRequests
   */
  public void organizeRecalcAndNonRecalcRequestsEntities(
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcess,
      int[] organizeRecalcAndNonRecalcRequests, boolean recalc) {
    
    Set<GrouperIncrementalDataItem> memberUuidsForEntityOnly = grouperIncrementalDataToProcess.getMemberUuidsForEntityOnly();
    Set<GrouperIncrementalDataItem> memberUuidsForEntityMembershipSync = grouperIncrementalDataToProcess.getMemberUuidsForEntityMembershipSync();
    Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsFieldIdsForMembershipSync = grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync();
  
    int origSize = GrouperUtil.length(memberUuidsForEntityOnly)
      + GrouperUtil.length(memberUuidsForEntityMembershipSync)
      + GrouperUtil.length(groupUuidsMemberUuidsFieldIdsForMembershipSync);
  
    Set<String> memberUuidsForEntityOnlyString = new HashSet<String>();
    
    // add existing, though there shouldnt really be any here yet
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(memberUuidsForEntityOnly)) {
      memberUuidsForEntityOnlyString.add((String)grouperIncrementalDataItem.getItem());
    }
  
    // check for new
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(memberUuidsForEntityMembershipSync)) {
      String entityId = (String)grouperIncrementalDataItem.getItem();
      if (!memberUuidsForEntityOnlyString.contains(entityId)) {
        memberUuidsForEntityOnlyString.add(entityId);
        memberUuidsForEntityOnly.add(new GrouperIncrementalDataItem(entityId, null));
      }
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(groupUuidsMemberUuidsFieldIdsForMembershipSync)) {
      {
        String entityId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(1);
        if (!memberUuidsForEntityOnlyString.contains(entityId)) {
          memberUuidsForEntityOnlyString.add(entityId);
          memberUuidsForEntityOnly.add(new GrouperIncrementalDataItem(entityId, null));
        }
      }
    }
    
    int newSize = GrouperUtil.length(memberUuidsForEntityOnly)
      + GrouperUtil.length(memberUuidsForEntityMembershipSync)
      + GrouperUtil.length(groupUuidsMemberUuidsFieldIdsForMembershipSync);
    
    organizeRecalcAndNonRecalcRequests[0] += newSize-origSize;
  }

  /**
   * convert many membership changes to a group sync
   */
  public void convertToGroupSync() {
    int convertToGroupSyncGroups = 0;
    int convertToGroupSyncMemberships = 0;
    
    int membershipsConvertToGroupSyncThreshold = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipsConvertToGroupSyncThreshold();
        

    // we might convert to group sync for other reasons
    //  if (membershipsConvertToGroupSyncThreshold < 0) {
    //    return;
    //  }
    
    boolean convertAllMembershipChangesToGroupSync = !(GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false));
    
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();

    Map<String, Integer> groupUuidToMembershipCount = new HashMap<String, Integer>();
    Map<String, Long> groupUuidToLatestMillisSince1970 = new HashMap<String, Long>();
    
    //recalc or not
    for (Object grouperIncrementalDataItemObject : new Object[] {grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync(),
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync()}) {
      Set<GrouperIncrementalDataItem> grouperIncrementalDataItemSet = (Set<GrouperIncrementalDataItem>)grouperIncrementalDataItemObject;
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : grouperIncrementalDataItemSet) {
        String groupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
        Integer count = groupUuidToMembershipCount.get(groupId);
        if (count == null) {
          count = 0;
        }
        count++;
        groupUuidToMembershipCount.put(groupId, count);
        
        Long latestMillisSince1970 = groupUuidToLatestMillisSince1970.get(groupId);
        
        // null means we are recalcing
        if (latestMillisSince1970 != null || !groupUuidToLatestMillisSince1970.containsKey(groupId)) {
          
          // if it didnt exist, or if it is about to be null
          if (latestMillisSince1970 == null || grouperIncrementalDataItem.getMillisSince1970() == null || grouperIncrementalDataItem.getMillisSince1970() > latestMillisSince1970) {
            latestMillisSince1970 = grouperIncrementalDataItem.getMillisSince1970();
            groupUuidToLatestMillisSince1970.put(groupId, latestMillisSince1970);
          }
        }
      }
    }

    // lets see whats over the threshold
    for (String groupId : groupUuidToMembershipCount.keySet()) {
      
      int membershipCount = groupUuidToMembershipCount.get(groupId);
      
      if (membershipCount >= membershipsConvertToGroupSyncThreshold || convertAllMembershipChangesToGroupSync) {
       
        convertToGroupSyncGroups++;        
        
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync().add(new GrouperIncrementalDataItem(groupId, groupUuidToLatestMillisSince1970.get(groupId)));
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupOnly().add(new GrouperIncrementalDataItem(groupId, null));
        
        //go through and remove from elsewhere
        Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly().iterator();

        while (iterator.hasNext()) {
          GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
          String currentGroupId = (String)grouperIncrementalDataItem.getItem();
          if (StringUtils.equals(groupId, currentGroupId)) {
            iterator.remove();
          }
        }
        
        iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync().iterator();

        while (iterator.hasNext()) {
          GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
          String currentGroupId = (String)grouperIncrementalDataItem.getItem();
          if (StringUtils.equals(groupId, currentGroupId)) {
            iterator.remove();
          }
        }
        
        iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();

        while (iterator.hasNext()) {
          GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
          String currentGroupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
          if (StringUtils.equals(groupId, currentGroupId)) {
            iterator.remove();
            convertToGroupSyncMemberships++;
          }
        }

      }
      
    }
        
    if (convertToGroupSyncGroups > 0) {
      this.getGrouperProvisioner().getDebugMap().put("convertToGroupSyncGroups", convertToGroupSyncGroups);
      this.getGrouperProvisioner().getDebugMap().put("convertToGroupSyncMemberships", convertToGroupSyncMemberships);
    }

  }


  public void convertToFullSync() {
    
    int convertToFullSyncScore = 0;

    int scoreConvertToFullSyncThreshold = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getScoreConvertToFullSyncThreshold();

    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();

    for (Object grouperIncrementalDataToProcessObject : new Object[] {grouperIncrementalDataToProcessWithoutRecalc,
        grouperIncrementalDataToProcessWithRecalc}) {
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcess = (GrouperIncrementalDataToProcess)grouperIncrementalDataToProcessObject;

      convertToFullSyncScore += 10 * GrouperUtil.length(grouperIncrementalDataToProcess.getGroupUuidsForGroupMembershipSync());
      convertToFullSyncScore += 10 * GrouperUtil.length(grouperIncrementalDataToProcess.getMemberUuidsForEntityMembershipSync());
      convertToFullSyncScore += GrouperUtil.length(grouperIncrementalDataToProcess.getGroupUuidsMemberUuidsForMembershipSync());
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
  
    int[] recalcEventsDuringGroupSync = new int[] {0};
    
    GrouperProvisioningDataIncrementalInput grouperProvisioningDataIncrementalInput = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithoutRecalc();
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = grouperProvisioningDataIncrementalInput.getGrouperIncrementalDataToProcessWithRecalc();
    
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();

    Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsFieldIdsForMembershipSync = 
        grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync();

    Iterator<GrouperIncrementalDataItem> iterator = groupUuidsMemberUuidsFieldIdsForMembershipSync.iterator();
    
    while (iterator.hasNext()) {
      
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      String currentGroupId = (String)((MultiKey)grouperIncrementalDataItem.getItem()).getKey(0);
      
      ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(currentGroupId);
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
      // if there wasnt a timestamp in this message, dont filter
      if (grouperIncrementalDataItem.getMillisSince1970() == null) {
        continue;
      }
      // if there wasnt a last group sync start
      if (gcGrouperSyncGroup == null || gcGrouperSyncGroup.getLastGroupSyncStart() == null || gcGrouperSyncGroup.getLastGroupSync() == null) {
        continue;
      }
      if (gcGrouperSyncGroup.getLastGroupSyncStart().getTime() <= grouperIncrementalDataItem.getMillisSince1970()
          && grouperIncrementalDataItem.getMillisSince1970() <= gcGrouperSyncGroup.getLastGroupSync().getTime()) {
        recalcEventsDuringGroupSync[0]++;
        iterator.remove();
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync().add(grouperIncrementalDataItem);
      }
      
    }
    
    this.getGrouperProvisioner().getDebugMap().put("recalcEventsDuringGroupSync", recalcEventsDuringGroupSync[0]);
  }

  /**
   * if a non recalc action is expected to not change the target, then ignore it
   */
  public void filterUnneededActions() {
    int filterUnneededMemberships = 0;
    
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    
    //go through and remove from elsewhere
    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();

    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      MultiKey groupIdMemberId = (MultiKey)grouperIncrementalDataItem.getItem();
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
      // not sure why this would be null...
      if (provisioningMembershipWrapper == null) {
        continue;
      }
      
      ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      switch (grouperIncrementalDataItem.getGrouperIncrementalDataAction()) {
        case delete:
          
          if (provisioningMembership == null && gcGrouperSyncMembership != null && !gcGrouperSyncMembership.isInTarget()) {
            filterUnneededMemberships++;
            iterator.remove();
            continue;
          }
          
          break;
          
        case insert:

          if (provisioningMembership != null && gcGrouperSyncMembership != null && gcGrouperSyncMembership.isInTarget()) {
            filterUnneededMemberships++;
            iterator.remove();
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
    
    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();

    GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    
    Set<GrouperIncrementalDataItem> groupUuidsMemberUuidsForMembershipSyncWithRecalc = 
        grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync();
    
    //go through and remove from elsewhere
    Iterator<GrouperIncrementalDataItem> iterator = grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync().iterator();

    while (iterator.hasNext()) {
      GrouperIncrementalDataItem grouperIncrementalDataItem = iterator.next();
      MultiKey groupIdMemberId = (MultiKey)grouperIncrementalDataItem.getItem();
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
      // not sure why this would be null...
      if (provisioningMembershipWrapper == null) {
        continue;
      }
      
      ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      switch (grouperIncrementalDataItem.getGrouperIncrementalDataAction()) {
        case delete:
          
          if (!provisioningMembershipWrapper.isDelete() && provisioningMembership != null) {
            convertInconsistentEventsToRecalc++;
            iterator.remove();
            groupUuidsMemberUuidsForMembershipSyncWithRecalc.add(grouperIncrementalDataItem);
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

          if (provisioningMembershipWrapper.isDelete() || provisioningMembership == null) {
            convertInconsistentEventsToRecalc++;
            iterator.remove();
            groupUuidsMemberUuidsForMembershipSyncWithRecalc.add(grouperIncrementalDataItem);
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
    
    int copyIncrementalStateToWrappersMissing = 0;
    
    Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper();
    Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper();
    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();

    {
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc();
  
      // ########### First do recalcs...
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupMembershipSync())) {
        String groupId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningGroupWrapper.setIncrementalSyncMemberships(true);
        provisioningGroupWrapper.setRecalc(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsForGroupOnly())) {
        String groupId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningGroupWrapper.setRecalc(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityMembershipSync())) {
        String memberId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningEntityWrapper.setIncrementalSyncMemberships(true);
        provisioningEntityWrapper.setRecalc(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithRecalc.getMemberUuidsForEntityOnly())) {
        String memberId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningEntityWrapper.setRecalc(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithRecalc.getGroupUuidsMemberUuidsForMembershipSync())) {
        MultiKey groupIdMemberId = (MultiKey)grouperIncrementalDataItem.getItem();
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
        if (provisioningMembershipWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningMembershipWrapper.setRecalc(true);
  
      }
    }
    
    {
      GrouperIncrementalDataToProcess grouperIncrementalDataToProcessWithoutRecalc = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
  
      // ########### Then do nonrecalcs...
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupMembershipSync())) {
        String groupId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        provisioningGroupWrapper.setIncrementalSyncMemberships(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsForGroupOnly())) {
        String groupId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupId);
        if (provisioningGroupWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityMembershipSync())) {
        String memberId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        provisioningEntityWrapper.setIncrementalSyncMemberships(true);
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithoutRecalc.getMemberUuidsForEntityOnly())) {
        String memberId = (String)grouperIncrementalDataItem.getItem();
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberId);
        if (provisioningEntityWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
  
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : GrouperUtil.nonNull(grouperIncrementalDataToProcessWithoutRecalc.getGroupUuidsMemberUuidsForMembershipSync())) {
        MultiKey groupIdMemberId = (MultiKey)grouperIncrementalDataItem.getItem();
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
        if (provisioningMembershipWrapper == null) {
          // why would this happen?
          copyIncrementalStateToWrappersMissing++;
          continue;
        }
        
        if (!provisioningMembershipWrapper.isRecalc() && provisioningMembershipWrapper.getGrouperIncrementalDataAction() == null) {
          provisioningMembershipWrapper.setGrouperIncrementalDataAction(grouperIncrementalDataItem.getGrouperIncrementalDataAction());
        }
      }
    }
    
    if (copyIncrementalStateToWrappersMissing > 0) {
      this.getGrouperProvisioner().getDebugMap().put("copyIncrementalStateToWrappersMissing", copyIncrementalStateToWrappersMissing);
    }
  }

  
  public void retrieveIncrementalTargetData() {
    TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncrementalDataRequest = new TargetDaoRetrieveIncrementalDataRequest();
    this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().setTargetDaoRetrieveIncrementalDataRequest(targetDaoRetrieveIncrementalDataRequest);
    boolean needsData = false;
    
    {
      List<ProvisioningGroup> grouperTargetGroupsRecalcForMembershipSync = new ArrayList<ProvisioningGroup>();
      List<ProvisioningGroup> grouperTargetGroupsRecalcForGroupOnly = new ArrayList<ProvisioningGroup>();

      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (provisioningGroupWrapper.getGrouperTargetGroup() != null) {
          if (provisioningGroupWrapper.isRecalc()) {
            if (provisioningGroupWrapper.isIncrementalSyncMemberships()) {
              grouperTargetGroupsRecalcForMembershipSync.add(provisioningGroupWrapper.getGrouperTargetGroup());
            } else {
              grouperTargetGroupsRecalcForGroupOnly.add(provisioningGroupWrapper.getGrouperTargetGroup());
            }
          } else {
            // TODO look at "state" here too
            if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
              // we need to retrieve or create this, its probably already a recalc but...
              grouperTargetGroupsRecalcForGroupOnly.add(provisioningGroupWrapper.getGrouperTargetGroup());
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

      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        if (provisioningEntityWrapper.getGrouperTargetEntity() != null) {
          if (provisioningEntityWrapper.isRecalc()) {
            if (provisioningEntityWrapper.isIncrementalSyncMemberships()) {
              grouperTargetEntitiesRecalcForMembershipSync.add(provisioningEntityWrapper.getGrouperTargetEntity());
            } else {
              grouperTargetEntitiesRecalcForEntityOnly.add(provisioningEntityWrapper.getGrouperTargetEntity());
            }
          } else {
            // TODO look at "state" here too
            if (provisioningEntityWrapper.getGcGrouperSyncMember() == null || !provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
              // we need to retrieve or create this, its probably already a recalc but...
              grouperTargetEntitiesRecalcForEntityOnly.add(provisioningEntityWrapper.getGrouperTargetEntity());
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
      
      switch(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        
        case membershipObjects:

          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
            if (provisioningMembershipWrapper.getGrouperTargetMembership() != null && provisioningMembershipWrapper.isRecalc()) {
              ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
              ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
              ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
              ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
              ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();

              if ((provisioningGroupWrapper != null && provisioningGroupWrapper.isIncrementalSyncMemberships())
                  || (provisioningEntityWrapper != null && provisioningEntityWrapper.isIncrementalSyncMemberships())) {
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
              if (provisioningMembershipWrapper.isRecalc()) {
                ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
                ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
    
                if ((provisioningGroupWrapper != null && provisioningGroupWrapper.isIncrementalSyncMemberships())
                    || (provisioningEntityWrapper != null && provisioningEntityWrapper.isIncrementalSyncMemberships())) {
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
              if (provisioningMembershipWrapper.isRecalc()) {
                ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
                ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
                ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
                ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
                ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
    
                if ((provisioningGroupWrapper != null && provisioningGroupWrapper.isIncrementalSyncMemberships())
                    || (provisioningEntityWrapper != null && provisioningEntityWrapper.isIncrementalSyncMemberships())) {
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
      if (provisioningObjectsRecalcForMembershipSync.size() > 0) {
        provisioningObjectsRecalcForMembershipSync = new ArrayList<Object>(new HashSet<Object>(provisioningObjectsRecalcForMembershipSync));
        targetDaoRetrieveIncrementalDataRequest.setTargetMembershipObjectsForMembershipSync(provisioningObjectsRecalcForMembershipSync);
        needsData = true;
      }
      {
        Set<Object> groupMatchingIdsToRetrieve = new HashSet<Object>();
        Set<Object> entityMatchingIdsToRetrieve = new HashSet<Object>();
        for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupMembershipSync()) ) {
          groupMatchingIdsToRetrieve.add(grouperTargetGroup.getMatchingId());
        }
        for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly()) ) {
          groupMatchingIdsToRetrieve.add(grouperTargetGroup.getMatchingId());
        }
        for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityMembershipSync())) {
          entityMatchingIdsToRetrieve.add(grouperTargetEntity.getMatchingId());
        }
        for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly())) {
          entityMatchingIdsToRetrieve.add(grouperTargetEntity.getMatchingId());
        }
        
        // we need to add groups that are there for entity recalcs, and entities there for group recalcs, and both for membership recalcs
        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
          boolean retrieveGroupAndMember = false;
          if (provisioningMembershipWrapper.isRecalc()) {
            retrieveGroupAndMember = true;
          }
          ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
          ProvisioningGroup grouperProvisioningGroup = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningGroup();
          ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getProvisioningGroupWrapper();
          ProvisioningEntity grouperProvisioningEntity = grouperProvisioningMembership == null ? null : grouperProvisioningMembership.getProvisioningEntity();
          ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.getProvisioningEntityWrapper();
          if (provisioningGroupWrapper != null && provisioningGroupWrapper.isRecalc()) {
            retrieveGroupAndMember = true;
          }
          if (provisioningEntityWrapper != null && provisioningEntityWrapper.isRecalc()) {
            retrieveGroupAndMember = true;
          }
          if (retrieveGroupAndMember) {
            {
              Object groupMatchingId = provisioningGroupWrapper == null || provisioningGroupWrapper.getGrouperTargetGroup() == null ? null : provisioningGroupWrapper.getGrouperTargetGroup().getMatchingId();
              if (groupMatchingId != null && !groupMatchingIdsToRetrieve.contains(groupMatchingId) && provisioningGroupWrapper.getGrouperTargetGroup() != null) {
                if (targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly() == null) {
                  targetDaoRetrieveIncrementalDataRequest.setTargetGroupsForGroupOnly(new ArrayList<ProvisioningGroup>());
                }
                needsData = true;
                targetDaoRetrieveIncrementalDataRequest.getTargetGroupsForGroupOnly().add(provisioningGroupWrapper.getGrouperTargetGroup());
              }
            }
            {
              Object entityMatchingId = provisioningEntityWrapper == null || provisioningEntityWrapper.getGrouperTargetEntity() == null ? null : provisioningEntityWrapper.getGrouperTargetEntity().getMatchingId();
              if (entityMatchingId != null && !entityMatchingIdsToRetrieve.contains(entityMatchingId) && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
                if (targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly() == null) {
                  targetDaoRetrieveIncrementalDataRequest.setTargetEntitiesForEntityOnly(new ArrayList<ProvisioningEntity>());
                }
                needsData = true;
                targetDaoRetrieveIncrementalDataRequest.getTargetEntitiesForEntityOnly().add(provisioningEntityWrapper.getGrouperTargetEntity());
              }
            }
          }
        }        
      }
    }
    if (!needsData) {
      return;
    }
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningLogicIncremental().filterNonRecalcActionsCapturedByRecalc();
    
    TargetDaoRetrieveIncrementalDataResponse targetDaoRetrieveIncrementalDataResponse 
      = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveIncrementalData(targetDaoRetrieveIncrementalDataRequest);
    
    GrouperProvisioningLists result = this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects();

    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups()) > 0) {
      List<ProvisioningGroup> provisioningGroups = result.getProvisioningGroups();
      if (provisioningGroups == null) {
        provisioningGroups = new ArrayList<ProvisioningGroup>();
        result.setProvisioningGroups(provisioningGroups);
      }          
      provisioningGroups.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningGroups());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities()) > 0) {
      List<ProvisioningEntity> provisioningEntities = result.getProvisioningEntities();
      if (provisioningEntities == null) {
        provisioningEntities = new ArrayList<ProvisioningEntity>();
        result.setProvisioningEntities(provisioningEntities);
      }          
      provisioningEntities.addAll(targetDaoRetrieveIncrementalDataResponse.getProvisioningEntities());
    }
    if (GrouperUtil.length(targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) > 0) {
      
      switch(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        
        case membershipObjects:

          List<ProvisioningMembership> provisioningMemberships = result.getProvisioningMemberships();
          if (provisioningMemberships == null) {
            provisioningMemberships = new ArrayList<ProvisioningMembership>();
            result.setProvisioningMemberships(provisioningMemberships);
          }
          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            provisioningMemberships.add((ProvisioningMembership)object);
          }
          
          break;

        case entityAttributes:

          List<ProvisioningEntity> provisioningEntities = result.getProvisioningEntities();
          if (provisioningEntities == null) {
            provisioningEntities = new ArrayList<ProvisioningEntity>();
            result.setProvisioningEntities(provisioningEntities);
          }
          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            provisioningEntities.add((ProvisioningEntity)object);
          }

          break;
          
        case groupAttributes:
          
          List<ProvisioningGroup> provisioningGroups = result.getProvisioningGroups();
          if (provisioningGroups == null) {
            provisioningGroups = new ArrayList<ProvisioningGroup>();
            result.setProvisioningGroups(provisioningGroups);
          }
          for (Object object : targetDaoRetrieveIncrementalDataResponse.getProvisioningMemberships()) {
            provisioningGroups.add((ProvisioningGroup)object);
          }

          break;
          
        default: 
          throw new RuntimeException("Not expecting GrouperProvisioningBehaviorMembershipType: "
              + this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType());
      }
    }
    
  }


  

}
