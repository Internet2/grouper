/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
      
      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListAll() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveData", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

    }
    

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      return runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery());      
            
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
      String sqlFrom = "select distinct " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select distinct " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveGroups", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);

      
    }


    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      Integer recordsChanged = runInsertsUpdatesDeletesGroupings(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery().allGroupings(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery().allGroupings());      
      
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

      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress() + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName();
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress() + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName();

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, "retrieveChangeFlag", null);
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDatas[0]);
      gcTableSync.getDataBeanTo().setDataInitialQuery(gcTableSyncTableDatas[1]);
      
    }

    /**
     * 
     */
    @Override
    public Integer syncData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      return runInsertsUpdatesDeletesChangeFlag(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), 
          gcTableSync.getDataBeanTo().getDataInitialQuery());      
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
      
      // get all data from destination based on primary key
      Set<MultiKey> primaryKeysToRetrieve = gcTableSync.getDataBeanFrom().getDataInitialQuery().allPrimaryKeys();
      
      GcTableSyncTableData gcTableSyncTableDataTo = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToRetrieve);
      
      int recordsChanged = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSync.getDataBeanFrom().getDataInitialQuery(), gcTableSyncTableDataTo);
      
      // update the real time incremented col...
      assignMaxProgress(gcTableSync.getDataBeanFrom().getDataInitialQuery(), true);

      return recordsChanged;
      
    }

    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {
      
      // where are we in the list?  do the whole list if we dont know where we are?
      Object lastRetrieved = GrouperClientUtils.defaultIfNull(gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis(), 0L);

      //TODO get a count at some point to see if full sync is better?
      
      //lets see what type the lastRetrieved column type is
      ColumnType columnType = gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn().getColumnType();

      // make sure right type
      lastRetrieved = columnType.convertToType(lastRetrieved);

      String sqlLastRetrieved = "select " 
          + gcTableSync.getDataBeanRealTime().getTableMetadata().columnListAll()
          + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName() 
          + " where " + gcTableSync.getDataBeanFrom().getTableMetadata().getIncrementalProgressColumn().getColumnName()
          + " > ?";
          
      GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSync.getDataBeanFrom(), 
          sqlLastRetrieved, "incrementalChanges", new Object[] {lastRetrieved} );
      
     
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableData);
      
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
      
      // get all data from destination based on primary key
      Set<MultiKey> primaryKeysToRetrieve = gcTableSync.getDataBeanFrom().getDataInitialQuery().allPrimaryKeys();
      
      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndToForAllDataFromPrimaryKeys(debugMap, gcTableSync, primaryKeysToRetrieve);
      
      GcTableSyncTableData gcTableSyncTableDataFrom = gcTableSyncTableDatas[0];
      GcTableSyncTableData gcTableSyncTableDataTo = gcTableSyncTableDatas[1];
      
      int recordsChanged = runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDataFrom, gcTableSyncTableDataTo);
      
      // update the real time incremented col...
      assignMaxProgress(gcTableSync.getDataBeanFrom().getDataInitialQuery(), true);
      
      return recordsChanged;
      
    }


    /**
     * 
     */
    @Override
    public void retrieveData(Map<String, Object> debugMap, GcTableSync gcTableSync) {

      // where are we in the list?  do the whole list if we dont know where we are?
      Object lastRetrieved = GrouperClientUtils.defaultIfNull(gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis(), 0L);

      //TODO get a count at some point to see if full sync is better?
      
      //lets see what type the lastRetrieved column type is
      ColumnType columnType = gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn().getColumnType();
      
      switch (columnType) {
        case NUMERIC:
          // leave it, its numeric
          break;
        case STRING:
          lastRetrieved = GrouperClientUtils.stringValue(lastRetrieved);
          break;
        case TIMESTAMP:
          lastRetrieved = new Timestamp((Long)lastRetrieved);
          break;
        default:
          throw new RuntimeException("Not expecting type: " + columnType);
      }
      
      String sqlLastRetrieved = "select " 
          + gcTableSync.getDataBeanRealTime().getTableMetadata().columnListPrimaryKeyAndIncrementalProgressColumn()
          + " from " + gcTableSync.getDataBeanRealTime().getTableMetadata().getTableName() 
          + " where " + gcTableSync.getDataBeanRealTime().getTableMetadata().getIncrementalProgressColumn().getColumnName()
          + " > ?";
          
      GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSync.getDataBeanFrom(), 
          sqlLastRetrieved, "incrementalChanges", new Object[] {lastRetrieved} );
      
     
      gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableData);

      
    }

    
  };
  
  /**
   * 
   * @param gcTableSyncTableBean
   * return the data
   */
  private static GcTableSyncTableData runQuery(final Map<String, Object> debugMap, GcTableSyncTableBean gcTableSyncTableBean, String sql, 
      String queryLogLabel, Object[] bindVars) {
    long start = System.nanoTime();
    try {
      //
      GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBean.getTableMetadata();

      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionNameOrReadonly());
      if (bindVars != null) {
        gcDbAccess.bindVars(bindVars);
      }
      List<Object[]> results = gcDbAccess.sql(sql).selectList(Object[].class);

      GcTableSyncTableData gcTableSyncTableData = new GcTableSyncTableData();
      gcTableSyncTableData.init(gcTableSyncTableBean, gcTableSyncTableMetadata.getColumnMetadata(), results);
      logIncrement(debugMap, queryLogLabel + "Count", GrouperClientUtils.length(results));

      return gcTableSyncTableData;
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query '" + sql + "', " + GrouperClientUtils.toStringForLog(bindVars));
      throw re;
    } finally {
      logIncrement(debugMap, queryLogLabel + "Millis", (int)((System.nanoTime() - start)/1000000));
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
   */
  private static void assignMaxProgress(GcTableSyncTableData gcTableSyncTableData, boolean requireProgressColumn) {
    GcTableSync gcTableSync = gcTableSyncTableData.getGcTableSyncTableBean().getGcTableSync();

    if (GrouperClientUtils.isBlank(gcTableSync.getGcTableSyncConfiguration().getIncrementalProgressColumnString())) {
      if (requireProgressColumn) {
        throw new RuntimeException("Incremental progress column configuration is required!");
      }
      return;
    }
    // update the real time incremented col...
    Object maxProgress = gcTableSyncTableData.maxIncrementalProgressValue();
    
    if (maxProgress != null) {
      Long maxProgressLong = (Long)ColumnType.NUMERIC.convertToType(maxProgress);
      if (maxProgressLong > gcTableSync.getGcGrouperSyncJob().getLastSyncIndexOrMillis()) {
        gcTableSync.getGcGrouperSyncJob().setLastSyncIndexOrMillis(maxProgressLong);
        gcTableSync.getGcGrouperSyncJob().store();
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
      
      int[] results = gcDbAccess.sql(sql).executeBatchSql();

      int inserts = GrouperClientUtils.length(primaryKeysToInsert);
      int actualInserts = 0;
      int missedInserts = 0;
      int multiInsertInstances = 0;
      
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
      return actualInserts;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      logIncrement(debugMap,queryLogLabel + "Millis", (int)((System.nanoTime() - start)/1000000));
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
      //
      GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
      sql = "delete from " + gcTableSyncTableMetadata.getTableName() + " where "
          + gcTableSyncTableMetadata.queryWherePrimaryKey();

      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());

      List<List<Object>> bindVars = convertToListOfBindVars(primaryKeysToDelete);
      
      gcDbAccess.batchBindVars(bindVars);

      gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
      
      int[] results = gcDbAccess.sql(sql).executeBatchSql();

      int deletes = GrouperClientUtils.length(primaryKeysToDelete);
      int actualDeletes = 0;
      int missedDeletes = 0;
      int multiDeleteInstances = 0;
      
      for (int result : results) {
        actualDeletes += result;
        if (result == 0) {
          missedDeletes++;
        }
        if (result>1) {
          multiDeleteInstances++;
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
      return actualDeletes;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      logIncrement(debugMap,queryLogLabel + "Millis",(int)( (System.nanoTime() - start)/1000000));
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

    final RuntimeException[] RUNTIME_EXCEPTION = new RuntimeException[1];
    final GcTableSyncTableData[] result = new GcTableSyncTableData[2];
    //lets get all from one side and the other and time it and do it in a thread so its faster
    Thread selectFromThread = new Thread(new Runnable() {
      
      @Override
      public void run() {
        
        try {
          result[0] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanFrom(), primaryKeys);
        } catch (RuntimeException re) {
          if (RUNTIME_EXCEPTION[0] != null) {
            LOG.error("Error retrieve by primary key", re);
          }
          RUNTIME_EXCEPTION[0] = re;
        }
        
      }
    });
    
    selectFromThread.start();
    
    result[1] = runQueryForAllDataFromPrimaryKeys(debugMap, gcTableSync.getDataBeanTo(), primaryKeys);
    
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
          result[0] = runQuery(debugMap, gcTableSync.getDataBeanFrom(), sqlFrom, queryLogLabel + "From", bindVars );
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
      result[1] = runQuery(debugMap, gcTableSync.getDataBeanTo(), sqlTo, queryLogLabel + "To", bindVars );
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
      //
      GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
      sql = "delete from " + gcTableSyncTableMetadata.getTableName() + " where "
          + gcTableSyncTableMetadata.getGroupColumnMetadata().getColumnName() + " = ?";
  
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
  
      List<List<Object>> bindVars = convertToListOfBindVarsObjects(groupingsToDelete);
      
      gcDbAccess.batchBindVars(bindVars);
  
      gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
      
      int[] results = gcDbAccess.sql(sql).executeBatchSql();
  
      int deletes = GrouperClientUtils.length(groupingsToDelete);
      int actualDeletes = 0;
      int missedDeletes = 0;
      int multiDeleteInstances = 0;
      
      for (int result : results) {
        actualDeletes += result;
        if (result == 0) {
          missedDeletes++;
        }
        if (result>1) {
          multiDeleteInstances++;
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
      return actualDeletes;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      logIncrement(debugMap,queryLogLabel + "Millis",(int)( (System.nanoTime() - start)/1000000));
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
          gcTableSyncTableDataFrom.getGcTableSyncTableBean(), primaryKeysToInsert);
      
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
          gcTableSyncTableDataFrom.getGcTableSyncTableBean(), primaryKeysToUpdate);
  
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
   * @return records changed
   */
  private static int runInsertsUpdatesDeletes(Map<String, Object> debugMap,
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
      
      inserts = runInserts(debugMap, gcTableSync.getDataBeanTo(), primaryKeysToInsert, 
          gcTableSyncTableDataFrom.allIndexByPrimaryKey(), "inserts");
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
      
    }
    
    assignMaxProgress(gcTableSyncTableDataFrom, false);

    return inserts + updates + deletes;
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
      //
      GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanTo.getTableMetadata();
      sql = "update " 
          + gcTableSyncTableMetadata.getTableName() + " set  " 
          + gcTableSyncTableMetadata.queryWherePrimaryKey()
          +  " where " + gcTableSyncTableMetadata.queryWherePrimaryKey() ;
  
      GcDbAccess gcDbAccess = new GcDbAccess().connectionName(gcTableSyncTableMetadata.getConnectionName());
  
      List<List<Object>> bindVars = convertToListOfBindVarsUpdate(primaryKeysToUpdate, allIndexByPrimaryKey);
      
      gcDbAccess.batchBindVars(bindVars);
  
      gcDbAccess.batchSize(gcTableSyncTableBeanTo.getGcTableSync().getGcTableSyncConfiguration().getBatchSize());
      
      int[] results = gcDbAccess.sql(sql).executeBatchSql();
  
      int inserts = GrouperClientUtils.length(primaryKeysToUpdate);
      int actualInserts = 0;
      int missedInserts = 0;
      int multiInsertInstances = 0;
      
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
      debugMap.put(queryLogLabel + "Count", actualInserts);
      if (inserts != actualInserts) {
        debugMap.put(queryLogLabel + "IntendedCount", inserts);
      }
      if (missedInserts > 0) {
        debugMap.put(queryLogLabel + "MissedInserts", missedInserts);
      }
      if (multiInsertInstances > 0) {
        debugMap.put(queryLogLabel + "PrimaryKeyProblems", multiInsertInstances);
      }
      return actualInserts;
      
    } catch (RuntimeException re) {
      GrouperClientUtils.injectInException(re, "Error in '" + queryLogLabel + "' query: '" + sql + "'");
      throw re;
    } finally {
      debugMap.put(queryLogLabel + "Millis", (System.nanoTime() - start)/1000000);
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
      
      String sqlFrom = "select " + gcTableSync.getDataBeanFrom().getTableMetadata().columnListAll() 
          + " from " + gcTableSync.getDataBeanFrom().getTableMetadata().getTableName()
          + " where " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " >= ? "
          + " and " + gcTableSync.getDataBeanFrom().getTableMetadata().getGroupColumnMetadata().getColumnName() + " <= ? ";
      String sqlTo = "select " + gcTableSync.getDataBeanTo().getTableMetadata().columnListAll() 
          + " from " + gcTableSync.getDataBeanTo().getTableMetadata().getTableName()
          + " where " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " >= ? "
          + " and " + gcTableSync.getDataBeanTo().getTableMetadata().getGroupColumnMetadata().getColumnName() + " <= ? ";

      GcTableSyncTableData[] gcTableSyncTableDatas = runQueryFromAndTo(debugMap, gcTableSync, sqlFrom, sqlTo, 
          "retrieveData", new Object[] {groupingsBatch.get(0), groupingsBatch.size()-1});

      recordsUpdated += runInsertsUpdatesDeletes(debugMap, gcTableSync, gcTableSyncTableDatas[0], 
          gcTableSyncTableDatas[1]);    
      
    }
    
    return recordsUpdated;
  }

  /**
   * get all columns based on certain primary keys.  do this with batches of bind vars
   * @param gcTableSyncTableBean
   * @param primaryKeys
   * @return the data
   */
  private static GcTableSyncTableData runQueryForAllDataFromPrimaryKeys(final Map<String, Object> debugMap,
      GcTableSyncTableBean gcTableSyncTableBean, 
      Set<MultiKey> primaryKeys) {

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
          sql.append(" ( ");

          // assign bind vars
          MultiKey primaryKey = primaryKeyBatch.get(primaryKeyIndex);
          for (Object primaryKeyValue : primaryKey.getKeys()) {
            bindVars[bindVarIndex++] = primaryKeyValue;
          }
          
          
        }

        GcTableSyncTableData gcTableSyncTableData = runQuery(debugMap, gcTableSyncTableBean, 
            sql.toString(), "selectAllColumns", bindVars );
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
