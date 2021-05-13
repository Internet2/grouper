package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectAttributes;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembershipWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * Sync up provisioning attributes with grouper group sync provisionable attributes
 */
public class ProvisioningSyncIntegration {

  public ProvisioningSyncIntegration() {
    
  }

  public static void fullSyncGroups(ProvisioningSyncResult provisioningSyncGroupResult, GcGrouperSync gcGrouperSync,
      List<GcGrouperSyncGroup> initialGcGrouperSyncGroups, Map<String, GrouperProvisioningObjectAttributes> groupUuidToProvisioningObjectAttributes) {

    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }

    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();

    initialGcGrouperSyncGroups = GrouperUtil.nonNull(initialGcGrouperSyncGroups);

    for (GcGrouperSyncGroup gcGrouperSyncGroup : initialGcGrouperSyncGroups) {
      groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }

    int removeSyncRowsAfterSecondsOutOfTarget = GrouperLoaderConfig.retrieveConfig().propertyValueInt(
        "grouper.provisioning.removeSyncRowsAfterSecondsOutOfTarget", 60*60*24*7);

    provisioningSyncGroupResult.setGcGrouperSync(gcGrouperSync);
    
    // start group ids to insert with all group ids minus those which have sync group objects already
    Set<String> groupIdsToInsert = new HashSet<String>(groupUuidToProvisioningObjectAttributes.keySet());
    provisioningSyncGroupResult.setGroupIdsToInsert(groupIdsToInsert);
    groupIdsToInsert.removeAll(groupUuidToSyncGroup.keySet());
    
    Set<String> groupIdsToUpdate = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsToUpdate(groupIdsToUpdate);

    List<GcGrouperSyncGroup> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncGroup>();
    
    Set<String> groupIdsWithChangedIdIndexes = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsWithChangedIdIndexes(groupIdsWithChangedIdIndexes);

    Set<String> groupIdsWithChangedNames = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsWithChangedNames(groupIdsWithChangedNames);
    

    // lets remove ones that dont need to be there
    if (GrouperUtil.length(groupUuidToSyncGroup) > 0) {
      
      // make an array list so we can remove from the map without exception
      List<GcGrouperSyncGroup> gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>(groupUuidToSyncGroup.values());
      
      for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
        
        GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = groupUuidToProvisioningObjectAttributes.get(gcGrouperSyncGroup.getGroupId());

        String newGroupName = grouperProvisioningObjectAttributes == null ? null : grouperProvisioningObjectAttributes.getName();
        Long newGroupIdIndex = grouperProvisioningObjectAttributes == null ? null : grouperProvisioningObjectAttributes.getIdIndex();
        String newMetadataJson = grouperProvisioningObjectAttributes == null ? null : grouperProvisioningObjectAttributes.getProvisioningMetadataJson();
        boolean groupIsProvisionable = grouperProvisioningObjectAttributes != null;
        
        gcGrouperSyncGroup.setMetadataJson(newMetadataJson);
        
        processSyncGroup(groupUuidToSyncGroup,
            removeSyncRowsAfterSecondsOutOfTarget, groupIdsToInsert, groupIdsToUpdate,
            gcGrouperSyncRowsToDeleteFromDatabase, groupIdsWithChangedIdIndexes,
            groupIdsWithChangedNames, gcGrouperSyncGroup, grouperProvisioningObjectAttributes,
            newGroupName, newGroupIdIndex, newMetadataJson, groupIsProvisionable);
      }

      gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncRowsToDeleteFromDatabase, true, true);
    }
    
    if (GrouperUtil.length(groupIdsToInsert) > 0) {
      
      Map<String, GcGrouperSyncGroup> mapGroupIdToSyncGroupInsert = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupIds(groupIdsToInsert);
      
      for (String groupIdToInsert : mapGroupIdToSyncGroupInsert.keySet()) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = mapGroupIdToSyncGroupInsert.get(groupIdToInsert);
        initialGcGrouperSyncGroups.add(gcGrouperSyncGroup);
        GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes = groupUuidToProvisioningObjectAttributes.get(groupIdToInsert);
        
        if (grouperProvisioningObjectAttributes == null) {
          continue;
        }
        String groupName = grouperProvisioningObjectAttributes.getName();
        Long groupIdIndex = grouperProvisioningObjectAttributes.getIdIndex();
        String groupMetadataJson = grouperProvisioningObjectAttributes.getProvisioningMetadataJson();

        processSyncGroupInsert(gcGrouperSync, groupUuidToSyncGroup, groupIdToInsert,
            gcGrouperSyncGroup, groupName, groupIdIndex, groupMetadataJson);
      }
      
    }
    
    Set<String> groupIdsToDelete = new HashSet<String>(groupUuidToSyncGroup.keySet());
    
    provisioningSyncGroupResult.setGroupIdsToDelete(groupIdsToDelete);
    
    groupIdsToDelete.removeAll(groupUuidToProvisioningObjectAttributes.keySet());
    
    processSyncGroupDelete(groupUuidToSyncGroup, groupIdsToDelete);
    
  }

  public static void processSyncGroupDelete(
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup,
      Set<String> groupIdsToDelete) {
    if (GrouperUtil.length(groupIdsToDelete) > 0) {

      Iterator<String> groupIdToDeleteIterator = groupIdsToDelete.iterator();
      
      while (groupIdToDeleteIterator.hasNext()) {
        
        String groupIdToDelete = groupIdToDeleteIterator.next();
        
        GcGrouperSyncGroup gcGrouperSyncGroup = groupUuidToSyncGroup.get(groupIdToDelete);
        
        if (gcGrouperSyncGroup == null) {
          throw new RuntimeException("why is gcGrouperSyncGroup null???");
        }

        if (gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.getProvisionableEnd() == null) {
          gcGrouperSyncGroup.setProvisionable(false);
          gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
        }
        
        // if we arent in target, dont worry about it
        if (!gcGrouperSyncGroup.isInTarget() ) {
          groupIdToDeleteIterator.remove();
          groupUuidToSyncGroup.remove(gcGrouperSyncGroup.getGroupId());
        }
        
      }
      
    }
  }

  public static void processSyncGroupInsert(GcGrouperSync gcGrouperSync,
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup, String groupIdToInsert,
      GcGrouperSyncGroup gcGrouperSyncGroup, String groupName,
      Long groupIdIndex, String metadataJson) {
    if (gcGrouperSyncGroup == null) {
      gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId(groupIdToInsert);
    }
    gcGrouperSyncGroup.setGroupName(groupName);
    gcGrouperSyncGroup.setGroupIdIndex(groupIdIndex);
    gcGrouperSyncGroup.setMetadataJson(metadataJson);
    gcGrouperSyncGroup.setProvisionable(true);
    gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
    groupUuidToSyncGroup.put(groupIdToInsert, gcGrouperSyncGroup);    
  }

  public static void processSyncGroup(
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup,
      int removeSyncRowsAfterSecondsOutOfTarget, Set<String> groupIdsToInsert,
      Set<String> groupIdsToUpdate,
      List<GcGrouperSyncGroup> gcGrouperSyncRowsToDeleteFromDatabase,
      Set<String> groupIdsWithChangedIdIndexes, Set<String> groupIdsWithChangedNames,
      GcGrouperSyncGroup gcGrouperSyncGroup,
      GrouperProvisioningObjectAttributes grouperProvisioningObjectAttributes, String newGroupName,
      Long newGroupIdIndex, String newMetadataJson, boolean groupIsProvisionable) {
    
//    {
//      // is in grouper?
//      Boolean inGrouper = null;
//      if (inGrouper == null && provisioningGroupWrapper != null && provisioningGroupWrapper.isDelete()) {
//        inGrouper = false;
//      }
//      if (inGrouper == null && provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
//        inGrouper = true;
//      }
//      if (inGrouper == null && groupIsProvisionable) {
//        inGrouper = true;
//      }
//      if (inGrouper == null) {
//        inGrouper = false;
//      }
//      if (gcGrouperSyncGroup.getInGrouper() != inGrouper) {
//        if (gcGrouperSyncGroup.getInGrouperInsertOrExistsDb() == null) {
//          gcGrouperSyncGroup.setInTargetInsertOrExists(false);
//        }
//        gcGrouperSyncGroup.setInGrouper(inGrouper);
//        if (inGrouper) {
//          gcGrouperSyncGroup.setInGrouperStart(new Timestamp(System.currentTimeMillis()));
//        } else {
//          gcGrouperSyncGroup.setInGrouperEnd(new Timestamp(System.currentTimeMillis()));
//        }
//      }
//    }
//    
    // keep it
    if (groupIsProvisionable || gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.isInTarget()) {
      
      // see if needs to update
      {
        if (!StringUtils.equals(newGroupName, gcGrouperSyncGroup.getGroupName())) {
          groupIdsWithChangedNames.add(gcGrouperSyncGroup.getGroupId());
        }
      }
      
      {
        if (!GrouperUtil.equals(newGroupIdIndex, gcGrouperSyncGroup.getGroupIdIndex())) {
          groupIdsWithChangedIdIndexes.add(gcGrouperSyncGroup.getGroupId());
        }
      }

      // see if not provisionable
      if (!gcGrouperSyncGroup.isProvisionable() && groupIsProvisionable) {
        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        gcGrouperSyncGroup.setProvisionableEnd(null);
        gcGrouperSyncGroup.setProvisionable(true);
      }
      if (gcGrouperSyncGroup.isProvisionable() && !groupIsProvisionable) {
        gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
        gcGrouperSyncGroup.setProvisionable(false);
      }

      // see if not provisionable
      if (!gcGrouperSyncGroup.isInTarget() && groupIsProvisionable) {
        groupIdsToInsert.add(gcGrouperSyncGroup.getGroupId());
      }
        
      if (gcGrouperSyncGroup.dbVersionDifferent()) {
        groupIdsToUpdate.add(gcGrouperSyncGroup.getGroupId());
      }
      
    }
    
    groupUuidToSyncGroup.remove(gcGrouperSyncGroup.getGroupId());

    //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
    if (!gcGrouperSyncGroup.isInTarget() && !gcGrouperSyncGroup.isProvisionable() && gcGrouperSyncGroup.getInTargetEnd() != null) {
      long targetEndMillis = gcGrouperSyncGroup.getInTargetEnd() == null ? 0 : gcGrouperSyncGroup.getInTargetEnd().getTime();
      targetEndMillis = Math.max(targetEndMillis, gcGrouperSyncGroup.getProvisionableEnd() == null ? 0 : gcGrouperSyncGroup.getProvisionableEnd().getTime());
      targetEndMillis = Math.max(targetEndMillis, gcGrouperSyncGroup.getLastUpdated() == null ? 0 : gcGrouperSyncGroup.getLastUpdated().getTime());
      if (targetEndMillis != 0 &&( (System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget)) {
        gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncGroup);
      }
    }
  }

  public static void fullSyncMembers(ProvisioningSyncResult provisioningSyncResult, GcGrouperSync gcGrouperSync,
      List<GcGrouperSyncMember> initialGcGrouperSyncMembers, Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper) {
  
    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }
  
    initialGcGrouperSyncMembers = GrouperUtil.nonNull(initialGcGrouperSyncMembers);

    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();

    for (GcGrouperSyncMember gcGrouperSyncMember : initialGcGrouperSyncMembers) {
      memberUuidToSyncMember.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
    }

    int removeSyncRowsAfterSecondsOutOfTarget = GrouperLoaderConfig.retrieveConfig().propertyValueInt(
        "grouper.provisioning.removeSyncRowsAfterSecondsOutOfTarget", 60*60*24*7);
  
    provisioningSyncResult.setGcGrouperSync(gcGrouperSync);
    
    // start group ids to insert with all group ids minus those which have sync group objects already
    Set<String> memberIdsToInsert = new HashSet<String>(memberUuidToProvisioningEntityWrapper.keySet());
    provisioningSyncResult.setMemberIdsToInsert(memberIdsToInsert);
    memberIdsToInsert.removeAll(memberUuidToSyncMember.keySet());
    
    Set<String> memberIdsToUpdate = new HashSet<String>();
    provisioningSyncResult.setMemberIdsToUpdate(memberIdsToUpdate);
  
    List<GcGrouperSyncMember> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncMember>();
    
    Set<String> memberIdsWithChangedSubjectIds = new HashSet<String>();
    provisioningSyncResult.setMemberIdsWithChangedSubjectIds(memberIdsWithChangedSubjectIds);
  
    // lets remove ones that dont need to be there
    if (GrouperUtil.length(memberUuidToSyncMember) > 0) {
      
      // make an array list so we can remove from the map without exception
      List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>(memberUuidToSyncMember.values());
      
      for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
        
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(gcGrouperSyncMember.getMemberId());
  
        ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperProvisioningEntity();
  
        // keep it
        if (grouperProvisioningEntity != null || gcGrouperSyncMember.isProvisionable() || gcGrouperSyncMember.isInTarget()) {
          
          // see if needs to update
          {
            String newSubjectId = grouperProvisioningEntity == null ? null : grouperProvisioningEntity.retrieveAttributeValueString("subjectId");
            if (!StringUtils.equals(newSubjectId, gcGrouperSyncMember.getSubjectId())) {
              memberIdsWithChangedSubjectIds.add(gcGrouperSyncMember.getMemberId());
            }
          }
          

          // see if not provisionable
          if (!gcGrouperSyncMember.isProvisionable() && grouperProvisioningEntity != null
              && (provisioningEntityWrapper == null || !provisioningEntityWrapper.isDelete())) {
            gcGrouperSyncMember.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncMember.setProvisionableEnd(null);
            gcGrouperSyncMember.setProvisionable(true);
          }
          if (gcGrouperSyncMember.isProvisionable() && grouperProvisioningEntity == null
              && (provisioningEntityWrapper == null || provisioningEntityWrapper.isDelete())) {
            gcGrouperSyncMember.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncMember.setProvisionable(false);
          }
  
          // see if not provisionable
          if (!gcGrouperSyncMember.isInTarget() && grouperProvisioningEntity != null
              && (provisioningEntityWrapper == null || !provisioningEntityWrapper.isDelete())) {
            memberIdsToInsert.add(gcGrouperSyncMember.getMemberId());
          }
            
          if (gcGrouperSyncMember.dbVersionDifferent()) {
            memberIdsToUpdate.add(gcGrouperSyncMember.getMemberId());
          }
          
          continue;
        }
        
        memberUuidToSyncMember.remove(gcGrouperSyncMember.getMemberId());
        
        if (!gcGrouperSyncMember.isInTarget() && !gcGrouperSyncMember.isProvisionable() && gcGrouperSyncMember.getInTargetEnd() != null) {
          long targetEndMillis = gcGrouperSyncMember.getInTargetEnd() == null ? 0 : gcGrouperSyncMember.getInTargetEnd().getTime();
          targetEndMillis = Math.max(targetEndMillis, gcGrouperSyncMember.getProvisionableEnd() == null ? 0 : gcGrouperSyncMember.getProvisionableEnd().getTime());
          targetEndMillis = Math.max(targetEndMillis, gcGrouperSyncMember.getLastUpdated() == null ? 0 : gcGrouperSyncMember.getLastUpdated().getTime());
          //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
          if (targetEndMillis != 0 &&( (System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget)) {
            gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncMember);
          }
        }
      }
  
      gcGrouperSync.getGcGrouperSyncMemberDao().memberDelete(gcGrouperSyncRowsToDeleteFromDatabase, true, true);
    }
    
    if (GrouperUtil.length(memberIdsToInsert) > 0) {
      
      Map<String, GcGrouperSyncMember> mapMemberIdToSyncMemberInsert = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberIds(memberIdsToInsert);
      
      for (String memberIdToInsert : mapMemberIdToSyncMemberInsert.keySet()) {
        
        GcGrouperSyncMember gcGrouperSyncMember = mapMemberIdToSyncMemberInsert.get(memberIdToInsert);
        ProvisioningEntityWrapper provisioningEntityWrapper = memberUuidToProvisioningEntityWrapper.get(memberIdToInsert);
        ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGrouperProvisioningEntity();
        
        if (grouperProvisioningEntity == null) {
          continue;
        }
        if (gcGrouperSyncMember == null) {
          gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberCreateByMemberId(memberIdToInsert);
        }
        gcGrouperSyncMember.setSourceId(grouperProvisioningEntity.retrieveAttributeValueString("subjectSourceId"));
        gcGrouperSyncMember.setSubjectId(grouperProvisioningEntity.getSubjectId());
        gcGrouperSyncMember.setSubjectIdentifier(grouperProvisioningEntity.retrieveAttributeValueString("subjectIdentifier0"));
        gcGrouperSyncMember.setProvisionable(true);
        gcGrouperSyncMember.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        memberUuidToSyncMember.put(memberIdToInsert, gcGrouperSyncMember);
        provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);

      }
      
    }
    
    Set<String> memberIdsToDelete = new HashSet<String>(memberUuidToSyncMember.keySet());
    
    provisioningSyncResult.setMemberIdsToDelete(memberIdsToDelete);
    
    memberIdsToDelete.removeAll(memberUuidToProvisioningEntityWrapper.keySet());
    
    if (GrouperUtil.length(memberIdsToDelete) > 0) {
  
      Iterator<String> memberIdToDeleteIterator = memberIdsToDelete.iterator();
      
      while (memberIdToDeleteIterator.hasNext()) {
        
        String memberIdToDelete = memberIdToDeleteIterator.next();
        
        GcGrouperSyncMember gcGrouperSyncMember = memberUuidToSyncMember.get(memberIdToDelete);
        
        if (gcGrouperSyncMember == null) {
          throw new RuntimeException("why is gcGrouperSyncMember null???");
        }
  
        if (gcGrouperSyncMember.isProvisionable() || gcGrouperSyncMember.getProvisionableEnd() == null) {
          gcGrouperSyncMember.setProvisionable(false);
          gcGrouperSyncMember.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
        }
        
        // if we arent in target, dont worry about it
        if (!gcGrouperSyncMember.isInTarget() ) {
          memberIdToDeleteIterator.remove();
          memberUuidToSyncMember.remove(gcGrouperSyncMember.getMemberId());
        }
        
      }
      
    }
    
  }

  public static void fullSyncMemberships(ProvisioningSyncResult provisioningSyncResult, GcGrouperSync gcGrouperSync,
      List<GcGrouperSyncGroup> initialGcGrouperSyncGroups, List<GcGrouperSyncMember> initialGcGrouperSyncMembers,
      List<GcGrouperSyncMembership> initialGcGrouperSyncMemberships, Map<MultiKey, 
      ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper) {
  
    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }
  
    initialGcGrouperSyncMemberships = GrouperUtil.nonNull(initialGcGrouperSyncMemberships);

    Map<String, GcGrouperSyncGroup> groupSyncIdToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(initialGcGrouperSyncGroups)) {
      groupSyncIdToSyncGroup.put(gcGrouperSyncGroup.getId(), gcGrouperSyncGroup);
    }
    Map<String, GcGrouperSyncMember> memberSyncIdToSyncMember = new HashMap<String, GcGrouperSyncMember>();
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(initialGcGrouperSyncMembers)) {
      memberSyncIdToSyncMember.put(gcGrouperSyncMember.getId(), gcGrouperSyncMember);
    }

    Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
    
    for (GcGrouperSyncMembership gcGrouperSyncMembership : initialGcGrouperSyncMemberships) {
      
      GcGrouperSyncGroup gcGrouperSyncGroup = groupSyncIdToSyncGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
      GcGrouperSyncMember gcGrouperSyncMember = memberSyncIdToSyncMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
      if (gcGrouperSyncGroup != null && gcGrouperSyncMember != null) {
        groupIdMemberIdToSyncMembership.put(
            new MultiKey(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncMember.getMemberId()), gcGrouperSyncMembership);
      }
    }

    int removeSyncRowsAfterSecondsOutOfTarget = GrouperLoaderConfig.retrieveConfig().propertyValueInt(
        "grouper.provisioning.removeSyncRowsAfterSecondsOutOfTarget", 60*60*24*7);
  
    provisioningSyncResult.setGcGrouperSync(gcGrouperSync);
    
    // start group ids to insert with all group ids minus those which have sync group objects already
    Set<MultiKey> groupIdMemberIdsToInsert = new HashSet<MultiKey>(groupIdMemberIdToProvisioningMembershipWrapper.keySet());
    provisioningSyncResult.setMembershipGroupIdMemberIdsToInsert(groupIdMemberIdsToInsert);
    groupIdMemberIdsToInsert.removeAll(groupIdMemberIdToSyncMembership.keySet());
    
    Set<MultiKey> groupIdMemberIdsToUpdate = new HashSet<MultiKey>();
    provisioningSyncResult.setMembershipGroupIdMemberIdsToUpdate(groupIdMemberIdsToUpdate);
  
    List<GcGrouperSyncMembership> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncMembership>();
    
    // lets remove ones that dont need to be there
    if (GrouperUtil.length(groupIdMemberIdToSyncMembership) > 0) {
      Set<MultiKey> groupIdMemberIds = new HashSet<MultiKey>(groupIdMemberIdToSyncMembership.keySet());
      for (MultiKey groupIdMemberId : groupIdMemberIds) {

        GcGrouperSyncMembership gcGrouperSyncMembership = groupIdMemberIdToSyncMembership.get(groupIdMemberId);

        GcGrouperSyncGroup gcGrouperSyncGroup = groupSyncIdToSyncGroup.get(gcGrouperSyncMembership.getGrouperSyncGroupId());
        
        GcGrouperSyncMember gcGrouperSyncMember = memberSyncIdToSyncMember.get(gcGrouperSyncMembership.getGrouperSyncMemberId());
        
        // not sure why this would happen, i guess if a group aged out and this is already removed????
        if (gcGrouperSyncGroup == null || gcGrouperSyncMember == null) {
          continue;
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupIdMemberIdToProvisioningMembershipWrapper.get(groupIdMemberId);
  
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGrouperProvisioningMembership();
  
        // keep it
        boolean membershipProvisionable = gcGrouperSyncGroup.isProvisionable() && gcGrouperSyncMember.isProvisionable();
                
        if (!membershipProvisionable && grouperProvisioningMembership != null
            && (provisioningMembershipWrapper == null || !provisioningMembershipWrapper.isDelete())) {
          gcGrouperSyncMember.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
          gcGrouperSyncMember.setProvisionableEnd(null);
          gcGrouperSyncMember.setProvisionable(true);
        }
        if (membershipProvisionable && grouperProvisioningMembership == null
            && (provisioningMembershipWrapper == null || provisioningMembershipWrapper.isDelete())) {
          gcGrouperSyncMember.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
          gcGrouperSyncMember.setProvisionable(false);
        }

        if (grouperProvisioningMembership != null || membershipProvisionable || gcGrouperSyncMembership.isInTarget()) {
  
          // see if not provisionable
          if (!gcGrouperSyncMembership.isInTarget() && grouperProvisioningMembership != null
              && (provisioningMembershipWrapper == null || provisioningMembershipWrapper.isDelete())) {
            groupIdMemberIdsToInsert.add(groupIdMemberId);
          }
            
          if (gcGrouperSyncMembership.dbVersionDifferent()) {
            groupIdMemberIdsToUpdate.add(groupIdMemberId);
          }
          
          continue;
        }
        
        groupIdMemberIdToSyncMembership.remove(groupIdMemberId);
  

        if (!gcGrouperSyncMembership.isInTarget() && gcGrouperSyncMembership.getInTargetEnd() != null) {
          //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
          long targetEndMillis = gcGrouperSyncMembership.getInTargetEnd() == null ? 0 : gcGrouperSyncMembership.getInTargetEnd().getTime();
          targetEndMillis = Math.max(targetEndMillis, gcGrouperSyncMembership.getLastUpdated() == null ? 0 : gcGrouperSyncMembership.getLastUpdated().getTime());
          //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
          if (targetEndMillis != 0 &&( (System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget)) {
            gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncMembership);
          }
        }
      }
  
      gcGrouperSync.getGcGrouperSyncMembershipDao().membershipDelete(gcGrouperSyncRowsToDeleteFromDatabase, true);
    }
    
    if (GrouperUtil.length(groupIdMemberIdsToInsert) > 0) {
      
      Map<MultiKey, GcGrouperSyncMembership> mapGroupIdMemberIdToSyncMembershipInsert = gcGrouperSync.getGcGrouperSyncMembershipDao()
          .membershipRetrieveOrCreateByGroupIdsAndMemberIds(groupIdMemberIdsToInsert);
      
      for (MultiKey groupIdMemberIdToInsert : mapGroupIdMemberIdToSyncMembershipInsert.keySet()) {
        
        GcGrouperSyncMembership gcGrouperSyncMembership = mapGroupIdMemberIdToSyncMembershipInsert.get(groupIdMemberIdToInsert);
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupIdMemberIdToProvisioningMembershipWrapper.get(groupIdMemberIdToInsert);
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGrouperProvisioningMembership();
        
        if (grouperProvisioningMembership == null) {
          continue;
        }
        if (gcGrouperSyncMembership == null) {
          gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipCreateBySyncGroupIdAndSyncMemberId((String)groupIdMemberIdToInsert.getKey(0),
              (String)groupIdMemberIdToInsert.getKey(1));
        }
        groupIdMemberIdToSyncMembership.put(groupIdMemberIdToInsert, gcGrouperSyncMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

      }
      
    }
    
    Set<MultiKey> groupIdMemberIdsToDelete = new HashSet<MultiKey>(groupIdMemberIdToSyncMembership.keySet());
    
    provisioningSyncResult.setMembershipGroupIdMemberIdsToDelete(groupIdMemberIdsToDelete);
    
    groupIdMemberIdsToDelete.removeAll(groupIdMemberIdToProvisioningMembershipWrapper.keySet());
    
    if (GrouperUtil.length(groupIdMemberIdsToDelete) > 0) {
  
      Iterator<MultiKey> groupIdMemberIdToDeleteIterator = groupIdMemberIdsToDelete.iterator();
      
      while (groupIdMemberIdToDeleteIterator.hasNext()) {
        
        MultiKey groupIdMemberIdToDelete = groupIdMemberIdToDeleteIterator.next();
        
        GcGrouperSyncMembership gcGrouperSyncMembership = groupIdMemberIdToSyncMembership.get(groupIdMemberIdToDelete);
        
        if (gcGrouperSyncMembership == null) {
          throw new RuntimeException("why is gcGrouperSyncMembership null???");
        }
  
        // if we arent in target, dont worry about it
        if (!gcGrouperSyncMembership.isInTarget() ) {
          groupIdMemberIdToDeleteIterator.remove();
          groupIdMemberIdToSyncMembership.remove(groupIdMemberIdToDelete);
        }
        
      }
      
    }
    
  }

}
