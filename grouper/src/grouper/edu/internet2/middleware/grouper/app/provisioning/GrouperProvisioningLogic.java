package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

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
   * see if there are any objects that need to be fixed or removed
   */
  public void validateInitialProvisioningData() {
    
    
  }
  

  /**
   * 
   */
  public void fullProvisionFull() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    try {
      debugMap.put("state", "retrieveAllData");
      long start = System.currentTimeMillis();
      retrieveAllData();
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
  
      debugMap.put("state", "translateGrouperToCommon");
      this.grouperProvisioner.retrieveTranslator().translateGrouperToCommon();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateGrouperToCommon");
    }

    try {
      debugMap.put("state", "translateTargetToCommon");
      this.grouperProvisioner.retrieveTranslator().translateTargetToCommon();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateTargetToCommon");
    }
    
    debugMap.put("state", "indexCommonObjects");
    this.indexCommonObjects();

    try {
      debugMap.put("state", "compareCommonObjects");
      this.compareCommonObjects();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("compareCommonObjects");
    }

    try {
      debugMap.put("state", "translateCommonToTarget");
      this.grouperProvisioner.retrieveTranslator().translateCommonToTarget();
    } finally {
      this.getGrouperProvisioner().getGrouperProvisioningObjectLog().debug("translateCommonToTarget");
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

  public void indexCommonObjects() {
    this.indexCommonObjectsGroups();
    this.indexCommonObjectsEntities();
    this.indexCommonObjectsMemberships();
  }

  public void indexCommonObjectsGroups() {

    Map<String, ProvisioningGroupWrapper> commonGroupIdToProvisioningGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonGroupIdToGroupWrapper(commonGroupIdToProvisioningGroupWrapper);

    for (ProvisioningGroup grouperCommonGroup : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getGrouperCommonObjects().getProvisioningGroups())) {
      
      String id = grouperCommonGroup.getId();
      if (StringUtils.isBlank(id)) {
        throw new NullPointerException("Cant find id for grouperCommonGroup! " + grouperCommonGroup);
      }
      
      if (commonGroupIdToProvisioningGroupWrapper.containsKey(id)) {
        throw new NullPointerException("Why do multiple groups from grouper have the same common id???\n" 
            + grouperCommonGroup + "\n" + commonGroupIdToProvisioningGroupWrapper.get(id));
      }

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperCommonGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper == null) {
        throw new NullPointerException("Cant find groupWrapper for grouperCommonGroup! " + grouperCommonGroup);
      }
      commonGroupIdToProvisioningGroupWrapper.put(id, provisioningGroupWrapper);
    }
    
    // make sure we arent double dipping target common ids
    Set<String> targetCommonIds = new HashSet<String>();
    for (ProvisioningGroup targetCommonGroup : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetCommonObjects().getProvisioningGroups())) {
      
      String id = targetCommonGroup.getId();
      if (StringUtils.isBlank(id)) {
        throw new NullPointerException("Cant find id for targetCommonGroup! " + targetCommonGroup);
      }
      
      if (targetCommonIds.contains(id)) {
        throw new NullPointerException("Why do multiple groups from target have the same common id???\n" 
            + targetCommonGroup + "\n" + commonGroupIdToProvisioningGroupWrapper.get(id));
      }
      targetCommonIds.add(id);
      
      ProvisioningGroupWrapper targetGroupWrapper = targetCommonGroup.getProvisioningGroupWrapper();
      if (targetGroupWrapper == null) {
        throw new NullPointerException("Cant find groupWrapper for targetCommonGroup! " + targetCommonGroup);
      }
      ProvisioningGroupWrapper grouperGroupWrapper = commonGroupIdToProvisioningGroupWrapper.get(id);
      
      // if there is no grouperGroupWrapper
      if (grouperGroupWrapper == null) {
        commonGroupIdToProvisioningGroupWrapper.put(id, targetGroupWrapper);
      } else {
        // lets merge these to get our complete wrapper
        grouperGroupWrapper.setTargetCommonGroup(targetCommonGroup);
        targetCommonGroup.setProvisioningGroupWrapper(grouperGroupWrapper);
        if (targetGroupWrapper.getTargetProvisioningGroup() != null) {
          grouperGroupWrapper.setTargetProvisioningGroup(targetGroupWrapper.getTargetProvisioningGroup());
          targetGroupWrapper.getTargetProvisioningGroup().setProvisioningGroupWrapper(grouperGroupWrapper);
        }
        grouperGroupWrapper.setTargetNativeGroup(targetGroupWrapper.getTargetNativeGroup());
      }
    }

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
          GrouperProvisioningLogic.this.grouperProvisioner.retrieveGrouperDao().retrieveAllSyncData();
        } catch (RuntimeException re) {
          LOG.error("error querying sync objects: " + GrouperProvisioningLogic.this.getGrouperProvisioner().getConfigId(), re);
          RUNTIME_EXCEPTION2[0] = re;
        }
        
      }
    });

    grouperSyncQueryThread.start();
    
    this.grouperProvisioner.retrieveGrouperDao().retrieveAllGrouperData();
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
  
  protected void compareCommonObjects() {
    GrouperProvisioningLists grouperCommonObjects = this.grouperProvisioner.getGrouperProvisioningData().getGrouperCommonObjects();
    GrouperProvisioningLists targetCommonObjects = this.grouperProvisioner.getGrouperProvisioningData().getTargetCommonObjects();
    
    compareCommonGroups(grouperCommonObjects.getProvisioningGroups(), targetCommonObjects.getProvisioningGroups());
    compareCommonEntities(grouperCommonObjects.getProvisioningEntities(), targetCommonObjects.getProvisioningEntities());
    compareCommonMemberships(grouperCommonObjects.getProvisioningMemberships(), targetCommonObjects.getProvisioningMemberships());

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    {
      int groupInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectInserts().getProvisioningGroups());
      if (groupInserts > 0) {
        debugMap.put("groupInsertsAfterCompare", groupInserts);
      }
    }
    {
      int groupUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectUpdates().getProvisioningGroups());
      if (groupUpdates > 0) {
        debugMap.put("groupUpdatesAfterCompare", groupUpdates);
      }
    }
    {
      int groupDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectDeletes().getProvisioningGroups());
      if (groupDeletes > 0) {
        debugMap.put("groupDeletesAfterCompare", groupDeletes);
      }
    }
    {
      int entityInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectInserts().getProvisioningEntities());
      if (entityInserts > 0) {
        debugMap.put("entityInsertsAfterCompare", entityInserts);
      }
    }
    {
      int entityUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectUpdates().getProvisioningEntities());
      if (entityUpdates > 0) {
        debugMap.put("entityUpdatesAfterCompare", entityUpdates);
      }
    }
    {
      int entityDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectDeletes().getProvisioningEntities());
      if (entityDeletes > 0) {
        debugMap.put("entityDeletesAfterCompare", entityDeletes);
      }
    }
    {
      int membershipInserts = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectInserts().getProvisioningMemberships());
      if (membershipInserts > 0) {
        debugMap.put("membershipInsertsAfterCompare", membershipInserts);
      }
    }
    {
      int membershipUpdates = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectUpdates().getProvisioningMemberships());
      if (membershipUpdates > 0) {
        debugMap.put("membershipUpdatesAfterCompare", membershipUpdates);
      }
    }
    {
      int membershipDeletes = GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getCommonObjectDeletes().getProvisioningMemberships());
      if (membershipDeletes > 0) {
        debugMap.put("membershipDeletesAfterCompare", membershipDeletes);
      }
    }
  
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
            grouperCommonMembership.getId(), targetCommonMembership.getId(),
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
        ProvisioningGroup groupToInsert = grouperCommonGroupIdToGroup.get(groupIdToInsert);
        provisioningGroupsToInsert.add(groupToInsert);
        for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(groupToInsert.getAttributes()).keySet())) {
          Object grouperValue = groupToInsert.getAttributes().get(attributeName).getValue();
          //TODO add fields
          if (GrouperUtil.isArrayOrCollection(grouperValue)) {
            if (grouperValue instanceof Collection) {
              for (Object value : (Collection)grouperValue) {
                groupToInsert.getInternal_objectChanges().add(
                    new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            } else {
              // array
              for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
                Object value = Array.get(grouperValue, i);
                groupToInsert.getInternal_objectChanges().add(
                    new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            }
          } else {
            // just a scalar
            groupToInsert.getInternal_objectChanges().add(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, grouperValue)
                );
            
          }
        }
      }
      
      this.grouperProvisioner.getGrouperProvisioningData()
      .getCommonObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
    
    
      // groups to delete
      Set<String> groupIdsToDelete = new HashSet<String>(targetCommonGroupIdToGroup.keySet());
      groupIdsToDelete.removeAll(grouperCommonGroupIdToGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
      
      for (String groupIdToDelete: groupIdsToDelete) {
        provisioningGroupsToDelete.add(targetCommonGroupIdToGroup.get(groupIdToDelete));
        
        //TODO add indiv fields and attributes
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
      ProvisioningAttribute grouperAttribute = grouperCommonAttributes.get(attributeName);
      Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
      Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();

      if (targetAttribute == null) {
        if (!alreadyAddedForUpdate) {
          
          grouperProvisioningUpdatable.setInternal_objectChanges(new HashSet<ProvisioningObjectChange>());
          provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
          alreadyAddedForUpdate = true;
        }
        
        if (GrouperUtil.isArrayOrCollection(grouperValue)) {
          if (grouperValue instanceof Collection) {
            for (Object value : (Collection)grouperValue) {
              grouperProvisioningUpdatable.getInternal_objectChanges().add(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.insert, null, value)
                  );
            }
          } else {
            // array
            for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
              Object value = Array.get(grouperValue, i);
              grouperProvisioningUpdatable.getInternal_objectChanges().add(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.insert, null, value)
                  );
            }
          }
        } else {
          // just a scalar
          grouperProvisioningUpdatable.getInternal_objectChanges().add(
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
            
            if (!alreadyAddedForUpdate) {
              grouperProvisioningUpdatable.setInternal_objectChanges(new HashSet<ProvisioningObjectChange>());
              provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
              alreadyAddedForUpdate = true;
            }

            grouperProvisioningUpdatable.getInternal_objectChanges().add(
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
            grouperProvisioningUpdatable.getInternal_objectChanges().add(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, insertValue)
                );
  
          }
          
          Collection deletes = new HashSet<Object>(targetCollection);
          deletes.removeAll(grouperCollection);
          
          for (Object deleteValue : deletes) {
            grouperProvisioningUpdatable.getInternal_objectChanges().add(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.delete, deleteValue, null)
                );
  
          }
        }        
        
      }
      
    }
    
    for (String attributeName: targetCommonAttributes.keySet()) {
      
      ProvisioningAttribute grouperAttribute = grouperCommonAttributes.get(attributeName);
      if (grouperAttribute == null) {
        Object targetValue = targetCommonAttributes.get(attributeName);
        // delete
        if (!alreadyAddedForUpdate) {
          grouperProvisioningUpdatable.setInternal_objectChanges(new HashSet<ProvisioningObjectChange>());
          provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
          alreadyAddedForUpdate = true;
        }
        
        if (GrouperUtil.isArrayOrCollection(targetValue)) {
          if (targetValue instanceof Collection) {
            for (Object value : (Collection)targetValue) {
              grouperProvisioningUpdatable.getInternal_objectChanges().add(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, value, null)
                  );
            }
          } else {
            // array
            for (int i=0;i<GrouperUtil.length(targetValue);i++) {
              Object value = Array.get(targetValue, i);
              grouperProvisioningUpdatable.getInternal_objectChanges().add(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, value, null)
                  );
            }
          }
          
          // indicate the attribute itself is gone
          grouperProvisioningUpdatable.getInternal_objectChanges().add(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
              ProvisioningObjectChangeAction.delete, null, null)
          );
          
        } else {
          // just a scalar
          grouperProvisioningUpdatable.getInternal_objectChanges().add(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                  ProvisioningObjectChangeAction.delete, targetValue, null)
              );
          
        }
      }
      
    }
  }

  protected boolean compareFieldValue(List provisioningUpdatablesToUpdate,
      String fieldName,
      Object grouperValue, Object targetValue,
      boolean alreadyAddedForUpdate, ProvisioningUpdatable grouperCommonUpdatable) {
    if (!GrouperUtil.equals(grouperValue, targetValue)) {
      if (!alreadyAddedForUpdate) {
        grouperCommonUpdatable.setInternal_objectChanges(new HashSet<ProvisioningObjectChange>());
        provisioningUpdatablesToUpdate.add(grouperCommonUpdatable);
        alreadyAddedForUpdate = true;
      }
      
      grouperCommonUpdatable.getInternal_objectChanges().add(
          new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, fieldName, null, 
              attributeChangeType(grouperValue, targetValue), targetValue, grouperValue)
          );
    }
    return alreadyAddedForUpdate;
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


  public void indexCommonObjectsEntities() {
  
    Map<String, ProvisioningEntityWrapper> commonEntityIdToProvisioningEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonEntityIdToEntityWrapper(commonEntityIdToProvisioningEntityWrapper);
  
    for (ProvisioningEntity grouperCommonEntity : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getGrouperCommonObjects().getProvisioningEntities())) {
      
      String id = grouperCommonEntity.getId();
      if (StringUtils.isBlank(id)) {
        throw new NullPointerException("Cant find id for grouperCommonEntity! " + grouperCommonEntity);
      }
      
      if (commonEntityIdToProvisioningEntityWrapper.containsKey(id)) {
        throw new NullPointerException("Why do multiple entities from grouper have the same common id???\n" 
            + grouperCommonEntity + "\n" + commonEntityIdToProvisioningEntityWrapper.get(id));
      }
  
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperCommonEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper == null) {
        throw new NullPointerException("Cant find entityWrapper for grouperCommonEntity! " + grouperCommonEntity);
      }
      commonEntityIdToProvisioningEntityWrapper.put(id, provisioningEntityWrapper);
    }
    
    // make sure we arent double dipping target common ids
    Set<String> targetCommonIds = new HashSet<String>();
    for (ProvisioningEntity targetCommonEntity : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetCommonObjects().getProvisioningEntities())) {
      
      String id = targetCommonEntity.getId();
      if (StringUtils.isBlank(id)) {
        throw new NullPointerException("Cant find id for targetCommonEntity! " + targetCommonEntity);
      }
      
      if (targetCommonIds.contains(id)) {
        throw new NullPointerException("Why do multiple entities from target have the same common id???\n" 
            + targetCommonEntity + "\n" + commonEntityIdToProvisioningEntityWrapper.get(id));
      }
      targetCommonIds.add(id);
      
      ProvisioningEntityWrapper targetEntityWrapper = targetCommonEntity.getProvisioningEntityWrapper();
      if (targetEntityWrapper == null) {
        throw new NullPointerException("Cant find entityWrapper for targetCommonEntity! " + targetCommonEntity);
      }
      ProvisioningEntityWrapper grouperEntityWrapper = commonEntityIdToProvisioningEntityWrapper.get(id);
      
      // if there is no grouperEntityWrapper
      if (grouperEntityWrapper == null) {
        commonEntityIdToProvisioningEntityWrapper.put(id, targetEntityWrapper);
      } else {
        // lets merge these to get our complete wrapper
        grouperEntityWrapper.setTargetCommonEntity(targetCommonEntity);
        targetCommonEntity.setProvisioningEntityWrapper(grouperEntityWrapper);
        if (targetEntityWrapper.getTargetProvisioningEntity() != null) {
          grouperEntityWrapper.setTargetProvisioningEntity(targetEntityWrapper.getTargetProvisioningEntity());
          targetEntityWrapper.getTargetProvisioningEntity().setProvisioningEntityWrapper(grouperEntityWrapper);
        }
        grouperEntityWrapper.setTargetNativeEntity(targetEntityWrapper.getTargetNativeEntity());
      }
    }
  
  }


  public void indexCommonObjectsMemberships() {
  
    Map<MultiKey, ProvisioningMembershipWrapper> commonGroupIdEntityIdToProvisioningMembershipWrapper = new HashMap<MultiKey, ProvisioningMembershipWrapper>();
    this.grouperProvisioner.getGrouperProvisioningData().setCommonGroupIdEntityIdToMembershipWrapper(commonGroupIdEntityIdToProvisioningMembershipWrapper);

    int grouperCommonMembershipDupes = 0;
    for (ProvisioningMembership grouperCommonMembership : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getGrouperCommonObjects().getProvisioningMemberships())) {
      
      String groupId = grouperCommonMembership.getProvisioningGroupId();
      if (StringUtils.isBlank(groupId)) {
        throw new NullPointerException("Cant find groupId for grouperCommonMembership! " + grouperCommonMembership);
      }
      String entityId = grouperCommonMembership.getProvisioningEntityId();
      if (StringUtils.isBlank(entityId)) {
        throw new NullPointerException("Cant find entityId for grouperCommonMembership! " + grouperCommonMembership);
      }
      MultiKey groupIdEntityId = new MultiKey(groupId, entityId);
      
      if (commonGroupIdEntityIdToProvisioningMembershipWrapper.containsKey(groupIdEntityId)) {
        
//        throw new NullPointerException("Why do multiple memberships from grouper have the same common id???\n" 
//            + grouperCommonMembership + "\n" + commonMembershipIdToProvisioningMembershipWrapper.get(groupIdEntityId));
        
        // i think this is ok
        grouperCommonMembershipDupes++;
        continue;
        
      }
  
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperCommonMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper == null) {
        throw new NullPointerException("Cant find membershipWrapper for grouperCommonMembership! " + grouperCommonMembership);
      }
      commonGroupIdEntityIdToProvisioningMembershipWrapper.put(groupIdEntityId, provisioningMembershipWrapper);
    }

    if (grouperCommonMembershipDupes > 0) {
      this.getGrouperProvisioner().getDebugMap().put("grouperCommonMembershipDupes", grouperCommonMembershipDupes);
    }

    int targetCommonMembershipDupes = 0;

    
    // make sure we arent double dipping target common ids
    Set<MultiKey> targetCommonIds = new HashSet<MultiKey>();
    for (ProvisioningMembership targetCommonMembership : 
      GrouperUtil.nonNull(this.grouperProvisioner.getGrouperProvisioningData()
          .getTargetCommonObjects().getProvisioningMemberships())) {
      
      String groupId = targetCommonMembership.getProvisioningGroupId();
      if (StringUtils.isBlank(groupId)) {
        throw new NullPointerException("Cant find groupId for targetCommonMembership! " + targetCommonMembership);
      }
      String entityId = targetCommonMembership.getProvisioningEntityId();
      if (StringUtils.isBlank(entityId)) {
        throw new NullPointerException("Cant find entityId for targetCommonMembership! " + targetCommonMembership);
      }
      MultiKey groupIdEntityId = new MultiKey(groupId, entityId);
      
      if (targetCommonIds.contains(groupIdEntityId)) {
//        throw new NullPointerException("Why do multiple memberships from target have the same common id???\n" 
//            + targetCommonMembership + "\n" + commonMembershipIdToProvisioningMembershipWrapper.get(id));
        
        // this is probably ok
        targetCommonMembershipDupes++;

        continue;
      }
      targetCommonIds.add(groupIdEntityId);
      
      ProvisioningMembershipWrapper targetMembershipWrapper = targetCommonMembership.getProvisioningMembershipWrapper();
      if (targetMembershipWrapper == null) {
        throw new NullPointerException("Cant find membershipWrapper for targetCommonMembership! " + targetCommonMembership);
      }
      ProvisioningMembershipWrapper grouperMembershipWrapper = commonGroupIdEntityIdToProvisioningMembershipWrapper.get(groupIdEntityId);
      
      // if there is no grouperMembershipWrapper
      if (grouperMembershipWrapper == null) {
        commonGroupIdEntityIdToProvisioningMembershipWrapper.put(groupIdEntityId, targetMembershipWrapper);
      } else {
        // lets merge these to get our complete wrapper
        grouperMembershipWrapper.setTargetCommonMembership(targetCommonMembership);
        targetCommonMembership.setProvisioningMembershipWrapper(grouperMembershipWrapper);
        if (targetMembershipWrapper.getTargetProvisioningMembership() != null) {
          grouperMembershipWrapper.setTargetProvisioningMembership(targetMembershipWrapper.getTargetProvisioningMembership());
          targetMembershipWrapper.getTargetProvisioningMembership().setProvisioningMembershipWrapper(grouperMembershipWrapper);
        }
        grouperMembershipWrapper.setTargetNativeMembership(targetMembershipWrapper.getTargetNativeMembership());
      }
    }
    if (targetCommonMembershipDupes > 0) {
      this.getGrouperProvisioner().getDebugMap().put("targetCommonMembershipDupes", targetCommonMembershipDupes);
    }

  }


}
