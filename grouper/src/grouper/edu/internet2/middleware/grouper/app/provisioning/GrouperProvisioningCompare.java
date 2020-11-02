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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

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


  protected ProvisioningObjectChangeAction attributeChangeType(Object old, Object theNew) {
    if (old == null) return ProvisioningObjectChangeAction.insert;
    if (theNew == null) return ProvisioningObjectChangeAction.delete;
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
      if (!provisioningUpdatableToInsert.canInsertAttribute(attributeName)) {
        continue;
      }
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
        
        if (grouperProvisioningUpdatable.canInsertAttribute(attributeName)) {
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
            if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.update, targetValue, grouperValue)
                  );
            }
          }
          continue;
        }

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
          
        Collection inserts = new HashSet<Object>(grouperCollection);
        inserts.removeAll(targetCollection);
        if (grouperProvisioningUpdatable.canInsertAttribute(attributeName)) {
          for (Object insertValue : inserts) {
            grouperProvisioningUpdatable.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                    ProvisioningObjectChangeAction.insert, null, insertValue)
                );
  
          }
        }        
        Collection deletes = new HashSet<Object>(targetCollection);
        deletes.removeAll(grouperCollection);
        if (grouperProvisioningUpdatable.canDeleteAttrbute(attributeName)) {
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
      if (grouperProvisioningUpdatable.canDeleteAttrbute(attributeName)) {
  
        ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeName);
        if (grouperAttribute == null) {
          ProvisioningAttribute targetAttribute = targetProvisioningAttributes.get(attributeName);
          Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();
          
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
              attributeChangeType(targetValue, grouperValue), targetValue, grouperValue)
          );
    }
  }


  protected void compareTargetEntities(Collection<ProvisioningEntityWrapper> provisioningEntityWrappers) { 
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: GrouperUtil.nonNull(provisioningEntityWrappers)) {
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity != null) {
        GrouperUtil.setClear(grouperTargetEntity.getInternal_objectChanges());
      }
    }

    Map<Object, ProvisioningEntity> grouperMatchingIdToTargetEntity = new HashMap<Object, ProvisioningEntity>();
    Map<Object, ProvisioningEntity> targetMatchingIdToTargetEntity = new HashMap<Object, ProvisioningEntity>();
    
    List<ProvisioningEntity> grouperTargetEntitiesWithNullIds = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: GrouperUtil.nonNull(provisioningEntityWrappers)) {
      
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.isDelete() ? null : provisioningEntityWrapper.getGrouperTargetEntity();
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      
      Object grouperMatchingId = grouperTargetEntity == null ? null : grouperTargetEntity.getMatchingId();
      Object targetMatchingId = targetProvisioningEntity == null ? null : targetProvisioningEntity.getMatchingId();
      
      if (!GrouperUtil.isBlank(grouperMatchingId)) {
        grouperMatchingIdToTargetEntity.put(grouperMatchingId, grouperTargetEntity);
      } else if (grouperTargetEntity != null) {
        grouperTargetEntitiesWithNullIds.add(grouperTargetEntity);
      }

      if (!GrouperUtil.isBlank(targetMatchingId)) {
        targetMatchingIdToTargetEntity.put(targetMatchingId, targetProvisioningEntity);
      }
    }
        
    {
      // entities to insert
      Set<Object> entityIdsToInsert = new HashSet<Object>(grouperMatchingIdToTargetEntity.keySet());
      entityIdsToInsert.removeAll(targetMatchingIdToTargetEntity.keySet());
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesInsert(), false)) {
        List<ProvisioningEntity> provisioningEntitiesToInsert = new ArrayList<ProvisioningEntity>();
        
        for (Object entityIdToInsert: entityIdsToInsert) {
          ProvisioningEntity entityToInsert = grouperMatchingIdToTargetEntity.get(entityIdToInsert);
          provisioningEntitiesToInsert.add(entityToInsert);
        }
        
        addInternalObjectChangeForEntitiesToInsert(provisioningEntitiesToInsert);
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
      }    
    
      // entities to delete
      Set<Object> entityIdsToDelete = new HashSet<Object>(targetMatchingIdToTargetEntity.keySet());
      entityIdsToDelete.removeAll(grouperMatchingIdToTargetEntity.keySet());
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesDeleteIfNotInGrouper(), false)
          || GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesDeleteIfNotInGrouper(), false)) {

        List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();
        
        for (Object entityIdToDelete: entityIdsToDelete) {
          ProvisioningEntity entityToDelete = targetMatchingIdToTargetEntity.get(entityIdToDelete);
          
          boolean deleteEntity = false;
          
          if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesDeleteIfNotInGrouper(), false)) {
            deleteEntity = true;
          } else {
            
            GcGrouperSyncMember gcGrouperSyncMember = entityToDelete.getProvisioningEntityWrapper().getGcGrouperSyncMember();
  
            // if we are tracking this member, then it must have existed in grouper...
            if (gcGrouperSyncMember != null) {
              deleteEntity = true;
            }
          }
          if (deleteEntity) {
  
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
        }
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().setProvisioningEntities(provisioningEntitiesToDelete);
      }
      
      // entities to update
      Set<Object> entityIdsToUpdate = new HashSet<Object>(targetMatchingIdToTargetEntity.keySet());
      entityIdsToUpdate.addAll(grouperMatchingIdToTargetEntity.keySet());
      entityIdsToUpdate.removeAll(entityIdsToInsert);
      entityIdsToUpdate.removeAll(entityIdsToDelete);
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntitiesUpdate(), false)) {

        List<ProvisioningEntity> provisioningEntitiesToUpdate = new ArrayList<ProvisioningEntity>();
        
        for (Object entityIdToUpdate: entityIdsToUpdate) {
          ProvisioningEntity grouperTargetEntity = grouperMatchingIdToTargetEntity.get(entityIdToUpdate);
          ProvisioningEntity targetProvisioningEntity = targetMatchingIdToTargetEntity.get(entityIdToUpdate);
          
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
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);
      }
    }
    
    
  }


  public void addInternalObjectChangeForEntitiesToInsert(
      List<ProvisioningEntity> provisioningEntitiesToInsert) {
    
    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(provisioningEntitiesToInsert)) {
      GrouperUtil.setClear(provisioningEntity.getInternal_objectChanges());
    }

    for (ProvisioningEntity entityToInsert : provisioningEntitiesToInsert) {

      if (entityToInsert.getId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canEntityInsertField("id")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getId()));
      }
      if (entityToInsert.getLoginId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canEntityInsertField("loginId")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "loginId", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getLoginId()));
      }
      if (entityToInsert.getName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canEntityInsertField("name")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getName()));
      }
      if (entityToInsert.getEmail() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canEntityInsertField("email")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "email", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getEmail()));
      }
 
      compareAttributesForInsert(entityToInsert);
 
    }
  }


  protected void compareTargetGroups(Collection<ProvisioningGroupWrapper> provisioningGroupWrappers) {
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: GrouperUtil.nonNull(provisioningGroupWrappers)) {
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup != null) {
        GrouperUtil.setClear(grouperTargetGroup.getInternal_objectChanges());
      }
    }
    
    // groups insert
    Map<Object, ProvisioningGroup> grouperMatchingIdToTargetGroup = new HashMap<Object, ProvisioningGroup>();
    Map<Object, ProvisioningGroup> targetMatchingIdToTargetGroup = new HashMap<Object, ProvisioningGroup>();
    
    List<ProvisioningGroup> grouperTargetGroupsWithNullIds = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: GrouperUtil.nonNull(provisioningGroupWrappers)) {
          
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.isDelete() ? null : provisioningGroupWrapper.getGrouperTargetGroup();
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      
      Object grouperMatchingId = grouperTargetGroup == null ? null : grouperTargetGroup.getMatchingId();
      Object targetMatchingId = targetProvisioningGroup == null ? null : targetProvisioningGroup.getMatchingId();
      
      if (!GrouperUtil.isBlank(grouperMatchingId)) {
        grouperMatchingIdToTargetGroup.put(grouperMatchingId, grouperTargetGroup);
      } else if (grouperTargetGroup != null) {
        grouperTargetGroupsWithNullIds.add(grouperTargetGroup);
      }

      if (!GrouperUtil.isBlank(targetMatchingId)) {
        targetMatchingIdToTargetGroup.put(targetMatchingId, targetProvisioningGroup);
      }
    }
    {
      // groups to insert
      Set<Object> groupIdsToInsert = new HashSet<Object>(grouperMatchingIdToTargetGroup.keySet());
      groupIdsToInsert.removeAll(targetMatchingIdToTargetGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToInsert = new ArrayList<ProvisioningGroup>();
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsInsert(), false)) {
        for (Object groupIdToInsert: groupIdsToInsert) {
          ProvisioningGroup groupToInsert = grouperMatchingIdToTargetGroup.get(groupIdToInsert);
          provisioningGroupsToInsert.add(groupToInsert);
        }
        
        addInternalObjectChangeForGroupsToInsert(provisioningGroupsToInsert);
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges()
          .getTargetObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
      }    
    
      // groups to delete
      Set<Object> groupIdsToDelete = new HashSet<Object>(targetMatchingIdToTargetGroup.keySet());
      groupIdsToDelete.removeAll(grouperMatchingIdToTargetGroup.keySet());
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsDeleteIfNotInGrouper(), false)
          || GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsDeleteIfNotInGrouper(), false)) {

        List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
        
        for (Object groupIdToDelete: groupIdsToDelete) {
          ProvisioningGroup groupToDelete = targetMatchingIdToTargetGroup.get(groupIdToDelete);
          
          boolean deleteGroup = false;
          
          if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsDeleteIfNotInGrouper(), false)) {
            deleteGroup = true;
          } else {
            
            GcGrouperSyncGroup gcGrouperSyncGroup = groupToDelete.getProvisioningGroupWrapper().getGcGrouperSyncGroup();

            // if we are tracking this group, then it must have existed in grouper...
            if (gcGrouperSyncGroup != null) {
              deleteGroup = true;
            }
          }
          if (deleteGroup) {
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
          
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges()
        .getTargetObjectDeletes().setProvisioningGroups(provisioningGroupsToDelete);
      }
      
      // groups to update
      Set<Object> groupIdsToUpdate = new HashSet<Object>(targetMatchingIdToTargetGroup.keySet());
      groupIdsToUpdate.addAll(grouperMatchingIdToTargetGroup.keySet());
      groupIdsToUpdate.removeAll(groupIdsToInsert);
      groupIdsToUpdate.removeAll(groupIdsToDelete);
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupsUpdate(), false)) {

        List<ProvisioningGroup> provisioningGroupsToUpdate = new ArrayList<ProvisioningGroup>();
        
        for (Object groupIdToUpdate: groupIdsToUpdate) {
          ProvisioningGroup grouperTargetGroup = grouperMatchingIdToTargetGroup.get(groupIdToUpdate);
          ProvisioningGroup targetProvisioningGroup = targetMatchingIdToTargetGroup.get(groupIdToUpdate);
          
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
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);
      }  
    }
    
    
  }


  public void addInternalObjectChangeForGroupsToInsert(
      List<ProvisioningGroup> provisioningGroupsToInsert) {
    
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(provisioningGroupsToInsert)) {
      GrouperUtil.setClear(provisioningGroup.getInternal_objectChanges());
    }

    for (ProvisioningGroup groupToInsert: provisioningGroupsToInsert) {
      
      if (groupToInsert.getId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canGroupInsertField("id")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getId()));
      }
      if (groupToInsert.getIdIndex() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canGroupInsertField("idIndex")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "idIndex", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getIdIndex()));
      }
      if (groupToInsert.getDisplayName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canGroupInsertField("displayName")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "displayName", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getDisplayName()));
      }
      if (groupToInsert.getName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canGroupInsertField("name")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getName()));
      }
      
      compareAttributesForInsert(groupToInsert);
    }
  }


  protected void compareTargetMemberships(Collection<ProvisioningMembershipWrapper> provisioningMembershipWrappers) { 
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: GrouperUtil.nonNull(provisioningMembershipWrappers)) {
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      if (grouperTargetMembership != null) {
        GrouperUtil.setClear(grouperTargetMembership.getInternal_objectChanges());
      }
    }

    Map<Object, ProvisioningMembership> grouperMatchingIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    Map<Object, ProvisioningMembership> targetMatchingIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    
    List<ProvisioningMembership> grouperTargetMembershipsWithNullIds = new ArrayList<ProvisioningMembership>();
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: GrouperUtil.nonNull(provisioningMembershipWrappers)) {
      
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.isDelete() ? null : provisioningMembershipWrapper.getGrouperTargetMembership();
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
      
      Object grouperMatchingId = grouperTargetMembership == null ? null : grouperTargetMembership.getMatchingId();
      Object targetMatchingId = targetProvisioningMembership == null ? null : targetProvisioningMembership.getMatchingId();
      
      if (!GrouperUtil.isBlank(grouperMatchingId)) {
        grouperMatchingIdToTargetMembership.put(grouperMatchingId, grouperTargetMembership);
      } else if (grouperTargetMembership != null) {
        grouperTargetMembershipsWithNullIds.add(grouperTargetMembership);
      }

      if (!GrouperUtil.isBlank(targetMatchingId)) {
        targetMatchingIdToTargetMembership.put(targetMatchingId, targetProvisioningMembership);
      }
    }
    
    {
      // memberships to insert
      Set<Object> matchingIdsToInsert = new HashSet<Object>(grouperMatchingIdToTargetMembership.keySet());
      matchingIdsToInsert.removeAll(targetMatchingIdToTargetMembership.keySet());
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getMembershipsInsert(), false)) {

        List<ProvisioningMembership> provisioningMembershipsToInsert = new ArrayList<ProvisioningMembership>();
        
        for (Object groupIdEntityIdToInsert: matchingIdsToInsert) {
          ProvisioningMembership membershipToInsert = grouperMatchingIdToTargetMembership.get(groupIdEntityIdToInsert);
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
    
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningMemberships(provisioningMembershipsToInsert);
      }      
    
      // memberships to delete
      Set<Object> groupIdEntityIdsToDelete = new HashSet<Object>(targetMatchingIdToTargetMembership.keySet());
      groupIdEntityIdsToDelete.removeAll(grouperMatchingIdToTargetMembership.keySet());
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getMembershipsDeleteIfNotInGrouper(), false)
          || GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getMembershipsDeleteIfNotInGrouper(), false)) {

        List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();
        
        for (Object matchingIdsToDelete: groupIdEntityIdsToDelete) {
          ProvisioningMembership membershipToDelete = targetMatchingIdToTargetMembership.get(matchingIdsToDelete);
  
          boolean deleteMembership = false;
          
          if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getMembershipsDeleteIfNotInGrouper(), false)) {
            deleteMembership = true;
          } else {
            
            GcGrouperSyncMembership gcGrouperSyncMembership = membershipToDelete.getProvisioningMembershipWrapper().getGcGrouperSyncMembership();
  
            // if we are tracking this group, then it must have existed in grouper...
            if (gcGrouperSyncMembership != null) {
              deleteMembership = true;
            }
          }
          if (deleteMembership) {
  
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
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().setProvisioningMemberships(provisioningMembershipsToDelete);
      }
      
      // memberships to update
      Set<Object> matchingIdsToUpdate = new HashSet<Object>(targetMatchingIdToTargetMembership.keySet());
      matchingIdsToUpdate.addAll(grouperMatchingIdToTargetMembership.keySet());
      matchingIdsToUpdate.removeAll(matchingIdsToInsert);
      matchingIdsToUpdate.removeAll(groupIdEntityIdsToDelete);
      
      if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getMembershipsUpdate(), false)) {

        List<ProvisioningMembership> provisioningMembershipsToUpdate = new ArrayList<ProvisioningMembership>();
        
        for (Object matchingIdToUpdate: matchingIdsToUpdate) {
          ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(matchingIdToUpdate);
          ProvisioningMembership targetProvisioningMembership = targetMatchingIdToTargetMembership.get(matchingIdToUpdate);
          
          compareFieldValue(provisioningMembershipsToUpdate, "id",
              grouperTargetMembership.getId(), targetProvisioningMembership.getId(),
              grouperTargetMembership);
          
          compareAttributeValues(provisioningMembershipsToUpdate, grouperTargetMembership.getAttributes(),
              targetProvisioningMembership.getAttributes(), grouperTargetMembership);
          
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
      }
    }
    
    
  }


  protected void compareTargetObjects() {
    
    compareTargetGroups(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupMatchingIdToProvisioningGroupWrapper().values());
    compareTargetEntities(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getEntityMatchingIdToProvisioningEntityWrapper().values());
    compareTargetMemberships(grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMembershipMatchingIdToProvisioningMembershipWrapper().values());
  
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    {
      int groupInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningGroups());
      if (groupInserts > 0) {
        debugMap.put("groupInsertsAfterCompare", groupInserts);
      }
    }
    {
      int groupUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
      if (groupUpdates > 0) {
        debugMap.put("groupUpdatesAfterCompare", groupUpdates);
      }
    }
    {
      int groupDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningGroups());
      if (groupDeletes > 0) {
        debugMap.put("groupDeletesAfterCompare", groupDeletes);
      }
    }
    {
      int entityInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningEntities());
      if (entityInserts > 0) {
        debugMap.put("entityInsertsAfterCompare", entityInserts);
      }
    }
    {
      int entityUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
      if (entityUpdates > 0) {
        debugMap.put("entityUpdatesAfterCompare", entityUpdates);
      }
    }
    {
      int entityDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningEntities());
      if (entityDeletes > 0) {
        debugMap.put("entityDeletesAfterCompare", entityDeletes);
      }
    }
    {
      int membershipInserts = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().getProvisioningMemberships());
      if (membershipInserts > 0) {
        debugMap.put("membershipInsertsAfterCompare", membershipInserts);
      }
    }
    {
      int membershipUpdates = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningMemberships());
      if (membershipUpdates > 0) {
        debugMap.put("membershipUpdatesAfterCompare", membershipUpdates);
      }
    }
    {
      int membershipDeletes = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().getProvisioningMemberships());
      if (membershipDeletes > 0) {
        debugMap.put("membershipDeletesAfterCompare", membershipDeletes);
      }
    }
  
  }
  
  
}
