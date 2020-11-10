/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcResultSetCallback;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata.ColumnType;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * metadata for connection name and table name
 */
public class GcTableSyncTableMetadata {

  /**
   * append the primary key where clause
   * @param sql
   */
  public String queryWherePrimaryKey() {
    
    if (GrouperClientUtils.length(this.getPrimaryKey()) == 0) {
      throw new RuntimeException("No primary key for '" + this.tableName + "'!");
    }
    boolean first = true;
    StringBuilder result = new StringBuilder();
    
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.getPrimaryKey()) {
      
      if (!first) {
        result.append(" and ");
      }
      
      result.append(" ").append(gcTableSyncColumnMetadata.getColumnName()).append(" = ? ");
      
      first = false;
    }
    
    return result.toString();
  }

  /**
   * append the nonprimary key update clause
   * @param sql
   */
  public String queryUpdateNonPrimaryKey() {
    
    if (GrouperClientUtils.length(this.getNonPrimaryKey()) == 0) {
      throw new RuntimeException("No non-primary key for '" + this.tableName + "'!");
    }
    boolean first = true;
    StringBuilder result = new StringBuilder();
    
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : this.getNonPrimaryKey()) {
      
      if (!first) {
        result.append(" , ");
      }
      
      result.append(" ").append(gcTableSyncColumnMetadata.getColumnName()).append(" = ? ");
      
      first = false;
    }
    
    return result.toString();
  }

  /**
   * non primary key col(s), which is sync'ed columns with primary key removed
   */
  private List<GcTableSyncColumnMetadata> nonPrimaryKey;

  /**
   * column in progress table which increments as integer or timestamp
   */
  private GcTableSyncColumnMetadata incrementalProgressColumn;
  
  /**
   * column in progress table which increments as integer or timestamp
   * @return column
   */
  public GcTableSyncColumnMetadata getIncrementalProgressColumn() {
    return this.incrementalProgressColumn;
  }

  /**
   * column in progress table which increments as integer or timestamp
   * @param incrementalProgressColumn1
   */
  public void setIncrementalProgressColumn(
      GcTableSyncColumnMetadata incrementalProgressColumn1) {
    this.incrementalProgressColumn = incrementalProgressColumn1;
  }

  /**
   * 
   * @param incrementalProgressColumnName
   */
  public void assignIncrementalProgressColumn(String incrementalProgressColumnName) {
    this.incrementalProgressColumn = this.lookupColumn(incrementalProgressColumnName, true);
  }

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
        GcTableSyncColumnMetadata gcTableSyncColumnMetadata = lookupColumn(columnName, true);
        result.add(gcTableSyncColumnMetadata);
      }
    }
    
    return result;
  }
  
  /**
   * lookup a column by name (case insensitive)
   * @param columnName
   * @param exceptionOnNotFound
   * @return the column metadata
   */
  public GcTableSyncColumnMetadata lookupColumn(String columnName, boolean exceptionOnNotFound) {
    
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
    
    if (gcTableSyncColumnMetadata == null && exceptionOnNotFound) {
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
    
    try {
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
          
          Collections.sort(gcTableSyncColumnMetadatas, new Comparator<GcTableSyncColumnMetadata>() {
  
            @Override
            public int compare(GcTableSyncColumnMetadata o1, GcTableSyncColumnMetadata o2) {
              
              if (o1 == o2) {
                return 0;
              }
              if (o1 == null) {
                return -1;
              }
              if (o2 == null) {
                return 1;
              }
              return o1.getColumnName().compareTo(o2.getColumnName());
            }
          });
          
          return null;
        }
        
      });
    } catch (Exception e) {
      LOG.error("Error finding metadata for '" + tableName + "' in database: '" + theConnectionName + "'", e);
    }

    if (gcTableSyncColumnMetadatas.size() == 0) {
      throw new RuntimeException("Cant find table metadata for '" + tableName + "' in database: '" + theConnectionName + "'");
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
   * database connection name or readonly
   */
  private String connectionNameOrReadonly;
  
  /**
   * database connection name or readonly
   * @return connection name
   */
  public String getConnectionNameOrReadonly() {
    return this.connectionNameOrReadonly;
  }

  /**
   * database connection name or readonly
   * @param connectionNameOrReadonly1
   */
  public void setConnectionNameOrReadonly(String connectionNameOrReadonly1) {
    this.connectionNameOrReadonly = connectionNameOrReadonly1;
  }

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
   * 
   */
  private static Log LOG = GrouperClientUtils.retrieveLog(GcTableSyncTableMetadata.class);

  /**
   * if group this is the group column
   */
  private GcTableSyncColumnMetadata groupColumn;

  /**
   * if full sync with change flag this is the column
   */
  private GcTableSyncColumnMetadata changeFlagColumn;

  /**
   * column in FROM table which has incrementing timestamp or integer
   */
  private GcTableSyncColumnMetadata incrementalAllCoumnsColumn;

  /**
   * column in FROM table which has incrementing timestamp or integer
   * @return metadata
   */
  public GcTableSyncColumnMetadata getIncrementalAllCoumnsColumn() {
    return this.incrementalAllCoumnsColumn;
  }

  /**
   * column in FROM table which has incrementing timestamp or integer
   * @param incrementalAllCoumnsColumn1
   */
  public void setIncrementalAllCoumnsColumn(
      GcTableSyncColumnMetadata incrementalAllCoumnsColumn1) {
    this.incrementalAllCoumnsColumn = incrementalAllCoumnsColumn1;
  }

  /**
   * if full sync with change flag this is the column
   * @return change flag
   */
  public GcTableSyncColumnMetadata getChangeFlagColumn() {
    return this.changeFlagColumn;
  }

  /**
   * if full sync with change flag this is the column
   * @param changeFlagColumn1
   */
  public void setChangeFlagColumn(GcTableSyncColumnMetadata changeFlagColumn1) {
    this.changeFlagColumn = changeFlagColumn1;
  }

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
      this.nonPrimaryKey = result;
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
   * get comma separated list of all columns
   * @return the columns
   */
  public String columnListAll() {
    StringBuilder result = new StringBuilder();
    boolean first = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : GrouperClientUtils.nonNull(this.columns)) {
      if (!first) {
        result.append(", ");
      }
      result.append(gcTableSyncColumnMetadata.getColumnName());
      
      first = false;
    }
    return result.toString();
  }
  
  /**
   * get comma separated list of primary key and change flag, and optional incremental change column
   * @return the columns
   */
  public String columnListPrimaryKeyAndChangeFlagAndOptionalIncrementalProgress() {
    StringBuilder result = new StringBuilder();

    Set<String> columnNames = new HashSet<String>();
    boolean first = true;
    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : GrouperClientUtils.nonNull(this.columns)) {

      if (!first) {
        result.append(", ");
      }

      result.append(gcTableSyncColumnMetadata.getColumnName());
      columnNames.add(gcTableSyncColumnMetadata.getColumnName());
      first = false;
    }
    if (!columnNames.contains(this.getChangeFlagColumn().getColumnName())) {
      result.append(", ");
      result.append(this.getChangeFlagColumn().getColumnName());
      columnNames.add(this.getChangeFlagColumn().getColumnName());
    }
    if (this.getIncrementalAllCoumnsColumn() != null) {
      if (!columnNames.contains(this.getIncrementalAllCoumnsColumn().getColumnName())) {
        result.append(", ");
        result.append(this.getIncrementalAllCoumnsColumn().getColumnName());
        columnNames.add(this.getChangeFlagColumn().getColumnName());
      }
    }
    return result.toString();
  }
  
  /**
   * get comma separated list of primary key and change flag
   * @return the columns
   */
  public String columnListInputtedColumnsAndIncrementalProgressColumn(List<GcTableSyncColumnMetadata> otherTablePrimaryKey) {
    StringBuilder result = new StringBuilder();

    for (GcTableSyncColumnMetadata gcTableSyncColumnMetadataOther : otherTablePrimaryKey) {

      GcTableSyncColumnMetadata gcTableSyncColumnMetadataThis = this.lookupColumn(gcTableSyncColumnMetadataOther.getColumnName(), true);
      result.append(gcTableSyncColumnMetadataThis.getColumnName());
      
      result.append(", ");
    }
    result.append(this.getIncrementalProgressColumn().getColumnName());
    return result.toString();
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
   * @param groupColumnName
   */
  public void assignGroupColumn(String groupColumnName) {
    this.groupColumn = this.lookupColumn(groupColumnName, true);
  }

  /**
   * 
   * @param changeFlagColumnName
   */
  public void assignChangeFlagColumn(String changeFlagColumnName) {
    this.changeFlagColumn = this.lookupColumn(changeFlagColumnName, true);
  }

  /**
   * get group column metadata
   * @return the metadata
   */
  public GcTableSyncColumnMetadata getGroupColumnMetadata() {
    return this.groupColumn;
  }

  /**
   * 
   * @param incrementalAllColumnsColumnName
   */
  public void assignIncrementalAllCoumnsColumn(String incrementalAllColumnsColumnName) {
    this.incrementalAllCoumnsColumn = this.lookupColumn(incrementalAllColumnsColumnName, true);
  }
  
}
