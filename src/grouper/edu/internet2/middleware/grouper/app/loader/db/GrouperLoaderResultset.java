/*
 * @author mchyzer
 * $Id: GrouperLoaderResultset.java,v 1.7 2009-03-22 05:41:01 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


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
  public static final String GROUP_NAME_COL = "GROUP_NAME";

  /**
   * 
   */
  public static final String GROUP_DISPLAY_NAME_COL = "GROUP_DISPLAY_NAME";

  /**
   * 
   */
  public static final String GROUP_DESCRIPTION_COL = "GROUP_DESCRIPTION";

  /**
   * 
   */
  public static final String SUBJECT_SOURCE_ID_COL = "SUBJECT_SOURCE_ID";

  /**
   * get a resultset on another resultset and a group name
   * @param parentResultSet
   * @param groupName
   */
  public GrouperLoaderResultset(GrouperLoaderResultset parentResultSet, String groupName) {
    this.columnNames = parentResultSet.columnNames == null ? null : new ArrayList<String>(parentResultSet.columnNames);
    this.columnTypes = parentResultSet.columnTypes == null ? null : new ArrayList<Integer>(parentResultSet.columnTypes);
    for (int i=0;i<parentResultSet.data.size();i++) {
      
      if (StringUtils.equals(groupName, (String)parentResultSet.getCell(i, GROUP_NAME_COL, true))) {
        //dont clone the row, just add the row there
        this.data.add(parentResultSet.data.get(i));
      }
      
    }
  }
  
  /**
   * get a set of group names
   * @return the set of names, never null
   */
  public Set<String> groupNames() {
    Set<String> groupNames = new LinkedHashSet<String>();
    for (int i=0;i<this.data.size();i++) {
      groupNames.add((String)this.getCell(i, GROUP_NAME_COL, true));
    }
    return groupNames;
  }
  
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
          this.columnNames.add(resultSetMetaData.getColumnLabel(i+1));
          this.columnTypes.add(resultSetMetaData.getColumnType(i+1));
        }
        
        while(resultSet.next()) {
          Row row = new Row();
          Object[] rowData = new Object[columnCount];
          row.setRowData(rowData);
          this.data.add(row);
          //at this point, assume everything is either a string or a timestamp
          for (int i=0;i<columnCount;i++) {
            if (this.columnTypes.get(i) == Types.TIMESTAMP) {
              rowData[i] = resultSet.getTimestamp(i+1);
            } else {
              rowData[i] = resultSet.getString(i+1);
            }
          }
          
        }
      } finally {
        //this is important so no one sneaks some delete statement in there
        GrouperUtil.rollbackQuietly(connection);
      }
    } catch (SQLException se) {
      throw new RuntimeException("Problem with query: " + query + ",  on db: " + grouperLoaderDb, se);
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
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
  private List<Row> data = new ArrayList<Row>();

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderResultset.class);

  /**
   * simple struct for subjects
   */
  public class Row {
    /** row data from db */
    private Object[] rowData;
    
    /** the subject for this row */
    private Subject subject;
    
    /** the error for this row */
    private String subjectError;

    /**
     * @return the rowData
     */
    public Object[] getRowData() {
      return this.rowData;
    }
    
    /**
     * @param rowData1 the rowData to set
     */
    public void setRowData(Object[] rowData1) {
      this.rowData = rowData1;
    }
    
    /**
     * @return the subject
     */
    public Subject getSubject() {
      if (this.subject != null || this.subjectError != null) {
        return this.subject;
      }
      //if it is null, and null, then it must not have been retrieved,
      //so get it
      String subjectId = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_ID_COL, true);
      String subjectSourceId = (String)this.getCell(
          GrouperLoaderResultset.SUBJECT_SOURCE_ID_COL, false);

      String defaultSubjectSourceId = GrouperLoaderConfig.getPropertyString(
          GrouperLoaderConfig.DEFAULT_SUBJECT_SOURCE_ID);
      
      //maybe get the sourceId from config file
      subjectSourceId = StringUtils.defaultString(subjectSourceId, defaultSubjectSourceId);
      try {
        if (!StringUtils.isBlank(subjectSourceId)) {
          this.subject = SubjectFinder.getSource(subjectSourceId).getSubject(subjectId, true);
        } else {
          this.subject = SubjectFinder.findById(subjectId, true);
        }
      } catch (Exception e) {
        this.subjectError = "Problem with subjectId: " 
            + subjectId + ", subjectSourceId: " + subjectSourceId;
        LOG.error(this.subjectError, e); 
        if (e instanceof SubjectNotFoundException
            || e instanceof SubjectNotUniqueException
            || e instanceof SourceUnavailableException) {
          //swallow these...
        } else {
          //rethrow these
          if (e instanceof RuntimeException) {
            throw (RuntimeException)e;
          }
          //this shouldnt really be possible
          throw new RuntimeException(e);
        }
         
      }
      return this.subject;
    }

    /**
     * get a cell in the data structure
     * @param columnName
     * @param exceptionOnColNotFound
     * @return the cell or null if col not found and not throwing exception if col not found
     */
    public Object getCell(String columnName, boolean exceptionOnColNotFound) {

      if (GrouperLoaderResultset.this.hasColumnName(columnName)) {
        int columnIndex = GrouperLoaderResultset.this.columnIndex(columnName);
        return this.rowData[columnIndex];
      }
      if (exceptionOnColNotFound) {
        throw new RuntimeException("Column not found: " + columnName);
      }
      return null;
    }
    
    /**
     * @return the error
     */
    public String getSubjectError() {
      return this.subjectError;
    }
    
  }
  
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
   * find a certain row
   * @param i
   * @return the row
   */
  public Row retrieveRow(int i) {
    return this.data.get(i);
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
    return this.data.get(rowIndex).getCell(columnName, exceptionOnColNotFound);
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
   * remove by row
   * @param row
   */
  public void remove(Row row) {
    this.data.remove(row);
  }

  /**
   * find a row and return
   * @param subjectId
   * @param subjectSourceId
   * @return row if found, else null
   */
  public Row find(String subjectId, String subjectSourceId) {
    int subjectIndex = this.columnIndex(SUBJECT_ID_COL);

    //might not have subject source id
    boolean hasSubjectSourceIdCol = this.hasColumnName(SUBJECT_SOURCE_ID_COL);
    int subjectSourceIdIndex = hasSubjectSourceIdCol ? this.columnIndex(SUBJECT_SOURCE_ID_COL) : -1;

    Iterator<Row> iterator = this.data.iterator();
    while(iterator.hasNext()) {
      Row row = iterator.next();
      Object[] rowData = row.getRowData();
      if (StringUtils.equals((String)rowData[subjectIndex], subjectId)) {
        if (hasSubjectSourceIdCol) {
          if (!StringUtils.equals((String)rowData[subjectSourceIdIndex], subjectSourceId)) {
            continue;
          }
        }
        //at this point, they are the same
        return row;
        //dont break, since could have multiple
      }
    }
    return null;
  }

}
