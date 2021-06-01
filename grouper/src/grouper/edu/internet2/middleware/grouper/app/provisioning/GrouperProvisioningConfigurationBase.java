package edu.internet2.middleware.grouper.app.provisioning;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * 
 * @author mchyzer
 *
 */
public abstract class GrouperProvisioningConfigurationBase {

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
   * if create group in target during diagnostics
   */
  private Boolean createGroupDuringDiagnostics;

  /**
   * if create group in target during diagnostics
   * @return if create
   */
  public boolean isCreateGroupDuringDiagnostics() {
    return GrouperUtil.booleanValue(createGroupDuringDiagnostics, false);
  }

  /**
   * if select all groups during diagnostics (default false)
   */
  private Boolean diagnosticsGroupsAllSelect;

  /**
   * if select all groups during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsGroupsAllSelect() {
    if (this.diagnosticsGroupsAllSelect != null) {
      return this.diagnosticsGroupsAllSelect;
    }
    return false;
  }
  
  /**
   * if select all entities during diagnostics (default false)
   */
  private Boolean diagnosticsEntitiesAllSelect;

  /**
   * if select all entities during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsEntitiesAllSelect() {
    if (this.diagnosticsEntitiesAllSelect != null) {
      return this.diagnosticsEntitiesAllSelect;
    }
    return false;
  }
  
  /**
   * group name of group to use for diagnostics
   */
  private String diagnosticsGroupName;

  /**
   * group name of group to use for diagnostics
   * @return the group name
   */
  public String getDiagnosticsGroupName() {
    return diagnosticsGroupName;
  }
  
  /**
   * if select all memberships during diagnostics (default false)
   */
  private Boolean diagnosticsMembershipsAllSelect;

  /**
   * if select all memberships during diagnostics
   * @return true if so
   */
  public boolean isDiagnosticsMembershipsAllSelect() {
    if (this.diagnosticsMembershipsAllSelect != null) {
      return this.diagnosticsMembershipsAllSelect;
    }
    return false;
  }
  
  /**
   * Only provision policy groups
   */
  private Boolean onlyProvisionPolicyGroups;
  
  /**
   * Only provision policy groups, default false
   * @return 
   */
  public boolean isOnlyProvisionPolicyGroups() {
    if (this.onlyProvisionPolicyGroups != null) {
      return this.onlyProvisionPolicyGroups;
    }
    return false;
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
    return true;
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
    return this.provisionableRegex;
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
    return true;
  }
  
  /** if the target should be checked before sending actions.  e.g. if an addMember is made to a provisionable group, then check the target to see if the entity is already a member first. */
  private boolean recalculateAllOperations;

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetGroupAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();
  
  /**
   * field name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetGroupFieldNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  /**
   * metadata name to metadata item
   */
  private Map<String, GrouperProvisioningObjectMetadataItem> metadataNameToMetadataItem = new TreeMap<String, GrouperProvisioningObjectMetadataItem>();
  
  /**
   * metadata name to metadata item
   * @return
   */
  public Map<String, GrouperProvisioningObjectMetadataItem> getMetadataNameToMetadataItem() {
    return metadataNameToMetadataItem;
  }

  /**
   * metadata name to metadata item
   * @param metadataNameToMetadataItem
   */
  public void setMetadataNameToMetadataItem(
      Map<String, GrouperProvisioningObjectMetadataItem> metadataNameToMetadataItem) {
    this.metadataNameToMetadataItem = metadataNameToMetadataItem;
  }

  /**
   * field name to config
   * @return
   */
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetGroupFieldNameToConfig() {
    return targetGroupFieldNameToConfig;
  }

  /**
   * get the group matching attribute object (could be field or attribute)
   * @return the attribute
   */
  public GrouperProvisioningConfigurationAttribute retrieveGroupAttributeMatching() {

    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.getTargetGroupAttributeNameToConfig().values()) {
      if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
        return grouperProvisioningConfigurationAttribute;
      }
    }
    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.getTargetGroupFieldNameToConfig().values()) {
      if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
        return grouperProvisioningConfigurationAttribute;
      }
    }
    
    return null;
  }
  
  /**
   * 
   * @return map
   */
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetGroupAttributeNameToConfig() {
    return targetGroupAttributeNameToConfig;
  }

  /**
   * use this attribute as a matching id
   */
  private String entityMatchingIdAttribute;

  /**
   * use this attribute as a matching id
   * @return
   */
  public String getEntityMatchingIdAttribute() {
    return entityMatchingIdAttribute;
  }

  /**
   * use this attribute as a matching id
   * @param entityMatchingIdAttribute
   */
  public void setEntityMatchingIdAttribute(String entityMatchingIdAttribute) {
    this.entityMatchingIdAttribute = entityMatchingIdAttribute;
  }

  /**
   * use this attribute as a matching id
   */
  private String groupMatchingIdAttribute;
  
  

  /**
   * use this attribute as a matching id
   * @return
   */
  public String getGroupMatchingIdAttribute() {
    return groupMatchingIdAttribute;
  }

  /**
   * use this attribute as a matching id
   * @param groupMatchingIdAttribute
   */
  public void setGroupMatchingIdAttribute(String groupMatchingIdAttribute) {
    this.groupMatchingIdAttribute = groupMatchingIdAttribute;
  }

  /**
   * id or "provisioningGroupId,provisioningEntityId"
   */
  private String membershipMatchingIdField;

  
  /**
   * id or "provisioningGroupId,provisioningEntityId"
   * @return
   */
  public String getMembershipMatchingIdField() {
    return membershipMatchingIdField;
  }



  /**
   * id or "provisioningGroupId,provisioningEntityId"
   * @param membershipMatchingIdField
   */
  public void setMembershipMatchingIdField(String membershipMatchingIdField) {
    this.membershipMatchingIdField = membershipMatchingIdField;
  }

  /**
   * id, subjectId, loginId
   */
  private String entityMatchingIdField;
  
  /**
   * id, subjectId, loginId
   * @return
   */
  public String getEntityMatchingIdField() {
    return entityMatchingIdField;
  }

  /**
   * id, subjectId, loginId
   * @param entityMatchingIdField
   */
  public void setEntityMatchingIdField(String entityMatchingIdField) {
    this.entityMatchingIdField = entityMatchingIdField;
  }

  /**
   * id, idIndex, or name
   */
  private String groupMatchingIdField;

  

  /**
   * id, idIndex, or name
   * @return
   */
  public String getGroupMatchingIdField() {
    return groupMatchingIdField;
  }



  /**
   * id, idIndex, or name
   * @param groupMatchingIdField
   */
  public void setGroupMatchingIdField(String groupMatchingIdField) {
    this.groupMatchingIdField = groupMatchingIdField;
  }

  /**
   * 
   */
  private String membershipMatchingIdAttribute;

  
  
  public String getMembershipMatchingIdAttribute() {
    return membershipMatchingIdAttribute;
  }

  
  public void setMembershipMatchingIdAttribute(String membershipMatchingIdAttribute) {
    this.membershipMatchingIdAttribute = membershipMatchingIdAttribute;
  }

  /**
   * expression to get the group id from target group
   */
  private String groupMatchingIdExpression;

  /**
   * expression to get the membership id from the target group
   */
  private String membershipMatchingIdExpression;

  /**
   * expression to get the entity id from the target entity
   */
  private String entityMatchingIdExpression;
  
  
  
  public String getGroupMatchingIdExpression() {
    return groupMatchingIdExpression;
  }


  
  public void setGroupMatchingIdExpression(String groupMatchingIdExpression) {
    this.groupMatchingIdExpression = groupMatchingIdExpression;
  }


  
  public String getMembershipMatchingIdExpression() {
    return membershipMatchingIdExpression;
  }


  
  public void setMembershipMatchingIdExpression(String membershipMatchingIdExpression) {
    this.membershipMatchingIdExpression = membershipMatchingIdExpression;
  }


  
  public String getEntityMatchingIdExpression() {
    return entityMatchingIdExpression;
  }


  
  public void setEntityMatchingIdExpression(String entityMatchingIdExpression) {
    this.entityMatchingIdExpression = entityMatchingIdExpression;
  }

  private boolean logAllObjectsVerbose = false;
  
  
  
  
  public boolean isLogAllObjectsVerbose() {
    return logAllObjectsVerbose;
  }

  
  public void setLogAllObjectsVerbose(boolean logAllObjectsVerbose) {
    this.logAllObjectsVerbose = logAllObjectsVerbose;
  }

  private boolean debugLog = false;
  
  
  
  
  public boolean isDebugLog() {
    return debugLog;
  }



  
  public void setDebugLog(boolean debugLog) {
    this.debugLog = debugLog;
  }

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

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

  private String configId;

  public void setConfigId(String configId) {
    this.configId = configId;
  }
  public String getConfigId() {
    return configId;
  }
  
  /**
   * key is groupEntity or membership
   */
  private Map<String, List<String>> grouperProvisioningToTargetTranslation = new HashMap<String, List<String>>();
  
  
  public Map<String, List<String>> getGrouperProvisioningToTargetTranslation() {
    return grouperProvisioningToTargetTranslation;
  }
  
  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   */
  private int scoreConvertToFullSyncThreshold;
  
  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   * @return
   */
  public int getScoreConvertToFullSyncThreshold() {
    return scoreConvertToFullSyncThreshold;
  }

  /**
   * In incremental processing, each provisionable group/entity to sync memberships to sync counts as 10, each provisionable membership to sync counts as 1.  
   * If the total score is more than this number, it will convert the incrementals to a a full sync.  e.g. 10000 individual memberships 
   * to sync (and not more than 500 in a single group), or 1000 groups to sync, or a combination.  -1 means do not convert to full sync
   * @param scoreConvertToFullSyncThreshold1
   */
  public void setScoreConvertToFullSyncThreshold(int scoreConvertToFullSyncThreshold1) {
    this.scoreConvertToFullSyncThreshold = scoreConvertToFullSyncThreshold1;
  }

  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500.
   * -1 to not use this feature
   */
  private int membershipsConvertToGroupSyncThreshold;
  
  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500
   * -1 to not use this feature
   * @return threshold
   */
  public int getMembershipsConvertToGroupSyncThreshold() {
    return membershipsConvertToGroupSyncThreshold;
  }

  /**
   * If there are this number of memberships or more for a single provisionable group, then perform a "group sync" instead of the individual operations instead, for efficiency
   * default to provisionerDefault.membershipsConvertToGroupSyncThreshold which is 500
   * -1 to not use this feature
   * @param membershipsConvertToGroupSyncThreshold
   */
  public void setMembershipsConvertToGroupSyncThreshold(
      int membershipsConvertToGroupSyncThreshold) {
    this.membershipsConvertToGroupSyncThreshold = membershipsConvertToGroupSyncThreshold;
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Boolean retrieveConfigBoolean(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.booleanObjectValue(configValueString);
  }

  /**
   * get a config name for this or dependency
   * @param configName
   * @param required 
   * @return the config
   */
  public Integer retrieveConfigInt(String configName, boolean required) {
    String configValueString = retrieveConfigString(configName, required);
    return GrouperClientUtils.intObjectValue(configValueString, true);
  }

  /**
   * get a config name for this or dependency
   * @param configSuffix
   * @param required 
   * @return the config
   */
  public String retrieveConfigString(String configSuffix, boolean required) {
    
    String value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner." + this.getConfigId() + "." + configSuffix);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    value = GrouperLoaderConfig.retrieveConfig().propertyValueString("provisionerDefault." + configSuffix);
    if (!StringUtils.isBlank(value)) {
      return value;
    }
    if (required) {
      throw new RuntimeException("Cant find config for provisioning: provisioner." + this.getConfigId() + "." + configSuffix);
    }
    return null;
  
  }

  private Map<String, Object> debugMap = null;

  public void preConfigure() {
    // must have key
    if (StringUtils.isBlank(this.grouperProvisioner.getConfigId())) {
      throw new RuntimeException("Why is config id blank?");
    }

    // must have provisioning type
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType() == null) {
      throw new RuntimeException("Why is provisioning type blank?");
    }

    this.setConfigId(this.grouperProvisioner.getConfigId());

    if (this.grouperProvisioner.getGcGrouperSync() == null) {
      this.grouperProvisioner.setGcGrouperSync(GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.getConfigId()));
    }
    
    if (StringUtils.isBlank(this.grouperProvisioner.getGcGrouperSync().getSyncEngine())) {
      this.grouperProvisioner.getGcGrouperSync().setSyncEngine(GcGrouperSync.PROVISIONING);
      this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncDao().store();
    }
    
    if (!GrouperClientUtils.equals(GcGrouperSync.PROVISIONING, this.grouperProvisioner.getGcGrouperSync().getSyncEngine())) {
      throw new RuntimeException("Why is sync engine not 'provisioning'?  " + this.getConfigId() + ", " + this.grouperProvisioner.getGcGrouperSync().getSyncEngine());
    }

    GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
    if (gcGrouperSyncJob == null) {
      gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncJobDao()
          .jobRetrieveOrCreateBySyncType(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().name());
      this.grouperProvisioner.setGcGrouperSyncJob(gcGrouperSyncJob);
    }
    if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner." + this.getConfigId() + ".debugConfig", false)) {
      
      debugMap = this.grouperProvisioner.getDebugMap();
    } else {
      debugMap = new LinkedHashMap<String, Object>();
    }
  }

  /**
   * If the subject API is needed to resolve attribute on subject  required, drives requirements of other configurations. defaults to false.
   */
  private boolean hasSubjectLink = false;
  
  /**
   * If groups need to be resolved in the target before provisioning
   */
  private boolean hasTargetGroupLink = false;
  
  /**
   * If users need to be resolved in the target before provisioning
   */
  private boolean hasTargetEntityLink = false;
  
  /**
   * subject sources to provision  optional, defaults to all except g:gsa, grouperExternal, g:isa, localEntities. comma separated list. checkboxes. 
   */
  private Set<String> subjectSourcesToProvision = null;
  
  
  private String subjectLinkMemberFromId2;

  private String subjectLinkMemberFromId3;

  private String subjectLinkMemberToId2;

  private String subjectLinkMemberToId3;

  
  public String getSubjectLinkMemberFromId2() {
    return subjectLinkMemberFromId2;
  }



  
  public void setSubjectLinkMemberFromId2(String subjectLinkMemberFromId2) {
    this.subjectLinkMemberFromId2 = subjectLinkMemberFromId2;
  }



  
  public String getSubjectLinkMemberFromId3() {
    return subjectLinkMemberFromId3;
  }



  
  public void setSubjectLinkMemberFromId3(String subjectLinkMemberFromId3) {
    this.subjectLinkMemberFromId3 = subjectLinkMemberFromId3;
  }



  
  public String getSubjectLinkMemberToId2() {
    return subjectLinkMemberToId2;
  }



  
  public void setSubjectLinkMemberToId2(String subjectLinkMemberToId2) {
    this.subjectLinkMemberToId2 = subjectLinkMemberToId2;
  }



  
  public String getSubjectLinkMemberToId3() {
    return subjectLinkMemberToId3;
  }



  
  public void setSubjectLinkMemberToId3(String subjectLinkMemberToId3) {
    this.subjectLinkMemberToId3 = subjectLinkMemberToId3;
  }
  
  /**
   * attributes to use when searching, targetId is first if multiple
   */
  private List<GrouperProvisioningConfigurationAttribute> entitySearchAttributes = null;
  
  /**
   * attributes to use when searching, targetId is first if multiple
   */
  private List<GrouperProvisioningConfigurationAttribute> groupSearchAttributes = null;
  
  /**
   * attributes to use when selecting from target
   */
  private Set<String> groupSelectAttributes = null;
  
  /**
   * attributes to use when selecting from target
   * @return
   */
  public Set<String> getGroupSelectAttributes() {
    return groupSelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   * @param groupSelectAttributes
   */
  public void setGroupSelectAttributes(Set<String> groupSelectAttributes) {
    this.groupSelectAttributes = groupSelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   * @return
   */
  public Set<String> getEntitySelectAttributes() {
    return entitySelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   * @param entitySelectAttributes
   */
  public void setEntitySelectAttributes(Set<String> entitySelectAttributes) {
    this.entitySelectAttributes = entitySelectAttributes;
  }

  /**
   * attributes to use when selecting from target
   */
  private Set<String> entitySelectAttributes = null;
  
  /**
   * someAttr  everything is assumed to be single valued except objectclass and the provisionedAttributeName optional
   */
  private Set<String> entityAttributesMultivalued = null;
  
  /**
   * someAttr  everything is assumed to be single valued except objectclass and the provisionedAttributeName optional
   */
  private Set<String> groupAttributesMultivalued = null;

  /**
   * if there are fewer than this many subjects to process, just resolve them
   */
  private int refreshSubjectLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many groups to process, just resolve them
   */
  private int refreshGroupLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many entities to process, just resolve them
   */
  private int refreshEntityLinkIfLessThanAmount;
  
  /**
   * if there are fewer than this many groups to process, just resolve them
   * @return
   */
  public int getRefreshGroupLinkIfLessThanAmount() {
    return refreshGroupLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many groups to process, just resolve them
   * @param refreshGroupLinkIfLessThanAmount
   */
  public void setRefreshGroupLinkIfLessThanAmount(int refreshGroupLinkIfLessThanAmount) {
    this.refreshGroupLinkIfLessThanAmount = refreshGroupLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many entities to process, just resolve them
   * @return
   */
  public int getRefreshEntityLinkIfLessThanAmount() {
    return refreshEntityLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many entities to process, just resolve them
   * @param refreshEntityLinkIfLessThanAmount
   */
  public void setRefreshEntityLinkIfLessThanAmount(int refreshEntityLinkIfLessThanAmount) {
    this.refreshEntityLinkIfLessThanAmount = refreshEntityLinkIfLessThanAmount;
  }



  /**
   * if there are fewer than this many subjects to process, just resolve them
   * @return
   */
  public int getRefreshSubjectLinkIfLessThanAmount() {
    return refreshSubjectLinkIfLessThanAmount;
  }

  /**
   * if there are fewer than this many subjects to process, just resolve them
   * @param refreshSubjectLinkIfLessThanAmount
   */
  public void setRefreshSubjectLinkIfLessThanAmount(
      int refreshSubjectLinkIfLessThanAmount) {
    this.refreshSubjectLinkIfLessThanAmount = refreshSubjectLinkIfLessThanAmount;
  }
  
  /**
   * if entities should be inserted in target 
   */
  private boolean insertEntities = false;

  /**
   * if memberships should be replaced in target
   */
  private boolean replaceMemberships = false;
  
  /**
   * if memberships should be replaced in target
   * @return
   */
  public boolean isReplaceMemberships() {
    return replaceMemberships;
  }
  
  /**
   * if memberships should be replaced in target
   * @return
   */
  public void setReplaceMemberships(boolean replaceMemberships) {
    this.replaceMemberships = replaceMemberships;
  }

  /**
   * if memberships should be inserted in target
   * @return
   */
  public boolean isInsertMemberships() {
    return insertMemberships;
  }

  /**
   * if memberships should be inserted in target
   * @param insertMemberships
   */
  public void setInsertMemberships(boolean insertMemberships) {
    this.insertMemberships = insertMemberships;
  }

  /**
   * if memberships should be deleted in target
   * @return
   */
  public boolean isDeleteMemberships() {
    return deleteMemberships;
  }

  /**
   * if memberships should be deleted in target
   * @param deleteMemberships
   */
  public void setDeleteMemberships(boolean deleteMemberships) {
    this.deleteMemberships = deleteMemberships;
  }

  /**
   * update memberships
   */
  private boolean updateMemberships = false;
  
  /**
   * update groups
   */
  private boolean updateGroups = false;

  /**
   * update entities
   */
  private boolean updateEntities = false;
  
  /**
   * update memberships
   * @return
   */
  public boolean isUpdateMemberships() {
    return updateMemberships;
  }

  /**
   * update memberships
   * @param updateMemberships
   */
  public void setUpdateMemberships(boolean updateMemberships) {
    this.updateMemberships = updateMemberships;
  }

  /**
   * update groups
   * @return
   */
  public boolean isUpdateGroups() {
    return updateGroups;
  }

  /**
   * update groups
   * @param updateGroups
   */
  public void setUpdateGroups(boolean updateGroups) {
    this.updateGroups = updateGroups;
  }

  /**
   * update entities
   * @return
   */
  public boolean isUpdateEntities() {
    return updateEntities;
  }

  /**
   * update entities
   * @param updateEntities
   */
  public void setUpdateEntities(boolean updateEntities) {
    this.updateEntities = updateEntities;
  }

  /**
   * delete groups
   */
  private boolean deleteGroups = false;

  /**
   * delete groups
   * @return
   */
  public boolean isDeleteGroups() {
    return deleteGroups;
  }

  /**
   * delete groups
   * @param deleteGroups
   */
  public void setDeleteGroups(boolean deleteGroups) {
    this.deleteGroups = deleteGroups;
  }

  /**
   * delete entities if grouper deleted them
   */
  private boolean deleteEntitiesIfGrouperDeleted = false;

  /**
   * delete entities if not exist in grouper
   */
  private boolean deleteEntitiesIfNotExistInGrouper = false;
  
  /**
   * delete memberships if grouper deleted them
   */
  private boolean deleteMembershipsIfGrouperDeleted = false;

  /**
   * delete memberships if not exist in grouper
   */
  private boolean deleteMembershipsIfNotExistInGrouper = false;

  /**
   * delete entities
   */
  private boolean deleteEntities = false;

  /**
   * select entities
   */
  private boolean selectEntities = false;
  
  /**
   * select memberships
   */
  private boolean selectMemberships = false;

  
  /**
   * delete entities if grouper deleted them
   * @return
   */
  public boolean isDeleteEntitiesIfGrouperDeleted() {
    return deleteEntitiesIfGrouperDeleted;
  }

  /**
   * delete entities if grouper deleted them
   * @param deleteEntitiesIfGrouperDeleted
   */
  public void setDeleteEntitiesIfGrouperDeleted(boolean deleteEntitiesIfGrouperDeleted) {
    this.deleteEntitiesIfGrouperDeleted = deleteEntitiesIfGrouperDeleted;
  }

  /**
   * delete entities if not exist in grouper
   * @return
   */
  public boolean isDeleteEntitiesIfNotExistInGrouper() {
    return deleteEntitiesIfNotExistInGrouper;
  }

  /**
   * delete entities if not exist in grouper
   * @param deleteEntitiesIfNotExistInGrouper
   */
  public void setDeleteEntitiesIfNotExistInGrouper(
      boolean deleteEntitiesIfNotExistInGrouper) {
    this.deleteEntitiesIfNotExistInGrouper = deleteEntitiesIfNotExistInGrouper;
  }

  /**
   * delete memberships if grouper deleted them
   * @return
   */
  public boolean isDeleteMembershipsIfGrouperDeleted() {
    return deleteMembershipsIfGrouperDeleted;
  }

  /**
   * delete memberships if grouper deleted them
   * @param deleteMembershipsIfGrouperDeleted
   */
  public void setDeleteMembershipsIfGrouperDeleted(
      boolean deleteMembershipsIfGrouperDeleted) {
    this.deleteMembershipsIfGrouperDeleted = deleteMembershipsIfGrouperDeleted;
  }

  /**
   * delete memberships if not exist in grouper
   * @return
   */
  public boolean isDeleteMembershipsIfNotExistInGrouper() {
    return deleteMembershipsIfNotExistInGrouper;
  }

  /**
   * delete memberships if not exist in grouper
   * @param deleteMembershipsIfNotExistInGrouper
   */
  public void setDeleteMembershipsIfNotExistInGrouper(
      boolean deleteMembershipsIfNotExistInGrouper) {
    this.deleteMembershipsIfNotExistInGrouper = deleteMembershipsIfNotExistInGrouper;
  }

  /**
   * delete entities
   * @return
   */
  public boolean isDeleteEntities() {
    return deleteEntities;
  }

  /**
   * delete entities
   * @param deleteEntities
   */
  public void setDeleteEntities(boolean deleteEntities) {
    this.deleteEntities = deleteEntities;
  }

  /**
   * select entities
   * @return
   */
  public boolean isSelectEntities() {
    return selectEntities;
  }

  /**
   * select entities
   * @param selectEntities
   */
  public void setSelectEntities(boolean selectEntities) {
    this.selectEntities = selectEntities;
  }

  /**
   * select memberships
   * @return
   */
  public boolean isSelectMemberships() {
    return selectMemberships;
  }

  /**
   * select memberships
   * @param selectMemberships
   */
  public void setSelectMemberships(boolean selectMemberships) {
    this.selectMemberships = selectMemberships;
  }

  /**
   * if memberships should be inserted in target
   */
  private boolean insertMemberships = false;

  /**
   * if memberships should be deleted in target
   */
  private boolean deleteMemberships = false;

  /**
   * if groups should be inserted in target
   */
  private boolean insertGroups = false;
  
  /**
   * if groups should be selected from target
   */
  private boolean selectGroups = false;
  
  /**
   * if groups should be selected from target
   * @return
   */
  public boolean isSelectGroups() {
    return selectGroups;
  }

  /**
   * if groups should be selected from target
   * @param selectGroups
   */
  public void setSelectGroups(boolean selectGroups) {
    this.selectGroups = selectGroups;
  }

  /**
   * search filter to look up entity if cannot just use the matchingId
   */
  private String entitySearchFilter = null;
  
  /**
   * search filter to look up entity if cannot just use the matchingId
   * @return
   */
  public String getEntitySearchFilter() {
    return entitySearchFilter;
  }

  /**
   * search filter to look up entity if cannot just use the matchingId
   * @param userSearchFilter
   */
  public void setEntitySearchFilter(String userSearchFilter) {
    this.entitySearchFilter = userSearchFilter;
  }

  private String entitySearchAllFilter;

  /**
   * search filter to look up all entities
   * @return
   */
  public String getEntitySearchAllFilter() {
    return entitySearchAllFilter;
  }

  /**
   * search filter to look up all entities
   * @param userSearchAllFilter
   */
  public void setEntitySearchAllFilter(String userSearchAllFilter) {
    this.entitySearchAllFilter = userSearchAllFilter;
  }
  
  
  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    GrouperProvisioningConfigurationBase provisionerConfiguration = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    Set<String> fieldNames = GrouperUtil.fieldNames(provisionerConfiguration.getClass(), 
        GrouperProvisioningConfigurationBase.class, null, true, false, false);
    
    // assume configurations cache stuff in fields.  We can make this more flexible / customizable at some point
    fieldNames.remove("configId");
    fieldNames.remove("debugLog");
    fieldNames.remove("debugMap");
    fieldNames.remove("grouperProvisioner");
    fieldNames.remove("targetEntityAttributeNameToConfig");
    fieldNames.remove("targetEntityFieldNameToConfig");
    fieldNames.remove("targetGroupAttributeNameToConfig");
    fieldNames.remove("targetGroupFieldNameToConfig");
    fieldNames.remove("targetMembershipAttributeNameToConfig");
    fieldNames.remove("targetMembershipFieldNameToConfig");
    fieldNames.remove("grouperProvisioningToTargetTranslation");
    fieldNames.remove("metadataNameToMetadataItem");
    
    
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      
      Object value = GrouperUtil.propertyValue(provisionerConfiguration, fieldName);
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
    for (String key : new TreeSet<String>(this.metadataNameToMetadataItem.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = this.metadataNameToMetadataItem.get(key);
      result.append(" - metadata item: " + key + ": " + grouperProvisioningObjectMetadataItem.toString());
    }
    for (String key : new TreeSet<String>(this.grouperProvisioningToTargetTranslation.keySet())) {
      List<String> translations = this.grouperProvisioningToTargetTranslation.get(key);
      for (int i=0;i<translations.size();i++) {
        if (result.charAt(result.length()-1) != '\n') {
          result.append("\n");
        }
        result.append(" - grouperProvisioningToTargetTranslation").append(key).append(".").append(i).append(".script = '").append(translations.get(i)).append("'");
      }
    }
    for (String key : new TreeSet<String>(this.targetGroupFieldNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetGroupFieldNameToConfig.get(key);
      result.append(" - target group field config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetGroupAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetGroupAttributeNameToConfig.get(key);
      result.append(" - target group attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetEntityFieldNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetEntityFieldNameToConfig.get(key);
      result.append(" - target entity field config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetEntityAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetEntityAttributeNameToConfig.get(key);
      result.append(" - target entity attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetMembershipFieldNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetMembershipFieldNameToConfig.get(key);
      result.append(" - target membership field config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    for (String key : new TreeSet<String>(this.targetMembershipAttributeNameToConfig.keySet())) {
      if (result.charAt(result.length()-1) != '\n') {
        result.append("\n");
      }
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.targetMembershipAttributeNameToConfig.get(key);
      result.append(" - target membership attribute config: " + key + ": " + grouperProvisioningConfigurationAttribute.toString());
    }
    
    return result.toString();
  }

  /**
   * search filter to look up group if cannot just use the matchingId
   */
  private String groupSearchFilter = null;
  
  
  
  /**
   * search filter to look up group if cannot just use the matchingId
   * @return
   */
  public String getGroupSearchFilter() {
    return groupSearchFilter;
  }



  /**
   * search filter to look up group if cannot just use the matchingId
   * @param groupSearchFilter
   */
  public void setGroupSearchFilter(String groupSearchFilter) {
    this.groupSearchFilter = groupSearchFilter;
  }
  
  private String groupSearchAllFilter;

  /**
   * search filter to look up all groups
   * @return
   */
  public String getGroupSearchAllFilter() {
    return groupSearchAllFilter;
  }

  /**
   * search filter to look up all groups
   * @param groupSearchAllFilter
   */
  public void setGroupSearchAllFilter(String groupSearchAllFilter) {
    this.groupSearchAllFilter = groupSearchAllFilter;
  }
  
  /**
   * 
   */
  private boolean deleteEntitiesIfGrouperCreated = false;
  
  /**
   * 
   */
  private boolean deleteGroupsIfGrouperCreated = false;

  /**
   * 
   */
  private boolean deleteMembershipsIfGrouperCreated = false;
  
  /**
   * 
   * @return
   */
  public boolean isDeleteEntitiesIfGrouperCreated() {
    return deleteEntitiesIfGrouperCreated;
  }

  /**
   * 
   * @param deleteEntitiesIfGrouperCreated
   */
  public void setDeleteEntitiesIfGrouperCreated(boolean deleteEntitiesIfGrouperCreated) {
    this.deleteEntitiesIfGrouperCreated = deleteEntitiesIfGrouperCreated;
  }

  /**
   * 
   * @return
   */
  public boolean isDeleteGroupsIfGrouperCreated() {
    return deleteGroupsIfGrouperCreated;
  }

  
  public void setDeleteGroupsIfGrouperCreated(boolean deleteGroupsIfGrouperCreated) {
    this.deleteGroupsIfGrouperCreated = deleteGroupsIfGrouperCreated;
  }

  
  public boolean isDeleteMembershipsIfGrouperCreated() {
    return deleteMembershipsIfGrouperCreated;
  }

  
  public void setDeleteMembershipsIfGrouperCreated(
      boolean deleteMembershipsIfGrouperCreated) {
    this.deleteMembershipsIfGrouperCreated = deleteMembershipsIfGrouperCreated;
  }

  /**
   * true or false if groups in full sync should be deleted if in group all filter and not in grouper
   * or for attributes delete other attribute not provisioned by grouper default to false
   */
  private boolean deleteGroupsIfNotExistInGrouper = false;

  /**
   * true or false if groups that were created in grouper were deleted should it be deleted in ldap?
   * or for attributes, delete attribute value if deleted in grouper default to true
   */
  private boolean deleteGroupsIfGrouperDeleted = true;

  /**
   * if provisioning normal memberships or privileges  default to "members" for normal memberships, otherwise which privileges
   */
  private GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType = null;

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   */
  private String attributeNameForMemberships;

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   * @return
   */
  public String getAttributeNameForMemberships() {
    return attributeNameForMemberships;
  }

  /**
   * attribute name in a group/entity object that refers to memberships (if applicable)
   * @param attributeNameForMemberships
   */
  public void setAttributeNameForMemberships(String attributeNameForMemberships) {
    this.attributeNameForMemberships = attributeNameForMemberships;
  }

  /**
   * attribute name in a group object that refers to memberships (if applicable)
   */
  private String groupAttributeNameForMemberships;
  
  /**
   * attribute name in a group object that refers to memberships (if applicable)
   * @return
   */
  public String getGroupAttributeNameForMemberships() {
    return groupAttributeNameForMemberships;
  }

  /**
   * attribute name in a group object that refers to memberships (if applicable)
   * @param groupAttributeNameForMemberships
   */
  public void setGroupAttributeNameForMemberships(String groupAttributeNameForMemberships) {
    this.groupAttributeNameForMemberships = groupAttributeNameForMemberships;
  }
  
  /**
   * attribute name in a user object that refers to memberships (if applicable)
   */
  private String entityAttributeNameForMemberships;
  
  /**
   * attribute name in a user object that refers to memberships (if applicable)
   * @return
   */
  public String getEntityAttributeNameForMemberships() {
    return entityAttributeNameForMemberships;
  }

  /**
   * attribute name in a user object that refers to memberships (if applicable)
   * @param userAttributeNameForMemberships
   */
  public void setEntityAttributeNameForMemberships(String userAttributeNameForMemberships) {
    this.entityAttributeNameForMemberships = userAttributeNameForMemberships;
  }

  /**
   * 
   */
  public abstract void configureSpecificSettings();

  private GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType;

  /**
   * number of metadata
   */
  private int numberOfMetadata;  
  
  
  /**
   * number of metadata
   * @return
   */
  public int getNumberOfMetadata() {
    return numberOfMetadata;
  }

  /**
   * number of metadata
   * @param numberOfMetadata
   */
  public void setNumberOfMetadata(int numberOfMetadata) {
    this.numberOfMetadata = numberOfMetadata;
  }

  public GrouperProvisioningBehaviorMembershipType getGrouperProvisioningBehaviorMembershipType() {
    return grouperProvisioningBehaviorMembershipType;
  }

  
  public void setGrouperProvisioningBehaviorMembershipType(
      GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType) {
    this.grouperProvisioningBehaviorMembershipType = grouperProvisioningBehaviorMembershipType;
  }
  
  public void configureProvisionableSettings() {
    if (this.getConfigId() == null) {
      this.setConfigId(this.grouperProvisioner.getConfigId());
    }
    
    this.onlyProvisionPolicyGroups = this.retrieveConfigBoolean("onlyProvisionPolicyGroups", false);

    this.allowPolicyGroupOverride = this.retrieveConfigBoolean("allowPolicyGroupOverride", false);

    this.allowProvisionableRegexOverride = this.retrieveConfigBoolean("allowProvisionableRegexOverride", false);

    this.provisionableRegex = this.retrieveConfigString("provisionableRegex", false);
  }

  public void configureGenericSettings() {
    configureProvisionableSettings();
    
    {
      Boolean operateOnGrouperMemberships = this.retrieveConfigBoolean("operateOnGrouperMemberships", false);
      if (operateOnGrouperMemberships == null) {
        operateOnGrouperMemberships = false;
      }
      
      if (operateOnGrouperMemberships) {
        String provisioningTypeString = this.retrieveConfigString("provisioningType", true);
        this.grouperProvisioningBehaviorMembershipType = GrouperProvisioningBehaviorMembershipType.valueOf(provisioningTypeString);
      }
      
    }

    this.numberOfMetadata = GrouperUtil.intValue(this.retrieveConfigInt("numberOfMetadata", false), 0);
    
    for (int i=0;i<this.numberOfMetadata;i++) {
      
      GrouperProvisioningObjectMetadataItem grouperProvisioningObjectMetadataItem = new GrouperProvisioningObjectMetadataItem();
      {
        String name = this.retrieveConfigString("metadata."+i+".name", false);
        if (!name.startsWith("md_")) {
          //TODO validate this
          this.debugMap.put("invalid_metadataName_" + name, true);
          continue;
        }
        grouperProvisioningObjectMetadataItem.setName(name);
        if (this.metadataNameToMetadataItem.containsKey(name)) {
          throw new RuntimeException("Conflicting metadata names! " + name);
        }
        this.metadataNameToMetadataItem.put(name, grouperProvisioningObjectMetadataItem);
      }
      
      grouperProvisioningObjectMetadataItem.setLabelKey(grouperProvisioningObjectMetadataItem.getName() + "_" + this.getGrouperProvisioner().getConfigId() + "_label");
      grouperProvisioningObjectMetadataItem.setDescriptionKey(grouperProvisioningObjectMetadataItem.getName() + "_" + this.getGrouperProvisioner().getConfigId() + "_description");
      
      {
        boolean showForFolder = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForFolder", false), false);
        grouperProvisioningObjectMetadataItem.setShowForFolder(showForFolder);
      }
      
      {
        boolean showForGroup = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForGroup", false), false);
        grouperProvisioningObjectMetadataItem.setShowForGroup(showForGroup);
      }
      
      {
        boolean showForMember = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForMember", false), false);
        grouperProvisioningObjectMetadataItem.setShowForMember(showForMember);
      }
      
      {
        boolean showForMembership = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".showForMembership", false), false);
        grouperProvisioningObjectMetadataItem.setShowForMembership(showForMembership);
      }

      {
        String valueType = this.retrieveConfigString("metadata."+i+".valueType", false);
        GrouperProvisioningObjectMetadataItemValueType grouperProvisioningObjectMetadataItemValueType = 
            StringUtils.isBlank(valueType) ? GrouperProvisioningObjectMetadataItemValueType.STRING 
                : GrouperProvisioningObjectMetadataItemValueType.valueOfIgnoreCase(valueType, true);
        grouperProvisioningObjectMetadataItem.setValueType(grouperProvisioningObjectMetadataItemValueType);
        
        if (grouperProvisioningObjectMetadataItemValueType == GrouperProvisioningObjectMetadataItemValueType.BOOLEAN) {
          grouperProvisioningObjectMetadataItem.setFormElementType(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON);
         
          String trueLabel = GrouperTextContainer.textOrNull("config.defaultTrueLabel");
          String falseLabel = GrouperTextContainer.textOrNull("config.defaultFalseLabel");
          
          String defaultValue = this.retrieveConfigString("metadata."+i+".defaultValue", false);
          
          List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
          
          if (StringUtils.isNotBlank(defaultValue)) {
            
            Boolean booleanObjectValue = GrouperUtil.booleanObjectValue(defaultValue);
            if (booleanObjectValue != null) {
              String defaultValueStr = booleanObjectValue ? "("+trueLabel+")" : "("+falseLabel+")"; 
              keysAndLabels.add(new MultiKey("", GrouperTextContainer.textOrNull("config.defaultValueLabel")+" " + defaultValueStr ));
            }
          }
          
          keysAndLabels.add(new MultiKey("true", trueLabel));
          keysAndLabels.add(new MultiKey("false", falseLabel));
          grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(keysAndLabels);
        } else {
          String formElementType = this.retrieveConfigString("metadata."+i+".formElementType", false);
          GrouperProvisioningObjectMetadataItemFormElementType grouperProvisioningObjectMetadataItemFormElementType =
              StringUtils.isBlank(formElementType) ? GrouperProvisioningObjectMetadataItemFormElementType.TEXT 
                  : GrouperProvisioningObjectMetadataItemFormElementType.valueOfIgnoreCase(formElementType, true);
          grouperProvisioningObjectMetadataItem.setFormElementType(grouperProvisioningObjectMetadataItemFormElementType);
        }
      }
      
      {
        String defaultValue = this.retrieveConfigString("metadata."+i+".defaultValue", false);
        grouperProvisioningObjectMetadataItem.setDefaultValue(defaultValue);
      }
      
      {
        String dropdownValues = this.retrieveConfigString("metadata."+i+".dropdownValues", false);
        if (!StringUtils.isBlank(dropdownValues)) {
          String[] dropdownValuesArray = GrouperUtil.splitTrim(dropdownValues, ",");
          List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
          keysAndLabels.add(new MultiKey("", ""));
          for (String dropdownValue : dropdownValuesArray) {
            dropdownValue = GrouperUtil.replace(dropdownValue, "&#x2c;", ",");
            MultiKey keyAndLabel = new MultiKey(dropdownValue, dropdownValue);
            keysAndLabels.add(keyAndLabel);
          }
          grouperProvisioningObjectMetadataItem.setKeysAndLabelsForDropdown(keysAndLabels);
        }
      }
      
      {
        boolean required = GrouperUtil.booleanValue(this.retrieveConfigBoolean("metadata."+i+".required", false), false);
        grouperProvisioningObjectMetadataItem.setRequired(required);
      }

      {
        String groupIdThatCanView = this.retrieveConfigString("metadata."+i+".groupIdThatCanView", false);
        grouperProvisioningObjectMetadataItem.setGroupIdThatCanView(groupIdThatCanView);
      }
      {
        String groupIdThatCanUpdate = this.retrieveConfigString("metadata."+i+".groupIdThatCanUpdate", false);
        grouperProvisioningObjectMetadataItem.setGroupIdThatCanUpdate(groupIdThatCanUpdate);
      }
      
    }
    
    for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute", "targetMembershipAttribute"}) {
      
      boolean foundMatchingId = false;
      String foundMatchingIdName = null;
      
      boolean foundMembershipAttribute = false;
      String foundMembershipAttributeName = null;
      
      for (int i=0; i< 20; i++) {
  
        GrouperProvisioningConfigurationAttribute attributeConfig = new GrouperProvisioningConfigurationAttribute();
  
        attributeConfig.setConfigIndex(i);
        
        if (StringUtils.equals("targetGroupAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        } else if (StringUtils.equals("targetMembershipAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
        } else {
          throw new RuntimeException("Cant find object type: " + objectType);
        }

        Boolean isField = this.retrieveConfigBoolean(objectType + "."+i+".isFieldElseAttribute" , false);
        if (isField == null) {
          break;
        }
        attributeConfig.setAttribute(!isField);

        String name = this.retrieveConfigString(objectType + "."+i+(isField ? ".fieldName" : ".name") , true);
        attributeConfig.setName(name);
        
        {
          boolean insert = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".insert" , false), false);
          attributeConfig.setInsert(insert);
        }

        {
          Integer maxlength = this.retrieveConfigInt(objectType + "."+i+".maxlength", false);
          attributeConfig.setMaxlength(maxlength);
        }
        
        {
          String validExpression = this.retrieveConfigString(objectType + "."+i+".validExpression", false);
          attributeConfig.setValidExpression(validExpression);
        }
        
        {
          boolean required = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".required" , false), false);
          attributeConfig.setRequired(required);
        }
  
        {
          boolean searchAttribute = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".searchAttribute" , false), false);
          attributeConfig.setSearchAttribute(searchAttribute);
        }
  
        {
          boolean membershipAttribute = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".membershipAttribute" , false), false);
          String translateExpressionFromMembership = this.retrieveConfigString(objectType + "." + i + ".translateExpressionFromMembership", false);
          if (membershipAttribute) {
            if (foundMembershipAttribute) {
              throw new RuntimeException("Can only have one " + objectType + " membershipAttribute attribute or field! " + name + ", " + foundMembershipAttributeName);
            }
            foundMembershipAttribute = true;
            foundMembershipAttributeName = name;
            
            attributeConfig.setTranslateExpressionFromMembership(translateExpressionFromMembership);
          }
          attributeConfig.setMembershipAttribute(membershipAttribute);
        }
  
        {
          boolean multiValued = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".multiValued" , false), false);
          attributeConfig.setMultiValued(multiValued);
        }
  
        {
          boolean select = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".select" , false), false);
          attributeConfig.setSelect(select);
        }
        
        {
          boolean matchingId = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".matchingId" , false), false);
          if (matchingId) {
            if (foundMatchingId) {
              throw new RuntimeException("Can only have one " + objectType + " matchingId attribute or field! " + name + ", " + foundMatchingIdName);
            }
            foundMatchingId = true;
            foundMatchingIdName = name;
          }
          attributeConfig.setMatchingId(matchingId);
        }
        
        {
          String defaultValue = this.retrieveConfigString(objectType + "."+i+".defaultValue" , false);
          attributeConfig.setDefaultValue(defaultValue);
        }
        
        {
          String translateExpression = this.retrieveConfigString(objectType + "."+i+".translateExpression" , false);
          attributeConfig.setTranslateExpression(translateExpression);
        }
        
        {
          String translateExpressionCreateOnly = this.retrieveConfigString(objectType+"."+i+".translateExpressionCreateOnly" , false);
          attributeConfig.setTranslateExpressionCreateOnly(translateExpressionCreateOnly);
        }
        
        {
          String translateFromGroupSyncField = this.retrieveConfigString(objectType+"."+i+".translateFromGroupSyncField" , false);
          attributeConfig.setTranslateFromGroupSyncField(translateFromGroupSyncField);
        }

        {
          String translateFromGrouperProvisioningGroupField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningGroupField" , false);
          attributeConfig.setTranslateFromGrouperProvisioningGroupField(translateFromGrouperProvisioningGroupField);
        }
        
        {
          String translateFromGrouperProvisioningEntityField = this.retrieveConfigString(objectType+"."+i+".translateFromGrouperProvisioningEntityField" , false);
          attributeConfig.setTranslateFromGrouperProvisioningEntityField(translateFromGrouperProvisioningEntityField);
        }
        
        {
          String translateFromMemberSyncField = this.retrieveConfigString(objectType+"."+i+".translateFromMemberSyncField" , false);
          attributeConfig.setTranslateFromMemberSyncField(translateFromMemberSyncField);
        }
        
        {
          String translateToGroupSyncField = this.retrieveConfigString(objectType+"."+i+".translateToGroupSyncField" , false);
          attributeConfig.setTranslateToGroupSyncField(translateToGroupSyncField);
        }
        
        {
          String translateToMemberSyncField = this.retrieveConfigString(objectType+"."+i+".translateToMemberSyncField" , false);
          attributeConfig.setTranslateToMemberSyncField(translateToMemberSyncField);
        }
        
        {
          String translateGrouperToGroupSyncField = this.retrieveConfigString(objectType+"."+i+".translateGrouperToGroupSyncField" , false);
          attributeConfig.setTranslateGrouperToGroupSyncField(translateGrouperToGroupSyncField);
        }
        
        {
          String translateGrouperToMemberSyncField = this.retrieveConfigString(objectType+"."+i+".translateGrouperToMemberSyncField" , false);
          attributeConfig.setTranslateGrouperToMemberSyncField(translateGrouperToMemberSyncField);
        }
        
        {
          boolean update = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType+"."+i+".update" , false), false);
          attributeConfig.setUpdate(update);
        }
        
        {
          GrouperProvisioningConfigurationAttributeValueType valueType = 
              GrouperProvisioningConfigurationAttributeValueType.valueOfIgnoreCase(
                  this.retrieveConfigString(objectType+ "."+i+".valueType" , false), false);
          if (valueType == null) {
            valueType = GrouperProvisioningConfigurationAttributeValueType.STRING;
          }
          attributeConfig.setValueType(valueType);
        }
        
        {
          String ignoreIfMatchesValuesRaw = this.retrieveConfigString(objectType + "."+i+".ignoreIfMatchesValue" , false);
          if (!StringUtils.isBlank(ignoreIfMatchesValuesRaw)) {
            GrouperProvisioningConfigurationAttributeValueType valueType = GrouperUtil.defaultIfNull(attributeConfig.getValueType(), 
                GrouperProvisioningConfigurationAttributeValueType.STRING);
            
            for (String ignoreIfMatchesValueRaw : GrouperUtil.splitTrim(ignoreIfMatchesValuesRaw, ",")) {
              ignoreIfMatchesValueRaw = StringUtils.replace(ignoreIfMatchesValueRaw, "U+002C", ",");
              Object ignoreIfMatchesValue = valueType.convert(ignoreIfMatchesValueRaw);
              attributeConfig.getIgnoreIfMatchesValues().add(ignoreIfMatchesValue);
            }
          }
        }

        if ("targetGroupAttribute".equals(objectType)) {
          if (!isField) {
            if (targetGroupAttributeNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
            }
          
            targetGroupAttributeNameToConfig.put(name, attributeConfig);
          } else {
            if (targetGroupFieldNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " field: " + name);
            }
            targetGroupFieldNameToConfig.put(name, attributeConfig);
          }
          
        } else if ("targetEntityAttribute".equals(objectType)) {
          if (!isField) {
            if (targetEntityAttributeNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
            }
            targetEntityAttributeNameToConfig.put(name, attributeConfig);
          } else {
            if (targetEntityFieldNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " field: " + name);
            }
            targetEntityFieldNameToConfig.put(name, attributeConfig);
          }
          
        } else if ("targetMembershipAttribute".equals(objectType)) {
          if (!isField) {
            if (targetMembershipAttributeNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " attribute: " + name);
            }
            targetMembershipAttributeNameToConfig.put(name, attributeConfig);
          } else {
            if (targetMembershipFieldNameToConfig.containsKey(name)) {
              throw new RuntimeException("Multiple configurations for " + objectType + " field: " + name);
            }
            targetMembershipFieldNameToConfig.put(name, attributeConfig);
          }
          
        } else {
          throw new RuntimeException("Invalid object type: '" + objectType + "'");
        }
      }
    }
    
    this.hasSubjectLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasSubjectLink", false), false);
    if (this.hasSubjectLink) {
      this.debugMap.put("hasSubjectLink", this.hasSubjectLink);
    }
    
    this.hasTargetGroupLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetGroupLink", false), false);
    if (this.hasTargetGroupLink) {
      this.debugMap.put("hasTargetGroupLink", this.hasTargetGroupLink);
    }

    this.hasTargetEntityLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetEntityLink", false), false);
    if (this.hasTargetEntityLink) {
      this.debugMap.put("hasTargetEntityLink", this.hasTargetEntityLink);
    }

    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);

    this.entityMatchingIdExpression = this.retrieveConfigString("entityMatchingIdExpression", false);
    this.groupMatchingIdExpression = this.retrieveConfigString("groupMatchingIdExpression", false);
    this.membershipMatchingIdExpression = this.retrieveConfigString("membershipMatchingIdExpression", false);

    this.entityMatchingIdAttribute = this.retrieveConfigString("entityMatchingIdAttribute", false);
    this.groupMatchingIdAttribute = this.retrieveConfigString("groupMatchingIdAttribute", false);
    
    this.logAllObjectsVerbose = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logAllObjectsVerbose", false), false);
    
    this.debugLog = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("debugLog", false), false);
    
    this.subjectSourcesToProvision = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(this.retrieveConfigString("subjectSourcesToProvision", false), ","));

    for (String sourceId : this.subjectSourcesToProvision) {
      if (null == SourceManager.getInstance().getSource(sourceId)) {
        throw new RuntimeException("Cant find source: '" + sourceId + "'");
      }
    }
    this.debugMap.put("subjectSourcesToProvision", GrouperUtil.join(this.subjectSourcesToProvision.iterator(), ','));

    this.subjectLinkMemberFromId2 = this.retrieveConfigString("common.subjectLink.memberFromId2", false);
    this.subjectLinkMemberFromId3 = this.retrieveConfigString("common.subjectLink.memberFromId3", false);
    this.subjectLinkMemberToId2 = this.retrieveConfigString("common.subjectLink.memberToId2", false);
    this.subjectLinkMemberToId3 = this.retrieveConfigString("common.subjectLink.memberToId3", false);

    this.groupLinkGroupFromId2 = this.retrieveConfigString("common.groupLink.groupFromId2", false);
    this.groupLinkGroupFromId3 = this.retrieveConfigString("common.groupLink.groupFromId3", false);
    this.groupLinkGroupToId2 = this.retrieveConfigString("common.groupLink.groupToId2", false);
    this.groupLinkGroupToId3 = this.retrieveConfigString("common.groupLink.groupToId3", false);

    this.entityLinkMemberFromId2 = this.retrieveConfigString("common.entityLink.memberFromId2", false);
    this.entityLinkMemberFromId3 = this.retrieveConfigString("common.entityLink.memberFromId3", false);
    this.entityLinkMemberToId2 = this.retrieveConfigString("common.entityLink.memberToId2", false);
    this.entityLinkMemberToId3 = this.retrieveConfigString("common.entityLink.memberToId3", false);

    this.refreshSubjectLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshSubjectLinkIfLessThanAmount", false), 20);
    this.refreshGroupLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshGroupLinkIfLessThanAmount", false), 20);
    this.refreshEntityLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshEntityLinkIfLessThanAmount", false), 20);
    
    this.scoreConvertToFullSyncThreshold = GrouperUtil.intValue(this.retrieveConfigInt("scoreConvertToFullSyncThreshold", false), 10000);
    this.membershipsConvertToGroupSyncThreshold = GrouperUtil.intValue(this.retrieveConfigInt("membershipsConvertToGroupSyncThreshold", false), 500);
    
    this.entitySearchFilter = this.retrieveConfigString("userSearchFilter", false);
    this.entitySearchAllFilter = this.retrieveConfigString("userSearchAllFilter", false);
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);
    this.groupSearchAllFilter = this.retrieveConfigString("groupSearchAllFilter", false);
    
    this.insertMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertMemberships", false), false);

    this.insertEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertEntities", false), false);

    this.insertGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("insertGroups", false), false);

    this.deleteMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMemberships", false), false);

    this.deleteEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntities", false), false);

    this.deleteGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroups", false), false);

    this.updateMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("updateMemberships", false), false);

    this.updateEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("updateEntities", false), false);

    this.updateGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("updateGroups", false), false);

    this.selectGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectGroups", false), false);

    this.selectEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectEntities", false), false);

    this.selectMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectMemberships", false), false);

    this.deleteGroupsIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfNotExistInGrouper", false), false);

    this.deleteGroupsIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfGrouperDeleted", false), false);

    this.deleteEntitiesIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfNotExistInGrouper", false), false);

    this.deleteEntitiesIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfGrouperDeleted", false), false);

    this.deleteMembershipsIfNotExistInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfNotExistInGrouper", false), false);

    this.deleteMembershipsIfGrouperDeleted = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfGrouperDeleted", false), false);

    this.deleteMembershipsIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteMembershipsIfGrouperCreated", false), false);

    this.deleteGroupsIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteGroupsIfGrouperCreated", false), false);

    this.deleteEntitiesIfGrouperCreated = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteEntitiesIfGrouperCreated", false), false);

    this.groupIdOfUsersToProvision = this.retrieveConfigString("groupIdOfUsersToProvision", false);

    // init this in the behavior
    this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().setGroupIdOfUsersToProvision(this.groupIdOfUsersToProvision);
    
    if (this.entityAttributesMultivalued == null) {
      this.entityAttributesMultivalued = new HashSet<String>();
    }
    
    if (this.groupAttributesMultivalued == null) {
      this.groupAttributesMultivalued = new HashSet<String>(); 
    }

    this.groupSelectAttributes = new HashSet<String>();
    this.groupSearchAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();

    for (String targetGroupAttributeName : this.targetGroupAttributeNameToConfig.keySet()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = targetGroupAttributeNameToConfig.get(targetGroupAttributeName);
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.groupSelectAttributes.add(targetGroupAttributeName);
      }
      
      if (grouperProvisioningConfigurationAttribute.isMembershipAttribute()) {
        this.groupAttributeNameForMemberships = targetGroupAttributeName;
        this.attributeNameForMemberships = targetGroupAttributeName;
        grouperProvisioningConfigurationAttribute.setSelect(this.isSelectMemberships());
        grouperProvisioningConfigurationAttribute.setInsert(this.isInsertMemberships());
        grouperProvisioningConfigurationAttribute.setUpdate(this.isUpdateMemberships());
      }
      
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        this.groupAttributesMultivalued.add(targetGroupAttributeName);
      }
      if (grouperProvisioningConfigurationAttribute.isSearchAttribute()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          this.groupSearchAttributes.add(0, grouperProvisioningConfigurationAttribute);
        } else {
          this.groupSearchAttributes.add(grouperProvisioningConfigurationAttribute);
        }
      }
    }
    
    this.entitySelectAttributes = new HashSet<String>();
    this.entitySearchAttributes = new ArrayList<GrouperProvisioningConfigurationAttribute>();
    for (String targetEntityAttributeName : this.targetEntityAttributeNameToConfig.keySet()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = targetEntityAttributeNameToConfig.get(targetEntityAttributeName);
      if (grouperProvisioningConfigurationAttribute.isSelect()) {
        this.entitySelectAttributes.add(targetEntityAttributeName);
      }
      
      if (grouperProvisioningConfigurationAttribute.isMembershipAttribute()) {
        this.attributeNameForMemberships = targetEntityAttributeName;
        this.entityAttributeNameForMemberships = targetEntityAttributeName;
        grouperProvisioningConfigurationAttribute.setSelect(this.isSelectMemberships());
        grouperProvisioningConfigurationAttribute.setInsert(this.isInsertMemberships());
        grouperProvisioningConfigurationAttribute.setUpdate(this.isUpdateMemberships());
      }
      
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        this.entityAttributesMultivalued.add(targetEntityAttributeName);
      }

      if (grouperProvisioningConfigurationAttribute.isSearchAttribute()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          this.entitySearchAttributes.add(0, grouperProvisioningConfigurationAttribute);
        } else {
          this.entitySearchAttributes.add(grouperProvisioningConfigurationAttribute);
        }
      }
    }

    if (StringUtils.isEmpty(this.groupAttributeNameForMemberships)) {
      this.groupAttributeNameForMemberships = this.retrieveConfigString("groupAttributeNameForMemberships", false);
    } else if (!StringUtils.isEmpty(this.retrieveConfigString("groupAttributeNameForMemberships", false))) {
      throw new RuntimeException("Should only specify membershipAttribute on one attribute or groupAttributeNameForMemberships");
    }
    
    if (StringUtils.isEmpty(this.entityAttributeNameForMemberships)) {
      this.entityAttributeNameForMemberships = this.retrieveConfigString("userAttributeNameForMemberships", false);
    } else if (!StringUtils.isEmpty(this.retrieveConfigString("userAttributeNameForMemberships", false))) {
      throw new RuntimeException("Should only specify membershipAttribute on one attribute or userAttributeNameForMemberships");
    }

    this.recalculateAllOperations = GrouperUtil.booleanValue(this.retrieveConfigBoolean("recalculateAllOperations", false), false);
    
    {
      String grouperProvisioningMembershipFieldTypeString = this.retrieveConfigString("membershipFields", false);
      if (StringUtils.isBlank(grouperProvisioningMembershipFieldTypeString) || StringUtils.equalsIgnoreCase("members", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.members;
      } else if (StringUtils.equals("admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.admin;
      } else if (StringUtils.equals("read,admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.readAdmin;
      } else if (StringUtils.equals("update,admin", grouperProvisioningMembershipFieldTypeString)) {
        this.grouperProvisioningMembershipFieldType = GrouperProvisioningMembershipFieldType.updateAdmin;
      } else {
        throw new RuntimeException("Invalid GrouperProvisioningMembershipFieldType: '" + grouperProvisioningMembershipFieldTypeString + "'");
      }
    }
    
    for (String configItem : new String[] {"grouperToTargetTranslationMembership", "grouperToTargetTranslationEntity",
        "grouperToTargetTranslationGroup", "grouperToTargetTranslationGroupCreateOnly"}) {
      String key = GrouperUtil.stripPrefix(configItem, "grouperToTargetTranslation");
      for (int i=0; i<= 1000; i++) {
        
        String script = this.retrieveConfigString(configItem + "."+i+".script" , false);
        if (StringUtils.isBlank(script)) {
          break;
        }
        List<String> scripts = this.grouperProvisioningToTargetTranslation.get(key);
        if (scripts == null) {
          scripts = new ArrayList<String>();
          this.grouperProvisioningToTargetTranslation.put(key, scripts);
        }
        scripts.add(script);
        
      }
      
    }
    
    // diagnostics settings
    this.diagnosticsGroupsAllSelect = this.retrieveConfigBoolean("selectAllGroupsDuringDiagnostics", false);
    this.diagnosticsEntitiesAllSelect = this.retrieveConfigBoolean("selectAllEntitiesDuringDiagnostics", false);
    this.diagnosticsMembershipsAllSelect = this.retrieveConfigBoolean("selectAllMembershipsDuringDiagnostics", false);
    this.diagnosticsGroupName = this.retrieveConfigString("testGroupName", false);
    this.createGroupDuringDiagnostics = this.retrieveConfigBoolean("createGroupDuringDiagnostics", false);

    //register metadata
    this.getGrouperProvisioner().retrieveGrouperProvisioningObjectMetadata().appendMetadataItemsFromConfig(this.metadataNameToMetadataItem.values());
    
    this.operateOnGrouperEntities = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperEntities", false), false);
    this.operateOnGrouperMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperMemberships", false), false);
    this.operateOnGrouperGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("operateOnGrouperGroups", false), false);
    
  }
  
  /**
   * operate on grouper entities
   */
  private boolean operateOnGrouperEntities;
  
  /**
   * operate on grouper memberships
   */
  private boolean operateOnGrouperMemberships;
  
  /**
   * operate on grouper groups
   */
  private boolean operateOnGrouperGroups;
  
  /**
   * operate on grouper entities
   * @return is operate
   */
  public boolean isOperateOnGrouperEntities() {
    return operateOnGrouperEntities;
  }

  /**
   * operate on grouper entities
   * @return is operate
   */
  public boolean isOperateOnGrouperMemberships() {
    return operateOnGrouperMemberships;
  }

  
  public boolean isOperateOnGrouperGroups() {
    return operateOnGrouperGroups;
  }

  public boolean isRecalculateAllOperations() {
    return recalculateAllOperations;
  }

  
  public void setRecalculateAllOperations(boolean recalculateAllOperations) {
    this.recalculateAllOperations = recalculateAllOperations;
  }

  /**
   * no need to configure twice if the caller needs to configure before provisioning
   */
  private boolean configured = false;

  private String entityLinkMemberFromId2;

  private String entityLinkMemberFromId3;

  private String entityLinkMemberToId2;

  private String entityLinkMemberToId3;
  
  
  
  public String getEntityLinkMemberFromId2() {
    return entityLinkMemberFromId2;
  }



  
  public void setEntityLinkMemberFromId2(String entityLinkMemberFromId2) {
    this.entityLinkMemberFromId2 = entityLinkMemberFromId2;
  }



  
  public String getEntityLinkMemberFromId3() {
    return entityLinkMemberFromId3;
  }



  
  public void setEntityLinkMemberFromId3(String entityLinkMemberFromId3) {
    this.entityLinkMemberFromId3 = entityLinkMemberFromId3;
  }



  
  public String getEntityLinkMemberToId2() {
    return entityLinkMemberToId2;
  }



  
  public void setEntityLinkMemberToId2(String entityLinkMemberToId2) {
    this.entityLinkMemberToId2 = entityLinkMemberToId2;
  }



  
  public String getEntityLinkMemberToId3() {
    return entityLinkMemberToId3;
  }



  
  public void setEntityLinkMemberToId3(String entityLinkMemberToId3) {
    this.entityLinkMemberToId3 = entityLinkMemberToId3;
  }

  private String groupLinkGroupFromId2;

  private String groupLinkGroupFromId3;

  private String groupLinkGroupToId2;

  private String groupLinkGroupToId3;

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetEntityAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  /**
   * field name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetEntityFieldNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  /**
   * attribute name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetMembershipAttributeNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();

  /**
   * field name to config
   */
  private Map<String, GrouperProvisioningConfigurationAttribute> targetMembershipFieldNameToConfig = new LinkedHashMap<String, GrouperProvisioningConfigurationAttribute>();
  
  
  
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetMembershipAttributeNameToConfig() {
    return targetMembershipAttributeNameToConfig;
  }

  
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetMembershipFieldNameToConfig() {
    return targetMembershipFieldNameToConfig;
  }

  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetEntityAttributeNameToConfig() {
    return targetEntityAttributeNameToConfig;
  }

  
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetEntityFieldNameToConfig() {
    return targetEntityFieldNameToConfig;
  }

  public String getGroupLinkGroupFromId2() {
    return groupLinkGroupFromId2;
  }



  
  public void setGroupLinkGroupFromId2(String groupLinkGroupFromId2) {
    this.groupLinkGroupFromId2 = groupLinkGroupFromId2;
  }



  
  public String getGroupLinkGroupFromId3() {
    return groupLinkGroupFromId3;
  }



  
  public void setGroupLinkGroupFromId3(String groupLinkGroupFromId3) {
    this.groupLinkGroupFromId3 = groupLinkGroupFromId3;
  }



  
  public String getGroupLinkGroupToId2() {
    return groupLinkGroupToId2;
  }



  
  public void setGroupLinkGroupToId2(String groupLinkGroupToId2) {
    this.groupLinkGroupToId2 = groupLinkGroupToId2;
  }



  
  public String getGroupLinkGroupToId3() {
    return groupLinkGroupToId3;
  }



  
  public void setGroupLinkGroupToId3(String groupLinkGroupToId3) {
    this.groupLinkGroupToId3 = groupLinkGroupToId3;
  }



  /**
   * configure the provisioner, call super if subclassing
   */
  public void configureProvisioner() {
    
    if (this.configured) {
      return;
    }
    try {
    
      this.preConfigure();
    
      this.configureGenericSettings();
      
      this.configureSpecificSettings();
    
    } catch (RuntimeException re) {
      if (this.grouperProvisioner != null && this.grouperProvisioner.getGcGrouperSyncLog() != null) {
        try {
          this.grouperProvisioner.getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.CONFIG_ERROR);
          this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncLogDao().internal_logStore(this.grouperProvisioner.getGcGrouperSyncLog());
        } catch (RuntimeException re2) {
          GrouperClientUtils.injectInException(re, "***** START ANOTHER EXCEPTON *******" + GrouperClientUtils.getFullStackTrace(re2) + "***** END ANOTHER EXCEPTON *******");
        }
      }
      throw re;
    }

    this.configured = true;
  }

  
  
  public boolean isConfigured() {
    return configured;
  }



  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

  
  public boolean isHasSubjectLink() {
    return hasSubjectLink;
  }

  
  public void setHasSubjectLink(boolean hasSubjectLink) {
    this.hasSubjectLink = hasSubjectLink;
  }

  
  public boolean isHasTargetGroupLink() {
    return hasTargetGroupLink;
  }

  
  public void setHasTargetGroupLink(boolean hasTargetGroupLink) {
    this.hasTargetGroupLink = hasTargetGroupLink;
  }

  
  public boolean isHasTargetEntityLink() {
    return hasTargetEntityLink;
  }

  
  public void setHasTargetEntityLink(boolean hasTargetEntityLink) {
    this.hasTargetEntityLink = hasTargetEntityLink;
  }
  
  public Set<String> getSubjectSourcesToProvision() {
    return subjectSourcesToProvision;
  }

  
  public void setSubjectSourcesToProvision(Set<String> subjectSourcesToProvision) {
    this.subjectSourcesToProvision = subjectSourcesToProvision;
  }
  
  public List<GrouperProvisioningConfigurationAttribute> getEntitySearchAttributes() {
    return entitySearchAttributes;
  }

  
  public void setEntitySearchAttributes(List<GrouperProvisioningConfigurationAttribute> userSearchAttributes ) {
    this.entitySearchAttributes = userSearchAttributes;
  }

  
  public List<GrouperProvisioningConfigurationAttribute> getGroupSearchAttributes() {
    return groupSearchAttributes;
  }

  public void setGroupSearchAttributes(List<GrouperProvisioningConfigurationAttribute> groupSearchAttributes) {
    this.groupSearchAttributes = groupSearchAttributes;
  }

  
  public Set<String> getEntityAttributesMultivalued() {
    return entityAttributesMultivalued;
  }

  
  public void setEntityAttributesMultivalued(Set<String> userAttributesMultivalued) {
    this.entityAttributesMultivalued = userAttributesMultivalued;
  }

  
  public Set<String> getGroupAttributesMultivalued() {
    return groupAttributesMultivalued;
  }

  
  public void setGroupAttributesMultivalued(Set<String> groupAttributesMultivalued) {
    this.groupAttributesMultivalued = groupAttributesMultivalued;
  }

  public boolean isInsertEntities() {
    return insertEntities;
  }

  
  public void setInsertEntities(boolean insertEntities) {
    this.insertEntities = insertEntities;
  }

  
  public boolean isInsertGroups() {
    return insertGroups;
  }

  
  public void setInsertGroups(boolean insertGroups) {
    this.insertGroups = insertGroups;
  }

  
  
  public boolean isDeleteGroupsIfNotExistInGrouper() {
    return deleteGroupsIfNotExistInGrouper;
  }

  
  public void setDeleteGroupsIfNotExistInGrouper(boolean deleteGroupsIfNotExistInGrouper) {
    this.deleteGroupsIfNotExistInGrouper = deleteGroupsIfNotExistInGrouper;
  }


  public boolean isDeleteGroupsIfGrouperDeleted() {
    return deleteGroupsIfGrouperDeleted;
  }

  
  public void setDeleteGroupsIfGrouperDeleted(boolean deleteGroupsIfGrouperDeleted) {
    this.deleteGroupsIfGrouperDeleted = deleteGroupsIfGrouperDeleted;
  }

  public GrouperProvisioningMembershipFieldType getGrouperProvisioningMembershipFieldType() {
    return grouperProvisioningMembershipFieldType;
  }

  
  public void setGrouperProvisioningMembershipFieldType(
      GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType) {
    this.grouperProvisioningMembershipFieldType = grouperProvisioningMembershipFieldType;
  }
  
}
