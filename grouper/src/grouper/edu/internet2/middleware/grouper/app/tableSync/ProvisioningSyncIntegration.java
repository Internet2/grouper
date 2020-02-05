package edu.internet2.middleware.grouper.app.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * 
 * @author mchyzer
 *
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
    
    if (!GrouperProvisioningSettings.getTargets().containsKey(target)) {
      throw new RuntimeException("Target '" + target + "' is not configured in grouper.properties: provisioning.target." + target + ".key");
    }
    // batch these up
    int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 800);
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + target + ".batchSize", defaultBatchSize);
    
    // groups with provisioning attributes
    Set<Group> groupsProvisioned = GrouperProvisioningService.findAllGroupsForTarget(target);

    provisioningSyncGroupResult.setGroupsProvisioned(groupsProvisioned);
    
    // map of group id to group
    Map<String, Group> mapGroupIdToGroup = new HashMap<String, Group>();
    for (Group group : GrouperUtil.nonNull(groupsProvisioned)) {
      mapGroupIdToGroup.put(group.getId(), group);
    }

    provisioningSyncGroupResult.setMapGroupIdToGroup(mapGroupIdToGroup);
    
    // get or create the grouper sync object
    GcGrouperSync gcGrouperSync = GcGrouperSync.retrieveOrCreateByProvisionerName("grouper", target);
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.retrieveAllGroups();

    // lets remove ones that dont need to be there
    if (GrouperUtil.length(gcGrouperSyncGroups) > 0) {
      
      Iterator<GcGrouperSyncGroup> gcGrouperSyncGroupsIterator = gcGrouperSyncGroups.iterator();
      List<GcGrouperSyncGroup> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncGroup>();
      
      Set<GcGrouperSyncGroup> gcGrouperSyncGroupsToUpdate = new LinkedHashSet<GcGrouperSyncGroup>();
      provisioningSyncGroupResult.setGcGrouperSyncGroupsToUpdate(gcGrouperSyncGroupsToUpdate);
      
      Set<GcGrouperSyncGroup> gcGrouperSyncGroupsNoChange = new LinkedHashSet<GcGrouperSyncGroup>();
      provisioningSyncGroupResult.setGcGrouperSyncGroupsNoChange(gcGrouperSyncGroupsNoChange);
      
      Map<String, GcGrouperSyncGroup> oldNameToGcGrouperSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
      provisioningSyncGroupResult.setOldNameToGcGrouperSyncGroup(oldNameToGcGrouperSyncGroup);

      Map<Long, GcGrouperSyncGroup> oldIdIndexToGcGrouperSyncGroup = new HashMap<Long, GcGrouperSyncGroup>();
      provisioningSyncGroupResult.setOldIdIndexToGcGrouperSyncGroup(oldIdIndexToGcGrouperSyncGroup);
      
      while (gcGrouperSyncGroupsIterator.hasNext()) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSyncGroupsIterator.next();
        Group group = mapGroupIdToGroup.get(gcGrouperSyncGroup.getGroupId());
        
        // keep it
        if (group != null || gcGrouperSyncGroup.isProvisionable() || gcGrouperSyncGroup.isInTarget()) {
          
          boolean hasChange = false;
          
          // see if needs to update
          if (StringUtils.equals(gcGrouperSyncGroup.getGroupName(), group == null ? null : group.getName())) {
            oldNameToGcGrouperSyncGroup.put(gcGrouperSyncGroup.getGroupName(), gcGrouperSyncGroup);
            hasChange = true;
            gcGrouperSyncGroup.setGroupName(group.getName());
          }
          
          // see if needs to update
          if (GrouperUtil.equals(gcGrouperSyncGroup.getGroupIdIndex(), group == null ? null : group.getIdIndex())) {
            oldIdIndexToGcGrouperSyncGroup.put(gcGrouperSyncGroup.getGroupIdIndex(), gcGrouperSyncGroup);
            hasChange = true;
            gcGrouperSyncGroup.setGroupName(group.getName());
          }
          
          // see if not provisionable
          if (!gcGrouperSyncGroup.isProvisionable()) {
            gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
            gcGrouperSyncGroup.setProvisionableEnd(null);
            gcGrouperSyncGroup.setProvisionable(true);
            hasChange = true;
          }

          // see if not provisionable
          if (!gcGrouperSyncGroup.isInTarget()) {
            // let the provisioner figure this out
            hasChange = true;
          }

          if (hasChange) {
            gcGrouperSyncGroupsToUpdate.add(gcGrouperSyncGroup);
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

      GcGrouperSyncGroup.deleteBatch(gcGrouperSyncRowsToDeleteFromDatabase, batchSize);
      GcGrouperSyncGroup.storeBatch(gcGrouperSyncGroupsToUpdate, batchSize);
    }
    
    provisioningSyncGroupResult.setGcGrouperSyncGroups(gcGrouperSyncGroups);
    
    // map of group id to grouper sync group objects
    Map<String, GcGrouperSyncGroup> mapGroupIdToGcGrouperSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(gcGrouperSyncGroups)) {
      mapGroupIdToGcGrouperSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }

    provisioningSyncGroupResult.setMapGroupIdToGcGrouperSyncGroup(mapGroupIdToGcGrouperSyncGroup);
    
    Set<String> groupIdsToInsert = new HashSet<String>(mapGroupIdToGroup.keySet());
    
    provisioningSyncGroupResult.setGroupIdsToInsert(groupIdsToInsert);
    
    groupIdsToInsert.removeAll(mapGroupIdToGcGrouperSyncGroup.keySet());
    
    if (GrouperUtil.length(groupIdsToInsert) > 0) {
      List<GcGrouperSyncGroup> gcGrouperSyncGroupsToInsert = new ArrayList<GcGrouperSyncGroup>();
      for (String groupIdToInsert : groupIdsToInsert) {
        
        Group group = mapGroupIdToGroup.get(groupIdToInsert);
        
        if (group == null) {
          throw new RuntimeException("why is group null???");
        }
        
        GcGrouperSyncGroup gcGrouperSyncGroup = new GcGrouperSyncGroup();
        gcGrouperSyncGroup.setGrouperSync(gcGrouperSync);
        gcGrouperSyncGroup.setGroupId(groupIdToInsert);
        gcGrouperSyncGroup.setGroupName(group.getName());
        gcGrouperSyncGroup.setGroupIdIndex(group.getIdIndex());
        gcGrouperSyncGroup.setProvisionable(true);
        gcGrouperSyncGroup.setProvisionableStart(new Timestamp(System.currentTimeMillis()));
        gcGrouperSyncGroupsToInsert.add(gcGrouperSyncGroup);
      }
      
      GcGrouperSyncGroup.storeBatch(gcGrouperSyncGroupsToInsert, batchSize);
    }
    
    Set<String> groupIdsToDelete = new HashSet<String>(mapGroupIdToGcGrouperSyncGroup.keySet());
    
    provisioningSyncGroupResult.setGroupIdsToDelete(groupIdsToDelete);
    
    groupIdsToDelete.removeAll(mapGroupIdToGroup.keySet());
    
    if (GrouperUtil.length(groupIdsToDelete) > 0) {

      Iterator<String> groupIdToDeleteIterator = groupIdsToDelete.iterator();
      
      List<GcGrouperSyncGroup> gcGrouperSyncRowsToDeleteFromDatabase = new ArrayList<GcGrouperSyncGroup>();
      
      while (groupIdToDeleteIterator.hasNext()) {
        
        String groupIdToDelete = groupIdToDeleteIterator.next();
        
        GcGrouperSyncGroup gcGrouperSyncGroup = mapGroupIdToGcGrouperSyncGroup.get(groupIdToDelete);
        
        if (gcGrouperSyncGroup == null) {
          throw new RuntimeException("why is gcGrouperSyncGroup null???");
        }
        
        if (!gcGrouperSyncGroup.isProvisionable()) {
          groupIdToDeleteIterator.remove();

          //if we arent provisionable, and the group has not been in the target for a week, then we done with that one
          if (!gcGrouperSyncGroup.isInTarget()) {
            long targetEndMillis = gcGrouperSyncGroup.getInTargetEnd() == null ? 0 : gcGrouperSyncGroup.getInTargetEnd().getTime();
            if ((System.currentTimeMillis() - targetEndMillis) / 1000 > removeSyncRowsAfterSecondsOutOfTarget) {
              gcGrouperSyncRowsToDeleteFromDatabase.add(gcGrouperSyncGroup);
            }
          }
          continue;
        }
        
      }
      
    }
    
    return provisioningSyncGroupResult;
    
    
  }
  
}
