package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDataProviderQueryFieldConfig {

  public GrouperDataProviderQueryFieldConfig() {
  }

  /**
   * 
   * @param configId
   */
  public void readFromConfig(String configId, int index) {

    //  # data field for this column
    //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 
    this.providerDataFieldConfigId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperDataProviderQuery." + configId + ".providerQueryDataField." + index + ".providerDataFieldConfigId");

    //  # mapping type for this data field
    //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 
    String fieldMappingTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataField." + index + ".providerDataFieldMappingType");
    GrouperDataProviderQueryFieldMappingType grouperDataProviderQueryFieldMappingType = 
        GrouperDataProviderQueryFieldMappingType.valueOfIgnoreCase(fieldMappingTypeString, true);
    this.providerDataFieldMappingType = grouperDataProviderQueryFieldMappingType;

    //  # mapping type for this data field
    //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
    this.providerDataFieldAttribute = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperDataProviderQuery." + configId + ".providerQueryDataField." + index + ".providerDataFieldAttribute");
    
  }  



  
  /**
   * data field for this column
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 
   */
  private String providerDataFieldConfigId;
  
  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 
   */
  private GrouperDataProviderQueryFieldMappingType providerDataFieldMappingType;
  
  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
   */
  private String providerDataFieldAttribute;

  /**
   * data field for this column
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 
   * @return
   */
  public String getProviderDataFieldConfigId() {
    return providerDataFieldConfigId;
  }

  /**
   * data field for this column
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 
   * @param providerDataFieldConfigId
   */
  public void setProviderDataFieldConfigId(String providerDataFieldConfigId) {
    this.providerDataFieldConfigId = providerDataFieldConfigId;
  }

  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 
   * @return
   */
  public GrouperDataProviderQueryFieldMappingType getProviderDataFieldMappingType() {
    return providerDataFieldMappingType;
  }

  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 
   * @param providerDataFieldMappingType
   */
  public void setProviderDataFieldMappingType(GrouperDataProviderQueryFieldMappingType providerDataFieldMappingType) {
    this.providerDataFieldMappingType = providerDataFieldMappingType;
  }

  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
   * @return
   */
  public String getProviderDataFieldAttribute() {
    return providerDataFieldAttribute;
  }

  /**
   * mapping type for this data field
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
   * @param providerDataFieldAttribute
   */
  public void setProviderDataFieldAttribute(String providerDataFieldAttribute) {
    this.providerDataFieldAttribute = providerDataFieldAttribute;
  }
  
  
}
