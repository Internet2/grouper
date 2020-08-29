package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntityWrapper;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroupWrapper;
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
      Map<String, ProvisioningGroupWrapper> groupUuidToProvisioningGroupWrapper, 
      Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup) {

    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }

    groupUuidToProvisioningGroupWrapper = GrouperUtil.nonNull(groupUuidToProvisioningGroupWrapper);
    groupUuidToSyncGroup = GrouperUtil.nonNull(groupUuidToSyncGroup);
    
    int removeSyncRowsAfterSecondsOutOfTarget = GrouperLoaderConfig.retrieveConfig().propertyValueInt(
        "grouper.provisioning.removeSyncRowsAfterSecondsOutOfTarget", 60*60*24*7);

    provisioningSyncGroupResult.setGcGrouperSync(gcGrouperSync);
    
    // start group ids to insert with all group ids minus those which have sync group objects already
    Set<String> groupIdsToInsert = new HashSet<String>(groupUuidToProvisioningGroupWrapper.keySet());
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
        
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(gcGrouperSyncGroup.getGroupId());

        ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperProvisioningGroup();

        // keep it
        if (grouperProvisioningGroup != null || gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.isInTarget()) {
          
          // see if needs to update
          {
            String newGroupName = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getName();
            if (!StringUtils.equals(newGroupName, gcGrouperSyncGroup.getGroupName())) {
              groupIdsWithChangedNames.add(gcGrouperSyncGroup.getGroupId());
            }
          }
          
          {
            Long newGroupIdIndex = grouperProvisioningGroup == null ? null : grouperProvisioningGroup.getIdIndex();
            if (!GrouperUtil.equals(newGroupIdIndex, gcGrouperSyncGroup.getGroupIdIndex())) {
              groupIdsWithChangedIdIndexes.add(gcGrouperSyncGroup.getGroupId());
            }
          }
          
          // see if not provisionable
          if (!gcGrouperSyncGroup.isProvisionable() && grouperProvisioningGroup != null) {
            gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncGroup.setProvisionableEnd(null);
            gcGrouperSyncGroup.setProvisionable(true);
          }
          if (gcGrouperSyncGroup.isProvisionable() && grouperProvisioningGroup == null) {
            gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncGroup.setProvisionable(false);
          }

          // see if not provisionable
          if (!gcGrouperSyncGroup.isInTarget() && grouperProvisioningGroup != null) {
            groupIdsToInsert.add(gcGrouperSyncGroup.getGroupId());
          }
            
          if (gcGrouperSyncGroup.dbVersionDifferent()) {
            groupIdsToUpdate.add(gcGrouperSyncGroup.getGroupId());
          }
          
          continue;
        }
        
        groupUuidToSyncGroup.remove(gcGrouperSyncGroup.getGroupId());

        //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
        long targetEndMillis = gcGrouperSyncGroup.getInTargetEnd() == null ? 0 : gcGrouperSyncGroup.getInTargetEnd().getTime();
        if ((System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget) {
          gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncGroup);
        }
      }

      gcGrouperSync.getGcGrouperSyncGroupDao().groupDelete(gcGrouperSyncRowsToDeleteFromDatabase, true, true);
    }
    
    if (GrouperUtil.length(groupIdsToInsert) > 0) {
      
      Map<String, GcGrouperSyncGroup> mapGroupIdToSyncGroupInsert = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupIds(groupIdsToInsert);
      
      for (String groupIdToInsert : mapGroupIdToSyncGroupInsert.keySet()) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = mapGroupIdToSyncGroupInsert.get(groupIdToInsert);
        ProvisioningGroupWrapper provisioningGroupWrapper = groupUuidToProvisioningGroupWrapper.get(groupIdToInsert);
        ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGrouperProvisioningGroup();
        
        if (grouperProvisioningGroup == null) {
          continue;
        }
        if (gcGrouperSyncGroup == null) {
          gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId(groupIdToInsert);
        }
        gcGrouperSyncGroup.setGroupName(grouperProvisioningGroup.getName());
        gcGrouperSyncGroup.setGroupIdIndex(grouperProvisioningGroup.getIdIndex());
        gcGrouperSyncGroup.setProvisionable(true);
        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        groupUuidToSyncGroup.put(groupIdToInsert, gcGrouperSyncGroup);
      }
      
    }
    
    Set<String> groupIdsToDelete = new HashSet<String>(groupUuidToSyncGroup.keySet());
    
    provisioningSyncGroupResult.setGroupIdsToDelete(groupIdsToDelete);
    
    groupIdsToDelete.removeAll(groupUuidToProvisioningGroupWrapper.keySet());
    
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

  public static void fullSyncMembers(ProvisioningSyncResult provisioningSyncResult, GcGrouperSync gcGrouperSync,
      Map<String, ProvisioningEntityWrapper> memberUuidToProvisioningEntityWrapper, 
      Map<String, GcGrouperSyncMember> memberUuidToSyncMember) {
  
    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }
  
    memberUuidToProvisioningEntityWrapper = GrouperUtil.nonNull(memberUuidToProvisioningEntityWrapper);
    memberUuidToSyncMember = GrouperUtil.nonNull(memberUuidToSyncMember);
    
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
          if (!gcGrouperSyncMember.isProvisionable() && grouperProvisioningEntity != null) {
            gcGrouperSyncMember.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncMember.setProvisionableEnd(null);
            gcGrouperSyncMember.setProvisionable(true);
          }
          if (gcGrouperSyncMember.isProvisionable() && grouperProvisioningEntity == null) {
            gcGrouperSyncMember.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncMember.setProvisionable(false);
          }
  
          // see if not provisionable
          if (!gcGrouperSyncMember.isInTarget() && grouperProvisioningEntity != null) {
            memberIdsToInsert.add(gcGrouperSyncMember.getMemberId());
          }
            
          if (gcGrouperSyncMember.dbVersionDifferent()) {
            memberIdsToUpdate.add(gcGrouperSyncMember.getMemberId());
          }
          
          continue;
        }
        
        memberUuidToSyncMember.remove(gcGrouperSyncMember.getMemberId());
  
        //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
        long targetEndMillis = gcGrouperSyncMember.getInTargetEnd() == null ? 0 : gcGrouperSyncMember.getInTargetEnd().getTime();
        if ((System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget) {
          gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncMember);
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
        gcGrouperSyncMember.setSubjectId(grouperProvisioningEntity.retrieveAttributeValueString("subjectId"));
        gcGrouperSyncMember.setSubjectIdentifier(grouperProvisioningEntity.retrieveAttributeValueString("subjectIdentifier0"));
        gcGrouperSyncMember.setProvisionable(true);
        gcGrouperSyncMember.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        memberUuidToSyncMember.put(memberIdToInsert, gcGrouperSyncMember);
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
      Map<MultiKey, ProvisioningMembershipWrapper> groupIdMemberIdToProvisioningMembershipWrapper, 
      Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToSyncMembership) {
  
    if (gcGrouperSync == null || StringUtils.isBlank(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("provisioner name is required");
    }
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(gcGrouperSync.getProvisionerName())) {
      throw new RuntimeException("Target '" + gcGrouperSync.getProvisionerName() 
        + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }
  
    groupIdMemberIdToProvisioningMembershipWrapper = GrouperUtil.nonNull(groupIdMemberIdToProvisioningMembershipWrapper);
    groupIdMemberIdToSyncMembership = GrouperUtil.nonNull(groupIdMemberIdToSyncMembership);
    
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
      
      // make an array list so we can remove from the map without exception
      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>(groupIdMemberIdToSyncMembership.values());
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
        
        GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
        
        // not sure why this would happen, i guess if a group aged out and this is already removed????
        if (gcGrouperSyncGroup == null || gcGrouperSyncMember == null) {
          continue;
        }

        MultiKey groupIdMemberId = new MultiKey(
            gcGrouperSyncGroup.getGroupId(), gcGrouperSyncMember.getMemberId());
        
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupIdMemberIdToProvisioningMembershipWrapper.get(groupIdMemberId);
  
        ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGrouperProvisioningMembership();
  
        // keep it
        boolean membershipProvisionable = gcGrouperSyncGroup.isProvisionable() && gcGrouperSyncMember.isProvisionable();
        
        if (grouperProvisioningMembership != null || membershipProvisionable || gcGrouperSyncMembership.isInTarget()) {
  
          // see if not provisionable
          if (!gcGrouperSyncMembership.isInTarget() && grouperProvisioningMembership != null) {
            groupIdMemberIdsToInsert.add(groupIdMemberId);
          }
            
          if (gcGrouperSyncMembership.dbVersionDifferent()) {
            groupIdMemberIdsToUpdate.add(groupIdMemberId);
          }
          
          continue;
        }
        
        groupIdMemberIdToSyncMembership.remove(groupIdMemberId);
  
        //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
        long targetEndMillis = gcGrouperSyncMembership.getInTargetEnd() == null ? 0 : gcGrouperSyncMembership.getInTargetEnd().getTime();
        if ((System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget) {
          gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncMembership);
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
