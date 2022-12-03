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
   * take data fields and make sure the have an internal id
   */
  public static void syncDataFields() {
    
    //  # aliases that this field is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
    //  # grouperDataField.dataFieldConfigId.fieldAliases = 
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
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
  public static void syncDataRows() {
    
    //  # aliases that this row is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataRow\\.[^.]+\\.rowAliases$"}
    //  # grouperDataRow.dataRowConfigId.rowAliases = 
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
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
  public static void syncDataAliases() {
    
    //  # aliases that this field is referred to as
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataField\\.[^.]+\\.fieldAliases$"}
    //  # grouperDataField.dataFieldConfigId.fieldAliases = 
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
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

  public static void syncDataProviders() {

    //  # data provider name, not really needed or used, but there to setup the provider
    //  # {valueType: "string", required: true, multiple: true, regex: "^grouperDataProvider\\.[^.]+\\.name$"}
    //  # grouperDataProvider.dataProviderConfigId.name = 

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
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

  /**
   * 
   * @param grouperDataProvider
   */
  public static void loadFull(GrouperDataProvider grouperDataProvider) {
    if (grouperDataProvider == null) {
      throw new NullPointerException();
    }
    
    // retrieve all fields and rows (definitions)
    List<GrouperDataField> grouperDataFields = GrouperUtil.nonNull(GrouperDataFieldDao.selectAll());
    List<GrouperDataRow> grouperDataRows = GrouperUtil.nonNull(GrouperDataRowDao.selectAll());

    Map<Long, GrouperDataField> internalIdToField = new HashMap<Long, GrouperDataField>();

    for (GrouperDataField grouperDataField : grouperDataFields) {
      internalIdToField.put(grouperDataField.getInternalId(), grouperDataField);
    }
    
    Map<Long, GrouperDataRow> internalIdToRow = new HashMap<Long, GrouperDataRow>();

    for (GrouperDataRow grouperDataRow : grouperDataRows) {
      internalIdToRow.put(grouperDataRow.getInternalId(), grouperDataRow);
    }
    
    Map<Long, String> dictionaryInternalIdToText = GrouperDictionaryDao.selectByDataProvider(grouperDataProvider.getInternalId());
    
    
    // get field assignments for this provider
    List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
    List<GrouperDataFieldAssignWrapper> grouperDataFieldAssignWrappers = new ArrayList<GrouperDataFieldAssignWrapper>();

    for (GrouperDataFieldAssign grouperDataFieldAssign : grouperDataFieldAssigns) {
      GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = new GrouperDataFieldAssignWrapper();
      grouperDataFieldAssignWrapper.setGrouperDataField(internalIdToField.get(grouperDataFieldAssign.getDataFieldInternalId()));
      if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
        String textValue = dictionaryInternalIdToText.get(grouperDataFieldAssign.getValueDictionaryInternalId());
        GrouperUtil.assertion(!StringUtils.isNotBlank(textValue), "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
        grouperDataFieldAssignWrapper.setTextValue(textValue);
      }
    }
    
    List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperDataRowAssignDao.selectByProvider(grouperDataProvider.getInternalId());
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperDataRowFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
    
    
    // wrapper object for fields, rows, and columns
    // index by person
    // remove invalid types
    // type cast by field type
    // have a map of values
    
    
    // from db
    // wrapper object for attributes, rows, and columns
    // map by user to objects
    
    // remove attributes not there
    // remove attributes not assigned
    // add row composite key attributes (to config)
    // match rows
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(dataProviderQueryPattern));

    for (String configId : configIdsInConfig) {
      
      String providerConfigId = grouperConfig.propertyValueString("grouperDataProviderQuery." + configId + ".providerConfigId");
      if (!StringUtils.equals(providerConfigId, grouperDataProvider.getConfigId())) {
        
        continue;
        
      }
      
      GrouperDataProviderQuery grouperDataProviderQuery = GrouperDataProviderQuery.parseFromConfig(configId, grouperConfig);
      
      List<Object[]> rows = GrouperUtil.nonNull(new GcDbAccess().connectionName(grouperDataProviderQuery.getProviderQuerySqlConfigId()).sql(grouperDataProviderQuery.getProviderQuerySqlQuery()).selectList(Object[].class));
      GcTableSyncTableMetadata tableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromCacheOrDatabase(configId, grouperDataProviderQuery.getProviderQuerySqlQuery());

      List<GcTableSyncColumnMetadata> columnMetadatas = tableMetadata.getColumnMetadata();
      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      for (GcTableSyncColumnMetadata columnMetadata : columnMetadatas ) {
        lowerColumnNameToZeroIndex.put(columnMetadata.getColumnName().toLowerCase(), columnMetadata.getColumnIndexZeroIndexed());
      }

      Map<Long, List<Object[]>> subjectIdToRows = new HashMap<Long, List<Object[]>>();

      String subjectIdAttribute = grouperDataProviderQuery.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQuery.getProviderQuerySubjectSourceId();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      GcTableSyncColumnMetadata gcTableSyncColumnMetadata = columnMetadatas.get(subjectIdZeroIndex);

      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceIdAttribute, true);
        
        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);

        Long memberInternalId = member.getInternalId();
        
        List<Object[]> usersRows = subjectIdToRows.get(memberInternalId);
        
        if (usersRows == null) {
          usersRows = new ArrayList<Object[]>();
          subjectIdToRows.put(memberInternalId, usersRows);
        }
        
        usersRows.add(row);
      }
      
    }
    
  }

}
