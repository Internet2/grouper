package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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


  public void addProvisioningUpdatableToUpdateIfNotThere(List provisioningUpdatablesToUpdate, 
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    // if there is nothing in the update list or this object is not last in there, then add it
    if (GrouperUtil.length(provisioningUpdatablesToUpdate) == 0 || 
        provisioningUpdatablesToUpdate.get(provisioningUpdatablesToUpdate.size()-1) != grouperProvisioningUpdatable) {
      provisioningUpdatablesToUpdate.add(grouperProvisioningUpdatable);
    }
    
  }


  public ProvisioningObjectChangeAction attributeChangeType(Object old, Object theNew) {
    if (old == null) return ProvisioningObjectChangeAction.insert;
    if (theNew == null) return ProvisioningObjectChangeAction.delete;
    return ProvisioningObjectChangeAction.update;
  }


  public boolean attributeValueEquals(Object first, Object second) {
    
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


  public void compareAttributeValues(
      List provisioningUpdatablesToUpdate,
      Map<String, ProvisioningAttribute> grouperTargetAttributes,
      Map<String, ProvisioningAttribute> targetProvisioningAttributes,
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    
    if (grouperProvisioningUpdatable == null) {
      return;
    }
    
    boolean recalc = grouperProvisioningUpdatable.isRecalc();
    
    if (!recalc) {
      String attributeForMemberships = null;
      boolean provisionOneAttribute = false;
      switch (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        case membershipObjects:
          // provision memberships
          if (grouperProvisioningUpdatable instanceof ProvisioningMembership) {
            break;
          }
          // otherwise ignore
          return;
        case entityAttributes:
          
          attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
          provisionOneAttribute = true;
          if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
            break;
          }
          // otherwise ignore
          return;
          
        case groupAttributes:

          attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
          provisionOneAttribute = true;
          if (grouperProvisioningUpdatable instanceof ProvisioningGroup) {
            break;
          }
          // otherwise ignore
          return;
          
        default:
          throw new RuntimeException("Not expecting membership type");
      }
      if (provisionOneAttribute) {
        
        if (StringUtils.isBlank(attributeForMemberships)) {
          throw new RuntimeException("Attribute for memberships is blank!");
        }
        
        ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeForMemberships);
        
        if (grouperAttribute == null) {
          return;
        }
        for (Object value : GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).keySet()) {
          ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(value);
          
          switch (provisioningMembershipWrapper.getGrouperIncrementalDataAction()) {
            case delete:
              
              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembership(provisioningMembershipWrapper.getGcGrouperSyncMembership())) {
                grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeForMemberships, 
                      ProvisioningObjectChangeAction.delete, value, null)
                );
              }
              break;
            case insert:
              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertMemberships()) {
                grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeForMemberships, 
                      ProvisioningObjectChangeAction.insert, null, value)
                );
              }
              break;
            default:
              throw new RuntimeException("Not expecting grouperIncrementalDataAction for " + grouperProvisioningUpdatable);
          }
          
        }
        if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
          addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
              grouperProvisioningUpdatable);
        }
        return;
      }
      
    }
    
    for (String attributeName: grouperTargetAttributes.keySet()) {

//      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateObjectAttribute(grouperProvisioningUpdatable, attributeName)) {
//        continue;
//      }

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
        if (grouperProvisioningUpdatable.canDeleteAttribute(attributeName)) {
          for (Object deleteValue : deletes) {
            
            if (grouperProvisioningUpdatable.canDeleteAttributeValue(attributeName, deleteValue)) {
            
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, deleteValue, null)
                  );
            }  
          }
        }
      }        
    }
    
    for (String attributeName: targetProvisioningAttributes.keySet()) {
      if (grouperProvisioningUpdatable.canDeleteAttribute(attributeName)) {
  
        ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeName);
        if (grouperAttribute == null) {
          ProvisioningAttribute targetAttribute = targetProvisioningAttributes.get(attributeName);
          Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();
          
          if (GrouperUtil.isArrayOrCollection(targetValue)) {
            if (targetValue instanceof Collection) {
              for (Object value : (Collection)targetValue) {
                if (grouperProvisioningUpdatable.canDeleteAttributeValue(attributeName, value)) {
                  grouperProvisioningUpdatable.addInternal_objectChange(
                      new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                          ProvisioningObjectChangeAction.delete, value, null)
                      );
                }
              }
            } else {
              // array
              for (int i=0;i<GrouperUtil.length(targetValue);i++) {
                Object value = Array.get(targetValue, i);
                if (grouperProvisioningUpdatable.canDeleteAttributeValue(attributeName, value)) {
                  grouperProvisioningUpdatable.addInternal_objectChange(
                      new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                          ProvisioningObjectChangeAction.delete, value, null)
                      );
                }
              }
            }
            
            // note for ldap I think we want this as false
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteBlankAttributesFromTarget()) {
              // indicate the attribute itself is gone
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                  ProvisioningObjectChangeAction.delete, null, null)
              );
            }            
          } else {
            // just a scalar
            if (grouperProvisioningUpdatable.canDeleteAttributeValue(attributeName, targetValue)) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                      ProvisioningObjectChangeAction.delete, targetValue, null)
                  );
            }
            
          }
        }
      }      
    }
    if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
      addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
          grouperProvisioningUpdatable);
    }
  }

  /**
   * compare methods to see if two fields have the same value or need an update
   * @param fieldName
   * @param grouperValue
   * @param targetValue
   * @param grouperTargetUpdatable
   * @return true if equals
   */
  public boolean compareFieldValueEquals(String fieldName,
      Object grouperValue, Object targetValue,
      ProvisioningUpdatable grouperTargetUpdatable) {
    return GrouperUtil.equals(grouperValue, targetValue);
  }

  public void compareFieldValue(List provisioningUpdatablesToUpdate,
      String fieldName,
      Object grouperValue, Object targetValue,
      ProvisioningUpdatable grouperTargetUpdatable) {
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateObjectField(grouperTargetUpdatable, fieldName)) {
      if (!compareFieldValueEquals(fieldName, grouperValue, targetValue, grouperTargetUpdatable) ) {
        addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
            grouperTargetUpdatable);
        grouperTargetUpdatable.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, fieldName, null, 
                attributeChangeType(targetValue, grouperValue), targetValue, grouperValue)
            );
      }
    }
  }

  public void compareTargetEntities(Collection<ProvisioningEntityWrapper> provisioningEntityWrappers) { 

    if (GrouperUtil.length(provisioningEntityWrappers) == 0) {
      return;
    }

    for (ProvisioningEntityWrapper provisioningEntityWrapper: GrouperUtil.nonNull(provisioningEntityWrappers)) {
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity != null) {
        GrouperUtil.setClear(grouperTargetEntity.getInternal_objectChanges());
      }
    }

    {
      provisioningEntityWrappers = new ArrayList<ProvisioningEntityWrapper>(provisioningEntityWrappers);
      Iterator<ProvisioningEntityWrapper> iterator = provisioningEntityWrappers.iterator();
      while (iterator.hasNext()) {
        ProvisioningEntityWrapper provisioningEntityWrapper = iterator.next();
        if (provisioningEntityWrapper.getErrorCode() != null) {
          iterator.remove();
        }
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
      Set<Object> entityIdsToInsert = new HashSet<Object>();
      for (Object key : grouperMatchingIdToTargetEntity.keySet()) {
        ProvisioningEntity grouperTargetEntity = grouperMatchingIdToTargetEntity.get(key);
        if (grouperTargetEntity.getProvisioningEntityWrapper().isRecalc()) {
          entityIdsToInsert.add(key);
        }
      }
      entityIdsToInsert.removeAll(targetMatchingIdToTargetEntity.keySet());

      GrouperProvisioningBehavior grouperProvisioningBehavior = this.grouperProvisioner.retrieveGrouperProvisioningBehavior();
      if (grouperProvisioningBehavior.isInsertEntities()) {
        List<ProvisioningEntity> provisioningEntitiesToInsert = new ArrayList<ProvisioningEntity>();
        
        for (Object entityIdToInsert: entityIdsToInsert) {
          ProvisioningEntity entityToInsert = grouperMatchingIdToTargetEntity.get(entityIdToInsert);
          provisioningEntitiesToInsert.add(entityToInsert);
        }
        
        addInternalObjectChangeForEntitiesToInsert(provisioningEntitiesToInsert);
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
      }    

      // entities to delete
      Set<Object> entityIdsToDelete = new HashSet<Object>();
      for (Object key : targetMatchingIdToTargetEntity.keySet()) {
        ProvisioningEntity targetEntity = targetMatchingIdToTargetEntity.get(key);
        if (targetEntity.getProvisioningEntityWrapper().isRecalc()) {
          entityIdsToDelete.add(key);
        }
      }
      entityIdsToDelete.removeAll(grouperMatchingIdToTargetEntity.keySet());

      {
        List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();

        for (Object entityIdToDelete: entityIdsToDelete) {
          ProvisioningEntity entityToDelete = targetMatchingIdToTargetEntity.get(entityIdToDelete);
          GcGrouperSyncMember gcGrouperSyncMember = entityToDelete.getProvisioningEntityWrapper().getGcGrouperSyncMember();
          if (grouperProvisioningBehavior.isDeleteEntity(gcGrouperSyncMember)) {
          
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
      

      List<ProvisioningEntity> provisioningEntitiesToUpdate = new ArrayList<ProvisioningEntity>();
      
      for (Object entityIdToUpdate: entityIdsToUpdate) {
        ProvisioningEntity grouperTargetEntity = grouperMatchingIdToTargetEntity.get(entityIdToUpdate);
        ProvisioningEntity targetProvisioningEntity = targetMatchingIdToTargetEntity.get(entityIdToUpdate);
        
        if (grouperProvisioningBehavior.isUpdateEntities()) {
          if (grouperTargetEntity != null && targetProvisioningEntity != null && grouperTargetEntity.getProvisioningEntityWrapper().isRecalc()) {
            compareFieldValue(provisioningEntitiesToUpdate, "name",
                grouperTargetEntity.getName() , targetProvisioningEntity.getName(),
                grouperTargetEntity);
            
            compareFieldValue(provisioningEntitiesToUpdate, "email",
                grouperTargetEntity.getEmail() , targetProvisioningEntity.getEmail(),
                grouperTargetEntity);
            
            compareFieldValue(provisioningEntitiesToUpdate, "loginId",
                grouperTargetEntity.getLoginId() , targetProvisioningEntity.getLoginId(),
                grouperTargetEntity);
          }          
        }        
        compareAttributeValues(provisioningEntitiesToUpdate, grouperTargetEntity == null ? null : grouperTargetEntity.getAttributes(),
            targetProvisioningEntity == null ? null : targetProvisioningEntity.getAttributes(), grouperTargetEntity);
        
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);
    }
    
    
  }


  public void addInternalObjectChangeForEntitiesToInsert(
      List<ProvisioningEntity> provisioningEntitiesToInsert) {
    
    if (provisioningEntitiesToInsert == null) {
      return;
    }

    for (ProvisioningEntity provisioningEntity: GrouperUtil.nonNull(provisioningEntitiesToInsert)) {
      GrouperUtil.setClear(provisioningEntity.getInternal_objectChanges());
    }

    for (ProvisioningEntity entityToInsert : provisioningEntitiesToInsert) {

      if (entityToInsert.getId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertEntityField("id")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getId()));
      }
      if (entityToInsert.getLoginId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertEntityField("loginId")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "loginId", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getLoginId()));
      }
      if (entityToInsert.getName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertEntityField("name")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getName()));
      }
      if (entityToInsert.getEmail() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertEntityField("email")) {
        entityToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "email", null, 
                ProvisioningObjectChangeAction.insert, null, entityToInsert.getEmail()));
      }
 
      compareAttributesForInsert(entityToInsert);
 
    }
  }


  public void compareTargetGroups(Collection<ProvisioningGroupWrapper> provisioningGroupWrappers) {
    
    if (GrouperUtil.length(provisioningGroupWrappers) == 0) {
      return;
    }
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: GrouperUtil.nonNull(provisioningGroupWrappers)) {
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup != null) {
        GrouperUtil.setClear(grouperTargetGroup.getInternal_objectChanges());
      }
    }

    {
      provisioningGroupWrappers = new ArrayList<ProvisioningGroupWrapper>(provisioningGroupWrappers);
      Iterator<ProvisioningGroupWrapper> iterator = provisioningGroupWrappers.iterator();
      while (iterator.hasNext()) {
        ProvisioningGroupWrapper provisioningGroupWrapper = iterator.next();
        if (provisioningGroupWrapper.getErrorCode() != null) {
          iterator.remove();
        }
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
      Set<Object> groupIdsToInsert = new HashSet<Object>();
      for (Object key : grouperMatchingIdToTargetGroup.keySet()) {
        ProvisioningGroup grouperTargetGroup = grouperMatchingIdToTargetGroup.get(key);
        if (grouperTargetGroup.getProvisioningGroupWrapper().isRecalc()) {
          groupIdsToInsert.add(key);
        }
      }
      groupIdsToInsert.removeAll(targetMatchingIdToTargetGroup.keySet());
      
      List<ProvisioningGroup> provisioningGroupsToInsert = new ArrayList<ProvisioningGroup>();
      
      GrouperProvisioningBehavior grouperProvisioningBehavior = this.grouperProvisioner.retrieveGrouperProvisioningBehavior();
      if (GrouperUtil.booleanValue(grouperProvisioningBehavior.isInsertGroups(), false)) {
        for (Object groupIdToInsert: groupIdsToInsert) {
          ProvisioningGroup groupToInsert = grouperMatchingIdToTargetGroup.get(groupIdToInsert);
          provisioningGroupsToInsert.add(groupToInsert);
        }
        
        addInternalObjectChangeForGroupsToInsert(provisioningGroupsToInsert);
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges()
          .getTargetObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
      }    
    
      // groups to delete
      Set<Object> groupIdsToDelete = new HashSet<Object>();
      for (Object key : targetMatchingIdToTargetGroup.keySet()) {
        ProvisioningGroup targetGroup = targetMatchingIdToTargetGroup.get(key);
        if (targetGroup.getProvisioningGroupWrapper().isRecalc()) {
          groupIdsToDelete.add(key);
        }
      }
      groupIdsToDelete.removeAll(grouperMatchingIdToTargetGroup.keySet());
      
      Iterator<Object> matchingIdsToDeleteIterator = groupIdsToDelete.iterator();
      while (matchingIdsToDeleteIterator.hasNext()) {
        
        Object groupIdToDelete = matchingIdsToDeleteIterator.next();
        ProvisioningGroup groupToDelete = targetMatchingIdToTargetGroup.get(groupIdToDelete);
        GcGrouperSyncGroup gcGrouperSyncGroup = groupToDelete.getProvisioningGroupWrapper().getGcGrouperSyncGroup();

        if (!grouperProvisioningBehavior.isDeleteGroup(gcGrouperSyncGroup)) {
          matchingIdsToDeleteIterator.remove();
        }

      }

      {
        List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<ProvisioningGroup>();
        
        for (Object groupIdToDelete: groupIdsToDelete) {
          
          ProvisioningGroup groupToDelete = targetMatchingIdToTargetGroup.get(groupIdToDelete);
          
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
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges()
          .getTargetObjectDeletes().setProvisioningGroups(provisioningGroupsToDelete);
      }
      
      // groups to update
      Set<Object> groupIdsToUpdate = new HashSet<Object>(targetMatchingIdToTargetGroup.keySet());
      groupIdsToUpdate.addAll(grouperMatchingIdToTargetGroup.keySet());
      groupIdsToUpdate.removeAll(groupIdsToInsert);
      groupIdsToUpdate.removeAll(groupIdsToDelete);
      
      List<ProvisioningGroup> provisioningGroupsToUpdate = new ArrayList<ProvisioningGroup>();
      
      for (Object groupIdToUpdate: groupIdsToUpdate) {
        ProvisioningGroup grouperTargetGroup = grouperMatchingIdToTargetGroup.get(groupIdToUpdate);
        ProvisioningGroup targetProvisioningGroup = targetMatchingIdToTargetGroup.get(groupIdToUpdate);
                
        if (grouperProvisioningBehavior.isUpdateGroups()) {
          if (grouperTargetGroup != null && targetProvisioningGroup != null && grouperTargetGroup.getProvisioningGroupWrapper().isRecalc()) {
            compareFieldValue(provisioningGroupsToUpdate, "displayName",
                grouperTargetGroup.getDisplayName(), targetProvisioningGroup.getDisplayName(),
                grouperTargetGroup);
            
            compareFieldValue(provisioningGroupsToUpdate, "name",
                grouperTargetGroup.getName(), targetProvisioningGroup.getName(),
                grouperTargetGroup);
            
            compareFieldValue(provisioningGroupsToUpdate, "idIndex",
                grouperTargetGroup.getIdIndex(), targetProvisioningGroup.getIdIndex(),
                grouperTargetGroup);
          }
        }          
        compareAttributeValues(provisioningGroupsToUpdate, grouperTargetGroup == null ? null : grouperTargetGroup.getAttributes(),
            targetProvisioningGroup == null ? null : targetProvisioningGroup.getAttributes(), 
                grouperTargetGroup);
          
      }  
      this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);
    }
    
    
  }


  public void addInternalObjectChangeForGroupsToInsert(
      List<ProvisioningGroup> provisioningGroupsToInsert) {
    
    if (provisioningGroupsToInsert == null) {
      return;
    }
    for (ProvisioningGroup provisioningGroup: GrouperUtil.nonNull(provisioningGroupsToInsert)) {
      GrouperUtil.setClear(provisioningGroup.getInternal_objectChanges());
    }

    for (ProvisioningGroup groupToInsert: provisioningGroupsToInsert) {
      
      if (groupToInsert.getId() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertGroupField("id")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "id", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getId()));
      }
      if (groupToInsert.getIdIndex() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertGroupField("idIndex")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "idIndex", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getIdIndex()));
      }
      if (groupToInsert.getDisplayName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertGroupField("displayName")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "displayName", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getDisplayName()));
      }
      if (groupToInsert.getName() != null && this.grouperProvisioner.retrieveGrouperProvisioningBehavior().canInsertGroupField("name")) {
        groupToInsert.addInternal_objectChange(
            new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, "name", null, 
                ProvisioningObjectChangeAction.insert, null, groupToInsert.getName()));
      }
      
      compareAttributesForInsert(groupToInsert);
    }
  }


  public void compareTargetMemberships(Collection<ProvisioningMembershipWrapper> provisioningMembershipWrappers) { 
    
    if (GrouperUtil.length(provisioningMembershipWrappers) == 0) {
      return;
    }

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: GrouperUtil.nonNull(provisioningMembershipWrappers)) {
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      if (grouperTargetMembership != null) {
        GrouperUtil.setClear(grouperTargetMembership.getInternal_objectChanges());
      }
    }

    {
      provisioningMembershipWrappers = new ArrayList<ProvisioningMembershipWrapper>(provisioningMembershipWrappers);
      Iterator<ProvisioningMembershipWrapper> iterator = provisioningMembershipWrappers.iterator();
      while (iterator.hasNext()) {
        ProvisioningMembershipWrapper provisioningMembershipWrapper = iterator.next();
        if (provisioningMembershipWrapper.getErrorCode() != null) {
          iterator.remove();
        }
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
    
    GrouperProvisioningBehavior grouperProvisioningBehavior = this.grouperProvisioner.retrieveGrouperProvisioningBehavior();
    {
      // memberships to insert
      Set<Object> matchingIdsToInsert = new HashSet<Object>();
      for (Object key : grouperMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(key);
        if (grouperTargetMembership.getProvisioningMembershipWrapper().isRecalc()) {
          matchingIdsToInsert.add(key);
        } else if (grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.insert) {
          matchingIdsToInsert.add(key);
        }
        
      }

      matchingIdsToInsert.removeAll(targetMatchingIdToTargetMembership.keySet());
      
      if (grouperProvisioningBehavior.isInsertMemberships()) {

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
      Set<Object> groupIdEntityIdsToDelete = new HashSet<Object>();
      for (Object key : targetMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(key);
        if (grouperTargetMembership.getProvisioningMembershipWrapper().isRecalc()) {
          groupIdEntityIdsToDelete.add(key);
        }
      }
      groupIdEntityIdsToDelete.removeAll(grouperMatchingIdToTargetMembership.keySet());
      for (Object key : grouperMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(key);
        if (!grouperTargetMembership.getProvisioningMembershipWrapper().isRecalc()) {
          if (grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.delete) {
            groupIdEntityIdsToDelete.add(key);
          }
        }
      }

      {
        List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();

        for (Object matchingIdsToDelete: groupIdEntityIdsToDelete) {
          ProvisioningMembership membershipToDelete = targetMatchingIdToTargetMembership.get(matchingIdsToDelete);
          GcGrouperSyncMembership gcGrouperSyncMembership = membershipToDelete.getProvisioningMembershipWrapper().getGcGrouperSyncMembership();
          if (grouperProvisioningBehavior.isDeleteMembership(gcGrouperSyncMembership)) {

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
      
      if (grouperProvisioningBehavior.isUpdateMemberships()) {

        List<ProvisioningMembership> provisioningMembershipsToUpdate = new ArrayList<ProvisioningMembership>();
        
        for (Object matchingIdToUpdate: matchingIdsToUpdate) {
          ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(matchingIdToUpdate);
          ProvisioningMembership targetProvisioningMembership = targetMatchingIdToTargetMembership.get(matchingIdToUpdate);
          
          //if one is null its not an update
          if (grouperTargetMembership != null && targetProvisioningMembership != null) {
            compareFieldValue(provisioningMembershipsToUpdate, "id",
                grouperTargetMembership.getId(), targetProvisioningMembership.getId(),
                grouperTargetMembership);
          }

          compareAttributeValues(provisioningMembershipsToUpdate, grouperTargetMembership == null ? null : grouperTargetMembership.getAttributes(),
              targetProvisioningMembership == null ? null : targetProvisioningMembership.getAttributes(), grouperTargetMembership);
          
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
      }
    }
  }

  public void compareTargetObjects() {
    
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
