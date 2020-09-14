package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

  /**
   * expression to get the group id from target group
   */
  private String targetGroupIdExpression;

  /**
   * expression to get the membership id from the target group
   */
  private String targetMembershipIdExpression;

  /**
   * expression to get the entity id from the target entity
   */
  private String targetEntityIdExpression;
  
  
  
  public String getTargetGroupIdExpression() {
    return targetGroupIdExpression;
  }


  
  public void setTargetGroupIdExpression(String targetGroupIdExpression) {
    this.targetGroupIdExpression = targetGroupIdExpression;
  }


  
  public String getTargetMembershipIdExpression() {
    return targetMembershipIdExpression;
  }


  
  public void setTargetMembershipIdExpression(String targetMembershipIdExpression) {
    this.targetMembershipIdExpression = targetMembershipIdExpression;
  }


  
  public String getTargetEntityIdExpression() {
    return targetEntityIdExpression;
  }


  
  public void setTargetEntityIdExpression(String targetEntityIdExpression) {
    this.targetEntityIdExpression = targetEntityIdExpression;
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
    if (this.grouperProvisioner.getGrouperProvisioningType() == null) {
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
          .jobRetrieveOrCreateBySyncType(this.grouperProvisioner.getGrouperProvisioningType().name());
      this.grouperProvisioner.setGcGrouperSyncJob(gcGrouperSyncJob);
    }
    this.grouperProvisioner.getGcGrouperSyncJob().waitForRelatedJobsToFinishThenRun(this.grouperProvisioner.getGrouperProvisioningType().isFullSync());
    
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
  private boolean hasTargetUserLink = false;

  /**
   * userSearchAttributeName employeeID  attribute to filter on  required if userAttributes or hasTargetUserLink
   */
  private String userSearchAttributeName = null;

  /**
   * userSearchAttributeValueFormat ${subject.id}, ${targetEntity.dn}, ${targetEntity.attributes['uid']}
   */
  private String userSearchAttributeValueFormat = null;


  /**
   * value for the user search attribute name  required if userAttributes or hasTargetUserLink
   * userAttributeReferredToByGroup  dn  in group memberships, this is the value that refers to the user 
   * optional. show if groupMemberships and hasTargetUserLink
   */
  private String userAttributeReferredToByGroup = null;

  /**
   * for subject link, this is the subject api identifier that is needed to look up the target user
   */
  private String subjectApiAttributeForTargetUser = null;

  /**
   * in user attributes, this is the value that refers to the group. show if userAttributes and hasTargetGroupLink. defaults to dn
   */
  private String groupAttributeReferredToByUser = null;

  /**
   * ${targetEntity.attributes['dn']}  main identifier of the user on the target side  show = false
   */
  private String syncMemberToId2AttributeValueFormat = null;
  
  /**
   * ${targetEntity.attributes['uid']} identifier of the user as referred to by the group
   */
  private String syncMemberToId3AttributeValueFormat = null;
  
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
   * target attribute value that helps look up user, ${targetEntity.attributes['netId']} 
   */
  private String syncMemberFromId2AttributeValueFormat = null;
  
  /**
   * ${subject.attributes['myLdapId']} subject attribute value that helps look up user
   */
  private String syncMemberFromId3AttributeValueFormat = null;
  
  /**
   * group id that identifies a group
   */
  private String syncGroupToId2AttributeValueFormat = null;
  
  /**
   * id from membership or use that refers to group
   */
  private String syncGroupToId3AttributeValueFormat = null;
  
  /**
   * target attribute that looks up the group
   */
  private String syncGroupFromId2AttributeValueFormat = null;
 
  /**
   */
  private String syncGroupFromId3AttributeValueFormat = null;
  
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
   * attribute name of target group used to lookup group, gidNumber
   */
  private String groupSearchAttributeName = null;
  
  /**
   * EL scriptlet to get to the group attribute name, ${syncGroup.groupIdIndex}
   */
  private String groupSearchAttributeValueFormat = null;
  
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

  private String membershipAttributeNames;

  /**
   * 
   */
  public abstract void configureSpecificSettings();

  public void configureGenericSettings() {

    this.hasSubjectLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasSubjectLink", false), false);
    if (this.hasSubjectLink) {
      this.debugMap.put("hasSubjectLink", this.hasSubjectLink);
    }
    
    this.hasTargetGroupLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetGroupLink", false), false);
    if (this.hasTargetGroupLink) {
      this.debugMap.put("hasTargetGroupLink", this.hasTargetGroupLink);
    }

    this.hasTargetUserLink = GrouperUtil.defaultIfNull(this.retrieveConfigBoolean("hasTargetUserLink", false), false);
    if (this.hasTargetUserLink) {
      this.debugMap.put("hasTargetUserLink", this.hasTargetUserLink);
    }

    this.targetEntityIdExpression = this.retrieveConfigString("targetEntityIdExpression", false);
    this.targetGroupIdExpression = this.retrieveConfigString("targetGroupIdExpression", false);
    this.targetMembershipIdExpression = this.retrieveConfigString("targetMembershipIdExpression", false);

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
    
    this.userSearchAttributeName = this.retrieveConfigString("userSearchAttributeName", this.hasTargetUserLink);
    if (!StringUtils.isBlank(this.userSearchAttributeName)) {
      this.debugMap.put("userSearchAttributeName", this.userSearchAttributeName);
    }

    this.userSearchAttributeValueFormat = this.retrieveConfigString("userSearchAttributeValueFormat", !StringUtils.isBlank(this.userSearchAttributeName));
    if (!StringUtils.isBlank(this.userSearchAttributeValueFormat)) {
      this.debugMap.put("userSearchAttributeValueFormat", this.userSearchAttributeValueFormat);
    }

    if (StringUtils.isBlank(this.userSearchAttributeName) != StringUtils.isBlank(this.userSearchAttributeValueFormat)) {
      throw new RuntimeException("If you specify userSearchAttributeName then you must specify userSearchAttributeValueFormat and vise versa! '" 
          + this.userSearchAttributeName + "', '" + this.userSearchAttributeValueFormat + "'");
    }

    this.subjectLinkMemberFromId2 = this.retrieveConfigString("common.subjectLink.memberFromId2", false);
    this.subjectLinkMemberFromId3 = this.retrieveConfigString("common.subjectLink.memberFromId3", false);
    this.subjectLinkMemberToId2 = this.retrieveConfigString("common.subjectLink.memberToId2", false);
    this.subjectLinkMemberToId3 = this.retrieveConfigString("common.subjectLink.memberToId3", false);

    this.refreshSubjectLinkIfLessThanAmount = GrouperUtil.intValue(this.retrieveConfigInt("refreshSubjectLinkIfLessThanAmount", this.hasSubjectLink), 20);
    
    this.userAttributeReferredToByGroup = this.retrieveConfigString("userAttributeReferredToByGroup", this.hasTargetUserLink);
    
    this.subjectApiAttributeForTargetUser = this.retrieveConfigString("subjectApiAttributeForTargetUser", this.hasSubjectLink);

    this.groupAttributeReferredToByUser = this.retrieveConfigString("groupAttributeReferredToByUser", this.hasTargetGroupLink);
    
    this.syncMemberToId2AttributeValueFormat = this.retrieveConfigString("syncMemberToId2AttributeValueFormat", this.hasTargetUserLink);

    this.syncMemberToId3AttributeValueFormat = this.retrieveConfigString("syncMemberToId3AttributeValueFormat", this.hasTargetUserLink);

    this.syncMemberFromId2AttributeValueFormat = this.retrieveConfigString("syncMemberFromId2AttributeValueFormat", this.hasTargetUserLink);

    this.syncMemberFromId3AttributeValueFormat = this.retrieveConfigString("syncMemberFromId3AttributeValueFormat", this.hasSubjectLink);

    this.syncGroupToId2AttributeValueFormat = this.retrieveConfigString("syncGroupToId2AttributeValueFormat", this.hasTargetGroupLink);

    this.syncGroupToId3AttributeValueFormat = this.retrieveConfigString("syncGroupToId3AttributeValueFormat", this.hasTargetGroupLink);

    this.syncGroupFromId2AttributeValueFormat = this.retrieveConfigString("syncGroupFromId2AttributeValueFormat", this.hasTargetGroupLink);

    this.syncGroupFromId3AttributeValueFormat = this.retrieveConfigString("syncGroupFromId3AttributeValueFormat", false);

    this.userSearchAttributes = GrouperUtil.splitTrimToSet(this.retrieveConfigString("userSearchAttributes", false), ",");

    this.groupSearchAttributes = GrouperUtil.splitTrimToSet(this.retrieveConfigString("groupSearchAttributes", false), ",");

    this.userAttributesMultivalued = GrouperUtil.splitTrimToSet(this.retrieveConfigString("userAttributesMultivalued", false), ",");

    this.groupAttributesMultivalued = GrouperUtil.splitTrimToSet(this.retrieveConfigString("groupAttributesMultivalued", false), ",");
    
    this.createMissingUsers = GrouperUtil.booleanValue(this.retrieveConfigBoolean("createMissingUsers", false), false);

    this.createMissingGroups = GrouperUtil.booleanValue(this.retrieveConfigBoolean("createMissingGroups", false), true);

    this.groupSearchAttributeName = this.retrieveConfigString("groupSearchAttributeName", false);

    this.groupAttributeNameForMemberships = this.retrieveConfigString("groupAttributeNameForMemberships", false);
    
    this.groupSearchAttributeValueFormat = this.retrieveConfigString("groupSearchAttributeValueFormat", false);
    
    if (StringUtils.isBlank(this.groupSearchAttributeName) != StringUtils.isBlank(this.groupSearchAttributeValueFormat)) {
      throw new RuntimeException("If you specify groupSearchAttributeName then you must specify groupSearchAttributeValueFormat and vise versa! '" 
          + this.groupSearchAttributeName + "', '" + this.groupSearchAttributeValueFormat + "'");
    }

    this.deleteInTargetIfInTargetAndNotGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteInTargetIfInTargetAndNotGrouper", false), false);

    this.deleteInTargetIfDeletedInGrouper = GrouperUtil.booleanValue(this.retrieveConfigBoolean("deleteInTargetIfDeletedInGrouper", false), true);

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
    
    for (int i=0; i<= 1000; i++) {
      
      String script = this.retrieveConfigString("grouperToTargetTranslation."+i+".script" , false);
      if (StringUtils.isBlank(script)) {
        break;
      }
      String forString = this.retrieveConfigString("grouperToTargetTranslation."+i+".for" , true);
      List<String> scripts = this.grouperProvisioningToTargetTranslation.get(forString);
      if (scripts == null) {
        scripts = new ArrayList<String>();
        this.grouperProvisioningToTargetTranslation.put(forString, scripts);
      } 
      scripts.add(script);
      
    }

  }
  
  /**
   * no need to configure twice if the caller needs to configure before provisioning
   */
  private boolean configured = false;
  
  
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

  
  public boolean isHasTargetUserLink() {
    return hasTargetUserLink;
  }

  
  public void setHasTargetUserLink(boolean hasTargetUserLink) {
    this.hasTargetUserLink = hasTargetUserLink;
  }

  
  public String getUserSearchAttributeName() {
    return userSearchAttributeName;
  }

  
  public void setUserSearchAttributeName(String userSearchAttributeName) {
    this.userSearchAttributeName = userSearchAttributeName;
  }

  
  public String getUserSearchAttributeValueFormat() {
    return userSearchAttributeValueFormat;
  }

  
  public void setUserSearchAttributeValueFormat(String userSearchAttributeValueFormat) {
    this.userSearchAttributeValueFormat = userSearchAttributeValueFormat;
  }

  
  public String getUserAttributeReferredToByGroup() {
    return userAttributeReferredToByGroup;
  }

  
  public void setUserAttributeReferredToByGroup(String userAttributeReferredToByGroup) {
    this.userAttributeReferredToByGroup = userAttributeReferredToByGroup;
  }

  
  public String getSubjectApiAttributeForTargetUser() {
    return subjectApiAttributeForTargetUser;
  }

  
  public void setSubjectApiAttributeForTargetUser(String subjectApiAttributeForTargetUser) {
    this.subjectApiAttributeForTargetUser = subjectApiAttributeForTargetUser;
  }

  
  public String getGroupAttributeReferredToByUser() {
    return groupAttributeReferredToByUser;
  }

  
  public void setGroupAttributeReferredToByUser(String groupAttributeReferredToByUser) {
    this.groupAttributeReferredToByUser = groupAttributeReferredToByUser;
  }

  
  public String getSyncMemberToId2AttributeValueFormat() {
    return syncMemberToId2AttributeValueFormat;
  }

  
  public void setSyncMemberToId2AttributeValueFormat(
      String syncMemberToId2AttributeValueFormat) {
    this.syncMemberToId2AttributeValueFormat = syncMemberToId2AttributeValueFormat;
  }

  
  public String getSyncMemberToId3AttributeValueFormat() {
    return syncMemberToId3AttributeValueFormat;
  }

  
  public void setSyncMemberToId3AttributeValueFormat(
      String syncMemberToId3AttributeValueFormat) {
    this.syncMemberToId3AttributeValueFormat = syncMemberToId3AttributeValueFormat;
  }

  
  public Set<String> getSubjectSourcesToProvision() {
    return subjectSourcesToProvision;
  }

  
  public void setSubjectSourcesToProvision(Set<String> subjectSourcesToProvision) {
    this.subjectSourcesToProvision = subjectSourcesToProvision;
  }

  
  public String getSyncMemberFromId2AttributeValueFormat() {
    return syncMemberFromId2AttributeValueFormat;
  }

  
  public void setSyncMemberFromId2AttributeValueFormat(
      String syncMemberFromId2AttributeValueFormat) {
    this.syncMemberFromId2AttributeValueFormat = syncMemberFromId2AttributeValueFormat;
  }

  
  public String getSyncMemberFromId3AttributeValueFormat() {
    return syncMemberFromId3AttributeValueFormat;
  }

  
  public void setSyncMemberFromId3AttributeValueFormat(
      String syncMemberFromId3AttributeValueFormat) {
    this.syncMemberFromId3AttributeValueFormat = syncMemberFromId3AttributeValueFormat;
  }

  
  public String getSyncGroupToId2AttributeValueFormat() {
    return syncGroupToId2AttributeValueFormat;
  }

  
  public void setSyncGroupToId2AttributeValueFormat(
      String syncGroupToId2AttributeValueFormat) {
    this.syncGroupToId2AttributeValueFormat = syncGroupToId2AttributeValueFormat;
  }

  
  public String getSyncGroupToId3AttributeValueFormat() {
    return syncGroupToId3AttributeValueFormat;
  }

  
  public void setSyncGroupToId3AttributeValueFormat(
      String syncGroupToId3AttributeValueFormat) {
    this.syncGroupToId3AttributeValueFormat = syncGroupToId3AttributeValueFormat;
  }

  
  public String getSyncGroupFromId2AttributeValueFormat() {
    return syncGroupFromId2AttributeValueFormat;
  }

  
  public void setSyncGroupFromId2AttributeValueFormat(
      String syncGroupFromId2AttributeValueFormat) {
    this.syncGroupFromId2AttributeValueFormat = syncGroupFromId2AttributeValueFormat;
  }

  
  public String getSyncGroupFromId3AttributeValueFormat() {
    return syncGroupFromId3AttributeValueFormat;
  }

  
  public void setSyncGroupFromId3AttributeValueFormat(
      String syncGroupFromId3AttributeValueFormat) {
    this.syncGroupFromId3AttributeValueFormat = syncGroupFromId3AttributeValueFormat;
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

  
  public String getGroupSearchAttributeName() {
    return groupSearchAttributeName;
  }

  
  public void setGroupSearchAttributeName(String groupSearchAttributeName) {
    this.groupSearchAttributeName = groupSearchAttributeName;
  }

  
  public String getGroupSearchAttributeValueFormat() {
    return groupSearchAttributeValueFormat;
  }

  
  public void setGroupSearchAttributeValueFormat(String groupSearchAttributeValueFormat) {
    this.groupSearchAttributeValueFormat = groupSearchAttributeValueFormat;
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
