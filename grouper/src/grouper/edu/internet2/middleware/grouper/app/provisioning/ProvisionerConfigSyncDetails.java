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
   * last incremental sync timestamp
   */
  private String lastIncrementalSyncTimestamp;
  
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

}



