/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;

/**
 * type of table sync
 */
public enum GcTableSyncSubtype {
  
  /**
   * full sync all columns
   */
  fullSyncFull {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
    

    /**
     * 
     */
    @Override
    public void retrieveData(final Map<String, Object> debugMap, final GcTableSync gcTableSync) {
      
      captureCurrentMaxIncrementalIndexIfNeeded(debugMap, gcTableSync);

      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListAll() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
          gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll(), sqlTo, gcTableSync.getDataBeanTo().getTableMetadata().columnListAll(), 
          "retrieveData", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      gcTableSync.getGcGrouperSync().setRecordsCount(GrouperClientUtils.length(gcTableSyncTableDatas[0].getRows()));

    }
    

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery());     
      
      if (!GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalAllColumnsColumnString())) {
        assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn());
      } else if (!GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalProgressColumnString())) {
        assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), null);
      }
      
      int recordsChanged = results[0] + results[1] + results[2];

      return  recordsChanged;
            
    }


    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return false;
    }


  },
  
  /**
   * full sync but do groups on single col primary key or single group col (e.g. group name of memberships)
   */
  fullSyncGroups {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }
    
    /**
     * see if needs group column
     * @return true if needs group column
     */
    @Override
    public boolean isNeedsGroupColumn() { 
      return true;
    }


    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      captureCurrentMaxIncrementalIndexIfNeeded(debugMap, gcTableSync);

      String sqlFrom = "select distinct " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select distinct " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
          gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName(), sqlTo, 
          gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName(), "retrieveGroups", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      String sqlCountFrom = "select count(1) from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      int count = new GcDbAccess().connectionName(gcTableSync.getDataBeanFrom().getTableMetadata().getConnectionName()).sql(sqlCountFrom).select(int.class);
      gcTableSync.getGcGrouperSync().setRecordsCount(count);
      
    }


    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      Integer recordsChanged = runInsertsUpdatesDeletesGroupings(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery().allGroupings(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery().allGroupings());      
      
      assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), null);

      return recordsChanged;
      
    }

    @Override
    public boolean isFullMetadataSync() {
      return false;
    }

    @Override
    public boolean isIncrementalSync() {
      return false;
    }

  },
  
  /**
   * full sync get primary keys and a col that if not matching indicates an update
   */
  fullSyncChangeFlag {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return true;
    }

    /**
     * 
     */
    @Override
    public void retrieveData(final Map<String, Object> debugMap, final GcTableSync gcTableSync) {

      captureCurrentMaxIncrementalIndexIfNeeded(debugMap, gcTableSync);

      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
          gcTableSync.getDataBeanFrom().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress(), sqlTo, 
          gcTableSync.getDataBeanTo().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress(), "retrieveChangeFlag", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      gcTableSync.getGcGrouperSync().setRecordsCount(GrouperClientUtils.length(gcTableSyncTableDatas[0].getRows()));

    }

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      Integer changes = runInsertsUpdatesDeletesChangeFlag(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery());      
      
      if (!GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalAllColumnsColumnString())) {
        assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn());
      } else if (!GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalProgressColumnString())) {
        assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), null);
      }

      return changes;
    }

    @Override
    public boolean isFullMetadataSync() {
      return false;
    }

    @Override
    public boolean isIncrementalSync() {
      return false;
    }
  },
  
  /**
   * get all incremental rows and all columns in those rows (e.g. last updated col on source, which might not get deletes unless there is a disabled flag)
   */
  incrementalAllColumns {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      if (gcTableSync.getDataBeanFrom().getDataInitialQuery() == null) {
        return 0;
      }

      // get all data from destination based on primary key
      Set<MultiKey> primaryKeysToRetrieve = gcTableSync.getDataBeanFrom().getDataInitialQuery().allPrimaryKeys();
      
      int recordsChanged = handleLotsOfChangesAnotherWay(debugMap, gcTableSync, primaryKeysToRetrieve, gcTableSync.getDataBeanFrom().getDataInitialQuery());
      
      GcTableSyncTableData gcTableSyncTableDataTo = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToRetrieve, false);
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), gcTableSyncTableDataTo);
      recordsChanged += results[0] + results[1] + results[2];

      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);

      // update the real time incremented col...
      assignIncrementalIndex(gcTableSync.getDataBeanFrom().getDataInitialQuery(), 
          gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn());

      return recordsChanged;
      
    }

    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      retrieveIncrementalDataHelper(debugMap, gcTableSync, gcTableSync.getDataBeanFrom(), 
          gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll(),
          gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn(), true);

    }

    @Override
    public boolean isFullMetadataSync() {
      return false;
    }

    @Override
    public boolean isIncrementalSync() {
      return true;
    }

  },
  
  /**
   * get all incremental rows, which have the primary keys of rows that need updating
   */
  incrementalPrimaryKey {

    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      if (gcTableSync.getDataBeanRealTime().getDataInitialQuery() == null) {
        return 0;
      }
      // get all data from destination based on primary key
      Set<MultiKey> primaryKeysToRetrieve = gcTableSync.getDataBeanRealTime().getDataInitialQuery().allDataInColumns(gcTableSync.getDataBeanFrom().getTableMetadata().getPrimaryKey());
      
      int recordsChanged = handleLotsOfChangesAnotherWay(debugMap, gcTableSync, primaryKeysToRetrieve, gcTableSync.getDataBeanRealTime().getDataInitialQuery());

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndToForAllDataFromPrimaryKeys(debugMap, gcTableSync, primaryKeysToRetrieve);
      
      GcTableSyncTableData gcTableSyncTableDataFrom = gcTableSyncTableDatas[0];
      GcTableSyncTableData gcTableSyncTableDataTo = gcTableSyncTableDatas[1];
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDataFrom, gcTableSyncTableDataTo);   
      recordsChanged += results[0] + results[1] + results[2];
      
      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);

      // update the real time incremented col...
      assignIncrementalIndex(gcTableSync.getDataBeanRealTime().getDataInitialQuery(), gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn());
      
      return recordsChanged;
      
    }


    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
               
      
      retrieveIncrementalDataHelper(debugMap, gcTableSync, 
          gcTableSync.getDataBeanRealTime(), 
          gcTableSync.getDataBeanRealTime().getTableMetadata()
            .columnListInputtedColumnsAndIncrementalProgressColumn(
                 gcTableSync.getDataBeanFrom().getTableMetadata().getPrimaryKey()), 
            gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn() , null);

    }


    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return true;
    }

    
  }, 
  /**
   * full sync but do groups on single col primary key or single group col, and only do add or remove on groups
   */
  fullSyncMetadata{
  
    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    
    /**
     * see if needs group column
     * @return true if needs group column
     */
    @Override
    public boolean isNeedsGroupColumn() { 
      return true;
    }
  
  
    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      String sqlFrom = "select distinct " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select distinct " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();
  
      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
          gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName(), sqlTo, 
          gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName(), "retrieveGroups", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);
  
    }
  
  
    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      Integer recordsChanged = runInsertsUpdatesDeletesGroupingsMetadata(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery().allGroupings(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery().allGroupings());      
      
      return recordsChanged;
      
    }

    @Override
    public boolean isFullMetadataSync() {
      return true;
    }

    @Override
    public boolean isIncrementalSync() {
      return false;
    }
  
  }, 
  
  /**
   * get all incremental rows, which have the primary keys of rows that need updating
   */
  incrementalFromIdentifiedPrimaryKeys {
  
    /**
     * see if full sync
     * @return true if full sync or false if incremental
     */
    @Override
    public boolean isFullSync() {
      return false;
    }
    
  
    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      // get all data from destination based on primary key
      Set<MultiKey> primaryKeysToRetrieve = gcTableSync.getPrimaryKeysToSync();

      if (GrouperClientUtils.length(primaryKeysToRetrieve) == 0) {
        return 0;
      }

      // organize the groups and count of members to analyze
      Map<Object, Set<MultiKey>> groupPrimaryKeys = new HashMap<Object, Set<MultiKey>>();

      for (MultiKey multiKey : primaryKeysToRetrieve) {

        Object groupLabel = multiKey.getKey(0);
        Set<MultiKey> currentPrimaryKeys = groupPrimaryKeys.get(groupLabel);
        if (currentPrimaryKeys == null) {
          currentPrimaryKeys = new HashSet<MultiKey>();
          groupPrimaryKeys.put(groupLabel, currentPrimaryKeys);
        }
        currentPrimaryKeys.add(multiKey);

      }

      
      int recordsChanged = handleLotsOfChangesAnotherWay(debugMap, gcTableSync, primaryKeysToRetrieve, groupPrimaryKeys);
  
      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndToForAllDataFromPrimaryKeys(debugMap, gcTableSync, primaryKeysToRetrieve);
      
      GcTableSyncTableData gcTableSyncTableDataFrom = gcTableSyncTableDatas[0];
      GcTableSyncTableData gcTableSyncTableDataTo = gcTableSyncTableDatas[1];
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDataFrom, gcTableSyncTableDataTo);   
      recordsChanged += results[0] + results[1] + results[2];
      
      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);
  
      return recordsChanged;
      
    }
  
  
    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
               
      // passed in  

    }


    @Override
    public boolean isFullMetadataSync() {
      return false;
    }


    @Override
    public boolean isIncrementalSync() {
      return true;
    }
  
    
  };
  
  /**
   * sync groupings in full
   * @param debugMap
   * @param gcTableSync
   * @param groupings
   * @return records changed
   */
  public static int syncGroupings(Map<String, Object> debugMap, GcTableSync gcTableSync, Collection<Object> groupings) {
    
    if (GrouperClientUtils.length(groupings) == 0) {
      return 0;
    }
    
    int groupingsBatchSize = Math.min(gcTableSync.getGcTableSyncConfiguration().getMaxBindVarsInSelect(), 
        gcTableSync.getGcTableSyncConfiguration().getGroupingSize());

    int groupingsNumberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupings, groupingsBatchSize);
    int recordsChanged = 0;
    for (int currentBatchIndex = 0; currentBatchIndex < groupingsNumberOfBatches; currentBatchIndex++) {
      
      List<Object> groupingsBatch = GrouperClientUtils.batchList(groupings, groupingsBatchSize, currentBatchIndex);
      
      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll()  
          + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName() + " where " 
          + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() 
          + " in (" + GrouperClientUtils.appendQuestions(groupingsBatch.size()) + ")";
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListAll() 
          + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName() + " where " 
              + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() 
              + " in (" + GrouperClientUtils.appendQuestions(groupingsBatch.size()) + ")";

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
          gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll(), sqlTo, 
          gcTableSync.getDataBeanTo().getTableMetadata().columnListAll(), "retrieveGroups", GrouperClientUtils.toArray(groupingsBatch, Object.class));
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDatas[0], 
          gcTableSyncTableDatas[1]);   

      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);

      recordsChanged += results[0] + results[1] + results[2];
      
      //TODO update the sync group objects?
    }
    return recordsChanged;
  }

  /**
   * use a full or group sync instead of purely incremental.  pop the affects primary keys off the stack if so...
   * @param debugMap
   * @param gcTableSync
   * @param primaryKeysToRetrieve
   * @return changes
   */
  private static int handleLotsOfChangesAnotherWay(Map<String, Object> debugMap, GcTableSync gcTableSync, Set<MultiKey> primaryKeysToRetrieve,
      GcTableSyncTableData fullDataOfQuery) {
    
    // we dont need to group or do full sync
    if (GrouperClientUtils.length(primaryKeysToRetrieve) == 0) {
      return 0;
    }

    Map<Object, Set<MultiKey>> groupPrimaryKeys = null;
    
    String groupingColumnName = gcTableSync.getGcTableSyncConfiguration().getGroupColumnString();
    if (!GrouperClientUtils.isBlank(groupingColumnName)) {

      Integer groupingColumnZeroIndex = fullDataOfQuery.getRows().iterator().next().lookupColumnToIndexZeroIndexed(groupingColumnName, false);
      
      if (groupingColumnZeroIndex != null) {

        groupPrimaryKeys = new HashMap<Object, Set<MultiKey>>();

        for (GcTableSyncRowData gcTableSyncRowData : fullDataOfQuery.getRows()) {

          Object groupLabel = gcTableSyncRowData.getData()[groupingColumnZeroIndex];
          Set<MultiKey> currentPrimaryKeys = groupPrimaryKeys.get(groupLabel);
          if (currentPrimaryKeys == null) {
            currentPrimaryKeys = new HashSet<MultiKey>();
            groupPrimaryKeys.put(groupLabel, currentPrimaryKeys);
          }
          currentPrimaryKeys.add(gcTableSyncRowData.getPrimaryKey());

        }
      }
    }
    return handleLotsOfChangesAnotherWay(debugMap, gcTableSync, primaryKeysToRetrieve, groupPrimaryKeys);
  }

  /**
   * use a full or group sync instead of purely incremental.  pop the affects primary keys off the stack if so...
   * @param debugMap
   * @param gcTableSync
   * @param primaryKeysToRetrieve
   * @return changes
   */
  private static int handleLotsOfChangesAnotherWay(Map<String, Object> debugMap, GcTableSync gcTableSync, Set<MultiKey> primaryKeysToRetrieve,
      Map<Object, Set<MultiKey>> groupPrimaryKeys) {

    // we dont need to group or do full sync
    if (GrouperClientUtils.length(primaryKeysToRetrieve) == 0) {
      return 0;
    }
    
    //  # switch from incremental to full if the number of incrementals is over this threshold
    //  # if this is less than 0, then it will not switch from incremental to full
    //  # {valueType: "integer"}
    //  # grouperClient.syncTable.personSource.switchFromIncrementalToFullIfOverRecords = 300000
    //
    //  # switch from incremental to full if the number of incrementals is over the threshold, this is full sync to switch to
    //  # fullSyncChangeFlag, fullSyncFull, fullSyncGroups
    //  # {valueType: "string"}
    //  # grouperClient.syncTable.personSource.switchFromIncrementalToFullSubtype = fullSyncFull
    //
    //  # switch from incremental to group (if theres a grouping col) if the number of incrementals for a certain group
    //  # if this is less than 0, then it will not switch from incremental to group
    //  # {valueType: "integer"}
    //  # grouperClient.syncTable.personSource.switchFromIncrementalToGroupIfOverRecordsInGroup = 50000
    //
    //  # switch from incremental to full if the number of groups (and records over threshold) is over this threshold
    //  # i.e. needs to be over 100 groups and over 300000 records
    //  # {valueType: "integer"}
    //  # grouperClient.syncTable.personSource.switchFromIncrementalToFullIfOverGroupCount = 100

    int switchFromIncrementalToFullIfOverRecords = gcTableSync.getGcTableSyncConfiguration().getSwitchFromIncrementalToFullIfOverRecords();
    GcTableSyncSubtype switchFromIncrementalToFullSubtype = gcTableSync.getGcTableSyncConfiguration().getSwitchFromIncrementalToFullSubtype();
    int switchFromIncrementalToGroupIfOverRecordsInGroup = gcTableSync.getGcTableSyncConfiguration().getSwitchFromIncrementalToGroupIfOverRecordsInGroup();
    int switchFromIncrementalToFullIfOverGroupCount = gcTableSync.getGcTableSyncConfiguration().getSwitchFromIncrementalToFullIfOverGroupCount();

    boolean needsFullSyncBasedOnRecords = (switchFromIncrementalToFullIfOverRecords > 0 && switchFromIncrementalToFullSubtype != null
        && GrouperClientUtils.length(primaryKeysToRetrieve) > switchFromIncrementalToFullIfOverRecords);

    boolean needsFullSyncBasedOnGroups = false;
    // figure out the group count

    if (needsFullSyncBasedOnRecords) {
      // see if we really want to do full sync still
      if (switchFromIncrementalToFullIfOverGroupCount > 0 && groupPrimaryKeys.size() > switchFromIncrementalToFullIfOverGroupCount) {
        needsFullSyncBasedOnGroups = true;
      }
    }
    
    if (!needsFullSyncBasedOnGroups) {
      // lets tackle this based on groups
      needsFullSyncBasedOnRecords = false;
      // take out groups that have fewer members than the limit
      for (Object grouping : new HashSet<Object>(groupPrimaryKeys.keySet())) {
        Set<MultiKey> primaryKeys = groupPrimaryKeys.get(grouping);
        if (primaryKeys.size() <= switchFromIncrementalToGroupIfOverRecordsInGroup) {
          groupPrimaryKeys.remove(grouping);
        }
      }
    
      if (groupPrimaryKeys.size() > 0) {
        // lets see what we got
        Set<Object> switchedToGroups = new HashSet<Object>(groupPrimaryKeys.keySet());
        gcTableSync.getGcTableSyncOutput().setSwitchedToGroups(switchedToGroups);
        logIncrement(debugMap,"switchedToGroupsCount", switchedToGroups.size());
        
        if (switchedToGroups.size() < 10) {
          int i=0;
          for (Object switchedToGroup : switchedToGroups) { 
            debugMap.put("switchedToGroup_" + i++, switchedToGroup);
          }
        }
        
        // sync groups
        int recordsChanged = syncGroupings(debugMap, gcTableSync, switchedToGroups);
        
        // now we need to go through records and remove ones that are part of this group
        for (Set<MultiKey> currentPrimaryKeys : groupPrimaryKeys.values()) {
          primaryKeysToRetrieve.removeAll(currentPrimaryKeys);
        }
        
        return recordsChanged;
      }
    }

    if (needsFullSyncBasedOnGroups || needsFullSyncBasedOnRecords) {
        
      runEmbeddedFullSync(debugMap, gcTableSync.getGcGrouperSyncJob(), gcTableSync.getGcGrouperSyncLog(), switchFromIncrementalToFullSubtype);

      gcTableSync.getGcTableSyncOutput().setSwitchedToFull(true);
      // we dont need to do any records anymore
      primaryKeysToRetrieve.clear();

    }
    return 0;
  }

  public static void runEmbeddedFullSync(Map<String, Object> debugMap,
      GcGrouperSyncJob gcGrouperSyncJob, GcGrouperSyncLog gcGrouperSyncLog, GcTableSyncSubtype switchFromIncrementalToFullSubtype) {
    // lets pause this guy
    debugMap.put("switchedToFullSync", true);
    debugMap.put("switchedToFullSyncSubtype", switchFromIncrementalToFullSubtype.name());
    debugMap.put("paused", true);
    
    gcGrouperSyncJob.setJobState(GcGrouperSyncJobState.pending);
    gcGrouperSyncJob.store();

    // wait a sec
    GrouperClientUtils.sleep(1000);
    
    // run a full
    GcTableSync newFull = new GcTableSync();
    newFull.setGcGrouperSync(gcGrouperSyncJob.getGrouperSync());
    newFull.setGcGrouperSyncLog(gcGrouperSyncLog);
    newFull.sync(gcGrouperSyncJob.getGrouperSync().getProvisionerName(), switchFromIncrementalToFullSubtype);

    // wait a sec
    GrouperClientUtils.sleep(1000);

    gcGrouperSyncJob.getGrouperSync().waitForRelatedJobsToFinishThenRun(gcGrouperSyncJob, false);
    debugMap.put("paused", false);
  }

  /**
   * get incremental data either from FROM table or from real time table
   * @param debugMap
   * @param gcTableSync
   * @param gcDbAccessMaxSql
   * @param columnTypeProgressColumn
   * @param sqlGetIncrementals
   * @param gcTableSyncTableBeanSelectFrom
   * @param isFrom true if from, false if "to", and null if neither
   */
  private static void retrieveIncrementalDataHelper(Map<String, Object> debugMap, GcTableSync gcTableSync, 
      GcTableSyncTableBean gcTableSyncTableBeanSelectFrom, String columnsToSelect, GcTableSyncColumnMetadata incrementalColumn, Boolean isFrom) {
    
    // where are we in the list?  do the whole list if we dont know where we are?
    Long lastRetrievedLong = gcTableSync.getGcGrouperSync().getIncrementalIndex();
    Timestamp lastRetrievedTimestamp = gcTableSync.getGcGrouperSync().getIncrementalTimestamp();

    Object lastRetrieved = null;

    ColumnType columnTypeProgressColumn = incrementalColumn.getColumnType();
    
    switch(columnTypeProgressColumn) {
      
      case NUMERIC:
        lastRetrieved = lastRetrievedLong;

        break;
      case TIMESTAMP:
        lastRetrieved = lastRetrievedTimestamp;
        
        break;
      case STRING:
        lastRetrieved = lastRetrievedLong == null ? null : ("" + lastRetrievedLong);
        
        break;
      default:
        throw new RuntimeException("Not expecting type: " + columnTypeProgressColumn);
      
    }

    debugMap.put("lastRetrieved", lastRetrieved);
    
    // havent run this before, just start now, if null then square one
    if (lastRetrieved != null) {

      String sqlGetIncrementals = "select " 
          + columnsToSelect
          + " from " + gcTableSyncTableBeanSelectFrom.getTableMetadata().getTableName() 
          + " where " + incrementalColumn.getColumnName()
          + " > ?";
  
      GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSyncTableBeanSelectFrom, 
          sqlGetIncrementals, columnsToSelect, "incrementalChanges", new Object[] {lastRetrieved}, isFrom );
  
      gcTableSyncTableBeanSelectFrom.setDataInitialQuery(gcTableSyncTableData);
    }      
  }
  
  /**
   * @param debugMap
   * @param gcTableSync
   */
  private static void captureCurrentMaxIncrementalIndexIfNeeded(final Map<String, Object> debugMap, final GcTableSync gcTableSync) {
    
    boolean hasProgressColumn = !GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalProgressColumnString());
    
    boolean hasAllColumnsColumn = !GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalAllColumnsColumnString());

    if (!hasProgressColumn && !hasAllColumnsColumn) {
      return;
    }
    
    Long maxProgress = null;
    
    if (hasProgressColumn) {
    
      String maxSql = "select max( " + gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn().getColumnName() + " ) "
          + " from " + gcTableSync.getDataBeanRealTime().getTableMetadata().getTableName();
          
      GcDbAccess gcDbAccessMaxSql = new GcDbAccess().sql(maxSql)
          .connectionName(gcTableSync.getDataBeanFrom().getTableMetadata().getConnectionNameOrReadonly());

      switch (gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn().getColumnType()) {
        
        case NUMERIC:
          
          maxProgress = gcDbAccessMaxSql.select(Long.class);
          
          break;
          
        case STRING:

          String maxProgressString = gcDbAccessMaxSql.select(String.class);
          maxProgress = GrouperClientUtils.longObjectValue(maxProgressString, true);

          break;
          
        case TIMESTAMP:
          
          Timestamp maxProgressTimestamp = gcDbAccessMaxSql.select(Timestamp.class);
          maxProgress = maxProgressTimestamp == null ? null : maxProgressTimestamp.getTime();

          break;
        default:
          throw new RuntimeException("Not expecting type: " + gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn().getColumnType());

      }
      
    }

    Long maxProgressAllColumns = null;

    if (hasAllColumnsColumn) {
      
      String maxSql = "select max( " + gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn().getColumnName() + " ) "
          + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
          
      GcDbAccess gcDbAccessMaxSql = new GcDbAccess().sql(maxSql)
          .connectionName(gcTableSync.getDataBeanFrom().getTableMetadata().getConnectionNameOrReadonly());

      switch (gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn().getColumnType()) {
        
        case NUMERIC:
          
          maxProgressAllColumns = gcDbAccessMaxSql.select(Long.class);
          
          break;
          
        case STRING:

          String maxProgressAllColumnsString = gcDbAccessMaxSql.select(String.class);
          maxProgressAllColumns = GrouperClientUtils.longObjectValue(maxProgressAllColumnsString, true);

          break;
          
        case TIMESTAMP:
          
          Timestamp maxProgressAllColumnsTimestamp = gcDbAccessMaxSql.select(Timestamp.class);
          maxProgressAllColumns = maxProgressAllColumnsTimestamp == null ? null : maxProgressAllColumnsTimestamp.getTime();

          break;
        default:
          throw new RuntimeException("Not expecting type: " + gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalAllCoumnsColumn().getColumnType());

      }
    }
    
    if (maxProgress != null) {
      logIncrement(debugMap,"initialMaxProgress", maxProgress);
    }
    
    if (maxProgressAllColumns != null) {
      logIncrement(debugMap,"initialMaxProgressAllColumns", maxProgressAllColumns);
    }
    
    if (maxProgress == null && maxProgressAllColumns == null) {
      return;
    }
    
    if (maxProgress == null) {
      gcTableSync.setLatestIncrementalValueBeforeStarted(maxProgressAllColumns);
    } else if (maxProgressAllColumns == null) {
      gcTableSync.setLatestIncrementalValueBeforeStarted(maxProgress);
    } else {
      gcTableSync.setLatestIncrementalValueBeforeStarted(Math.max(maxProgress, maxProgressAllColumns));
    }
    
  }
  
  /**
   * 
   * @param gcTableSyncTableBean
   * @param isFrom true for from, false for to, null for neither
   * return the data
   * 
   */
  private static GcTableSyncTableData runQuery(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBean, String sql, String columns,
      String queryLogLabel, Object[] bindVars, Boolean isFrom) {
    long start = System.nanoTime();
    //
    GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBean.getTableMetadata();
    String connectionName = gcTableSyncTableMetadata.getConnectionNameOrReadonly();
    try {

      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(connectionName);
      if (bindVars != null) {
        gcDbAccess.bindVars(bindVars);
      }
      List<Object[]> results = gcDbAccess.sql(sql).selectList(Object[].class);

      GcTableSyncTableData gcTableSyncTableData = new GcTableSyncTableData();
      gcTableSyncTableData.init(gcTableSyncTableBean, gcTableSyncTableMetadata.lookupColumns(columns), results);
      logIncrement(debugMap, queryLogLabel + "Count", GrouperClientUtils.length(results));

      if (isFrom != null) { 
        if (isFrom) {
          gcTableSyncTableBean.getGcTableSync().getGcTableSyncOutput().addRowsSelectedFrom(GrouperClientUtils.length(results));
        } else {
          gcTableSyncTableBean.getGcTableSync().getGcTableSyncOutput().addRowsSelectedTo(GrouperClientUtils.length(results));
        }
      }
      
      return gcTableSyncTableData;
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' connectionName: " + connectionName + ", query '" + sql + "', " + GrouperClientUtils.toStringForLog(bindVars));
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      logIncrement(debugMap, queryLogLabel + "Millis", (long)((System.nanoTime() - start)/1000));
    }
  }

  /**
   * 
   * @param debugMap
   * @param label
   * @param amountToAdd
   */
  private static void logIncrement(final Map<String, Object> debugMap, String label, long amountToAdd) {

    Number currentValue = (Number)debugMap.get(label);
    
    if (currentValue != null) {
      amountToAdd += currentValue.longValue();
    }
    
    debugMap.put(label, amountToAdd);
    
  }

  /**
   * assign max progress to job object
   * @param gcTableSyncTableData
   * @param progressColumnMetadata
   */
  private static void assignIncrementalIndex(GcTableSyncTableData gcTableSyncTableData, GcTableSyncColumnMetadata progressColumnMetadata) {

    GcTableSync gcTableSync = gcTableSyncTableData.getGcTableSyncTableBean().getGcTableSync();

    // update the real time incremented col...
    Object maxProgress = progressColumnMetadata == null ? null : gcTableSyncTableData.maxProgressValue(progressColumnMetadata);
    
    // consider the max of full sync at the beginning
    Long lastIncrementalValueBeforeStarted = gcTableSync.getLatestIncrementalValueBeforeStarted();
    
    if (maxProgress != null || lastIncrementalValueBeforeStarted != null) {
      
      if (maxProgress == null) {
        maxProgress = lastIncrementalValueBeforeStarted;
      } else if (lastIncrementalValueBeforeStarted == null) {
        // keep max progress
      } else {
        Long maxProgressLong = null;
        if (maxProgress instanceof Number) {
          maxProgressLong = ((Number)maxProgress).longValue();
        } else if (maxProgress instanceof Timestamp ) {
          maxProgressLong = ((Timestamp)maxProgress).getTime();
        } else if (maxProgress instanceof String) {
          maxProgressLong = GrouperClientUtils.longValue((String)maxProgress);
        } else {
          throw new RuntimeException("Not expecting type: " + maxProgress.getClass());
        }
        maxProgress = Math.max(maxProgressLong, lastIncrementalValueBeforeStarted);
      }
    }
    
    long millisWhenJobStarted = gcTableSync.getMillisWhenSyncStarted();

    if (maxProgress != null) {

      boolean madeProgress = false;
      if (maxProgress instanceof Date) {
        
        long maxProgressMillis = ((Date) maxProgress).getTime();
        
        if (gcTableSync.getGcGrouperSync().getIncrementalTimestamp() == null || gcTableSync.getGcGrouperSync().getIncrementalTimestamp().getTime() < maxProgressMillis ) {
          madeProgress = true;
          gcTableSync.getGcGrouperSyncJob().setLastSyncIndex(maxProgressMillis);
          gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(maxProgressMillis));
          gcTableSync.getGcGrouperSync().setIncrementalIndex(maxProgressMillis);
          gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(maxProgressMillis));
        }

      } else if (maxProgress instanceof Number) {
          
        long maxProgressLong = ((Number)maxProgress).longValue();

        gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(millisWhenJobStarted));
        gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(millisWhenJobStarted));
        madeProgress = true;

        if (gcTableSync.getGcGrouperSyncJob().getLastSyncIndex() == null || gcTableSync.getGcGrouperSyncJob().getLastSyncIndex() < maxProgressLong ) {

          gcTableSync.getGcGrouperSyncJob().setLastSyncIndex(maxProgressLong);
          gcTableSync.getGcGrouperSync().setIncrementalIndex(maxProgressLong);
        }
      } else if (maxProgress instanceof String) {

        long maxProgressLong = GrouperClientUtils.longObjectValue((String)maxProgress, false);

        gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(millisWhenJobStarted));
        gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(millisWhenJobStarted));
        madeProgress = true;

        if (gcTableSync.getGcGrouperSyncJob().getLastSyncIndex() == null || gcTableSync.getGcGrouperSyncJob().getLastSyncIndex() < maxProgressLong ) {

          gcTableSync.getGcGrouperSync().setIncrementalIndex(maxProgressLong);
        }
      }

    }

  }
  
  /**
   * 
   * @param debugMap
   * @param gcTableSyncTableBeanTo
   * @param primaryKeysToInsertInput
   * @param allIndexByPrimaryKey
   * @param queryLogLabel
   * @return the number inserted
   */
  private static int runInserts(Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo,
      Set<MultiKey> primaryKeysToInsertInput,
      Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey, String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {

      int totalInserts = 0;
      logIncrement(debugMap,queryLogLabel + "Count", 0);
      
      if (GrouperClientUtils.length(primaryKeysToInsertInput) > 0) {
        
        // memory problems if doing too much at much
        int batchSize = 10000;
        
        //List
        List<MultiKey> primaryKeysToInsertList = new ArrayList<MultiKey>(primaryKeysToInsertInput);

        int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(primaryKeysToInsertList, batchSize);
        
        for (int batchIndex = 0;batchIndex < numberOfBatches; batchIndex++) {

          List<MultiKey> primaryKeysToInsertBatch = GrouperClientUtils.batchList(primaryKeysToInsertList, batchSize, batchIndex);
        
          //
          GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
          sql = "insert into " 
              + gcTableSyncTableMetadata.getTableName() + " ( " 
              + gcTableSyncTableMetadata.columnListAll() 
              +  " ) values ( " + GrouperClientUtils.appendQuestions(GrouperClientUtils.length(gcTableSyncTableMetadata.getColumns())) +  " )" ;
    
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
    
          List<List<Object>> bindVars = convertToListOfBindVarsValues(primaryKeysToInsertBatch, allIndexByPrimaryKey);
          
          gcDbAccess.batchBindVars(bindVars);
    
          gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
          
          int[] batchResults = gcDbAccess.sql(sql).executeBatchSql();
          
          int inserts = GrouperClientUtils.length(primaryKeysToInsertInput);
          int actualInserts = 0;
          int missedInserts = 0;
          int multiInsertInstances = 0;
          
          if (GrouperClientUtils.length(batchResults) > 0) {
            for (int result : batchResults) {
              actualInserts += Math.max(result, 0);
              if (result == 0) {
                missedInserts++;
              }
              // why would this happen???
              if (result>1) {
                multiInsertInstances++;
              }
            }
          }
          totalInserts += actualInserts;
          logIncrement(debugMap, queryLogLabel + "Count", actualInserts);
          if (inserts != actualInserts) {
            logIncrement(debugMap, queryLogLabel + "IntendedCount", inserts);
          }
          if (missedInserts > 0) {
            logIncrement(debugMap, queryLogLabel + "MissedInserts", missedInserts);
          }
          if (multiInsertInstances > 0) {
            logIncrement(debugMap, queryLogLabel + "PrimaryKeyProblems", multiInsertInstances);
          }
          gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncOutput().addInsert(actualInserts);

        }
      }

      return totalInserts;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      logIncrement(debugMap,queryLogLabel + "Millis", (int)((System.nanoTime() - start)/1000));
    }
  }

  /**
   * 
   * @param gcTableSyncTableBeanTo
   * return the data
   */
  private static int runDeletes(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo, Set<MultiKey> primaryKeysToDeleteInput, 
      String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {
      
      logIncrement(debugMap,queryLogLabel + "Count", 0);
      int totalDeletes = 0;
      
      if (GrouperClientUtils.length(primaryKeysToDeleteInput) > 0) {
        
        // memory problems if doing too much at much
        int batchSize = 10000;
        
        //List
        List<MultiKey> primaryKeysToDeleteList = new ArrayList<MultiKey>(primaryKeysToDeleteInput);

        int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(primaryKeysToDeleteList, batchSize);
        
        for (int batchIndex = 0;batchIndex < numberOfBatches; batchIndex++) {

          List<MultiKey> primaryKeysToDeleteBatch = GrouperClientUtils.batchList(primaryKeysToDeleteList, batchSize, batchIndex);
              
          GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
          sql = "delete from " + gcTableSyncTableMetadata.getTableName() + " where "
              + gcTableSyncTableMetadata.queryWherePrimaryKey();
    
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
    
          List<List<Object>> bindVars = convertToListOfBindVars(primaryKeysToDeleteBatch);
          
          gcDbAccess.batchBindVars(bindVars);
    
          gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
          
          int[] batchResults = gcDbAccess.sql(sql).executeBatchSql();

          int deletes = GrouperClientUtils.length(batchResults);
          int actualDeletes = 0;
          int missedDeletes = 0;
          int multiDeleteInstances = 0;
          
          if (GrouperClientUtils.length(batchResults) > 0) {
            for (int result : batchResults) {
              // issue with it being negative for some reason
              actualDeletes += Math.max(result, 0);
              if (result == 0) {
                missedDeletes++;
              }
              if (result>1) {
                multiDeleteInstances++;
              }
            }
          }
          totalDeletes += actualDeletes;
          logIncrement(debugMap,queryLogLabel + "Count", actualDeletes);
          if (deletes != actualDeletes) {
            logIncrement(debugMap,queryLogLabel + "IntendedCount", deletes);
          }
          if (missedDeletes > 0) {
            logIncrement(debugMap,queryLogLabel + "MissedDeletes", missedDeletes);
          }
          if (multiDeleteInstances > 0) {
            logIncrement(debugMap, queryLogLabel + "PrimaryKeyProblems", multiDeleteInstances);
          }

          gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncOutput().addDelete(actualDeletes);

          
        }
        
      }
            
      return totalDeletes;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      logIncrement(debugMap,queryLogLabel + "Millis",(int)( (System.nanoTime() - start)/1000));
    }

  }

  /**
   * 
   * @param setOfMultiKeys
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVarsValues (
      Collection<MultiKey> setOfMultiKeys, Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey) {
    List<List<Object>> bindVars = new ArrayList<List<Object>>();
    
    for (MultiKey multiKey : setOfMultiKeys) {
      
      List<Object> bindVarRow = new ArrayList<Object>();
      
      GcTableSyncRowData gcTableSyncRowData = allIndexByPrimaryKey.get(multiKey);
      
      for (Object bindVar : gcTableSyncRowData.getData()) {
        bindVarRow.add(bindVar);
      }
      bindVars.add(bindVarRow);
    }
    return bindVars;
  }
  
  /**
   * 
   * @param setOfMultiKeys
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVars (
      Collection<MultiKey> setOfMultiKeys) {
    List<List<Object>> bindVars = new ArrayList<List<Object>>();
    
    for (MultiKey multiKey : setOfMultiKeys) {
      
      List<Object> bindVarRow = new ArrayList<Object>();
      for (Object bindVar : multiKey.getKeys()) {
        bindVarRow.add(bindVar);
      }
      bindVars.add(bindVarRow);
    }
    return bindVars;
  }

  /**
   * 
   * @param setOfObjects
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVarsObjects (
      Set<Object> setOfObjects) {
    List<List<Object>> bindVars = new ArrayList<List<Object>>();
    
    for (Object object : setOfObjects) {
      
      List<Object> bindVarRow = new ArrayList<Object>();
      bindVarRow.add(object);
      bindVars.add(bindVarRow);
    }
    return bindVars;
  }

  /**
   * 
   * @param debugMap
   * @param gcTableSync
   * @return two table datas (from is 0 to is 1)
   */
  private static GcTableSyncTableData[] runQueryFromAndToForAllDataFromPrimaryKeys(final Map<String, Object> debugMap, 
      final GcTableSync gcTableSync, final Set<MultiKey> primaryKeys) {

    if (GrouperClientUtils.length(primaryKeys) == 0) {
      return new GcTableSyncTableData[] {new GcTableSyncTableData(), new GcTableSyncTableData()};
    }
    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    final GcTableSyncTableData[] result = new GcTableSyncTableData[2];
    //lets get all from one side and the other and time it and do it in a thread so its faster
    Thread selectFromThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          result[0] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanFrom(), primaryKeys, true);
        } catch (RuntimeException re) {
          if (RUNTIME_EXCEPTION[0] != null) {
            LOG.error("Error retrieve by primary key", re);
          }
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    selectFromThread.start();
    
    result[1] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeys, false);
    
    GrouperClientUtils.join(selectFromThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
    }

    return result;
  }

    
  /**
   * 
   * @param debugMap
   * @param gcTableSync
   * @return two table datas (from is 0 to is 1)
   */
  private static GcTableSyncTableData[] runQueryFromAndTo(final Map<String, Object> debugMap, 
      final GcTableSync gcTableSync, final String sqlFrom, final String columnsFrom, final String sqlTo, final String columnsTo, 
      final String queryLogLabel, final Object[] bindVars) {

    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    final GcTableSyncTableData[] result = new GcTableSyncTableData[2];
    //lets get all from one side and the other and time it and do it in a thread so its faster
    Thread selectFromThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        try {
          result[0] = runQuery(debugMap, gcTableSync.getDataBeanFrom(), sqlFrom, columnsFrom, queryLogLabel + "From", bindVars, true );
        } catch (RuntimeException re) {
          if (RUNTIME_EXCEPTION[0] != null) {
            LOG.error("error", RUNTIME_EXCEPTION[0]);
          }
          RUNTIME_EXCEPTION[0] = re;
        }
      }
    });
    
    selectFromThread.start();
    try {
      result[1] = runQuery(debugMap, gcTableSync.getDataBeanTo(), sqlTo, columnsTo, queryLogLabel + "To", bindVars, false );
    } catch (RuntimeException re) {
      if (RUNTIME_EXCEPTION[0] != null) {
        LOG.error("error", RUNTIME_EXCEPTION[0]);
      }
      RUNTIME_EXCEPTION[0] = re;
    }
    
    GrouperClientUtils.join(selectFromThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw new RuntimeException("error", RUNTIME_EXCEPTION[0]);
    }

    return result;
  }

  /**
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GcTableSyncSubtype.class);
  
  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullSync();
  
  /**
   * see if full metadata sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isFullMetadataSync();

  /**
   * see if incremental sync
   * @return true if full sync or false if incremental
   */
  public abstract boolean isIncrementalSync();
  

  /**
   * see if full sync
   * @return true if full sync or false if incremental
   */
  public boolean isNeedsGroupColumn() { 
    return false;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GcTableSyncSubtype valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperClientUtils.enumValueOfIgnoreCase(GcTableSyncSubtype.class, 
        string, exceptionOnNull);
  }

  /**
   * do the initial select query for the sync
   * @param debugMap, gcTableSync
   */
  public abstract void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync);
  
  /**
   * do the initial compare step
   * @param debugMap, gcTableSync
   * @return records changed or null if not applicable
   */
  public abstract Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync);

  /**
   * find inserts/udpates/deletes based on queries for change flag
   * @param debugMap
   * @param gcTableSync
   * @param groupingsFrom
   * @param groupingsTo
   * @return records changed
   */
  private static int runInsertsUpdatesDeletesGroupings(Map<String, Object> debugMap,
      GcTableSync gcTableSync, Set<Object> groupingsFrom, Set<Object> groupingsTo) {
    
    // delete ones which arent there
    int deletes = -1;
    {
      Set<Object> groupingsToDelete = new HashSet<Object>(groupingsTo);
      groupingsToDelete.removeAll(groupingsFrom);
      
      deletes = runDeletesOfGroupings(debugMap, gcTableSync.getDataBeanTo(), groupingsToDelete, "deleteGroupings");
    }      
    
    if (GrouperClientUtils.length(groupingsFrom) == 0) {
      return deletes;
    }
    
    // we need to batch these for the grouping size, need to be sorted
    List<Object> groupings = new ArrayList<Object>(groupingsFrom);
    Collections.sort(groupings, new Comparator<Object>() {
  
      @SuppressWarnings("rawtypes")
      @Override
      public int compare(Object o1, Object o2) {
        return ((Comparable)o1).compareTo((Comparable)o2);
      }
    });
  
    int batchSize = gcTableSync.getGcTableSyncConfiguration().getGroupingSize();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupings, batchSize);
    
    int recordsUpdated = 0;
    
    
    
    for (int currentBatchIndex=0;currentBatchIndex<numberOfBatches;currentBatchIndex++) {
      List<Object> groupingsBatch = GrouperClientUtils.batchList(groupings, batchSize, currentBatchIndex);
  
      int[] results = null;
      if (GrouperClientUtils.length(groupingsBatch) > 0) {
        String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll() 
            + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName()
            + " where " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " >= ? "
            + " and " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " <= ? ";
        String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListAll() 
            + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName()
            + " where " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " >= ? "
            + " and " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " <= ? ";
  
        GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, 
            gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll(), sqlTo, 
            gcTableSync.getDataBeanTo().getTableMetadata().columnListAll(),
            "retrieveData", new Object[] {groupingsBatch.get(0), groupingsBatch.get(groupingsBatch.size()-1)});
        
        results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDatas[0], 
            gcTableSyncTableDatas[1]);   
      } else {
        results = new int[] {0,0,0};
      }
  
      
      recordsUpdated +=  results[0] + results[1] + results[2];
      
    }
    
    return recordsUpdated;
  }

  /**
   * 
   * @param gcTableSyncTableBeanTo
   * return the data
   */
  private static int runDeletesOfGroupings(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo, 
      Set<Object> groupingsToDelete, String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {
      int[] results = null;
      
      if (GrouperClientUtils.length(groupingsToDelete) > 0) {
        //
        GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
        sql = "delete from " + gcTableSyncTableMetadata.getTableName() + " where "
            + gcTableSyncTableMetadata.getGroupColumnMetadata().getColumnName() + " = ?";
    
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
    
        List<List<Object>> bindVars = convertToListOfBindVarsObjects(groupingsToDelete);
        
        gcDbAccess.batchBindVars(bindVars);
    
        gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
        
        results = gcDbAccess.sql(sql).executeBatchSql();
      }
      
      int deletes = GrouperClientUtils.length(groupingsToDelete);
      int actualDeletes = 0;
      int missedDeletes = 0;
      int multiDeleteInstances = 0;

      if (GrouperClientUtils.length(groupingsToDelete) > 0) {
        for (int result : results) {
          actualDeletes += Math.max(result, 0);
          if (result == 0) {
            missedDeletes++;
          }
          if (result>1) {
            multiDeleteInstances++;
          }
        }
      }
      logIncrement(debugMap,queryLogLabel + "Count", actualDeletes);
      logIncrement(debugMap,queryLogLabel + "GroupsCount", deletes);
      if (missedDeletes > 0) {
        logIncrement(debugMap,queryLogLabel + "MissedDeletes", missedDeletes);
      }
      if (multiDeleteInstances > 0) {
        logIncrement(debugMap, queryLogLabel + "PrimaryKeyProblems", multiDeleteInstances);
      }
      gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncOutput().addDelete(actualDeletes);

      return actualDeletes;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      logIncrement(debugMap,queryLogLabel + "Millis",(int)( (System.nanoTime() - start)/1000));
    }
  
  }

  /**
   * find inserts/udpates/deletes based on queries for change flag
   * @param debugMap
   * @param gcTableSync
   * @param gcTableSyncTableDataTo
   * @param gcTableSyncTableDataFrom
   * @return records changed
   */
  private static int runInsertsUpdatesDeletesChangeFlag(Map<String, Object> debugMap,
      GcTableSync gcTableSync, GcTableSyncTableData gcTableSyncTableDataFrom, GcTableSyncTableData gcTableSyncTableDataTo) {
    // delete ones which arent there
    int deletes = -1;
    {
      Set<MultiKey> primaryKeysToDelete = new LinkedHashSet<MultiKey>(gcTableSyncTableDataTo.allPrimaryKeys());
      primaryKeysToDelete.removeAll(gcTableSyncTableDataFrom.allPrimaryKeys());
      
      deletes = runDeletes(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToDelete, "deletes");
    }      
    
    int inserts = -1;
    {
      Set<MultiKey> primaryKeysToInsert = new LinkedHashSet<MultiKey>(gcTableSyncTableDataFrom.allPrimaryKeys());
      primaryKeysToInsert.removeAll(gcTableSyncTableDataTo.allPrimaryKeys());
      
      // get data for from
      GcTableSyncTableData gcTableSyncTableDataInserts = runQueryForAllDataFromPrimaryKeys(debugMap, 
          gcTableSyncTableDataFrom.getGcTableSyncTableBean(), primaryKeysToInsert, true);
      
      Map<MultiKey, GcTableSyncRowData> indexByPrimaryKeyInserts = gcTableSyncTableDataInserts.allIndexByPrimaryKey();
      
      inserts = runInserts(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToInsert, 
          indexByPrimaryKeyInserts, "inserts");
    }      
    
    int updates = -1;
    {
      Set<MultiKey> primaryKeysToUpdate = new LinkedHashSet<MultiKey>();
      
      for (MultiKey multiKey : gcTableSyncTableDataFrom.allPrimaryKeys()) {
        
        GcTableSyncRowData gcTableSyncRowData = gcTableSyncTableDataTo.allIndexByPrimaryKey().get(multiKey);
        
        // this is an insert
        if (gcTableSyncRowData == null) {
          continue;
        }
        MultiKey multiKeyDataTo = new MultiKey(gcTableSyncRowData.getData());
        MultiKey multiKeyDataFrom = new MultiKey(gcTableSyncTableDataFrom.allIndexByPrimaryKey().get(multiKey).getData());
        if (multiKeyDataFrom.equals(multiKeyDataTo)) {
          continue;
        }
        
        // must be an update
        primaryKeysToUpdate.add(multiKey);
        
      }
  
      // get data for from
      GcTableSyncTableData gcTableSyncTableDataUpdates = runQueryForAllDataFromPrimaryKeys(debugMap, 
          gcTableSyncTableDataFrom.getGcTableSyncTableBean(), primaryKeysToUpdate, true);
  
      Map<MultiKey, GcTableSyncRowData> indexByPrimaryKeyUpdates = gcTableSyncTableDataUpdates.allIndexByPrimaryKey();
      
      updates = runUpdates(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToUpdate, 
          indexByPrimaryKeyUpdates, "updates");
      
    }
    
    return inserts + updates + deletes;
  }

  /**
   * find inserts/udpates/deletes based on queries
   * @param debugMap
   * @param gcTableSync
   * @param gcTableSyncTableDataTo
   * @param gcTableSyncTableDataFrom
   * @return inserts, updates, deletes
   */
  private static int[] runInsertsUpdatesDeletes(Map<String, Object> debugMap,
      GcTableSync gcTableSync, GcTableSyncTableData gcTableSyncTableDataFrom, GcTableSyncTableData gcTableSyncTableDataTo) {
    
    int[] results = new int[3];
    
    // delete ones which arent there
    int deletes = -1;
    {
      Set<MultiKey> primaryKeysToDelete = new LinkedHashSet<MultiKey>(gcTableSyncTableDataTo.allPrimaryKeys());
      primaryKeysToDelete.removeAll(gcTableSyncTableDataFrom.allPrimaryKeys());
      
      deletes = runDeletes(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToDelete, "deletes");
      results[2] = deletes;
    }      
    
    int inserts = -1;
    {
      Set<MultiKey> primaryKeysToInsert = new LinkedHashSet<MultiKey>(gcTableSyncTableDataFrom.allPrimaryKeys());
      primaryKeysToInsert.removeAll(gcTableSyncTableDataTo.allPrimaryKeys());
      
      inserts = runInserts(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToInsert, 
          gcTableSyncTableDataFrom.allIndexByPrimaryKey(), "inserts");
      results[0] = inserts;
    }      
    
    int updates = -1;
    {
      Set<MultiKey> primaryKeysToUpdate = new LinkedHashSet<MultiKey>();
      for (MultiKey multiKey : gcTableSyncTableDataFrom.allPrimaryKeys()) {
        GcTableSyncRowData gcTableSyncRowData = gcTableSyncTableDataTo.allIndexByPrimaryKey().get(multiKey);
        
        // this is an insert
        if (gcTableSyncRowData == null) {
          continue;
        }
        MultiKey multiKeyDataTo = new MultiKey(gcTableSyncRowData.getData());
        MultiKey multiKeyDataFrom = new MultiKey(gcTableSyncTableDataFrom.allIndexByPrimaryKey().get(multiKey).getData());
        if (multiKeyDataFrom.equals(multiKeyDataTo)) {
          continue;
        }
        
        // must be an update
        primaryKeysToUpdate.add(multiKey);
        
      }
      updates = runUpdates(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToUpdate, 
          gcTableSyncTableDataFrom.allIndexByPrimaryKey(), "updates");
      results[1] = updates;
    }

    return results ;
  }

  /**
   * 
   * @param setOfMultiKeys
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVarsUpdate (
      Collection<MultiKey> setOfMultiKeys, Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey) {
    List<List<Object>> bindVars = new ArrayList<List<Object>>();
    
    for (MultiKey multiKey : setOfMultiKeys) {
      
      List<Object> bindVarRow = new ArrayList<Object>();
      
      GcTableSyncRowData gcTableSyncRowData = allIndexByPrimaryKey.get(multiKey);
      
      for (Object bindVar : gcTableSyncRowData.getNonPrimaryKey().getKeys()) {
        bindVarRow.add(bindVar);
      }
      for (Object bindVar : gcTableSyncRowData.getPrimaryKey().getKeys()) {
        bindVarRow.add(bindVar);
      }
      bindVars.add(bindVarRow);
    }
    return bindVars;
  }

  /**
   * 
   * @param debugMap
   * @param gcTableSyncTableBeanTo
   * @param primaryKeysToUpdateInput
   * @param allIndexByPrimaryKey
   * @param queryLogLabel
   * @return the number inserted
   */
  private static int runUpdates(Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo,
      Set<MultiKey> primaryKeysToUpdateInput,
      Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey, String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {
      
      logIncrement(debugMap,queryLogLabel + "Count", 0);
      int totalUpdates = 0;
      
      if (GrouperClientUtils.length(primaryKeysToUpdateInput) > 0) {
        
        // memory problems if doing too much at much
        int batchSize = 10000;
        
        //List
        List<MultiKey> primaryKeysToUpdateList = new ArrayList<MultiKey>(primaryKeysToUpdateInput);

        int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(primaryKeysToUpdateList, batchSize);
        
        for (int batchIndex = 0;batchIndex < numberOfBatches; batchIndex++) {

          List<MultiKey> primaryKeysToUpdateBatch = GrouperClientUtils.batchList(primaryKeysToUpdateList, batchSize, batchIndex);
      
          //
          GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
          sql = "update " 
              + gcTableSyncTableMetadata.getTableName() + " set  " 
              + gcTableSyncTableMetadata.queryUpdateNonPrimaryKey()
              +  " where " + gcTableSyncTableMetadata.queryWherePrimaryKey() ;
      
          GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
      
          List<List<Object>> bindVars = convertToListOfBindVarsUpdate(primaryKeysToUpdateBatch, allIndexByPrimaryKey);
          
          gcDbAccess.batchBindVars(bindVars);
      
          gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
          
          int[] batchResults = gcDbAccess.sql(sql).executeBatchSql();
          
          int updates = GrouperClientUtils.length(primaryKeysToUpdateBatch);
          int actualUpdates = 0;
          int missedUpdates = 0;
          int multiUpdateInstances = 0;

          if (GrouperClientUtils.length(batchResults) > 0) {
            for (int result : batchResults) {
              actualUpdates += Math.max(result, 0);
              if (result == 0) {
                missedUpdates++;
              }
              // why would this happen???
              if (result>1) {
                multiUpdateInstances++;
              }
            }
          }
          totalUpdates += actualUpdates;
          logIncrement(debugMap,queryLogLabel + "Count", actualUpdates);
          if (updates != actualUpdates) {
            logIncrement(debugMap,queryLogLabel + "IntendedCount", updates);
          }
          if (missedUpdates > 0) {
            logIncrement(debugMap,queryLogLabel + "MissedUpdates", missedUpdates);
          }
          if (multiUpdateInstances > 0) {
            logIncrement(debugMap,queryLogLabel + "PrimaryKeyProblems", multiUpdateInstances);
          }
          gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncOutput().addUpdate(actualUpdates);
        }
      }

      return totalUpdates;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      logIncrement(debugMap,queryLogLabel + "Millis", (System.nanoTime() - start)/1000);
    }
  }

  /**
   * find inserts/deletes based on queries for groupings
   * @param debugMap
   * @param gcTableSync
   * @param groupingsFrom
   * @param groupingsTo
   * @return records changed
   */
  private static int runInsertsUpdatesDeletesGroupingsMetadata(Map<String, Object> debugMap,
      GcTableSync gcTableSync, Set<Object> groupingsFrom, Set<Object> groupingsTo) {
    
    // delete ones which arent there
    int deletes = 0;
    {
      Set<Object> groupingsToDelete = new HashSet<Object>(groupingsTo);
      groupingsToDelete.removeAll(groupingsFrom);
      
      deletes = runDeletesOfGroupings(debugMap, gcTableSync.getDataBeanTo(), groupingsToDelete, "deleteGroupings");
      
    }      
    
    if (GrouperClientUtils.length(groupingsFrom) == 0) {
      return deletes;
    }

    Set<Object> groupingsToInsert = new HashSet<Object>(groupingsFrom);
    groupingsToInsert.removeAll(groupingsTo);

    if (GrouperClientUtils.length(groupingsToInsert) == 0) {
      return deletes;
    }

    // we need to batch these for the grouping size, need to be sorted
    List<Object> groupings = new ArrayList<Object>(groupingsToInsert);
    Collections.sort(groupings, new Comparator<Object>() {

      @SuppressWarnings("rawtypes")
      @Override
      public int compare(Object o1, Object o2) {
        return ((Comparable)o1).compareTo((Comparable)o2);
      }
    });

    int batchSize = gcTableSync.getGcTableSyncConfiguration().getGroupingSize();
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupings, batchSize);
    
    int recordsUpdated = deletes;
    int inserts = 0;
    for (int currentBatchIndex=0;currentBatchIndex<numberOfBatches;currentBatchIndex++) {
      List<Object> groupingsBatch = GrouperClientUtils.batchList(groupings, batchSize, currentBatchIndex);

      if (GrouperClientUtils.length(groupingsBatch) > 0) {
        String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll() 
            + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName()
            + " where " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " >= ? "
            + " and " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " <= ? ";
  
        GcTableSyncTableData gcTableSyncTableDataFrom = runQuery(debugMap, gcTableSync.getDataBeanFrom(), sqlFrom, 
            gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll(), 
            "retrieveData", new Object[] {groupingsBatch.get(0), groupingsBatch.get(groupingsBatch.size()-1)}, true);
        
        Set<MultiKey> primaryKeysToInsert = new LinkedHashSet<MultiKey>(gcTableSyncTableDataFrom.allPrimaryKeys());
        
        inserts += runInserts(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToInsert, 
            gcTableSyncTableDataFrom.allIndexByPrimaryKey(), "inserts");

      }      
    }
    recordsUpdated += inserts;
    
    return recordsUpdated;
  }

  /**
   * get all columns based on certain primary keys.  do this with batches of bind vars
   * @param gcTableSyncTableBean
   * @param primaryKeys
   * @param isFrom true if "from" or true if "to"
   * @return the data
   */
  private static GcTableSyncTableData runQueryForAllDataFromPrimaryKeys(final Map<String, Object> debugMap,
      GcTableSyncTableBean gcTableSyncTableBean, 
      Set<MultiKey> primaryKeys, boolean isFrom) {

    Map<MultiKey, GcTableSyncRowData> results = new LinkedHashMap<MultiKey, GcTableSyncRowData>();
    int numberOfRecordsToSelect = GrouperClientUtils.length(primaryKeys);
    
    if (numberOfRecordsToSelect > 0) {
      
      int numberOfPrimaryKeyColumns = GrouperClientUtils.length(primaryKeys.iterator().next().getKeys());
      
      //  7 primary keys
      //  // dont have more than 
      //  900 - batch size 128
      //  100 - batch size 13
      //  3 - batch size 1
      
      int maxBindVarsInSelect = gcTableSyncTableBean.getGcTableSync().getGcTableSyncConfiguration().getMaxBindVarsInSelect();
      
      int batchSize = maxBindVarsInSelect / numberOfPrimaryKeyColumns;
      // cant be less than 1
      batchSize = batchSize < 1 ? 1 : batchSize;

      int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(primaryKeys, batchSize);

      List<MultiKey> primaryKeysList = new ArrayList<MultiKey>(primaryKeys);
      
      String queryWherePrimaryKey = gcTableSyncTableBean.getTableMetadata().queryWherePrimaryKey();
      
      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        List<MultiKey> primaryKeyBatch = GrouperClientUtils.batchList(primaryKeysList, batchSize, batchIndex);
        
        Object[] bindVars = new Object[GrouperClientUtils.length(primaryKeyBatch) * numberOfPrimaryKeyColumns];
        int bindVarIndex = 0;
        
        StringBuilder sql = new StringBuilder("select " + gcTableSyncTableBean.getTableMetadata().columnListAll() 
            + " from " + gcTableSyncTableBean.getTableMetadata().getTableName() 
            + " where ");
        
        for (int primaryKeyIndex = 0; primaryKeyIndex < GrouperClientUtils.length(primaryKeyBatch); primaryKeyIndex++) {
          
          if (primaryKeyIndex > 0) {
            sql.append(" or ");
          }
          sql.append(" ( ");
          sql.append(queryWherePrimaryKey);
          sql.append(" ) ");

          // assign bind vars
          MultiKey primaryKey = primaryKeyBatch.get(primaryKeyIndex);
          for (Object primaryKeyValue : primaryKey.getKeys()) {
            bindVars[bindVarIndex++] = primaryKeyValue;
          }
          
          
        }

        GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSyncTableBean, 
            sql.toString(), gcTableSyncTableBean.getTableMetadata().columnListAll(), "selectAllColumns", bindVars, isFrom );
        for (GcTableSyncRowData gcTableSyncRowData : gcTableSyncTableData.getRows()) {
          results.put(gcTableSyncRowData.getPrimaryKey(), gcTableSyncRowData);
        }
        
      }
    }
    GcTableSyncTableData gcTableSyncTableData = new GcTableSyncTableData();
    gcTableSyncTableData.init(gcTableSyncTableBean, gcTableSyncTableBean.getTableMetadata().getColumnMetadata(), results);

    return gcTableSyncTableData;
  }
}
