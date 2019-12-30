/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcResultSetCallback;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 * metadata for connection name and table name
 */
public class GcTableSyncTableMetadata {


  /**
   * non primary key col(s), which is sync'ed columns with primary key removed
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKey;

  /**
   * "primary key" col(s), as assigned by configuration
   */
  private List<GcTableSyncColumnMetadata> primaryKeyColumns;

  /**
   * 
   */
  private Map<String, GcTableSyncColumnMetadata> columnUpperNameToGcColumnMetadata;
  
  /**
   * find metadata for columns
   * @param columnNames
   * @return the list of columns
   */
  public List<GcTableSyncColumnMetadata> lookupColumns(String columnNames) {
    if (StringUtils.isBlank(columnNames)) {
      throw new RuntimeException("Pass in columns for " + this.getConnectionName() + " -> " + this.getTableName());
    }
    
    List<GcTableSyncColumnMetadata> result = new ArrayList<GcTableSyncColumnMetadata>();
    
    if (StringUtils.equals(columnNames, "*")) {
      result.addAll(this.getColumnMetadata());
    } else {
      
      for (String columnName : GrouperClientUtils.splitTrim(columnNames, ",")) {
        GcTableSyncColumnMetadata gcTableSyncColumnMetadata = lookupColumn(columnName);
        result.add(gcTableSyncColumnMetadata);
      }
    }
    
    return result;
  }
  
  /**
   * lookup a column by name (case insensitive)
   * @param columnName
   * @return the column metadata
   */
  public GcTableSyncColumnMetadata lookupColumn(String columnName) {
    
    if (this.columnUpperNameToGcColumnMetadata == null) {
      
      if (GrouperClientUtils.length(this.columnMetadata) == 0) {
        throw new RuntimeException("Cant find table metadata for " + this.connectionName + " -> " + this.tableName + "!");
      }
      
      Map<String, GcTableSyncColumnMetadata> tempColumnUpperNameToGcColumnMetadata = new HashMap<String, GcTableSyncColumnMetadata>();
      
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.columnMetadata) {
        tempColumnUpperNameToGcColumnMetadata.put(gcTableSyncColumnMetadata.getColumnName().toUpperCase(), gcTableSyncColumnMetadata);
      }
      
      this.columnUpperNameToGcColumnMetadata = tempColumnUpperNameToGcColumnMetadata;
    }
    
    GcTableSyncColumnMetadata gcTableSyncColumnMetadata = this.columnUpperNameToGcColumnMetadata.get(columnName.toUpperCase());
    
    if (gcTableSyncColumnMetadata == null) {
      throw new RuntimeException("Cant find " + this.connectionName + " -> " + this.tableName + " -> " + columnName);
    }
    
    return gcTableSyncColumnMetadata;
    
  }
  
  /**
   * cache database metadata for a little while
   */
  private static ExpirableCache<MultiKey, GcTableSyncTableMetadata> metadataCache = null;

  /**
   * cache metadata for tables for a little while
   * @return the cache
   */
  private static ExpirableCache<MultiKey, GcTableSyncTableMetadata> metadataCache() {
    if (metadataCache == null) {
      metadataCache = new ExpirableCache(GrouperClientConfig.retrieveConfig().propertyValueInt("tableSyncMetadataCacheMinutes", 10));
    }
    return metadataCache;
  }

  /**
   * get metadata for table
   * @param connectionName
   * @param tableName
   * @return the metadata for a connection, table, and query
   */
  public static GcTableSyncTableMetadata retrieveTableMetadataFromCacheOrDatabase(String connectionName, String tableName) {
    
    MultiKey multiKey = new MultiKey(connectionName, tableName);
    GcTableSyncTableMetadata gcTableSyncTableMetadata = metadataCache().get(multiKey);
    if (gcTableSyncTableMetadata != null) {
      return gcTableSyncTableMetadata;
    }
    
    gcTableSyncTableMetadata = retrieveTableMetadataFromDatabase(connectionName, tableName);
    metadataCache().put(multiKey, gcTableSyncTableMetadata);
    return gcTableSyncTableMetadata;
  }
    

  /**
   * get metadata for table
   * @param theConnectionName
   * @param tableName
   * @return the metadata for a connection, table, and query
   */
  public static GcTableSyncTableMetadata retrieveTableMetadataFromDatabase(String theConnectionName, String tableName) {
    
      
    if (GrouperClientUtils.isBlank(tableName)) {
      throw new RuntimeException("tableName cannot be blank");
    }
    if (GrouperClientUtils.isBlank(theConnectionName)) {
      throw new RuntimeException("connectionName cannot be blank");
    }
    
    GcTableSyncTableMetadata gcTableSyncTableMetadata = new GcTableSyncTableMetadata();
    gcTableSyncTableMetadata.setConnectionName(theConnectionName);
    gcTableSyncTableMetadata.setTableName(tableName);
    
    String sql = "select * from " + tableName + " where 1 != 1";
    
    final ArrayList<GcTableSyncColumnMetadata> gcTableSyncColumnMetadatas = new ArrayList<GcTableSyncColumnMetadata>();
    gcTableSyncTableMetadata.setColumnMetadata(gcTableSyncColumnMetadatas);
    // go to database from and look up metadata
    new GcDbAccess().connectionName(theConnectionName).sql(sql).callbackResultSet(new GcResultSetCallback() {

      @Override
      public Object callback(ResultSet resultSet) throws Exception {
        
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        
        for (int i=0;i<resultSetMetaData.getColumnCount();i++) {
          GcTableSyncColumnMetadata gcTableSyncColumnMetadata = new GcTableSyncColumnMetadata();
          gcTableSyncColumnMetadatas.add(gcTableSyncColumnMetadata);
          
          String columnName = resultSetMetaData.getColumnName(i+1);
          int dataType = resultSetMetaData.getColumnType(i+1);
          String typeName = resultSetMetaData.getColumnTypeName(i+1);

          gcTableSyncColumnMetadata.setColumnIndexZeroIndexed(i);
          
          gcTableSyncColumnMetadata.setColumnName(columnName);
          
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
              {
                int precision = resultSetMetaData.getPrecision(i+1);
                gcTableSyncColumnMetadata.setPrecision(precision);
              }

              {
                int scale = resultSetMetaData.getScale(i+1);
                gcTableSyncColumnMetadata.setScale(scale);
              }
              break;
              
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:

              gcTableSyncColumnMetadata.setColumnType(ColumnType.STRING);
              {
                int columnDisplaySize = resultSetMetaData.getColumnDisplaySize(i+1);
                gcTableSyncColumnMetadata.setColumnDisplaySize(columnDisplaySize);
              }
              break;

            case Types.DATE:
            case Types.TIMESTAMP:
              
              gcTableSyncColumnMetadata.setColumnType(ColumnType.TIMESTAMP);
              break; 
              
            default:
              throw new RuntimeException("Type not supported: " + dataType + ", " + typeName);
              

          }
        }
        return null;
      }
      
    });

    if (gcTableSyncColumnMetadatas.size() == 0) {
      throw new RuntimeException("Cant find table metadata for '" + tableName + "' in grouper.client.properties database: '" + theConnectionName + "'");
    }
      
    return gcTableSyncTableMetadata;
  }
  
  
  /**
   * @return the connectionName
   */
  public String getConnectionName() {
    return this.connectionName;
  }

  
  /**
   * @param connectionName the connectionName to set
   */
  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  
  /**
   * @return the tableName
   */
  public String getTableName() {
    return this.tableName;
  }

  
  /**
   * @param tableName the tableName to set
   */
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  /** 
   * database connection name
   */
  private String connectionName;
  
  /**
   * table name (could include schema at front)
   */
  private String tableName;
  
  /**
   * 
   */
  public GcTableSyncTableMetadata() {
  }
  
  /**
   * columns in table
   */
  private List<GcTableSyncColumnMetadata> columnMetadata;

  /**
   * all columns to sync that we care about
   */
  private List<GcTableSyncColumnMetadata> columns;

  /**
   * if grouping this is the grouping column
   */
  private GcTableSyncColumnMetadata groupingColumn;

  
  /**
   * columns in table
   * @return the columnMetadata
   */
  public List<GcTableSyncColumnMetadata> getColumnMetadata() {
    return this.columnMetadata;
  }

  
  /**
   * columns in table
   * @param columnMetadata1 the columnMetadata to set
   */
  public void setColumnMetadata(List<GcTableSyncColumnMetadata> columnMetadata1) {
    this.columnMetadata = columnMetadata1;
  }

  /**
   * @param theColumns could be * or list of columns
   */
  public void assignPrimaryKeyColumns(String theColumns) {
    this.primaryKeyColumns = this.lookupColumns(theColumns);
  }

  /**
   * mtadata for columns synced
   * @return the columns
   */
  public List<GcTableSyncColumnMetadata> getColumns() {
    return this.columns;
  }

  /**
   * non primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getNonPrimaryKey() {
    if (this.nonPrimaryKey == null) {
      
      List<GcTableSyncColumnMetadata> result = new ArrayList();
      result.addAll(this.getColumns());
      result.removeAll(this.getPrimaryKey());
      
    }
    
    return this.nonPrimaryKey;
  }

  /**
   * primary key col(s), lazy loaded
   * @return the primary key
   */
  public List<GcTableSyncColumnMetadata> getPrimaryKey() {
    return this.primaryKeyColumns;
  }

  /**
   * @param theColumns could be * or list of columns
   */
  public void assignColumns(String theColumns) {
    this.columns = this.lookupColumns(theColumns);
  }

  /**
   * mtadata for columns synced
   * @param columns1 the columns to set
   */
  public void setColumns(List<GcTableSyncColumnMetadata> columns1) {
    this.columns = columns1;
  }

  /**
   * 
   * @param groupingColumnName
   */
  public void assignGroupingColumn(String groupingColumnName) {
    this.groupingColumn = this.lookupColumn(groupingColumnName);
  }

  /**
   * get grouping column metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getGroupingColumnMetadata() {
    return this.getGroupingColumnMetadata();
  }
  
}
