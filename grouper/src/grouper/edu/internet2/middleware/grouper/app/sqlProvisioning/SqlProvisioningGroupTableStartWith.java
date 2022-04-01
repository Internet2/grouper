package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

/**
 */
public class SqlProvisioningGroupTableStartWith extends ProvisionerStartWithBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
 
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "sqlGroupTable";
  }
  
  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue, Set<String> suffixesUserJustChanged) {
    
    /**
     * have a  hidden uuid on the form, save the state based on the that on the server side in an expirable cache map
     *  of Map<UUID, Map<String, String>>
     */
    
    return null;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
    GrouperConfigurationModuleAttribute groupTableName = this.retrieveAttributes().get("groupTableName");
    GrouperConfigurationModuleAttribute groupTableIdColumn = this.retrieveAttributes().get("groupTableIdColumn");
    GrouperConfigurationModuleAttribute columnNames = this.retrieveAttributes().get("columnNames");
    
    GcTableSyncTableMetadata tableMetadata = null;
    try {
      tableMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromCacheOrDatabase(sqlExternalSystemConnectionName.getValueOrExpressionEvaluation(),
          groupTableName.getValueOrExpressionEvaluation());
    } catch (Exception e) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupTableNotFound");
      errorMessage = errorMessage.replace("$$groupTableName$$", groupTableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(groupTableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
     
    if (tableMetadata == null) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupTableNotFound");
      errorMessage = errorMessage.replace("$$groupTableName$$", groupTableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(groupTableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
    
    
    String columnNamesCommaSeparated = columnNames.getValueOrExpressionEvaluation();
    Set<String> colNamesSet = GrouperUtil.splitTrimToSet(columnNamesCommaSeparated.toLowerCase(), ",");
    
    String groupTableIdColumnValue = GrouperUtil.trim(groupTableIdColumn.getValueOrExpressionEvaluation()).toLowerCase();
    
    List<GcTableSyncColumnMetadata> columnsMetadata = tableMetadata.getColumnMetadata();
    
    boolean groupTableIdColumnFound = false;
    
    for (GcTableSyncColumnMetadata columnMetadata: GrouperUtil.nonNull(columnsMetadata)) {
      String columnName = columnMetadata.getColumnName().toLowerCase();
      if (StringUtils.equals(columnName, groupTableIdColumnValue)) {
        groupTableIdColumnFound = true;
      }
      if (colNamesSet.contains(columnName)) {
        colNamesSet.remove(columnName);
      }
    }
    
    if (!groupTableIdColumnFound) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupIdColumnNotFound");
      errorMessage = errorMessage.replace("$$groupTableIdColumn$$", groupTableIdColumn.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(groupTableIdColumn.getHtmlForElementIdHandle(), errorMessage);
    }
    
    if (colNamesSet.size() > 0) {
      String notFoundColNames = GrouperUtil.join(colNamesSet.iterator(), ',');
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupColumnsNotFound");
      errorMessage = errorMessage.replace("$$groupTableColumns$$", notFoundColNames);
      validationErrorsToDisplay.put(columnNames.getHtmlForElementIdHandle(), errorMessage);
    }
    
  }

  /**
   * return provisioning suffix to value
   * @param startWithSuffixToValue
   * @param provisionerSuffixToValue
   * @return
   */
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue, 
      Map<String, Object> provisionerSuffixToValue) {
    
    String columnNames = startWithSuffixToValue.get("columnNames");
    String[] colNames = GrouperUtil.splitTrim(columnNames, ",");
    provisionerSuffixToValue.put("numberOfGroupAttributes", colNames.length);
    
    for (int i=0; i<colNames.length; i++) {
      provisionerSuffixToValue.put("targetGroupAttribute."+i+".name", colNames[i]);
      if (StringUtils.equalsIgnoreCase(colNames[i], "group_name")) {
        //TODO set the translation value
      }
    }
    
    provisionerSuffixToValue.put("operateOnGrouperGroups", true);
    provisionerSuffixToValue.put("provisioningType", "groupAttributes");
    
  }
}
