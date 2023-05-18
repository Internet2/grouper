package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

/**
 */
public class SqlProvisioningStartWith extends ProvisionerStartWithBase {
  
  public final static Set<String> allKeys = new HashSet<>();
  static {
    allKeys.add("membershipStructure");
    allKeys.add("hasEntityTable");
    allKeys.add("hasEntityAttributeTable");
    allKeys.add("hasMembershipTable");
    allKeys.add("hasGroupTable");
    allKeys.add("hasGroupAttributeTable");
    allKeys.add("membershipTableEntityValue");
    allKeys.add("membershipTableEntityValue");
  }
  
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
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasEntityTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableWithAttributeTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasEntityTable", "true");
          result.put("hasEntityAttributeTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasEntityTable", "hasEntityAttributeTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableWithAttributeTableAndMemberships")) {
          result.put("membershipStructure", "entityAttributes");
          result.put("hasEntityTable", "true");
          result.put("hasEntityAttributeTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasEntityTable", "hasEntityAttributeTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "entityTableMembershipTable")) {
          result.put("hasEntityTable", "true");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableEntityValue", "entityPrimaryKey");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasEntityTable", "hasMembershipTable", "membershipTableEntityValue"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasGroupTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableWithAttributeTable")) {
          result.put("membershipStructure", "notApplicable");
          result.put("hasGroupTable", "true");
          result.put("hasGroupAttributeTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasGroupTable", "hasGroupAttributeTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableWithAttributeTableAndMemberships")) {
          result.put("membershipStructure", "groupAttributes");
          result.put("hasGroupTable", "true");
          result.put("hasGroupAttributeTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasGroupTable", "hasGroupAttributeTable"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableMembershipTable")) {
          result.put("hasGroupTable", "true");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          result.put("membershipTableGroupValue", "groupPrimaryKey");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasGroupTable", "hasMembershipTable", "membershipTableGroupValue"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "groupTableEntityTableMembershipTable")) {
          result.put("hasEntityTable", "true");
          result.put("hasGroupTable", "true");
          result.put("membershipTableGroupValue", "groupPrimaryKey");
          result.put("membershipTableEntityValue", "entityPrimaryKey");
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasEntityTable", "hasGroupTable", "hasMembershipTable", "membershipTableEntityValue", "membershipTableGroupValue"), allKeys);
          
        } else if (StringUtils.equals(valueUserEnteredOnScreen, "membershipTable")) {
          result.put("membershipStructure", "membershipObjects");
          result.put("hasMembershipTable", "true");
          
          setValuesToNull(result, GrouperUtil.toSet("membershipStructure", "hasMembershipTable"), allKeys);
          
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
    
    //if has entity table is false, you cannot pick entityPrimaryKey as group membership attribute value
    GrouperConfigurationModuleAttribute hasEntityTableAttribute = this.retrieveAttributes().get("hasEntityTable");
    if (!GrouperUtil.booleanValue(hasEntityTableAttribute.getValueOrExpressionEvaluation(), false)) {
      GrouperConfigurationModuleAttribute groupMembershipAttributeValueAttribute = this.retrieveAttributes().get("groupMembershipAttributeValue");
      if (groupMembershipAttributeValueAttribute != null) {
        String value = groupMembershipAttributeValueAttribute.getValueOrExpressionEvaluation();
        if (StringUtils.equals(value, "entityPrimaryKey")) {
          String errorMessage = GrouperTextContainer.textOrNull("groupMembershipAttributeValueCannotBeEntityPrimaryKey");
          validationErrorsToDisplay.put(groupMembershipAttributeValueAttribute.getHtmlForElementIdHandle(), errorMessage);
        }
      }
    }
    
    if (hasGroupTableAttribute != null && GrouperUtil.booleanValue(hasGroupTableAttribute.getValue(), false)) {
      GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
      GrouperConfigurationModuleAttribute groupTableName = this.retrieveAttributes().get("groupTableName");
      GrouperConfigurationModuleAttribute groupTableIdColumn = this.retrieveAttributes().get("groupTableIdColumn");
      GrouperConfigurationModuleAttribute groupTableColumnNames = this.retrieveAttributes().get("groupTableColumnNames");
      
      validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, groupTableName, groupTableIdColumn, groupTableColumnNames);
      
      GrouperConfigurationModuleAttribute hasGroupAttributeTableAttribute = this.retrieveAttributes().get("hasGroupAttributeTable");
      if (GrouperUtil.booleanValue(hasGroupAttributeTableAttribute.getValueOrExpressionEvaluation(), false)) {
        
        GrouperConfigurationModuleAttribute attributeTableName = this.retrieveAttributes().get("groupAttributesTableName");
        GrouperConfigurationModuleAttribute attributeTableIdColumn = this.retrieveAttributes().get("groupAttributesGroupForeignKeyColumn");
        GrouperConfigurationModuleAttribute attributeTableColumnName = this.retrieveAttributes().get("groupAttributeNameColumnName");
        GrouperConfigurationModuleAttribute attributeTableValueColumn = this.retrieveAttributes().get("groupAttributeValueColumnName");
        
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableColumnName, attributeTableValueColumn);
        validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, attributeTableName, attributeTableIdColumn, null);
        
      }
    }
      
    
    if (hasEntityTableAttribute != null && GrouperUtil.booleanValue(hasEntityTableAttribute.getValue(), false)) {
      
      GrouperConfigurationModuleAttribute sqlExternalSystemConnectionName = this.retrieveAttributes().get("dbExternalSystemConfigId");
      
      GrouperConfigurationModuleAttribute entityTableName = this.retrieveAttributes().get("entityTableName");
      GrouperConfigurationModuleAttribute entityTableIdColumn = this.retrieveAttributes().get("entityTableIdColumn");
      GrouperConfigurationModuleAttribute entityTableColumnNames = this.retrieveAttributes().get("entityTableColumnNames");
      
      validateTableAndColumns(validationErrorsToDisplay, sqlExternalSystemConnectionName, entityTableName, entityTableIdColumn, entityTableColumnNames);
      
      GrouperConfigurationModuleAttribute hasEntityAttributeTableAttribute = this.retrieveAttributes().get("hasEntityAttributeTable");
      if (GrouperUtil.booleanValue(hasEntityAttributeTableAttribute.getValueOrExpressionEvaluation(), false)) {
        
        GrouperConfigurationModuleAttribute attributeTableName = this.retrieveAttributes().get("entityAttributesTableName");
        GrouperConfigurationModuleAttribute attributeTableIdColumn = this.retrieveAttributes().get("entityAttributesEntityForeignKeyColumn");
        GrouperConfigurationModuleAttribute attributeTableColumnName = this.retrieveAttributes().get("entityAttributesAttributeNameColumn");
        GrouperConfigurationModuleAttribute attributeTableValueColumn = this.retrieveAttributes().get("entityAttributesAttributeValueColumn");
        
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
      if (list.size() > 2) {
        String errorMessage = GrouperTextContainer.textOrNull("subjectSourceEntityResolverAttributesTooManyAttributes");
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
    
    provisionerSuffixToValue.put("class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner");
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "entityResolver") || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("entityResolver.entityAttributesNotInSubjectSource", "true");
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSource") 
        || StringUtils.equals(startWithSuffixToValue.get("userAttributesType"), "subjectSourceAndEntityResolver")) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      String attributesCommaSeparated = startWithSuffixToValue.get("subjectSourceEntityResolverAttributes");
      if (StringUtils.isNotBlank(attributesCommaSeparated)) {
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        String[] attributes = GrouperUtil.splitTrim(attributesCommaSeparated, ",");
        // by this time the validation is already done that there are no more than 2 attributes
        for (int i=0; i<attributes.length; i++) {
          int j = i+2;
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"type", "subjectTranslationScript");
          provisionerSuffixToValue.put("entityAttributeValueCache"+j+"translationScript", "${subject.getAttributeValue('"+attributes[i]+"')}");
        }
        
      }
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      
      provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("entityAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("entityAttributeValueCache0type", "entityAttribute");
      
      provisionerSuffixToValue.put("entityAttributeValueCache0entityAttribute", startWithSuffixToValue.get("entityTableIdColumn"));
      
      provisionerSuffixToValue.put("entityMatchingAttributeCount", "1");
      provisionerSuffixToValue.put("entityMatchingAttribute0name", startWithSuffixToValue.get("entityTableIdColumn"));
      
    }
    
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
      provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
      
      provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0has", "true");
      provisionerSuffixToValue.put("groupAttributeValueCache0source", "target");
      provisionerSuffixToValue.put("groupAttributeValueCache0type", "groupAttribute");
      
      provisionerSuffixToValue.put("groupAttributeValueCache0groupAttribute", startWithSuffixToValue.get("groupTableIdColumn"));
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "1");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", startWithSuffixToValue.get("groupTableIdColumn"));
      
    }
    
    int entityAttributeConfigIndex = 0;
    
    //TODO move it in the above if condition
    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
      
      provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
      provisionerSuffixToValue.put("userTableName", startWithSuffixToValue.get("entityTableName"));
      
      provisionerSuffixToValue.put("selectAllEntities", "true");
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("manageEntities"))) {
        provisionerSuffixToValue.put("makeChangesToEntities", "true");
      }
      
      
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
      
      if (StringUtils.equals("email", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "email");
      } else if (StringUtils.equals("uuid", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "id");
      } else if (StringUtils.equals("subjectId", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
      } else if (StringUtils.equals("subjectIdentifier0", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectIdentifier0");
      } else if (StringUtils.equals("subjectIdentifier1", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectIdentifier1");
      } else if (StringUtils.equals("subjectIdentifier2", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectIdentifier2");
      } else if (StringUtils.equals("script", startWithEntityTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetEntityAttribute.0.translateExpression", startWithSuffixToValue.get("entityTablePrimaryKeyValueTranslationScript"));
      }
      
      
      entityAttributeConfigIndex = 1;
      for (String entityTableCol: entityTableCols) {
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".name", entityTableCol);
        
        if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_uuid") || StringUtils.equalsIgnoreCase(entityTableCol, "uuid") || StringUtils.equalsIgnoreCase(entityTableCol, "id")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "id");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_name") || StringUtils.equalsIgnoreCase(entityTableCol, "name")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "name");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "email")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "email");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "entity_description") || StringUtils.equalsIgnoreCase(entityTableCol, "description") ) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "description");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_id")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "subjectId");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier0")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "subjectIdentifier0");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier1")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "subjectIdentifier1");
        } else if (StringUtils.equalsIgnoreCase(entityTableCol, "subject_identifier2")) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", "subjectIdentifier2");
        } 
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".storageType", "entityTableColumn"); // storageType is visible only when hasEntityAttributeTable is true
        }
        
        entityAttributeConfigIndex++;
      }
      
      for (String otherAttributeCol: otherAttributeNamesSet) {
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".name", otherAttributeCol);
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".storageType", "separateAttributesTable");
        entityAttributeConfigIndex++;
      }
      
      if (entityAttributeConfigIndex <= numberOfEntityAttributes - 1) {
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".name", entityMembershipAttributeName);
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".storageType", "separateAttributesTable");
        entityAttributeConfigIndex++;
      }
      
      //TODO debug why this is not getting populated
      provisionerSuffixToValue.put("userPrimaryKey", startWithSuffixToValue.get("entityTableIdColumn"));
      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes")) {
      
      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
        provisionerSuffixToValue.put("customizeEntityCrud", "true");
        provisionerSuffixToValue.put("selectEntities", "false");
      }
      
      String groupMembershipAttributeValue = startWithSuffixToValue.get("groupMembershipAttributeValue");
      
      if (StringUtils.equals(groupMembershipAttributeValue, "entityPrimaryKey")) {
        groupMembershipAttributeValue = startWithSuffixToValue.get("entityTableIdColumn");
      }
      
      if (!StringUtils.equals((String)provisionerSuffixToValue.get("entityAttributeValueCache0entityAttribute"), groupMembershipAttributeValue)) {
        
        provisionerSuffixToValue.put("operateOnGrouperEntities", "true");
        
        provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
        provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
        provisionerSuffixToValue.put("entityAttributeValueCache1source", "grouper");
        provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
        
        if (!StringUtils.equals("script", groupMembershipAttributeValue) && !StringUtils.equals("other", groupMembershipAttributeValue)) {
          provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", groupMembershipAttributeValue);
        }
   
        provisionerSuffixToValue.put("groupMembershipAttributeValue", "entityAttributeValueCache1");
        
        String entityAttributeNameForMemberships = startWithSuffixToValue.get("entityAttributeNameForMemberships");
        
        provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", entityAttributeNameForMemberships);
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".showAdvancedAttribute", "true");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".showAttributeCrud", "true");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".insert", "false");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".update", "false");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".select", "false");
        }
        
        if (StringUtils.equals("script", groupMembershipAttributeValue)) {
          String groupMembershipAttributeValueTranslationScript = startWithSuffixToValue.get("groupMembershipAttributeValueTranslationScript");
          
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpression", groupMembershipAttributeValueTranslationScript);
        } else if (!StringUtils.equals("other", groupMembershipAttributeValue)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "grouperProvisioningEntityField");
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateFromGrouperProvisioningEntityField", groupMembershipAttributeValue);
        }
        
        provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".name", entityAttributeNameForMemberships);
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".storageType", "separateAttributesTable");
        }
        
        entityAttributeConfigIndex++;
        
      } else {
        provisionerSuffixToValue.put("groupMembershipAttributeValue", "entityAttributeValueCache0");
      }
      
      
    }
    
    int groupAttributeConfigIndex = 0;
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
      if (StringUtils.equals("extension", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "extension");
      } else if (StringUtils.equals("idIndex", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "idIndex");
      } else if (StringUtils.equals("name", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
      } else if (StringUtils.equals("uuid", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "id");
      } else if (StringUtils.equals("script", startWithGroupTablePrimaryKeyValue)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpressionType", "translationScript");
        provisionerSuffixToValue.put("targetGroupAttribute.0.translateExpression", startWithSuffixToValue.get("groupTablePrimaryKeyValueTranslationScript"));
      }
      
      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
        provisionerSuffixToValue.put("targetGroupAttribute.0.storageType", "groupTableColumn");
      }
      
      provisionerSuffixToValue.put("groupTableIdColumn", startWithSuffixToValue.get("groupTableIdColumn"));
      
      groupAttributeConfigIndex = 1;
      for (String groupTableCol: groupTableCols) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", groupTableCol);
        
        if (StringUtils.equalsIgnoreCase(groupTableCol, "group_uuid") || StringUtils.equalsIgnoreCase(groupTableCol, "uuid") || StringUtils.equalsIgnoreCase(groupTableCol, "id")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "id");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_name") || StringUtils.equalsIgnoreCase(groupTableCol, "name")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "name");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_id_index") || StringUtils.equalsIgnoreCase(groupTableCol, "id_index")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "idIndex");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_extension") || StringUtils.equalsIgnoreCase(groupTableCol, "extension")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "extension");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_display_name") || StringUtils.equalsIgnoreCase(groupTableCol, "display_name")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "displayName");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_display_extension") || StringUtils.equalsIgnoreCase(groupTableCol, "display_extension")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "displayExtension");
        } else if (StringUtils.equalsIgnoreCase(groupTableCol, "group_description") || StringUtils.equalsIgnoreCase(groupTableCol, "description")) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", "description");
        }
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".storageType", "groupTableColumn"); // storageType is visible only when hasGroupAttributeTable is true
        }
        
        groupAttributeConfigIndex++;
      }
      
      for (String otherAttributeCol: otherAttributeNamesSet) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", otherAttributeCol);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".storageType", "separateAttributesTable");
        groupAttributeConfigIndex++;
      }
      
      if ( groupAttributeConfigIndex <= numberOfGroupAttributes - 1) {
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", groupMembershipAttributeName);
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".storageType", "separateAttributesTable");
        groupAttributeConfigIndex++;
      }
      
      provisionerSuffixToValue.put("groupMatchingAttributeCount", "1");
      provisionerSuffixToValue.put("groupMatchingAttribute0name", startWithSuffixToValue.get("groupTableIdColumn"));
      
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "membershipObjects") ||
        StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "groupAttributes") || 
        StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
      
      provisionerSuffixToValue.put("operateOnGrouperMemberships", "true");
      
      provisionerSuffixToValue.put("provisioningType", startWithSuffixToValue.get("membershipStructure"));
    }
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "membershipObjects")) {
      
        if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
          provisionerSuffixToValue.put("customizeGroupCrud", "true");
          provisionerSuffixToValue.put("selectGroups", "false");
        }
        if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
          provisionerSuffixToValue.put("customizeEntityCrud", "true");
          provisionerSuffixToValue.put("selectEntities", "false");
        }
        
        provisionerSuffixToValue.put("membershipTableName", startWithSuffixToValue.get("membershipTableName"));
        
        provisionerSuffixToValue.put("numberOfMembershipAttributes", 2);
        
        String membershipTableGroupColumn = startWithSuffixToValue.get("membershipTableGroupColumn");
        
        provisionerSuffixToValue.put("targetMembershipAttribute.0.name", membershipTableGroupColumn);

        String membershipTableGroupValue = startWithSuffixToValue.get("membershipTableGroupValue");
        // "extension", "groupPrimaryKey", "idIndex", "name", "other", "script", "uuid"
        if (StringUtils.equalsAny(membershipTableGroupValue, "name", "extension", "idIndex", "uuid", "groupPrimaryKey")) {
          
          provisionerSuffixToValue.put("targetMembershipAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
          if (StringUtils.equals("name", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "name");
          }
          if (StringUtils.equals("extension", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "extension");
          }
          if (StringUtils.equals("idIndex", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "idIndex");
          }
          if (StringUtils.equals("uuid", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField",  "id");
          }
          if (StringUtils.equals("groupPrimaryKey", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache0") ;
          }
          
        } else if (StringUtils.equalsAny(membershipTableGroupValue, "script", "other")) {
          String membershipGroupMembershipAttributeName = startWithSuffixToValue.get("membershipGroupMembershipAttributeName");
          
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", membershipGroupMembershipAttributeName);
          
          if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAdvancedAttribute", "true");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAttributeCrud", "true");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".insert", "false");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".update", "false");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".select", "false");
          }
          
          if (StringUtils.equals("script", membershipTableGroupValue)) {
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "translationScript");
            String membershipTableGroupValueTranslationScript = startWithSuffixToValue.get("membershipTableGroupValueTranslationScript");
            provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpression", membershipTableGroupValueTranslationScript);
          }
          
          provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
          provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
          provisionerSuffixToValue.put("groupAttributeValueCache1source", "grouper");
          provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
          
          provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", membershipGroupMembershipAttributeName);
          
          provisionerSuffixToValue.put("targetMembershipAttribute.0.translateFromGrouperProvisioningGroupField", "groupAttributeValueCache1") ;
          
          groupAttributeConfigIndex++;
          
        }
        
        String membershipTableEntityColumn = startWithSuffixToValue.get("membershipTableEntityColumn");
                
        provisionerSuffixToValue.put("targetMembershipAttribute.1.name", membershipTableEntityColumn);
        
        String membershipTableEntityValue = startWithSuffixToValue.get("membershipTableEntityValue");

        if (StringUtils.equalsAny(membershipTableEntityValue, "email", "entityPrimaryKey", "idIndex", "uuid", "subjectId", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
          
          provisionerSuffixToValue.put("targetMembershipAttribute.1.translateExpressionType", "grouperProvisioningEntityField");
          if (StringUtils.equals("email", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "email");
          }
          if (StringUtils.equals("idIndex", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "idIndex");
          }
          if (StringUtils.equals("uuid", membershipTableEntityValue)) {
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
          if (StringUtils.equals("entityPrimaryKey", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField",  "entityAttributeValueCache0");
          }
        } else if (StringUtils.equalsAny(membershipTableEntityValue, "script", "other")) {
          String membershipEntityMembershipAttributeName = startWithSuffixToValue.get("membershipEntityMembershipAttributeName");
          
          provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".name", membershipEntityMembershipAttributeName);
          if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasEntityTable"), false)) {
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".showAdvancedAttribute", "true");
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".showAttributeCrud", "true");
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".insert", "false");
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".update", "false");
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".select", "false");
          }
          
          if (StringUtils.equals("script", membershipTableEntityValue)) {
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpressionType", "translationScript");
            String membershipTableEntityValueTranslationScript = startWithSuffixToValue.get("membershipTableEntityValueTranslationScript");
            provisionerSuffixToValue.put("targetEntityAttribute."+entityAttributeConfigIndex+".translateExpression", membershipTableEntityValueTranslationScript);
          }
          
          provisionerSuffixToValue.put("entityAttributeValueCacheHas", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache1has", "true");
          provisionerSuffixToValue.put("entityAttributeValueCache1source", "grouper");
          provisionerSuffixToValue.put("entityAttributeValueCache1type", "entityAttribute");
          
          provisionerSuffixToValue.put("entityAttributeValueCache1entityAttribute", membershipEntityMembershipAttributeName);
          
          provisionerSuffixToValue.put("targetMembershipAttribute.1.translateFromGrouperProvisioningEntityField", "entityAttributeValueCache1") ;
          
          entityAttributeConfigIndex++;
          
        }
        
        provisionerSuffixToValue.put("membershipGroupForeignKeyColumn", membershipTableGroupColumn);
        provisionerSuffixToValue.put("membershipEntityForeignKeyColumn", membershipTableEntityColumn);
    }
    
    
    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
      
      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
        provisionerSuffixToValue.put("customizeGroupCrud", "true");
        provisionerSuffixToValue.put("selectGroups", "false");
        provisionerSuffixToValue.put("insertGroups", "false");
        provisionerSuffixToValue.put("deleteGroups", "false");
        provisionerSuffixToValue.put("updateGroups", "false");
      }
      
      String entityMembershipAttributeValue = startWithSuffixToValue.get("entityMembershipAttributeValue");
      
      if (StringUtils.equals(entityMembershipAttributeValue, "groupPrimaryKey")) {
        entityMembershipAttributeValue = startWithSuffixToValue.get("groupTableIdColumn");
      }
      
      if (!StringUtils.equals((String)provisionerSuffixToValue.get("groupAttributeValueCache0groupAttribute"), entityMembershipAttributeValue)) {
        
        provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
        
        provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
        provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
        provisionerSuffixToValue.put("groupAttributeValueCache1source", "grouper");
        provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
        
        if (!StringUtils.equals("script", entityMembershipAttributeValue) && !StringUtils.equals("other", entityMembershipAttributeValue)) {
          provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", entityMembershipAttributeValue);
        }
   
        provisionerSuffixToValue.put("entityMembershipAttributeValue", "groupAttributeValueCache1");
        
        String groupAttributeNameForMemberships = startWithSuffixToValue.get("groupAttributeNameForMemberships");
        
        provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", groupAttributeNameForMemberships);
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAdvancedAttribute", "true");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAttributeCrud", "true");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".insert", "false");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".update", "false");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".select", "false");
        }
        
        if (StringUtils.equals("script", entityMembershipAttributeValue)) {
          String entityMembershipAttributeValueTranslationScript = startWithSuffixToValue.get("entityMembershipAttributeValueTranslationScript");
          
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "translationScript");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpression", entityMembershipAttributeValueTranslationScript);
        } else if (!StringUtils.equals("other", entityMembershipAttributeValue)) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", entityMembershipAttributeValue);
        }
        
        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", groupAttributeNameForMemberships);
        
        if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
          provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".storageType", "separateAttributesTable");
        }
        
        groupAttributeConfigIndex++;
        
      } else {
        provisionerSuffixToValue.put("entityMembershipAttributeValue", "groupAttributeValueCache0");
      }
      
    }
    
//    if (StringUtils.equals(startWithSuffixToValue.get("membershipStructure"), "entityAttributes")) {
//      
//      if (!GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupTable"), false)) {
//        provisionerSuffixToValue.put("customizeGroupCrud", "true");
//        provisionerSuffixToValue.put("selectGroups", "false");
//      }
//      
//      String entityMembershipAttributeValue = startWithSuffixToValue.get("entityMembershipAttributeValue");
//      
//      if (StringUtils.equals(entityMembershipAttributeValue, "groupPrimaryKey")) {
//        entityMembershipAttributeValue = startWithSuffixToValue.get("groupTableIdColumn");
//      }
//      
//      if (!StringUtils.equals((String)provisionerSuffixToValue.get("groupAttributeValueCache0groupAttribute"), entityMembershipAttributeValue)) {
//        
//        provisionerSuffixToValue.put("operateOnGrouperGroups", "true");
//        
//        provisionerSuffixToValue.put("groupAttributeValueCacheHas", "true");
//        provisionerSuffixToValue.put("groupAttributeValueCache1has", "true");
//        provisionerSuffixToValue.put("groupAttributeValueCache1source", "grouper");
//        provisionerSuffixToValue.put("groupAttributeValueCache1type", "groupAttribute");
//        
//        if (!StringUtils.equals("script", entityMembershipAttributeValue) && !StringUtils.equals("other", entityMembershipAttributeValue)) {
//          provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", entityMembershipAttributeValue);
//        }
//      }
//      
//      String groupAttributeNameForMemberships = startWithSuffixToValue.get("groupAttributeNameForMemberships");
//      
//      provisionerSuffixToValue.put("groupAttributeValueCache1groupAttribute", groupAttributeNameForMemberships);
//      
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAdvancedAttribute", "true");
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".showAttributeCrud", "true");
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".insert", "false");
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".update", "false");
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".select", "false");
//      
//      if (StringUtils.equals("script", entityMembershipAttributeValue)) {
//        String entityMembershipAttributeValueTranslationScript = startWithSuffixToValue.get("entityMembershipAttributeValueTranslationScript");
//        
//        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "translationScript");
//        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpression", entityMembershipAttributeValueTranslationScript);
//      } else if (!StringUtils.equals("other", entityMembershipAttributeValue)) {
//        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateExpressionType", "grouperProvisioningGroupField");
//        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".translateFromGrouperProvisioningGroupField", entityMembershipAttributeValue);
//      }
//      
//      provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".name", groupAttributeNameForMemberships);
//      if (GrouperUtil.booleanValue(startWithSuffixToValue.get("hasGroupAttributeTable"), false)) {
//        provisionerSuffixToValue.put("targetGroupAttribute."+groupAttributeConfigIndex+".storageType", "separateAttributesTable");
//      }
//      
//      groupAttributeConfigIndex++;
//      
//    }
    if (entityAttributeConfigIndex > 0) {
      provisionerSuffixToValue.put("numberOfEntityAttributes", entityAttributeConfigIndex);
    }
    if (groupAttributeConfigIndex > 0) {
      provisionerSuffixToValue.put("numberOfGroupAttributes", groupAttributeConfigIndex);
    }

    if (GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledFullSyncDaemon"), true) || GrouperUtil.booleanValue(startWithSuffixToValue.get("addDisabledIncrementalSyncDaemon"), true)) {
      provisionerSuffixToValue.put("showAdvanced", "true");
    }
    
  }

  @Override
  public Class<? extends ProvisioningConfiguration> getProvisioningConfiguration() {
    return SqlProvisionerConfiguration.class;
  }
}
