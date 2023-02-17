package edu.internet2.middleware.grouper.app.provisioning;

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
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningCompare {

  private GrouperProvisioner grouperProvisioner = null;

  private int membershipAddCount = 0;
  
  private Map<String, Integer> groupUuidToMembershipDeleteCount = new HashMap<String, Integer>();
  
  public Map<String, Integer> getGroupUuidToMembershipDeleteCount() {
    return groupUuidToMembershipDeleteCount;
  }

  private Map<String, Integer> groupUuidToMembershipAddCount = new HashMap<String, Integer>();
  
  public Map<String, Integer> getGroupUuidToMembershipAddCount() {
    return groupUuidToMembershipAddCount;
  }


  public int getMembershipAddCount() {
    return membershipAddCount;
  }

  /**
   * group uuids to delete
   */
  private Set<String> groupUuidsToDelete = new HashSet<String>();


  /**
   * group uuids to delete
   * @return
   */
  public Set<String> getGroupUuidsToDelete() {
    return groupUuidsToDelete;
  }

  private int membershipDeleteCount = 0;


  public int getMembershipDeleteCount() {
    return membershipDeleteCount;
  }

  
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


  public boolean attributeValueEquals(String attributeName, Object first, Object second, ProvisioningUpdatable grouperTargetUpdatable) {
    
    // update
    Collection<Object> firstCollection = null;
    if (first != null) {
      if (first instanceof Collection) {
        firstCollection = (Collection)first;
      }
    }
    Collection<Object> secondCollection = null;
    if (second != null) {
      if (second instanceof Collection) {
        secondCollection = (Collection)second;
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
    
    boolean membershipAttribute = false;
    
    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    String attributeNameForMemberships = null;
    
    if (grouperProvisioningBehaviorMembershipType != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      
      if (grouperProvisioningBehaviorMembershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes
          && provisioningUpdatableToDelete instanceof ProvisioningGroup) {
        membershipAttribute = true;
      }
      if (grouperProvisioningBehaviorMembershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes
          && provisioningUpdatableToDelete instanceof ProvisioningEntity) {
        membershipAttribute = true;
      }
      if (membershipAttribute) {
        attributeNameForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
      }
    }

    for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(provisioningUpdatableToDelete.getAttributes()).keySet())) {

      ProvisioningAttribute provisioningAttribute = provisioningUpdatableToDelete.getAttributes().get(attributeName);
      Object grouperValue = provisioningAttribute.getValue();
  
      if (GrouperUtil.isArrayOrCollection(grouperValue)) {
        if (grouperValue instanceof Collection) {
          for (Object value : (Collection)grouperValue) {
            if (membershipAttribute && StringUtils.equals(attributeNameForMemberships, attributeName)) {
              this.membershipDeleteCount++;
              countDeleteMembershipObjectCount(provisioningAttribute, value);

            }
            provisioningUpdatableToDelete.addInternal_objectChange(
                new ProvisioningObjectChange(attributeName, 
                    ProvisioningObjectChangeAction.delete, value, null)
                );
          }
        } else {
          throw new RuntimeException("Arrays not supported");
        }
      } else {
        // just a scalar
        ProvisioningObjectChange provisioningObjectChange = new ProvisioningObjectChange(attributeName, 
            ProvisioningObjectChangeAction.delete, grouperValue, null);
        provisioningUpdatableToDelete.addInternal_objectChange(provisioningObjectChange);
        
      }
    }
  }


  private void countDeleteMembershipObjectCount(ProvisioningAttribute provisioningAttribute, Object value) {
    
    if (provisioningAttribute == null) {
      return;
    }
      
    Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = 
        provisioningAttribute.getValueToProvisioningMembershipWrapper();

    if (valueToProvisioningMembershipWrapper != null) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(value);
      if (provisioningMembershipWrapper == null) {
        return;
      }
      countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
    }
  }


  public void compareAttributesForInsert(ProvisioningUpdatable provisioningUpdatableToInsert) {
    
    boolean membershipAttribute = false;
    
    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = 
        this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType();
    String attributeNameForMemberships = null;
    
    if (grouperProvisioningBehaviorMembershipType != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      
      if (grouperProvisioningBehaviorMembershipType == GrouperProvisioningBehaviorMembershipType.groupAttributes
          && provisioningUpdatableToInsert instanceof ProvisioningGroup) {
        membershipAttribute = true;
      }
      if (grouperProvisioningBehaviorMembershipType == GrouperProvisioningBehaviorMembershipType.entityAttributes
          && provisioningUpdatableToInsert instanceof ProvisioningEntity) {
        membershipAttribute = true;
      }
      if (membershipAttribute) {
        attributeNameForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
      }
    }

    for (String attributeName : GrouperUtil.nonNull(GrouperUtil.nonNull(provisioningUpdatableToInsert.getAttributes()).keySet())) {
      if (!provisioningUpdatableToInsert.canInsertAttribute(attributeName)) {
        continue;
      }
      ProvisioningAttribute provisioningAttribute = provisioningUpdatableToInsert.getAttributes().get(attributeName);

      Object grouperValue = provisioningAttribute.getValue();
  
      if (GrouperUtil.isArrayOrCollection(grouperValue)) {
        if (grouperValue instanceof Collection) {
          for (Object value : (Collection)grouperValue) {
            
            if (shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(provisioningUpdatableToInsert, provisioningAttribute, value)) {
              continue;
            }
            
            provisioningUpdatableToInsert.addInternal_objectChange(
                new ProvisioningObjectChange(attributeName, 
                    ProvisioningObjectChangeAction.insert, null, value)
                );
            if (membershipAttribute && StringUtils.equals(attributeNameForMemberships, attributeName)) {
              this.membershipAddCount++;
              countAddMembershipObjectCount(provisioningAttribute, value);
            }
          }
        } else {
          throw new RuntimeException("Arrays not supported");
        }
      } else {
        // just a scalar
        if (shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(provisioningUpdatableToInsert, provisioningAttribute, grouperValue)) {
          continue;
        }
        
        ProvisioningObjectChange provisioningObjectChange = new ProvisioningObjectChange(attributeName, 
            ProvisioningObjectChangeAction.insert, null, grouperValue);
        provisioningUpdatableToInsert.addInternal_objectChange(provisioningObjectChange);
        
      }
    }
  }


  public void compareAttributesForUpdate(
      List provisioningUpdatablesToUpdate,
      Map<String, ProvisioningAttribute> grouperTargetAttributes,
      Map<String, ProvisioningAttribute> targetProvisioningAttributes,
      ProvisioningUpdatable grouperProvisioningUpdatable) {
    
    if (grouperProvisioningUpdatable == null) {
      return;
    }

    
    boolean recalcObject = grouperProvisioningUpdatable.getProvisioningWrapper().getProvisioningState().isRecalcObject();
    boolean recalcObjectMemberships = grouperProvisioningUpdatable.getProvisioningWrapper().getProvisioningState().isRecalcObjectMemberships();
    
    for (String attributeName: GrouperUtil.nonNull(grouperTargetAttributes).keySet()) {

      ProvisioningAttribute targetAttribute = GrouperUtil.nonNull(targetProvisioningAttributes).get(attributeName);
      ProvisioningAttribute grouperAttribute = GrouperUtil.nonNull(grouperTargetAttributes).get(attributeName);
      
      ProvisioningUpdatable targetProvisioningUpdatable = null;
      if (grouperProvisioningUpdatable instanceof ProvisioningGroup) {
        ProvisioningGroupWrapper provisioningGroupWrapper = ((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper();
        targetProvisioningUpdatable = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getTargetProvisioningGroup();
      }
      if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
        ProvisioningEntityWrapper provisioningEntityWrapper = ((ProvisioningEntity)grouperProvisioningUpdatable).getProvisioningEntityWrapper();
        targetProvisioningUpdatable = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getTargetProvisioningEntity();
      }
      compareAttributeForUpdateValue(grouperProvisioningUpdatable, grouperAttribute, targetProvisioningUpdatable, targetAttribute, attributeName, recalcObject);
      compareAttributeForUpdateValueMembershipOnly(grouperProvisioningUpdatable, grouperAttribute, targetProvisioningUpdatable, targetAttribute, attributeName, recalcObjectMemberships);
    }
    if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
      addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
          grouperProvisioningUpdatable);
    }
  }
  
  private boolean shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(ProvisioningUpdatable grouperProvisioningUpdatable, ProvisioningAttribute grouperAttribute, Object value) {
    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isUnresolvableSubjectsInsert()) {
      return false;
    }
    
    // membership
    if (grouperProvisioningUpdatable instanceof ProvisioningMembership) {
      ProvisioningMembership provisioningMembership = (ProvisioningMembership)grouperProvisioningUpdatable;

      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningMembership.getProvisioningMembershipWrapper();;

      if (provisioningMembershipWrapper == null) {
        return false;
      }
      
      ProvisioningMembership grouperProvisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
      
      if (grouperProvisioningMembership == null) {
        return false;
      }
      
      ProvisioningEntity provisioningEntity = grouperProvisioningMembership.getProvisioningEntity();
      
      if (provisioningEntity == null) {
        return false;
      }
      
      return !provisioningEntity.getSubjectResolutionResolvable();
    }
    
    if (grouperAttribute == null) {
      // would this ever be null?
      return false;
    }
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();

    if (!StringUtils.equals(attributeForMemberships, grouperAttribute.getName())) {
      return false;
    }

    // entity
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
      ProvisioningEntity provisioningEntity = (ProvisioningEntity)grouperProvisioningUpdatable;

      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper == null) {
        return false;
      }
      
      ProvisioningEntity grouperProvisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
      
      if (grouperProvisioningEntity == null) {
        return false;
      }
      
      return !grouperProvisioningEntity.getSubjectResolutionResolvable();
    }

    // group
    ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
    
    Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper();
    if (valueToProvisioningMembershipWrapper != null) {
      provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(value);
    }

    if (provisioningMembershipWrapper == null) {
      return false;
    }
    
    ProvisioningMembership provisioningMembership = provisioningMembershipWrapper.getGrouperProvisioningMembership();
    
    if (provisioningMembership == null) {
      return false;
    }
    
    ProvisioningEntity provisioningEntity = provisioningMembership.getProvisioningEntity();
    
    if (provisioningEntity == null) {
      return false;
    }
    
    return !provisioningEntity.getSubjectResolutionResolvable();
  }
  
  public void compareAttributeForUpdateValueMembershipOnly(ProvisioningUpdatable grouperProvisioningUpdatable, ProvisioningAttribute grouperAttribute, 
      ProvisioningUpdatable targetProvisioningUpdatable, ProvisioningAttribute targetAttribute, String attributeName, 
      boolean recalcProvisioningUpdateable) {
    
    if (grouperProvisioningUpdatable == null) {
      return;
    }

    String attributeForMemberships = null;

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != null) {
      switch (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        case membershipObjects:
          // we dont update any attribute for memberships, we just insert and delete them
          return;
        case entityAttributes:
          
          attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
          if (grouperProvisioningUpdatable instanceof ProvisioningEntity && StringUtils.equals(attributeForMemberships,  attributeName)) {
            break;
          }
          // otherwise ignore
          return;
          
        case groupAttributes:
    
          attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
          if (grouperProvisioningUpdatable instanceof ProvisioningGroup && StringUtils.equals(attributeForMemberships,  attributeName)) {
            break;
          }
          // otherwise ignore
          return;
          
        default:
          throw new RuntimeException("Not expecting membership type");
      }
    }
    // not syncing memberships?
    if (StringUtils.isBlank(attributeForMemberships)) {
      return;
    }
    if (!recalcProvisioningUpdateable) {
      
      if (grouperAttribute == null) {
        return;
      }
      for (Object value : GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).keySet()) {
        ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(value);
        if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
          if (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction() != null) {
            switch (provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction()) {
              case delete:
                
                if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembership(provisioningMembershipWrapper.getGcGrouperSyncMembership())) {
                  this.membershipDeleteCount++;
                  countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
  
                  grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(attributeForMemberships, 
                        ProvisioningObjectChangeAction.delete, value, null)
                  );
                }
                break;
              case insert:
                if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertMemberships() && 
                    !shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(grouperProvisioningUpdatable, grouperAttribute, value)) {
                  this.membershipAddCount++;
                  countAddMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
                  grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(attributeForMemberships, 
                        ProvisioningObjectChangeAction.insert, null, value)
                  );
                }
                break;
              default:
                throw new RuntimeException("Not expecting grouperIncrementalDataAction for " + grouperProvisioningUpdatable);
            }
          }          
        }
      }
    }
    // if we're doing an update for an incremental and it's not a recalc, then skip this because we are only doing memberships above
    if (grouperProvisioningUpdatable instanceof ProvisioningGroup && ((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper().getTargetProvisioningGroup() == null ) {
      return;
    }
    
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity && ((ProvisioningEntity)grouperProvisioningUpdatable).getProvisioningEntityWrapper().getTargetProvisioningEntity() == null ) {
      return;
    }
          
    Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
    Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();

    if (targetAttribute == null) {
      
      if (grouperProvisioningUpdatable.canInsertAttribute(attributeName)) {
        if (GrouperUtil.isArrayOrCollection(grouperValue)) {
          if (grouperValue instanceof Collection) {
            for (Object value : (Collection)grouperValue) {
              if (grouperAttribute.getValueToProvisioningMembershipWrapper() == null) {
                continue;
              }
              ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(value);
              if (recalcProvisioningUpdateable || provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
                value = filterDeletedMemberships(grouperAttribute, value);
                
                if (value == null) {
                  continue;
                }
                
                if (filterNonRecalcMemberships(grouperAttribute, value, recalcProvisioningUpdateable)) {
                  continue;
                }
                
                if (shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(grouperProvisioningUpdatable, grouperAttribute, value)) {
                  continue;
                }
                
                grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(attributeName, 
                        ProvisioningObjectChangeAction.insert, null, value)
                    );
              }
            }
          } else {
            throw new RuntimeException("Arrays not supported");
          }
        } else {
          ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
          if (grouperAttribute != null) {
            provisioningMembershipWrapper = GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).get(grouperValue);
          }
          if (recalcProvisioningUpdateable || (provisioningMembershipWrapper != null && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject())) {
            // just a scalar
            
            if (!shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(grouperProvisioningUpdatable, grouperAttribute, grouperValue)) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(attributeName, 
                      ProvisioningObjectChangeAction.insert, null, grouperValue)
                  );
            }
          }
          
        }
      }
    } else {

      // update
      Collection<Object> targetCollection = null;
      if (targetValue != null) {
        if (targetValue instanceof Collection) {
          targetCollection = (Collection)targetValue;
        }
      }
      Collection<Object> grouperCollection = null;
      if (grouperValue != null) {
        if (grouperValue instanceof Collection) {
          grouperCollection = new HashSet<Object>((Collection)grouperValue);
        }
      }
      
      if (grouperCollection != null) {
        Iterator<Object> iterator = grouperCollection.iterator();
        while (iterator.hasNext()) {
          Object value = iterator.next();
          value = filterDeletedMemberships(grouperAttribute, value);
          
          if (filterNonRecalcMemberships(grouperAttribute, value, recalcProvisioningUpdateable)) {
            continue;
          }
          
          if (value == null) {
            iterator.remove();
          }
        }
      }
      
      // scalar
      if (grouperCollection == null && targetCollection == null) {
        if (!attributeValueEquals(attributeName, grouperValue, targetValue, grouperProvisioningUpdatable)) {
          if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {

            ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
            if (grouperAttribute != null) {
              provisioningMembershipWrapper = GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).get(grouperValue);
            }
            if (recalcProvisioningUpdateable || (provisioningMembershipWrapper != null && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject())) {

              if (shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(grouperProvisioningUpdatable, grouperAttribute, grouperValue)) {
                grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(attributeName, 
                        ProvisioningObjectChangeAction.delete, targetValue, null)
                    );
              } else {
                grouperProvisioningUpdatable.addInternal_objectChange(
                    new ProvisioningObjectChange(attributeName, 
                        ProvisioningObjectChangeAction.update, targetValue, grouperValue)
                    );
              }
            }
          }
        }
        return;
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

          ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
          if (grouperAttribute != null) {
            provisioningMembershipWrapper = GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).get(insertValue);
          }
          if (recalcProvisioningUpdateable || (provisioningMembershipWrapper != null && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject())) {
            if (!shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(grouperProvisioningUpdatable, grouperAttribute, insertValue)) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(attributeName, 
                      ProvisioningObjectChangeAction.insert, null, insertValue)
                  );
            }
          }
        }
      }        
      Collection deletes = new HashSet<Object>(targetCollection);
      deletes.removeAll(grouperCollection);
      if (grouperProvisioningUpdatable.canDeleteAttribute(attributeName)) {
        for (Object deleteValue : deletes) {
          
          if (grouperProvisioningUpdatable.canDeleteAttributeValue(attributeName, deleteValue)) {
          
            ProvisioningMembershipWrapper provisioningMembershipWrapper = null;
            if (grouperAttribute != null) {
              provisioningMembershipWrapper = GrouperUtil.nonNull(grouperAttribute.getValueToProvisioningMembershipWrapper()).get(deleteValue);
            }
            if (recalcProvisioningUpdateable || (provisioningMembershipWrapper != null && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject())) {
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(attributeName, 
                      ProvisioningObjectChangeAction.delete, deleteValue, null)
                  );
            }
          }  
        }
      }
    }        
  }
  

  /**
   * dont compare the membership attribute of group or entity.  dont compare memberships at all
   * @param grouperProvisioningUpdatable
   * @param attributeName
   * @param grouperAttribute
   * @param targetProvisioningUpdatable
   * @param targetAttribute
   * @param recalc
   */
  public void compareAttributeForUpdateValue(
      ProvisioningUpdatable grouperProvisioningUpdatable, ProvisioningAttribute grouperAttribute, 
      ProvisioningUpdatable targetProvisioningUpdatable, ProvisioningAttribute targetAttribute, String attributeName, 
      boolean recalc) {

    // we dont update memberships right now
    if (grouperProvisioningUpdatable instanceof ProvisioningMembership) {
      return;
    }

    // this is probably a membership update
    if (grouperProvisioningUpdatable instanceof ProvisioningGroup 
        && !((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper().getProvisioningStateGroup().isUpdate()
        && !((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper().getProvisioningStateGroup().isRecalcObject()) {
      return;
    }
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != null) {
      switch (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
        case membershipObjects:
          // this is ok
          break;
        case entityAttributes:
        
          // dont deal with entity membership attribute
          if (grouperProvisioningUpdatable instanceof ProvisioningEntity && StringUtils.equals(attributeForMemberships,  attributeName)) {
            return;
          }
          break;
        case groupAttributes:
    
          // dont deal with group membership attribute
          if (grouperProvisioningUpdatable instanceof ProvisioningGroup && StringUtils.equals(attributeForMemberships,  attributeName)) {
            return;
          }
          // otherwise ignore
          break;
          
        default:
          throw new RuntimeException("Not expecting membership type");
      }
    }
    
    // We're here because we're updating the membership attribute but we're not updating other attributes
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity && !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateEntities()) {
      return;
    }
    
    // We're here because we're updating the membership attribute but we're not updating other attributes
    if (grouperProvisioningUpdatable instanceof ProvisioningGroup && !this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isUpdateGroups()) {
      return;
    }

    Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
    Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();

    // update
    Collection<Object> targetCollection = null;
    if (targetValue != null) {
      if (targetValue instanceof Collection) {
        targetCollection = (Collection)targetValue;
      }
    }
    Collection<Object> grouperCollection = null;
    if (grouperValue != null) {
      if (grouperValue instanceof Collection) {
        grouperCollection = new HashSet<Object>((Collection)grouperValue);
      }
    }
    
    // remove null values from collection
    if (grouperCollection != null) {
      Iterator<Object> iterator = grouperCollection.iterator();
      while (iterator.hasNext()) {
        Object value = iterator.next();
        
        if (value == null) {
          iterator.remove();
        }
      }
    }

    // if its not a recalc and collection
    if (!recalc && grouperCollection != null) {
      // update all updatable fields?  weird that target value will be null, but thats ok
      if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        for (Object value : grouperCollection) {
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
                  ProvisioningObjectChangeAction.insert, null, value)
              );
        }
      }
      return;
    }

    // if its not a recalc and scalar
    if (!recalc) {
      // update all updatable fields?  weird that target value will be null, but thats ok
      if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        grouperProvisioningUpdatable.addInternal_objectChange(
            new ProvisioningObjectChange(attributeName, 
                ProvisioningObjectChangeAction.update, null, grouperValue)
            );
      }
      return;
    }

    // its a recalc, and collection insert
    if (targetValue == null && grouperCollection != null) {
      
      if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        for (Object value : grouperCollection) {
          
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
                  ProvisioningObjectChangeAction.insert, null, value)
              );
        }
      }
      return;
    }
    
    // its a recalc, and scalar insert
    if (targetValue == null && grouperValue != null) {
      
      if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        // just a scalar
        grouperProvisioningUpdatable.addInternal_objectChange(
            new ProvisioningObjectChange(attributeName, 
                targetAttribute == null ? ProvisioningObjectChangeAction.insert : ProvisioningObjectChangeAction.update, null, grouperValue)
            );
      }
      return;
    }

    // do collection deletes
    if (grouperValue == null && targetCollection != null) {
      for (Object value : targetCollection) {
        if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
                  ProvisioningObjectChangeAction.delete, value, null)
              );
        }
      }
      return;
    }  

    // do scalar deletes
    if (grouperValue == null && targetValue != null) {
      
      if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        grouperProvisioningUpdatable.addInternal_objectChange(
            new ProvisioningObjectChange(attributeName, 
                grouperAttribute == null ? ProvisioningObjectChangeAction.delete : ProvisioningObjectChangeAction.update, targetValue, null)
            );
      }
      return;
    }
      
    // scalar update
    if (grouperCollection == null && targetCollection == null) {
      if (!attributeValueEquals(attributeName, grouperValue, targetValue, grouperProvisioningUpdatable)) {
        if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
                  ProvisioningObjectChangeAction.update, targetValue, grouperValue)
              );
        }
      }
      return;
    }

    // convert both to collections
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
    if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
      for (Object insertValue : inserts) {
        grouperProvisioningUpdatable.addInternal_objectChange(
            new ProvisioningObjectChange(attributeName, 
                ProvisioningObjectChangeAction.insert, null, insertValue)
            );

      }
    }        
    Collection deletes = new HashSet<Object>(targetCollection);
    deletes.removeAll(grouperCollection);
    if (grouperProvisioningUpdatable.canDeleteAttribute(attributeName)) {
      for (Object deleteValue : deletes) {
        
        if (grouperProvisioningUpdatable.canUpdateAttribute(attributeName)) {
        
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
                  ProvisioningObjectChangeAction.delete, deleteValue, null)
              );
        }  
      }
    }
  }
  
  /**
   * if this is a membership and the membership is deleted, return null
   * @param grouperAttribute
   * @param grouperValue
   * @return
   */
  public Object filterDeletedMemberships(ProvisioningAttribute grouperAttribute, Object grouperValue) {
    
    if (grouperAttribute == null || grouperAttribute.getValueToProvisioningMembershipWrapper() == null) {
      return grouperValue;
    }
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(grouperValue);
    
    if (provisioningMembershipWrapper != null && provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
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
  public boolean filterNonRecalcMemberships(ProvisioningAttribute grouperAttribute, Object grouperValue, boolean groupOrEntityRecalc) {
    
    if (groupOrEntityRecalc) {
      return false;
    }
    
    if (grouperAttribute == null || grouperAttribute.getValueToProvisioningMembershipWrapper() == null) {
      return false;
    }
    
    ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperAttribute.getValueToProvisioningMembershipWrapper().get(grouperValue);
    
    if (provisioningMembershipWrapper != null && !provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
      return true;
    }

    return false;
    
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
        if (provisioningEntityWrapper.getErrorCode() != null && !provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
          iterator.remove();
        }
      }
    }
    
    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForInsert = new HashSet<ProvisioningEntityWrapper>();
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForUpdate = new HashSet<ProvisioningEntityWrapper>();
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForDelete = new HashSet<ProvisioningEntityWrapper>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper: GrouperUtil.nonNull(provisioningEntityWrappers)) {
      
      if (provisioningEntityWrapper.getTargetProvisioningEntity() != null && provisioningEntityWrapper.getTargetProvisioningEntity().getProvisioningEntityWrapper() == null ) {
        continue;
      }

      if (!provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
        
        if (provisioningEntityWrapper.getGcGrouperSyncMember() != null && provisioningEntityWrapper.getGcGrouperSyncMember().isProvisionable() && !provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget()) {
        
          if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
          
            if (provisioningEntityWrapper.getGrouperTargetEntity() != null && provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
              
              boolean isUnresolvableSubject = provisioningEntityWrapper.getGrouperProvisioningEntity() != null && provisioningEntityWrapper.getGrouperProvisioningEntity().getSubjectResolutionResolvable() == Boolean.FALSE;

              // check if we're inserting unresolvable subjects
              if (!isUnresolvableSubject || this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isUnresolvableSubjectsInsert()) {
                provisioningEntityWrappersForInsert.add(provisioningEntityWrapper);
              }
              continue;
            }
          }
        }
        if (provisioningEntityWrapper.getGcGrouperSyncMember() != null && provisioningEntityWrapper.getGcGrouperSyncMember().isProvisionable() 
            && provisioningEntityWrapper.getGcGrouperSyncMember().isInTarget() 
            // if its entity attributes there could be memberships
            && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.entityAttributes
            && provisioningEntityWrapper.getProvisioningStateEntity().isCreate()) {
          //inserts happen at previous points in the workflow (createMissingEntities)
          continue;
        }
      }
      
      // deletes
      if (provisioningEntityWrapper.getGcGrouperSyncMember() == null || !provisioningEntityWrapper.getGcGrouperSyncMember().isProvisionable()) {
        
        boolean deleteMembershipAttributeValues = false;
        if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
          
          if ( (provisioningEntityWrapper.getGrouperTargetEntity() == null || provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) && provisioningEntityWrapper.getTargetProvisioningEntity() != null) { 
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntities()) {
              provisioningEntityWrappersForDelete.add(provisioningEntityWrapper);
              continue;
            }
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.entityAttributes) {
              continue;
            }

            deleteMembershipAttributeValues = true;
          }
          
        } else {
          // isDelete is applicable only for non-recalc 
          if (provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntity(provisioningEntityWrapper.getGcGrouperSyncMember())) {
              provisioningEntityWrappersForDelete.add(provisioningEntityWrapper);
              continue;
            }
            
            deleteMembershipAttributeValues = true;
          }
          
        }

        if (deleteMembershipAttributeValues) {
          if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes 
              && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
            String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
            
            Set<?> attributeValueSet = provisioningEntityWrapper.getGrouperTargetEntity().retrieveAttributeValueSet(attributeForMemberships);
            ProvisioningAttribute provisioningAttribute = provisioningEntityWrapper.getGrouperTargetEntity().getAttributes().get(attributeForMemberships);
            boolean deleted = false;
            for (Object obj: GrouperUtil.nonNull(attributeValueSet)) {
              String membershipValue = GrouperUtil.stringValue(obj);
              ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(membershipValue);
              if (!provisioningMembershipWrapper.getGcGrouperSyncMembership().isInTarget()) {
                continue;
              }

              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembership(provisioningMembershipWrapper.getGcGrouperSyncMembership())) {
                this.membershipDeleteCount++;
                countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
                provisioningEntityWrapper.getGrouperTargetEntity().addInternal_objectChange(
                  new ProvisioningObjectChange(attributeForMemberships, 
                      ProvisioningObjectChangeAction.delete, membershipValue, null)
                );
                if (!deleted) {
                  deleted = true;
                  provisioningEntityWrappersForUpdate.add(provisioningEntityWrapper);
                }
              }
              
            }
          }

        }
        continue;
        
      }
      
      // updates
      if (provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
        
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
        
        if (!provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed() && provisioningEntityWrapper.getProvisioningStateEntity().isDelete() && provisioningEntityWrapper.getGrouperTargetEntity() != null 
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

        compareAttributesForUpdate(provisioningEntitiesToUpdate, grouperTargetEntity == null ? null : grouperTargetEntity.getAttributes(),
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
        if (provisioningGroupWrapper.getErrorCode() != null && !provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
          iterator.remove();
        }
      }
    }
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForInsert = new HashSet<ProvisioningGroupWrapper>();
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForUpdate = new HashSet<ProvisioningGroupWrapper>();
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForDelete = new HashSet<ProvisioningGroupWrapper>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper: GrouperUtil.nonNull(provisioningGroupWrappers)) {
      
      if (provisioningGroupWrapper.getTargetProvisioningGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup().getProvisioningGroupWrapper() == null ) {
        continue;
      }

      if (!provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
        
        if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable() && !provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()) {
        
          if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
          
            if (provisioningGroupWrapper.getGrouperTargetGroup() != null && provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
              
              provisioningGroupWrappersForInsert.add(provisioningGroupWrapper);
              continue;
            }
          }
        }
        
        if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable() 
            && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()
            && provisioningGroupWrapper.getProvisioningStateGroup().isCreate()
            // if its group attributes there could be memberships
            && this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.groupAttributes ) {
          //inserts happen at previous points in the workflow (createMissingGroups)
          continue;
        }

      }
      
      // deletes
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null || !provisioningGroupWrapper.getGcGrouperSyncGroup().isProvisionable()) {
        
        boolean deleteMembershipAttributeValues = false;
        if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
          
          if ( (provisioningGroupWrapper.getGrouperTargetGroup() == null || provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) && provisioningGroupWrapper.getTargetProvisioningGroup() != null) { 
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteGroups()) {
              provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
              continue;
            }
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.groupAttributes) {
              continue;
            }
            deleteMembershipAttributeValues = true;

          }
          
        } else {
          // isDelete is applicable only for non-recalc 
          if (provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
            provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
            continue;
          }
          
          deleteMembershipAttributeValues = true;
        }

        if (deleteMembershipAttributeValues) {
          if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes 
              && provisioningGroupWrapper.getGrouperTargetGroup() != null) {
            String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
            
            Set<?> attributeValueSet = provisioningGroupWrapper.getGrouperTargetGroup().retrieveAttributeValueSet(attributeForMemberships);
            ProvisioningAttribute provisioningAttribute = provisioningGroupWrapper.getGrouperTargetGroup().getAttributes().get(attributeForMemberships);
            boolean deleted = false;
            for (Object obj: GrouperUtil.nonNull(attributeValueSet)) {
              String membershipValue = GrouperUtil.stringValue(obj);
              if (provisioningAttribute.getValueToProvisioningMembershipWrapper() == null) {
                continue;
              }
              ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(membershipValue);
              if (!provisioningMembershipWrapper.getGcGrouperSyncMembership().isInTarget()) {
                continue;
              }
              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteMembership(provisioningMembershipWrapper.getGcGrouperSyncMembership())) {
                this.membershipDeleteCount++;
                countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
                provisioningGroupWrapper.getGrouperTargetGroup().addInternal_objectChange(
                  new ProvisioningObjectChange(attributeForMemberships, 
                      ProvisioningObjectChangeAction.delete, membershipValue, null)
                );
                if (!deleted) {
                  deleted = true;
                  provisioningGroupWrappersForUpdate.add(provisioningGroupWrapper);
                }
              }
              
            }
          }

        }
      } else if (provisioningGroupWrapper.getGcGrouperSyncGroup() != null && provisioningGroupWrapper.getGcGrouperSyncGroup().isInTarget()
          && provisioningGroupWrapper.getProvisioningStateGroup().isDelete() && provisioningGroupWrapper.getErrorCode() == GcGrouperSyncErrorCode.MEM) {
        provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
        continue;
      }
      
      // updates
      if (provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
        
        if (provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
          
          provisioningGroupWrappersForUpdate.add(provisioningGroupWrapper);
          continue;
          
        }
        
        
      } else {
        
        boolean doUpdate = provisioningGroupWrapper.getProvisioningStateGroup().isUpdate();
        doUpdate = doUpdate 
            || this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes;

        if (!doUpdate) {
          // if there's no changelog that says it was updated then it might just be a membership change so skip the update
          continue;
        }

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
        
        if (!provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed() && provisioningGroupWrapper.getProvisioningStateGroup().isDelete() && provisioningGroupWrapper.getGrouperTargetGroup() != null 
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
        
        // this could delete stuff in target that grouper doesnt track... 
        // at some point we can add that to failsafes but right now its not there...
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null 
            && !StringUtils.isBlank(provisioningGroupWrapper.getGrouperProvisioningGroup().getId())) {
          this.groupUuidsToDelete.add(provisioningGroupWrapper.getGrouperProvisioningGroup().getId());
        }
        
        provisioningGroupsToDelete.add(groupToDelete);
        
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
        
        compareAttributesForUpdate(provisioningGroupsToUpdate, grouperTargetGroup == null ? null : grouperTargetGroup.getAttributes(),
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
        if (provisioningMembershipWrapper.getErrorCode() != null && !provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
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
        
        boolean shouldReplace = provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed() || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync();
        if (!shouldReplace) {
          
          shouldReplace = !(GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
                .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembership(), false)
              || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false));
          
          shouldReplace = shouldReplace && !(GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
              .getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)
            || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false));
        }
        if (shouldReplace) {
          
          ProvisioningGroup provisioningGroup = provisioningMembershipWrapper.getGrouperTargetMembership().getProvisioningGroup();
          if (provisioningGroup.getProvisioningGroupWrapper().getProvisioningStateGroup().isDelete()) {
            continue;
          }
          List<ProvisioningMembership> provisioningMemberships = provisioningMembershipsToReplace.get(provisioningGroup);
          if (provisioningMemberships == null) {
            provisioningMemberships = new ArrayList<ProvisioningMembership>();
          }
          
          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
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
      if (!handleRecalcs && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
        continue;
      }
      
      ProvisioningMembership grouperTargetMembership = (provisioningMembershipWrapper.getProvisioningStateMembership().isDelete() && provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) ? null : provisioningMembershipWrapper.getGrouperTargetMembership();
      
      if (grouperTargetMembership != null && grouperTargetMembership.getProvisioningMembershipWrapper() == null ) {
        continue;
      }
      
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
      
      Object grouperMatchingId = null;
      if (grouperTargetMembership != null && GrouperUtil.length(grouperTargetMembership.getMatchingIdAttributeNameToValues()) > 0) {
        grouperMatchingId = grouperTargetMembership.getMatchingIdAttributeNameToValues().iterator().next().getAttributeValue(); 
      }
      Object targetMatchingId = null;
      if (targetProvisioningMembership != null && GrouperUtil.length(targetProvisioningMembership.getMatchingIdAttributeNameToValues()) > 0) {
        targetMatchingId = targetProvisioningMembership.getMatchingIdAttributeNameToValues().iterator().next().getAttributeValue(); 
      }
      
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
        
        if (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()) {
          continue;
        }
        
        if (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isSelectResultProcessed() || this.grouperProvisioner.getProvisioningStateGlobal().isSelectResultProcessedMemberships()) {
          matchingIdsToInsert.add(key);
        } else if (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.insert) {
          matchingIdsToInsert.add(key);
        }
        
      }

      matchingIdsToInsert.removeAll(targetMatchingIdToTargetMembership.keySet());
      
      if (grouperProvisioningBehavior.isInsertMemberships()) {

        List<ProvisioningMembership> provisioningMembershipsToInsert = new ArrayList<ProvisioningMembership>();
        
        for (Object groupIdEntityIdToInsert: matchingIdsToInsert) {
          ProvisioningMembership membershipToInsert = grouperMatchingIdToTargetMembership.get(groupIdEntityIdToInsert);

          if (shouldSkipMembershipAttributeInsertDueToUnresolvableSubject(membershipToInsert, null, null)) {
            continue;
          }
          
          this.membershipAddCount++;
          countAddMembershipObjectCount(membershipToInsert);
          compareAttributesForInsert(membershipToInsert);
    
          provisioningMembershipsToInsert.add(membershipToInsert);
        }
    
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectInserts().setProvisioningMemberships(provisioningMembershipsToInsert);
      }      
    
      // memberships to delete
      Set<Object> groupIdEntityIdsToDelete = new HashSet<Object>();
      for (Object key : targetMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = targetMatchingIdToTargetMembership.get(key);
        if (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isSelectResultProcessed()) {
          groupIdEntityIdsToDelete.add(key);
        }
      }
      groupIdEntityIdsToDelete.removeAll(grouperMatchingIdToTargetMembership.keySet());
      for (Object key : grouperMatchingIdToTargetMembership.keySet()) {
        ProvisioningMembership grouperTargetMembership = grouperMatchingIdToTargetMembership.get(key);
        if (!grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isSelectResultProcessed()) {
          if (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.delete || 
              (grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()
                  && (grouperTargetMembership.getProvisioningMembershipWrapper().getTargetProvisioningMembership() != null 
                  || grouperTargetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().isInTarget() ))) {
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
            this.membershipDeleteCount++;
            countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());

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
          
          compareAttributesForUpdate(provisioningMembershipsToUpdate, grouperTargetMembership == null ? null : grouperTargetMembership.getAttributes(),
              targetProvisioningMembership == null ? null : targetProvisioningMembership.getAttributes(), grouperTargetMembership);
          
        }
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().getTargetObjectUpdates().setProvisioningMemberships(provisioningMembershipsToUpdate);
      }
    }
  }

  /**
   * 
   * @param grouperProvisioningMembership
   */
  private void countDeleteMembershipObjectCount(ProvisioningMembership grouperProvisioningMembership) {
    if (grouperProvisioningMembership == null) {
      return;
    }
    ProvisioningGroup provisioningGroup = grouperProvisioningMembership.getProvisioningGroup();
    if (provisioningGroup == null) {
      return;
    }
    String groupUuid = provisioningGroup.getId();
    if (StringUtils.isBlank(groupUuid)) {
      return;
    }
    Integer count = this.groupUuidToMembershipDeleteCount.get(groupUuid);
    if (count == null) {
      count = 0;
    }
    count++;
    this.groupUuidToMembershipDeleteCount.put(groupUuid, count);
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

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroupForUpdate.getProvisioningGroupWrapper();
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
      boolean groupProvisionable = gcGrouperSyncGroup == null || gcGrouperSyncGroup.isProvisionable();
      
      if (groupProvisionable && membershipCountForGroup > countMembershipDelete) {
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
  
  
  public void addEntityDefaultMembershipAttributeValueIfAllRemoved(List<ProvisioningEntity> grouperTargetEntitiesForUpdate) {
    
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
      
      int membershipCountForEntity = this.grouperProvisioner.retrieveGrouperDao().retrieveMembershipCountForEntity(grouperTargetEntityForUpdate.getProvisioningEntityWrapper());

      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntityForUpdate.getProvisioningEntityWrapper();
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGcGrouperSyncMember();
      boolean entityProvisionable = gcGrouperSyncMember == null || gcGrouperSyncMember.isProvisionable();
      
      if (entityProvisionable && membershipCountForEntity > countMembershipDelete) {
        continue;
      }

      if (membershipCountForEntity > countMembershipDelete) {
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


  private void countAddMembershipObjectCount(ProvisioningAttribute provisioningAttribute, Object value) {
    
    if (provisioningAttribute == null) {
      return;
    }
      
    Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = 
        provisioningAttribute.getValueToProvisioningMembershipWrapper();
  
    if (valueToProvisioningMembershipWrapper != null) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper.get(value);
      if (provisioningMembershipWrapper == null) {
        return;
      }
      countAddMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());
    }
  }


  /**
   * 
   * @param grouperProvisioningMembership
   */
  private void countAddMembershipObjectCount(ProvisioningMembership grouperProvisioningMembership) {
    if (grouperProvisioningMembership == null) {
      return;
    }
    ProvisioningGroup provisioningGroup = grouperProvisioningMembership.getProvisioningGroup();
    if (provisioningGroup == null) {
      return;
    }
    String groupUuid = provisioningGroup.getId();
    if (StringUtils.isBlank(groupUuid)) {
      return;
    }
    Integer count = this.groupUuidToMembershipAddCount.get(groupUuid);
    if (count == null) {
      count = 0;
    }
    count++;
    this.groupUuidToMembershipAddCount.put(groupUuid, count);
  }
  
  
}
