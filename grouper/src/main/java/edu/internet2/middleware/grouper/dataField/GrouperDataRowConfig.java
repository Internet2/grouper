package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperDataRowConfig {

  /**
   * 
   */
  public GrouperDataRowConfig() {
  }

  /**
   * 
   * @param configId
   */
  public void readFromConfig(String configId) {
    
    this.configId = configId;
    
    String rowAliasesString = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperDataRow." + configId + ".rowAliases");
    this.rowAliases = GrouperUtil.splitTrimToSet(rowAliasesString, ",");
    
    //TODO String rowPrivacyRealmString = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperDataRow." + configId + ".rowPrivacyRealm");
    //this.privacyRealmName = rowPrivacyRealmString;

    int rowNumberOfDataFields = GrouperConfig.retrieveConfig().propertyValueIntRequired("grouperDataRow." + configId + ".rowNumberOfDataFields");
    for (int i=0;i<rowNumberOfDataFields;i++) {
      String dataFieldConfigId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperDataRow." + configId + ".rowDataField." + i + ".colDataFieldConfigId");
      this.dataFieldConfigIds.add(dataFieldConfigId);

      boolean rowKeyField = GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDataRow." + configId + ".rowDataField." + i + ".rowKeyField", false);
      if (rowKeyField) {
        this.rowKeyFieldConfigIds.add(dataFieldConfigId);
      }

    }
    
  }
  
  private String configId;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  /**
   * if this data field is the key or part of a composite key that uniquely matches this row from source and grouper
   * {valueType: "boolean", required: true, order: 4010, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30}
   * grouperDataRow.dataRowConfigId.rowDataField.$i$.rowKeyField =
   */
  private Set<String> rowKeyFieldConfigIds = new LinkedHashSet<>();
  
  
  
  /**
   * if this data field is the key or part of a composite key that uniquely matches this row from source and grouper
   * {valueType: "boolean", required: true, order: 4010, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30}
   * grouperDataRow.dataRowConfigId.rowDataField.$i$.rowKeyField =
   * @return config ids
   */
  public Set<String> getRowKeyFieldConfigIds() {
    return this.rowKeyFieldConfigIds;
  }


  /**
   * number of fields in this row
   * {valueType: "string", order: 3000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataRow.dataRowConfigId.rowNumberOfDataFields = 
   *
   * data field for this column
   * {valueType: "string", required: true, order: 4000, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataRow.dataRowConfigId.rowDataField.$i$.colDataFieldConfigId =
   */
  private Set<String> dataFieldConfigIds = new HashSet<>();
  
  

  /**
   * number of fields in this row
   * {valueType: "string", order: 3000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataRow.dataRowConfigId.rowNumberOfDataFields = 
   *
   * data field for this column
   * {valueType: "string", required: true, order: 4000, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataRow.dataRowConfigId.rowDataField.$i$.colDataFieldConfigId =
   * @return
   */
  public Set<String> getDataFieldConfigIds() {
    return this.dataFieldConfigIds;
  }

  /**
   * number of fields in this row
   * {valueType: "string", order: 3000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
   * grouperDataRow.dataRowConfigId.rowNumberOfDataFields = 
   *
   * data field for this column
   * {valueType: "string", required: true, order: 4000, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
   * grouperDataRow.dataRowConfigId.rowDataField.$i$.colDataFieldConfigId =
   * @param dataFieldConfigIds1
   */
  public void setDataFieldConfigIds(Set<String> dataFieldConfigIds1) {
    this.dataFieldConfigIds = dataFieldConfigIds1;
  }

  /**
   * aliases that this row is referred to as
   * {valueType: "string", order: 1000, subSection: "dataRowConfig", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
   * grouperDataRow.dataRowConfigId.rowAliases = 
   */
  private Set<String> rowAliases = new HashSet<>();
  
  /**
   * privacy realm for people who can see or use this data row
   * {valueType: "string", order: 2000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowPrivacyRealm$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealm"}
   * grouperDataRow.dataRowConfigId.rowPrivacyRealm = 
   */
  private String privacyRealmName = null;

  
  
  /**
   * privacy realm for people who can see or use this data row
   * {valueType: "string", order: 2000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowPrivacyRealm$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealm"}
   * grouperDataRow.dataRowConfigId.rowPrivacyRealm = 
   * @return realm
   */
  public String getPrivacyRealmName() {
    return this.privacyRealmName;
  }

  /**
   * privacy realm for people who can see or use this data row
   * {valueType: "string", order: 2000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowPrivacyRealm$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealm"}
   * grouperDataRow.dataRowConfigId.rowPrivacyRealm = 
   * @param privacyRealm1
   */
  public void setPrivacyRealmName(String privacyRealm1) {
    this.privacyRealmName = privacyRealm1;
  }

  /**
   * aliases that this row is referred to as
   * {valueType: "string", order: 1000, subSection: "dataRowConfig", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
   * grouperDataRow.dataRowConfigId.rowAliases = 
   * @return field aliases
   */
  public Set<String> getRowAliases() {
    return rowAliases;
  }

  /**
   * aliases that this row is referred to as
   * {valueType: "string", order: 1000, subSection: "dataRowConfig", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
   * grouperDataRow.dataRowConfigId.rowAliases = 
   * @param fieldAliases
   */
  public void setRowAliases(Set<String> fieldAliases) {
    this.rowAliases = fieldAliases;
  }


}
