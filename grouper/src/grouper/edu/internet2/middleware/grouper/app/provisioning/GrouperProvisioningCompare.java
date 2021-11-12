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
    
    String attributeForMemberships = null;

    if (!recalc) {
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
          if (provisioningMembershipWrapper.isRecalc()) {
            continue;
          }
          if (provisioningMembershipWrapper.getGrouperIncrementalDataAction() != null) {
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
        }
        if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
          addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
              grouperProvisioningUpdatable);
        }
      } else {
        return;
      }
      
    }
    // if we're doing an update for an incremental and it's not a recalc, then skip this because we are only doing memberships above
    if (grouperProvisioningUpdatable instanceof ProvisioningGroup && ((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper().getTargetProvisioningGroup() == null ) {
      return;
    }
    
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity && ((ProvisioningEntity)grouperProvisioningUpdatable).getProvisioningEntityWrapper().getTargetProvisioningEntity() == null ) {
      return;
    }
    
    for (String attributeName: GrouperUtil.nonNull(grouperTargetAttributes).keySet()) {

//      if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateObjectAttribute(grouperProvisioningUpdatable, attributeName)) {
//        continue;
//      }

      if (!recalc && !StringUtils.equals(attributeForMemberships, attributeName)) {
        continue;
      }
      
      
      ProvisioningAttribute targetAttribute = GrouperUtil.nonNull(targetProvisioningAttributes).get(attributeName);
      ProvisioningAttribute grouperAttribute = grouperTargetAttributes.get(attributeName);
      
      Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
      Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();

      if (targetAttribute == null) {
        
        if (grouperProvisioningUpdatable.canInsertAttribute(attributeName)) {
          if (GrouperUtil.isArrayOrCollection(grouperValue)) {
            if (grouperValue instanceof Collection) {
              for (Object value : (Collection)grouperValue) {
                
                value = filterDeletedMemberships(grouperAttribute, value);
                
                if (filterNonRecalcMemberships(grouperAttribute, value, recalc)) {
                  continue;
                }
                
                grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            } else {
              // array
              for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
                Object value = Array.get(grouperValue, i);
                value = filterDeletedMemberships(grouperAttribute, value);
                
                if (filterNonRecalcMemberships(grouperAttribute, value, recalc)) {
                  continue;
                }
                
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
            grouperCollection = new HashSet<Object>((Collection)grouperValue);
          } else if (grouperValue.getClass().isArray()) {
            grouperCollection = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(grouperValue);i++) {
              grouperCollection.add(Array.get(grouperValue, i));
            }
          }
        }
        
        if (grouperCollection != null) {
          Iterator<Object> iterator = grouperCollection.iterator();
          while (iterator.hasNext()) {
            Object value = iterator.next();
            value = filterDeletedMemberships(grouperAttribute, value);
            
            if (filterNonRecalcMemberships(grouperAttribute, value, recalc)) {
              continue;
            }
            
            if (value == null) {
              iterator.remove();
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
    
    for (String attributeName: GrouperUtil.nonNull(targetProvisioningAttributes).keySet()) {
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
   * if this is a membership and the membership is deleted, return null
   * @param grouperAttribute
   * @param grouperValue
   * @return
   */
  private Object filterDeletedMemberships(ProvisioningAttribute grouperAttribute, Object grouperValue) {
    
    if (grouperAttribute == null || grouperAttribute.getValueToProvisioningMembershipWrapper() == null) {
      return grouperValue;
    }
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(grouperValue);
    
    if (provisioningMembershipWrapper != null && provisioningMembershipWrapper.isDelete()) {
      grouperValue = null;
    }

    return grouperValue;
    
  }
  
  /**
   * if this is a membership and the membership is deleted, return null
   * @param grouperAttribute
   * @param grouperValue
   * @return
   */
  private boolean filterNonRecalcMemberships(ProvisioningAttribute grouperAttribute, Object grouperValue, boolean groupOrEntityRecalc) {
    
    if (groupOrEntityRecalc) {
      return false;
    }
    
    if (grouperAttribute == null || grouperAttribute.getValueToProvisioningMembershipWrapper() == null) {
      return false;
    }
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(grouperValue);
    
    if (provisioningMembershipWrapper != null && !provisioningMembershipWrapper.isRecalc()) {
      return true;
    }

    return false;
    
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
      ProvisioningUpdatable grouperTargetUpdatable,
      ProvisioningUpdatable targetUpdatable) {
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateObjectField(grouperTargetUpdatable, fieldName)) {
      
      boolean isRecalc = false;
      boolean isUpdate = false;
      
      if (grouperTargetUpdatable instanceof ProvisioningGroup) {
        isRecalc = ((ProvisioningGroup)grouperTargetUpdatable).getProvisioningGroupWrapper().isRecalc();
        isUpdate = ((ProvisioningGroup)grouperTargetUpdatable).getProvisioningGroupWrapper().isUpdate();
      } else if (grouperTargetUpdatable instanceof ProvisioningEntity) {
        isRecalc = ((ProvisioningEntity)grouperTargetUpdatable).getProvisioningEntityWrapper().isRecalc();
      }
      
      if (isRecalc) {
        if (targetUpdatable != null) {
          if (!compareFieldValueEquals(fieldName, grouperValue, targetValue, grouperTargetUpdatable) ) {
            addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
                grouperTargetUpdatable);
            grouperTargetUpdatable.addInternal_objectChange(
                new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, fieldName, null, 
                    attributeChangeType(targetValue, grouperValue), targetValue, grouperValue)
                );
          }
        } else {
          this.grouperProvisioner.getDebugMap().put("nullTargetObjectToUpdate", true);
        }
      } else {
        
        if (isUpdate) {

          addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, grouperTargetUpdatable);
          grouperTargetUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.field, fieldName, null, 
                  ProvisioningObjectChangeAction.update, targetValue, grouperValue)
              );
          
        }
        
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
    
    
    List<ProvisioningEntityWrapper> provisioningEntityWrappersForInsert = new ArrayList<ProvisioningEntityWrapper>();
    List<ProvisioningEntityWrapper> provisioningEntityWrappersForUpdate = new ArrayList<ProvisioningEntityWrapper>();
    List<ProvisioningEntityWrapper> provisioningEntityWrappersForDelete = new ArrayList<ProvisioningEntityWrapper>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: GrouperUtil.nonNull(provisioningEntityWrappers)) {
      
      if (!provisioningEntityWrapper.isDelete()) {
        
        if (provisioningEntityWrapper.getGcGrouperSyncMember() != null && provisioningEntityWrapper.getGcGrouperSyncMember().isProvisionable() && !provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
        
          if (provisioningEntityWrapper.isRecalc()) {
          
            if (provisioningEntityWrapper.getGrouperTargetEntity() != null && provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
              
              provisioningEntityWrappersForInsert.add(provisioningEntityWrapper);
              continue;
            }
          } else {
            // isCreate is applicable only for non-recalc 
            if (provisioningEntityWrapper.isCreate()) {
              // non recalc inserts happen at previous points in the workflow (createMissingEntities)
              //provisioningEntityWrappersForInsert.add(provisioningEntityWrapper);
              continue;
            }
            
          }
        } 
      }
      
      // deletes
      if (provisioningEntityWrapper.getGcGrouperSyncMember() == null || !provisioningEntityWrapper.getGcGrouperSyncMember().isProvisionable()) {
        
        if (provisioningEntityWrapper.isRecalc()) {
          
          if ( (provisioningEntityWrapper.getGrouperTargetEntity() == null || provisioningEntityWrapper.isDelete()) && provisioningEntityWrapper.getTargetProvisioningEntity() != null) { 
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
              provisioningEntityWrappersForDelete.add(provisioningEntityWrapper);
              continue;
            }
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.entityAttributes) {
              continue;
            }
            
          }
          
        } else {
          // isDelete is applicable only for non-recalc 
          if (provisioningEntityWrapper.isDelete()) {
            provisioningEntityWrappersForDelete.add(provisioningEntityWrapper);
            continue;
            
            //TODO if it's not a recalc and the behavior is not a delete, we need to delete memberships in the target (the ones that we know about hint, sync objects) if it's entity attributes
            
          }
          
        }
        
      }
      
      // updates
      if (provisioningEntityWrapper.isRecalc()) {
        
        if (provisioningEntityWrapper.getTargetProvisioningEntity() != null) {
          
          provisioningEntityWrappersForUpdate.add(provisioningEntityWrapper);
          continue;
          
        }
        
        
      } else {
        provisioningEntityWrappersForUpdate.add(provisioningEntityWrapper);
      }
      
    }
    
    List<ProvisioningEntity> provisioningEntitiesToInsert = new ArrayList<ProvisioningEntity>();
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertEntities()) {
      for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappersForInsert) {
        provisioningEntitiesToInsert.add(provisioningEntityWrapper.getGrouperTargetEntity());
      }
      addInternalObjectChangeForEntitiesToInsert(provisioningEntitiesToInsert);
    }
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningEntities(provisioningEntitiesToInsert);
    
    List<ProvisioningEntity> provisioningEntitiesToDelete = new ArrayList<ProvisioningEntity>();
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
      for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappersForDelete) {
        
        ProvisioningEntity entityToDelete = null;
        
        if (!provisioningEntityWrapper.isRecalc() && provisioningEntityWrapper.isDelete() && provisioningEntityWrapper.getGrouperTargetEntity() != null 
            && provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
          entityToDelete = provisioningEntityWrapper.getGrouperTargetEntity();
        } else {
          entityToDelete = provisioningEntityWrapper.getTargetProvisioningEntity();
        }
        
        if (entityToDelete == null) {
          continue;
        }
        
        boolean shouldDelete = false;
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntitiesIfNotExistInGrouper()) {
          shouldDelete = true;
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntitiesIfGrouperDeleted()) {
          
          if (provisioningEntityWrapper.getGcGrouperSyncMember() != null && provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
            shouldDelete = true;
          }
          
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntitiesIfGrouperCreated()) {
          
          if (provisioningEntityWrapper.getGcGrouperSyncMember() != null && provisioningEntityWrapper.getGcGrouperSyncMember().isInTargetInsertOrExists() 
                && provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
            shouldDelete = true;
          }
          
        }
        
        if (!shouldDelete) {
          continue;
        }
        
        provisioningEntitiesToDelete.add(entityToDelete);
        
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
        
      }
    }
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().setProvisioningEntities(provisioningEntitiesToDelete);
   
    List<ProvisioningEntity> provisioningEntitiesToUpdate = new ArrayList<ProvisioningEntity>();
    
    boolean behaviorIsUpdateEntities = (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateEntities() || 
        (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes &&
        (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isInsertMemberships() ||this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteMemberships())));
    
    if (behaviorIsUpdateEntities) {
      
      for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappersForUpdate) {
        
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
        ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateEntities()) {
          if (grouperTargetEntity != null && targetProvisioningEntity != null && grouperTargetEntity.getProvisioningEntityWrapper().isRecalc()) {
            compareFieldValue(provisioningEntitiesToUpdate, "name",
                grouperTargetEntity.getName() , targetProvisioningEntity.getName(),
                grouperTargetEntity, targetProvisioningEntity);
            
            compareFieldValue(provisioningEntitiesToUpdate, "email",
                grouperTargetEntity.getEmail() , targetProvisioningEntity.getEmail(),
                grouperTargetEntity, targetProvisioningEntity);
            
            compareFieldValue(provisioningEntitiesToUpdate, "loginId",
                grouperTargetEntity.getLoginId() , targetProvisioningEntity.getLoginId(),
                grouperTargetEntity, targetProvisioningEntity);
          }          
        }        
        compareAttributeValues(provisioningEntitiesToUpdate, grouperTargetEntity == null ? null : grouperTargetEntity.getAttributes(),
            targetProvisioningEntity == null ? null : targetProvisioningEntity.getAttributes(), grouperTargetEntity);
        
      }
    }
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningEntities(provisioningEntitiesToUpdate);

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
    
    
    List<ProvisioningGroupWrapper> provisioningGroupWrappersForInsert = new ArrayList<ProvisioningGroupWrapper>();
    List<ProvisioningGroupWrapper> provisioningGroupWrappersForUpdate = new ArrayList<ProvisioningGroupWrapper>();
    List<ProvisioningGroupWrapper> provisioningGroupWrappersForDelete = new ArrayList<ProvisioningGroupWrapper>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: GrouperUtil.nonNull(provisioningGroupWrappers)) {
      
      if (!provisioningGroupWrapper.isDelete()) {
        
        if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable() && !provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
        
          if (provisioningGroupWrapper.isRecalc()) {
          
            if (provisioningGroupWrapper.getGrouperTargetGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
              
              provisioningGroupWrappersForInsert.add(provisioningGroupWrapper);
              continue;
            }
          } else {
            // isCreate is applicable only for non-recalc 
            if (provisioningGroupWrapper.isCreate()) {
              // non recalc inserts happen at previous points in the workflow (createMissingGroups)
              //provisioningGroupWrappersForInsert.add(provisioningGroupWrapper);
              continue;
            }
            
          }
        } 
      }
      
      // deletes
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()) {
        
        if (provisioningGroupWrapper.isRecalc()) {
          
          if ( (provisioningGroupWrapper.getGrouperTargetGroup() == null || provisioningGroupWrapper.isDelete()) && provisioningGroupWrapper.getTargetProvisioningGroup() != null) { 
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroups()) {
              provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
              continue;
            }
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
              continue;
            }
            
          }
          
        } else {
          // isDelete is applicable only for non-recalc 
          if (provisioningGroupWrapper.isDelete()) {
            provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
            continue;
            
            //TODO if it's not a recalc and the behavior is not a delete, we need to delete memberships in the target (the ones that we know about hint, sync objects) if it's group attributes
            
          }
          
        }
        
      }
      
      // updates
      if (provisioningGroupWrapper.isRecalc()) {
        
        if (provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
          
          provisioningGroupWrappersForUpdate.add(provisioningGroupWrapper);
          continue;
          
        }
        
        
      } else {
        provisioningGroupWrappersForUpdate.add(provisioningGroupWrapper);
      }
      
    }
    
    List<ProvisioningGroup> provisioningGroupsToInsert = new ArrayList<ProvisioningGroup>();
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertGroups()) {
      for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappersForInsert) {
        provisioningGroupsToInsert.add(provisioningGroupWrapper.getGrouperTargetGroup());
      }
      addInternalObjectChangeForGroupsToInsert(provisioningGroupsToInsert);
    }
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningGroups(provisioningGroupsToInsert);
    
    List<ProvisioningGroup> provisioningGroupsToDelete = new ArrayList<>();
    
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroups()) {
      for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappersForDelete) {
        
        ProvisioningGroup groupToDelete = null;
        
        if (!provisioningGroupWrapper.isRecalc() && provisioningGroupWrapper.isDelete() && provisioningGroupWrapper.getGrouperTargetGroup() != null 
            && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
          groupToDelete = provisioningGroupWrapper.getGrouperTargetGroup();
        } else {
          groupToDelete = provisioningGroupWrapper.getTargetProvisioningGroup();
        }
        
        if (groupToDelete == null) {
          continue;
        }
        
        boolean shouldDelete = false;
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroupsIfNotExistInGrouper()) {
          shouldDelete = true;
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroupsIfGrouperDeleted()) {
          
          if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
            shouldDelete = true;
          }
          
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroupsIfGrouperCreated()) {
          
          if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTargetInsertOrExists() 
                && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
            shouldDelete = true;
          }
          
        }
        
        
        if (!shouldDelete) {
          continue;
        }
        
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
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectDeletes().setProvisioningGroups(provisioningGroupsToDelete);
   
    List<ProvisioningGroup> provisioningGroupsToUpdate = new ArrayList<>();
    
    boolean behaviorIsUpdateGroups = (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateGroups() || 
        (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes &&
        (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isInsertMemberships() ||this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isDeleteMemberships())));
    
    if (behaviorIsUpdateGroups) {
      
      for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappersForUpdate) {
        
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
        ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateGroups()) {
          if (grouperTargetGroup != null) {
            compareFieldValue(provisioningGroupsToUpdate, "displayName",
                grouperTargetGroup.getDisplayName(), targetProvisioningGroup == null? null: targetProvisioningGroup.getDisplayName(),
                grouperTargetGroup, targetProvisioningGroup);
            compareFieldValue(provisioningGroupsToUpdate, "name",
                grouperTargetGroup.getName(), targetProvisioningGroup == null? null: targetProvisioningGroup.getName(),
                grouperTargetGroup, targetProvisioningGroup);
            compareFieldValue(provisioningGroupsToUpdate, "idIndex",
                grouperTargetGroup.getIdIndex(), targetProvisioningGroup == null? null: targetProvisioningGroup.getIdIndex(),
                grouperTargetGroup, targetProvisioningGroup);
          }          
        }        
        compareAttributeValues(provisioningGroupsToUpdate, grouperTargetGroup == null ? null : grouperTargetGroup.getAttributes(),
            targetProvisioningGroup == null ? null : targetProvisioningGroup.getAttributes(), grouperTargetGroup);
        
      }
    }
    this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningGroups(provisioningGroupsToUpdate);

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

    boolean handleRecalcs = true;
    
    // if the target cannot recalc a membership then replace the full group list here and skip the recalc below
    if (!this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMemberships() &&  this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isReplaceMemberships()) {
      
      handleRecalcs = false;
      
      Map<ProvisioningGroup, List<ProvisioningMembership>> provisioningMembershipsToReplace = new HashMap<ProvisioningGroup, List<ProvisioningMembership>>();
      
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper: GrouperUtil.nonNull(provisioningMembershipWrappers)) { 
        if (provisioningMembershipWrapper.isRecalc()) {
          
          ProvisioningGroup provisioningGroup = provisioningMembershipWrapper.getGrouperTargetMembership().getProvisioningGroup();
          
          List<ProvisioningMembership> provisioningMemberships = provisioningMembershipsToReplace.get(provisioningGroup);
          if (provisioningMemberships == null) {
            provisioningMemberships = new ArrayList<ProvisioningMembership>();
          }
          
          if (!provisioningMembershipWrapper.isDelete()) {
            provisioningMemberships.add(provisioningMembershipWrapper.getGrouperTargetMembership());
          }
          provisioningMembershipsToReplace.put(provisioningGroup, provisioningMemberships);
          
        }
      }
      
      this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces()
      .setProvisioningMemberships(provisioningMembershipsToReplace);
        
    }
    
    Map<Object, ProvisioningMembership> grouperMatchingIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    Map<Object, ProvisioningMembership> targetMatchingIdToTargetMembership = new HashMap<Object, ProvisioningMembership>();
    
    List<ProvisioningMembership> grouperTargetMembershipsWithNullIds = new ArrayList<ProvisioningMembership>();
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper: GrouperUtil.nonNull(provisioningMembershipWrappers)) {
      
      // if the target cannot recalc a membership then handle with the replace above and skip it here
      if (!handleRecalcs && provisioningMembershipWrapper.isRecalc()) {
        continue;
      }
      
      ProvisioningMembership grouperTargetMembership = (provisioningMembershipWrapper.isDelete() && provisioningMembershipWrapper.isRecalc()) ? null : provisioningMembershipWrapper.getGrouperTargetMembership();
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
        ProvisioningMembership grouperTargetMembership = targetMatchingIdToTargetMembership.get(key);
        if (grouperTargetMembership.getProvisioningMembershipWrapper().isRecalc()) {
          groupIdEntityIdsToDelete.add(key);
        }
      }
      groupIdEntityIdsToDelete.removeAll(grouperMatchingIdToTargetMembership.keySet());
      for (Object key : grouperMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(key);
        if (!grouperTargetMembership.getProvisioningMembershipWrapper().isRecalc()) {
          if (grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.delete || 
              grouperTargetMembership.getProvisioningMembershipWrapper().isDelete()) {
            groupIdEntityIdsToDelete.add(key);
          }
        }
      }

      {
        List<ProvisioningMembership> provisioningMembershipsToDelete = new ArrayList<ProvisioningMembership>();

        for (Object matchingIdToDelete: groupIdEntityIdsToDelete) {
          ProvisioningMembership membershipToDelete = targetMatchingIdToTargetMembership.get(matchingIdToDelete);
          
          if (membershipToDelete == null) {
            // target probably doesn't allow retrieving memberships so we need to send membership delete based on grouper side only
            membershipToDelete = grouperMatchingIdToTargetMembership.get(matchingIdToDelete);
          }
          
          
          ProvisioningMembershipWrapper provisioningMembershipWrapper = membershipToDelete.getProvisioningMembershipWrapper();
          
          GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
          if (grouperProvisioningBehavior.isDeleteMembership(gcGrouperSyncMembership)) {
            
//            boolean shouldDelete = false;
//            
//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembershipsIfNotExistInGrouper()) {
//              shouldDelete = true;
//            }
//            
//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembershipsIfGrouperDeleted()) {
//              
//              if (gcGrouperSyncMembership != null && gcGrouperSyncMembership.isInTarget()) {
//                shouldDelete = true;
//              }
//              
//            }
//            
//            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembershipsIfGrouperCreated()) {
//              
//              if (gcGrouperSyncMembership != null && gcGrouperSyncMembership.isInTargetInsertOrExists() && gcGrouperSyncMembership.isInTarget()) {
//                shouldDelete = true;
//              }
//              
//            }
//            
//            if (!shouldDelete) {
//              continue;
//            }

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
                grouperTargetMembership, targetProvisioningMembership);
          }

          compareAttributeValues(provisioningMembershipsToUpdate, grouperTargetMembership == null ? null : grouperTargetMembership.getAttributes(),
              targetProvisioningMembership == null ? null : targetProvisioningMembership.getAttributes(), grouperTargetMembership);
          
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
      }
    }
  }

  public void compareTargetObjects() {
    compareTargetGroups(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers());
    compareTargetEntities(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers());
    compareTargetMemberships(grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
    
    addGroupDefaultMembershipAttributeValueIfAllRemoved(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    addEntityDefaultMembershipAttributeValueIfAllRemoved(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningEntities());
    
    removeGroupDefaultMembershipAttributeValueIfAnyAdded(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().getProvisioningGroups());
    
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
    {
      int membershipReplaces = GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataChanges().getTargetObjectReplaces().getProvisioningMemberships());
      if (membershipReplaces > 0) {
        debugMap.put("membershipReplacesAfterCompare", membershipReplaces);
      }
    }
  
  }


  public void addGroupDefaultMembershipAttributeValueIfAllRemoved(List<ProvisioningGroup> grouperTargetGroupsForUpdate) {
    
    if (grouperTargetGroupsForUpdate == null || grouperTargetGroupsForUpdate.size() == 0) {
      return;
    }
   
    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    if (grouperProvisioningBehaviorMembershipType != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      return;
    }
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    GrouperProvisioningConfigurationAttribute attribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeForMemberships);
    
    if (attribute == null || StringUtils.isBlank(attribute.getDefaultValue())) {
      return;
    }
    
    for (ProvisioningGroup grouperTargetGroupForUpdate : grouperTargetGroupsForUpdate) {
      
      boolean hasMembershipInsert = false;
      int countMembershipDelete = 0;
      
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetGroupForUpdate.getInternal_objectChanges())) {
        
        if (!StringUtils.equals(attributeForMemberships, provisioningObjectChange.getAttributeName())) {
          continue;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          hasMembershipInsert = true;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
          countMembershipDelete++;
        }
        
      }
      
      if (countMembershipDelete == 0 || hasMembershipInsert == true) {
        continue;
      }
      
      int membershipCountForGroup = this.grouperProvisioner.retrieveGrouperDao().retrieveMembershipCountForGroup(grouperTargetGroupForUpdate.getProvisioningGroupWrapper());
      
      if (membershipCountForGroup > countMembershipDelete) {
        continue;
      }
      
      ProvisioningObjectChange defaultObjectChange = new ProvisioningObjectChange();
      defaultObjectChange.setAttributeName(attributeForMemberships);
      if (StringUtils.equals(attribute.getDefaultValue(), "<emptyString>")) {
        defaultObjectChange.setNewValue("");
      } else {
        defaultObjectChange.setNewValue(attribute.getDefaultValue());
      }
      defaultObjectChange.setProvisioningObjectChangeAction(ProvisioningObjectChangeAction.insert);
      grouperTargetGroupForUpdate.addInternal_objectChange(defaultObjectChange);
      
    }
    
    
  }
  
  public void removeGroupDefaultMembershipAttributeValueIfAnyAdded(List<ProvisioningGroup> grouperTargetGroupsForUpdate) {
    
    if (grouperTargetGroupsForUpdate == null || grouperTargetGroupsForUpdate.size() == 0) {
      return;
    }
   
    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    if (grouperProvisioningBehaviorMembershipType != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      return;
    }
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    GrouperProvisioningConfigurationAttribute attribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeForMemberships);
    
    if (attribute == null || StringUtils.isBlank(attribute.getDefaultValue())) {
      return;
    }
    
    for (ProvisioningGroup grouperTargetGroupForUpdate : grouperTargetGroupsForUpdate) {
      
      boolean hasMembershipDelete = false;
      int countMembershipInsert = 0;
      
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetGroupForUpdate.getInternal_objectChanges())) {
        
        if (!StringUtils.equals(attributeForMemberships, provisioningObjectChange.getAttributeName())) {
          continue;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
          hasMembershipDelete = true;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          countMembershipInsert++;
        }
        
      }
      
      if (countMembershipInsert == 0 || hasMembershipDelete == true) {
        continue;
      }
      
      int membershipCountForGroup = this.grouperProvisioner.retrieveGrouperDao().retrieveMembershipCountForGroup(grouperTargetGroupForUpdate.getProvisioningGroupWrapper());
      
      if (membershipCountForGroup > 0) {
        continue;
      }
      
      ProvisioningObjectChange defaultObjectChange = new ProvisioningObjectChange();
      defaultObjectChange.setAttributeName(attributeForMemberships);
      if (StringUtils.equals(attribute.getDefaultValue(), "<emptyString>")) {
        defaultObjectChange.setOldValue("");
      } else {
        defaultObjectChange.setOldValue(attribute.getDefaultValue());
      }
      defaultObjectChange.setProvisioningObjectChangeAction(ProvisioningObjectChangeAction.delete);
      grouperTargetGroupForUpdate.addInternal_objectChange(defaultObjectChange);
      
    }
    
    
  }
  
  
  private void addEntityDefaultMembershipAttributeValueIfAllRemoved(List<ProvisioningEntity> grouperTargetEntitiesForUpdate) {
    
    if (grouperTargetEntitiesForUpdate == null || grouperTargetEntitiesForUpdate.size() == 0) {
      return;
    }
    
    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    
    if (grouperProvisioningBehaviorMembershipType != GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      return;
    }
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    GrouperProvisioningConfigurationAttribute attribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(attributeForMemberships);
    
    if (attribute == null || StringUtils.isBlank(attribute.getDefaultValue())) {
      return;
    }
    
    for (ProvisioningEntity grouperTargetEntityForUpdate : grouperTargetEntitiesForUpdate) {
      
      boolean hasMembershipInsert = false;
      int countMembershipDelete = 0;
      
      for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil.nonNull(grouperTargetEntityForUpdate.getInternal_objectChanges())) {
        
        if (!StringUtils.equals(attributeForMemberships, provisioningObjectChange.getAttributeName())) {
          continue;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
          hasMembershipInsert = true;
        }
        
        if (provisioningObjectChange.getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
          countMembershipDelete++;
        }
        
      }
      
      if (countMembershipDelete == 0 || hasMembershipInsert == true) {
        continue;
      }
      
      int membershipCountForGroup = this.grouperProvisioner.retrieveGrouperDao().retrieveMembershipCountForEntity(grouperTargetEntityForUpdate.getProvisioningEntityWrapper());
      
      if (membershipCountForGroup > countMembershipDelete) {
        continue;
      }
      
      ProvisioningObjectChange defaultObjectChange = new ProvisioningObjectChange();
      defaultObjectChange.setAttributeName(attributeForMemberships);
      if (StringUtils.equals(attribute.getDefaultValue(), "<emptyString>")) {
        defaultObjectChange.setNewValue("");
      } else {
        defaultObjectChange.setNewValue(attribute.getDefaultValue());
      }
      defaultObjectChange.setProvisioningObjectChangeAction(ProvisioningObjectChangeAction.insert);
      grouperTargetEntityForUpdate.addInternal_objectChange(defaultObjectChange);
      
    }
    
    
  }
  
  
}
