package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningMatchingIdIndex {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * these inputs might not have a group wrapper, might not have a matching id
   * @param targetGroups
   */
  public List<ProvisioningGroup> mergeInNewTargetGroupsForMemberships(List<ProvisioningGroup> targetGroups) {
    
    List<ProvisioningGroup> result = new ArrayList<>();
    if (GrouperUtil.length(targetGroups) == 0) {
      return targetGroups;
    }

    int cantMatchMembershipGroup = 0;
    
    String membershipAttributeName = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    // index the existing target groups by matching id, and pick the best one if multiple
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningGroup> existingTargetMatchingAttributeToTargetGroup = new HashMap<>();
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningGroup> newUnmatchedTargetMatchingAttributeToTargetGroup = new HashMap<>();

    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups())) {
      if (GrouperUtil.length(targetGroup.getMatchingIdAttributeNameToValues()) > 0) {
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetGroup.getMatchingIdAttributeNameToValues().iterator().next();
        existingTargetMatchingAttributeToTargetGroup.put(bestMatchingId, targetGroup);
      }
    }
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(membershipAttributeName);
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return targetGroups;
    }
    
    String defaultValue = grouperProvisioningConfigurationAttribute.getDefaultValue();
    if (StringUtils.equals(defaultValue, GrouperProvisioningAttributeManipulation.DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
      defaultValue = "";
    }
    
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      if (GrouperUtil.length(targetGroup.getMatchingIdAttributeNameToValues()) > 0) {
        
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetGroup.getMatchingIdAttributeNameToValues().iterator().next();
        
        ProvisioningGroup existingTargetGroup = existingTargetMatchingAttributeToTargetGroup.get(bestMatchingId);        

        if (existingTargetGroup != null) {
          result.add(existingTargetGroup);
          mergeInMembershipValues(existingTargetGroup, targetGroup, membershipAttributeName, defaultValue);
        } else {
          
          ProvisioningGroup firstTargetGroup = newUnmatchedTargetMatchingAttributeToTargetGroup.get(bestMatchingId);
          if (firstTargetGroup == null) {
            firstTargetGroup = targetGroup;
            newUnmatchedTargetMatchingAttributeToTargetGroup.put(bestMatchingId, targetGroup);
            result.add(targetGroup);
          } else {
            mergeInMembershipValues(firstTargetGroup, targetGroup, membershipAttributeName, defaultValue);
          }
          cantMatchMembershipGroup++;
        }
      }
    }
    
    if (cantMatchMembershipGroup > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "cantMatchMembershipGroup", cantMatchMembershipGroup);
    }
    return result;
  }

  public void mergeInMembershipValues(ProvisioningGroup existingTargetGroup, ProvisioningGroup targetGroup, String membershipAttributeName, Object defaultValue) {
    Set<?> values = targetGroup.retrieveAttributeValueSetForMemberships();

    // if the new part has nothing, continue
    if (GrouperUtil.length(values) == 0) {
      return;
    }
    
    // if the new part only has default, then ignore it
    if (GrouperUtil.length(values) == 0 && defaultValue != null && GrouperUtil.equals(defaultValue, values.iterator().next())) {
      return;
    }
    
    // if the old part only has default, and the new exists, remove the old default
    Set<?> membershipAttributeValueSet = existingTargetGroup.retrieveAttributeValueSetForMemberships();
    if (GrouperUtil.length(membershipAttributeValueSet) == 1
        && GrouperUtil.equals(defaultValue, values.iterator().next())) {
      membershipAttributeValueSet.remove(defaultValue);
    }
    
    for (Object membershipValue : GrouperUtil.nonNull(values)) {
      existingTargetGroup.addAttributeValueForMembership(membershipValue, null, false);
    }

  }
  
  /**
   * these inputs might not have a entity wrapper, might not have a matching id
   * @param targetEntities
   */
  public List<ProvisioningEntity> mergeInNewTargetEntitiesForMemberships(List<ProvisioningEntity> targetEntities) {
    
    List<ProvisioningEntity> result = new ArrayList<>();
    if (GrouperUtil.length(targetEntities) == 0) {
      return targetEntities;
    }

    int cantMatchMembershipEntity = 0;
    
    String membershipAttributeName = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    // index the existing target entities by matching id, and pick the best one if multiple
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningEntity> existingTargetMatchingAttributeToTargetEntity = new HashMap<>();
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningEntity> newUnmatchedTargetMatchingAttributeToTargetEntity = new HashMap<>();

    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities())) {
      if (GrouperUtil.length(targetEntity.getMatchingIdAttributeNameToValues()) > 0) {
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetEntity.getMatchingIdAttributeNameToValues().iterator().next();
        existingTargetMatchingAttributeToTargetEntity.put(bestMatchingId, targetEntity);
      }
    }
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(membershipAttributeName);
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return targetEntities;
    }
    
    String defaultValue = grouperProvisioningConfigurationAttribute.getDefaultValue();
    if (StringUtils.equals(defaultValue, GrouperProvisioningAttributeManipulation.DEFAULT_VALUE_EMPTY_STRING_CONFIG)) {
      defaultValue = "";
    }
    
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      if (GrouperUtil.length(targetEntity.getMatchingIdAttributeNameToValues()) > 0) {
        
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetEntity.getMatchingIdAttributeNameToValues().iterator().next();
        
        ProvisioningEntity existingTargetEntity = existingTargetMatchingAttributeToTargetEntity.get(bestMatchingId);        

        if (existingTargetEntity != null) {
          result.add(existingTargetEntity);
          mergeInMembershipValues(existingTargetEntity, targetEntity, membershipAttributeName, defaultValue);
        } else {
          
          ProvisioningEntity firstTargetEntity = newUnmatchedTargetMatchingAttributeToTargetEntity.get(bestMatchingId);
          if (firstTargetEntity == null) {
            firstTargetEntity = targetEntity;
            newUnmatchedTargetMatchingAttributeToTargetEntity.put(bestMatchingId, targetEntity);
            result.add(targetEntity);
          } else {
            mergeInMembershipValues(firstTargetEntity, targetEntity, membershipAttributeName, defaultValue);
          }
          cantMatchMembershipEntity++;
        }
      }
    }
    
    if (cantMatchMembershipEntity > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "cantMatchMembershipEntity", cantMatchMembershipEntity);
    }
    return result;
  }

  /**
   * 
   * @param targetGroups
   */
  public void mergeInNewTargetGroups(List<ProvisioningGroup> targetGroups) {
    
    if (GrouperUtil.length(targetGroups) == 0) {
      return;
    }
    
    int duplicateTargetGroups = 0;
    
    // lets try to merge these in, if they replace newer ones
    Set<ProvisioningGroupWrapper> newWrappers = new HashSet<>();
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      if (targetGroup.getProvisioningGroupWrapper() != null) {
        newWrappers.add(targetGroup.getProvisioningGroupWrapper());
      }
    }

    // index the new target groups by matching id, and pick the best one if multiple
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningGroupWrapper> targetMatchingAttributeToNewWrapper = new HashMap<>();
    for (ProvisioningGroup targetGroup : GrouperUtil.nonNull(targetGroups)) {
      if (targetGroup.getProvisioningGroupWrapper() == null) {
        continue;
      }
      if (GrouperUtil.length(targetGroup.getMatchingIdAttributeNameToValues()) > 0) {
        
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetGroup.getMatchingIdAttributeNameToValues().iterator().next();
        ProvisioningGroupWrapper previousNewTargetGroupWrapper = targetMatchingAttributeToNewWrapper.get(bestMatchingId);
        ProvisioningGroupWrapper currentNewTargetGroupWrapper = targetGroup.getProvisioningGroupWrapper();
        
        boolean useNew = shouldReplaceTargetProvisioningGroup(previousNewTargetGroupWrapper, currentNewTargetGroupWrapper);
        
        if (useNew) {
          targetMatchingAttributeToNewWrapper.put(
              bestMatchingId, targetGroup.getProvisioningGroupWrapper());
        }
      }
    }
    
    // loop through existing wrappers and see if the targets match, and pick the best one
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningGroupWrapper> foundMatchOfMatchingAttributeToWrapper = new HashMap<>();
    for (ProvisioningGroupWrapper existingGroupWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (existingGroupWrapper.getGrouperTargetGroup() != null && existingGroupWrapper.getTargetProvisioningGroup() != null) {
        if (GrouperUtil.length(existingGroupWrapper.getTargetProvisioningGroup().getMatchingIdAttributeNameToValues()) > 0) {
          ProvisioningUpdatableAttributeAndValue matchingId = existingGroupWrapper.getTargetProvisioningGroup().getMatchingIdAttributeNameToValues().iterator().next();
          if (targetMatchingAttributeToNewWrapper.containsKey(matchingId)) {
            foundMatchOfMatchingAttributeToWrapper.put(matchingId, existingGroupWrapper);
            ProvisioningGroupWrapper newWrapper = targetMatchingAttributeToNewWrapper.get(matchingId);
            boolean useNew = shouldReplaceTargetProvisioningGroup(existingGroupWrapper, newWrapper);
            if (useNew) {
              existingGroupWrapper.setTargetProvisioningGroup(newWrapper.getTargetProvisioningGroup());
              duplicateTargetGroups++;
            }
          }
        }
      }
    }

    // loop through wrappers and remove the new ones which arent needed
    Iterator<ProvisioningGroupWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().iterator();
    while (iterator.hasNext()) {
      ProvisioningGroupWrapper newGroupWrapper = iterator.next();
      // see if this is a new one
      if (newGroupWrapper.getTargetProvisioningGroup() != null && newGroupWrapper.getGrouperTargetGroup() == null) {
        if (foundMatchOfMatchingAttributeToWrapper.containsKey(newGroupWrapper.getTargetProvisioningGroup().getMatchingIdAttributeNameToValues().iterator().next())) {
          iterator.remove();
          // note: no need to remove from indexes...
        }
      }
    }
    if (duplicateTargetGroups > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "duplicateTargetGroups", duplicateTargetGroups);
    }
  }

  /**
   * 
   * @param targetEntities
   */
  public void mergeInNewTargetEntities(List<ProvisioningEntity> targetEntities) {
    
    if (GrouperUtil.length(targetEntities) == 0) {
      return;
    }
    
    int duplicateTargetEntities = 0;
    
    // lets try to merge these in, if they replace newer ones
    Set<ProvisioningEntityWrapper> newWrappers = new HashSet<>();
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      if (targetEntity.getProvisioningEntityWrapper() != null) {
        newWrappers.add(targetEntity.getProvisioningEntityWrapper());
      }
    }

    // index the new target entities by matching id, and pick the best one if multiple
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningEntityWrapper> targetMatchingAttributeToNewWrapper = new HashMap<>();
    for (ProvisioningEntity targetEntity : GrouperUtil.nonNull(targetEntities)) {
      if (targetEntity.getProvisioningEntityWrapper() == null) {
        continue;
      }
      if (GrouperUtil.length(targetEntity.getMatchingIdAttributeNameToValues()) > 0) {
        
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetEntity.getMatchingIdAttributeNameToValues().iterator().next();
        ProvisioningEntityWrapper previousNewTargetEntityWrapper = targetMatchingAttributeToNewWrapper.get(bestMatchingId);
        ProvisioningEntityWrapper currentNewTargetEntityWrapper = targetEntity.getProvisioningEntityWrapper();
        
        boolean useNew = shouldReplaceTargetProvisioningEntity(previousNewTargetEntityWrapper, currentNewTargetEntityWrapper);
        
        if (useNew) {
          targetMatchingAttributeToNewWrapper.put(
              bestMatchingId, targetEntity.getProvisioningEntityWrapper());
        }
      }
    }
    
    // loop through existing wrappers and see if the targets match, and pick the best one
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningEntityWrapper> foundMatchOfMatchingAttributeToWrapper = new HashMap<>();
    for (ProvisioningEntityWrapper existingEntityWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      if (existingEntityWrapper.getGrouperTargetEntity() != null && existingEntityWrapper.getTargetProvisioningEntity() != null) {
        if (GrouperUtil.length(existingEntityWrapper.getTargetProvisioningEntity().getMatchingIdAttributeNameToValues()) > 0) {
          ProvisioningUpdatableAttributeAndValue matchingId = existingEntityWrapper.getTargetProvisioningEntity().getMatchingIdAttributeNameToValues().iterator().next();
          if (targetMatchingAttributeToNewWrapper.containsKey(matchingId)) {
            foundMatchOfMatchingAttributeToWrapper.put(matchingId, existingEntityWrapper);
            ProvisioningEntityWrapper newWrapper = targetMatchingAttributeToNewWrapper.get(matchingId);
            boolean useNew = shouldReplaceTargetProvisioningEntity(existingEntityWrapper, newWrapper);
            if (useNew) {
              existingEntityWrapper.setTargetProvisioningEntity(newWrapper.getTargetProvisioningEntity());
              duplicateTargetEntities++;
            }
          }
        }
      }
    }

    // loop through wrappers and remove the new ones which arent needed
    Iterator<ProvisioningEntityWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().iterator();
    while (iterator.hasNext()) {
      ProvisioningEntityWrapper newEntityWrapper = iterator.next();
      // see if this is a new one
      if (newEntityWrapper.getTargetProvisioningEntity() != null && newEntityWrapper.getGrouperTargetEntity() == null 
          && GrouperUtil.length(newEntityWrapper.getTargetProvisioningEntity().getMatchingIdAttributeNameToValues()) > 0) {
        if (foundMatchOfMatchingAttributeToWrapper.containsKey(newEntityWrapper.getTargetProvisioningEntity().getMatchingIdAttributeNameToValues().iterator().next())) {
          iterator.remove();
          // note: no need to remove from indexes...
        }
      }
    }
    if (duplicateTargetEntities > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "duplicateTargetEntities", duplicateTargetEntities);
    }
  }

  public boolean shouldReplaceTargetProvisioningGroup(
      ProvisioningGroupWrapper previousNewTargetGroupWrapper,
      ProvisioningGroupWrapper currentNewTargetGroupWrapper) {
    boolean useNew = false;
    boolean isGroupAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
        == GrouperProvisioningBehaviorMembershipType.groupAttributes;
    String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();

    if (previousNewTargetGroupWrapper == null) {
      useNew = true;
    } else if (isGroupAttributes 
        && GrouperUtil.length(previousNewTargetGroupWrapper.getTargetProvisioningGroup().retrieveAttributeValueSetForMemberships())
        > GrouperUtil.length(currentNewTargetGroupWrapper.getTargetProvisioningGroup().retrieveAttributeValueSetForMemberships())) {
      // if the current one that is new has the most memberships, then keep it
      useNew = false;
    } else {
      useNew = true;
    }
    return useNew;
  }


  public boolean shouldReplaceTargetProvisioningEntity(
      ProvisioningEntityWrapper previousNewTargetEntityWrapper,
      ProvisioningEntityWrapper currentNewTargetEntityWrapper) {
    boolean useNew = false;
    boolean isEntityAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() 
        == GrouperProvisioningBehaviorMembershipType.entityAttributes;
    String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();

    if (previousNewTargetEntityWrapper == null) {
      useNew = true;
    } else if (isEntityAttributes 
        && GrouperUtil.length(previousNewTargetEntityWrapper.getTargetProvisioningEntity().retrieveAttributeValueSetForMemberships())
        > GrouperUtil.length(currentNewTargetEntityWrapper.getTargetProvisioningEntity().retrieveAttributeValueSetForMemberships())) {
      // if the current one that is new has the most memberships, then keep it
      useNew = false;
    } else {
      useNew = true;
    }
    return useNew;
  }

  /**
   * 
   * @param targetMemberships
   */
  public void mergeInNewTargetMemberships(List<ProvisioningMembership> targetMemberships) {
    
    if (GrouperUtil.length(targetMemberships) == 0) {
      return;
    }
    
    int duplicateTargetMemberships = 0;

    // lets try to merge these in, if they replace newer ones
    Set<ProvisioningMembershipWrapper> newWrappers = new HashSet<>();
    for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
      if (targetMembership.getProvisioningMembershipWrapper() != null) {
        newWrappers.add(targetMembership.getProvisioningMembershipWrapper());
      }
    }

    // index the new target Memberships by matching id, and pick the best one if multiple
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningMembershipWrapper> targetMatchingAttributeToNewWrapper = new HashMap<>();
    for (ProvisioningMembership targetMembership : GrouperUtil.nonNull(targetMemberships)) {
      if (targetMembership.getProvisioningMembershipWrapper() == null) {
        continue;
      }
      if (GrouperUtil.length(targetMembership.getMatchingIdAttributeNameToValues()) > 0) {
        
        ProvisioningUpdatableAttributeAndValue bestMatchingId = targetMembership.getMatchingIdAttributeNameToValues().iterator().next();
        
        targetMatchingAttributeToNewWrapper.put(
            bestMatchingId, targetMembership.getProvisioningMembershipWrapper());
      }
    }
    
    // loop through existing wrappers and see if the targets match, and pick the best one
    Map<ProvisioningUpdatableAttributeAndValue, ProvisioningMembershipWrapper> foundMatchOfMatchingAttributeToWrapper = new HashMap<>();
    for (ProvisioningMembershipWrapper existingMembershipWrapper : this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (existingMembershipWrapper.getGrouperTargetMembership() != null && existingMembershipWrapper.getTargetProvisioningMembership() != null) {
        if (GrouperUtil.length(existingMembershipWrapper.getTargetProvisioningMembership().getMatchingIdAttributeNameToValues()) > 0) {
          ProvisioningUpdatableAttributeAndValue matchingId = existingMembershipWrapper.getTargetProvisioningMembership().getMatchingIdAttributeNameToValues().iterator().next();
          if (targetMatchingAttributeToNewWrapper.containsKey(matchingId)) {
            foundMatchOfMatchingAttributeToWrapper.put(matchingId, existingMembershipWrapper);
            ProvisioningMembershipWrapper newWrapper = targetMatchingAttributeToNewWrapper.get(matchingId);
            existingMembershipWrapper.setTargetProvisioningMembership(newWrapper.getTargetProvisioningMembership());
          }
        }
      }
    }

    // loop through wrappers and remove the new ones which arent needed
    Iterator<ProvisioningMembershipWrapper> iterator = this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();
    while (iterator.hasNext()) {
      ProvisioningMembershipWrapper newMembershipWrapper = iterator.next();
      // see if this is a new one
      if (newMembershipWrapper.getTargetProvisioningMembership() != null && newMembershipWrapper.getGrouperTargetMembership() == null) {
        if (foundMatchOfMatchingAttributeToWrapper.containsKey(newMembershipWrapper.getTargetProvisioningMembership().getMatchingIdAttributeNameToValues().iterator().next())) {
          iterator.remove();
          // note: no need to remove from indexes...
        }
      }
    }
    if (duplicateTargetMemberships > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "duplicateTargetMemberships", duplicateTargetMemberships);
    }

  }

  /**
   * look through group wrappers and add matching IDs to the index and make sure everything is linked up
   * @param useTheseTargetProvisioningGroups or null to just use what is in the data model
   */
  public void indexMatchingIdGroups(List<ProvisioningGroup> useTheseTargetProvisioningGroups) {
  
    Map<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningGroup>> groupMatchingIdToTargetProvisioningGroupWrapper = new HashMap<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningGroup>>();
    
    if (useTheseTargetProvisioningGroups == null) {
      // lets index the target objects first
      for (ProvisioningGroupWrapper provisioningGroupWrapper : new ArrayList<ProvisioningGroupWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()))) {
  
        // its not there!
        if (provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
          continue;
        }
        
        ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
        
        // these are already matched
        if (provisioningGroupWrapper.getGrouperTargetGroup() != null && targetProvisioningGroup != null) {
          continue;
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(targetProvisioningGroup.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningGroup> targetProvisioningGroups = groupMatchingIdToTargetProvisioningGroupWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningGroups == null) {
            targetProvisioningGroups = new HashSet<ProvisioningGroup>();
            groupMatchingIdToTargetProvisioningGroupWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningGroups);
          }
          targetProvisioningGroups.add(targetProvisioningGroup);
        }
      }
    } else {
    
      for (ProvisioningGroup extraTargetProvsisioningGroup : useTheseTargetProvisioningGroups) {
        
        ProvisioningGroupWrapper targetProvisioningGroupWrapper = extraTargetProvsisioningGroup.getProvisioningGroupWrapper();
        
        // if its already matched, skip
        if (targetProvisioningGroupWrapper != null) {
          if (targetProvisioningGroupWrapper.getGrouperTargetGroup() != null) {
            continue;
          }
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(extraTargetProvsisioningGroup.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningGroup> targetProvisioningGroups = groupMatchingIdToTargetProvisioningGroupWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningGroups == null) {
            targetProvisioningGroups = new HashSet<ProvisioningGroup>();
            groupMatchingIdToTargetProvisioningGroupWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningGroups);
          }
          targetProvisioningGroups.add(extraTargetProvsisioningGroup);
        }
        
      }
    }
    
    // how many groups the target group matches
    Map<ProvisioningGroup, Integer> targetProvisioningGroupToMatchCount = new HashMap<>();
    Map<ProvisioningGroup, ProvisioningGroup> grouperTargetGroupMatchesTargetProvisioningGroup = new HashMap<>();
    Map<ProvisioningGroup, Set<ProvisioningGroup>> targetProvisioningGroupToSetOfTargetProvisioningGroups = new HashMap<>();
    Map<ProvisioningGroup, ProvisioningUpdatableAttributeAndValue> grouperTargetGroupToTargetId = new HashMap<>();
    
    int matchingIdToMultipleTargetProvisioningGroups = 0;
    int matchingIdToMultipleGrouperTargetGroups = 0;
    
    // lets do non deleted grouper objects first
    for (boolean deleted : new boolean[] {false, true}) {
      
      // lets do current value of all attributes first
      for (boolean currentValue : new boolean[] {true, false}) {

        // lets look in matching attributes in order
        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
          targetProvisioningGroupToMatchCount.clear();
          grouperTargetGroupMatchesTargetProvisioningGroup.clear();
          targetProvisioningGroupToSetOfTargetProvisioningGroups.clear();
          grouperTargetGroupToTargetId.clear();
          

          String matchingAttributeName = matchingAttribute.getName();
          
          // go through unmatched grouper objects and try to find a match
          LOOP_THROUGH_GROUPS: for (ProvisioningGroupWrapper provisioningGroupWrapper : new ArrayList<ProvisioningGroupWrapper>(
              GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()))) {

            if (provisioningGroupWrapper.getGrouperTargetGroup() == null) {
              continue;
            }
            
            ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
            
            // these are already matched
            if (provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
              continue;
            }

            // make sure we are doing the right deleted flag
            if (deleted != provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
              continue;
            }

            for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(grouperTargetGroup.getMatchingIdAttributeNameToValues())) {
              
              // are we looking at right current value or past value?
              if (currentValue != provisioningUpdatableAttributeAndValue.getCurrentValue().booleanValue()) {
                continue;
              }
              
              if (!StringUtils.equals(matchingAttributeName, provisioningUpdatableAttributeAndValue.getAttributeName())) {
                continue;
              }
              
              // if we had an error, dont match on next (less important) attribute
              if (grouperTargetGroup.getProvisioningGroupWrapper().getErrorCode() != null) {
                continue;
              }

              // already found one
              if (grouperTargetGroupMatchesTargetProvisioningGroup.containsKey(grouperTargetGroup)) {
                continue;
              }
              // ok we have the right one!
              Set<ProvisioningGroup> targetProvisioningGroups = groupMatchingIdToTargetProvisioningGroupWrapper.get(provisioningUpdatableAttributeAndValue);
              
              // couldnt find
              if (GrouperUtil.length(targetProvisioningGroups) == 0) {
                continue;
              }

              // remove invalids that have been matched
              Iterator<ProvisioningGroup> iterator = targetProvisioningGroups.iterator();
              while (iterator.hasNext()) {
                
                ProvisioningGroup targetProvisioningGroup = iterator.next();

                // this means it has been matched already
                if (targetProvisioningGroup.getProvisioningGroupWrapper() != null && targetProvisioningGroup.getProvisioningGroupWrapper().getGrouperTargetGroup() != null) {
                
                  iterator.remove();
                  
                }                  
                
              }

              if (GrouperUtil.length(targetProvisioningGroups) == 0) {
                continue;
              }

              if (GrouperUtil.length(targetProvisioningGroups) > 1) {
                // this is a validation problems
                provisioningGroupWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
                
                GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
                
                if (gcGrouperSyncGroup != null) {
                  gcGrouperSyncGroup.setErrorCode(GcGrouperSyncErrorCode.MAT);
                  gcGrouperSyncGroup.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + GrouperUtil.length(targetProvisioningGroups) + " target groups");
                  gcGrouperSyncGroup.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
                
                continue;
              }
              
              // its 1!!!!!!
              ProvisioningGroup targetProvisioningGroup = targetProvisioningGroups.iterator().next();
              grouperTargetGroupMatchesTargetProvisioningGroup.put(grouperTargetGroup, targetProvisioningGroup);
              targetProvisioningGroupToSetOfTargetProvisioningGroups.put(targetProvisioningGroup, targetProvisioningGroups);
              grouperTargetGroupToTargetId.put(grouperTargetGroup, provisioningUpdatableAttributeAndValue);
              
              if (targetProvisioningGroupToMatchCount.containsKey(targetProvisioningGroup)) {

                Integer count = targetProvisioningGroupToMatchCount.get(targetProvisioningGroup);
                count++;
                targetProvisioningGroupToMatchCount.put(targetProvisioningGroup, count);
                
                // this is a validation problems
                provisioningGroupWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
                
                GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
                
                if (gcGrouperSyncGroup != null) {
                  gcGrouperSyncGroup.setErrorCode(GcGrouperSyncErrorCode.MAT);
                  gcGrouperSyncGroup.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches multiple grouper groups");
                  gcGrouperSyncGroup.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
                matchingIdToMultipleTargetProvisioningGroups++;
                
                continue;
                
              }

              targetProvisioningGroupToMatchCount.put(targetProvisioningGroup, 1);
              continue LOOP_THROUGH_GROUPS;
            }
          }
          
          // loop through groups with matches
          for (ProvisioningGroup grouperTargetGroup : grouperTargetGroupMatchesTargetProvisioningGroup.keySet()) {
            
            ProvisioningGroup targetProvisioningGroup = grouperTargetGroupMatchesTargetProvisioningGroup.get(grouperTargetGroup);
            ProvisioningGroupWrapper targetProvisioningGroupWrapper = targetProvisioningGroup.getProvisioningGroupWrapper();
            ProvisioningGroupWrapper grouperTargetGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
            
            Set<ProvisioningGroup> targetProvisioningGroups = targetProvisioningGroupToSetOfTargetProvisioningGroups.get(targetProvisioningGroup);
            
            // dont match with someone else
            targetProvisioningGroups.remove(targetProvisioningGroup);

            // make sure there is only one group that matches to the target group
            Integer count = targetProvisioningGroupToMatchCount.get(targetProvisioningGroup);
            
            if (count > 1) {
              ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
              GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
              
              if (gcGrouperSyncGroup != null) {
                ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = grouperTargetGroupToTargetId.get(grouperTargetGroup);
                gcGrouperSyncGroup.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + count + " grouper groups");
              }
              
              matchingIdToMultipleGrouperTargetGroups++;
              continue;
              
            }
           
            grouperTargetGroupWrapper.setTargetProvisioningGroup(targetProvisioningGroup);
            if (targetProvisioningGroupWrapper != null) {
              grouperTargetGroupWrapper.setTargetNativeGroup(targetProvisioningGroupWrapper.getTargetNativeGroup());
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningGroupWrappers().remove(targetProvisioningGroupWrapper);
            }
          }          
          
        }
      }
            
      
    }
    
    int provisioningGroupWrappersWithNoMatchingId = 0;
    int provisioningGroupWrappersWithNoMatch = 0;
    int provisioningGroupWrappersWithMatch = 0;
    

    // go through unmatched grouper objects and try to find a match
    for (ProvisioningGroupWrapper provisioningGroupWrapper : new ArrayList<ProvisioningGroupWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()))) {

      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper.getTargetProvisioningGroup();

      if (grouperTargetGroup != null && targetProvisioningGroup != null) {
        provisioningGroupWrappersWithMatch++;
        continue;
      }
      
      if (grouperTargetGroup != null && targetProvisioningGroup == null && 
          provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
        continue;
      }
      
      if (grouperTargetGroup != null && targetProvisioningGroup == null && 
          !provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
        continue;
      }
      
      if (grouperTargetGroup == null) {
        provisioningGroupWrappersWithNoMatch++;
        continue;
      }

      if (GrouperUtil.length(grouperTargetGroup.getMatchingIdAttributeNameToValues()) == 0) {
        provisioningGroupWrappersWithNoMatchingId++;
        continue;
      }
      if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
        provisioningGroupWrappersWithNoMatch++;
      }
      continue;
    }
    
    if (provisioningGroupWrappersWithMatch > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersWithMatch"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersWithMatch", oldCount + provisioningGroupWrappersWithMatch);
    }
    if (provisioningGroupWrappersWithNoMatchingId > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersWithNoMatchingId"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersWithNoMatchingId", oldCount + provisioningGroupWrappersWithNoMatchingId);
    }
    if (provisioningGroupWrappersWithNoMatch > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningGroupWrappersWithNoMatch"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningGroupWrappersWithNoMatch", oldCount + provisioningGroupWrappersWithNoMatch);
    }
    if (matchingIdToMultipleGrouperTargetGroups > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleGrouperTargetGroups"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleGrouperTargetGroups", oldCount + matchingIdToMultipleGrouperTargetGroups);
    }
    if (matchingIdToMultipleTargetProvisioningGroups > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleTargetProvisioningGroups"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleTargetProvisioningGroups", oldCount + matchingIdToMultipleTargetProvisioningGroups);
    }
  }

  /**
   * look through membership wrappers and add matching IDs to the index and make sure everything is linked up
   * @param useTheseTargetProvisioningMemberships or null to just use what is in the data model
   */
  public void indexMatchingIdMemberships(List<ProvisioningMembership> useTheseTargetProvisioningMemberships) {
  
    /**
     * 
     *  4 membership wrappers 
     *    2 have grouper and target side and matching ids are populated
     *    2 have only the target side and matching ids
     *    
     *    the matching ids can be more than one plain object. It can be a set - Set(String(uuid), MultiKey(groupId, entityId))
     *  
     *    loop through membership wrappers and find ones that have grouper side and target side
     *    // for each one of them, get their matching id value objects ProvisioningUpdatableAttributeAndValue
     *    // and for each one add them to the set
     *    // e.g. you can have 1 or more ProvisioningUpdatableAttributeAndValue objects per wrapper 
     *      Set<ProvisioningUpdatableAttributeAndValue> 
     *      
     *    //  membershipWrappers.iterator use this to iterate and when we find a wrapper that has only the target side
     *    // look through their matching ids and if even one of them matches with the items in the set above, toss it out
     *    
     *     if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isSelectMembershipsForGroup()) {
        iterator.remove();
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().remove(provisioningMembershipWrapper.getGroupIdMemberId());
        filterNonRecalcActionsCapturedByRecalc++;
      }
     * 
     */
    
    Set<ProvisioningUpdatableAttributeAndValue> matchingIds = new HashSet<>();
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {
      
      
      if (provisioningMembershipWrapper.getGrouperProvisioningMembership() != null 
          && provisioningMembershipWrapper.getTargetProvisioningMembership() != null) {
        
        ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
        
        matchingIds.addAll(GrouperUtil.nonNull(targetProvisioningMembership.getMatchingIdAttributeNameToValues()));
        
      }
      
    }
    
    Iterator<ProvisioningMembershipWrapper> membershipWrappersIterator = this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().iterator();
    
    while (membershipWrappersIterator.hasNext()) {
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = membershipWrappersIterator.next();
      if (provisioningMembershipWrapper.getTargetProvisioningMembership() != null 
          && provisioningMembershipWrapper.getGrouperTargetMembership() == null) {
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(provisioningMembershipWrapper.getTargetProvisioningMembership().getMatchingIdAttributeNameToValues())) {
          if (matchingIds.contains(provisioningUpdatableAttributeAndValue)) {
            membershipWrappersIterator.remove();
          }
        }
        
      }
    }
    
    Map<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningMembership>> membershipMatchingIdToTargetProvisioningMembershipWrapper = new HashMap<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningMembership>>();
    
    if (useTheseTargetProvisioningMemberships == null) {
      // lets index the target objects first
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {
  
        // its not there!
        if (provisioningMembershipWrapper.getTargetProvisioningMembership() == null) {
          continue;
        }
        
        ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
        
        // these are already matched
        if (provisioningMembershipWrapper.getGrouperTargetMembership() != null && targetProvisioningMembership != null) {
          continue;
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(targetProvisioningMembership.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningMembership> targetProvisioningMemberships = membershipMatchingIdToTargetProvisioningMembershipWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningMemberships == null) {
            targetProvisioningMemberships = new HashSet<ProvisioningMembership>();
            membershipMatchingIdToTargetProvisioningMembershipWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningMemberships);
          }
          targetProvisioningMemberships.add(targetProvisioningMembership);
        }
      }
    } else {
    
      for (ProvisioningMembership extraTargetProvsisioningMembership : useTheseTargetProvisioningMemberships) {
        
        ProvisioningMembershipWrapper targetProvisioningMembershipWrapper = extraTargetProvsisioningMembership.getProvisioningMembershipWrapper();
        
        // if its already matched, skip
        if (targetProvisioningMembershipWrapper != null) {
          if (targetProvisioningMembershipWrapper.getGrouperTargetMembership() != null) {
            continue;
          }
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(extraTargetProvsisioningMembership.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningMembership> targetProvisioningMemberships = membershipMatchingIdToTargetProvisioningMembershipWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningMemberships == null) {
            targetProvisioningMemberships = new HashSet<ProvisioningMembership>();
            membershipMatchingIdToTargetProvisioningMembershipWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningMemberships);
          }
          targetProvisioningMemberships.add(extraTargetProvsisioningMembership);
        }
        
      }
    }
    
    // how many groups the target membership matches
    Map<ProvisioningMembership, Integer> targetProvisioningMembershipToMatchCount = new HashMap<>();
    Map<ProvisioningMembership, ProvisioningMembership> grouperTargetMembershipMatchesTargetProvisioningMembership = new HashMap<>();
    Map<ProvisioningMembership, Set<ProvisioningMembership>> targetProvisioningMembershipToSetOfTargetProvisioningMemberships = new HashMap<>();
    Map<ProvisioningMembership, ProvisioningUpdatableAttributeAndValue> grouperTargetMembershipToTargetId = new HashMap<>();
    
    int matchingIdToMultipleTargetProvisioningMemberships = 0;
    int matchingIdToMultipleGrouperTargetMemberships = 0;
    
    // lets do non deleted grouper objects first
    for (boolean deleted : new boolean[] {false, true}) {
      
      // lets do current value of all attributes first
      for (boolean currentValue : new boolean[] {true, false}) {
  
        targetProvisioningMembershipToMatchCount.clear();
        grouperTargetMembershipMatchesTargetProvisioningMembership.clear();
        targetProvisioningMembershipToSetOfTargetProvisioningMemberships.clear();
        grouperTargetMembershipToTargetId.clear();
        
        // lets look in matching attributes in order
        // first do all current values, then do all past values
        String matchingAttributeName = "id";
        
        // go through unmatched grouper objects and try to find a match
        LOOP_THROUGH_MEMBERSHIPS: for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
            GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {

          if (provisioningMembershipWrapper.getGrouperTargetMembership() == null) {
            continue;
          }
          
          ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
          
          // these are already matched
          if (provisioningMembershipWrapper.getTargetProvisioningMembership() != null) {
            continue;
          }

          // make sure we are doing the right deleted flag
          if (deleted != provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
            continue;
          }

          for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(grouperTargetMembership.getMatchingIdAttributeNameToValues())) {
            
            // are we looking at right current value or past value?
            if (currentValue != provisioningUpdatableAttributeAndValue.getCurrentValue().booleanValue()) {
              continue;
            }
            
            if (!StringUtils.equals(matchingAttributeName, provisioningUpdatableAttributeAndValue.getAttributeName())) {
              continue;
            }
            
            // if we had an error, dont match on next (less important) attribute
            if (grouperTargetMembership.getProvisioningMembershipWrapper().getErrorCode() != null) {
              continue;
            }

            // already found one
            if (grouperTargetMembershipMatchesTargetProvisioningMembership.containsKey(grouperTargetMembership)) {
              continue;
            }
            // ok we have the right one!
            Set<ProvisioningMembership> targetProvisioningMemberships = membershipMatchingIdToTargetProvisioningMembershipWrapper.get(provisioningUpdatableAttributeAndValue);
            
            // couldnt find
            if (GrouperUtil.length(targetProvisioningMemberships) == 0) {
              continue;
            }

            // remove invalids that have been matched
            Iterator<ProvisioningMembership> iterator = targetProvisioningMemberships.iterator();
            while (iterator.hasNext()) {
              
              ProvisioningMembership targetProvisioningMembership = iterator.next();

              // this means it has been matched already
              if (targetProvisioningMembership.getProvisioningMembershipWrapper() != null && targetProvisioningMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() != null) {
              
                iterator.remove();
                
              }                  
              
            }

            if (GrouperUtil.length(targetProvisioningMemberships) == 0) {
              continue;
            }

            if (GrouperUtil.length(targetProvisioningMemberships) > 1) {
              // this is a validation problems
              provisioningMembershipWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
              
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
              
              if (gcGrouperSyncMembership != null) {
                gcGrouperSyncMembership.setErrorCode(GcGrouperSyncErrorCode.MAT);
                gcGrouperSyncMembership.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + GrouperUtil.length(targetProvisioningMemberships) + " target memberships");
                gcGrouperSyncMembership.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
              }
              
              
              continue;
            }
            
            // its 1!!!!!!
            ProvisioningMembership targetProvisioningMembership = targetProvisioningMemberships.iterator().next();
            grouperTargetMembershipMatchesTargetProvisioningMembership.put(grouperTargetMembership, targetProvisioningMembership);
            targetProvisioningMembershipToSetOfTargetProvisioningMemberships.put(targetProvisioningMembership, targetProvisioningMemberships);
            grouperTargetMembershipToTargetId.put(grouperTargetMembership, provisioningUpdatableAttributeAndValue);
            
            if (targetProvisioningMembershipToMatchCount.containsKey(targetProvisioningMembership)) {

              Integer count = targetProvisioningMembershipToMatchCount.get(targetProvisioningMembership);
              count++;
              targetProvisioningMembershipToMatchCount.put(targetProvisioningMembership, count);
              
              // this is a validation problems
              provisioningMembershipWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
              
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
              
              if (gcGrouperSyncMembership != null) {
                gcGrouperSyncMembership.setErrorCode(GcGrouperSyncErrorCode.MAT);
                gcGrouperSyncMembership.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches multiple grouper memberships");
                gcGrouperSyncMembership.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
              }
              
              matchingIdToMultipleTargetProvisioningMemberships++;
              
              continue;
              
            }

            targetProvisioningMembershipToMatchCount.put(targetProvisioningMembership, 1);
            continue LOOP_THROUGH_MEMBERSHIPS;
          }
        }
        
        // loop through memberships with matches
        for (ProvisioningMembership grouperTargetMembership : grouperTargetMembershipMatchesTargetProvisioningMembership.keySet()) {
          
          ProvisioningMembership targetProvisioningMembership = grouperTargetMembershipMatchesTargetProvisioningMembership.get(grouperTargetMembership);
          ProvisioningMembershipWrapper targetProvisioningMembershipWrapper = targetProvisioningMembership.getProvisioningMembershipWrapper();
          ProvisioningMembershipWrapper grouperTargetMembershipWrapper = grouperTargetMembership.getProvisioningMembershipWrapper();
          
          Set<ProvisioningMembership> targetProvisioningMemberships = targetProvisioningMembershipToSetOfTargetProvisioningMemberships.get(targetProvisioningMembership);
          
          // dont match with someone else
          targetProvisioningMemberships.remove(targetProvisioningMembership);

          // make sure there is only one membership that matches to the target membership
          Integer count = targetProvisioningMembershipToMatchCount.get(targetProvisioningMembership);
          
          if (count > 1) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership.getProvisioningMembershipWrapper();
            GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
            
            if (gcGrouperSyncMembership != null) {
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = grouperTargetMembershipToTargetId.get(grouperTargetMembership);
              gcGrouperSyncMembership.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + count + " grouper memberships");
            }
            
            matchingIdToMultipleGrouperTargetMemberships++;
            continue;
            
          }
         
          grouperTargetMembershipWrapper.setTargetProvisioningMembership(targetProvisioningMembership);
          if (targetProvisioningMembershipWrapper != null) {
            grouperTargetMembershipWrapper.setTargetNativeMembership(targetProvisioningMembershipWrapper.getTargetNativeMembership());
            this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers().remove(targetProvisioningMembershipWrapper);
          }
        }          
      }
    }
    
    int provisioningMembershipWrappersWithNoMatchingId = 0;
    int provisioningMembershipWrappersWithNoMatch = 0;
    
  
    // go through unmatched grouper objects and try to find a matc
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {
  
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
  
      if (grouperTargetMembership != null && targetProvisioningMembership != null) {
        continue;
      }
      
      if (grouperTargetMembership != null && targetProvisioningMembership == null && 
          provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
        continue;
      }
      
      if (grouperTargetMembership != null && targetProvisioningMembership == null && 
          !provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed()) {
        continue;
      }
      
      if (grouperTargetMembership == null) {
        provisioningMembershipWrappersWithNoMatch++;
        continue;
      }
  
      if (GrouperUtil.length(grouperTargetMembership.getMatchingIdAttributeNameToValues()) == 0) {
        provisioningMembershipWrappersWithNoMatchingId++;
        continue;
      }
      provisioningMembershipWrappersWithNoMatch++;
      continue;
    }
    
    if (provisioningMembershipWrappersWithNoMatchingId > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningMembershipWrappersWithNoMatchingId"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningMembershipWrappersWithNoMatchingId", oldCount + provisioningMembershipWrappersWithNoMatchingId);
    }
    if (provisioningMembershipWrappersWithNoMatch > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningMembershipWrappersWithNoMatch"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningMembershipWrappersWithNoMatch", oldCount + provisioningMembershipWrappersWithNoMatch);
    }
    if (matchingIdToMultipleGrouperTargetMemberships > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleGrouperTargetMemberships"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleGrouperTargetMemberships", oldCount + matchingIdToMultipleGrouperTargetMemberships);
    }
    if (matchingIdToMultipleTargetProvisioningMemberships > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleTargetProvisioningMemberships"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleTargetProvisioningMemberships", oldCount + matchingIdToMultipleTargetProvisioningMemberships);
    }
  }


  /**
   * look through entity wrappers and add matching IDs to the index and make sure everything is linked up
   * @param useTheseTargetProvisioningEntities or null to just use what is in the data model
   */
  public void indexMatchingIdEntities(List<ProvisioningEntity> useTheseTargetProvisioningEntities) {
  
    Map<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningEntity>> entityMatchingIdToTargetProvisioningEntityWrapper = new HashMap<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningEntity>>();
    
    if (useTheseTargetProvisioningEntities == null) {
      // lets index the target objects first
      for (ProvisioningEntityWrapper provisioningEntityWrapper : new ArrayList<ProvisioningEntityWrapper>(
          GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()))) {
  
        // its not there!
        if (provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
          continue;
        }
        
        ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
        
        // these are already matched
        if (provisioningEntityWrapper.getGrouperTargetEntity() != null && targetProvisioningEntity != null) {
          continue;
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(targetProvisioningEntity.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningEntity> targetProvisioningEntities = entityMatchingIdToTargetProvisioningEntityWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningEntities == null) {
            targetProvisioningEntities = new HashSet<ProvisioningEntity>();
            entityMatchingIdToTargetProvisioningEntityWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningEntities);
          }
          targetProvisioningEntities.add(targetProvisioningEntity);
        }
      }
    } else {
    
      for (ProvisioningEntity extraTargetProvsisioningEntity : useTheseTargetProvisioningEntities) {
        
        ProvisioningEntityWrapper targetProvisioningEntityWrapper = extraTargetProvsisioningEntity.getProvisioningEntityWrapper();
        
        // if its already matched, skip
        if (targetProvisioningEntityWrapper != null) {
          if (targetProvisioningEntityWrapper.getGrouperTargetEntity() != null) {
            continue;
          }
        }
        
        for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(extraTargetProvsisioningEntity.getMatchingIdAttributeNameToValues())) {
          Set<ProvisioningEntity> targetProvisioningEntities = entityMatchingIdToTargetProvisioningEntityWrapper.get(provisioningUpdatableAttributeAndValue);
          if (targetProvisioningEntities == null) {
            targetProvisioningEntities = new HashSet<ProvisioningEntity>();
            entityMatchingIdToTargetProvisioningEntityWrapper.put(provisioningUpdatableAttributeAndValue, targetProvisioningEntities);
          }
          targetProvisioningEntities.add(extraTargetProvsisioningEntity);
        }
        
      }
    }
    
    // how many entities the target entity matches
    Map<ProvisioningEntity, Integer> targetProvisioningEntityToMatchCount = new HashMap<>();
    Map<ProvisioningEntity, ProvisioningEntity> grouperTargetEntityMatchesTargetProvisioningEntity = new HashMap<>();
    Map<ProvisioningEntity, Set<ProvisioningEntity>> targetProvisioningEntityToSetOfTargetProvisioningEntities = new HashMap<>();
    Map<ProvisioningEntity, ProvisioningUpdatableAttributeAndValue> grouperTargetEntityToTargetId = new HashMap<>();
    
    int matchingIdToMultipleTargetProvisioningEntities = 0;
    int matchingIdToMultipleGrouperTargetEntities = 0;
    
    // lets do non deleted grouper objects first
    for (boolean deleted : new boolean[] {false, true}) {
      
      // lets do current value of all attributes first
      for (boolean currentValue : new boolean[] {true, false}) {
  
        // lets look in matching attributes in order
        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
          
          targetProvisioningEntityToMatchCount.clear();
          grouperTargetEntityMatchesTargetProvisioningEntity.clear();
          targetProvisioningEntityToSetOfTargetProvisioningEntities.clear();
          grouperTargetEntityToTargetId.clear();
          

          String matchingAttributeName = matchingAttribute.getName();
          
          // go through unmatched grouper objects and try to find a match
          LOOP_THROUGH_ENTITIES: for (ProvisioningEntityWrapper provisioningEntityWrapper : new ArrayList<ProvisioningEntityWrapper>(
              GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()))) {
  
            if (provisioningEntityWrapper.getGrouperTargetEntity() == null) {
              continue;
            }
            
            ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
            
            // these are already matched
            if (provisioningEntityWrapper.getTargetProvisioningEntity() != null) {
              continue;
            }
  
            // make sure we are doing the right deleted flag
            if (deleted != provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
              continue;
            }
  
            for (ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue : GrouperUtil.nonNull(grouperTargetEntity.getMatchingIdAttributeNameToValues())) {
              
              // are we looking at right current value or past value?
              if (currentValue != provisioningUpdatableAttributeAndValue.getCurrentValue().booleanValue()) {
                continue;
              }
              
              if (!StringUtils.equals(matchingAttributeName, provisioningUpdatableAttributeAndValue.getAttributeName())) {
                continue;
              }
              
              // if we had an error, dont match on next (less important) attribute
              if (grouperTargetEntity.getProvisioningEntityWrapper().getErrorCode() != null) {
                continue;
              }
  
              // already found one
              if (grouperTargetEntityMatchesTargetProvisioningEntity.containsKey(grouperTargetEntity)) {
                continue;
              }
              // ok we have the right one!
              Set<ProvisioningEntity> targetProvisioningEntities = entityMatchingIdToTargetProvisioningEntityWrapper.get(provisioningUpdatableAttributeAndValue);
              
              // couldnt find
              if (GrouperUtil.length(targetProvisioningEntities) == 0) {
                continue;
              }
  
              // remove invalids that have been matched
              Iterator<ProvisioningEntity> iterator = targetProvisioningEntities.iterator();
              while (iterator.hasNext()) {
                
                ProvisioningEntity targetProvisioningEntity = iterator.next();
  
                // this means it has been matched already
                if (targetProvisioningEntity.getProvisioningEntityWrapper() != null && targetProvisioningEntity.getProvisioningEntityWrapper().getGrouperTargetEntity() != null) {
                
                  iterator.remove();
                  
                }                  
                
              }
  
              if (GrouperUtil.length(targetProvisioningEntities) == 0) {
                continue;
              }
  
              if (GrouperUtil.length(targetProvisioningEntities) > 1) {
                // this is a validation problems
                provisioningEntityWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
                
                GcGrouperSyncMember gcGrouperSyncEntity = provisioningEntityWrapper.getGcGrouperSyncMember();
                
                if (gcGrouperSyncEntity != null) {
                  gcGrouperSyncEntity.setErrorCode(GcGrouperSyncErrorCode.MAT);
                  gcGrouperSyncEntity.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + GrouperUtil.length(targetProvisioningEntities) + " target entities");
                  gcGrouperSyncEntity.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
                
                continue;
              }
              
              // its 1!!!!!!
              ProvisioningEntity targetProvisioningEntity = targetProvisioningEntities.iterator().next();
              grouperTargetEntityMatchesTargetProvisioningEntity.put(grouperTargetEntity, targetProvisioningEntity);
              targetProvisioningEntityToSetOfTargetProvisioningEntities.put(targetProvisioningEntity, targetProvisioningEntities);
              grouperTargetEntityToTargetId.put(grouperTargetEntity, provisioningUpdatableAttributeAndValue);
              
              if (targetProvisioningEntityToMatchCount.containsKey(targetProvisioningEntity)) {
  
                Integer count = targetProvisioningEntityToMatchCount.get(targetProvisioningEntity);
                count++;
                targetProvisioningEntityToMatchCount.put(targetProvisioningEntity, count);
                
                // this is a validation problems
                provisioningEntityWrapper.setErrorCode(GcGrouperSyncErrorCode.MAT);
                
                GcGrouperSyncMember gcGrouperSyncEntity = provisioningEntityWrapper.getGcGrouperSyncMember();
                
                if (gcGrouperSyncEntity != null) {
                  gcGrouperSyncEntity.setErrorCode(GcGrouperSyncErrorCode.MAT);
                  gcGrouperSyncEntity.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches multiple grouper entities");
                  gcGrouperSyncEntity.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
                }
                
                matchingIdToMultipleTargetProvisioningEntities++;
                
                continue;
                
              }
  
              targetProvisioningEntityToMatchCount.put(targetProvisioningEntity, 1);
              continue LOOP_THROUGH_ENTITIES;
            }
          }
          
          // loop through entities with matches
          for (ProvisioningEntity grouperTargetEntity : grouperTargetEntityMatchesTargetProvisioningEntity.keySet()) {
            
            ProvisioningEntity targetProvisioningEntity = grouperTargetEntityMatchesTargetProvisioningEntity.get(grouperTargetEntity);
            ProvisioningEntityWrapper targetProvisioningEntityWrapper = targetProvisioningEntity.getProvisioningEntityWrapper();
            ProvisioningEntityWrapper grouperTargetEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
            
            Set<ProvisioningEntity> targetProvisioningEntities = targetProvisioningEntityToSetOfTargetProvisioningEntities.get(targetProvisioningEntity);
            
            // dont match with someone else
            targetProvisioningEntities.remove(targetProvisioningEntity);
  
            // make sure there is only one entity that matches to the target entity
            Integer count = targetProvisioningEntityToMatchCount.get(targetProvisioningEntity);
            
            if (count > 1) {
              ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity.getProvisioningEntityWrapper();
              GcGrouperSyncMember gcGrouperSyncEntity = provisioningEntityWrapper.getGcGrouperSyncMember();
              
              if (gcGrouperSyncEntity != null) {
                ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = grouperTargetEntityToTargetId.get(grouperTargetEntity);
                gcGrouperSyncEntity.setErrorMessage("Matching ID " + provisioningUpdatableAttributeAndValue.toString() + " matches " + count + " grouper entities");
              }
              
              matchingIdToMultipleGrouperTargetEntities++;
              continue;
              
            }
           
            grouperTargetEntityWrapper.setTargetProvisioningEntity(targetProvisioningEntity);
            if (targetProvisioningEntityWrapper != null) {
              grouperTargetEntityWrapper.setTargetNativeEntity(targetProvisioningEntityWrapper.getTargetNativeEntity());
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningEntityWrappers().remove(targetProvisioningEntityWrapper);
            }
          }          
          
        }
      }
            
      
    }
    
    int provisioningEntityWrappersWithNoMatchingId = 0;
    int provisioningEntityWrappersWithNoMatch = 0;
    int provisioningEntityWrappersWithMatch = 0;
    
  
    // go through unmatched grouper objects and try to find a match
    for (ProvisioningEntityWrapper provisioningEntityWrapper : new ArrayList<ProvisioningEntityWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()))) {
  
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
  
      if (grouperTargetEntity != null && targetProvisioningEntity != null) {
        provisioningEntityWrappersWithMatch++;
        continue;
      }
      

      if (grouperTargetEntity != null && targetProvisioningEntity == null && 
          provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
        continue;
      }
      
      if (grouperTargetEntity != null && targetProvisioningEntity == null && 
          !provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
        continue;
      }
      
      if (grouperTargetEntity == null) {
        provisioningEntityWrappersWithNoMatch++;
        continue;
      }
  
      if (GrouperUtil.length(grouperTargetEntity.getMatchingIdAttributeNameToValues()) == 0) {
        provisioningEntityWrappersWithNoMatchingId++;
        continue;
      }
      if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
        provisioningEntityWrappersWithNoMatch++;
      }
      continue;
    }
    
    if (provisioningEntityWrappersWithMatch > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningEntityWrappersWithMatch"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntityWrappersWithMatch", oldCount + provisioningEntityWrappersWithMatch);
    }
    if (provisioningEntityWrappersWithNoMatchingId > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningEntityWrappersWithNoMatchingId"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntityWrappersWithNoMatchingId", oldCount + provisioningEntityWrappersWithNoMatchingId);
    }
    if (provisioningEntityWrappersWithNoMatch > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("provisioningEntityWrappersWithNoMatch"), 0);
      this.getGrouperProvisioner().getDebugMap().put("provisioningEntityWrappersWithNoMatch", oldCount + provisioningEntityWrappersWithNoMatch);
    }
    if (matchingIdToMultipleGrouperTargetEntities > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleGrouperTargetEntities"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleGrouperTargetEntities", oldCount + matchingIdToMultipleGrouperTargetEntities);
    }
    if (matchingIdToMultipleTargetProvisioningEntities > 0) {
      Integer oldCount = GrouperUtil.defaultIfNull((Integer)this.getGrouperProvisioner().getDebugMap().get("matchingIdToMultipleTargetProvisioningEntities"), 0);
      this.getGrouperProvisioner().getDebugMap().put("matchingIdToMultipleTargetProvisioningEntities", oldCount + matchingIdToMultipleTargetProvisioningEntities);
    }
  }


  public void mergeInMembershipValues(ProvisioningEntity existingTargetEntity, ProvisioningEntity targetEntity, String membershipAttributeName, Object defaultValue) {
    Set<?> values = targetEntity.retrieveAttributeValueSetForMemberships();
  
    // if the new part has nothing, continue
    if (GrouperUtil.length(values) == 0) {
      return;
    }
    
    // if the new part only has default, then ignore it
    if (GrouperUtil.length(values) == 0 && defaultValue != null && GrouperUtil.equals(defaultValue, values.iterator().next())) {
      return;
    }
    
    // if the old part only has default, and the new exists, remove the old default
    Set<?> membershipAttributeValueSet = existingTargetEntity.retrieveAttributeValueSetForMemberships();
    if (GrouperUtil.length(membershipAttributeValueSet) == 1
        && GrouperUtil.equals(defaultValue, values.iterator().next())) {
      membershipAttributeValueSet.remove(defaultValue);
    }
    
    for (Object membershipValue : GrouperUtil.nonNull(values)) {
      existingTargetEntity.addAttributeValueForMembership(membershipValue, null, false);
    }
  
  }

  
}
