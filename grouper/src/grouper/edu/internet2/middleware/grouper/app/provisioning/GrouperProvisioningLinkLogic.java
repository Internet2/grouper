package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;

public class GrouperProvisioningLinkLogic {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  public GrouperProvisioningLinkLogic() {
  }

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

  /**
   * 
   * @param gcGrouperSyncGroup
   * @return
   */
  public boolean groupLinkMissing(GcGrouperSyncGroup gcGrouperSyncGroup) {
    
    if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasTargetGroupLink(), false)) {
      return false;
    }

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[0];
    boolean hasGroupLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[1];
    boolean hasGroupLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[2];
    boolean hasGroupLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[3];
    boolean hasGroupLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasGroupLinkAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache0()));
    needsRefresh = needsRefresh || (hasGroupLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache1()));
    needsRefresh = needsRefresh || (hasGroupLinkAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache2()));
    needsRefresh = needsRefresh || (hasGroupLinkAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache3()));
    return needsRefresh;
  
  }

  /**
   * 
   * @param gcGrouperSyncMember
   * @return
   */
  public boolean entityLinkMissing(GcGrouperSyncMember gcGrouperSyncMember) {
    
    if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasTargetEntityLink(), false)) {
      return false;
    }

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[0];
    boolean hasEntityLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[1];
    boolean hasEntityLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[2];
    boolean hasEntityLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[3];
    boolean hasEntityLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasEntityLinkAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache0()));
    needsRefresh = needsRefresh || (hasEntityLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache1()));
    needsRefresh = needsRefresh || (hasEntityLinkAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache2()));
    needsRefresh = needsRefresh || (hasEntityLinkAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache3()));
    return needsRefresh;
  
  }

  public void retrieveSubjectLink() {

    Set<GcGrouperSyncMember> gcGrouperSyncMembers = new HashSet<GcGrouperSyncMember>(); 
  
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      if (provisioningEntityWrapper.getGcGrouperSyncMember() != null) {
        gcGrouperSyncMembers.add(provisioningEntityWrapper.getGcGrouperSyncMember());
      }
    }
    
    if (GrouperUtil.length(gcGrouperSyncMembers) == 0) {
      return;
    }
    if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isEntityAttributeValueCacheHas()) {
      return;
    }
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[0];
    boolean hasSubjectLinkEntityAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript());
    
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[1];
    boolean hasSubjectLinkEntityAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[2];
    boolean hasSubjectLinkEntityAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[3];
    boolean hasSubjectLinkEntityAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript());

    if (!hasSubjectLinkEntityAttributeValueCache0 && !hasSubjectLinkEntityAttributeValueCache1 && !hasSubjectLinkEntityAttributeValueCache2 && !hasSubjectLinkEntityAttributeValueCache3) {
      return;
    }
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink = new ArrayList<GcGrouperSyncMember>();
    
    int refreshSubjectLinkIfLessThanAmount = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getRefreshSubjectLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncMembers) <= refreshSubjectLinkIfLessThanAmount) {
      gcGrouperSyncMembersToRefreshSubjectLink.addAll(gcGrouperSyncMembers);
    } else {
      for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
        boolean needsRefresh = false;
        needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache0()));
        needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache1()));
        needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache2()));
        needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache3()));
        if (needsRefresh) {
          gcGrouperSyncMembersToRefreshSubjectLink.add(gcGrouperSyncMember);
        }
      }
    }
    int subjectsNeedsRefreshDueToLink = GrouperUtil.length(gcGrouperSyncMembersToRefreshSubjectLink);
    this.grouperProvisioner.getDebugMap().put("subjectsNeedRefreshDueToLink", subjectsNeedsRefreshDueToLink);
    if (subjectsNeedsRefreshDueToLink == 0) {
      return;
    }
    this.grouperProvisioner.retrieveGrouperProvisioningSyncDao().updateSubjectLink(gcGrouperSyncMembersToRefreshSubjectLink);
  }

  /**
   * update group link for these groups
   * @param provisioningGroupWrappers
   */
  public void updateGroupLink(Collection<ProvisioningGroupWrapper> provisioningGroupWrappers) {
  
    if (GrouperUtil.length(provisioningGroupWrappers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isGroupAttributeValueCacheHas()) {
      return;
    }
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[0];
    boolean hasGroupLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache0Attribute = 
        grouperProvisioningConfigurationAttributeDbCache0 == null ? null : grouperProvisioningConfigurationAttributeDbCache0.retrieveAttribute();
    
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[1];
    boolean hasGroupLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null;
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache1Attribute = 
        grouperProvisioningConfigurationAttributeDbCache1 == null ? null : grouperProvisioningConfigurationAttributeDbCache1.retrieveAttribute();

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[2];
    boolean hasGroupLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null;
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache2Attribute = 
        grouperProvisioningConfigurationAttributeDbCache2 == null ? null : grouperProvisioningConfigurationAttributeDbCache2.retrieveAttribute();

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[3];
    boolean hasGroupLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null;
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache3Attribute = 
        grouperProvisioningConfigurationAttributeDbCache3 == null ? null : grouperProvisioningConfigurationAttributeDbCache3.retrieveAttribute();

    if (!hasGroupLinkAttributeValueCache0 && !hasGroupLinkAttributeValueCache1 && !hasGroupLinkAttributeValueCache2 && !hasGroupLinkAttributeValueCache3) {
      return;
    }

    int groupsCannotFindLinkData = 0;
  
    int groupsCannotFindSyncGroup = 0;

    int targetGroupsForLinkNull = 0;

    int changeCount = 0;
    
    List<ProvisioningGroup> changedGroups = new ArrayList<ProvisioningGroup>();
    
    for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
  
      boolean hasChange = false;
      
      ProvisioningGroup targetGroup = provisioningGroupWrapper.getTargetProvisioningGroup();
      
      // not sure why this would happen... deleted?
      if (targetGroup == null) {
        targetGroupsForLinkNull++;
        continue;
      }

      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      if (gcGrouperSyncGroup == null) {
        groupsCannotFindSyncGroup++;
        continue;
      }
  
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetGroup", targetGroup);
      
      if (hasGroupLinkAttributeValueCache0) {
        String groupAttributeValueCache0Value = null;
        if (groupLinkGroupAttributeValueCache0Attribute != null) {
          groupAttributeValueCache0Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache0Attribute);
        } else {
          groupAttributeValueCache0Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache0Value, gcGrouperSyncGroup.getGroupAttributeValueCache0())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache0(groupAttributeValueCache0Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkAttributeValueCache1) {
        String groupAttributeValueCache1Value = null;
        if (groupLinkGroupAttributeValueCache1Attribute != null) {
          groupAttributeValueCache1Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache1Attribute);
        } else {
          groupAttributeValueCache1Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache1Value, gcGrouperSyncGroup.getGroupAttributeValueCache1())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache1(groupAttributeValueCache1Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkAttributeValueCache2) {
        String groupAttributeValueCache2Value = null;
        if (groupLinkGroupAttributeValueCache2Attribute != null) {
          groupAttributeValueCache2Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache2Attribute);
        } else {
          groupAttributeValueCache2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache2Value, gcGrouperSyncGroup.getGroupAttributeValueCache2())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache2(groupAttributeValueCache2Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkAttributeValueCache1) {
        String groupAttributeValueCache3Value = null;
        if (groupLinkGroupAttributeValueCache3Attribute != null) {
          groupAttributeValueCache3Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache3Attribute);
        } else {
          groupAttributeValueCache3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache3Value, gcGrouperSyncGroup.getGroupAttributeValueCache3())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache3(groupAttributeValueCache3Value);
          hasChange = true;
        }
      }
      
      if (hasChange) {
        changeCount++;
        if (provisioningGroupWrapper.getGrouperProvisioningGroup() != null) {
          changedGroups.add(provisioningGroupWrapper.getGrouperProvisioningGroup());
        }
      }
    }

    if (changedGroups.size() > 0) {
      // these need to be translated and indexed
      List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetGroups(changedGroups, false, false);
      
      if (GrouperUtil.length(grouperTargetGroups) > 0) {
        
        translateAndManipulateMembershipsForGroupsEntitiesCreate();
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().setProvisioningGroups(grouperTargetGroups);
        
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);

        // index
        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetGroups(grouperTargetGroups);

        this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();
        
        for (ProvisioningGroup grouperTargetGroup : grouperTargetGroups) {
          grouperTargetGroup.getProvisioningGroupWrapper().setGrouperTargetGroup(grouperTargetGroup);
        }
      }
      
    }
    
    if (changeCount > 0) {
      this.grouperProvisioner.getDebugMap().put("linkGcSyncGroupsUpdated", changeCount);
    }
    if (targetGroupsForLinkNull > 0) {
      this.grouperProvisioner.getDebugMap().put("targetGroupsForLinkNull", targetGroupsForLinkNull);
    }
    if (groupsCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("groupsCannotFindLinkData", groupsCannotFindLinkData);
    }
    if (groupsCannotFindSyncGroup > 0) {
      this.grouperProvisioner.getDebugMap().put("groupsCannotFindSyncGroup", groupsCannotFindSyncGroup);
    }
    
    
  }

  public void updateGroupLinkFull() {
    updateGroupLink(GrouperUtil.nonNull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()));
  }

  public void updateEntityLinkFull() {
    updateEntityLink(GrouperUtil.nonNull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()));
  }
  
  
  
  private void translateAndManipulateMembershipsForGroupsEntitiesCreate() {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isCreateGroupsAndEntitiesBeforeTranslatingMemberships()) {
      
      Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
      
      try {
        debugMap.put("state", "translateGrouperMembershipsToTarget");
        {
          List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>(this.getGrouperProvisioner().
              retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships(true));
          
          List<ProvisioningMembership> grouperTargetMemberships = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetMemberships(
              grouperProvisioningMemberships, false);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
        }    

      } finally {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget);
      }

      try {
        debugMap.put("state", "manipulateGrouperMembershipTargetAttributes");
        List<ProvisioningMembership> grouperTargetMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForMemberships(grouperTargetMemberships);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(grouperTargetMemberships, true, false, false);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(grouperTargetMemberships);
    
      } finally {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.manipulateGrouperTargetMembershipsAttributes);
      }

      try {
        debugMap.put("state", "matchingIdGrouperMemberships");
        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetMemberships(true));
      } finally {
        this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.matchingIdGrouperMemberships);
      }

      // index the memberships
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdMemberships();

    }
    
  }

  /**
   * update entity link for these entities
   * @param provisioningEntityWrappers
   */
  public void updateEntityLink(Collection<ProvisioningEntityWrapper> provisioningEntityWrappers) {
  
    if (GrouperUtil.length(provisioningEntityWrappers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    
    if (!this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isEntityAttributeValueCacheHas()) {
      return;
    }
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[0];
    boolean hasEntityLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;
    GrouperProvisioningConfigurationAttribute entityLinkGroupAttributeValueCache0Attribute = 
        grouperProvisioningConfigurationAttributeDbCache0 == null ? null : grouperProvisioningConfigurationAttributeDbCache0.retrieveAttribute();

    
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[1];
    boolean hasEntityLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;
    GrouperProvisioningConfigurationAttribute entityLinkGroupAttributeValueCache1Attribute = 
        grouperProvisioningConfigurationAttributeDbCache1 == null ? null : grouperProvisioningConfigurationAttributeDbCache1.retrieveAttribute();

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[2];
    boolean hasEntityLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;
    GrouperProvisioningConfigurationAttribute entityLinkGroupAttributeValueCache2Attribute = 
        grouperProvisioningConfigurationAttributeDbCache2 == null ? null : grouperProvisioningConfigurationAttributeDbCache2.retrieveAttribute();

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[3];
    boolean hasEntityLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;
    GrouperProvisioningConfigurationAttribute entityLinkGroupAttributeValueCache3Attribute = 
        grouperProvisioningConfigurationAttributeDbCache3 == null ? null : grouperProvisioningConfigurationAttributeDbCache3.retrieveAttribute();

    if (!hasEntityLinkAttributeValueCache0 && !hasEntityLinkAttributeValueCache1 && !hasEntityLinkAttributeValueCache2 && !hasEntityLinkAttributeValueCache3) {
      return;
    }

    int entitiesCannotFindLinkData = 0;
  
    int entitiesCannotFindSyncMember = 0;
  
    int targetEntitiesForLinkNull = 0;
    
    int changeCount = 0;
    
    List<ProvisioningEntity> changedEntities = new ArrayList<ProvisioningEntity>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {
  
      boolean hasChange = false;
      ProvisioningEntity targetEntity = provisioningEntityWrapper.getTargetProvisioningEntity();
      
      if (targetEntity == null) {
        targetEntitiesForLinkNull++;
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncEntity = targetEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      
      if (gcGrouperSyncEntity == null) {
        entitiesCannotFindSyncMember++;
        continue;
      }
  
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetEntity", targetEntity);
      
      if (hasEntityLinkAttributeValueCache0) {
        String entityFromId2Value = null;
        if (entityLinkGroupAttributeValueCache0Attribute != null) {
          entityFromId2Value = targetEntity.retrieveAttributeValueString(entityLinkGroupAttributeValueCache0Attribute);
        } else {
          entityFromId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityFromId2Value, gcGrouperSyncEntity.getEntityAttributeValueCache0())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache0(entityFromId2Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkAttributeValueCache1) {
        String entityFromId3Value = null;
        if (entityLinkGroupAttributeValueCache1Attribute != null) {
          entityFromId3Value = targetEntity.retrieveAttributeValueString(entityLinkGroupAttributeValueCache1Attribute);
        } else {
          entityFromId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityFromId3Value, gcGrouperSyncEntity.getEntityAttributeValueCache1())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache1(entityFromId3Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkAttributeValueCache2) {
        String entityToId2Value = null;
        if (entityLinkGroupAttributeValueCache2Attribute != null) {
          entityToId2Value = targetEntity.retrieveAttributeValueString(entityLinkGroupAttributeValueCache2Attribute);
        } else {
          entityToId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityToId2Value, gcGrouperSyncEntity.getEntityAttributeValueCache2())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache2(entityToId2Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkAttributeValueCache3) {
        String entityToId3Value = null;
        if (entityLinkGroupAttributeValueCache3Attribute != null) {
          entityToId3Value = targetEntity.retrieveAttributeValueString(entityLinkGroupAttributeValueCache3Attribute);
        } else {
          entityToId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript(), variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityToId3Value, gcGrouperSyncEntity.getEntityAttributeValueCache3())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache3(entityToId3Value);
          hasChange = true;
        }
      }
      if (hasChange) {
        changeCount++;
        if (provisioningEntityWrapper.getGrouperProvisioningEntity() != null) {
          changedEntities.add(provisioningEntityWrapper.getGrouperProvisioningEntity());
        }
      }
 
    }
    if (changedEntities.size() > 0) {
      // these need to be translated and indexed
      List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperProvisioningTranslator().translateGrouperToTargetEntities(changedEntities, false, false);
      
      if (GrouperUtil.length(grouperTargetEntities) > 0) {
        
        translateAndManipulateMembershipsForGroupsEntitiesCreate();
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().setProvisioningEntities(grouperTargetEntities);
        
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
        // index
        this.grouperProvisioner.retrieveGrouperProvisioningTranslator().idTargetEntities(grouperTargetEntities);
        this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();
        
        for (ProvisioningEntity grouperTargetEntity : grouperTargetEntities) {
          grouperTargetEntity.getProvisioningEntityWrapper().setGrouperTargetEntity(grouperTargetEntity);
        }
      }
      
    }
    
    if (changeCount > 0) {
      this.grouperProvisioner.getDebugMap().put("linkGcSyncEntitiesUpdated", changeCount);
    }
    if (entitiesCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("entitiesCannotFindLinkData", entitiesCannotFindLinkData);
    }
    if (entitiesCannotFindSyncMember > 0) {
      this.grouperProvisioner.getDebugMap().put("entitiesCannotFindSyncMember", entitiesCannotFindSyncMember);
    }
    if (targetEntitiesForLinkNull > 0) {
      this.grouperProvisioner.getDebugMap().put("targetEntitiesForLinkNull", targetEntitiesForLinkNull);
    }
    
  }

  /**
   * 
   * @param gcGrouperSyncMember
   * @return
   */
  public boolean subjectLinkMissing(GcGrouperSyncMember gcGrouperSyncMember) {
    
    if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasSubjectLink(), false)) {
      return false;
    }
  
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[0];
    boolean hasSubjectLinkEntityAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript());
    
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[1];
    boolean hasSubjectLinkEntityAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[2];
    boolean hasSubjectLinkEntityAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationEntityAttributeDbCaches()[3];
    boolean hasSubjectLinkEntityAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript());
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache0()));
    needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache1()));
    needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache2()));
    needsRefresh = needsRefresh || (hasSubjectLinkEntityAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache3()));
    return needsRefresh;
  
  }

  /**
   * see which entities needs to be retrieve in incremental logic if non recalc, and needs link data
   * @param provisioningEntityWrappers
   * @return
   */
  public List<ProvisioningEntity> retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks(
      Set<ProvisioningEntityWrapper> provisioningEntityWrappers) {
    
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();
    
    if (GrouperUtil.length(provisioningEntityWrappers) == 0) {
      return grouperTargetEntities;
    }
    
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isHasTargetEntityLink(), false)) {
      return grouperTargetEntities;
    }
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[0];
    boolean hasEntityLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[1];
    boolean hasEntityLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[2];
    boolean hasEntityLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[3];
    boolean hasEntityLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    if (!hasEntityLinkAttributeValueCache0 && !hasEntityLinkAttributeValueCache1 && !hasEntityLinkAttributeValueCache2 && !hasEntityLinkAttributeValueCache3) {
      return grouperTargetEntities;
    }
  
    int retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks = 0;
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {

      if (provisioningEntityWrapper.isRecalc()) {
        continue;
      }

      boolean hasChange = false;
  
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      if (grouperTargetEntity == null) {
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncEntity = provisioningEntityWrapper.getGcGrouperSyncMember();

      if (gcGrouperSyncEntity == null) {
        continue;
      }
  
      if (hasEntityLinkAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache0())) {
        hasChange = true;
      }
      
      if (hasEntityLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache1())) {
        hasChange = true;
      }
      
      if (hasEntityLinkAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache2())) {
        hasChange = true;
      }
      
      if (hasEntityLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache3())) {
        hasChange = true;
      }
      if (hasChange) {
        grouperTargetEntities.add(grouperTargetEntity);
        retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks++;
      }
  
    }
    if (retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks > 0) {
      this.getGrouperProvisioner().getDebugMap().put("retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks", retrieveIncrementalNonRecalcTargetEntitiesThatNeedLinks);
    }
    return grouperTargetEntities;

  }

  public List<ProvisioningGroup> retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks(
      Set<ProvisioningGroupWrapper> provisioningGroupWrappers) {
    
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();

    if (GrouperUtil.length(provisioningGroupWrappers) == 0) {
      return grouperTargetGroups;
    }
    
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isHasTargetGroupLink(), false)) {
      return grouperTargetGroups;
    }
    
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[0];
    boolean hasGroupLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[1];
    boolean hasGroupLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[2];
    boolean hasGroupLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningConfigurationGroupAttributeDbCaches()[3];
    boolean hasGroupLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target;

    if (!hasGroupLinkAttributeValueCache0 && !hasGroupLinkAttributeValueCache1 && !hasGroupLinkAttributeValueCache2 && !hasGroupLinkAttributeValueCache3) {
      return grouperTargetGroups;
    }
  
    int retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks = 0;
  
    for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
  
      if (provisioningGroupWrapper.isRecalc()) {
        continue;
      }

      boolean hasChange = false;
  
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      if (grouperTargetGroup == null) {
        continue;
      }
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();

      if (gcGrouperSyncGroup == null) {
        continue;
      }
      
      if (hasGroupLinkAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache0())) {
        hasChange = true;
      }
      
      if (hasGroupLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache1())) {
        hasChange = true;
      }
      
      if (hasGroupLinkAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache2())) {
        hasChange = true;
      }
      
      if (hasGroupLinkAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache3())) {
        hasChange = true;
      }
      
      if (hasChange) {
        grouperTargetGroups.add(grouperTargetGroup);
        retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks++;

      }
    }
    if (retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks > 0) {
      this.getGrouperProvisioner().getDebugMap().put("retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks", retrieveIncrementalNonRecalcTargetGroupsThatNeedLinks);
    }
    return grouperTargetGroups;
  }

}
