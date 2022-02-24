package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * <p>Sync to a table from a dataset, columns, etc</p>
 * <p>Sample call
 * 
 * <blockquote>
 * <pre>
 * import edu.internet2.middleware.grouperClient.jdbc.tableSync.*;
 * 
 * new GcTableSyncFromData().assignDebugMap(debugMap).assignConnectionName(connectionName).assignTableName(tableName)
 *       .assignColumnNames(columnNames).assignColumnNamesPrimaryKey(columnNamesPrimaryKey).assignData(wsRows).sync();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * @author mchyzer
 *
 */
public class GcTableSyncFromData {

  /**
   * optional debug map
   */
  private Map<String, Object> debugMap;

  /**
   * optional debug map
   * @param theDebugMap1
   * @return this for chaining
   */
  public GcTableSyncFromData assignDebugMap(Map<String, Object> theDebugMap1) {
    this.debugMap = theDebugMap1;
    return this;
  }
  
  /**
   * prefix for main entries.  note nested entries will be additive
   */
  private String debugMapPrefix;
  
  /**
   * prefix for main entries.  note nested entries will be additive
   * @param theDebugMapPrefix
   * @return this for chaining
   */
  public GcTableSyncFromData assignDebugMapPrefix(String theDebugMapPrefix) {
    this.debugMapPrefix = theDebugMapPrefix;
    return this;
  }
  
  /**
   * external system connection name (default is grouper)
   */
  private String connectionName;

  /**
   * external system connection name (default is grouper)
   * @param theConnectionName
   * @return this for chaining
   */
  public GcTableSyncFromData assignConnectionName(String theConnectionName) {
    this.connectionName = theConnectionName;
    return this;
  }

  /**
   * table name (could be qualified by schema)
   */
  private String tableName;

  /**
   * table name (could be qualified by schema)
   * @param theTableName
   * @return this for chaining
   */
  public GcTableSyncFromData assignTableName(String theTableName) {
    this.tableName = theTableName;
    return this;
  }

  /**
   * columns of table
   */
  private List<String> columnNames;

  /**
   * columns of table
   * @param theColumnNames
   * @return this for chaining
   */
  public GcTableSyncFromData assignColumnNames(List<String> theColumnNames) {
    this.columnNames = theColumnNames;
    return this;
  }

  /**
   * primary key column names
   */
  private List<String> columnNamesPrimaryKey;

  /**
   * primary key column names
   * @param theColumnNamesPrimaryKey
   * @return this for chaining
   */
  public GcTableSyncFromData assignColumnNamesPrimaryKey(List<String> theColumnNamesPrimaryKey) {
    this.columnNamesPrimaryKey = theColumnNamesPrimaryKey;
    return this;
  }

  /**
   * list of object arrays must be in same order as columnNames
   */
  private List<Object[]> data;

  /**
   * list of object arrays must be in same order as columnNames
   * @param theData
   * @return this for chaining
   */
  public GcTableSyncFromData assignData(List<Object[]> theData) {
    this.data = theData;
    return this;
  }
  
  /**
   * sync data from a list of object arrays to a SQL table
   */
  public void sync() { 

    this.connectionName = GrouperClientUtils.defaultString(this.connectionName, "grouper");
    
    GrouperClientUtils.assertion(GrouperClientUtils.length(this.columnNames) > 0, "Must pass in column names");
    GrouperClientUtils.assertion(GrouperClientUtils.length(this.columnNamesPrimaryKey) > 0, "Must pass in primary key column names");
    
    this.data = GrouperClientUtils.nonNull(this.data);

    GrouperClientUtils.assertion(!GrouperClientUtils.isBlank(this.tableName), "Must pass in table name");

    if (this.debugMap == null) {
      this.debugMap = new LinkedHashMap<String, Object>();
    }
    this.debugMapPrefix = GrouperClientUtils.defaultString(this.debugMapPrefix);
    String columnsCommaSeparated = GrouperClientUtils.join(this.columnNames.iterator(), ",");
    // setup the table sync
    GcTableSync gcTableSync = new GcTableSync();
    gcTableSync.setGcTableSyncConfiguration(new GcTableSyncConfiguration());
    gcTableSync.setGcTableSyncOutput(new GcTableSyncOutput());

    // setup the data from the database
    GcTableSyncTableBean gcTableSyncTableBeanSql = new GcTableSyncTableBean(gcTableSync);
    gcTableSyncTableBeanSql.configureMetadata(this.connectionName, this.tableName);
    gcTableSync.setDataBeanTo(gcTableSyncTableBeanSql);

    GcTableSyncTableMetadata gcTableSyncTableMetadata = gcTableSyncTableBeanSql.getTableMetadata();
    gcTableSyncTableMetadata.assignColumns(columnsCommaSeparated);
    gcTableSyncTableMetadata.assignPrimaryKeyColumns(GrouperClientUtils.join(this.columnNamesPrimaryKey.iterator(), ","));

    String sql = "select " + gcTableSyncTableMetadata.columnListAll() + " from " + gcTableSyncTableMetadata.getTableName();
    long nowNanos = System.nanoTime();
    
    List<Object[]> sqlResults = new GcDbAccess().connectionName(this.connectionName).sql(sql).selectList(Object[].class);

    this.debugMap.put(this.debugMapPrefix+"dbRows", GrouperClientUtils.length(sqlResults));
    this.debugMap.put(this.debugMapPrefix+"sqlSelectMillis", (System.nanoTime() - nowNanos)/1000000);

    GcTableSyncTableData gcTableSyncTableDataSql = new GcTableSyncTableData();
    gcTableSyncTableDataSql.init(gcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(columnsCommaSeparated), sqlResults);
    gcTableSyncTableDataSql.indexData();

    gcTableSyncTableBeanSql.setDataInitialQuery(gcTableSyncTableDataSql);
    gcTableSyncTableBeanSql.setGcTableSync(gcTableSync);

    // setup the data from the WS
    GcTableSyncTableBean gcTableSyncTableBeanWs = new GcTableSyncTableBean();
    gcTableSync.setDataBeanFrom(gcTableSyncTableBeanWs);
    gcTableSyncTableBeanWs.setTableMetadata(gcTableSyncTableBeanSql.getTableMetadata());
    gcTableSyncTableBeanWs.setGcTableSync(gcTableSync);
    
    GcTableSyncTableData gcTableSyncTableDataWs = new GcTableSyncTableData();
    gcTableSync.getDataBeanFrom().setDataInitialQuery(gcTableSyncTableDataWs);

    gcTableSyncTableDataWs.setColumnMetadata(gcTableSyncTableDataSql.getColumnMetadata());

    gcTableSyncTableDataWs.setGcTableSyncTableBean(gcTableSyncTableDataSql.getGcTableSyncTableBean());

    gcTableSyncTableDataWs.init(gcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(columnsCommaSeparated), this.data);
    gcTableSyncTableDataWs.indexData();

    GcTableSyncSubtype.fullSyncFull.syncData(this.debugMap, gcTableSync);

  }
  

}
