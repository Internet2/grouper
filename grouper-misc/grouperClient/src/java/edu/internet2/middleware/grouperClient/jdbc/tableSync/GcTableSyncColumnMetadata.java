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

  equals
  
  hashcode
  
  /**
   * 
   */
  public static enum ColumnType {
    
    /**
     * 
     */
    NUMERIC {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getBigDecimal(gcTableSyncColumnMetadata.getColumnName());
      }
    },
    
    /**
     * 
     */
    STRING {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getString(gcTableSyncColumnMetadata.getColumnName());
      }
    },
    
    /**
     * 
     */
    TIMESTAMP {

      @Override
      public Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException {
        return resultSet.getTimestamp(gcTableSyncColumnMetadata.getColumnName());
      }
    };

    /**
     * read data from result set based on column index
     * @param columnOneIndexed
     * @param resultSet
     * @return the object
     * @throws SQLException
     */
    public abstract Object readDataFromResultSet(GcTableSyncColumnMetadata gcTableSyncColumnMetadata, ResultSet resultSet) throws SQLException;
    
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
   * precision of number in database
   */
  private int precision;
  
  
  /**
   * precision of number in database
   * @return the precision
   */
  public int getPrecision() {
    return this.precision;
  }

  
  /**
   * precision of number in database
   * @param precision1 the precision to set
   */
  public void setPrecision(int precision1) {
    this.precision = precision1;
  }

  /**
   * scale of number in database
   */
  private int scale;

  
  /**
   * scale of number in database
   * @return the scale
   */
  public int getScale() {
    return this.scale;
  }
  
  /**
   * scale of number in database
   * @param scale1 the scale to set
   */
  public void setScale(int scale1) {
    this.scale = scale1;
  }


  /**
   * length of string cols
   */
  private int columnDisplaySize;
  
  /**
   * length of string cols
   * @return the stringLength
   */
  public int getColumnDisplaySize() {
    return this.columnDisplaySize;
  }

  
  /**
   * length of string cols
   * @param stringLength1 the stringLength to set
   */
  public void setColumnDisplaySize(int stringLength1) {
    this.columnDisplaySize = stringLength1;
  }

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

  
}
