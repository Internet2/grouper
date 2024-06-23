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
  
  private boolean disableGroupsInsteadOfDelete = false;
  
  public boolean isGithubOrgConfiguration() {
    return StringUtils.equals("Github", this.getScimType())
        && this.isOperateOnGrouperGroups()
        && this.getTargetGroupAttributeNameToConfig().containsKey("id");
  }
  
  private boolean disableEntitiesInsteadOfDelete = false;
  
  private boolean includeActiveOnEntityCreate = true;
  
  private boolean includeActiveOnGroupCreate = true;
  
  private Map<String, String> entityAttributeJsonValueType = new HashMap<>();
  
  private Map<String, String> entityAttributeJsonPointer = new HashMap<>();
  
  private Map<String, String> groupAttributeJsonValueType = new HashMap<>();
  
  private Map<String, String> groupAttributeJsonPointer = new HashMap<>();
  
  
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



  public boolean isDisableGroupsInsteadOfDelete() {
    return disableGroupsInsteadOfDelete;
  }

  
  public void setDisableGroupsInsteadOfDelete(boolean disableGroupsInsteadOfDelete) {
    this.disableGroupsInsteadOfDelete = disableGroupsInsteadOfDelete;
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

  
  public boolean isIncludeActiveOnGroupCreate() {
    return includeActiveOnGroupCreate;
  }

  
  public void setIncludeActiveOnGroupCreate(boolean includeActiveOnGroupCreate) {
    this.includeActiveOnGroupCreate = includeActiveOnGroupCreate;
  }

  @Override
  public void configureSpecificSettings() {
    
    this.bearerTokenExternalSystemConfigId = this.retrieveConfigString("bearerTokenExternalSystemConfigId", true);
    this.scimType = this.retrieveConfigString("scimType", true);
    this.acceptHeader = this.retrieveConfigString("acceptHeader", false);
    this.disableGroupsInsteadOfDelete = GrouperUtil.booleanValue(this.retrieveConfigBoolean("disableGroupsInsteadOfDelete", false), false);
    this.disableEntitiesInsteadOfDelete = GrouperUtil.booleanValue(this.retrieveConfigBoolean("disableEntitiesInsteadOfDelete", false), false);

    this.includeActiveOnEntityCreate = GrouperUtil.booleanValue(this.retrieveConfigBoolean("includeActiveOnEntityCreate", false), true);
    this.includeActiveOnGroupCreate = GrouperUtil.booleanValue(this.retrieveConfigBoolean("includeActiveOnGroupCreate", false), true);
    
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

  public void setBearerTokenExternalSystemConfigId(String azureExternalSystemConfigId) {
    this.bearerTokenExternalSystemConfigId = azureExternalSystemConfigId;
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
  
  
}