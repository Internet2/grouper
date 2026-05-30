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
  
  private String privacyRealmConfigId;
  
  private String privilegeHumanized;
  
  private String dataRowName;
  
  private String dataRowConfigId;
  
  private String jexlSnippet;
  
  private String dataRowDataOwner;
  
  private String dataRowHowToGetAccess;

  
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

  public String getPrivacyRealmConfigId() {
    return privacyRealmConfigId;
  }

  public void setPrivacyRealmConfigId(String privacyRealmConfigId) {
    this.privacyRealmConfigId = privacyRealmConfigId;
  }

  public String getPrivilegeHumanized() {
    return privilegeHumanized;
  }

  public void setPrivilegeHumanized(String privilegeHumanized) {
    this.privilegeHumanized = privilegeHumanized;
  }

  public String getDataRowName() {
    return dataRowName;
  }

  public void setDataRowName(String dataRowName) {
    this.dataRowName = dataRowName;
  }

  public String getDataRowConfigId() {
    return dataRowConfigId;
  }

  public void setDataRowConfigId(String dataRowConfigId) {
    this.dataRowConfigId = dataRowConfigId;
  }

  public String getJexlSnippet() {
    return jexlSnippet;
  }

  public void setJexlSnippet(String jexlSnippet) {
    this.jexlSnippet = jexlSnippet;
  }

  public String getDataRowDataOwner() {
    return dataRowDataOwner;
  }

  public void setDataRowDataOwner(String dataRowDataOwner) {
    this.dataRowDataOwner = dataRowDataOwner;
  }

  public String getDataRowHowToGetAccess() {
    return dataRowHowToGetAccess;
  }

  public void setDataRowHowToGetAccess(String dataRowHowToGetAccess) {
    this.dataRowHowToGetAccess = dataRowHowToGetAccess;
  }

  /**
   * Returns the humanized privilege string for the stored privilege value.
   * @param privilege raw privilege value
   * @return humanized string
   */
  public static String humanizePrivilege(String privilege) {
    if ("view".equals(privilege)) {
      return "See that this field exists";
    } else if ("read".equals(privilege)) {
      return "See who has this value";
    } else if ("update".equals(privilege)) {
      return "Use this field in ABAC scripts";
    }
    return privilege;
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
