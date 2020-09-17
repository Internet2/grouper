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

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisioningCompare {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  private void addProvisioningUpdatableToUpdateIfNotThere(List provisioningUpdatablesToUpdate, 
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    // if there is nothing in the update list or this object is not last in there, then add it
    if (GrouperUtil.length(provisioningUpdatablesToUpdate) == 0 || 
        provisioningUpdatablesToUpdate.get(provisioningUpdatablesToUpdate.size()-1) != grouperProvisioningUpdatable) {
      provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
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


  public void compareAttributesForDelete(ProvisioningUpdatable provisioningUpdatableToDelete) {
    for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(provisioningUpdatableToDelete.getAttributes()).keySet())) {
      Object grouperValue = provisioningUpdatableToDelete.getAttributes().get(attributeName).getValue();
  
      if (GrouperUtil.isArrayOrCollection(grouperValue)) {
        if (grouperValue instanceof Collection) {
          for (Object value : (Collection)grouperValue) {
            provisioningUpdatableToDelete.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.delete, null, value)
                );
          }
        } else {
          // array
          for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
            Object value = Array.get(grouperValue, i);
            provisioningUpdatableToDelete.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.delete, null, value)
                );
          }
        }
      } else {
        // just a scalar
        ProvisioningObjectChange provisioningObjectChange = new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
            ProvisioningObjectChangeAction.delete, null, grouperValue);
        provisioningUpdatableToDelete.addInternal_objectChange(provisioningObjectChange);
        
      }
    }
  }


  public void compareAttributesForInsert(ProvisioningUpdatable provisioningUpdatableToInsert) {
    for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(provisioningUpdatableToInsert.getAttributes()).keySet())) {
      Object grouperValue = provisioningUpdatableToInsert.getAttributes().get(attributeName).getValue();
  
      if (GrouperUtil.isArrayOrCollection(grouperValue)) {
        if (grouperValue instanceof Collection) {
          for (Object value : (Collection)grouperValue) {
            provisioningUpdatableToInsert.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, value)
                );
          }
        } else {
          // array
          for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
            Object value = Array.get(grouperValue, i);
            provisioningUpdatableToInsert.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, value)
                );
          }
        }
      } else {
        // just a scalar
        ProvisioningObjectChange provisioningObjectChange = new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
            ProvisioningObjectChangeAction.insert, null, grouperValue);
        provisioningUpdatableToInsert.addInternal_objectChange(provisioningObjectChange);
        
      }
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
          if (grouperCollection == null || targetCollection == null) {
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
        ProvisioningEntity entityToInsert = grouperTargetIdToTargetEntity.get(entityIdToInsert);
        if (entityToInsert.getId() != null) {
          entityToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.insert, null, entityToInsert.getId()));
        }
        if (entityToInsert.getLoginId() != null) {
          entityToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "loginId", null, 
                  ProvisioningObjectChangeAction.insert, null, entityToInsert.getLoginId()));
        }
        if (entityToInsert.getName() != null) {
          entityToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                  ProvisioningObjectChangeAction.insert, null, entityToInsert.getName()));
        }
        if (entityToInsert.getEmail() != null) {
          entityToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "email", null, 
                  ProvisioningObjectChangeAction.insert, null, entityToInsert.getEmail()));
        }
  
        compareAttributesForInsert(entityToInsert);
  
        provisioningEntitiesToInsert.add(entityToInsert);
      }
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
    
    
      // entities to delete
      Set<Object> entityIdsToDelete = new HashSet<Object>(targetTargetIdToTargetEntity.keySet());
      entityIdsToDelete.removeAll(grouperTargetIdToTargetEntity.keySet());
      
      List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();
      
      for (Object entityIdToDelete: entityIdsToDelete) {
        ProvisioningEntity entityToDelete = targetTargetIdToTargetEntity.get(entityIdToDelete);
        
        if (entityToDelete.getId() != null) {
          entityToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.delete, entityToDelete.getId(), null));
        }
        if (entityToDelete.getLoginId() != null) {
          entityToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "loginId", null, 
                  ProvisioningObjectChangeAction.delete, entityToDelete.getLoginId(), null));
        }
        if (entityToDelete.getName() != null) {
          entityToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                  ProvisioningObjectChangeAction.delete, entityToDelete.getName(), null));
        }
        if (entityToDelete.getEmail() != null) {
          entityToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "email", null, 
                  ProvisioningObjectChangeAction.delete, entityToDelete.getEmail(), null));
        }
  
        compareAttributesForDelete(entityToDelete);

        provisioningEntitiesToDelete.add(entityToDelete);
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes().setProvisioningEntities(provisioningEntitiesToDelete);
    
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
      
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);
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
        
        if (groupToInsert.getId() != null) {
          groupToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.insert, null, groupToInsert.getId()));
        }
        if (groupToInsert.getIdIndex() != null) {
          groupToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "idIndex", null, 
                  ProvisioningObjectChangeAction.insert, null, groupToInsert.getIdIndex()));
        }
        if (groupToInsert.getDisplayName() != null) {
          groupToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "displayName", null, 
                  ProvisioningObjectChangeAction.insert, null, groupToInsert.getDisplayName()));
        }
        if (groupToInsert.getName() != null) {
          groupToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                  ProvisioningObjectChangeAction.insert, null, groupToInsert.getName()));
        }
        
        compareAttributesForInsert(groupToInsert);
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningData()
      .getTargetObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
    
    
      // groups to delete
      Set<Object> groupIdsToDelete = new HashSet<Object>(targetTargetIdToTargetGroup.keySet());
      groupIdsToDelete.removeAll(grouperTargetIdToTargetGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
      
      for (Object groupIdToDelete: groupIdsToDelete) {
        ProvisioningGroup groupToDelete = targetTargetIdToTargetGroup.get(groupIdToDelete);
        provisioningGroupsToDelete.add(groupToDelete);
        
        if (groupToDelete.getId() != null) {
          groupToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.delete, groupToDelete.getId(), null));
        }
        if (groupToDelete.getIdIndex() != null) {
          groupToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "idIndex", null, 
                  ProvisioningObjectChangeAction.delete, groupToDelete.getIdIndex(), null));
        }
        if (groupToDelete.getDisplayName() != null) {
          groupToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "displayName", null, 
                  ProvisioningObjectChangeAction.delete, groupToDelete.getDisplayName(), null));
        }
        if (groupToDelete.getName() != null) {
          groupToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                  ProvisioningObjectChangeAction.delete, groupToDelete.getName(), null));
        }
        
        compareAttributesForDelete(groupToDelete);

        
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningData()
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
      
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);
  
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
        ProvisioningMembership membershipToInsert = grouperTargetIdToTargetMembership.get(groupIdEntityIdToInsert);
        if (membershipToInsert.getId() != null) {
          membershipToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.insert, null, membershipToInsert.getId()));
        }
        if (membershipToInsert.getProvisioningGroupId() != null) {
          membershipToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "provisioningGroupId", null, 
                  ProvisioningObjectChangeAction.insert, null, membershipToInsert.getProvisioningGroupId()));
        }
        if (membershipToInsert.getProvisioningEntityId() != null) {
          membershipToInsert.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "provisioningEntityId", null, 
                  ProvisioningObjectChangeAction.insert, null, membershipToInsert.getProvisioningEntityId()));
        }
        compareAttributesForInsert(membershipToInsert);
  
        provisioningMembershipsToInsert.add(membershipToInsert);
      }
  
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectInserts().setProvisioningMemberships(provisioningMembershipsToInsert);
    
    
      // memberships to delete
      Set<Object> groupIdEntityIdsToDelete = new HashSet<Object>(targetTargetIdToTargetMembership.keySet());
      groupIdEntityIdsToDelete.removeAll(grouperTargetIdToTargetMembership.keySet());
      
      List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();
      
      for (Object targetIdsToDelete: groupIdEntityIdsToDelete) {
        ProvisioningMembership membershipToDelete = targetTargetIdToTargetMembership.get(targetIdsToDelete);
        provisioningMembershipsToDelete.add(membershipToDelete);
        
        if (membershipToDelete.getId() != null) {
          membershipToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                  ProvisioningObjectChangeAction.delete, membershipToDelete.getId(), null));
        }
        if (membershipToDelete.getProvisioningGroupId() != null) {
          membershipToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "provisioningGroupId", null, 
                  ProvisioningObjectChangeAction.delete, membershipToDelete.getProvisioningGroupId(), null));
        }
        if (membershipToDelete.getProvisioningEntityId() != null) {
          membershipToDelete.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "provisioningEntityId", null, 
                  ProvisioningObjectChangeAction.delete, membershipToDelete.getProvisioningEntityId(), null));
        }
        compareAttributesForDelete(membershipToDelete);

      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectDeletes().setProvisioningMemberships(provisioningMembershipsToDelete);
    
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
      
      this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
    }
    
    
  }


  protected void compareTargetObjects() {
    GrouperProvisioningLists grouperTargetObjects = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperTargetObjects();
    GrouperProvisioningLists targetProvisioningObjects = this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects();
    
    compareTargetGroups(grouperTargetObjects.getProvisioningGroups(), targetProvisioningObjects.getProvisioningGroups());
    compareTargetEntities(grouperTargetObjects.getProvisioningEntities(), targetProvisioningObjects.getProvisioningEntities());
    compareTargetMemberships(grouperTargetObjects.getProvisioningMemberships(), targetProvisioningObjects.getProvisioningMemberships());
  
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    {
      int groupInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningGroups());
      if (groupInserts > 0) {
        debugMap.put("groupInsertsAfterCompare", groupInserts);
      }
    }
    {
      int groupUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningGroups());
      if (groupUpdates > 0) {
        debugMap.put("groupUpdatesAfterCompare", groupUpdates);
      }
    }
    {
      int groupDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningGroups());
      if (groupDeletes > 0) {
        debugMap.put("groupDeletesAfterCompare", groupDeletes);
      }
    }
    {
      int entityInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningEntities());
      if (entityInserts > 0) {
        debugMap.put("entityInsertsAfterCompare", entityInserts);
      }
    }
    {
      int entityUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningEntities());
      if (entityUpdates > 0) {
        debugMap.put("entityUpdatesAfterCompare", entityUpdates);
      }
    }
    {
      int entityDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningEntities());
      if (entityDeletes > 0) {
        debugMap.put("entityDeletesAfterCompare", entityDeletes);
      }
    }
    {
      int membershipInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectInserts().getProvisioningMemberships());
      if (membershipInserts > 0) {
        debugMap.put("membershipInsertsAfterCompare", membershipInserts);
      }
    }
    {
      int membershipUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectUpdates().getProvisioningMemberships());
      if (membershipUpdates > 0) {
        debugMap.put("membershipUpdatesAfterCompare", membershipUpdates);
      }
    }
    {
      int membershipDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getTargetObjectDeletes().getProvisioningMemberships());
      if (membershipDeletes > 0) {
        debugMap.put("membershipDeletesAfterCompare", membershipDeletes);
      }
    }
  
  }
  
  
}
