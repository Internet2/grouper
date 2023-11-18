package edu.internet2.middleware.grouper.dataField;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * operations on grouper data tables 
 */

public class EntityDataFieldsService {
  
  /**
   * 
   * @return number of data fields configs
   */
  public static int retrieveDataFieldsNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataFieldPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of data rows configs
   */
  public static int retrieveDataRowsNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataRowPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of data provider configs
   */
  public static int retrieveDataProvidersNumberOfConfigs() {
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataProviderPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of data provider queries configs
   */
  public static int retrieveDataProviderQueriesNumberOfConfigs() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataProviderQueryPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of data provider change log query configs
   */
  public static int retrieveDataProviderChangeLogQueriesNumberOfConfigs() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataProviderChangeLogQueryPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return number of data provider queries configs
   */
  public static int retrievePrivacyRealmNumberOfConfigs() {
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.privacyRealmPattern));
    return configIdsInConfig.size();
  }
  
  /**
   * 
   * @return all grouper data fields
   */
  public static List<GrouperDataField> retrieveGrouperDataFields() {
    
    List<GrouperDataField> grouperDataFields = new GcDbAccess().connectionName("grouper")
      .sql("select * from grouper_data_field")
      .selectList(GrouperDataField.class);
    
    return grouperDataFields;
  }
  
  /**
   * 
   * @return all grouper data rows
   */
  public static List<GrouperDataRow> retrieveGrouperDataRows() {
    
    List<GrouperDataRow> grouperDataRows = new GcDbAccess().connectionName("grouper")
      .sql("select * from grouper_data_row")
      .selectList(GrouperDataRow.class);
    
    return grouperDataRows;
  }
  
  /**
   * 
   * @return all grouper data providers
   */
  public static List<GrouperDataProvider> retrieveGrouperDataProviders() {
    
    List<GrouperDataProvider> grouperDataProviders = new GcDbAccess().connectionName("grouper")
      .sql("select * from grouper_data_provider")
      .selectList(GrouperDataProvider.class);
    
    return grouperDataProviders;
  }


}
