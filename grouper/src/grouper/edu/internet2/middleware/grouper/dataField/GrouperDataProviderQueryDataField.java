package edu.internet2.middleware.grouper.dataField;


public class GrouperDataProviderQueryDataField {

  //  # data field for this column
  //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 

  private String providerDataFieldConfigId;
  
  //  # mapping type for this data field
  //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 

  private String providerDataFieldMappingType;

  //  # mapping type for this data field
  //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
  
  private String providerDataFieldAttribute;

  
  public String getProviderDataFieldConfigId() {
    return providerDataFieldConfigId;
  }

  
  public void setProviderDataFieldConfigId(String providerDataFieldConfigId) {
    this.providerDataFieldConfigId = providerDataFieldConfigId;
  }

  
  public String getProviderDataFieldMappingType() {
    return providerDataFieldMappingType;
  }

  
  public void setProviderDataFieldMappingType(String providerDataFieldMappingType) {
    this.providerDataFieldMappingType = providerDataFieldMappingType;
  }

  
  public String getProviderDataFieldAttribute() {
    return providerDataFieldAttribute;
  }

  
  public void setProviderDataFieldAttribute(String providerDataFieldAttribute) {
    this.providerDataFieldAttribute = providerDataFieldAttribute;
  }

}
