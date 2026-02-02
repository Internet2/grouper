/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc.tableSync;

import java.util.Set;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 *
 */
public class GcTableSyncOutput {

  
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * millis get data
   */
  private long millisGetData = 0;
  
  /**
   * millis load data
   */
  private long millisLoadData = 0;
  
  
  
  /**
   * millis get data
   * @return
   */
  public long getMillisGetData() {
    return this.millisGetData;
  }

  /**
   * millis get data
   * @param millisGetData1
   */
  public void setMillisGetData(long millisGetData1) {
    this.millisGetData = millisGetData1;
  }

  /**
   * 
   * @return
   */
  public long getMillisLoadData() {
    return this.millisLoadData;
  }

  /**
   * 
   * @param millisLoadData1
   */
  public void setMillisLoadData(long millisLoadData1) {
    this.millisLoadData = millisLoadData1;
  }

  /**
   * total count of records
   */
  private int totalCount;
  
  /**
   * total count of records
   * @return the total count
   */
  public int getTotalCount() {
    return this.totalCount;
  }

  /**
   * total count of records
   * @param totalCount1
   */
  public void setTotalCount(int totalCount1) {
    this.totalCount = totalCount1;
  }

  /**
   * groups that were switched to
   */
  private Set<Object> switchedToGroups = null;
  
  /**
   * groups that were switched to
   * @return groups
   */
  public Set<Object> getSwitchedToGroups() {
    return this.switchedToGroups;
  }

  /**
   * groups that were switched to
   * @param switchedToGroups1
   */
  public void setSwitchedToGroups(Set<Object> switchedToGroups1) {
    this.switchedToGroups = switchedToGroups1;
  }

  /**
   * if there were so many records that switched to full sync
   */
  private boolean switchedToFull = false;
  
  /**
   * if there were so many records that switched to full sync
   * switchFromIncrementalToFullIfOverRecords
   * @return if switched to full
   */
  public boolean isSwitchedToFull() {
    return this.switchedToFull;
  }

  /**
   * if there were so many records that switched to full sync
   * @param switchedToFull1
   */
  public void setSwitchedToFull(boolean switchedToFull1) {
    this.switchedToFull = switchedToFull1;
  }

  /**
   * 
   */
  public GcTableSyncOutput() {
  }

  /**
   * rows selected from the FROM database
   */
  private int rowsSelectedFrom = 0;
  
  /**
   * rows selected from the FROM database
   * @return the rowsSelectedFrom
   */
  public int getRowsSelectedFrom() {
    return this.rowsSelectedFrom;
  }
  
  /**
   * rows selected from the FROM database
   * @param rowsSelectedFrom1 the rowsSelectedFrom to set
   */
  public void setRowsSelectedFrom(int rowsSelectedFrom1) {
    this.rowsSelectedFrom = rowsSelectedFrom1;
  }

  /**
   * add rows selected from the FROM database
   * @param rowsToAdd
   */
  public void addRowsSelectedFrom(int rowsToAdd) {
    this.rowsSelectedFrom += rowsToAdd;
  }
  
  /**
   * @return the queryCount
   */
  public int getQueryCount() {
    return this.queryCount;
  }


  
  /**
   * @param queryCount the queryCount to set
   */
  public void setQueryCount(int queryCount) {
    this.queryCount = queryCount;
  }

  /**
   * add to query count
   * @param amountToAdd
   */
  public synchronized void addQueryCount(int amountToAdd) {
    this.queryCount += amountToAdd;
  }
  
  
  /**
   * query count
   */
  private int queryCount = 0;
  
  /**
   * rows selected in the "to" database
   */
  private int rowsSelectedTo = 0;
  
  /**
   * rows selected in the "to" database
   * @return the rowsSelectedTo
   */
  public int getRowsSelectedTo() {
    return this.rowsSelectedTo;
  }
  
  /**
   * rows selected in the "to" database
   * @param rowsSelectedTo1 the rowsSelectedTo to set
   */
  public void setRowsSelectedTo(int rowsSelectedTo1) {
    this.rowsSelectedTo = rowsSelectedTo1;
  }

  /**
   * add rows selected to
   * @param rowsToAdd
   */
  public void addRowsSelectedTo(int rowsToAdd) {
    this.rowsSelectedTo += rowsToAdd;
  }

  /**
   * rows with equal data
   */
  private int rowsWithEqualData = 0;
  
  /**
   * rows with equal data
   * @return the rowsWithEqualData
   */
  public int getRowsWithEqualData() {
    return this.rowsWithEqualData;
  }
  
  /**
   * rows with equal data
   * @param rowsWithEqualData1 the rowsWithEqualData to set
   */
  public void setRowsWithEqualData(int rowsWithEqualData1) {
    this.rowsWithEqualData = rowsWithEqualData1;
  }

  /**
   * rows with equal data
   * @param rowsWithEqualDataToAdd
   */
  public void addRowsWithEqualData(int rowsWithEqualDataToAdd) {
    this.rowsWithEqualData += rowsWithEqualDataToAdd;
  }
  
  /**
   * rows with delete errors
   */
  private int rowsWithDeleteErrors = 0;
  
  /**
   * @return the rowsWithDeleteErrors
   */
  public int getRowsWithDeleteErrors() {
    return this.rowsWithDeleteErrors;
  }

  
  /**
   * @param rowsWithDeleteErrors the rowsWithDeleteErrors to set
   */
  public void setRowsWithDeleteErrors(int rowsWithDeleteErrors) {
    this.rowsWithDeleteErrors = rowsWithDeleteErrors;
  }

  /**
   * add rows with delete error
   * @param rowsWithDeleteToAdd
   */
  public void addRowsWithDeleteErrors(int rowsWithDeleteToAdd) {
    this.rowsWithDeleteErrors += rowsWithDeleteToAdd;
  }

  /**
   * rows with insert errors
   */
  private int rowsWithInsertErrors = 0;


  
  /**
   * rows with insert errors
   * @return the rowsWithInsertErrors
   */
  public int getRowsWithInsertErrors() {
    return this.rowsWithInsertErrors;
  }

  
  /**
   * rows with insert errors
   * @param rowsWithInsertErrors the rowsWithInsertErrors to set
   */
  public void setRowsWithInsertErrors(int rowsWithInsertErrors) {
    this.rowsWithInsertErrors = rowsWithInsertErrors;
  }

  /**
   * add rows with insert errors
   * @param insertErrorsToAdd
   */
  public void addRowsWithInsertErrors(int insertErrorsToAdd) {
    this.rowsWithInsertErrors += insertErrorsToAdd;
  }
  /**
   * rows with update errors
   */
  private int rowsWithUpdateErrors = 0;
  
  /**
   * rows with update errors
   * @return the rowsWithUpdateErrors
   */
  public int getRowsWithUpdateErrors() {
    return this.rowsWithUpdateErrors;
  }

  
  /**
   * rows with update errors
   * @param rowsWithUpdateErrors1 the rowsWithUpdateErrors to set
   */
  public void setRowsWithUpdateErrors(int rowsWithUpdateErrors1) {
    this.rowsWithUpdateErrors = rowsWithUpdateErrors1;
  }

  /**
   * add rows with update errors
   * @param rowsWithUpdateErrorsToAdd
   */
  public void addRowsWithUpdateErrors(int rowsWithUpdateErrorsToAdd) {
    this.rowsWithUpdateErrors += rowsWithUpdateErrorsToAdd;
  }

  /**
   * rows with primary key errors
   */
  private int rowsWithPrimaryKeyErrors = 0;

  /**
   * rows with primary key errors
   * @return the rowsWithPrimaryKeyErrors
   */
  public int getRowsWithPrimaryKeyErrors() {
    return this.rowsWithPrimaryKeyErrors;
  }

  
  /**
   * rows with primary key errors
   * @param rowsWithPrimaryKeyErrors1 the rowsWithPrimaryKeyErrors to set
   */
  public void setRowsWithPrimaryKeyErrors(int rowsWithPrimaryKeyErrors1) {
    this.rowsWithPrimaryKeyErrors = rowsWithPrimaryKeyErrors1;
  }

  /**
   * add rows with primary key errors
   * @param addRowsWithPrimaryKeyErrors
   */
  public void addRowsWithPrimaryKeyErrors(int addRowsWithPrimaryKeyErrors) {
    this.rowsWithPrimaryKeyErrors += addRowsWithPrimaryKeyErrors;
  }
  
  /**
   * total insert
   */
  private int insert;
  
  
  /**
   * @return the insert
   */
  public int getInsert() {
    return this.insert;
  }


  
  /**
   * @param insert1 the insert to set
   */
  public void setInsert(int insert1) {
    this.insert = insert1;
  }

  /**
   * add insert rows
   * @param insertToAdd
   */
  public void addInsert(int insertToAdd) {
    this.insert += insertToAdd;
  }
  
  /**
   * total update
   */
  private int update;
  
  
  /**
   * @return the update
   */
  public int getUpdate() {
    return this.update;
  }


  
  /**
   * @param update1 the update to set
   */
  public void setUpdate(int update1) {
    this.update = update1;
  }

  /**
   * add update count
   * @param updatesToAdd
   */
  public void addUpdate(int updatesToAdd) {
    this.update += updatesToAdd;
  }

  /**
   * total delete
   */
  private int delete;
  
  
  /**
   * @return the delete
   */
  public int getDelete() {
    return this.delete;
  }


  
  /**
   * @param delete1 the delete to set
   */
  public void setDelete(int delete1) {
    this.delete = delete1;
  }

  /**
   * add delete count
   * @param deletesToAdd
   */
  public void addDelete(int deletesToAdd) {
    this.delete += deletesToAdd;
  }
  
  /**
   * message
   */
  private String message;
  
  /**
   * @return the message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * @param message1 the message to set
   */
  public void setMessage(String message1) {
    this.message = message1;
  }
  
}
