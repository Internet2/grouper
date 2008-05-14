/*
 * @author mchyzer
 * $Id: GrouperLoaderResultset.java,v 1.1 2008-04-28 06:40:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.loader.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.loader.util.GrouperLoaderUtils;


/**
 * encapsulate a resultset into this resultset to be case-insensitive and
 * column-order insensitive
 */
public class GrouperLoaderResultset {
  
  /**
   * 
   */
  public static final String SUBJECT_ID_COL = "SUBJECT_ID";

  /**
   * 
   */
  public static final String SUBJECT_SOURCE_ID_COL = "SUBJECT_SOURCE_ID";

  /**
   * get a resultset based on a db and query
   * @param grouperLoaderDb
   * @param query
   */
  public GrouperLoaderResultset(GrouperLoaderDb grouperLoaderDb, String query) {

    //small security check (not failsafe, but better than nothing)
    if (!query.toLowerCase().trim().startsWith("select")) {
      throw new RuntimeException("Invalid query, must start with select: " + query);
    }
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      connection = grouperLoaderDb.connection();
      try {
        // create and execute a SELECT
        statement = connection.createStatement();
        resultSet = statement.executeQuery(query);
        
        //lets get some column info and stuff
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        
        int columnCount = resultSetMetaData.getColumnCount();
        for (int i=0;i<columnCount;i++) {
          this.columnNames.add(resultSetMetaData.getColumnName(i+1));
          this.columnTypes.add(resultSetMetaData.getColumnType(i+1));
        }
        
        while(resultSet.next()) {
          
          Object[] row = new Object[columnCount];
          this.data.add(row);
          //at this point, assume everything is either a string or a timestamp
          for (int i=0;i<columnCount;i++) {
            if (this.columnTypes.get(i) == Types.TIMESTAMP) {
              row[i] = resultSet.getTimestamp(i+1);
            } else {
              row[i] = resultSet.getString(i+1);
            }
          }
          
        }
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperLoaderUtils.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
    } finally {
      GrouperLoaderUtils.closeQuietly(resultSet);
      GrouperLoaderUtils.closeQuietly(statement);
      GrouperLoaderUtils.closeQuietly(connection);
    }
    
  }
  
  /** column names (toUpper) */
  private List<String> columnNames = new ArrayList<String>(); 
  
  /** column types (from java.sql.Types) */
  private List<Integer> columnTypes = new ArrayList<Integer>(); 
  
  /** array of arrays of data for the grid of the resultset
   * the number of cols will equal the number of column names
   * user arrays since lightweight
   */
  private List<Object[]> data = new ArrayList<Object[]>();

  /**
   * find a column index in the resultset
   * @param columnNameInput
   * @return the column index
   */
  public int columnIndex(String columnNameInput) {
    int i = 0; 
    for(String columnName : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, columnNameInput)) {
        return i;
      }
      i++;
    }
    throw new RuntimeException("Cant find column: " + columnNameInput);
  }

  /**
   * return the number of rows
   * @return the number of rows
   */
  public int numberOfRows() {
    return this.data == null ? 0 : this.data.size();
  }
  
  /**
   * return the column names
   * @return the column names
   */
  public List<String> getColumnNames() {
    return this.columnNames;
  }
  
  /**
   * get a cell in the data structure
   * @param rowIndex
   * @param columnName
   * @param exceptionOnColNotFound
   * @return the cell or null if col not found and not throwing exception if col not found
   */
  public Object getCell(int rowIndex, String columnName, boolean exceptionOnColNotFound) {
    if (this.hasColumnName(columnName)) {
      int columnIndex = this.columnIndex(columnName);
      Object[] row = this.data.get(rowIndex);
      return row[columnIndex];
    }
    if (exceptionOnColNotFound) {
      throw new RuntimeException("Column not found: " + columnName);
    }
    return null;
  }
  
  /**
   * make sure this column name is here
   * @param columnName
   */
  public void assertColumnName(String columnName) {
    for (String existingColumn : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, existingColumn)) {
        return;
      }
    }
    StringBuilder error = new StringBuilder("Cant find column: '" + columnName + "' in columns: ");
    for (String existingColumn : this.columnNames) {
      error.append(existingColumn).append(", ");
    }
    throw new RuntimeException(error.toString());
  }

  /**
   * make sure this column name is here
   * @param columnName
   * @return true if the column is there
   */
  public boolean hasColumnName(String columnName) {

    for (String existingColumn : this.columnNames) {
      if (StringUtils.equalsIgnoreCase(columnName, existingColumn)) {
        return true;
      }
    }
    return false;
  }

  /**
   * find a member and remove
   * @param subjectId
   * @param subjectSourceId
   * @return if one was removed
   */
  public boolean remove(String subjectId, String subjectSourceId) {
    int subjectIndex = this.columnIndex(SUBJECT_ID_COL);

    //might not have subject source id
    boolean hasSubjectSourceIdCol = this.hasColumnName(SUBJECT_SOURCE_ID_COL);
    int subjectSourceIdIndex = hasSubjectSourceIdCol ? this.columnIndex(SUBJECT_SOURCE_ID_COL) : -1;

    boolean foundMatch = false;
    
    Iterator<Object[]> iterator = this.data.iterator();
    while(iterator.hasNext()) {
      Object[] row = iterator.next();
      if (StringUtils.equals((String)row[subjectIndex], subjectId)) {
        if (hasSubjectSourceIdCol) {
          if (!StringUtils.equals((String)row[subjectSourceIdIndex], subjectSourceId)) {
            continue;
          }
        }
        //at this point, they are the same
        foundMatch = true;
        iterator.remove();
        //dont break, since could have multiple
      }
    }
    return foundMatch;
  }
}
