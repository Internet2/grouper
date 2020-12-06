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
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
   * field name to config
   * @return
   */
  public Map<String, GrouperProvisioningConfigurationAttribute> getTargetGroupFieldNameToConfig() {
    return targetGroupFieldNameToConfig;
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
    this.grouperProvisioner.getGcGrouperSyncJob().waitForRelatedJobsToFinishThenRun(this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync());
    
    if (this.grouperProvisioner.getGcGrouperSyncLog() == null) {
      this.grouperProvisioner.setGcGrouperSyncLog(this.grouperProvisioner.getGcGrouperSync().getGcGrouperSyncJobDao().jobCreateLog(this.grouperProvisioner.getGcGrouperSyncJob()));
    }
    
    this.grouperProvisioner.getGcGrouperSyncLog().setSyncTimestamp(new Timestamp(System.currentTimeMillis()));
  
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
   * attributes to query when retrieving users, e.g.
   * dn,cn,uid,mail,samAccountName,uidNumber,
   */
  private Set<String> userSearchAttributes = null;
  
  /**
   * attributes to query when retrieving groups, e.g.
   * cn,gidNumber,samAccountName,objectclass
   */
  private Set<String> groupSearchAttributes = null;
  
  /**
   * someAttr  everything is assumed to be single valued except objectclass and the provisionedAttributeName optional
   */
  private Set<String> userAttributesMultivalued = null;
  
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
   * if should create missing users, default false
   */
  private boolean createMissingUsers = false;

  /**
   * if should create missing groups, default true
   */
  private boolean createMissingGroups = true;
  
  /**
   * search filter to look up entity if cannot just use the matchingId
   */
  private String userSearchFilter = null;
  
  /**
   * search filter to look up entity if cannot just use the matchingId
   * @return
   */
  public String getUserSearchFilter() {
    return userSearchFilter;
  }

  /**
   * search filter to look up entity if cannot just use the matchingId
   * @param userSearchFilter
   */
  public void setUserSearchFilter(String userSearchFilter) {
    this.userSearchFilter = userSearchFilter;
  }

  private String userSearchAllFilter;

  /**
   * search filter to look up all entities
   * @return
   */
  public String getUserSearchAllFilter() {
    return userSearchAllFilter;
  }

  /**
   * search filter to look up all entities
   * @param userSearchAllFilter
   */
  public void setUserSearchAllFilter(String userSearchAllFilter) {
    this.userSearchAllFilter = userSearchAllFilter;
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
   * true or false if groups in full sync should be deleted if in group all filter and not in grouper
   * or for attributes delete other attribute not provisioned by grouper default to false
   */
  private boolean deleteInTargetIfInTargetAndNotGrouper = false;

  /**
   * true or false if groups that were created in grouper were deleted should it be deleted in ldap?
   * or for attributes, delete attribute value if deleted in grouper default to true
   */
  private boolean deleteInTargetIfDeletedInGrouper = true;

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
  private String userAttributeNameForMemberships;
  
  /**
   * attribute name in a user object that refers to memberships (if applicable)
   * @return
   */
  public String getUserAttributeNameForMemberships() {
    return userAttributeNameForMemberships;
  }

  /**
   * attribute name in a user object that refers to memberships (if applicable)
   * @param userAttributeNameForMemberships
   */
  public void setUserAttributeNameForMemberships(String userAttributeNameForMemberships) {
    this.userAttributeNameForMemberships = userAttributeNameForMemberships;
  }

  private String membershipAttributeNames;

  /**
   * 
   */
  public abstract void configureSpecificSettings();

  public void configureGenericSettings() {

    for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute", "targetMembershipAttribute"}) {
      
      boolean foundMatchingId = false;
      String foundMatchingIdName = null;
      
      boolean foundMembershipAttribute = false;
      String foundMembershipAttributeName = null;
      
      for (int i=0; i<= 1000; i++) {
  
        GrouperProvisioningConfigurationAttribute attributeConfig = new GrouperProvisioningConfigurationAttribute();
  
        if (StringUtils.equals("targetGroupAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.group);
        } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
        } else if (StringUtils.equals("targetMembershipAttribute", objectType)) {
          attributeConfig.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.membership);
        } else {
          throw new RuntimeException("Cnat find object type: " + objectType);
        }
        
        String name = this.retrieveConfigString(objectType + "."+i+".name" , false);
        if (StringUtils.isBlank(name)) {
          break;
        }
        attributeConfig.setName(name);
  
        boolean attribute = !GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".isFieldElseAttribute" , false), false);
        attributeConfig.setAttribute(attribute);
        
        {
          boolean insert = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".insert" , false), false);
          attributeConfig.setInsert(insert);
        }
  
        {
          boolean delete = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType + "."+i+".delete" , false), false);
          attributeConfig.setDelete(delete);
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
          boolean update = GrouperUtil.booleanValue(this.retrieveConfigBoolean(objectType+"."+i+".update" , false), false);
          attributeConfig.setUpdate(update);
        }
        
        {
          GrouperProvisioningConfigurationAttributeValueType valueType = 
              GrouperProvisioningConfigurationAttributeValueType.valueOfIgnoreCase(
                  this.retrieveConfigString(objectType+ "."+i+".valueType" , false), false);
          attributeConfig.setValueType(valueType);
        }
        if ("targetGroupAttribute".equals(objectType)) {
          if (attribute) {
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
          if (attribute) {
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
          if (attribute) {
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

    this.hasTargetEntityLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetUserLink", false), false);
    if (this.hasTargetEntityLink) {
      this.debugMap.put("hasTargetUserLink", this.hasTargetEntityLink);
    }

    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);

    this.entityMatchingIdExpression = this.retrieveConfigString("entityMatchingIdExpression", false);
    this.groupMatchingIdExpression = this.retrieveConfigString("groupMatchingIdExpression", false);
    this.membershipMatchingIdExpression = this.retrieveConfigString("membershipMatchingIdExpression", false);

    
    this.entityMatchingIdAttribute = this.retrieveConfigString("entityMatchingIdAttribute", false);
    this.groupMatchingIdAttribute = this.retrieveConfigString("groupMatchingIdAttribute", false);
    
    this.logAllObjectsVerbose = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("logAllObjectsVerbose", false), false);
    
    this.debugLog = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("debugLog", false), false);
    
    this.membershipAttributeNames = this.retrieveConfigString("membershipAttributeNames", false);
    
    this.subjectSourcesToProvision = GrouperUtil.splitTrimToSet(this.retrieveConfigString("subjectSourcesToProvision", false), ",");

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
    
    this.userSearchFilter = this.retrieveConfigString("userSearchFilter", false);
    this.userSearchAllFilter = this.retrieveConfigString("userSearchAllFilter", false);
    this.groupSearchFilter = this.retrieveConfigString("groupSearchFilter", false);
    this.groupSearchAllFilter = this.retrieveConfigString("groupSearchAllFilter", false);
    
    this.userSearchAttributes = GrouperUtil.splitTrimToSet(this.retrieveConfigString("userSearchAttributes", false), ",");
    if (this.userSearchAttributes == null) {
      this.userSearchAttributes = new HashSet<String>();
    }
    
    this.groupSearchAttributes = GrouperUtil.splitTrimToSet(this.retrieveConfigString("groupSearchAttributes", false), ",");
    if (this.groupSearchAttributes == null) {
      this.groupSearchAttributes = new HashSet<String>();
    }
    
    this.userAttributesMultivalued = GrouperUtil.splitTrimToSet(this.retrieveConfigString("userAttributesMultivalued", false), ",");
    if (this.userAttributesMultivalued == null) {
      this.userAttributesMultivalued = new HashSet<String>();
    }
    
    this.groupAttributesMultivalued = GrouperUtil.splitTrimToSet(this.retrieveConfigString("groupAttributesMultivalued", false), ",");
    if (this.groupAttributesMultivalued == null) {
      this.groupAttributesMultivalued = new HashSet<String>(); 
    }
    
    for (String targetGroupAttributeName : this.targetGroupAttributeNameToConfig.keySet()) {
      if (targetGroupAttributeNameToConfig.get(targetGroupAttributeName).isSelect()) {
        this.groupSearchAttributes.add(targetGroupAttributeName);
      }
      
      if (targetGroupAttributeNameToConfig.get(targetGroupAttributeName).isMembershipAttribute()) {
        this.groupAttributeNameForMemberships = targetGroupAttributeName;
        this.attributeNameForMemberships = targetGroupAttributeName;
      }
      
      if (targetGroupAttributeNameToConfig.get(targetGroupAttributeName).isMultiValued()) {
        this.groupAttributesMultivalued.add(targetGroupAttributeName);
      }
    }
    
    for (String targetEntityAttributeName : this.targetEntityAttributeNameToConfig.keySet()) {
      if (targetEntityAttributeNameToConfig.get(targetEntityAttributeName).isSelect()) {
        this.userSearchAttributes.add(targetEntityAttributeName);
      }
      
      if (targetEntityAttributeNameToConfig.get(targetEntityAttributeName).isMembershipAttribute()) {
        this.attributeNameForMemberships = targetEntityAttributeName;
        this.userAttributeNameForMemberships = targetEntityAttributeName;
      }
      
      if (targetEntityAttributeNameToConfig.get(targetEntityAttributeName).isMultiValued()) {
        this.userAttributesMultivalued.add(targetEntityAttributeName);
      }
    }
    
    this.createMissingUsers = GrouperUtil.booleanValue(this.retrieveConfigBoolean("createMissingUsers", false), false);

    this.createMissingGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("createMissingGroups", false), true);

    if (StringUtils.isEmpty(this.groupAttributeNameForMemberships)) {
      this.groupAttributeNameForMemberships = this.retrieveConfigString("groupAttributeNameForMemberships", false);
    } else if (!StringUtils.isEmpty(this.retrieveConfigString("groupAttributeNameForMemberships", false))) {
      throw new RuntimeException("Should only specify membershipAttribute on one attribute or groupAttributeNameForMemberships");
    }
    
    if (StringUtils.isEmpty(this.userAttributeNameForMemberships)) {
      this.userAttributeNameForMemberships = this.retrieveConfigString("userAttributeNameForMemberships", false);
    } else if (!StringUtils.isEmpty(this.retrieveConfigString("userAttributeNameForMemberships", false))) {
      throw new RuntimeException("Should only specify membershipAttribute on one attribute or userAttributeNameForMemberships");
    }

    this.deleteInTargetIfInTargetAndNotGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteInTargetIfInTargetAndNotGrouper", false), false);

    this.deleteInTargetIfDeletedInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteInTargetIfDeletedInGrouper", false), true);

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
    
      //validate
      this.validate();
      
    
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

  
  
  public void validate() {
    
    Set<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = new HashSet<GrouperProvisioningConfigurationAttribute>();
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetGroupFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetGroupAttributeNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetEntityFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetEntityAttributeNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetMembershipFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(this.getTargetMembershipAttributeNameToConfig()).values());

    boolean hasGroupMatchingId = false;
    boolean hasMemberMatchingId = false;
    boolean hasMembershipMatchingId = false;

    boolean hasGroupMembershipId = false;
    boolean hasMemberMembershipId = false;

    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : grouperProvisioningConfigurationAttributes) {
      //validate the matching attributes come from gcSync objects
      
      if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
        
        if (StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())
            && StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {

          throw new RuntimeException(grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType()
              + " " + (grouperProvisioningConfigurationAttribute.isAttribute() ? "attribute" : "field") + " '" + grouperProvisioningConfigurationAttribute.getName() 
              + "' is a matching ID but does not have a translation from a sync field.  It must have a translation from a sync field! " + grouperProvisioningConfigurationAttribute);
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
          hasMemberMatchingId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
          hasGroupMatchingId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.membership) {
          hasMembershipMatchingId = true;
        }
        
      }
      
      
      if (grouperProvisioningConfigurationAttribute.isMembershipAttribute()) {
        
        if (StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())
            && StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {

          throw new RuntimeException(grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType()
              + " " + (grouperProvisioningConfigurationAttribute.isAttribute() ? "attribute" : "field") + " '" + grouperProvisioningConfigurationAttribute.getName() 
              + "' is a membership attribute but does not have a translation from a sync field.  It must have a translation from a sync field! " + grouperProvisioningConfigurationAttribute);
        }

        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
          hasMemberMembershipId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
          hasGroupMembershipId = true;
        }
        
      }
      
    }

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      if (!hasGroupMatchingId) {
        throw new RuntimeException("You are provisioning groupAttributes but you do you not have a group field or attribute marked as matchingId.  Identify a group field or attribute as matchingId!");
      }
      if (!hasGroupMembershipId) {
        throw new RuntimeException("You are provisioning groupAttributes but you do you not have a group attribute marked as membership attribute.  Identify a group attribute as membership attribute!!");
      }
    } else if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      if (!hasMemberMatchingId) {
        throw new RuntimeException("You are provisioning entityAttributes but you do you not have an entity field or attribute marked as matchingId.  Identify an entity field or attribute as matchingId!");
      }
      if (!hasMemberMembershipId) {
        throw new RuntimeException("You are provisioning entityAttributes but you do you not have an entity attribute marked as membership attribute.  Identify an entity attribute as membership attribute!!");
      }
    }
    
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

  
  public void setHasTargetEntityLink(boolean hasTargetUserLink) {
    this.hasTargetEntityLink = hasTargetUserLink;
  }
  
  public Set<String> getSubjectSourcesToProvision() {
    return subjectSourcesToProvision;
  }

  
  public void setSubjectSourcesToProvision(Set<String> subjectSourcesToProvision) {
    this.subjectSourcesToProvision = subjectSourcesToProvision;
  }
  
  public Set<String> getUserSearchAttributes() {
    return userSearchAttributes;
  }

  
  public void setUserSearchAttributes(Set<String> userSearchAttributes) {
    this.userSearchAttributes = userSearchAttributes;
  }

  
  public Set<String> getGroupSearchAttributes() {
    return groupSearchAttributes;
  }

  
  public void setGroupSearchAttributes(Set<String> groupSearchAttributes) {
    this.groupSearchAttributes = groupSearchAttributes;
  }

  
  public Set<String> getUserAttributesMultivalued() {
    return userAttributesMultivalued;
  }

  
  public void setUserAttributesMultivalued(Set<String> userAttributesMultivalued) {
    this.userAttributesMultivalued = userAttributesMultivalued;
  }

  
  public Set<String> getGroupAttributesMultivalued() {
    return groupAttributesMultivalued;
  }

  
  public void setGroupAttributesMultivalued(Set<String> groupAttributesMultivalued) {
    this.groupAttributesMultivalued = groupAttributesMultivalued;
  }

  
  public boolean isCreateMissingUsers() {
    return createMissingUsers;
  }

  
  public void setCreateMissingUsers(boolean createMissingUsers) {
    this.createMissingUsers = createMissingUsers;
  }

  
  public boolean isCreateMissingGroups() {
    return createMissingGroups;
  }

  
  public void setCreateMissingGroups(boolean createMissingGroups) {
    this.createMissingGroups = createMissingGroups;
  }
  
  public boolean isDeleteInTargetIfInTargetAndNotGrouper() {
    return deleteInTargetIfInTargetAndNotGrouper;
  }

  
  public void setDeleteInTargetIfInTargetAndNotGrouper(
      boolean deleteInTargetIfInTargetAndNotGrouper) {
    this.deleteInTargetIfInTargetAndNotGrouper = deleteInTargetIfInTargetAndNotGrouper;
  }

  
  public boolean isDeleteInTargetIfDeletedInGrouper() {
    return deleteInTargetIfDeletedInGrouper;
  }

  
  public void setDeleteInTargetIfDeletedInGrouper(
      boolean deleteInTargetIfDeletedInGrouper) {
    this.deleteInTargetIfDeletedInGrouper = deleteInTargetIfDeletedInGrouper;
  }

  
  public GrouperProvisioningMembershipFieldType getGrouperProvisioningMembershipFieldType() {
    return grouperProvisioningMembershipFieldType;
  }

  
  public void setGrouperProvisioningMembershipFieldType(
      GrouperProvisioningMembershipFieldType grouperProvisioningMembershipFieldType) {
    this.grouperProvisioningMembershipFieldType = grouperProvisioningMembershipFieldType;
  }

  
  public String getMembershipAttributeNames() {
    return membershipAttributeNames;
  }

  
  public void setMembershipAttributeNames(String membershipAttributeNames) {
    this.membershipAttributeNames = membershipAttributeNames;
  }

  
}
