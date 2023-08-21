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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

public class GrouperDataEngine {

  private Map<String, Object> debugMap = new LinkedHashMap<>();
  
  
  
  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }

  
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

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
   * provider query configs by config id
   * @return
   */
  public Map<String, Map<String, GrouperDataProviderQueryConfig>> getProviderIdToProviderQueryConfigByConfigId() {
    return providerIdToProviderQueryConfigByConfigId;
  }

  /**
   * provider query configs by config id
   * @return
   */
  public Map<String, GrouperDataProviderQueryConfig> getProviderQueryConfigByConfigId() {
    return providerQueryConfigByConfigId;
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
   * provider query configs by config id
   */
  private Map<String, Map<String, GrouperDataProviderQueryConfig>> providerIdToProviderQueryConfigByConfigId 
    = new HashMap<String, Map<String, GrouperDataProviderQueryConfig>>();
  
  /**
   * provider query configs by config id
   */
  private Map<String, GrouperDataProviderQueryConfig> providerQueryConfigByConfigId 
    = new HashMap<String, GrouperDataProviderQueryConfig>();
  
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
  
  public void loadConfigProviderQueries(GrouperConfig grouperConfig) {
    
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    
    //  # data provider config id
    //  # {valueType: "string", required: true, regex: "^grouperDataProviderQuery\\.[^.]+\\.providerConfigId$", formElement: "dropdown", optionValuesFromClass: "edu.internet2.middleware.grouper.dataField.GrouperDataProvider"}
    //  # grouperDataProviderQuery.dataProviderQueryConfigId.providerConfigId = 

    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataProviderQueryPattern));
    
    for (String queryConfigId : configIdsInConfig) {
      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = new GrouperDataProviderQueryConfig();
      grouperDataProviderQueryConfig.readFromConfig(queryConfigId, grouperConfig );
      
      String providerConfigId = grouperConfig.propertyValueString("grouperDataProviderQuery." + queryConfigId + ".providerConfigId");
      Map<String, GrouperDataProviderQueryConfig> queryConfigIdToQueryConfig = providerIdToProviderQueryConfigByConfigId.get(providerConfigId);
      if (queryConfigIdToQueryConfig == null) {
        queryConfigIdToQueryConfig = new HashMap<>();
        providerIdToProviderQueryConfigByConfigId.put(providerConfigId, queryConfigIdToQueryConfig);
      }
      queryConfigIdToQueryConfig.put(queryConfigId, grouperDataProviderQueryConfig);
      
      if (this.grouperDataProvider != null && StringUtils.equals(providerConfigId, this.grouperDataProvider.getConfigId())) {
        this.providerQueryConfigByConfigId.put(queryConfigId, grouperDataProviderQueryConfig);
      }
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

  /**
   * 
   */
  private GrouperDataProvider grouperDataProvider;
  
  /**
   * 
   * @return
   */
  public GrouperDataProvider getGrouperDataProvider() {
    return grouperDataProvider;
  }
  
  /**
   * 
   * @param dataProviderConfigId
   * @param hib3GrouperLoaderLog
   */
  public static Map<String, Object> loadFull(String dataProviderConfigId, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_" + dataProviderConfigId);
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.DATA_PROVIDER);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
     

    RuntimeException runtimeException = null;
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    Map<String, Object> debugMap = grouperDataEngine.getDebugMap();

    try {
      grouperDataEngine.loadFullHelper(dataProviderConfigId, hib3GrouperLoaderLog);
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);

      synchronized (GrouperDataEngine.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));

          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
    return debugMap;
  }
  
  /**
   * 
   * @param dataProviderConfigId
   * @param hib3GrouperLoaderLog
   */
  public static Map<String, Object> loadIncremental(String dataProviderConfigId, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {

    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("dataProvider_" + dataProviderConfigId);
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.DATA_PROVIDER);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(true);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(true);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
     
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    RuntimeException runtimeException = null;
    
    try {
      // TODO add incremental
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);

      synchronized (GrouperDataEngine.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));

          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    
    return debugMap;
  }

  /**
   * 
   * @param grouperDataProvider
   * @param hib3GrouperLoaderLog
   */
  private void loadFullHelper(String dataProviderConfigId, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    if (StringUtils.isEmpty(dataProviderConfigId)) {
      throw new NullPointerException();
    }
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    this.syncDataProviders(grouperConfig);
    this.syncDataFields(grouperConfig);
    this.syncDataRows(grouperConfig);
    this.syncDataAliases(grouperConfig);

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);

    this.grouperDataProvider = grouperDataProvider;
    this.loadFieldsAndRows(grouperConfig);

    // maybe things in DB arent in sync with the config yet
    if (!this.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      this.debugMap.put("dataProviderConfigNotFound", dataProviderConfigId);
      return;
    }

    // wrapper object for fields, rows, and columns

    // get all dictionary text for field and row assignments for this data provider
    this.grouperDataProviderIndex.getDictionaryTextByInternalId().putAll(GrouperDictionaryDao.selectByDataProvider(grouperDataProvider.getInternalId()));
    
    // get all the members that are assigned in a data provider to fields or rows
    for (Long memberInternalId : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getMember().selectByDataProvider(grouperDataProvider.getInternalId()))) {

      GrouperDataMemberWrapper grouperDataMemberWrapper = new GrouperDataMemberWrapper(this, memberInternalId);
      this.grouperDataProviderIndex.getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
    }
    
    {
      // get field assignments in the database for this provider
      List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
  
      for (GrouperDataFieldAssign grouperDataFieldAssign : grouperDataFieldAssigns) {
        GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = new GrouperDataFieldAssignWrapper(this, grouperDataFieldAssign);
        
        this.grouperDataProviderIndex.getFieldAssignWrapperByInternalId().put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssignWrapper);
        
        grouperDataFieldAssignWrapper.setGrouperDataFieldWrapper(this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(grouperDataFieldAssign.getDataFieldInternalId()));

        grouperDataFieldAssignWrapper.setMemberWrapper(this.grouperDataProviderIndex.getMemberWrapperByInternalId().get(grouperDataFieldAssign.getMemberInternalId()));

        if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
          // TODO fix race conditions here
          String textValue = this.grouperDataProviderIndex.getDictionaryTextByInternalId().get(grouperDataFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
          grouperDataFieldAssignWrapper.setTextValue(textValue);
        }
      }
    }
    
    {
      // get row assignments in the database for this provider
      List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
      
      for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
        GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = new GrouperDataRowAssignWrapper(this, grouperDataRowAssign);
        
        this.grouperDataProviderIndex.getRowAssignWrapperByInternalId().put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper);
        
        grouperDataRowAssignWrapper.setGrouperDataRowWrapper(this.grouperDataProviderIndex
            .getRowWrapperByInternalId().get(grouperDataRowAssign.getDataRowInternalId()));

        grouperDataRowAssignWrapper.setMemberWrapper(this.grouperDataProviderIndex.getMemberWrapperByInternalId().get(grouperDataRowAssign.getMemberInternalId()));
        
      }
    }    

    {
      List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
  
      for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
  
        GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper = new GrouperDataRowFieldAssignWrapper(this, grouperDataRowFieldAssign);
        
        this.grouperDataProviderIndex.getRowFieldAssignWrapperByInternalId().put(grouperDataRowFieldAssign.getInternalId(), grouperDataRowFieldAssignWrapper);
  
        grouperDataRowFieldAssignWrapper.setGrouperDataFieldWrapper(this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(grouperDataRowFieldAssign.getDataFieldInternalId()));
        
        if (grouperDataRowFieldAssign.getValueDictionaryInternalId() != null) {
          // TODO fix race conditions here
          String textValue = this.grouperDataProviderIndex.getDictionaryTextByInternalId().get(grouperDataRowFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataRowFieldAssign.getValueDictionaryInternalId());
          grouperDataRowFieldAssignWrapper.setTextValue(textValue);
        }
  
        grouperDataRowFieldAssignWrapper.setGrouperDataRowAssignWrapper(this.grouperDataProviderIndex
            .getRowAssignWrapperByInternalId().get(grouperDataRowFieldAssign.getDataRowAssignInternalId()));
        
      }
    }
    
    // index attribute assignments by person
    for (GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper  : this.grouperDataProviderIndex.getFieldAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataFieldAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = this.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
      long dataFieldInternalId = grouperDataFieldAssignWrapper.getGrouperDataFieldAssign().getDataFieldInternalId();
      List<GrouperDataFieldAssignWrapper> grouperDataFieldAssignWrappers = grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId);
      
      if (grouperDataFieldAssignWrappers == null) {
        grouperDataFieldAssignWrappers = new ArrayList<>();
        grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().put(dataFieldInternalId, grouperDataFieldAssignWrappers);
      }
      grouperDataFieldAssignWrappers.add(grouperDataFieldAssignWrapper);
    }
    
    // index rows by user
    // index attribute assignments by person
    for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper  : this.grouperDataProviderIndex.getRowAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataRowAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = this.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
      long dataRowInternalId = grouperDataRowAssignWrapper.getGrouperDataRowAssign().getDataRowInternalId();
      List<GrouperDataRowAssignWrapper> grouperDataRowAssignWrappers = grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId().get(dataRowInternalId);

      if (grouperDataRowAssignWrappers == null) {
        grouperDataRowAssignWrappers = new ArrayList<>();
        grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId().put(dataRowInternalId, grouperDataRowAssignWrappers);
      }
      grouperDataRowAssignWrappers.add(grouperDataRowAssignWrapper);
    }
    
    // put the row fields in the rows
    // index attribute assignments by person
    for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper  : this.grouperDataProviderIndex.getRowFieldAssignWrapperByInternalId().values()) {
      
      Long rowAssignId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataRowAssignInternalId();
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = this.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(rowAssignId);
      
      long dataFieldInternalId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataFieldInternalId();
      List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId);
      
      if (grouperDataRowFieldAssignWrappers == null) {
        grouperDataRowFieldAssignWrappers = new ArrayList<>();
        grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().put(dataFieldInternalId, grouperDataRowFieldAssignWrappers);
      }
      grouperDataRowFieldAssignWrappers.add(grouperDataRowFieldAssignWrapper);
    }

    // get the values and index per user
    // remove invalid types
    // type cast by field type
    // have a map of values
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : this.grouperDataProviderIndex.getMemberWrapperByInternalId().values()) {

      for (Long fieldInternalId : grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().keySet()) {
        
        List<GrouperDataFieldAssignWrapper> dataFieldAssignWrappers = GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(fieldInternalId));
        GrouperDataFieldWrapper grouperDataFieldWrapper = this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(fieldInternalId);

        String dataFieldConfigId = grouperDataFieldWrapper.getGrouperDataField().getConfigId();
        GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(dataFieldConfigId);
        
        Set<Object> values = grouperDataMemberWrapper.getFieldIdToValues().get(fieldInternalId);
        
        if (values == null) {
          values = new HashSet<>();
          grouperDataMemberWrapper.getFieldIdToValues().put(fieldInternalId, values);
        }

        HashMap<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = new HashMap<>();
        grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper().put(fieldInternalId, valueToFieldAssignWrapper);
        
        for (GrouperDataFieldAssignWrapper dataFieldAssignWrapper : dataFieldAssignWrappers) {
          Object value = grouperDataFieldConfig.getFieldDataType().convertValue(
              dataFieldAssignWrapper.getGrouperDataFieldAssign().getValueInteger(),
              dataFieldAssignWrapper.getTextValue());
          
          // cant have same value
          if (valueToFieldAssignWrapper.containsKey(value)) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
            hib3GrouperLoaderLog.addDeleteCount(1);
            continue;
          }
          
          if (!grouperDataFieldConfig.isFieldMultiValued() && valueToFieldAssignWrapper.size() >= 1) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
            hib3GrouperLoaderLog.addDeleteCount(1);
            continue;
          }
          values.add(value);
          valueToFieldAssignWrapper.put(value, dataFieldAssignWrapper);
        }
      }
    }
    
    
    
    // from db
    // wrapper object for attributes, rows, and columns
    // map by user to objects
    
    // remove attributes not there
    // remove attributes not assigned
    // add row composite key attributes (to config)
    // match rows
    
    Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();

    Map<String, GrouperDataProviderQueryConfig> queryConfigIdToQueryConfig = this.providerIdToProviderQueryConfigByConfigId.get(grouperDataProvider.getConfigId());
    for (GrouperDataProviderQueryConfig grouperDataProviderQueryConfig : GrouperUtil.nonNull(queryConfigIdToQueryConfig).values()) {
      queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), new HashMap<String, Integer>());
      
      List<Object[]> rows = new ArrayList<Object[]>();
      
      if (grouperDataProviderQueryConfig.getProviderQueryType() == GrouperDataProviderQueryType.sql) {
        rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId()).sql(grouperDataProviderQueryConfig.getProviderQuerySqlQuery()).selectList(Object[].class));
        GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId(), grouperDataProviderQueryConfig.getProviderQuerySqlQuery());
  
        this.getQueryConfigIdToTableMetadata().put(grouperDataProviderQueryConfig.getConfigId(), tableMetadata);
        
        List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
        for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
          queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
        }
      } else if (grouperDataProviderQueryConfig.getProviderQueryType() == GrouperDataProviderQueryType.ldap) {
        List<String> ldapAttributes = new ArrayList<String>();
        for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs()) {
          ldapAttributes.add(grouperDataProviderQueryFieldConfig.getProviderDataFieldAttribute());
        }
        
        if (ldapAttributes.size() == 0) {
          //??
          continue;
        }
        
        if (!ldapAttributes.contains(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute())) {
          ldapAttributes.add(grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute());
        }
        
        for (int i = 0; i < ldapAttributes.size(); i++) {
          queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).put(ldapAttributes.get(i).toLowerCase(), i);
        }
        
        List<LdapEntry> ldapEntries = LdapSessionUtils.ldapSession().list(grouperDataProviderQueryConfig.getProviderQueryLdapConfigId(), grouperDataProviderQueryConfig.getProviderQueryLdapBaseDn(), LdapSearchScope.valueOfIgnoreCase(grouperDataProviderQueryConfig.getProviderQueryLdapSearchScope(), true), grouperDataProviderQueryConfig.getProviderQueryLdapFilter(), ldapAttributes.toArray(new String[0]), null);
        for (LdapEntry ldapEntry : ldapEntries) {
          Object[] row = new Object[ldapAttributes.size()];
          
          for (int i = 0; i < ldapAttributes.size(); i++) {
            Object value = null;

            String ldapAttributeString = ldapAttributes.get(i);
            LdapAttribute ldapAttribute = ldapEntry.getAttribute(ldapAttributeString);
            if (ldapAttribute != null && ldapAttribute.getStringValues().size() > 0) {
              if (ldapAttribute.getStringValues().size() == 1) {
                value = ldapAttribute.getStringValues().iterator().next();
              } else {
                value = new HashSet<String>(ldapAttribute.getStringValues()); 
              }
            }
            
            row[i] = value;
          }
          
          rows.add(row);
        }
      } else {
        throw new RuntimeException("Unexpected providerQueryType for " + grouperDataProviderQueryConfig.getConfigId() + ": " + grouperDataProviderQueryConfig.getProviderQueryType());
      }

      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      Integer subjectIdZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceIdAttribute, true);
        
        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

        Long memberInternalId = member.getInternalId();
        
        GrouperDataMemberWrapper grouperDataMemberWrapper = this.grouperDataProviderIndex.getMemberWrapperByInternalId().get(memberInternalId);
        
        if (grouperDataMemberWrapper == null) {
          grouperDataMemberWrapper = new GrouperDataMemberWrapper(this, memberInternalId);
          this.grouperDataProviderIndex.getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
        }

        List<Object[]> userRowsforQuery = grouperDataMemberWrapper.getQueryConfigIdToRowData().get(grouperDataProviderQueryConfig.getConfigId());
        if (userRowsforQuery == null) {
          userRowsforQuery = new ArrayList<Object[]>();
          grouperDataMemberWrapper.getQueryConfigIdToRowData().put(grouperDataProviderQueryConfig.getConfigId(), userRowsforQuery);
        }
        
        userRowsforQuery.add(row);
      }
      
    }
    
    // go through each user, index and convert the data
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : this.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {
      
      // go through each query
      for (GrouperDataProviderQueryConfig grouperDataProviderQueryConfig : GrouperUtil.nonNull(this.providerQueryConfigByConfigId).values()) {
        
        String queryConfigId = grouperDataProviderQueryConfig.getConfigId();
        
        List<Object[]> providerRows = GrouperUtil.nonNull(grouperDataMemberWrapper.getQueryConfigIdToRowData().get(queryConfigId));
        
        String rowConfigId = grouperDataProviderQueryConfig.getProviderQueryRowConfigId();

        //GrouperDataFieldConfig grouperDataRowConfig = null;
        
        GrouperDataRowWrapper grouperDataRowWrapper = null;
        List<Map<Long, List<Object>>> rowsOfFieldInternalIdToValues = null;
        
        // if this is a direct assignment
        if (!StringUtils.isBlank(rowConfigId)) {
          
          //grouperDataRowConfig = this.fieldConfigByConfigId.get(rowConfigId);
          
          // if this is a row assignment
          grouperDataRowWrapper = this.grouperDataProviderIndex.getRowWrapperByConfigId().get(rowConfigId);
          
          rowsOfFieldInternalIdToValues = grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().get(grouperDataRowWrapper.getGrouperDataRow().getInternalId());
          if (rowsOfFieldInternalIdToValues == null) {
            rowsOfFieldInternalIdToValues = new ArrayList<>();
            grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().put(grouperDataRowWrapper.getGrouperDataRow().getInternalId(), rowsOfFieldInternalIdToValues);
          }
        }
        

        List<GrouperDataProviderQueryFieldConfig> grouperDataProviderQueryFieldConfigs =
            grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs();

        for (Object[] row : providerRows) {

          Map<Long, List<Object>> rowDataFieldInternalIdToValues = null;
          
          if (!StringUtils.isBlank(rowConfigId)) {
            rowDataFieldInternalIdToValues = new HashMap<>();
            rowsOfFieldInternalIdToValues.add(rowDataFieldInternalIdToValues);
          }

          for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : GrouperUtil.nonNull(grouperDataProviderQueryFieldConfigs)) {
          
            GrouperDataProviderQueryFieldMappingType providerDataFieldMappingType = 
                grouperDataProviderQueryFieldConfig.getProviderDataFieldMappingType();
            
            // could be the subject attribute?
            if (providerDataFieldMappingType == null) {
              continue;
            }
            
            // this is really the only option right now
            if (providerDataFieldMappingType == GrouperDataProviderQueryFieldMappingType.attribute) {
              
              String columnName = grouperDataProviderQueryFieldConfig.getProviderDataFieldAttribute();
              String dataFieldConfigId = grouperDataProviderQueryFieldConfig.getProviderDataFieldConfigId();
  
              GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(dataFieldConfigId);
              
              GrouperDataFieldWrapper grouperDataFieldWrapper = this.grouperDataProviderIndex.getFieldWrapperByConfigId().get(dataFieldConfigId);
                
              Integer rowIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(columnName.toLowerCase());
              if (rowIndex == null) {
                throw new RuntimeException("Unable to find index for configId=" + grouperDataProviderQueryConfig.getConfigId() + ", columnName=" + columnName.toLowerCase());
              }
              
              Object value = row[rowIndex];
              
              if (value instanceof Set) {
                if (((Set)value).size() > 0) {
                  List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (data == null) {
                    data = new ArrayList<>();
                    grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
                  }
                  
                  for (Object currentValue : (Set)value) {
                    currentValue = grouperDataFieldConfig.getFieldDataType().convertValue(currentValue);
                    
                    if (currentValue != null && currentValue != Void.TYPE) {
                      data.add(currentValue);
                    }
                  }
                }
              } else {
                value = grouperDataFieldConfig.getFieldDataType().convertValue(value);
                
                // if this is a direct assignment
                if (StringUtils.isBlank(rowConfigId)) {

                  List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (data == null) {
                    data = new ArrayList<>();
                    grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
                  }
                  
                  if (value != null && value != Void.TYPE) {
                    data.add(value);
                  }
                } else {
                  // if this is a row
                  List<Object> values = rowDataFieldInternalIdToValues.get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (values == null) {
                    values = new ArrayList<>();
                    rowDataFieldInternalIdToValues.put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), values);
                  }
                  values.add(value);
                } 
              }
            }
              
          }          
        }

      }

      {
        // change the database for fields
        // go through each dataFieldConfigId where there is provider or grouper data
        Set<Long> dataFieldInternalIds = new HashSet<Long>();
        dataFieldInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().keySet());
        dataFieldInternalIds.addAll(grouperDataMemberWrapper.getFieldIdToValues().keySet());
        Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper();
        
        for (Long dataFieldInternalId : dataFieldInternalIds) {
  
          GrouperDataField grouperDataField = this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
          GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(grouperDataField.getConfigId());

          if (grouperDataFieldConfig.getFieldDataStructure() == GrouperDataFieldStructure.attribute) {
            Map<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = GrouperUtil.nonNull(fieldIdToValueToFieldAssignWrapper.get(dataFieldInternalId));
            
            Set<Object> dataFromProvider = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(dataFieldInternalId)));
            Set<Object> dataFromGrouper = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldIdToValues().get(dataFieldInternalId)));
            
            Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
            dataToDelete.removeAll(dataFromProvider);
            
            for (Object value : dataToDelete) {
              GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);
              GrouperDataFieldAssignDao.delete(grouperDataFieldAssignWrapper.getGrouperDataFieldAssign());
              hib3GrouperLoaderLog.addDeleteCount(1);
            }
            
            Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
            dataToInsert.removeAll(dataFromGrouper);
    
            
            for (Object value : dataToInsert) {
              GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
              grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
              grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
              grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
              grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value);
              GrouperDataFieldAssignDao.store(grouperDataFieldAssign);
              hib3GrouperLoaderLog.addInsertCount(1);
            }
          }
        }
      }

      { 
        //change database for rows
        Set<Long> dataRowInternalIds = new HashSet<Long>();
        dataRowInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().keySet());
        Map<Long, List<GrouperDataRowAssignWrapper>> rowAssignWrappersByRowInternalId = grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId();
        dataRowInternalIds.addAll(GrouperUtil.nonNull(rowAssignWrappersByRowInternalId).keySet());
        
        // go through each row id: (dataRowInternalIds is null)
        for (Long dataRowInternalId : dataRowInternalIds) {
          
          GrouperDataRow grouperDataRow = this.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(dataRowInternalId).getGrouperDataRow();
          GrouperDataRowConfig grouperDataRowConfig = this.rowConfigByConfigId.get(grouperDataRow.getConfigId());
          List<GrouperDataRowAssignWrapper> grouperDataRowAssignWrappers = GrouperUtil.nonNull(rowAssignWrappersByRowInternalId.get(dataRowInternalId));
          Map<MultiKey, GrouperDataRowAssignWrapper> grouperDataRowKeyToRowAssignWrapper = new HashMap<>();
          for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper : GrouperUtil.nonNull(grouperDataRowAssignWrappers)) {
            MultiKey rowKey = grouperDataRowAssignWrapper.rowKey();
            grouperDataRowKeyToRowAssignWrapper.put(rowKey, grouperDataRowAssignWrapper);
          }
          
          List<Map<Long, List<Object>>> providerRowsOfDataFieldInternalIdToListOfValues = GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().get(dataRowInternalId));
          Map<MultiKey, Map<Long, List<Object>>> providerDataRowKeyToDataFieldInternalIdsAndValues = new HashMap<>();
          for (Map<Long, List<Object>> providerDataFieldInternalIdToValues : GrouperUtil.nonNull(providerRowsOfDataFieldInternalIdToListOfValues)) {
            Object[] keyValues = new Object[GrouperUtil.length(grouperDataRowConfig.getRowKeyFieldConfigIds())];
            int i = 0;

            for (String rowKeyFieldConfigId : grouperDataRowConfig.getRowKeyFieldConfigIds()) {

                GrouperDataFieldConfig grouperDataFieldConfig = this.getFieldConfigByConfigId().get(rowKeyFieldConfigId);
                GrouperDataField grouperDataField = this.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(rowKeyFieldConfigId).getGrouperDataField();
                List<Object> values = providerDataFieldInternalIdToValues.get(grouperDataField.getInternalId());

                if (GrouperUtil.length(values) != 1) {
                  throw new RuntimeException("Provider row field key must have one value: " + grouperDataRowConfig.getConfigId() 
                    + ", field: " + grouperDataFieldConfig.getConfigId() + ", " + GrouperUtil.stringValue(values));
                }

                keyValues[i] = grouperDataFieldConfig.getFieldDataType().convertValue(values.iterator().next());

                GrouperUtil.assertion(keyValues[i] != null && keyValues[i] != Void.TYPE, 
                    "Data row field key must not have a null value: " + grouperDataRowConfig.getConfigId() 
                    + ", rowAssignId: " + grouperDataRow.getInternalId() + ", field: " + grouperDataFieldConfig.getConfigId());
                i++;
              }
              MultiKey rowKey = new MultiKey(keyValues);
              providerDataRowKeyToDataFieldInternalIdsAndValues.put(rowKey, providerDataFieldInternalIdToValues);
            }
          
            Set<MultiKey> rowKeyFieldsToDeletes = new HashSet<>(grouperDataRowKeyToRowAssignWrapper.keySet());
            rowKeyFieldsToDeletes.removeAll(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
            
            for (MultiKey rowKeyFieldsToDelete : rowKeyFieldsToDeletes) {
              GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(rowKeyFieldsToDelete);
              for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {
                for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                  GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign());
                }
              }
              
              GrouperDataRowAssignDao.delete(grouperDataRowAssignWrapper.getGrouperDataRowAssign());
              hib3GrouperLoaderLog.addDeleteCount(1);
            }

            Set<MultiKey> rowKeyFieldsToInserts = new HashSet<>(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
            rowKeyFieldsToInserts.removeAll(grouperDataRowKeyToRowAssignWrapper.keySet());
    
            
            for (MultiKey rowKeyFieldsToInsert : rowKeyFieldsToInserts) {
              GrouperDataRowAssign grouperDataRowAssign = new GrouperDataRowAssign();
              grouperDataRowAssign.setDataRowInternalId(dataRowInternalId);
              grouperDataRowAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
              grouperDataRowAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
              GrouperDataRowAssignDao.store(grouperDataRowAssign);
              
              Map<Long, List<Object>> dataFieldInternalIdToValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(rowKeyFieldsToInsert);
              for (Long dataFieldInternalId : GrouperUtil.nonNull(dataFieldInternalIdToValues.keySet())) {

                GrouperDataField grouperDataField = this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
                GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(grouperDataField.getConfigId());

                List<Object> values = dataFieldInternalIdToValues.get(dataFieldInternalId);
                for (Object value : values) {
                  GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                  grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                  grouperDataRowFieldAssign.setDataRowAssignInternalId(grouperDataRowAssign.getInternalId());
                  grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, value);

                  GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssign);
                  
                }
              }
              
              hib3GrouperLoaderLog.addInsertCount(1);
            }

            // do the updates
            for (MultiKey grouperDataRowKey : grouperDataRowKeyToRowAssignWrapper.keySet()) {
              if (providerDataRowKeyToDataFieldInternalIdsAndValues.containsKey(grouperDataRowKey)) {
                GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(grouperDataRowKey);
                Map<Long, List<Object>> providerDataFieldInternalIdsAndValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(grouperDataRowKey);
                
                for (Long dataFieldInternalId : GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.keySet())) {

                  GrouperDataField grouperDataField = this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
                  GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(grouperDataField.getConfigId());

                  List<Object> providerValues = providerDataFieldInternalIdsAndValues.get(dataFieldInternalId);
                  List<Object> grouperValuesConverted = new ArrayList<Object>();
                  List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = GrouperUtil.nonNull(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId));
                  for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                    Object grouperValueConverted = grouperDataFieldConfig.getFieldDataType().convertValue(
                        grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueInteger(),
                        grouperDataRowFieldAssignWrapper.getTextValue());
                    if (providerValues.contains(grouperValueConverted)) {
                      grouperValuesConverted.add(grouperValueConverted);
                    } else {
                      GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign());
                      hib3GrouperLoaderLog.addDeleteCount(1);
                    }
                  }
                  
                  Set<Object> valuesToAdd = new HashSet<Object>(providerValues);
                  valuesToAdd.removeAll(grouperValuesConverted);
                  
                  for (Object valueToAdd : valuesToAdd) {
                    if (valueToAdd != null && valueToAdd != Void.TYPE) {
                      GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                      grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                      grouperDataRowFieldAssign.setDataRowAssignInternalId(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getInternalId());
                      grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, valueToAdd);
                      GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssign);
                      hib3GrouperLoaderLog.addInsertCount(1);
                    }
                  }
                }
              }
            }
          }

          
        }

//      {
//        // change the database for rows
//        // go through each dataFieldConfigId where there is provider or grouper data
//        Set<Long> dataFieldInternalIds = new HashSet<Long>();
//        dataFieldInternalIds.addAll(grouperDataMemberWrapper.get
//        dataFieldInternalIds.addAll(grouperDataMemberWrapper.getFieldIdToValues().keySet());
//        Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper();
//        
//        for (Long dataFieldInternalId : dataFieldInternalIds) {
//  
//          GrouperDataField grouperDataField = this.grouperDataProviderIndex.getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
//          
//          Map<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = GrouperUtil.nonNull(fieldIdToValueToFieldAssignWrapper.get(dataFieldInternalId));
//          
//          Set<Object> dataFromProvider = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(dataFieldInternalId)));
//          Set<Object> dataFromGrouper = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldIdToValues().get(dataFieldInternalId)));
//          
//          Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
//          dataToDelete.removeAll(dataFromProvider);
//          
//          for (Object value : dataToDelete) {
//            GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);
//            GrouperDataFieldAssignDao.delete(grouperDataFieldAssignWrapper.getGrouperDataFieldAssign());
//  
//          }
//          
//          Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
//          dataToInsert.removeAll(dataFromGrouper);
//  
//          GrouperDataFieldConfig grouperDataFieldConfig = this.fieldConfigByConfigId.get(grouperDataField.getConfigId());
//          
//          for (Object value : dataToInsert) {
//            GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
//            grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
//            grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
//            grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
//            grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value);
//            GrouperDataFieldAssignDao.store(grouperDataFieldAssign);
//          }
//          
//        }
//      }
      
      
    }
    
  }

  public void loadFieldsAndRows(GrouperConfig grouperConfig) {
    if (grouperConfig == null) {
      grouperConfig = GrouperConfig.retrieveConfig();
    }
    // load config from config file
    this.loadConfigFields(grouperConfig);
    this.loadConfigProviders(grouperConfig);

    this.loadConfigProviderQueries(grouperConfig);
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
