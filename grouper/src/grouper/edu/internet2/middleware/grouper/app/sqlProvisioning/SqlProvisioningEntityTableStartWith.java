package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

/**
 * 
 */
public class SqlProvisioningEntityTableStartWith extends ProvisionerStartWithBase {
  
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "sqlEntityTable";
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);

    GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
    GrouperConfigurationModuleAttribute userTableName = this.retrieveAttributes().get("userTableName");
    GrouperConfigurationModuleAttribute userTableIdColumn = this.retrieveAttributes().get("userPrimaryKey");
    GrouperConfigurationModuleAttribute columnNames = this.retrieveAttributes().get("columnNames");
    
    GcTableSyncTableMetadata tableMetadata = null;
    try {
      tableMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromCacheOrDatabase(sqlExternalSystemConnectionName.getValueOrExpressionEvaluation(),
          userTableName.getValueOrExpressionEvaluation());
    } catch (Exception e) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithEntityTableConfigurationValidationUserTableNotFound");
      errorMessage = errorMessage.replace("$$userTableName$$", userTableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(userTableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
     
    if (tableMetadata == null) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithEntityTableConfigurationValidationUserTableNotFound");
      errorMessage = errorMessage.replace("$$userTableName$$", userTableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(userTableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
    
    
    String columnNamesCommaSeparated = columnNames.getValueOrExpressionEvaluation();
    Set<String> colNamesSet = GrouperUtil.splitTrimToSet(columnNamesCommaSeparated.toLowerCase(), ",");
    
    String userTableIdColumnValue = GrouperUtil.trim(userTableIdColumn.getValueOrExpressionEvaluation()).toLowerCase();
    
    List<GcTableSyncColumnMetadata> columnsMetadata = tableMetadata.getColumnMetadata();
    
    boolean userTableIdColumnFound = false;
    
    for (GcTableSyncColumnMetadata columnMetadata: GrouperUtil.nonNull(columnsMetadata)) {
      String columnName = columnMetadata.getColumnName().toLowerCase();
      if (StringUtils.equals(columnName, userTableIdColumnValue)) {
        userTableIdColumnFound = true;
      }
      if (colNamesSet.contains(columnName)) {
        colNamesSet.remove(columnName);
      }
    }
    
    if (!userTableIdColumnFound) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithEntityTableConfigurationValidationUserIdColumnNotFound");
      errorMessage = errorMessage.replace("$$userTableIdColumn$$", userTableIdColumn.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(userTableIdColumn.getHtmlForElementIdHandle(), errorMessage);
    }
    
    if (colNamesSet.size() > 0) {
      String notFoundColNames = GrouperUtil.join(colNamesSet.iterator(), ',');
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithEntityTableConfigurationValidationEntityColumnsNotFound");
      errorMessage = errorMessage.replace("$$userTableColumns$$", notFoundColNames);
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

  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue,
      Set<String> suffixesUserJustChanged) {
    // TODO Auto-generated method stub
    return null;
  }

}
