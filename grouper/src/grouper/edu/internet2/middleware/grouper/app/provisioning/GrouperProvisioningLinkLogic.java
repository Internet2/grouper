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
    boolean hasGroupLinkGroupAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache0();
    boolean hasGroupLinkGroupAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache1();
    boolean hasGroupLinkGroupAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache2();
    boolean hasGroupLinkGroupAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache3();
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasGroupLinkGroupAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache0()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache1()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache2()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache3()));
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

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    boolean hasEntityLinkEntityAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache0();
    boolean hasEntityLinkEntityAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache1();
    boolean hasEntityLinkEntityAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache2();
    boolean hasEntityLinkEntityAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache3();
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasEntityLinkEntityAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache0()));
    needsRefresh = needsRefresh || (hasEntityLinkEntityAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache1()));
    needsRefresh = needsRefresh || (hasEntityLinkEntityAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache2()));
    needsRefresh = needsRefresh || (hasEntityLinkEntityAttributeValueCache3 && StringUtils.isBlank(gcGrouperSyncMember.getEntityAttributeValueCache3()));
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
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String subjectLinkEntityAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache0();
    boolean hasSubjectLinkEntityAttributeValueCache0 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache0);
    
    String subjectLinkEntityAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache1();
    boolean hasSubjectLinkEntityAttributeValueCache1 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache1);
  
    String subjectLinkEntityAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache2();
    boolean hasSubjectLinkEntityAttributeValueCache2 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache2);
  
    String subjectLinkEntityAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache3();
    boolean hasSubjectLinkEntityAttributeValueCache3 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache3);
  
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
   * @param gcGrouperSyncGroupsToRefreshGroupLink
   */
  public void updateGroupLink(Collection<ProvisioningGroupWrapper> provisioningGroupWrappers) {
  
    if (GrouperUtil.length(provisioningGroupWrappers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String groupLinkGroupAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupAttributeValueCache0();
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache0Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupAttributeValueCache0Attribute();
    boolean hasGroupLinkGroupAttributeValueCache0 = !StringUtils.isBlank(groupLinkGroupAttributeValueCache0) || groupLinkGroupAttributeValueCache0Attribute != null;
    
    String groupLinkGroupAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupAttributeValueCache1();
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache1Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupAttributeValueCache1Attribute();
    boolean hasGroupLinkGroupAttributeValueCache1 = !StringUtils.isBlank(groupLinkGroupAttributeValueCache1) || groupLinkGroupAttributeValueCache1Attribute != null;
  
    String groupLinkGroupAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupAttributeValueCache2();
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupAttributeValueCache2Attribute();
    boolean hasGroupLinkGroupAttributeValueCache2 = !StringUtils.isBlank(groupLinkGroupAttributeValueCache2) || groupLinkGroupAttributeValueCache2Attribute != null;
  
    String groupLinkGroupAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupAttributeValueCache3();
    GrouperProvisioningConfigurationAttribute groupLinkGroupAttributeValueCache3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupAttributeValueCache3Attribute();
    boolean hasGroupLinkGroupAttributeValueCache3 = !StringUtils.isBlank(groupLinkGroupAttributeValueCache3) || groupLinkGroupAttributeValueCache3Attribute != null;
  
    if (!hasGroupLinkGroupAttributeValueCache0 && !hasGroupLinkGroupAttributeValueCache1 && !hasGroupLinkGroupAttributeValueCache2 && !hasGroupLinkGroupAttributeValueCache3) {
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
      
      if (hasGroupLinkGroupAttributeValueCache0) {
        String groupAttributeValueCache0Value = null;
        if (groupLinkGroupAttributeValueCache0Attribute != null) {
          groupAttributeValueCache0Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache0Attribute);
        } else {
          groupAttributeValueCache0Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupAttributeValueCache0, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache0Value, gcGrouperSyncGroup.getGroupAttributeValueCache0())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache0(groupAttributeValueCache0Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupAttributeValueCache1) {
        String groupAttributeValueCache1Value = null;
        if (groupLinkGroupAttributeValueCache1Attribute != null) {
          groupAttributeValueCache1Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache1Attribute);
        } else {
          groupAttributeValueCache1Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupAttributeValueCache1, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache1Value, gcGrouperSyncGroup.getGroupAttributeValueCache1())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache1(groupAttributeValueCache1Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupAttributeValueCache2) {
        String groupAttributeValueCache2Value = null;
        if (groupLinkGroupAttributeValueCache2Attribute != null) {
          groupAttributeValueCache2Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache2Attribute);
        } else {
          groupAttributeValueCache2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupAttributeValueCache2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupAttributeValueCache2Value, gcGrouperSyncGroup.getGroupAttributeValueCache2())) {
          gcGrouperSyncGroup.setGroupAttributeValueCache2(groupAttributeValueCache2Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupAttributeValueCache1) {
        String groupAttributeValueCache3Value = null;
        if (groupLinkGroupAttributeValueCache3Attribute != null) {
          groupAttributeValueCache3Value = targetGroup.retrieveAttributeValueString(groupLinkGroupAttributeValueCache3Attribute);
        } else {
          groupAttributeValueCache3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupAttributeValueCache3, variableMap, true, false, true));
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
    String entityLinkEntityAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkEntityAttributeValueCache0();
    GrouperProvisioningConfigurationAttribute entityLinkEntityAttributeValueCache0Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkEntityAttributeValueCache0Attribute();
    boolean hasEntityLinkEntityAttributeValueCache0 = !StringUtils.isBlank(entityLinkEntityAttributeValueCache0) || entityLinkEntityAttributeValueCache0Attribute != null;
    
    String entityLinkEntityAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkEntityAttributeValueCache1();
    GrouperProvisioningConfigurationAttribute entityLinkEntityAttributeValueCache1Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkEntityAttributeValueCache1Attribute();
    boolean hasEntityLinkEntityAttributeValueCache1 = !StringUtils.isBlank(entityLinkEntityAttributeValueCache1) || entityLinkEntityAttributeValueCache1Attribute != null;
  
    String entityLinkEntityAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkEntityAttributeValueCache2();
    GrouperProvisioningConfigurationAttribute entityLinkEntityAttributeValueCache2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkEntityAttributeValueCache2Attribute();
    boolean hasEntityLinkEntityAttributeValueCache2 = !StringUtils.isBlank(entityLinkEntityAttributeValueCache2) || entityLinkEntityAttributeValueCache2Attribute != null;
  
    String entityLinkEntityAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkEntityAttributeValueCache3();
    GrouperProvisioningConfigurationAttribute entityLinkEntityAttributeValueCache3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkEntityAttributeValueCache3Attribute();
    boolean hasEntityLinkEntityAttributeValueCache3 = !StringUtils.isBlank(entityLinkEntityAttributeValueCache3) || entityLinkEntityAttributeValueCache3Attribute != null;
  
    if (!hasEntityLinkEntityAttributeValueCache0 && !hasEntityLinkEntityAttributeValueCache1 && !hasEntityLinkEntityAttributeValueCache2 && !hasEntityLinkEntityAttributeValueCache3) {
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
      
      if (hasEntityLinkEntityAttributeValueCache0) {
        String entityFromId2Value = null;
        if (entityLinkEntityAttributeValueCache0Attribute != null) {
          entityFromId2Value = targetEntity.retrieveAttributeValueString(entityLinkEntityAttributeValueCache0Attribute);
        } else {
          entityFromId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkEntityAttributeValueCache0, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityFromId2Value, gcGrouperSyncEntity.getEntityAttributeValueCache0())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache0(entityFromId2Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkEntityAttributeValueCache1) {
        String entityFromId3Value = null;
        if (entityLinkEntityAttributeValueCache1Attribute != null) {
          entityFromId3Value = targetEntity.retrieveAttributeValueString(entityLinkEntityAttributeValueCache1Attribute);
        } else {
          entityFromId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkEntityAttributeValueCache1, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityFromId3Value, gcGrouperSyncEntity.getEntityAttributeValueCache1())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache1(entityFromId3Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkEntityAttributeValueCache2) {
        String entityToId2Value = null;
        if (entityLinkEntityAttributeValueCache2Attribute != null) {
          entityToId2Value = targetEntity.retrieveAttributeValueString(entityLinkEntityAttributeValueCache2Attribute);
        } else {
          entityToId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkEntityAttributeValueCache2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityToId2Value, gcGrouperSyncEntity.getEntityAttributeValueCache2())) {
          gcGrouperSyncEntity.setEntityAttributeValueCache2(entityToId2Value);
          hasChange = true;
        }
      }
      
      if (hasEntityLinkEntityAttributeValueCache3) {
        String entityToId3Value = null;
        if (entityLinkEntityAttributeValueCache3Attribute != null) {
          entityToId3Value = targetEntity.retrieveAttributeValueString(entityLinkEntityAttributeValueCache3Attribute);
        } else {
          entityToId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkEntityAttributeValueCache3, variableMap, true, false, true));
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
    String subjectLinkEntityAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache0();
    boolean hasSubjectLinkEntityAttributeValueCache0 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache0);
    
    String subjectLinkEntityAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache1();
    boolean hasSubjectLinkEntityAttributeValueCache1 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache1);
  
    String subjectLinkEntityAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache2();
    boolean hasSubjectLinkEntityAttributeValueCache2 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache2);
  
    String subjectLinkEntityAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkEntityAttributeValueCache3();
    boolean hasSubjectLinkEntityAttributeValueCache3 = !StringUtils.isBlank(subjectLinkEntityAttributeValueCache3);
  
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

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    boolean hasEntityLinkEntityAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache0();
    boolean hasEntityLinkEntityAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache1();
    boolean hasEntityLinkEntityAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache2();
    boolean hasEntityLinkEntityAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkEntityAttributeValueCache3();

    if (!hasEntityLinkEntityAttributeValueCache0 && !hasEntityLinkEntityAttributeValueCache1 && !hasEntityLinkEntityAttributeValueCache2 && !hasEntityLinkEntityAttributeValueCache3) {
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
  
      if (hasEntityLinkEntityAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache0())) {
        hasChange = true;
      }
      
      if (hasEntityLinkEntityAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache1())) {
        hasChange = true;
      }
      
      if (hasEntityLinkEntityAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache2())) {
        hasChange = true;
      }
      
      if (hasEntityLinkEntityAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncEntity.getEntityAttributeValueCache3())) {
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
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    boolean hasGroupLinkGroupAttributeValueCache0 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache0();
    boolean hasGroupLinkGroupAttributeValueCache1 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache1();
    boolean hasGroupLinkGroupAttributeValueCache2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache2();
    boolean hasGroupLinkGroupAttributeValueCache3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupAttributeValueCache3();
  
    if (!hasGroupLinkGroupAttributeValueCache0 && !hasGroupLinkGroupAttributeValueCache1 && !hasGroupLinkGroupAttributeValueCache2 && !hasGroupLinkGroupAttributeValueCache3) {
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
      
      if (hasGroupLinkGroupAttributeValueCache0 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache0())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache1())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupAttributeValueCache2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache2())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupAttributeValueCache1 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupAttributeValueCache3())) {
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
