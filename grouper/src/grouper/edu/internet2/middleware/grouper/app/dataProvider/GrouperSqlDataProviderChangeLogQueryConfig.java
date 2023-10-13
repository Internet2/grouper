package edu.internet2.middleware.grouper.app.dataProvider;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfig;

public class GrouperSqlDataProviderChangeLogQueryConfig extends GrouperDataProviderChangeLogQueryConfig {

  public GrouperSqlDataProviderChangeLogQueryConfig() {

  }

  /**
   * SQL config id
   */
  private String providerChangeLogQuerySqlConfigId;

  /**
   * @return SQL config id
   */
  public String getProviderChangeLogQuerySqlConfigId() {
    return providerChangeLogQuerySqlConfigId;
  }

  /**
   * SQL config id
   * @param providerChangeLogQuerySqlConfigId
   */
  public void setProviderChangeLogQuerySqlConfigId(String providerChangeLogQuerySqlConfigId) {
    this.providerChangeLogQuerySqlConfigId = providerChangeLogQuerySqlConfigId;
  }

  /**
   * SQL change log query
   */
  private String providerChangeLogQuerySqlQuery;

  
  
  /**
   * @return SQL change log query
   */
  public String getProviderChangeLogQuerySqlQuery() {
    return providerChangeLogQuerySqlQuery;
  }

  /**
   * SQL change log query
   * @param providerChangeLogQuerySqlQuery
   */
  public void setProviderChangeLogQuerySqlQuery(String providerChangeLogQuerySqlQuery) {
    this.providerChangeLogQuerySqlQuery = providerChangeLogQuerySqlQuery;
  }
  

  @Override
  public void configureSpecificSettings() {
    this.providerChangeLogQuerySqlConfigId = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + this.getConfigId() + ".providerChangeLogQuerySqlConfigId");
    this.providerChangeLogQuerySqlQuery = GrouperConfig.retrieveConfig().propertyValueString("grouperDataProviderChangeLogQuery." + this.getConfigId() + ".providerChangeLogQuerySqlQuery");
  }
}
