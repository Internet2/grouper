package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;

public class GrouperSqlDataProviderQueryConfig extends GrouperDataProviderQueryConfig {

  public GrouperSqlDataProviderQueryConfig() {

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
  

  @Override
  public void configureSpecificSettings() {
    //  # SQL config id
    //  # {valueType: "string", required: true, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.app.loader.db.DatabaseGrouperExternalSystem", regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlConfigId$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlConfigId = 
    this.providerQuerySqlConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQuerySqlConfigId");
   
    //  # SQL query
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerQuerySqlQuery$"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerQuerySqlQuery =
    this.providerQuerySqlQuery = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderQuery." + this.getConfigId() + ".providerQuerySqlQuery");

  }
}
