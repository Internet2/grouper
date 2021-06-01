/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 *
 */
public class GrouperProvisioningOutput {

  
  
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
  private Set<ProvisioningGroup> switchedToGroups = null;
  
  /**
   * groups that were switched to
   * @return groups
   */
  public Set<ProvisioningGroup> getSwitchedToGroups() {
    return this.switchedToGroups;
  }

  /**
   * groups that were switched to
   * @param switchedToGroups1
   */
  public void setSwitchedToGroups(Set<ProvisioningGroup> switchedToGroups1) {
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
  public GrouperProvisioningOutput() {
  }

  /**
   * records selected from the from side
   */
  private int recordsSelectedFromOrigin = 0;
  
  /**
   * records selected from the FROM database
   * @return the recordsSelectedFrom
   */
  public int getRecordsSelectedFromOrigin() {
    return this.recordsSelectedFromOrigin;
  }
  
  /**
   * records selected from the FROM database
   * @param recordsSelectedFrom1 the recordsSelectedFrom to set
   */
  public void setRecordsSelectedFromOrigin(int recordsSelectedFrom1) {
    this.recordsSelectedFromOrigin = recordsSelectedFrom1;
  }

  /**
   * add records selected from the FROM database
   * @param recordsToAdd
   */
  public void addRecordsSelectedFromOrigin(int recordsToAdd) {
    this.recordsSelectedFromOrigin += recordsToAdd;
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
   * records selected in the "to" database
   */
  private int recordsSelectedTarget = 0;
  
  /**
   * records selected in the "to" database
   * @return the recordsSelectedTo
   */
  public int getRecordsSelectedTarget() {
    return this.recordsSelectedTarget;
  }
  
  /**
   * records selected in the "to" database
   * @param recordsSelectedTo1 the recordsSelectedTo to set
   */
  public void setRecordsSelectedTarget(int recordsSelectedTo1) {
    this.recordsSelectedTarget = recordsSelectedTo1;
  }

  /**
   * add records selected to
   * @param recordsToAdd
   */
  public void addRecordsSelectedTarget(int recordsToAdd) {
    this.recordsSelectedTarget += recordsToAdd;
  }

  /**
   * records with equal data
   */
  private int recordsWithEqualData = 0;
  
  /**
   * records with equal data
   * @return the recordsWithEqualData
   */
  public int getRecordsWithEqualData() {
    return this.recordsWithEqualData;
  }
  
  /**
   * records with equal data
   * @param recordsWithEqualData1 the recordsWithEqualData to set
   */
  public void setRecordsWithEqualData(int recordsWithEqualData1) {
    this.recordsWithEqualData = recordsWithEqualData1;
  }

  /**
   * records with equal data
   * @param recordsWithEqualDataToAdd
   */
  public void addRecordsWithEqualData(int recordsWithEqualDataToAdd) {
    this.recordsWithEqualData += recordsWithEqualDataToAdd;
  }
  
  /**
   * records with delete errors
   */
  private int recordsWithDeleteErrors = 0;

  /**
   * count of all errors
   * @return
   */
  public int getRecordsWithErrors() {
    return this.recordsWithDeleteErrors + this.recordsWithInsertErrors + this.recordsWithUpdateErrors + this.recordsWithPrimaryKeyErrors;
  }
  
  /**
   * @return the recordsWithDeleteErrors
   */
  public int getRecordsWithDeleteErrors() {
    return this.recordsWithDeleteErrors;
  }

  
  /**
   * @param recordsWithDeleteErrors the recordsWithDeleteErrors to set
   */
  public void setRecordsWithDeleteErrors(int recordsWithDeleteErrors) {
    this.recordsWithDeleteErrors = recordsWithDeleteErrors;
  }

  /**
   * add records with delete error
   * @param recordsWithDeleteToAdd
   */
  public void addRecordsWithDeleteErrors(int recordsWithDeleteToAdd) {
    this.recordsWithDeleteErrors += recordsWithDeleteToAdd;
  }

  /**
   * records with insert errors
   */
  private int recordsWithInsertErrors = 0;


  
  /**
   * records with insert errors
   * @return the recordsWithInsertErrors
   */
  public int getRecordsWithInsertErrors() {
    return this.recordsWithInsertErrors;
  }

  
  /**
   * records with insert errors
   * @param recordsWithInsertErrors the recordsWithInsertErrors to set
   */
  public void setRecordsWithInsertErrors(int recordsWithInsertErrors) {
    this.recordsWithInsertErrors = recordsWithInsertErrors;
  }

  /**
   * add records with insert errors
   * @param insertErrorsToAdd
   */
  public void addRecordsWithInsertErrors(int insertErrorsToAdd) {
    this.recordsWithInsertErrors += insertErrorsToAdd;
  }
  /**
   * records with update errors
   */
  private int recordsWithUpdateErrors = 0;
  
  /**
   * records with update errors
   * @return the recordsWithUpdateErrors
   */
  public int getRecordsWithUpdateErrors() {
    return this.recordsWithUpdateErrors;
  }

  
  /**
   * records with update errors
   * @param recordsWithUpdateErrors1 the recordsWithUpdateErrors to set
   */
  public void setRecordsWithUpdateErrors(int recordsWithUpdateErrors1) {
    this.recordsWithUpdateErrors = recordsWithUpdateErrors1;
  }

  /**
   * add records with update errors
   * @param recordsWithUpdateErrorsToAdd
   */
  public void addRecordsWithUpdateErrors(int recordsWithUpdateErrorsToAdd) {
    this.recordsWithUpdateErrors += recordsWithUpdateErrorsToAdd;
  }

  /**
   * records with primary key errors
   */
  private int recordsWithPrimaryKeyErrors = 0;

  /**
   * records with primary key errors
   * @return the recordsWithPrimaryKeyErrors
   */
  public int getRecordsWithPrimaryKeyErrors() {
    return this.recordsWithPrimaryKeyErrors;
  }

  
  /**
   * records with primary key errors
   * @param recordsWithPrimaryKeyErrors1 the recordsWithPrimaryKeyErrors to set
   */
  public void setRecordsWithPrimaryKeyErrors(int recordsWithPrimaryKeyErrors1) {
    this.recordsWithPrimaryKeyErrors = recordsWithPrimaryKeyErrors1;
  }

  /**
   * add records with primary key errors
   * @param addRecordsWithPrimaryKeyErrors
   */
  public void addRecordsWithPrimaryKeyErrors(int addRecordsWithPrimaryKeyErrors) {
    this.recordsWithPrimaryKeyErrors += addRecordsWithPrimaryKeyErrors;
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
   * add insert records
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

  private GrouperProvisioner grouperProvisioner = null;
  
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

  public void copyToHib3LoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    hib3GrouperLoaderLog.setDeleteCount(this.delete);
    hib3GrouperLoaderLog.setInsertCount(this.insert);
    hib3GrouperLoaderLog.setInsertCount(this.update);
    hib3GrouperLoaderLog.setTotalCount(this.totalCount);
  }

  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * increment a value in the debug map
   * @param key
   * @param value
   */
  public void debugMapAdd(String key, int value) {
    
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
    
    Integer currentValue = GrouperUtil.defaultIfNull((Integer)debugMap.get(key), 0);
    
    debugMap.put(key, currentValue + value);
    
  }
  
}
