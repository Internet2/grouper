package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldStructure;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldType;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataMemberWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataProvider;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderChangeLogQueryConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderQueryFieldMappingType;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssign;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignWrapper;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowWrapper;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class GrouperDataProviderLogic {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDataProviderLogic.class);
      
  private GrouperDataProviderSync grouperDataProviderSync;
  private GrouperDataProvider grouperDataProvider;

  public void setGrouperDataProviderSync(GrouperDataProviderSync grouperDataProviderSync) {
    this.grouperDataProviderSync = grouperDataProviderSync;
  }
  
  public void setGrouperDataProvider(GrouperDataProvider grouperDataProvider) {
    this.grouperDataProvider = grouperDataProvider;
  }

  /**
   * 
   */
  public void syncFull() {
    String dataProviderConfigId = grouperDataProviderSync.getConfigId();
    
    if (grouperDataProviderSync.getGrouperDataEngine() == null) {
      grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    }
    
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
    GrouperDataEngine.syncDataRows(grouperConfig);
    GrouperDataEngine.syncDataAliases(grouperConfig);

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);
    setGrouperDataProvider(grouperDataProvider);
    
    dataEngine.loadFieldsAndRows(grouperConfig);

    // maybe things in DB arent in sync with the config yet
    if (!dataEngine.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      grouperDataProviderSync.getDebugMap().put("dataProviderConfigNotFound", dataProviderConfigId);
      return;
    }

    // wrapper object for fields, rows, and columns

    // get all dictionary text for field and row assignments for this data provider
    Map<Long, String> dictionariesByDataProvider = GrouperDictionaryDao.selectByDataProvider(grouperDataProvider.getInternalId());
    
    dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().putAll(dictionariesByDataProvider);
    for (Map.Entry<Long, String> entry : dictionariesByDataProvider.entrySet()) {
      dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().put(entry.getValue(), entry.getKey());
    }
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // get all the members that are assigned in a data provider to fields or rows
    for (Long memberInternalId : GrouperUtil.nonNull(GrouperDAOFactory.getFactory().getMember().selectByDataProvider(grouperDataProvider.getInternalId()))) {

      GrouperDataMemberWrapper grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
      dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      // get field assignments in the database for this provider
      List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId());
      processDataFieldAssignWrappers(grouperDataFieldAssigns);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      // get row assignments in the database for this provider
      List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
      processDataRowAssignWrappers(grouperDataRowAssigns);
    }    

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProvider(grouperDataProvider.getInternalId()));
      processDataRowFieldAssignWrappers(grouperDataRowFieldAssigns);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    indexDataByMember();
    
    
    
    // from db
    // wrapper object for attributes, rows, and columns
    // map by user to objects
    
    // remove attributes not there
    // remove attributes not assigned
    // add row composite key attributes (to config)
    // match rows
    
    Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();
    
    retrieveSourceData(queryConfigIdToLowerColumnNameToZeroIndex, true);
    
    calculateAndStoreChanges(queryConfigIdToLowerColumnNameToZeroIndex);
    
    // TODO should this be a separate daemon or handled somewhere else?  It would do the same thing for every provider full sync.
    deleteOldHistory();
  }
  
  /**
   * 
   */
  public void syncIncremental() {

    String dataProviderConfigId = grouperDataProviderSync.getConfigId();
    
    if (grouperDataProviderSync.getGrouperDataEngine() == null) {
      grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    }
    
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);
    
    // what are the cases where we'd want to refresh this?
    if (grouperDataProvider == null) {
      GrouperDataEngine.syncDataProviders(grouperConfig);
      GrouperDataEngine.syncDataFields(grouperConfig);
      GrouperDataEngine.syncDataRows(grouperConfig);
      GrouperDataEngine.syncDataAliases(grouperConfig);     
      
      grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);
    }
    
    setGrouperDataProvider(grouperDataProvider);

    dataEngine.loadFieldsAndRows(grouperConfig);

    // maybe things in DB arent in sync with the config yet
    if (!dataEngine.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      grouperDataProviderSync.getDebugMap().put("dataProviderConfigNotFound", dataProviderConfigId);
      return;
    }

    Timestamp changesFromTimestamp = grouperDataProviderSync.getGcGrouperSyncJob().getLastSyncTimestamp(); // start time of the last success
    Timestamp changesToTimestamp = grouperDataProviderSync.getGcGrouperSyncJob().getLastSyncStart();
    
    {
      GcGrouperSyncJob gcGrouperSyncFullJob = grouperDataProviderSync.getGcGrouperSync().getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
      if (gcGrouperSyncFullJob != null && gcGrouperSyncFullJob.getLastSyncTimestamp() != null) {
        if (changesFromTimestamp == null || gcGrouperSyncFullJob.getLastSyncTimestamp().getTime() > changesFromTimestamp.getTime()) {
          changesFromTimestamp = gcGrouperSyncFullJob.getLastSyncTimestamp();
        }
      }
    }
    
    Map<String, Map<String, Integer>> changeLogQueryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();

    Map<String, Set<String>> sourceToSubjectIds = new HashMap<String, Set<String>>();
    Map<String, Set<String>> sourceToSubjectIdentifiers = new HashMap<String, Set<String>>();
    Set<String> subjectIds = new HashSet<String>();
    Set<String> subjectIdentifiers = new HashSet<String>();
    
    for (GrouperDataProviderChangeLogQuery grouperDataProviderChangeLogQuery : grouperDataProviderSync.retrieveGrouperDataProviderChangeLogQueries()) {
      GrouperDataProviderChangeLogQueryConfig grouperDataProviderChangeLogQueryConfig = grouperDataProviderChangeLogQuery.retrieveGrouperDataProviderChangeLogQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      changeLogQueryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderChangeLogQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);
      
      List<Object[]> rows = grouperDataProviderChangeLogQuery.retrieveGrouperDataProviderQueryTargetDao().selectChangeLogData(lowerColumnNameToZeroIndex, changesFromTimestamp, changesToTimestamp);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      if (rows.size() == 0) {
        return;
      }
      
      String subjectIdAttribute = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectSourceId();
      String subjectIdType = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectIdType();
      Integer subjectIdZeroIndex = changeLogQueryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderChangeLogQueryConfig.getConfigId()).get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      if (!"subjectId".equals(subjectIdType) && !"subjectIdentifier".equals(subjectIdType)) {
        throw new RuntimeException("Unexpected providerChangeLogQuerySubjectIdType: " + subjectIdType);
      }
      
      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);

        if (StringUtils.isBlank(sourceIdAttribute)) {
          if (subjectIdType.equals("subjectId")) {
            subjectIds.add(subjectId);         
          } else {
            subjectIdentifiers.add(subjectId);         
          }
        } else {
          if (subjectIdType.equals("subjectId")) {
            Set<String> subjectIdsForSource = sourceToSubjectIds.get(sourceIdAttribute);
            if (subjectIdsForSource == null) {
              subjectIdsForSource = new HashSet<String>();
              sourceToSubjectIds.put(sourceIdAttribute, subjectIdsForSource);
            }
            subjectIdsForSource.add(subjectId);
          } else {
            Set<String> subjectIdentifiersForSource = sourceToSubjectIdentifiers.get(sourceIdAttribute);
            if (subjectIdentifiersForSource == null) {
              subjectIdentifiersForSource = new HashSet<String>();
              sourceToSubjectIdentifiers.put(sourceIdAttribute, subjectIdentifiersForSource);
            }
            subjectIdentifiersForSource.add(subjectId);
          }
        }
      }
        
    }
    
    // resolve the subjects
    Set<Subject> allSubjects = new LinkedHashSet<Subject>();
    if (GrouperUtil.length(subjectIds) > 0) {
      Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(subjectIds, null, true);
      allSubjects.addAll(subjectsByIds.values());
    }
    if (GrouperUtil.length(subjectIdentifiers) > 0) {
      Map<String, Subject> subjectsByIdentifiers = SubjectFinder.findByIdentifiers(subjectIdentifiers);
      allSubjects.addAll(subjectsByIdentifiers.values());
    }
    for (String sourceId : sourceToSubjectIds.keySet()) {
      Set<String> theSubjectIds = sourceToSubjectIds.get(sourceId);
      if (GrouperUtil.length(theSubjectIds) > 0) {
        Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(theSubjectIds, sourceId, true);
        allSubjects.addAll(subjectsByIds.values());
      }
    }
    for (String sourceId : sourceToSubjectIdentifiers.keySet()) {
      Set<String> theSubjectIdentifiers = sourceToSubjectIdentifiers.get(sourceId);
      if (GrouperUtil.length(theSubjectIdentifiers) > 0) {
        Map<String, Subject> subjectsByIdentitifers = SubjectFinder.findByIdentifiers(theSubjectIdentifiers, sourceId);
        allSubjects.addAll(subjectsByIdentitifers.values());
      }
    }
    
    Set<Member> members = MemberFinder.findBySubjects(allSubjects, true);
    for (Member member : members) {
      Long memberInternalId = member.getInternalId();

      GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
      if (grouperDataMemberWrapper == null) {
        grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
        dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
      }    
      
      grouperDataMemberWrapper.setMember(member);
    }
    
    Set<Long> memberInternalIds = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().keySet();
    
    // get all dictionary text for field and row assignments for this data provider for the members of interest
    Map<Long, String> dictionariesByDataProvider = GrouperDictionaryDao.selectByDataProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds);
    dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().putAll(dictionariesByDataProvider);
    for (Map.Entry<Long, String> entry : dictionariesByDataProvider.entrySet()) {
      dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().put(entry.getValue(), entry.getKey());
    }

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      // get field assignments in the database for this provider
      List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds);
      processDataFieldAssignWrappers(grouperDataFieldAssigns);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      // get row assignments in the database for this provider
      List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds));
      processDataRowAssignWrappers(grouperDataRowAssigns);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    {
      List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds));
      processDataRowFieldAssignWrappers(grouperDataRowFieldAssigns);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    indexDataByMember();
    
    Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();
    
    retrieveSourceData(queryConfigIdToLowerColumnNameToZeroIndex, false);
    
    calculateAndStoreChanges(queryConfigIdToLowerColumnNameToZeroIndex);    
  }
  
  private void addUnresolvableSubjectToJobMessage(String subjectIdValue) {
    if (!grouperDataProviderSync.getDebugMap().containsKey("unresolvableSubjectsFirst50")) {
      grouperDataProviderSync.getDebugMap().put("unresolvableSubjectsFirst50", new LinkedHashSet<String>()); 
    }
    
    @SuppressWarnings("unchecked")
    Set<String> unresolvableSubjects = (Set<String>)grouperDataProviderSync.getDebugMap().get("unresolvableSubjectsFirst50");
    
    if (unresolvableSubjects.size() >= 50) {
      return;
    }
    
    unresolvableSubjects.add(subjectIdValue);
  }
  
  private void processDataFieldAssignWrappers(List<GrouperDataFieldAssign> grouperDataFieldAssigns) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    for (GrouperDataFieldAssign grouperDataFieldAssign : grouperDataFieldAssigns) {
      GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = new GrouperDataFieldAssignWrapper(dataEngine, grouperDataFieldAssign);
      
      dataEngine.getGrouperDataProviderIndex().getFieldAssignWrapperByInternalId().put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssignWrapper);
      
      grouperDataFieldAssignWrapper.setGrouperDataFieldWrapper(dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(grouperDataFieldAssign.getDataFieldInternalId()));

      grouperDataFieldAssignWrapper.setMemberWrapper(dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(grouperDataFieldAssign.getMemberInternalId()));

      if (grouperDataFieldAssign.getValueDictionaryInternalId() != null) {
        // TODO fix race conditions here
        String textValue = dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().get(grouperDataFieldAssign.getValueDictionaryInternalId());
        GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
        grouperDataFieldAssignWrapper.setTextValue(textValue);
      }
    }
  }
  
  private void processDataRowAssignWrappers(List<GrouperDataRowAssign> grouperDataRowAssigns) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = new GrouperDataRowAssignWrapper(dataEngine, grouperDataRowAssign);
      
      dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper);
      
      grouperDataRowAssignWrapper.setGrouperDataRowWrapper(dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(grouperDataRowAssign.getDataRowInternalId()));

      grouperDataRowAssignWrapper.setMemberWrapper(dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(grouperDataRowAssign.getMemberInternalId()));
      
    }
  }
  
  private void processDataRowFieldAssignWrappers(List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
      
      GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper = new GrouperDataRowFieldAssignWrapper(dataEngine, grouperDataRowFieldAssign);
      
      dataEngine.getGrouperDataProviderIndex().getRowFieldAssignWrapperByInternalId().put(grouperDataRowFieldAssign.getInternalId(), grouperDataRowFieldAssignWrapper);

      grouperDataRowFieldAssignWrapper.setGrouperDataFieldWrapper(dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(grouperDataRowFieldAssign.getDataFieldInternalId()));
      
      if (grouperDataRowFieldAssign.getValueDictionaryInternalId() != null) {
        // TODO fix race conditions here
        String textValue = dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().get(grouperDataRowFieldAssign.getValueDictionaryInternalId());
        GrouperUtil.assertion(!StringUtils.isBlank(textValue), "Cant find text: " + grouperDataRowFieldAssign.getValueDictionaryInternalId());
        grouperDataRowFieldAssignWrapper.setTextValue(textValue);
      }

      grouperDataRowFieldAssignWrapper.setGrouperDataRowAssignWrapper(dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(grouperDataRowFieldAssign.getDataRowAssignInternalId()));
      
    }
  }
  
  private void indexDataByMember() {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    // index attribute assignments by person
    for (GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getFieldAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataFieldAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
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
    for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().values()) {
      
      Long memberInternalId = grouperDataRowAssignWrapper.getMemberWrapper().getInternalId();
      GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
      
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
    for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper  : dataEngine.getGrouperDataProviderIndex().getRowFieldAssignWrapperByInternalId().values()) {
      
      Long rowAssignId = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataRowAssignInternalId();
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(rowAssignId);
      
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
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {

      for (Long fieldInternalId : grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().keySet()) {
        
        List<GrouperDataFieldAssignWrapper> dataFieldAssignWrappers = GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldAssignWrappersByFieldInternalId().get(fieldInternalId));
        GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(fieldInternalId);

        String dataFieldConfigId = grouperDataFieldWrapper.getGrouperDataField().getConfigId();
        GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
        
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
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            continue;
          }
          
          if (!grouperDataFieldConfig.isFieldMultiValued() && valueToFieldAssignWrapper.size() >= 1) {
            GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            continue;
          }
          values.add(value);
          valueToFieldAssignWrapper.put(value, dataFieldAssignWrapper);
        }
      }
    }
  }
  
  private void retrieveSourceData(Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex, boolean isFullSync) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    Map<MultiKey, Subject> subjectIdAttributeSubjectIdSourceIdToSubject = new HashMap<MultiKey, Subject>();

    Map<String, Set<String>> sourceToSubjectIds = new HashMap<String, Set<String>>();
    Map<String, Set<String>> sourceToSubjectIdentifiers = new HashMap<String, Set<String>>();
    Set<String> subjectIds = new HashSet<String>();
    Set<String> subjectIdentifiers = new HashSet<String>();
    
    Map<GrouperDataProviderQuery, List<Object[]>> grouperDataProviderQueryToRows = new LinkedHashMap<>();
    
    // pass one, resolve all the subjects
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);
      
      List<Object[]> rows;

      // get the data from the source
      if (isFullSync) {
        rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectData(lowerColumnNameToZeroIndex);
      } else {
        // TODO for incremental, we probably don't need to look up again
        Collection<GrouperDataMemberWrapper> grouperDataMemberWrappers = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values();
        Set<Member> members = new HashSet<Member>();
        for (GrouperDataMemberWrapper grouperDataMemberWrapper : grouperDataMemberWrappers) {
          if (grouperDataMemberWrapper.getMember() != null) {
            members.add(grouperDataMemberWrapper.getMember());
          }
        }
        
        rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectDataByMembers(lowerColumnNameToZeroIndex, members);
      }
      grouperDataProviderQueryToRows.put(grouperDataProviderQuery, rows);
      
      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      String subjectIdType = grouperDataProviderQueryConfig.getProviderQuerySubjectIdType();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      if (!"subjectId".equals(subjectIdType) && !"subjectIdentifier".equals(subjectIdType)) {
        throw new RuntimeException("Unexpected providerQuerySubjectIdType: " + subjectIdType);
      }
      
      // loop over the rows and get the subject ids or identifiers and collect them up
      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);

        if (StringUtils.isBlank(sourceIdAttribute)) {
          if (subjectIdType.equals("subjectId")) {
            subjectIds.add(subjectId);         
          } else {
            subjectIdentifiers.add(subjectId);         
          }
        } else {
          if (subjectIdType.equals("subjectId")) {
            Set<String> subjectIdsForSource = sourceToSubjectIds.get(sourceIdAttribute);
            if (subjectIdsForSource == null) {
              subjectIdsForSource = new HashSet<String>();
              sourceToSubjectIds.put(sourceIdAttribute, subjectIdsForSource);
            }
            subjectIdsForSource.add(subjectId);
          } else {
            Set<String> subjectIdentifiersForSource = sourceToSubjectIdentifiers.get(sourceIdAttribute);
            if (subjectIdentifiersForSource == null) {
              subjectIdentifiersForSource = new HashSet<String>();
              sourceToSubjectIdentifiers.put(sourceIdAttribute, subjectIdentifiersForSource);
            }
            subjectIdentifiersForSource.add(subjectId);
          }
        }
      }
    }
    // resolve all the subjects at once depending on the source and type
    if (GrouperUtil.length(subjectIds) > 0) {
      Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(subjectIds, null, true);
      for (String subjectId : subjectsByIds.keySet()) {
        Subject subject = subjectsByIds.get(subjectId);
        MultiKey subjectIdAttributeSubjectIdSourceId = new MultiKey("subjectId", subjectId, null);
        subjectIdAttributeSubjectIdSourceIdToSubject.put(subjectIdAttributeSubjectIdSourceId, subject);
      }
    }
    if (GrouperUtil.length(subjectIdentifiers) > 0) {
      Map<String, Subject> subjectsByIdentifiers = SubjectFinder.findByIdentifiers(subjectIdentifiers);
      for (String subjectIdentifier : subjectsByIdentifiers.keySet()) {
        Subject subject = subjectsByIdentifiers.get(subjectIdentifier);
        MultiKey subjectIdAttributeSubjectIdSourceId = new MultiKey("subjectIdentifier", subjectIdentifier, null);
        subjectIdAttributeSubjectIdSourceIdToSubject.put(subjectIdAttributeSubjectIdSourceId, subject);
      }
    }
    for (String sourceId : sourceToSubjectIds.keySet()) {
      Set<String> theSubjectIds = sourceToSubjectIds.get(sourceId);
      if (GrouperUtil.length(theSubjectIds) > 0) {
        Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(theSubjectIds, sourceId, true);
        for (String subjectId : subjectsByIds.keySet()) {
          Subject subject = subjectsByIds.get(subjectId);
          MultiKey subjectIdAttributeSubjectIdSourceId = new MultiKey("subjectId", subjectId, sourceId);
          subjectIdAttributeSubjectIdSourceIdToSubject.put(subjectIdAttributeSubjectIdSourceId, subject);
        }
      }
    }
    for (String sourceId : sourceToSubjectIdentifiers.keySet()) {
      Set<String> theSubjectIdentifiers = sourceToSubjectIdentifiers.get(sourceId);
      if (GrouperUtil.length(theSubjectIdentifiers) > 0) {
        Map<String, Subject> subjectsByIdentitifers = SubjectFinder.findByIdentifiers(theSubjectIdentifiers, sourceId);
        for (String subjectIdentifier : subjectsByIdentitifers.keySet()) {
          Subject subject = subjectsByIdentitifers.get(subjectIdentifier);
          MultiKey subjectIdAttributeSubjectIdSourceId = new MultiKey("subjectIdentifier", subjectIdentifier, sourceId);
          subjectIdAttributeSubjectIdSourceIdToSubject.put(subjectIdAttributeSubjectIdSourceId, subject);
        }
      }
    }

    Set<Subject> subjects = new HashSet<Subject>();
    subjects.addAll(subjectIdAttributeSubjectIdSourceIdToSubject.values());
    
    Map<Subject, Member> subjectToMember = MemberFinder.findBySubjectsToMap(subjects, true);
    
    // pass two, assign the data to the members
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId());
      
      List<Object[]> rows = grouperDataProviderQueryToRows.get(grouperDataProviderQuery);

      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      String subjectIdType = grouperDataProviderQueryConfig.getProviderQuerySubjectIdType();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        MultiKey multiKey = new MultiKey(subjectIdType, subjectId, sourceIdAttribute);
        
        Subject subject = subjectIdAttributeSubjectIdSourceIdToSubject.get(multiKey);
        Member member = subjectToMember.get(subject);
        
        if (member == null) {
          LOG.warn("Unable to resolve subject " + subjectId + ", " + sourceIdAttribute + ", " + subjectIdType);
          grouperDataProviderSync.getHib3GrouperLoaderLog().addUnresolvableSubjectCount(1);
          addUnresolvableSubjectToJobMessage(subjectId);
          continue;
        }
          

        Long memberInternalId = member.getInternalId();
        
        GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
        
        if (grouperDataMemberWrapper == null) {
          grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
          dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
          grouperDataMemberWrapper.setMember(member);
        }
        

        List<Object[]> userRowsforQuery = grouperDataMemberWrapper.getQueryConfigIdToRowData().get(grouperDataProviderQueryConfig.getConfigId());
        if (userRowsforQuery == null) {
          userRowsforQuery = new ArrayList<Object[]>();
          grouperDataMemberWrapper.getQueryConfigIdToRowData().put(grouperDataProviderQueryConfig.getConfigId(), userRowsforQuery);
        }
        
        userRowsforQuery.add(row);
      }
      
    }
  }
  
  private void calculateAndStoreChanges(Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    List<GrouperDataFieldAssign> grouperDataFieldAssignsToDelete = new ArrayList<GrouperDataFieldAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataFieldAssignsToDelete = new LinkedHashSet<ChangeLogEntry>();
    
    List<GrouperDataFieldAssign> grouperDataFieldAssignsToInsert = new ArrayList<GrouperDataFieldAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataFieldAssignsToInsert = new LinkedHashSet<ChangeLogEntry>();
    
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssignsToDelete = new ArrayList<GrouperDataRowFieldAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataRowFieldAssignsToDelete = new LinkedHashSet<ChangeLogEntry>();

    List<GrouperDataRowAssign> grouperDataRowAssignsToDelete = new ArrayList<GrouperDataRowAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataRowAssignsToDelete = new LinkedHashSet<ChangeLogEntry>();

    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssignsToInsert = new ArrayList<GrouperDataRowFieldAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataRowFieldAssignsToInsert = new LinkedHashSet<ChangeLogEntry>();

    List<GrouperDataRowAssign> grouperDataRowAssignsToInsert = new ArrayList<GrouperDataRowAssign>();
    Set<ChangeLogEntry> changeLogEntriesDataRowAssignsToInsert = new LinkedHashSet<ChangeLogEntry>();

    List<GrouperDataRowAssign> grouperDataRowAssignsToUpdate = new ArrayList<GrouperDataRowAssign>();

    Set<String> needsDictionaryText = new HashSet<String>();
    
    // go through each user, index and convert the data
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {
      
      // go through each query
      for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
        
        GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();
        String queryConfigId = grouperDataProviderQueryConfig.getConfigId();
        
        List<Object[]> providerRows = GrouperUtil.nonNull(grouperDataMemberWrapper.getQueryConfigIdToRowData().get(queryConfigId));
        grouperDataProviderSync.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(providerRows));
        
        String rowConfigId = grouperDataProviderQueryConfig.getProviderQueryRowConfigId();

        //GrouperDataFieldConfig grouperDataRowConfig = null;
        
        GrouperDataRowWrapper grouperDataRowWrapper = null;
        List<Map<Long, List<Object>>> rowsOfFieldInternalIdToValues = null;
        
        // if this is a direct assignment
        if (!StringUtils.isBlank(rowConfigId)) {
          
          //grouperDataRowConfig = this.fieldConfigByConfigId.get(rowConfigId);
          
          // if this is a row assignment
          grouperDataRowWrapper = dataEngine.getGrouperDataProviderIndex().getRowWrapperByConfigId().get(rowConfigId);
          
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
  
              GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);
              
              GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(dataFieldConfigId);
                
              Integer rowIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(columnName.toLowerCase());
              if (rowIndex == null) {
                throw new RuntimeException("Unable to find index for configId=" + grouperDataProviderQueryConfig.getConfigId() + ", columnName=" + columnName.toLowerCase());
              }
              
              Object value = row[rowIndex];
              
              if (value instanceof Set) {
                if (((Set)value).size() > 0) {
                  
                  if (!grouperDataProviderQueryConfig.isStoreNulls()) {
                    ((Set)value).remove(null);
                    if (((Set) value).size() == 0) {
                      continue;
                    }
                  }
                  
                  List<Object> data = grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(grouperDataFieldWrapper.getGrouperDataField().getInternalId());
                  if (data == null) {
                    data = new ArrayList<>();
                    grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().put(grouperDataFieldWrapper.getGrouperDataField().getInternalId(), data);
                  }
                  
                  for (Object currentValue : (Set)value) {
                    currentValue = grouperDataFieldConfig.getFieldDataType().convertValue(currentValue);
                    if (currentValue != null && currentValue != Void.TYPE && grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string) {
                      needsDictionaryText.add((String)currentValue);
                    }
                    data.add(currentValue);
                  }
                }
              } else {
                
                if (!grouperDataProviderQueryConfig.isStoreNulls()) {
                  if (value == null) {
                    continue;
                  }
                }
                value = grouperDataFieldConfig.getFieldDataType().convertValue(value);
                if (value != null && value != Void.TYPE && grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string) {
                  needsDictionaryText.add((String)value);
                }

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
        needsDictionaryText.removeAll(dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().values());
        
        if (needsDictionaryText.size() > 0) {
          Map<String, Long> dictionaryTextToInternalId = GrouperDictionaryDao.findOrAdd(needsDictionaryText);

          for (String text : needsDictionaryText) {
            Long internalId = dictionaryTextToInternalId.get(text);
            dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().put(internalId, text);
            dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().put(text, internalId);
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
  
          GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
          GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

          if (grouperDataFieldConfig.getFieldDataStructure() == GrouperDataFieldStructure.attribute) {
            Map<Object, GrouperDataFieldAssignWrapper> valueToFieldAssignWrapper = GrouperUtil.nonNull(fieldIdToValueToFieldAssignWrapper.get(dataFieldInternalId));
            
            Set<Object> dataFromProvider = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataFieldIternalId().get(dataFieldInternalId)));
            Set<Object> dataFromGrouper = new HashSet<>(GrouperUtil.nonNull(grouperDataMemberWrapper.getFieldIdToValues().get(dataFieldInternalId)));
            
            Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
            dataToDelete.removeAll(dataFromProvider);
            
            for (Object value : dataToDelete) {
              GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);
              
              GrouperDataFieldAssign grouperDataFieldAssign = grouperDataFieldAssignWrapper.getGrouperDataFieldAssign();
              
              grouperDataFieldAssignsToDelete.add(grouperDataFieldAssign);
              
              Long valueOrInternalId = grouperDataFieldAssign.getValueInteger() != null ? 
                  grouperDataFieldAssign.getValueInteger() 
                  : grouperDataFieldAssign.getValueDictionaryInternalId();
              
              changeLogEntriesDataFieldAssignsToDelete.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_FIELD_ASSIGN_DELETE,
                  ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.id.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.dataFieldInternalId.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getDataFieldInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.memberInternalId.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getMemberInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.valueOrInternalId.name(),
                  GrouperUtil.stringValue(valueOrInternalId)));

            }
            
            Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
            dataToInsert.removeAll(dataFromGrouper);
    
            
            for (Object value : dataToInsert) {
              GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
              grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
              grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
              grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
              grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value, dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());
              grouperDataFieldAssignsToInsert.add(grouperDataFieldAssign);
              
              Long valueOrInternalId = grouperDataFieldAssign.getValueInteger() != null ? 
                  grouperDataFieldAssign.getValueInteger() 
                  : grouperDataFieldAssign.getValueDictionaryInternalId();

              changeLogEntriesDataFieldAssignsToInsert.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_FIELD_ASSIGN_ADD,
                  ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.id.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.dataFieldInternalId.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getDataFieldInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.memberInternalId.name(),
                  GrouperUtil.stringValue(grouperDataFieldAssign.getMemberInternalId()),
                  ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.valueOrInternalId.name(),
                  GrouperUtil.stringValue(valueOrInternalId)));

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
          
          GrouperDataRow grouperDataRow = dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(dataRowInternalId).getGrouperDataRow();
          GrouperDataRowConfig grouperDataRowConfig = dataEngine.getRowConfigByConfigId().get(grouperDataRow.getConfigId());
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

              GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(rowKeyFieldConfigId);
              GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(rowKeyFieldConfigId).getGrouperDataField();
              List<Object> values = providerDataFieldInternalIdToValues.get(grouperDataField.getInternalId());

              if (GrouperUtil.length(values) != 1) {
                throw new RuntimeException("Provider row field key must have one value: " + grouperDataRowConfig.getConfigId() 
                + ", field: " + grouperDataFieldConfig.getConfigId() + ", " + GrouperUtil.stringValue(values));
              }

              keyValues[i] = grouperDataFieldConfig.getFieldDataType().convertValue(values.iterator().next());

//              GrouperUtil.assertion(keyValues[i] != null && keyValues[i] != Void.TYPE, 
//                  "Data row field key must not have a null value: " + grouperDataRowConfig.getConfigId() 
//                  + ", rowAssignId: " + grouperDataRow.getInternalId() + ", field: " + grouperDataFieldConfig.getConfigId());
              i++;
            }
            MultiKey rowKey = new MultiKey(keyValues);
            providerDataRowKeyToDataFieldInternalIdsAndValues.put(rowKey, providerDataFieldInternalIdToValues);
          }

          Set<MultiKey> rowKeyFieldsToDeletes = new HashSet<>(grouperDataRowKeyToRowAssignWrapper.keySet());
          rowKeyFieldsToDeletes.removeAll(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());

          for (MultiKey rowKeyFieldsToDelete : rowKeyFieldsToDeletes) {
            GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(rowKeyFieldsToDelete);
            GrouperDataRowAssign grouperDataRowAssign = grouperDataRowAssignWrapper.getGrouperDataRowAssign();

            for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {
              
              for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                
                GrouperDataRowFieldAssign grouperDataRowFieldAssign = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign();

                Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ? 
                    grouperDataRowFieldAssign.getValueInteger() 
                    : grouperDataRowFieldAssign.getValueDictionaryInternalId();

                grouperDataRowFieldAssignsToDelete.add(grouperDataRowFieldAssign);
                
                changeLogEntriesDataRowFieldAssignsToDelete.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_DELETE,
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.id.name(),
                    GrouperUtil.stringValue(grouperDataRowFieldAssign.getInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataRowInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataRowAssignInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataFieldInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataFieldInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.memberInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.valueOrInternalId.name(),
                    GrouperUtil.stringValue(valueOrInternalId)));
                
              }
            }

            grouperDataRowAssignsToDelete.add(grouperDataRowAssignWrapper.getGrouperDataRowAssign());
            changeLogEntriesDataRowAssignsToDelete.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROW_ASSIGN_DELETE,
                    ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.id.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                    ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.dataRowInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                    ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.memberInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId())));

          }

          Set<MultiKey> rowKeyFieldsToInserts = new HashSet<>(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
          rowKeyFieldsToInserts.removeAll(grouperDataRowKeyToRowAssignWrapper.keySet());


          for (MultiKey rowKeyFieldsToInsert : rowKeyFieldsToInserts) {
            GrouperDataRowAssign grouperDataRowAssign = new GrouperDataRowAssign();
            grouperDataRowAssign.setDataRowInternalId(dataRowInternalId);
            grouperDataRowAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
            grouperDataRowAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
            
            grouperDataRowAssignsToInsert.add(grouperDataRowAssign);
            
            changeLogEntriesDataRowAssignsToInsert.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROW_ASSIGN_ADD,
                ChangeLogLabels.DATA_ROW_ASSIGN_ADD.id.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                ChangeLogLabels.DATA_ROW_ASSIGN_ADD.dataRowInternalId.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                ChangeLogLabels.DATA_ROW_ASSIGN_ADD.memberInternalId.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId())));
            

            Map<Long, List<Object>> dataFieldInternalIdToValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(rowKeyFieldsToInsert);
            for (Long dataFieldInternalId : GrouperUtil.nonNull(dataFieldInternalIdToValues.keySet())) {

              GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
              GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

              List<Object> values = dataFieldInternalIdToValues.get(dataFieldInternalId);
              for (Object value : values) {
                // TODO This is Void.TYPE, not null
                GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                grouperDataRowFieldAssign.setDataRowAssign(grouperDataRowAssign);
                grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, value, dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());

                Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ? 
                    grouperDataRowFieldAssign.getValueInteger() 
                    : grouperDataRowFieldAssign.getValueDictionaryInternalId();

                grouperDataRowFieldAssignsToInsert.add(grouperDataRowFieldAssign);
                
                changeLogEntriesDataRowFieldAssignsToInsert.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_ADD,
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.id.name(),
                    GrouperUtil.stringValue(grouperDataRowFieldAssign.getInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowAssignInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataFieldInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataFieldInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.memberInternalId.name(),
                    GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId()),
                    ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.valueOrInternalId.name(),
                    GrouperUtil.stringValue(valueOrInternalId)));


              }
            }

          }

          // do the updates
          for (MultiKey grouperDataRowKey : grouperDataRowKeyToRowAssignWrapper.keySet()) {
            if (providerDataRowKeyToDataFieldInternalIdsAndValues.containsKey(grouperDataRowKey)) {
              GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(grouperDataRowKey);

              GrouperDataRowAssign grouperDataRowAssign = grouperDataRowAssignWrapper.getGrouperDataRowAssign();
              Map<Long, List<Object>> providerDataFieldInternalIdsAndValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(grouperDataRowKey);

              Set<Long> dataFieldInternalIds = new LinkedHashSet<>(GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.keySet()));
              dataFieldInternalIds.addAll(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().keySet());
              
              for (Long dataFieldInternalId : dataFieldInternalIds) {
                
                GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
                GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

                List<Object> providerValues = GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.get(dataFieldInternalId));
                List<Object> grouperValuesConverted = new ArrayList<Object>();
                List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = GrouperUtil.nonNull(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId));
                for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
                  GrouperDataRowFieldAssign grouperDataRowFieldAssign = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign();
                  
                  Object grouperValueConverted = grouperDataFieldConfig.getFieldDataType().convertValue(
                      grouperDataRowFieldAssign.getValueInteger(),
                      grouperDataRowFieldAssignWrapper.getTextValue());
                  if (providerValues.contains(grouperValueConverted)) {
                    grouperValuesConverted.add(grouperValueConverted);
                  } else {
                    grouperDataRowFieldAssignsToDelete.add(grouperDataRowFieldAssign);
                    
                    Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ? 
                        grouperDataRowFieldAssign.getValueInteger() 
                        : grouperDataRowFieldAssign.getValueDictionaryInternalId();

                    changeLogEntriesDataRowFieldAssignsToDelete.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_DELETE,
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.id.name(),
                        GrouperUtil.stringValue(grouperDataRowFieldAssign.getInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataRowInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataRowAssignInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.dataFieldInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataFieldInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.memberInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_DELETE.valueOrInternalId.name(),
                        GrouperUtil.stringValue(valueOrInternalId)));

                  }
                }

                Set<Object> valuesToAdd = new HashSet<Object>(providerValues);
                valuesToAdd.removeAll(grouperValuesConverted);

                for (Object valueToAdd : valuesToAdd) {
                  if (valueToAdd != null && valueToAdd != Void.TYPE) {
                    GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
                    grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
                    grouperDataRowFieldAssign.setDataRowAssign(grouperDataRowAssignWrapper.getGrouperDataRowAssign());
                    grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, valueToAdd, dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());
                    grouperDataRowFieldAssignsToInsert.add(grouperDataRowFieldAssign);
                    
                    Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ? 
                        grouperDataRowFieldAssign.getValueInteger() 
                        : grouperDataRowFieldAssign.getValueDictionaryInternalId();

                    changeLogEntriesDataRowFieldAssignsToInsert.add(new ChangeLogEntry(true, ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_ADD,
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.id.name(),
                        GrouperUtil.stringValue(grouperDataRowFieldAssign.getInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowAssignInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataFieldInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataFieldInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.memberInternalId.name(),
                        GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId()),
                        ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.valueOrInternalId.name(),
                        GrouperUtil.stringValue(valueOrInternalId)));

                  }
                }
              }
            }
          }
        }
      }
      GrouperDaemonUtils.stopProcessingIfJobPaused();

    }
        
    // see if we're adding to field assign history
    List<GrouperDataFieldAssignHst> grouperDataFieldAssignHstsToInsert = new ArrayList<>();
    for (GrouperDataFieldAssign grouperDataFieldAssignToDelete : grouperDataFieldAssignsToDelete) {
      GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(grouperDataFieldAssignToDelete.getDataFieldInternalId());
      
      if (grouperDataFieldWrapper != null) {
        if (grouperDataFieldWrapper.getGrouperDataFieldConfig().isFieldDataStorePit()) {
          GrouperDataFieldAssignHst grouperDataFieldAssignHst = new GrouperDataFieldAssignHst();
          grouperDataFieldAssignHst.setDataFieldInternalId(grouperDataFieldAssignToDelete.getDataFieldInternalId());
          grouperDataFieldAssignHst.setMemberInternalId(grouperDataFieldAssignToDelete.getMemberInternalId());
          grouperDataFieldAssignHst.setValueInteger(grouperDataFieldAssignToDelete.getValueInteger());
          grouperDataFieldAssignHst.setValueDictionaryInternalId(grouperDataFieldAssignToDelete.getValueDictionaryInternalId());
          grouperDataFieldAssignHst.setStartTime(grouperDataFieldAssignToDelete.getCreatedOn().getTime() * 1000L);
          grouperDataFieldAssignHst.setEndTime(System.currentTimeMillis() * 1000L);
          grouperDataFieldAssignHstsToInsert.add(grouperDataFieldAssignHst);
        }
      }
    }
    
    // see if we're adding to row assign history.
    // row assign history is added if row assign is being deleted or if a field is be added/deleted for an existing row assign
    Set<Long> rowAssignInternalIdsForHistoryIfConfigured = new LinkedHashSet<>();
    Set<Long> rowAssignInternalIdsBeingDeleted = new LinkedHashSet<>();
    List<GrouperDataRowAssignHst> grouperDataRowAssignHstsToInsert = new ArrayList<>();
    List<GrouperDataRowFieldAssignHst> grouperDataRowFieldAssignHstsToInsert = new ArrayList<>();
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssignToDelete : grouperDataRowFieldAssignsToDelete) {
      rowAssignInternalIdsForHistoryIfConfigured.add(grouperDataRowFieldAssignToDelete.getDataRowAssignInternalId());
    }
    for (GrouperDataRowAssign grouperDataRowAssignToDelete : grouperDataRowAssignsToDelete) {
      rowAssignInternalIdsForHistoryIfConfigured.add(grouperDataRowAssignToDelete.getInternalId());
      rowAssignInternalIdsBeingDeleted.add(grouperDataRowAssignToDelete.getInternalId());
    }
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssignToInsert : grouperDataRowFieldAssignsToInsert) {
      // for new row assigns, the internal id isn't assigned at this point so if it's not -1, then that means it's an existing one
      if (grouperDataRowFieldAssignToInsert.getDataRowAssign().getInternalId() != -1) {
        rowAssignInternalIdsForHistoryIfConfigured.add(grouperDataRowFieldAssignToInsert.getDataRowAssign().getInternalId());
      }
    }
    for (long rowAssignInternalId : rowAssignInternalIdsForHistoryIfConfigured) {
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().get(rowAssignInternalId);
      GrouperDataRowWrapper grouperDataRowWrapper = dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getDataRowInternalId());

      if (grouperDataRowAssignWrapper != null && grouperDataRowWrapper != null) {
        if (grouperDataRowWrapper.getGrouperDataRowConfig().isRowDataStorePit()) {
          Long endTime = System.currentTimeMillis() * 1000L;
          Long startTime = grouperDataRowAssignWrapper.getGrouperDataRowAssign().getLastUpdated();
          if (startTime == null) {
            startTime = grouperDataRowAssignWrapper.getGrouperDataRowAssign().getCreatedOn().getTime() * 1000L;
          }
          
          GrouperDataRowAssignHst grouperDataRowAssignHst = new GrouperDataRowAssignHst();
          grouperDataRowAssignHst.setMemberInternalId(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getMemberInternalId());
          grouperDataRowAssignHst.setDataRowInternalId(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getDataRowInternalId());
          grouperDataRowAssignHst.setDataRowAssignInternalId(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getInternalId());
          grouperDataRowAssignHst.setStartTime(startTime);
          grouperDataRowAssignHst.setEndTime(endTime);
          grouperDataRowAssignHstsToInsert.add(grouperDataRowAssignHst);
          
          if (!rowAssignInternalIdsBeingDeleted.contains(rowAssignInternalId)) {
            // we need to update the last_updated
            grouperDataRowAssignWrapper.getGrouperDataRowAssign().setLastUpdated(endTime);
            grouperDataRowAssignsToUpdate.add(grouperDataRowAssignWrapper.getGrouperDataRowAssign());
          }
    
          for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {
            for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {          
              GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst = new GrouperDataRowFieldAssignHst();
              grouperDataRowFieldAssignHst.setDataRowAssignHst(grouperDataRowAssignHst);
              grouperDataRowFieldAssignHst.setDataFieldInternalId(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataFieldInternalId());
              grouperDataRowFieldAssignHst.setValueInteger(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueInteger());
              grouperDataRowFieldAssignHst.setValueDictionaryInternalId(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueDictionaryInternalId());
              grouperDataRowFieldAssignHstsToInsert.add(grouperDataRowFieldAssignHst);
            }
          }
        }
      }
    }
    
    
    //TODO use transactions and maybe do the batching here
    GrouperDataFieldAssignHstDao.store(grouperDataFieldAssignHstsToInsert);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(grouperDataFieldAssignHstsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    GrouperDataFieldAssignDao.delete(grouperDataFieldAssignsToDelete);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataFieldAssignsToDelete, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(changeLogEntriesDataFieldAssignsToDelete.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();


    GrouperDataFieldAssignDao.store(grouperDataFieldAssignsToInsert);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataFieldAssignsToInsert, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(changeLogEntriesDataFieldAssignsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    GrouperDataRowAssignHstDao.store(grouperDataRowAssignHstsToInsert);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(grouperDataRowAssignHstsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    GrouperDataRowFieldAssignHstDao.store(grouperDataRowFieldAssignHstsToInsert);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(grouperDataRowFieldAssignHstsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    GrouperDataRowAssignDao.store(grouperDataRowAssignsToUpdate);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addUpdateCount(grouperDataRowAssignsToUpdate.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();
    
    GrouperDataRowFieldAssignDao.delete(grouperDataRowFieldAssignsToDelete);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataRowFieldAssignsToDelete, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(changeLogEntriesDataRowFieldAssignsToDelete.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    GrouperDataRowAssignDao.delete(grouperDataRowAssignsToDelete);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataRowAssignsToDelete, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(changeLogEntriesDataRowAssignsToDelete.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    GrouperDataRowAssignDao.store(grouperDataRowAssignsToInsert);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataRowAssignsToInsert, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(changeLogEntriesDataRowAssignsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    GrouperDataRowFieldAssignDao.store(grouperDataRowFieldAssignsToInsert);
    GrouperDAOFactory.getFactory().getChangeLogEntry().saveBatch(changeLogEntriesDataRowFieldAssignsToInsert, true);
    grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(changeLogEntriesDataRowFieldAssignsToInsert.size());
    GrouperDaemonUtils.stopProcessingIfJobPaused();


  }
  
  private void deleteOldHistory() {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    Set<Long> grouperDataFieldInternalIdsNoHistoryConfigured = new LinkedHashSet<>();
    Set<Long> grouperDataRowInternalIdsNoHistoryConfigured = new LinkedHashSet<>();
    List<GrouperDataFieldAssignHst> grouperDataFieldAssignHstsToDelete = new ArrayList<>();
    List<GrouperDataRowAssignHst> grouperDataRowAssignHstsToDelete = new ArrayList<>();

    for (GrouperDataFieldConfig grouperDataFieldConfig : dataEngine.getFieldConfigByConfigId().values()) {
      GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(grouperDataFieldConfig.getConfigId()).getGrouperDataField();
      if (!grouperDataFieldConfig.isFieldDataStorePit()) {
        // delete any field assigns in history
        grouperDataFieldInternalIdsNoHistoryConfigured.add(grouperDataField.getInternalId());
      } else {
        // delete field assigns in history older than configured days
        long days = grouperDataFieldConfig.getFieldDataStorePitDays();
        long endTimeBeforeMicros = System.currentTimeMillis() * 1000L - days * 24 * 60 * 60 * 1000 * 1000;
        grouperDataFieldAssignHstsToDelete.addAll(GrouperDataFieldAssignHstDao.selectByDataFieldInternalIdAndEndTimeBefore(grouperDataField.getInternalId(), endTimeBeforeMicros));
      }
      
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
    
    for (GrouperDataRowConfig grouperDataRowConfig : dataEngine.getRowConfigByConfigId().values()) {
      GrouperDataRow grouperDataRow = dataEngine.getGrouperDataProviderIndex().getRowWrapperByConfigId().get(grouperDataRowConfig.getConfigId()).getGrouperDataRow();
      if (!grouperDataRowConfig.isRowDataStorePit()) {
        // delete any row assigns in history
        grouperDataRowInternalIdsNoHistoryConfigured.add(grouperDataRow.getInternalId());
      } else {
        // delete row assigns in history older than configured days
        long days = grouperDataRowConfig.getRowDataStorePitDays();
        long endTimeBeforeMicros = System.currentTimeMillis() * 1000L - days * 24 * 60 * 60 * 1000 * 1000;
        grouperDataRowAssignHstsToDelete.addAll(GrouperDataRowAssignHstDao.selectByDataRowInternalIdAndEndTimeBefore(grouperDataRow.getInternalId(), endTimeBeforeMicros));
      }
      
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
    
    if (grouperDataFieldInternalIdsNoHistoryConfigured.size() > 0) {
      grouperDataFieldAssignHstsToDelete.addAll(GrouperDataFieldAssignHstDao.selectByDataFieldInternalIds(grouperDataFieldInternalIdsNoHistoryConfigured));
    }
    
    if (grouperDataRowInternalIdsNoHistoryConfigured.size() > 0) {
      grouperDataRowAssignHstsToDelete.addAll(GrouperDataRowAssignHstDao.selectByDataRowInternalIds(grouperDataRowInternalIdsNoHistoryConfigured));
    }
    
    if (grouperDataFieldAssignHstsToDelete.size() > 0) {
      GrouperDataFieldAssignHstDao.delete(grouperDataFieldAssignHstsToDelete);
      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(grouperDataFieldAssignHstsToDelete.size());
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
    
    if (grouperDataRowAssignHstsToDelete.size() > 0) {
      // delete row fields first
      Set<Long> grouperDataRowAssignHstInternalIds = new LinkedHashSet<>();
      for (GrouperDataRowAssignHst grouperDataRowAssignHst : grouperDataRowAssignHstsToDelete) {
        grouperDataRowAssignHstInternalIds.add(grouperDataRowAssignHst.getInternalId());
      }
      List<GrouperDataRowFieldAssignHst> grouperDataRowFieldAssignHstsToDelete = GrouperDataRowFieldAssignHstDao.selectByDataRowAssignHstInternalIds(grouperDataRowAssignHstInternalIds);
      
      GrouperDataRowFieldAssignHstDao.delete(grouperDataRowFieldAssignHstsToDelete);
      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(grouperDataRowFieldAssignHstsToDelete.size());
      GrouperDaemonUtils.stopProcessingIfJobPaused();
      
      GrouperDataRowAssignHstDao.delete(grouperDataRowAssignHstsToDelete);
      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(grouperDataRowAssignHstsToDelete.size());
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
  }
}
