package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;

public class GrouperDataProviderQueryConfig {

  public GrouperDataProviderQueryConfig() {

  }

  private String configId;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  /**
   * 
   * @param configId
   * @param grouperConfig optional
   */
  public void readFromConfig(String configId, GrouperConfig grouperConfig) {
    
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    this.configId = configId;
    
    //  # data provider config id
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 
    this.providerConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerConfigId");
    
    //  # data provider query type
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryType$", formElement: "dropdown", optionValues: ["sql"]}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryType = 
    String providerQueryTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryType");
    GrouperDataProviderQueryType grouperDataProviderQueryType = GrouperDataProviderQueryType.valueOfIgnoreCase(providerQueryTypeString, true);
    this.providerQueryType = grouperDataProviderQueryType;

    //  # SQL config id
    //  # {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 
    this.providerQuerySqlConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySqlConfigId");
   
    //  # SQL query
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =
    this.providerQuerySqlQuery = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySqlQuery");

    //  # Data structure
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryDataStructure$", formElement: "dropdown", optionValues: ["attribute", "row"]}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataStructure =
    String providerQueryDataStructureString = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryDataStructure");
    GrouperDataFieldStructure grouperDataFieldStructure = GrouperDataFieldStructure.valueOfIgnoreCase(providerQueryDataStructureString, true);
    this.providerQueryDataStructure = grouperDataFieldStructure;

    //  # Data row to link to
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryRowConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider", showEl: "${providerQueryDataStructure == 'dataRow'}"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryRowConfigId =
    this.providerQueryRowConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQueryRowConfigId");

    //  # Attribute which links this data to subjects 
    //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdAttribute$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdAttribute = 
    this.providerQuerySubjectIdAttribute = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectIdAttribute");

    //  # Which type of subject id
    //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdType$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdType = 
    this.providerQuerySubjectIdType = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectIdType");

    //  # which subject source this is a subject id for
    //  # {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectSourceId$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectSourceId = 
    this.providerQuerySubjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + configId + ".providerQuerySubjectSourceId");

    //  # number of fields in this row
    //  # {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryNumberOfDataFields = 
    this.providerQueryNumberOfDataFields = GrouperConfig.retrieveConfig().propertyValueInt("grouperDataProviderQuery." + configId + ".providerQueryNumberOfDataFields");

    for (int i=0;i<this.providerQueryNumberOfDataFields;i++) {

      GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig = new GrouperDataProviderQueryFieldConfig();
      grouperDataProviderQueryFieldConfig.readFromConfig(configId, i);
      this.grouperDataProviderQueryFieldConfigs.add(grouperDataProviderQueryFieldConfig);
      
    }

  }

  /**
   * data provider config id
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 
   */
  private String providerConfigId;

  
  
  /**
   * data provider config id
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 
   * @return
   */
  public String getProviderConfigId() {
    return providerConfigId;
  }

  /**
   * data provider config id
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 
   * @param providerConfigId
   */
  public void setProviderConfigId(String providerConfigId) {
    this.providerConfigId = providerConfigId;
  }

  /**
   * data provider query type
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryType$", formElement: "dropdown", optionValues: ["sql"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryType = 
   */
  private GrouperDataProviderQueryType providerQueryType;

  
  
  /**
   * data provider query type
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryType$", formElement: "dropdown", optionValues: ["sql"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryType = 
   * @return
   */
  public GrouperDataProviderQueryType getProviderQueryType() {
    return providerQueryType;
  }

  /**
   * data provider query type
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryType$", formElement: "dropdown", optionValues: ["sql"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryType = 
   * @param providerQueryType
   */
  public void setProviderQueryType(GrouperDataProviderQueryType providerQueryType) {
    this.providerQueryType = providerQueryType;
  }

  /**
   * SQL config id
   * {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 
   */
  private String providerQuerySqlConfigId;

  /**
   * SQL config id
   * {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 
   * @return
   */
  public String getProviderQuerySqlConfigId() {
    return providerQuerySqlConfigId;
  }

  /**
   * SQL config id
   * {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 
   * @param providerQuerySqlConfigId
   */
  public void setProviderQuerySqlConfigId(String providerQuerySqlConfigId) {
    this.providerQuerySqlConfigId = providerQuerySqlConfigId;
  }

  /**
   * SQL query
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =
   */
  private String providerQuerySqlQuery;

  
  
  /**
   * SQL query
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =
   * @return
   */
  public String getProviderQuerySqlQuery() {
    return providerQuerySqlQuery;
  }

  /**
   * SQL query
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =
   * @param providerQuerySqlQuery
   */
  public void setProviderQuerySqlQuery(String providerQuerySqlQuery) {
    this.providerQuerySqlQuery = providerQuerySqlQuery;
  }

  /**
   * Data structure
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryDataStructure$", formElement: "dropdown", optionValues: ["attribute", "row"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataStructure =
   */
  private GrouperDataFieldStructure providerQueryDataStructure;

  
  /**
   * Data structure
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryDataStructure$", formElement: "dropdown", optionValues: ["attribute", "row"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataStructure =
   * @return
   */
  public GrouperDataFieldStructure getProviderQueryDataStructure() {
    return providerQueryDataStructure;
  }

  /**
   * Data structure
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryDataStructure$", formElement: "dropdown", optionValues: ["attribute", "row"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryDataStructure =
   * @param providerQueryDataStructure
   */
  public void setProviderQueryDataStructure(
      GrouperDataFieldStructure providerQueryDataStructure) {
    this.providerQueryDataStructure = providerQueryDataStructure;
  }

  /**
   * Data row to link to
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryRowConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider", showEl: "${providerQueryDataStructure == 'dataRow'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryRowConfigId =
   */
  private String providerQueryRowConfigId;

  
  
  /**
   * Data row to link to
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryRowConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider", showEl: "${providerQueryDataStructure == 'dataRow'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryRowConfigId =
   * @return
   */
  public String getProviderQueryRowConfigId() {
    return providerQueryRowConfigId;
  }

  /**
   * Data row to link to
   * {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQueryRowConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider", showEl: "${providerQueryDataStructure == 'dataRow'}"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryRowConfigId =
   * @param providerQueryRowConfigId
   */
  public void setProviderQueryRowConfigId(String providerQueryRowConfigId) {
    this.providerQueryRowConfigId = providerQueryRowConfigId;
  }

  /**
   * Attribute which links this data to subjects 
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdAttribute$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdAttribute = 
   */
  private String providerQuerySubjectIdAttribute;

  
  
  /**
   * Attribute which links this data to subjects 
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdAttribute$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdAttribute = 
   * @return
   */
  public String getProviderQuerySubjectIdAttribute() {
    return providerQuerySubjectIdAttribute;
  }

  /**
   * Attribute which links this data to subjects 
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdAttribute$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdAttribute = 
   * @param providerQuerySubjectIdAttribute
   */
  public void setProviderQuerySubjectIdAttribute(String providerQuerySubjectIdAttribute) {
    this.providerQuerySubjectIdAttribute = providerQuerySubjectIdAttribute;
  }

  /**
   * Which type of subject id
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdType$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdType = 
   */
  private String providerQuerySubjectIdType;

  
  
  /**
   * Which type of subject id
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdType$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdType = 
   * @return
   */
  public String getProviderQuerySubjectIdType() {
    return providerQuerySubjectIdType;
  }

  /**
   * Which type of subject id
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectIdType$", formElement: "dropdown", optionValues: ["subjectId", "subjectIdentifier"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectIdType = 
   * @param providerQuerySubjectIdType
   */
  public void setProviderQuerySubjectIdType(String providerQuerySubjectIdType) {
    this.providerQuerySubjectIdType = providerQuerySubjectIdType;
  }

  /**
   * which subject source this is a subject id for
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectSourceId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectSourceId = 
   */
  private String providerQuerySubjectSourceId;
  
  
  
  /**
   * which subject source this is a subject id for
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectSourceId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectSourceId = 
   * @return
   */
  public String getProviderQuerySubjectSourceId() {
    return providerQuerySubjectSourceId;
  }

  /**
   * which subject source this is a subject id for
   * {valueType: "boolean", defaultValue: "false", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySubjectSourceId$"}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySubjectSourceId = 
   * @param providerQuerySubjectSourceId
   */
  public void setProviderQuerySubjectSourceId(String providerQuerySubjectSourceId) {
    this.providerQuerySubjectSourceId = providerQuerySubjectSourceId;
  }
  
  /**
   * number of fields in this row
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryNumberOfDataFields = 
   */
  private int providerQueryNumberOfDataFields;



  /**
   * number of fields in this row
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryNumberOfDataFields = 
   * @return
   */
  public int getProviderQueryNumberOfDataFields() {
    return providerQueryNumberOfDataFields;
  }

  /**
   * number of fields in this row
   * {valueType: "string", required: true, regex: "^dataProviderQueryConfigId\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataProviderQuery.dataProviderQueryConfigId.providerQueryNumberOfDataFields = 
   * @param providerQueryNumberOfDataFields
   */
  public void setProviderQueryNumberOfDataFields(int providerQueryNumberOfDataFields) {
    this.providerQueryNumberOfDataFields = providerQueryNumberOfDataFields;
  }
  

  private List<GrouperDataProviderQueryFieldConfig> grouperDataProviderQueryFieldConfigs = new ArrayList<>();


  
  public List<GrouperDataProviderQueryFieldConfig> getGrouperDataProviderQueryFieldConfigs() {
    return grouperDataProviderQueryFieldConfigs;
  }


  
  public void setGrouperDataProviderQueryFieldConfigs(
      List<GrouperDataProviderQueryFieldConfig> grouperDataProviderQueryFieldConfigs) {
    this.grouperDataProviderQueryFieldConfigs = grouperDataProviderQueryFieldConfigs;
  }

  


}
