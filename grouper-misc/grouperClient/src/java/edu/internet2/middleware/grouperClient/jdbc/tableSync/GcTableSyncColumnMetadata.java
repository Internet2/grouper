/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 */
public class GcTableSyncColumnMetadata {

  /**
   * 
   */
  public static enum ColumnType {
    
    /**
     * 
     */
    NUMERIC {

      @Override
      public Object readDataFromResultSet(int columnOneIndexed, ResultSet resultSet) throws SQLException {
        return resultSet.getBigDecimal(columnOneIndexed);
      }
    },
    
    /**
     * 
     */
    STRING {

      @Override
      public Object readDataFromResultSet(int columnOneIndexed, ResultSet resultSet) throws SQLException {
        return resultSet.getString(columnOneIndexed);
      }
    },
    
    /**
     * 
     */
    TIMESTAMP {

      @Override
      public Object readDataFromResultSet(int columnOneIndexed, ResultSet resultSet) throws SQLException {
        return resultSet.getTimestamp(columnOneIndexed);
      }
    };

    /**
     * read data from result set based on column index
     * @param columnOneIndexed
     * @param resultSet
     * @return the object
     * @throws SQLException
     */
    public abstract Object readDataFromResultSet(int columnOneIndexed, ResultSet resultSet) throws SQLException;
    
  }
  
  /**
   * column index zero indexed
   */
  private int columnIndexZeroIndexed = -1;
  
  /**
   * column index zero indexed
   * @return the columnIndexZeroIndexed
   */
  public int getColumnIndexZeroIndexed() {
    return this.columnIndexZeroIndexed;
  }
  
  /**
   * @param columnIndexZeroIndexed1 the columnIndexZeroIndexed to set
   */
  public void setColumnIndexZeroIndexed(int columnIndexZeroIndexed1) {
    this.columnIndexZeroIndexed = columnIndexZeroIndexed1;
  }

  /**
   * type of column
   */
  private ColumnType columnType;
  
  
  /**
   * type of column
   * @return the columnType
   */
  public ColumnType getColumnType() {
    return this.columnType;
  }


  
  /**
   * type of column
   * @param columnType1 the columnType to set
   */
  public void setColumnType(ColumnType columnType1) {
    this.columnType = columnType1;
  }


  /**
   * name of column
   */
  private String columnName;
  
  /**
   * 
   */
  public GcTableSyncColumnMetadata() {
  }

  
  /**
   * name of column
   * @return the columnName
   */
  public String getColumnName() {
    return this.columnName;
  }

  
  /**
   * name of column
   * @param columnName1 the columnName to set
   */
  public void setColumnName(String columnName1) {
    this.columnName = columnName1;
  }

  /**
   * if is primary key
   */
  private boolean primaryKey;
  
  /**
   * if is primary key
   * @return the primaryKey
   */
  public boolean isPrimaryKey() {
    return this.primaryKey;
  }
  
  /**
   * @param primaryKey1 the primaryKey to set
   */
  public void setPrimaryKey(boolean primaryKey1) {
    this.primaryKey = primaryKey1;
  }

  /**
   * grouping column
   */
  private boolean groupingColumn;
  
  /**
   * grouping column
   * @return the groupingColumn
   */
  public boolean isGroupingColumn() {
    return this.groupingColumn;
  }
  
  /**
   * @param groupingColumn1 the groupingColumn to set
   */
  public void setGroupingColumn(boolean groupingColumn1) {
    this.groupingColumn = groupingColumn1;
  }

  /**
   * realTimeLastUpdatedColumn column
   */
  private boolean realTimeLastUpdatedColumn;
  
  /**
   * @param b
   */
  public void setRealTimeLastUpdatedColumn(boolean b) {
    this.realTimeLastUpdatedColumn = b;
  }

  /**
   * is it real time last updated column
   * @return if real time last updated
   */
  public boolean isRealTimeLastUpdatedColumn() {
    return this.realTimeLastUpdatedColumn;
  }
  
}
