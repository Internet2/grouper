package edu.internet2.middleware.grouper.app.dataProvider;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.OtherJobException;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntryTemp;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntryTempDao;
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
import edu.internet2.middleware.grouper.dataField.GrouperDataProviderIndex;
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
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class GrouperDataProviderLogic {
  
  private static final Log LOG = GrouperUtil.getLog(GrouperDataProviderLogic.class);
      
  private static final int MAX_CHANGE_EXAMPLES = 20;

  /**
   * last debug map from the most recent full sync, for testing purposes only.
   * set at the end of syncFull() so tests can inspect accumulated counts and change examples.
   */
  public static Map<String, Object> testingDebugMap;

  private GrouperDataProviderSync grouperDataProviderSync;
  private GrouperDataProvider grouperDataProvider;
  private GrouperDataEngine dataEngine;
  private Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex = new HashMap<String, Map<String, Integer>>();


  public void setGrouperDataProviderSync(GrouperDataProviderSync grouperDataProviderSync) {
    this.grouperDataProviderSync = grouperDataProviderSync;
  }
  
  public void setGrouperDataProvider(GrouperDataProvider grouperDataProvider) {
    this.grouperDataProvider = grouperDataProvider;
  }

  /**
   * @return the stem name
   */
  public static String dataProviderStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":dataProvider";
  }
  
  /**
   * @return the group name
   */
  public static String dataProviderSubjectListSyncAllowedGroupName() {
    return dataProviderStemName() + ":" + "subjectListSyncAllowedGroup";
  }
  
  /**
   * run a full sync for this data provider.
   * retrieves distinct subject ids from all source queries and from grouper,
   * unions them into one sorted list, and processes in batches of configurable size.
   * each batch loads source data and grouper data for that range, compares,
   * and writes changes. after all batches, any missed subject ids are processed
   * as a straggler pass.
   */
  public void syncFull() {

    if (!syncFullSetup()) {
      return;
    }

    syncFullValidateQueryFieldConfigs();

    // get the configured batch size for this provider
    int subjectIdBatchSize = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouperDataProvider." + grouperDataProviderSync.getConfigId() + ".fullSyncSubjectIdBatchSize", 10000);

    // collect distinct lowercased subject ids from all source queries
    Set<String> allSubjectIdsFromSource = new TreeSet<>();
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      List<String> querySubjectIds = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectDistinctSubjectIds();
      allSubjectIdsFromSource.addAll(querySubjectIds);
    }
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // collect distinct lowercased subject ids from existing grouper data
    List<String> grouperSubjectIds = GrouperDAOFactory.getFactory().getMember().selectDistinctSubjectIdsByDataProvider(grouperDataProvider.getInternalId());
    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // union source and grouper subject ids into one sorted list
    Set<String> allSubjectIds = new TreeSet<>();
    allSubjectIds.addAll(allSubjectIdsFromSource);
    allSubjectIds.addAll(grouperSubjectIds);
    List<String> sortedSubjectIds = new ArrayList<>(allSubjectIds);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Data provider " + grouperDataProviderSync.getConfigId() + " full sync: "
          + sortedSubjectIds.size() + " distinct subject ids (" + allSubjectIdsFromSource.size()
          + " from source, " + grouperSubjectIds.size() + " from grouper), batch size " + subjectIdBatchSize);
    }

    // failsafe check #1: if too many grouper subject ids are missing from source, abort early.
    // only runs if failsafeMinSubjectCount is configured (opt-in).
    Integer failsafeMinSubjectCount = grouperDataProviderSync.getFailsafeMinSubjectCount();
    if (failsafeMinSubjectCount != null && grouperSubjectIds.size() >= failsafeMinSubjectCount && grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove() != null
        && grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove() >= 0) {
      Set<String> grouperSubjectIdsMissingFromSource = new TreeSet<>(grouperSubjectIds);
      grouperSubjectIdsMissingFromSource.removeAll(allSubjectIdsFromSource);
      double percentMissing = (100.0 * grouperSubjectIdsMissingFromSource.size()) / grouperSubjectIds.size();

      if (percentMissing > grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove()) {
        boolean isFailsafeApproved = GrouperFailsafe.isApproved(grouperDataProviderSync.getJobName());
        if (!isFailsafeApproved) {
          Map<String, Object> failsafeDebug = new LinkedHashMap<>();
          failsafeDebug.put("grouperSubjectIdCount", grouperSubjectIds.size());
          failsafeDebug.put("sourceSubjectIdCount", allSubjectIdsFromSource.size());
          failsafeDebug.put("missingSubjectIdCount", grouperSubjectIdsMissingFromSource.size());
          failsafeDebug.put("percentMissing", String.format("%.2f", percentMissing));
          failsafeDebug.put("percentAllowedToBeRemoved", grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove());
          grouperDataProviderSync.getDebugMap().putAll(failsafeDebug);
          GrouperFailsafe.assignFailed(grouperDataProviderSync.getJobName());
          throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, "Aborting due to too many grouper subject ids missing from source: " + failsafeDebug);
        }
      }
    }

    // track all subject ids we successfully retrieve from source, to find stragglers
    Set<String> retrievedSubjectIds = new HashSet<>();

    // accumulate counts across batches for failsafe and reporting
    ChangeState accumulatedState = new ChangeState();

    // process subject ids in batches
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(sortedSubjectIds.size(), subjectIdBatchSize, false);
    for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {

      List<String> batchSubjectIds = GrouperUtil.batchList(sortedSubjectIds, subjectIdBatchSize, batchIndex);
      String fromSubjectIdLower = batchSubjectIds.get(0);
      String toSubjectIdLower = batchSubjectIds.get(batchSubjectIds.size() - 1);

      if (LOG.isDebugEnabled()) {
        LOG.debug("Data provider " + grouperDataProviderSync.getConfigId() + " processing batch "
            + (batchIndex + 1) + " of " + numberOfBatches + " (range: " + fromSubjectIdLower + " to " + toSubjectIdLower + ")");
      }

      // create a fresh index for this batch and re-populate field/row wrapper maps
      dataEngine.setGrouperDataProviderIndex(new GrouperDataProviderIndex());
      dataEngine.loadFieldsAndRows(null);

      // load grouper data for members in this subject id range
      syncFullLoadGrouperDataForRange(fromSubjectIdLower, toSubjectIdLower);

      // retrieve source data for this range
      syncFullRetrieveSourceDataForRange(fromSubjectIdLower, toSubjectIdLower, retrievedSubjectIds);

      // compare and write changes (failsafe check happens inside, using accumulated totals)
      ChangeState batchState = calculateAndStoreChanges(queryConfigIdToLowerColumnNameToZeroIndex, true, accumulatedState);

      // accumulate counts across batches
      accumulatedState.totalFieldAssignsInGrouper += batchState.totalFieldAssignsInGrouper;
      accumulatedState.totalFieldAssignsToRemove += batchState.totalFieldAssignsToRemove;
      accumulatedState.numberOfDuplicateRowKeysFoundInSource += batchState.numberOfDuplicateRowKeysFoundInSource;
      accumulatedState.fieldAssignInsertCount += batchState.fieldAssignInsertCount;
      accumulatedState.fieldAssignDeleteCount += batchState.fieldAssignDeleteCount;
      accumulatedState.rowAssignInsertCount += batchState.rowAssignInsertCount;
      accumulatedState.rowAssignDeleteCount += batchState.rowAssignDeleteCount;

      // save the loader log after each batch so counts are visible during the sync
      if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
        grouperDataProviderSync.getHib3GrouperLoaderLog().store();
      }

      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }

    // straggler pass: find subject ids from the source that weren't retrieved in any range query
    Set<String> missedSubjectIds = new TreeSet<>(allSubjectIdsFromSource);
    missedSubjectIds.removeAll(retrievedSubjectIds);

    if (missedSubjectIds.size() > 0) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Data provider " + grouperDataProviderSync.getConfigId() + " straggler pass: " + missedSubjectIds.size() + " missed subject ids");
      }

      // create a fresh index for the straggler pass and re-populate field/row wrapper maps
      dataEngine.setGrouperDataProviderIndex(new GrouperDataProviderIndex());
      dataEngine.loadFieldsAndRows(null);

      syncFullLoadGrouperDataForSubjectIds(new ArrayList<>(missedSubjectIds));

      syncFullRetrieveSourceDataForSubjectIds(new ArrayList<>(missedSubjectIds));

      ChangeState stragglerState = calculateAndStoreChanges(queryConfigIdToLowerColumnNameToZeroIndex, true, accumulatedState);

      accumulatedState.totalFieldAssignsInGrouper += stragglerState.totalFieldAssignsInGrouper;
      accumulatedState.numberOfDuplicateRowKeysFoundInSource += stragglerState.numberOfDuplicateRowKeysFoundInSource;
      accumulatedState.fieldAssignInsertCount += stragglerState.fieldAssignInsertCount;
      accumulatedState.fieldAssignDeleteCount += stragglerState.fieldAssignDeleteCount;
      accumulatedState.rowAssignInsertCount += stragglerState.rowAssignInsertCount;
      accumulatedState.rowAssignDeleteCount += stragglerState.rowAssignDeleteCount;
    }

    calculateReportDuplicateRowKeys(accumulatedState);
    calculateReportChangeCounts(accumulatedState);

    // save debug map for testing
    testingDebugMap = grouperDataProviderSync.getDebugMap();
  }

  /**
   * initialize the data engine, sync config from db, load the data provider,
   * fields, and rows. does not load per-member data (that happens per batch).
   * @return true if setup succeeded, false if provider config not found
   */
  private boolean syncFullSetup() {
    String dataProviderConfigId = grouperDataProviderSync.getConfigId();

    if (grouperDataProviderSync.getGrouperDataEngine() == null) {
      grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    }

    this.dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataEngine.syncDataProviders(grouperConfig);
    GrouperDataEngine.syncDataFields(grouperConfig);
    GrouperDataEngine.syncDataRows(grouperConfig);
    GrouperDataEngine.syncDataAliases(grouperConfig);

    this.grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);

    dataEngine.loadFieldsAndRows(grouperConfig);

    if (!dataEngine.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      grouperDataProviderSync.getDebugMap().put("dataProviderConfigNotFound", dataProviderConfigId);
      return false;
    }

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    return true;
  }

  /**
   * load existing grouper data from the database for members whose lowercased subject id
   * falls within the given range. loads dictionary text, member wrappers, field assigns,
   * row assigns, row field assigns, and indexes them by member.
   * @param fromSubjectIdLower lower bound (inclusive), lowercased
   * @param toSubjectIdLower upper bound (inclusive), lowercased
   */
  private void syncFullLoadGrouperDataForRange(String fromSubjectIdLower, String toSubjectIdLower) {

    Set<Long> memberInternalIds = GrouperDAOFactory.getFactory().getMember()
        .selectByDataProviderAndSubjectIdRange(grouperDataProvider.getInternalId(), fromSubjectIdLower, toSubjectIdLower);

    syncFullLoadGrouperDataForMemberInternalIds(memberInternalIds);
  }

  /**
   * load existing grouper data from the database for specific subject ids (straggler pass).
   * resolves subject ids to member internal ids, then loads their data.
   * @param subjectIdsLower lowercased subject ids to load
   */
  private void syncFullLoadGrouperDataForSubjectIds(List<String> subjectIdsLower) {

    // look up member internal ids for these subject ids by querying the member table
    Set<Long> memberInternalIds = new HashSet<>();
    int batchSize = 800;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectIdsLower.size(), batchSize, true);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> batch = GrouperUtil.batchList(subjectIdsLower, batchSize, i);
      StringBuilder sql = new StringBuilder("select gm.internal_id from grouper_members gm where lower(gm.subject_id) in (");
      GrouperClientUtils.appendQuestions(sql, batch.size());
      sql.append(") and gm.internal_id in ("
          + "select gdfa.member_internal_id from grouper_data_field_assign gdfa where gdfa.data_provider_internal_id = ? "
          + "union select gdra.member_internal_id from grouper_data_row_assign gdra where gdra.data_provider_internal_id = ?)");
      GcDbAccess gcDbAccess = new GcDbAccess();
      for (String subjectId : batch) {
        gcDbAccess.addBindVar(subjectId);
      }
      gcDbAccess.addBindVar(grouperDataProvider.getInternalId());
      gcDbAccess.addBindVar(grouperDataProvider.getInternalId());
      memberInternalIds.addAll(GrouperUtil.nonNull(gcDbAccess.sql(sql.toString()).selectList(Long.class)));
    }

    syncFullLoadGrouperDataForMemberInternalIds(memberInternalIds);
  }

  /**
   * load existing grouper data from the database for a set of member internal ids.
   * loads dictionary text, creates member wrappers, loads field/row/row-field assigns,
   * and indexes by member. operates on the current GrouperDataProviderIndex.
   * @param memberInternalIds the member internal ids to load data for
   */
  private void syncFullLoadGrouperDataForMemberInternalIds(Set<Long> memberInternalIds) {

    if (memberInternalIds.isEmpty()) {
      return;
    }

    // load dictionary text for these members
    Map<Long, String> dictionariesByDataProvider = GrouperDictionaryDao.selectByDataProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds);
    dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().putAll(dictionariesByDataProvider);
    for (Map.Entry<Long, String> entry : dictionariesByDataProvider.entrySet()) {
      dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().put(entry.getValue(), entry.getKey());
    }

    // create member wrappers
    for (Long memberInternalId : memberInternalIds) {
      GrouperDataMemberWrapper grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
      dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
    }

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // load field assignments
    List<GrouperDataFieldAssign> grouperDataFieldAssigns = GrouperDataFieldAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds);
    processDataFieldAssignWrappers(grouperDataFieldAssigns);

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // load row assignments
    List<GrouperDataRowAssign> grouperDataRowAssigns = GrouperUtil.nonNull(GrouperDataRowAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds));
    processDataRowAssignWrappers(grouperDataRowAssigns);

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    // load row field assignments
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = GrouperUtil.nonNull(GrouperDataRowFieldAssignDao.selectByProviderAndMembers(grouperDataProvider.getInternalId(), memberInternalIds));
    processDataRowFieldAssignWrappers(grouperDataRowFieldAssigns);

    GrouperDaemonUtils.stopProcessingIfJobPaused();

    indexDataByMember();
  }

  /**
   * retrieve source data for subjects in the given range from all configured queries.
   * uses selectDataBySubjectIdRange on each query's DAO, then resolves subjects
   * and assigns rows to member wrappers.
   * @param fromSubjectIdLower lower bound (inclusive), lowercased
   * @param toSubjectIdLower upper bound (inclusive), lowercased
   * @param retrievedSubjectIds set to track which subject ids were successfully retrieved (updated in place)
   */
  private void syncFullRetrieveSourceDataForRange(String fromSubjectIdLower, String toSubjectIdLower, Set<String> retrievedSubjectIds) {

    Map<GrouperDataProviderQuery, List<Object[]>> grouperDataProviderQueryToRows = new LinkedHashMap<>();

    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId());
      if (lowerColumnNameToZeroIndex == null) {
        lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
        queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);
      }

      List<Object[]> rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao()
          .selectDataBySubjectIdRange(lowerColumnNameToZeroIndex, fromSubjectIdLower, toSubjectIdLower);

      grouperDataProviderQueryToRows.put(grouperDataProviderQuery, rows);
    }

    retrieveSourceDataProcessRows(queryConfigIdToLowerColumnNameToZeroIndex, grouperDataProviderQueryToRows, true, retrievedSubjectIds);

    GrouperDaemonUtils.stopProcessingIfJobPaused();
  }

  /**
   * retrieve source data for specific subject ids from all configured queries (straggler pass).
   * uses selectDataBySubjectIds on each query's DAO, then resolves subjects
   * and assigns rows to member wrappers.
   * @param subjectIdsLower lowercased subject ids to retrieve
   */
  private void syncFullRetrieveSourceDataForSubjectIds(List<String> subjectIdsLower) {

    Map<GrouperDataProviderQuery, List<Object[]>> grouperDataProviderQueryToRows = new LinkedHashMap<>();

    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId());
      if (lowerColumnNameToZeroIndex == null) {
        lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
        queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);
      }

      List<Object[]> rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao()
          .selectDataBySubjectIds(lowerColumnNameToZeroIndex, subjectIdsLower);

      grouperDataProviderQueryToRows.put(grouperDataProviderQuery, rows);
    }

    retrieveSourceDataProcessRows(queryConfigIdToLowerColumnNameToZeroIndex, grouperDataProviderQueryToRows, true, null);

    GrouperDaemonUtils.stopProcessingIfJobPaused();
  }

  /**
   * validate that all data field configIds referenced by query field configs exist.
   * throws RuntimeException if a referenced data field config is missing.
   */
  private void syncFullValidateQueryFieldConfigs() {
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();
      for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : GrouperUtil.nonNull(grouperDataProviderQueryConfig.getGrouperDataProviderQueryFieldConfigs())) {
        String dataFieldConfigId = grouperDataProviderQueryFieldConfig.getProviderDataFieldConfigId();
        if (!StringUtils.isBlank(dataFieldConfigId) && !dataEngine.getFieldConfigByConfigId().containsKey(dataFieldConfigId)) {
          throw new RuntimeException("Data field config 'grouperDataField." + dataFieldConfigId
              + ".*' not found, referenced by grouperDataProviderQuery." + grouperDataProviderQueryConfig.getConfigId()
              + ". Fix the data provider query config or add the missing data field config.");
        }
      }
    }
  }
  
  /**
   * run an incremental sync for this data provider.
   * queries the change log to find subjects that changed since the last sync,
   * then loads and processes only those subjects.
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
    
    boolean isSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).isSubjectSource();
    String subjectSourceIdIfSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).getSubjectSourceId();
    
    if (isSubjectSource && StringUtils.isBlank(subjectSourceIdIfSubjectSource)) {
      throw new RuntimeException("subjectSourceId is not specified for " + grouperDataProviderSync.getConfigId());
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
    
    if (changesFromTimestamp != null) {
      // subtract 100ms just in case there are small commit delays
      changesFromTimestamp = new Timestamp(changesFromTimestamp.getTime() - 100);
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
        continue;
      }
      
      String subjectIdAttribute = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectSourceId();
      String subjectIdType = grouperDataProviderChangeLogQueryConfig.getProviderChangeLogQuerySubjectIdType();
      Integer subjectIdZeroIndex = changeLogQueryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderChangeLogQueryConfig.getConfigId()).get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      if (!"subjectId".equals(subjectIdType) && !"subjectIdentifier".equals(subjectIdType)) {
        throw new RuntimeException("Unexpected providerChangeLogQuerySubjectIdType: " + subjectIdType);
      }
      
      if (isSubjectSource && !"subjectId".equals(subjectIdType)) {
        throw new RuntimeException("subjectIdType type must be subjectId for subject source data providers.");
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
    
    syncIncremental(subjectIds, subjectIdentifiers, sourceToSubjectIds, sourceToSubjectIdentifiers);
  }
  
  /**
   * sync specific subjects for this data provider (e.g. from a web service call).
   * loads config and then delegates to the incremental sync logic for the given subjects.
   * @param subjectIds subject ids to sync (no source specified)
   * @param subjectIdentifiers subject identifiers to sync (no source specified)
   * @param sourceToSubjectIds subject ids keyed by source id
   * @param sourceToSubjectIdentifiers subject identifiers keyed by source id
   */
  public void syncSubjects(Set<String> subjectIds, Set<String> subjectIdentifiers, Map<String, Set<String>> sourceToSubjectIds, Map<String, Set<String>> sourceToSubjectIdentifiers) {
    
    String dataProviderConfigId = grouperDataProviderSync.getConfigId();
    
    if (grouperDataProviderSync.getGrouperDataEngine() == null) {
      grouperDataProviderSync.setGrouperDataEngine(new GrouperDataEngine());
    }
    
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

    GrouperDataProvider grouperDataProvider = GrouperDataProviderDao.selectByText(dataProviderConfigId);
    
    // what are the cases where we'd want to refresh this?
    if (grouperDataProvider == null) {
      throw new RuntimeException("Unable to find data provider: " + dataProviderConfigId);
    }
    
    setGrouperDataProvider(grouperDataProvider);

    dataEngine.loadFieldsAndRows(grouperConfig);

    // maybe things in DB arent in sync with the config yet
    if (!dataEngine.getProviderConfigByConfigId().containsKey(dataProviderConfigId)) {
      throw new RuntimeException("dataProviderConfigNotFound: " + dataProviderConfigId);
    }
    
    boolean isSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).isSubjectSource();
    String subjectSourceIdIfSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).getSubjectSourceId();
    
    if (isSubjectSource && StringUtils.isBlank(subjectSourceIdIfSubjectSource)) {
      throw new RuntimeException("subjectSourceId is not specified for " + grouperDataProviderSync.getConfigId());
    }
    
    syncIncremental(subjectIds, subjectIdentifiers, sourceToSubjectIds, sourceToSubjectIdentifiers);
  }
  
  /**
   * incremental sync logic shared by syncIncremental() and syncSubjects().
   * resolves subjects to members, loads their existing grouper data,
   * retrieves source data, and calculates/stores changes.
   * @param subjectIds subject ids to sync (no source specified)
   * @param subjectIdentifiers subject identifiers to sync (no source specified)
   * @param sourceToSubjectIds subject ids keyed by source id
   * @param sourceToSubjectIdentifiers subject identifiers keyed by source id
   */
  private void syncIncremental(Set<String> subjectIds, Set<String> subjectIdentifiers, Map<String, Set<String>> sourceToSubjectIds, Map<String, Set<String>> sourceToSubjectIdentifiers) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();
    String dataProviderConfigId = grouperDataProviderSync.getConfigId();

    boolean isSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).isSubjectSource();
    String subjectSourceIdIfSubjectSource = dataEngine.getProviderConfigByConfigId().get(dataProviderConfigId).getSubjectSourceId();
    
    // resolve the subjects
    Set<Subject> allSubjects = new LinkedHashSet<Subject>();
    if (GrouperUtil.length(subjectIds) > 0) {
      Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(subjectIds);
      allSubjects.addAll(subjectsByIds.values());
    }
    if (GrouperUtil.length(subjectIdentifiers) > 0) {
      Map<String, Subject> subjectsByIdentifiers = SubjectFinder.findByIdentifiers(subjectIdentifiers);
      allSubjects.addAll(subjectsByIdentifiers.values());
    }
    if (GrouperUtil.length(sourceToSubjectIds) > 0) {
      for (String sourceId : sourceToSubjectIds.keySet()) {
        Set<String> theSubjectIds = sourceToSubjectIds.get(sourceId);
        if (GrouperUtil.length(theSubjectIds) > 0) {
          Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(theSubjectIds, sourceId, true);
          allSubjects.addAll(subjectsByIds.values());
        }
      }
    }
    if (GrouperUtil.length(sourceToSubjectIdentifiers) > 0) {
      for (String sourceId : sourceToSubjectIdentifiers.keySet()) {
        Set<String> theSubjectIdentifiers = sourceToSubjectIdentifiers.get(sourceId);
        if (GrouperUtil.length(theSubjectIdentifiers) > 0) {
          Map<String, Subject> subjectsByIdentitifers = SubjectFinder.findByIdentifiers(theSubjectIdentifiers, sourceId);
          allSubjects.addAll(subjectsByIdentitifers.values());
        }
      }
    }
    
    if (allSubjects.size() == 0) {
      if (isSubjectSource) {
        if (GrouperUtil.length(subjectIds) == 0 && (GrouperUtil.length(sourceToSubjectIds) == 0 || GrouperUtil.length(sourceToSubjectIds.get(subjectSourceIdIfSubjectSource)) == 0)) {
          return;
        }
      } else {
        return;
      }
    }
    
    Set<Member> members = new HashSet<>();
    if (!allSubjects.isEmpty()) {
      members = MemberFinder.findBySubjects(allSubjects, true);
    }

    if (isSubjectSource) {
      createMemberObjects(subjectIds, sourceToSubjectIds == null ? null : sourceToSubjectIds.get(subjectSourceIdIfSubjectSource), members);
      members.addAll(grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMembersToAddBySubjectId().values());
      members.addAll(grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getUnresolvedSubjectsWithMembersBySubjectId().values());
    }
    
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

    ChangeState state = calculateAndStoreChanges(queryConfigIdToLowerColumnNameToZeroIndex, false, null);

    calculateReportDuplicateRowKeys(state);
    calculateReportChangeCounts(state);
  }
  
  /**
   * track an unresolvable subject id in the debug map, up to 50 examples.
   * @param subjectIdValue the subject id that could not be resolved
   */
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
  
  /**
   * wrap field assignments loaded from the database and register them in the provider index.
   * links each assignment to its field wrapper, member wrapper, and dictionary text.
   * @param grouperDataFieldAssigns field assignments to process
   */
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
        
        GrouperUtil.assertion(dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().containsKey(grouperDataFieldAssign.getValueDictionaryInternalId()), 
            "Cant find text: " + grouperDataFieldAssign.getValueDictionaryInternalId());
        grouperDataFieldAssignWrapper.setTextValue(textValue);
      }
    }
  }
  
  /**
   * wrap row assignments loaded from the database and register them in the provider index.
   * links each assignment to its row wrapper and member wrapper.
   * @param grouperDataRowAssigns row assignments to process
   */
  private void processDataRowAssignWrappers(List<GrouperDataRowAssign> grouperDataRowAssigns) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
      GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = new GrouperDataRowAssignWrapper(dataEngine, grouperDataRowAssign);
      
      dataEngine.getGrouperDataProviderIndex().getRowAssignWrapperByInternalId().put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper);
      
      grouperDataRowAssignWrapper.setGrouperDataRowWrapper(dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(grouperDataRowAssign.getDataRowInternalId()));

      grouperDataRowAssignWrapper.setMemberWrapper(dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(grouperDataRowAssign.getMemberInternalId()));
      
    }
  }
  
  /**
   * wrap row field assignments loaded from the database and register them in the provider index.
   * links each assignment to its field wrapper, row assign wrapper, and dictionary text.
   * @param grouperDataRowFieldAssigns row field assignments to process
   */
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
  
  /**
   * reorganize field/row/row-field assign wrappers from the global provider index
   * into per-member structures on each GrouperDataMemberWrapper.  also extracts
   * and converts stored values into the fieldIdToValues map used for comparison
   * against provider data.
   */
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
            if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
              grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
            }
            continue;
          }
          
          // don't delete this here since it wouldn't take into account fail safe
          //if (!grouperDataFieldConfig.isFieldMultiValued() && valueToFieldAssignWrapper.size() >= 1) {
          //  GrouperDataFieldAssignDao.delete(dataFieldAssignWrapper.getGrouperDataFieldAssign());
          //  grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(1);
          //  continue;
          //}
          values.add(value);
          valueToFieldAssignWrapper.put(value, dataFieldAssignWrapper);
        }
      }
    }
  }
  
  /**
   * retrieve data from the external source systems (SQL, LDAP) for all configured queries.
   * resolves subjects from the returned rows, creates member wrappers, and stores the raw
   * row data on each member wrapper keyed by query config id.
   * @param queryConfigIdToLowerColumnNameToZeroIndex populated with column name to index mappings per query
   * @param isFullSync true for full sync (uses selectData), false for incremental (uses selectDataByMembers)
   */
  private void retrieveSourceData(Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex, boolean isFullSync) {

    Map<GrouperDataProviderQuery, List<Object[]>> grouperDataProviderQueryToRows = new LinkedHashMap<>();

    // fetch rows from each query
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = new HashMap<String, Integer>();
      queryConfigIdToLowerColumnNameToZeroIndex.put(grouperDataProviderQueryConfig.getConfigId(), lowerColumnNameToZeroIndex);

      List<Object[]> rows;

      if (isFullSync) {
        rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectData(lowerColumnNameToZeroIndex);
      } else {
        Collection<GrouperDataMemberWrapper> grouperDataMemberWrappers = grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMemberWrapperByInternalId().values();
        Set<Member> members = new HashSet<Member>();
        for (GrouperDataMemberWrapper grouperDataMemberWrapper : grouperDataMemberWrappers) {
          if (grouperDataMemberWrapper.getMember() != null) {
            members.add(grouperDataMemberWrapper.getMember());
          }
        }
        rows = grouperDataProviderQuery.retrieveGrouperDataProviderQueryTargetDao().selectDataByMembers(lowerColumnNameToZeroIndex, members);
      }
      grouperDataProviderQueryToRows.put(grouperDataProviderQuery, rows);
    }

    retrieveSourceDataProcessRows(queryConfigIdToLowerColumnNameToZeroIndex, grouperDataProviderQueryToRows, isFullSync, null);
  }

  /**
   * retrieve source data using pre-fetched rows (used by the full sync batch loop).
   * @param queryConfigIdToLowerColumnNameToZeroIndex column name to index mappings per query
   * @param grouperDataProviderQueryToRows pre-fetched rows keyed by query
   * @param isFullSync true for full sync
   * @param retrievedSubjectIds if non-null, lowercased subject ids found in the rows are added to this set
   */
  private void retrieveSourceDataProcessRows(
      Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex,
      Map<GrouperDataProviderQuery, List<Object[]>> grouperDataProviderQueryToRows,
      boolean isFullSync,
      Set<String> retrievedSubjectIds) {

    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    boolean isSubjectSource = dataEngine.getProviderConfigByConfigId().get(grouperDataProviderSync.getConfigId()).isSubjectSource();
    String subjectSourceIdIfSubjectSource = dataEngine.getProviderConfigByConfigId().get(grouperDataProviderSync.getConfigId()).getSubjectSourceId();

    if (isSubjectSource && StringUtils.isBlank(subjectSourceIdIfSubjectSource)) {
      throw new RuntimeException("subjectSourceId is not specified for " + grouperDataProviderSync.getConfigId());
    }

    Map<MultiKey, Subject> subjectIdAttributeSubjectIdSourceIdToSubject = new HashMap<MultiKey, Subject>();

    Map<String, Set<String>> sourceToSubjectIds = new HashMap<String, Set<String>>();
    Map<String, Set<String>> sourceToSubjectIdentifiers = new HashMap<String, Set<String>>();
    Set<String> subjectIds = new HashSet<String>();
    Set<String> subjectIdentifiers = new HashSet<String>();

    // pass one: collect subject ids from all rows
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderQueryToRows.keySet()) {

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId());

      List<Object[]> rows = grouperDataProviderQueryToRows.get(grouperDataProviderQuery);
      
      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute1 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      String sourceIdAttribute2 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId2();
      String sourceIdAttribute3 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId3();
      
      Set<String> sourceIdAttributes = new HashSet<String>();
      if (!StringUtils.isBlank(sourceIdAttribute1)) {
        sourceIdAttributes.add(sourceIdAttribute1);
      }
      if (!StringUtils.isBlank(sourceIdAttribute2)) {
        sourceIdAttributes.add(sourceIdAttribute2);
      }
      if (!StringUtils.isBlank(sourceIdAttribute3)) {
        sourceIdAttributes.add(sourceIdAttribute3);
      }
      
      
      String subjectIdType = grouperDataProviderQueryConfig.getProviderQuerySubjectIdType();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      GrouperUtil.assertion(subjectIdZeroIndex != null, "Cannot find subject id attribute column: " + subjectIdAttribute);

      if (!"subjectId".equals(subjectIdType) && !"subjectIdentifier".equals(subjectIdType)) {
        throw new RuntimeException("Unexpected providerQuerySubjectIdType: " + subjectIdType);
      }
      
      if (isSubjectSource && !"subjectId".equals(subjectIdType)) {
        throw new RuntimeException("subjectIdType type must be subjectId for subject source data providers.");
      }
      
      // loop over the rows and get the subject ids or identifiers and collect them up
      for (Object[] row : rows) {

        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);

        // track which subject ids were actually retrieved from the source
        if (retrievedSubjectIds != null && subjectId != null) {
          retrievedSubjectIds.add(subjectId.toLowerCase());
        }

        if (GrouperUtil.length(sourceIdAttributes) == 0) {
          if (subjectIdType.equals("subjectId")) {
            subjectIds.add(subjectId);         
          } else {
            subjectIdentifiers.add(subjectId);         
          }
        } else {
          for (String sourceIdAttribute : sourceIdAttributes) {
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
    }
    // resolve all the subjects at once depending on the source and type
    if (GrouperUtil.length(subjectIds) > 0) {
      Map<String, Subject> subjectsByIds = SubjectFinder.findByIds(subjectIds);
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
    
    if (isSubjectSource && isFullSync) {
      createMemberObjects(subjectIds, sourceToSubjectIds.get(subjectSourceIdIfSubjectSource), subjectToMember.values());
    }
    
    Set<Long> memberInternalIdsWithSourceRows = new LinkedHashSet<>();
    
    // pass two, assign the data to the members
    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();

      Map<String, Integer> lowerColumnNameToZeroIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId());
      
      List<Object[]> rows = grouperDataProviderQueryToRows.get(grouperDataProviderQuery);

      String subjectIdAttribute = grouperDataProviderQueryConfig.getProviderQuerySubjectIdAttribute().toLowerCase();
      String sourceIdAttribute1 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId();
      String sourceIdAttribute2 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId2();
      String sourceIdAttribute3 = grouperDataProviderQueryConfig.getProviderQuerySubjectSourceId3();
      
      Set<String> sourceIdAttributes = new HashSet<String>();
      String sourceIdForLog = "";
      if (!StringUtils.isBlank(sourceIdAttribute1)) {
        sourceIdAttributes.add(sourceIdAttribute1);
        sourceIdForLog = sourceIdAttribute1;
      }
      if (!StringUtils.isBlank(sourceIdAttribute2)) {
        sourceIdAttributes.add(sourceIdAttribute2);
        if (!StringUtils.isBlank(sourceIdForLog)) {
          sourceIdForLog += ", ";
        }
        sourceIdForLog += sourceIdAttribute2;
      }
      if (!StringUtils.isBlank(sourceIdAttribute3)) {
        sourceIdAttributes.add(sourceIdAttribute3);
        if (!StringUtils.isBlank(sourceIdForLog)) {
          sourceIdForLog += ", ";
        }
        sourceIdForLog += sourceIdAttribute3;
      }
      if (sourceIdAttributes.size() == 0) {
        sourceIdAttributes.add(null);
        sourceIdForLog = "no source id";
      }


      String subjectIdType = grouperDataProviderQueryConfig.getProviderQuerySubjectIdType();
      Integer subjectIdZeroIndex = lowerColumnNameToZeroIndex.get(subjectIdAttribute);
      
      for (Object[] row : rows) {
        
        String subjectId = GrouperUtil.stringValue(row[subjectIdZeroIndex]);
        
        Subject subject = null;
        for (String sourceIdAttribute : sourceIdAttributes) {
          MultiKey multiKey = new MultiKey(subjectIdType, subjectId, sourceIdAttribute);
          
          subject = subjectIdAttributeSubjectIdSourceIdToSubject.get(multiKey);
          if (subject != null) {
            break;
          }
        }
        Member member = subject == null ? null : subjectToMember.get(subject);
        
        if (member == null) {
          if (isSubjectSource) {
            member = grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMembersToAddBySubjectId().get(subjectId);
            if (member == null) {
              member = grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getUnresolvedSubjectsWithMembersBySubjectId().get(subjectId);
            }
          } else {
            LOG.warn("Unable to resolve subject " + subjectId + ", " + sourceIdForLog + ", " + subjectIdType);
            if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
              grouperDataProviderSync.getHib3GrouperLoaderLog().addUnresolvableSubjectCount(1);
            }
            addUnresolvableSubjectToJobMessage(subjectId);
            continue; 
          }
        }

        Long memberInternalId = member.getInternalId();
        
        GrouperDataMemberWrapper grouperDataMemberWrapper = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().get(memberInternalId);
        
        if (grouperDataMemberWrapper == null) {
          grouperDataMemberWrapper = new GrouperDataMemberWrapper(dataEngine, memberInternalId);
          dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().put(memberInternalId, grouperDataMemberWrapper);
        }

        grouperDataMemberWrapper.setMember(member);

        List<Object[]> userRowsforQuery = grouperDataMemberWrapper.getQueryConfigIdToRowData().get(grouperDataProviderQueryConfig.getConfigId());
        if (userRowsforQuery == null) {
          userRowsforQuery = new ArrayList<Object[]>();
          grouperDataMemberWrapper.getQueryConfigIdToRowData().put(grouperDataProviderQueryConfig.getConfigId(), userRowsforQuery);
        }
        
        userRowsforQuery.add(row);
        memberInternalIdsWithSourceRows.add(memberInternalId);
      }
    }

    if (isSubjectSource && !isFullSync) {
      // potentially remove members that we would have added if there wasn't any data
      Iterator<Map.Entry<String, Member>> it = grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMembersToAddBySubjectId().entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, Member> entry = it.next();
        Long internalId = entry.getValue().getInternalId();
        if (!memberInternalIdsWithSourceRows.contains(internalId)) {
          it.remove();
        }
      }      
    }
  }
  
  private void createMemberObjects(Set<String> subjectIds1, Set<String> subjectIds2, Collection<Member> membersFound) {
    String subjectSourceIdIfSubjectSource = grouperDataProviderSync.getGrouperDataEngine().getProviderConfigByConfigId().get(grouperDataProviderSync.getConfigId()).getSubjectSourceId();
    Source source = SubjectFinder.getSource(subjectSourceIdIfSubjectSource);
    String sourceType = source.getSubjectTypes().iterator().next().getName();
    
    Set<String> subjectIdsToAdd = new LinkedHashSet<>();
    if (subjectIds1 != null) {
      subjectIdsToAdd.addAll(subjectIds1);
    }
    
    if (subjectIds2 != null) {
      subjectIdsToAdd.addAll(subjectIds2);
    }
          
    Set<String> allSubjectIdsFoundInMembersTable = new LinkedHashSet<>();
    for (Member member : membersFound) {
      allSubjectIdsFoundInMembersTable.add(member.getSubjectId());
    }
          
    subjectIdsToAdd.removeAll(allSubjectIdsFoundInMembersTable);
    
    Set<Member> unresolvedSubjectsWithMemberObjects = GrouperDAOFactory.getFactory().getMember().findBySubjectIds(subjectIdsToAdd, subjectSourceIdIfSubjectSource);
    for (Member member : unresolvedSubjectsWithMemberObjects) {
      subjectIdsToAdd.remove(member.getSubjectId());
      grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getUnresolvedSubjectsWithMembersBySubjectId().put(member.getSubjectId(), member);
    }
    
    if (subjectIdsToAdd.size() > 0) {
      List<Long> idIndexes = TableIndex.reserveIds(TableIndexType.member, subjectIdsToAdd.size());
      List<Long> internalIds = TableIndex.reserveIds(TableIndexType.memberInternalId, subjectIdsToAdd.size());
      
      int count = 0;
      
      for (String subjectIdToAdd : subjectIdsToAdd) {
        
        Member member = new Member();
        member.setSubjectIdDb(subjectIdToAdd);
        member.setSubjectSourceIdDb(subjectSourceIdIfSubjectSource);
        member.setSubjectTypeId(sourceType);
        member.setUuid(GrouperUuid.getUuid());
        member.setIdIndex(idIndexes.get(count));
        member.setInternalId(internalIds.get(count));
        grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMembersToAddBySubjectId().put(member.getSubjectId(), member);
        
        count++;
      }
    }    
  }


  /**
   * holds per-call state for calculateAndStoreChanges.
   * these collections are populated during the member comparison loop and consumed
   * during the write phase. they are created fresh for each call (each subject ID batch).
   */
  private static class ChangeState {
    // field assign deletes and their change log entries
    Map<Long, GrouperDataFieldAssign> fieldAssignIdToFieldAssignsToDelete = new LinkedHashMap<>();
    Map<Long, ChangeLogEntryTemp> fieldAssignIdToChangeLogDelete = new LinkedHashMap<>();

    // field assign inserts and their change log entries
    List<GrouperDataFieldAssign> fieldAssignsToInsert = new ArrayList<>();
    Map<Long, ChangeLogEntryTemp> fieldAssignIdToChangeLogInsert = new LinkedHashMap<>();

    // row field assign deletes and their change log entries (keyed by row assign internal id)
    Map<Long, List<GrouperDataRowFieldAssign>> rowAssignIdToRowFieldAssignsToDelete = new LinkedHashMap<>();
    Map<Long, Set<ChangeLogEntryTemp>> rowAssignIdToChangeLogRowFieldDelete = new LinkedHashMap<>();

    // row assign deletes and their change log entries
    Map<Long, GrouperDataRowAssign> rowAssignIdToRowAssignsToDelete = new LinkedHashMap<>();
    Map<Long, ChangeLogEntryTemp> rowAssignIdToChangeLogRowDelete = new LinkedHashMap<>();

    // row field assign inserts and their change log entries
    List<GrouperDataRowFieldAssign> rowFieldAssignsToInsert = new ArrayList<>();
    Map<Long, Set<ChangeLogEntryTemp>> rowAssignIdToChangeLogRowFieldInsert = new LinkedHashMap<>();

    // row assign inserts and their change log entries
    List<GrouperDataRowAssign> rowAssignsToInsert = new ArrayList<>();
    Map<Long, ChangeLogEntryTemp> rowAssignIdToChangeLogRowInsert = new LinkedHashMap<>();

    // row assigns that need last_updated changed (field added/removed on existing row)
    Map<Long, GrouperDataRowAssign> rowAssignIdToRowAssignsToUpdate = new LinkedHashMap<>();

    // maps to track which member owns each field/row assign (used to group writes by member)
    Map<Long, Long> fieldAssignIdToMemberInternalId = new LinkedHashMap<>();
    Map<Long, Long> rowAssignIdToMemberInternalId = new LinkedHashMap<>();

    // keyed insert maps - populated by calculateGenerateInternalIdsAndChangeLog after IDs are assigned
    Map<Long, GrouperDataFieldAssign> fieldAssignIdToFieldAssignsToInsertKeyed;
    Map<Long, GrouperDataRowAssign> rowAssignIdToRowAssignsToInsertKeyed;
    Map<Long, List<GrouperDataRowFieldAssign>> rowAssignIdToRowFieldAssignsToInsertKeyed;

    // history maps - populated by calculateGenerateHistory
    Map<Long, GrouperDataFieldAssignHst> fieldAssignIdToFieldAssignHstsToInsert = new LinkedHashMap<>();
    Map<Long, GrouperDataRowAssignHst> rowAssignIdToRowAssignHstsToInsert = new LinkedHashMap<>();
    Map<Long, List<GrouperDataRowFieldAssignHst>> rowAssignIdToRowFieldAssignHstsToInsert = new LinkedHashMap<>();

    // per-batch counters
    long totalFieldAssignsInGrouper = 0;
    long totalFieldAssignsToRemove = 0;
    int numberOfDuplicateRowKeysFoundInSource = 0;
    int fieldAssignInsertCount = 0;
    int fieldAssignDeleteCount = 0;
    int rowAssignInsertCount = 0;
    int rowAssignDeleteCount = 0;
  }

  /**
   * compare provider source data against existing Grouper data for each member in the
   * current GrouperDataProviderIndex, compute inserts/deletes/updates, and write them
   * to the database. reads member wrappers, field/row assigns, and dictionary text
   * from the index. the caller is responsible for populating the index before calling
   * this method and for creating a fresh index for each subject ID batch.
   *
   * returns the ChangeState so the caller can accumulate counts across batches
   * for failsafe checks and reporting.
   *
   * @param queryConfigIdToLowerColumnNameToZeroIndex mapping of query config ID to column name to
   *   zero-based index in the result row array
   * @param isFullSync true for full sync (enables failsafe checks), false for incremental
   * @param accumulatedState accumulated counts from previous batches, used for failsafe check.
   *   may be null if this is the only call (e.g. incremental sync).
   * @return the ChangeState with per-batch counters
   */
  private ChangeState calculateAndStoreChanges(Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex, boolean isFullSync, ChangeState accumulatedState) {
    GrouperDataEngine dataEngine = grouperDataProviderSync.getGrouperDataEngine();

    ChangeState state = new ChangeState();

    // convert raw query data to typed field/row values, then compare
    // provider data against existing Grouper data to compute inserts and deletes
    for (GrouperDataMemberWrapper grouperDataMemberWrapper : dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId().values()) {
      calculateConvertSourceDataForMember(grouperDataMemberWrapper, queryConfigIdToLowerColumnNameToZeroIndex, dataEngine);
      calculateCompareMemberData(grouperDataMemberWrapper, state, dataEngine);
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }

    // compute total field assigns to remove for this batch
    state.totalFieldAssignsToRemove = state.fieldAssignIdToFieldAssignsToDelete.size();
    for (Long rowAssignInternalId : state.rowAssignIdToRowFieldAssignsToDelete.keySet()) {
      state.totalFieldAssignsToRemove += state.rowAssignIdToRowFieldAssignsToDelete.get(rowAssignInternalId).size();
    }

    // failsafe check #2: per-batch check - if too many of this batch's field assigns are being removed
    calculateCheckFailsafe(state.totalFieldAssignsInGrouper, state.totalFieldAssignsToRemove, isFullSync,
        "Aborting due to too many field assigns being removed in batch");

    // failsafe check #3: running total check - if too many across all batches so far
    if (accumulatedState != null) {
      long runningTotalInGrouper = state.totalFieldAssignsInGrouper + accumulatedState.totalFieldAssignsInGrouper;
      long runningTotalToRemove = state.totalFieldAssignsToRemove + accumulatedState.totalFieldAssignsToRemove;
      calculateCheckFailsafe(runningTotalInGrouper, runningTotalToRemove, isFullSync,
          "Aborting due to too many field assigns being removed across all batches");
    }

    if (grouperDataProviderSync.isReadOnly()) {
      calculateTrackReadOnlyCounts(state);
      return state;
    }

    calculateGenerateInternalIdsAndChangeLog(state, dataEngine);

    calculateGenerateHistory(state, dataEngine);

    calculateWriteChanges(state, dataEngine);

    return state;
  }

  /**
   * for a single member, convert raw query result rows (Object[]) into typed field/row values
   * stored on the member wrapper. resolves dictionary text for string values.
   * @param grouperDataMemberWrapper the member to process
   * @param queryConfigIdToLowerColumnNameToZeroIndex column index mappings per query
   * @param dataEngine the data engine
   */
  private void calculateConvertSourceDataForMember(GrouperDataMemberWrapper grouperDataMemberWrapper,
      Map<String, Map<String, Integer>> queryConfigIdToLowerColumnNameToZeroIndex,
      GrouperDataEngine dataEngine) {

    Set<String> needsDictionaryText = new HashSet<String>();

    for (GrouperDataProviderQuery grouperDataProviderQuery : grouperDataProviderSync.retrieveGrouperDataProviderQueries()) {

      GrouperDataProviderQueryConfig grouperDataProviderQueryConfig = grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig();
      String queryConfigId = grouperDataProviderQueryConfig.getConfigId();

      List<Object[]> providerRows = GrouperUtil.nonNull(grouperDataMemberWrapper.getQueryConfigIdToRowData().get(queryConfigId));
      if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
        grouperDataProviderSync.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(providerRows));
      }

      String rowConfigId = grouperDataProviderQueryConfig.getProviderQueryRowConfigId();

      GrouperDataRowWrapper grouperDataRowWrapper = null;
      List<Map<Long, List<Object>>> rowsOfFieldInternalIdToValues = null;

      if (!StringUtils.isBlank(rowConfigId)) {
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
          if (dataEngine.getRowConfigByConfigId().get(rowConfigId).isOneRowPerSubject()) {

            if (rowsOfFieldInternalIdToValues.size() == 0) {
              rowsOfFieldInternalIdToValues.add(new HashMap<Long, List<Object>>());
            }

            rowDataFieldInternalIdToValues = rowsOfFieldInternalIdToValues.get(0);
          } else {
            rowDataFieldInternalIdToValues = new HashMap<>();
            rowsOfFieldInternalIdToValues.add(rowDataFieldInternalIdToValues);
          }
        }

        for (GrouperDataProviderQueryFieldConfig grouperDataProviderQueryFieldConfig : GrouperUtil.nonNull(grouperDataProviderQueryFieldConfigs)) {

          GrouperDataProviderQueryFieldMappingType providerDataFieldMappingType =
              grouperDataProviderQueryFieldConfig.getProviderDataFieldMappingType();

          if (providerDataFieldMappingType == null) {
            continue;
          }

          if (providerDataFieldMappingType == GrouperDataProviderQueryFieldMappingType.attribute) {

            String columnNameLowerCase = grouperDataProviderQueryFieldConfig.getProviderDataFieldAttributeLowerCase();
            String dataFieldConfigId = grouperDataProviderQueryFieldConfig.getProviderDataFieldConfigId();

            GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(dataFieldConfigId);

            GrouperDataFieldWrapper grouperDataFieldWrapper = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(dataFieldConfigId);

            Integer rowIndex = queryConfigIdToLowerColumnNameToZeroIndex.get(grouperDataProviderQueryConfig.getConfigId()).get(columnNameLowerCase);
            if (rowIndex == null) {
              throw new RuntimeException("Unable to find index for configId=" + grouperDataProviderQueryConfig.getConfigId() + ", columnName=" + columnNameLowerCase);
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

                  if (!grouperDataProviderQueryConfig.isStoreNulls()) {
                    if (currentValue == Void.TYPE || (grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string && currentValue instanceof String && ((String)currentValue).isBlank())) {
                      continue;
                    }
                  }

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

              if (!grouperDataProviderQueryConfig.isStoreNulls()) {
                if (value == Void.TYPE || (grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string && value instanceof String && ((String)value).isBlank())) {
                  continue;
                }
              }

              if (value != null && value != Void.TYPE && grouperDataFieldConfig.getFieldDataType() == GrouperDataFieldType.string) {
                needsDictionaryText.add((String)value);
              }

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

    // resolve any new dictionary text values
    needsDictionaryText.removeAll(dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().keySet());

    if (needsDictionaryText.size() > 0) {
      Map<String, Long> dictionaryTextToInternalId = GrouperDictionaryDao.findOrAdd(needsDictionaryText);

      for (String text : needsDictionaryText) {
        Long internalId = dictionaryTextToInternalId.get(text);
        dataEngine.getGrouperDataProviderIndex().getDictionaryTextByInternalId().put(internalId, text);
        dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString().put(text, internalId);
      }
    }
  }

  /**
   * for a single member, compare provider data against existing Grouper data and populate
   * the change state with inserts, deletes, and updates for both field assigns and row assigns.
   * @param grouperDataMemberWrapper the member to compare
   * @param state the change state to populate
   * @param dataEngine the data engine
   */
  private void calculateCompareMemberData(GrouperDataMemberWrapper grouperDataMemberWrapper, ChangeState state, GrouperDataEngine dataEngine) {

    // compare field assigns
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

        state.totalFieldAssignsInGrouper += dataFromGrouper.size();

        if (dataFromProvider.size() > 1 && !grouperDataFieldConfig.isFieldMultiValued()) {
          throw new RuntimeException("Found multiple values from provider for field with configId=" + grouperDataFieldConfig.getConfigId() + " and memberInternalId=" + grouperDataMemberWrapper.getInternalId());
        }

        Set<Object> dataToDelete = new HashSet<>(dataFromGrouper);
        dataToDelete.removeAll(dataFromProvider);

        for (Object value : dataToDelete) {
          GrouperDataFieldAssignWrapper grouperDataFieldAssignWrapper = valueToFieldAssignWrapper.get(value);

          GrouperDataFieldAssign grouperDataFieldAssign = grouperDataFieldAssignWrapper.getGrouperDataFieldAssign();

          state.fieldAssignIdToFieldAssignsToDelete.put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssign);
          state.fieldAssignIdToMemberInternalId.put(grouperDataFieldAssign.getInternalId(), grouperDataFieldAssign.getMemberInternalId());

          Long valueOrInternalId = grouperDataFieldAssign.getValueInteger() != null ?
              grouperDataFieldAssign.getValueInteger()
              : grouperDataFieldAssign.getValueDictionaryInternalId();

          state.fieldAssignIdToChangeLogDelete.put(grouperDataFieldAssign.getInternalId(),
              new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_FIELD_ASSIGN_DELETE,
              ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.id.name(),
              GrouperUtil.stringValue(grouperDataFieldAssign.getInternalId()),
              ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.dataFieldInternalId.name(),
              GrouperUtil.stringValue(grouperDataFieldAssign.getDataFieldInternalId()),
              ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.memberInternalId.name(),
              GrouperUtil.stringValue(grouperDataFieldAssign.getMemberInternalId()),
              ChangeLogLabels.DATA_FIELD_ASSIGN_DELETE.valueOrInternalId.name(),
              GrouperUtil.stringValue(valueOrInternalId)));

          if (state.fieldAssignDeleteCount < MAX_CHANGE_EXAMPLES) {
            String subjectId = grouperDataMemberWrapper.getMember() != null ? grouperDataMemberWrapper.getMember().getSubjectId() : String.valueOf(grouperDataMemberWrapper.getInternalId());
            grouperDataProviderSync.getDebugMap().put("fieldAssignDelete_" + state.fieldAssignDeleteCount, "subjectId=" + subjectId + ", field=" + grouperDataFieldConfig.getConfigId() + ", value=" + GrouperUtil.stringValue(value));
          }
          state.fieldAssignDeleteCount++;

        }

        Set<Object> dataToInsert = new HashSet<>(dataFromProvider);
        dataToInsert.removeAll(dataFromGrouper);

        for (Object value : dataToInsert) {
          GrouperDataFieldAssign grouperDataFieldAssign = new GrouperDataFieldAssign();
          grouperDataFieldAssign.setDataFieldInternalId(dataFieldInternalId);
          grouperDataFieldAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
          grouperDataFieldAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());
          grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataFieldAssign, value, dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());
          state.fieldAssignsToInsert.add(grouperDataFieldAssign);

          if (state.fieldAssignInsertCount < MAX_CHANGE_EXAMPLES) {
            String subjectId = grouperDataMemberWrapper.getMember() != null ? grouperDataMemberWrapper.getMember().getSubjectId() : String.valueOf(grouperDataMemberWrapper.getInternalId());
            grouperDataProviderSync.getDebugMap().put("fieldAssignInsert_" + state.fieldAssignInsertCount, "subjectId=" + subjectId + ", field=" + grouperDataFieldConfig.getConfigId() + ", value=" + GrouperUtil.stringValue(value));
          }
          state.fieldAssignInsertCount++;
        }
      }
    }

    // compare row assigns
    Set<Long> dataRowInternalIds = new HashSet<Long>();
    dataRowInternalIds.addAll(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().keySet());
    Map<Long, List<GrouperDataRowAssignWrapper>> rowAssignWrappersByRowInternalId = grouperDataMemberWrapper.getRowAssignWrappersByRowInternalId();
    dataRowInternalIds.addAll(GrouperUtil.nonNull(rowAssignWrappersByRowInternalId).keySet());

    for (Long dataRowInternalId : dataRowInternalIds) {

      GrouperDataRow grouperDataRow = dataEngine.getGrouperDataProviderIndex().getRowWrapperByInternalId().get(dataRowInternalId).getGrouperDataRow();
      GrouperDataRowConfig grouperDataRowConfig = dataEngine.getRowConfigByConfigId().get(grouperDataRow.getConfigId());
      boolean isOneRowPerSubject = grouperDataRowConfig.isOneRowPerSubject();
      List<GrouperDataRowAssignWrapper> grouperDataRowAssignWrappers = GrouperUtil.nonNull(rowAssignWrappersByRowInternalId.get(dataRowInternalId));
      Map<MultiKey, GrouperDataRowAssignWrapper> grouperDataRowKeyToRowAssignWrapper = new HashMap<>();
      for (GrouperDataRowAssignWrapper grouperDataRowAssignWrapper : GrouperUtil.nonNull(grouperDataRowAssignWrappers)) {
        MultiKey rowKey = isOneRowPerSubject ? new MultiKey(new Object[] { grouperDataMemberWrapper.getInternalId() }) : grouperDataRowAssignWrapper.rowKey();
        grouperDataRowKeyToRowAssignWrapper.put(rowKey, grouperDataRowAssignWrapper);
      }

      List<Map<Long, List<Object>>> providerRowsOfDataFieldInternalIdToListOfValues = GrouperUtil.nonNull(grouperDataMemberWrapper.getDataProviderDataByDataRowInternalId().get(dataRowInternalId));
      Map<MultiKey, Map<Long, List<Object>>> providerDataRowKeyToDataFieldInternalIdsAndValues = new HashMap<>();

      if (isOneRowPerSubject) {
        if (GrouperUtil.length(providerRowsOfDataFieldInternalIdToListOfValues) == 1) {
          providerDataRowKeyToDataFieldInternalIdsAndValues.put(new MultiKey(new Object[] { grouperDataMemberWrapper.getInternalId() }), providerRowsOfDataFieldInternalIdToListOfValues.get(0));
        }
      } else {
        for (Map<Long, List<Object>> providerDataFieldInternalIdToValues : GrouperUtil.nonNull(providerRowsOfDataFieldInternalIdToListOfValues)) {
          Object[] keyValues = new Object[GrouperUtil.length(grouperDataRowConfig.getRowKeyFieldConfigIds())];
          int i = 0;
          boolean foundNotNullKey = false;

          for (String rowKeyFieldConfigId : grouperDataRowConfig.getRowKeyFieldConfigIds()) {

            GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(rowKeyFieldConfigId);
            GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByConfigId().get(rowKeyFieldConfigId).getGrouperDataField();
            List<Object> values = providerDataFieldInternalIdToValues.get(grouperDataField.getInternalId());

            if (GrouperUtil.length(values) > 1) {
              throw new RuntimeException("Provider row field key must not have more than one value: " + grouperDataRowConfig.getConfigId()
              + ", field: " + grouperDataFieldConfig.getConfigId() + ", " + GrouperUtil.stringValue(values));
            } else if (GrouperUtil.length(values) == 1) {
              keyValues[i] = grouperDataFieldConfig.getFieldDataType().convertValue(values.iterator().next());
              foundNotNullKey = true;
            } else {
              keyValues[i] = null;
            }

            i++;
          }

          if (foundNotNullKey) {
            MultiKey rowKey = new MultiKey(keyValues);
            Map<Long, List<Object>> existingValue = providerDataRowKeyToDataFieldInternalIdsAndValues.put(rowKey, providerDataFieldInternalIdToValues);
            if (existingValue != null) {
              LOG.warn("Found duplicate keys for row: " + grouperDataRowConfig.getConfigId()
                  + ", key: " + rowKey + ", subjectId: " + grouperDataMemberWrapper.getMember().getSubjectId()
                  + ", subjectSourceId: " + grouperDataMemberWrapper.getMember().getSubjectSourceId());
              state.numberOfDuplicateRowKeysFoundInSource++;
            }
          } else {
            LOG.warn("Skipping provider row with all null keys: " + grouperDataRowConfig.getConfigId());
          }
        }
      }

      // row deletes
      Set<MultiKey> rowKeyFieldsToDeletes = new HashSet<>(grouperDataRowKeyToRowAssignWrapper.keySet());
      rowKeyFieldsToDeletes.removeAll(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());

      for (MultiKey rowKeyFieldsToDelete : rowKeyFieldsToDeletes) {
        GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(rowKeyFieldsToDelete);
        GrouperDataRowAssign grouperDataRowAssign = grouperDataRowAssignWrapper.getGrouperDataRowAssign();

        for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {

          state.totalFieldAssignsInGrouper += grouperDataRowFieldAssignWrappers.size();
          for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {

            GrouperDataRowFieldAssign grouperDataRowFieldAssign = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign();

            Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ?
                grouperDataRowFieldAssign.getValueInteger()
                : grouperDataRowFieldAssign.getValueDictionaryInternalId();

            if (state.rowAssignIdToRowFieldAssignsToDelete.get(grouperDataRowAssign.getInternalId()) == null) {
              state.rowAssignIdToRowFieldAssignsToDelete.put(grouperDataRowAssign.getInternalId(), new ArrayList<>());
              state.rowAssignIdToChangeLogRowFieldDelete.put(grouperDataRowAssign.getInternalId(), new LinkedHashSet<>());
            }

            state.rowAssignIdToRowFieldAssignsToDelete.get(grouperDataRowAssign.getInternalId()).add(grouperDataRowFieldAssign);
            state.rowAssignIdToMemberInternalId.put(grouperDataRowAssign.getInternalId(), grouperDataRowAssign.getMemberInternalId());

            state.rowAssignIdToChangeLogRowFieldDelete.get(grouperDataRowAssign.getInternalId()).add(
                new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_DELETE,
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

        state.rowAssignIdToRowAssignsToDelete.put(grouperDataRowAssign.getInternalId(), grouperDataRowAssignWrapper.getGrouperDataRowAssign());
        state.rowAssignIdToMemberInternalId.put(grouperDataRowAssign.getInternalId(), grouperDataRowAssign.getMemberInternalId());

        state.rowAssignIdToChangeLogRowDelete.put(grouperDataRowAssign.getInternalId(),
            new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_ROW_ASSIGN_DELETE,
                ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.id.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getInternalId()),
                ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.dataRowInternalId.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
                ChangeLogLabels.DATA_ROW_ASSIGN_DELETE.memberInternalId.name(),
                GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId())));

        if (state.rowAssignDeleteCount < MAX_CHANGE_EXAMPLES) {
          String subjectId = grouperDataMemberWrapper.getMember() != null ? grouperDataMemberWrapper.getMember().getSubjectId() : String.valueOf(grouperDataMemberWrapper.getInternalId());
          grouperDataProviderSync.getDebugMap().put("rowAssignDelete_" + state.rowAssignDeleteCount, "subjectId=" + subjectId + ", row=" + grouperDataRowConfig.getConfigId() + ", key=" + rowKeyFieldsToDelete);
        }
        state.rowAssignDeleteCount++;

      }

      // row inserts
      Set<MultiKey> rowKeyFieldsToInserts = new HashSet<>(providerDataRowKeyToDataFieldInternalIdsAndValues.keySet());
      rowKeyFieldsToInserts.removeAll(grouperDataRowKeyToRowAssignWrapper.keySet());

      for (MultiKey rowKeyFieldsToInsert : rowKeyFieldsToInserts) {
        GrouperDataRowAssign grouperDataRowAssign = new GrouperDataRowAssign();
        grouperDataRowAssign.setDataRowInternalId(dataRowInternalId);
        grouperDataRowAssign.setDataProviderInternalId(grouperDataProvider.getInternalId());
        grouperDataRowAssign.setMemberInternalId(grouperDataMemberWrapper.getInternalId());

        state.rowAssignsToInsert.add(grouperDataRowAssign);

        if (state.rowAssignInsertCount < MAX_CHANGE_EXAMPLES) {
          String subjectId = grouperDataMemberWrapper.getMember() != null ? grouperDataMemberWrapper.getMember().getSubjectId() : String.valueOf(grouperDataMemberWrapper.getInternalId());
          grouperDataProviderSync.getDebugMap().put("rowAssignInsert_" + state.rowAssignInsertCount, "subjectId=" + subjectId + ", row=" + grouperDataRowConfig.getConfigId() + ", key=" + rowKeyFieldsToInsert);
        }
        state.rowAssignInsertCount++;

        Map<Long, List<Object>> dataFieldInternalIdToValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(rowKeyFieldsToInsert);
        for (Long dataFieldInternalId : GrouperUtil.nonNull(dataFieldInternalIdToValues.keySet())) {

          GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
          GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

          List<Object> values = dataFieldInternalIdToValues.get(dataFieldInternalId);

          if (values.size() > 1 && !grouperDataFieldConfig.isFieldMultiValued()) {
            throw new RuntimeException("Found multiple values from provider for field with configId=" + grouperDataFieldConfig.getConfigId() + " and memberInternalId=" + grouperDataMemberWrapper.getInternalId());
          }

          for (Object value : values) {
            GrouperDataRowFieldAssign grouperDataRowFieldAssign = new GrouperDataRowFieldAssign();
            grouperDataRowFieldAssign.setDataFieldInternalId(dataFieldInternalId);
            grouperDataRowFieldAssign.setDataRowAssign(grouperDataRowAssign);
            grouperDataFieldConfig.getFieldDataType().assignValue(grouperDataRowFieldAssign, value, dataEngine.getGrouperDataProviderIndex().getDictionaryTextByString());

            state.rowFieldAssignsToInsert.add(grouperDataRowFieldAssign);
          }
        }

      }

      // row updates (existing rows where fields changed)
      for (MultiKey grouperDataRowKey : grouperDataRowKeyToRowAssignWrapper.keySet()) {
        if (providerDataRowKeyToDataFieldInternalIdsAndValues.containsKey(grouperDataRowKey)) {
          GrouperDataRowAssignWrapper grouperDataRowAssignWrapper = grouperDataRowKeyToRowAssignWrapper.get(grouperDataRowKey);

          GrouperDataRowAssign grouperDataRowAssign = grouperDataRowAssignWrapper.getGrouperDataRowAssign();
          Map<Long, List<Object>> providerDataFieldInternalIdsAndValues = providerDataRowKeyToDataFieldInternalIdsAndValues.get(grouperDataRowKey);

          Set<Long> dataFieldInternalIds2 = new LinkedHashSet<>(GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.keySet()));
          dataFieldInternalIds2.addAll(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().keySet());

          for (Long dataFieldInternalId : dataFieldInternalIds2) {

            GrouperDataField grouperDataField = dataEngine.getGrouperDataProviderIndex().getFieldWrapperByInternalId().get(dataFieldInternalId).getGrouperDataField();
            GrouperDataFieldConfig grouperDataFieldConfig = dataEngine.getFieldConfigByConfigId().get(grouperDataField.getConfigId());

            List<Object> providerValues = GrouperUtil.nonNull(providerDataFieldInternalIdsAndValues.get(dataFieldInternalId));
            List<Object> grouperValuesConverted = new ArrayList<Object>();

            if (providerValues.size() > 1 && !grouperDataFieldConfig.isFieldMultiValued()) {
              throw new RuntimeException("Found multiple values from provider for field with configId=" + grouperDataFieldConfig.getConfigId() + " and memberInternalId=" + grouperDataMemberWrapper.getInternalId());
            }

            List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers = GrouperUtil.nonNull(grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().get(dataFieldInternalId));
            state.totalFieldAssignsInGrouper += grouperDataRowFieldAssignWrappers.size();

            for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
              GrouperDataRowFieldAssign grouperDataRowFieldAssign = grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign();

              Object grouperValueConverted = grouperDataFieldConfig.getFieldDataType().convertValue(
                  grouperDataRowFieldAssign.getValueInteger(),
                  grouperDataRowFieldAssignWrapper.getTextValue());
              if (providerValues.contains(grouperValueConverted)) {
                grouperValuesConverted.add(grouperValueConverted);
              } else {

                Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ?
                    grouperDataRowFieldAssign.getValueInteger()
                    : grouperDataRowFieldAssign.getValueDictionaryInternalId();

                if (state.rowAssignIdToRowFieldAssignsToDelete.get(grouperDataRowAssign.getInternalId()) == null) {
                  state.rowAssignIdToRowFieldAssignsToDelete.put(grouperDataRowAssign.getInternalId(), new ArrayList<>());
                  state.rowAssignIdToChangeLogRowFieldDelete.put(grouperDataRowAssign.getInternalId(), new LinkedHashSet<>());
                }

                state.rowAssignIdToRowFieldAssignsToDelete.get(grouperDataRowAssign.getInternalId()).add(grouperDataRowFieldAssign);
                state.rowAssignIdToMemberInternalId.put(grouperDataRowAssign.getInternalId(), grouperDataRowAssign.getMemberInternalId());

                state.rowAssignIdToChangeLogRowFieldDelete.get(grouperDataRowAssign.getInternalId()).add(
                    new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_DELETE,
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
                state.rowFieldAssignsToInsert.add(grouperDataRowFieldAssign);
              }
            }
          }
        }
      }
    }
  }

  /**
   * log a warning if duplicate row keys were found in the source data
   * @param state the change state with the duplicate count
   */
  private void calculateReportDuplicateRowKeys(ChangeState state) {
    if (state.numberOfDuplicateRowKeysFoundInSource > 0) {
      if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
        grouperDataProviderSync.getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.WARNING.name());
      }

      if (grouperDataProviderSync.getDebugMap() != null) {
        grouperDataProviderSync.getDebugMap().put("numberOfDuplicateRowKeysFoundInSource", state.numberOfDuplicateRowKeysFoundInSource);
      }
    }
  }

  /**
   * check if the percentage of field assigns being removed exceeds the configured
   * failsafe threshold. throws OtherJobException if so and failsafe is not approved.
   * @param totalFieldAssignsInGrouper total field assigns in grouper
   * @param totalFieldAssignsToRemove total field assigns to remove
   * @param isFullSync only check failsafe for full syncs
   * @param abortMessage message prefix for the exception if failsafe triggers
   */
  private void calculateCheckFailsafe(long totalFieldAssignsInGrouper, long totalFieldAssignsToRemove, boolean isFullSync, String abortMessage) {
    if (isFullSync && totalFieldAssignsInGrouper > 0 && grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove() != null && grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove() >= 0) {

      Map<String, Object> failsafeDebug = new LinkedHashMap<String, Object>();

      failsafeDebug.put("totalFieldAssignsInGrouper", totalFieldAssignsInGrouper);
      failsafeDebug.put("totalFieldAssignsToRemove", totalFieldAssignsToRemove);
      failsafeDebug.put("percentFieldAssignsAllowedToBeRemoved", grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove());

      double percentFieldAssignsToRemove = (100.0 * totalFieldAssignsToRemove)/totalFieldAssignsInGrouper;

      failsafeDebug.put("percentFieldAssignsToRemove", String.format("%.2f", percentFieldAssignsToRemove));

      grouperDataProviderSync.getDebugMap().putAll(failsafeDebug);

      if (percentFieldAssignsToRemove > grouperDataProviderSync.getFailsafeMaxOverallPercentFieldAssignRemove()) {
        boolean isFailsafeApproved = GrouperFailsafe.isApproved(grouperDataProviderSync.getJobName());

        if (!isFailsafeApproved) {
          GrouperFailsafe.assignFailed(grouperDataProviderSync.getJobName());
          throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, abortMessage + ": " + failsafeDebug);
        }
      }
    }
  }

  /**
   * in readonly mode, track insert/delete/update counts on the loader log without writing anything.
   * @param state the change state with computed changes
   */
  private void calculateTrackReadOnlyCounts(ChangeState state) {
    if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
      grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(state.fieldAssignsToInsert.size());
      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(state.fieldAssignIdToFieldAssignsToDelete.size());
      grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(state.rowAssignsToInsert.size());
      int rowFieldDeleteCount = 0;
      for (List<GrouperDataRowFieldAssign> list : state.rowAssignIdToRowFieldAssignsToDelete.values()) {
        rowFieldDeleteCount += list.size();
      }
      grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(state.rowAssignIdToRowAssignsToDelete.size() + rowFieldDeleteCount);
      grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(state.rowFieldAssignsToInsert.size());
      grouperDataProviderSync.getHib3GrouperLoaderLog().addUpdateCount(state.rowAssignIdToRowAssignsToUpdate.size());
    }
  }

  /**
   * generate internal IDs for new field/row/row-field assigns and build change log entries
   * for each insert.
   * @param state the change state to update with generated IDs and change log entries
   * @param dataEngine the data engine
   */
  private void calculateGenerateInternalIdsAndChangeLog(ChangeState state, GrouperDataEngine dataEngine) {

    // field assigns
    GrouperDataFieldAssignDao.generateInternalIdsIfNeeded(state.fieldAssignsToInsert);
    Map<Long, GrouperDataFieldAssign> fieldAssignIdToFieldAssignsToInsert = new LinkedHashMap<>();
    for (GrouperDataFieldAssign grouperDataFieldAssign : state.fieldAssignsToInsert) {
      Long internalId = grouperDataFieldAssign.getInternalId() == -1 ? grouperDataFieldAssign.getTempInternalIdOnDeck() : grouperDataFieldAssign.getInternalId();
      fieldAssignIdToFieldAssignsToInsert.put(internalId, grouperDataFieldAssign);
      state.fieldAssignIdToMemberInternalId.put(internalId, grouperDataFieldAssign.getMemberInternalId());

      Long valueOrInternalId = grouperDataFieldAssign.getValueInteger() != null ?
          grouperDataFieldAssign.getValueInteger()
          : grouperDataFieldAssign.getValueDictionaryInternalId();

      state.fieldAssignIdToChangeLogInsert.put(internalId,
          new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_FIELD_ASSIGN_ADD,
          ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.id.name(),
          GrouperUtil.stringValue(internalId),
          ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.dataFieldInternalId.name(),
          GrouperUtil.stringValue(grouperDataFieldAssign.getDataFieldInternalId()),
          ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.memberInternalId.name(),
          GrouperUtil.stringValue(grouperDataFieldAssign.getMemberInternalId()),
          ChangeLogLabels.DATA_FIELD_ASSIGN_ADD.valueOrInternalId.name(),
          GrouperUtil.stringValue(valueOrInternalId)));
    }
    // store the keyed map back so calculateWriteChanges can use it
    state.fieldAssignIdToFieldAssignsToInsertKeyed = fieldAssignIdToFieldAssignsToInsert;

    // row assigns
    GrouperDataRowAssignDao.generateInternalIdsIfNeeded(state.rowAssignsToInsert);
    Map<Long, GrouperDataRowAssign> rowAssignIdToRowAssignsToInsert = new LinkedHashMap<>();
    for (GrouperDataRowAssign grouperDataRowAssign : state.rowAssignsToInsert) {
      Long internalId = grouperDataRowAssign.getInternalId() == -1 ? grouperDataRowAssign.getTempInternalIdOnDeck() : grouperDataRowAssign.getInternalId();
      rowAssignIdToRowAssignsToInsert.put(internalId, grouperDataRowAssign);
      state.rowAssignIdToMemberInternalId.put(internalId, grouperDataRowAssign.getMemberInternalId());

      state.rowAssignIdToChangeLogRowInsert.put(internalId,
          new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_ROW_ASSIGN_ADD,
          ChangeLogLabels.DATA_ROW_ASSIGN_ADD.id.name(),
          GrouperUtil.stringValue(internalId),
          ChangeLogLabels.DATA_ROW_ASSIGN_ADD.dataRowInternalId.name(),
          GrouperUtil.stringValue(grouperDataRowAssign.getDataRowInternalId()),
          ChangeLogLabels.DATA_ROW_ASSIGN_ADD.memberInternalId.name(),
          GrouperUtil.stringValue(grouperDataRowAssign.getMemberInternalId())));
    }
    state.rowAssignIdToRowAssignsToInsertKeyed = rowAssignIdToRowAssignsToInsert;

    // row field assigns
    GrouperDataRowFieldAssignDao.generateInternalIdsIfNeeded(state.rowFieldAssignsToInsert);
    Map<Long, List<GrouperDataRowFieldAssign>> rowAssignIdToRowFieldAssignsToInsert = new LinkedHashMap<>();
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : state.rowFieldAssignsToInsert) {
      Long internalId = grouperDataRowFieldAssign.getInternalId() == -1 ? grouperDataRowFieldAssign.getTempInternalIdOnDeck() : grouperDataRowFieldAssign.getInternalId();
      Long rowAssignId = grouperDataRowFieldAssign.getDataRowAssignInternalId();
      if (rowAssignId == -1) {
        rowAssignId = grouperDataRowFieldAssign.getDataRowAssign().getInternalId() == -1 ? grouperDataRowFieldAssign.getDataRowAssign().getTempInternalIdOnDeck() : grouperDataRowFieldAssign.getDataRowAssign().getInternalId();
      }

      if (rowAssignIdToRowFieldAssignsToInsert.get(rowAssignId) == null) {
        rowAssignIdToRowFieldAssignsToInsert.put(rowAssignId, new ArrayList<>());
        state.rowAssignIdToChangeLogRowFieldInsert.put(rowAssignId, new LinkedHashSet<>());
      }
      rowAssignIdToRowFieldAssignsToInsert.get(rowAssignId).add(grouperDataRowFieldAssign);
      state.rowAssignIdToMemberInternalId.put(rowAssignId, grouperDataRowFieldAssign.getDataRowAssign().getMemberInternalId());

      Long valueOrInternalId = grouperDataRowFieldAssign.getValueInteger() != null ?
          grouperDataRowFieldAssign.getValueInteger()
          : grouperDataRowFieldAssign.getValueDictionaryInternalId();

      state.rowAssignIdToChangeLogRowFieldInsert.get(rowAssignId).add(
          new ChangeLogEntryTemp(ChangeLogTypeBuiltin.DATA_ROWFIELD_ASSIGN_ADD,
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.id.name(),
          GrouperUtil.stringValue(internalId),
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowInternalId.name(),
          GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataRowAssign().getDataRowInternalId()),
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataRowAssignInternalId.name(),
          GrouperUtil.stringValue(rowAssignId),
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.dataFieldInternalId.name(),
          GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataFieldInternalId()),
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.memberInternalId.name(),
          GrouperUtil.stringValue(grouperDataRowFieldAssign.getDataRowAssign().getMemberInternalId()),
          ChangeLogLabels.DATA_ROWFIELD_ASSIGN_ADD.valueOrInternalId.name(),
          GrouperUtil.stringValue(valueOrInternalId)));
    }
    state.rowAssignIdToRowFieldAssignsToInsertKeyed = rowAssignIdToRowFieldAssignsToInsert;
  }

  /**
   * create point-in-time history records for field and row assign deletes/updates if configured.
   * @param state the change state with computed deletes and updates
   * @param dataEngine the data engine
   */
  private void calculateGenerateHistory(ChangeState state, GrouperDataEngine dataEngine) {

    // field assign history
    for (GrouperDataFieldAssign grouperDataFieldAssignToDelete : state.fieldAssignIdToFieldAssignsToDelete.values()) {
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

          state.fieldAssignIdToFieldAssignHstsToInsert.put(grouperDataFieldAssignToDelete.getInternalId(), grouperDataFieldAssignHst);
          state.fieldAssignIdToMemberInternalId.put(grouperDataFieldAssignToDelete.getInternalId(), grouperDataFieldAssignToDelete.getMemberInternalId());
        }
      }
    }

    // row assign history - added if row is being deleted or if fields are added/deleted on existing row
    Set<Long> rowAssignIdsForHistoryIfConfigured = new LinkedHashSet<>();
    Set<Long> rowAssignIdsBeingDeleted = new LinkedHashSet<>(state.rowAssignIdToRowAssignsToDelete.keySet());

    rowAssignIdsForHistoryIfConfigured.addAll(state.rowAssignIdToRowFieldAssignsToDelete.keySet());
    rowAssignIdsForHistoryIfConfigured.addAll(state.rowAssignIdToRowAssignsToDelete.keySet());

    for (GrouperDataRowFieldAssign grouperDataRowFieldAssignToInsert : state.rowFieldAssignsToInsert) {
      if (grouperDataRowFieldAssignToInsert.getDataRowAssign().getInternalId() != -1) {
        rowAssignIdsForHistoryIfConfigured.add(grouperDataRowFieldAssignToInsert.getDataRowAssign().getInternalId());
      }
    }
    for (long rowAssignInternalId : rowAssignIdsForHistoryIfConfigured) {
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
          state.rowAssignIdToRowAssignHstsToInsert.put(rowAssignInternalId, grouperDataRowAssignHst);

          if (!rowAssignIdsBeingDeleted.contains(rowAssignInternalId)) {
            grouperDataRowAssignWrapper.getGrouperDataRowAssign().setLastUpdated(endTime);
            state.rowAssignIdToRowAssignsToUpdate.put(grouperDataRowAssignWrapper.getGrouperDataRowAssign().getInternalId(), grouperDataRowAssignWrapper.getGrouperDataRowAssign());
          }

          for (List<GrouperDataRowFieldAssignWrapper> grouperDataRowFieldAssignWrappers : grouperDataRowAssignWrapper.getRowFieldAssignWrappersByFieldInternalId().values()) {
            for (GrouperDataRowFieldAssignWrapper grouperDataRowFieldAssignWrapper : grouperDataRowFieldAssignWrappers) {
              GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst = new GrouperDataRowFieldAssignHst();
              grouperDataRowFieldAssignHst.setDataRowAssignHst(grouperDataRowAssignHst);
              grouperDataRowFieldAssignHst.setDataFieldInternalId(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getDataFieldInternalId());
              grouperDataRowFieldAssignHst.setValueInteger(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueInteger());
              grouperDataRowFieldAssignHst.setValueDictionaryInternalId(grouperDataRowFieldAssignWrapper.getGrouperDataRowFieldAssign().getValueDictionaryInternalId());

              if (state.rowAssignIdToRowFieldAssignHstsToInsert.get(rowAssignInternalId) == null) {
                state.rowAssignIdToRowFieldAssignHstsToInsert.put(rowAssignInternalId, new ArrayList<>());
              }
              state.rowAssignIdToRowFieldAssignHstsToInsert.get(rowAssignInternalId).add(grouperDataRowFieldAssignHst);
            }
          }
        }
      }
    }
  }

  /**
   * write all computed changes to the database in sub-batches of 200 members per transaction.
   * also resolves subjects for subject source data providers.
   * @param state the change state with all inserts/deletes/updates/history/changelog
   * @param dataEngine the data engine
   */
  private void calculateWriteChanges(ChangeState state, GrouperDataEngine dataEngine) {

    // build reverse maps: from member internal ID to the set of field/row assign internal IDs
    Map<Long, Set<Long>> memberIdToFieldAssignIds = new LinkedHashMap<>();
    for (Long fieldAssignInternalId : state.fieldAssignIdToMemberInternalId.keySet()) {
      Long memberInternalId = state.fieldAssignIdToMemberInternalId.get(fieldAssignInternalId);
      if (memberIdToFieldAssignIds.get(memberInternalId) == null) {
        memberIdToFieldAssignIds.put(memberInternalId, new LinkedHashSet<>());
      }
      memberIdToFieldAssignIds.get(memberInternalId).add(fieldAssignInternalId);
    }

    Map<Long, Set<Long>> memberIdToRowAssignIds = new LinkedHashMap<>();
    for (Long rowAssignInternalId : state.rowAssignIdToMemberInternalId.keySet()) {
      Long memberInternalId = state.rowAssignIdToMemberInternalId.get(rowAssignInternalId);
      if (memberIdToRowAssignIds.get(memberInternalId) == null) {
        memberIdToRowAssignIds.put(memberInternalId, new LinkedHashSet<>());
      }
      memberIdToRowAssignIds.get(memberInternalId).add(rowAssignInternalId);
    }

    Map<Long, Member> membersToAddByInternalId = new LinkedHashMap<>();
    for (Member member : grouperDataProviderSync.getGrouperDataEngine().getGrouperDataProviderIndex().getMembersToAddBySubjectId().values()) {
      membersToAddByInternalId.put(member.getInternalId(), member);
    }

    Set<Long> allMemberInternalIdsToUpdate = new LinkedHashSet<>();
    allMemberInternalIdsToUpdate.addAll(memberIdToFieldAssignIds.keySet());
    allMemberInternalIdsToUpdate.addAll(memberIdToRowAssignIds.keySet());
    allMemberInternalIdsToUpdate.addAll(membersToAddByInternalId.keySet());
    List<Long> allMemberInternalIdsToUpdateList = new ArrayList<>(allMemberInternalIdsToUpdate);

    int batchSize = 200;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(allMemberInternalIdsToUpdateList.size(), batchSize, false);
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {

      final int theBatchIndex = batchIndex;
      List<Long> batchOfMemberInternalIds = GrouperUtil.batchList(allMemberInternalIdsToUpdateList, batchSize, theBatchIndex);

      // insert new Member rows for this sub-batch
      Set<Member> batchOfMembersToAdd = new LinkedHashSet<>();
      for (Long memberInternalId : batchOfMemberInternalIds) {
        if (membersToAddByInternalId.containsKey(memberInternalId)) {
          batchOfMembersToAdd.add(membersToAddByInternalId.get(memberInternalId));
        }
      }

      if (batchOfMembersToAdd.size() > 0) {
        try {
          HibernateSession.byObjectStatic().saveBatch(batchOfMembersToAdd);
        } catch (Exception e) {
          for (Member memberToAdd : batchOfMembersToAdd) {
            try {
              HibernateSession.byObjectStatic().save(memberToAdd);
            } catch (Exception e2) {
              LOG.error("Error adding member", e2);
              batchOfMemberInternalIds.remove(memberToAdd.getInternalId());
            }
          }
        }
      }

      if (batchOfMemberInternalIds.size() == 0) {
        continue;
      }

      // write all field/row assign inserts, deletes, updates, history, and change log entries
      // for this sub-batch of members in a single transaction
      new GcDbAccess().callbackTransaction(new GcTransactionCallback<Boolean>() {

        @Override
        public Boolean callback(GcDbAccess dbAccessForStorage) {

          List<GrouperDataFieldAssignHst> batchOfFieldAssignHsts = new ArrayList<>();
          List<GrouperDataFieldAssign> batchOfFieldAssignsToDelete = new ArrayList<>();
          List<GrouperDataFieldAssign> batchOfFieldAssignsToInsert = new ArrayList<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogFieldDelete = new LinkedHashSet<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogFieldInsert = new LinkedHashSet<>();

          List<GrouperDataRowFieldAssign> batchOfRowFieldAssignsToInsert = new ArrayList<>();
          List<GrouperDataRowFieldAssign> batchOfRowFieldAssignsToDelete = new ArrayList<>();
          List<GrouperDataRowAssign> batchOfRowAssignsToInsert = new ArrayList<>();
          List<GrouperDataRowAssign> batchOfRowAssignsToDelete = new ArrayList<>();
          List<GrouperDataRowAssign> batchOfRowAssignsToUpdate = new ArrayList<>();
          List<GrouperDataRowAssignHst> batchOfRowAssignHsts = new ArrayList<>();
          List<GrouperDataRowFieldAssignHst> batchOfRowFieldAssignHsts = new ArrayList<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogRowFieldInsert = new LinkedHashSet<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogRowFieldDelete = new LinkedHashSet<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogRowInsert = new LinkedHashSet<>();
          Set<ChangeLogEntryTemp> batchOfChangeLogRowDelete = new LinkedHashSet<>();

          // gather operations for each member in this sub-batch
          for (Long memberInternalId : batchOfMemberInternalIds) {

            for (Long fieldAssignInternalId : GrouperUtil.nonNull(memberIdToFieldAssignIds.get(memberInternalId))) {
              if (state.fieldAssignIdToFieldAssignHstsToInsert.containsKey(fieldAssignInternalId)) {
                batchOfFieldAssignHsts.add(state.fieldAssignIdToFieldAssignHstsToInsert.get(fieldAssignInternalId));
              }
              if (state.fieldAssignIdToFieldAssignsToDelete.containsKey(fieldAssignInternalId)) {
                batchOfFieldAssignsToDelete.add(state.fieldAssignIdToFieldAssignsToDelete.get(fieldAssignInternalId));
              }
              if (state.fieldAssignIdToFieldAssignsToInsertKeyed.containsKey(fieldAssignInternalId)) {
                batchOfFieldAssignsToInsert.add(state.fieldAssignIdToFieldAssignsToInsertKeyed.get(fieldAssignInternalId));
              }
              if (state.fieldAssignIdToChangeLogDelete.containsKey(fieldAssignInternalId)) {
                batchOfChangeLogFieldDelete.add(state.fieldAssignIdToChangeLogDelete.get(fieldAssignInternalId));
              }
              if (state.fieldAssignIdToChangeLogInsert.containsKey(fieldAssignInternalId)) {
                batchOfChangeLogFieldInsert.add(state.fieldAssignIdToChangeLogInsert.get(fieldAssignInternalId));
              }
            }

            for (Long rowAssignInternalId : GrouperUtil.nonNull(memberIdToRowAssignIds.get(memberInternalId))) {
              if (state.rowAssignIdToRowAssignHstsToInsert.containsKey(rowAssignInternalId)) {
                batchOfRowAssignHsts.add(state.rowAssignIdToRowAssignHstsToInsert.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowFieldAssignHstsToInsert.containsKey(rowAssignInternalId)) {
                batchOfRowFieldAssignHsts.addAll(state.rowAssignIdToRowFieldAssignHstsToInsert.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowFieldAssignsToDelete.containsKey(rowAssignInternalId)) {
                batchOfRowFieldAssignsToDelete.addAll(state.rowAssignIdToRowFieldAssignsToDelete.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowAssignsToDelete.containsKey(rowAssignInternalId)) {
                batchOfRowAssignsToDelete.add(state.rowAssignIdToRowAssignsToDelete.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowAssignsToInsertKeyed.containsKey(rowAssignInternalId)) {
                batchOfRowAssignsToInsert.add(state.rowAssignIdToRowAssignsToInsertKeyed.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowFieldAssignsToInsertKeyed != null && state.rowAssignIdToRowFieldAssignsToInsertKeyed.containsKey(rowAssignInternalId)) {
                batchOfRowFieldAssignsToInsert.addAll(state.rowAssignIdToRowFieldAssignsToInsertKeyed.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToRowAssignsToUpdate.containsKey(rowAssignInternalId)) {
                batchOfRowAssignsToUpdate.add(state.rowAssignIdToRowAssignsToUpdate.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToChangeLogRowFieldDelete.containsKey(rowAssignInternalId)) {
                batchOfChangeLogRowFieldDelete.addAll(state.rowAssignIdToChangeLogRowFieldDelete.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToChangeLogRowDelete.containsKey(rowAssignInternalId)) {
                batchOfChangeLogRowDelete.add(state.rowAssignIdToChangeLogRowDelete.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToChangeLogRowInsert.containsKey(rowAssignInternalId)) {
                batchOfChangeLogRowInsert.add(state.rowAssignIdToChangeLogRowInsert.get(rowAssignInternalId));
              }
              if (state.rowAssignIdToChangeLogRowFieldInsert.containsKey(rowAssignInternalId)) {
                batchOfChangeLogRowFieldInsert.addAll(state.rowAssignIdToChangeLogRowFieldInsert.get(rowAssignInternalId));
              }
            }
          }

          // write in dependency order: history before deletes, deletes before inserts

          GrouperDataFieldAssignHstDao.store(batchOfFieldAssignHsts);

          GrouperDataFieldAssignDao.delete(batchOfFieldAssignsToDelete);
          ChangeLogEntryTempDao.store(batchOfChangeLogFieldDelete);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(batchOfFieldAssignsToDelete.size());
          }

          GrouperDataFieldAssignDao.store(batchOfFieldAssignsToInsert);
          ChangeLogEntryTempDao.store(batchOfChangeLogFieldInsert);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(batchOfFieldAssignsToInsert.size());
          }

          GrouperDataRowAssignHstDao.store(batchOfRowAssignHsts);
          GrouperDataRowFieldAssignHstDao.store(batchOfRowFieldAssignHsts);

          GrouperDataRowFieldAssignDao.delete(batchOfRowFieldAssignsToDelete);
          ChangeLogEntryTempDao.store(batchOfChangeLogRowFieldDelete);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(batchOfRowFieldAssignsToDelete.size());
          }

          GrouperDataRowAssignDao.delete(batchOfRowAssignsToDelete);
          ChangeLogEntryTempDao.store(batchOfChangeLogRowDelete);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addDeleteCount(batchOfRowAssignsToDelete.size());
          }

          GrouperDataRowAssignDao.store(batchOfRowAssignsToInsert);
          ChangeLogEntryTempDao.store(batchOfChangeLogRowInsert);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(batchOfRowAssignsToInsert.size());
          }

          GrouperDataRowFieldAssignDao.store(batchOfRowFieldAssignsToInsert);
          ChangeLogEntryTempDao.store(batchOfChangeLogRowFieldInsert);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addInsertCount(batchOfRowFieldAssignsToInsert.size());
          }

          GrouperDataRowAssignDao.store(batchOfRowAssignsToUpdate);
          if (grouperDataProviderSync.getHib3GrouperLoaderLog() != null) {
            grouperDataProviderSync.getHib3GrouperLoaderLog().addUpdateCount(batchOfRowAssignsToUpdate.size());
          }

          return null;
        }
      });

      // resolve subjects for subject source data providers
      boolean isSubjectSource = dataEngine.getProviderConfigByConfigId().get(grouperDataProviderSync.getConfigId()).isSubjectSource();
      String subjectSourceIdIfSubjectSource = dataEngine.getProviderConfigByConfigId().get(grouperDataProviderSync.getConfigId()).getSubjectSourceId();

      if (isSubjectSource) {
        Map<Long, GrouperDataMemberWrapper> memberWrapperByInternalId = dataEngine.getGrouperDataProviderIndex().getMemberWrapperByInternalId();
        Map<String, Member> subjectIdsToResolve = new LinkedHashMap<>();
        for (long memberInternalId : batchOfMemberInternalIds) {
          GrouperDataMemberWrapper grouperDataMemberWrapper = memberWrapperByInternalId.get(memberInternalId);
          if (grouperDataMemberWrapper != null && grouperDataMemberWrapper.getMember() != null) {
            Member member = grouperDataMemberWrapper.getMember();
            String subjectId = member.getSubjectId();
            subjectIdsToResolve.put(subjectId, member);
          }
        }

        int skipCacheStoreThreshold = GrouperConfig.retrieveConfig().propertyValueInt("grouper.dataProvider.skipSubjectCacheStoreThreshold", 100000);
        boolean skipCacheStore = subjectIdsToResolve.size() > skipCacheStoreThreshold;
        if (skipCacheStore) {
          SubjectSourceCache.assignThreadLocalSkipCacheStore(true);
        }
        try {
          Map<String, Subject> subjectIdToSubjectMap = SubjectFinder.findByIds(subjectIdsToResolve.keySet(), subjectSourceIdIfSubjectSource, false, true);
          for (String subjectId : subjectIdToSubjectMap.keySet()) {
            Member member = subjectIdsToResolve.get(subjectId);
            Subject subject = subjectIdToSubjectMap.get(subjectId);
            member.updateMemberAttributes(subject, true);
          }
        } finally {
          if (skipCacheStore) {
            SubjectSourceCache.assignThreadLocalSkipCacheStore(false);
          }
        }
      }

      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
  }

  /**
   * add change counts to the debug map for the daemon job message
   * @param state the change state with the counts
   */
  private void calculateReportChangeCounts(ChangeState state) {
    if (state.fieldAssignInsertCount > 0) {
      grouperDataProviderSync.getDebugMap().put("fieldAssignInserts", state.fieldAssignInsertCount);
    }
    if (state.fieldAssignDeleteCount > 0) {
      grouperDataProviderSync.getDebugMap().put("fieldAssignDeletes", state.fieldAssignDeleteCount);
    }
    if (state.rowAssignInsertCount > 0) {
      grouperDataProviderSync.getDebugMap().put("rowAssignInserts", state.rowAssignInsertCount);
    }
    if (state.rowAssignDeleteCount > 0) {
      grouperDataProviderSync.getDebugMap().put("rowAssignDeletes", state.rowAssignDeleteCount);
    }
  }



  /**
   * delete old data field and data row history records based on configured retention.
   * called from OTHER_JOB_cleanLogs (GrouperDaemonDeleteOldRecords)
   * @param jobMessage
   * @param hib3GrouploaderLog
   */
  public static void deleteOldDataFieldRowHistory(StringBuilder jobMessage, Hib3GrouperLoaderLog hib3GrouploaderLog) {
    GrouperDataEngine dataEngine = new GrouperDataEngine();
    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    dataEngine.loadFieldsAndRows(grouperConfig);

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

    int totalDeleted = 0;

    if (grouperDataFieldAssignHstsToDelete.size() > 0) {
      GrouperDataFieldAssignHstDao.delete(grouperDataFieldAssignHstsToDelete);
      totalDeleted += grouperDataFieldAssignHstsToDelete.size();
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
      totalDeleted += grouperDataRowFieldAssignHstsToDelete.size();
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      GrouperDataRowAssignHstDao.delete(grouperDataRowAssignHstsToDelete);
      totalDeleted += grouperDataRowAssignHstsToDelete.size();
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }

    if (hib3GrouploaderLog != null) {
      hib3GrouploaderLog.addDeleteCount(totalDeleted);
    }
    if (jobMessage != null) {
      jobMessage.append("Deleted " + totalDeleted + " old data field/row history records.\n");
    }
  }
}

