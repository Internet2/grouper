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

    
    boolean recalc = grouperProvisioningUpdatable.isRecalc();
    
    for (String attributeName: GrouperUtil.nonNull(grouperTargetAttributes).keySet()) {

      ProvisioningAttribute targetAttribute = GrouperUtil.nonNull(targetProvisioningAttributes).get(attributeName);
      ProvisioningAttribute grouperAttribute = GrouperUtil.nonNull(grouperTargetAttributes).get(attributeName);
      
      Object grouperValue = grouperAttribute == null ? null : grouperAttribute.getValue();
      Object targetValue = targetAttribute == null ? null : targetAttribute.getValue();
      
      ProvisioningUpdatable targetProvisioningUpdatable = null;
      if (grouperProvisioningUpdatable instanceof ProvisioningGroup) {
        ProvisioningGroupWrapper provisioningGroupWrapper = ((ProvisioningGroup)grouperProvisioningUpdatable).getProvisioningGroupWrapper();
        targetProvisioningUpdatable = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getTargetProvisioningGroup();
      }
      if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
        ProvisioningEntityWrapper provisioningEntityWrapper = ((ProvisioningEntity)grouperProvisioningUpdatable).getProvisioningEntityWrapper();
        targetProvisioningUpdatable = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getTargetProvisioningEntity();
      }
      compareAttributeForUpdateValue(grouperProvisioningUpdatable, grouperAttribute, targetProvisioningUpdatable, targetAttribute, attributeName, recalc);
      compareAttributeForUpdateValueMembershipOnly(grouperProvisioningUpdatable, grouperAttribute, targetProvisioningUpdatable, targetAttribute, attributeName, recalc);
    }
    if (GrouperUtil.length(grouperProvisioningUpdatable.getInternal_objectChanges()) > 0) {
      addProvisioningUpdatableToUpdateIfNotThere(provisioningUpdatablesToUpdate, 
          grouperProvisioningUpdatable);
    }
  }
  
  public void compareAttributeForUpdateValueMembershipOnly(ProvisioningUpdatable grouperProvisioningUpdatable, ProvisioningAttribute grouperAttribute, 
      ProvisioningUpdatable targetProvisioningUpdatable, ProvisioningAttribute targetAttribute, String attributeName, 
      boolean recalc) {
    
    if (grouperProvisioningUpdatable == null) {
      return;
    }

    String attributeForMemberships = null;

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
    if (StringUtils.isBlank(attributeForMemberships)) {
      throw new RuntimeException("Attribute for memberships is blank!");
    }
    if (!recalc) {
      
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
                this.membershipDeleteCount++;
                countDeleteMembershipObjectCount(provisioningMembershipWrapper.getGrouperProvisioningMembership());

                grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(attributeForMemberships, 
                      ProvisioningObjectChangeAction.delete, value, null)
                );
              }
              break;
            case insert:
              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isInsertMemberships()) {
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
      return;
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
              
              value = filterDeletedMemberships(grouperAttribute, value);
              
              if (filterNonRecalcMemberships(grouperAttribute, value, recalc)) {
                continue;
              }
              
              grouperProvisioningUpdatable.addInternal_objectChange(
                  new ProvisioningObjectChange(attributeName, 
                      ProvisioningObjectChangeAction.insert, null, value)
                  );
            }
          } else {
            throw new RuntimeException("Arrays not supported");
          }
        } else {
          // just a scalar
          grouperProvisioningUpdatable.addInternal_objectChange(
              new ProvisioningObjectChange(attributeName, 
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
              new ProvisioningObjectChange(attributeName, 
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
                new ProvisioningObjectChange(attributeName, 
                    ProvisioningObjectChangeAction.delete, deleteValue, null)
                );
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
    
    String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
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
                  ProvisioningObjectChangeAction.update, null, value)
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
                  ProvisioningObjectChangeAction.update, null, value)
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
                ProvisioningObjectChangeAction.update, null, grouperValue)
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
                  ProvisioningObjectChangeAction.update, value, null)
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
                ProvisioningObjectChangeAction.update, targetValue, null)
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
                ProvisioningObjectChangeAction.update, null, insertValue)
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
                  ProvisioningObjectChangeAction.update, deleteValue, null)
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
  public boolean filterNonRecalcMemberships(ProvisioningAttribute grouperAttribute, Object grouperValue, boolean groupOrEntityRecalc) {
    
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
        if (provisioningEntityWrapper.getErrorCode() != null && !provisioningEntityWrapper.isDelete()) {
          iterator.remove();
        }
      }
    }
    
    
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForInsert = new HashSet<ProvisioningEntityWrapper>();
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForUpdate = new HashSet<ProvisioningEntityWrapper>();
    Set<ProvisioningEntityWrapper> provisioningEntityWrappersForDelete = new HashSet<ProvisioningEntityWrapper>();
    
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
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isDeleteEntity(provisioningEntityWrapper.getGcGrouperSyncMember())) {
              provisioningEntityWrappersForDelete.add(provisioningEntityWrapper);
              continue;
            }
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes 
                && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
              String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
              
              Set<?> attributeValueSet = provisioningEntityWrapper.getGrouperTargetEntity().retrieveAttributeValueSet(attributeForMemberships);
              ProvisioningAttribute provisioningAttribute = provisioningEntityWrapper.getGrouperTargetEntity().getAttributes().get(attributeForMemberships);
              boolean deleted = false;
              for (Object obj: GrouperUtil.nonNull(attributeValueSet)) {
                String membershipValue = GrouperUtil.stringValue(obj);
                ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(membershipValue);
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
          
        }
        
        continue;
        
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
        if (provisioningGroupWrapper.getErrorCode() != null && !provisioningGroupWrapper.isDelete()) {
          iterator.remove();
        }
      }
    }
    
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForInsert = new HashSet<ProvisioningGroupWrapper>();
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForUpdate = new HashSet<ProvisioningGroupWrapper>();
    Set<ProvisioningGroupWrapper> provisioningGroupWrappersForDelete = new HashSet<ProvisioningGroupWrapper>();
    
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
          }
          
          if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes 
              && provisioningGroupWrapper.getGrouperTargetGroup() != null) {
            String attributeForMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
            
            Set<?> attributeValueSet = provisioningGroupWrapper.getGrouperTargetGroup().retrieveAttributeValueSet(attributeForMemberships);
            ProvisioningAttribute provisioningAttribute = provisioningGroupWrapper.getGrouperTargetGroup().getAttributes().get(attributeForMemberships);
            boolean deleted = false;
            for (Object obj: GrouperUtil.nonNull(attributeValueSet)) {
              String membershipValue = GrouperUtil.stringValue(obj);
              ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningAttribute.getValueToProvisioningMembershipWrapper().get(membershipValue);
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
          && provisioningGroupWrapper.isDelete() && provisioningGroupWrapper.getErrorCode() == GcGrouperSyncErrorCode.MEM) {
        provisioningGroupWrappersForDelete.add(provisioningGroupWrapper);
        continue;
      }
      
      // updates
      if (provisioningGroupWrapper.isRecalc()) {
        
        if (provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
          
          provisioningGroupWrappersForUpdate.add(provisioningGroupWrapper);
          continue;
          
        }
        
        
      } else {
        
        if (!provisioningGroupWrapper.isUpdate()) {
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
        if (provisioningMembershipWrapper.getErrorCode() != null && !provisioningMembershipWrapper.isDelete()) {
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
          if (provisioningGroup.getProvisioningGroupWrapper().isDelete()) {
            continue;
          }
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
          this.membershipAddCount++;
          ProvisioningMembership membershipToInsert = grouperMatchingIdToTargetMembership.get(groupIdEntityIdToInsert);
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
