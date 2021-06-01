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
    boolean hasGroupLinkGroupFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupToId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupToId3();
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasGroupLinkGroupFromId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId2()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupFromId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId3()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupToId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId2()));
    needsRefresh = needsRefresh || (hasGroupLinkGroupToId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId3()));
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
    boolean hasEntityLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberFromId2();
    boolean hasEntityLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberFromId3();
    boolean hasEntityLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberToId2();
    boolean hasEntityLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberToId3();
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasEntityLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId2()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId3()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId2()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberToId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId3()));
    return needsRefresh;
  
  }

  public void retrieveSubjectLink() {

    Set<GcGrouperSyncMember> gcGrouperSyncMembers = new HashSet<GcGrouperSyncMember>(); 
  
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers())) {
      gcGrouperSyncMembers.add(provisioningEntityWrapper.getGcGrouperSyncMember());
    }
    
    if (GrouperUtil.length(gcGrouperSyncMembers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String subjectLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId2();
    boolean hasSubjectLinkMemberFromId2 = !StringUtils.isBlank(subjectLinkMemberFromId2);
    
    String subjectLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId3();
    boolean hasSubjectLinkMemberFromId3 = !StringUtils.isBlank(subjectLinkMemberFromId3);
  
    String subjectLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId2();
    boolean hasSubjectLinkMemberToId2 = !StringUtils.isBlank(subjectLinkMemberToId2);
  
    String subjectLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId3();
    boolean hasSubjectLinkMemberToId3 = !StringUtils.isBlank(subjectLinkMemberToId3);
  
    if (!hasSubjectLinkMemberFromId2 && !hasSubjectLinkMemberFromId3 && !hasSubjectLinkMemberToId2 && !hasSubjectLinkMemberToId3) {
      return;
    }
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink = new ArrayList<GcGrouperSyncMember>();
    
    int refreshSubjectLinkIfLessThanAmount = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getRefreshSubjectLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncMembers) <= refreshSubjectLinkIfLessThanAmount) {
      gcGrouperSyncMembersToRefreshSubjectLink.addAll(gcGrouperSyncMembers);
    } else {
      for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
        boolean needsRefresh = false;
        needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId2()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId3()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId2()));
        needsRefresh = needsRefresh || (hasSubjectLinkMemberToId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId3()));
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
    this.grouperProvisioner.retrieveGrouperSyncDao().updateSubjectLink(gcGrouperSyncMembersToRefreshSubjectLink);
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
    String groupLinkGroupFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId2();
    GrouperProvisioningConfigurationAttribute groupLinkGroupFromId2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupFromId2Attribute();
    boolean hasGroupLinkGroupFromId2 = !StringUtils.isBlank(groupLinkGroupFromId2) || groupLinkGroupFromId2Attribute != null;
    
    String groupLinkGroupFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId3();
    GrouperProvisioningConfigurationAttribute groupLinkGroupFromId3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupFromId3Attribute();
    boolean hasGroupLinkGroupFromId3 = !StringUtils.isBlank(groupLinkGroupFromId3) || groupLinkGroupFromId3Attribute != null;
  
    String groupLinkGroupToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId2();
    GrouperProvisioningConfigurationAttribute groupLinkGroupToId2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupToId2Attribute();
    boolean hasGroupLinkGroupToId2 = !StringUtils.isBlank(groupLinkGroupToId2) || groupLinkGroupToId2Attribute != null;
  
    String groupLinkGroupToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId3();
    GrouperProvisioningConfigurationAttribute groupLinkGroupToId3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGroupLinkGroupToId3Attribute();
    boolean hasGroupLinkGroupToId3 = !StringUtils.isBlank(groupLinkGroupToId3) || groupLinkGroupToId3Attribute != null;
  
    if (!hasGroupLinkGroupFromId2 && !hasGroupLinkGroupFromId3 && !hasGroupLinkGroupToId2 && !hasGroupLinkGroupToId3) {
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
      
      if (hasGroupLinkGroupFromId2) {
        String groupFromId2Value = null;
        if (groupLinkGroupFromId2Attribute != null) {
          groupFromId2Value = targetGroup.retrieveFieldOrAttributeValueString(groupLinkGroupFromId2Attribute);
        } else {
          groupFromId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupFromId2Value, gcGrouperSyncGroup.getGroupFromId2())) {
          gcGrouperSyncGroup.setGroupFromId2(groupFromId2Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupFromId3Value = null;
        if (groupLinkGroupFromId3Attribute != null) {
          groupFromId3Value = targetGroup.retrieveFieldOrAttributeValueString(groupLinkGroupFromId3Attribute);
        } else {
          groupFromId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId3, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupFromId3Value, gcGrouperSyncGroup.getGroupFromId3())) {
          gcGrouperSyncGroup.setGroupFromId3(groupFromId3Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupToId2) {
        String groupToId2Value = null;
        if (groupLinkGroupToId2Attribute != null) {
          groupToId2Value = targetGroup.retrieveFieldOrAttributeValueString(groupLinkGroupToId2Attribute);
        } else {
          groupToId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupToId2Value, gcGrouperSyncGroup.getGroupToId2())) {
          gcGrouperSyncGroup.setGroupToId2(groupToId2Value);
          hasChange = true;
        }
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupToId3Value = null;
        if (groupLinkGroupToId3Attribute != null) {
          groupToId3Value = targetGroup.retrieveFieldOrAttributeValueString(groupLinkGroupToId3Attribute);
        } else {
          groupToId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId3, variableMap, true, false, true));
        }
        if (!StringUtils.equals(groupToId3Value, gcGrouperSyncGroup.getGroupToId3())) {
          gcGrouperSyncGroup.setGroupToId3(groupToId3Value);
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
      List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(changedGroups, false, false);
      
      if (GrouperUtil.length(grouperTargetGroups) > 0) {
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().setProvisioningGroups(grouperTargetGroups);
        
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);

        // index
        this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);

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

  /**
   * update entity link for these entities
   * @param gcGrouperSyncGroupsToRefreshGroupLink
   */
  public void updateEntityLink(Collection<ProvisioningEntityWrapper> provisioningEntityWrappers) {
  
    if (GrouperUtil.length(provisioningEntityWrappers) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String entityLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId2();
    GrouperProvisioningConfigurationAttribute entityLinkMemberFromId2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkMemberFromId2Attribute();
    boolean hasEntityLinkMemberFromId2 = !StringUtils.isBlank(entityLinkMemberFromId2) || entityLinkMemberFromId2Attribute != null;
    
    String entityLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId3();
    GrouperProvisioningConfigurationAttribute entityLinkMemberFromId3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkMemberFromId3Attribute();
    boolean hasEnityLinkMemberFromId3 = !StringUtils.isBlank(entityLinkMemberFromId3) || entityLinkMemberFromId3Attribute != null;
  
    String entityLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId2();
    GrouperProvisioningConfigurationAttribute entityLinkMemberToId2Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkMemberToId2Attribute();
    boolean hasEnityLinkMemberToId2 = !StringUtils.isBlank(entityLinkMemberToId2) || entityLinkMemberToId2Attribute != null;
  
    String entityLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId3();
    GrouperProvisioningConfigurationAttribute entityLinkMemberToId3Attribute = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getEntityLinkMemberToId3Attribute();
    boolean hasEnityLinkMemberToId3 = !StringUtils.isBlank(entityLinkMemberToId3) || entityLinkMemberToId3Attribute != null;
  
    if (!hasEntityLinkMemberFromId2 && !hasEnityLinkMemberFromId3 && !hasEnityLinkMemberToId2 && !hasEnityLinkMemberToId3) {
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
      
      if (hasEntityLinkMemberFromId2) {
        String entityFromId2Value = null;
        if (entityLinkMemberFromId2Attribute != null) {
          entityFromId2Value = targetEntity.retrieveFieldOrAttributeValueString(entityLinkMemberFromId2Attribute);
        } else {
          entityFromId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkMemberFromId2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityFromId2Value, gcGrouperSyncEntity.getMemberFromId2())) {
          gcGrouperSyncEntity.setMemberFromId2(entityFromId2Value);
          hasChange = true;
        }
      }
      
      if (hasEnityLinkMemberFromId3) {
        String entityFromId3Value = null;
        if (entityLinkMemberFromId3Attribute != null) {
          entityFromId3Value = targetEntity.retrieveFieldOrAttributeValueString(entityLinkMemberFromId3Attribute);
        } else {
          entityFromId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkMemberFromId3, variableMap, true, false, true));
        }

        if (!StringUtils.equals(entityFromId3Value, gcGrouperSyncEntity.getMemberFromId3())) {
          gcGrouperSyncEntity.setMemberFromId3(entityFromId3Value);
          hasChange = true;
        }
      }
      
      if (hasEnityLinkMemberToId2) {
        String entityToId2Value = null;
        if (entityLinkMemberToId2Attribute != null) {
          entityToId2Value = targetEntity.retrieveFieldOrAttributeValueString(entityLinkMemberToId2Attribute);
        } else {
          entityToId2Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkMemberToId2, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityToId2Value, gcGrouperSyncEntity.getMemberToId2())) {
          gcGrouperSyncEntity.setMemberToId2(entityToId2Value);
          hasChange = true;
        }
      }
      
      if (hasEnityLinkMemberToId3) {
        String entityToId3Value = null;
        if (entityLinkMemberToId3Attribute != null) {
          entityToId3Value = targetEntity.retrieveFieldOrAttributeValueString(entityLinkMemberToId3Attribute);
        } else {
          entityToId3Value = StringUtils.trimToNull(GrouperUtil.substituteExpressionLanguage(entityLinkMemberToId3, variableMap, true, false, true));
        }
        if (!StringUtils.equals(entityToId3Value, gcGrouperSyncEntity.getMemberToId3())) {
          gcGrouperSyncEntity.setMemberToId3(entityToId3Value);
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
      List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(changedEntities, false, false);
      
      if (GrouperUtil.length(grouperTargetEntities) > 0) {
        
        this.grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjectsChangedInLink().setProvisioningEntities(grouperTargetEntities);
        
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
    String subjectLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId2();
    boolean hasSubjectLinkMemberFromId2 = !StringUtils.isBlank(subjectLinkMemberFromId2);
    
    String subjectLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId3();
    boolean hasSubjectLinkMemberFromId3 = !StringUtils.isBlank(subjectLinkMemberFromId3);
  
    String subjectLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId2();
    boolean hasSubjectLinkMemberToId2 = !StringUtils.isBlank(subjectLinkMemberToId2);
  
    String subjectLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId3();
    boolean hasSubjectLinkMemberToId3 = !StringUtils.isBlank(subjectLinkMemberToId3);
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId2()));
    needsRefresh = needsRefresh || (hasSubjectLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId3()));
    needsRefresh = needsRefresh || (hasSubjectLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId2()));
    needsRefresh = needsRefresh || (hasSubjectLinkMemberToId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId3()));
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
    boolean hasEntityLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberFromId2();
    boolean hasEntityLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberFromId3();
    boolean hasEntityLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberToId2();
    boolean hasEntityLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasEntityLinkMemberToId3();

    if (!hasEntityLinkMemberFromId2 && !hasEntityLinkMemberFromId3 && !hasEntityLinkMemberToId2 && !hasEntityLinkMemberToId3) {
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
  
      if (hasEntityLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncEntity.getMemberFromId2())) {
        hasChange = true;
      }
      
      if (hasEntityLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncEntity.getMemberFromId3())) {
        hasChange = true;
      }
      
      if (hasEntityLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncEntity.getMemberToId2())) {
        hasChange = true;
      }
      
      if (hasEntityLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncEntity.getMemberToId3())) {
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
    boolean hasGroupLinkGroupFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupToId2 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId3 = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isHasGroupLinkGroupToId3();
  
    if (!hasGroupLinkGroupFromId2 && !hasGroupLinkGroupFromId3 && !hasGroupLinkGroupToId2 && !hasGroupLinkGroupToId3) {
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
      
      if (hasGroupLinkGroupFromId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId2())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupFromId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupFromId3())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupToId2 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId2())) {
        hasChange = true;
      }
      
      if (hasGroupLinkGroupFromId3 && StringUtils.isBlank(gcGrouperSyncGroup.getGroupToId3())) {
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
