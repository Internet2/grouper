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
   * look through group wrappers and add matching IDs to the index and make sure everything is linked up
   * @param useTheseTargetProvisioningGroups or null to just use what is in the data model
   */
  public void indexMatchingIdGroups(List<ProvisioningGroup> useTheseTargetProvisioningGroups) {
  
    Map<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningGroup>> groupMatchingIdToTargetProvisioningGroupWrapper = new HashMap<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningGroup>>();
    
    if (GrouperUtil.length(useTheseTargetProvisioningGroups) == 0) {
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

        targetProvisioningGroupToMatchCount.clear();
        grouperTargetGroupMatchesTargetProvisioningGroup.clear();
        targetProvisioningGroupToSetOfTargetProvisioningGroups.clear();
        grouperTargetGroupToTargetId.clear();
        
        // lets look in matching attributes in order
        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
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
            if (deleted != provisioningGroupWrapper.isDelete()) {
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
            }
            targetProvisioningGroup.setProvisioningGroupWrapper(grouperTargetGroupWrapper);
            if (targetProvisioningGroupWrapper != null) {
              targetProvisioningGroupWrapper.setTargetProvisioningGroup(null);
              targetProvisioningGroupWrapper.setTargetNativeGroup(null);
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
      
      if (grouperTargetGroup == null) {
        provisioningGroupWrappersWithNoMatch++;
        continue;
      }

      if (GrouperUtil.length(grouperTargetGroup.getMatchingIdAttributeNameToValues()) == 0) {
        provisioningGroupWrappersWithNoMatchingId++;
        continue;
      }
      if (provisioningGroupWrapper.isRecalcObject()) {
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
  
    Map<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningMembership>> membershipMatchingIdToTargetProvisioningMembershipWrapper = new HashMap<ProvisioningUpdatableAttributeAndValue, Set<ProvisioningMembership>>();
    
    if (GrouperUtil.length(useTheseTargetProvisioningMemberships) == 0) {
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
        LOOP_THROUGH_GROUPS: for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
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
          if (deleted != provisioningMembershipWrapper.isDelete()) {
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
            continue LOOP_THROUGH_GROUPS;
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
          }
          targetProvisioningMembership.setProvisioningMembershipWrapper(grouperTargetMembershipWrapper);
          if (targetProvisioningMembershipWrapper != null) {
            targetProvisioningMembershipWrapper.setTargetProvisioningMembership(null);
            targetProvisioningMembershipWrapper.setTargetNativeMembership(null);
          }
        }          
      }
    }
    
    int provisioningMembershipWrappersWithNoMatchingId = 0;
    int provisioningMembershipWrappersWithNoMatch = 0;
    
  
    // go through unmatched grouper objects and try to find a match
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : new ArrayList<ProvisioningMembershipWrapper>(
        GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()))) {
  
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper.getGrouperTargetMembership();
      ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper.getTargetProvisioningMembership();
  
      if (grouperTargetMembership != null && targetProvisioningMembership != null) {
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
    
    if (GrouperUtil.length(useTheseTargetProvisioningEntities) == 0) {
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
  
        targetProvisioningEntityToMatchCount.clear();
        grouperTargetEntityMatchesTargetProvisioningEntity.clear();
        targetProvisioningEntityToSetOfTargetProvisioningEntities.clear();
        grouperTargetEntityToTargetId.clear();
        
        // lets look in matching attributes in order
        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
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
            if (deleted != provisioningEntityWrapper.isDelete()) {
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
            }
            targetProvisioningEntity.setProvisioningEntityWrapper(grouperTargetEntityWrapper);
            if (targetProvisioningEntityWrapper != null) {
              targetProvisioningEntityWrapper.setTargetProvisioningEntity(null);
              targetProvisioningEntityWrapper.setTargetNativeEntity(null);
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
      
      if (grouperTargetEntity == null) {
        provisioningEntityWrappersWithNoMatch++;
        continue;
      }
  
      if (GrouperUtil.length(grouperTargetEntity.getMatchingIdAttributeNameToValues()) == 0) {
        provisioningEntityWrappersWithNoMatchingId++;
        continue;
      }
      if (provisioningEntityWrapper.isRecalcObject()) {
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


  
}
