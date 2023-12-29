package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.app.provisioning.GrouperIncrementalDataItem;

public class GuiDataFieldRowDictionary {
  
  private String dataFieldConfigId;
  
  private String dataFieldAliases;
  
  private String description;
  
  private String privilege;
  
  private String dataType;
  
  private String dataOwner;
  
  private String howToGetAccess;
  
  private String examples;
  
  private String valueType;
  
  private boolean multiValued;

  
  public String getDataFieldAliases() {
    return dataFieldAliases;
  }

  
  public void setDataFieldAliases(String dataFieldAliases) {
    this.dataFieldAliases = dataFieldAliases;
  }

  
  public String getDescription() {
    return description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getPrivilege() {
    return privilege;
  }

  
  public void setPrivilege(String privilege) {
    this.privilege = privilege;
  }

  
  public String getDataType() {
    return dataType;
  }

  
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  
  public String getDataOwner() {
    return dataOwner;
  }

  
  public void setDataOwner(String dataOwner) {
    this.dataOwner = dataOwner;
  }

  
  public String getHowToGetAccess() {
    return howToGetAccess;
  }

  
  public void setHowToGetAccess(String howToGetAccess) {
    this.howToGetAccess = howToGetAccess;
  }

  
  public String getExamples() {
    return examples;
  }

  
  public void setExamples(String examples) {
    this.examples = examples;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
  
  public boolean isMultiValued() {
    return multiValued;
  }
  
  public void setMultiValued(boolean multiValued) {
    this.multiValued = multiValued;
  }

  public String getDataFieldConfigId() {
    return dataFieldConfigId;
  }

  public void setDataFieldConfigId(String dataFieldConfigId) {
    this.dataFieldConfigId = dataFieldConfigId;
  }


  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.dataFieldConfigId).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GuiDataFieldRowDictionary)) {
      return false;
    }
    GuiDataFieldRowDictionary guiDataFieldRowDictionary = (GuiDataFieldRowDictionary)obj;
    return new EqualsBuilder()
        .append(this.dataFieldConfigId, guiDataFieldRowDictionary.dataFieldConfigId)
        .isEquals();
  }
  
}
