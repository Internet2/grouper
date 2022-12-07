package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.subject.Subject;

public class GrouperDataEngine {

  /**
   * data field
   */
  private static Pattern dataFieldPattern = Pattern.compile("^grouperDataField\\.([^.]+)\\.fieldAliases$");
  
  /**
   * data provider
   */
  private static Pattern dataProviderPattern = Pattern.compile("^grouperDataProvider\\.([^.]+)\\.name$");
  
  /**
   * data provider
   */
  private static Pattern dataRowPattern = Pattern.compile("^grouperDataRow\\.([^.]+)\\.rowAliases$");
  
  /**
   * data provider query
   */
  private static Pattern dataProviderQueryPattern = Pattern.compile("^grouperDataProviderQuery\\.([^.]+)\\.providerConfigId$");
  
  /**
   * field configs by config id
   */
  private Map<String, GrouperDataFieldConfig> fieldConfigByConfigId = new HashMap<String, GrouperDataFieldConfig>();
  
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
   * prviders by config id
   */
  private Map<String, GrouperDataProviderConfig> providerConfigByConfigId = new HashMap<String, GrouperDataProviderConfig>();
  
  
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
   * @param grouperDataProvider
   */
  public static void loadFull(GrouperDataProvider grouperDataProvider) {
    if (grouperDataProvider == null) {
      throw new NullPointerException();
    }
    
    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    
    grouperDataEngine.grouperDataProvider = grouperDataProvider;
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    
    // load config from config file
    grouperDataEngine.loadConfigFields(grouperConfig);
    grouperDataEngine.loadConfigProviders(grouperConfig);
    grouperDataEngine.loadConfigProviderQueries(grouperConfig);
    
    // retrieve all fields and rows (definitions)
    grouperDataEngine.grouperDataProviderData.setGrouperDataFields(GrouperUtil.nonNull(GrouperDataFieldDao.selectAll()));
    grouperDataEngine.grouperDataProviderData.setGrouperDataRows(GrouperUtil.nonNull(GrouperDataRowDao.selectAll()));
    
    // index those
    {
      for (GrouperDataField grouperDataField : grouperDataEngine.grouperDataProviderData.getGrouperDataFields()) {
        GrouperDataFieldWrapper grouperDataFieldWrapper = new GrouperDataFieldWrapper(grouperDataEngine, grouperDataField);
        grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByInternalId().put(grouperDataField.getInternalId(), grouperDataFieldWrapper);
        grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByConfigId().put(grouperDataField.getConfigId(), grouperDataFieldWrapper);
      }
    }

    {
      Map<Long, GrouperDataRow> dataRowInternalIdToRow = new HashMap<Long, GrouperDataRow>();
  
      for (GrouperDataRow grouperDataRow : grouperDataEngine.grouperDataProviderData.getGrouperDataRows()) {
        GrouperDataRowWrapper grouperDataRowWrapper = new GrouperDataRowWrapper(grouperDataEngine, grouperDataRow);
        grouperDataEngine.grouperDataProviderIndex.getRowWrapperByInternalId().put(grouperDataRow.getInternalId(), grouperDataRowWrapper);
      }
    }
    
    // wrapper object for fields, rows, and columns

    grouperDataEngine.grouperDataProviderIndex.getDictionaryTextByInternalId().putAll(GrouperDictionaryDao.selectByDataProvider(grouperDataProvider.getInternalId()));
    
    for (Long memberInternalId : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getMember().selectByDataProvider(grouperDataProvider.getInternalId()))) {
      GrouperDataMemberWrapper grouperDataMemberWrapper = new GrouperDataMemberWrapper(grouperDataEngine, memberInternalId);
      grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
    }
    
    {
      // get field assignments for this provider
      List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
  
      for (GrouperDataFieldAssign grouperDataFieldAssign : grouperDataFieldAssigns) {
        GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = new GrouperDataFieldAssignWrapper(grouperDataEngine, grouperDataFieldAssign);
        
        grouperDataEngine.grouperDataProviderData.getGrouperDataFieldAssignWrappers().add(grouperDataFieldAssignWrapper);
        
        grouperDataEngine.grouperDataProviderIndex.getFieldAssignWrapperByInternalId().put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssignWrapper);
        
        grouperDataFieldAssignWrapper.setGrouperDataFieldWrapper(grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByInternalId().get(grouperDataFieldAssign.getDataFieldInternalId()));

        grouperDataFieldAssignWrapper.setMemberWrapper(grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().get(grouperDataFieldAssign.getMemberInternalId()));

        if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
          String textValue = grouperDataEngine.grouperDataProviderIndex.getDictionaryTextByInternalId().get(grouperDataFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isNotBlank(textValue), "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
          grouperDataFieldAssignWrapper.setTextValue(textValue);
        }
      }
    }
    
    {
      // get row assignments
      List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
      
      for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
        GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = new GrouperDataRowAssignWrapper(grouperDataEngine, grouperDataRowAssign);
        grouperDataEngine.grouperDataProviderIndex.getRowAssignWrapperByInternalId().put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper);
        
        grouperDataRowAssignWrapper.setGrouperDataRowWrapper(grouperDataEngine.grouperDataProviderIndex
            .getRowWrapperByInternalId().get(grouperDataRowAssign.getDataRowInternalId()));

        grouperDataRowAssignWrapper.setMemberWrapper(grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().get(grouperDataRowAssign.getMemberInternalId()));
        
      }
    }    

    {
      List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
  
      for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
  
        GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper = new GrouperDataRowFieldAssignWrapper(grouperDataEngine, grouperDataRowFieldAssign);
        
        grouperDataEngine.grouperDataProviderIndex.getRowFieldAssignWrapperByInternalId().put(grouperDataRowFieldAssign.getInternalId(), grouperDataRowFieldAssignWrapper);
  
        grouperDataRowFieldAssignWrapper.setGrouperDataFieldWrapper(grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByInternalId().get(grouperDataRowFieldAssign.getDataFieldInternalId()));
        
        if (grouperDataRowFieldAssign.getValueDictionaryInternalId() != null) {
          String textValue = grouperDataEngine.grouperDataProviderIndex.getDictionaryTextByInternalId().get(grouperDataRowFieldAssign.getValueDictionaryInternalId());
          GrouperUtil.assertion(!StringUtils.isNotBlank(textValue), "Cant find text: " + grouperDataRowFieldAssign.getValueDictionaryInternalId());
          grouperDataRowFieldAssignWrapper.setTextValue(textValue);
        }
  
        grouperDataRowFieldAssignWrapper.setGrouperDataRowAssignWrapper(grouperDataEngine.grouperDataProviderIndex
            .getRowAssignWrapperByInternalId().get(grouperDataRowFieldAssign.getDataRowAssignInternalId()));
        
      }
    }
    
    // index attribute assignments by person
    for (GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper  : grouperDataEngine.grouperDataProviderIndex.getFieldAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataFieldAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = grouperDataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
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
    for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper  : grouperDataEngine.grouperDataProviderIndex.getRowAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataRowAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = grouperDataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
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
    for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper  : grouperDataEngine.grouperDataProviderIndex.getRowFieldAssignWrapperByInternalId().values()) {
      
      Long rowFieldAssignId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getInternalId();
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(rowFieldAssignId);
      
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
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().values()) {

      for (Long fieldInternalId : grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().keySet()) {
        
        List<GrouperDataFieldAssignWrapper> dataFieldAssignWrappers = GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(fieldInternalId));
        GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByInternalId().get(fieldInternalId);

        String dataFieldConfigId = grouperDataFieldWrapper.getGrouperDataField().getConfigId();
        GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.fieldConfigByConfigId.get(dataFieldConfigId);
        
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
            continue;
          }
          
          if (!grouperDataFieldConfig.isFieldMultiValued() && valueToFieldAssignWrapper.size() >= 1) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
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
    
    Map<String, GrouperDataProviderQueryConfig> queryConfigIdToQueryConfig = grouperDataEngine.providerIdToProviderQueryConfigByConfigId.get(grouperDataProvider.getConfigId());
    for (GrouperDataProviderQueryConfig grouperDataProviderQueryConfig : GrouperUtil.nonNull(queryConfigIdToQueryConfig).values()) {
      
      List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId()).sql(grouperDataProviderQueryConfig.getProviderQuerySqlQuery()).selectList(Object[].class));
      GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(grouperDataProviderQueryConfig.getProviderQuerySqlConfigId(), grouperDataProviderQueryConfig.getProviderQuerySqlQuery());

      grouperDataEngine.getQueryConfigIdToTableMetadata().put(grouperDataProviderQueryConfig.getConfigId(), tableMetadata);
      
      List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
        lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
      }

      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      GcTableSyncColumnMetadata gcTableSyncColumnMetadata = columnMetadatas.get(subjectIdZeroIndex);

      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceIdAttribute, true);
        
        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

        Long memberInternalId = member.getInternalId();
        
        GrouperDataMemberWrapper grouperDataMemberWrapper = grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().get(memberInternalId);
        
        if (grouperDataMemberWrapper == null) {
          grouperDataMemberWrapper = new GrouperDataMemberWrapper(grouperDataEngine, memberInternalId);
          grouperDataEngine.grouperDataProviderIndex.getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
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
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : grouperDataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {
      
      for (GrouperDataProviderQueryConfig grouperDataProviderQueryConfig : GrouperUtil.nonNull(grouperDataEngine.providerQueryConfigByConfigId).values()) {
        
        String queryConfigId = grouperDataProviderQueryConfig.getConfigId();
        
        GcTableSyncTableMetadata gcTableSyncTableMetadata = grouperDataEngine
            .getQueryConfigIdToTableMetadata().get(queryConfigId);
        
        List<Object[]> providerRows = GrouperUtil.nonNull(grouperDataMemberWrapper.getQueryConfigIdToRowData().get(queryConfigId));
        
        List<GrouperDataProviderQueryFieldConfig> grouperDataProviderQueryFieldConfigs =
            grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs();
        
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

            GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.fieldConfigByConfigId.get(dataFieldConfigId);
            
            GrouperDataFieldWrapper grouperDataFieldWrapper = grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByConfigId().get(dataFieldConfigId);
            
            GcTableSyncColumnMetadata gcTableSyncColumnMetadata = gcTableSyncTableMetadata.lookupColumn(columnName, true);

            for (Object[] row : providerRows) {
              
              Object value = row[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()];
              
              value = grouperDataFieldConfig.getFieldDataType().convertValue(value);
              
              List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
              if (data == null) {
                data = new ArrayList<>();
                grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
              }
              data.add(value);
            }
            
          }
          
        }
        
      }
      
      // change the database
      // go through each dataFieldConfigId where there is provider or grouper data
      Set<Long> dataFieldInternalIds = new HashSet<Long>();
      dataFieldInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().keySet());
      dataFieldInternalIds.addAll(grouperDataMemberWrapper.getFieldIdToValues().keySet());
      Map<Long, Map<Object, GrouperDataFieldAssignWrapper>> fieldIdToValueToFieldAssignWrapper = grouperDataMemberWrapper.getFieldIdToValueToFieldAssignWrapper();
      
      for (Long dataFieldInternalId : dataFieldInternalIds) {

        GrouperDataField grouperDataField = grouperDataEngine.grouperDataProviderIndex.getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
        
        Map<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = GrouperUtil.nonNull(fieldIdToValueToFieldAssignWrapper.get(dataFieldInternalId));
        
        Set<Object> dataFromProvider = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(dataFieldInternalId)));
        Set<Object> dataFromGrouper = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldIdToValues().get(dataFieldInternalId)));
        
        Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
        dataToDelete.removeAll(dataFromProvider);
        
        for (Object value : dataToDelete) {
          GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);
          GrouperDataFieldAssignDao.delete(grouperDataFieldAssignWrapper.getGrouperDataFieldAssign());

        }
        
        Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
        dataToInsert.removeAll(dataFromGrouper);

        GrouperDataFieldConfig grouperDataFieldConfig = grouperDataEngine.fieldConfigByConfigId.get(grouperDataField.getConfigId());
        
        for (Object value : dataToInsert) {
          GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
          grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
          grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
          grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
          grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value);
          GrouperDataFieldAssignDao.store(grouperDataFieldAssign);
        }
        
      }
    }
    
  }

  private Map<String, GcTableSyncTableMetadata> queryConfigIdToTableMetadata = new HashMap<>();


  
  public Map<String, GcTableSyncTableMetadata> getQueryConfigIdToTableMetadata() {
    return queryConfigIdToTableMetadata;
  }
  
  
}
