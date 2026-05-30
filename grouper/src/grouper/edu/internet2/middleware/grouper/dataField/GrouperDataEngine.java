package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperDataEngine {

  private Map<String, Object> debugMap = new LinkedHashMap<>();
  
  public static void clearHighestLevelCache() {
    sourceIdSubjectIdToIfHasUsedPrivacyRecently.clear();
    sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache.clear();
  }
  
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
  
  
  private Map<String, GrouperPrivacyRealmConfig> privacyRealmConfigByConfigId = new HashMap<String, GrouperPrivacyRealmConfig>();
  
  
  
  
  public Map<String, GrouperPrivacyRealmConfig> getPrivacyRealmConfigByConfigId() {
    return privacyRealmConfigByConfigId;
  }


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
  
  public void loadConfigPrivacyRealms(GrouperConfig grouperConfig) {
    
   /**
    * # name of this privacy realm, not really used, just here to configure the realm
      # {valueType: "string", required: true, regex: "^grouperPrivacyRealm\\.[^.]+\\.privacyRealmName$"}
      # grouperPrivacyRealm.privacyRealmConfigId.privacyRealmName = 
    */
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(privacyRealmPattern));
    
    for (String configId : configIdsInConfig) {
      GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = new GrouperPrivacyRealmConfig();
      grouperPrivacyRealmConfig.readFromConfig(configId);
      privacyRealmConfigByConfigId.put(configId, grouperPrivacyRealmConfig);
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
    GrouperDataFieldDao.insertMissingConfigIds(configIdsToInsert);
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataFieldInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    
    List<GrouperDataField> dataFieldsToDelete = new ArrayList<>();
    
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataField grouperDataField = configIdToGrouperDataFieldInDb.get(configIdToDelete);
      dataFieldsToDelete.add(grouperDataField);
    }
    GrouperDataFieldDao.delete(dataFieldsToDelete);
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
    GrouperDataRowDao.insertMissingConfigIds(configIdsToInsert);
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataRowInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    
    List<GrouperDataRow> dataRowsToDelete = new ArrayList<>();
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataRow grouperDataRow = configIdToGrouperDataRowInDb.get(configIdToDelete);
      dataRowsToDelete.add(grouperDataRow);
    }
    GrouperDataRowDao.delete(dataRowsToDelete);
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
    
    List<GrouperDataAlias> grouperDataAliasesToDelete = new ArrayList<>();
    
    // delete field aliases that shouldnt be there
    for (String configId : configIdToGrouperDataFieldAliasesInDb.keySet()) {
      
      Set<String> aliasesInDbToDelete = new HashSet<String>(configIdToGrouperDataFieldAliasesInDb.get(configId));

      aliasesInDbToDelete.removeAll(GrouperUtil.nonNull(dataFieldConfigIdToAliases.get(configId)));

      for (String aliasInDbToDelete : aliasesInDbToDelete) {
        GrouperDataAlias grouperDataAlias = aliasNameToGrouperDataAliasInDb.get(aliasInDbToDelete);
        grouperDataAliasesToDelete.add(grouperDataAlias);
      }
      
    }
    
    // delete row aliases that shouldnt be there
    for (String configId : configIdToGrouperDataRowAliasesInDb.keySet()) {
      
      Set<String> aliasesInDbToDelete = new HashSet<String>(configIdToGrouperDataRowAliasesInDb.get(configId));

      aliasesInDbToDelete.removeAll(GrouperUtil.nonNull(dataRowConfigIdToAliases.get(configId)));
      
      for (String aliasInDbToDelete : aliasesInDbToDelete) {
        GrouperDataAlias grouperDataAlias = aliasNameToGrouperDataAliasInDb.get(aliasInDbToDelete);
        grouperDataAliasesToDelete.add(grouperDataAlias);
      }
    }
    
    GrouperDataAliasDao.delete(grouperDataAliasesToDelete);

    // add field aliases that should be there
    for (String configId : dataFieldConfigIdToAliases.keySet()) {
      
      Set<String> aliasesToAdd = new HashSet<String>(dataFieldConfigIdToAliases.get(configId));

      aliasesToAdd.removeAll(GrouperUtil.nonNull(configIdToGrouperDataFieldAliasesInDb.get(configId)));
      
      GrouperDataField grouperDataField = configIdToGrouperDataFieldInDb.get(configId);
      if (grouperDataField == null) {
        continue;
      }
      GrouperDataAliasDao.insertMissingAliases(grouperDataField.getInternalId(), null, aliasesToAdd);
    }
    
    // add row aliases that should be there
    for (String configId : dataRowConfigIdToAliases.keySet()) {
      
      Set<String> aliasesToAdd = new HashSet<String>(dataRowConfigIdToAliases.get(configId));

      aliasesToAdd.removeAll(GrouperUtil.nonNull(configIdToGrouperDataRowAliasesInDb.get(configId)));
      
      GrouperDataRow grouperDataRow = configIdToGrouperDataRowInDb.get(configId);
      
      GrouperDataAliasDao.insertMissingAliases(null, grouperDataRow.getInternalId(), aliasesToAdd);
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
    GrouperDataProviderDao.insertMissingConfigIds(configIdsToInsert);
    
    // deletions
    Set<String> configIdsToDelete = new HashSet<String>(configIdToGrouperDataProviderInDb.keySet());
    configIdsToDelete.removeAll(configIdsInConfig);
    
    List<GrouperDataProvider> grouperDataProvidersToDelete = new ArrayList<>();
    
    for (String configIdToDelete : configIdsToDelete) {
      GrouperDataProvider grouperDataProvider = configIdToGrouperDataProviderInDb.get(configIdToDelete);
      grouperDataProvidersToDelete.add(grouperDataProvider);
    }
    GrouperDataProviderDao.delete(grouperDataProvidersToDelete);
    
  }

  private GrouperDataProviderData grouperDataProviderData = new GrouperDataProviderData();
  
  public GrouperDataProviderData getGrouperDataProviderData() {
    return grouperDataProviderData;
  }
  
  private GrouperDataProviderIndex grouperDataProviderIndex = new GrouperDataProviderIndex();
  
  public GrouperDataProviderIndex getGrouperDataProviderIndex() {
    return grouperDataProviderIndex;
  }

  /**
   * replace the provider index with a fresh instance.
   * used between subject ID batches in full sync to release per-batch state
   * (member wrappers, assigns, dictionary text, members to add).
   * @param grouperDataProviderIndex the new index
   */
  public void setGrouperDataProviderIndex(GrouperDataProviderIndex grouperDataProviderIndex) {
    this.grouperDataProviderIndex = grouperDataProviderIndex;
  }

  public void loadFieldsAndRows(GrouperConfig grouperConfig) {
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    // load config from config file
    this.loadConfigFields(grouperConfig);
    this.loadConfigProviders(grouperConfig);
    this.loadConfigPrivacyRealms(grouperConfig);

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
  
  private static ExpirableCache<MultiKey, Boolean> sourceIdSubjectIdToIfHasUsedPrivacyRecently = new ExpirableCache<>(60); // 60 minutes
  
  private static ExpirableCache<Boolean, Map<MultiKey, Boolean>> sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache = new ExpirableCache<>(2); // 2 minutes
  
  
  
  /**
   * find all the privacy realm memberships for a subject and add them to the cache
   * @param grouperPrivacyRealmConfig
   * @param subject
   * @return
   */
  private Map<MultiKey, Boolean> populatePrivacyCacheForUser(Set<MultiKey> sourceIdSubjectIdsSet, boolean replaceExisting) {
    
    Map<MultiKey, Boolean> sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess = new HashMap<MultiKey, Boolean>();
    
    if (GrouperUtil.length(sourceIdSubjectIdsSet) == 0) {
      return sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess;
    }

    // get a list of all groups of all privacy realm configs
    Set<String> groupNames = new HashSet<String>();
    for (GrouperPrivacyRealmConfig grouperPrivacyRealmConfig : this.getPrivacyRealmConfigByConfigId().values()) {
      
      String viewersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmViewersGroupName();
      String updatersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmUpdatersGroupName();
      String readersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmReadersGroupName();
      
      if (StringUtils.isNotBlank(viewersGroupName)) {
        groupNames.add(viewersGroupName);
      }
      
      if (StringUtils.isNotBlank(updatersGroupName)) {
        groupNames.add(updatersGroupName);
      }
      
      if (StringUtils.isNotBlank(readersGroupName)) {
        groupNames.add(readersGroupName);
      }

    }
    
    if (groupNames.isEmpty()) {
      return sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess;
    }
    
    List<MultiKey> sourceIdSubjectIds = new ArrayList<>(sourceIdSubjectIdsSet);
    
    //process sourceIdSubjectIds in batches of 500
    int batchSize = 500;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(sourceIdSubjectIds, batchSize, false);
    
    for (int i = 0; i < numberOfBatches; i++) {
      
      List<MultiKey> sourceIdSubjectIdsBatch = GrouperUtil.batchList(sourceIdSubjectIds, batchSize, i);
      
      StringBuilder sql = new StringBuilder("""
          select gg.name, gm.subject_source, gm.subject_id
            from grouper_groups gg,  grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_members gm, grouper_fields gf 
            where gg.internal_id  = gscg.group_internal_id 
            and gscm.sql_cache_group_internal_id = gscg.internal_id
            and gm.internal_id = gscm.member_internal_id  
            and gf.internal_id =  gscg.field_internal_id  
            and gf.name = 'members'
            and gg.name in ( 
          """);
      
      GrouperClientUtils.appendQuestions(sql, groupNames.size());
      sql.append(") and (");
      
      GcDbAccess dbAccess = new GcDbAccess();
      
      for (String groupName : groupNames) {
        dbAccess.addBindVar(groupName);
      }
      
      boolean isFirst = true;
      
      for (MultiKey sourceIdSubjectId : sourceIdSubjectIdsBatch) {
        if (isFirst) {
          isFirst = false;
        } else {
          sql.append(" or ");
        }
        sql.append(" (gm.subject_source = ? and gm.subject_id = ?) ");
        dbAccess.addBindVar(sourceIdSubjectId.getKey(0)) // sourceId
          .addBindVar(sourceIdSubjectId.getKey(1)); // subjectId
      }
      sql.append(")");
      
      dbAccess.sql(sql.toString());
      Set<Object[]> groupNamesSubjectSourceSubjectIdFromDb = new HashSet<Object[]>(dbAccess.selectList(Object[].class));
      
      //convert groupNamesSubjectSourceSubjectIdFromDb to multikey
      Set<MultiKey> groupNamesSubjectSourceSubjectIdFromDbSet = new HashSet<MultiKey>();
      for (Object[] row : groupNamesSubjectSourceSubjectIdFromDb) {
        if (row.length == 3) {
          String groupName = (String) row[0];
          String sourceId = (String) row[1];
          String subjectId = (String) row[2];
          groupNamesSubjectSourceSubjectIdFromDbSet.add(new MultiKey(groupName, sourceId, subjectId));
        }
      }
      
      for (MultiKey sourceIdSubjectIdFromSingleBatch : sourceIdSubjectIdsBatch) {
        
        for (GrouperPrivacyRealmConfig grouperPrivacyRealmConfig : this.getPrivacyRealmConfigByConfigId().values()) {
          
          String viewersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmViewersGroupName();
          String updatersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmUpdatersGroupName();
          String readersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmReadersGroupName();
          
          String sourceId = (String)sourceIdSubjectIdFromSingleBatch.getKey(0);
          String subjectId = (String)sourceIdSubjectIdFromSingleBatch.getKey(1);
          
          MultiKey viewersGroupNameSourceSubjectToLookup = new MultiKey(viewersGroupName, sourceId, subjectId);
          
          if (StringUtils.isNotBlank(viewersGroupName)) {
            MultiKey multiKey = new MultiKey(sourceId, subjectId, grouperPrivacyRealmConfig.getConfigId(), "view");
            if (groupNamesSubjectSourceSubjectIdFromDbSet.contains(viewersGroupNameSourceSubjectToLookup)) {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.TRUE);
              
            } else {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.FALSE);
            }
          }
          
          MultiKey updatersGroupNameSourceSubjectToLookup = new MultiKey(updatersGroupName, sourceId, subjectId);
          if (StringUtils.isNotBlank(updatersGroupName)) {
            MultiKey multiKey = new MultiKey(sourceId, subjectId,
                grouperPrivacyRealmConfig.getConfigId(), "update");
            if (groupNamesSubjectSourceSubjectIdFromDbSet.contains(updatersGroupNameSourceSubjectToLookup)) {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.TRUE);
            } else {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.FALSE);
            }
          }
          
          MultiKey readersGroupNameSourceSubjectToLookup = new MultiKey(readersGroupName, sourceId, subjectId);
          if (StringUtils.isNotBlank(readersGroupName)) {
            MultiKey multiKey = new MultiKey(sourceId, subjectId, grouperPrivacyRealmConfig.getConfigId(), "read");
            if (groupNamesSubjectSourceSubjectIdFromDbSet.contains(readersGroupNameSourceSubjectToLookup)) {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.TRUE);
            } else {
              sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.put(multiKey, Boolean.FALSE);
            }
          }
        }
      }
      
    }
    
    synchronized (sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache) {
      // if we are replacing existing cache, then replace it
      if (replaceExisting) {
        sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache.put(Boolean.TRUE,
            sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess);
      } else {
        // otherwise merge it with existing cache
        Map<MultiKey, Boolean> existingCache = sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache.get(Boolean.TRUE);
        if (existingCache != null) {
          existingCache.putAll(sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess);
        } else {
          sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache.put(Boolean.TRUE, sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess);
        }
      }
    }
    
    return sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess;
    
  }
  
  private static long lastTimeSourceIdSubjectIdCacheWasRefreshed = 0L;
  
  private void refreshSourceIdSubjectIdToIfHasUsedPrivacyRecentlyCacheIfNeeded() {
    
    //populate sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache for every subject in sourceIdSubjectIdToIfHasUsedPrivacyRecently cache
    long currentTime = System.currentTimeMillis();
    // only refresh the cache every 2 minutes
    if (currentTime - lastTimeSourceIdSubjectIdCacheWasRefreshed < 120000) {
      return;
    }
    synchronized (sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache) {
      
      if (currentTime - lastTimeSourceIdSubjectIdCacheWasRefreshed < 120000) {
        return;
      }
      
      lastTimeSourceIdSubjectIdCacheWasRefreshed = currentTime;
      populatePrivacyCacheForUser(sourceIdSubjectIdToIfHasUsedPrivacyRecently.keySet(), true);
    }
   
  }
  
  public String calculateHighestLevelAccess(GrouperPrivacyRealmConfig grouperPrivacyRealmConfig, Subject subject) {

    if (grouperPrivacyRealmConfig == null) {
      LOG.warn("grouperPrivacyRealmConfig is null in calculateHighestLevelAccess for subject: " + (subject == null ? null : subject.getId()));
      // if no privacy realm is configured, sysadmins should still see the field
      if (subject != null && PrivilegeHelper.isWheelOrRoot(subject)) {
        return "update";
      }
      if (subject != null && PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
        return "read";
      }
      if (subject != null && PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
        return "view";
      }
      return "";
    }

    Map<MultiKey, Boolean> sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess = sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccessCache.get(Boolean.TRUE);
    
    MultiKey sourceIdSubjectId = new MultiKey(subject.getSourceId(), subject.getId());
    sourceIdSubjectIdToIfHasUsedPrivacyRecently.put(sourceIdSubjectId, Boolean.TRUE);
    
    refreshSourceIdSubjectIdToIfHasUsedPrivacyRecentlyCacheIfNeeded();
   
    String viewersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmViewersGroupName();
    String updatersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmUpdatersGroupName();
    String readersGroupName = grouperPrivacyRealmConfig.getPrivacyRealmReadersGroupName();
    boolean canSysadminsAccess = grouperPrivacyRealmConfig.isPrivacyRealmSysadminsCanView();
    
    String highestLevelAccess = "";
    if (canSysadminsAccess) {
       if (PrivilegeHelper.isWheelOrRoot(subject)) {
         highestLevelAccess = "update";
       } else if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
         highestLevelAccess = "read";
       } else if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
         highestLevelAccess = "view";
       } 
    }
    
    if (!highestLevelAccess.equals("update")) {
      //we need to check access only if it's not highest otherwise what's the point. 
      // user already has the max access
      
      if (StringUtils.isNotBlank(updatersGroupName)) {
        
        MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(), grouperPrivacyRealmConfig.getConfigId(), "update");
        
        Boolean hasAccess = sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess == null ? null : sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey);
        if (hasAccess == null) {
          sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess = populatePrivacyCacheForUser(GrouperUtil.toSet(sourceIdSubjectId), false);
          hasAccess = GrouperUtil.booleanValue(sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey), false);
        }
        
        if (hasAccess) {
          highestLevelAccess = "update";
        }
      }
      
      if ((highestLevelAccess.equals("") || highestLevelAccess.equals("view")) && StringUtils.isNotBlank(readersGroupName)) {
        MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(),
            grouperPrivacyRealmConfig.getConfigId(), "read");

        Boolean hasAccess = sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess == null
            ? null
            : sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey);
        if (hasAccess == null) {
          sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess = populatePrivacyCacheForUser(GrouperUtil.toSet(sourceIdSubjectId), false);
          hasAccess = GrouperUtil.booleanValue(sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey), false);
        }

        if (hasAccess) {
          highestLevelAccess = "read";
        }
      }
      
      if (highestLevelAccess.equals("") && StringUtils.isNotBlank(viewersGroupName)) {
        MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId(),
            grouperPrivacyRealmConfig.getConfigId(), "view");

        Boolean hasAccess = sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess == null
            ? null
            : sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey);
        if (hasAccess == null) {
          sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess = populatePrivacyCacheForUser(GrouperUtil.toSet(sourceIdSubjectId), false);
          hasAccess = GrouperUtil.booleanValue(sourceIdSubjectIdPrivacyRealmConfigIdRoleToHasAccess.get(multiKey), false);
        }

        if (hasAccess) {
          highestLevelAccess = "view";
        }
      }
      
    }
    
    if (!StringUtils.equals(highestLevelAccess, "update") && grouperPrivacyRealmConfig.isPrivacyRealmAuthenticated() && subject != null) {
      if (StringUtils.isBlank(highestLevelAccess) || StringUtils.equals(highestLevelAccess, "view")) {
        highestLevelAccess = "read";
      }
    }
    
    if (!StringUtils.equals(highestLevelAccess, "update") && grouperPrivacyRealmConfig.isPrivacyRealmPublic()) {
      highestLevelAccess = "read";
    }
    
    return highestLevelAccess;
  }
  
  
  public MultiKey retrieveGrouperDataFieldsForDataFieldAndDictionary(Subject subject, String fieldDataAssignableToArg) {
    
    List<GrouperDataFieldConfig> dataFieldConfigs = new ArrayList<>();
    boolean hasAccess = true;

    for (String configId : fieldConfigByConfigId.keySet()) {
      
      GrouperDataFieldConfig dataFieldConfig = fieldConfigByConfigId.get(configId);
      
      String fieldDataAssignableTo = GrouperUtil.defaultIfBlank(dataFieldConfig.getFieldDataAssignableTo(), "individuals");
      
      if (!StringUtils.equals(fieldDataAssignableToArg, fieldDataAssignableTo)) {
        continue;
      }
      
      GrouperDataFieldStructure fieldDataStructure = dataFieldConfig.getFieldDataStructure();
      
      if (fieldDataStructure == null || fieldDataStructure != GrouperDataFieldStructure.rowColumn) {
        
        String grouperPrivacyRealmConfigId = dataFieldConfig.getGrouperPrivacyRealmConfigId();
        GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
        String highestLevelAccess = calculateHighestLevelAccess(grouperPrivacyRealmConfig, subject);
        if (StringUtils.isNotBlank(highestLevelAccess)) {
          dataFieldConfigs.add(dataFieldConfig);
        } else {
          hasAccess = false;
        }
      }
    }
    MultiKey result = new MultiKey(dataFieldConfigs, hasAccess);
    return result;
  }

  
  public List<GrouperDataRowConfig> retrieveGrouperDataRowsForDataFieldAndDictionary(Subject loggedInSubject) {
    
    List<GrouperDataRowConfig> result = new ArrayList<>();
    
    for (String configId : rowConfigByConfigId.keySet()) {
      
      GrouperDataRowConfig dataRowConfig = rowConfigByConfigId.get(configId);
      
      String grouperPrivacyRealmConfigId = dataRowConfig.getPrivacyRealmName();
      GrouperPrivacyRealmConfig grouperPrivacyRealmConfig = getPrivacyRealmConfigByConfigId().get(grouperPrivacyRealmConfigId);
      String highestLevelAccess = calculateHighestLevelAccess(grouperPrivacyRealmConfig, loggedInSubject);
      if (StringUtils.isNotBlank(highestLevelAccess)) {
        result.add(dataRowConfig);
      }
    }
    return result;
  }

}
