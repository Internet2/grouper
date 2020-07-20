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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

/**
 * Sync up provisioning attributes with grouper group sync provisionable attributes
 * @author mchyzer
 */
public class ProvisioningSyncIntegration {

  public ProvisioningSyncIntegration() {
  }

  /**
   * provisioning target
   */
  private String target;
  
  /**
   * assign the target of the sync
   * @param theTarget
   * @return this for chaining
   */
  public ProvisioningSyncIntegration assignTarget(String theTarget) {
    this.target = theTarget;
    return this;
  }
  
  /**
   * target of provisioning attribute must match provisioner in GcGrouperSync
   * sync up provisioning attributes to gcGrouperSync objects.
   * @param target
   * @return the gcGrouperSyncGroup objects which have provisioning information
   */
  public ProvisioningSyncResult fullSync() {
    
    if (StringUtils.isBlank(this.target)) {
      throw new RuntimeException("target is required");
    }
    
    int removeSyncRowsAfterSecondsOutOfTarget = GrouperLoaderConfig.retrieveConfig().propertyValueInt(
        "grouper.provisioning.removeSyncRowsAfterSecondsOutOfTarget", 60*60*24*7);
    
    ProvisioningSyncResult provisioningSyncGroupResult = new ProvisioningSyncResult();
    
    if (!GrouperProvisioningSettings.getTargets(true).containsKey(target)) {
      throw new RuntimeException("Target '" + target + "' is not configured. Go to Miscellaneous -> Provisioning to configure a new target.");
    }
    
    // groups with provisioning attributes
    Set<Group> groupsProvisioned = GrouperProvisioningService.findAllGroupsForTarget(target);

    // map of group id to group
    Map<String, Group> mapGroupIdToGroup = new HashMap<String, Group>();
    for (Group group : GrouperUtil.nonNull(groupsProvisioned)) {
      mapGroupIdToGroup.put(group.getId(), group);
    }

    provisioningSyncGroupResult.setMapGroupIdToGroup(mapGroupIdToGroup);
    
    // get or create the grouper sync object
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("grouper", target);
    
    provisioningSyncGroupResult.setGcGrouperSync(gcGrouperSync);
    
    // all existing groups
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();

    // map of group id to grouper sync group objects
    Map<String, GcGrouperSyncGroup> mapGroupIdToGcGrouperSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(gcGrouperSyncGroups)) {
      mapGroupIdToGcGrouperSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }

    provisioningSyncGroupResult.setMapGroupIdToGcGrouperSyncGroup(mapGroupIdToGcGrouperSyncGroup);

    // start group ids to insert with all group ids minus those which have sync group objects already
    Set<String> groupIdsToInsert = new HashSet<String>(mapGroupIdToGroup.keySet());
    provisioningSyncGroupResult.setGroupIdsToInsert(groupIdsToInsert);
    groupIdsToInsert.removeAll(mapGroupIdToGcGrouperSyncGroup.keySet());
    
    Set<String> groupIdsToUpdate = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsToUpdate(groupIdsToUpdate);

    List<GcGrouperSyncGroup> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncGroup>();
    
    Set<String> groupIdsWithChangedIdIndexes = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsWithChangedIdIndexes(groupIdsWithChangedIdIndexes);

    Set<String> groupIdsWithChangedNames = new HashSet<String>();
    provisioningSyncGroupResult.setGroupIdsWithChangedNames(groupIdsWithChangedNames);
    

    // lets remove ones that dont need to be there
    if (GrouperUtil.length(gcGrouperSyncGroups) > 0) {
      
      Iterator<GcGrouperSyncGroup> gcGrouperSyncGroupsIterator = gcGrouperSyncGroups.iterator();

      while (gcGrouperSyncGroupsIterator.hasNext()) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSyncGroupsIterator.next();
        Group group = mapGroupIdToGroup.get(gcGrouperSyncGroup.getGroupId());
        
        // keep it
        if (group != null || gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.isInTarget()) {
          
          // see if needs to update
          {
            String newGroupName = group == null ? null : group.getName();
            if (!StringUtils.equals(newGroupName, gcGrouperSyncGroup.getGroupName())) {
              groupIdsWithChangedNames.add(gcGrouperSyncGroup.getGroupId());
            }
            gcGrouperSyncGroup.setGroupName(newGroupName);
          }
          
          {
            Long newGroupIdIndex = group == null ? null : group.getIdIndex();
            if (!GrouperUtil.equals(newGroupIdIndex, gcGrouperSyncGroup.getGroupIdIndex())) {
              groupIdsWithChangedIdIndexes.add(gcGrouperSyncGroup.getGroupId());
            }
            gcGrouperSyncGroup.setGroupIdIndex(newGroupIdIndex);
          }
          
          // see if not provisionable
          if (!gcGrouperSyncGroup.isProvisionable() && group != null) {
            gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncGroup.setProvisionableEnd(null);
            gcGrouperSyncGroup.setProvisionable(true);
          }
          if (gcGrouperSyncGroup.isProvisionable() && group == null) {
            gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncGroup.setProvisionable(false);
          }

          // see if not provisionable
          if (!gcGrouperSyncGroup.isInTarget() && group != null) {
            groupIdsToInsert.add(gcGrouperSyncGroup.getGroupId());
          }
            
          if (gcGrouperSyncGroup.dbVersionDifferent()) {
            groupIdsToUpdate.add(gcGrouperSyncGroup.getGroupId());
          }
          
          continue;
        }
        
        gcGrouperSyncGroupsIterator.remove();

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
        Group group = mapGroupIdToGroup.get(groupIdToInsert);
        if (group == null) {
          continue;
        }
        if (gcGrouperSyncGroup == null) {
          gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupCreateByGroupId(groupIdToInsert);
        }
        gcGrouperSyncGroup.setGroupName(group.getName());
        gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
        gcGrouperSyncGroup.setProvisionable(true);
        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        mapGroupIdToGcGrouperSyncGroup.put(groupIdToInsert, gcGrouperSyncGroup);
      }
      
    }
    
    Set<String> groupIdsToDelete = new HashSet<String>(mapGroupIdToGcGrouperSyncGroup.keySet());
    
    provisioningSyncGroupResult.setGroupIdsToDelete(groupIdsToDelete);
    
    groupIdsToDelete.removeAll(mapGroupIdToGroup.keySet());
    
    if (GrouperUtil.length(groupIdsToDelete) > 0) {

      Iterator<String> groupIdToDeleteIterator = groupIdsToDelete.iterator();
      
      while (groupIdToDeleteIterator.hasNext()) {
        
        String groupIdToDelete = groupIdToDeleteIterator.next();
        
        GcGrouperSyncGroup gcGrouperSyncGroup = mapGroupIdToGcGrouperSyncGroup.get(groupIdToDelete);
        
        if (gcGrouperSyncGroup == null) {
          throw new RuntimeException("why is gcGrouperSyncGroup null???");
        }

        gcGrouperSyncGroup.setProvisionable(false);
        gcGrouperSyncGroup.setProvisionableEnd(new Timestamp(System.currentTimeMillis()));
        
        // if we arent in target, dont worry about it
        if (!gcGrouperSyncGroup.isInTarget() ) {
          groupIdToDeleteIterator.remove();
        }
        
        //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
        if (!gcGrouperSyncGroup.isInTarget()) {
          long targetEndMillis = gcGrouperSyncGroup.getInTargetEnd() == null ? 0 : gcGrouperSyncGroup.getInTargetEnd().getTime();
          if ((System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget) {
            gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncGroup);
          }
        }
        
      }
      
    }
    
    int objectStoreCount = gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    provisioningSyncGroupResult.setSyncObjectStoreCount(objectStoreCount);
    
    return provisioningSyncGroupResult;
    
    
  }
  
}
