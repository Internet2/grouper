package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.HashSet;
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
        
        if (StringUtils.isNotBlank(valueUserEnteredOnScreen)) {
          result.put("userAttributesType", "core");
        }
        
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
          result.put("groupTablePrimaryKeyValue", "groupIdIndex");
          result.put("groupTableColumnNames", "groupIdIndex");
          result.put("needGroupLink", "false");
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableWithAttributeTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          result.put("groupTableName", "from_grouper_group");
          result.put("groupTableIdColumn", "group_id_index");
          result.put("groupTablePrimaryKeyValue", "groupIdIndex");
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
          result.put("groupTablePrimaryKeyValue", "groupIdIndex");
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
          result.put("groupTablePrimaryKeyValue", "groupIdIndex");
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
          result.put("groupTablePrimaryKeyValue", "groupIdIndex");
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
    
    GrouperConfigurationModuleAttribute membershipStructureAttribute = this.retrieveAttributes().get("membershipStructure");
    GrouperConfigurationModuleAttribute hasMembershipTableAttribute = this.retrieveAttributes().get("hasMembershipTable");
    
    if (membershipStructureAttribute != null && StringUtils.equals(membershipStructureAttribute.getValue(), "notApplicable") && 
        hasMembershipTableAttribute != null && GrouperUtil.booleanValue(hasMembershipTableAttribute.getValue(), false)) {
      GrouperTextContainer.textOrNull("grouperStartWithInvalidMembershipStructureHasMembershipTable");
    }
    
    GrouperConfigurationModuleAttribute subjectSourceEntityResoverModuleAttribute = this.retrieveAttributes().get("subjectSourceEntityResolverAttributes");
    if (subjectSourceEntityResoverModuleAttribute != null && StringUtils.isNotBlank(subjectSourceEntityResoverModuleAttribute.getValue())) {
      String commaSeparatedResolverAttributes = subjectSourceEntityResoverModuleAttribute.getValue();
      List<String> list = GrouperUtil.splitTrimToList(commaSeparatedResolverAttributes, ",");
      if (list.size() > 3) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesMoreThanThreeAttributes");
        validationErrorsToDisplay.put(subjectSourceEntityResoverModuleAttribute.getHtmlForElementIdHandle(), errorMessage);
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
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "entityResolver") || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasTargetEntityLink"), false)) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
      
      //TODO debug why this is not getting populated
      provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", startWithSuffixToValue.get("entityTableIdColumn"));
      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSource") || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      String attributesCommaSeparated = startWithSuffixToValue.get("subjectSourceEntityResolverAttributes");
      if (StringUtils.isNotBlank(attributesCommaSeparated)) {
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        String[] attributes = GrouperUtil.splitTrim(attributesCommaSeparated, ",");
        // by this time the validation is already done that there are no more than 3 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+1;
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"type", "subjectTranslationScript");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"translationScript", "${subject.getAttributeValue("+attributes[i]+")}");
        }
        
      }
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      provisionerSuffixToValue.put("userTableName", startWithSuffixToValue.get("entityTableName"));
      
      Set<String> otherAttributeNamesSet = new HashSet<>();
      
      String entityMembershipAttributeName = null;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityAttributeTable"), false)) {
        provisionerSuffixToValue.put("useSeparateTableForEntityAttributes", "true");

        String otherAttributeNames = startWithSuffixToValue.get("entityOtherAttributeNames");
        otherAttributeNamesSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(otherAttributeNames, ","));
        
        entityMembershipAttributeName = startWithSuffixToValue.get("entityMembershipAttributeName");
        
      }
      
      String commaSeparatedColNames = startWithSuffixToValue.get("entityTableColumnNames");
      Set<String> entityTableCols = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(commaSeparatedColNames, ","));
      
      int numberOfEntityAttributes = entityTableCols.size() + otherAttributeNamesSet.size() + 1; // +1 for entityTableIdColumn
      
      if (StringUtils.isNotBlank(entityMembershipAttributeName)) {
        if (entityTableCols.contains(entityMembershipAttributeName) || otherAttributeNamesSet.contains(entityMembershipAttributeName) || StringUtils.equalsIgnoreCase(startWithSuffixToValue.get("entityTableIdColumn"), entityMembershipAttributeName)) {
          provisionerSuffixToValue.put("entityMembershipAttributeName", entityMembershipAttributeName);
        } else {
          numberOfEntityAttributes++;
        }
      }
      
      provisionerSuffixToValue.put("numberOfEntityAttributes", numberOfEntityAttributes);
      
      provisionerSuffixToValue.put("targetEntityAttribute.0.name", startWithSuffixToValue.get("entityTableIdColumn"));
      provisionerSuffixToValue.put("targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
      provisionerSuffixToValue.put("targetEntityAttribute.0.storageType", "entityTableColumn");
      
      String startWithEntityTablePrimaryKeyValue = startWithSuffixToValue.get("entityTablePrimaryKeyValue");
      String grouperProvisioningEntityFieldValue = null;

      if (StringUtils.equals("email", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "email";
      } else if (StringUtils.equals("entityUuid", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "id";
      } else if (StringUtils.equals("entityDescription", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "description";
      } else if (StringUtils.equals("entityName", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "name";
      } else if (StringUtils.equals("subjectId", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "subjectId";
      } else if (StringUtils.equals("subjectIdentifier0", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "subjectIdentifier0";
      } else if (StringUtils.equals("subjectIdentifier1", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "subjectIdentifier1";
      } else if (StringUtils.equals("subjectIdentifier2", startWithEntityTablePrimaryKeyValue)) {
        grouperProvisioningEntityFieldValue = "subjectIdentifier2";
      } else if (StringUtils.equals("other", startWithEntityTablePrimaryKeyValue) || StringUtils.equals("script", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateExpressionType", "translationScript");
      }
      
      if (grouperProvisioningEntityFieldValue != null) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", grouperProvisioningEntityFieldValue);
      }
      
      int i = 1;
      for (String entityTableCol: entityTableCols) {
        provisionerSuffixToValue.put("targetEntityAttribute."+i+".name", entityTableCol);
        
        if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_uuid") || StringUtils.equalsIgnoreCase(entityTableCol, "uuid") || StringUtils.equalsIgnoreCase(entityTableCol, "id")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "id");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_name") || StringUtils.equalsIgnoreCase(entityTableCol, "name")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "name");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "email")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "email");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_description")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "description");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_id")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "subjectId");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier0")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "subjectIdentifier0");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier1")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "subjectIdentifier1");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier2")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".translateFromGrouperProvisioningEntityField", "subjectIdentifier2");
        } 
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+i+".storageType", "entityTableColumn"); // storageType is visible only when hasEntityAttributeTable is true
        }
        
        i++;
      }
      
      for (String otherAttributeCol: otherAttributeNamesSet) {
        provisionerSuffixToValue.put("targetEntityAttribute."+i+".name", otherAttributeCol);
        provisionerSuffixToValue.put("targetEntityAttribute."+i+".storageType", "separateAttributesTable");
        i++;
      }
      
      if (i <= numberOfEntityAttributes - 1) {
        provisionerSuffixToValue.put("targetEntityAttribute."+i+".name", entityMembershipAttributeName);
        provisionerSuffixToValue.put("targetEntityAttribute."+i+".storageType", "separateAttributesTable");
        i++;
      }
      
      //TODO debug why this is not getting populated
      provisionerSuffixToValue.put("userPrimaryKey", startWithSuffixToValue.get("entityTableIdColumn"));
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      provisionerSuffixToValue.put("groupTableName", startWithSuffixToValue.get("groupTableName"));
      
      Set<String> otherAttributeNamesSet = new HashSet<>();
      String groupMembershipAttributeName = null;
      
      //TODO Later
      String groupMembershipAttributeValue = null;
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
        provisionerSuffixToValue.put("useSeparateTableForGroupAttributes", "true");
        groupMembershipAttributeName = startWithSuffixToValue.get("groupMembershipAttributeName");
        groupMembershipAttributeValue = startWithSuffixToValue.get("groupMembershipAttributeValue");
        
        String otherAttributeNames = startWithSuffixToValue.get("groupOtherAttributeNames");
        otherAttributeNamesSet = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(otherAttributeNames, ","));
        
      }
      
      String commaSeparatedColNames = startWithSuffixToValue.get("groupTableColumnNames");
      Set<String> groupTableCols = GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(commaSeparatedColNames, ","));
      
      int numberOfGroupAttributes = groupTableCols.size() + otherAttributeNamesSet.size() + 1; // +1 for groupTableIdColumn
      
      if (StringUtils.isNotBlank(groupMembershipAttributeName)) {
        if (groupTableCols.contains(groupMembershipAttributeName) || otherAttributeNamesSet.contains(groupMembershipAttributeName) || StringUtils.equalsIgnoreCase(startWithSuffixToValue.get("groupTableIdColumn"), groupMembershipAttributeName)) {
          provisionerSuffixToValue.put("groupMembershipAttributeName", groupMembershipAttributeName);
        } else {
          numberOfGroupAttributes++;
        }
      }
      
      provisionerSuffixToValue.put("numberOfGroupAttributes", numberOfGroupAttributes);
      
      provisionerSuffixToValue.put("targetGroupAttribute.0.name", startWithSuffixToValue.get("groupTableIdColumn"));
      provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
      
      String startWithGroupTablePrimaryKeyValue = startWithSuffixToValue.get("groupTablePrimaryKeyValue");
      String grouperProvisioningGroupFieldValue = null;
     
      if (StringUtils.equals("groupExtension", startWithGroupTablePrimaryKeyValue)) {
        grouperProvisioningGroupFieldValue = "extension";
      } else if (StringUtils.equals("groupIdIndex", startWithGroupTablePrimaryKeyValue)) {
        grouperProvisioningGroupFieldValue = "idIndex";
      } else if (StringUtils.equals("groupName", startWithGroupTablePrimaryKeyValue)) {
        grouperProvisioningGroupFieldValue = "name";
      } else if (StringUtils.equals("groupUuid", startWithGroupTablePrimaryKeyValue)) {
        grouperProvisioningGroupFieldValue = "id";
      } else if (StringUtils.equals("other", startWithGroupTablePrimaryKeyValue) || StringUtils.equals("script", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "translationScript");
      }
      
      if (grouperProvisioningGroupFieldValue != null) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", grouperProvisioningGroupFieldValue);
      }
      
      provisionerSuffixToValue.put("groupTableIdColumn", startWithSuffixToValue.get("groupTableIdColumn"));
      
      int i = 1;
      for (String groupTableCol: groupTableCols) {
        provisionerSuffixToValue.put("targetGroupAttribute."+i+".name", groupTableCol);
        
        if (StringUtils.equalsIgnoreCase(groupTableCol, "group_uuid") || StringUtils.equalsIgnoreCase(groupTableCol, "uuid") || StringUtils.equalsIgnoreCase(groupTableCol, "id")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "id");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_name") || StringUtils.equalsIgnoreCase(groupTableCol, "name")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "name");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_id_index") || StringUtils.equalsIgnoreCase(groupTableCol, "id_index")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "idIndex");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_extension") || StringUtils.equalsIgnoreCase(groupTableCol, "extension")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "extension");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_display_name") || StringUtils.equalsIgnoreCase(groupTableCol, "display_name")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "displayName");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_display_extension") || StringUtils.equalsIgnoreCase(groupTableCol, "display_extension")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "displayExtension");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_description") || StringUtils.equalsIgnoreCase(groupTableCol, "description")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".translateFromGrouperProvisioningGroupField", "description");
        }
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetGroupAttribute."+i+".storageType", "groupTableColumn"); // storageType is visible only when hasGroupAttributeTable is true
        }
        
        i++;
      }
      
      for (String otherAttributeCol: otherAttributeNamesSet) {
        provisionerSuffixToValue.put("targetGroupAttribute."+i+".name", otherAttributeCol);
        provisionerSuffixToValue.put("targetGroupAttribute."+i+".storageType", "separateAttributesTable");
        i++;
      }
      
      if ( i <= numberOfGroupAttributes - 1) {
        provisionerSuffixToValue.put("targetGroupAttribute."+i+".name", groupMembershipAttributeName);
        provisionerSuffixToValue.put("targetGroupAttribute."+i+".storageType", "separateAttributesTable");
        i++;
      }
      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "membershipObjects") ||
        StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes") || 
        StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
      
      provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
      
      provisionerSuffixToValue.put("provisioningType", startWithSuffixToValue.get("membershipStructure"));
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "membershipObjects")) {
        
        provisionerSuffixToValue.put("membershipTableName", startWithSuffixToValue.get("membershipTableName"));
        
        provisionerSuffixToValue.put("numberOfMembershipAttributes", 2);
        
        String membershipTableGroupColumn = startWithSuffixToValue.get("membershipTableGroupColumn");
        
        provisionerSuffixToValue.put("targetMembershipAttribute.0.name", membershipTableGroupColumn);

        String membershipTableGroupValue = startWithSuffixToValue.get("membershipTableGroupValue");
        
        if (StringUtils.equalsAny(membershipTableGroupValue, "groupName", "groupExtension", "groupIdIndex", "groupUUID")) {
          
          provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
          if (StringUtils.equals("groupName", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "name");
          }
          if (StringUtils.equals("groupExtension", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "extension");
          }
          if (StringUtils.equals("groupIdIndex", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "idIndex");
          }
          if (StringUtils.equals("groupUUID", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "id");
          }
          
        }
        
        String membershipTableEntityColumn = startWithSuffixToValue.get("membershipTableEntityColumn");
                
        provisionerSuffixToValue.put("targetMembershipAttribute.1.name", membershipTableEntityColumn);
        
        String membershipTableEntityValue = startWithSuffixToValue.get("membershipTableEntityValue");

        if (StringUtils.equalsAny(membershipTableEntityValue, "email", "entityUuid", "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
          
          provisionerSuffixToValue.put("targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
          if (StringUtils.equals("email", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "email");
          }
          if (StringUtils.equals("entityUuid", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "id");
          }
          if (StringUtils.equals("subjectId", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "subjectId");
          }
          if (StringUtils.equals("subjectIdentifier0", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "subjectIdentifier0");
          }
          if (StringUtils.equals("subjectIdentifier1", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "subjectIdentifier1");
          }
          if (StringUtils.equals("subjectIdentifier2", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "subjectIdentifier2");
          }
          
        }
                
        provisionerSuffixToValue.put("membershipGroupForeignKeyColumn", membershipTableGroupColumn);
        provisionerSuffixToValue.put("membershipEntityForeignKeyColumn", membershipTableEntityColumn);
    }

    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }
    
    
  }
}
