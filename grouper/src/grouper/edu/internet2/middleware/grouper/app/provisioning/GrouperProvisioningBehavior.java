package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * how this provisioner interacts with the target.
 * some of these things default to the common configuration
 * @author mchyzer-local
 *
 */
public class GrouperProvisioningBehavior {
  
  private boolean createGroupsAndEntitiesBeforeTranslatingMemberships = true;

  /**
   * if set then only provision users who are in this group
   */
  private String groupIdOfUsersToProvision;
  
  /**
   * if set then only provision users who are in this group
   * @return group id
   */
  public String getGroupIdOfUsersToProvision() {
    return groupIdOfUsersToProvision;
  }


  /**
   * if set then only provision users who are in this group
   * @param groupIdOfUsersToProvision
   */
  public void setGroupIdOfUsersToProvision(String groupIdOfUsersToProvision) {
    this.groupIdOfUsersToProvision = groupIdOfUsersToProvision;
  }


  /**
   * Only provision policy groups
   */
  private Boolean onlyProvisionPolicyGroups;
  
  /**
   * Only provision policy groups
   * @return
   */
  public boolean isOnlyProvisionPolicyGroups() {
    if (this.onlyProvisionPolicyGroups != null) {
      return this.onlyProvisionPolicyGroups;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isOnlyProvisionPolicyGroups();
  }
  
  /**
   * If you want a metadata item on folders for specifying if provision only policy groups
   */
  private Boolean allowPolicyGroupOverride;
  
  /**
   * If you want a metadata item on folders for specifying if provision only policy groups
   * @return
   */
  public boolean isAllowPolicyGroupOverride() {
    if (this.allowPolicyGroupOverride != null) {
      return this.allowPolicyGroupOverride;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isAllowPolicyGroupOverride();
  }
  
  /**
   * If you want to filter for groups in a provisionable folder by a regex on its name, specify here.  If the regex matches then the group in the folder is provisionable.  e.g. folderExtension matches ^.*_someExtension   folderName matches ^.*_someExtension   groupExtension matches ^.*_someExtension   groupName matches ^.*_someExtension$
   */
  private String provisionableRegex;

  /**
   * If you want to filter for groups in a provisionable folder by a regex on its name, specify here.  If the regex matches then the group in the folder is provisionable.  e.g. folderExtension matches ^.*_someExtension   folderName matches ^.*_someExtension   groupExtension matches ^.*_someExtension   groupName matches ^.*_someExtension$
   * @return
   */
  public String getProvisionableRegex() {
    if (this.provisionableRegex != null) {
      return this.provisionableRegex;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getProvisionableRegex();
  }
  
  /**
   * If you want a metadata item on folders for specifying regex of names of objects to provision
   */
  private Boolean allowProvisionableRegexOverride;
  
  /**
   * If you want a metadata item on folders for specifying regex of names of objects to provision
   * @return
   */
  public boolean isAllowProvisionableRegexOverride() {
    if (this.allowProvisionableRegexOverride != null) {
      return this.allowProvisionableRegexOverride;
    }
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isAllowProvisionableRegexOverride();
  }

  
  
//  # Only provision policy groups
//  # {valueType: "boolean", order: 86700, defaultValue: "false", subSection: "assigningProvisioning"}
//  # provisioner.genericProvisioner.onlyProvisionPolicyGroups =
//
//  # If you want a metadata item on folders for specifying if provision only policy groups
//  # {valueType: "boolean", order: 86750, defaultValue: "true", subSection: "assigningProvisioning"}
//  # provisioner.genericProvisioner.allowPolicyGroupOverride =
//
//  # If you want to filter for groups in a provisionable folder by a regex on its name, specify here.  If the regex matches then the group in the folder is provisionable.  e.g. folderExtension matches ^.*_someExtension   folderName matches ^.*_someExtension   groupExtension matches ^.*_someExtension   groupName matches ^.*_someExtension$
//  # {valueType: "boolean", order: 86775, subSection: "assigningProvisioning"}
//  # provisioner.genericProvisioner.provisionableRegex =
//
//  # If you want a metadata item on folders for specifying regex of names of objects to provision
//  # {valueType: "boolean", order: 86800, subSection: "assigningProvisioning"}
//  # provisioner.genericProvisioner. =

  
  /**
   * If the subject API is needed to resolve attribute on subject  required, drives requirements of other configurations. defaults to false.
   */
  private Boolean hasSubjectLink = null;
  
  /**
   * If groups need to be resolved in the target before provisioning
   */
  private Boolean hasTargetGroupLink = null;
  
  /**
   * If users need to be resolved in the target before provisioning
   */
  private Boolean hasTargetEntityLink = null;
  
  public boolean canInsertGroupAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(name);
    if (grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isInsert()) {
      return true;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(name, this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName())) {
      return this.isInsertMemberships();
    }
    return false;
  }
  
  public boolean canUpdateGroupAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(name);
    if (grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isUpdate()) {
      return true;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes
        && StringUtils.equals(name, this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName())) {
      return this.isUpdateMemberships();
    }
    return false;
  }

  public boolean canInsertEntityAttribute(String name) {
    
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(name);
    if (grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isInsert()) {
      return true;
    }
    
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(name, this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName())) {
      return this.isInsertMemberships();
    }
    return false;
    
  }
  public boolean canUpdateEntityAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(name);
    if (grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isUpdate()) {
      return true;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
        && StringUtils.equals(name, this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName())) {
      return this.isUpdateMemberships();
    }
    return false;
  }

  public boolean canInsertMembershipAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isInsert();
  }
  public boolean canUpdateMembershipAttribute(String name) {
    GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().get(name);
    return grouperProvisioningConfigurationAttribute != null && grouperProvisioningConfigurationAttribute.isUpdate();
  }

  
  public boolean isHasTargetEntityLink() {
    if (hasTargetEntityLink != null) {
      return hasTargetEntityLink;
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isHasTargetEntityLink()) {
      return true;
    }
    for (GrouperProvisioningConfigurationAttributeDbCache entityAttributeCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
      if (entityAttributeCache == null) {
        continue;
      }
      if (entityAttributeCache.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target) {
        return true;
      }
    }
    
    return false;
  }



  
  public void setHasTargetEntityLink(Boolean hasTargetEntityLink) {
    this.hasTargetEntityLink = hasTargetEntityLink;
  }



  public boolean isHasSubjectLink() {
    if (hasSubjectLink == null) {
      GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[0];
      boolean hasSubjectLinkEntityAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
          && grouperProvisioningConfigurationAttributeDbCache0.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
          && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript());
      
      GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[1];
      boolean hasSubjectLinkEntityAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
          && grouperProvisioningConfigurationAttributeDbCache1.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
          && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript());

      GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[2];
      boolean hasSubjectLinkEntityAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
          && grouperProvisioningConfigurationAttributeDbCache2.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
          && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript());

      GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
          this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[3];
      boolean hasSubjectLinkEntityAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
          && grouperProvisioningConfigurationAttributeDbCache3.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
          && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript());
      this.hasSubjectLink = hasSubjectLinkEntityAttributeValueCache0 || hasSubjectLinkEntityAttributeValueCache1
          || hasSubjectLinkEntityAttributeValueCache2 || hasSubjectLinkEntityAttributeValueCache3;
    }
    
    return hasSubjectLink;
    
  }


  
  public void setHasSubjectLink(Boolean hasSubjectLink) {
    this.hasSubjectLink = hasSubjectLink;
  }

  public boolean isHasTargetGroupLink() {
    if (hasTargetGroupLink != null) {
      return hasTargetGroupLink;
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isHasTargetGroupLink()) {
      return true;
    }
    for (GrouperProvisioningConfigurationAttributeDbCache groupAttributeCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
      if (groupAttributeCache == null) {
        continue;
      }
      if (groupAttributeCache.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.target) {
        return true;
      }
    }

    return false;
  }

  private Set<String> groupAttributeNamesWithCache = null;

  public boolean isGroupAttributeNameHasCache(String attributeName) {
    
    if (this.groupAttributeNamesWithCache == null) {
      Set<String> result = new HashSet<String>();
      
      for (GrouperProvisioningConfigurationAttributeDbCache groupAttributeCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
        if (groupAttributeCache == null) {
          continue;
        }
        if (groupAttributeCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute
            && !StringUtils.isBlank(groupAttributeCache.getAttributeName())) {
          result.add(groupAttributeCache.getAttributeName());
        }
      }
      
      this.groupAttributeNamesWithCache = result;
    }
    return this.groupAttributeNamesWithCache.contains(attributeName);
  }
  
  private Set<String> entityAttributeNamesWithCache;
  
  public boolean isEntityAttributeNameHasCache(String attributeName) {
    
    if (this.entityAttributeNamesWithCache == null) {
      Set<String> result = new HashSet<String>();
      
      for (GrouperProvisioningConfigurationAttributeDbCache entityAttributeCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
        if (entityAttributeCache == null) {
          continue;
        }
        if (entityAttributeCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute
            && !StringUtils.isBlank(entityAttributeCache.getAttributeName())) {
          result.add(entityAttributeCache.getAttributeName());
        }
      }
      
      this.entityAttributeNamesWithCache = result;
    }
    return this.entityAttributeNamesWithCache.contains(attributeName);
  }
  

  public void setHasTargetGroupLink(Boolean hasTargetGroupLink) {
    this.hasTargetGroupLink = hasTargetGroupLink;
  }

  /**
   * 
   */
  private Boolean selectGroupMissingIncremental;

  /**
   * 
   */
  private Boolean selectEntityMissingIncremental;

  
  /**
   * 
   * @return
   */
  public boolean isSelectGroupMissingIncremental() {
    if (selectGroupMissingIncremental != null) {
      return selectGroupMissingIncremental;
    }
    if (!this.getGrouperProvisioningType().isIncrementalSync()) {
      selectGroupMissingIncremental = false;
      return selectGroupMissingIncremental;
    }
    if (this.isSelectGroups()) {
      selectGroupMissingIncremental = true;
      return selectGroupMissingIncremental;
    }
    selectGroupMissingIncremental = false;
    return selectGroupMissingIncremental;
  }


  /**
   * 
   * @return
   */
  public boolean isSelectEntityMissingIncremental() {
    if (selectEntityMissingIncremental != null) {
      return selectEntityMissingIncremental;
    }
    if (!this.getGrouperProvisioningType().isIncrementalSync()) {
      selectEntityMissingIncremental = false;
      return selectEntityMissingIncremental;
    }
    if (this.isSelectEntities()) {
      selectEntityMissingIncremental = true;
      return selectEntityMissingIncremental;
    }
    selectEntityMissingIncremental = false;
    return selectEntityMissingIncremental;
  }


  /**
   * 
   * @param retrieveMissingGroupsIncremental
   */
  public void setSelectGroupMissingIncremental(
      Boolean retrieveMissingGroupsIncremental) {
    this.selectGroupMissingIncremental = retrieveMissingGroupsIncremental;
  }


  /**
   * 
   */
  private GrouperProvisioningType grouperProvisioningType;
  
  
  public GrouperProvisioningType getGrouperProvisioningType() {
    return grouperProvisioningType;
  }

  
  public void setGrouperProvisioningType(GrouperProvisioningType grouperProvisioningType) {
    this.grouperProvisioningType = grouperProvisioningType;
  }


  private GrouperProvisioner grouperProvisioner;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public GrouperProvisioningBehavior(GrouperProvisioner grouperProvisioner) {
    super();
    this.grouperProvisioner = grouperProvisioner;
  }

  public GrouperProvisioningBehavior() {
    super();
    // TODO Auto-generated constructor stub
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  public GrouperProvisioningBehaviorMembershipType getGrouperProvisioningBehaviorMembershipType() {
    if (this.grouperProvisioningBehaviorMembershipType == null) {
      return this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningBehaviorMembershipType();
    }
    return grouperProvisioningBehaviorMembershipType;
  }
  
  public void setGrouperProvisioningBehaviorMembershipType(
      GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType) {
    this.grouperProvisioningBehaviorMembershipType = grouperProvisioningBehaviorMembershipType;
  }

  private GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType;

  private Boolean selectEntities;

  
  public boolean isSelectEntities() {
    if (this.selectEntities != null) {
      return this.selectEntities;
    }
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)) {
      selectEntities = false;
      return selectEntities;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      selectEntities = false;
      return selectEntities;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeEntityCrud()) {
      selectEntities = true;
      return selectEntities;
    }

    this.selectEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectEntities();
    return this.selectEntities;

  }


  
  public void setSelectEntities(Boolean entitiesRetrieve) {
    this.selectEntities = entitiesRetrieve;
  }

  private Boolean selectMemberships;

  public boolean isSelectMemberships() {
    if (this.selectMemberships != null) {
      return selectMemberships;
    }
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
        .getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMemberships(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsBulk(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)) {
      this.selectMemberships = false;
      return this.selectMemberships;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      selectMemberships = false;
      return selectMemberships;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeMembershipCrud()) {
      selectMemberships = true;
      return selectMemberships;
    }
    this.selectMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectMemberships();
    return this.selectMemberships;
  }
  
  private Boolean selectMembershipsForGroup;
  
  public void setSelectMembershipsForGroup(Boolean selectMembershipsForGroup) {
    this.selectMembershipsForGroup = selectMembershipsForGroup;
  }


  public boolean isSelectMembershipsForGroup() {
    if (this.selectMembershipsForGroup != null) {
      return selectMembershipsForGroup;
    }
    if (!this.isSelectGroups()) {
      this.selectMembershipsForGroup = false;
      return selectMembershipsForGroup;
    }
    if (!this.isSelectMemberships()) {
      this.selectMembershipsForGroup = false;
      return selectMembershipsForGroup;
    }

    this.selectMembershipsForGroup = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().canRecalcGroupMemberships();
    return this.selectMembershipsForGroup;
  }
  
  private Boolean selectMembershipsForEntity;
  
  
  public void setSelectMembershipsForEntity(Boolean selectMembershipsForEntity) {
    this.selectMembershipsForEntity = selectMembershipsForEntity;
  }


  public boolean isSelectMembershipsForEntity() {
    if (this.selectMembershipsForEntity != null) {
      return selectMembershipsForEntity;
    }
    if (!this.isSelectEntities()) {
      this.selectMembershipsForEntity = false;
      return selectMembershipsForEntity;
    }
    if (!this.isSelectMemberships()) {
      this.selectMembershipsForEntity = false;
      return selectMembershipsForEntity;
    }
    
    this.selectMembershipsForEntity = this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().canRecalcEntityMemberships();
    return this.selectMembershipsForEntity;
    
  }
  
  private Boolean replaceMemberships;
  
  public boolean isReplaceMemberships() {
    
    if (replaceMemberships != null) {
      return replaceMemberships;
    }
    
    this.replaceMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isReplaceMemberships();
    if (this.replaceMemberships.booleanValue() == false) {
      return replaceMemberships;
    }
    
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      //can the provisioner even do this?
      if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanReplaceGroupMemberships(), false)) {
        return true;
      }
    }    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      replaceMemberships = false;
      return replaceMemberships;
    }

    return false;
  }

  
  public void setReplaceMemberships(Boolean replaceMemberships) {
    this.replaceMemberships = replaceMemberships;
  }


  public void setSelectMemberships(Boolean membershipsRetrieve) {
    this.selectMemberships = membershipsRetrieve;
  }

  private Boolean selectGroups;

  public boolean isSelectGroups() {
    
    if (this.selectGroups != null) {
      return selectGroups;
    }

    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)
        && !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)) {
      this.selectGroups = false;
      return this.selectGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      selectGroups = false;
      return selectGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeGroupCrud()) {
      selectGroups = true;
      return selectGroups;
    }

    this.selectGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectGroups();
    return this.selectGroups;
    
  }
  
  public void setSelectGroups(Boolean groupsRetrieve) {
    this.selectGroups = groupsRetrieve;
  }

  private Boolean selectGroupsAll;

  private Set<String> selectGroupsAttributes;

  private Boolean updateGroups;

  private Set<String> updateGroupAttributes;

  private Boolean insertGroups;

  private Boolean deleteGroupsIfNotExistInGrouper;
  
  private Boolean deleteGroupsIfGrouperDeleted;

  private Boolean deleteEntitiesIfGrouperCreated;
  private Boolean deleteGroupsIfGrouperCreated;
  private Boolean deleteMembershipsIfGrouperCreated;


  public boolean isDeleteEntitiesIfGrouperCreated() {
    if (this.deleteEntitiesIfGrouperCreated != null) {
      return deleteEntitiesIfGrouperCreated;
    }

    //can the provisioner even do this?
    if (!isDeleteEntities()) {
      deleteEntitiesIfGrouperCreated = false;
      return deleteEntitiesIfGrouperCreated;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      deleteEntitiesIfGrouperCreated = false;
      return deleteEntitiesIfGrouperCreated;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isMakeChangesToEntities()) {
      deleteEntitiesIfGrouperCreated = false;
      return deleteEntitiesIfGrouperCreated;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeEntityCrud()) {
      deleteEntitiesIfGrouperCreated = true;
      return deleteEntitiesIfGrouperCreated;
    }

    // is it configured to?
    deleteEntitiesIfGrouperCreated = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteEntitiesIfGrouperCreated();
    return deleteEntitiesIfGrouperCreated;

  }

  public void setDeleteEntitiesIfGrouperCreated(Boolean deleteEntitiesIfGrouperCreated) {
    this.deleteEntitiesIfGrouperCreated = deleteEntitiesIfGrouperCreated;
  }

  public boolean isDeleteGroupsIfGrouperCreated() {
    if (this.deleteGroupsIfGrouperCreated != null) {
      return deleteGroupsIfGrouperCreated;
    }

    //can the provisioner even do this?
    if (!isDeleteGroups()) {
      deleteGroupsIfGrouperCreated = false;
      return deleteGroupsIfGrouperCreated;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      deleteGroupsIfGrouperCreated = false;
      return deleteGroupsIfGrouperCreated;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeGroupCrud()) {
      deleteGroupsIfGrouperCreated = true;
      return deleteGroupsIfGrouperCreated;
    }

    // is it configured to?
    deleteGroupsIfGrouperCreated = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteGroupsIfGrouperCreated();
    return deleteGroupsIfGrouperCreated;
  }
  
  public void setDeleteGroupsIfGrouperCreated(Boolean deleteGroupsIfGrouperCreated) {
    this.deleteGroupsIfGrouperCreated = deleteGroupsIfGrouperCreated;
  }
  
  public boolean isDeleteMembershipsIfGrouperCreated() {
    
    if (this.deleteMembershipsIfGrouperCreated != null) {
      return deleteMembershipsIfGrouperCreated;
    }

    //can the provisioner even do this?
    if (!this.isDeleteMemberships()) {
      this.deleteMembershipsIfGrouperCreated = false;
      return this.deleteMembershipsIfGrouperCreated;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      deleteMembershipsIfGrouperCreated = false;
      return deleteMembershipsIfGrouperCreated;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeMembershipCrud()) {
      deleteMembershipsIfGrouperCreated = true;
      return deleteMembershipsIfGrouperCreated;
    }

    // is it configured to?
    this.deleteMembershipsIfGrouperCreated = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteMembershipsIfGrouperCreated();
    return this.deleteMembershipsIfGrouperCreated;

  }

  public void setDeleteMembershipsIfGrouperCreated(
      Boolean deleteMembershipsIfGrouperCreated) {
    this.deleteMembershipsIfGrouperCreated = deleteMembershipsIfGrouperCreated;
  }
  
  private Boolean selectEntitiesAll;

  private Set<String> selectEntityAttributes;

  private Boolean updateEntities;

  private Set<String> updateEntityAttributes;

  private Boolean insertEntities;

  private Set<String> insertEntityAttributes;

  private Boolean deleteEntitiesIfNotExistInGrouper;

  private Boolean deleteGroups;
  
  private Boolean deleteEntities;
  
  private Boolean deleteMemberships;
  
  /**
   * 
   * @return
   */
  public boolean isDeleteGroups() {
    if (this.deleteGroups != null) {
      return deleteGroups;
    }

    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteGroup(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteGroups(), false)
        ) {
      deleteGroups = false;
      return deleteGroups;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      deleteGroups = false;
      return deleteGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeGroupCrud()) {
      deleteGroups = true;
      return deleteGroups;
    }

    // is it configured to?
    deleteGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteGroups();
    return deleteGroups;
  }

  /**
   * 
   * @param deleteGroups
   */
  public void setDeleteGroups(boolean deleteGroups) {
    this.deleteGroups = deleteGroups;
  }

  /**
   * 
   * @return
   */
  public boolean isDeleteEntities() {
    if (this.deleteEntities != null) {
      return deleteEntities;
    }

    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteEntity(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteEntities(), false)
        ) {
      deleteEntities = false;
      return deleteEntities;
    }

    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      deleteEntities = false;
      return deleteEntities;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isMakeChangesToEntities()) {
      deleteEntities = false;
      return deleteEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeEntityCrud()) {
      deleteEntities = true;
      return deleteEntities;
    }

    // is it configured to?
    deleteEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteEntities();
    return deleteEntities;

  }
  
  public void setDeleteEntities(boolean deleteEntities) {
    this.deleteEntities = deleteEntities;
  }

  /**
   * @param gcGrouperSyncMembership
   * @return false
   */
  public boolean isDeleteMembership(GcGrouperSyncMembership gcGrouperSyncMembership) {
    
    if (this.isDeleteMembershipsIfNotExistInGrouper()) {
      
      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteMembershipsOnlyInTrackedGroups()) {
        
        /**
         * If this is true, then only delete memberships if:

          the group is being deleted
          (or) if the group is or was provisionable (has a grouper sync group record and provisionable is true or provisionable_end is not null)
         */
        
        if (gcGrouperSyncMembership == null) {
          return false;
        } 
          
        GcGrouperSyncGroup grouperSyncGroup = gcGrouperSyncMembership.getGrouperSyncGroup();
        if (grouperSyncGroup == null) {
          // maybe the grouperSyncGroup needs to be put in the membership somewhere
          return false;
        }
        
        if (grouperSyncGroup.isProvisionable()) {
          return true;
        }
        
        if (grouperSyncGroup.getProvisionableEnd() != null) {
          return true;
        }
        
        return false;
      }
      return true;
    }
    
    if (gcGrouperSyncMembership == null) {
      return false;
    }
    
    // grouper deleted it
    if (this.isDeleteMembershipsIfGrouperDeleted()) {
      return true;
    }
    
    // delete if inserted and delete if grouper created
    return gcGrouperSyncMembership.isInTargetInsertOrExists() && this.isDeleteMembershipsIfGrouperCreated();
  }

  /**
   * @param gcGrouperSyncMember
   * @return false
   */
  public boolean isDeleteEntity(GcGrouperSyncMember gcGrouperSyncMember) {
    
    if (this.isDeleteEntitiesIfNotExistInGrouper()) {
      return true;
    }
    
    if (gcGrouperSyncMember == null) {
      return false;
    }
    
    // grouper deleted it
    if (this.isDeleteEntitiesIfGrouperDeleted()) {
      return true;
    }
    
    // delete if inserted and delete if grouper created
    return gcGrouperSyncMember.isInTargetInsertOrExists() && this.isDeleteEntitiesIfGrouperCreated();
  }

  /**
   * @param gcGrouperSyncGroup
   * @return false
   */
  public boolean isDeleteGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    
    if (this.isDeleteGroupsIfNotExistInGrouper()) {
      return true;
    }
    
    if (gcGrouperSyncGroup == null) {
      return false;
    }
    
    // grouper deleted it
    if (this.isDeleteGroupsIfGrouperDeleted()) {
      return true;
    }
    
    // delete if inserted and delete if grouper created
    return gcGrouperSyncGroup.isInTargetInsertOrExists() && this.isDeleteGroupsIfGrouperCreated();
  }

  public boolean isDeleteMemberships() {
    if (this.deleteMemberships != null) {
      return deleteMemberships;
    }

    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      //can the provisioner even do this?
      if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteMembership(), false)
        &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanDeleteMemberships(), false)
          ) {
        this.deleteMemberships = false;
        return this.deleteMemberships;
      }

    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      deleteMemberships = false;
      return deleteMemberships;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeMembershipCrud()) {
      deleteMemberships = true;
      return deleteMemberships;
    }

    // is it configured to?
    this.deleteMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteMemberships();
    return this.deleteMemberships;
  }
  
  public void setDeleteMemberships(boolean deleteMemberships) {
    this.deleteMemberships = deleteMemberships;
  }

  private Boolean deleteEntitiesIfGrouperDeleted;

  private Boolean selectMembershipsAll;

  private Set<String> selectMembershipAttributes;

  private Boolean updateMemberships;

  private Set<String> updateMembershipsAttributes;

  private Boolean insertMemberships;

  private Set<String> insertMembershipsAttributes;

  private Boolean deleteMembershipsIfNotExistInGrouper;

  private Boolean deleteMembershipsOnlyInTrackedGroups;
  
  private Boolean deleteMembershipsIfGrouperDeleted;
  
  public boolean isSelectGroupsAll() {
    if (this.selectGroupsAll != null) {
      return selectGroupsAll;
    }
   
  
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
      selectGroupsAll = false;
      return selectGroupsAll;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllGroups()) {
      selectGroupsAll = false;
      return selectGroupsAll;
    }
    
    selectGroupsAll = true;
    
    return selectGroupsAll;
  
  }
  
  public void setSelectGroupsAll(Boolean groupsRetrieveAll) {
    this.selectGroupsAll = groupsRetrieveAll;
  }

  
  public Set<String> getSelectGroupsAttributes() {
    return selectGroupsAttributes;
  }

  
  public void setSelectGroupsAttributes(Set<String> groupsRetrieveAttributes) {
    this.selectGroupsAttributes = groupsRetrieveAttributes;
  }

  
  public boolean isUpdateGroups() {
    if (this.updateGroups != null) {
      return updateGroups;
    }

    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateGroup(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateGroups(), false)
        ) {
      updateGroups = false;
      return updateGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      updateGroups = false;
      return updateGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeGroupCrud()) {
      updateGroups = true;
      return updateGroups;
    }

    // is it configured to?
    updateGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isUpdateGroups();
    return this.updateGroups;
  }
  
  public void setUpdateGroups(Boolean groupsUpdate) {
    this.updateGroups = groupsUpdate;
  }

  
  public Set<String> getUpdateGroupAttributes() {
    return updateGroupAttributes;
  }

  
  public void setUpdateGroupAttributes(Set<String> groupsUpdateAttributes) {
    this.updateGroupAttributes = groupsUpdateAttributes;
  }

  
  public boolean isInsertGroups() {
    if (this.insertGroups != null) {
      return insertGroups;
    }
    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertGroup(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertGroups(), false)
        ) {
      insertGroups = false;
      return insertGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      insertGroups = false;
      return insertGroups;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeGroupCrud()) {
      insertGroups = true;
      return insertGroups;
    }

    // is it configured to?
    insertGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isInsertGroups();
    return insertGroups;
  }

  
  public void setInsertGroups(Boolean groupsInsert) {
    this.insertGroups = groupsInsert;
  }

  
  
  public boolean isDeleteGroupsIfNotExistInGrouper() {
    if (this.deleteGroupsIfNotExistInGrouper != null) {
      return deleteGroupsIfNotExistInGrouper;
    }
    if (!this.isDeleteGroups()) {
      deleteGroupsIfNotExistInGrouper = false;
      return deleteGroupsIfNotExistInGrouper;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      deleteGroupsIfNotExistInGrouper = false;
      return deleteGroupsIfNotExistInGrouper;
    }

    deleteGroupsIfNotExistInGrouper = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteGroupsIfNotExistInGrouper();
    return deleteGroupsIfNotExistInGrouper;
  }

  
  public void setDeleteGroupsIfNotExistInGrouper(Boolean groupsDeleteIfNotInGrouper) {
    this.deleteGroupsIfNotExistInGrouper = groupsDeleteIfNotInGrouper;
  }
  
  public boolean isDeleteGroupsIfGrouperDeleted() {
    
    if (this.deleteGroupsIfGrouperDeleted != null) {
      return deleteGroupsIfGrouperDeleted;
    }
    
    if (!this.isDeleteGroups()) {
      deleteGroupsIfGrouperDeleted = false;
      return deleteGroupsIfGrouperDeleted;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      deleteGroupsIfGrouperDeleted = false;
      return deleteGroupsIfGrouperDeleted;
    }

    deleteGroupsIfGrouperDeleted = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteGroupsIfGrouperDeleted();
    return deleteGroupsIfGrouperDeleted;
  }

  
  public void setDeleteGroupsIfGrouperDeleted(
      Boolean groupsDeleteIfDeletedFromGrouper) {
    this.deleteGroupsIfGrouperDeleted = groupsDeleteIfDeletedFromGrouper;
  }

 
  public boolean isSelectEntitiesAll() {
    if (this.selectEntitiesAll != null) {
      return selectEntitiesAll;
    }

    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
      selectEntitiesAll = false;
      return selectEntitiesAll;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectAllEntities()) {
      selectEntitiesAll = false;
      return selectEntitiesAll;
    }
    
    selectEntitiesAll = true;
    
    return selectEntitiesAll;    
    
  }

  
  public void setSelectEntitiesAll(Boolean entitiesRetrieveAll) {
    this.selectEntitiesAll = entitiesRetrieveAll;
  }

  
  public Set<String> getSelectEntityAttributes() {
    return selectEntityAttributes;
  }

  
  public void setSelectEntityAttributes(Set<String> entitiesRetrieveAttributes) {
    this.selectEntityAttributes = entitiesRetrieveAttributes;
  }

  
  public boolean isUpdateEntities() {
    if (this.updateEntities != null) {
      return this.updateEntities;
    }
    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateEntity(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateEntities(), false)
        ) {
      updateEntities = false;
      return updateEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      updateEntities = false;
      return updateEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isMakeChangesToEntities()) {
      updateEntities = false;
      return updateEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeEntityCrud()) {
      updateEntities = true;
      return updateEntities;
    }

    // is it configured to?
    updateEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isUpdateEntities();
    return updateEntities;
  }

  
  public void setUpdateEntities(Boolean entitiesUpdate) {
    this.updateEntities = entitiesUpdate;
  }

  
  public Set<String> getUpdateEntityAttributes() {
    return updateEntityAttributes;
  }

  
  public void setUpdateEntityAttributes(Set<String> entitiesUpdateAttributes) {
    this.updateEntityAttributes = entitiesUpdateAttributes;
  }

  
  public boolean isInsertEntities() {
    if (this.insertEntities != null) {
      return this.insertEntities;
    }
    
    //can the provisioner even do this?
    if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertEntity(), false)
      &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertEntities(), false)
        ) {
      insertEntities = false;
      return insertEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      insertEntities = false;
      return insertEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isMakeChangesToEntities()) {
      insertEntities = false;
      return insertEntities;
    }

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeEntityCrud()) {
      insertEntities = true;
      return insertEntities;
    }

    // is it configured to?
    insertEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isInsertEntities();
    return insertEntities;
  }
  
  public void setInsertEntities(Boolean entitiesInsert) {
    this.insertEntities = entitiesInsert;
  }

  
  public Set<String> getInsertEntityAttributes() {
    return insertEntityAttributes;
  }

  
  public void setInsertEntityAttributes(Set<String> entitiesInsertAttributes) {
    this.insertEntityAttributes = entitiesInsertAttributes;
  }

  
  public boolean isDeleteEntitiesIfNotExistInGrouper() {
    
    if (this.deleteEntitiesIfNotExistInGrouper != null) {
      return this.deleteEntitiesIfNotExistInGrouper;
    }

    //can the provisioner even do this?
    if (!isDeleteEntities()) {
      deleteEntitiesIfNotExistInGrouper = false;
      return deleteEntitiesIfNotExistInGrouper;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      deleteEntitiesIfNotExistInGrouper = false;
      return deleteEntitiesIfNotExistInGrouper;
    }
    
    // is it configured to?
    deleteEntitiesIfNotExistInGrouper = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteEntitiesIfNotExistInGrouper();
    return deleteEntitiesIfNotExistInGrouper;
  }

  
  public void setDeleteEntitiesIfNotExistInGrouper(Boolean entitiesDeleteIfNotInGrouper) {
    this.deleteEntitiesIfNotExistInGrouper = entitiesDeleteIfNotInGrouper;
  }

  
  public boolean isDeleteEntitiesIfGrouperDeleted() {
    
    if (this.deleteEntitiesIfGrouperDeleted != null) {
      return this.deleteEntitiesIfGrouperDeleted;
    }
    
    //can the provisioner even do this?
    if (!isDeleteEntities()) {
      deleteEntitiesIfGrouperDeleted = false;
      return deleteEntitiesIfGrouperDeleted;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      deleteEntitiesIfGrouperDeleted = false;
      return deleteEntitiesIfGrouperDeleted;
    }
    
    // is it configured to?
    deleteEntitiesIfGrouperDeleted = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteEntitiesIfGrouperDeleted();
    return deleteEntitiesIfGrouperDeleted;
  }

  public void setDeleteEntitiesIfGrouperDeleted(
      Boolean entitiesDeleteIfDeletedFromGrouper) {
    this.deleteEntitiesIfGrouperDeleted = entitiesDeleteIfDeletedFromGrouper;
  }

  public boolean isSelectMembershipsAll() {
    if (this.selectMembershipsAll != null) {
      return this.selectMembershipsAll;
    }
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isSelectMemberships()) {
      selectMembershipsAll = false;
      return selectMembershipsAll;
    }

    if (GrouperUtil.booleanValue(this.getGrouperProvisioner()
        .retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false) || 
        GrouperUtil.booleanValue(this.getGrouperProvisioner()
            .retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllData(), false)) {
      this.selectMembershipsAll = true;
      return this.selectMembershipsAll;
    }
    
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      if ((this.isSelectEntitiesAll() || this.isSelectEntities()) 
            && this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().canRetrieveMembershipsWithEntity()) {
        this.selectMembershipsAll = true;
        return this.selectMembershipsAll;
      } else {
        this.selectMembershipsAll = false;
        return this.selectMembershipsAll;
      }
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      if ( (this.isSelectGroupsAll() || this.isSelectGroups()) 
          && this.grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().canRetrieveMembershipsWithGroup()) {
        this.selectMembershipsAll = true;
        return this.selectMembershipsAll;
      } else {
        this.selectMembershipsAll = false;
        return this.selectMembershipsAll;
      }
    }
    
    if (this.isSelectEntitiesAll()) {
      if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
          .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntity(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByEntities(), false)) {
        this.selectMembershipsAll = true;
        return this.selectMembershipsAll;
      }
    }
      
    if (this.isSelectGroupsAll()) {
      if (GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
          .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroup(), false)
        || GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getWrappedDao()
            .getGrouperProvisionerDaoCapabilities().getCanRetrieveMembershipsByGroups(), false)) {
        this.selectMembershipsAll = true;
        return this.selectMembershipsAll;
      }
    }

    this.selectMembershipsAll = false;
    return this.selectMembershipsAll;
  }

  public void setSelectMembershipsAll(Boolean membershipsRetrieveAll) {
    this.selectMembershipsAll = membershipsRetrieveAll;
  }
  
  public Set<String> getSelectMembershipAttributes() {
    return selectMembershipAttributes;
  }

  
  public void setSelectMembershipAttributes(Set<String> membershipsRetrieveAttributes) {
    this.selectMembershipAttributes = membershipsRetrieveAttributes;
  }
  
  
  public boolean isUpdateMemberships() {
    if (updateMemberships != null) {
      return updateMemberships;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      //can the provisioner even do this?
      if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateMembership(), false)
        &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanUpdateMemberships(), false)
          ) {
        updateMemberships = false;
        return updateMemberships;
      }
    }    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      updateMemberships = false;
      return updateMemberships;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeMembershipCrud()) {
      updateMemberships = true;
      return updateMemberships;
    }

    // is it configured to?  theres not a lot of use cases for updating memberships, so lets sort of ignore this for now
    this.updateMemberships = this.isInsertMemberships();
    return this.updateMemberships;
  }

  
  public void setUpdateMemberships(Boolean membershipsUpdate) {
    this.updateMemberships = membershipsUpdate;
  }

  
  public Set<String> getUpdateMembershipsAttributes() {
    return updateMembershipsAttributes;
  }

  
  public void setUpdateMembershipsAttributes(Set<String> membershipsUpdateAttributes) {
    this.updateMembershipsAttributes = membershipsUpdateAttributes;
  }

  
  public boolean isInsertMemberships() {
    if (insertMemberships != null) {
      return insertMemberships;
    }
    if (this.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      //can the provisioner even do this?
      if (!GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertMembership(), false)
        &&  !GrouperUtil.booleanValue(this.getGrouperProvisioner().retrieveGrouperProvisioningTargetDaoAdapter().getGrouperProvisionerDaoCapabilities().getCanInsertMemberships(), false)
          ) {
        this.insertMemberships = false;
        return this.insertMemberships;
      }
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      insertMemberships = false;
      return insertMemberships;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isCustomizeMembershipCrud()) {
      insertMemberships = true;
      return insertMemberships;
    }

    this.insertMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isInsertMemberships();
    return this.insertMemberships;
  }

  
  public void setInsertMemberships(Boolean membershipsInsert) {
    this.insertMemberships = membershipsInsert;
  }

  
  public Set<String> getInsertMembershipsAttributes() {
    return insertMembershipsAttributes;
  }

  
  public void setInsertMembershipsAttributes(Set<String> membershipsInsertAttributes) {
    this.insertMembershipsAttributes = membershipsInsertAttributes;
  }

  
  public boolean isDeleteMembershipsIfNotExistInGrouper() {
    if (deleteMembershipsIfNotExistInGrouper != null) {
      return deleteMembershipsIfNotExistInGrouper;
    }
    
    //can the provisioner even do this?
    if (!this.isDeleteMemberships()) {
      this.deleteMembershipsIfNotExistInGrouper = false;
      return this.deleteMembershipsIfNotExistInGrouper;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      deleteMembershipsIfNotExistInGrouper = false;
      return deleteMembershipsIfNotExistInGrouper;
    }

    // is it configured to?
    this.deleteMembershipsIfNotExistInGrouper = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteMembershipsIfNotExistInGrouper();
    return this.deleteMembershipsIfNotExistInGrouper;
  }
  
  public void setDeleteMembershipsIfNotExistInGrouper(Boolean membershipsDeleteIfNotInGrouper) {
    this.deleteMembershipsIfNotExistInGrouper = membershipsDeleteIfNotInGrouper;
  }
  
  
  public Boolean getDeleteMembershipsOnlyInTrackedGroups() {
    return deleteMembershipsOnlyInTrackedGroups;
  }
  
  public void setDeleteMembershipsOnlyInTrackedGroups(Boolean deleteMembershipsOnlyInTrackedGroups) {
    this.deleteMembershipsOnlyInTrackedGroups = deleteMembershipsOnlyInTrackedGroups;
  }


  public boolean isDeleteMembershipsIfGrouperDeleted() {
    if (deleteMembershipsIfGrouperDeleted != null) {
      return deleteMembershipsIfGrouperDeleted;
    }
    //can the provisioner even do this?
    if (!this.isDeleteMemberships()) {
      this.deleteMembershipsIfGrouperDeleted = false;
      return this.deleteMembershipsIfGrouperDeleted;
    }
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()) {
      deleteMembershipsIfGrouperDeleted = false;
      return deleteMembershipsIfGrouperDeleted;
    }

    // is it configured to?
    this.deleteMembershipsIfGrouperDeleted = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isDeleteMembershipsIfGrouperDeleted();
    return this.deleteMembershipsIfGrouperDeleted;

  }
  
  public void setDeleteMembershipsIfGrouperDeleted(
      Boolean membershipsDeleteIfDeletedFromGrouper) {
    this.deleteMembershipsIfGrouperDeleted = membershipsDeleteIfDeletedFromGrouper;
  }

  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisioningBehavior.class, null, false);
        
    fieldNames = new TreeSet<String>(fieldNames);
    
    fieldNames.remove("grouperProvisioner");
    fieldNames.remove("entityAttributeNamesWithCache");
    fieldNames.remove("groupAttributeNamesWithCache");
    
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      // call getter
      Object value = GrouperUtil.propertyValue(this, fieldName);
      if (!GrouperUtil.isBlank(value)) {
        
        if ((value instanceof Collection) && ((Collection)value).size() == 0) {
          continue;
        }
        if ((value instanceof Map) && ((Map)value).size() == 0) {
          continue;
        }
        if ((value.getClass().isArray()) && Array.getLength(value) == 0) {
          continue;
        }
        
        if (!firstField) {
          result.append(", ");
        }
        firstField = false;
        result.append(fieldName).append(" = '").append(GrouperUtil.toStringForLog(value, false)).append("'");
      }
    }
//    for (String propertyName : new String[] {"hasEntityLinkEntityAttributeValueCache0",
//        "hasEntityLinkEntityAttributeValueCache1", "hasEntityLinkEntityAttributeValueCache2", "hasEntityLinkEntityAttributeValueCache3",
//        "hasGroupLinkGroupAttributeValueCache0", "hasGroupLinkGroupAttributeValueCache1", "hasGroupLinkGroupAttributeValueCache2", "hasGroupLinkGroupAttributeValueCache3",
//        "groupLinkGroupAttributeValueCache0Attribute", "groupLinkGroupAttributeValueCache1Attribute", "groupLinkGroupAttributeValueCache2Attribute",
//        "groupLinkGroupAttributeValueCache3Attribute", "entityLinkEntityAttributeValueCache0Attribute", "entityLinkEntityAttributeValueCache1Attribute",
//        "entityLinkEntityAttributeValueCache2Attribute", "entityLinkEntityAttributeValueCache3Attribute"}) {
//
//      Object value = GrouperUtil.propertyValue(this, propertyName);
//      if (value != null) {
//        if (!firstField) {
//          result.append(", ");
//        }
//        firstField = false;
//        result.append(propertyName).append(" = '").append(GrouperUtil.toStringForLog(value, false)).append("'");
//        
//      }
//    }
      
    return result.toString();
  }
  
  public boolean canUpdateObjectAttribute(
      ProvisioningUpdatable grouperProvisioningUpdatable, String attributeName) {

    if (grouperProvisioningUpdatable instanceof ProvisioningGroup) {
      return this.canUpdateGroupAttribute(attributeName);
    }
    if (grouperProvisioningUpdatable instanceof ProvisioningEntity) {
      return this.canUpdateEntityAttribute(attributeName);
    }
    if (grouperProvisioningUpdatable instanceof ProvisioningMembership) {
      return this.canUpdateMembershipAttribute(attributeName);
    }
    throw new RuntimeException("Not expecting object type: " + (grouperProvisioningUpdatable == null ? "null" : grouperProvisioningUpdatable.getClass().getName()));
  }
  
  
  public boolean isCreateGroupsAndEntitiesBeforeTranslatingMemberships() {
    return createGroupsAndEntitiesBeforeTranslatingMemberships;
  }

  
  public void setCreateGroupsAndEntitiesBeforeTranslatingMemberships(boolean createGroupsAndEntitiesBeforeTranslatingMemberships) {
    this.createGroupsAndEntitiesBeforeTranslatingMemberships = createGroupsAndEntitiesBeforeTranslatingMemberships;
  }
  
  private String subjectIdentifierForMemberSyncTable;
  
  public String getSubjectIdentifierForMemberSyncTable() {
    
    if (this.subjectIdentifierForMemberSyncTable != null) {
      return this.subjectIdentifierForMemberSyncTable;
    }
    
    String currSubjectIdentifierForMemberSyncTable = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getSubjectIdentifierForMemberSyncTable();

    // no override, try to compute it
    if (StringUtils.isBlank(currSubjectIdentifierForMemberSyncTable)) {
      List<GrouperProvisioningConfigurationAttribute> searchAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes();
      for (GrouperProvisioningConfigurationAttribute searchAttribute : GrouperUtil.nonNull(searchAttributes)) {
        String value = searchAttribute.getTranslateFromGrouperProvisioningEntityField();
        if (value != null && value.startsWith("subjectIdentifier")) {
          currSubjectIdentifierForMemberSyncTable = value;
        }
      }
    }
    
    if (StringUtils.isBlank(currSubjectIdentifierForMemberSyncTable)) {
      List<GrouperProvisioningConfigurationAttribute> matchingAttributes = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes();
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : GrouperUtil.nonNull(matchingAttributes)) {
        String value = matchingAttribute.getTranslateFromGrouperProvisioningEntityField();
        if (value != null && value.startsWith("subjectIdentifier")) {
          currSubjectIdentifierForMemberSyncTable = value; //value.substring(11);
        }
      }
      
    }
    
    if (StringUtils.isBlank(currSubjectIdentifierForMemberSyncTable) || 
        StringUtils.equals("subjectIdentifier", currSubjectIdentifierForMemberSyncTable)) {
      // default
      currSubjectIdentifierForMemberSyncTable = "subjectIdentifier0";
    }
    
    if (!currSubjectIdentifierForMemberSyncTable.equals("subjectIdentifier0") && 
        !currSubjectIdentifierForMemberSyncTable.equals("subjectIdentifier1") &&
        !currSubjectIdentifierForMemberSyncTable.equals("subjectIdentifier2")) {
      throw new RuntimeException("Not expecting subject identifier for member sync: " + currSubjectIdentifierForMemberSyncTable);
    }

    this.subjectIdentifierForMemberSyncTable = currSubjectIdentifierForMemberSyncTable;

    return this.subjectIdentifierForMemberSyncTable;
  }


  public boolean isLoadEntitiesToGrouperTable() {
    return this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isLoadEntitiesToGrouperTable();
  }
  
}
