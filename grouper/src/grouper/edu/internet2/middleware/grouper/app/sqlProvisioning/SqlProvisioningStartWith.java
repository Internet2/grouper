package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
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
public class SqlProvisioningStartWith extends ProvisionerStartWithBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
 
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "sqlCommon";
  }
  
  @Override
  public Map<String, String> screenRedraw(Map<String, String> suffixToValue, Set<String> suffixesUserJustChanged) {
    
    Map<String, String> result = new HashMap<>();
    
    for (String suffixUserJustChanged: suffixesUserJustChanged) {
      
      if (StringUtils.equals(suffixUserJustChanged, "sqlPattern")) {
        String valueUserEnteredOnScreen = suffixToValue.get(suffixUserJustChanged);
        if (StringUtils.equals(valueUserEnteredOnScreen, "entityTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasEntityTable", "true");
          result.put("entityTableName", "from_grouper_entity");
          result.put("entityTableIdColumn", "entity_uuid");
          result.put("entityTablePrimaryKeyValue", "entityUuid");
          result.put("entityTableColumnNames", "subject_id");
          result.put("needEntityLink", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableWithAttributeTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasEntityTable", "true");
          result.put("entityTableName", "from_grouper_entity");
          result.put("entityTableIdColumn", "entity_uuid");
          result.put("entityTablePrimaryKeyValue", "entityUuid");
          result.put("entityTableColumnNames", "subject_id");
          result.put("needEntityLink", "false");
          result.put("hasEntityAttributeTable", "true");
          result.put("entityAttributeTableName", "from_grouper_entity_attr");
          result.put("columnNameForeignKeyToEntityTable", "entity_uuid");
          result.put("entityAttributeNameColumnName", "attribute_name");
          result.put("entityAttributeNameColumnValue", "attribute_value");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableWithAttributeTableAndMemberships")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("hasEntityTable", "true");
          result.put("entityTableName", "from_grouper_entity");
          result.put("entityTableIdColumn", "entity_uuid");
          result.put("entityTablePrimaryKeyValue", "entityUuid");
          result.put("entityTableColumnNames", "subject_id");
          result.put("hasEntityAttributeTable", "true");
          result.put("entityAttributeTableName", "from_grouper_entity_attr");
          result.put("columnNameForeignKeyToEntityTable", "entity_uuid");
          result.put("entityAttributeNameColumnName", "attribute_name");
          result.put("entityAttributeNameColumnValue", "attribute_value");
          result.put("entityMembershipAttributeName", "memberOf");
          result.put("needEntityLink", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableMembershipTable")) {
          result.put("hasEntityTable", "true");
          result.put("entityTableName", "from_grouper_entity");
          result.put("entityTableIdColumn", "entity_uuid");
          result.put("entityTablePrimaryKeyValue", "entityUuid");
          result.put("entityTableColumnNames", "subject_id");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableName", "from_grouper_mship");
          result.put("membershipTableGroupColumn", "group_name");
          result.put("membershipTableGroupValue", "groupName");
          result.put("membershipTableEntityColumn", "entity_uuid");
          result.put("membershipTableEntityValue", "entityUuid");
          result.put("needGroupLink", "false");
          result.put("needEntityLink", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("needGroupLink", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableWithAttributeTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("needGroupLink", "false");
          result.put("hasGroupAttributeTable", "true");
          result.put("groupAttributeTableName", "from_grouper_group_attr");
          result.put("columnNameForeignKeyToGroupTable", "group_id_index");
          result.put("groupAttributeNameColumnName", "attribute_name");
          result.put("groupAttributeNameColumnValue", "attribute_value");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableWithAttributeTableAndMemberships")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("hasGroupAttributeTable", "true");
          result.put("groupAttributeTableName", "from_grouper_group_attr");
          result.put("columnNameForeignKeyToGroupTable", "group_id_index");
          result.put("groupAttributeNameColumnName", "attribute_name");
          result.put("groupAttributeNameColumnValue", "attribute_value");
          result.put("groupMembershipAttributeName", "hasMember");
          result.put("needGroupLink", "true");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableMembershipTable")) {
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("needGroupLink", "true");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableName", "from_grouper_mship");
          result.put("membershipTableGroupColumn", "group_id_index");
          result.put("membershipTableGroupValue", "groupIdIndex");
          result.put("membershipTableEntityColumn", "subject_id");
          result.put("membershipTableEntityValue", "subjectId");
          result.put("needEntityLink", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableEntityTableMembershipTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasEntityTable", "true");
          result.put("entityTableName", "from_grouper_entity");
          result.put("entityTableIdColumn", "entity_uuid");
          result.put("entityTablePrimaryKeyValue", "entityUuid");
          result.put("entityTableColumnNames", "subject_id");
          result.put("needEntityLink", "true");
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("needGroupLink", "true");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableName", "from_grouper_mship");
          result.put("membershipTableGroupColumn", "group_id_index");
          result.put("membershipTableGroupValue", "groupIdIndex");
          result.put("membershipTableEntityColumn", "entity_uuid");
          result.put("membershipTableEntityValue", "entityUuid");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "membershipTable")) {
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableName", "from_grouper_mship");
          result.put("membershipTableGroupColumn", "group_name");
          result.put("membershipTableGroupValue", "groupName");
          result.put("membershipTableEntityColumn", "subject_id");
          result.put("membershipTableEntityValue", "subjectId");
          result.put("needGroupLink", "false");
          result.put("needEntityLink", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "other")) {
          result.clear();
        }
      }
      
    }
    
    return result;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    GrouperConfigurationModuleAttribute hasGroupTableAttribute = this.retrieveAttributes().get("hasGroupTable");
    
    if (hasGroupTableAttribute != null && GrouperUtil.booleanValue(hasGroupTableAttribute.getValue(), false)) {
      GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
      GrouperConfigurationModuleAttribute groupTableName = this.retrieveAttributes().get("groupTableName");
      GrouperConfigurationModuleAttribute groupTableIdColumn = this.retrieveAttributes().get("groupTableIdColumn");
      GrouperConfigurationModuleAttribute groupTableColumnNames = this.retrieveAttributes().get("groupTableColumnNames");
      
      validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, groupTableName, groupTableIdColumn, groupTableColumnNames);
      
      GrouperConfigurationModuleAttribute hasGroupAttributeTableAttribute = this.retrieveAttributes().get("hasGroupAttributeTable");
      if (GrouperUtil.booleanValue(hasGroupAttributeTableAttribute.getValueOrExpressionEvaluation(), false)) {
        
        GrouperConfigurationModuleAttribute attributeTableName = this.retrieveAttributes().get("groupAttributeTableName");
        GrouperConfigurationModuleAttribute attributeTableIdColumn = this.retrieveAttributes().get("columnNameForeignKeyToGroupTable");
        GrouperConfigurationModuleAttribute attributeTableColumnName = this.retrieveAttributes().get("groupAttributeNameColumnName");
        GrouperConfigurationModuleAttribute attributeTableValueColumn = this.retrieveAttributes().get("groupAttributeValueColumnName");
        
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableColumnName, attributeTableValueColumn);
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableIdColumn, null);
        
      }
    }
      
    GrouperConfigurationModuleAttribute hasEntityTableAttribute = this.retrieveAttributes().get("hasEntityTable");
    
    if (hasGroupTableAttribute != null && GrouperUtil.booleanValue(hasEntityTableAttribute.getValue(), false)) {
      
      GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
      
      GrouperConfigurationModuleAttribute entityTableName = this.retrieveAttributes().get("entityTableName");
      GrouperConfigurationModuleAttribute entityTableIdColumn = this.retrieveAttributes().get("entityTableIdColumn");
      GrouperConfigurationModuleAttribute entityTableColumnNames = this.retrieveAttributes().get("entityTableColumnNames");
      
      validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, entityTableName, entityTableIdColumn, entityTableColumnNames);
      
      GrouperConfigurationModuleAttribute hasEntityAttributeTableAttribute = this.retrieveAttributes().get("hasEntityAttributeTable");
      if (GrouperUtil.booleanValue(hasEntityAttributeTableAttribute.getValueOrExpressionEvaluation(), false)) {
        
        GrouperConfigurationModuleAttribute attributeTableName = this.retrieveAttributes().get("entityAttributeTableName");
        GrouperConfigurationModuleAttribute attributeTableIdColumn = this.retrieveAttributes().get("columnNameForeignKeyToEntityTable");
        GrouperConfigurationModuleAttribute attributeTableColumnName = this.retrieveAttributes().get("entityAttributeNameColumnName");
        GrouperConfigurationModuleAttribute attributeTableValueColumn = this.retrieveAttributes().get("entityAttributeValueColumnName");
        
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableColumnName, attributeTableValueColumn);
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableIdColumn, null);
        
      }
    }
    
  }
  
  
  private void validateTableAndColumns(Map<String, String> validationErrorsToDisplay, GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName,
      GrouperConfigurationModuleAttribute tableName, GrouperConfigurationModuleAttribute column, GrouperConfigurationModuleAttribute columnNames) {
    
    GcTableSyncTableMetadata tableMetadata = null;
    try {
      tableMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromCacheOrDatabase(sqlExternalSystemConnectionName.getValueOrExpressionEvaluation(),
          tableName.getValueOrExpressionEvaluation());
    } catch (Exception e) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupTableNotFound");
      errorMessage = errorMessage.replace("$$tableName$$", tableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(tableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
     
    if (tableMetadata == null) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupTableNotFound");
      errorMessage = errorMessage.replace("$$tableName$$", tableName.getValueOrExpressionEvaluation());
      validationErrorsToDisplay.put(tableName.getHtmlForElementIdHandle(), errorMessage);
      return;
    }
    
    if (columnNames != null && StringUtils.isNotBlank(columnNames.getValueOrExpressionEvaluation())) {
      
      String columnNamesCommaSeparated = columnNames.getValueOrExpressionEvaluation();
      Set<String> colNamesSet = GrouperUtil.splitTrimToSet(columnNamesCommaSeparated.toLowerCase(), ",");
      
      List<GcTableSyncColumnMetadata> columnsMetadata = tableMetadata.getColumnMetadata();
      
      for (GcTableSyncColumnMetadata columnMetadata: GrouperUtil.nonNull(columnsMetadata)) {
        String columnName = columnMetadata.getColumnName().toLowerCase();
        
        if (colNamesSet.contains(columnName)) {
          colNamesSet.remove(columnName);
        }
      }
      
      if (colNamesSet.size() > 0) {
        String notFoundColNames = GrouperUtil.join(colNamesSet.iterator(), ',');
        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupIdColumnNotFound");
        errorMessage = errorMessage.replace("$$column$$", notFoundColNames);
        validationErrorsToDisplay.put(columnNames.getHtmlForElementIdHandle(), errorMessage);
      }
      
    }
    
    if (column != null && StringUtils.isNotBlank(column.getValueOrExpressionEvaluation())) {
      String groupTableIdColumnValue = GrouperUtil.trim(column.getValueOrExpressionEvaluation()).toLowerCase();
      boolean groupTableIdColumnFound = false;
      
      List<GcTableSyncColumnMetadata> columnsMetadata = tableMetadata.getColumnMetadata();
      
      for (GcTableSyncColumnMetadata columnMetadata: GrouperUtil.nonNull(columnsMetadata)) {
        String columnName = columnMetadata.getColumnName().toLowerCase();
        if (StringUtils.equals(columnName, groupTableIdColumnValue)) {
          groupTableIdColumnFound = true;
          break;
        }
      }
      
      if (!groupTableIdColumnFound) {
        String errorMessage = GrouperTextContainer.textOrNull("grouperStartWithGroupTableConfigurationValidationGroupIdColumnNotFound");
        errorMessage = errorMessage.replace("$$column$$", column.getValueOrExpressionEvaluation());
        validationErrorsToDisplay.put(column.getHtmlForElementIdHandle(), errorMessage);
      }
      
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
    
  }
}
