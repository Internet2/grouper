/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
   * data bean in the from table
   */
  private GcTableSyncTableBean dataBeanFrom;
  
  /**
   * data bean in the from table
   * @return the fromData
   */
  public GcTableSyncTableBean getDataBeanFrom() {
    return this.dataBeanFrom;
  }
  
  /**
   * data in the from table
   * @param fromData1 the fromData to set
   */
  public void setDataBeanFrom(GcTableSyncTableBean fromData1) {
    this.dataBeanFrom = fromData1;
  }

  /**
   * data in the to table
   */
  private GcTableSyncTableBean dataBeanTo;
  
  /**
   * data in the to table
   * @return the toData
   */
  public GcTableSyncTableBean getDataBeanTo() {
    return this.dataBeanTo;
  }
  
  /**
   * data in the to table
   * @param toData1 the toData to set
   */
  public void setDataBeanTo(GcTableSyncTableBean toData1) {
    this.dataBeanTo = toData1;
  }

  /**
   * data in the status table
   */
  private GcTableSyncTableBean dataBeanStatus;
  
  /**
   * data in the status table
   * @return the dataBeanStatus
   */
  public GcTableSyncTableBean getDataBeanStatus() {
    return this.dataBeanStatus;
  }
  
  /**
   * data in the status table
   * @param dataBeanStatus the dataBeanStatus to set
   */
  public void setDataBeanStatus(GcTableSyncTableBean dataBeanStatus) {
    this.dataBeanStatus = dataBeanStatus;
  }

  /**
   * data in the realtime table
   */
  private GcTableSyncTableBean dataBeanRealTime;
  
  /**
   * data in the realtime table
   * @return the dataBeanRealTime
   */
  public GcTableSyncTableBean getDataBeanRealTime() {
    return this.dataBeanRealTime;
  }

  
  /**
   * data in the realtime table
   * @param dataBeanRealTime1 the dataBeanRealTime to set
   */
  public void setDataBeanRealTime(GcTableSyncTableBean dataBeanRealTime1) {
    this.dataBeanRealTime = dataBeanRealTime1;
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
   
    final GcTableSyncColumnMetadata realTimeLastUpdatedColumnMetadata = this.getRealTimeLastUpdatedColumnMetadata();

    // where we at with full?
    String sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusTable()
        + " where name = ?"; 

    Timestamp fullLastUpdated = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar("tableSync_full_" + this.key).select(Timestamp.class);
    
    if (fullLastUpdated != null && (System.currentTimeMillis() - fullLastUpdated.getTime() ) < (1000 * 60 * 5 )  ) {
      
      debugMap.put("fullSyncIsRunning", true);
      return true;
    }
    
    return false;
  }
  
  /**
   * @param gcTableSyncOutputArray
   * TODO merge with sync()
   */
  public void incrementalSync(GcTableSyncOutput[] gcTableSyncOutputArray) {
    
    if (gcTableSyncOutputArray == null || gcTableSyncOutputArray.length != 1 || gcTableSyncOutputArray[0] == null) {
      throw new RuntimeException("Pass in a gcTableSyncOutput array of size 1 which is not null");
    }
    
    final GcTableSyncOutput gcTableSyncOutput = gcTableSyncOutputArray[0];
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
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
      debugMap.put("tableFrom", this.getTableNameFrom());
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
              logPeriodically(debugMap, gcTableSyncOutput);
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
        
        String sql = "select " + this.tableMetadata.getRealTimeLastUpdatedColumnMetadata().getColumnName() + " from " + this.getRealTimeTable() + " where " 
            + realTimeLastUpdatedColumnMetadata.getColumnName() + " > ?";
  
        int total = (Integer)new GcDbAccess().connectionName(this.databaseFrom).sql(sql).callbackResultSet(new GcResultSetCallback() {
  
          @Override
          public Object callback(ResultSet resultSet) throws Exception {
            try {
              
              while (resultSet.next()) {
                Object value = realTimeLastUpdatedColumnMetadata.getColumnType().readDataFromResultSet(1, resultSet);
                GcTableSync.this.fromGroupingUniqueValues.add(value);
                debugMap.put("fromGroupingUniqueValues", GcTableSync.this.fromGroupingUniqueValues.size());
                logPeriodically(debugMap, gcTableSyncOutput);
              }
              
            } finally {
              GrouperClientUtils.closeQuietly(resultSet);
            }
  
            return null;
          }
        });
        debugMap.put("totalCountFrom", total);
        gcTableSyncOutput.setRowsSelectedFrom(total);
  
        if (total > 0) {
          // sorted unique values
          Collections.sort((List)this.fromGroupingUniqueValues);
          logPeriodically(debugMap, gcTableSyncOutput);
    
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
              sqlBuilder.append(" from " + this.tableMetadata.getTableNameFrom() 
                  + " where " + realTimeLastUpdatedColumnMetadata.getColumnName() + " >= ? and " + realTimeLastUpdatedColumnMetadata.getColumnName() + " <= ?");
              // add first
              bindVars.add(groupingsFromBatch.get(0));
              // add last.  note, you might get more than you bargained for... maybe check for that?
              bindVars.add(groupingsFromBatch.get(groupingsFromBatch.size()-1));
              
              this.fromData = new GcTableSyncTableData();
              this.fromData.setGcTableSync(this);
              
              this.fromData.setRows(new ArrayList<GcTableSyncRowData>());
    
              retrieveDataBatchFromDb(this.databaseFrom, bindVars, sqlBuilder.toString(), GcTableSync.this.fromData.getRows(), GcTableSync.this.fromData);
              gcTableSyncOutput.addRowsSelectedFrom(this.fromData.getRows().size());
      
              debugMap.put("rowsSelectedFrom", gcTableSyncOutput.getRowsSelectedFrom());
              // logPeriodically(debugMap);  wait until we get the "to" groupings
            }
            
            // get the to batch
            debugMap.put("state", "retrieveToRealTimeBatch");
            {
              StringBuilder sqlBuilder = new StringBuilder("select ");
        
              this.tableMetadata.appendColumns(sqlBuilder);
              
              List<Object> bindVars = new ArrayList<Object>();
              
              sqlBuilder.append(" from " + this.tableMetadata.getTableNameTo() 
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
              gcTableSyncOutput.addRowsSelectedTo(this.toData.getRows().size());
      
              debugMap.put("rowsSelectedTo", gcTableSyncOutput.getRowsSelectedTo());
              logPeriodically(debugMap, gcTableSyncOutput);
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
                  gcTableSyncOutput.addRowsWithEqualData(1);
                  debugMap.put("rowsWithEqualData", gcTableSyncOutput.getRowsWithEqualData());
                  continue;
                }
    
                //if it matches and isnt equal, thats an update
                StringBuilder sqlBuilder = new StringBuilder("update " + this.tableMetadata.getTableNameTo() 
                    + " set " );
    
                this.tableMetadata.appendNonPrimaryKeyUpdateColumnNames(sqlBuilder);
                
                sqlBuilder.append(" where ");
    
                this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
                
                sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.nonPrimaryAndThenPrimaryKeyData());
                
              } else {
                //if not in destination and is in source, then insert
    
                StringBuilder sqlBuilder = new StringBuilder("insert into " + this.tableMetadata.getTableNameTo() 
                    + " (" );
                
                this.tableMetadata.appendColumns(sqlBuilder);
                
                sqlBuilder.append(" ) values ( ");
    
                this.tableMetadata.appendQuestionsAllCols(sqlBuilder);
                
                sqlBuilder.append(")");
      
                sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.allColumnsData());
                
              }
            }
            
            logPeriodically(debugMap, gcTableSyncOutput);
          }
          
          debugMap.put("state", "inserts");
    
          //run the batches
          for (int localResult : this.sqlBatchExecute(sqlBatch, "insert", debugMap)) {
            if (localResult != 1) {
              gcTableSyncOutput.addRowsWithInsertErrors(1);
              debugMap.put("rowsWithInsertErrors", gcTableSyncOutput.getRowsWithInsertErrors());
              LOG.error("Rows inserted is not 1, its " + localResult + "!!!!");
            }
            gcTableSyncOutput.addInsert(localResult);
            debugMap.put("rowsNeedInsert", gcTableSyncOutput.getInsert());
          }
          debugMap.put("state", "updates");
          for (int localResult : this.sqlBatchExecute(sqlBatch, "update", debugMap)) {
            if (localResult != 1) {
              gcTableSyncOutput.addRowsWithUpdateErrors(1);
              debugMap.put("rowsWithUpdateErrors", gcTableSyncOutput.getRowsWithUpdateErrors());
              LOG.error("Rows updated is not 1, its " + localResult + "!!!!");
            }
            gcTableSyncOutput.addUpdate(1);
            debugMap.put("rowsNeedUpdate", gcTableSyncOutput.getUpdate());
          }
          debugMap.put("state", "deletes");
          for (int localResult : this.sqlBatchExecute(sqlBatch, "delete", debugMap)) {
            if (localResult != 1) {
              gcTableSyncOutput.addRowsWithDeleteErrors(1);
              debugMap.put("rowsWithDeleteErrors", gcTableSyncOutput.getRowsWithDeleteErrors());
              LOG.error("Rows deleted is not 1, its " + localResult + "!!!!");
            }
            gcTableSyncOutput.addDelete(localResult);
            debugMap.put("rowsNeedDelete", gcTableSyncOutput.getDelete());
          }
          
          if (sqlBatch.size() > 0) {
            throw new RuntimeException("Why is SQL batch more than 1???");
          }
        }
        
        // where we at with incremental, update the status?
        sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusTable()
            + " where name = ?"; 
  
        final String statusNameColumnName = "tableSync_incremental_" + this.key;
        Timestamp lastRun = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar(statusNameColumnName).select(Timestamp.class);
  
        if (lastRun == null) {
  
          sql = "insert into " + this.getStatusTable()
              + " ( name, last_sequence_processed, last_updated, created_on, id, hibernate_version_number ) values ( ?, ?, ?, ?, ?, ? )";
  
          new GcDbAccess().connectionName(this.statusDatabase).sql(sql).bindVars(new Object[] { statusNameColumnName, millisOfLastRecordUpdated, millisOfLastRecordUpdated, millisOfLastRecordUpdated, GrouperClientUtils.uuid(), 0}).select(Timestamp.class);
  
        } else {
          
          sql = "update " + this.getStatusTable()
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
      
      // already set total
      //gcTableSyncOutput.setTotal();
      gcTableSyncOutput.setMessage(debugString);
      
    }
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
    String sql = "select " + realTimeLastUpdatedColumnMetadata.getColumnName() + " from " + this.getStatusTable()
        + " where name = ?"; 

    final String statusNameColumnName = "tableSync_" + type +  "_" + this.key;
    Long lastRun = new GcDbAccess().connectionName(this.statusDatabase).sql(sql).addBindVar(statusNameColumnName).select(Long.class);
    
    return lastRun == null ? null : lastRun;

  }
  
  /**
   * configuration for this table sync
   */
  private GcTableSyncConfiguration gcTableSyncConfiguration = null;
  
  /**
   * configuration for this table sync
   * @return the gcTableSyncConfiguration
   */
  public GcTableSyncConfiguration getGcTableSyncConfiguration() {
    return this.gcTableSyncConfiguration;
  }

  /**
   * @param gcTableSyncConfiguration1 the gcTableSyncConfiguration to set
   */
  public void setGcTableSyncConfiguration(GcTableSyncConfiguration gcTableSyncConfiguration1) {
    this.gcTableSyncConfiguration = gcTableSyncConfiguration1;
  }

  /**
   * pass in the output which will update as it runs
   * @param gcTableSyncOutputArray 
   * 
   */
  public void sync(GcTableSyncOutput[] gcTableSyncOutputArray) {
    
    if (gcTableSyncOutputArray == null || gcTableSyncOutputArray.length != 1 || gcTableSyncOutputArray[0] == null) {
      throw new RuntimeException("Pass in a gcTableSyncOutput array of size 1 which is not null");
    }
    
    final GcTableSyncOutput gcTableSyncOutput = gcTableSyncOutputArray[0];
    
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    long now = System.currentTimeMillis();
    final boolean[] done = new boolean[]{false};

    Map<String, List<List<Object>>> sqlBatch = new HashMap<String, List<List<Object>>>();
    
    try {

      debugMap.put("finalLog", false);
      debugMap.put("state", "retrieveCount");

      this.gcTableSyncConfiguration = new GcTableSyncConfiguration();
      this.gcTableSyncConfiguration.setConfigKey(this.configKey);
      this.gcTableSyncConfiguration.configureTableSync(debugMap);
      
      this.tableMetadata = GcTableSyncTableMetadata.retrieveTableMetadataFromCacheOrDatabase(
          this.gcTableSyncConfiguration.getDatabaseFrom(), this.gcTableSyncConfiguration.getTableFrom());

      for (String primaryKeyColumn : GrouperClientUtils.splitTrim(primaryKeyColumnsString, ",")) {
        primaryKeyColumnsSet.add(primaryKeyColumn);
      }

  
      if (StringUtils.isBlank(groupingColumn) && primaryKeyColumnsSet.size() == 1) {
        groupingColumn = primaryKeyColumnsSet.iterator().next();
      }
      if (StringUtils.isBlank(groupingColumn)) {
        throw new RuntimeException("You need to specify a grouping column if the primary key is more than one column! " + primaryKeyColumnsString);
      }
      
   // TODO
//    this.tableMetadata.setColumnMetadata(processDatabaseColumnMetadata(this.databaseFrom));
//    
//    if (!columnNameSet.contains("*") && !columnNameSet.contains(columnName)) {
//      continue;
//    }
//    GcTableSync.this.tableMetadata.getColumnMetadata().add(gcTableSyncColumnMetadata);

//    if (StringUtils.equals(columnName, groupingColumn)) {
//      gcTableSyncColumnMetadata.setGroupingColumn(true);
//    }
//    
//    if (StringUtils.equals(columnName, realTimeLastUpdatedCol)) {
//      gcTableSyncColumnMetadata.setRealTimeLastUpdatedColumn(true);
//    }
//    
//    if (primaryKeyColumnsSet.contains("*") || primaryKeyColumnsSet.contains(columnName)) {
//      gcTableSyncColumnMetadata.setPrimaryKey(true);
//    }



    }

      debugMap.put("databaseFrom", this.databaseFrom);
      debugMap.put("tableFrom", this.tableMetadata.getTableNameFrom());
      debugMap.put("databaseTo", this.databaseTo);
      debugMap.put("tableTo", this.tableMetadata.getTableNameTo());

      Thread thread = new Thread(new Runnable() {

        public void run() {
          
          try {
            while(true) {
              //if process is done then done TODO not full anymore... maybe add subtype?  or name?
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
              logPeriodically(debugMap, gcTableSyncOutput);
            }
          } catch (InterruptedException ie) {
            
          } catch (Exception e) {
            LOG.error("Error assigning status and logging", e);
          }
          
        }
        
      });
      
      thread.start();
      
      final GcTableSyncColumnMetadata groupingColumnMetadata = this.tableMetadata.getGroupingColumnMetadata();

      String sql = "select count(*) from " + this.tableMetadata.getTableNameFrom();

      int total = new GcDbAccess().connectionName(this.databaseFrom).sql(sql).select(int.class);
      debugMap.put("totalCountFrom", total);
      gcTableSyncOutput.addRowsSelectedFrom(total);
      
      debugMap.put("state", "retrieveAllFromGroupings");

      sql = "select distinct " + groupingColumnMetadata.getColumnName() + " from " + this.tableMetadata.getTableNameFrom();
      
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
  
      sql = "select distinct " + groupingColumnMetadata.getColumnName() + " from " + this.tableMetadata.getTableNameTo();
      
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
          StringBuilder sqlBuilder = new StringBuilder("delete from " + this.tableMetadata.getTableNameTo() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
          GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsToDeleteBatch));
          sqlBuilder.append(")");
          sqlBatchAdd(sqlBatch, sqlBuilder.toString(), groupingsToDeleteBatch);
        }      

      }
      
      for (int localResult : this.sqlBatchExecute(sqlBatch, null, debugMap)) {
        gcTableSyncOutput.addDelete(localResult);
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
            sqlBuilder.append(" from " + this.tableMetadata.getTableNameFrom() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
              GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsFromBatch));
            sqlBuilder.append(")");
            bindVars = groupingsFromBatch;
          } else {
            sqlBuilder.append(" from " + this.tableMetadata.getTableNameFrom() 
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
          gcTableSyncOutput.addRowsSelectedFrom(this.fromData.getRows().size());
  
          debugMap.put("rowsSelectedFrom", gcTableSyncOutput.getRowsSelectedFrom());
        }
        
        
        // get the to batch
        debugMap.put("state", "retrieveToGroupingsOfBatch");
        {
          StringBuilder sqlBuilder = new StringBuilder("select ");
    
          this.tableMetadata.appendColumns(sqlBuilder);
          
          
          List<Object> bindVars = new ArrayList<Object>();
          if (selectIndividual) {
            sqlBuilder.append(" from " + this.tableMetadata.getTableNameTo() 
              + " where " + groupingColumnMetadata.getColumnName() + " in (");
              GrouperClientUtils.appendQuestions(sqlBuilder, GrouperClientUtils.length(groupingsFromBatch));
            sqlBuilder.append(")");
            bindVars = groupingsFromBatch;
          } else {
            sqlBuilder.append(" from " + this.tableMetadata.getTableNameTo() 
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
          gcTableSyncOutput.addRowsSelectedTo(this.toData.getRows().size());
  
          debugMap.put("rowsSelectedTo", gcTableSyncOutput.getRowsSelectedTo());
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
              gcTableSyncOutput.addRowsWithEqualData(1);
              debugMap.put("rowsWithEqualData", gcTableSyncOutput.getRowsWithEqualData());
              continue;
            }

            //if it matches and isnt equal, thats an update
            StringBuilder sqlBuilder = new StringBuilder("update " + this.tableMetadata.getTableNameTo() 
                + " set " );

            this.tableMetadata.appendNonPrimaryKeyUpdateColumnNames(sqlBuilder);
            
            sqlBuilder.append(" where ");

            this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
            
            sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.nonPrimaryAndThenPrimaryKeyData());
            
          } else {
            //if not in destination and is in source, then insert

            StringBuilder sqlBuilder = new StringBuilder("insert into " + this.tableMetadata.getTableNameTo() 
                + " (" );
            
            this.tableMetadata.appendColumns(sqlBuilder);
            
            sqlBuilder.append(" ) values ( ");

            this.tableMetadata.appendQuestionsAllCols(sqlBuilder);
            
            sqlBuilder.append(")");
  
            sqlBatchAdd(sqlBatch, sqlBuilder.toString(), sourceRow.allColumnsData());
            
          }
        }
        
        for (MultiKey keyToDelete : keysToDelete) {
          
          StringBuilder sqlBuilder = new StringBuilder("delete from " + this.tableMetadata.getTableNameTo() 
              + " where ");
          this.tableMetadata.appendPrimaryKeyColumnNames(sqlBuilder);
          
          sqlBatchAdd(sqlBatch, sqlBuilder.toString(), GrouperClientUtils.toList(keyToDelete.getKeys()));
        }
      }
      
      debugMap.put("state", "inserts");

      //run the batches
      for (int localResult : this.sqlBatchExecute(sqlBatch, "insert", debugMap)) {
        if (localResult != 1) {
          gcTableSyncOutput.addRowsWithInsertErrors(1);
          debugMap.put("rowsWithInsertErrors", gcTableSyncOutput.getRowsWithInsertErrors());
          LOG.error("Rows inserted is not 1, its " + localResult + "!!!!");
        }
        gcTableSyncOutput.addInsert(localResult);
      }
      debugMap.put("rowsNeedInsert", gcTableSyncOutput.getInsert());
      debugMap.put("state", "updates");
      for (int localResult : this.sqlBatchExecute(sqlBatch, "update", debugMap)) {
        if (localResult != 1) {
          gcTableSyncOutput.addRowsWithUpdateErrors(1);
          debugMap.put("rowsWithUpdateErrors", gcTableSyncOutput.getRowsWithUpdateErrors());
          LOG.error("Rows updated is not 1, its " + localResult + "!!!!");
        }
        gcTableSyncOutput.addUpdate(localResult);
        debugMap.put("rowsNeedUpdate", gcTableSyncOutput.getUpdate());
      }
      debugMap.put("state", "deletes");
      for (int localResult : this.sqlBatchExecute(sqlBatch, "delete", debugMap)) {
        if (localResult != 1) {
          gcTableSyncOutput.addRowsWithDeleteErrors(1);
          debugMap.put("rowsWithDeleteErrors", gcTableSyncOutput.getRowsWithDeleteErrors());
          LOG.error("Rows deleted is not 1, its " + localResult + "!!!!");
        }
        gcTableSyncOutput.addDelete(localResult);
        debugMap.put("rowsNeedDelete", gcTableSyncOutput.getDelete());
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
      
      // already set total
      //gcTableSyncOutput.setTotal();
      gcTableSyncOutput.setMessage(debugString);
      
    }
  }

  /**
   * log periodically
   * @param debugMap
   * @param gcTableSyncOutput 
   */
  public void logPeriodically(Map<String, Object> debugMap, GcTableSyncOutput gcTableSyncOutput) {
    
    if (System.currentTimeMillis() - this.lastLog > (1000 * 60) - 10) {
    
      String debugString = GrouperClientUtils.mapToString(debugMap);
      gcTableSyncOutput.setMessage(debugString);
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

              // TODO if there is an error, run each individually
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
   * key in config that points to this instance of table sync
   */
  private String configKey;
  
  /**
   * @param args
   */
  public static void main(String[] args) {

    
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


  /**
   * @param sqlBuilder
   */
  public void appendNonPrimaryKeyUpdateColumnNames(StringBuilder sqlBuilder) {
    List<GcTableSyncColumnMetadata> nonPrimaryKeyMetadata = this.getNonPrimaryKey();
    if (nonPrimaryKeyMetadata.size() == 0) {
      throw new RuntimeException("Why is non primary key size 0 with an update????");
    }
    
    int colNum = 0;
    for (GcTableSyncColumnMetadata nonPrimaryKeyMetadatum : nonPrimaryKeyMetadata) {
      if (colNum != 0) {
        sqlBuilder.append(", ");
      }
      sqlBuilder.append(nonPrimaryKeyMetadatum.getColumnName() + " = ?");
      colNum++;
    }
  
  }


  /**
   * @param sqlBuilder
   */
  public void appendPrimaryKeyColumnNames(StringBuilder sqlBuilder) {
    List<GcTableSyncColumnMetadata> primaryKeyMetadata = this.getPrimaryKey();
    if (primaryKeyMetadata.size() == 0) {
      throw new RuntimeException("Why is primary key size 0????");
    }
    int colNum = 0;
    for (GcTableSyncColumnMetadata primaryKeyMetadatum : primaryKeyMetadata) {
      if (colNum != 0) {
        sqlBuilder.append(" and ");
      }
      sqlBuilder.append(primaryKeyMetadatum.getColumnName() + " = ?");
      colNum++;
    }
  
  }


  /**
   * get realTimeLastUpdatedColumn metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getRealTimeLastUpdatedColumnMetadata() {
    for (GcTableSyncColumnMetadata theColumnMetadata : GrouperClientUtils.nonNull(this.columnMetadata)) {
      if (theColumnMetadata.isRealTimeLastUpdatedColumn()) {
        return theColumnMetadata;
      }
    }
    throw new RuntimeException("Cant find realTimeLastUpdatedColumn! " + this.tableNameFrom);
  }



  /**
   * key in config that points to this instance of table sync
   * @return the key
   */
  public String getConfigKey() {
    return this.configKey;
  }



  /**
   * key in config that points to this instance of table sync
   * @param key1 the key to set
   */
  public void setConfigKey(String key1) {
    this.configKey = key1;
  }
  
}
