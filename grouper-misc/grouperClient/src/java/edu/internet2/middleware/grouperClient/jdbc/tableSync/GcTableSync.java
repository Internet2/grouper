/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcConnectionCallback;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcResultSetCallback;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.time.DurationFormatUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * sync a table
 */
public class GcTableSync {

  /**
   * log every minute
   */
  private long lastLog = System.currentTimeMillis();
  
  /**
   * key in config that points to this instance of table sync
   */
  private String key;
  
  
  /**
   * key in config that points to this instance of table sync
   * @return the key
   */
  public String getKey() {
    return this.key;
  }

  
  /**
   * key in config that points to this instance of table sync
   * @param key1 the key to set
   */
  public void setKey(String key1) {
    this.key = key1;
  }

  /**
   * 
   */
  private GcTableSyncTableMetadata tableMetadata;
  
  
  /**
   * @return the tableMetadata
   */
  public GcTableSyncTableMetadata getTableMetadata() {
    return this.tableMetadata;
  }


  
  /**
   * @param tableMetadata1 the tableMetadata to set
   */
  public void setTableMetadata(GcTableSyncTableMetadata tableMetadata1) {
    this.tableMetadata = tableMetadata1;
  }

  /**
   * data in the from table
   */
  private GcTableSyncTableData fromData;
  
  
  /**
   * data in the from table
   * @return the fromData
   */
  public GcTableSyncTableData getFromData() {
    return this.fromData;
  }


  
  /**
   * data in the from table
   * @param fromData1 the fromData to set
   */
  public void setFromData(GcTableSyncTableData fromData1) {
    this.fromData = fromData1;
  }

  /**
   * data in the to table
   */
  private GcTableSyncTableData toData;
  
  /**
   * data in the to table
   * @return the toData
   */
  public GcTableSyncTableData getToData() {
    return this.toData;
  }
  
  /**
   * data in the to table
   * @param toData1 the toData to set
   */
  public void setToData(GcTableSyncTableData toData1) {
    this.toData = toData1;
  }

  /**
   * 
   */
  public GcTableSync() {
  }

  /**
   * 
   * @param debugMap 
   * @return true if running, false if not
   */
  public boolean statusIsFullRunning(final Map<String, Object> debugMap) {
   
    final GcTableSyncColumnMetadata realTimeLastUpdatedColumnMetadata = this.tableMetadata.getRealTimeLastUpdatedColumnMetadata();

    // where we at with full?
    String sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusSchema() + "." + this.getStatusTable()
        + " where name = ?"; 

    Timestamp fullLastUpdated = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar("tableSync_full_" + this.key).select(Timestamp.class);
    
    if (fullLastUpdated != null && (System.currentTimeMillis() - fullLastUpdated.getTime() ) < (1000 * 60 * 5 )  ) {
      
      debugMap.put("fullSyncIsRunning", true);
      return true;
    }
    
    return false;
  }
  
  /**
   * @return  the output
   * 
   */
  public GcTableSyncOutput incrementalSync() {
    
    GcTableSyncOutput gcTableSyncOutput = new GcTableSyncOutput();
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    int rowsSelectedFrom = 0;
    int rowsSelectedTo = 0;
    int rowsWithEqualData = 0;
    int rowsNeedInsert = 0;
    int rowsNeedUpdate = 0;
    int rowsNeedDelete = 0;
    int rowsWithPrimaryKeyErrors = 0;
    int rowsWithDeleteErrors = 0;
    int rowsWithInsertErrors = 0;
    long now = System.currentTimeMillis();
    long millisOfLastRecordUpdated = now;
    final boolean[] done = new boolean[]{false};
    
    Map<String, List<List<Object>>> sqlBatch = new HashMap<String, List<List<Object>>>();

    try {

      debugMap.put("incrementalSync", true);
      debugMap.put("key", this.key);

      debugMap.put("finalLog", false);
      debugMap.put("state", "retrieveCount");

      configureTableSync();
      debugMap.put("databaseFrom", this.databaseFrom);
      debugMap.put("tableFrom", this.tableMetadata.getTableNameFrom());
      debugMap.put("databaseTo", this.databaseTo);
      debugMap.put("tableTo", this.tableMetadata.getTableNameTo());

      final GcTableSyncColumnMetadata realTimeLastUpdatedColumnMetadata = this.tableMetadata.getRealTimeLastUpdatedColumnMetadata();

      if (GcTableSync.this.statusIsFullRunning(debugMap)) {
        done[0] = true;
      }

      Thread thread = new Thread(new Runnable() {

        public void run() {
          
          try {
            while(true) {
              //if process is done then done
              GcTableSync.this.statusAssignLastUpdated("incremental", System.currentTimeMillis());

              for (int i=0;i<60;i++) {
                if (done[0]) {
                  return;
                }
                Thread.sleep(1000);
                if (done[0]) {
                  return;
                }
              }
              if (GcTableSync.this.statusIsFullRunning(debugMap)) {
                done[0] = true;
              }
              logPeriodically(debugMap);
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning incremental status and logging", e);
          }
          
        }
        
      });
      
      thread.run();

      if (done[0]) {
        debugMap.put("fullIsRunning", true);
      } else {
      
        Long receiveUpdatesSince = statusRetrieveLastUpdated("incremental");
        
        // start with 5 minutes ago, dont get all updates ever
        if (receiveUpdatesSince == null) {
          receiveUpdatesSince = System.currentTimeMillis() - (1000 * 60 * 5);
        }
        
        String sql = "select " + this.tableMetadata.getRealTimeLastUpdatedColumnMetadata().getColumnName() + " from " + this.tableMetadata.getSchemaFrom() + "." + this.getRealTimeTable() + " where " 
            + realTimeLastUpdatedColumnMetadata.getColumnName() + " > ?";
  
        int total = (Integer)new GcDbAccess().connectionName(this.databaseFrom).sql(sql).callbackResultSet(new GcResultSetCallback() {
  
          @Override
          public Object callback(ResultSet resultSet) throws Exception {
            try {
              
              while (resultSet.next()) {
                Object value = realTimeLastUpdatedColumnMetadata.getColumnType().readDataFromResultSet(1, resultSet);
                GcTableSync.this.fromGroupingUniqueValues.add(value);
                debugMap.put("fromGroupingUniqueValues", GcTableSync.this.fromGroupingUniqueValues.size());
                logPeriodically(debugMap);
              }
              
            } finally {
              GrouperClientUtils.closeQuietly(resultSet);
            }
  
            return null;
          }
        });
        debugMap.put("totalCountFrom", total);
        gcTableSyncOutput.setTotal(total);
  
        if (total > 0) {
          // sorted unique values
          Collections.sort((List)this.fromGroupingUniqueValues);
          logPeriodically(debugMap);
    
          int realTimeBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable.personSource.realTimeBatchSize", 1000);
          
          // 900 since might be other ones sneaking in there since might have same last updated
          int maxBatchSize = 900 / this.tableMetadata.getPrimaryKey().size();
          
          if (realTimeBatchSize > maxBatchSize) {
            realTimeBatchSize = maxBatchSize;
          }
          
          //batch through to select from from
          int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(total, realTimeBatchSize);
          
          debugMap.put("numberOfBatches", numberOfBatches);
          
          for (int i=0;i<numberOfBatches;i++) {
            
            debugMap.put("currentBatch", i);
    
            debugMap.put("state", "retrieveFromRealTimeBatch");
  
            List<Object> groupingsFromBatch = GrouperClientUtils.batchList(this.fromGroupingUniqueValues, realTimeBatchSize, i);
  
            // get the from batch
            {
              StringBuilder sqlBuilder = new StringBuilder("select ");
        
              this.tableMetadata.appendColumns(sqlBuilder);
              
              List<Object> bindVars = new ArrayList<Object>();
              sqlBuilder.append(" from " + this.tableMetadata.getSchemaFrom() + "." + this.tableMetadata.getTableNameFrom() 
                  + " where " + realTimeLastUpdatedColumnMetadata.getColumnName() + " >= ? and " + realTimeLastUpdatedColumnMetadata.getColumnName() + " <= ?");
              // add first
              bindVars.add(groupingsFromBatch.get(0));
              // add last.  note, you might get more than you bargained for... maybe check for that?
              bindVars.add(groupingsFromBatch.get(groupingsFromBatch.size()-1));
              
              this.fromData = new GcTableSyncTableData();
              this.fromData.setGcTableSync(this);
              
              this.fromData.setRows(new ArrayList<GcTableSyncRowData>());
    
              retrieveDataBatchFromDb(this.databaseFrom, bindVars, sqlBuilder.toString(), GcTableSync.this.fromData.getRows(), GcTableSync.this.fromData);
              rowsSelectedFrom += this.fromData.getRows().size();
      
              debugMap.put("rowsSelectedFrom", rowsSelectedFrom);
              // logPeriodically(debugMap);  wait until we get the "to" groupings
            }
            
            // get the to batch
            debugMap.put("state", "retrieveToRealTimeBatch");
            {
              StringBuilder sqlBuilder = new StringBuilder("select ");
        
              this.tableMetadata.appendColumns(sqlBuilder);
              
              List<Object> bindVars = new ArrayList<Object>();
              
              sqlBuilder.append(" from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                + " where ");
              
              int rowNum=0;
              for (GcTableSyncRowData gcTableSyncRowData : this.fromData.getRows()) {
                
                if (rowNum > 0) {
                  sqlBuilder.append(" and ");
                }
                
                sqlBuilder.append(" ( ");
  
                this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
                MultiKey primaryKey = gcTableSyncRowData.getPrimaryKey();
                for (Object object : primaryKey.getKeys()) {
                  bindVars.add(object);
                }
  
                sqlBuilder.append(" ) ");
                rowNum++;
              }
                  
              this.toData = new GcTableSyncTableData();
              this.toData.setGcTableSync(this);
              
              this.toData.setRows(new ArrayList<GcTableSyncRowData>());
              
              retrieveDataBatchFromDb(this.databaseTo, bindVars, sqlBuilder.toString(), GcTableSync.this.toData.getRows(), GcTableSync.this.toData);
              rowsSelectedTo += this.toData.getRows().size();
      
              debugMap.put("rowsSelectedTo", rowsSelectedTo);
              logPeriodically(debugMap);
            }
      
            // index the to side for quick lookups
            this.toData.indexData();
                        
            debugMap.put("state", "compareBatch");
            // compare rows in the FROM
            for (GcTableSyncRowData sourceRow : this.fromData.getRows()) {
              
              MultiKey primaryKey = sourceRow.getPrimaryKey();
              
              // see if there is a corresponding row in the destination
              GcTableSyncRowData destinationRow = this.toData.findRowFromPrimaryKey(primaryKey);
              
              if (destinationRow != null) {
                
                if (sourceRow.equals(destinationRow)) {
                  rowsWithEqualData++;
                  debugMap.put("rowsWithEqualData", rowsWithEqualData);
                  continue;
                }
    
                //if it matches and isnt equal, thats an update
                StringBuilder sqlBuilder = new StringBuilder("update " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                    + " set " );
    
                this.tableMetadata.appendNonPrimaryKeyUpdateColumnNames(sqlBuilder);
                
                sqlBuilder.append(" where ");
    
                this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
                
                sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.nonPrimaryAndThenPrimaryKeyData());
                
              } else {
                //if not in destination and is in source, then insert
    
                StringBuilder sqlBuilder = new StringBuilder("insert into " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                    + " (" );
                
                this.tableMetadata.appendColumns(sqlBuilder);
                
                sqlBuilder.append(" ) values ( ");
    
                this.tableMetadata.appendQuestionsAllCols(sqlBuilder);
                
                sqlBuilder.append(")");
      
                sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.allColumnsData());
                
              }
            }
            
            logPeriodically(debugMap);
          }
          
          debugMap.put("state", "inserts");
    
          //run the batches
          for (int localResult : this.sqlBatchExecute(sqlBatch, "insert", debugMap)) {
            if (localResult != 1) {
              rowsWithInsertErrors++;
              debugMap.put("rowsWithInsertErrors", rowsWithInsertErrors);
              LOG.error("Rows inserted is not 1, its " + localResult + "!!!!");
            }
            rowsNeedInsert += localResult;
            debugMap.put("rowsNeedInsert", rowsNeedInsert);
          }
          debugMap.put("state", "updates");
          for (int localResult : this.sqlBatchExecute(sqlBatch, "update", debugMap)) {
            if (localResult != 1) {
              rowsWithPrimaryKeyErrors++;
              debugMap.put("rowsWithPrimaryKeyErrors", rowsWithPrimaryKeyErrors);
              LOG.error("Rows updated is not 1, its " + localResult + "!!!!");
            }
            rowsNeedUpdate += localResult;
            debugMap.put("rowsNeedUpdate", rowsNeedUpdate);
          }
          debugMap.put("state", "deletes");
          for (int localResult : this.sqlBatchExecute(sqlBatch, "delete", debugMap)) {
            if (localResult != 1) {
              rowsWithDeleteErrors++;
              debugMap.put("rowsWithDeleteErrors", rowsWithDeleteErrors);
              LOG.error("Rows deleted is not 1, its " + localResult + "!!!!");
            }
            rowsNeedDelete += localResult;
            debugMap.put("rowsNeedDelete", rowsNeedDelete);
          }
          
          if (sqlBatch.size() > 0) {
            throw new RuntimeException("Why is SQL batch more than 1???");
          }
        }
        
        // where we at with incremental, update the status?
        sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusSchema() + "." + this.getStatusTable()
            + " where name = ?"; 
  
        final String statusNameColumnName = "tableSync_incremental_" + this.key;
        Timestamp lastRun = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar(statusNameColumnName).select(Timestamp.class);
  
        if (lastRun == null) {
  
          sql = "insert into " + this.getStatusSchema() + "." + this.getStatusTable()
              + " ( name, last_sequence_processed, last_updated, created_on, id, hibernate_version_number ) values ( ?, ?, ?, ?, ?, ? )";
  
          new GcDbAccess().connectionName(this.statusDatabase).sql(sql).bindVars(new Object[] { statusNameColumnName, millisOfLastRecordUpdated, millisOfLastRecordUpdated, millisOfLastRecordUpdated, GrouperClientUtils.uuid(), 0}).select(Timestamp.class);
  
        } else {
          
          sql = "update " + this.getStatusSchema() + "." + this.getStatusTable()
              + " set last_updated = ? where name = ?";
  
          new GcDbAccess().connectionName(this.statusDatabase).sql(sql).bindVars(new Object[] { statusNameColumnName, millisOfLastRecordUpdated   }).select(Timestamp.class);
          
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
    } finally {
      debugMap.put("finalLog", true);

      debugMap.put("took", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-now));
      
      String debugString = GrouperClientUtils.mapToString(debugMap);
      GcTableSyncLog.debugLog(debugString);
      
      gcTableSyncOutput.setDelete(rowsNeedDelete);
      gcTableSyncOutput.setUpdate(rowsNeedUpdate);
      gcTableSyncOutput.setInsert(rowsNeedInsert);
      // already set total
      //gcTableSyncOutput.setTotal();
      gcTableSyncOutput.setMessage(debugString);
      
    }
    return gcTableSyncOutput;
  }

  /**
   * type is 
   * @param type full or incremental
   * @return the last updated for a status
   */
  public Long statusRetrieveLastUpdated(String type) {

    if (!StringUtils.equals(type, "full") && !StringUtils.equals(type, "incremental")) {
      throw new RuntimeException("Not expecting type: " + type);
    }
    
    final GcTableSyncColumnMetadata realTimeLastUpdatedColumnMetadata = this.tableMetadata.getRealTimeLastUpdatedColumnMetadata();

    // where we at with incremental, update the status?
    String sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusSchema() + "." + this.getStatusTable()
        + " where name = ?"; 

    final String statusNameColumnName = "tableSync_" + type +  "_" + this.key;
    Long lastRun = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar(statusNameColumnName).select(Long.class);
    
    return lastRun == null ? null : lastRun;

  }
  
  /**
   * 
   * @param type
   * @param lastUpdated
   */
  public void statusAssignLastUpdated(String type, long lastUpdated) {
  
    if (!StringUtils.isBlank(this.statusDatabase)) {
      
      final String statusNameColumnName = "tableSync_" + type +  "_" + this.key;
      String sql = null;
  
      sql = "update " + this.getStatusSchema() + "." + this.getStatusTable()
          + " set last_updated = ? where name = ?";
  
      int rowsUpdated = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).bindVars(new Object[] { statusNameColumnName, new Timestamp(lastUpdated) }).executeSql();
  
      if (rowsUpdated == 0) {
  
        sql = "insert into " + this.getStatusSchema() + "." + this.getStatusTable()
            + " ( name, last_sequence_processed, last_updated, created_on, id, hibernate_version_number ) values ( ?, ?, ?, ?, ?, ? )";
  
        new GcDbAccess().connectionName(this.statusDatabase).sql(sql).bindVars(new Object[] { statusNameColumnName, lastUpdated, lastUpdated, lastUpdated, GrouperClientUtils.uuid(), 0}).select(Timestamp.class);
  
      }
    }  
  }
  
  /**
   * @return  the output
   * 
   */
  public GcTableSyncOutput fullSync() {
    
    GcTableSyncOutput gcTableSyncOutput = new GcTableSyncOutput();
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    int rowsSelectedFrom = 0;
    int rowsSelectedTo = 0;
    int rowsWithEqualData = 0;
    int rowsNeedInsert = 0;
    int rowsNeedUpdate = 0;
    int rowsNeedDelete = 0;
    int rowsWithPrimaryKeyErrors = 0;
    int rowsWithDeleteErrors = 0;
    int rowsWithInsertErrors = 0;
    long now = System.currentTimeMillis();
    final boolean[] done = new boolean[]{false};

    Map<String, List<List<Object>>> sqlBatch = new HashMap<String, List<List<Object>>>();
    
    try {


      debugMap.put("fullSync", true);
      debugMap.put("key", this.key);
  
      debugMap.put("finalLog", false);
      debugMap.put("state", "retrieveCount");

      configureTableSync();
      debugMap.put("databaseFrom", this.databaseFrom);
      debugMap.put("tableFrom", this.tableMetadata.getTableNameFrom());
      debugMap.put("databaseTo", this.databaseTo);
      debugMap.put("tableTo", this.tableMetadata.getTableNameTo());

      Thread thread = new Thread(new Runnable() {

        public void run() {
          
          try {
            while(true) {
              //if process is done then done
              GcTableSync.this.statusAssignLastUpdated("full", System.currentTimeMillis());
              for (int i=0;i<60;i++) {
                if (done[0]) {
                  return;
                }
                Thread.sleep(1000);
                if (done[0]) {
                  return;
                }
              }
              logPeriodically(debugMap);
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning status and logging", e);
          }
          
        }
        
      });
      
      thread.start();
      
      final GcTableSyncColumnMetadata groupingColumnMetadata = this.tableMetadata.getGroupingColumnMetadata();

      String sql = "select count(*) from " + this.tableMetadata.getSchemaFrom() + "." + this.tableMetadata.getTableNameFrom();

      int total = new GcDbAccess().connectionName(this.databaseFrom).sql(sql).select(int.class);
      debugMap.put("totalCountFrom", total);
      gcTableSyncOutput.setTotal(total);
      
      debugMap.put("state", "retrieveAllFromGroupings");

      sql = "select distinct " + groupingColumnMetadata.getColumnName() + " from " + this.tableMetadata.getSchemaFrom() + "." + this.tableMetadata.getTableNameFrom();
      
      this.fromGroupingUniqueValues = new ArrayList<Object>();
      
      // lets get the from source group values
      new GcDbAccess().connectionName(this.databaseFrom).sql(sql).callbackResultSet(new GcResultSetCallback() {
  
        @Override
        public Object callback(ResultSet resultSet) throws Exception {
          try {
            
            while (resultSet.next()) {
              Object value = groupingColumnMetadata.getColumnType().readDataFromResultSet(1, resultSet);
              GcTableSync.this.fromGroupingUniqueValues.add(value);
              debugMap.put("fromGroupingUniqueValues", GcTableSync.this.fromGroupingUniqueValues.size());
            }
            
          } finally {
            GrouperClientUtils.closeQuietly(resultSet);
          }
          return null;
        }
      });

      // sorted unique values
      Collections.sort((List)this.fromGroupingUniqueValues);
      
      debugMap.put("state", "retrieveAllToGroupings");
  
      sql = "select distinct " + groupingColumnMetadata.getColumnName() + " from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo();
      
      this.toGroupingUniqueValues = new LinkedHashSet<Object>();
      
      // lets get the from source group values
      new GcDbAccess().connectionName(this.databaseTo).sql(sql).callbackResultSet(new GcResultSetCallback() {
  
        @Override
        public Object callback(ResultSet resultSet) throws Exception {
          try {
            
            while (resultSet.next()) {
              Object value = groupingColumnMetadata.getColumnType().readDataFromResultSet(1, resultSet);
              GcTableSync.this.toGroupingUniqueValues.add(value);
              debugMap.put("toGroupingUniqueValues", GcTableSync.this.toGroupingUniqueValues.size());
            }
            
          } finally {
            GrouperClientUtils.closeQuietly(resultSet);
          }
          return null;
        }
      });

      debugMap.put("state", "deleteGroupings");

      //delete batches which arent there
      Set<Object> groupingsToDelete = new LinkedHashSet<Object>(this.toGroupingUniqueValues);
      groupingsToDelete.removeAll(new HashSet<Object>(this.fromGroupingUniqueValues));
  
      debugMap.put("groupingsToDelete", groupingsToDelete.size());

      //batch through to delete
      int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(groupingsToDelete, 200);

      for (int i=0;i<numberOfBatches;i++) {

        List<Object> groupingsToDeleteBatch = GrouperClientUtils.batchList(groupingsToDelete, 200, i);
        
        if (GrouperClientUtils.length(groupingsToDeleteBatch) > 0) {
          StringBuilder sqlBuilder = new StringBuilder("delete from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
          GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsToDeleteBatch));
          sqlBuilder.append(")");
          sqlBatchAdd(sqlBatch, sqlBuilder.toString(), groupingsToDeleteBatch);
        }      

      }
      
      for (int localResult : this.sqlBatchExecute(sqlBatch, null, debugMap)) {
        rowsNeedDelete += localResult;
      }

      //batch through to select from from
      numberOfBatches = GrouperClientUtils.batchNumberOfBatches(this.fromGroupingUniqueValues, this.groupingSize);
      
      final boolean selectIndividual = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperClient.syncTable." + this.key + ".selectIndividual", false);
      
      debugMap.put("numberOfBatches", numberOfBatches);
      
      for (int i=0;i<numberOfBatches;i++) {
        
        debugMap.put("currentBatch", i);

        debugMap.put("state", "retrieveFromGroupingsOfBatch");

        List<Object> groupingsFromBatch = GrouperClientUtils.batchList(this.fromGroupingUniqueValues, this.groupingSize, i);
        
        // get the from batch
        {
          StringBuilder sqlBuilder = new StringBuilder("select ");
    
          this.tableMetadata.appendColumns(sqlBuilder);
          
          List<Object> bindVars = new ArrayList<Object>();
          if (selectIndividual) {
            sqlBuilder.append(" from " + this.tableMetadata.getSchemaFrom() + "." + this.tableMetadata.getTableNameFrom() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
              GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsFromBatch));
            sqlBuilder.append(")");
            bindVars = groupingsFromBatch;
          } else {
            sqlBuilder.append(" from " + this.tableMetadata.getSchemaFrom() + "." + this.tableMetadata.getTableNameFrom() 
                + " where " + groupingColumnMetadata.getColumnName() + " >= ? and " + groupingColumnMetadata.getColumnName() + " <= ?");
            // add first
            bindVars.add(groupingsFromBatch.get(0));
            // add last
            bindVars.add(groupingsFromBatch.get(groupingsFromBatch.size()-1));
          }
          
          this.fromData = new GcTableSyncTableData();
          this.fromData.setGcTableSync(this);
          
          this.fromData.setRows(new ArrayList<GcTableSyncRowData>());

          retrieveDataBatchFromDb(this.databaseFrom, bindVars, sqlBuilder.toString(), GcTableSync.this.fromData.getRows(), GcTableSync.this.fromData);
          rowsSelectedFrom += this.fromData.getRows().size();
  
          debugMap.put("rowsSelectedFrom", rowsSelectedFrom);
        }
        
        
        // get the to batch
        debugMap.put("state", "retrieveToGroupingsOfBatch");
        {
          StringBuilder sqlBuilder = new StringBuilder("select ");
    
          this.tableMetadata.appendColumns(sqlBuilder);
          
          
          List<Object> bindVars = new ArrayList<Object>();
          if (selectIndividual) {
            sqlBuilder.append(" from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
              GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsFromBatch));
            sqlBuilder.append(")");
            bindVars = groupingsFromBatch;
          } else {
            sqlBuilder.append(" from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                + " where " + groupingColumnMetadata.getColumnName() + " >= ? and " + groupingColumnMetadata.getColumnName() + " <= ?");
            // add first
            bindVars.add(groupingsFromBatch.get(0));
            // add last
            bindVars.add(groupingsFromBatch.get(groupingsFromBatch.size()-1));
          }

          this.toData = new GcTableSyncTableData();
          this.toData.setGcTableSync(this);
          
          this.toData.setRows(new ArrayList<GcTableSyncRowData>());
          
          retrieveDataBatchFromDb(this.databaseTo, bindVars, sqlBuilder.toString(), GcTableSync.this.toData.getRows(), GcTableSync.this.toData);
          rowsSelectedTo += this.toData.getRows().size();
  
          debugMap.put("rowsSelectedTo", rowsSelectedTo);
        }
  
        // index the to side for quick lookups
        this.toData.indexData();
        
        // put the keys in a delete set
        Set<MultiKey> keysToDelete = new HashSet<MultiKey>();
        for (GcTableSyncRowData gcTableSyncRowData : this.toData.getRows()) {
          keysToDelete.add(gcTableSyncRowData.getPrimaryKey());
        }
        
        debugMap.put("state", "compareBatch");
        // compare rows in the FROM
        for (GcTableSyncRowData sourceRow : this.fromData.getRows()) {
          
          MultiKey primaryKey = sourceRow.getPrimaryKey();
          
          // see if there is a corresponding row in the destination
          GcTableSyncRowData destinationRow = this.toData.findRowFromPrimaryKey(primaryKey);
          
          if (destinationRow != null) {
            
            keysToDelete.remove(primaryKey);
            
            if (sourceRow.equals(destinationRow)) {
              rowsWithEqualData++;
              debugMap.put("rowsWithEqualData", rowsWithEqualData);
              continue;
            }

            //if it matches and isnt equal, thats an update
            StringBuilder sqlBuilder = new StringBuilder("update " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                + " set " );

            this.tableMetadata.appendNonPrimaryKeyUpdateColumnNames(sqlBuilder);
            
            sqlBuilder.append(" where ");

            this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
            
            sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.nonPrimaryAndThenPrimaryKeyData());
            
          } else {
            //if not in destination and is in source, then insert

            StringBuilder sqlBuilder = new StringBuilder("insert into " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
                + " (" );
            
            this.tableMetadata.appendColumns(sqlBuilder);
            
            sqlBuilder.append(" ) values ( ");

            this.tableMetadata.appendQuestionsAllCols(sqlBuilder);
            
            sqlBuilder.append(")");
  
            sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.allColumnsData());
            
          }
        }
        
        for (MultiKey keyToDelete : keysToDelete) {
          
          StringBuilder sqlBuilder = new StringBuilder("delete from " + this.tableMetadata.getSchemaTo() + "." + this.tableMetadata.getTableNameTo() 
              + " where ");
          this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
          
          sqlBatchAdd(sqlBatch, sqlBuilder.toString(), GrouperClientUtils.toList(keyToDelete.getKeys()));
        }
      }
      
      debugMap.put("state", "inserts");

      //run the batches
      for (int localResult : this.sqlBatchExecute(sqlBatch, "insert", debugMap)) {
        if (localResult != 1) {
          rowsWithInsertErrors++;
          debugMap.put("rowsWithInsertErrors", rowsWithInsertErrors);
          LOG.error("Rows inserted is not 1, its " + localResult + "!!!!");
        }
        rowsNeedInsert += localResult;
        debugMap.put("rowsNeedInsert", rowsNeedInsert);
      }
      debugMap.put("state", "updates");
      for (int localResult : this.sqlBatchExecute(sqlBatch, "update", debugMap)) {
        if (localResult != 1) {
          rowsWithPrimaryKeyErrors++;
          debugMap.put("rowsWithPrimaryKeyErrors", rowsWithPrimaryKeyErrors);
          LOG.error("Rows updated is not 1, its " + localResult + "!!!!");
        }
        rowsNeedUpdate += localResult;
        debugMap.put("rowsNeedUpdate", rowsNeedUpdate);
      }
      debugMap.put("state", "deletes");
      for (int localResult : this.sqlBatchExecute(sqlBatch, "delete", debugMap)) {
        if (localResult != 1) {
          rowsWithDeleteErrors++;
          debugMap.put("rowsWithDeleteErrors", rowsWithDeleteErrors);
          LOG.error("Rows deleted is not 1, its " + localResult + "!!!!");
        }
        rowsNeedDelete += localResult;
        debugMap.put("rowsNeedDelete", rowsNeedDelete);
      }
      
      if (sqlBatch.size() > 0) {
        throw new RuntimeException("Why is SQL batch more than 1???");
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
    } finally {
      done[0]=true;
      debugMap.put("finalLog", true);

      debugMap.put("took", DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-now));
      
      String debugString = GrouperClientUtils.mapToString(debugMap);
      GcTableSyncLog.debugLog(debugString);
      
      gcTableSyncOutput.setDelete(rowsNeedDelete);
      gcTableSyncOutput.setUpdate(rowsNeedUpdate);
      gcTableSyncOutput.setInsert(rowsNeedInsert);
      // already set total
      //gcTableSyncOutput.setTotal();
      gcTableSyncOutput.setMessage(debugString);
      
    }
    return gcTableSyncOutput;
  }

  /**
   * log periodically
   * @param debugMap
   */
  public void logPeriodically(Map<String, Object> debugMap) {
    
    if (System.currentTimeMillis() - this.lastLog > (1000 * 60) - 10) {
    
      String debugString = GrouperClientUtils.mapToString(debugMap);
      GcTableSyncLog.debugLog(debugString);
      this.lastLog = System.currentTimeMillis();

    }
    
  }
  
  /**
   * log object
   */
  private static final Log LOG = GrouperClientUtils.retrieveLog(GcTableSync.class);

  /**
   * @param dbName 
   * @param bindVars
   * @param sql
   * @param dataReturnedHere 
   * @param gcTableSyncTableData 
   */
  public void retrieveDataBatchFromDb(String dbName, List<Object> bindVars, String sql, final List<GcTableSyncRowData> dataReturnedHere, final GcTableSyncTableData gcTableSyncTableData) {
    new GcDbAccess().connectionName(dbName).bindVars(bindVars.toArray())
      .sql(sql).callbackResultSet(new GcResultSetCallback() {

        @Override
        public Object callback(ResultSet resultSet) throws Exception {
          
          try {
            while (resultSet.next()) {
              Object[] row = new Object[GcTableSync.this.tableMetadata.getColumnMetadata().size()];
              int indexZeroIndexed = 0;
              for (GcTableSyncColumnMetadata columnMetadata : GcTableSync.this.tableMetadata.getColumnMetadata()) {
                Object value = columnMetadata.getColumnType().readDataFromResultSet(indexZeroIndexed+1, resultSet);
                row[indexZeroIndexed] = value;
                indexZeroIndexed++;
              }
              GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
              gcTableSyncRowData.setGcTableSyncTableData(gcTableSyncTableData);
              dataReturnedHere.add(gcTableSyncRowData);
              gcTableSyncRowData.setData(row);
            }
          } finally {
            GrouperClientUtils.closeQuietly(resultSet);
          }
          
          return null;
        }
      });
  }

  /**
   */
  public void configureTableSync() {
    /**
     * 
     */
    if (StringUtils.isBlank(this.key)) {
      throw new RuntimeException("Why is key blank?");
    }

    //  grouperClient.syncTable.personSource.databaseFrom = pcom
    this.databaseFrom = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.syncTable." + this.key + ".databaseFrom");

    //  grouperClient.syncTable.personSource.databaseTo = awsDev
    this.databaseTo = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.syncTable." + this.key + ".databaseTo");
    
    this.tableMetadata = new GcTableSyncTableMetadata();
    
    //  grouperClient.syncTable.personSource.tableFrom = PERSON_SOURCE_TEMP
    this.tableMetadata.setTableNameFrom(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.syncTable." + this.key + ".tableFrom"));

    //  grouperClient.syncTable.personSource.schemaFrom = 
    this.tableMetadata.setSchemaFrom(GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".schemaFrom"));
    if (StringUtils.isBlank(this.tableMetadata.getSchemaFrom())) {
      this.tableMetadata.setSchemaFrom(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + this.databaseFrom + ".user"));
    }
    
    //  grouperClient.syncTable.personSource.tableTo = PERSON_SOURCE_TEMP
    this.tableMetadata.setTableNameTo(GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".tableTo"));

    if (StringUtils.isBlank(this.tableMetadata.getTableNameTo())) {
      this.tableMetadata.setTableNameTo(this.tableMetadata.getTableNameFrom());
    }
    
    //  grouperClient.syncTable.personSource.schemaTo = 
    this.tableMetadata.setSchemaTo(GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".schemaTo"));

    if (StringUtils.isBlank(this.tableMetadata.getSchemaTo())) {
      this.tableMetadata.setSchemaTo(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + this.databaseTo + ".user"));
    }

    processDatabaseColumnMetadata();
    
    this.groupingSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.key + ".groupingSize", 10000);
    
    // grouperClient.syncTable.personSource.statusDatabase = awsDev
    this.statusDatabase = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".statusDatabase");
    
    // grouperClient.syncTable.personSource.statusSchema = 
    this.statusSchema = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".statusSchema");
    
    // default to the database user
    if (StringUtils.isBlank(this.getStatusSchema()) && !StringUtils.isBlank(this.statusDatabase)) {
      this.tableMetadata.setSchemaTo(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + this.statusDatabase + ".user"));
    }
    
    // grouperClient.syncTable.personSource.statusTable = grouper_chance_log_consumer
    this.statusTable = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".statusTable");

    // grouperClient.syncTable.personSource.fullSyncHourStart = 3
    this.fullSyncHourStart = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.key + ".fullSyncHourStart", 3);

    // grouperClient.syncTable.personSource.fullSyncHourEnd = 4
    this.fullSyncHourEnd = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.key + ".fullSyncHourEnd", 4);

    // grouperClient.syncTable.personSource.realTimeTable = grouper_chance_log_consumer
    this.realTimeTable = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".realTimeTable");

    // grouperClient.syncTable.personSource.realTimeSchema = 
    this.realTimeSchema = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + this.key + ".realTimeSchema");
    
    // default to the database user
    if (StringUtils.isBlank(this.getRealTimeSchema()) && !StringUtils.isBlank(this.realTimeTable)) {
      this.realTimeSchema = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.jdbc." + this.databaseFrom + ".user");
    }

  }

  /**
   * schema where real time table is.  blank for the FROM schema
   * grouperClient.syncTable.personSource.realTimeSchema = 
   */
  private String realTimeSchema;
  
  /**
   * schema where real time table is.  blank for the FROM schema
   * grouperClient.syncTable.personSource.realTimeSchema = 
   * @return the realTimeSchema
   */
  public String getRealTimeSchema() {
    return this.realTimeSchema;
  }
  
  /**
   * schema where real time table is.  blank for the FROM schema
   * grouperClient.syncTable.personSource.realTimeSchema = 
   * @param realTimeSchema1 the realTimeSchema to set
   */
  public void setRealTimeSchema(String realTimeSchema1) {
    this.realTimeSchema = realTimeSchema1;
  }

  /**
   * table where real time primary key and last_updated col is
   */
  private String realTimeTable;
  
  /**
   * table where real time primary key and last_updated col is
   * @return the realTimeTable
   */
  public String getRealTimeTable() {
    return this.realTimeTable;
  }
  
  /**
   * table where real time primary key and last_updated col is
   * @param realTimeTable1 the realTimeTable to set
   */
  public void setRealTimeTable(String realTimeTable1) {
    this.realTimeTable = realTimeTable1;
  }

  /**
   * grouperClient.syncTable.personSource.statusDatabase = awsDev
   */
  private String statusDatabase;
  
  /**
   * @return the statusDatabase
   */
  public String getStatusDatabase() {
    return this.statusDatabase;
  }
  
  /**
   * grouperClient.syncTable.personSource.statusDatabase = awsDev
   * @param statusDatabase1 the statusDatabase to set
   */
  public void setStatusDatabase(String statusDatabase1) {
    this.statusDatabase = statusDatabase1;
  }

  /**
   * grouperClient.syncTable.personSource.statusSchema = 
   */
  private String statusSchema;
  
  /**
   * grouperClient.syncTable.personSource.statusSchema = 
   * @return the statusSchema
   */
  public String getStatusSchema() {
    return this.statusSchema;
  }
  
  /**
   * grouperClient.syncTable.personSource.statusSchema = 
   * @param statusSchema1 the statusSchema to set
   */
  public void setStatusSchema(String statusSchema1) {
    this.statusSchema = statusSchema1;
  }

  /**
   * grouperClient.syncTable.personSource.statusTable = grouper_chance_log_consumer
   */
  private String statusTable;
  
  /**
   * grouperClient.syncTable.personSource.statusTable = grouper_chance_log_consumer
   * @return the statusTable
   */
  public String getStatusTable() {
    return this.statusTable;
  }
  
  /**
   * grouperClient.syncTable.personSource.statusTable = grouper_chance_log_consumer
   * @param statusTable1 the statusTable to set
   */
  public void setStatusTable(String statusTable1) {
    this.statusTable = statusTable1;
  }

  /**
   * grouperClient.syncTable.personSource.fullSyncHourStart = 3
   */
  private int fullSyncHourStart;
  
  /**
   * grouperClient.syncTable.personSource.fullSyncHourStart = 3
   * @return the fullSyncHourStart
   */
  public int getFullSyncHourStart() {
    return this.fullSyncHourStart;
  }
  
  /**
   * grouperClient.syncTable.personSource.fullSyncHourStart = 3
   * @param fullSyncHourStart1 the fullSyncHourStart to set
   */
  public void setFullSyncHourStart(int fullSyncHourStart1) {
    this.fullSyncHourStart = fullSyncHourStart1;
  }

  /**
   * grouperClient.syncTable.personSource.fullSyncHourEnd = 4
   */
  private int fullSyncHourEnd;
  
  /**
   * grouperClient.syncTable.personSource.fullSyncHourEnd = 4
   * @return the fullSyncHourEnd
   */
  public int getFullSyncHourEnd() {
    return this.fullSyncHourEnd;
  }
  
  /**
   * @param fullSyncHourEnd1 the fullSyncHourEnd to set
   */
  public void setFullSyncHourEnd(int fullSyncHourEnd1) {
    this.fullSyncHourEnd = fullSyncHourEnd1;
  }

  /**
   * grouping unique vals from source
   */
  private List<Object> fromGroupingUniqueValues;
  
  /**
   * grouping unique vals from source
   * @return the fromGroupingUniqueValues
   */
  public List<Object> getFromGroupingUniqueValues() {
    return this.fromGroupingUniqueValues;
  }

  /**
   * grouping unique vals from source
   * @param fromGroupingUniqueValues1 the fromGroupingUniqueValues to set
   */
  public void setFromGroupingUniqueValues(List<Object> fromGroupingUniqueValues1) {
    this.fromGroupingUniqueValues = fromGroupingUniqueValues1;
  }

  /**
   * grouping unique vals to source
   */
  private Set<Object> toGroupingUniqueValues;
  
  /**
   * grouping unique vals to source
   * @return the toGroupingUniqueValues
   */
  public Set<Object> getToGroupingUniqueValues() {
    return this.toGroupingUniqueValues;
  }

  /**
   * grouping unique vals to source
   * @param toGroupingUniqueValues1 the toGroupingUniqueValues to set
   */
  public void setToGroupingUniqueValues(Set<Object> toGroupingUniqueValues1) {
    this.toGroupingUniqueValues = toGroupingUniqueValues1;
  }

  /**
   * 
   */
  public void processDatabaseColumnMetadata() {
    
    this.tableMetadata.setColumnMetadata(new ArrayList<GcTableSyncColumnMetadata>());
    
    // go to database from and look up metadata
    new GcDbAccess().connectionName(this.databaseFrom).callbackConnection(new GcConnectionCallback() {

      @Override
      public Object callback(Connection connection) {
        
        //  grouperClient.syncTable.personSource.primaryKeyColumns = penn_id
        String primaryKeyColumnsString = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperClient.syncTable." + GcTableSync.this.key + ".primaryKeyColumns");

        Set<String> primaryKeyColumnsSet = new HashSet<String>();
        
        for (String primaryKeyColumn : GrouperClientUtils.splitTrim(primaryKeyColumnsString, ",")) {
          primaryKeyColumnsSet.add(primaryKeyColumn.toUpperCase());
        }
        
        //  grouperClient.syncTable.personSource.groupingColumn = penn_id
        String groupingColumn = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + GcTableSync.this.key + ".groupingColumn");

        if (groupingColumn != null) {
          groupingColumn = groupingColumn.toUpperCase();
        }
        
        if (StringUtils.isBlank(groupingColumn) && primaryKeyColumnsSet.size() == 1) {
          groupingColumn = primaryKeyColumnsSet.iterator().next();
        }
        
        if (StringUtils.isBlank(groupingColumn)) {
          throw new RuntimeException("You need to specify a grouping column if the primary key is more than one column! " + primaryKeyColumnsString);
        }
        
        //  grouperClient.syncTable.personSource.realTimeLastUpdatedCol = last_updated
        String realTimeLastUpdatedCol = StringUtils.defaultString(GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + GcTableSync.this.key + ".realTimeLastUpdatedCol")).toUpperCase();

        //  grouperClient.syncTable.personSource.columns = *
        ResultSet resultSet = null;
        
        try {
          
          // resultSet = connection.getMetaData().getTables(null, "AUTHZADM", GcTableSync.this.tableMetadata.getTableNameFrom().toUpperCase(), new String[]{"TABLE"});
          resultSet = connection.getMetaData().getColumns(null, GcTableSync.this.tableMetadata.getSchemaFrom().toUpperCase(), GcTableSync.this.tableMetadata.getTableNameFrom().toUpperCase(), "%");
          // COLUMN_NAME, DATA_TYPE, TYPE_NAME
          //ResultSetMetaData resultSetMetadata = resultSet.getMetaData();

          //  grouperClient.syncTable.personSource.columns = *
          String columnNames = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.syncTable." + GcTableSync.this.key + ".columns", "*");
          Set<String> columnNameSet = new HashSet<String>();
          
          for (String columnName : GrouperClientUtils.splitTrimToSet(columnNames, ",")) {
            columnNameSet.add(columnName.toUpperCase());
          }

          int indexZeroIndexed = 0;
          
          while (resultSet.next()) {
            
            String columnName = resultSet.getString("COLUMN_NAME").toUpperCase();
            int dataType = resultSet.getBigDecimal("DATA_TYPE").intValue();
            String typeName = resultSet.getString("TYPE_NAME");
            
            if (!columnNameSet.contains("*") && !columnNameSet.contains(columnName)) {
              continue;
            }

            GcTableSyncColumnMetadata gcTableSyncColumnMetadata = new GcTableSyncColumnMetadata();
            gcTableSyncColumnMetadata.setColumnIndexZeroIndexed(indexZeroIndexed++);
            GcTableSync.this.tableMetadata.getColumnMetadata().add(gcTableSyncColumnMetadata);
            
            gcTableSyncColumnMetadata.setColumnName(columnName);

            if (StringUtils.equals(columnName, groupingColumn)) {
              gcTableSyncColumnMetadata.setGroupingColumn(true);
            }
            
            if (StringUtils.equals(columnName, realTimeLastUpdatedCol)) {
              gcTableSyncColumnMetadata.setRealTimeLastUpdatedColumn(true);
            }
            
            if (primaryKeyColumnsSet.contains("*") || primaryKeyColumnsSet.contains(columnName)) {
              gcTableSyncColumnMetadata.setPrimaryKey(true);
            }
            
            switch (dataType) {
              case Types.BIGINT: 
              case Types.DECIMAL:
              case Types.DOUBLE:
              case Types.FLOAT:
              case Types.INTEGER:
              case Types.NUMERIC:
              case Types.REAL:
              case Types.SMALLINT:
              case Types.TINYINT:
                
                gcTableSyncColumnMetadata.setColumnType(ColumnType.NUMERIC);
                break;
                
              case Types.CHAR:
              case Types.VARCHAR:
              case Types.LONGVARCHAR:

                gcTableSyncColumnMetadata.setColumnType(ColumnType.STRING);
                break;

              case Types.DATE:
              case Types.TIMESTAMP:
                
                gcTableSyncColumnMetadata.setColumnType(ColumnType.TIMESTAMP);
                break; 
                
              default:
                throw new RuntimeException("Type not supported: " + dataType + ", " + typeName);
                
            }
            
//            for (int i=0;i<resultSetMetadata.getColumnCount();i++) {
//              String columnName = resultSetMetadata.getColumnName(i+1);
//              int columnType = resultSetMetadata.getColumnType(i+1);
//              Object value = null;
//              switch (columnType) {
//                case Types.BIGINT: 
//                case Types.DECIMAL:
//                case Types.DOUBLE:
//                case Types.FLOAT:
//                case Types.INTEGER:
//                case Types.NUMERIC:
//                case Types.REAL:
//                case Types.SMALLINT:
//                case Types.TINYINT:
//                  
//                  value = resultSet.getBigDecimal(i+1);
//                  break;
//                  
//                case Types.CHAR:
//                case Types.VARCHAR:
//                case Types.LONGVARCHAR:
//
//                  value = resultSet.getString(i+1);
//                  break;
//
//                case Types.DATE:
//                case Types.TIMESTAMP:
//                  
//                  value = resultSet.getTimestamp(i+1);
//
//                default:
//                  throw new RuntimeException("Type not supported: " + columnType);
//                  
//              }
//              
//              System.out.println("Column: " + columnName + ": " + value);
//            }
          }
        } catch (Exception e) {
          throw new RuntimeException("error", e);
        } finally {
          GrouperClientUtils.closeQuietly(resultSet);
        }
        
        return null;
      }
    });
  }
  
  /**
   * sql batch add
   * @param sqlBatch
   * @param sql
   * @param args
   */
  public void sqlBatchAdd(Map<String, List<List<Object>>> sqlBatch, String sql, List<Object> args) {
    List<List<Object>> argLists = sqlBatch.get(sql);
    if (argLists == null) {
      argLists = new ArrayList<List<Object>>();
      sqlBatch.put(sql, argLists);
    }
    argLists.add(args);
  }

  /**
   * execute a batch, return the count
   * @param sqlBatch
   * @param sqlPrefix if we only want to run a certain prefix
   * @param debugMap 
   * @return the count
   */
  public List<Integer> sqlBatchExecute(Map<String, List<List<Object>>> sqlBatch, String sqlPrefix, Map<String, Object> debugMap) {

    try {
      List<Integer> result = new ArrayList<Integer>();
      
      int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTable." + this.key + ".batchSize", 1000);
      
      // sql batch
      for (String sql : new HashSet<String>(sqlBatch.keySet())) {
  
        // see if we are doing the right one
        if (sqlPrefix == null || sql.startsWith(sqlPrefix)) {
          
          List<List<Object>> argLists = sqlBatch.get(sql);
          
          if (GrouperClientUtils.length(argLists) > 0) {
            int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(argLists, batchSize);
            
            for (int i=0;i<numberOfBatches;i++) {
              debugMap.put("sqlBatchExecute", i + " of " + numberOfBatches);
              List<List<Object>> batchListOfArgs = GrouperClientUtils.batchList(argLists, batchSize, i);
              
              int[] localResults = new GcDbAccess().connectionName(this.databaseTo).batchBindVars(batchListOfArgs).sql(sql).executeBatchSql();
              if (localResults != null) {
                for (int localResult : localResults) {
                  result.add(localResult);
                }
              }
  
            }
          }        
          sqlBatch.remove(sql);
        }
      }
      return result;
    } finally {
      
    }
  }
  
  /**
   * database to key
   */
  private String databaseTo;
  
  /**
   * database to key
   * @return the databaseTo
   */
  public String getDatabaseTo() {
    return this.databaseTo;
  }
  
  /**
   * database to key
   * @param databaseTo1 the databaseTo to set
   */
  public void setDatabaseTo(String databaseTo1) {
    this.databaseTo = databaseTo1;
  }

  /**
   * database from key
   */
  private String databaseFrom;
  
  /**
   * database from key
   * @return the databaseFrom
   */
  public String getDatabaseFrom() {
    return this.databaseFrom;
  }
  
  /**
   * database from key
   * @param databaseFrom1 the databaseFrom to set
   */
  public void setDatabaseFrom(String databaseFrom1) {
    this.databaseFrom = databaseFrom1;
  }

  /**
   * how many to group by
   */
  private int groupingSize;
  
  /**
   * @return the groupingSize
   */
  public int getGroupingSize() {
    return this.groupingSize;
  }
  
  /**
   * @param groupingSize1 the groupingSize to set
   */
  public void setGroupingSize(int groupingSize1) {
    this.groupingSize = groupingSize1;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    GcTableSync gcTableSync = new GcTableSync();
    gcTableSync.setKey("personSource");
    gcTableSync.fullSync();

    
//    GcTableSync gcTableSync = new GcTableSync();
//    gcTableSync.setKey("personSource");
//    gcTableSync.selectStuffTest();
  }

  /**
   * 
   */
  public void selectStuffTest() {
    
    this.configureTableSync();
    
    this.fromData = new GcTableSyncTableData();
    this.fromData.setGcTableSync(this);
    
    this.fromData.setRows(new ArrayList<GcTableSyncRowData>());

    new GcDbAccess().connectionName("pcom").bindVars(new Object[]{"10015257"})
      .sql("select PENN_ID, PENNNAME, NAME, DESCRIPTION, DESCRIPTION_LOWER, FIRST_NAME, LAST_NAME, AFFILIATION_ID, PERSON_ACTIVE, EMAIL, EMAIL_PUBLIC, NAME_FIRST_PUBLIC, NAME_LAST_PUBLIC, NAME_PUBLIC, EPPN, PREFERRED_FIRST_NAME, INT_PENN_ID from authzadm.PERSON_SOURCE_TEMP where PENN_ID in (?)")
      .callbackResultSet(new GcResultSetCallback() {

      @Override
      public Object callback(ResultSet resultSet) throws Exception {
    
        try {
          while (resultSet.next()) {
            Object[] row = new Object[17];
            int indexZeroIndexed = 0;
            for (GcTableSyncColumnMetadata columnMetadata : GcTableSync.this.tableMetadata.getColumnMetadata()) {
              Object value = columnMetadata.getColumnType().readDataFromResultSet(indexZeroIndexed+1, resultSet);
              row[indexZeroIndexed] = value;
              indexZeroIndexed++;
            }
            GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
            GcTableSync.this.fromData.getRows().add(gcTableSyncRowData);
            gcTableSyncRowData.setData(row);
          }
        } finally {
          GrouperClientUtils.closeQuietly(resultSet);
        }
        
        return null;
      }
    });

  }
  
}
