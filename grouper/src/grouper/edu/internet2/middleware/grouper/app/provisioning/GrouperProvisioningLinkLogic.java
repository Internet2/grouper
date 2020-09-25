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
import edu.internet2.middleware.grouperClient.collections.MultiKey;
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
    
    if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getHasTargetGroupLink(), false)) {
      return false;
    }

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String groupLinkGroupFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId2 = !StringUtils.isBlank(groupLinkGroupFromId2);
    
    String groupLinkGroupFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupFromId3 = !StringUtils.isBlank(groupLinkGroupFromId3);
  
    String groupLinkGroupToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId2 = !StringUtils.isBlank(groupLinkGroupToId2);
  
    String groupLinkGroupToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId3();
    boolean hasGroupLinkGroupToId3 = !StringUtils.isBlank(groupLinkGroupToId3);
  
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
    
    if (GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getHasTargetEntityLink(), false)) {
      return false;
    }

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String entityLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId2();
    boolean hasEntityLinkMemberFromId2 = !StringUtils.isBlank(entityLinkMemberFromId2);
    
    String entityLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId3();
    boolean hasEntityLinkMemberFromId3 = !StringUtils.isBlank(entityLinkMemberFromId3);
  
    String entityLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId2();
    boolean hasEntityLinkMemberToId2 = !StringUtils.isBlank(entityLinkMemberToId2);
  
    String entityLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId3();
    boolean hasEntityLinkMemberToId3 = !StringUtils.isBlank(entityLinkMemberToId3);
  
    boolean needsRefresh = false;
    needsRefresh = needsRefresh || (hasEntityLinkMemberFromId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId2()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberFromId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberFromId3()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberToId2 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId2()));
    needsRefresh = needsRefresh || (hasEntityLinkMemberToId3 && StringUtils.isBlank(gcGrouperSyncMember.getMemberToId3()));
    return needsRefresh;
  
  }

  public void retrieveSubjectLink() {
  
    Collection<GcGrouperSyncMember> gcGrouperSyncMembers = GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember()).values();
  
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

  public void retrieveTargetEntityLink() {
    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().updateEntityLink(this.grouperProvisioner);
  }

  /**
   */
  public void retrieveTargetGroupLink() {
    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().updateGroupLink(this.grouperProvisioner);
  }

  /**
   * update group link for these groups
   * @param gcGrouperSyncGroupsToRefreshGroupLink
   */
  public void updateGroupLink(List<ProvisioningGroup> targetGroups) {
  
    if (GrouperUtil.length(targetGroups) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String groupLinkGroupFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId2();
    boolean hasGroupLinkGroupFromId2 = !StringUtils.isBlank(groupLinkGroupFromId2);
    
    String groupLinkGroupFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupFromId3();
    boolean hasGroupLinkGroupFromId3 = !StringUtils.isBlank(groupLinkGroupFromId3);
  
    String groupLinkGroupToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId2();
    boolean hasGroupLinkGroupToId2 = !StringUtils.isBlank(groupLinkGroupToId2);
  
    String groupLinkGroupToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupLinkGroupToId3();
    boolean hasGroupLinkGroupToId3 = !StringUtils.isBlank(groupLinkGroupToId3);
  
    if (!hasGroupLinkGroupFromId2 && !hasGroupLinkGroupFromId3 && !hasGroupLinkGroupToId2 && !hasGroupLinkGroupToId3) {
      return;
    }
  
    int groupsCannotFindLinkData = 0;
  
    int groupsCannotFindSyncGroup = 0;
  
    for (ProvisioningGroup targetGroup : targetGroups) {
  
      GcGrouperSyncGroup gcGrouperSyncGroup = targetGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      
      if (gcGrouperSyncGroup == null) {
        groupsCannotFindSyncGroup++;
        continue;
      }
  
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetGroup", targetGroup);
      
      if (hasGroupLinkGroupFromId2) {
        String groupFromId2Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId2, variableMap);
        gcGrouperSyncGroup.setGroupFromId2(groupFromId2Value);
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupFromId3Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupFromId3, variableMap);
        gcGrouperSyncGroup.setGroupFromId3(groupFromId3Value);
      }
      
      if (hasGroupLinkGroupToId2) {
        String groupToId2Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId2, variableMap);
        gcGrouperSyncGroup.setGroupToId2(groupToId2Value);
      }
      
      if (hasGroupLinkGroupFromId3) {
        String groupToId3Value = GrouperUtil.substituteExpressionLanguage(groupLinkGroupToId3, variableMap);
        gcGrouperSyncGroup.setGroupToId3(groupToId3Value);
      }
      
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
        this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningGroups()));
  }

  public void updateGroupLinkIncremental() {
    // If using target group link and the ID is not in the group sync cache object, then resolve the target group, and put the id in the group sync object
    Collection<GcGrouperSyncGroup> gcGrouperSyncGroups = GrouperUtil.nonNull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getGroupUuidToSyncGroup()).values();
  
    if (GrouperUtil.length(gcGrouperSyncGroups) == 0) {
      return;
    }
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroupsToRefreshGroupLink = new ArrayList<GcGrouperSyncGroup>();
    
    int refreshGroupLinkIfLessThanAmount = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getRefreshGroupLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncGroups) <= refreshGroupLinkIfLessThanAmount) {
      gcGrouperSyncGroupsToRefreshGroupLink.addAll(gcGrouperSyncGroups);
    } else {
      for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
        if (groupLinkMissing(gcGrouperSyncGroup)) {
          gcGrouperSyncGroupsToRefreshGroupLink.add(gcGrouperSyncGroup);
        }
      }
    }
    int gcGrouperSyncGroupsToRefreshGroupLinkCount = GrouperUtil.length(gcGrouperSyncGroupsToRefreshGroupLink);
    if (gcGrouperSyncGroupsToRefreshGroupLinkCount > 0) {
      this.grouperProvisioner.getDebugMap().put("gcGrouperSyncGroupsToRefreshGroupLink", gcGrouperSyncGroupsToRefreshGroupLinkCount);
    }
    if (gcGrouperSyncGroupsToRefreshGroupLinkCount == 0) {
      return;
    }
    // TODO retrieve groups and updateGroupLink(gcGrouperSyncGroupsToRefreshGroupLink);
  }

  public void updateEntityLinkFull() {
    updateEntityLink(GrouperUtil.nonNull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getTargetProvisioningObjects().getProvisioningEntities()));
  }

  /**
   * update entity link for these entities
   * @param gcGrouperSyncGroupsToRefreshGroupLink
   */
  public void updateEntityLink(List<ProvisioningEntity> targetEntities) {
  
    if (GrouperUtil.length(targetEntities) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String entityLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId2();
    boolean hasEnityLinkMemberFromId2 = !StringUtils.isBlank(entityLinkMemberFromId2);
    
    String entityLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberFromId3();
    boolean hasEnityLinkMemberFromId3 = !StringUtils.isBlank(entityLinkMemberFromId3);
  
    String entityLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId2();
    boolean hasEnityLinkMemberToId2 = !StringUtils.isBlank(entityLinkMemberToId2);
  
    String entityLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityLinkMemberToId3();
    boolean hasEnityLinkMemberToId3 = !StringUtils.isBlank(entityLinkMemberToId3);
  
    if (!hasEnityLinkMemberFromId2 && !hasEnityLinkMemberFromId3 && !hasEnityLinkMemberToId2 && !hasEnityLinkMemberToId3) {
      return;
    }
  
    int entitiesCannotFindLinkData = 0;
  
    int entitiesCannotFindSyncMember = 0;
  
    for (ProvisioningEntity targetEntity : targetEntities) {
  
      GcGrouperSyncMember gcGrouperSyncEntity = targetEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      
      if (gcGrouperSyncEntity == null) {
        entitiesCannotFindSyncMember++;
        continue;
      }
  
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetGroup", targetEntity);
      
      if (hasEnityLinkMemberFromId2) {
        String groupFromId2Value = GrouperUtil.substituteExpressionLanguage(entityLinkMemberFromId2, variableMap);
        gcGrouperSyncEntity.setMemberFromId2(groupFromId2Value);
      }
      
      if (hasEnityLinkMemberFromId3) {
        String groupFromId3Value = GrouperUtil.substituteExpressionLanguage(entityLinkMemberFromId3, variableMap);
        gcGrouperSyncEntity.setMemberFromId3(groupFromId3Value);
      }
      
      if (hasEnityLinkMemberToId2) {
        String groupToId2Value = GrouperUtil.substituteExpressionLanguage(entityLinkMemberToId2, variableMap);
        gcGrouperSyncEntity.setMemberToId2(groupToId2Value);
      }
      
      if (hasEnityLinkMemberFromId3) {
        String groupToId3Value = GrouperUtil.substituteExpressionLanguage(entityLinkMemberToId3, variableMap);
        gcGrouperSyncEntity.setMemberToId3(groupToId3Value);
      }
      
    }
  
    if (entitiesCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("entitiesCannotFindLinkData", entitiesCannotFindLinkData);
    }
    if (entitiesCannotFindSyncMember > 0) {
      this.grouperProvisioner.getDebugMap().put("entitiesCannotFindSyncMember", entitiesCannotFindSyncMember);
    }
    
    
  }

  public void updateEntityLinkIncremental() {
    // If using target group link and the ID is not in the group sync cache object, then resolve the target group, and put the id in the group sync object
    Collection<GcGrouperSyncMember> gcGrouperSyncMembers = GrouperUtil.nonNull(
        this.grouperProvisioner.retrieveGrouperProvisioningData().getMemberUuidToSyncMember()).values();
  
    if (GrouperUtil.length(gcGrouperSyncMembers) == 0) {
      return;
    }
    
    List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshEntityLink = new ArrayList<GcGrouperSyncMember>();
    
    int refreshEntityLinkIfLessThanAmount = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getRefreshEntityLinkIfLessThanAmount();
    if (GrouperUtil.length(gcGrouperSyncMembers) <= refreshEntityLinkIfLessThanAmount) {
      gcGrouperSyncMembersToRefreshEntityLink.addAll(gcGrouperSyncMembers);
    } else {
      for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
        if (entityLinkMissing(gcGrouperSyncMember)) {
          gcGrouperSyncMembersToRefreshEntityLink.add(gcGrouperSyncMember);
        }
      }
    }
    int gcGrouperSyncMembersToRefreshEntityLinkCount = GrouperUtil.length(gcGrouperSyncMembersToRefreshEntityLink);
    if (gcGrouperSyncMembersToRefreshEntityLinkCount > 0) {
      this.grouperProvisioner.getDebugMap().put("gcGrouperSyncMembersToRefreshEntityLinkCount", gcGrouperSyncMembersToRefreshEntityLinkCount);
    }
    if (gcGrouperSyncMembersToRefreshEntityLinkCount == 0) {
      return;
    }
    // TODO retrieve entities and updateEntityLink(gcGrouperSyncMembersToRefreshEntityLink);
  }

}
