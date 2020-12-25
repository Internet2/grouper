package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningObjectMetadataItem {

  
  /**
   * camelCase alphaNumeric unique name per provisioner
   */
  private String name; 
  private GrouperProvisioningObjectMetadataItemValueType valueType;
  private GrouperProvisioningObjectMetadataItemFormElementType formElementType;
  private boolean required;
  private String labelKey;
  private String descriptionKey;
  private Object defaultValue;
  private String groupIdThatCanView;
  private String groupIdThatCanUpdate;
  private List<MultiKey> keysAndLabelsForDropdown = new ArrayList<MultiKey>();
  
  private boolean showForGroup;
  private boolean showForMember;
  private boolean showForMembership;
  private boolean showForFolder;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public GrouperProvisioningObjectMetadataItemValueType getValueType() {
    return valueType;
  }
  
  public void setValueType(GrouperProvisioningObjectMetadataItemValueType valueType) {
    this.valueType = valueType;
  }
  
  public GrouperProvisioningObjectMetadataItemFormElementType getFormElementType() {
    return formElementType;
  }
  
  public void setFormElementType(
      GrouperProvisioningObjectMetadataItemFormElementType formElementType) {
    this.formElementType = formElementType;
  }
  
  public boolean isRequired() {
    return required;
  }
  
  public void setRequired(boolean required) {
    this.required = required;
  }
  
  public String getLabelKey() {
    return labelKey;
  }
  
  public void setLabelKey(String labelKey) {
    this.labelKey = labelKey;
  }
  
  public String getDescriptionKey() {
    return descriptionKey;
  }
  
  public void setDescriptionKey(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }
  
  
  public Object getDefaultValue() {
    return defaultValue;
  }

  
  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getGroupIdThatCanView() {
    return groupIdThatCanView;
  }
  
  public void setGroupIdThatCanView(String groupIdThatCanView) {
    this.groupIdThatCanView = groupIdThatCanView;
  }
  
  public String getGroupIdThatCanUpdate() {
    return groupIdThatCanUpdate;
  }
  
  public void setGroupIdThatCanUpdate(String groupIdThatCanUpdate) {
    this.groupIdThatCanUpdate = groupIdThatCanUpdate;
  }
  
  public List<MultiKey> getKeysAndLabelsForDropdown() {
    return keysAndLabelsForDropdown;
  }
  
  public void setKeysAndLabelsForDropdown(List<MultiKey> keysAndLabelsForDropdown) {
    this.keysAndLabelsForDropdown = keysAndLabelsForDropdown;
  }

  
  public boolean isShowForGroup() {
    return showForGroup;
  }

  
  public void setShowForGroup(boolean showForGroup) {
    this.showForGroup = showForGroup;
  }

  
  public boolean isShowForMember() {
    return showForMember;
  }

  
  public void setShowForMember(boolean showForMember) {
    this.showForMember = showForMember;
  }

  
  public boolean isShowForMembership() {
    return showForMembership;
  }

  
  public void setShowForMembership(boolean showForMembership) {
    this.showForMembership = showForMembership;
  }

  
  public boolean isShowForFolder() {
    return showForFolder;
  }

  
  public void setShowForFolder(boolean showForFolder) {
    this.showForFolder = showForFolder;
  }

}
