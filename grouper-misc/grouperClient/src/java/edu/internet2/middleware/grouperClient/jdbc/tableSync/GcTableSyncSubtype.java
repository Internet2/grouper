/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveData", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      gcTableSync.getGcGrouperSync().setRecordsCount(GrouperClientUtils.length(gcTableSyncTableDatas[0].getRows()));
      gcTableSync.getGcGrouperSync().store();

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

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveGroups", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);


      String sqlCountFrom = "select count(1) from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      int count = new GcDbAccess().connectionName(gcTableSync.getDataBeanFrom().getTableMetadata().getConnectionName()).sql(sqlCountFrom).select(int.class);
      gcTableSync.getGcGrouperSync().setRecordsCount(count);
      gcTableSync.getGcGrouperSync().store();
      
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

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveChangeFlag", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      gcTableSync.getGcGrouperSync().setRecordsCount(GrouperClientUtils.length(gcTableSyncTableDatas[0].getRows()));
      gcTableSync.getGcGrouperSync().store();

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
      
      GcTableSyncTableData gcTableSyncTableDataTo = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToRetrieve, false);
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), gcTableSyncTableDataTo);
      int recordsChanged = results[0] + results[1] + results[2];

      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);
      gcTableSync.getGcGrouperSync().store();

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
      
      retrieveIncrementalDataHelper(debugMap, gcTableSync, gcTableSync.getDataBeanFrom(), gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll());

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
      
      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndToForAllDataFromPrimaryKeys(debugMap, gcTableSync, primaryKeysToRetrieve);
      
      GcTableSyncTableData gcTableSyncTableDataFrom = gcTableSyncTableDatas[0];
      GcTableSyncTableData gcTableSyncTableDataTo = gcTableSyncTableDatas[1];
      
      int[] results = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDataFrom, gcTableSyncTableDataTo);   
      int recordsChanged = results[0] + results[1] + results[2];
      
      gcTableSync.getGcGrouperSync().setRecordsCount(gcTableSync.getGcGrouperSync().getRecordsCount() + results[0] - results[2]);
      gcTableSync.getGcGrouperSync().store();

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
          use the primar key of from table!
          gcTableSync.getDataBeanRealTime().getTableMetadata().columnListPrimaryKeyAndIncrementalProgressColumn(gcTableSync.getDataBeanFrom().getTableMetadata().getPrimaryKey()));

    }

    
  };
  
  /**
   * get incremental data either from FROM table or from real time table
   * @param debugMap
   * @param gcTableSync
   * @param gcDbAccessMaxSql
   * @param columnTypeProgressColumn
   * @param sqlGetIncrementals
   * @param gcTableSyncTableBeanSelectFrom
   */
  private static void retrieveIncrementalDataHelper(Map<String, Object> debugMap, GcTableSync gcTableSync, 
      GcTableSyncTableBean gcTableSyncTableBeanSelectFrom, String columnsToSelect) {
    
    // where are we in the list?  do the whole list if we dont know where we are?
    Long lastRetrievedLong = gcTableSync.getGcGrouperSync().getIncrementalIndexOrMillis();
    Timestamp lastRetrievedTimestamp = gcTableSync.getGcGrouperSync().getIncrementalTimestamp();

    Object lastRetrieved = null;

    ColumnType columnTypeProgressColumn = gcTableSyncTableBeanSelectFrom.getTableMetadata().getIncrementalProgressColumn().getColumnType();
    
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
          + " where " + gcTableSyncTableBeanSelectFrom.getTableMetadata().getIncrementalProgressColumn().getColumnName()
          + " > ?";
  
      GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSyncTableBeanSelectFrom, 
          sqlGetIncrementals, "incrementalChanges", new Object[] {lastRetrieved}, true );
  
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
      debugMap.put("initialMaxProgress", maxProgress);
    }
    
    if (maxProgressAllColumns != null) {
      debugMap.put("initialMaxProgressAllColumns", maxProgressAllColumns);
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
  private static GcTableSyncTableData runQuery(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBean, String sql, 
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
      gcTableSyncTableData.init(gcTableSyncTableBean, gcTableSyncTableMetadata.getColumnMetadata(), results);
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
      logIncrement(debugMap, queryLogLabel + "Millis", (int)((System.nanoTime() - start)/1000));
    }
  }

  /**
   * 
   * @param debugMap
   * @param label
   * @param amountToAdd
   */
  private static void logIncrement(final Map<String, Object> debugMap, String label, int amountToAdd) {

    Integer currentValue = (Integer)debugMap.get(label);
    
    if (currentValue != null) {
      amountToAdd += currentValue;
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
          gcTableSync.getGcGrouperSyncJob().setLastSyncIndexOrMillis(maxProgressMillis);
          gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(maxProgressMillis));
          gcTableSync.getGcGrouperSync().setIncrementalIndexOrMillis(maxProgressMillis);
          gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(maxProgressMillis));
        }

      } else if (maxProgress instanceof Number) {
          
        long maxProgressLong = ((Number)maxProgress).longValue();

        gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(millisWhenJobStarted));
        gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(millisWhenJobStarted));
        madeProgress = true;

        if (gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis() == null || gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis() < maxProgressLong ) {

          gcTableSync.getGcGrouperSyncJob().setLastSyncIndexOrMillis(maxProgressLong);
          gcTableSync.getGcGrouperSync().setIncrementalIndexOrMillis(maxProgressLong);
        }
      } else if (maxProgress instanceof String) {

        long maxProgressLong = GrouperClientUtils.longObjectValue((String)maxProgress, false);

        gcTableSync.getGcGrouperSyncJob().setLastSyncTimestamp(new Timestamp(millisWhenJobStarted));
        gcTableSync.getGcGrouperSync().setIncrementalTimestamp(new Timestamp(millisWhenJobStarted));
        madeProgress = true;

        if (gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis() == null || gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis() < maxProgressLong ) {

          gcTableSync.getGcGrouperSync().setIncrementalIndexOrMillis(maxProgressLong);
        }
      }

      if (madeProgress) {
        gcTableSync.getGcGrouperSyncJob().store();
        gcTableSync.getGcGrouperSync().store();
      }
    }

  }
  
  /**
   * 
   * @param debugMap
   * @param dataBeanTo
   * @param primaryKeysToInsert
   * @param allIndexByPrimaryKey
   * @param queryLogLabel
   * @return the number inserted
   */
  private static int runInserts(Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo,
      Set<MultiKey> primaryKeysToInsert,
      Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey, String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {
      int[] results = null;
      
      if (GrouperClientUtils.length(primaryKeysToInsert) > 0) {
        //
        GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
        sql = "insert into " 
            + gcTableSyncTableMetadata.getTableName() + " ( " 
            + gcTableSyncTableMetadata.columnListAll() 
            +  " ) values ( " + GrouperClientUtils.appendQuestions(GrouperClientUtils.length(gcTableSyncTableMetadata.getColumns())) +  " )" ;
  
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
  
        List<List<Object>> bindVars = convertToListOfBindVarsValues(primaryKeysToInsert, allIndexByPrimaryKey);
        
        gcDbAccess.batchBindVars(bindVars);
  
        gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
        
        results = gcDbAccess.sql(sql).executeBatchSql();
      }
      
      int inserts = GrouperClientUtils.length(primaryKeysToInsert);
      int actualInserts = 0;
      int missedInserts = 0;
      int multiInsertInstances = 0;
      
      if (GrouperClientUtils.length(results) > 0) {
        for (int result : results) {
          actualInserts += result;
          if (result == 0) {
            missedInserts++;
          }
          // why would this happen???
          if (result>1) {
            multiInsertInstances++;
          }
        }
      }
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

      return actualInserts;
      
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
  private static int runDeletes(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo, Set<MultiKey> primaryKeysToDelete, 
      String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {
      int[] results = null;
      
      if (GrouperClientUtils.length(primaryKeysToDelete) > 0) {
        //
        GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
        sql = "delete from " + gcTableSyncTableMetadata.getTableName() + " where "
            + gcTableSyncTableMetadata.queryWherePrimaryKey();
  
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
  
        List<List<Object>> bindVars = convertToListOfBindVars(primaryKeysToDelete);
        
        gcDbAccess.batchBindVars(bindVars);
  
        gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
        
        results = gcDbAccess.sql(sql).executeBatchSql();
      }
      
      int deletes = GrouperClientUtils.length(primaryKeysToDelete);
      int actualDeletes = 0;
      int missedDeletes = 0;
      int multiDeleteInstances = 0;
      
      if (GrouperClientUtils.length(results) > 0) {
        for (int result : results) {
          actualDeletes += result;
          if (result == 0) {
            missedDeletes++;
          }
          if (result>1) {
            multiDeleteInstances++;
          }
        }
      }
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
   * 
   * @param setOfMultiKeys
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVarsValues (
      Set<MultiKey> setOfMultiKeys, Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey) {
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
      Set<MultiKey> setOfMultiKeys) {
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
      final GcTableSync gcTableSync, final String sqlFrom, final String sqlTo, final String queryLogLabel, final Object[] bindVars) {

    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    final GcTableSyncTableData[] result = new GcTableSyncTableData[2];
    //lets get all from one side and the other and time it and do it in a thread so its faster
    Thread selectFromThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        try {
          result[0] = runQuery(debugMap, gcTableSync.getDataBeanFrom(), sqlFrom, queryLogLabel + "From", bindVars, true );
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
      result[1] = runQuery(debugMap, gcTableSync.getDataBeanTo(), sqlTo, queryLogLabel + "To", bindVars, false );
    } catch (RuntimeException re) {
      if (RUNTIME_EXCEPTION[0] != null) {
        LOG.error("error", RUNTIME_EXCEPTION[0]);
      }
      RUNTIME_EXCEPTION[0] = re;
    }
    
    GrouperClientUtils.join(selectFromThread);
    if (RUNTIME_EXCEPTION[0] != null) {
      throw RUNTIME_EXCEPTION[0];
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
          actualDeletes += result;
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

    gcTableSync.getGcGrouperSync().store();

    return results ;
  }

  /**
   * 
   * @param setOfMultiKeys
   * @return the list of bind vars
   */
  private static List<List<Object>> convertToListOfBindVarsUpdate (
      Set<MultiKey> setOfMultiKeys, Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey) {
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
   * @param dataBeanTo
   * @param primaryKeysToUpdate
   * @param allIndexByPrimaryKey
   * @param queryLogLabel
   * @return the number inserted
   */
  private static int runUpdates(Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBeanTo,
      Set<MultiKey> primaryKeysToUpdate,
      Map<MultiKey, GcTableSyncRowData> allIndexByPrimaryKey, String queryLogLabel) {
    long start = System.nanoTime();
    String sql = null;
    try {

      int[] results = null;
      
      if (GrouperClientUtils.length(primaryKeysToUpdate) > 0) {
        //
        GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
        sql = "update " 
            + gcTableSyncTableMetadata.getTableName() + " set  " 
            + gcTableSyncTableMetadata.queryUpdateNonPrimaryKey()
            +  " where " + gcTableSyncTableMetadata.queryWherePrimaryKey() ;
    
        GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
    
        List<List<Object>> bindVars = convertToListOfBindVarsUpdate(primaryKeysToUpdate, allIndexByPrimaryKey);
        
        gcDbAccess.batchBindVars(bindVars);
    
        gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
        
        results = gcDbAccess.sql(sql).executeBatchSql();
      }
      
      int updates = GrouperClientUtils.length(primaryKeysToUpdate);
      int actualUpdates = 0;
      int missedUpdates = 0;
      int multiUpdateInstances = 0;

      if (GrouperClientUtils.length(results) > 0) {
        for (int result : results) {
          actualUpdates += result;
          if (result == 0) {
            missedUpdates++;
          }
          // why would this happen???
          if (result>1) {
            multiUpdateInstances++;
          }
        }
      }
      debugMap.put(queryLogLabel + "Count", actualUpdates);
      if (updates != actualUpdates) {
        debugMap.put(queryLogLabel + "IntendedCount", updates);
      }
      if (missedUpdates > 0) {
        debugMap.put(queryLogLabel + "MissedUpdates", missedUpdates);
      }
      if (multiUpdateInstances > 0) {
        debugMap.put(queryLogLabel + "PrimaryKeyProblems", multiUpdateInstances);
      }
      gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncOutput().addUpdate(actualUpdates);

      return actualUpdates;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      //temporarily store as micros, then divide in the end
      debugMap.put(queryLogLabel + "Millis", (System.nanoTime() - start)/1000);
    }
  }

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
  
        GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, 
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
      
      int numberOfPrimaryKeyColumns = GrouperClientUtils.length(primaryKeys.iterator().next());
      
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
            sql.toString(), "selectAllColumns", bindVars, isFrom );
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
