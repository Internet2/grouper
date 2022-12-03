package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class GrouperDataProviderQuery {

  public static void main(String[] args) {

  }

  public static GrouperDataProviderQuery parseFromConfig(String configId, GrouperConfig grouperConfig) {
    GrouperDataProviderQuery grouperDataProviderQuery = new GrouperDataProviderQuery();
    
    grouperDataProviderQuery.setConfigId(configId);

    grouperDataProviderQuery.setProviderConfigId(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerConfigId"));
    grouperDataProviderQuery.setProviderQueryType(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryType"));
    grouperDataProviderQuery.setProviderQuerySqlConfigId(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySqlConfigId"));
    grouperDataProviderQuery.setProviderQuerySqlQuery(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySqlQuery"));
    grouperDataProviderQuery.setProviderQueryDataStructure(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataStructure"));
    grouperDataProviderQuery.setProviderQueryRowConfigId(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryRowConfigId"));
    grouperDataProviderQuery.setProviderQuerySubjectIdAttribute(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectIdAttribute"));
    grouperDataProviderQuery.setProviderQuerySubjectIdType(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectIdType"));
    grouperDataProviderQuery.setProviderQuerySubjectSourceId(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectSourceId"));
    grouperDataProviderQuery.setProviderQueryNumberOfDataFields(grouperConfig.propertyValueInt("grouperDataProviderQuery." + configId + ".providerQueryNumberOfDataFields"));

    List<GrouperDataProviderQueryDataField> grouperDataProviderQueryDataFields = new ArrayList<GrouperDataProviderQueryDataField>();
    grouperDataProviderQuery.setGrouperDataProviderQueryDataFields(grouperDataProviderQueryDataFields);
    for (int i=0;i<grouperDataProviderQuery.getProviderQueryNumberOfDataFields();i++) {
      
      GrouperDataProviderQueryDataField grouperDataProviderQueryDataField = new GrouperDataProviderQueryDataField();
      grouperDataProviderQueryDataFields.add(grouperDataProviderQueryDataField);
      
      //  # data field for this column
      //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
      //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldConfigId = 

      grouperDataProviderQueryDataField.setProviderDataFieldConfigId(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataField." + i + ".providerDataFieldConfigId" ));
      
      //  # mapping type for this data field
      //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldMappingType$", formElement: "dropdown", optionValues: ["attribute"]}
      //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldMappingType = 

      grouperDataProviderQueryDataField.setProviderDataFieldMappingType(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataField." + i + ".providerDataFieldMappingType" ));

      //  # mapping type for this data field
      //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.providerQueryDataField\\.[0-9]+\\.providerDataFieldAttribute$", showEl: "${providerQueryDataField.$i$.providerDataFieldMappingType == 'attribute'}"}
      //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataField.$i$.providerDataFieldAttribute = 
      
      grouperDataProviderQueryDataField.setProviderDataFieldAttribute(grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataField." + i + ".providerDataFieldAttribute" ));
      
    }

    return grouperDataProviderQuery;
  }
  
  private String configId;
  
  //  # data provider config id
  //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 

  private String providerConfigId;
  
  //  # data provider query type
  //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryType$", formElement: "dropdown", optionValues: ["sql"]}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryType = 

  private String providerQueryType;
  
  //  # SQL config id
  //  # {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 

  private String providerQuerySqlConfigId;
  
  //  # SQL query
  //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =

  private String providerQuerySqlQuery;
  
  //  # Data structure
  //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryDataStructure$", formElement: "dropdown", optionValues: ["dataFields", "dataRow"]}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataStructure =

  private String providerQueryDataStructure;
  
  //  # Data row to link to
  //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryRowConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider", showEl: "${providerQueryDataStructure == 'dataRow'}"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryRowConfigId =

  private String providerQueryRowConfigId;
  
  //  # Attribute which links this data to subjects 
  //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdAttribute$"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdAttribute = 

  private String providerQuerySubjectIdAttribute;
  
  //  # Which type of subject id
  //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdType$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdType = 

  private String providerQuerySubjectIdType;
  
  //  # which subject source this is a subject id for
  //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectSourceId$"}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectSourceId = 

  private String providerQuerySubjectSourceId;
  
  //  # number of fields in this row
  //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
  //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryNumberOfDataFields = 

  private int providerQueryNumberOfDataFields;

  private List<GrouperDataProviderQueryDataField> grouperDataProviderQueryDataFields = null;
  
  
  
  
  public List<GrouperDataProviderQueryDataField> getGrouperDataProviderQueryDataFields() {
    return grouperDataProviderQueryDataFields;
  }

  
  public void setGrouperDataProviderQueryDataFields(
      List<GrouperDataProviderQueryDataField> grouperDataProviderQueryDataFields) {
    this.grouperDataProviderQueryDataFields = grouperDataProviderQueryDataFields;
  }

  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public String getProviderConfigId() {
    return providerConfigId;
  }

  
  public void setProviderConfigId(String providerConfigId) {
    this.providerConfigId = providerConfigId;
  }

  
  public String getProviderQueryType() {
    return providerQueryType;
  }

  
  public void setProviderQueryType(String providerQueryType) {
    this.providerQueryType = providerQueryType;
  }

  
  public String getProviderQuerySqlConfigId() {
    return providerQuerySqlConfigId;
  }

  
  public void setProviderQuerySqlConfigId(String providerQuerySqlConfigId) {
    this.providerQuerySqlConfigId = providerQuerySqlConfigId;
  }

  
  public String getProviderQuerySqlQuery() {
    return providerQuerySqlQuery;
  }

  
  public void setProviderQuerySqlQuery(String providerQuerySqlQuery) {
    this.providerQuerySqlQuery = providerQuerySqlQuery;
  }

  
  public String getProviderQueryDataStructure() {
    return providerQueryDataStructure;
  }

  
  public void setProviderQueryDataStructure(String providerQueryDataStructure) {
    this.providerQueryDataStructure = providerQueryDataStructure;
  }

  
  public String getProviderQueryRowConfigId() {
    return providerQueryRowConfigId;
  }

  
  public void setProviderQueryRowConfigId(String providerQueryRowConfigId) {
    this.providerQueryRowConfigId = providerQueryRowConfigId;
  }

  
  public String getProviderQuerySubjectIdAttribute() {
    return providerQuerySubjectIdAttribute;
  }

  
  public void setProviderQuerySubjectIdAttribute(String providerQuerySubjectIdAttribute) {
    this.providerQuerySubjectIdAttribute = providerQuerySubjectIdAttribute;
  }

  
  public String getProviderQuerySubjectIdType() {
    return providerQuerySubjectIdType;
  }

  
  public void setProviderQuerySubjectIdType(String providerQuerySubjectIdType) {
    this.providerQuerySubjectIdType = providerQuerySubjectIdType;
  }

  
  public String getProviderQuerySubjectSourceId() {
    return providerQuerySubjectSourceId;
  }

  
  public void setProviderQuerySubjectSourceId(String providerQuerySubjectSourceId) {
    this.providerQuerySubjectSourceId = providerQuerySubjectSourceId;
  }

  
  public int getProviderQueryNumberOfDataFields() {
    return providerQueryNumberOfDataFields;
  }

  
  public void setProviderQueryNumberOfDataFields(int providerQueryNumberOfDataFields) {
    this.providerQueryNumberOfDataFields = providerQueryNumberOfDataFields;
  }
  

}
