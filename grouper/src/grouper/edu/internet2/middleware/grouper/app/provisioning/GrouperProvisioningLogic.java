package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
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
  public void fullProvisionFull() {

    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          
          GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao().retrieveAllGroups();
          GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao().retrieveAllMemberships();
          
        } catch (RuntimeException re) {
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();
    
    GrouperProvisioningLists grouperProvisioningObjects = new GrouperProvisioningLists();
    grouperProvisioningData.setGrouperProvisioningObjects(grouperProvisioningObjects);
    
    grouperProvisioningObjects.setProvisioningGroups(grouperProvisioner.retrieveGrouperDao().retrieveAllGroups());
    grouperProvisioningObjects.setProvisioningEntities(grouperProvisioner.retrieveGrouperDao().retrieveAllMembers());
    grouperProvisioningObjects.setProvisioningMemberships(grouperProvisioner.retrieveGrouperDao().retrieveAllMemberships());
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
    
    this.grouperProvisioner.retrieveTranslator().translateGrouperToTarget();
    
    // TODO issues with dn comparison with case/spacing differences
    this.compareCommonObjects();
    
    this.getGrouperProvisioner().retrieveTargetDao().sendChangesToTarget();
    
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
  
  private void compareCommonObjects() {
    GrouperProvisioningLists grouperCommonObjects = this.grouperProvisioner.getGrouperProvisioningData().getGrouperCommonObjects();
    GrouperProvisioningLists targetCommonObjects = this.grouperProvisioner.getGrouperProvisioningData().getTargetCommonObjects();
    
    GrouperProvisioningLists commonObjectsToInsert = new GrouperProvisioningLists();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonObjectInserts(commonObjectsToInsert);
    
    GrouperProvisioningLists commonObjectsToDelete = new GrouperProvisioningLists();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonObjectDeletes(commonObjectsToDelete);
    
    GrouperProvisioningLists commonObjectsToUpdate = new GrouperProvisioningLists();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonObjectUpdates(commonObjectsToUpdate);
    
    compareCommonGroups(grouperCommonObjects.getProvisioningGroups(), targetCommonObjects.getProvisioningGroups());
    compareCommonEntities(grouperCommonObjects.getProvisioningEntities(), targetCommonObjects.getProvisioningEntities());
    compareCommonMemberships(grouperCommonObjects.getProvisioningMemberships(), targetCommonObjects.getProvisioningMemberships());
  }
  
  protected void compareCommonMemberships(List<ProvisioningMembership> grouperCommonMemberships, List<ProvisioningMembership> targetCommonMemberships) { 
    
    Map<MultiKey, ProvisioningMembership> grouperCommonGroupIdEntityIdToMembership = new HashMap<MultiKey, ProvisioningMembership>();
    Map<MultiKey, ProvisioningMembership> targetCommonGroupIdEntityIdToMembership = new HashMap<MultiKey, ProvisioningMembership>();
    
    
    for (ProvisioningMembership provisioningMembership: GrouperUtil.nonNull(grouperCommonMemberships)) {
      grouperCommonGroupIdEntityIdToMembership.put(new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), provisioningMembership);
    }
    
    for (ProvisioningMembership provisioningMembership: GrouperUtil.nonNull(targetCommonMemberships)) {
      targetCommonGroupIdEntityIdToMembership.put(new MultiKey(provisioningMembership.getProvisioningGroupId(), provisioningMembership.getProvisioningEntityId()), provisioningMembership);
    }
    
    {
      // memberships to insert
      Set<MultiKey> groupIdEntityIdsToInsert = new HashSet<MultiKey>(grouperCommonGroupIdEntityIdToMembership.keySet());
      groupIdEntityIdsToInsert.removeAll(targetCommonGroupIdEntityIdToMembership.keySet());
      
      List<ProvisioningMembership> provisioningMembershipsToInsert = new ArrayList<ProvisioningMembership>();
      
      for (MultiKey groupIdEntityIdToInsert: groupIdEntityIdsToInsert) {
        provisioningMembershipsToInsert.add(grouperCommonGroupIdEntityIdToMembership.get(groupIdEntityIdToInsert));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectInserts().setProvisioningMemberships(provisioningMembershipsToInsert);
    
    
      // memberships to delete
      Set<MultiKey> groupIdEntityIdsToDelete = new HashSet<MultiKey>(targetCommonGroupIdEntityIdToMembership.keySet());
      groupIdEntityIdsToDelete.removeAll(grouperCommonGroupIdEntityIdToMembership.keySet());
      
      List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();
      
      for (MultiKey groupIdEntityIdToDelete: groupIdEntityIdsToDelete) {
        provisioningMembershipsToDelete.add(targetCommonGroupIdEntityIdToMembership.get(groupIdEntityIdToDelete));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectDeletes().setProvisioningMemberships(provisioningMembershipsToDelete);
    
      // memberships to update
      Set<MultiKey> groupIdEntityIdsToUpdate = new HashSet<MultiKey>(targetCommonGroupIdEntityIdToMembership.keySet());
      groupIdEntityIdsToUpdate.addAll(grouperCommonGroupIdEntityIdToMembership.keySet());
      groupIdEntityIdsToUpdate.removeAll(groupIdEntityIdsToInsert);
      groupIdEntityIdsToUpdate.removeAll(groupIdEntityIdsToDelete);
      
      List<ProvisioningMembership> provisioningMembershipsToUpdate = new ArrayList<ProvisioningMembership>();
      
      for (MultiKey groupIdEntityIdToUpdate: groupIdEntityIdsToUpdate) {
        ProvisioningMembership grouperCommonMembership = grouperCommonGroupIdEntityIdToMembership.get(groupIdEntityIdToUpdate);
        ProvisioningMembership targetCommonMembership = targetCommonGroupIdEntityIdToMembership.get(groupIdEntityIdToUpdate);
        
        boolean alreadyAddedForUpdate = false;
        
        alreadyAddedForUpdate = compareFieldValue(provisioningMembershipsToUpdate, "id",
            grouperCommonMembership.getId()  , targetCommonMembership.getId(),
            alreadyAddedForUpdate, grouperCommonMembership);
        
        compareAttributeValues(provisioningMembershipsToUpdate, grouperCommonMembership.getAttributes(),
            targetCommonMembership.getAttributes(), grouperCommonMembership, alreadyAddedForUpdate);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
    }
    
    
  }
  
  protected void compareCommonEntities(List<ProvisioningEntity> grouperCommonEntities, List<ProvisioningEntity> targetCommonEntities) { 
    
    Map<String, ProvisioningEntity> grouperCommonEntityIdToEntity = new HashMap<String, ProvisioningEntity>();
    Map<String, ProvisioningEntity> targetCommonEntityIdToEntity = new HashMap<String, ProvisioningEntity>();
    
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(grouperCommonEntities)) {
      grouperCommonEntityIdToEntity.put(provisioningEntity.getId(), provisioningEntity);
    }
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(targetCommonEntities)) {
      targetCommonEntityIdToEntity.put(provisioningEntity.getId(), provisioningEntity);
    }
    
    {
      // entities to insert
      Set<String> entityIdsToInsert = new HashSet<String>(grouperCommonEntityIdToEntity.keySet());
      entityIdsToInsert.removeAll(targetCommonEntityIdToEntity.keySet());
      
      List<ProvisioningEntity> provisioningEntitiesToInsert = new ArrayList<ProvisioningEntity>();
      
      for (String entityIdToInsert: entityIdsToInsert) {
        provisioningEntitiesToInsert.add(grouperCommonEntityIdToEntity.get(entityIdToInsert));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
    
    
      // entities to delete
      Set<String> entityIdsToDelete = new HashSet<String>(targetCommonEntityIdToEntity.keySet());
      entityIdsToDelete.removeAll(grouperCommonEntityIdToEntity.keySet());
      
      List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();
      
      for (String entityIdToDelete: entityIdsToDelete) {
        provisioningEntitiesToDelete.add(targetCommonEntityIdToEntity.get(entityIdToDelete));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectDeletes().setProvisioningEntities(provisioningEntitiesToDelete);
    
      // entities to update
      Set<String> entityIdsToUpdate = new HashSet<String>(targetCommonEntityIdToEntity.keySet());
      entityIdsToUpdate.addAll(grouperCommonEntityIdToEntity.keySet());
      entityIdsToUpdate.removeAll(entityIdsToInsert);
      entityIdsToUpdate.removeAll(entityIdsToDelete);
      
      List<ProvisioningEntity> provisioningEntitiesToUpdate = new ArrayList<ProvisioningEntity>();
      
      for (String entityIdToUpdate: entityIdsToUpdate) {
        ProvisioningEntity grouperCommonEntity = grouperCommonEntityIdToEntity.get(entityIdToUpdate);
        ProvisioningEntity targetCommonEntity = targetCommonEntityIdToEntity.get(entityIdToUpdate);
        
        boolean alreadyAddedForUpdate = false;
        
        alreadyAddedForUpdate = compareFieldValue(provisioningEntitiesToUpdate, "name",
            grouperCommonEntity.getName() , targetCommonEntity.getName(),
            alreadyAddedForUpdate, grouperCommonEntity);
        
        alreadyAddedForUpdate = compareFieldValue(provisioningEntitiesToUpdate, "email",
            grouperCommonEntity.getEmail() , targetCommonEntity.getEmail(),
            alreadyAddedForUpdate, grouperCommonEntity);
        
        alreadyAddedForUpdate = compareFieldValue(provisioningEntitiesToUpdate, "loginId",
            grouperCommonEntity.getLoginId() , targetCommonEntity.getLoginId(),
            alreadyAddedForUpdate, grouperCommonEntity);
        
        
        compareAttributeValues(provisioningEntitiesToUpdate, grouperCommonEntity.getAttributes(),
            targetCommonEntity.getAttributes(), grouperCommonEntity, alreadyAddedForUpdate);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);
    }
    
    
  }
  
  protected void compareCommonGroups(List<ProvisioningGroup> grouperCommonGroups, List<ProvisioningGroup> targetCommonGroups) {
    
    // groups insert
    Map<String, ProvisioningGroup> grouperCommonGroupIdToGroup = new HashMap<String, ProvisioningGroup>();
    Map<String, ProvisioningGroup> targetCommonGroupIdToGroup = new HashMap<String, ProvisioningGroup>();
    
    
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(grouperCommonGroups)) {
      grouperCommonGroupIdToGroup.put(provisioningGroup.getId(), provisioningGroup);
    }
    
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(targetCommonGroups)) {
      targetCommonGroupIdToGroup.put(provisioningGroup.getId(), provisioningGroup);
    }
    
    {
      // groups to insert
      Set<String> groupIdsToInsert = new HashSet<String>(grouperCommonGroupIdToGroup.keySet());
      groupIdsToInsert.removeAll(targetCommonGroupIdToGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToInsert = new ArrayList<ProvisioningGroup>();
      
      for (String groupIdToInsert: groupIdsToInsert) {
        provisioningGroupsToInsert.add(grouperCommonGroupIdToGroup.get(groupIdToInsert));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData()
      .getCommonObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
    
    
      // groups to delete
      Set<String> groupIdsToDelete = new HashSet<String>(targetCommonGroupIdToGroup.keySet());
      groupIdsToDelete.removeAll(grouperCommonGroupIdToGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
      
      for (String groupIdToDelete: groupIdsToDelete) {
        provisioningGroupsToDelete.add(targetCommonGroupIdToGroup.get(groupIdToDelete));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData()
      .getCommonObjectDeletes().setProvisioningGroups(provisioningGroupsToDelete);
    
      // groups to update
      Set<String> groupIdsToUpdate = new HashSet<String>(targetCommonGroupIdToGroup.keySet());
      groupIdsToUpdate.addAll(grouperCommonGroupIdToGroup.keySet());
      groupIdsToUpdate.removeAll(groupIdsToInsert);
      groupIdsToUpdate.removeAll(groupIdsToDelete);
      
      
      List<ProvisioningGroup> provisioningGroupsToUpdate = new ArrayList<ProvisioningGroup>();
      
      for (String groupIdToUpdate: groupIdsToUpdate) {
        ProvisioningGroup grouperCommonGroup = grouperCommonGroupIdToGroup.get(groupIdToUpdate);
        ProvisioningGroup targetCommonGroup = targetCommonGroupIdToGroup.get(groupIdToUpdate);
        
        boolean alreadyAddedForUpdate = false;
        
        alreadyAddedForUpdate = compareFieldValue(provisioningGroupsToUpdate, "displayName",
            grouperCommonGroup.getDisplayName(), targetCommonGroup.getDisplayName(),
            alreadyAddedForUpdate, grouperCommonGroup);
        
        alreadyAddedForUpdate = compareFieldValue(provisioningGroupsToUpdate, "name",
            grouperCommonGroup.getName(), targetCommonGroup.getName(),
            alreadyAddedForUpdate, grouperCommonGroup);
        
        alreadyAddedForUpdate = compareFieldValue(provisioningGroupsToUpdate, "idIndex",
            grouperCommonGroup.getIdIndex(), targetCommonGroup.getIdIndex(),
            alreadyAddedForUpdate, grouperCommonGroup);
        
        compareAttributeValues(provisioningGroupsToUpdate, grouperCommonGroup.getAttributes(),
            targetCommonGroup.getAttributes(), grouperCommonGroup, alreadyAddedForUpdate);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);
    }
    
    
  }

  protected void compareAttributeValues(
      List provisioningUpdatablesToUpdate,
      Map<String, ProvisioningAttribute> grouperCommonAttributes,
      Map<String, ProvisioningAttribute> targetCommonAttributes,
      ProvisioningUpdatable grouperProvisioningUpdatable,
      boolean alreadyAddedForUpdate) {
    for (String attributeName: grouperCommonAttributes.keySet()) {
      
      ProvisioningAttribute targetAttribute = targetCommonAttributes.get(attributeName);
      if (targetAttribute == null) {
        if (!alreadyAddedForUpdate) {
          grouperProvisioningUpdatable.setInternal_fieldsToUpdate(new HashMap<MultiKey, Object>());
          provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
          alreadyAddedForUpdate = true;
        }
        
        grouperProvisioningUpdatable.getInternal_fieldsToUpdate().put(new MultiKey("attribute", attributeName, "insert"), null);
      } else {
        
        if (!attributeValueEquals(grouperCommonAttributes.get(attributeName).getValue(),
            targetAttribute.getValue())) {
          
          if (!alreadyAddedForUpdate) {
            grouperProvisioningUpdatable.setInternal_fieldsToUpdate(new HashMap<MultiKey, Object>());
            provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
            alreadyAddedForUpdate = true;
          }
          
          grouperProvisioningUpdatable.getInternal_fieldsToUpdate().put(new MultiKey("attribute", attributeName, "update"), targetAttribute.getValue());
          
        } 
        
      }
      
    }
    
    for (String attributeName: targetCommonAttributes.keySet()) {
      
      ProvisioningAttribute grouperAttribute = grouperCommonAttributes.get(attributeName);
      if (grouperAttribute == null) {
        if (!alreadyAddedForUpdate) {
          grouperProvisioningUpdatable.setInternal_fieldsToUpdate(new HashMap<MultiKey, Object>());
          provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
          alreadyAddedForUpdate = true;
        }
        
        grouperProvisioningUpdatable.getInternal_fieldsToUpdate().put(new MultiKey("attribute", attributeName, "delete"), 
            targetCommonAttributes.get(attributeName).getValue());
      }
      
    }
  }

  protected boolean compareFieldValue(List provisioningUpdatablesToUpdate,
      String fieldName,
      Object grouperValue, Object targetValue,
      boolean alreadyAddedForUpdate, ProvisioningUpdatable grouperCommonUpdatable) {
    if (!GrouperUtil.equals(grouperValue, targetValue)) {
      if (!alreadyAddedForUpdate) {
        grouperCommonUpdatable.setInternal_fieldsToUpdate(new HashMap<MultiKey, Object>());
        provisioningUpdatablesToUpdate.add(grouperCommonUpdatable);
        alreadyAddedForUpdate = true;
      }
      
      grouperCommonUpdatable.getInternal_fieldsToUpdate().put(new MultiKey("field", fieldName,
          attributeChangeType(grouperValue, targetValue)), targetValue);
    }
    return alreadyAddedForUpdate;
  }
  
  
  
  
  private String attributeChangeType(Object first, Object second) {
    if (first == null) return "insert";
    if (second == null) return "delete";
    return "update";
  }
  
  private boolean attributeValueEquals(Object first, Object second) {
    
    if (first instanceof Collection || second instanceof Collection) {
      
      if (GrouperUtil.length(first) == 0 && GrouperUtil.length(second) == 0) {
        return true;
      }
      
      Collection intersection = CollectionUtils.intersection((Collection)first, (Collection)second);
      
      if (GrouperUtil.length(first) == intersection.size() && GrouperUtil.length(second) == intersection.size()) {
        return true;
      }
      
      return false;
      
    }
    
    return GrouperUtil.equals(first, second);
  }
  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

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

}
