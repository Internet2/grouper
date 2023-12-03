package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperDataFieldConfig {

  /**
   * 
   */
  public GrouperDataFieldConfig() {
  }

  /**
   * 
   * @param configId
   */
  public void readFromConfig(String configId) {
    
    this.configId = configId;
    
    String fieldAliasesString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".fieldAliases");
    this.fieldAliases = GrouperUtil.splitTrimToSet(fieldAliasesString, ",");

    this.grouperPrivacyRealmConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".fieldPrivacyRealm");
    
    String fieldDataStructureString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".fieldDataStructure", "attribute");
    GrouperDataFieldStructure grouperDataFieldStructure = GrouperDataFieldStructure.valueOfIgnoreCase(fieldDataStructureString, true);
    this.fieldDataStructure = grouperDataFieldStructure;
    
    String fieldDataTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".fieldDataType", "string");
    GrouperDataFieldType grouperDataFieldType = GrouperDataFieldType.valueOfIgnoreCase(fieldDataTypeString, true);
    this.fieldDataType = grouperDataFieldType;

    this.fieldMultiValued = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDataField." + configId + ".fieldMultiValued", false);
    
    this.descriptionHtml = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperDataField." + configId + ".descriptionHtml");
    this.dataOwnerHtml = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".dataOwnerHtml");
    this.howToGetAccessHtml = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".howToGetAccessHtml");
    this.zeroToManyExamplesHtml = GrouperConfig.retrieveConfig().propertyValueString("grouperDataField." + configId + ".zeroToManyExamplesHtml");
    
  }
  
  private String configId;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }
  
  private String grouperPrivacyRealmConfigId; 
  
  
  public String getGrouperPrivacyRealmConfigId() {
    return grouperPrivacyRealmConfigId;
  }

  /**
   * aliases that this field is referred to as
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
   * grouperDataField.dataFieldConfigId.fieldAliases = 
   */
  private Set<String> fieldAliases = new HashSet<>();
  
  /**
   * # if this field can have multiple values
   * # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataField\\.[^.]+\\.fieldMultiValued$"}
   * # grouperDataField.dataFieldConfigId.fieldMultiValued = 
   */
  private boolean fieldMultiValued = false;
  
  /**
   * # data type for this field
   * # {valueType: "string", defaultValue: "string", regex: "^grouperDataField\\.[^.]+\\.fieldDataType$", formElement: "dropdown", optionValues: ["string", "integer", "timestamp", "boolean"]}
   * # grouperDataField.dataFieldConfigId.fieldDataType = 
   */
  private GrouperDataFieldType fieldDataType;

  /**
   * # data structure for this field
   * # {valueType: "string", defaultValue: "attribute", regex: "^grouperDataField\\.[^.]+\\.fieldDataStructure$", formElement: "dropdown", optionValues: ["attribute", "rowColumn"]}
   * # grouperDataField.dataFieldConfigId.fieldDataStructure = 
   */
  private GrouperDataFieldStructure fieldDataStructure;
  
  /**
   * # Description html
   * # {valueType: "string", required: true, regex: "^dataRowConfigId\\.[^.]+\\.descriptionHtml$", formElement: "textarea"}
   */
  private String descriptionHtml;
  
  /**
   * # Data owner html
   * # {valueType: "string", regex: "^dataRowConfigId\\.[^.]+\\.dataOwnerHtml$", formElement: "textarea"}
   */
  private String dataOwnerHtml;
  
  /**
   * # How to get access html
   * # {valueType: "string", regex: "^dataRowConfigId\\.[^.]+\\.howToGetAccessHtml$", formElement: "textarea"}
   */
  private String howToGetAccessHtml;
  
  /**
   * # Zero to many examples html
   * # {valueType: "string", regex: "^dataRowConfigId\\.[^.]+\\.zeroToManyExamplesHtml$", formElement: "textarea"}
   */
  private String zeroToManyExamplesHtml;

  /**
   * aliases that this field is referred to as
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
   * grouperDataField.dataFieldConfigId.fieldAliases = 
   * @return field aliases
   */
  public Set<String> getFieldAliases() {
    return fieldAliases;
  }

  /**
   * aliases that this field is referred to as
   * {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
   * grouperDataField.dataFieldConfigId.fieldAliases = 
   * @param fieldAliases
   */
  public void setFieldAliases(Set<String> fieldAliases) {
    this.fieldAliases = fieldAliases;
  }

  /**
   * # if this field can have multiple values
   * # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataField\\.[^.]+\\.fieldMultiValued$"}
   * # grouperDataField.dataFieldConfigId.fieldMultiValued = 
   * @return
   */
  public boolean isFieldMultiValued() {
    return fieldMultiValued;
  }

  /**
   * # if this field can have multiple values
   * # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataField\\.[^.]+\\.fieldMultiValued$"}
   * # grouperDataField.dataFieldConfigId.fieldMultiValued = 
   * @param fieldMultiValued
   */
  public void setFieldMultiValued(boolean fieldMultiValued) {
    this.fieldMultiValued = fieldMultiValued;
  }

  /**
   * # data type for this field
   * # {valueType: "string", defaultValue: "string", regex: "^grouperDataField\\.[^.]+\\.fieldDataType$", formElement: "dropdown", optionValues: ["string", "integer", "timestamp", "boolean"]}
   * # grouperDataField.dataFieldConfigId.fieldDataType = 
   * @return
   */
  public GrouperDataFieldType getFieldDataType() {
    return fieldDataType;
  }

  /**
   * # data type for this field
   * # {valueType: "string", defaultValue: "string", regex: "^grouperDataField\\.[^.]+\\.fieldDataType$", formElement: "dropdown", optionValues: ["string", "integer", "timestamp", "boolean"]}
   * # grouperDataField.dataFieldConfigId.fieldDataType = 
   * @param fieldDataType
   */
  public void setFieldDataType(GrouperDataFieldType fieldDataType) {
    this.fieldDataType = fieldDataType;
  }

  /**
   * # data structure for this field
   * # {valueType: "string", defaultValue: "attribute", regex: "^grouperDataField\\.[^.]+\\.fieldDataStructure$", formElement: "dropdown", optionValues: ["attribute", "rowColumn"]}
   * # grouperDataField.dataFieldConfigId.fieldDataStructure = 
   * @return
   */
  public GrouperDataFieldStructure getFieldDataStructure() {
    return fieldDataStructure;
  }

  /**
   * # data structure for this field
   * # {valueType: "string", defaultValue: "attribute", regex: "^grouperDataField\\.[^.]+\\.fieldDataStructure$", formElement: "dropdown", optionValues: ["attribute", "rowColumn"]}
   * # grouperDataField.dataFieldConfigId.fieldDataStructure = 
   * @param fieldDataStructure
   */
  public void setFieldDataStructure(GrouperDataFieldStructure fieldDataStructure) {
    this.fieldDataStructure = fieldDataStructure;
  }

  
  public String getDescriptionHtml() {
    return descriptionHtml;
  }

  
  public void setDescriptionHtml(String descriptionHtml) {
    this.descriptionHtml = descriptionHtml;
  }

  
  public String getDataOwnerHtml() {
    return dataOwnerHtml;
  }

  
  public void setDataOwnerHtml(String dataOwnerHtml) {
    this.dataOwnerHtml = dataOwnerHtml;
  }

  
  public String getHowToGetAccessHtml() {
    return howToGetAccessHtml;
  }

  
  public void setHowToGetAccessHtml(String howToGetAccessHtml) {
    this.howToGetAccessHtml = howToGetAccessHtml;
  }

  
  public String getZeroToManyExamplesHtml() {
    return zeroToManyExamplesHtml;
  }

  
  public void setZeroToManyExamplesHtml(String zeroToManyExamplesHtml) {
    this.zeroToManyExamplesHtml = zeroToManyExamplesHtml;
  }

}
