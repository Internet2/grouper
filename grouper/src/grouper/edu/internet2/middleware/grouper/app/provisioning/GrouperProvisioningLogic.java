package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * does the logic to use the data from the DAOs and call the correct methods to synnc things up or dry run or send messages for async
 * @author mchyzer
 *
 */
public class GrouperProvisioningLogic {
  
  /**
   * see if there are any objects that need to be fixed or removed
   */
  public void validateInitialProvisioningData() {
    
    
  }
  

  /**
   * 
   */
  public void provision() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    try {
      debugMap.put("state", "retrieveAllData");
      long start = System.currentTimeMillis();
      this.getGrouperProvisioner().getGrouperProvisioningType().retrieveData(this.grouperProvisioner);
      debugMap.put("retrieveAllDataMillis", System.currentTimeMillis()-start);
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("retrieveAllData");
    }

    try {
      debugMap.put("state", "retrieveSubjectLink");
      this.retrieveSubjectLink();
      
      debugMap.put("state", "retrieveTargetGroupLink");
      this.retrieveTargetGroupLink();
      
      debugMap.put("state", "retrieveTargetEntityLink");
      this.retrieveTargetEntityLink();
      
      debugMap.put("state", "validateInitialProvisioningData");
      this.validateInitialProvisioningData();
  
      debugMap.put("state", "translateGrouperToTarget");
      this.grouperProvisioner.retrieveTranslator().translateGrouperToTarget();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateGrouperToTarget");
    }
    
    try {
      debugMap.put("state", "idTargetObjects");
      this.grouperProvisioner.retrieveTranslator().idTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("idTargetObjects");
    }
    
    debugMap.put("state", "indexTargetObjects");
    this.indexTargetObjects();
    
    try {
      debugMap.put("state", "compareTargetObjects");
      this.compareTargetObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("compareTargetObjects");
    }

    try {
      debugMap.put("state", "sendChangesToTarget");
      this.getGrouperProvisioner().retrieveTargetDao().sendChangesToTarget();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("sendChangesToTarget");
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


  public void indexTargetObjects() {
    this.indexTargetGroups();
    
    this.indexTargetEntities();

    this.indexTargetMemberships();
  }

  public void retrieveTargetEntityLink() {
    // TODO If using target entity link and the ID is not in the member sync cache object, then resolve the target entity, and put the id in the member sync object
    
  }


  public void retrieveTargetGroupLink() {
    // TODO If using target group link and the ID is not in the group sync cache object, then resolve the target group, and put the id in the group sync object
    
  }


  public void retrieveSubjectLink() {
    // TODO If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    
  }

  /**
   * retrieve all data from both sides, grouper and target, do this in a thread
   */
  protected void retrieveAllData() {
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao().retrieveAllData();
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperDao().retrieveSyncData(GrouperProvisioningLogic.this.grouperProvisioner.getGrouperProvisioningType());
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

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperDao().fixSyncObjects();
    
    this.grouperProvisioner.retrieveGrouperDao().calculateProvisioningDataToDelete();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }
  }
  
  protected void compareTargetObjects() {
    GrouperProvisioningLists grouperTargetObjects = this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects();
    GrouperProvisioningLists targetProvisioningObjects = this.grouperProvisioner.getGrouperProvisioningData().getTargetProvisioningObjects();
    
    compareTargetGroups(grouperTargetObjects.getProvisioningGroups(), targetProvisioningObjects.getProvisioningGroups());
    compareTargetEntities(grouperTargetObjects.getProvisioningEntities(), targetProvisioningObjects.getProvisioningEntities());
    compareTargetMemberships(grouperTargetObjects.getProvisioningMemberships(), targetProvisioningObjects.getProvisioningMemberships());

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    {
      int groupInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningGroups());
      if (groupInserts > 0) {
        debugMap.put("groupInsertsAfterCompare", groupInserts);
      }
    }
    {
      int groupUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningGroups());
      if (groupUpdates > 0) {
        debugMap.put("groupUpdatesAfterCompare", groupUpdates);
      }
    }
    {
      int groupDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningGroups());
      if (groupDeletes > 0) {
        debugMap.put("groupDeletesAfterCompare", groupDeletes);
      }
    }
    {
      int entityInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningEntities());
      if (entityInserts > 0) {
        debugMap.put("entityInsertsAfterCompare", entityInserts);
      }
    }
    {
      int entityUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningEntities());
      if (entityUpdates > 0) {
        debugMap.put("entityUpdatesAfterCompare", entityUpdates);
      }
    }
    {
      int entityDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningEntities());
      if (entityDeletes > 0) {
        debugMap.put("entityDeletesAfterCompare", entityDeletes);
      }
    }
    {
      int membershipInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectInserts().getProvisioningMemberships());
      if (membershipInserts > 0) {
        debugMap.put("membershipInsertsAfterCompare", membershipInserts);
      }
    }
    {
      int membershipUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectUpdates().getProvisioningMemberships());
      if (membershipUpdates > 0) {
        debugMap.put("membershipUpdatesAfterCompare", membershipUpdates);
      }
    }
    {
      int membershipDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getTargetObjectDeletes().getProvisioningMemberships());
      if (membershipDeletes > 0) {
        debugMap.put("membershipDeletesAfterCompare", membershipDeletes);
      }
    }
  
  }
  
  protected void compareTargetMemberships(List<ProvisioningMembership> grouperTargetMemberships, List<ProvisioningMembership> targetProvisioningMemberships) { 
    
    Map<Object, ProvisioningMembership> grouperTargetIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    Map<Object, ProvisioningMembership> targetTargetIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    
    List<ProvisioningMembership> grouperTargetMembershipsWithNullIds = new ArrayList<ProvisioningMembership>();
    
    for (ProvisioningMembership provisioningMembership: GrouperUtil.nonNull(grouperTargetMemberships)) {
      Object targetId = provisioningMembership.getTargetId();
      if (targetId != null) {
        grouperTargetIdToTargetMembership.put(provisioningMembership.getTargetId(), provisioningMembership);
      } else {
        grouperTargetMembershipsWithNullIds.add(provisioningMembership);
      }
    }
    
    for (ProvisioningMembership provisioningMembership: GrouperUtil.nonNull(targetProvisioningMemberships)) {
      Object targetId = provisioningMembership.getTargetId();
      if (targetId != null) {
        targetTargetIdToTargetMembership.put(provisioningMembership.getTargetId(), provisioningMembership);
      }
    }
    
    {
      // memberships to insert
      Set<Object> targetIdsToInsert = new HashSet<Object>(grouperTargetIdToTargetMembership.keySet());
      targetIdsToInsert.removeAll(targetTargetIdToTargetMembership.keySet());
      
      List<ProvisioningMembership> provisioningMembershipsToInsert = new ArrayList<ProvisioningMembership>();
      
      for (Object groupIdEntityIdToInsert: targetIdsToInsert) {
        provisioningMembershipsToInsert.add(grouperTargetIdToTargetMembership.get(groupIdEntityIdToInsert));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectInserts().setProvisioningMemberships(provisioningMembershipsToInsert);
    
    
      // memberships to delete
      Set<Object> groupIdEntityIdsToDelete = new HashSet<Object>(targetTargetIdToTargetMembership.keySet());
      groupIdEntityIdsToDelete.removeAll(grouperTargetIdToTargetMembership.keySet());
      
      List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();
      
      for (Object targetIdsToDelete: groupIdEntityIdsToDelete) {
        provisioningMembershipsToDelete.add(targetTargetIdToTargetMembership.get(targetIdsToDelete));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectDeletes().setProvisioningMemberships(provisioningMembershipsToDelete);
    
      // memberships to update
      Set<Object> targetIdsToUpdate = new HashSet<Object>(targetTargetIdToTargetMembership.keySet());
      targetIdsToUpdate.addAll(grouperTargetIdToTargetMembership.keySet());
      targetIdsToUpdate.removeAll(targetIdsToInsert);
      targetIdsToUpdate.removeAll(groupIdEntityIdsToDelete);
      
      List<ProvisioningMembership> provisioningMembershipsToUpdate = new ArrayList<ProvisioningMembership>();
      
      for (Object targetIdToUpdate: targetIdsToUpdate) {
        ProvisioningMembership grouperTargetMembership = grouperTargetIdToTargetMembership.get(targetIdToUpdate);
        ProvisioningMembership targetProvisioningMembership = targetTargetIdToTargetMembership.get(targetIdToUpdate);
        
        compareFieldValue(provisioningMembershipsToUpdate, "id",
            grouperTargetMembership.getId(), targetProvisioningMembership.getId(),
            grouperTargetMembership);
        
        compareAttributeValues(provisioningMembershipsToUpdate, grouperTargetMembership.getAttributes(),
            targetProvisioningMembership.getAttributes(), grouperTargetMembership);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
    }
    
    
  }
  
  protected void compareTargetEntities(List<ProvisioningEntity> grouperTargetEntities, List<ProvisioningEntity> targetProvisioningEntities) { 
    
    Map<Object, ProvisioningEntity> grouperTargetIdToTargetEntity = new HashMap<Object, ProvisioningEntity>();
    Map<Object, ProvisioningEntity> targetTargetIdToTargetEntity = new HashMap<Object, ProvisioningEntity>();
    
    List<ProvisioningEntity> grouperTargetEntitiesWithNullIds = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(grouperTargetEntities)) {
      Object targetId = provisioningEntity.getTargetId();
      if (targetId != null) {
        grouperTargetIdToTargetEntity.put(provisioningEntity.getTargetId(), provisioningEntity);
      } else {
        grouperTargetEntitiesWithNullIds.add(provisioningEntity);
      }
    }
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(targetProvisioningEntities)) {
      Object targetId = provisioningEntity.getTargetId();
      if (targetId != null) {
        targetTargetIdToTargetEntity.put(provisioningEntity.getTargetId(), provisioningEntity);
      }
    }
        
    {
      // entities to insert
      Set<Object> entityIdsToInsert = new HashSet<Object>(grouperTargetIdToTargetEntity.keySet());
      entityIdsToInsert.removeAll(targetTargetIdToTargetEntity.keySet());
      
      List<ProvisioningEntity> provisioningEntitiesToInsert = new ArrayList<ProvisioningEntity>();
      
      for (Object entityIdToInsert: entityIdsToInsert) {
        provisioningEntitiesToInsert.add(grouperTargetIdToTargetEntity.get(entityIdToInsert));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
    
    
      // entities to delete
      Set<Object> entityIdsToDelete = new HashSet<Object>(targetTargetIdToTargetEntity.keySet());
      entityIdsToDelete.removeAll(grouperTargetIdToTargetEntity.keySet());
      
      List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();
      
      for (Object entityIdToDelete: entityIdsToDelete) {
        provisioningEntitiesToDelete.add(targetTargetIdToTargetEntity.get(entityIdToDelete));
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectDeletes().setProvisioningEntities(provisioningEntitiesToDelete);
    
      // entities to update
      Set<Object> entityIdsToUpdate = new HashSet<Object>(targetTargetIdToTargetEntity.keySet());
      entityIdsToUpdate.addAll(grouperTargetIdToTargetEntity.keySet());
      entityIdsToUpdate.removeAll(entityIdsToInsert);
      entityIdsToUpdate.removeAll(entityIdsToDelete);
      
      List<ProvisioningEntity> provisioningEntitiesToUpdate = new ArrayList<ProvisioningEntity>();
      
      for (Object entityIdToUpdate: entityIdsToUpdate) {
        ProvisioningEntity grouperTargetEntity = grouperTargetIdToTargetEntity.get(entityIdToUpdate);
        ProvisioningEntity targetProvisioningEntity = targetTargetIdToTargetEntity.get(entityIdToUpdate);
        
        compareFieldValue(provisioningEntitiesToUpdate, "name",
            grouperTargetEntity.getName() , targetProvisioningEntity.getName(),
            grouperTargetEntity);
        
        compareFieldValue(provisioningEntitiesToUpdate, "email",
            grouperTargetEntity.getEmail() , targetProvisioningEntity.getEmail(),
            grouperTargetEntity);
        
        compareFieldValue(provisioningEntitiesToUpdate, "loginId",
            grouperTargetEntity.getLoginId() , targetProvisioningEntity.getLoginId(),
            grouperTargetEntity);
        
        
        compareAttributeValues(provisioningEntitiesToUpdate, grouperTargetEntity.getAttributes(),
            targetProvisioningEntity.getAttributes(), grouperTargetEntity);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);
    }
    
    
  }
  
  protected void compareTargetGroups(List<ProvisioningGroup> grouperTargetGroups, List<ProvisioningGroup> targetProvisioningGroups) {
    
    // groups insert
    Map<Object, ProvisioningGroup> grouperTargetIdToTargetGroup = new HashMap<Object, ProvisioningGroup>();
    Map<Object, ProvisioningGroup> targetTargetIdToTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    List<ProvisioningGroup> grouperTargetGroupsWithNullIds = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(grouperTargetGroups)) {
      Object targetId = provisioningGroup.getTargetId();
      if (targetId != null) {
        grouperTargetIdToTargetGroup.put(provisioningGroup.getTargetId(), provisioningGroup);
      } else {
        grouperTargetGroupsWithNullIds.add(provisioningGroup);
      }
    }
    
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(targetProvisioningGroups)) {
      Object targetId = provisioningGroup.getTargetId();
      if (targetId != null) {
        targetTargetIdToTargetGroup.put(provisioningGroup.getTargetId(), provisioningGroup);
      }
    }
    
    {
      // groups to insert
      Set<Object> groupIdsToInsert = new HashSet<Object>(grouperTargetIdToTargetGroup.keySet());
      groupIdsToInsert.removeAll(targetTargetIdToTargetGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToInsert = new ArrayList<ProvisioningGroup>();
      
      for (Object groupIdToInsert: groupIdsToInsert) {
        ProvisioningGroup groupToInsert = grouperTargetIdToTargetGroup.get(groupIdToInsert);
        provisioningGroupsToInsert.add(groupToInsert);
        for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(groupToInsert.getAttributes()).keySet())) {
          Object grouperValue = groupToInsert.getAttributes().get(attributeName).getValue();
          //TODO add fields
          if (GrouperUtil.isArrayOrCollection(grouperValue)) {
            if (grouperValue instanceof Collection) {
              for (Object value : (Collection)grouperValue) {
                groupToInsert.addInternal_objectChange(
                    new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            } else {
              // array
              for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
                Object value = Array.get(grouperValue, i);
                groupToInsert.addInternal_objectChange(
                    new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            }
          } else {
            // just a scalar
            ProvisioningObjectChange provisioningObjectChange = new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                ProvisioningObjectChangeAction.insert, null, grouperValue);
            groupToInsert.addInternal_objectChange(provisioningObjectChange);
            
          }
        }
      }
      
      this.grouperProvisioner.getGrouperProvisioningData()
      .getTargetObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
    
    
      // groups to delete
      Set<Object> groupIdsToDelete = new HashSet<Object>(targetTargetIdToTargetGroup.keySet());
      groupIdsToDelete.removeAll(grouperTargetIdToTargetGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
      
      for (Object groupIdToDelete: groupIdsToDelete) {
        provisioningGroupsToDelete.add(targetTargetIdToTargetGroup.get(groupIdToDelete));
        
        //TODO add indiv fields and attributes
      }
      
      this.grouperProvisioner.getGrouperProvisioningData()
      .getTargetObjectDeletes().setProvisioningGroups(provisioningGroupsToDelete);
    
      // groups to update
      Set<Object> groupIdsToUpdate = new HashSet<Object>(targetTargetIdToTargetGroup.keySet());
      groupIdsToUpdate.addAll(grouperTargetIdToTargetGroup.keySet());
      groupIdsToUpdate.removeAll(groupIdsToInsert);
      groupIdsToUpdate.removeAll(groupIdsToDelete);
      
      
      List<ProvisioningGroup> provisioningGroupsToUpdate = new ArrayList<ProvisioningGroup>();
      
      for (Object groupIdToUpdate: groupIdsToUpdate) {
        ProvisioningGroup grouperTargetGroup = grouperTargetIdToTargetGroup.get(groupIdToUpdate);
        ProvisioningGroup targetProvisioningGroup = targetTargetIdToTargetGroup.get(groupIdToUpdate);
        
        compareFieldValue(provisioningGroupsToUpdate, "displayName",
            grouperTargetGroup.getDisplayName(), targetProvisioningGroup.getDisplayName(),
            grouperTargetGroup);
        
        compareFieldValue(provisioningGroupsToUpdate, "name",
            grouperTargetGroup.getName(), targetProvisioningGroup.getName(),
            grouperTargetGroup);
        
        compareFieldValue(provisioningGroupsToUpdate, "idIndex",
            grouperTargetGroup.getIdIndex(), targetProvisioningGroup.getIdIndex(),
            grouperTargetGroup);
        
        compareAttributeValues(provisioningGroupsToUpdate, grouperTargetGroup.getAttributes(),
            targetProvisioningGroup.getAttributes(), grouperTargetGroup);
        
      }
      
      this.grouperProvisioner.getGrouperProvisioningData().getTargetObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);
    }
    
    
  }

  protected void compareAttributeValues(
      List provisioningUpdatablesToUpdate,
      Map<String, ProvisioningAttribute> grouperTargetAttributes,
      Map<String, ProvisioningAttribute> targetProvisioningAttributes,
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    for (String attributeName: grouperTargetAttributes.keySet()) {
      
      ProvisioningAttribute targetAttribute = targetProvisioningAttributes.get(attributeName);
      ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeName);
      Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
      Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();

      if (targetAttribute == null) {
        
        if (GrouperUtil.isArrayOrCollection(grouperValue)) {
          if (grouperValue instanceof Collection) {
            for (Object value : (Collection)grouperValue) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.insert, null, value)
                  );
            }
          } else {
            // array
            for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
              Object value = Array.get(grouperValue, i);
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.insert, null, value)
                  );
            }
          }
        } else {
          // just a scalar
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                  ProvisioningObjectChangeAction.insert, null, grouperValue)
              );
          
        }
      } else {

        // update
        Collection<Object> targetCollection = null;
        if (targetValue != null) {
          if (targetValue instanceof Collection) {
            targetCollection = (Collection)targetValue;
          } else if (targetValue.getClass().isArray()) {
            targetCollection = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(targetValue);i++) {
              targetCollection.add(Array.get(targetValue, i));
            }
          }
        }
        Collection<Object> grouperCollection = null;
        if (grouperValue != null) {
          if (grouperValue instanceof Collection) {
            grouperCollection = (Collection)grouperValue;
          } else if (grouperValue.getClass().isArray()) {
            grouperCollection = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
              grouperCollection.add(Array.get(grouperValue, i));
            }
          }
        }
        // scalar
        if (grouperCollection == null && targetCollection == null) {
          if (!attributeValueEquals(grouperValue, targetValue)) {
            
            grouperProvisioningUpdatable.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.update, targetValue, grouperValue)
                );
            continue;
          }
          if (grouperCollection != null || targetCollection != null) {
            if (grouperCollection == null) {
              grouperCollection = new HashSet<Object>();
              if (grouperValue != null) {
                grouperCollection.add(grouperValue);
              }
            }
            if (targetCollection == null) {
              targetCollection = new HashSet<Object>();
              if (targetValue != null) {
                targetCollection.add(targetValue);
              }
            }
            
          }
          
          Collection inserts = new HashSet<Object>(grouperCollection);
          inserts.removeAll(targetCollection);
          
          for (Object insertValue : inserts) {
            grouperProvisioningUpdatable.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, insertValue)
                );
  
          }
          
          Collection deletes = new HashSet<Object>(targetCollection);
          deletes.removeAll(grouperCollection);
          
          for (Object deleteValue : deletes) {
            grouperProvisioningUpdatable.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.delete, deleteValue, null)
                );
  
          }
        }        
        
      }
      
    }
    
    for (String attributeName: targetProvisioningAttributes.keySet()) {
      
      ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeName);
      if (grouperAttribute == null) {
        Object targetValue = targetProvisioningAttributes.get(attributeName);
        
        if (GrouperUtil.isArrayOrCollection(targetValue)) {
          if (targetValue instanceof Collection) {
            for (Object value : (Collection)targetValue) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, value, null)
                  );
            }
          } else {
            // array
            for (int i=0;i<GrouperUtil.length(targetValue);i++) {
              Object value = Array.get(targetValue, i);
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, value, null)
                  );
            }
          }
          
          // indicate the attribute itself is gone
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
              ProvisioningObjectChangeAction.delete, null, null)
          );
          
        } else {
          // just a scalar
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                  ProvisioningObjectChangeAction.delete, targetValue, null)
              );
          
        }
      }
      
    }
    if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
      addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
          grouperProvisioningUpdatable);
    }
  }

  private void addProvisioningUpdatableToUpdateIfNotThere(List provisioningUpdatablesToUpdate, 
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    // if there is nothing in the update list or this object is not last in there, then add it
    if (GrouperUtil.length(provisioningUpdatablesToUpdate) == 0 || 
        provisioningUpdatablesToUpdate.get(provisioningUpdatablesToUpdate.size()-1) != grouperProvisioningUpdatable) {
      provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
    }
    
  }
  
  protected void compareFieldValue(List provisioningUpdatablesToUpdate,
      String fieldName,
      Object grouperValue, Object targetValue,
      ProvisioningUpdatable grouperTargetUpdatable) {
    if (!GrouperUtil.equals(grouperValue, targetValue)) {
      addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
          grouperTargetUpdatable);
      grouperTargetUpdatable.addInternal_objectChange(
          new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, fieldName, null, 
              attributeChangeType(grouperValue, targetValue), targetValue, grouperValue)
          );
    }
  }
  
  
  
  
  protected ProvisioningObjectChangeAction attributeChangeType(Object first, Object second) {
    if (first == null) return ProvisioningObjectChangeAction.insert;
    if (second == null) return ProvisioningObjectChangeAction.delete;
    return ProvisioningObjectChangeAction.update;
  }
  
  protected boolean attributeValueEquals(Object first, Object second) {
    
    // update
    Collection<Object> firstCollection = null;
    if (first != null) {
      if (first instanceof Collection) {
        firstCollection = (Collection)first;
      } else if (first.getClass().isArray()) {
        firstCollection = new HashSet<Object>();
        for (int i=0;i<GrouperUtil.length(first);i++) {
          firstCollection.add(Array.get(first, i));
        }
      }
    }
    Collection<Object> secondCollection = null;
    if (second != null) {
      if (second instanceof Collection) {
        secondCollection = (Collection)second;
      } else if (second.getClass().isArray()) {
        secondCollection = new HashSet<Object>();
        for (int i=0;i<GrouperUtil.length(second);i++) {
          secondCollection.add(Array.get(second, i));
        }
      }
    }

    if (firstCollection != null || secondCollection != null) {
      if (firstCollection == null) {
        firstCollection = new HashSet<Object>();
        if (first != null) {
          firstCollection.add(first);
        }
      }
      if (secondCollection == null) {
        secondCollection = new HashSet<Object>();
        if (second != null) {
          secondCollection.add(second);
        }
      }
      
    }

    // scalar
    if (firstCollection == null && secondCollection == null) {
      return GrouperUtil.equals(first, second);
    }

      
    if (GrouperUtil.length(firstCollection) == 0 && GrouperUtil.length(secondCollection) == 0) {
      return true;
    }
      
    Collection intersection = CollectionUtils.intersection(firstCollection, secondCollection);
    
    if (GrouperUtil.length(firstCollection) == intersection.size() && GrouperUtil.length(secondCollection) == intersection.size()) {
      return true;
    }
      
    return false;
      
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

  

  public void indexTargetEntities() {
  
    Map<Object, ProvisioningEntityWrapper> targetEntityIdToProvisioningEntityWrapper = new HashMap<Object, ProvisioningEntityWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setTargetEntityIdToProvisioningEntityWrapper(targetEntityIdToProvisioningEntityWrapper);
  
    int grouperTargetEntitiesWithNullTargetIds = 0;
    for (ProvisioningEntity grouperTargetEntity : GrouperUtil.nonNull(
        this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningEntities())) {
      
      Object targetId = grouperTargetEntity.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetEntitiesWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetEntityIdToProvisioningEntityWrapper.containsKey(targetId)) {
        throw new NullPointerException("Why do multiple entities from grouper have the same target id???\n" 
            + grouperTargetEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(targetId));
      }
  
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper == null) {
        throw new NullPointerException("Cant find entity wrapper: " + grouperTargetEntity);
      }
      targetEntityIdToProvisioningEntityWrapper.put(targetId, provisioningEntityWrapper);
    }
    
    if (grouperTargetEntitiesWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetEntitiesWithNullTargetIds", grouperTargetEntitiesWithNullTargetIds);
    }

    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningEntity targetProvisioningEntity : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningEntities())) {
      
      Object targetId = targetProvisioningEntity.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningEntity! " + targetProvisioningEntity);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple entities from target have the same target id???\n" 
            + targetProvisioningEntity + "\n" + targetEntityIdToProvisioningEntityWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);

      ProvisioningEntityWrapper provisioningEntityWrapperReal = targetEntityIdToProvisioningEntityWrapper.get(targetId);
      if (provisioningEntityWrapperReal == null) {
        provisioningEntityWrapperReal = new ProvisioningEntityWrapper();
        targetEntityIdToProvisioningEntityWrapper.put(targetId, provisioningEntityWrapperReal);
      }

      ProvisioningEntityWrapper provisioningEntityWrapperTarget = targetProvisioningEntity.getProvisioningEntityWrapper();

      // lets merge these to get our complete wrapper
      targetProvisioningEntity.setProvisioningEntityWrapper(provisioningEntityWrapperReal);
      provisioningEntityWrapperReal.setTargetProvisioningEntity(targetProvisioningEntity);
      if (provisioningEntityWrapperTarget != null) {
        provisioningEntityWrapperReal.setTargetNativeEntity(provisioningEntityWrapperTarget.getTargetNativeEntity());
      }
    }
  
  }



  public void indexTargetGroups() {
  
    Map<Object, ProvisioningGroupWrapper> targetGroupIdToProvisioningGroupWrapper = new HashMap<Object, ProvisioningGroupWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setTargetGroupIdToProvisioningGroupWrapper(targetGroupIdToProvisioningGroupWrapper);
  
    int grouperTargetGroupsWithNullTargetIds = 0;
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(
        this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningGroups())) {
      
      Object targetId = grouperTargetGroup.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetGroupsWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetGroupIdToProvisioningGroupWrapper.containsKey(targetId)) {
        throw new NullPointerException("Why do multiple groups from grouper have the same target id???\n" 
            + grouperTargetGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(targetId));
      }
  
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        throw new NullPointerException("Cant find group wrapper: " + grouperTargetGroup);
      }
      targetGroupIdToProvisioningGroupWrapper.put(targetId, provisioningGroupWrapper);
    }
    
    if (grouperTargetGroupsWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetGroupsWithNullTargetIds", grouperTargetGroupsWithNullTargetIds);
    }
  
    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningGroup targetProvisioningGroup : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningGroups())) {
      
      Object targetId = targetProvisioningGroup.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningGroup! " + targetProvisioningGroup);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple groups from target have the same target id???\n" 
            + targetProvisioningGroup + "\n" + targetGroupIdToProvisioningGroupWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);
  
      ProvisioningGroupWrapper provisioningGroupWrapperReal = targetGroupIdToProvisioningGroupWrapper.get(targetId);
      if (provisioningGroupWrapperReal == null) {
        provisioningGroupWrapperReal = new ProvisioningGroupWrapper();
        targetGroupIdToProvisioningGroupWrapper.put(targetId, provisioningGroupWrapperReal);
      }
  
      ProvisioningGroupWrapper provisioningGroupWrapperTarget = targetProvisioningGroup.getProvisioningGroupWrapper();
  
      // lets merge these to get our complete wrapper
      targetProvisioningGroup.setProvisioningGroupWrapper(provisioningGroupWrapperReal);
      provisioningGroupWrapperReal.setTargetProvisioningGroup(targetProvisioningGroup);
      if (provisioningGroupWrapperTarget != null) {
        provisioningGroupWrapperReal.setTargetNativeGroup(provisioningGroupWrapperTarget.getTargetNativeGroup());
      }
    }
  
  }


  public void indexTargetMemberships() {
  
    Map<Object, ProvisioningMembershipWrapper> targetMembershipIdToProvisioningMembershipWrapper = new HashMap<Object, ProvisioningMembershipWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setTargetMembershipIdToProvisioningMembershipWrapper(targetMembershipIdToProvisioningMembershipWrapper);
  
    int grouperTargetMembershipsWithNullTargetIds = 0;
    for (ProvisioningMembership grouperTargetMembership : GrouperUtil.nonNull(
        this.grouperProvisioner.getGrouperProvisioningData().getGrouperTargetObjects().getProvisioningMemberships())) {
      
      Object targetId = grouperTargetMembership.getTargetId();
      if (targetId == null) {
        // this could be an insert?
        grouperTargetMembershipsWithNullTargetIds++;
        // TODO make sure to handle this in the compare
        continue;
      }
      
      if (targetMembershipIdToProvisioningMembershipWrapper.containsKey(targetId)) {
        throw new NullPointerException("Why do multiple memberships from grouper have the same target id???\n" 
            + grouperTargetMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(targetId));
      }
  
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper == null) {
        throw new NullPointerException("Cant find membership wrapper: " + grouperTargetMembership);
      }
      targetMembershipIdToProvisioningMembershipWrapper.put(targetId, provisioningMembershipWrapper);
    }
    
    if (grouperTargetMembershipsWithNullTargetIds > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperTargetMembershipsWithNullTargetIds", grouperTargetMembershipsWithNullTargetIds);
    }
  
    // make sure we arent double dipping target provisioning target ids
    Set<Object> targetProvisioningTargetIds = new HashSet<Object>();
    for (ProvisioningMembership targetProvisioningMembership : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetProvisioningObjects().getProvisioningMemberships())) {
      
      Object targetId = targetProvisioningMembership.getTargetId();
      if (targetId == null) {
        throw new NullPointerException("Cant find id for targetProvisioningMembership! " + targetProvisioningMembership);
      }
      
      if (targetProvisioningTargetIds.contains(targetId)) {
        throw new NullPointerException("Why do multiple memberships from target have the same target id???\n" 
            + targetProvisioningMembership + "\n" + targetMembershipIdToProvisioningMembershipWrapper.get(targetId));
      }
      targetProvisioningTargetIds.add(targetId);
  
      ProvisioningMembershipWrapper provisioningMembershipWrapperReal = targetMembershipIdToProvisioningMembershipWrapper.get(targetId);
      if (provisioningMembershipWrapperReal == null) {
        provisioningMembershipWrapperReal = new ProvisioningMembershipWrapper();
        targetMembershipIdToProvisioningMembershipWrapper.put(targetId, provisioningMembershipWrapperReal);
      }
  
      ProvisioningMembershipWrapper provisioningMembershipWrapperTarget = targetProvisioningMembership.getProvisioningMembershipWrapper();
  
      // lets merge these to get our complete wrapper
      targetProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapperReal);
      provisioningMembershipWrapperReal.setTargetProvisioningMembership(targetProvisioningMembership);
      if (provisioningMembershipWrapperTarget != null) {
        provisioningMembershipWrapperReal.setTargetNativeMembership(provisioningMembershipWrapperTarget.getTargetNativeMembership());
      }
      
      
      
    }
  
  }

  /**
   * get data from change log
   */
  public void retrieveIncrementalData() {
    
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    this.grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    // lets get the grouper side first
    
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    
    Thread targetQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.getGrouperProvisioner().retrieveTargetDao().retrieveAllData();
        } catch (RuntimeException re) {
          LOG.error("error querying target: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    targetQueryThread.start();

    final RuntimeException[] RUNTIME_EXCEPTION2 = new RuntimeException[1];
    
    Thread grouperSyncQueryThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperDao().retrieveSyncData(GrouperProvisioningLogic.this.grouperProvisioner.getGrouperProvisioningType());
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

    GrouperClientUtils.join(grouperSyncQueryThread);
    if (RUNTIME_EXCEPTION2[0] != null) {
      throw RUNTIME_EXCEPTION2[0];
    }

    this.grouperProvisioner.retrieveGrouperDao().fixSyncObjects();
    
    this.grouperProvisioner.retrieveGrouperDao().calculateProvisioningDataToDelete();
    
    GrouperClientUtils.join(targetQueryThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }

  }


}
