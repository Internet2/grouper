package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

/**
 * sync details for a provisioner config
 */
public class ProvisionerConfigSyncDetails {
  
  /**
   * last full sync timestamp
   */
  private String lastFullSyncTimestamp;
  
  /**
   * last full sync start
   */
  private String lastFullSyncStartTimestamp;
  
  
  /**
   * last incremental sync timestamp
   */
  private String lastIncrementalSyncTimestamp;
  
  /**
   * last full metadata sync start timestamp
   */
  private String lastFullMetadataSyncStartTimestamp;
  
  /**
   * last full metadata sync timestamp
   */
  private String lastFullMetadataSyncTimestamp;
  
  /**
   * group count
   */
  private int groupCount;
  
  /**
   * user count
   */
  private int userCount;
  
  /**
   * records count
   */
  private int recordsCount;
  
  /**
   * number of validation errors for a given provisioner across groups, members, and memberships
   */
  private int validationErrorCount;
  
  /**
   * number of exceptions for a given provisioner across groups, members, and memberships
   */
  private int exceptionCount;
  
  /**
   * number of target errors for a given provisioner across groups, members, and memberships
   */
  private int targetErrorCount;
  
  /**
   * list of sync jobs for a provisioner config
   */
  private List<GrouperSyncJobWrapper> syncJobs = new ArrayList<GrouperSyncJobWrapper>();
  
  public String getLastFullSyncTimestamp() {
    return lastFullSyncTimestamp;
  }

  
  public void setLastFullSyncTimestamp(String lastFullSyncTimestamp) {
    this.lastFullSyncTimestamp = lastFullSyncTimestamp;
  }

  
  public String getLastIncrementalSyncTimestamp() {
    return lastIncrementalSyncTimestamp;
  }

  
  public void setLastIncrementalSyncTimestamp(String lastIncrementalSyncTimestamp) {
    this.lastIncrementalSyncTimestamp = lastIncrementalSyncTimestamp;
  }

  
  public int getGroupCount() {
    return groupCount;
  }

  
  public void setGroupCount(int groupCount) {
    this.groupCount = groupCount;
  }

  
  public int getUserCount() {
    return userCount;
  }

  
  public void setUserCount(int userCount) {
    this.userCount = userCount;
  }

  
  public int getRecordsCount() {
    return recordsCount;
  }

  
  public void setRecordsCount(int recordsCount) {
    this.recordsCount = recordsCount;
  }

  
  public List<GrouperSyncJobWrapper> getSyncJobs() {
    return syncJobs;
  }

  
  public void setSyncJobs(List<GrouperSyncJobWrapper> syncJobs) {
    this.syncJobs = syncJobs;
  }


  
  public String getLastFullSyncStartTimestamp() {
    return lastFullSyncStartTimestamp;
  }


  
  public void setLastFullSyncStartTimestamp(String lastFullSyncStartTimestamp) {
    this.lastFullSyncStartTimestamp = lastFullSyncStartTimestamp;
  }


  
  public String getLastFullMetadataSyncStartTimestamp() {
    return lastFullMetadataSyncStartTimestamp;
  }


  
  public void setLastFullMetadataSyncStartTimestamp(
      String lastFullMetadataSyncStartTimestamp) {
    this.lastFullMetadataSyncStartTimestamp = lastFullMetadataSyncStartTimestamp;
  }


  
  public String getLastFullMetadataSyncTimestamp() {
    return lastFullMetadataSyncTimestamp;
  }


  
  public void setLastFullMetadataSyncTimestamp(String lastFullMetadataSyncTimestamp) {
    this.lastFullMetadataSyncTimestamp = lastFullMetadataSyncTimestamp;
  }


  
  public int getValidationErrorCount() {
    return validationErrorCount;
  }


  
  public void setValidationErrorCount(int validationErrorCount) {
    this.validationErrorCount = validationErrorCount;
  }


  
  public int getExceptionCount() {
    return exceptionCount;
  }


  
  public void setExceptionCount(int exceptionCount) {
    this.exceptionCount = exceptionCount;
  }


  
  public int getTargetErrorCount() {
    return targetErrorCount;
  }


  
  public void setTargetErrorCount(int targetErrorCount) {
    this.targetErrorCount = targetErrorCount;
  }
  

}



