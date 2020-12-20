package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * group in target system
 * @author mchyzer
 *
 */
public class ProvisioningGroup extends ProvisioningUpdatable {
  
  private ProvisioningGroupWrapper provisioningGroupWrapper;
  
  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   */
  private String id;

  /**
   * name of group in target system.  could be group system name, extension, or other
   */
  private String name;
  
  /**
   * id index in target (optional)
   */
  private Long idIndex;

  /**
   * display name (optional)
   */
  private String displayName;
  
  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @return id
   */
  public String getId() {
    return this.id;
  }

  /**
   * id uniquely identifies this record, might be a target uuid, or grouper id index, uuid, or name
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of group in target system.  could be group system name, extension, or other
   * @param name1
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * id index in target (optional)
   * @return id index
   */
  public Long getIdIndex() {
    return this.idIndex;
  }

  /**
   * id index in target (optional)
   * @param idIndex1
   */
  public void setIdIndex(Long idIndex1) {
    this.idIndex = idIndex1;
  }

  /**
   * display name (optional)
   * @return display name
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * display name (optional)
   * @param displayName1
   */
  public void setDisplayName(String displayName1) {
    this.displayName = displayName1;
  }

  public ProvisioningGroupWrapper getProvisioningGroupWrapper() {
    return provisioningGroupWrapper;
  }


  public void setProvisioningGroupWrapper(ProvisioningGroupWrapper provisioningGroupWrapper) {
    this.provisioningGroupWrapper = provisioningGroupWrapper;
  }

  public String toString() {
    StringBuilder result = new StringBuilder("Group(");
    boolean firstField = true;
    firstField = toStringAppendField(result, firstField, "id", this.id);
    firstField = toStringAppendField(result, firstField, "idIndex", this.idIndex);
    firstField = toStringAppendField(result, firstField, "name", this.name);
    firstField = toStringAppendField(result, firstField, "displayName", this.displayName);
    firstField = this.toStringProvisioningUpdatable(result, firstField);
    
    if (this.provisioningGroupWrapper != null) {
      if (this.provisioningGroupWrapper.isRecalc()) {
        firstField = toStringAppendField(result, firstField, "recalc", this.provisioningGroupWrapper.isRecalc());
      }
      if (this.provisioningGroupWrapper.isCreate()) {
        firstField = toStringAppendField(result, firstField, "create", this.provisioningGroupWrapper.isCreate());
      }
      if (this.provisioningGroupWrapper.isDelete()) {
        firstField = toStringAppendField(result, firstField, "delete", this.provisioningGroupWrapper.isDelete());
      }
      if (this.provisioningGroupWrapper.isIncrementalSyncMemberships()) {
        firstField = toStringAppendField(result, firstField, "incrementalSyncMemberships", this.provisioningGroupWrapper.isIncrementalSyncMemberships());
      }
      if (this.provisioningGroupWrapper.getErrorCode() != null) {
        firstField = toStringAppendField(result, firstField, "errorCode", this.provisioningGroupWrapper.getErrorCode().name());
      }
    }
    
    return result.append(")").toString();
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public ProvisioningGroup clone() {

    ProvisioningGroup provisioningGroup = new ProvisioningGroup();

    this.cloneUpdatable(provisioningGroup);
    provisioningGroup.displayName = this.displayName;
    provisioningGroup.id = this.id;
    provisioningGroup.idIndex = this.idIndex;
    provisioningGroup.name = this.name;
    provisioningGroup.provisioningGroupWrapper = this.provisioningGroupWrapper;

    return provisioningGroup;
  }

  public void assignSearchFilter() {
    String groupSearchFilter = this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchFilter();
    if (!StringUtils.isBlank(groupSearchFilter)) {
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("targetGroup", this);
      String result = GrouperUtil.stringValue(this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperTranslator().runExpression(groupSearchFilter, variableMap));
      this.setSearchFilter(result);
    }
  }

  /**
   * see if this object is empty e.g. after translating if empty then dont keep track of group
   * since the translation might have affected another object
   * @return
   */
  public boolean isEmpty() {
    if (StringUtils.isBlank(this.displayName)
        && StringUtils.isBlank(this.id)
        && this.idIndex == null
        && StringUtils.isBlank(this.name)
        && this.isEmptyUpdatable()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean canInsertAttribute(String name) {
    return this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canGroupInsertAttribute(name);
  }

  @Override
  public boolean canUpdateAttribute(String name) {
    return this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canGroupUpdateAttribute(name);
  }

  @Override
  public boolean canDeleteAttrbute(String name) {
    return this.getProvisioningGroupWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canGroupDeleteAttribute(name);
  }

  /**
   * 
   * @param name
   * @param value
   */
  public String retrieveFieldOrAttributeValueString(GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    return GrouperUtil.stringValue(this.retrieveFieldOrAttributeValue(grouperProvisioningConfigurationAttribute));
    
  }

  
  /**
   * base on attribute get the value
   * @param grouperProvisioningConfigurationAttribute
   * @return the value
   */
  public Object retrieveFieldOrAttributeValue(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      throw new NullPointerException("attribute is null: " + this);
    }
    if (grouperProvisioningConfigurationAttribute.isAttribute()) {
      return this.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName());
    } else {
      if ("displayName".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getDisplayName();
      }
      if ("id".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getId();
      }
      if ("idIndex".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getIdIndex() == null ? null : this.getIdIndex().toString();
      }
      if ("name".equals(grouperProvisioningConfigurationAttribute.getName())) {
        return this.getName();
      }
      throw new RuntimeException("Invalid field name '" + grouperProvisioningConfigurationAttribute.getName() + "': " + this);
    }
  }

}
