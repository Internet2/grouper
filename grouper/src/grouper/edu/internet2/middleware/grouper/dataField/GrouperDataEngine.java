package edu.internet2.middleware.grouper.dataField;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;

public class GrouperDataEngine {

  private Map<String, Object> debugMap = new LinkedHashMap<>();
  
  
  
  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperDataEngine.class);
      
  /**
   * privacy realm
   */
  public static final Pattern privacyRealmPattern = Pattern.compile("^grouperPrivacyRealm\\.([^.]+)\\.privacyRealmName$");
  
  /**
   * data field
   */
  public static final Pattern dataFieldPattern = Pattern.compile("^grouperDataField\\.([^.]+)\\.fieldAliases$");
  
  /**
   * data provider
   */
  public static final Pattern dataProviderPattern = Pattern.compile("^grouperDataProvider\\.([^.]+)\\.name$");
  
  /**
   * data provider
   */
  public static final Pattern dataRowPattern = Pattern.compile("^grouperDataRow\\.([^.]+)\\.rowAliases$");
  
  /**
   * data provider query
   */
  public static final Pattern dataProviderQueryPattern = Pattern.compile("^grouperDataProviderQuery\\.([^.]+)\\.providerConfigId$");
  
  /**
   * data provider change log query
   */
  public static final Pattern dataProviderChangeLogQueryPattern = Pattern.compile("^grouperDataProviderChangeLogQuery\\.([^.]+)\\.providerConfigId$");
  
  /**
   * field configs by config id
   */
  private Map<String, GrouperDataFieldConfig> fieldConfigByConfigId = new HashMap<String, GrouperDataFieldConfig>();
  
  /**
   * lower alias to GrouperDataFieldConfig
   */
  private Map<String, GrouperDataFieldConfig> fieldConfigByAlias = new HashMap<String, GrouperDataFieldConfig>();
  
  /**
   * lower alias to GrouperDataFieldConfig
   * @return field config
   */
  public Map<String, GrouperDataFieldConfig> getFieldConfigByAlias() {
    return fieldConfigByAlias;
  }

  /**
   * lower alias to GrouperDataRowConfig
   */
  private Map<String, GrouperDataRowConfig> rowConfigByAlias = new HashMap<String, GrouperDataRowConfig>();
  
  /**
   * lower alias to GrouperDataRowConfig
   * @return field config
   */
  public Map<String, GrouperDataRowConfig> getRowConfigByAlias() {
    return rowConfigByAlias;
  }

  /**
   * field configs by config id
   * @return
   */
  public Map<String, GrouperDataFieldConfig> getFieldConfigByConfigId() {
    return fieldConfigByConfigId;
  }

  /**
   * row configs by config id
   * @return
   */
  public Map<String, GrouperDataRowConfig> getRowConfigByConfigId() {
    return rowConfigByConfigId;
  }

  /**
   * providers by config id
   * @return
   */
  public Map<String, GrouperDataProviderConfig> getProviderConfigByConfigId() {
    return providerConfigByConfigId;
  }

  /**
   * row configs by config id
   */
  private Map<String, GrouperDataRowConfig> rowConfigByConfigId = new HashMap<String, GrouperDataRowConfig>();
  
  /**
   * providers by config id
   */
  private Map<String, GrouperDataProviderConfig> providerConfigByConfigId = new HashMap<String, GrouperDataProviderConfig>();
  
  public void loadConfigRows(GrouperConfig grouperConfig) {
    
    //  # aliases that this row is referred to as
    //  # {valueType: "string", order: 1000, subSection: "dataRowConfig", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
    //  # grouperDataRow.dataRowConfigId.rowAliases = 
    //
    //  # privacy realm for people who can see or use this data row
    //  # {valueType: "string", order: 2000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowPrivacyRealm$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperPrivacyRealm"}
    //  # grouperDataRow.dataRowConfigId.rowPrivacyRealm = 
    //
    //  # number of fields in this row
    //  # {valueType: "string", order: 3000, subSection: "dataRowConfig", required: true, regex: "^grouperDataRow\\.[^.]+\\.rowNumberOfDataFields$", formElement: "dropdown", optionValues: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"]}
    //  # grouperDataRow.dataRowConfigId.rowNumberOfDataFields = 
    //
    //  # data field for this column
    //  # {valueType: "string", required: true, order: 4000, showEl: "${rowNumberOfDataFields > $i$}", repeatGroup: "rowDataField", repeatCount: 30, formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataField"}
    //  # grouperDataRow.dataRowConfigId.rowDataField.$i$.colDataFieldConfigId =
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataRowPattern));
    
    for (String configId : configIdsInConfig) {
      GrouperDataRowConfig grouperDataRowConfig = new GrouperDataRowConfig();
      grouperDataRowConfig.readFromConfig(configId);
      rowConfigByConfigId.put(configId, grouperDataRowConfig);
      
      for (String alias : grouperDataRowConfig.getRowAliases()) {
        rowConfigByAlias.put(alias.toLowerCase(), grouperDataRowConfig);
      }

    }
    
  }
  
  public void loadConfigFields(GrouperConfig grouperConfig) {
    
    //  # aliases that this field is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
    //  # grouperDataField.dataFieldConfigId.fieldAliases = 
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataFieldPattern));
    
    for (String configId : configIdsInConfig) {
      GrouperDataFieldConfig grouperDataFieldConfig = new GrouperDataFieldConfig();
      grouperDataFieldConfig.readFromConfig(configId);
      fieldConfigByConfigId.put(configId, grouperDataFieldConfig);
      for (String alias : grouperDataFieldConfig.getFieldAliases()) {
        fieldConfigByAlias.put(alias.toLowerCase(), grouperDataFieldConfig);
      }
    }
    
  }
  
  public void loadConfigProviders(GrouperConfig grouperConfig) {
    
    //  # data provider name, not really needed or used, but there to setup the provider
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
    //  # grouperDataProvider.dataProviderConfigId.name = 

    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }

    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataProviderPattern));
    
    for (String configId : configIdsInConfig) {
      GrouperDataProviderConfig grouperDataProviderConfig = new GrouperDataProviderConfig();
      grouperDataProviderConfig.readFromConfig(configId);
      providerConfigByConfigId.put(configId, grouperDataProviderConfig);
    }
  }
  
  /**
   * take data fields and make sure the have an internal id
   */
  public static void syncDataFields(GrouperConfig grouperConfig) {
    
    //  # aliases that this field is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
    //  # grouperDataField.dataFieldConfigId.fieldAliases = 
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataFieldPattern));

    
    List<GrouperDataField> grouperDataFieldsInDb = GrouperUtil.nonNull(GrouperDataFieldDao.selectAll());

    Map<String, GrouperDataField> configIdToGrouperDataFieldInDb = new HashMap<String, GrouperDataField>();
    Map<Long, GrouperDataField> internalIdToGrouperDataFieldInDb = new HashMap<Long, GrouperDataField>();

    for (GrouperDataField grouperDataField : grouperDataFieldsInDb) {
      configIdToGrouperDataFieldInDb.put(grouperDataField.getConfigId(), grouperDataField);
      internalIdToGrouperDataFieldInDb.put(grouperDataField.getInternalId(), grouperDataField);
    }
    
    // additions
    Set<String> configIdsToInsert = new HashSet<String>(configIdsInConfig);
    configIdsToInsert.removeAll(configIdToGrouperDataFieldInDb.keySet());
    for (String configIdToInsert : configIdsToInsert) {
      GrouperDataFieldDao.findOrAdd(configIdToInsert);
    }
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataFieldInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataField grouperDataField = configIdToGrouperDataFieldInDb.get(configIdToDelete);
      GrouperDataFieldDao.delete(grouperDataField);
    }
    
  }
  
  /**
   * take data rows and make sure the have an internal id
   */
  public static void syncDataRows(GrouperConfig grouperConfig) {
    
    //  # aliases that this row is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
    //  # grouperDataRow.dataRowConfigId.rowAliases = 
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }

    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataRowPattern));
    
    List<GrouperDataRow> grouperDataRowsInDb = GrouperUtil.nonNull(GrouperDataRowDao.selectAll());

    Map<String, GrouperDataRow> configIdToGrouperDataRowInDb = new HashMap<String, GrouperDataRow>();
    Map<Long, GrouperDataRow> internalIdToGrouperDataRowInDb = new HashMap<Long, GrouperDataRow>();

    for (GrouperDataRow grouperDataRow : grouperDataRowsInDb) {
      configIdToGrouperDataRowInDb.put(grouperDataRow.getConfigId(), grouperDataRow);
      internalIdToGrouperDataRowInDb.put(grouperDataRow.getInternalId(), grouperDataRow);
    }
    
    // additions
    Set<String> configIdsToInsert = new HashSet<String>(configIdsInConfig);
    configIdsToInsert.removeAll(configIdToGrouperDataRowInDb.keySet());
    for (String configIdToInsert : configIdsToInsert) {
      GrouperDataRowDao.findOrAdd(configIdToInsert);
    }
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataRowInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataRow grouperDataRow = configIdToGrouperDataRowInDb.get(configIdToDelete);
      GrouperDataRowDao.delete(grouperDataRow);
    }

  }
  
  /**
   * take data aliases and make sure the have an internal id
   */
  public static void syncDataAliases(GrouperConfig grouperConfig) {
    
    //  # aliases that this field is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
    //  # grouperDataField.dataFieldConfigId.fieldAliases = 
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }

    Set<String> dataFieldConfigIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataFieldPattern));
    Map<String, Set<String>> dataFieldConfigIdToAliases = new HashMap<String, Set<String>>();
    Map<String, String> dataFieldAliasNameLowerToConfigId = new HashMap<String, String>();

    for (String configId : dataFieldConfigIdsInConfig) {
      
      String aliasesString = grouperConfig.propertyValueString("grouperDataField." + configId + ".fieldAliases");
      Set<String> aliases = GrouperUtil.splitTrimToSet(aliasesString, ",");
      
      dataFieldConfigIdToAliases.put(configId, aliases);
      
      for (String alias : aliases) {
        if (dataFieldAliasNameLowerToConfigId.containsKey(alias.toLowerCase())) {
          throw new RuntimeException("Two aliases used in different dataField configs! " + alias + ", " + configId + ", " + dataFieldAliasNameLowerToConfigId.get(alias.toLowerCase()));
        }
        dataFieldAliasNameLowerToConfigId.put(alias.toLowerCase(), configId);
      }
      
    }

    //  # aliases that this row is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
    //  # grouperDataRow.dataRowConfigId.rowAliases = 
    Set<String> dataRowConfigIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataRowPattern));
    Map<String, Set<String>> dataRowConfigIdToAliases = new HashMap<String, Set<String>>();
    Map<String, String> dataRowAliasNameLowerToConfigId = new HashMap<String, String>();

    for (String configId : dataRowConfigIdsInConfig) {
      
      String aliasesString = grouperConfig.propertyValueString("grouperDataRow." + configId + ".rowAliases");
      Set<String> aliases = GrouperUtil.splitTrimToSet(aliasesString, ",");
      
      dataRowConfigIdToAliases.put(configId, aliases);

      for (String alias : aliases) {
        if (dataFieldAliasNameLowerToConfigId.containsKey(alias.toLowerCase())) {
          throw new RuntimeException("Two aliases used in different configs! " + alias + ", row: " + configId + ", field: " + dataFieldAliasNameLowerToConfigId.get(alias.toLowerCase()));
        }
        if (dataRowAliasNameLowerToConfigId.containsKey(alias.toLowerCase())) {
          throw new RuntimeException("Two aliases used in different dataRow configs! " + alias + ", " + configId + ", " + dataRowAliasNameLowerToConfigId.get(alias.toLowerCase()));
        }
        dataRowAliasNameLowerToConfigId.put(alias.toLowerCase(), configId);
      }

    }

    
    List<GrouperDataField> grouperDataFieldsInDb = GrouperUtil.nonNull(GrouperDataFieldDao.selectAll());

    Map<String, GrouperDataField> configIdToGrouperDataFieldInDb = new HashMap<String, GrouperDataField>();
    Map<Long, GrouperDataField> internalIdToGrouperDataFieldInDb = new HashMap<Long, GrouperDataField>();

    // index fields in db
    for (GrouperDataField grouperDataField : grouperDataFieldsInDb) {
      configIdToGrouperDataFieldInDb.put(grouperDataField.getConfigId(), grouperDataField);
      internalIdToGrouperDataFieldInDb.put(grouperDataField.getInternalId(), grouperDataField);
    }

    List<GrouperDataRow> grouperDataRowsInDb = GrouperUtil.nonNull(GrouperDataRowDao.selectAll());

    Map<String, GrouperDataRow> configIdToGrouperDataRowInDb = new HashMap<String, GrouperDataRow>();
    Map<Long, GrouperDataRow> internalIdToGrouperDataRowInDb = new HashMap<Long, GrouperDataRow>();

    // index rows in db
    for (GrouperDataRow grouperDataRow : grouperDataRowsInDb) {
      configIdToGrouperDataRowInDb.put(grouperDataRow.getConfigId(), grouperDataRow);
      internalIdToGrouperDataRowInDb.put(grouperDataRow.getInternalId(), grouperDataRow);
    }

    
    List<GrouperDataAlias> grouperDataFieldAliasesInDb = GrouperUtil.nonNull(GrouperDataAliasDao.selectAllFieldAliases());
    List<GrouperDataAlias> grouperDataRowAliasesInDb = GrouperUtil.nonNull(GrouperDataAliasDao.selectAllRowAliases());
    
    Map<Long, GrouperDataAlias> internalIdToGrouperDataAliasInDb = new HashMap<Long, GrouperDataAlias>();
    Map<String, GrouperDataAlias> aliasNameToGrouperDataAliasInDb = new HashMap<String, GrouperDataAlias>();

    Map<String, Set<String>> configIdToGrouperDataFieldAliasesInDb = new HashMap<String, Set<String>>();

    // index field aliases in db
    for (GrouperDataAlias grouperDataAlias : grouperDataFieldAliasesInDb) {

      internalIdToGrouperDataAliasInDb.put(grouperDataAlias.getInternalId(), grouperDataAlias);
      aliasNameToGrouperDataAliasInDb.put(grouperDataAlias.getName(), grouperDataAlias);
      
      GrouperDataField grouperDataField = internalIdToGrouperDataFieldInDb.get(grouperDataAlias.getDataFieldInternalId());
      
      Set<String> aliases = configIdToGrouperDataFieldAliasesInDb.get(grouperDataField.getConfigId());
      if (aliases == null) {
        aliases = new HashSet<String>();
        configIdToGrouperDataFieldAliasesInDb.put(grouperDataField.getConfigId(), aliases);
      }
      
      aliases.add(grouperDataAlias.getName());
      
    }

    Map<String, Set<String>> configIdToGrouperDataRowAliasesInDb = new HashMap<String, Set<String>>();

    // index row aliases in db
    for (GrouperDataAlias grouperDataAlias : grouperDataRowAliasesInDb) {

      internalIdToGrouperDataAliasInDb.put(grouperDataAlias.getInternalId(), grouperDataAlias);
      aliasNameToGrouperDataAliasInDb.put(grouperDataAlias.getName(), grouperDataAlias);

      GrouperDataRow grouperDataRow = internalIdToGrouperDataRowInDb.get(grouperDataAlias.getDataRowInternalId());
      
      Set<String> aliases = configIdToGrouperDataRowAliasesInDb.get(grouperDataRow.getConfigId());
      if (aliases == null) {
        aliases = new HashSet<String>();
        configIdToGrouperDataRowAliasesInDb.put(grouperDataRow.getConfigId(), aliases);
      }
      
      aliases.add(grouperDataAlias.getName());
    }

    // do deletes before inserts since a row delete might need to happen before a field add
    
    // delete field aliases that shouldnt be there
    for (String configId : configIdToGrouperDataFieldAliasesInDb.keySet()) {
      
      Set<String> aliasesInDbToDelete = new HashSet<String>(configIdToGrouperDataFieldAliasesInDb.get(configId));

      aliasesInDbToDelete.removeAll(GrouperUtil.nonNull(dataFieldConfigIdToAliases.get(configId)));
      
      for (String aliasInDbToDelete : aliasesInDbToDelete) {
        GrouperDataAlias grouperDataAlias = aliasNameToGrouperDataAliasInDb.get(aliasInDbToDelete);
        GrouperDataAliasDao.delete(grouperDataAlias);
      }
    }

    // delete row aliases that shouldnt be there
    for (String configId : configIdToGrouperDataRowAliasesInDb.keySet()) {
      
      Set<String> aliasesInDbToDelete = new HashSet<String>(configIdToGrouperDataRowAliasesInDb.get(configId));

      aliasesInDbToDelete.removeAll(GrouperUtil.nonNull(dataRowConfigIdToAliases.get(configId)));
      
      for (String aliasInDbToDelete : aliasesInDbToDelete) {
        GrouperDataAlias grouperDataAlias = aliasNameToGrouperDataAliasInDb.get(aliasInDbToDelete);
        GrouperDataAliasDao.delete(grouperDataAlias);
      }
    }

    // add field aliases that should be there
    for (String configId : dataFieldConfigIdToAliases.keySet()) {
      
      Set<String> aliasesToAdd = new HashSet<String>(dataFieldConfigIdToAliases.get(configId));

      aliasesToAdd.removeAll(GrouperUtil.nonNull(configIdToGrouperDataFieldAliasesInDb.get(configId)));
      
      GrouperDataField grouperDataField = configIdToGrouperDataFieldInDb.get(configId);
      
      for (String aliasToAdd : aliasesToAdd) {
        GrouperDataAliasDao.findOrAddFieldAlias(grouperDataField.getInternalId(), aliasToAdd);
      }
    }
    
    // add row aliases that should be there
    for (String configId : dataRowConfigIdToAliases.keySet()) {
      
      Set<String> aliasesToAdd = new HashSet<String>(dataRowConfigIdToAliases.get(configId));

      aliasesToAdd.removeAll(GrouperUtil.nonNull(configIdToGrouperDataRowAliasesInDb.get(configId)));
      
      GrouperDataRow grouperDataRow = configIdToGrouperDataRowInDb.get(configId);
      
      for (String aliasToAdd : aliasesToAdd) {
        GrouperDataAliasDao.findOrAddRowAlias(grouperDataRow.getInternalId(), aliasToAdd);
      }
    }
    
  }

  public static void syncDataProviders(GrouperConfig grouperConfig) {

    //  # data provider name, not really needed or used, but there to setup the provider
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
    //  # grouperDataProvider.dataProviderConfigId.name = 

    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }

    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataProviderPattern));

    
    List<GrouperDataProvider> grouperDataProvidersInDb = GrouperUtil.nonNull(GrouperDataProviderDao.selectAll());

    Map<String, GrouperDataProvider> configIdToGrouperDataProviderInDb = new HashMap<String, GrouperDataProvider>();
    Map<Long, GrouperDataProvider> internalIdToGrouperDataProviderInDb = new HashMap<Long, GrouperDataProvider>();

    for (GrouperDataProvider grouperDataProvider : grouperDataProvidersInDb) {
      configIdToGrouperDataProviderInDb.put(grouperDataProvider.getConfigId(), grouperDataProvider);
      internalIdToGrouperDataProviderInDb.put(grouperDataProvider.getInternalId(), grouperDataProvider);
    }
    
    // additions
    Set<String> configIdsToInsert = new HashSet<String>(configIdsInConfig);
    configIdsToInsert.removeAll(configIdToGrouperDataProviderInDb.keySet());
    for (String configIdToInsert : configIdsToInsert) {
      GrouperDataProviderDao.findOrAdd(configIdToInsert);
    }
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataProviderInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataProvider grouperDataProvider = configIdToGrouperDataProviderInDb.get(configIdToDelete);
      GrouperDataProviderDao.delete(grouperDataProvider);
    }
    
  }

  private GrouperDataProviderData grouperDataProviderData = new GrouperDataProviderData();
  
  public GrouperDataProviderData getGrouperDataProviderData() {
    return grouperDataProviderData;
  }
  
  private GrouperDataProviderIndex grouperDataProviderIndex = new GrouperDataProviderIndex();
  
  public GrouperDataProviderIndex getGrouperDataProviderIndex() {
    return grouperDataProviderIndex;
  }

  public void loadFieldsAndRows(GrouperConfig grouperConfig) {
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    // load config from config file
    this.loadConfigFields(grouperConfig);
    this.loadConfigProviders(grouperConfig);

    this.loadConfigRows(grouperConfig);

    // retrieve all fields and rows (definitions) from database
    this.grouperDataProviderData.setGrouperDataFields(GrouperUtil.nonNull(GrouperDataFieldDao.selectAll()));
    this.grouperDataProviderData.setGrouperDataRows(GrouperUtil.nonNull(GrouperDataRowDao.selectAll()));
    
    // index those
    {
      for (GrouperDataField grouperDataField : this.grouperDataProviderData.getGrouperDataFields()) {
        
        // maybe things in DB arent in sync with the config yet
        if (!this.getFieldConfigByConfigId().containsKey(grouperDataField.getConfigId())) {
          continue;
        }
        GrouperDataFieldWrapper grouperDataFieldWrapper = new GrouperDataFieldWrapper(this, grouperDataField);
        this.grouperDataProviderIndex.getFieldWrapperByInternalId().put(grouperDataField.getInternalId(), grouperDataFieldWrapper);
        this.grouperDataProviderIndex.getFieldWrapperByConfigId().put(grouperDataField.getConfigId(), grouperDataFieldWrapper);

        GrouperDataFieldConfig grouperDataFieldConfig = this.getFieldConfigByConfigId().get(grouperDataField.getConfigId());
        grouperDataFieldWrapper.setGrouperDataFieldConfig(grouperDataFieldConfig);
        for (String alias : grouperDataFieldConfig.getFieldAliases()) {
          this.grouperDataProviderIndex.getFieldWrapperByLowerAlias().put(alias.toLowerCase(), grouperDataFieldWrapper);
        }
      }
    }

    {
  
      for (GrouperDataRow grouperDataRow : this.grouperDataProviderData.getGrouperDataRows()) {

        // maybe things in DB arent in sync with the config yet
        if (!this.getRowConfigByConfigId().containsKey(grouperDataRow.getConfigId())) {
          continue;
        }
        GrouperDataRowWrapper grouperDataRowWrapper = new GrouperDataRowWrapper(this, grouperDataRow);
        this.grouperDataProviderIndex.getRowWrapperByInternalId().put(grouperDataRow.getInternalId(), grouperDataRowWrapper);
        this.grouperDataProviderIndex.getRowWrapperByConfigId().put(grouperDataRow.getConfigId(), grouperDataRowWrapper);

        GrouperDataRowConfig grouperDataRowConfig = this.getRowConfigByConfigId().get(grouperDataRow.getConfigId());
        grouperDataRowWrapper.setGrouperDataRowConfig(grouperDataRowConfig);
        for (String alias : grouperDataRowConfig.getRowAliases()) {
          this.grouperDataProviderIndex.getRowWrapperByLowerAlias().put(alias.toLowerCase(), grouperDataRowWrapper);
        }
        
      }
    }
    
  }

  private Map<String, GcTableSyncTableMetadata> queryConfigIdToTableMetadata = new HashMap<>();


  
  public Map<String, GcTableSyncTableMetadata> getQueryConfigIdToTableMetadata() {
    return queryConfigIdToTableMetadata;
  }
  
  
}
