package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperScim2ProvisionerConfiguration extends GrouperProvisioningConfiguration {

  private String bearerTokenExternalSystemConfigId;
  
  private String scimType;
  
  private String acceptHeader;

  private String scimNamePatchStrategy;
  
  private String scimEmailPatchStrategy;
  
  private String scimContentType;
  
  private int scimMembershipBatchSize = 100;
  
  private String membershipStrategy;
  
  private boolean scimIgnorePagingMetadata = false;
  
  private boolean scimRetrieveMembershipsByUser = true;
  
  private boolean scimRetrieveMembershipsByGroup = true;

  private String scimEmailFilterStrategy;
  
  public boolean isGithubOrgConfiguration() {
    return StringUtils.equals("Github", this.getScimType())
        && this.isOperateOnGrouperGroups()
        && this.getTargetGroupAttributeNameToConfig().containsKey("id");
  }
  
  private boolean disableEntitiesInsteadOfDelete = false;
  
  private boolean includeActiveOnEntityCreate = true;
  
  private Map<String, String> entityAttributeJsonValueType = new HashMap<>();
  
  private Map<String, String> entityAttributeJsonPointer = new HashMap<>();
  
  private Map<String, String> groupAttributeJsonValueType = new HashMap<>();
  
  private Map<String, String> groupAttributeJsonPointer = new HashMap<>();
  
  private boolean selectAllMemberships = true;
  
  
  public int getScimMembershipBatchSize() {
    return scimMembershipBatchSize;
  }

  
  public void setScimMembershipBatchSize(int scimMembershipBatchSize) {
    this.scimMembershipBatchSize = scimMembershipBatchSize;
  }

  public Map<String, String> getGroupAttributeJsonValueType() {
    return groupAttributeJsonValueType;
  }
  
  public void setGroupAttributeJsonValueType(
      Map<String, String> groupAttributeJsonValueType) {
    this.groupAttributeJsonValueType = groupAttributeJsonValueType;
  }

  
  public Map<String, String> getGroupAttributeJsonPointer() {
    return groupAttributeJsonPointer;
  }

  
  public void setGroupAttributeJsonPointer(Map<String, String> groupAttributeJsonPointer) {
    this.groupAttributeJsonPointer = groupAttributeJsonPointer;
  }

  public Map<String, String> getEntityAttributeJsonValueType() {
    return entityAttributeJsonValueType;
  }

  public void setEntityAttributeJsonValueType(Map<String, String> entityAttributeJsonValueType) {
    this.entityAttributeJsonValueType = entityAttributeJsonValueType;
  }
  
  public Map<String, String> getEntityAttributeJsonPointer() {
    return entityAttributeJsonPointer;
  }

  public void setEntityAttributeJsonPointer(
      Map<String, String> entityAttributeJsonPointer) {
    this.entityAttributeJsonPointer = entityAttributeJsonPointer;
  }


  public String getScimNamePatchStrategy() {
    return scimNamePatchStrategy;
  }


  public void setScimNamePatchStrategy(String scimNamePatchStrategy) {
    this.scimNamePatchStrategy = scimNamePatchStrategy;
  }
  
  
  public boolean isScimIgnorePagingMetadata() {
    return scimIgnorePagingMetadata;
  }

  
  public void setScimIgnorePagingMetadata(boolean scimIgnoreTotalResults) {
    this.scimIgnorePagingMetadata = scimIgnoreTotalResults;
  }

  public boolean isDisableEntitiesInsteadOfDelete() {
    return disableEntitiesInsteadOfDelete;
  }

  
  public void setDisableEntitiesInsteadOfDelete(boolean disableEntitiesInsteadOfDelete) {
    this.disableEntitiesInsteadOfDelete = disableEntitiesInsteadOfDelete;
  }
  
  
  public boolean isIncludeActiveOnEntityCreate() {
    return includeActiveOnEntityCreate;
  }

  
  public void setIncludeActiveOnEntityCreate(boolean includeActiveOnEntityCreate) {
    this.includeActiveOnEntityCreate = includeActiveOnEntityCreate;
  }
  
  public boolean isSelectAllMemberships() {
    return selectAllMemberships;
  }

  
  public void setSelectAllMemberships(boolean selectAllMemberships) {
    this.selectAllMemberships = selectAllMemberships;
  }

  public String getScimContentType() {
    return scimContentType;
  }

  
  public void setScimContentType(String scimContentType) {
    this.scimContentType = scimContentType;
  }

  
  public boolean isScimRetrieveMembershipsByUser() {
    return scimRetrieveMembershipsByUser;
  }

  
  public void setScimRetrieveMembershipsByUser(boolean scimRetrieveMembershipsByUser) {
    this.scimRetrieveMembershipsByUser = scimRetrieveMembershipsByUser;
  }

  
  public boolean isScimRetrieveMembershipsByGroup() {
    return scimRetrieveMembershipsByGroup;
  }

  
  public void setScimRetrieveMembershipsByGroup(boolean scimRetrieveMembershipsByGroup) {
    this.scimRetrieveMembershipsByGroup = scimRetrieveMembershipsByGroup;
  }
  
  
  public String getMembershipStrategy() {
    return membershipStrategy;
  }

  
  public void setMembershipStrategy(String membershipStrategy) {
    this.membershipStrategy = membershipStrategy;
  }

  @Override
  public void configureSpecificSettings() {
    
    this.bearerTokenExternalSystemConfigId = this.retrieveConfigString("bearerTokenExternalSystemConfigId", true);
    this.scimType = this.retrieveConfigString("scimType", true);
    this.acceptHeader = this.retrieveConfigString("acceptHeader", false);

    
    this.scimNamePatchStrategy = GrouperUtil.defaultIfBlank(this.retrieveConfigString("scimNamePatchStrategy", false), "nonqualified");
    this.membershipStrategy = this.retrieveConfigString("membershipStrategy", false);
    this.scimEmailPatchStrategy = GrouperUtil.defaultIfBlank(this.retrieveConfigString("scimEmailPatchStrategy", false), "pathEmails");
    this.scimContentType = GrouperUtil.defaultIfBlank(this.retrieveConfigString("scimContentType", false), "application/json");
    
    this.selectAllMemberships = GrouperUtil.booleanValue(this.retrieveConfigBoolean("selectAllMemberships", false), true);
    
    this.disableEntitiesInsteadOfDelete = GrouperUtil.booleanValue(this.retrieveConfigBoolean("disableEntitiesInsteadOfDelete", false), false);

    this.includeActiveOnEntityCreate = GrouperUtil.booleanValue(this.retrieveConfigBoolean("includeActiveOnEntityCreate", false), true);
    
    this.scimRetrieveMembershipsByUser = GrouperUtil.booleanValue(this.retrieveConfigBoolean("scimRetrieveMembershipsByUser", false), true);
    
    this.scimRetrieveMembershipsByGroup = GrouperUtil.booleanValue(this.retrieveConfigBoolean("scimRetrieveMembershipsByGroup", false), true);

    this.scimIgnorePagingMetadata = GrouperUtil.booleanValue(this.retrieveConfigBoolean("scimIgnorePagingMetadata", false), false);
    
    this.scimMembershipBatchSize = GrouperUtil.intValue(this.retrieveConfigInt("scimMembershipBatchSize", false), 100);

    this.scimEmailFilterStrategy = GrouperUtil.defaultIfBlank(this.retrieveConfigString("scimEmailFilterStrategy", false), "email");
    
    for (String attributeName : this.getTargetEntityAttributeNameToConfig().keySet()) {
      GrouperProvisioningConfigurationAttribute configurationAttribute = this.getTargetEntityAttributeNameToConfig().get(attributeName);
      int configIndex = configurationAttribute.getConfigIndex();
      String jsonValueType = this.retrieveConfigString("targetEntityAttribute."+configIndex+".jsonValueType", false);
      String entityAttributeJsonPointerVal = this.retrieveConfigString("targetEntityAttribute."+configIndex+".entityAttributeJsonPointer", false);
      if (StringUtils.isNotBlank(entityAttributeJsonPointerVal)) {
        this.entityAttributeJsonPointer.put(attributeName, entityAttributeJsonPointerVal);
      }
      if (StringUtils.isBlank(jsonValueType) || StringUtils.equals(jsonValueType, "string")) {
        continue;
      }
      entityAttributeJsonValueType.put(attributeName, jsonValueType);
    }
    
    for (String attributeName : this.getTargetGroupAttributeNameToConfig().keySet()) {
      GrouperProvisioningConfigurationAttribute configurationAttribute = this.getTargetGroupAttributeNameToConfig().get(attributeName);
      int configIndex = configurationAttribute.getConfigIndex();
      String jsonValueType = this.retrieveConfigString("targetGroupAttribute."+configIndex+".jsonValueType", false);
      String groupAttributeJsonPointerVal = this.retrieveConfigString("targetGroupAttribute."+configIndex+".groupAttributeJsonPointer", false);
      if (StringUtils.isNotBlank(groupAttributeJsonPointerVal)) {
        this.groupAttributeJsonPointer.put(attributeName, groupAttributeJsonPointerVal);
      }
      if (StringUtils.isBlank(jsonValueType) || StringUtils.equals(jsonValueType, "string")) {
        continue;
      }
      groupAttributeJsonValueType.put(attributeName, jsonValueType);
    }
  }

  public String getBearerTokenExternalSystemConfigId() {
    return bearerTokenExternalSystemConfigId;
  }

  public void setBearerTokenExternalSystemConfigId(String scimExternalSystemConfigId) {
    this.bearerTokenExternalSystemConfigId = scimExternalSystemConfigId;
  }

  
  public String getScimType() {
    return scimType;
  }

  
  public void setScimType(String scimType) {
    this.scimType = scimType;
  }

  
  public String getAcceptHeader() {
    return acceptHeader;
  }

  
  public void setAcceptHeader(String acceptHeader) {
    this.acceptHeader = acceptHeader;
  }

  
  public String getScimEmailFilterStrategy() {
    return scimEmailFilterStrategy;
  }

  public void setScimEmailFilterStrategy(String scimEmailFilterStrategy) {
    this.scimEmailFilterStrategy = scimEmailFilterStrategy;
  }

  public String getScimEmailPatchStrategy() {
    return scimEmailPatchStrategy;
  }

  
  public void setScimEmailPatchStrategy(String scimEmailPatchStrategy) {
    this.scimEmailPatchStrategy = scimEmailPatchStrategy;
  }
  
  
}